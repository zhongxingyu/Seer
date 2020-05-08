 package com.example.example.masterdetail_tabs;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class Tab1Fragment extends Fragment {
 
   public Tab1Fragment() {
   }
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
       Bundle savedInstanceState) {
     View rootView = inflater.inflate(R.layout.activity_tab1_list, container, false);
     return rootView;
   }
 }
