 package cs309.a1.player.activities;
 
 import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
 import cs309.a1.player.R;
 
 public class ConnectActivity extends Activity{
 
 	@Override
 	protected void onStart() {
 		super.onStart();
		setContentView(R.layout.connect);
 
		Button connectButton = (Button) findViewById(R.id.connectButton);
		connectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ConnectActivity.this, ShowCardsActivity.class);
				startActivity(i);
			}
		});
 
 	}
 }
