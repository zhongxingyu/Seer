 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.CacheControl;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.ResourceInfo;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dlibra.service.AccessDeniedException;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.sun.jersey.api.ConflictException;
 
 /**
  * Research Object REST API resource.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("ROs/{ro_id}/")
 public class ResearchObjectResource {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(ResearchObjectResource.class);
 
     /** An application that displays the HTML version of a workflow. */
     private static final URI RO_HTML_PORTAL = URI.create("http://sandbox.wf4ever-project.org/portal/ro");
 
     /** HTTP request. */
     @Context
     HttpServletRequest request;
 
     /** URI info. */
     @Context
     UriInfo uriInfo;
 
 
     /**
      * Returns zip archive with contents of RO version.
      * 
      * @param researchObjectId
      *            RO identifier - defined by the user
      * @return 200 OK
      */
     @GET
     @Produces({ "application/zip", "multipart/related", "*/*" })
     public Response getZippedRO(@PathParam("ro_id") String researchObjectId) {
         return Response.seeOther(getZippedROURI(uriInfo.getBaseUriBuilder(), researchObjectId)).build();
     }
 
 
     /**
      * Redirect to the manifest.
      * 
      * @param researchObjectId
      *            RO id
      * @return 303 See Other
      */
     @GET
     @Produces({ "application/rdf+xml", "application/x-turtle", "text/turtle", "application/x-trig", "application/trix",
             "text/rdf+n3" })
     public Response getROMetadata(@PathParam("ro_id") String researchObjectId) {
         return Response.seeOther(getROMetadataURI(uriInfo.getBaseUriBuilder(), researchObjectId)).build();
     }
 
 
     /**
      * Redirect to the HTML page of the RO.
      * 
      * @param researchObjectId
      *            RO id
      * @return 303 See Other
      * @throws URISyntaxException
      *             could not construct a valid redirection URI
      */
     @GET
     @Produces({ MediaType.TEXT_HTML })
     public Response getROHtml(@PathParam("ro_id") String researchObjectId)
             throws URISyntaxException {
         URI uri = getROHtmlURI(uriInfo.getBaseUriBuilder(), researchObjectId);
         return Response.seeOther(uri).build();
     }
 
 
     /**
      * Delete a research object.
      * 
      * @param researchObjectId
      *            research object id
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws NotFoundException
      *             Research Object not found neither in dLibra nor in SMS
      */
     @DELETE
     public void deleteResearchObject(@PathParam("ro_id") String researchObjectId)
             throws DigitalLibraryException, NotFoundException {
         ResearchObject researchObject = ResearchObject.findByUri(uriInfo.getAbsolutePath());
        ROSRService.deleteResearchObject(researchObject);
     }
 
 
     /**
      * Add a resource with no specific MIME type.
      * 
      * @param researchObjectId
      *            RO id
      * @param content
      *            resource content
      * @return 201 Created with proxy URI
      * @throws BadRequestException
      *             annotation target is an incorrect URI
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @POST
     public Response addResource(@PathParam("ro_id") String researchObjectId, InputStream content)
             throws BadRequestException, AccessDeniedException, DigitalLibraryException, NotFoundException {
         ResearchObject researchObject = ResearchObject.findByUri(uriInfo.getAbsolutePath());
         URI resource;
         if (request.getHeader(Constants.SLUG_HEADER) != null) {
             resource = uriInfo.getAbsolutePathBuilder().path(request.getHeader(Constants.SLUG_HEADER)).build();
         } else {
             resource = uriInfo.getAbsolutePathBuilder().path(UUID.randomUUID().toString()).build();
         }
         if (ROSRService.SMS.get().isAggregatedResource(researchObject, resource)) {
             throw new ConflictException("This resource has already been aggregated. Use PUT to update it.");
         }
 
         List<URI> annotationTargets = new ArrayList<>();
         for (@SuppressWarnings("unchecked")
         Enumeration<String> en = request.getHeaders(Constants.LINK_HEADER); en.hasMoreElements();) {
             Matcher m = Constants.AO_LINK_HEADER_PATTERN.matcher(en.nextElement());
             if (m.matches()) {
                 try {
                     annotationTargets.add(new URI(m.group(1)));
                 } catch (URISyntaxException e) {
                     throw new BadRequestException("Annotation target " + m.group(1) + " is incorrect", e);
                 }
             }
         }
 
         if (!annotationTargets.isEmpty()) {
             ROSRService.aggregateInternalResource(researchObject, resource, content, request.getContentType(), null);
             ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, resource);
             return ROSRService.addAnnotation(researchObject, resource, annotationTargets);
         } else {
             ResponseBuilder builder = ROSRService.aggregateInternalResource(researchObject, resource, content,
                 request.getContentType(), null);
             if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
                 ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, resource);
             }
             ResourceInfo resInfo = ROSRService.getResourceInfo(researchObject, resource, null);
             if (resInfo != null) {
                 CacheControl cache = new CacheControl();
                 cache.setMustRevalidate(true);
                 builder = builder.cacheControl(cache).tag(resInfo.getChecksum())
                         .lastModified(resInfo.getLastModified().toDate());
             }
             return builder.build();
         }
     }
 
 
     /**
      * Create a new proxy.
      * 
      * @param researchObjectId
      *            RO id
      * @param content
      *            proxy description
      * @return 201 Created response pointing to the proxy
      * @throws BadRequestException
      *             wrong request body
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @POST
     @Consumes(Constants.PROXY_MIME_TYPE)
     public Response addProxy(@PathParam("ro_id") String researchObjectId, InputStream content)
             throws BadRequestException, AccessDeniedException, DigitalLibraryException, NotFoundException {
         ResearchObject researchObject = ResearchObject.findByUri(uriInfo.getAbsolutePath());
         URI proxyFor;
         if (request.getHeader(Constants.SLUG_HEADER) != null) {
             // internal resource
             proxyFor = uriInfo.getAbsolutePathBuilder().path(request.getHeader(Constants.SLUG_HEADER)).build();
         } else {
             // external resource
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
             model.read(content, researchObject.getUri().toString());
             ExtendedIterator<Individual> it = model.listIndividuals(Constants.ORE_PROXY_CLASS);
             if (it.hasNext()) {
                 NodeIterator it2 = it.next().listPropertyValues(Constants.ORE_PROXY_FOR_PROPERTY);
                 if (it2.hasNext()) {
                     RDFNode proxyForResource = it2.next();
                     if (proxyForResource.isURIResource()) {
                         try {
                             proxyFor = new URI(proxyForResource.asResource().getURI());
                         } catch (URISyntaxException e) {
                             throw new BadRequestException("Wrong target resource URI", e);
                         }
                     } else {
                         throw new BadRequestException("The target is not an URI resource.");
                     }
                 } else {
                     //The ore:Proxy does not have a ore:proxyFor property.
                     proxyFor = uriInfo.getAbsolutePathBuilder().path(UUID.randomUUID().toString()).build();
                 }
             } else {
                 throw new BadRequestException("The entity body does not define any ore:Proxy.");
             }
         }
         return ROSRService.aggregateExternalResource(researchObject, proxyFor).build();
     }
 
 
     /**
      * Add an annotation stub.
      * 
      * @param researchObjectId
      *            RO id
      * @param content
      *            annotation definition
      * @return 201 Created response pointing to the annotation stub
      * @throws BadRequestException
      *             wrong request body
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @POST
     @Consumes(Constants.ANNOTATION_MIME_TYPE)
     public Response addAnnotation(@PathParam("ro_id") String researchObjectId, InputStream content)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException, BadRequestException {
         ResearchObject researchObject = ResearchObject.findByUri(uriInfo.getAbsolutePath());
         URI body;
         List<URI> targets = new ArrayList<>();
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(content, researchObject.getUri().toString());
         ExtendedIterator<Individual> it = model.listIndividuals(Constants.RO_AGGREGATED_ANNOTATION_CLASS);
         if (it.hasNext()) {
             Individual aggregatedAnnotation = it.next();
             NodeIterator it2 = aggregatedAnnotation.listPropertyValues(Constants.AO_BODY_PROPERTY);
             if (it2.hasNext()) {
                 RDFNode bodyResource = it2.next();
                 if (bodyResource.isURIResource()) {
                     try {
                         body = new URI(bodyResource.asResource().getURI());
                     } catch (URISyntaxException e) {
                         throw new BadRequestException("Wrong body resource URI", e);
                     }
                 } else {
                     throw new BadRequestException("The body is not an URI resource.");
                 }
             } else {
                 throw new BadRequestException("The ro:AggregatedAnnotation does not have a ao:body property.");
             }
             it2 = aggregatedAnnotation.listPropertyValues(Constants.AO_ANNOTATES_RESOURCE_PROPERTY);
             while (it2.hasNext()) {
                 RDFNode targetResource = it2.next();
                 if (targetResource.isURIResource()) {
                     try {
                         targets.add(new URI(targetResource.asResource().getURI()));
                     } catch (URISyntaxException e) {
                         throw new BadRequestException("Wrong target resource URI", e);
                     }
                 } else {
                     throw new BadRequestException("The target is not an URI resource.");
                 }
             }
         } else {
             throw new BadRequestException("The entity body does not define any ro:AggregatedAnnotation.");
         }
         if (ROSRService.SMS.get().isAggregatedResource(researchObject, body)) {
             ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, body);
         }
 
         return ROSRService.addAnnotation(researchObject, body, targets);
     }
 
 
     /**
      * Create a URI pointing to the zipped RO.
      * 
      * @param baseUriBuilder
      *            base URI builder
      * @param researchObjectId
      *            RO id
      * @return the URI pointing to the zipped RO
      */
     private static URI getZippedROURI(UriBuilder baseUriBuilder, String researchObjectId) {
         return baseUriBuilder.path("zippedROs").path(researchObjectId).path("/").build();
     }
 
 
     /**
      * Create a URI pointing to the HTML page of the RO.
      * 
      * @param baseUriBuilder
      *            base URI builder
      * @param researchObjectId
      *            RO id
      * @return the URI pointing to the HTML page of the RO
      * @throws URISyntaxException
      *             could not construct a valid redirection URI
      */
     private static URI getROHtmlURI(UriBuilder baseUriBuilder, String researchObjectId)
             throws URISyntaxException {
         return new URI(RO_HTML_PORTAL.getScheme(), RO_HTML_PORTAL.getAuthority(), RO_HTML_PORTAL.getPath(), "ro="
                 + baseUriBuilder.path("ROs").path(researchObjectId).path("/").build().toString(), null);
     }
 
 
     /**
      * Create a URI pointing to the manifest.
      * 
      * @param baseUriBuilder
      *            base URI builder
      * @param researchObjectId
      *            RO id
      * @return the URI pointing to the manifest of the RO
      */
     private static URI getROMetadataURI(UriBuilder baseUriBuilder, String researchObjectId) {
         return baseUriBuilder.path("ROs").path(researchObjectId).path("/.ro/manifest.rdf").build();
     }
 
 
     /**
      * Add Link headers pointing to different RO formats.
      * 
      * @param responseBuilder
      *            response builder to which to add the links
      * @param linkUriInfo
      *            URI info
      * @param researchObjectId
      *            RO id
      * @return the original response builder with the Link headers
      */
     public static ResponseBuilder addLinkHeaders(ResponseBuilder responseBuilder, UriInfo linkUriInfo,
             String researchObjectId) {
         try {
             return responseBuilder
                     .header(
                         Constants.LINK_HEADER,
                         String.format(Constants.LINK_HEADER_TEMPLATE,
                             getROHtmlURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"))
                     .header(
                         Constants.LINK_HEADER,
                         String.format(Constants.LINK_HEADER_TEMPLATE,
                             getZippedROURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"))
                     .header(
                         Constants.LINK_HEADER,
                         String.format(Constants.LINK_HEADER_TEMPLATE,
                             getROMetadataURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"));
         } catch (URISyntaxException e) {
             LOGGER.error("Could not create RO Portal URI", e);
             return responseBuilder.header(
                 Constants.LINK_HEADER,
                 String.format(Constants.LINK_HEADER_TEMPLATE,
                     getZippedROURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark")).header(
                 Constants.LINK_HEADER,
                 String.format(Constants.LINK_HEADER_TEMPLATE,
                     getROMetadataURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"));
         }
     }
 }
