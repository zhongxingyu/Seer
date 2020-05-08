 package edu.umich.lib.solr.analysis;
 import java.util.Map;
 import org.apache.lucene.analysis.util.TokenFilterFactory;
 import org.apache.lucene.analysis.TokenStream;
 
 public class LCCNNormalizerFilterFactory extends TokenFilterFactory
 {
    protected LCCNNormalizerFilterFactory(Map<String, String> args) {
		super(args);
	}
    
     public LCCNNormalizerFilter create(TokenStream input)
     {
         return new LCCNNormalizerFilter(input);
     }
 }
 
