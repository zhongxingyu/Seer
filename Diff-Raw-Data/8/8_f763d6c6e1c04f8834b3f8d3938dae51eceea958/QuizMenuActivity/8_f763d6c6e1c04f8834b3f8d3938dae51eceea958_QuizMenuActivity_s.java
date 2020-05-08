 package com.androidbook.btdt.hour6;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.URI;
 import java.nio.CharBuffer;
 import java.util.HashMap;
 import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
 import java.util.zip.CRC32;
 
 import org.xmlrpc.android.XMLRPCClient;
 
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.ProgressDialog;
 import android.content.ComponentCallbacks;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 public class QuizMenuActivity extends QuizActivity {
 
 	protected Object mActionMode;
 	GuidTask downloader;
 	ProgressDialog pleaseWaitDialog;
 	private DatabaseConnector datasource;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ActionBar actionBar = getActionBar();
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			actionBar.setHomeButtonEnabled(true);
 		}
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setDisplayShowTitleEnabled(false);
 
 		// open database need to make sure the context is not gone while assess
 		// database
 		try {
 			datasource = new DatabaseConnector(this.getBaseContext());
 		} catch (SQLException e) {
 
 			throw new Error("Error copying database");
 
 		}
 
 		// Start loading the questions in the background
 		downloader = new GuidTask();
 		downloader.execute("test", "test2");
 
 		// actionBar.setSelectedNavigationItem(2);
 		// ActionBar.OnNavigationListener
 		// actionBar.setListNavigationCallbacks(mSpinnerAdapter,
 		// mNavigationCallback);
 		// setContentView(R.layout.activity_quiz_menu);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		setContentView(R.layout.activity_quiz_menu);
 		// Define the contextual action mode
 		View view = findViewById(R.id.myView2);
 		view.setOnLongClickListener(new View.OnLongClickListener() {
 			// Called when the user long-clicks on someView
 			public boolean onLongClick(View view) {
 				if (mActionMode != null) {
 					return false;
 				}
 
 				// Start the CAB using the ActionMode.Callback defined above
 				mActionMode = QuizMenuActivity.this
 						.startActionMode(mActionModeCallback);
 				view.setSelected(true);
 				return true;
 			}
 		});
 
 		/** Create an array adapter to populate dropdownlist */
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter
 				.createFromResource(this, R.array.action_list,
 						android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		// ArrayAdapter<String> adapter = new
 		// ArrayAdapter<String>(getBaseContext(),
 		// android.R.layout.simple_spinner_dropdown_item, actions);
 
 		/** Enabling dropdown list navigation for the action bar */
 		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		/** Defining Navigation listener */
 		OnNavigationListener navigationListener = new OnNavigationListener() {
 
 			@Override
 			public boolean onNavigationItemSelected(int itemPosition,
 					long itemId) {
 				Toast.makeText(getBaseContext(),
 						"You selected : " + itemPosition, Toast.LENGTH_SHORT)
 						.show();
 				getActionBar().setSelectedNavigationItem(itemPosition);
 				return false;
 			}
 		};
 
 		/**
 		 * Setting dropdown items and item navigation listener for the actionbar
 		 */
 		getActionBar().setListNavigationCallbacks(adapter, navigationListener);
 		getActionBar().setSelectedNavigationItem(2);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Toast.makeText(this, "Menu Item 1 selected", Toast.LENGTH_SHORT)
 					.show();
 			// The animation has ended, transition to the Main Menu screen
 			startActivity(new Intent(QuizMenuActivity.this,
 					QuizHelpActivity.class));
 			QuizMenuActivity.this.finish();
 			break;
 		case R.id.help_settings:
 			Toast.makeText(this, "Menu item 2 selected", Toast.LENGTH_SHORT)
 					.show();
 			break;
 		case android.R.id.home:
 			Intent intent = new Intent(this, QuizSplashActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			break;
 		default:
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void registerComponentCallbacks(ComponentCallbacks callback) {
 		// TODO Auto-generated method stub
 		super.registerComponentCallbacks(callback);
 	}
 
 	@Override
 	public void unregisterComponentCallbacks(ComponentCallbacks callback) {
 		// TODO Auto-generated method stub
 		super.unregisterComponentCallbacks(callback);
 	}
 
 	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
 
 		// Called when the action mode is created; startActionMode() was called
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			// Inflate a menu resource providing context menu items
 			MenuInflater inflater = mode.getMenuInflater();
 			// Assumes that you have "contexual.xml" menu resources
 			inflater.inflate(R.menu.contextual, menu);
 			return true;
 		}
 
 		// Called each time the action mode is shown. Always called after
 		// onCreateActionMode, but
 		// may be called multiple times if the mode is invalidated.
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false; // Return false if nothing is done
 		}
 
 		// Called when the user selects a contextual menu item
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			switch (item.getItemId()) {
 			case R.id.toast:
 				Toast.makeText(QuizMenuActivity.this, "Selected menu",
 						Toast.LENGTH_LONG).show();
 				mode.finish(); // Action picked, so close the CAB
 				return true;
 			default:
 				return false;
 			}
 		}
 
 		// Called when the user exits the action mode
 		public void onDestroyActionMode(ActionMode mode) {
 			datasource.close();
 
 			mActionMode = null;
 		}
 	};
 
 	private class GuidTask extends AsyncTask<Object, String, Boolean> {
 		private static final String DEBUG_TAG = "GuidActivity$QuizTask";
 		int startingNumber;
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			// TODO Auto-generated method stub
 			Log.d(DEBUG_TAG, "onPostExecute -- dialog");
 			pleaseWaitDialog.dismiss();
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			Log.d(DEBUG_TAG, "onPreExecute -- dialog");
 			pleaseWaitDialog = ProgressDialog.show(QuizMenuActivity.this,
 					"VDR Guid", "Downloading VDR Guid data", true, true);
 			pleaseWaitDialog.setOnCancelListener(new OnCancelListener() {
 				public void onCancel(DialogInterface dialog) {
 					Log.d(DEBUG_TAG, "onCancel -- dialog");
 					GuidTask.this.cancel(true);
 				}
 			});
 
 			super.onPreExecute();
 		}
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			// TODO Auto-generated method stub
 			Log.d(DEBUG_TAG, "onProgressUpdate -- dialog " + values);
 			// setProgressPercent(Integer.parseInt((Sting()) values);
 			super.onProgressUpdate(values);
 		}
 
 		@Override
 		protected Boolean doInBackground(Object... params) {
 			// TODO Auto-generated method stub
 
 			MovieMeterPluginSession session = null;
 			boolean result = false;
 			long Ev_ch_key = 0;
 			int Ev_nr = 0;
 			long Ev_time = 0;
 			int Ev_dr = 0;
 			String Ev_tt = "";
 			String Ev_gt = "";
 			String Ev_rft = "";
 			String Ev_rlt = "";
 			String Ev_st = "";
 			long Ev_hsh_key = 0;
 
 			session = new MovieMeterPluginSession();
 			try {
 				datasource.open();
 				// datasource.deleteAllChannels();
 				// datasource.deleteAllEvents();
 				// datasource.deleteAllHash();
 				int type;
 				int toChannel = 14;
 				Boolean endOfSession = false;
 				String data = new String();
 				CRC32 checkSum = new CRC32();
 				String sendSting = new String();
 				// Socket s = new Socket("192.168.2.13", 6419);
 				// OutputStream os = s.getOutputStream();
 				// InputStream is = s.getInputStream();
 				// DataInputStream dis = new DataInputStream(is);
 				// DataOutputStream dos = new DataOutputStream(os);
 				byte[] rl = new byte[] { 13, 10 };
 				byte[] buffer = new byte[250];
 				// delete all channels for now the easy way.
 
 				/*
 				 * //datasource.deleteAllChannels(); for (int channel = 1;
 				 * channel <= toChannel; channel++) { // for all channel that
 				 * need to be collected // for now its the first 30 channels
 				 * 
 				 * 
 				 * long cur_Id = -1; long cur_event_Id = -1; Socket s = new
 				 * Socket("192.168.2.13", 6419);
 				 * 
 				 * OutputStream os = s.getOutputStream(); InputStream is =
 				 * s.getInputStream(); DataInputStream dis = new
 				 * DataInputStream(is); DataOutputStream dos = new
 				 * DataOutputStream(os);
 				 * 
 				 * 
 				 * publishProgress(Integer.toString((int) ((channel / (float)
 				 * toChannel) * 100))); // sendSting = "LSTE " + channel ; //+
 				 * " NOW"; // currently
 				 * 
 				 * sendSting = "LSTR " + channel ; //+ " NOW"; // currently //
 				 * only // the // now // event // data
 				 * dos.write(sendSting.getBytes()); dos.write(rl); // clear data
 				 * cur_Id = -1; Ev_time = -1; Ev_dr = -1; endOfSession = false;
 				 * Log.d(DEBUG_TAG, "TI: channel " + Integer.toString(channel));
 				 * 
 				 * do { try { data = dis.readLine(); data.getBytes(0, 2, buffer,
 				 * 0); type = Integer.parseInt(data.substring(0, 3)); switch
 				 * (type) { case 214: // Help message break; case 215: // EPG
 				 * data record String dataObj[] = data.split(" ", 3);
 				 * 
 				 * if (dataObj[0].contentEquals("215-C")) { // new channel
 				 * record store in database cur_Id =
 				 * datasource.insertChannel(channel, dataObj[2], dataObj[1]);
 				 * break; } else if (dataObj[0].contentEquals("215-E") & cur_Id
 				 * >= 0) { // Event info Ev_ch_key = cur_Id; Ev_nr =
 				 * Integer.parseInt(dataObj[1]); String eventObj[] = dataObj[2]
 				 * .split(" ", 4); Ev_time = Long.parseLong(eventObj[0]); Ev_dr
 				 * = Integer.parseInt(eventObj[1]); Ev_tt = ""; Ev_st = "";
 				 * Ev_gt = ""; Ev_rft = ""; Ev_rlt = ""; Ev_hsh_key = 0;
 				 * 
 				 * } else if (dataObj[0].contentEquals("215-T") & Ev_time > 0 &
 				 * Ev_dr > 0) { // Title info if (dataObj.length < 3) { Ev_tt =
 				 * dataObj[1];
 				 * 
 				 * } else { Ev_tt = dataObj[1] + " " + dataObj[2]; // cat //
 				 * together }
 				 * 
 				 * } else if (dataObj[0].contentEquals("215-S") & Ev_time > 0 &
 				 * Ev_dr > 0) { // Sub Title info Ev_st = dataObj[1]; } else if
 				 * (dataObj[0].contentEquals("215-D") & !Ev_tt.isEmpty() ) { //
 				 * Title info // genre is mostely the first word // regie is
 				 * mostely the first 2 words behind Regie:
 				 * 
 				 * int regie = 0;
 				 * 
 				 * Ev_gt = dataObj[1]; // genre dataObj[1] =
 				 * dataObj[1].replaceAll("Film|film|\\.", "");
 				 * 
 				 * Ev_gt = Character.toUpperCase(dataObj[1].charAt(0)) +
 				 * dataObj[1].substring(1);
 				 * 
 				 * String eventObj[] =
 				 * dataObj[dataObj.length-1].split("[ \\.]"); for ( int i = 0;
 				 * eventObj.length > i; i++ ) { if (
 				 * eventObj[i].equals("Regie:")) { regie = 2; continue; } if (
 				 * regie > 1 ) Ev_rft = eventObj[i]; if ( regie > 0 ) Ev_rlt =
 				 * eventObj[i]; regie--; } } else if
 				 * (dataObj[0].contentEquals("215-e") & !Ev_tt.isEmpty() ) { //
 				 * write event data Ev_hsh_key = datasource.findHashKeyEvent(
 				 * Ev_ch_key, Ev_nr ); if (Ev_hsh_key <=0 ) {
 				 * 
 				 * checkSum.reset(); checkSum.update(Ev_tt.getBytes());
 				 * 
 				 * Cursor c = datasource.getOneHash( checkSum.getValue() ); if (
 				 * c != null ) { if ( c.getCount() < 1) { // hash not found
 				 * HashMap filmInfo = null;
 				 * //session.getMovieDetailsByTitleAndYear(Ev_tt , "");
 				 * 
 				 * if (!Ev_rlt.isEmpty()) { filmInfo =
 				 * session.getMovieByTitleRegieGenre(Ev_tt, Ev_rft, Ev_rlt,
 				 * Ev_gt); if ( filmInfo != null) Log.d(DEBUG_TAG, "NEW FILM " +
 				 * String.valueOf(checkSum.getValue()) ); }
 				 * 
 				 * // session.getMovieByTitle(Ev_tt);
 				 * //////session.getMovieByTitle("Fame"); //Log.d(DEBUG_TAG,
 				 * "NEW HASH " + String.valueOf(checkSum.getValue()) );
 				 * Ev_hsh_key = datasource.insertHash(0, checkSum.getValue()); }
 				 * else { if (c.moveToFirst() ) { Ev_hsh_key =
 				 * c.getLong(c.getColumnIndex(DatabaseOpenHelper.TBL_ID)); } }
 				 * 
 				 * datasource.insertEventNoCheck( Ev_ch_key, Ev_nr, Ev_time,
 				 * Ev_dr, Ev_tt, Ev_st, Ev_gt, Ev_rft + " " + Ev_rlt, Ev_hsh_key
 				 * );
 				 * 
 				 * } } } if (dataObj[0].contentEquals("215")) { // Last event
 				 * data record quit connection endOfSession = true; break; }
 				 * break; case 220: // VDR service ready break; case 221: // VDR
 				 * service closing transmission // channel endOfSession = true;
 				 * break; case 554: // Transaction failed case 550: // Requested
 				 * action not taken case 250: // Requested VDR action okay,
 				 * completed case 354: // Start sending EPG data case 451: //
 				 * Requested action aborted: local error // in processing case
 				 * 502: // Command not implemented case 504: // Command
 				 * parameter not implemented Log.d(DEBUG_TAG, data.toString());
 				 * endOfSession = true; break;
 				 * 
 				 * case 500: // Syntax error, command unrecognized case 501: //
 				 * Syntax error in parameters or arguments
 				 * dos.write(sendSting.getBytes()); dos.write(rl);
 				 * Log.d(DEBUG_TAG, "Try again same command "+ data.toString());
 				 * 
 				 * break; default: Log.d(DEBUG_TAG, "Default case "+
 				 * data.toString()); break; } //Log.d(DEBUG_TAG,
 				 * data.toString());
 				 * 
 				 * } catch (Exception NumberFormatException) { // TODO: handle
 				 * exception Log.d(DEBUG_TAG, data.toString() +
 				 * NumberFormatException.getMessage() ); endOfSession = true; //
 				 * break out of while loop and go no next channel break; }
 				 * 
 				 * } while (!endOfSession); sendSting = "QUIT";
 				 * dos.write(sendSting.getBytes()); dos.write(rl); data =
 				 * dis.readLine(); s.close();
 				 * 
 				 * }
 				 */
				do {
 					// for all recordings in database
 					String recording = new String();
 					boolean endRecordings = false;
 					endOfSession = false;
 					int recNr = 1;
 
 					long cur_Id = -1;
 					long cur_event_Id = -1;
 					Socket s = new Socket("192.168.2.13", 6419);
 
 					OutputStream os = s.getOutputStream();
 					InputStream is = s.getInputStream();
 					DataInputStream dis = new DataInputStream(is);
 					DataOutputStream dos = new DataOutputStream(os);
 
 					sendSting = "LSTR " + recNr++; // all recordings
 					dos.write(sendSting.getBytes());
 					dos.write(rl);
 					// clear data
 					cur_Id = -1;
 					Ev_time = -1;
 					Ev_dr = -1;
 
 					do {
 						try {
 							// recording = dis.readLine();
 							// sendSting = "LSTR " + recNr++; // all recordings
 							// dos.write(sendSting.getBytes());
 							// dos.write(rl);
 							sendSting = "LSTR " + recNr++; // all recordings
 							dos.write(sendSting.getBytes());
 							dos.write(rl);
 							// clear data
 							cur_Id = -1;
 							Ev_time = -1;
 							Ev_dr = -1;
 							endOfSession = false;
 
 							do {
 								data = dis.readLine();
 
 								type = Integer.parseInt(data.substring(0, 3));
 								switch (type) {
 								case 214: // Help message
 									break;
 								case 215: // EPG data record
 									String dataObj[] = data.split(" ", 3);
 
 									if (dataObj[0].contentEquals("215-C")) {
 										// search channel id related to this
 										// service
 
 										cur_Id = datasource
 												.getCannelIdService(dataObj[1]);
 										break;
 									} else if (dataObj[0]
 											.contentEquals("215-E")
 											& cur_Id >= 0) {
 										// Event info
 										Ev_ch_key = cur_Id;
 										Ev_nr = Integer.parseInt(dataObj[1]);
 										String eventObj[] = dataObj[2].split(
 												" ", 4);
 										Ev_time = Long.parseLong(eventObj[0]);
 										Ev_dr = Integer.parseInt(eventObj[1]);
 										Ev_tt = "";
 										Ev_st = "";
 										Ev_gt = "";
 										Ev_rft = "";
 										Ev_rlt = "";
 										Ev_hsh_key = 0;
 
 									} else if (dataObj[0]
 											.contentEquals("215-T")
 											& Ev_time > 0 & Ev_dr > 0) {
 										// Title info
 										if (dataObj.length < 3) {
 											Ev_tt = dataObj[1];
 
 										} else {
 											Ev_tt = dataObj[1] + " "
 													+ dataObj[2]; // cat
 																	// together
 										}
 
 									} else if (dataObj[0]
 											.contentEquals("215-S")
 											& Ev_time > 0 & Ev_dr > 0) {
 										// Sub Title info
 										Ev_st = dataObj[1];
 									} else if (dataObj[0]
 											.contentEquals("215-D")
 											& !Ev_tt.isEmpty()) {
 										// Title info
 										// genre is mostely the first word
 										// regie is mostely the first 2 words
 										// behind Regie:
 
 										int regie = 0;
 
 										Ev_gt = dataObj[1]; // genre
 										dataObj[1] = dataObj[1].replaceAll(
 												"Film|film|\\.", "");
 
 										Ev_gt = Character
 												.toUpperCase(dataObj[1]
 														.charAt(0))
 												+ dataObj[1].substring(1);
 
 										String eventObj[] = dataObj[dataObj.length - 1]
 												.split("[ \\.]");
 										for (int i = 0; eventObj.length > i; i++) {
 											if (eventObj[i].equals("Regie:")) {
 												regie = 2;
 												continue;
 											}
 											if (regie > 1)
 												Ev_rft = eventObj[i];
 											if (regie > 0)
 												Ev_rlt = eventObj[i];
 											regie--;
 										}
 									} else if (dataObj[0]
 											.contentEquals("215")
 											& !Ev_tt.isEmpty()) {
										// write event data
 										Ev_hsh_key = datasource
 												.findHashKeyEvent(Ev_ch_key,
 														Ev_nr);
 										if (Ev_hsh_key <= 0) {
 
 											checkSum.reset();
 											checkSum.update(Ev_tt.getBytes());
 
 											Cursor c = datasource
 													.getOneHash(checkSum
 															.getValue());
 											if (c != null) {
 												if (c.getCount() < 1) {
 													// hash not found
 													HashMap filmInfo = null;
 													// session.getMovieDetailsByTitleAndYear(Ev_tt
 													// , "");
 
 													if (!Ev_rlt.isEmpty()) {
 														filmInfo = session
 																.getMovieByTitleRegieGenre(
 																		Ev_tt,
 																		Ev_rft,
 																		Ev_rlt,
 																		Ev_gt);
 														if (filmInfo != null)
 															Log.d(DEBUG_TAG,
 																	"NEW FILM "
 																			+ String.valueOf(checkSum
 																					.getValue()));
 													}
 
 													// session.getMovieByTitle(Ev_tt);
 													// ////session.getMovieByTitle("Fame");
 													// Log.d(DEBUG_TAG,
 													// "NEW HASH " +
 													// String.valueOf(checkSum.getValue())
 													// );
 													Ev_hsh_key = datasource
 															.insertHash(
 																	0,
 																	checkSum.getValue());
 												} else {
 													if (c.moveToFirst()) {
 														Ev_hsh_key = c
 																.getLong(c
 																		.getColumnIndex(DatabaseOpenHelper.TBL_ID));
 													}
 												}
 
 												datasource.insertEventNoCheck(
 														Ev_ch_key, Ev_nr,
 														Ev_time, Ev_dr, Ev_tt,
 														Ev_st, Ev_gt, Ev_rft
 																+ " " + Ev_rlt,
 														Ev_hsh_key);
 
 											}
 										}
 									}
 									if (dataObj[0].contentEquals("215")) {
 										// Last event data record quit
 										// connection
 										endOfSession = true;
 										break;
 									}
 									break;
 								case 220: // VDR service ready
 									break;
 								case 221: // VDR service closing transmission
 											// channel
 									endOfSession = true;
 									break;
 								case 554: // Transaction failed
 								case 550: // Requested action not taken
 								case 250: // Requested VDR action okay,
 											// completed
 								case 354: // Start sending EPG data
 								case 451: // Requested action aborted: local
 											// error
 											// in processing
 								case 502: // Command not implemented
 								case 504: // Command parameter not implemented
 									Log.d(DEBUG_TAG, data.toString());
 									endOfSession = true;
 									break;
 
 								case 500: // Syntax error, command unrecognized
 								case 501: // Syntax error in parameters or
 											// arguments
 									dos.write(sendSting.getBytes());
 									dos.write(rl);
 									Log.d(DEBUG_TAG, "Try again same command "
 											+ data.toString());
 
 									break;
 								default:
 									Log.d(DEBUG_TAG,
 											"Default case " + data.toString());
 									break;
 								}
 								Log.d(DEBUG_TAG, data.toString());
 							} while (!endOfSession);
 
 						} catch (Exception NumberFormatException) {
 							// TODO: handle exception
 							Log.d(DEBUG_TAG, data.toString()
 									+ NumberFormatException.getMessage());
 							endOfSession = true;
 							// break out of while loop and go no next channel
 							break;
 						}
 
 					} while (!endRecordings);
 					sendSting = "QUIT";
 					dos.write(sendSting.getBytes());
 					dos.write(rl);
 					data = dis.readLine();
 					s.close();
				} while (!endOfSession);
 
 				// sendSting = "QUIT";
 				// dos.write(sendSting.getBytes());
 				// dos.write(rl);
 				// data = dis.readLine();
 				// Log.d(DEBUG_TAG, data.toString());
 				// s.close();
 
 			} catch (Exception e) {
 				Log.e(DEBUG_TAG, "Unexpected failure in collecting event data",
 						e);
 
 			}
 
 			datasource.close();
 
 			return result;
 		}
 
 	}
 
 }
