 package ch.ethz.inf.vs.android.g54.a3;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 public class ChatManager {
 	// Singleton
 	private static ChatManager instance;
 	public static ChatManager getInstance() {
 		if (instance == null) {
 			instance = new ChatManager();
 		}
 		return instance;
 	}
 
 	final String SERVER = "vswot.inf.ethz.ch";
 	final int MESSAGE_BUFFER_SIZE = 2048;
 
 	String user = "Llama";
 	DatagramSocket sockCmd = null;
 	Thread chatThread = null;
 	Handler handler = null;
 
 	Map<Integer, Integer> clocks = new HashMap<Integer, Integer>();
 	Map<String, String> clients = new HashMap<String, String>();
 
 	private ChatManager() {
 		try {
 			InetAddress adrServer = InetAddress.getByName(SERVER);
 			sockCmd = new DatagramSocket(4000);
 			sockCmd.setSoTimeout(10 * 1000); // read timeout of 10s
 			sockCmd.connect(adrServer, 4000); // set default socket for send/recv
 
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	class ChatThread implements Runnable {
 		
 		DatagramSocket sock = null;
 		Handler handler;
 		
 		public ChatThread(Handler handler) {
 			this.handler = handler;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				// TODO: CHECK: if we only receive on this port but don't send
 				// we don't need the hostname resolved, nor the sock.connect
 				InetAddress adrServer = InetAddress.getByName(SERVER);
 				sock = new DatagramSocket(4001);
 				sock.connect(adrServer, 4001); // set default socket for send/recv
 
 				while (true) {
 					byte[] msg = new byte[MESSAGE_BUFFER_SIZE];
 					DatagramPacket pkt = new DatagramPacket(msg, msg.length);
 					sockCmd.receive(pkt); // blocking read
 					Message m = handler.obtainMessage();
 					Bundle b = new Bundle();
 					b.putString("msg", String.valueOf(msg));
 					m.setData(b);
 					m.sendToTarget();
 				}
 
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (SocketException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	class MessageHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			try {
 				JSONObject o = new JSONObject(msg.getData().getString("msg"));
 				// TODO: what to do on message ?
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private String cmdReg(String user) {
 		JSONObject o = new JSONObject();
 		try {
 			o.put("cmd", "register");
 			o.put("user", user);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return o.toString();
 	}
 
 	private String cmdDereg() {
 		JSONObject o = new JSONObject();
 		try {
 			o.put("cmd", "deregister");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return o.toString();
 	}
 
 	private String cmdMsg(String text, Map<Integer, Integer> clocks) {
 		JSONObject o = new JSONObject();
 		JSONObject t = new JSONObject(clocks);
 		try {
 			o.put("cmd", "message");
 			o.put("text", text);
 			o.put("time_vector", t);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return o.toString();
 	}
 
 	private String cmdGetClients() {
 		JSONObject o = new JSONObject();
 		try {
 			o.put("cmd", "get_clients");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return o.toString();
 
 	}
 	
 	private JSONObject execCmd(String cmd) {

 		try {
 			byte[] req = cmd.getBytes();
 			DatagramPacket pkt = new DatagramPacket(req, req.length);
 			sockCmd.send(pkt);
 
 			byte[] ans = new byte[MESSAGE_BUFFER_SIZE];
 			pkt = new DatagramPacket(ans, ans.length);
 			sockCmd.receive(pkt); // blocking read
			return new JSONObject(String.valueOf(ans));
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public void sendMsg(String text) {
 		execCmd(cmdMsg(text, clocks));
 	}
 
 	@SuppressWarnings("unchecked")
 	public void connect() {
 		try {
 			// register
 			JSONObject o = execCmd(cmdReg(user));
 			// answer:
 			// {"index":3,"time_vector":{"3":0,"2":70,"1":71,"0":74},"success":"reg_ok"}
 			assert (o.get("success").equals("reg_ok"));
 
 			// get clocks (from register answer)
			String index = (String) o.get("index");
 			JSONObject v = o.getJSONObject("time_vector");
 			Iterator<Integer> i = v.keys();
 			while (i.hasNext()) {
 				Integer c = i.next();
 				clocks.put(c, o.getInt(c.toString()));
 			}
 
 			// get client list
 			o = execCmd(cmdGetClients()).getJSONObject("clients");
 			// answer: {"clients":
 			// {"/129.132.75.130":"QuestionBot","/129.132.252.221":"AnswerBot","/77.58.228.17":"willi"}
 			// }
 			Iterator<String> s = o.keys();
 			while (s.hasNext()) {
 				String c = s.next();
 				clients.put(c, (String) o.getString(c));
 			}
 
 			chatThread = new Thread(new ChatThread(handler));
 			chatThread.start();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void disconnect() {
 		chatThread.stop();
 		JSONObject o = execCmd(cmdDereg()); // answer {"success":"dreg_ok"}
 		try {
 			assert (o.getString("success").equals("dreg_ok"));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 }
