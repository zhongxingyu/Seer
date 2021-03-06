 /*
  * Created on 26.05.2005
  */
 package org.tmatesoft.svn.core.wc;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.tmatesoft.svn.core.SVNProperty;
 import org.tmatesoft.svn.core.internal.wc.SVNDirectory;
 import org.tmatesoft.svn.core.internal.wc.SVNEntries;
 import org.tmatesoft.svn.core.internal.wc.SVNEntry;
 import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
 import org.tmatesoft.svn.core.internal.wc.SVNEventFactory;
 import org.tmatesoft.svn.core.internal.wc.SVNExternalInfo;
 import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
 import org.tmatesoft.svn.core.internal.wc.SVNProperties;
 import org.tmatesoft.svn.core.internal.wc.SVNTranslator;
 import org.tmatesoft.svn.core.internal.wc.SVNWCAccess;
 import org.tmatesoft.svn.core.io.ISVNCredentialsProvider;
 import org.tmatesoft.svn.core.io.SVNDirEntry;
 import org.tmatesoft.svn.core.io.SVNException;
 import org.tmatesoft.svn.core.io.SVNLock;
 import org.tmatesoft.svn.core.io.SVNNodeKind;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.io.SVNRepositoryLocation;
 import org.tmatesoft.svn.util.DebugLog;
 import org.tmatesoft.svn.util.PathUtil;
 import org.tmatesoft.svn.util.TimeUtil;
 
 public class SVNWCClient extends SVNBasicClient {
 
 	public SVNWCClient(final ISVNCredentialsProvider credentials, ISVNEventListener eventDispatcher) {
 		this(credentials, null, eventDispatcher);
     }
 
     public SVNWCClient(final ISVNCredentialsProvider credentials, SVNOptions options, ISVNEventListener eventDispatcher) {
         super(new ISVNRepositoryFactory() {
             public SVNRepository createRepository(String url) throws SVNException {
                 SVNRepository repos = SVNRepositoryFactory.create(SVNRepositoryLocation.parseURL(url));
                 repos.setCredentialsProvider(credentials);
                 return repos;
             }
         }, options, eventDispatcher);
     }
 
     public SVNWCClient(ISVNRepositoryFactory repositoryFactory, SVNOptions options, ISVNEventListener eventDispatcher) {
         super(repositoryFactory, options, eventDispatcher);
     }
     
     public void doCleanup(File path) throws SVNException {
         if (!SVNWCAccess.isVersionedDirectory(path)) {
             SVNErrorManager.error(0, null);
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         wcAccess.open(true, true, true);
         wcAccess.getAnchor().cleanup();
         wcAccess.close(true, true);
     }
     
     public void doSetProperty(File path, String propName, String propValue, boolean force, boolean recursive, ISVNPropertyHandler handler) throws SVNException {
         propName = validatePropertyName(propName);
         if (REVISION_PROPS.contains(propName)) {
             SVNErrorManager.error("svn: Revision property '" + propName + "' not allowed in this context");
         } else if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName + "' is a wcprop , thus not accessible to clients");
         }
         propValue = validatePropertyValue(propName, propValue, force);
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, recursive);
             doSetLocalProperty(wcAccess.getAnchor(), wcAccess.getTargetName(), propName, propValue, force, recursive, handler);
         } finally {
             wcAccess.close(true, recursive);
         }
     }
 
     public void doSetRevisionProperty(File path, SVNRevision revision, String propName, String propValue, boolean force, ISVNPropertyHandler handler) throws SVNException {
         propName = validatePropertyName(propName);
         if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName + "' is a wcprop , thus not accessible to clients");
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, false);
             String url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
             SVNRevision pegRevision = SVNRevision.parse(wcAccess.getTargetEntryProperty(SVNProperty.REVISION));
             doSetRevisionProperty(url, pegRevision, revision, propName, propValue, force, handler);
         } finally {
             wcAccess.close(true, false);
         }
     }
 
     public void doSetRevisionProperty(String url, SVNRevision pegRevision, SVNRevision revision, String propName, String propValue, boolean force, ISVNPropertyHandler handler) throws SVNException {
         propName = validatePropertyName(propName);
         if (!force && "svn:author".equals(propName) && propValue != null && propValue.indexOf('\n') >= 0) {
             SVNErrorManager.error("svn: Value will not be set unless forced");
         }
         if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         url = getURL(url, pegRevision, revision);
         long revNumber = getRevisionNumber(url, revision);
         // 
         SVNRepository repos = createRepository(url);
         repos.setRevisionPropertyValue(revNumber, propName, propValue);
         if (handler != null) {
             handler.handleProperty(revNumber + "", new SVNPropertyData(propName, propValue));
         }
     }
     
     public void doGetProperty(File path, String propName, SVNRevision pegRevision, SVNRevision revision, boolean recursive, ISVNPropertyHandler handler) throws SVNException {
        if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.WORKING;
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, recursive);
             if (revision != SVNRevision.WORKING && revision != SVNRevision.BASE) {
                 String url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
                 if (pegRevision == null || !pegRevision.isValid()) {
                     pegRevision = SVNRevision.parse(wcAccess.getTargetEntryProperty(SVNProperty.REVISION));
                 }
                 revision = SVNRevision.create(getRevisionNumber(path, revision));
                 doGetProperty(url, propName, pegRevision, revision, recursive, handler);
                 return;
             }
             // local prop.
             doGetLocalProperty(wcAccess.getAnchor(), wcAccess.getTargetName(), propName, revision, recursive, handler);
         } finally {
             wcAccess.close(true, recursive);
         }
     }
 
     public void doGetProperty(String url, String propName, SVNRevision pegRevision, SVNRevision revision, boolean recursive, ISVNPropertyHandler handler) throws SVNException {
        if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         url = getURL(url, pegRevision, revision);
         
         SVNRepository repos = createRepository(url);
         doGetRemoteProperty(url, "", repos, propName, revision, recursive, handler);
     }
 
     public void doGetRevisionProperty(File path, String propName, SVNRevision pegRev, SVNRevision revision, ISVNPropertyHandler handler) throws SVNException {
        if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName + "' is a wcprop , thus not accessible to clients");
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, false);
             String url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
             long revNumber = getRevisionNumber(path, revision);
             if (pegRev == null || !pegRev.isValid()) {
                 pegRev = SVNRevision.parse(wcAccess.getTargetEntryProperty(SVNProperty.REVISION));
             }
             revision = SVNRevision.create(revNumber);
             url = getURL(url, pegRev, revision);
             doGetRevisionProperty(url, propName, revision, handler);
         } finally {
             wcAccess.close(true, false);
         }
     }
 
     public void doGetRevisionProperty(String url, String propName, SVNRevision revision, ISVNPropertyHandler handler) throws SVNException {
         if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         SVNRepository repos = createRepository(url);
         long revNumber = getRevisionNumber(url, revision);
         if (propName != null) {
             String value = repos.getRevisionPropertyValue(revNumber, propName);
             if (value != null) {
                 handler.handleProperty(revNumber + "", new SVNPropertyData(propName, value));
             }
         } else {
             Map props = new HashMap();
             repos.getRevisionProperties(revNumber, props);
             for (Iterator names = props.keySet().iterator(); names.hasNext();) {
                 String name = (String) names.next();
                 String value = (String) props.get(name);
                 handler.handleProperty(revNumber + "", new SVNPropertyData(name, value));
             }
         }
     }
     
     public void doDelete(File path, boolean force, boolean dryRun) throws SVNException {
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
         	wcAccess.open(true, true, true);
         	if (!force) {
         		wcAccess.getAnchor().canScheduleForDeletion(wcAccess.getTargetName());
         	}
         	if (!dryRun) {
         		wcAccess.getAnchor().scheduleForDeletion(wcAccess.getTargetName());
         	}
         } finally {
         	wcAccess.close(true, true);
         }
     }
 
     public void doAdd(File path, boolean force, boolean mkdir, boolean recursive) throws SVNException {
     	if (mkdir && !path.exists()) {
     		path.mkdirs();
     	}
     	if (!path.exists()) {
     		// error
     	}
     	SVNWCAccess wcAccess = createWCAccess(path);
     	try {
     		wcAccess.open(true, recursive);
     		String name = wcAccess.getTargetName();
 
     		if ("".equals(name) && !force) {
     			// error.
     		}
 			SVNDirectory dir = wcAccess.getAnchor();
     		if (path.isFile() || SVNFileUtil.isSymlink(path)) {
     			addSingleFile(wcAccess, dir, name);
     		} else if (path.isDirectory() && recursive) {
 	    		// add dir and recurse.
     			addDirectory(wcAccess, wcAccess.getAnchor(), name, force);
 	    	} else {
 	    		// add single dir
 	    		dir.add(wcAccess.getTargetName(), true);
 	    	}
     	} finally {
     		wcAccess.close(true, recursive);
     	}
     }
 
 	public void doRevert(File path, boolean recursive) throws SVNException {
         SVNWCAccess wcAccess = createWCAccess(path);
         
         // force recursive lock.
         boolean reverted = false;
         boolean replaced = false;
         SVNNodeKind kind = null;
         Collection recursiveFiles = new ArrayList();
         try {
             wcAccess.open(true, false);
             SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(wcAccess.getTargetName());
             if (entry == null) {
                 SVNErrorManager.error("svn: '" + path + "' is not under version control");
             }
             kind = entry.getKind();
             File file = wcAccess.getAnchor().getFile(wcAccess.getTargetName(), false);
             if (entry.isDirectory()) {
                 if (!entry.isScheduledForAddition() && !file.isDirectory()) {
                     svnEvent(SVNEventFactory.createNotRevertedEvent(wcAccess, wcAccess.getAnchor(), entry), ISVNEventListener.UNKNOWN);
                     return;
                 }
             }
 
             SVNEvent event = SVNEventFactory.createRevertedEvent(wcAccess, wcAccess.getAnchor(), entry);
             if (entry.isScheduledForAddition()) {
                 boolean deleted = entry.isDeleted();
                 if (entry.isFile()) {
                     wcAccess.getAnchor().destroy(entry.getName(), false);
                 } else if (entry.isDirectory()) {
                     if ("".equals(wcAccess.getTargetName())) {
                         SVNErrorManager.error("svn: Cannot revert addition of the root directory; please try again from the parent directory");
                     }
                     if (!file.exists()) {
                         wcAccess.getAnchor().getEntries().deleteEntry(entry.getName());
                     } else {
                         wcAccess.getAnchor().destroy(entry.getName(), false);
                     }
                 }
                 reverted = true;
                 if (deleted && !"".equals(wcAccess.getTargetName())) {
                     // we are not in the root.
                     SVNEntry replacement = wcAccess.getAnchor().getEntries().addEntry(entry.getName());
                     replacement.setDeleted(true);
                     replacement.setKind(kind);
                 }
             } else if (entry.isScheduledForReplacement() || entry.isScheduledForDeletion()) {
                 replaced = entry.isScheduledForReplacement();
                 if (entry.isDirectory()) {
                     reverted |= wcAccess.getTarget().revert("");
                 } else {
                     reverted |= wcAccess.getAnchor().revert(entry.getName());
                 }
                 reverted = true;
             } else {
                 if (entry.isDirectory()) {
                     reverted |= wcAccess.getTarget().revert("");
                 } else {
                     reverted |= wcAccess.getAnchor().revert(entry.getName());
                 }
             }
             if (reverted) {
                 if (kind == SVNNodeKind.DIR && replaced) {
                     recursive = true;
                 }
                 if (!"".equals(wcAccess.getTargetName())) {
                     entry.unschedule();
                     entry.setConflictNew(null);
                     entry.setConflictOld(null);
                     entry.setConflictWorking(null);
                     entry.setPropRejectFile(null);
                 }
                 wcAccess.getAnchor().getEntries().save(false);
                 if (kind == SVNNodeKind.DIR) {
                 	DebugLog.log("reverted, unscheduling target root entry: " + wcAccess.getTarget().getRoot());
                     SVNEntry inner = wcAccess.getTarget().getEntries().getEntry("");                    
                     if (inner != null) {
                     	// may be null if it was removed from wc.
 	                    inner.unschedule();
 	                    inner.setConflictNew(null);
 	                    inner.setConflictOld(null);
 	                    inner.setConflictWorking(null);
 	                    inner.setPropRejectFile(null);
                     }
                 }
                 wcAccess.getTarget().getEntries().save(false);
             }
             if (kind == SVNNodeKind.DIR && recursive) {
                 // iterate over targets and revert
                 for (Iterator ents = wcAccess.getTarget().getEntries().entries(); ents.hasNext();) {
                     SVNEntry childEntry = (SVNEntry) ents.next();
                     if ("".equals(childEntry.getName())) {
                         continue;
                     }
                     recursiveFiles.add(wcAccess.getTarget().getFile(childEntry.getName(), false));
                 }
             }
             if (reverted) {
                 // fire reverted event.
                 svnEvent(event, ISVNEventListener.UNKNOWN);
             }
         } finally {
             wcAccess.close(true, false);
         }
         // recurse
         if (kind == SVNNodeKind.DIR && recursive) {
             // iterate over targets and revert
             for (Iterator files = recursiveFiles.iterator(); files.hasNext();) {
                 File file = (File) files.next();
                 doRevert(file, recursive);
             }
         }
     }
 
     public void doResolve(File path, boolean recursive) {
     }
 
     public void doLock(File[] paths, boolean stealLock, String lockMessage) throws SVNException {
         Map entriesMap = new HashMap();
         for (int i = 0; i < paths.length; i++) {
             SVNWCAccess wcAccess = createWCAccess(paths[i]);
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(wcAccess.getTargetName());
                 if (entry == null || entry.isHidden()) {
                     SVNErrorManager.error("svn: '" + entry.getName() + "' is not under version control");
                 }
                 if (entry.getURL() == null) {
                     SVNErrorManager.error("svn: '" + entry.getName() + "' has no URL");
                 }
                 SVNRevision revision = stealLock ? SVNRevision.UNDEFINED : SVNRevision.create(entry.getRevision());
                 entriesMap.put(entry.getURL(), new LockInfo(paths[i], revision));
                 wcAccess.getAnchor().getEntries().close();
             } finally {
                 wcAccess.close(true, false);
             }
         }
         for (Iterator urls = entriesMap.keySet().iterator(); urls.hasNext();) {
             String url = (String) urls.next();
             LockInfo info = (LockInfo) entriesMap.get(url);
             SVNWCAccess wcAccess = createWCAccess(info.myFile);
 
             SVNRepository repos = createRepository(url);
             SVNLock lock = null;
             try {
                 lock = repos.setLock("", lockMessage, stealLock, info.myRevision.getNumber());
             } catch (SVNException error) {
                 svnEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess.getTargetName(), SVNEventAction.LOCK_FAILED, null,
                         error.getMessage()),
                         ISVNEventListener.UNKNOWN);
                 continue;
             }
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(wcAccess.getTargetName());
                 entry.setLockToken(lock.getID());
                 entry.setLockComment(lock.getComment());
                 entry.setLockOwner(lock.getOwner());
                 entry.setLockCreationDate(TimeUtil.formatDate(lock.getCreationDate()));
                 if (wcAccess.getAnchor().getProperties(entry.getName(), false).getPropertyValue(SVNProperty.NEEDS_LOCK) != null) {
                     try {
                         SVNFileUtil.setReadonly(wcAccess.getAnchor().getFile(entry.getName(), false), false);
                     } catch (IOException e) {
                     }
                 }
                 wcAccess.getAnchor().getEntries().save(true);
                 wcAccess.getAnchor().getEntries().close();
                 svnEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess.getTargetName(), SVNEventAction.LOCKED, lock, null),
                         ISVNEventListener.UNKNOWN);
             } catch (SVNException e) {
                 svnEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess.getTargetName(), SVNEventAction.LOCK_FAILED, lock, e.getMessage()),
                         ISVNEventListener.UNKNOWN);
             } finally {
                 wcAccess.close(true, false);
             }
         }
     }
 
     public void doLock(String[] urls, boolean stealLock, String lockMessage) throws SVNException  {
         for (int i = 0; i < urls.length; i++) {           
             String url = validateURL(urls[i]);
             SVNRepository repos = createRepository(url);
             SVNLock lock = null;
             try {
                 lock = repos.setLock("", lockMessage, stealLock, -1);
             } catch (SVNException error) {
                 svnEvent(SVNEventFactory.createLockEvent(url, SVNEventAction.LOCK_FAILED, lock, null),
                         ISVNEventListener.UNKNOWN);
                 continue;
             }
             svnEvent(SVNEventFactory.createLockEvent(url, SVNEventAction.LOCKED, lock, null),
                     ISVNEventListener.UNKNOWN);
         }
     }
     
     public void doUnlock(File[] paths, boolean breakLock) throws SVNException {
         Map entriesMap = new HashMap();
         for (int i = 0; i < paths.length; i++) {
             SVNWCAccess wcAccess = createWCAccess(paths[i]);
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(wcAccess.getTargetName());
                 if (entry == null || entry.isHidden()) {
                     SVNErrorManager.error("svn: '" + entry.getName() + "' is not under version control");
                 }
                 if (entry.getURL() == null) {
                     SVNErrorManager.error("svn: '" + entry.getName() + "' has no URL");
                 }
                 String lockToken = entry.getLockToken();
                 if (!breakLock && lockToken == null) {
                     SVNErrorManager.error("svn: '" + entry.getName() + "' is not locked in this working copy");
                 }
                 entriesMap.put(entry.getURL(), new LockInfo(paths[i], lockToken));
                 wcAccess.getAnchor().getEntries().close();
             } finally {
                 wcAccess.close(true, false);
             }
         }
         for (Iterator urls = entriesMap.keySet().iterator(); urls.hasNext();) {
             String url = (String) urls.next();
             LockInfo info = (LockInfo) entriesMap.get(url);
             SVNWCAccess wcAccess = createWCAccess(info.myFile);
 
             SVNRepository repos = createRepository(url);
             SVNLock lock = null;
             boolean removeLock = false;
             try {
                 repos.removeLock("", info.myToken, breakLock);
                 removeLock = true;
             } catch (SVNException error) {
                 // remove lock if error is owner_mismatch.
                 removeLock = true;
             }
             if (!removeLock) {
                 svnEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess.getTargetName(), SVNEventAction.UNLOCK_FAILED, null, 
                         "unlock failed"), ISVNEventListener.UNKNOWN);
                 continue;
             }
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(wcAccess.getTargetName());
                 entry.setLockToken(null);
                 entry.setLockComment(null);
                 entry.setLockOwner(null);
                 entry.setLockCreationDate(null);
                 wcAccess.getAnchor().getEntries().save(true);
                 wcAccess.getAnchor().getEntries().close();
                 if (wcAccess.getAnchor().getProperties(entry.getName(), false).getPropertyValue(SVNProperty.NEEDS_LOCK) != null) {
                     try {
                         SVNFileUtil.setReadonly(wcAccess.getAnchor().getFile(entry.getName(), false), true);
                     } catch (IOException e) {
                     }
                 }
                 svnEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess.getTargetName(), SVNEventAction.UNLOCKED, lock, null),
                         ISVNEventListener.UNKNOWN);
             } catch (SVNException e) {
                 svnEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess.getTargetName(), SVNEventAction.UNLOCK_FAILED, lock, 
                         e.getMessage()),
                         ISVNEventListener.UNKNOWN);
             } finally {
                 wcAccess.close(true, false);
             }
         }
     }
 
     public void doUnlock(String[] urls, boolean breakLock) throws SVNException  {
         Map lockTokens = new HashMap();
         if (!breakLock) {
             for (int i = 0; i < urls.length; i++) {
                 String url = validateURL(urls[i]);
                 // get lock token for url
                 SVNRepository repos = createRepository(url);
                 SVNLock lock = repos.getLock("");
                 if (lock == null) {
                     SVNErrorManager.error("svn: '" + url+ "' is not locked");
                 }                
                 lockTokens.put(url, lock.getID());
             }
         }
         for (int i = 0; i < urls.length; i++) {
             String url = validateURL(urls[i]);
             // get lock token for url
             SVNRepository repos = createRepository(url);
             String id = (String) lockTokens.get(url);
             try {
                 repos.removeLock("", id, breakLock);
             } catch (SVNException e) {
                 svnEvent(SVNEventFactory.createLockEvent(url, SVNEventAction.UNLOCK_FAILED, null, null),
                         ISVNEventListener.UNKNOWN);
                 continue;
             }
             svnEvent(SVNEventFactory.createLockEvent(url, SVNEventAction.UNLOCKED, null, null),
                     ISVNEventListener.UNKNOWN);
         }
     }
     
     public void doInfo(File path, SVNRevision revision, boolean recursive, ISVNInfoHandler handler) throws SVNException {
         if (handler == null) {
             return;
         }
         if (!(revision == null || !revision.isValid() || revision == SVNRevision.WORKING)) {
             SVNWCAccess wcAccess = createWCAccess(path);
             SVNRevision wcRevision = null;
             String url = null;
             try {
                 wcAccess.open(true, false);
                 url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
                 if (url == null) {
                     SVNErrorManager.error("svn: '" + path.getAbsolutePath() + "' has no URL");
                 }
                 wcRevision = SVNRevision.parse(wcAccess.getTargetEntryProperty(SVNProperty.REVISION));
             } finally {
                 wcAccess.close(true, false);
             }
             doInfo(url, wcRevision, revision, recursive, handler);
             return;
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, recursive);
             collectInfo(wcAccess.getAnchor(), wcAccess.getTargetName(), recursive, handler);
         } finally {
             wcAccess.close(true, recursive);
         }
     }
     
     public void doInfo(String url, SVNRevision pegRevision, SVNRevision revision, boolean recursive, ISVNInfoHandler handler) throws SVNException {
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         url = getURL(url, pegRevision, revision);
         long revNum = getRevisionNumber(url, revision);
 
         SVNRepository repos = createRepository(url);
         SVNDirEntry rootEntry = repos.info("", revNum);
         if (rootEntry == null || rootEntry.getKind() == SVNNodeKind.NONE) {
             SVNErrorManager.error("'" + url + "' non-existent in revision " + revNum);
         }
         String reposRoot = repos.getRepositoryRoot();
         String reposUUID = repos.getRepositoryUUID();
         // 1. get locks for this dir and below.
         SVNLock[] locks = null;
         try {
             locks = repos.getLocks("");
         } catch (SVNException e) {
             // may be not supported.
             locks = new SVNLock[0];
         }
         locks = locks == null ? new SVNLock[0] : locks;
         Map locksMap = new HashMap();
         for (int i = 0; i < locks.length; i++) {
             SVNLock lock = locks[i];
             locksMap.put(lock.getPath(), lock);
         }
         String fullPath = SVNRepositoryLocation.parseURL(PathUtil.decode(url)).getPath();
         String rootPath = fullPath.substring(reposRoot.length());
         if (!rootPath.startsWith("/")) {
             rootPath = "/" + rootPath;
         }
         reposRoot = PathUtil.append(url.substring(0, url.length() - fullPath.length()), reposRoot);
         collectInfo(repos, rootEntry, SVNRevision.create(revNum), rootPath, reposRoot, reposUUID, url, locksMap, recursive, handler);
     }
 
     public SVNInfo doInfo(File path, SVNRevision revision) throws SVNException {
         final SVNInfo[] result = new SVNInfo[1];
         doInfo(path, revision, false, new ISVNInfoHandler() {
             public void handleInfo(SVNInfo info) {
                 if (result[0] == null) {
                     result[0] = info;
                 }
             }
         });
         return result[0];
     }
 
     public SVNInfo doInfo(String url, SVNRevision pegRevision, SVNRevision revision) throws SVNException {
         final SVNInfo[] result = new SVNInfo[1];
         doInfo(url, pegRevision, revision, false, new ISVNInfoHandler() {
             public void handleInfo(SVNInfo info) {
                 if (result[0] == null) {
                     result[0] = info;
                 }
             }
         });
         return result[0];
     }
     
     private static void collectInfo(SVNDirectory dir, String name, boolean recursive, ISVNInfoHandler handler) throws SVNException {
         SVNEntries entries = dir.getEntries();
         SVNEntry entry = entries.getEntry(name);
         try {
             if (entry != null && !entry.isHidden()) {
                 if (entry.isFile()) {
                     // child file
                     File file = dir.getFile(name, false);
                     handler.handleInfo(SVNInfo.createInfo(file, entry));
                     return;
                 } else if (entry.isDirectory() && !"".equals(name)) {
                     // child dir
                 	dir = dir.getChildDirectory(name);
                 	if (dir != null) {
                 		collectInfo(dir, "", recursive, handler);
                 	}
                     return;
                 } else if ("".equals(name)) {
                     // report root.
                     handler.handleInfo(SVNInfo.createInfo(dir.getRoot(), entry));
                 }
               
                 if (recursive) {
                     for (Iterator ents = entries.entries(); ents.hasNext();) {
                         SVNEntry childEntry = (SVNEntry) ents.next();
                         if ("".equals(childEntry.getName())) {
                             continue;
                         }
                         if (entry.isDirectory()) {
                             SVNDirectory childDir = dir.getChildDirectory(childEntry.getName());
                             if (childDir != null) {
                                 collectInfo(childDir, "", recursive, handler);
                             }
                         } else if (entry.isFile()) {
                             handler.handleInfo(SVNInfo.createInfo(dir.getFile(childEntry.getName(), false), entry));
                         }
                     }
                 }
             } 
         } finally {
             entries.close();
         }
         
     }
     
     private static void collectInfo(SVNRepository repos, SVNDirEntry entry, SVNRevision rev, String path, String root, String uuid, String url, Map locks, boolean recursive,
             ISVNInfoHandler handler) throws SVNException {
         String rootPath = repos.getLocation().getPath();
         rootPath = PathUtil.decode(rootPath);
         String displayPath = PathUtil.append(repos.getRepositoryRoot(), path).substring(rootPath.length());
         if ("".equals(displayPath) || "/".equals(displayPath)) {
             displayPath = path;
         }
         displayPath = PathUtil.removeLeadingSlash(displayPath);
         handler.handleInfo(SVNInfo.createInfo(displayPath, root, uuid, url, rev, entry, (SVNLock) locks.get(path)));
         if (entry.getKind() == SVNNodeKind.DIR && recursive) {
             Collection children = repos.getDir(path, rev.getNumber(), null, new ArrayList());
             for (Iterator ents = children.iterator(); ents.hasNext();) {
                 SVNDirEntry child = (SVNDirEntry) ents.next();
                 String childURL = PathUtil.append(url, PathUtil.encode(child.getName()));
                 collectInfo(repos, child, rev, PathUtil.append(path, child.getName()), root, uuid, childURL, locks,
                         recursive,
                         handler);                
             }
         }
     }
     
     // add, del@path, revert, resolved, *lock, *unlock, *info, +cleanup -> "wc" client
     
     // copy, move -> "copy" client
     // log, cat, blame, ls -> "repos" client
     // commit, mkdir, import, del@url -> "commit" client
     // status -> "status" client
     // export, update, switch -> "update" client
     // diff, merge -> "diff" client
     
     // (?) ps,pg,pe,pl -> "prop" client
 
     private void addDirectory(SVNWCAccess wcAccess, SVNDirectory dir, String name, boolean force) throws SVNException {
 		DebugLog.log("ading file " + name + " into " + dir.getRoot());
 		dir.add(name, force);
 			
 		SVNDirectory childDir = dir.getChildDirectory(name);
     	File file = dir.getFile(name, false);
     	File[] children = file.listFiles();
     	for (int i = 0; children != null && i < children.length; i++) {
     		File childFile = children[i];
     		if (getOptions().isIgnored(childFile.getName())) {
     			continue;
     		}
     		if (".svn".equals(childFile.getName())) {
     			continue;
     		}
     		DebugLog.log("processing file " + childFile + " in " + file);
     		if (childFile.isFile() || SVNFileUtil.isSymlink(childFile)) {
     			SVNEntry entry = childDir.getEntries().getEntry(childFile.getName());
     			DebugLog.log("existing entry: " + entry);
     			if (force && entry != null && !entry.isScheduledForDeletion() && !entry.isDeleted()) {
         			DebugLog.log("this entry will not be added: " + entry.getName());
     				continue;
     			}
     			addSingleFile(wcAccess, childDir, childFile.getName());
     		} else if (childFile.isDirectory()) {
     			DebugLog.log("recursing into " + childFile.getName());
     			addDirectory(wcAccess, childDir, childFile.getName(), force);
     		}
 		}
     }
 
 	private void addSingleFile(SVNWCAccess wcAccess, SVNDirectory dir, String name) throws SVNException {
 		File file = dir.getFile(name, false);
 		ISVNEventListener oldDisptcher = wcAccess.getEventDispatcher();
 		SVNEntry entry = null;
 		try {
 			entry = dir.add(name, false);
 		} finally {
 			wcAccess.setEventDispatcher(oldDisptcher);
 		}
 		String mimeType = null;
 		SVNProperties properties = dir.getProperties(name, false);
 		if (SVNFileUtil.isSymlink(file)) {
 			properties.setPropertyValue(SVNProperty.SPECIAL, "*");
 		} else {
 			Map props = new HashMap();
 			boolean executable = false;
 			props = getOptions().getAutoProperties(name, props);
 			mimeType = (String) props.get(SVNProperty.MIME_TYPE);
 			if (mimeType == null) {
 				mimeType = SVNFileUtil.detectMimeType(file);
 				if (mimeType != null) {
 					props.put(SVNProperty.MIME_TYPE, mimeType);
 				}
 			}
 			if (!props.containsKey(SVNProperty.EXECUTABLE)) {
 				executable = SVNFileUtil.isExecutable(file);
 				if (executable) {
 					props.put(SVNProperty.EXECUTABLE, "*");
 				}
 			}
 			for (Iterator names = props.keySet().iterator(); names.hasNext();) {
 				String propName = (String) names.next();
 				String propValue = (String) props.get(propName);
 				properties.setPropertyValue(propName, propValue);
 			}
 		}
 		SVNEvent event = SVNEventFactory.createAddedEvent(wcAccess, dir, entry);
 		wcAccess.svnEvent(event, ISVNEventListener.UNKNOWN);
 	}
 
     private void doGetRemoteProperty(String url, String path, SVNRepository repos, String propName, SVNRevision rev, boolean recursive, ISVNPropertyHandler handler) throws SVNException {
         long revNumber = getRevisionNumber(url, rev);
         SVNNodeKind kind = repos.checkPath(path, revNumber);
         Map props = new HashMap();
         if (kind == SVNNodeKind.DIR) {
             Collection children = repos.getDir(path, revNumber, props, recursive ? new ArrayList() : null);
             if (propName != null) {
                 String value = (String) props.get(propName);
                 if (value != null) {
                     handler.handleProperty(url, new SVNPropertyData(propName, value));
                 }
             } else {
                 for (Iterator names = props.keySet().iterator(); names.hasNext();) {
                     String name = (String) names.next();
                     if (name.startsWith(SVNProperty.SVN_ENTRY_PREFIX) || name.startsWith(SVNProperty.SVN_WC_PREFIX)) {
                         continue;
                     }
                     String value = (String) props.get(name);
                     handler.handleProperty(url, new SVNPropertyData(name, value));
                 }
             }
             if (recursive) {
                 for (Iterator entries = children.iterator(); entries.hasNext();) {
                     SVNDirEntry child = (SVNDirEntry) entries.next();
                     String childURL = PathUtil.append(url, PathUtil.encode(child.getName()));
                     String childPath = "".equals(path) ? child.getName() : PathUtil.append(path, child.getName());
                     doGetRemoteProperty(childURL, childPath, repos, propName, rev, recursive, handler);
                 }
             }
         } else if (kind == SVNNodeKind.FILE) {
             repos.getFile(path, revNumber, props, null);
             if (propName != null) {
                 String value = (String) props.get(propName);
                 if (value != null) {
                     handler.handleProperty(url, new SVNPropertyData(propName, value));
                 }
             } else {
                 for (Iterator names = props.keySet().iterator(); names.hasNext();) {
                     String name = (String) names.next();
                     if (name.startsWith(SVNProperty.SVN_ENTRY_PREFIX) || name.startsWith(SVNProperty.SVN_WC_PREFIX)) {
                         continue;
                     }
                     String value = (String) props.get(name);
                     handler.handleProperty(url, new SVNPropertyData(name, value));
                 }
             }
         }
     }
 
     private void doGetLocalProperty(SVNDirectory anchor, String name, String propName, SVNRevision rev, boolean recursive, ISVNPropertyHandler handler) throws SVNException {
         SVNEntries entries = anchor.getEntries();
         if (!"".equals(name)) {
             SVNEntry entry = entries.getEntry(name);
             if (entry == null) {
                 return;
             }
             if (entry.getKind() == SVNNodeKind.DIR) {
                 SVNDirectory dir = anchor.getChildDirectory(name);
                 if (dir != null) {
                     doGetLocalProperty(dir, "", propName, rev, recursive, handler);
                 }
             } else if (entry.getKind() == SVNNodeKind.FILE) {
                 SVNProperties props = rev == SVNRevision.WORKING ? anchor.getProperties(name, false) : anchor.getBaseProperties(name, false);
                 if (propName != null) {
                     String value = props.getPropertyValue(propName);
                     if (value != null) {
                         handler.handleProperty(anchor.getFile(name, false), new SVNPropertyData(propName, value));
                     }
                 } else {
                     Map propsMap = props.asMap();
                     for (Iterator names = propsMap.keySet().iterator(); names.hasNext();) {
                         String pName = (String) names.next();
                         String value = (String) propsMap.get(pName);
                         handler.handleProperty(anchor.getFile(name, false), new SVNPropertyData(pName, value));
                     }
                 }
             }
             entries.close();
             return;
         }
         SVNProperties props = rev == SVNRevision.WORKING ? anchor.getProperties(name, false) : anchor.getBaseProperties(name, false);
         if (propName != null) {
             String value = props.getPropertyValue(propName);
             if (value != null) {
                 handler.handleProperty(anchor.getFile(name, false), new SVNPropertyData(propName, value));
             }
         } else {
             Map propsMap = props.asMap();
             for (Iterator names = propsMap.keySet().iterator(); names.hasNext();) {
                 String pName = (String) names.next();
                 String value = (String) propsMap.get(pName);
                 handler.handleProperty(anchor.getFile(name, false), new SVNPropertyData(pName, value));
             }
         }
         if (!recursive) {
             return;
         }
         for (Iterator ents = entries.entries(); ents.hasNext();) {
             SVNEntry entry = (SVNEntry) ents.next();
             if ("".equals(entry.getName())) {
                 continue;
             }
             doGetLocalProperty(anchor, entry.getName(), propName, rev, recursive, handler);
         }
     }
 
     private void doSetLocalProperty(SVNDirectory anchor, String name, String propName, String propValue, boolean force, boolean recursive, ISVNPropertyHandler handler) throws SVNException {
         SVNEntries entries = anchor.getEntries();
         DebugLog.log("anchor: " + anchor.getRoot());
         if (!"".equals(name)) {
             SVNEntry entry = entries.getEntry(name);
             DebugLog.log("entry in anchor: " + entry);
             if (entry == null || (recursive && entry.isDeleted())) {
                 return;
             }
             if (entry.getKind() == SVNNodeKind.DIR) {
                 SVNDirectory dir = anchor.getChildDirectory(name);
                 if (dir != null) {
                     doSetLocalProperty(dir, "", propName, propValue, force, recursive, handler);
                 }
             } else if (entry.getKind() == SVNNodeKind.FILE) {
                 if (SVNProperty.IGNORE.equals(propName) || SVNProperty.EXTERNALS.equals(propName)) {
                     if (!recursive) {
                         SVNErrorManager.error("svn: setting '" + propName + "' property is not supported for files");
                     }
                     return;
                 }
                 SVNProperties props = anchor.getProperties(name, false);
                 File wcFile = anchor.getFile(name, false);
                 if (!force && SVNProperty.EOL_STYLE.equals(propName) && propValue != null) {
                     if (SVNWCUtil.isBinaryMimetype(props.getPropertyValue(SVNProperty.MIME_TYPE))) {
                         if (!recursive) {
                             SVNErrorManager.error("svn: File '" + wcFile + "' has binary mime type property");
                         }
                         return;
                     }
                     if (!SVNTranslator.checkNewLines(wcFile)) {
                         SVNErrorManager.error("svn: File '" + wcFile + "' has inconsistent newlines");
                     }                    
                 }
                 props.setPropertyValue(propName, propValue);
 
                 if (SVNProperty.EOL_STYLE.equals(propName) || SVNProperty.KEYWORDS.equals(propName)) {
                     entry.setTextTime(null);
                     entries.save(false);
                 } else if (SVNProperty.NEEDS_LOCK.equals(propName) && propValue == null) {
                     try {
                         SVNFileUtil.setReadonly(wcFile, false);
                     } catch (IOException e) {
                     }
                 }
                 handler.handleProperty(anchor.getFile(name, false), new SVNPropertyData(propName, propValue));
             }
             entries.close();
             return;
         }
         DebugLog.log("setting property (" + propName + ") on dir " + anchor.getRoot());
         SVNProperties props = anchor.getProperties(name, false);
         if (SVNProperty.KEYWORDS.equals(propName) || SVNProperty.EOL_STYLE.equals(propName) ||
                 SVNProperty.REVISION.equals(propName) || SVNProperty.MIME_TYPE.equals(propName)) {
             if (!recursive) {
                 SVNErrorManager.error("svn: setting '" + propName + "' property is not supported for directories");
             }
         } else {
             props.setPropertyValue(propName, propValue);
             handler.handleProperty(anchor.getFile(name, false), new SVNPropertyData(propName, propValue));
         }
         if (!recursive) {
             return;
         }
         for (Iterator ents = entries.entries(); ents.hasNext();) {
             SVNEntry entry = (SVNEntry) ents.next();
             if ("".equals(entry.getName())) {
                 continue;
             }
             doSetLocalProperty(anchor, entry.getName(), propName, propValue, force, recursive, handler);
         }
     }
     
     private static String validatePropertyName(String name) throws SVNException {
         if (name == null || name.trim().length() == 0) {
             SVNErrorManager.error("svn: Bad property name: '" + name + "'");
         }
         name = name.trim();
         if (!(Character.isLetter(name.charAt(0)) || name.charAt(0) == ':' || name.charAt(0) == '_')) {
             SVNErrorManager.error("svn: Bad property name: '" + name + "'");
         }
         for (int i = 1; i < name.length(); i++) {
             if (!(Character.isLetterOrDigit(name.charAt(i)) || name.charAt(i) == '-' || name.charAt(i) == '.' ||
                     name.charAt(i) == ':' || name.charAt(i) == '_')) {
                 SVNErrorManager.error("svn: Bad property name: '" + name + "'");
             }
         }
         return name;
     }
     
     private static String validatePropertyValue(String name, String value, boolean force) throws SVNException {
         if (value == null) {
             return value;
         }
         if (!force && SVNProperty.EOL_STYLE.equals(name)) {
             value = value.trim();
         } else if (!force && SVNProperty.MIME_TYPE.equals(name)) {
             value = value.trim();
         } else if (SVNProperty.IGNORE.equals(name) || SVNProperty.EXTERNALS.equals(name)) {
             if (!value.endsWith("\n")) {
                 value += "\n";
             }
             if (SVNProperty.EXTERNALS.equals(name)) {
                 SVNExternalInfo[] externalInfos = SVNWCAccess.parseExternals("", value);
                 for (int i = 0; i < externalInfos.length; i++) {
                     String path = externalInfos[i].getPath();
                     if (path.indexOf(".") >=0 || path.indexOf("..") >= 0 ||
                             path.startsWith("/")) {
                         SVNErrorManager.error("svn: Invalid external definition: " + value);
                     }
                             
                 }
             }
         } else if (SVNProperty.KEYWORDS.equals(name)) {
             value = value.trim();
         } else if (SVNProperty.EXECUTABLE.equals(name) || SVNProperty.SPECIAL.equals(name)) {
             value = "*";
         }
         return value;
     }
     
     private static final Collection REVISION_PROPS = new HashSet();
     static {
         REVISION_PROPS.add("svn:author");
         REVISION_PROPS.add("svn:log");
         REVISION_PROPS.add("svn:date");
         REVISION_PROPS.add("svn:autoversioned");
         REVISION_PROPS.add("svn:original-date");
     }
 
 
     private static class LockInfo {
         
         public LockInfo(File file, SVNRevision rev) {
             myFile = file;
             myRevision = rev;
         }
 
         public LockInfo(File file, String token) {
             myFile = file;
             myToken = token;
         }
         
         private File myFile;
         private SVNRevision myRevision;
         private String myToken;
     }
 }
