 package net.nexustools.steve.randomkeypinunlock.activity;
 
 import java.util.ArrayList;
 
 import net.nexustools.steve.randomkeypinunlock.R;
 import net.nexustools.steve.randomkeypinunlock.listener.LockScreenService;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.app.Activity;
 import android.content.Intent;
 
 public class LockScreenActivity extends Activity {
 	public static final int[] ZERO_TO_NINE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
 	public RelativeLayout layout;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.TYPE_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_SECURE);
 		setContentView(R.layout.activity_lockscreen);
 		layout = (RelativeLayout)findViewById(R.id.randomPINLayout);
 		OnClickListener listener = new OnClickListener() {
 			@Override
             public void onClick(View v) {
 				randomizePINButtons();
             }
 		};
 		for(int i = 0; i < layout.getChildCount() - 1; i++)
 			((Button)layout.getChildAt(i)).setOnClickListener(listener);
 		randomizePINButtons();
		startService(new Intent(this, LockScreenService.class));
 	}
 	
 	public void randomizePINButtons() {
 		ArrayList<Integer> numbersToAdd = new ArrayList<Integer>(ZERO_TO_NINE.length);
 		for(int i : ZERO_TO_NINE)
 			numbersToAdd.add(i);
 		if(numbersToAdd.size() != layout.getChildCount() - 1)
 			throw new RuntimeException("Malformed layout VS numbers size for the pin numbers?");
 		
 		for(int i = 0; i < layout.getChildCount() - 1; i++) {
 			Button b = (Button)layout.getChildAt(i);
 			b.setText("" + numbersToAdd.remove((int)(Math.random() * numbersToAdd.size())));
 		}
 	}
 }
