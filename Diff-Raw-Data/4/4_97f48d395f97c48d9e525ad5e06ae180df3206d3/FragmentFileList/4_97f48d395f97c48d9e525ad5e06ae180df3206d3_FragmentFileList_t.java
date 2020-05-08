 package info.mabin.android.fakemalwareapp;
 
 import info.mabin.android.fakemalwareapp.R;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import android.os.Bundle;
import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 public class FragmentFileList extends SherlockFragment {
 	List<Map<String, String>> files;
	private final static String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();;
 	
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_filelist, container, false);
         
         ListView listViewFiles = (ListView) rootView.findViewById(R.id.listViewFiles);
         
         files = new ArrayList<Map<String, String>>();
         
         File sdcardFolder = new File(sdcardPath);
         File[] fileArr = sdcardFolder.listFiles();
         
         for(int i = 0; i < fileArr.length; i++){
         	Map<String, String> file = new HashMap<String, String>();
         	file.put("file_name", fileArr[i].getName());
         	files.add(file);
         }
         
         Log.d("filelength", fileArr.length + "");
         
         AdapterFiles adapterFiles = new AdapterFiles(getActivity(), R.layout.listitem_file, files);
         listViewFiles.setAdapter(adapterFiles);
         
         return rootView;
     }
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
     	//first saving my state, so the bundle wont be empty. 
     	outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE"); 
     	super.onSaveInstanceState(outState); 
     }
 }
