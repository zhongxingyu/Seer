 package com.googlecode.javacv;
 
 import static com.googlecode.javacv.cpp.opencv_core.IplImage;
 import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
 import java.awt.Polygon;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 /**
  *
  * @author jimmie
  */
 public class PlayerDetector extends Thread{
     Polygon _area;
     boolean _inWatchedArea;
     Thread _areaWatcher;
    PlayerDetector _finder;
     
     PlayerDetector(){
         _inWatchedArea = false;
         this._areaWatcher = this;
         
         // Setup the object finder
         IplImage image = cvLoadImage("/home/jimmie/Downloads/box.png");
         ObjectFinder.Settings settings = new ObjectFinder.Settings();
         settings.objectImage = image;
         settings.useFLANN = true;
         settings.ransacReprojThreshold = 5;
         _finder = new ObjectFinder(settings);
 
     }
     
     /*
      * Should take some sort of camera object, recognize then save to points
      */
     void updateAreaLocation(){
         IplImage scene = cvLoadImage("/home/jimmie/Downloads/monkey.jpg");
         double[] points = _finder.find(scene);
         _area = new Polygon();
                 
         // There are always four points output from the finder
         for( int i = 0; i < 4; i++ ){
             _area.addPoint( (int)points[i], ( int )points[i+1] );
         }
     }
     
     /*
      * Compare last recorded area location, set inArea to true if in the area
      * Set inWatchedArea to false if not (for case where they move outside area after 
      * entering
      */
     boolean inWatchedArea( double[] legs, int numberLegs ){
         // Compare leg locations to the location described by points
         
         boolean oneLegIn = false;
         
         
         // Get vectors from point representing each leg
         for( int i = 0; i< numberLegs; i++ ){
             if( _area.inside( (int)legs[i], (int)legs[i+1] ) ){
                
             }
         }
         
         if( oneLegIn == true ){
             _inWatchedArea = true;
         }
         
         return _inWatchedArea;
     }
     
     void stopWatchingArea(){
        _areaWatcher.stop(); 
     }
     
     void startWatchingArea(){
         // Probably should be in it's own thread  
         _areaWatcher.run();
     }
     
     public void run() {
         while( true ){
             updateAreaLocation();
         }
     }
 }
