 package au.com.pingmate.controller;
 
 import au.com.pingmate.domain.GameRequest;
 import au.com.pingmate.domain.PingPongGame;
 import au.com.pingmate.domain.PingPongPlayer;
 import au.com.pingmate.domain.PlayerStats;
 import au.com.pingmate.service.DefaultMatchService;
 import au.com.pingmate.service.GameService;
 import au.com.pingmate.service.PlayerService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 @Controller
 @RequestMapping("/game")
 public class GameController {
 
     @Autowired
     private PlayerService playerService;
 
     @Autowired
     private GameService gameService;
 
     @RequestMapping(value = "/add", method = RequestMethod.GET)
     public ModelAndView add() {
         ModelAndView modelAndView = new ModelAndView("game/add", "command", new GameRequest());
         modelAndView.addObject("players", playerService.listActivePlayers());
         return modelAndView;
     }
 
     @RequestMapping(value = "/add", method = RequestMethod.POST)
     public String addAction(@ModelAttribute("gameResult") GameRequest result) {
         PingPongPlayer player1 = playerService.findPlayer(result.getPlayer1());
         PingPongPlayer player2 = playerService.findPlayer(result.getPlayer2());
         PingPongGame game = new DefaultMatchService().createGame(
                 player1, player2, result.getPlayer1Score(), result.getPlayer2Score(), result.getPlayed());
         gameService.addGame(game);
         return "redirect:/player";
     }
 
     @RequestMapping(value = "/player/{id}/history")
     public ModelAndView listPlayerGames(@PathVariable int id) {
         List<PingPongGame> games = gameService.findGamesPlayedBy(id);
         ModelAndView modelAndView = new ModelAndView("game/game-history", "games", games);
         modelAndView.addObject("player", getPlayerFromGames(id, games));
         return modelAndView;
     }
 
     @RequestMapping(value = "/player/{id}/stats")
     public ModelAndView showStats(@PathVariable int id) {
         List<PingPongGame> games = gameService.findGamesPlayedBy(id);
         ModelAndView modelAndView = new ModelAndView("game/game-stats", "stats", new PlayerStats(games, id));
         modelAndView.addObject("player", getPlayerFromGames(id, games));
         return modelAndView;
     }
 
     @RequestMapping(value = "/player/{id}/chart")
     public ModelAndView showChart(@PathVariable int id) {
         return new ModelAndView("game/game-chart", "player", playerService.findPlayer(id));
     }
 
     @RequestMapping(value = "/player/{id}/json")
     public @ResponseBody List<PingPongGame> showGamesAsJson(@PathVariable int id) {
        return gameService.findGamesPlayedBy(id);
     }
 
     private PingPongPlayer getPlayerFromGames(int id, List<PingPongGame> games) {
         //To save a database call, get a player from the game list
         if (!games.isEmpty()) {
             PingPongGame aGame = games.get(0);
             return aGame.getWinner().getIdentifier() == id ? aGame.getWinner() : aGame.getLoser();
         }
 
         //no games played so you have to go to the database
         return playerService.findPlayer(id);
     }
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yy");
         dateFormat.setLenient(false);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
     }
 }
