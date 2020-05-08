 package controllers.server;
 
 import channels.SocketChannel;
 import channels.SocketChannelListener;
 import channels.messages.ChannelMessage;
 import messages.PlayerDetailsMessage;
import messages.playerDisconnectedMessage;
 
 import java.io.IOException;
 
 public class Player implements SocketChannelListener {
 
     private final SocketChannel channel;
     private God god;
     private String name;
 
     public Player(SocketChannel channel, God god) {
 
         this.channel = channel;
         this.god = god;
         channel.bind(this);
     }
 
     @Override
     public void onClose(SocketChannel channel, Exception e) {
     }
 
     @Override
     public void onSendFailed(SocketChannel channel, IOException e, ChannelMessage message) {
     }
 
     @Override
     public void onNewMessageArrived(SocketChannel channel, ChannelMessage message) {
         if (message instanceof PlayerDetailsMessage) {
             PlayerDetailsMessage playerDetailsMessage = (PlayerDetailsMessage) message;
             name = playerDetailsMessage.getPlayerName();
             god.playersJoined(this);
         }
        else if(message instanceof playerDisconnectedMessage)
         {
            playerDisconnectedMessage playerDisconnectedMessage =  (messages.playerDisconnectedMessage) message;
             name = playerDisconnectedMessage.getPlayerName();
            god.playerDisconnected(this);
         }
     }
 
     @Override
     public void onMessageReadError(SocketChannel channel, Exception e) {
     }
 
     public String getName() {
         return name;
     }
 
     public void sendMessage(ChannelMessage message) {
         channel.send(message);
     }
 }
