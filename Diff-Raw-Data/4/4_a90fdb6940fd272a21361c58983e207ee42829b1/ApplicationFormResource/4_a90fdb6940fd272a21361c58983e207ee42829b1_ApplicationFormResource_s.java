 package jmdb.tutorial.dropwizard.app.applicationform;
 
 import com.yammer.dropwizard.views.View;
 import jmdb.tutorial.dropwizard.app.FreemarkerView;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 @Path("/applicationform")
 @Produces(MediaType.TEXT_HTML)
 public class ApplicationFormResource {
 
     @GET
     public FreemarkerView blankApplicationForm() {
        return new FreemarkerView("applicationform/applicationform.ftl");
     }
 

 }
