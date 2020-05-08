 package dfs;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.BufferOverflowException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import virtualdisk.VirtualDisk;
 
 import common.Constants;
 
 import dblockcache.DBuffer;
 import dblockcache.DBufferCache;
 
 public abstract class DFS {
 
 	private boolean myFormat;
 	private String myVolName;
 	private ArrayList<Integer> myFreeINodes;
 	private ArrayList<Integer> myFreeBlocks;
 	private ArrayList<Integer> myDFileList;// partition at 512
 	private DBufferCache myDBCache;
 
 	DFS(String volName, boolean format) {
 		myVolName = volName;
 		myFormat = format;
 		try {
 			myDBCache = new DBufferCache(Constants.CACHE_SIZE, new VirtualDisk(
 					myVolName, format));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}// this must be wrong since need VDF to be maintained session to
 			// session?
		initializeMData();
 	}
 
 	DFS(boolean format) {
 		this(Constants.vdiskName, format);
 	}
 
 	DFS() {
 		this(Constants.vdiskName, false);
 	}
 
 	/*
 	 * If format is true, the system should format the underlying disk contents,
 	 * i.e., initialize to empty. On success returns true, else return false.
 	 */
 
 	public boolean format() {
 		return false; // Don't think this is necessary with the VirtualDisk
 						// implementation that formatstores at bootup.
 	}
 
 	// scan VDF to form the metadata stuctures
 	private void initializeMData() {
 		formatFreeBlockList(); // set free block list to all free blocks
 
 		for (int i = 0; i < Constants.NUM_OF_INODES; i++) {
 			ArrayList<Integer> iNodeInfo = parseINode(i);
 			if (iNodeInfo.get(0) == 0) {
 				myFreeINodes.add(i);
 			} else {
 				myDFileList.add(i);
 				for (int blockID = 1; blockID < iNodeInfo.size(); blockID++) {
 					if (iNodeInfo.get(blockID) != 0)
 						myFreeBlocks.remove(blockID);
 				}
 			}
 		}
 
 	}
 
 	private void formatFreeBlockList() {
 		for (int i = Constants.NUM_OF_INODES + 1; i < Constants.NUM_OF_BLOCKS; i++) {
 			myFreeBlocks.add(i);
 		}
 	}
 
 	private ArrayList<Integer> parseINode(int dFID) {
 		byte[] buffer = new byte[Constants.BLOCK_SIZE];
 		ArrayList<Integer> parsedINode = new ArrayList<Integer>();
 
 		DBuffer blockToParse = myDBCache.getBlock(dFID);
 		blockToParse.read(buffer, 0, Constants.BLOCK_SIZE);
 		blockToParse.release();
 		for (int loc = 0; loc < Constants.BLOCK_SIZE; loc += 4)
 			parsedINode.add(ByteBuffer.wrap(
 					Arrays.copyOfRange(buffer, loc, loc + 4)).getInt());
 
 		return parsedINode;
 	}
 
 	/*
 	 * creates a new DFile and returns the DFileID, which is useful to uniquely
 	 * identify the DFile
 	 */
 	public synchronized int createDFile() {
 		sortMetadata();
 		int DFileID = myFreeINodes.get(0);
 		myFreeINodes.remove(0);
 		return DFileID;
 	}
 
 	/* destroys the file specified by the DFileID */
 	public void destroyDFile(int dFID) {
 		ArrayList<Integer> iNodeInfo = parseINode(dFID);
 
 		formatBlock(dFID);
 		myFreeINodes.remove(myFreeINodes.indexOf(dFID));
 		myDFileList.remove(myDFileList.indexOf(dFID));
 
 		for (int i = 1; i < iNodeInfo.size(); i++) {
 			myFreeBlocks.remove(myFreeBlocks.indexOf(iNodeInfo.get(i)));
 			formatBlock(iNodeInfo.get(i));
 		}
 	}
 
 	/* Zeros out a block */
 	private void formatBlock(int dFID) {
 		byte[] zeroBuffer = new byte[Constants.BLOCK_SIZE];
 		for (byte b : zeroBuffer) {
 			zeroBuffer[b] = 0;
 		}
 		DBuffer iNodeToKill = myDBCache.getBlock(dFID);
 		iNodeToKill.write(zeroBuffer, 0, Constants.BLOCK_SIZE);
 		iNodeToKill.release();
 	}
 
 	/*
 	 * reads the file dfile named by DFileID into the buffer starting from the
 	 * buffer offset startOffset; at most count bytes are transferred
 	 */
 	public int read(int dFID, byte[] buffer, int startOffset, int count) {
 		ArrayList<Integer> iNodeInfo = parseINode(dFID);
 		int bytesRead = 0;
 
 		for (int i = 1; i < iNodeInfo.size(); i++) {
			DBuffer toRead = myDBCache.getBlock(iNodeInfo.get(1));
 			bytesRead += toRead.read(buffer, startOffset + bytesRead, count
 					- bytesRead);
 			toRead.release();
 			if (bytesRead >= count)
 				break;
 		}
 		return bytesRead;
 	}
 
 	/*
 	 * writes to the file specified by DFileID from the buffer starting from the
 	 * buffer offset startOffsetl at most count bytes are transferred
 	 */
 	public int write(int dFID, byte[] buffer, int startOffset, int count) {
 		sortMetadata();
 		if(myFreeBlocks.size()==0){
 			System.out.println("VOLUME SIZE EXCEEDED");
 			return -1;
 		}
 		ArrayList<Integer> iNodeInfo = parseINode(dFID);
 		int bytesWritten = 0;
 		int vBlockNumber = 1;
 		while (bytesWritten < count
 				&& buffer.length > (startOffset + bytesWritten)) {
 			DBuffer nextBlock;
 			if (vBlockNumber < iNodeInfo.size() - 1) {
 				nextBlock = myDBCache.getBlock(iNodeInfo.get(vBlockNumber));
 				vBlockNumber++;
 			} else {
 					DBuffer iNode = myDBCache.getBlock(dFID);
 					ByteBuffer b = ByteBuffer.allocate(Constants.BLOCK_SIZE);
 					int nextPBlockNumber;
 					synchronized (this) {
 						nextPBlockNumber = myFreeBlocks.get(0);
 						myFreeBlocks.remove(0);
 					}
 					for(int i:iNodeInfo)
 						b.putInt(i);
 					try{
 					b.putInt(nextPBlockNumber);}
 					catch(BufferOverflowException e){
 						System.out.println("MAX FILE SIZE EXCEEDED");
 						return -1;
 					}
 					iNode.write(b.array(), 0, Constants.BLOCK_SIZE);
 					iNode.release();
 					nextBlock=myDBCache.getBlock(nextPBlockNumber);
 			}
 			bytesWritten+=nextBlock.write(buffer, startOffset+bytesWritten, count-bytesWritten);
 			nextBlock.release();
 		}
 		return count;
 	}
 
 	/* returns the size in bytes of the file indicated by DFileID. */
 	public int sizeDFile(int dFID) {
 		ArrayList<Integer> iNodeInfo = parseINode(dFID);
 		return iNodeInfo.get(0);
 	}
 
 	/*
 	 * List all the existing DFileIDs in the volume
 	 */
 	public List<Integer> listAllDFiles() {
 		sortMetadata();
 		return myDFileList;
 	}
 
 	public void sync() {
 		myDBCache.sync();
 	}
 
 	private void sortMetadata() {
 		Collections.sort(myDFileList);
 		Collections.sort(myFreeINodes);
 		Collections.sort(myFreeBlocks);
 	}
 
 }
