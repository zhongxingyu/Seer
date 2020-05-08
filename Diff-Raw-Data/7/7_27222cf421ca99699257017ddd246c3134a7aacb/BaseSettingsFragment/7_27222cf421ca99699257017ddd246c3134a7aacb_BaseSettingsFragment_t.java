 package com.quizz.core.fragments;
 
import com.quizz.core.activities.BaseQuizzActivity;

 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 
 public class BaseSettingsFragment extends Fragment {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
		if (getActivity() instanceof BaseQuizzActivity) {
			((BaseQuizzActivity) getActivity())
					.setHideAbOnRotationChange(false);
		}
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 }
