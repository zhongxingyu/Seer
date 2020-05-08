 package de.hsbremen.android.dribl.fragments;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import de.hsbremen.android.dribl.R;
 import de.hsbremen.android.dribl.adapter.IconTextArrayAdapter;
 
 public class InfoFragment extends Fragment {
 	
 	private ListAdapter mListAdapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Retain this fragment between orientation changes
 		setRetainInstance(true);
 		
 		// Prepare info content
 		String[] texts = getResources().getStringArray(R.array.detail_list);
 		for (int i = 0; i < texts.length; ++i) {
 			texts[i] = "1337 " + texts[i]; 
 		}
 		int[] icons = {
 				R.drawable.icon_likes,
 				R.drawable.icon_buckets,
 				R.drawable.icon_views
 		};
 		
 		// Create the listadapter
 		mListAdapter = new IconTextArrayAdapter(getActivity(), icons, texts, R.layout.row_icontext) {
 			@Override
 			public boolean isEnabled(int position) {
 				// Make all items in this list non-clickable
 				return false;
 			}
 		};
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_imageinfo, container, false);
 
 		ListView detailList = (ListView) view.findViewById(R.id.detailList);
 		detailList.setAdapter(mListAdapter);
 		
 		return view;
 	}		
 
 }
