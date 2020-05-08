 package edu.rit.csh.androidwebnews;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class RecentFragment extends Fragment {
 	ListView lv;
 	RecentListAdapter<PostThread> listAdapter;
 	NewsgroupListMenu newsgroupListMenu;
 	ArrayList<PostThread> recentPostThreads;
 	HttpsConnector hc;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		Log.d("jddebug", "frag");
 		newsgroupListMenu = ((RecentActivity)getActivity()).newsgroupListMenu;
 		lv = new WebnewsListView(getActivity(), newsgroupListMenu);
 		
 		recentPostThreads = new ArrayList<PostThread>();
 		
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
 	    String apiKey = sharedPref.getString("api_key", "");
 		hc = new HttpsConnector(apiKey, getActivity());
 		
 		
 		
 		listAdapter = new RecentListAdapter<PostThread>(getActivity(), R.layout.rowlayout, new ArrayList<PostThread>());
 		lv.setAdapter(listAdapter);
 		
 
 		lv.setOnItemClickListener(new OnItemClickListener()
 		{
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View arg1, int position, long id) {
 				PostThread thread = (PostThread) adapter.getItemAtPosition(position);
 				((RecentActivity)getActivity()).onThreadSelected(thread);
 				
 			}
 				
 
 		});
 		
 		
 		
 		return lv;
 	}
 	
 
 	public void update(ArrayList<PostThread> newestFromString) {
 		listAdapter.clear();
 		recentPostThreads = newestFromString;
 		for (PostThread t : newestFromString) {
 			listAdapter.add(t);
 		}
 		listAdapter.notifyDataSetChanged();
 		
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		Log.d("MyDebugging", "Refreshing view!");
 		if(listAdapter != null)
 		{
 			hc.getNewsGroups();
			hc.getNewest(false);
 		}
 	}
 }
