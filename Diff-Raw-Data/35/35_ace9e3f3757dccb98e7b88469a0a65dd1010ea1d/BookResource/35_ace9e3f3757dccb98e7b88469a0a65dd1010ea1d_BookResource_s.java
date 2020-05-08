 /**
  * Created by Apache CXF WadlToJava code generator
 **/
 package com.bp.bs;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 
import org.apache.cxf.jaxrs.model.wadl.ElementClass;

@Path("/books/{isbn}")
 public interface BookResource {
 
     @GET
     @Produces("application/xml")
    @ElementClass(response = BookState.class)
    Response get(@Context Request request, @PathParam("isbn") String isbn);
 
 }
