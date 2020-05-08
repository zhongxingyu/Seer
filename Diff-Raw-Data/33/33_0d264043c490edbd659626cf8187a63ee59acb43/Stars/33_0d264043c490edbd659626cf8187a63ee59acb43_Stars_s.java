 package engine.stars;
 
 import java.util.Random;
 
 import javax.vecmath.Vector3f;
 
 import engine.Engine;
 import engine.entity.Entity;
 
 public class Stars {
 	public Stars(Engine engine, int rows, int columns, int space_between_levels, int num_levels, int starting_distance) {
 		int count=0;
 		for(int i=0;i<num_levels;i++) {
 			for(int j=0;j<rows;j++) {
 				for(int k=0;k<columns;k++) {
 					Random rand = new Random();
 					if(rand.nextInt(50000)==10){
 						Entity star = engine.addEntity("star"+count++,1.0f,true,"box","default");
 						star.setPosition(new Vector3f(j-starting_distance,k-starting_distance,i*-space_between_levels-starting_distance));
 						star.applyImpulse(new Vector3f(0,-100,0), new Vector3f(0,0,0));
 					}
 				}
 			}
 		}
 	}
 }
