 package solarsystem;
 
 import java.io.IOException;
 
 import javax.media.opengl.GLException;
 
 import glproject.Material;
 import glproject.Mesh;
 import glproject.RealToVector3fFunction;
 import glproject.ShaderProgram;
 import glproject.TextureLoader;
 import glproject.Vector3f;
 import glproject.Vector4f;
 
 public class Uranus extends Planet {
     public static final float SCALING_FACTOR = Sun.SCALE_FACTOR * 0.32f;
     public static final float ROTATIONAL_VELOCITY = 0.7f;
     public Uranus() {
     	/*
 	try {
 	    this.mesh = Mesh.loadMeshFromObjFile("solarplanet.obj");
 	} catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	this.mesh.reverseVertexWinding();
 	*/
 	
 	Material mat = new Material();
 	mat.ambient = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
 	mat.diffuse = new Vector4f(0.75f, 0.75f, 0.75f, 1.0f);
 	mat.specular = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
 	mat.shininess = 40.0f;
 	try {
 	    mat.texture = TextureLoader.loadTexture("uranus.jpg");
 	} catch (GLException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	
 	this.function = new RealToVector3fFunction() {
 	    public Vector3f eval(float t) {
 		
 		float a = 1400;
 		float b = 1400;
 		
 		t = t / 5.0f;
 		
 		float x = (float) (a * Math.cos(1.3*t));
 		float y = (float) (b * Math.sin(1.3*t));
 		return new Vector3f(x, 0.0f, y);
 	    }
 	};
 	
 	this.mesh.scaling = new Vector3f(Mercury.SCALING_FACTOR);
 	
 	this.rotationalVelocity = ROTATIONAL_VELOCITY;
 	
 	this.mesh.setMaterial(mat);
 	this.smaller.setMaterial(mat);
 	this.mesh.scaling = new Vector3f(SCALING_FACTOR);
 	this.smaller.scaling = new Vector3f(SCALING_FACTOR * Planet.PLANET_SCALE);
 	this.mesh.useFixedShader();
	this.mesh.setShaderProgram(ShaderProgram.getFromShaderLibrary("phong_textured"));
 	this.mesh.setColor(new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));
     }
 
     @Override
     public float getAttractionValue() {
     	return SCALING_FACTOR;
     }
 
     @Override
     public float getRadius() {
 	// TODO Auto-generated method stub
 	return 0;
     }
 
 }
