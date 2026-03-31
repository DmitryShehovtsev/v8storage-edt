package com.sdp.edt.internal.v8storage.ui;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class GitActions
{
    private Repository repository;

    public GitActions(Repository repository)
    {
        this.repository = repository;
    }

    public String getCommitContextInfo()
    {
        try
        {
            if (repository == null)
            {
                return Messages.Error_NotGitRepository;
            }

            RevCommit headCommit = getHeadCommit();
            if (headCommit == null)
            {
                return Messages.Error_NoHeadCommit;
            }

            String headInfo = String.format("HEAD commit %s", headCommit.getId().getName()); //$NON-NLS-1$
            String parentInfo = getParentCommitInfo(headCommit);

            return String.format("%s\n%s", headInfo, parentInfo); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            return String.format("%s: %s", Messages.Error_Exception, e.getMessage()); //$NON-NLS-1$
        }
    }

    public String getHeadHash()
    {
        try
        {
            ObjectId headId = repository.resolve("HEAD"); //$NON-NLS-1$
            return headId.getName();
        }
        catch (Exception e)
        {
            return ""; //$NON-NLS-1$
        }
    }

    public String getParentHash()
    {
        try
        {
            RevCommit headCommit = getHeadCommit();
            return getParentCommitIdMergedBranch(headCommit).getName();
        }
        catch (Exception e)
        {
            return ""; //$NON-NLS-1$
        }
    }

    private RevCommit getHeadCommit() throws IOException
    {
        try (RevWalk revWalk = new RevWalk(repository))
        {
            ObjectId headId = repository.resolve("HEAD"); //$NON-NLS-1$
            return revWalk.parseCommit(headId);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private String getParentCommitInfo(RevCommit headCommit) throws IOException
    {
        ObjectId parentId = getParentCommitIdMergedBranch(headCommit);
        try (RevWalk revWalk = new RevWalk(repository))
        {
            RevCommit parent = revWalk.parseCommit(parentId);
            return String.format("Parent commit %s", parent.getId().getName()); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            return Messages.Error_NoParentCommit;
        }
    }

    private ObjectId getParentCommitIdMergedBranch(RevCommit commit)
    {
        ObjectId commitId = null;

        switch (commit.getParentCount())
        {
        case 0:
            commitId = null;
            break;
        case 1:
            commitId = commit.getParent(0).getId();
            break;
        case 2:
            commitId = commit.getParent(1).getId();
            break;
        default:
            commitId = commit.getParent(0).getId();
            break;
        }

        return commitId;
    }

}
