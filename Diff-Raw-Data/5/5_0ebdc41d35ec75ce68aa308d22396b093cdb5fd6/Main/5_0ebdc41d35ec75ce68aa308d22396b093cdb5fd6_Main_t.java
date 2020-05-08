 package heapManagement;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import datatype.*;
 
 public class Main {
 
 	public static final DataType[] datatype = {
 		new Integer1Byte(), 
 		new Integer2Byte(), 
 		new Integer4Byte(), 
 		new Integer8Byte(),
 		new Real4Byte(),
 		new Real8Byte(),
 		new CharacterXByte()
 	};
 
 	public static void main(String args[]) {
 		if (args.length < 1 ) {
 			System.out.println("Usage: heapfile.hf -i with CSV file in standard input, -bn where n is a " +
 					"column to build a linear hash index on that column or heapfile -sn op " + 
 			"constant | -pn for any n columns in heap file");
 			System.exit(1);
 		}
 
 		String heapFile = args[0];
 
 		if (!heapFile.endsWith(".hf")) {
 			System.out.println("Heapfile must end with a .hf suffix");
 			System.exit(1);
 		}
 
 		boolean shouldInsert = false;
 		ArrayList<Integer> buildList = new ArrayList<Integer>();
 		for (String arg : args) {
 			if (arg.matches("-i"))
 				shouldInsert = true;
 			else if (arg.matches("-b[0-9]*"))
 				buildList.add(new Integer(arg.substring(2)));
 		}
 
 		Heap heap = new Heap(heapFile, datatype);
 		
 		if (shouldInsert || !buildList.isEmpty()) {
 			ArrayList<Integer> hashFiles = heap.getHashColumns();
 
 			if (heap.doesHeapFileExist()) {
 				ArrayList<Integer> newBuilds = new ArrayList<Integer>();
 				/* For every index in buildList and not in existing hashFiles, create and update */
 				for (Integer newIndex : buildList)
 					if (!hashFiles.contains(newIndex)) {
 						newBuilds.add(newIndex);
 						hashFiles.add(newIndex);
 					}
 				buildNewIndices(heap, newBuilds);
 			}
 
 			/* 
 			 * If no shouldInsert, only need to build. Already did that above.
 			 * Otherwise, insert and update heap and hash at same time. heap.hashColumns contains
 			 * all old indices AND newly built indices.
 			 */
 			if (shouldInsert)
 				insertRecords(heap);
 		}		
 		else {
 			String[] conditions = new String[args.length-1];
 			for (int i = 1; i < args.length; i++)
 				conditions[i-1] = args[i];
 			queryRecordsInHeap(heap, conditions);
 		}
 	}
 
 	private static ArrayList<String> getRecordsFromCSV() {
 		CSVFileReader cfr = new CSVFileReader(System.in);
 		cfr.ReadFile();
 		return cfr.getStoreValues();
 	}
 
 	private static void buildNewIndices(Heap heap, ArrayList<Integer> newBuilds) {
 		if (newBuilds.isEmpty())
 			return;
 		
 		try {
 			heap.openFile();
 			heap.buildNewIndices(newBuilds);
 			heap.closeFile();
 		} catch (FileNotFoundException e) {
 			System.out.println ("Directory, not a file");
 			System.exit(1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void queryRecordsInHeap(Heap heap, String[] conditions) {
 		if (heap.doesHeapFileExist()) {
 			try {
 				heap.openFile();
 				heap.getHeapHeader();
 				heap.setIndices();
 			} catch (FileNotFoundException e) {
 				System.out.println ("Directory, not a file");
 			} catch (Exception e) {
 				System.out.println("Invalid heap file format");
 			}
 		}
 		else {
 			System.out.println("Heapfile " + heap.fileName + " not found. ");
 			System.exit(1);
 		}
 
 		try {
 			Cursor cursor = new Cursor(heap, conditions, datatype);
 			String record;
 			/* Header */
 			System.out.println(cursor.getHeader());
 			while ((record = cursor.getNextRecord()) != null)
 				System.out.println(record);
 			heap.closeFile();
 		} catch (Exception e) {
 			System.out.println("Invalid argument in list of selections and projections");
 			System.exit(1);
 		}
 
 	}
 	
 	/*
 	 * This function will extract the records from the CSV file and insert it into the heap file
 	 * If heap file does not exist it will create one, otherwise just append at the end of existing file
 	 */
 	private static void insertRecords(Heap heapFile) {
 		ArrayList<String> records = getRecordsFromCSV();
 		System.out.println("Extracted records from the CSV file");
 		/* 
 		 * Set the indices inside Heap to be the full list of hashcolumns, if any
 		 * This creates the HashIndex classes and opens those files if they exist
 		 */
 		try{
 			String originalCSVHeader = records.get(0);
 
 			ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
 			attributeList = Utilities.makeAttributeList(originalCSVHeader);
 			String encodedCSVHeader = Utilities.formatHeaderSchema(attributeList);
 			//1st check if the heap file already exists
 			if(!heapFile.doesHeapFileExist()){
 				heapFile.openFile();
 				int count = 0;
 				long offset = 0;
 				for(String record : records){
 					if(count++ == 0) {
 						heapFile.makeHeapHeader(record,records.size()-1); // make the header and insert it
						heapFile.setIndices();
 						offset = heapFile.head.getHeaderSize();
 					} else {
 						heapFile.insertInHeap(record, offset);	
 						offset += heapFile.head.getSizeOfRecord();
 					}	
 				}
 				heapFile.closeFile();
 			} else { //append to existing file
 				heapFile.openFile();
				heapFile.setIndices();
 				//compare heap header for validation
 				HeapHeader heapHeader = heapFile.getHeapHeader();
 				if(encodedCSVHeader.compareToIgnoreCase(Utilities.formatHeaderSchema(heapHeader.getAttributeList())) == 0){
 					//schema matched, append the records
 					int  count = 0;
 					long offset = heapHeader.getTotalRecords()*heapHeader.getSizeOfRecord()+heapHeader.getHeaderSize();
 					for(String record : records){
 						if (count++ == 0) {
 							long totalRecords = heapHeader.getTotalRecords() + records.size()-1;
 							heapFile.makeHeapHeader(record, totalRecords); 
 
 						} else {
 							heapFile.insertInHeap(record, offset);
 							offset += heapFile.head.getSizeOfRecord();	
 						}
 					}
 				}
 				else {
 					//throw exception - or display error message
 					System.out.println("The CSV schema does not match with heap header schema");
 					System.exit(1);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
