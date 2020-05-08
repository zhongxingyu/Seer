 package helpers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Iterator;
 
 import net.coobird.thumbnailator.Thumbnails;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.PrefixFileFilter;
 import org.apache.commons.lang.StringUtils;
 
 import com.typesafe.config.ConfigFactory;
 import play.Logger;
 
 public class ImageHelper {
 
   public static File IMAGE_ROOT = new File(ConfigFactory.load().getString("dvddb.imagespath"));
 
   /**
    * Loads the image from the given url and saves it to the filesystem
    * 
   * @param dvdId
    * @param urlStr
   * @param imageType
   * @param imageSize
    * @return
    */
   public static boolean createFileFromUrl(final Long dvdId, final String urlStr, final EImageType imageType, final EImageSize imageSize) {
 
     if (StringUtils.isEmpty(urlStr) == true) {
       return false;
     }
 
     try {
       final URL url = new URL(urlStr);
       final InputStream is = url.openStream();
 
       final File file = ImageHelper.createFile(dvdId, imageType, imageSize);
 
       if (file.exists() == true) {
         file.delete();
 
         final IOFileFilter fileFilter = new PrefixFileFilter(dvdId + "_" + imageType.name());
         // delete the thumbnails :)
         final Iterator<File> iterateFiles = FileUtils.iterateFiles(ImageHelper.IMAGE_ROOT, fileFilter, null);
         while (iterateFiles.hasNext()) {
           iterateFiles.next().delete();
         }
       }
 
       final FileOutputStream fos = new FileOutputStream(file);
 
       final byte[] buffer = new byte[4096];
       int bytesRead;
       while ((bytesRead = is.read(buffer)) != -1) {
         fos.write(buffer, 0, bytesRead);
       }
       fos.close();
       is.close();
 
       return true;
     } catch (final Exception e) {
       if(Logger.isErrorEnabled()) {
         Logger.error("An error happend while saving the image to the filesystem",e);
       }
       return false;
     }
 
   }
 
   public static File getImageFile(final Long dvdId, final EImageType imageType, final EImageSize imageSize) {
     final File file = ImageHelper.createFile(dvdId, imageType, imageSize);
 
     // if this is not the original one we will creat one :)
     if (file.exists() == false && imageSize.equals(EImageSize.ORIGINAL) == false) {
       // first we load the original one
       final File origFile = ImageHelper.createFile(dvdId, imageType, EImageSize.ORIGINAL);
 
       if (origFile.exists() == false) {
         return null;
       }
 
       ImageHelper.resizeImage(origFile, file, imageSize);
     }
 
     if (file.exists() == true) {
       return file;
     } else {
       return null;
     }
 
   }
 
   /**
    * resizes the image
    * 
    * @param origImgFile
    * @param destSize
    */
   private static void resizeImage(final File origImgFile, final File destImgFile, final EImageSize destSize) {
 
     try {
       Thumbnails.of(origImgFile).size(destSize.getWidth(), destSize.getHeight()).toFile(destImgFile);
     } catch (final IOException e) {
       if(Logger.isErrorEnabled()) {
         Logger.error("An error happend whileresizing the image: "+ origImgFile.getAbsolutePath() +" to: "+destImgFile.getAbsolutePath(),e);
       }
     }
 
   }
 
   private static File createFile(final Long dvdId, final EImageType imageType, final EImageSize imageSize) {
     final File file = new File(ImageHelper.IMAGE_ROOT, dvdId + "_" + imageType.name() + "_" + imageSize.name() + ".jpg");
     return file;
   }
 
 }
