 package txtfnnl.uima.analysis_component;
 
 import java.io.File;
 
 import martin.common.ArgParser;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.AnalysisComponent;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 
 import txtfnnl.uima.AnalysisComponentBuilder;
 import txtfnnl.uima.tcas.SemanticAnnotation;
 
 import uk.ac.man.entitytagger.EntityTagger;
 import uk.ac.man.entitytagger.Mention;
 import uk.ac.man.entitytagger.matching.Matcher;
 
 public class LinnaeusAnnotator extends JCasAnnotator_ImplBase {
   public static final String URI = LinnaeusAnnotator.class.getName();
   public static final String NAMESPACE = "http://www.uniprot.org/taxonomy/"; // TODO
   public static final String PARAM_CONFIG_FILE_PATH = "ConfigFilePath";
   @ConfigurationParameter(name = PARAM_CONFIG_FILE_PATH,
       description = "A property file path with Linnaeus configuration data.")
   private String configFilePath;
   private Logger logger;
   private Matcher linnaeus;
 
   public static class Builder extends AnalysisComponentBuilder {
     protected Builder(Class<? extends AnalysisComponent> klass, File configFile) {
       super(klass);
       setConfigurationFilePath(configFile);
     }
 
     public Builder(File configFile) {
       this(LinnaeusAnnotator.class, configFile);
     }
 
     public Builder setConfigurationFilePath(File configFile) {
       setRequiredParameter(PARAM_CONFIG_FILE_PATH, configFile.getAbsolutePath());
       return this;
     }
   }
 
   public static Builder configure(File configFile) {
     return new Builder(configFile);
   }
 
   @Override
   public void initialize(UimaContext ctx) throws ResourceInitializationException {
     super.initialize(ctx);
     logger = ctx.getLogger();
     logger.log(Level.INFO, "reading configuration file at " + configFilePath);
     ArgParser ap = new ArgParser(new String[] { "--properties", configFilePath });
     java.util.logging.Logger l = java.util.logging.Logger.getLogger("Linnaeus");
     l.setLevel(java.util.logging.Level.WARNING);
     linnaeus = EntityTagger.getMatcher(ap, l);
   }
 
   @Override
   public void process(JCas cas) throws AnalysisEngineProcessException {
     int countMentions = 0;
     int countIds = 0;
     for (Mention mention : linnaeus.match(cas.getDocumentText())) {
       ++countMentions;
       String[] ids = mention.getIds();
       Double[] probs = mention.getProbabilities();
       for (int i = ids.length - 1; i > -1; --i) {
         ++countIds;
        // Linnaeus sets p to NULL in some cases, so:
        if (probs[i] == null) probs[i] = 1.0 / ((double) probs.length);
         SemanticAnnotation species = new SemanticAnnotation(cas, mention.getStart(),
             mention.getEnd());
         species.setAnnotator(URI);
         species.setConfidence(probs[i]);
         species.setIdentifier(ids[i]);
         species.setNamespace(NAMESPACE);
         species.addToIndexes();
       }
     }
     logger.log(Level.FINE, "tagged {0} mentions with {1} IDs", new Object[] { countMentions,
         countIds });
   }
 }
