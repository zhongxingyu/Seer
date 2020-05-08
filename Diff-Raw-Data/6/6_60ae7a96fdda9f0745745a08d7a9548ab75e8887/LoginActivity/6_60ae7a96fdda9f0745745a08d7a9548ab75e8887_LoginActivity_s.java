 package com.your.worth.controller;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.your.worth.R;
 import com.your.worth.model.AppModel;
 import com.your.worth.model.PIN;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Catalin BORA
  * Date: 11/7/13
  * Time: 10:49 PM
  */
 public class LoginActivity extends Activity {
 
     private ImageView mOKIcon = null;
     private Button mSignIn = null;
     private final Character[] mPIN = new Character[4];
     private AlertDialog mAlertDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
 
         mOKIcon = (ImageView) findViewById(R.id.approve_icon);
         mSignIn = (Button) findViewById(R.id.loginButton);
 
         final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
         final ImageView imageBig = (ImageView) findViewById(R.id.icon_image);
         imageBig.startAnimation(animationFadeIn);
 
         EditText mText1 = (EditText) findViewById(R.id.digit_one);
         EditText mText2 = (EditText) findViewById(R.id.digit_two);
         EditText mText3 = (EditText) findViewById(R.id.digit_tree);
         EditText mText4 = (EditText) findViewById(R.id.digit_four);
 
         mText1.addTextChangedListener(new TextChangeListener(0,mText2));
         mText2.addTextChangedListener(new TextChangeListener(1,mText3));
         mText3.addTextChangedListener(new TextChangeListener(2,mText4));
         mText4.addTextChangedListener(new TextChangeListener(3,mText4));
 
         mText4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_DONE) {
                     signIn(view);
                 }
                 return false;
             }
         });
 
         AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
         alertBuilder.setIcon(R.drawable.worth);
         alertBuilder.setTitle(R.string.forgot_pin);
         alertBuilder.setMessage("This will ERASE your data. Are you sure?");
 
         alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 mAlertDialog.hide();
             }
         });
 
         alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // reset PIN
                 AppModel.getInstance().deactivatePIN();
                 // reset Data
                 AppModel.getInstance().clearLists(getApplicationContext());
 
                 Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                 startActivity(intent);
 
                 mAlertDialog.hide();
 
 
             }
         });
         mAlertDialog = alertBuilder.create();
     }
 
     /**
      * Deactivate the back functionality
      */
     @Override
     public void onBackPressed() {
        // do nothing
     }
 
     public void showPopup(View view) {
         mAlertDialog.show();
     }
 
     public void signIn(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
     }
 
     /**
      * Validate the PIN entry
      * @param value the digit in a specific position
      * @param place the position of the value
      */
     private void validatePIN(Character value, int place) {
         mPIN[place]=value;
         if(PIN.isPINComplete(mPIN)){
             if(PIN.isPINOk(mPIN)){
                 mOKIcon.setVisibility(View.VISIBLE);
                 mOKIcon.setImageResource(R.drawable.pin_good);
                 mSignIn.setVisibility(View.VISIBLE);
             } else {
                 mOKIcon.setVisibility(View.VISIBLE);
                 mOKIcon.setImageResource(R.drawable.pin_bad);
                 mSignIn.setVisibility(View.GONE);
             }
         } else {
             mOKIcon.setVisibility(View.GONE);
             mSignIn.setVisibility(View.GONE);
         }
     }
 
     /**
      * Listener for all the text fields
      */
     public class TextChangeListener implements TextWatcher {
 
         private int mPos;
         private EditText mNextText;
 
         private TextChangeListener(int pos,EditText textToSelectNext){
             mPos = pos;
             mNextText = textToSelectNext;
         }
 
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
         }
 
         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
             if(charSequence.length()!=0){
                 validatePIN(charSequence.charAt(0), mPos);
                 mNextText.requestFocus();
                 mNextText.selectAll();
             } else {
                 validatePIN(null, mPos);
             }
 
 
         }
 
         @Override
         public void afterTextChanged(Editable editable) {
         }
 
 
     }
 }
