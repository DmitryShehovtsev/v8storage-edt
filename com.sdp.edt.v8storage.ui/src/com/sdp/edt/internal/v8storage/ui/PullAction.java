package com.sdp.edt.internal.v8storage.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.sdp.edt.internal.v8storage.Activator;

public class PullAction
    implements IActions
{
    private IProject project;

    public PullAction(IProject project)
    {
        this.project = project;
    }

    @Override
    public void run()
    {
        ScriptRunnerJob job = new ScriptRunnerJob(this, project);
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

    @Override
    public IStatus beforeRunJob(IProgressMonitor subMonitor) throws InvocationTargetException
    {
        IStatus status = new Status(IStatus.OK, Activator.PLUGIN_ID, "", null); //$NON-NLS-1$
        return status;
    }

}
