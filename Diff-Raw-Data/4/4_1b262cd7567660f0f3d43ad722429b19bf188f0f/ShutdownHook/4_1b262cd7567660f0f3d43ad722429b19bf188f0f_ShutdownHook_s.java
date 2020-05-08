 package ch.romibi.minecraft.toIrc;
 
 import java.io.IOException;
 
 public class ShutdownHook extends Thread{
 	public void run() {
		McToIrc.sendCommandToMc("stop");
 		McToIrc.irc.stop();
 		try {
 			McToIrc.console.consoleReader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
