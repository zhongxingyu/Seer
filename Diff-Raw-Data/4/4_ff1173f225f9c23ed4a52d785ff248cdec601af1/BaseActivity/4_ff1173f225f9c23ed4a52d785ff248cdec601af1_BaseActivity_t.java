 package com.andrewquitmeyer.MarkYourTerritory;
 
 import com.andrewquitmeyer.MarkYourTerritory.MyLocation.LocationResult;
 
 import fi.foyt.foursquare.api.FoursquareApi;
 import fi.foyt.foursquare.api.FoursquareApiException;
 import fi.foyt.foursquare.api.Result;
 import fi.foyt.foursquare.api.entities.CompactVenue;
 import fi.foyt.foursquare.api.entities.VenuesSearchResult;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.SoundPool;
 import android.media.ToneGenerator;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import net.londatiga.fsq.FoursquareApp;
 import net.londatiga.fsq.FsqVenue;
 import net.londatiga.fsq.NearbyAdapter;
 import net.londatiga.fsq.FoursquareApp.FsqAuthListener;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
 
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.sql.PreparedStatement;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 
 public class BaseActivity extends DemoKitActivity {
 
 	private InputController mInputController;
 	WebView webview;
 	MyLocation myLocation;
 	Button nearbyBtn;
 
 	ProgressBar mProgressbar;
 
 	/*
 	 * For Sound
 	 */
 	
 	public static final int TONE_ID = 1;
 	SoundPool soundPool;
 	HashMap<Integer, Integer> soundPoolMap;
 	int currentstreamID=-1;
 
 	
 	
 	
 	
     MediaPlayer mediaPlayer;
 
 //	PlaySound tone;
 	ToneGenerator tonegen;
 	 private final int duration = 1; // seconds
 	    private final int sampleRate = 8000;
 	    private final int numSamples = duration * sampleRate;
 	    private final double sample[] = new double[numSamples];
 	    private final double freqOfTone = 440; // hz
 
 	    private final byte generatedSnd[] = new byte[2 * numSamples];
 
 	    Handler soundhandler = new Handler();
 	    
 	    final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                 sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 AudioFormat.ENCODING_PCM_16BIT, numSamples,
                 AudioTrack.MODE_STATIC);
         AudioTrack otheraudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                 sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 AudioFormat.ENCODING_PCM_16BIT, numSamples,
                 AudioTrack.MODE_STATIC);
 
 	    MediaPlayer mp = new MediaPlayer();
 	  
 	
 	
 	
 	private FoursquareApp mFsqApp;
 	private ListView mListView;
 	private NearbyAdapter mAdapter;
 	private ArrayList<FsqVenue> mNearbyList;
 	private ProgressDialog mProgress;
 
 	
 	private final DecimalFormat mcountDownFormatter = new DecimalFormat("##.##");
	public static final String CLIENT_ID = "INSERT YOUR OWN ID";
	public static final String CLIENT_SECRET = "INSERT YOUR OWN SECRET!";
 	public static String accessToken;
 	TextView tvLatitude;
 	TextView tvLongitude;
 	TextView tvStatus;
 	FrameLayout moistureFrame;
 	Drawable overlay;
 	String currentVenue = "";
 	String currentVenueID = "";
 	int currentmoisturelevel;
 	double avemoisturelevel;
 	int rating=0;
 	String alpharating="";
 	String checkinmessage="";
 	CountDownTimer peetimer;
 	//Prepared means that values have been set and updated
 	boolean locationpreparedtoCheckin=false;
 	//ready means that we have peed and all values are set to actually send the message to check in
 	boolean readytoCheckin=false;
 
 	int maxedmoisturelevel=0;
 	int moisturemaxcounter=0;
 
 	public BaseActivity() {
 		super();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (mAccessory != null) {
 			showControls();
 		} else {
 			hideControls();
 		}
 		
 		/*
 		 * Audio Stuff
 		 */
 		initSounds();
 //		playSound(TONE_ID);
 //		soundPool.pause(0);
 //		tonegen= new ToneGenerator(0x80000000, 100);
 //		tone = new PlaySound();
 //        audioTrack.write(generatedSnd, 0, generatedSnd.length);
 //        genTone();
 //        playtone();
 //		mediaPlayer = MediaPlayer.create(BaseActivity.this, R.raw.twentytone);
 //        
 //        mediaPlayer.setLooping(true);
 //        mediaPlayer.setVolume(100, 100);
 //        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
 //			
 //			public void onPrepared(MediaPlayer mp) {
 //				// TODO Auto-generated method stub
 //				mediaPlayer.start();
 //				Log.e("MYT", "PLAYING SOUND");
 //				
 //			}
 //		});
 //        mediaPlayer.start(); // no need to call prepare(); create() does that for you
         
 
 //        
         
 
 		
 		setContentView(R.layout.maintwo);
 		mProgressbar= (ProgressBar) findViewById(R.id.progressBar1);
 
 		webview = (WebView) findViewById(R.id.webview);
 		nodeviceview = (View) findViewById(R.id.noDevice);
 
 //		final TextView nameTv = (TextView) findViewById(R.id.titleText);
 		tvLatitude = (TextView) findViewById(R.id.et_latitude);
 		tvLongitude = (TextView) findViewById(R.id.et_longitude);
 		nearbyBtn = (Button) findViewById(R.id.b_go);
 		mListView = (ListView) findViewById(R.id.lv_places);
 		moistureFrame=(FrameLayout) findViewById(R.id.moistureframe);
 		overlay= moistureFrame.getForeground();
 
 		tvStatus = (TextView) findViewById(R.id.tvStatus);
 		
 		mFsqApp = new FoursquareApp(this, CLIENT_ID, CLIENT_SECRET);
 
 		
 		peetimer = new CountDownTimer(20000, 50) {
 			
 			@Override
 			public void onTick(long millisUntilFinished) {
 				// TODO Auto-generated method stub
 				tvStatus.setText("Seconds Remaining: " + mcountDownFormatter.format((millisUntilFinished / 1000.000)));
 				
 				calculateMark();
 				
 				
 			}
 			
 			@Override
 			public void onFinish() {
 				//Reset the list
 				
 				analyzeMark();
 				mAdapter.unselectAll();
 				
 				
 				currentVenue = "none_selected";
 				tvStatus.setText("Select a Location");
 				tvStatus.setTextColor(Color.RED);
 				moistureFrame.setForeground(new ColorDrawable(Color.argb(100, 255, 0, 0)));
 				stopSound(currentstreamID);
 //				soundPool.release();
 //				initSounds();
 //				soundPool.pause(0);
 			
 			}
 		};
 		
 		
 		
 		mAdapter = new NearbyAdapter(this);
 		mNearbyList = new ArrayList<FsqVenue>();
 		mProgress = new ProgressDialog(this);
 		final Button loginbtn = (Button) findViewById(R.id.buttonlogin);
 
 		mProgress.setMessage("Loading data ...");
 tvStatus.setText("Need FS Authorize");
 tvStatus.setTextColor(Color.RED);
 
 		if (mFsqApp.hasAccessToken()){
 		loginbtn.setText("Connected as \n" + mFsqApp.getUserName());
 //			nameTv.setText("Connected as " + mFsqApp.getUserName());
 		tvStatus.setText("Select Location...");
 		tvStatus.setTextColor(Color.RED);
 
 		}
 		FsqAuthListener listener = new FsqAuthListener() {
 			public void onSuccess() {
 				Toast.makeText(BaseActivity.this,
 						"Connected as " + mFsqApp.getUserName(),
 						Toast.LENGTH_SHORT).show();
 				loginbtn.setText("Connected as \n" + mFsqApp.getUserName());
 				tvStatus.setText("Authorized");
 				tvStatus.setTextColor(Color.MAGENTA);
 //				nameTv.setText("Connected as " + mFsqApp.getUserName());
 			}
 
 			public void onFail(String error) {
 				Toast.makeText(BaseActivity.this, error, Toast.LENGTH_SHORT)
 						.show();
 				tvStatus.setText("Need FS Authorize");
 				tvStatus.setTextColor(Color.RED);
 			}
 		};
 
 		mFsqApp.setListener(listener);
 
 		loginbtn.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mFsqApp.authorize();
 			}
 		});
 
 		// Setup GPS coordinate GEtting
 		myLocation = new MyLocation();
 		myLocation.getLocation(BaseActivity.this, locationResult);
 
 		// use access token to get nearby places
 		nearbyBtn.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				// Location activator
 				tvStatus.setText("Loading Nearby...");
 				tvStatus.setTextColor(Color.RED);
 				mNearbyList = new ArrayList<FsqVenue>();
 				mAdapter = new NearbyAdapter(BaseActivity.this);
 				myLocation.getLocation(BaseActivity.this, locationResult);
 
 			}
 		});
 
 		//Clicking a virtual location target
 		mListView.setClickable(true);
 		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 
 				Object o = mListView.getItemAtPosition(position);
 				mListView.setSelection(position);
 
 //				nameTv.setText(o.toString());
 				String latitude = tvLatitude.getText().toString();
 				String longitude = tvLongitude.getText().toString();
 				double lat = Double.valueOf(latitude);
 				double lon = Double.valueOf(longitude);
 				//mNearbyList.get(position).id.toString();
 				
 				//If already the same deselect
 				if(currentVenue.equalsIgnoreCase(mNearbyList.get(position).name.toString())){
 					peetimer.cancel();
 
 					currentVenue = "none_selected";
 					tvStatus.setText("Select a Location");
 					tvStatus.setTextColor(Color.RED);
 					mListView.setSelection(-1);
 					ColorDrawable newover=new ColorDrawable(R.color.lightred);
 					moistureFrame.setForeground(new ColorDrawable(Color.argb(100, 255, 0, 0)));
 //					mediaPlayer.stop();
 //					audioTrack.stop();
 //					stopSound(TONE_ID);
 //					soundPool.release();
 //					initSounds();
 //					soundPool.pause(0);
 					stopSound(currentstreamID);
 
 
 
 
 
 				}
 				//Select a location and start the marking timer
 				else{
 				currentVenue = mNearbyList.get(position).name.toString();
 				currentVenueID = mNearbyList.get(position).id.toString();
 				
 				tvStatus.setText("Start Marking");
 				tvStatus.setTextColor(Color.GREEN);
 				ColorDrawable newover=new ColorDrawable(Color.TRANSPARENT);
 				moistureFrame.setForeground(newover);
 
 				soundPool.resume(0);
 				
 				playSound(TONE_ID);
 				peetimer.start();
 				
 //				 genTone();
 //			        playtone();
 				
 				
 //				mediaPlayer = MediaPlayer.create(BaseActivity.this, R.raw.twentytoneloud);
 //				mediaPlayer.setVolume(0.4f, 0.4f);
 //				mediaPlayer.setLooping(true);
 //				mediaPlayer.start();
 //				try {
 //					mediaPlayer.prepareAsync();
 //				} catch (IllegalStateException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 				
 //				genTone();
 //                soundplay();
 
 //				playtone();
 //				tone.playSound();
 //				Intent intent = new Indtent(BaseActivity.this, PlaySound.class);
 //		        startActivity(intent);
 				}
 //				Log.e("MYT", "Trying to check into= " + currentVenueID);
 //				CheckIntoVenue(lat, lon, 5, currentVenueID);
 
 			}
 		});
 
 			// End OnCreate
 	}
 
 	public LocationResult locationResult = new LocationResult() {
 		@Override
 		public void gotLocation(final Location location) {
 			// do something
 			location.getLongitude();
 			location.getLatitude();
 			Log.e("GPS", "Long is=" + location.getLongitude() + "Latitudeis ="
 					+ location.getLatitude());
 			tvLatitude.setText("" + location.getLatitude());
 			tvLongitude.setText("" + location.getLongitude());
 			// nearbyBtn.performClick();
 			getNearby();
 		}
 	};
 	public void getNearby() {
 		String latitude = tvLatitude.getText().toString();
 		String longitude = tvLongitude.getText().toString();
 
 		if (latitude.equals("") || longitude.equals("")) {
 			Toast.makeText(BaseActivity.this, "Latitude or longitude is empty",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		double lat = Double.valueOf(latitude);
 		double lon = Double.valueOf(longitude);
 		loadNearbyPlaces(lat, lon);
 		
 
 
 	}
 
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add("Simulate");
 		menu.add("Quit");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getTitle() == "Simulate") {
 			showControls();
 		} else if (item.getTitle() == "Quit") {
 			finish();
 			System.exit(0);
 		}
 		return true;
 	}
 
 	protected void enableControls(boolean enable) {
 		if (enable) {
 			showControls();
 		} else {
 			hideControls();
 		}
 	}
 
 	protected void hideControls() {
 		// setContentView(R.layout.no_device);
 		nodeviceview.setVisibility(View.VISIBLE);
 
 		mInputController = null;
 	}
 
 	protected void showControls() {
 		// setContentView(R.layout.maintwo);
 		nodeviceview.setVisibility(View.GONE);
 		mInputController = new InputController(this);
 		mInputController.accessoryAttached();
 	}
 
 	// protected void handleJoyMessage(JoyMsg j) {
 	// if (mInputController != null) {
 	// mInputController.joystickMoved(j.getX(), j.getY());
 	// }
 	// }
 	//
 	// protected void handleLightMessage(LightMsg l) {
 	// if (mInputController != null) {
 	// mInputController.setLightValue(l.getLight());
 	// }
 	// }
 
 	protected void handleMoistureMessage(MoistureMsg t) {
 		if (mInputController != null) {
 			currentmoisturelevel = t.getMoisture();
 //			mInputController.setMoisture(currentmoisturelevel);
 			//smooth things out a bit with an moving average
 			avemoisturelevel=avemoisturelevel+(currentmoisturelevel-avemoisturelevel)*.1;
 //			mInputController.setMoisture((int) avemoisturelevel);
 			mInputController.setMoisture((int) maxedmoisturelevel);
 
 			if(locationpreparedtoCheckin){
 				}
 			
 		
 
 		}
 	}
 
 	protected void analyzeMark() {
 		//After the peetimer is up or the user ends the mark early we perform a quick set of calculations to decide
 		// Handle whether or not moist enough to check in!
 		//This is where the rhetoric of this program is implemented
 		/*
 		 * Here is what we are looking for
 		 * A) if they pass a certain threshold
 		 * B) if they maintain a certain value (ie didn't just accidentally brush it or knock something loose)
 		 * C)
 		 * 
 		 * Then we reward/punish the individual's check-in based on how soaking wet they got the target
 		 * 
 		 * A) we shift their lat/long coordinates a proportional distance away from their actual location
 		 * (foursquare won't give you badges if you are too far away from a place)
 		 * B)we leave a spectrum of comments with their check in ranging from
 		 * "I wasn't here at all, I am lame."
 		 * to
 		 * "I was SUPREMELY AT this location!" 
 		 */
 		Log.e("MYT", "Maxedmoisture was "+maxedmoisturelevel+"Shift Amount= " + (100-maxedmoisturelevel)/100);
 
 		//Rate the mark
 		rating = 0;
 		alpharating="F";
 		checkinmessage="I have no ownership of anything";
 
 			
 	if(maxedmoisturelevel>50){
 		rating = 1;
 		alpharating="D";
 		checkinmessage="I am so lame.\nMYT score = "+alpharating;
 	}
 	if(maxedmoisturelevel>200){
 		rating = 2;
 		alpharating="C";
 		checkinmessage="I wasn't really here, just pretending.\nMYT score = "+alpharating;
 
 	}
 	if(maxedmoisturelevel>600){
 		rating = 3;
 		alpharating="B";
 		checkinmessage="Yeah, look for me, IM HERE!\nMYT score = "+alpharating;
 
 	}
 	if(maxedmoisturelevel>850){
 		rating = 4;
 		alpharating="A";
 		checkinmessage="THIS IS MY PLACE! PHYSICAL CHECK-IN!\nMYT score = "+alpharating;
 
 	}
 	if(maxedmoisturelevel>950){
 		rating = 5;
 		alpharating="AA";
 		checkinmessage="I AM THE SUPREME RULER HERE! CLAIMED SO HARD! THE DIGITAL AND PHSYICAL REALMS ARE MINE!\nMYT score = "+alpharating;
 
 	}
 		
 	Log.e("MYT", "rating= "+rating+" " +alpharating);
 
 		if(rating>0){
 		String latitude = tvLatitude.getText().toString();
 		String longitude = tvLongitude.getText().toString();
 
 		double manipulatedlat = Double.valueOf(latitude)+(100-maxedmoisturelevel)/100;
 		double manipulatedlon = Double.valueOf(longitude)+(100.00-maxedmoisturelevel)/100.00;
 		Log.e("MYT", "Trying to physically check into= " + currentVenue);
 		checkinmessage=URLEncoder.encode(checkinmessage);
 		CheckIntoVenue(manipulatedlat, manipulatedlon, rating, alpharating, checkinmessage, currentVenueID);
 		Toast.makeText(BaseActivity.this, URLDecoder.decode(checkinmessage)+" @"+currentVenue, Toast.LENGTH_LONG).show();
 
 		}
 		else{
 			Toast.makeText(BaseActivity.this, URLDecoder.decode(checkinmessage)+" @"+currentVenue, Toast.LENGTH_LONG).show();
 		}
 		
 		
 		
 		//Reset all the values for next check in
 		rating=-1;
 		alpharating="NA";
 		maxedmoisturelevel=-1;
 		
 	}
 
 	protected void calculateMark() {
 	
 		//For SIMULATION ONLY REMOVE THIS CODE
 //		avemoisturelevel=avemoisturelevel+.5;
 //		mProgressbar.setProgress((int) avemoisturelevel);
 		
 		//For SIMULATION ONLY REMOVE THIS CODE
 
 		
 		if(maxedmoisturelevel<avemoisturelevel){
 		maxedmoisturelevel=(int) avemoisturelevel;
 		}
 		int tonenum= (int) (avemoisturelevel/10);
 //		tonegen.startTone(tonenum,100);
 //		audioTrack.setPlaybackRate(sampleRate/2);
 //		audioTrack.play();
 		soundPool.setRate(currentstreamID, (float) (.5+1.5*avemoisturelevel/100));
 	
 		
 	}
 	
 	
 
 	/**
 	 * Long and lat are current lat and long level = how well you were peering
 	 */
 	private void CheckIntoVenue(final double latitude, final double longitude,
 			int rat,String alpharat, final String messagetoshout, final String venueid) {
 		mProgress.show();
 
 		new Thread() {
 			@Override
 			public void run() {
 				int what = 0;
 
 				try {
 
 					Log.e("MYT", mFsqApp.checkIn(latitude, longitude,
 							messagetoshout, venueid));
 				} catch (Exception e) {
 					what = 1;
 					e.printStackTrace();
 				}
 
 				mProgress.dismiss();
 //				mfsHandler.sendMessage(mfscheckinHandler.obtainMessage(what));
 			}
 		}.start();
 	}
 
 	private Handler mfscheckinHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			mProgress.dismiss();
 
 			if (msg.what == 0) {
 				if (mNearbyList.size() == 0) {
 					Toast.makeText(BaseActivity.this,
 							"No nearby places available", Toast.LENGTH_SHORT)
 							.show();
 					return;
 				}
 
 //				mAdapter.setData(mNearbyList);
 //				mListView.setAdapter(mAdapter);
 				
 				
 				
 			} else {
 				Toast.makeText(BaseActivity.this,
 						"Failed to load nearby places", Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 	};
 	
 	private void loadNearbyPlaces(final double latitude, final double longitude) {
 		mProgress.show();
 
 		new Thread() {
 			@Override
 			public void run() {
 				int what = 0;
 
 				try {
 
 					mNearbyList = mFsqApp.getNearby(latitude, longitude);
 				} catch (Exception e) {
 					what = 1;
 					e.printStackTrace();
 				}
 				mfsHandler.sendMessage(mfsHandler.obtainMessage(what));
 				
 			}
 		}.start();
 	}
 
 
 	private Handler mfsHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			mProgress.dismiss();
 
 			if (msg.what == 0) {
 				if (mNearbyList.size() == 0) {
 					Toast.makeText(BaseActivity.this,
 							"No nearby places available", Toast.LENGTH_SHORT)
 							.show();
 					return;
 				}
 
 				mAdapter.setData(mNearbyList);
 				mListView.setAdapter(mAdapter);
 				//Initialize
 //				mListView.setSelection(0);
 //				currentVenue = mNearbyList.get(0).name.toString();
 //				currentVenueID = mNearbyList.get(0).id.toString();
 				locationpreparedtoCheckin=true;
 				
 				
 				
 				
 			} else {
 				Toast.makeText(BaseActivity.this,
 						"Failed to load nearby places", Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 	};
 	
 	/*
 	 * Sound Stuff
 	 */
 	/**void playtone(){
 		 // Use a new tread as this can take a while
         final Thread thread = new Thread(new Runnable() {
             public void run() {
                 genTone();
                 soundhandler.post(new Runnable() {
 
                     public void run() {
                         soundplay();
                     }
                 });
             }
         });
         thread.start();
 	}
 	void genTone(){
         // fill out the array
         for (int i = 0; i < numSamples; ++i) {
             sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
         }
 
         // convert to 16 bit pcm sound array
         // assumes the sample buffer is normalised.
         int idx = 0;
         for (final double dVal : sample) {
             // scale to maximum amplitude
             final short val = (short) ((dVal * 32767));
             // in 16 bit wav PCM, first byte is the low order byte
             generatedSnd[idx++] = (byte) (val & 0x00ff);
             generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
 
         }
     }
 
     void soundplay(){
         final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                 sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 AudioFormat.ENCODING_PCM_16BIT, numSamples,
                 AudioTrack.MODE_STATIC);
         audioTrack.write(generatedSnd, 0, generatedSnd.length);
 //        audioTrack.setLoopPoints(0, 1, -1);
         audioTrack.setStereoVolume(100, 100);
         audioTrack.play();
        
         Log.e("MYT","Sound PLAY");
     }
     **/
     private void initSounds() {
         soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
         soundPoolMap = new HashMap<Integer, Integer>();
         soundPoolMap.put(TONE_ID, soundPool.load(getBaseContext(), R.raw.threeloud, 1));
 //        soundPool.setLoop(TONE_ID, -1);
    }
              
    public void playSound(int sound) {
        /* Updated: The next 4 lines calculate the current volume in a scale of 0.0 to 1.0 */
 //       AudioManager mgr = (AudioManager)getBaseContext().getSystemService(Context.AUDIO_SERVICE);
 //       float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
 //       float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
 //       float volume = streamVolumeCurrent / streamVolumeMax;
        
        /* Play the sound with the correct volume */
 	   currentstreamID=soundPool.play(soundPoolMap.get(sound), 0.5f, 0.5f, 1, -1, 1f);
        Log.e("MYT",currentstreamID+" play output");     
    }
    public void stopSound(int sound){
 	   soundPool.stop(currentstreamID);
    }
    
     
 
 	
 }
