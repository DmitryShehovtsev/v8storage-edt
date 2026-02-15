package com.sdp.edt.v8storage.ui;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com._1c.g5.v8.dt.common.git.GitUtils;

public class GitUtil
{

    public static String getInstructionFromGit(IWorkbenchWindow window)
    {
        try
        {
            IProject project = getActiveProject(window);
            if (project == null)
            {
                return "Не обнаружен активный проект."; //$NON-NLS-1$
            }

            Repository repository = GitUtils.getGitRepository(project);
            if (repository == null)
            {
                return "Проект не является Git-репозиторием."; //$NON-NLS-1$
            }

            // Получаем HEAD commit
            RevCommit headCommit = getHeadCommit(repository);
            if (headCommit == null)
            {
                return "Ошибка: Не удалось получить HEAD commit."; //$NON-NLS-1$
            }
            String headInfo = "HEAD commit " + headCommit.getId().getName(); //$NON-NLS-1$

            // Получаем parent commit info
            String parentInfo = getParentCommitInfo(headCommit, repository);

            return headInfo + "\n" + parentInfo; // Вертикальное размещение //$NON-NLS-1$
        }
        catch (Exception e)
        { // Ловим все исключения
            return "Ошибка: " + e.getMessage(); //$NON-NLS-1$
        }
    }

    public static String getHeadHash(IWorkbenchWindow window)
    {
        try
        {
            IProject project = getActiveProject(window);
            if (project == null)
            {
                return ""; //$NON-NLS-1$
            }

            Repository repository = GitUtils.getGitRepository(project);
            if (repository == null)
            {
                return ""; //$NON-NLS-1$
            }

            ObjectId headId = repository.resolve("HEAD"); //$NON-NLS-1$
            return headId != null ? headId.getName() : ""; //$NON-NLS-1$
        }
        catch (Exception e)
        {
            return ""; //$NON-NLS-1$
        }
    }

    public static String getParentHash(IWorkbenchWindow window)
    {
        try
        {
            IProject project = getActiveProject(window);
            if (project == null)
            {
                return ""; //$NON-NLS-1$
            }

            Repository repository = GitUtils.getGitRepository(project);
            if (repository == null)
            {
                return ""; //$NON-NLS-1$
            }

            RevCommit headCommit = getHeadCommit(repository);
            if (headCommit == null || headCommit.getParentCount() == 0)
            {
                return ""; //$NON-NLS-1$
            }

            return headCommit.getParent(0).getId().getName();
        }
        catch (Exception e)
        {
            return ""; //$NON-NLS-1$
        }
    }

    private static RevCommit getHeadCommit(Repository repository) throws IOException
    {
        ObjectId headId = repository.resolve("HEAD"); //$NON-NLS-1$
        if (headId == null)
        {
            return null;
        }
        try (RevWalk revWalk = new RevWalk(repository))
        {
            return revWalk.parseCommit(headId);
        }
    }

    private static String getParentCommitInfo(RevCommit headCommit, Repository repository) throws IOException
    {
        if (headCommit.getParentCount() > 0)
        {
            ObjectId parentId = headCommit.getParent(0).getId();
            try (RevWalk revWalk = new RevWalk(repository))
            {
                RevCommit parent = revWalk.parseCommit(parentId);
                return "Parent commit " + parent.getId().getName(); //$NON-NLS-1$
            }
        }
        else
        {
            return "Нет parent-коммита (возможно, initial commit)."; //$NON-NLS-1$
        }
    }

    public static IProject getActiveProject(IWorkbenchWindow window)
    {
        if (window == null)
        {
            window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        }
        IWorkbenchPage page = window.getActivePage();
        if (page != null)
        {
            IEditorPart editor = page.getActiveEditor();
            if (editor != null)
            {
                IEditorInput input = editor.getEditorInput();
                if (input instanceof IAdaptable)
                {
                    org.eclipse.core.resources.IFile file =
                        ((IAdaptable)input).getAdapter(org.eclipse.core.resources.IFile.class);
                    if (file != null)
                    {
                        return file.getProject();
                    }
                }
            }

            for (IProject p : org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProjects())
            {
                if (p.isOpen())
                {
                    return p;
                }
            }
        }
        return null;
    }
}
