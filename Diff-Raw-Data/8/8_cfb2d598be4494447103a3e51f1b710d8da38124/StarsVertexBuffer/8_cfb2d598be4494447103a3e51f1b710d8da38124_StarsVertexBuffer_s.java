 package com.secondhand.opengl;
 
 import java.util.List;
 import java.util.Random;
 
 import org.anddev.andengine.opengl.util.FastFloatBuffer;
 import org.anddev.andengine.opengl.vertex.VertexBuffer;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class StarsVertexBuffer extends VertexBuffer {
 	
 		
 	// ===========================================================
 	// Constants
 	// ===========================================================
 		
 	// ===========================================================
 	// Fields
 	// ===========================================================
	
	private List<Vector2> mVertices;
 
 	private int mStars;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	
	
	public List<Vector2> getVertices() {
		return this.mVertices;
	}
	
 
 	public StarsVertexBuffer(final int stars, final int pDrawType, final boolean pManaged) {
 		super(stars * 2, pDrawType, pManaged);
 		this.mStars = stars;
 	}
 
 	
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 	
 	public int getStars() {
 		return this.mStars;
 	}
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	
 	public synchronized void update(final float width, final float height) {
 		
 	    final int[] vertices = this.mBufferData;
 	    
 	    Random rng = new Random();
 	    int count = 0;
 	    for (int i = 0; i < this.getStars(); ++i) {
 	    	float x = (float) (rng.nextInt(Integer.MAX_VALUE) % (int)width);
 	    	float y = (float) (rng.nextInt(Integer.MAX_VALUE) % (int)height);
 	    	
 	    	vertices[count++] = Float.floatToRawIntBits(x);
 	    	vertices[count++] = Float.floatToRawIntBits(y);
 	    }
 
 	    final FastFloatBuffer buffer = this.getFloatBuffer();
 	    buffer.position(0);
 	    buffer.put(vertices);
 	    buffer.position(0);
 
 		super.setHardwareBufferNeedsUpdate();
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 }
