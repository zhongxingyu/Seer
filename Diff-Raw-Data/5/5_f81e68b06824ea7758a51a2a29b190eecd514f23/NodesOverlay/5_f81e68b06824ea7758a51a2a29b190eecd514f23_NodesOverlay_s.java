 package org.yaoha;
 
 import java.util.HashMap;
 
 import microsoft.mappoint.TileSystem;
 
 import org.osmdroid.api.IGeoPoint;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.MapView.Projection;
 import org.osmdroid.views.overlay.Overlay;
 import org.osmdroid.views.overlay.PathOverlay;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.Log;
 
 public class NodesOverlay extends Overlay {
     
     public NodesOverlay(Context ctx) {
         super(ctx);
         // TODO Auto-generated constructor stub
     }
     
     @Override
     protected void draw(Canvas c, MapView osmv, boolean shadow) {
         if (shadow)
             return;
 
         final int offset = 20;
         
         //Define brush 1
         Paint paint = new Paint();
         paint.setColor(Color.BLACK);
 //        paint.setStrokeWidth(10);
 //        paint.setStyle(Paint.Style.FILL);
 //        paint.setStyle(Paint.Style.STROKE);
         paint.setStyle(Paint.Style.FILL_AND_STROKE);
         
         paint.setTextAlign(Paint.Align.LEFT);
         
         // Calculate the half-world size
         final Rect viewportRect = new Rect();
         final Projection projection = osmv.getProjection();
         // Save the Mercator coordinates of what is on the screen
         viewportRect.set(projection.getScreenRect());
         
         if (false) {
             Log.d(NodesOverlay.class.getSimpleName(), "drawing black center");
             int center_x = viewportRect.centerX();
             int center_y = viewportRect.centerY();
             
             Point pt = new Point(center_x, center_y);
             Path path = new Path();
             path.moveTo(pt.x-offset, pt.y-offset);
             
             path.lineTo(pt.x-offset, pt.y+offset);
             path.lineTo(pt.x+offset, pt.y+offset);
             path.lineTo(pt.x+offset, pt.y-offset);
             path.lineTo(pt.x-offset, pt.y-offset);
             
             c.drawPath(path, paint);
             c.drawLine(viewportRect.left, viewportRect.top, viewportRect.left + 100, viewportRect.top + 100, paint);
             c.drawText("Hier könnte ihre Werbung stehen", viewportRect.left + 100, viewportRect.top + 110, paint);
             
             // DON'T set offset with either of below
             //viewportRect.offset(-mWorldSize_2, -mWorldSize_2);
             //viewportRect.offset(mWorldSize_2, mWorldSize_2);
 
             // Draw a line from one corner to the other
 //            c.drawLine(viewportRect.left, viewportRect.top, viewportRect.right, viewportRect.bottom, paint);
         }
         
         HashMap<Integer, OsmNode> nodes = Nodes.getInstance().getNodeMap();
         for (Integer index : nodes.keySet()) {
             OsmNode node = nodes.get(index);
             // TODO draw node
             //Translate point to x y coordinates on the screen
             IGeoPoint igeo_in = new GeoPoint(node.getLatitudeE6(), node.getLongitudeE6());
 //            Point pt = osmv.getProjection().toPixels(igeo, null);
             Point pt = projection.toMapPixels(igeo_in, null);
             
             //Is the node outside the viewing area? If yes, do not draw it
            if(pt.x < viewportRect.left-offset || pt.x > viewportRect.right+offset || pt.y < viewportRect.bottom-offset || pt.y > viewportRect.top+offset)
                 continue;
             
             Log.d(NodesOverlay.class.getSimpleName(), "drawing one node");
             //Define path
             Path path = new Path();
             path.moveTo(pt.x-offset, pt.y-offset);
             
             path.lineTo(pt.x-offset, pt.y+offset);
             path.lineTo(pt.x+offset, pt.y+offset);
             path.lineTo(pt.x+offset, pt.y-offset);
             path.lineTo(pt.x-offset, pt.y-offset);
             
             c.drawPath(path, paint);
         }
     }
 }
