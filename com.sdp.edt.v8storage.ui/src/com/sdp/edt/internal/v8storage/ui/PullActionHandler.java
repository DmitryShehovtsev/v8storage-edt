package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;

public class PullActionHandler
    extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        IProject project = CommonUI.getActiveProject();
        PullAction action = new PullAction(project);
        action.run();

        return null;
    }
}