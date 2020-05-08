 package aigilas.net;
 
 import aigilas.AigilasConfig;
 import aigilas.states.LoadingState;
 import aigilas.states.MainMenuState;
 import com.badlogic.gdx.Gdx;
 import sps.bridge.Command;
 import sps.bridge.Commands;
 import sps.bridge.Contexts;
 import sps.bridge.Sps;
 import sps.core.Logger;
 import sps.core.RNG;
 import sps.io.CommandState;
 import sps.io.Input;
 import sps.states.StateManager;
 
 import java.net.ConnectException;
 import java.net.Socket;
 
 public class LanClient extends IClient {
     private static final float _heartBeatWaitSeconds = .1f;
     int _playerCount = 0;
     // Client <-> Server
     private Message _message;
     private MessageHandler _comm;
     private float _heartBeat = _heartBeatWaitSeconds;
     // Client <-> Game
     private Integer _initialPlayerIndex;
     private boolean _isGameStarting;
     private boolean _dungeonHasLoaded = false;
     private boolean _isConnected = false;
     private CommandState state = new CommandState();
     private int connectAttemptLimit = AigilasConfig.get().lanConnectRetries;
 
     public LanClient() {
         connect();
     }
 
     private void connect() {
         Logger.info("Trying to connect to server, " + connectAttemptLimit-- + " tries remaining.");
         try {
             Socket server = new Socket(AigilasConfig.get().serverIp(), AigilasConfig.get().port());
             _comm = new MessageHandler(server);
             _comm.owner = "CLIENT";
             sendMessage(Message.createInit(0, 0));
             awaitReply(MessageTypes.Connect);
             handleResponse(_message);
         }
         catch (ConnectException cE) {
             try {
                 Thread.sleep(1000);
                 if (connectAttemptLimit > 0) {
                     connect();
                 }
             }
             catch (InterruptedException e) {
                 Logger.exception(e);
             }
         }
         catch (Exception e) {
             Logger.exception(e);
         }
     }
 
     // Client <-> Game communication
     public boolean isGameStarting() {
         return _isGameStarting;
     }
 
     public boolean isConnected() {
         return _isConnected;
     }
 
     public void heartBeat() {
         if (!_dungeonHasLoaded) {
             _heartBeat -= Gdx.graphics.getDeltaTime();
             if (_heartBeat <= 0) {
                 sendMessage(Message.createHeartBeat());
                 _heartBeat = _heartBeatWaitSeconds;
             }
         }
     }
 
     public void dungeonHasLoaded() {
         _dungeonHasLoaded = true;
     }
 
     public boolean nextTurn() {
         update();
         if (_message != null && _message.MessageType == MessageTypes.Sync_State) {
             RNG.seed(_message.RngSeed);
             _heartBeat = _heartBeatWaitSeconds;
             return true;
         }
         return false;
     }
 
     public int getFirstPlayerIndex() {
         return _initialPlayerIndex;
     }
 
     public void prepareForNextTurn() {
         if (_dungeonHasLoaded) {
             sendMessage(Message.createReadyForNextTurn());
         }
     }
 
     // Client <-> Server communication
     public boolean isActive(Command command, int playerIndex) {
         return state.isActive(playerIndex, command);
     }
 
     public void setState(Command command, int playerIndex, boolean isActive) {
         if (isActive != state.isActive(playerIndex, command)) {
             //Logger.info("MISMATCH: command:"+command.name()+", active:" +isActive);
             sendMessage(Message.createMovement(command, playerIndex, isActive));
         }
     }
 
     @Override
     public void pollLocalState() {
         //Logger.info(state.debug());
         for (Command command : Commands.values()) {
             setState(command, getFirstPlayerIndex(), Input.get().detectState(command, getFirstPlayerIndex()));
         }
     }
 
     public int getPlayerCount() {
         if (_playerCount == 0) {
             sendMessage(Message.createPlayerCount(0));
             awaitReply(MessageTypes.Player_Count);
             _playerCount = _message.PlayerCount;
         }
         return _playerCount;
     }
 
     public void startGame() {
         sendMessage(Message.create(MessageTypes.Start_Game));
     }
 
     private void sendMessage(Message contents) {
         contents.PlayerIndex = _initialPlayerIndex;
         contents.LocalPort = _comm.getLocalPort();
         _comm.sendOutboundMessage(contents);
     }
 
     // If the server doesn't reply at some point with the messageType you expect
     // Then the client will hang in an infinite loop.
     private void awaitReply(MessageTypes messageType) {
         while (true) {
             _message = _comm.readInboundMessage();
             if (_message != null) {
                 if (_message.MessageType == messageType) {
                     return;
                 }
                 else {
                     handleResponse(_message);
                 }
             }
         }
     }
 
     public void update() {
        if (_comm.isClosing()) {
             StateManager.loadState(new MainMenuState());
         }
         _message = _comm.readInboundMessage();
         if (_message != null) {
             handleResponse(_message);
         }
     }
 
     private void handleResponse(Message contents) {
         switch (contents.MessageType) {
             case Connect:
                 RNG.seed(contents.RngSeed);
                 _initialPlayerIndex = (int) contents.PlayerCount - 1;
                 _isConnected = true;
                 break;
             case Start_Game:
                 _isGameStarting = true;
                 _playerCount = contents.PlayerCount;
                 break;
             case Sync_State:
                 state.reset(contents.CommandState);
                 RNG.seed(contents.RngSeed);
                 //Setup the game if this is the first sync
                 if (contents.TurnCount == 0) {
                     for (int ii = 0; ii < Client.get().getPlayerCount(); ii++) {
                         Input.get().setContext(Contexts.get(Sps.Contexts.Free), ii);
                     }
                     StateManager.loadState(new LoadingState());
                 }
                 //Logger.info("CLIENT: Synced-> "+state.debug());
                //Logger.info("CLIENT: Synced to turnCount: " + contents.TurnCount + " new seed is " + contents.RngSeed + " with this many players " + _playerCount);
                 break;
             default:
                 break;
         }
     }
 
     public void close() {
         _comm.close();
     }
 }
