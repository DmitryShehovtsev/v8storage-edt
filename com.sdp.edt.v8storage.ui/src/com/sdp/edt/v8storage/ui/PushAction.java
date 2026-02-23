package com.sdp.edt.v8storage.ui;

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
            MessageDialog.openError(shell, Messages.Error_Exception, Messages.Error_NoActiveProject);
            return null;
        }
        IPath projectLocation = activeProject.getLocation();
        String scriptPath = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_SCRIPT_PATH);

        String projectDir = projectLocation.toOSString();

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists() || !scriptFile.isFile())
        {
            MessageDialog.openError(shell, Messages.Error_Exception,
                Messages.V8StoragePreferencePage_InvalidPath + ": " + scriptPath); //$NON-NLS-1$
            return null;
        }

        CommitHandler callback = (hash, message) -> {
            String commandText = "v8storage push -f -h %h -m %m".formatted(hash, message); //$NON-NLS-1$
            String header = Messages.PushDialog_Header;
            ScriptRunnerJob job = new ScriptRunnerJob(scriptPath, projectDir, commandText, header);
            job.schedule();
        };

        PushDialog dialog = new PushDialog(shell, callback);
        dialog.open();

        return null;
    }
}