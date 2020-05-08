 package at.subera.memento.files;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 
 import javax.imageio.ImageIO;
 
 import org.apache.log4j.Logger;
 
 import at.subera.memento.rest.bean.Image;
 import at.subera.memento.util.AeSimpleSHA1;
 
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.Directory;
 import com.drew.metadata.Metadata;
 import com.drew.metadata.MetadataException;
 import com.drew.metadata.exif.ExifIFD0Directory;
 import com.drew.metadata.exif.ExifSubIFDDirectory;
 import com.drew.metadata.exif.GpsDirectory;
 
 public class ImageHelper {
 	// found due tests
 	public static final int TAG_WIN_RATING = 0x4746;
 
 	public static Image readImageFromFile(Path file, Logger logger)
 			throws IOException, ImageProcessingException,
 			NoSuchAlgorithmException {
 		// check if file is image
 		@SuppressWarnings("unused")
 		BufferedImage image = ImageIO.read(file.toFile());
 
 		Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());
 
 		// add image to service
 		Image i = new Image();
 		i.setId(AeSimpleSHA1.SHA1(file.toString()));
 		i.setPath(file.toString());
 		i.setFilename(file.getFileName().toString());
 
 		// add basic file attributes
 		BasicFileAttributes attributes = Files.readAttributes(file,
 				BasicFileAttributes.class);
 		i.setFilesize(attributes.size());
 		i.setLastModifiedTime(new Date(attributes.lastModifiedTime().toMillis()));
 
 		if (logger.isInfoEnabled()) {
 			logger.info(metadata);
 		}
 
 		// JPEG Metadata
 		try {
 			Directory directory;
 			// obtain the Exif directory
 			if (metadata.containsDirectory(ExifIFD0Directory.class)) {
 				directory = metadata.getDirectory(ExifIFD0Directory.class);
 				// Image Orientation
				if (directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
					i.setOrientation((byte) directory
							.getInt(ExifIFD0Directory.TAG_ORIENTATION));
				}
 				// Windows Keywords
 				if (directory.containsTag(ExifIFD0Directory.TAG_WIN_KEYWORDS)) {
 					i.setKeywords(directory
 							.getDescription(ExifIFD0Directory.TAG_WIN_KEYWORDS));
 				}
 				// Windows Rating
 				int rating = -1;
 				if (directory.containsTag(TAG_WIN_RATING)) {
 					rating = directory.getInt(TAG_WIN_RATING);
 				}
 				i.setRating(rating);
 			}
 			// obtain the Exif sub directory
 			if (metadata.containsDirectory(ExifSubIFDDirectory.class)) {
 				directory = metadata.getDirectory(ExifSubIFDDirectory.class);
 				// creation date
 				i.setCreationTime(directory
 						.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
 				// resolution width
 				i.setWidth(directory
 						.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
 				// resolution height
 				i.setHeight(directory
 						.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
 			}
 			// obtain the GPS directory
 			if (metadata.containsDirectory(GpsDirectory.class)) {
 				directory = metadata.getDirectory(GpsDirectory.class);
 				// gps coordinations
 				i.setLatitude(directory.getInt(GpsDirectory.TAG_GPS_LATITUDE));
 				i.setLongitude(directory.getInt(GpsDirectory.TAG_GPS_LONGITUDE));
 			}
 		} catch (MetadataException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			// if (logger.isInfoEnabled()) {
 			// logger.info(e);
 			// }
 		}
 
 		if (logger.isDebugEnabled()) {
 			logger.debug(i);
 		}
 
 		return i;
 	}
 
 	/**
 	 * scaleImage
 	 * 
 	 * found on:
 	 * http://www.webmaster-talk.com/coding-forum/63227-image-resizing-
 	 * in-java.html
 	 * 
 	 * This returns an inputstream which you can save to your database or write
 	 * to a file
 	 * 
 	 * @param file
 	 * @param p_width
 	 * @param p_height
 	 * @return
 	 * @throws Exception
 	 */
 	public static InputStream scaleImage(File file, int p_width, int p_height)
 			throws Exception 
 	{
 
 		java.awt.Image image = (java.awt.Image) ImageIO.read(file);
 
 		int thumbWidth = p_width;
 		int thumbHeight = p_height;
 
 		// Make sure the aspect ratio is maintained, so the image is not skewed
 		double thumbRatio = (double) thumbWidth / (double) thumbHeight;
 		int imageWidth = image.getWidth(null);
 		int imageHeight = image.getHeight(null);
 		double imageRatio = (double) imageWidth / (double) imageHeight;
 		if (thumbRatio < imageRatio) {
 			thumbHeight = (int) (thumbWidth / imageRatio);
 		} else {
 			thumbWidth = (int) (thumbHeight * imageRatio);
 		}
 
 		// Draw the scaled image
 		BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
 				BufferedImage.TYPE_INT_RGB);
 		Graphics2D graphics2D = thumbImage.createGraphics();
 		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
 
 		// Write the scaled image to the outputstream
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		ImageIO.write(thumbImage, "jpg", out);
 
 		// Read the outputstream into the inputstream for the return value
 		ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
 
 		return bis;
 	}
 }
