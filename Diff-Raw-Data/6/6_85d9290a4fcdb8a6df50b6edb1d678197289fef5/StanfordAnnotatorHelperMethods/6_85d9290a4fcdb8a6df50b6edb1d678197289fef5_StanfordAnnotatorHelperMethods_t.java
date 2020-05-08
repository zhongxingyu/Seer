 package edu.knowitall.tac2013.stanford.annotator.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Stack;
import java.util.WeakHashMap;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 
 import edu.knowitall.collection.immutable.Interval;
 import edu.stanford.nlp.dcoref.CorefChain;
 import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
 import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
 import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
 import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefGraphAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.*;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.StanfordCoreNLP;
 import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation;
 import edu.stanford.nlp.time.Timex;
 import edu.stanford.nlp.util.CoreMap;
 import edu.stanford.nlp.util.IntTuple;
 import edu.stanford.nlp.util.Pair;
 
 import edu.knowitall.tac2013.app.Candidate;
 import edu.knowitall.tac2013.app.KBPQueryEntityType;
 import edu.knowitall.tac2013.solr.query.SolrHelper;
 
 
 
 public class StanfordAnnotatorHelperMethods {
 	
 	private final StanfordCoreNLP suTimePipeline;
 	private final StanfordCoreNLP corefPipeline;
 	private String filePath = "/homes/gws/jgilme1/docs/";
 	private Map<String,Annotation> corefAnnotationMap;
 	private Map<String,Annotation> suTimeAnnotationMap;
 	
 	
 	public StanfordAnnotatorHelperMethods(){
 		Properties suTimeProps = new Properties();
 		suTimeProps.put("annotators", "tokenize, ssplit, pos, lemma, cleanxml, ner");
 		suTimeProps.put("sutime.binders", "0");
 		suTimeProps.put("clean.datetags","datetime|date|dateline");
 		this.suTimePipeline = new StanfordCoreNLP(suTimeProps);
 		
 		Properties corefProps = new Properties();
 	    corefProps.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse, dcoref");
 	    corefProps.put("clean.allowflawedxml", "true");
 	    corefProps.put("ner.useSUTime", "false");
 	    //clean all xml tags
 		this.corefPipeline = new StanfordCoreNLP(corefProps);
 		
		corefAnnotationMap = new WeakHashMap<String,Annotation>();
		suTimeAnnotationMap = new WeakHashMap<String,Annotation>();
 
 
 	}
 	
 	public static void main(String[] args) throws FileNotFoundException, IOException{
 		StanfordAnnotatorHelperMethods sh = new StanfordAnnotatorHelperMethods();
 		sh.runSuTime("testXMLDoc");
 		
 	}
 	
 	public void runSuTime(String docID) throws FileNotFoundException, IOException{
 		Annotation document;
 		if(suTimeAnnotationMap.containsKey(docID)){
 			document = suTimeAnnotationMap.get(docID);
 		}
 		else{
 		  String filePathPlusDocId = this.filePath+docID;
 		  FileInputStream in = new FileInputStream(new File(filePathPlusDocId));
 		  String fileString = IOUtils.toString(in,"UTF-8");
 		  in.close();
 		
 		  document = new Annotation(fileString);
 		  suTimePipeline.annotate(document);
 		  suTimeAnnotationMap.put(docID, document);
 		}
 		
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
 	    for(CoreMap sentence: sentences){
 	    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
 	    		String word = token.get(TextAnnotation.class);
 	    		String ne = token.get(NamedEntityTagAnnotation.class);
 	    		String net = token.get(NormalizedNamedEntityTagAnnotation.class);
 	    		Timex tt = token.get(TimexAnnotation.class);
 	    		String tts = "";
 	    		if(tt != null){
 	    			tts = tt.value();
 	    		}
 	    		System.out.println(word+ " " + ne + " " + net + " " + tts);
 	    	}
 	    }
 	    
 	    String s =document.get(NamedEntityTagAnnotation.class);
 	    System.out.println(s);
 
 	}
 	
 	private String normalizeTimex(Timex t){
 		if(t.timexType() == "DATE"){
 	      String timexString = t.value();
 	      if (timexString == null) return "";
 	      String formattedString = normalizeDate(timexString);
 		  return formattedString;
 		}
 		else{
 			return "";
 		}
 	}
 	
 	private String normalizeDate(String dateString){
 		  String formattedString = null;
 	      if(Pattern.matches("\\w{4}", dateString)){
 	    	  formattedString = dateString +"-XX-XX";
 	      }
 	      else if(Pattern.matches("\\w{2}-\\w{2}",dateString)){
 	    	  formattedString = "XXXX-" + dateString; 
 	      }
 	      else if(Pattern.matches("\\w{4}-\\w{2}", dateString)){
 	    	  formattedString = dateString + "-XX";
 	      }
 		  
 	      if(formattedString == null){
 	    	  return dateString;
 	      }
 	      else{
 	    	  return formattedString;
 	      }
 	}
 	
 
 	
 	public String getNormalizedDate(Interval charInterval, String docId, String originalString) throws IOException{
 		Annotation document;
 		if(suTimeAnnotationMap.containsKey(docId)){
 			document = suTimeAnnotationMap.get(docId);
 		}
 		else{
 			String xmlDoc = SolrHelper.getRawDoc(docId);
 			if(xmlDoc.trim().isEmpty()){
 				return originalString;
 			}
 			document = new Annotation(xmlDoc);
 			suTimePipeline.annotate(document);
 			suTimeAnnotationMap.put(docId, document);
 		}
 	
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
 		    for(CoreMap sentence: sentences){
 		    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
 		    		Timex tt = token.get(TimexAnnotation.class);
 		    		if(charInterval.intersects(Interval.closed(token.beginPosition(), token.endPosition()))){
 		    			if(tt != null && tt.value() != null){
 		    				return normalizeTimex(tt);
 		    			}
 		    		}
 		    	}
 		    }
 	       return normalizeDate(originalString);
 	}
 	
 	public List<CorefMention> getCorefMentions(String xmlString, Interval interval) {
 		Annotation document = new Annotation(xmlString);
 		corefPipeline.annotate(document);
 		
 		
 		
 		Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
 		for(Integer i : graph.keySet()){
 			System.out.println("GROUP " + i);
 			CorefChain x = graph.get(i);
 			for( CorefMention m : x.getMentionsInTextualOrder()){
 				System.out.println(m.mentionSpan);
 			}
 		}
 
 		Integer corefClusterID = null;
 		
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
 		for(CoreMap sentence : sentences){
 			for(CoreLabel token : sentence.get(TokensAnnotation.class)){
 				
 			}
 		}
 		List<Pair<IntTuple,IntTuple>> x  = document.get(CorefGraphAnnotation.class);
 
 		
 	    for(CoreMap sentence: sentences){
 	    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
 	    		if(token.beginPosition() == interval.start()){
 	    			corefClusterID = token.get(CorefClusterIdAnnotation.class);
 	    		}
 	    	}
 	    }
 	    
 		
 	    if(corefClusterID != null){
 	    	return graph.get(corefClusterID).getMentionsInTextualOrder();
 	    }
 	    else{
 	    	return new ArrayList<CorefMention>();
 	    }
 		
 	}
 	
 	
 	/**
 	 * Provides a lookup method for taking corefMentions and finding their NER tagged
 	 * substrings.
 	 * @param annotatedDocument
 	 * @param position
 	 * @return
 	 */
 	private Interval getNamedEntityAtPosition(Annotation annotatedDocument, IntTuple position, KBPQueryEntityType entityType){
 		
 		return Interval.open(0, 1);
 	}
 	
 	
 	private CoreLabel getTokenBeginningAtByteOffset(Annotation annotatedDocument, Integer beg){
 		
 		List<CoreMap> sentences = annotatedDocument.get(SentencesAnnotation.class);
 		for(CoreMap sentence : sentences){
 			for(CoreLabel token : sentence.get(TokensAnnotation.class)){
 				if(token.beginPosition() == beg ){
 					return token;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Given the information from a CorefMention determine the byte offsets
 	 * of the whole mention and return as a knowitall Interval.
 	 * @param document
 	 * @param sentNum
 	 * @param startIndex
 	 * @param endIndex
 	 * @return
 	 */
 	private Interval getCharIntervalFromCorefMention(Annotation document, Integer sentNum, Integer startIndex, Integer endIndex){
 		
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
 		CoreMap sentence = sentences.get(sentNum-1);
 		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 		List<CoreLabel> spanningTokens = new ArrayList<CoreLabel>();
 		for(int i = startIndex; i < endIndex; i++){
 			spanningTokens.add(tokens.get(i-1));
 		}
 		
 		return Interval.closed(spanningTokens.get(0).beginPosition(),spanningTokens.get(spanningTokens.size()-1).endPosition());
 		
 	}
 	
 	public Interval getIntervalOfKBPEntityMention(String kbpEntityString, Interval originalInterval, String docID){
 		Annotation document;
 		if(corefAnnotationMap.containsKey(docID)){
 			document = corefAnnotationMap.get(docID);
 		}
 		else{
 			String xmlDoc = SolrHelper.getRawDoc(docID);
 			if(xmlDoc.trim().isEmpty()){
 				return null;
 			}
 			document = new Annotation(xmlDoc);
 			try{
 			 corefPipeline.annotate(document);
 			 corefAnnotationMap.put(docID, document);
 			}
 			catch (Exception e){
 				if(corefAnnotationMap.containsKey(docID)){
 					corefAnnotationMap.remove(docID);
 				}
 				return null;
 			}
 		}
 
 		
 		//get token of possible coref mention
 		CoreLabel token = getTokenBeginningAtByteOffset(document, originalInterval.start());
 		if(token == null){
 			return null;
 		}
 		Integer corefID = token.get(CorefClusterIdAnnotation.class);
 		if(corefID == null){
 			return null;
 		}
 		Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
 		List<CorefMention> mentionsInOrder = graph.get(corefID).getMentionsInTextualOrder();
 
 		
 		for(CorefMention corefMention : mentionsInOrder){
 			if (corefMention.mentionSpan.trim().toLowerCase().equals(kbpEntityString.trim().toLowerCase())){
 				// this is a match and the originalInterval corefers to the kbpEntityString
 				// return the proper interval of this mention of the kbpEntityString
 				return getCharIntervalFromCorefMention(document,corefMention.sentNum,corefMention.startIndex,corefMention.endIndex);
 			}
 		}
 		return null;
 	}
 }
