package com.sdp.edt.storagehelper.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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

        String scriptPath = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_SCRIPT_PATH);

        //IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        //shell = window.getShell();

        // Предполагаем, shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        CommitHandler callback = (hash, message) -> {
            ScriptRunnerJob job = new ScriptRunnerJob(shell, scriptPath, hash, message);
            job.schedule();
        };

        PushDialog dialog = new PushDialog(shell, callback);
        dialog.open(); // Откроется немодально, код продолжит выполнение сразу

        return null;
    }

    public PushAction()
    {
        setBaseEnabled(true);
    }
}