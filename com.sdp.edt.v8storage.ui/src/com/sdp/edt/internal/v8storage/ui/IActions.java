package com.sdp.edt.internal.v8storage.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface IActions
{
    void run();

    String header();

    String command();

    String scriptEngine();

    IStatus beforeRunJob(IProgressMonitor subMonitor) throws InvocationTargetException;
}

