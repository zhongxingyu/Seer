 package com.secondhand.opengl;
 
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.opengles.GL11;
 
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.opengl.util.GLHelper;
 import org.anddev.andengine.opengl.vertex.VertexBuffer;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 
 
 /**
  * A circle class used for drawing circles. 
  * @author erkastina
  *
  */
 public class Polygon extends Shape {
 	
 	
 	protected final PolygonVertexBuffer mPolygonVertexBuffer;
 	
 	protected List<Vector2> mPolygon;
 	private Body mBody;
 	private PolygonShape mPolygonShape;
 	private final float mMaxLength;
 	
 	/**
 	 * Culling enabled by default. 
 	 */
 	public Polygon(final float pX, final float pY, final List<Vector2> polygon) {
 		this(pX, pY, polygon, true);
 	}
 	
 
 	/**
 	 * 
 	 * @param pX
 	 * @param pY
 	 * @param polygon the points that define the polygon. e.g., for a square these points will be its four corners.
 	 * @param cullingEnabled
 	 */
 	public Polygon(final float pX, final float pY, final List<Vector2> polygon, boolean cullingEnabled) {
 
		super(pX/*+ radius*/, pY/* + radius*/);
 		
 		this.mPolygon = polygon;
 		
 		super.setCullingEnabled(cullingEnabled);
 		
 		this.mPolygonVertexBuffer = new PolygonVertexBuffer(this.mPolygon.size(), GL11.GL_STATIC_DRAW, true);
 		this.updateVertexBuffer();
 		
 		// used in the culling logic
 		this.mMaxLength = computeMaxLength(polygon);
 	}
 	
 	private final float computeMaxLength(final List<Vector2> polygon) {
 		// compute the maximum length between 2 points in the polygon.
 		float maxLength = 0;
 		
 		for(int i = 0; i < polygon.size(); ++i) {
 			for(int j = 0; j < polygon.size(); ++j) {
 				if(j != i) {
 					Vector2 p1 = polygon.get(i);
 					Vector2 p2 = polygon.get(j);
 					
 					final float length = (float)Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
 					if(length > maxLength) {
 						maxLength = length;
 					}
 				}
 			}
 		}
 		
 		return maxLength;
 	}
 	
 	
 	
 	public List<Vector2> getPolygon() {
 		return this.mPolygon;
 	}
 	
 	
 	@Override
 	public float getWidth() {
 		return 0;
 		//throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public float getHeight() {
 	return 0;
 		//	throw new UnsupportedOperationException();	
 	}
 	
 	@Override
 	public float getBaseWidth() {
 		return getWidth();
 	}
 
 	
 	
 	@Override
 	public float getBaseHeight() {
 		return getBaseWidth();
 	}
 
 	@Override
 	public boolean collidesWith(IShape pOtherShape) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean contains(float pX, float pY) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	protected void onUpdateVertexBuffer() {
 		this.mPolygonVertexBuffer.update(this.mPolygon);
 	}
 	
 	@Override
 	protected VertexBuffer getVertexBuffer() {
 		return this.mPolygonVertexBuffer;
 	}
 
 	public List<Vector2> getVertices() {
 		return this.mPolygonVertexBuffer.getVertices();
 	}
 
 	@Override
 	protected void onInitDraw(final GL10 pGL) {
 		super.onInitDraw(pGL);
 		GLHelper.disableTextures(pGL);
 		GLHelper.disableTexCoordArray(pGL);
 	}
 
 	
 	@Override
 	protected void drawVertices(GL10 pGL, Camera pCamera) {
 		pGL.glDrawArrays(GL10.GL_TRIANGLES, 0, this.mPolygonVertexBuffer.getVertices().size());
 	}	
 
 	
 	private static boolean isPointInCamera(Camera camera, Vector2 point) {
 		return camera.getMinX() < point.x && camera.getMaxX() > point.x &&
 				camera.getMinY() < point.y && camera.getMaxY() > point.y;
 	}
 	
 	public void setBody(Body body) {
 		this.mBody = body;
 		
 		final Fixture fixture = body.getFixtureList().get(0);
 		this.mPolygonShape	 = (PolygonShape)fixture.getShape();
 	}
 	
 	@Override
 	protected boolean isCulled(Camera pCamera) {
 		return false;
 		// TODO: get culling to work.	
 		/*
 		for(int i = 0; i < this.mPolygonShape.getVertexCount(); ++i) {
 			Vector2 v = new Vector2(0,0);		
 			this.mPolygonShape.getVertex(i, v);
 			
 			Vector2 translatedPoint = new Vector2(
 					PIXEL_TO_METER_RATIO_DEFAULT * v.x + this.getX(),
 					PIXEL_TO_METER_RATIO_DEFAULT * v.y + this.getY());
 			
 			if(isPointInCamera(pCamera, translatedPoint))
 				return false;
 		}
 		return true;
 		*/
 		/*
 		final float x = this.mX;
 		final float y = this.mY;
 		boolean culled =  x - this.mMaxLength > pCamera.getMaxX()
 			|| y - this.mMaxLength > pCamera.getMaxY()
 			|| x + this.mMaxLength< pCamera.getMinX()
 			|| y + this.mMaxLength < pCamera.getMinY();
 	
 			return culled;
 		
 			// TOOD: get this more precise culling to work:
 	*/
 		/*
 		for(int i = 0; i < this.mPolygonShape.getVertexCount(); ++i) {
 			Vector2 v = new Vector2(0,0);		
 			this.mPolygonShape.getVertex(i, v);
 			
 			Vector2 translatedPoint = new Vector2(
 					PIXEL_TO_METER_RATIO_DEFAULT * v.x + this.getX(),
 					PIXEL_TO_METER_RATIO_DEFAULT * v.y + this.getY());
 	if(isPointInCamera(pCamera, translatedPoint))
 				return false;
 		}
 		
 		
 		MyDebug.d("culling polygon!");
 		
 		return true;
 		*/
 	}
 
 }
