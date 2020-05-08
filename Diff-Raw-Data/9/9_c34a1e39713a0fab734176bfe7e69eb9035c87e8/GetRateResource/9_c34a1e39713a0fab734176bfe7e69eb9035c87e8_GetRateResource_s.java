 package parkservice.resources;
 
 
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.ContextResolver;
 import javax.xml.bind.JAXBElement;
 
 import com.parq.server.dao.ClientDao;
 import com.parq.server.dao.GeolocationDao;
 import com.parq.server.dao.ParkingRateDao;
 import com.parq.server.dao.ParkingSpaceDao;
 import com.parq.server.dao.UserDao;
 import com.parq.server.dao.model.object.Geolocation;
 import com.parq.server.dao.model.object.ParkingRate;
 import com.parq.server.dao.model.object.ParkingSpace;
 import com.parq.server.dao.model.object.User;
 
 import parkservice.model.AuthRequest;
 import parkservice.model.GpsRequest;
 import parkservice.model.QrcodeRequest;
 import parkservice.model.RateObject;
 import parkservice.model.RateResponse;
 
 @Path("/")
 public class GetRateResource {
 	@Context 
 	ContextResolver<JAXBContextResolver> providers;
 
 
 	@POST
 	@Path("/qrcode")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public RateResponse unwrapQrcode(JAXBElement<QrcodeRequest> info){
 		QrcodeRequest input = info.getValue();
 		AuthRequest userInfo = input.getUserInfo();
 		RateResponse test = new RateResponse();
 		long uid=input.getUid();
 		if(uid==innerAuthenticate(userInfo)){
 			//http://www.parqme.com/x86gg0/a80
 
 			ParkingRateDao p = new ParkingRateDao();
 			//getbyname should use p.getParkingRateByName(x86gg0, a808);
 			ParkingRate pr = null;
 			try{
 				pr =p.getParkingRateByName(input.getLot(), input.getSpot());
 			}catch (Exception e){}
			
 			GeolocationDao gdao = new GeolocationDao();
 			ParkingSpaceDao psdao = new ParkingSpaceDao();
 			ParkingSpace pspace = null;
 			Geolocation loc = null; 
 			try{
 				loc = gdao.getLocationById(pr.getLocationId());
 				pspace =psdao.getParkingSpaceBySpaceIdentifier(input.getLot(), input.getSpot());
 			}catch (Exception e){}
 
			RateResponse output = new RateResponse();
 			if(loc!=null && pspace!=null){
 				RateObject rate = new RateObject();
 				rate.setLat(loc.getLatitude());
 				rate.setLon(loc.getLongitude());
 				rate.setLocation(pspace.getSpaceName());
 				rate.setSpotId(pr.getSpaceId());
 				rate.setMinTime(pr.getMinParkMins());
 				rate.setMaxTime(pr.getMaxParkMins());
 				rate.setDefaultRate(pr.getParkingRateCents());
 				rate.setMinIncrement(pr.getTimeIncrementsMins());
 				output.setResp("OK");
 				output.setRateObject(rate);
 				return output;
 			}else{
 				String x = "";
 				if(loc==null) x+=pr.getLocationId();
 				if(pspace==null) x+="spacid:" +pr.getSpaceId() + " lot:" + input.getLot() + " spot:" + input.getSpot();
 				x+= pr.getLocationName() + pr.getLocationId() + pr.getMaxParkMins() + pr.getSpaceName() + pr.getTimeIncrementsMins();
 				
 				output.setResp(x);
 				return output;
 			}
 		}else{
 			test.setResp("BAD_AUTH");
 			return test;
 		}		
 	}
 
 	@POST
 	@Path("/gps")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public RateResponse unwrapGps(JAXBElement<GpsRequest> gpsrequest){
 		GpsRequest input = gpsrequest.getValue();	
 		RateResponse output = new RateResponse();
 		
 		AuthRequest userInfo = input.getUserInfo();
 		long uid=input.getUid();
 		if(uid==innerAuthenticate(userInfo)){
 			double x = input.getLat();
 			double y = input.getLon();
 
 			GeolocationDao gdao = new GeolocationDao();
 			double distance = 0.002;
 			List<Geolocation> spots = gdao.findCloseByParkingLocation(x-distance, x+distance, y-distance, y+distance);
 			String ss = "";
 			for(Geolocation g: spots){
 					ss+="loc string " + g.getLocationIdentifier();
 					ParkingRateDao p = new ParkingRateDao();
 					ParkingRate pr;
 					//											this is main_lot	   input.getspot is 1412
 					try{
 						pr = p.getParkingRateByName(g.getLocationIdentifier(), input.getSpot());
 						ss+="  pr spaceid" + pr.getSpaceId();
 						ParkingSpaceDao psdao = new ParkingSpaceDao();
 						ParkingSpace pspace = null;
 						try{
 							pspace =psdao.getParkingSpaceBySpaceIdentifier(g.getLocationIdentifier(), input.getSpot());
 						}catch (Exception e){}
 						if(pspace!=null){
 							RateObject rate = new RateObject();
 							rate.setLat(g.getLatitude());
 							rate.setLon(g.getLongitude());
 							output.setResp("OK");
 							rate.setLocation(pspace.getSpaceName());
 							rate.setSpotId(pr.getSpaceId());
 							rate.setMinTime(pr.getMinParkMins());
 							rate.setMaxTime(pr.getMaxParkMins());
 							rate.setDefaultRate(pr.getParkingRateCents());
 							rate.setMinIncrement(pr.getTimeIncrementsMins());
 							output.setRateObject(rate);
 							return output;
 						}else{
 							output.setResp("SPACE_NULL" + pr.getSpaceId());
 							return output;
 						}
 					}catch (Exception e){
 						output.setResp("EXCEPTION GETTING PARKING RATE");
 						return output;
 					}
 				
 			}
 			output.setResp("NO_SPOTS");
 			return output;
 		}else{
 			output.setResp("BAD_AUTH");
 			return output;
 		}
 	}
 	/**
 	 * returns User_ID, or -1 if bad
 	 * */
 	private long innerAuthenticate(AuthRequest in){
 		UserDao userDb = new UserDao();
 		User user = null;
 		try{
 			user = userDb.getUserByEmail(in.getEmail());
 		}catch(RuntimeException e){
 		}
 		if(user!=null&&user.getPassword().equals(in.getPassword())){
 			return user.getUserID();
 		}else{
 			return -1;
 		}
 	}
 
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
 	public String sayPlainTextHello1() {
 		return "got RATE?";
 	}
 }
