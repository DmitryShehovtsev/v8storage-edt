package com.sdp.edt.storagehelper.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com._1c.g5.v8.dt.common.git.GitUtils;

public class GitUtil2
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

            File workTree = repository.getWorkTree();
            File file = new File(workTree, "test.txt"); //$NON-NLS-1$
            if (!file.exists() || !file.isFile())
            {
                return "Файл test.txt отсутствует."; //$NON-NLS-1$
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    content.append(line).append("\n"); //$NON-NLS-1$
                }
            }
            return content.toString().trim();
        }
        catch (IOException e)
        {
            return "Ошибка чтения: " + e.getMessage(); //$NON-NLS-1$
        }
    }

    private static IProject getActiveProject(IWorkbenchWindow window)
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
