 import java.util.Arrays;
 
 /**
  * Make calls on a Memory Manager. This is the high level interface to the
  * Memory Manager that is closer to the client-side aspect of the project.
  * 
  * @author Tyler Kahn
  * @author Reese Moore
  * @version 12.09.2011
  */
 public class Executor {
 	// Private Variables
 	private MemoryManager mm;
 	private Handle[] handleArray;
 	private int numRecs;
 	private byte[] byteBuffer;
 	
 	/**
 	 * Allocate a new Executor.
 	 * @param mm The Memory Manager to make calls to.
 	 * @param numRecs The number of records we have.
 	 */
 	public Executor(MemoryManager mm, int numRecs) {
 		this.mm = mm;
 		this.numRecs = numRecs;
 		handleArray = new Handle[numRecs];
 		byteBuffer = new byte[256];
 	}
 	
 	/**
 	 * Insert a new Record into Memory.
 	 * @param recNum The number of the record.
 	 * @param x The X coordinate of the city.
 	 * @param y The Y coordinate of the city.
 	 * @param name The Name of the city.
 	 */
 	public void insert(Integer recNum, Integer x, Integer y, String name) {
 		// Get a record and convert it to a byte stream.
 		Record r = new Record(name, x, y);
 		byte[] recordBytes = r.toBytes();
 		
 		// Remove the old handle if we need to.
 		Handle oldHandle = handleArray[recNum];
 		if (oldHandle != null) {
 			mm.remove(oldHandle);
 		}
 		
 		// The new handle we are inserting.
 		Handle handle = mm.insert(recordBytes, recordBytes.length);
 		
 		// Make sure it's not an error.
 		if (handle == Handle.ERROR_HANDLE) {
			System.out.println(Errors.CannotAllocateMem);
 			return;
 		}
 		
 		// If we've gotten here, save the handle.
 		handleArray[recNum] = handle;
 	}
 	
 	/**
 	 * Remove a record from Memory.
 	 * @param recNum The record number to remove.
 	 */
 	public void remove(Integer recNum) {
 		// Error Checking.
 		if (recNum < 0 || recNum >= numRecs) {
			System.out.println(Errors.RecnumOutOfBounds);
 			return;
 		}
 		if (handleArray[recNum] == null) {
			System.out.println(Errors.HandleNotInArray);
 			return;
 		} 
 		
 		// Pass removal down to the memory manager.
 		Handle handle = handleArray[recNum];
 		mm.remove(handle);
 		handleArray[recNum] = null;
 	}
 	
 	/**
 	 * Print the information about the Record specified by recNum.
 	 * @param recNum The record number to retrieve information about.
 	 */
 	public void print(Integer recNum) {
 		Handle handle = handleArray[recNum];
 		if (handle == null) {
 			System.out.println("No Record at " + recNum);
 			return;
 		}
 		
 		// Copy the data into our byteBuffer
 		int bytesReturned = mm.get(handle, byteBuffer, byteBuffer.length);
 		
 		// Truncate it and restore the record.
 		Record rec = Record.fromBytes(Arrays.copyOf(byteBuffer, bytesReturned));
 		
 		// Print the record
 		System.out.println(rec);
 	}
 	
 	/**
 	 * Print information about all records, as well as the underlying 
 	 * FreeBlockList.
 	 */
 	public void print() {
 		for (int i = 0; i < numRecs; i++) {
 			if (handleArray[i] != null) {
 				String str = "[" + handleArray[i].getOffset() + "] --> " + i + ": ";
 				System.out.print(str);
 			}
 			print(i);
 		}
 		
 		mm.dump();
 	}
 }
