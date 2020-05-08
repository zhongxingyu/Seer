 package com.restaurant.fragment;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.restaurant.adapter.NoteGridViewAdapter;
 import com.restaurant.collection.R;
 import com.restaurant.collection.api.RestaurantAPI;
 import com.restaurant.collection.db.SQLiteRestaurant;
 import com.restaurant.collection.entity.Note;
 import com.restaurant.customized.view.LoadMoreGridView;
 
 @SuppressLint("ValidFragment")
 public class GridEatNoteFragment extends Fragment {
 
     private ArrayList<Note>             notes           = new ArrayList<Note>();
     private LoadMoreGridView                  myGrid;
     private NoteGridViewAdapter               myGridViewAdapter;
     private LinearLayout                      progressLayout;
     private LinearLayout                      loadmoreLayout;
     private LinearLayout                      layoutReload;
     private Button                            buttonReload;
 
     private int                               myPage          = 1;
     private Boolean                           checkLoad       = true;
     private  ArrayList<Note> moreNotes = new ArrayList<Note>();
     private int area_id;
 	private int category_id;
 	private int type_id;
 	private boolean is_collection;
 	private boolean is_selected;
 	private LinearLayout noDataLayout;
 	private int rank_category_id;
 
     public GridEatNoteFragment() {
 
     }
     
     public static final GridEatNoteFragment newInstance(int area_id,int rank_category_id, int category_id, int type_id, boolean is_collection, boolean is_selected) {
     	GridEatNoteFragment f = new GridEatNoteFragment();
         Bundle bdl = new Bundle();
         bdl.putInt("AreaId", area_id);
         bdl.putInt("CategoryId", category_id);
         bdl.putInt("RankCategoryId", rank_category_id);
         bdl.putInt("TypeId", type_id);
         bdl.putBoolean("IsCollection", is_collection);
         bdl.putBoolean("IsSelected", is_selected);
         f.setArguments(bdl);
         return f;
     }
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	area_id = getArguments().getInt("AreaId");
 		rank_category_id = getArguments().getInt("RankCategoryId");
 		category_id = getArguments().getInt("CategoryId");
 		type_id = getArguments().getInt("TypeId");
 		is_collection = getArguments().getBoolean("IsCollection");
 		is_selected = getArguments().getBoolean("IsSelected");
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View myFragmentView = inflater.inflate(R.layout.loadmore_grid, container, false);
         progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
         loadmoreLayout = (LinearLayout) myFragmentView.findViewById(R.id.load_more_grid);
         layoutReload = (LinearLayout) myFragmentView.findViewById(R.id.layout_reload);
         noDataLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_no_data);
         buttonReload = (Button) myFragmentView.findViewById(R.id.button_reload);
         myGrid = (LoadMoreGridView) myFragmentView.findViewById(R.id.news_list);
         myGrid.setOnLoadMoreListener(new LoadMoreGridView.OnLoadMoreListener() {
             public void onLoadMore() {
                 if (checkLoad) {
                     myPage = myPage + 1;
                     new LoadMoreTask().execute();
                 } else {
                     myGrid.onLoadMoreComplete();
                 }
             }
         });
 
         buttonReload.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 progressLayout.setVisibility(View.VISIBLE);
                 layoutReload.setVisibility(View.GONE);
                 new DownloadNotesTask().execute();
             }
         });
 
         if (myGridViewAdapter != null) {
             progressLayout.setVisibility(View.GONE);
             loadmoreLayout.setVisibility(View.GONE);
             myGrid.setAdapter(myGridViewAdapter);
         } else {
             new DownloadNotesTask().execute();
         }
 
         return myFragmentView;
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
     }
 
     private class DownloadNotesTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
 
         }
 
         @Override
         protected Object doInBackground(Object... params) {
         	
         	if(area_id !=0 && category_id != 0){
         		notes = RestaurantAPI.getAreaCategoryNotes(area_id, category_id, 1);
         	}else if(area_id != 0 && rank_category_id != 0){
//        		notes = RestaurantAPI.get(area_id, type_id, 1);
         	}else if(area_id != 0 && type_id != 0){
         		notes = RestaurantAPI.getAreaTypeNotes(area_id, type_id, 1);
         	}else if(is_collection){
         		SQLiteRestaurant db = new SQLiteRestaurant(getActivity());
         		notes = db.getAllNotes();
         	}else if(is_selected){
         		notes = RestaurantAPI.getSelectNotes(1);
         	}
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             progressLayout.setVisibility(View.GONE);
             loadmoreLayout.setVisibility(View.GONE);
 
             if (notes != null && notes.size() != 0) {
                 try {
                     layoutReload.setVisibility(View.GONE);
                     myGridViewAdapter = new NoteGridViewAdapter(getActivity(), notes);
                     myGrid.setAdapter(myGridViewAdapter);
                 } catch (Exception e) {
 
                 }
             } else if(notes != null && notes.size() == 0){
             	noDataLayout.setVisibility(View.VISIBLE);
             	layoutReload.setVisibility(View.GONE);
             } else {
                 layoutReload.setVisibility(View.VISIBLE);
             }
 
         }
     }
 
     private class LoadMoreTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             loadmoreLayout.setVisibility(View.VISIBLE);
 
         }
 
         @Override
         protected Object doInBackground(Object... params) {
 
         	moreNotes.clear();
         	if(area_id !=0 && category_id != 0){
         		moreNotes = RestaurantAPI.getAreaCategoryNotes(area_id, category_id, myPage);
         	}else if(area_id != 0 && rank_category_id != 0){
//        		moreNotes = RestaurantAPI.get(area_id, type_id, 1);
         	}else if(area_id != 0 && type_id != 0){
         		moreNotes = RestaurantAPI.getAreaTypeNotes(area_id, type_id, myPage);
         	}else if(is_selected){
         		moreNotes = RestaurantAPI.getSelectNotes(myPage);
         	}
         	
         	for (int i = 0; i < moreNotes.size(); i++) {
         		notes.add(moreNotes.get(i));
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             loadmoreLayout.setVisibility(View.GONE);
 
             if (moreNotes != null && moreNotes.size() != 0) {
                 myGridViewAdapter.notifyDataSetChanged();
             } else {
                 checkLoad = false;
                 Toast.makeText(getActivity(), "no more data", Toast.LENGTH_SHORT).show();
             }
             myGrid.onLoadMoreComplete();
 
         }
     }
 
 }
