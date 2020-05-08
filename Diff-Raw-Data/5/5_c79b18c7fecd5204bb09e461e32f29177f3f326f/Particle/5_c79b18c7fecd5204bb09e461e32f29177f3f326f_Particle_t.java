 package yang.graphics.particles;
 
 import yang.graphics.model.FloatColor;
 import yang.graphics.textures.TextureCoordinatesQuad;
 import yang.math.MathFunc;
 import yang.surface.YangSurface;
 
 public class Particle {
 	
 	//Properties
 	public TextureCoordinatesQuad mTextureCoordinates;
 	
 	//State
 	public boolean mExists;
 	public float mPosX,mPosY,mPosZ;
 	public float mNormLifeTime;
 	public float mScaleLifeTimeFactor;
 	public float mLifeTimeNormFactor;
 	public float[] mColor;
 
 	public float mRotation;
 	public float mScaleX,mScaleY;
 	
 	public Particle() {
 		mExists = false;
 		mColor = new float[4];
 		setColor(1,1,1,1);
 		mRotation = 0;
 		mScaleX = 1;
 		mScaleY = 1;
 		mScaleLifeTimeFactor = 1;
 		mTextureCoordinates = TextureCoordinatesQuad.FULL_TEXTURE;
 		mLifeTimeNormFactor = 0;
 	}
 	
 	public void setScaleLifeTimeFactor(float minFactor,float maxFactor) {
 		mScaleLifeTimeFactor = MathFunc.randomF(minFactor, maxFactor);
 	}
 	
 	public void setPosition(float x, float y, float z) {
 		mPosX = x;
 		mPosY = y;
 		mPosZ = z;
 	}
 	
 	public void setPosition(float x, float y) {
 		mPosX = x;
 		mPosY = y;
 	}
 	
 	public void setColor(float r,float g,float b,float a) {
 		mColor[0] = r;
 		mColor[1] = g;
 		mColor[2] = b;
 		mColor[3] = a;
 	}
 	
 	public void setColor(FloatColor c) {
 		setColor(c.mValues[0], c.mValues[1], c.mValues[2], c.mValues[3]);
 	}
 	
 	public void setColor(float[] color) {
		mColor[0] = color[0];
		mColor[1] = color[1];
		mColor[2] = color[2];
		mColor[3] = color[3];
 	}
 	
 	public void derivedStep() { };
 	
 	public void step() {
 		if(!mExists)
 			return;
 	    mNormLifeTime += YangSurface.deltaTimeSeconds*mLifeTimeNormFactor;
 	    
 	    derivedStep();
 	    
 	    if(mNormLifeTime>1)
 	    	mExists = false;
 	}
 	
 	public void setScale(float minScale, float maxScale) {
 		mScaleX = MathFunc.randomF(minScale, maxScale);
 		mScaleY = mScaleX;
 	}
 	
 	public void setScaleX(float minScale, float maxScale) {
 		mScaleX = MathFunc.randomF(minScale, maxScale);
 	}
 	
 	public void setScaleY(float minScale, float maxScale) {
 		mScaleY = MathFunc.randomF(minScale, maxScale);
 	}
 	
 	public void setLifeTime(float lifeTime) {
 		mLifeTimeNormFactor = 1/lifeTime;
 	}
 	
 	public void setLifeTime(float minLifeTime,float maxLifeTime) {
 		mLifeTimeNormFactor = 1/MathFunc.randomF(minLifeTime, maxLifeTime);
 	}
 	
 	public float shiftPosition2D(float minRadius, float maxRadius, float minAngle, float maxAngle) {
 		float a = MathFunc.randomF(minAngle, maxAngle); 
 		float r = MathFunc.randomF(minRadius, maxRadius);
 		mPosX += (float)(Math.cos(a)*r);
 		mPosY += (float)(Math.sin(a)*r);
 		return a;
 	}
 	
 	public float shiftPositionSpread2D(float minRadius, float maxRadius, float direction, float spreadAngle) {
 		return shiftPosition2D(minRadius,maxRadius, direction-spreadAngle*0.5f, direction+spreadAngle*0.5f);
 	}
 	
 	public void spawn(float posX, float posY, float posZ) {
 		mNormLifeTime = 0;
 		mExists = true;
 		mRotation = 0;
 		mPosX = posX;
 		mPosY = posY;
 		mPosZ = posZ;
 	}
 	
 	public void kill() {
 		mExists = false;
 	}
 
 	public void respawn() {
 		
 	}
 	
 }
