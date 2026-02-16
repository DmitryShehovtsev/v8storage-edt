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

public class PullAction
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
            MessageDialog.openError(shell, "Ошибка", "Не обнаружен активный проект."); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        IPath projectLocation = activeProject.getLocation();
        String scriptPath = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_SCRIPT_PATH);

        String projectDir = projectLocation.toOSString();

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists() || !scriptFile.isFile())
        {
            MessageDialog.openError(shell, "Ошибка", //$NON-NLS-1$
                "Неопределен путь к скрипту синхронизации с хранилищем: " + scriptPath); //$NON-NLS-1$
            return null;
        }

        ScriptRunnerJob job = new ScriptRunnerJob(scriptPath, projectDir, "createfile", "test1", "test2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        job.schedule();

        return null;
    }
}