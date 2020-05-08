 package aigilas.net;
 
 import aigilas.Config;
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
 import sps.bridge.Command;
 import sps.bridge.Commands;
 import sps.core.RNG;
 import sps.io.CommandState;
 import sps.io.Input;
 
 public class LocalClient extends IClient {
     private boolean _isGameStarting;
     private float _turnTimer = 0;
     private final CommandState state = new CommandState();
 
     public LocalClient() {
 
     }
 
     public boolean isGameStarting() {
         return _isGameStarting;
     }
 
     public boolean isConnected() {
         return true;
     }
 
     public boolean nextTurn() {
         update();
         if (_turnTimer >= Config.get().turnTime) {
             _turnTimer = 0;
             return true;
         }
         return false;
     }
 
     public int getFirstPlayerIndex() {
         return 0;
     }
 
     public boolean isActive(Command command, int playerIndex) {
         return state.isActive(playerIndex, command);
     }
 
     public void setState(Command command, int playerIndex, boolean isActive) {
         state.setState(playerIndex, command, isActive);
     }
 
 
     public int getPlayerCount() {
        if (Controllers.getControllers().size > 0) {
            return Controllers.getControllers().size;
        }
        return 1;
     }
 
     public void startGame() {
         RNG.seed((int) System.currentTimeMillis());
         _isGameStarting = true;
     }
 
     public void update() {
         _turnTimer += Gdx.graphics.getDeltaTime();
     }
 
     public void close() {
     }
 
     public void dungeonHasLoaded() {
 
     }
 
     public void heartBeat() {
     }
 
     public void prepareForNextTurn() {
     }
 
     public void pollLocalState() {
         for (int ii = 0; ii < Client.get().getPlayerCount(); ii++) {
             for (Command command : Commands.values()) {
                 setState(command, ii, Input.detectState(command, ii));
             }
         }
     }
 }
