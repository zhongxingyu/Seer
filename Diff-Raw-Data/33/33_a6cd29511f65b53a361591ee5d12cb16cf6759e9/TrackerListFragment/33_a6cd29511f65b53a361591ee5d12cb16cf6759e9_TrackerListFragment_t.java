 package com.valfom.tracker;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView.MultiChoiceModeListener;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 
 public class TrackerListFragment extends SherlockListFragment {
 
 	@Override
 	public void onResume() {
 
 		super.onResume();
 		
 		loadTracks();
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 
 		super.onActivityCreated(savedInstanceState);
 		
 		getListView().setEmptyView(getActivity().findViewById(R.id.empty));
 		
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			
 			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 			getListView().setMultiChoiceModeListener(new MultiChoiceModeListener() {
 
 			    public void onItemCheckedStateChanged(ActionMode mode, int position,
 			                                      long id, boolean checked) {}
 		
 			    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 		
 			    	switch (item.getItemId()) {
 			    	
 			            case R.id.menu_row_delete:
 			            	
 			            	DB db = new DB(getActivity());
 			            	
 			            	SparseBooleanArray checked = getListView().getCheckedItemPositions();
 			            	
 			                for (int i = 0; i < checked.size(); i++) {
 			                 
 			                	if(checked.valueAt(i) == true) {
 			                    
 			                		Cursor c = (Cursor) getListView().getItemAtPosition(checked.keyAt(i));
 			                		db.deleteTrack(c.getInt(0));
 			                    }
 			                }
 			                
 			                loadTracks();
 			                
 			                mode.finish();
 			                return true;
 			            default:
 			                return false;
 			        }
 			    }
 		
 			    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 		
 			    	MenuInflater inflater = mode.getMenuInflater();
 			        inflater.inflate(R.menu.menu_list_row_selection, menu);
 			        
 			        return true;
 			    }
 		
 			    public void onDestroyActionMode(ActionMode mode) {}
 		
 			    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			        
 			    	return false;
 			    }
 			});
 		} else {
 			
 			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			registerForContextMenu(getListView());
 		}
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 
 		menu.add(Menu.NONE, 0, 0, "Delete");
 		
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		AdapterView.AdapterContextMenuInfo info;
 		
 	    try {
 	        
 	    	info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 	    } catch (ClassCastException e) {
 	        
 	    	return false;
 	    }
 	    
 	    TrackerListFragment frList = (TrackerListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.pager);
 	    int trackId = (int) frList.getListAdapter().getItemId(info.position);
 	    
 	    DB db = new DB(getActivity());
 		db.deleteTrack(trackId);
 		
 		frList.loadTracks();
 		
 	    return true;
 	}
 	
 	public void loadTracks() {
 		
 		DB db = new DB(getActivity());
 		
 		Cursor cursor = db.getAllTracks();
 		
 	    String[] from = new String[] { DB.KEY_PREFIX_ID, DB.KEY_DATE, 
 	    		DB.KEY_DIST, DB.KEY_DURATION };
 	    int[] to = new int[] { R.id.tvId, R.id.tvDate, R.id.tvDistance, R.id.tvDuration };
 	    
 	    SimpleCursorAdapter scAdapter;
 	    
	    scAdapter = new TrackerSimpleCursorAdapter(getActivity(), R.layout.list_row, cursor, from, to);
 		
 	    setListAdapter(scAdapter);
 	    
 	    db.close();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		return inflater.inflate(R.layout.fragment_list, null);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 
 		super.onListItemClick(l, v, position, id);
 		
 		TextView idTV = (TextView) v.findViewById(R.id.tvId);
 		int trackId = Integer.parseInt(idTV.getText().toString());
 		
 		Intent trackInfo = new Intent(getActivity(), TrackerInfoActivity.class);
 		trackInfo.putExtra("trackId", trackId);
 		startActivity(trackInfo);
 	}
 }
