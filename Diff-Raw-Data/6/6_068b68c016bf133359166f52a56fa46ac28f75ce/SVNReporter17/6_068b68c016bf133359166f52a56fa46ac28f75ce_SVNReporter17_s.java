 /*
  * ====================================================================
  * Copyright (c) 2004-2010 TMate Software Ltd.  All rights reserved.
  *
  * This software is licensed as described in the file COPYING, which
  * you should have received as part of this distribution.  The terms
  * are also available at http://svnkit.com/license.html.
  * If newer versions of this license are posted there, you may use a
  * newer version instead, at your option.
  * ====================================================================
  */
 package org.tmatesoft.svn.core.internal.wc17;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNDirEntry;
 import org.tmatesoft.svn.core.SVNErrorCode;
 import org.tmatesoft.svn.core.SVNErrorMessage;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
 import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
 import org.tmatesoft.svn.core.internal.util.SVNSkel;
 import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
 import org.tmatesoft.svn.core.internal.wc.SVNEventFactory;
 import org.tmatesoft.svn.core.internal.wc.SVNFileListUtil;
 import org.tmatesoft.svn.core.internal.wc.SVNFileType;
 import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.SVNWCDbKind;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.SVNWCDbLock;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.SVNWCDbStatus;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbAdditionInfo.AdditionInfoField;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbBaseInfo;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbBaseInfo.BaseInfoField;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbDeletionInfo.DeletionInfoField;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbInfo;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbInfo.InfoField;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbRepositoryInfo;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.WCDbRepositoryInfo.RepositoryInfoField;
 import org.tmatesoft.svn.core.internal.wc17.db.SVNWCDb;
 import org.tmatesoft.svn.core.internal.wc17.db.Structure;
 import org.tmatesoft.svn.core.internal.wc17.db.StructureFields.AdditionInfo;
 import org.tmatesoft.svn.core.internal.wc17.db.SvnWcDbShared;
 import org.tmatesoft.svn.core.io.ISVNReporter;
 import org.tmatesoft.svn.core.io.ISVNReporterBaton;
 import org.tmatesoft.svn.core.wc.SVNConflictChoice;
 import org.tmatesoft.svn.core.wc.SVNEventAction;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.util.ISVNDebugLog;
 import org.tmatesoft.svn.util.SVNLogType;
 
 /**
  * @version 1.3
  * @author TMate Software Ltd.
  */
 public class SVNReporter17 implements ISVNReporterBaton {
 
     private final File path;
     private SVNWCContext wcContext;
     private SVNDepth depth;
     private final boolean isRestoreFiles;
     private final boolean isUseDepthCompatibilityTrick;
     private final boolean isHonorDepthExclude;
     private boolean isUseCommitTimes;
     private int reportedFilesCount;
     private int totalFilesCount;
 
     public SVNReporter17(File path, SVNWCContext wcContext, boolean restoreFiles, boolean useDepthCompatibilityTrick, SVNDepth depth, boolean lockOnDemand, boolean isStatus,
             boolean isHonorDepthExclude, boolean isUseCommitTimes, ISVNDebugLog log) {
         this.path = path;
         this.wcContext = wcContext;
         this.isRestoreFiles = restoreFiles;
         this.isUseDepthCompatibilityTrick = useDepthCompatibilityTrick;
         this.depth = depth;
         this.isHonorDepthExclude = isHonorDepthExclude;
         this.isUseCommitTimes = isUseCommitTimes;
     }
 
     public int getReportedFilesCount() {
         return reportedFilesCount;
     }
 
     public int getTotalFilesCount() {
         return totalFilesCount;
     }
 
     public void report(ISVNReporter reporter) throws SVNException {
         assert (SVNWCDb.isAbsolute(path));
         
         SVNWCDbStatus status = null;
         SVNWCDbKind target_kind = null;
         long target_rev = 0;
         File repos_relpath = null;
         SVNURL repos_root = null;
         SVNDepth target_depth = SVNDepth.UNKNOWN;
         SVNWCDbLock target_lock = null;
        boolean explicit_rev, start_empty;
         SVNErrorMessage err = null;
         
         try {
 
             final WCDbBaseInfo baseInfo = wcContext.getDb().getBaseInfo(path, 
                     BaseInfoField.status, BaseInfoField.kind, BaseInfoField.revision, 
                     BaseInfoField.reposRelPath, BaseInfoField.reposRootUrl,
                     BaseInfoField.depth, BaseInfoField.lock);
 
             status = baseInfo.status;
             target_kind = baseInfo.kind;
             target_rev = baseInfo.revision;
             repos_relpath = baseInfo.reposRelPath;
             repos_root = baseInfo.reposRootUrl;
             target_depth = baseInfo.depth;
             target_lock = baseInfo.lock;
 
         } catch (SVNException e) {
             if (e.getErrorMessage().getErrorCode() != SVNErrorCode.WC_PATH_NOT_FOUND) {
                 throw e;
             }
             err = e.getErrorMessage();
         }
         if (err != null || (status != SVNWCDbStatus.Normal && status != SVNWCDbStatus.Incomplete)) {
             if (depth == SVNDepth.UNKNOWN) {
                 depth = SVNDepth.INFINITY;
             }
             reporter.setPath("", null, 0, depth, false);
             reporter.deletePath("");
             reporter.finishReport();
             return;
         }
         
         if (repos_relpath == null) {
             WCDbRepositoryInfo rInfo = wcContext.getDb().scanBaseRepository(path, RepositoryInfoField.relPath, RepositoryInfoField.rootUrl);
             repos_relpath = rInfo.relPath;
             repos_root = rInfo.rootUrl;
         }
         if (target_depth == SVNDepth.UNKNOWN) {
             target_depth = SVNDepth.INFINITY;
         }
         start_empty = status == SVNWCDbStatus.Incomplete;
         if (isUseDepthCompatibilityTrick 
                 && target_depth.compareTo(SVNDepth.IMMEDIATES) <= 0 
                 && depth.compareTo(target_depth) > 0) {
             start_empty = true;
         }
         SVNFileType diskType = SVNFileType.UNKNOWN;
         if (isRestoreFiles) {
             diskType = SVNFileType.getType(path);
         } 
             
         if (isRestoreFiles && diskType == SVNFileType.NONE) {
             // restore node
             WCDbInfo wInfo = null;
             SVNWCDbKind wrkKind;
             SVNWCDbStatus wrkStatus;
             try {
                 wInfo = wcContext.getDb().readInfo(path, InfoField.status, InfoField.kind);
                 wrkStatus = wInfo.status;
                 wrkKind = wInfo.kind;
             } catch (SVNException e) {
                 if (e.getErrorMessage().getErrorCode() == SVNErrorCode.WC_PATH_NOT_FOUND) {
                     wrkStatus = SVNWCDbStatus.NotPresent;
                     wrkKind = SVNWCDbKind.File;
                 } else {
                     throw e;
                 }
             }
             if (wrkStatus == SVNWCDbStatus.Added) {
                 Structure<AdditionInfo> additionInfo = SvnWcDbShared.scanAddition((SVNWCDb) wcContext.getDb(), path, AdditionInfo.status);
                 wrkStatus = additionInfo.get(AdditionInfo.status);
                 additionInfo.release();
             }
             if (wrkStatus == SVNWCDbStatus.Normal
                     || wrkStatus == SVNWCDbStatus.Copied
                     || wrkStatus == SVNWCDbStatus.MovedHere
                     || (wrkKind == SVNWCDbKind.Dir && (wrkStatus == SVNWCDbStatus.Added || wrkStatus == SVNWCDbStatus.Incomplete))) {
                 restoreNode(wcContext, path, wrkKind, target_rev, isUseCommitTimes);
             }
         }
         
         try {
             SVNDepth reportDepth = target_depth;
             if (isHonorDepthExclude && depth != SVNDepth.UNKNOWN && depth.compareTo(target_depth) < 0) {
                 reportDepth = depth;
             }
             reporter.setPath("", null, target_rev, reportDepth, start_empty);
             if (target_kind == SVNWCDbKind.Dir) {
                 if (depth != SVNDepth.EMPTY) {
                     reportRevisionsAndDepths(path, 
                             SVNFileUtil.createFilePath(""), 
                             target_rev, repos_relpath, repos_root, reportDepth, reporter, isRestoreFiles, depth, start_empty);
                 }
             } else if (target_kind == SVNWCDbKind.Symlink || target_kind == SVNWCDbKind.File) {
                 String base = SVNFileUtil.getFileName(path);
                 File parentPath = SVNFileUtil.getParentFile(path);
                 WCDbBaseInfo parentInfo = wcContext.getDb().getBaseInfo(parentPath, BaseInfoField.reposRelPath);
                 if (!repos_relpath.equals(SVNFileUtil.createFilePath(parentInfo.reposRelPath, base))) {
                     SVNURL url = SVNWCUtils.join(repos_root, repos_relpath);
                     reporter.linkPath(url, "", target_lock != null ? target_lock.token : null, target_rev, SVNDepth.INFINITY, false);
                 } else if (target_lock != null) {
                     reporter.setPath("", target_lock.token, target_rev, SVNDepth.INFINITY, false);
                 }
             } 
             reporter.finishReport();
         } catch (SVNException e) {
             // abort report
             try {
                 reporter.abortReport();
             } catch (SVNException inner) {
                 e.getErrorMessage().setChildErrorMessage(inner.getErrorMessage());
             }
             throw e;
         }
     }
 
     public static boolean restoreNode(SVNWCContext context, File local_abspath, SVNWCDbKind kind, long target_rev, boolean useCommitTimes) throws SVNException {
         boolean restored = false;
 
         /*
          * Currently we can only restore files, but we will be able to restore
          * directories after we move to a single database and pristine store.
          */
         if (kind == SVNWCDbKind.File || kind == SVNWCDbKind.Symlink) {
             /* ... recreate file from text-base, and ... */
             restoreFile(context, local_abspath, useCommitTimes, true);
             restored = true;
         } else if (kind == SVNWCDbKind.Dir) {
             /* Recreating a directory is just a mkdir */
             local_abspath.mkdirs();
             restored = true;
         }
 
         if (restored) {
             /* ... report the restoration to the caller. */
             if (context.getEventHandler() != null) {
                 context.getEventHandler().handleEvent(SVNEventFactory.createSVNEvent(local_abspath, SVNNodeKind.FILE, null, target_rev, SVNEventAction.RESTORE, null, null, null), 0);
             }
         }
         return restored;
     }
 
     /**
      * Helper for svn_wc_crawl_revisions5() that finds a base revision for a
      * node that doesn't have one itself.
      */
     private long findBaseRev(File local_abspath, File top_local_abspath) throws SVNException {
         File op_root_abspath;
 
         final WCDbInfo readInfo = wcContext.getDb().readInfo(local_abspath, InfoField.status, InfoField.revision, InfoField.haveBase);
         SVNWCDbStatus status = readInfo.status;
         boolean have_base = readInfo.haveBase;
         long baseRev = readInfo.revision;
 
         if (SVNRevision.isValidRevisionNumber(baseRev))
             return baseRev;
 
         if (have_base) {
             return wcContext.getDb().getBaseInfo(local_abspath, BaseInfoField.revision).revision;
         }
 
         if (status == SVNWCDbStatus.Added) {
             op_root_abspath = wcContext.getDb().scanAddition(local_abspath, AdditionInfoField.opRootAbsPath).opRootAbsPath;
             return findBaseRev(SVNFileUtil.getFileDir(op_root_abspath), top_local_abspath);
         } else if (status == SVNWCDbStatus.Deleted) {
             File work_del_abspath = wcContext.getDb().scanDeletion(local_abspath, DeletionInfoField.workDelAbsPath).workDelAbsPath;
             if (work_del_abspath != null)
                 return findBaseRev(work_del_abspath, top_local_abspath);
         }
 
         SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.WC_CORRUPT, "Can't retrieve base revision for ''{0}''", top_local_abspath);
         SVNErrorManager.error(err, SVNLogType.WC);
         return SVNWCContext.INVALID_REVNUM;
     }
 
     private void reportRevisionsAndDepths(File dirPath, File reportRelPath, long dirRev, File dirReposRelPath, SVNURL dirReposRoot,  
             SVNDepth dirDepth, ISVNReporter reporter, boolean restoreFiles, SVNDepth depth, boolean reportEverything) throws SVNException {
         
         Set<String> baseChildren = wcContext.getDb().getBaseChildren(dirPath);
         Set<String> dirEntries = null;
         if (restoreFiles) {
             dirEntries = new HashSet<String>();
             File[] list = SVNFileListUtil.listFiles(dirPath);
             if (list != null) {
                 for (File file : list) {
                     dirEntries.add(SVNFileUtil.getFileName(file));
                 }
             }
         }
         for (String child : baseChildren) {
             boolean thisSwitched = false;
             wcContext.checkCancelled();
             
             String thisReportRelpath = SVNFileUtil.getFilePath(SVNFileUtil.createFilePath(reportRelPath, child));
             File thisAbsPath = SVNFileUtil.createFilePath(dirPath, child);
             
             WCDbBaseInfo ths = wcContext.getDb().getBaseInfo(thisAbsPath, BaseInfoField.updateRoot, BaseInfoField.status, BaseInfoField.revision, BaseInfoField.reposRelPath, BaseInfoField.depth, BaseInfoField.kind, BaseInfoField.lock);
             
             if (ths.updateRoot) {
                 continue;
             }
             if (ths.status == SVNWCDbStatus.Excluded) {
                 if (isHonorDepthExclude) {
                     reporter.setPath(thisReportRelpath, null, dirRev, SVNDepth.EXCLUDE, false);
                 } else {
                     if (!reportEverything) {
                         reporter.deletePath(thisReportRelpath);
                     }
                 }
                 continue;
             }
             if (ths.status == SVNWCDbStatus.Excluded || ths.status == SVNWCDbStatus.NotPresent) {
                 if (!reportEverything) {
                     reporter.deletePath(thisReportRelpath);
                 }
                 continue;
             }
             if (restoreFiles && !dirEntries.contains(child)) {
                 WCDbInfo wInfo = null;
                 SVNWCDbKind wrkKind;
                 SVNWCDbStatus wrkStatus;
                 try {
                     wInfo = wcContext.getDb().readInfo(thisAbsPath, InfoField.status, InfoField.kind);
                     wrkStatus = wInfo.status;
                     wrkKind = wInfo.kind;
                 } catch (SVNException e) {
                     if (e.getErrorMessage().getErrorCode() == SVNErrorCode.WC_PATH_NOT_FOUND) {
                         wrkStatus = SVNWCDbStatus.NotPresent;
                         wrkKind = SVNWCDbKind.File;
                     } else {
                         throw e;
                     }
                 }
                 if (wrkStatus == SVNWCDbStatus.Added) {
                     Structure<AdditionInfo> additionInfo = SvnWcDbShared.scanAddition((SVNWCDb) wcContext.getDb(), thisAbsPath, AdditionInfo.status);
                     wrkStatus = additionInfo.get(AdditionInfo.status);
                     additionInfo.release();
                 }
                 if (wrkStatus == SVNWCDbStatus.Normal
                         || wrkStatus == SVNWCDbStatus.Copied
                         || wrkStatus == SVNWCDbStatus.MovedHere
                         || (wrkKind == SVNWCDbKind.Dir && (wrkStatus == SVNWCDbStatus.Added || wrkStatus == SVNWCDbStatus.Incomplete))) {
                     if (SVNFileType.getType(thisAbsPath) == SVNFileType.NONE) {
                         restoreNode(wcContext, thisAbsPath, wrkKind, dirRev, isUseCommitTimes);
                     }
                 }                
             }
             if (ths.reposRelPath == null) {
                 ths.reposRelPath = SVNFileUtil.createFilePath(dirReposRelPath, child);
             } else {
                if (!SVNWCUtils.isChild(dirReposRelPath, ths.reposRelPath)) {
                     thisSwitched = true;
                 }
             }
             if (ths.depth == SVNDepth.UNKNOWN) {
                 ths.depth = SVNDepth.INFINITY;
             }
             if (ths.kind == SVNWCDbKind.File || ths.kind == SVNWCDbKind.Symlink) {
                  if (reportEverything) {
                      if (thisSwitched) {
                          SVNURL url = SVNWCUtils.join(dirReposRoot, ths.reposRelPath);
                          reporter.linkPath(url, thisReportRelpath, 
                                  ths.lock != null ? ths.lock.token : null, 
                                          ths.revision, ths.depth, false);
                      } else {
                          reporter.setPath(thisReportRelpath, ths.lock != null ? ths.lock.token : null, ths.revision, ths.depth, false);
                      }
                  } else if (thisSwitched) {
                      SVNURL url = SVNWCUtils.join(dirReposRoot, ths.reposRelPath);
                      reporter.linkPath(url, thisReportRelpath, 
                              ths.lock != null ? ths.lock.token : null, 
                                      ths.revision, ths.depth, false);
                  } else if (ths.revision != dirRev || ths.lock != null || dirDepth == SVNDepth.EMPTY) {
                      reporter.setPath(thisReportRelpath, ths.lock != null ? ths.lock.token : null, ths.revision, ths.depth, false);
                  }
             } else if (ths.kind == SVNWCDbKind.Dir && (depth == SVNDepth.UNKNOWN || depth.compareTo(SVNDepth.FILES) > 0)) {
                 boolean isIncomplete = ths.status == SVNWCDbStatus.Incomplete;
                 boolean startEmpty = isIncomplete;
                 if (isIncomplete && ths.revision < 0) {
                     ths.revision = dirRev;
                 }
                 if (isUseDepthCompatibilityTrick 
                         && ths.depth.compareTo(SVNDepth.FILES) <= 0
                         && depth.compareTo(ths.depth) > 0) {
                     startEmpty = true;
                 }
                 if (reportEverything) {
                     if (thisSwitched) {
                         SVNURL url = SVNWCUtils.join(dirReposRoot, ths.reposRelPath);
                         reporter.linkPath(url, thisReportRelpath, 
                                 ths.lock != null ? ths.lock.token : null, 
                                         ths.revision, ths.depth, startEmpty);
                     } else {
                         reporter.setPath(thisReportRelpath, ths.lock != null ? ths.lock.token : null, ths.revision, ths.depth, startEmpty);
                     }
                 } else if (thisSwitched) {
                     SVNURL url = SVNWCUtils.join(dirReposRoot, ths.reposRelPath);
                     reporter.linkPath(url, thisReportRelpath, 
                             ths.lock != null ? ths.lock.token : null, 
                                     ths.revision, ths.depth, startEmpty);
                 } else if (ths.revision != dirRev 
                         || ths.lock != null 
                         || dirDepth == SVNDepth.EMPTY
                         || dirDepth == SVNDepth.FILES
                         || (dirDepth == SVNDepth.IMMEDIATES && ths.depth != SVNDepth.EMPTY)
                         || (ths.depth.compareTo(SVNDepth.INFINITY) < 0 && depth.isRecursive())) {
                     reporter.setPath(thisReportRelpath, ths.lock != null ? ths.lock.token : null, ths.revision, ths.depth, startEmpty);
                 }
                 if (depth.isRecursive()) {
                     File reposRelPath = ths.reposRelPath;
                     if (reposRelPath == null) {
                         reposRelPath = SVNFileUtil.createFilePath(dirReposRelPath, child);
                     }
                     reportRevisionsAndDepths(thisAbsPath, 
                             SVNFileUtil.createFilePath(thisReportRelpath), 
                             ths.revision, 
                             reposRelPath, 
                             dirReposRoot, ths.depth, reporter, restoreFiles, depth, startEmpty);
                 }
             }
         }
     }
 
     private void reportRevisionsAndDepths(File anchor_abspath, String dir_path, long dir_rev, ISVNReporter reporter, boolean report_everything) throws SVNException {
 
         /*
          * Get both the SVN Entries and the actual on-disk entries. Also notice
          * that we're picking up hidden entries too (read_children never hides
          * children).
          */
         File dir_abspath = SVNFileUtil.createFilePath(anchor_abspath, dir_path);
 
         Set<String> base_children = wcContext.getDb().getBaseChildren(dir_abspath);
 
         Set<String> dirents = new HashSet<String>();
         {
             File[] list = SVNFileListUtil.listFiles(dir_abspath);
             if (list != null) {
                 for (File file : list) {
                     dirents.add(SVNFileUtil.getFileName(file));
                 }
             }
         }
 
         /*** Do the real reporting and recursing. ***/
 
         /* First, look at "this dir" to see what its URL and depth are. */
         final WCDbInfo readInfo = wcContext.getDb().readInfo(dir_abspath, InfoField.reposRelPath, InfoField.reposRootUrl, InfoField.depth);
         SVNURL dir_repos_root = readInfo.reposRootUrl;
         File dir_repos_relpath = readInfo.reposRelPath;
         SVNDepth dir_depth = readInfo.depth;
 
         /* If the directory has no url, search its parents */
         if (dir_repos_relpath == null) {
             final WCDbRepositoryInfo scan = wcContext.getDb().scanBaseRepository(dir_abspath, RepositoryInfoField.relPath, RepositoryInfoField.rootUrl);
             dir_repos_relpath = scan.relPath;
             dir_repos_root = scan.rootUrl;
         }
 
         /*
          * If "this dir" has "svn:externals" property set on it, call the
          * external_func callback.
          */
         // TODO external_func ?
 
         // if (external_func)
         // SVN_ERR(read_externals_info(db, dir_abspath, external_func,
         // external_baton, dir_depth, iterpool));
 
         /* Looping over current directory's BASE children: */
         for (String child : base_children) {
 
             /* Compute the paths and URLs we need. */
             File this_path = SVNFileUtil.createFilePath(dir_path, child);
             File this_abspath = SVNFileUtil.createFilePath(dir_abspath, child);
 
             SVNWCDbStatus this_status;
             SVNWCDbKind this_kind = null;
             long this_rev = SVNWCContext.INVALID_REVNUM;
             File this_repos_relpath = null;
             SVNDepth this_depth = null;
             SVNWCDbLock this_lock = null;
             boolean this_switched;
 
             try {
 
                 final WCDbBaseInfo baseInfo = wcContext.getDb().getBaseInfo(this_abspath, BaseInfoField.status, BaseInfoField.kind, BaseInfoField.revision, BaseInfoField.reposRelPath,
                         BaseInfoField.reposRootUrl, BaseInfoField.depth, BaseInfoField.lock, BaseInfoField.updateRoot);
 
                 if (baseInfo.updateRoot) {
                     continue;
                 }
                 
                 this_status = baseInfo.status;
                 this_kind = baseInfo.kind;
                 this_rev = baseInfo.revision;
                 this_repos_relpath = baseInfo.reposRelPath;
                 this_depth = baseInfo.depth;
                 this_lock = baseInfo.lock;
                 
 
             } catch (SVNException e) {
 
                 if (e.getErrorMessage().getErrorCode() != SVNErrorCode.WC_PATH_NOT_FOUND)
                     throw e;
 
                 /*
                  * THIS_ABSPATH was listed as a BASE child of DIR_ABSPATH. Yet,
                  * we just got an error trying to read it. What gives? :-P
                  *
                  * This happens when THIS_ABSPATH is a subdirectory that is
                  * marked in the parent stub as "not-present". The subdir is
                  * then removed. Later, an addition is scheduled, putting the
                  * subdirectory back, but ONLY containing WORKING nodes.
                  *
                  * Thus, the BASE fetch comes out of the subdir, and fails.
                  *
                  * For this case, we go ahead and treat this as a simple
                  * not-present, and ignore whatever is in the subdirectory.
                  */
 
                 this_status = SVNWCDbStatus.NotPresent;
 
                 /*
                  * Note: the other THIS_* local variables pass to base_get_info
                  * are NOT set at this point. But we don't need them...
                  */
             }
 
             /*
              * Note: some older code would attempt to check the parent stub of
              * subdirectories for the not-present state. That check was
              * redundant since a not-present directory has no BASE nodes within
              * it which may report another status.
              *
              * There might be NO BASE node (per the condition above), but the
              * typical case is that base_get_info() reads the parent stub
              * because there is no subdir (with administrative data). Thus, we
              * already have all the information we need. No further testing.
              */
 
             /* First check for exclusion */
             if (this_status == SVNWCDbStatus.Excluded) {
                 if (isHonorDepthExclude) {
                     /*
                      * Report the excluded path, no matter whether
                      * report_everything flag is set. Because the
                      * report_everything flag indicates that the server will
                      * treat the wc as empty and thus push full content of the
                      * files/subdirs. But we want to prevent the server from
                      * pushing the full content of this_path at us.
                      */
 
                     /*
                      * The server does not support link_path report on excluded
                      * path. We explicitly prohibit this situation in
                      * svn_wc_crop_tree().
                      */
                     reporter.setPath(SVNFileUtil.getFilePath(this_path), null, dir_rev, SVNDepth.EXCLUDE, false);
                 } else {
                     /*
                      * We want to pull in the excluded target. So, report it as
                      * deleted, and server will respond properly.
                      */
                     if (!report_everything)
                         reporter.deletePath(SVNFileUtil.getFilePath(this_path));
                 }
                 continue;
             }
 
             /*** The Big Tests: ***/
             if (this_status == SVNWCDbStatus.ServerExcluded || this_status == SVNWCDbStatus.NotPresent) {
                 /*
                  * If the entry is 'absent' or 'not-present', make sure the
                  * server knows it's gone... ...unless we're reporting
                  * everything, in which case we're going to report it missing
                  * later anyway.
                  *
                  * This instructs the server to send it back to us, if it is now
                  * available (an addition after a not-present state), or if it
                  * is now authorized (change in authz for the absent item).
                  */
                 if (!report_everything)
                     reporter.deletePath(SVNFileUtil.getFilePath(this_path));
                 continue;
             }
 
             /* Is the entry NOT on the disk? We may be able to restore it. */
             if (!dirents.contains(child)) {
 
                 final WCDbInfo info = wcContext.getDb().readInfo(this_abspath, InfoField.status, InfoField.kind);
                 SVNWCDbStatus wrk_status = info.status;
                 SVNWCDbKind wrk_kind = info.kind;
 
                 if (wrk_status == SVNWCDbStatus.Added)
                     wrk_status = wcContext.getDb().scanAddition(this_abspath, AdditionInfoField.status).status;
 
                 if (isRestoreFiles && wrk_status != SVNWCDbStatus.Added && wrk_status != SVNWCDbStatus.Deleted && wrk_status != SVNWCDbStatus.Excluded && wrk_status != SVNWCDbStatus.NotPresent
                         && wrk_status != SVNWCDbStatus.ServerExcluded) {
                     /*
                      * It is possible on a case insensitive system that the
                      * entry is not really missing, but just cased incorrectly.
                      * In this case we can't overwrite it with the pristine
                      * version
                      */
                     SVNNodeKind dirent_kind = SVNFileType.getNodeKind(SVNFileType.getType(this_abspath));
 
                     if (dirent_kind == SVNNodeKind.NONE) {
                         restoreNode(wcContext, this_abspath, wrk_kind, this_rev, isUseCommitTimes);
                     }
                 }
 
                 /*
                  * With single-db, we always know about all children, so never
                  * tell the server that we don't know, but want to know about
                  * the missing child.
                  */
 
             }
 
             /* And finally prepare for reporting */
             if (this_repos_relpath == null) {
                 this_switched = false;
                 this_repos_relpath = SVNFileUtil.createFilePath(dir_repos_relpath, child);
             } else {
                 final String childname = SVNWCUtils.getPathAsChild(dir_repos_relpath, this_repos_relpath);
 
                 if (childname == null || !childname.equals(child))
                     this_switched = true;
                 else
                     this_switched = false;
             }
 
             /* Tweak THIS_DEPTH to a useful value. */
             if (this_depth == SVNDepth.UNKNOWN)
                 this_depth = SVNDepth.INFINITY;
 
             /*
              * Obstructed nodes might report SVN_INVALID_REVNUM. Tweak it.
              *
              * ### it seems that obstructed nodes should be handled quite a ###
              * bit differently. maybe reported as missing, like not-present ###
              * or absent nodes?
              */
             if (!SVNRevision.isValidRevisionNumber(this_rev))
                 this_rev = dir_rev;
 
             /*** Files ***/
             if (this_kind == SVNWCDbKind.File || this_kind == SVNWCDbKind.Symlink) {
                 if (report_everything) {
                     /* Report the file unconditionally, one way or another. */
                     if (this_switched)
                         reporter.linkPath(SVNURL.parseURIEncoded(SVNPathUtil.append(dir_repos_root.toString(), SVNEncodingUtil.uriEncode(SVNFileUtil.getFilePath(this_repos_relpath)))),
                                 SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, false);
                     else
                         reporter.setPath(SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, false);
                 }
 
                 /* Possibly report a disjoint URL ... */
                 else if (this_switched)
                     reporter.linkPath(SVNURL.parseURIEncoded(SVNPathUtil.append(dir_repos_root.toString(), SVNEncodingUtil.uriEncode(SVNFileUtil.getFilePath(this_repos_relpath)))),
                             SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, false);
                 /*
                  * ... or perhaps just a differing revision or lock token, or
                  * the mere presence of the file in a depth-empty dir.
                  */
                 else if (this_rev != dir_rev || this_lock != null || dir_depth == SVNDepth.EMPTY)
                     reporter.setPath(SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, false);
             } /* end file case */
 
             /*** Directories (in recursive mode) ***/
             else if (this_kind == SVNWCDbKind.Dir && (depth.compareTo(SVNDepth.FILES) > 0 || depth == SVNDepth.UNKNOWN)) {
                 boolean is_incomplete;
                 boolean start_empty;
 
                 /*
                  * If the subdir and its administrative area are not present,
                  * then do NOT bother to report this node, much less recurse
                  * into the thing.
                  *
                  * Note: if the there is nothing on the disk, then we may have
                  * reported it missing further above.
                  *
                  * ### hmm. but what if we have a *file* obstructing the dir?
                  * ### the code above will not report it, and we'll simply ###
                  * skip it right here. I guess with an obstruction, we ### can't
                  * really do anything with info the server might ### send, so
                  * maybe this is just fine.
                  */
                 if (this_status == SVNWCDbStatus.Obstructed)
                     continue;
 
                 is_incomplete = (this_status == SVNWCDbStatus.Incomplete);
                 start_empty = is_incomplete;
 
                 if (isUseDepthCompatibilityTrick && this_depth.compareTo(SVNDepth.FILES) <= 0 && depth.compareTo(this_depth) > 0) {
                     start_empty = true;
                 }
 
                 if (report_everything) {
                     /* Report the dir unconditionally, one way or another. */
                     if (this_switched)
                         reporter.linkPath(SVNURL.parseURIEncoded(SVNPathUtil.append(dir_repos_root.toString(), SVNEncodingUtil.uriEncode(SVNFileUtil.getFilePath(this_repos_relpath)))),
                                 SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, start_empty);
                     else
                         reporter.setPath(SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, start_empty);
                 }
 
                 /* Possibly report a disjoint URL ... */
                 else if (this_switched)
                     reporter.linkPath(SVNURL.parseURIEncoded(SVNPathUtil.append(dir_repos_root.toString(), SVNEncodingUtil.uriEncode(SVNFileUtil.getFilePath(this_repos_relpath)))),
                             SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, start_empty);
                 /*
                  * ... or perhaps just a differing revision, lock token,
                  * incomplete subdir, the mere presence of the directory in a
                  * depth-empty or depth-files dir, or if the parent dir is at
                  * depth-immediates but the child is not at depth-empty. Also
                  * describe shallow subdirs if we are trying to set depth to
                  * infinity.
                  */
                 else if (this_rev != dir_rev || this_lock != null || is_incomplete || dir_depth == SVNDepth.EMPTY || dir_depth == SVNDepth.FILES
                         || (dir_depth == SVNDepth.IMMEDIATES && this_depth != SVNDepth.EMPTY) || (this_depth.compareTo(SVNDepth.INFINITY) < 0 && depth.isRecursive()))
                     reporter.setPath(SVNFileUtil.getFilePath(this_path), this_lock != null ? this_lock.token : null, this_rev, this_depth, start_empty);
 
                 if (SVNDepth.recurseFromDepth(depth))
                     reportRevisionsAndDepths(anchor_abspath, SVNFileUtil.getFilePath(this_path), this_rev, reporter, start_empty);
             } /* end directory case */
         } /* end main entries loop */
 
         return;
     }
 
     /**
      * Helper for report_revisions_and_depths().
      *
      * Perform an atomic restoration of the file LOCAL_ABSPATH; that is, copy
      * the file's text-base to the administrative tmp area, and then move that
      * file to LOCAL_ABSPATH with possible translations/expansions. If
      * USE_COMMIT_TIMES is set, then set working file's timestamp to
      * last-commit-time. Either way, set entry-timestamp to match that of the
      * working file when all is finished.
      *
      * Not that a valid access baton with a write lock to the directory of
      * LOCAL_ABSPATH must be available in DB.
      */
     private static void restoreFile(SVNWCContext context, File localAbsPath, boolean useCommitTimes, boolean removeTextConflicts) throws SVNException {
         SVNSkel workItem = context.wqBuildFileInstall(localAbsPath, null, useCommitTimes, true);
         context.getDb().addWorkQueue(localAbsPath, workItem);
         if (removeTextConflicts) {
             resolveTextConflict(context, localAbsPath);
         }
     }
 
     private static void resolveTextConflict(SVNWCContext context, File localAbsPath) throws SVNException {
         context.resolveConflictOnNode(localAbsPath, true, false, SVNConflictChoice.MERGED);
     }
 
 }
