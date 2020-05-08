 package controllers.client;
 
 import channels.SocketChannel;
 import channels.SocketChannelListener;
 import channels.messages.ChannelMessage;
 import controllers.Workflow;
 import messages.*;
 import view.JoinGameView;
 
 import java.io.IOException;
 
 public class JoinGameController implements SocketChannelListener {
     private Workflow workflow;
     private final SocketChannel channel;
     private final String serverName;
     private final String playerName;
     private JoinGameView view;
 
     public JoinGameController(Workflow workflow, SocketChannel channel, String serverName, String playerName) {
 
         this.workflow = workflow;
         this.channel = channel;
         this.serverName = serverName;
         this.playerName = playerName;
         channel.bind(this);
     }
 
     public void bind(JoinGameView view) {
         this.view = view;
     }
 
 
     @Override
     public void onClose(SocketChannel channel, Exception e) {
 
     }
 
     @Override
     public void onSendFailed(SocketChannel channel, IOException e, ChannelMessage message) {
     }
 
     @Override
     public void onNewMessageArrived(SocketChannel channel, ChannelMessage message) {
         if (message instanceof PlayerConnectedMessage) {
             PlayerConnectedMessage playerConnectedMessage = (PlayerConnectedMessage) message;
             view.displayConnectedPlayers(playerConnectedMessage.getPlayersConnected());
         } else if (message instanceof ServerDisconnectedMessage) {
             view.serverDisconnected(serverName);
             channel.stop();
         } else if (message instanceof GameStartedMessage) {
             view.gameStarted();
         }
     }
 
     @Override
     public void onMessageReadError(SocketChannel channel, Exception e) {
     }
 
     public void start() {
         view.connectedToServer(serverName, playerName);
         channel.send(new PlayerDetailsMessage(playerName));
     }
 
     public void goToHome() {
         workflow.goBackToHome();
     }
 
     public void disconnectingFromServer() {
        channel.send(new PlayerDisconnectedMessage(playerName));
         channel.stop();
         workflow.getClientDetails();
     }
 
     public void gameStarted() {
         workflow.startGame();
     }
 }
