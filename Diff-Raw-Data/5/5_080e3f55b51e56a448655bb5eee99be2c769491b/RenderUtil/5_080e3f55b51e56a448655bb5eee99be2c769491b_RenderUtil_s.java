 package rendering;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.FloatBuffer;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLException;
 
 import manager.UberManager;
 import shader.Shader;
 import shader.ShaderScript;
 import util.GLUtil;
 import util.Log;
 import util.Util;
 
 import com.jogamp.common.nio.Buffers;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureIO;
 
 public class RenderUtil {
 	private static final float[] rectangle = new float[] { -0.5f, 0.5f, 0.0f,
 			0.5f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, };
 	private static int[] textureBuffer;
 
 	public static void drawRec(float[] bbox, GL2 gl) {
 		gl.glTexCoord2f(0, 0);
 		gl.glVertex3f(bbox[0], bbox[1], 0);
 		gl.glTexCoord2f(0, 1);
 		gl.glVertex3f(bbox[0], bbox[3], 0);
 		gl.glTexCoord2f(1, 1);
 		gl.glVertex3f(bbox[2], bbox[3], 0);
 		gl.glTexCoord2f(1, 0);
 		gl.glVertex3f(bbox[2], bbox[1], 0);
 	}
 
 	public static void drawSphere(float x, float y, float radius,
 			float[] color, GL2 gl) {
 
 		int i;
 		int sections = 20; // number of triangles to use to estimate a circle
 		// (a higher number yields a more perfect circle)
 		float twoPi = 2.0f * (float) Math.PI;
 
 		gl.glBegin(GL2.GL_TRIANGLE_FAN);
 		gl.glVertex2d(0.0, 0.0); // origin
 		if (color != null)
 			gl.glColor4f(color[0], color[1], color[2], 0);
 		for (i = 0; i <= sections; i++) {
 			// make $section number of circles
 			gl.glVertex2d(radius * Math.cos(i * twoPi / sections), radius
 					* Math.sin(i * twoPi / sections));
 		}
 		gl.glEnd();
 
 	}
 
 	public static void drawSphere(float pos[], float radius, float[] color,
 			GL2 gl, boolean b) {
 		if (color != null)
 			gl.glColor3fv(color, 0);
 		gl.glPushMatrix();
 		gl.glTranslatef(pos[0], pos[1], pos[2]);
 		if (b)
 			RenderUpdater.glut.glutWireSphere(radius, 5, 5);
 		else
 			RenderUpdater.glut.glutSolidSphere(radius, 10, 10);
 		gl.glPopMatrix();
 
 	}
 
 	public static void drawLinedBox(float[] b, GL2 gl) {
 		gl.glVertex3f(b[0], b[2], b[4]);
 		gl.glVertex3f(b[0], b[2], b[5]);
 
 		gl.glVertex3f(b[0], b[2], b[5]);
 		gl.glVertex3f(b[0], b[3], b[5]);
 
 		gl.glVertex3f(b[0], b[3], b[5]);
 		gl.glVertex3f(b[1], b[3], b[5]);
 
 		gl.glVertex3f(b[1], b[3], b[5]);
 		gl.glVertex3f(b[1], b[3], b[4]);
 
 		gl.glVertex3f(b[1], b[3], b[4]);
 		gl.glVertex3f(b[1], b[2], b[4]);
 
 		gl.glVertex3f(b[1], b[2], b[4]);
 		gl.glVertex3f(b[0], b[2], b[4]);
 
 	}
 
 	public static void gluPerspective(GL2 gl, double fovY, double aspect,
 			double zNear, double zFar) {
 		double fH = Math.tan(fovY / 360 * Math.PI) * zNear;
 		double fW = fH * aspect;
 		// glu.gluPerspective(fov_y, (float) width / height, ZNear, ZFar);
 		gl.glFrustum(-fW, fW, -fH, fH, zNear, zFar);
 	}
 
 	public static void init(GL2 gl) {
 		FloatBuffer verticeUVs = FloatBuffer.wrap(rectangle);
 		textureBuffer = new int[1];
 		int[] vertexArrayID = new int[1];
 		gl.glGenVertexArrays(1, vertexArrayID, 0);
 		gl.glBindVertexArray(vertexArrayID[0]);
 		gl.glGenBuffers(1, textureBuffer, 0);
 		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, textureBuffer[0]);
 		gl.glBufferData(GL2.GL_ARRAY_BUFFER, verticeUVs.capacity()
 				* Buffers.SIZEOF_FLOAT, verticeUVs, GL2.GL_STATIC_DRAW);
 	}
 
 	public static void drawTexture(GL2 gl, GLUtil glutil, float x, float y,
 			float z, float width, float height, int texID, float translateX,
 			float colorScale) {
 		ShaderScript fpsShader = UberManager.getShader(Shader.FPS);
 		if (fpsShader == null)
 			return;
 		gl.glEnable(GL2.GL_BLEND);
 		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
 
 		gl.glDisable(GL2.GL_CULL_FACE);
 		glutil.glPushMatrix();
 		{
 			glutil.glTranslatef(x, y, z);
 			glutil.scale(width, height, 1);
 			fpsShader.execute(gl);
 			{
 				ShaderScript.setUniformTexture(gl, "fpsTex", 0, texID);
 				ShaderScript.setUniformMatrix4(gl, "modelviewprojection",
 						glutil.getModelViewProjection(), true);
 				ShaderScript.setUniform(gl, "translateX", translateX);
 				ShaderScript.setUniform(gl, "colorScale", colorScale);
 
 				gl.glEnableVertexAttribArray(0);
 				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, textureBuffer[0]);
 				gl.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);
 				gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, rectangle.length / 3);
 			}
 			fpsShader.end(gl);
 		}
 		gl.glEnable(GL2.GL_CULL_FACE);
 		glutil.glPopMatrix();
 
 		gl.glDisable(GL2.GL_BLEND);
 	}
 
 	public static float[] merge(float[]... boxs) {
 		int length = 0;
 		for (float[] box : boxs)
 			length += box.length;
 		float verts[] = new float[length];
 		int count = 0;
 		for (float[] box : boxs) {
 			for (int i = 0; i < box.length; i++)
 				verts[count++] = box[i];
 		}
 		return verts;
 	}
 
 	public static float[] box(float x, float y, float z, float width,
 			float height, float depth) {
 		float[] verts = new float[6 * 2 * 3 * 3];
 		int count = 0;
 		// back t1
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z;
 		// back t2
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		// front t1
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		// front t2
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		// bottom t1
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		// bottom t2
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		// top t1
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		// top t2
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		// left t1
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		// left t2
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		verts[count++] = x;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		// right t1
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		// right t2
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z;
 		verts[count++] = x + width;
 		verts[count++] = y + height;
 		verts[count++] = z + depth;
 		verts[count++] = x + width;
 		verts[count++] = y;
 		verts[count++] = z + depth;
 		return verts;
 	}
 
 	public static void textureToFile(int textureID, int width, int height,
			File generateScreenshotFile) {
 		Texture text = TextureIO.newTexture(textureID, GL2.GL_TEXTURE_2D,
 				width, height, width, height, false);
 		try {
			TextureIO.write(text, Util.generateScreenshotFile());
 			Log.log(RenderUtil.class, "saving texture screenshot");
 		} catch (GLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
