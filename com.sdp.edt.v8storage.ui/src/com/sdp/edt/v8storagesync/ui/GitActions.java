package com.sdp.edt.v8storagesync.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
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

    public String getCommitContextInfo() throws InvocationTargetException
    {
        RevCommit headCommit = getHeadCommit();
        if (headCommit == null)
        {
            return Messages.Error_NoHeadCommit;
        }

        String headInfo = String.format("HEAD commit %s", headCommit.getId().getName()); //$NON-NLS-1$
        String parentInfo = getParentCommitInfo(headCommit);

        return String.format("%s\n%s", headInfo, parentInfo); //$NON-NLS-1$
    }

    public String getHeadHash() throws InvocationTargetException, RevisionSyntaxException, AmbiguousObjectException,
        IncorrectObjectTypeException, IOException
    {
        ObjectId headId = repository.resolve("HEAD"); //$NON-NLS-1$
        return headId.getName();
    }

    public String getParentHash() throws InvocationTargetException
    {
        RevCommit headCommit = getHeadCommit();
        return getParentCommitIdMergedBranch(headCommit).getName();
    }

    private RevCommit getHeadCommit() throws InvocationTargetException
    {
        try (RevWalk revWalk = new RevWalk(repository))
        {
            ObjectId headId = repository.resolve("HEAD"); //$NON-NLS-1$
            return revWalk.parseCommit(headId);
        }
        catch (Exception e)
        {
            throw new InvocationTargetException(e);
        }
    }

    private String getParentCommitInfo(RevCommit headCommit) throws InvocationTargetException
    {
        ObjectId parentId = getParentCommitIdMergedBranch(headCommit);
        try (RevWalk revWalk = new RevWalk(repository))
        {
            RevCommit parent = revWalk.parseCommit(parentId);
            return String.format("Parent commit %s", parent.getId().getName()); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new InvocationTargetException(e);
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
