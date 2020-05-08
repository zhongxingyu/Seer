 package aigilas.net;
 
 import aigilas.AigilasConfig;
 import sps.core.Logger;
 import sps.io.CommandState;
 import sps.util.RealTime;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 public class Server extends Thread {
     private final static int seedBound = Integer.MAX_VALUE;
     private static Server server;
     private final CommandState state = new CommandState();
     private final List<Boolean> _readyCheckIn = new ArrayList<Boolean>();
     private final ClientManager clients;
     private boolean isRunning = true;
     private int _rngSeed = UUID.randomUUID().hashCode();
     private Message _message = Message.empty();
     private Integer _turnCount = 0;
     private float _turnTime = 0;
     private boolean gameHasStarted = false;
 
     private Server() {
         setName("Server");
         clients = new ClientManager();
     }
 
    public static void shutdown() {
        if (server != null) {
             server.close();
         }
     }
 
     public static void reset() {
         Logger.info("Starting the server");
         if (server != null) {
             server.close();
             try {
                 Thread.sleep(200);
             }
             catch (InterruptedException e) {
                 Logger.exception(e, false);
             }
         }
         server = new Server();
         server.start();
         try {
             Thread.sleep(200);
         }
         catch (InterruptedException e) {
             Logger.exception(e);
         }
     }
 
    public void close() {
         isRunning = false;
         clients.close();
     }
 
     public void run() {
 
         try {
             while (isRunning) {
                 Thread.sleep(1);
                 pollForNewMessages();
                 broadCastGameState();
                 RealTime.get().update();
             }
         }
         catch (InterruptedException e) {
             //Should only happen when we explicitly dispose of the server instance
            Logger.exception(e, false);
         }
         Logger.info("Server thread is closing");
     }
 
     private void pollForNewMessages() {
         _message = clients.readMessage();
         if (_message != null) {
             switch (_message.MessageType) {
                 case Connect:
                     sendMessage(Message.createInit(clients.size(), _rngSeed), _message.LocalPort);
                     _readyCheckIn.add(false);
 
                     Logger.info("SERVER: A player has connected");
                     _turnCount = 0;
                     break;
                 case Check_State:
                     _message.IsActive = state.isActive(_message.PlayerIndex, _message.Command);
                     sendMessage(_message, _message.LocalPort);
                     break;
 
                 case Movement:
                     state.setState(_message.PlayerIndex, _message.Command, _message.IsActive);
                     break;
 
                 case Start_Game:
                     _message.setPlayerCount(clients.size());
                     gameHasStarted = true;
                     Logger.info("SERVER: Game has been started");
                     announce(_message);
                     break;
 
                 case Player_Count:
                     sendMessage(Message.createPlayerCount(clients.size()), _message.LocalPort);
                     break;
 
                 case Ready_For_Next_Turn:
                     _readyCheckIn.set(_message.PlayerIndex, true);
                     break;
 
                 case Heart_Beat:
                    if (_readyCheckIn.size() <= _message.PlayerIndex) {
                        _readyCheckIn.add(true);
                    }
                    else {
                        _readyCheckIn.set(_message.PlayerIndex, true);
                    }
                     break;
                 default:
                     break;
             }
         }
     }
 
     private void broadCastGameState() {
         if (gameHasStarted) {
             _turnTime += RealTime.get().delta();
             if (_turnTime >= AigilasConfig.get().turnTime) {
                 int readyCount = 0;
                 for (boolean checkedIn : _readyCheckIn) {
                     readyCount += checkedIn ? 1 : 0;
                 }
                 if (readyCount >= clients.size()) {
                     _turnTime = 0;
                     _rngSeed = UUID.randomUUID().hashCode();
 
                     //Logger.info("SERVER: Announce -> "+state.debug());
                     //System.out.println("Announcing turn: " + _turnCount);
                     announce(Message.createPlayerState(state, _turnCount++, _rngSeed));
                     for (int ii = 0; ii < _readyCheckIn.size(); ii++) {
                         _readyCheckIn.set(ii, false);
                     }
                 }
             }
         }
     }
 
     private void announce(Message contents) {
         clients.announce(contents);
     }
 
     private void sendMessage(Message contents, int localPort) {
         contents.LocalPort = localPort;
         clients.send(contents);
     }
 }
