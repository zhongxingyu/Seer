 package rocnikovkapc;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import lejos.robotics.mapping.MapPanel;
 import lejos.robotics.mapping.MenuAction;
 import lejos.robotics.mapping.NavigationModel;
 import lejos.robotics.mapping.NavigationPanel;
 import lejos.robotics.mapping.SliderPanel;
 import lejos.robotics.navigation.Pose;
 import lejos.robotics.navigation.Waypoint;
 import lejos.util.PilotProps;
 
 /**
  *
  * @author viki
  */
 public class TestingPanel extends NavigationPanel {
 
     private static final int WINDOW_WIDTH = 800;
     private static final int WINDOW_HEIGHT = 600;
     private static final Dimension MAP_SIZE = new Dimension(700, 600);
     private static final Point INITIAL_VIEW_START = new Point(0, -10);
     private static final int INITIAL_ZOOM = 110;
     private JButton stopButton = new JButton("nigga halt!");
     private JTextArea logArea = new JTextArea("Tady běží logování \n", 35, 35);
     private JScrollPane log = new JScrollPane(logArea);
     private JButton clearButton = new JButton("Clear log");
     private SliderPanel setHeading, rotate, travelSpeed, rotateSpeed;
     private static WayGenerator wayGenerator;
     private static ObstacleAvoider obstacleAv;
     private static final long TRACK_WIDTH = 16; //sirka kol robota
     private static final long STEP_LENGTH = 20; //vlastne presnost mereni
     private static final long XRANGE = 100;
     private static final long YRANGE = 100;  //velikost mapovane oblasti
     public static int objizdeni = 0;
 
     public static void main(String[] args) throws IOException {
         PilotProps pp = new PilotProps();
         try {
             pp.loadPersistentValues();
         } catch (IOException ioe) {
             System.exit(1);
         }
         wayGenerator = new WayGenerator(TRACK_WIDTH, STEP_LENGTH, XRANGE, YRANGE);
         (new TestingPanel()).run();
     }
 
     private TestingPanel() {
         buildGUI();
     }
 
     @Override
     protected void buildGUI() {
         title = "GA NXT";
         // Set the map size and suppress unwanted panels
         mapPaneSize = MAP_SIZE;
         showReadingsPanel = false;
         showLastMovePanel = true;
         showParticlePanel = false;
         showConnectPanel = false;
         showStatusPanel = true;
         showMoves = true;
         showZoomLabels = true;
         initialViewStart = INITIAL_VIEW_START;
 
         super.buildGUI();
 
         zoomSlider.setValue(INITIAL_ZOOM);
 
         //Set the map color
         mapPanel.colors[MapPanel.MAP_COLOR_INDEX] = Color.DARK_GRAY;
         mapPanel.colors[MapPanel.ROBOT_COLOR_INDEX] = Color.PINK;
         //Display mesh
         showMesh = true;
 
         stopButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent event) {
                 model.clearPath();
             }
         });
 
         clearButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent event) {
                 logArea.setText("");
             }
         });
 
         commandPanel.add(stopButton);
 
         //Display mesh
         showMesh = true;
 
         log.setBorder(BorderFactory.createTitledBorder("Log"));
         logPanel.add(log);
         logPanel.add(clearButton);
         logPanel.setPreferredSize(new Dimension(400, 650));
         add(logPanel);
 
 //        createMenu();
     }
 
     @Override
     public void log(String message) {
         logArea.append(message + "\n");
     }
 
     @Override
     protected void popupMenuItems(Point p, JPopupMenu menu) {
         // Include set pose and set target menu items
         menu.add(new MenuAction(NavigationModel.NavEvent.GOTO, "Go To", p, model, this));
         menu.add(new MenuAction(NavigationModel.NavEvent.SET_POSE, "Place robot", p, model, this));
         menu.add(new MenuAction(NavigationModel.NavEvent.ADD_WAYPOINT, "Add Waypoint", p, model, this));
         menu.add(new MenuAction(NavigationModel.NavEvent.SET_TARGET, "Set target", p, model, this));
     }
 
     private void run() throws IOException {
         model.setDebug(true);
         model.connect("NXT");
         openInJFrame(this, WINDOW_WIDTH, WINDOW_HEIGHT, title, Color.LIGHT_GRAY, menuBar);
     }
 
     @Override
     public void eventReceived(NavigationModel.NavEvent navEvent) {
         super.eventReceived(navEvent);
         System.out.println(navEvent.name());
         if (navEvent == NavigationModel.NavEvent.WAYPOINT_REACHED) {
             if (objizdeni == 0) {
             Waypoint nextWP = wayGenerator.gnw(new Waypoint(model.getRobotPose()));
             model.goTo(nextWP);
             }
 //            if (objizdeni == 1) { //objizdim ale u nemam prekazku pred sebou
 //                ArrayList<lejos.geom.Point> features = model.getFeatures();
 //                model.goTo(obstacleAv.avoidF2(getLastFeature(), model.getRobotPose()));
 //                objizdeni = 2;
 //            }
 //            if (objizdeni == 3) { //robot uz je nad prekazkou, vraci se do puvodni drahy 
 //            }
         }
 
         if (navEvent == NavigationModel.NavEvent.FEATURE_DETECTED) {
             ArrayList<lejos.geom.Point> features = model.getFeatures();
             System.out.println("FEATURE_DETECTED");
             float featureX = 0;
             float featureY = 0;
             for (lejos.geom.Point point : features) {
                 featureX = point.x;
                 featureY = point.y;
             }
             System.out.println("prekazka: " + featureX + " | " + featureY);
             Pose pozice = model.getRobotPose();
             System.out.println("robot:    " + pozice.getX() + " | " + pozice.getY());
             obstacleAv = new ObstacleAvoider(TRACK_WIDTH, model);
 //                zjistit kdy jsem moc blízko a objet prekazku 
             if (objizdeni == 0 && featureX != 0 && featureY != 0) {
                 if (featureY < pozice.getY() + TRACK_WIDTH * 2 && pozice.getHeading() > 2) {
                     System.out.println("1. Musim objet " + featureY + " < " + pozice.getY() + " + " + TRACK_WIDTH * 2 + " H: " + pozice.getHeading());
                     objizdeni = 1;
                     obstacleAv.bypass();
 //                    model.goTo(obstacleAv.avoidF1(new lejos.geom.Point(featureX, featureY), pozice));
                 } else if (featureY > pozice.getY() - TRACK_WIDTH * 2 && pozice.getHeading() < -2) {
                     System.out.println("2. Musim objet " + featureY + " > " + pozice.getY() + " - " + TRACK_WIDTH * 2 + " H: " + pozice.getHeading());
                     objizdeni = 1;
                     obstacleAv.bypass();
 //                    model.goTo(obstacleAv.avoidF1(new lejos.geom.Point(featureX, featureY), pozice));
                 } else if (featureX < pozice.getX() + TRACK_WIDTH * 2 && (pozice.getHeading() >= -2 && pozice.getHeading() <= 2)) {
                     System.out.println("3. Musim objet " + featureX + " > " + pozice.getX() + " + " + TRACK_WIDTH * 2 + " H: " + pozice.getHeading());
                     objizdeni = 1;
                     obstacleAv.bypass();
                     //model.goTo(obstacleAv.avoidF1(new lejos.geom.Point(featureX, featureY), pozice));
                 }
             }
 //            //kdyz objizdim faze 1 a prekazka mi stale prekazi
 //            if (objizdeni == 1 && featureX != 0 && featureY != 0) {
 //                if (featureY < pozice.getY() + TRACK_WIDTH && pozice.getHeading() > 2) {
 //                    System.out.println("1. Porad prekazi " + featureY + " < " + pozice.getY() + " + " + TRACK_WIDTH + " H: " + pozice.getHeading());
 //                    model.goTo(obstacleAv.avoidF1(new lejos.geom.Point(featureX, featureY), pozice));
 //                } else if (featureY > pozice.getY() - TRACK_WIDTH && pozice.getHeading() < -2) {
 //                    System.out.println("2. Porad prekazi " + featureY + " > " + pozice.getY() + " - " + TRACK_WIDTH + " H: " + pozice.getHeading());
 //                    model.goTo(obstacleAv.avoidF1(new lejos.geom.Point(featureX, featureY), pozice));
 //
 //                } else if (featureX < pozice.getX() + TRACK_WIDTH) {
 //                    System.out.println("3. Porad prekazi " + featureX + " > " + pozice.getX() + " + " + TRACK_WIDTH + " H: " + pozice.getHeading());
 //                    model.goTo(obstacleAv.avoidF1(new lejos.geom.Point(featureX, featureY), pozice));
 //                }
 //            }
 //
 //            //kdyz objizdim faze 2 a furt se nemuzu vratit
 //            if (objizdeni == 2 && featureX != 0 && featureY != 0) {
 //                if (featureX < pozice.getX() + TRACK_WIDTH && pozice.getHeading() == 180) {
 //                    System.out.println("1. F2 Porad prekazi " + featureX + " < " + pozice.getX() + " + " + TRACK_WIDTH + " H: " + pozice.getHeading());
 //                    model.goTo(obstacleAv.avoidF2(new lejos.geom.Point(featureX, featureY), pozice));
 //                } else if (featureX > pozice.getX() - TRACK_WIDTH && pozice.getHeading() == 0) {
 //                    System.out.println("2. F2 Porad prekazi " + featureY + " > " + pozice.getY() + " - " + TRACK_WIDTH + " H: " + pozice.getHeading());
 //                    model.goTo(obstacleAv.avoidF2(new lejos.geom.Point(featureX, featureY), pozice));
 //
 //                } else if (featureX < pozice.getX() + TRACK_WIDTH) {
 //                    System.out.println("3. F2 Porad prekazi " + featureX + " > " + pozice.getX() + " + " + TRACK_WIDTH + " H: " + pozice.getHeading());
 //                    model.goTo(obstacleAv.avoidF2(new lejos.geom.Point(featureX, featureY), pozice));
 //                }
 //            }
             System.out.println("---");
         }
     }
 
     @Override
     public void whenConnected() {
         super.whenConnected();
         model.setRotateSpeed(60);
         model.setTravelSpeed(60);
         model.setPose(new Pose(TRACK_WIDTH / 2, TRACK_WIDTH / 2, 90)); // proste tak musi zacinat 
         model.goTo(wayGenerator.gnw(new Waypoint(model.getRobotPose())));
     }
 
     public lejos.geom.Point getLastFeature() {
         ArrayList<lejos.geom.Point> features = model.getFeatures();
         return features.get(features.size() - 1);
     }
 }
