 package com.comsysto.findparty.web;
 
 import java.util.List;
 
 import com.comsysto.findparty.ErrorModel;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.comsysto.findparty.Party;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletResponse;
 
 @Controller
 @RequestMapping("/parties")
 public class PartyControllerImpl implements PartyController {
 
     @Autowired
     public PartyService partyService;
 
     public static final Logger LOGGER = Logger.getLogger(PartyControllerImpl.class);
 
     public static final Double KILOMETER = 111.0d;
 
     /**
      * The Attribute that is used for the search for the start position
      */
     public static final String START = "start";
 
     /**
      * The Attribute that is used for the search for the user
      */
 
     // private static final String USER = "user";
 
     /*
      * @RequestMapping(value = "/get", method = RequestMethod.GET, produces =
      * "application/json") public
      * 
      * @ResponseBody List<com.comsysto.findparty.Party> getAll() throws
      * Exception { return
      * mongoOperations.findAll(com.comsysto.findparty.Party.class); }
      */
 
     /*
      * @RequestMapping(value = "/get/{lon1}/{lat1}/{lon2}/{lat2}/", method =
      * RequestMethod.GET, produces = "application/json") public
      * 
      * @ResponseBody List<com.comsysto.findparty.Party>
      * getByBounds(@PathVariable("lon1") Double lon1, @PathVariable("lat1")
      * Double lat1, @PathVariable("lon2") Double lon2, @PathVariable("lat2")
      * Double lat2) throws Exception { /* > box = [[40.73083, -73.99756],
      * [40.741404, -73.988135]] > db.places.find({"loc" : {"$within" : {"$box" :
      * box}}})
      */
     /*
      * Criteria criteria = new Criteria(START).within(new Box(new
      * com.comsysto.findparty.Point(lon1, lat1), new
      * com.comsysto.findparty.Point(lon2, lat2)));
      * List<com.comsysto.findparty.Party> tracks = mongoOperations.find(new
      * Query(criteria), com.comsysto.findparty.Party.class); return tracks; }
      */
 
     @ExceptionHandler(PartyServiceException.class)
     public ModelAndView handleIOException(PartyServiceException ex, HttpServletResponse response) {
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         LOGGER.warn("An error occurred in the service", ex);
         ErrorModel errorModel = new ErrorModel();
         errorModel.setThrowable(ex);
         return new ModelAndView("/error.jsp", "error", errorModel);
     }
 
     @ExceptionHandler(RuntimeException.class)
     public ModelAndView handleIOException(RuntimeException ex, HttpServletResponse response) {
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         LOGGER.warn("An error occurred in the service", ex);
         PartyServiceException partyServiceException = new PartyServiceException("An error occurred in the service", ex);
         ErrorModel errorModel = new ErrorModel();
         errorModel.setThrowable(partyServiceException);
         return new ModelAndView("/error.jsp", "error", errorModel);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.comsysto.findparty.web.IPartyController#searchParties(java.lang.Double
      * , java.lang.Double, java.lang.Double)
      */
     @RequestMapping(value = "/search/{lon}/{lat}/{maxdistance}", method = RequestMethod.GET, produces = "application/json")
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     List<Party> search(@PathVariable("lon") Double lon, @PathVariable("lat") Double lat, @PathVariable("maxdistance") Double maxdistance) {
         List<Party> parties = partyService.searchParties(lon, lat, maxdistance);
         return parties;
     }
 
 
     @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
     @ResponseBody
     public String create(@RequestBody Party party) {
         LOGGER.info("received party: " + party.getName());
         String partyId = partyService.createParty(party);
         return partyId;
     }
 
     @RequestMapping(value = "/{partyId}", method = RequestMethod.PUT, consumes = "application/json")
     @ResponseStatus(HttpStatus.OK)
     public void update(@RequestBody Party party) {
         partyService.update(party);            
     }
     
     @RequestMapping(value = "/{partyId}/subscriptions", method = RequestMethod.PUT, consumes = "application/json")
     @ResponseStatus(HttpStatus.OK)
     public void subscribe(@PathVariable String partyId, @RequestParam(value = "action", required = false) String action, @RequestBody String username) {
         
         if(action!=null && action.equals("cancel"))
             partyService.cancelParty(username, partyId);
         
         partyService.joinParty(username, partyId);
     }
     
     @RequestMapping(value = "/{partyId}", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public void show(@PathVariable("partyId") String partyId) {
         partyService.showDetails(partyId);
     }
     
     @RequestMapping(method = RequestMethod.GET)
     public @ResponseBody List<Party> findByUsername(@RequestParam(value = "user", required = true) String username) {
         return partyService.getAllParties(username);        
     }
     
     
     @RequestMapping(method = RequestMethod.PUT)
     @ResponseStatus(HttpStatus.OK)
     public void joinParty(@PathVariable("partyId") String partyId, @RequestBody String username) {
         LOGGER.info("received request to join user:" + username + " to party with id:" + partyId);
         partyService.joinParty(username, partyId);
     }
 
     @RequestMapping(method = RequestMethod.DELETE)
     @ResponseStatus(HttpStatus.OK)
     public void delete(@PathVariable String partyId) {
         partyService.delete(partyId);        
     }
     
     
     @RequestMapping(value="/echo/{input}", method = RequestMethod.GET, produces = "application/json")
     public @ResponseBody String echo(@PathVariable("input") String input) {
         return input;
     }
 
     /*
      * @RequestMapping(value = "/add", method = RequestMethod.POST, consumes =
      * "application/json")
      * 
      * @ResponseStatus(HttpStatus.OK) public void add(@RequestBody
      * com.comsysto.findparty.Party track) throws Exception {
      * mongoOperations.insert(track); }
      */
 
     /*
      * @RequestMapping(value = "/foruser", method = RequestMethod.GET)
      * 
      * @ResponseStatus(HttpStatus.OK) public
      * 
      * @ResponseBody List<com.comsysto.findparty.Party> tracksForUser() throws
      * Exception { com.comsysto.findparty.User principal =
      * (com.comsysto.findparty.User)
      * SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      * Criteria criteria = Criteria.where(USER).is(principal.getUsername());
      * List<com.comsysto.findparty.Party> tracks = mongoOperations.find(new
      * Query(criteria), com.comsysto.findparty.Party.class); return tracks; }
      */
 
     /*
      * @RequestMapping(value = "/upload", method = RequestMethod.POST)
      * 
      * @ResponseStatus(HttpStatus.OK) public void upload(@RequestParam("file")
      * MultipartFile multipartFile) throws Exception {
      * com.comsysto.findparty.User principal = (com.comsysto.findparty.User)
      * SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      * ObjectMapper mapper = new ObjectMapper(); com.comsysto.findparty.Party
      * track = mapper.readValue(multipartFile.getBytes(),
      * com.comsysto.findparty.Party.class);
      * track.setUser(principal.getUsername()); mongoOperations.insert(track); }
      */
 }
