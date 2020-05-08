 package pl.edu.agh.mobile.adhoccom;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.PBEParameterSpec;
 
 import pl.edu.agh.mobile.adhoccom.flooder.AdHocFlooder;
 import pl.edu.agh.mobile.adhoccom.flooder.BroadcastAdHocFlooder;
 import pl.edu.agh.mobile.adhoccom.flooder.MessageListener;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 
 
 public class ChatService extends Service implements MessageListener {
 
 	public static final String MESSAGE_RECEIVED = "MESSAGE_RECEIVED";
 	public static final String NEW_MSG_ID_EXTRA = "pl.edu.agh.mobile.adhoccom.newMsgId";
 	public static final String NEW_MSG_GROUP_EXTRA = "pl.edu.agh.mobile.adhoccom.newMsgGroup";
 	public static final String DEFAULT_GROUP = "default";
 	private static final String LOGGER_TAG = "ChatService";
 	private static final String SECRET = "p28etluthlu0Luh";
 	private final IBinder mBinder = new ChatServiceBinder();
 	private ListeningThread mListeningThread;
 	private ChatDbAdapter mDbAdapter;
 	private AdHocFlooder adHocFlooder;
 	private Map<String, String> chatGroups = new HashMap<String, String>();
 	private MessageDigest messageDigest;
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	private void startService() {
 		mListeningThread = new ListeningThread();
 		mListeningThread.start();
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mDbAdapter = (new ChatDbAdapter(this)).open();
 		adHocFlooder = new BroadcastAdHocFlooder(8888, 10, this);
 		try {
 			messageDigest = MessageDigest.getInstance("SHA-1");
 		} catch (NoSuchAlgorithmException e) {
 			Log.e(LOGGER_TAG, "SHA-1 diggest not supported: " + e.getMessage());
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mDbAdapter.close();
 		adHocFlooder.stop();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (startId == 1) { // start listening threads only once
 			startService();
 		}
 		return Service.START_STICKY;
 	}
 
 	public void sendMessage(Message msg) throws IOException {
 		announceMessage(msg);
 		if (!msg.getGroupName().equals(DEFAULT_GROUP)) {
 			msg.setBody(encryptMessage(msg.getBody(), msg.getGroupName()));
 			msg.setGroupChalenge(getGroupChalenge(msg.getGroupName()));
 		}
 		adHocFlooder.send(msg.toByteArray());
 	}
 
 	private void announceMessage(Message msg) {
 		long newMsgId = mDbAdapter.addNewMessage(msg);
 		Intent intent = new Intent(MESSAGE_RECEIVED);
 		intent.putExtra("newMsgId", newMsgId);
 		intent.putExtra(NEW_MSG_ID_EXTRA, newMsgId);
 		intent.putExtra(NEW_MSG_GROUP_EXTRA, msg.getGroupName());
 		sendBroadcast(intent);
 	}
 
 	public List<String> getChatGroups() {
 		return new ArrayList<String>(chatGroups.keySet());
 	}
 	
 	/**
 	 * Join given group. Starts receiving massages for given group
 	 * @param groupName
 	 * @param groupCode
 	 */
 	public void joinGroup(String groupName, String groupCode) {
 		if (!chatGroups.containsKey(groupName)) {
 			chatGroups.put(groupName, groupCode);
 		}
 	}
 
 	/**
 	 * Leave given group. Stops receiving messages for given group
 	 * @param groupName
 	 */
 	public void leaveGroup(String groupName) throws ChatServiceException {
 		if (ChatService.DEFAULT_GROUP.equals(groupName)) {
 			throw new ChatServiceException(getString(R.string.leave_default_group_error));
 		}
 		chatGroups.remove(groupName);
 	}
 	
 	public boolean isAttachedToGroup(String groupName) {
 		return chatGroups.containsKey(groupName);
 	}
 
 	public class ChatServiceBinder extends Binder {
 		public ChatService getService() {
 			return ChatService.this;
 		}
 	}
 
 	private class ListeningThread extends Thread {
 		@Override
 		public void run() {
 			ChatService.this.adHocFlooder.start();
 		}
 	}
 
 	@Override
 	public void onMessageReceive(DatagramPacket packet) {
 		try {
 			Message msg = Message.parseFrom(packet);
 			if (!msg.getGroupName().equals(DEFAULT_GROUP)) {
 				if (msg.getGroupChalenge().equals(getGroupChalenge(msg.getGroupName()))) {
 					msg.setBody(decryptMessage(msg.getBody(), msg.getGroupName()));
					announceMessage(msg);
 				}
			} else {
				announceMessage(msg);
 			}
 		} catch (InvalidProtocolBufferException e) {
 			Log.i(LOGGER_TAG, "Exception while parsing message: " + e.getMessage());
 		}
 
 	}
 	
 	private String encryptMessage(String body, String groupName) {
 		String encryptedMessage = null;
 		try {
 			Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, chatGroups.get(groupName));
 			encryptedMessage = new String(cipher.doFinal(body.getBytes()));
 		} catch(Exception e) {
 			Log.e(LOGGER_TAG, "Encryption error: " + e.getMessage());
 		}
 		return encryptedMessage;
 	}
 	
 	private String decryptMessage(String body, String groupName) {
 		String decryptedMessage = null;
 		try {
 			Cipher cipher = getCipher(Cipher.DECRYPT_MODE, chatGroups.get(groupName));
 			decryptedMessage = new String(cipher.doFinal(body.getBytes()));
 		} catch(Exception e) {
 			Log.e(LOGGER_TAG, "Decryption error: " + e.getMessage()); 
 		}
 		return decryptedMessage;
 	}
 	
 	private Cipher getCipher(int mode, String pass) {
 		Cipher cipher = null;
 		try {
 			PBEKeySpec keySpec = new PBEKeySpec(pass.toCharArray());
 			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
 			SecretKey key = keyFactory.generateSecret(keySpec);
 			byte[] salt = { 0x7d, 0x60, 0x43, 0x5f, 0x02, (byte) 0xe9, (byte) 0xe0, (byte) 0xae };
 			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 30);
 			cipher = Cipher.getInstance("PBEWithMD5AndDES");
 			cipher.init(mode, key, paramSpec);
 		} catch(Exception e) {
 			Log.e(LOGGER_TAG, "Cipher error: " + e.getMessage());
 		}
 		
     	return cipher;
 	}
 
 	private String getGroupChalenge(String groupName) {
 		messageDigest.update(SECRET.getBytes());
 		messageDigest.update(groupName.getBytes());
 		messageDigest.update(chatGroups.get(groupName).getBytes());
 		messageDigest.update(SECRET.getBytes());
 		return new String(messageDigest.digest());
 	}
 }
