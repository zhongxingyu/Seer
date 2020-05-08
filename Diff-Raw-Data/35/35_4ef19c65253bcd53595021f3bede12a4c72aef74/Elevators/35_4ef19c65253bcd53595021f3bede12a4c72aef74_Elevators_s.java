 package elevator;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.net.*;
 import java.io.*;
 
 import controller.MasterController;
 
 /**
  * Title:        Green Elevator
  * Description:  Green Elevator, 2G1915
  * Copyright:    Copyright (c) 2001
  * Company:      IMIT/KTH
  * @author Vlad Vlassov
  * @version 1.0
  */
 /**
  * The primary class of the Elevators application written in Java that simulates
  * elevators (motors, doors, scales, inside button panels and floor buttons).
  * <p>The application is constructed with the MVC (Model-View-Controller)
  * architecture. According to the FOLDOC dictionary, MVC is a way of partitioning
  * the design of interactive software. The "Model" is the internal workings of the program
  * (e.g., state of motors, doors, scales and buttons), the "View" is
  * how the user sees the state of the model (Elevators GUI, I/O and RMI),
  * and the "Controller" is how the user changes the state or provides input.
  * <p>The application provide the "View" and the "Model" parts. The "controller"
  * part can be connected to the application via standard input/output, TCP sockets
  * or/and Java RMI.
  * <p>The application performs an animation of elevators at a predefined rate
  * set to the application Timer which starts after the application GUI ("view") is created.
  * The Timer clocks the animation. On each time step, the "view" part of the
  * application inspects a state of the "model" part (elevator positions, direction
  * of movement, door sates, value of scales) and displays a new state if the state
  * of the model has changed since the previous time step.
  * <p>The "controller" part is not provided except of some input to the controller:
  * action events from floor and inside buttons and current positions of elevators.
  * The controller can get the input from the standard output of Elevators, a TCP socket
  * output stream or/and via Java RMI (remote call backs).
  * The elevators can be controlled via standard input, socket connections and/or Java RMI
  * (controlling stubs).
  * <p>When the user presses buttons, a controller receives action commands (action
  * events) from the buttons via controller's input stream or/and via RMI (call
  * backs). The controller should react on button pressings according to some control
  * algorithm, i.e. it should get and analyze current positions of elevators,
  * make decisions to start/stop motors, to open/close doors, to set values to
  * elevator scales, to start monitoring positions of elevators. Current positions of moving
  * elevator(s) are provided to the controller with a predefined rate of
  * the application Timer to the output stream (standard or socket)
  * of the application. To monitor current positions of elevator(s) via Java RMI,
  * the controller must register (add) a position listener(s) to a motor, all motors
  * or a group of motors. The listener gets a sequence of position values
  * with the rate of the Timer if the position is changing over the time.
  * The rate of the Timer is controlled "by-hand" with a special gauge of the application GUI.
  * <p>The controller can control the elevators (motors, doors and scales) by sending
  * control commands to the input stream (standard or socket) of Elevators or
  * by calling remote methods on Elevators via RMI stubs which can be obtained by
  * static methods of the MakeAll class.
  * <p>When starts, the application accepts the following options specified with
  * or without values:
  * <p>
  * <center><table border="1">
  * <tr><td><code>-number int</code><td>The number of elevators</tr>
  * <tr><td><code>-top int</code><td>The number of the top floor.
  *                                  The number of floors is the number of
  *                                  the top floor plus one (ground floor)</tr>
  * <tr><td><code>-tcp</code><td>Forces the application to open a TCP socket connection
  *                              for controlling the elevators via TCP socket. Both, input and
  *                              output is provided on the default port 4711 (easy to remember in English)</tr>
  * <tr><td><code>-tcpin inputPort</code><td>Forces the application to open TCP socket
  *                              connection(s) for controlling the elevators via TCP socket(s).
  *                              Input to the application is provided on the specified port,
  *                              whereas output can
  *                              be provided on the same or some other port.</tr>
  * <tr><td><code>-tcpout inputPort</code><td>Forces the application to open TCP socket connection(s)
  *                              for controlling the elevators via TCP socket(s). Output from the
  *                              application is provided on the specified port, whereas input can
  *                              be provided on the same or some other port.</tr>
  * <tr><td><code>-rmi</code><td>The application creates a proxy object with the
  *                              <code>GetAll</code>interface and binds it's reference
  *                              to rmiregistry to allow controlling the application
  *                              via Java RMI. Static methods
  *                              of the <code>elevator.rmi.MakeAll</code> class are used to control
  *                              elevators via Java RMI.</tr>
  * <tr><td><code>-nopos</code><td>Do not print positions of moving elevators</tr>
  * <tr><td><code>-h (-help)</code><td>Forces to print a string with a list of options</tr>
  * <tr><td><code>-precision value</code><td>Specifies the movement step of one elevator, i.e.
  *                              the precision of the model, in floor units per time step.
  *                              If not specified, defaults to some hard-coded value.</tr>
  * </table> </center>
  * <p>
  * To control the application via standard input and/or a TCP socket input stream
  * (try telnet), use the following text commands:
  * <p>
  * <center><table border="1">
  * <tr><td><code> m <i>n c </i></code><td>Start moving the elevator number <i>n</i> upwards
  *                                      (<i>c</i> = 1) or downwards (<i>c</i> = -1).<br>
  *                                      Stop the elevator if <i>c</i> = 0</tr>
  * <tr><td><code> d <i>n c </i></code><td>Open (<i>c</i> = 1) or close (<i>c</i> = -1)
  *                                      the door of the elevator number <i>n</i></tr>
  * <tr><td><code> s <i>n f </i></code><td>Set the given floor number <i>f</i> to
  *                                      the scale (floor indicator) in the cabin
  *                                      of the elevator number <i>n</i></tr>
  * <tr><td><code> w <i>n </i></code><td>Inspect a current position of the elevator
  *                                    number <i>n</i>. <br>In response,
  *                                    the application prints a string "f <i>n p</i>",
  *                                    where "f" stands for "floor units",
  *                                    <i>n</i> is the elevator number and <i>p</i> is a
  *                                    float position value in "floor units"
  *                                    (e.g. "f 2 2.5" means that the 2nd elevator
  *                                    is exactly in between the 2nd and the 3rd floors.</tr>
  * <tr><td><code> v <td>Get the current velocity of elevators.<br>In response,
  *                                    the application prints a string "v <i>value</i>",
  *                                    where "v" stands for "velocity",
  *                                    <i>value</i> is the double value of the
  *                                    elevators' velocity in "floor units"
  *                                    per millisecond.</tr>
  * </table> </center>
  * <p>
  * When an inside panel button or a floor button is pressed the application prints
  * an action command associated with the button to the standard output and to
  * a TCP socket output stream, if the application has started with the <code>-tcp</code>
  * option. When controlling the application via Java RMI, use static methods
  * of the <code>elevator.rmi.MakeAll</code> class for adding <code>ActionListener</code>
  * objects in order to receive and to react on action events from the buttons
  * of elevators.
  * <p>An action command associated with an inside panel button printed to the standard
  * output or a TCP socket is "p <i>n f</i>" (where "p"
  * stands for "panel", <i>n</i> is the number of the elevator where the button
  * is located and <i>f</i> is a floor number assigned to the button.
  * <p>An action command associated with a floor button and printed to the standard
  * output or a TCP socket is "b <i>n d</i>" (where "b"
  * stands for "button", <i>n</i> is the number of the floor where the button
  * is located and <i>d</i> is a direction (up or down) associated with the button.
  * <p>As default (if the "-nopos" command line option is not specified),
  * the application prints to the
  * standard output or a TCP socket the position of an elevator when it moves. The
  * new position is printed at a predefined rate set for the internal Timer object of the
  * elevators. You can change the rate of the Timer (i.e. velocity of the elevators)
  * with a control velocity slider
  * of the Elevators GUI. The position is printed in "floor" units.
  * An RMI-based controller can add a position listener with the <code>ActionListener</code>
  * interface to the elevators time to monitor positions of moving elevators. The
  * listener will be notified with an action event when the position has changed.
  * An action command that comes in the event object contains position(s) of
  * elevator(s) in the same format ("f elevator position") as it's printed to the standard
  * output or a TCP socket connection. The RMI-based controller can add also a velocity
  * listener that will be notified with an action event when the velocity has been changed
  * by the velocity slider. An action command that comes with the action event is
  * "v value".
  * <p>The <a href="package-summary.html">elevator</a> package provides classes
  * and interfaces for the Elevators application.
  * The <a href="rmi/package-summary.html">elevator.rmi</a> package provides classes
  * and interfaces for controlling the Elevators via Java RMI.
  * The <a href="rmi/impl/package-summary.html">elevator.rmi.impl</a> package contains proxy classes
  * that implement remote interfaces of the <code>elevator.rmi</code> package for
  * controlling Elevators via Java RMI.
  * The <a href="demo/package-summary.html">elevator.demo</a> package
  * contains a primary class of the AnimationDemo application that illustrates
  * controlling the Elevators application via Java RMI.
  * <p>The static methods of the <code>elevator.rmi.MakeAll</code> class allow
  * obtaining remote references to different components of elevators (motors,
  * doors and scales), and adding listeners with the <code>ActionListener</code>
  * interface to buttons of elevators. See documentation on the
  * <code>elevator.rmi.MakeAll</code> class for details.
  * <p>To run the Elevators application, first, start rmiregistry and then Elevators.
  * When starting Elevators, you must indicate a path to <code>elevator.jar</code>
  * with the <code>-classpath</code> option.
  * The JAR file is located under the <code>lib</code> directory of the Elevators home.
  * When starting Elevators, you should also specify the <code>-Djava.security.policy</code>
  * property that should be set to the <code>rmi.policy</code> file located
  * under the <code>lib</code> directory and the <code>-Djava.rmi.server.codebase</code>
  * property that should point to the <code>elevator.jar</code> file.
  * For example (assume that the Elevators is located under "D:\home\vlad\edu\elevator\"),
  * <p><blockquote><pre>
  * C:\>start rmiregistry
  * C:\>java -classpath D:\home\vlad\edu\elevator\lib\elevator.jar -Djava.security.policy=D:\home\vlad\edu\elevator\lib\rmi.policy
 -Djava.rmi.server.codebase=file:d:\home\vlad\edu\elevator\lib\elevator.jar elevator.Elevators -top 5 -number 5 -rmi
  * </pre></blockquote>
  * The <code>rmi.security</code> file is a text file that may specify the following permission:
  * <p><blockquote><pre>
  * grant {
  * // Allow everything for now
  * permission java.security.AllPermission;
  * };
  * </pre></blockquote>
  * The <code>elevator.jar</code> file is a JAR file that includes all resources
  * of the Elevators application including classes and interfaces needed to
  * develop a control program with Java RMI.
  * @author Vlad Vlassov, IMIT/KTH, Stockholm, Sweden
  * @version 1.0
  * @see elevator.rmi.MakeAll
  * @see elevator.demo.AnimationDemo
  */
 
 public class Elevators {
 	/**
 	 * Maximum possible number of elevators = 5
 	 */
 	public final static int MaxNumberOfElevators = 5;
 	/**
 	 * Maximum possible number of the top floor = 6
 	 */
 	public final static int MaxTopFloor = 6;
 	/**
 	 * Default number of elevators = 1
 	 */
 	public final static int DefaultNumberOfElevators = 1;
 	/**
 	 * Default top floor number = 2
 	 */
 	public final static int DefaultTopFloor = 2;
 	/**
 	 * System "end-of-line" character. Defaults to "\n".
 	 */
 	public final static String EOL = System.getProperty("line.separator", "\n");
 	/**
 	 * A contant (32000) that specifies the special "floor number" associated
 	 * with the Stop inside panel button.
 	 */
 	public final static int SPECIAL_FOR_STOP = 32000;
 	/**
 	 * The opcode of the "open door" command sent to a door, for example "d 1 1"
 	 * (open the door of the 1st elevator)
 	 */
 	public final static int OPEN = 1;
 	/**
 	 * The opcode of the "close door" command sent to a door, for example, "d 1 -1"
 	 * (close the door of the 1st elevator)
 	 */
 	public final static int CLOSE = -1;
 	/**
 	 * The code of the "stop motor" command sent to a motor, for example "m 1 0"
 	 * (stop the 1st elevator)
 	 */
 	public final static int STOP = 0;
 	/**
 	 * The code of the "move upwards" command sent to a motor, for example "m 1 1"
 	 * (start moving the 1st elevator upwards)
 	 */
 	public static final int UP = 1;
 	/**
 	 * The code of the "move downwards" command sent to a motor, for example "m 1 -1"
 	 * (start moving the 1st elevator downwards)
 	 */
 	public static final int DOWN = -1;
 	/**
 	 * Default port for input/output via a TCP socket = 4711
 	 */
 	public static final int defaultPort = 4711;
 	/**
 	 * Number of elevators. Defaults to 1. Can be changed with the "-number n" command line
 	 * argument
 	 */
 	public static int numberOfElevators = DefaultNumberOfElevators;
 	/**
 	 * The number of the top floor to be displayed. Defaults to 2. Can be changed with the
 	 * "-top f" command line argument
 	 */
 	public static int topFloor = DefaultTopFloor;
 	/**
 	 * The number of floors. Computed as (top - bottom + 1) after parsing input parameters
 	 */
 	public static int numberOfFloors;
 
 	protected Elevator[] allElevators;
 	/**
 	 * A boolean option that indicates whether a TCP socket(s) must be open for for input/output.
 	 * Defaults to false. Set by "-tcp" command line option
 	 */
 	protected static boolean tcp = false;
 	/**
 	 * A boolean option that indicates whether an object with the getAllImpl class must
 	 * be created and its remote reference (stub with the GetAll remote interface)
 	 * must be bound to the "GetAll" name
 	 * at the rmi registry so that client(s)
 	 * (controller(s) can obtain the GetAll reference (stub) from
 	 * the rmiregitry for controlling elevators via Java RMI. The clients should
 	 * not look up for this reference explicitly, but rather use static methods
 	 * of the MakeAll class to get remote references to elevator components with
 	 * suitable interfaces.
 	 * The option defaults to false (do not use RMI). Set by "-rmi" command line option
 	 */
 	protected static boolean rmi = false;
 	/**
 	 * The input port number to which a TCP socket must be bound to provide an input stream
 	 * for reading control commands if the application starts with -tcp option.
 	 * Defaults to 4711. Can be changed with "-tcpin port" command line argument.
 	 */
 	protected static int inPort = defaultPort;
 	/**
 	 * The output port number to which a TCP socket must be bound to provide an output stream
 	 * for printing action commands from buttons and current positions of elevators
 	 * if the application starts with -tcp option.
 	 * Defaults to 4711. Can be changed with "-tcpout port" command line argument.
 	 */
 	protected static int outPort = defaultPort;
 	/**
 	 * The name of the host where rmiregitry provides Naming service. Defaults to
 	 * "localhost". Can be changed by calling the <code>init</code> static method
 	 * of the <code>MakeAll</code> class.
 	 */
 	protected static String rmiHost = "localhost";
 	/**
 	 * The number of the port on which rmiregitry provides Naming service. Defaults to
 	 * 1099. Can be changed by calling the <code>init</code> static method
 	 * of the <code>MakeAll</code> class.
 	 */
 	protected static int rmiPort = 1099;
 	/**
 	 * The boolen variable that indicates whether to print postions of moving
 	 * elevators to the standard output. Defaults to true (do print).
 	 */
 	protected static boolean posOutput = true;
 	/**
 	 * A movement step of an elevator, i.e precision of the model. Defaults to 0.04
 	 */
 	public static double step = (double)0.04;
 	/**
 	 * Creates an instance of <code>Elevators</code>, parses the input parameters,
 	 * creates the "Model" of the application (array of objects
 	 * with the <code>Elevator</code> class) and the "View" of the application
 	 * (an object of the <code>ElevatorGUI</code> class).
 	 *
 	 */
 	public Elevators(String[] args) {
 		initOptions(args);
 		numberOfFloors = topFloor + 1;
 		if (numberOfFloors > MaxTopFloor + 1) {
 			System.err.println("illegal parameters" + EOL + USAGE);
 			System.exit(1);
 		} else
 			System.err.println("number of elevators = "+ numberOfElevators + EOL +
 					"number of floors = " + numberOfFloors + EOL);
 		allElevators = new Elevator[numberOfElevators + 1];
 		for (int i = 0; i < numberOfElevators; i++) {
 			allElevators[i] = new Elevator(i + 1);
 		}
 		ElevatorGUI window1 =  new ElevatorGUI("Elevator", this);
 	}
 	/**
 	 * Command line options of the Elevators application
 	 */
 	private final static String[] opts  = {
 		"-h",
 		"-help",
 		"-number",
 		"-top",
 		"-tcpin",
 		"-tcpout",
 		"-tcp",
 		"-rmiHost",
 		"-rmiPort",
 		"-rmi",
 		"-nopos",
 		"-precision"
 	};
 	/**
 	 * Number of accepted command line options
 	 */
 	private final static int NOA = opts.length;
 	/**
 	 * The usage message to be printed on the -help request
 	 */
 	public static final String USAGE =
 			"USAGE: java Elevators [-number numberOfElevators] [-top topFloor] [-tcp] [-tcpin portForInput] [-tcpout portForOutput] [-rmi] [-nopos] [-precision value]" + EOL +
 			"max number of elevators is " + MaxNumberOfElevators + EOL +
 			"max number of floors (including BV) is " + (MaxTopFloor + 1);
 
 	private void initOptions(String[] args) {
 		int argc = args.length;
 		if (argc > 0) {
 			for (int currentOpt = 0; currentOpt < NOA; currentOpt++) {
 				for (int currentArg = 0; currentArg < argc; currentArg++) {
 					if (args[currentArg].equalsIgnoreCase(opts[currentOpt])) {
 						int number = 0;
 						double fnumber = (double)0.0;
 						boolean isInteger = false;
 						boolean isString = false;
 						boolean isdouble = false;
 						if (currentArg + 1 < argc)
 							try {
 								number = Integer.parseInt(args[currentArg + 1]);
 								isInteger = true;
 								currentArg++;
 							} catch (NumberFormatException e) {
 								try {
 									fnumber = (double)(Float.parseFloat(args[currentArg + 1]));
 									isdouble = true;
 								} catch (NumberFormatException e1) {
 									isString = true;
 								}
 							}
 						switch (currentOpt) {
 						case 0: // -help
 						case 1:
 							System.err.println(USAGE);
 							System.exit(0);
 						case 2: //-number numberOfElevators
 						if (isInteger && number > 0 && number <= MaxNumberOfElevators) {
 							numberOfElevators = number;
 							break;
 						} else {
 							System.err.println("illegal parameters" + EOL + USAGE);
 							System.exit(1);
 						}
 						case 3: // -top topFloor
 							if (isInteger && number <= MaxTopFloor && number >= 1) {
 								topFloor = number;
 								break;
 							} else {
 								System.err.println("illegal parametersr" + EOL + USAGE);
 								System.exit(1);
 							}
 						case 4:  {// -tcpin inPort (io via TCP socket connection)
 							tcp = true;
 							if (isInteger && number > 0) inPort = number;
 							break;
 						}
 						case 5:  {// -tcpout outPort (io via TCP socket connection)
 							tcp = true;
 							if (isInteger && number > 0) outPort = number;
 							break;
 						}
 						case 6: { // -tcp (io via TCP socket connections)
 							tcp = true;
 							break;
 						}
 						case 7: { // -rmiHost rmiHost (io via RMI interface) does not really work
 							rmi = true;
 							if (isString) rmiHost = args[currentArg + 1];
 							break;
 						}
 						case 8: { // -rmiPort rmiPort (io via RMI interface) does not really work
 							rmi = true;
 							if (isInteger && number > 0) rmiPort = number;
 							break;
 						}
 						case 9: { // -rmi (io via RMI interface)
 							rmi = true;
 							break;
 						}
 						case 10: { // -nopos (do not print position of movind elevators)
 							posOutput = false;
 							break;
 						}
 						case 11: { // -precision value (precision of the model in floor per time step
 							if (isdouble && fnumber > 0.0) step = fnumber;
 							else {
 								System.err.println("illegal parameters" + EOL + USAGE);
 								System.exit(1);
 							}
 						}
 						default: ;
 						}
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * The main entry to the application. Creates the <code>Elevators</code>
 	 * object that holds most of static global parameters and constants, as well as
 	 * the reference to the "Model" of the application, i.e. array of
 	 * objects with the <code>Elevator</code> class.
 	 */
 	public static void main(String[] args) {
 		Elevators elevators = new Elevators(args);
 	}
 
 }
