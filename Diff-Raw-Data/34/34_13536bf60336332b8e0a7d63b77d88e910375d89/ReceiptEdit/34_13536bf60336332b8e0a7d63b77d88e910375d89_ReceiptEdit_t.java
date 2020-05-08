 package net.m_kawato.tabletpos;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.content.Intent;
import android.util.Log;
 import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
 
public class ReceiptEdit extends Activity implements OnClickListener {
    private Globals globals;
    private static final String TAG = "ReceiptEdit";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_receipt_edit);

        this.globals = (Globals) this.getApplication();

        // Event handler of button (Top Menu)
        Button btnTopMenu = (Button) findViewById(R.id.btn_topmenu);
        btnTopMenu.setOnClickListener(this);   
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.receipt_edit, menu);
         return true;
     }
 
    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
        case R.id.btn_topmenu:
            Log.d(TAG, "Top Menu button is clicked");
            globals.saveLoading();
            i = new Intent(this, TopMenu.class);
            startActivity(i);
            break;
        case R.id.btn_loading:
        }                
    }

 }
