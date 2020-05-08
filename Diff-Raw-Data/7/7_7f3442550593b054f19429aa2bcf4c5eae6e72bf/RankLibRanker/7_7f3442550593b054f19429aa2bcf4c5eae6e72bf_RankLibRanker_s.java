 package txtfnnl.uima.resource;
 
 import ciir.umass.edu.learning.RankList;
 import ciir.umass.edu.learning.Ranker;
 import ciir.umass.edu.learning.RankerFactory;
 import org.apache.uima.resource.DataResource;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.SharedResourceObject;
 import org.uimafit.component.ExternalResourceAware;
 import org.uimafit.component.initialize.ConfigurationParameterInitializer;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.factory.ExternalResourceFactory;
 import txtfnnl.uima.SharedResourceBuilder;
 
 import java.net.URI;
 import java.util.List;
 
 /**
  * A shared, external RankLib Ranker model resource.
  */
 public
 class RankLibRanker implements RankerResource, ExternalResourceAware {
   @ConfigurationParameter(name = ExternalResourceFactory.PARAM_RESOURCE_NAME)
   private String resourceName;
   protected String resourceUrl = null;
   private Ranker ranker = null;
 
   public static
   class Builder extends SharedResourceBuilder {
     protected
     Builder(Class<? extends SharedResourceObject> klass, String url) {
       super(klass, url);
     }
 
     public
     Builder(String url) {
       this(RankLibRanker.class, url);
     }
   }
 
   /**
    * Configure a new RankLib Ranker resource.
    *
    * @param resourceUrl the URL where this RankLib model file is located; in the case of a file,
    *                    make sure that the (data) resource URL is prefixed with the "file:" schema
    *                    prefix.)
    */
   public static
   Builder configure(String resourceUrl) {
     return new Builder(resourceUrl);
   }
 
   public
   void load(DataResource data) throws ResourceInitializationException {
     ConfigurationParameterInitializer.initialize(this, data);
     final URI uri = data.getUri();
     if (uri != null) {
       resourceUrl = uri.toString();
     } else {
       throw new ResourceInitializationException();
     }
   }
 
   public
   void afterResourcesInitialized() {
     RankerFactory rf = new RankerFactory();
    ranker = rf.loadRanker(resourceUrl);
     if (ranker == null) throw new RuntimeException(
        "loading ranker model URL '" + resourceUrl + "' failed"
     );
   }
 
   public
   String getResourceName() {
     return resourceName;
   }
 
   public
   Ranker getRanker() {
     return ranker;
   }
 
   public
   RankList rank(RankList rl) {
     return ranker.rank(rl);
   }
 
   public
   List<RankList> rank(List<RankList> rll) {
     return ranker.rank(rll);
   }
 }
