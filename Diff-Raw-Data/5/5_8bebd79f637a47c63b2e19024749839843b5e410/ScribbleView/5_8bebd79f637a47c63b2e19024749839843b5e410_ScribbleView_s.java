 package mtu.notes;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.PorterDuff;
 
 public class ScribbleView extends View{
 	private static Paint mPaint = new Paint();
 	public Canvas sCanvas;
 	public Bitmap bitmap;
 	private Path    mPath;
 	private Paint   mBitmapPaint;
 	private ArrayList<PaintStuff> Stuff = new ArrayList<PaintStuff>();
 	Context context;
 	static boolean highlighter = false;
 	static boolean eraser = false;
 	String text="";
 	public int height = 400;
 	public int width = 5000;
 
 	public ScribbleView(Context c){
 		super(c);
 		init(c);
 	}
 	public ScribbleView(Context c, AttributeSet at){
 		this(c, at,0);
 		init(c);
 	}
 
 	public ScribbleView(Context c, AttributeSet at, int defStyle){
 		super(c,at,defStyle);
 		init(c);
 	}
 
 	public static void erase(){
 		eraser = true;
 		highlighter = false;
 		mPaint = new Paint();
 		//mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
 		mPaint.setStrokeWidth(30);
 		mPaint.setColor(Color.WHITE);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.MITER);
 		mPaint.setStrokeCap(Paint.Cap.SQUARE);
 	}
 
 	public static void highlightColor(int color){
 		highlighter=true;
 		eraser = false;
 		mPaint = new Paint();
 		mPaint.setStrokeWidth(20);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.MITER);
 		mPaint.setStrokeCap(Paint.Cap.SQUARE);
 		mPaint.setColor(color);
 		mPaint.setAlpha(126);
 	}
 
 	public static void penColor(int color){
 		highlighter=false;
 		eraser = false;
 		mPaint = new Paint();
 		mPaint.setColor(color);
 		mPaint.setStrokeWidth(0);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.ROUND);
 		mPaint.setStrokeCap(Paint.Cap.ROUND);
 		mPaint.setStrokeWidth(0);
 	}
 
 	public static void setWidth(float width){
 		mPaint.setStrokeWidth(width);
 	}
 
 	public  void init(Context c) {
 		context = c;
 		this.requestFocus();
 		this.setFocusableInTouchMode(true);
 		mPath = new Path();
 		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
 		penColor(Color.BLACK);
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		if(bitmap == null)
 		{
 			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
 		}
 		sCanvas = new Canvas(bitmap);
 	}
 
 	public void toggleHighlight(){
 		if (!highlighter){
 			highlighter  = true; 
 		}
 		else{
 			highlighter = false;
 		}
 	}
 
 	public void Save(EditText editText, String path, Bitmap fBitmap) {
 		if(editText.getText().toString().isEmpty())
 		{
 			String filename = "notes.png";
 			File file = new File(path, filename);
 			try {
 				FileOutputStream out = new FileOutputStream(file);
 				fBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
 				out.close();
 				CharSequence toastText = filename + " saved.";
 				int duration = Toast.LENGTH_SHORT;
 
 				Toast toast = Toast.makeText(context, toastText, duration);
 				toast.show();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			String filename = editText.getText().toString() + ".png";
 			File file = new File(path, filename);
 			try {
 				FileOutputStream out = new FileOutputStream(file);
 				fBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
 				out.close();
 				CharSequence toastText = filename + " saved.";
 				int duration = Toast.LENGTH_SHORT;
 
 				Toast toast = Toast.makeText(context, toastText, duration);
 				toast.show();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void load(String path, String filename){
 		File file = new File(path, filename);
 		Bitmap loaded = BitmapFactory.decodeFile(file.getAbsolutePath());
 		bitmap = loaded.copy(Bitmap.Config.ARGB_8888, true);
 		invalidate();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		/*
 		if(highlighter||eraser)
 			canvas.drawPath(mPath, highlight);
 		canvas.drawBitmap(hBitmap, 0, 0, mBitmapPaint);
 		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
 		if (!highlighter||eraser)
 			canvas.drawPath(mPath,mPaint);*/
		System.out.println("here");
 		canvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
 		for(PaintStuff item : Stuff)
 		{
 			Paint temp = item.getColor();
 			canvas.drawPath(item.getPath(), temp);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		float x = event.getX();
 		float y = event.getY();
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			mPath = new Path();
 			Stuff.add(new PaintStuff());
 			if(highlighter == true)
 			{
 				Stuff.get(Stuff.size()-1).setHighlighter();
 			}
 			if(eraser == true)
 			{
 				Stuff.get(Stuff.size()-1).setEraser();
 			}
 			Stuff.get(Stuff.size()-1).setColor(mPaint);
 			mPath.moveTo(x, y);
 			Stuff.get(Stuff.size()-1).setPath(mPath);
 			invalidate();
 			break;
 		case MotionEvent.ACTION_MOVE:
 			mPath.lineTo(x, y);
 			Stuff.get(Stuff.size()-1).setPath(mPath);
 			invalidate();
 			break;
 		case MotionEvent.ACTION_UP:
 			mPath.lineTo(x, y);
 			Stuff.get(Stuff.size()-1).setPath(mPath);
 			invalidate();
 			break;
 		}
 		return true;
 	}
 	public void clear() {
		sCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
 		invalidate();
 	} 
 }
