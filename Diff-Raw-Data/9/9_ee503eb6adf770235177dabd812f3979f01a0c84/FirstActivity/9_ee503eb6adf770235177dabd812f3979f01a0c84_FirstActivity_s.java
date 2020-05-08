 package com.example;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 import static java.lang.Long.parseLong;
 
 
 public class FirstActivity extends Activity {
     private long arg1;
     private boolean isCounted = false;
 
     private enum Action {SUM, MINUS, MULTIPLY, DIVISION}
 
     private Action action;
     private TextView txt, txtHistory;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         txt = (TextView) findViewById(R.id.text);
         txtHistory = (TextView) findViewById(R.id.history);
     }
 
 
 
 
 
     public void NumButtonAction(View view) {
         int value;
 
         if (isCounted) {
             clearAction();
             isCounted = false;
         }
 
        NumButton NumButon = (NumButton) view;
        value = NumButon.getValue();
 
 
        txt.append(String.valueOf(value));
        txtHistory.append(String.valueOf(value));
 
     }
 
     public void actionButton(View view) {
 
         String string = null;
 
         if (action == null)
             if (txt.getText().length() != 0) {
 
                 arg1 = parseLong(txt.getText().toString());
 
                 switch (view.getId()) {
                     case R.id.sum:
                         action = Action.SUM;
                         string = "+";
                         break;
                     case R.id.minus:
                         action = Action.MINUS;
                         string = "-";
                         break;
                     case R.id.multiply:
                         action = Action.MULTIPLY;
                         string = "×";
                         break;
                     case R.id.division:
                         action = Action.DIVISION;
                         string = "/";
                         break;
             }
 
         txt.setText(null);
         txtHistory.setText(txtHistory.getText() + string);
 
         }
     }
 
     public void summaryButton(View view) {
 
         String sResult = null;
 
         if (action == Action.DIVISION && !isCounted) {
             sResult = Double.toString((double) arg1 / parseLong(txt.getText().toString()));
             txtHistory.setText(txtHistory.getText() + "=" + String.format("%.2f", Double.parseDouble(sResult)));
             txt.setText(String.format("%.2f", Double.parseDouble(sResult)));
         }
 
         else
         {
 
         if (action == Action.SUM && !isCounted) {
             sResult = Long.toString(arg1 + parseLong(txt.getText().toString()));
         }
 
         if (action == Action.MINUS && !isCounted) {
             sResult = Long.toString(arg1 - parseLong(txt.getText().toString()));
         }
 
         if (action == Action.MULTIPLY && !isCounted) {
             sResult = Long.toString(arg1 * parseLong(txt.getText().toString()));
         }
 
         txtHistory.setText(txtHistory.getText() + "=" + sResult);
         txt.setText(sResult);
 
         }
 
         isCounted = true;
         action = null;
 
     }
 
     public void clearButton(View view) {
         clearAction();
     }
 
 
     private void clearAction() {
 
         arg1 = 0;
         action = null;
 
         txt.setText(null);
         txtHistory.setText(null);
     }
 
 }
