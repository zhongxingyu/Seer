 package tw.edu.ntust.dt.herbal2.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import tw.edu.ntust.dt.herbal2.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.Menu;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 
 public class AskActivity extends Activity {
 
 	// private final static Map<Integer, List<Integer>> edge = new
 	// HashMap<Integer, List<Integer>>();
 
 	private final static SparseArray<List<Integer>> edge = new SparseArray<List<Integer>>();
 
 	private static Animation animationFadeIn;
 	private static Animation animationFadeIn2;
 
 	private ImageView askQ, askYes, askNo;
 
 	private int[][] ask = new int[][] {
 			{ R.drawable.ask_0, R.drawable.ask_0_yes, R.drawable.ask_0_no },
 			{ R.drawable.ask_1, R.drawable.ask_1_yes, R.drawable.ask_1_no },
 			{ R.drawable.ask_2, R.drawable.ask_2_yes, R.drawable.ask_2_no },
 			{ R.drawable.ask_3, R.drawable.ask_3_yes, R.drawable.ask_3_no },
 			{ R.drawable.ask_4, R.drawable.ask_4_yes, R.drawable.ask_4_no },
 			{ R.drawable.ask_5, R.drawable.ask_5_yes, R.drawable.ask_5_no },
 			{ R.drawable.ask_6, R.drawable.ask_6_yes, R.drawable.ask_6_no },
 			{ R.drawable.ask_7, R.drawable.ask_7_yes, R.drawable.ask_7_no } };
 
 	static {
 		for (int i = 0; i < 8 * 3; i++) {
 			edge.put(i, new ArrayList<Integer>());
 		}
 		edge.get(0).add(5);
 		edge.get(0).add(4);
 
 		edge.get(5).add(1);
 		edge.get(5).add(8 + 4);
 
 		edge.get(1).add(R.drawable.result_yenyen);
 		edge.get(1).add(6);
 
 		edge.get(6).add(R.drawable.result_jie);
 		edge.get(6).add(R.drawable.result_ya);
 
 		edge.get(8 + 4).add(7);
 		edge.get(8 + 4).add(2);
 
 		edge.get(7).add(3);
 		edge.get(7).add(6);
 
 		edge.get(3).add(R.drawable.result_nee);
 		edge.get(3).add(R.drawable.result_yenyen);
 
 		edge.get(6).add(R.drawable.result_jie);
 		edge.get(6).add(R.drawable.result_ya);
 
 		edge.get(2).add(R.drawable.result_yen);
 		edge.get(2).add(R.drawable.result_jian);
 
 		edge.get(4).add(8 + 7);
 		edge.get(4).add(8 + 3);
 
 		edge.get(8 + 7).add(8 * 2 + 3);
 		edge.get(8 + 7).add(8 + 6);
 
 		edge.get(8 * 2 + 3).add(R.drawable.result_nee);
 		edge.get(8 * 2 + 3).add(R.drawable.result_ya);
 
 		edge.get(8 + 6).add(R.drawable.result_jie);
 		edge.get(8 + 6).add(R.drawable.result_yen);
 
 		edge.get(8 + 3).add(8 * 2 + 2);
 		edge.get(8 + 3).add(8 * 2 + 7);
 
 		edge.get(8 * 2 + 2).add(R.drawable.result_yen);
 		edge.get(8 * 2 + 2).add(R.drawable.result_jian);
 
 		edge.get(8 * 2 + 7).add(R.drawable.result_yen);
 		edge.get(8 * 2 + 7).add(R.drawable.result_nee);
 
 	}
 
 	private int currentState = 0;
 
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_ask);
 
 		askQ = (ImageView) findViewById(R.id.ask_question);
 		askYes = (ImageView) findViewById(R.id.ask_yes);
 		askNo = (ImageView) findViewById(R.id.ask_no);
 
 		animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
 		animationFadeIn2 = AnimationUtils.loadAnimation(this, R.anim.fadein2);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void clickImage(View view) {
 
 		Log.d("debug", "currentState:" + currentState);
 
 		int state;
 		if (view.getId() == R.id.ask_yes) {
 			state = edge.get(currentState).get(0);
 		} else {
 			state = edge.get(currentState).get(1);
 		}
 		currentState = state;
 		if (state < 8 * 3) {
 
 			askQ.setImageResource(ask[state % 8][0]);
 			askYes.setImageResource(ask[state % 8][1]);
 			askNo.setImageResource(ask[state % 8][2]);
 
 			askQ.startAnimation(animationFadeIn);
 			askYes.startAnimation(animationFadeIn2);
 			askNo.startAnimation(animationFadeIn2);
 
 		} else {
 
			SharedPreferences.Editor editor = getSharedPreferences("herbal",
					Context.MODE_PRIVATE).edit();
			editor.putInt("resId", currentState);
			editor.commit();
 
 			Intent intent = new Intent();
 			intent.setClass(this, ResultActivity.class);
 			intent.putExtra("result", currentState);
 			startActivity(intent);
 		}
 	}
 
 }
