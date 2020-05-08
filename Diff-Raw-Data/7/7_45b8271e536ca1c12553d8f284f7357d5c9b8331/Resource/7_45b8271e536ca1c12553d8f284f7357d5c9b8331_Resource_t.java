 package org.purl.wf4ever.rosrs.client;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.HttpStatus;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.openrdf.rio.RDFFormat;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * ro:Resource.
  * 
  * @author piotrekhol
  * 
  */
 public class Resource extends Thing implements Annotable {
 
     /** id. */
     private static final long serialVersionUID = 7593887876508190085L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(Resource.class);
 
     /** The RO it is aggregated by. */
     protected final ResearchObject researchObject;
 
     /** URI of the proxy. */
     protected final URI proxyUri;
 
     /** Resource size in bytes. */
     protected long size = -1;
 
     /** Is this resource a nested RO? */
     boolean nestedRO = false;
 
 
     /**
      * Constructor.
      * 
      * @param researchObject
      *            The RO it is aggregated by
      * @param uri
      *            resource URI
      * @param proxyURI
      *            URI of the proxy
      * @param creator
      *            author of the resource
      * @param created
      *            creation date
      */
     public Resource(ResearchObject researchObject, URI uri, URI proxyURI, Person creator, DateTime created) {
         super(uri, creator, created);
         this.researchObject = researchObject;
         this.proxyUri = proxyURI;
     }
 
 
     /**
      * Add an internal resource to the research object. Does not add the resource instance to the {@link ResearchObject}
      * instance.
      * 
      * @param researchObject
      *            the RO
      * @param path
      *            resource path, relative to the RO URI
      * @param content
      *            resource content
      * @param contentType
      *            resource Content Type
      * @return the resource instance
      * @throws ROSRSException
      *             server returned an unexpected response
      */
     public static Resource create(ResearchObject researchObject, String path, InputStream content, String contentType)
             throws ROSRSException {
         ClientResponse response = researchObject.getRosrs().aggregateInternalResource(researchObject.getUri(), path,
             content, contentType);
         if (response.getStatus() != HttpStatus.SC_CREATED) {
             throw new ROSRSException("Can't create a resource", response);
         }
         Multimap<String, URI> headers = Utils.getLinkHeaders(response.getHeaders().get("Link"));
         URI resourceUri = headers.get(ORE.proxyFor.getURI()).isEmpty() ? null : headers.get(ORE.proxyFor.getURI())
                 .iterator().next();
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(response.getEntityInputStream(), null);
         response.close();
         return readFromModel(researchObject, response.getLocation(), resourceUri, model);
     }
 
 
     /**
      * Create a resource based on an RDF description.
      * 
      * @param researchObject
      *            the research object
      * @param proxyUri
      *            proxy URI. If null, will be searched for in the model
      * @param resourceUri
      *            resource URI
      * @param model
      *            Jena model
      * @return a new resource
      */
     static Resource readFromModel(ResearchObject researchObject, URI proxyUri, URI resourceUri, OntModel model) {
         Individual r = model.getIndividual(resourceUri.toString());
         com.hp.hpl.jena.rdf.model.Resource creatorNode = r.getPropertyResourceValue(DCTerms.creator);
         Person resCreator = Person.create(creatorNode);
         RDFNode createdNode = r.getPropertyValue(DCTerms.created);
         DateTime resCreated = createdNode != null && createdNode.isLiteral() ? DateTime.parse(createdNode.asLiteral()
                 .getString()) : null;
         if (proxyUri == null) {
             List<com.hp.hpl.jena.rdf.model.Resource> proxies = model.listSubjectsWithProperty(ORE.proxyFor, r).toList();
             if (!proxies.isEmpty()) {
                 proxyUri = URI.create(proxies.get(0).getURI());
             }
         }
         return new Resource(researchObject, resourceUri, proxyUri, resCreator, resCreated);
     }
 
 
     /**
      * Add an external resource (a reference to a resource) to the research object. Does not add the resource instance
      * to the {@link ResearchObject} instance.
      * 
      * @param researchObject
      *            the RO
      * @param uri
      *            resource URI
      * @return the resource instance
      * @throws ROSRSException
      *             server returned an unexpected response
      */
     public static Resource create(ResearchObject researchObject, URI uri)
             throws ROSRSException {
         ClientResponse response = researchObject.getRosrs().aggregateExternalResource(researchObject.getUri(), uri);
         Multimap<String, URI> headers = Utils.getLinkHeaders(response.getHeaders().get("Link"));
         URI resource = headers.get(ORE.proxyFor.getURI()).isEmpty() ? null : headers.get(ORE.proxyFor.getURI())
                 .iterator().next();
         response.close();
         //FIXME creator/created dates are null but see WFE-758
         return new Resource(researchObject, resource, response.getLocation(), null, null);
     }
 
 
     /**
      * Delete the resource from ROSRS and from the research object.
      * 
      * @throws ROSRSException
      *             server returned an unexpected response
      */
     public void delete()
             throws ROSRSException {
         researchObject.getRosrs().deleteResource(proxyUri);
         researchObject.removeResource(this);
     }
 
 
     /**
      * Update the content of an aggregated resource.
      * 
      * @param content
      *            content
      * @param contentType
      *            content MIME type
      * @throws ROSRSException
      *             unexpected response from the server
      */
     public void update(InputStream content, String contentType)
             throws ROSRSException {
         researchObject.getRosrs().updateResource(uri, content, contentType);
     }
 
 
     public ResearchObject getResearchObject() {
         return researchObject;
     }
 
 
     public URI getProxyUri() {
         return proxyUri;
     }
 
 
     public long getSize() {
         return size;
     }
 
 
     public void setSize(long size) {
         this.size = size;
     }
 
 
     /**
      * Resource size nicely formatted.
      * 
      * @return the size, nicely formatted (i.e. 23 MB)
      */
     public String getSizeFormatted() {
         if (getSize() >= 0) {
             return humanReadableByteCount(getSize());
         } else {
             return null;
         }
     }
 
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((created == null) ? 0 : created.hashCode());
         result = prime * result + ((creator == null) ? 0 : creator.hashCode());
         result = prime * result + ((proxyUri == null) ? 0 : proxyUri.hashCode());
         result = prime * result + ((uri == null) ? 0 : uri.hashCode());
         return result;
     }
 
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Resource other = (Resource) obj;
         if (uri == null) {
             if (other.uri != null) {
                 return false;
             }
         } else if (!uri.equals(other.uri)) {
             return false;
         }
         if (created == null) {
             if (other.created != null) {
                 return false;
             }
         } else if (!created.equals(other.created)) {
             return false;
         }
         if (creator == null) {
             if (other.creator != null) {
                 return false;
             }
         } else if (!creator.equals(other.creator)) {
             return false;
         }
         if (proxyUri == null) {
             if (other.proxyUri != null) {
                 return false;
             }
         } else if (!proxyUri.equals(other.proxyUri)) {
             return false;
         }
         return true;
     }
 
 
     /**
      * Adapted from http://stackoverflow.com/questions/3758606/how-to-convert-byte
      * -size-into-human-readable-format-in-java.
      * 
      * @param bytes
      *            size in bytes
      * @return nicely formatted size
      */
     private static String humanReadableByteCount(long bytes) {
         int unit = 1024;
         if (bytes < unit) {
             return bytes + " B";
         }
         int exp = (int) (Math.log(bytes) / Math.log(unit));
         return String.format("%.1f %cB", bytes / Math.pow(unit, exp), "KMGTPE".charAt(exp - 1));
     }
 
 
     @Override
     public Collection<Annotation> getAnnotations() {
         return researchObject.getAllAnnotations() != null ? researchObject.getAllAnnotations().get(uri) : null;
     }
 
 
     @Override
     public Annotation annotate(String bodyPath, InputStream body, String bodyContentType)
             throws ROSRSException, ROException {
         return researchObject.annotate(this, bodyPath, body, bodyContentType);
     }
 
 
     public boolean isInternal() {
         return uri.toString().startsWith(researchObject.getUri().toString());
     }
 
 
     @Override
     public List<AnnotationTriple> getPropertyValues(URI property, boolean merge) {
         List<AnnotationTriple> list = new ArrayList<>();
         for (Annotation annotation : getAnnotations()) {
             try {
                 List<String> literals = annotation.getPropertyValues(this, property);
                 if (!literals.isEmpty()) {
                     if (merge) {
                         list.add(new AnnotationTriple(annotation, this, property, StringUtils.join(literals, "; "),
                                 true));
                     } else {
                         for (String literal : literals) {
                             list.add(new AnnotationTriple(annotation, this, property, literal, false));
                         }
                     }
                 }
             } catch (ROSRSException e) {
                 LOG.error("Can't load annotation body", e);
             }
         }
         return list;
     }
 
 
     @Override
     public List<AnnotationTriple> getPropertyValues(Property property, boolean merge) {
         return getPropertyValues(URI.create(property.getURI()), merge);
     }
 
 
     @Override
     public AnnotationTriple createPropertyValue(URI property, String value)
             throws ROSRSException, ROException {
         Annotation annotation = this.annotate(null,
             Annotation.wrapAnnotationBody(Collections.singletonList(new Statement(this.getUri(), property, value))),
             RDFFormat.RDFXML.getDefaultMIMEType());
        this.researchObject.getAllAnnotations().put(uri, annotation);
         return new AnnotationTriple(annotation, this, property, value, false);
     }
 
 
     @Override
     public AnnotationTriple createPropertyValue(URI property, URI value)
             throws ROSRSException, ROException {
         return createPropertyValue(property, value.toString());
     }
 
 
     @Override
     public AnnotationTriple createPropertyValue(Property property, String value)
             throws ROSRSException, ROException {
         return createPropertyValue(URI.create(property.getURI()), value);
     }
 
 
     @Override
     public AnnotationTriple createPropertyValue(Property property, URI value)
             throws ROSRSException, ROException {
         return createPropertyValue(URI.create(property.getURI()), value.toString());
     }
 
 
     @Override
     public List<AnnotationTriple> getAnnotationTriples() {
         List<AnnotationTriple> list = new ArrayList<>();
         for (Annotation annotation : getAnnotations()) {
             try {
                 list.addAll(annotation.getPropertyValues(this));
             } catch (ROSRSException e) {
                 LOG.error("Can't load annotation body", e);
             }
         }
         Collections.sort(list, new AnnotationTripleByPredicateLocalNameComparator());
         return list;
     }
 
 
     @Override
     public boolean isLoaded() {
         return researchObject.isLoaded();
     }
 
 
     public String getPath() {
         return researchObject.getUri().relativize(uri).getPath();
     }
 
 
     /**
      * Get client response when doing a HEAD request.
      * 
      * @return a client response with headers
      * @throws ROSRSException
      *             when the response from the server is not 200 OK
      */
     public ClientResponse getHead()
             throws ROSRSException {
         return researchObject.getRosrs().getResourceHead(uri);
     }
 
 
     public boolean isNestedRO() {
         return nestedRO;
     }
 
 
     public void setNestedRO(boolean nestedRO) {
         this.nestedRO = nestedRO;
     }
 }
