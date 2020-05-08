 package com.wajumbie.nasadailyimage;
 
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.Fragment;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.ListFragment;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 
 @SuppressLint("ValidFragment")
 public class BreakingNewsFragment extends ListFragment {
 	private static Activity mainActivity;
 	private static ArrayList<Story> stories=new ArrayList<Story>();
 	private static ArrayList<String> storyTitles=new ArrayList<String>();
	private static RssNewsParser parser;
 	
 	public BreakingNewsFragment(){
 		
 	}
 	public BreakingNewsFragment(Activity mainActivity){
 		this.mainActivity=mainActivity;
 		parser=new RssNewsParser(mainActivity,this);
 	}
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		
 		return super.onCreateView(inflater, container, savedInstanceState);
 	}
 
 	@Override
 	public void onHiddenChanged(boolean hidden) {
 		// TODO Auto-generated method stub
 		super.onHiddenChanged(hidden);
 		
 	}
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 			
 			
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		super.onListItemClick(l, v, position, id);
 		getStories();
 		String url = stories.get(position).getURL();
 		Intent i = new Intent(Intent.ACTION_VIEW);
 		i.setData(Uri.parse(url));
 		startActivity(i);
 	}
 	public void fetchStories(){
 		
 		parser=new RssNewsParser(mainActivity,this);
 		if(stories.isEmpty()){
 		parser.execute();
 		}
 	}
 	public ArrayList<Story> getStories(){
 		try {
 			stories=parser.get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 		return null;
 	
 	}
 public void updateList(){
 	setListAdapter(new ArrayAdapter<String>(mainActivity,android.R.layout.simple_list_item_1,storyTitles));
 }
 	@Override
 	public void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		
 	
 		
 		
 	}
 	public void onRefresh() {
 		fetchStories();
 		
 	}
 
 }
