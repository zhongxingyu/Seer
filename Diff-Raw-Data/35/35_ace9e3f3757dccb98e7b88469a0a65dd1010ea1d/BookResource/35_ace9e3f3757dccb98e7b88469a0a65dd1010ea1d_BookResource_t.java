 /**
  * Created by Apache CXF WadlToJava code generator
 **/
 package com.bp.bs;
 
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
@Path("/books")
 public interface BookResource {
 
    @POST
    @Consumes("application/xml")
    @Produces("application/octet-stream")
    Response add(BookState bookstate);

    @DELETE
    @Produces("application/octet-stream")
    @Path("/{isbn}")
    Response deleteIsbn(@PathParam("isbn") String isbn);

     @GET
     @Produces("application/xml")
    @Path("/{isbn}")
    BookState getIsbn(@PathParam("isbn") String isbn);
 
    @PUT
    @Consumes("application/xml")
    @Produces("application/octet-stream")
    @Path("/{isbn}")
    Response update(@PathParam("isbn") String isbn, BookState bookstate);
 }
