 package pl.psnc.dl.wf4ever.portal.services;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.log4j.Logger;
 import org.purl.wf4ever.rosrs.client.Annotable;
 import org.purl.wf4ever.rosrs.client.ROSRService;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 import org.purl.wf4ever.wf2ro.ServiceException;
 import org.purl.wf4ever.wf2ro.Wf2ROService;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.BaseResource;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.BaseResourceHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.File;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.FileHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.InternalPackItem;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.InternalPackItemHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.Pack;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.PackHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.User;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.Workflow;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.WorkflowHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.wizard.ImportModel;
 import pl.psnc.dl.wf4ever.portal.myexpimport.wizard.ImportModel.ImportStatus;
 import pl.psnc.dl.wf4ever.vocabulary.FOAF;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 /**
  * Service for importing content from myExperiment to RODL. Runs a background thread.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public final class MyExpImportService {
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(MyExpImportService.class);
 
 
     /**
      * Private constructor.
      */
     private MyExpImportService() {
         // nope
     }
 
 
     /**
      * Starts a background thread that does the import.
      * 
      * @param model
      *            model with all import settings
      * @param rosrs
      *            the ROSRS client
      * @param wf2ROService
      *            Wf-RO transformation service URI
      * @param myExpAccessToken
      *            myExp access token
      * @param consumerKey
      *            myExp consumer key
      * @param consumerSecret
      *            myExp consumer secret
      */
     public static void startImport(ImportModel model, ROSRService rosrs, Wf2ROService wf2ROService,
             Token myExpAccessToken, String consumerKey, String consumerSecret) {
         new ImportThread(model, rosrs, wf2ROService, myExpAccessToken, consumerKey, consumerSecret).start();
     }
 
 
     /**
      * Get a myExp user based on OAuth access tokens.
      * 
      * @param accessToken
      *            myExp access token
      * @param service
      *            myExp OAuth service
      * @return a {@link User}
      * @throws OAuthException
      *             when there is an error in connection with myExperiment
      * @throws JAXBException
      *             when data from myExperiment cannot be parsed
      */
     public static User retrieveMyExpUser(Token accessToken, OAuthService service)
             throws OAuthException, JAXBException {
         User myExpUser;
         Response response = OAuthHelpService.sendRequest(service, Verb.GET, MyExpApi.WHOAMI_URL, accessToken);
         myExpUser = createMyExpUserModel(response.getBody());
 
         response = OAuthHelpService.sendRequest(service, Verb.GET,
             URI.create(String.format(MyExpApi.GET_USER_URL_TMPL, myExpUser.getId())), accessToken);
         myExpUser = createMyExpUserModel(response.getBody());
         return myExpUser;
     }
 
 
     /**
      * Deserialize user data from myExperiment.
      * 
      * @param xml
      *            data from myExperiment
      * @return a {@link User}
      * @throws JAXBException
      *             when data from myExperiment cannot be parsed
      */
     private static User createMyExpUserModel(String xml)
             throws JAXBException {
         JAXBContext jc = JAXBContext.newInstance(User.class);
 
         Unmarshaller u = jc.createUnmarshaller();
         StringBuffer xmlStr = new StringBuffer(xml);
         return (User) u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
     }
 
 
     /**
      * A background thread that imports data from myExperiment and uploads them to RODL.
      * 
      * @author piotrekhol
      * 
      */
     private static class ImportThread extends Thread {
 
         /** Logger. */
         private static final Logger LOG = Logger.getLogger(ImportThread.class);
 
         /** myExperiment service. */
         private final OAuthService service;
 
         /** Import model with all the settings. */
         private final ImportModel model;
 
         /** myExperiment OAuth access token. */
         private final Token myExpToken;
 
         /** Total number of steps to be done during import. */
         private int stepsTotal = 0;
 
         /** Number of steps completed during import. */
         private int stepsComplete = 0;
 
         /** List of errors that happened during the import. */
         private final List<String> errors = new ArrayList<>();
 
         /** Wf-RO transformation service. */
         private final Wf2ROService wf2ROService;
 
         /** ROSRS client. */
         private ROSRService rosrs;
 
 
         /**
          * Constructor.
          * 
          * @param importModel
          *            Import model with all the settings
          * @param rosrs
          *            the ROSRS client
          * @param wf2roService2
          *            Wf-RO transformation service URI
          * @param myExpAccessToken
          *            myExperiment OAuth access token
          * @param consumerKey
          *            myExp consumer key
          * @param consumerSecret
          *            myExp consumer secret
          */
         public ImportThread(ImportModel importModel, ROSRService rosrs, Wf2ROService wf2roService2,
                 Token myExpAccessToken, String consumerKey, String consumerSecret) {
             super();
             model = importModel;
             this.wf2ROService = wf2roService2;
             myExpToken = myExpAccessToken;
             this.rosrs = rosrs;
             service = MyExpApi.getOAuthService(consumerKey, consumerSecret);
         }
 
 
         @Override
         public void run() {
             model.setStatus(ImportStatus.RUNNING);
             model.setMessage("Preparing the data");
 
             List<Pack> packs = getPacks(model.getSelectedPacks());
             if (model.getPublicPackId() != null) {
                 try {
                     packs.add(getPack(model.getPublicPackId()));
                 } catch (Exception e) {
                     LOG.error("Preparing public pack", e);
                     errors.add(String.format("When fetching pack with ID %s: %s", model.getPublicPackId(),
                         e.getMessage()));
                 }
             }
             if (model.getPublicWorkflowId() != null) {
                 try {
                     model.getSelectedWorkflows().add(getWorkflowHeader(model.getPublicWorkflowId()));
                 } catch (Exception e) {
                     LOG.error("Preparing public workflow", e);
                     errors.add(String.format("When fetching workflow with ID %s: %s", model.getPublicWorkflowId(),
                         e.getMessage()));
                 }
             }
             int simpleResourcesCnt = model.getSelectedFiles().size() + model.getSelectedWorkflows().size();
             for (Pack pack : packs) {
                 simpleResourcesCnt += pack.getResources().size();
             }
             stepsTotal = simpleResourcesCnt * 4 + packs.size() * 2 + 3;
 
             try {
                 createRO();
             } catch (Exception e) {
                 LOG.error("Creating RO", e);
                 errors.add(String.format("When creating RO: %s", e.getMessage()));
                 model.setProgressInPercent(100);
                 model.setStatus(ImportStatus.FAILED);
             }
             if (model.getResearchObject() != null) {
                 importFiles(model.getSelectedFiles());
                 importWorkflows(model.getSelectedWorkflows());
                 importPacks(packs);
                 model.setProgressInPercent(100);
                 model.setStatus(ImportStatus.FINISHED);
             }
             String finalMessage;
             if (model.getStatus() == ImportStatus.FINISHED) {
                 finalMessage = "Import finished successfully!";
             } else {
                 finalMessage = "Import failed.";
             }
             if (!errors.isEmpty()) {
                 finalMessage = finalMessage.concat("<br/>Some errors occurred:<br/><ul>");
                 for (String error : errors) {
                     finalMessage = finalMessage.concat("<br/><li>").concat(error).concat("</li>");
                 }
                 finalMessage = finalMessage.concat("</ul>");
             }
             model.setMessage(finalMessage);
         }
 
 
         /**
          * Create an RO in RODL.
          * 
          * @throws ROSRSException
          *             when the RO could not be created
          * @throws ROException
          *             when incorrect data were returned
          */
         private void createRO()
                 throws ROSRSException, ROException {
             model.setMessage(String.format("Creating a Research Object \"%s\"", model.getRoId()));
 
             ResearchObject ro;
             if (model.getTemplate() == null) {
                 ro = ResearchObject.create(rosrs, model.getRoId());
             } else {
                 model.setMessage(String.format("Applying template: \"%s\"", model.getTemplate().getTitle()));
                 ro = model.getTemplate().create(rosrs, model.getRoId());
             }
             if (model.getTitle() != null) {
                 model.setMessage("Setting title");
                 ro.createPropertyValue(DCTerms.title, model.getTitle());
             }
             if (model.getDescription() != null) {
                 model.setMessage("Setting description");
                 ro.createPropertyValue(DCTerms.description, model.getDescription());
             }
             model.setResearchObject(ro);
             incrementStepsComplete();
         }
 
 
         /**
          * Import files.
          * 
          * @param fileHeaders
          *            a list of headers of files.
          */
         private void importFiles(List<FileHeader> fileHeaders) {
             for (FileHeader header : fileHeaders) {
                 try {
                     File r = importFile(header);
                     downloadResourceMetadata(r);
                 } catch (Exception e) {
                     LOG.error("When importing simple resource " + header.getResource(), e);
                     errors.add(String.format("When importing %s: %s", header.getResource(), e.getMessage()));
                 }
             }
         }
 
 
         /**
          * Import workfows.
          * 
          * @param workflowHeaders
          *            a list of headers of workflows.
          */
         private void importWorkflows(List<WorkflowHeader> workflowHeaders) {
             for (WorkflowHeader header : workflowHeaders) {
                 try {
                     Workflow w = importWorkflow(header);
                     downloadResourceMetadata(w);
                 } catch (Exception e) {
                     LOG.error("When importing workflow " + header.getResource(), e);
                     errors.add(String.format("When importing %s: %s", header.getResource(), e.getMessage()));
                 }
             }
         }
 
 
         /**
          * Import a workflow using the Wf-RO transformation service.
          * 
          * @param header
          *            workflow metadata
          * @return workflow complete metadata
          * @throws OAuthException
          *             when there is a problem with authorization with myExperiment
          * @throws JAXBException
          *             when there is a problem with parsing the workflow metadata
          * @throws IOException
          *             when there is a problem with the Wf-RO service
          * @throws ServiceException
          *             when the workflow cannot be transformed
          */
         private Workflow importWorkflow(WorkflowHeader header)
                 throws OAuthException, JAXBException, IOException, ServiceException {
             Workflow w = (Workflow) getResource(header, Workflow.class);
             model.setMessage(String.format("Transforming workflow %s", w.getResource()));
             wf2ROService.transform(URI.create(w.getContentUri()), w.getContentType(), model.getResearchObject()
                     .getUri());
             incrementStepsComplete();
             return w;
         }
 
 
         /**
          * Download complete pack metadata based on short pack metadata.
          * 
          * @param packHeaders
          *            list of pack headers
          * @return a list of pack metadata
          */
         private List<Pack> getPacks(List<PackHeader> packHeaders) {
             List<Pack> packs = new ArrayList<Pack>();
             for (PackHeader packHeader : packHeaders) {
                 try {
                     packs.add((Pack) getResource(packHeader, Pack.class));
                 } catch (Exception e) {
                     LOG.error("Preparing packs", e);
                     errors.add(String.format("When fetching pack %s: %s", packHeader.getResource().toString(),
                         e.getMessage()));
                 }
             }
             return packs;
         }
 
 
         /**
          * Download pack metadata.
          * 
          * @param customPackId
          *            myExperiment pack id
          * @return pack metadata
          * @throws OAuthException
          *             when there is a problem with authorization
          * @throws JAXBException
          *             when there is a problem with parsing the pack metadata
          */
         private Pack getPack(String customPackId)
                 throws OAuthException, JAXBException {
             PackHeader packHeader = new PackHeader();
             packHeader.setUri(URI.create("http://www.myexperiment.org/pack.xml?id=" + customPackId));
             return (Pack) getResource(packHeader, Pack.class);
         }
 
 
         /**
          * Prepare short workflow metadata for a given id.
          * 
          * @param customWorkflowId
          *            myExperiment workflow id
          * @return workflow short metadata
          * @throws OAuthException
          *             when there is a problem with authorization
          */
         private WorkflowHeader getWorkflowHeader(String customWorkflowId)
                 throws OAuthException {
             WorkflowHeader workflowHeader = new WorkflowHeader();
             workflowHeader.setUri(URI.create("http://www.myexperiment.org/workflow.xml?id=" + customWorkflowId));
             return workflowHeader;
         }
 
 
         /**
          * Import a set of packs.
          * 
          * @param packs
          *            list of packs metadata
          */
         private void importPacks(List<Pack> packs) {
             for (Pack pack : packs) {
                 try {
                     downloadResourceMetadata(pack);
 
                     for (InternalPackItemHeader packItemHeader : pack.getResources()) {
                         try {
                             importInternalPackItem(pack, packItemHeader);
                         } catch (Exception e) {
                             LOG.error("When importing internal pack item " + packItemHeader.getResource(), e);
                             errors.add(String.format("When importing %s: %s", packItemHeader.getResource(),
                                 e.getMessage()));
                         }
                     }
                 } catch (Exception e) {
                     LOG.error("When importing pack metadata " + pack.getResource(), e);
                     errors.add(String.format("When importing %s metadata: %s", pack.getResource(), e.getMessage()));
                 }
             }
         }
 
 
         /**
          * Import an internal pack item.
          * 
          * @param pack
          *            pack metadata
          * @param packItemHeader
          *            pack internal item short metadata
          * @throws OAuthException
          *             when there is a problem with authorization
          * @throws JAXBException
          *             when there is a problem with parsing the pack item metadata
          * @throws URISyntaxException
          *             when the resource URI cannot be created
          * @throws IOException
          *             when there is a problem with Wf-RO service
          * @throws ROSRSException
          *             the resource couldn't be uploaded to ROSRS
          * @throws ROException
          *             ROSRS returned incorrect data
          * @throws ServiceException
          *             when the workflow cannot be transformed
          */
         private void importInternalPackItem(Pack pack, InternalPackItemHeader packItemHeader)
                 throws JAXBException, OAuthException, URISyntaxException, IOException, ROSRSException, ROException,
                 ServiceException {
             InternalPackItem internalItem = (InternalPackItem) getResource(packItemHeader, InternalPackItem.class);
             BaseResourceHeader resourceHeader = internalItem.getItem();
             BaseResource r;
             if (resourceHeader instanceof FileHeader) {
                 r = importFile((FileHeader) resourceHeader);
             } else {
                 r = importWorkflow((WorkflowHeader) resourceHeader);
             }
             downloadResourceMetadata(r);
         }
 
 
         /**
          * Import a file.
          * 
          * @param res
          *            resource short metadata
          * @return resource complete metadata
          * @throws OAuthException
          *             when there is a problem with authorization
          * @throws JAXBException
          *             when there is a problem with parsing the pack item metadata
          * @throws URISyntaxException
          *             when the resource URI cannot be created
          * @throws ROSRSException
          *             the resource couldn't be created in ROSRS
          * @throws ROException
          *             ROSRS returned incorrect data
          */
         private File importFile(FileHeader res)
                 throws OAuthException, JAXBException, URISyntaxException, ROSRSException, ROException {
             File r = (File) getResource(res, File.class);
             incrementStepsComplete();
 
             model.setMessage(String.format("Uploading %s", r.getFilename()));
             model.getResearchObject().aggregate(r.getFilename(), new ByteArrayInputStream(r.getContentDecoded()),
                 r.getContentType());
 
             incrementStepsComplete();
             return r;
         }
 
 
         /**
          * Download complete resource metadata.
          * 
          * @param res
          *            resource short metadata
          * @param resourceClass
          *            resource Java class
          * @return complete resource metadata
          * @throws OAuthException
          *             when there is a problem with authorization
          * @throws JAXBException
          *             when there is a problem with parsing the resource metadata
          */
         private BaseResource getResource(BaseResourceHeader res, Class<? extends BaseResource> resourceClass)
                 throws OAuthException, JAXBException {
             model.setMessage(String.format("Downloading %s", res.getResourceUrl()));
             Response response = OAuthHelpService.sendRequest(service, Verb.GET, res.getResourceUrl(), myExpToken);
             BaseResource r = (BaseResource) createMyExpResource(response.getBody(), resourceClass);
             return r;
         }
 
 
         /**
          * Import selected statements from myExperiment RDF file describing a resource as an annotation body.
          * 
          * @param res
          *            resource complete metadata
          * @throws OAuthException
          *             when there is a problem with authorization
          * @throws URISyntaxException
          *             when the resource URI cannot be created
          * @throws ROSRSException
          *             the annotation couldn't be created in ROSRS
          */
         private void downloadResourceMetadata(BaseResource res)
                 throws OAuthException, URISyntaxException, ROSRSException {
             model.setMessage(String.format("Downloading metadata file %s", res.getResource()));
             Response response = OAuthHelpService.sendRequest(service, Verb.GET, res.getResource(), myExpToken,
                 "application/rdf+xml");
             // in the future, the RDF could be parsed (and somewhat validated)
             // and the filename can be extracted from it
             String rdf = response.getBody();
             Annotable annTarget;
             if (res instanceof Pack) {
                 annTarget = model.getResearchObject();
             } else if (res instanceof File) {
                annTarget = new org.purl.wf4ever.rosrs.client.Resource(model.getResearchObject(),
                        ((File) res).getFilenameURI(), null, null, null);
             } else {
                 incrementStepsComplete();
                 return;
             }
             incrementStepsComplete();
 
             String bodyPath = createAnnotationBodyPath(annTarget.getName());
             model.setMessage(String.format("Uploading annotation body %s", bodyPath));
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             createAnnotationBody(annTarget.getUri(), rdf).write(out);
             rosrs.addAnnotation(model.getResearchObject().getUri(), Collections.singleton(annTarget.getUri()),
                 bodyPath, new ByteArrayInputStream(out.toByteArray()), "application/rdf+xml");
             incrementStepsComplete();
         }
 
 
         /**
          * Generate a path for an annotation body of a resource. The template is ["ro"|resource_name] + "-" +
          * random_string.
          * 
          * @param targetName
          *            the last segment or full URI
          * @return an annotation body path relative to the RO URI
          */
         private static String createAnnotationBodyPath(String targetName) {
             if (targetName.endsWith("/")) {
                 targetName = targetName.substring(0, targetName.length() - 1);
             }
             String randomBit = "" + Math.abs(UUID.randomUUID().getLeastSignificantBits());
             return ".ro/" + targetName + "-" + randomBit + ".ttl";
         }
 
 
         /**
          * Deserialize a myExperiment complete metadata file.
          * 
          * @param xml
          *            XML with metadata
          * @param resourceClass
          *            Java class
          * @return deserialized metadata object
          * @throws JAXBException
          *             when the metadata could not be parsed
          */
         private static Object createMyExpResource(String xml, Class<? extends BaseResource> resourceClass)
                 throws JAXBException {
             JAXBContext jc = JAXBContext.newInstance(resourceClass);
             Unmarshaller u = jc.createUnmarshaller();
             StringBuffer xmlStr = new StringBuffer(xml);
             return u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
         }
 
 
         /**
          * Mark one more import step done.
          */
         private void incrementStepsComplete() {
             stepsComplete++;
             model.setProgressInPercent((int) Math.round((double) stepsComplete / stepsTotal * 100));
         }
 
     }
 
 
     /**
      * Create a Jena model with selected statements built from myExperiment metadata.
      * 
      * @param targetURI
      *            the subject of the statements
      * @param myExperimentRDF
      *            the RDF graph
      * @return a Jena model describing the subject
      */
     static Model createAnnotationBody(URI targetURI, String myExperimentRDF) {
         OntModel me = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         try {
             me.read(new ByteArrayInputStream(myExperimentRDF.getBytes("UTF-8")), null);
         } catch (UnsupportedEncodingException e) {
             LOG.error("UTF-8 is not supported", e);
         }
         Model body = ModelFactory.createDefaultModel();
 
         Resource target = body.createResource(targetURI.toString());
 
         // source
         Resource source = me.listObjectsOfProperty(FOAF.primaryTopic).next().asResource();
         target.addProperty(DCTerms.source, source);
 
         // title
         if (source.hasProperty(DCTerms.title)) {
             target.addProperty(DCTerms.title, source.getProperty(DCTerms.title).getLiteral());
         }
 
         // description
         if (source.hasProperty(DCTerms.description)) {
             target.addProperty(DCTerms.description, source.getProperty(DCTerms.description).getLiteral());
         }
 
         // creator
         Property owner = me.createProperty("http://rdfs.org/sioc/ns#has_owner");
         if (source.hasProperty(owner)) {
             target.addProperty(DCTerms.creator, source.getPropertyResourceValue(owner));
         }
         return body;
     }
 
 
     /**
      * Get a sample creator from an RDF graph.
      * 
      * @param myExperimentRDF
      *            an RDF graph
      * @return URI of a sample author in the graph
      * @throws URISyntaxException
      *             when the author URI is not valid
      */
     static URI getResourceAuthor(String myExperimentRDF)
             throws URISyntaxException {
         OntModel me = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         try {
             me.read(new ByteArrayInputStream(myExperimentRDF.getBytes("UTF-8")), null);
         } catch (UnsupportedEncodingException e) {
             LOG.error("UTF-8 is not supported", e);
         }
 
         Resource source = me.listObjectsOfProperty(FOAF.primaryTopic).next().asResource();
 
         // creator
         Property owner = me.createProperty("http://rdfs.org/sioc/ns#has_owner");
         if (source.hasProperty(owner)) {
             Resource user = source.getPropertyResourceValue(owner);
             return new URI(user.getURI());
         }
         return null;
     }
 
 }
