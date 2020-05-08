 package com.tuvistavie.meetup.event.fragment;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 
 import com.tuvistavie.meetup.event.activity.CreateEventActivity;
 import com.tuvistavie.meetup.R;
import com.tuvistavie.meetup.event.model.Timeline;
 
 import roboguice.fragment.RoboFragment;
 import roboguice.inject.InjectView;
 
 /**
  * Created by daniel on 8/31/13.
  */
 public class ToolbarFragment extends RoboFragment {
     @InjectView(R.id.add_event_button) ImageButton addEventButton;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         View rootView = inflater.inflate(R.layout.fragment_toolbar, container, false);
         return rootView;
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         addEventButton.setOnClickListener(new AddButtonListener());
     }
 
     private class AddButtonListener implements View.OnClickListener {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent(getActivity(), CreateEventActivity.class);
             startActivity(intent);
         }
     }
 }
