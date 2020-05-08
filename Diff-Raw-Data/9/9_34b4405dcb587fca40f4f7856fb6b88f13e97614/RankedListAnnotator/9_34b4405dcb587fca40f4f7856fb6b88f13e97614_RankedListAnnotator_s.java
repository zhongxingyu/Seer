 package txtfnnl.uima.analysis_component;
 
 import ciir.umass.edu.learning.DataPoint;
 import ciir.umass.edu.learning.RankList;
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.AnalysisComponent;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.descriptor.ExternalResource;
 import txtfnnl.uima.AnalysisComponentBuilder;
 import txtfnnl.uima.cas.Property;
 import txtfnnl.uima.resource.RankerResource;
 import txtfnnl.uima.tcas.TextAnnotation;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract
 class RankedListAnnotator extends JCasAnnotator_ImplBase {
   /** The URI of this Annotator. */
   public static final String URI = RankedListAnnotator.class.getName();
 
   /** A shared, external ranker resource implementation to use by this annotator. */
   public static final String MODEL_KEY_RANKER_RESOURCE = "RankerResource";
   @ExternalResource(key = MODEL_KEY_RANKER_RESOURCE, mandatory = false)
   private RankerResource ranker;
 
   /** The Annotator's logger instance. */
   protected Logger logger;
 
   /** The name of the {@link Property} to add to the annotations with the rank result. */
   public static final String RANK_PROPERTY = "rank";
 
   public static final String PARAM_IDENTIFIER = "Identifier";
   @ConfigurationParameter(name = PARAM_IDENTIFIER, description = "ID of the annotations to rank")
   private String identifier;
 
   public static final String PARAM_NAMESPACE = "Namespace";
   @ConfigurationParameter(name = PARAM_NAMESPACE,
                           description = "namespace of the annotations to rank")
   private String namespace;
 
   public static final String PARAM_ANNOTATOR_URI = "AnnotatorUri";
   @ConfigurationParameter(name = PARAM_ANNOTATOR_URI,
                           description = "URI of the annotator that provided the annotations to rank")
   private String annotatorUri;
 
 
   public static
   class Builder extends AnalysisComponentBuilder {
     protected
     Builder(Class<? extends AnalysisComponent> klass,
             ExternalResourceDescription rankerResourceDescription) {
       super(klass);
       setRequiredParameter(MODEL_KEY_RANKER_RESOURCE, rankerResourceDescription);
     }
 
     public
     Builder(ExternalResourceDescription rankerResourceDescription) {
       this(RankedListAnnotator.class, rankerResourceDescription);
     }
 
     public
     Builder setIdentifier(String id) {
       setOptionalParameter(PARAM_IDENTIFIER, id);
       return this;
     }
 
     public
     Builder setNamespace(String ns) {
       setOptionalParameter(PARAM_NAMESPACE, ns);
       return this;
     }
 
     public
     Builder setAnnotatorUri(String uri) {
       setOptionalParameter(PARAM_ANNOTATOR_URI, uri);
       return this;
     }
   }
 
   public static
   Builder configure(ExternalResourceDescription rankerResourceDescription) {
     return new Builder(rankerResourceDescription);
   }
 
   @Override
   public
   void initialize(UimaContext ctx) throws ResourceInitializationException {
     super.initialize(ctx);
     logger = ctx.getLogger();
     logger.log(Level.CONFIG, "RankedListAnnotator initialized");
   }
 
   @Override
   public
   void process(JCas jcas) throws AnalysisEngineProcessException {
     RankList rl = getRankedList(jcas, "1"); // XXX: could use article ID as QID if useful?
     process(jcas, ranker.rank(rl));
   }
 
   /**
    * A default {@link RankList} processing implementation that expects the value of {@link
    * TextAnnotation#toString()} as the {@link ciir.umass.edu.learning.DataPoint#getDescription()
    * data point key} to identify the {@link TextAnnotation annotations} to which the computed rank
    * should be added as a {@link Property}.
    */
   protected
   void process(JCas jcas, RankList rl) {
     Map<String, String> ranks = new HashMap<String, String>();
     for (int i = 0; i < rl.size(); ++i) {
       DataPoint dp = rl.get(i);
      ranks.put(dp.getDescription(), String.format("%f", dp.getLabel()));
     }
     FSIterator<Annotation> it = getAnnotationIterator(jcas);
     while (it.hasNext()) {
       TextAnnotation ann = (TextAnnotation) it.next();
       String key = ann.toString();
       if (ranks.containsKey(key)) {
         Property rank = new Property(jcas);
         rank.setName(RANK_PROPERTY);
         rank.setValue(ranks.get(key));
         ann.addProperty(jcas, rank);
       }
     }
   }
 
   protected
   FSIterator<Annotation> getAnnotationIterator(JCas jcas) {
     return jcas.createFilteredIterator(
         TextAnnotation.getIterator(jcas),
         TextAnnotation.makeConstraint(jcas, annotatorUri, namespace, identifier)
     );
   }
 
   protected abstract
   RankList getRankedList(JCas jcas, String qid);
 
   protected
   DataPoint makeDataPoint(float label, String qid, String key, double... features) {
     StringBuilder rawData = new StringBuilder();
     rawData.append(String.format("%f", label));
     rawData.append(" qid:");
     rawData.append(qid);
     rawData.append(" ");
     for (int i = 0; i < features.length; i++)
       rawData.append(String.format("%d:%.8f ", i + 1, features[i]));
     rawData.append("# ");
     rawData.append(key);
     return new DataPoint(rawData.toString());
   }
 
   @Override
   public
   void destroy() {
     super.destroy();
     logger.log(Level.CONFIG, "RankedListAnnotator destroyed");
   }
 }
