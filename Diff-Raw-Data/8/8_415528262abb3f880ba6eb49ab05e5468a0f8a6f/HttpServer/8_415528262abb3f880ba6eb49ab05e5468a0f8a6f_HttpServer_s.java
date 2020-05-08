 package de.hsrm.inspector.web;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.http.ConnectionReuseStrategy;
 import org.apache.http.HttpException;
 import org.apache.http.impl.DefaultConnectionReuseStrategy;
 import org.apache.http.impl.DefaultHttpResponseFactory;
 import org.apache.http.impl.DefaultHttpServerConnection;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.BasicHttpProcessor;
 import org.apache.http.protocol.HttpRequestHandlerRegistry;
 import org.apache.http.protocol.HttpService;
 import org.apache.http.protocol.ResponseConnControl;
 import org.apache.http.protocol.ResponseContent;
 import org.apache.http.protocol.ResponseDate;
 import org.apache.http.protocol.ResponseServer;
 
 import android.content.Context;
 import android.util.Log;
 import de.hsrm.inspector.gadgets.intf.Gadget;
 import de.hsrm.inspector.gadgets.pool.ResponsePool;
 import de.hsrm.inspector.handler.GadgetHandler;
 import de.hsrm.inspector.handler.ResponseHandler;
 
 /**
  * WebServer Thread to parse inspector's config file and start apache server
  * with pattern handler.
  */
 public class HttpServer extends Thread {
 
 	public static final String SERVER_NAME = "html5audio";
 	public static final int SERVER_PORT = 9090;
 	public static final int STATE_PORT = 9191;
 	public static final int SERVER_BACKLOG = 50;
 	private static final String DEFAULT_PATTERN = "/inspector/*";
 	private static final String STATE_PATTERN = "/state/*";
 	private ServerSocket mCommandSocket, mResponseSocket;
 
 	private final BasicHttpProcessor mHttpProcessor;
 	private final HttpParams mHttpParams;
 	private final HttpRequestHandlerRegistry mHttpRegistry;
 	private final ConnectionReuseStrategy mReuseStrat;
 	private GadgetHandler mGadgetHandler;
 	private ResponseHandler mResponseHandler;
 	private volatile Thread mCommandWorker, mResponseWorker;
 	private ResponsePool mResponsePool;
 
 	private ConcurrentHashMap<String, Gadget> mConfiguration;
 	private long mTimeout = 60;
 	private Timer mTimeoutTimer;
 
 	/**
 	 * Constructor for web server.
 	 * 
 	 * @param context
 	 *            Android application context.
 	 * @param configuration
 	 *            InputStream for configuration file.
 	 */
 	public HttpServer(Context context, ResponsePool responsePool) {
 		super(SERVER_NAME);
 		mReuseStrat = new DefaultConnectionReuseStrategy();
 		mResponsePool = responsePool;
 
 		mHttpParams = new BasicHttpParams();
 		mHttpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 60000)
 				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
 				.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
 				.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
 
 		mHttpProcessor = new BasicHttpProcessor();
 		mHttpProcessor.addInterceptor(new ResponseDate());
 		mHttpProcessor.addInterceptor(new ResponseServer());
 		mHttpProcessor.addInterceptor(new ResponseContent());
 		mHttpProcessor.addInterceptor(new ResponseConnControl());
 
 		mHttpRegistry = new HttpRequestHandlerRegistry();
 		mGadgetHandler = new GadgetHandler(context, mResponsePool);
 		mHttpRegistry.register(DEFAULT_PATTERN, mGadgetHandler);
 		mResponseHandler = new ResponseHandler(context, mResponsePool);
 		mHttpRegistry.register(STATE_PATTERN, mResponseHandler);
 	}
 
 	/**
 	 * Safe method to start server.
 	 */
 	public synchronized void startThread() {
 		if (mCommandSocket == null) {
 			try {
 				mCommandSocket = new ServerSocket();
 				mCommandSocket.setReuseAddress(true);
 				mCommandSocket.setReceiveBufferSize(10);
 				mCommandSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), SERVER_PORT),
 						SERVER_BACKLOG);
 				RequestWorker worker = new RequestWorker(mCommandSocket);
 				mCommandWorker = new Thread(worker);
 				worker.setWorkerThread(mCommandWorker);
 				mCommandWorker.setDaemon(false);
 				mCommandWorker.start();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (mCommandSocket != null && mResponseSocket == null) {
 			try {
 				mResponseSocket = new ServerSocket();
 				mResponseSocket.setReuseAddress(true);
 				mResponseSocket.setReceiveBufferSize(10);
 				mResponseSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), STATE_PORT),
 						SERVER_BACKLOG);
 				RequestWorker worker = new RequestWorker(mResponseSocket);
 				mResponseWorker = new Thread(worker);
 				worker.setWorkerThread(mResponseWorker);
 				mResponseWorker.setDaemon(false);
 				mResponseWorker.start();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Safe method to stop server.
 	 */
 	public synchronized void stopThread() {
 		if (mCommandSocket != null) {
 			try {
 				mCommandSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				mCommandSocket = null;
 			}
 			if (mCommandWorker != null) {
 				mCommandWorker.interrupt();
 			}
 		}
 		if (mResponseSocket != null) {
 			try {
 				mResponseSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				mResponseSocket = null;
 			}
 			if (mResponseWorker != null) {
 				mResponseWorker.interrupt();
 			}
 		}
 	}
 
 	/**
 	 * Returns true is {@link HttpServer} is started.
 	 * 
 	 * @return {@link Boolean}
 	 */
 	public synchronized boolean isRunning() {
 		return mCommandSocket != null && !mCommandSocket.isClosed();
 	}
 
 	/**
 	 * Returns {@link #mConfiguration}.
 	 * 
 	 * @return {@link ConcurrentHashMap}
 	 */
 	public ConcurrentHashMap<String, Gadget> getConfiguration() {
 		return mConfiguration;
 	}
 
 	/**
 	 * Sets {@link #mConfiguration}.
 	 * 
 	 * @param map
 	 *            {@link ConcurrentHashMap} to set.
 	 */
 	public void setConfiguration(ConcurrentHashMap<String, Gadget> map) {
 		mConfiguration = map;
 		mGadgetHandler.setGadgetConfiguration(mConfiguration);
 	}
 
 	/**
 	 * Set {@link #mTimeout} for {@link #mTimeoutTimer}.
 	 * 
 	 * @param timeout
 	 *            {@link Long} in milliseconds.
 	 */
 	public void setTimeoutTime(long timeout) {
 		this.mTimeout = timeout;
 	}
 
 	/**
 	 * Starts {@link #mTimeoutTimer}.
 	 */
 	public synchronized void startTimeout() {
 		if (mTimeoutTimer != null) {
 			mTimeoutTimer.cancel();
 		}
 
 		mTimeoutTimer = new Timer();
 		TimerTask task = new TimerTask() {
 
 			@Override
 			public void run() {
 				Log.e("SERVER", "Server timed out!");
 				HttpServer.this.stopThread();
 				mResponsePool.clearAll();
 			}
 		};
 		mTimeoutTimer.schedule(task, mTimeout);
 	}
 
 	/**
 	 * Stops {@link #mTimeoutTimer}.
 	 */
 	public synchronized void stopTimeout() {
 		if (mTimeoutTimer != null) {
 			mTimeoutTimer.cancel();
 		}
 	}
 
 	/**
 	 * The {@link RequestWorker} accepts multiple HTTP-Requests and encapsulate
 	 * them into {@link Worker}.
 	 */
 	public class RequestWorker implements Runnable {
 
 		private ServerSocket mSocket;
 		private Thread mWorkerThread;
 
 		public RequestWorker(ServerSocket socket) {
 			mSocket = socket;
 		}
 
 		public void setWorkerThread(Thread workerThread) {
 			mWorkerThread = workerThread;
 		}
 
 		@Override
 		public void run() {
 			Log.d("Server", "Server: " + mSocket.getInetAddress().toString() + ":" + mSocket.getLocalPort());
 			while ((mSocket != null) && (mWorkerThread == Thread.currentThread()) && !Thread.interrupted()) {
 				try {
 					accept();
 				} catch (SocketException e) {
 					e.printStackTrace();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		/**
 		 * Accepting new HTTP-Connections, creating {@link Worker} threads and
 		 * start them.
 		 * 
 		 * @throws IOException
 		 */
 		protected void accept() throws IOException, HttpException, SocketException {
 			Socket socket = mSocket.accept();
 			DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
 			conn.bind(socket, new BasicHttpParams());
 
 			HttpService httpService = new HttpService(mHttpProcessor, mReuseStrat, new DefaultHttpResponseFactory());
 			httpService.setHandlerResolver(mHttpRegistry);
 			httpService.handleRequest(conn, new BasicHttpContext());
 		}
 
 	}
 }
