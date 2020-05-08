 package com.github.karahiyo.hanoi_picker;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 
 /**
  * receive message from PickerClient, and count-up each keys.
  * 
  * @author yu_ke
  *
  */
 public class PickerDaemon implements Runnable {
 
 	private DataInputStream is = null;
 	private PrintStream os = null;
 
 	/** server socket */
 	private ServerSocket serverSocket;
 	
 	/** log output directory. default "/tmp" */
 	private String outdir = "/tmp";
 	private final String outfile = "hanoi-trace.log";
 
 	/** set port */
 	private static final int PORT = 9999;
 	private static final int MINIMUM_PORT_NUMBER = 1024;
 	private static final int MAXMUM_PORT_NUMBER = 65535;
 
 	/** a flag for receiving data from client now. */
 	private boolean         isRecievedNow;
 
 	/** a flag for receiving terminate message. */
 	private boolean         isTermination;
 
 	/** server socket timeout(ms) */
 	public static final int    TIMEOUT_SERVER_SOCKET     = 500;
 
 	/** daemon start time */
 	private long 	DAEMON_START_TIME; 
 	private long 	LAST_HIST_OUT_TIME;
 
 	/** hist out interval */
 	public static final int HIST_OUT_INTETRVAL = 5000; //ms
 
 	/** main histogram data */
 	public Map<String, Long> hist = new HashMap<String, Long>();
 
 	// construct
 	public PickerDaemon() {}
 	
 	/**
 	 * setup socket connection
 	 * @param 
 	 * @throws IOException
 	 */
 	public void setupSocket() throws IOException {
 		this.serverSocket = new ServerSocket(this.PORT);
 		this.serverSocket.setSoTimeout(this.TIMEOUT_SERVER_SOCKET);
 	}
 	
 	/**
 	 * setup socket connection 
 	 * @param port
 	 * @throws IOException
 	 */
 	public void setupSocket(int port) throws IOException {
 		this.serverSocket = new ServerSocket(port);
 		this.serverSocket.setSoTimeout(this.TIMEOUT_SERVER_SOCKET);
 	}
 	
 	public void setupOutDir(String file) throws IOException {
 		this.outdir = file;
 	}
 
 	public void run() {
 
 		DAEMON_START_TIME = System.currentTimeMillis();
 		LAST_HIST_OUT_TIME = System.currentTimeMillis();
 
 		while ( ! this.isTermination) {
 			Socket connectedSocket = null;
 
 			try {
 				connectedSocket = this.serverSocket.accept();
 			} catch (SocketTimeoutException timeExc) {
 				// nothing to do
 			} catch (IOException e) {
 				System.out.println(e);
 			}
 
 			// periodical output hist 
 			if(LAST_HIST_OUT_TIME + HIST_OUT_INTETRVAL <= System.currentTimeMillis() ) {
 				long timestamp = LAST_HIST_OUT_TIME+ HIST_OUT_INTETRVAL; 
 				String json = makeJsonString(timestamp, hist);
 
 				// debug
 				System.out.println(json);
 
 				PrintWriter pw;
 				try {
					File log_file = new File(this.outdir + "/" + this.outfile);
					pw = new PrintWriter(new BufferedWriter(new FileWriter(log_file)));
 					pw.println(json);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				/** update */
 				LAST_HIST_OUT_TIME += HIST_OUT_INTETRVAL;
 				hist.clear();
 			}
 
 
 			// not accepting data
 			if ( connectedSocket == null) {
 				continue;
 			}
 
 			try {
 				/* get data input stream of socket */
 				is = new DataInputStream(connectedSocket.getInputStream());
 
 				/* get output stream of sub process */
 				os  = new PrintStream(connectedSocket.getOutputStream());
 				BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 
 
 
 				String line;
 				while ( (line = bf.readLine()) != null) {
 					line = URLDecoder.decode(line, "UTF-8");
 					if ( hist.containsKey(line) ) {
 						hist.put(line, hist.get(line) + 1);
 					} else {
 						hist.put(line, 1L);
 					}
 					os.println(line);
 				}
 
 
 				/* close child process */
 				if (is != null) is.close();
 				if (os != null) os.close();
 
 			} catch(Exception e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					/* close child process */
 					if (is != null) is.close();
 					if (os != null) os.close();
 				} catch (IOException e) {
 					System.out.println(e);
 				}
 			}
 		}
 	}
 
 	public String makeJsonString(long time, Map<String, Long> hist) {
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("time", time);
 		map.put("keymap", hist);
 		long sum = countAllFreq(hist);
 		map.put("sum", sum);
 		ObjectMapper mapper = new ObjectMapper();
 		String json = null;
 
 		try {
 			json = mapper.writeValueAsString(map);
 		} catch (JsonGenerationException e) {
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return json;
 	}
 	
 	public long countAllFreq(Map<String, Long> map) {
 		int sum = 0;
 		for (Map.Entry<String, Long> e : map.entrySet()) {
 			sum += e.getValue();
 		}
 		return sum;
 	}
 }
 
