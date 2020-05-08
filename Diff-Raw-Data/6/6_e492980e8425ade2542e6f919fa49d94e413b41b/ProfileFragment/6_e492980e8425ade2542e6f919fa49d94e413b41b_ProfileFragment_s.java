 package net.honeybadgerlabs.adventurequest;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.TextView;
 
 public class ProfileFragment extends Fragment {
   private GridView listView;
   private TextView button;
   private StatAdapter adapter;
 
   @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     View view = inflater.inflate(R.layout.profile, container, false);
 
     listView = (GridView) view.findViewById(R.id.attribute_list);
     button = (TextView) view.findViewById(R.id.spend_points);
 
     return view;
   }
 
   @Override public void onActivityCreated(Bundle savedInstanceState) {
     super.onActivityCreated(savedInstanceState);
     adapter = ((TitleActivity) getActivity()).getStatAdapter();
     listView.setAdapter(adapter);
     updateButton();
   }
 
   public void updateButton() {
    try {
       int points = ((TitleActivity) getActivity()).getCharLevel() - adapter.totalPoints() - 1;
 
       if (points > 0) {
         String message = getResources().getQuantityString(R.plurals.points, points, points);
         button.setText(message);
         button.setVisibility(View.VISIBLE);
       } else {
         button.setVisibility(View.GONE);
       }
    } catch (NullPointerException e) {
     }
   }
 }
