 package kits.vdroid;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class del_servers extends Activity {
     private ListView lv;
 	private VDRDBHelper db;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.delservers);
         lv = (ListView) findViewById(R.id.del_list);
         
         //Check if we have a stored VDR-Connection
       	db = new VDRDBHelper(del_servers.this);
 		Cursor servers = db.getServersCursor();
     	
 		startManagingCursor(servers);
 		
 		//Serverliste anlegen
         lv.setTextFilterEnabled(true);
 		lv.setOnItemClickListener(new OnItemClickListener()
 		{
 		    public void onItemClick(AdapterView<?> parent, android.view.View view,int position, long id)
 		    {
 		      // When clicked, show a toast with the TextView text
 		      deleteServer(id);
 		    }
 		});
 		
 		String[] from = new String[] { "name", "host" };
 		int[] to = new int[] { R.id.tv_name, R.id.tv_host };
 		
 		//Adpater
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.delserver_li, servers,from, to);
 		
 		lv.setAdapter(adapter);
 	}
 	
 	private void deleteServer(long id)
 	{
 		//Name der Verbindung erfragen
 		String name = db.getServernameById(id);
 		final long server_id = id;
 		
 		//User fragen
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setMessage("Verbindung \""+ name + "\" löschen?")
     	       .setCancelable(false)
     	       .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	                db.deleteServerById(server_id);
     	                finish();
       	           }
     	       })
     	       .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	                dialog.cancel();
     	           }
     	       });
     	AlertDialog alert = builder.create();
     	alert.show();
 	}
 	
 }
