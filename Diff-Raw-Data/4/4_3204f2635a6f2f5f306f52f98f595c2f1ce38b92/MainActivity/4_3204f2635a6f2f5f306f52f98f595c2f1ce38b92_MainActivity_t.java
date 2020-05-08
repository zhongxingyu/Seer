 package leaf.ttree.lifecounter;
 
 import android.os.Bundle;
import android.os.SystemClock;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Chronometer;
 
 public class MainActivity extends Activity {
 
 	Chronometer chrono;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		chrono = (Chronometer) this.findViewById(R.id.chronometer);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void clickStartPauseButton(View v) {
		chrono.setBase(SystemClock.elapsedRealtime());
 		chrono.start();
 
 		Button startPauseButton = getStartStopButton();
 		startPauseButton.setText(R.string.pause_button);
 		startPauseButton.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.pause_button));
 		int padding = getResources().getDimensionPixelSize(R.dimen.padding);
 		startPauseButton.setPadding(padding, padding, padding, padding);
 	}
 
 	public Button getStartStopButton() {
 		return (Button) this.findViewById(R.id.start_pause_button);
 
 	}
 
 }
