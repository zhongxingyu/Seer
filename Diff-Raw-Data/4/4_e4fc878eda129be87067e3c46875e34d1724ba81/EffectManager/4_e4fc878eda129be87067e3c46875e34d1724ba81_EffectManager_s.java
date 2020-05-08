 package cz.emo4d.zen.gameplay;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.utils.Array;
 
 public class EffectManager {
 
 	public enum AvailableEffects {
 		BULLET_EXPLOSION, DIE_EXPLOSION, HIT_BLOOD
 	}
 
 	private Array<Effect> activeEffects = new Array<Effect>();
 	private Texture explosionTexture, dieTexture, hitBloodTexture;
 
 	public EffectManager() {
 		explosionTexture = new Texture(Gdx.files.internal("data/effects/fx_bombsplosion_big_32.png"));
		dieTexture = new Texture(Gdx.files.internal("data/effects/blood_death.png"));
		hitBloodTexture = new Texture(Gdx.files.internal("data/effects/blood_hit.png"));
 	}
 
 	public void addEffect(AvailableEffects effect, float posX, float posY) {
 
 		switch (effect) {
 			case BULLET_EXPLOSION:
 				activeEffects.add(new Effect(explosionTexture, 8, 1, 0.05f, 0, posX, posY));
 				break;
 			case DIE_EXPLOSION:
 				activeEffects.add(new Effect(dieTexture, 8, 1, 0.075f, 0, posX - (1 / 32f * 15), posY - (1 / 32f * 12)));
 				break;
 			case HIT_BLOOD:
 				activeEffects.add(new Effect(hitBloodTexture, 10, 1, 0.075f, 0, posX - (1 / 32f * 15), posY - (1 / 32f * 12)));
 				break;
 		}
 	}
 
 	public void update(float deltaTime) {
 		for (int i = 0; i < activeEffects.size; i++) {
 			activeEffects.get(i).update(0, false);
 			if (activeEffects.get(i).isAnimationFinished(0)) {
 				activeEffects.removeIndex(i);
 			}
 		}
 		/*for (Effect e : activeEffects) {	
 			e.update(0, false);
 		}*/
 	}
 
 	public void render(SpriteBatch spriteBatch) {
 		for (Effect e : activeEffects) {
 			e.render(spriteBatch, e.posX, e.posY, 1 / 32f * e.width, 1 / 32f * e.height);
 		}
 	}
 }
