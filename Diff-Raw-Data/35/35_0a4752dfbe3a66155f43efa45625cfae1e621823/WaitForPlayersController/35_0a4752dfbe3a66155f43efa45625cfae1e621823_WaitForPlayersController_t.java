 package controllers.server;
 
 import channels.ConnectionListener;
 import channels.messages.ChannelMessage;
 import controllers.Workflow;
 import messages.GameStartedMessage;
 import messages.PlayerConnectedMessage;
 import channels.server.SocketServer;
 import channels.SocketChannel;
import messages.playerDisconnectedMessage;
 import messages.ServerDisconnectedMessage;
 import view.WaitForPlayersView;
 
 import java.util.ArrayList;
 
 public class WaitForPlayersController implements God, ConnectionListener {
     private Workflow workflow;
     private WaitForPlayersView view;
     SocketServer server = new SocketServer(1254,this);
     private ArrayList<Player> players = new ArrayList<Player>();
 
     public WaitForPlayersController(Workflow workflow) {
 
         this.workflow = workflow;
     }
 
     public void start() {
         server.start();
     }
 
     public void bind(WaitForPlayersView view) {
         this.view = view;
     }
 
     public void startGame() {
         sendMessage(new GameStartedMessage());
         workflow.startGame();
     }
 
     public void stopServer() {
         sendMessage(new ServerDisconnectedMessage());
         server.stop();
         workflow.goBackToHome();
     }
 
     @Override
     public void onConnectionEstablished(SocketChannel channel) {
         players.add(new Player(channel, this));
     }
 
     @Override
     public void onConnectionFailed(String serverAddress, int serverPort, Exception e) {
     }
 
     @Override
     public void playersJoined(Player player) {
         view.addPlayers(players);
         sendMessage(new PlayerConnectedMessage(getPlayerNames()));
     }
 
     @Override
     public void playerDisconnected(Player player) {
         view.removePlayer(players, player.getName());
        sendMessage(new playerDisconnectedMessage(player.getName()));
     }
 
     private void sendMessage(ChannelMessage message) {
         for (Player player : players) {
             player.sendMessage(message);
         }
     }
 
     private String getPlayerNames() {
         String resultName = "";
         for (Player player : players) {
             resultName += player.getName() + "\n";
         }
         return resultName;
     }
 }
