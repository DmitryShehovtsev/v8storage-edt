package com.sdp.edt.v8storage.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class ScriptRunnerJob
    extends Job
{

    private static final String CONSOLE_NAME = "V8 Storage Output"; //$NON-NLS-1$

    private final String scriptPath;
    private final String projectDir;
    private final String command;

    public ScriptRunnerJob(String scriptPath, String projectDir, String command, String header)
    {
        super(header);
        this.scriptPath = scriptPath;
        this.projectDir = projectDir;
        this.command = command;
        this.setUser(true);
        this.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

        List<String> cmd = new ArrayList<>();
        cmd.add("oscript"); //$NON-NLS-1$
        cmd.add(scriptPath);
        cmd.addAll(Arrays.asList(command.trim().split("\\s+"))); //$NON-NLS-1$

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.directory(new File(projectDir));

        Process process = null;
        try
        {
            process = pb.start();

            MessageConsole console = findOrCreateConsole();
            MessageConsoleStream outputStream = console.newMessageStream();

            String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
            Charset charset = osName.contains("win") ? Charset.forName("CP866") : StandardCharsets.UTF_8; //$NON-NLS-1$ //$NON-NLS-2$

            Process finalProcess = process;
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(finalProcess.getInputStream(), charset)))
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
                    logError(Messages.ScriptRunnerJob_ErrorReading + ": " + e.getMessage(), e); //$NON-NLS-1$
                }
            });
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
                String errorMsg = Messages.ScriptRunnerJob_ErrorCode + " " + exitCode; //$NON-NLS-1$
                monitor.subTask(errorMsg);
                IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMsg);
                return errorStatus;
            }
            else
            {
                monitor.subTask(Messages.ScriptRunnerJob_Done);
                return Status.OK_STATUS;
            }
        }
        catch (IOException | InterruptedException e)
        {
            String errorMsg = Messages.ScriptRunnerJob_ErrorDuringExecution + ": " + e.getMessage(); //$NON-NLS-1$
            monitor.subTask(errorMsg);
            IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMsg, e);
            return errorStatus;
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

    private void logError(String message, Throwable e)
    {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e);
        Activator.getDefault().getLog().log(status);
    }

}