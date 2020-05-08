 package com.vitaltech.bioink;
 
 import rajawali.primitives.Sphere;
 import android.graphics.Color;
 import android.opengl.GLES20;
import android.util.Log;
 
 public class Blob extends Sphere {
 	// current data
 	private float _x,_y,_z; // position
 	private int _color; // color
 	private float _energy; // amount of energy on the surface of the blob
 	private float _dist; // distance from the center of the scene
 	
 	// target data to animate towards
 	public float x,y,z;
 	public int color;
 	public float radius;
 	public float energy;
 	public float dist;
 	
 	// smooth out the animations with exponential decay towards the target
 	private float DECAY = 50.0f; // 1 = instantaneous, >1 for slower decay
 	
 	private BlobShader shader;
 	private int frameCount = 0;
 	
 	public Blob(){
 		super(2,60,60);
 		_x = _y = _z = x = y = z = 0;
 		_color = color = Color.BLACK;
 		_energy = energy = 0;
 		_dist = dist = 0;
 		
 		shader = new BlobShader();
 		shader.setUseColor(true);
 		setMaterial(shader);
 		setDrawingMode(GLES20.GL_LINES);
 	}
 	
 	public void draw(){
 		animate();
 		
 		setColor(_color);
 		shader.setTime((float)frameCount++);
 		shader.setStrength(_energy/100);
 	}
 	
 	public void adjustColor(float temp){
 		final float MIN_TEMP = 96.0f;
 		final float MAX_TEMP = 102.0f;
 		
 		float ratio = Math.max(0,Math.min((temp - MIN_TEMP) / (MAX_TEMP - MIN_TEMP),1.0f)); //clamp between 0 and 1
 		color = interpolateColor(Color.BLUE,Color.RED,ratio);	
 	}
 	
 	// update current data by moving towards the target
 	private void animate(){
 		_x = _x + (x-_x)/DECAY;
 		_y = _y + (y-_y)/DECAY;
 		_z = _z + (z-_z)/DECAY;
 		_energy = _energy + (energy-_energy)/DECAY;
 		_dist = _dist + (dist-_dist)/DECAY;
 		_color = interpolateColor(_color,color,1/DECAY);
 	}
 	
 	private int interpolateColor(int a, int b, float proportion) {
 	    float[] hsva = new float[3];
 	    float[] hsvb = new float[3];
 	    Color.colorToHSV(a, hsva);
 	    Color.colorToHSV(b, hsvb);
 	    for (int i = 0; i < 3; i++) {
	    	if(i==0)
	    		hsvb[i] = interpolateCircular(hsva[i],hsvb[i],proportion);
	    	else
	    		hsvb[i] = interpolateLinear(hsva[i],hsvb[i],proportion);
 	    }
 	    return Color.HSVToColor(hsvb);
 	}
 	
	// interpolate linearly (usually 0-1)
	private float interpolateLinear(float a, float b, float proportion){
		return (a + (b-a)*proportion);
	}
	
	// interpolate from 0-360 around a circle
	private float interpolateCircular(float a, float b, float proportion) {
		float dist = Math.min(Math.abs(b-a),Math.abs(360-b+a)); // shortest distance between these colors
 		if(Math.abs(b-a) == dist)
 			return (a + (b-a)*proportion);
 		else {
			// it wraps around the circle, so interpolate the other way
			float value = a + (360-b+a)*proportion;
			
			// perform the wrap
			if (value < 0) value += 360;
			if (value > 360) value -= 360;
			Log.d("visualization","a:"+a+", b:"+b+", prop:"+proportion+", result: "+value);
			return value;
 		}
 			
 		//return (a + ((b - a) * proportion));
 	}
 }
