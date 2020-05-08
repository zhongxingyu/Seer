 //        Guidebook is an Android application that reads audioguides using Text-to-Speech services.
 //        Copyright (C) 2013  Adrián Romero Corchado
 //
 //        This program is free software: you can redistribute it and/or modify
 //        it under the terms of the GNU General Public License as published by
 //        the Free Software Foundation, either version 3 of the License, or
 //        (at your option) any later version.
 //
 //        This program is distributed in the hope that it will be useful,
 //        but WITHOUT ANY WARRANTY; without even the implied warranty of
 //        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //        GNU General Public License for more details.
 //
 //        You should have received a copy of the GNU General Public License
 //        along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.adrguides;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 /**
  * Created by adrian on 28/09/13.
  */
 public class LoadFragment extends Fragment implements LoadGuideFragment.LoadGuideCallbacks {
 
     public final static String TAG = "LOAD_FRAGMENT";
 
     private View v;
     private LoadGuideFragment loadguide;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         v = inflater.inflate(R.layout.activity_load, container, false);
 
         if (savedInstanceState != null) {
             ((TextView) v.findViewById(R.id.textException)).setText(savedInstanceState.getCharSequence("_textException"));
             if (savedInstanceState.getInt("_displayedChild") == 1) {
                 ((ViewSwitcher) v.findViewById(R.id.mySwitcher)).showNext();
             }
         }
 
         // Calculate the title to display.
         String title = getActivity().getIntent().getStringExtra(ReadGuideActivity.ARG_GUIDE_TITLE);
         if (title == null || title.equals("")) {
            if (getActivity().getIntent().getData() == null) {
                title = "";
            } else {
                title = getActivity().getIntent().getData().getLastPathSegment();
            }
         }
 
         ((TextView) v.findViewById(R.id.textGuideName)).setText(
                 getResources().getString(R.string.msg_loading, title));
 
         return v;
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         loadguide = (LoadGuideFragment) getFragmentManager().findFragmentByTag(LoadGuideFragment.TAG);
         loadguide.setLoadListener(this);
     }
     @Override
     public void onStop() {
         loadguide.setLoadListener(null);
         loadguide = null;
 
         super.onStop();
     }
 
     @Override
     public void  onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
 
         savedInstanceState.putCharSequence("_textException", ((TextView) v.findViewById(R.id.textException)).getText());
         savedInstanceState.putInt("_displayedChild", ((ViewSwitcher) v.findViewById(R.id.mySwitcher)).getDisplayedChild());
     }
 
     @Override
     public void onFinishLoad(LoadedGuide result) {
 
         if (result.getStatus() == 0) {
             Log.d("com.adrguides.LoadFragment", "going loading");
             ((ReadGuideActivity) getActivity()).playGuide(result.getGuide());
         } else {
             ((TextView) v.findViewById(R.id.textException)).setText(result.getException());
             ViewSwitcher sw = (ViewSwitcher) v.findViewById(R.id.mySwitcher);
             sw.showNext();
         }
     }
 }
