 package net.ednovak.zip;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class Freestyle extends Activity implements Runnable{
 	
 	private Thread t;
 	private ZipSurfaceView SV;
 	private double curFreq = 400;
 	private AudioTrack audioTrack;
 	private final int sampleRate = 8000;
 	private boolean fingerDown;
 	private int ATBufferSize;
 	private byte[] myBuffer;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    SV = new ZipSurfaceView(this);
 		setContentView(SV);
 		
 		ATBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
 		Log.d("Freestyle:onCreate", "bufferSize: " + ATBufferSize);
 		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
 				 AudioFormat.CHANNEL_CONFIGURATION_MONO, 
 				 AudioFormat.ENCODING_PCM_16BIT, ATBufferSize,
 				 AudioTrack.MODE_STREAM);
 		
 		//requestWindowFeature(Window.FEATURE_NO_TITLE);
 		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		changeTone(100, 200);
 
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 		SV.onResumeSV();
 	}
 	
 	@Override
 	protected void onPause(){
 		super.onPause();
 		SV.onPauseSV();
 	}
 	
 	
 	protected void changeTone(double y, double max){
 		// first number + (percentage of X)  hz range of [firstnumber, firstnumber+lastNumber]
 		curFreq = 100 + ((y / max) * 700); 
 		
 		int length = (int) (Math.round(sampleRate / curFreq) + 1);
 		myBuffer = new byte[length*2];
 
 		for(int i = 0; i < length; i++){
 			double val = sampleSin(i);
 			short sVal = (short)(val * (Short.MAX_VALUE));
 			myBuffer[i*2] = (byte) (sVal & 0x00ff);
 			myBuffer[i*2 + 1] = (byte) ((sVal & 0xff00) >>> 8);
 		}
 	}
 	
 	private void startSoundThread(){
 		t = new Thread(this);
 		t.start();
 	}
 	
 	private void stopSoundThread(){
 		audioTrack.pause();
 		audioTrack.flush();
 		audioTrack.stop();
		//audioTrack.stop();
 	}
 	
 	
 	private double sampleSin(int index){
 		return Math.sin( 2 * Math.PI * index * (curFreq/sampleRate));
 	}
 	
 	@Override
 	public void run(){
 		audioTrack.play();
 		while(fingerDown){
 			audioTrack.write(myBuffer, 0, myBuffer.length);
 		}
 	}
 
 	
 	class ZipSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
 		SurfaceHolder surfaceHolder;
 		private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
 		public ZipSurfaceView(Context ctx){
 			super(ctx);
 			surfaceHolder = getHolder();
 			surfaceHolder.addCallback(this);
 			p.setColor(Color.BLACK);
 		}
 		
 		public void onResumeSV(){
 
 		}
 		
 		public void onPauseSV(){

 		}
 		
 		
 		protected void reDraw(Canvas c, MotionEvent e){
 			
 			double freq = .02;
 			int red =(int) (Math.sin(freq*e.getX() + 0) * 127) + 128;
 			int green =(int) (Math.sin(freq*e.getX() + 2) * 127) + 128;
 			int blue =(int) (Math.sin(freq*e.getX() + 4) * 127) + 128;
 
 			c.drawColor(Color.rgb(red, green, blue));
 			float rad = (float) ( 20 + ((e.getY() / c.getHeight()) * c.getWidth()/2) );
 			c.drawCircle(e.getX(), e.getY(), rad, p);
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder surfaceHolder){
 			
 		}
 		
 		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
 			Log.d("ZipSurfaceView:surfaceChange", "the surface changed for some reason?");
 		}
 		
 		public void surfaceCreated(SurfaceHolder holder){
 			surfaceHolder = holder;
 		}
 		
 		@Override
 		public boolean onTouchEvent(MotionEvent event){
 			//Log.d("zipSurfaceView:onTouch", "touched!");
 			
 			if (event.getAction() == MotionEvent.ACTION_DOWN){
 				fingerDown = true;
 				startSoundThread();
 				Log.d("Freestyle:onTouchEvent", "Recorded action down");
 				
 			
 			}
 			if (event.getAction() == MotionEvent.ACTION_UP){
 				Log.d("Freestyle:onTouchEvent", "Recorded action up");
 				fingerDown = false;
 				stopSoundThread();
 			}
 			
 			
 			Canvas c = null;
 			while (c == null){
 				c = surfaceHolder.lockCanvas();
 			}
 			changeTone(c.getHeight() - event.getY(), c.getWidth());
 			reDraw(c, event);
 			surfaceHolder.unlockCanvasAndPost(c);
 			
 
 			
 			return true;
 			//return super.onTouchEvent(event);
 		}
 		
 		
 	}
 }
