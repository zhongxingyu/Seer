 package aether.repl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import aether.io.Chunk;
 
 /**
  * Stores the metadata of what chunks of which file 
  * are stored at the current node in the cluster. 
  * This is called when we get confirmation from 
  * destination that the chunk has been received.
  * */
 class FileChunkMetadata {
 	private static FileChunkMetadata fcm;
 	Map<String, ArrayList<ChunkMetadata>> fileChunkMap;
 	public static synchronized FileChunkMetadata getInstance () {
 		if (fcm == null) {
 			fcm = new FileChunkMetadata ();
 		}
 		return fcm;
 	}
 	
 	public  FileChunkMetadata () {
 		fileChunkMap = new ConcurrentHashMap<String, ArrayList<ChunkMetadata>> ();
 	}
 	
 	public ArrayList<String> getFileList () {
 		ArrayList<String> fileList = new ArrayList<String> ();
 		
 		for (String entry : fileChunkMap.keySet()) {
 			fileList.add(entry);
 		}
 		
 		return fileList;
 	}
 	public synchronized void addChunk (String f, ChunkMetadata c) {
 		ArrayList<ChunkMetadata> chunkList = fileChunkMap.get(f);
 		//Set<Map.Entry<String, ArrayList<ChunkMetadata>>> entry = this.getFileChunkMetadataSet();
 		//Iterator iter = entry.iterator();
 		
 		//for () {
 			//System.out.println(iter.next());
 		//}
 		if (chunkList == null) {
 			chunkList = new ArrayList<ChunkMetadata> ();
 		}
 		chunkList.add(c);
 		fileChunkMap.put(f, chunkList);
 	}
 	
 	public synchronized void removeChunk (String f,Chunk c) {
 		ArrayList<ChunkMetadata> chunkList = fileChunkMap.get(f);
 		chunkList.remove(c);
 	}
 	
 	public synchronized void removeFile (String f) {		
 		fileChunkMap.remove(f);
 	}	
 	public synchronized boolean doesChunkExists (String f) {
 		return fileChunkMap.containsKey(f);
 	}
 	
 	/**
 	 * This function gets the chunks ids present at a given node
 	 * for the particular file.
 	 * */
 	public synchronized Integer[] getChunkIds (String f) {
 		ArrayList<ChunkMetadata> cList = fileChunkMap.get(f);
 		ArrayList<Integer> chunkIds = new ArrayList<Integer> ();
 		Integer[] arrList = new Integer[cList.size()];
 		int counter = 0;
 		for (Iterator iter = cList.iterator(); iter.hasNext() == true; ) {
 			ChunkMetadata cm = (ChunkMetadata) iter.next();
			arrList[counter] = cm.getChunkId();
 			//chunkIds.add(cm.getChunkId());
 		}
 		
 		return arrList;
 	}
 	
 	/*Returns the contents of the hash map as a set. Useful 
 	 * if someone wants to iterate over the elements of the hash 
 	 * map as a set and perform operations. Used in heartbeat 
 	 * functionality.
 	 * 
 	 * **/
 	public synchronized Set<Map.Entry<String, ArrayList<ChunkMetadata>>> getFileChunkMetadataSet () {
 		return fileChunkMap.entrySet();
 	}
 	
 	/**
 	 * This function retrieves the chunk using the metadata hashmap.
 	 * It searches in the arraylist that is returns if the required 
 	 * chunk is found. If the chunk is found, read the chunk from the 
 	 * disk and return it. If not, then return null.
 	 * */
 	public synchronized Chunk retrieveChunk (String f, String c) {
 		ArrayList<ChunkMetadata> chunkList = fileChunkMap.get(f);
 		for (Iterator iter = chunkList.iterator(); iter.hasNext() == true; ) {
 			ChunkMetadata ch = (ChunkMetadata) iter.next();
 			if (ch.getChunkName().compareTo(c) == 0) {
 				Chunk chunk = readChunkFromFile (c);
 				return chunk;
 			}
 		}
 		return null;
 		
 		
 	}
 	
 	/**
 	 * This function actually reads the chunk written as a file
 	 * into the chunk object. It then returns the chunk object.
 	 * Returns null if the chunk object cannot for created.
 	 *  
 	 * */
 	public synchronized Chunk readChunkFromFile (String c) {
 		FileInputStream fis;
 		Chunk chunk = null;
 		try {
 			//FileOutputStream fos = new FileOutputStream (new File ("test.txt"));
 			fis = new FileInputStream (new File(c));
 			ObjectInputStream ois = new ObjectInputStream (fis);
 			chunk = (Chunk) ois.readObject();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return chunk;
 	}
 }
