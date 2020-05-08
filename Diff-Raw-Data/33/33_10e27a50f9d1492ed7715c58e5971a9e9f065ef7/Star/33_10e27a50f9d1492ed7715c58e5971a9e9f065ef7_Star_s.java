 package spaceshooters.entities.fx;
 
 import java.util.Random;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import spaceshooters.gfx.ImageRegister;
 import spaceshooters.util.config.Configuration;
 import spaceshooters.util.config.Options;
 
 /**
  * Basic effect - star.
  * 
  * @author Mat
  * 
  */
 public class Star extends Effect {
 	
 	private static boolean oldGfx = Configuration.getConfiguration().getBoolean(Options.OLD_GRAPHICS);
	private String[] textures = { "star.png", "star1.png", "star2.png", "star3.png" };
 	
 	public Star(int x, int y) throws SlickException {
 		super(x, y, EffectTypes.STAR);
 		Random random = new Random();
 		int i = random.nextInt(3);
 		velocity = i == 2 ? 0.2F : i == 1 ? 0.35F : 0.3F;
 		if (!oldGfx) {
			texture = ImageRegister.getEffect(textures[random.nextInt(textures.length - 1)]);
 		}
 	}
 	
 	@Override
 	public void update(GameContainer container, int delta) {
 		y += velocity * delta;
 		if (y >= container.getHeight()) {
 			this.setDead();
 		}
 	}
 	
 	@Override
 	public void render(Graphics g) {
 		texture.draw(x, y);
 	}
 	
 	@Override
 	public String getTextureFile() {
 		return "star.png";
 	}
 }
