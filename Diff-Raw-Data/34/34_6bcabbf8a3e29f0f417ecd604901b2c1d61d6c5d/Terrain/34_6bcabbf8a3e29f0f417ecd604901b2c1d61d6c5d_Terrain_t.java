 package poomonkeys.common;
 
 import javax.media.opengl.GL2;
 
 public class Terrain extends Drawable
 {
 
	int NUM_POINTS = 256;
 	float segmentWidth;
 	float points[] = new float[NUM_POINTS];
 	float previousPoints[] = new float[NUM_POINTS];
 	float offsets[] = new float[NUM_POINTS];
 
 	public Terrain() 
 	{
 	}
 	
 	public void buildGeometry(float viewWidth, float viewHeight)
 	{
 		baseGeometry = new float[NUM_POINTS*3];
 		for(int i = 0; i < NUM_POINTS; i++)
 		{
 			baseGeometry[i*3] = segmentWidth*i;;
 			baseGeometry[i*3+1] = points[i];
 			baseGeometry[i*3+2] = 0;
 		}
 		drawMode = GL2.GL_LINE_STRIP;
 	}
 }
