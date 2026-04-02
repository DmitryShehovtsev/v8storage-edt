package com.sdp.edt.v8storagesync.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.IProgressConstants;

import com._1c.g5.v8.dt.common.runtime.ProgressMonitors;
import com.e1c.g5.dt.applications.ApplicationException;
import com.sdp.edt.v8storagesync.preferences.PreferencesChecks;

public class ScriptRunnerJob
    extends Job
{

    private static final String CONSOLE_NAME = "V8 Storage Sync Output"; //$NON-NLS-1$

    private final AbstractActions action;

    public ScriptRunnerJob(AbstractActions action)
    {
        super(action.header());

        this.action = action;
        this.setUser(true);
        this.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        monitor.beginTask(null, IProgressMonitor.UNKNOWN);

        IStatus status = null;

        try
        {
            status = runWithSubMonitor(monitor);
        }
        catch (Exception e)
        {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof ApplicationException)
            {
               ApplicationException applicationError = (ApplicationException)cause;
               ApplicationException eApp = (ApplicationException)cause;
               status = applicationError.getStatus();
               if (status.matches(IStatus.CANCEL))
               {
                   status = CommonUtils.statusError(Messages.ScriptRunnerJob_UserCancel, eApp);
               }
               else
               {
                   status = CommonUtils.statusError(Messages.Application_Error, eApp);
               }
            }
            else
            {
                status = CommonUtils.statusError(Messages.Application_Error, e);
            }
        }

        return status;
    }

    private IStatus runWithSubMonitor(IProgressMonitor monitor) throws Exception
    {
        return ProgressMonitors.computeWithSubMonitor(null, IProgressMonitor.UNKNOWN, monitor, (subMonitor) -> {

            String scriptPath = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_SCRIPT_PATH);
            if (!PreferencesChecks.FileExistsUI(scriptPath))
            {
                String msg = PreferencesChecks.messageInvalidPath();
                return CommonUtils.statusError(msg, null);
            }

            IProject[] projects = action.projects();

            IStatus status = null;
            if (projects.length == 0)
                status = new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.Error_NoActiveProject, null);

            for (IProject project : projects)
            {
                status = action.beforeRunJob(project, subMonitor);
                if (status.isOK())
                {
                    IPath projectLocation = project.getLocation();
                    String cwd = projectLocation.toOSString();
                    status = scriptRun(scriptPath, cwd, subMonitor);
                }
                if (status.getCode() == IStatus.ERROR)
                {
                    return status;
                }
            }

            return status;

        });
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
                    return Status.CANCEL_STATUS;
                }
                Thread.sleep(100);
            }

            outputThread.join();

            int exitCode = process.exitValue();
            if (exitCode != 0)
            {
                String errorMsg = MessageFormat.format(Messages.ScriptRunnerJob_ErrorCode, exitCode);
                monitor.subTask(errorMsg);
                return CommonUtils.statusError(errorMsg, null);
            }
            else
            {
                monitor.subTask(Messages.ScriptRunnerJob_Done);
                return Status.OK_STATUS;
            }
        }
        catch (IOException | InterruptedException e)
        {
            String errorMsg = MessageFormat.format(Messages.ScriptRunnerJob_ErrorDuringExecution, e.getMessage());
            monitor.subTask(errorMsg);
            return CommonUtils.statusError(errorMsg, e);
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
                String msg = MessageFormat.format(Messages.ScriptRunnerJob_ErrorReading, e.getMessage());
                CommonUtils.statusError(msg, e);

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
}