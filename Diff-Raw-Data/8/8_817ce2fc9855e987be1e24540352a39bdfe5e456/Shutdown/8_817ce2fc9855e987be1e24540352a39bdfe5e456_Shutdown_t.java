 package net.esper.tacos.ircbot;
 
 public class Shutdown implements Runnable {
 
 	@Override
 	public void run() {
 		TacoBot.bot.disconnect();
		System.out.println("Shutting down");
		System.exit(0);
 	}
 }
