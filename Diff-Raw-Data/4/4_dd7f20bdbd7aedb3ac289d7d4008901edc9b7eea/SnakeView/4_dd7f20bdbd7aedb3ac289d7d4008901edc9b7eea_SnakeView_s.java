 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package se.chalmers.dat255.sleepfighter.challenge.motionsnake;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.sleepfighter.challenge.snake.CircleEntity;
 import se.chalmers.dat255.sleepfighter.challenge.snake.Segment;
 import se.chalmers.dat255.sleepfighter.utils.geometry.Dimension;
 import se.chalmers.dat255.sleepfighter.utils.geometry.Position;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 
 /**
  * The view class responsible for rendering a single position in the challenge.
  * Original author Mazdak, modified by Laszlo for SleepFighter.
  */
 public class SnakeView extends SurfaceView implements Callback {
 
 	private Dimension gameSize;
 	private int tileSize;
 
 	private boolean isSurfaceCreated;
 
 	private SnakeModel snakeModel;
 
 	// paint which should be used to clear the canvas
 	private Paint clearPaint = new Paint();
 
 	protected Paint bodyPaint = new Paint();
 	protected Paint headPaint = new Paint();
 	protected Paint foodPaint = new Paint();
 
 	/**
 	 * Constructs the SnakeView.
 	 */
 	public SnakeView(Context context, SnakeModel snakeModel) {
 		this(context);
 
 		// Set size & tile size.
 		this.gameSize = SnakeConstants.getGameSize();
 		this.tileSize = SnakeConstants.getTileSize();
 
 		this.bodyPaint.setColor(SnakeConstants.getSnakeColor());
 		this.headPaint.setColor(SnakeConstants.getSnakeHeadColor());
 		this.foodPaint.setColor(SnakeConstants.getFoodColor());
 
 		isSurfaceCreated = false;
 		this.snakeModel = snakeModel;
 		clearPaint.setARGB(255, 0, 0, 0);
 
 		getHolder().addCallback(this);
 		setFocusable(true);
 	}
 
 	private SnakeView(Context context) {
 		super(context);
 	}
 
 	/**
 	 * Tests if the head will be accented.
 	 * 
 	 * @return true if the head will be accented.
 	 */
 	public boolean isAccentedHead() {
 		return !(this.bodyPaint == this.headPaint || bodyPaint
 				.equals(this.headPaint));
 	}
 
 	/**
 	 * Sets the Paint used for head of snake. A null parameter value means
 	 * indicates the head won't be differentiated.
 	 * 
 	 * @param paint
 	 *            The Paint used for head of snake.
 	 */
 	public void setHeadColor(final Paint paint) {
 		this.headPaint = paint;
 	}
 
 	/**
 	 * Returns the Paint used for head of snake. A null parameter value means
 	 * indicates the head won't be differentiated.
 	 * 
 	 * @return The Paint used for head of snake.
 	 */
 	public Paint getHeadPaint() {
 		return this.headPaint;
 	}
 
 	/**
 	 * Sets the Paint used for body of snake.
 	 * 
 	 * @param paint
 	 *            The Paint used for body of snake.
 	 */
 	public void setBodyPaint(final Paint paint) {
 		this.bodyPaint = paint;
 	}
 
 	/**
 	 * Returns the Paint used for body of snake.
 	 * 
 	 * @return The Paint used for body of snake.
 	 */
 	public Paint getBodyPaint() {
 		return this.bodyPaint;
 	}
 
 	/**
 	 * Will try to render the model on the canvas provided
 	 */
 	public void render() {
 		if (isSurfaceCreated) {
 			Canvas c = null;
 
 			c = getHolder().lockCanvas();
 
 			synchronized (getHolder()) {
 
 				if (c != null) {
 					// clear the canvas
 					c.drawRect(0, 0, c.getWidth(), c.getHeight(), clearPaint);
 
 					float scaleX = c.getWidth() * 1f / gameSize.getWidth();
 					float scaleY = c.getHeight() * 1f / gameSize.getHeight();
 
 					if (snakeModel.hasFood()) {
 						drawFood(c, scaleX, scaleY);
 					}
 
 					// draw the snake above everything
 					drawSnake(c, scaleX, scaleY);
 
 					// update the view with the updated canvas
 					getHolder().unlockCanvasAndPost(c);
 				}
 			}
 		}
 	}
 
 	private void drawFood(Canvas c, float scaleX, float scaleY) {
 		Position sphereFood = snakeModel.getFoodPosition();
 
 		CircleEntity f = new CircleEntity(sphereFood.getX(), sphereFood.getY(),
 				tileSize, foodPaint);
 
 		c.drawOval(
 				new RectF(f.getX() * scaleX - f.getRadius() * scaleX, f.getY()
 						* scaleY - f.getRadius() * scaleY, f.getX() * scaleX
 						+ f.getRadius() * scaleX, f.getY() * scaleY
 						+ f.getRadius() * scaleY), f.getPaint());
 	}
 
 	private void drawSnake(Canvas c, float scaleX, float scaleY) {
 		List<Position> snakePositions = new ArrayList<Position>(
 				snakeModel.getSnakePositions());
 
 		for (Position p : snakePositions) {
 			Segment s = new Segment(p.getX(), p.getY(), tileSize, bodyPaint);
 
 			c.drawOval(new RectF((s.getX() * scaleX - s.getRadius() * scaleX),
 					(s.getY() * scaleY - s.getRadius() * scaleY), (s.getX()
 							* scaleX + s.getRadius() * scaleX), (s.getY()
 							* scaleY + s.getRadius() * scaleY)), s.getPaint());
 		}
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
 		this.isSurfaceCreated = true;
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 	}
 }
