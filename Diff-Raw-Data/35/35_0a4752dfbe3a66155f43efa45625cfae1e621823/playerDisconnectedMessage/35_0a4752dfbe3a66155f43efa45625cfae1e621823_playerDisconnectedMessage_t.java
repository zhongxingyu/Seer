 package messages;
 
 import channels.messages.ChannelMessage;
 
 import java.io.Serializable;
 
public class playerDisconnectedMessage extends ChannelMessage implements Serializable {
     private String playerName;
 
    public playerDisconnectedMessage(String playerName) {
         this.playerName = playerName;
     }
 
     public String getPlayerName() {
         return playerName;
     }
 }
