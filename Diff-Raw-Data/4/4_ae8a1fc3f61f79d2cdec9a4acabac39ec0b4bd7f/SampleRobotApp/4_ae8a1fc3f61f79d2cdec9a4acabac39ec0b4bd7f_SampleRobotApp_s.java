 // XXX TODO add in error correction, return-to-path functionality,
 // and end of followpath behavior
 
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
 
 import java.io.*;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.*;
 
 import edu.cmu.ri.mrpl.*;
 import edu.cmu.ri.mrpl.CommClient.CommException;
 import edu.cmu.ri.mrpl.Robot;
 import edu.cmu.ri.mrpl.comm.Messaging;
 import edu.cmu.ri.mrpl.kinematics2D.Angle;
 import edu.cmu.ri.mrpl.kinematics2D.RealPoint2D;
 import edu.cmu.ri.mrpl.kinematics2D.RealPose2D;
 import edu.cmu.ri.mrpl.maze.MazeGraphics;
 import edu.cmu.ri.mrpl.maze.MazeGraphics.ContRobot;
 import edu.cmu.ri.mrpl.maze.MazeLocalizer;
 import static edu.cmu.ri.mrpl.maze.MazeLocalizer.*;
 import edu.cmu.ri.mrpl.maze.MazeRobot;
 import edu.cmu.ri.mrpl.maze.MazeSolver;
 import edu.cmu.ri.mrpl.maze.MazeState;
 import edu.cmu.ri.mrpl.maze.MazeWorld;
 import edu.cmu.ri.mrpl.maze.MazeWorld.Direction;
 import edu.cmu.ri.mrpl.usbCamera.PicCanvas;
 import edu.cmu.ri.mrpl.usbCamera.UsbCamera;
 import edu.cmu.ri.mrpl.util.AngleMath;
 import edu.cmu.ri.mrpl.util.Cooperation;
 import edu.cmu.ri.mrpl.util.GradientDescent;
 import edu.cmu.ri.mrpl.util.GradientDescent.WallPointFitter;
 import edu.cmu.ri.mrpl.util.Lookahead;
 import edu.cmu.ri.mrpl.util.MazeFileChooser;
 import edu.cmu.ri.mrpl.util.RingBuffer;
 import edu.cmu.ri.mrpl.util.GradientDescent.ErrorFunction;
 import static java.lang.Math.*;
 import static edu.cmu.ri.mrpl.RobotModel.*;
 
 public class SampleRobotApp extends JFrame implements ActionListener, TaskController {
 
 	public static final boolean HAS_PARTNER = true;
 	public static final boolean IS_FIRST_PARTNER = true;
 	
 	private Robot robot;
 	private SonarConsole sc;
 
 	private JFrame scFrame;
 
 	private JButton connectButton;
 	private JButton disconnectButton;
 
 	private JButton pauseButton;
 	private JButton waitButton;
 	private JButton turnToButton;
 	private JButton goToButton;
 	private JButton poseToButton;
 	private JFormattedTextField argumentField;
 	private JTextField remainingField;
 	private JFormattedTextField xField;
 	private JFormattedTextField yField;
 	private JFormattedTextField thField;
 
 	private JButton stopButton;
 	private JButton quitButton;
 	private JButton executeButton;
 	private JButton loadMazeButton;
 	private JButton solveMazeButton;
 
 	private java.util.LinkedList<Task> upcomingTasks;
 
 	static final int DEFAULT_ROOM_SIZE = 4;
 
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
 
 		pauseButton = new JButton("Test camera");
 		waitButton = new JButton("Setup comms");
 		turnToButton = new JButton("GOOOOOO"); // was "Turn to angle!"
 		goToButton = new JButton("Go to distance!");
 		poseToButton = new JButton("twitch..."); // was "Go to pose!"
 		argumentField = new JFormattedTextField(NumberFormat.getInstance());
 		argumentField.setValue(1);
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMaximumFractionDigits(2);
 		remainingField = new JTextField();
 		remainingField.setEditable(false);
 		xField = new JFormattedTextField(NumberFormat.getInstance());
 		xField.setValue(1);
 		yField = new JFormattedTextField(NumberFormat.getInstance());
 		yField.setValue(1);
 		thField = new JFormattedTextField(NumberFormat.getInstance());
 		thField.setValue(1);
 		stopButton = new JButton(">> stop <<");
 		quitButton = new JButton(">> quit <<");
 		executeButton = new JButton("Execute File");
 		loadMazeButton = new JButton("Load Maze File");
 		solveMazeButton = new JButton("Michael Phelps");
 
 		connectButton.addActionListener(this);
 		disconnectButton.addActionListener(this);
 
 		pauseButton.addActionListener(this);
 		waitButton.addActionListener(this);
 		turnToButton.addActionListener(this);
 		goToButton.addActionListener(this);
 		poseToButton.addActionListener(this);
 		stopButton.addActionListener(this);
 		quitButton.addActionListener(this);
 		executeButton.addActionListener(this);
 		loadMazeButton.addActionListener(this);
 		solveMazeButton.addActionListener(this);
 
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
 		box.add(pauseButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(waitButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(turnToButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(goToButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(poseToButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(executeButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(loadMazeButton);
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(solveMazeButton);
 		
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(argumentField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(remainingField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(xField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(yField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(thField);
 		box.add(Box.createHorizontalStrut(30));
 
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
 
 		double argument = Double.parseDouble(argumentField.getText());
 		double x = Double.parseDouble(xField.getText());
 		double y = Double.parseDouble(yField.getText());
 		double th = Double.parseDouble(thField.getText());
 
 		if (source==connectButton) {
 			connect();
			solveMazeButton.requestFocusInWindow();
 		} else if ( source==disconnectButton ) {
 			disconnect();
 		} else if ( source==stopButton ) {
 			stop();
 		} else if ( source==quitButton ) {
 			quit();
 		} else if ( source==pauseButton ) {
 //			upcomingTasks.add(new PauseTask(this));
 //			startUpcomingTasks();
 			UsbCamera cam = UsbCamera.getInstance();
 			
 			System.out.println("blue: "+checkForBlue(cam));
 		} else if ( source==waitButton ) {
 //			upcomingTasks.add(new WaitTask(this, argument));
 //			startUpcomingTasks();
 			comm = new CommClient("128.237.236.81");
 			
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
 							//Again, anything except freinds-ready is not handled.
 							System.err.println("Giving up");
 							return;
 						}
 					}
 				}
 			}
 			messaging = new Messaging(comm, myFriends[0]);
 		} else if ( source==turnToButton ) {
 			//upcomingTasks.add(new TurnToTask(this, argument));
 			//startUpcomingTasks();
 			timeToGo = true;
 		} else if ( source==goToButton ) {
 			upcomingTasks.add(new GoToTask(this, argument));
 			startUpcomingTasks();
 		} else if (source == poseToButton) {
 			upcomingTasks.add(new PoseToTask(this, x, y, th));
 			startUpcomingTasks();
 		} else if ( source==executeButton ) {
 			addFileTasksToQueue();
 		} else if ( source==loadMazeButton ) {
 			JFileChooser chooser = new JFileChooser();
 			int returnVal = chooser.showOpenDialog(this);
 			if(returnVal == JFileChooser.APPROVE_OPTION) {
 				System.out.println("You chose to open this file: " +
 						chooser.getSelectedFile().getName());
 			}
 			try {
 				upcomingTasks.add(new DrawMazeTask(this, chooser.getSelectedFile().getPath()));
 			} catch (IOException e1) {
 				System.err.println("Couldn't read maze file");
 			}
 
 			startUpcomingTasks();
 		} else if ( source==solveMazeButton ) {
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
 					
 					// get initial heading correct
 					int i = 0;
 					double theta = 0;
 					while (i > -1) {
 						switch (commands.charAt(i++)) {
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
 					
 					// follow the path
 					// TODO switched these for testing
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
 			}
 		}
 	}
 
 	public void addFileTasksToQueue() {
 		JFileChooser chooser = new JFileChooser();
 		int returnVal = chooser.showOpenDialog(this);
 		if(returnVal == JFileChooser.APPROVE_OPTION) {
 			System.out.println("You chose to open this file: " +
 					chooser.getSelectedFile().getName());
 		}
 
 		CommandSequence commandSequence = new CommandSequence();
 
 		try {
 			commandSequence.readFile(chooser.getSelectedFile().getPath());
 		} catch (IOException e) {
 			System.err.println("Couldn't read file");
 		}
 
 		executeActionList(commandSequence);
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
 
 	public void executeActionList (ArrayList<Command> commands) {
 		for (Command cmd : commands) {
 			System.err.println(cmd.type);
 			switch (cmd.type) {
 			case PAUSE:
 				upcomingTasks.add(new PauseTask(this));
 				break;
 
 			case TURNTO:
 				upcomingTasks.add(new TurnToTask(this,
 						((Command.AngleArgument) cmd.argument).angle.angleValue()));
 				break;
 
 			case GOTO:
 				upcomingTasks.add(new GoToTask(this,
 						((Command.LengthArgument) cmd.argument).d));
 				break;
 
 			case WAIT:
 				upcomingTasks.add(new WaitTask(this,
 						((Command.LengthArgument) cmd.argument).d));
 				break;
 
 			case POSETO:
 				RealPose2D pose = ((Command.PoseArgument) cmd.argument).pose;
 				upcomingTasks.add(new PoseToTask(this,
 						pose.getX(),
 						pose.getY(),
 						pose.getTh()));
 				break;
 			case FOLLOWPATH:
 				Path path = ((Command.PathArgument) cmd.argument).path;
 				upcomingTasks.add(new FollowPathTask(this, path, 0.1));
 				break;
 
 			default:
 				System.err.println("unimplemented Command type: " + cmd.type);
 				new Speech().speak("unimplemented Command type: " + cmd.type);
 				break;
 			}
 		}
 
 		startUpcomingTasks();
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
 			pauseButton.addKeyListener(this);
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
 			pauseButton.removeKeyListener(this);
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
 
 	class WaitTask extends Task {
 
 		Speech speech;
 		double duration;
 		private long startTime;
 
 		WaitTask(TaskController tc, double d) {
 			super(tc);
 			setDuration(d);
 		}
 
 		public void setDuration (double d) {
 			duration = d;
 		}
 
 		public void taskRun() {
 			speech = new Speech();
 			speech.speak("Waiting " + AngleMath.roundTo2(duration) + " seconds");
 			startTime = System.currentTimeMillis();
 			double elapsed = elapsedTime();
 
 			while(!shouldStop() && elapsed < duration) {
 				elapsed = elapsedTime();
 				if (elapsed >= duration) {
 					break;
 				}
 				try {
 					//remainingField.setValue(duration - elapsed);
 
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("wait sleep interrupted");
 				}
 			}
 			// final update
 			remainingField.setText(duration - elapsed + "");
 		}
 
 		private double elapsedTime() {
 			return (System.currentTimeMillis() - startTime) / 1000.0;
 		}
 
 		public String toString() {
 			return "wait task: " + duration;
 		}
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
 
 	class GoToTask extends Task {
 
 		double desiredDistance;
 		RealPose2D robotStartedHere;
 		Controller controller;
 		Perceptor perceptor;
 		Speech speech;
 
 		GoToTask(TaskController tc, double d) {
 			super(tc);
 			setDesiredDistance(d);
 		}
 
 		public void setDesiredDistance (double d) {
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			desiredDistance = d;
 		}
 
 		// setDesiredDistance should be called before calling this method
 		public void taskRun() {
 			speech = new Speech();
 			robot.updateState();
 			final double Kp = 1.5;
 			final double Kd = 15;
 			double u = 0;
 			robotStartedHere = perceptor.getWorldPose();
 			RealPose2D curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			RealPose2D lastPoseRelStart = curPoseRelStart;
 			double curTime = System.nanoTime();
 			double lastTime = curTime;
 			final Point2D destPointRelStart = new Point2D.Double(desiredDistance, 0);
 			double distanceErr = destPointRelStart.getX() - curPoseRelStart.getX();
 
 			final double DISTANCE_TOLERANCE = 0.005;
 			final double SPEED_TOLERANCE = 0.01;
 
 			while(!shouldStop() &&
 					(abs(distanceErr) > DISTANCE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				robot.updateState();
 				lastPoseRelStart = curPoseRelStart;
 				lastTime = curTime;
 				curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 				curTime = System.currentTimeMillis();
 				distanceErr = destPointRelStart.getX() - curPoseRelStart.getX();
 
 				double distanceTraveled = curPoseRelStart.getX() - lastPoseRelStart.getX();
 				boolean progressMade = signum(distanceTraveled) == signum(distanceErr);
 
 				double pterm = Kp*distanceErr;
 				double dterm = Kd*(abs(distanceTraveled))/(curTime-lastTime);
 				if (progressMade) {
 					dterm *= -1;
 				}
 				//System.err.println("pterm = " + pterm + "\ndterm = " + dterm + "\n");
 				u = pterm + dterm;
 				double speed = u;
 				controller.setVel(speed, speed);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("go-to sleep interrupted");
 				}
 			}
 
 			speech.speak("error " + AngleMath.roundTo2(distanceErr*1000) + " millimeters");
 			controller.stop();
 		}
 
 		public String toString() {
 			return "go-to task: " + desiredDistance;
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
 
 	class PoseToTask extends Task {
 		RealPose2D destPoseRelStart;
 		Controller controller;
 		Perceptor perceptor;
 		Speech speech;
 		TaskController tc;
 
 		PoseToTask(TaskController tc, double x, double y, double th) {
 			super(tc);
 			this.tc = tc;
 			setDesiredPose(x,y,th);
 		}
 
 		public void setDesiredPose (double x, double y, double th) {
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			destPoseRelStart = new RealPose2D(x,y,th);
 		}
 
 		// setDesiredPose should be called before calling this method
 		public void taskRun() {
 			speech = new Speech();
 			robot.updateState();
 
 			final double Kp = 2;
 			//final double Kd = 10;
 
 			RealPose2D robotStartedHere = perceptor.getWorldPose();
 			RealPose2D curPoseRelStart = new RealPose2D();
 			//RealPose2D lastPoseRelStart;
 			//double curTime = System.nanoTime();
 			//double lastTime;
 
 			// calculate arc info
 			double[] arcInfo = calculateArc(destPoseRelStart.getPosition());
 			double radius = arcInfo[0];
 			double distanceErr = arcInfo[1];
 			System.err.printf("arc: radius=%.2f, distanceErr=%.2f\n", radius, distanceErr);
 
 			do {
 				// update everything
 				//	lastPoseRelStart = curPoseRelStart;
 				//lastTime = curTime;
 				robot.updateState();
 				curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 				//				curTime = System.currentTimeMillis();
 
 				// figure out how far we've been
 				//RealPose2D curPoseRelLast = RealPose2D.multiply(
 				//		lastPoseRelStart.inverse(), curPoseRelStart);
 				//double distanceTraveled = calculateArcLength(curPoseRelLast.getPosition(), radius);
 
 				// compute whether progress has been made
 				RealPose2D destPoseRelCur = RealPose2D.multiply(
 						curPoseRelStart.inverse(), destPoseRelStart);
 				distanceErr = calculateArcLength(destPoseRelCur.getPosition(), radius);
 				//boolean progressMade = signum(distanceTraveled) == signum(distanceErr);
 
 				// calculate dterm
 				/*
 				double dterm = Kd*(abs(distanceTraveled))/(curTime-lastTime);
 				if (progressMade) {
 					dterm *= -1;
 				}
 				 */
 
 				// set wheel speeds
 				double pterm = Kp*distanceErr;
 				double speed = pterm;// + dterm;
 				controller.setCurvVel(1.0/radius, speed);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("go-to sleep interrupted");
 				}
 			} while(!shouldStop() &&
 					(abs(distanceErr) > DISTANCE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE));
 
 			System.out.println("Current x = "+curPoseRelStart.getX());
 			System.out.println("Current y = "+curPoseRelStart.getY());
 			System.out.println("FINISHED X/Y. STARTING THETA");
 
 			speech.speak("remaining error " + AngleMath.roundTo2(distanceErr*1000)
 					+ " millimeters");
 
 			upcomingTasks.add(0, new TurnToTask(tc, Angle.normalize(
 					destPoseRelStart.getTh() + robotStartedHere.getTh() - robot.getHeading())));
 			controller.stop();
 		}
 
 		public String toString() {
 			return "pose-to task: " + destPoseRelStart;
 		}
 	}
 
 	class FollowPathTask extends Task {
 		ArrayList<RealPose2D> desiredPath;
 		java.util.List<Line2D> pathSegments;
 		double maxDeviation;
 		RealPose2D robotStartedHere;
 		Controller controller;
 		Perceptor perceptor;
 		Speech speech;
 
 		private final double LOOKAHEAD_DISTANCE = MazeLocalizer.WALL_METERS;
 
 		//private TaskController tc;
 
 		FollowPathTask(TaskController tc, ArrayList<RealPose2D> poses, double maxDeviation) {
 			super(tc);
 			//this.tc = tc;
 			setDesiredPath(poses, maxDeviation);
 		}
 
 		public void setDesiredPath (ArrayList<RealPose2D> poses, double maxDeviation) {
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			desiredPath = new ArrayList<RealPose2D>(poses);
 			this.maxDeviation = maxDeviation;
 
 			// construct list of line segments
 			pathSegments = Lookahead.posesToPath(poses);
 			System.err.println(desiredPath.size() + " PATH SEGMENTS");
 		}
 
 		// setDesiredPath should be called before calling this method
 		public void taskRun() {
 			robot.updateState();
 			speech = new Speech();
 			double Kp = 2;
 			//double Kd = 10;
 			//double u = 0;
 
 			robotStartedHere = perceptor.getWorldPose();
 			RealPose2D curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			RealPoint2D tmp = new RealPoint2D();
 			Lookahead.findLookaheadPoint(pathSegments, curPoseRelStart.getPosition(), LOOKAHEAD_DISTANCE, tmp);
 			Point2D destPointRelCur = robotStartedHere.inverseTransform(tmp, null);
 
 			double[] arcInfo = calculateArc(destPointRelCur);
 			double distanceErr = arcInfo[1];
 
 			while(!shouldStop() &&
 					(abs(distanceErr) > DISTANCE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				robot.updateState();
 				curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 
 				// remove the old lookahead point
 				if (robot instanceof SimRobot) {
 					((SimRobot) robot).deleteObstacle(tmp.getX(), tmp.getY());
 				}
 
 				// recalculate destination (code copied from before loop)
 				Lookahead.findLookaheadPoint(pathSegments, curPoseRelStart.getPosition(), LOOKAHEAD_DISTANCE, tmp);
 				destPointRelCur = curPoseRelStart.inverseTransform(tmp, null);
 
 				// draw the lookahead point
 				if (robot instanceof SimRobot) {
 					if (tmp.distance(perceptor.getWorldPose().getPosition()) > .25) {
 						((SimRobot) robot).addObstacle(tmp.getX(), tmp.getY(), 0.01);
 					}
 				}
 
 				arcInfo = calculateArc(destPointRelCur);
 				controller.setCurvVel(1.0/arcInfo[0], Kp*arcInfo[1]);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("go-to sleep interrupted");
 				}
 			}
 			System.out.println("Current x = "+curPoseRelStart.getX());
 			System.out.println("Current y = "+curPoseRelStart.getY());
 
 			// figure out what to do at the end of the path (turnto, etc)
 			/* copied code from TurnToTask.
 			// Check whether the "rapid transition" rule is broken here.
 			System.out.println("FINISHED X/Y. STARTING THETA");
 			Kp = .4;
 			Kd = 95;
 			u = 0;
 			robot.updateState();
 			double curAngle = Angle.normalize(robot.getHeading());
 			double lastAngle = curAngle;
 			curTime = System.nanoTime();
 			lastTime = curTime;
 			curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			double desiredAngle = Angle.normalize(desiredPose.getTh() - curPoseRelStart.getTh());
 			double destAngle = Angle.normalize(curAngle + desiredAngle);
 			//System.out.println("destAngle: " + destAngle);
 			double angleErr = Angle.normalize(destAngle - curAngle);
 
 			final double ANGLE_TOLERANCE = 0.01;
 			SPEED_TOLERANCE = 0.01;
 
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
 					System.err.println("pose-to (turn) sleep interrupted");
 				}
 			}
 
 
 			speech.speak("remaining error " + AngleMath.roundTo2(distanceErr*1000)
 					+ " millimeters");
 			//*/
 			controller.stop();
 		}
 
 		public String toString() {
 			return "follow-path task: " + desiredPath;
 		}
 	}
 
 	class DrawMazeTask extends Task {
 
 		Perceptor perceptor;
 		MazeLocalizer correctedLocalizer;
 		MazeLocalizer rawLocalizer;
 		MazeWorld mazeWorld;
 		MazeGraphics mazeGraphics;
 		MazeRobot mazeRobot;
 		JFrame wrapper;
 		
 		private static final boolean USE_SONARS = true;
 
 		DrawMazeTask(TaskController tc, String fileName) throws IOException {
 			super(tc);
 			mazeWorld = new MazeWorld(fileName);
 			mazeGraphics = new MazeGraphics(mazeWorld);
 			
 			// construct corrected localizer
 			correctedLocalizer = new MazeLocalizer(mazeWorld, false);
 			// save init
 			MazeState init = mazeWorld.getInits().iterator().next();
 			
 			if (!USE_SONARS) {
 				mazeWorld.removeAllInits();
 				mazeWorld.addInit(new MazeState(0, 0, MazeWorld.Direction.East));
 			}
 			
 			// construct uncorrected localizer
 			rawLocalizer = new MazeLocalizer(mazeWorld, true);
 		
 			if (!USE_SONARS) {
 				// replace original init
 				mazeWorld.removeAllInits();
 				mazeWorld.addInit(init);
 			}
 			
 			if (USE_SONARS) {
 				robot.turnSonarsOn();
 			}
 			
 			perceptor = new Perceptor(robot);
 			perceptor.setCorrectedPose(MazeLocalizer.mazeStateToWorldPose(init));
 			wrapper = new JFrame();
 			wrapper.add(mazeGraphics);
 			wrapper.setSize(400, 400);
 			wrapper.setVisible(true);
 		}
 
 		public void taskRun() {
 			RingBuffer<Point2D> pointsBuffer;
 			RealPoint2D[] sonarPointsBuffer;
 			double[] directSonarReadings;
 			
 			if (USE_SONARS) {
 				pointsBuffer = new RingBuffer<Point2D>(400);
 				sonarPointsBuffer = null;
 				directSonarReadings = new double[16];
 			}
 			
 			java.util.List<ContRobot> list = Collections.synchronizedList(new ArrayList<ContRobot>()); 
 			RealPose2D correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 			RealPose2D rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 			list.add(new ContRobot(rawPosition, Color.RED));
 			list.add(new ContRobot(correctedPosition, Color.GREEN));
 			
 			mazeGraphics.setContRobots(list);
 			
 			RealPose2D curPose = perceptor.getCorrectedPose();
 			RealPose2D lastPollPosition = perceptor.getCorrectedPose();
 			RealPose2D lastGradientPosition = perceptor.getCorrectedPose();
 			double pollInterval = 0.01; // meters between polling the sonars
 			double gradientInterval = 0.1; // meters between running gradient descent on points
 						
 			while(!shouldStop()) {
 				robot.updateState();
 				
 				curPose = perceptor.getCorrectedPose();
 					
 				if (USE_SONARS && curPose.getPosition().distance(lastPollPosition.getPosition())  > pollInterval) {
 					lastPollPosition = curPose;
 					
 					robot.getSonars(directSonarReadings);
 					sonarPointsBuffer = perceptor.getSonarObstacles();
 					for (int i=0; i<sonarPointsBuffer.length; i++) {
 						// only use the sonar readings that are within a certain distance
 						if (directSonarReadings[i] < 2.0 ) {
 							pointsBuffer.add(correctedLocalizer.transformInitToWorld(perceptor.getCorrectedPose().transform(sonarPointsBuffer[i], null)));
 						}
 					}
 				}
 				
 				// Every interval (.25 meters) do this
 				// Run gradient descent and correct the position of the robot in the maze based on the sonars and the walls
 				if (USE_SONARS && curPose.getPosition().distance(lastGradientPosition.getPosition()) > gradientInterval) {
 					
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
 				
 				correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 				rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 				
 				synchronized(list) {
 					list.get(0).pose.setPose(rawPosition.getX(), rawPosition.getY(), rawPosition.getTh()*PI/2);
 					list.get(1).pose.setPose(correctedPosition.getX(), correctedPosition.getY(), correctedPosition.getTh()*PI/2);
 				}
 				
 				remainingField.setText(String.format("(%.2f, %.2f, %.2f)",
 						correctedPosition.getX(), correctedPosition.getY(), (correctedPosition.getTh()+4)%4));
 				mazeGraphics.setContRobots(list);
 				mazeGraphics.repaint();
 				
 				try {
 					Thread.sleep(50);
 				} catch(InterruptedException iex) {
 					System.err.println("draw maze interrupted");
 				}
 			}
 
 			remainingField.setText("");
 			if (USE_SONARS) {
 				robot.turnSonarsOff();
 			}
 		}
 
 		public String toString() {
 			return "draw maze task";
 		}
 	}
 	
 	class SolveMazeTask extends Task {
 		// Globals from followpath
 		ArrayList<RealPose2D> desiredPath;
 		java.util.List<Line2D> pathSegments;
 		double maxDeviation;
 		RealPose2D robotStartedHere;
 		Controller controller;
 		Speech speech;
 
 		// globals from mazedrawer
 		Perceptor perceptor;
 		MazeLocalizer correctedLocalizer;
 		MazeLocalizer rawLocalizer;
 		MazeWorld mazeWorld;
 		MazeGraphics mazeGraphics;
 		MazeRobot mazeRobot;
 		JFrame wrapper;
 		
 		private final double LOOKAHEAD_DISTANCE = 0.6;
 
 		private static final boolean USE_SONARS = true;
 		
 		private TaskController tc;
 
 		SolveMazeTask(TaskController tc, double maxDeviation, String mazeFileName) {
 			super(tc);
 			this.tc = tc;
 			
 			try {
 				mazeWorld = new MazeWorld(mazeFileName);
 			} catch (IOException e) {
 				System.err.println("Couldn't read maze file!");
 			}
 			mazeGraphics = new MazeGraphics(mazeWorld);
 			
 			java.util.List<RealPose2D> poses;
 			// findPath no longer supports finding generic "goals"
 			List<MazeState> states = new MazeSolver(mazeWorld).findPath(true);
 			poses = MazeLocalizer.statesToPoses(states);
 			setDesiredPath(poses, maxDeviation);
 			
 			// construct corrected localizer
 			correctedLocalizer = new MazeLocalizer(mazeWorld, false);
 			// save init
 			MazeState init = mazeWorld.getInits().iterator().next();
 			
 			if (!USE_SONARS) {
 				mazeWorld.removeAllInits();
 				mazeWorld.addInit(new MazeState(0, 0, MazeWorld.Direction.East));
 			}
 			
 			// construct uncorrected localizer
 			rawLocalizer = new MazeLocalizer(mazeWorld, true);
 		
 			if (!USE_SONARS) {
 				// replace original init
 				mazeWorld.removeAllInits();
 				mazeWorld.addInit(init);
 			}
 			
 			if (USE_SONARS) {
 				robot.turnSonarsOn();
 			}
 			
 			perceptor = new Perceptor(robot);
 			perceptor.setCorrectedPose(MazeLocalizer.mazeStateToWorldPose(init));
 			wrapper = new JFrame();
 			wrapper.add(mazeGraphics);
 			wrapper.setSize(400, 400);
 			wrapper.setVisible(true);
 		}
 		
 		public void setDesiredPath (List<RealPose2D> poses, double maxDeviation) {
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			desiredPath = new ArrayList<RealPose2D>(poses);
 			this.maxDeviation = maxDeviation;
 
 			// construct list of line segments
 			pathSegments = Lookahead.posesToPath(poses);
 			System.err.println(desiredPath.size() + " PATH SEGMENTS");
 		}
 
 		public void taskRun() {
 			RingBuffer<Point2D> pointsBuffer;
 			RealPoint2D[] sonarPointsBuffer;
 			double[] directSonarReadings;
 			
 			robot.updateState();
 			speech = new Speech();
 			double Kp = 2;
 			//double Kd = 10;
 			//double u = 0;
 
 			robotStartedHere = perceptor.getWorldPose();
 //			RealPose2D curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 //			RealPoint2D tmp = new RealPoint2D();
 //			Lookahead.findLookaheadPoint(pathSegments, curPoseRelStart.getPosition(), LOOKAHEAD_DISTANCE, tmp);
 //			Point2D destPointRelCur = robotStartedHere.inverseTransform(tmp, null);
 
 //			double[] arcInfo = calculateArc(destPointRelCur);
 //			double distanceErr = arcInfo[1];
 			
 			if (USE_SONARS) {
 				pointsBuffer = new RingBuffer<Point2D>(400);
 				sonarPointsBuffer = null;
 				directSonarReadings = new double[16];
 			}
 			
 			java.util.List<ContRobot> list = Collections.synchronizedList(new ArrayList<ContRobot>()); 
 			RealPose2D correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 			RealPose2D rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 			list.add(new ContRobot(rawPosition, Color.RED));
 			list.add(new ContRobot(correctedPosition, Color.GREEN));
 			
 			mazeGraphics.setContRobots(list);
 			
 			RealPose2D curPose = perceptor.getCorrectedPose();
 			
 			// TODO: Look at my comment below using this same garbage...
 			RealPose2D correctedPoseRelStart = new RealPose2D(perceptor.getCorrectedPose().getX() - CELL_RADIUS, perceptor.getCorrectedPose().getY() - CELL_RADIUS, perceptor.getCorrectedPose().getTh());
 			
 			RealPose2D lastPollPosition = perceptor.getCorrectedPose();
 			RealPose2D lastGradientPosition = perceptor.getCorrectedPose();
 			
 			RealPoint2D tmp = new RealPoint2D();
 			int segment = Lookahead.findLookaheadPoint(pathSegments, correctedPoseRelStart.getPosition(), LOOKAHEAD_DISTANCE, tmp);
 			Point2D destPointRelCur = robotStartedHere.inverseTransform(tmp, null);
 			
 			double pollInterval = 0.01; // meters between polling the sonars
 			double gradientInterval = .1; // meters between running gradient descent on points
 						
 			double[] arcInfo = calculateArc(destPointRelCur);
 			double distanceErr = arcInfo[1];
 			
 			while(!shouldStop() &&
 					(abs(distanceErr) > DISTANCE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				robot.updateState();
 				curPose = perceptor.getCorrectedPose();
 					
 				if (USE_SONARS && curPose.getPosition().distance(lastPollPosition.getPosition())  > pollInterval) {
 					lastPollPosition = curPose;
 					
 					robot.getSonars(directSonarReadings);
 					sonarPointsBuffer = perceptor.getSonarObstacles();
 					for (int i=0; i<sonarPointsBuffer.length; i++) {
 						// only use the sonar readings that are within a certain distance
 						if (directSonarReadings[i] < 2.0 ) {
 							pointsBuffer.add(correctedLocalizer.transformInitToWorld(perceptor.getCorrectedPose().transform(sonarPointsBuffer[i], null)));
 						}
 					}
 				}
 				
 				// Every interval (.25 meters) do this
 				// Run gradient descent and correct the position of the robot in the maze based on the sonars and the walls
 				if (USE_SONARS && curPose.getPosition().distance(lastGradientPosition.getPosition()) > gradientInterval) {
 					
 					ErrorFunction fitter = new WallPointFitter(pointsBuffer, curPose);
 					double[] coords = new double[]{curPose.getX(), curPose.getY(), curPose.getTh()};
 					GradientDescent.descend(fitter, coords);
 					
 					curPose.setPose(coords[0], coords[1], coords[2]);
 					lastGradientPosition = curPose;
 					lastPollPosition = curPose;
 					
 					/*
 					RealPose2D uncorrected = perceptor.getCorrectedPose();
 					RealPose2D diffPose = RealPose2D.multiply(uncorrected.inverse(), curPose);
 					final double CORRECTION_FACTOR = 0.5;
 					diffPose.setPose(CORRECTION_FACTOR*diffPose.getX(), CORRECTION_FACTOR*diffPose.getY(), CORRECTION_FACTOR*diffPose.getTh());
 					RealPose2D partiallyCorrected = new RealPose2D(
 							uncorrected.getX() + diffPose.getX(),
 							uncorrected.getY() + diffPose.getY(),
 							uncorrected.getTh() + diffPose.getTh()
 					);
 					/*/
 					// toggle comments to not do smoothing
 					RealPose2D partiallyCorrected = curPose;
 					//*/
 					
 					perceptor.setCorrectedPose(partiallyCorrected);
 					
 					// Clear the points buffer
 					pointsBuffer.clear();
 				}
 				
 				correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 				rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 				
 				synchronized(list) {
 					list.get(0).pose.setPose(rawPosition.getX(), rawPosition.getY(), rawPosition.getTh()*PI/2);
 					list.get(1).pose.setPose(correctedPosition.getX(), correctedPosition.getY(), correctedPosition.getTh()*PI/2);
 				}
 				
 				remainingField.setText(String.format("(%.2f, %.2f, %.2f)",
 						correctedPosition.getX(), correctedPosition.getY(), (correctedPosition.getTh()+4)%4));
 				mazeGraphics.setContRobots(list);
 				mazeGraphics.repaint();
 				
 				// remove the old lookahead point
 				if (robot instanceof SimRobot) {
 					((SimRobot) robot).deleteObstacle(tmp.getX(), tmp.getY());
 				}
 				
 				// TODO: Make this right...so essentially what I'm doing here is subtracting half of the width of a cell from the X and Y of this correctedPose because
 				// it should have been at 0,0 when it was saying .3683, .3683
 				// It's late and I can't wrap my brain around the needed transform so I just did this and it seems to work except the robot has become "sloppy" now
 				// instead of the nice smooth runs we had in Lab 4
 				correctedPoseRelStart = new RealPose2D(perceptor.getCorrectedPose().getX() - CELL_RADIUS, perceptor.getCorrectedPose().getY() - CELL_RADIUS, perceptor.getCorrectedPose().getTh());
 
 				segment = Lookahead.findLookaheadPoint(pathSegments, correctedPoseRelStart.getPosition(), LOOKAHEAD_DISTANCE, tmp);
 				destPointRelCur = correctedPoseRelStart.inverseTransform(tmp, null);
 
 				// draw the lookahead point
 				if (robot instanceof SimRobot) {
 					if (tmp.distance(perceptor.getWorldPose().getPosition()) > .25) {
 						((SimRobot) robot).addObstacle(tmp.getX(), tmp.getY(), 0.01);
 					}
 				}
 				
 				arcInfo = calculateArc(destPointRelCur);
 				// end of path
 				if (segment == pathSegments.size() - 1
 						&& destPointRelCur.distance(new Point2D.Double()) < 0.3) {
 					controller.setVel(0, 0);
 					if (perceptor.getSpeed() == 0) {
 						double targetTheta = desiredPath.get(desiredPath.size() - 1).getTh();
 						double curTheta = correctedPoseRelStart.getTh();
 						double dTheta = Angle.normalize(targetTheta - curTheta);
 						System.out.println(targetTheta);
 						System.out.println(curTheta);
 						System.out.println(dTheta);
 						upcomingTasks.add(0, new TurnToTask(tc, dTheta));
 						break;
 					}
 				}
 				else {
 					controller.setCurvVel(1.0/arcInfo[0], Kp*arcInfo[1]);
 				}
 				
 				try {
 					Thread.sleep(50);
 				} catch(InterruptedException iex) {
 					System.err.println("draw maze interrupted");
 				}
 			}
 
 			remainingField.setText("");
 			if (USE_SONARS) {
 				robot.turnSonarsOff();
 			}
 		}
 
 		public String toString() {
 			return "draw maze task";
 		}
 	}
 
 	enum Subtask {
 		START,
 		FOLLOWPATH_GOLD_CELL,
 		GOTO_GOLD_WALL, GO_FROM_GOLD_WALL,
 		FOLLOWPATH_DROP_CELL, DROP_GOLD,
 		TURNTO_GOLD, TURNTO_GOLD_CHECK, TURNTO_PATH, TURNTO_DROP,
 		
 		WAIT_FOR_PARTNER,
 		
 		END_TASK // used to end the overall Task
 	}
 	
 	// named so because it gets all the golds
 	class MichaelPhelpsTask extends Task {
 		private RealPose2D robotStartedHere;
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
 		
 		private Subtask curSubtask = IS_FIRST_PARTNER ? Subtask.START : Subtask.WAIT_FOR_PARTNER;
 		
 		// used in Subtasks GOTO_GOLD_WALL, GO_FROM_GOLD_WALL
 		private MazeState goalState;
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
 		private RealPose2D curPoseRelStart;
 		private RealPose2D lastPoseRelStart;
 		private Point2D destPointRelStart;
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
 		private boolean inCharge = IS_FIRST_PARTNER;
 		private long inChargeSince = System.currentTimeMillis();
 		public static final long IN_CHARGE_DURATION = 1000;
 		// TODO get this working if it could be a problem
 		private boolean partnerAwol = false;
 		public static final long PARTNER_AWOL_TIMEOUT = 5*IN_CHARGE_DURATION;
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
 
 			robotStartedHere = perceptor.getWorldPose();
 			
 			// set up continuous robots on graphical maze
 			java.util.List<ContRobot> contBotList = Collections.synchronizedList(new ArrayList<ContRobot>()); 
 			RealPose2D correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 			RealPose2D rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 			contBotList.add(new ContRobot(rawPosition, Color.RED));
 			contBotList.add(new ContRobot(correctedPosition, Color.GREEN));
 			// TODO add robot used for lookahead point if needed
 			//contBotList.add(new ContRobot(new RealPose2D(0,0,0), new Color(0f, 0f, 1f, 0.5f)));
 			mazeGraphics.setContRobots(contBotList);
 			
 			curPose = perceptor.getCorrectedPose();
 			lastPollPosition = curPose;
 			lastGradientPosition = curPose;
 			
 			controller.setVel(0.1, 0.1);
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			controller.setVel(-0.1, -0.1);
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			controller.stop();
 			
 			
 			speech.speak("ready");
 			while(!timeToGo) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
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
 				trySendTakeCharge();
 				updateGraphics(contBotList);
 				
 				// end the task if requested
 				if (curSubtask == Subtask.END_TASK){
 					break;
 				}
 				
 				if (HAS_PARTNER) {
 					try {
 						executeMessage();
 					} catch (CommException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						System.exit(1);
 					}
 				}
 				
 				switch (curSubtask) {
 				case WAIT_FOR_PARTNER:
 					controller.stop();
 					break;
 					
 				case START:
 					start();
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
 					// XXX debug
 					//curSubtask = Subtask.GO_FROM_GOLD_WALL;
 					//break;
 				case GO_FROM_GOLD_WALL:
 					goTo();
 					// XXX debug
 					//destAngle = Angle.normalize(curPose.getTh() + PI/2);
 					//transitionTo(Subtask.TURNTO_GOLD_CHECK);
 					break;
 				case DROP_GOLD:
 					// handle this inline since it's so simple
 					if (System.currentTimeMillis() - dropStartTime > 5000) {
 						if (HAS_PARTNER) {
 							messaging.sendAction(Messaging.Action.GO, null);
 							transitionTo(Subtask.WAIT_FOR_PARTNER);
 						}
 						else {
 							transitionTo(Subtask.START);
 						}
 					}
 					break;
 				}
 				
 				try {
 					Thread.sleep(50);
 				} catch(InterruptedException iex) {
 					System.err.println("michael phelps interrupted");
 				}
 			}
 
 			remainingField.setText("");
 			robot.turnSonarsOff();
 		}
 		
 		private void fakeWallsAround (MazeState s) {
 			int x = s.x();
 			int y = s.y();
 			int dir = s.dir().ordinal();
 			for (int i = 0; i < 4; i++) {
 				fakeWall(new MazeState(x, y, Direction.values()[(dir + i) % 4]));
 			}
 		}
 		
 		private void clearFakeWalls () {
 			for (MazeState s : fakeWalls) {
 				messaging.sendAction(Messaging.Action.REMOVE_WALL, s);
 			}
 			fakeWalls.clear();
 		}
 		
 		private void fakeWall(MazeState s) {
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
 			// TODO add speech back in? nah....
 			//*
 			switch (t) {
 			case TURNTO_DROP:
 				hasGold = false;
 				mazeWorld.removeDrop(goalState);
 				mazeWorld.removeAllInits();
 				mazeWorld.addInit(goalState);
 				if (HAS_PARTNER) {
 					fakeWallsAround(goalState);
 				}
 				break;
 				
 			case DROP_GOLD: 
 				// skrillex
 				//SoundExample.playClips("DropItLonger.wav");
 				
 				// snoop dogg
 				SoundExample.playClips("snoopdrop.wav");
 				break;
 				
 			case FOLLOWPATH_GOLD_CELL:
 				SoundExample.playClips("speed.wav");
 				if (HAS_PARTNER) {
 					messaging.sendAction(Messaging.Action.REMOVE_GOLD, goalState);
 				}
 				break;
 				
 			case FOLLOWPATH_DROP_CELL:
 				if (HAS_PARTNER) {
 					messaging.sendAction(Messaging.Action.REMOVE_DROP, goalState);
 				}
 				break;
 				
 			case TURNTO_PATH:
 				if (hasGold) {
 					SoundExample.playClips("success.wav speed.wav");
 				}
 				break;
 			
 			case TURNTO_GOLD:
 				SoundExample.playClips("golddigga.wav");
 				break;
 			}
 			//*/
 		}
 		
 		private void start() {
 			if (mazeWorld.getFreeGolds().isEmpty()) {
 				transitionTo(Subtask.END_TASK);
 				// in order to get the partner to END_TASK
 				messaging.sendAction(Messaging.Action.GO, null);
 			}
 			else {
 				setupPathHelper();
 			}
 		}
 		
 		private void setupPathHelper () {
 			System.err.println("setupPathHelper: hasGold = " + hasGold);
 			java.util.List<RealPose2D> poses;
 			// find next gold
 			List<MazeState> states = new MazeSolver(mazeWorld).findPath(!hasGold);
 			String commands = MazeSolver.statesToCommandsString(states);
 			System.out.println(commands);
 			goalState = states.get(states.size() - 1);
 			poses = MazeLocalizer.statesToPoses(states);
 			setDesiredPath(poses, maxDeviation);
 			
 			// set up for TURNTO the first pose orientation
 			destAngle = poses.get(0).getTh();
 			transitionTo(Subtask.TURNTO_PATH);
 		}
 		
 		private void setupGotoHelper (double xOffset) {
 			this.xOffset = xOffset;
 			// XXX janky
 			startPoint = curPose.getPosition();
 			System.out.println("setupGotoHelper: xOffset = " + xOffset);
 			robotStartedHere = curPose;
 			curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			lastPoseRelStart = curPoseRelStart;
 			
 			double curTime = System.nanoTime();
 			lastTime = curTime;
 			destPointRelStart = new Point2D.Double(xOffset, 0);
 		}
 		
 		private void goTo() {
 			final double Kp = 1.5;
 			final double Kd = 15;
 //			lastPoseRelStart = curPoseRelStart;
 //			lastTime = curTime;
 //			curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 //			curTime = System.currentTimeMillis();
 //			double distanceErr = destPointRelStart.getX() - curPoseRelStart.getX();
 			double distanceErr = xOffset - signum(xOffset) * curPose.getPosition().distance(startPoint);
 			
 			final double DISTANCE_TOLERANCE = 0.01;
 			final double SPEED_TOLERANCE = 0.01;
 			
 			double distanceTraveled = curPoseRelStart.getX() - lastPoseRelStart.getX();
 
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
 						destAngle = Angle.normalize(curPose.getTh() + PI/2);
 						transitionTo(Subtask.TURNTO_GOLD_CHECK);
 						break;
 					}
 				}
 				return;
 			}
 			
 			// XXX don't do fancy shit
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
 							
 							// TODO change index to change technique
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
 							// TODO try to grab again? NOPE
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
 			final double Kp = 1;// TODO switch between 1.2 and 2
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
 				case GO:
 					clearFakeWalls();
 					transitionTo(Subtask.START);
 					break;
 					
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
 					inChargeSince = System.currentTimeMillis();
 					speech.speak("me");
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
 		
 		private void trySendTakeCharge() {
 			if (inCharge && System.currentTimeMillis() - IN_CHARGE_DURATION > inChargeSince) {
 				messaging.sendAction(Messaging.Action.TAKE_CHARGE, null);
 				inCharge = false;
 			}
 		}
 		
 		private void updateGraphics (java.util.List<ContRobot> contBotList) {
 			RealPose2D correctedPosition = correctedLocalizer.fromInitToCell(perceptor.getCorrectedPose());
 			RealPose2D rawPosition = rawLocalizer.fromInitToCell(perceptor.getWorldPose());
 			
 			synchronized(contBotList) {
 				contBotList.get(0).pose.setPose(rawPosition.getX(), rawPosition.getY(), rawPosition.getTh()*PI/2);
 				contBotList.get(1).pose.setPose(correctedPosition.getX(), correctedPosition.getY(), correctedPosition.getTh()*PI/2);
 				if (lookaheadPointRelWorld != null) {
 					// TODO use this to debug lookahead if needed
 					//contBotList.get(2).pose.setPose(lookaheadPointRelWorld.getX(), lookaheadPointRelWorld.getY(), 0);
 				}
 			}
 			
 			remainingField.setText(String.format("(%.2f, %.2f, %.2f)",
 					correctedPosition.getX(), correctedPosition.getY(), (correctedPosition.getTh()+4)%4));
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
 		
 		int margin = 5;
 		
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
