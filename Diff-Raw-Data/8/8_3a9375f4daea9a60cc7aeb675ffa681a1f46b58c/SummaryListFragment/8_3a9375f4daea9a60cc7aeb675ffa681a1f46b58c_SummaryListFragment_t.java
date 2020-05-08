 package uk.co.huntersix.android.comic;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.ListAdapter;
 
 public class SummaryListFragment extends Fragment {
	private ListAdapter adapter = null;
	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 	    View view = inflater.inflate(R.layout.summary_list, container, false);
 
 	    GridView gridview = (GridView) view.findViewById(R.id.gridview);
	    if (adapter == null) {
	    	adapter = new SummaryListAdapter(this.getActivity().getApplicationContext());
	    }
	    
 	    gridview.setAdapter(adapter);
 
 	    gridview.setOnItemClickListener(new OnItemClickListener() {
 	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 	        	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
 	        	
 	        	SummaryDetailsFragment summaryDetailsFragment = new SummaryDetailsFragment();
 	        	fragmentTransaction.replace(R.id.summarylist, summaryDetailsFragment);
 	        	fragmentTransaction.addToBackStack(null);
 	        	fragmentTransaction.commit();
 	        }
 	    });
 
 	    return view;
     }
 }
