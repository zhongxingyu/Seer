 package org.cloudydemo;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.cloudydemo.model.Application;
 
 import com.google.gson.Gson;
 
 @Path("/display/{since}")
 @Stateless
 public class DisplayHits {
 	@EJB
 	private HitTracker hitTracker;
 	
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
	public String getHits(@PathParam("since") @DefaultValue("0") long time) {
 		Application app = hitTracker.displayHitsSince(time);
 		return new Gson().toJson(app);
 	}
}
