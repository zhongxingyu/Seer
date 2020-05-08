 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.api.porcelain;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 
 import org.geogit.api.AbstractGeoGitOp;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Platform;
 import org.geogit.api.Ref;
 import org.geogit.api.RevCommit;
 import org.geogit.api.RevTree;
 import org.geogit.api.SymRef;
 import org.geogit.api.plumbing.CreateTree;
 import org.geogit.api.plumbing.RefParse;
 import org.geogit.api.plumbing.ResolveTreeish;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.api.plumbing.UpdateRef;
 import org.geogit.api.plumbing.UpdateSymRef;
 import org.geogit.api.plumbing.WriteTree;
 import org.geogit.api.porcelain.ConfigOp.ConfigAction;
 import org.geogit.repository.CommitBuilder;
 import org.geogit.repository.Repository;
 import org.geogit.storage.ObjectInserter;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
 import com.google.common.base.Suppliers;
 import com.google.inject.Inject;
 
 /**
  * Commits the staged changed in the index to the repository, creating a new commit pointing to the
  * new root tree resulting from moving the staged changes to the repository, and updating the HEAD
  * ref to the new commit object.
  * <p>
  * Like {@code git commit -a}, If the {@link #setAll(boolean) all} flag is set, first stages all the
  * changed objects in the index, but does not state newly created (unstaged) objects that are not
  * already staged.
  * </p>
  * 
  * @author groldan
  * 
  */
 public class CommitOp extends AbstractGeoGitOp<RevCommit> {
 
     private Optional<String> authorName;
 
     private Optional<String> authorEmail;
 
     private String message;
 
     private Long timeStamp;
 
     /**
      * This commit's parents. Will be the current HEAD, but when we support merges it should include
      * the equivalent to git's .git/MERGE_HEAD
      */
     private List<ObjectId> parents = new LinkedList<ObjectId>();
 
     // like the -a option in git commit
     private boolean all;
 
     private Repository repository;
 
     private Platform platform;
 
     private boolean allowEmpty;
 
     private String committerName;
 
     private String committerEmail;
 
     @Inject
     public CommitOp(final Repository repository, final Platform platform) {
         this.repository = repository;
         this.platform = platform;
     }
 
     /**
      * If set, overrides the author's name from the configuration
      */
     public CommitOp setAuthor(final @Nullable String authorName, @Nullable final String authorEmail) {
         this.authorName = Optional.fromNullable(authorName);
         this.authorEmail = Optional.fromNullable(authorEmail);
         return this;
     }
 
     /**
      * If set, overrides the committer's name from the configuration
      */
     public void setCommitter(String committerName, @Nullable String committerEmail) {
         Preconditions.checkNotNull(committerName);
         this.committerName = committerName;
         this.committerEmail = committerEmail;
     }
 
     /**
      * Sets the {@link RevCommit#getMessage() commit message}.
      * 
      * @param message description of the changes to record the commit with.
      * @return {@code this}, to ease command chaining
      */
     public CommitOp setMessage(@Nullable final String message) {
         this.message = message;
         return this;
     }
 
     /**
      * Sets the {@link RevCommit#getTimestamp() timestamp} the commit will be marked to, or if not
      * set defaults to the current system time at the time {@link #call()} is called.
      * 
      * @param timestamp commit timestamp, in milliseconds, as in {@link Date#getTime()}
      * @return {@code this}, to ease command chaining
      */
     public CommitOp setTimestamp(@Nullable final Long timestamp) {
         this.timeStamp = timestamp;
         return this;
     }
 
     /**
      * If {@code true}, tells {@link #call()} to stage all the unstaged changes that are not new
      * object before performing the commit.
      * 
      * @param all {@code true} to {@link AddOp#setUpdateOnly(boolean) stage changes) before commit,
      *        {@code false} to not do that. Defaults to {@code false}.
      * @return {@code this}, to ease command chaining
      */
     public CommitOp setAll(boolean all) {
         this.all = all;
         return this;
     }
 
     /**
      * @return the commit just applied, or {@code null} iif
      *         {@code getProgressListener().isCanceled()}
      * @see org.geogit.api.AbstractGeoGitOp#call()
      * @throws NothingToCommitException if there are no staged changes by comparing the index
      *         staging tree and the repository HEAD tree.
      */
     public RevCommit call() throws NothingToCommitException {
         final String committer = resolveCommitter();
         final String committerEmail = resolveCommitterEmail();
         final String author = resolveAuthor();
         final String authorEmail = resolveAuthorEmail();
 
         getProgressListener().started();
         float writeTreeProgress = 99f;
         if (all) {
             writeTreeProgress = 50f;
             command(AddOp.class).addPattern(".").setUpdateOnly(true)
                     .setProgressListener(subProgress(49f)).call();
         }
         if (getProgressListener().isCanceled()) {
             return null;
         }
 
         final Optional<Ref> currHead = command(RefParse.class).setName(Ref.HEAD).call();
         Preconditions.checkState(currHead.isPresent(), "Repository has no HEAD, can't commit");
         final Ref headRef = currHead.get();
         Preconditions
                 .checkState(headRef instanceof SymRef,//
                         "HEAD is in a dettached state, cannot commit. Create a branch from it before committing");
 
         final ObjectId currHeadCommitId = headRef.getObjectId();
         parents.add(currHeadCommitId);
 
         // final ObjectId newTreeId = index.writeTree(currHead.get(), subProgress(49f));
         final ObjectId newTreeId = command(WriteTree.class).setOldRoot(resolveOldRoot())
                 .setProgressListener(subProgress(writeTreeProgress)).call();
 
         if (getProgressListener().isCanceled()) {
             return null;
         }
 
         final ObjectId currentRootTreeId = command(ResolveTreeish.class)
                 .setTreeish(currHeadCommitId.toString()).call().or(ObjectId.NULL);
         if (currentRootTreeId.equals(newTreeId)) {
             if (!allowEmpty) {
                 throw new NothingToCommitException("Nothing to commit after " + currHeadCommitId);
             }
         }
 
         final ObjectId commitId;
         {
             CommitBuilder cb = new CommitBuilder();
             cb.setAuthor(author);
             cb.setAuthorEmail(authorEmail);
             cb.setCommitter(committer);
             cb.setCommitterEmail(committerEmail);
             cb.setMessage(getMessage());
             cb.setParentIds(parents);
             cb.setTreeId(newTreeId);
             cb.setTimestamp(getTimeStamp());
             // cb.setBounds(bounds);
 
             if (getProgressListener().isCanceled()) {
                 return null;
             }
             ObjectInserter objectInserter = repository.newObjectInserter();
             commitId = objectInserter.insert(repository.newCommitWriter(cb.build(ObjectId.NULL)));
         }
         final RevCommit commit = repository.getCommit(commitId);
         // set the HEAD pointing to the new commit
         final String branch = "refs/heads/master";
         final Optional<Ref> branchHead = command(UpdateRef.class).setName(branch)
                 .setNewValue(commitId).call();
         Preconditions.checkState(commitId.equals(branchHead.get().getObjectId()));
         LOGGER.fine("New head: " + branchHead);
 
         final Optional<SymRef> newHead = command(UpdateSymRef.class).setName(Ref.HEAD)
                 .setNewValue(branch).call();
         Preconditions.checkState(branch.equals(newHead.get().getTarget()));
 
         ObjectId treeId = repository.getCommit(branchHead.get().getObjectId()).getTreeId();
         Preconditions.checkState(newTreeId.equals(treeId));
 
         getProgressListener().progress(100f);
         getProgressListener().complete();
 
         return commit;
     }
 
     private Supplier<RevTree> resolveOldRoot() {
         Supplier<RevTree> supplier = new Supplier<RevTree>() {
             @Override
             public RevTree get() {
                 Optional<ObjectId> head = command(ResolveTreeish.class).setTreeish(Ref.HEAD).call();
                 ObjectId id = head.get();
                 if (id.isNull()) {
                     return command(CreateTree.class).setIndex(false).call();
                 }
                 return command(RevObjectParse.class).setObjectId(id).call(RevTree.class).get();
             }
         };
         return Suppliers.memoize(supplier);
     }
 
     private long getTimeStamp() {
         return timeStamp == null ? platform.currentTimeMillis() : timeStamp.longValue();
     }
 
     private String getMessage() {
         return message;
     }
 
     private String resolveCommitter() {
         if (committerName != null) {
             return committerName;
         }
         String key = "user.name";
         Optional<Map<String, String>> result = command(ConfigOp.class)
                 .setAction(ConfigAction.CONFIG_GET).setName(key).call();
         if (result.isPresent()) {
             return result.get().get(key);
         }
         throw new IllegalStateException(key + " not found in config. "
                + "Use geogit config [--global] " + key + " <your name> to configure it.");
     }
 
     private String resolveCommitterEmail() {
         if (committerEmail != null) {
             return committerEmail;
         }
         String key = "user.email";
         Optional<Map<String, String>> result = command(ConfigOp.class)
                 .setAction(ConfigAction.CONFIG_GET).setName(key).call();
         if (result.isPresent()) {
             return result.get().get(key);
         }
         throw new IllegalStateException(key + " not found in config. "
                + "Use geogit config [--global] " + key + " <your email> to configure it.");
     }
 
     private String resolveAuthor() {
         return authorName == null ? resolveCommitter() : authorName.orNull();
     }
 
     private String resolveAuthorEmail() {
         // only use provided authorEmail if authorName was provided
         return authorName == null ? resolveCommitterEmail() : authorEmail.orNull();
     }
 
     /**
      * @param allowEmptyCommit whether to allow a commit that represents no changes over its parent
      */
     public CommitOp setAllowEmpty(boolean allowEmptyCommit) {
         this.allowEmpty = allowEmptyCommit;
         return this;
     }
 
     public boolean isAllowEmpty() {
         return allowEmpty;
     }
 }
