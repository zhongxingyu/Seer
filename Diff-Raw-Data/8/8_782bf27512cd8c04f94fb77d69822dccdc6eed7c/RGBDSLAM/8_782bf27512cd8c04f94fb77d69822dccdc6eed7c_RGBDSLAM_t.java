 package rgbdslam;
 
 import java.util.*;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import lcm.lcm.*;
 
 import april.vis.*;
 import april.jmat.*;
 import april.jmat.geom.*;
 import april.util.*;
 import april.lcmtypes.*;
 
 import kinect.*;
 
 import rgbdslam.*;
 import rgbdslam.DescriptorMatcher.Match;
 import rgbdslam.vis.*;
 
 public class RGBDSLAM implements LCMSubscriber {
 
     LCM lcm = LCM.getSingleton();
     KinectThread kt;
     RenderThread rt;
     RGBDThread rgbd;
     FeatureVisualizer fv;
     double DEFAULT_RES = 0.01;
     double currRes = DEFAULT_RES;
     VoxelArray globalVoxelFrame = new VoxelArray(DEFAULT_RES);
     // State
     boolean loadedFromFile = false;
     // Kinect position
     Object rbtLock = new Object();
     ArrayList<double[]> trajectory = new ArrayList<double[]>();
     double[][] Grbt = Matrix.identity(4, 4).copyArray();
     // Gamepad_t
     ExpiringMessageCache<gamepad_t> lgp = new ExpiringMessageCache<gamepad_t>(0.25);
     // Saved options
     GetOpt opts;
     
     Kinect.Frame currFrame, lastFrame;
 
     public RGBDSLAM(GetOpt opts) {
         this.opts = opts;
 
         // Load from file
         if (opts.getString("file") != null) {
             System.out.println("Loading from file...");
             VoxelArray va = VoxelArray.readFromFile(opts.getString("file"));
             if (va != null) {
                 globalVoxelFrame = va;
                 loadedFromFile = true;
                 System.out.println("Successfully loaded!");
             }
         }
 
         lcm.subscribe("GAMEPAD", this);
 
         kt = new KinectThread();
         rt = new RenderThread();
         rgbd = new RGBDThread();
 
         if (!loadedFromFile) {
             kt.start();
         }
         rt.start();
         if (!loadedFromFile) {
             rgbd.start();
         }
     }
 
     /** Receive LCM messages from the Gamepad */
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
         try {
             if (channel.equals("GAMEPAD")) {
                 gamepad_t gp = new gamepad_t(ins);
                 lgp.put(gp, gp.utime);
             }
         } catch (IOException ex) {
             System.err.println("ERR: Failed to decoded message on: " + channel);
         }
     }
 
     class KinectThread extends Thread {
 
         int pollRate = 5;
         boolean closeSignal = false;
         boolean closed = true;
 
         public void run() {
             closed = false;
             Kinect kinect = new Kinect();
 
             kinect.init();
             kinect.start();
 
             while (true) {
                 Kinect.Frame f = kinect.getFrame();
                 if (f != null) {
                     // XXX Might want to dump frames
                     rgbd.handleFrame(f);
                 }
 
                 if (closeSignal) {
                     break;
                 }
 
                 TimeUtil.sleep(1000 / pollRate);
             }
 
             System.out.println("Stopping kinect...");
             kinect.stop();
             System.out.println("Kinect stopped");
             System.out.println("Closing kinect...");
             kinect.close();
             System.out.println("Kinect closed");
 
             closed = true;
         }
 
         synchronized public void close() {
             closeSignal = true;
         }
 
         synchronized public boolean isClosed() {
             return closed;
         }
 
         synchronized public void setPollRate(int pr) {
             pollRate = pr;
         }
     }
 
     class RenderThread extends Thread {
 
         int fps = 5;
         // Vis
         VisWorld vw;
         VisLayer vl;
         VisCanvas vc;
         // Knobs
         ParameterGUI pg;
         // Gamepad
         boolean turbo = false;
         double SLOW_VEL = -1.0;
         double FAST_VEL = -4.0;
         double SLOW_TVEL = Math.toRadians(15);
         double FAST_TVEL = Math.toRadians(36);
         double vel = SLOW_VEL;
         double theta_vel = SLOW_TVEL;
 
         public RenderThread() {
             vw = new VisWorld();
             vl = new VisLayer(vw);
             vc = new VisCanvas(vl);
 
             fv = new FeatureVisualizer();
 
             JFrame jf = new JFrame("RGBDSLAM Visualization");
             jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             jf.setSize(1280, 720);
             jf.setLayout(new BorderLayout());
 
             // Make sure kinect gets closed
             jf.addWindowListener(new WindowAdapter() {
 
                 synchronized public void windowClosing(WindowEvent e) {
                     System.out.println("Sending kinect close message...");
                     kt.close();
                     while (!kt.isClosed()) {
                         try {
                             System.out.println("Kinect not yet closed...");
                             wait(1000);
                         } catch (InterruptedException ex) {
                         }
                     }
                     System.out.println("Shutdown complete");
                     System.out.println("Goodbye!");
                 }
             });
             pg = new ParameterGUI();
             pg.addDoubleSlider("resolution", "Voxel Resolution (m)", 0.005, 0.5, DEFAULT_RES);
             pg.addIntSlider("kfps", "Kinect FPS", 1, 30, 20);
             pg.addIntSlider("rfps", "Render FPS", 1, 60, 15);
             pg.addButtons("save", "Save to file", "saveFrame", "Save frame");
             pg.addListener(new ParameterListener() {
 
                 public void parameterChanged(ParameterGUI pg, String name) {
                     if (name.equals("resolution") && !loadedFromFile) {
                         synchronized (globalVoxelFrame) {
                             currRes = pg.gd("resolution");
                             // Totally wipe out our old data and make a VA
                             globalVoxelFrame = new VoxelArray(currRes);
                         }
                     } else if (name.equals("save")) {
                         long time = System.currentTimeMillis();
                         synchronized (globalVoxelFrame) {
                             String filename = "va_" + time + ".vx";
                             globalVoxelFrame.writeToFile(filename);
                             System.out.println("Saved to " + filename);
                         }
                     } else if(name.equals("saveFrame")) {
                         long time = System.currentTimeMillis();
                         synchronized (globalVoxelFrame) {
                             String filename = "kf_" + time + ".kframe";
                             try {
                                 FileOutputStream os = new FileOutputStream(filename);
                                 ObjectOutputStream objstream = new ObjectOutputStream(os);
                                 objstream.writeObject(currFrame);
                             } catch(Exception e) {
                                 System.err.println("Could not save frame: " + e.getLocalizedMessage());
                             }
                             System.out.println("Saved frame to " + filename);
                         }
                     }else {
                         updateFPS();
                     }
                 }
             });
             updateFPS();
 
             //VzGrid.addGrid(vw);
 
 
             // XXX May need to change default position. Moving still
             // glitchy. Twitches and doesn't respect our axes
             DefaultCameraManager dcm = new DefaultCameraManager();
             dcm.UI_ANIMATE_MS = 25;
             dcm.interfaceMode = 3.0;
             dcm.setDefaultPosition(new double[]{-5, 0, 0}, new double[]{-4, 0, 0}, new double[]{0, 0, 1});
             dcm.uiDefault();
             vl.cameraManager = dcm;          
 
             jf.add(vc, BorderLayout.CENTER);
             jf.add(pg, BorderLayout.SOUTH);
 
             jf.setVisible(true);
         }
 
         // --- Render Loop ----------------------------------------------------
         synchronized public void run() {
             Tic tic = new Tic();
             while (true) {
                 double[] xyzrpy = getCameraXYZRPY(tic.toctic());
                 if (vc.getLastRenderInfo() != null) {
                     VisCameraManager.CameraPosition cpos = vc.getLastRenderInfo().cameraPositions.get(vl);
 
                     // These are all out of wack. Remap em XXX
                     double[] xaxis = LinAlg.normalize(LinAlg.subtract(cpos.lookat, cpos.eye));
                     double[] zaxis = LinAlg.normalize(cpos.up);
                     double[] yaxis = LinAlg.normalize(LinAlg.crossProduct(zaxis, xaxis));
 
                     double[][] rotx = LinAlg.quatToMatrix(LinAlg.angleAxisToQuat(xyzrpy[3], xaxis));
                     double[][] roty = LinAlg.quatToMatrix(LinAlg.angleAxisToQuat(xyzrpy[4], yaxis));
                     double[][] rotz = LinAlg.quatToMatrix(LinAlg.angleAxisToQuat(xyzrpy[5], zaxis));
 
                     // Translation
                     double[] eye = LinAlg.copy(cpos.eye);
                     double[] lookat = LinAlg.copy(cpos.lookat);
                     double[] up = LinAlg.copy(cpos.up);
                     double x = xyzrpy[0];
                     double y = xyzrpy[1];
                     double z = xyzrpy[2];
                     double[] dx = new double[]{x*xaxis[0], x*xaxis[1], x*xaxis[2]};
                     double[] dy = new double[]{y*yaxis[0], y*yaxis[1], y*yaxis[2]};
                     double[] dz = new double[]{z*zaxis[0], z*zaxis[1], z*zaxis[2]};
 
                     eye = LinAlg.add(dx, LinAlg.add(dy, LinAlg.add(dz, eye)));
                     lookat = LinAlg.add(dx, LinAlg.add(dy, LinAlg.add(dz, lookat)));
 
                     // Rotation
                     double[][] eye_trans = LinAlg.translate(eye);
                     double[][] eye_inv = LinAlg.inverse(eye_trans);
 
                     LinAlg.timesEquals(roty, eye_inv);
                     LinAlg.timesEquals(rotz, roty);
                     LinAlg.timesEquals(eye_trans, rotz);
 
                     lookat = LinAlg.transform(eye_trans, lookat);
                     up = LinAlg.transform(rotx, up);
 
                     vl.cameraManager.uiLookAt(eye, lookat, up, false);
                 }
 
                 synchronized (globalVoxelFrame) {
                     if (globalVoxelFrame.size() > 0 && !opts.getBoolean("kinect-only")) {
                         VisWorld.Buffer vb = vw.getBuffer("voxels");
                         vb.addBack(new VisLighting(false,
                                 globalVoxelFrame.getPointCloud()));
                         vb.addBack(new VzAxes());
                         vb.swap();
                     }
                 }
 
                 synchronized (rbtLock) {
                     VisWorld.Buffer vb = vw.getBuffer("kinect-pos");
                     vb.addBack(new VisChain(Grbt,
                             new VzKinect()));
 
                     vb.addBack(new VzLines(new VisVertexData(trajectory),
                             VzLines.LINE_STRIP,
                             new VzLines.Style(Color.red, 1)));
 
                     vb.swap();
                 }
 
                 TimeUtil.sleep(1000 / fps);
             }
         }
 
         public void updateFPS() {
             fps = pg.gi("rfps");
             kt.setPollRate(pg.gi("kfps"));
         }
 
         private double[] getCameraXYZRPY(double dt) {
             gamepad_t gp = lgp.get();
             if (gp == null) {
                 return new double[6];
             }
             if ((gp.buttons & 0xF0) > 1 && !turbo) {
                 vel = FAST_VEL;
                 theta_vel = FAST_TVEL;
                 turbo = true;
             } else if ((gp.buttons & 0xF0) == 0 && turbo) {
                 vel = SLOW_VEL;
                 theta_vel = SLOW_TVEL;
                 turbo = false;
             }
 
             return new double[]{gp.axes[1]*vel*dt,
                                 gp.axes[0]*vel*dt,
                                 gp.axes[5]*vel*dt,
                                 gp.axes[4]*theta_vel*dt,
                                 gp.axes[3]*theta_vel*dt,
                                 gp.axes[2]*-theta_vel*dt};
         }
     }
 
     class RGBDThread extends Thread {
         
         ArrayList<ImageFeature> featuresL;
         ColorPointCloud lastFullPtCloud;
         ColorPointCloud lastDecimatedPtCloud;
 
         synchronized public void run() {
             AlignFrames af = null;
 
            //double[][] KtoGrbt = new double[][]{{0, 0, -1, 0}, {1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 1}};
             //double[][] Grbt = KtoGrbt;
             double[][] I = new double[][]{{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
             double[][] KtoGrbt = new double[][]{{0, 0, 1, 0}, {-1, 0, 0, 0}, {0, -1, 0, 0}, {0, 0, 0, 1}};
             Grbt = KtoGrbt;
 
             while (true) {
                 synchronized (globalVoxelFrame) {
                     synchronized (rbtLock) {
                         if (currFrame != null && lastFrame != null && af == null) {
                             af = new AlignFrames(currFrame, lastFrame);
                         } else if (currFrame != null && lastFrame != null) {
                             VoxelArray va = new VoxelArray(DEFAULT_RES); // XXX
 
                             af = new AlignFrames(currFrame,
                                     af.getCurrFeatures(),
                                     af.getCurrFullPtCloud(),
                                     af.getCurrDecimatedPtCloud());
 
                             ArrayList<Match> allFeatMatches = new ArrayList<Match>();
                             ArrayList<Match> inlierFeatMatches = new ArrayList<Match>();
                             double[][] transform = af.align(allFeatMatches, inlierFeatMatches);
 
 
                             //Grbt = LinAlg.matrixAB(Grbt, transform); // XXX which order!???
                             LinAlg.timesEquals(Grbt, transform);
 
                             // constrain to no translation for now
                             //Grbt[0][3] = 0;
                             //Grbt[1][3] = 0;
                             //Grbt[2][3] = 0;
                             
                             //System.out.println("Current position");
                             //LinAlg.print(Grbt);
                             //System.out.println();
                             fv.updateFrames(currFrame.makeRGB(), lastFrame.makeRGB(), allFeatMatches, inlierFeatMatches);
 
                             va.voxelizePointCloud(af.getCurrFullPtCloud());
 
                             // Let render thread do its thing
                             globalVoxelFrame.merge(va, Grbt);
                             trajectory.add(LinAlg.resize(LinAlg.matrixToXyzrpy(Grbt), 3));
                         }
                     }
                 }
 
                 try {
                     wait();
                 } catch (InterruptedException ex) {
                 }
             }
         }
 
         public synchronized void handleFrame(Kinect.Frame frame) {
             synchronized(globalVoxelFrame) {
                 lastFrame = currFrame;
                 currFrame = frame;
             }
             notifyAll();
         }
     }
 
     static public void main(String[] args) {
         GetOpt opts = new GetOpt();
         opts.addBoolean('h', "help", false, "Show this help screen");
         opts.addString('f', "file", null, "Load scene from file");
         opts.addBoolean((char) 0, "kinect-only", false, "Only render the kinect trajectory");
 
         if (!opts.parse(args)) {
             System.err.println("ERR: Opts error - " + opts.getReason());
             System.exit(1);
         }
 
         if (opts.getBoolean("help")) {
             opts.doHelp();
             System.exit(1);
         }
 
         RGBDSLAM rgbdSLAM = new RGBDSLAM(opts);
     }
 }
