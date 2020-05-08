 package org.droidraw;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.SweepGradient;
 import android.graphics.Canvas.VertexMode;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 
 public class BrushDialog extends Dialog {
 
 	public interface OnBrushChangedListener {
 		void brushChanged(int color, int size);
 	}
 
 	private static class ColorView extends View {
 
 		private final int[] colors;
 		private final float[] points;
 
 		public int getCircleColor() {
 			float th = (float)(this.th * ((colors.length - 1) / (2 * Math.PI)));
 			int idx = (int)Math.floor(th);
 			Log.v("DroiDraw", "got idx " + idx);
 			int c1 = colors[idx];
 			int c2 = colors[idx + 1];
 			float t = th - idx;
 			return Color.argb(
 				255,
 				(int)(Color.red(c1) + (Color.red(c2) - Color.red(c1)) * t),
 				(int)(Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t),
 				(int)(Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * t)
 			);
 		}
 		
 		private Paint wheelPaint;
 		private Paint selPaint;
 		private Paint triPaint;
 		private float th = 0.0f;
 		private float px = 70;
 		private float py = 0;
 		public float i = 0;
 		public float j = 0;
 
 		public ColorView(Context context) {
 			super(context);
 			colors = new int[] {
 				Color.argb(255, 255,   0,   0),
 				Color.argb(255, 255,   0, 255),
 				Color.argb(255,   0,   0, 255),
 				Color.argb(255,   0, 255, 255),
 				Color.argb(255,   0, 255,   0),
 				Color.argb(255, 255, 255,   0),
 				Color.argb(255, 255,   0,   0)
 			};
 
 			points = new float[] {
 					100 + (float)Math.cos(0) * 70, 100 + (float)Math.sin(0) * 70,
 					100 + (float)Math.cos((1.0 / 3) * Math.PI * 2) * 70, 100 + (float)Math.sin((1.0 / 3) * Math.PI * 2) * 70,
 					100 + (float)Math.cos((2.0 / 3) * Math.PI * 2) * 70, 100 + (float)Math.sin((2.0 / 3) * Math.PI * 2) * 70 };
 
 			wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			wheelPaint.setShader(new SweepGradient(100, 100, colors, null));
 			wheelPaint.setStyle(Paint.Style.STROKE);
 			wheelPaint.setStrokeWidth(20);
 
 			selPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			selPaint.setStyle(Paint.Style.STROKE);
 			selPaint.setStrokeWidth(2);
 
 			triPaint = new Paint(Paint.DITHER_FLAG);
 			triPaint.setStyle(Paint.Style.FILL);
 		}
 
 		private boolean wheelDrag = false; 
 		private boolean colorDrag = false; 
 		
 		@Override public boolean onTouchEvent(MotionEvent event) {
 			float x = event.getX() - 100;
 			float y = event.getY() - 100;
 			float dist = (float)Math.sqrt(x*x + y*y);
 			float ni = (x / 70) * 2.0f / 3 + 1.0f / 3;
 			float nj = (x / 70) * -1.0f / 3 + (y / 70) * (float)-Math.sqrt(3) / 3 + 1.0f / 3;
 
 			int a = event.getAction();
 			if (a == MotionEvent.ACTION_DOWN) {
 				if (Math.abs(80 - dist) < 20)
 					wheelDrag = true;
 				else if (Math.min(ni, Math.min(nj, 1 - ni - nj)) >= 0)
 					colorDrag = true;
 			}
 
 			if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE || 
 			    a == MotionEvent.ACTION_UP) {
 				if (wheelDrag) {
 					th = (float)(Math.atan2(-y, -x) + Math.PI);
 					invalidate();
 				}
 				else if (colorDrag) {
 					float k = Math.max(0, Math.min(1 - ni - nj, 1));
 					i = Math.max(0, Math.min(ni, 1));
 					j = Math.max(0, Math.min(nj, 1));
 					float len = i + j + k;
 					i /= len;
 					j /= len;
 
 					Log.v("DroiDraw", "i=" + i + ", j=" + j);
 					px = 35 * (3 * i - 1);
 					py = -(210 * j + px - 70) / (float)Math.sqrt(3);
 					invalidate();
 				}
 			}
 
 			if (a == MotionEvent.ACTION_UP)
 				wheelDrag = colorDrag = false;				
 
 			return true;
 		}
 
 		@Override protected void onDraw(Canvas canvas) {
 			float r = 80;
 			canvas.drawCircle(100, 100, r, wheelPaint);
 			canvas.drawCircle(100 + (float)Math.cos(th) * r, 100 + (float)Math.sin(th) * r, 10, selPaint);
 			int [] colors = new int[] {
					getCircleColor(),
					Color.argb(255,   0,   0,   0),
					Color.argb(255, 255, 255, 255),
					0, 0, 0 // pad due to a bug in the Android runtime...
 			};
 
 			canvas.drawVertices(VertexMode.TRIANGLES, 6,
 			    points, 0, // verts
 			    null, 0, // texs
 			    colors, 0, // colors
 			    null, 0, 0, // indices
 			    triPaint);
 /*
 			canvas.drawVertices(VertexMode.TRIANGLES, 6,
 				    points, 0, // verts
 				    null, 0, // texs
 				    null, 0, // colors
 				    null, 0, 0, // indices
 				    selPaint); */
 			canvas.drawCircle(100 + px, 100 + py, 10, selPaint);
 		}
 
 		@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 			setMeasuredDimension(200, 200);
 		}
 	}
 
 	private ColorView colorView;
 	private SeekBar alphaBar;
 	private SeekBar sizeBar;
 	private Button okButton;
 	private Button cancelButton;
 	private OnBrushChangedListener listener;
 
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		LinearLayout layout = new LinearLayout(getContext());
 		layout.setOrientation(LinearLayout.VERTICAL);
 
 		colorView = new ColorView(getContext());
 		layout.addView(colorView);
 		
 		alphaBar = new SeekBar(getContext());
 		alphaBar.setMax(255);
 		alphaBar.setProgress(255);
 		layout.addView(alphaBar);
 
 		sizeBar = new SeekBar(getContext());
 		sizeBar.setMax(64);
 		sizeBar.setProgress(8);
 		layout.addView(sizeBar);
 
 		okButton = new Button(getContext());
 		okButton.setText("OK");
 		okButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				int c = colorView.getCircleColor();
 				c = Color.argb(
 					alphaBar.getProgress(),
 					(int)Math.min(255, Color.red(c) * colorView.i + 255 * colorView.j),
 					(int)Math.min(255, Color.green(c) * colorView.i + 255 * colorView.j),
 					(int)Math.min(255, Color.blue(c) * colorView.i + 255 * colorView.j)
 				);
 				listener.brushChanged(c, sizeBar.getProgress());
 				dismiss();
 			}
 		});
 		layout.addView(okButton);
 
 		cancelButton = new Button(getContext());
 		cancelButton.setText("Cancel");
 		cancelButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				cancel();
 			}
 		});
 		layout.addView(cancelButton);
 
 		// we're ready!
 		setContentView(layout);
 		setTitle("Pick a Color");
 	}
 
 	
 	public BrushDialog(Context context) {
 		super(context);
 	}
 
 	public BrushDialog(Context context, OnBrushChangedListener listener) {
 		super(context);
 		this.listener = listener;
 	}
 }
