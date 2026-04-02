package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.osgi.util.NLS;

public class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.sdp.edt.internal.v8storage.ui.messages"; //$NON-NLS-1$

    public static String pullCommandName;
    public static String pullCommandLabel;
    public static String pullCommandTooltip;

    public static String pushCommandName;
    public static String pushCommandLabel;
    public static String pushCommandTooltip;

    public static String Error_NoActiveProject;
    public static String Error_NotGitRepository;
    public static String Error_NoHeadCommit;
    public static String Error_Exception;
    public static String Error_NoParentCommit;

    public static String PushDialog_Header;
    public static String PushDialog_Title;
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

    public static String ScriptRunnerJob_ErrorReading;
    public static String ScriptRunnerJob_UserCancel;
    public static String ScriptRunnerJob_ErrorCode;
    public static String ScriptRunnerJob_Done;
    public static String ScriptRunnerJob_ErrorDuringExecution;
    public static String ScriptRunnerJob_Pull;
    public static String ScriptRunnerJob_Push;

    public static String Application_Error;

    static
    {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
