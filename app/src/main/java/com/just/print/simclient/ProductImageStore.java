package com.just.print.simclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Shared product-image lookup and background installer, compatible with Security. */
public final class ProductImageStore {
    private static final String DOWNLOAD_URL =
            "https://www.sharethegoodones.com/products/mobileSync/productImages";
    private static final long NUMERIC_OFFSET = 10754L;
    private static final String[] EXTENSIONS = {".png", ".jpg", ".jpeg", ".webp"};
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private ProductImageStore() {}

    public static File directory(Context context) {
        // Keep the same shared Windows location as Security when available.
        File shared = new File("C:\\AikaPos\\.Storm070111\\product");
        if (shared.exists() || shared.mkdirs()) return shared;
        File privateDir = new File(context.getFilesDir(), "product-images");
        if (!privateDir.exists()) privateDir.mkdirs();
        return privateDir;
    }

    /** The exact endpoint selected for the configured server. */
    public static String downloadUrl(String configuredServer) {
        return configuredServer == null || configuredServer.trim().isEmpty()
                ? DOWNLOAD_URL : websiteImageUrl(configuredServer);
    }

    public static String downloadSummary(Context context, String configuredServer) {
        return "URL=" + downloadUrl(configuredServer)
                + "; local path=" + directory(context).getAbsolutePath();
    }

    public static File find(Context context, String code) {
        if (code == null || code.trim().isEmpty()) return null;
        File dir = directory(context);
        for (String key : candidateKeys(code)) {
            for (String ext : EXTENSIONS) {
                File file = new File(dir, key + ext);
                if (file.isFile() && file.length() > 0) return file;
            }
        }
        return null;
    }

    public static File primaryFile(Context context, String code) {
        if (code == null || code.trim().isEmpty()) return null;
        return new File(directory(context), candidateKeys(code)[0] + ".jpg");
    }

    public static String primaryKey(String code) {
        String[] keys = candidateKeys(code);
        return keys.length == 0 ? "" : keys[0];
    }

    public static Bitmap decode(Context context, String code) {
        File file = find(context, code);
        return file == null ? null : BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    public static boolean hasAnyImage(Context context) {
        File dir = directory(context);
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase(java.util.Locale.ROOT);
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp");
        });
        return files != null && files.length > 0;
    }

    public static void downloadInBackground(Context context, Runnable completion) {
        downloadInBackground(context, null, error -> { if (completion != null) completion.run(); });
    }

    public static void downloadInBackground(Context context, String configuredServer, Runnable completion) {
        downloadInBackground(context, configuredServer, error -> { if (completion != null) completion.run(); });
    }

    public interface DownloadListener { void finished(Exception error); }
    public interface BatchListener { void batchFinished(int batch, int imageCount, int totalImages); }
    private static final class BatchResult {
        final int images, total;
        BatchResult(int images, int total) { this.images = images; this.total = total; }
    }

    public static void downloadInBackground(Context context, String configuredServer, DownloadListener listener) {
        downloadInBackground(context, configuredServer, listener, null);
    }

    public static void downloadInBackground(Context context, String configuredServer,
                                             DownloadListener listener, BatchListener batchListener) {
        final Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            Exception error = null;
            try { downloadAndMerge(app, configuredServer, batchListener); } catch (Exception ex) { error = ex; }
            final Exception result = error;
            if (listener != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> listener.finished(result));
            }
        });
    }

    private static void downloadAndMerge(Context context, String configuredServer, BatchListener batchListener) throws Exception {
        final String progressKey = "product_image_next_batch";
        int batch = context.getSharedPreferences("ProductImageStore", Context.MODE_PRIVATE)
                .getInt(progressKey, 0);
        while (true) {
            BatchResult result = null;
            Exception last = null;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try { result = downloadBatch(context, configuredServer, batch, 20); break; }
                catch (Exception ex) { last = ex; android.util.Log.w("ProductImages", "batch=" + batch + " attempt=" + attempt + " failed", ex); if (attempt < 3) try { Thread.sleep(attempt * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); } }
            }
            if (result == null) throw last;
            int received = result.images;
            if (received == 0) {
                context.getSharedPreferences("ProductImageStore", Context.MODE_PRIVATE)
                        .edit().remove(progressKey).apply();
                return;
            }
            if (batchListener != null) batchListener.batchFinished(batch, received, result.total);
            context.getSharedPreferences("ProductImageStore", Context.MODE_PRIVATE)
                    .edit().putInt(progressKey, batch + 1).apply();
            batch++;
        }
    }

    private static BatchResult downloadBatch(Context context, String configuredServer, int batch, int batchSize) throws Exception {
        File dir = directory(context);
        File temp = File.createTempFile("menu_images_" + batch + "_", ".jar", context.getCacheDir());
        String source = downloadUrl(configuredServer) + "?batch=" + batch + "&size=" + batchSize;
        android.util.Log.i("ProductImages", "configuredServer=" + configuredServer
                + ", isLan=" + isLanAddress(configuredServer) + ", source=" + source);
        HttpURLConnection connection = (HttpURLConnection) new URL(source).openConnection();
        connection.setConnectTimeout(20000); connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);
        // Activity.getPreferences() stores this activity's values under its
        // local class name. Keep the image request on that exact store so it
        // sees the token saved by SimClientActivity after activation.
        String token = context.getSharedPreferences("SimClientActivity", Context.MODE_PRIVATE)
                .getString("token", "").trim();
        android.util.Log.i("ProductImages", "authorizationTokenPresent=" + !token.isEmpty());
        // Toastdale's mobileSession() reads this exact header. Do not use
        // Authorization: Bearer here; that header is not used by this API.
        if (!token.isEmpty()) connection.setRequestProperty("X-Mobile-Sync-Token", token);
        try {
            int responseCode = connection.getResponseCode();
            android.util.Log.i("ProductImages", "HTTP=" + responseCode
                    + ", responseUrl=" + connection.getURL()
                    + ", length=" + connection.getContentLengthLong()
                    + ", contentType=" + connection.getContentType());
            if (responseCode / 100 != 2) {
                throw new IOException("HTTP " + responseCode + " from " + connection.getURL());
            }
            int totalImages = connection.getHeaderFieldInt("X-Image-Shared-Total", -1);
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp))) {
                byte[] buffer = new byte[8192]; int n;
                while ((n = in.read(buffer)) >= 0) { if (n > 0) out.write(buffer, 0, n); }
            }
            android.util.Log.i("ProductImages", "downloadedBytes=" + temp.length());
            int imageEntries = 0;
            int allEntries = 0;
            StringBuilder entrySummary = new StringBuilder();
            try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(temp)))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    allEntries++;
                    String name = entry.getName().replace('\\', '/');
                    if (entrySummary.length() < 600) {
                        if (entrySummary.length() > 0) entrySummary.append(", ");
                        entrySummary.append(name);
                    }
                    if (name.startsWith("menu_images/")) name = name.substring(12);
                    if (entry.isDirectory() || name.isEmpty() || name.startsWith("META-INF/") || name.contains("..")) continue;
                    String lowerName = name.toLowerCase(java.util.Locale.ROOT);
                    if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp")) {
                        imageEntries++;
                        if (imageEntries <= 10) android.util.Log.i("ProductImages", "zipEntry=" + name);
                    }
                    File target = new File(dir, name).getCanonicalFile();
                    if (!target.getPath().startsWith(dir.getCanonicalPath() + File.separator) || target.exists()) continue;
                    File parent = target.getParentFile(); if (parent != null) parent.mkdirs();
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target))) {
                        byte[] buffer = new byte[8192]; int n;
                        while ((n = zip.read(buffer)) >= 0) { if (n > 0) out.write(buffer, 0, n); }
                    }
                }
            }
            android.util.Log.i("ProductImages", "zipEntries=" + allEntries
                    + ", imageEntries=" + imageEntries + ", names=" + entrySummary
                    + ", installDir=" + dir.getAbsolutePath());
            if (imageEntries == 0) return new BatchResult(0, totalImages);
            return new BatchResult(imageEntries, totalImages);
        } finally { connection.disconnect(); if (!temp.delete()) temp.deleteOnExit(); }
    }

    private static boolean isLanAddress(String value) {
        if (value == null) return false;
        String s = value.trim().toLowerCase(java.util.Locale.ROOT);
        return s.matches("https?://(10\\.|192\\.168\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.).*")
                || s.matches("(10\\.|192\\.168\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.).*");
    }

    private static String lanImageUrl(String value) {
        String s = value.trim();
        if (!s.matches("^[a-zA-Z]+://.*")) s = "http://" + s;
        if (!s.matches("^[a-zA-Z]+://[^/:]+:[0-9]+.*")) s += ":8880";
        return s.replaceAll("/+$", "") + "/AikaPos/productImages";
    }

    private static String websiteImageUrl(String value) {
        String s = value.trim().replaceAll("/+$", "");
        if (!s.matches("^[a-zA-Z]+://.*")) s = "https://" + s;
        if (isLanAddress(s)) return s + "/AikaPos/productImages";
        return s + "/products/mobileSync/productImages";
    }

    private static String[] candidateKeys(String code) {
        String sanitized = code.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();
        if (sanitized.matches("\\d+")) keys.add("m_" + new BigInteger(sanitized).add(BigInteger.valueOf(NUMERIC_OFFSET)));
        else keys.add("m_x_" + sanitized);
        keys.add(legacyXor(sanitized)); keys.add(sanitized);
        return keys.toArray(new String[0]);
    }

    private static String legacyXor(String value) {
        byte[] src = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] salt = "AikaPos_Menu_Image".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        StringBuilder out = new StringBuilder("m_");
        for (int i = 0; i < src.length; i++) { int x = (src[i] ^ salt[i % salt.length]) & 255; if (x < 16) out.append('0'); out.append(Integer.toHexString(x)); }
        return out.toString();
    }
}
