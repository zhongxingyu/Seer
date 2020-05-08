 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.replica.core.graphics;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.CharBuffer;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 /**
  * @author matt
  * 
  * 
  *         A hack of Grid.java from Replica Island.
  * 
  */
 public class VertexGrid {
 
 	public VertexGrid(int width, int height) {
 
 		width_ = width;
 		height_ = height;
 		final int vertsAcross = width * 2;
 		final int vertsDown = height * 2;
 		final int size = vertsAcross * vertsDown;
 		final int quadCount = width * height;
 		final int indexCount = quadCount * 6;
 		indexCount_ = indexCount;
 		
 		vertexBuffer_ = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * size * 3)
 				.order(ByteOrder.nativeOrder()).asFloatBuffer();
 		texBuffer_ = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * size * 2)
 				.order(ByteOrder.nativeOrder()).asFloatBuffer();
 		indexBuffer_ = ByteBuffer.allocateDirect(BYTES_PER_CHAR * indexCount)
 				.order(ByteOrder.nativeOrder()).asCharBuffer();
 
 		for (int y = 0; y < height; y++) {
 			final int indexY = y * 2;
 			for (int x = 0; x < width; x++) {
 				final int indexX = x * 2;
 
 				char v0 = (char) ((indexY + 1) * vertsAcross + indexX);
 				char v1 = (char) (indexY * vertsAcross + indexX);
 				char v2 = (char) (indexY * vertsAcross + indexX + 1);
 				char v3 = (char) ((indexY + 1) * vertsAcross + indexX + 1);
 
 				char[] quadIndex = { v0, v1, v2, v0, v2, v3 };
 
 				indexBuffer_.put(quadIndex);
 			}
 		}
 		indexBuffer_.position(0);
 	}
 
 	public void set(int quadX, int quadY, float[][] positions, float[][] uvs) {
 		if (quadX < 0 || quadX >= width_) {
 			throw new IllegalArgumentException("quadX");
 		}
 		if (quadY < 0 || quadY >= height_) {
 			throw new IllegalArgumentException("quadY");
 		}
 		if (positions.length < 4) {
 			throw new IllegalArgumentException("positions");
 		}
 		if (uvs.length < 4) {
 			throw new IllegalArgumentException("quadY");
 		}
 
 		int i = quadX * 2;
 		int j = quadY * 2;
 
		setVertex(i, j + 1, positions[0][0], positions[0][1], positions[0][2],
				uvs[0][0], uvs[0][1]);
		setVertex(i, j, positions[1][0], positions[1][1], positions[1][2],
				uvs[1][0], uvs[1][1]);
		setVertex(i + 1, j, positions[2][0], positions[2][1], positions[2][2],
				uvs[2][0], uvs[2][1]);
		setVertex(i + 1, j + 1, positions[3][0], positions[3][1],
				positions[3][2], uvs[3][0], uvs[3][1]);
 	}
 
 	private void setVertex(int i, int j, float x, float y, float z, float u,
 			float v) {
 		if (i < 0 || i >= width_ * 2) {
 			throw new IllegalArgumentException("i");
 		}
 		if (j < 0 || j >= height_ * 2) {
 			throw new IllegalArgumentException("j");
 		}
 
 		final int index = width_ * 2 * j + i;
 
 		final int posIndex = index * 3;
 		final int texIndex = index * 2;
 
 		vertexBuffer_.put(posIndex, x);
 		vertexBuffer_.put(posIndex + 1, y);
 		vertexBuffer_.put(posIndex + 2, z);
 
 		texBuffer_.put(texIndex, u);
 		texBuffer_.put(texIndex + 1, v);
 
 	}
 
 	public static void BeginDrawingVertexGrid(GL10 gl, boolean useTexture) {
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		if (useTexture) {
 			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 			gl.glEnable(GL10.GL_TEXTURE_2D);
 		} else {
 			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 			gl.glDisable(GL10.GL_TEXTURE_2D);
 		}
 	}
 
 	public static void EndDrawingVertexGrid(GL10 gl) {
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 	}
 
 	public void draw(GL10 gl, boolean useTexture) {
 
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer_);
 		if (useTexture) {
 			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer_);
 		}
 		gl.glDrawElements(GL10.GL_TRIANGLES, indexCount_,
 				GL10.GL_UNSIGNED_SHORT, indexBuffer_);
 	}
 
 	public void beginDrawingStrips(GL10 gl, boolean useTexture) {
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer_);
 
 		if (useTexture) {
 			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer_);
 		}
 
 	}
 
 	// Assumes beginDrawingStrips() has been called before this.
 	public void drawStrip(GL10 gl, boolean useTexture, int startIndex,
 			int indexCount) {
 		int count = indexCount;
     	if (startIndex + indexCount >= indexCount_) {
     		count = indexCount_ - startIndex;
     	}
 		gl.glDrawElements(GL10.GL_TRIANGLES, count, GL10.GL_UNSIGNED_SHORT,
 				indexBuffer_.position(startIndex));
 	}
 
 	public float getWidth() {
 		return width_;
 	}
 
 	public float getHeight() {
 		return height_;
 	}
 
 	// variables
 	private int width_;
 	private int height_;
 	private int indexCount_;
 	private FloatBuffer vertexBuffer_;
 	private CharBuffer indexBuffer_;
 	private FloatBuffer texBuffer_;
 
 	private static final int BYTES_PER_FLOAT = 4;
 	private static final int BYTES_PER_CHAR = 2;
 }
