 public class Annotate {
 	public static native void init();
 	public static native void startTask(String name);
 	public static native void endTask(String name);
 	public static native void setPathID(int pathid);
 	public static native void endPathID(int pathid);
 	public static native void notice(String str);
	public static native void send(int msgid, int size);
	public static native void receive(int msgid, int size);
 
 	static {
 		System.loadLibrary("jannotate");
 	}
 }
