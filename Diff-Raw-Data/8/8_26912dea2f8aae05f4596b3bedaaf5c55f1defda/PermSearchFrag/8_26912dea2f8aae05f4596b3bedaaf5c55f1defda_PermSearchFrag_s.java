 package edu.nkuresearch.securitychecker.fragments;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import edu.nkuresearch.securitychecker.R;
 
 public class PermSearchFrag extends Fragment{
 
 	private ListView lv;
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.app_list, container, false);
 		lv = (ListView) v.findViewById(R.id.applist);
 		return v;
 	}
 	
 	private void readInPerms(){
		getActivity().getAssets().open(fileName);
 	}
 }
