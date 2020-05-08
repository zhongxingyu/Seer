 package edu.cmu.cs.lti.ark.ssl.pos;
 
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import edu.cmu.cs.lti.ark.ssl.util.BasicFileIO;
 import fig.basic.Pair;
 
 public class TabSeparatedFileReader {
 
 	private static Logger log = Logger.getLogger(TabSeparatedFileReader.class.getCanonicalName());
 	
 	public static Collection<Pair<List<String>, List<String>>> 
 	readPOSSequences(String path, 
 			int numSequences, 
 			int maxSequenceLength) {
 		Collection<Pair<List<String>, List<String>>> sequences = 
 			new ArrayList<Pair<List<String>,List<String>>>();
 		BufferedReader reader = BasicFileIO.openFileToRead(path);
 		int countLines = 0;
 		while (true) {
 			Pair<List<String>, List<String>> sequence = getSequenceInfo(reader);
 			if (sequence == null) {
 				break;
 			}
 			if (sequence.getFirst().size() > maxSequenceLength) {
 				continue;
 			}
 			countLines++;
 			if (countLines > numSequences) {
 				break;
 			}
 			sequences.add(sequence);
 		}
 		return sequences;
 	}
 	
 	public static Collection<Pair<List<String>, List<String>>> 
 	readPOSFeatSequences(String path, 
 			int numSequences, 
 			int maxSequenceLength) {
 		Collection<Pair<List<String>, List<String>>> sequences = 
 				new ArrayList<Pair<List<String>,List<String>>>();
 		
 		int nSeq = 0;
 		for (List<String> seq : UnlabeledSentencesReader.readSequencesOneTokenPerLine(path, numSequences, maxSequenceLength)) {
 			List<String> observations = new ArrayList<String>();
 			List<String> labels = new ArrayList<String>();
 			for (String tokFeats : seq) {
				String lbl = tokFeats.substring(tokFeats.lastIndexOf('\t'));	// last column holds the label
 				tokFeats = tokFeats.substring(0,tokFeats.lastIndexOf('\t'));
 				observations.add(tokFeats);
 				labels.add(lbl);
 			}
 			sequences.add(Pair.makePair(observations, labels));
 			nSeq++;
 		}
 		
 		return sequences;
 	}
 
 	public static Pair<List<String>, List<String>> 
 	getSequenceInfo(BufferedReader reader) {
 		List<String> words = new ArrayList<String>();
 		List<String> posTags = new ArrayList<String>();	
 		String line = BasicFileIO.getLine(reader);
 		if (line == null) {
 			return null;
 		}
 		line = line.trim();
		while (!line.equals("") && line != null) {
 			String[] toks = getToks(line);
 			if (toks.length != 2) {
 				log.severe("Problem with line:" + line);
 				System.exit(-1);
 			}
 			words.add(toks[0]);
 			posTags.add(toks[1]);
 			line = BasicFileIO.getLine(reader);
 			if (line != null) {
 				line = line.trim();
 			}
 		}
 		return Pair.makePair(words, posTags);
 	}
 	
 	public static String[] getToks(String line) {
 		return getToks(line, " \t", true);
 	}
 	
 	public static String[] getToks(String line, String delim, boolean skipWhitespaceToks) {
 		List<String> tokList = new ArrayList<String>();
 		StringTokenizer st = new StringTokenizer(line.trim(), delim, true);
 		while (st.hasMoreTokens()) {
 			String tok = st.nextToken().trim();
 			if (skipWhitespaceToks && tok.equals("")) {
 				continue;
 			}
 			tokList.add(tok);
 		}
 		String[] arr = new String[tokList.size()];
 		tokList.toArray(arr);
 		return arr;
 	}
 	
 }
