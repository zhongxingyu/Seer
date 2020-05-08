 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 
 /**
  *
  * @author Owner
  */
 public class TestBox extends InteractiveObject{
 
     public TestBox(Vector3f position, Vector3f size) {
        super(new Geometry("Nyan", new Box(size.x, size.y, size.z)), position);
         
     }
     
     
     
 }
