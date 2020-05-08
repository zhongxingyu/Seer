 package com.vobileinc.tvsyncexample;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import org.json.JSONObject;
 
 import tvhackfest.likeplause.R;
 import tvhackfest.likeplause.ViewWebContentActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.vobileinc.tvsyncapi.TVSYNCError;
 import com.vobileinc.tvsyncapi.TVSYNCQuery;
 import com.vobileinc.tvsyncapi.TVSYNCQuery.TVSYNCQueryParameterKey;
 import com.vobileinc.tvsyncapi.TVSYNCQuery.TVSYNCQueryParameterValue;
 import com.vobileinc.tvsyncapi.TVSYNCQueryActivityCallbacks;
 import com.vobileinc.tvsyncapi.TVSYNCQueryType;
 import com.vobileinc.tvsyncapi.TVSYNCService;
 import com.vobileinc.tvsyncapi.TVSYNCService.OnConnectListener;
 import com.vobileinc.tvsyncapi.TVSYNCService.OnQueryCreateListener;
 
 public class TVSYNCExampleActivity extends Activity {
 	
	public static String LAST_RESULT = "No Result";
 
 	static final String				kApiKey		= "twmidgtol7t99yc4v86j6k2hi73kbq8q";
 	static final String				LOG_TAG		= "TVSYNCExampleActivity";
 
 	Button							skip;
 	Button							setupImg;
 	Button							setupVid;
 	Button							setupAud;
 	TextView						status;
 	Button							start;
 	Button							stopBtn;
 	FrameLayout						previewFrame;
 	ImageView						capturedImg;
 	TextView						resultTxt;
 
 	TVSYNCQuery						query;
 
 	TVSYNCQueryActivityCallbacks	callbacks	= new TVSYNCQueryActivityCallbacks() {
 
 													@Override
 													public void queryDidTimeout(final TVSYNCQuery query) {
 														// query has no match
 														resultTxt.setText("Query did timeout");
 														resultTxt.setVisibility(View.VISIBLE);
 														capturedImg.setVisibility(View.INVISIBLE);
 														query.stopQuery();
 													}
 
 													@Override
 													public void didStartQuery(final TVSYNCQuery query) {
 														// query started
 														status.setText("Query started");
 													}
 
 													@Override
 													public void didReceiveQueryResult(final TVSYNCQuery query, final JSONObject result) {
 														// receive query result
 														resultTxt.setVisibility(View.VISIBLE);
 
 														// for image, result is a string
 														resultTxt.setText("Result " + result.toString());
 
 														capturedImg.setVisibility(View.INVISIBLE);
 
 														// stops the query
 														query.stopQuery();
 														
 														LAST_RESULT = result.toString();
 														
 														
 														Intent intent = new Intent(TVSYNCExampleActivity.this, ViewWebContentActivity.class);
 														startActivity(intent);
 														
 														
 													}
 
 													@Override
 													public void didFailWithError(final TVSYNCQuery query, final TVSYNCError error) {
 														// query failed
 														status.setText("Query failed " + error.errorCode + error.description);
 
 														// stop query
 														query.stopQuery();
 													}
 
 													@Override
 													public void didEndQuery(final TVSYNCQuery query, final long duration) {
 														// query ended
 														status.setText("Query ended with duration: " + duration + " ms");
 
 														// enable setup buttons
 														setQueryButtonsEnabled(true);
 														start.setText("Start Query!");
 														start.setEnabled(false);
 													}
 
 													@Override
 													public void didInitializeCamera(final TVSYNCQuery query) {
 														status.setText("Camera session initialized");
 														// enable "Start Query!" button
 														start.setEnabled(true);
 													}
 
 													@Override
 													public void didCaptureImage(final TVSYNCQuery query, final Bitmap image) {
 														// captured image for TVSYNCQuery with type TVSYNCQueryTypeImage
 														status.setText("Captured image");
 
 														// display it
 														capturedImg.setVisibility(View.VISIBLE);
 														capturedImg.setImageBitmap(image);
 
 														// query it
 														query.startQuery();
 													}
 
 													@Override
 													public FrameLayout cameraPreviewFrame(final TVSYNCQuery query) {
 														return previewFrame;
 													}
 
 													public void clientProgress(final TVSYNCQuery query, final int progress) {
 														// receive client progress
 														status.setText("Client progress update " + progress);
 													}
 
 													public void serverProgress(final TVSYNCQuery query, final int progress) {
 														// receive server progress
 														status.setText("Server progress update " + progress);
 													}
 
 													public void didInitializeAudioWithQuery(final TVSYNCQuery query) {
 														// audio session initialized for TVSYNCQuery with type
 														// TVSYNCQueryTypeAudio
 														status.setText("Audio session initialized");
 
 														// enable "Start Query!" button
 														start.setEnabled(true);
 													}
 
 													public Rect partialAreaForQuery(final TVSYNCQuery query) {
 														return null;
 													}
 												};
 
 	private void createQuery(TVSYNCQueryType type) {
 		// disable all query buttons
 		setQueryButtonsEnabled(false);
 
 		// clear previous result
 		resultTxt.setText("");
 		resultTxt.setVisibility(View.INVISIBLE);
 
 		// construct parameters
 		// live or non-live
 		HashMap<TVSYNCQueryParameterKey, TVSYNCQueryParameterValue> params = new HashMap<TVSYNCQueryParameterKey, TVSYNCQueryParameterValue>();
 		params.put(TVSYNCQueryParameterKey.TVSYNCQueryParameterKeyLiveQuery, TVSYNCQueryParameterValue.No);
 		// non-stop?
 		// params.put(TVSYNCQueryParameterKey.TVSYNCQueryParameterKeyQueryTimeout, TVSYNCQueryParameterValue.);
 
 		// create a TVSYNCQuery, use xml configuration
 		TVSYNCService.sharedService().createQuery(type, callbacks, params, new OnQueryCreateListener() {
 
 			@Override
 			public void onQueryCreated(TVSYNCQuery query) {
 				status.setText(" Query created");
 
 				// TVSYNCQuery created successfully
 				TVSYNCExampleActivity.this.query = query;
 
 				// setup the query
 				TVSYNCExampleActivity.this.query.initialize();
 			}
 
 			@Override
 			public void onCreationFailed(TVSYNCError error) {
 				// failed to create TVSYNCQuery
 				Log.d(LOG_TAG, "Failed to create TVSYNCQuery, error code " + error.errorCode + ", error description " + error.description);
 
 				setQueryButtonsEnabled(true);
 				status.setText("Failed to create query");
 			}
 		});
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		skip = (Button) findViewById(R.id.skip);
 		skip.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(TVSYNCExampleActivity.this, ViewWebContentActivity.class);
 				startActivity(intent);
 			}
 		});
 		
 		
 		setupImg = (Button) findViewById(R.id.setupImg);
 		setupImg.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createQuery(TVSYNCQueryType.TVSYNCQueryTypeImage);
 			}
 		});
 
 		setupVid = (Button) findViewById(R.id.setupVid);
 		setupVid.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				createQuery(TVSYNCQueryType.TVSYNCQueryTypeVideo);
 			}
 		});
 
 		setupAud = (Button) findViewById(R.id.setupAud);
 		setupAud.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				createQuery(TVSYNCQueryType.TVSYNCQueryTypeAudio);
 			}
 		});
 
 		previewFrame = (FrameLayout) findViewById(R.id.previewFrame);
 		capturedImg = (ImageView) findViewById(R.id.capturedImg);
 		resultTxt = (TextView) findViewById(R.id.resultTxt);
 		status = (TextView) findViewById(R.id.status);
 		stopBtn = (Button) findViewById(R.id.stop);
 		start = (Button) findViewById(R.id.start);
 
 		start.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (start.getText().equals("Start Query!")) {
 					if (query.getQueryType() == TVSYNCQueryType.TVSYNCQueryTypeImage) {
 						query.captureImage();
 						start.setEnabled(false);
 					}
 					else {
 						query.startQuery();
 						start.setText("Pause");
 					}
 				}
 				else if (start.getText().equals("Pause")) {
 					query.pauseQuery();
 					start.setText("Resume");
 				}
 				else if (start.getText().equals("Resume")) {
 					query.resumeQuery();
 					start.setText("Pause");
 				}
 				stopBtn.setEnabled(true);
 			}
 		});
 
 		stopBtn.setEnabled(false);
 		stopBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				query.stopQuery();
 				stopBtn.setEnabled(false);
 			}
 		});
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		if (query != null) {
 			query.stopQuery();
 		}
 
 		TVSYNCService.sharedService().disconnect();
 	}
 
 	protected void onResume() {
 		super.onResume();
 		// Connection to TVSYNCService
 		final InputStream is;
 		try {
 			is = getAssets().open("config.xml");
 			TVSYNCService.sharedService().connectService(this, kApiKey, is, null, new OnConnectListener() {
 
 				@Override
 				public void onServiceConnected() {
 					// successfully connected to TVSYNCService
 					// now we can enable query buttons and ready for queries
 					setQueryButtonsEnabled(true);
 					status.setText("Successfully connected to TVSYNCService");
 					try {
 						is.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 
 				@Override
 				public void onConnectionFailed(TVSYNCError error) {
 					// failed to connect to TVSYNCService
 					Log.v(LOG_TAG, "Failed to connect to TVSYNCService, error code:" + error.errorCode + ", error description:" + error.description);
 					status.setText("Failed to connect to TVSYNCService");
 
 					try {
 						is.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void setQueryButtonsEnabled(boolean enabled) {
 		setupImg.setEnabled(enabled);
 		setupAud.setEnabled(enabled);
 		setupVid.setEnabled(enabled);
 	}
 }
