 package net.dean.ljgm;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javafx.beans.InvalidationListener;
 import javafx.beans.Observable;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 
 import javax.xml.transform.stream.StreamResult;
 
 import net.dean.ljgm.logging.LoggingLevel;
 import net.dean.util.CollectionUtils;
 import net.dean.util.file.FileUtil;
 import net.dean.util.file.XMLUtils;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This class' job is to manage the IO of the project. This mainly includes
  * loading and saving gallery metadata to {@link #CONFIG_XML}.
  */
 public class GalleryManager {
 
 	/**
 	 * The location of the "library.xml" file that holds all the data about the
 	 * Galleries and their images. This location points to
 	 * <code>{BASE_DIR}/library.xml</code>.
 	 */
 	private static final File CONFIG_XML = FileUtil.getRelativeFile("/library.xml");
 
 	/**
 	 * A list of {@link Gallery} objects that are updated directly from the.
 	 * {@link #CONFIG_XML} file.
 	 */
 	private ObservableList<Gallery> currentGalleries;
 
 	private DateFormat backupDateFormat;
 
 	/**
 	 * Instantiates a new GalleryManger.
 	 */
 	public GalleryManager() {
 		this.currentGalleries = loadFromFile();
 		currentGalleries.addListener(new InvalidationListener() {
 			
 			@Override
 			public void invalidated(Observable observable) {
 				LJGM.instance().refreshGalleries();
 			}
 		});
 		
 		this.backupDateFormat = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
 	}
 
 	/**
 	 * Converts the values of {@link #currentGalleries} into XML Elements and
 	 * then outputs the file into the location specified by {@link #CONFIG_XML}.
 	 */
 	public void save(boolean backup) {
		if (backup && CONFIG_XML.exists()) {
 			// Create the backup directory
 			File target = new File(System.getProperty("user.dir") + "/backups/libary-" + backupDateFormat.format(new Date())
 					+ ".xml");
 			
 			// Assume the worst
 			boolean created = false;
 			try {
 				created = target.getParentFile().mkdirs();
 			} catch (SecurityException e) {
 				LJGM.instance().getLogger().err("Could not create the backup directory. Make sure you have the permission to write here.");
 			}
 			
 			// If a security exception was thrown then this will
 			// not execute because created was set to false
 			if (!created && !target.getParentFile().exists()) {
 				// mkdirs() returned false and the parent file does not exist
 				// We have to include the second test because mkdirs() returns false
 				// if the directory wasn't created or if the directory already exists.
 				LJGM.instance().getLogger().warn("Could not create backup directory!");
 			} else {
 				try {
 					Files.copy(Paths.get(CONFIG_XML.toURI()), Paths.get(target.toURI()));
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (SecurityException e) {
 					// This will be thrown if the backup directory already exists but 
 					LJGM.instance().getLogger().err("Could not back up the library. Make sure you have permission to write here.");
 				}
 			}
 
 		}
 
 		Document d = XMLUtils.newDocument();
 		Element rootElem = d.createElement("library");
 
 		for (Gallery g : currentGalleries) {
 			// Create an element to represent the gallery, and assign the
 			// attribute "name"
 			Element galleryElem = d.createElement("gallery");
 			galleryElem.setAttribute("name", g.getName());
 
 			// Iterate through all of the images
 			for (GallerySource source : g.getSources()) {
 				Element sourceElement = d.createElement("source");
 				// Set the watched and subdirs attributes
 				sourceElement.setAttribute("watched", String.valueOf(source.isWatched()));
 				sourceElement.setAttribute("subdirs", String.valueOf(source.isIncludeSubdirectories()));
 				sourceElement.setAttribute("directory", source.getDirectory().getAbsolutePath());
 
 				// Set the files attribute if it isn't watched
 				if (!source.isWatched()) {
 					StringBuilder files = new StringBuilder();
 					for (String str : source.getImages()) {
 						files.append(str + ";");
 					}
 					sourceElement.setAttribute("files", files.toString());
 				}
 
 				galleryElem.appendChild(sourceElement);
 			}
 
 			rootElem.appendChild(galleryElem);
 		}
 		d.appendChild(rootElem);
 
 		XMLUtils.export(d, new StreamResult(CONFIG_XML));
 	}
 
 	/**
 	 * Loads the file {@link #CONFIG_XML} and gets a list of galleries objects
 	 * from it.
 	 * 
 	 * @return A list of galleries objects parsed from the XML.
 	 */
 	public ObservableList<Gallery> loadFromFile() {
 		LJGM.instance().getLogger().info("Loading galleries from the library...");
 		ObservableList<Gallery> galleries = FXCollections.observableArrayList();
 
 		Document d;
 		try {
 			d = XMLUtils.newDocument(CONFIG_XML);
 		} catch (IOException e) {
 			// Could not locate the file, bad encoding, etc.
 			LJGM.instance().getLogger().throwable(e, "There was a problem reading the file");
 			return galleries;
 		} catch (SAXException e) {
 			// Syntax error
 			LJGM.instance().getLogger().throwable(e, "There was a problem parsing the file");
 			return galleries;
 		}
 
 		LJGM.instance().getLogger().info("The library contains no XML syntax errors.");
 
 		Element library = d.getDocumentElement();
 
 		// Get all the gallery elements
 		NodeList galleryNL = library.getElementsByTagName("gallery");
 
 		// Iterate through the gallery elements
 		for (int i = 0; i < galleryNL.getLength(); i++) {
 			Node n = galleryNL.item(i);
 
 			// Cast it to an element
 			if (n.getNodeType() == Node.ELEMENT_NODE) {
 				Element gallery = (Element) n;
 
 				// Get the name of the gallery, which defaults to
 				// gallery.UNKNOWN_NAME.
 				String name = getGalleryElementName(gallery);
 
 				// Contains the gallery's images
 				List<GallerySource> gallerySources = new ArrayList<GallerySource>();
 
 				// Optimized by only looking for images if the parent has
 				// child nodes
 				if (gallery.hasChildNodes()) {
 					// Get all the sources for the gallery
 					NodeList sourcesNodeList = gallery.getElementsByTagName("source");
 					for (int j = 0; j < sourcesNodeList.getLength(); j++) {
 						Node sourceNode = sourcesNodeList.item(j);
 						if (sourceNode.getNodeType() == Node.ELEMENT_NODE) {
 							GallerySource source = getSourceFrom((Element) sourceNode);
 							// Could be null if the directory is non-existent or
 							// not a directory
 							if (source != null) {
 								gallerySources.add(source);
 							}
 						}
 					}
 				}
 
 				// Obtained the necessary objects to construct a gallery object
 				Gallery g = new Gallery(name, gallerySources);
 				LJGM.instance().getLogger()
 						.info("Parsed a new gallery with " + g.getAllImages().size() + " images: " + g.getName());
 				galleries.add(g);
 			}
 		}
 
 		// LJGM.instance().getLogger().info("Checking for duplicate galleries...");
 		// // TODO: Check for duplicates
 		// int duplicates = 0;
 		// List<Gallery> duplicatesRemoved = new ArrayList<>();
 		// for (Gallery g : galleries) {
 		// for (Gallery g2 : galleries) {
 		// if (g.getName().equals(g2.getName())) {
 		// duplicatesRemoved.add(combine(g, g2));
 		// duplicates++;
 		// } else if (g.equals(g2)) {
 		// // Exact same object
 		// } else {
 		// duplicatesRemoved.add(g);
 		// }
 		// }
 		// }
 		// LJGM.instance().getLogger().info(duplicates +
 		// " duplicates removed.");
 		return galleries;
 		// return duplicatesRemoved;
 	}
 
 	//
 	// /**
 	// * Combines two Galleries together. If the names are the same, then the
 	// * combined name will be also. However, if they are different, then the
 	// new
 	// * name will be equal to <code>one.getName + " + " + two.getName()</code>
 	// *
 	// * @param one
 	// * The first gallery
 	// * @param two
 	// * The second gallery
 	// * @return A combined version of the two galleries
 	// */
 	// private Gallery combine(Gallery one, Gallery two) {
 	// String name = one.getName();
 	// if (!one.getName().equals(two.getName())) {
 	// name = one + " + " + two;
 	// }
 	// List<File> images = new ArrayList<>(one.getSources());
 	// images.addAll(two.getSources());
 	// return new Gallery(name, images);
 	// }
 
 	/**
 	 * Gets the files from a given 'folder' element. The element must have a
 	 * <code>src</code> attribute to specify the location of the directory that
 	 * the images are in. It can also have a <code>subdirs</code> attribute,
 	 * which specified whether or not to look in the subdirectories of the given
 	 * folder. The default value is <code>false</code>.
 	 * 
 	 * 
 	 * @param sourceElement
 	 *            The folder element.
 	 * @return A List of files from the given directory of the folder element's
 	 *         "src" attribute.
 	 */
 	private GallerySource getSourceFrom(Element sourceElement) {
 		final List<String> sourceFiles = new ArrayList<String>();
 		//@formatter:off
 		final Path dir = Paths.get(new File(XMLUtils.getIfHasAttribute(sourceElement, "directory",
 				LJGM.instance().getLogger().format("Could not find the source directory for a source node in gallery "
 								+ getGalleryElementName((Element) sourceElement.getParentNode()) + ".", LoggingLevel.WARN), System.err)).toURI());
 		//@formatter:on
 		final boolean includeSubdirs = Boolean.parseBoolean(XMLUtils.getIfHasAttribute(sourceElement, "subdirs", "false"));
 		final boolean watched = Boolean.parseBoolean(XMLUtils.getIfHasAttribute(sourceElement, "watched", "false"));
 
 		File f = dir.toFile();
 		if (!f.exists()) {
 			LJGM.instance()
 					.getLogger()
 					.err("No such file or directory in the folder element " + "\""
 							+ getGalleryElementName((Element) sourceElement.getParentNode()) + "\": " + f.getAbsolutePath());
 
 			return null;
 		}
 
 		// Check for non-directories
 		if (!f.isDirectory()) {
 			LJGM.instance()
 					.getLogger()
 					.err("A non-directory was specified in the folder element \""
 							+ getGalleryElementName((Element) sourceElement.getParentNode()) + "\": " + f.getAbsolutePath()
 							+ "; ignoring.");
 
 			return null;
 		}
 
 		if (!watched) {
 			// Get all the files separated by a semicolon
 			String[] fileArray = XMLUtils.getIfHasAttribute(sourceElement, "files", "").split(";");
 
 			// Nothing else to do, return.
 			return new GallerySource(f, CollectionUtils.toCollection(fileArray));
 		}
 
 		// Directory can only be watched at this point, so we walk the file tree
 		sourceFiles.addAll(LJGMUtils.getRelativeImagesFrom(f, includeSubdirs));
 
 		return new GallerySource(f, sourceFiles, watched, includeSubdirs);
 	}
 
 	/**
 	 * Gets the name of a given Gallery element. If the element has an attribute
 	 * called <code>name</code>, then it will return the value of said
 	 * attribute. However, if the attribute is <i>not</i> found, then it will
 	 * return "Unknown".
 	 * 
 	 * @param galleryElement
 	 *            the gallery element
 	 * @return the gallery element name
 	 */
 	private String getGalleryElementName(Element galleryElement) {
 		return XMLUtils.getIfHasAttribute(galleryElement, "name", Gallery.UNKNOWN_NAME);
 	}
 
 	/**
 	 * A list of galleries that have been parsed from {@link #CONFIG_XML}.
 	 * 
 	 * @return A List of parsed {@link Gallery} objects.
 	 */
 	public List<Gallery> getGalleries() {
 		return currentGalleries;
 	}
 
 	/**
 	 * Searches for a gallery with a particular name.
 	 * 
 	 * @param name
 	 *            The name of the wanted gallery
 	 * @return A {@link Gallery} object with the given name. If no
 	 *         {@link Gallery} has that name, <code>null</code> is returned.
 	 */
 	public Gallery getGallery(String name) {
 		for (Gallery g : currentGalleries) {
 			if (g.getName().equals(name)) {
 				return g;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * If the given gallery has the same name as one of the current galleries,
 	 * then it is removed. The gallery is then added to the list.
 	 * 
 	 * @param g
 	 */
 	public void updateGallery(Gallery g) {
 		// If the gallery already exists, remove it from the list first
 		// to avoid duplication
 		boolean contains = false;
 		for (Gallery gal : currentGalleries) {
 			if (gal.getName().equals(g.getName())) {
 				contains = true;
 				// Don't want to waste any more time
 				break;
 			}
 		}
 		
 		if (contains) {
 			currentGalleries.remove(g);
 		}
 
 		// Add the gallery to the list. The galleries will automatically
 		// be refreshed in the ViewingArea.
 		currentGalleries.add(g);
 
 		// Save the galleries to the file
 		save(true);
 	}
 }
