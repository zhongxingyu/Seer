 package com.github.mahmoudhossam.height;
 
 import java.text.NumberFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import com.github.mahmoudhossam.height.R;
 
 public class HeightActivity extends Activity {
 
 	private static enum Mode {
 		IMPERIAL, METRIC
 	};
 
 	private static final int SWITCH_ID = Menu.FIRST;
 
 	private static Mode current;
 
 	private EditText cm;
 	private EditText feet;
 	private EditText inches;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		initializeVariables();
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		cm.setText(savedInstanceState.getString("cm"));
 		feet.setText(savedInstanceState.getString("feet"));
 		inches.setText(savedInstanceState.getString("inches"));
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putString("cm", cm.getText().toString());
 		outState.putString("inches", inches.getText().toString());
 		outState.putString("feet", feet.getText().toString());
 		super.onSaveInstanceState(outState);
 	}
 
 	public void initializeVariables() {
 		cm = (EditText) findViewById(R.id.editText1);
 		feet = (EditText) findViewById(R.id.editText2);
 		inches = (EditText) findViewById(R.id.editText3);
 		current = Mode.METRIC;
 	}
 
 	public void onConvertClick(View view) {
 		if (current == Mode.IMPERIAL) {
 			NumberFormat nf = NumberFormat.getInstance();
 			nf.setMaximumFractionDigits(1);
 			double output = Backend.getCentimeters(parseInput(feet),
 					parseInput(inches));
 			String result = nf.format(output);
 			createResultDialog(result + " centimeters").show();
 		} else {
 			int[] result = Backend.getFeetAndInches(parseInput(cm));
 			createResultDialog(
 					"" + result[0] + " feet, " + result[1] + " inches.").show();
 		}
 
 	}
 
 	private AlertDialog createResultDialog(String result) {
 		Builder builder = new AlertDialog.Builder(this);
 		builder.setCancelable(false).setTitle("Result").setMessage(result)
 				.setNegativeButton("OK", new OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				});
 		return builder.create();
 	}
 
 	public double parseInput(EditText input) {
 		if (input.getText().length() > 0) {
 			String text = input.getText().toString();
 			double content = Double.parseDouble(text);
 			return content;
 		} else {
 			return 0;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, SWITCH_ID, 0, R.string.change);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == SWITCH_ID) {
 			doSwitch();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void doSwitch() {
 		if (current == Mode.METRIC) {
 			toImperial();
 		} else {
 			toMetric();
 		}
 		emptyBoxes();
 	}
 
 	private void toImperial() {
 		toggleFeetAndInches(true);
 		toggleCentimeters(false);
 		current = Mode.IMPERIAL;
 		feet.requestFocus();
 	}
 
 	private void toMetric() {
 		toggleFeetAndInches(false);
 		toggleCentimeters(true);
 		current = Mode.METRIC;
 		cm.requestFocus();
 	}
 
 	private void toggleCentimeters(boolean on) {
 		cm.setEnabled(on);
		
 	}
 
 	private void toggleFeetAndInches(boolean on) {
 		feet.setEnabled(on);
 		inches.setEnabled(on);
 	}
 
 	private void emptyBoxes() {
 		cm.setText("");
 		feet.setText("");
 		inches.setText("");
 	}
 
 }
