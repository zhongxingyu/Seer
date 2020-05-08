 package user;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import database.DataBaseObject;
 
 
 public abstract class Message extends DataBaseObject {
 	protected String sender;
 	protected String recipient;
 	protected boolean beenRead;
 	
 	//Constructor from db row
 	public Message(String[] args, Connection conn) {
 		super(args, conn);
 		sender = args[1];
 		recipient = args[2];
 		beenRead = Boolean.parseBoolean(args[4]);
 	}
 	
 	//Constructor of new Message
 	public Message(String sender, String recipient) {
 		this.sender = sender;
 		this.recipient = recipient;
 		beenRead = false;
 		
 	}
 	public String getSender() {
 		return sender;
 	}
 	
 	public String getRecipient() {
 		return recipient;
 	}
 	
 	public void setBeenRead(Connection conn) {
 		beenRead = true; //can never "unread" something
 		saveToDataBase(conn);
 	}
 	
 	public void saveToMsg(Connection conn) {
 		try {
 			Statement stmt = conn.createStatement();
 			String query;
 			if (dbID == -1) {
 				generateID(conn, "Message");
 				query = "Insert into Message VALUES ('" + dbID + "', '" + sender + "', '" + recipient + "', " + beenRead + ");";
 				stmt.executeUpdate(query);
 			} else {
				query = "UPDATE Message set beenRead='"+ beenRead +"' WHERE id='" + dbID + "';";
 				stmt.executeUpdate(query);
 			}
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String getHTMLSummary() {
 		return "This is the deault message summary. <br/>";
 	}
 	
 
 }
