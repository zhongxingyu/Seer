 package ru.rkfg.jtagsfs;
 
 import static ru.rkfg.jtagsfs.Consts.*;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.fusejna.DirectoryFiller;
 import net.fusejna.ErrorCodes;
 import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
 import net.fusejna.StructStat.StatWrapper;
 
 import org.hibernate.NonUniqueResultException;
 import org.hibernate.Session;
 
 import ru.rkfg.jtagsfs.domain.FileRecord;
 import ru.rkfg.jtagsfs.domain.Tag;
 
 public class FSHandlerManager {
 
     public static class FSHandlerException extends Exception {
 
         /**
          * 
          */
         private static final long serialVersionUID = 7065891672128697332L;
 
         public FSHandlerException(String string) {
             super(string);
         }
 
     }
 
     public static class FSHandlerRuntimeException extends RuntimeException {
 
         /**
          * 
          */
         private static final long serialVersionUID = 3480870833432919617L;
 
         public FSHandlerRuntimeException(String message) {
             super(message);
         }
     }
 
     public static class FSHandlerFileException extends FSHandlerRuntimeException {
 
         /**
          * 
          */
         private static final long serialVersionUID = 6910554242554795364L;
 
         public FSHandlerFileException(String message) {
             super(message);
         }
 
     }
 
     private static HashMap<String, File> fileCache = new HashMap<String, File>();
 
     public static List<String> fileRecordsToNames(List<FileRecord> fileRecords) {
         List<String> result = new LinkedList<String>();
         for (FileRecord fileRecord : fileRecords) {
             result.add(fileRecord.getName());
         }
         return result;
     }
 
     public static FileRecord getFileRecordByFilepath(final Filepath filepath) {
         return HibernateUtil.exec(new HibernateCallback<FileRecord>() {
 
             public FileRecord run(Session session) {
                 return getFileRecordByFilepath(filepath, session);
             }
         });
     }
 
     public static FileRecord getFileRecordByFilepath(final Filepath filepath, Session session) {
         String name = filepath.getName();
         try {
             StringBuilder queryTags = new StringBuilder();
            queryTags.append("from FileRecord f where f.name = :name and (1=1");
             for (String tag : filepath.getPath()) {
                 if (tag.equals(CONCATTAGS)) {
                     queryTags.append(" or 1=1");
                 } else {
                     queryTags.append(" and '").append(tag).append("' in (select t.name from Tag t where t in elements(f.tags))");
                 }
             }
            queryTags.append(")");
             int index = name.indexOf(IDSEPARATOR);
             if (index > 0) {
                 try {
                     Long id = Long.valueOf(name.substring(0, index));
                     queryTags.append(" and f.id = ").append(id);
                 } catch (NumberFormatException e) {
                     throw new FSHandlerFileException("File with name " + name
                             + " not found in DB and contains invalid ID before separator.");
                 }
             }
            FileRecord fileRecord = (FileRecord) session.createQuery(queryTags.toString())
                     .setString("name", FSHandlerManager.stripFilename(name)).uniqueResult();
             if (fileRecord != null) {
                 return fileRecord;
             } else {
                 throw new FSHandlerFileException("File with name " + name + " not found in DB.");
             }
         } catch (NonUniqueResultException e) {
             throw new FSHandlerFileException("Duplicate files found with name " + name);
         }
     }
 
     public static String stripFilename(String filename) {
         int index = filename.lastIndexOf(IDSEPARATOR);
         if (index > 0) {
             return filename.substring(index + IDSEPARATOR.length());
         }
         return filename;
     }
 
     public static List<String> getTags(final String[] exclude) {
         return getTags(exclude, true);
     }
 
     public static List<String> getTags(final String[] exclude, final boolean strict) {
         return HibernateUtil.exec(new HibernateCallback<List<String>>() {
 
             @SuppressWarnings("unchecked")
             public List<String> run(Session session) {
                 LinkedList<String> result = new LinkedList<String>();
                 List<Tag> tags;
                 HashMap<String, Object> params = new HashMap<String, Object>();
                 String query = "select t from Tag t left join t.parent p where 1=1";
                 if (exclude != null && exclude.length > 0) {
                     query += " and t.name not in (:exc)";
                     params.put("exc", exclude);
                 }
                 if (strict) {
                     if (exclude.length > 0) {
                         query += " and t.parent.name = :p";
                         params.put("p", exclude[exclude.length - 1]);
                     } else {
                         query += " and t.parent is null";
                     }
                 } else {
                     query += " and ((t.parent is not null and p.name in (:p)) or t.parent is null)";
                     params.put("p", exclude);
                 }
                 tags = session.createQuery(query).setProperties(params).list();
                 for (Tag tag : tags) {
                     result.add(tag.getName());
                 }
                 return result;
             }
         });
     }
 
     public static List<String> listFiles(final Filepath filepath) {
         return HibernateUtil.exec(new HibernateCallback<List<String>>() {
 
             public List<String> run(Session session) {
                 StringBuilder where = new StringBuilder();
                 int n = 0;
                 HashMap<String, Object> params = new HashMap<String, Object>();
                 for (String tag : filepath.getPath()) {
                     if (tag.equals(CONCATTAGS)) {
                         where.append(" or 1=1");
                     } else {
                         Tag tagEntity = (Tag) session.createQuery("from Tag t where t.name = :tag").setString("tag", tag).uniqueResult();
                         if (tagEntity != null) {
                             where.append(" and :tag" + n + " in elements(f.tags)");
                             params.put("tag" + n, tagEntity);
                         }
                         n++;
                     }
                 }
                 @SuppressWarnings("unchecked")
                 List<FileRecord> fileRecords = session
                         .createQuery("select f from FileRecord f where 1=1" + where.toString() + " order by f.name").setProperties(params)
                         .list();
                 // if we're in @@ directory, every file will have an id and tags anyway
                 if (fileRecords.size() > 1 && !filepath.isContentWithTags()) {
                     FileRecord curRec = fileRecords.get(0);
                     boolean doRename = false;
                     boolean equal = false;
                     for (FileRecord fileRecord : fileRecords.subList(1, fileRecords.size())) {
                         equal = curRec.getName().equals(fileRecord.getName());
                         if (equal || doRename) {
                             session.evict(curRec);
                             curRec.setName(curRec.getId() + IDSEPARATOR + curRec.getName());
                         }
                         doRename = equal;
                         curRec = fileRecord;
                     }
                     if (doRename) {
                         session.evict(curRec);
                         curRec.setName(curRec.getId() + IDSEPARATOR + curRec.getName());
                     }
                 }
                 List<String> result = new LinkedList<String>();
                 if (filepath.isContentWithTags()) {
                     StringBuilder strRecord = new StringBuilder();
                     for (FileRecord fileRecord : fileRecords) {
                         strRecord.append(fileRecord.getId()).append(IDSEPARATOR);
                         for (Tag tag : fileRecord.getTags()) {
                             strRecord.append(tag.getName()).append(IDSEPARATOR);
                         }
                         strRecord.append(fileRecord.getName());
                         result.add(strRecord.toString());
                         strRecord.setLength(0);
                     }
                 } else {
                     for (FileRecord fileRecord : fileRecords) {
                         result.add(fileRecord.getName());
                     }
                 }
                 return result;
             }
         });
     }
 
     public static File openFileByFilepath(Filepath filepath) {
         String strPath = filepath.asStringPath();
         File result = fileCache.get(strPath);
         if (result == null) {
             FileRecord fileRecord = getFileRecordByFilepath(filepath);
             result = openFileByNameId(fileRecord.getName(), fileRecord.getId());
             synchronized (fileCache) {
                 fileCache.put(strPath, result);
             }
         }
         return result;
     }
 
     public static void removeFileFromCache(Filepath filepath) {
         synchronized (fileCache) {
             fileCache.remove(filepath.asStringPath());
         }
     }
 
     public static File openFileByNameId(String name, Long id) {
         return new File(STORAGE + File.separator + id % 1000 + File.separator + id + IDSEPARATOR + name);
     }
 
     RootHandler rootHandler = new RootHandler();
 
     List<FSHandler> handlers = rootHandler.getHandlers();
 
     HashMap<String, FSHandler> pathHandlerMap = new HashMap<String, FSHandler>();
 
     public FSHandlerManager() {
         handlers.add(rootHandler);
         for (FSHandler handler : handlers) {
             pathHandlerMap.put(handler.getPrefix(), handler);
         }
     }
 
     public static void addTag(final String newTag) {
         addTag(newTag, null);
     }
 
     public static void addTag(final String newTag, final String parent) {
         HibernateUtil.exec(new HibernateCallback<Void>() {
 
             public Void run(Session session) {
                 Tag tag = getTagByName(newTag, session);
                 if (tag == null) {
                     tag = new Tag(newTag);
                     if (parent != null) {
                         Tag parentTag = getTagByName(parent, session);
                         tag.setParent(parentTag);
                     }
                     session.save(tag);
                 }
                 return null;
             }
         });
     }
 
     public boolean containsByName(List<FileRecord> fileRecords, String name) {
         for (FileRecord fileRecord : fileRecords) {
             if (fileRecord.getName().equals(name)) {
                 return true;
             }
         }
         return false;
     }
 
     public int deleteTag(final String delTag) {
         try {
             HibernateUtil.exec(new HibernateCallback<Void>() {
 
                 public Void run(Session session) {
                     Tag tag = getTagByName(delTag, session);
                     if (tag == null) {
                         throw new RuntimeException();
                     }
                     session.delete(tag);
                     return null;
                 }
             });
         } catch (RuntimeException e) {
             return -ErrorCodes.ENOENT();
         }
         return 0;
     }
 
     public void getattr(String path, StatWrapper stat) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler;
         if (filepath.getName() == null) {
             handler = getHandlerByPath(filepath.up(1));
         } else {
             handler = getHandlerByPath(filepath);
         }
         handler.getattr(filepath.strip(handler.getDepth()), stat);
     }
 
     public FSHandler getHandlerByPath(Filepath filepath) throws FSHandlerException {
         FSHandler result;
         if (filepath.getPath().length > 0) {
             result = pathHandlerMap.get(filepath.getPath()[0]);
         } else {
             result = pathHandlerMap.get("/");
         }
         if (result == null) {
             throw new FSHandlerException("Handler not found.");
         }
         return result;
     }
 
     public static List<Tag> getTagsEntries(final Filepath filepath) {
         return HibernateUtil.exec(new HibernateCallback<List<Tag>>() {
 
             @SuppressWarnings("unchecked")
             public List<Tag> run(Session session) {
                 return session.createQuery("from Tag t where t.name in (:tags)").setParameterList("tags", filepath.getPath()).list();
             }
         });
     }
 
     public boolean isContent(String[] pathTags) {
         if (pathTags == null || pathTags.length == 0) {
             return false;
         }
         return pathTags[pathTags.length - 1].equals(ENDOFTAGS);
     }
 
     public Filepath parseFilePath(String path) {
         String[] pathTags = splitPath(path);
         Filepath result = new Filepath();
         if (pathTags.length > 1 && (pathTags[pathTags.length - 2].equals(ENDOFTAGS) || pathTags[pathTags.length - 2].equals(TAGGEDCONTENT))) {
             result.setName(pathTags[pathTags.length - 1]);
             result.setPath(Arrays.copyOf(pathTags, pathTags.length - 2));
         } else {
             if (pathTags.length > 1 && pathTags[pathTags.length - 1].equals(ENDOFTAGS)) {
                 result.setPath(Arrays.copyOf(pathTags, pathTags.length - 1));
                 result.setContent(true);
             } else {
                 if (pathTags.length > 1 && pathTags[pathTags.length - 1].equals(TAGGEDCONTENT)) {
                     result.setPath(Arrays.copyOf(pathTags, pathTags.length - 1));
                     result.setContent(true);
                     result.setContentWithTags(true);
                 } else {
                     result.setPath(pathTags);
                 }
             }
         }
         return result;
     }
 
     public int read(String path, ByteBuffer buffer, long size, long offset, FileInfoWrapper info) {
         Filepath filepath = parseFilePath(path);
         try {
             FSHandler handler = getHandlerByPath(filepath);
             return handler.read(strip(filepath, handler), buffer, size, offset);
         } catch (FSHandlerException e) {
             return 0;
         }
     }
 
     public void readdir(String path, DirectoryFiller filler) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler = getHandlerByPath(filepath);
         filler.add(handler.readdir(strip(filepath, handler)));
     }
 
     public String[] splitPath(String path) {
         path = path.substring(1);
         if (path.isEmpty()) {
             return new String[0];
         }
         return path.split(File.separator);
     }
 
     public void mkdir(String path) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler = getHandlerByPath(filepath);
         handler.mkdir(strip(filepath, handler));
     }
 
     private Filepath strip(Filepath filepath, FSHandler handler) {
         return filepath.strip(handler.getDepth());
     }
 
     public void rename(String path, String newName) throws FSHandlerException {
         Filepath from = parseFilePath(path);
         Filepath to = parseFilePath(newName);
         FSHandler handlerFrom = getHandlerByPath(from);
         FSHandler handlerTo = getHandlerByPath(to);
         if (handlerFrom == handlerTo) {
             handlerFrom.rename(strip(from, handlerFrom), strip(to, handlerFrom));
         } else {
             throw new FSHandlerException("cross-module renaming not allowed.");
         }
     }
 
     public static Tag getTagByName(String tagName, Session session) {
         return (Tag) session.createQuery("from Tag t where t.name = :tag").setString("tag", tagName).uniqueResult();
     }
 
     public void unlink(String path) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler = getHandlerByPath(filepath);
         handler.unlink(strip(filepath, handler));
     }
 
     public void truncate(String path, long offset) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler = getHandlerByPath(filepath);
         handler.truncate(strip(filepath, handler), offset);
     }
 
     public void create(String path, FileInfoWrapper info) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler = getHandlerByPath(filepath);
         handler.create(strip(filepath, handler), info);
     }
 
     public int write(String path, ByteBuffer buffer, long bufSize, long writeOffset, FileInfoWrapper wrapper) {
         Filepath filepath = parseFilePath(path);
         try {
             FSHandler handler = getHandlerByPath(filepath);
             return handler.write(strip(filepath, handler), buffer, bufSize, writeOffset);
         } catch (FSHandlerException e) {
             return 0;
         }
     }
 
     public void open(String path, FileInfoWrapper info) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler = getHandlerByPath(filepath);
         handler.open(strip(filepath, handler), info);
     }
 
     public void release(String path, FileInfoWrapper info) throws FSHandlerException {
         Filepath filepath = parseFilePath(path);
         FSHandler handler = getHandlerByPath(filepath);
         handler.release(strip(filepath, handler), info);
     }
 }
