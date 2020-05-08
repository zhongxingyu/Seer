 package org.makumba.parade.model.managers;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.FileCVS;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowCVS;
 import org.makumba.parade.model.interfaces.CacheRefresher;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 
 public class CVSManager implements CacheRefresher, RowRefresher, ParadeManager {
 
     public static Logger logger = Logger.getLogger(CVSManager.class.getName());
 
     public static Integer IGNORED = new Integer(101);
 
     public static Integer UNKNOWN = new Integer(-1);
 
     public static Integer UP_TO_DATE = new Integer(100);
 
     public static Integer LOCALLY_MODIFIED = new Integer(1);
 
     public static Integer NEEDS_CHECKOUT = new Integer(2);
 
     public static Integer NEEDS_UPDATE = new Integer(3);
 
     public static Integer ADDED = new Integer(4);
 
     public static Integer DELETED = new Integer(5);
 
     public static Integer CONFLICT = new Integer(6);
 
     public static DateFormat cvsDateFormat;
 
     static {
         cvsDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.UK);
         cvsDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
     }
 
     public void directoryRefresh(Row row, String path, boolean local) {
 
         // for the root paths
         java.io.File currDir = new java.io.File(path);
 
         // we will go through the CVS entries of the real directory
         if (currDir.isDirectory() && !(currDir.getName() == null)) {
             // getting the File object mapped to this dir
             File currFile = (File) row.getFiles().get(currDir.getAbsolutePath());
 
             // you never know
             if (!(currFile == null) && currFile.getIsDir())
                 readFiles(row, currFile);
         }
     }
     
     public void fileRefresh(Row row, String absolutePath) {
         java.io.File f = new java.io.File(absolutePath);
         File currFile = (File) row.getFiles().get(f.getParent());
         if(!(currFile == null) && currFile.getIsDir()) {
             readCVSEntries(row, currFile, f.getName());
         }
     }
 
     public void rowRefresh(Row row) {
         logger.debug("Refreshing row information for row "+row.getRowname());
 
         RowCVS cvsdata = new RowCVS();
         cvsdata.setDataType("cvs");
 
         readUserAndModule(row, cvsdata);
 
         row.addManagerData(cvsdata);
     }
 
     private void readUserAndModule(Row row, RowCVS data) {
 
         String path = (String) row.getRowpath();
         String s = null;
         try {
             s = new BufferedReader(new FileReader(path + java.io.File.separator + "CVS" + java.io.File.separator
                     + "Root")).readLine();
         } catch (FileNotFoundException e) {
             return;
         } catch (IOException ioe) {
             throw new RuntimeException(ioe.getMessage());
         }
 
         if (s.startsWith(":pserver")) {
             s = s.substring(":pserver:".length());
             data.setUser(s.substring(0, s.indexOf("@")));
         } else if (s.startsWith(":extssh:")) {
             s = s.substring(":extssh:".length());
             data.setUser(s.substring(0, s.indexOf("@")));
         } else
             data.setUser("non :pserver");
 
         try {
             s = new BufferedReader(new FileReader(path + java.io.File.separator + "CVS" + java.io.File.separator
                     + "Repository")).readLine();
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage());
         }
         data.setModule(s.substring(s.lastIndexOf('/') + 1));
 
         try {
             s = "TMAIN";
             s = new BufferedReader(new FileReader(path + java.io.File.separator + "CVS" + java.io.File.separator
                     + "Tag")).readLine();
         } catch (FileNotFoundException e) {
         } catch (IOException ioe) {
             throw new RuntimeException(ioe.getMessage());
         }
         data.setBranch(s.substring(1));
 
     }
 
     private void readFiles(Row r, File f) {
 
         readCVSEntries(r, f, null);
         readCVSIgnore(r, f);
         // readCVSCheckUpdate(paradeRow, data, pc);
     }
 
     /* Reads Entries file and extracts information */
     private void readCVSEntries(Row r, File file, String entry) {
         java.io.File f = new java.io.File((file.getPath() + "/" + "CVS/Entries").replace('/',
                 java.io.File.separatorChar));
         if (!f.exists())
             return;
         
         Set<String> cvsFiles = new HashSet<String>();
 
         try {
             BufferedReader br = new BufferedReader(new FileReader(f));
             String line = null;
             while ((line = br.readLine()) != null) {
 
                 // if the Entry is a file
                 if (line.startsWith("/")) {
                     int n = line.indexOf('/', 1);
                     if (n == -1)
                         continue;
                     String name = line.substring(1, n);
                     
                     if(entry != null && !name.equals(entry))
                         continue;
                     
                     // logger.warn("Looking for CVS file: "+name);
 
                     String absoluteFilePath = file.getPath() + java.io.File.separator + name;
                     java.io.File currFile = new java.io.File(absoluteFilePath);
 
                     
                     // we add this file entry to the other entries, for further checking agains the cache
                     cvsFiles.add(absoluteFilePath);
                     
                     // checking if the file we are looking for is mapped
                     File cvsfile = (File) r.getFiles().get(absoluteFilePath);
                     
                     boolean missing = false;
                     boolean fileOnDisk = currFile.exists();
                     if(cvsfile == null && !fileOnDisk) {
                         missing = true;
                     } else if(cvsfile != null && !fileOnDisk && cvsfile.getOnDisk()) {
                         //this ain't a virtual file but it's still there
                         //that's not ok, we have some zombie file info in cache
                         r.getFiles().remove(absoluteFilePath);
                         missing = true;
                     } else if(cvsfile == null && fileOnDisk) {
                         //the bloody filemanager didn't do his job. we ask it to do it again
                         FileManager fileMgr = new FileManager();
                         fileMgr.cacheFile(r, currFile, true);
                         missing = false;
                     }
                         
                     if (missing) {
                         cvsfile = FileManager.setVirtualFileData(r, file, name, false);
                         r.getFiles().put(absoluteFilePath, cvsfile);
                     }
                     FileCVS cvsdata = (FileCVS) cvsfile.getFiledata().get("cvs");
                     if (cvsdata == null) {
                         cvsdata = new FileCVS();
                         cvsdata.setDataType("cvs");
                         cvsdata.setFile(cvsfile);
                         
                         cvsfile.getFiledata().put("cvs", cvsdata);
                     }
 
                     // setting CVS status
                     cvsdata.setStatus(UNKNOWN);
                     if(missing) {
                         cvsdata.setStatus(NEEDS_CHECKOUT);
                         continue;
                     }
                         
                     
                     line = line.substring(n + 1);
                     n = line.indexOf('/');
                     if (n == -1)
                         continue;
                     String revision = line.substring(0, n);
                     cvsdata.setRevision(revision);
                     line = line.substring(n + 1);
                     n = line.indexOf('/');
                     if (n == -1)
                         continue;
 
                     // we check if the file exists on the disk
                     java.io.File fl = new java.io.File(cvsfile.getPath());
 
                     if (fl == null && !revision.startsWith("-")) {
                         cvsdata.setStatus(NEEDS_CHECKOUT);
                         continue;
                     }
 
                     String date = line.substring(0, n);
 
                     if (date.equals("Result of merge")) {
                         cvsdata.setStatus(LOCALLY_MODIFIED);
                         continue;
                     }
 
                     if (date.startsWith("Result of merge+")) {
                         cvsdata.setStatus(CONFLICT);
                         continue;
                     }
 
                     if (date.equals("dummy timestamp")) {
                         cvsdata.setStatus(revision.startsWith("-") ? DELETED : ADDED);
                         continue;
                     }
 
                     Date fd = null;
                     try {
                         cvsdata.setDate(fd = cvsDateFormat.parse(date));
                     } catch (Throwable t) {
                         logger.error("Couldn't parse date of CVS File " + file.getPath(), t);
                         continue;
                     }
 
                     long cvsModified = fd.getTime();
 
                     long l = fl.lastModified() - cvsModified;
                     if (Math.abs(l) < 1500
                     // for some stupid reason, lastModified() is different in
                             // Windows than Unix
                             // the difference seems to have to do with daylight
                             // saving
                             || Math.abs(Math.abs(l) - 3600000) < 1000) {
                         cvsdata.setStatus(UP_TO_DATE);
                         continue;
                     }
 
                     cvsdata.setStatus(l > 0 ? LOCALLY_MODIFIED : NEEDS_UPDATE);
                     continue;
 
                     // if the entry is a dir
                 } else if (line.startsWith("D/")) {
                     if(entry != null)
                         return;
                     
                     int n = line.indexOf('/', 2);
                     if (n == -1)
                         continue;
                     String name = line.substring(2, n);
                     
                     String absoluteDirectoryPath = file.getPath() + java.io.File.separator + name;
                     
                     // we add this directory entry to the other entries, for further checking agains the cache
                     cvsFiles.add(absoluteDirectoryPath);
 
                     // checking if the directory we are looking for is mapped
                     File cvsfile = (File) r.getFiles().get(absoluteDirectoryPath);
                     if (cvsfile == null) {
                         cvsfile = FileManager.setVirtualFileData(r, file, name, true);
                         r.getFiles().put(absoluteDirectoryPath, cvsfile);
                     }
 
                     FileCVS cvsdata = (FileCVS) cvsfile.getFiledata().get("cvs");
                     if (cvsdata == null) {
                         cvsdata = new FileCVS();
                         cvsdata.setDataType("cvs");
                         cvsdata.setFile(cvsfile);
                         cvsdata.setStatus(NEEDS_CHECKOUT);
                         cvsfile.getFiledata().put("cvs", cvsdata);
 
                     } else {
                         cvsdata.setStatus(UP_TO_DATE);
                         cvsdata.setRevision("(dir)");
                     }
                 }
                 
             }
             br.close();
             
             if(entry != null)
                 return;
             
             // now we check if our cache doesn't contain "zombie" cvsdata elements, i.e. if a file doesn't have
             // outdated cvs information
             List<String> cachedFiles = file.getChildrenPaths();
             Iterator<String> i = cachedFiles.iterator();
             while(i.hasNext()) {
                 String filePath = i.next();
                 if(!cvsFiles.contains(filePath)) {
                     // remove zombie entry
                     r.getFiles().get(filePath).getFiledata().remove("cvs");
                 }
             }
             
             
         } catch (Throwable t) {
             logger.error("Error while trying to set CVS information for file " + file.getName(), t);
         }
     }
 
     /* Reads .cvsignore */
     private void readCVSIgnore(Row r, File file) {
         if (!file.getIsDir())
             return;
 
         java.io.File f = new java.io.File((file.getPath() + "/" + ".cvsignore")
                 .replace('/', java.io.File.separatorChar));
 
         if (!f.exists())
             return;
 
         FileCVS cvsdata = (FileCVS) file.getFiledata().get("cvs");
 
         try {
             BufferedReader br = new BufferedReader(new FileReader(f));
             String line = null;
             while ((line = br.readLine()) != null) {
                 line = line.trim();
                 File cvsfile = (File) r.getFiles().get(file.getPath() + java.io.File.separator + line);
                 if (cvsfile == null)
                     continue;
                 cvsdata = new FileCVS();
                 cvsdata.setDataType("cvs");
                 cvsdata.setFile(cvsfile);
                 cvsdata.setStatus(IGNORED);
 
                 cvsfile.getFiledata().put("cvs", cvsdata);
             }
             br.close();
         } catch (Throwable t) {
             logger.error("Error while trying to read .cvsignore of directory " + file.getName(), t);
         }
     }
 
     public void newRow(String name, Row r, Map m) {
         // TODO Auto-generated method stub
 
     }
     
     public synchronized static void updateCvsCache(String context, String path, boolean local) {
         logger.debug("Refreshing CVS cache for path "+path+" of row "+context+((local)?" locally":" recursively"));
         CVSManager cvsMgr = new CVSManager();
         Session s = InitServlet.getSessionFactory().openSession();
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         Row r = Row.getRow(p, context);
         Transaction tx = s.beginTransaction();
         cvsMgr.directoryRefresh(r, path, local);
         tx.commit();
         s.close();
         logger.debug("Finished refreshing CVS cache for path "+path+" of row "+context+((local)?" locally":" recursively"));
     }
     
     public synchronized static void updateSimpleCvsCache(String context, String path, String filename) {
         logger.debug("Refreshing CVS cache for file "+filename+" in path "+path+" of row "+context);
         CVSManager cvsMgr = new CVSManager();
         Session s = InitServlet.getSessionFactory().openSession();
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         Row r = Row.getRow(p, context);
         Transaction tx = s.beginTransaction();
         cvsMgr.fileRefresh(r, path + java.io.File.separator + filename);
         tx.commit();
         s.close();logger.debug("Finished refreshing CVS cache for file "+filename+" in path "+path+" of row "+context);
     }
 }
