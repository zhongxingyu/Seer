 package org.triple_brain.module.model;
 
 import java.net.URI;
 import java.util.UUID;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class UserUris {
     private User user;
     public UserUris(User user){
         this.user = user;
     }
 
     public URI baseUri() {
         return URI.create(
                 "/users/" + user.username()
         );
     }
 
     public URI graphUri() {
         return URI.create(
                 baseUri() + "/graph"
         );
 
     }
 
     public URI baseVertexUri() {
         return URI.create(
                 graphUri() + "/vertex"
         );
     }
 
     public URI baseEdgeUri() {
         return URI.create(
                 graphUri() + "/edge"
         );
     }
 
     public URI defaultVertexUri() {
         return URI.create(
                 baseVertexUri() + "/" + TripleBrainUris.DEFAULT_VERTEX_END_OF_URI
         );
     }
 
     public URI edgeUriFromShortId(String shortId){
         return URI.create(
                 baseEdgeUri() + "/" + shortId
         );
     }
 
     public URI vertexUriFromShortId(String shortId){
         return URI.create(
             baseVertexUri() + "/" + shortId
         );
     }
 
     public URI generateVertexUri() {
         return URI.create(
                 baseVertexUri() + "/" + UUID.randomUUID().toString()
         );
     }
 
     public URI generateEdgeUri() {
         return URI.create(
                 baseEdgeUri() + "/" + UUID.randomUUID().toString()
         );
     }

    public static String edgeShortId(String id){
        return id.substring(id.lastIndexOf("/") + 1);
    }
 }
