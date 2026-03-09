package com.sdp.edt.internal.v8storage.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.sdp.edt.internal.v8storage.preferences.messages"; //$NON-NLS-1$

    public static String V8StoragePreferencePage_PageName;
    public static String V8StoragePreferencePage_Description;
    public static String V8StoragePreferencePage_ScriptPath;
    public static String V8StoragePreferencePage_InvalidPath;
    public static String V8StoragePreferencePage_Error;

    static
    {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
