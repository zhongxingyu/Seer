 package ece5984.phase2.truerandomstudy;
 
 import java.text.DecimalFormat;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.view.SurfaceView;
 
 public class Grapher 
 {
 	public static Bitmap graph(Analysis analysis)
 	{
 		DecimalFormat df = new DecimalFormat("#.#");
 		Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
 		Canvas canvas = new Canvas(bmp);
 		Paint paintBlack = new Paint();
 		paintBlack.setColor(Color.WHITE);
 		paintBlack.setTextAlign(Align.LEFT);
 		Paint paint = new Paint();
 		paint.setColor(Color.RED);
 		paint.setTextAlign(Align.CENTER);
 		canvas.drawLine(0, 180, 200, 180, paint);
 		canvas.drawText("0", 10, 200, paint);
 		float percentage = (((float)analysis.zeros)/(analysis.ones+analysis.zeros));
 		canvas.drawRect(0, 180-percentage*170, 20, 180, paint);
 		canvas.save();
 		canvas.rotate(90, 0, 100);
 		canvas.drawText(df.format(percentage*100)+"%", 0, 100, paintBlack);
 		canvas.restore();
 		
 		canvas.drawText("1", 30, 200, paint);
 		percentage = (((float)analysis.ones)/(analysis.ones+analysis.zeros));
 		canvas.drawRect(20, 180-percentage*170, 40, 180, paint);
 		canvas.save();
		canvas.rotate(90, 0, 100);
		canvas.drawText(df.format(percentage*100)+"%", 0, 100, paintBlack);
 		canvas.restore();
 		
 		paint.setColor(Color.BLUE);
 		int sum = 0;
 		for (int i=0;i<4;i++)
 		{
 			canvas.drawText(""+i, 50+i*20, 200, paint);
 			sum += analysis.pairs[i];
 		}
 		for (int i=0;i<4;i++)
 		{
 			percentage = (((float)analysis.pairs[i])/sum);
 			float left = 40+i*20;
 			float top = 180-percentage*170;
 			canvas.drawRect(left,top,left+20,180, paint);
 			canvas.save();
 			canvas.rotate(90, left, 100);
 			canvas.drawText(df.format(percentage*100)+"%", left, 100, paintBlack);
 			canvas.restore();
 		}
 		paint.setColor(Color.GREEN);
 		sum = 0;
 		for (int i=0;i<8;i++)
 		{
 			canvas.drawText(""+i, 125+i*10, 200, paint);
 			sum += analysis.triples[i];
 		}
 		for (int i=0;i<8;i++)
 		{
 			percentage = (((float)analysis.triples[i])/sum);
 			float left = 120+i*10;
 			float top = 180-percentage*170;
 			canvas.save();
 			canvas.drawRect(left,top,left+10,180, paint);
 			canvas.rotate(90, left, 100);
 			canvas.drawText(df.format(percentage*100)+"%", left, 100, paintBlack);
 			canvas.restore();
 		}
 		return bmp;
 		
 	}
 }
