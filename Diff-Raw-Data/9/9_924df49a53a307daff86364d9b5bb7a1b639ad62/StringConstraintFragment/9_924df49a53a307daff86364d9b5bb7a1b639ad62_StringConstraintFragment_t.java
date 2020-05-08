 /*
  * Copyright 2012 ToureNPlaner
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package de.uni.stuttgart.informatik.ToureNPlaner.UI.ConstraintFragments;
 
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 
 public class StringConstraintFragment extends ConstraintFragment {
 	public static StringConstraintFragment newInstance(Constraint constraint, int index) {
 		StringConstraintFragment fragment = new StringConstraintFragment();
 		Bundle args = new Bundle();
 		args.putSerializable("constraint", constraint);
 		args.putInt("index", index);
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.string_constraint_layout, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
 		TextView value = ((TextView) getView().findViewById(R.id.value));
 		value.setText((String) constraint.getValue());
 		value.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				constraint.setValue(s.toString());
 				dirty = true;
 			}
 		});
 	}
 }
