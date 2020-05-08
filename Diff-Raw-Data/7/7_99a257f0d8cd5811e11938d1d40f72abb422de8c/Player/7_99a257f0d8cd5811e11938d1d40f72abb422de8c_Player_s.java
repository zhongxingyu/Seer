 public abstract class Player extends Thread {
 	protected Cell currentCell;
 	private long sleepTime;
 	private boolean dead;
 
 	public Player(Cell startCell, long sleepTime) {
 		currentCell = startCell;
 		this.sleepTime = sleepTime;
 		this.dead = false;
 	}
 
 	public void run() {
 		while (true) {
 			try {
				sleep(sleepTime);	
				step();
 			} catch (InterruptedException e) {
 				if (this.dead)
 					return;
			}
 		}
 	}
 
 	public void kill() {
 		this.dead = true;
 		this.interrupt();
 	}
 
 	protected abstract void step();
 }
