 /***********************************************************************************************
  * Copyright (c) Microsoft Corporation All rights reserved.
  * 
  * MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  ***********************************************************************************************/
 
 package com.microsoft.gittf.core.tasks;
 
 import java.net.URI;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jgit.dircache.DirCache;
 import org.eclipse.jgit.dircache.DirCacheCheckout;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.RefUpdate;
 import org.eclipse.jgit.lib.Repository;
 
 import com.microsoft.gittf.core.GitTFConstants;
 import com.microsoft.gittf.core.Messages;
 import com.microsoft.gittf.core.config.ChangesetCommitMap;
 import com.microsoft.gittf.core.interfaces.VersionControlService;
 import com.microsoft.gittf.core.tasks.framework.NullTaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.Task;
 import com.microsoft.gittf.core.tasks.framework.TaskExecutor;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressDisplay;
 import com.microsoft.gittf.core.tasks.framework.TaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.TaskStatus;
 import com.microsoft.gittf.core.util.Check;
 import com.microsoft.gittf.core.util.CommitUtil;
 import com.microsoft.tfs.core.clients.versioncontrol.GetItemsOptions;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.DeletedState;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Item;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
 
 public class CloneTask
     extends Task
 {
     private final URI serverURI;
     private final VersionControlService vcClient;
     private final String tfsPath;
     private final Repository repository;
 
     private boolean bare;
     private VersionSpec versionSpec = LatestVersionSpec.INSTANCE;
     private int depth = 1;
 
     private static final Log log = LogFactory.getLog(CloneTask.class);
 
     public CloneTask(
         final URI serverURI,
         final VersionControlService vcClient,
         final String tfsPath,
         final Repository repository)
     {
         Check.notNull(serverURI, "serverURI"); //$NON-NLS-1$
         Check.notNull(vcClient, "vcClient"); //$NON-NLS-1$
         Check.notNullOrEmpty(tfsPath, "tfsPath"); //$NON-NLS-1$
         Check.notNull(repository, "repository"); //$NON-NLS-1$
 
         this.serverURI = serverURI;
         this.vcClient = vcClient;
         this.tfsPath = tfsPath;
         this.repository = repository;
     }
 
     public void setBare(boolean bare)
     {
         this.bare = bare;
     }
 
     public boolean isBare()
     {
         return bare;
     }
 
     public void setVersionSpec(final VersionSpec versionSpec)
     {
         Check.notNull(versionSpec, "versionSpec"); //$NON-NLS-1$
 
         this.versionSpec = versionSpec;
     }
 
     public VersionSpec getVersionSpec()
     {
         return versionSpec;
     }
 
     public void setDepth(int depth)
     {
         Check.isTrue(depth >= 1, "depth >= 1"); //$NON-NLS-1$
 
         this.depth = depth;
     }
 
     public int getDepth()
     {
         return depth;
     }
 
     public Repository getRepository()
     {
         return repository;
     }
 
     @Override
     public TaskStatus run(final TaskProgressMonitor progressMonitor)
         throws Exception
     {
         final String taskName = Messages.formatString("CloneTask.CloningFormat", //$NON-NLS-1$
             tfsPath,
             bare ? repository.getDirectory().getAbsolutePath() : repository.getWorkTree().getAbsolutePath());
 
         progressMonitor.beginTask(
             taskName,
             1,
             TaskProgressDisplay.DISPLAY_PROGRESS.combine(TaskProgressDisplay.DISPLAY_SUBTASK_DETAIL));
 
         /*
          * Query the changesets.
          */
 
         /* See if this is an actual server path. */
         Item item = vcClient.getItem(tfsPath, versionSpec, DeletedState.NON_DELETED, GetItemsOptions.NONE);
         Check.notNull(item, "item"); //$NON-NLS-1$
 
         if (item.getItemType() != ItemType.FOLDER)
         {
             return new TaskStatus(TaskStatus.ERROR, Messages.formatString("CloneTask.CannotCloneFileFormat", tfsPath)); //$NON-NLS-1$
         }
 
         /* Determine the latest changeset on the server. */
         final Changeset[] changesets =
             vcClient.queryHistory(
                 tfsPath,
                 versionSpec,
                 0,
                 RecursionType.FULL,
                 null,
                 new ChangesetVersionSpec(0),
                 versionSpec,
                 depth,
                 false,
                 false,
                 false,
                 false);
 
         /*
          * Create and configure the repository.
          */
 
         repository.create(bare);
 
         final ConfigureRepositoryTask configureTask = new ConfigureRepositoryTask(repository, serverURI, tfsPath);
         configureTask.setDeep(depth > GitTFConstants.GIT_TF_SHALLOW_DEPTH);
 
         TaskStatus configureStatus = new TaskExecutor(new NullTaskProgressMonitor()).execute(configureTask);
 
         if (!configureStatus.isOK())
         {
             return configureStatus;
         }
 
         if (changesets.length > 0)
         {
             ObjectId lastCommitID = null, lastTreeID = null;
 
             /*
              * Download changesets.
              */
             int numberOfChangesetToDownload = changesets.length - 1;
 
             progressMonitor.setWork(numberOfChangesetToDownload + 1);
 
             for (int i = numberOfChangesetToDownload; i >= 0; i--)
             {
                 CreateCommitTask commitTask =
                     new CreateCommitTask(repository, vcClient, changesets[i].getChangesetID(), lastCommitID);
 
                 TaskStatus commitStatus = new TaskExecutor(progressMonitor.newSubTask(1)).execute(commitTask);
 
                 if (!commitStatus.isOK())
                 {
                     return commitStatus;
                 }
 
                 lastCommitID = commitTask.getCommitID();
                 lastTreeID = commitTask.getCommitTreeID();
 
                 Check.notNull(lastCommitID, "lastCommitID"); //$NON-NLS-1$
                 Check.notNull(lastTreeID, "lastTreeID"); //$NON-NLS-1$
 
                 new ChangesetCommitMap(repository).setChangesetCommit(
                     changesets[i].getChangesetID(),
                     commitTask.getCommitID());
 
                 progressMonitor.displayVerbose(Messages.formatString("CloneTask.ClonedFormat", //$NON-NLS-1$
                     Integer.toString(changesets[i].getChangesetID()),
                     CommitUtil.abbreviate(repository, lastCommitID)));
             }
 
             progressMonitor.setDetail(Messages.getString("CloneTask.Finalizing")); //$NON-NLS-1$
 
             RefUpdate ref = repository.updateRef(Constants.R_HEADS + Constants.MASTER);
             ref.setNewObjectId(lastCommitID);
             ref.update();
 
             /*
              * Check out the cloned commit.
              */
             if (!bare)
             {
                 DirCache dirCache = repository.lockDirCache();
                 DirCacheCheckout checkout = new DirCacheCheckout(repository, dirCache, lastTreeID);
                 checkout.checkout();
                 dirCache.unlock();
             }
 
             progressMonitor.endTask();
 
             int finalChangesetID = changesets[0].getChangesetID();
 
             if (numberOfChangesetToDownload <= 1)
             {
                 progressMonitor.displayMessage(Messages.formatString("CloneTask.ClonedFormat", //$NON-NLS-1$
                     Integer.toString(finalChangesetID),
                     CommitUtil.abbreviate(repository, lastCommitID)));
             }
             else
             {
                 progressMonitor.displayMessage(Messages.formatString("CloneTask.ClonedMultipleFormat", //$NON-NLS-1$
                    numberOfChangesetToDownload,
                     Integer.toString(finalChangesetID),
                     CommitUtil.abbreviate(repository, lastCommitID)));
             }
         }
         else if (changesets.length == 0)
         {
             // the folder exists on the server but is empty
 
             progressMonitor.displayMessage(Messages.getString("CloneTask.NothingToDownload")); //$NON-NLS-1$
 
             progressMonitor.displayMessage(Messages.formatString("CloneTask.ClonedFolderEmptyFormat", //$NON-NLS-1$
                 tfsPath));
         }
 
         log.info("Clone task completed."); //$NON-NLS-1$
 
         return TaskStatus.OK_STATUS;
     }
 }
