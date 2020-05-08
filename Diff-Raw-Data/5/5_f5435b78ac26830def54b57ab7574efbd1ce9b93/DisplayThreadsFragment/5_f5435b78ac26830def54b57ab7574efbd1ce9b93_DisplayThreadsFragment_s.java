 package edu.rit.csh.androidwebnews;
 
 import java.util.ArrayList;
 
 import android.support.v4.app.Fragment;
 import android.app.ProgressDialog;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class DisplayThreadsFragment extends Fragment {
 	String newsgroupName;
 	ArrayList<PostThread> threads;
 	ArrayList<PostThread> rootThreads;
 	boolean[] threadStatus;
 	DisplayThreadsListAdapter<PostThread> listAdapter;
 	int[] extraEntries;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 
 		Log.d("MyDebugging", "Starting ThreadsListFragment constructor");
 		newsgroupName = ((DisplayThreadsActivity)getActivity()).newsgroupName;
 
 		ListView mainListView = new ListView(getActivity());
 
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
 	    String apiKey = sharedPref.getString("api_key", "");
 		HttpsConnector hc = new HttpsConnector(apiKey, getActivity());
 		hc.getNewsgroupThreads(newsgroupName, 20);
 	    threads = new ArrayList<PostThread>();//hc.getNewsgroupThreads(newsgroupName, 20);
 	    rootThreads = new ArrayList<PostThread>();
 	    
 	    for(PostThread thread : threads)
 	    	rootThreads.add(thread);
 
 	    for(PostThread thread : threads)
 	    {
 	    	((DisplayThreadsActivity)getActivity()).threadsDirectMap.add(thread);
 	    }
 
 	    threadStatus = new boolean[20];
 	    extraEntries = new int[20];
 
 	    for(int x = 0; x < 20 ; x++)
 	    {
 	    	threadStatus[x] = false;
 	    	extraEntries[x] = 0;
 	    }
 		Log.d("MyDebugging", "displayedStrings populated");
 
 	    listAdapter = new DisplayThreadsListAdapter<PostThread>(getActivity(), R.layout.threadlayout, threads);
 		Log.d("MyDebugging", "list adapter made");
 		
 		
 		
 	    mainListView.setAdapter(listAdapter);
 		Log.d("MyDebugging", "listadapter set");
 		mainListView.setOnItemClickListener(new OnItemClickListener()
 		{
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View arg1, int position,
 					long id) {
 				int originalPos = findOriginalPos(((DisplayThreadsActivity)getActivity()).threadsDirectMap.get(position));
				Log.d("MyDebugging", "item " + position + " clicked on");
 				Log.d("MyDebugging", "original position is " + originalPos);
 				Log.d("MyDebugging", "threadStatus[originalPos] = " + threadStatus[originalPos]);
 				Log.d("MyDebugging", "extraEntries[originalPos] = " + extraEntries[originalPos]);
				Log.d("MyDebugging", "sub threads of threads.get(originalPosition) = " + rootThreads.get(originalPos).getSubThreadCount());
 				if(originalPos > -1)
 				{
 					if(threadStatus[originalPos])
 					{
 						for(int x = 0; x < extraEntries[originalPos]; x++)
 						{
 							threads.remove(position + 1);
 							((DisplayThreadsActivity)getActivity()).threadsDirectMap.remove(position + 1);
 						}
 						extraEntries[originalPos] = 0;
 						listAdapter.clear();
 						listAdapter.addAll(threads);
 						listAdapter.notifyDataSetChanged();
 						threadStatus[originalPos] = false;					
 					}
 					else
 					{
 						
 						expandThread(rootThreads.get(originalPos), position);
 						listAdapter.clear();
 						listAdapter.addAll(threads);
 						listAdapter.notifyDataSetChanged();
 						threadStatus[originalPos] = true;					
 					}
 				}
 			}
 
 		});
 		Log.d("MyDebugging", "ThreadsListFragment made");
 
 	    return mainListView;
 	}
 
 	public void expandThread(PostThread thread, int pos)
 	{
 		for(int x = thread.children.size() - 1; x > -1; x--)
 		{
 			PostThread childThread = thread.children.get(x);
 			int originalPos = findOriginalPos(thread);
 			if(originalPos > -1)
 			{
 				if(childThread.children.size() != 0) {
 					Log.d("output", childThread.depth + childThread.authorName);
 					expandThread(childThread, originalPos, pos, 2);
 				}
 				
 				threads.add(pos + 1, childThread);
 				extraEntries[originalPos] += 1;
 				((DisplayThreadsActivity)getActivity()).threadsDirectMap.add(pos+1, childThread);
 				//pos += thread.children.size();
 			}
 		}
 	}
 
 	private void expandThread(PostThread thread, int originalPos, int pos, int level)
 	{
 		String temp = "";
 		for(int i = 0; i < level; i++)
 			temp += "||";
 		for(int x = thread.children.size() - 1; x > -1; x--)
 		{
 			PostThread childThread = thread.children.get(x);
 			if(childThread.children.size() != 0) {
 				Log.d("output", childThread.depth + childThread.authorName);
 				expandThread(childThread, originalPos, pos, level + 1);
 				
 			}
 			
 			threads.add(pos + 1, childThread);
 			extraEntries[originalPos] += 1;
 			((DisplayThreadsActivity)getActivity()).threadsDirectMap.add(pos+1, childThread);
 			//pos += thread.children.size();
 		}
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		if(listAdapter != null)
 			listAdapter.notifyDataSetChanged();
 	}
 	
 	public void update(ArrayList<PostThread> threads)
 	{
 		this.threads = threads;
 		rootThreads.clear();
 		((DisplayThreadsActivity)getActivity()).threadsDirectMap.clear();
 		for(PostThread thread : threads)
 		{
 	    	rootThreads.add(thread);
 	    	((DisplayThreadsActivity)getActivity()).threadsDirectMap.add(thread);
 		}
 		for(boolean b : threadStatus)
 			b = false;
 		for(int l : extraEntries)
 			l = 0;
 		
 		// Opens threads with unread posts in them
 				ArrayList<Integer> toOpenIndexes = new ArrayList<Integer>(); // list of indexes to open
 				for (int i = threads.size() - 1 ; i >= 0  ; i--) {
 					Log.d("ints", threads.size() + "");
 					if (threads.get(i).containsUnread()) {
 						int originalPos = findOriginalPos(((DisplayThreadsActivity)getActivity()).threadsDirectMap.get(i));
 						Log.d("ints", originalPos + ":" + i);
 						expandThread(threads.get(originalPos), i);
 						listAdapter.notifyDataSetChanged();
 						threadStatus[originalPos] = true;	
 					}
 					
 				}
 				// Opens the unread posts in the list of indexes
 				for (Integer i : toOpenIndexes) {
 					int originalPos = findOriginalPos(((DisplayThreadsActivity)getActivity()).threadsDirectMap.get(i));
 					Log.d("ints", originalPos + ":" + i);
 					expandThread(threads.get(originalPos), i);
 					listAdapter.notifyDataSetChanged();
 					threadStatus[originalPos] = true;	
 				}
 		
 		listAdapter.clear();
 		listAdapter.addAll(threads);
 		listAdapter.notifyDataSetChanged();
 	}
 
 	private int findOriginalPos(PostThread thread)
 	{
 		Log.d("MyDebugging", thread.authorName + "'s original pos requested");
 		for(int x = 0; x < rootThreads.size(); x++)
 			if(rootThreads.get(x).Equals(thread))
 			{
 				Log.d("MyDebugging", "returning " + x);
 				return x;
 			}
 		return -1;
 	}
 }
