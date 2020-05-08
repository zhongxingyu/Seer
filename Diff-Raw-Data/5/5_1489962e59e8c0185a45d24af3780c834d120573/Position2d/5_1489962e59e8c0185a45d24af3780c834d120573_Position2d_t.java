 package device;
 
 import data.Position;
 import javaclient3.PlayerClient;
 import javaclient3.PlayerException;
 import javaclient3.Position2DInterface;
 import javaclient3.structures.PlayerConstants;
 
 public class Position2d implements Runnable{
 	protected Position2DInterface posi  = null;
 	protected Position pos = null;
 	protected final int SLEEPTIME = 100;
 	
 	// Every class of this type has it's own thread
 	public Thread thread = new Thread ( this );
 	private double speed = 0.;
 	private double turnrate = 0.;
 
 	// Host id
 	public Position2d (PlayerClient host, int id) {
 		try {
 			this.posi = host.requestInterfacePosition2D (0, PlayerConstants.PLAYER_OPEN_MODE);
 
 			// Automatically start own thread in constructor
 			this.thread.start();
 			System.out.println("Running "
 					+ this.toString()
 					+ " in thread: "
 					+ this.thread.getName()
 					+ " of robot "
 					+ id);
 
 		} catch ( PlayerException e ) {
 			System.err.println ("Position: > Error connecting to Player: ");
 			System.err.println ("    [ " + e.toString() + " ]");
 			System.exit (1);
 		}
 	}
 	public Position getPosition() {
 		return this.pos;
 	}
 	// TODO implement
 	public void setPosition (Position pos) {
 //		this.pos = pos;
 	}
 	public void setSpeed (double speed, double turnrate){
 		this.speed = speed;
 		this.turnrate = turnrate;
 	}
 	// TODO implement
 //	public getSpeed () {
 //		return 
 //	}
 	// Only to be called @~10Hz
 	protected void update() {
 		// Wait for sonar readings
 		while ( ! posi.isDataReady() ){
 			try { Thread.sleep (this.SLEEPTIME); }
 			catch (InterruptedException e) { this.thread.interrupt(); }
 		}
 		if(posi.getData() != null) { // TODO should not happen
 			// Request current position
 			this.pos = new Position(
 					this.posi.getData().getPos().getPx(),
 					this.posi.getData().getPos().getPy(),
 					this.posi.getData().getPos().getPa() );
 		}
		// Set new speed
		this.posi.setSpeed(this.speed, this.turnrate);
 	}
 
 	@Override
 	public void run() {
 		while ( ! this.thread.isInterrupted()) {
 			this.update ();
 		}
 		System.out.println("Shutdown of " + this.toString());
 	}
 }
