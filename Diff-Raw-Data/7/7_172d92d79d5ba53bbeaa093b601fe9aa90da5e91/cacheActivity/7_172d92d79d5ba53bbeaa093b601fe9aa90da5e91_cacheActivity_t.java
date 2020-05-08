 package dbit.belegii;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class cacheActivity extends Activity {
     private Buffer buffer = new LRU(3);
     
     //content
 	private EditText query;
     private TextView output;
     private TextView time_output;
     private TextView time_description;
     private TextView time_output_recent;
     
     //preferences
     SharedPreferences preferences;
 	
 	/** Called when the activity is first created. */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         //fill content objects
     	query = (EditText)findViewById(R.id.edit);
         output = (TextView) findViewById(R.id.output);
         time_output = (TextView) findViewById(R.id.time_output_0);
         time_description = (TextView) findViewById(R.id.time_0);
         time_output_recent = (TextView) findViewById(R.id.time_output_1);
         //done
         
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
         //generates a new buffer
         /*
         switch(Integer.parseInt(preferences.getString("bufferType", "0"))){
 	        case 0:{
 	        	buffer = new FiFo(Integer.parseInt(preferences.getString("buffersize", "3")));
 	        	break;
 	        }
 	        case 1:{
 	        	buffer = new LRU(Integer.parseInt(preferences.getString("buffersize", "3")));
 	        	break;
 	        }
 	        case 2:{
 	        	buffer = new FiFo(Integer.parseInt(preferences.getString("buffersize", "3")));
 	        	break;
 	        }
         }
         */
         if(savedInstanceState != null && !savedInstanceState.isEmpty()){
         	buffer = savedInstanceState.getParcelable("buffer");
         }
         Log.d(this.getClass().getSimpleName(), "onCreate");
     }
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(keyCode == KeyEvent.KEYCODE_MENU){
 			startActivity(new Intent(this, Preferences.class));
 		}else if(keyCode == KeyEvent.KEYCODE_BACK){
 			finish();
 		}
 	    return(true);
 	}
     
     public void startQuery(View view) {
 //    	  final EditText query = (EditText)findViewById(R.id.edit);
 //        final TextView output = (TextView) findViewById(R.id.output);
 //        final TextView time_output = (TextView) findViewById(R.id.time_output_0);
 //        final TextView time_description = (TextView) findViewById(R.id.time_0);
 //        final TextView time_output_recent = (TextView) findViewById(R.id.time_output_1);
 
     	Toast.makeText(cacheActivity.this, query.getText(), Toast.LENGTH_SHORT).show();
     
     	output.setText("");
     	
     	String result = "";
     	long elapsedtime;
     	
     	
     	try {
     		Log.i("postgres","Go Postgres!");
 			Class.forName("org.postgresql.Driver");
 			String url = "jdbc:postgresql://anna05.medien.uni-weimar.de/felix?user=felix&password=felix1621";
 			Connection conn = DriverManager.getConnection(url);
 			//asking the buffer
 			Query q;
 			elapsedtime = SystemClock.elapsedRealtime();
			String statement = (query.getText().toString()+";");
			if((q = (buffer.get(statement))) != null){
 				output.append(q.result);
 				time_description.setText("Zeit (cache): ");
 				time_output.setText(""+(SystemClock.elapsedRealtime() - elapsedtime)+" ms");
 				time_output_recent.setText(""+q.getDuration()+" ms");
 			}else{//no matching item in buffer			
 				elapsedtime = SystemClock.elapsedRealtime();
				Statement st = conn.createStatement();				
 				ResultSet rs = st.executeQuery(statement);
 				ResultSetMetaData rsmd = rs.getMetaData();
 				
 				//check, if the statement changes DB entries
 //				if((statement.toLowerCase()).contains("insert") || statement.toLowerCase().contains("update")){ //if it changes something, the buffer will be cleared
 //					//TODO: intelligenteres Vorgehen! vlt. nur die betroffenen Zeilen aus dem Buffer löschen o.ä. 
 //					//TODO: wenn etwas an der DB verändert wird, dann puffer anpassen und diesen auftrag nicht im puffer speichern
 //					buffer.clear();
 //				}
 				
 			    int numCols = rsmd.getColumnCount();
 				
 				while (rs.next()) {
 					Log.i("postgres","getting one column");
 					String row = "";
 				    for (int i = 1; i <= numCols; i++) {
 				    	 row = row + "| " + rs.getString(i);
 					}
 				    row = row + " |";
 				    Log.i("postgres",row);
 				    output.append(row + "\n");
 				    result += row;
 				    result += "\n";
 				}
 				rs.close();
 				st.close();
 				//output the elapsed time for asking postgres
 				time_output.setText(""+(elapsedtime = (SystemClock.elapsedRealtime() - elapsedtime))+" ms");
 				time_description.setText("Zeit (WAN): ");
 				
 				//check, if the statement changes DB entries
 				updateChecker uc = new updateChecker();
 				String tables;
 				if((tables = uc.checkUpdate(statement)) != null){
 					buffer.cleanBuffer(tables);
 				}else{
 					//add the query and its result to the buffer
 					Query buffelem = new Query(statement, result, elapsedtime, SystemClock.elapsedRealtime());
 					buffer.add(buffelem);
 					time_output_recent.setText("-");
 				}				
 			}			
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
     }
     
     @Override
     public void onResume(){
     	super.onResume();
     	
     	//generates a new buffer if it is necessary
 //    	SharedPreferences tmp_pref = PreferenceManager.getDefaultSharedPreferences(this);
     	if(buffer == null ||
     			!(buffer.bufferTypeID == Integer.parseInt((preferences.getString("bufferType", "-1")))) ||
     			!(buffer.size == Integer.parseInt((preferences.getString("buffersize", "-1"))))){
     		Log.i(this.getClass().getSimpleName(), "new buffer type: "+preferences.getString("bufferType", "0"));
     		Log.i(this.getClass().getSimpleName(), "new buffer size: "+preferences.getString("buffersize", "-1"));
           switch(Integer.parseInt(preferences.getString("bufferType", "0"))){
 	        case 0:{
 	        	buffer = new FiFo(Integer.parseInt(preferences.getString("buffersize", "3")));
 	        	Log.i(this.getClass().getSimpleName(), "new buffer type: FIFO");
 	        	break;
 	        }
 	        case 1:{
 	        	buffer = new LRU(Integer.parseInt(preferences.getString("buffersize", "3")));
 	        	Log.i(this.getClass().getSimpleName(), "new buffer type: LRU");
 	        	break;
 	        }
 	        case 2:{
 	        	buffer = new LFU(Integer.parseInt(preferences.getString("buffersize", "3")));
 	        	Log.i(this.getClass().getSimpleName(), "new buffer type: LFU");
 	        	break;
 	        }
         }
     	}
 
         Log.d(this.getClass().getSimpleName(), "onResume");
     }
     
     @Override
     public void onStart(){
     	super.onStart();
     	Log.d(this.getClass().getSimpleName(), "onStart");
     }
     
     @Override
     public void onPause(){
     	super.onPause();
     	Bundle b = new Bundle();
     	b.putParcelable("buffer", buffer);
     	onSaveInstanceState(b);
     	Log.d(this.getClass().getSimpleName(), "onPause");
     }
     
 //    @Override
 //    public void onSaveInstanceState(Bundle savedInstanceState){
 //            super.onSaveInstanceState(savedInstanceState);
 //            savedInstanceState.putParcelable("buffer", buffer);
 //            Log.d(this.getClass().getSimpleName(), "onSave....");
 //            
 //    }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
              super.onRestoreInstanceState(savedInstanceState);
              Log.d(this.getClass().getSimpleName(), "onRestore....");
              if(savedInstanceState != null && !savedInstanceState.isEmpty()){
              	buffer = savedInstanceState.getParcelable("buffer");
              }
     }
 }
