 package com.canefaitrien.spacetrader;
 
 import android.app.ActionBar.LayoutParams;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 import com.canefaitrien.spacetrader.models.EncounterType;
 import com.canefaitrien.spacetrader.models.PirateEncounter;
 import com.canefaitrien.spacetrader.models.Planet;
 import com.canefaitrien.spacetrader.models.PoliceEncounter;
 import com.canefaitrien.spacetrader.models.Ship;
 import com.canefaitrien.spacetrader.models.TraderEncounter;
 import com.canefaitrien.spacetrader.utils.MusicManager;
 
 /**
  * This class handles the actions on the Galaxy menu
  * 
  * 
  * @author Daniel Xiao
  * 
  */
 public class GalaxyMapActivity extends RootActivity implements OnTouchListener {
 
 	private String TAG = "Galaxy Activity";
 	private GalaxyView galaxy;
 	private PlanetsView planetsView;
 	// LinearLayout gfl = (LinearLayout) findViewById(R.id.galaxy_view);
 	// //galaxy frame layout
 	/**
 	 * Displays the galaxy canvas and draws it on Creates a listener for screen
 	 * pressing
 	 */
 	private boolean continueMusic;
 
 	protected void onCreate(Bundle savedInstanceState) {
 
 		FrameLayout main = new FrameLayout(this);
 		main.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 		//
 		super.onCreate(savedInstanceState);
 		Log.d("Galaxy", "Created Galaxy");
 		// draw planets
 		galaxy = new GalaxyView(this, SpaceTrader.getController().getUniverse());
 		planetsView = new PlanetsView(this, SpaceTrader.getController()
 				.getUniverse());
 		galaxy.setOnTouchListener(this);
 		setContentView(main);
 		main.setBackgroundResource(R.drawable.starfield_a);
 		main.addView(planetsView);
 		main.addView(galaxy);
 
 	}
 
 	/**
 	 * Creates popup menus for when a planet is clicked on
 	 * 
 	 * @param planet
 	 *            Takes in the planet clicked on.
 	 */
 	public void onCreateDialog(Planet planet) {
 		final Planet p = planet;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("" + planet.getName());
 		Planet location = SpaceTrader.getController().getLocation();
		builder.setMessage("Tech Level: " + location.getStringTechLevel()
				+ "\nSituation: " + location.getStringSituation()
				+ "\nFuel needed to travel: " + location.distance(p) / Ship.MPG
 				+ "/" + SpaceTrader.getController().getShip().getFuel());
 		builder.setPositiveButton("Close",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// do nothing
 					}
 
 				});
 		if (planet != location) {
 			builder.setNegativeButton("Travel",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							try {
 								SpaceTrader.getController().move(p);
 								galaxy.invalidate();
 								Log.d("Galaxy", "traveling to " + p.getName());
 								//
 								listDialog();
 							} catch (Exception e) {
 								Toast.makeText(getApplicationContext(),
 										"" + e.getMessage(), Toast.LENGTH_SHORT)
 										.show();
 								e.printStackTrace();
 							}
 						}
 					});
 		}
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	/**
 	 * Creates a dialog for when an encounter appears
 	 */
 	public void listDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		EncounterType encounter = EncounterType.getEncounterType();
 		builder.setTitle("Encounter: " + encounter.name());
 		String[] strs;
 
 		switch (encounter) {// handle encounters
 		case PIRATE:
 			strs = new String[] { "Attack", "Flee", "Surrender" };
 			final PirateEncounter pirate = new PirateEncounter(
 					SpaceTrader.getController());
 			builder.setItems(strs, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 					case 0:
 						pirate.pirateBattle();
 						break;
 					case 1:
 						Toast.makeText(getApplicationContext(),
 								"Escaped Pirate", Toast.LENGTH_SHORT).show();
 						pirate.pirateFlee();
 						break;
 					case 2:
 						pirate.takeGoods();
 						break;
 					}
 				}
 			});
 			break;
 		case POLICE:
 			strs = new String[] { "Attack", "Bribe 10%", "Comply", "Flee" };
 			final PoliceEncounter police = new PoliceEncounter(
 					SpaceTrader.getController());
 			builder.setItems(strs, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 					case 0:
 						police.policeBattle();
 						break;
 					case 1:
 						police.bribePolice((int) ((float) SpaceTrader
 								.getController().getMoney() * .1));
 						break;
 					case 2:
 						police.checkGoods();
 						break;
 					case 3:
 						Toast.makeText(getApplicationContext(),
 								"Escaped Police", Toast.LENGTH_SHORT).show();
 						police.policeFlee();
 						break;
 					}
 				}
 			});
 			break;
 		case TRADER:
 			strs = new String[] { "Attack", "Trade", "Flee" };
 			final TraderEncounter trader = new TraderEncounter(
 					SpaceTrader.getController());
 			builder.setItems(strs, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 					case 0:
 						trader.traderBattle();
 						break;
 					case 1:
 						break;
 					case 2:
 						Toast.makeText(getApplicationContext(), "Left Trader",
 								Toast.LENGTH_SHORT).show();
 						break;
 					}
 				}
 			});
 			break;
 		case NOTHING:
 			break;
 		}
 		if (encounter != EncounterType.NOTHING) {// no encounter
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 
 	/**
 	 * Checks entire screen when pressed and sees if a planet contains those
 	 * coordinates
 	 * 
 	 * @param takes
 	 *            in a view and an event
 	 * @return returns true when the view is touched
 	 */
 	public boolean onTouch(View v, MotionEvent e) {
 		if (e.getAction() == MotionEvent.ACTION_DOWN) {
 			Log.d("Galaxy",
 					"ActionDown" + (int) e.getRawX() + " " + (int) e.getRawY());
 			for (Planet p : SpaceTrader.getController().getUniverse()) {
 				if (p.isClicked(new Point((int) e.getRawX(), (int) e.getRawY()))) {
 					// if(p!=SpaceTrader.getController().getLocation()){//is
 					// this the planet we're on
 					Log.d("Galaxy", "clicked " + p.getName());
 					// passes the planet clicked on
 					onCreateDialog(p);
 					// }
 				}
 			}
 		}
 		return true;
 	}
 
 	// @Override
 	// public void onBackPressed() {
 	// super.onBackPressed();
 	// continueMusic = true;
 	// }
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.d(TAG, "onPause called.");
 		if (!continueMusic) {
 			MusicManager.pause();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d(TAG, "onResume called.");
 		continueMusic = true;
 		MusicManager.start(this, MusicManager.MUSIC_GAME);
 
 		galaxy.invalidate();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.d(TAG, "onStart called.");
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Log.d(TAG, "onStop called.");
 		// finish();
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		Log.d(TAG, "onRestart called.");
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 }
