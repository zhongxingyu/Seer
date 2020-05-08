 package pl.psnc.dl.wf4ever.fs;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.net.URI;
 import java.nio.file.DirectoryIteratorException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.NoSuchFileException;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.StandardCopyOption;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 
 import pl.psnc.dl.wf4ever.common.HibernateUtil;
 import pl.psnc.dl.wf4ever.common.ResourceInfo;
 import pl.psnc.dl.wf4ever.common.UserProfile;
 import pl.psnc.dl.wf4ever.dao.ResourceInfoDAO;
 import pl.psnc.dl.wf4ever.dao.UserProfileDAO;
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
 
 import com.google.common.collect.Multimap;
 
 /**
  * Filesystem-based digital library.
  * 
  * @author piotrekhol
  * 
  */
 public class FilesystemDL implements DigitalLibrary {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(FilesystemDL.class);
 
     /** base path under which the files will be stored. */
     private Path basePath;
 
 
     /**
      * Constructor.
      * 
      * @param basePath
      *            file path under which the files will be stored
      * @param user
      *            user login
      */
     public FilesystemDL(String basePath, UserMetadata user) {
         if (basePath.endsWith("/")) {
             this.basePath = FileSystems.getDefault().getPath(basePath);
         } else {
             this.basePath = FileSystems.getDefault().getPath(basePath.concat("/"));
         }
     }
 
 
     /**
      * Get resource paths for a folder path.
      * 
      * @param path
      *            path to the folder
      * @return list of resources, excluding folders
      * @throws DigitalLibraryException
      *             an error while traversing the filesystem
      */
     private List<Path> getResourcePaths(Path path)
             throws DigitalLibraryException {
         List<Path> result = new ArrayList<>();
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
             for (Path entry : stream) {
                 if (entry.toFile().isDirectory()) {
                     result.addAll(getResourcePaths(entry));
                 } else {
                     result.add(entry);
                 }
             }
         } catch (DirectoryIteratorException | IOException e) {
             // I/O error encounted during the iteration, the cause is an IOException
             throw new DigitalLibraryException(e);
         }
         return result;
     }
 
 
     @Override
     public InputStream getZippedFolder(URI ro, String folder)
             throws DigitalLibraryException, NotFoundException {
         final Path roPath = getPath(ro, null);
         Path path = getPath(ro, folder);
         final List<Path> paths = getResourcePaths(path);
 
         PipedInputStream in = new PipedInputStream();
         final PipedOutputStream out;
         try {
             out = new PipedOutputStream(in);
         } catch (IOException e) {
             throw new RuntimeException("This should never happen", e);
         }
         final ZipOutputStream zipOut = new ZipOutputStream(out);
         new Thread("edition zip downloader (" + path.toString() + ")") {
 
             @Override
             public void run() {
                 try {
                     for (Path filePath : paths) {
                         ZipEntry entry = new ZipEntry(roPath.relativize(filePath).normalize().toString());
                         zipOut.putNextEntry(entry);
                         InputStream in = Files.newInputStream(filePath);
                         try {
                             IOUtils.copy(in, zipOut);
                         } finally {
                             in.close();
                         }
                     }
                 } catch (IOException e) {
                     LOGGER.error("Zip transmission failed", e);
                 } finally {
                     try {
                         zipOut.close();
                     } catch (Exception e) {
                         LOGGER.warn("Could not close the ZIP file: " + e.getMessage());
                         try {
                             out.close();
                         } catch (IOException e1) {
                             LOGGER.error("Could not close the ZIP output stream", e1);
                         }
                     }
                 }
             };
         }.start();
         return in;
     }
 
 
     @Override
     public InputStream getFileContents(URI ro, String filePath)
             throws DigitalLibraryException, NotFoundException {
         Path path = getPath(ro, filePath);
         try {
             return Files.newInputStream(path);
         } catch (NoSuchFileException e) {
             throw new NotFoundException("File doesn't exist", e);
         } catch (IOException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public boolean fileExists(URI ro, String filePath)
             throws DigitalLibraryException {
         Path path = getPath(ro, filePath);
         return path.toFile().exists();
     }
 
 
     @Override
     public ResourceInfo createOrUpdateFile(URI ro, String filePath, InputStream inputStream, String mimeType)
             throws DigitalLibraryException {
         Path path = getPath(ro, filePath);
         try {
             Files.createDirectories(path.getParent());
             Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
             FileInputStream fis = new FileInputStream(path.toFile());
             String md5 = DigestUtils.md5Hex(fis);
 
             DateTime lastModified = new DateTime(Files.getLastModifiedTime(path).toMillis());
             ResourceInfoDAO dao = new ResourceInfoDAO();
             ResourceInfo res = dao.create(path.toString(), path.getFileName().toString(), md5, Files.size(path), "MD5",
                 lastModified, mimeType);
             dao.save(res);
             HibernateUtil.getSessionFactory().getCurrentSession().flush();
             return res;
         } catch (IOException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public ResourceInfo getFileInfo(URI ro, String filePath) {
         Path path = getPath(ro, filePath);
         ResourceInfoDAO dao = new ResourceInfoDAO();
         return dao.findByPath(path.toString());
     }
 
 
     @Override
     public void deleteFile(URI ro, String filePath)
            throws DigitalLibraryException {
         Path path = getPath(ro, filePath);
         try {
             Files.delete(path);
             ResourceInfoDAO dao = new ResourceInfoDAO();
             ResourceInfo res = dao.findByPath(path.toString());
             dao.delete(res);
             HibernateUtil.getSessionFactory().getCurrentSession().flush();
         } catch (IOException e) {
             throw new DigitalLibraryException(e);
         }
         try {
             path = path.getParent();
             while (path != null) {
                 Files.delete(path);
                 path = path.getParent();
             }
         } catch (IOException e) {
             //it was non empty
             LOGGER.debug("Tried to delete a directory", e);
         }
     }
 
 
     @Override
     public void createResearchObject(URI ro, InputStream mainFileContent, String mainFilePath, String mainFileMimeType)
             throws DigitalLibraryException, ConflictException {
         if (fileExists(ro, mainFilePath)) {
             throw new ConflictException("RO exists");
         }
         createOrUpdateFile(ro, mainFilePath, mainFileContent, mainFileMimeType);
     }
 
 
     @Override
     public void deleteResearchObject(URI ro)
             throws DigitalLibraryException, NotFoundException {
         Path path = getPath(ro, null);
         try {
             Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
 
                 @Override
                 public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                         throws IOException {
                     Files.delete(file);
                     return FileVisitResult.CONTINUE;
                 }
 
 
                 @Override
                 public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                         throws IOException {
                     if (exc == null) {
                         Files.delete(dir);
                         return FileVisitResult.CONTINUE;
                     } else {
                         throw exc;
                     }
                 }
 
             });
         } catch (NoSuchFileException e) {
             throw new NotFoundException("File doesn't exist", e);
         } catch (IOException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public boolean createUser(String login, String password, String username)
             throws DigitalLibraryException {
         UserProfile.Role role;
         if (login.equals("wfadmin")) {
             role = Role.ADMIN;
         } else if (login.equals("wf4ever_reader")) {
             role = Role.PUBLIC;
         } else {
             role = Role.AUTHENTICATED;
         }
         if (userExists(login)) {
             return false;
         }
         UserProfileDAO dao = new UserProfileDAO();
         UserProfile user2 = dao.create(login, username, role);
         dao.save(user2);
         HibernateUtil.getSessionFactory().getCurrentSession().flush();
         return true;
     }
 
 
     @Override
     public UserMetadata getUserProfile(String login) {
         UserProfileDAO dao = new UserProfileDAO();
         return dao.findByLogin(login);
     }
 
 
     @Override
     public boolean userExists(String userId)
             throws DigitalLibraryException {
         UserProfileDAO dao = new UserProfileDAO();
         return dao.findByLogin(userId) != null;
     }
 
 
     @Override
     public void deleteUser(String userId)
             throws DigitalLibraryException, NotFoundException {
         UserProfileDAO dao = new UserProfileDAO();
         UserProfile user2 = dao.findByLogin(userId);
         if (user2 == null) {
             throw new NotFoundException("user not found");
         } else {
             dao.delete(user2);
             HibernateUtil.getSessionFactory().getCurrentSession().flush();
         }
     }
 
 
     @Override
     public InputStream getZippedResearchObject(URI ro)
             throws DigitalLibraryException, NotFoundException {
         return getZippedFolder(ro, ".");
     }
 
 
     @Override
     public void storeAttributes(URI ro, Multimap<URI, Object> roAttributes)
             throws NotFoundException, DigitalLibraryException {
         // TODO Auto-generated method stub
 
     }
 
 
     /**
      * Calculate path from a resource URI.
      * 
      * @param ro
      *            research object
      * @param resourcePath
      *            path or null
      * @return filesystem path
      */
     private Path getPath(URI ro, String resourcePath) {
         Path path = basePath;
         if (ro.getHost() != null) {
             path = path.resolve(ro.getHost());
         }
         if (ro.getPath() != null) {
             if (ro.getPath().startsWith("/")) {
                 path = path.resolve(ro.getPath().substring(1));
             } else {
                 path = path.resolve(ro.getPath());
             }
         }
         if (resourcePath != null) {
             path = path.resolve(resourcePath);
         }
         return path;
     }
 
 }
