 package ch.kanti_wohlen.asteroidminer.animations;
 
 import java.util.AbstractMap;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.Map;
 
 import ch.kanti_wohlen.asteroidminer.AsteroidMiner;
 import ch.kanti_wohlen.asteroidminer.Player;
 import ch.kanti_wohlen.asteroidminer.audio.SoundPlayer;
 import ch.kanti_wohlen.asteroidminer.audio.SoundPlayer.SoundEffect;
 import ch.kanti_wohlen.asteroidminer.entities.Damageable;
 import ch.kanti_wohlen.asteroidminer.entities.Entity;
 import ch.kanti_wohlen.asteroidminer.entities.EntityType;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.QueryCallback;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class Explosion implements Animation {
 
 	private static final ShapeRenderer renderer = new ShapeRenderer();
 	private static final float EXPLOSION_SCORE_MULTIPLIER = 0.05f;
 	private static final float EXPLOSION_SPEED = 1.3f;
 
 	private final World world;
 	private final Vector2 center;
 	private final float maxRadius;
 	private final int maxDmg;
 	private final boolean dmgPlayer;
 	private final Player player;
 	private final LinkedList<AbstractMap.SimpleImmutableEntry<Entity, Float>> entities;
 
 	private float currentRadius;
 	private boolean removed;
 
 	public Explosion(World theWorld, Vector2 explosionCenter, float radius, int maxDamage, boolean hurtPlayer, Player owner) {
 		world = theWorld;
 		center = explosionCenter;
 		maxRadius = radius;
 		maxDmg = maxDamage;
 		dmgPlayer = hurtPlayer;
 		currentRadius = 1;
 		player = owner;
 
 		entities = new LinkedList<AbstractMap.SimpleImmutableEntry<Entity, Float>>();
 		final Vector2 upperLeft = explosionCenter.cpy().sub(maxRadius, maxRadius);
 		final Vector2 lowerRight = explosionCenter.cpy().add(maxRadius, maxRadius);
 		world.QueryAABB(new QueryCallback() {
 
 			@Override
 			public boolean reportFixture(Fixture fixture) {
 				final Entity entity = (Entity) fixture.getBody().getUserData();
 				if (entity.getType() == EntityType.ASTEROID || (entity.getType() == EntityType.SPACESHIP) && dmgPlayer) {
 					final float r2 = fixture.getBody().getPosition().dst2(center);
 
 					if (r2 <= maxRadius * maxRadius) {
 						entities.add(new AbstractMap.SimpleImmutableEntry<Entity, Float>(entity, r2));
 					}
 				}
 				return true;
 			}
 		}, upperLeft.x, upperLeft.y, lowerRight.x, lowerRight.y);
 
 		Collections.sort(entities, new Comparator<Map.Entry<Entity, Float>>() {
 
 			@Override
 			public int compare(Map.Entry<Entity, Float> o1, Map.Entry<Entity, Float> o2) {
 				return o1.getValue().compareTo(o2.getValue());
 			}
 		});
 
 		center.scl(Entity.BOX2D_TO_PIXEL); // Scale center for rendering.
 		AsteroidMiner.INSTANCE.getGameScreen().getAnimations().addAnimation(this);
 		SoundPlayer.playSound(SoundEffect.EXPLOSION, 1f);
 	}
 
 	@Override
 	public void render(SpriteBatch batch) {
 		batch.end();
 		Gdx.gl.glEnable(GL10.GL_BLEND);
 		renderer.begin(ShapeType.Filled);
 		renderer.setTransformMatrix(batch.getTransformMatrix());
 		renderer.setProjectionMatrix(batch.getProjectionMatrix());
 		final float alpha = (maxRadius - currentRadius) / maxRadius;
 		final float yellow = Math.abs((alpha % 0.4f) - 0.2f);
 		renderer.setColor(new Color(1f, yellow, 0f, alpha));
 		renderer.circle(center.x, center.y, currentRadius * Entity.BOX2D_TO_PIXEL);
 		renderer.end();
 		batch.begin();
 	}
 
 	public void tick(float deltaTime) {
 		currentRadius += EXPLOSION_SPEED;
 		if (currentRadius > maxRadius) {
 			removed = true;
 			return;
 		}
 		if (entities.isEmpty()) return;
 
 		final float radiusMax = currentRadius * currentRadius;
 
 		for (Map.Entry<Entity, Float> entry = entities.getFirst(); entry.getValue() < radiusMax; entry = entities.getFirst()) {
 			if (entry.getKey().isRemoved()) {
				entities.removeFirst();
				if (entities.isEmpty()) {
					break;
				} else {
					continue;
				}
 			}
 
 			final int damage = (int) (maxDmg * Math.min(1f, 1.25f * (maxRadius - currentRadius) / maxRadius));
 			final Damageable damageable = (Damageable) entry.getKey();
 			damageable.damage(damage, player, EXPLOSION_SCORE_MULTIPLIER);
 			entities.removeFirst();
 			if (entities.isEmpty()) break;
 		}
 	}
 
 	@Override
 	public boolean isRemoved() {
 		return removed;
 	}
 
 	@Override
 	public void dispose() {
 		entities.clear();
 	}
 }
