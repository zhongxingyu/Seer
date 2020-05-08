 package skm.android.ViewModle.ViewModles.main;
 
 import android.content.Context;
 import android.graphics.*;
 import android.view.MotionEvent;
 import android.view.View;
 import skm.android.R;
 import skm.android.ViewModle.ViewModles.Shared;
 import skm.android.ViewModle.ViewModles.base.ViewModleBase;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
import java.lang.String;
 
 /**
  * Created by IntelliJ IDEA.
  * User: 0884588
  * Date: 01/03/12
  * Time: 17:59
  * To change this template use File | Settings | File Templates.
  */
 public class LightHouseViewViewModle  extends ViewModleBase implements Serializable {
     interface IClip {
          boolean play(Canvas c,Context context);
      }
     interface IDrawClip extends IClip{
         void draw(Canvas c);
     }
     interface ITouchClip extends IClip{
          void onTouch(MotionEvent e);
      }
     //int lightHouseFlashSpeed = Shared.getOptionAtribute(getString(R.string.lighthouseflashkey), getString(R.string.))
     List<IClip> clips;
     View view;
     private int flashcount =0;
     public int getFlashcount(){return flashcount;}
     private static LightHouseViewViewModle instance=null;
 
     boolean useSequence = false;
     private boolean flashesSpecified;
     private int noFlashesSpecified;
     float headonSpeed;
     float rotateSpeed;
     float flashSize;
 
     protected LightHouseViewViewModle(Context c,View view)
     {
         super(c);
         this.view=view;
         instance=this;
         init();
 
     }
     public static LightHouseViewViewModle getInstance(Context c,View v){
            return instance!=null?instance:(instance=new LightHouseViewViewModle(c,v));
        }
 
     public boolean isUseSequence() {
         return useSequence;
     }
 
     public void setUseSequence(boolean useSequence) {
         this.useSequence = useSequence;
         init();
     }
 
     public static void clear(){
            instance=null;
      }
 
 
      public void  init() {

          noFlashesSpecified = Integer.parseInt(Shared.getOptionAtribute(getString(R.string.lighthouseflashkey), getString(R.string.flashcount), this));
          String sequence = Shared.getOptionAtribute(getString(R.string.lighthouseflashkey), getString(R.string.sequence), this);
          sequence = sequence.trim();
          Boolean rotate = Boolean.parseBoolean(Shared.getOptionAtribute(getString(R.string.lighthouseflashkey), getString(R.string.rotate), this));
 
          headonSpeed= (Float.parseFloat(Shared.getOptionAtribute(getString(R.string.lighthouseflashkey), getString(R.string.headonSpeed), this)))/10000;
          rotateSpeed = (Float.parseFloat(Shared.getOptionAtribute(getString(R.string.lighthouseflashkey), getString(R.string.rotateSpeed), this)))/10000;
          flashSize = (Float.parseFloat(Shared.getOptionAtribute(getString(R.string.lighthouseflashkey), getString(R.string.flashSize), this)))/10000;
 
          //String sequence = "3,2,1";
          String[] sequenceArray = sequence.split(",");
          for(int i = 0; i < sequenceArray.length; i++)
          {
              sequenceArray[i] = sequenceArray[i].trim();
          }
 
 
              flashesSpecified = !useSequence;
 
 
 
 
          //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.lighthouse_pannel);
          //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.simple_lighthouse_pannel);
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inSampleSize=Shared.SUBSAMPLE;
          //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon,options);
          clips  = new Vector<IClip>();
          Random random = new Random(System.currentTimeMillis());
 
 
          if(flashesSpecified == false)
          {
              if(sequenceArray[0] != "")
              {
                  if(clips.size() == 0)
                  {
                      addClip(clips, start, 1);
 
                      for(int i = 0; i < sequenceArray.length; i++)
                      {
                          try{
                              if(sequenceArray[i] != "")
                              {
                                 if(rotate == true)
                                 {
                                     addClip(clips, flash, Integer.decode(sequenceArray[i]));
                                 } else {
                                     addClip(clips, frontFlash, Integer.decode(sequenceArray[i]));
                                 }
                                 flashcount += Integer.decode(sequenceArray[i]);
                              }
                              addClip(clips, wait, 8);
                          } catch (Exception e)
                          {
 
                          }
                      }
 
                  }
              }
 
          } else {
 
              addClip(clips,start,1);
              addClip(clips,wait,6);
              if(rotate == true)
              {
                 addClip(clips,flash,noFlashesSpecified);
              } else {
                  addClip(clips, frontFlash, noFlashesSpecified);
              }
 
              flashcount = noFlashesSpecified;
          }
 
          addClip(clips,end,1);
 
 
      }
 
 
      private void addClip(List<IClip> clips,IClip clip,int times){
          for(int i=0;i<times;i++)
          {
              clips.add(clip);
          }
      }
 
      private Bitmap bmp;
 
      private int count = 0;
 
      public void onDraw(Canvas c)
      {
          //if(bmp==null)bmp = BitmapFactory.decodeResource(getResources(), R.drawable.lighthouse_pannel);
 
          //if(bmp ==null)bmp = BitmapFactory.decodeResource(getResources(), R.drawable.simple_lighthouse_pannel);
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inSampleSize=Shared.SUBSAMPLE;
          options.inDither=true;
          options.inScreenDensity = c.getDensity();
          //if(bmp ==null)bmp = BitmapFactory.decodeResource(getResources(), R.drawable.lighthouse20frames,options);
 
          if(count<clips.size()&&clips.get(count).play(c,getApplicationContext()))
          {
              count++;
          }
      }
 
     private IClip frontFlash = new IClip()
     {
         long currentTime=System.currentTimeMillis() ,lastTime=System.currentTimeMillis();
         private  int count =0;
         int lightpoint1X = 300;
         int lightpoint1Y = 150;
         int lightpoint2X = 300;
         int lightpoint2Y = 150;
         int radius = 2;
 
         int lightXpoints[] = {350, lightpoint1X , lightpoint2X};
         int lightYpoints[] = {150, lightpoint1Y, lightpoint2Y};
 
         Vector<Integer> lightVector = new Vector<Integer>(2,2);
 
         //railing                // roof    //                 //left railing
         int x2Points[] = {205, 395, 355, 380, 380, 370, 370, 355, 355, 375, 375, 300, 225, 225, 245, 245, 225, 225, 215, 215, 245 };
         int y2Points[] = {600, 600, 190, 190, 170, 170, 180, 180, 90,  90,  80,  70,  80,  90,  90,  180, 180, 170, 170, 190, 190};
         // top
 
         Paint paint = new Paint();
         int flashCountTracker = 0;
         boolean rightFlash = false;
 
         int lightSpeedLateral = 50;
         int lightSpeedVirtical = 40;
 
         boolean lightExpanding = true;
 
 
         public boolean play(Canvas out,Context context)
         {
             float aspect = out.getHeight()*1.0f/out.getWidth();
             int width = out.getWidth();
             Bitmap bitmap = Bitmap.createBitmap(width,Math.round(width*aspect), Bitmap.Config.RGB_565);
             Canvas c = new Canvas(bitmap);
 
             Path lighthousePath = new Path();
             paint.setStyle(Paint.Style.FILL);
             paint.setColor(Color.WHITE);
 
             lighthousePath.moveTo (x2Points[0], y2Points[0]);
 
             for (int index = 1; index < x2Points.length; index++) {
                 lighthousePath.lineTo(x2Points[index], y2Points[index]);
             };
             Matrix translate = new Matrix();
             translate.setTranslate(100,100);
             lighthousePath.transform(translate);
 
             c.drawPath(lighthousePath, paint);
 
             paint.setColor(Color.YELLOW);
             c.drawCircle(400, 230, radius, paint);
 
             float expansionTemp = (int) (radius/0.8);
             expansionTemp = expansionTemp*headonSpeed;
             int expansion = (int) expansionTemp;
 
             boolean firstDecrease = true;
 
             out.drawBitmap(bitmap,c.getClipBounds(),out.getClipBounds(),paint);
             if(lightExpanding == true)
             {
                 if(radius < 800 && (radius+expansion) < 800)
                 {
 
 
                     if(expansion < 1)
                     {
                         radius += 1;
                     }else{
                         radius+= expansion;   //////////////////////////////// speed variable
                     }
 
 
                 } else {
                     lightExpanding = false;
                 }
                 return false;
             } else {
 
                 if(firstDecrease == true)
                 {
                     // the first time we get to here and try to reduce the size of the circle, the expansion variable might actually be bigger than the circle and cause it to disapear.
                     // to prevent this, reduce the expansion variable by one step before applying the rest of the logic
                     double temp = expansion;
                     temp = expansion*0.8;
                     expansion = (int) temp;
     //                expansionTemp = (int) (radius/0.8);
     //                expansionTemp = expansionTemp*headonSpeed;
     //                expansion = (int) expansionTemp;
                     firstDecrease = false;
                 }
                 if(radius > 10 && (radius-expansion) > 10)
                 {
                     if(expansion < 1)
                     {
                         radius -= 1;
                     }else{
                         radius-= expansion;   //////////////////////////////// speed variable
                     }
 
                 } else {
                     lightExpanding = true;
                     return true;
                 }
                 return false;
             }
 
 
 
 
 
         }
 
     };
 
     private IClip flash = new IClip()
     {
         long currentTime=System.currentTimeMillis() ,lastTime=System.currentTimeMillis();
         private  int count =0;
         int lightpoint1X = 360;
         int lightpoint1Y = 140;
         int lightpoint2X = 360;
         int lightpoint2Y = 160;
 
         int lightXpoints[] = {350, lightpoint1X , lightpoint2X};
         int lightYpoints[] = {150, lightpoint1Y, lightpoint2Y};
 
         Vector<Integer> lightVector = new Vector<Integer>(2,2);
 
                                                    //railing                // roof    //                 //left railing
 //        int x2Points[] = {200, 400, 360, 385, 385, 375, 375, 360, 360, 380, 380, 300, 220, 220, 240, 240, 220, 220, 210, 210, 240 };
 //        int y2Points[] = {600, 600, 200, 200, 180, 180, 190, 190, 100, 100, 90,  80,  90,  100, 100, 190, 190, 180, 180, 200, 200};
 //                                                                                                               // top
 
                                                    //railing                // roof    //                 //left railing
         int x2Points[] = {205, 395, 355, 380, 380, 370, 370, 355, 355, 375, 375, 300, 225, 225, 245, 245, 225, 225, 215, 215, 245 };
         int y2Points[] = {600, 600, 190, 190, 170, 170, 180, 180, 90,  90,  80,  70,  80,  90,  90,  180, 180, 170, 170, 190, 190};
                                                                                                                      // top
         Paint paint = new Paint();
         int flashCountTracker = 0;
         boolean rightFlash = false;
 
 
         int lightSpeedLateral = 100;   // variables for speed
         int lightSpeedVirtical = 80;
         boolean first = true;
 
 
         boolean fullRotation = false;
         public boolean play(Canvas c,Context context)
         {
 
             if(first == true)
             {
 
                 if(rotateSpeed > 0)
                 {
                     float temp = lightSpeedLateral* rotateSpeed;
                     lightSpeedLateral = (int) temp;
 
                     temp = lightSpeedVirtical * rotateSpeed;
                     lightSpeedVirtical = (int) temp;
                 }
 
                 if(lightSpeedLateral <= 0)
                 {
                     lightSpeedLateral = 1;
                 }
 
                 if(lightSpeedVirtical <= 0)
                 {
                     lightSpeedVirtical = 1;
                 }
                 first = false;
             }
 
 
             Path lighthousePath = new Path();
             paint.setStyle(Paint.Style.FILL);
             paint.setColor(Color.WHITE);
 
             lighthousePath.moveTo (x2Points[0], y2Points[0]);
 
             for (int index = 1; index < x2Points.length; index++) {
                 lighthousePath.lineTo(x2Points[index], y2Points[index]);
             };
 
             Matrix translate = new Matrix();
             translate.setTranslate(100,100);
             lighthousePath.transform(translate);
 
             c.drawPath(lighthousePath, paint);
 
             // -------------------------------------
 
             Path lightPath = new Path();
 
             Paint lightPaint = new Paint();
             lightPaint.setStyle(Paint.Style.FILL);
             lightPaint.setColor(Color.YELLOW);
 
 
 
             if(rightFlash == false)
             {
                 if(lightpoint1X < 900)
                 {
 
                     lightpoint1X +=lightSpeedLateral;
                     lightpoint2X +=lightSpeedLateral;
 
                     lightXpoints[1] = lightpoint1X ;
                     lightXpoints[2] = lightpoint2X ;
 
 //                    if(lightpoint1X%3 == 0)
 //                    {
 //                        lightpoint1Y += 3;
 //                        lightpoint2Y -= 3;
 //                        lightYpoints[1] = lightpoint1Y;
 //                        lightYpoints[2] = lightpoint2Y;
 //
 //                    }
     //                lightYpoints[1] = ++lightpoint1Y;
     //                lightYpoints[2] = --lightpoint2Y;
                 }
                 else if( lightpoint2Y > -400)
                 {
                     lightpoint1Y += lightSpeedVirtical;
                     lightpoint2Y -= lightSpeedVirtical;
 
                     lightYpoints[1] = lightpoint1Y;
                     lightYpoints[2] = lightpoint2Y;
                 }
                 // flashCountTracker is a variable used to make sure that the yellow square, that represents the flash, stays on screen for 4 iterations
                 else if (flashCountTracker < 5)
                 {
                     // flash
                     Paint yellow = new Paint();
                     yellow.setColor(Color.YELLOW);
                     flashCountTracker++;
 
                     c.drawRect(0,0,2000,2000, yellow);
                 }
                 else
                 {
                     // reinitialise variables
                     lightpoint1X = -300;
                     lightpoint2X = -300;
                     lightXpoints[1] = lightpoint1X;
                     lightXpoints[2] = lightpoint2X;
                     lightXpoints[0] = 250;
 
                     rightFlash = true;
                 }
             } else {
                 if(lightpoint2Y < 140 && (lightpoint2Y + lightSpeedVirtical) < 140)
                 {
                     lightpoint1Y -= lightSpeedVirtical;
                     lightpoint2Y += lightSpeedVirtical;
 
                     lightYpoints[1] = lightpoint1Y;
                     lightYpoints[2] = lightpoint2Y;
                 }
                 else if(lightpoint1X < 240 && (lightpoint1X + lightSpeedLateral) < 240)
                 {
 
                     lightYpoints[1] = 140 ;
                     lightYpoints[2] = 160 ;
 
                     lightpoint1X +=lightSpeedLateral;
                     lightpoint2X +=lightSpeedLateral;
 
                     lightXpoints[1] = lightpoint1X ;
                     lightXpoints[2] = lightpoint2X ;
 
 //                    if(lightpoint1X%3 == 0)
 //                    {
 //                        lightpoint1Y -= 3;
 //                        lightpoint2Y += 3;
 //                        lightYpoints[1] = lightpoint1Y;
 //                        lightYpoints[2] = lightpoint2Y;
 //
 //                    }
                     //                lightYpoints[1] = ++lightpoint1Y;
                     //                lightYpoints[2] = --lightpoint2Y;
                 } else {
                     //reinitalise
                     rightFlash = false;
                     lightpoint1X = 360;
                     lightpoint1Y = 140;
                     lightpoint2X = 360;
                     lightpoint2Y = 160;
 
                     lightXpoints[0] = 350;
                     lightXpoints[1] = lightpoint1X;
                     lightXpoints[2] = lightpoint2X;
 
                     lightYpoints[0] = 150;
                     lightYpoints[1] = lightpoint1Y;
                     lightYpoints[2] = lightpoint2Y;
 
                     flashCountTracker = 0;
                     fullRotation = true;
 
                 }
             }
 
             lightPath.moveTo (lightXpoints[0], lightYpoints[0]);
 
             for (int index = 1; index < lightYpoints.length; index++) {
                 lightPath.lineTo(lightXpoints[index], lightYpoints[index]);
             };
             translate.setTranslate(100,90);
             lightPath.transform(translate);
 
 
 
             while(currentTime-lastTime<1000/30)
             {
                 currentTime = System.currentTimeMillis();
             }
 
 
             c.drawPath(lightPath, lightPaint);
 
 
 
             count=(count +1)%(58);
             //setFrame(c,getFrame(bmp,count,20,1));
             lastTime=currentTime;
             if(fullRotation == true)
             {
                 fullRotation = false;
                 return true;
             }else{
                 return false;
             }
 //            if(count==0)
 //            {
 //                return true;
 //            }else{
 //                return false;
 //            }
 
 
 
 
         }
 
     };
 
      private IClip wait = new IClip(){
              long currentTime=System.currentTimeMillis() ,lastTime =currentTime,endtTime=currentTime+250;
              public boolean play(Canvas c,Context context){
                  if(endtTime<lastTime) endtTime=(lastTime=currentTime=System.currentTimeMillis())+250;
                  while(currentTime-lastTime<50)  currentTime = System.currentTimeMillis();
                  //setFrame(c,getFrame(bmp,0,1,1));
                  return (lastTime=currentTime)>=endtTime;
              }
          };
 
      private IClip start = new ITouchClip(){
          boolean finished = false;
          Paint paint = null;
 
          public boolean play(Canvas out,Context context){
              float aspect = out.getHeight()*1.0f/out.getWidth();
              int width = out.getWidth();
              Bitmap bitmap = Bitmap.createBitmap(width,Math.round(width*aspect), Bitmap.Config.RGB_565);
              Canvas c = new Canvas(bitmap);
 
              if(paint==null){
                  String currentSize = Shared.getOptionAtribute(context.getString(R.string.FontSize), getString(R.string.current), context);
                  String textColour = Shared.getOptionAtribute(context.getString(R.string.Colours), getString(R.string.text), context);
                  paint=new Paint();
                  paint.setColor(Color.parseColor(Shared.getOption(context.getString(R.string.Colours) + "/" + textColour, context).getTextContent()));
                  paint.setTextSize(new Integer(Shared.getOption(context.getString(R.string.FontSize) +"/"+ currentSize,context).getTextContent()));
                  String font = Shared.getOptionAtribute(getString(R.string.Font), getString(R.string.current), getApplicationContext());
                  Typeface face=Typeface.createFromAsset(getAssets(), Shared.getOption(getString(R.string.Font)+"/"+font,getApplicationContext()).getTextContent());
                  paint.setTypeface(face);
 
 
 
              }
              c.drawText("tap the screen",50,50,paint);
              c.drawText("to start",50,100,paint);
              c.drawText("the test",50,150,paint);
 
              out.drawBitmap(bitmap,c.getClipBounds(),out.getClipBounds(),paint);
 
              return finished;
          }
 
          public void onTouch(MotionEvent e) {
 
                   finished=true;
          }
      };
 
     private class Prompt implements ITouchClip
     {
         boolean finished = false;
         Paint paint = null;
 
         public boolean play(Canvas out,Context context)
         {
             float aspect = out.getHeight()*1.0f/out.getWidth();
             int width = out.getWidth();
             Bitmap bitmap = Bitmap.createBitmap(width,Math.round(width*aspect), Bitmap.Config.RGB_565);
             Canvas c = new Canvas(bitmap);
 
             if(paint==null){
                 String currentSize = Shared.getOptionAtribute(context.getString(R.string.FontSize), getString(R.string.current), context);
                 String textColour = Shared.getOptionAtribute(context.getString(R.string.Colours), getString(R.string.text), context);
                 paint=new Paint();
                 paint.setColor(Color.parseColor(Shared.getOption(context.getString(R.string.Colours) + "/" + textColour, context).getTextContent()));
                 //paint.setStrokeWidth(5);
                 paint.setTextSize(new Integer(Shared.getOption(context.getString(R.string.FontSize) +"/"+ currentSize,context).getTextContent()));
 
             }
             c.drawText("Touch Screen for",50,50,paint);
             c.drawText("the next flash",50,100,paint);
             c.drawText("test",50,150,paint);
 
             out.drawBitmap(bitmap,c.getClipBounds(),out.getClipBounds(),paint);
 
             return finished;
         }
 
         public void onTouch(MotionEvent e) {
 
             finished=true;
         }
     };
 
      private IClip end = new IClip(){
          boolean finished = false;
          Paint paint = null;
 
          public boolean play(Canvas out,Context context){
 
              float aspect = out.getHeight()*1.0f/out.getWidth();
              int width = out.getWidth();
              Bitmap bitmap = Bitmap.createBitmap(width,Math.round(width*aspect), Bitmap.Config.RGB_565);
              Canvas c = new Canvas(bitmap);
 
              if(paint==null){
                  String currentSize = Shared.getOptionAtribute(context.getString(R.string.FontSize), getString(R.string.current), context);
                  String textColour = Shared.getOptionAtribute(context.getString(R.string.Colours), getString(R.string.text), context);
                  paint=new Paint();
                  paint.setColor(Color.parseColor(Shared.getOption(context.getString(R.string.Colours) + "/" + textColour, context).getTextContent()));
                  //paint.setStrokeWidth(5);
                  paint.setTextSize(new Integer(Shared.getOption(context.getString(R.string.FontSize) +"/"+ currentSize,context).getTextContent()));
 
              }
              c.drawText("please enter",50,50,paint);
              c.drawText("the number of ",50,100,paint);
              c.drawText("flashes below",50,150,paint);
              out.drawBitmap(bitmap,c.getClipBounds(),out.getClipBounds(),paint);
 
              return finished;
          }
 
 
          public void onTouch(MotionEvent e) {
 
              finished=true;
 
          }
      };
 
 
      public boolean onTouchEvent(MotionEvent e){
          if(count<clips.size()&&clips.get(count) instanceof ITouchClip)((ITouchClip) clips.get(count)).onTouch(e);
          return true;
      }
 
      private void setFrame(Canvas c, Rect frame) {
          c.drawBitmap(bmp,frame,new Rect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom()),null);
      }
 
      public Rect getFrame(Bitmap bitmap,int pos, int horizontalDevisions,int virticalDevisions){
          int startX = Math.round((pos%horizontalDevisions)*(bmp.getWidth()*1.0f/horizontalDevisions)); // fix this for no gitter
          int startY =(pos/horizontalDevisions)*(bmp.getHeight()/virticalDevisions);
          int endX = ((pos%horizontalDevisions)+1)*(bmp.getWidth()/horizontalDevisions);
          int endY = ((pos/horizontalDevisions)+1)*(bmp.getHeight()/virticalDevisions);
          return new Rect(startX,startY,endX,endY);
      }
 
 }
