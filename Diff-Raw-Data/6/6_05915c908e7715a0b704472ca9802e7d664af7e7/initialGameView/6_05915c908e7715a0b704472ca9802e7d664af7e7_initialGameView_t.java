 package com.example.initial_game;
 
 import java.util.ArrayList;
 import java.lang.Math;
 import java.lang.Number;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class initialGameView extends View implements OnTouchListener {
 
 	private Paint p;
 	Bitmap.Config conf = Bitmap.Config.ARGB_8888;
 
 	ArrayList<LineObject> line_array;
 
 	boolean touched;
 	Point line_touch;
 	Bitmap bouncing_dot;
 	private Rect dot_bounds;
 	private Point dot_point;
 	private Point dot_velocity;
 	//private Point lastCollision = new Point(-1, -1);
 	private sprite dot;
 
 	int dot_color;
 
 	public initialGameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		line_touch = new Point();
 		touched = false;
 		p = new Paint();
 
 		line_array = new ArrayList<LineObject>();
 
 		bouncing_dot = Bitmap.createBitmap(100, 100, conf);
 		dot_bounds = new Rect(0, 0, bouncing_dot.getWidth(),
 				bouncing_dot.getHeight());
 		dot_point = new Point(-1, -1);
 		dot_color = Color.YELLOW;
 		dot_velocity = new Point(-1, -1);
 		dot = new sprite(bouncing_dot, dot_point, dot_bounds);
 
 		// initialize things
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent event) {
 		Point point = new Point();
 		point.x = (int) event.getX();
 		point.y = (int) event.getY();
 
 		//Double unit_x;
 		//Double unit_y;
 		//Double norm;
 
 		if (touched) {
 			Point line_deltas = new Point();
 			line_deltas.x = point.x - line_touch.x;
 			line_deltas.y = point.y - line_touch.y;
 
 			//norm = Math.sqrt(Math.pow(point.x - line_touch.x, 2)
 			//		+ Math.pow(point.y - line_touch.y, 2));
 			//unit_x = (point.x - line_touch.x) / norm;
 			//unit_y = (point.y - line_touch.y) / norm;
 
 			Bitmap new_Bitmap = Bitmap.createBitmap(
 					Math.abs(point.x - line_touch.x),
 					Math.abs(point.y - line_touch.y), conf);
 			Rect new_bounds = new Rect(0, 0, new_Bitmap.getWidth(),
 					new_Bitmap.getHeight());
 			Point new_point = new Point();
 			new_point.x = (point.x >= line_touch.x ? line_touch.x : point.x);
 			new_point.y = (point.y >= line_touch.y ? line_touch.y : point.y);
 			boolean positive;
 
 			if (point.x >= line_touch.x) {
 				if (point.y >= line_touch.y) {
 					positive = false;
 				} else {
 					positive = true;
 				}
 			} else {
 				if (point.y >= line_touch.y) {
 					positive = true;
 				} else {
 					positive = false;
 				}
 			}
 
 			line_array.add(new LineObject(new_Bitmap, new_point, positive,
 					new_bounds, line_deltas));
 			touched = false;
 		} else {
 			line_touch = point;
 			touched = true;
 		}
 		invalidate();
 		return true;
 	}
 
 	synchronized public void setDot(int x, int y) {
 		dot_point = new Point(x, y);
 		dot.setPoint(dot_point);
 	}
 
 	synchronized public Point getDot() {
 		return dot.getPoint();
 	}
 
 	synchronized public int getDotX() {
 		return dot_point.x;
 	}
 
 	synchronized public int getDotY() {
 		return dot_point.y;
 	}
 
 	synchronized public int getDotWidth() {
 
 		return dot.getBounds().width();
 	}
 
 	synchronized public int getDotHeight() {
 		return dot.getBounds().height();
 	}
 
 	synchronized public Point getDotVelocity() {
 		return dot_velocity;
 	}
 
 	synchronized public void setDotVelocity(Point new_velocity) {
 		dot_velocity = new_velocity;
 	}
 
 	synchronized public void setDotVelocityX(int new_x) {
 		dot_velocity.x = new_x;
 	}
 
 	synchronized public void setDotVelocityY(int new_y) {
 		dot_velocity.y = new_y;
 	}
 
 	private boolean checkForCollision(sprite sprite_first, sprite sprite_second) {
 		Point sprite1 = sprite_first.getPoint();
 		Point sprite2 = sprite_second.getPoint();
 		Rect sprite1_bounds = sprite_first.getBounds();
 		Rect sprite2_bounds = sprite_second.getBounds();
 		Bitmap bm1 = sprite_first.getBitmap();
 		Bitmap bm2 = sprite_second.getBitmap();
 		if (sprite1.x < 0 && sprite2.x < 0 && sprite1.y < 0 && sprite2.y < 0)
 			return false;
 		Rect r1 = new Rect(sprite1.x, sprite1.y, sprite1.x
 				+ sprite1_bounds.width(), sprite1.y + sprite1_bounds.height());
 		Rect r2 = new Rect(sprite2.x, sprite2.y, sprite2.x
 				+ sprite2_bounds.width(), sprite2.y + sprite2_bounds.height());
 		Rect r3 = new Rect(r1);
 		if (r1.intersect(r2)) {
 			for (int i = r1.left; i < r1.right; i++) {
 				for (int j = r1.top; j < r1.bottom; j++) {
 					if (bm1.getPixel(i - r3.left, j - r3.top) != Color.TRANSPARENT) {
 						if (bm2.getPixel(i - r2.left, j - r2.top) != Color.TRANSPARENT) {
 							//lastCollision = new Point(sprite2.x + i - r2.left,
 							//		sprite2.y + j - r2.top);
 							return true;
 						}
 					}
 				}
 			}
 		}
 		//lastCollision = new Point(-1, -1);
 		return false;
 	}
 
 	@Override
 	synchronized public void onDraw(Canvas canvas) {
 		p.setColor(Color.BLACK);
 		p.setAlpha(255);
 		p.setStrokeWidth(1);
 		canvas.drawRect(0, 0, getWidth(), getHeight(), p);
 		p.setColor(Color.WHITE);
 
 		int maxX = canvas.getWidth();
 		int maxY = canvas.getHeight();
 
 		Canvas BM_canvas = new Canvas();
 		BM_canvas.setBitmap(dot.getBitmap());
 		p.setColor(Color.TRANSPARENT);
 		BM_canvas.drawRect(0, 0, 100, 100, p);
 
 		p.setColor(dot_color);
 		BM_canvas.drawCircle(50, 50, 50, p);
 		canvas.drawBitmap(dot.getBitmap(), dot.getPointX(), dot.getPointY(),
 				null);
 
 		Bitmap map;
 		p.setStrokeWidth(10);
 		for (LineObject line : line_array) {
 
 			map = line.getBitmap();
 			Point line_point = line.getPoint();
 			BM_canvas.setBitmap(map);
 			p.setColor(Color.TRANSPARENT);
 			BM_canvas.drawRect(0, 0, map.getWidth(), map.getHeight(), p);
 			p.setColor(Color.WHITE);
 			if (line.getPositive()) {
 				BM_canvas.drawLine(0 + 10, map.getHeight() - 10,
 						map.getWidth() - 10, 0 + 10, p);
 			} else {
 				BM_canvas.drawLine(0 + 10, 0 + 10, map.getWidth() - 10,
 						map.getHeight() - 10, p);
 			}
 			canvas.drawBitmap(map, line_point.x, line_point.y, null);
 		}
 
 		for (LineObject collision_line : line_array) {
 			if (checkForCollision(dot, (sprite) collision_line)) {
 				Point new_velocity = new Point();
 				Double x;
 				Double y;
 				
				int m2 = dot_velocity.y/dot_velocity.x;
				Double m1 = collision_line.getSlope();
				double theta = 2*Math.atan((m1 - m2) / (1 + m1*m2));
 				
 				x = dot_velocity.x*Math.cos(theta) - dot_velocity.y*Math.sin(theta);
 				y = dot_velocity.x*Math.sin(theta) + dot_velocity.y*Math.cos(theta);
 				new_velocity.x = x.intValue();
 				new_velocity.y = y.intValue();
 				
 				dot_velocity = new_velocity;
 				Point buffer_point = new Point(dot.getPoint());
 				buffer_point.x += dot_velocity.x * 2;
 				buffer_point.y += dot_velocity.y * 2;
 				setDot(buffer_point.x, buffer_point.y);
 
 				
 				
 				BM_canvas.setBitmap(dot.getBitmap());
 				if (dot_color == Color.YELLOW) {
 					dot_color = Color.RED;
 				} else {
 					dot_color = Color.YELLOW;
 				}
 				break;
 			} else {
 			}
 		}
 
 		// draw things
 
 	}
 
 }
