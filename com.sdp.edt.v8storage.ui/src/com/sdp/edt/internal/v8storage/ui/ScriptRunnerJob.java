package com.sdp.edt.internal.v8storage.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.IProgressConstants;

import com._1c.g5.v8.dt.common.runtime.ProgressMonitors;
import com._1c.g5.wiring.ServiceAccess;
import com._1c.g5.wiring.ServiceSupplier;
import com.e1c.g5.dt.applications.ApplicationException;
import com.e1c.g5.dt.applications.ApplicationUpdateState;
import com.e1c.g5.dt.applications.ApplicationUpdateType;
import com.e1c.g5.dt.applications.ExecutionContext;
import com.e1c.g5.dt.applications.IApplication;
import com.e1c.g5.dt.applications.IApplicationManager;
import com.sdp.edt.internal.v8storage.Activator;
import com.sdp.edt.internal.v8storage.preferences.PreferencesChecks;

public class ScriptRunnerJob
    extends Job
{

    private static final String CONSOLE_NAME = "V8 Storage Output"; //$NON-NLS-1$

    private final IActions action;
    private final IProject project;
    private final Shell shell;

    private ServiceSupplier<IApplicationManager> applicationManager =
        ServiceAccess.supplier(IApplicationManager.class, Activator.getDefault());

    public void dispose()
    {
        applicationManager.close();
    }

    private IApplicationManager getApplicationManager()
    {
        return applicationManager.get();
    }

    public ScriptRunnerJob(IActions action, IProject project, Shell shell)
    {
        super(action.header());

        this.action = action;
        this.project = project;
        this.shell = shell;
        this.setUser(true);
        this.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

        IStatus status = null;

        try
        {
            status = runWithSubMonitor(monitor);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof ApplicationException)
            {
               ApplicationException applicationError = (ApplicationException)cause;
               ApplicationException eApp = (ApplicationException)cause;
               status = applicationError.getStatus();
               if (status.matches(status.CANCEL))
               {
                   logError("Задание отменено пользователем", eApp);
               }
               else
               {
                   logError(Messages.Application_Error, eApp);
               }
            }
            else
            {
                logError(Messages.Application_Error, e);
            }
        }

        return status;
    }

    private IStatus runWithSubMonitor(IProgressMonitor monitor) throws InvocationTargetException
    {
        return ProgressMonitors.computeWithSubMonitor("", IProgressMonitor.UNKNOWN, monitor, (subMonitor) -> {

            IStatus status = null;
            ApplicationUpdateState updateState = null;
            String scriptPath = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_SCRIPT_PATH);
            if (!PreferencesChecks.FileExistsUI(scriptPath))
            {
                String errorMsg = PreferencesChecks.messageInvalidPath();
                return logError(errorMsg, null);
            }
            try
            {
                updateState = applicationUpdate(project, subMonitor);
            }
            catch (ApplicationException e)
            {
                throw new InvocationTargetException(e);
            }

            switch (updateState)
            {
            case UPDATED:
                {
                    IPath projectLocation = project.getLocation();
                    String cwd = projectLocation.toOSString();
                    //status = scriptRun(scriptPath, cwd, subMonitor.split(IProgressMonitor.UNKNOWN));
                    break;
                }
            case INCREMENTAL_UPDATE_REQUIRED:
                {
                    String message = "Требуется вручную завершить обновление и повторить операцию";
                    Throwable t = new NullPointerException(message);
                    status = new Status(IStatus.CANCEL, JobManager.PI_JOBS, JobManager.PLUGIN_ERROR, message, t);
//                    this.setName(String.format("%s. %s", action.header(),
//                        "Требуется вручную завершить обновление и повторить операцию"));
                    break;
            }
            default:
                throw new IllegalArgumentException("Unexpected value: " + updateState);
            }

            return status;

        });
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

    private IStatus scriptRun(String scriptPath, String cwd, IProgressMonitor monitor)
    {
        List<String> cmd = new ArrayList<>();
        cmd.add(action.scriptEngine());
        cmd.add(scriptPath);
        cmd.addAll(Arrays.asList(action.command().trim().split("\\s+"))); //$NON-NLS-1$

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.directory(new File(cwd));

        Process process = null;
        try
        {
            process = pb.start();

            Thread outputThread = newOutputThread(process, monitor);
            outputThread.start();

            while (process.isAlive())
            {
                if (monitor.isCanceled())
                {
                    process.destroy();
                    monitor.subTask(Messages.ScriptRunnerJob_UserCancel);
                    return Status.CANCEL_STATUS;
                }
                Thread.sleep(100);
            }

            outputThread.join();

            int exitCode = process.exitValue();
            if (exitCode != 0)
            {
                String errorMsg = String.format("%s %s", Messages.ScriptRunnerJob_ErrorCode, exitCode); //$NON-NLS-1$
                monitor.subTask(errorMsg);
                return logError(errorMsg, null);
            }
            else
            {
                monitor.subTask(Messages.ScriptRunnerJob_Done);
                return Status.OK_STATUS;
            }
        }
        catch (IOException | InterruptedException e)
        {
            String errorMsg = String.format("%s: %s", Messages.ScriptRunnerJob_ErrorDuringExecution, e.getMessage()); //$NON-NLS-1$
            monitor.subTask(errorMsg);
            return logError(errorMsg, e);
        }
        finally
        {
            monitor.done();
            if (process != null && process.isAlive())
            {
                process.destroy();
            }
        }
    }

    private Thread newOutputThread(Process process, IProgressMonitor monitor)
    {
        MessageConsole console = findOrCreateConsole();
        MessageConsoleStream outputStream = console.newMessageStream();

        String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
        Charset charset = osName.contains("win") ? Charset.forName("CP866") : StandardCharsets.UTF_8; //$NON-NLS-1$ //$NON-NLS-2$

        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (monitor.isCanceled())
                    {
                        break;
                    }
                    outputStream.println(line);
                    monitor.subTask(line);
                }
            }
            catch (IOException e)
            {
                logError(String.format("%s: %s", Messages.ScriptRunnerJob_ErrorReading, e.getMessage()), e); //$NON-NLS-1$
            }
        });

        return outputThread;
    }

    private MessageConsole findOrCreateConsole()
    {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        for (IConsole existing : consoleManager.getConsoles())
        {
            if (CONSOLE_NAME.equals(existing.getName()))
            {
                return (MessageConsole)existing;
            }
        }
        MessageConsole newConsole = new MessageConsole(CONSOLE_NAME, null);
        consoleManager.addConsoles(new IConsole[] { newConsole });
        return newConsole;
    }

    private IStatus logError(String message, Throwable e)
    {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e);
        Activator.getDefault().getLog().log(status);
        return status;
    }

}