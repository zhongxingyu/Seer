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
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.EnumConstraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 
 public class EnumConstraintFragment extends ConstraintFragment {
 	public static EnumConstraintFragment newInstance(Constraint constraint, int index) {
 		EnumConstraintFragment fragment = new EnumConstraintFragment();
 		Bundle args = new Bundle();
 		args.putSerializable("constraint", constraint);
 		args.putInt("index", index);
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.enum_constraint_layout, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
 		Spinner value = ((Spinner) getView().findViewById(R.id.value));
 		EnumConstraint c = (EnumConstraint) constraint.getType();
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, c.getValues());
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		value.setAdapter(adapter);
 		value.setSelection(c.getValues().indexOf(constraint.getValue()));
 		value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 				constraint.setValue((String) parent.getItemAtPosition(position));
 				dirty = true;
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 			}
 		});
 	}
 }
