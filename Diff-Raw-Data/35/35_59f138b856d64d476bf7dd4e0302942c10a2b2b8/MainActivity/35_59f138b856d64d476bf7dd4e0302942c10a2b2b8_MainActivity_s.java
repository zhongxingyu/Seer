 package ru.ifmo.rain.evdokimov;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener {
 //	class MainView extends View {
 //		public MainView(Context context) {
 //			super(context);
 //		}
 //		
 //		@Override
 //		public void onDraw(Canvas canvas) {
 //			Paint paint = new Paint();
 //			paint.setColor(Color.YELLOW);
 //			canvas.drawRect(10, 10, 110, 110, paint);
 //		}
 //	}
 	Button btnTranslate;
 	TextView tView;
 	EditText eText;
 	boolean f = true;
 	
 	// stub for translate
 	private String getTranslate(String s) {
 		return s;
 	}
 	
 	// stub for pictures
 	private ArrayList<String> getPicturesUrl(String s) {
 		ArrayList<String> res = new ArrayList<String>();
 		res.add("http://grandwallpapers.net/wallpapers/igraushii-kotenok/igraushii-kotenok_1920x1200.jpg");
 		return res;
 	}
 	
 	
 	// clear input form
 	@Override
 	public void onResume() {
 		super.onResume();
 		eText.setText("");
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
         
         btnTranslate = (Button)findViewById(R.id.button1);
         tView = (TextView)findViewById(R.id.textView1);
         eText = (EditText)findViewById(R.id.editText1);
         
         btnTranslate.setOnClickListener(this);
     }
 
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.button1:
 			Intent intent = new Intent(this, TranslateActivity.class);
 			startActivity(intent);
 			break;
 		default:
 			break;
 		}
 	}
 }
