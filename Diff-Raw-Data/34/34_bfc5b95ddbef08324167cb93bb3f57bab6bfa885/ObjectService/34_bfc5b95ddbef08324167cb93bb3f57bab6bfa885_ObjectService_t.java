 package com.uwetrottmann.getglue.services;
 
import com.uwetrottmann.getglue.entities.GetGlueResource;
import retrofit.http.EncodedPath;
 import retrofit.http.GET;
 
 /**
  * Endpoints for <a href="http://developer.getglue.com/#object-resources">Object</a>.
  */
 public interface ObjectService {
 
    @GET("/{object-id}")
    GetGlueResource getObject(
            @EncodedPath("object-id") String objectId
     );
 
 }
