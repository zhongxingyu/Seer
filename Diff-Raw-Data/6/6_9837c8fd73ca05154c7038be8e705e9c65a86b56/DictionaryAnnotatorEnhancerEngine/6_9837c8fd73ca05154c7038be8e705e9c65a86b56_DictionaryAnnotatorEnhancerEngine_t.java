 /*
  * Copyright 2013 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.fusepool.enhancer.engines.dictionaryannotator;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.security.Permission;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import org.w3c.dom.Element;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import opennlp.tools.tokenize.Tokenizer;
 import opennlp.tools.tokenize.TokenizerME;
 import opennlp.tools.tokenize.TokenizerModel;
 import org.apache.clerezza.platform.Constants;
 import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
 import org.apache.clerezza.rdf.core.Language;
 import org.apache.clerezza.rdf.core.LiteralFactory;
 import org.apache.clerezza.rdf.core.MGraph;
 import org.apache.clerezza.rdf.core.TypedLiteral;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.apache.clerezza.rdf.core.access.LockableMGraph;
 import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
 import org.apache.clerezza.rdf.core.access.TcManager;
 import org.apache.clerezza.rdf.core.access.security.TcAccessController;
 import org.apache.clerezza.rdf.core.access.security.TcPermission;
 import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
 import org.apache.clerezza.rdf.core.impl.TripleImpl;
 import org.apache.clerezza.rdf.core.sparql.ParseException;
 import org.apache.clerezza.rdf.core.sparql.QueryParser;
 import org.apache.clerezza.rdf.core.sparql.ResultSet;
 import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
 import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.ConfigurationPolicy;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.PropertyOption;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.stanbol.enhancer.servicesapi.Blob;
 import org.apache.stanbol.enhancer.servicesapi.ContentItem;
 import org.apache.stanbol.enhancer.servicesapi.EngineException;
 import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
 import org.apache.stanbol.enhancer.servicesapi.EnhancementJobManager;
 import org.apache.stanbol.enhancer.servicesapi.InvalidContentException;
 import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
 import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
 import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
 import org.apache.stanbol.enhancer.servicesapi.impl.AbstractEnhancementEngine;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_REFERENCE;
 import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_CONFIDENCE;
 import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_END;
 import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_START;
 import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_LABEL;
 import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_TYPE;
 import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.DC_TYPE;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 @Component(configurationFactory = true, 
     policy = ConfigurationPolicy.OPTIONAL,
     metatype = true, immediate = true, inherit = true)
 @Service
 @Properties(value = {
     @Property(name = EnhancementEngine.PROPERTY_NAME)
 })
 public class DictionaryAnnotatorEnhancerEngine
         extends AbstractEnhancementEngine<RuntimeException,RuntimeException>
         implements EnhancementEngine, ServiceProperties {
 
     @Reference
     ContentGraphProvider cgp;
     
     @Property
     public static final String DESCRIPTION = "eu.fusepool.enhancer.engines.dictionaryannotator.description";
     
     @Property
     public static final String GRAPH_URI = "eu.fusepool.enhancer.engines.dictionaryannotator.graphURI";
     
     @Property
     public static final String LABEL_FIELD = "eu.fusepool.enhancer.engines.dictionaryannotator.labelField";
     
     @Property
     public static final String URI_FIELD = "eu.fusepool.enhancer.engines.dictionaryannotator.URIField";    
     
     @Property(cardinality = 10)
     public static final String ENTITY_PREFIXES = "eu.fusepool.enhancer.engines.dictionaryannotator.entityPrefixes";  
     
     @Property(cardinality = 10)
     public static final String RDF_FIELDS = "eu.fusepool.enhancer.engines.dictionaryannotator.rdfFields"; 
     
     @Property
     public static final String TYPE = "eu.fusepool.enhancer.engines.dictionaryannotator.type";
     
     @Property(options={
         @PropertyOption(value="None",name="None"),
         @PropertyOption(value="Danish",name="Danish"),
         @PropertyOption(value="Dutch",name="Dutch"),
         @PropertyOption(value="English",name="English"),
         @PropertyOption(value="Finnish",name="Finnish"),
         @PropertyOption(value="French",name="French"),
         @PropertyOption(value="German",name="German"),
         @PropertyOption(value="Hungarian",name="Hungarian"),
         @PropertyOption(value="Italian",name="Italian"),
         @PropertyOption(value="Norwegian",name="Norwegian"),
         @PropertyOption(value="Portuguese",name="Portuguese"),
         @PropertyOption(value="Romanian",name="Romanian"),
         @PropertyOption(value="Russian",name="Russian"),
         @PropertyOption(value="Spanish",name="Spanish"),
         @PropertyOption(value="Swedish",name="Swedish"),
         @PropertyOption(value="Turkish",name="Turkish")
         },value="None")
     public static final String STEMMING_LANGUAGE = "eu.fusepool.enhancer.engines.dictionaryannotator.stemmingLanguage";
     
     @Property(boolValue=false)
     public static final String CASE_SENSITIVE = "eu.fusepool.enhancer.engines.dictionaryannotator.caseSensitive";
 
     @Property(intValue=3)
     public static final String CASE_SENSITIVE_LENGTH = "eu.fusepool.enhancer.engines.dictionaryannotator.caseSensitiveLength";
     
     @Property(boolValue=false)
     public static final String ELIMINATE_OVERLAPS = "eu.fusepool.enhancer.engines.dictionaryannotator.eliminateOverlapping";
     
     /**
      * Default value for the {@link Constants#SERVICE_RANKING} used by this engine.
      * This is a negative value to allow easy replacement by this engine depending
      * to a remote service with one that does not have this requirement
      */
     public static final int DEFAULT_SERVICE_RANKING = 100;
     /**
      * The default value for the Execution of this Engine. Currently set to
      * {@link EnhancementJobManager#DEFAULT_ORDER}
      */
     public static final Integer defaultOrder = ORDERING_EXTRACTION_ENHANCEMENT;
     
     /**
      * This contains the MIME type text/plain.
      */
     private static final String TEXT_PLAIN_MIMETYPE = "text/plain";
     /**
      * This contains the MIME type application/rdf+xml.
      */
     private static final String RDF_XML_MIMETYPE = "application/rdf+xml";
     /**
      * Set containing the only supported mime type {@link #TEXT_PLAIN_MIMETYPE}
      */
     private static Set<String> SUPPORTED_MIMTYPES;
     /**
      * This contains the logger.
      */
     private static final Logger log = LoggerFactory.getLogger(DictionaryAnnotatorEnhancerEngine.class);
     
     private DictionaryAnnotator annotator;
     private DictionaryStore dictionary;
     private String description;
     private String graphURI;
     private String labelField;
     private String URIField;
     private List<String> rdfFields;
     private String type;
     private UriRef typeURI;
     private List<String> entityPrefixes;
     private String stemmingLanguage;
     private Boolean caseSensitive;
     private Integer caseSensitiveLength;
     private Boolean eliminateOverlapping;
     private Tokenizer tokenizer;
     
     @Activate
     @Override
     protected void activate(ComponentContext context) throws ConfigurationException {
         super.activate(context);
 
         SUPPORTED_MIMTYPES = new HashSet();
         SUPPORTED_MIMTYPES.add(TEXT_PLAIN_MIMETYPE);
         SUPPORTED_MIMTYPES.add(RDF_XML_MIMETYPE);
         
         //Read configuration
         if (context != null) {
             Dictionary<String,Object> config = context.getProperties();
             
             Object gu = config.get(GRAPH_URI);
             graphURI = gu == null || gu.toString().isEmpty() ? null : gu.toString();
 
             Object lf = config.get(LABEL_FIELD);
             labelField = lf == null || lf.toString().isEmpty() ? null : lf.toString();
             
             Object uf = config.get(URI_FIELD);
             URIField = uf == null || uf.toString().isEmpty() ? null : uf.toString();
             
             Object ep = config.get(ENTITY_PREFIXES);
             if (ep instanceof Iterable<?>) {
                 entityPrefixes = new ArrayList<String>();
                 for (Object o : (Iterable<Object>) ep) {
                     if (o != null && !o.toString().isEmpty()) {
                         entityPrefixes.add(o.toString());
                     } else {
                         log.warn("Entity prefixes configuration '{}' contained illegal value '{}' -> removed", ep, o);
                     }
                 }
             } else if (ep.getClass().isArray()) {
                 entityPrefixes = new ArrayList<String>();
                 for (Object modelObj : (Object[]) ep) {
                     if (modelObj != null) {
                         entityPrefixes.add(modelObj.toString());
                     } else {
                         log.warn("Entity prefixes configuration '{}' contained illegal value '{}' -> removed",
                                 Arrays.toString((Object[]) ep), ep);
                     }
                 }
             } else {
                 entityPrefixes = null;
             }
 
             Object rf = config.get(RDF_FIELDS);         
             if (rf instanceof Iterable<?>) {
                 rdfFields = new ArrayList<String>();
                 for (Object o : (Iterable<Object>) rf) {
                     if (o != null && !o.toString().isEmpty()) {
                         rdfFields.add(o.toString());
                     } else {
                         log.warn("Entity prefixes configuration '{}' contained illegal value '{}' -> removed", rf, o);
                     }
                 }
             } else if (rf.getClass().isArray()) {
                 rdfFields = new ArrayList<String>();
                 for (Object modelObj : (Object[]) rf) {
                     if (modelObj != null) {
                         rdfFields.add(modelObj.toString());
                     } else {
                         log.warn("Entity prefixes configuration '{}' contained illegal value '{}' -> removed",
                                 Arrays.toString((Object[]) rf), rf);
                     }
                 }
             } else {
                 rdfFields = null;
             }
                   
             Object t = config.get(TYPE);        
             if(t != null || !t.toString().isEmpty())
             {
                 String[] val = t.toString().split(";", 2);
                 type = val[0];
                 typeURI = new UriRef(val[1]);
             }
 
             Object sl = config.get(STEMMING_LANGUAGE);
             stemmingLanguage = sl == null ? "None" : sl.toString();
             
             Object cs = config.get(CASE_SENSITIVE);
             caseSensitive = cs == null || cs.toString().isEmpty() ? null : (Boolean) cs;
             
             Object csl = config.get(CASE_SENSITIVE_LENGTH);
             caseSensitiveLength = csl == null || csl.toString().isEmpty() ? null : (Integer) csl;
 
             Object eo = config.get(ELIMINATE_OVERLAPS);
             eliminateOverlapping = eo == null || eo.toString().isEmpty() ? null : (Boolean) eo;
         }        
 
         //Loading tokenizer model
         InputStream modelTokIn;
         TokenizerModel modelTok;
         try {
             modelTokIn = this.getClass().getResourceAsStream("/models/en-token.bin");
             modelTok = new TokenizerModel(modelTokIn);
             tokenizer = new TokenizerME(modelTok);
         } catch (FileNotFoundException ex) {
             log.error("Error while loading tokenizer model: {}", ex.getMessage());
         } catch (IOException ex) {
             log.error("Error while loading tokenizer model: {}", ex.getMessage());
         }
         if(tokenizer == null){
             log.error("Tokenizer cannot be NULL");
         }
         
         //Concatenating SPARQL query
         String sparqlQuery = "";
         for (String prefix : entityPrefixes) {
             sparqlQuery += "PREFIX " + prefix + "\n";
         }
         sparqlQuery += "SELECT distinct ?uri ?label WHERE { "
                 + "?uri a " + URIField + " ."   
                 + "?uri " + labelField + " ?label ."
                 + " }";
 
         try {
             //Get TcManager
             TcManager tcManager = TcManager.getInstance();
             TcAccessController tca;
             
             //Get the graph by its URI
             LockableMGraph graph = null;
             try {
                 //graph = tcManager.getMGraph(new UriRef(graphURI));
                 graph = tcManager.getMGraph(new UriRef(graphURI));
                 tca = new TcAccessController(tcManager);
                 tca.setRequiredReadPermissions(new UriRef(graphURI),Collections.singleton((Permission)new TcPermission(
                     "urn:x-localinstance:/content.graph", "read"))
                 );
                cgp.addTemporaryAdditionGraph(new UriRef(graphURI));
             } catch (NoSuchEntityException e) {
                 log.error("Enhancement Graph must be existing", e);
             }
 
             //Parse the SPARQL query
             SelectQuery selectQuery = null;
             try {
                 selectQuery = (SelectQuery) QueryParser.getInstance().parse(sparqlQuery);
             } catch (ParseException e) {
                 log.error("Cannot parse the SPARQL query", e);
             }
             
             if (graph != null) {
                 Lock l = graph.getLock().readLock();
                 l.lock();
                 try{
                     //Execute the SPARQL query
                     ResultSet resultSet = tcManager.executeSparqlQuery(selectQuery, graph);
                     
                     //ResultSet resultSet = (ResultSet) tcManager.executeSparqlQuery(sparqlQuery, graph);
                     dictionary = new DictionaryStore();
                     while (resultSet.hasNext()) {
                         SolutionMapping mapping = resultSet.next();
                         try{
                             TypedLiteral label = (TypedLiteral) mapping.get("label");
                             UriRef uri = (UriRef) mapping.get("uri");
                             dictionary.AddOriginalElement(label.getLexicalForm(), uri);
                         }catch(Exception e){
                             continue;
                         }
                     }
                     
                     long start, end;
                     start = System.currentTimeMillis();
                     System.err.print("Loading dictionary from " + graphURI + " (" + dictionary.keywords.size() + ") and creating search trie ...");
                     annotator = new DictionaryAnnotator(dictionary, tokenizer, stemmingLanguage, caseSensitive, caseSensitiveLength, eliminateOverlapping);
                     end = System.currentTimeMillis();
                     System.err.println(" done [" + Double.toString((double)(end - start)/1000) + " sec] .");
                     log.info("Loading dictionary from " + graphURI + " (" + dictionary.keywords.size() + ") and creating search trie ... done [" + Double.toString((double)(end - start)/1000) + " sec] .");
                 } finally {
                     l.unlock();
                 }
             } else {
                 log.error("There is no registered graph with given uri: " + graphURI);
                 System.out.println("There is no registered graph with given uri: " + graphURI);
             }
         } catch(Exception e) {
             log.error("Error happened while creating the dictionary!",e);
             System.err.println("Error happened while creating the dictionary!");
             e.printStackTrace();
         }
     }
 
     @Deactivate
     @Override
     protected void deactivate(ComponentContext context) {
         super.deactivate(context);
        cgp.removeTemporaryAdditionGraph(new UriRef(graphURI));
         annotator = null;
     }
 
     @Override
     public int canEnhance(ContentItem ci) throws EngineException {
         return ENHANCE_ASYNC;
     }
 
     @Override
     public void computeEnhancements(ContentItem ci) throws EngineException {
         Map.Entry<UriRef, Blob> contentPart = null;
         List<Entity> entities = null;
         String text = "";
         List<String> texts = new ArrayList<String>();
         Document rdf = null;
         Element rootElement;
         
         if(TEXT_PLAIN_MIMETYPE.equals(ci.getMimeType())){
             contentPart = ContentItemHelper.getBlob(ci, SUPPORTED_MIMTYPES);
             
             try {
                 text = ContentItemHelper.getText(contentPart.getValue());
             } catch (IOException e) {
                 throw new InvalidContentException(this, ci, e);
             }
             if (text.trim().length() == 0) {
                 log.info("No text contained in ContentPart {} of ContentItem {}", contentPart.getKey(), ci.getUri());
                 return;
             }
 
             try {
                 entities = annotator.Annotate(text);
                 log.info("entities identified: {}", entities);
             } catch (Exception e) {
                 log.warn("Could not recognize entities", e);
                 return;
             }
         }
         else if(RDF_XML_MIMETYPE.equals(ci.getMimeType())){
             contentPart = ContentItemHelper.getBlob(ci, SUPPORTED_MIMTYPES);
 
             try {
                 text = ContentItemHelper.getText(contentPart.getValue());
                 rdf = this.stringToDom(text);
                 rootElement = rdf.getDocumentElement();
                 
                 for (String field : rdfFields) {
                     List<String> currentText = this.findTag(field, rootElement);
                     if(currentText != null){
                         texts.addAll(currentText);
                     }
                     else{
                         log.warn("RDF label '" + field + "' was not found in ContentItem {}",ci.getUri());
                     }
                 }
             } catch (Exception e) {
                 throw new InvalidContentException(this, ci, e);
             }
 
             try {
                 entities = new ArrayList<Entity>();
                 for (String textItem : texts) {
                     List<Entity> currentEntities = annotator.Annotate(textItem);
                     if (currentEntities != null) {
                         entities.addAll(currentEntities);
                     }
                 }
             } catch (Exception e) {
                 log.warn("Could not recognize entities", e);
                 e.printStackTrace();
                 return;
             }
         }
         
         if (contentPart == null) {
             throw new IllegalStateException("No ContentPart with Mimetype '"
                     + TEXT_PLAIN_MIMETYPE + "' or '" + RDF_XML_MIMETYPE + "' found for ContentItem " + ci.getUri()
                     + ": This is also checked in the canEnhance method! -> This "
                     + "indicated an Bug in the implementation of the "
                     + "EnhancementJobManager!");
         }
 
 
         // Add entities to metadata
         if (entities != null) {
             LiteralFactory literalFactory = LiteralFactory.getInstance();
             MGraph g = ci.getMetadata();
             ci.getLock().writeLock().lock();
             try {
                 Language language = new Language("en");
 
                 for (Entity e : entities) {
                     UriRef textEnhancement = EnhancementEngineHelper.createTextEnhancement(ci, this);
                     g.add(new TripleImpl(textEnhancement, ENHANCER_CONFIDENCE, literalFactory.createTypedLiteral(e.score)));
                     g.add(new TripleImpl(textEnhancement, DC_TYPE, typeURI));
                     g.add(new TripleImpl(textEnhancement, ENHANCER_ENTITY_TYPE, new PlainLiteralImpl(type))); 
                     g.add(new TripleImpl(textEnhancement, ENHANCER_ENTITY_LABEL, new PlainLiteralImpl(e.label,language)));
                     g.add(new TripleImpl(textEnhancement, ENHANCER_START, new PlainLiteralImpl(Integer.toString(e.begin))));
                     g.add(new TripleImpl(textEnhancement, ENHANCER_END, new PlainLiteralImpl(Integer.toString(e.end))));
                     g.add(new TripleImpl(textEnhancement, ENHANCER_ENTITY_REFERENCE, e.uri));
                     g.add(new TripleImpl(e.uri, org.apache.clerezza.rdf.ontologies.RDF.type, typeURI));
                     g.add(new TripleImpl(e.uri, org.apache.clerezza.rdf.ontologies.RDFS.label, new PlainLiteralImpl(e.label,language)));   
                 }
             } finally {
                 ci.getLock().writeLock().unlock();
             }
         }
     }
 
     @Override
     public Map<String, Object> getServiceProperties() {
         return Collections.unmodifiableMap(Collections.singletonMap(ENHANCEMENT_ENGINE_ORDERING, (Object) defaultOrder));
     } 
     
     public Document stringToDom(String xmlSource) 
             throws SAXException, ParserConfigurationException, IOException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder.parse(new InputSource(new StringReader(xmlSource)));
     }
     
     public List<String> findTag(String tagName, Element element) {
         List<String> resultSet = null;
         NodeList list = element.getElementsByTagName(tagName);
         //System.out.println(list.getLength());
         
         try{
             if (list != null && list.getLength() > 0) {
                 resultSet = new ArrayList<String>();
                 for (int i = 0; i < list.getLength(); i++) {
                     Element e = (Element) list.item(i);
                     if (e.getAttribute("xml:lang").equals("en") || e.getAttribute("xml:lang").equals("")) {
                         resultSet.add(innerXml(list.item(i)));
                     }
                 }
             }
             return resultSet;
         }catch(IOException ex){
             return null;
         }
     }
     
     public String innerXml(Node node) throws IOException {
         DOMImplementationLS lsImpl = 
                 (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
         LSSerializer lsSerializer = lsImpl.createLSSerializer();
         lsSerializer.getDomConfig().setParameter("xml-declaration", false);
         NodeList childNodes = node.getChildNodes();
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < childNodes.getLength(); i++) {
             sb.append(lsSerializer.writeToString(childNodes.item(i)));
         }
         Html2Text parser = new Html2Text();
         parser.parse(new StringReader(sb.toString()));
         return parser.getText();
     }
 }
