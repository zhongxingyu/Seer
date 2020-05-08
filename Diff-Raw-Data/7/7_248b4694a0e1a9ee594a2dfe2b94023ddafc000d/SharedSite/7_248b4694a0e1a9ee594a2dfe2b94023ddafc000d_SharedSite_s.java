 package museumassault.monitor;
 
 import java.util.HashMap;
 import museumassault.Room;
 import museumassault.Team;
 import museumassault.custom_message.HandCanvasMessage;
 import museumassault.custom_message.PrepareAssaultMessage;
 import museumassault.message_broker.IndexedMessageBroker;
 import museumassault.message_broker.Message;
 import museumassault.message_broker.MessageBroker;
 
 /**
  *
  * @author André
  */
 public class SharedSite implements ChiefControlSite, ThievesConcentrationSite
 {
     public static final int PREPARE_ASSAULT_ACTION = 1;
     public static final int SEND_ASSAULT_PARTY_ACTION = 2;
     public static final int THIEF_HAND_CANVAS_ACTION = 3;
     public static final int THIEF_ARRIVE_ACTION = 4;
     public static final int THIEF_READY_FOR_DEPARTURE_ACTION = 5;
 
     protected final MessageBroker chiefBroker = new IndexedMessageBroker();
     protected final MessageBroker thievesBroker = new IndexedMessageBroker();
 
     protected Room[] rooms;
     protected HashMap roomsHash = new HashMap();
 
     protected Team[] teams;
     protected HashMap teamsHash = new HashMap();
     protected HashMap teamsBroker = new HashMap();
 
     protected int nrRoomsToBeRobed;
     protected int nrCanvasCollected = 0;
 
     /**
      *
      */
     public SharedSite(Room[] rooms, Team[] teams)
     {
         this.teams = teams;
         int nrTeams = teams.length;
         for (int x = 0; x < nrTeams; x++) {
             this.teamsHash.put(teams[x].getId(), teams[x]);
             this.teamsBroker.put(teams[x].getId(), new IndexedMessageBroker());
         }
 
         this.rooms = rooms;
         this.nrRoomsToBeRobed = rooms.length;
         for (int x = 0; x < this.nrRoomsToBeRobed; x++) {
             this.roomsHash.put(rooms[x].getId(), rooms[x]);
         }
     }
 
     /**
      *
      */
     @Override
     public Integer appraiseSit()
     {
         synchronized (this.chiefBroker) {
 
             if (this.nrRoomsToBeRobed > 0) {
 
                 int nrRooms = this.rooms.length;
                 for (int x = 0; x < nrRooms; x++) {
                     if (this.rooms[x].stillHasCanvas() && !this.rooms[x].isBeingRobed()) {
                         this.rooms[x].isBeingRobed(true);
                         return this.rooms[x].getId();
                     }
                 }
             }
 
             return null;
         }
     }
 
     /**
      *
      */
     @Override
     public Integer prepareAssaultParty(int roomId)
     {
         Room room = (Room) this.roomsHash.get(roomId);
         if (room == null) {
             throw new RuntimeException("Unknown room with id #" + roomId);
         }
 
         synchronized (this.chiefBroker) {
 
             int nrTeams = this.teams.length;
 
             for (int x = 0; x < nrTeams; x++) {
 
                 if (!this.teams[x].isBusy() && !this.teams[x].isPrepared()) {
 
                     this.teams[x].isPrepared(true);
                     this.teams[x].setAssignedRoom(room);
                     int nrthieves = this.teams[x].getNrthieves();
                     for (int y = 0; y < nrthieves; y++) {
                         this.thievesBroker.writeMessage(new PrepareAssaultMessage(PREPARE_ASSAULT_ACTION, this.teams[x].getId(), roomId));
                         synchronized (this.thievesBroker) {
                             this.thievesBroker.notify();
                         }
                     }
 
                     return this.teams[x].getId();
                 }
             }
 
             room.isBeingRobed(false);
 
             return null;
         }
     }
 
     /**
      *
      */
     @Override
     public void sendAssaultParty(int teamId)
     {
         Team team = (Team) this.teamsHash.get(teamId);
         if (team == null) {
             throw new RuntimeException("Unknown team with id #" + teamId);
         }
 
         synchronized (this.chiefBroker) {
 
             if (!team.isPrepared()) {
                 throw new RuntimeException("Team with id #" + teamId + " is not prepared yet.");
             }
 
             team.isBusy(true);
 
             MessageBroker broker = (MessageBroker) this.teamsBroker.get(teamId);
             int nrthieves = team.getNrthieves();
             for (int x = 0; x < nrthieves; x++) {
 
                 while (broker.readMessage(THIEF_READY_FOR_DEPARTURE_ACTION) == null) {
                     Thread.yield();
                 }
 
                 broker.writeMessage(new Message(SEND_ASSAULT_PARTY_ACTION));
             }
 
             System.out.println("[Chief] Notifying all thieves of the party to start crawling to room #" + team.getAssignedRoom().getId() + "..");
 
             synchronized (broker) {
                 broker.notifyAll();
             }
         }
     }
 
     /**
      *
      * @return
      */
     @Override
     public Integer takeARest()
     {
         while (true) {
 
             synchronized (this.chiefBroker) {
 
                 Message message = this.chiefBroker.readMessage(THIEF_ARRIVE_ACTION);
                 if (message != null) {
                     return message.getOriginId();
                 }
 
                 int nrTeams = this.teams.length;
                 boolean assaultRunning = false;
                 for (int x = 0; x < nrTeams; x++) {
                     if (this.teams[x].isBusy()) {
                         assaultRunning = true;
                         break;
                     }
                 }
 
                 if (!assaultRunning) {
                     return null;
                 }
 
                 try {
                     this.chiefBroker.wait();
                 } catch (InterruptedException ex) {}
             }
         }
     }
 
     /**
      *
      */
     @Override
     public void collectCanvas(int thiefId)
     {
         while (true) {
 
             HandCanvasMessage message = (HandCanvasMessage) this.chiefBroker.readMessage(THIEF_HAND_CANVAS_ACTION, thiefId);
             if (message != null) {
 
                 Team team = (Team) this.teamsHash.get(message.getTeamId());
                 if (team == null) {
                     throw new RuntimeException("Unknown team with id #" + message.getTeamId());
                 }
 
                 synchronized (this.chiefBroker) {
 
                     if (message.rolledCanvas()) this.nrCanvasCollected++;
 
                     Room room = team.getAssignedRoom();
                     if (room.stillHasCanvas() && !message.rolledCanvas()) {
                         room.stillHasCanvas(false);
                         this.nrRoomsToBeRobed--;
                     }
                 }
 
                 break;
             }
         }
     }
 
     /**
      *
      */
     @Override
     public Integer amINeeded(int thiefId)
     {
         while (true) {
 
             synchronized (this.thievesBroker) {
 
                 System.out.println("[Thief #" + thiefId + "] Waiting for orders..");
                 try {
                     this.thievesBroker.wait();
                 } catch (InterruptedException ex) {}
             }
 
             PrepareAssaultMessage message = (PrepareAssaultMessage) this.thievesBroker.readMessage(PREPARE_ASSAULT_ACTION);
             if (message != null) {
                 System.out.println("[Thief #" + thiefId + "] I am needed on team " + message.getTeamId() + "..");
                 return message.getTeamId();
             }
         }
     }
 
 
     /**
      *
      */
     @Override
     public void prepareExcursion(int teamId) {
 
         MessageBroker broker = (MessageBroker) this.teamsBroker.get(teamId);
         if (broker == null) {
             throw new RuntimeException("Unknown team with id #" + teamId);
         }
 
         broker.writeMessage(new Message(THIEF_READY_FOR_DEPARTURE_ACTION));
 
         while (true) {
 
             synchronized (broker) {
 
                 Team team = (Team) this.teamsHash.get(teamId);
                 team.incrementNrBusythieves();
 
                 try {
                     broker.wait();
                 } catch (InterruptedException ex) {}
             }
 
             Message message = broker.readMessage(SEND_ASSAULT_PARTY_ACTION);
             if (message != null) {
                 break;
             }
         }
     }
 
     /**
      *
      */
     @Override
     public void handACanvas(int thiefId, int teamId, boolean rolledCanvas)
     {
         Team team = (Team) this.teamsHash.get(teamId);
         if (team == null) {
             throw new RuntimeException("Unknown team with id #" + teamId);
         }
 
         MessageBroker broker = (MessageBroker) this.teamsBroker.get(teamId);

         synchronized (broker) {
             team.decrementNrBusythieves();
         }
 
         this.chiefBroker.writeMessage(new Message(THIEF_ARRIVE_ACTION, thiefId));
         this.chiefBroker.writeMessage(new HandCanvasMessage(THIEF_HAND_CANVAS_ACTION, thiefId, team.getId(), rolledCanvas));
         synchronized (this.chiefBroker) {
             this.chiefBroker.notify();
         }
     }
 }
