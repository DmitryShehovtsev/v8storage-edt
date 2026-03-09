package com.sdp.edt.internal.v8storage.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.IProgressConstants;

import com._1c.g5.wiring.ServiceAccess;
import com._1c.g5.wiring.ServiceSupplier;
import com.e1c.g5.dt.applications.ApplicationException;
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

    public ScriptRunnerJob(IActions action, Shell shell)
    {
        super(action.header());

        this.action = action;
        this.shell = shell;
        this.setUser(true);
        this.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IProject project = GitActions.getActiveProject(window);
        if (project == null)
        {
            String errorMsg = Messages.Error_NoActiveProject;
            MessageDialog.openError(shell, Messages.Error_Exception, errorMsg);
            IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMsg);
            return errorStatus;
        }

        String scriptPath = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_SCRIPT_PATH);
        if (!PreferencesChecks.FileExistsUI(scriptPath, shell))
        {
            String errorMsg = PreferencesChecks.messageInvalidPath();
            IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMsg);
            return errorStatus;
        }

        applicationUpdate(project, monitor);

        IPath projectLocation = project.getLocation();
        String cwd = projectLocation.toOSString();
        IStatus status = processRun(scriptPath, cwd, monitor);

        return status;

    }

    private void applicationUpdate(IProject project, IProgressMonitor monitor)
    {
        try
        {
            IApplicationManager applicationManager = getApplicationManager();
            Optional<IApplication> application = applicationManager.getDefaultApplication(project);
            ExecutionContext context = new ExecutionContext(Map.of("activeShell", shell)); //$NON-NLS-1$
            applicationManager.prepare(application.get(), "debug", context, monitor); //$NON-NLS-1$
            applicationManager.update(application.get(), ApplicationUpdateType.INCREMENTAL, context, monitor);
        }
        catch (ApplicationException e)
        {
            throw new ApplicationException("Error working with the application", e);
        }
    }

    private IStatus processRun(String scriptPath, String cwd, IProgressMonitor monitor)
    {
        List<String> cmd = new ArrayList<>();
        cmd.add("oscript"); //$NON-NLS-1$
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