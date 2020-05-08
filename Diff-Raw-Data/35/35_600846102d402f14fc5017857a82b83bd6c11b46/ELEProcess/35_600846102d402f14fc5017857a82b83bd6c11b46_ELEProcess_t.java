 public class ELEProcess extends Process {
 
 	public ELEProcess(String name, int id, int size) {
 		super(name, id, size);
 		elector = new EventuallyLeaderElector(this);
 	}
 
 	private IFailureDetector elector;
 
 	public void begin() {
 		elector.begin();
 	}
 
 	public synchronized void receive(Message m) {
		String type = m.getType();
		if (type.equals("heartbeat")) {
			elector.receive(m);
		}
 	}
 
 	public static void main(String[] args) {
 		String name = args[0];
 		int id = Integer.parseInt(args[1]);
 		int n = Integer.parseInt(args[2]);
 		ELEProcess p = new ELEProcess(name, id, n);
 		p.registeR();
 		p.begin();
 	}
 
 }
