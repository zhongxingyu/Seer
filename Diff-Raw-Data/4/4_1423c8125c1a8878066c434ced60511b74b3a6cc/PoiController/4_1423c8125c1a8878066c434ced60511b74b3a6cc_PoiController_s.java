 package de.christianzunker.mobilecitygate.controller;
 
 import java.net.SocketTimeoutException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.WebResource;
 
 import de.christianzunker.mobilecitygate.beans.Client;
 import de.christianzunker.mobilecitygate.beans.Config;
 import de.christianzunker.mobilecitygate.beans.Poi;
 import de.christianzunker.mobilecitygate.beans.PoiCategory;
 import de.christianzunker.mobilecitygate.beans.Profile;
 import de.christianzunker.mobilecitygate.beans.Route;
 import de.christianzunker.mobilecitygate.dao.ClientDao;
 import de.christianzunker.mobilecitygate.dao.MessageDao;
 import de.christianzunker.mobilecitygate.dao.PoiCategoryDao;
 import de.christianzunker.mobilecitygate.dao.PoiDao;
 import de.christianzunker.mobilecitygate.dao.ProfileDao;
 import de.christianzunker.mobilecitygate.dao.RouteDao;
 import de.christianzunker.mobilecitygate.utils.GoogleServices;
 
 @Controller
 public class PoiController { // NO_UCD
 
 	private static final Logger logger = Logger.getLogger(PoiController.class);
 	
 	@Autowired
 	private GoogleServices google;
 	
 	@Autowired
 	private PoiCategoryDao poiCatDao;
 	
 	@Autowired
 	private Config config;
 	
 	@Autowired
 	private ProfileDao profileDao;
 	
 	@Autowired
 	private PoiDao poiDao;
 	
 	@Autowired
 	private RouteDao routeDao;
 	
 	@Autowired
 	private ClientDao clientDao;
 	
 	@Autowired
 	private MessageDao messageDao;
 	
 	@Cacheable("pois")
 	@RequestMapping(value = "/{client}/{locale}/pois")
 	public String getPois(@PathVariable("client") String client, @PathVariable("locale") String locale, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 		logger.debug("entering method getPois");
 		Calendar cal = Calendar.getInstance();
 		long starttime = cal.getTimeInMillis();
 		long curtime = 0L;
 		logger.trace("starttime: " + starttime);
 		
 		//TODO do a more precise check on locale with table languages?
 		if (!locale.matches("[a-z]{2}") ||
 			!client.matches("[0-9a-zA-Z_-]*") ||
 			client.length() > 45) {
 			logger.error("Fehlerhafte Parameter!");
 			throw new Exception("Fehlerhafte Parameter!");
 		}
 
 		Client clientObj = clientDao.getClientByUrl(client);
 		HashMap<String, String> hashMessagesPoiOverview = messageDao.getMessagesByPageClientIdLocale("poi-overview", clientObj.getId(), locale);
 		HashMap<String, String> hashMessagesMap = messageDao.getMessagesByPageClientIdLocale("map", clientObj.getId(), locale);
 		
 		curtime = cal.getTimeInMillis();
 		logger.trace("PERF: after get messages took ms: " + (curtime - starttime));
         
 		//get shortened URL for easier Twitter usage
		String longUrl = "http://" + request.getServerName() + request.getContextPath() + "/" + clientObj.getUrl() + "/" + locale + "/";
 		clientObj.setShortUrl(google.getShortUrl(longUrl));
 		
 		curtime = cal.getTimeInMillis();
 		logger.trace("PERF: after get short url took ms: " + (curtime - starttime));
 		
 		List<PoiCategory> cats = poiCatDao.getActiveCategoriesByClientLocale(clientObj.getId(), locale);
         List<Profile> profiles = profileDao.getActiveProfilesByClientLocale(clientObj.getId(), locale);
 
         
         //List<Poi> pois = poiDao.getPoisNotInRoute();
         List<Poi> pois = poiDao.getPoisNotInRouteByClientLocale(clientObj.getId(), locale);
         List<Route> routes = routeDao.getRouteListByClientLocale(clientObj.getId(), locale);
 
         curtime = cal.getTimeInMillis();
         logger.trace("PERF: after get categories, profiles, routes and pois took ms: " + (curtime - starttime));
         
         for (Route route : routes) {
 			route.setPois(poiDao.getPoisByRouteLocale(route.getId(), locale));
 			if (logger.isDebugEnabled()) {
         		logger.debug("#route pois: " + route.getPois().size());
 			}
 		}
         
         curtime = cal.getTimeInMillis();
         logger.trace("PERF: after set pois for route took ms: " + (curtime - starttime));
         
         for (Profile profile : profiles) {
 			List<Integer> ids = poiDao.getPoiIdsByProfileByClientLocale(profile.getId(), clientObj.getId(), locale);
 			String stringIds = "";
 			for (Integer id : ids) {
 				stringIds += id.toString() + ",";				
 			}
 			//remove last ,
 			if (stringIds.length() > 0) {
 				stringIds = stringIds.substring(0, stringIds.length() - 1);
 			}
 			profile.setNonUsablePoiIds(stringIds);
 		}
         
         curtime = cal.getTimeInMillis();
         logger.trace("PERF: after associate pois to profiles took ms: " + (curtime - starttime));
         
         for (PoiCategory cat : cats) {
 			List<Integer> ids = poiDao.getPoiIdsByCategoryLocale(cat.getId(), locale);
 			String stringIds = "";
 			for (Integer id : ids) {
 				stringIds += id.toString() + ",";				
 			}
 			//remove last ,
 			if (stringIds.length() > 0) {
 				stringIds = stringIds.substring(0, stringIds.length() - 1);
 			}
 			cat.setPois(stringIds);
 		}
         
         curtime = cal.getTimeInMillis();
         logger.trace("PERF: after associate pois to categories took ms: " + (curtime - starttime));
         
         model.addAttribute("tilesServers", config.getTilesServers());
         model.addAttribute("profiles", profiles);
         model.addAttribute("poiCategories", cats);
         model.addAttribute("pois", pois);
         model.addAttribute("config", config);
         model.addAttribute("routes", routes);
         model.addAttribute("locale", locale);
         model.addAttribute("client", clientObj);
         model.addAttribute("messagesPoiOverview", hashMessagesPoiOverview);
         model.addAttribute("messagesMap", hashMessagesMap);
         
         curtime = cal.getTimeInMillis();
         logger.trace("PERF: after setting the model took ms: " + (curtime - starttime));
         
         logger.debug("leaving method getPois");
 		return "poi-map";
 	}
 	
 	/*
 	@Cacheable("urls")
 	public String getGoogleShortUrl(String longUrl) {
 		String shortUrl = longUrl;
 		String googleUrl = "https://www.googleapis.com/urlshortener/v1/url?" + config.getGoogleApiKey();
 		try {
 			com.sun.jersey.api.client.Client jsonClient = com.sun.jersey.api.client.Client.create();
 			int timeout = config.getGoogleTimeout();
 			if (!(timeout > 0)) {
 				// config not set, connect with a default timeout of 200ms
 				timeout = 200;
 			}
 			logger.trace("Google timeout: " + timeout + "ms");
 			jsonClient.setConnectTimeout(timeout);
 			WebResource webResource = jsonClient.resource(googleUrl);
 			String jsonRequest = "{ \"longUrl\": \"" + longUrl + "\" }";
 			logger.debug("jsonRequest: " + jsonRequest);
 			String jsonResponse = webResource.accept(
 			        MediaType.APPLICATION_JSON_TYPE).
 			        type(MediaType.APPLICATION_JSON_TYPE).
 			        post(String.class, jsonRequest);
 			logger.debug("jsonResponse: " + jsonResponse);
 			
 			JsonParser jsonParser = new JsonParser();
 			JsonObject jsonObj = (JsonObject)jsonParser.parse(jsonResponse);
 			if ( jsonObj != null && jsonObj.has("id")) {
 				shortUrl = jsonObj.get("id").toString();
 				shortUrl = shortUrl.replace("\"", "");
 			}
 		}
 		catch (ClientHandlerException ex) {
 			Exception rootCause = (Exception) ex.getCause();
 			if (rootCause.getClass().equals(SocketTimeoutException.class)) {
 				logger.error("Got connection timeout!");
 			}
 			logger.error("Couldn't connect to Google URL Shortener!", ex);
 		}
 		return shortUrl;
 	}
 	*/
 	
 	
 	@Cacheable("pois")
 	@RequestMapping(value = "/poi/{poiId}", headers="Accept=*/*", method=RequestMethod.GET)
 	public @ResponseBody Poi getPoiById(@PathVariable("poiId") int poiId) {
 		logger.debug("entering method getPoiById");
 		
         Poi poi = poiDao.getPoiById(poiId);
         try {
         	Route route = routeDao.getRouteByPoiId(poiId);
         	poi.setRoute(route.getName());
             poi.setRouteId(route.getId());
         }
         catch (EmptyResultDataAccessException ex) {
         	logger.info("no route assigned for poi " + poi.getName() + "(id: " + poi.getId() + ")");
         	poi.setRoute("no route assigned");
         }
         PoiCategory cat = poiCatDao.getCategoryByPoi(poiId);
         List<Profile> profiles = profileDao.getProfilesByPoi(poiId);
         poi.setIcon(cat.getIcon());
         poi.setPoiCategory(cat.getName());
         poi.setPoiCategoryId(cat.getId());
         List<String> profileNames = new Vector<String>();
         List<Integer> profileIds = new Vector<Integer>();
         for (Profile profile : profiles) {
         	logger.trace("profileName: " + profile.getName());
         	profileNames.add(profile.getName());
         	logger.trace("profileId: " + profile.getId());
         	profileIds.add(new Integer(profile.getId()));
 		}
         poi.setPoiProfileIds(profileIds);
         logger.trace("#profileNames: " + poi.getPoiProfileIds().size());
         poi.setPoiProfiles(profileNames);
         logger.trace("#profileNames: " + poi.getPoiProfiles().size());
         
         logger.debug("leaving method getPoiById");
 		return poi;
 	}
 	
 	@CacheEvict(value = "pois", allEntries=true)
 	@RequestMapping(value = "/poi/{poiId}", headers="Accept=application/json", method=RequestMethod.POST)
 	public @ResponseBody int setPoiById(@PathVariable("poiId") int poiId, @RequestBody Poi poi) {
 		logger.debug("entering method setPoiById");
 		
 		int rc = 0;
 		if (poiId > 0) {
 			rc = poiDao.updatePoiById(poiId, poi);
 			if (poi.getRouteId() > 0) {
 				// TODO: What if new assignment?
 				rc = poiDao.updatePoiRouteById(poiId, poi.getRouteId());
 			}
 			else {
 				rc = poiDao.deletePoiRouteById(poiId);
 			}
 			logger.debug("poi.getPoiProfileIds().size(): " + poi.getPoiProfileIds().size());
 			if (poi.getPoiProfileIds().size() > 0) {
 				rc = poiDao.updatePoiProfilesById(poiId, poi);
 			}
 			else {
 				rc = poiDao.deletePoiProfilesById(poiId);
 			}
 			if (poi.getPoiCategoryId() > 0) {
 				// TODO: What if new assignment?
 				rc = poiDao.updatePoiCategoryById(poiId, poi.getPoiCategoryId());
 			}
 			else {
 				rc = poiDao.deletePoiCategoryById(poiId);
 			}
 				
 		}
 		else {
 			// TODO: good design? split it in multiple methods?
 			rc = poiDao.createPoi(poi);
 			poiId = rc;
 			rc = poiDao.createPoiRoute(poiId, poi);
 		}
         logger.debug("leaving method setPoiById");
         return rc;
 	}
 	
 	@CacheEvict(value = "pois", allEntries=true)
 	@RequestMapping(value = "/poi/{poiId}", method=RequestMethod.DELETE)
 	public @ResponseBody int deletePoiById(@PathVariable("poiId") int poiId) {
 		logger.debug("entering method deletePoiById");
 		
         int rc = poiDao.deletePoiById(poiId);
         
         logger.debug("leaving method deletePoiById");
         return rc;
 	}
 	
 	@RequestMapping(value = "/pois/{routeId}", headers="Accept=*/*", method=RequestMethod.GET)
 	// TODO is it allowed to throw exceptions in this method or is there a different way because of REST + JSON?
 	public @ResponseBody List<Poi> getPoisByRouteId(@PathVariable("routeId") int routeId) throws Exception {
 		logger.debug("entering method getPois");
 		
 		List<Poi> pois = poiDao.getPoisByRoute(routeId);
         
         logger.debug("leaving method getPois");
 		return pois;
 	}
 	
 	@RequestMapping(value = "/pois/{clientid}/{locale}", headers="Accept=*/*", method=RequestMethod.GET)
 	// TODO is it allowed to throw exceptions in this method or is there a different way because of REST + JSON?
 	public @ResponseBody List<Poi> getUnassignedPois(@PathVariable("clientid") int clientId, @PathVariable("locale") String locale) throws Exception {
 		logger.debug("entering method getUnassignedPois");
 		
 		if (!locale.matches("[a-z]{2}")) {
 			logger.error("Fehlerhafte Parameter!");
 			throw new Exception("Fehlerhafte Parameter!");
 		}
 		
 		List<Poi> pois = poiDao.getPoisNotInRouteByClientLocale(clientId, locale);
         
         logger.debug("leaving method getUnassignedPois");
 		return pois;
 	}
 	
 	@RequestMapping(value = "/pois", method=RequestMethod.POST)
 	// TODO is it allowed to throw exceptions in this method or is there a different way because of REST + JSON?
 	public @ResponseBody List<Poi> getPois(@ModelAttribute Integer[] poiIds) throws Exception {
 		logger.debug("entering method getPois");
 		
 		logger.debug("poiIds.toString(): " + poiIds.toString());
 		List<Poi> pois = poiDao.getPoisByIds(poiIds.toString());
         
         logger.debug("leaving method getPois");
 		return pois;
 	}
 	
 	@CacheEvict(value = "pois", allEntries=true)
 	@RequestMapping(value = "/poi/publish", method=RequestMethod.POST)
 	// TODO is it allowed to throw exceptions in this method or is there a different way because of REST + JSON?
 	public @ResponseBody int publishAllPois() throws Exception {
 		logger.debug("entering method publishAllPois");
 		
 		// TODO: Publish routes by client and locale depending on the logged in user and his rights
 		int rc = poiDao.publishAllPois();
         
         logger.debug("leaving method publishAllPois");
 		return rc;
 	}
 	
 	@CacheEvict(value = "pois", allEntries=true)
 	@RequestMapping(value = "/poi/publish/{poiId}", method=RequestMethod.POST)
 	public @ResponseBody int publishPoiById(@PathVariable("poiId") int poiId) {
 		logger.debug("entering method publishPoiById");
 		
         int rc = poiDao.publishPoiById(poiId);
         
         logger.debug("leaving method publishPoiById");
         return rc;
 	}
 	
 	@ExceptionHandler(Exception.class)
 	public String handleException(Exception ex, HttpServletRequest request) {
 		logger.error("Error in " + this.getClass(), ex);
 		return "general-error";
 	}
 }
