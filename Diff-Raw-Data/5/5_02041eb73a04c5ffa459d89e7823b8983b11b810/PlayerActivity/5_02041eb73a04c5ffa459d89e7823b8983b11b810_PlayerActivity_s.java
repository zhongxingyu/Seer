 package com.scrye.badgertunes;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class PlayerActivity extends Activity
 	                        implements ListView.OnItemClickListener {
 
     private Handler handler = new Handler();
     private Node remote_root;
     private Node local_root;
     private Node current_dir;
     private boolean use_local;
     public String remote_address = "http://10.1.10.9:8080";
     public File local_root_dir = new File(Environment.getExternalStorageDirectory(), "badgertunes");
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
               
     	ToggleButtonBar tree_chooser_bar = (ToggleButtonBar) findViewById(R.id.tree_chooser_bar);
     	tree_chooser_bar.addButton("local", "Local", false);
     	tree_chooser_bar.addButton("remote", "Remote", true);
     	tree_chooser_bar.setListener(this);
     	
         setLocal(false);
     }
     
     private void setLocal(boolean  _use_local) {
     	use_local = _use_local;
     	fillDirectoryBrowser();
     }
     
     private void fillDirectoryBrowser() {
        	LinearLayout parent_dirs_layout = (LinearLayout) findViewById(R.id.parent_dirs);
     	parent_dirs_layout.removeAllViews();
     	
     	showLoading();
 
         // Start lengthy operation in a background thread
         new Thread(new Runnable() {
             public void run() {
             	Log.w("PlayerActivity", "loading song list");
             	if(use_local) {
             		loadLocalSongs();
             	} else {
             		loadRemoteSongs();
             	}
             	// When that is done, trigger the display of the list in the GUI thread.
                 handler.post(new Runnable() {
                     public void run() {
                         showSongList();
                     }
                 });
             }
         }).start();
     }
 
     private StringBuilder stringBuilderFromReader(InputStreamReader input_stream) throws IOException {
     	BufferedReader r = new BufferedReader(input_stream);
     	StringBuilder total = new StringBuilder();
     	String line;
     	while ((line = r.readLine()) != null) {
     		total.append(line);
     	}
     	return total;
     }
 
     // Must be run in non-GUI thread
     private void loadLocalSongs() {
         local_root = Node.readLocal(local_root_dir);
         if(local_root.children == null) {
         	local_root.children = new ArrayList<Node>();
         }
     	current_dir = local_root;
     }
     
     private void showLoading() {
     	ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
         ListView song_list_view = (ListView) findViewById(R.id.song_list);
         song_list_view.setEmptyView(progress_bar);
         
        	View empty_indicator = findViewById(R.id.empty_indicator);
         empty_indicator.setVisibility(View.GONE);
     }
     
     private void showEmpty() {
     	View empty_indicator = findViewById(R.id.empty_indicator);
     	ListView song_list_view = (ListView) findViewById(R.id.song_list);
         song_list_view.setEmptyView(empty_indicator);
         
         View progress_bar = findViewById(R.id.progress_bar);
         progress_bar.setVisibility(View.GONE);
     }
 
     // Must be run in non-GUI thread
     private void loadRemoteSongs() {
     	try{
     	    // Create a new HTTP Client
     	    DefaultHttpClient defaultClient = new DefaultHttpClient();
     	    // Setup the get request
     	    HttpGet httpGetRequest = new HttpGet(remote_address + "/list");
 
     	    // Execute the request in the client
     	    HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
     	    // Grab the response
     	    StringBuilder sb = stringBuilderFromReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
     	    
     	    // Instantiate a JSON array from the request response
     	    JSONObject json = new JSONObject(sb.toString());
             remote_root = Node.readJson( json );
             current_dir = remote_root;
 
     	} catch(Exception e){
     	    // TODO In your production code handle any errors and catch the individual exceptions
             Log.e("PlayerActivity", e.toString());
     	}
     }
     
     // Must be run in GUI thread
     private void showSongList() {
     	// set up horizontal list of parent directories
     	ArrayList<Node> parents = new ArrayList<Node>();
     	for(Node node = current_dir; node != null; node = node.parent) {
     		parents.add(node);
     	}
     	Collections.reverse(parents);
     	LinearLayout parent_dirs_layout = (LinearLayout) findViewById(R.id.parent_dirs);
     	parent_dirs_layout.removeAllViews();
     	for(int i = 0; i < parents.size(); i++) {
     		DirectoryButton dir_button = new DirectoryButton(this, parents.get(i));
     		parent_dirs_layout.addView(dir_button);
     	}
     	ArrayList<Node> list_to_display;
     	if(current_dir == null || current_dir.children == null)	{
     		list_to_display = new ArrayList<Node>();
     	} else {
     		list_to_display = current_dir.children;
     	}
     	// set up main list of songs or directories
     	NodeAdapter adapter = new NodeAdapter(this, R.id.title, list_to_display, use_local);
         ListView song_list_view = (ListView) findViewById(R.id.song_list);
     	song_list_view.setAdapter(adapter);
     	song_list_view.setOnItemClickListener(this);
     	
     	showEmpty();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.player, menu);
         return true;
     }
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
 		Node node = current_dir.children.get(position);
 		if( node != null ) {
 			if( node.children != null && node.children.size() > 0 ) {
 				setCurrentDirectory(node);
 			}
 		}
 	}
 
 	public void setCurrentDirectory(Node node) {
 		current_dir = node;
 		showSongList();
 	}
 
 	public void onToggleButtonChanged(String button_tag) {
 		boolean new_use_local = (button_tag == "local");
 		if(new_use_local != use_local) {
 			setLocal(new_use_local);
 		}
 	}
 	
 	public void onListedNodeClicked(Node node) {
 		if( node != null ) {
 			if( node.children != null && node.children.size() > 0 ) {
 				setCurrentDirectory(node);
 			}
 		}
 	}
 	
 	// Call this from GUI thread, and it spawns a thread which recursively downloads node.
 	public void downloadNode(Node node) {
 		Toast.makeText(this, "Downloading " + node.filename, Toast.LENGTH_SHORT).show();
 		// Start lengthy operation in a background thread
         new DownloadFilesTask(this, node).execute();
 	}
 	
 	// Call this from GUI thread.
 	public void playNode(Node node) {
 		Toast.makeText(this,  "Playing " + node.filename, Toast.LENGTH_SHORT).show();
 	}
 }
