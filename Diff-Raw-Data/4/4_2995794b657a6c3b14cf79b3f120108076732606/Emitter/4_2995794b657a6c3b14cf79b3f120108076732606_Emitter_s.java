 package cge.zeppelin.prototype.particule;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GL2GL3;
 import javax.media.opengl.GL3;
 
 import processing.core.PApplet;
 import cge.zeppelin.util.Helper;
 import de.bht.jvr.core.AttributeCloud;
 import de.bht.jvr.core.GroupNode;
 import de.bht.jvr.core.Shader;
 import de.bht.jvr.core.ShaderMaterial;
 import de.bht.jvr.core.ShaderProgram;
 import de.bht.jvr.core.ShapeNode;
 import de.bht.jvr.core.attributes.AttributeFloat;
 import de.bht.jvr.core.attributes.AttributeVector3;
 import de.bht.jvr.core.uniforms.UniformFloat;
 import de.bht.jvr.math.Vector3;
 
 public class Emitter {
 
     private GroupNode parent;
     private AttributeCloud cloud;
     private int count;
 
     private ArrayList<Vector3> position;
     private ArrayList<Float> age;
     private ArrayList<Float> startPosition;
     private ArrayList<Float> radius;
 
 	private PApplet noiseMaker = new PApplet();
 	private ShapeNode emitter;
 
     public Emitter(GroupNode p, int c) {
         parent = p;
         count = c;
 
         emitter = new ShapeNode("Emitter");
         
         cloud = new AttributeCloud(count, GL.GL_POINTS);
         ShaderProgram shader = null;
         try {
             Shader vert = new Shader(Helper.getInputStreamResource("/prototype/particule/sparks.vs"), GL3.GL_VERTEX_SHADER);
             Shader geom = new Shader(Helper.getInputStreamResource("/prototype/particule/sparks.gs"), GL3.GL_GEOMETRY_SHADER);
             Shader frag = new Shader(Helper.getInputStreamResource("/prototype/particule/sparks.fs"), GL3.GL_FRAGMENT_SHADER);
 
             shader = new ShaderProgram(vert, frag, geom);            
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         shader.setParameter(GL2GL3.GL_GEOMETRY_INPUT_TYPE_ARB, GL.GL_POINTS);
         shader.setParameter(GL2GL3.GL_GEOMETRY_OUTPUT_TYPE_ARB, GL2.GL_QUADS);
         shader.setParameter(GL2GL3.GL_GEOMETRY_VERTICES_OUT_ARB, 4);
 
         ShaderMaterial material = new ShaderMaterial("AMBIENT", shader);
         material.setMaterialClass("PARTICLE");
 
         emitter.setGeometry(cloud);
         emitter.setMaterial(material);
 
         parent.addChildNode(emitter);
 
         position = new ArrayList<Vector3>(count);
         for (int i = 0; i != count; i++)
             position.add(new Vector3(0, 0, 0));
 
         age = new ArrayList<Float>(count);
         for (int i = 0; i != count; i++)
             age.add(Float.POSITIVE_INFINITY);
 
         startPosition = new ArrayList<Float>(count);
         for (int i = 0; i != count; i++)
         	startPosition.add(PApplet.map(i, 0, count, 0, 360));
 
         radius = new ArrayList<Float>(count);
         for (int i = 0; i != count; i++)
        	radius.add(noiseMaker.random(1,2));
         
         cloud.setAttribute("partPosition", new AttributeVector3(position));
     }
 
     public void simulate(float elapsed) {
 
         for (int i = 0; i != count; i++) {
             age.set(i, age.get(i) + elapsed*50);
            
             if (age.get(i) > 360) {
             	age.set(i, (float) 0);
             }
 
             float n = (float) noiseMaker.noise(age.get(i));
             
         	float r = age.get(i);
             float x1 = (float) (1 + Math.cos(PApplet.radians(r + startPosition.get(i))) * (radius.get(i)+n/30));
             float y1 = (float) (1 + Math.sin(PApplet.radians(r + startPosition.get(i))) * (radius.get(i)+n/30));
             position.set(i, new Vector3(x1,y1,1));
         }
         // Set
         cloud.setAttribute("partPosition", new AttributeVector3(position));
         cloud.setAttribute("partRadius", new AttributeFloat(radius));
     }
 
     private static Random random = new Random();
 
     private float randomValue(float min, float max) {
         return min + random.nextFloat() * (max - min);
     }
 
     private Vector3 randomVector3(Vector3 min, Vector3 max) {
         return new Vector3(randomValue(min.x(), max.x()), randomValue(min.y(), max.y()),
                 randomValue(min.z(), max.z()));
     }
 
     public void refreshShader(){
     	ShaderProgram shader = null;
     	     
     	try {
             Shader vert = new Shader(Helper.getInputStreamResource("/prototype/particule/sparks.vs"), GL3.GL_VERTEX_SHADER);
             Shader geom = new Shader(Helper.getInputStreamResource("/prototype/particule/sparks.gs"), GL3.GL_GEOMETRY_SHADER);
             Shader frag = new Shader(Helper.getInputStreamResource("/prototype/particule/sparks.fs"), GL3.GL_FRAGMENT_SHADER);
 
             shader = new ShaderProgram(vert, frag, geom);            
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         shader.setParameter(GL2GL3.GL_GEOMETRY_INPUT_TYPE_ARB, GL.GL_POINTS);
         shader.setParameter(GL2GL3.GL_GEOMETRY_OUTPUT_TYPE_ARB, GL2.GL_QUADS);
         shader.setParameter(GL2GL3.GL_GEOMETRY_VERTICES_OUT_ARB, 4);
 
         ShaderMaterial material = new ShaderMaterial("AMBIENT", shader);
         material.setMaterialClass("PARTICLE");
 
         emitter.setMaterial(material);
 
     }
     
 }
