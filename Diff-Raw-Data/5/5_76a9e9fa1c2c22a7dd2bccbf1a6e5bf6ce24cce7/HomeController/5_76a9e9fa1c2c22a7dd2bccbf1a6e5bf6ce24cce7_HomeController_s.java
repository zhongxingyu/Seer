 package edu.wm.something;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
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
 	@RequestMapping(value="/players/location/{playerId}/{newLat}/{newLng}",method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setlocation(@PathVariable("playerId") String playerId,@PathVariable("newLat") long newLat,@PathVariable("newLng") long newLng)
 	{
 		GPSLocation location = new GPSLocation();
 		location.setLat(newLat);
 		location.setLng(newLng);
 		logger.info("moving player, in home controller");
 		try {
 			gameService.updatePosition(gameService.getPlayerByIDStr(playerId),location);
 		} catch (NoPlayerFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return jsonResponse;
 	}
 	
 	@RequestMapping(value="/players/add/{playerName}/{isWerewolf}/{lat}/{lng}",method=RequestMethod.POST)
 	public @ResponseBody void addPlayer(@PathVariable("playerName") String newPlayerId, @PathVariable("isWerewolf") Boolean isWerewolf,@PathVariable("lat") long lat,@PathVariable("lng") long lng) 
 	{
 		Random random = new Random();
 		Player p = new Player();
 		p.setId(newPlayerId);
 		p.setDead(false);
 		p.setWerewolf(isWerewolf);
 		p.setLat(lat);
 		p.setLng(lng);
 		p.setUserID(random.nextInt());
 		p.setPicture("none");
 		logger.info("Started to add player, in home controller now");
 		playerService.addplayer(p);
 		
 	}
 	
 	
 
 	@RequestMapping(value = "/players/kill/{killerId}/{victimId}", method=RequestMethod.POST)
 	public @ResponseBody boolean killPlayerById(@PathVariable("killerId") String killerIdStr, @PathVariable("victimId") String victimIdStr) throws NoPlayerFoundException, NoPlayersException
 	{
 		Player p = new Player();
 		System.out.println("About to kill!");
 		logger.info("killerId is:"+killerIdStr);
 		logger.info("victimId is:"+victimIdStr);
 		Player killer = gameService.getPlayerByIDStr(killerIdStr);
 		Player victim = gameService.getPlayerByIDStr(victimIdStr);
 		if (gameService.canKill(killer,victim)){
 			gameService.Kill(victim);
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	
 	@RequestMapping(value = "/players/vote/{voterId}/{voteId}", method=RequestMethod.POST)	
	public @ResponseBody void voteOnPlayer(@PathVariable int voterId, @PathVariable int voteId) throws NoPlayerFoundException {
 		JsonResponse response = new JsonResponse();
 		logger.info("voter is:"+voterId);;
		gameService.voteOnPlayer(gameService.getPlayerByID(voteId));
 		//return response;
     }
 	
 	@RequestMapping(value = "/players/alive/{ownerId}", method = RequestMethod.GET)
 	public  @ResponseBody Player getPlayerById(@PathVariable int ownerId) throws NoPlayerFoundException
 	{
 		Player players = gameService.getPlayerByID(ownerId);
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
 	
 	@RequestMapping(value ="/players/isnight", method =RequestMethod.GET)
 	public @ResponseBody boolean isNight(){
 		if (gameService.isNight()){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	@RequestMapping(value ="/admin/setnight", method =RequestMethod.POST)
 	public @ResponseBody void setNight(){
 		gameService.setNight(true);
 	}
 	
 	@RequestMapping(value ="/admin/setday", method =RequestMethod.POST)
 	public @ResponseBody void setDay(){
 		gameService.setNight(false);
 	}
 	
 }
