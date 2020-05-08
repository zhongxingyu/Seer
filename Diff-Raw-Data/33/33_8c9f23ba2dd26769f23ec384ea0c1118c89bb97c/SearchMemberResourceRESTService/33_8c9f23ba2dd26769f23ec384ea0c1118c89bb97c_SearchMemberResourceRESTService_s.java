 package com.laurinka.skga.server.rest;
 
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import com.laurinka.skga.server.rest.model.NameNumberXml;
import com.laurinka.skga.server.services.WebsiteService;
 
 /**
  * JAX-RS Example
  * <p/>
  * This class produces a RESTful service to read the contents of the members
  * table.
  */
 @Path("/members")
 @RequestScoped
public class SearchMemberResourceRESTService extends AbstractMemberResourceRestService{
 
     @GET
     @Path("/search")
     @Produces("text/xml")
     public List<NameNumberXml> lookupMemberByName(@QueryParam("q") String q) {
         String s = "select new com.laurinka.skga.server.rest.model.NameNumberXml(m.name2, m.nr) from SkgaNumber m ";
        return getNameNumberXmls(q, s);
     }
 
 }
