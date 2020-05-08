 import java.io.*;
 import java.util.*;
 
 
 public class FilterTool {
 	static Integer numberToDetect;
 		
 	public static void main(String[] args) throws IOException {
 		
 		String outputFileName = null;
 		
 		File outputFile1 = null;
 		File outputFile2 = null;
 		
 		File firstEnd = null;
 		File secondEnd = null;
 		
 		int i=0;
 		boolean printHelp = false;
 		while(i<args.length && args[i].charAt(0)=='-') {
 			if("-f".equalsIgnoreCase(args[i])) {
 				i++;
 				firstEnd = new File(args[i]);
 			} else if("-s".equalsIgnoreCase(args[i])) {
 				i++;
 				secondEnd = new File(args[i]); //age.parseInt(args[i]);
 			} else if("-n".equalsIgnoreCase(args[i])) {
 				i++;
 				numberToDetect = numberToDetect.parseInt(args[i]);
 			} else if("-o".equalsIgnoreCase(args[i])) {
 				i++;
				outputFileName = args[i];
 			} 
 			else {
 				 printHelp = true;
 			}
 			i++;
 		}
 		
 		if( args.length < 8 || printHelp ) {
 			System.out.println("USAGE: extract -f <first-paired-end-read> -s <second-paired-end-read> -n <number-to-detect> -o <output-file-name>");
 			return;
 		}
				
 		Writer outputWriter1 = new BufferedWriter(new FileWriter(outputFile1));	
 		Writer outputWriter2 = new BufferedWriter(new FileWriter(outputFile2));
 		
 		try {
			
			
 			Scanner firstEndScanner = new Scanner(firstEnd);
 			Scanner secondEndScanner = new Scanner(secondEnd);
 			
 			
 			while(secondEndScanner.hasNext()){
 				String secondSequence = null;
 				String secondRead =secondEndScanner.next();
 				secondSequence = secondEndScanner.next();
 				secondRead = secondRead + "\n" + secondSequence + "\n";
 				secondRead = secondRead + secondEndScanner.next() + "\n";
 				secondRead = secondRead + secondEndScanner.next();
 				
 				String firstSequence = null;
 				String firstRead = firstEndScanner.next();
 				firstSequence = firstEndScanner.next();
 				firstRead = firstRead + "\n" + firstSequence + "\n";
 				firstRead = firstRead + firstEndScanner.next() + "\n";
 				firstRead = firstRead + firstEndScanner.next();
 				
 				if (tCounter(secondSequence) == numberToDetect) {
 					
 					outputWriter1.write(firstRead + "\n");
 					outputWriter2.write(secondRead + "\n");
 				}
 			}
 			
 		} 
 		catch (FileNotFoundException e) {
 			e.getMessage();
 		}
 		
 		outputWriter1.close();
 		outputWriter2.close();		
 	}
 	
 	public static int tCounter(String sequence) {
 		int count = 0;
 		for (int i=0; i<numberToDetect ;i++){
 			Character b = sequence.charAt(i);
 			if (b.equals('T') || b.equals('t')) {
 				count++;
 			}
 		}
 		return count;
 	}
 
 }
