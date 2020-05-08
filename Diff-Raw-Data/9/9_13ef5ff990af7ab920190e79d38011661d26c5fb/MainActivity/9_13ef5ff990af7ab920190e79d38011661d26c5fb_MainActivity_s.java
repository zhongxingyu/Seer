 package de.ohmhochschule.bme.activities;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.ohmhochschule.bme.R;
 import de.ohmhochschule.bme.datatypes.MycelMessage;

 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AbsListView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener {
 	
 	public static int mode; // 0:internet, 1:shadownet
 	public final static String EXTRA_SENDING_MESSAGE = "de.ohmhochschule.bme.SENDING_MESSAGE";
 	public final static String EXTRA_SENDING_TYPE = "de.ohmhochschule.bme.SENDING_TYPE";
 
 	private UdpServerThreadActivity udpServer;
 
 	private Button sendButton;
 	private EditText statusContentEditText;
 
 	private List<Map<String, String>> statusHistoryData;
 	private SimpleAdapter statusHistoryAdapter;
 
 	public ListView statusHistoryView;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		mode = 1;
 
 		// connect Views
 		statusHistoryView = (ListView) this.findViewById(R.id.chatHistory);
 
 		statusHistoryView
 				.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
 
 		statusHistoryData = new ArrayList<Map<String, String>>();
 		statusHistoryAdapter = new SimpleAdapter(this, statusHistoryData,
 				android.R.layout.simple_list_item_2, new String[] { "message",
 						"sender" }, new int[] { android.R.id.text1,
 						android.R.id.text2 });
 		statusHistoryView.setAdapter(statusHistoryAdapter);
 		
 		statusContentEditText = (EditText)this.findViewById(R.id.message);
 
 		// add new view and connect it to button via event handler:
 		sendButton = (Button) this.findViewById(R.id.btn_send);
 		sendButton.setOnClickListener(this);
 		udpServer = new UdpServerThreadActivity(this);
 		udpServer.start();
 		
 		
 //		chatHistoryData = new ArrayList<Map<String, String>>();
 //		chatHistoryAdapter = new SimpleAdapter(this, chatHistoryData,
 //				android.R.layout.simple_list_item_2, new String[] { "message",
 //						"sender" }, new int[] { android.R.id.text1,
 //						android.R.id.text2 });
 //		chatHistoryView.setAdapter(chatHistoryAdapter);
 //
 //		// add new view and connect it to button via event handler:
 //		sendButton = (Button) this.findViewById(R.id.btn_send);
 //		sendButton.setOnClickListener(this);
 //		//udpServer = new UdpServerThreadActivity(this);
 //		udpServer.start();
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu( menu );
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate( R.menu.main_menu, menu );
		inflater.inflate( R.menu.main_menu, menu );
 		return true;
 	}
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch ( item.getItemId() ) {
 		case R.id.Conversations:
 			startActivity( new Intent( this, ConversationListActivity.class ) );
 			return true;
 		case R.id.Wall:
 			return true;
 		}
 		return false;
 	}
 
 
 
 	
 	
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btn_send:
 			new AlertDialog.Builder(this).setTitle(R.string.lbl_network).setItems(R.array.arr_network, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 					case 0:
 						Intent intent = new Intent(getApplicationContext(), UdpMessageDistributorActivity.class);
 						intent.putExtra(EXTRA_SENDING_MESSAGE, statusContentEditText.getText().toString());
 						//intent.putExtra(EXTRA_SENDING_TYPE, 1+"");
 						intent.putExtra(EXTRA_SENDING_TYPE, 1);
 						startActivity(intent);
 						break;
 					case 1:
 						Toast.makeText(getApplicationContext(), "facebook not implemented", Toast.LENGTH_SHORT).show();
 						break;
 					case 2:
 						Toast.makeText(getApplicationContext(), "twitter not implemented", Toast.LENGTH_SHORT).show();
 						break;
 					default:
 						break;
 					}
 					
 				}
 
 	
 			}).show();
 			break;
 		default:
 			Log.e(getLocalClassName(), "Id not found!");
 		}
 	}
 
 	public void displayMessage(MycelMessage msg) {
 		// if called by more than one thread use synchronized
 		Map<String, String> messageAndSender = new HashMap<String, String>(2);
 		messageAndSender.put("message", msg.getMessage());
 		messageAndSender.put("sender", msg.getSender());
 		statusHistoryData.add(messageAndSender);
 		statusHistoryAdapter.notifyDataSetChanged();
 	}
 }
