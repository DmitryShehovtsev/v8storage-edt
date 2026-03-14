package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class PushAction
    implements IActions
{
    private String hash;
    private String commitMessage;
    private IProject project;
    private Shell shell;

    public PushAction(IProject project, Shell shell)
    {
        this.project = project;
        this.shell = shell;
    }

    @Override
    public void run()
    {
        ICommitHandler callback = (hash, commitMessage) -> {
            ScriptRunnerJob job = new ScriptRunnerJob(this, project, shell);
            job.schedule();
        };

        Shell parentShell = Display.getCurrent().getActiveShell();
        PushDialog.show(parentShell, callback);
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

    @Override
    public String scriptEngine()
    {
        return "oscript"; //$NON-NLS-1$
    }
}
