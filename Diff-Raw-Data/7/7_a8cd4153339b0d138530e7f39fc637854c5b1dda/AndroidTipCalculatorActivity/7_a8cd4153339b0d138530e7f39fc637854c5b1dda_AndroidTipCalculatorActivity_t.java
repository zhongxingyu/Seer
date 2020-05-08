 package com.android.androidtipcalculator;
 
 import java.text.DecimalFormat;
 
 import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AndroidTipCalculatorActivity extends Activity {
 
 	private EditText etBillInput; 
 	private TextView tvTipOP;
 	private TextView tvTotalOP;
 	private TextView tvSeekProgress;
 	private TextView tvMsgId;
 	private SeekBar sbPct;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_android_tip_calculator);
 		etBillInput = (EditText) findViewById(R.id.etBillInput);
 		tvTipOP = (TextView) findViewById(R.id.tvTipOP);
 		tvTotalOP = (TextView) findViewById(R.id.tvTotalOP);
 		tvSeekProgress = (TextView) findViewById(R.id.tvSeekProgress);
 		tvMsgId =  (TextView) findViewById(R.id.tvMsgId);
 		sbPct = (SeekBar) findViewById(R.id.sbPct);
 		addSeekEventListner();
 	}
 
 	private void addSeekEventListner() {
 		sbPct.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
 			
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				
 			}
 			
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				
 			}
 			
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				tvSeekProgress.setText(String.valueOf(progress) + "%");
 				if(progress > 20) {
 					tvMsgId.setText("Don't you think, you are being too generous :)");
 				}else {
 					tvMsgId.setText("");
 				}
 				double tipPercent = Double.valueOf(progress);
				if(StringUtils.isNotEmpty(etBillInput.getText().toString()) && NumberUtils.isNumber(etBillInput.getText().toString())) {
 					double billAmnt = Double.valueOf(etBillInput.getText().toString());
 					double tipAmnt = (billAmnt * tipPercent) / 100;
 					double totaAmnt = billAmnt + tipAmnt;
 					DecimalFormat df = new DecimalFormat("#.##"); 
 					String displaTipAmnt = getString(R.string.tipStaticLbl) + "   "  + String.valueOf(df.format(tipAmnt));
 					tvTipOP.setText(displaTipAmnt);
 					String displaTotalAmnt = getString(R.string.totalStaticLbl) + "   "  + String.valueOf(df.format(totaAmnt));
 					tvTotalOP.setText(displaTotalAmnt);
 				}
 			}
 		});
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.android_tip_calculator, menu);
 		return true;
 	}
 	
 	public void onBtnClick(View v) {
 		Button clickedBtn = (Button) v;
 		String clickedText = clickedBtn.getText().toString();
 		double tipPercent = Double.valueOf(clickedText.substring(0, clickedText.length()-1));
		if(StringUtils.isNotEmpty(etBillInput.getText().toString()) && NumberUtils.isNumber(etBillInput.getText().toString())) {
 			double billAmnt = Double.valueOf(etBillInput.getText().toString());
 			double tipAmnt = (billAmnt * tipPercent) / 100;
 			double totaAmnt = billAmnt + tipAmnt;
 			DecimalFormat df = new DecimalFormat("#.##"); 
 			String displaTipAmnt = getString(R.string.tipStaticLbl) + "   "  + String.valueOf(df.format(tipAmnt));
 			tvTipOP.setText(displaTipAmnt);
 			String displaTotalAmnt = getString(R.string.totalStaticLbl) + "   "  + String.valueOf(df.format(totaAmnt));
 			tvTotalOP.setText(displaTotalAmnt);
 		}else {
 			Toast.makeText(this, "Please enter a valid bill amount.", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 }
