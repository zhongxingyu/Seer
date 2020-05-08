 package com.lookout.keymaster.fragments;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import com.lookout.keymaster.gpg.GPGFactory;
 import com.lookout.keymaster.R;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ayerra
  * Date: 5/29/13
  * Time: 10:25 PM
  * To change this template use File | Settings | File Templates.
  */
 public class KeyFragment extends Fragment {
     SimpleAdapter adapter;
 
     public KeyFragment() {
         // Empty constructor required for fragment subclasses
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         View rootView = inflater.inflate(R.layout.fragment_key, container, false);
 
         GPGFactory.buildData();
 
         ListView lv = (ListView) rootView.findViewById(R.id.keyView);
        String[] from = { "full_name", "short_id", "email" };
        int[] to = { R.id.full_name, R.id.short_id, R.id.email };
         adapter = new SimpleAdapter(rootView.getContext(), GPGFactory.getKeys(), R.layout.key_list_item, from, to);
         lv.setAdapter(adapter);
 
         getActivity().setTitle("Public Keys");
         return rootView;
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         if(adapter != null) {
             adapter.notifyDataSetChanged();
         }
     }
 }
