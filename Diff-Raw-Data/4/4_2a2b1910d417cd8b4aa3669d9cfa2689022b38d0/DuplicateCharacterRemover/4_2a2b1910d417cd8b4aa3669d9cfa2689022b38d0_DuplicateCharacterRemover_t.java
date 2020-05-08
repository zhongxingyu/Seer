 package edu.columbia.watson.twitter;
 
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class DuplicateCharacterRemover {
 	public static void main(String args[]){
 		if (args.length < 1){
 			System.err.println("Usage: run.sh edu.columbia.watson.twitter.DuplicateCharacterRemover filename");
 			return;
 		}
 
 		BufferedReader in;
 		try {
 			in = new BufferedReader(new FileReader(args[0]));
 			while (in.ready()) {	
 				String line = in.readLine();
 				String[] all = line.split("\t");
				if (all.length < 7){
					System.out.println(line);
					continue;
				}
 				String tweet = all[6];
 				String[] splitted = tweet.split(" ");
 				StringBuilder all_sb = new StringBuilder();
 				for (String word : splitted){	
 					StringBuilder sb = new StringBuilder();
 					if (word.length() >=1)
 						sb.append(word.charAt(0));
 					if (word.length() >=2)
 						sb.append(word.charAt(1));
 					for (int i = 2 ; i < word.length() ; ++i){
 						if (word.charAt(i) != word.charAt(i-1) || word.charAt(i) != word.charAt(i-2) || word.charAt(i-1) != word.charAt(i-2))
 							sb.append(word.charAt(i));
 					}
 					sb.append(" ");
 					all_sb.append(sb);
 					//System.out.print(sb.toString() + " ");
 				}
 				System.out.println(all[0] + "\t" + all[1] + "\t" + all[2] + "\t" + all[3] + "\t" + all[4] + "\t" + all[5] + "\t" + all_sb.toString());
 				//System.out.println(tweet);
 			}
 		}
 		catch  (IOException e) {
 			System.err.println("IO exception!");
 			e.printStackTrace();
 		} 
 
 	}
 }
