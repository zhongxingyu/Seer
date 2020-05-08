 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
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
 
 import org.apache.http.HttpStatus;
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.auth.RequestAttribute;
 import pl.psnc.dl.wf4ever.db.ResearchObjectId;
 import pl.psnc.dl.wf4ever.db.dao.ResearchObjectIdDAO;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.exceptions.IsDeletedException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.ORE.Proxy;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.model.RO.RoBundle;
 import pl.psnc.dl.wf4ever.util.HeaderUtils;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
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
 
     /** URI info. */
     @Context
     UriInfo uriInfo;
 
     /** Resource builder. */
     @RequestAttribute("Builder")
     private Builder builder;
 
 
     /**
      * Returns zip archive with contents of RO version.
      * 
      * @param researchObjectId
      *            RO identifier - defined by the user
      * @return 200 OK
      * @throws IsDeletedException
      *             when the research object existed but has been deleted
      */
     @GET
     @Produces({ "application/zip", "multipart/related", "*/*" })
     public Response getZippedRO(@PathParam("ro_id") String researchObjectId)
             throws IsDeletedException {
         ResearchObject researchObject = ResearchObject.get(builder, uriInfo.getAbsolutePath());
         if (researchObject == null) {
             ResearchObjectIdDAO dao = new ResearchObjectIdDAO();
             if (dao.findByPrimaryKey(uriInfo.getAbsolutePath()) != null) {
                 throw new IsDeletedException("This research object has been deleted");
             } else {
                 throw new NotFoundException("This research object does not exist");
             }
         }
         return Response.seeOther(getZippedROURI(uriInfo.getBaseUriBuilder(), researchObjectId)).build();
     }
 
 
     /**
      * Returns this RO as an RO bundle.
      * 
      * @param researchObjectId
      *            RO identifier - defined by the user
      * @return 200 OK
      * @throws IsDeletedException
      *             when the research object existed but has been deleted
      */
     @GET
     @Produces(RoBundle.MIME_TYPE)
     public Response getROBundle(@PathParam("ro_id") String researchObjectId)
             throws IsDeletedException {
         ResearchObject researchObject = ResearchObject.get(builder, uriInfo.getAbsolutePath());
         if (researchObject == null) {
             ResearchObjectIdDAO dao = new ResearchObjectIdDAO();
             if (dao.findByPrimaryKey(uriInfo.getAbsolutePath()) != null) {
                 throw new IsDeletedException("This research object has been deleted");
             } else {
                 throw new NotFoundException("This research object does not exist");
             }
         }
         URI bundleUri = researchObject.getBundleUri();
         if (bundleUri == null) {
             return Response.status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                     .entity("This Research Object is not available as an RO Bundle.").build();
         }
         return Response.ok(researchObject.getBundle(), RoBundle.MIME_TYPE).build();
     }
 
 
     /**
      * Redirect to the manifest.
      * 
      * @param researchObjectId
      *            RO id
      * @return 303 See Other
      * @throws IsDeletedException
      *             when the research object existed but has been deleted
      */
     @GET
     @Produces({ "application/rdf+xml", "application/x-turtle", "text/turtle", "application/x-trig", "application/trix",
             "text/rdf+n3" })
     public Response getROMetadata(@PathParam("ro_id") String researchObjectId)
             throws IsDeletedException {
         ResearchObject researchObject = ResearchObject.get(builder, uriInfo.getAbsolutePath());
         if (researchObject == null) {
             ResearchObjectIdDAO dao = new ResearchObjectIdDAO();
             if (dao.findByPrimaryKey(uriInfo.getAbsolutePath()) != null) {
                 throw new IsDeletedException("This research object has been deleted");
             } else {
                 throw new NotFoundException("This research object does not exist");
             }
         }
         return Response
                 .seeOther(getROMetadataURI(uriInfo.getBaseUriBuilder(), researchObjectId + "/"))
                 .header(
                     Constants.LINK_HEADER,
                     String.format(Constants.LINK_HEADER_TEMPLATE,
                         getROEvoLinkURI(uriInfo.getBaseUriBuilder(), researchObjectId), ORE.isDescribedBy)).build();
     }
 
 
     /**
      * Redirect to the HTML page of the RO.
      * 
      * @param researchObjectId
      *            RO id
      * @return 303 See Other
      * @throws URISyntaxException
      *             could not construct a valid redirection URI
      * @throws IsDeletedException
      *             when the research object existed but has been deleted
      */
     @GET
     @Produces({ MediaType.TEXT_HTML })
     public Response getROHtml(@PathParam("ro_id") String researchObjectId)
             throws URISyntaxException, IsDeletedException {
         ResearchObject researchObject = ResearchObject.get(builder, uriInfo.getAbsolutePath());
         if (researchObject == null) {
             ResearchObjectIdDAO dao = new ResearchObjectIdDAO();
             if (dao.findByPrimaryKey(uriInfo.getAbsolutePath()) != null) {
                 throw new IsDeletedException("This research object has been deleted");
             } else {
                 throw new NotFoundException("This research object does not exist");
             }
         }
         URI uri = getROHtmlURI(uriInfo.getBaseUriBuilder(), researchObjectId);
         return Response.seeOther(uri).build();
     }
 
 
     /**
      * Delete a research object.
      * 
      * @param researchObjectId
      *            research object id
      * @param purge
      *            force delete of the ID
      * @throws DigitalLibraryException
      *             could not connect to the DL
      */
     @DELETE
     public void deleteResearchObject(@PathParam("ro_id") String researchObjectId,
             @DefaultValue("false") @HeaderParam("Purge") boolean purge)
             throws DigitalLibraryException {
         URI uri = uriInfo.getAbsolutePath();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject != null) {
             researchObject.delete();
         }
         if (!purge) {
             if (researchObject == null) {
                 throw new NotFoundException("Research Object not found");
             }
         } else {
             ResearchObjectIdDAO dao = new ResearchObjectIdDAO();
             ResearchObjectId id = dao.findByPrimaryKey(uri);
             if (id == null) {
                 throw new NotFoundException("Research Object ID not found");
             }
             dao.delete(id);
         }
     }
 
 
     /**
      * Add a resource with no specific MIME type.
      * 
      * @param researchObjectId
      *            RO id
      * @param path
      *            resource path
      * @param accept
      *            Accept header
      * @param links
      *            Link headers
      * @param contentType
      *            content type header
      * @param content
      *            resource content
      * @return 201 Created with proxy URI
      * @throws BadRequestException
      *             annotation target is an incorrect URI
      */
     @POST
     public Response addResource(@PathParam("ro_id") String researchObjectId, @HeaderParam("Slug") String path,
             @HeaderParam("Accept") String accept, @HeaderParam("Link") Set<String> links,
             @HeaderParam("Content-Type") String contentType, InputStream content)
             throws BadRequestException {
         URI uri = uriInfo.getAbsolutePath();
         RDFFormat responseSyntax = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         if (path == null) {
             path = UUID.randomUUID().toString();
         }
         URI resourceUri = uriInfo.getAbsolutePathBuilder().path(path).build();
         if (researchObject.getAggregatedResources().containsKey(resourceUri)) {
             throw new ConflictException("This resource has already been aggregated. Use PUT to update it.");
         }
         if (researchObject.isUriUsed(resourceUri)) {
             throw new ConflictException("This URI is already used.");
         }
         Collection<URI> annotated = HeaderUtils.getLinkHeaders(links).get(AO.annotatesResource.getURI());
         Set<Thing> annotationTargets = new HashSet<>();
         for (URI targetUri : annotated) {
             Thing target = Annotation.validateTarget(researchObject, targetUri);
             annotationTargets.add(target);
         }
         if (!annotationTargets.isEmpty()) {
             pl.psnc.dl.wf4ever.model.RO.Resource roResource = researchObject.aggregate(path, content, contentType);
             Annotation annotation = researchObject.annotate(resourceUri, annotationTargets);
             String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotation.getBody().getUri()
                     .toString(), AO.body);
             InputStream annotationDesc = researchObject.getManifest().getGraphAsInputStream(responseSyntax,
                 roResource.getProxy(), roResource, annotation);
             ResponseBuilder response = Response.created(annotation.getUri()).entity(annotationDesc)
                     .type(responseSyntax.getDefaultMIMEType()).header(Constants.LINK_HEADER, annotationBodyHeader);
             for (Thing target : annotation.getAnnotated()) {
                 String targetHeader = String.format(Constants.LINK_HEADER_TEMPLATE, target.toString(),
                     AO.annotatesResource);
                 response = response.header(Constants.LINK_HEADER, targetHeader);
             }
             return response.build();
         } else {
             pl.psnc.dl.wf4ever.model.RO.Resource resource = researchObject.aggregate(path, content, contentType);
             String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, resource.getUri().toString(),
                 ORE.proxyFor.getURI());
             InputStream proxyAndResourceDesc = researchObject.getManifest().getGraphAsInputStream(responseSyntax,
                 resource.getProxy(), resource);
             ResponseBuilder rb = Response.created(resource.getProxy().getUri()).entity(proxyAndResourceDesc)
                     .type(responseSyntax.getDefaultMIMEType()).header(Constants.LINK_HEADER, proxyForHeader);
             if (resource.getStats() != null) {
                 CacheControl cache = new CacheControl();
                 cache.setMustRevalidate(true);
                 rb = rb.cacheControl(cache).tag(resource.getStats().getChecksum())
                         .lastModified(resource.getStats().getLastModified().toDate());
             }
             //researchObject.updateIndexAttributes();
             return rb.build();
         }
     }
 
 
     /**
      * Create a new proxy.
      * 
      * @param researchObjectId
      *            RO id
      * @param slug
      *            Slug header
      * @param accept
      *            Accept header
      * @param content
      *            proxy description
      * @return 201 Created response pointing to the proxy
      * @throws BadRequestException
      *             wrong request body
      */
     @POST
     @Consumes(Constants.PROXY_MIME_TYPE)
     public Response addProxy(@PathParam("ro_id") String researchObjectId, @HeaderParam("Slug") String slug,
             @HeaderParam("Accept") String accept, InputStream content)
             throws BadRequestException {
         URI uri = uriInfo.getAbsolutePath();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         URI proxyFor;
         if (slug != null) {
             // internal resource
             proxyFor = uriInfo.getAbsolutePathBuilder().path(slug).build();
         } else {
             // external resource
             proxyFor = Proxy.assemble(researchObject, content);
             if (proxyFor == null) {
                 //The ore:Proxy does not have a ore:proxyFor property.
                 proxyFor = UriBuilder.fromUri(researchObject.getUri()).path(UUID.randomUUID().toString()).build();
             }
         }
         pl.psnc.dl.wf4ever.model.RO.Resource resource = researchObject.aggregate(proxyFor);
         RDFFormat syntax = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
         String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, proxyFor.toString(),
             ORE.proxyFor.getURI());
         InputStream proxyDesc = researchObject.getManifest().getGraphAsInputStream(syntax, resource.getProxy());
         return Response.created(resource.getProxy().getUri()).entity(proxyDesc).type(syntax.getDefaultMIMEType())
                 .header(Constants.LINK_HEADER, proxyForHeader).build();
     }
 
 
     /**
      * Add an annotation stub.
      * 
      * @param researchObjectId
      *            RO id
      * @param accept
      *            Accept header
      * @param content
      *            annotation definition
      * @return 201 Created response pointing to the annotation stub
      * @throws BadRequestException
      *             wrong request body
      */
     @POST
     @Consumes(Constants.ANNOTATION_MIME_TYPE)
     public Response addAnnotation(@PathParam("ro_id") String researchObjectId, @HeaderParam("Accept") String accept,
             InputStream content)
             throws BadRequestException {
         URI uri = uriInfo.getAbsolutePath();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         Annotation annotation = researchObject.annotate(content);
         String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotation.getBody().getUri()
                 .toString(), AO.body);
         RDFFormat syntax = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
         InputStream annotationDesc = researchObject.getManifest().getGraphAsInputStream(syntax, annotation);
         ResponseBuilder response = Response.created(annotation.getUri()).entity(annotationDesc)
                 .type(syntax.getDefaultMIMEType()).header(Constants.LINK_HEADER, annotationBodyHeader);
         for (Thing target : annotation.getAnnotated()) {
             String targetHeader = String
                     .format(Constants.LINK_HEADER_TEMPLATE, target.toString(), AO.annotatesResource);
             response = response.header(Constants.LINK_HEADER, targetHeader);
         }
         return response.build();
     }
 
 
     /**
      * Add an ro:Folder.
      * 
      * @param researchObjectId
      *            RO id
      * @param path
      *            folder path
      * @param accept
      *            Accept header
      * @param content
      *            folder definition
      * @return 201 Created response pointing to the folder
      * @throws BadRequestException
      *             wrong request body
      */
     @POST
     @Consumes(Constants.FOLDER_MIME_TYPE)
     public Response addFolder(@PathParam("ro_id") String researchObjectId, @HeaderParam("Accept") String accept,
             @HeaderParam("Slug") String path, InputStream content)
             throws BadRequestException {
         URI uri = uriInfo.getAbsolutePath();
         ResearchObject researchObject = ResearchObject.get(builder, uri);
         if (researchObject == null) {
             throw new NotFoundException("Research Object not found");
         }
         if (path == null) {
             path = UUID.randomUUID().toString();
         }
         URI folderUri = uriInfo.getAbsolutePathBuilder().path(path).build();
         Folder folder = researchObject.aggregateFolder(folderUri, content);
 
         RDFFormat syntax = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
         Model folderDesc = ModelFactory.createDefaultModel();
         folderDesc.read(folder.getResourceMap().getGraphAsInputStream(syntax), null);
         folderDesc.read(researchObject.getManifest().getGraphAsInputStream(syntax, folder, folder.getProxy()), null);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         folderDesc.write(out);
 
         ResponseBuilder rb = Response.created(folder.getProxy().getUri()).type(Constants.FOLDER_MIME_TYPE);
         rb = rb.header(Constants.LINK_HEADER,
             String.format(Constants.LINK_HEADER_TEMPLATE, folder.getUri().toString(), ORE.proxyFor.getURI()));
         rb = rb.header(Constants.LINK_HEADER, String.format(Constants.LINK_HEADER_TEMPLATE, folder.getResourceMap()
                 .getUri().toString().toString(), ORE.isDescribedBy.getURI()));
         rb = rb.entity(new ByteArrayInputStream(out.toByteArray())).type(syntax.getDefaultMIMEType());
         return rb.build();
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
      * Create a URI pointing to the roevo information.
      * 
      * @param baseUriBuilder
      *            base URI builder
      * @param researchObjectId
      *            RO id
      * @return the URI pointing to the manifest of the RO
      */
     private static URI getROEvoLinkURI(UriBuilder baseUriBuilder, String researchObjectId) {
         String roUri = baseUriBuilder.clone().path("/ROs/").path(researchObjectId + "/").build().toString();
         return baseUriBuilder.path("/evo/info").queryParam("ro", roUri).build();
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
