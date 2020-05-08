 package il.ac.tau.team3.shareaprayer;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.Pray;
 import il.ac.tau.team3.uiutils.MenuSettingsUtils;
 import il.ac.tau.team3.uiutils.MenuUtils;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.LightingColorFilter;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.text.TextPaint;
 
 
 public class BitmapDrawableNumbered  extends BitmapDrawable 
 {
 	private GeneralPlace place;
 	private Paint pBmp = new Paint();
 	private Paint p    = new Paint();
 	private Bitmap glow = null;
 	
 	//private static int lastDisplayed = 0;
 	
 	public BitmapDrawableNumbered(Bitmap image, GeneralPlace a_place)	{
 		this(image, a_place, null);
 	}
 	
 	public BitmapDrawableNumbered(Bitmap image, GeneralPlace a_place, Bitmap glow)	{
 		super(image);
 		place = a_place;
 		this.setBounds(-image.getWidth()/2, -image.getWidth(), image.getWidth()/2 , 0);
 		this.glow = glow;
 	}
 	
 	public int getNumber()	{
 		return MenuSettingsUtils.chooseMaxOrMin(place);
 	}
 	
 	private ColorFilter determineColor(double num)	{
 		if (num > 10)	{
 			return new LightingColorFilter(Color.argb(0, 0x01, 0xFF, 0x01), 0);
 		}
 		
 		final int colorSat = 0xFF;
 		
 		int redAdd = (int) Math.min(Math.max(Math.exp(-num/4)*colorSat,1),colorSat);
 		int BlueAdd = (int) Math.min(Math.max((1.0-Math.exp(-(num-3)/2))*Math.exp(-(num-7)/2)/Math.exp(2)*4*colorSat,1),colorSat);
 		int GreenAdd = (int) Math.min(Math.max((1.0-Math.exp(-(num-5)))*colorSat,1),colorSat);
 		
 		return new LightingColorFilter(Color.argb(0, redAdd, GreenAdd, BlueAdd), 0);
 	}
 	
     @Override
     public void draw(Canvas arg)    {
     	
     		int  numToDisplay= getNumber();
     		String strToDisplay = numToDisplay < 10 ? new Integer(numToDisplay).toString() : "M"; 
     		pBmp.setColorFilter(determineColor(numToDisplay));
             
     		int x = this.getBounds().left;
     		int y = this.getBounds().top;
             arg.drawBitmap(getBitmap(), x, y, pBmp);
             
             if (null != glow)	{
             	arg.drawBitmap(glow, x, y, new Paint());
             }
             
             p.setColor(Color.WHITE);
                     p.setStyle(Paint.Style.STROKE);
                     p.setStrokeWidth(2);
                     //p.setARGB(255, 255, 0, 0);
                     p.setTypeface(Typeface.DEFAULT);
                    p.setTextSize(getBounds().height()/3);
             
             p.setAntiAlias(true);
             
             TextPaint tp = new TextPaint(p);
 	        Rect rect = new Rect();
 	        tp.getTextBounds(strToDisplay, 0, strToDisplay.length(), rect);
             
 	        arg.drawText(strToDisplay , 
                            getBounds().left + getBitmap().getWidth()/2 - rect.width()/2, 
                             getBounds().top + getBitmap().getHeight()/2 + rect.height()/2 , p);
     }
 }
