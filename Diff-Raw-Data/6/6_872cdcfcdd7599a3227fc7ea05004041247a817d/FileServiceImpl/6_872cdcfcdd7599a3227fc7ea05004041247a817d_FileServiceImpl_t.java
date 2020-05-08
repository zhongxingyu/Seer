 package edu.ucla.loni.server;
 
 import edu.ucla.loni.client.FileService;
 import edu.ucla.loni.shared.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import org.jdom2.Document;
 
 @SuppressWarnings("serial")
 public class FileServiceImpl extends RemoteServiceServlet implements FileService {	
 	////////////////////////////////////////////////////////////
 	// Private Functions
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 * Returns returns an ArrayList of all the pipefiles in the root directory
 	 * Note: this functions only search two levels in
 	 * 
 	 * @param dir, file representing root directory 
 	 */
 	private ArrayList<File> getAllPipefiles(File dir){
 		// Level == 2, dir == root directory
 		// Level == 1, dir == package folder
 		// Level == 0, dir == type folder (do not look go any deeper)
 		return getAllPipefilesRecursive(dir, 2);
 	}
 	
 	/**
 	 *  Recursively get all pipefiles
 	 */
 	private ArrayList<File> getAllPipefilesRecursive(File dir, int level){
 		ArrayList<File> files = new ArrayList<File>();
 		
 		if (level >= 0){		
 			for (File file : dir.listFiles()){
 				if (file.isDirectory()){
 					files.addAll( getAllPipefilesRecursive(file, (level - 1)) );
 				} 
 				else {
 					String name = file.getName();
 					if (name.endsWith(".pipe")){
 						files.add(file);
 					}
 				}
 			}
 		}
 	    
 	    return files;
 	}
 	
 	/**
 	 *  Remove files from the database in the case that they were deleted
 	 */
 	private void cleanDatabase(Directory root) throws Exception{
 		Pipefile[] pipes = Database.selectPipefiles(root.dirId);
 		
 		if (pipes != null){
 			for(Pipefile pipe : pipes){
 				File file = new File(pipe.absolutePath);
 				if (!file.exists()){
 					Database.deletePipefile(pipe);
 				}
 			}
 		}
 	}
 	
 	/**
 	 *  Update the database for this root
 	 */
 	private void updateDatabase(Directory root) throws Exception {
 		// Clean the database
 		cleanDatabase(root);
 		
 		// Get all the files
 		File rootDir = new File(root.absolutePath);
 		ArrayList<File> files = getAllPipefiles(rootDir);
 				
 		// For each pipefile
 		for (File file : files){	
 			Timestamp db_lastModified = Database.selectPipefileLastModified(file.getAbsolutePath());
 		    
 			// Determine if the row needs to be updated or inserted
 		    boolean update = false;
 		    boolean insert = false;
 		    
 		    Timestamp fs_lastModified = new Timestamp(file.lastModified());
 			
 		    if (db_lastModified != null){				
 				// If file has been modified
 				if ( !db_lastModified.equals(fs_lastModified) ){
 					update = true;
 				}
 			} 
 		    else {
 				insert = true;
 			}
 			
 			// If we need to update or insert a row
 		    if (update || insert){			    	
 		    	Pipefile pipe = ServerUtils.parseXML(file);
 		    	pipe.lastModified = fs_lastModified;
 				
 				if (insert){
 					Database.insertPipefile(root.dirId, pipe);
 				} else {
 					pipe.fileId = Database.selectPipefileId(file.getAbsolutePath());
 					Database.updatePipefile(pipe);
 				}
  		    }
 		}
 	}
 	
 	private void updateMonitorAndAccessFile(Directory root) throws Exception{
 		ServerUtils.touchMonitorFile(root);
 		ServerUtils.writeAccessFile(root);
 	}
 	
 	/**
 	 *  Removes a file from the server
 	 *  @param filename absolute path of the file
 	 */
 	private void removeFile(Directory root, Pipefile pipe) throws Exception {		
 		File f = new File(pipe.absolutePath);
 		if (f.exists()){
 			// Delete file on file-system
 			boolean success = f.delete();
 			if (!success){
 				throw new Exception("Failed to remove file " + pipe.absolutePath);
 			}
 			
 			// Remove parent directory if it is empty
 			ServerUtils.removeEmptyDirectory(f.getParentFile());
 			
 			// Delete file from database
 			Database.deletePipefile(pipe);
 		}
 	}
 	
 	/**
 	 *  Copy a file from the server to the proper package
 	 *  @param filename absolute path of the file
 	 *  @param packageName absolute path of the package
 	 */
 	private void copyFile(Directory root, Pipefile pipe, String packageName) throws Exception {
 		copyOrMoveFile(root, pipe, packageName, true);
 	}
 	
 	/**
 	 *  Move a file to another package
 	 *  @param filename absolute path of the file = source path of file
 	 *  @param packageName is the name of the package as it appears in the Database in column PACKAGENAME
 	 *  @throws Exception 
 	 */
 	public void moveFile(Directory root, Pipefile pipe, String packageName) throws Exception{
 		copyOrMoveFile(root, pipe, packageName, false);
 	}
 	
 	private void copyOrMoveFile(Directory root, Pipefile pipe, String packageName, boolean copy) throws Exception{
 		// Source
 		String oldAbsolutePath = pipe.absolutePath;
 		File src = new File(oldAbsolutePath);
 		
 		// If the source does not exist
 		if (!src.exists()) {
 			throw new Exception("Soruce file does not exist");
 		}
 		
 		// Destination
 		String destPath = ServerUtils.newAbsolutePath(root.absolutePath, packageName, pipe.type, pipe.name);
 		File dest = new File(destPath);
 		
 		// Update the name if the filename had to be saved as something else
 		String filename = dest.getName().replaceAll(".pipe", "").replace("_", " ");
 		if (!pipe.name.equals(filename)){
 			pipe.name = filename;
 		}
 		
 		// If the destination directory does not exist, create it and necessary parent directories
 		File destDir = dest.getParentFile();
 		if (!destDir.exists()){
 			boolean success = destDir.mkdirs();
 			if (!success){
 				throw new Exception("Destination folders could not be created");
 			}
 		}
 		
 		// Copy or Move the file 
 		if (copy){
 			FileInputStream in = new FileInputStream(src);
 			FileOutputStream out = new FileOutputStream(dest);
 			
 			int length = 0;
 			byte[] buffer = new byte[8192];
 			while ((length = in.read(buffer)) != -1){
 				out.write(buffer, 0, length);
 			}
 			
 			in.close();
 			out.flush();
 			out.close();
 		} 
 		else {
 			boolean success = src.renameTo(dest);
 			if(!success) {
 				throw new Exception("File could not be moved");
 			}
 		}
 		
 		// Remove parent directory if it is empty
 		ServerUtils.removeEmptyDirectory(src.getParentFile());
 		
 		// Update Pipefile
 		pipe.packageName = packageName;
 		pipe.absolutePath = destPath;
 				
 		// Update XML
 		Document doc = ServerUtils.readXML(dest);
 		doc = ServerUtils.updateXML(doc, pipe);
 		ServerUtils.writeXML(dest, doc);
 			
 		// Update Database
 		pipe.lastModified = new Timestamp(dest.lastModified());
 		
 		if (copy) {
 			Database.insertPipefile(root.dirId, pipe);
 		} 
 		else {
 			Database.updatePipefile(pipe);
 		}
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Public Functions
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *  Returns the directory object for the absolutePath
 	 */
 	public Directory getDirectory(String absolutePath) throws Exception {
 		try {
 			File rootDir = new File(absolutePath);
 			if (rootDir.exists() && rootDir.isDirectory()){
				Directory root = new Directory();
				root.absolutePath = rootDir.getAbsolutePath();
 				
 				Timestamp monitorModified =  ServerUtils.getMonitorFileModified(root);
 				Timestamp accessModified =  ServerUtils.getAccessFileModified(root);
 				
 				// Insert if needed
				root = Database.selectDirectory(rootDir.getAbsolutePath());
				
 				boolean inserted = false;
 				if (root == null){
 					Database.insertDirectory(rootDir.getAbsolutePath(), monitorModified, accessModified);
 					root = Database.selectDirectory(rootDir.getAbsolutePath());
 					inserted = true;
 				}
 				
 				// Update the file in the database
 				if (inserted || monitorModified == null || !monitorModified.equals(root.monitorModified)){
 					root.monitorModified = monitorModified;
 					Database.updateDirectory(root);
 					updateDatabase(root);
 				}
 				
 				// Update access
 				if (accessModified != null && (inserted || !accessModified.equals(root.accessModified))){
 					ServerUtils.readAccessFile(root);
 				}
 				
 				return root;
 			} 
 			else {
 				return null;
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Returns an array of all the pipefiles
 	 */
 	public Pipefile[] getFiles(Directory root) throws Exception {
 		try {
 			return Database.selectPipefiles(root.dirId);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Returns an array of all the pipefiles that match the query
 	 */
 	public Pipefile[] getSearchResults(Directory root, String query) throws Exception{
 		try {
 			return Database.selectPipefilesSearch(root.dirId, query);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Updates the file on the server
 	 */
 	public void updateFile(Directory root, Pipefile pipe) throws Exception{
 		try {
 			File file = new File(pipe.absolutePath);
 			
 			// Update the XML
 			Document doc = ServerUtils.readXML(file);
 			doc = ServerUtils.updateXML(doc, pipe);
 			ServerUtils.writeXML(file, doc);
 			
 			// Update the filename if the name changed
 			if (pipe.nameUpdated || pipe.packageUpdated){
 				String destPath = ServerUtils.newAbsolutePath(root.absolutePath, pipe.packageName, pipe.type, pipe.name);
 				File dest = new File(destPath);
 				
 				// Update the name if the filename had to be saved as something else
 				String filename = dest.getName().replaceAll(".pipe", "").replace("_", " ");
 				if (!pipe.name.equals(filename)){
 					pipe.name = filename;
 				}
 				
 				// Create parent folders if needed
 				File destDir = dest.getParentFile();
 				if (!destDir.exists()){
 					boolean success = destDir.mkdirs();
 					if (!success){
 						throw new Exception("Destination folders could not be created");
 					}
 				}
 				
 				// Move the file
 				boolean success = file.renameTo(dest);
 				if(!success) {
 					throw new Exception("Failed to rename file");
 				}
 				
 				// Remove parent directory if it is empty
 				ServerUtils.removeEmptyDirectory(file.getParentFile());
 				
 				// Update file and absolutePath
 				file = dest;
 				pipe.absolutePath = destPath;
 			}
 			
 			// Update the database
 			pipe.lastModified = new Timestamp(file.lastModified());
 			Database.updatePipefile(pipe);
 			
 			// Update monitor and access files
 			updateMonitorAndAccessFile(root);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 
 	}
 	
 	/**
 	 *  Removes files from the server
 	 */
 	public void removeFiles(Directory root, Pipefile[] pipes) throws Exception {
 		try {
 			// Remove each file
 			for (Pipefile pipe : pipes) {
 				removeFile(root, pipe);
 			}
 			
 			// Update monitor and access files
 			updateMonitorAndAccessFile(root);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Copies files from the server to the proper package
 	 */
 	public void copyFiles(Directory root, Pipefile[] pipes, String packageName) throws Exception {		
 		try {
 			// Copy each file
 			for (Pipefile pipe : pipes) {
 				copyFile(root, pipe, packageName);
 			}
 
 			// Update monitor and access files
 			updateMonitorAndAccessFile(root);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Moves files from the server to the proper package
 	 */
 	public void moveFiles(Directory root, Pipefile[] pipes, String packageName) throws Exception{
 		try {
 			// Move each file
 			for (Pipefile pipe : pipes) {
 				moveFile(root, pipe, packageName);
 			}
 			
 			// Update monitor and access files
 			updateMonitorAndAccessFile(root);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Returns an array of all the groups
 	 */
 	public Group[] getGroups(Directory root) throws Exception {
 		try {
 			return Database.selectGroups(root.dirId);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Inserts or Updates a group on the server
 	 */
 	public void	updateGroup(Directory root, Group group) throws Exception{
 		try {			
 			// Insert or update the group
 			if (group.groupId == -1){
 				Database.insertGroup(root.dirId, group);
 			} else {
 				Database.updateGroup(group);
 			}
 			
 			// Write the access file
 			ServerUtils.writeAccessFile(root);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Deletes groups on the server
 	 */
 	public void	removeGroups(Directory root, Group[] groups) throws Exception{
 		try {			
 			// Delete each group
 			for (Group group: groups){
 				Database.deleteGroup(group);
 			}
 			
 			// Write the access file
 			ServerUtils.writeAccessFile(root);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 }
 
