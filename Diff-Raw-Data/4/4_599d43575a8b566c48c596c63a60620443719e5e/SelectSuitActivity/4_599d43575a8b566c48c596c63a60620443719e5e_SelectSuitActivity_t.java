 package cs309.a1.player.activities;
 
 import static cs309.a1.crazyeights.Constants.SUIT_CLUBS;
 import static cs309.a1.crazyeights.Constants.SUIT_DIAMONDS;
 import static cs309.a1.crazyeights.Constants.SUIT_HEARTS;
import static cs309.a1.crazyeights.Constants.SUIT_SPADES;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import cs309.a1.player.R;
 
 public class SelectSuitActivity extends Activity{
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.selectsuit);
 		
 		Button spade = (Button) findViewById(R.id.Spades);
 		spade.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				setResult(SUIT_SPADES);
 				finish();
 			}
 		});
 		Button heart = (Button) findViewById(R.id.Hearts);
 		heart.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				setResult(SUIT_HEARTS);
 				finish();
 			}
 		});
 		
 		Button club = (Button) findViewById(R.id.Clubs);
 		club.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				setResult(SUIT_CLUBS);
 				finish();
 			}
 		});
 		
 		Button diamond = (Button) findViewById(R.id.Diamonds);
 		diamond.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				setResult(SUIT_DIAMONDS);
 				finish();
 			}
 		});
 	}
 
 }
