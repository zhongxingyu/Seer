 package ru.jauseg.snowpaper;
 
 import java.nio.Buffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import js.engine.BufferAllocator;
 import js.engine.FrameRateCalculator;
 import js.engine.QuadBuffer;
 import js.engine.SimplexNoise;
 import js.engine.TextureManager;
 import js.engine.FrameRateCalculator.FrameRateUpdateInterface;
 import js.gesture.FlingDetector;
 import js.gesture.FlingDetector.FlingDetectorListener;
 import js.jni.code.NativeCalls;
 import net.rbgrn.android.glwallpaperservice.GLWallpaperService;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.opengl.GLU;
 import android.opengl.GLUtils;
 import android.os.SystemClock;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.MotionEvent;
 
 public class SnowWallpaperRenderer implements GLWallpaperService.Renderer, FlingDetectorListener,
 		FrameRateUpdateInterface
 {
 	private static final String TAG = "SnowWallpaperRenderer";
 
 	private FlingDetector flingDetector;
 
 	private Bitmap bitmapShows;
 	private Bitmap bitmapNoise;
 
 	private TextureManager textures;
 	private int textureSnowsIndex = 0;
 	private int textureNoiseIndex = 0;
 	private QuadBuffer posBuffer = new QuadBuffer(-0.5f, -0.5f, 0.5f, 0.5f);
 
 	private int framesToSkip = 0;
 
 	private float width;
 	private float height;
 	private float offset = 0.5f;
 
 	private FrameRateCalculator fps;
 
 	public SnowWallpaperRenderer(Context context)
 	{
 		flingDetector = new FlingDetector(this);
 		bitmapShows = TextureManager.load(context, "snows.png");
 		fps = new FrameRateCalculator(this);
 
 		app.indexSnowCount(app.indexSnowCount());
 		app.indexSnowSpeed(app.indexSnowSpeed());
 		app.indexTurbulence(app.indexTurbulence());
 		int size = 32;
 		bitmapNoise = Bitmap.createBitmap(size, size, Config.ARGB_8888);
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config)
 	{
 		Log.v(TAG, "onSurfaceCreated");
 
 		gl.glDisable(GL10.GL_DEPTH_TEST);
 		gl.glDisable(GL10.GL_LIGHTING);
 		gl.glDisable(GL10.GL_CULL_FACE);
 
 		gl.glDisable(GL10.GL_DITHER);
 
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
 
 		gl.glEnable(GL10.GL_TEXTURE_2D);
 
 		gl.glShadeModel(GL10.GL_SMOOTH);
 		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
 
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 
 		gl.glClearColor(0, 0, 0, 0);
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height)
 	{
 		Log.v(TAG, "onSurfaceChanged");
 
 		this.width = width;
 		this.height = height;
 
 		if (textures == null)
 		{
 			textures = new TextureManager();
 			generateTextureCoords();
 		}
 
 		if (bitmapShows.isRecycled() == false)
 		{
 			textureSnowsIndex = textures.createTexture(gl, bitmapShows, true);
 			textureNoiseIndex = textures.createTexture(gl, TextureManager.noise(32, 16, 0), true);
 		}
 
 		textures.useTexture(gl, textureSnowsIndex);
 
 		gl.glMatrixMode(GL10.GL_PROJECTION);
 		gl.glLoadIdentity();
 
 		gl.glViewport(0, 0, width, height);
 		GLU.gluOrtho2D(gl, 0, width, height, 0);
 
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 
 		if (app.ss == null)
 		{
 			Log.v(TAG, "app.ss == null");
 			app.ss = new SnowSystemNative(width, height, textures);
 		}
 		else
 		{
 			if (app.ss.getWidth() != width)
 			{
 				Log.v(TAG, "width change");
 				app.ss = new SnowSystemNative(width, height, textures);
 			}
 		}
 	}
 
 	int noiseCounter = 0;
 
 	@Override
 	public void onDrawFrame(GL10 gl)
 	{
 		while (framesToSkip > 0)
 		{
 			SystemClock.sleep(18);
 			app.ss.skip();
 			framesToSkip--;
			noiseCounter++;
 		}
 		framesToSkip = app.indexFramesSkip();
 
 		fps.frameBegin();
 
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 
 		// gl.glDisable(GL10.GL_BLEND);
 
 		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, posBuffer.buffer);

 		if (app.isBackgroundStatic())
 		{
 			textures.useTexture(gl, textureSnowsIndex);
 			textures.useCoords(gl, 8);
 		}
 		else
 		{
 			noise(bitmapNoise, 16, 0, ((float) noiseCounter) / 500.0f);
 			textures.useTexture(gl, textureNoiseIndex);
 			textures.useCoords(gl, 9);
 			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapNoise, 0);
 		}
 		gl.glPushMatrix();
 		gl.glColor4f(1, 1, 1, app.getMotionBlur());
 
 		gl.glTranslatef(width * (-offset + 1.0f), height * 0.5f, 0);
 		gl.glScalef(width * 2.01f, height * 1.01f, 1);
 		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
 		gl.glPopMatrix();
 		// gl.glEnable(GL10.GL_BLEND);
 
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
 		textures.useTexture(gl, textureSnowsIndex);
 		app.ss.draw(gl);
 
 		fps.frameDone();
 		// SystemClock.sleep(10);
 	}
 
 	public void offset(float offset)
 	{
 		this.offset = offset;
 	}
 
 	public synchronized void release()
 	{
 		Log.v(TAG, "release");
 
 		if (bitmapShows.isRecycled() == false)
 		{
 			Log.v(TAG, String.format("bitmap %s is recycled", bitmapShows));
 			bitmapShows.recycle();
 		}
 	}
 
 	public boolean onTouch(MotionEvent event)
 	{
 		return flingDetector.onTouchEvent(event);
 	}
 
 	@Override
 	public void onFling(float fromX, float fromY, float toX, float toY, int deltaTime)
 	{
 		app.ss.fling(fromX, fromY, toX - fromX, toY - fromY);
 	}
 
 	private void generateTextureCoords()
 	{
 		for (int i = 0; i < 8; i++)
 		{
 			int x = (i % 4) * 64;
 			int y = (i / 4) * 64;
 			textures.createCoords(x, y, 64, 64, 256, 256);
 		}
 
 		textures.createCoords(0, 128, 256, 128, 256, 256);
 		textures.createCoords(0, 0, 32, 32, 32, 32);
 	}
 
 	@Override
 	public void onFrameRateUpdate(FrameRateCalculator frameRateCalculator)
 	{
 		// Log.v(TAG, frameRateCalculator.frameString());
 	}
 
 	Buffer pixels = null;
 
 	public void noise(Bitmap bmp, float freq, float offset, float time)
 	{
 		int size = bmp.getWidth();
 
 		if (pixels == null)
 		{
 			pixels = BufferAllocator.createBuffer(4 * size * size);
 		}
 
 		NativeCalls.noise(pixels, size, freq, offset, time);
 
 		// bmp.setPixels(pixels, 0, size, 0, 0, size, size);
 		pixels.position(0);
 		bmp.copyPixelsFromBuffer(pixels);
 
 	}
 
 }
