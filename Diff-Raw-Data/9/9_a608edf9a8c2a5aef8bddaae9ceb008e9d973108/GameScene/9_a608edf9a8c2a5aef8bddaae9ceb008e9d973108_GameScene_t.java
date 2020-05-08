 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.view;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.vecmath.Vector2d;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import ultraextreme.model.IUltraExtremeModel;
 import ultraextreme.model.item.ItemBar;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.Position;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 /**
  * Class responsible for reflecting the model with a graphical representation
  * 
  * @author Johan Gronvall
  * @author Daniel Jonsson
  * 
  */
 public class GameScene extends Scene implements SensorEventListener {
 
 	// FIXME These shouldn't be hard-coded in?
 	private static final Position SCORE_POS = new Position(10, 10);
 	private static final Position LIVES_POS = new Position(10, 40);
	private static final Position ITEMBAR_POS = new Position(145, 1400);
 
 	private final IUltraExtremeModel gameModel;
 	private GameObjectSprite shipSprite;
 
 	private final SensorManager sensorManager;
 	private final List<GameObjectSprite> gameObjectSprites;
 	private ItemBarPanel itemBarPanel;
 	private final HUD hud;
 
 	public GameScene(final IUltraExtremeModel gameModel,
 			final VertexBufferObjectManager vertexBufferObjectManager,
 			final SensorManager sensorManager, final double screenWidth,
 			final double screenHeight, Camera camera, Font font) {
 		super();
 
 		this.gameModel = gameModel;
 		setBackground(new Background(0, 0, 0));
 
 		gameObjectSprites = new LinkedList<GameObjectSprite>();
 		final GameObjectSprite playerSprite = SpriteFactory.getInstance()
 				.getNewSprite(gameModel.getPlayer().getShip(),
 						vertexBufferObjectManager);
 
 		gameObjectSprites.add(playerSprite);
 		attachChild(playerSprite);
 
 		final Dimension screenDimension = new Dimension(screenWidth,
 				screenHeight);
 		Vector2d scaling = screenDimension.getQuotient(Constants
 				.getLevelDimension());
 
 		ItemBar itemBar = gameModel.getPlayer().getItemBar();
 		itemBarPanel = new ItemBarPanel(itemBar, SpriteFactory.getInstance(),
 				vertexBufferObjectManager, ITEMBAR_POS, scaling);
 
 		ScoreText scoreText = new ScoreText(SCORE_POS, font,
 				vertexBufferObjectManager);
 		gameModel.registerPlayerListener(scoreText);
 		LivesText livesText = new LivesText(LIVES_POS, font,
 				vertexBufferObjectManager);
 		gameModel.registerPlayerListener(livesText);
 
 		hud = new HUD();
 		hud.setVisible(false);
 		hud.attachChild(itemBarPanel);
 		hud.attachChild(scoreText);
 		hud.attachChild(livesText);
 		camera.setHUD(hud);
 
 		this.sensorManager = sensorManager;
 		sensorManager.registerListener(this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_GAME);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, final int accuracy) {
 		// Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		// shipSprite.setX(shipSprite.getX() + event.values[1]);
 	}
 
 	public List<GameObjectSprite> getGameObjectSprites() {
 		return gameObjectSprites;
 	}
 
 	public ItemBarPanel getItemBarPanel() {
 		return itemBarPanel;
 	}
 
 	public void setHUDVisible(boolean b) {
 		hud.setVisible(b);
 	}
 }
