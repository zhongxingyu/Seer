 package edu.csumb.ideasofmarch.codecruncher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class BinaryRow implements GameRow {
 	
 	static int rowCount = 0;
 	private LinearLayout pll,ll;
 	private int randNum, numDigits;
 	private TextView question;
 	private ToggleButton t1,t2,t3,t4;
 	private List<ToggleButton> buttonList = new ArrayList<ToggleButton>();
 	
 	public BinaryRow(LinearLayout aLayout, Context aContext, int Digits) {
 		pll = aLayout;
 		numDigits = Digits;
		randNum = (int) Math.floor(Math.random() * (Math.pow(2,numDigits)));	
 		question = new TextView(aContext);
 		
 		int resID = (int) Math.floor(Math.random()*3);
 		if(resID == 0) {
 			resID = R.drawable.redbutton;
 		} else if(resID == 1) {
 			resID = R.drawable.yellowbutton;
 		}else {
 			resID = R.drawable.bluebutton;
 		}
 		
 		for (int i = 0; i < numDigits; i++) {
 			t1 = new ToggleButton(aContext);
 			t1.setBackgroundResource(resID);
 			t1.setText(R.string.binaryInput); 
 			t1.setTextOff("0");
 			t1.setTextOn("1");
 			t1.setTextColor(Color.WHITE);
 			t1.setChecked(false);
 			buttonList.add(t1);
 		}
 		
 		ll = new LinearLayout(aContext);
 		for (int i = 0; i < numDigits; i++) {
 			ll.addView(buttonList.get(i));
 		}
 		
 		question.setText("" + randNum);
 		question.setTextColor(Color.WHITE);
 		question.setTextSize(20);
 		question.setBackgroundResource(R.drawable.textbox);
 		
 		ll.addView(question);
 	}
 
 	@Override
 	public boolean putNewRow() {
 		if(rowCount < 8){
 			rowCount++;
 			pll.addView(ll);
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	@Override
 	public boolean checkProblem() {
 		String answer = getGuess();
 		int intAnswer = Integer.parseInt(answer, 2);
 		
 		if(intAnswer == randNum){
 			// Sound will play in the BinaryToDecimal and BinaryToHex activity files
 			removeRow();
 			return true;
 		}else{
 			question.setTextColor(Color.RED);
 			return false;
 		}
 	}
 	
     public String getGuess() {
     	String binaryInput = "";
     	
     	for (int i = 0; i < numDigits; i++){
 			if (buttonList.get(i).isChecked()) {
 				binaryInput += "1";
 			} else {
 				binaryInput += "0";
 			}
     	}
     	    	
     	return binaryInput;
     }
 
 	@Override
 	public boolean removeRow() {
 		if(rowCount > 0){
 			ll.setVisibility(View.GONE);
 			((ViewGroup) ll.getParent()).removeView(ll);
 			rowCount--;
 			return true;
 		}else{
			randNum = (int) Math.floor(Math.random()*(Math.pow(2,numDigits)));
 			question.setText("" + randNum);
 		return false;
 		}
 	}
 	
 	public int score(){
 		return randNum;
 	}
 	
 	public void resetRowCount(){
 		rowCount = 0;
 	}
 }
 
