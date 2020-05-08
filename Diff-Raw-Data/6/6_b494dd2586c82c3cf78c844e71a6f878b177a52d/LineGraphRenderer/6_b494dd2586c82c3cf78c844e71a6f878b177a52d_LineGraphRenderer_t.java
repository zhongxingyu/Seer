 package budgetapp.util.graph;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Cap;
 import android.graphics.Path;
 import android.view.View;
 
 public class LineGraphRenderer implements IGraphRenderer{
 	
 	float[] arrangedValues;
 	String[] values;
     Paint paint = new Paint();
     Paint textPaint = new Paint();
     Paint bgPaint = new Paint();
     Canvas canvas = new Canvas();
     float originX;
     float originY;
     float[] drawingValues;
     
     public String[] legends;
     
     public LineGraphRenderer(float[] theX, float[] theY) {
         
         arrangeValues(theX,theY);
         setStandardPaint();
         setStandardTextPaint();
     }
     
     public LineGraphRenderer(float[] theX, float[] theY,String[] theValues) {
         
           arrangeValues(theX,theY);
           setStandardPaint();
           setStandardTextPaint();
           
             values = new String[theValues.length];
             for(int i=0;i<theValues.length;i++)
             	values[i] = theValues[i];
     }
     
     public LineGraphRenderer(float[] theX, float[] theY,String[] theValues, String[] theLegends) {
         
         arrangeValues(theX,theY);
         setStandardPaint();
         setStandardTextPaint();
         
           values = new String[theValues.length];
           legends = new String[theLegends.length];
           for(int i=0;i<theValues.length;i++)
           {
         	  values[i] = theValues[i];
         	  legends[i] = theLegends[i];
           }
   }
     
     public LineGraphRenderer(float[] theX, float[] theY, Paint p) {
         
           arrangeValues(theX,theY);
           paint = p;
           setStandardTextPaint();
     }
     public LineGraphRenderer(float[] theX, float[] theY, String[] theValues, Paint p) {
         
         arrangeValues(theX,theY);
         values = new String[theValues.length];
         for(int i=0;i<theValues.length;i++)
         	values[i] = theValues[i];
         paint = p;
         setStandardTextPaint();
    }
     
     private void setStandardPaint()
     {
     	paint.setStrokeWidth(10);
         paint.setStyle(Paint.Style.STROKE);
         paint.setColor(Color.rgb(80, 80, 255));
         paint.setStrokeCap(Cap.ROUND);
         paint.setAntiAlias(true);
         paint.setStrokeMiter(2.0f);
         
     }
     private void setStandardTextPaint()
     {
     	textPaint.setTextSize(20.0f);
     	textPaint.setColor(Color.WHITE);
     	textPaint.setAntiAlias(true);
     }
     /**
      * Rearranges two arrays of values so that they can be drawn using drawLines
      * @param x The x-values
      * @param y The y-values
      */
     private void arrangeValues(float[] x, float[] y)
     {
     	if(x.length<=1)
     		return;
     	
     	arrangedValues = new float[x.length+y.length+(2*x.length-4)];
     	
     	//Fix first
     	int xIndex = 1;
     	arrangedValues[0] = x[0];
     	arrangedValues[1] = y[0];
     	int arrangedIndex = 2;
     	
     	// Fix values between the first and the last
     	for(; xIndex<x.length-1;xIndex++)
     	{
     		arrangedValues[arrangedIndex] = x[xIndex];
     		arrangedValues[arrangedIndex+1] = y[xIndex];
     		arrangedValues[arrangedIndex+2] = x[xIndex];
     		arrangedValues[arrangedIndex+3] = y[xIndex];
     		arrangedIndex+=4;
     	}
     	
     	//Fix last
     	arrangedValues[arrangedIndex] = x[xIndex];
 		arrangedValues[arrangedIndex+1] = y[xIndex];
 		
 		drawingValues = new float[arrangedValues.length];
 		
 		
     	
     }
     public void setColor(int c)
     {
     	paint.setColor(c);
     }
     
     public void setPaint(Paint p)
     {
     	paint = p;
     }
     
     public void drawBackground(float x,float y, float xScale, float yScale, Canvas c)
     {
     	//Activity ac = getResources().getConfiguration().orientation==1
     	
     	
     }
 	@Override
 	public void drawGraph(float x, float y, float xScale, float yScale, Canvas c) {
 		
 		//arrangedValues = arrangedValues;
 		//Put in the offset
		if(drawingValues==null)
			return;
 		for(int i=0;i<drawingValues.length;i+=2)
 		{
 			drawingValues[i]=arrangedValues[i]*xScale+x;
 			// Flip the graph upside down to have negative values down and positive up
 			drawingValues[i+1]=-1*(arrangedValues[i+1]*yScale+y); 
 			//c.drawCircle(drawingValues[i], drawingValues[i+1], 1, paint);
 		}
 		c.drawLines(drawingValues, paint);
 		
 		
 	}
 	@Override
 	public void drawGraphAndValues(float x, float y, float xScale, float yScale,String[] values, Canvas c) {
 		
 		//arrangedValues = arrangedValues;
 		//Put in the offset
 		for(int i=0;i<4;i++)
 		{
 			drawingValues[i]=arrangedValues[i]*xScale+x;
 			// Flip the graph upside down to have negative values down and positive up
 			drawingValues[i+1]=-1*(arrangedValues[i+1]*yScale+y); 
 		}
 		c.drawLine(drawingValues[0],drawingValues[1],drawingValues[2],drawingValues[3], paint);
 		
 		for(int i=2;i<drawingValues.length-2;i+=2)
 		{
 			drawingValues[i]=arrangedValues[i]*xScale+x;
 			// Flip the graph upside down to have negative values down and positive up
 			drawingValues[i+1]=-1*(arrangedValues[i+1]*yScale+y); 
 			c.drawLine(drawingValues[i],drawingValues[i+1],drawingValues[i+2],drawingValues[i+3], paint);
 		}
 		
 		
 		//c.drawCircle(40, 40, 30, paint);
 		
 	}
 	
 	/**
 	 * Prints all the values
 	 */
 	@Override
 	public void drawValues(float x, float y, float xScale, float yScale, Canvas c) {
 		int j=0;
		if(drawingValues==null)
			return;
 		//c.drawText(values[0], drawingValues[0], drawingValues[1], textPaint);
 		for(int i=0;i<drawingValues.length-2;i+=4)
 		{
 			drawingValues[i]=arrangedValues[i]*xScale+x;
 			// Flip the graph upside down to have negative values down and positive up
 			drawingValues[i+1]=-1*(arrangedValues[i+1]*yScale+y); 
 
 			c.drawText(values[j], drawingValues[i], drawingValues[i+1], textPaint);
 			
 			j++;
 		}
 		c.drawText(values[j], drawingValues[drawingValues.length-2], drawingValues[drawingValues.length-1], textPaint);
 		
 	}
 	
 
 
 }
