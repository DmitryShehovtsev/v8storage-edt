package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.swt.widgets.Shell;

public class PullAction
    implements IActions
{
    private Shell shell;

    public PullAction(Shell shell)
    {
        //
    }

    @Override
    public void run()
    {
        ScriptRunnerJob job = new ScriptRunnerJob(this, shell);
        job.schedule();
    }

    @Override
    public String header()
    {
        return Messages.ScriptRunnerJob_Pull;
    }

    @Override
    public String command()
    {
        return "v8storage pull"; //$NON-NLS-1$
    }

}
