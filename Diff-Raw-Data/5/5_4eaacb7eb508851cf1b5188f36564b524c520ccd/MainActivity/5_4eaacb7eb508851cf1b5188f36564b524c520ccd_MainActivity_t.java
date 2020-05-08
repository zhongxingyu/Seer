 package com.adamharley.happyhouse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.Random;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	private static boolean debug = false;
 	private static final String TAG = "HappyHouse";
 	private JSONObject data;
 	private Integer currentFrame;
 	private RelativeLayout stage;
 	private MediaPlayer mediaPlayer;
 	private Handler ticker = new Handler();
 	private static int framesPerSecond = 5;
 	private Runnable tick = new Runnable() {
 
 		@Override
 		public void run() {
 			ticker.removeCallbacks(this);
 			
 			if (!(currentFrame instanceof Integer)) {
 				currentFrame = 0;
 			}
 			
 			currentFrame += 1;
 
     		checkFPS(currentFrame);
 			
 	    	if (!checkEvent(currentFrame - 1)) {
 	    		loadImageFrame(currentFrame);
 	    		loadSoundFrame(currentFrame);
 	    	}
 			
 			ticker.postDelayed(this, 1000 / framesPerSecond);
 		}
 		
 	};
 	private Runnable soundTimeout = new Runnable() {
 		
 		@Override
 		public void run() {
 			ticker.removeCallbacks(this);
 			
 			if (mediaPlayer instanceof MediaPlayer) {
 				mediaPlayer.release();
 				mediaPlayer = null;
 			}
 		}
 		
 	};
 	private class TurnAnimRunnable implements Runnable {
 		private int n;
 		private String channel;
 		private JSONArray sprite;
 		private int image;
 		
 		private TurnAnimRunnable(int _n, String _channel, JSONArray _sprite, int _image) {
 			this.n = _n;
 			this.channel = _channel;
 			this.sprite = _sprite;
 			this.image = _image;
 		}
 
 		public void run() {
 			try {
 				sprite.put(0, image);
 				loadImage(n, channel, sprite);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         stage = (RelativeLayout) findViewById(R.id.stage);
         
         if( loadData() ) {
         	loadScene("start0");
         }
     }
     
     
     @Override
     public void onPause() {
     	super.onPause();
     	
     	ticker.removeCallbacks(soundTimeout);
 		if (mediaPlayer instanceof MediaPlayer) {
 			mediaPlayer.release();
 			mediaPlayer = null;
 		}
     }
     
     
     @Override
     public void onResume() {
     	super.onResume();
     	
 		ticker.postDelayed(tick, 1000 / framesPerSecond);
     }
     
     
     @Override
     public void onStop() {
     	super.onStop();
     	
     	ticker.removeCallbacks(tick);
     	ticker.removeCallbacks(soundTimeout);
     	
 		if (mediaPlayer instanceof MediaPlayer) {
 			mediaPlayer.release();
 			mediaPlayer = null;
 		}
     }
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.action_about:
                 Intent intent = new Intent(this, AboutActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     
     private int getRand(int n) {
     	Random r = new Random();
     	return r.nextInt(n) + 1;
     }
     
     
     private boolean getRandMatch(int n) {
     	return getRand(n) == n;
     }
     
     
     private boolean loadData() {
     	try
     	{
     		InputStream is = getResources().openRawResource(R.raw.data);
     		byte [] buffer = new byte[is.available()];
     		while (is.read(buffer) != -1);
     		String jsontext = new String(buffer);
     		data = new JSONObject(jsontext);
 		} catch (IOException e) {
 			Log.e(TAG,"IOException: "+e.getMessage());
 			return false;
     	} catch (JSONException e) {
 			Log.e(TAG,"JSONException: "+e.getMessage());
 			return false;
 		}
     	
 		return true;
     }
     
     
     private void loadSoundFrame(Integer n) {
     	try {
     		final JSONObject soundFrames = data.getJSONObject("soundFrames");
     		
     		if (!soundFrames.isNull(n.toString())) {
 	    		JSONArray frame = soundFrames.getJSONArray(n.toString());
 	    		String sound = frame.getString(0);
 	    		
 	    		Log.i(TAG,"Frame "+n+": Playing sound "+sound);
 	    		
 	    		String soundFile = "sounds/" + sound.replace("-", "_") + ".wav";
 	    		
 	    	    AssetFileDescriptor descriptor = getAssets().openFd(soundFile);
 	    	    long start = descriptor.getStartOffset();
 	    	    long end = descriptor.getLength();
 
 	    		if (mediaPlayer instanceof MediaPlayer) {
 	    			mediaPlayer.reset();
 	    		} else {
 		    	    mediaPlayer = new MediaPlayer();
 	    		}
 	    		
 	    	    mediaPlayer.setDataSource(descriptor.getFileDescriptor(), start, end);
 	    	    descriptor.close();
 	    	    mediaPlayer.prepare();
 	    		
 	    		if (frame.isNull(1)) {
 	    			mediaPlayer.setLooping(false);
 	    			
 	    			if (sound.equals("Sr-twkl2")) {
 	    				mediaPlayer.seekTo(4); // Offset to avoid popping noise
 	    			}
 	    		} else { // Looped
 	    			mediaPlayer.setLooping(true);
 	    			
 	    			int duration = frame.getInt(1) - n;
 	    			
 	    			ticker.postDelayed(soundTimeout, duration * 1000 / framesPerSecond);
 	    			Log.i(TAG,"Frame "+n+": Looping sound for "+duration+" frames");
 	    		}
 	    		
 	    	    mediaPlayer.start();
     		}
     	} catch (JSONException e) {
 			Log.e(TAG,"JSONException: "+e.getMessage());
     	} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
     
     
     private void loadImageFrame(int n) {
     	// Hide all channels
     	for (int i = 1; i < 30; i++ ) {
     		ImageView iv = (ImageView) stage.findViewWithTag("channel"+i);
     		iv.setVisibility(View.INVISIBLE);
     	}
     	
     	try {
     		final JSONArray imageFrames = data.getJSONArray("imageFrames");
     		JSONObject frame = imageFrames.getJSONObject(n-1);
     		
             Iterator<?> channels = frame.keys();
             
             while( channels.hasNext() ){
                 String channel = (String)channels.next();
             	JSONArray sprite = (JSONArray) frame.get(channel);
             	
             	loadImage(n, channel, sprite);
 			}
     		
 //    		Log.i(TAG,"Frame "+n+": Loaded");
     	} catch (JSONException e) {
 			Log.e(TAG,"JSONException: "+e.getMessage());
     	}
     }
     
     
 	private void loadImage(final int n, final String channel, final JSONArray sprite) throws JSONException {
     	
     	String imageName = sprite.getString(0);
 		Integer imageResID = getResources().getIdentifier("member_"+imageName, "drawable", getPackageName());
 		
 		ImageView iv = (ImageView) stage.findViewWithTag("channel"+channel);
 		iv.setImageResource(android.R.color.transparent);
 		iv.setClickable(false);
 		
     	if( imageResID.equals(0) ) {
     		switch (Integer.parseInt(imageName)) { // Convert to integer to work around for being below JRE 1.7
 				case 288: // Quit button
 					int width = 52 * 2;
 					int height = 22 * 2;
 					
 					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
 	    			params.leftMargin = sprite.getInt(1) * 2;
 	    			params.topMargin = sprite.getInt(2) * 2;
 	    			
 	    			iv.setLayoutParams(params);
 	    			iv.setVisibility(View.VISIBLE);
 	    			
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 	    		        	loadScene("end1A");
 	    		        }
 	    			});
     				break;
 				case 289: // Basket button? (found in start0, end1 and hello2)
 					break;
 				case 290: // Open button
 					width = 52 * 2;
 					height = 22 * 2;
 					
 					params = new RelativeLayout.LayoutParams(width, height);
 	    			params.leftMargin = sprite.getInt(1) * 2;
 	    			params.topMargin = sprite.getInt(2) * 2;
 	    			
 	    			iv.setLayoutParams(params);
 	    			iv.setVisibility(View.VISIBLE);
 	    			
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 	    		        	loadScene("end1B");
 	    		        }
 	    			});
     				break;
 				case 291: // Black background
 					break;
 				default:
 					Log.i(TAG,"Frame "+n+": Could not find cast member "+imageName);
 			}
     	} else {
 			switch (Integer.parseInt(imageName)) { // Convert to integer to work around for being below JRE 1.7
 				case 12: // Basket top
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 	    		        	loadScene("hello1");
 	    		        }
 	    			});
     				break;
 				case 14: // Basket full
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 	    		        	loadScene("s"+getRand(3));
 	    		        }
 	    			});
     				break;
 				case 18: // Bowl
 				case 19:
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 	    		        	try {
     	    		        	if (
     	    		        		(
     		        					currentFrame >= data.getJSONObject("scenes").getInt("sitA")
     		        					&& currentFrame < data.getJSONObject("scenes").getInt("sB2A")
 	    		        			)
 	    		        			||
 	    		        			(
     		        					currentFrame >= data.getJSONObject("scenes").getInt("sleepA")
     		        					&& currentFrame < data.getJSONObject("scenes").getInt("akubi")
 	    		        			)
 	    		        		) {
 	    		        			loadScene("food1");
     	    		        	} else if (
 	    		        			currentFrame >= data.getJSONObject("scenes").getInt("sitB")
 	    		        			&& currentFrame < data.getJSONObject("scenes").getInt("sitC")
     		        			) {
     	    		        		loadScene("food2");
     	    		        	} else if (
 	    		        			currentFrame >= data.getJSONObject("scenes").getInt("sitC")
 	    		        			&& currentFrame < data.getJSONObject("scenes").getInt("walkA")
     	    		        	) {
     	    		        		loadScene("food3");
     	    		        	}
 	    		        	} catch (JSONException e) {
 	    		    			Log.e(TAG,"JSONException: "+e.getMessage());
 	    		        	}
     					}
 	    			});
     				break;
 				case 46: // Walking left hamster
 				case 47:
 				case 48:
 				case 49:
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 	    					ticker.removeCallbacks(tick);
 	    					ticker.removeCallbacks(soundTimeout);
 	    					
 	    		        	int[] sequence = {1,2,3,4,3,4,3,5};
 	    		        	
 	    		        	for(int i = 0; i < sequence.length; i++) {
 		    		        	TurnAnimRunnable obj = new TurnAnimRunnable(n, channel, sprite, sequence[i]);
 		    		        	ticker.postDelayed(obj, 200 * (i + 1));
 	    		        	}
 	    		        	
 	    		        	ticker.postDelayed(tick, 200 * (sequence.length + 1));
 	    		        }
 	    			});
 					break;
 				case 61: // Walking right hamster
 				case 62:
 				case 63:
 				case 64:
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 	    					ticker.removeCallbacks(tick);
 	    					ticker.removeCallbacks(soundTimeout);
 	    					
 	    		        	int[] sequence = {6,7,8,9,8,9,8,10};
 	    		        	
 	    		        	for(int i = 0; i < sequence.length; i++) {
 		    		        	TurnAnimRunnable obj = new TurnAnimRunnable(n, channel, sprite, sequence[i]);
 		    		        	ticker.postDelayed(obj, 200 * (i + 1));
 	    		        	}
 	    		        	
 	    		        	ticker.postDelayed(tick, 200 * (sequence.length + 1));
 	    		        }
 	    			});
 					break;
 				case 77: // SitA hamster
 				case 78:
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 							int rand = getRand(5);
 							
 							if (rand <= 2) {
								loadScene("sA2B");
 							} else if (rand <= 4) {
 								loadScene("sitC");
 							} else {
 								loadScene("go");
 							}
 	    		        }
 	    			});
 					break;
 				case 90: // SitB hamster
 				case 91:
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 							if (getRandMatch(5)) {
								loadScene("sB2A");
 							} else {
 								int rand = getRand(4);
 								loadScene( (rand == 4) ? "lunch" : "lunch"+getRand(3) );
 							}
 	    		        }
 	    			});
 					break;
 				case 116: // SitC hamster
 				case 117:
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 							if (getRandMatch(5)) {
 								loadScene("sleepA");
 							} else {
 								loadScene("go");
 							}
 	    		        }
 	    			});
 					break;
 				case 118: // Sleeping hamster
 	    			iv.setClickable(true);
 	    			iv.setOnClickListener(new OnClickListener(){
 	    		        @Override
 	    		        public void onClick(View v) {
 							if (getRandMatch(15)) {
 								loadScene("gloomA");
 							} else {
 								loadScene("akubi");
 							}
 	    		        }
 	    			});
     				break;
 			}
 			
 			Bitmap bm = BitmapFactory.decodeResource(getResources(), imageResID);
 			BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
 			bd.setFilterBitmap(false);
 			int width = bd.getIntrinsicWidth();
 			int height = bd.getIntrinsicHeight();
 			
 			iv.setImageDrawable(bd);
 			
 			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
 			params.leftMargin = sprite.getInt(1) * 2;
 			params.topMargin = sprite.getInt(2) * 2;
 			
 			iv.setLayoutParams(params);
 			iv.setVisibility(View.VISIBLE);
         }
 	}
 	
 
     private void loadScene(String n) {
     	ticker.removeCallbacks(tick);
     	ticker.removeCallbacks(soundTimeout);
 		if (mediaPlayer instanceof MediaPlayer) {
 			mediaPlayer.release();
 			mediaPlayer = null;
 		}
     	
     	try {
     		currentFrame = data.getJSONObject("scenes").getInt(n);
     		Log.i(TAG, "Scene "+n+": Loaded");
     		
     		if (debug) {
     			Toast.makeText(this, n, Toast.LENGTH_SHORT).show();
     		}
 
     		checkFPS(currentFrame);
 			ticker.postDelayed(tick, 1000 / framesPerSecond);
 			loadImageFrame(currentFrame);
 			loadSoundFrame(currentFrame);
 	
     	} catch (JSONException e) {
     		Log.i(TAG, "Scene "+n+": Not found");
     	}
     }
     
     
     private boolean checkEvent(int n) {
     	switch (n) {
 			case 14: // start0
 				if (getRandMatch(10)) {
 					loadScene("hello2");
 				} else {
 					loadScene("start0");
 				}
 				break;
 			case 44: // s1
 				if (getRandMatch(15)) {
 					loadScene("go");
 				} else {
 					loadScene("sitA");
 				}
 				break;
 			case 73: // s2
 				if (getRandMatch(15)) {
 					loadScene("sB2A");
 				} else {
 					loadScene("sitB");
 				}
 				break;
 			case 102: // s3
 				if (getRandMatch(15)) {
 					loadScene("gloomA");
 				} else {
 					loadScene("sleepA");
 				}
 				break;
 			case 110: // end1
 				loadScene("hello2");
 				break;
 			case 130: // eAN - quit dialog
 				loadScene("eAN");
 				break;
 			case 136: // end1A - quit selected
 				loadScene("end2A");
 				break;
 			case 141: // end1B - open selected
 				loadScene("start0");
 				break;
 			case 151: // end2A
 				loadScene("end2A");
 				break;
 			case 155: // mmd.logo
 				break;
 			case 161: // st_dmy
 				break;
 			case 177: // sitA
 				switch (getRand(15)) {
 					case 5:
 						loadScene("sitC");
 						break;
 					case 10:
 						loadScene("sA2B");
 						break;
 					default:
 						loadScene("sitA");
 				}
 				break;
 			case 192: // sA2B
 				loadScene("sitB");
 				break;
 			case 207: // sB2A
 				loadScene("sitA");
 				break;
 			case 220: // sitB
 				switch (getRand(15)) {
 					case 5:
 						int rand = getRand(4);
 						loadScene( (rand == 4) ? "lunch" : "lunch"+getRand(3) );
 						break;
 					case 10:
 						loadScene("sB2A");
 						break;
 					default:
 						loadScene("sitB");
 				}
 				break;
 			case 233: // sitC
 				switch (getRand(25)) {
 					case 5:
 						loadScene("nobiA");
 						break;
 					case 10:
 						loadScene("walkA");
 						break;
 					case 15:
 						loadScene("akubi");
 						break;
 					case 20:
 						loadScene("gloomA");
 						break;
 					default:
 						loadScene("sitC");
 				}
 				break;
 			case 245: // walkA
 				if (getRandMatch(15)) {
 					loadScene("gloomB");
 				}
 				break;
 			case 311: // w-4
 				if (getRandMatch(15)) {
 					loadScene("return");
 				}
 				break;
 			case 348: // w-7
 				loadScene("walkA");
 				break;
 			case 369: // go
 				loadScene("g-1");
 				break;
 			case 376: // g-1
 				if (getRandMatch(15)) {
 					loadScene("gloomB");
 				} else {
 					loadScene("w-1");
 				}
 				break;
 			case 403: // nobiA
 				if (getRandMatch(15)) {
 					loadScene("gloomB");
 				} else {
 					loadScene("g-1");
 				}
 				break;
 			case 426: // return
 				loadScene("sitA");
 				break;
 			case 442: // look
 				if (getRandMatch(15)) {
 					loadScene("sB2A");
 				} else {
 					loadScene("sitB");
 				}
 				break;
 			case 544: // lunch
 				if (getRandMatch(15)) {
 					loadScene("sB2A");
 				} else {
 					loadScene("sitB");
 				}
 				break;	
 			case 555: // sleepA
 				switch (getRand(15)) {
 					case 5:
 						loadScene("akubi");
 						break;
 					case 10:
 						loadScene("gloomA");
 						break;
 					default:
 						loadScene("sleepA");
 				}
 				break;
 			case 636: // akubi
 				loadScene("gloomA");
 				break;
 			case 674: // gloomA
 				loadScene("sitA");
 				break;
 			case 786: // gloomB
 				loadScene("w-2");
 				break;
 			case 816: // food1
 				loadScene("eatA");
 				break;
 			case 845: // food2
 				loadScene("eatA");
 				break;
 			case 874: // food3
 				loadScene("eatA");
 				break;
 			case 1189: // eatA
 				if (getRandMatch(15)) {
 					loadScene("sB2A");
 				} else {
 					loadScene("sitB");
 				}
 				break;
 			case 1269: // lunch1
 				if (getRandMatch(15)) {
 					loadScene("sB2A");
 				} else {
 					loadScene("sitB");
 				}
 				break;
 			case 1343: // lunch2
 				if (getRandMatch(15)) {
 					loadScene("sB2A");
 				} else {
 					loadScene("sitB");
 				}
 				break;
 			case 1422: // lunch3
 				if (getRandMatch(15)) {
 					loadScene("sB2A");
 				} else {
 					loadScene("sitB");
 				}
 				break;
 			case 1508: // hello1 (basket closed)
 				loadScene("eAN");
 				break;
 			case 1605: // hello2 (waiting for basket open)
 				loadScene("start0");
 				break;
 			default:
 				return false;
 		}
 		
 		return true;
     }
     
     private void checkFPS(int n) {
     	switch (n) {
 			case 25: // open sequence
 			case 54:
 			case 83:
 				framesPerSecond = 30;
 				break;
 			case 1056: // run sequence
 				framesPerSecond = 15;
 				break;
 			case 44: // sequence end
 			case 73:
 			case 102:
 			case 1068:
 				framesPerSecond = 5;
 				break;
     	}
     }
     
 }
