 package net.metadata.openannotation.lorestore.servlet.rdf2go;
 
 import static net.metadata.openannotation.lorestore.common.LoreStoreConstants.DCTERMS_CREATED;
 import static net.metadata.openannotation.lorestore.common.LoreStoreConstants.DCTERMS_MODIFIED;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.Date;
 
 import javax.servlet.http.HttpServletResponse;
 
 import net.metadata.openannotation.lorestore.access.LoreStoreAccessPolicy;
 import net.metadata.openannotation.lorestore.exceptions.LoreStoreException;
 import net.metadata.openannotation.lorestore.exceptions.NotFoundException;
 import net.metadata.openannotation.lorestore.model.NamedGraph;
 import net.metadata.openannotation.lorestore.model.rdf2go.NamedGraphImpl;
 import net.metadata.openannotation.lorestore.servlet.LoreStoreControllerConfig;
 import net.metadata.openannotation.lorestore.servlet.LoreStoreUpdateHandler;
 import net.metadata.openannotation.lorestore.servlet.LorestoreResponse;
 import net.metadata.openannotation.lorestore.triplestore.TripleStoreConnectorFactory;
 import net.metadata.openannotation.lorestore.util.OATripleCallback;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.ontoware.rdf2go.ModelFactory;
 import org.ontoware.rdf2go.RDF2Go;
 import org.ontoware.rdf2go.exception.ModelRuntimeException;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.ModelSet;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdf2go.model.node.URI;
 import org.openrdf.rdf2go.RepositoryModelFactory;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.rio.RDFFormat;
 
 import de.dfki.km.json.JSONUtils;
 import de.dfki.km.json.jsonld.JSONLD;
 import de.dfki.km.json.jsonld.JSONLDProcessingError;
 
 import au.edu.diasb.chico.mvc.RequestFailureException;
 
 /**
  * The RDF2GoOREUpdateHandler class processes updates to compound object or annotation by
  * OREController
  * 
  * Based loosely on DannoUpdateHandler
  * 
  */
 public abstract class AbstractRDF2GoUpdateHandler implements LoreStoreUpdateHandler {
 
     protected final LoreStoreControllerConfig occ;
     protected final TripleStoreConnectorFactory cf;
     protected LoreStoreAccessPolicy ap;
     protected final Logger LOG = Logger.getLogger(AbstractRDF2GoUpdateHandler.class);
 
     
     public AbstractRDF2GoUpdateHandler(LoreStoreControllerConfig occ) {
         this.occ = occ;
         this.cf = occ.getContainerFactory();
         this.ap = occ.getAccessPolicy();
     }
 
     /* (non-Javadoc)
      * @see net.metadata.openannotation.lorestore.servlet.rdf2go.OREUpdateHandler#post(java.io.InputStream)
      */
     @Override
     public LorestoreResponse post(InputStream inputRDF, String contentType) throws RequestFailureException,
             IOException, LoreStoreException, InterruptedException {
         
         // TODO: probably should check after we've loaded the model,
         // but at this stage, the security check ignores it anyway
         ap.checkCreate(null);
         RepositoryModelFactory mf = new RepositoryModelFactory();
         String uid = occ.getUidGenerator().newUID();
         URI newUri = mf.createModel().createURI(occ.getBaseUri() + uid);
         Model model = mf.createModel(newUri);
         try {
             model.open();
            if (contentType.equals(Syntax.RdfXml.getMimeType())
                     || contentType.equals(Syntax.Trix.getMimeType()) 
                     || contentType.equals(Syntax.Trig.getMimeType())){
                 model.readFrom(inputRDF, Syntax.forMimeType(contentType), occ.getBaseUri());
             
            } else if (contentType.equals("application/json")){
                 Object jsonObject = JSONUtils.fromInputStream(inputRDF);
                 // TODO if no @context, inject default context
                 // TODO if no @id, inject dummy identifier (will be replaced)
                 OATripleCallback callback = new OATripleCallback();
                 callback.setModel(model);
                             JSONLD.toRDF(jsonObject, callback);
             } else {
                 throw new RequestFailureException(HttpServletResponse.SC_BAD_REQUEST, "Acceptable content types are RDF/XML or JSON-LD");
             }
         } catch (ModelRuntimeException e) {
             try{
                 
                 LOG.info("Post: Model read failed " + e.getMessage() + " for posted content: " + IOUtils.toString(inputRDF, "UTF-8"));
                 
                 model.close();
             } catch (Exception e2){
                 // ignore
             }
             throw new RequestFailureException(
                     HttpServletResponse.SC_BAD_REQUEST, "Error reading RDF " + e.getMessage());
         } catch (JSONLDProcessingError e) {
             LOG.info("Post: Model read failed " + e.getMessage());
             throw new RequestFailureException(
                     HttpServletResponse.SC_BAD_REQUEST, "Error reading JSON " + e.getMessage());
         } catch (Exception e) {
             LOG.info("Post failed " + e.getMessage());
             throw new RequestFailureException(
                     HttpServletResponse.SC_BAD_REQUEST, "Post failed " + e.getMessage());
         }
 
         NamedGraphImpl obj = makeNewObject(model);
         model.close();
         obj.assignURI(newUri.toString());
 
         // Update created and modified dates
         String userURI = occ.getIdentityProvider().obtainUserURI();
         obj.setUser(userURI);
         Date now = new Date();
         obj.setDate(now, DCTERMS_CREATED);
         obj.setDate(now, DCTERMS_MODIFIED);
 
         ModelSet ms = null;
         try {
             ms = cf.retrieveConnection();
             // Load schema model for validation
             Model schemaModel = null;
             String schema = occ.getDefaultSchema();
             if (schema != null){
                 try {
                     URI schemaURI = mf.createModel().createURI(schema);
                     schemaModel = ms.getModel(schemaURI);
                     if (schemaModel != null){
                         obj.findValidObject(schemaModel);
                     }
                 } finally {
                     if (schemaModel != null){
                         schemaModel.close();
                     }
                 }
             }
             ms.addModel(obj.getModel(), newUri);
             ms.commit();
         } finally {
             cf.release(ms);
         }
         LorestoreResponse response = new LorestoreResponse(getDefaultViewName());
         response.setRDFModel(obj.getModel());
         response.setLocationHeader(occ.getBaseUri() + uid);
         response.setOverrideContentType(contentType);
         response.setReturnStatus(201);
         return response;
     }
     
     
     /* (non-Javadoc)
      * @see net.metadata.openannotation.lorestore.servlet.rdf2go.OREUpdateHandler#delete(java.lang.String)
      */
     @Override
     public void delete(String objId) throws NotFoundException,
             InterruptedException {
         ModelSet container = null;
         Model model = null;
         try {
             container = cf.retrieveConnection();
             URI objURI = container.createURI(occ.getBaseUri() + objId);
             if (!container.containsModel(objURI)) {
                 throw new NotFoundException("Cannot delete, object not found");
             }
             model = container.getModel(objURI);
             NamedGraph obj = makeNewObject(model);
             model.close();
             ap.checkDelete(obj);
             obj.close();
             container.removeModel(objURI);
             container.commit();
         } finally {
             cf.release(container);
         }
     }
 
 
     @Override
     public LorestoreResponse put(String objId, InputStream inputRDF, String contentType)
             throws RequestFailureException, IOException, LoreStoreException,
             InterruptedException {
         ModelFactory mf = RDF2Go.getModelFactory();
         URI objURI = mf.createModel().createURI(occ.getBaseUri() + objId);
 
         Model newModel = null;
         Model schemaModel = null;
         ModelSet container = cf.retrieveConnection();
         String oldUserUri = null;
         try {
             Model oldModel = container.getModel(objURI);
             try {
                 oldUserUri = checkUserCanUpdate(oldModel);
             } finally {
                 oldModel.close();
             }
             
             newModel = mf.createModel(objURI);
             newModel.open();
             try {
                     if (contentType.equals(Syntax.RdfXml.getMimeType()) 
                                     || contentType.equals(Syntax.Trix.getMimeType()) 
                                     || contentType.equals(Syntax.Trig.getMimeType())){
                                 newModel.readFrom(inputRDF, Syntax.forMimeType(contentType), occ.getBaseUri());
                             
                             } else if (contentType.equals("application/json")){
                                 Object jsonObject = JSONUtils.fromInputStream(inputRDF);
                                 OATripleCallback callback = new OATripleCallback();
                                 callback.setModel(newModel);
                                 JSONLD.toRDF(jsonObject, callback);
                             } else {
                                 throw new RequestFailureException(HttpServletResponse.SC_BAD_REQUEST, "Acceptable content types are RDF/XML or JSON-LD");
                             }
             } catch (ModelRuntimeException e) {
                 LOG.info("Put: Model read failed " + e.getMessage() + " for " + objURI.toString());
                 newModel.close();
                 throw new RequestFailureException(
                         HttpServletResponse.SC_BAD_REQUEST, "Error reading RDF");
             } catch (JSONLDProcessingError e) {
                 LOG.info("Put: Model read failed " + e.getMessage() + " for " + objURI.toString());
                 throw new RequestFailureException(
                         HttpServletResponse.SC_BAD_REQUEST, "Error reading JSON " + " for " + objURI.toString());
             }
             NamedGraphImpl obj = makeNewObject(newModel);
             newModel.close();
             
             // Load schema model
             String schema = occ.getDefaultSchema();
             if (schema != null){
                 try {
                     
                     URI schemaURI = mf.createModel().createURI(schema);
                     schemaModel = container.getModel(schemaURI);
                     if (schemaModel != null){
                         obj.findValidObject(schemaModel);
                     }
                 } finally {
                     if (schemaModel != null){
                         schemaModel.close();
                     }
                 }
             }
             if (oldUserUri != null){
                 obj.setUser(oldUserUri);
             } else {
                 LOG.info("User is null for " + objURI);
             }
             obj.setDate(new Date(), DCTERMS_MODIFIED);
             newModel = obj.getModel();
             container.removeModel(objURI);
             container.addModel(newModel, objURI);
             container.commit();
         } finally {
             cf.release(container);
         }
         LorestoreResponse resp = new LorestoreResponse(getDefaultViewName());
         resp.setRDFModel(newModel);
         resp.setOverrideContentType(contentType);
         return resp;
     }
     protected String getDefaultViewName() {
         // override in subclasses
         return "";
     }
     private String checkUserCanUpdate(Model oldModel) throws LoreStoreException {
         if (oldModel == null || oldModel.isEmpty()) {
             if (oldModel != null)
                 oldModel.close();
             throw new LoreStoreException("Cannot update non-existant object");
         }
         String oldUserUri = null;
         NamedGraphImpl o = makeNewObject(oldModel);
         try {
             oldUserUri = o.getOwnerId();
             ap.checkUpdate(o);
         } finally {
             o.close();
         }
         return oldUserUri;
     }
 
     /* (non-Javadoc)
      * @see net.metadata.openannotation.lorestore.servlet.rdf2go.OREUpdateHandler#bulkImport(java.io.InputStream, java.lang.String)
      */
     @Override
     public int bulkImport(InputStream body, String fileName) throws Exception {
         RepositoryConnection con = null;
         ModelSet modelSet = null;
         int delta = 0;
         try {
             modelSet = cf.retrieveConnection();
     
             Repository rep = (Repository) modelSet
                     .getUnderlyingModelSetImplementation();
             con = rep.getConnection();
             long connectionSize = con.size();
             con.setAutoCommit(false);
             RDFFormat rdfFormat = RDFFormat.forFileName(fileName);
             con.add(body, "", rdfFormat);
             con.commit();
             delta = (int) (con.size() - connectionSize);
         } finally {
             if (con != null)
                 con.close();
             if (modelSet != null)
                 cf.release(modelSet);
         }
         return delta;
     }
     
     protected NamedGraphImpl makeNewObject (Model model){
         return null;
     }
     
     /* (non-Javadoc)
      * @see net.metadata.openannotation.lorestore.servlet.rdf2go.OREUpdateHandler#wipeDatabase()
      */
     @Override
     public void wipeDatabase() throws InterruptedException {
 
         ModelSet modelSet = null;
         try {
             modelSet = cf.retrieveConnection();
             
             modelSet.removeAll();
             
             modelSet.commit();
             
         } finally {
             if (modelSet != null)
                 cf.release(modelSet);
         }
     }
 }
