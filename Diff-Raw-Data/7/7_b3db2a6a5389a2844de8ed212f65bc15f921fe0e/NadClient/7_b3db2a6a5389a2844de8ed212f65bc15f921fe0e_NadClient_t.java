 /* Copyright (C) 2010 Jon Anders Skorpen
  * 
  * This file is part of aNADroid.
  *
  * aNADroid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * aNADroid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  
  * You should have received a copy of the GNU General Public License
  * along with aNADroid.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.mindmutation.anadroid;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.res.Configuration;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.AdapterView;
 import android.widget.AdapterView.*;
 import android.widget.ArrayAdapter;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.net.InetAddress;
 
 import java.io.OutputStream;
 import java.io.IOException;
 
 
 public class NadClient extends Activity
 {
     private Button volumeUp;
     private Button volumeDown;
     private Button sourceUp;
     private Button sourceDown;
 
     private Spinner sourceSpinner;
 
     private TextView error;
 
     private Socket socket;
     private InetAddress addr;
     private OutputStream output;
     private byte[] command;
 
     private boolean realSelection;
     private int lastSelection;
 
 
     private void sendCommand(String command)
     {
 	try {
 	    // Fix user defined host
	    String host = getResources().getText(R.string.host).toString();
	    addr = InetAddress.getByName(host);
 	    socket = new Socket(addr, 1234);
 	} catch (UnknownHostException e) {
 	    error.setText("Unknown host: " + e.getMessage());
 	    return;
 	} catch (IOException e) {
 	    error.setText("IO: " + e.getMessage());
 	    return;
 	} catch (SecurityException e) {
 	    error.setText("Security: " + e.getMessage());
 	    return;
 	}
 		
 	try {
 	    output = socket.getOutputStream();
 	    output.write(command.getBytes());
 	    socket.close();
 	} catch(IOException e) {
 	    error.setText("Could not write command: " + e.getMessage());
 	    return;
 	}
 	
     }
 
 
     public void clickHandler(View v)
     {
	/* Would be nice to get these strings
	 * from res/values
	 */
 	switch(v.getId()) {
 	case R.id.volUp:
 	    sendCommand("\rMain.Volume+\r\0");
 	    break;
 	case R.id.volDown:
 	    sendCommand("\rMain.Volume-\r\0");
 	    break;
 	case R.id.srcUp:
 	    sendCommand("\rMain.Source+\r\0");
 	    break;
 	case R.id.srcDown:
 	    sendCommand("\rMain.Source-\r\0");
 	    break;
 	}
     }
 
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
 	realSelection = false;
 
         setContentView(R.layout.main);
 
 	error = (TextView)this.findViewById(R.id.error);
 
 	sourceSpinner = (Spinner)this.findViewById(R.id.sourceSpinner);
 	ArrayAdapter<CharSequence> adapter =
 	    ArrayAdapter.createFromResource(
 					    this, R.array.srcList,
 					    android.R.layout.simple_spinner_item);
 	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 	sourceSpinner.setAdapter(adapter);
 	sourceSpinner.setOnItemSelectedListener(new OnSourceSelectedListener());
     }
 
 
     // @Override
     // public void onRestart()
     // {
     // 	Toast.makeText(this, "onRestart",
     // 		       Toast.LENGTH_LONG).show();
 
     // 	super.onRestart();
     // }
 
 
     // @Override
     // public void onStart()
     // {
     // 	super.onStart();
     // }
 
 
     // @Override
     // public void onResume()
     // {
     // 	super.onResume();
     // 	realSelection = false;
     // }
 
 
     public class OnSourceSelectedListener implements OnItemSelectedListener
     {
 	public void onItemSelected(AdapterView<?> parent, View view,
 				   int pos, long id)
 	{
 	    String source;
 
 	    /* Hackity hack hack since the spinner for some reason
 	     * does not support onItemClick, only onItemSelected
 	     * But this works great. Probably... :p
 	     */
 	    if (!realSelection) {
 		realSelection = true;
 		return;
 	    }
 	    lastSelection = pos;
 	    realSelection = true;
 
 	    source = parent.getItemAtPosition(pos).toString();
 
 	    Toast.makeText(parent.getContext(), "Source: " +
 		source, Toast.LENGTH_LONG).show();
 
 	    sendCommand("\rMain.Source=" +
 			source + "\r\0");
 
 	}
 
 	public void onNothingSelected(AdapterView parent)
 	{
 	}
     }
 }
