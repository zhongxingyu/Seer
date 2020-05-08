 package org.kronstadt;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.kronstadt.network.HTTPClient;
 import org.kronstadt.util.FileUtil;
 import org.kronstadt.util.Util;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class FileSystemActivity extends ListActivity {
 
 	private HTTPClient http;
 	private String pwd;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		http = new HTTPClient(getApplicationContext());
 
 		pwd = "/";
 		loadList(pwd);
 	}
 
 	public void loadList(String path) {
 		loadList(path, false);
 	}
 
 	public void loadBookmarks() {
 		loadList(pwd, true);
 	}
 
 	private void loadList(String path, boolean bookmarks) {
 		ArrayList<String> files = new ArrayList<String>();
 		if (!bookmarks) {
 			files.add("bookmarks");
 		}
 
 		String jsonString = http.ls(path, bookmarks);
 		try {
 			JSONObject response = new JSONObject(jsonString);
 			JSONArray fileNames = response.getJSONArray("file_names");
 			for (int i = 0; i < fileNames.length(); i++) {
 				files.add((String) fileNames.get(i));
 			}
 			pwd = response.getString("pwd");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		Util.log(pwd);
 
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.file_system,
 				files));
 
 		ListView listView = getListView();
 		listView.setTextFilterEnabled(true);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String s = (String) (((TextView) view).getText());
 				if (position == 0 && s.equals("bookmarks")) {
 					loadBookmarks();
 					return;
 				}
 
 				loadList(FileUtil.join(pwd, s));
 			}
 		});
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			if (!pwd.equals("/")) {
 				String prevDir = FileUtil.prevDir(pwd);
 				loadList(prevDir);
 				Util.log("back button pressed");
 			}
			return false;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 }
