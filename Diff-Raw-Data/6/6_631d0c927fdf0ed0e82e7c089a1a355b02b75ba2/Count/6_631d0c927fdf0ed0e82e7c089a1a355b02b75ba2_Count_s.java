 package ch.expectusafterlun.androidtutorial;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Count extends Activity {
 
	private int counter;
 	private Button add, sub;
 	private TextView display;
 	private String total;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.count);
        counter = 0;
         add = (Button) findViewById(R.id.b_add);
         sub = (Button) findViewById(R.id.b_sub);
         display = (TextView) findViewById(R.id.tv_display);
         total = "Your total is ";
         add.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				display.setText(total + ++counter);
 			}
 		});
         sub.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				display.setText(total + --counter);
 			}
 		});
     }
 }
