package com.just.print.simclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Button;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Local OCR only. No image, raw OCR, or birth date is sent to the POS or persisted. */
public class IdCheckingPage {
    private final Activity activity;
    private final Runnable stateChanged;
    private final java.util.function.Supplier<List<PhotoProductMatcher.Item>> products;
    private final java.util.function.Consumer<Integer> openProduct;
    private final java.util.function.Consumer<String> openProductSearch;
    private List<PhotoProductMatcher.Item> capturedProducts;
    private static final int CAPTURE = 601;
    private final ExecutorService decoder = Executors.newSingleThreadExecutor();
    private TextRecognizer recognizer;
    private TextView status;
    private LinearLayout results;

    private File photo;
    private boolean cameraPending, recognizing;
    private volatile boolean closed;
    private LocalDate displayedBirth;
    private EditText yearInput, monthInput, dayInput;
    private TextView manualAge;

    public IdCheckingPage(Activity activity, Runnable stateChanged,
                          java.util.function.Supplier<List<PhotoProductMatcher.Item>> products,
                          java.util.function.Consumer<Integer> openProduct,
                          java.util.function.Consumer<String> openProductSearch) {
        this.activity = activity;
        this.stateChanged = stateChanged;
        this.products = products;
        this.openProduct = openProduct;
        this.openProductSearch = openProductSearch;
    }

    public android.view.View createView(Bundle saved) {
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        root.setGravity(android.view.Gravity.CENTER);
        root.setBackground(new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xfff0f8fa, 0xffffffff}));
        scroll.addView(root);
        status = text("Tap the camera to photograph a product or ID.", 17);
        status.setAccessibilityLiveRegion(android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE);
        root.addView(status);
        results = new LinearLayout(activity);
        results.setOrientation(LinearLayout.VERTICAL);
        results.setGravity(android.view.Gravity.CENTER);
        root.addView(results, new LinearLayout.LayoutParams(-1, -2));
        if (saved != null && saved.getBoolean("cameraPending")) {
            String name = saved.getString("captureName", "");
            if (name.matches("id-[a-zA-Z0-9-]+\\.jpg")) {
                photo = new File(captureDirectory(), name);
                cameraPending = true;
                stateChanged.run();
                showStatus("Waiting for camera…");
            }
        }
        removeStalePhotos();
        return scroll;
    }

    private LocalDate today() { return LocalDate.now(ZoneId.of("America/Toronto")); }
    private int dp(int value) { return Math.round(value * activity.getResources().getDisplayMetrics().density); }
    private TextView text(String value, int size) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setGravity(android.view.Gravity.CENTER);
        view.setPadding(0, dp(8), 0, dp(8));
        return view;
    }
    private File captureDirectory() { return new File(activity.getCacheDir(), "age-check"); }
    private void clearResult() {
        displayedBirth = null;
        yearInput = monthInput = dayInput = null;
        manualAge = null;

        results.removeAllViews();
    }

    public void capture() {
        if (cameraPending || recognizing || closed) return;
        clearResult();
        capturedProducts = products.get();
        deletePhoto();
        try {
            File directory = captureDirectory();
            if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("No cache directory");
            photo = File.createTempFile("id-", ".jpg", directory);
            Intent intent = new Intent(activity, CameraCaptureActivity.class);
            intent.putExtra(CameraCaptureActivity.EXTRA_OUTPUT_PATH, photo.getAbsolutePath());
            cameraPending = true;
            stateChanged.run();
            showStatus("Waiting for camera…");
            activity.startActivityForResult(intent, CAPTURE);
        } catch (Exception exception) {
            cameraPending = false;
            deletePhoto();
            stateChanged.run();
            showRecovery("Camera unavailable. Search for a product or enter a birth date.");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE) return;
        cameraPending = false;
        if (resultCode != Activity.RESULT_OK || photo == null || photo.length() == 0) {
            deletePhoto();
            stateChanged.run();
            showRecovery("No photo captured. Search for a product or enter a birth date.");
            return;
        }
        recognizing = true;
        showStatus("Reading photo…");
        final File captured = photo;
        decoder.execute(() -> {
            try {
                Bitmap bitmap = decode(captured);
                activity.runOnUiThread(() -> recognize(bitmap));
            } catch (Exception | OutOfMemoryError failure) {
                activity.runOnUiThread(() -> recognitionFailed());
            }
        });
    }

    private static Bitmap decode(File file) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("Invalid photo");
        options.inSampleSize = 1;
        while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > 3200) options.inSampleSize *= 2;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (bitmap == null) throw new IOException("Cannot decode photo");
        int orientation = new ExifInterface(file.getAbsolutePath()).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1, -1); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(270); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(270); break;
            default: return bitmap;
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotated != bitmap) bitmap.recycle();
        return rotated;
    }

    private void recognize(Bitmap bitmap) {
        if (closed) { bitmap.recycle(); deletePhoto(); return; }
        try {
            com.google.mlkit.vision.barcode.BarcodeScanner scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient(
                    new com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder().setBarcodeFormats(
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8,
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A,
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E,
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF,
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128).build());
            scanner.process(InputImage.fromBitmap(bitmap, 0)).addOnCompleteListener(task -> {
                scanner.close();
                java.util.List<String> codes = new java.util.ArrayList<>();
                if (task.isSuccessful()) for (com.google.mlkit.vision.barcode.common.Barcode code : task.getResult()) {
                    if (code.getRawValue() != null && code.getRawValue().matches("[0-9]{6,14}")) codes.add(code.getRawValue());
                }
                if (!codes.isEmpty() && !closed) {
                    bitmap.recycle();
                    finishRecognition();
                    showBarcodeResult(codes, true, "Decoded barcode:\n" + String.join("\n", codes));
                } else recognizeText(bitmap);
            });
        } catch (Exception failure) { recognizeText(bitmap); }
    }

    private void recognizeText(Bitmap bitmap) {
        if (closed) { bitmap.recycle(); deletePhoto(); return; }
        try {
            recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnCompleteListener(task -> {
                        bitmap.recycle();
                        finishRecognition();
                        if (closed) return;
                        if (task.isSuccessful()) handlePhotoText(task.getResult().getText());
                        else showUnknownAsId("Could not read the photo. Assuming it is an ID.");
                    });
        } catch (Exception failure) {
            bitmap.recycle();
            recognitionFailed();
        }
    }

    private void recognitionFailed() {
        if (!closed) showUnknownAsId("Could not read the photo. Assuming it is an ID.");
        finishRecognition();
    }
    private void finishRecognition() {
        recognizing = false;
        deletePhoto();
        if (recognizer != null) { recognizer.close(); recognizer = null; }
        if (!closed) stateChanged.run();
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisibility(android.view.View.VISIBLE);
    }

    private void handlePhotoText(String value) {
        List<BirthDateParser.Candidate> dates = BirthDateParser.extract(value, today());
        boolean identity = PhotoKindClassifier.hasIdentityEvidence(value,
                dates.stream().anyMatch(date -> date.birthLabel));
        if (identity) { showCandidates(dates); return; }
        List<String> printedCodes = BarcodeProductMatcher.printedCodes(value);
        if (!printedCodes.isEmpty()) {
            showBarcodeResult(printedCodes, false, value);
            return;
        }
        List<PhotoProductMatcher.Item> catalog = capturedProducts == null ? products.get() : capturedProducts;
        List<PhotoProductMatcher.Match> matches = PhotoProductMatcher.match(value, catalog);
        if (matches.size() == 1 && matches.get(0).exact) {
            clearResult();
            showStatus("Product found: " + matches.get(0).item.name);
            openProduct.accept(matches.get(0).item.id);
        } else if (!matches.isEmpty()) {
            clearResult();
            showStatus("Text recognized, but the product is uncertain. Choose a matching product.");
            showRecognizedText(value);
            results.addView(action("Choose product (" + matches.size() + ")", () -> chooseProduct(matches)));
            results.addView(action("This is an ID", () -> showCandidates(dates)));
            chooseProduct(matches);
        } else {
            if (PhotoKindClassifier.hasBeerEvidence(value, false, false)) {
                openProductSearch.accept(bestProductQuery(value));
            } else {
                showUnknownAsId(value.trim().isEmpty()
                        ? "No text detected. Assuming it is an ID."
                        : "Could not tell whether this is beer or an ID. Assuming it is an ID.");
            }
        }
    }

    private void showRecognizedText(String value) {
        TextView label = text("Recognized text", 16);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        results.addView(label);
        TextView raw = text(value.trim().isEmpty() ? "(No text detected)" : value, 16);
        raw.setTextIsSelectable(true);
        raw.setSaveEnabled(false);
        raw.setPadding(dp(12), dp(12), dp(12), dp(12));
        raw.setBackgroundColor(0xffe4f1f5);
        results.addView(raw, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showBarcodeResult(List<String> codes, boolean decoded, String raw) {
        List<BarcodeProductMatcher.Match> matches = BarcodeProductMatcher.match(codes,
                capturedProducts == null ? products.get() : capturedProducts);
        if (decoded && matches.size() == 1 && matches.get(0).exact) {
            clearResult();
            showStatus("Barcode matched: " + codes.get(0));
            openProduct.accept(matches.get(0).item.id);
            return;
        }
        if (matches.isEmpty()) {
            openProductSearch.accept(codes.get(0));
            return;
        }
        showRecovery("Barcode / printed digits: " + String.join(", ", codes)
                + ". Confirm the product below.", raw);
        if (!matches.isEmpty()) {
            Runnable choose = () -> {
                String[] labels = new String[matches.size()];
                for (int i = 0; i < matches.size(); i++) {
                    BarcodeProductMatcher.Match match = matches.get(i);
                    labels[i] = match.item.name + "\n" + match.item.code + " · " + match.item.category
                            + (match.exact ? "" : " · Partial barcode");
                }
                new AlertDialog.Builder(activity).setTitle("Confirm barcode match")
                        .setItems(labels, (dialog, index) -> openProduct.accept(matches.get(index).item.id))
                        .setNegativeButton("Cancel", null).show();
            };
            results.addView(action("Choose product (" + matches.size() + ")", choose));
            choose.run();
        }
    }

    private String bestProductQuery(String value) {
        String[] lines = value.split("\\R");
        for (String line : lines) {
            String candidate = line.trim().replaceAll("\\s+", " ");
            if (candidate.matches("(?i).*[A-ZÀ-ÿ].*") && candidate.length() >= 3 && candidate.length() <= 40
                    && !candidate.matches("(?i).*(BEER|BI[EÈ]RE|LAGER|ALE|ALC|VOL|ML|CL|CAN|BOTTLE).*"))
                return candidate;
        }
        return "";
    }

    private void showUnknownAsId(String message) {
        showManualEntry(message + " Enter the birth date below.", null);
        results.addView(action("This is beer — search products", () -> openProductSearch.accept("")));
    }

    private Button action(String label, Runnable callback) {
        Button button = new Button(activity);
        button.setText(label); button.setAllCaps(false);
        button.setOnClickListener(view -> callback.run());
        return button;
    }

    private void chooseProduct(List<PhotoProductMatcher.Match> matches) {
        String[] labels = new String[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            PhotoProductMatcher.Item item = matches.get(i).item;
            labels[i] = item.name + "\n" + item.category + " · " + (item.code.isEmpty() ? item.id : item.code);
        }
        new AlertDialog.Builder(activity).setTitle(matches.get(0).item.preferred
                ? "Products in selected category" : "Products in other categories")
                .setItems(labels, (dialog, position) -> openProduct.accept(matches.get(position).item.id))
                .setNegativeButton("Cancel", null).show();
    }

    private void showRecovery(String message) {
        showRecovery(message, null);
    }

    private void showRecovery(String message, String recognizedText) {
        clearResult();
        showStatus(message);
        if (recognizedText != null) showRecognizedText(recognizedText);
        EditText query = new EditText(activity);
        query.setHint("Product barcode or name");
        query.setSingleLine(true);
        selectAllOnFocus(query);
        query.setSaveEnabled(false);
        results.addView(query, new LinearLayout.LayoutParams(-1, -2));
        results.addView(action("Find product", () -> {
            List<String> codes = BarcodeProductMatcher.printedCodes(query.getText().toString());
            if (!codes.isEmpty()) { showBarcodeResult(codes, false, query.getText().toString()); return; }
            List<PhotoProductMatcher.Match> matches = PhotoProductMatcher.match(query.getText().toString(), products.get());
            if (matches.isEmpty()) showStatus("No matches. Try another name or refresh the product catalog.");
            else chooseProduct(matches);
        }));
        results.addView(action("Enter birth date", () -> showManualEntry("Enter birth date", null)));
    }

    private void showCandidates(List<BirthDateParser.Candidate> candidates) {
        clearResult();
        BirthDateParser.Candidate selected = null;
        int labelled = 0;
        for (BirthDateParser.Candidate candidate : candidates) {
            if (candidate.birthLabel) { selected = candidate; labelled++; }
        }
        if (labelled == 1) {
            showAge(selected.date);
        } else if (labelled == 0 && candidates.size() == 1) {
            showAge(candidates.get(0).date);
        } else {
            showManualEntry("Birth date unclear. Enter it below.", null);
        }
    }

    private void showAge(LocalDate birth) {
        displayedBirth = birth;
        results.removeAllViews();
        status.setText("");
        status.setVisibility(android.view.View.GONE);
        TextView birthLabel = text("Birth Date: " + birth, 24);
        birthLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_edit, 0);
        birthLabel.setCompoundDrawablePadding(dp(8));
        birthLabel.setTooltipText("Tap to correct birth date");
        birthLabel.setContentDescription("Birth date " + birth + ". Tap to correct.");
        birthLabel.setOnClickListener(v -> showManualEntry("Correct birth date", birth));
        results.addView(birthLabel);
        TextView age = text(String.valueOf(BirthDateParser.age(birth, today())), 96);
        age.setTypeface(null, android.graphics.Typeface.BOLD);
        age.setTextColor(0xff167d8d);
        age.setContentDescription(age.getText() + " years old");
        results.addView(age);
    }

    private void showManualEntry(String message, LocalDate initial) {
        clearResult();
        showStatus(message);
        LinearLayout fields = new LinearLayout(activity);
        yearInput = dateField(fields, "Year", "YYYY", 4);
        monthInput = dateField(fields, "Month", "MM", 2);
        dayInput = dateField(fields, "Day", "DD", 2);
        results.addView(fields, new LinearLayout.LayoutParams(-1, -2));
        manualAge = text("—", 64);
        manualAge.setTextColor(0xff167d8d);
        manualAge.setTypeface(null, android.graphics.Typeface.BOLD);
        manualAge.setAccessibilityLiveRegion(android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE);
        results.addView(manualAge);
        if (initial != null) {
            yearInput.setText(String.valueOf(initial.getYear()));
            monthInput.setText(String.valueOf(initial.getMonthValue()));
            dayInput.setText(String.valueOf(initial.getDayOfMonth()));
        }
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable value) { updateManualAge(); }
        };
        for (EditText field : new EditText[]{yearInput, monthInput, dayInput}) field.addTextChangedListener(watcher);
        updateManualAge();
    }

    private EditText dateField(LinearLayout parent, String label, String hint, int digits) {
        LinearLayout column = new LinearLayout(activity);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(4), 0, dp(4), 0);
        column.addView(text(label, 14));
        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        selectAllOnFocus(input);
        input.setSaveEnabled(false);
        input.setHint(hint);
        input.setContentDescription("Birth " + label.toLowerCase(java.util.Locale.ROOT));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(digits)});
        input.setGravity(android.view.Gravity.CENTER);
        input.setTextSize(22);
        input.setMinimumHeight(dp(52));
        input.setImeOptions(label.equals("Day") ? android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                : android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        column.addView(input, new LinearLayout.LayoutParams(-1, -2));
        parent.addView(column, new LinearLayout.LayoutParams(0, -2, digits == 4 ? 1.4f : 1));
        return input;
    }

    private void selectAllOnFocus(EditText input) {
        input.setSelectAllOnFocus(true);
        input.setOnFocusChangeListener((view, focused) -> {
            if (focused) input.post(() -> { if (input.hasFocus()) input.selectAll(); });
        });
    }

    private void updateManualAge() {
        if (manualAge == null) return;
        manualAge.setText("—");
        manualAge.setContentDescription("Age not yet available");
        for (EditText field : new EditText[]{yearInput, monthInput, dayInput}) field.setError(null);
        String year = yearInput.getText().toString();
        String month = monthInput.getText().toString();
        String day = dayInput.getText().toString();
        if (year.length() != 4) return;
        EditText invalidField = yearInput;
        try {
            int y = Integer.parseInt(year);
            PartialBirthDate.ages(y, null, null, today());
            Integer m = month.isEmpty() ? null : Integer.valueOf(month);
            if (m != null) {
                invalidField = monthInput;
                PartialBirthDate.ages(y, m, null, today());
            }
            invalidField = dayInput;
            Integer d = day.isEmpty() ? null : Integer.valueOf(day);
            int[] ages = PartialBirthDate.ages(y, m, d, today());
            manualAge.setText(ages[0] == ages[1] ? String.valueOf(ages[0]) : ages[0] + "–" + ages[1]);
            manualAge.setContentDescription(manualAge.getText() + " years old");
        } catch (java.time.DateTimeException | IllegalArgumentException invalid) {
            invalidField.setError("Enter a valid birth date, not in the future.");
        }
    }

    public void activate() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if (manualAge != null) updateManualAge();
        else if (displayedBirth != null) showAge(displayedBirth);
    }
    public void deactivate() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
    public boolean isBusy() { return cameraPending || recognizing; }
    public void onSaveInstanceState(Bundle state) {
        state.putBoolean("cameraPending", cameraPending);
        if (cameraPending && photo != null) state.putString("captureName", photo.getName());
        // Never put OCR, photos, or DOB into saved state.
    }
    private void deletePhoto() {
        if (photo != null) { photo.delete(); photo = null; }
    }
    private void removeStalePhotos() {
        File[] files = captureDirectory().listFiles();
        if (files == null) return;
        long cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
        for (File file : files) {
            if (file.getName().startsWith("id-") && !file.equals(photo) && file.lastModified() < cutoff) file.delete();
        }
    }
    public void dispose() {
        closed = true;
        decoder.shutdown();
        // Let an in-flight decode/OCR complete before releasing its file/bitmap.
        if (!recognizing && !cameraPending) deletePhoto();
    }
}
