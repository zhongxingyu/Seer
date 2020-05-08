 package yang.graphics.defaults;
 
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import javax.vecmath.Point4f;
 
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.buffers.UniversalVertexBuffer;
 import yang.graphics.defaults.meshcreators.StripCreator;
 import yang.graphics.font.BitmapFont;
 import yang.graphics.font.DrawableString;
 import yang.graphics.model.FloatColor;
 import yang.graphics.programs.BasicProgram;
 import yang.graphics.textures.TextureCoordinatesQuad;
 import yang.graphics.translator.AbstractGraphics;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.math.MatrixOps;
 import yang.math.objects.Quadruple;
 import yang.math.objects.matrix.YangMatrix;
 import yang.util.Util;
 
 public abstract class DefaultGraphics<ShaderType extends BasicProgram> extends AbstractGraphics<ShaderType> {
 	
 	public static final float[][] DEFAULT_NEUTRAL_ELEMENTS = { { 0, 0, 0 }, { 0, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 } };
 
 	public static final float[] RECT_TEXTURECOORDS = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
 	public static final TextureCoordinatesQuad RECT_TEXQUAD = new TextureCoordinatesQuad().init(0, 0, 1, 1);
 
 	public static final float[] RECT_WHITE = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
 	
 	public static final int ID_POSITIONS = 0;
 	public static final int ID_TEXTURES = 1;
 	public static final int ID_COLORS = 2;
 	public static final int ID_SUPPDATA = 3;
 	public static final int ID_NORMALS = 4;
 
 	public static final float DEFAULTBIAS = 0.0018f;
 	public static final float DEFAULTTEXBIAS = 0;
 	public static final float[] NULLVECTOR = { 0, 0, 0, 0 };
 	public static final float[] BLACK = { 0, 0, 0, 1 };
 	public static final float[] WHITE = { 1, 1, 1, 1 };
 
 	public static final float[] FLOAT_ZERO_12 = {0,0,0,0,0,0,0,0,0,0,0,0};
 	
 	public StripCreator mDefaultStripCreator;
 	
 	protected float[] mInterArray = new float[1024];
 
 	//Properties
 	public BitmapFont mDefaultFont;
 	
 	//State
 	public float mCurrentZ;
 	public float[] mAmbientColor;
 	
 	// Buffers
 	protected DrawableString mInterString = new DrawableString(2048);
 	public int mPositionDimension;
 	public ShortBuffer mIndexBuffer;
 	public FloatBuffer mPositions;
 	public FloatBuffer mTextures;
 	public FloatBuffer mColors;
 	public FloatBuffer mSuppData;
 	public FloatBuffer mNormals;
 	
 	protected abstract void refreshViewTransform();
 
 	public DefaultGraphics(GraphicsTranslator translator, int positionBytes) {
 		super(translator);
 		mPositionDimension = positionBytes;
 		mDefaultStripCreator = new StripCreator(this);
 		mAmbientColor = new float[4];
 	}
 
 	@Override
 	public void restart() {
 		super.restart();
 		setAmbientColor(1);
 	}
 
 	@Override
 	protected void derivedInit() {
 		setAmbientColor(1);
 	}
 
 	@Override
 	public IndexedVertexBuffer createVertexBuffer(boolean dynamicVertices, boolean dynamicIndices, int maxIndices, int maxVertices) {
 		assert mTranslator.preCheck("create vertex buffer");
 		IndexedVertexBuffer vertexBuffer = mTranslator.createUninitializedVertexBuffer(dynamicVertices, dynamicIndices, maxIndices, maxVertices);
 		vertexBuffer.init(new int[] { mPositionDimension, 2, 4, 4 }, DEFAULT_NEUTRAL_ELEMENTS);
 		assert mTranslator.checkErrorInst("Create vertex buffer");
 		return vertexBuffer;
 	}
 
 	public void bindBuffers() {
 		assert mTranslator.checkErrorInst("PRE bind buffers 2D");
 		mTranslator.setAttributeBuffer(mCurrentProgram.mPositionHandle, DefaultGraphics.ID_POSITIONS);
 		if (mCurrentProgram.mHasTextureCoords)
 			mTranslator.setAttributeBuffer(mCurrentProgram.mTextureHandle, DefaultGraphics.ID_TEXTURES);
 		if (mCurrentProgram.mHasColor)
 			mTranslator.setAttributeBuffer(mCurrentProgram.mColorHandle, DefaultGraphics.ID_COLORS);
 		if (mCurrentProgram.mHasSuppData)
 			mTranslator.setAttributeBuffer(mCurrentProgram.mSuppDataHandle, DefaultGraphics.ID_SUPPDATA);
 		assert mTranslator.checkErrorInst("Bind buffers 2D");
 	}
 
 	public void enableBuffers() {
 		assert mTranslator.checkErrorInst("PRE enable buffers 2D");
 		mTranslator.enableAttributePointer(mCurrentProgram.mPositionHandle);
 		if (mCurrentProgram.mHasTextureCoords)
 			mTranslator.enableAttributePointer(mCurrentProgram.mTextureHandle);
 		if (mCurrentProgram.mHasColor)
 			mTranslator.enableAttributePointer(mCurrentProgram.mColorHandle);
 		if (mCurrentProgram.mHasSuppData)
 			mTranslator.enableAttributePointer(mCurrentProgram.mSuppDataHandle);
 		assert mTranslator.checkErrorInst("Enable buffers 2D");
 		//if(!mTranslator.checkErrorInst("Enable buffers 2D"))
 			//System.err.println("Handles: "+mCurrentProgram.mPositionHandle+" "+mCurrentProgram.mTextureHandle+" "+mCurrentProgram.mColorHandle+" "+mCurrentProgram.mSuppDataHandle);
 	}
 
 	public void disableBuffers() {
 		assert mTranslator.checkErrorInst("PRE disable buffers 2D");
 		mTranslator.disableAttributePointer(mCurrentProgram.mPositionHandle);
 		if (mCurrentProgram.mHasTextureCoords)
 			mTranslator.disableAttributePointer(mCurrentProgram.mTextureHandle);
 		if (mCurrentProgram.mHasColor)
 			mTranslator.disableAttributePointer(mCurrentProgram.mColorHandle);
 		if (mCurrentProgram.mHasSuppData)
 			mTranslator.disableAttributePointer(mCurrentProgram.mSuppDataHandle);
 		assert mTranslator.checkErrorInst("Disable buffers 2D");
 	}
 	
 	public void getToScreenTransform(YangMatrix target) {
 		refreshViewTransform();
 		target.set(mCameraProjectionMatrix);
 		if (mWorldTransformEnabled)
 			target.multiplyRight(mWorldTransform);
 		target.postScale(mTranslator.mRatioX, 1, 1);
		//target.scale(mTranslator.mRatioX, 1);
 	}
 
 	protected void updateProgramProjection() {
 		assert mTranslator.preCheck("Set program projection");
 		BasicProgram program = mCurrentProgram;
 		if (program != null) {
 			refreshViewTransform();
 			if (mWorldTransformEnabled) {
 				if (program.mHasWorldTransform) {
 					program.setWorldTransform(mWorldTransform.mMatrix);
 					program.setProjection(mCameraProjectionMatrix.mMatrix);
 				} else {
 					mResultTransformationMatrix.multiply(mCameraProjectionMatrix, mWorldTransform);
 					program.setProjection(mResultTransformationMatrix.mMatrix);
 				}
 			} else {
 				program.setProjection(mCameraProjectionMatrix.mMatrix);
 				if (program.mHasWorldTransform)
 					program.setWorldTransform(MatrixOps.IDENTITY);
 			}
 			if(program.mNormalTransformHandle>=0) {
 				refreshNormalTransform();
 				program.setNormalTransform(mNormalTransform);
 			}
 		}
 	}
 
 	@Override
 	public boolean setShaderProgram(ShaderType program) {
 		assert mTranslator.preCheck("Set shader program");
 		if (super.setShaderProgram(program)) {
 			if(program.mHasAmbientColor)
 				program.setAmbientColor(mAmbientColor);
 			program.setTime(mTime);
 			assert mTranslator.checkErrorInst("Set shader program");
 			return true;
 		} else
 			return false;
 	}
 
 	public float readPosition() {
 		return mPositions.get();
 	}
 
 	public void readColor(float[] target) {
 		if (target == null) {
 			mColors.get();
 			mColors.get();
 			mColors.get();
 			mColors.get();
 		} else {
 			target[0] = mColors.get();
 			target[1] = mColors.get();
 			target[2] = mColors.get();
 			target[3] = mColors.get();
 		}
 	}
 
 	// ---RECTS---
 
 	public void drawQuad(YangMatrix worldTransform, YangMatrix textureTransform) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		putTransformedPositionRect(worldTransform);
 		putTransformedTextureRect(textureTransform);
 		putColorRect(mCurColor);
 		putSuppDataRect(mCurSuppData);
 	}
 	
 	public void drawQuad(float worldX1,float worldY1,float worldX2,float worldY2,float worldX3,float worldY3,float worldX4,float worldY4, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		putPosition(worldX1,worldY1);
 		putPosition(worldX2,worldY2);
 		putPosition(worldX3,worldY3);
 		putPosition(worldX4,worldY4);
 		if(texCoordinates!=null)
 			putTextureArray(texCoordinates.mAppliedCoordinates);
 		else
 			putTextureArray(RECT_TEXTURECOORDS);
 		putColorRect(mCurColor);
 		putSuppDataRect(mCurSuppData);
 	}
 
 	public void drawQuad(YangMatrix worldTransform, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		putTransformedPositionRect(worldTransform);
 		if(texCoordinates!=null)
 			putTextureArray(texCoordinates.mAppliedCoordinates);
 		else
 			putTextureArray(RECT_TEXTURECOORDS);
 		putColorRect(mCurColor);
 		putSuppDataRect(mCurSuppData);
 	}
 
 	public void drawQuad(YangMatrix transform) {
 		drawQuad(transform, mTexIdentity);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2, float texX1, float texY1, float texX2, float texY2) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		mCurrentVertexBuffer.putRect2D(ID_TEXTURES,texX1, texY1, texX2, texY2);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, texCoordinates.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2, YangMatrix textureTransform) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		putTransformedTextureRect(textureTransform);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, mTexIdentity.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 	
 	public void drawRectWH(float left, float bottom, float width, float height, TextureCoordinatesQuad texCoords) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,left, bottom, left+width, bottom+height, mCurrentZ);
 		if(texCoords==null)
 			mCurrentVertexBuffer.putArray(ID_TEXTURES, RECT_TEXTURECOORDS);
 		else
 			mCurrentVertexBuffer.putArray(ID_TEXTURES, texCoords.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 	
 	public void drawRectCentered(float centerX, float centerY, float width, float height) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,centerX-width*0.5f,centerY-height*0.5f,centerX+width*0.5f,centerY+height*0.5f,mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, RECT_TEXTURECOORDS);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 	
 	public void drawRectCentered(float centerX, float centerY, float widthAndHeight) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		float d = widthAndHeight*0.5f;
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,centerX-d,centerY-d,centerX+d,centerY+d,mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, RECT_TEXTURECOORDS);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 	
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float angle, float texX1, float texY1, float texX2, float texY2) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRotatedRect3D(ID_POSITIONS,width,height,centerX,centerY,mCurrentZ,angle);
 		mCurrentVertexBuffer.putRect2D(ID_TEXTURES,texX1, texY1, texX2, texY2);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 	
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float angle, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRotatedRect3D(ID_POSITIONS,width,height,centerX,centerY,mCurrentZ,angle);
 //		if(mTranslator.mTimer>10) return;
 		mCurrentVertexBuffer.putArray(ID_TEXTURES,texCoordinates.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 	
 	
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float angle) {
 		drawRectCentered(centerX,centerY,width,height,angle,RECT_TEXQUAD);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float texX1, float texY1, float texX2, float texY2) {
 		mCurrentVertexBuffer.beginQuad(mTranslator.mWireFrames);
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,centerX-width*0.5f,centerY-height*0.5f,centerX+width*0.5f,centerY+height*0.5f,mCurrentZ);
 		mCurrentVertexBuffer.putVec4(ID_TEXTURES, texX1,texY1,texX2,texY2);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	/**
 	 * width = scale * texCoordRatio
 	 * height = scale
 	 */
 	public void drawRectCentered(float centerX, float centerY, float scale, float angle, TextureCoordinatesQuad texCoordinates) {
 		drawRectCentered(centerX,centerY,scale*texCoordinates.mRatioWidth,scale,angle,texCoordinates);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float scale, TextureCoordinatesQuad texCoordinates) {
 		drawRectCentered(centerX, centerY, scale, 0, texCoordinates);
 	}
 	
 	public void drawLine(float fromX,float fromY, float toX,float toY, float width, YangMatrix textureTransform) {
 		mInterTransf1.setLine(fromX, fromY, toX, toY, width);
 		drawQuad(mInterTransf1,textureTransform);
 	}
 	
 	public void drawLine(float fromX,float fromY, float toX,float toY, float width, TextureCoordinatesQuad texCoordinates) {
 		mInterTransf1.setLine(fromX, fromY, toX, toY, width);
 		drawQuad(mInterTransf1,texCoordinates);
 	}
 	
 	public void drawLine(float fromX,float fromY, float toX,float toY, float width) {
 		drawLine(fromX,fromY,toX,toY,width,mTexIdentity);
 	}
 
 	// ---PUT-POSITIONS---
 	
 	public void putTransformedPositionRect(YangMatrix transform) {
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,0,0,0, transform.mMatrix);
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,1,0,0, transform.mMatrix);
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,0,1,0, transform.mMatrix);
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,1,1,0, transform.mMatrix);
 	}
 	
 	public void putPosition(float x, float y) {
 		mCurrentVertexBuffer.putVec3(ID_POSITIONS, x, y, mCurrentZ);
 	}
 	
 	public void putPosition(float x, float y,YangMatrix transform) {
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS, x, y, mCurrentZ, transform.mMatrix);
 	}
 	
 	public void putPosition(float x, float y,float z) {
 		mCurrentVertexBuffer.putVec3(ID_POSITIONS, x, y, z);
 	}
 	
 	public void putPosition(float x,float y,float z,YangMatrix transform) {
 		transform.apply3D(x, y, z, mInterArray, 0);
 		if(mInterArray[3]!=1) {
 			float d = 1f/mInterArray[3];
 			mInterArray[0] *= d;
 			mInterArray[1] *= d;
 			mInterArray[2] *= d;
 		}
 		mPositions.put(mInterArray, 0, 3);
 	}
 
 	public void putPositionArray(float[] positions) {
 		mPositions.put(positions);
 	}
 
 	public void putPositionQuad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
 		mCurrentVertexBuffer.putVec8(ID_POSITIONS, x1, y1, x2, y2, x3, y3, x4, y4);
 	}
 
 	public void putPositionRect(float x1, float y1, float x2, float y2, float bias) {
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,x1+bias,y1+bias,x2-bias,y2-bias,mCurrentZ);
 	}
 
 	public void putPositionRect(float x1, float y1, float x2, float y2) {
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,x1,y1,x2,y2,mCurrentZ);
 	}
 	
 	public void putNormal(float nx,float ny,float nz) {
 		mCurrentVertexBuffer.putVec3(ID_NORMALS, nx, ny, nz);
 	}
 	
 	// ---PUT-TEXTURES---
 
 	public void putTextureArray(float[] texCoords) {
 		mCurrentVertexBuffer.putArray(ID_TEXTURES,texCoords);
 	}
 
 	public void putTextureCoord(float u, float v) {
 		mCurrentVertexBuffer.putVec2(ID_TEXTURES, u,v);
 	}
 	
 	public void putTextureCoordPair(float u1, int v1, float u2, float v2) {
 		mCurrentVertexBuffer.putVec4(ID_TEXTURES,u1,v1,u2,v2);
 	}
 
 	public void putTextureRect(float x1, float y1, float x2, float y2, float bias) {
 		mCurrentVertexBuffer.putVec8(ID_TEXTURES,
 				x1 - bias, y1 - bias,
 				x2 + bias, y1 - bias,
 				x1 - bias, y2 + bias,
 				x2 + bias, y2 + bias);
 	}
 
 	public void putTextureRect(float x1, float y1, float x2, float y2) {
 		putTextureRect(x1, y1, x2, y2, DEFAULTTEXBIAS);
 	}
 	
 	public void putTextureRect(TextureCoordinatesQuad texCoords) {
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, texCoords.mAppliedCoordinates);
 	}
 
 	public void putTransformedTextureRect(YangMatrix transform) {
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,0,1, transform.mMatrix);
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,1,1, transform.mMatrix);
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,0,0, transform.mMatrix);
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,1,0, transform.mMatrix);
 	}
 
 	// ---PUT-COLORS---
 
 	public void putColor(float r, float g, float b, float a) {
 		mCurrentVertexBuffer.putVec4(ID_COLORS, r, g, b, a);
 	}
 
 	public void putColor(float[] color) {
 		mCurrentVertexBuffer.putArray(ID_COLORS,color);
 	}
 
 	public void putColor(FloatColor color) {
 		mCurrentVertexBuffer.putArray(ID_COLORS,color.mValues);
 	}
 
 	public void putColorRect(float[] color) {
 		if(mCurrentProgram.mHasColor) {
 			mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,color,4);
 		}
 	}
 
 	public void putColorWhite(int amount) {
 		putColor(WHITE,amount);
 	}
 
 	public void putColor(float[] color, int amount) {
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,color, amount);
 	}
 
 	// ---AMBIENT-COLOR---
 
 	public void setAmbientColor(float r, float g, float b) {
 		flush();
 		mAmbientColor[0] = r;
 		mAmbientColor[1] = g;
 		mAmbientColor[2] = b;
 		mAmbientColor[3] = 1;
 		if (mCurrentProgram != null && mCurrentProgram.mHasAmbientColor)
 			mCurrentProgram.setAmbientColor(r, g, b);
 	}
 
 	public void setAmbientColor(float r, float g, float b, float a) {
 		flush();
 		mAmbientColor[0] = r;
 		mAmbientColor[1] = g;
 		mAmbientColor[2] = b;
 		mAmbientColor[3] = a;
 		if (mCurrentProgram != null && mCurrentProgram.mHasAmbientColor)
 			mCurrentProgram.setAmbientColor(r, g, b, a);
 	}
 
 	public void setAmbientColor(float brightness) {
 		assert mTranslator.preCheck("Set ambient color");
 		flush();
 		mAmbientColor[0] = brightness;
 		mAmbientColor[1] = brightness;
 		mAmbientColor[2] = brightness;
 		mAmbientColor[3] = 1;
 		if (mCurrentProgram != null && mCurrentProgram.mHasAmbientColor)
 			mCurrentProgram.setAmbientColor(brightness, brightness, brightness, 1);
 	}
 	
 	public void setAmbientColor(FloatColor color) {
 		setAmbientColor(color.mValues[0],color.mValues[1],color.mValues[2],color.mValues[3]);
 	}
 
 	// ---PUT-SUPPLEMENTARY-DATA---
 
 	public void putSuppData(float v1, float v2, float v3, float v4) {
 		mSuppData.put(v1);
 		mSuppData.put(v2);
 		mSuppData.put(v3);
 		mSuppData.put(v4);
 	}
 
 	public void putSuppDataZero(int amount) {
 		while (amount > 0) {
 			putSuppData(NULLVECTOR);
 			amount--;
 		}
 	}
 
 	public void putSuppData(float[] data) {
 		mSuppData.put(data);
 	}
 
 	public void putSuppData(Quadruple data) {
 		mSuppData.put(data.mValues);
 	}
 
 	public void putSuppDataRect(float[] data) {
 		putSuppData(data);
 		putSuppData(data);
 		putSuppData(data);
 		putSuppData(data);
 	}
 
 	public void putSuppData(float[] data, int amount) {
 		while (amount > 0) {
 			putSuppData(data);
 			amount--;
 		}
 	}
 
 	public String buffersToString() {
 		String res = "";
 		res += "Indices:\t" + Util.bufferToString(mIndexBuffer) + "\n";
 		res += "Positions:\t" + Util.bufferToString(mPositions) + "\n";
 		res += "Textures:\t" + Util.bufferToString(mTextures) + "\n";
 		res += "Colors:\t\t" + Util.bufferToString(mColors) + "\n";
 		res += "SuppData:\t\t" + Util.bufferToString(mSuppData) + "\n";
 		return res;
 	}
 
 	public boolean checkBufferIndices() {
 		mIndexBuffer.position(0);
 		for (int i = 0; i < mCurrentVertexBuffer.getIndexCount(); i++) {
 			int index = mIndexBuffer.get();
 			if (index >= mCurrentVertexBuffer.getVertexCount())
 				throw new RuntimeException();
 		}
 		mIndexBuffer.position(0);
 		return true;
 	}
 
 	public void setVertexBuffer(IndexedVertexBuffer vertexBuffer) {
 		super.setVertexBuffer(vertexBuffer);
 		UniversalVertexBuffer buf = (UniversalVertexBuffer) vertexBuffer;
 		mIndexBuffer = buf.mIndexBuffer;
 		FloatBuffer[] buffers = buf.mFloatBuffers;
 		mPositions = buffers[ID_POSITIONS];
 		mTextures = buffers[ID_TEXTURES];
 		mColors = buffers[ID_COLORS];
 		mSuppData = buffers[ID_SUPPDATA];
 		if (buffers.length > 4)
 			mNormals = buffers[ID_NORMALS];
 		else
 			mNormals = null;
 	}
 
 	public void drawRect(Point4f rect, TextureCoordinatesQuad texCoords) {
 		drawRect(rect.x, rect.y, rect.z, rect.w, texCoords);
 	}
 
 	public void drawRect(Point4f rect) {
 		drawRect(rect.x, rect.y, rect.z, rect.w);
 	}
 
 	public void onPreDraw() {
 		updateProgramProjection();
 		mCurrentProgram.prepareDraw();
 	}
 
 	public void setTime(float time) {
 		mTime = time;
 		if (mCurrentProgram != null)
 			mCurrentProgram.setTime(time);
 	}
 	
 	public void drawString(YangMatrix transform,DrawableString string) {
 		string.draw(transform);
 	}
 	
 	public void drawString(float x,float y,float lineHeight,DrawableString string) {
 		string.draw(x,y,lineHeight);
 	}
 	
 	public void drawString(float x,float y,float lineHeight,float rotation,DrawableString string) {
 		string.draw(x,y,lineHeight,rotation);
 	}
 
 }
