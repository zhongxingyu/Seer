 package heapManagement;
 
 import hashManagement.HashIndex;
 import hashManagement.HashIndexRecord;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import datatype.DataType;
 
 public class Heap {
 	String fileName;
 	RandomAccessFile randomAccessFile;
 	int writeOffset, readOffset;
 	HeapHeader head;
 	DataType[] datatype;
 	ArrayList<Integer> hashColumns;
 	HashMap<Integer, HashIndex> indices;
 
 	public Heap(String fileName, DataType[] datatypes)  {
 		this.fileName = fileName;
 		this.writeOffset = 0;
 		this.readOffset = 0;
 		this.datatype = datatypes;
 		this.hashColumns = getHashFiles();
 		this.indices = null;
 	}
 
 	public boolean insertInHeap(String record, long offset) throws IOException {
 		StringTokenizer stringTok = new StringTokenizer(record, ",");
 		ArrayList<Attribute> attributeList = head.getAttributeList();
 
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		randomAccessFile.seek(offset);
 
 		try {
 			for(int i = 0; i < attributeList.size(); i++) {
 				if(stringTok.hasMoreTokens()) {
 					ByteArrayOutputStream attribute = new ByteArrayOutputStream();
 					String nextTok = stringTok.nextToken();
 					int attributeCode = Utilities.getIntDatatypeCode(attributeList.get(i).getType(),attributeList.get(i).getSize());
 					datatype[attributeCode].write(byteArrayOutputStream, nextTok, attributeList.get(i).getSize());
 					datatype[attributeCode].write(attribute, nextTok, attributeList.get(i).getSize());
 					long rid = offset;
 					Integer column = new Integer(i+1);
 					if (this.indices.containsKey(column)) {
 						HashIndexRecord toInsert = new HashIndexRecord(rid, attribute.toByteArray());
 						indices.get(column).insertInhashIndex(toInsert);
 					}
 				} else {
 					System.out.println("Invalid record..");
 				}
 			}
 			randomAccessFile.write(byteArrayOutputStream.toByteArray());
 		} catch (IOException e) {
 			return false;
 		}	
 		return true;
 	}
 
 	public HashMap<Integer, HashIndex> getIndices() {
 		return indices;
 	}
 	
 	public void setIndices() {
 		this.indices = new HashMap<Integer, HashIndex>();
 		ArrayList<Attribute> attributeList = head.getAttributeList();
 
 		for (Integer i : this.hashColumns) {
 			String iFileName = this.fileName + "." + i.toString() + ".lht";
 			String oFileName = this.fileName + "." + i.toString() + ".lho";
 			Attribute indexOn = attributeList.get(i.intValue()-1);
 			String attributeSchema = new String(indexOn.getType() + new Integer(indexOn.getSize()).toString());
 			HashIndex newIndex;
 			try {
 				newIndex = new HashIndex(iFileName, oFileName, attributeSchema, this.datatype);
 				indices.put(i, newIndex);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void openFile() throws FileNotFoundException {
 		this.randomAccessFile = new RandomAccessFile(new File(this.fileName), "rw");
 	}
 
 	public boolean closeFile(){
 		try {
 			randomAccessFile.close();
 			Set<Integer> keys = this.indices.keySet();
 			for (Integer i : keys)
 				this.indices.get(i).closeHeap();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public boolean doesHeapFileExist() {
 		File f = new File(fileName);
 		return f.exists();
 	}
 
 	public void buildNewIndices(ArrayList<Integer> newBuilds) throws Exception {
 		this.getHeapHeader();
 
 		ArrayList<Attribute> attributeList = this.head.getAttributeList();	
 		HashMap<Integer, HashIndex> newIndices = new HashMap<Integer, HashIndex>();
 
 		for (Integer i : newBuilds) {
			String iFileName = this.fileName + "." + i.toString() + ".lht";
			String oFileName = this.fileName + "." + i.toString() + ".lho";
 			Attribute indexOn = attributeList.get(i.intValue()-1);
 			String attributeSchema = new String(indexOn.getType() + new Integer(indexOn.getSize()).toString());
 			HashIndex newIndex = new HashIndex(iFileName, oFileName, attributeSchema, this.datatype);
 			newIndices.put(i, newIndex);
 		}
 
 		long offset = (long) this.head.getHeaderSize();
 		for(int i = 0 ; i < this.head.getTotalRecords(); i++) {
 			randomAccessFile.seek(offset);
 			byte[] buf = new byte[this.head.getSizeOfRecord()];
 			randomAccessFile.read(buf);
 			offset += this.head.getSizeOfRecord();	
 			int size = 0;
 			for(int j = 0; j < attributeList.size(); j++) {
 				byte[] temp = Arrays.copyOfRange(buf, size, size+attributeList.get(j).getSize());
 				size +=  attributeList.get(j).getSize();
 				long rid = offset;
 				Integer column = new Integer(j+1);
 				if (newIndices.containsKey(column)) {
 					HashIndexRecord toInsert = new HashIndexRecord(rid, temp);
 					newIndices.get(column).insertInhashIndex(toInsert);
 				}
 			}
 		}
 
 		Set<Integer> keys = newIndices.keySet();
 		for (Integer i : keys)
 			newIndices.get(i).closeHeap();
 	}
 
 	public HeapHeader getHeapHeader() throws Exception {
 		//retrieve header and advance the pointer
 		/*
 		 * Format of the header:
 		 *   1st 4 bytes - Size of the header
 		 *   2nd 4 bytes - No of Attributes
 		 *   3rd 4 bytes - Size of one record
 		 *   4th 8 bytes - Total number of Records
 		 *   5th rest of the bytes - Schema
 		 */
 		randomAccessFile.seek(0);
 		byte[] headSize = new byte[4];
 		randomAccessFile.read(headSize);
 		this.head = new HeapHeader();
 		int headIntSize = Utilities.toInt(headSize);
 		byte[] head = new byte[headIntSize];
 		randomAccessFile.seek(0);
 		randomAccessFile.read(head);
 
 
 		byte[] temp = Arrays.copyOfRange(head, 0, 4);
 		this.head.setHeaderSize(Utilities.toInt(temp));
 
 		temp = Arrays.copyOfRange(head, 4, 8);
 		this.head.setNoOfCol(Utilities.toInt(temp));
 
 		temp = Arrays.copyOfRange(head, 8, 12);
 		this.head.setSizeOfRecord(Utilities.toInt(temp));
 
 		temp = Arrays.copyOfRange(head, 12, 20);
 		this.head.setTotalRecords(Utilities.toLong(temp));
 
 		temp = Arrays.copyOfRange(head, 20, head.length);
 		this.head.setSchema(new String(temp));
 
 		return this.head;
 	}
 
 	public void makeHeapHeader(String header, long totalRecords) {
 		//String encodedSchema = new String();
 		StringTokenizer stringTok = new StringTokenizer(header, ",");
 
 		int recordSize = 0, attributes = 0;
 		while(stringTok.hasMoreTokens()){
 			attributes++;
 			String nextTok = stringTok.nextToken();
 			int size = Integer.parseInt(nextTok.substring(1));
 			//encodedSchema = encodedSchema + encodeToSchema(nextTok.charAt(0), size);
 			recordSize += size;
 		}
 
 		this.head = new HeapHeader();
 		//head.setEncodedSchema(encodedSchema);
 		head.setSchema(header);
 		head.setNoOfCol(attributes);
 		head.setSizeOfRecord(recordSize);
 		head.setTotalRecords(totalRecords);
 		head.setHeaderSize(20+head.getSchema().length());
 
 		/*
 		 * Format of the header:
 		 *   1st 4 bytes - Size of the header
 		 *   2nd 4 bytes - No of Attributes
 		 *   3rd 4 bytes - Size of one record
 		 *   4th 8 bytes - Total number of Records
 		 *   5th rest of the bytes - Schema
 		 */
 		try {
 			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 			byteArrayOutputStream.write(Utilities.toByta(head.getHeaderSize()));
 			byteArrayOutputStream.write(Utilities.toByta(head.getNoOfCol()));
 			byteArrayOutputStream.write(Utilities.toByta(head.getSizeOfRecord()));
 			byteArrayOutputStream.write(Utilities.toByta(head.getTotalRecords()));
 			byteArrayOutputStream.write(head.getSchema().getBytes());
 			randomAccessFile.seek(0);
 			randomAccessFile.write(byteArrayOutputStream.toByteArray());			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public ArrayList<Integer> getHashColumns() {
 		return hashColumns;
 	}
 	
 	public byte[] retrieveRecordFromHeap(long rid) throws IOException{
 		randomAccessFile.seek(rid);
 		byte[] buf = new byte[this.head.getSizeOfRecord()];
 		randomAccessFile.read(buf);
 		return buf;
 	}
 
 	private ArrayList<Integer> getHashFiles() {
 		ArrayList<Integer> existingHashFiles = new ArrayList<Integer>();
 		String[] children = new File(".").list();
 		for (String child : children) {
 			/* Only adding index file. Overflow must exist */
 			if (child.matches(this.fileName + ".[0-9]*.lht"))  {
 				String indexNumber = child.replace(this.fileName + ".", "").replace(".lht", "");
 				Integer index = new Integer(indexNumber);
 				existingHashFiles.add(index);
 			}
 		}
 		return existingHashFiles;
 	}
 }
