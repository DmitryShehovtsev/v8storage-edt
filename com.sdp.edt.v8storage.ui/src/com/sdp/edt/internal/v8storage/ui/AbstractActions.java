
package com.sdp.edt.internal.v8storage.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com._1c.g5.v8.dt.common.git.GitUtils;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.RuntimeExecutionException;
import com.google.common.base.Preconditions;

@SuppressWarnings("restriction")
public abstract class AbstractActions
{
    public Repository repo;

    public AbstractActions() {
        repo = getSelectedRepository();
        Preconditions.checkNotNull(repo);
    }

    public void run() throws InvocationTargetException
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
        return "oscript"; //$NON-NLS-1$
    }

    public IStatus beforeRunJob(IProject project, IProgressMonitor subMonitor)
        throws InvocationTargetException, RevisionSyntaxException, AmbiguousObjectException,
        IncorrectObjectTypeException, IOException, RuntimeExecutionException
    {
        IStatus status = new Status(IStatus.OK, Activator.PLUGIN_ID, "", null); //$NON-NLS-1$
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
        Preconditions.checkNotNull(view);

        IStructuredSelection selection = (IStructuredSelection)view.getSite().getSelectionProvider().getSelection();
        Preconditions.checkNotNull(selection);
        Preconditions.checkArgument(selection.isEmpty());

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