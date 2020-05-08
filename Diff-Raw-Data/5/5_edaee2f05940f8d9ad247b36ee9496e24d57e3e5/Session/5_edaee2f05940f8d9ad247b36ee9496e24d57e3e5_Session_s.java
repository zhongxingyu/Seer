 package Core;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.sql.ResultSet;
 
 public class Session implements Runnable, Opcode
 {
     private Client c;
     private ObjectInputStream in;
     private ObjectOutputStream out;
     
     private volatile Thread session;
     
     public Session(Client c, ObjectInputStream in, ObjectOutputStream out)
     {
         this.c = c;
         this.in = in;
         this.out = out;
     }
     
     public void stop()
     {
         session = null;
     }
     
     public ObjectInputStream getInputStream()
     {
         return this.in;
     }
     
     public ObjectOutputStream getOutputStream()
     {
         return this.out;
     }
     
     public void run()
     {
         try
         {
             this.session = Thread.currentThread();
             
             while(session == Thread.currentThread())
             {
                 byte b = in.readByte();
                 
                 switch(b)
                 {
                     case CMSG_GET_CONTACT_LIST:
                         HandleGetContactListOpcode();
                         break;
                     case CMSG_LOGOUT:
                         HandleLogoutOpcode();
                         break;
                     case CMSG_STATUS_CHANGED:
                         HandleStatusChangedOpcode();
                         break;
                     case CMSG_ADD_CONTACT:
                         HandleAddContactOpcode();
                         break;
                     case CMSG_CONTACT_ACCEPT:
                         HandleContactAcceptOpcode();
                         break;
                     case CMSG_CONTACT_DECLINE:
                         HandleContactDeclineOpcode();
                         break;
                     case CMSG_REMOVE_CONTACT:
                         HandleRemoveContactOpcode();
                         break;
                     case CMSG_SEND_CHAT_MESSAGE:
                         HandleChatMessageOpcode();
                         break;
                     default:
                         System.out.printf("Unknown Opcode Receive\n");
                         break;
                 }
             }
             
             System.out.printf("Session thread %d stopped successfully.\n", c.getGuid());
             
             c = null;
             out = null;
         }
         catch(Exception e){}
     }
     
     void HandleGetContactListOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_GET_CONTACT_LIST\n");
         
         ResultSet rs = Main.db.query("SELECT a.guid, a.username, a.title, a.psm FROM contact AS c LEFT JOIN account AS a ON c.c_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
         
         while(rs.next())
         {
             int guid = rs.getInt(1);
             String username = rs.getString(2);
             String title = rs.getString(3);
             String psm = rs.getString(4);
             
             Client target = Main.clientList.findClient(rs.getInt(1));
             
             int status = target != null ? target.getStatus() : 3;
             
             out.writeByte(SMSG_CONTACT_DETAIL);
             out.writeInt(guid);
             out.writeObject(username);
             out.writeObject(title);
             out.writeObject(psm);
             out.writeInt(status);
             out.flush();
             
             Thread.sleep(10);
             System.out.printf("Send Contact: %s to client %d\n", rs.getString(2), c.getGuid());
         }
         
         System.out.print("Send Opcode: SMSG_CONTACT_LIST_ENDED\n");
         out.writeByte(SMSG_CONTACT_LIST_ENDED);
         out.flush();
         
         System.out.printf("Send contact: Finish\n");
         
         System.out.printf("Send recent contact request to client %d.\n", c.getGuid());
         
         ResultSet requestRS = Main.db.query("SELECT a.guid, a.username FROM contact_request AS c LEFT JOIN account AS a ON c.r_guid = a.guid WHERE c.o_guid = %d", c.getGuid());
         
         while (requestRS.next())
         {
             System.out.printf("Send Contact Request: %s to client %d\n", requestRS.getString(2), c.getGuid());
             out.writeByte(SMSG_CONTACT_REQUEST);
             out.writeInt(requestRS.getInt(1));
             out.writeObject(requestRS.getString(2));
             out.flush();
         }                
     }
     
     void HandleLogoutOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_LOGOUT\n");
         
         Main.clientList.remove(c);
         
         System.out.printf("Closing client socket %d.\n", c.getGuid());
         c.getSocket().close();
         
         Main.db.execute("UPDATE account SET online = 0 WHERE guid = %d", c.getGuid());
         System.out.printf("Stopping session thread %d.\n", c.getGuid());
         
         ResultSet rs = Main.db.query("SELECT c_guid FROM contact WHERE o_guid = %d", c.getGuid());
         
         while(rs.next())
         {
             Client target = Main.clientList.findClient(rs.getInt(1));
         
             if (target != null)
                 target.getSession().SendStatusChanged(c.getGuid(), 3);
         }
         
         stop();               
     }
     
     void HandleStatusChangedOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_STATUS_CHANGED\n");
         
         int toStatus = in.readInt();
         c.setStatus(toStatus);
         
         System.out.printf("Client %d change status to %d.\n" , c.getGuid(), toStatus);
         
         ResultSet rs = Main.db.query("SELECT c_guid FROM contact WHERE o_guid = %d", c.getGuid());
         
         while(rs.next())
         {
             Client target = Main.clientList.findClient(rs.getInt(1));
         
             if (target != null)
                 target.getSession().SendStatusChanged(c.getGuid(), c.getStatus());
         }
         
         System.out.printf("Client %d update status to %d: Finish.\n", c.getGuid(), toStatus);
     }
     
     void HandleAddContactOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_ADD_CONTACT\n");
         
         String username = String.format("%s", in.readObject());
         
         // Contact to add is self
         if (c.getUsername().equalsIgnoreCase(username))
         {
             System.out.printf("Client %d add self to contact list.\n", c.getGuid());
             
             if (!Main.db.query("SELECT id FROM contact WHERE o_guid = %d AND c_guid= %d", c.getGuid(), c.getGuid()).first())
             {
                 out.writeByte(SMSG_ADD_CONTACT_SUCCESS);
                 out.writeInt(c.getGuid());
                 out.writeObject(c.getUsername());
                 out.writeObject(c.getTitle());
                 out.writeObject(c.getPSM());
                 out.writeInt(c.getStatus());
                 out.flush();
                 
                 Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), c.getGuid());
             }
             
             return;
         }
         
         ResultSet ars = Main.db.query("SELECT guid, username, title, psm FROM account WHERE username = '%s'", username);
         
         if (ars.first())
         {
             int guid = ars.getInt(1);
             username = ars.getString(2);
             String title = ars.getString(3);
             String psm = ars.getString(4);
             
             ResultSet acrs = Main.db.query("SELECT id FROM contact WHERE o_guid = %d and c_guid = %d", c.getGuid(), guid);
             
             if (acrs.first())
                 out.writeByte(SMSG_CONTACT_ALREADY_IN_LIST);
             else
             {
                 System.out.printf("Send Contact: %s to client %d\n", username, c.getGuid());
                 
                 Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), guid);
                 
                 Client target = Main.clientList.findClient(guid);
                 
                 int currentStatus = 3;
                 
                 ResultSet ccrs = Main.db.query("SELECT id FROM contact WHERE o_guid = %d and c_guid = %d", guid, c.getGuid());
                 
                 if (!ccrs.first())
                 {
                     if (target != null)
                     {
                         System.out.printf("Send Contact Request: %s to client %d\n", c.getUsername(), guid);
                         target.getSession().getOutputStream().writeByte(SMSG_CONTACT_REQUEST);
                         target.getSession().getOutputStream().writeInt(c.getGuid());
                         target.getSession().getOutputStream().writeObject(c.getUsername());
                         target.getSession().getOutputStream().flush();
                     }
                     else
                         Main.db.execute("INSERT INTO contact_request(o_guid, r_guid) VALUES(%d, %d)", guid, c.getGuid());
                 }
                 else
                 {
                     System.out.printf("Send Contact Request Cancel: %s is already in contact list of %s.\n", c.getUsername(), username);
                     
                     if (target != null)
                     {
                         currentStatus = target.getStatus();
                         
                         target.getSession().SendStatusChanged(c.getGuid(), c.getStatus());
                     }
                 }
                 
                 out.writeByte(SMSG_ADD_CONTACT_SUCCESS);
                 out.writeInt(guid);
                 out.writeObject(username);
                 out.writeObject(title);
                 out.writeObject(psm);
                 out.writeInt(currentStatus);
             }
         }
         else
         {
             out.writeByte(SMSG_CONTACT_NOT_FOUND);
         }
                         
         out.flush();
     }
     
     void HandleContactAcceptOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_CONTACT_ACCEPT\n");
         
         int guid = in.readInt();
         
         Main.db.execute("DELETE FROM contact_request WHERE o_guid = %d and r_guid = %d", c.getGuid(), guid);
         Main.db.execute("INSERT INTO contact(o_guid, c_guid) VALUES(%d, %d)", c.getGuid(), guid);
         
         ResultSet rrs = Main.db.query("SELECT username, title, psm FROM account WHERE guid = %d", guid);
         
         Client requestor = Main.clientList.findClient(guid);
         
         int requestorStatus = requestor != null ? requestor.getStatus() : 3;
         
         if (rrs.first())
         {
             System.out.printf("Send Contact: %s to client %d\n", rrs.getString(1), c.getGuid());
             out.writeByte(SMSG_ADD_CONTACT_SUCCESS);
             out.writeInt(guid);
             out.writeObject(rrs.getString(1));
             out.writeObject(rrs.getString(2));
             out.writeObject(rrs.getString(3));
             out.writeInt(requestorStatus);
             out.flush();
         }
         
         if (requestor != null)
             requestor.getSession().SendStatusChanged(c.getGuid(), c.getStatus());
     }
     
     void HandleContactDeclineOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_CONTACT_DECLINE\n");
         
         int guid = in.readInt();
         
         Main.db.execute("DELETE FROM contact_request WHERE o_guid = %d and r_guid = %d", c.getGuid(), guid);
     }
     
     void HandleRemoveContactOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_REMOVE_CONTACT\n");
         
         int guid = in.readInt();
         
         Main.db.execute("DELETE FROM contact WHERE o_guid = %d AND c_guid = %d", c.getGuid(), guid);
         
         Client target = Main.clientList.findClient(guid);
         
         if (target != null)
             target.getSession().SendStatusChanged(c.getGuid(), 3);
     }
     
     void HandleChatMessageOpcode() throws Exception
     {
         System.out.printf("\nOpcode: CMSG_SEND_CHAT_MESSAGE\n");
                         
         int from = c.getGuid();
         int to = in.readInt();
         String message = String.format("%s", in.readObject());
                         
         System.out.printf("Chat Message Receive From: %d, To %d, Message: %s\n", from, to, message);
         
         Client target = Main.clientList.findClient(to);
         
        if (target.getGuid() == to)
         {
             target.getSession().getOutputStream().writeByte(SMSG_SEND_CHAT_MESSAGE);
             target.getSession().getOutputStream().writeInt(from);
             target.getSession().getOutputStream().writeObject(message);
             target.getSession().getOutputStream().flush();
             System.out.printf("Send message success\n");
         }
     }
     
     void SendStatusChanged(int guid, int status) throws Exception
     {
         System.out.printf("Send status change From: %d, To: %d, Status: %d\n", guid, c.getGuid(), status);
         out.writeByte(SMSG_STATUS_CHANGED);
         out.writeInt(guid);
         out.writeInt(status);
         out.flush();
     }
 }
