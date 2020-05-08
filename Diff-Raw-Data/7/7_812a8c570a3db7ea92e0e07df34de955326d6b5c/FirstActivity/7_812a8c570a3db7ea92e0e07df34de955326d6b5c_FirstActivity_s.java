 package com.example;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class FirstActivity extends Activity
 {
     private int arg1, action; // action: sum=1, minus=2, multiply=3, division=4
     private boolean isCounted=false;
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 
 
     public void numButton(View view) {
         String string="";
         TextView txt = (TextView) findViewById(R.id.text);
         TextView txtHistory = (TextView)findViewById(R.id.history);
          if (this.isCounted){
              this.clearAction();
              this.isCounted=false;
          }
         switch (view.getId()) {
             case R.id.num1:
                 string = txt.getText() + Integer.toString(1);
                 break;
             case R.id.num2:
                 string = txt.getText() + Integer.toString(2);
                 break;
             case R.id.num3:
                 string = txt.getText() + Integer.toString(3);
                 break;
             case R.id.num4:
                 string = txt.getText() + Integer.toString(4);
                 break;
             case R.id.num5:
                 string = txt.getText() + Integer.toString(5);
                 break;
             case R.id.num6:
                 string = txt.getText() + Integer.toString(6);
                 break;
             case R.id.num7:
                 string = txt.getText() + Integer.toString(7);
                 break;
             case R.id.num8:
                 string = txt.getText() + Integer.toString(8);
                 break;
             case R.id.num9:
                 string = txt.getText() + Integer.toString(9);
                 break;
             case R.id.num0:
                 string = txt.getText() + Integer.toString(0);
                 break;
         }
         txt.setText(string);
         txtHistory.setText(txtHistory.getText() + string);
 
     }
 
     public void actionButton(View view){
         String string="";
         TextView txtHistory = (TextView)findViewById(R.id.history);
         TextView txt = (TextView) findViewById(R.id.text);
         arg1 = Integer.parseInt((String) txt.getText());
         switch (view.getId()){
             case R.id.sum:
                 action = 1;
                 string = "+";
                 break;
             case R.id.minus:
                 action = 2;
                 string = "-";
                 break;
             case R.id.multiply:
                 action = 3;
                 string = "*";
                 break;
             case R.id.division:
                 action = 4;
                 string = "/";
                 break;
         }
         txt.setText("");
         txtHistory.setText(txtHistory.getText() + string);
     }
 
     public void summaryButton(View view){
         String sResult = "";
         TextView txt = (TextView) findViewById(R.id.text);
         TextView txtHistory = (TextView)findViewById(R.id.history);
 
 
         if (action == 1) {
             sResult =    Integer.toString(arg1+Integer.parseInt((String) txt.getText()));
             txt.setText(sResult);
             action = 0;
         }
         if (action == 2) {
             sResult =  Integer.toString(arg1-Integer.parseInt((String) txt.getText()));
             txt.setText(sResult);
             action = 0;
         }
         if (action == 3) {
             sResult =    Integer.toString(arg1*Integer.parseInt((String) txt.getText()));
             txt.setText(sResult);
             action = 0;
         }
         if (action == 4) {
             sResult =    Integer.toString(arg1/Integer.parseInt((String) txt.getText()));
             txt.setText(sResult);
             action = 0;
         }
         this.isCounted = true;
        txtHistory.setText(txtHistory.getText() + "=" + sResult );
     }
 
     public void clearButton(View view){
           this.clearAction();
     }
 
 
     private void clearAction(){
         TextView txt = (TextView) findViewById(R.id.text);
         TextView txtHistory = (TextView)findViewById(R.id.history);
         arg1 = action = 0;
         txt.setText("");
         txtHistory.setText("");
     }
 
 }
