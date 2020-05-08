 package com.teamluper.luper;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EFragment;
 
 @EFragment
 public class TabHomeFragment extends Fragment {
   SQLiteDataSource dataSource = null;
   User activeUser = null;
 
   @Override
 
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
   }
 
   @Override
   public View onCreateView(LayoutInflater infl, ViewGroup vg, Bundle state) {
     if(vg == null) return null;
     View v = infl.inflate(R.layout.tab_home_layout, vg, false);
     dataSource = ((LuperMainActivity) getActivity()).getDataSource();
     if(dataSource != null) {
       activeUser = dataSource.getActiveUser();
       if(activeUser != null) {
        ((TextView) v.findViewById(R.id.mainWelcome)).setText("Welcome to Lüper, "+activeUser.getUsername()+"!");
       }
     }

     /*String video_path = "http://www.youtube.com/watch?v=fpo58HKmZXs";
     Uri uri = Uri.parse(video_path);
     uri = Uri.parse("vnd.youtube:"  + uri.getQueryParameter("v"));
     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
     startActivity(intent);*/

     return v;
   }
 
 }
