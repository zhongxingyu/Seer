 package uk.org.smithfamily.mslogger.widgets;
 
 import java.text.DecimalFormat;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.View;
 
 /**
  * View used in scatter plot to display a ROYGB (Red-Orange-Yellow-Green-Blue) gradient
  */
 public class ScatterPlotZAxisGradient extends View
 {
     private double min;
     private double max;
     
     private Paint valuePaint;
     DecimalFormat decimalFormat;
     
     private boolean ready = false;
     
     ZAxisGradient color;
    
    final float scale = getResources().getDisplayMetrics().density;
     private Paint gradientPaint;
     
     /**
      * 
      * @param context
      */
     public ScatterPlotZAxisGradient(Context context)
     {
         super(context);
     }
     
     /**
      * 
      * @param context
      * @param s
      */
     public ScatterPlotZAxisGradient(Context context, AttributeSet s)
     {
         super(context, s);
         init(context);
     }
 
     /**
      * 
      * @param context
      * @param attr
      * @param defaultStyles
      */
     public ScatterPlotZAxisGradient(Context context, AttributeSet attr, int defaultStyles)
     {
         super(context, attr, defaultStyles);
         init(context);
     }
     
     /**
      * Initialise component that are going to be used to draw on the view
      * 
      * @param context
      */
     public void init(Context context)
     {
         gradientPaint = new Paint();
         
         color = new ZAxisGradient(1020,0);
         
         valuePaint = new Paint();
         valuePaint.setColor(Color.WHITE);
         valuePaint.setTextSize(14);
         valuePaint.setTextAlign(Paint.Align.LEFT);
         valuePaint.setAntiAlias(true);
        
        decimalFormat = new DecimalFormat("#.00");
     }
     
     /**
      * Initialise the view with the minimum and maximum value of the gradient
      * 
      * @param min
      * @param max
      */
     public void initWithMinMax(double min, double max)
     {
         this.min = min;
         this.max = max;
         
         this.ready  = true;
     }
     
     /**
      * Set the dimension of the view
      * 
      * @param widthSpec
      * @param heightSpec
      */
     @Override
     protected void onMeasure(int widthSpec, int heightSpec)
     {
         int measuredWidth = MeasureSpec.getSize(widthSpec);
         int measuredHeight = MeasureSpec.getSize(heightSpec);
         
         setMeasuredDimension(measuredWidth, measuredHeight);
     }
     
     /**
      * Method where everything is drawn
      * 
      * @param canvas
      */
     @Override
     protected void onDraw(Canvas canvas)
     {            
         if (ready) {
            int height = getHeight();
             float yRescaleFactor = height / 1020f;
             
             // Draw the gradient line per line
             int y = 0;
             while (y < 1020)
             {
                 gradientPaint.setColor(color.getColorForValue(y));
                    
                 canvas.drawLine(0, y * yRescaleFactor, 20, y * yRescaleFactor, gradientPaint); 
                 
                 y++;
             }
 
            final float textHeight = 16;
             final int nbSteps = (int) (height / textHeight);
             
            double range = max - min;
            double step = range / nbSteps;
             
             // Draw the scale
             for (int i = 0; i < nbSteps; i ++)
             {                
                 canvas.drawText(decimalFormat.format(max - (i * step)), 25, 12 + (i * textHeight), valuePaint);
             }
         }
     }
 
 }
