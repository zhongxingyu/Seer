 /*
  * 
  */
 package net.dean.ljgm.gui.gallerycreator;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javafx.beans.InvalidationListener;
 import javafx.beans.Observable;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.scene.control.TextField;
 import javafx.scene.control.Tooltip;
 
 /*
  * DirectoryTextField.java
  *
  * Part of project LJGM (Lightweight Java Gallery Manager) (net.dean.ljgm.gui.gallerycreator)
  *
  * Originally created on Jul 19, 2013 by Matthew
  */
 /**
  * This class is used to show the user what this source is referring to, and if
  * the source exists or not.
  */
 class DirectoryTextField extends TextField {
 
 	/** The base directory of this source */
 	private ObjectProperty<File> directory;
 
 	/** The list of image names and directories relative to {@link #directory}. */
 	private ObservableList<String> images;
 
 	/**
 	 * The selector in which the component is contained.
 	 */
 	private SourceSelector sourceSelector;
 
 	/**
 	 * Instantiates a new directory text field.
 	 * 
 	 * @param sourceSelector
 	 *            The source selector used to determine if the source is valid
 	 */
 	public DirectoryTextField(SourceSelector sourceSelector) {
 		this(sourceSelector, null);
 	}
 
 	/**
 	 * Instantiates a new directory text field with a base directory.
 	 * 
 	 * @param sourceSelector
 	 *            The source selector used to determine if the source is valid
 	 * @param directory
 	 *            the directory The directory of the source
 	 */
 	public DirectoryTextField(SourceSelector sourceSelector, File directory) {
 		this(sourceSelector, directory, FXCollections.observableArrayList(new ArrayList<String>()));
 	}
 
 	/**
 	 * Instantiates a new directory text field with a base directory and a list
 	 * of images.
 	 * 
 	 * @param sourceSelector
 	 *            The source selector used to determine if the source is valid
 	 * @param directory
 	 *            the directory The directory of the source
 	 * @param images
 	 *            the images The list of images of the source
 	 */
 	public DirectoryTextField(SourceSelector sourceSelector, final File directory, ObservableList<String> images) {
 		this.directory = new SimpleObjectProperty<File>(directory);
 		this.images = images;
 		this.sourceSelector = sourceSelector;
 
 		InvalidationListener isValidSourceListener = new InvalidationListener() {
 			
 			@Override
 			public void invalidated(Observable observable) {
 				DirectoryTextField.this.sourceSelector.updateValidity();
 			}
 		};
 		this.images.addListener(isValidSourceListener);
 		this.directory.addListener(isValidSourceListener);
 		
 		// Only update if a directory was given
 		if (directory != null) {
 			updateContents();
 		}
 
 		setEditable(false);
 
 		// We do not need to know what the new value is, only that it has
 		// changed and the directory needs to be revalidated, so I use an
 		// InvalidationListener instead of a ChangeListener.
 //		textProperty().addListener(new InvalidationListener() {
 //
 //			@Override
 //			public void invalidated(Observable o) {
 //				updateBorderColorAndTooltip();
 //			}
 //		});
 		
 		this.sourceSelector.validProperty().addListener(new InvalidationListener() {
 			
 			@Override
 			public void invalidated(Observable observable) {
 				updateBorderColorAndTooltip();
 			}
 		});
 	}
 	
 	void updateBorderColorAndTooltip() {
 		boolean valid = DirectoryTextField.this.sourceSelector.validProperty().get();
 		//System.out.println("Tooltip: Valid source? " + valid + "; Valid dir? " + isValidDirectory() + "; valid images? " + isValidImages() + "; text not empty? " + getText().isEmpty());
 		// Assume the directory is invalid
 		String backgroundColor = "red";
 		// Not a valid directory
 		if (valid || getText().isEmpty()) {
 			backgroundColor = "-fx-focus-color";
 		}
 
 		DirectoryTextField.this.sourceSelector.getDirectoryField().setStyle(
 				"-fx-background-color: " + backgroundColor + ", -fx-text-box-border, -fx-control-inner-background;");
 
 		// If it is not valid, then disable the button. If it is
 		// valid, enable it.
 		DirectoryTextField.this.sourceSelector.updateImageButton();
 
 		// Update the tooltip
 		if (!valid && !getText().isEmpty()) {
 			DirectoryTextField.this.setTooltip(new Tooltip(getErrorMessage()));
 		} else {
 			DirectoryTextField.this.setTooltip(null);
 		}
 	}
 
 	/**
 	 * Gets an error message about why the source is invalid.
 	 * 
 	 * @return An error message
 	 * @throws IllegalArgumentException
 	 *             If the source is valid
 	 */
 	private String getErrorMessage() {
 		if (sourceSelector.validProperty().get()) {
 			throw new IllegalArgumentException("There is nothing wrong");
 		}
 
 		// User opened the directory chooser and closed it without picking
 		// anything
 		if (directory == null) {
 			throw new IllegalArgumentException("There is nothing wrong");
 		}
 
 		// Something wrong with the directory
 		if (!directory.get().exists()) {
 			return "This directory does not exist: " + directory.get().getAbsolutePath();
 		}
 		
 		if (images.size() == 0) {
 			return "You need to add some images!";
 		}
 
 		// Something wrong with the images
 		List<String> nonExistantImages = new ArrayList<>();
 		for (String relative : images) {
 			if (!new File(directory.get(), relative).exists()) {
 				nonExistantImages.add(relative);
 			}
 		}
 
 		StringBuilder error = new StringBuilder("Images do not exist: ");
 		for (int i = 0; i < nonExistantImages.size(); i++) {
 			error.append(nonExistantImages.get(i));
 			if (i != nonExistantImages.size()) {
 				error.append(", ");
 			}
 		}
 		return error.toString();
 	}
 
 	/**
 	 * Updates the contents of the textbox to display the information about the
 	 * directory and the images.
 	 */
 	private void updateContents() {
 		if (directory == null) {
 			setText("");
 			return;
 		}
 
 		StringBuilder imagesString = new StringBuilder();
 		for (String image : images) {
 			imagesString.append(" \"" + image + "\"");
 		}
 		setText("\"" + directory.get() + "\"" + imagesString.toString());
 	}
 
 	/**
 	 * Sets the directory and clears the images.
 	 * 
 	 * @param dir
 	 *            The new directory
 	 */
 	public void setDirectory(File dir) {
 		this.directory.set(dir);
 
 		// Reset the files
 		images.clear();
 
 		updateContents();
 	}
 
 	/**
 	 * Sets the images. Once the images are set, the contents of the textfield
 	 * are updated.
 	 * 
 	 * @param images
 	 *            The new images
 	 */
 	public void setImages(List<String> images) {
 		images.clear();
 		images.addAll(images);
 		updateContents();
 	}
 
 	/**
 	 * Checks if the directory is valid.
 	 * 
 	 * @return True, if the directory exists. False if the directory does not
 	 *         exist or is null.
 	 */
 	public boolean isValidDirectory() {
		if (directory == null) {
 			return false;
 		}
 		return directory.get().exists();
 	}
 
 	/**
 	 * Checks if all the images in the list exist.
 	 * 
 	 * @return True, if all the images in the list exist. False if the size of
 	 * the list is 0.
 	 */
 	public boolean isValidImages() {
 		if (images == null) {
 			return false;
 		}
 
 		if (images.isEmpty()) {
 			return false;
 		}
 
 		for (String relative : images) {
 			if (!new File(directory.get(), relative).exists()) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Resets this directory textfield by setting the directory to null, which in turn
 	 * clears the images, which then updates the contents so that the field is empty.
 	 */
 	public void reset() {
 		// By passing null it makes sure the textbox is empty in
 		// updateContents()
 		setDirectory(null);
 	}
 
 	/**
 	 * Gets the directory.
 	 * 
 	 * @return The directory
 	 */
 	public File getDirectory() {
 		return directory.get();
 	}
 
 	/**
 	 * Gets the images.
 	 * 
 	 * @return the images
 	 */
 	public List<String> getImages() {
 		return images;
 	}
 }
