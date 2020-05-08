 package com.jonmort.plugin.aoso;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 
 @Path("/search/{scope}/{search:.*}")
 public class SearchResource {
     private final StorageService storageService;
 
     public SearchResource(StorageService storageService) {
         this.storageService = storageService;
     }
 
 
     @GET
     public Response get(@PathParam("scope") String scope,
                         @PathParam("search") String search,
                         @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("offset") @DefaultValue("0") int offset) {
        Iterable<Storage> storage = storageService.search(scope, search, 10, 0);
         if (storage != null) {
             return Response.ok(StorageRepresentation.fromStorage(storage)).build();
         }
         return Response.status(Response.Status.NOT_FOUND).build();
     }
 
 }
