 package core;
 
 import java.awt.geom.Point2D;
 import java.io.ByteArrayInputStream;
 import java.util.HashSet;
 import java.util.Random;
 
 import states.Scene;
 
 public abstract class LevelGenerator {
 	
 	private static Random rand = new Random(System.currentTimeMillis()+19238);
 	
 	static Level generateLevel(Scene scene) {
 		StringBuilder sb = new StringBuilder();
 		
		double newspeed = (double)((int)(scene.getStaticScrollSpeed()*1000) + 1) / 1000;
		
		sb.append(newspeed);
		System.out.println("Next level with speed "+newspeed);
 		sb.append(System.getProperty("line.separator"));		
 		
 		// generator rv1
 		int obstacleCount = 30;
 		Random r = rand;
 		double x,y;
 		int w=10;
 
 		HashSet<Point2D.Double> set = new HashSet<>();
 		
 		for(int i = 0; i < obstacleCount; i++){
 			x = ((r.nextInt(5)) * 0.15) + (i % w) + 1  ;
 			int ra = r.nextInt(10);
 			y = ((ra <= 1 ? 0 : ra <= 3 ? 0.17 : 0.34 )) + 0.45;
 			Point2D.Double p = new Point2D.Double(x, y);
 			if(!set.contains(p)) {
 				switch((int)(rand.nextDouble()*2+0.1)){
 					case 0 : {
 						sb.append("block;"+x+";"+y+";");
 						break;
 					}
 					case 1 : {
 						sb.append("triangle;"+x+";"+y+";");
 						break;
 					}
 					default:{
 						sb.append("star;"+x+";"+y+";");
 						break;
 					}
 				}
 				sb.append(System.getProperty("line.separator"));
 				set.add(p);
 			}
 		}
 		// /generator rv1
 		
 		return new Level(scene, new ByteArrayInputStream(sb.toString().getBytes()));
 	}
 }
