 package madsdf.ardrone;
 
 import madsdf.ardrone.controller.templates.KNNGestureController;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.*;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import javax.swing.JFrame;
 import javax.swing.JProgressBar;
 import javax.swing.JTextArea;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.plaf.basic.BasicProgressBarUI;
 import madsdf.shimmer.gui.ShimmerMoveAnalyzerFrame;
 import static com.google.common.base.Preconditions.*;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSortedMap;
 import com.google.common.collect.Maps;
 import com.google.common.eventbus.EventBus;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import madsdf.ardrone.controller.DummyController;
 import madsdf.ardrone.controller.KeyboardController;
 import madsdf.ardrone.controller.templates.TimeseriesChartPanel;
 import madsdf.shimmer.event.Globals;
 
 /**
  * ########### Gesture Command of a Quadricopter ###########
  *
  * The goal of this project is to control an ARDrone Parrot with gesture
  * command. The gesture recognition is made with neural networks and two Shimmer
  * sensors each containing an accelerometer and a gyroscope. Here is the
  * principal class of the project. It manage the configuration of the drone and
  * create all the thread to communicate with it. This class manage the list of
  * command to send to the drone and maintain the action map. The action map is
  * updated by the controllers, which can be a keyboard controller or a neural
  * controller. The connection with the Shimmer sensors is entirely handled by
  * the neural controllers which depends on it.
  *
  * ########### Keyboard control mode ###########
  *
  * Takeoff : PageUp Landing : PageDown Hovering: Space (automatically hovering
  * when no command entry) Switch between video mode : V
  *
  * Arrow keys: Go Forward ^ | Go Left <---+---> Go Right | v Go Backward
  *
  * WASD keys: Go Up ^ W Rotate Left <-A-+-D-> Rotate Right S v Go Down
  *
  * Digital keys 1~9: Change speed (rudder rate 5%~99%), 1 is min and 9 is max.
  *
  * Java version : JDK 1.6.0_21 IDE : Netbeans 7.1.1
  *
  * @author Gregoire Aubert
  * @version 1.0
  */
 public class ARDrone extends JFrame implements Runnable {
 
     private static final long serialVersionUID = 1L;
     // ARDrone listening port
     static final int NAVDATA_PORT = 5554;
     static final int VIDEO_PORT = 5555;
     static final int AT_PORT = 5556;
     // ARDrone constant
     static final int COM_MAX_LENGTH = 1024;
     // NavDataReader offset
     static final int NAVDATA_STATE = 4;
     static final int NAVDATA_BATTERY = 24;
     static final int NAVDATA_ALTITUDE = 40;
     // AT*REF values
     /*static final int AT_REF_TAKEOFF = 290718208;
     static final int AT_REF_LANDING = 290717696;
     static final int AT_REF_EMERGENCY = 290717952;
     static final int AT_REF_RESET = 290717696;*/
     static final int AT_REF_TAKEOFF = 1 << 9;
     static final int AT_REF_LANDING = 0;
     static final int AT_REF_EMERGENCY = 1 << 8;
     static final int AT_REF_RESET = 0;
     // End line char
     static final String LF = "\r";
     // Default time between two commands
     static final int CONFIG_INTERVAL = 30;
     // Base configuration
     static final float CONFIG_EULER_MAX = 0.22f;
     static final int CONFIG_VZ_MAX = 1300;
     static final float CONFIG_YAW_MAX = 4.0f;
     static final int CONFIG_ALTITUDE_MAX = 3000;
     static final int CONFIG_ALTITUDE_MIN = 20;
     static final float CONFIG_SPEED = 0.25f;
     static final String CONFIG_DEFAULT_IP = "192.168.1.1";
     static final String CONFIG_DRONE_PROPERTIES = "ardrone.properties";
     static final String CONFIG_MOVEMENT_MODEL = "gesture.SevenFeaturesSelectionMovement";
     // Drone ip adresse
     private InetAddress droneAdr;
     // Command sequence number
     // Send AT command with sequence number 1 will reset the counter
     private int seq = 1;
     // Socket to send command to the drone
     private DatagramSocket atSocket;
     // Drone speed
     //private float speed = CONFIG_SPEED;
     // Speed multiplier. Can be used to modulate speed
     private float speedMultiplier = 1.0f;
     // Time between two commands
     private int cmdInterval = CONFIG_INTERVAL;
     // Drone video chanel
     private int videoChannel = 0;
     // Drone state
     private FlyingState droneState = FlyingState.LANDED;
     // Boolean action map
     private Map<ActionCommand, Boolean> commandState = Maps.newHashMap();
     // Program state
     private boolean exit = false;
     private boolean navDataBootStrap = false;
     private boolean emergency = false;
     // Sending commands thread and command to send list
     Thread commandSender;
     ConcurrentLinkedQueue<String> comList;
     // The panel containing the video
     VideoPanel videoPanel;
     // The battery progress bar
     JProgressBar batteryLevel;
     JTextArea logArea;
     JButton resetButton;
     JSlider forwardBiasSlider;
     JSlider leftBiasSlider;
     JSlider speedSlider;
     TimeseriesChartPanel commandChart;
     TimeseriesChartPanel speedChart;
     TimeseriesChartPanel pcmdChart;
     // The properties objects
     private Properties configDrone;
     private final String leftShimmerID;
     private final String rightShimmerID;
     private ShimmerMoveAnalyzerFrame leftShimmer;
     private ShimmerMoveAnalyzerFrame rightShimmer;
     private final KeyboardController keyboardController;
     
     private final ControlSender controlSender;
 
     /**
      * Main process, create the drone controller.
      *
      * @param args the command line argument, can be the drone ip address
      */
     public static void main(String args[]) throws Exception {
         // Set the Default look and feel
         boolean nimbus = false;
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
             if ("Nimbus".equals(info.getName())) {
                 javax.swing.UIManager.setLookAndFeel(info.getClassName());
                 nimbus = true;
                 break;
             }
         }
         if (!nimbus || System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
             javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
         }
 
         // Set the drone ip adress
         String ip = null;
 
         // The adress can be set with a command line parameter
         if (args.length >= 1) {
             ip = args[0];
         }
 
         // Create the drone controller
         final String rightShimmerID = "BDCD";
         final String leftShimmerID = "9EDB";
         ARDrone arDrone = new ARDrone(ip, leftShimmerID, rightShimmerID);
     }
 
     /**
      * Constructor of the drone controller
      *
      * @param droneIp the ip address of the drone
      */
     public ARDrone(String droneIp, String leftShimmerID, String rightShimmerID) throws Exception {
         super();
         this.leftShimmerID = leftShimmerID;
         this.rightShimmerID = rightShimmerID;
 
         // Load the properties files
         loadProperties();
 
         // Set the ip
         try {
             if (droneIp == null || droneIp.isEmpty()) {
                 this.droneAdr = InetAddress.getByName(configDrone.getProperty("ip", CONFIG_DEFAULT_IP));
             } else {
                 this.droneAdr = InetAddress.getByName(droneIp);
             }
         } catch (UnknownHostException ex) {
             System.err.println("ARDrone.main : " + ex);
         }
         
         // Initialize commandState
         for (ActionCommand a : ActionCommand.values()) {
             commandState.put(a, false);
         }
 
         // Set the title and create the video panel
         this.setTitle("ARDrone gesture control");
         setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("parrot_icon.png")));
         videoPanel = new VideoPanel();
         
         JPanel northPanel = new JPanel();
         northPanel.setLayout(new GridLayout(4, 2));
         
         this.getContentPane().add(northPanel, BorderLayout.NORTH);
         
         JPanel centerPanel = new JPanel();
         centerPanel.setLayout(new GridLayout(1, 2));
         this.getContentPane().add(centerPanel, BorderLayout.CENTER);
         
         
         JPanel southPanel = new JPanel();
         southPanel.setLayout(new GridBagLayout());
         this.getContentPane().add(southPanel, BorderLayout.SOUTH);
         
         // Controls
         resetButton = new JButton("Reset");
         resetButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 resetEmergency();
             }
             
         });
         
         northPanel.add(new JLabel("Reset"));
         northPanel.add(resetButton);
         
         northPanel.add(new JLabel("Forward bias %"));
         forwardBiasSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
         forwardBiasSlider.setMajorTickSpacing(20);
         forwardBiasSlider.setMinorTickSpacing(5);
         forwardBiasSlider.setPaintLabels(true);
         forwardBiasSlider.setPaintTicks(true);
         northPanel.add(forwardBiasSlider);
         
         northPanel.add(new JLabel("Left bias %"));
         leftBiasSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
         leftBiasSlider.setMajorTickSpacing(20);
         leftBiasSlider.setMinorTickSpacing(5);
         leftBiasSlider.setPaintLabels(true);
         leftBiasSlider.setPaintTicks(true);
         northPanel.add(leftBiasSlider);
         
         northPanel.add(new JLabel("Speed %"));
         speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 20);
         speedSlider.setMajorTickSpacing(10);
         speedSlider.setMinorTickSpacing(5);
         speedSlider.setPaintLabels(true);
         speedSlider.setPaintTicks(true);
         northPanel.add(speedSlider);
 
         logArea = new JTextArea();
         logArea.setEnabled(false);
         centerPanel.add(logArea);
         
         commandChart = new TimeseriesChartPanel("Drone commands",
                 "Time (samples)", "Activation", ActionCommand.ordinalToName,
                 100, 0, 1);
         commandChart.setPreferredSize(new Dimension(0, 150));
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         c.weighty = 0.5;
         c.weightx = 1;
         southPanel.add(commandChart, c);
         
         speedChart = new TimeseriesChartPanel("Drone speed",
                 "Time (samples)", "Speed", ImmutableSortedMap.of(0, "Speed"),
                 100, 0, 1);
         speedChart.setPreferredSize(new Dimension(0, 150));
         c.gridy = 1;
         southPanel.add(speedChart, c);
         
         pcmdChart = new TimeseriesChartPanel("PCMD",
                 "Time (samples)", "Value", ImmutableSortedMap.of(0, "flag",
                     1, "roll", 2, "pitch", 3, "gaz", 4, "yaw"),
                 100, 0, 1);
         pcmdChart.setPreferredSize(new Dimension(0, 150));
         c.gridy = 2;
         southPanel.add(pcmdChart, c);
         
         new Timer().schedule(new TimerTask() {
             @Override
             public void run() {
                 updateCharts();
             }
         }, 500, 100);
 
         // Discover the bluetooth devices if needed
       /*if(configSensors != null && configSensors.length > 0)
          BluetoothDiscovery.getInstance().launchDevicesDiscovery();*/
 
         try {
             // Define the socket and the application itself
             atSocket = new DatagramSocket(AT_PORT);
         } catch (SocketException ex) {
             System.err.println("ARDrone: " + ex);
         }
         // Define the frame
         videoPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
         centerPanel.add(videoPanel);
 
         // Define the battery progress bar
         batteryLevel = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
         batteryLevel.setStringPainted(true);
         batteryLevel.setBackground(Color.WHITE);
         batteryLevel.setFocusable(false);
         batteryLevel.setRequestFocusEnabled(false);
         batteryLevel.setPreferredSize(new Dimension(52, 16));
         batteryLevel.setFont(new java.awt.Font("Tahoma", Font.BOLD, 11));
         batteryLevel.setUI(new BasicProgressBarUI() {
             @Override
             protected Color getSelectionBackground() {
                 return Color.BLACK;
             }
 
             @Override
             protected Color getSelectionForeground() {
                 return Color.BLACK;
             }
         });
         batteryLevel.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (batteryLevel.getValue() > 50) {
                     batteryLevel.setForeground(Color.GREEN);
                 } else if (batteryLevel.getValue() < 15) {
                     batteryLevel.setForeground(Color.RED);
                 } else {
                     batteryLevel.setForeground(Color.YELLOW);
                 }
             }
         });
         videoPanel.add(batteryLevel);
        setSize(600, 800);
         setVisible(true);
 
         // Define the window closing action
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 exit = true;
                 System.exit(0);
             }
         });
 
         // Create the thread and the list to send the commands
         comList = new ConcurrentLinkedQueue<>();
         commandSender = new Thread(this);
 
         // Create the keyboard controller
         keyboardController = new KeyboardController(
                 ActionCommand.allCommandMask(), this);
         
         EventBus controllerTickBus = new EventBus();
         controlSender = new ControlSender(this, controllerTickBus);
 
         /*DummyController controller = new DummyController(ActionCommand.allCommandMask(), this);
         controllerTickBus.register(controller);*/
         
        leftShimmer = new ShimmerMoveAnalyzerFrame("Left", leftShimmerID);
         rightShimmer = new ShimmerMoveAnalyzerFrame("Right", rightShimmerID);
         leftShimmer.setVisible(true);
         rightShimmer.setVisible(true);
 
         EventBus leftBus = Globals.getBusForShimmer(leftShimmerID);
         EventBus rightBus = Globals.getBusForShimmer(rightShimmerID);
 
         // TODO: Should use configDrone.controller property
         //NeuralController.FromProperties(ActionCommand.allCommandMask(), this, rightBus, "mouvements_sensor_droit.properties");
         //NeuralController.FromProperties(ActionCommand.allCommandMask(), this, leftBus, "mouvements_sensor_gauche.properties");
 
         //ShimmerAngleController leftAngleController = ShimmerAngleController.FromProperties(ActionCommand.allCommandMask(), this, leftBus, "angle_left.properties");
         //ShimmerAngleController rightAngleController = ShimmerAngleController.FromProperties(ActionCommand.allCommandMask(), this, rightBus, "angle_right.properties");
         
         KNNGestureController rightGestureController =
                 KNNGestureController.FromProperties("right",
                 ActionCommand.allCommandMask(), this, rightBus,
                 "dtw_gestures_right.properties");
         KNNGestureController leftGestureController =
                 KNNGestureController.FromProperties("left",
                 ActionCommand.allCommandMask(), this, leftBus,
                 "dtw_gestures_left.properties");
         controllerTickBus.register(rightGestureController);
        controllerTickBus.register(leftGestureController);
         
         // Launch the configuration of the drone
         startConfig();
         
         System.out.println("Running..");
     }
     
     private void updateCharts() {
         SwingUtilities.invokeLater(new Runnable(){
             @Override
             public void run() {
                 // Commands map
                 final ImmutableMap.Builder<Integer, Float> data = ImmutableMap.builder();
                 for (Entry<ActionCommand, Boolean> e : commandState.entrySet()) {
                     final float v = e.getValue() ? 1 : 0;
                     data.put(e.getKey().ordinal(), v);
                 }
                 commandChart.addToChart(/*System.currentTimeMillis(),*/ data.build());
                 
                 // Speed
                 speedChart.addToChart(ImmutableMap.of(0, getFinalSpeed()));
             }
         });
     }
 
     /**
      * Send the command to the drone with an interval of CONFIG_INTERVAL ms
      */
     public void run() {
 
         // Exit when the ARDrone principal thread is closed
         while (!isExit()) {
 
             // Send more than one command in one packet, but less than the max length of a packet
             String cmd = "";
             while (comList.size() > 0 && cmd.length() + comList.peek().length() < COM_MAX_LENGTH) {
 
                 // Take the first command of the list
                 cmd += comList.poll();
             }
 
             try {
                 // Verify there is a command to send
                 if (cmd.length() > 0) {
 
                     // Create the packet
                     byte[] buf = cmd.getBytes();
                     DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, getDroneAdr(), AT_PORT);
 
                     // And send it
                     atSocket.send(sendPacket);
                 }
 
                 // Wait between two command
                 Thread.sleep(cmdInterval);
             } catch (IOException ex) {
                 System.err.println("ARDrone.run: " + ex);
             } catch (InterruptedException ex) {
                 System.err.println("ARDrone.run: " + ex);
             }
         }
     }
 
     /**
      * Load the properties of the drone and the sensors.
      */
     private void loadProperties() {
         // Create the drone properties object
         configDrone = new Properties();
         try {
             // Load the properties
             configDrone.load(new FileInputStream(CONFIG_DRONE_PROPERTIES));
 
             // Set the timer interval in ms
             String cmdi = configDrone.getProperty("cmd_interval", "");
             checkState(!cmdi.isEmpty());
             cmdInterval = Integer.parseInt(cmdi);
 
             // Set the start speed
             String sp = configDrone.getProperty("speed", "");
             checkState(!sp.isEmpty());
             System.out.println("Ignoring speed property");
             //speed = Float.parseFloat(sp);
 
             // Verify if the sensors property is set
             String leftSensor = configDrone.getProperty("left_sensor");
             String rightSensor = configDrone.getProperty("right_sensor");
             checkState(!leftSensor.isEmpty());
             checkState(!rightSensor.isEmpty());
         } catch (FileNotFoundException ex) {
             System.err.println("ARDrone.loadProperties: " + ex);
         } catch (IOException ex) {
             System.err.println("ARDrone.loadProperties: " + ex);
         }
     }
     
     // WARNING: Don't call this method when drone is flying, will cause crash
     private void resetEmergency() {
         checkState(emergency,
                    "Only call resetEmergency when drone is in emergency");
         
         // Reset emergency state
         sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_RESET);
         sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_EMERGENCY);
 
         // Reset the command watchdog just in case
         sendATCmd("AT*COMWDG=" + getSeq());
     }
 
     /**
      * Configure the drone and start the control sender, the navigation data
      * thread, the video reader thread and the control sender thread.
      */
     private void startConfig() {
         // Launch the command sender thread
         commandSender.start();
 
         // Initialisation of the drone
         /*sendATCmd("AT*PMODE=" + getSeq() + ",2");
         sendATCmd("AT*MISC=" + getSeq() + ",2,20,2000,3000");*/
 
         //resetEmergency();
 
         // Set the altitude max
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:altitude_max\",\""
                 + configDrone.getProperty("altitude_max", CONFIG_ALTITUDE_MAX + "") + "\"");
 
         // Set the altitude min
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:altitude_min\",\""
                 + configDrone.getProperty("altitude_min", CONFIG_ALTITUDE_MIN + "") + "\"");
 
         // Set the control level (0 = no combined yaw mode)
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:control_level\",\"0\"");
 
         // Tell the drone we are indoor/outdoor
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:outdoor\",\"FALSE\"");
 
         // Tell the drone it has the shell or not
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:flight_without_shell\",\"FALSE\"");
 
         // Set the drone max angle
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:euler_angle_max\",\""
                 + configDrone.getProperty("euler_max", CONFIG_EULER_MAX + "") + "\"");
 
         // Set the drone max up/down speed
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:control_vz_max\",\""
                 + configDrone.getProperty("vz_max", CONFIG_VZ_MAX + "") + "\"");
 
         // Set the drone yaw speed
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"control:control_yaw\",\""
                 + configDrone.getProperty("yaw_max", CONFIG_YAW_MAX + "") + "\"");
 
         // Set the navdata demo mode
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"general:navdata_demo\",\"TRUE\"");
 
         // Set the video stream
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"general:video_enable\",\"TRUE\"");
 
         // Set the video channel
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"video:video_channel\",\"" + videoChannel + "\"");
 
         // Set the ultra sound frequence
         //sendATCmd("AT*CONFIG=" + getSeq() + ",\"pic:ultrasound_freq\",\"8\"");
 
         // Tell the drone it is laying horizontally
         sendATCmd("AT*FTRIM=" + getSeq());
 
         // Reset emergency state
         //sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_RESET);
 
         // Send an hovering command
         sendPCMD(0, 0, 0, 0, 0);
 
         // Reset emergency state
         //sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_RESET);
 
         // Reset emergency state
         //sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_RESET);
 
         // Wait a little before starting all thread
         try {
             Thread.sleep(cmdInterval * 10);
         } catch (InterruptedException ex) {
             System.err.println("ARDrone.startConfig: " + ex);
         }
 
         // Launch the navadata thread
         NavDataReader navDataThread = new NavDataReader(this);
         navDataThread.start();
 
         // Launch the video reader
         VideoReader videoReader = new VideoReader(this);
         videoReader.start();
 
         // Launch the control sender thread
         controlSender.start();
     }
 
     /**
      * Is fired when a new video frame is received by the VideoReader. The video
      * panel is updated with the new picture.
      *
      * @param startX the X position
      * @param startY the X position
      * @param w the width
      * @param h the height
      * @param rgbArray the picture
      * @param offset the offset
      * @param scansize the scan size (width)
      */
     void videoFrameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
         videoPanel.frameReceived(startX, startY, w, h, rgbArray, offset, scansize);
     }
 
     /**
      * Send an AT command to the drone, always use this method to send a
      * command.
      *
      * @param cmd the AT command to send without the final "LF"
      */
     public void sendATCmd(String cmd) {
 
         // Add the command to the end of the list of commands to send
         comList.offer(cmd + LF);
     }
 
     /**
      * Send a movement command to the drone, always use this method to send a
      * movement command. This method use the sendATCmd() method.
      *
      * @param flag 0 for hovering, 1 to make the next parameter useful
      * @param roll the roll degree
      * @param pitch the pitch degree
      * @param gas the gas command (up & down)
      * @param yaw the yaw speed (rotation left & right)
      */
     public void sendPCMD(int flag, float roll, float pitch, float gas, float yaw) {
         sendATCmd("AT*PCMD=" + getSeq() + ","
                 + flag + ","
                 + Float.floatToIntBits(roll) + ","
                 + Float.floatToIntBits(pitch) + ","
                 + Float.floatToIntBits(gas) + ","
                 + Float.floatToIntBits(yaw));
         pcmdChart.addToChart(ImmutableMap.of(0, (float)flag, 1, roll,
                 2, pitch, 3, gas, 4, yaw));
     }
 
     /**
      * Make the drone take off if the condition are filled
      */
     public void takeOff() {
         // If the drone is already flying cancel the command        
         if (getFlyingState() == FlyingState.FLYING ||
             getFlyingState() == FlyingState.LANDING ||
             navDataBootStrap ||
             emergency) {
             commandState.put(ActionCommand.TAKEOFF, false);
 
             // If the drone is in emergency state, reset the flag
             if (emergency) {
                 sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_RESET);
                 sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_EMERGENCY);
             }
         } // Otherwise send it
         else {
             sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_TAKEOFF);
             System.out.println("Takeoff");
         }
     }
 
     /**
      * Make the drone land
      */
     public void land() {
         // If the drone is already landed cancel the status
         if (getFlyingState() == FlyingState.LANDED) {
             commandState.put(ActionCommand.LAND, false);
         }
 
         // But keep sending the command, we never know...
         sendATCmd("AT*REF=" + getSeq() + "," + AT_REF_LANDING);
         System.out.println("Landing");
     }
 
     /**
      * Change the video chanel
      *
      * @param chanel the new chanel to switch to
      */
     private void changeVideoChannel(int chanel) {
         System.out.println("Video channel : " + chanel);
         sendATCmd("AT*CONFIG=" + getSeq() + ",\"video:video_channel\",\"" + chanel + "\"");
     }
 
     /**
      * Update the map containing the action to send to the drone
      *
      * @param command the ActionCommand to update
      * @param startAction true to send the command and false to stop sending the
      * command
      */
     public synchronized void updateActionMap(ActionCommand command, boolean startAction) {
         // Add this single action to chart so we are sure the chart updating
         // thread doesn't miss it
         /*final float v = startAction ? 1 : 0;
         commandPanel.addToChart(System.currentTimeMillis(), command.ordinal(), v);*/
         updateCharts();
         
         // TODO: We should do the same with ControlSender (make sure it doesn't
         // miss too short commands)
          
         // process command
         switch (command) {
             case SPEED:
                 switch (command.getVal()) {
                     case 1:
                         setSpeed(0.05, startAction);
                         break;
                     case 2:
                         setSpeed(0.1, startAction);
                         break;
                     case 3:
                         setSpeed(0.15, startAction);
                         break;
                     case 4:
                         setSpeed(0.25, startAction);
                         break;
                     case 5:
                         setSpeed(0.35, startAction);
                         break;
                     case 6:
                         setSpeed(0.45, startAction);
                         break;
                     case 7:
                         setSpeed(0.6, startAction);
                         break;
                     case 8:
                         setSpeed(0.8, startAction);
                         break;
                     case 9:
                         setSpeed(0.99, startAction);
                         break;
                 }
                 break;
 
             // Switch video channel
             case CHANGEVIDEO:
                 if (startAction) {
                     videoChannel = (videoChannel + 1) % 4;
                     changeVideoChannel(videoChannel);
                 }
                 break;
 
             // Go up (gas+)
             case GOTOP:
             case GODOWN:
             case ROTATERIGHT:
             case ROTATELEFT:
             case GOFORWARD:
             case GOBACKWARD:
             case GORIGHT:
             case GOLEFT:
                 commandState.put(command, startAction);
                 break;
             // Try to take off
             case TAKEOFF:
                 if (startAction) {
                     commandState.put(ActionCommand.TAKEOFF, true);
                     commandState.put(ActionCommand.LAND, false);
                     takeOff();
                 }
                 break;
 
             // Land
             case LAND:
                 if (startAction) {
                     commandState.put(ActionCommand.LAND, true);
                     commandState.put(ActionCommand.TAKEOFF, false);
                     land();
                 }
                 break;
 
             // Force the drone to hover
             case HOVER:
 //            System.out.println("Hovering");
                 commandState.put(ActionCommand.HOVER, startAction);
                 sendPCMD(0, 0, 0, 0, 0);
                 break;
 
             default:
                 break;
         }
         updateLog();
     }
 
     private void updateLog() {
         String status = "";
         for (ActionCommand cmd : ActionCommand.values()) {
             status += cmd.name() + " : " + commandState.get(cmd) + "\n";
         }
         logArea.setText(status);
     }
 
     /**
      * @return the command sequence number and increase it
      */
     public synchronized int getSeq() {
         return seq++;
     }
 
     /**
      * @return the application exit state
      */
     public boolean isExit() {
         return exit;
     }
 
     /**
      * @return the drone flying state
      */
     public FlyingState getFlyingState() {
         return droneState;
     }
 
     /**
      * @param flyingState set the new flying state
      */
     public void setFlyingState(FlyingState flyingState) {
         this.droneState = flyingState;
     }
 
     /**
      * @return the drone ip address
      */
     public InetAddress getDroneAdr() {
         return droneAdr;
     }
 
     /**
      * @param navDataBootStrap the navDataBootStrap to set
      * @return the navDatabootStrap value
      */
     public boolean setNavDataBootStrap(boolean navDataBootStrap) {
         return this.navDataBootStrap = navDataBootStrap;
     }
 
     /**
      * @param emergency the emergency to set
      * @return the emergency field value
      */
     public boolean setEmergency(boolean emergency) {
         return this.emergency = emergency;
     }
 
     /**
      * @param speed the speed to set
      * @param set true to set the speed and false to ignore it
      */
     public void setSpeed(double speed, boolean set) {
         if (set) {
             speedSlider.setValue((int)(100*speed));
             //this.speed = (float) speed;
 
             // Print the speed
             System.out.println("Speed: " + getFinalSpeed());
         }
     }
     
     public void setSpeedMultiplier(double multiplier) {
         speedMultiplier = (float)multiplier;
     }
     
     public float getForwardBias() {
         return forwardBiasSlider.getValue() / 100.f;
     }
     
     public float getLeftBias() {
         return leftBiasSlider.getValue() / 100.f;
     }
 
     /**
      * @return the actual speed
      */
     public float getFinalSpeed() {
         return speedMultiplier * (speedSlider.getValue() / 100.f);
         //return speedMultiplier * speed;
     }
 
     /**
      * @return the take off action value
      */
     public boolean isActionTakeOff() {
         return commandState.get(ActionCommand.TAKEOFF);
     }
 
     /**
      * @return the landing action value
      */
     public boolean isActionLanding() {
         return commandState.get(ActionCommand.LAND);
     }
 
     /**
      * @return the top action value
      */
     public boolean isActionTop() {
         return commandState.get(ActionCommand.GOTOP);
     }
 
     /**
      * @return the down action value
      */
     public boolean isActionDown() {
         return commandState.get(ActionCommand.GODOWN);
     }
 
     /**
      * @return the forward action value
      */
     public boolean isActionForward() {
         return commandState.get(ActionCommand.GOFORWARD);
     }
 
     /**
      * @return the backward action value
      */
     public boolean isActionBackward() {
         return commandState.get(ActionCommand.GOBACKWARD);
     }
 
     /**
      * @return the left action value
      */
     public boolean isActionLeft() {
         return commandState.get(ActionCommand.GOLEFT);
     }
 
     /**
      * @return the right action value
      */
     public boolean isActionRight() {
          return commandState.get(ActionCommand.GORIGHT);
     }
 
     /**
      * @return the rotate left action value
      */
     public boolean isActionRotateLeft() {
          return commandState.get(ActionCommand.ROTATELEFT);
     }
 
     /**
      * @return the rotate right action value
      */
     public boolean isActionRotateRight() {
         return commandState.get(ActionCommand.ROTATERIGHT);
     }
 
     /**
      * @return the hovering action value
      */
     public boolean isActionHovering() {
          return commandState.get(ActionCommand.HOVER);
     }
 }
