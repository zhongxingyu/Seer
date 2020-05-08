 package org.purl.wf4ever.rosrs.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;
 import org.purl.wf4ever.rosrs.client.evo.EvoType;
 import org.purl.wf4ever.rosrs.client.evo.JobStatus;
 import org.purl.wf4ever.rosrs.client.evo.ROEVOService;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.PROV;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 import pl.psnc.dl.wf4ever.vocabulary.ROEVO;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * ro:ResearchObject.
  * 
  * @author piotrekhol
  * 
  */
 public class ResearchObject extends Thing implements Annotable {
 
     /** id. */
     private static final long serialVersionUID = -2279202661374054080L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(ResearchObject.class);
 
     /** ROSRS client. */
     private final ROSRService rosrs;
 
     /** ROEVO client. */
     private final ROEVOService roevo;
 
     /** has the RO been loaded from ROSRS. */
     private boolean loaded;
 
     /** aggregated ro:Resources, excluding ro:Folders. */
     private Map<URI, Resource> resources;
 
     /** aggregated ro:Folders. */
     private Map<URI, Folder> folders;
 
     /** aggregated annotations, grouped based on ao:annotatesResource. */
     private Multimap<URI, Annotation> annotations;
 
     /** root folders of the RO. */
     private Set<Folder> rootFolders;
 
     /** RO title from annotations (any one in case of many). */
     private String title;
 
     /** RO description from annotations (any one in case of many). */
     private String description;
 
     /** RO evolution class from annotations (any one in case of many). */
     private EvoType evoType;
 
     /** All snapshots of this RO. */
     private Set<ResearchObject> snapshots = new HashSet<>();;
 
     /** All archives of this RO. */
     private Set<ResearchObject> archives = new HashSet<>();;
 
     /** Any live RO that this RO comes from. */
     private ResearchObject liveRO;
 
     /** A previous snapshot of this RO. */
     private ResearchObject previousSnapshot;
 
     /** Has the evolution information been loaded. */
     private boolean evolutionInformationLoaded;
 
 
     /**
      * Constructor.
      * 
      * @param uri
      *            RO URI
      * @param rosrs
      *            ROSRS client
      */
     public ResearchObject(URI uri, ROSRService rosrs) {
         super(uri, null, null);
         this.rosrs = rosrs;
         //HACK
         this.roevo = rosrs != null ? new ROEVOService(rosrs.getRosrsURI().resolve(".."), rosrs.getToken()) : null;
         this.loaded = false;
     }
 
 
     /**
      * Create a new Research Object.
      * 
      * @param rosrs
      *            ROSRS client
      * @param id
      *            RO id
      * @return the RO
      * @throws ROSRSException
      *             the creation failed
      */
     public static ResearchObject create(ROSRService rosrs, String id)
             throws ROSRSException {
         ClientResponse response = rosrs.createResearchObject(id);
         response.close();
         return new ResearchObject(response.getLocation(), rosrs);
     }
 
 
     public ROSRService getRosrs() {
         return rosrs;
     }
 
 
     public boolean isLoaded() {
         return loaded;
     }
 
 
     /**
      * Load and parse the manifest.
      * 
      * @throws ROSRSException
      *             could not download the manifest
      * @throws ROException
      *             the manifest is incorrect
      */
     public void load()
             throws ROSRSException, ROException {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         ClientResponse response = rosrs.getResource(uri, "application/rdf+xml");
         try {
             //HACK there's no way to get the URI after redirection, so we're using a fixed one which may change for different ROSR services
             model.read(response.getEntityInputStream(), uri.resolve(".ro/manifest.rdf").toString());
         } finally {
             try {
                 response.getEntityInputStream().close();
             } catch (IOException e) {
                 LOG.warn("Failed to close the manifest input stream", e);
             }
         }
         this.creator = extractCreator(model);
         this.created = extractCreated(model);
         this.resources = extractResources(model);
         this.folders = extractFolders(model);
         this.rootFolders = new HashSet<>();
         for (Folder folder : folders.values()) {
             if (folder.isRootFolder()) {
                 rootFolders.add(folder);
             }
         }
         this.annotations = extractAnnotations(model);
         for (Annotation annotation : this.getAnnotations()) {
             try {
                 annotation.load();
                 model.read(new ByteArrayInputStream(annotation.getBodySerializedAsString().getBytes()), null);
             } catch (ROSRSException e) {
                 LOG.error("Can't load annotation: " + annotation.getUri(), e);
             }
         }
         Map<Annotation, String> titles = getPropertyValues(URI.create(DCTerms.title.getURI()));
         // choose any
         this.title = titles.isEmpty() ? null : titles.values().iterator().next();
         Map<Annotation, String> descriptions = getPropertyValues(URI.create(DCTerms.description.getURI()));
         // choose any
         this.description = descriptions.isEmpty() ? null : descriptions.values().iterator().next();
         this.evoType = findEvoType(model);
         this.loaded = true;
     }
 
 
     /**
      * Look for the evo class of the RO.
      * 
      * @param model
      *            model to search in
      * @return any RO title found, null if not found
      */
     private EvoType findEvoType(OntModel model) {
         Individual ro = model.getIndividual(uri.toString());
         for (com.hp.hpl.jena.rdf.model.Resource clazz : ro.listRDFTypes(true).toSet()) {
             if (clazz.equals(ROEVO.LiveRO)) {
                 return EvoType.LIVE;
             } else if (clazz.equals(ROEVO.SnapshotRO)) {
                 return EvoType.SNAPSHOT;
             } else if (clazz.equals(ROEVO.ArchivedRO)) {
                 return EvoType.ARCHIVE;
             }
         }
         return null;
     }
 
 
     /**
      * Delete the research object.
      * 
      * @throws ROSRSException
      *             unexpected server response
      */
     public void delete()
             throws ROSRSException {
         this.rosrs.deleteResearchObject(uri);
         this.loaded = false;
         this.resources = null;
         this.folders = null;
         this.created = null;
         this.creator = null;
     }
 
 
     public Map<URI, Resource> getResources() {
         return resources;
     }
 
 
     /**
      * Get a resource with a given URI or null if doesn't exist.
      * 
      * @param resourceUri
      *            resource URI
      * @return resource instance or null
      */
     public Resource getResource(URI resourceUri) {
         return getResources().get(resourceUri);
     }
 
 
     public Map<URI, Folder> getFolders() {
         return folders;
     }
 
 
     /**
      * Returns resources which are not aggregated in any folder.
      * 
      * @return a list of resources sorted by name
      * @throws ROSRSException
      *             unexpected response from the server when loading the folders
      */
     public List<Resource> getResourcesWithoutFolders()
             throws ROSRSException {
         List<Resource> resourcesWithoutFolders = new ArrayList<>(getResources().values());
         for (Folder folder : getFolders().values()) {
             if (!folder.isLoaded()) {
                 folder.load(true);
             }
             resourcesWithoutFolders.removeAll(folder.getResources());
         }
         Collections.sort(resourcesWithoutFolders, new ResourceByNameComparator());
         return resourcesWithoutFolders;
     }
 
 
     public Set<Folder> getRootFolders() {
         return rootFolders;
     }
 
 
     /**
      * Get a folder with a given URI or null if doesn't exist.
      * 
      * @param folderURI
      *            folder URI
      * @return folder instance or null
      */
     public Folder getFolder(URI folderURI) {
         return folders.get(folderURI);
     }
 
 
     public Multimap<URI, Annotation> getAllAnnotations() {
         return annotations;
     }
 
 
     public String getTitle() {
         return title;
     }
 
 
     public void setTitle(String title) {
         this.title = title;
     }
 
 
     public String getDescription() {
         return description;
     }
 
 
     public EvoType getEvoType() {
         return evoType;
     }
 
 
     /**
      * Find the dcterms:creator of the RO.
      * 
      * @param model
      *            manifest model
      * @return creator URI or null if not defined
      * @throws ROException
      *             incorrect manifest
      */
     private URI extractCreator(OntModel model)
             throws ROException {
         Individual ro = model.getIndividual(uri.toString());
         if (ro == null) {
             throw new ROException("RO not found in the manifest", uri);
         }
         com.hp.hpl.jena.rdf.model.Resource c = ro.getPropertyResourceValue(DCTerms.creator);
         return c != null ? URI.create(c.getURI()) : null;
     }
 
 
     /**
      * Find the dcterms:created date of the RO.
      * 
      * @param model
      *            manifest model
      * @return creation date or null if not defined
      * @throws ROException
      *             incorrect manifest
      */
     private DateTime extractCreated(OntModel model)
             throws ROException {
         Individual ro = model.getIndividual(uri.toString());
         if (ro == null) {
             throw new ROException("RO not found in the manifest", uri);
         }
         RDFNode d = ro.getPropertyValue(DCTerms.created);
         if (d == null || !d.isLiteral()) {
             return null;
         }
         return DateTime.parse(d.asLiteral().getString());
     }
 
 
     /**
      * Identify ro:Resources that are not ro:Folders, aggregated by the RO.
      * 
      * @param model
      *            manifest model
      * @return a set of resources (not loaded)
      */
     private Map<URI, Resource> extractResources(OntModel model) {
         Map<URI, Resource> resources2 = new HashMap<>();
         String queryString = String
                 .format(
                     "PREFIX ore: <%s> PREFIX dcterms: <%s> PREFIX ro: <%s> SELECT ?resource ?proxy ?created ?creator WHERE { <%s> ore:aggregates ?resource . ?resource a ro:Resource . ?proxy ore:proxyFor ?resource . OPTIONAL { ?resource dcterms:creator ?creator . } OPTIONAL { ?resource dcterms:created ?created . } }",
                     ORE.NAMESPACE, DCTerms.NS, RO.NAMESPACE, uri.toString());
 
         Query query = QueryFactory.create(queryString);
         QueryExecution qe = QueryExecutionFactory.create(query, model);
         try {
             ResultSet results = qe.execSelect();
             while (results.hasNext()) {
                 QuerySolution solution = results.next();
                 RDFNode r = solution.get("resource");
                 if (r.as(Individual.class).hasRDFType(RO.Folder)) {
                     continue;
                 }
                 URI rURI = URI.create(r.asResource().getURI());
                 RDFNode p = solution.get("proxy");
                 RDFNode creatorNode = solution.get("creator");
                 URI resCreator = creatorNode != null && creatorNode.isURIResource() ? URI.create(creatorNode
                         .asResource().getURI()) : null;
                 RDFNode createdNode = solution.get("created");
                 DateTime resCreated = createdNode != null && createdNode.isLiteral() ? DateTime.parse(createdNode
                         .asLiteral().getString()) : null;
                 resources2.put(rURI, new Resource(this, rURI, URI.create(p.asResource().getURI()), resCreator,
                         resCreated));
             }
         } finally {
             qe.close();
         }
 
         return resources2;
     }
 
 
     /**
      * Identify ro:Resources that are not ro:Folders, aggregated by the RO.
      * 
      * @param model
      *            manifest model
      * @return a set of folders (not loaded)
      */
     private Map<URI, Folder> extractFolders(OntModel model) {
         Map<URI, Folder> folders2 = new HashMap<>();
         String queryString = String
                 .format(
                     "PREFIX ore: <%s> PREFIX dcterms: <%s> PREFIX ro: <%s> SELECT ?folder ?proxy ?resourcemap ?created ?creator WHERE { <%s> ore:aggregates ?folder . ?folder a ro:Folder ; ore:isDescribedBy ?resourcemap . ?proxy ore:proxyFor ?folder . OPTIONAL { ?folder dcterms:creator ?creator . } OPTIONAL { ?folder dcterms:created ?created . } }",
                     ORE.NAMESPACE, DCTerms.NS, RO.NAMESPACE, uri.toString());
 
         Query query = QueryFactory.create(queryString);
         QueryExecution qe = QueryExecutionFactory.create(query, model);
         try {
             ResultSet results = qe.execSelect();
             while (results.hasNext()) {
                 QuerySolution solution = results.next();
                 RDFNode f = solution.get("folder");
                 URI fURI = URI.create(f.asResource().getURI());
                 RDFNode p = solution.get("proxy");
                 RDFNode rm = solution.get("resourcemap");
                 RDFNode creatorNode = solution.get("creator");
                 URI resCreator = creatorNode != null && creatorNode.isURIResource() ? URI.create(creatorNode
                         .asResource().getURI()) : null;
                 RDFNode createdNode = solution.get("created");
                 DateTime resCreated = createdNode != null && createdNode.isLiteral() ? DateTime.parse(createdNode
                         .asLiteral().getString()) : null;
 
                 String queryString2 = String.format("PREFIX ro: <%s> ASK { <%s> ro:rootFolder <%s> }", RO.NAMESPACE,
                     uri.toString(), fURI.toString());
                 Query query2 = QueryFactory.create(queryString2);
                 QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
                 boolean isRootFolder = false;
                 try {
                     isRootFolder = qe2.execAsk();
                 } finally {
                     qe2.close();
                 }
 
                 folders2.put(fURI,
                     new Folder(this, fURI, URI.create(p.asResource().getURI()), URI.create(rm.asResource().getURI()),
                             resCreator, resCreated, isRootFolder));
             }
         } finally {
             qe.close();
         }
 
         return folders2;
     }
 
 
     /**
      * Identify ro:AggregatedAnnotations that aggregated by the RO.
      * 
      * @param model
      *            manifest model
      * @return a multivalued map of annotations, with bodies not loaded
      */
     private Multimap<URI, Annotation> extractAnnotations(OntModel model) {
         Multimap<URI, Annotation> annotations2 = HashMultimap.<URI, Annotation> create();
         Map<URI, Annotation> annotationsByUri = new HashMap<>();
         String queryString = String
                 .format(
                     "PREFIX ore: <%s> PREFIX dcterms: <%s> PREFIX ao: <%s> PREFIX ro: <%s> SELECT ?annotation ?body ?target ?created ?creator WHERE { <%s> ore:aggregates ?annotation . ?annotation a ro:AggregatedAnnotation ; ao:body ?body ; ro:annotatesAggregatedResource ?target . OPTIONAL { ?annotation dcterms:creator ?creator . } OPTIONAL { ?annotation dcterms:created ?created . } }",
                     ORE.NAMESPACE, DCTerms.NS, AO.NAMESPACE, RO.NAMESPACE, uri.toString());
 
         Query query = QueryFactory.create(queryString);
         QueryExecution qe = QueryExecutionFactory.create(query, model);
         try {
             ResultSet results = qe.execSelect();
             while (results.hasNext()) {
                 QuerySolution solution = results.next();
                 RDFNode a = solution.get("annotation");
                 URI aURI = URI.create(a.asResource().getURI());
                 RDFNode t = solution.get("target");
                 URI tURI = URI.create(t.asResource().getURI());
                 Annotation annotation;
                 if (annotationsByUri.containsKey(aURI)) {
                     annotation = annotationsByUri.get(aURI);
                     annotation.getTargets().add(tURI);
                 } else {
                     RDFNode b = solution.get("body");
                     RDFNode creatorNode = solution.get("creator");
                     URI resCreator = creatorNode != null && creatorNode.isURIResource() ? URI.create(creatorNode
                             .asResource().getURI()) : null;
                     RDFNode createdNode = solution.get("created");
                     DateTime resCreated = createdNode != null && createdNode.isLiteral() ? DateTime.parse(createdNode
                             .asLiteral().getString()) : null;
                     annotation = new Annotation(this, aURI, URI.create(b.asResource().getURI()), tURI, resCreator,
                             resCreated);
                 }
                 annotations2.put(tURI, annotation);
             }
         } finally {
             qe.close();
         }
 
         return annotations2;
     }
 
 
     /**
      * Add an internal resource to the research object.
      * 
      * @param path
      *            resource path, relative to the RO URI
      * @param content
      *            resource content
      * @param contentType
      *            resource Content Type
      * @return the resource instance
      * @throws ROSRSException
      *             server returned an unexpected response
      * @throws ROException
      *             the manifest is incorrect
      */
     public Resource aggregate(String path, InputStream content, String contentType)
             throws ROSRSException, ROException {
         Resource resource = Resource.create(this, path, content, contentType);
         if (!loaded) {
             load();
         }
         this.resources.put(resource.getUri(), resource);
         return resource;
     }
 
 
     /**
      * Add an external resource (a reference to a resource) to the research object.
      * 
      * @param uri
      *            resource URI
      * @return the resource instance
      * @throws ROSRSException
      *             server returned an unexpected response
      * @throws ROException
      *             the manifest is incorrect
      */
     public Resource aggregate(URI uri)
             throws ROSRSException, ROException {
         Resource resource = Resource.create(this, uri);
         if (!loaded) {
             load();
         }
         this.resources.put(resource.getUri(), resource);
         return resource;
     }
 
 
     /**
      * Create a new folder in the RO.
      * 
      * @param path
      *            folder path
      * @return folder instance
      * @throws ROSRSException
      *             server returned an unexpected response
      * @throws ROException
      *             the manifest is incorrect
      */
     public Folder createFolder(String path)
             throws ROSRSException, ROException {
         Folder folder = Folder.create(this, path);
         if (!loaded) {
             load();
         }
         this.folders.put(folder.getUri(), folder);
         //FIXME seems that the manifest needs to be reloaded to fetch creator/created/rootfolder
         return folder;
     }
 
 
     /**
      * Add an annotation about this research object.
      * 
      * @param target
      *            resource that is annotated
      * @param path
      *            resource path, relative to the RO URI
      * @param content
      *            resource content
      * @param contentType
      *            resource Content Type
      * @return the resource instance
      * @throws ROSRSException
      *             server returned an unexpected response
      * @throws ROException
      *             the manifest is incorrect
      */
     public Annotation annotate(Annotable target, String path, InputStream content, String contentType)
             throws ROSRSException, ROException {
         Resource body = aggregate(path, content, contentType);
         Annotation annotation = Annotation.create(this, body.getUri(), target.getUri());
         if (!loaded) {
             load();
         }
         this.annotations.put(target.getUri(), annotation);
         this.resources.remove(body.getUri());
         return annotation;
     }
 
 
     /**
      * Add an annotation.
      * 
      * @param path
      *            resource path, relative to the RO URI
      * @param content
      *            resource content
      * @param contentType
      *            resource Content Type
      * @return the resource instance
      * @throws ROSRSException
      *             server returned an unexpected response
      * @throws ROException
      *             the manifest is incorrect
      */
     public Annotation annotate(String path, InputStream content, String contentType)
             throws ROSRSException, ROException {
         return annotate(this, path, content, contentType);
     }
 
 
     /**
      * Remove references to the resource and the annotations about it.
      * 
      * @param resource
      *            resource to delete
      */
     void removeResource(Resource resource) {
         if (resources != null) {
             this.resources.remove(resource.getUri());
         }
         if (annotations != null) {
             for (Annotation annotation : this.annotations.get(resource.getUri())) {
                 annotation.getTargets().remove(resource.getUri());
             }
             this.annotations.removeAll(resource.getUri());
         }
     }
 
 
     /**
      * Remove references to the folder and the annotations about it.
      * 
      * @param folder
      *            folder to delete
      */
     void removeFolder(Folder folder) {
         if (folders != null) {
             this.folders.remove(folder.getUri());
         }
         if (annotations != null) {
             for (Annotation annotation : this.annotations.get(folder.getUri())) {
                 annotation.getTargets().remove(folder.getUri());
             }
             this.annotations.removeAll(folder.getUri());
         }
     }
 
 
     /**
      * Remove references to the annotation.
      * 
      * @param annotation
      *            the annotation
      */
     void removeAnnotation(Annotation annotation) {
         if (annotations == null) {
             return;
         }
         for (URI target : annotation.getTargets()) {
             annotations.get(target).remove(annotation);
         }
 
     }
 
 
     @Override
     public Collection<Annotation> getAnnotations() {
         return getAllAnnotations() != null ? getAllAnnotations().get(uri) : Collections.<Annotation> emptyList();
     }
 
 
     /**
      * Start the snapshotting operation. The snapshot will be immediately frozen.
      * 
      * @param target
      *            id of the snapshot
      * @return the job status describing the progress of the operation
      */
     public JobStatus snapshot(String target) {
         return roevo.createSnapshot(uri, target, true);
     }
 
 
     /**
      * Start the archival operation. The archive will be immediately frozen.
      * 
      * @param target
      *            id of the archive
      * @return the job status describing the progress of the operation
      */
     public JobStatus archive(String target) {
         return roevo.createArchive(uri, target, true);
     }
 
 
     /**
      * Get the ontology model with the evolution information.
      */
     public void loadEvolutionInformation() {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         try (InputStream in = roevo.getEvolutionInformationInputStream(uri)) {
             model.read(in, null, "TURTLE");
         } catch (IOException e) {
             LOG.error("Could not close the input stream", e);
         }
         this.evoType = findEvoType(model);
         Individual thisRO = model.getIndividual(uri.toString());
         if (thisRO == null) {
             LOG.warn("The evolution information has no info about this RO (" + uri + ")");
             return;
         }
         com.hp.hpl.jena.rdf.model.Resource liveR = thisRO.getPropertyResourceValue(ROEVO.isArchiveOf);
         if (liveR == null) {
             liveR = thisRO.getPropertyResourceValue(ROEVO.isSnapshotOf);
         }
         if (liveR != null && liveR.isURIResource()) {
             liveRO = new ResearchObject(URI.create(liveR.getURI()), rosrs);
         }
         Set<RDFNode> archivesR = thisRO.listPropertyValues(ROEVO.hasArchive).toSet();
         for (RDFNode node : archivesR) {
             if (node.isURIResource()) {
                 archives.add(new ResearchObject(URI.create(node.asResource().getURI()), rosrs));
             }
         }
         Set<RDFNode> snapshotsR = thisRO.listPropertyValues(ROEVO.hasSnapshot).toSet();
         for (RDFNode node : snapshotsR) {
             if (node.isURIResource()) {
                 snapshots.add(new ResearchObject(URI.create(node.asResource().getURI()), rosrs));
             }
         }
         com.hp.hpl.jena.rdf.model.Resource previousR = thisRO.getPropertyResourceValue(PROV.wasRevisionOf);
         if (previousR != null && previousR.isURIResource()) {
             previousSnapshot = new ResearchObject(URI.create(previousR.getURI()), rosrs);
         }
         evolutionInformationLoaded = true;
     }
 
 
     public boolean isEvolutionInformationLoaded() {
         return evolutionInformationLoaded;
     }
 
 
     public Set<ResearchObject> getSnapshots() {
         return snapshots;
     }
 
 
     public Set<ResearchObject> getArchives() {
         return archives;
     }
 
 
     public ResearchObject getLiveRO() {
         return liveRO;
     }
 
 
     public ResearchObject getPreviousSnapshot() {
         return previousSnapshot;
     }
 
 
     @Override
     public Map<Annotation, String> getPropertyValues(URI property) {
         Map<Annotation, String> map = new HashMap<>();
         for (Annotation annotation : getAnnotations()) {
             try {
                 List<String> literals = annotation.getPropertyValues(this, property);
                 if (!literals.isEmpty()) {
                     map.put(annotation, StringUtils.join(literals, "; "));
                 }
             } catch (ROSRSException e) {
                 LOG.error("Can't load annotation body", e);
             }
         }
         return map;
     }
 
 
     @Override
     public Annotation createPropertyValue(URI property, String value)
             throws ROSRSException, ROException {
         return this.annotate(null,
             Annotation.wrapAnnotationBody(Collections.singletonList(new Statement(this.getUri(), property, value))),
            RDFFormat.RDFXML.getDefaultMIMEType());
     }
 
 
     @Override
     public Annotation updatePropertyValue(Annotation annotation, URI property, String value)
             throws ROSRSException {
         annotation.updatePropertyValue(this, property, value);
         annotation.update();
         return annotation;
     }
 
 
     @Override
     public void deletePropertyValue(Annotation annotation, URI property)
             throws ROSRSException {
         annotation.deletePropertyValue(this, property);
         if (annotation.getStatements().isEmpty()) {
             annotation.delete();
         } else {
             annotation.update();
         }
     }
 }
