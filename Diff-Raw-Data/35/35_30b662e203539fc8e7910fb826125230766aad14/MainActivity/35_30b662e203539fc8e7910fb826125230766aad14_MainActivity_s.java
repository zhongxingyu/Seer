 package com.example.htttppost;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener{
 
 	private Button _sendButton;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         _sendButton = (Button) findViewById(R.id.sendButton);
         assert(_sendButton != null);
         _sendButton.setOnClickListener(this);
         return true;
     }
 
 
 	@Override
 	public void onClick(View arg0) {
		httpService _hs = new httpService();
 		EditText _urlText = (EditText) findViewById(R.id.urlText);
 		EditText _valueText = (EditText) findViewById(R.id.valueText);
		String _url = _urlText.getText().toString();
		String _value = _valueText.getText().toString();
		TextView _result = (TextView) findViewById(R.id.resultTextView);
		_result.setText(_hs.httpGet(_url, _value));	
 	}
 }
