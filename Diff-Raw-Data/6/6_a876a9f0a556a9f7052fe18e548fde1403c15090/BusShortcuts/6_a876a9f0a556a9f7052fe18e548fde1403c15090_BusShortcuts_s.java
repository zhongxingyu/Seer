 /**
  * 
  */
 package net.neoturbine.autolycus;
 
 import net.neoturbine.autolycus.providers.Routes;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * @author Joseph Booker
  *
  */
 public class BusShortcuts extends Activity {
 	private static final int PICK_STOP = 1;
 	private String system;
 	private String route;
 	private String direction;
 	private String stopname;
 	private int stopid;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
         final Intent intent = getIntent();
         final String action = intent.getAction();
 		
 		if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
 			Intent stopintent = new Intent(Intent.ACTION_PICK,Routes.CONTENT_URI);
 			startActivityForResult(stopintent, PICK_STOP);
 		} else {
 			setResult(RESULT_CANCELED);
 			finish();
 		}
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == PICK_STOP) {
 			 if(resultCode == RESULT_OK) {
 				 setContentView(R.layout.shortcut);
 				 system = data.getStringExtra(SelectStop.EXTRA_SYSTEM);
 				 route = data.getStringExtra(SelectStop.EXTRA_ROUTE);
 				 direction = data.getStringExtra(SelectStop.EXTRA_DIRECTION);
 				 stopid = data.getIntExtra(SelectStop.EXTRA_STOPID,-1);
 				 stopname = data.getStringExtra(SelectStop.EXTRA_STOPNAME);
 				 
				 ((TextView)findViewById(R.id.shortcut_stop)).setText(
						 system + " Stop \"" + stopname +"\" (" + direction +") on Route "+route);
 				 ((EditText)findViewById(R.id.shortcut_stopname)).setText(stopname);
 				 
 				 ((Button)findViewById(R.id.shortcut_submit))
 				 	.setOnClickListener(new View.OnClickListener(){
 				 		public void onClick(View v) {
 				 			returnShortcut();
 				 		}
 				 	});
 			 }
 		}
 	}
 	
 	public void returnShortcut() {
 		Intent shortcutIntent =  new Intent();
 		shortcutIntent.setAction(StopPrediction.OPEN_STOP_ACTION);
 		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		shortcutIntent
 			.putExtra(SelectStop.EXTRA_SYSTEM, system)
 			.putExtra(SelectStop.EXTRA_ROUTE, route)
 			.putExtra(SelectStop.EXTRA_DIRECTION, direction)
 			.putExtra(SelectStop.EXTRA_STOPNAME, stopname)
 			.putExtra(SelectStop.EXTRA_STOPID, stopid);
 		
 		Intent intent = new Intent();
 		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
 		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
 				((TextView)findViewById(R.id.shortcut_stopname)).getText().toString());
 		Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
 				this, R.drawable.icon);
 		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
 		
 		setResult(RESULT_OK, intent);
 		finish();
 	}
 	
 }
