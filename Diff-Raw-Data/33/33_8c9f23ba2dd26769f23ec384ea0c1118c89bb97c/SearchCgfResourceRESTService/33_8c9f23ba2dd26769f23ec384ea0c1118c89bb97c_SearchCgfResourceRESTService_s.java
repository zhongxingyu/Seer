 package com.laurinka.skga.server.rest;
 
 import com.laurinka.skga.server.rest.model.NameNumberXml;
 import com.laurinka.skga.server.services.WebsiteService;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import java.util.List;
 
 /**
  * JAX-RS Example
  * <p/>
  * This class produces a RESTful service to read the contents of the cgf members
  * table.
  */
 @Path("/cgfs")
 @RequestScoped
 public class SearchCgfResourceRESTService extends AbstractMemberResourceRestService {
     @GET
     @Path("/search")
     @Produces("text/xml")
     public List<NameNumberXml> lookupMemberByName(@QueryParam("q") String q) {
         String s = "select new com.laurinka.skga.server.rest.model.NameNumberXml(m.name2, m.nr) from CgfNumber m ";
        return getNameNumberXmls(q, s);
     }
 }
