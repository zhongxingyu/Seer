 package org.daum.library.javase.copterManager;
 
 
 import org.daum.common.followermodel.Event;
 import org.daum.common.followermodel.Follower;
 import org.kevoree.annotation.*;
 import org.kevoree.extra.marshalling.RichJSONObject;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 14/02/13
  * Time: 13:56
  * To change this template use File | Settings | File Templates.
  */
 @Library(name = "JavaSE")
 @Requires({
         @RequiredPort(name = "location", type = PortType.MESSAGE,optional = true)
 })
 @org.kevoree.annotation.DictionaryType
         ({
                 @org.kevoree.annotation.DictionaryAttribute(name = "id", defaultValue = "jed",optional = true),
                 @org.kevoree.annotation.DictionaryAttribute(name = "distance", defaultValue = "5",optional = true)
         })
 @ComponentType
 public class FakeFollower extends AbstractComponentType implements  Runnable {
 
 
     private Follower firefighter = new Follower();
     private Thread cthread = null;
     private boolean alive = false;
     @Start
     public void start(){
         // random base on RENNES
         double lat =    48.115683+ (Math.random() * 0.1 );
         double lon =         -1.664286+ (Math.random() * 0.1);
         firefighter.lat=(int)(lat* 1E6);
         firefighter.lon =  (int)(lon* 1E6);
         firefighter.id = getDictionary().get("id").toString();
         firefighter.accuracy = 3;
         firefighter.altitude= 10;
         firefighter.safety_distance = 5;
         firefighter.event = Event.UPDATE;
         cthread = new Thread(this);
         alive = true;
         cthread.start();
     }
 
     public double meters2degrees_lat(double meters) {
         return meters / (2.0*Math.PI*6356752.3142/360.0);
     }
     public double meters2degrees_lon(double meters) {
         return (double)((meters / (2.0*Math.PI*6378137.0/360.0)));
     }
 
     public double feet2meters(double feet) {
         return feet * 0.3048006096;
     }
     public double meters2feet(double meters) {
         return meters / 0.3048006096;
     }
 
     public  double deg2rad2(double degrees) { /* convert degrees to radians */
         return degrees * (Math.PI/180.0);
     }
 
     public  double rad2deg2(double radians) { /* convert radian to degree */
         return radians * (180.0/Math.PI);
     }
 
 
     int Random (int _iMin, int _iMax)
     {
         return (int)(Math.random() * (_iMax-_iMin)) + _iMin;
     }
 
 
     public void randomPoint(double distance)
     {
         double width, height;
         int yaw = Random(0,360);
         double lat;
         double lon;
         if (yaw< 90)
         {
             width  =  distance * Math.sin(deg2rad2(yaw));
             height =  distance * Math.cos(deg2rad2(yaw));
         } else if (yaw < 180) {
             width  =  distance * Math.cos(deg2rad2(yaw-90));
             height = -distance * Math.sin(deg2rad2(yaw-90));
         } else if (yaw < 270) {
             width  = -distance * Math.sin(deg2rad2(yaw-180));
             height = -distance * Math.cos(deg2rad2(yaw-180));
         } else {
             width  = -distance * Math.cos(deg2rad2(yaw-270));
             height =  distance * Math.sin(deg2rad2(yaw-270));
         }
 
         lat  = firefighter.lat /1E6+ meters2degrees_lat(height);
         lon = firefighter.lon /1E6+  meters2degrees_lon(width);
 
         firefighter.lat=  (int)(lat* 1E6);
         firefighter.lon =  (int)(lon* 1E6);
 
     }
 
 
     @Stop
     public void stop() {
         alive =false;
         cthread.interrupt();
         try {
             cthread.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         firefighter.event = Event.DELETE;
         RichJSONObject t = new RichJSONObject(firefighter);
         getPortByName("location", MessagePort.class).process(t.toJSON());
 
     }
 
 
     @Override
     public void run() {
         while (alive){
 
             try
             {
                 randomPoint(Integer.parseInt(getDictionary().get("distance").toString()));
                 firefighter.accuracy  = Random(0,3);
                 firefighter.event = Event.UPDATE;
                 firefighter.temperature  = Random(30,26);
                 firefighter.heartmonitor  = Random(60,120);
                 RichJSONObject t = new RichJSONObject(firefighter);
                 getPortByName("location", MessagePort.class).process(t.toJSON());
 
                 Thread.sleep(2000);
             } catch (InterruptedException e) {
                 if(alive){
                     e.printStackTrace();
                 }
             }
         }
     }
 }
