 package org.triple_brain.service.resources;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.model.ExternalFriendlyResource;
 import org.triple_brain.module.model.FreebaseExternalFriendlyResource;
 import org.triple_brain.module.model.graph.GraphElement;
 import org.triple_brain.module.model.json.ExternalResourceJson;
 import org.triple_brain.service.ExternalResourceServiceUtils;
 
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 
 import static org.triple_brain.module.common_utils.Uris.decodeURL;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 public class GraphElementIdentificationResource {
     public static enum identification_types {
         SAME_AS, TYPE
     }
 
     public static final String IDENTIFICATION_TYPE_STRING = "type";
 
     @Inject
     ExternalResourceServiceUtils externalResourceServiceUtils;
 
     private GraphElement graphElement;
 
     @AssistedInject
     public GraphElementIdentificationResource(
             @Assisted GraphElement graphElement
     ) {
         this.graphElement = graphElement;
     }
 
     @POST
     @Produces(MediaType.TEXT_PLAIN)
     @Path("/")
     public Response add(JSONObject identification) {
         ExternalFriendlyResource externalFriendlyResource = ExternalResourceJson.fromJson(
                 identification
         );
         String type = identification.optString("type");
         if (type.equalsIgnoreCase(identification_types.SAME_AS.name())) {
             graphElement.addSameAs(
                     externalFriendlyResource
             );
         } else if (type.equalsIgnoreCase(identification_types.TYPE.name())) {
             graphElement.addType(
                     externalFriendlyResource
             );
         } else {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
         updateImagesOfExternalResourceIfNecessary(externalFriendlyResource);
         updateDescriptionOfExternalResourceIfNecessary(externalFriendlyResource);
         return Response.ok().build();
     }
 
     @DELETE
     @Produces(MediaType.TEXT_PLAIN)
     @Path("{friendly_resource_uri}")
     public Response removeFriendlyResource(
             @PathParam("friendly_resource_uri") String friendlyResourceUri
     ) {
         try {
             friendlyResourceUri = decodeURL(friendlyResourceUri);
         } catch (UnsupportedEncodingException e) {
             return Response.status(Response.Status.BAD_REQUEST).build();
         }
        ExternalFriendlyResource type = graphElement.friendlyResourceWithUri(
                 URI.create(friendlyResourceUri)
         );
        graphElement.removeFriendlyResource(type);
         return Response.ok().build();
     }
 
     private void updateImagesOfExternalResourceIfNecessary(ExternalFriendlyResource externalFriendlyResource) {
         if (!externalFriendlyResource.gotTheImages()) {
             if (FreebaseExternalFriendlyResource.isFromFreebase(externalFriendlyResource)) {
                 FreebaseExternalFriendlyResource freebaseResource = FreebaseExternalFriendlyResource.fromExternalResource(
                         externalFriendlyResource
                 );
                 freebaseResource.getImages(
                         externalResourceServiceUtils.imagesUpdateHandler
                 );
             }
         }
     }
 
     private void updateDescriptionOfExternalResourceIfNecessary(ExternalFriendlyResource externalFriendlyResource) {
         if (!externalFriendlyResource.gotADescription()) {
             if (FreebaseExternalFriendlyResource.isFromFreebase(externalFriendlyResource)) {
                 FreebaseExternalFriendlyResource freebaseResource = FreebaseExternalFriendlyResource.fromExternalResource(
                         externalFriendlyResource
                 );
                 freebaseResource.getDescription(
                         externalResourceServiceUtils.descriptionUpdateHandler
                 );
             }
         }
     }
 }
