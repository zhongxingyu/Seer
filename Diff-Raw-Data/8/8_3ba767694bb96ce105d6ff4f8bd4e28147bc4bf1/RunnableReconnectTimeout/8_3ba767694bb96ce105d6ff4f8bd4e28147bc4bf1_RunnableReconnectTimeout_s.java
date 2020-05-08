 
 package me.heldplayer.irc;
 
 import me.heldplayer.irc.api.BotAPI;
 
 class RunnableReconnectTimeout implements Runnable {
 
     private ServerConnection connection;
 
     public RunnableReconnectTimeout(ServerConnection connection) {
         this.connection = connection;
     }
 
     @Override
     public void run() {
         try {
            Thread.sleep(120000L);
         }
         catch (InterruptedException e) {
             e.printStackTrace();
             BotAPI.console.shutdown();
             return;
         }
 
         try {
             this.connection.connect(this.connection.getNickname());
         }
         catch (Throwable e) {
             e.printStackTrace();
             BotAPI.console.shutdown();
         }
     }
 
 }
