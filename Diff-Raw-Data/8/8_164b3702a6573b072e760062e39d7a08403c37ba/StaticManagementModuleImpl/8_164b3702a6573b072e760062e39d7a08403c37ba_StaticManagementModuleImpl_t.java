 package se.umu.cs.jsgajn.gcom.management;
 
 import java.io.IOException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Properties;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.PriorityBlockingQueue;
 import java.util.concurrent.atomic.AtomicLong;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import se.umu.cs.jsgajn.gcom.Client;
 import se.umu.cs.jsgajn.gcom.GNS;
 import se.umu.cs.jsgajn.gcom.MemberCrashException;
 import se.umu.cs.jsgajn.gcom.Message;
 import se.umu.cs.jsgajn.gcom.MessageCouldNotBeSentException;
 import se.umu.cs.jsgajn.gcom.MessageImpl;
 import se.umu.cs.jsgajn.gcom.MessageType;
 import se.umu.cs.jsgajn.gcom.communication.CommunicationModule;
 import se.umu.cs.jsgajn.gcom.communication.CommunicationsModuleImpl;
 import se.umu.cs.jsgajn.gcom.communication.Multicast;
 import se.umu.cs.jsgajn.gcom.communication.MulticastType;
 import se.umu.cs.jsgajn.gcom.communication.Multicasts;
 import se.umu.cs.jsgajn.gcom.debug.Debugger;
 import se.umu.cs.jsgajn.gcom.ordering.Ordering;
 import se.umu.cs.jsgajn.gcom.ordering.OrderingModule;
 import se.umu.cs.jsgajn.gcom.ordering.OrderingModuleImpl;
 import se.umu.cs.jsgajn.gcom.ordering.OrderingType;
 import se.umu.cs.jsgajn.gcom.ordering.Orderings;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * author dit06ajn, dit06jsg
  */
 public class StaticManagementModuleImpl implements ManagementModule {
     private static final Logger logger = LoggerFactory.getLogger(StaticManagementModuleImpl.class);
     private static final Debugger debugger = Debugger.getDebugger();
 
     private boolean staticGroupInitialized = false;
     private Client client;
     private GNS gns;
     private GroupView groupView;
     private OrderingModule orderingModule;
     private CommunicationModule communicationModule;
     private GroupLeader gl;
     //private Receiver receiver;
 
     // Own group member
     private GroupMember groupMember;
 
     // Queue to contain newly delivered messages
     private LinkedBlockingQueue<Message> receiveQueue;
 
     // Queue to contain messages that should be sent
     private PriorityBlockingQueue<FIFOEntry<Message>> sendQueue;
 
     private Thread messageReceiverThread;
     private List<Thread> messageSenderThreads;
     private boolean running;
     private List<String> memberNames;
 
     public StaticManagementModuleImpl(Client client, String gnsHost, int gnsPort,
             String groupName, List<String> memberNames)
     throws RemoteException, AlreadyBoundException, NotBoundException {
         this(client, gnsHost, gnsPort, groupName, Registry.REGISTRY_PORT, memberNames);
     }
 
     /**
      * Implementations responsible for management of group, communication with
      * group and ordering of messages. Clients should use this class to send
      * messages and receive messages with a group.
      *
      * @param gnsHost GNS host
      * @param gnsPort GNS port
      * @param groupName Group name to connect to
      * @throws RemoteException If GNS throws exception
      * @throws AlreadyBoundException If it's not possible to bind this to own register.
      * @throws NotBoundException If GNS stub is not found in GNS register.
      */
     public StaticManagementModuleImpl(final Client client, final String gnsHost,
             final int gnsPort, final String groupName, final int clientPort,
             List<String> memberNames)
     throws RemoteException, AlreadyBoundException, NotBoundException, IllegalArgumentException {
         this.client = client;
         this.receiveQueue = new LinkedBlockingQueue<Message>();
         this.sendQueue = new PriorityBlockingQueue<FIFOEntry<Message>>();
 
         this.orderingModule = new OrderingModuleImpl(this);
         this.communicationModule = new CommunicationsModuleImpl(this, clientPort);
         this.communicationModule.setOrderingModule(this.orderingModule);
         this.orderingModule.setCommunicationsModule(this.communicationModule);
 
         this.memberNames = memberNames;
         this.groupMember = new GroupMember(communicationModule.getReceiver(), memberNames.get(0));
         // Temp groupview
         this.groupView =  new GroupViewImpl(groupName, this.groupMember);
         GroupSettings gs = initGroupSettings(groupMember, groupName);
 
         this.gns = getGNS(gnsHost, gnsPort);
         // TODO: what happens if another client connects directly after this
         //       client. Not all threads are started.
         gs = gns.connect(gs);
         Ordering o = Orderings.newInstance(gs.getOrderingType());
         this.orderingModule.setOrdering(o);
         Multicast m = Multicasts.newInstance(gs.getMulticastType());
         this.communicationModule.setMulticastMethod(m);
 
         // Start modules
         this.orderingModule.start();
         this.communicationModule.start();
 
         if (gs.isNew()) { // Group is empty I am leader
            // Remove own name from membernames
            memberNames.remove(memberNames.get(0));
             if (memberNames.size() < 2) {
                 client.deliver("Static group with one member, why!");
                 System.exit(1); 
             }
             logger.debug("Got new group from GNS, this member is leader");
             this.gl = new GroupLeaderImpl();
             debugger.groupChange(this.groupView);
         } else {
             logger.debug("Got existing group from GNS, sending join message");
             MessageImpl joinMessage =
                 new MessageImpl(this.groupMember,
                         MessageType.JOIN,
                         PID,
                         groupView.getID());
             this.groupView = new GroupViewImpl(groupName, gs.getLeader());
             this.groupView.add(this.groupMember);
             //.getReceiver().receive(joinMessage);
             send(joinMessage, this.groupView);
         }
 
         this.messageReceiverThread = new Thread(new MessageReceiver(), "GroupModule Receive-Thread");
 
         int nrSendThreads = 2;
         this.messageSenderThreads = new ArrayList<Thread>(nrSendThreads);
         for (int i = 0; i < nrSendThreads; i++) {
             this.messageSenderThreads.add(new Thread(new MessageSender(), "GroupModule Send-Thread " + i));
         }
         //this.messageSenderThread = new Thread(new MessageSender(), "GroupModule Send-Thread");
 
         start();
     }
 
     public void start() {
         logger.debug("Starting GroupModuleImpl");
         //Module is started in constructor
         this.running = true;
         this.messageReceiverThread.start();
         for (Thread t : this.messageSenderThreads) {
             t.start();
         }
     }
 
     public void stop() {
         logger.debug("Stopping GroupModuleImpl");
         this.running = false;
         this.communicationModule.stop();
         this.orderingModule.stop();
     }
 
     private GroupSettings initGroupSettings(GroupMember leader, String groupName) {
         Properties prop = new Properties();
         Properties sys = System.getProperties();
         String ordering = "FIFO";
         String multicastMethod = "BASIC_MULTICAST";
         try {
             prop.load(this.getClass().getResourceAsStream("/application.properties"));
 
             // Get ordering
             if (sys.containsKey("gcom.ordering")) {
                 ordering = sys.getProperty("gcom.ordering");
             } else if (prop.containsKey("gcom.ordering")) {
                 ordering = prop.getProperty("gcom.ordering");
             }
 
             // Get multicastMethod
             if (sys.containsKey("gcom.multicast")) {
                 multicastMethod = sys.getProperty("gcom.multicast");
             } else if (prop.containsKey("gcom.multicast")) {
                 multicastMethod = prop.getProperty("gcom.multicast");
             }
         } catch (IOException e) {
             logger.warn("application.properties not found");
         }
 
         OrderingType otype = OrderingType.valueOf(ordering);
         MulticastType mtype = MulticastType.valueOf(multicastMethod);
         if (otype == null || mtype == null) {
             throw new Error("Coudn't initialize groupsettings");
         }
         logger.debug("Init groupsettings: " + mtype + ", " + otype);
         return new GroupSettings(groupName, leader, mtype, otype);
     }
 
     /**
      * Will send package Object in appropriate Message and send it to every
      * group member.
      *
      * @param clientMessage The Object to send.
      */
     public void send(Object clientMessage) {
         Message m = new MessageImpl(clientMessage,
                 MessageType.CLIENTMESSAGE, PID, groupView.getID());
         send(m, this.groupView);
     }
 
     /**
      * If not groupchange-messages will send message
      * to {@link OrderingModule} -> {@link CommunicationModule} ->
      * every member of the {@link GroupView}.
      *
      * If groupchange-message will send directly to {@link CommunicationModule}
      *
      * @param m The Message to send.
      * @param g The GroupView Message should be sent to.
      */
     public void send(Message m, GroupView g) {
         if (m.getMessageType().equals(MessageType.CLIENTMESSAGE)
                 && !staticGroupInitialized) {
             client.deliver("All members are not connected, please wait");
             return;
         }
         sendQueue.put(new FIFOEntry<Message>(m));
     }
 
     public GroupView getGroupView() {
         // Used in reliable multicast to resend all messages that are note yet
         // received
         GroupView gCopy;
         synchronized (groupView) {
             gCopy = new GroupViewImpl(groupView);
         }
         return gCopy;
     }
 
     /** GroupLeader ************/
     private class GroupLeaderImpl implements GroupLeader {
         public void removeFromGroup(GroupMember member) {
             throw new Error("TODO: not implemented");
         }
 
         public boolean isAllMembersConnected() {
            logger.info("Members connected? Size {}", memberNames.size());
            return memberNames.isEmpty();
         }
 
         public void addMemberToGroup(GroupMember member) {
             // TODO: fix this
             GroupView groupViewCopy;
 
             synchronized (groupView) {
                 // Only add members that are in memberNames
                 if (memberNames.contains(member.getName())) {
                    memberNames.remove(member.getName());
                     groupView.add(member);
                 }
                 groupViewCopy = new GroupViewImpl(groupView);
             } 
 
             // Multicast new groupView
             if (isAllMembersConnected()) {
                 send(new MessageImpl(groupViewCopy, MessageType.GROUPCHANGE, PID, groupViewCopy.getID()),
                         groupView); // TODO: Important don't send with wrong send!!!!
             }
         }
     }
 
     /**
      * Thread to handle message sending.
      */
     private class MessageSender implements Runnable {
         public void run() {
             while (running) {
                 try {
                     FIFOEntry<Message> fifoEntry = sendQueue.take();
                     logger.debug("Sending message: [{}]", fifoEntry);
                     Message m = fifoEntry.getEntry();
                     GroupView groupViewCopy;
 
                     synchronized (groupView) {
                         groupViewCopy = new GroupViewImpl(groupView);
 
                         // If memberCrash message, don't send it to the crashed members
                         if (m.getMessageType().equals(MessageType.MEMBERCRASH)) {
                             if (!groupViewCopy.removeAll(((CrashList ) m.getMessage()).getAll())) {
                                 logger.warn("Tried to send MEMBERCRASH but the member that crashed is not in my list");
                             }
                         }
                     }
                     try {
                         orderingModule.send(m, groupViewCopy);
                     } catch (MemberCrashException e) {
                         logger.debug("Caught MemberCrashException, trying to send fifoEntry: [{}]", fifoEntry);
                         handleMemberCrashException(e);
                     } catch (MessageCouldNotBeSentException e) {
                         logger.debug("Caught MessageCouldNotBeSentException fifoEntry: {}",
                                 fifoEntry);
                         handleMemberCrashException(new MemberCrashException(e.getCrashedMembers()));
                         // Put message back in queue
                         sendQueue.put(fifoEntry);
                         logger.debug("FIFOEntry put back in queue, fifoEntry: [{}]", fifoEntry);
                     }
                 } catch (InterruptedException e) {
                     // TODO - fix error message
                     e.printStackTrace();
                 }
             }
         }
 
         private void handleMemberCrashException(MemberCrashException e) {
 
             CrashList crashedMembers = e.getCrashedMembers();
             Message memberCrashMessage = new MessageImpl(crashedMembers,
                     MessageType.MEMBERCRASH, ManagementModule.PID,
                     groupView.getID());
             send(memberCrashMessage, groupView);
         }
     }
 
     /**
      * Thread to handle message receiving.
      */
     private class MessageReceiver implements Runnable {
         public void run() {
             while (running) {
                 try {
                     handleDelivered(receiveQueue.take());
                 } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
 
         private void handleDelivered(Message m) {
             logger.info("Got message!");
             MessageType type = m.getMessageType();
             switch (type) {
             case GROUPCHANGE:
                 logger.info("GROUPCHANGE");
                 staticGroupInitialized = true;
                 synchronized (groupView) {
                     groupView = (GroupView) m.getMessage();
                 }
                 debugger.groupChange(groupView);
                 break;
             case CLIENTMESSAGE:
                 logger.info("CLIENTMESSAGE");
                 client.deliver(m.getMessage());
                 break;
             case MEMBERCRASH:
                 logger.info("MEMBERCRASH");
                 // TODO: what now?
                 handleMemberCrash((CrashList) m.getMessage());
                 break;
             case JOIN:
                 logger.info("JOIN");
                 if (gl == null) {
                     logger.error("Got join message but I'm not leader");
                 } else {
                     logger.info("Join message new member, (THIS module is the group leader)");
                     gl.addMemberToGroup((GroupMember) m.getMessage());
                 }
                 break;
             default:
                 logger.error("Unkwon MessageType in message: " + m);
             }
         }
 
         private void handleMemberCrash(CrashList crashedMembers) {
             // TODO: verify and test this!
             synchronized (groupView) {
                 // Leader crash?
                 boolean newLeader = false;
                 if (crashedMembers.contains(groupView.getGroupLeaderGroupMember())) {
                     logger.debug("Leader crashed");
                     if (!groupView.removeAll(crashedMembers.getAll())) {
                         logger.warn("Someone crashed, but is not in my list!");
                     }
                     // Am I the new leader?
                     if (ManagementModule.PID.equals(groupView.getHighestUUID())) {
                         logger.info("I am new leader :)");
                         newLeader = true;
                         try {
                             gns.setNewLeader(
                                     StaticManagementModuleImpl.this.groupMember, groupView.getName());
                             groupView.setNewLeader(StaticManagementModuleImpl.this.groupMember);
                             StaticManagementModuleImpl.this.gl = new GroupLeaderImpl(); // TODO: Sync erros?
                         } catch (RemoteException e1) {
                             logger.debug("Error, GNS cant change groupleader");
                             e1.printStackTrace();
                         }
                     }
                 }
 
                 // Am I leader
                 if (groupView.getGroupLeaderGroupMember().getPID().equals(ManagementModule.PID)) {
                     logger.info("Leader: Remove all crashed members, send a GroupChange");
                     if (!groupView.removeAll(crashedMembers.getAll())) {
                         logger.warn("No members removed, newLeader: {}", newLeader);
                     }
                     Message groupChangeMessage =
                         new MessageImpl(groupView, MessageType.GROUPCHANGE,
                                 ManagementModule.PID,
                                 groupView.getID());
                     send(groupChangeMessage, groupView);
                 }
             }
         }
     }
 
     public void deliver(Message m) {
         try {
             debugger.messageDelivered(m);
             receiveQueue.put(m);
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     /**
      * @param host Host to GNS
      * @param port Port to GNS
      * @return GNS stub
      * @throws RemoteException If GNS throws exception
      * @throws NotBoundException If GNS stub can't be found.
      */
     private GNS getGNS(String host, int port) throws RemoteException,
     NotBoundException {
         Registry gnsReg = LocateRegistry.getRegistry(host, port);
         return (GNS) gnsReg.lookup(GNS.STUB_NAME);
     }
 
     protected static class FIFOEntry<E extends Comparable<? super E>>
     implements Comparable<FIFOEntry<E>> {
         final static AtomicLong seq = new AtomicLong();
         final long seqNum;
         final E entry;
         public FIFOEntry(E entry) {
             seqNum = seq.getAndIncrement();
             this.entry = entry;
         }
 
         public E getEntry() { return entry; }
 
         public int compareTo(FIFOEntry<E> other) {
             int res = entry.compareTo(other.entry);
             if (res == 0 && other.entry != this.entry)
                 res = (seqNum < other.seqNum ? -1 : 1);
             return res;
         }
 
         @Override
         public String toString() {
             return "Seq: " + seqNum + ", entry: " + entry.toString();
         }
     }
 }
