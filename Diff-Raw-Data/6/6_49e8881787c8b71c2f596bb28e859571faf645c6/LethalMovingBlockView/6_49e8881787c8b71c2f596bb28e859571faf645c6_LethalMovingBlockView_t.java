 package edu.chl.codenameg.view.entity;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.entity.LethalMovingBlock;
 import edu.chl.codenameg.view.EntityView;
 
 /**
  * The moving lethal block's view that sets the graphic for the entity
  */
 public class LethalMovingBlockView implements EntityView{
 	private SpriteSheet spriteSheet;
 	private Animation 	spin;
 	
 	public LethalMovingBlockView(){
 		spriteSheet = null;
 		spin 		= null;
 		
 		try {
			spriteSheet = new SpriteSheet("res/lethal3.png", 33, 32,Color.white);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		spin = new Animation();
 		for (int i = 0; i < 5; i++) {
 			spin.addFrame(spriteSheet.getSprite(i, 0), 150);
 		}
 	}
 
 	@Override
 	public void render(Entity ent, Graphics g) {
 		if (ent instanceof LethalMovingBlock) {
 			LethalMovingBlock lmb = (LethalMovingBlock) ent;
 			spin.draw(lmb.getPosition().getX(), lmb.getPosition().getY());
 		}
 		
 	}
}
