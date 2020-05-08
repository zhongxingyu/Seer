 package kap.placity_beta;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import android.os.Bundle;
import android.os.SystemClock;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ChooseGame extends ListActivity {
 	
 	ArrayList<HashMap<String, String>> gamelist = new ArrayList<HashMap<String,String>>();
 	private File dir;
 	private String[] folders; //get list of folders in the main directory
 	private SimpleArrayAdapter listAdapter;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_choose_game);//Init
         
         dir = getApplicationContext().getDir("games", Context.MODE_PRIVATE);
         folders = dir.list();
         
         XMLParser parser = new XMLParser();
         
         if (folders.length != 0) {
         	Log.v("test", folders[0]);
         	for (int g = 0; g < folders.length; g++) {
 	        	File xml = new File(dir.getAbsolutePath() + File.separator + folders[g] + File.separator + "source.xml");
 	        	String file = parser.getXmlFromFile(xml);
 	        	Log.v("XML String",file);
 	        	Document doc = parser.getDomElement(file);
 	        	HashMap<String, String> map = new HashMap<String,String>();
 	        	Element e = (Element) doc.getElementsByTagName("GAME").item(0);
 	        	map.put("KEY_GAMEID", parser.getValue(e, "GAMEID"));
 	        	map.put("KEY_NAME", parser.getValue(e, "NAME"));
 	        	map.put("KEY_IMG", parser.getValue(e, "IMAGE"));
 	        	map.put("KEY_DESC", parser.getValue(e, "DESCRIBTION"));
         	
 	        	gamelist.add(map);
         	}
         }
         	
         listAdapter = new SimpleArrayAdapter(this, gamelist); //create List entries
         setListAdapter(listAdapter); //push entries into listview        
 	}
 	
 	public void onListItemClick(ListView l, View v, int position, long id)
 	{
 			super.onListItemClick(l, v, position, id);
 			
 			String name = gamelist.get(position).get("KEY_GAMEID"); //get clicked game id
 			System.out.println(name);
 
 			Intent i = new Intent(getApplicationContext(), MainActivity.class);
 			i.putExtra("ID", name);
 			i.putExtra("sender", "chooseGame");
 			i.putExtra("loading", false);
 			startActivity(i); //start main activity with game id and sender
 	}
 	
 	public void deleteGame(View v) {
 		int position = getListView().getPositionForView((LinearLayout)v.getParent());
 		File folder = new File(dir + File.separator + folders[position]);
 		if (folder.isDirectory()) {
 	        String[] children = folder.list();
 	        for (int i = 0; i < children.length; i++) {
 	            new File(folder, children[i]).delete();
 	        }
 	        folder.delete();
 	    }
 		Toast.makeText(this, gamelist.get(position).get("KEY_NAME") + " gelscht!", Toast.LENGTH_LONG).show();
 		listAdapter.notifyDataSetChanged();
 	}
 	
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_choose_game, menu);
         return true;
     }
 
 }
