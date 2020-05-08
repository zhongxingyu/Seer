 package com.jpqr.listpad.fragments;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.jpqr.listpad.R;
 import com.jpqr.listpad.activities.EditActivity;
 import com.jpqr.listpad.adapters.FileListAdapter;
 import com.jpqr.listpad.database.FilesDataSource;
 
 public class ListFilesFragment extends SherlockFragment {
 	public static final String ARG_TYPE = "type";
 	private ListView mListView;
 	private ArrayList<File> mFiles;
 	private int mType;
 	private FilesDataSource mDataSource;
 	private View mEmptyText;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.list_files_fragment, container, false);
 		mListView = (ListView) view.findViewById(R.id.list_files);
 		mEmptyText = view.findViewById(R.id.empty_text);
 		mDataSource = new FilesDataSource(getActivity());
 		mDataSource.open();
 
 		mType = getArguments().getInt(ARG_TYPE);
 		mFiles = mDataSource.getAllFiles(mType);
 
 		mListView.setAdapter(new FileListAdapter(getActivity(), mFiles));
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				EditActivity.newInstance(getActivity(), mFiles.get(position).getAbsolutePath());
 			}
 
 		});
 		registerForContextMenu(mListView);
 		
 		updateEmptyText();
 		mDataSource.close();
 		return view;
 	}
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mDataSource.open();
 		mFiles.clear();
 		mFiles.addAll(mDataSource.getAllFiles(mType));
 		mListView.invalidateViews();
 		updateEmptyText();
 		mDataSource.close();
 
 	}
 	
 	private void updateEmptyText() {
 		if (mFiles.size() == 0) {
 			mEmptyText.setVisibility(View.VISIBLE);
 		} else {
 			mEmptyText.setVisibility(View.GONE);
 		}
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		if (view.getId() == R.id.list_files) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 			menu.setHeaderTitle(mFiles.get(info.position).getName());
 			switch (mType) {
 				case FilesDataSource.Type.FAVOURITE:
					menu.add("Remove from favorites");
 				break;
 				case FilesDataSource.Type.RECENT:
					menu.add("Remove from recent");
 				break;
 			}
 		}
 	}
  
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		File file = mFiles.get(info.position);
 		mDataSource.open();
 		int deleted = mDataSource.deleteFile(file.getAbsolutePath(), mType);
 		mDataSource.close();
 		if (deleted >= 1) {
 			mFiles.remove(info.position);
 		} else {
 			Toast.makeText(getActivity(), "Problem removing file", Toast.LENGTH_LONG).show();
 		}
 		mListView.invalidateViews();
 		updateEmptyText();
 		
 		return super.onContextItemSelected(item);
 	}
 
 }
