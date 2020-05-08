 package org.netvogue.server.webmvc.controllers;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.Size;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.neo4japi.common.NetworkStatus;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.NetworkService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.domain.ContactInfo;
 import org.netvogue.server.webmvc.domain.ImageURLsResponse;
 import org.netvogue.server.webmvc.domain.JsonResponse;
 import org.netvogue.server.webmvc.domain.Network;
 import org.netvogue.server.webmvc.domain.Networks;
 import org.netvogue.server.webmvc.domain.Notification;
 import org.netvogue.server.webmvc.pusher.PusherChannel;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class NetworkController {
 
 	@Autowired NetvogueUserDetailsService userDetailsService;
 	@Autowired UserService 			userService;
 	@Autowired NetworkService		networkService;
 	@Autowired ConversionService	conversionService;
 	@Autowired UploadManager 		uploadManager;
 	
 	@RequestMapping(value={"network/getnetworks", "network/getnetworks/{profileid}"}, method=RequestMethod.GET)
	public @ResponseBody Networks GetNetworks(@ModelAttribute("profileid") String profileid,
											  @RequestParam(value="onlyconfirmed", required=false) boolean onlyconfirmed) {
 		System.out.println("Get Networks: id is:" + profileid);
 		User user = userDetailsService.getUserFromSession();
 		String loggedinuser = user.getUsername();
 		Networks response = new Networks();
 		
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null) {
 				return response;
 			}
 		}
 		response.setName(user.getName());
 		response.setIsbrand(USER_TYPE.BRAND == user.getUserType()?true:false);
 		response.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		Set<Network> networksTemp = new LinkedHashSet<Network>();
 		
 		String username = user.getUsername();
		Iterable<org.netvogue.server.neo4japi.domain.Network> dbNetworks = 
									networkService.getNetworks(user.getUsername(), onlyconfirmed);
 		if(null == dbNetworks) {
 			System.out.println("No networks found: ");
 			return response;
 		}
 		
 		Iterator<org.netvogue.server.neo4japi.domain.Network> first = dbNetworks.iterator();
 		while ( first.hasNext() ){
 			org.netvogue.server.neo4japi.domain.Network dbNetwork = first.next() ;
 			if(
 				(dbNetwork.getStatus() != NetworkStatus.CONFIRMED && !loggedinuser.equals(username))	
 			 ||	(dbNetwork.getStatus() == NetworkStatus.PENDING && loggedinuser.equals(dbNetwork.getRequestBy().getUsername()))
 			 || (dbNetwork.getStatus() == NetworkStatus.BLOCK && !loggedinuser.equals(dbNetwork.getBreakupby()))
 			  )	 {
 				System.out.println("Ignoring this" + dbNetwork.getStatus().toString());
 				System.out.println("Loggedinuser" + loggedinuser);
 				System.out.println("blocked by" + dbNetwork.getBreakupby());
 				continue;
 			}
 			Network newNetwork = conversionService.convert(dbNetwork, Network.class);
 			String thumbpic = "";
 			
 			User networkUser = dbNetwork.getRequestBy();
 			if(username.equals(networkUser.getUsername())) {
 				networkUser = dbNetwork.getRequestTo();
 			}
 			newNetwork.setProfileid(networkUser.getUsername());
 			newNetwork.setCity(networkUser.getCity());
 			newNetwork.setCountry(networkUser.getCountry());
 			newNetwork.setUsertype(networkUser.getUserType() == USER_TYPE.BRAND? "BRAND": "BOUTIQUE");
 			newNetwork.setName(networkUser.getName());
 			thumbpic = networkUser.getProfilePicLink();
 			
 			if(null != thumbpic) {
 				thumbpic = uploadManager.getQueryString(thumbpic, ImageType.PROFILE_PIC, Size.PThumb);
 			}
 			newNetwork.setThumbnail_url(thumbpic);
 			networksTemp.add(newNetwork);
 		}		
 		
 		response.setNetworks(networksTemp);
 		
 		//Set Contact info as well
 		//Get ContactInfo
 		ContactInfo contactInfo = new ContactInfo();
 		contactInfo.setAddress(user.getAddress());
 		contactInfo.setCity(user.getCity());
 		contactInfo.setState(user.getState());
 		contactInfo.setCountry(user.getCountry());
 		contactInfo.setZip(String.valueOf(user.getZipCode()));
 		contactInfo.setEmail(user.getEmail());
 		contactInfo.setLandline1(String.valueOf(user.getTelephoneNo1()));
 		contactInfo.setLandline2(String.valueOf(user.getTelephoneNo2()));
 		contactInfo.setMobile(String.valueOf(user.getMobileNo()));
 		contactInfo.setWebsite(user.getWebsite());
 		contactInfo.setYearest(user.getYearofEst());
 		
 		response.setContactinfo(contactInfo);
 		System.out.println("Sent Networks:" + response.getNetworks().size());
 		return response;
 	}
 	
 	@RequestMapping(value="network/createnetwork", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse createNetwork(@RequestBody String profileid) {
 		System.out.println("Create Network: id is:" + profileid);
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		if(profileid.isEmpty()) {
 			response.setError("Username is empty");
 			return response;
 		}
 		
 		User user = userDetailsService.getUserFromSession();
 		org.netvogue.server.neo4japi.domain.Notification newNotification =  
 				new org.netvogue.server.neo4japi.domain.Notification(user);
 		if(ResultStatus.SUCCESS == networkService.CreateNetwork(user, profileid, newNotification, error)) {
 			//Add notification to pubsub node here //Azeez
 			//Make this call asynchronous
 			ObjectMapper mapper = new ObjectMapper();
 			Notification notification = conversionService.convert(newNotification, Notification.class);
 			PusherChannel pusher= new PusherChannel(profileid);
 			try{
 				String notificationToSent = mapper.writeValueAsString(notification);
 				pusher.pushEvent("notification", notificationToSent);
 			} catch (Exception e) {
 				System.out.println("Error in pusher" + error);
 				return response;
 			}
 			response.setStatus(true);
 		}
 		else
 			response.setError(error);
 		
 		System.out.println("Created Network" + error);
 		return response;
 	}
 	
 	@RequestMapping(value="network/confirmnetwork", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse confirmNetwork(@RequestBody String profileid) {
 		System.out.println("Confirm Network: id is:" + profileid);
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		if(profileid.isEmpty()) {
 			response.setError("Username is empty");
 			return response;
 		}
 		
 		User user = userDetailsService.getUserFromSession();
 		org.netvogue.server.neo4japi.domain.Notification newNotification = 
 				new org.netvogue.server.neo4japi.domain.Notification(user, NetworkStatus.CONFIRMED);;
 		if(ResultStatus.SUCCESS == networkService.ConfirmNetwork(user, profileid, newNotification, error)) {
 			//Add notification to pubsub node here //Azeez
 			//Make this call asynchronous
 			ObjectMapper mapper = new ObjectMapper();
 			Notification notification = conversionService.convert(newNotification, Notification.class);
 			PusherChannel pusher= new PusherChannel(profileid);
 			try{
 				String notificationToSent = mapper.writeValueAsString(notification);
 				System.out.println("notifi in pusher" + notificationToSent);
 				System.out.println("ori notifi in pusher" + notification.getName());
 				pusher.pushEvent("notification", notificationToSent);
 			} catch (Exception e) {
 				System.out.println("Error in pusher" + e.toString());
 				return response;
 			}
 			response.setStatus(true);
 			//Add notification to pubsub node here //Azeez 
 		}
 		else
 			response.setError(error);
 		
 		System.out.println("Confirmed Network");
 		return response;
 	}
 	
 	@RequestMapping(value="network/deletenetwork", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse deleteNetwork(@RequestBody String profileid) {
 		System.out.println("Delete Network: id is:" + profileid);
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		if(profileid.isEmpty()) {
 			response.setError("Username is empty");
 			return response;
 		}
 		
 		User user = userDetailsService.getUserFromSession();
 		if(ResultStatus.SUCCESS == networkService.DeleteNetwork(user.getUsername(), profileid, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error);
 		
 		System.out.println("Network deleted");
 		return response;
 	}
 	
 	@RequestMapping(value="network/blocknetwork", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse blockNetwork(@RequestBody String profileid) {
 		System.out.println("Block Network: id is:" + profileid);
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		if(profileid.isEmpty()) {
 			response.setError("Username is empty");
 			return response;
 		}
 		
 		User user = userDetailsService.getUserFromSession();
 		if(ResultStatus.SUCCESS == networkService.BlockNetwork(user.getUsername(), profileid, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error);
 		
 		System.out.println("Network blocked");
 		return response;
 	}
 	
 	@RequestMapping(value="network/unblocknetwork", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse unblockNetwork(@RequestBody String profileid) {
 		System.out.println("unblock Network: id is:" + profileid);
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		if(profileid.isEmpty()) {
 			response.setError("Username is empty");
 			return response;
 		}
 		
 		User user = userDetailsService.getUserFromSession();
 		if(ResultStatus.SUCCESS == networkService.UnblockNetwork(user.getUsername(), profileid, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error);
 		
 		System.out.println("unblocked Network");
 		return response;
 	}
 }
