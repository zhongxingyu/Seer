 package com.cruk.angryhippos;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.ToggleButton;
 
 import com.cruk.angryhippos.data.RawDataProvider;
 import com.cruk.angryhippos.model.DataCluster;
 import com.cruk.angryhippos.views.GameView;
 
 public class GameActivity extends Activity {
 
 	private GameView gameView;
 
 	private View selected = null;
 
 	private int[] hippos = { R.id.hippo1, R.id.hippo2, R.id.hippo3,
			R.id.hippo4, R.id.hippo5, R.id.hippo6, R.id.hippo7,
			R.id.hippo8, R.id.hippo9 };
 
 	private Handler handler;
 	
 	private MediaPlayer nom1;
 	
 	private ToggleButton muteButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		gameView = (GameView) findViewById(R.id.gameView);
		selected = findViewById(R.id.hippo5);
 		muteButton = (ToggleButton) findViewById(R.id.muteButton);
 		nom1 = MediaPlayer.create(this, R.raw.nom1);
 		
 		handler = new Handler(getMainLooper());
 
 		new AsyncTask<Void, Void, List<List<DataCluster>>>() {
 
 			int maxClusterSize;
 			
 			@Override
 			protected List<List<DataCluster>> doInBackground(Void... params) {
 				try {
 					RawDataProvider provider = new RawDataProvider().loadFromFile(GameActivity.this).sort().group(50).reduceData(10);
 					maxClusterSize = provider.getMaxClusterSize();
 					return provider.getClusters();
 				} catch (IOException e) {
 					throw new RuntimeException(e);
 				}
 			}
 
 			@Override
 			protected void onPostExecute(List<List<DataCluster>> result) {
 				gameView.setClusters(result);
 				gameView.setMaxClusterSize(maxClusterSize);
 				findViewById(R.id.loading_layout).setVisibility(View.GONE);
 			}
 		}.execute();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		gameView.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		gameView.onPause();
 	}
 
 	public void hippoAttack(View view) {
 		if (muteButton.isChecked()) {
 			nom1.start();
 		}
 		if (selected != null) {
 			selected.setVisibility(View.INVISIBLE);
 		}
 		selected = findViewById(hippos[Integer.valueOf((String) view.getTag())]);
 		selected.setVisibility(View.VISIBLE);
 		selected.setSelected(true);
 		final View oneTimeView = selected;
 		handler.postDelayed(new Runnable() {
 			
 			@Override
 			public void run() {
 				oneTimeView.setSelected(false);
 			}
 		}, 300);
 	}
 
 }
