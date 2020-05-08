 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.smartitengineering.demo.roa;
 
 import com.smartitengineering.demo.roa.domains.Author;
 import com.smartitengineering.demo.roa.services.Services;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
 
 /**
  *
  * @author imyousuf
  */
 @Path("/authors")
 public class AuthorsResource extends AbstractResource {
 
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response createAuthor(Author author) {
     ResponseBuilder responseBuilder;
     try {
       Services.getInstance().getAuthorService().save(author);
       responseBuilder = Response.status(Status.CREATED);
      UriBuilder authorClone = AuthorResource.AUTHOR_URI_BUILDER.clone();
      setBaseUri(authorClone);
      responseBuilder.location(authorClone.build(author.getId()));
     }
     catch (Exception ex) {
       responseBuilder = Response.status(Status.INTERNAL_SERVER_ERROR);
       ex.printStackTrace();
     }
     return responseBuilder.build();
   }
 }
