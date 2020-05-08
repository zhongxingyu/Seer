 package mygame;
 
 import kinecttcpclient.KinectTCPClient;
 
 /*This class handles the kinect data, either from
  * 1) The kinect it self
  * 2) The Mocap class, which gives preloaded data for debugging.
  */
 public class KinectInterface {
 
     boolean mocap = true;
     KinectTCPClient c1;
     int[][] joint;
     Main m;
 
     public KinectInterface(Main m) {
         this.m = m;
         String ipaddress = "127.0.0.1";
         int port = 8001;
         if (mocap == false) {
             c1 = new KinectTCPClient(ipaddress, port);
             c1.sayHello();
         }
     }
 
     public void getData() {
         if (mocap == false) {
             int[] skRaw = c1.readSkeleton();
             joint = KinectTCPClient.getJointPositions(skRaw, 1);
         } else {
            joint = m.moCap.getJoints();
         }
     }
 }
