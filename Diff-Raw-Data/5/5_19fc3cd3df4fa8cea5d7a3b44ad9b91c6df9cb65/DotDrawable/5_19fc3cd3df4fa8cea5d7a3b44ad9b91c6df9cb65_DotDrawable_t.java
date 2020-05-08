 package org.umece.android.umaine;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class DotDrawable extends View {
 	Context mContext;
 	private List<Float> x_queue;
 	private List<Float> y_queue;
 	private List<Float> rad_queue;
 	private List<Integer> color_queue;
 	public int x, y;
 	public double each_width;
 	private Course course;
 	private Color color;
 	
 	public DotDrawable(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mContext = context;
 		this.course = null;
 		
 		x_queue = new ArrayList<Float>();
 		y_queue = new ArrayList<Float>();
 		rad_queue = new ArrayList<Float>();
 		color_queue = new ArrayList<Integer>();
 		color = null;
 	}
 	
 	public DotDrawable(Context context) {
 		this(context, (Course)null);
 	}
 
 	public DotDrawable(Context context, Course course) {
 		super(context);
 		mContext = context;
 		this.course = course;
 		
 		x_queue = new ArrayList<Float>();
 		y_queue = new ArrayList<Float>();
 		rad_queue = new ArrayList<Float>();
 		color_queue = new ArrayList<Integer>();
 		color = null;
 	}
 	
 	public void onChange() {
 		x_queue.clear();
 		y_queue.clear();
 		rad_queue.clear();
 		color_queue.clear();
 		
 		if (course != null) {
 			x_queue.add((float)x / 2);
 			y_queue.add((float)y / 2);
			rad_queue.add((float)0.45*x);
 			color_queue.add(course.getColor());
 		}
 		
 		if (color != null) {
 			x_queue.add((float)x / 2);
 			y_queue.add((float)y / 2);
			rad_queue.add((float)0.45*x);
 			color_queue.add(color.getColor());
 		}
 		
 		invalidate();
 	}
 
 	public void setCourse(Course crs) {
 		course = crs;
 		if (course != null) {
 			onChange();
 		}
 	}
 	
 	public void setColor(Color color) {
 		this.color = color;
 	}
 	
 	public Course getCourse() {
 		return course;
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		x = MeasureSpec.getSize(widthMeasureSpec);
 		y = MeasureSpec.getSize(heightMeasureSpec);
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		onChange();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		drawQueue(canvas);
 	}
 
 	private void drawQueue(Canvas canvas) {
 		int i;
 		
 		if (x_queue.size() != color_queue.size()) {
 			throw new RuntimeException();
 		}
 		
 		for (i = 0; i < x_queue.size(); i++) {
 			drawCirc(x_queue.get(i), y_queue.get(i), rad_queue.get(i), color_queue.get(i), canvas);
 		}
 	}
 
 	private void drawCirc(Float x, Float y, Float rad,
 			int color, Canvas canvas) {
 		Paint paint = new Paint();
 		paint.setAntiAlias(true);
 		paint.setStrokeWidth(3);
 		paint.setColor(color);
 		
 		canvas.drawCircle(x, y, rad, paint);
 	}
 
 }
