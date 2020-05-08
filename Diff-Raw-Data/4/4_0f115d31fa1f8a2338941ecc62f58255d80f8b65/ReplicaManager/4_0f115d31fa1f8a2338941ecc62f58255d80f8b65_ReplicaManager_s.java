 package filesystem;
 
 import communication.FileIdentifierFactory;
 import communication.Messages.ProcessIdentifier;
 import communication.Messages.FileIdentifier;
 import membership.MemberList;
 import membership.Proc;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import membership.MemberList;
 
 public class ReplicaManager {
 
     private Proc proc;
     private AtomicBoolean shouldStop;
 
     private static final Integer SCAN_INTERVAL = 5000;
     private static final Integer REPLICA_COUNT = 2;
 
     public ReplicaManager(){
         shouldStop = new AtomicBoolean(false);
     }
 
     public void start(){
         new Thread(new Runnable() {
             @Override
             public void run() {
                 while(!shouldStop.get()){
                     try {
                         Thread.sleep(SCAN_INTERVAL);
                     } catch(InterruptedException e) {
                         //
                     }
                     scanFileList();
                 }
             }
         }).start();
 
     }
 
     private FileList getFileList(){
         return proc.getSDFS().getFileList();
     }
 
     private void scanFileList(){
         HashMap<String, Integer> replicaCounter = new HashMap<String, Integer>();
         List<FileIdentifier> fileShouldBeReplicated = new LinkedList<FileIdentifier>();
 
         for(FileIdentifier f: getFileList()) {
             if(!f.getFileStoringProcess().getId().equals(proc.getId())) {   //this file doesn't stored locally
                 continue;
             }
            fileShouldBeReplicated.add(f);
         }
 
         for(FileIdentifier f : getFileList()){
 
             String key = f.getFilepath();
 
             if(!replicaCounter.containsKey(key)){
                 replicaCounter.put(key, 1);
             }
             else {
                 //TODO recheck if its correct
                 replicaCounter.put(key, replicaCounter.get(key)+1);
             }
         }
 
         for(FileIdentifier f: fileShouldBeReplicated) {
             Integer replicaCount = replicaCounter.get(f.getFilepath());
             Integer requiredReplicas = Math.min(REPLICA_COUNT, proc.getMemberList().size());
             requiredReplicas = requiredReplicas - replicaCount;
             if(requiredReplicas <=0 ) {
                 continue;
             }
             createReplicas(requiredReplicas, f.getFilepath());
         }
     }
 
     public void createReplicas(Integer requiredReplicas, String SDFSFilepath){
 
         List<ProcessIdentifier> replicateTo = new ArrayList<ProcessIdentifier>(requiredReplicas);
 
         for(int i = 0; i < requiredReplicas;){
             ProcessIdentifier randomProcess = selectRandomProcess();
             if(!replicateTo.contains(randomProcess)
                     && notMySelf(randomProcess) && notStoredOnProcess(randomProcess, SDFSFilepath)){
                 replicateTo.add(randomProcess);
                 new FileOperations().setProc(proc).sendPutMessage(SDFSFilepath, randomProcess.getIP(), randomProcess.getPort());
                 i++;
             }
         }
     }
 
     private boolean notMySelf(ProcessIdentifier identifier) {
         return ! identifier.getId().equals(proc.getId());
     }
 
     private boolean notStoredOnProcess(ProcessIdentifier identifier, String filePath) {
         FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(identifier, filePath);
         return proc.getSDFS().getFileList().find(fileIdentifier) == -1;
     }
 
     private boolean theSameProcessIdentifier(ProcessIdentifier p1, ProcessIdentifier p2){
         return p1.getId().equals(p2.getId());
     }
 
     public ProcessIdentifier selectRandomProcess(){
         Random rand = new Random();
         Integer randomProcess = rand.nextInt(proc.getMemberList().size());
         if(randomProcess < 0) randomProcess = 0;
         return proc.getMemberList().get(randomProcess);
     }
 
     public void setProc(Proc proc){
         this.proc = proc;
     }
 
     public void stop(){
         shouldStop.set(true);
         Thread.interrupted();
     }
 }
