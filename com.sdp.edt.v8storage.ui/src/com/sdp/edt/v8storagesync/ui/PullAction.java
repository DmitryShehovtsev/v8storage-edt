package com.sdp.edt.v8storagesync.ui;

public class PullAction
    extends AbstractActions
{
    @Override
    public void run()
    {
        ScriptRunnerJob job = new ScriptRunnerJob(this);
        job.schedule();
    }

    @Override
    public String header()
    {
        return Messages.ScriptRunnerJob_HeaderPull;
    }

    @Override
    public String command()
    {
        return "v8storage pull"; //$NON-NLS-1$
    }
}
