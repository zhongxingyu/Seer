 package uk.org.smithfamily.mslogger.widgets;
 
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.content.Context;
 import android.graphics.*;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class MSGauge extends View implements Indicator
 {
     private int                diameter;
     private String             name        = "RPM";
     private String             title       = "RPM";
     private String             channel     = "rpm";
     private String             units       = "";
     private double             min         = 0;
     private double             max         = 7000;
     private double             lowD        = 0;
     private double             lowW        = 0;
     private double             hiW         = 5000;
     private double             hiD         = 7000;
     private int                vd          = 0;
     private int                ld          = 0;
     private double             value       = 2500;
     private double             pi          = Math.PI;
     private double             offsetAngle = 0;
     final float                scale       = getResources().getDisplayMetrics().density;
     private Paint              titlePaint;
     private Paint              valuePaint;
     private Paint              pointerPaint;
     private Paint              scalePaint;
     private RectF              rimRect;
     private Paint              rimPaint;
     private Paint              rimCirclePaint;
     private RectF              faceRect;
     private Paint              facePaint;
     private boolean            disabled;
     private static final float rimSize     = 0.02f;
 
     public MSGauge(Context context)
     {
         super(context);
         init(context);
     }
 
     public MSGauge(Context c, AttributeSet s)
     {
         super(c, s);
         init(c);
     }
 
     public MSGauge(Context context, AttributeSet attr, int defaultStyles)
     {
         super(context, attr, defaultStyles);
         init(context);
     }
 
     private void init(Context c)
     {
         initDrawingTools(c);
     }
 
     @Override
     protected void onMeasure(int widthSpec, int heightSpec)
     {
 
         int measuredWidth = MeasureSpec.getSize(widthSpec);
 
         int measuredHeight = MeasureSpec.getSize(heightSpec);
 
         /*
          * measuredWidth and measured height are your view boundaries. You need to change these values based on your requirement E.g.
          * 
          * if you want to draw a circle which fills the entire view, you need to select the Min(measuredWidth,measureHeight) as the radius.
          * 
          * Now the boundary of your view is the radius itself i.e. height = width = radius.
          */
 
         /*
          * After obtaining the height, width of your view and performing some changes you need to set the processed value as your view dimension by using the method setMeasuredDimension
          */
 
         diameter = Math.min(measuredHeight, measuredWidth);
         setMeasuredDimension(diameter, diameter);
 
         /*
          * If you consider drawing circle as an example, you need to select the minimum of height and width and set that value as your screen dimensions
          * 
          * int d=Math.min(measuredWidth, measuredHeight);
          * 
          * setMeasuredDimension(d,d);
          */
 
     }
 
     @Override
     protected void onDraw(Canvas canvas)
     {
         int height = getMeasuredHeight();
 
         int width = getMeasuredWidth();
 
         float scale = (float) getWidth();
         canvas.save(Canvas.MATRIX_SAVE_FLAG);
         canvas.scale(scale, scale);
         float dx = 0.0f;
         float dy = 0.0f;
         if (width > height)
         {
             dx = (width - height) / 2.0f;
         }
         if (height > width)
         {
             dy = (height - width) / 2.0f;
         }
         canvas.translate(dx, dy);
 
         drawFace(canvas);
 
         drawScale(canvas);
 
         if (!disabled)
         {
             drawPointer(canvas);
             drawValue(canvas);
         }
 
         drawTitle(canvas);
         canvas.restore();
     }
 
     private void initDrawingTools(Context context)
     {
         int anti_alias_flag = Paint.ANTI_ALIAS_FLAG;
         if (this.isInEditMode())
         {
             anti_alias_flag = 0;
         }
         rimRect = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
 
         faceRect = new RectF();
         if (!isInEditMode())
         {
             faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, rimRect.right - rimSize, rimRect.bottom - rimSize);
         }
         else
             faceRect = rimRect;
 
         // the linear gradient is a bit skewed for realism
         rimPaint = new Paint();
         if (!this.isInEditMode())
         {
             rimPaint.setFlags(anti_alias_flag);
             rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, Color.rgb(0xf0, 0xf5, 0xf0), Color
                     .rgb(0x30, 0x31, 0x30), Shader.TileMode.CLAMP));
         }
         rimCirclePaint = new Paint();
         if (!this.isInEditMode())
         {
             rimCirclePaint.setAntiAlias(true);
             rimCirclePaint.setStyle(Paint.Style.STROKE);
             rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
             rimCirclePaint.setStrokeWidth(0.005f);
         }
         facePaint = new Paint();
         facePaint.setFilterBitmap(true);
         facePaint.setStyle(Paint.Style.FILL);
         facePaint.setColor(Color.BLACK);
         facePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 
         float cx = 0.5f;
         titlePaint = new Paint();
         titlePaint.setColor(Color.WHITE);
         titlePaint.setTextSize((float) ((cx / 5.0) / scale));
         titlePaint.setTextAlign(Paint.Align.CENTER);
         titlePaint.setFlags(anti_alias_flag);
 
         valuePaint = new Paint();
         valuePaint.setColor(Color.WHITE);
         valuePaint.setTextSize((cx / 4) / scale);
         valuePaint.setTextAlign(Paint.Align.CENTER);
         valuePaint.setFlags(anti_alias_flag);
         pointerPaint = new Paint();
         pointerPaint.setColor(Color.WHITE);
         pointerPaint.setAntiAlias(true);
         pointerPaint.setStrokeWidth(cx / 48.0f);
         pointerPaint.setStyle(Style.FILL_AND_STROKE);
         pointerPaint.setFlags(anti_alias_flag);
         scalePaint = new Paint();
         // scalePaint.setStyle(Paint.Style.STROKE);
         scalePaint.setColor(Color.WHITE);
         // scalePaint.setStrokeWidth(0.005f);
         // scalePaint.setAntiAlias(true);
         scalePaint.setTextSize((cx / 8) / scale);
         // scalePaint.setTypeface(Typeface.SANS_SERIF);
         // scalePaint.setTextScaleX(0.8f);
         scalePaint.setTextAlign(Paint.Align.CENTER);
         scalePaint.setFlags(anti_alias_flag);
     }
 
     private void drawTitle(Canvas canvas)
     {
         titlePaint.setColor(getFgColour());
         canvas.drawText(title, 0.5f, 0.25f, titlePaint);
         canvas.drawText(units, 0.5f, 0.35f, titlePaint);
     }
 
     private void drawValue(Canvas canvas)
     {
         valuePaint.setColor(getFgColour());
 
         float displayValue = (float) (Math.floor(value / Math.pow(10, -vd) + 0.5) * Math.pow(10, -vd));
 
         String text;
 
         if (vd <= 0)
         {
             text = Integer.toString((int) displayValue);
         }
         else
         {
             text = Float.toString(displayValue);
         }
 
         canvas.drawText(text, 0.5f, 0.65f, valuePaint);
     }
 
     private void drawPointer(Canvas canvas)
     {
         float radius = 0.42f;
 
         double range = 270.0 / (max - min);
         double pointerValue = value;
         if (pointerValue < min)
         {
             pointerValue = min;
         }
         if (pointerValue > max)
         {
             pointerValue = max;
         }
         double angle = (pointerValue - min) * range + offsetAngle;
         double rads = angle * pi / 180.0;
         float x = (float) (0.5f - radius * Math.cos(rads - pi / 2.0));
         float y = (float) (0.5f - radius * Math.sin(rads - pi / 2.0));
 
         pointerPaint.setColor(getFgColour());
 
         canvas.drawLine(0.5f, 0.5f, x, y, pointerPaint);
         // canvas.drawText(Integer.toString((int) val), x, y, scalePaint);
     }
 
     private void drawScale(Canvas canvas)
     {
         float radius = 0.42f;
         scalePaint.setColor(getFgColour());
         double range = (max - min);
         double tenpower = Math.floor(Math.log10(range));
         double scalefactor = Math.pow(10, tenpower);
         double maxprimarydigit = Math.ceil(max / scalefactor);
         double gaugeMax = max;// maxprimarydigit * scalefactor;
 
         double minprimarydigit = Math.ceil(min / scalefactor);
         double gaugeMin = min;// minprimarydigit * scalefactor;
         // gaugeMin = Math.min(0, gaugeMin);
 
         double gaugeRange = gaugeMax - gaugeMin;
 
         double step = scalefactor;
 
         while ((gaugeRange / step) < 10)
             step = step / 2;
         for (double val = gaugeMin; val <= gaugeMax; val += step)
         {
 
             float displayValue = (float) (Math.floor(val / Math.pow(10, -ld) + 0.5) * Math.pow(10, -ld));
 
             String text;
 
             if (ld <= 0)
             {
                 text = Integer.toString((int) displayValue);
             }
             else
             {
                 text = Float.toString(displayValue);
             }
             // text = text.substring(0, (text.length() - ld)-1);
             double anglerange = 270.0 / gaugeRange;
             double angle = (val - gaugeMin) * anglerange + offsetAngle;
             double rads = angle * pi / 180.0;
             float x = (float) (0.5f - radius * Math.cos(rads - pi / 2.0));
             float y = (float) (0.5f - radius * Math.sin(rads - pi / 2.0));
             canvas.drawText(text, x, y, scalePaint);
         }
     }
 
     private int getFgColour()
     {
         if(disabled)
         {
             return Color.DKGRAY;
         }
         if (value > lowW && value < hiW)
         {
             return Color.WHITE;
         }
         else
         {
             return Color.BLACK;
         }
     }
 
     private int getBgColour()
     {
         int c = Color.GRAY;
         if (this.disabled)
         {
             return c;
         }
         if (value > lowW && value < hiW)
         {
             return Color.BLACK;
         }
         else if (value <= lowW || value >= hiW)
         {
             c = Color.YELLOW;
         }
         if (value <= lowD || value >= hiD)
         {
             c = Color.RED;
         }
         return c;
 
     }
 
     private void drawFace(Canvas canvas)
     {
         if(isInEditMode())
         {
             facePaint.setColor(Color.RED);
             
             facePaint.setStyle(Style.FILL);
             canvas.drawOval(rimRect, facePaint);
             return;
         }
         canvas.drawOval(rimRect, rimPaint);
         // now the outer rim circle
         canvas.drawOval(rimRect, rimCirclePaint);
         facePaint.setColor(getBgColour());
         canvas.drawOval(faceRect, facePaint);
 
     }
 
     @Override
     public void setName(String name)
     {
         this.name = name;
     }
 
     @Override
     public void setChannel(String channelName)
     {
         this.channel = channelName;
     }
 
     @Override
     public void setTitle(String title)
     {
         this.title = title;
 
     }
 
     @Override
     public void setUnits(String units)
     {
         this.units = units;
 
     }
 
     @Override
     public void setMin(float min)
     {
         this.min = min;
     }
 
     @Override
     public void setMax(float max)
     {
         this.max = max;
 
     }
 
     @Override
     public void setLowD(float lowD)
     {
         this.lowD = lowD;
 
     }
 
     @Override
     public void setLowW(float lowW)
     {
         this.lowW = lowW;
 
     }
 
     @Override
     public void setHiW(float hiW)
     {
         this.hiW = hiW;
 
     }
 
     @Override
     public void setHiD(float hiD)
     {
         this.hiD = hiD;
 
     }
 
     @Override
     public void setVD(int vd)
     {
         this.vd = vd;
 
     }
 
     @Override
     public void setLD(int ld)
     {
         this.ld = ld;
 
     }
 
     @Override
     public void setCurrentValue(double value)
     {
         this.value = value;
         invalidate();
     }
 
     @Override
     public String getChannel()
     {
         return channel;
     }
 
     @Override
     public void setDisabled(boolean disabled)
     {
         this.disabled = disabled;
         this.invalidate();
     }
 
     public GaugeDetails getDetails()
     {
         GaugeDetails gd = new GaugeDetails(name, channel, title, units, min, max, lowD, lowW, hiW, hiD, vd, ld, offsetAngle);
 
         return gd;
     }
 
     public void initFromGD(GaugeDetails gd)
     {
         name = gd.getName();
         title = gd.getTitle();
         channel = gd.getChannel();
         units = gd.getUnits();
         min = gd.getMin();
         max = gd.getMax();
         lowD = gd.getLoD();
 
         lowW = gd.getLoW();
         hiW = gd.getHiW();
 
         hiD = gd.getHiD();
         vd = gd.getVd();
         ld = gd.getLd();
         offsetAngle = gd.getOffsetAngle();
         value = (max - min) / 2.0;
 
     }
 
     public void initFromName(String nme)
     {
         GaugeDetails gd = GaugeRegister.INSTANCE.getGaugeDetails(nme);
         if (gd == null)
         {   
             DebugLogManager.INSTANCE.log("Can't find gauge : " + nme);
            gd = GaugeRegister.INSTANCE.getGaugeDetails("deadGauge");

         }
         initFromGD(gd);
     }
 
     public String getName()
     {
         return name;
     }
 
     @Override
     protected void onAttachedToWindow()
     {
         super.onAttachedToWindow();
         IndicatorManager.INSTANCE.registerIndicator(this);
     }
 
     public double getOffsetAngle()
     {
         return offsetAngle;
     }
 
     public void setOffsetAngle(double offsetAngle)
     {
         this.offsetAngle = offsetAngle;
     }
 }
