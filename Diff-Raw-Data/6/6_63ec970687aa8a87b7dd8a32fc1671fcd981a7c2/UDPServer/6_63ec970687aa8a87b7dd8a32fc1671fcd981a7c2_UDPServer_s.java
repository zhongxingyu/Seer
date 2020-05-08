 package communication;
 
 import filesystem.SDFS;
 import membership.MemberList;
 import membership.Proc;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import static communication.Messages.*;
 
 public class UDPServer {
     private Integer udpPort;
     private AtomicBoolean shouldStop;
     private DatagramSocket serverSocket;
     private static Logger logger = Logger.getLogger(UDPServer.class);
     private Proc proc;
 
     private static final Integer BUFFER_SIZE = 4096;
 
     public UDPServer(Integer udpPort) {
         this.udpPort = udpPort;
         shouldStop = new AtomicBoolean(false);
     }
 
 
 
     public boolean start() {
 
         try {
             serverSocket = new DatagramSocket(udpPort);
         } catch (SocketException e) {
             logger.error("udp server binding port error", e);
             return false;
         }
 
         new Thread(new Runnable() {
             @Override
             public void run() {
                 startListening();
             }
         }).start();
         return true;
     }
 
     private void startListening() {
 
         while(!shouldStop.get()) {
             DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
             try {
                 serverSocket.receive(packet);
                 int len = packet.getLength();
                 byte[] bytes = new byte[len];
                 System.arraycopy(packet.getData(), 0, bytes, 0, len);
                 Message message = Message.parseFrom(bytes);
 //                logger.debug("Received Message: " + message.toString());
                 handleMessage(message);
 
             } catch (IOException e) {
                 if(e.getMessage().equals("socket close")) {
                     break;
                 } else {
                     logger.error("udp server socket exception" + e);
                 }
             }
 
         }
     }
 
     public void handleMessage(Message m) {
         proc.increaseAndGetTimeStamp();
 
         switch (m.getType()) {
             case SyncProcesses:
                 SyncProcessesMessage syncProcessesMessage = m.getSyncProcessesMessage();
                 handleSyncMessage(syncProcessesMessage);
                 break;
 
             case Heartbeat:
                 HeartBeatMessage heartBeatMessage = m.getHeartBeatMessage();
                 ProcessIdentifier fromMachine = heartBeatMessage.getFromMachine();
 //                System.out.println("Receive heart beat from " + fromMachine.getIP() + ":" + fromMachine.getPort());
                 proc.getFailureDetector().onReceivingHeartBeat();
                 break;
 
             case SyncFiles:
                 SyncFilesListMessage syncFilesListMessage = m.getSyncFilesMessage();
                 handleSyncFileListMessage(syncFilesListMessage);
                 break;
 
             default:
                 break;
         }
     }
 
     public void handleSyncMessage(SyncProcessesMessage spm) {
         synchronized (this) {
             List<ProcessIdentifier> list = spm.getMembersList();
 //            MemberList newMemberList = new MemberList();
 
 //            for(ProcessIdentifier identifier : list) {
 //                Integer pos = proc.getMemberList().find(identifier);
 //                if(pos != -1) {
 //                    ProcessIdentifier identifierInProc = proc.getMemberList().get(pos);
 //                    if(identifierInProc.getTimestamp() > identifier.getTimestamp()) {
 //                        newMemberList.add(identifierInProc, proc.getMemberList().getTime(pos));
 //                        continue;
 //                    }
 //                }
 //
 //                newMemberList.add(identifier);
 //            }
 //
 //            for(ProcessIdentifier identifier : proc.getMemberList()) {
 //                if(newMemberList.find(identifier)==-1) {
 //                    newMemberList.add(identifier);
 //                }
 //            }
             for(ProcessIdentifier identifier : list) {
                 MemberList memberlist = proc.getMemberList();
                 Integer pos = memberlist.find(identifier);
                 if(pos == -1) {
                     memberlist.add(identifier);
                 } else {
                     ProcessIdentifier identifierInMemberList = memberlist.get(pos);
                     if(identifier.getTimestamp() > identifierInMemberList.getTimestamp()) {
                         memberlist.updateProcessIdentifier(identifier);
                     }
                 }
             }
         }
     }
 
     public void handleSyncFileListMessage(SyncFilesListMessage sfm) {
         List<FileIdentifier> list = sfm.getFilesList();
         List<Integer> timeStampList = sfm.getTimestampList();
         SDFS sdfs = proc.getSDFS();
 
         for(int i=0; i<list.size(); ++i) {
             Integer timeStamp = timeStampList.get(i);
             FileIdentifier identifier = list.get(i);
             if(sdfs.getFileList().find(identifier) != -1) {
                 Integer oldTimeStamp = sdfs.getFileTimeStamp(identifier);
                 if(oldTimeStamp < timeStamp) {
                     sdfs.updateFileListEntry(identifier, timeStamp);
                 } else if (identifier.getFileStoringProcess().getId().equals(proc.getId())) {
                     sdfs.updateFileListEntry(identifier, timeStamp);
                 }
             } else {
                 sdfs.addAvailableEntryToFileList(identifier, timeStamp);
             }
         }
     }
 
 
     public void setProc(Proc proc) {
         this.proc = proc;
     }
 
     public void stop() {
         shouldStop.set(true);
     }
 }
