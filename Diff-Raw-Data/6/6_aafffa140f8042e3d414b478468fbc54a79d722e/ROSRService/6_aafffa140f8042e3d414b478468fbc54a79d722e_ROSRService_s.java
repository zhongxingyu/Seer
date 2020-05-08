 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.dlibra.ConflictException;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
 import pl.psnc.dl.wf4ever.evo.EvoType;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 import pl.psnc.dlibra.service.AccessDeniedException;
 
 import com.google.common.collect.Multimap;
 import com.sun.jersey.core.header.ContentDisposition;
 
 public final class ROSRService {
 
     private final static Logger LOG = Logger.getLogger(ROSRService.class);
 
     public static final ThreadLocal<DigitalLibrary> DL = new ThreadLocal<>();
 
     public static final ThreadLocal<SemanticMetadataService> SMS = new ThreadLocal<>();
 
 
     /**
      * Create a new research object.
      * 
      * @param researchObjectURI
      *            the research object URI
      * @return research object URI
      * @throws ConflictException
      *             the research object id is already used
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      */
     public static URI createResearchObject(URI researchObjectURI)
             throws ConflictException, DigitalLibraryException, NotFoundException {
         return createResearchObject(researchObjectURI, null, null);
     }
 
 
     /**
      * Create a new research object of a specific ROEVO type.
      * 
      * @param researchObjectURI
      *            the research object URI
      * @param type
      *            ROEVO type, may be null
      * @param sourceRO
      *            live RO for snapshots and archives, any URI for live ROs, may be null if type is null
      * @return research object URI
      * @throws ConflictException
      *             the research object id is already used
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      */
     public static URI createResearchObject(URI researchObjectURI, EvoType type, URI sourceRO)
             throws ConflictException, DigitalLibraryException, NotFoundException {
         String researchObjectId = getResearchObjectId(researchObjectURI);
         InputStream manifest;
         try {
             if (type == null) {
                 ROSRService.SMS.get().createResearchObject(researchObjectURI);
             } else {
                 switch (type) {
                     default:
                     case LIVE:
                         ROSRService.SMS.get().createLiveResearchObject(researchObjectURI, sourceRO);
                         break;
                     case SNAPSHOT:
                         ROSRService.SMS.get().createSnapshotResearchObject(researchObjectURI, sourceRO);
                         break;
                     case ARCHIVED:
                         ROSRService.SMS.get().createArchivedResearchObject(researchObjectURI, sourceRO);
                         break;
                 }
             }
             manifest = ROSRService.SMS.get().getManifest(researchObjectURI.resolve(Constants.MANIFEST_PATH),
                 RDFFormat.RDFXML);
         } catch (IllegalArgumentException e) {
             // RO already existed in sms, maybe created by someone else
             throw new ConflictException("The RO with identifier " + researchObjectId + " already exists");
         }
 
         try {
             ROSRService.DL.get().createWorkspace(Constants.workspaceId);
         } catch (ConflictException e) {
             // nothing
         }
         try {
             ROSRService.DL.get().createResearchObject(Constants.workspaceId, researchObjectId);
         } catch (ConflictException e) {
             // nothing
         }
         ROSRService.DL.get().createVersion(Constants.workspaceId, researchObjectId, Constants.versionId, manifest,
             Constants.MANIFEST_PATH, RDFFormat.RDFXML.getDefaultMIMEType());
         ROSRService.DL.get().publishVersion(Constants.workspaceId, researchObjectId, Constants.versionId);
         return researchObjectURI;
     }
 
 
     /**
      * Delete a research object
      * 
      * @param researchObject
      *            research object URI
      * @throws DigitalLibraryException
      *             could not connect to the DL
      */
     public static void deleteResearchObject(URI researchObject)
             throws DigitalLibraryException {
         String researchObjectId = getResearchObjectId(researchObject);
         try {
             ROSRService.DL.get().deleteVersion(Constants.workspaceId, researchObjectId, Constants.versionId);
             if (ROSRService.DL.get().getVersionIds(Constants.workspaceId, researchObjectId).isEmpty()) {
                 ROSRService.DL.get().deleteResearchObject(Constants.workspaceId, researchObjectId);
                 if (ROSRService.DL.get().getResearchObjectIds(Constants.workspaceId).isEmpty()) {
                     ROSRService.DL.get().deleteWorkspace(Constants.workspaceId);
                 }
             }
         } catch (NotFoundException e) {
             LOG.warn("URI not found in dLibra: " + researchObject);
         } finally {
             try {
                 ROSRService.SMS.get().removeResearchObject(researchObject);
             } catch (IllegalArgumentException e) {
                 LOG.warn("URI not found in SMS: " + researchObject);
             }
         }
     }
 
 
     /**
      * Aggregate a new internal resource in the research object, upload the resource and add a proxy.
      * 
      * @param researchObject
      *            the research object
      * @param resource
      *            resource URI, must be internal
      * @param entity
      *            request entity
      * @param contentType
      *            resource content type
      * @param original
      *            original resource in case of using a format specific URI
      * @return 201 Created with proxy URI
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder aggregateInternalResource(URI researchObject, URI resource, InputStream entity,
             String contentType, String original)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         if (original != null) {
             resource = resource.resolve(original);
         }
         String researchObjectId = getResearchObjectId(researchObject);
         String filePath = researchObject.relativize(resource).getPath();
         ResourceInfo resourceInfo = ROSRService.DL.get().createOrUpdateFile(Constants.workspaceId, researchObjectId,
             Constants.versionId, filePath, entity, contentType != null ? contentType : "text/plain");
         ROSRService.SMS.get().addResource(researchObject, resource, resourceInfo);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(Constants.MANIFEST_PATH, researchObject,
             researchObject.resolve(Constants.MANIFEST_PATH));
         return addProxy(researchObject, resource);
     }
 
 
     /**
      * Aggregate a resource in the research object without uploading its content and add a proxy.
      * 
      * @param researchObject
      *            research object
      * @param resource
      *            resource that will be aggregated
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder aggregateExternalResource(URI researchObject, URI resource)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         ROSRService.SMS.get().addResource(researchObject, resource, null);
         ResponseBuilder builder = addProxy(researchObject, resource);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(Constants.MANIFEST_PATH, researchObject,
             researchObject.resolve(Constants.MANIFEST_PATH));
         return builder;
     }
 
 
     /**
      * De-aggregate an internal resource, delete its content and proxy.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            resource to delete
      * @param original
      *            original resource in case of using a format specific URI
      * @return 204 No Content response
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response deaggregateInternalResource(URI researchObject, URI resource)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String researchObjectId = getResearchObjectId(researchObject);
         String filePath = researchObject.relativize(resource).getPath();
         ROSRService.DL.get().deleteFile(Constants.workspaceId, researchObjectId, Constants.versionId, filePath);
         if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
             ROSRService.SMS.get().removeNamedGraph(researchObject, resource);
         } else {
             ROSRService.SMS.get().removeResource(researchObject, resource);
         }
         URI proxy = ROSRService.SMS.get().getProxyForResource(researchObject, resource);
         Response response = deleteProxy(researchObject, proxy);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(Constants.MANIFEST_PATH, researchObject,
             researchObject.resolve(Constants.MANIFEST_PATH));
         return response;
     }
 
 
     /**
      * Remove the aggregation of a resource in a research object and its proxy.
      * 
      * @param researchObject
      *            research object that aggregates the resource
      * @param proxy
      *            external resource proxy
      * @return 204 No Content
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static Response deaggregateExternalResource(URI researchObject, URI proxy)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         URI resource = ROSRService.SMS.get().getProxyFor(researchObject, proxy);
         ROSRService.SMS.get().removeResource(researchObject, resource);
         // update the manifest that describes the resource in dLibra
         updateNamedGraphInDlibra(Constants.MANIFEST_PATH, researchObject,
             researchObject.resolve(Constants.MANIFEST_PATH));
         return deleteProxy(researchObject, proxy);
     }
 
 
     /**
      * Check if the resource is internal. Resource is internal only if its content has been deployed under the control
      * of the service. A resource that has "internal" URI but the content has not been uploaded is considered external.
      * 
      * @param researchObject
      *            research object that aggregates the resource
      * @param resource
      *            the resource that may be internal
      * @return true if the resource content is deployed under the control of the service, false otherwise
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      */
     public static boolean isInternalResource(URI researchObject, URI resource)
             throws NotFoundException, DigitalLibraryException {
         String researchObjectId = getResearchObjectId(researchObject);
         String filePath = researchObject.relativize(resource).getPath();
         return ROSRService.DL.get().fileExists(Constants.workspaceId, researchObjectId, Constants.versionId, filePath);
     }
 
 
     /**
      * Add a proxy to the research object.
      * 
      * @param researchObject
      *            the research object
      * @param proxyFor
      *            resource for which the proxy is
      * @return 201 Created response pointing to the proxy
      */
     private static ResponseBuilder addProxy(URI researchObject, URI proxyFor) {
         URI proxy = ROSRService.SMS.get().addProxy(researchObject, proxyFor);
 
         String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, proxyFor.toString(),
             Constants.ORE_PROXY_FOR_HEADER);
         return Response.created(proxy).header(Constants.LINK_HEADER, proxyForHeader);
     }
 
 
     /**
      * Delete the proxy. Does not delete the resource that it points to.
      * 
      * @param researchObject
      *            research object that aggregates the proxy
      * @param proxy
      *            proxy to delete
      * @return 204 No Content
      */
     private static Response deleteProxy(URI researchObject, URI proxy) {
         ROSRService.SMS.get().deleteProxy(researchObject, proxy);
         return Response.noContent().build();
     }
 
 
     /**
      * Update an aggregated resource, which may be a regular file or RO metadata. The resource must be aggregated by the
      * research object but may have no content.
      * 
      * @param researchObject
      *            research object which aggregates the resource
      * @param resource
      *            resource URI
      * @param entity
      *            resource content
      * @param contentType
      *            resource content type
      * @return 200 OK response or 201 Created if there had been no content
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static ResponseBuilder updateInternalResource(URI researchObject, URI resource, String entity,
             String contentType)
             throws AccessDeniedException, DigitalLibraryException, NotFoundException {
         String researchObjectId = getResearchObjectId(researchObject);
         String filePath = researchObject.relativize(resource).getPath();
         boolean contentExisted = ROSRService.DL.get().fileExists(Constants.workspaceId, researchObjectId,
             Constants.versionId, filePath);
         if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
             RDFFormat format = RDFFormat.forMIMEType(contentType);
             if (format == null) {
                 format = RDFFormat.forFileName(resource.getPath(), RDFFormat.RDFXML);
             }
             ROSRService.SMS.get().addNamedGraph(resource, new ByteArrayInputStream(entity.getBytes()), format);
             // update the named graph copy in dLibra, the manifest is not changed
             updateNamedGraphInDlibra(filePath, researchObject, resource);
             updateROAttributesInDlibra(researchObject);
         } else {
             ResourceInfo resourceInfo = ROSRService.DL.get().createOrUpdateFile(Constants.workspaceId,
                 researchObjectId, Constants.versionId, filePath, new ByteArrayInputStream(entity.getBytes()),
                 contentType != null ? contentType : "text/plain");
             ROSRService.SMS.get().addResource(researchObject, resource, resourceInfo);
             // update the manifest that describes the resource in dLibra
             updateNamedGraphInDlibra(Constants.MANIFEST_PATH, researchObject,
                 researchObject.resolve(Constants.MANIFEST_PATH));
         }
         if (contentExisted) {
             return Response.ok();
         } else {
             return Response.created(resource);
         }
     }
 
 
     public static ResourceInfo getResourceInfo(URI researchObject, URI resource, String original)
             throws AccessDeniedException, NotFoundException, DigitalLibraryException {
         String researchObjectId = getResearchObjectId(researchObject);
         String filePath = researchObject.relativize(original != null ? resource.resolve(original) : resource).getPath();
         return ROSRService.DL.get().getFileInfo(Constants.workspaceId, researchObjectId, Constants.versionId, filePath);
     }
 
 
     /**
      * Return the content of an internal resource.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            resource URI
      * @param accept
      *            requested MIME type
      * @return 200 OK with resource content
      * @throws NotFoundException
      * @throws AccessDeniedException
      * @throws DigitalLibraryException
      */
     public static ResponseBuilder getInternalResource(URI researchObject, URI resource, String accept, String original)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String researchObjectId = getResearchObjectId(researchObject);
         String filePath = researchObject.relativize(resource).getPath();
 
         // check if request is for a specific format
         if (original != null) {
             URI originalResource = resource.resolve(original);
             filePath = researchObject.relativize(originalResource).getPath();
             if (ROSRService.SMS.get().containsNamedGraph(originalResource)
                     && ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, originalResource)) {
                 RDFFormat format = RDFFormat.forMIMEType(accept);
                 if (format == null) {
                     format = RDFFormat.forFileName(resource.getPath(), RDFFormat.RDFXML);
                 }
                 InputStream graph = ROSRService.SMS.get().getNamedGraph(originalResource, format);
                 ContentDisposition cd = ContentDisposition.type(format.getDefaultMIMEType())
                         .fileName(getFilename(resource)).build();
                 return Response.ok(graph).header("Content-disposition", cd);
             } else {
                 return Response.status(Status.NOT_FOUND).type("text/plain").entity("Original resource not found");
             }
         }
 
         if (ROSRService.SMS.get().containsNamedGraph(resource)
                 && ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
             RDFFormat acceptFormat = RDFFormat.forMIMEType(accept);
             RDFFormat extensionFormat = RDFFormat.forFileName(resource.getPath());
             if (extensionFormat != null && (acceptFormat == null || extensionFormat == acceptFormat)) {
                 // 1. GET manifest.rdf Accept: application/rdf+xml
                 // 2. GET manifest.rdf
                 InputStream graph = ROSRService.SMS.get().getNamedGraph(resource, extensionFormat);
                 ContentDisposition cd = ContentDisposition.type(extensionFormat.getDefaultMIMEType())
                         .fileName(getFilename(resource)).build();
                 return Response.ok(graph).header("Content-disposition", cd);
             }
             // 3. GET manifest.rdf Accept: text/turtle
             // 4. GET manifest Accept: application/rdf+xml
             // 5. GET manifest
             URI formatSpecificURI = createFormatSpecificURI(resource, extensionFormat,
                 (acceptFormat != null ? acceptFormat : RDFFormat.RDFXML));
             return Response.temporaryRedirect(formatSpecificURI);
         }
 
         String mimeType = ROSRService.DL.get().getFileMimeType(Constants.workspaceId, researchObjectId,
             Constants.versionId, filePath);
         ContentDisposition cd = ContentDisposition.type(mimeType).fileName(getFilename(resource)).build();
         InputStream body = ROSRService.DL.get().getFileContents(Constants.workspaceId, researchObjectId,
             Constants.versionId, filePath);
         return Response.ok(body).header("Content-disposition", cd).header("Content-type", mimeType);
     }
 
 
     private static String getFilename(URI uri) {
         return uri.resolve(".").relativize(uri).toString();
     }
 
 
     private static URI createFormatSpecificURI(URI absolutePath, RDFFormat extensionFormat, RDFFormat newFormat) {
         String path = absolutePath.getPath().toString();
         if (extensionFormat != null) {
             for (String extension : extensionFormat.getFileExtensions()) {
                 if (path.endsWith(extension)) {
                     path = path.substring(0, path.length() - extension.length() - 1);
                     break;
                 }
             }
         }
         path = path.concat(".").concat(newFormat.getDefaultFileExtension());
         try {
             return new URI(absolutePath.getScheme(), absolutePath.getAuthority(), path,
                     "original=".concat(getFilename(absolutePath)), null);
         } catch (URISyntaxException e) {
             LOG.error("Can't create a format-specific URI", e);
             return null;
         }
     }
 
 
     /**
      * An existing aggregated resource is being used as an annotation body now. Add it to the triplestore. If the
      * aggregated resource is external, do nothing.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            URI of the resource that is converted
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public static void convertAggregatedResourceToAnnotationBody(URI researchObject, URI resource)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String researchObjectId = getResearchObjectId(researchObject);
         String filePath = researchObject.relativize(resource).getPath();
         InputStream data = ROSRService.DL.get().getFileContents(Constants.workspaceId, researchObjectId,
             Constants.versionId, filePath);
         RDFFormat format = RDFFormat.forMIMEType(ROSRService.DL.get().getFileMimeType(Constants.workspaceId,
             researchObjectId, Constants.versionId, filePath));
         ROSRService.SMS.get().addNamedGraph(resource, data, format);
         // update the named graph copy in dLibra, the manifest is not changed
         updateNamedGraphInDlibra(filePath, researchObject, resource);
         updateROAttributesInDlibra(researchObject);
     }
 
 
     /**
      * An aggregated resource is no longer an annotation body. Remove it from the triplestore. If the annotation body is
      * not inside the triplestore (i.e. it's an external resource), do nothing.
      * 
      * @param researchObject
      *            research object aggregating the resource
      * @param resource
      *            URI of the resource that is converted
      */
     public static void convertAnnotationBodyToAggregatedResource(URI researchObject, URI resource) {
        ROSRService.SMS.get().removeNamedGraph(researchObject, resource);
        updateROAttributesInDlibra(researchObject);
     }
 
 
     /**
      * Add and aggregate a new annotation to the research object.
      * 
      * @param researchObject
      *            the research object
      * @param annotationBody
      *            annotation body URI
      * @param annotationTargets
      *            list of annotated resources URIs
      * @return 201 Created response
      */
     public static Response addAnnotation(URI researchObject, URI annotationBody, List<URI> annotationTargets) {
         URI annotation = ROSRService.SMS.get().addAnnotation(researchObject, annotationTargets, annotationBody);
 
         String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotationBody.toString(),
             Constants.AO_ANNOTATION_BODY_HEADER);
         ResponseBuilder response = Response.created(annotation).header(Constants.LINK_HEADER, annotationBodyHeader);
         for (URI target : annotationTargets) {
             String targetHeader = String.format(Constants.LINK_HEADER_TEMPLATE, target.toString(),
                 Constants.AO_ANNOTATES_RESOURCE_HEADER);
             response = response.header(Constants.LINK_HEADER, targetHeader);
         }
         return response.build();
     }
 
 
     /**
      * Update the annotation targets and the annotation body of an annotation. This method does not add/delete the
      * annotation body to the triplestore.
      * 
      * @param researchObject
      *            the research object
      * @param annotation
      *            annotation URI
      * @param annotationBody
      *            annotation body URI
      * @param annotationTargets
      *            list of annotated resources URIs
      * @return 200 OK response
      */
     public static Response updateAnnotation(URI researchObject, URI annotation, URI annotationBody,
             List<URI> annotationTargets) {
         ROSRService.SMS.get().updateAnnotation(researchObject, annotation, annotationTargets, annotationBody);
 
         String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotationBody.toString(),
             Constants.AO_ANNOTATION_BODY_HEADER);
         ResponseBuilder response = Response.ok().header(Constants.LINK_HEADER, annotationBodyHeader);
         for (URI target : annotationTargets) {
             String targetHeader = String.format(Constants.LINK_HEADER_TEMPLATE, target.toString(),
                 Constants.AO_ANNOTATES_RESOURCE_HEADER);
             response = response.header(Constants.LINK_HEADER, targetHeader);
         }
         return response.build();
     }
 
 
     /**
      * Get the URI of an annotation body. If acceptHeader is not null, will return a format-specific URI.
      * 
      * @param researchObject
      *            research object in which the annotation is defined
      * @param annotation
      *            the annotation
      * @param acceptHeader
      *            MIME type requested (content negotiation) or null
      * @return URI of the annotation body
      * @throws NotFoundException
      *             could not find the resource in DL when checking if it's internal
      * @throws DigitalLibraryException
      *             could not connect to the DL to check if it's internal
      */
     public static URI getAnnotationBody(URI researchObject, URI annotation, String acceptHeader)
             throws NotFoundException, DigitalLibraryException {
         URI body = ROSRService.SMS.get().getAnnotationBody(researchObject, annotation);
         RDFFormat acceptFormat = RDFFormat.forMIMEType(acceptHeader);
         if (acceptFormat != null && isInternalResource(researchObject, body)) {
             RDFFormat extensionFormat = RDFFormat.forFileName(annotation.getPath());
             return createFormatSpecificURI(body, extensionFormat, acceptFormat);
         } else {
             return body;
         }
     }
 
 
     /**
      * Delete and deaggregate an annotation. Does not delete the annotation body.
      * 
      * @param researchObject
      *            research object in which the annotation is defined
      * @param annotation
      *            the annotation
      * @return 204 No Content
      */
     public static Response deleteAnnotation(URI researchObject, URI annotation) {
         ROSRService.SMS.get().deleteAnnotation(researchObject, annotation);
         return Response.noContent().build();
     }
 
 
     private static void updateROAttributesInDlibra(URI researchObject) {
         String researchObjectId = getResearchObjectId(researchObject);
         Multimap<URI, Object> roAttributes = ROSRService.SMS.get().getAllAttributes(researchObject);
         roAttributes.put(URI.create("Identifier"), researchObject);
         try {
             ROSRService.DL.get().storeAttributes(Constants.workspaceId, researchObjectId, Constants.versionId,
                 roAttributes);
         } catch (Exception e) {
             LOG.error("Caught an exception when updating RO attributes, will continue", e);
         }
     }
 
 
     private static void updateNamedGraphInDlibra(String filePath, URI researchObject, URI namedGraphURI)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String researchObjectId = getResearchObjectId(researchObject);
         RDFFormat format = RDFFormat.forFileName(filePath, RDFFormat.RDFXML);
         InputStream dataStream = ROSRService.SMS.get().getNamedGraphWithRelativeURIs(namedGraphURI, researchObject,
             format);
         ROSRService.DL.get().createOrUpdateFile(Constants.workspaceId, researchObjectId, Constants.versionId, filePath,
             dataStream, format.getDefaultMIMEType());
     }
 
 
     private static String getResearchObjectId(URI researchObject) {
         String[] segments = researchObject.getPath().split("/");
         return segments[segments.length - 1];
     }
 
 }
