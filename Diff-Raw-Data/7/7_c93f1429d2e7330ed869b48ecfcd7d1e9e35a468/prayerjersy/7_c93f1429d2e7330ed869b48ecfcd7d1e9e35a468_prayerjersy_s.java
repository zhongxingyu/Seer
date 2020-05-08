 package il.ac.tau.team3.prayerjersy;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.SPGeoPoint;
 import il.ac.tau.team3.datastore.EMF;
 import il.ac.tau.team3.datastore.PlaceLocation;
 import il.ac.tau.team3.datastore.UserLocation;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 
 import javax.ws.rs.Path;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import com.beoui.geocell.GeocellManager;
 import com.beoui.geocell.model.GeocellQuery;
 import com.beoui.geocell.model.Point;
 import com.google.appengine.api.users.UserService;
 
 //The Java class will be hosted at the URI path "/helloworld"
 
 @Path("/prayerjersy")
 public class prayerjersy {
 
 	private static final Logger log = Logger.getLogger(ServerCls.class
 			.getName());
 	private EntityManager entity;
 
 	public prayerjersy() {
 		entity = EMF.get().createEntityManager();
 	}
 
 	/*@GET
 	@Path("/user/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public GeneralUser retrieveUser(@PathParam("id") int id) {
 		if(id < users.size()){
 			return users.get(id);
 		}
 		else return null;
 	}
 	*/
 	@GET
 	@Path("/updateuserbyid")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean UpdateUserLocationById(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("id") long id){
 		entity.getTransaction().begin();
 		UserLocation userx = entity.find(UserLocation.class, id);
 		if(userx!=null){
 			userx.setLatitude(latitude);
 			userx.setLongitude(longitude);
 			userx.setGeoCellsData(latitude, longitude);
 			entity.getTransaction().commit();
 			return true;
 		}
 		return false;
 	}
 	
 	
 	
 	@POST
 	@Path("/updateuserbyname")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Long UpdateUserLocationByName(GeneralUser user){
 		
 		Query q = entity.createQuery("SELECT x FROM UserLocation x WHERE x.name='"+user.getName()+"'");
 		 
 		List<UserLocation> list = q.getResultList();
 		if(list.isEmpty()){
 			return this.createUserInDS(user.getSpGeoPoint().getLongitudeInDegrees(), user.getSpGeoPoint().getLatitudeInDegrees(), user.getName(), user.getStatus());
 			// return Response.status(Response.Status.ACCEPTED).build();
 			 
 		}
 		
 		UserLocation userx = (UserLocation)list.get(0);
 		entity.getTransaction().begin();
 		userx.setLatitude(user.getSpGeoPoint().getLatitudeInDegrees());
 		userx.setLongitude(user.getSpGeoPoint().getLongitudeInDegrees());
 		userx.setGeoCellsData(user.getSpGeoPoint().getLatitudeInDegrees(), user.getSpGeoPoint().getLongitudeInDegrees());
 		userx.setStatus(user.getStatus());
 		entity.getTransaction().commit();
 		
 		return userx.getKey();
 		//return Response.status(Response.Status.ACCEPTED).build();
 	}
 	
 	@POST
 	@Path("/updateplacebylocation")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Long UpdatePlaceLocationByLocation(GeneralPlace place){
 		
 		//Query q = entity.createQuery("SELECT x FROM PlaceLocation x WHERE " +
 		//		"x.latitude="+place.getSpGeoPoint().getLatitudeInDegrees()+" AND x.longitude="+place.getSpGeoPoint().getLongitudeInDegrees());
 		 
 		
 		//if(q.getResultList().size() == 0){
 			return createPlaceInDS(place);
 			//return Response.status(Response.Status.ACCEPTED).build();
 		//}
 		
 		// register changed place in DS
 		/*entity.getTransaction().begin();
 		
 		entity.getTransaction().commit();*/
 		
 		//return Response.status(Response.Status.ACCEPTED).build();
 	}
 	
 	@POST
 	@Path("/addjoiner")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response UpdatePlaceJoinersByName(GeneralPlace place){
 		
 		//Query q = entity.createQuery("SELECT x FROM PlaceLocation x WHERE x.id='"+place.getId()+"'");
 		 
 		
 		//PlaceLocation placex = (PlaceLocation)q.getResultList().get(0);
 		PlaceLocation placex = entity.find(PlaceLocation.class, place.getId());
 		if(placex!=null){
 			entity.getTransaction().begin();
 			placex.setAllJoiners(place.getAllJoiners());
 			entity.getTransaction().commit();
 		}
 		return Response.status(Response.Status.ACCEPTED).build();
 		
 	}
 	
 	
 	@GET
 	@Path("/users")
 	@Produces(MediaType.APPLICATION_JSON)
 	public GeneralUser[] retrieveAllUsers(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("radius")long radius) {
 		return convertServerUserObjToClientUserObj(requestDatastoreForUsers(longitude, latitude, radius)).toArray(new GeneralUser[0]);
 	}
 	
 	@GET
 	@Path("/places")
 	@Produces(MediaType.APPLICATION_JSON)
 	public GeneralPlace[] retrieveAllPlaces(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("radius")long radius) {
 		return convertServerPlaceObjToClientPlaceObj(requestDatastoreForPlaces(longitude, latitude, radius)).toArray(new GeneralPlace[0]);
 	}
 	
 	@GET
 	@Path("/newuser")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Long CreateNewUser(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("name")String name,@QueryParam("status")String status) {
 		return this.createUserInDS(longitude, latitude, name, status);
 	}
 	
 	/*@GET
 	@Path("/newplace")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Long CreateNewPlace(@QueryParam("longitude") double longitude, @QueryParam("latitude")double latitude, @QueryParam("name")String name,@QueryParam("status")String address) {
 		GeneralPlace u = new GeneralPlace(name,address,new SPGeoPoint((int)latitude*1000000, (int)longitude*1000000));
 		
 		PlaceLocation uloc = new PlaceLocation(u);
 		entity.getTransaction().begin();
 		entity.persist(uloc);
 		entity.getTransaction().commit();
 		
 		return uloc.getKey();
 	}*/
 	
 	@GET
 	@Path("/signtoplace")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean SignToPlace(@QueryParam("id") long id,  @QueryParam("name")String name) {
 		entity.getTransaction().begin();
 		PlaceLocation placex = entity.find(PlaceLocation.class, id);
 		if(placex!=null){
 			placex.addJoiner(name);
 			entity.getTransaction().commit();
 			return true;
 		}
 		return false;
 	}
 	
 /*	@GET
 	@Path("/place/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public GeneralPlace retrievePlace(@PathParam("id") int id) {
 		if (id < places.size()) return places.get(id);
 		else return null;
 	}
 */
 	@PUT
 	@Consumes("application/json")
 	public void store(GeneralUser user) {
 
 	}
 
 	@GET
 	@Path("/deleteuser")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean removeUser(@QueryParam("id") long id) {
 		entity.getTransaction().begin();
 		UserLocation userx = entity.find(UserLocation.class, id);
 		if(userx!=null){
 		entity.remove(userx); 
 		entity.getTransaction().commit();
 		return true;
 		}
 		return false;
 	}
 	
 	@POST
 	@Path("/addjoinerbyid")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public boolean AddJoinerByPlaceId(@QueryParam("joiner")String joiner, @QueryParam("id") long id){
 		entity.getTransaction().begin();
 		PlaceLocation placex =
 
 			entity.find(PlaceLocation.class, id);
 		if(placex!=null){
 			placex.addJoiner(joiner);
 
 			entity.getTransaction().commit();
 			return true;
 		}
 
 		return false;
 	}
 	
 	@GET
 	@Path("/deleteplace")
 	@Produces(MediaType.APPLICATION_JSON)
 	public boolean removePlace(@QueryParam("id") long id) {
 		entity.getTransaction().begin();
 		PlaceLocation placex = entity.find(PlaceLocation.class, id);
 		if(placex!=null){
 		entity.remove(placex); 
 		entity.getTransaction().commit();
 		return true;
 		}
 		return false;
 	}
 	
 	private List<UserLocation> requestDatastoreForUsers(double longitude,double latitude,long radius){
 		GeocellQuery baseQuery = new GeocellQuery("SELECT x FROM UserLocation x");
 		Point center = new Point (latitude, longitude);
		List<UserLocation> results = GeocellManager.proximitySearch(center, 100000000, radius, UserLocation.class, baseQuery, entity, 13);
 		return results;
 	}
 	private List<PlaceLocation> requestDatastoreForPlaces(double longitude,double latitude,long radius){
 		GeocellQuery baseQuery = new GeocellQuery("SELECT x FROM PlaceLocation x");
 		Point center = new Point (latitude, longitude);
		List<PlaceLocation> results = GeocellManager.proximitySearch(center, 100000000, radius, PlaceLocation.class, baseQuery, entity, 13);
 		return results;
 	}
 	
 	
 	private List<GeneralUser> convertServerUserObjToClientUserObj(List<UserLocation> serverUsers){
 		List<GeneralUser> returnList = new ArrayList<GeneralUser>();
 		for(UserLocation serverUser : serverUsers){
 			GeneralUser tmp = new GeneralUser(serverUser.getName(),new SPGeoPoint((int)(serverUser.getLatitude()*1000000), (int)(serverUser.getLongitude()*1000000)),serverUser.getStatus());
 			tmp.setId(serverUser.getKey());
 			returnList.add(tmp);
 			
 		}
 		return returnList;
 	}
 	
 	private List<GeneralPlace> convertServerPlaceObjToClientPlaceObj(List<PlaceLocation> serverPlaces){
 		List<GeneralPlace> returnList = new ArrayList<GeneralPlace>();
 		for(PlaceLocation serverPlace : serverPlaces){
 			GeneralPlace tmp = new GeneralPlace(serverPlace.getName(),serverPlace.getAddress(),new SPGeoPoint((int)(serverPlace.getLatitude()*1000000), (int)(serverPlace.getLongitude()*1000000)));
 			tmp.setId(serverPlace.getKey());
 			tmp.setAllJoiners(new ArrayList<String>(serverPlace.getAllJoiners()));
 			returnList.add(tmp);
 			
 		}
 		return returnList;
 	}
 	
 	private Long createUserInDS(double longitude, double latitude, String name,String status){
 		GeneralUser u = new GeneralUser(name,new SPGeoPoint((int)(latitude*1000000), (int)(longitude*1000000)),status);
 		UserLocation uloc = new UserLocation(u);
 		entity.getTransaction().begin();
 		entity.persist(uloc);
 		entity.getTransaction().commit();
 		
 		return uloc.getKey();
 	}
 	
 	private Long createPlaceInDS(GeneralPlace place){
 		//GeneralPlace u = new GeneralPlace(name,address,new SPGeoPoint((int)(latitude*1000000), (int)(longitude*1000000)));
 		PlaceLocation uloc = new PlaceLocation(place);
 		entity.getTransaction().begin();
 		entity.persist(uloc);
 		entity.getTransaction().commit();
 		
 		return uloc.getKey();
 	}
 }
