 package com.supernaiba.ui;
 
 import java.util.List;
 
 import greendroid.app.GDListActivity;
 import greendroid.widget.LoaderActionBarItem;
 import greendroid.widget.ActionBar.OnActionBarListener;
 import greendroid.widget.ActionBarItem.Type;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 import com.parse.ParseAnalytics;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseQueryAdapter.OnQueryLoadListener;
 import com.parse.ParseQueryAdapter.QueryFactory;
 import com.parse.ParseUser;
 import com.supernaiba.R;
 import com.supernaiba.parse.Query;
 import com.supernaiba.parse.QueryAdapter;
 
 public class ShowFavorites extends GDListActivity {
 	private LoaderActionBarItem refreshAction;
 	QueryAdapter<ParseObject> adapter;
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ParseAnalytics.trackAppOpened(getIntent());
 		
 		this.setTitle(getString(R.string.favorites));
 		refreshAction=(LoaderActionBarItem)addActionBarItem(Type.Refresh);
 		refreshAction.setLoading(true);
 		this.getActionBar().setOnActionBarListener(new OnActionBarListener(){
 
 			@Override
 			public void onActionBarItemClicked(int position) {
 				switch(position){
 				case 0:
 					refresh();
 					break;
 				default:
 					ShowFavorites.this.onBackPressed();
 				break;
 				}
 			}
 			
 		});
 		
 		adapter=new QueryAdapter<ParseObject>(this,new QueryFactory<ParseObject>(){
 			@Override
 			public ParseQuery<ParseObject> create() {
 				Query<ParseObject> query=new Query<ParseObject>("favorite");
 				query.whereEqualTo("owner", ParseUser.getCurrentUser());
 				return query;
 			}
 		});
 		adapter.addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>(){
 
 			@Override
 			public void onLoaded(List<ParseObject> objects, Exception arg1) {
 				refreshAction.setLoading(false);
 			}
 
 			@Override
 			public void onLoading() {
 				refreshAction.setLoading(true);
 			}
 			
 		});
 		adapter.setTextKey("title");
		adapter.setImageKey("thumb");
 		this.setListAdapter(adapter);
 	}
 
 	protected void refresh() {
 		adapter.clear();
 		adapter.loadObjects();
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent intent=new Intent(this,ShowPost.class);
 		intent.putExtra("ID", ((ParseObject)v.getTag()).getObjectId());
 		this.startActivity(intent);
 	}
 
 }
