 package engine;
 
 import game.Game;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import commands.Command;
 
 /**
  * Singleton
  */
 public class Engine implements Runnable {
 
 	private ConcurrentLinkedQueue<Command> commands;
 	
 	public ConcurrentLinkedQueue<Command> getCommands() {
 		return commands;
 	}
 	
 	public void start(){
 		new Thread(this).start();
     }
 		
 	@Override
 	public void run() {
 		while (Game.getInstance().isRunning()) {
			
			while(!commands.isEmpty()) {
				Command command = commands.remove();
 				command.doCommand();
 			}
 			
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	
 	private Engine() {
 		super();
 		this.commands = new ConcurrentLinkedQueue<Command>();
 	}
 	
 	/* 
 	 * 
 	 ************    Holder class : for the Singleton pattern implementation   ******************
 	 */
 	
 	private static class EngineHolder
 	{		
 		// unique instance, not preinitialized
 		private final static Engine instance = new Engine();
 	}
  
 	// Getter for the unique instance of the Singleton
 	public static Engine getInstance()
 	{
 		return EngineHolder.instance;
 	}
 	
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
