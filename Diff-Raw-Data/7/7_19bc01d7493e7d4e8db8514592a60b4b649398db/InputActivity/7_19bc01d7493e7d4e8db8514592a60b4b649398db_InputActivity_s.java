 package com.example.pincrack;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.List;
 import computation.Computer;
 import computation.Digit;
 import com.example.pincrack.R;
 import dialogs.PINValidatorDialog;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 /**
  * This activity is used to receive and process the user input (PIN).
  * It then issues the call to the Computer to guess the PIN. 
  * 
  * @author Jay Wang
  */
 public class InputActivity extends Activity {
 
     @Override
     public void onCreate (Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.input_two);
         
         
         final int[] confidence_array = new int[4];        
         final Digit digit1 = new Digit((EditText) findViewById(R.id.phone_dialer1), (SeekBar) findViewById(R.id.seek1), (TextView) findViewById(R.id.confidence_text1));
         final Digit digit2 = new Digit((EditText) findViewById(R.id.phone_dialer2), (SeekBar) findViewById(R.id.seek2), (TextView) findViewById(R.id.confidence_text2));
         final Digit digit3 = new Digit((EditText) findViewById(R.id.phone_dialer3), (SeekBar) findViewById(R.id.seek3), (TextView) findViewById(R.id.confidence_text3));
         final Digit digit4 = new Digit((EditText) findViewById(R.id.phone_dialer4), (SeekBar) findViewById(R.id.seek4), (TextView) findViewById(R.id.confidence_text4));
         final List<Digit> digits = Arrays.asList(digit1, digit2, digit3, digit4);
         
         setConfidence(digits, confidence_array);
         
         Button submit = (Button) findViewById(R.id.button3);
         submit.setOnClickListener(new View.OnClickListener()
         {
             public void onClick (View v)
             {
                 InputStream inputStream;
                 try {
             		final String guessedPin = concatenateGuesses(digits);
             		
                     if (validationPassed(guessedPin)) {
                         inputStream = getAssets().open("orderings.txt");
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 						Computer.calculate(Integer.parseInt(guessedPin),
 								reader, getApplicationContext(),
 								confidence_array);
 						Log.i("TAG", "Got here...");
                         Intent resultIntent =
                                 new Intent(InputActivity.this, ResultActivity.class);
                         InputActivity.this.startActivity(resultIntent);
                         finish();
                     }
                     else {
                         PINValidatorDialog dialog = new PINValidatorDialog();
                         dialog.show(getFragmentManager(), "error Dialog");
                     }
                 }
                 catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     /**
      * Helper method used to validate the PIN is 4 digits long
      */
     protected boolean validationPassed (String string) {
         if (string.length() == 4) return true;
         return false;
     }
     
     /**
      * Helper method that handles the confidence for each digit of the user-guessed PIN
      */
     private void setConfidence(List<Digit> digits, final int[] confidence_array) {
        for (final Digit digit : digits) {
         	digit.getTextView().setText("Confidence: 0");
             digit.getSeekBar().setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
             	 
                 public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                     digit.getTextView().setText("Confidence: " + progress);
                    confidence_array[0] = progress;
                 }
 
     			@Override
     			public void onStartTrackingTouch(SeekBar arg0) {}
 
     			@Override
     			public void onStopTrackingTouch(SeekBar seekBar) {}
             });
         }
     }
     
     /**
      * Concatenate the four individual digits into one string
      */
 	private String concatenateGuesses(List<Digit> digits) {
 		return digits.get(0).getEditText().getText().toString()
 				+ digits.get(1).getEditText().getText().toString()
 				+ digits.get(2).getEditText().getText().toString()
 				+ digits.get(3).getEditText().getText().toString();
 	}
 
 }
