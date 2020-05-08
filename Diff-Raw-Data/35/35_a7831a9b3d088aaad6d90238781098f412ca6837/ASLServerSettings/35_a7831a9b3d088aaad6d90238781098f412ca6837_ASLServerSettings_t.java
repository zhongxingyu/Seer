 package asl;
 
 import java.nio.charset.Charset;
 
 public class ASLServerSettings {
 	public static final int SOCKET_PORT = 8123;
 	public static final int NUM_CLIENTREQUESTWORKER_THREADS = 20;
	public static final Charset CHARSET = Charset.forName("UTF-8");
	public static final int MESSAGE_MAX_LENGTH = 4096;
 }
