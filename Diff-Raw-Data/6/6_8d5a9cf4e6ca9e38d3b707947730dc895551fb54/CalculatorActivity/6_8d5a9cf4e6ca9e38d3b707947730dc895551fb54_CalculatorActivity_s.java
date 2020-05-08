 package com.olleicua.calculator;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.Toast;
 
 public class CalculatorActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         text = Toast.makeText(CalculatorActivity.this, "", Toast.LENGTH_SHORT);
         
         GridView gridview = (GridView) findViewById(R.id.gridview);
         gridview.setAdapter(new ImageAdapter(this));
 
         gridview.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
             	switch (position) {
                 case 0 : // 1
                 	number(1) ;
                 	break ;
                 case 1 : // 2
                 	number(2) ;
                 	break ;
                 case 2 : // 3
                 	number(3) ;
                 	break ;
                 case 3 : // plus
                 	evaluate() ;
                 	operator = PLUS ;
                 	break ;
                 case 4 : // 4
                 	number(4) ;
                 	break ;
                 case 5 : // 5
                 	number(5) ;
                 	break ;
                 case 6 : // 6
                 	number(6) ;
                 	break ;
                 case 7 : // minus
                 	evaluate() ;
                 	operator = MINUS ;
                 	break ;
                 case 8 : // 7
                 	number(7) ;
                 	break ;
                 case 9 : // 8
                 	number(8) ;
                 	break ;
                 case 10 : // 9
                 	number(9) ;
                 	break ;
                 case 11 : // times
                 	evaluate() ;
                 	operator = TIMES ;
                 	break ;
                 case 12 : // equals
                 	evaluate() ;
                 	//value = previousValue ;
                 	resultValue = previousValue ;
                 	break ;
                 case 13 : // zero
                 	number(0) ;
                 	break ;
                 case 14 : // clear
                 	cleared = true ;
                 	value = 0 ;
                 	previousValue = 0 ;
                 	resultValue = null ;
                 	operator = NONE ;
                 	break ;
                 case 15 : // divide
                	evaluate() ;
                	operator = DIVIDE ;
                 	break ;
                 default :
                 	break ;
                 }
             	text.cancel();
             	text = Toast.makeText(CalculatorActivity.this, display(), Toast.LENGTH_SHORT);
             	text.show();
             	//Toast.makeText(CalculatorActivity.this, display(), Toast.LENGTH_SHORT).show();
             }
         });
     }
     
     public void number(int n) {
     	if (resultValue != null) {
     		resultValue = null ;
     	}
     	cleared = false ;
     	value *= 10 ;
     	value += n ;
     }
     
     public void evaluate() {
     	switch (operator) {
     	case PLUS :
     		previousValue += value ;
     		break ;
     	case MINUS :
     		previousValue -= value ;
     		break ;
     	case TIMES :
     		previousValue *= value ;
     		break ;
     	case DIVIDE :
     		previousValue /= value ;
     		break ;
     	case NONE :
     		previousValue = value ;
     		break ;
     	default :
     		break ;
     	}
     	if (resultValue != null) {
     		previousValue = (Integer) resultValue ;
     		resultValue = null ;
     	}
     	value = 0 ;
     	operator = NONE ;
     }
     
     public String display() {
     	String out = Integer.toString(previousValue) ;
     	switch (operator) {
     	case PLUS :
     		out += " + " ;
     		break ;
        	case MINUS :
     		out += " - " ;
     		break ;
     	case TIMES :
     		out += " x " ;
     		break ;
     	case DIVIDE :
     		out += " / " ;
     		break ;
     	default :
     		if (cleared) {
     			return "" ;
     		}
     		if (null != resultValue) {
     			return Integer.toString((Integer) resultValue) ;
     		}
     		if (NONE == operator) {
     			return Integer.toString(value) ;
     		}
     		return Integer.toString(previousValue) ;
     	}
     	return out + Integer.toString(value) ;
     }
 
     // OPERATOR CONSTANTS
     private static final int NONE = 0 ;
     private static final int PLUS = 1 ;
     private static final int MINUS = 2 ;
     private static final int TIMES = 3 ;
     private static final int DIVIDE = 4 ;
 
     // STATE VARIABLES
     public Boolean cleared = true ;
     public int operator = NONE ;
     public int value = 0 ;
     public int previousValue = 0 ;
     public Object resultValue = null ;
     public Toast text ;
     //public Toast display = Toast.makeText(CalculatorActivity.this, "", Toast.LENGTH_SHORT);
 }
