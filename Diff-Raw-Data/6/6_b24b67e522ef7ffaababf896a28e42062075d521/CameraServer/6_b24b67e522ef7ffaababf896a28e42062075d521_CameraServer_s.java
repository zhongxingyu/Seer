 /* REMOVE ON C-BUILD */
 import se.lth.cs.fakecamera.Axis211A;
 import se.lth.cs.fakecamera.MotionDetector;
 /* END REMOVE ON C-BUILD */
 
 /* ADD ON C-BUILD */
 //import se.lth.cs.camera.Axis211A;
 //import se.lth.cs.camera.MotionDetector;
 /* END ADD ON C-BUILD */
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 
 public class CameraServer extends Thread {
     private ServerStateMonitor serverStateMonitor = new ServerStateMonitor();
     private MotionDetector motionDetector = new MotionDetector();
     private final static int WAIT_TIME = 5000;
     private Axis211A camera;
     private int port;
 
     public CameraServer(int picturePort, int stateSendPort, String stateReceiveAddress, int stateReceivePort) {
         this.port = picturePort;
         camera = new Axis211A();
         (new ServerStateSender(stateSendPort, serverStateMonitor)).start();
         (new ServerStateReceiver(stateReceiveAddress, stateReceivePort, serverStateMonitor)).start();
     }
 
     public CameraServer(int port, int stateSendPort, String stateReceiveAddress, int stateReceivePort, Axis211A camera) {
         this.camera = camera;
         this.port = port;
         (new ServerStateSender(stateSendPort, serverStateMonitor)).start();
         (new ServerStateReceiver(stateReceiveAddress, stateReceivePort, serverStateMonitor)).start();
     }
 
     public void run() {
         try {
             ServerSocket serverSocket = new ServerSocket(port);
             OutputStream outputStream = serverSocket.accept().getOutputStream();
             byte[] JPEGdata = new byte[Axis211A.IMAGE_BUFFER_SIZE];
             int length, currentMode, previousMode = serverStateMonitor.getMode();
             long waitTime;
 
             while (true) {
                 currentMode = serverStateMonitor.getMode();
                 if (currentMode == ServerStateMonitor.IDLE || currentMode == ServerStateMonitor.IDLE_FORCED) {
                     waitTime = System.currentTimeMillis() + WAIT_TIME;
                     while ((currentMode == ServerStateMonitor.IDLE_FORCED && waitTime > System.currentTimeMillis()) || (currentMode == ServerStateMonitor.IDLE && waitTime > System.currentTimeMillis() && !motionDetector.detect())) {
                         try {
                             sleep(100l);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         currentMode = serverStateMonitor.getMode();
                     }
                 }
 
                 length = camera.getJPEG(JPEGdata, 0);
                 outputStream.write(
                     new byte[] {
                         (byte) (length >>> 24),
                         (byte) (length >>> 16),
                         (byte) (length >>> 8),
                         (byte) length
                     }
                 );
                 outputStream.write(JPEGdata, 0, length);
 
                 if (previousMode == ServerStateMonitor.IDLE && currentMode == ServerStateMonitor.IDLE && motionDetector.detect()) {
                     serverStateMonitor.setMode(ServerStateMonitor.MOVIE);
                 } else if (previousMode == ServerStateMonitor.MOVIE && currentMode == ServerStateMonitor.MOVIE && !motionDetector.detect()) {
                     serverStateMonitor.setMode(ServerStateMonitor.IDLE);
                 }
 
                previousMode = currentMode;
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
        (new CameraServer(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]))).start();
     }
 }
