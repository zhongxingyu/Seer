 package fourth.example.graphicdesign;
 
 import android.app.Activity;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity {
 
 	AnimationDrawable logoAnimation;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		final ImageView logoAnimHolder = (ImageView) findViewById(R.id.imageView1);
 		logoAnimHolder.setBackgroundResource(R.drawable.logo_animation);
 		logoAnimHolder.post(new Runnable() {
 			public void run() {
 				logoAnimation = (AnimationDrawable) logoAnimHolder.getBackground();
 			}
 		});
 	}
 
//	@Override
	public void onWindowFocusedChanged (boolean hasFocus) {
 		logoAnimation.start();
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
