 package org.triple_brain.service.resources;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.model.*;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.GraphElement;
 import org.triple_brain.module.model.graph.Vertex;
 import org.triple_brain.module.model.validator.FriendlyResourceValidator;
 import org.triple_brain.module.search.GraphIndexer;
 import org.triple_brain.service.ResourceServiceUtils;
 
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
         SAME_AS, TYPE, GENERIC
     }
 
     public static final String IDENTIFICATION_TYPE_STRING = "type";
 
     @Inject
     FriendlyResourceFactory friendlyResourceFactory;
 
     @Inject
     GraphIndexer graphIndexer;
 
     @Inject
     ResourceServiceUtils resourceServiceUtils;
 
     @Inject
     BeforeAfterEachRestCall beforeAfterEachRestCall;
 
     private GraphElement graphElement;
     private boolean isVertex;
 
     @AssistedInject
     public GraphElementIdentificationResource(
             @Assisted GraphElement graphElement,
             @Assisted boolean isVertex
     ) {
         this.graphElement = graphElement;
         this.isVertex = isVertex;
     }
 
     @POST
     @Produces(MediaType.TEXT_PLAIN)
     @Path("/")
     public Response add(JSONObject identification) {
         FriendlyResourceValidator validator = new FriendlyResourceValidator();
         if(!validator.validate(identification).isEmpty()){
             throw new WebApplicationException(
                     Response.Status.NOT_ACCEPTABLE
             );
         }
         FriendlyResource friendlyResource = friendlyResourceFactory.createOrLoadUsingJson(
                 identification
         );
         String type = identification.optString("type");
         if (type.equalsIgnoreCase(identification_types.SAME_AS.name())) {
             graphElement.addSameAs(
                     friendlyResource
             );
         } else if (type.equalsIgnoreCase(identification_types.TYPE.name())) {
             graphElement.addType(
                     friendlyResource
             );
         } else if(type.equalsIgnoreCase(identification_types.GENERIC.name())){
             graphElement.addGenericIdentification(
                     friendlyResource
             );
         } else {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
         reindexGraphElement();
         FriendlyResourceCached friendlyResourceCached = FriendlyResourceCached.fromFriendlyResource(
                 friendlyResource
         );
         updateImagesOfExternalResourceIfNecessary(
                 friendlyResourceCached
         );
         updateDescriptionOfExternalResourceIfNecessary(
                 friendlyResourceCached
         );
         return Response.ok().build();
     }
 
     @DELETE
     @Produces(MediaType.TEXT_PLAIN)
    @Path("/{friendly_resource_uri}")
     public Response removeFriendlyResource(
             @PathParam("friendly_resource_uri") String friendlyResourceUri
     ) {
         try {
             friendlyResourceUri = decodeURL(friendlyResourceUri);
         } catch (UnsupportedEncodingException e) {
             return Response.status(Response.Status.BAD_REQUEST).build();
         }
         FriendlyResource friendlyResource = friendlyResourceFactory.createOrLoadFromUri(
                 URI.create(friendlyResourceUri)
         );
         graphElement.removeIdentification(friendlyResource);
        reindexGraphElement();
         return Response.ok().build();
     }
 
     private void updateImagesOfExternalResourceIfNecessary(FriendlyResourceCached friendlyResource) {
         if (!friendlyResource.gotTheImages()) {
             if (FreebaseFriendlyResource.isFromFreebase(friendlyResource)) {
                 FreebaseFriendlyResource freebaseResource = FreebaseFriendlyResource.fromFriendlyResource(
                         friendlyResource
                 );
                 freebaseResource.getImages(
                         resourceServiceUtils.imagesUpdateHandler
                 );
             }
         }
     }
 
     private void updateDescriptionOfExternalResourceIfNecessary(FriendlyResourceCached friendlyResource) {
         if (!friendlyResource.gotComments()) {
             if (FreebaseFriendlyResource.isFromFreebase(friendlyResource)) {
                 FreebaseFriendlyResource freebaseResource = FreebaseFriendlyResource.fromFriendlyResource(
                         friendlyResource
                 );
                 freebaseResource.getDescription(
                         resourceServiceUtils.descriptionUpdateHandler
                 );
             }
         }
     }
 
     private void reindexGraphElement(){
         if(isVertex){
             graphIndexer.indexVertex(
                     (Vertex) graphElement
             );
         }else{
             graphIndexer.indexRelation(
                     (Edge) graphElement
             );
         }
     }
 }
