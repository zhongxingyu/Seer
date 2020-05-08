 package com.uwetrottmann.getglue.services;
 
import com.uwetrottmann.getglue.entities.GetGlueObject;
 import retrofit.http.GET;
import retrofit.http.Path;
 
 /**
  * Endpoints for <a href="http://developer.getglue.com/#object-resources">Object</a>.
  */
 public interface ObjectService {
 
    @GET("/tv_shows/{object-id}")
    GetGlueObject getTvShow(
            @Path("object-id") String objectId
     );
 
 }
