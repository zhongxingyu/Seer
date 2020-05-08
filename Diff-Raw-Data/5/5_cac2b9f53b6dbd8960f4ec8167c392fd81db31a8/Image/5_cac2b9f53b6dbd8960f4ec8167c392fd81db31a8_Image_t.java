 /*
  * Copyright 2008 Markus Koller
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.blackspirit.graphics.jogl;
 
 import java.io.IOException;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import javax.media.opengl.GL;
 
 import ch.blackspirit.graphics.BufferType;
 
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureData;
 import com.sun.opengl.util.texture.TextureIO;
 
 /**
  * @author Markus Koller
  */
 public class Image implements ch.blackspirit.graphics.Image {
 	private final int width;
 	private final int height;
 	private final URL url;
 	private final boolean buffered;
 	private final BufferType bufferType;
 	private final boolean alpha;
 	private final boolean forceAlpha;
 
 	
 	ResourceManager resourceManager;
 	
 	private final byte[] bytes;
 	final ByteBuffer byteBuffer; 
 	private TextureData textureData;
 	// texture being set, indicates that the image is cached!
 	public Texture texture = null;
 	
 	public Image(URL url, ResourceManager resourceManager, boolean buffered, boolean forceAlpha) throws IOException {
 		this.url = url;
 		this.resourceManager = resourceManager;
 		this.buffered = buffered;
 		this.forceAlpha = forceAlpha;
 		
 		if(buffered) {
 			if(forceAlpha) throw new UnsupportedOperationException("Loading buffered image from url forcing alpha must be manually done using explicit buffer type");
 			textureData = TextureIO.newTextureData(url, false, null);
 			if(textureData.getPixelFormat() == GL.GL_RGBA) {
 				bufferType = BufferTypes.RGBA_4Byte;
 				this.alpha = true;
 			} else if(textureData.getPixelFormat() == GL.GL_RGB) {
 				bufferType = BufferTypes.RGB_3Byte;
 				this.alpha = false;
 			} else {
 				throw new RuntimeException("Unsupported pixel format: " + textureData.getPixelFormat());
 			}
 			this.width = textureData.getWidth();
 			this.height = textureData.getHeight();
 			this.byteBuffer = ((ByteBuffer)textureData.getBuffer());
 			this.bytes = byteBuffer.array();
 		} else {
 			// preload data to now image size
 			this.textureData = createTextureData();
 			this.alpha = textureData.getPixelFormat() == GL.GL_RGBA; 
 			this.width = textureData.getWidth();
 			this.height = textureData.getHeight();
 			this.bytes = null;
 			this.byteBuffer = null;
 			this.bufferType = null;
 		}
 	}
 	
 	public Image(int width, int height, ResourceManager resourceManager, boolean alpha) {
 		this.width = width;
 		this.height = height;
 		this.bufferType = null;
 		this.byteBuffer = null;
 		this.bytes = null;
 		this.buffered = false;
 		this.alpha = alpha;
 		this.forceAlpha = false;
 		this.resourceManager = resourceManager;
 		this.url = null;
 	}
 
 	public Image(URL url, ResourceManager resourceManager, BufferType bufferType) throws IOException {
 		this.url = url;
 		this.resourceManager = resourceManager;
 		this.bufferType = bufferType;
 		this.buffered = true;
 		this.forceAlpha = false;
 		
 		if(bufferType == BufferTypes.RGBA_4Byte) {
 			TextureData tempData = TextureIO.newTextureData(url, GL.GL_RGBA, GL.GL_RGBA, false, null);
 			if(tempData.getPixelFormat() != GL.GL_RGBA) {
 				textureData = convertToRGBA(tempData);
 				tempData.flush();
 			} else {
 				textureData = tempData;
 			}
 			this.alpha = true;
 		} else if(bufferType == BufferTypes.RGB_3Byte) {
 			TextureData tempData = TextureIO.newTextureData(url, GL.GL_RGB, GL.GL_RGB, false, null);
 			if(tempData.getPixelFormat() != GL.GL_RGB) {
 				textureData = convertToRGB(tempData);
 				tempData.flush();
 			} else {
 				textureData = tempData;
 			}
 			if(textureData.getPixelFormat() != GL.GL_RGB) throw new RuntimeException("Unexpected pixel format");
 			this.alpha = false;
 		} else {
 			throw new UnsupportedOperationException("Unsupported buffer type");
 		}
 		
 		this.width = textureData.getWidth();
 		this.height = textureData.getHeight();
 		this.byteBuffer = ((ByteBuffer)textureData.getBuffer());
 		this.bytes = byteBuffer.array();
 	}
 	public Image(int width, int height, ResourceManager resourceManager, BufferType bufferType) {
 		this.bufferType = bufferType;
 		this.width = width;
 		this.height = height;
 		this.buffered = true;
 		this.forceAlpha = false;
 		
 		if(bufferType == BufferTypes.RGBA_4Byte) {
 			bytes = new byte[width*height*4];
 			Arrays.fill(bytes, (byte)0);
 			byteBuffer = ByteBuffer.wrap(bytes);
 			
 			textureData = new TextureData(GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, false, true, byteBuffer, null);
			textureData.setRowLength(width);
 			if(textureData.getPixelFormat() != GL.GL_RGBA) throw new RuntimeException("Unexpected pixel format");
 			this.alpha = true;
 		} else if(bufferType == BufferTypes.RGB_3Byte) {
 			bytes = new byte[width*height*3];
 			Arrays.fill(bytes, (byte)0);
 			byteBuffer = ByteBuffer.wrap(bytes);
 			
 			textureData = new TextureData(GL.GL_RGB, width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, false, false, true, byteBuffer, null);
			textureData.setRowLength(width);
 			if(textureData.getPixelFormat() != GL.GL_RGB) throw new RuntimeException("Unexpected pixel format");
 			this.alpha = false;
 		} else {
 			throw new UnsupportedOperationException("Unsupported buffer type");
 		}
 		this.resourceManager = resourceManager;
 		this.url = null;
 	}
 
 	protected TextureData getTextureData() {
 		if(!isBuffered()) throw new RuntimeException("TextureData only available when buffered");
 		return textureData;
 	}
 	protected TextureData createTextureData() throws IOException {
 		if(isBuffered()) throw new RuntimeException("TextureData must only be created when not buffered");
 		if(textureData != null) {
 			TextureData temp = textureData;
 			this.textureData = null;
 			return temp;
 		} else {
 			if(url != null) {
 				TextureData data;
 				if(forceAlpha) {
 					TextureData tempData = TextureIO.newTextureData(url, GL.GL_RGBA, GL.GL_RGBA, false, null);
 					if(tempData.getPixelFormat() != GL.GL_RGBA) {
 						data = convertToRGBA(tempData);
 						tempData.flush();
 					} else {
 						data = tempData;
 					}
 				} else {
 					data = TextureIO.newTextureData(url, false, null);
 				}
 				return data;
 			} else {
 				int numBytes;
 				int pixelFormat;
 				if(alpha) {
 					numBytes = 4;
 					pixelFormat = GL.GL_RGBA;
 				} else {
 					numBytes = 3;
 					pixelFormat = GL.GL_RGB;
 				}
 
 				byte[] tempBytes = new byte[width*height*numBytes];
 				Arrays.fill(tempBytes, (byte)0);
 				ByteBuffer tempByteBuffer = ByteBuffer.wrap(tempBytes);
 				
 				TextureData data = new TextureData(pixelFormat, width, height, 0, pixelFormat, GL.GL_UNSIGNED_BYTE, false, false, false, tempByteBuffer, null);
				data.setRowLength(width);
 				if(data.getPixelFormat() != pixelFormat) throw new RuntimeException("Unexpected pixel format");
 				return data;
 			}
 		}
 	}
 	
 	private TextureData convertToRGB(TextureData data) {
 		if(data.getPixelFormat() != GL.GL_RGBA)	throw new RuntimeException("Unsupported pixel format: " + data.getPixelFormat());
 
 		// Manually create the correct buffer as TextureIO returns the wrong format.
 		byte[] tempbytes = new byte[data.getWidth()*data.getHeight()*3];
 		int sourceoffset = 0;
 		int destoffset = 0;
 		byte[] source = ((ByteBuffer)data.getBuffer()).array();
 		while(sourceoffset < source.length) {
 			tempbytes[destoffset++] = source[sourceoffset++]; // Red
 			tempbytes[destoffset++] = source[sourceoffset++]; // Green
 			tempbytes[destoffset++] = source[sourceoffset++]; // Blue
 			sourceoffset++; // ignore Alpha
 		}
 
 		return new TextureData(GL.GL_RGB, data.getWidth(), data.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, false, false, data.getMustFlipVertically(), ByteBuffer.wrap(tempbytes), null);
 	}
 	private TextureData convertToRGBA(TextureData data) {
 		if(data.getPixelFormat() != GL.GL_RGB) 	throw new RuntimeException("Unsupported pixel format: " + data.getPixelFormat());
 
 		// Manually create the correct buffer as TextureIO returns the wrong format.
 		byte[] tempbytes = new byte[data.getWidth()*data.getHeight()*4];
 		Arrays.fill(tempbytes, (byte)0);
 		int sourceoffset = 0;
 		int destoffset = 0;
 		byte[] source = ((ByteBuffer)data.getBuffer()).array();
 		while(sourceoffset < source.length) {
 			tempbytes[destoffset++] = source[sourceoffset++]; // Red
 			tempbytes[destoffset++] = source[sourceoffset++]; // Green
 			tempbytes[destoffset++] = source[sourceoffset++]; // Blue
 			tempbytes[destoffset++] = (byte)255; // Alpha
 		}
 
 		return new TextureData(GL.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, false, data.getMustFlipVertically(), ByteBuffer.wrap(tempbytes), null);
 	}
 	
 	public URL getURL() {
 		return url;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 	
 	public Object getBuffer() {
 		return bytes;
 	}
 	
 	public void updateCache() {
 		if(bytes == null) return;
 		resourceManager.updateCache(this);
 	}
 
 	public void updateCache(int xOffset, int yOffset, int width, int height) {
 		if(bytes == null) return;
 		resourceManager.updateCacheRegion(this, xOffset, yOffset, width, height);
 	}
 
 	public boolean isBuffered() {
 		return buffered;
 	}
 	public BufferType getBufferType() {
 		return bufferType;
 	}
 
 	public void updateBuffer() {
 		if(bytes == null) return;
 		resourceManager.updateBuffer(this);
 	}
 
 	public void updateBuffer(int xOffset, int yOffset, int width, int height) {
 		if(bytes == null) return;
 		resourceManager.updateBuffer(this, xOffset, yOffset, width, height);
 	}
 	
 	public String toString() {
 		StringBuffer desc = new StringBuffer();
 		if(url != null) {
 			desc.append(url.toString() + ": ");
 		}
 		desc.append(getWidth());
 		desc.append("x");
 		desc.append(getHeight());
 		if(isBuffered()) {
 			desc.append(" ");
 			desc.append(getBufferType().toString());
 		} else {
 			desc.append(" unbuffered");
 		}
 		return desc.toString();
 	}
 }
