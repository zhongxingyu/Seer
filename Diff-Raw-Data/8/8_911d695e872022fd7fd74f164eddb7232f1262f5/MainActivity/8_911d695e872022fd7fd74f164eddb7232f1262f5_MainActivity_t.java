 package se.chalmers.dat255.craycray;
 
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
 
 import se.chalmers.dat255.craycray.database.DatabaseAdapter;
 import se.chalmers.dat255.craycray.database.DatabaseConstants;
 import se.chalmers.dat255.craycray.model.DeadException;
 import se.chalmers.dat255.craycray.model.NeedsModel;
 import se.chalmers.dat255.craycray.notifications.NotificationSender;
 import se.chalmers.dat255.craycray.util.TimeUtil;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.graphics.Color;
 import android.graphics.PorterDuff.Mode;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 public class MainActivity extends Activity {
 
 	// The buttons of the application
 	private ImageButton feedButton;
 	private ImageButton cuddleButton;
 	private ImageButton cleanButton;
 	private ImageButton energyButton;
 	private ImageButton removePooButton;
 	private ImageButton cureButton;
 
 	// The bars of the application
 	private ProgressBar foodBar;
 	private ProgressBar cuddleBar;
 	private ProgressBar cleanBar;
 	private ProgressBar energyBar;
 	private ImageView crayView;
 	private ImageView pooImage;
 
 	private NeedsModel model;
 	private Thread t;
 
	private final int HUNGER = 5;
 	private final int CLEANNESS = 2;
 	private final int HAPPINESS = 3;
 	private final int ENERGY = 4;
 
 	private final int POO = 1;
 	private final int NOPOO = 2;
 
 	private boolean cleanability = true;
 
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
 			}
 
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 		model = NeedsModel.getInstance();
 		dbA = new DatabaseAdapter(getBaseContext());
 
 		// Button - variables set to xml ID
 		feedButton = (ImageButton) findViewById(R.id.feedButton);
 		cleanButton = (ImageButton) findViewById(R.id.cleanButton);
 		cuddleButton = (ImageButton) findViewById(R.id.cuddleButton);
 		energyButton = (ImageButton) findViewById(R.id.energyButton);
 		removePooButton = (ImageButton) findViewById(R.id.removePooButton);
 		cureButton = (ImageButton) findViewById(R.id.cureButton);
 
 		// Sets correct image to the buttons
 		feedButton.setImageResource(R.drawable.button_food);
 		cleanButton.setImageResource(R.drawable.button_clean);
 		cuddleButton.setImageResource(R.drawable.button_happiness);
 		energyButton.setImageResource(R.drawable.button_energy);
 		removePooButton.setImageResource(R.drawable.button_poo);
 		cureButton.setImageResource(R.drawable.button_cure);
 
 		// Bar - variables set to xml ID
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
 
 		// sets the latest values of the progressbars
 		foodBar.setProgress(model.getHungerLevel());
 		cuddleBar.setProgress(model.getCuddleLevel());
 		cleanBar.setProgress(model.getCleanLevel());
 		energyBar.setProgress(model.getEnergyLevel());
 
 		t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				while (true) {
 					try {
 						
 						handler.sendMessage(handler.obtainMessage());
 						
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
 							
 							model.setEnergyLevel(model.getEnergyLevel() + 10);
 							activatedButtons(false);
 						
 						} else {
 							
 							model.setEnergyLevel(model.getEnergyLevel() - 1);
 							
 							setCrayExpression(HAPPINESS, model.getCuddleLevel());
 							setCrayExpression(HUNGER, model.getHungerLevel());
 							setCrayExpression(CLEANNESS, model.getCleanLevel());
 
 							activatedButtons(true);
 
 						}
 
 
 						System.out.println("PRINT IN THREAD:");
 						System.out.println("Hunger" + model.getHungerLevel());
 						System.out.println("Clean" + model.getCleanLevel());
 						System.out.println("Cuddle" + model.getCuddleLevel());
 						System.out.println("Energy" + model.getEnergyLevel());
 
 						// if CrayCray is sick send an ill-notification
 						// if(model.isIll()){
 						// if(!hasWindowFocus()){
 						// notifications.sendIllNotification();
 						// }
 						// }
 
 						// if CrayCray is dirty send a dirty-notification
 						if (model.getCleanLevel() < 50) {
 							if (!hasWindowFocus()) {
 								notifications.sendDirtyNotification();
 							}
 						}
 
 						Thread.sleep(2000);
 
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
 		});
 
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
 					announceDeath(e);
 //					Message msg = Message.obtain();
 //					msg.obj = e;
 //					handler.sendMessage(msg);
 				}
 			}
 
 		}
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		t.start();
 
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
 //			setCrayExpression(-1, -1);
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
 //				setCrayExpression(-1, -1);
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
 
 	public synchronized void cure(View view) {
 		if (model.isIll()) {
 			cleanability = true;
 			model.setIllness(false);
 			handler.sendMessage(handler.obtainMessage());
 
 //			setCrayExpression(CLEANNESS, model.getCleanLevel());
 //			setCrayExpression(HUNGER, model.getHungerLevel());
 //			setCrayExpression(HAPPINESS, model.getCuddleLevel());
 
 		}
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
 
 		//Check if CrayCray should sleep of not
 		case ENERGY:
 			if (level >= 100) {
 				model.setSleep(false);
 
 			} else if (level == 0 || model.isSleeping()) {
 				
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
 				crayView.setImageResource(R.drawable.sick_baby);
 			}
			break;
 		// check hungryLvl
 		case HUNGER:
 			if (level == 0) {
 				crayView.setImageResource(R.drawable.dead_baby);
 				
 			} else if (level < 50) {
 				System.out.println("inside case 1 (hungry)" + level);
 				crayView.setImageResource(R.drawable.feed_baby);
 
 			}
 			break;
 		// check hungryLvl
 		case HAPPINESS:
 			if (level > 70) {
 				crayView.setImageResource(R.drawable.happy_baby);
 			}
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
 	 * Checks if the window is in focus, if the window is in focus a pop-up with
 	 * a death announcement shows up if the window is not in focus a
 	 * notification with a death announcement shows up
 	 */
 	public void announceDeath(DeadException e) {
 //		setCrayExpression(2, 0);
 		if (!hasWindowFocus()) {
 			notifications.sendDeadNotification();
 		} else {
 			String message = e.getDeathCause();
 			createDeathAlert().setMessage(message).show();
 		}
 	}
 	
 	public synchronized void activatedButtons(boolean state){
 		if(state){
 			feedButton.setClickable(true);
 			cuddleButton.setClickable(true);
 			cleanButton.setClickable(true);
 			energyButton.setClickable(true);
 			removePooButton.setClickable(true);
 			cureButton.setClickable(true);
 		}else{
 			feedButton.setClickable(false);
 			cuddleButton.setClickable(false);
 			cleanButton.setClickable(false);
 			energyButton.setClickable(false);
 			removePooButton.setClickable(false);
 			cureButton.setClickable(false);
 		}
 	}
 
 }
