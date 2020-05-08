 package ca.softwarebyslim.filefilter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author slim902
  * 
  * SnycFolder will setup a folder in a user specified destination on the hard drive
  * 		- Should set up the folder on initial run
  * 		- Should be able to determine if a folder has been created (ie not the first run)
  * 		
  */
 public class SyncFolder {
 
 	static Boolean DEBUG_MODE = true;
 	
 	public final static String DEFAULT_FOLDERNAME = "FileFilter SyncFolder";        // Name of Folder we create
	String USER_DEFINED_SYNC_FOLDER = "";											// if non null, user chose a location 
	boolean usingDefaultSyncFolderLocation = true;									// instead of default location
 	
 	static FilterRuleManager ruleManager;
 	static SyncFolder sf;
 	
 	/**
 	 * Main method. Creates new instances of a SyncFolder, DirectoryWatcher, FilterRuleManger, 
 	 * and our GUIBuilder, all on their own threads. We also create a executor service that checks 
 	 * for new WatchEvents at a specified time interval.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		// Create a new SyncFolder instance
 		sf = new SyncFolder();
 		sf.checkSyncFolderExists(new File(DEFAULT_FOLDERNAME));
 
 		// Create a new DirectoryWatcher instance
 		final DirectoryWatcher dw = new DirectoryWatcher(sf.getSyncFolderPath());
 		Thread watcherThread = new Thread(dw);
 		watcherThread.start();
 		
 		// Schedule a new thread to continuously check for watchEvents
 		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
 		exec.scheduleAtFixedRate(new Runnable() {
 			@Override
 			public void run() {
 				if(DEBUG_MODE) {
 					System.out.println("Finally Running!!");
 				}
 				  
 				final String[] lastEvent = dw.getOldestWatchEvent();
 				if(lastEvent.length > 0) {
 					sf.processWatchEvent(lastEvent);
 				}
 			      
 				for(String s : lastEvent) {
 					System.out.println(s);
 			    }
 			}
 		}, 0, 10, TimeUnit.SECONDS);		
 
 		
 		ruleManager = new FilterRuleManager();
 		
 		// new thread to build gui
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				CardLayoutBuilder cardLayout = new CardLayoutBuilder(ruleManager);
 				cardLayout.buildGUI();
 			}
 		});
 	
 	}
 	
 	/**
 	 * Get the path to our SyncFolder.
 	 * @return (String): path to users SyncFolder
 	 */
 	public String getSyncFolderPath() {
 		if(usingDefaultSyncFolderLocation){
 			return getDesktopPath() + "\\" + DEFAULT_FOLDERNAME;
 		}
 		else {
 			return USER_DEFINED_SYNC_FOLDER;
 		}
 	}
 	
 	/**
 	 * Gets the users desktopPath.
 	 * @return (String): path to users desktop.
 	 */
 	public String getDesktopPath() {
 		return System.getProperty("user.home") + "/Desktop";
 	}
 
 	/**
 	 * Checks if a specified directory exists, and if not, create it
 	 * @param pathToDir (String): path to the directory that we are verifying exists
 	 * @author slim902
 	 */
 	private void dirExistsCheck(String pathToDir){
 		File dirFile = new File(pathToDir);
 		if(!dirFile.isDirectory()) {
 			dirFile.mkdirs();
 			if(DEBUG_MODE) {
 				System.out.println("Directory Created!");
 			}
 		} else {
 			if(DEBUG_MODE) {
 				System.out.println("Directory Exists!");
 			}
 		}
 	}
 	
 	/**
 	 *  Takes a path to a file, and checks if a directory 
 	 *  is at that location.
 	 * 
 	 * @param dirName: File of dirPath. want to check if this file exists
 	 */
 	public void checkSyncFolderExists(File dirName) {
 		
 		String desktopPath = getDesktopPath() + "\\" + dirName;
 		String conflictPath = desktopPath + "\\Conflicts";
 		String userDefinedPath = USER_DEFINED_SYNC_FOLDER + "\\" + DEFAULT_FOLDERNAME;
 		File conflictFolder;
 		
 		// if user has specified a folder use that
 		// else use default path (create folder on users desktop)
 		if(!(USER_DEFINED_SYNC_FOLDER == "")) {
 			String conflictDir = userDefinedPath + "\\Conflicts";
 			conflictFolder = new File(conflictDir);
 			
 		} else {  		
 			conflictFolder = new File(conflictPath);
 		}
 		
 		// check if dir exists, if not make it
 		if(!conflictFolder.isDirectory()) {
 			conflictFolder.mkdirs();
 			if(DEBUG_MODE) {
 				System.out.println("Directory Created!");
 			}
 		} else {
 			if(DEBUG_MODE) {
 				System.out.println("Directory Exists!");
 			}
 
 		}
 	}
 	
 	
 	/**
 	 * Moves/Copies a file to a specified location. 
 	 * @param fromPath (String): path to the current location of file
 	 * @param toPath (String): path to the desired location for the file.
 	 * @param deleteOriginal (Boolean): if true, file is moved (original deleted); if false file is  copied (original not delete) 
 	 * @author slim902
 	 */
 	public static void copyFile(String fromPath, String toPath, Boolean deleteOriginal) {
 		if(DEBUG_MODE) {
 			System.out.println("CopyFile fromPath: " + fromPath + "\ntoPath: " + toPath + "\ndeleteOriginal: " + deleteOriginal);
 		}
 		InputStream inStream = null;
 		OutputStream outStream  = null;
 		
 		try {
 			File fromFile = new File(fromPath);
 			File toFile = new File(toPath);
 			
 			inStream = new FileInputStream(fromFile);
 			outStream = new FileOutputStream(toFile);
 			
 			byte[] buffer = new byte[1024];
 			
 			int length;
 			// copy the file content in bytes
 			while((length = inStream.read(buffer)) > 0) {
 				outStream.write(buffer, 0, length);
 			}
 			
 			inStream.close();
 			outStream.close();
 			
 			// delete the original file
 			if(deleteOriginal){
 				fromFile.delete();
 			}
 			
 			if(DEBUG_MODE) {
 				System.out.println("File is copied successfully!");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * Moves/Copies a folder to a specified location. 
 	 * @param fromPath (String): path to the current location of folder
 	 * @param toPath (String): path to the desired location for the folder.
 	 * @param deleteOriginal (Boolean): if true, folder is moved (original deleted); if false folder is  copied (original not delete) 
 	 * @author slim902
 	 */
 
 	public static void copyFolder(String fromPath, String toPath, Boolean deleteOriginal) {
 		
 		// ArrayList Passed from filewalker where each string[] {path, filename, isDir}
 		ArrayList<String[]> listOfSubdirectories;
 		// ArrayList to put the pathbreakdowns in. send that to be moved and deleted if necessary
 		ArrayList<String[]> listOfBrokendownPaths = new ArrayList<String[]>();
 		//ArrayList that holds the path of all folders to be deleted
 		ArrayList<File> deleteQueue = new ArrayList<File>(); 
 		
 		sf.dirExistsCheck(toPath);
 		listOfSubdirectories = FileWalker.walk(fromPath);
 		
 		// take list of subdirectories and breakdown each path, then put into listOfBrokendownPaths
 		for(String[] aFile : listOfSubdirectories) {
 			PathBreakdown brokendownPath = new PathBreakdown(aFile);
 			listOfBrokendownPaths.add(brokendownPath.breakdownPath());
 			if(DEBUG_MODE){
 				System.out.println(brokendownPath + " added to listOfBrokendownPaths.");
 			}
 		}
 		
 		// paths[] = {path, fileName, isDir}
 		for( String[] paths : listOfBrokendownPaths ) {
 			if(DEBUG_MODE){
 				System.out.println("Paths[0]: " + paths[0]);
 				System.out.println("Paths[1]: " + paths[1]);
 				System.out.println("Paths[2]: " + paths[2]);
 				System.out.println("Paths[3]: " + paths[3]);
 			}
 			
 			// we have a dir: make a new dir of the same name at toPath location
 			if(paths[2].equalsIgnoreCase("dir")) {
 				File newFolderName = new File(toPath + paths[3] + "\\" + paths[1]);
 				newFolderName.mkdir();
 				
 				// add the folder that was copied to the deleteQueue, if deleteOriginal set
 				if(deleteOriginal) {
 					File oldFolderName = new File(paths[0]);
 					deleteQueue.add(oldFolderName);
 					if(DEBUG_MODE) {
 						System.out.println("Folder to be Deleted: " + paths[0]);
 					}
 				}
 			}
 			// we have a file: make sure the destination path exists, the move the file to the desired location
 			else if(paths[2].equalsIgnoreCase("file")){
 				
 				File destinationFolder = new File(toPath + paths[3]);
 				if(!destinationFolder.isDirectory()) {
 					destinationFolder.mkdirs();
 					if(DEBUG_MODE) {
 						System.out.println("Directory Created!");
 					}
 				}
 				copyFile(paths[0], toPath + paths[3] + "\\" + paths[1], deleteOriginal);
 				if(DEBUG_MODE){
 					System.out.println(paths[1] + " copied to " + toPath + paths[3] + "\\" + paths[1] + " from " + paths[0]);
 				}
 			}else {}
 		}
 		// add the original from folder to the deleteQueue, and delete all folders in the deleteQueue
 		if(deleteOriginal){
 			deleteQueue.add(new File(fromPath));
 		}
 		deleteFolder(deleteQueue);
 		
 	}
 	
 	/**
 	 * Deletes a set of nested, empty directories. Directories pre-empited 
 	 * by SyncFolder.copyFolder().  
 	 * @param deleteQueue (ArrayList<File>): a list of files to be deleted
 	 * @author slim902
 	 */
 	public static void deleteFolder(ArrayList<File> deleteQueue) {
 		for(File folder: deleteQueue) {
 			if(DEBUG_MODE) {
 				System.out.println("TO Be Deleted: " + folder.toString());
 			}
 			folder.delete();
 		}
 		
 	}
 	
 	/**
 	 * Take a watchEvent, analyze, move to the location set by FilterRuleManger
 	 * @param watchEvent (String[]): {FilePath, FileExtension, FileName} passed from DirectoryWatcher.
 	 * @author slim902
 	 */
 	public void processWatchEvent(String[] watchEvent) {
 		
 		String filePath = watchEvent[0];
 		String ext = watchEvent[1];
 		String fileName = watchEvent[2];
 
 		// list of destinationPaths for the given file ext
 		ArrayList<String> destinationPathList = new ArrayList<String>();
 		destinationPathList = ruleManager.findDestinationPathOfFileType(ext);
 
 		// if we have a folder, send it to copyFolder
 		if(ext.equalsIgnoreCase("folder")){
 			// if destinationPathList is empty, there is no rule set for this file ext in FilterRuleManager. Send to conflict folder, and alert user
 			if(destinationPathList.size() == 0){
 				copyFolder(filePath, getSyncFolderPath() + "\\Conflicts\\" + fileName, true);
 				if(DEBUG_MODE){
 					System.out.println("Folder Moved to: " + getSyncFolderPath() + "\\Conflicts\\" + fileName +"........");
 				}
 			}
 			else{
 				for(String path : destinationPathList) {
 					copyFolder(filePath, path + "\\" + fileName, true);
 					if(DEBUG_MODE){
 						System.out.println("Folder Moved to: " + path +"........");
 					}
 				}
 			}
 		}
 		// if not a folder, we have some type of file. send it to copyFile
 		else {
 			// if destinationPathList is empty, there is no rule set for this file ext in FilterRuleManager. Send to conflict folder, and alert user
 			if(destinationPathList.size() == 0){
 				copyFile(filePath, getSyncFolderPath() + "\\Conflicts\\" + fileName, true);
 				if(DEBUG_MODE){
 					System.out.println("Folder Moved to: " + getSyncFolderPath() + "\\Conflicts\\" + fileName +"........");
 				}
 			}
 			// otherwise, for each destinationPath, send a copy of the file to that location
 			else {
 				for(String path : destinationPathList) {
 					copyFile(filePath, path + "\\" + fileName, true);
 					if(DEBUG_MODE){
 						System.out.println("File Moved to: " + path +"........");
 					}
 				}
 			}
 		}
 	}
}
