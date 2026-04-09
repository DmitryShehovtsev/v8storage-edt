package com.sdp.edt.v8storagesync.ui;

import org.eclipse.osgi.util.NLS;

public class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.sdp.edt.v8storagesync.ui.messages"; //$NON-NLS-1$

    public static String Error_NoActiveProject;
    public static String Error_NoHeadCommit;

    public static String PushDialog_Header;
    public static String PushDialog_hashLabel;
    public static String PushDialog_messageLabel;
    public static String PushDialog_check;
    public static String PushDialog_checkMessage;
    public static String PushDialog_Description;

    public static String PushAction_Updated;
    public static String PushAction_NeedsRepeated;
    public static String PushAction_UnexpectedValue;
    public static String PushAction_ConfigDumpSuccess;
    public static String PushAction_ConfigDumpError;
    public static String PushAction_ApplicationNotFound;
    public static String PushAction_DumpingConf;

    public static String ScriptRunnerJob_ErrorReading;
    public static String ScriptRunnerJob_UserCancel;
    public static String ScriptRunnerJob_ErrorCode;
    public static String ScriptRunnerJob_Done;
    public static String ScriptRunnerJob_ErrorDuringExecution;
    public static String ScriptRunnerJob_HeaderPull;
    public static String ScriptRunnerJob_HeaderPush;

    public static String GitActions_CommitIDEmpty;
    public static String GitActions_CommitIDNotFound;
    public static String GitActions_CommitIDInvalid;
    public static String GitActions_CheckoutFailed;
    public static String GitActions_RefreshFailed;

    static
    {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
