 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at  
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1.fragments;
 
 import uk.co.jwlawson.nof1.R;
 import uk.co.jwlawson.nof1.containers.Question;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * Fragment containing a number of input fields to allow clinician to build a
  * question. Can be displayed as either a dialog or a view.
  * 
  * @author John Lawson
  * 
  */
 public class QuestionBuilderFragment extends SherlockDialogFragment implements AdapterView.OnItemSelectedListener {
 
 	private static final String TAG = "QuestionBuilderFragment";
 	private static final boolean DEBUG = true;
 
 	/** View type. Show fragment as a dialog */
 	public static final int DIALOG = 0;
 	/** View Type. Show fragment as a view */
 	public static final int VIEW = 1;
 
 	/** Layout containing input fields for SCALE questions only */
 	private RelativeLayout mScaleLayout;
 
 	/** True if fragment shown as dialog, false for view */
 	private boolean mDialog;
 
 	/** Activity interface which gets callback */
 	private OnQuestionEditedListener mListener;
 
 	private EditText mEditQuestion;
 
 	private EditText mEditMin;
 
 	private EditText mEditMax;
 
 	private int mInputType;
 
 	public QuestionBuilderFragment() {
 	}
 
 	public interface OnQuestionEditedListener {
 
 		/**
 		 * Called when the question in QuestionBuilder is edited and saved.
 		 * 
 		 * @param question A copy of the question with new data
 		 */
 		public void onQuestionEdited(Question question);
 	}
 
 	public static QuestionBuilderFragment newInstance(int viewType, Question q) {
 		if (DEBUG) Log.d(TAG, "New QuestionBuilderFragment instanced");
 		QuestionBuilderFragment qbf = new QuestionBuilderFragment();
 
 		// Set arguments for new fragment
 		Bundle args = new Bundle();
 
 		// Check whether fragment will be dialog or view
 		boolean dialog;
 		switch (viewType) {
 		case DIALOG:
 			dialog = true;
 			break;
 		case VIEW:
 			dialog = false;
 			break;
 		default:
 			// Invalid value passed to method
 			throw new IllegalArgumentException(TAG + " viewType should be either DIALOG or VIEW");
 		}
 		args.putBoolean("viewType", dialog);
 
 		if (q != null) {
 			args.putString("questionText", q.getQuestionStr());
 			args.putInt("questionType", q.getInputType());
 			args.putString("questionMin", q.getMin());
 			args.putString("questionMax", q.getMax());
 		}
 
 		qbf.setArguments(args);
 
 		return qbf;
 	}
 
 	public String getQuestionText() {
 		return getArguments().getString("questionText");
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mDialog = getArguments().getBoolean("viewType");
 
 		if (!mDialog) {
 			// If view, register fragment has options
 			setHasOptionsMenu(true);
 		} else {
 			// If dialog, set so no title is displayed
 			setStyle(STYLE_NO_TITLE, 0);
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		// Check the activity implements interface
 		try {
 			mListener = (OnQuestionEditedListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString() + " must implement OnQuestionEditedListener");
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.config_question, container, false);
 
 		Bundle args = getArguments();
 
 		// Layout containing extra information for SCALE type questions. Hidden for other types.
 		mScaleLayout = (RelativeLayout) view.findViewById(R.id.config_question_minmax_layout);
 		mScaleLayout.setVisibility(View.INVISIBLE);
 
 		// Set the spinner listener and initial position
 		Spinner spnInput = (Spinner) view.findViewById(R.id.config_question_spinner_type);
 		spnInput.setOnItemSelectedListener(this);
 		spnInput.setSelection(args.getInt("questionType", 0));
 
 		// If shown as dialog, set button click Listeners, otherwise hide buttons
 		if (!mDialog) {
 			LinearLayout buttonBar = (LinearLayout) view.findViewById(R.id.config_question_button_bar);
 			buttonBar.setVisibility(View.INVISIBLE);
 		} else {
 			Button btnOK = (Button) view.findViewById(R.id.config_question_button_ok);
 			btnOK.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					save();
 					dismiss();
 				}
 			});
 
 			Button btnCan = (Button) view.findViewById(R.id.config_question_button_cancel);
 			btnCan.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					dismiss();
 				}
 			});
 		}
 
 		if (DEBUG) Log.d(TAG, "View created");
 		return view;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 
 		if (DEBUG) Log.d(TAG, "Menu size " + menu.size());
 		inflater.inflate(R.menu.menu_config_questions, menu);
 		if (DEBUG) Log.d(TAG, "new menu size " + menu.size());
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_config_questions_save:
 			// Save menu item selected
 			save();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void save() {
 		if (DEBUG) Log.d(TAG, "Saving edited question");
 		mListener.onQuestionEdited(getQuestion());
 	}
 
 	private Question getQuestion() {
 		Question q = new Question(mInputType, mEditQuestion.getText().toString());
 		if (mInputType == Question.SCALE) {
 			q.setMinMax(mEditMin.getText().toString(), mEditMax.getText().toString());
 		}
 		return q;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		switch (parent.getId()) {
 		case R.id.config_question_spinner_type:
 			// Input type spinner
 			String item = (String) parent.getItemAtPosition(position);
 			if (item.equalsIgnoreCase("Scale")) {
 				mScaleLayout.setVisibility(View.VISIBLE);
 				mInputType = Question.SCALE;
 			} else if (item.equalsIgnoreCase("number")) {
 				mScaleLayout.setVisibility(View.INVISIBLE);
 				mInputType = Question.NUMBER;
 			} else {
 				mScaleLayout.setVisibility(View.INVISIBLE);
 				mInputType = Question.CHECK;
 			}
 			break;
 
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// Don't care
 	}
 }
