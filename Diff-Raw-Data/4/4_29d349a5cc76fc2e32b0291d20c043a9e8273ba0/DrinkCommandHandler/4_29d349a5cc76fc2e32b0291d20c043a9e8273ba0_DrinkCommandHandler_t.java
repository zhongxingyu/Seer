 package com.drtshock.willie.command;
 
 import com.drtshock.willie.Willie;
 import java.util.ArrayList;
 import java.util.Random;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 /**
  *
  * @author drtshock
  */
 public class DrinkCommandHandler implements CommandHandler {
 
     private Random rand;
     private ArrayList<String> messages;
 
     public DrinkCommandHandler() {
         this.rand = new Random();
         this.messages = new ArrayList<>();
 
         this.messages.add(Colors.NORMAL + "mixes %s a drink!");
         this.messages.add(Colors.NORMAL + "gives %s a wine cooler.");
         this.messages.add(Colors.NORMAL + "pours %s a shot of rum!");
         this.messages.add(Colors.NORMAL + "pours %s a jaeger bomb!");
     }
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) {
         String message;
        if (args.length > 0) {
            message = String.format(this.messages.get(this.rand.nextInt(this.messages.size())), args[0]);
         } else {
             message = String.format(this.messages.get(this.rand.nextInt(this.messages.size())), "");
         }
         bot.sendAction(channel, message);
     }
 }
