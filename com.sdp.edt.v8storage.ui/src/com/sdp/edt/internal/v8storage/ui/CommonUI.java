package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Preconditions;

public class CommonUI
{

    public static IProject getActiveProject()
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        if (page != null)
        {
            IEditorPart editor = page.getActiveEditor();
            if (editor != null)
            {
                IEditorInput input = editor.getEditorInput();
                if (input instanceof IAdaptable)
                {
                    IFile file = ((IAdaptable)input).getAdapter(IFile.class);
                    Preconditions.checkNotNull(file);
                    return file.getProject();
                }
            }
        }

        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
        {
            if (project.isOpen())
            {
                return project;
            }
        }

        return null;
    }

}
