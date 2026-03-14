package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;

public class PullAction
    implements IActions
{
    private IProject project;
    private Shell shell;

    public PullAction(IProject project, Shell shell)
    {
        this.project = project;
        this.shell = shell;
    }

    @Override
    public void run()
    {
        ScriptRunnerJob job = new ScriptRunnerJob(this, project, shell);
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

    @Override
    public String scriptEngine()
    {
        return "oscript"; //$NON-NLS-1$
    }

}
