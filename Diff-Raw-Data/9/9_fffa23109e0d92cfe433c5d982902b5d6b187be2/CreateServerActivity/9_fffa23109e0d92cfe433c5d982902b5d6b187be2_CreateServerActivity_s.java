 package org.touchirc.activity;
 
 import java.util.ArrayList;
 
 import org.touchirc.R;
 import org.touchirc.TouchIrc;
 import org.touchirc.model.Server;
 import org.touchirc.view.MultipleChannelTextView;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class CreateServerActivity extends SherlockActivity {
 
 	private TextView server_Name_TV;
 	private EditText serverName_ET;
 
 	private TextView server_Hostname_TV;
 	private EditText serverHostname_ET;
 
 	private TextView server_port_TV;
 	private EditText serverPort_ET;
 
 	private TextView server_password_TV;
 	private EditText serverPassword_ET;
 
 	private CheckBox useSSL_CB;
 	private String [] charsetArray;
 	private String selectedCharset = "";
 
 	private Button charset_BT;
 
 	private MultipleChannelTextView mu;
 
 	private Server serv;
 	private Bundle bundleEdit = null;
 	private Bundle bundleAddFromMenu = null;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Allows to the actionBar's icon to do "Previous"
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setTitle("Create a Server");
 
 		// Prevents the keyboard to be displayed when you come on this activity
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 
 		setContentView(R.layout.create_server_layout);
 
 		Intent i = getIntent();
 
 		// Collect the Bundle object if the activity is started by ExistingServersActivity : Add or Edit
 		bundleEdit = i.getBundleExtra("ServerIdentification");
 		bundleAddFromMenu = i.getBundleExtra("AddingFromExistingServersActivity");
 
 		// TextViews : black ones (default color)s are compulsories, gray ones are optionals
 
 		this.server_Name_TV = (TextView) findViewById(R.id.textView_server_name);	
 
 		this.server_Hostname_TV = (TextView) findViewById(R.id.textView_hostname);
 
 		this.server_port_TV = (TextView) findViewById(R.id.textView_server_port);
 
 		this.server_password_TV = (TextView) findViewById(R.id.textView_server_password);
 		this.server_password_TV.setTextColor(Color.GRAY);
 
 		// EditTexts		
 
 		this.serverName_ET = (EditText) findViewById(R.id.editText_server_name);
 		this.serverHostname_ET = (EditText) findViewById(R.id.editText_hostname);
 		this.serverPort_ET = (EditText) findViewById(R.id.editText_server_port);
 		this.serverPassword_ET = (EditText) findViewById(R.id.editText_server_password);
 
 		// Checkbox : using SSL
 
 		this.useSSL_CB = (CheckBox) findViewById(R.id.checkBox_SSL_use);
 
 		// Button : charset
 
 		this.charsetArray = getResources().getStringArray(R.array.charset_array_name);
 
 		this.charset_BT = (Button) findViewById(R.id.button_select_charset);
 		this.charset_BT.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// Instantiate an AlertDialog.Builder with its constructor
 				AlertDialog.Builder builder = new AlertDialog.Builder(CreateServerActivity.this);
 
 				// Chain together various setter methods to set the dialog characteristics
 				builder.setTitle(R.string.charsetTitle);
 				// Integrate the charsetArray inside the Pop-up
 				builder.setItems(charsetArray, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						charset_BT.setText(charsetArray[which]);
 						selectedCharset = charsetArray[which];
 						dialog.dismiss();
 					}
 				});
 
 				// Get the AlertDialog from create()
 				AlertDialog dialog = builder.create();
 				dialog.show();
 
 			}
 		});
 
 		this.mu = (MultipleChannelTextView) findViewById(R.id.editText_associated_channels);
 
 		// if started by ExistingServersActivity, changing the EditTexts'value
 		if(bundleEdit != null && bundleEdit.containsKey("ServerId")){
 			// We collect the server from available servers list
 			Server serverToEdit = TouchIrc.getInstance().getAvailableServers().get(bundleEdit.getInt("ServerId"));
 
 			// And put values in corresponding editText
 			this.serverName_ET.setText(serverToEdit.getName());
 			this.serverHostname_ET.setText(serverToEdit.getHost());
 			this.serverPort_ET.setText(String.valueOf(serverToEdit.getPort()));
 			this.serverPassword_ET.setText(serverToEdit.getPassword());
 
 			if(serverToEdit.useSSL()){
 				this.useSSL_CB.setChecked(true);
 			}
 			selectedCharset = serverToEdit.getCharset();
 			this.charset_BT.setText(selectedCharset);
 
 			// if there is an auto-connected channels'list
 			if(serverToEdit.getAutoConnectedChannels() != null){
 				for(String s : serverToEdit.getAutoConnectedChannels()){
					this.mu.append(s); // Put the channel in the EditText
 					this.mu.append(" "); // Add a space in the EditText to make the bubble appear
 				}
 			}
 		}
 	}
 
 	/**
 	 * Define the display of the actionBar
 	 * 
 	 * @param menu
 	 * @return true
 	 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.create_object_activity, menu);
 		return true;
 	}
 
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the icon 
 	 * in the action bar: When this button is clicked, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			Intent intent = new Intent(this, MenuActivity.class);
 			// According to the origin of the triggering of the activity
 			if(bundleEdit != null || bundleAddFromMenu != null){
 				intent.setClass(this, ExistingServersActivity.class);
 			}
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 
 		case R.id.save :
 			
 			puttDefaultTextViews();
 
 			if(addServer()){				
 				finish(); // close the activity
 			}
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	private void puttDefaultTextViews(){
 
 		this.server_Name_TV.setText(R.string.serverName);
 		this.server_Hostname_TV.setText(R.string.serverHostname);
 		this.server_port_TV.setText(R.string.serverPort);
 
 	}
 
 	private boolean missingInformation() {
 		
 		boolean b = false;
 
 		// And indicate him of which informations we are lacking of (the color of the corresponding textviews becomes red)
 		if(serverPort_ET.getText().length() == 0){
 
 			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.serverPortNotCompleted));
 			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 			StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 			sb.setSpan(fcs, 14, 27, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			sb.setSpan(bss, 14, 27, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			this.server_port_TV.setText(sb);
 
 			serverPort_ET.requestFocus();
 			 b = true;
 		}
 		if(serverHostname_ET.getText().length() == 0){
 
 			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.serverHostnameNotCompleted));
 			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 			StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 			sb.setSpan(fcs, 18, 31, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			sb.setSpan(bss, 18, 31, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			this.server_Hostname_TV.setText(sb);
 
 			serverHostname_ET.requestFocus();
 			b = true;
 		}
 		if(serverName_ET.getText().length() == 0){
 
 			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.serverNameNotCompleted));
 			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 			StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 			sb.setSpan(fcs, 14, 27, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			sb.setSpan(bss, 14, 27, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			this.server_Name_TV.setText(sb);
 
 			serverName_ET.requestFocus();
 			b = true;
 		}
 		return b;
 	}
 
 	private boolean addServer() {
 
 		// TODO Add Regex on hostname, port, password (?)
 		
 		// Check if all main informations are indicated
 		if(missingInformation()){
 			return false;
 		}
 		
 		/** -------------------------------------------------------- **/
 
 		// the Server is created with the datas given by the user
 		
 		int port = Integer.parseInt(serverPort_ET.getText().toString());
 
 		serv = new Server(serverName_ET.getText().toString(),
 				serverHostname_ET.getText().toString(),
 				port,
 				serverPassword_ET.getText().toString()
 				);
 
 		// Use SSL
 		if(this.useSSL_CB.isChecked()){
 			serv.setUseSSL(true);
 		}
 
 		// Charset chosen
 		if(selectedCharset != ""){
 			serv.setEncoding(selectedCharset);
 		}
 
 		// if a channel at least is inputted
 		if(mu.getText().toString().length() > 0){
 			ArrayList<String> channelList = this.mu.getChannelList();
 			serv.setAutoConnectedChannels(channelList);
 		}
 		
 		/** -------------------------------------------------------- **/
 
 		Intent i = new Intent(CreateServerActivity.this, ExistingServersActivity.class);
 		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		String s, ts = getResources().getString(R.string.theServer);
 
 		if(bundleEdit != null){
 			// Update the Server in the database
 			s = getResources().getString(R.string.hasBeenModified);
 			TouchIrc.getInstance().updateServer(bundleEdit.getInt("ServerId"), serv, getApplicationContext());
 			Toast.makeText(getApplicationContext(), ts + " " + serv.getName() + " " + s, Toast.LENGTH_SHORT).show();
 
 			startActivity(i);
 		}
 		else{
 			// Add the Server just created into the database
 			if(!TouchIrc.getInstance().addServer(serv, this)){
 
 				SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.serverNameAlreadyInUse));
 				ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 				StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 				sb.setSpan(fcs, 14, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				sb.setSpan(bss, 14, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				this.server_Name_TV.setText(sb);
 
 				return false;
 			}
 			else{
 				s = getResources().getString(R.string.hasBeenAdded);
 				Toast.makeText(getApplicationContext(), ts  + " " + serv.getName() + " " + s, Toast.LENGTH_SHORT).show();
 				// We go back to the ExistingServersActivity and transmit the new server
 				if(bundleAddFromMenu != null && bundleAddFromMenu.containsKey("comingFromExistingServersActivity")){
 					startActivity(i);
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the physical button 
 	 * "Back" (on device) : When this button is clicked, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 
 			Intent intent = new Intent(this, MenuActivity.class);
 			// According to the origin of the triggering of the activity
 			if(bundleEdit != null || bundleAddFromMenu != null){
 				intent.setClass(this, ExistingServersActivity.class);
 			}
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 }
