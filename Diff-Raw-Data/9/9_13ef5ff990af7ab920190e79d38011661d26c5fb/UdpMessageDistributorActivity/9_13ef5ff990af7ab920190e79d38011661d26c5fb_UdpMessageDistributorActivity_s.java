 package de.ohmhochschule.bme.activities;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import de.ohmhochschule.bme.R;
 import de.ohmhochschule.bme.datatypes.MycelMessage;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.widget.TextView;
 
 public class UdpMessageDistributorActivity extends Activity {
 
 	private ArrayList<MycelMessage> message;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_udp_message_distributor);
 
 		TextView text = (TextView) this.findViewById(R.id.textView1);
 		text.setText(getIntent().getStringExtra(
 				MainActivity.EXTRA_SENDING_MESSAGE)
 				+ getIntent().getIntExtra(MainActivity.EXTRA_SENDING_TYPE, 99));
 
 		MycelMessage mm = new MycelMessage("testsender", "testmessage",
 				"testrecipient", 1);
 
 		// TODO: change for multiple messages
 		message = new ArrayList<MycelMessage>();
 		message.add(mm);
 		
 		sendUdpMessage();
 	}
 
 	private void sendUdpMessage() {
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ObjectOutputStream out;
 		try {
 			out = new ObjectOutputStream(baos);
 			out.writeObject(message);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		byte[] bytes = baos.toByteArray();
 		
 		
 		// dev only
 		ObjectInputStream in = null;
 		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
 		try {
 			in = new ObjectInputStream(bais);
 			ArrayList<MycelMessage> al = (ArrayList<MycelMessage>)in.readObject();
 			Log.v("test", al.toString());
 		} catch (StreamCorruptedException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		// MycelMessage message = new MycelMessage(userName, messageContent);
 		DatagramSocket ds = null;
 
 		try {
 
 			// broadcast
 			ds = new DatagramSocket();
 			InetAddress serverAddr = InetAddress.getByName("255.255.255.255");
 			DatagramPacket dp;
 
 			dp = new DatagramPacket(bytes,
 					bytes.length, serverAddr, 11111);
 			ds.setBroadcast(true);
 			ds.send(dp);
 
 		} catch (SocketException e) {
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (ds != null) {
 				ds.close();
 			}
 		}
 	}
 
 }
