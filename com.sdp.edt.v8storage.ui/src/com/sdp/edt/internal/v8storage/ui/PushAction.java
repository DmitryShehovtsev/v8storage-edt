package com.sdp.edt.internal.v8storage.ui;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com._1c.g5.v8.dt.platform.services.core.infobases.export.ExportConfigurationFileException;
import com._1c.g5.v8.dt.platform.services.ui.PlatformServicesUiPlugin;
import com.e1c.g5.dt.applications.ApplicationException;
import com.e1c.g5.dt.applications.ApplicationUpdateState;
import com.sdp.edt.internal.v8storage.Activator;

public class PushAction
    extends AbstractActions
{

    private String hash;
    private String commitMessage;
    private Shell shell;
    private CommonUtils commonUtils;
    private GitActions gitActions;

    public PushAction(Shell shell)
    {
        this.shell = shell;
        this.commonUtils = new CommonUtils();
        this.gitActions = new GitActions(repo);
    }

    @Override
    public void run()
    {
        ICommitHandler callback = (hash, commitMessage) -> {
            this.hash = hash;
            this.commitMessage = commitMessage;
            ScriptRunnerJob job = new ScriptRunnerJob(this);
            job.schedule();
        };

        Shell parentShell = Display.getCurrent().getActiveShell();
        PushDialog.show(repo, parentShell, callback);
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
    public IStatus beforeRunJob(IProject project, IProgressMonitor subMonitor) throws InvocationTargetException
    {
        IStatus status = doUpdate(project, subMonitor);
        if (status.isOK())
        {
            status = doConfigDump(project, subMonitor);
        }
        return status;
    }

    private IStatus doUpdate(IProject project, IProgressMonitor subMonitor) throws InvocationTargetException
    {
        ApplicationUpdateState updateState = null;

        try
        {
            updateState = commonUtils.applicationUpdate(project, subMonitor, shell);
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

    private IStatus doConfigDump(IProject project, IProgressMonitor subMonitor) throws InvocationTargetException
    {
        IStatus status = null;
        String dumpName = gitActions.getHeadHash();
        try
        {
            commonUtils.applicationConfigDump(project, dumpName, subMonitor);
        }
        catch (final InvocationTargetException | ExportConfigurationFileException e)
        {
            PlatformServicesUiPlugin.log(e);
        }
        return status;
    }

}
