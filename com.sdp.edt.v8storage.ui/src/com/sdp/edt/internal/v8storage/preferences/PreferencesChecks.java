package com.sdp.edt.internal.v8storage.preferences;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class PreferencesChecks
{
    public static boolean FileExistsUI(String path, Shell shell)
    {
        File file = new File(path);
        if (isFile(file))
        {
            return true;
        }
        else
        {
            MessageDialog.openError(shell, Messages.V8StoragePreferencePage_Error,
                Messages.V8StoragePreferencePage_InvalidPath);
            return false;
        }
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
