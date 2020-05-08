 package ch.bergturbenthal.image.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.im4java.core.ConvertCmd;
 import org.im4java.core.IM4JavaException;
 import org.im4java.core.IMOperation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.Directory;
 import com.drew.metadata.Metadata;
 import com.drew.metadata.MetadataException;
 import com.drew.metadata.exif.ExifDirectory;
 import com.drew.metadata.exif.GpsDirectory;
 
 public class AlbumImage {
   private static class TagId {
     private final Class<? extends Directory> directory;
     private final int tagId;
 
     public TagId(final Class<? extends Directory> directory, final int tagId) {
       this.directory = directory;
       this.tagId = tagId;
     }
 
   }
 
   private final static Logger logger = LoggerFactory.getLogger(AlbumImage.class);
   private final static MessageFormat THUMBNAIL_MESSAGE_FORMAT = new MessageFormat("{0}-{1}_{2}.jpg");
   private final static MessageFormat CROP_THUMBNAIL_MESSAGE_FORMAT = new MessageFormat("{0}-{1}_{2}c.jpg");
   private final File file;
   private final File cacheDir;
   private Metadata metadata = null;
   private Date captureDate = null;
 
   public AlbumImage(final File file, final File cacheDir) {
     this.file = file;
     this.cacheDir = cacheDir;
   }
 
   public Date captureDate() {
     if (captureDate == null)
       captureDate = readCaptureDateFromMetadata();
 
     return captureDate;
   }
 
   public String getName() {
     return file.getName();
   }
 
   public File getThumbnail(final int width, final int height, final boolean crop, final boolean onlyFromCache) {
     try {
       final File cachedFile = new File(cacheDir, makeCachedFilename(width, height, crop));
       if (cachedFile.exists() && cachedFile.lastModified() > file.lastModified())
         return cachedFile;
       if (onlyFromCache)
         return null;
       synchronized (this) {
         if (cachedFile.exists() && cachedFile.lastModified() >= file.lastModified())
           return cachedFile;
         scaleImageDown(width, height, crop, cachedFile);
       }
       return cachedFile;
     } catch (final IOException e) {
       throw new RuntimeException("Cannot make thumbnail of " + file, e);
     } catch (final InterruptedException e) {
       throw new RuntimeException("Cannot make thumbnail of " + file, e);
     } catch (final IM4JavaException e) {
       throw new RuntimeException("Cannot make thumbnail of " + file, e);
     }
   }
 
   public long readSize() {
     return file.length();
   }
 
   @Override
   public String toString() {
     return "AlbumImage [file=" + file.getName() + "]";
   }
 
   private Metadata getMetadata() {
     if (metadata != null)
       return metadata;
     try {
       metadata = ImageMetadataReader.readMetadata(file);
     } catch (final ImageProcessingException e) {
       throw new RuntimeException("Cannot read metadata from " + file, e);
     }
     return metadata;
   }
 
   private String makeCachedFilename(final int width, final int height, final boolean crop) {
     final MessageFormat filenameFormat;
     if (crop)
       filenameFormat = CROP_THUMBNAIL_MESSAGE_FORMAT;
     else
       filenameFormat = THUMBNAIL_MESSAGE_FORMAT;
     final String cacheFileName;
     synchronized (filenameFormat) {
       cacheFileName = filenameFormat.format(new Object[] { file.getName(), width, height });
     }
     return cacheFileName;
   }
 
   private Date readCaptureDateFromMetadata() {
     final Date gpsDate = readGpsDate();
     if (gpsDate != null)
       return gpsDate;
     for (final TagId index : Arrays.asList(new TagId(ExifDirectory.class, ExifDirectory.TAG_DATETIME_ORIGINAL), new TagId(ExifDirectory.class,
                                                                                                                           ExifDirectory.TAG_DATETIME))) {
       final Date date = readDate(index.directory, index.tagId);
       if (date != null)
         return date;
     }
     return null;
   }
 
   private Date readDate(final Class<? extends Directory> directory, final int tag) {
     try {
       final Metadata metadata = getMetadata();
       if (metadata.containsDirectory(directory)) {
         final Directory directory2 = metadata.getDirectory(directory);
         if (directory2.containsTag(tag))
           try {
             return directory2.getDate(tag);
           } catch (final MetadataException e) {
             throw new RuntimeException("Cannot read " + directory.getName() + ":" + directory2.getDescription(tag) + " from " + file, e);
           }
       }
       return null;
     } catch (final MetadataException e) {
       throw new RuntimeException("Cannot read " + directory.getName() + ":" + tag + " from " + file, e);
     }
   }
 
   private Date readGpsDate() {
     try {
       final Metadata metadata = getMetadata();
       if (!metadata.containsDirectory(GpsDirectory.class))
         return null;
       final Directory directory = metadata.getDirectory(GpsDirectory.class);
       if (!directory.containsTag(GpsDirectory.TAG_GPS_TIME_STAMP))
         return null;
       final int[] time = directory.getIntArray(7);
       final String date = directory.getString(29);
       final Object[] values = new MessageFormat("{0,number}:{1,number}:{2,number}").parse(date);
       final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
       calendar.set(((Number) values[0]).intValue(), ((Number) values[1]).intValue() - 1, ((Number) values[2]).intValue(), time[0], time[1], time[2]);
       return calendar.getTime();
     } catch (final MetadataException e) {
       throw new RuntimeException("Cannot read Gps-Date from " + file, e);
     } catch (final ParseException e) {
       throw new RuntimeException("Cannot read Gps-Date from " + file, e);
     }
   }
 
   private void scaleImageDown(final int width, final int height, final boolean crop, final File cachedFile) throws IOException, InterruptedException,
                                                                                                            IM4JavaException {
     final File tempPngFilename = new File(cachedFile.getParentFile(), cachedFile.getName() + ".tmp.png");
    final File tempFile = new File(cachedFile.getParentFile(), cachedFile.getName() + ".tmp.jpg");
     logger.debug("Start convert " + file);
     final ConvertCmd cmd = new ConvertCmd();
     final IMOperation primaryOperation = new IMOperation();
     primaryOperation.addImage(file.getAbsolutePath());
     primaryOperation.addImage(tempPngFilename.getAbsolutePath());
     final IMOperation secondOperation = new IMOperation();
     secondOperation.addImage(tempPngFilename.getAbsolutePath());
     secondOperation.autoOrient();
     if (crop) {
       secondOperation.resize(Integer.valueOf(width), Integer.valueOf(height), "^");
       secondOperation.gravity("center");
       secondOperation.extent(Integer.valueOf(width), Integer.valueOf(height));
     } else
       secondOperation.resize(Integer.valueOf(width), Integer.valueOf(height));
    secondOperation.quality(Double.valueOf(70));
     secondOperation.addImage(tempFile.getAbsolutePath());
     logger.debug("Start operation 1: " + primaryOperation);
     cmd.run(primaryOperation);
     logger.debug("Start operation 2: " + secondOperation);
     cmd.run(secondOperation);
     tempFile.renameTo(cachedFile);
     tempPngFilename.delete();
     logger.debug("End operation");
   }
 
 }
