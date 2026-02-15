package com.sdp.edt.storagehelper.ui;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class PushAction
    extends AbstractHandler
{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        Shell shell = HandlerUtil.getActiveShell(event);
        if (shell == null)
        {
            shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        }

        IProject activeProject = GitUtil.getActiveProject(null);
        if (activeProject == null)
        {
            MessageDialog.openError(shell, "Error", "No active project found."); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        IPath projectLocation = activeProject.getLocation();
        String scriptPath = projectLocation.append("edt-helper-test") //$NON-NLS-1$
            .append("main.os") //$NON-NLS-1$
            .toOSString();

        String projectDir = projectLocation.toOSString();

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists() || !scriptFile.isFile())
        {
            MessageDialog.openError(shell, "Error", "Script file not found: " + scriptPath); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

        CommitHandler callback = (hash, message) -> {
            ScriptRunnerJob job = new ScriptRunnerJob(scriptPath, hash, message, projectDir);
            job.schedule();
        };

        PushDialog dialog = new PushDialog(shell, callback);
        dialog.open();

        return null;
    }
}