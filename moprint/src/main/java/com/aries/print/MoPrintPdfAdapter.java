package com.aries.print;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author crazy.mo
 */
public class MoPrintPdfAdapter extends PrintDocumentAdapter {
    private String mFilePath;

    public MoPrintPdfAdapter(String file) {
        this.mFilePath = file;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, Bundle extras) {
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }
        PrintDocumentInfo info = new PrintDocumentInfo.Builder(getJobName())
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build();
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal,
                        WriteResultCallback callback) {
        InputStream input = null;
        OutputStream output = null;

        try {

            input = new FileInputStream(mFilePath);
            output = new FileOutputStream(destination.getFileDescriptor());

            byte[] buf = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }

            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

        } catch (FileNotFoundException e) {
            // Catch exception
            e.printStackTrace();
        } catch (Exception e) {
            // Catch exception
            e.printStackTrace();
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return
     */
    private String getJobName() {
        try {
            String[] filePaths = mFilePath.split(File.separator);
            return filePaths[filePaths.length - 1];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.valueOf(System.currentTimeMillis());
    }
}
