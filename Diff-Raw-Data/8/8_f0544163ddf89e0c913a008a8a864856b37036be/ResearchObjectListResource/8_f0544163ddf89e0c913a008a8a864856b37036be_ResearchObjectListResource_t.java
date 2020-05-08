 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilderException;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.UserProfile;
 import pl.psnc.dl.wf4ever.common.UserProfile.Role;
 import pl.psnc.dl.wf4ever.dlibra.ConflictException;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * A list of research objects REST APIs.
  * 
  * @author piotrhol
  * 
  */
 @Path("ROs/")
 public class ResearchObjectListResource {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(ResearchObjectListResource.class);
 
     /** HTTP request. */
     @Context
     HttpServletRequest request;
 
     /** URI info. */
     @Context
     UriInfo uriInfo;
 
 
     /**
      * Returns list of relative links to research objects.
      * 
      * @return 200 OK
      */
     @GET
     @Produces("text/plain")
     public Response getResearchObjectList() {
         UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 
         Set<URI> list;
         if (user.getRole() == Role.PUBLIC) {
             list = ROSRService.SMS.get().findResearchObjects();
         } else {
             list = ROSRService.SMS.get().findResearchObjectsByCreator(user.getUri());
         }
         StringBuilder sb = new StringBuilder();
         for (URI id : list) {
             sb.append(id.toString());
             sb.append("\r\n");
         }
 
         ContentDisposition cd = ContentDisposition.type("text/plain").fileName("ROs.txt").build();
 
         return Response.ok().entity(sb.toString()).header("Content-disposition", cd).build();
     }
 
 
     /**
      * Creates new RO with given RO_ID.
      * 
      * @return 201 (Created) when the RO was successfully created, 409 (Conflict) if the RO_ID is already used in the
      *         WORKSPACE_ID workspace
      * @throws BadRequestException
      *             the RO id is incorrect
      * @throws NotFoundException
      *             problem with creating the RO in dLibra
      * @throws DigitalLibraryException
      *             problem with creating the RO in dLibra
      * @throws ConflictException
      *             problem with creating the RO in dLibra
      * @throws UriBuilderException
      *             problem with creating the RO in dLibra
      * @throws IllegalArgumentException
      *             problem with creating the RO in dLibra
      */
     @POST
     public Response createResearchObject()
             throws BadRequestException, IllegalArgumentException, UriBuilderException, ConflictException,
             DigitalLibraryException, NotFoundException {
         LOGGER.debug(String.format("%s\t\tInit create RO", new DateTime().toString()));
         String researchObjectId = request.getHeader(Constants.SLUG_HEADER);
         if (researchObjectId == null || researchObjectId.isEmpty()) {
             throw new BadRequestException("Research object ID is null or empty");
         }
        URI uri = uriInfo.getAbsolutePathBuilder().path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.findByUri(uri);
        if (researchObject != null) {
            throw new ConflictException("RO already exists");
        }
        researchObject = new ResearchObject(uri);
         URI researchObjectURI = ROSRService.createResearchObject(researchObject);
         LOGGER.debug(String.format("%s\t\tRO created", new DateTime().toString()));
 
         RDFFormat format = RDFFormat.forMIMEType(request.getHeader(Constants.ACCEPT_HEADER), RDFFormat.RDFXML);
         InputStream manifest = ROSRService.SMS.get().getNamedGraph(researchObject.getManifestUri(), format);
         ContentDisposition cd = ContentDisposition.type(format.getDefaultMIMEType())
                 .fileName(ResearchObject.MANIFEST_PATH).build();
 
         LOGGER.debug(String.format("%s\t\tReturning", new DateTime().toString()));
         return Response.created(researchObjectURI).entity(manifest).header("Content-disposition", cd).build();
     }
 }
