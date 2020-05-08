 
 public class SFDProcess extends Process {
     private StrongFailureDetector strongFailureDetection;
 
     public SFDProcess(String name, int pid, int n, int x) {
         super(name, pid, n);
 
         strongFailureDetection = new StrongFailureDetector(this, x);
     }
 
     public void begin() {
         strongFailureDetection.begin();
     }
 
     public synchronized void receive(Message message) {
 
         String type = message.getType();
         if (type.equals(Utils.HEARTBEAT_MESSAGE)) {
             strongFailureDetection.receive(message);
         } else if(type.equals(Utils.STRONG_FAILURE_LEADER_ELECTION)) {
             strongFailureDetection.addStrongLeaderElectionMessage(message);
         }
 
     }
 
     public static void main (String [] args){
         String name = args[0];
         int id = Integer.parseInt(args[1]);
         int n = Integer.parseInt(args[2]);
       // int x = Integer.parseInt(args[3]);
 
        SFDProcess p = new SFDProcess(name, id, n, id);
         p.registeR();
         p.begin();
     }
 
 }
