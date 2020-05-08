 package com.project.latviankeyboard;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class PrefsAltKey extends PreferenceActivity {
 
 	OnPreferenceClickListener myListener;
 	SeekBar redBar, greenBar, blueBar, alphaBar, seekBar;
 	View colorTest;
 	LinearLayout colorChooser, seekBarDialog;
 	SeekBar.OnSeekBarChangeListener seekBarChangeListener, sbChangeListener;
 	Button btnOk, btnCancel, sbBtnCancel, sbBtnOk;
 	Dialog d, sbD;
 	SharedPreferences prefs;
 	String prefKey;
 	TextView sbText, textRed, textGreen, textBlue, textAlpha;
 	CheckBoxPreference defaultStyle, soundSwitch;
 	int bkColor, sbProgress;
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		// inflates preferences
 		addPreferencesFromResource(R.xml.prefs_alt_key);
 
 		// inflates colorChooser and its objects
 		colorChooser = (LinearLayout) getLayoutInflater().inflate(R.layout.color_chooser, null);
 		redBar = (SeekBar) colorChooser.findViewById(R.id.sbRed);
 		greenBar = (SeekBar) colorChooser.findViewById(R.id.sbGreen);
 		blueBar = (SeekBar) colorChooser.findViewById(R.id.sbBlue);
 		alphaBar = (SeekBar) colorChooser.findViewById(R.id.sbAlpha);
 		textRed = (TextView) colorChooser.findViewById((R.id.textRed));
 		textGreen = (TextView) colorChooser.findViewById((R.id.textGreen));
 		textBlue = (TextView) colorChooser.findViewById((R.id.textBlue));
 		textAlpha = (TextView) colorChooser.findViewById((R.id.textAlpha));
 		btnOk = (Button) colorChooser.findViewById(R.id.btnOk);
 		btnCancel = (Button) colorChooser.findViewById(R.id.btnCancel);
 
 		// inflates seekBarDialog and its objects
 		seekBarDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.seek_bar_dialog, null);
 		seekBar = (SeekBar) seekBarDialog.findViewById(R.id.seekBar);
 		sbText = (TextView) seekBarDialog.findViewById(R.id.textRed);
 		sbBtnOk = (Button) seekBarDialog.findViewById(R.id.sbBtnOk);
 		sbBtnCancel = (Button) seekBarDialog.findViewById(R.id.sbBtnCancel);
 
 		// gets shared preferences to edit them later
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		final SharedPreferences.Editor editor = prefs.edit();
 
 		// inflates Dialog context and adds colorChooser layout
 		d = new Dialog(PrefsAltKey.this);
 		d.setContentView(colorChooser);
 		d.setCanceledOnTouchOutside(true);
 
 		// inflates Dialog context and adds seekBar layout
 		sbD = new Dialog(PrefsAltKey.this);
 		sbD.setContentView(seekBarDialog);
 		sbD.setCanceledOnTouchOutside(true);
 
 		// ColorChooser action listener
 		seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				updateBackground();
 			}
 
 			// upgrades view background color from seekBar values
 			private void updateBackground() {
 				textRed.setText("" + redBar.getProgress());
 				textGreen.setText("" + greenBar.getProgress());
 				textBlue.setText("" + blueBar.getProgress());
 				textAlpha.setText("" + alphaBar.getProgress());
 				colorTest.setBackgroundColor(Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress()));
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 		};
 
 		// SeekBar action listener
 		sbChangeListener = new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				sbText.setText("" + progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 		};
 
 		// adds actionlistener to "Ok" button [ColorChooser]
 		btnOk.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				editor.putInt(prefKey, Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress()));
 				editor.commit();
 				d.dismiss();
 			}
 		});
 
 		// adds actionListener to "Cancel" button [ColorChooser]
 		btnCancel.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				d.dismiss();
 			}
 		});
 
 		// adds actionlistener to "Ok" button [SeekBarDialog]
 		sbBtnOk.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// if (prefKey.equals("akBtnRoundness")) {
 				// editor.putFloat(prefKey, (Float) seekBar.getProgress());
 				// } else {
 				editor.putInt(prefKey, seekBar.getProgress());
 				editor.commit();
 				sbD.dismiss();
 				// }
 			}
 		});
 
 		// adds actionListener to "Cancel" button [SeekBarDialog]
 		sbBtnCancel.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				sbD.dismiss();
 			}
 		});
 
 		// adds actionListener to seekbars
 		redBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		greenBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		blueBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		alphaBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		seekBar.setOnSeekBarChangeListener(sbChangeListener);
 
 		// inflates view that is used to test color
 		colorTest = colorChooser.findViewById(R.id.testColor);
 
 		// CheckBoxPreference
 		defaultStyle = (CheckBoxPreference) findPreference("akDefaultStyle");
 		soundSwitch = (CheckBoxPreference) findPreference("akSwitchSounds");
 
 		// creates actionlistener for preferences
 		myListener = new OnPreferenceClickListener() {
 
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				prefKey = preference.getKey();
 				if (prefKey.equals("akBackgroundColor")) {
 					bkColor = prefs.getInt(prefKey, Color.BLACK);
 					setColorChooserDialog(preference, prefKey);
 				} else if (prefKey.equals("akBtnColor")) {
 					bkColor = prefs.getInt(prefKey, Color.WHITE);
 					setColorChooserDialog(preference, prefKey);
 				} else if (prefKey.equals("akLetterColor")) {
 					bkColor = prefs.getInt(prefKey, Color.RED);
 					setColorChooserDialog(preference, prefKey);
 				} else if (prefKey.equals("akBorderColor")) {
 					bkColor = prefs.getInt(prefKey, Color.LTGRAY);
 					setColorChooserDialog(preference, prefKey);
 				} else if (prefKey.equals("akBtnRoundness")) {
 					sbProgress = prefs.getInt(prefKey, 1);
 					seekBar.setMax(30);
 					setSeekBarDialog(preference, prefKey);
 				} else if (prefKey.equals("akBorderWidth")) {
 					sbProgress = prefs.getInt(prefKey, 2);
 					seekBar.setMax(15);
 					setSeekBarDialog(preference, prefKey);
 				} else if (prefKey.equals("akCallKeyboard")) {
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
 				} else if (prefKey.equals("akDefaultStyle")) {
 					editor.putBoolean(prefKey, defaultStyle.isChecked());
 					editor.commit();
 				}
 				return false;
 			}
 		};
 
 		// adds actionListeners for preferences
 		findPreference("akBackgroundColor").setOnPreferenceClickListener(myListener);
 		findPreference("akBtnColor").setOnPreferenceClickListener(myListener);
 		findPreference("akLetterColor").setOnPreferenceClickListener(myListener);
 		findPreference("akBorderColor").setOnPreferenceClickListener(myListener);
 		findPreference("akBtnRoundness").setOnPreferenceClickListener(myListener);
 		findPreference("akBorderWidth").setOnPreferenceClickListener(myListener);
 		findPreference("akCallKeyboard").setOnPreferenceClickListener(myListener);
 		findPreference("akDefaultStyle").setOnPreferenceClickListener(myListener);
 
 		// akDefaultStyle checkbox bugfix
 		if (prefs.getBoolean("akDefaultStyle", true)) {
 			defaultStyle.setChecked(true);
 		} else {
 			defaultStyle.setChecked(false);
 		}
 
 		// akSwitchSounds checkbox bugfix
 		if (prefs.getBoolean("akSwtichSounds", true)) {
 			soundSwitch.setChecked(true);
 		} else {
 			soundSwitch.setChecked(false);
 		}
 
 	}
 	public void setColorChooserDialog(Preference preference, String prefKey) {
 		d.setTitle(preference.getTitle());
 		colorTest.setBackgroundColor(bkColor);
 		redBar.setProgress(Color.red(bkColor));
 		greenBar.setProgress(Color.green(bkColor));
 		blueBar.setProgress(Color.blue(bkColor));
 		alphaBar.setProgress(Color.alpha(bkColor));
 		textRed.setText("" + redBar.getProgress());
 		textGreen.setText("" + greenBar.getProgress());
 		textBlue.setText("" + blueBar.getProgress());
 		textAlpha.setText("" + alphaBar.getProgress());
 		d.show();
 	}
 
 	public void setSeekBarDialog(Preference preference, String prefKey) {
 		sbD.setTitle(preference.getTitle());
 		seekBar.setProgress(sbProgress);
 		sbText.setText("" + sbProgress);
 		sbD.show();
 	}
 }
