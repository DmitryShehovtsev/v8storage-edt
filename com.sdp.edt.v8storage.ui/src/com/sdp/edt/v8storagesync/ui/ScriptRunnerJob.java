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
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
        IStatus status = null;

        try
        {
            ProgressMonitors.runAsTask("", IProgressMonitor.UNKNOWN, monitor, (subMonitor) -> { //$NON-NLS-1$
                runWithSubMonitor(subMonitor.split(1));
            });
            return Status.OK_STATUS;
        }
        catch (Exception e)
        {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof ApplicationException)
            {
                ApplicationException applicationError = (ApplicationException)cause;
                status = applicationError.getStatus();
                if (status.matches(IStatus.CANCEL))
                {
                    status = CommonUtils.statusError(Messages.ScriptRunnerJob_UserCancel, applicationError);
                }
                else
                {
                    status = CommonUtils.statusError(applicationError.getMessage(), applicationError);
                }
            }
            else
            {
                status = CommonUtils.statusError(e.getMessage(), e);
            }
        }
        finally
        {
            monitor.done();
        }

        return status;
    }

    private void runWithSubMonitor(IProgressMonitor monitor) throws Exception
    {
        IStatus status = null;

        String scriptPath = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_SCRIPT_PATH);
        if (!PreferencesChecks.FileExistsUI(scriptPath))
        {
            String msg = PreferencesChecks.messageInvalidPath();
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, null);
            throw new CoreException(status);
        }

        IProject[] projects = action.projects();

        if (projects.length == 0)
        {
            status = new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.Error_NoActiveProject, null);
            throw new CoreException(status);
        }

        for (IProject project : projects)
        {
            monitor.setTaskName(project.getName());
            status = action.beforeRunJob(project, monitor);
            if (status.isOK())
            {
                IPath projectLocation = project.getLocation();
                String cwd = projectLocation.toOSString();
                status = scriptRun(scriptPath, cwd, monitor);
                project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            }
            if (status.matches(IStatus.ERROR))
            {
                status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, status.getMessage(), null);
                throw new CoreException(status);
            }
        }

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

            MessageConsole console = findOrCreateConsole();

            String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
            Charset charset = osName.contains("win") ? Charset.forName("CP866") : StandardCharsets.UTF_8; //$NON-NLS-1$ //$NON-NLS-2$

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
                MessageConsoleStream outputStream = console.newMessageStream())
            {
                while (process.isAlive())
                {
                    if (monitor.isCanceled())
                    {
                        process.destroy();
                        return Status.CANCEL_STATUS;
                    }

                    while (reader.ready())
                    {
                        String line = reader.readLine();
                        if (line != null)
                        {
                            outputStream.println(line);
                            monitor.subTask(line);
                        }
                    }

                    process.waitFor(100, TimeUnit.MILLISECONDS);
                }

                while (reader.ready())
                {
                    String line = reader.readLine();
                    if (line != null)
                    {
                        outputStream.println(line);
                        monitor.subTask(line);
                    }
                }

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
