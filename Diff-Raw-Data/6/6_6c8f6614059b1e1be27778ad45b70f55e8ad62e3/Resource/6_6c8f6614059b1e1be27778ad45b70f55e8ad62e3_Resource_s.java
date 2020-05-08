 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.CacheControl;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.EntityTag;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.http.HttpStatus;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.auth.ForbiddenException;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
 import pl.psnc.dlibra.service.AccessDeniedException;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 
 /**
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("ROs/{ro_id}/{filePath: .+}")
 public class Resource {
 
     @Context
     private HttpServletRequest servletRequest;
 
     @Context
     private UriInfo uriInfo;
 
 
     /**
      * 
      * @param researchObjectId
      * @param filePath
      * @param original
      * @param entity
      * @return
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @PUT
     public Response putResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original, String entity)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         URI resource = uriInfo.getAbsolutePath();
 
         if (ROSRService.SMS.get().isProxy(researchObject, resource)) {
             return Response.status(Status.TEMPORARY_REDIRECT)
                     .location(ROSRService.SMS.get().getProxyFor(researchObject, resource)).build();
         }
         if (ROSRService.SMS.get().isAggregatedResource(researchObject, resource)) {
             if (ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
                 return Response
                         .status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                         .entity(
                             "This resource is an annotation, only \"application/vnd.wf4ever.annotation\" media type is accepted")
                         .build();
             }
             if (original != null) {
                 resource = resource.resolve(original);
             }
             if (researchObject.resolve(Constants.MANIFEST_PATH).equals(resource)) {
                 throw new ForbiddenException("Can't update the manifest");
             }
             ResponseBuilder builder = ROSRService.updateInternalResource(researchObject, resource, entity,
                 servletRequest.getContentType());
             ResourceInfo resInfo = ROSRService.getResourceInfo(researchObject, resource, original);
             if (resInfo != null) {
                 CacheControl cache = new CacheControl();
                 cache.setMustRevalidate(true);
                 builder = builder.cacheControl(cache).tag(resInfo.getChecksum())
                         .lastModified(resInfo.getLastModified().toDate());
             }
             return builder.build();
         } else {
             throw new ForbiddenException(
                     "You cannot use PUT to create new resources unless they have been referenced in a proxy or an annotation. Use POST instead.");
         }
     }
 
 
     /**
      * Make a PUT to update an annotation.
      * 
      * @param researchObjectId
      *            research object ID
      * @param filePath
      *            the file path
      * @param original
      *            original resource in case of a format-specific URI
      * @param content
      *            RDF/XML representation of an annotation
      * @return 200 OK
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      * @throws BadRequestException
      *             the RDF/XML content is incorrect
      */
     @PUT
     @Consumes(Constants.ANNOTATION_MIME_TYPE)
     public Response updateAnnotation(@PathParam("ro_id") String researchObjectId,
             @PathParam("filePath") String filePath, @QueryParam("original") String original, InputStream content)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException, BadRequestException {
         URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         URI resource = uriInfo.getAbsolutePath();
         URI body;
         List<URI> targets = new ArrayList<>();
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(content, researchObject.toString());
         ExtendedIterator<Individual> it = model.listIndividuals(Constants.RO_AGGREGATED_ANNOTATION_CLASS);
         if (it.hasNext()) {
            NodeIterator it2 = it.next().listPropertyValues(Constants.AO_BODY_PROPERTY);
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
            it2 = it.next().listPropertyValues(Constants.AO_ANNOTATES_RESOURCE_PROPERTY);
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
 
         if (!ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
             throw new ForbiddenException("You cannot create a new annotation using PUT, use POST instead.");
         }
         URI oldAnnotationBody = ROSRService.getAnnotationBody(researchObject, resource, null);
         if (oldAnnotationBody == null || !oldAnnotationBody.equals(body)) {
             ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, oldAnnotationBody);
             if (ROSRService.SMS.get().isAggregatedResource(researchObject, body)) {
                 ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, body);
             }
         }
         return ROSRService.updateAnnotation(researchObject, resource, body, targets);
     }
 
 
     /**
      * 
      * @param researchObjectId
      * @param filePath
      * @param original
      * @return
      * @throws NotFoundException
      * @throws DigitalLibraryException
      * @throws AccessDeniedException
      */
     @GET
     public Response getResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original, @Context Request request)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         URI resource = uriInfo.getAbsolutePath();
 
         if (ROSRService.SMS.get().isProxy(researchObject, resource)) {
             return Response.status(Status.SEE_OTHER)
                     .location(ROSRService.SMS.get().getProxyFor(researchObject, resource)).build();
         }
         if (ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
             return Response
                     .status(Status.SEE_OTHER)
                     .location(
                         ROSRService.getAnnotationBody(researchObject, resource,
                             servletRequest.getHeader(Constants.ACCEPT_HEADER))).build();
         }
 
         ResourceInfo resInfo = ROSRService.getResourceInfo(researchObject, resource, original);
         ResponseBuilder rb = request.evaluatePreconditions(resInfo.getLastModified().toDate(),
             new EntityTag(resInfo.getChecksum()));
         if (rb != null) {
             return rb.build();
         }
 
         ResponseBuilder builder = ROSRService.getInternalResource(researchObject, resource,
             servletRequest.getHeader("Accept"), original);
 
         if (resInfo != null) {
             CacheControl cache = new CacheControl();
             cache.setMustRevalidate(true);
             builder = builder.cacheControl(cache).tag(resInfo.getChecksum())
                     .lastModified(resInfo.getLastModified().toDate());
         }
         return builder.build();
     }
 
 
     /**
      * 
      * @param researchObjectId
      * @param filePath
      * @param original
      * @return
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     @DELETE
     public Response deleteResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
             @QueryParam("original") String original)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
         URI resource = uriInfo.getAbsolutePath();
 
         if (ROSRService.SMS.get().isProxy(researchObject, resource)) {
             URI proxyFor = ROSRService.SMS.get().getProxyFor(researchObject, resource);
             if (ROSRService.isInternalResource(researchObject, proxyFor)) {
                 return Response.status(Status.TEMPORARY_REDIRECT).location(proxyFor).build();
             } else {
                 return ROSRService.deaggregateExternalResource(researchObject, resource);
             }
         }
         if (ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
             URI annotationBody = ROSRService.getAnnotationBody(researchObject, resource, null);
             ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, annotationBody);
             return ROSRService.deleteAnnotation(researchObject, resource);
         }
         if (original != null) {
             resource = resource.resolve(original);
         }
         if (researchObject.resolve(Constants.MANIFEST_PATH).equals(resource)) {
             throw new ForbiddenException("Can't delete the manifest");
         }
         return ROSRService.deaggregateInternalResource(researchObject, resource);
     }
 }
