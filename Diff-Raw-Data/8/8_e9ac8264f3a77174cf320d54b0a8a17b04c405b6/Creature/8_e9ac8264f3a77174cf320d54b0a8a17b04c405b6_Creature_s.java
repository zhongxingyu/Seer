 package Game;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 
 import javafx.animation.Animation;
 import javafx.geometry.Rectangle2D;
 import javafx.scene.Node;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
 import javafx.util.Duration;
 
 public class Creature extends Entity {
 	public boolean status;
 	public Image image;
 	public int col;
 	public int count;
 	public int offsetX;
 	public int offsetY;
 	public int pWidth;
 	public int pHeight;
 
 	public Creature(float posX, float posY) {
 		//Image Credits: http://www.bit-101.com/blog/wp-content/uploads/2011/03/spritesheet.png
 		image = new Image("file:spritesheet.png");
 		xPos = posX;
 		yPos = posY;
 		col = 8;
 		count = 60;
 		offsetX = 0;
 		offsetY = 0;
 		pWidth = 60;
 		pHeight = 60;
 		width = Utility.toWidth(pWidth);
 		height = Utility.toHeight(pHeight);
 	}
 
 	public Node create() {
 		ImageView imageView = new ImageView(image);
 		imageView.setViewport(new Rectangle2D(offsetX, offsetY, pWidth, pHeight));
 		Animation animation = new SpriteAnimation(imageView,
 				Duration.millis(1000), count, col, offsetX, offsetY, pWidth,
 				pHeight);
 		animation.setCycleCount(Animation.INDEFINITE);
 		animation.play();
 				
 		imageView.setLayoutX(Utility.toPixelPosX(xPos) - Utility.toPixelWidth(width)/2);
 		imageView.setLayoutY(Utility.toPixelPosY(yPos) -  Utility.toPixelWidth(height)/2);
 		
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set(xPos, yPos);
 		bd.fixedRotation = true;
 		
 		PolygonShape ps = new PolygonShape();
 		ps.setAsBox(width/2, height/2);
 		
 		FixtureDef fd = new FixtureDef();
 		fd.shape = ps;
 		fd.density = 0.1f;
 		fd.friction = 0.3f;
 		fd.restitution = 0f;
 		
 		Body body = world.world.createBody(bd);
 		body.createFixture(fd);
 		imageView.setUserData(body);
 		return imageView;
 
 		
 	}
 }
