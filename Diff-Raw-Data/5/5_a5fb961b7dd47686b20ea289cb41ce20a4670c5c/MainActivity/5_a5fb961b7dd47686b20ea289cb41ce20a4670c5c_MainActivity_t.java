 package se.chalmers.dat255.craycray.activity;
 /*
  * CrayCray - A game formed as a pet in you android device for the user to take care of.
  *
  * Copyright (C) 2013  Sofia Edstrom, Emma Gustafsson, Patricia Paulsson, 
  * Josefin Ondrus, Hanna Materne, Elin Ljunggren & Jonathan Thunberg.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/.
  * 
  * To contact the creators of this program, please make use of any of the email addresses below.
  * hanna.materne@gmail.com, elin.l.ljunggren@gmail.com, sofia.edstrom@galaxen.se
  * chorriballong@gmail.com, jonathan.thunberg@gmail.com, emma.i.gustafsson@gmail.com,
  * josefin.ondrus@gmail.com
  *   
  */
 
 import se.chalmers.dat255.craycray.R;
 import se.chalmers.dat255.craycray.database.DatabaseAdapter;
 import se.chalmers.dat255.craycray.database.DatabaseConstants;
 import se.chalmers.dat255.craycray.model.DeadException;
 import se.chalmers.dat255.craycray.model.NeedsModel;
 import se.chalmers.dat255.craycray.notifications.NotificationSender;
 import se.chalmers.dat255.craycray.util.Constants;
 import se.chalmers.dat255.craycray.util.TimeUtil;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.PorterDuff.Mode;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	MainActivity main = this;
 
 	private boolean isActive;
 
 	// The buttons of the application
 	private ImageButton  feedButton;
 	private ImageButton  cuddleButton;
 	private ImageButton  cleanButton;
 	private ImageButton  energyButton;
 	private ImageButton  removePooButton;
 	private ImageButton  cureButton;
 	private ImageButton happypotionButton;
 	private ImageButton russianButton;
 	private ImageButton aboutButton;
 
 	// The bars of the application
 	private ProgressBar foodBar;
 	private ProgressBar cuddleBar;
 	private ProgressBar cleanBar;
 	private ProgressBar energyBar;
 	private ImageView crayView;
 	private ImageView pooImage;
 	private View fade;
 
 	private NeedsModel model;
 	private Thread t;
 
 	private final int HUNGER = 1;
 	private final int CLEANNESS = 2;
 	private final int HAPPINESS = 3;
 	private final int ENERGY = 4;
 	private final int DRUNK = 5;
 
 	private final int POO = 1;
 	private final int NOPOO = 2;
 	
 	private int drunkCount = Constants.MAX_DRUNK_COUNT;
 
 	private boolean cleanability = true;
 	private boolean isDrunk = false;
 
 	private DatabaseAdapter dbA;
 	private NotificationSender notifications = new NotificationSender(this);
 
 
 	// A Handler to take care of updates in UI-thread
 	// When sendMessage method is called, this is where the message is sent
 	Handler handler = new Handler() {
 
 		@Override
 		public synchronized void handleMessage(Message msg) {
 			super.handleMessage(msg);
 
 			// sets/updates the values of the progressbars
 			foodBar.setProgress(model.getHungerLevel());
 			cuddleBar.setProgress(model.getCuddleLevel());
 			cleanBar.setProgress(model.getCleanLevel());
 			energyBar.setProgress(model.getEnergyLevel());
 
 			// force imageview to update
 			crayView.invalidate();
 
 			if (msg.obj instanceof DeadException) {
 
 				DeadException e = (DeadException) msg.obj;
 				announceDeath(e);
 				activatedButtons(false);
 			}
 
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 		fade=(View) findViewById(R.id.fade);
 		model = NeedsModel.getInstance();
 		dbA = new DatabaseAdapter(getBaseContext());
 
 		isActive = true;
 		Log.w("russian", "testing testing");
 
 		// Button - variables set to xml ID
 		feedButton = (ImageButton) findViewById(R.id.feedButton);
 		cleanButton = (ImageButton) findViewById(R.id.cleanButton);
 		cuddleButton = (ImageButton) findViewById(R.id.cuddleButton);
 		energyButton = (ImageButton) findViewById(R.id.energyButton);
 		removePooButton = (ImageButton) findViewById(R.id.removePooButton);
 		cureButton = (ImageButton) findViewById(R.id.cureButton);
 		happypotionButton = (ImageButton) findViewById(R.id.happypotionButton);
 		russianButton = (ImageButton) findViewById(R.id.russianButton);
 		aboutButton = (ImageButton) findViewById(R.id.aboutButton);
 		
 		
 		// Sets correct image to the buttons
 		feedButton.setImageResource(R.drawable.button_food);
 		cleanButton.setImageResource(R.drawable.button_clean);
 		cuddleButton.setImageResource(R.drawable.button_happiness);
 		energyButton.setImageResource(R.drawable.button_energy);
 		removePooButton.setImageResource(R.drawable.button_poo);
 		cureButton.setImageResource(R.drawable.button_cure);
 		happypotionButton.setImageResource(R.drawable.button_alcohol);
 		russianButton.setImageResource(R.drawable.button_roulette);
 		aboutButton.setImageResource(R.drawable.button_about);
 
 
 		//Bar - variables set to xml ID
 		foodBar = (ProgressBar) findViewById(R.id.foodBar);
 		cuddleBar = (ProgressBar) findViewById(R.id.cuddleBar);
 		cleanBar = (ProgressBar) findViewById(R.id.cleanBar);
 		energyBar = (ProgressBar) findViewById(R.id.energyBar);
 		crayView = (ImageView) findViewById(R.id.crayCray);
 
 		// Sets the color of the progressbar
 		foodBar.getProgressDrawable().setColorFilter(
 				Color.parseColor("#33FF99"), Mode.MULTIPLY);
 		cuddleBar.getProgressDrawable().setColorFilter(
 				Color.parseColor("#FF3366"), Mode.MULTIPLY);
 		cleanBar.getProgressDrawable().setColorFilter(
 				Color.parseColor("#66FFFF"), Mode.MULTIPLY);
 		energyBar.getProgressDrawable().setColorFilter(
 				Color.parseColor("#FFFF66"), Mode.MULTIPLY);
 
 		model = NeedsModel.getInstance();
 
 		// sets the latest values of the progressbars
 		foodBar.setProgress(model.getHungerLevel());
 		cuddleBar.setProgress(model.getCuddleLevel());
 		cleanBar.setProgress(model.getCleanLevel());
 		energyBar.setProgress(model.getEnergyLevel());
 
 		if(t == null){
 			t = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 
 					while (true) {
 						try {
 
 							if(isActive){
 
 								// check if pooImage should be drawn or not
 
 								model.setHungerLevel(model.getHungerLevel() - 1);
 								model.setCleanLevel(model.getCleanLevel() - 1);
 								model.setCuddleLevel(model.getCuddleLevel() - 1);
 								model.setPooLevel(model.getPooLevel() - 1);
 
 								setCrayExpression(ENERGY, model.getEnergyLevel());
 
 								//Check if user should be able to clean CrayCray
 								drawPooImage(model.getPooLevel());
 								cleanButton.setClickable(cleanability);
 
 								//deactivate buttons if CrayCray is sleeping
 								//increase energy level when sleeping
 								if (model.isSleeping()) {
 									fade.setAlpha(0.5F);
 									fade.invalidate();
 									model.setEnergyLevel(model.getEnergyLevel() + 15);
 									activatedButtons(false);
 
 								} else {
 									fade.setAlpha(0F);
 									fade.invalidate();
 									model.setEnergyLevel(model.getEnergyLevel() - 1 );
 									setCrayExpression(HAPPINESS, model.getCuddleLevel());
 									setCrayExpression(HUNGER, model.getHungerLevel());
 									setCrayExpression(CLEANNESS, model.getCleanLevel());
 									
 									activatedButtons(true);
 
 								}
 
 								// If window does not have focus an ill notification is send.
 								// remove 1 from illCount. 
 								// Then checks if the count has reached zero and in that case CrayCray dies.
 								if(model.isIll()){
 									if(!hasWindowFocus()){
 										notifications.sendIllNotification();
 									}
 
 									try{
 										model.setIllCount(model.getIllCount() - 1);
 										model.killWhenIll();
 									} catch(Exception e){
 										if (e instanceof DeadException) {
 											System.out.println("DEAD BY ILLNESS");
 											Message msg = Message.obtain();
 											msg.obj = e;
 											handler.sendMessage(msg);
 											break;
 										}
 									}
 								}
 
 								System.out.println("PRINT IN THREAD:");
 								System.out.println("Hunger" + model.getHungerLevel());
 								System.out.println("Clean" + model.getCleanLevel());
 								System.out.println("Cuddle" + model.getCuddleLevel());
 								System.out.println("Energy" + model.getEnergyLevel());
 
 
 								// if CrayCray is dirty send a dirty-notification
 								if (model.getCleanLevel() < 50) {
 									if (!hasWindowFocus()) {
 										notifications.sendDirtyNotification();
 									}
 								}
 								
 								//if CrayCray is drunk show drunkpicture
 								if(isDrunk){
 									setCrayExpression(DRUNK, 0);
 								}
 								//decrease drunkCount
 								setDrunkCount(drunkCount -1);
 								//when drunkCount is 0 decide what picture to show
 								if(drunkCount == 0){
 									isDrunk = false;
 									model.setEnergyLevel(model.getEnergyLevel() - 1 );
 									setCrayExpression(HAPPINESS, model.getCuddleLevel());
 									setCrayExpression(HUNGER, model.getHungerLevel());
 									setCrayExpression(CLEANNESS, model.getCleanLevel());
 									drunkCount = Constants.MAX_DRUNK_COUNT;
 								}
 								
 
 								handler.sendMessage(handler.obtainMessage());
 								Thread.sleep(800);
 							}
 						} catch (Exception e) {
 							if (e instanceof DeadException) {
 								Message msg = Message.obtain();
 								msg.obj = e;
 								handler.sendMessage(msg);
 								break;
 							}
 						}
 					}
 				}
 			}
 		);
 	}	
 
 
		t.start();
 			// checks if the database exists
 			if (dbA.getValue("Firsttime") == -1) {
 				dbA.addValue("Firsttime", 1);
 				dbA.addValue(DatabaseConstants.HUNGER, model.getHungerLevel());
 				dbA.addValue(DatabaseConstants.CUDDLE, model.getCuddleLevel());
 				dbA.addValue(DatabaseConstants.POO, model.getPooLevel());
 				dbA.addValue(DatabaseConstants.CLEAN, model.getCleanLevel());
 				dbA.addStringValue(DatabaseConstants.TIME,
 						TimeUtil.getCurrentTime());
 			} else {
 				int differenceInSeconds = TimeUtil.compareTime(dbA
 						.getStringValue(DatabaseConstants.TIME));
 				Log.w("Database",
 						differenceInSeconds + ", "
 								+ dbA.getValue(DatabaseConstants.HUNGER));
 				try {
 					model.setHungerLevel(dbA.getValue(DatabaseConstants.HUNGER)
 							+ differenceInSeconds * (-1));
 					model.setCuddleLevel(dbA.getValue(DatabaseConstants.CUDDLE)
 							+ differenceInSeconds * (-3));
 					model.setCleanLevel(dbA.getValue(DatabaseConstants.CLEAN)
 							+ differenceInSeconds * (-2));
 					model.setPooLevel(dbA.getValue(DatabaseConstants.POO));
 				} catch (DeadException e) {
 					if (e instanceof DeadException) {
 						Message msg = Message.obtain();
 						msg.obj = e;
 						handler.sendMessage(msg);
 					}
 				}
 			}
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if(!t.isAlive()){
			t.run();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Updates the database if the application is shut down
 	 */
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		dbA.updateValue(DatabaseConstants.HUNGER, model.getHungerLevel());
 		dbA.updateValue(DatabaseConstants.CUDDLE, model.getCuddleLevel());
 		dbA.updateValue(DatabaseConstants.CLEAN, model.getCleanLevel());
 		dbA.updateValue(DatabaseConstants.POO, model.getPooLevel());
 		dbA.updateStringValue(DatabaseConstants.TIME, TimeUtil.getCurrentTime());
 	}
 
 	/**
 	 * increases hungerlevel by 5
 	 */
 	public synchronized void feed(View view) {
 		try {
 			model.setHungerLevel(model.getHungerLevel() + 5);
 
 		} catch (DeadException e) {
 			// handled elsewhere?
 		}
 		if (model.getHungerLevel() > 50) {
 			setCrayExpression(-1, -1);
 		}
 		handler.sendMessage(handler.obtainMessage());
 	}
 
 	/**
 	 * increases cleanlevel by 10
 	 */
 	public synchronized void clean(View view) {
 		if (cleanability) {
 			model.setCleanLevel(model.getCleanLevel() + 10);
 			if (model.getCleanLevel() > 50) {
 				setCrayExpression(-1, -1);
 			}
 
 			handler.sendMessage(handler.obtainMessage());
 		}
 	}
 
 	/**
 	 * increases cuddlelevel by 7
 	 */
 	public synchronized void cuddle(View view) {
 
 		model.setCuddleLevel(model.getCuddleLevel() + 7);
 		handler.sendMessage(handler.obtainMessage());
 
 	}
 
 	/**
 	 * increases energylevel by 50
 	 */
 	public synchronized void sleep(View view) {
 		model.setSleep(true);
 
 	}
 
 	/**
 	 * removes poo from screen and increses poolevel by 100
 	 * 
 	 * @param view
 	 */
 	public synchronized void removePoo(View view) {
 		if (model.hasPooped()) {
 			model.setPooLevel(100);
 			drawPooImage(model.getPooLevel());
 			cleanability = true;
 			model.setHasPooped(false);
 		}
 		handler.sendMessage(handler.obtainMessage());
 
 	}
 
 	/**
 	 * cures the pet if it is ill
 	 * @param view
 	 */
 	public synchronized void cure(View view) {
 		if (model.isIll()) {
 			cleanability = true;
 			model.setIllness(false);
 			model.setIllCount(30);
 			handler.sendMessage(handler.obtainMessage());
 
 			setCrayExpression(CLEANNESS, model.getCleanLevel());
 			setCrayExpression(HUNGER, model.getHungerLevel());
 			setCrayExpression(HAPPINESS, model.getCuddleLevel());
 
 		}
 	}
 
 	/**
 	 * Called when user clicks to play russian roulette
 	 * @param view
 	 */
 	public void playRussianRoulette(View view){
 		createRussianAlert().show();
 	}
 
 	/**
 	 * Called when user clicks to drink Happy Potion
 	 * @param view
 	 */
 	public void happyPotion(View view){
 		//setDrunkExpression for some period of time
 		isDrunk = true;
 		model.setCuddleLevel(model.getCuddleLevel()+17);
 	}
 	
 	private void setDrunkCount(int count){
 		drunkCount = count;
 	}
 
 	/**
 	 * Displays the instructions-pop up 
 	 */
 	public void howToPlay(View view) {
 		createInstructionsAlert().show();
 
 
 	}
 
 
 	/**
 	 * Check if pooImage should be drawn or not
 	 * 
 	 * @param level
 	 */
 	public synchronized void drawPooImage(int level) {
 		pooImage = (ImageView) findViewById(R.id.pooImage);
 		if (level <= 100 && level > 50) {
 			setPoo(NOPOO);
 			handler.sendMessage(handler.obtainMessage());
 		} else if ((level <= 50 && level >= 20) && (!model.hasPooped())) {
 			setPoo(POO);
 			cleanability = false;
 			model.setHasPooped(true);
 			handler.sendMessage(handler.obtainMessage());
 		} else if (level < 20) {
 			model.setCleanLevel(model.getCleanLevel() - 5);
 		}
 	}
 
 	/**
 	 * set image of poo or an "invisible" picture to visualize removing the
 	 * poopicture
 	 * 
 	 * @param pooOrNot
 	 */
 	public synchronized void setPoo(int pooOrNot) {
 		int image;
 		switch (pooOrNot) {
 
 		case POO:
 			image = R.drawable.poo;
 			pooImage.setImageResource(image);
 			break;
 
 		case NOPOO:
 			image = R.drawable.invisible;
 			pooImage.setImageResource(image);
 			break;
 
 		default:
 			image = R.drawable.invisible;
 			pooImage.setImageResource(image);
 		}
 	}
 
 	/**
 	 * set correct image of craycray based on the different levels.
 	 * 
 	 * @param mode
 	 *            which level to check
 	 * @param level
 	 *            the value of the level
 	 */
 	public synchronized void setCrayExpression(int mode, int level) {
 
 		switch (mode) {
 		case ENERGY:
 			if(level >= 100){
 				model.setSleep(false);
 
 			}else if (level == 0 || model.isSleeping()) {
 				crayView.setImageResource(R.drawable.sleeping_baby);
 				model.setSleep(true);
 			}
 			break;
 
 			// check dirtyLvl
 		case CLEANNESS:
 			if (level >20 && level < 50) {
 				crayView.setImageResource(R.drawable.dirty_baby);
 			}
 			if (level <= 20) {
 				System.out.println("baby sick");
 				crayView.setImageResource(R.drawable.sick_baby);
 				model.setIllness(true);
 			}
 			break;
 
 			// check hungryLvl
 		case HUNGER:
 			if (level == 0) {
 				crayView.setImageResource(R.drawable.dead_baby);
 
 			} else if (model.isIll()) {
 				crayView.setImageResource(R.drawable.sick_baby);
 
 			} else if (level < 50) {
 				System.out.println("inside case 1 (hungry)" + level);
 				crayView.setImageResource(R.drawable.feed_baby);
 
 			}
 			break;
 
 			// check cuddleLvl
 		case HAPPINESS:
 			if (level > 70) {
 				crayView.setImageResource(R.drawable.happy_baby);
 
 			} else if(level < 10){
 				crayView.setImageResource(R.drawable.crying_baby);
 			}else{
 				crayView.setImageResource(R.drawable.regular_baby);
 			}
 			break;
 			
 		case DRUNK:
 			crayView.setImageResource(R.drawable.wasted_baby);
 			break;
 
 		default:
 			System.out.println("inside base-case" + level);
 			crayView.setImageResource(R.drawable.regular_baby);
 		}
 		handler.sendMessage(handler.obtainMessage());
 
 	}	
 
 
 	/**
 	 * Creates a pop-up with a death announcement
 	 */
 	public AlertDialog.Builder createDeathAlert() {
 
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		alertDialog.setTitle("Game Over");
 		alertDialog.setPositiveButton("New Game",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 
 			}
 		});
 		alertDialog.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 			}
 		});
 
 		return alertDialog;
 	}
 
 	/**
 	 * Creates a pop-up with instructions about how to play
 	 */
 	public AlertDialog.Builder createInstructionsAlert(){
 
 		isActive = false;
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		alertDialog.setTitle("How to play");
 		alertDialog.setNeutralButton("Ok",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				isActive = true;
 			}
 		});
 
 		return alertDialog;
 
 	}
 
 	/**
 	 * Creates a pop-up asking if the user really wants to
 	 * play Russian Roulette.
 	 */
 	public AlertDialog.Builder createRussianAlert(){
 
 		isActive = false;
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		alertDialog.setTitle("Russian Roulette");
 		alertDialog.setMessage("Do you really want to play? No turning back...");
 		alertDialog.setPositiveButton("Hell Yeah!",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Intent rusIntent = new Intent(main, RussianActivity.class);
 				startActivityForResult(rusIntent, Constants.RUSSIAN_REQUEST_CODE);
 			}
 		});
 		alertDialog.setNegativeButton("God no!",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				isActive = true;
 			}
 		});
 
 		return alertDialog;
 
 	}
 
 	/**
 	 * Tells the user CrayCray has died, usually by a pop-up. 
 	 * If the the program is not active a notification will
 	 * be sent instead.
 	 */
 	public void announceDeath(DeadException e) {
 		setCrayExpression(HUNGER, 0);
 		if (!hasWindowFocus()) {
 			notifications.sendDeadNotification();
 		} else {
 			String message = e.getDeathCause();
 			createDeathAlert().setMessage(message).show();
 		}
 		model.minAllNeeds();
 	}
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		// Check which request we're responding to
 		if (requestCode == Constants.RUSSIAN_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				Bundle bundle = data.getExtras();
 				boolean result = bundle.getBoolean("key");
 				if(result == Constants.RUSSIAN_LOOSE){
 					Log.w("russian", "result = loose in onactivityres");
 					DeadException e = new DeadException(Constants.RUSSIAN_DEATH);
 					Message msg = Message.obtain();
 					msg.obj = e;
 					handler.sendMessage(msg);
 
 					Log.w("russian", "handler message with dedex sent in onactivityresult");
 				}
 			}
 		}
 		isActive = true;
 	}
 
 	public synchronized void activatedButtons(boolean state){
 			feedButton.setClickable(state);
 			cuddleButton.setClickable(state);
 			cleanButton.setClickable(state);
 			energyButton.setClickable(state);
 			removePooButton.setClickable(state);
 			cureButton.setClickable(state);
 			happypotionButton.setClickable(state);
 			russianButton.setClickable(state);
 	}
 }
 
