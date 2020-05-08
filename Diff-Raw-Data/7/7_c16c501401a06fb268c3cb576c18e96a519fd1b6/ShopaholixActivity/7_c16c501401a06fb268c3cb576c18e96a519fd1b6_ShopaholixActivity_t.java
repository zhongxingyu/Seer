 package Shopaholix.main;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 import android.widget.TextView;
 
 public class ShopaholixActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
     }
     public void myButtonPressed(View v){
     	TextView t = (TextView)this.findViewById(R.id.textView1);
    	t.setText("Goodbye 21w.789");
     }
}
