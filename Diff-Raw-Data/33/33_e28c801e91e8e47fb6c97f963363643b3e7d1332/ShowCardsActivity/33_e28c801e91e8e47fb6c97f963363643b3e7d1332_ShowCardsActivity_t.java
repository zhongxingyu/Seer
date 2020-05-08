 package cs309.a1.player.activities;
 
 import android.app.Activity;
 import cs309.a1.player.R;
 
 public class ShowCardsActivity extends Activity{
 
 	@Override
 	protected void onStart() {
 		super.onStart();
		setContentView(R.layout.player_hand);
	}
 
 
 }
