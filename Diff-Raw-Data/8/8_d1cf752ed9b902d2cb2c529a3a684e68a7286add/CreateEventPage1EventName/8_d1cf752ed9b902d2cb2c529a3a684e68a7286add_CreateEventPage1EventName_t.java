 package com.appchallenge.android;
 
 import com.appchallenge.android.Event.Type;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 
 /**
  * The first step of the create event wizard. This collects the name of
  * the event from the user.
  */
 public class CreateEventPage1EventName extends Fragment {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         int layoutId = R.layout.fragment_create_event_page_1;
         ViewGroup rootView = (ViewGroup)inflater.inflate(layoutId, container, false);
         
         EditText titleBox = (EditText)rootView.findViewById(R.id.event_name);
         EditText descBox = (EditText)rootView.findViewById(R.id.event_description);
         Spinner typeSpinner = (Spinner)rootView.findViewById(R.id.event_type_spinner);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                                                              R.array.type_array,
                                                                              android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         typeSpinner.setAdapter(adapter);
 
        // Read saved data from the parent Activity.
         titleBox.setText(((CreateEventInterface)getActivity()).getEventTitle());
         descBox.setText(((CreateEventInterface)getActivity()).getDescription());
        int typeVal = ((CreateEventInterface)getActivity()).getType().getValue();
        typeSpinner.setSelection(typeVal == 0 ? Type.values().length - 1 : typeVal - 1);
 
         // Handle keeping track of future updates to the text.
         titleBox.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				((CreateEventInterface)getActivity()).setTitle(s.toString());
 			}
 			
 			// Unused interface methods of TextWatcher.
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
         });
         descBox.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				((CreateEventInterface)getActivity()).setDescription(s.toString());
 			}
 			
 			// Unused interface methods of TextWatcher.
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
         });
         
         typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
             	// Grab the enum by matching the indices of this spinner to the enumerated types.
             	int index = (position == Type.values().length - 1 ? 0 : position + 1);
            	((CreateEventInterface)getActivity()).setType(Type.typeIndices[index]);
             }
             // Interface requirements
             public void onNothingSelected(AdapterView<?> parentView) {}
         });
 
         return rootView;
     }
 }
