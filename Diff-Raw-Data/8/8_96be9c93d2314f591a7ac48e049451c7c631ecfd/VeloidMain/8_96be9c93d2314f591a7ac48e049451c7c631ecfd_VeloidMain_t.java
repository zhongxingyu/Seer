 package com.xirgonium.android.veloid.veloid2;
 
 //adb uninstall com.xirgonium.android.veloid
 //adb install /Users/xirgonium/Documents/android/apks/Veloid.apk
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TabHost.TabSpec;
 
 import com.xirgonium.android.config.ConfigurationContext;
 import com.xirgonium.android.manager.CommonStationManager;
 import com.xirgonium.android.manager.VeloStarRennes;
 import com.xirgonium.android.util.Constant;
 import com.xirgonium.android.util.NetworkSkeletonParameter;
 import com.xirgonium.android.util.NetworkSkeletonParameters;
 import com.xirgonium.android.veloid.R;
 import com.xirgonium.android.veloid.config.VeloidPreferences;
 import com.xirgonium.android.veloid.veloid2.favorite.FavoriteListActivity;
 import com.xirgonium.android.veloid.veloid2.map.VeloidMap;
 import com.xirgonium.android.veloid.veloid2.search.SearchForBikesOrSlotsActivity;
 import com.xirgonium.android.veloid.veloid2.timer.ITimerServiceMain2;
 import com.xirgonium.android.veloid.veloid2.timer.Timer;
 import com.xirgonium.android.view.FilterAdapter;
 import com.xirgonium.exception.NoInternetConnection;
 
 public class VeloidMain extends TabActivity {
 
 	ITimerServiceMain2			timerService						= null;
 	VeloidMain					thisInstance						= null;
 
 	TabChanger					tabChanger;
 
 	CommonStationManager		mgr									= null;
 
 	TabSpec						mapTab								= null;
 
 	static ProgressDialog		pd;
 
 	boolean						noPub								= false;
 
 	private static final int	NO_INTERNET_CONNECTION_MSG			= 1;
 	private static final int	DISPLAY_GATHERED_STATIONS_MSG		= 0;
 	private static final int	DISPLAY_UPDATE_STATIONS_MSG			= 2;
 	private static final int	DISPLAY_FIRST_TIME_NETWORK			= 3;
 	//	private static final int	DISPLAY_QUICK_START					= 4;
 	private static final int	DISMISS_PROGRESS_UPDATING_STATION	= 5;
 
 	private Handler				handler								= new Handler() {
 
 																		@Override
 																		public void handleMessage(Message msg) {
 																			switch (msg.what) {
 																			case NO_INTERNET_CONNECTION_MSG:
 																				if (pd != null)
 																					pd.dismiss();
 																				thisInstance.displayNoConnection();
 																				break;
 																			case DISMISS_PROGRESS_UPDATING_STATION:
 																				// System.err.println("Message received");
 																				if (pd != null) {
 																					// System.err.println("pd != null");
 																					pd.dismiss();
 																				}
 																				//																					if (signets.size() != 0) {
 																				//																						lastUpdate = new Date();
 																				//																					} else {
 																				//																						lastUpdate = null;
 																				//																					}
 																				//																					for (Iterator<Station> iterator = signets.iterator(); iterator.hasNext();) {
 																				//																						Station aStation = (Station) iterator.next();
 																				//																						if (aStation.getUpdateStatus() < 0) {
 																				//																							lastUpdate = null;
 																				//																							break;
 																				//																						}
 																				//																					}
 
 																				//																					((TextView) findViewById(R.id.last_update_lbl)).setText(FormatUtility.generateLastUpdateField(lastUpdate,
 																				//																							thisVeloid));
 
 																				//																					refreshMainView();
 																				break;
 																			case DISPLAY_GATHERED_STATIONS_MSG:
 																				// pd.dismiss();
 																				thisInstance.displayTheNumberOfStations();
 																				break;
 																			case DISPLAY_UPDATE_STATIONS_MSG:
 																				// if (pd != null)
 																				// pd.dismiss();
 																				// if (!waitForFirstChoice) {
 																				thisInstance.displayUpdateStationDialog(R.string.main_update_station_list_after_chage);
 
 																				// }
 																				break;
 																			case DISPLAY_FIRST_TIME_NETWORK:
 																				thisInstance.selectNetworkForTheFirstTime();
 																				break;
 																			//																				case DISPLAY_QUICK_START:
 																			// if (firstLaunch)
 																			//thisInstance.displayQuickstart();
 																			//																					break;
 
 																			}
 
 																		}
 																	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.amain_tabs);
 
 		//--- remove ads on small screens
		/*if (isSmallScreen() || noPub) {
 			findViewById(R.id.ad).setVisibility(View.GONE);
		}*/
 
 		// --- Restore configuration
 		ConfigurationContext.restoreConfig(this);
 		NetworkSkeletonParameters.init(this);
 
 		thisInstance = this;
 
 		tabChanger = new TabChanger();
 		tabChanger.setTabInstance(this);
 
 		mgr = ConfigurationContext.getCurrentStationManager(this);
 
 		actionIfOnVeloStar();
 		
 		// -- Create the tabs
 		initTabs(new Intent(this, VeloidMap.class));
 
 		if (getIntent().getStringExtra("tab") != null) {
 			getTabHost().setCurrentTabByTag(getIntent().getStringExtra("tab"));
 		} else if (!"signets".equals(ConfigurationContext.getTabOnStartup())) {
 			getTabHost().setCurrentTabByTag(ConfigurationContext.getTabOnStartup());
 		}
 
 	}
 
 	public void initTabs(Intent mapIntent) {
 		final TabHost tabHost = getTabHost();
 		tabHost.clearAllTabs();
 
 		tabHost.addTab(tabHost.newTabSpec("signets").setIndicator(getString(R.string.tab_title_fav), getResources().getDrawable(R.drawable.img_star)).setContent(
 				new Intent(this, FavoriteListActivity.class)));
 
 		tabHost.addTab(tabHost.newTabSpec("search").setIndicator(getString(R.string.tab_title_search), getResources().getDrawable(R.drawable.img_search)).setContent(
 				new Intent(this, SearchForBikesOrSlotsActivity.class)));
 
 		mapTab = tabHost.newTabSpec("map").setIndicator(getString(R.string.tab_title_map), getResources().getDrawable(R.drawable.img_map)).setContent(mapIntent);
 		tabHost.addTab(mapTab);
 
 		tabHost.addTab(tabHost.newTabSpec("timer").setIndicator(getString(R.string.tab_title_timer), getResources().getDrawable(R.drawable.img_timer)).setContent(new Intent(this, Timer.class)));
 
 		tabHost.addTab(tabHost.newTabSpec("config").setIndicator(getString(R.string.config), getResources().getDrawable(R.drawable.tools)).setContent(new Intent(this, VeloidPreferences.class)));
 
 	}
 
 	// public void changeMapContent(Intent i){
 	// mapTab.setContent(arg0)
 	// }
 
 	@Override
 	protected void onResume() {
 
 		if (!ConfigurationContext.isAcceptedEULA()) {
 			showEULA();
 		}
 
 		// else if (!waitForFirstChoice && !mgr.isThereAtLeastOneStationInDBForNetwork()) {
 		// // --- No -> Propose update
 		// handler.sendEmptyMessage(DISPLAY_UPDATE_STATIONS_MSG);
 		// }
 
 		// updateFavorite(false);
 		// TODO or special network action
 		// setSpecificActionController();
 
 		super.onResume();
 		registerReceiver(tabChanger, TabChanger.getTabChangingIntentFilter());
 		
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(tabChanger);
 	}
 
 	public void startTimer(int minutes) {
 		try {
 
 			if (!ConfigurationContext.isTimerServiceRunning()) {
 				Intent i = new Intent(ITimerServiceMain2.class.getName());
 				i.putExtra(Constant.TIMER_MINUTE_BUNDLE_KEY, minutes);
 				startService(i);
 			}
 
 			Thread.sleep(500);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void showEULA() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setIcon(R.drawable.icon_small);
 		builder.setTitle(getString(R.string.eula_title));
 		builder.setMessage(getString(R.string.eula_content));
 		builder.setPositiveButton(R.string.eula_btn_accept, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dl, int which) {
 				if (which == AlertDialog.BUTTON1) {
 					ConfigurationContext.setAcceptedEULA(true);
 					ConfigurationContext.saveConfig(thisInstance);
 					// firstLaunch = true;
 					handler.sendEmptyMessage(DISPLAY_FIRST_TIME_NETWORK);
 				}
 			}
 		});
 		builder.setNegativeButton(R.string.eula_btn_decline, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dl, int which) {
 				thisInstance.finish();
 			}
 		});
 
 		builder.create().show();
 	}
 
 	private void selectNetworkForTheFirstTime() {
 		final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View dialogSetBNetworkView = (View) vi.inflate(R.layout.dialog_select_network, null);
 
 		final Spinner spnBNetwork = (Spinner) dialogSetBNetworkView.findViewById(R.id.select_network_spinner);
 		FilterAdapter admab = new FilterAdapter((Context) this, NetworkSkeletonParameters.getDescriptionArray());
 		admab.setTextSize(20);
 		spnBNetwork.setAdapter(admab);
 
 		// spnBNetwork.setSelection(mgr.getMenuSelectionIndex());
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setIcon(R.drawable.icon_small);
 		builder.setTitle(getString(R.string.menu_network));
 		builder.setView(dialogSetBNetworkView);
 		builder.setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dl, int which) {
 				int selected = spnBNetwork.getSelectedItemPosition();
 
 				NetworkSkeletonParameter selectedNetwork = NetworkSkeletonParameters.getNetworks().elementAt(selected);
 				ConfigurationContext.setNetwork(selectedNetwork.getId());
 
 				ConfigurationContext.saveConfig(thisInstance);
 				mgr = ConfigurationContext.getCurrentStationManager(thisInstance);
 				
 				actionIfOnVeloStar();
 				// refreshMainView();
 				// initMainView();
 				// waitForFirstChoice = false;
 
 				handler.sendEmptyMessage(DISPLAY_UPDATE_STATIONS_MSG);
 				dl.dismiss();
 			}
 		});
 
 		builder.setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dl, int which) {
 				dl.dismiss();
 				//handler.sendEmptyMessage(DISPLAY_QUICK_START);
 			}
 		});
 
 		builder.create().show();
 
 	}
 
 	public void displayQuickstart() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setIcon(R.drawable.icon_small);
 		builder.setTitle(R.string.quick_start_title);
 		builder.setMessage(R.string.quick_start_message);
 		builder.setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dl, int which) {
 				if (which == AlertDialog.BUTTON1) {
 					try {
 						Uri uri = Uri.parse(getString(R.string.quick_start_url));
 						startActivity(new Intent(Intent.ACTION_VIEW, uri));
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		builder.setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dl, int which) {
 				if (which == AlertDialog.BUTTON2) {
 					dl.dismiss();
 				}
 			}
 		});
 
 		// reset the first launch to avoid sde effect
 		// firstLaunch = false;
 		builder.create().show();
 
 	}
 
 	public synchronized void displayUpdateStationDialog(int idTextDialog) {
 
 		// if (updateStationsDisplayed) {
 		// // to avoid two displays... that makes issues
 		// return;
 		// }
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setIcon(R.drawable.icon_small);
 		builder.setTitle(getString(R.string.menu_update_station_list));
 		builder.setMessage(getString(idTextDialog));
 		builder.setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dl, int which) {
 				if (which == AlertDialog.BUTTON1) {
 
 					dl.dismiss();
 					// updateStationsDisplayed = false;
 					pd = ProgressDialog.show(thisInstance, getString(R.string.main_update_progress_dialog_title), getString(R.string.main_update_progress_dialog_station_updating), true, false);
 
 					Thread thread = new Thread() {
 
 						public void run() {
 							try {
 								mgr.updateStationListDynamicaly();
 								handler.sendEmptyMessage(DISMISS_PROGRESS_UPDATING_STATION);
 								handler.sendEmptyMessage(DISPLAY_GATHERED_STATIONS_MSG);
 								// dialogValidateUpdate.dismiss();
 							} catch (NoInternetConnection e) {
 								handler.sendEmptyMessage(NO_INTERNET_CONNECTION_MSG);
 							}
 
 						}
 					};
 
 					thread.start();
 
 				}
 			}
 		});
 		builder.setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dl, int which) {
 				if (which == AlertDialog.BUTTON2) {
 					dl.dismiss();
 					// updateStationsDisplayed = false;
 					//handler.sendEmptyMessage(DISPLAY_QUICK_START);
 				}
 			}
 		});
 		// updateStationsDisplayed = true;
 		builder.create().show();
 
 	}
 
 	public void displayNoConnection() {
 		new AlertDialog.Builder(this).setMessage(this.getString(R.string.error_no_internet_connection)).setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int whichButton) {
 				dialog.dismiss();
 			}
 		}).setIcon(R.drawable.warning).create().show();
 	}
 
 	public void displayTheNumberOfStations() {
 
 		// if (dialogValidateUpdate.isShowing()) {
 		// dialogValidateUpdate.dismiss();
 		// }
 
 		StringBuffer buf = new StringBuffer();
 
 		buf.append(mgr.getNbStationsInDB());
 		buf.append(" ");
 		buf.append(getString(R.string.main_nb_stations_grabbed_for_network));
 		buf.append(" ");
 		buf.append(mgr.getCommonName());
 
 		new AlertDialog.Builder(this).setMessage(buf.toString()).setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int whichButton) {
 				dialog.dismiss();
 
 				//handler.sendEmptyMessage(DISPLAY_QUICK_START);
 			}
 		}).create().show();
 	}
 
 	public boolean isSmallScreen() {
 		final Window win = getWindow();
 		final int screenHeight = win.getWindowManager().getDefaultDisplay().getHeight();
 		final int screenWidth = win.getWindowManager().getDefaultDisplay().getWidth();
 
 		if ((screenHeight == 320 && screenWidth == 240) || (screenHeight == 240 && screenWidth == 320)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void actionIfOnVeloStar() {
 		try {
 			if (ConfigurationContext.getCurrentStationManager(this) instanceof VeloStarRennes) {
 				//--- remove ads on small screens
				/*findViewById(R.id.ad).setVisibility(View.GONE);*/
 
 				TextView tv = (TextView) findViewById(R.id.legal);
 				tv.setText(R.string.data_provided_by_velostar);
 				tv.setGravity(Gravity.RIGHT);
 				tv.setVisibility(View.VISIBLE);
 
 				if(isSmallScreen()){
 					tv.setTextSize(5);
 				}
 			}
 		} catch (Exception e) {
 
 		}
 	}
 
 }
