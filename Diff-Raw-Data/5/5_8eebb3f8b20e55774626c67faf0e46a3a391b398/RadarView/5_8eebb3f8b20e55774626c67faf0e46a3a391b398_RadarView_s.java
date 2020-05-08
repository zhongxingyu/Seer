 package me.kevinwells.darxen;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import me.kevinwells.darxen.data.DataFile;
 import me.kevinwells.darxen.data.Description.OpMode;
 import me.kevinwells.darxen.data.RadialDataPacket;
 import me.kevinwells.darxen.data.RadialPacket;
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLU;
 import android.opengl.Matrix;
 import android.view.MotionEvent;
 
 public class RadarView extends GLSurfaceView implements GLSurfaceView.Renderer, GestureSurface {
 
 	private DataFile mData;
 	private LatLon mPos;
 	private FloatBuffer mPosBuf;
 	
 	private GestureRecognizer mRecognizer = new GestureRecognizer(this);
 
 	private float[] mTransform = new float[32];
 	
 	private FloatBuffer[] mRadialBuffers = new FloatBuffer[16];
 	private int[] mRadialSize = new int[16];
 
 	private static Color[] REFLECTIVITY_PALETTE = new Color[] {
 			new Color(50.0f / 255.0f, 50.0f / 255.0f, 50.0f / 255.0f),
 			new Color(16.0f / 255.0f, 16.0f / 255.0f, 16.0f / 255.0f),
 			new Color(33.0f / 255.0f, 33.0f / 255.0f, 33.0f / 255.0f),
 			new Color(40.0f / 255.0f, 126.0f / 255.0f, 40.0f / 255.0f),
 			new Color(60.0f / 255.0f, 160.0f / 255.0f, 20.0f / 255.0f),
 			new Color(120.0f / 255.0f, 220.0f / 255.0f, 20.0f / 255.0f),
 			new Color(250.0f / 255.0f, 250.0f / 255.0f, 20.0f / 255.0f),
 			new Color(250.0f / 255.0f, 204.0f / 255.0f, 20.0f / 255.0f),
 			new Color(250.0f / 255.0f, 153.0f / 255.0f, 20.0f / 255.0f),
 			new Color(250.0f / 255.0f, 79.0f / 255.0f, 20.0f / 255.0f),
 			new Color(250.0f / 255.0f, 0.0f / 255.0f, 20.0f / 255.0f),
 			new Color(220.0f / 255.0f, 30.0f / 255.0f, 70.0f / 255.0f),
 			new Color(200.0f / 255.0f, 30.0f / 255.0f, 100.0f / 255.0f),
 			new Color(170.0f / 255.0f, 30.0f / 255.0f, 150.0f / 255.0f),
 			new Color(255.0f / 255.0f, 0.0f / 255.0f, 156.0f / 255.0f),
 			new Color(255.0f / 255.0f, 255.0f / 255.0f, 255.0f / 255.0f) };
 
 	private static Color[] CLEANAIR_PALETTE = new Color[] {
 			new Color(50.0f / 255.0f, 50.0f / 255.0f, 50.0f / 255.0f),
 			new Color(30.0f / 255.0f, 30.0f / 255.0f, 30.0f / 255.0f),
 			new Color(40.0f / 255.0f, 40.0f / 255.0f, 40.0f / 255.0f),
 			new Color(50.0f / 255.0f, 50.0f / 255.0f, 50.0f / 255.0f),
 			new Color(60.0f / 255.0f, 160.0f / 255.0f, 20.0f / 255.0f),
 			new Color(70.0f / 255.0f, 70.0f / 255.0f, 70.0f / 255.0f),
 			new Color(80.0f / 255.0f, 80.0f / 255.0f, 80.0f / 255.0f),
 			new Color(90.0f / 255.0f, 90.0f / 255.0f, 90.0f / 255.0f),
 			new Color(100.0f / 255.0f, 100.0f / 255.0f, 100.0f / 255.0f),
 			new Color(20.0f / 255.0f, 70.0f / 255.0f, 20.0f / 255.0f),
 			new Color(30.0f / 255.0f, 120.0f / 255.0f, 20.0f / 255.0f),
 			new Color(30.0f / 255.0f, 155.0f / 255.0f, 20.0f / 255.0f),
 			new Color(60.0f / 255.0f, 175.0f / 255.0f, 20.0f / 255.0f),
 			new Color(80.0f / 255.0f, 200.0f / 255.0f, 20.0f / 255.0f),
 			new Color(110.0f / 255.0f, 210.0f / 255.0f, 20.0f / 255.0f),
 			new Color(240.0f / 255.0f, 240.0f / 255.0f, 20.0f / 255.0f) };
 	
 	public RadarView(Context context) {
 		super(context);
 		//setEGLContextClientVersion(2);
 		setRenderer(this);
 		Matrix.setIdentityM(mTransform, 0);
 	}
 
 	public void setData(DataFile file) {
 		mData = file;
 		
 		Matrix.setIdentityM(mTransform, 0);
 		RadialDataPacket packet = (RadialDataPacket)mData.description.symbologyBlock.packets[0];
 		scale(1.0f/packet.rangeBinCount);
 	}
 	
 	@Override
 	public void scale(float factor) {
 		Matrix.setIdentityM(mTransform, 16);
 		Matrix.scaleM(mTransform, 16, factor, factor, 1.0f);
 		Matrix.multiplyMM(mTransform, 0, mTransform, 16, mTransform, 0);
 	}
 	
 	@Override
 	public void translate(float dx, float dy) {
 		Matrix.setIdentityM(mTransform, 16);
 		Matrix.translateM(mTransform, 16, dx, dy, 0.0f);
 		Matrix.multiplyMM(mTransform, 0, mTransform, 16, mTransform, 0);
 	}
 	
 	public void setLocation(LatLon pos) {
 		mPos = pos;
 		
 		if (mPos != null) {
 			ByteBuffer vbb = ByteBuffer.allocateDirect(2 * 4);
 			vbb.order(ByteOrder.nativeOrder());
 			mPosBuf = vbb.asFloatBuffer();
 	
 			Point2D p = mPos.project(new LatLon(mData.description.lat, mData.description.lon));
 			mPosBuf.put((float)p.x);
 			mPosBuf.put((float)p.y);
 			mPosBuf.position(0);
 		} else {
 			mPosBuf = null;
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent e) {
 		return mRecognizer.onTouchEvent(e);
 	}
 
 	@Override
 	public void onDrawFrame(GL10 gl) {
 		if (mData == null)
 			return;
 		
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();
 		gl.glMultMatrixf(mTransform, 0);
 		
 		RadialDataPacket packet = (RadialDataPacket)mData.description.symbologyBlock.packets[0];
 
 		Color[] palette = CLEANAIR_PALETTE;
 		if (mData.description.opmode == OpMode.PRECIPITATION)
 			palette = REFLECTIVITY_PALETTE;
 		renderRadialData(gl, packet, palette);
 		
 		if (mPosBuf != null) {
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mPosBuf);
 			gl.glPointSize(5.0f);
 			gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
 		}
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		gl.glViewport(0, 0, width, height);
 		
 		gl.glMatrixMode(GL10.GL_PROJECTION);
 		gl.glLoadIdentity();
 		if (height > width) {
 			float aspect = (float)height / width;
 			GLU.gluOrtho2D(gl, -1, 1, -aspect, aspect);
 		} else {
 			float aspect = (float)width/ height;
 			GLU.gluOrtho2D(gl, -aspect, aspect, -1, 1);
 		}
 		
 		gl.glMatrixMode(GL10.GL_MODELVIEW);
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 	}
 	
 	private void renderRadialData(GL10 gl, RadialDataPacket packet, Color[] palette) {
 		float kmPerRangeBin = 1.0f;
 		
 		for (int i = 1; i < 16; i++) {
 			if (mRadialBuffers[i] == null) {
 				TriangleBuffer buffer = new TriangleBuffer();
 				for (int az = 0; az < packet.radials.length; az++) {
 					RadialPacket radial = packet.radials[az];
 					
 					float start = 90.0f - (radial.start + radial.delta);
 					float end = start + radial.delta;
 					float cosx1 = (float)Math.cos(Math.toRadians(start));
 					float siny1 = (float)Math.sin(Math.toRadians(start));
 					float cosx2 = (float)Math.cos(Math.toRadians(end));
 					float siny2 = (float)Math.sin(Math.toRadians(end));
 					
 					int startRange = 0;
 					for (int range = 0; range < radial.codes.length; range++) {
 						int color = radial.codes[range];
 						if (startRange == 0 && color == i)
							startRange = range+1;
 						
 						if ((startRange != 0 && color < i) ||
 								(startRange != 0 && (range == packet.rangeBinCount-1))) {
							if (range == packet.rangeBinCount-1)
 								range++;
 							Vertex v1 = new Vertex(
 									(startRange-1) * kmPerRangeBin * cosx1,
 									(startRange-1) * kmPerRangeBin * siny1);
 							Vertex v2 = new Vertex(
 									(range-1) * kmPerRangeBin * cosx1,
 									(range-1) * kmPerRangeBin * siny1);
 							Vertex v3 = new Vertex(
 									(range-1) * kmPerRangeBin * cosx2,
 									(range-1) * kmPerRangeBin * siny2);
 							Vertex v4 = new Vertex(
 									(startRange-1) * kmPerRangeBin * cosx2,
 									(startRange-1) * kmPerRangeBin * siny2);
 							
 							buffer.addTriangle(v1, v2, v3);
 							buffer.addTriangle(v3, v4, v1);
 							startRange = 0;
 						}
 					}
 				}
 				mRadialBuffers[i] = buffer.getBuffer();
 				mRadialSize[i] = buffer.size();
 			}
 			
 			Color c = palette[i];
 			gl.glColor4f(c.r, c.g, c.b, 1.0f);
 			FloatBuffer buf = mRadialBuffers[i];
 			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, buf);
 			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, mRadialSize[i]);
 		}
 	}
 	
 	public class Vertex {
 		public float x;
 		public float y;
 		public Vertex(float x, float y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 	
 	public class TriangleBuffer {
 		
 		private class Triangle {
 			public Vertex p1;
 			public Vertex p2;
 			public Vertex p3;
 			public Triangle(Vertex p1, Vertex p2, Vertex p3) {
 				this.p1 = p1;
 				this.p2 = p2;
 				this.p3 = p3;
 			}
 		}
 		
 		private ArrayList<Triangle> buffer;
 		
 		public TriangleBuffer() {
 			buffer = new ArrayList<Triangle>();
 		}
 		
 		public int size() {
 			return buffer.size() * 3;
 		}
 
 		public void addTriangle(Vertex p1, Vertex p2, Vertex p3) {
 			buffer.add(new Triangle(p1, p2, p3));
 		}
 		
 		public FloatBuffer getBuffer() {
 			ByteBuffer vbb = ByteBuffer.allocateDirect(buffer.size() * 2 * 3 * 4);
 			vbb.order(ByteOrder.nativeOrder());
 			FloatBuffer buf = vbb.asFloatBuffer();
 			
 			for (Triangle t : buffer) {
 				buf.put(t.p1.x);
 				buf.put(t.p1.y);
 				buf.put(t.p2.x);
 				buf.put(t.p2.y);
 				buf.put(t.p3.x);
 				buf.put(t.p3.y);
 			}
 			buf.position(0);
 			return buf;
 		}
 	}
 	
 }
