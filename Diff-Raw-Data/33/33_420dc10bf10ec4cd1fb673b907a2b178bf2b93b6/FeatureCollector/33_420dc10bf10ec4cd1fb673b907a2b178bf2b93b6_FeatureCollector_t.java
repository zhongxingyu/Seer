 package com.zuehlke.reuters.mahout.features;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.mahout.math.RandomAccessSparseVector;
 import org.apache.mahout.math.Vector;
 
 import com.zuehlke.reuters.mahout.ReutersMessage;
 
 public class FeatureCollector {
 	public static final int VECTOR_SIZE = 1;
	private Set<Feature> features = new HashSet<Feature>();
	private Map<String, List<String>> categoryWords;
 	
 
 	public FeatureCollector(Map<String, List<String>> categoryWords){
 		this.categoryWords = categoryWords;
 	}
 	
 	public Vector extractFeatures(ReutersMessage message){
		
		addFeatures();
		
 		Vector vector = new RandomAccessSparseVector(VECTOR_SIZE);
 		for(Feature feature : features){
 			feature.extract(message, vector);
 		}
 		return vector;
 	}
 	
	private void addFeatures() {
		features.add( new NumberCountFeature() );
		features.add( new WordCountFeature(categoryWords) );
		features.add( new BiasFeature() );
		features.add( new CurrencyCountFeature() );
		features.add( new AdaptativeWordCountFeature() );
	}

 	public int getFeatureCount() {
 		return VECTOR_SIZE;
 	}
 
 	public Vector extractFeatures(String text) {
 		return extractFeatures(new ReutersMessage(null, null, text));
 	}
 }
