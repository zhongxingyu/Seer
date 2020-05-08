 package org.openshift.webservice;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.persistence.UniqueConstraint;
 import javax.ws.rs.Produces;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 
 import org.openshift.lucene.FileHandler;
 import org.openshift.lucene.QueryHandler;
 
 @Path("/parks")
 public class lucenews {
 	
 	QueryHandler queryHandler = new QueryHandler();
 	
 	//TODO why does injection work here but not in QueryHandler
 	@Inject
 	private FileHandler fileHandler;
 	
 	@GET()
 	@Produces("application/json")
 	public List getAllParks(){
 		ArrayList allParksList = new ArrayList();
 		
 		allParksList = (ArrayList) queryHandler.getAllParks(fileHandler);
 		
 		return allParksList;
 	}
 	
 	@GET()
 	@Produces("application/json")
 	@Path("near")
 	public List findParksNear(@QueryParam("lat") float lat, @QueryParam("lon") float lon){
 		ArrayList<Map> allParksList = new ArrayList<Map>();
 		
 		//how big a radius search
 		double distance = 1.5;
 		
		allParksList = (ArrayList) queryHandler.getParksNear(lat, lon, distance, fileHandler);
 		
 		return allParksList;
 	}
 	
 	@GET()
 	@Produces("application/json")
 	@Path("within")
     public List findParksWithin(@QueryParam("lat1") float lat1, @QueryParam("lon1") float lon1, @QueryParam("lat2") float lat2, @QueryParam("lon2") float lon2){
         
 		List allParksList = queryHandler.getABoxOfPoints(lon2, lon1, lat2, lat1, fileHandler);
 		
 	    return allParksList;
 	}
 }
