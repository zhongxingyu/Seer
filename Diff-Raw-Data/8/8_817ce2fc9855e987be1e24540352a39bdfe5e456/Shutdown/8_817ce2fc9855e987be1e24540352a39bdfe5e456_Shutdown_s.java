 package net.esper.tacos.ircbot;
 
 public class Shutdown implements Runnable {
 
 	@Override
 	public void run() {
 		TacoBot.bot.disconnect();
 	}
 }
