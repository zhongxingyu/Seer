 package cc.rainwave.android;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import cc.rainwave.android.adapters.ElectionListAdapter;
 import cc.rainwave.android.api.Session;
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.RainwaveResponse;
 import cc.rainwave.android.api.types.Song;
 import cc.rainwave.android.api.types.Station;
 import cc.rainwave.android.listeners.HexadecimalKeyListener;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.PixelFormat;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.EditText;
 
 /**
  * First thing the user sees when starting the app.
  * Provides an easier prompt for getting identified.
  * @author pkilgo
  *
  */
 public class LandingActivity extends Activity {
 	
 	private VerifyCredentials mVerifyCredentialsTask;
 	
     private Session mSession;
 
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		preLayout();
 		setContentView(R.layout.activity_landing);
 		postLayout();
 	}
 	
 	public void onActivityResult(int request, int result, Intent data) {
 		IntentResult ir = IntentIntegrator.parseActivityResult(request, result, data);
 		if(ir == null) return;
 		
 		String raw = ir.getContents();
 		if(raw == null) return;
 		
 		Uri uri = Uri.parse(raw);
 		String userInfo = uri.getUserInfo();
 		String user = Rainwave.extractUserId(userInfo);
 		String key = Rainwave.extractKey(userInfo);
 		
 		((EditText)findViewById(R.id.land_userId)).setText(user);
 		((EditText)findViewById(R.id.land_apiKey)).setText(key);
 	}
 	
 	private void postLayout() {
 		((EditText)findViewById(R.id.land_apiKey)).setKeyListener(new HexadecimalKeyListener());
 		
 		findViewById(R.id.land_login).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String user = ((EditText)findViewById(R.id.land_userId)).getText().toString();
 				String key = ((EditText)findViewById(R.id.land_apiKey)).getText().toString();
				verifyUserInfo(user,key);
 			}
 		});
 		
 		findViewById(R.id.land_later).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startNowPlaying();
 			}
 		});
 		
 		findViewById(R.id.land_never).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Rainwave.setSkipLanding(LandingActivity.this, true);
 				startNowPlaying();
 			}
 		});
 		
 		findViewById(R.id.land_qrButton).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				IntentIntegrator.initiateScan(LandingActivity.this);
 			}
 		});
 	}
 
 	private void preLayout() {
 		if(Rainwave.hasUserInfo(this) || Rainwave.skipLanding(this)) {
 			startNowPlaying();
 		}
 		
 		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		try {
 			mSession = Session.makeSession(LandingActivity.this);
 		} catch (MalformedURLException e) {
 			Rainwave.showError(this, e);
 		}
 	}
 	
 	private void verifyUserInfo(String user, String key) {
 		if(mVerifyCredentialsTask == null) {
 			mVerifyCredentialsTask = new VerifyCredentials();
 			mVerifyCredentialsTask.execute(user,key);
 		}
 	}
 	
 	private void setInfoAndStart(String user, String key) {
 		Rainwave.putUserId(this, user);
 		Rainwave.putKey(this, key);
 		startNowPlaying();
 	}
 	
 	private void startNowPlaying() {
 		Intent i = new Intent(this, NowPlayingActivity.class);
 		startActivity(i);
 		finish();
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 	    super.onAttachedToWindow();
 	    Window window = getWindow();
 	    window.setFormat(PixelFormat.RGBA_8888);
 	}
 	
 	 /**
      * Fetches the now playing info.
      * Expects one argument to <code>execute(Object...params)</code> which
      * is the flag to indicate if this is an initializing (e.g., non-longpoll)
      * fetch of the schedule data.
      * @author pkilgo
      *
      */
     protected class VerifyCredentials extends AsyncTask<String, Integer, Bundle> {
         private String TAG = "VerifyCredentials";
 
         private String mUser, mKey;
         
         @Override
         protected Bundle doInBackground(String ... args) {
             mUser = args[0];
             mKey = args[1];
             
             mSession.setUserInfo(mUser, mKey);
             dispatchThrobberVisibility(true);
             
             Bundle b = new Bundle();
             try {
             	RainwaveResponse organizer = mSession.syncInit();
                 b.putParcelable(Rainwave.SCHEDULE, organizer);
                 return b;
             } catch (IOException e) {
                 Log.e(TAG, "IOException occured: " + e);
                 Rainwave.showError(LandingActivity.this, e);
                 return null;
             } catch (RainwaveException e) {
             	Log.e(TAG, "API error: " + e.getMessage());
             	Rainwave.showError(LandingActivity.this, e);
             	return null;
             }
         }
         
         protected void onPostExecute(Bundle result) {
             super.onPostExecute(result);
             
             dispatchThrobberVisibility(false);
             
             mVerifyCredentialsTask = null;
             
             // Was there an IO failure?
             if(result == null) {
             	return;
             }
             
             RainwaveResponse tmp = result.getParcelable(Rainwave.SCHEDULE);
             
             if(tmp == null) {
             	return;
             }
             
             // Set user credentials and start next activity.
             setInfoAndStart(mUser, mKey);
         }
     }
     
     private void dispatchThrobberVisibility(boolean state) {
     	Message msg = mHandler.obtainMessage(HANDLER_SET_INDETERMINATE);
     	Bundle data = msg.getData();
     	data.putBoolean("bool", state);
     	msg.sendToTarget();
     }
     
     private Handler mHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		Bundle data = msg.getData();
     		switch(msg.what) {
     		case HANDLER_SET_INDETERMINATE:
     			setProgressBarIndeterminateVisibility( data.getBoolean("value") );
     			break;
     		}
     	}
     };
     
     public static final int
     	HANDLER_SET_INDETERMINATE = 0x1D373;
 }
