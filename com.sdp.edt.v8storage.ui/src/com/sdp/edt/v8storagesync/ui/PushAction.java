package com.sdp.edt.v8storagesync.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.platform.services.core.infobases.export.ExportConfigurationFileException;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.RuntimeExecutionException;
import com._1c.g5.wiring.ServiceAccess;
import com._1c.g5.wiring.ServiceSupplier;
import com.e1c.g5.dt.applications.ApplicationUpdateState;
import com.e1c.g5.dt.applications.IApplication;

public class PushAction
    extends AbstractActions
{
    private String hash;
    private String commitMessage;
    private Shell shell;
    private CommonUtils commonUtils;
    private GitActions gitActions;

    private ServiceSupplier<IV8ProjectManager> v8projectManager =
        ServiceAccess.supplier(IV8ProjectManager.class, Activator.getDefault());

    public PushAction(Shell shell)
    {
        this.shell = shell;
        this.commonUtils = new CommonUtils();
        this.gitActions = new GitActions(repo);
    }

    @Override
    public void run() throws InvocationTargetException
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
        return Messages.ScriptRunnerJob_HeaderPush;
    }

    @Override
    public String command()
    {
        return String.format("v8storage push -h \"%s\" -m \"%s\"", hash, commitMessage); //$NON-NLS-1$
    }

    @Override
    public IStatus beforeRunJob(IProject project, IProgressMonitor subMonitor)
        throws InvocationTargetException, RevisionSyntaxException, AmbiguousObjectException,
        IncorrectObjectTypeException, IOException, RuntimeExecutionException
    {
        IStatus status = new Status(IStatus.OK, Activator.PLUGIN_ID, "", null); //$NON-NLS-1$

        IV8ProjectManager v8projectManager = getv8projectManager();
        IV8Project v8Project = v8projectManager.getProject(project);
        if (v8Project instanceof IExtensionProject)
            return status;

        Optional<IApplication> application = commonUtils.defaultApplication(project);
        if (application.isEmpty())
        {
            String msg = MessageFormat.format(Messages.PushAction_ApplicationNotFound, project.getName());
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, null);
            return status;
        }

        status = doUpdate(application, subMonitor);
        if (status.isOK())
            status = doConfigDump(application, project, subMonitor);

        return status;
    }

    private IStatus doUpdate(Optional<IApplication> application, IProgressMonitor subMonitor)
        throws InvocationTargetException
    {
        IStatus status = null;
        ApplicationUpdateState updateState = commonUtils.applicationUpdate(application, subMonitor, shell);
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
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, null);
        }

        return status;
    }

    private IStatus doConfigDump(Optional<IApplication> application, IProject project, IProgressMonitor subMonitor)
        throws InvocationTargetException, RevisionSyntaxException, AmbiguousObjectException,
        IncorrectObjectTypeException, IOException, RuntimeExecutionException
    {
        String projectName = project.getName();
        String msgDump = MessageFormat.format(Messages.PushAction_DumpingConf, projectName);
        subMonitor.subTask(msgDump);

        IStatus status = null;
        String dumpName = gitActions.getHeadHash();
        try
        {
            commonUtils.applicationConfigDump(application, project, dumpName, subMonitor);
            String msg = MessageFormat.format(Messages.PushAction_ConfigDumpSuccess, projectName);
            status = new Status(IStatus.OK, Activator.PLUGIN_ID, msg, null);
        }
        catch (final InvocationTargetException | ExportConfigurationFileException | CoreException e)
        {
            String msg = MessageFormat.format(Messages.PushAction_ConfigDumpError, projectName);
            status = CommonUtils.statusError(msg, e);
        }
        return status;
    }

    @Override
    public void dispose()
    {
        v8projectManager.close();
    }

    private IV8ProjectManager getv8projectManager()
    {
        return v8projectManager.get();
    }

}
