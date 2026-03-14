package com.sdp.edt.internal.v8storage.preferences;

import java.io.File;

public class PreferencesChecks
{
    public static boolean FileExistsUI(String path)
    {
        File file = new File(path);
        return isFile(file);
    }

    public static boolean isFile(File file)
    {
        return file.exists() && file.isFile();
    }

    public static String messageInvalidPath()
    {
        return Messages.V8StoragePreferencePage_InvalidPath;
    }
}
