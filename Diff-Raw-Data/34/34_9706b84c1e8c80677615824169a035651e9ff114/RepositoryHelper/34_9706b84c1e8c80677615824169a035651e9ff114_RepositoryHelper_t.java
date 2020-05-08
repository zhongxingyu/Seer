 
 package com.randomhumans.svnindex.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.tmatesoft.svn.core.SVNDirEntry;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.io.SVNFileRevision;
 import org.tmatesoft.svn.core.io.SVNRepository;
 
 public class RepositoryHelper
 {
     static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(RepositoryHelper.class);
 
     public static long getLatestRevision() throws SVNException
     {
         return new RepositoryTemplate<Long>().execute(Configuration.getConfig().getPathToRepository(), new RepositoryAction<Long>()
         {
             public Long execute(final SVNRepository repo) throws SVNException
             {
                 return repo.getLatestRevision();
             }
         });
     }
 
     public static SVNNodeKind checkPath(final String path) throws SVNException
     {
         return new RepositoryTemplate<SVNNodeKind>().execute(Configuration.getConfig().getPathToRepository(), new RepositoryAction<SVNNodeKind>()
         {
             public SVNNodeKind execute(final SVNRepository repo) throws SVNException
             {
                 return repo.checkPath(path, -1);
             }
         });
     }
 
     public static Collection<SVNDirEntry> dir(final String url, final long revision) throws SVNException
     {        
         return new RepositoryTemplate<Collection<SVNDirEntry>>()
         .execute(Configuration.getConfig().getPathToRepository(), new RepositoryAction<Collection<SVNDirEntry>>()
         {
             @SuppressWarnings("unchecked")
             public Collection<SVNDirEntry> execute(final SVNRepository repo) throws SVNException 
             {
                 return repo.getDir(url, revision, null, (Collection<SVNDirEntry>)null);
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     public static SVNLogEntry getLogEntry(final long revisionNumber) throws SVNException
     {
         return new RepositoryTemplate<SVNLogEntry>().execute(Configuration.getConfig().getPathToRepository(),new RepositoryAction<SVNLogEntry>()
         {
             public SVNLogEntry execute(final SVNRepository repo) throws SVNException
             {
                 ArrayList<SVNLogEntry> logs;
                 logs = new ArrayList<SVNLogEntry>(repo.log(new String[] {""}, null, revisionNumber, revisionNumber, true, true));
                 return logs.get(0);
             }
         });
     }
 
     public static synchronized SVNFileRevision getFileRevision(final String path, final long revision) 
     {
         
         try
         {
             return new RepositoryTemplate<SVNFileRevision>().execute(Configuration.getConfig().getPathToRepository(), new RepositoryAction<SVNFileRevision>()
                 {
                     public SVNFileRevision execute(final SVNRepository repo) throws SVNException 
                     {
                         log.debug(checkPath(path));
                        Collection<?> revisions = repo.getFileRevisions(path , null, revision,revision);
                        SVNFileRevision[] fileRevisionInfos = (SVNFileRevision[]) revisions.toArray(new SVNFileRevision[0]);
                         SVNFileRevision s = fileRevisionInfos[0];
                         return s;
                     }
                 });
         }
         catch (SVNException e)
         {
             log.warn("Problem retrieving filerevision for " + path, e);
             return null;
         }        
     }
     
     public static SVNFileRevision getFileRevision(final SVNDirEntry entry, final String path) throws SVNException 
     {
         return getFileRevision(path +"/" + entry.getName(), entry.getRevision());
     }
 
 }
