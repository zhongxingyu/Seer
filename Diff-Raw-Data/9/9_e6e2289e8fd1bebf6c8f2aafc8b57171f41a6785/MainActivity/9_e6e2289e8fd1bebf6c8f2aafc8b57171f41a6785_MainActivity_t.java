 package com.example.calculator;
 
 import java.util.ArrayList;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 	private EditText text;
 	private Calculator calc;
 	private Integer first, sec;
 	private ArrayList<Integer> hist;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         text = (EditText)findViewById(R.id.Row1_ShowInput);
         
         final Button[] b = new Button[16];
         final int[] ids = {R.id.button0,R.id.button1,R.id.button2,R.id.button3,R.id.button4,R.id.button5,
         		R.id.button6,R.id.button7,R.id.button8,R.id.button9,R.id.buttonADD,R.id.buttonMINUS,
         		R.id.buttonDIVIDE,R.id.buttonMULTIPLY,R.id.buttonClear,R.id.buttonEQUALS};
         
         
         for (int i = 0; i < b.length; i++) {
         	b[i] = (Button)findViewById(ids[i]);
         	b[i].setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {
     				CharSequence click = ((Button)v).getText();
     				text.setText(click);
         		    
//        		    if (isInteger(click.toString()))
//        		    	hist.add(Integer.parseInt(click.toString()));
 
         		    char op = 0;
     		        switch(v.getId()) {
     		        	case R.id.buttonClear:
     		        		first = 0;
     		        		sec = 0;
     		        		break;    		     
     		        	case R.id.buttonADD:
 							op = '+';
 						case R.id.buttonMINUS:
 							op = '-';
 							break;
 						case R.id.buttonMULTIPLY:
 							op = '*';
 							break;
 						case R.id.buttonDIVIDE:
 							op = '/';
 							break;
 						case R.id.buttonEQUALS:
//							first = hist.get(0);
//							sec = hist.get(1);
 							switch(op) {
 								case '+':
 									calculate('+');
 									break;
 								case '-':
 									calculate('-');
 									break;
 								case '*':
 									calculate('*');
 									break;
 								case '/':
 									calculate('/');
 									break;
 							}
 							break;
     		        }	
     			}
     		});
         }
 
     }
     public static boolean isInteger(String str) {
         try { 
             Integer.parseInt(str); 
         } catch(NumberFormatException e) { 
             return false; 
         }
         return true;
     }
     private void calculate(char c) {	
     	calc = new Calculator(first,sec);
     	switch(c) {
 			case '+':
 				calc.add();
 				break;
 			case '-':
 				calc.minus();
 				break;
 			case '*':
 				calc.mult();
 				break;
 			case '/':
 				calc.div();
 				break;
 		}
     	updateDisplay();
     }
     
     public void updateDisplay() {
     	text.setText(calc.toString());
     }
     
     public class Calculator {
     	private int num1, num2;
     	private String res;
     	public Calculator(int a, int b) {
     		num1 = a;
     		num2 = b;
     		res = "";
     	}
     	public void add() {
             Integer sum = num1 + num2;
             res = sum.toString();
     	}
         public void div() {
             if (num2 == 0) {
                 res = "error: cannot divide by 0";
             } else {
             	Double temp = (double)num1 / (double)num2;
                 res = temp.toString();
             }
         }
         public void minus() {
             Integer diff = num1 - num2;
             res = diff.toString();
         }
         public int mult() {
             return num1 * num2;
         }
         @Override
         public String toString() {
         	return res;
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
