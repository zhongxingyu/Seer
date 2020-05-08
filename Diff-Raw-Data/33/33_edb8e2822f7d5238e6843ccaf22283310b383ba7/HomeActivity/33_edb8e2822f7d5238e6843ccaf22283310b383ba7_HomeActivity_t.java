 package com.example;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
 
public class HomeActivity extends Activity implements TextView.OnEditorActionListener {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        EditText issueNumber = (EditText) findViewById(R.id.issueNumber);
        issueNumber.setOnEditorActionListener(this);
    }

    public void onCameraCapture(View view) {
        Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
    }

    public void onKeyboardCapture(View view) {
        Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
    }

    public void onGestureCapture(View view) {
        Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            Toast.makeText(this, "Search jira", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
     }
 }
