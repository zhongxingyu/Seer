 package gov.cdc;
 
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import java.util.ArrayList;
 
 public class FootnotesView extends ListActivity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		ArrayList<String> footnotes = MainView.controller.currentNode.getFootnotes();
 		String [] footnotes_array = footnotes.toArray(new String[footnotes.size()]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, footnotes_array);
 		setListAdapter(adapter);
 		// setContentView(R.layout.activity_footnotes_view);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_footnotes_view, menu);
 		return true;
 	}
 
 }
