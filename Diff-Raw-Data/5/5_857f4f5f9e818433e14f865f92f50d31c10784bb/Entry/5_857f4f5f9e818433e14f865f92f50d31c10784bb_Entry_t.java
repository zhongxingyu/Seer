 package CLRLM;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 public class Entry {
 	String sourceFile = "";
 	String targetFile = "";
 	String stopWordsFile = "data/stopwords.txt";
 	HashSet<String> stopWords = new HashSet<String>();
 	boolean t = false;
 	HashMap<String, HashMap<Integer, Double>> probWordGivenSource = new HashMap<String, HashMap<Integer, Double>>();
 	HashMap<String, HashMap<Integer, Double>> probWordGivenTarget = new HashMap<String, HashMap<Integer, Double>>();
 	HashMap<String, Double> ps = new HashMap<String, Double>();
 	HashMap<String, Double> pt = new HashMap<String, Double>();
 	
 	public static void main(String[] args) {
 		Entry entry = new Entry();		
 		entry.stopWords();
 		if(args.length > 0 && args[0].equalsIgnoreCase("t")) {
 			entry.t = true;
 			entry.sourceFile = "data/source.txt";
 			entry.targetFile = "data/target.txt";
 			entry.train();
 		}
 		Relevance relevance = new Relevance(entry);
 		try{
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			String line;
 			while(true) {
 				line = br.readLine();
 				if(line.equalsIgnoreCase("exit"))
 					break;
 				relevance.configure(line);
 			}
 			relevance.close();
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Entry() {
 		
 	}
 	
 	public void stopWords() {
 		try{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stopWordsFile)));
 			String line;
 			while((line = br.readLine()) != null) {
 				stopWords.add(line);
 			}
 			br.close();
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void train() {
 		try{
 			String line;			
 			HashMap<String, Integer> countSource = new HashMap<String, Integer>();
 			HashMap<String, Integer> countTarget = new HashMap<String, Integer>();
 			int totalSource = 0, totalTarget = 0;
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));		
 			int No = 0;
 			while((line = br.readLine()) != null) {
 				No++;
 				HashMap<String, Integer> tmpCount = new HashMap<String, Integer>();
 				String terms[] = line.split(" ");
 				int tmpTotalSource = 0;
 				for(String term: terms) {
 					if(tmpCount.containsKey(term)) {
 						tmpCount.put(term, tmpCount.get(term)+1);
 					}
 					else {
 						tmpCount.put(term, 1);
 					}
 					tmpTotalSource++;
 				}
 				totalSource += tmpTotalSource;
 				for(Map.Entry<String, Integer> term:tmpCount.entrySet()) {
 					if(countSource.containsKey(term.getKey())) {
 						countSource.put(term.getKey(), countSource.get(term.getKey()) + term.getValue());
 						probWordGivenSource.get(term.getKey()).put(No, ((double)term.getValue()/tmpTotalSource));
 					}
 					else {
 						countSource.put(term.getKey(), term.getValue());
 						HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
 						tmp.put(No, ((double)term.getValue()/tmpTotalSource));
 						probWordGivenSource.put(term.getKey(), tmp);
 					}
 				}
 			}
 			br.close();
 			
 			br = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile)));			
 			No = 0;
 			while((line = br.readLine()) != null) {
 				No++;
 				HashMap<String, Integer> tmpCount = new HashMap<String, Integer>();
 				String terms[] = line.split(" ");
 				int tmpTotalTarget = 0;
 				for(String term: terms) {
 					term = term.toLowerCase();
 					if(stopWords.contains(term)) continue;
 					if(tmpCount.containsKey(term)) {
 						tmpCount.put(term, tmpCount.get(term)+1);
 					}
 					else {
 						tmpCount.put(term, 1);
 					}
 					tmpTotalTarget++;
 				}
 				totalTarget += tmpTotalTarget;
 				for(Map.Entry<String, Integer> term:tmpCount.entrySet()) {
 					if(countTarget.containsKey(term.getKey())) {
 						countTarget.put(term.getKey(), countTarget.get(term.getKey()) + term.getValue());
 						probWordGivenTarget.get(term.getKey()).put(No, ((double)term.getValue()/tmpTotalTarget));
 					}
 					else {
 						countTarget.put(term.getKey(), term.getValue());
 						HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
 						tmp.put(No, ((double)term.getValue()/tmpTotalTarget));
 						probWordGivenTarget.put(term.getKey(), tmp);
 					}
 				}
 			}
 			br.close();
 
 			
 			PrintWriter bw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("data/probSource.txt")));
 			for(Map.Entry<String, Integer> m:countSource.entrySet()) {
 				bw.println(m.getKey() + " " + (double)m.getValue()/(double)totalSource);
 				ps.put(m.getKey(), (double)m.getValue()/(double)totalSource);
 			}
 			bw.close();
 			
 			bw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("data/probTarget.txt")));
 			for(Map.Entry<String, Integer> m:countTarget.entrySet()) {
 				bw.println(m.getKey() + " " + (double)m.getValue()/(double)totalTarget);
 				pt.put(m.getKey(), (double)m.getValue()/(double)totalSource);
 			}
 			bw.close();
 			bw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("data/probWordGivenSource.txt")));
 			for(Map.Entry<String, HashMap<Integer, Double>> m:probWordGivenSource.entrySet()) {
 				bw.print(m.getKey() + " ");
 				for(Map.Entry<Integer, Double> x: m.getValue().entrySet()) {	
					bw.print(x.getKey() + " " + x.getValue() + " ");
 				}
 				bw.println();
 			}
 			bw.close();
 			bw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("data/probWordGivenTarget.txt")));
 			for(Map.Entry<String, HashMap<Integer, Double>> m:probWordGivenTarget.entrySet()) {
 				bw.print(m.getKey() + " ");
 				for(Map.Entry<Integer, Double> x: m.getValue().entrySet()) {	
					bw.print(x.getKey() + " " + x.getValue() + " ");
 				}
 				bw.println();
 			}
 			bw.close();
 			
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
