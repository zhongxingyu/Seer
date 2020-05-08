 package net.fobel.android.remoteduino;
 
 import android.app.Activity;
 
 import android.os.Bundle;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.widget.Toast;
import android.widget.TextView;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import java.io.IOException;
 
 
 public class RemoteDuinoActivity extends Activity {
 	RemoteCommandManager cmd_manager;
 	String label;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         try {
         	cmd_manager = new RemoteCommandManager(getApplicationContext());
     	} catch (IOException e) {
 			Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
 		} catch(Exception e) {
 			Toast.makeText(this, "[onCreate] Unexpected exception: " + e, Toast.LENGTH_SHORT).show();
 		}
         setContentView(R.layout.main);
 		update_codes_list();
     }
     
     
     void update_codes_list() {
     	LinearLayout ll = (LinearLayout)findViewById(R.id.llo__remote_buttons);
     	ll.removeAllViews();
     	final Context c = this.getApplicationContext();
     	for(RemoteCommand cmd : cmd_manager.get_commands()) {
    		cmd.code = "0x" + cmd.code;
 	    	final RemoteButton temp = new RemoteButton(c, cmd);
     		ll.addView(temp);
     	}
     }
     
     
     public void set_label(String label) {
     	this.label = label;
     }
     
     
     public void on__learn_code__handler(View view) {
 		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
 		final RemoteDuinoActivity here = this;
 		alert.setView(input);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				here.set_label(input.getText().toString().trim());
 		    	RemoteCommand cmd;
 				try {
 					cmd = RemoteCommand.learn_command(label);
 					cmd_manager.add(cmd);
 					Toast.makeText(here, "Added code to local list: " + cmd.code + ", " + cmd.protocol, Toast.LENGTH_SHORT).show();
 					here.update_codes_list();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						dialog.cancel();
 					}
 				});
 		alert.show();
     }
    
    
    public void on__on_off__handler(View view) {
        RemoteCommand cmd = new RemoteCommand("TV ON/OFF", "1", "0xEE1101FE");
        String result = cmd.send();
		//Toast.makeText(this, "Result: " + result, Toast.LENGTH_SHORT).show();
    }
 }
