 package ch.softhenge.supren.exif.service;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import ch.softhenge.supren.exif.entity.FilePattern;
 import ch.softhenge.supren.exif.entity.ImageFile;
 
 public class ImageServiceTestAllPhotos {
 
 	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
 
 	private ImageService imageService;
 
 	@Before
 	public void setUp() throws Exception {
 		imageService = new ImageService("ruro.properties", "D:\\photos");
 		LOGGER.setLevel(Level.FINE);
         ConsoleHandler handler = new ConsoleHandler();
         handler.setLevel(Level.FINE);
 		LOGGER.addHandler(handler);
 	}
 
 	@Test
 	@Ignore
 	public void testListImageFilesToRename() {
 		imageService.createImageFilesMap();
 		imageService.createImageFilesMap();
 		Map<FilePattern, Collection<ImageFile>> mapOfImageFileCollection = imageService.getMapOfImageFiles();
 		for (Entry<FilePattern, Collection<ImageFile>> imageFiles : mapOfImageFileCollection.entrySet()) {
 			LOGGER.info("Image Files of pattern " + imageFiles.getKey() + " has " + imageFiles.getValue().size() + " values");
 		}
 	}
 	
 	@Test
	@Ignore
 	public void testCreateCsvSeperatedStringOfImageFiles() throws IOException {
 		String csvText = imageService.createCsvSeperatedStringOfImageFiles();
 		File file = new File("csvFileOut");
 		FileWriter fw = new FileWriter(file);
 	    BufferedWriter bw = new BufferedWriter(fw);
 	    bw.write(csvText);
 	    bw.close();
 	}
 	
 }
