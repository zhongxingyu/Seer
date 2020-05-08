 /*
 *Chris Card
 *Ben Gillman
 *10/18/2012
 *This class contains the icon to be drawn and methods for clicking the icon
 */
 package csci422.ccard.androidmathtutor;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Point;
 
 public class Targets {
 
 	private Matrix mat;
 	private Point loc;
 	private Point origin;
 	private Bitmap bit;//icon to draw
 	private final int iceburg = 20;//increases the touch area that user can click
 	private boolean isanswer = false;
 
 	public Targets(int icon, Point loc, Context ctxt)
 	{
 		this.loc = loc;
 		origin = loc;
 		mat = new Matrix();
 		mat.postTranslate((float)loc.x,(float)loc.y);
 		bit = BitmapFactory.decodeResource(ctxt.getResources(), icon);
 	}
 
 	/**
 	 *This method changes 
 	 * 
 	 */
 	public void clicked(int icon, Context ctxt)
 	{
 		bit = BitmapFactory.decodeResource(ctxt.getResources(), icon);
 	}
 	
 	/**
 	 * only call if it is the basket
 	 */
 	public void isAnswer()
 	{
 		isanswer = true;
 	}
 	
 	public boolean isAnAnswer()
 	{
 		return isanswer;
 	}
 
 	/**
 	 * translates the matrix by dx and dy to simulate sliding
 	 * @param dx x direction
 	 * @param dy y direction
 	 */
 	public void move(float dx, float dy)
 	{
 			mat.postTranslate(dx,dy);
 	}
 	
 	/**
 	 * sets the current location to what the matrix contains
 	 */
 	public void setLoc()
 	{
 		float locs[] = new float[9];
 		mat.getValues(locs);
 		loc.set((int)locs[2], (int)locs[5]);
 	}
 	
 	public Matrix getMat()
 	{
 		return mat;
 	}
 
 	/**
 	 * This method draws the icon onto the canvas
 	 * @param canvas place to draw on
 	 */
 	public void draw(Canvas canvas)
 	{
 		canvas.drawBitmap(bit, mat, null);
 	}
 
 	public boolean isInAnswer(Targets other)
 	{
 		float thiss[] = new float[9];
 		float others[] = new float[9];
 		mat.getValues(thiss);
 		other.getMat().getValues(others);
 		//if it is bounded in the x axis by the icon
 		if ((others[2] > (thiss[2] - iceburg)) && (others[2] < (thiss[2] + bit.getWidth() + iceburg)))
 		{
 			//if it is bounded in the y axis by the icon
 			if((others[5] > (thiss[5] - iceburg)) && (others[5] < (thiss[5] + bit.getHeight() + iceburg)))
 			{
 				other.move(0, 180);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * This method checks to see if the icon + iceberg contains the passed in point
 	 * @param p point that was touched by user
 	 * @return true if the icon contains the point false other wise
 	 */
 	public boolean hasPoint(Point p)
 	{
 		if(!isanswer)
 		{
 			//if it is bounded in the x axis by the icon
 			if ((p.x > (loc.x - iceburg)) && (p.x < (loc.x + bit.getWidth() + iceburg)))
 			{
 				//if it is bounded in the y axis by the icon
 				if((p.y > (loc.y - iceburg)) && (p.y < (loc.y + bit.getHeight() + iceburg)))
 				{
 					return true;
 				}
 			}
 		}
 
 		return false;
 	} 
 }
