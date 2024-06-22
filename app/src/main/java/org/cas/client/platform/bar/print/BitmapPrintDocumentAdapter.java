package org.cas.client.platform.bar.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class BitmapPrintDocumentAdapter extends PrintDocumentAdapter {
    private Context context;
    private Bitmap bitmap;

    public BitmapPrintDocumentAdapter(Context context, Bitmap bitmap) {
        this.context = context;
        this.bitmap = bitmap;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo info = new PrintDocumentInfo
                .Builder("print_output.png")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO)
                .setPageCount(1)
                .build();

        boolean changed = !newAttributes.equals(oldAttributes);
        callback.onLayoutFinished(info, changed);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(destination.getFileDescriptor());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } catch (Exception e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
    }
}
