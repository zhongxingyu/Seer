 package com.reflexer.ui;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.reflexer.R;
 import com.reflexer.model.RXConditionDefinition;
 import com.reflexer.model.RXReflex;
 import com.reflexer.model.RXStimuli;
 import com.reflexer.model.RXStimuliCondition;
 import com.reflexer.model.RXStimuliDefinition;
 import com.reflexer.ui.views.RXTypeView;
 import com.reflexer.ui.views.RXTypeView.OnValueChangedListener;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * This fragment is the visual representation of RXStimuli. It is used to select
  * which type of stimuli is used and to define corresponding coniditions.
  * 
  * 
  * @author ivan
  * 
  */
 public class RXStimuliFragment extends Fragment {
 
 	/**
 	 * Reflex that is being updated or created.
 	 */
 	private RXReflex reflex;
 
 	/**
 	 * Index of the selected stimuli type in the stimuli definitions array.
 	 */
 	private int selectedIndex;
 
 	private ImageView stimuliImage;
 
 	private TextView stimuliName;
 
 	/**
 	 * Layout that holds the views for defining the conditions.
 	 */
 	private LinearLayout conditionsLayout;
 
 	private ArrayList<RXStimuliDefinition> stimuliDefinitions;
 
 	private final ArrayList<RXTypeView> conditionViews = new ArrayList<RXTypeView>();
 
 	public static RXStimuliFragment newInstance() {
 		RXStimuliFragment fragment = new RXStimuliFragment();
 
 		return fragment;
 	}
 
 	public RXStimuliFragment() {
 		super();
		getStimuliDefinitions();
 	}
 
 	public void setReflex(RXReflex reflex) {
 		this.reflex = reflex;
 
 		showConditions();
 		updateConditions();
 		updateStimuliIndex(reflex);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_stimuli, null);
 
 		setupStimuliPicker(view);
 		conditionsLayout = (LinearLayout) view.findViewById(R.id.conditions_layout);
 
 		stimuliImage = (ImageView) view.findViewById(R.id.image);
 		stimuliName = (TextView) view.findViewById(R.id.label);
 
 		setupStimuliPicker(view);
 		showStimuli(selectedIndex);
 
 		return view;
 	}
 
 	private void setupStimuliPicker(View rootView) {
 		ImageView leftStimuliPicker = (ImageView) rootView.findViewById(R.id.stimuli_picker_left);
 		ImageView rightStimuliPicker = (ImageView) rootView.findViewById(R.id.stimuli_picker_right);
 
 		leftStimuliPicker.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Log.d("RXStimuliFragment", "left");
 				selectedIndex = (selectedIndex - 1);
 				if (selectedIndex < 0) {
 					selectedIndex = stimuliDefinitions.size() - 1;
 				}
 
 				showStimuli(selectedIndex);
 			}
 		});
 
 		rightStimuliPicker.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Log.d("RXStimuliFragment", "right");
 				selectedIndex = (selectedIndex + 1) % stimuliDefinitions.size();
 
 				showStimuli(selectedIndex);
 			}
 		});
 	}
 
 	private void getStimuliDefinitions() {
 		try {
 			this.stimuliDefinitions = RXStimuli.getStimuliDefinitions(getActivity());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets the stimuli index to match index of the stimuli used in reflex
 	 * 
 	 * 
 	 * @param reflex
 	 */
 	private void updateStimuliIndex(RXReflex reflex) {
 		RXStimuliDefinition def = reflex.getStimuli().getDefinition();
 
 		for (int i = 0; i < stimuliDefinitions.size(); i++) {
 			if (stimuliDefinitions.get(i).getName().equals(def.getName())) {
 				selectedIndex = i;
 				showStimuli(selectedIndex);
 			}
 		}
 
 	}
 
 	private void showStimuli(int index) {
 		if (stimuliName != null) {
 			stimuliName.setText(stimuliDefinitions.get(index).getName());
 		}
 	}
 
 	/**
 	 * Shows the conditions for the selected stimuli.
 	 */
 	private void showConditions() {
 		conditionViews.clear();
 		conditionsLayout.removeAllViews();
 
 		for (RXConditionDefinition condDef : reflex.getStimuli().getDefinition().getConditionDefinitons()) {
 			RXTypeView conditionView = RXTypeView.createView(getActivity(), condDef.getName(), condDef.getType());
 			conditionView.setRequired(condDef.isRequired());
 			conditionView.setEnabled(condDef.getDependsOn().size() == 0);
 			conditionView.setOnValueChangedListener(new OnValueChangedListener() {
 
 				@Override
 				public void onValueChanged(String name, Object value) {
 					reflex.getStimuli().setCondition(new RXStimuliCondition(name, value));
 
 					updateConditions();
 				}
 			});
 
 			conditionViews.add(conditionView);
 			conditionsLayout.addView(conditionView, new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			Log.d("showConditions", "added: " + condDef.getName());
 		}
 	}
 
 	private void updateConditions() {
 		for (RXTypeView typeView : conditionViews) {
 			RXConditionDefinition condDef = reflex.getStimuli().getDefinition()
 					.getConditionDefinitionByName(typeView.getName());
 
 			if (condDef.getDependsOn().size() > 0) {
 				typeView.setEnabled(true);
 
 				for (RXConditionDefinition dependency : condDef.getDependsOn()) {
 					RXStimuliCondition cond = reflex.getStimuli().getConditionByName(dependency.getName());
 					if (cond == null || cond.getValue() == null) {
 						typeView.setEnabled(false);
 						break;
 					}
 				}
 			}
 		}
 	}
 }
