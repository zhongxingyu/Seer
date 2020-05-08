 /**
  *
  */
 package eu.fusepool.linking.silk;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import org.apache.clerezza.rdf.core.TripleCollection;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.ConfigurationPolicy;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import eu.fusepool.datalifecycle.Interlinker;
 import eu.fusepool.java.silk.client.SilkClient;
 import eu.fusepool.java.silk.client.SilkException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Set;
 import java.util.UUID;
 import org.apache.clerezza.rdf.core.MGraph;
 import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
 import org.apache.clerezza.rdf.core.serializedform.Parser;
 import org.apache.clerezza.rdf.core.serializedform.Serializer;
 import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.felix.scr.annotations.Reference;
 
 /**
  * @author giorgio
  * @author luigi
  *
  */
 public abstract class SilkInterlinker implements Interlinker {
 
     private static Logger logger = LoggerFactory.getLogger(SilkInterlinker.class);
 
     /**
      * Default value for the {@link Constants#SERVICE_RANKING} used by this
      * engine. This is a negative value to allow easy replacement by this engine
      * depending to a remote service with one that does not have this
      * requirement   
      */
     public static final int DEFAULT_SERVICE_RANKING = 101;
 
     /**
      * Component configuration
      * - Sparql endpoint
      * - instance name (this property is used to select the link specifications (config file)
      */
     public static final String DEFAULT_SPARQL_ENDPOINT = "http://localhost:8080/sparql";
     //public static final String DEFAULT_SPARQL_ENDPOINT = "http://platform.fusepool.info/sparql";
     // Label for the component configuration panel
     public static final String SPARQL_ENDPOINT_LABEL = "SPARQL endpoint";
     // Component name in the configuration panel
     public static final String SPARQL_ENDPOINT_NAME = "endpoint";
     // Component description in the config panel
     public static final String SPARQL_ENDPOINT_DESCRIPTION = "SPARQL endpoint of the target (master) repository";
     // sparql endpoint variable set at the bundle activation from the component configuration panel
     String sparqlEndpoint = SilkInterlinker.DEFAULT_SPARQL_ENDPOINT;
     
 
     protected ComponentContext componentContext;
     protected BundleContext bundleContext;
 
     // properties file containing a list of all the silk configuration files available
     java.util.Properties linkSpecList = null;
 
     String sparqlGraph = "";
 
     @Activate
     protected void activate(ComponentContext ce) throws IOException,
             ConfigurationException {
         this.componentContext = ce;
         this.bundleContext = ce.getBundleContext();
 
         @SuppressWarnings("rawtypes")
         Dictionary dict = ce.getProperties();
         // set the endpoint from the component configuration
         Object endpointObj = dict.get(SilkInterlinker.SPARQL_ENDPOINT_NAME);
         if (endpointObj != null && !"".equals(endpointObj.toString())) {
             sparqlEndpoint = endpointObj.toString();
         }
 
         logger.info("The {} Silk Linking Service is being activated. SPARQL endpoint: {}", getName(), sparqlEndpoint);
 
     }
 
     @Deactivate
     protected void deactivate(ComponentContext context) {
         logger.info("The Silk Linking Service is being deactivated");
     }
 
     public TripleCollection interlink(TripleCollection dataToInterlink, UriRef targetGraphRef) {
         SilkJob job = new SilkJob(bundleContext, sparqlEndpoint, targetGraphRef);
         return job.executeJob(dataToInterlink);
     }
     
     // name set in component configuration panel
     public abstract String getName();
     
     protected abstract InputStream getConfigTemplate();
     
     protected abstract Parser getParser();
 
     protected abstract Serializer getSerializer();
 
     protected abstract SilkClient getSilk();
     
     public class SilkJob {
 
         private static final String SOURCE_RDF_FILE_TAG = "[SOURCE_RDF_FILE]";
         private static final String ACCEPT_LINKS_FILE_PATH_TAG = "[ACCEPT_LINKS_FILE_PATH]";
         private static final String TARGET_ENDPOINT_TAG = "[TARGET_ENDPOINT]";
         private static final String TARGET_GRAPH_TAG = "[TARGET_GRAPH]";
 
         // sparql endpoint
         String sparqlEndpoint;
         
         // target graph to be set in the silk config file
         String targetGraph;
 
         private static final boolean holdDataFiles = true;
 
         protected BundleContext bundleContext;
 
         // id of the interlinking job
         private String jobId;
         
         
         File rdfData; // source RDF data
         File outputData; //accepted intelinks file 
 
         public SilkJob(BundleContext ctx, String endpoint, UriRef graphRef) {
             bundleContext = ctx;
             this.sparqlEndpoint = endpoint;
             this.targetGraph = graphRef.getUnicodeString();
             logger.info("Silk job started. Endpoint: " + sparqlEndpoint + ", graph: " + targetGraph);
         }
 
         @SuppressWarnings("unused")
         public MGraph executeJob(TripleCollection dataToInterlink) {
 
             jobId = UUID.randomUUID().toString();
             logger.info("executing job " + jobId);
             try {
                 TripleCollection inputGraph = null;
                 createTempFiles();
                 // serialize the source graph in a file
                 logger.info("Serializing the source graph of size " + dataToInterlink.size() + " in a file");
                 OutputStream rdfOS = new FileOutputStream(rdfData);
                 getSerializer().serialize(rdfOS, dataToInterlink, SupportedFormat.RDF_XML);
                 rdfOS.close();
                 logger.info("Source graph serialization completed.");
                 logger.info("Building Silk config file with Sparql endpoint and target graph");
                 String silkConfig = buildConfig();
                 logger.info("Executing all link specifications in the SILK config file");
                 // execute all the link specifications in the silk config file (set the 2nd argument to null)
                 getSilk().executeStream(IOUtils.toInputStream(silkConfig, "UTF-8"), null, 1, true);
 
 			    // This graph will contain the results of the duplicate detection
                 // i.e. owl:sameAs statements
                 MGraph owlSameAsStatements = new SimpleMGraph();
                 InputStream is = new FileInputStream(outputData);
                 getParser().parse(owlSameAsStatements, is, SupportedFormat.N_TRIPLE);
                 is.close();
                 
                // remove triples that can be inferred by a reasoner by reflexivity 
                // or symmetry of owl:sameAs
                LinkUtils.removeInferenceableEquivalences(owlSameAsStatements);
                
                 logger.info(owlSameAsStatements.size() + " triples extracted by job: " + jobId);
                 
                 return owlSameAsStatements;
 
             } catch (IOException e) {
                 throw new RuntimeException("Exception creating the silk files", e);
             } catch (SilkException e) {
                 throw new RuntimeException(e);
             } finally {
                 cleanUpFiles();
             }
         }
 
         private void createTempFiles() {
             String ifName = jobId + "-in.rdf";
             String ofName = jobId + "-out.rdf";
             rdfData = bundleContext.getDataFile(ifName);
             outputData = bundleContext.getDataFile(ofName);
         }
 
         /**
          * Builds the configuration for the silk job. The template config file is selected by
          * the name of the instance from a list in a properties file.
          *
          * @throws IOException
          */
         private String buildConfig() throws IOException {
             // Load the template config file to be updated with endpoint and graph set in the component configuration panel
             //InputStream cfgIs = this.getClass().getResourceAsStream(linkSpecList.getProperty(instanceName));
             InputStream cfgIs = getConfigTemplate();
             String silkConfig = IOUtils.toString(cfgIs, "UTF-8");
             silkConfig = StringUtils.replace(silkConfig, TARGET_ENDPOINT_TAG, this.sparqlEndpoint);
             
             if (this.targetGraph != null && !"".equals(this.targetGraph)) {
                 
                 silkConfig = StringUtils.replace(silkConfig, TARGET_GRAPH_TAG, this.targetGraph);
                 
             } else {
                 silkConfig = StringUtils.replace(silkConfig, TARGET_GRAPH_TAG, "");
             }
             
             silkConfig = StringUtils.replace(silkConfig, SOURCE_RDF_FILE_TAG, rdfData.getAbsolutePath());
             silkConfig = StringUtils.replace(silkConfig, ACCEPT_LINKS_FILE_PATH_TAG, outputData.getAbsolutePath());
             
             logger.info("Silk configuration file built for the job:" + jobId + "\n" + silkConfig) ;
             
             return silkConfig;
             
         }
 
         /**
          * Removes temporary files
          */
         private void cleanUpFiles() {
             if (holdDataFiles) {
                 return;
             }
             if (rdfData != null) {
                 rdfData.delete();
             }
             if (outputData != null) {
                 outputData.delete();
             }
         }
 
         /**
          * @return the sparqlEndpoint
          */
         public String getSparqlEndpoint() {
             return sparqlEndpoint;
         }
 
         /**
          * @param sparqlEndpoint the sparqlEndpoint to set
          */
         public void setSparqlEndpoint(String sparqlEndpoint) {
             this.sparqlEndpoint = sparqlEndpoint;
         }
 
         /**
          * @return the sparqlGraph
          */
         public String getSparqlGraph() {
             return sparqlGraph;
         }
 
         /**
          * @param graph name the target graph to be set in silk config file. 
          */
         public void setSparqlGraph(String graphName) {
             this.targetGraph = graphName;
         }
     }
 }
