 import lejos.nxt.*;
 import lejos.nxt.addon.ColorSensor;
 import lejos.util.TimerListener;
 
 /**
  * Representation of a Lego-Tape with controlling functions.
  * @author Sven Schuster
  *
  */
 public class Tape extends Thread {
 	private static final int TIMER_LENGTH = 800;
 	private static final int LEFT_END_MARKER_COLOR = 2;
 	private static final int MARKER_COLOR_MIN = 5;
 	private static final int MARKER_COLOR_MAX = 9;
 
 	private int counter = 0;
 	private int length = 0;
 	private long time;
 	private boolean failed = false;
 	private ColorSensor counterSensor = new ColorSensor(SensorPort.S3);
 	private Timer timer;
 	private boolean requestStop;
 	private boolean dontStop = false;
 
 	private TimerListener tl = new TimerListener(){ 
 		public void timedOut() {
 			Motor.A.stop();
 			failed = true;
 		}
 	};
 
 	/**
 	 * Creates a new Line with given length. The Line will initially move to the leftmost position.
 	 * @param length Length of line
 	 */
 	public Tape(int length) {
 		this.length = length;
 		Motor.A.setSpeed(Common.LINE_SPEED);
 		initialize();
 	}
 
 	// Initializing the Line to the mostleft position.
 	private void initialize() {
 		if(counterSensor.getColorNumber() != Tape.LEFT_END_MARKER_COLOR)
 			Motor.A.forward();
 		while(counterSensor.getColorNumber() != Tape.LEFT_END_MARKER_COLOR) {}
 		Motor.A.stop();
 	}
 
 	/**
 	 * Thread counting at which position the Tape currently is, starting at the leftmost position 0.
 	 */
 	public void run() {
 		this.requestStop = false;
 		while(!this.requestStop) {
 			if ((counterSensor.getColorNumber() >= Tape.MARKER_COLOR_MIN && counterSensor.getColorNumber() <= Tape.MARKER_COLOR_MAX) || counterSensor.getColorNumber() == Tape.LEFT_END_MARKER_COLOR){
 				if (!this.dontStop) {
 					if (Motor.A.isBackward()) {
 						
 						this.counter++;						 
						LCD.drawString("POS:" + this.counter + " ", 6, 3);
 					}
 					if (Motor.A.isForward()) {
 						
 						this.counter--;
						LCD.drawString("POS:" + this.counter + " ", 6, 3);
 					}
 					Motor.A.stop();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Moving the head one position to the left.
 	 * @return true - Repositioning successful<br />
 	 * 		   false - Repositioning failed
 	 */
 	public boolean moveLeft() {
 		if(this.counter == 0)
 			return false;
 		else{
 			failed = false;
 			timer = new Timer(this.TIMER_LENGTH,tl);
 
 			this.dontStop = true;
 			Motor.A.forward();
 			try {
 				Thread.sleep(400);
 			}
 			catch (Exception e) {
 
 			}
 			this.dontStop = false;
 
 			while(!Motor.A.isStopped()){}
 			if(failed) {
 				LCD.drawString("Security warning: Timer elapsed!", 0, 2);
 				return false;
 			}
 			timer.stop();
 			//LCD.clear();
 			return true;
 		}
 	}
 
 	/**
 	 * Moving the head one position to the right.
 	 * @return true - Repositioning successful<br />
 	 * 		   false - Repositioning failed
 	 */
 	public boolean moveRight() {
 		if(this.counter == this.length-1)
 			return false;
 		else {
 			failed = false;
 			timer = new Timer(this.TIMER_LENGTH,tl);
 
 			this.dontStop = true;
 			Motor.A.backward();
 			try {
 				Thread.sleep(400);
 			}
 			catch (Exception e) {
 
 			}
 			this.dontStop = false;
 
 			timer.start();
 			while(!Motor.A.isStopped()){}
 			if(failed) {
 				LCD.drawString("Security warning: Timer elapsed!", 0, 2);
 				return false;
 			}
 			timer.stop();
 			//LCD.clear();
 			return true;
 		}
 	}
 
 	/**
 	 * Returns the ColorSensor responsible for detecting the current position.
 	 * @return ColorSensor
 	 */
 	public ColorSensor getCounterSensor() {
 		return this.counterSensor;
 	}
 
 	/**
 	 * Returns the current position, starting with the leftmost position 0.
 	 * @return int position
 	 */
 	public int getCount() {
 		return this.counter;
 	}
 
 	public boolean clearTape() {
 		for (int i = 0; i < Common.TAPE_SIZE; i++) {
 			Common.pushBits(true,true);
 			if (this.counter < Common.TAPE_SIZE-1 && !this.moveRight()) {
 				return false;
 			}
 		}
 		for (int i = Common.TAPE_SIZE-1; i > 0; i--) {
 			if (!this.moveLeft()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void requestStop() {
 		this.requestStop = true;
 	}
 
 	public void stop() {
 		this.requestStop();
 		try {
 			this.join();
 		}
 		catch (InterruptedException e) {
 
 		}
 	}
 }
