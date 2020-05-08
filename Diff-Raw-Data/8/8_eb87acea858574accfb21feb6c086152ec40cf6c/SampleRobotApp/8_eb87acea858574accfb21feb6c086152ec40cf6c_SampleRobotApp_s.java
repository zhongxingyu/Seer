 /* XXX TODO
  * adjust path following to account for carpet
  * remove fake walls behind us as we travel along path
  * stopping/turning sloppiness when not toward gold
  * 
  * maybe replan on the fly
  */
 
 package edu.cmu.ri.mrpl.example;
 /*
 
  * Sample code for the 16x62 class taught at the Robotics Institute
  * of Carnegie Mellon University
  *
  * written from scratch by Martin Stolle in 2006
  *
  * inspired by code started by Illah Nourbakhsh and used
  * for many years.
  */
 
 import static edu.cmu.ri.mrpl.RobotModel.ROBOT_RADIUS;
 import static edu.cmu.ri.mrpl.maze.MazeLocalizer.*;
 import static java.lang.Math.*;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import edu.cmu.ri.mrpl.*;
 import edu.cmu.ri.mrpl.CommClient.CommException;
 import edu.cmu.ri.mrpl.comm.Messaging;
 import edu.cmu.ri.mrpl.kinematics2D.*;
 import edu.cmu.ri.mrpl.maze.*;
 import edu.cmu.ri.mrpl.maze.MazeGraphics.ContRobot;
 import edu.cmu.ri.mrpl.maze.MazeWorld.Direction;
 import edu.cmu.ri.mrpl.usbCamera.*;
 import edu.cmu.ri.mrpl.util.*;
 import edu.cmu.ri.mrpl.util.GradientDescent.*;
 
 public class SampleRobotApp extends JFrame implements ActionListener, TaskController {
 
 	private boolean HAS_PARTNER = true;
 	public static final boolean IS_FIRST_PARTNER = true;
 	
 	private Robot robot;
 	private SonarConsole sc;
 
 	private JFrame scFrame;
 
 	private JButton connectButton;
 	private JButton disconnectButton;
 
 	private JButton testCameraButton;
 	private JButton setupCommsButton;
 	private JButton gooButton;
 
 	private JButton stopButton;
 	private JButton quitButton;
 	private JButton michaelPhelpsButton;
 
 	private java.util.LinkedList<Task> upcomingTasks;
 
 	final double DISTANCE_TOLERANCE = 0.01;
 	final double ANGLE_TOLERANCE = 0.01;
 	final double SPEED_TOLERANCE = 0.01;
 
 	private Task curTask = null;
 	
 	JFrame cameraFrame;
 	JFrame feedbackFrame;
 	PicCanvas cameraCanvas;
 	PicCanvas feedbackCanvas;
 	CommClient comm;
 	Messaging messaging;
 	boolean timeToGo = false;
 
 	public SampleRobotApp() {
 		super("Kick Ass Sample Robot App");
 		
 		cameraFrame = new JFrame("Camera");
 		cameraCanvas = new PicCanvas();
 		cameraFrame.getContentPane().add(cameraCanvas, BorderLayout.CENTER);
 		cameraFrame.setSize(UsbCamera.XSIZE, UsbCamera.YSIZE);
 		cameraFrame.setLocation(500, 0);
 		cameraFrame.setVisible(true);
 
 		feedbackFrame = new JFrame("Feedback");
 		feedbackCanvas = new PicCanvas();
 		feedbackFrame.getContentPane().add(feedbackCanvas, BorderLayout.CENTER);
 		feedbackFrame.setSize(UsbCamera.XSIZE, UsbCamera.YSIZE);
 		feedbackFrame.setLocation(500, 300);
 		feedbackFrame.setVisible(true);
 
 		upcomingTasks = new LinkedList<Task>();
 
 		connectButton = new JButton("connect");
 		disconnectButton = new JButton("disconnect");
 
 		testCameraButton = new JButton("Test camera");
 		setupCommsButton = new JButton("Setup comms");
 		gooButton = new JButton("GOOOOOO"); // was "Turn to angle!"
 		stopButton = new JButton(">> stop <<");
 		quitButton = new JButton(">> quit <<");
 		michaelPhelpsButton = new JButton("Michael Phelps");
 
 		connectButton.addActionListener(this);
 		disconnectButton.addActionListener(this);
 
 		testCameraButton.addActionListener(this);
 		setupCommsButton.addActionListener(this);
 		gooButton.addActionListener(this);
 		stopButton.addActionListener(this);
 		quitButton.addActionListener(this);
 		michaelPhelpsButton.addActionListener(this);
 
 		Container main = getContentPane();
 		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
 		Box box;
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(connectButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(disconnectButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(testCameraButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(setupCommsButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(gooButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(michaelPhelpsButton);
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(stopButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(quitButton);
 
 		main.add(Box.createVerticalStrut(30));
 
 		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				quit();
 			}
 		});
 
 
 		//this.setLocationByPlatform(true);
 		this.pack();
 		// moved this line to the bottom to make the controls show up on top
 		//this.setVisible(true);
 
 		// construct the SonarConsole, but don't make it visible
 		scFrame = new JFrame("Sonar Console");
 		scFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		sc = new SonarConsole();
 		scFrame.add(sc);
 		scFrame.pack();
 
 		this.setVisible(true);
 
 		String message = "Is this running on the robot?";
 		int isRobot = JOptionPane.showConfirmDialog(null, message, message, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 		if (isRobot == JOptionPane.YES_OPTION) {
 			robot = new ScoutRobot();
 		}
 		else {
 			robot = new SimRobot();
 		}
 
 		robot.setAccel(0.25, 0.25);
 	}
 
 	// call from GUI thread
 	public void connect() {
 		try {
 			robot.connect();
 		} catch(IOException ioex) {
 			System.err.println("couldn't connect to robot:");
 			ioex.printStackTrace();
 		}
 	}
 
 	// call from GUI thread
 	public void disconnect() {
 		try {
 			robot.disconnect();
 		} catch(IOException ioex) {
 			System.err.println("couldn't disconnect from robot:");
 			ioex.printStackTrace();
 		}
 	}
 
 	// call from GUI thread
 	public synchronized void stop() {
 		upcomingTasks.clear();
 		if (curTask!=null)
 			curTask.pleaseStop();
 	}
 
 	// call from GUI thread
 	public void quit() {
 
 		synchronized(this) {
 			if (curTask!=null) {
 				curTask.pleaseStop();
 
 				try {
 					this.wait(1000);
 				} catch(InterruptedException iex) {
 					System.err.println("interrupted while waiting for worker termination signal");
 				}
 			}
 
 			if (curTask!=null) {
 				System.err.println("exiting even though robot is running!");
 			} else {
 				disconnect();
 			}
 		}
 
 		System.exit(0);
 	}
 
 	// invoked from non-GUI thread to hide/show SonarConsole
 	public void showSC() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				scFrame.setLocationByPlatform(true);
 				scFrame.setVisible(true);
 			}
 		});
 	}
 
 	// invoked from non-GUI thread to hide/show SonarConsole
 	public void hideSC() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				scFrame.setVisible(false);
 			}
 		});
 	}
 
 	// this doesn't work quite right if called multiple times when the task list is non-empty
 	// something to do with how canStart works vs checking for a null curTask
 	private void startUpcomingTasks () {
 		System.err.println("startUpcomingTasks");
 		if (!upcomingTasks.isEmpty() && curTask == null) {
 			Task next = upcomingTasks.remove();
 			System.err.println("attempting to start " + next);
 			//if (canStart(next)) {
 			new Thread(next).start();
 			//}
 		}
 		else {
 			System.err.println("no more tasks left or task is currently running");
 		}
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		Object source = e.getSource();
 
 		if (source==connectButton) {
 			connect();
 			if (HAS_PARTNER) {
 				setupCommsButton.requestFocusInWindow();
 			} else {
 				michaelPhelpsButton.requestFocusInWindow();
 			}
 		} else if ( source==disconnectButton ) {
 			disconnect();
 		} else if ( source==stopButton ) {
 			stop();
 		} else if ( source==quitButton ) {
 			quit();
 		} else if ( source==testCameraButton ) {
 //			upcomingTasks.add(new PauseTask(this));
 //			startUpcomingTasks();
 			UsbCamera cam = UsbCamera.getInstance();
 			
 			System.out.println("blue: "+checkForBlue(cam));
 		} else if ( source==setupCommsButton ) {
 //			upcomingTasks.add(new WaitTask(this, argument));
 //			startUpcomingTasks();
 			michaelPhelpsButton.requestFocusInWindow();
 			String ipAddress = "128.237.231.235";
 			comm = new CommClient(ipAddress);
 			
 			//commclient test stuff
 			String myName = "SinNombre_" + IS_FIRST_PARTNER;
 			String myFriends[] = {"SinNombre_" + !IS_FIRST_PARTNER};
 
 			//*
 			try{
 				//CommClient will throw an exception if it does not succeed
 				comm.connectToFriends(myName, myFriends);
 			}
 			catch(CommException err) {
 				System.err.println("Comm Exception: " + err);
 				//All errors except missing-friends are not handled here.
 				if(!err.getMessage().startsWith("missing-friends")){
 					System.err.println("Giving up");
 					return;
 				}
 
 				//Wait until all friends connect.
 				boolean friendsReady = false;
 				while(!friendsReady){
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e1) {
 						e1.printStackTrace();
 					}
 					try {
 						comm.getIncomingMessage();
 					} catch (CommException e1) {
 						System.err.println("Comm Exception: " + e1);
 						if(e1.getMessage().startsWith("friends-ready"))
 							friendsReady = true;
 						else{
 							//Again, anything except friends-ready is not handled.
 							System.err.println("Giving up");
 							return;
 						}
 					}
 				}
 			}
 			messaging = new Messaging(comm, myFriends[0]);
 		} else if ( source==gooButton ) {
 			//upcomingTasks.add(new TurnToTask(this, argument));
 			//startUpcomingTasks();
 			timeToGo = true;
 		} else if ( source==michaelPhelpsButton ) {
 			JFileChooser chooser = new MazeFileChooser();
 			int returnVal = chooser.showOpenDialog(this);
 			if(returnVal == JFileChooser.APPROVE_OPTION) {
 				String mazeFileName = chooser.getSelectedFile().getAbsolutePath();
 				System.out.println("You chose to open this file: " + mazeFileName);
 				
 				try {
 					MazeWorld world = new MazeWorld(mazeFileName);
 					// start by finding next gold
 					List<MazeState> states = new MazeSolver(world).findPath(true);
 					String commands = MazeSolver.statesToCommandsString(states);
 					System.out.println(commands);
 					//java.util.List<RealPose2D> poses = MazeLocalizer.statesToPoses(states);
 					
 					double theta = AngleMath.calculateTurnAngle(commands);
 					upcomingTasks.add(new TurnToTask(this, Angle.normalize(theta)));
 					
 					// follow the path
 					// switched these for testing
 					//upcomingTasks.add(new SolveMazeTask(this, 0.1, mazeFileName));
 					upcomingTasks.add(new MichaelPhelpsTask(this, 0.1, mazeFileName));
 					
 					/* get final heading correct
 					theta = 0;
 					i = poses.size()-1;
 					while (i > -1) {
 						switch (commands.charAt(i--)) {
 						case 'G':
 							i = -1;
 							break;
 						case 'L':
 							theta += PI/2;
 							break;
 						case 'R':
 							theta -= PI/2;
 							break;
 						}
 					}
 					upcomingTasks.add(new TurnToTask(this, Angle.normalize(theta)));
 					//*/
 					
 					startUpcomingTasks();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 
 				this.requestFocus();
 				gooButton.requestFocusInWindow();
 			}
 		}
 	}
 	
 	public synchronized boolean canStart(Task t) {
 		if (curTask!=null)
 			return false;
 
 		curTask = t;
 		return true;
 	}
 
 	public synchronized void finished(Task t) {
 		if (curTask!=t) {
 			System.err.println("ignoring finished() from unknown task "+t+"!");
 			return;
 		}
 		curTask=null;
 		startUpcomingTasks();
 		this.notifyAll();
 	}
 
 	public static void main(String[] argv) {
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new SampleRobotApp();
 			}
 		});
 	}
 
 	// here we implement the different robot controllers
 	// as Tasks
 	//
 
 	class PauseTask extends Task implements KeyListener, MouseListener {
 
 		Speech speech;
 		boolean done;
 
 		PauseTask(TaskController tc) {
 			super(tc);
 		}
 
 		public void taskRun() {
 			speech = new Speech();
 			speech.speak("Waiting for you");
 			done = false;
 			//long startTime = System.currentTimeMillis();
 
 			// these only catch events on the specific object
 			testCameraButton.addKeyListener(this);
 			SampleRobotApp.this.addKeyListener(this);
 			SampleRobotApp.this.addMouseListener(this);
 
 			while(!shouldStop() && !done) {
 				try {
 					Thread.sleep(50);
 				} catch(InterruptedException iex) {
 					System.err.println("pause sleep interrupted");
 				}
 			}
 
 			//long endTime = System.currentTimeMillis();
 			//double elapsedTime = (endTime - startTime) / 1000.0;
 			//remainingField.setValue(elapsedTime);
 		}
 
 		public String toString() {
 			return "pause task";
 		}
 
 		public void keyPressed(KeyEvent e) {
 			done = true;
 			testCameraButton.removeKeyListener(this);
 			SampleRobotApp.this.removeKeyListener(this);
 		}
 
 		public void mouseClicked(MouseEvent arg0) {
 			done = true;
 			SampleRobotApp.this.removeMouseListener(this);
 		}
 
 		public void keyReleased(KeyEvent e) {}
 		public void keyTyped(KeyEvent e) {}
 		public void mouseEntered(MouseEvent arg0) {}
 		public void mouseExited(MouseEvent arg0) {}
 		public void mousePressed(MouseEvent arg0) {}
 		public void mouseReleased(MouseEvent arg0) {}
 	}
 
 	class TurnToTask extends Task {
 
 		Speech speech;
 		double desiredAngle;
 		Controller controller;
 
 		TurnToTask(TaskController tc, double a) {
 			super(tc);
 			setDesiredAngle(a);
 		}
 
 		public void setDesiredAngle (double a) {
 			desiredAngle = a;
 			//System.out.println("desiredAngle: " + desiredAngle);
 		}
 
 		public void taskRun() {
 			speech = new Speech();
 			controller = new Controller(robot);
 			final double Kp = .4;
 			final double Kd = 95;
 			double u = 0;
 			robot.updateState();
 			double curAngle = Angle.normalize(robot.getHeading());
 			double lastAngle = curAngle;
 			double curTime = System.nanoTime();
 			double lastTime = curTime;
 			double destAngle = Angle.normalize(curAngle + desiredAngle);
 			//System.out.println("destAngle: " + destAngle);
 			double angleErr = Angle.normalize(destAngle - curAngle);
 
 			final double ANGLE_TOLERANCE = 0.01;
 			final double SPEED_TOLERANCE = 0.01;
 
 			while(!shouldStop() &&
 					(abs(angleErr) > ANGLE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				//System.out.println("angleErr = " + angleErr);
 				robot.updateState();
 				lastAngle = curAngle;
 				lastTime = curTime;
 				curAngle = Angle.normalize(robot.getHeading());
 				//curTime = System.nanoTime();
 				curTime = System.currentTimeMillis();
 				angleErr = Angle.normalize(destAngle - curAngle);
 
 				double angleTraveled = Angle.normalize(curAngle - lastAngle);
 				boolean progressMade = signum(angleTraveled) == signum(angleErr);
 
 				double pterm = Kp*abs(angleErr);
 				double dterm = Kd*(abs(angleTraveled))/(curTime-lastTime);
 				if (progressMade) {
 					dterm *= -1;
 				}
 				//System.err.println("pterm = " + pterm + "\ndterm = " + dterm + "\n");
 				u = pterm + dterm;
 				double direction = signum(angleErr);
 				//System.err.println(angleErr + " off, speed = " + u*direction);
 				double speed = direction*u;
 				controller.setVel(-speed, speed);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("turn-to sleep interrupted");
 				}
 			}
 
 			//speech.speak("error " + AngleMath.roundTo2(angleErr*1000) + " milliradians");
 			//System.out.println("curAngle: " + curAngle);
 			controller.stop();
 		}
 
 		public String toString() {
 			return "turn-to task: " + desiredAngle;
 		}
 	}
 
 	private static double calculateArcLength (Point2D dest, double radius) {
 		double x = dest.getX();
 		double y = dest.getY();
 		double angle;
 		if (radius > 0) {
 			angle = Math.atan2(x, radius - y);
 		}
 		else {
 			angle = Math.atan2(x, y - radius);
 		}
 		double arcLength = radius*angle;
 		if (radius < 0) {
 			arcLength *= -1;
 		}
 		return arcLength;
 	}
 
 	// returns the radius and arc length to get to a given point relative to the robot
 	private static double[] calculateArc (Point2D dest) {
 		double x = dest.getX();
 		double y = dest.getY();
 		if (y == 0) {
 			return new double[]{Double.MAX_VALUE, x};
 		}
 		double radius = (x*x + y*y) / (2*y);
 		double arcLength = calculateArcLength(dest, radius);
 		/*
 		if (arcLength < 0) {
 			System.err.println("NEGATIVE ARC LENGTH");
 		}
 		*/
 		return new double[]{radius, arcLength};
 	}
 
 	enum Subtask {
 		START,
 		FOLLOWPATH_GOLD_CELL,
 		GOTO_GOLD_WALL, GO_FROM_GOLD_WALL,
 		FOLLOWPATH_DROP_CELL, DROP_GOLD,
 		TURNTO_GOLD, TURNTO_GOLD_CHECK, TURNTO_PATH, TURNTO_DROP,
 		
 		END_TASK // used to end the overall Task
 	}
 	
 	// named so because it gets all the golds
 	class MichaelPhelpsTask extends Task {
 //		private RealPose2D robotStartedHere;
 		private Controller controller;
 		private Speech speech;
 		private UsbCamera cam;
 
 		private Perceptor perceptor;
 		private MazeLocalizer correctedLocalizer;
 		private MazeLocalizer rawLocalizer;
 		private MazeWorld mazeWorld;
 		private MazeWorld originalMazeWorld; // doesn't have fake walls
 		private MazeGraphics mazeGraphics;
 		private JFrame wrapper;
 		
 		private Subtask curSubtask = Subtask.START;
 		
 		// used in Subtasks GOTO_GOLD_WALL, GO_FROM_GOLD_WALL
 		private MazeState goalState;
 		private MazeState lastState;
 		private boolean hasGold = false;
 		
 		// variables that are updated by the main taskRun loop before calling subtask methods
 		private RealPose2D curPose;
 		
 		// path following stuff
 		// used in Subtasks GOTO_GOLD_CELL, GOTO_DROP_CELL
 		private java.util.List<Line2D> pathSegments;
 		private double maxDeviation;
 		private static final double LOOKAHEAD_DISTANCE = 1;
 		private Point2D lookaheadPointRelWorld;
 		private boolean stopping = false;
 		// used when no paths can be found
 		private boolean playMoveBitch = true;
 		private final String moveBitchClip = "move_bitch.wav";
 		private final String getOutTheWayClip = "get_out_tha_way.wav";
 		private String currentBitchClip = moveBitchClip;
 		
 		// turn to stuff
 		// used in Subtask TURNTO
 		private double curAngle;
 		private double lastAngle;
 		private double curTime;
 		private double lastTime;
 		private double destAngle;
 		private static final double ANGLE_TOLERANCE = 0.01;
 		private static final double SPEED_TOLERANCE = 0.01;
 		
 		// used in Subtask DROP_GOLD
 		private double dropStartTime;
 		
 		// GOTO stuff
 		private double xOffset;
 //		private RealPose2D curPoseRelStart;
 //		private RealPose2D lastPoseRelStart;
 		private Point2D startPoint;
 		static final double IDEAL_GOLD_OFFSET = CELL_RADIUS - 1.4*ROBOT_RADIUS;
 		static final double MAGNET_DISTANCE = 0.5 * ROBOT_RADIUS;
 		private boolean retryGoto = false;
 		static final double RETRY_OFFSET = 0.0375;
 		
 		// sonar localization stuff
 		private RealPose2D lastPollPosition;
 		private static final double POLL_INTERVAL = 0.01; // meters between polling the sonars
 		private RealPose2D lastGradientPosition;
 		private static final double GRADIENT_INTERVAL = .1; // meters between running gradient descent on points
 		double[] directSonarReadings = new double[16];
 		private RingBuffer<Point2D> pointsBuffer;
 		
 		// whether or not we can edit ours/partners maze
 		private boolean inCharge = !HAS_PARTNER || IS_FIRST_PARTNER;
 		private long inChargeSince = System.currentTimeMillis();
 		public static final long IN_CHARGE_DURATION = 1000;
 		public List<MazeState> fakeWalls = new LinkedList<MazeState>();
 
 		public MichaelPhelpsTask(TaskController tc, double maxDeviation, String mazeFileName) {
 			super(tc);
 			
 			try {
 				mazeWorld = new MazeWorld(mazeFileName);
 				originalMazeWorld = new MazeWorld(mazeWorld);
 			} catch (IOException e) {
 				System.err.println("Couldn't read maze file!");
 			}
 
 			// init globals
 			mazeGraphics = new MazeGraphics(mazeWorld);
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			speech = new Speech();
 			cam = UsbCamera.getInstance();
 			pointsBuffer = new RingBuffer<Point2D>(400);
 			correctedLocalizer = new MazeLocalizer(mazeWorld, false);
 			rawLocalizer = new MazeLocalizer(mazeWorld, true);
 			
 			// locate robot in maze
 			MazeState init = mazeWorld.getInits().iterator().next();
 			perceptor.setCorrectedPose(MazeLocalizer.mazeStateToWorldPose(init));
 			
 			// init graphics
 			wrapper = new JFrame();
 			wrapper.add(mazeGraphics);
 			wrapper.setSize(200, 200);
 			wrapper.setLocation(0, 500);
 			wrapper.setVisible(true);
 		}
 		
 		public void setDesiredPath (List<RealPose2D> poses, double maxDeviation) {
 			this.maxDeviation = maxDeviation;
 
 			// construct list of line segments
 			pathSegments = Lookahead.posesToPath(poses);
 			System.err.println(pathSegments.size() + " PATH SEGMENTS");
 			Lookahead.zeroCurrentSegment();
 		}
 
 		public void taskRun() {
 			robot.turnSonarsOn();		
 			robot.updateState();
 
 //			robotStartedHere = perceptor.getWorldPose();
 			
 			// set up continuous robots on graphical maze
 			java.util.List<ContRobot> contBotList = Collections.synchronizedList(new ArrayList<ContRobot>()); 
 			RealPose2D correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 			RealPose2D rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 			contBotList.add(new ContRobot(rawPosition, Color.RED));
 			contBotList.add(new ContRobot(correctedPosition, Color.GREEN));
 			// add robot used for lookahead point if needed
 			//contBotList.add(new ContRobot(new RealPose2D(0,0,0), new Color(0f, 0f, 1f, 0.5f)));
 			mazeGraphics.setContRobots(contBotList);
 			
 			curPose = perceptor.getCorrectedPose();
 			lastPollPosition = curPose;
 			lastGradientPosition = curPose;
 			
 			controller.setVel(0.1, 0.1);
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 			controller.setVel(-0.1, -0.1);
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 			controller.stop();
 			
 			
 			speech.speak("ready");
 			while(!timeToGo) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 
 			// stopping handled by START subtask method
 			// which transitions to END_TASK if there are no more golds
 			while (!shouldStop())
 			{
 				robot.updateState();
 				curPose = perceptor.getCorrectedPose();
 				
 				tryPollSonars();
 				tryDescendPose();		
 				transferCharge();
 				updateGraphics(contBotList);
 				
 				// end the task if requested
 				if (curSubtask == Subtask.END_TASK){
 					break;
 				}
 				
 				if (HAS_PARTNER) {
 					try {
 						executeMessage();
 					} catch (CommException e) {
 						e.printStackTrace();
 						System.exit(1);
 					}
 				}
 				
 				switch (curSubtask) {					
 				case START:
 					if (inCharge) {
 						start();
 					}
 					break;
 					
 				case FOLLOWPATH_GOLD_CELL:
 				case FOLLOWPATH_DROP_CELL:
 					followPath();
 					break;
 				
 				case TURNTO_GOLD:
 				case TURNTO_GOLD_CHECK:
 				case TURNTO_PATH:
 				case TURNTO_DROP:
 					turnTo();
 					break;
 					
 				case GOTO_GOLD_WALL:
 				case GO_FROM_GOLD_WALL:
 					goTo();
 					break;
 					
 				case DROP_GOLD:
 					// handle this inline since it's so simple
 					if (System.currentTimeMillis() - dropStartTime > 5000) {
 						transitionTo(Subtask.START);
 					}
 					break;
 				}
 				
 				try {
 					Thread.sleep(50);
 				} catch(InterruptedException iex) {
 					System.err.println("michael phelps interrupted");
 				}
 			}
 
 			robot.turnSonarsOff();
 		}
 		
 		private void fakeWallsAroundWaypoints(List<MazeState> waypoints) {
 			for (MazeState s : waypoints) {
 				fakeWallsAround(s);
 			}
 		}
 		
 		private void fakeWallsAround (MazeState s) {
 			int x = s.x();
 			int y = s.y();
 			int dir = s.dir().ordinal();
 			for (int i = 0; i < 4; i++) {
 				fakeWall(new MazeState(x, y, Direction.values()[(dir + i) % 4]));
 			}
 		}
 		
 		private void clearFakeWall (MazeState s) {
 			if (!HAS_PARTNER) {
 				return;
 			}
 			messaging.sendAction(Messaging.Action.REMOVE_WALL, s);
 			fakeWalls.remove(s);
 		}
 		
 		private void clearFakeWalls () {
 			for (MazeState s : fakeWalls) {
 				clearFakeWall(s);
 			}
 		}
 		
 		private void clearFakeWallsAround (MazeState s) {
 			int x = s.x();
 			int y = s.y();
 			Direction dir = s.dir();
 			do {
 				MazeState wall = new MazeState(x, y, dir);
 				clearFakeWall(wall);
 				dir = dir.left();
 			} while (dir != s.dir());
 		}
 		
 		private void fakeWall(MazeState s) {
 			if (!HAS_PARTNER) {
 				return;
 			}
 			if (fakeWalls.contains(s)) {
 				return;
 			}
 			else if (originalMazeWorld.isWall(s.x(), s.y(), s.dir())) {
 				return;
 			}
 			else {
 				fakeWalls.add(s);
 				messaging.sendAction(Messaging.Action.ADD_WALL, s);
 			}
 		}
 		
 		private void transitionTo (Subtask t) {
 			curSubtask = t;
 			
 			// debug output
 			System.err.println("-> " + t);
 			
 			// play sound clips and send actions to partner
 			//*
 			switch (t) {				
 			case TURNTO_DROP:
 				hasGold = false;
 				mazeWorld.removeDrop(goalState);
 				mazeWorld.removeAllInits();
 				mazeWorld.addInit(goalState);
 				fakeWallsAround(goalState);
 				break;
 				
 			case DROP_GOLD: 
 				// skrillex
 				//SoundExample.playClips("DropItLonger.wav");
 				
 				// snoop dogg
 				SoundExample.playClips("snoopdrop.wav");
 				break;
 				
 			case FOLLOWPATH_GOLD_CELL:
 			case FOLLOWPATH_DROP_CELL:
 				currentBitchClip = moveBitchClip;
 				lastState = MazeLocalizer.fromWorldToMazeState(curPose);
 				if (t == Subtask.FOLLOWPATH_GOLD_CELL) {
 					SoundExample.playClips("speed.wav");
 				}
 				break;
 				
 			case TURNTO_PATH:
 				if (hasGold) {
 					SoundExample.playClips("success.wav speed.wav");
 				}
 				break;
 			
 			case TURNTO_GOLD:
 				SoundExample.playClips("golddigga.wav");
 				fakeWallsAround(goalState);
 				break;
 			}
 			//*/
 		}
 		
 		private void start() {
 			if (mazeWorld.getFreeGolds().isEmpty()) {
 				if (HAS_PARTNER) {
 					messaging.sendAction(Messaging.Action.TAKE_CHARGE, null);
 				}
 				controller.stop();
 				robot.turnSonarsOff();
 			}
 			else {
 				setupPathHelper();
 			}
 		}
 		
 		// should only be called if we're inCharge
 		private void setupPathHelper () {
 			if (!inCharge) {
 				speech.speak("not in charge");
 			}
 			
 			//System.err.println("setupPathHelper: hasGold = " + hasGold);
 			java.util.List<RealPose2D> poses;
 			// find next gold
 			List<MazeState> states = new MazeSolver(mazeWorld).findPath(!hasGold);
 			
 			// if we didn't find a path, return
 			// will get called again once we're inCharge
 			if (states == null) {
 				if (playMoveBitch) {
 					playMoveBitch = false;
 					SoundExample.playClips(currentBitchClip);
 					currentBitchClip = currentBitchClip == moveBitchClip
 							? getOutTheWayClip
 							: moveBitchClip;
 				}
 				return;
 			}
 			
 			fakeWallsAroundWaypoints(states);
 			String commands = MazeSolver.statesToCommandsString(states);
 			System.out.println(commands);
 			goalState = states.get(states.size() - 1);
 			poses = MazeLocalizer.statesToPoses(states);
 			setDesiredPath(poses, maxDeviation);
 			
 			// set up for TURNTO the first pose orientation
 			destAngle = poses.get(0).getTh();
 			transitionTo(Subtask.TURNTO_PATH);
 
 			// remove destination from partner's golds or drops
 			if (HAS_PARTNER) {
 				if (hasGold) {
 					// remove drop
 					messaging.sendAction(Messaging.Action.REMOVE_DROP, goalState);
 				}
 				else {
 					// remove gold
 					messaging.sendAction(Messaging.Action.REMOVE_GOLD, goalState);
 				}
 			}
 		}
 		
 		private void setupGotoHelper (double xOffset) {
 			this.xOffset = xOffset;
 			startPoint = curPose.getPosition();
 			System.out.println("setupGotoHelper: xOffset = " + xOffset);
 //			robotStartedHere = curPose;
 //			curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 //			lastPoseRelStart = curPoseRelStart;
 			
 			double curTime = System.nanoTime();
 			lastTime = curTime;
 		}
 		
 		private void goTo() {
 			/*
 			final double Kp = 1.5;
 			final double Kd = 15;
 			lastPoseRelStart = curPoseRelStart;
 			lastTime = curTime;
 			curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			curTime = System.currentTimeMillis();
 			double distanceErr = destPointRelStart.getX() - curPoseRelStart.getX();
 			
 			final double DISTANCE_TOLERANCE = 0.01;
 			final double SPEED_TOLERANCE = 0.01;
 			
 			double distanceTraveled = curPoseRelStart.getX() - lastPoseRelStart.getX();
 			*/
 			
 			double distanceErr = xOffset - signum(xOffset) * curPose.getPosition().distance(startPoint);
 
 			// transition to next state
 			if (signum(distanceErr) != signum(xOffset)) {
 						//&& abs(robot.getVelLeft()) < SPEED_TOLERANCE) {
 				controller.stop();
 				// transition to next state
 				if (perceptor.getSpeed() == 0) {
 					//System.err.println("finished goto: " + distanceTraveled);
 					switch (curSubtask) {
 					case GOTO_GOLD_WALL:
 						setupGotoHelper(-xOffset); //was -IDEAL_GOLD_OFFSET
 						transitionTo(Subtask.GO_FROM_GOLD_WALL);
 						break;
 								
 					case GO_FROM_GOLD_WALL:
 						// figure out which way to turn to get to a drop,
 						// and start turning in that direction
 						mazeWorld.removeAllInits();
 						mazeWorld.addInit(goalState);
 						List<MazeState> states = new MazeSolver(mazeWorld).findPath(false);
 						String commands = MazeSolver.statesToCommandsString(states);
 						double turnAngle = AngleMath.calculateTurnAngle(commands);
 						System.out.printf("gold check turn angle: %.2f, path: %s \n", turnAngle, commands);
 						destAngle = Angle.normalize(curPose.getTh() + turnAngle);
 						transitionTo(Subtask.TURNTO_GOLD_CHECK);
 						break;
 					}
 				}
 				return;
 			}
 			
 			// don't do fancy shit
 			/*
 			boolean progressMade = signum(distanceTraveled) == signum(distanceErr);
 
 			double pterm = Kp*distanceErr;
 			double dterm = Kd*(abs(distanceTraveled))/(curTime-lastTime);
 			if (progressMade) {
 				dterm *= -1;
 			}
 			//System.err.println("pterm = " + pterm + "\ndterm = " + dterm + "\n");
 			double u = pterm + dterm;
 			//double speed = u;
 			double speed = pterm;
 			if(speed > 0.1) //cap the speed since were going short distance
 				speed = 0.1;
 			 */
 			double speed = signum(xOffset)/10;
 			controller.setVel(speed, speed);
 		}
 		
 		private void turnTo() {
 			final double Kp = .4;
 			final double Kd = 95;
 			
 			lastAngle = curAngle;
 			lastTime = curTime;
 			curAngle = Angle.normalize(curPose.getTh());
 			//curTime = System.nanoTime();
 			curTime = System.currentTimeMillis();
 			double angleErr = Angle.normalize(destAngle - curAngle);
 
 			// stop and transition if we're close enough
 			if (abs(angleErr) < ANGLE_TOLERANCE
 					&& abs(robot.getVelLeft()) < SPEED_TOLERANCE) {
 				controller.stop();
 				// transition to next state
 				if (perceptor.getSpeed() == 0) {
 					switch (curSubtask) {
 					case TURNTO_PATH:
 							if (hasGold) {
 								transitionTo(Subtask.FOLLOWPATH_DROP_CELL);
 							}
 							else {
 								transitionTo(Subtask.FOLLOWPATH_GOLD_CELL);
 							}
 						break;
 						
 					case TURNTO_GOLD:
 						double[] actualOffset = new double[3];
 						if(checkForBlue(cam)) {
 							// TODO get this working right
 							// technique 1
 							actualOffset[0] = 0.1;
 							
 							//* technique 2
 							RealPose2D goalPose = MazeLocalizer.mazeStateToWorldPose(goalState);
 							Point2D idealGoalPoint = goalPose.inverseTransform(new Point2D.Double(IDEAL_GOLD_OFFSET, 0), null);
 							actualOffset[1] = curPose.transform(idealGoalPoint, null).getX();
 							//*/
 						
 							// technique 3
 							actualOffset[2] = directSonarReadings[0] - MAGNET_DISTANCE;
 							//System.out.println("first sonar: " + directSonarReadings[0]);
 							robot.updateState();
 							actualOffset[2] = actualOffset[2] + directSonarReadings[0] - MAGNET_DISTANCE;
 							//System.out.println("second sonar: " + directSonarReadings[0]);
 							robot.updateState();
 							actualOffset[2] = (actualOffset[2] + directSonarReadings[0] - MAGNET_DISTANCE)/3;
 							//System.out.println("third sonar: " + directSonarReadings[0]);
 							
 							/*
 							for (int i = 0; i < actualOffset.length; i++) {
 								System.out.printf("actualOffset[%d] = %.2f \n", i, actualOffset[i]);
 							}
 							//*/
 							
 							// change index to change technique
 							double chosenOffset;
 							if (retryGoto) {
 								chosenOffset = -xOffset;
 								chosenOffset += RETRY_OFFSET;
 							}
 							else {
 								chosenOffset = actualOffset[0];
 							}
 
 							// clamp offset to work around shitty sonars/calculations
 							chosenOffset = max(chosenOffset, 0);
 							chosenOffset = min(chosenOffset, WALL_METERS/5);
 							setupGotoHelper(chosenOffset);
 							
 							//speech.speak("found, picking up");
 							transitionTo(Subtask.GOTO_GOLD_WALL);
 						}
 						else
 						{
 							mazeWorld.removeGold(goalState);
 							mazeWorld.removeAllInits();
 							mazeWorld.addInit(goalState);
 							//speech.speak("not found");
 							transitionTo(Subtask.START);
 						}
 						break;
 						
 					case TURNTO_GOLD_CHECK:
 						if (inCharge) {
 							if (checkForBlue(cam)) {
 								hasGold = true;
 								mazeWorld.removeGold(goalState);
 								mazeWorld.removeAllInits();
 								mazeWorld.addInit(goalState);
 								//speech.speak("success, moving");
 								setupPathHelper();
 								retryGoto = false;
 							}
 							else {
 								retryGoto = true;
 								// same conversation as MazeLocalizer.mazeStateToWorldPose
 								destAngle = goalState.dir().ordinal() * PI/2;
 								transitionTo(Subtask.TURNTO_GOLD);
 								/*
 								hasGold = false;
 								mazeWorld.removeGold(goalState);
 								mazeWorld.removeAllInits();
 								mazeWorld.addInit(goalState);
 								goalState = null;
 								//speech.speak("failure, skipping");
 								transitionTo(Subtask.START);
 								*/
 							}
 						}
 						break;
 						
 					case TURNTO_DROP:
 						// mark current time
 						dropStartTime = System.currentTimeMillis();
 						transitionTo(Subtask.DROP_GOLD);
 						break;
 					}
 				}
 			}
 			// if not close enough, keep turning
 			else {
 				double angleTraveled = Angle.normalize(curAngle - lastAngle);
 				boolean progressMade = signum(angleTraveled) == signum(angleErr);
 	
 				double pterm = Kp*abs(angleErr);
 				double dterm = Kd*(abs(angleTraveled))/(curTime-lastTime);
 				if (progressMade) {
 					dterm *= -1;
 				}
 				//System.err.println("pterm = " + pterm + "\ndterm = " + dterm + "\n");
 				double u = pterm + dterm;
 				double direction = signum(angleErr);
 				//System.err.println(angleErr + " off, speed = " + u*direction);
 				double speed = direction*u;
 				controller.setVel(-speed, speed);
 			}
 		}
 		
 		private void followPath() {
 			// try to clean up behind self
 			MazeState curState = MazeLocalizer.fromWorldToMazeState(curPose);
 			if (lastState != curState) {
 				clearFakeWallsAround(lastState);
 				lastState = curState;
 			}
 			
 			final double Kp = 1;// switch between 1.2 and 2
 			RealPose2D correctedPoseRelStart = new RealPose2D(perceptor.getCorrectedPose().getX() - CELL_RADIUS, perceptor.getCorrectedPose().getY() - CELL_RADIUS, perceptor.getCorrectedPose().getTh());
 			RealPoint2D tmp = new RealPoint2D();
 			int segment = Lookahead.findLookaheadPoint(pathSegments, correctedPoseRelStart.getPosition(), LOOKAHEAD_DISTANCE, tmp);
 			lookaheadPointRelWorld = tmp;
 			Point2D destPointRelCur = correctedPoseRelStart.inverseTransform(tmp, null);
 			// transition to something if we're approaching the end of the path
 			double distToEnd = destPointRelCur.distance(new Point2D.Double());
 			if (stopping || pathSegments.size() == 0 || (segment == pathSegments.size() - 1
 					&& distToEnd < 0.80*perceptor.getSpeed() || distToEnd < 0.05)) {
 				//stopping = true;
 				controller.stop();
 				if (perceptor.getSpeed() == 0) {
 					clearFakeWalls();
 					stopping = false;
 					// same conversation as MazeLocalizer.mazeStateToWorldPose
 					destAngle = goalState.dir().ordinal() * PI/2;
 					if (hasGold) {
 						transitionTo(Subtask.TURNTO_DROP);
 					}
 					else {
 						//speech.speak("searching");
 						transitionTo(Subtask.TURNTO_GOLD);
 					}
 				}
 			}
 			else {
 				double[] arcInfo = calculateArc(destPointRelCur);
 				controller.setCurvVel(1.0/arcInfo[0], Kp*arcInfo[1]);
 			}
 		}
 		
 		private void executeMessage() throws CommException
 		{
 			String m;
 			while ((m = comm.getIncomingMessage()) != null) {
 				Messaging.Action doThis = Messaging.Action.values()[Integer.parseInt(m)];
 				switch (doThis) {					
 				case REMOVE_DROP:
 					MazeState drop = messaging.receiveMazeState();
 					mazeWorld.removeDrop(drop);
 					break;
 					
 				case REMOVE_GOLD:
 					MazeState gold = messaging.receiveMazeState();
 					mazeWorld.removeGold(gold);
 					break;
 					
 				case ADD_WALL:
 					MazeState wall = messaging.receiveMazeState();
 					mazeWorld.addWall(wall.x(), wall.y(), wall.dir());
 					break;
 					
 				case REMOVE_WALL:
 					MazeState badWall = messaging.receiveMazeState();
 					mazeWorld.removeWall(badWall.x(), badWall.y(), badWall.dir());
 					break;
 					
 				case TAKE_CHARGE:
 					inCharge = true;
 					playMoveBitch = true;
 					inChargeSince = System.currentTimeMillis();
 					//speech.speak("me");
 					break;
 				}
 			}
 		}
 
 		// read hits from sonars and add to buffers
 		private void tryPollSonars () {
 			if (curPose.getPosition().distance(lastPollPosition.getPosition()) > POLL_INTERVAL) {
 				lastPollPosition = curPose;
 				//System.err.println("tryPollSonars");
 				robot.getSonars(directSonarReadings);
 				RealPoint2D[] sonarPointsBuffer = perceptor.getSonarObstacles();
 				for (int i=0; i<sonarPointsBuffer.length; i++) {
 					// only use the sonar readings that are within a certain distance
 					if (directSonarReadings[i] < 2.0 ) {
 						pointsBuffer.add(correctedLocalizer.transformInitToWorld(perceptor.getCorrectedPose().transform(sonarPointsBuffer[i], null)));
 					}
 				}
 			}
 		}
 		
 		// Run gradient descent and correct the position of the robot in the maze based on the sonars and the walls		
 		private void tryDescendPose () {
 			if (curPose.getPosition().distance(lastGradientPosition.getPosition()) > GRADIENT_INTERVAL) {
 				//System.err.println("tryDescendPose");
 				ErrorFunction fitter = new WallPointFitter(pointsBuffer, curPose);
 				double[] coords = new double[]{curPose.getX(), curPose.getY(), curPose.getTh()};
 				GradientDescent.descend(fitter, coords);
 				
 				curPose.setPose(coords[0], coords[1], coords[2]);
 				lastGradientPosition = curPose;
 				lastPollPosition = curPose;
 				
 				perceptor.setCorrectedPose(curPose);
 				
 				// Clear the points buffer
 				pointsBuffer.clear();
 			}
 		}
 		
 		private void transferCharge() {
 			if (!HAS_PARTNER) {
 				return;
 			}
 			
 			double time = System.currentTimeMillis();
 			if (inCharge) {
 				// transfer control if our time is up
 				if (time - IN_CHARGE_DURATION > inChargeSince) {
 					messaging.sendAction(Messaging.Action.TAKE_CHARGE, null);
 					inCharge = false;
 				}
 			} else if (time - 5*IN_CHARGE_DURATION > inChargeSince) {
 				// assume partner is AWOL, go to single player mode
 				HAS_PARTNER = false;
 				inCharge = true;
 				speech.speak("partner awol");
 			}
 		}
 		
 		private void updateGraphics (java.util.List<ContRobot> contBotList) {
 			RealPose2D correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 			RealPose2D rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 			
 			synchronized(contBotList) {
 				contBotList.get(0).pose.setPose(rawPosition.getX(), rawPosition.getY(), rawPosition.getTh()*PI/2);
 				contBotList.get(1).pose.setPose(correctedPosition.getX(), correctedPosition.getY(), correctedPosition.getTh()*PI/2);
 				if (lookaheadPointRelWorld != null) {
 					// use this to debug lookahead if needed
 					//contBotList.get(2).pose.setPose(lookaheadPointRelWorld.getX(), lookaheadPointRelWorld.getY(), 0);
 				}
 			}
 
 			mazeGraphics.setContRobots(contBotList);
 			mazeGraphics.repaint();
 		}
 
 		public String toString() {
 			return "michael phelps task";
 		}
 	}
 	
 	private boolean isPixelBlue (int[] pixel) {
 		//*
 		// kory's technique
 		int red = pixel[UsbCamera.RED];
 		int blue = pixel[UsbCamera.BLUE];
 		int green = pixel[UsbCamera.GREEN];
 		
 //		int margin = 5;
 		
 		//return (blue - margin > red) && (blue - margin > green);
 		return (blue + green/2 > 2*red);
 		
 		/*
 		// chromaticity technique
 		double nr = ((double)pixel[UsbCamera.RED])/(pixel[UsbCamera.RED] + pixel[UsbCamera.BLUE] + pixel[UsbCamera.GREEN]);
 		double nb = ((double)pixel[UsbCamera.BLUE])/(pixel[UsbCamera.RED] + pixel[UsbCamera.BLUE] + pixel[UsbCamera.GREEN]);
 		double ng = ((double)pixel[UsbCamera.GREEN])/(pixel[UsbCamera.RED] + pixel[UsbCamera.BLUE] + pixel[UsbCamera.GREEN]);
 		
 		// constants experimentally determined
 		return .236 < nr && nr < .359
 		    && .312 < ng && ng < .367;
 		    //try both together?
 		    //&& (blue - margin > red) && (blue - margin > green);
 		//*/
 	}
 	
 	public boolean checkForBlue(UsbCamera cam)
 	{
 		/* Display the window */
 		int xSize = cam.getXSize();
 		int ySize = cam.getYSize();
 		
 		int goldSum = 0;
 		int sum = 0;
 		
 		cam.snap();
 		cam.snap();
 		
 		BufferedImage feedbackImage = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
 		
 		for(int x = 0; x < xSize; x++)
 		{
 			for(int y = 0; y < ySize; y++)
 			{
 				sum++;
 				int[] pixel = cam.getPixel(x, y);
 				if (isPixelBlue(pixel))
 				{
 					goldSum++;
 					feedbackImage.setRGB(x,y,Integer.MAX_VALUE);
 				}
 				else {
 					feedbackImage.setRGB(x,y,0);
 				}
 			}
 		}
 		
 		cameraCanvas.setImage(cam.getImage());
 		cameraCanvas.repaint();
 		cameraFrame.repaint();
 		
 		feedbackCanvas.setImage(feedbackImage);
 		feedbackCanvas.repaint();
 		feedbackFrame.repaint();
 		
 		if((double)goldSum/sum > 0.1) {
 			System.out.println(/*i + */ ": yes & goldsum:"+goldSum+" sum:"+sum);
 			return true;
 		}
 		else {
 			System.out.println(/*i + */ ": no & goldsum:"+goldSum+" sum:"+sum);
 			return false;
 		}
 	}
 	
 	private static final long serialVersionUID = 0;
 }
