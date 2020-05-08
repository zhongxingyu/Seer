 package entities;
 
 import javafx.scene.image.Image;
 import javafx.scene.paint.Color;
 import javafx.scene.paint.ImagePattern;
 import javafx.scene.shape.Polygon;
 
 import org.jbox2d.common.Vec2;
 
 import utils.PathUtil;
 
 public class DeadlyDynamicPathEntity extends DynamicPathEntity{
 
 	public DeadlyDynamicPathEntity(Vec2[] verts) {
 		super(verts);
 		// TODO Auto-generated constructor stub
		Image clouds = new Image("sprites/DeadlyTexture.jpg");
 		((Polygon)node).setFill(new ImagePattern(clouds));
 	}
 	public DeadlyDynamicPathEntity(Vec2[] lp, Vec2[] local, float x, float y,
 			float width, float height) {
 		super(lp, local, x, y, width, height);
		Image clouds = new Image("sprites/DeadlyTexture.jpg");
 		((Polygon) node).setFill(new ImagePattern(clouds));
 	}
 	public static DeadlyDynamicPathEntity parse(String[] frags) {
 		return new DeadlyDynamicPathEntity(PathUtil.parseVec2(frags[3]),
 				PathUtil.parseVec2(frags[4]), Float.parseFloat(frags[1]),
 				Float.parseFloat(frags[2]), Float.parseFloat(frags[5]),
 				Float.parseFloat(frags[6]));
 	}
 
 
 }
