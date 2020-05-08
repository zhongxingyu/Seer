 package com.DrawProto;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.View;
 
 public class PathView extends View{
 	
 	Bitmap[] layers = new Bitmap[2];
 	
 	public PathView(Context context) {
 		super(context);
 	}
 	
 	public void drawPath(ArrayList<Node> nodes){
 		Canvas pathCanvas = new Canvas();
		int x1, y1, x2 = 0, y2 = 0, i;
 		Paint p = new Paint();
 		
 		p.setColor(0);
 		p.setStrokeWidth(4);
 		
 		
 		for(i = 0; i < (nodes.size()-1); i++){
 			x1 = nodes.get(i).getX();
 			y1 = nodes.get(i).getY();
 			x2 = nodes.get(i+1).getX();
 			y2 = nodes.get(i+1).getY(); 
 			
 			pathCanvas.drawCircle(x1, y1, 5, p);
 			pathCanvas.drawLine(x1, y1, x2, y2, p);
			
 		}
 		
		pathCanvas.drawCircle(x2, y2, 5, p);
 		
 	}
 
 }
