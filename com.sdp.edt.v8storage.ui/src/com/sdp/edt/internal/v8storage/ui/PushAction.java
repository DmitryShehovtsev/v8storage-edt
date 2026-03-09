package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.swt.widgets.Shell;

public class PushAction
    implements IActions
{
    private Shell shell;
    private String hash;
    private String commitMessage;

    public PushAction(Shell shell)
    {
        //
    }

    @Override
    public void run()
    {
        ICommitHandler callback = (hash, commitMessage) -> {
            ScriptRunnerJob job = new ScriptRunnerJob(this, shell);
            job.schedule();
        };

        PushDialog dialog = new PushDialog(shell, callback);
        dialog.open();
    }

    @Override
    public String header()
    {
        return Messages.ScriptRunnerJob_Push;
    }

    @Override
    public String command()
    {
        return String.format("v8storage push -f -h \"%s\" -m \"%s\"", hash, commitMessage); //$NON-NLS-1$
    }
}
