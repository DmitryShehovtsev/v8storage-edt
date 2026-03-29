package com.sdp.edt.internal.v8storage.ui;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com._1c.g5.wiring.ServiceAccess;
import com._1c.g5.wiring.ServiceSupplier;
import com.e1c.g5.dt.applications.ApplicationException;
import com.e1c.g5.dt.applications.ApplicationUpdateState;
import com.e1c.g5.dt.applications.ApplicationUpdateType;
import com.e1c.g5.dt.applications.ExecutionContext;
import com.e1c.g5.dt.applications.IApplication;
import com.e1c.g5.dt.applications.IApplicationManager;
import com.sdp.edt.internal.v8storage.Activator;

public class PushAction
    implements IActions
{
    private String hash;
    private String commitMessage;
    private IProject project;
    private Shell shell;

    private ServiceSupplier<IApplicationManager> applicationManager =
        ServiceAccess.supplier(IApplicationManager.class, Activator.getDefault());

    public PushAction(IProject project, Shell shell)
    {
        this.project = project;
        this.shell = shell;
    }

    public void dispose()
    {
        applicationManager.close();
    }

    private IApplicationManager getApplicationManager()
    {
        return applicationManager.get();
    }

    @Override
    public void run()
    {
        ICommitHandler callback = (hash, commitMessage) -> {
            this.hash = hash;
            this.commitMessage = commitMessage;
            ScriptRunnerJob job = new ScriptRunnerJob(this, project);
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

    @Override
    public IStatus beforeRunJob(IProgressMonitor subMonitor) throws InvocationTargetException
    {
        ApplicationUpdateState updateState = null;

        try
        {
            updateState = applicationUpdate(project, subMonitor);
        }
        catch (ApplicationException e)
        {
            throw new InvocationTargetException(e);
        }

        IStatus status = null;

        switch (updateState)
        {
        case UPDATED:
            {
                status = new Status(IStatus.OK, Activator.PLUGIN_ID, Messages.PushAction_Updated, null);
                break;
            }
        case INCREMENTAL_UPDATE_REQUIRED:
            {
                Throwable t = new NullPointerException(Messages.PushAction_NeedsRepeated);
                status = new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.PushAction_NeedsRepeated, t);
                break;
            }
        default:
            String msg = MessageFormat.format(Messages.PushAction_UnexpectedValue, updateState);
            throw new IllegalArgumentException(msg);
        }

        return status;
    }

    private ApplicationUpdateState applicationUpdate(IProject project, IProgressMonitor monitor)
        throws InvocationTargetException
    {
        IApplicationManager applicationManager = getApplicationManager();
        Optional<IApplication> application = applicationManager.getDefaultApplication(project);
        ExecutionContext context = new ExecutionContext(Map.of("activeShell", shell)); //$NON-NLS-1$
        applicationManager.prepare(application.get(), "debug", context, monitor); //$NON-NLS-1$
        ApplicationUpdateState updateState =
            applicationManager.update(application.get(), ApplicationUpdateType.INCREMENTAL, context, monitor);
        return updateState;
    }
}
