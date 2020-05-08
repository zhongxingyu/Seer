 package justen;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 public class FileManager implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private HashSet<String> localFiles;
 	private HashSet<String> remoteFiles;
 	private HashSet<String> openFiles;
 	private HashMap<String, ArrayList<Integer>> versionMap;
 	private HashMap<String, Lock> lockMap;
 	
 	private final Object lock = new Object();
 	
 	public FileManager() {
 		localFiles = new HashSet<String>();
 		remoteFiles = new HashSet<String>();
 		openFiles = new HashSet<String>();
 		versionMap = new HashMap<String, ArrayList<Integer>>();
 		lockMap = new HashMap<String, Lock>();
 	}
 	
 	public HashSet<String> getLocalFiles() {
 		return localFiles;
 	}
 	
 	public HashSet<String> getRemoteFiles() {
 		return remoteFiles;
 	}
 	
 	public void openFile(String fileName) {
 		openFiles.add(fileName);
 	}
 	public boolean isFileOpen(String fileName) {
 		return openFiles.contains(fileName);
 	}
 	
 	public void closeFile(String fileName) {
 		openFiles.remove(fileName);
 	}
 	
 	public HashMap<String, ArrayList<Integer>> getVersionMap() {
 		return versionMap;
 	}
 	
 	public ArrayList<String> getLogicalView() {
 		ArrayList<String> allFiles = new ArrayList<String>();
 		for (Entry<String, ArrayList<Integer>> e : versionMap.entrySet()) {
 			String fileName = e.getKey();
 			ArrayList<Integer> temp = e.getValue();
 			String properName = fileName.substring(0, fileName.lastIndexOf("."));
 			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
 			for (int i = 0; i < temp.size(); i++) {
 				allFiles.add(properName + "_v" + i + "." + extension);
 			}
 		}
 		return allFiles;
 	}
 	
 	public HashMap<String, Lock> getLockMap() {
 		return lockMap;
 	}
 	
 	// filename contains version num
 	public void setLock(String fileName, Lock lock) {
 		lockMap.put(fileName, lock);
 	}
 	
 	/**
 	 * Returns type of lock on file. If null, no lock on file.
 	 * @param fileName
 	 * @return lock type
 	 */
 	public Lock getLockType(String fileName) {
 		if (lockMap.containsKey(fileName))
 			return lockMap.get(fileName);
 		
 		return null;
 	}
 	
 	public void addLocalFile(String fileName) { 
 		localFiles.add(fileName);
 		ArrayList<Integer> bitString = new ArrayList<Integer>();
 		bitString.add(1);
 		versionMap.put(fileName, bitString);
 	}
 	
 	public void processStatusUpdate(Status s) {
 		synchronized(lock) {
 			processVersionMap(s.fileVersionMap);
 		}
 	}
 	
 	private void processVersionMap(HashMap<String, ArrayList<Integer>> map) {
 		for (String file : map.keySet()) {
 			if (localFiles.contains(file) || remoteFiles.contains(file)) {
 				// we have the file, check versions
 				ArrayList<Integer> remoteBitString = map.get(file);
 				ArrayList<Integer> localBitString = versionMap.get(file);
 				if (remoteBitString.size() > localBitString.size()) {
 					// new version
 					int j = localBitString.size();
 					localBitString.ensureCapacity(remoteBitString.size());
 					for (int i = j; i < localBitString.size(); i++) {
 						localBitString.set(i, 0);
 					}
 					versionMap.put(file, localBitString);
 				}
 			}
 			else
 			{
 				remoteFiles.add(file);
 				versionMap.put(file, map.get(file));
 			}
 		}
 	}
 	
 	public boolean addLocalFileVersion(String fileName, int versionNum) {
 		if (!versionMap.containsKey(fileName))
 			return false;
 
 		ArrayList<Integer> temp = versionMap.get(fileName);
 		if (temp.size() >= versionNum)
 			temp.set(versionNum, 1);
 		else // higher version num
 			temp.add(1); // assume that there is time between new file saves
 		versionMap.put(fileName, temp);
 		
 		return true;
 	}
 	
 	public boolean removeLocalFile(String fileName, boolean allVersions) {
 		if (!localFiles.contains(fileName))
 			return false;
 		if (allVersions) {
 			localFiles.remove(fileName);
 //			for (int i = 0; i < versionMap.get(fileName).size(); i++)
 //				versionMap.get(fileName).set(i, 0);
 			versionMap.remove(fileName);
 			return true;
 		}
 		else {
 			//fileName = justen_stable_v1.docx
 			int v = fileName.lastIndexOf("_") + 2; // 13
 			String vNum = fileName.substring(v, fileName.lastIndexOf("."));
 			int versionNumber = Integer.parseInt(vNum);
 			if (versionMap.get(fileName).size() >= versionNumber)
 				versionMap.get(fileName).set(versionNumber, 0);
 			
 			boolean allGone = true;
 			for (int i = 0; i < versionMap.get(fileName).size(); i++) {
 				if (versionMap.get(fileName).get(i) == 1)
 				{
 					allGone = false;
 					break;
 				}
 			}
 			if (allGone)
 				localFiles.remove(fileName);
 			return true;
 		}
 	}
 	
	public boolean fileExists(String filename) {
		return versionMap.containsKey(filename);
 	}
 	
 	public void addRemoteFile(String fileName) {
 		remoteFiles.add(fileName);
 	}
 	
 	public boolean containsFileLocally(String fileName, int versionNum) {
 		if (!versionMap.containsKey(fileName))
 			return false;
 		else
 		{
 			ArrayList<Integer> temp = versionMap.get(fileName);
 			if (versionNum > temp.size())
 				return false;
 			return (temp.get(versionNum) == 1);
 		}
 	}
 	
 	public boolean containsFileRemotely(String filename) {
 		return remoteFiles.contains(filename);
 	}
 }
