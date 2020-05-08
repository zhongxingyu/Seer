 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal.services;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.log4j.Logger;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 
 import pl.psnc.dl.wf4ever.portal.model.RoFactory;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.BaseResource;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.InternalPackItem;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.InternalPackItemHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.Pack;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.PackHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.ResourceHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.SimpleResource;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.SimpleResourceHeader;
 import pl.psnc.dl.wf4ever.portal.myexpimport.model.User;
 import pl.psnc.dl.wf4ever.portal.myexpimport.wizard.ImportModel;
 import pl.psnc.dl.wf4ever.portal.myexpimport.wizard.ImportModel.ImportStatus;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.AnonId;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 /**
  * @author Piotr Hołubowicz
  * 
  */
 public class MyExpImportService
 {
 
 	public static void startImport(ImportModel model, Token myExpAccessToken, Token dLibraAccessToken,
 			String consumerKey, String consumerSecret)
 	{
 		new ImportThread(model, myExpAccessToken, dLibraAccessToken, consumerKey, consumerSecret).start();
 	}
 
 
 	/**
 	 * @param user
 	 * @param service
 	 * @return
 	 * @throws OAuthException
 	 * @throws JAXBException
 	 */
 	public static User retrieveMyExpUser(Token accessToken, OAuthService service)
 		throws OAuthException, JAXBException
 	{
 		User myExpUser;
 		Response response = OAuthHelpService.sendRequest(service, Verb.GET, MyExpApi.WHOAMI_URL, accessToken);
 		myExpUser = createMyExpUserModel(response.getBody());
 
 		response = OAuthHelpService.sendRequest(service, Verb.GET,
 			URI.create(String.format(MyExpApi.GET_USER_URL_TMPL, myExpUser.getId())), accessToken);
 		myExpUser = createMyExpUserModel(response.getBody());
 		return myExpUser;
 	}
 
 
 	private static User createMyExpUserModel(String xml)
 		throws JAXBException
 	{
 		JAXBContext jc = JAXBContext.newInstance(User.class);
 
 		Unmarshaller u = jc.createUnmarshaller();
 		StringBuffer xmlStr = new StringBuffer(xml);
 		return (User) u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
 	}
 
 	private static class ImportThread
 		extends Thread
 	{
 
 		private static final Logger log = Logger.getLogger(ImportThread.class);
 
 		private final OAuthService service;
 
 		private final ImportModel model;
 
 		private final Token myExpToken;
 
 		private final Token dLibraToken;
 
 		private int stepsTotal = 0;
 
 		private int stepsComplete = 0;
 
 		private URI researchObjectURI;
 
 		private OntModel manifest;
 
 		/**
 		 * bodyURI, bodyRDF
 		 */
 		private final Map<URI, Model> annBodies = new HashMap<>();
 
 		/**
 		 * targetURI, bodyURI
 		 */
 		private final Map<URI, URI> annotations = new HashMap<>();
 
 
 		public ImportThread(ImportModel importModel, Token myExpAccessToken, Token dLibraToken, String consumerKey,
 				String consumerSecret)
 		{
 			super();
 			model = importModel;
 			myExpToken = myExpAccessToken;
 			this.dLibraToken = dLibraToken;
 			service = MyExpApi.getOAuthService(consumerKey, consumerSecret);
 		}
 
 
 		@Override
 		public void run()
 		{
 			model.setStatus(ImportStatus.RUNNING);
 			model.setMessage("Preparing the data");
 
 			try {
 				List<Pack> packs = getPacks(model.getSelectedPacks());
 				if (model.getCustomPackId() != null) {
 					packs.add(getPack(model.getCustomPackId()));
 				}
 				int simpleResourcesCnt = model.getSelectedFiles().size() + model.getSelectedWorkflows().size();
 				for (Pack pack : packs) {
 					simpleResourcesCnt += pack.getResources().size();
 				}
 				stepsTotal = simpleResourcesCnt * 4 + packs.size() * 2 + 3;
 
 				researchObjectURI = createRO(model.getRoId());
 				importSimpleResources(model.getSelectedFiles());
 				importSimpleResources(model.getSelectedWorkflows());
 				importPacks(packs);
 				manifest = getManifest();
 				updateManifest();
 				uploadAnnotations();
 				model.setMessage("Finished");
 			}
 			catch (Exception e) {
 				log.error("Error during import", e);
 				model.setMessage("Error during import: " + e.getMessage());
 			}
 			model.setStatus(ImportStatus.FINISHED);
 		}
 
 
 		private URI createRO(String roId)
 			throws Exception
 		{
 			model.setMessage(String.format("Creating a Research Object \"%s\"", roId));
 			URI roURI = ROSRService.createResearchObject(roId, dLibraToken, model.isMergeROs());
 			incrementStepsComplete();
 			return roURI;
 		}
 
 
 		private OntModel getManifest()
 			throws OAuthException
 		{
 			model.setMessage("Downloading the manifest");
 			InputStream is = ROSRService.getResource(researchObjectURI.resolve(".ro/manifest"));
 			OntModel manifest = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
 			manifest.read(is, null);
 			incrementStepsComplete();
 			return manifest;
 		}
 
 
 		private void updateManifest()
 			throws Exception
 		{
 			model.setMessage("Updating the manifest");
 			for (Entry<URI, URI> e : annotations.entrySet()) {
 				addAnnotation(manifest, researchObjectURI, e.getKey(), e.getValue());
 			}
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			manifest.write(out);
 			ROSRService.sendResource(researchObjectURI.resolve(".ro/manifest"), out.toByteArray(),
 				"application/rdf+xml", dLibraToken);
 			incrementStepsComplete();
 		}
 
 
 		private void uploadAnnotations()
 			throws Exception
 		{
 			for (Entry<URI, Model> e : annBodies.entrySet()) {
 				model.setMessage(String.format("Uploading annotation body %s", e.getKey()));
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				e.getValue().write(out);
 				URI annBodyURI = e.getKey();
 				ROSRService.sendResource(annBodyURI, out.toByteArray(), "application/rdf+xml", dLibraToken);
 				incrementStepsComplete();
 			}
 		}
 
 
 		private void importSimpleResources(List< ? extends SimpleResourceHeader> resourceHeaders)
 			throws JAXBException, Exception
 		{
 			for (SimpleResourceHeader header : resourceHeaders) {
 				SimpleResource r = importSimpleResource(header, header.getResourceClass());
 				downloadResourceMetadata(r);
 			}
 		}
 
 
 		private List<Pack> getPacks(List<PackHeader> packHeaders)
 			throws OAuthException, JAXBException
 		{
 			List<Pack> packs = new ArrayList<Pack>();
 			for (PackHeader packHeader : packHeaders) {
 				packs.add((Pack) getResource(packHeader, Pack.class));
 			}
 			return packs;
 		}
 
 
 		private Pack getPack(String customPackId)
 			throws OAuthException, JAXBException
 		{
 			PackHeader packHeader = new PackHeader();
 			packHeader.setUri(URI.create("http://www.myexperiment.org/pack.xml?id=" + customPackId));
 			return (Pack) getResource(packHeader, Pack.class);
 		}
 
 
 		/**
 		 * @param roName
 		 * @param packs
 		 * @param model
 		 * @param ro
 		 * @param myExpToken
 		 * @param dLibraUser
 		 * @throws JAXBException
 		 * @throws Exception
 		 */
 		private void importPacks(List<Pack> packs)
 			throws JAXBException, Exception
 		{
 			for (Pack pack : packs) {
 				downloadResourceMetadata(pack);
 
 				for (InternalPackItemHeader packItemHeader : pack.getResources()) {
 					importInternalPackItem(pack, packItemHeader);
 				}
 			}
 		}
 
 
 		/**
 		 * @param model
 		 * @param ro
 		 * @param myExpToken
 		 * @param user
 		 * @param pack
 		 * @param r
 		 * @throws JAXBException
 		 * @throws Exception
 		 */
 		private void importInternalPackItem(Pack pack, InternalPackItemHeader packItemHeader)
 			throws JAXBException, Exception
 		{
 			InternalPackItem internalItem = (InternalPackItem) getResource(packItemHeader, InternalPackItem.class);
 			SimpleResourceHeader resourceHeader = internalItem.getItem();
 			SimpleResource r = importSimpleResource(resourceHeader, resourceHeader.getResourceClass());
 			downloadResourceMetadata(r);
 		}
 
 
 		private SimpleResource importSimpleResource(SimpleResourceHeader res,
 				Class< ? extends SimpleResource> resourceClass)
 			throws Exception
 		{
 			SimpleResource r = (SimpleResource) getResource(res, resourceClass);
 			incrementStepsComplete();
 
 			model.setMessage(String.format("Uploading %s", r.getFilename()));
			ROSRService.sendResource(researchObjectURI.resolve(r.getFilename()), r.getContentDecoded(),
 				r.getContentType(), dLibraToken);
 
 			incrementStepsComplete();
 			return r;
 		}
 
 
 		/**
 		 * @param res
 		 * @param path
 		 * @param resourceClass
 		 * @return
 		 * @throws OAuthException
 		 * @throws JAXBException
 		 */
 		private BaseResource getResource(ResourceHeader res, Class< ? extends BaseResource> resourceClass)
 			throws OAuthException, JAXBException
 		{
 			model.setMessage(String.format("Downloading %s", res.getResourceUrl()));
 			Response response = OAuthHelpService.sendRequest(service, Verb.GET, res.getResourceUrl(), myExpToken);
 			BaseResource r = (BaseResource) createMyExpResource(response.getBody(), resourceClass);
 			return r;
 		}
 
 
 		private void downloadResourceMetadata(BaseResource res)
 			throws Exception
 		{
 			model.setMessage(String.format("Downloading metadata file %s", res.getResource()));
 			Response response = OAuthHelpService.sendRequest(service, Verb.GET, res.getResource(), myExpToken,
 				"application/rdf+xml");
 			// in the future, the RDF could be parsed (and somewhat validated)
 			// and the filename can be extracted from it
 			String rdf = response.getBody();
 			URI annTargetURI;
 			if (res instanceof SimpleResource) {
				annTargetURI = researchObjectURI.resolve(((SimpleResource) res).getFilename());
 			}
 			else {
 				annTargetURI = researchObjectURI;
 			}
 			URI bodyURI = createAnnotationBodyURI(researchObjectURI, annTargetURI);
 			annBodies.put(bodyURI, createAnnotationBody(annTargetURI, rdf));
 			annotations.put(annTargetURI, bodyURI);
 
 			incrementStepsComplete();
 		}
 
 
 		private static Object createMyExpResource(String xml, Class< ? extends BaseResource> resourceClass)
 			throws JAXBException
 		{
 			JAXBContext jc = JAXBContext.newInstance(resourceClass);
 			Unmarshaller u = jc.createUnmarshaller();
 			StringBuffer xmlStr = new StringBuffer(xml);
 			return u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
 		}
 
 
 		private void incrementStepsComplete()
 		{
 			stepsComplete++;
 			model.setProgressInPercent((int) Math.round((double) stepsComplete / stepsTotal * 100));
 		}
 
 	}
 
 
 	static Model createAnnotationBody(URI targetURI, String myExperimentRDF)
 		throws UnsupportedEncodingException
 	{
 		OntModel me = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
 		me.read(new ByteArrayInputStream(myExperimentRDF.getBytes("UTF-8")), null);
 		Model body = ModelFactory.createDefaultModel();
 
 		Resource target = body.createResource(targetURI.toString());
 
 		// source
 		Resource source = me.listObjectsOfProperty(RoFactory.foafPrimaryTopic).next().asResource();
 		target.addProperty(DCTerms.source, source);
 
 		//title
 		if (source.hasProperty(DCTerms.title))
 			target.addProperty(DCTerms.title, source.getProperty(DCTerms.title).getLiteral());
 
 		//description
 		if (source.hasProperty(DCTerms.description))
 			target.addProperty(DCTerms.description, source.getProperty(DCTerms.description).getLiteral());
 
 		//contributor
 		Property owner = me.createProperty("http://rdfs.org/sioc/ns#has_owner");
 		if (source.hasProperty(owner))
 			target.addProperty(DCTerms.contributor, source.getPropertyResourceValue(owner));
 		return body;
 	}
 
 
 	static void addAnnotation(OntModel manifest, URI researchObjectURI, URI targetURI, URI bodyURI)
 		throws URISyntaxException
 	{
 		Individual ann = manifest.createIndividual(createAnnotationURI(manifest, researchObjectURI).toString(),
 			RoFactory.aggregatedAnnotation);
 		ann.addProperty(RoFactory.annotatesResource, manifest.createResource(targetURI.toString()));
 		ann.addProperty(RoFactory.aoBody, manifest.createResource(bodyURI.toString()));
 		ann.addProperty(DCTerms.created, manifest.createTypedLiteral(Calendar.getInstance()));
 		Individual agent = manifest.createResource(new AnonId("myExperiment")).as(Individual.class);
 		agent.setOntClass(RoFactory.foafAgent);
 		agent.addProperty(RoFactory.foafName, "myExperiment");
 		ann.addProperty(DCTerms.creator, agent);
 	}
 
 
 	/**
 	 * 
 	 * @param researchObjectURI
 	 * @param targetURI
 	 * @return i.e. http://sandbox.wf4ever-project.org/rosrs5/ROs/ann217/.ro/ro--5600459667350895101.rdf
 	 * @throws URISyntaxException
 	 */
 	static URI createAnnotationBodyURI(URI researchObjectURI, URI targetURI)
 		throws URISyntaxException
 	{
 		String targetName;
 		if (targetURI.equals(researchObjectURI))
 			targetName = "ro";
 		else
 			targetName = targetURI.resolve(".").relativize(targetURI).toString();
 		String randomBit = "" + Math.abs(UUID.randomUUID().getLeastSignificantBits());
 
 		return researchObjectURI.resolve(".ro/" + targetName + "-" + randomBit + ".rdf");
 	}
 
 
 	/**
 	 * 
 	 * @param manifest
 	 * @param researchObjectURI
 	 * @return i.e. http://sandbox.wf4ever-project.org/rosrs5/ROs/ann217/52a272f1-864f-4a42-89ff-2501a739d6f0
 	 */
 	static URI createAnnotationURI(OntModel manifest, URI researchObjectURI)
 	{
 		URI ann = null;
 		do {
 			ann = researchObjectURI.resolve(UUID.randomUUID().toString());
 		}
 		while (manifest.containsResource(manifest.createResource(ann.toString())));
 		return ann;
 	}
 }
