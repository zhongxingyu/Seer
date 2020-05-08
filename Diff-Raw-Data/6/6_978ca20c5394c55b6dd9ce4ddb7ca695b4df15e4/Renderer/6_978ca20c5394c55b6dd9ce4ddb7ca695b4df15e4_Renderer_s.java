 package com.gmail.sid9102.eyewallpaper;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import rajawali.BaseObject3D;
 import rajawali.animation.Animation3D;
 import rajawali.animation.RotateAnimation3D;
 import rajawali.lights.ALight;
 import rajawali.lights.DirectionalLight;
 import rajawali.lights.PointLight;
 import rajawali.materials.DiffuseMaterial;
 import rajawali.materials.GouraudMaterial;
 import rajawali.materials.SimpleMaterial;
 import rajawali.materials.PhongMaterial;
 import rajawali.materials.TextureManager.TextureType;
 import rajawali.math.Number3D;
 import rajawali.parser.AParser.ParsingException;
 import rajawali.parser.ObjParser;
 import rajawali.primitives.Cube;
 import rajawali.primitives.Sphere;
 import rajawali.renderer.RajawaliRenderer;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.animation.AccelerateDecelerateInterpolator;
 
 public class Renderer extends RajawaliRenderer {
 	private BaseObject3D face;
 	private BaseObject3D cornea;
 	private BaseObject3D eyeBall;
 	private Bitmap irisTex;
 	private Bitmap faceTex;
 	
 	private boolean reset;
 	private float xResult;
 	private float yResult;
 	private float oldYRot;
 	private float oldXRot;
 	private long timeUp;	
 	private boolean cleanUp;
 	
 	private UserPrefs mUserPrefs;
 	private boolean disembodied;
 
 	public Renderer(Context context) {
 		super(context);
 		// Application context so that we do not keep a reference to a service
 		// that might not be needed any longer such as the preview.
 		mUserPrefs = UserPrefs.getInstance(context.getApplicationContext());
 	}
 
 	public void initScene() {
 		
 		ALight light = new DirectionalLight();
 		light.setPower(1.2f);
 		light.setPosition(-3, 3, -10);
 		light.setLookAt(0, 0, 0);
 		
 		ALight cLight = new DirectionalLight();
 		cLight.setPower(0.8f);
 		cLight.setPosition(-3, 10, -10);
 		cLight.setLookAt(0, 0, 0);
 		
 		mCamera.setPosition(0, 0.2f, -4.6f);
 		mCamera.setLookAt(0, 0.2f, 0);
 		
 		disembodied = mUserPrefs.getBody();
 		
 		ObjParser faceParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.face_obj);
 		try {
 			faceParser.parse();
 			face = faceParser.getParsedObject();
 			SimpleMaterial fMat = new SimpleMaterial();
 			face.setMaterial(fMat);
 			faceTex = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.faceprototype);
 			face.addTexture(mTextureManager.addTexture(faceTex));
 			face.setTransparent(true);
 			face.addLight(light);
 		} catch (ParsingException e) {
 			e.printStackTrace();
 		}
 		
 		ObjParser corneaParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.cornea_obj);
 		try {
 			corneaParser.parse();
 			cornea = corneaParser.getParsedObject();
 			GouraudMaterial cMat = new GouraudMaterial();
 			irisTex = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iris);
 			cMat.addTexture(mTextureManager.addTexture(irisTex, TextureType.DIFFUSE));
 			cMat.setSpecularIntensity(0.01f, 0.01f, 0.01f, 0.01f);
 			
 			cornea.setMaterial(cMat);
 			cornea.addLight(cLight);			
 		} catch (ParsingException e) {
 			e.printStackTrace();
 		}
 		
 		ObjParser eyeParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.eyeball_obj);
 		try {
 			eyeParser.parse();
 			eyeBall = eyeParser.getParsedObject();
 			eyeBall.addLight(light);
 			addChild(eyeBall);
 			eyeBall.addChild(cornea);
 			if(!disembodied)
 			{
 				addChild(face);
 			}
 		} catch (ParsingException e) {
 			e.printStackTrace();
 		}
 		
 		eyeBall.setPosition(eyeBall.getX() + 0.05f, eyeBall.getY() - 0.018f, eyeBall.getZ());
 		// Prevent the eyeball from warping to center position when scene is cleaned up
 		eyeBall.setRotX(oldXRot);
 		eyeBall.setRotY(oldYRot);
 	}
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		super.onSurfaceCreated(gl, config);
 		xResult = 0;
 		yResult = 0;
 		reset = true;
 		Log.e("surface", "created");
 		//Prevent garbage textures/model when recreating surface
 		cleanUp = true;
 	}
 	
 	@Override
 	public void onSurfaceDestroyed(){
 		super.onSurfaceDestroyed();
 		Log.e("surface", "destroyed");
 		irisTex.recycle(); // Clears the bitmap when the app (or LWP/LWP preview) dies
 		faceTex.recycle();
 		System.gc(); // Forces System garbage collection to take place
 	}
 	
 	@Override
     public void onTouchEvent(MotionEvent event)
 	{
 		int action = event.getAction();
 		float touchX = event.getX();
 		float touchY = event.getY();
 		
 		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)
 		{
 			reset = false;
 			
 			xResult = (touchX - (mViewportWidth / 2)) * (60 / ((float) mViewportWidth));
 			xResult = Math.round(xResult);
 			xResult = -xResult;
 			
 			yResult = (touchY - (mViewportHeight / 2)) * (45 / ((float) mViewportHeight));
 			yResult = Math.round(yResult);
 		}		
 		
 		if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP)
 		{
 			timeUp = System.currentTimeMillis();
 			reset = true;
 		}
 	}
 
 	public void onDrawFrame(GL10 glUnused) {
 		super.onDrawFrame(glUnused);
 
 		if(cleanUp)
 		{
 			cleanUp = false;
 			destroyScene();
 			initScene();
 		}
 		
 		if(reset && ((System.currentTimeMillis() - timeUp) >= 400))
 		{
 			if(disembodied != mUserPrefs.getBody())
 			{
 				destroyScene();
 				initScene();
 			}
 			
 			//Make sure rotations are whole numbers!
 			float wholeX = Math.round(eyeBall.getRotY());
 			float wholeY = Math.round(eyeBall.getRotX());
 			eyeBall.setRotY(wholeX);
 			eyeBall.setRotX(wholeY);
 			
			oldYRot = eyeBall.getRotY();
			oldXRot = eyeBall.getRotX();
 			
 			if (eyeBall.getRotY() != 0) {
 				if (eyeBall.getRotY() < 0) {
 					eyeBall.setRotY(eyeBall.getRotY() + 1);
 				} else
 					eyeBall.setRotY(eyeBall.getRotY() - 1);
 			}
 			if (eyeBall.getRotX() != 0) {
 				if (eyeBall.getRotX() < 0) {
 					eyeBall.setRotX(eyeBall.getRotX() + 1);
 				} else
 					eyeBall.setRotX(eyeBall.getRotX() - 1);
 			}
 		}
 		else
 		{
 			if(eyeBall.getRotY() != xResult)
 			{
 				if(eyeBall.getRotY() < xResult)
 				{
 					float delta = xResult - eyeBall.getRotY();
 					if(delta < 5)
 					{
 						eyeBall.setRotY(eyeBall.getRotY() + delta);
 					}
 					else
 					{
 						eyeBall.setRotY(eyeBall.getRotY() + 5);
 					}
 				}
 				else
 				{
 					float delta = eyeBall.getRotY() - xResult;
 					if(delta < 5)
 					{
 						eyeBall.setRotY(eyeBall.getRotY() - delta);
 					}
 					else
 					{
 						eyeBall.setRotY(eyeBall.getRotY() - 5);
 					}
 				}
 			}
 			
 			if(eyeBall.getRotX() != yResult)
 			{
 				if(eyeBall.getRotX() < yResult)
 				{
 					float delta = yResult - eyeBall.getRotX();
 					if(delta < 5)
 					{
 						eyeBall.setRotX(eyeBall.getRotX() + delta);
 					}
 					else
 					{
 						eyeBall.setRotX(eyeBall.getRotX() + 5);
 					}
 				}
 				else
 				{
 					float delta = eyeBall.getRotX() - yResult;
 					if(delta < 5)
 					{
 						eyeBall.setRotX(eyeBall.getRotX() - delta);
 					}
 					else
 					{
 						eyeBall.setRotX(eyeBall.getRotX() - 5);
 					}
 				}
 			}
 		}		
 	}
 }
