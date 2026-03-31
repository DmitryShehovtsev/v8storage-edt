package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class PullActionHandler
    extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        PullAction action = new PullAction();
        action.run();

        return null;
    }
}