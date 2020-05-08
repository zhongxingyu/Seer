 package edu.wm.something;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.util.JSONPObject;
 import org.json.simple.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import Exceptions.NoPlayerFoundException;
 import Exceptions.NoPlayersException;
 
 //import werewolf.dao.IPlayerDAO;
 
 import edu.wm.service.GameService;
 import edu.wm.service.PlayerService;
 import edu.wm.something.domain.GPSLocation;
 import edu.wm.something.domain.Player;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	int id;
 	JsonResponse jsonResponse = new JsonResponse();
 	Player genericPlayer;
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	//@Autowired private IPlayerDAO playerDao;
 	@Autowired private GameService gameService;
 	@Autowired private PlayerService playerService;
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		logger.info("Welcome home! The client locale is {}.", locale);
 		
 		Date date = new Date();
 		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
 		
 		String formattedDate = dateFormat.format(date);
 		
 		model.addAttribute("serverTime", formattedDate );
 		
 		return "home";
 	}
 	
 	@RequestMapping(value = "/players/alive", method = RequestMethod.GET)
 	public  @ResponseBody List<Player> getAllAlive() throws NoPlayersException
 	{
 		logger.info("In players/alive!");
 		List<Player> players = gameService.getAllAlive();
 		return players;
 	}
 	@RequestMapping(value="/players/location",method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setlocation(@RequestParam(value="playerId",required=true) String playerId,@RequestParam(value="lat",required=true) Long lat,@RequestParam(value="lng",required=true) Long lng)
 	{
 		GPSLocation location = new GPSLocation();
 		location.setLat(lat);
 		location.setLng(lng);
 		logger.info("moving player, in home controller");
 		logger.info("lat for moving player is: "+lat);
 		logger.info("lng for moving player is: "+lng);
 		try {
 			gameService.updatePosition(gameService.getPlayerByIDStr(playerId),location);
 		} catch (NoPlayerFoundException e) {
 			e.printStackTrace();
 		}
 		return jsonResponse;
 	}
 	
 	@RequestMapping(value="/players/add",method=RequestMethod.POST)
 	public @ResponseBody void addPlayer(@RequestParam(value="playerId",required=true) String playerId,@RequestParam(value="lat",required=true) Long lat,@RequestParam(value="lng",required=true) Long lng,@RequestParam(value="isWerewolf",required=true) boolean isWerewolf)
 	{
 		Random random = new Random();
 		Player p = new Player();
 		p.setId(playerId);
 		p.setDead(false);
 		p.setWerewolf(isWerewolf);
 		p.setLat(lat);
 		p.setLng(lng);
 		p.setUserID(random.nextInt());
 		p.setPicture("none");
 		logger.info("Started to add player, in home controller now");
 		playerService.addplayer(p);
 		
 	}
 	
 	
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/players/kill", method=RequestMethod.POST)
 	public @ResponseBody JSONObject killPlayerById(@RequestParam(value="killerId",required=true) String killerIdStr,@RequestParam(value="victimId",required=true) String victimIdStr) throws NoPlayerFoundException, NoPlayersException
 	{
 		System.out.println("About to kill!");
 		logger.info("killerId is:"+killerIdStr);
 		logger.info("victimId is:"+victimIdStr);
 		Player killer = gameService.getPlayerByIDStr(killerIdStr);
 		Player victim = gameService.getPlayerByIDStr(victimIdStr);
 	
 		if (gameService.canKill(killer,victim)){
 			gameService.Kill(victim);
 			JSONObject json = new JSONObject();
 			json.put("isDead", true);
 			return json;
 		}
 		else{
 			gameService.Kill(victim);
 			JSONObject json = new JSONObject();
 			json.put("isDead", false);
 			return json;
 		}
 	}
 
 	
 	@RequestMapping(value = "/players/vote", method=RequestMethod.POST)	
 	public @ResponseBody void voteOnPlayer(@RequestParam(value="voterId",required=true) String voterId,@RequestParam(value="voteId",required=true) String voteId) throws NoPlayerFoundException {
 		JsonResponse response = new JsonResponse();
 		logger.info("voter is:"+voterId);
 		gameService.voteOnPlayer(gameService.getPlayerByIDStr(voteId));
 		//return response;
     }
 	
 	@RequestMapping(value = "/players/id", method = RequestMethod.GET)
 	public  @ResponseBody Player getPlayerById(@RequestParam(value="ownerId",required=true)String ownerId) throws NoPlayerFoundException
 	{
 		Player players = gameService.getPlayerByIDStr(ownerId);
 		return players;
 	}
 	
 	@RequestMapping(value = "/players/alive/{ownerId}/pic", method = RequestMethod.GET)
 	public  @ResponseBody Player getPicById(@PathVariable int ownerId) throws NoPlayerFoundException
 	{
 		Player player = gameService.getPicByID(ownerId);
 		return player;
 		
 	}
 	
 	@RequestMapping(value ="/admin/restartGame", method =RequestMethod.POST)
 	public @ResponseBody void restartGame(){
 		try {
 			gameService.restartGame();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value ="/players/isnight", method =RequestMethod.GET)
 	public @ResponseBody JSONObject isNight(){
 		if (gameService.isNight()){
 			JSONObject json = new JSONObject();
 			json.put("isNight", true);
 			return json;
 		}
 		else{
 			JSONObject json = new JSONObject();
 			json.put("isNight", false);
 			return json;
 		}
 	}
 	
 	@RequestMapping(value ="/admin/setnight", method =RequestMethod.POST)
 	public @ResponseBody void setNight(){
 		gameService.setNight(true);
 	}
 	
	@SuppressWarnings("unchecked")
 	@RequestMapping(value ="/users/login",produces = "application/json", method =RequestMethod.POST)
	public @ResponseBody JSONObject logint(@RequestBody String username,@RequestBody String password,@RequestBody double lat,@RequestBody  double lng){
 		Player p;
 		Random random = new Random();
 		Random randomWerewolf = new Random(4);
 		boolean isWerewolf = false;
 		int isWerewolfSource = randomWerewolf.nextInt();
 		if (isWerewolfSource == 3){
 			isWerewolf = true;
 		}
 		try {
 			p = gameService.getPlayerByIDStr(username);
 			JSONObject json = new JSONObject();
 			json.put("addedUser", false);
 			return json;//Already logged in
 		} catch (NoPlayerFoundException e) {
 			p = new Player(username, false, lat, lng, random.nextInt(), isWerewolf,0);
 			logger.info("Started to add player, in home controller now");
 			playerService.addplayer(p);
 			JSONObject json = new JSONObject();
 			json.put("addedUser", true);
 			return json;//added player sucessfully
 		}
 
 	}
 	
 	@RequestMapping(value ="/admin/setday", method =RequestMethod.POST)
 	public @ResponseBody void setDay(){
 		gameService.setNight(false);
 	}
 	
 	@RequestMapping(value ="/users/register", method =RequestMethod.POST)
 	public @ResponseBody void register(){
 		//TODO: Add a method for adding a user!
 	}
 	
 }
