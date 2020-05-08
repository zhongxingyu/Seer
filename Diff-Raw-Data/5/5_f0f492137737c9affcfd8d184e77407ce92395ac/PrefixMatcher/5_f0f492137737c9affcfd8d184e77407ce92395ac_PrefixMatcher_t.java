 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.LinkedList;
 
 public class PrefixMatcher {
 
 	/**
 	 * @param args
 	 *            this program takes a file as command line parameter <br/>
 	 *            This file should contain a number of strings within which we
 	 *            will search for prefixes<br/>
 	 */
 	public static void main(String[] args) throws IOException {
 
 		if (args.length != 1) {
 			System.out
 					.println("Usage: give the name of a single dictionary file as the sole argument");
 			System.exit(-1);
 		}
 
 		String s;
 		BufferedReader br = null;
 		FileReader in = null;
 
 		Trie trie = new Trie();
 		
 		int trieWordCount = 0;
 		double trieElapsedTime;
 		long trieBytes;
 		
 		try {
 			System.out.println("Trie");
 			
 			double start = System.nanoTime();
 			File file = new File(args[0]);
 			in = new FileReader(file);
 			br = new BufferedReader(in);
 			System.out.print("Loading file " + args[0] + "...\n");
 
 			while ((s = br.readLine()) != null) {
 				trie.insert(s, trie.root);
 				trieWordCount++;
 			}
 
 			trieElapsedTime = ((System.nanoTime() - start)/1000);
 			trieBytes = trie.calculateStorage();
 			
 			
 			
 
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		// now read in the array storage
 		ArrayStorage array = new ArrayStorage();
 		
 		int arrayWordCount = 0;
 		double arrayElapsedTime;
 		long arrayBytes;
 
 		try {
 			System.out.println("ArrayStorage");
 			File file = new File(args[0]);
 			in = new FileReader(file);
 			br = new BufferedReader(in);
 
 			System.out.print("Loading file " + args[0] + "...\n");
 
 			int asCount = 0;
 			double asStart = System.nanoTime();
 			while ((s = br.readLine()) != null) {
 				array.add(s);
 				arrayWordCount++;
 			}
 			array.sort();
 			arrayElapsedTime = ((System.nanoTime() - asStart)/1000);
 			arrayBytes = array.calculateStorage();
 
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		
 		
 		
 		//print initialization stats
 		StringBuffer sb = new StringBuffer();
 		sb.append(hardLine('*', 80)+"\n");
 		String str = "";
 		str = hardLine(' ',15-str.length())+"Trie";
 		str += hardLine(' ',40-str.length());
 		str+="|";
 		str+=hardLine(' ', 10)+"Array";
 		str+="\n";
 		sb.append(str);
 		
 		str="Word Count";
 		str += hardLine(' ',15-str.length());
 		str+=String.format("%,d",trieWordCount);
 		str += hardLine(' ',40-str.length());
 		str+="|";
 		str += hardLine(' ', 10);
 		str+=String.format("%,d",arrayWordCount);
 		str+="\n";
 		sb.append(str);
 		
 		str="Init Time";
 		str += hardLine(' ',15-str.length());
 		str+=String.format("%,.0f",trieElapsedTime);
 		str += hardLine(' ',40-str.length());
 		str+="|";
 		str += hardLine(' ', 10);
 		str+=String.format("%,.0f",arrayElapsedTime);
 		str+="\n";
 		sb.append(str);
 		
 		str="Bytes";
 		str += hardLine(' ',15-str.length());
 		str+=String.format("%,d",trieBytes);
 		str += hardLine(' ',40-str.length());
 		str+="|";
 		str += hardLine(' ', 10);
 		str+=String.format("%,d",arrayBytes);
 		str+="\n";
 		sb.append(str);
 		sb.append(hardLine('*', 80)+"\n");
 		System.out.print(sb.toString());
 
 		// debug
 		// System.out.println(trie.preorder(trie.root, new StringBuffer()));
 
 		BufferedReader buffy = new BufferedReader(new InputStreamReader(
 				System.in));
 		System.out
 				.println("Type a pattern to find matches in this dictionary. Type '/q' to exit the program.");
 		while (true) {
 			System.out.flush();
 			System.out.print("prefix-matcher # ");
 			System.out.flush();
 			String p = buffy.readLine();
 			if (p.equals("/q")) {
 				System.exit(0);
 			} else {
 
 				// perform query
 				double start = System.nanoTime();
 				ArrayList<String> trieResults = trie.search(p);
 				double elapsed = (System.nanoTime() - start);
 
 				double asStart = System.nanoTime();
 				LinkedList<String> arrayResults = array.search(p);
				if(arrayResults!=null){
					Collections.sort(arrayResults);
				}
 				double asElapsed = System.nanoTime() - asStart;
 				
 				// print output
 				int rows=0;
 				if(trieResults != null && arrayResults !=null){
 					rows = Math.max(arrayResults.size(), trieResults.size());
 				}
 				sb = new StringBuffer();
 				sb.append(hardLine('*', 80)+"\n");
 				str = "";
 				str = hardLine(' ',15-str.length())+"Trie";
 				str += hardLine(' ',40-str.length());
 				str+="|";
 				str+=hardLine(' ', 10)+"Array";
 				str+="\n";
 				sb.append(str);
 				
 				str="Matches";
 				str += hardLine(' ',15-str.length());
 				str+=trieResults != null ? trieResults.size() : 0;
 				str += hardLine(' ',40-str.length());
 				str+="|";
 				str += hardLine(' ', 10);
 				str+=arrayResults != null ? arrayResults.size() : 0;
 				str+="\n";
 				sb.append(str);
 				
 				str="Time";
 				str += hardLine(' ',15-str.length());
 				str+=String.format("%,.0f", elapsed);
 				str+=" microsec";
 				str += hardLine(' ',40-str.length());
 				str+="|";
 				str += hardLine(' ', 10);
 				str+=String.format("%,.0f", asElapsed);
 				str+=" microsec";
 				str+="\n";
 				sb.append(str);
 				
 				sb.append(hardLine('-', 80)+"\n");
 				
 				for (int i = 0; i < rows; i++) {
 					str = "";
 					if (trieResults.get(i) != null) {
 						str += trieResults.get(i);
 					}
 					str += hardLine(' ', 40 - str.length());
 					str += "|";
 					str += hardLine(' ', 10);
 					if (arrayResults.get(i) != null) {
 						str += arrayResults.get(i);
 					}
 					str+="\n";
 					sb.append(str);
 				}System.out.println(sb.toString());
 
 			}
 
 		}
 	}
 
 	public static String formatOutput(int columns) {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i <= columns; i++) {
 			sb.append("    ");
 		}
 
 		return sb.toString();
 	}
 
 	public static String hardLine(char c, int cols) {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i <= cols; i++) {
 			sb.append(c);
 		}
 		;
 		return sb.toString();
 	}
 }
