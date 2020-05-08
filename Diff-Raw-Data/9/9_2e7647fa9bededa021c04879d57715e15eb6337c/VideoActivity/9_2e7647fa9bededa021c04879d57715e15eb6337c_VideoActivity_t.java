 package eu.isweb.animeplayer;
 
 import java.util.ArrayList;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.ActionBar.OnMenuVisibilityListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnSystemUiVisibilityChangeListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.WindowManager.LayoutParams;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.VideoView;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class VideoActivity extends Activity {
 
 	protected static AlertDialog alertDialog;
 	int mLastSystemUiVis;
 	AnimeMediaController mc;
 	Video video;
 	Anime anime;
 	AnimeDatabaseManager db;
 	Context instance;
 	static ProgressBar mProgress;
 	VideoView videoView;
 	
 	void setNavVisibility(boolean visible) {
 		Log.d("JD", "setNavVisibility("+visible+")");
 		int newVis = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
 		if (!visible) {
 			newVis |= View.SYSTEM_UI_FLAG_LOW_PROFILE
 					| View.SYSTEM_UI_FLAG_FULLSCREEN
 					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
 		}
 		getWindow().getDecorView().setSystemUiVisibility(newVis);
 		mc.toggle(visible);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			videoView.stopPlayback();
 			finish();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.video, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.video_jump:
 			AlertDialog.Builder builder;
 
 			Context mContext = getApplicationContext();
 			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layout = inflater.inflate(R.layout.video_jump, (ViewGroup) findViewById(R.id.layout_root));
 			
 			final Button jump = (Button) layout.findViewById(R.id.jump);
 			final SeekBar sb = (SeekBar) layout.findViewById(R.id.seekBar);
 			sb.setMax(3*60);
 			sb.setProgress(db.getJumpTime(anime.URL));
 			jump.setText(getString(R.string.jump) + " +" + displayTime(sb.getProgress()) + " min");
 
 			sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 					jump.setText(getString(R.string.jump)+" +" + displayTime(progress) + " min");
 				}
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {}
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {}
 			});
 			
 			jump.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					db.setJumpTime(anime.URL, sb.getProgress());
					videoView.seekTo(videoView.getCurrentPosition() + sb.getProgress()*1000);
 					videoView.start();
 					VideoActivity.alertDialog.dismiss();
 				}
 			});
 					
 			builder = new AlertDialog.Builder(this);
 			builder.setView(layout);
 			alertDialog = builder.create();
 			alertDialog.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	private String displayTime(int sec) {
 		int min = sec / 60;
 		sec = sec - (min*60);
 		return min + ":" + (sec<10?("0"+sec):sec);
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Bundle extras = getIntent().getExtras();
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 //        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 //		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
 //		getActionBar().setBackgroundDrawable(null);
 		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
         getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
         getActionBar().setBackgroundDrawable(null);
         
         setContentView(R.layout.activity_video);
 
         instance = this;
         db = ((AnimeApp) getApplication()).getDB();
         
         mProgress = (ProgressBar)findViewById(R.id.progress);
         videoView = (VideoView)findViewById(R.id.video);
         
         mc = new AnimeMediaController(this);
         mc.setAnchorView(videoView);
         mc.setMediaPlayer(videoView);
         mc.setOnHideListener(new OnHideListener() {
 			@Override
 			public void onHide() {
 				Log.d("JD", "hide jo jo");
 				setNavVisibility(false);
 			}
 		});
         videoView.setMediaController(mc);
         videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
 			@Override
 			public void onPrepared(MediaPlayer mp) {
 				setNavVisibility(false);
 				mProgress.setVisibility( View.INVISIBLE );
 				mp.start();
 			}
 		});
 
         View view = findViewById(R.id.video_player_layout);
 		view.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
 			@Override
 			public void onSystemUiVisibilityChange(int visibility) {
 				int diff = mLastSystemUiVis ^ visibility;
 				mLastSystemUiVis = visibility;
 				if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
 						&& (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
 					setNavVisibility(true);
 				}
 			}
 		});
         
 		if (extras != null) {
 			video = (Video) extras.getSerializable("video");
 			anime = (Anime) extras.getSerializable("anime");
 			Epizode e = (Epizode) extras.getSerializable("epizode");
 			
 			setTitle(anime.name + " : " + e.name);
 
 			if(video.type.equals(Video.TYPE_VK)) {
 				new AnimeDownloader<Video>() {
 					@Override
 					protected void onPostExecuteAction(final ArrayList<Video> result) {
 						AlertDialog.Builder builder = new AlertDialog.Builder(instance);
 						
 						if(result.size() == 0) {
 							builder.setMessage("Error, can not find valid video url")
 						       .setCancelable(false)
 						       .setNeutralButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
 						           @Override
 								public void onClick(DialogInterface dialog, int id) {
 						                VideoActivity.this.finish();
 						           }
 						       });
 							AlertDialog alert = builder.create();
 							alert.show();
 							return;
 						}
 						
 						ArrayList<String> stringResult = new ArrayList<String>();
 						for(Video item : result)
 							stringResult.add(item.name);
 						
 						//select quality
 						builder.setTitle("Select quality");
 						builder.setItems(stringResult.toArray(new CharSequence[stringResult.size()]), new DialogInterface.OnClickListener() {
 						    @Override
 							public void onClick(DialogInterface dialog, int item) {
 						    	videoView.setVideoURI(Uri.parse(result.get(item).URL));
 						        videoView.requestFocus();
 						        videoView.start();  
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 						}
 
 					@Override
 					protected ArrayList<Video> doInBackgroundAction(Document doc) {
 						ArrayList<Video> result = new ArrayList<Video>();
 						
 						Elements elements = doc.select("source[src]");
 						for (Element element : elements) {
 							String split[]= element.attr("src").split("\\.");
 							if(split.length == 0) continue;
 							
 						    result.add(new Video( split[split.length-2] + "p" , element.attr("src"), Video.TYPE_VK ));
 						}
 						
 						return result;
 					};
 				}.execute(video.URL);
 			}else{
 				videoView.setVideoURI(Uri.parse(video.URL));
 		        videoView.requestFocus();
 		        videoView.start();  
 			}
 		}
     }
 	
 }
