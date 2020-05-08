 package cse509.similarity;
 
 import java.io.*;
 import java.util.*;
 
 public class QueryFileReader {
 	private static final String QUERY_FILE_LOCATION = "inputs/queryFile.csv";
 	private static final String DISCO_COMMAND = "java -jar ";
 	private static String DISCO_JAR;
 	private static String DISCO_REPO;
 	private static String DISCO_OPT;
 	private static final String DELIMITER = ",";
 	private static long scores[][];
 	private static Vector<String> queries;
 	private static Vector<String> queryTypes;
 	private static final int PRECISION = 1000000000;
 	
 	public static void main(String[] args) {
 
 		BufferedReader bufferedReader;
 		String line;
 		int counter = 0;
 		queries = new Vector<String>();
 		queryTypes = new Vector<String>();
 		
 		/*
 		if (args.length != 3) {
 			System.out.println("Usage: java -jar Query.jar DISCO_JAR DISCO_REPO DISCO_OPT");
 			System.exit(1);
 		}
 		
 		DISCO_JAR = new String(args[0].toString());
 		DISCO_REPO = new String(args[1].toString());
 		DISCO_OPT = new String(args[2].toString());
 		*/
 
 		/* Part 1: Read all input queries */
 		try {
 			bufferedReader = new BufferedReader(new FileReader(new File(QUERY_FILE_LOCATION)));
 			
 			//Read header of CSV
 			bufferedReader.readLine();
 			
 			while ((line = bufferedReader.readLine()) != null) {
 				//Extract Column 1 : Query
 				int col1Limit = line.indexOf(DELIMITER);
 				String q = new String(line.substring(0, col1Limit));
 				queries.add(q);
 				
 				//Extract Column 2 : Type
 				int col2Limit = line.substring(col1Limit+1).indexOf(DELIMITER);
 				String type = new String(line.substring((col1Limit+1), (col1Limit+col2Limit+1)));
 				queryTypes.add(type);
 				
 				counter++;
 			}
 			
 			bufferedReader.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		/* Part 2: Compute scores for each query */
 		scores = new long[counter][counter];
 		
 		/* Initialize scores */
 		for (int i = 0; i < counter; i++) {
 			for (int j = 0; j < counter; j++) {
 				scores[i][j] = -1;
 			}
 		}
 		
 		for (int i = 0; i < counter; i++) {
 			System.out.format("%s  ", queryTypes.get(i), i);
 			for (int j = 0; j < counter; j++) {
 				if (i == j)
 					scores[i][j] = PRECISION;
 				else if (scores[j][i] == -1)
 					scores[i][j] = Math.round((PRECISION)*calculateScore(queries.elementAt(i), queries.elementAt(j)));
 				else scores[i][j] = scores[j][i];
 				
 				System.out.format("%10d ",scores[i][j]);
 			}
 			System.out.println();
 		}
 	}
 	
 	static double calculateScore(String s, String t) {
 		String line = "";
 		Process child;
 		double maxScore = 0.0;
 		double aggregate = 0.0;
 		
 		/* Step 1: Extract words from s and t */
 		Vector<String> sWords = new Vector<String>();
 		Vector<String> tWords = new Vector<String>();
 		sWords = tokenize(s);
 		tWords = tokenize(t);
 		
 		/* Step 2: Compute the score between each word */
 		for (int i = 0; i < sWords.size(); i++) {
 			for (int j = 0; j < tWords.size(); j++) {
 				try {
 					//child = Runtime.getRuntime().exec(DISCO_COMMAND + " " + DISCO_JAR + " " + DISCO_REPO + " " + DISCO_OPT + " " + sWords.get(i) + " " + tWords.get(j));
 					child = Runtime.getRuntime().exec("./ngd.py " + sWords.get(i) + " " + tWords.get(j)+" 2>> err");
 					int result = child.waitFor();
 					
 					if (result == 0) {
 						BufferedReader stdInput = new BufferedReader(new InputStreamReader(child.getInputStream()));
 						line = stdInput.readLine();
 						if (maxScore < Double.parseDouble(line))
 							maxScore = Double.parseDouble(line);
 					} else {
 						System.out.println("Error in process: " + child.toString());
 					}
 				} catch (Exception e) {
 					//Ignore 'Word Not Found'
 				}
 			}
 			aggregate+=maxScore;
 		}
 		
 		//Return normalized score
 		return (double)aggregate/(double)(Math.max(sWords.size(), tWords.size()));
 	}
 	
 	static Vector<String> tokenize(String str) {
 		Vector<String> v = new Vector<String>();
 		StringTokenizer st = new StringTokenizer(str);
 		
 		while (st.hasMoreElements()) {
 			v.add(st.nextToken());
 		}
 		
 		return v;
 	}
 	
  	static void printScores() {
 		for (int i = 0; i < scores.length; i++) {
 			System.out.format("%s  ", queryTypes.get(i), i);
 			for (int j = 0; j < scores[i].length; j++) {
 				System.out.format("%10d ",scores[i][j]);
 			}
 			System.out.println();
 		}
 	}
 }
