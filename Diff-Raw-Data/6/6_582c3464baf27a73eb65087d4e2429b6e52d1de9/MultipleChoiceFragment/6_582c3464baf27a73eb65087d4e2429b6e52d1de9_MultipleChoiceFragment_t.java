 package com.albinstjerna.ananke;
 
 import android.os.Bundle;
 import android.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.View;
 import java.util.Random;
 import android.app.Activity;
 import android.widget.Button;
 import android.widget.EditText;
 import java.util.ArrayList;
 import android.widget.RelativeLayout;
 
 public class MultipleChoiceFragment extends Fragment {
     RecieveDecisionData mCallback;
 
     // Container Activity must implement this interface
     public interface RecieveDecisionData {
         public void handleDecision(String decision);
     }
 
     private String chooseRandomTextView(ViewGroup group) {
         
         // Loop over all TextEdit fields and accumulate contents.
         ArrayList<String> strings = new ArrayList<String>();
         for (int i = 0, count = group.getChildCount(); i < count; ++i) {
             View view_thing = group.getChildAt(i);
             if (view_thing instanceof EditText) {
                 EditText editText = (EditText) view_thing;
                 
                 // This syntaxt is just whack.
                 String text = ("" + editText.getText()).trim();
                 
                 // Make sure the added strings AREN'T empty.
                 if (text.length() > 0) {
                     strings.add(text);
                 }
             }
         }
 
         Random randgen = new Random();
        if(strings.size() == 0) {
            return "No choices entered";
        } else {
            return strings.get(randgen.nextInt(strings.size()));
        }
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, 
         Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.multiplechoice, container, false);
 
         Button decide = (Button) view.findViewById(R.id.multiple_choice_button);
         decide.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View view) {
 
                     ViewGroup group = (ViewGroup) view.getParent().getParent();
                     if(group != null) {
                         mCallback.handleDecision(chooseRandomTextView(group));
                     }
                 }
             });
 
         Button add = (Button) view.findViewById(R.id.multiple_choice_add_button);
         add.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View view) {
                     EditText et = new EditText(getActivity());
                     et.setHint(getResources().getString(R.string.alternative_text));
                     
                     ViewGroup group = (ViewGroup) view.getParent().getParent();
                                                                                         
                     group.addView(et);
                 }
             });
 
 
         return view;
 
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         // This makes sure that the container activity has implemented
         // the callback interface. If not, it throws an exception.
         try {
             mCallback = (RecieveDecisionData) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString()
                     + " must implement RecieveDecisionData");
         }
     }
 
 }
