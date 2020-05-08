 package third.example.userinterfacedesigns;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		RelativeLayout bkgr = (RelativeLayout)findViewById(R.id.uilayout);
 		ImageView image = (ImageView)findViewById(R.id.imageView1);
 		
 		switch (item.getItemId()) {
 		
 		case R.id.buttonone:
 			image.setImageResource(R.drawable.image1);
 			return true;
 		case R.id.buttontwo:
 			image.setImageResource(R.drawable.image2);
 			return true;
 		case R.id.buttonthree:
			bkgr.setBackgroundResource(R.color.background);
 			return true;
 		case R.id.buttonfour:
			bkgr.setBackgroundResource(R.color.background2);
 			return true;
 		case R.id.buttonfive:
 			// The Alert Code For Next Section Goes Here!
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
 
