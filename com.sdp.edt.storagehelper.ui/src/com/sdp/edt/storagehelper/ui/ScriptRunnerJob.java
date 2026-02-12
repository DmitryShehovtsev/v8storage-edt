package com.sdp.edt.storagehelper.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;

public class ScriptRunnerJob
    extends Job
{
    private final String scriptPath;
    private final String hash;
    private final String message;

    public ScriptRunnerJob(String scriptPath, String hash, String message)
    {
        super("Running Pull Storage Script"); //$NON-NLS-1$
        this.scriptPath = scriptPath;
        this.setUser(true);
        this.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
        System.out.println("ScriptRunnerJob created and scheduled."); //$NON-NLS-1$

        this.addJobChangeListener(new IJobChangeListener()
        {
            @Override
            public void done(IJobChangeEvent event)
            {
                System.out.println("Job done with status: " + event.getResult().getMessage()); //$NON-NLS-1$
            }

            @Override
            public void aboutToRun(IJobChangeEvent event)
            {
                //
            }

            @Override
            public void awake(IJobChangeEvent event)
            {
                //
            }

            @Override
            public void running(IJobChangeEvent event)
            {
                //
            }

            @Override
            public void scheduled(IJobChangeEvent event)
            {
                //
            }

            @Override
            public void sleeping(IJobChangeEvent event)
            {
                //
            }
        });
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        monitor.beginTask("Starting script...", IProgressMonitor.UNKNOWN);//$NON-NLS-1$
        System.out.println("Job run() started."); //$NON-NLS-1$

        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", scriptPath); //$NON-NLS-1$ //$NON-NLS-2$
        pb.redirectErrorStream(true);

        try
        {
            Process process = pb.start();
            System.out.println("Process started: " + scriptPath); //$NON-NLS-1$

            // Thread for reading output in real-time
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)))
                {
                    String line;
                    String lastLine = ""; //$NON-NLS-1$
                    while ((line = reader.readLine()) != null)
                    {
                        lastLine = line;
                        monitor.subTask(lastLine); // Update with last line
                        System.out.println("Output line: " + line); //$NON-NLS-1$
                    }
                }
                catch (IOException e)
                {
                    System.out.println("Error reading output: " + e.getMessage()); //$NON-NLS-1$
                }
            });
            outputThread.start();

            // Wait for process to finish
            int exitCode = process.waitFor();
            outputThread.join(); // Wait for output thread to finish
            System.out.println("Process exited with code: " + exitCode); //$NON-NLS-1$

            if (exitCode != 0)
            {
                monitor.subTask("Process failed: Script exited with code " + exitCode); //$NON-NLS-1$
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Script failed with code " + exitCode); //$NON-NLS-1$
            }
            else
            {
                monitor.subTask("Process completed successfully."); //$NON-NLS-1$
                return Status.OK_STATUS;
            }
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("Exception in run(): " + e.getMessage()); //$NON-NLS-1$
            monitor.subTask("Process failed: " + e.getMessage()); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to run script: " + e.getMessage(), e); //$NON-NLS-1$
        }
        finally
        {
            monitor.done();
            System.out.println("Job run() finished."); //$NON-NLS-1$
        }

    }
}