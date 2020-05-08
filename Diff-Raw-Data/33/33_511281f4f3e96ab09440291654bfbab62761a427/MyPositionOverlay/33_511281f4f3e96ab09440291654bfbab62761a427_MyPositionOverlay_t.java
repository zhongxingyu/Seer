 package edu.klemen.rekreAsist.android;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Picture;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.location.Location;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class MyPositionOverlay extends Overlay {
 
   private final int mRadius = 8;
   public Bitmap slika=BitmapFactory.decodeFile("res/drawable/ludek.jpg");
 
   public MyPositionOverlay() {
 	  super();
 	  locations = new ArrayList<Location>();
   }
   Location location;
   int oznaka;
   ArrayList<Location> locations;
   
  
   public Location getLocation() {
     return location;
   }
   public void setLocation(Location location,int oznaka) {
     this.location = location;
     this.oznaka=oznaka;
     this.locations.add(location);
   }
   public void setLocation(Location location) {
 	    this.location = location;
 	    this.locations.add(location);
 	  }
 	
   @Override
   public boolean onTap(GeoPoint point, MapView mapView) {
     return false;
   }
   
   @Override
   public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 	  
 	  Projection projection = mapView.getProjection();
     try{
     	if((locations.size()>=2)&&(shadow==false)&&(location!=null)){
     		
     		for(int i=0;i<locations.size()-1;i++){
     			Location zac=locations.get(i);
     			Location kon=locations.get(i+1);
     			Double lat1 = zac.getLatitude()*1E6;
     			Double lon1 = zac.getLongitude()*1E6;
     			Double lat2 = kon.getLatitude()*1E6;
     			Double lon2 = kon.getLongitude()*1E6;
     			GeoPoint gp1= new GeoPoint(lat1.intValue(),lon1.intValue());
     			GeoPoint gp2= new GeoPoint(lat2.intValue(),lon2.intValue());
     			Path path = new Path();
     			Point p1=new Point();
     			Point p2=new Point();
     	        projection.toPixels(gp1, p1);
     	        projection.toPixels(gp2, p2);
     	        path.moveTo(p2.x, p2.y);
     	        path.lineTo(p1.x,p1.y);
     	        Paint crta=new Paint();
    	        
    	        
    	        
     	        if(oznaka==0) crta.setARGB(250, 0, 0, 0);
     	        if(oznaka==1) crta.setARGB(250, 0, 255, 0);
     	        if(oznaka==2) crta.setARGB(250, 255, 0, 0);
     	        
     	        
     	        crta.setStrokeWidth(10);
     	       // crta.setStyle(Style.FILL_AND_STROKE);
     	        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, crta);
     	        //canvas.drawPath(path,);
     	        
     	        
     		}
     	}
     //if ((location!=null)&&(shadow == false)) {
     	else{// Get the current location    
       Double latitude = location.getLatitude()*1E6;
       Double longitude = location.getLongitude()*1E6;
       GeoPoint geoPoint;
       geoPoint = new GeoPoint(latitude.intValue(),longitude.intValue());
 
       
      // projection.toPixels(, out)
       
       // Convert the location to screen pixels     
       Point point = new Point();
       projection.toPixels(geoPoint, point);
 
       
      
       RectF oval = new RectF(point.x - mRadius, point.y - mRadius, 
                              point.x + mRadius, point.y + mRadius);
 
       // Setup the paint
       Paint paint = new Paint();
       paint.setARGB(255, 150, 150, 150);
       paint.setAntiAlias(true);
       paint.setFakeBoldText(true);
 
       Paint backPaint = new Paint();
       backPaint.setARGB(170, 255, 255, 255);
       backPaint.setAntiAlias(true);
 
       RectF backRect = new RectF(point.x + 2 + mRadius, 
                                  point.y - 3*mRadius,
                                  point.x + 120, point.y + mRadius);
 
       // Draw the marker    
       canvas.drawOval(oval, paint);
       FileInputStream in = new FileInputStream("res/drawable/ludek.jpg");
       BufferedInputStream buf = new BufferedInputStream(in);
       Picture a=Picture.createFromStream(buf);
     //  canvas.drawRoundRect(backRect, 5, 5, backPaint);
       canvas.drawPicture(a, backRect);
 //      canvas.drawText("o", 
 //                      point.x + 2*mRadius+2, point.y, 
 //                      paint);
     }
     
     }catch(Exception a){};
     super.draw(canvas, mapView, shadow);
   }
 
 }
