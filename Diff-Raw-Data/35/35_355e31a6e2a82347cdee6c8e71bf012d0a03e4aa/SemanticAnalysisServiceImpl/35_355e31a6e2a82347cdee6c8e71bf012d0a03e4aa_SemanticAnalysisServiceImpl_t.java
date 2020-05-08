 package org.nuxeo.ecm.platform.semanticentities.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.nuxeo.common.utils.StringUtils;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
 import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
 import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;
 import org.nuxeo.ecm.core.api.repository.Repository;
 import org.nuxeo.ecm.core.api.repository.RepositoryManager;
 import org.nuxeo.ecm.core.convert.api.ConversionService;
 import org.nuxeo.ecm.core.schema.FacetNames;
 import org.nuxeo.ecm.core.schema.SchemaManager;
 import org.nuxeo.ecm.core.utils.BlobsExtractor;
 import org.nuxeo.ecm.platform.semanticentities.AnalysisTask;
 import org.nuxeo.ecm.platform.semanticentities.Constants;
 import org.nuxeo.ecm.platform.semanticentities.DereferencingException;
 import org.nuxeo.ecm.platform.semanticentities.EntitySuggestion;
 import org.nuxeo.ecm.platform.semanticentities.LocalEntityService;
 import org.nuxeo.ecm.platform.semanticentities.SemanticAnalysisService;
 import org.nuxeo.ecm.platform.semanticentities.SerializationTask;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceGroup;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceInfo;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.ComponentContext;
 import org.nuxeo.runtime.model.DefaultComponent;
 import org.nuxeo.runtime.transaction.TransactionHelper;
 
 import com.google.common.collect.MapMaker;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 
 public class SemanticAnalysisServiceImpl extends DefaultComponent implements SemanticAnalysisService {
 
     private static final Log log = LogFactory.getLog(SemanticAnalysisServiceImpl.class);
 
     protected final Map<DocumentRef,String> states = new MapMaker().concurrencyLevel(4)
             .expiration(10, TimeUnit.MINUTES).makeMap();
 
     private static final String ANY2TEXT = "any2text";
 
     protected static final String DEFAULT_ENGINE_URL = "https://stanbol.demo.nuxeo.com/engines";
 
     protected static final String ENGINE_URL_PROPERTY = "org.nuxeo.ecm.platform.semanticentities.stanbolUrl";
 
     protected static final String DEFAULT_SPARQL_QUERY = "SELECT ?label ?type ?context ";
 
     protected static final String DEFAULT_SOURCE_NAME = "dbpedia";
 
     protected static final String DEFAULT_ENGINE_OUTPUT_FORMAT = "application/rdf+xml";
 
     // TODO: turn the following fields into configurable parameters set by a contribution to an extension
     // point
 
     protected String sparqlQuery = DEFAULT_SPARQL_QUERY;
 
     protected String sourceName = DEFAULT_SOURCE_NAME;
 
     protected String outputFormat = DEFAULT_ENGINE_OUTPUT_FORMAT;
 
     protected boolean linkToUnrecognizedEntities = true;
 
     protected boolean linkToAmbiguousEntities = true;
 
     protected boolean linkShortPersonNames = false;
 
     // use framework property by default and fallback to public nuxeo instance
     protected String engineURL = null;
 
     protected HttpClient httpClient;
 
     protected ConversionService conversionService;
 
     protected LocalEntityService leService;
 
     protected PathSegmentService pathService;
 
     protected SchemaManager schemaManager;
 
     protected BlockingQueue<Runnable> analysisTaskQueue;
 
     protected ThreadPoolExecutor analysisExecutor;
 
     protected BlockingQueue<SerializationTask> serializationTaskQueue;
 
     protected boolean serializerActive;
 
     // TODO: make the following configurable using an extension point
     protected static final Map<String,String> localTypes = new HashMap<String,String>();
     static {
         localTypes.put("http://dbpedia.org/ontology/Place", "Place");
         localTypes.put("http://dbpedia.org/ontology/Person", "Person");
         localTypes.put("http://dbpedia.org/ontology/Organisation", "Organization");
     }
 
     @Override
     public void activate(ComponentContext context) throws Exception {
         super.activate(context);
         conversionService = Framework.getService(ConversionService.class);
         leService = Framework.getService(LocalEntityService.class);
         pathService = Framework.getService(PathSegmentService.class);
         schemaManager = Framework.getService(SchemaManager.class);
         initHttpClient();
 
         analysisTaskQueue = new LinkedBlockingQueue<Runnable>();
        analysisExecutor = new ThreadPoolExecutor(10, 10, 5, TimeUnit.MINUTES, analysisTaskQueue);
         serializationTaskQueue = new LinkedBlockingQueue<SerializationTask>();
         serializerActive = true;
         Thread serializer = new Thread() {
             @Override
             public void run() {
                 RepositoryManager manager = null;
                 try {
                     manager = Framework.getService(RepositoryManager.class);
                 } catch (Exception e) {
                     log.error(e, e);
                     serializerActive = false;
                 }
                 while (serializerActive) {
                     try {
                         SerializationTask task = serializationTaskQueue.take();
                         if (task.isLastTask()) {
                             serializerActive = false;
                             break;
                         }
                         TransactionHelper.startTransaction();
                        LoginContext lc = null;
                         try {
                            lc = Framework.login();
                             CoreSession session = manager.getRepository(task.getRepositoryName()).open();
                             try {
                                 createLinks(session.getDocument(task.getDocumentRef()), session,
                                     task.getOccurrenceGroups());
                             } finally {
                                 Repository.close(session);
                             }
                         } catch (Exception e) {
                             TransactionHelper.setTransactionRollbackOnly();
                             log.error(e.getMessage(), e);
                         } finally {
                             states.remove(task.getDocumentRef());
                            if (lc != null) {
                                try {
                                    lc.logout();
                                } catch (LoginException e) {
                                    log.error(e, e);
                                }
                            }
                             TransactionHelper.commitOrRollbackTransaction();
                         }
                     } catch (InterruptedException e) {
                         log.error(e.getMessage(), e);
                     }
 
                 }
             }
         };
         serializer.start();
     }
 
     @Override
     public void scheduleSerializationTask(SerializationTask task) {
         serializationTaskQueue.add(task);
     }
 
     protected void initHttpClient() {
         // Create and initialize a scheme registry
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 
         // Create an HttpClient with the ThreadSafeClientConnManager.
         // This connection manager must be used if more than one thread will
         // be using the HttpClient.
         HttpParams params = new BasicHttpParams();
         ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
 
         httpClient = new DefaultHttpClient(cm, params);
     }
 
     protected boolean shouldSkip(DocumentModel doc) throws PropertyException, ClientException {
         if (schemaManager.getDocumentTypeNamesExtending(Constants.ENTITY_TYPE).contains(doc.getType())
             || schemaManager.getDocumentTypeNamesExtending(Constants.OCCURRENCE_TYPE).contains(doc.getType())) {
             // do not try to analyze local entities themselves
             return true;
         }
 
         String lang = doc.getProperty("dc:language").getValue(String.class);
         if (lang != null && !lang.isEmpty() && !"en".equalsIgnoreCase(lang)
             && !"english".equalsIgnoreCase(lang)) {
             // XXX: temporary hack!
             // skip documents explicitly detected in a non English language; to
             // be disabled once we have explicit multi-lingual support in Apache
             // Stanbol
             return true;
         }
         return false;
     }
 
     @Override
     public void launchAnalysis(DocumentModel doc) throws ClientException {
         if (shouldSkip(doc)) {
             return;
         }
         states.put(doc.getRef(), STATUS_ANALYSIS_PENDING);
         try {
             // extract the text synchronously to benefit from the cache and spare some session house keeping
             // in the analysis thread pool
             String textContent = extractText(doc);
             AnalysisTask task = new AnalysisTask(doc.getRepositoryName(), doc.getRef(), textContent, this);
             while (analysisTaskQueue.remove(task)) {
                 // remove any previous version of the same document to avoid analyzing several times in a row
             }
            analysisExecutor.execute(task);
         } finally {
             states.remove(doc.getRef());
         }
 
     }
 
     @Override
     public void launchSynchronousAnalysis(DocumentModel doc, CoreSession session) throws ClientException,
                                                                                  IOException {
         if (shouldSkip(doc)) {
             return;
         }
         try {
             states.put(doc.getRef(), STATUS_ANALYSIS_PENDING);
             String textContent = extractText(doc);
             createLinks(doc, session, analyze(textContent));
         } finally {
             states.remove(doc.getRef());
         }
     }
 
     protected void createLinks(DocumentModel doc, CoreSession session, List<OccurrenceGroup> groups) throws ClientException,
                                                                                                     DereferencingException,
                                                                                                     IOException {
         if (groups.isEmpty()) {
             return;
         }
         states.put(doc.getRef(), STATUS_LINKING_PENDING);
 
         DocumentModel entityContainer = leService.getEntityContainer(session);
         for (OccurrenceGroup group : groups) {
 
             // hardcoded trick to avoid linking to persons just based on their first name or last names for
             // instance
             if (!linkShortPersonNames && "Person".equals(group.type)
                 && group.name.trim().split(" ").length <= 1) {
                 continue;
             }
 
             List<EntitySuggestion> suggestions = leService.suggestEntity(session, group.name, group.type, 3);
 
             if (suggestions.isEmpty() && linkToUnrecognizedEntities) {
                 DocumentModel localEntity = session.createDocumentModel(group.type);
                 localEntity.setPropertyValue("dc:title", group.name);
                 String pathSegment = pathService.generatePathSegment(localEntity);
                 localEntity.setPathInfo(entityContainer.getPathAsString(), pathSegment);
                 localEntity = session.createDocument(localEntity);
                 session.save();
                 leService.addOccurrences(session, doc.getRef(), localEntity.getRef(), group.occurrences);
             } else {
                 if (suggestions.size() > 1 && !linkToAmbiguousEntities) {
                     continue;
                 }
                 EntitySuggestion bestGuess = suggestions.get(0);
                 leService.addOccurrences(session, doc.getRef(), bestGuess, group.occurrences);
             }
         }
     }
 
     public List<OccurrenceGroup> findStanbolEntityOccurrences(Model model) {
         // Retrieve the existing text annotations handling the subsumption
         // relationships
         Property type = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
         Property entityType = model.getProperty("http://purl.org/dc/terms/type");
         Property dcRelation = model.getProperty("http://purl.org/dc/terms/relation");
         Resource textAnnotationType = model.getResource("http://fise.iks-project.eu/ontology/TextAnnotation");
         ResIterator it = model.listSubjectsWithProperty(type, textAnnotationType);
         List<OccurrenceGroup> groups = new ArrayList<OccurrenceGroup>();
         for (; it.hasNext();) {
             Resource annotation = it.nextResource();
             if (model.listObjectsOfProperty(annotation, dcRelation).hasNext()) {
                 // this is not the most specific occurrence of this name: skip
                 continue;
             }
 
             Statement typeStmt = annotation.getProperty(entityType);
             if (typeStmt == null || !typeStmt.getObject().isURIResource()) {
                 continue;
             }
             Resource typeResouce = (Resource) typeStmt.getObject().as(Resource.class);
             String localType = localTypes.get(typeResouce.getURI());
             if (localType == null) {
                 continue;
             }
             OccurrenceInfo occInfo = getOccurrenceInfo(model, annotation);
             if (occInfo == null) {
                 continue;
             }
             OccurrenceGroup group = new OccurrenceGroup(occInfo.mention, localType);
             group.occurrences.add(occInfo);
 
             // This is a first occurrence, collect any subsumed annotations
             ResIterator it2 = model.listSubjectsWithProperty(dcRelation, annotation);
             for (; it2.hasNext();) {
                 OccurrenceInfo subMention = getOccurrenceInfo(model, it2.nextResource());
                 if (subMention == null) {
                     continue;
                 }
                 group.occurrences.add(subMention);
             }
             groups.add(group);
         }
         return groups;
     }
 
     protected OccurrenceInfo getOccurrenceInfo(Model model, Resource annotation) {
         Property mentionProp = model.getProperty("http://fise.iks-project.eu/ontology/selected-text");
         Statement mentionStmt = annotation.getProperty(mentionProp);
         if (mentionStmt == null || !mentionStmt.getObject().isLiteral()) {
             return null;
         }
         Literal mentionLiteral = (Literal) mentionStmt.getObject().as(Literal.class);
         String mention = mentionLiteral.getString().trim();
 
         Property contextProp = model.getProperty("http://fise.iks-project.eu/ontology/selection-context");
         Statement contextStmt = annotation.getProperty(contextProp);
         if (contextStmt != null && contextStmt.getObject().isLiteral()) {
             Literal contextLiteral = (Literal) contextStmt.getObject().as(Literal.class);
             // TODO: normalize whitespace
             String context = contextLiteral.getString().trim();
 
             if (!context.contains(mention) || context.length() > 500) {
                 // context extraction is likely to have failed on some complex
                 // layout
                 context = mention;
             }
             return new OccurrenceInfo(mention, context);
         } else {
             return new OccurrenceInfo(mention, mention);
         }
     }
 
     public List<OccurrenceGroup> analyze(String textContent) throws IOException {
         String output = callSemanticEngine(textContent, outputFormat, 2);
         Model model = ModelFactory.createDefaultModel().read(new StringReader(output), null);
         return findStanbolEntityOccurrences(model);
     }
 
     public String callSemanticEngine(String textContent, String outputFormat, int retry) throws IOException {
 
         String effectiveEngineUrl = engineURL;
         if (effectiveEngineUrl == null) {
             // no Automation Chain configuration available: use the
             // configuration from a properties file
             effectiveEngineUrl = Framework.getProperty(ENGINE_URL_PROPERTY, DEFAULT_ENGINE_URL);
             if (effectiveEngineUrl.trim().isEmpty()) {
                 effectiveEngineUrl = DEFAULT_ENGINE_URL;
             }
         }
         HttpPost post = new HttpPost(effectiveEngineUrl);
         try {
             post.setHeader("Accept", outputFormat);
             // TODO: fix the Stanbol engine handling of charset in mimetype
             // negociation
             // post.setHeader("Content-Type", "text/plain; charset=utf-8");
             post.setHeader("Content-Type", "text/plain");
             post.setEntity(new ByteArrayEntity(textContent.getBytes("utf-8")));
             HttpResponse response = httpClient.execute(post);
             InputStream content = response.getEntity().getContent();
             String body = IOUtils.toString(content);
             content.close();
             if (response.getStatusLine().getStatusCode() == 200) {
                 return body;
             } else {
                 if (retry > 0) {
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException e) {
                         // pass
                     }
                     return callSemanticEngine(textContent, outputFormat, retry - 1);
                 } else {
                     String errorMsg = String.format("Unexpected response from '%s': %s\n %s",
                         effectiveEngineUrl, response.getStatusLine().toString(), body);
                     throw new IOException(errorMsg);
                 }
             }
         } catch (ClientProtocolException e) {
             post.abort();
             throw new ClientProtocolException(String.format("Error connecting to '%s': %s",
                 effectiveEngineUrl, e.getMessage(), e));
         } catch (IOException e) {
             post.abort();
             throw e;
         }
     }
 
     protected String extractText(DocumentModel doc) throws ClientException {
         StringBuilder sb = new StringBuilder();
         // TODO: un-hardcode me and refactorize into a reuseable, standalone
         // operation
         sb.append(doc.getTitle());
         sb.append("\n\n");
         Serializable description = doc.getPropertyValue("dc:description");
         if (description != null) {
             sb.append(description);
             sb.append("\n\n");
         }
 
         // special handling of the HTML payload of notes
         try {
             String noteContent = (String) doc.getPropertyValue("note:note");
             StreamingBlob blob = StreamingBlob.createFromString(noteContent);
             blob.setMimeType("text/html");
             BlobHolder bh = new SimpleBlobHolder(blob);
             BlobHolder converted = conversionService.convert(ANY2TEXT, bh, null);
             sb.append(converted.getBlob().getString());
             sb.append("\n\n");
         } catch (PropertyException pe) {
             // ignore, not a note document
         } catch (IOException e) {
             throw new ClientException(e);
         }
 
         if (doc.hasFacet(FacetNames.HAS_RELATED_TEXT)) {
             @SuppressWarnings("unchecked")
             List<Map<String,String>> resources = doc.getProperty("relatedtext:relatedtextresources")
                     .getValue(List.class);
             for (Map<String,String> relatedResource : resources) {
                 String text = relatedResource.get("relatedtext");
                 if (text != null && !text.trim().isEmpty()) {
                     sb.append(text);
                     sb.append("\n\n");
                 }
             }
         }
 
         BlobsExtractor extractor = new BlobsExtractor();
         sb.append(blobsToText(extractor.getBlobs(doc)));
         return sb.toString();
     }
 
     protected String blobsToText(List<Blob> blobs) {
         List<String> strings = new LinkedList<String>();
         for (Blob blob : blobs) {
             try {
                 SimpleBlobHolder bh = new SimpleBlobHolder(blob);
                 BlobHolder result = conversionService.convert(ANY2TEXT, bh, null);
                 if (result == null) {
                     continue;
                 }
                 blob = result.getBlob();
                 if (blob == null) {
                     continue;
                 }
                 String string = new String(blob.getByteArray(), "UTF-8");
                 // strip '\0 chars from text
                 if (string.indexOf('\0') >= 0) {
                     string = string.replace("\0", " ");
                 }
                 strings.add(string);
             } catch (Exception e) {
                 log.error(e.getMessage(), e);
                 continue;
             }
         }
         return StringUtils.join(strings, "\n\n");
     }
 
     @Override
     public String getProgressStatus(DocumentRef docRef) {
         return states.get(docRef);
     }
 
     @Override
     public void clearProgressStatus(DocumentRef docRef) {
         states.remove(docRef);
     }
 
 }
