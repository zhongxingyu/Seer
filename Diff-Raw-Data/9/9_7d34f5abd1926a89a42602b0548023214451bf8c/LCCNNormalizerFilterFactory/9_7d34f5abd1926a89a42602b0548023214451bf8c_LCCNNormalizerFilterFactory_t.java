 package edu.umich.lib.solr.analysis;
 import java.util.Map;
 import org.apache.lucene.analysis.util.TokenFilterFactory;
 import org.apache.lucene.analysis.TokenStream;
 
 public class LCCNNormalizerFilterFactory extends TokenFilterFactory
 {
    public LCCNNormalizerFilterFactory(Map<String, String> args) {
        super(args);
    }
   
    @Override 
     public LCCNNormalizerFilter create(TokenStream input)
     {
         return new LCCNNormalizerFilter(input);
     }
 }
 
