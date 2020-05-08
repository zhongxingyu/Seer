 package ultraextreme.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import javax.vecmath.Vector2d;
 
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import ultraextreme.model.GameModel;
 import ultraextreme.model.ModelInput;
 import ultraextreme.model.enemy.AbstractEnemy;
 import ultraextreme.model.enemy.IEnemy;
 import ultraextreme.model.entity.AbstractEntity;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.view.GameObjectSprite;
 import ultraextreme.view.GameScene;
 import ultraextreme.view.SpriteFactory;
 import android.util.Log;
 
 /**
  * 
  * @author Daniel Jonsson, Bjorn Persson Mattsson
  * 
  */
 public class GameLoop implements IUpdateHandler, PropertyChangeListener {
 
 	private GameScene gameScene;
 	private GameModel gameModel;
 	private List<GameObjectSprite> gameObjectSprites;
 	private VertexBufferObjectManager vertexBufferObjectManager;
 	private SpriteFactory spriteFactory;
 	private Vector2d scalingQuotient;
 
 	private boolean firing;
 	private double moveX;
 	private double moveY;
 	private boolean specialAttack;
 
 	public GameLoop(GameScene gameScene, GameModel gameModel,
 			List<GameObjectSprite> gameObjectSprites,
 			VertexBufferObjectManager vertexBufferObjectManager,
 			SpriteFactory spriteFactory, double screenWidth, double screenHeight) {
 
 		Dimension screenDimension = new Dimension(screenWidth, screenHeight);
 		this.scalingQuotient = screenDimension.getQuotient(
 				Constants.getInstance().getLevelDimension());
 		this.gameScene = gameScene;
 		this.gameModel = gameModel;
 		this.gameObjectSprites = gameObjectSprites;
 		this.vertexBufferObjectManager = vertexBufferObjectManager;
 		this.spriteFactory = spriteFactory;
 	}
 
 	@Override
 	public void onUpdate(float time) {
 		gameModel.update(new ModelInput(moveX / scalingQuotient.x, 
 				moveY / scalingQuotient.y, firing, specialAttack),
 				time);
 		moveX = 0;
 		moveY = 0;
 
 		for (GameObjectSprite sprite : gameObjectSprites) {
 			sprite.update();
			specialAttack = false;

 		}
 	}
 
 	@Override
 	public void reset() {
 		// Auto-generated method stub
 
 	}
 
 	// @Override
 	// public void propertyChange(PropertyChangeEvent event) {
 	// if (event.getPropertyName().equals("newBullet")) {
 	// BulletSprite b = new BulletSprite(
 	// (AbstractBullet) event.getNewValue(),
 	// vertexBufferObjectManager);
 	// bulletSprites.add(b);
 	// gameScene.attachChild(b);
 	// Log.d("Bullet list length View", "" + bulletSprites.size());
 
 	// } else if (event.getPropertyName().equals(EnemyManager.NEW_ENEMY)) {
 	// EnemySprite e = new EnemySprite(
 	// ((IEnemy) event.getNewValue()).getShip(),
 	// vertexBufferObjectManager);
 	// enemySprites.add(e);
 	// gameScene.attachChild(e);
 	// }
 	// }
 
 	/**
 	 * If a new sprite is created, adds it to the scene and to the list if a
 	 * sprite is removed, removes it from the scene and the list
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equals("add")) {
 			AbstractEntity entity;
 
 			if (event.getNewValue() instanceof IEnemy) {
 				entity = ((AbstractEnemy) event.getNewValue()).getShip();
 			} else { // if item or bullet
 				entity = (AbstractEntity) event.getNewValue();
 
 				GameObjectSprite newSprite = spriteFactory.getNewSprite(entity,
 						vertexBufferObjectManager);
 				gameScene.attachChild(newSprite);
 				gameObjectSprites.add(newSprite);
 			}
 
 		} else if (event.getPropertyName().equals("remove")) {
 			// Find the GameObjectSprite that has a reference to this entity and
 			// remove
 			// it from the GameObjectSprite list and from the render scene.
 			// Note: It's generally not a very good idea to remove elements when
 			// iterating through them, but this breaks the loop when one element
 			// is
 			// removed.
 			for (GameObjectSprite sprite : gameObjectSprites) {
 				if (sprite.getEntity() == event.getNewValue()) {
 					gameScene.detachChild(sprite);
 					gameObjectSprites.remove(sprite);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets if the player is firing or not.
 	 * 
 	 * @param b
 	 *            true if the player is firing, otherwise false.
 	 */
 	public void setFiring(boolean b) {
 		this.firing = b;
 	}
 
 	/**
 	 * Adds the provided change to the movement.
 	 * 
 	 * @param dX
 	 *            The change in the x axis.
 	 * @param dY
 	 *            The change in the y axis.
 	 */
 	public void addToMovement(double dX, double dY) {
 		this.moveX += dX;
 		this.moveY += dY;
 	}
 
 	/**
 	 * Orders the player to fire a special attack.
 	 */
 	public void fireSpecialAttack() {
 		specialAttack = true;
 		Log.d("DEBUG", "specialAttack " + specialAttack);
 	}
 }
