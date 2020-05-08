 package filters;
 
 import general.Email;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import weka.core.Attribute;
 
 public class WordFrequencyFilterCreator implements FilterCreator{
 
 	private HashMap<String, LabelTermFrequencyManager> labelFreqMgrMap;
 	private HashMap<String, HashSet<String>> wordToLabelsMap;
 	private final String ATT_NAME_PREFIX = "WFF_";
 	private final int IMP_WORDS_PER_LABEL = 80;
 	
 	//TODO: un-comments the prev. line
     //private final int IMP_WORDS_PER_LABEL = 5; //just for testing
 	
 	public WordFrequencyFilterCreator() {
 		labelFreqMgrMap = new HashMap<String, WordFrequencyFilterCreator.LabelTermFrequencyManager>();
 		wordToLabelsMap = new HashMap<String, HashSet<String>>();
 	}
 	
 	private HashMap<String, Double> calcNormalizedFrequency(Email email){
 		HashMap<String, Double> normFreq = new HashMap<String, Double>();
 		HashSet<String> unique = new HashSet<String>();
 		
 		String splitRegex = " ";
 		String[] toks = (email.getSubject() + " " + email.getContent().trim()).split(splitRegex);
 		for(int i=0; i<toks.length; i++){
 			
			//XXX revise this 
			//(DONE : might happen on few cases) like empty subject or subject with extra spaces at end
			if (toks[i].length() == 0)
				continue;
 			
 			Double freq = normFreq.get(toks[i]);
 			if(freq == null){
 				freq = 0.0;
 				unique.add(toks[i]);
 			}
 			normFreq.put(toks[i], ++freq);
 		}
 
 		//normalize frequencies
 		//XXX Frequencies should be normalized by number
 		//of words in the email not email size !!
 		//int size = email.getSize();
 		int size = toks.length;
 		Iterator<String> itr = unique.iterator();
 		while(itr.hasNext()){
 			String s = itr.next();
 			normFreq.put(s, normFreq.get(s)/size);
 		}
 		
 		return normFreq;
 	}
 	
 	private ArrayList<Attribute> extractAttributes(){
 		ArrayList<Attribute> atts = new ArrayList<Attribute>();
 		
 		Iterator<Map.Entry<String, LabelTermFrequencyManager>> itr = labelFreqMgrMap.entrySet().iterator();
 		HashSet<String> uniqueWords = new HashSet<String>();
 		while(itr.hasNext()){
 			Map.Entry<String, LabelTermFrequencyManager> pair = itr.next();
 			String[] words = pair.getValue().extractImportantWords(IMP_WORDS_PER_LABEL);
 			for(int i=0; i<words.length; i++){
 				if(!uniqueWords.contains(words[i])){
 					uniqueWords.add(words[i]);
 					atts.add(new Attribute(ATT_NAME_PREFIX + words[i]));
 				}
 			}
 		}
 		
 		return atts;
 	}
 	
 	@Override
 	public Filter createFilter(Email[] emails) {
 		//Steps
 		// 1- loop on emails and for each email
 		//   a- calculate normalized frequencies
 		//   b- update frequencies for each label (labelFreqMgr)
 		//   c- update wordTolapel (how many times each term appears /label)
 		
 		for(Email email : emails){
 			String lbl = email.getLabel();
 			LabelTermFrequencyManager mgr = labelFreqMgrMap.get(lbl);
 			if(mgr == null){
 				mgr = new LabelTermFrequencyManager();
 				labelFreqMgrMap.put(lbl, mgr);
 			}
 			
 			HashMap<String, Double> emailNormFreq = calcNormalizedFrequency(email);
 			mgr.updateFrequencies(emailNormFreq);
 			
 			// update wordToLabelMap
 			//XXX can avoid the overhead of this loop by having this work 
 			//done as a side-effect in the calcNormalizedFreq() function
 			Iterator<Map.Entry<String, Double>> it = emailNormFreq.entrySet().iterator();
 			while(it.hasNext()){
 				String word = it.next().getKey();
 				if(! wordToLabelsMap.containsKey(word))
 					wordToLabelsMap.put(word, new HashSet<String>());
 				
 				wordToLabelsMap.get(word).add(lbl);
 			}
 		}
 		
 		ArrayList<Attribute> atts = extractAttributes();
 		String[] options = new String[]{ATT_NAME_PREFIX};
 		Filter wordFrequencyFilter = new WordFrequencyFilter(atts, options);
 
 		return wordFrequencyFilter;
 	}
 
 	private class LabelTermFrequencyManager{
 		public HashMap<String, Double> normFreq;
 		
 		public LabelTermFrequencyManager(){
 			normFreq = new HashMap<String, Double>();
 		}
 
 		public void updateFrequencies(HashMap<String, Double> emailNormFreq){
 			Iterator<Map.Entry<String, Double>> itr = emailNormFreq.entrySet().iterator();			
 			while(itr.hasNext()){
 				Map.Entry<String, Double> pair = itr.next();
 				Double normalized = normFreq.get(pair.getKey());
 				if(normalized == null) normalized = 0.0;
 				normalized += pair.getValue();
 				normFreq.put(pair.getKey(), normalized);
 			}
 		}
 		
 		public String[] extractImportantWords(int maxSize){
 			TfIdfScore[] tfidf = new TfIdfScore[normFreq.size()];
 
 			Iterator<Map.Entry<String, Double>> itr = normFreq.entrySet().iterator();
 			int index = 0;
 			while(itr.hasNext()){
 				Map.Entry<String, Double> pair = itr.next();
 				double tf = pair.getValue();
 				double idf = Math.log10(((double) labelFreqMgrMap.size()) / wordToLabelsMap.get(pair.getKey()).size());
 				tfidf[index++] = new TfIdfScore(pair.getKey(), tf*idf);
 			}
 			
 			if(tfidf.length > maxSize)
 				Arrays.sort(tfidf);
 			
 			String[] importantWords = new String[Math.min(maxSize, tfidf.length)];
 			
 			for(int i=0; i<importantWords.length; i++) 
 				importantWords[i] = tfidf[i].word;
 			
 			return importantWords;
 		}
 	}
 	
 	private class TfIdfScore implements Comparable<TfIdfScore>{
 		public String word;
 		public double score;
 		private double eps = 1e-10;
 		
 		public TfIdfScore(String word, double score){
 			this.word = word;
 			this.score = score;
 		}
 
 		@Override
 		public int compareTo(TfIdfScore s) {
 			if(score > s.score+eps) return -1;
 			else if(score + eps < s.score) return 1;
 			return 0;
 		}
 	}
 }
