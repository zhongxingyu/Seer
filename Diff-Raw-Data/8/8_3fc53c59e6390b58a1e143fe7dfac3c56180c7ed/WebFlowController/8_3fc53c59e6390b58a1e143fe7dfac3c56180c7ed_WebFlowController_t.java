 package pl.edu.agh.to1.dice.gameControllers.gameplay;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import pl.edu.agh.to1.dice.logic.figures.IFigureManager;
 import pl.edu.agh.to1.dice.logic.players.Player;
 import pl.edu.agh.to1.dice.logic.players.UserFactory;
 import pl.edu.agh.to1.dice.playermodel.UserModel;
 import pl.edu.agh.to1.dice.statistics.StatisticsModel.GlobalStatistics;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Controller
 @Scope("session")
 public class WebFlowController {
     @Autowired
     private IFigureManager figureManager;
 
     @Autowired
     private ScoreController scoreController;
 
     private Integer roundsPlayed = 0;
     private int currentPlayerId = 0;
 
     private List<Player> players;
 
     public void init() {
         this.players = Arrays.asList((Player) UserFactory.newInstance(new UserModel("Player1", new GlobalStatistics(0, 0, 0)))
                 , (Player) UserFactory.newInstance(new UserModel("Player2", new GlobalStatistics(0,0,0))));
     }
 
     public Player getActivePlayer() {
         return players.get(currentPlayerId);
     }
 
     public List<Player> getPlayers() {
         return players;
     }
 
     public void setPlayers(List<Player> players) {
         this.players = players;
     }
 
    public int getPlayersCount() {
        return players.size();
    }

    public Integer getAmountOfPlayers() {
        return players.size()+1;
    }

     public Boolean mustChooseFigure() {
         //TODO
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public Boolean canRoll() {
         //TODO
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public Boolean hasFinished() {
         //TODO
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public Integer getRoundsPlayed() {
         return roundsPlayed;
     }
 
     public boolean isPlayerRound(Player player) {
         return player.getName().
                 equals(getActivePlayer().getName());
     }
 
     public void reset() {
         roundsPlayed = 0;
     }
 
 }
