 package com.phinmadvader.andcpp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.phinvader.libjdcpp.DCFileList;
 
 public class DirectoryViewFragment extends Fragment {
 	private MainActivity mainActivity;
 	private FileListFragment filelist_fragment; // has all depth info
 	public boolean is_ready = false;
 
 	private ListView fileListView;
 	private ArrayAdapter<DCFileList> filesAdapter; // adapter adapted on
 													// curFiles
 	private List<DCFileList> curFiles; // adapter list being displayed
 	private List<DCFileList> fileList; // reference to file list, same items as
 										// curFiles
 	private int depth = 0; // current depth at FileList
 	
 	public final static String NUM_ARG_KEY = "dirfrag_num_arg";
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         depth = getArguments() != null ? getArguments().getInt(NUM_ARG_KEY) : 0;
     }
 
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mainActivity = (MainActivity) getActivity();
 		filelist_fragment = mainActivity.get_filelist_fragment();
 
 		curFiles = new ArrayList<DCFileList>();
 		filesAdapter = new FileListAdapter(mainActivity,
 				android.R.layout.simple_list_item_1, curFiles);
 		View rootview = inflater.inflate(R.layout.file_list, container, false);
 		fileListView = (ListView) rootview.findViewById(R.id.listView);
 		fileListView.setAdapter(filesAdapter);
 		fileListView
 				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> adapterView,
 							View view, int i, long l) {
 						if (fileList.get(i).isDirectory()) {
 							filelist_fragment.openDirectory(depth,
 									fileList.get(i));
 						} else {
 							// Download File
							String path = "";
 							for (int j = 1; j <= depth; j++)
								path += filelist_fragment.filelist_stack.get(j).name + "\\";
 							path +=  fileList.get(i).name;
 							Log.e("andcpp", path);
 							mainActivity.mService.download_file(
 									mainActivity.chosenNick,
 									Constants.dcDirectory + "/"
 											+ fileList.get(i).name, path,
 									fileList.get(i).size);
 
 							mainActivity.runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									Toast.makeText(mainActivity,
 											"Download Started",
 											Toast.LENGTH_LONG).show();
 								}
 							});
 						}
 					}
 				});
 
 		curFiles.clear();
 		fileList = filelist_fragment.filelist_stack.get(depth).children;
 		for (int i = 0; i < fileList.size(); i++) {
 			curFiles.add(fileList.get(i));
 		}
 		filesAdapter.notifyDataSetChanged();
 
 		is_ready = true;
 		return rootview;
 	}
     static DirectoryViewFragment newInstance(int num) {
         DirectoryViewFragment f = new DirectoryViewFragment();
 
         Bundle args = new Bundle();
         args.putInt(NUM_ARG_KEY, num);
         f.setArguments(args);
 
         return f;
     }
 
 }
