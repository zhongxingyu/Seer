 package com.thorgaming.throwme.screens;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.WindowManager;
 
 import com.thorgaming.throwme.GameState;
 import com.thorgaming.throwme.R;
 import com.thorgaming.throwme.ThrowMe;
 import com.thorgaming.throwme.billing.BillingService;
 import com.thorgaming.throwme.callback.Callback;
 import com.thorgaming.throwme.callback.MouseCallback;
import com.thorgaming.throwme.displayobjects.DispGif;
 import com.thorgaming.throwme.displayobjects.DispRes;
 import com.thorgaming.throwme.displayobjects.DispRes_Rel;
 import com.thorgaming.throwme.displayobjects.game.Crane;
 import com.thorgaming.throwme.displayobjects.game.HUD;
 import com.thorgaming.throwme.displayobjects.game.PauseMenu;
 import com.thorgaming.throwme.displayobjects.game.characters.Character;
 import com.thorgaming.throwme.displayobjects.game.characters.Characters;
 import com.thorgaming.throwme.displayobjects.game.cloud.BoostCloud;
 import com.thorgaming.throwme.displayobjects.game.cloud.ColouredCloud;
 import com.thorgaming.throwme.displayobjects.game.cloud.LightningCloud;
 import com.thorgaming.throwme.displayobjects.shape.PhysCircle;
 import com.thorgaming.throwme.displayobjects.shape.Rect;
 import com.thorgaming.throwme.displayobjects.shape.Text_Rel;
 import com.thorgaming.throwme.drawing.DrawThread;
 import com.thorgaming.throwme.drawing.HitListener;
 import com.thorgaming.throwme.drawing.RenderPriority;
 
 /**
  * Game screen
  * 
  * @author Thomas Cheyney
  * @version 1.0
  */
 public class Game extends Screen {
 
 	static {
 		@SuppressWarnings("unused")
 		byte dummy[] = new byte[9 * 1024 * 1024];
 	}
 	/**
 	 * The background gradient used to calculate the gradient to show to the user
 	 */
 	private int gradient[][] = new int[9][3];
 	/**
 	 * Hill backdrops
 	 */
 	private DispRes hills1, hills2;
 	/**
 	 * Box the character starts in
 	 */
 	private DispRes_Rel box;
 	/**
 	 * Physics hills at the bottom of the world
 	 */
 	private PhysCircle[] randomHills = new PhysCircle[7];
 	/**
 	 * The player's character
 	 */
 	private Character character;
 	/**
 	 * Used to calculate where to position the next hill
 	 */
 	private int hillDistance = 6;
 	/**
 	 * Limits how often cranes can be generated
 	 */
 	private int lastCrane = 6;
 	/**
 	 * Stores if the game has been lost yet
 	 */
 	private boolean ended = false;
 	/**
 	 * Random used to generate the size and frequency of cranes and hills
 	 */
 	private Random random = new Random();
 	/**
 	 * Menu displayed when the game is paused
 	 */
 	private PauseMenu pauseMenu;
 
 	private Characters currentChar;
 
 	public Game(Object[] data) {
 		super(data);
 
 		currentChar = Characters.getFromId(ThrowMe.getInstance().customisationSettings.getInt("char", 0));
 		if (!BillingService.purchases.isPurchased(currentChar.getMarketId())) { // Make sure we bought that :P
 			currentChar = Characters.GUY;
 			ThrowMe.getInstance().customisationSettings.edit().putInt("char", 0).commit();
 		}
 
 		ThrowMe.getInstance().stage.camera.setCameraXY(0, 0);
 		ThrowMe.getInstance().stage.drawThread.setPhysics(true);
 		ThrowMe.getInstance().runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				ThrowMe.getInstance().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 			}
 		});
 
 		gradient[0][0] = 255;
 		gradient[0][1] = 255;
 		gradient[0][2] = 255;
 
 		gradient[1][0] = 0;
 		gradient[1][1] = 102;
 		gradient[1][2] = 204;
 
 		gradient[2][0] = 255;
 		gradient[2][1] = 255;
 		gradient[2][2] = 0;
 
 		gradient[3][0] = 255;
 		gradient[3][1] = 153;
 		gradient[3][2] = 0;
 
 		gradient[4][0] = 153;
 		gradient[4][1] = 0;
 		gradient[4][2] = 51;
 
 		gradient[5][0] = 51;
 		gradient[5][1] = 255;
 		gradient[5][2] = 102;
 
 		gradient[6][0] = 0;
 		gradient[6][1] = 51;
 		gradient[6][2] = 204;
 
 		gradient[7][0] = 0;
 		gradient[7][1] = 0;
 		gradient[7][2] = 0;
 
 		gradient[8][0] = 0;
 		gradient[8][1] = 0;
 		gradient[8][2] = 0;
 
 		int[] ng = new int[2];
 		ng[0] = Color.rgb(0, 102, 204);
 		ng[1] = Color.rgb(255, 255, 255);
 
 		DrawThread.setgrad(ng);
 
 		pauseMenu = new PauseMenu();
 
 		hills1 = (DispRes) new DispRes_Rel(R.drawable.bg).setWidth(879).setHeight(240).setY(300).addToScreen(RenderPriority.Highest);
 		hills2 = (DispRes) new DispRes_Rel(R.drawable.bg).setWidth(879).setHeight(240).setX(800).setY(300).addToScreen(RenderPriority.Highest);
 
 		for (int i = 0; i < 7; i++) {
 			randomHills[i] = (PhysCircle) new PhysCircle(0).setRadius(random.nextInt(80) + 80).setX(160 * i + 80).setY(480).addToScreen();
 		}
 
 		box = (DispRes_Rel) new DispRes_Rel(R.drawable.box).setWidth(150).setHeight(150).setX(325).setY(105).addToScreen();
 		new Rect().setWidth(800).setHeight(480).setAlpha(0).setMouseDownEvent(new MouseCallback() {
 			@Override
 			public boolean sendCallback(int x, int y) {
 				if (character == null) {
 					box.destroy();
 					character = (Character) currentChar.createNew().setX(400).setY(240).addToScreen();
 					new HUD(character).addToScreen(RenderPriority.Low);
					new DispGif(R.drawable.explosion, 1, 4).setWidth(764).setHeight(556).setX(18).setY(-38).addToScreen();
 				}
 				return true;
 			}
 		}).addToScreen();
 
 		new LightningCloud().setWidth(133).setHeight(175).setX(1000).setY(-4000).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(800).setY(-3700).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(300).setY(-3500).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(550).setY(-3300).addToScreen(RenderPriority.High);
 		new LightningCloud().setWidth(133).setHeight(175).setX(500).setY(-2800).addToScreen(RenderPriority.High);
 		new ColouredCloud().setWidth(133).setHeight(75).setX(800).setY(-2500).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(550).setY(-1920).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(800).setY(-1320).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(300).setY(-1120).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(900).setY(-920).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(150).setY(-720).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(600).setY(-620).addToScreen(RenderPriority.High);
 		new BoostCloud().setX(850).setY(-120).addToScreen(RenderPriority.High);
 
 		new Text_Rel().setText("Press down on the box").setSize(30).setX(50).setY(45).addToScreen();
 		new Text_Rel().setText("and drag to throw!").setSize(30).setX(73).setY(75).addToScreen();
 
 		ThrowMe.getInstance().stage.world.setContactListener(new HitListener());
 
 		ThrowMe.getInstance().stage.draw = new Callback() {
 			@Override
 			public void sendCallback() {
 				if (character != null && character.getGameState() == GameState.END && !ended) {
 					ThrowMe.getInstance().stage.drawThread.runOnUi(new Runnable() {
 						@Override
 						public void run() {
 							final Activity act = ThrowMe.getInstance();
 							int check = Screen.checkCount;
 							act.runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									new Submit(new Object[] {ThrowMe.getInstance().stage.camera.getX() / 10});
 								}
 							});
 							while (check == Screen.checkCount) {
 							}
 						}
 					});
 					ended = true;
 				}
 				if (!ended) {
 					if (hills1.getScreenX() < -ThrowMe.getInstance().stage.camera.getScreenWidth()) {
 						hills1.setX(ThrowMe.getInstance().stage.camera.getX() + ThrowMe.getInstance().stage.camera.getScreenWidth());
 					}
 					if (hills2.getScreenX() < -ThrowMe.getInstance().stage.camera.getScreenWidth()) {
 						hills2.setX(ThrowMe.getInstance().stage.camera.getX() + ThrowMe.getInstance().stage.camera.getScreenWidth());
 					}
 					for (int i = 0; i < 7; i++) {
 						if (randomHills[i].getScreenX() < -160) {
 							hillDistance++;
 							randomHills[i].move(hillDistance * 160 + 80, 480);
 							randomHills[i].randomiseColor();
 							randomHills[i].setRadius(random.nextInt(80) + 80);
 
 							if (lastCrane < hillDistance) {
 								if (random.nextInt(40) < 5) {
 									lastCrane = hillDistance + 3;
 									new Crane().setX(hillDistance * 160 + random.nextInt(80)).setY(90 + random.nextInt(40)).addToScreen(RenderPriority.High);
 								}
 							}
 						}
 					}
 
 					int ng[] = new int[2];
 					if (ThrowMe.getInstance().stage.camera.getY() > 7999) {
 
 						ng[0] = Color.rgb(0, 0, 0);
 						ng[1] = Color.rgb(0, 0, 0);
 
 					} else if (ThrowMe.getInstance().stage.camera.getY() > 0) {
 
 						int ny = ThrowMe.getInstance().stage.camera.getY() + 10;
 						if (ny % 1000 < ThrowMe.getInstance().stage.camera.getY() % 1000) {
 							ny -= ny % 1000 + 1;
 						}
 						ng[0] = blend(gradient[(int) Math.floor(ThrowMe.getInstance().stage.camera.getY() / 1000) + 1], gradient[(int) Math.floor(ThrowMe.getInstance().stage.camera.getY() / 1000)], ny % 1000);
 						ng[1] = blend(gradient[(int) Math.floor(ThrowMe.getInstance().stage.camera.getY() / 1000) + 1], gradient[(int) Math.floor(ThrowMe.getInstance().stage.camera.getY() / 1000)], ThrowMe.getInstance().stage.camera.getY() % 1000);
 
 					} else {
 						ng[0] = Color.rgb(255, 255, 255);
 						ng[1] = Color.rgb(255, 255, 255);
 					}
 					DrawThread.setgrad(ng);
 				}
 			}
 		};
 	}
 
 	/**
 	 * Blends two colours together to get the colour midway through a gradient
 	 * 
 	 * @param rgb1 Colour one
 	 * @param rgb2 Colour Two
 	 * @param ratio Ratio of each colour to mix
 	 * @return The colour achieved by mixing the two input colours
 	 */
 	public static int blend(int[] rgb1, int[] rgb2, double ratio) {
 		float r = (float) ratio / 1000;
 		float ir = (float) 1.0 - r;
 
 		int color = Color.rgb((int) (rgb1[0] * r + rgb2[0] * ir), (int) (rgb1[1] * r + rgb2[1] * ir), (int) (rgb1[2] * r + rgb2[2] * ir));
 		return color;
 	}
 
 	@Override
 	public boolean onTouch(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			if (character != null && character.getGameState() == GameState.ON_SPRING && character.throwTimeout <= 0) {
 				character.setGameState(GameState.LOOSE);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			pauseMenu.toggle();
 			return true;
 		}
 		return false;
 	}
 
 }
