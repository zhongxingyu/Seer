 package de.thatsich.core.javafx;
 
 import java.io.File;
 import java.nio.file.Path;
 
 import javafx.stage.FileChooser;
 import javafx.stage.FileChooser.ExtensionFilter;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import de.thatsich.bachelor.service.IConfigService;
 import de.thatsich.core.Log;
 
 /**
  * ImageFileChooser
  * 
  * Injected Singleton Class.
  * Set up automatically the last selected location
  * and enables only OpenCV Supported images.
  * 
  * @author Tran Minh Do
  *
  */
 @Singleton
 public class ImageFileChooser {
 
 	/**
 	 * JavaFX FileChooser
 	 */
 	private FileChooser chooser;
 	
 	/**
 	 * Title of ImageFileChooser.
 	 */
 	final private static String TITLE = "Add Image File";
 	
 	/**
 	 * Injected Config to retrieve last location.
 	 */
 	private final IConfigService config;
 	
 	/**
 	 * Injected Logger
 	 */
 	private final Log log;
 	
 	/**
 	 * Guice Injected Constructor
 	 * 
 	 * @param Log log Injecting Logger
 	 * @param IConfigService config Access to preferences
 	 */
 	@Inject
 	public ImageFileChooser(Log log, IConfigService config) {
 		this.chooser = new FileChooser();
 		this.log = log;
 		this.config = config;
 		
 		this.chooser.getExtensionFilters().addAll(this.getExtensions());
 		this.log.info("Set up OpenCV-supported extensions.");
 		
 		this.chooser.setTitle(TITLE);
 		this.log.info("Set up Title: " + TITLE);
 		
 		File lastLocation = new File(this.config.getLastLocationString());
 		if (!lastLocation.isDirectory()) {
 			this.log.warning("Last Location is invalid, loading user dir.");
 			lastLocation = new File(System.getProperty("user.home"));
 		}
 		
 		this.chooser.setInitialDirectory(lastLocation);
 		this.log.info("Set up initial directory: " + lastLocation.getAbsolutePath());
 	}
 
 	
 	/**
 	 * Show the Open Dialog
 	 * 
 	 * @return null if no file selected else the selected File converted to Path
 	 */
 	public Path show() {
 		File result = this.chooser.showOpenDialog(null);
 		this.log.info("Showing Open Dialog.");
 
 		if (result == null) {
 			this.log.warning("No File selected.");
 			return null;
 		}
 		
 		this.config.setLastLocationString(result.getParent());
 		this.log.info("Stored last DirectoryPath: " + result.getParent());
 		
 		return result.toPath();
 	}
 	
 	/**
 	 * Results in an Array of File-Extensions used by OpenCV and JavaFX (jpg, png)
 	 * 
 	 * @return all OpenCV-supported Image-extensions
 	 */
 	private ExtensionFilter getExtensions() {	
 		
 		String[] extensions = {
 			"*.jpeg",
 			"*.jpg",
 			"*.jpe",
 
 			"*.png",
 		};
 		this.log.info("Created Extension Array: " + extensions.length + ".");
 		
 		return new ExtensionFilter("OpenCV Supported", extensions);
 	}
 }
