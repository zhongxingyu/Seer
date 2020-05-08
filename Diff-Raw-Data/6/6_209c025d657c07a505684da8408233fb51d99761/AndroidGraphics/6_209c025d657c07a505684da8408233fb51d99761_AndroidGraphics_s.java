 package yang.android.graphics;
 
 import java.nio.ByteBuffer;
 
 import yang.android.io.AndroidGFXLoader;
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.programs.GLProgram;
 import yang.graphics.textures.TextureRenderTarget;
import yang.graphics.textures.TextureSettings;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.graphics.translator.Texture;
 import yang.model.enums.ByteFormat;
 import android.content.Context;
 import android.opengl.GLES20;
 
 public class AndroidGraphics extends GraphicsTranslator {
 
 	public Context mContext;
 	
 	public AndroidGraphics(Context context) {
 		super();
 		mDriverKey = "ANDROID_GLES20";
 		mContext = context;
 		
 		mGFXLoader = new AndroidGFXLoader(this,mContext);
 	}
 	
 	public static final boolean clearError() {
 		GLES20.glGetError();
 		return true;
 	}
 	
 	public static final boolean checkError(String message,boolean pre) {
 		int error = GLES20.glGetError();
 		if (error != 0) {
 			if(pre)
 				System.err.println("----!GLES-ERROR!---- [PRE] "+message);
 			else
 				System.err.println("----!GLES-ERROR!---- " + message + ": "+errorCodeToString(error));
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	public static final boolean checkError(String message) {
 		return checkError(message,false);
 	}
 
 	public static int channelsToConst(int channels) {
 		switch(channels) {
 		case 3:
 			return GLES20.GL_RGB;
 		case 4:
 			return GLES20.GL_RGBA;
 			default: throw new RuntimeException(channels + " channels not supported.");
 		}
 	}
 	
 	public static int byteFormatToConst(ByteFormat byteFormat) {
 		switch(byteFormat) {
 		case BYTE: return GLES20.GL_BYTE;
 		case UNSIGNED_BYTE: return GLES20.GL_UNSIGNED_BYTE;
 		case SHORT: return GLES20.GL_SHORT;
 		case INT: return GLES20.GL_INT;
 		case FLOAT: return GLES20.GL_FLOAT;
 		default: throw new RuntimeException(byteFormat+" not supported");
 		}
 	}
 
 	@Override
 	public void postInit() {
 		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
 	}
 	
 	public GLProgram createProgram() {
 		return new AndroidGLProgram();
 	}
 
 	@Override
 	public void setClearColor(float r, float g, float b,float a) {
 		GLES20.glClearColor(r,g,b,a);
 	}
 	
 	@Override
 	public void clear(int mask) {
 		assert preCheck("Clear");
 		GLES20.glClear(mask);
 		assert checkError("Clear");
 	}
 
 	@Override
 	public void genTextures(int[] target,int count) {
 		assert preCheck("Generate texture");
 		GLES20.glGenTextures(count, target, 0);
 		assert checkError("Generate texture");
 	}
 	
 	@Override
	protected void setTextureData(int texId,int width,int height, ByteBuffer buffer, TextureSettings textureSettings) {
 		assert preCheck("Set texture data");
 		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
 		assert checkError("Bind new texture");
 		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, GLES20.GL_TRUE);
 		switch(textureSettings.mWrapX) {
 		case CLAMP: GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE); break;
 		case REPEAT: GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT); break;
 		case MIRROR: GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT); break;
 		}
 		switch(textureSettings.mWrapY) {
 		case CLAMP: GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE); break;
 		case REPEAT: GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT); break;
 		case MIRROR: GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT); break;
 		}
 		assert checkError("Set texture wrap");
 		int format;
 		int outFormat;
 		switch(textureSettings.mChannels) {
 //		case 1: 
 //			format = GLES20.GL_LUMINANCE;
 //			outFormat = GLES20.GL_RGB;
 //			break;
 		case 3: 
 			format = GLES20.GL_RGB;
 			outFormat = GLES20.GL_RGB;
 			break;
 		case 4:
 			format = GLES20.GL_RGBA;
 			outFormat = GLES20.GL_RGBA;
 			break;
 			default: throw new RuntimeException(textureSettings.mChannels + " channels not supported.");
 		}
 
 		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, outFormat, GLES20.GL_UNSIGNED_BYTE, buffer);
 		assert checkError("Pass texture data");
 		
 		switch(textureSettings.mFilter) {
 		case NEAREST: 
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
 			break;
 		case LINEAR_MIP_LINEAR:
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
 			break;
 		case NEAREST_MIP_LINEAR:
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_LINEAR);
 			break;
 		default:
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
 			break;
 		}
 		assert checkError("Set texture filter");
 	}
 
 	@Override
 	protected void drawDefaultVertices(int bufferStart, int drawVertexCount,boolean wireFrames,IndexedVertexBuffer vertexBuffer) {
 		if(wireFrames)
 			GLES20.glDrawArrays(GLES20.GL_LINES, bufferStart, drawVertexCount);
 		else
 			GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawVertexCount, GLES20.GL_UNSIGNED_SHORT, vertexBuffer.mIndexBuffer);
 	}
 	
 	@Override
 	public void derivedSetAttributeBuffer(int handle, int bufferIndex,IndexedVertexBuffer vertexBuffer) {
 		GLES20.glVertexAttribPointer(handle, vertexBuffer.mFloatBufferElementSizes[bufferIndex], GLES20.GL_FLOAT, false, 0, vertexBuffer.getFloatBuffer(bufferIndex));
 	}
 	
 	@Override
 	public void enableAttributePointer(int handle) {
 		GLES20.glEnableVertexAttribArray(handle);
 	}
 
 	@Override
 	public void disableAttributePointer(int handle) {
 		GLES20.glDisableVertexAttribArray(handle);
 	}
 
 	@Override
 	protected void setViewPort(int width, int height) {
 		GLES20.glViewport(0, 0, width, height);
 	}
 	
 	@Override
 	public void setCullMode(boolean drawClockwise) {
 		if(drawClockwise)
 			GLES20.glCullFace(GLES20.GL_FRONT);
 		else
 			GLES20.glCullFace(GLES20.GL_BACK);
 	}
 
 	@Override
 	public void deleteTextures(int[] ids) {
 		assert preCheck("Delete textures");
 		GLES20.glDeleteTextures(ids.length, ids, 0);
 		assert checkError("Delete textures");
 	}
 
 	@Override
 	public void derivedSetScreenRenderTarget() {
 		assert preCheck("Set screen render target");
 		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
 		assert checkError("Set screen render target");
 	}
 	
 	@Override
 	public TextureRenderTarget derivedCreateRenderTarget(Texture texture) {
 		assert preCheck("Create render target");
 		GLES20.glGenFramebuffers(1, mTempInt, 0);
 		GLES20.glGenRenderbuffers(1, mTempInt2, 0);
 		int frameId = mTempInt[0];
 		int depthId = mTempInt2[0];
 		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthId);
 		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, texture.getWidth(), texture.getHeight());
 //		while(GLES20.glCheckFramebufferStatus(depthId)==0 && GLES20.glCheckFramebufferStatus(frameId)==0) {System.out.println(GLES20.glCheckFramebufferStatus(depthId)+GLES20.glCheckFramebufferStatus(frameId));
 //			try {
 //				Thread.sleep(10);
 //			} catch (InterruptedException e) {
 //				e.printStackTrace();
 //			}
 //		}
 		assert checkError("Create render target");
 		return new TextureRenderTarget(texture,frameId,depthId);
 	}
 
 	@Override
 	public void derivedSetTextureRenderTarget(TextureRenderTarget renderTarget) {
 		assert preCheck("Set texture render target");
 		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, renderTarget.mFrameBufferId);
 		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTarget.mTargetTexture.mId, 0);
 		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderTarget.mDepthBufferId);
 		assert checkError("Set texture render target");
 	}
 
 	@Override
 	public void setDepthFunction(boolean less) {
 		if(less)
 			GLES20.glDepthFunc(GLES20.GL_LESS);
 		else
 			GLES20.glDepthFunc(GLES20.GL_GREATER);
 		assert checkError("Set depth function");
 	}
 	
 	@Override
 	public void bindTexture(int texId,int level) {
 		GLES20.glActiveTexture(GLES20.GL_TEXTURE0+level);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
 		assert checkError("Bind texture");
 	}
 
 	@Override
 	public void generateMipMap() {
 		assert preCheck("Generate mip map");
 		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
 		assert checkError("Generate mip map");
 	}
 	
 	@Override
 	public void readPixels(int x,int y,int width,int height,int channels,ByteFormat byteFormat,ByteBuffer target) {
 		if(byteFormat!=ByteFormat.UNSIGNED_BYTE)
 			throw new RuntimeException("Android read pixels only supports unsigned byte");
 		assert preCheck("Read pixels");
 		GLES20.glReadPixels(x, y, width, height, channelsToConst(channels), GLES20.GL_UNSIGNED_BYTE, target);
 		assert checkError("Read pixels");
 	}
 
 	@Override
 	public boolean checkErrorInst(String message,boolean pre) {
 		return checkError(message,pre);
 	}
 
 	@Override
 	public void enable(int glConstant) {
 		GLES20.glEnable(glConstant);
 	}
 
 	@Override
 	public void disable(int glConstant) {
 		GLES20.glDisable(glConstant);
 	}
 	
 	@Override
 	public void setBlendFunction(int sourceFactor,int destFactor) {
 		GLES20.glBlendFunc(sourceFactor, destFactor);
 	}
 
 	@Override
 	public void setStencilFunction(int function, int ref, int mask) {
 		GLES20.glStencilFunc(function, ref, mask);
 	}
 
 	@Override
 	public void setStencilOperation(int fail, int zFail, int zPass) {
 		GLES20.glStencilOp(fail,zFail,zPass);
 	}
 
 	@Override
 	public void setScissorRectI(int x, int y, int width, int height) {
 		GLES20.glScissor(x, y, width, height);
 	}
 
 }
