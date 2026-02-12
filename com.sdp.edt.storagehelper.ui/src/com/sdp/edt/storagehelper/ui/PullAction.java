package com.sdp.edt.storagehelper.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class PullAction
    extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        return null;
    }

    public PullAction()
    {
        setBaseEnabled(true);
    }
}