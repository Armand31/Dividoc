package com.insalyon.dividoc;

import android.app.Activity;
import android.webkit.MimeTypeMap;

public class FileSelectActivity extends Activity {

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
