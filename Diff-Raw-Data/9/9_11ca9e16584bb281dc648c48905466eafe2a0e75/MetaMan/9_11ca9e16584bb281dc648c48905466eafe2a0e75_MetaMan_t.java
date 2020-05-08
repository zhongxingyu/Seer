 package org.coms362.group7.MetaMan;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 
  * @author COM S 362 Group 7
  * @version 1.0
  * 
  */
 public class MetaMan {
 
 	/**
 	 * List of files resulting from the last ls command
 	 */
 	private List<MetaManFile> cache;
 
 	/**
 	 * the currently selected audio file
 	 */
 	private MetaManFile selectedFile;
 
 	/**
 	 * The current working directory
 	 */
 	private File workingDirectory;
 
 	/**
 	 * A flag to determine if a listing method has been called. If not then no
 	 * other methods can be called.
 	 */
 	private boolean listingMethodHasBeenCalled = false;
 
 	public MetaMan(File startingDirectory) throws MetaManException {
 		this.workingDirectory = startingDirectory;
 		if (!this.workingDirectory.exists()) {
 			throw new MetaManException("The home directory could not be loaded");
 		}
 	}
 
 	/**
 	 * Change the directory to the one specified
 	 * 
 	 * @param dir
 	 *            the target directory to change to
 	 * @return true if changing was successful
 	 */
 	public boolean changeDirectory(String dir) {
 		File file;
 		if (dir.equals("..") || dir.equals("../")) {
 			this.goUpOneDirectory();
 			return true;
 		}
 		if (dir.equals("../../") || dir.equals("../..")) {
 			this.goUpOneDirectory();
 			this.goUpOneDirectory();
 			return true;
 		}
 		if ((file = new File(dir)).exists()) {
 			this.workingDirectory = file;
 			this.listingMethodHasBeenCalled = false;
 			return true;
 		} else if ((file = new File(this.workingDirectory + "\\" + dir))
 				.exists()) {
 			this.listingMethodHasBeenCalled = false;
 			this.workingDirectory = file;
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Deletes the currently selected file.
 	 * 
 	 * @return True if the file is deleted
 	 */
 	public boolean deleteSelectedFile() {
 		if (this.selectedFile == null) {
 			throw new NullPointerException();
 		}
 		return this.selectedFile.delete();
 	}
 
 	/**
 	 * Move to the parent directory
 	 * 
 	 * @return true if the move was successful
 	 */
 	public boolean goUpOneDirectory() {
 		final String inCaseOfNull = this.workingDirectory.getAbsolutePath();
 		this.workingDirectory = this.workingDirectory.getParentFile();
 		if (this.workingDirectory == null) {
 			this.workingDirectory = new File(inCaseOfNull);
 			return false;
 		}
 		this.listingMethodHasBeenCalled = false;
 		return true;
 	}
 
 	/**
 	 * Lists the metaman files in the current directory
 	 * 
 	 * @return list of all of the compatible files
 	 * @throws MetaManException
 	 */
 	public List<MetaManFile> listing() throws MetaManException {
 		final ArrayList<MetaManFile> list = new ArrayList<MetaManFile>();
 		list.addAll(this.listingDirectoriesOnly());
 		list.addAll(this.listingAudioOnly());
 		list.addAll(this.listingImagesOnly());
 		this.cache = list;
 		this.listingMethodHasBeenCalled = true;
 		return list;
 	}
 
 	/**
 	 * List only the audio files
 	 * 
 	 * @return list of all supported audio files in the directory
 	 * @throws MetaManException
 	 */
 	public List<MetaManFile> listingAudioOnly() throws MetaManException {
 		final List<MetaManFile> list = new ArrayList<MetaManFile>();
 		for (final File f : this.workingDirectory
 				.listFiles(new AudioFileFilter())) {
 			list.add(new AudioFile(f.getAbsolutePath()));
 		}
 		this.cache = list;
 		this.listingMethodHasBeenCalled = true;
 		return list;
 	}
 
 	/**
 	 * List the directories in the current working directory
 	 * 
 	 * @return list of all the directories
 	 */
 	public List<MetaManFile> listingDirectoriesOnly() {
 		final ArrayList<MetaManFile> list = new ArrayList<MetaManFile>();
 		for (final File f : this.workingDirectory.listFiles()) {
 			if (this.workingDirectory.isDirectory()) {
 				list.add(new MetaManDirectory(f.getAbsolutePath()));
 			}
 		}
 		this.cache = list;
 		this.listingMethodHasBeenCalled = true;
 		return list;
 	}
 
 	public List<MetaManFile> listingImagesOnly() throws MetaManException {
 		final List<MetaManFile> list = new ArrayList<MetaManFile>();
 		for (final File f : this.workingDirectory
 				.listFiles(new ImageFileFilter())) {
 			list.add(new ImageFile(f.getAbsolutePath()));
 		}
 		this.cache = list;
 		this.listingMethodHasBeenCalled = true;
 		return list;
 	}
 
 	/**
 	 * Locks the selected file
 	 * 
 	 * @return true if locked
 	 */
 	public boolean lockSelectedFile() {
 		if (this.selectedFile == null) {
 			throw new NullPointerException();
 		}
 		return this.selectedFile.lock();
 	}
 
 	/**
 	 * Modifies the metadata tag with the newValue
 	 * 
 	 * @param key
 	 *            The meta data key to be changed
 	 * @param newValue
 	 *            The value the key will be changed to
 	 * @return
 	 * @throws MetaManException
 	 */
 	public boolean modMetaDataOfSelectedFile(String key, String newValue)
 			throws MetaManException {
 		if (this.selectedFile == null) {
 			throw new NullPointerException();
 		}
 		return this.selectedFile.setMetaData(key, newValue);
 	}
 
 	/**
 	 * Opens the selected file
 	 * 
 	 * @return true if the file is opened
 	 */
 	public boolean openSelectedFile() {
 		if (this.selectedFile == null) {
 			throw new NullPointerException();
 		}
 		return this.selectedFile.open();
 	}
 
 	/**
 	 * Prints the current working directory's absolute path
 	 * 
 	 * @return the absolute path of the file
 	 */
 	public String printWorkingDirectory() {
 		return this.workingDirectory.getAbsolutePath();
 	}
 
 	/**
 	 * Sets the selected audio file
 	 * 
 	 * @param index
 	 *            position of the file in the cache(this is the index that is
 	 *            printed in the veiw)
 	 * @return true if the file could be selected
 	 * @throws MetaManException
 	 */
 	public boolean setSelectedFile(int index) throws MetaManException {
 		if (!this.listingMethodHasBeenCalled) {
 			throw new MetaManException(
 					"Cannot preform action.  No listing method has been called.");
 		}
 		this.selectedFile = this.cache.get(index);
 		return true;
 	}
 
 	/**
 	 * Unlocks the selected file
 	 * 
 	 * @return true if unlocked
 	 */
 	public boolean unlockSelectedFile() {
 		if (this.selectedFile == null) {
 			throw new NullPointerException();
 		}
 		return this.selectedFile.unlock();
 	}
 
 	/**
 	 * Gets a string representation of the meta data
 	 * 
 	 * @return the meta data of the file
 	 * @throws MetaManException
 	 */
 	public String viewMetaDataOfSelectedFile() throws MetaManException {
 		if (this.selectedFile == null) {
 			throw new NullPointerException();
 		}
 		return this.selectedFile.view();
 	}
 	
 	/**
 	 * Renames the selected file
 	 * 
 	 * @return True if the filename was changed
 	 */
 	public boolean renameSelectedFile(String newName) {
 		if (this.selectedFile == null) {
 			throw new NullPointerException();
 		}
		return this.selectedFile.rename(this.printWorkingDirectory().toString() + "\\" + newName.trim() + this.selectedFile.getName().substring(this.selectedFile.getName().lastIndexOf('.')).trim());
 	}
 
 }
