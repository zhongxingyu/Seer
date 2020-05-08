 public class AnnTest implements Runnable {
 	public static void main(String[] args) {
 		Annotate.init();
 		Annotate.setPathID(5040);
 		Annotate.startTask("one to a hundred million");
 		for (int i=0; i<100000000; i++) {}
 		Annotate.startTask("subtask");
 		Annotate.endTask("subtask");
		Thread t = new Thread(new AnnTest());
		t.run();
 		Annotate.endTask("one to a hundred million");
 		Annotate.startTask("has an event");
 		Annotate.notice("the event");
 		Annotate.endTask("has an event");
 	}
 	public void run() {
		System.out.println("child thread");
 		Annotate.setPathID(8712);
 		Annotate.startTask("child's hundred million");
 		for (int i=0; i<100000000; i++) {}
 		Annotate.endTask("child's hundred million");
 	}
 }
