 package com.gradugation;
 
 import java.io.IOException;
 
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.adt.color.Color;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Handler;
 import android.text.Editable;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class BalancingMiniGame extends SimpleBaseGameActivity {
 
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 320;
 
 	Scene scene;
 	static int CREDITS_EARNED = 3;
 
 	private int mysteryWeight = 1 + (int) (Math.random() * 100);
 	private int numGuesses = 1;
 
 	private ITextureRegion needLessWeightRegion;
 	private BitmapTextureAtlas needLessWeightAtlas;
 	private ITextureRegion needMoreWeightRegion;
 	private BitmapTextureAtlas needMoreWeightAtlas;
 	private ITextureRegion equalWeightRegion;
 	private BitmapTextureAtlas equalWeightAtlas;
 
 	private ITextureRegion bgR;
 	private BitmapTextureAtlas bgA;
 	private Sprite bgS;
 
 	private Sprite needLessWeightSprite;
 	private Sprite needMoreWeightSprite;
 	private Sprite equalWeightSprite;
 
 	private AlertDialog.Builder alertDialogBuilder;
 	private AlertDialog alertDialog;
 
 	private String characterType;
 	private boolean more = true;
 	private boolean prevMore = true;
 
 	final Handler mHandler = new Handler();
 
 	final Runnable mUpdateResults = new Runnable() {
 		public void run() {
 			updateResultsInUi();
 		}
 	};
 
 	private void updateResultsInUi() {
 		alertDialogBuilder = new AlertDialog.Builder(this);
 
 		// set title and message
 		alertDialogBuilder
 				.setTitle("Try to balance the scale! It is between 1 and 100lbs. You have 7 guesses.");
 		alertDialogBuilder.setMessage("Press Continue to play.");
 		alertDialogBuilder.setCancelable(false);
 
 		// create continue button
 		alertDialogBuilder.setNeutralButton("Continue",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// if this button is clicked, close
 						// dialog box
 						scene.attachChild(needMoreWeightSprite);
 						guessSeven();
 						dialog.cancel();
 						dialog.dismiss();
 
 					}
 				});
 
 		// create alert dialog
 		alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		final Camera mCamera = new BoundCamera(0, 0, CAMERA_WIDTH,
 				CAMERA_HEIGHT);
 
 		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
 	}
 
 	@Override
 	protected void onCreateResources() throws IOException {
 		characterType = getIntent().getStringExtra("character_type");
 		if (characterType == null)
 			characterType = "Gradugator";
 
 		BitmapTextureAtlasTextureRegionFactory
 				.setAssetBasePath("gfx/balance_game_images/");
 
 		needLessWeightAtlas = new BitmapTextureAtlas(this.getTextureManager(),
 				1024, 1024, TextureOptions.BILINEAR);
 		needLessWeightRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(needLessWeightAtlas, this,
 						"NeedLessWeight_scale.png", 0, 0);
 		needLessWeightAtlas.load();
 
 		needMoreWeightAtlas = new BitmapTextureAtlas(this.getTextureManager(),
 				1024, 1024, TextureOptions.BILINEAR);
 		needMoreWeightRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(needMoreWeightAtlas, this,
 						"NeedMoreWeight_scale.png", 0, 0);
 		needMoreWeightAtlas.load();
 
 		equalWeightAtlas = new BitmapTextureAtlas(this.getTextureManager(),
 				1024, 1024, TextureOptions.BILINEAR);
 		equalWeightRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(equalWeightAtlas, this,
 						"EqualWeight_scale.png", 0, 0);
 		equalWeightAtlas.load();
 
 		bgA = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024,
 				TextureOptions.BILINEAR);
 		bgR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bgA, this,
 				"BalanceBG.png", 0, 0);
 		bgA.load();
 
 		bgS = new Sprite(240, 160, bgR, this.getVertexBufferObjectManager());
 		needLessWeightSprite = new Sprite(240, 160, needLessWeightRegion,
 				this.getVertexBufferObjectManager());
 		needMoreWeightSprite = new Sprite(240, 160, needMoreWeightRegion,
 				this.getVertexBufferObjectManager());
 		equalWeightSprite = new Sprite(240, 160, equalWeightRegion,
 				this.getVertexBufferObjectManager());
 
 		needLessWeightSprite.setScale((float) .8);
 		needMoreWeightSprite.setScale((float) .8);
 		equalWeightSprite.setScale((float) .8);
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 		scene = new Scene();
 		scene.setBackground(new Background(Color.WHITE));
 		scene.attachChild(bgS);
 
 		mHandler.post(mUpdateResults);
 
 		return scene;
 	}
 
 	private void guessSeven() {
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		if (numGuesses == 1)
 			alert.setTitle("Guess " + numGuesses
 					+ ": What is your first guess?");
 		else if (more == true) {
 			alert.setTitle("Guess " + numGuesses + ": You need more weight!");
 		} else {
 			alert.setTitle("Guess " + numGuesses + ": You need less weight!");
 		}
 
 		alert.setMessage("Input amount below.");
 
 		alert.setCancelable(false);
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				Editable value = input.getText();
 				try {
 					int x = Integer.parseInt(value.toString());
 					//Toast.makeText(getApplicationContext(), "x is "+String.valueOf(x),Toast.LENGTH_LONG).show();
 					numGuesses++;
 					
					if ((int) x == (int) mysteryWeight) {
 						win();
						return;
 					}
 					if (numGuesses > 7){
 						dialog.cancel();
 						dialog.dismiss();
 						lose();
 					}
 					else {
 						if (x < mysteryWeight) {
 							detachRightSprite();
 							scene.attachChild(needMoreWeightSprite);
 							prevMore = more;
 							more = true;
 							guessSeven();
 						} else {
 							detachRightSprite();
 							scene.attachChild(needLessWeightSprite);
 							prevMore = more;
 							more = false;
 							guessSeven();
 						}
 					}
 				} catch (NumberFormatException e) {
 					numGuesses++;
 					if (numGuesses > 7){
 						dialog.cancel();
 						dialog.dismiss();
 						lose();
 					}
 					guessSeven();
 				}
 
 			}
 		});
 		if (numGuesses > 7)
 			lose();
 		alert.show();
 	}
 
 	private void detachRightSprite() {
 		scene.detachChild(needMoreWeightSprite);
 		scene.detachChild(needLessWeightSprite);
 	}
 
 	private void win() {
 		detachRightSprite();
 		scene.attachChild(equalWeightSprite);
 
 		// update the player's credits
 		Intent output = new Intent();
 		output.putExtra(Event.BALANCE_REQUEST_CODE + "", CREDITS_EARNED);
 		setResult(RESULT_OK, output);
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Correct!");
 		alert.setMessage("You've earned some credits!");
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Do something with value!
 				finish();
 			}
 		});
 
 		alert.show();
 
 	}
 
 	private void lose() {
 		Intent output = new Intent();
 		output.putExtra(Event.BALANCE_REQUEST_CODE + "", 0);
 		setResult(RESULT_OK, output);
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Sorry!");
 		alert.setMessage("Come back to try again. The mystery weight was "+ mysteryWeight+".");
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Do something with value!
 				finish();
 			}
 		});
 
 		alert.show();
 	}
 }
