 package at.subera.memento.filevisitor;
 
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.log4j.Logger;
 
 import at.subera.memento.image.ContentTypeExcpetion;
import at.subera.memento.image.ImageHelper;
 import at.subera.memento.rest.service.ImageService;
 
 import com.drew.imaging.ImageProcessingException;
 
 /**
  * ImageFileVisitor
  * 
  * checks Files if they are Images and add them into the imageService
  */
 public class ImageFileVisitor extends SimpleFileVisitor<Path> {
 	protected ImageService imageService;
 	
 	private static final Logger logger = Logger.getLogger(ImageFileVisitor.class);
 	
 	public void setImageService(ImageService imageService) {
 		this.imageService = imageService;
 	}
 	
 	@Override
 	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
 			throws IOException {
 		imageService.removeByPath(dir.toString());
 		return FileVisitResult.CONTINUE;
 	}
 
 	@Override
 	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
 			throws IOException {
 		if (logger.isInfoEnabled()) {
 			logger.info("Working on File: " + file.toString());
 		}
 		
 		// check if File is image
 		String contentType = Files.probeContentType(file);
		if (ImageHelper.isContentTypeAnImage(contentType)) {
 			return FileVisitResult.CONTINUE;
 		}
 		
 		try {			
 			imageService.add(ImageHelper.readImageFromFile(file, logger));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ImageProcessingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ContentTypeExcpetion e) {
 			// Typical, not an image
 			if (logger.isInfoEnabled()) {
 				logger.info(e);
 			}
 		}
 		
 		return FileVisitResult.CONTINUE;
 	}
 }
