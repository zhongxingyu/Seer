 package com.oakonell.chaotictactoe.ui.menu;
 
import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.oakonell.chaotictactoe.R;
 import com.oakonell.chaotictactoe.model.MarkerChance;
 import com.oakonell.utils.StringUtils;
 
 public class NewLocalGameDialog extends SherlockDialogFragment {
 	private String xName;
 	private String oName;
 
 	public interface LocalGameModeListener {
 		void chosenMode(MarkerChance chance, String xName, String oName);
 	}
 
 	private LocalGameModeListener listener;
 
 	public void initialize(LocalGameModeListener listener) {
 		this.listener = listener;
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.dialog_local_game, container,
 				false);
 
 		getDialog().setTitle(R.string.choose_local_game_mode_title);
 		defaultNamesFromPreferences();
 
 		ImageButton switchPlayers = (ImageButton) view
 				.findViewById(R.id.switch_players);
 		final EditText xNameText = (EditText) view
 				.findViewById(R.id.player_x_name);
 		final EditText oNameText = (EditText) view
 				.findViewById(R.id.player_o_name);
 
 		final MarkerChanceFragment frag = new MarkerChanceFragment();
 		FragmentManager fragmentManager = getChildFragmentManager();
 		FragmentTransaction transaction = fragmentManager.beginTransaction();
 		transaction.replace(R.id.fragmentContainer, frag);
 
 		transaction.commit();
 
 		xNameText.setText(xName);
 		oNameText.setText(oName);
 
 		OnFocusChangeListener onNameFocusChangeListener = new OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (!hasFocus) {
 					validate(xNameText, oNameText, frag);
 				}
 			}
 		};
 		xNameText.setOnFocusChangeListener(onNameFocusChangeListener);
 		xNameText.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				xNameText.setError(null);
 			}
 		});
 		oNameText.setOnFocusChangeListener(onNameFocusChangeListener);
 		oNameText.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				oNameText.setError(null);
 			}
 		});
 
 		switchPlayers.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Editable temp = xNameText.getText();
 				xNameText.setText(oNameText.getText());
 				oNameText.setText(temp);
 			}
 		});
 
 		Button start = (Button) view.findViewById(R.id.start);
 		start.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

 				if (!validate(xNameText, oNameText, frag)) {
 					return;
 				}
 				xName = xNameText.getText().toString();
 				oName = oNameText.getText().toString();
 				MarkerChance chance = frag.getChance();
 				writeNamesToPreferences();
 
 				dismiss();
 				listener.chosenMode(chance, xName, oName);
 			}
 		});
 		return view;
 
 	}
 
 	protected boolean validate(EditText xNameText, EditText oNameText,
 			MarkerChanceFragment frag) {
 		boolean isValid = true;
 		String xName = xNameText.getText().toString();
 		if (StringUtils.isEmpty(xName)) {
 			isValid = false;
 			xNameText.setError(getResources().getString(R.string.error_x_name));
 		}
 		String oName = oNameText.getText().toString();
 		if (StringUtils.isEmpty(oName)) {
 			isValid = false;
 			oNameText.setError(getResources().getString(R.string.error_o_name));
 		}
 
 		if (xName.equals(oName)) {
 			isValid = false;
 			oNameText.setError(getResources().getString(R.string.unique_error_o_name));
 		}
 
 		isValid &= frag.validate();
 		return isValid;
 	}
 
 	private static final String PREF_X_NAME = "x-name";
 	private static final String PREF_O_NAME = "o-name";
 
 	private void defaultNamesFromPreferences() {
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getSherlockActivity());
 		// TODO store multiple names of the last players, and hook "search" into
 		// the text entry
 		xName = sharedPrefs.getString(PREF_X_NAME, "");
 		oName = sharedPrefs.getString(PREF_O_NAME, "");
 	}
 
 	private void writeNamesToPreferences() {
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getSherlockActivity());
 		Editor edit = sharedPrefs.edit();
 		edit.putString(PREF_X_NAME, xName);
 		edit.putString(PREF_O_NAME, oName);
 		edit.commit();
 	}
 
 }
