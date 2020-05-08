 package uk.ac.kcl.inf._5ccs2seg.Logic;
 
 import java.util.ArrayList;
 
 import uk.ac.kcl.inf._5ccs2seg.Data.Bot;
 import uk.ac.kcl.inf._5ccs2seg.Data.TargetBox;
 
 /**
  * @ Chris Jones The Class Follow2_3.
  */
 public class WallFollow extends Bot {
	private final static ArrayList<TargetBox> startBox = new ArrayList<>();
 
	private final static ArrayList<TargetBox> doorBox = new ArrayList<>();
 
 	/** The Constant FAST. */
 	private final static double FAST = 0.4;
 
 	protected boolean STOP = false;
 
 	/** The Constant SLOW. */
 	private final double SLOW = 0.4;
 
 	/** The direction. */
 	private int dir;
 
 	/** Constant to convert degrees to radians. */
 	private final double d = Math.PI / 180;
 
 	/** The counter. */
 	private int counter = 1;
 
 	/** The Constant time. */
 	private final long time = System.currentTimeMillis();
 
 	private int botIndex = -1;
 
 	private final double[] doorCheck = new double[20];
 
 	private final int c = 0;
 
 	/**
 	 * Instantiates a new follow2_3.
 	 * 
 	 * @param index
 	 *            the index
 	 * @param debug
 	 *            the debug
 	 */
 	public WallFollow(int index, boolean debug) {
 		super(index, debug);
 
 		botIndex = index;
 		findNearestWall();
 		wallFollowThread(1);
 		targetThread();
 
 		// avoidanceThread();
 		// alignThread();
 	}
 
 	/**
 	 * Find nearest wall.
 	 */
 	public synchronized void findNearestWall() {
 		setSpeed(0);
 		turnTo(90);
 
 		double q = getShortestDistDir();
 
 		if ((q + getHead()) > 180) {
 			turnTo(179.8);
 		} else {
 			turnTo(getHead() + q);
 		}
 
 		while (getFrontRange() > 0.9) {
 			setSpeed(FAST);
 			pause(20);
 
 		}
 		setSpeed(0);
 
 		TargetBox start = new TargetBox(this.getBot(), getX(), getY(),
 				getHead(), getDir(), 2.0, "start");
 		startBox.add(start);
 
 		System.out.println("Y top = " + start.getTop() + " Y bottom = "
 				+ start.getBottom() + "\n X left = " + start.getLeft()
 				+ " X Right = " + start.getRight());
 
 		startBox.add(start);
 	}
 
 	public int getDir() {
 		return dir;
 	}
 
 	public int getIndex() {
 		return botIndex;
 	}
 
 	/**
 	 * Wall follow thread.
 	 * 
 	 * @param i
 	 *            0 = antiClockwise follow. 1 = Clockwise Follow
 	 * 
 	 */
 	protected synchronized void wallFollowThread(int i) {
 		dir = i;
 		final Thread wallFollow = new Thread() {
 			@Override
 			public void run() {
 				while (!STOP) {
 
 					if (dir == 0) {
 						setSpeed(0);
 						turn(90*d + calcTurn(0),0.4);
 						setSpeed(0.4);
 					}
 					if (dir == 1) {
 						System.out.println("Wall");
 						setSpeed(0);
 						turn(-90*d + calcTurn(0), 0.4);
 						setSpeed(0.4);
 						
 					} else {
 						System.err.println("Invalid direction!");
 						System.exit(1);
 					}
 
 					while (getFrontRange() > 0.9) {
 						setSpeed(SLOW);
 
 						/*
 						 * Doorway algorithm
 						 */
 
 						double r3 = getRange(3);
 						double r5 = getRange(5);
 
 						if (r3 > (2 * r5)) {
 							
 							
 								setSpeed(0.2);
 								while(getRange(5)*2 < r3);
 								setSpeed(0);
 								turn(45*d, 0.3);
 								setSpeed(0.2);
 								pause(500);
 								setSpeed(0);
 								double x = calcTurn(1);
 								System.out.print("doorway angle "+x);
 								if( Math.abs(x*d) > 8) turn(x, 0.3);
 								else turn(40*d, 0.3);
 								
 	
 								setSpeed(SLOW);
 								// pause(1500);
 							
 	
 								counter = 1;
 						}
 
 						/*
 						 * Proximity algorithm
 						 */
 
 						if (getRange(4) < 0.9) {
 							counter = 1;
 							System.out.println("prox");
 							setSpeed(-0.4);
 							pause(1000);
 							setSpeed(0);
 							turn( - (10 * d), 0.5);
 							setSpeed(SLOW);
 						}
 
 						if (getRange(7) < 0.9) {
 							counter = 1;
 							System.out.println("prox");
 							setSpeed(-0.4);
 							pause(1000);
 							setSpeed(0);
 							turn((10 * d), 0.5);
 							setSpeed(SLOW);
 						}
 
 						/*
 						 * Wall Align algorithm
 						 */
 
 						if ((counter % 15) == 0) {
 							System.out
 									.println("correction "
 											+ ((int) (((System
 													.currentTimeMillis() - time) / 1000)) / 60)
 											+ ":"
 											+ (((System.currentTimeMillis() - time) / 1000) % 60));
 							setSpeed(0);
 							turn(calcTurn(1), 0.25);
 							setSpeed(SLOW);
 						}
 						try {
 							Thread.sleep(300);
 						} catch (InterruptedException e) {
 						}
 						counter++;
 
 						/***************************/
 
 						try {
 							Thread.sleep(100);
 						} catch (InterruptedException e) {
 						}
 
 					}
 
 				}
 				setSpeed(0);
 				for (int count = 0; count < doorBox.size(); count++) {
 					System.out.println(doorBox.get(count).getCounter() + "  "
 							+ doorBox.get(count).getInitialX() + "  "
 							+ doorBox.get(count).getInitialY());
 				}
 
 			}
 		};
 		wallFollow.start();
 	}
 
 	/**
 	 * Target thread.
 	 */
 	protected synchronized void targetThread() {
 		Thread target = new Thread() {
 			@Override
 			public void run() {
 				while (!STOP) {
 					for (int count = 0; count < startBox.size(); count++) {
 						if ((startBox.get(count).getIndex() != getIndex())
 								&& startBox.get(count).checkTarget(getX(),
 										getY())) {
 							STOP = true;
 						}
 
 					}
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 					}
 
 				}
 			}
 		};
 		target.start();
 	}
 }
