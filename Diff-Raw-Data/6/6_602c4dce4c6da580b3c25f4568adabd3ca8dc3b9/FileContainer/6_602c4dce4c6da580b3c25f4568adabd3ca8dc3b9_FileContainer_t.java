 // Kyle Geib - Program 4 - CSS434 Fall 2012 - Dr Fukuda - December 13th 2012
 
 package main;
 
 import java.util.Vector;
 
 /**Class holds file caching information for DFSServer.
  * This includes data such as a file name, the IPs of client readers, the
  * owner, the state of the file, and the data (FileContents).
  * @author Kyle Geib
  */
 public class FileContainer {
 	
 	/**Enumerator to track the different states of this FileContainer.*/
 	public enum FileState {
 		Not_Shared, Read_Shared, Write_Shared, Ownership_Change
 	}
 	
 	
 	// Important data for this file. Made public to avoid getters and setters.
 	public String fileName;
 	public Vector<String> readers;	// List of current readers.
 	public String owner;
 	public FileState fileState;
 	public FileContents data;
 	
 	
 	/**Constructor - Create a new file for the DFSServer cache with only name.
 	 * @param fileName
 	 */
 	public FileContainer(String fileName) {
 		this.fileName = fileName;
 		readers = new Vector<String>();
 		owner = "";
 		fileState = FileState.Not_Shared;
 		data = new FileContents(new byte[0]); // Start with size-of-zero array
 	}
 	
 	
 	/**Constructor - Create a new file for the DFSServer's cache.
 	 * @param fileName
 	 * @param clientIP
 	 * @param mode
 	 */
 	public FileContainer(String fileName, String clientIP, String mode) {
 		this.fileName = fileName;
 		readers = new Vector<String>();
		owner = "";
 		
 		if (mode.equals("w")) {	// Write mode.
 			
 			owner = clientIP;	// If writing, then this creator is the owner
 			fileState = FileState.Write_Shared;	// Write
 			
 		} else {				// Read mode.
 			
 			safeAddReader(clientIP);	// Add creating client to the readers list
 			fileState = FileState.Read_Shared;	// Read	
 			
 		}
 		
 		data = new FileContents(new byte[0]); // Start with size-of-zero array
 	}
 	
 	
 	/**Add a clientIP as a reader for this file without inserting repeats.
 	 * @param clientIP The new client wanting to read this file.
 	 */
 	public void safeAddReader(String clientIP) {
 		if (!readers.contains(clientIP))
 			readers.add(clientIP);
 	}
 	
 	
 	/**Remove a clientIP from the readers list. This is important to do
 	 * to avoid mistakingly invalidating its files later on. Reader-ship is
 	 * lost when it changes files or becomes the owner/writer of the file.
 	 * @param clientIP The client
 	 */
 	public void removeReader(String clientIP) {
 		int index = readers.indexOf(clientIP);
 		if (index != -1)
 			readers.remove(index);
		
		// If all readers and owner are removed...
		if (readers.size() == 0 && owner.equals(""))
			fileState = FileState.Not_Shared;
 	}
 	
 	
 	/**Prints out the contents of the file's readers to the console.
 	 */
 	public void reportReaders() {
 		if (!owner.equals("")) 
 			System.out.println("owner: " + owner);
 		
 		System.out.println("# of readers: " + readers.size());
 		for (int x = 0; x < readers.size(); ++x) {
 			System.out.println("    reader = " + readers.elementAt(x));
 		}
 	}
 }
