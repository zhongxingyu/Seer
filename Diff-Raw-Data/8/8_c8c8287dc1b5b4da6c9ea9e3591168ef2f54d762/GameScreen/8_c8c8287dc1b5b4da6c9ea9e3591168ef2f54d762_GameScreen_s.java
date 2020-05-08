 package com.gamelab.mmi;
 
 import java.awt.List;
 import java.sql.BatchUpdateException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Vector2;
 
 public class GameScreen implements Screen {
 
 	private Mmi game;
 	private GameScreenInputHandler gameScreenInputHandler;
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	private Texture texture;
 	private Sprite sprite;
 	private Player player;
 	private Door door;
 	private Random rand = new Random();
 	private Map map;
 	private Button[] buttons;
 	private Button settings;
 	private Enemy[] enemies;
 	private PercentagePanel percentagePanel;
 	private int level = 0;
 	private LevelParameters lvlParm;
 
 	private MusicController musicController;
 
 	private void createButtons() {
 		buttons = new Button[Player.numberOfTools];
 		if (lvlParm.getPixelTool()) {
 			buttons[0] = new Button(5, 5, 100, 100, "data/Pinsel.png",
 					new ClickEvent() {
 
 						@Override
 						public void onClick(int x, int y) {
 							for (Button b : buttons) {
 								if (b != null) {
 									if (b.getState() == Button.STATE_ACTIVE) {
 										b.setState(Button.STATE_INACTIVE);
 									}
 								}
 							}
 							buttons[0].setState(Button.STATE_ACTIVE);
 							player.setTool(Player.TOOL_PIXEL);
 
 						}
 					}, Button.STATE_INACTIVE, 0);
 		}
 		if (lvlParm.getHuetralizerTool()) {
 			buttons[1] = new Button(125, 5, 100, 100, "data/Pinsel.png",
 					new ClickEvent() {
 
 						@Override
 						public void onClick(int x, int y) {
 							for (Button b : buttons) {
 								if (b != null) {
 									if (b.getState() == Button.STATE_ACTIVE) {
 										b.setState(Button.STATE_INACTIVE);
 									}
 								}
 							}
 							buttons[1].setState(Button.STATE_ACTIVE);
 							player.setTool(Player.TOOL_HUETRALIZER);
 
 						}
 					}, Button.STATE_INACTIVE, 5);
 		}
 		if (lvlParm.getColorSuckerTool()) {
 			buttons[2] = new Button(245, 5, 100, 100, "data/Pinsel.png",
 					new ClickEvent() {
 
 						@Override
 						public void onClick(int x, int y) {
 							for (Button b : buttons) {
 								if (b != null) {
 									if (b.getState() == Button.STATE_ACTIVE) {
 										b.setState(Button.STATE_INACTIVE);
 									}
 								}
 							}
 							buttons[2].setState(Button.STATE_ACTIVE);
 							player.setTool(Player.TOOL_COLOR_SUCKER);
 
 						}
 					}, Button.STATE_INACTIVE, 1);
 		}
 		if (lvlParm.getPixelSwapperTool()) {
 			buttons[3] = new Button(365, 5, 100, 100, "data/Pinsel.png",
 					new ClickEvent() {
 
 						@Override
 						public void onClick(int x, int y) {
 							for (Button b : buttons) {
 								if (b != null) {
 									if (b.getState() == Button.STATE_ACTIVE) {
 										b.setState(Button.STATE_INACTIVE);
 									}
 								}
 							}
 							buttons[3].setState(Button.STATE_ACTIVE);
 							player.setTool(Player.TOOL_PIXEL_SWAPPER);
 
 						}
 					}, Button.STATE_INACTIVE, 3);
 		}
 		if (lvlParm.getNegatronTool()) {
 			buttons[4] = new Button(485, 5, 100, 100, "data/Pinsel.png",
 					new ClickEvent() {
 
 						@Override
 						public void onClick(int x, int y) {
 							for (Button b : buttons) {
 								if (b != null) {
 									if (b.getState() == Button.STATE_ACTIVE) {
 										b.setState(Button.STATE_INACTIVE);
 									}
 								}
 							}
 							buttons[4].setState(Button.STATE_ACTIVE);
 							player.setTool(Player.TOOL_NEGATRON);
 
 						}
 					}, Button.STATE_INACTIVE, 4);
 		}
 		if (lvlParm.getWetWiperTool()) {
 			buttons[5] = new Button(605, 5, 100, 100, "data/Pinsel.png",
 					new ClickEvent() {
 
 						@Override
 						public void onClick(int x, int y) {
 							for (Button b : buttons) {
 								if (b != null) {
 									if (b.getState() == Button.STATE_ACTIVE) {
 										b.setState(Button.STATE_INACTIVE);
 									}
 								}
 							}
 							buttons[5].setState(Button.STATE_ACTIVE);
 							player.setTool(Player.TOOL_WETWIPER);
 
 						}
 					}, Button.STATE_INACTIVE, 2);
 		}
 		if (lvlParm.getWalkTool()) {
 			buttons[6] = new Button(725, 5, 100, 100, "data/Pinsel.png",
 					new ClickEvent() {
 
 						@Override
 						public void onClick(int x, int y) {
 							for (Button b : buttons) {
 								if (b != null) {
 									if (b.getState() == Button.STATE_ACTIVE) {
 										b.setState(Button.STATE_INACTIVE);
 									}
 								}
 							}
 							buttons[6].setState(Button.STATE_ACTIVE);
 							player.setTool(Player.TOOL_WALK);
 
 						}
 					}, Button.STATE_ACTIVE, 8);
 		}
 		settings = new Button(Gdx.graphics.getWidth() - 100,
 				Gdx.graphics.getHeight() - 43, 36, 36,
 				"data/Circle-Settings.png", new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						showMenu();
 					}
 				}, 0, 0);
 	}
 
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	private void showMenu() {
 		game.setScreen(new MenuScreen(game));
 	}
 
 	public GameScreen(Mmi game, LevelParameters lvlParm) {
 		this.game = game;
 		this.lvlParm = lvlParm;
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 
 		createButtons();
 
 		map = new Map(lvlParm.file);
 		camera = new OrthographicCamera(1, h / w);
 		batch = new SpriteBatch();
 
 		door = new Door(game);
 		door.activate(new Vector2(Math.abs(rand.nextInt())
 				% (w - Door.SIZE * 2) + Door.SIZE, Math.abs(rand.nextInt())
 				% (h - Door.SIZE * 2) + Door.SIZE));
 		door.deactivate();
 		texture = new Texture(Gdx.files.internal(lvlParm.file));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
 
 		sprite = new Sprite(region);
 		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
 		sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
 		sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);
 
 		player = new Player(new Vector2(w / 2, h / 2), lvlParm.firstTool, map);
 
 		gameScreenInputHandler = new GameScreenInputHandler(this, player);
 		for (Button b : buttons) {
 			if (b != null) {
 				gameScreenInputHandler.addEvent(b.getOnClick());
 			}
 		}
 		gameScreenInputHandler.addEvent(settings.getOnClick());
 		Gdx.input.setInputProcessor(gameScreenInputHandler);
 
 		musicController = new MusicController();
 		map.calcRelativeColors();
 		musicController.setRelVolumes(map.getRelRed(), map.getRelGreen(),
 				map.getRelBlue());
 
 		percentagePanel = new PercentagePanel();
 
 		createEnemies();
 
 	}
 
 	public void setInputProcessor() {
 		Gdx.input.setInputProcessor(gameScreenInputHandler);
 	}
 
 	@Override
 	public void render(float delta) {
 		if(!update(delta)) {
 			return;
 		}
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		batch.begin();
 
 		map.getMapPh().sprite.draw(batch);
 		batch.end();
 
 		door.render();
 		player.render();
 		for (Enemy e : enemies) {
 			e.render();
 		}
 		for (Button b : buttons) {
 			if (b != null) {
 				b.render();
 			}
 		}
 
 		if (musicController.needVolumeUpdate(delta)) {
 			map.calcRelativeColors();
 			musicController.setRelVolumes(map.getRelRed(), map.getRelGreen(),
 					map.getRelBlue());
 			musicController.updateVolumes(map.getRelativeTouched());
 		}
 
 		percentagePanel.render(Integer.toString((int) (100 * map
 				.getRelativeTouched())) + "%");
 	}
 
 	private void createEnemies() {
 		final int numberOfEnemies = lvlParm.getNumberOfEnemys();
 		enemies = new Enemy[numberOfEnemies];
 		for (int i = 0; i < numberOfEnemies; i++) {
 			createEnemy(i);
 		}
 	}
 
 	private void createEnemy(int index) {
 		enemies[index] = new Enemy(new Vector2(),
 				lvlParm.getEnemyParameters(index).enemy, map, player);
 	}
 
 	private Vector2 calcEnemySpawn() {
 		float randWidth = rand.nextFloat();
 		float randHeight = rand.nextFloat();
 		if (Math.abs(randWidth - 0.5f) < Math.abs(randHeight - 0.5f)) {
 			if (randWidth >= 0) {
 				randWidth = 1;
 			} else {
 				randWidth = 0;
 			}
 		} else {
 			if (randHeight >= 0) {
 				randHeight = 1;
 			} else {
 				randHeight = 0;
 			}
 		}
 		return new Vector2(randWidth * Gdx.graphics.getWidth(), randHeight
 				* Gdx.graphics.getHeight());
 	}
 
 	private void updateEnemies(float delta) {
 		final float rt = map.getRelativeTouched();
 		final int noe = lvlParm.getNumberOfEnemys();
 		for (int i = 0; i < noe; i++) {
 			final EnemyParameters ep = lvlParm.getEnemyParameters(i);
 			final Enemy e = enemies[i];
 			if (!e.getActive() && rt > ep.enemySpawn) {
 				e.activate(calcEnemySpawn());
 			}
 			if (e.getActive() && rt < ep.enemyDespawn) {
 				e.deactivate();
 			}
 			e.update(delta);
 		}
 	}
 
 	public boolean update(float delta) {
 		player.update(delta);
 		updateEnemies(delta);
 		if (!door.isActive() && map.getRelativeTouched() >= lvlParm.doorSpawn) {
 			door.activate(new Vector2(Math.abs(rand.nextInt())
 					% (Gdx.graphics.getWidth() - Door.SIZE * 2) + Door.SIZE,
 					Math.abs(rand.nextInt())
 							% (Gdx.graphics.getHeight() - Door.SIZE * 2)
 							+ Door.SIZE));
 
 		}
 		if (map.getRelativeTouched() < lvlParm.doorDespawn) {
 			door.deactivate();
 		}
 
 		collideEnemies();
 		if (door.isActive()
 				&& Intersector.overlapCircleRectangle(player.getHitbox(),
 						door.getHitbox())) {
 			game.prefs.putInt("level", level);
 			game.nextLevel();
 			return false;
 		}
 		return true;
 	}
 
 	private void collideEnemies() {
 		for (Enemy e : enemies) {
 			e.collide();
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		musicController.startRed(map.getRelRed());
 		musicController.startGreen(map.getRelGreen());
 		musicController.startBlue(map.getRelBlue());
 		Gdx.input.setInputProcessor(gameScreenInputHandler);
 
 	}
 
 	@Override
 	public void hide() {
 		musicController.dispose();
 		player.disposeSounds();
 		map.dispose();
 		for (Enemy enemy : enemies) {
 			enemy.dispose();
 		}
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
		batch.dispose();
		texture.dispose();

		player.dispose();
 	}
 
 }
