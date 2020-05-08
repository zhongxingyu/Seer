 package shu.journal;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class ListPagesActivity extends ListActivity implements OnClickListener{
 	StringBuilder sb = new StringBuilder("");
 	DBAdapter dbAdapter = new DBAdapter(this);
 	Long user_id;
 	TextView username;
 	static SimpleCursorAdapter pages;
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.list_pages);
         dbAdapter.open();
         
         Bundle extras = getIntent().getExtras();
         user_id = extras.getLong(dbAdapter.KEY_USERID);
         
         username = (TextView)findViewById(R.id.show_username);
 		
         fillData();
         //getListView returns the current instance of local list view
         registerForContextMenu(getListView());       
     }  
 	public void onResume(){
 		super.onResume();
 		pages.notifyDataSetChanged();
 	}
   	
   	//Will show selected item from the listview
   	protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         Intent i = new Intent(this, JournalEntryUpdateActivity.class);
         i.putExtra(dbAdapter.KEY_USERID, user_id);
 	    JournalEntryUpdateActivity.cursor = dbAdapter.getPageById(id);
 	    startActivity(i);
     }
   	 //Fills the listview with data from the DB
 	  @SuppressWarnings("deprecation")
 	private void fillData() {
 		Cursor c = dbAdapter.getAllUserPages(user_id);
 		startManagingCursor(c);
 		
 
      @SuppressWarnings("static-access")
 		String[] from = new String[] {dbAdapter.KEY_DATE};
      	int[] to = new int[] { R.id.show_date};
       
       //Now create an array adapter and set it to display using our row
      	pages =	new SimpleCursorAdapter(this, R.layout.list_row, c, from, to,0);
      	setListAdapter(pages);
 	  }
 	  
 	//Options menu
 	@Override
   public boolean onCreateOptionsMenu(Menu menu){
   	super.onCreateOptionsMenu(menu);
   	MenuInflater inflater = getMenuInflater();
   	inflater.inflate(R.menu.activity_pages, menu);
   	return true;
   }
 	
 	//Actions for individual menu items
   @Override
   public boolean onOptionsItemSelected(MenuItem item){
   	switch(item.getItemId()){
   	case R.id.add_page:
   		Intent i = new Intent(getApplicationContext(), JournalEntryActivity.class);
 		i.putExtra(dbAdapter.KEY_USERID, user_id);
 		startActivity(i);
   		return true;
   	}
   	return false;
   }
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 	}
 }
