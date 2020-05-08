 package de.hsbremen.android.dribl.fragments;
 
 import de.hsbremen.android.dribl.R;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class ImageFragment extends Fragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.component_image, container, false);
 		return view;
 	}
 	
 }
