 package yonsei.highfive.library;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import yonsei.highfive.R;
 import yonsei.highfive.game.CardReverseGameActivity;
 import yonsei.highfive.junction.JunctionAsyncTask;
 import yonsei.highfive.library.circulation.SearchBookActivity;
 import yonsei.highfive.library.seat.SearchSeatActivity;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.RadioButton;
 import android.widget.Toast;
 import edu.stanford.junction.api.activity.JunctionActor;
 import edu.stanford.junction.api.messaging.MessageHeader;
 
 public class LibrarySystemActivity extends Activity {
 	/** Called when the activity is first created. */
 
 	// NFC ߿ Activity ±׸ Share     ˴ϴ.
 	// private Nfc mNfc;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		final SharedPreferences pref = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		if (!pref.getBoolean("certification", false)) {
 			AlertDialog.Builder builder = new Builder(this);
 			builder.setCancelable(true)
 					.setMessage("л Ǿ ʽϴ. մ԰ αϽðڽϱ?")
 					.setPositiveButton("",
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 									Intent setintent = new Intent(LibrarySystemActivity.this,Settings.class);
 									pref.edit().putString("id", "guest").commit();
 									pref.edit().putString("pw", "guest").commit();
									TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
									String IMEI = tm.getDeviceId();
 									
 									JSONObject message = new JSONObject();
 									
 									try {
 										message.put("service", "certification");
 										message.put("id","guest");
 										message.put("pw","guest");
										message.put("IMEI", IMEI);
 									} catch (JSONException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 									
 									AsyncTask<JSONObject, Void, Void> mJunctionBindingAsyncTask = new JunctionAsyncTask(LibrarySystemActivity.this, actor, "db", "л Դϴ.");
 									
 									mJunctionBindingAsyncTask.execute(message); // AsyncTask Thread 
 									
 								}
 							})
 							.setNegativeButton("ƴϿ", new DialogInterface.OnClickListener() {
 								
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									// TODO Auto-generated method stub
 									
 								}
 							})
 							.create().show();
 		}
 		
 		
 		
 		/**
 		 * NFCڵ鷯  Intent ޾  ȿ ִ URI Ͽ ش service ´ Activity ϴ
 		 * Intent . ⼭  service ʿ ߰ parameters Extra ̿  .
 		 */
 		if (getIntent().getData() != null
 				/*&& getIntent().getData().toString()
 						.startsWith("http://mobilesw.yonsei.ac.kr")*/) {
 			Uri data = getIntent().getData();
 			String service = data.getQueryParameter("service");
 			if(service==null){
 				finish();
 			}
 			if (service.equals("circulation")) {
 				String bookid = data.getQueryParameter("bookid");
 				Intent intent = new Intent(
 						this,
 						yonsei.highfive.library.circulation.CirculationActivity.class);
 
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 				Bundle intent_data = new Bundle();
 				intent_data.putString("bookid", bookid);
 				intent.putExtras(intent_data);
 				startActivity(intent);
 			} else if (service.equals("seat")) {
 				String SeatID = data.getQueryParameter("SeatID");
 				Intent intent = new Intent(this,
 						yonsei.highfive.library.seat.SeatActivity.class);
 
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 				Bundle intent_data = new Bundle();
 				intent_data.putString("SeatID", SeatID);
 				intent.putExtras(intent_data);
 				startActivity(intent);
 			}else if(service.equals("gateway")){
 		    	String UserID = data.getQueryParameter("UserID");
 		    	Intent intent = new Intent(this, yonsei.highfive.library.gate.GatewayActivity.class);
 
 		    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 		    	Bundle intent_data = new Bundle();
 		    	intent_data.putString("UserID", UserID);
 		    	intent.putExtras(intent_data);
 		    	startActivity(intent);
 		    } else if (service.equals("slideshow")) {
 				String SessionID = data.getQueryParameter("SessionID");
 				Intent intent = new Intent(this, yonsei.highfive.slideshow.SlideShowActivity.class);
 
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 				Bundle intent_data = new Bundle();
 				intent_data.putString("SessionID", SessionID);
 				intent.putExtras(intent_data);
 				startActivity(intent);
 			}
 			/*
 			 * Ƽ̵ ڷ  ó
 			 */
 			else if (service.equals("media_circulation")) {
 				String mediaid = data.getQueryParameter("mediaid");
 				Intent intent = new Intent(
 						this,
 						yonsei.highfive.library.multimedia.MediaCirculationActivity.class);
 
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 				Bundle intent_data = new Bundle();
 				intent_data.putString("mediaid", mediaid);
 				intent.putExtras(intent_data);
 				startActivity(intent);
 			}
 			else if (service.equals("cardgame")){
 				Intent intent = new Intent(this, yonsei.highfive.game.CardReverseGameActivity.class);
 
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 				startActivity(intent);
 			}
 			else if (service.equals("player")){
 				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobilesw.yonsei.ac.kr/player"));
 
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 				startActivity(intent);
 			}
 			else if (service.equals("showviewer")){
 				String SessionID = data.getQueryParameter("SessionID");
 				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobilesw.yonsei.ac.kr/slideshow?jxsessionid="+SessionID));
 
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				
 				startActivity(intent);
 			}
 			/* Ÿ ó */
 			finish();
 
 		}
 
 
 		/*
 		 *  ̿ ޴
 		 */
 		int[] image = {R.drawable.book, R.drawable.seat,  R.drawable.game, R.drawable.setting, R.drawable.help};
 		
 		GalleryForOneFling gallery = (GalleryForOneFling)findViewById(R.id.gallerymenu);
 		GalleryMenuAdapter gm = new GalleryMenuAdapter(this, R.layout.gallerymenu, image);
 		gallery.setAdapter(gm);
 		gallery.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView parent, View v, int pos, long id) {
 				// TODO Auto-generated method stub
 				switch(pos){
 				case 0:
 					Intent intent = new Intent(LibrarySystemActivity.this, SearchBookActivity.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 					startActivity(intent);
 					break;
 				case 1:
 					intent = new Intent(LibrarySystemActivity.this, SearchSeatActivity.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 					startActivity(intent);
 					break;
 				case 2:
 					intent = new Intent(LibrarySystemActivity.this, CardReverseGameActivity.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 					startActivity(intent);
 					break;
 				case 3:
 					intent = new Intent(LibrarySystemActivity.this, Settings.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 					startActivity(intent);
 					break;
 				case 4:
 					intent = new Intent(LibrarySystemActivity.this, HelpActivity.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 					startActivity(intent);
 					break;
 				case 5:
 					break;
 				}
 				
 				
 			}
 		});
 	}
 
 	/**
 	 * ޴ư   ޴ 
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 
 		return true;
 
 	}
 
 	/**
 	 * ޴    â ̵ϴ Intent ߻
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case R.id.settings:
 			startActivity(new Intent(this, Settings.class));
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		/*
 		 *  UI  Ÿ ư
 		 */
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		RadioButton r_inlibrary = (RadioButton)findViewById(R.id.radio_inlibrary);
 		RadioButton r_certification = (RadioButton)findViewById(R.id.radio_certification);
 		r_inlibrary.setChecked(pref.getBoolean("inlibrary", false));
 		r_certification.setChecked(pref.getBoolean("certification", false));
 
 	}
 
 	private UserJunction actor = new UserJunction();
 
 	/**
 	 * custom Junction Actor Ŭ  ø̼ǿ Actor Role user ( Java Director
 	 * Role director ) onMessageReceived ڵ鷯 .
 	 * 
 	 * @author Lee
 	 * @issue  Ŭ ܺο ٽ Ͽ  ϵ .
 	 */
 	private class UserJunction extends JunctionActor {
 		public UserJunction() {
 			super("user");
 		}
 
 		public UserJunction(String role) {
 			super(role);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public void onMessageReceived(MessageHeader header, JSONObject message) {
 			// TODO Auto-generated method stub
 			if (message.has("accept")) {
 				try {
 					if (message.getString("accept").equals("true")) {
 						SharedPreferences pref = PreferenceManager
 								.getDefaultSharedPreferences(LibrarySystemActivity.this);
 						pref.edit().putBoolean("certification", true).commit();
 						synchronized (actor) {
 							actor.notify();
 							actor.leave();
 						}
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								// TODO Auto-generated method stub
 								Toast.makeText(LibrarySystemActivity.this, "л Ͽϴ.",
 										Toast.LENGTH_SHORT).show();
 							}
 						});
 						LibrarySystemActivity.this.finish();
 					} else if (message.getString("accept").equals("false")) {
 						SharedPreferences pref = PreferenceManager
 								.getDefaultSharedPreferences(LibrarySystemActivity.this);
 						pref.edit().putBoolean("certification", false).commit();
 						synchronized (actor) {
 							actor.notify();
 							actor.leave();
 						}
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								// TODO Auto-generated method stub
 								Toast.makeText(LibrarySystemActivity.this, "л Ͽϴ.",
 										Toast.LENGTH_SHORT).show();
 							}
 						});
 						LibrarySystemActivity.this.finish();
 					}
 					if (message.getString("inlibrary").equals("true")) {
 						SharedPreferences pref = PreferenceManager
 								.getDefaultSharedPreferences(LibrarySystemActivity.this);
 						pref.edit().putBoolean("inlibrary", true).commit();
 					} else if (message.getString("inlibrary").equals("false")) {
 						SharedPreferences pref = PreferenceManager
 								.getDefaultSharedPreferences(LibrarySystemActivity.this);
 						pref.edit().putBoolean("inlibrary", false).commit();
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
