 /*
  * #%L
  * debox-photos
  * %%
  * Copyright (C) 2012 Debox
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 package org.debox.photo.job;
 
 import org.debox.photo.model.configuration.ThumbnailSize;
 import java.io.IOException;
 import java.nio.file.*;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.*;
import java.util.logging.Level;
 import org.apache.shiro.SecurityUtils;
 import org.debox.imaging.ImageUtils;
 import org.debox.imaging.ThumbnailGenerator;
 import org.debox.photo.dao.AlbumDao;
 import org.debox.photo.dao.PhotoDao;
 import org.debox.photo.model.*;
 import org.debox.photo.util.FileUtils;
 import org.debox.photo.util.SessionUtils;
 import org.debox.photo.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class RegenerateThumbnailsJob implements FileVisitor<Path>, Runnable {
 
     private static final Logger logger = LoggerFactory.getLogger(RegenerateThumbnailsJob.class);
     
     protected static PhotoDao photoDao = new PhotoDao();
     protected static AlbumDao albumDao = new AlbumDao();
     protected ForkJoinPool forkJoinPool = new ForkJoinPool();
     
     protected Path source;
     protected Path target;
     
     protected boolean aborted = false;
     
     protected ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));
     protected List<Future> imageProcesses = new ArrayList<>();
 
     public RegenerateThumbnailsJob(Path source, Path target) {
         this.source = source;
         this.target = target;
     }
     
     @Override
     public void run() {
         try {
             // Reset & cleaning somes resources
             threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));
             forkJoinPool = new ForkJoinPool();
             
             // Cleaning target path
             FileUtils.deleteDirectory(target.toFile());
 
             // Create target if not exists
             if (!Files.exists(target)) {
                 FileUtils.createDirectories(target);
             }
 
             // Launch directory scan
             Files.walkFileTree(source, this);
             
         } catch (IOException ex) {
             logger.error(ex.getMessage(), ex);
         }
     }
 
     public Path getSource() {
         return source;
     }
 
     public Path getTarget() {
         return target;
     }
 
     public void setSource(Path source) {
         this.source = source;
     }
 
     public void setTarget(Path target) {
         this.target = target;
     }
     
     public boolean isTerminated() {
         for (Future future : imageProcesses) {
             if (!future.isDone()) {
                 return false;
             }
         }
         return true;
     }
     
     public long getNumberToProcess() {
         long result = getPhotosToProcess();
         return result;
     }
     
     public long getNumberProcessed() {
         long result = getTerminatedProcessesCount();
         return result;
     }
 
     public long getTerminatedProcessesCount() {
         long result = 0;
         for (Future future : imageProcesses) {
             if (future.isDone()) {
                 result++;
             }
         }
         return result;
     }
     
     public long getPhotosToProcess() {
         return imageProcesses.size();
     }
     
     public void abort() {
         aborted = true;
         threadPool.shutdownNow();
         forkJoinPool.shutdownNow();
         imageProcesses.clear();
         aborted = false;
     }
 
     @Override
     public FileVisitResult preVisitDirectory(Path currentPath, BasicFileAttributes attributes) throws IOException {
         logger.info("Begin directory visit: {}", currentPath);
 
         if (aborted) {
             return FileVisitResult.TERMINATE;
 
         } else if (currentPath.equals(this.source)) {
             return FileVisitResult.CONTINUE;
         }
 
         try {
             // Create target path if not exists
             String albumsBasePath = ImageUtils.getAlbumsBasePath(SessionUtils.getUserId());
             String relativePath = StringUtils.substringAfter(currentPath.toString(), albumsBasePath);
             Path targetPath = Paths.get(this.target + relativePath);
             if (!Files.exists(targetPath)) {
                 Files.createDirectories(targetPath, FileUtils.PERMISSIONS);
             }
         } catch (SQLException ex) {
             return FileVisitResult.TERMINATE;
         }
 
         return FileVisitResult.CONTINUE;
     }
 
     @Override
     public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
         try {
             logger.info("File visited: {}", path);
 
             String mimeType = Files.probeContentType(path);
             if (!ImageUtils.JPEG_MIME_TYPE.equals(mimeType)) {
                 logger.warn("Tried to process a non-jpeg image, mime-type was: {}", mimeType);
                 return FileVisitResult.CONTINUE;
 
             } else if (aborted) {
                 return FileVisitResult.TERMINATE;
             }
 
             Photo photo = new Photo();
             String albumsBasePath = ImageUtils.getAlbumsBasePath(SessionUtils.getUserId());
             photo.setRelativePath(StringUtils.substringAfter(path.getParent().toString(), albumsBasePath));
             photo.setFilename(path.getFileName().toString());
             photo.setOwnerId(SessionUtils.getUser(SecurityUtils.getSubject()).getId());
             ThumbnailGenerator processor = new ThumbnailGenerator(photo, ThumbnailSize.LARGE, ThumbnailSize.SQUARE);
             Future future = threadPool.submit(processor);
             imageProcesses.add(future);
             return FileVisitResult.CONTINUE;
         } catch (SQLException ex) {
             return FileVisitResult.CONTINUE;
         }
     }
 
     @Override
     public FileVisitResult visitFileFailed(Path path, IOException exception) throws IOException {
         logger.error("Error visiting file " + path.toString(), exception);
         if (aborted) {
             return FileVisitResult.TERMINATE;
         }
         return FileVisitResult.CONTINUE;
     }
 
     @Override
     public FileVisitResult postVisitDirectory(Path path, IOException ioe) throws IOException {
         logger.info("End directory visit: {}", path);
         if (aborted) {
             return FileVisitResult.TERMINATE;
 
         } else if (!path.equals(source)) {
             return FileVisitResult.CONTINUE;
         }
         return FileVisitResult.TERMINATE;
     }
 
 }
