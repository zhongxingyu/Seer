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
 package org.debox.imaging;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import org.apache.commons.lang3.tuple.Pair;
 import org.debox.imaging.gm.GraphicsMagickImageHandler;
 import org.debox.imaging.gm.ImageMagickDateReader;
 import org.debox.imaging.gm.StringOutputConsumer;
 import org.debox.imaging.imgscalr.ImgScalrImageHandler;
 import org.debox.imaging.metadataextractor.MetadataExtractorDateReader;
 import org.debox.imaging.thumbnailator.ThumbnailatorImageHandler;
 import org.debox.photo.dao.PhotoDao;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.Configuration;
 import org.debox.photo.model.Media;
import org.debox.photo.model.Photo;
 import org.debox.photo.model.Video;
 import org.debox.photo.model.configuration.ThumbnailSize;
 import org.debox.photo.server.ApplicationContext;
 import org.im4java.core.IM4JavaException;
 import org.im4java.core.IMOperation;
 import org.im4java.core.IdentifyCmd;
 import org.im4java.core.ImageCommand;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class ImageUtils {
     
     private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);
     
     public static final String JPEG_MIME_TYPE = "image/jpeg";
     
     protected static PhotoDao photoDao = new PhotoDao();
     
     protected static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
     protected static ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 2, 1));
     protected static final ConcurrentHashMap<String, Future> inProgressPaths = new ConcurrentHashMap<>();
     
     static {
         // Schedule a cleaner for futures collection, every 1 minute
         scheduler.scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
                 for (String path : inProgressPaths.keySet()) {
                     Future future = inProgressPaths.get(path);
                     if (future.isDone()) {
                         inProgressPaths.remove(path);
                     }
                 }
             }
         }, 1, 1, TimeUnit.MINUTES);
     }
     
     protected static List<ImageHandler> implementations = new ArrayList(3){{
         add(new ThumbnailatorImageHandler());
         add(new ImgScalrImageHandler());
         add(new GraphicsMagickImageHandler());
     }};
     
     protected static List<DateReader> dateReaders = new ArrayList(2){{
         add(new MetadataExtractorDateReader());
         add(new ImageMagickDateReader());
         add(new DefaultFileDateReader());
     }};
     
     public void abort() {
         threadPool.shutdownNow();
         threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 2, 1));
     }
     
     public static FileInputStream getStream(Media media, ThumbnailSize size) throws Exception {
         String targetPath;
         if (ThumbnailSize.ORIGINAL.equals(size)) {
             targetPath = ImageUtils.getSourcePath(media);
         } else {
             targetPath = ImageUtils.getThumbnailPath(media, size);
         }
         
         FileInputStream fis = null;
         try {
             fis = new FileInputStream(targetPath);
 
         } catch (IOException ex) {
             log.warn("Unable to load stream for path " + targetPath);
             if (!ThumbnailSize.ORIGINAL.equals(size)) {
                 Future<Pair<String, FileInputStream>> future = inProgressPaths.get(targetPath);
                 if (future == null) {
                     ThumbnailGenerator processor = new ThumbnailGenerator(media, size);
                     future = threadPool.submit(processor);
                     inProgressPaths.put(targetPath, future);
                 }
                 fis = future.get().getValue();
                 try {
                     photoDao.saveThumbnailGenerationTime(media.getId(), size, new Date().getTime());
                 } catch (SQLException sqle) {
                     log.error("Unable to save time generation for photo: " + targetPath, sqle);
                 }
                 inProgressPaths.remove(targetPath);
             }
         }
         return fis;
     }
 
     protected static String getBasePath() {
         Configuration configuration = ApplicationContext.getInstance().getOverallConfiguration();
         String basePath = configuration.get(Configuration.Key.WORKING_DIRECTORY);
         return basePath;
     }
     
     public static String getAlbumsBasePath(String ownerId) throws SQLException {
         Configuration configuration = ApplicationContext.getInstance().getUserConfiguration(ownerId);
         String basePath = configuration.get(Configuration.Key.ALBUMS_DIRECTORY);
         if (basePath == null) {
             basePath = getBasePath() + File.separatorChar + "albums";
         }
         return basePath;
     }
 
     public static String getThumbnailsBasePath(String ownerId) throws SQLException {
         Configuration configuration = ApplicationContext.getInstance().getUserConfiguration(ownerId);
         String basePath = configuration.get(Configuration.Key.THUMBNAILS_DIRECTORY);
         if (basePath == null) {
             basePath = getBasePath() + File.separatorChar + "thumbnails";
         }
         return basePath;
     }
 
     public static String getSourcePath(Media media) throws SQLException {
         String path = getAlbumsBasePath(media.getOwnerId()) + media.getRelativePath() + File.separatorChar + media.getFilename();
         if (media instanceof Video) {
             path += ".jpg";
         }
         return path;
     }
 
     public static String getOggSourcePath(Video video) throws SQLException {
         return getAlbumsBasePath(video.getOwnerId()) + video.getRelativePath() + File.separatorChar + video.getOggFilename();
     }
 
     public static String getH264SourcePath(Video video) throws SQLException {
         return getAlbumsBasePath(video.getOwnerId()) + video.getRelativePath() + File.separatorChar + video.getH264Filename();
     }
 
     public static String getWebMSourcePath(Video video) throws SQLException {
         return getAlbumsBasePath(video.getOwnerId()) + video.getRelativePath() + File.separatorChar + video.getWebMFilename();
     }
 
     public static String getOggThumbnailPath(Video video) throws SQLException {
         return getThumbnailsBasePath(video.getOwnerId()) + video.getRelativePath() + File.separatorChar + video.getOggFilename();
     }
 
     public static String getH264ThumbnailPath(Video video) throws SQLException {
         return getThumbnailsBasePath(video.getOwnerId()) + video.getRelativePath() + File.separatorChar + video.getH264Filename();
     }
 
     public static String getWebMThumbnailPath(Video video) throws SQLException {
         return getThumbnailsBasePath(video.getOwnerId()) + video.getRelativePath() + File.separatorChar + video.getWebMFilename();
     }
 
     public static String getThumbnailPath(Media media, ThumbnailSize size) throws SQLException {
         if (ThumbnailSize.ORIGINAL.equals(size)) {
             return getSourcePath(media);
         }
         String path = getThumbnailsBasePath(media.getOwnerId()) + media.getRelativePath() + File.separatorChar + size.getPrefix() + media.getFilename();
         if (media instanceof Video) {
             path += ".jpg";
         }
         return path;
     }
 
     public static String getSourcePath(Album album) throws SQLException {
         return getAlbumsBasePath(album.getOwnerId()) + album.getRelativePath();
     }
 
     public static String getTargetPath(Album album) throws SQLException {
         return getThumbnailsBasePath(album.getOwnerId()) + album.getRelativePath();
     }
 
     public static Date getShootingDate(Path path) {
         Date result = null;
         for (DateReader dateReader : dateReaders) {
             try {
                 result = dateReader.getShootingDate(path);
                 if (result != null) {
                     break;
                 }
                 log.warn("Unable to read original date in EXIF with {} implementation (unknown reason)", dateReader.getClass().getCanonicalName());
             } catch (Exception ex) {
                 log.warn("Unable to read original date in EXIF with {} implementation, reason: {}", dateReader.getClass().getCanonicalName(), ex.getMessage());
             }
         }
         return result;
     }
 
     public static String getOrientation(String path) throws IOException, IM4JavaException, InterruptedException {
         IMOperation op = new IMOperation();
         op.format("%[exif:Orientation]");
         op.addImage(path);
 
         ImageCommand cmd = new IdentifyCmd();
         StringOutputConsumer output = new StringOutputConsumer();
         cmd.setOutputConsumer(output);
         cmd.run(op);
 
         String result = output.getOutput();
         return result;
     }
 
     public static boolean thumbnail(String sourcePath, String targetPath, ThumbnailSize size) {
         Path source = Paths.get(sourcePath);
         Path target = Paths.get(targetPath);
         for (ImageHandler imageHandler : implementations) {
             try {
                 imageHandler.thumbnail(source, target, size);
                 return true;
             } catch (Exception ex) {
                 log.error("Unable to create thumbnail with " + imageHandler.getClass().getCanonicalName() + " implementation, reason:", ex);
             }
         }
         return false;
     }
     
     public static void thumbnail(String sourcePath, String targetPath, ThumbnailSize size, boolean async) {
         throw new RuntimeException("Not implemented yet!");
     }
 
 }
