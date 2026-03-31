
package com.sdp.edt.internal.v8storage.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com._1c.g5.v8.dt.common.git.GitUtils;

@SuppressWarnings("restriction")
public abstract class AbstractActions
{
    public Repository repo;

    public AbstractActions() {
        repo = getSelectedRepository();
    }

    public void run()
    {
        //
    }

    public String header()
    {
        return ""; //$NON-NLS-1$
    }

    public String command()
    {
        return ""; //$NON-NLS-1$
    }

    public String scriptEngine()
    {
        return ""; //$NON-NLS-1$
    }

    public IStatus beforeRunJob(IProject project, IProgressMonitor subMonitor) throws InvocationTargetException
    {
        IStatus status = null;
        return status;
    }

    public IProject[] projects()
    {
        return GitUtils.getProjectsInRepository(repo);
    }

    public Repository getSelectedRepository()
    {
        IViewPart view = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage()
            .findView("org.eclipse.egit.ui.RepositoriesView"); //$NON-NLS-1$
        if (view == null)
        {
            return null;
        }
        IStructuredSelection selection = (IStructuredSelection)view.getSite().getSelectionProvider().getSelection();
        if (selection.isEmpty())
        {
            return null;
        }
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof RepositoryTreeNode)
        {
            RepositoryTreeNode<?> node = (RepositoryTreeNode<?>)firstElement;
            return node.getRepository();
        }
        return null;
    }

    public void dispose()
    {
        //
    }

}