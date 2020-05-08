 package synthseq;
 
 
 import synthseq.clojureinterop.ClojureServer;
 import synthseq.oscinterop.OSCServer;
 import telnet.Telnet;
 
 public class Main {
 	public static void main(String[] args) throws Exception {
		new Telnet("localhost", 9000);
 		ClojureServer.start(9000);
 		OSCServer.start(8000);
 	}
 }
