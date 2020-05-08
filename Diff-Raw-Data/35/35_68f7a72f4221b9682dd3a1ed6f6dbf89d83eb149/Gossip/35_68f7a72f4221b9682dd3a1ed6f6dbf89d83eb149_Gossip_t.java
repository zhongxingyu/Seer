 package communication;
 
 import membership.MemberList;
 import communication.Messages.ProcessIdentifier;
 import membership.Proc;
 
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 public class Gossip {
 
     private static final Integer NUM_OF_TARGETS = 2;
     private AtomicBoolean shouldStop;
     private long delay;
     private Proc proc;
 
 
 
     public Gossip(){
         shouldStop = new AtomicBoolean(false);
         delay = 500;
     }
 
     private MemberList getMemberList(){
         return proc.getMemberList();
     }
 
     //TODO: this function is wrong, processes once have been picked in one round shouldn't be re-picked in same round
     public ProcessIdentifier selectRandomTarget(){
         Random rand = new Random();
         Integer randomTarget = rand.nextInt(getMemberList().size());
         return getMemberList().getProcessIdentifier(randomTarget);
     }
 
     public void start(){
 
         new Thread(new Runnable() {
 
             public void run() {
                 while(!shouldStop.get()) {
                     try {
                         Thread.sleep(delay);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     startInfecting();
                 }
             }
         }).start();
 
     }
 
     private void startInfecting(){
         for(Integer i = 0; i < NUM_OF_TARGETS; i++){
             ProcessIdentifier infectedProcess = selectRandomTarget();
             if(notSelf(infectedProcess)) {
                 sendSyncMessage(infectedProcess);
             }
         }
     }
 
     private boolean notSelf(ProcessIdentifier identifier) {
         return !proc.getId().equals(identifier.getId());
     }
 
     void sendSyncMessage(ProcessIdentifier process){
         UDPClient udpClient = new UDPClient(process);
         Messages.Message message = MessagesFactory.generateSyncProcessMessage(
                proc.getTimeStamp(), proc.getIdentifier(), proc.getMemberList());
         udpClient.sendMessage(message.toByteArray());
     }
 
     public void stop() {
         shouldStop.set(true);
         Thread.interrupted();
     }
 
     public void setProc(Proc proc) {
         this.proc = proc;
     }
 
 }
