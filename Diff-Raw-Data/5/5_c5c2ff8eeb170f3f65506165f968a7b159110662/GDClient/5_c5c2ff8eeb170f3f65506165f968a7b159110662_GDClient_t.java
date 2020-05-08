 package com.dbstar.guodian;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.LinkedList;
 
 import com.dbstar.guodian.data.LoginData;
 import com.dbstar.util.GDNetworkUtil;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Message;
 import android.os.Process;
 import android.util.Log;
 
 public class GDClient {
 	private static final String TAG = "GDClient";
 
 	public static final int MSG_REQUEST = 0x1001;
 	public static final int MSG_RESPONSE = 0x1002;
 	public static final int MSG_COMMAND = 0x1003;
 
 	// Command type
 	public static final int CMD_CONNECT = 0x2001;
 	public static final int CMD_STOP = 0x2002;
 
 	// Request type
 	public static final int REQUEST_LOGIN = 0x3001;
 
 	class Task {
 		public int TaskType;
 		public String TaskId;
 		public String Command;
 		public String[] ResponseData;
 		public Object ParsedData;
 	}
 
 	private String mHostAddr = null;
 	private int mHostPort = 0;
 	private Socket mSocket = null;
 	private BufferedReader mIn = null;
 	private BufferedWriter mOut = null;
 	ReceiveThread mInThread;
 	HandlerThread mClientThread = null;
 	Handler mClientHandler = null;
 	Context mContext = null;
 
 	LinkedList<Task> mWaitingQueue = new LinkedList<Task>();
 	Handler mAppHander = null;
 
 	public GDClient(Context context, Handler handler) {
 		mContext = context;
 		mAppHander = handler;
 
 		mClientThread = new HandlerThread("GDClient",
 				Process.THREAD_PRIORITY_BACKGROUND);
 		mClientThread.start();
 
 		mClientHandler = new Handler(mClientThread.getLooper()) {
 			@Override
 			public void handleMessage(Message msg) {
 				int msgType = msg.what;
 				switch (msgType) {
 				case MSG_COMMAND: {
 					performCommand(msg.arg1, msg.obj);
 					break;
 				}
 				case MSG_REQUEST: {
 					performRequest((Task) msg.obj);
 					break;
 				}
 				case MSG_RESPONSE: {
 					handleResponse((String) msg.obj);
 					break;
 				}
 				}
 			}
 		};
 
 	}
 
 	public void setHostAddress(String hostAddr, int port) {
 		mHostAddr = hostAddr;
 		mHostPort = port;
 	}
 
 	public void connectToServer() {
 		Message msg = mClientHandler.obtainMessage(MSG_COMMAND);
 		msg.arg1 = CMD_CONNECT;
 		msg.sendToTarget();
 	}
 
 	public void login() {
 		Task task = new Task();
 		String taskId = GDCmdHelper.generateUID();
 		String macAddr = GDNetworkUtil.getMacAddress(mContext, true);
 		String cmdStr = GDCmdHelper.constructLoginCmd(taskId, macAddr);
 
 		task.TaskType = REQUEST_LOGIN;
 		task.TaskId = taskId;
 		task.Command = cmdStr;
 
 		Message msg = mClientHandler.obtainMessage(MSG_REQUEST);
 		msg.obj = task;
 		msg.sendToTarget();
 	}
 
 	public void stop() {
 		Log.d(TAG, " ============ stop GDClient thread ============");
 		Message msg = mClientHandler.obtainMessage(MSG_COMMAND);
 		msg.arg1 = CMD_STOP;
 		msg.sendToTarget();
 	}
 
 	public void destroy() {
 		Log.d(TAG, " ============ destroy GDClient thread ============");
 		mClientThread.quit();
 
 		doStop();
 	}
 
 	// run in client thread
 	private void performCommand(int cmdType, Object cmdData) {
 		switch (cmdType) {
 		case CMD_CONNECT: {
 			doConnectToServer();
 			break;
 		}
 		case CMD_STOP: {
 			doStop();
 			break;
 		}
 		}
 	}
 
 	private void performRequest(Task task) {
 		doRequest(task);
 	}
 
 	private void handleResponse(String response) {
 		Log.d(TAG, " ++++++++++++handleResponse++++++++" + response);
 
 		String[] data = GDCmdHelper.processResponse(response);
 		String id = data[0];
 		Task task = null;
 		for (Task t : mWaitingQueue) {
 			if (t.TaskId.equals(id)) {
 				mWaitingQueue.remove(t);
 				task = t;
 				task.ResponseData = data;
 				processResponse(task);
 			}
 		}
 	}
 
 	private void processResponse(Task task) {
 
 		Log.d(TAG, " ++++++++++++processResponse++++++++" + task.TaskType);
 
 		switch (task.TaskType) {
 		case REQUEST_LOGIN: {
 			LoginData loginData = LoginDataHandler.parse(task.ResponseData[6]);
 			task.ParsedData = loginData;
 			break;
 		}
 		}
 
 		if (mAppHander != null) {
 			Message msg = mAppHander
 					.obtainMessage(GDEngine.MSG_REQUEST_FINISHED);
 			msg.obj = task;
 			msg.sendToTarget();
 		}
 	}
 
 	private void doConnectToServer() {
 		try {
 			Log.d(TAG, " ====== doConnectToServer ===");
 			if (mSocket != null) {
 				if (mSocket.isConnected() && !mSocket.isClosed()) {
 					return;
 				}
 
 				mSocket = null;
 			}
 
 			Log.d(TAG, " server ip = " + mHostAddr + " port=" + mHostPort);
 
 			mSocket = new Socket(mHostAddr, mHostPort);
 			mSocket.setKeepAlive(true);
 
 			mIn = new BufferedReader(new InputStreamReader(
					mSocket.getInputStream(), "UTF-8"));
 
 			Log.d(TAG, " ==== mIn " + mSocket.isInputShutdown());
 
 			mOut = new BufferedWriter(new OutputStreamWriter(
					mSocket.getOutputStream(), "UTF-8"));
 
 			Log.d(TAG, " ==== mOut " + mSocket.isOutputShutdown());
 
 			mInThread = new ReceiveThread(mSocket, mIn, mClientHandler);
 			mInThread.start();
 
 			Log.d(TAG, " ====== doConnectToServer ===" + mSocket.isConnected());
 
 			if (mSocket.isConnected()) {
 				mAppHander.sendEmptyMessage(GDEngine.MSG_CONNECT_SUCCESSED);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean isConnectionSetup() {
 		return (mSocket != null && mSocket.isConnected());
 	}
 
 	private boolean isOutputAvailable() {
 		if (mSocket == null)
 			return false;
 
 		Log.d(TAG, "isOutputShutdown " + mSocket.isOutputShutdown());
 
 		return isConnectionSetup() && !mSocket.isClosed();
 	}
 
 	private void doRequest(Task task) {
 
 		Log.d(TAG, "======= doRequest =========");
 		Log.d(TAG, "task type" + task.TaskType);
 		Log.d(TAG, "task cmd" + task.Command);
 
 		if (!isOutputAvailable()) {
 			Log.d(TAG, "======= no connection to server =========");
 			return;
 		}
 
 		mWaitingQueue.add(task);
 
 		try {
 			mOut.write(task.Command);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// stop receive thread, 
 	// close socket.
 	private void doStop() {
 		Log.d(TAG, " ============ doStop ============");
 
 		if (mInThread != null) {
 			mInThread.setExit();
 			mInThread = null;
 		}
 
 		Log.d(TAG, " ============ stop 1 ============");
 
 		try {
 			if (mSocket != null && (mSocket.isConnected() || !mSocket.isClosed())) {
 				if (!mSocket.isInputShutdown()) {
 					mSocket.shutdownInput();
 				}
 				
 				if (!mSocket.isOutputShutdown()) {
 					mSocket.shutdownOutput();
 				}
 
 				mSocket.close();
 			}
 			
 			mSocket = null;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		mWaitingQueue.clear();
 
 		Log.d(TAG, " ============ stop 3 ============");
 	}
 }
