 package entities;
 
 import java.io.IOException;
 
 import javafx.scene.image.Image;
 import javafx.scene.paint.ImagePattern;
 import javafx.scene.shape.Polygon;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 
 import utils.PathUtil;
 
 public class StaticPathEntity extends PathEntity {
 
 	public StaticPathEntity(Vec2[] verts) {
 		super(verts);
		Image clouds = new Image("sprites/DreamTexture.jpg");
 		((Polygon) node).setFill(new ImagePattern(clouds));
 
 	}
 
 	public StaticPathEntity() {
 		super();
 	}
 
 	public StaticPathEntity(Vec2[] lp, Vec2[] local, float x, float y,
 			float width, float height) {
 		super(lp, local, x, y, width, height);
		Image clouds = new Image("sprites/DreamTexture.jpg");
 		((Polygon) node).setFill(new ImagePattern(clouds));
 	}
 
 	@Override
 	protected BodyDef createBD() {
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.position.set(xPos, yPos);
 		bodyDef.type = BodyType.STATIC;
 		bodyDef.userData = this.getClass();
 		return bodyDef;
 	}
 
 	@Override
 	protected FixtureDef createFD() {
 		FixtureDef fix = new FixtureDef();
 		fix.shape = ps;
 		fix.density = 1.0f;
 		fix.friction = 10.3f;
 		return fix;
 	}
 
 	public static StaticPathEntity parse(String[] frags) {
 		return new StaticPathEntity(PathUtil.parseVec2(frags[3]),
 				PathUtil.parseVec2(frags[4]), Float.parseFloat(frags[1]),
 				Float.parseFloat(frags[2]), Float.parseFloat(frags[5]),
 				Float.parseFloat(frags[6]));
 	}
 
 	public String toString() {
 		return super.toString();
 	}
 }
