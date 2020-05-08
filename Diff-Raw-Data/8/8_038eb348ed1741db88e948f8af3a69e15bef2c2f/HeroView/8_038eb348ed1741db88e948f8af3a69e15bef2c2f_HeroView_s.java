 package se.chalmers.tda367.group15.game.views;
 
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import se.chalmers.tda367.group15.game.models.Hero;
 
 /**
  * Class representing the model of a hero.
  * 
  * @author ?????, Carl, tholene
  * 
  */
 public class HeroView implements View {
 
 	private final Hero hero;
 
 	private Animation heroMove;
 	private AffineTransformOp imgTransform;
 	
 	/**
 	 * Create a new hero view.
 	 * @param heroModel 
 	 */
 	public HeroView(final Hero hero) {
 		this.hero = hero;
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g) {
 		
		
 		float rotation = (float) hero.getDirection();
 		
 		if(heroMove != null) {
 			
 			/* We don't want to run the animation if we're not moving */
 			if(!hero.isMoving()) {
 				heroMove.setAutoUpdate(false);
 				heroMove.setCurrentFrame(1);
 			}else {
 				heroMove.setAutoUpdate(true);
 			}
			heroMove.getCurrentFrame().setRotation( rotation );
 			heroMove.draw(hero.getX(), hero.getY());
 			
 		}
 	}
 
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		
 		Image[] movement = {new Image("res/animation/hero/unarmed/1.png"), 
 							new Image("res/animation/hero/unarmed/2.png"), 
 							new Image("res/animation/hero/unarmed/3.png"),
 							new Image("res/animation/hero/unarmed/4.png"),
 							new Image("res/animation/hero/unarmed/5.png"),
 							new Image("res/animation/hero/unarmed/6.png"),
 							new Image("res/animation/hero/unarmed/7.png"),
 							new Image("res/animation/hero/unarmed/8.png"), 
 							new Image("res/animation/hero/unarmed/9.png"),
 							new Image("res/animation/hero/unarmed/10.png"),
 							new Image("res/animation/hero/unarmed/11.png"),
 							new Image("res/animation/hero/unarmed/12.png")
 							};
		heroMove = new Animation(movement, 80, true);
 	}
 
 }
