 package text_to_other;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 /***
  * Retrieves data from CSV files into a variety of structures
  */
 public class CSVDecoder {
 	
 	private final static String DELIMITER = (",");
 	
 	/**
 	 * @param csv CSV File to read entries from
 	 * @return ArrayList of String values read from the input file
 	 * */
 	private static ArrayList<String> readCSVFileLineByLine(File csv){
 		
 		BufferedReader br = null;
 		ArrayList<String> arrayList = new ArrayList<String>();
 		
 	    try {
 	    	
 	    	br = new BufferedReader(new FileReader(csv));	        
	        String line = br.readLine();
 
 	        while ((line = br.readLine()) != null) {
 	        	arrayList.add(line);
 	        }
 	        
 	    } catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 	        try {
 				br.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	    }
 	    
 	    return arrayList;
 	    
 	}
 	
 	/**
 	 * @param list ArrayList of String values to tokenize
 	 * @return ArrayList of String values that were previously comma separated
 	 * */
 	private static ArrayList<String> tokenizeArrayList(ArrayList<String> list){
 		
 		ArrayList<String> csvEntries = new ArrayList<String>();
 		
 		for (String line : list){
 			for (String entry : tokenizeLine(line)){
 				csvEntries.add(entry);
 			}
 		}
 		
 		return csvEntries;
 		
 	}
 	
 	private static ArrayList<String[]> tokenizeArrayListArray(ArrayList<String> list){
 		
 		ArrayList<String[]> csvEntries = new ArrayList<String[]>();
 		
 		for (String line : list){
 			ArrayList<String> aList = tokenizeLine(line);
 			String[] array = new String[aList.size()];
 			for (int i = 0; i < array.length; i++){
 				array[i] = (aList.get(i));
 			}
 			csvEntries.add(array);
 		}
 		
 		return csvEntries;
 		
 	}
 	
 	/**
 	 * @param line String to tokenize
 	 * @return ArrayList of String values that were previously comma separated
 	 * */
 	private static ArrayList<String> tokenizeLine(String line){
 
 		StringTokenizer token = new StringTokenizer(line, DELIMITER);
 		ArrayList<String> entries = new ArrayList<String>();
 		
 		while (token.hasMoreElements()) {
 			entries.add(token.nextToken());
 		}
 		
 		return entries;
 		
 	}
 	
 	public static String[][] CSVToTwoDimensionalArray(File csv){
 		
 		ArrayList<String> arrayList = readCSVFileLineByLine(csv);
 		ArrayList<String[]> csvEntries = new ArrayList<String[]>();
 		
 		for (String line : arrayList){
 			ArrayList<String> temp = tokenizeLine(line);
 			String[] tokenizedLine = new String[temp.size()];
 			for (int i = 0; i < temp.size(); i++){
 				tokenizedLine[i] = (temp.get(i));
 			}
 			csvEntries.add(tokenizedLine);
 		}
 		
 		String[][] results = new String[csvEntries.size()][];
 		for (int i = 0; i < csvEntries.size(); i++){
			results[0] = csvEntries.get(i);
 		}
 		
 		return results;
 		
 	}
 	
 	public static ArrayList<String> CSVToArrayList(File csv){
 		
 		ArrayList<String> arrayList = readCSVFileLineByLine(csv);
 		
 		return tokenizeArrayList(arrayList);
 		
 	}
 	
 	public static ArrayList<String[]> CSVToArrayListArray(File csv){
 		
 		ArrayList<String> arrayList = readCSVFileLineByLine(csv);
 		
 		return tokenizeArrayListArray(arrayList);
 		
 	}
 	
 }
