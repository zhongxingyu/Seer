 package net.donizyo.hexempire;
 
 public final class ServerEntry {
 	private final Server server;
 
 	static {
 		initEngine();
 	}
 	
 	protected static native void initEngine();
 
 	public ServerEntry(Server parent) {
 		server = parent;
 	}
 
 	public native void process(ClientHandler handler, int command);
 
	protected static native void destroyEngine();
 }
