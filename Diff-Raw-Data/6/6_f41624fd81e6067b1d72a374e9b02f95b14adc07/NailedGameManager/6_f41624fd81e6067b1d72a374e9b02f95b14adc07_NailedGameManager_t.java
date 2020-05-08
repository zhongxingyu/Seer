 package jk_5.nailed.map.game;
 
 import jk_5.nailed.api.map.GameManager;
 import jk_5.nailed.api.map.Map;
 import jk_5.nailed.api.map.PossibleWinner;
 import jk_5.nailed.api.map.scoreboard.DisplayType;
 import jk_5.nailed.map.NailedMap;
 import jk_5.nailed.map.stat.StatTypeManager;
 import jk_5.nailed.map.stat.types.StatTypeGameHasWinner;
 import jk_5.nailed.map.stat.types.StatTypeGameloopRunning;
 import jk_5.nailed.map.stat.types.StatTypeGameloopStopped;
 import jk_5.nailed.map.stat.types.StatTypeIsWinner;
 import jk_5.nailed.network.NailedNetworkHandler;
 import jk_5.nailed.network.NailedPacket;
 import lombok.Getter;
 import lombok.RequiredArgsConstructor;
 import lombok.Setter;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 @RequiredArgsConstructor
 public class NailedGameManager implements GameManager {
 
     private final Map map;
     @Getter @Setter private boolean watchUnready = false;
     @Getter @Setter private boolean winnerInterrupt = false;
     @Getter private boolean gameRunning;
     @Getter private PossibleWinner winner = null;
 
     @Override
     public void setCountdownMessage(String message){
         NailedNetworkHandler.sendPacketToAllPlayersInDimension(new NailedPacket.TimeUpdate(true, message), this.map.getID());
     }
 
     @Override
     public void setWinner(PossibleWinner winner){
         if(!this.gameRunning || this.winner != null) return;
         this.winner = winner;
        if(this.winnerInterrupt){
            this.gameRunning = false;
            this.stopGame();
        }
         StatTypeManager.instance().getStatType(StatTypeGameHasWinner.class).onWin(this.map);
         StatTypeManager.instance().getStatType(StatTypeIsWinner.class).onWinnerSet(this.map, winner);
         this.map.broadcastNotification("Winner is " + winner.getWinnerColoredName());
     }
 
     public void startGame(){
         ((NailedMap) this.map).getMachine().queueEvent("game_start");
     }
 
     public void stopGame(){
         ((NailedMap) this.map).getMachine().queueEvent("game_stop");
     }
 
     @Override
     public void onStarted(){
         this.gameRunning = true;
         StatTypeManager.instance().getStatType(StatTypeGameloopRunning.class).onStart(this.map);
     }
 
     @Override
     public void onStopped(boolean finished){
         this.gameRunning = false;
         StatTypeManager.instance().getStatType(StatTypeGameloopStopped.class).onEnd(this.map);
         StatTypeManager.instance().getStatType(StatTypeGameloopRunning.class).onEnd(this.map);
 
         //Reset some stuff
         this.setCountdownMessage("");
         this.map.getScoreboardManager().setDisplay(DisplayType.BELOW_NAME, null);
         this.map.getScoreboardManager().setDisplay(DisplayType.SIDEBAR, null);
         this.map.getScoreboardManager().setDisplay(DisplayType.BELOW_NAME, null);
     }
 }
