 package components;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class HealthBarComponent extends RenderComponent{
 
 	private Image damage, health, bar;
 
 	public HealthBarComponent(String id, Image damage, Image health, Image bar) {
 		super(id);
 		this.damage = damage;
 		this.health = health;
 		this.bar = bar;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics gr) {
 		Vector2f pos = owner.getPosition();
 		float scale = owner.getScale();
 
		damage.draw(pos.x - owner.getRadius(), pos.y - 30, scale);
		health.draw(pos.x - owner.getRadius(), pos.y - 30, (float)(owner.getHealth())/(float)(owner.getMaximumHealth()) * health.getWidth(), health.getHeight());
 		bar.draw(pos.x - owner.getRadius(), pos.y - 30, scale);
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		
 		
 		
 	}
 }
