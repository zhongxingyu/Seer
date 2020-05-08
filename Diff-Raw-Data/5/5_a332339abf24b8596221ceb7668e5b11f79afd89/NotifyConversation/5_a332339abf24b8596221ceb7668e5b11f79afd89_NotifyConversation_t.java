 package com.nexus.notify;
 
 import java.lang.reflect.Type;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import com.google.common.collect.Lists;
 import com.google.gson.reflect.TypeToken;
 import com.nexus.NexusServer;
 import com.nexus.interfaces.ISaveJson;
 import com.nexus.json.IProvideJsonMap;
 import com.nexus.json.JSONList;
 import com.nexus.json.JSONPacket;
 import com.nexus.logging.NexusLog;
 import com.nexus.mysql.MySQLHelper;
 import com.nexus.mysql.TableList;
 import com.nexus.network.PacketDispatcher;
 import com.nexus.network.packets.Packet20NotifyMessage;
 import com.nexus.network.packets.Packet21NotifyConversationCreate;
 import com.nexus.users.User;
 import com.nexus.utils.Utils;
 
 
 public class NotifyConversation implements IProvideJsonMap, ISaveJson{
 	
 	private List<User> Participants = Lists.newArrayList();
 	private List<NotifyMessage> Messages = Lists.newArrayList();
 	
 	private int ID;
 	
 	private String Name;
 	
 	public static NotifyConversation FromID(int id){
 		return NexusServer.instance().Notify.getConversationFromID(id);
 	}
 	
 	public static NotifyConversation FromResultSet(ResultSet rs){
 		Type ListType = new TypeToken<ArrayList<Integer>>(){}.getType();
 		NotifyConversation conversation = new NotifyConversation();
 		try{
 			conversation.ID = rs.getInt("ID");
 			conversation.Name = rs.getString("Name");
 			ArrayList<Integer> p = Utils.Gson.fromJson(rs.getString("Participants"), ListType);
 			for(Integer s : p){
 				conversation.Participants.add(User.FromID(s));
 			}
 		}catch(SQLException e){}
 		return conversation;
 	}
 	
 	public NotifyConversation(){
 		
 	}
 	
 	public NotifyConversation(String name){
 		this.Name = name;
 	}
 	
 	public void AddParticipant(User u){
 		this.Participants.add(u);
 	}
 
 	public int getID(){
 		return this.ID;
 	}
 
 	public String getName(){
 		return this.Name;
 	}
 
 	public List<User> getParticipants(){
 		return this.Participants;
 	}
 	
 	@Override
 	public JSONPacket toJsonMap(){
 		JSONPacket p = new JSONPacket();
 		p.put("ID", this.ID);
 		p.put("Name", this.Name);
 		p.put("Participants", JSONList.fromUserList(this.Participants));
 		return p;
 	}
 
 	@Override
 	public void writeToJson(String key, JSONPacket packet){
 		packet.put(key, this.toJsonMap());
 	}
 
 	@Override
 	public void readFromJson(String key, JSONPacket packet){
 		if(packet.get(key) instanceof JSONPacket){
 			new Exception("Unexpected call, fix me!").printStackTrace();
 			/*JSONPacket p = packet.getJSON(key);
 			this.ID = p.getInt("ID");
 			this.Name = p.getString("Name");
 			
 			Type ArrayType = new TypeToken<ArrayList<String>>(){}.getType();
 			List<String> Participants = Utils.Gson.fromJson(p.getString("participants"), ArrayType);
 			
 			for(String u : Participants){
 				this.AddParticipant(User.FromUsername(u));
 			}*/
 		}else{
 			NotifyConversation c = NotifyConversation.FromID(packet.getInt(key));
 			this.ID = c.ID;
 			this.Messages = c.Messages;
 			this.Name = c.Name;
 			this.Participants = c.Participants;
 		}
 	}
 
 	public void SendMessage(NotifyMessage message){
 		this.Messages.add(message);
 		Packet20NotifyMessage packet = new Packet20NotifyMessage();
 		packet.Conversation = this;
 		packet.Message = message;
 		PacketDispatcher.SendPacketToAllUsersInIterable(this.getParticipants(), packet);
 	}
 
 	public void Create(){
		List<Integer> participantList = Lists.newArrayList();
 		for(User u : this.Participants){
			participantList.add(u.getID());
 		}
 		try{
 			Connection conn = MySQLHelper.GetConnection();
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(String.format("INSERT INTO %s(Name, Participants) VALUES('%s','%s')", TableList.TABLE_CONVERSATIONS, this.Name, Utils.Gson.toJson(participantList)), Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			rs.first();
 			this.ID = rs.getInt(1);
 			rs.close();
 			stmt.close();
 			conn.close();
 		
 			NexusServer.instance().Notify.RegisterConversation(this);
 			PacketDispatcher.SendPacketToAllUsersInIterable(this.getParticipants(), new Packet21NotifyConversationCreate(this));
 		}catch(SQLException e){
 			NexusLog.log(Level.SEVERE, e, "Database error while registering an conversation in the database");
 		}
 	}
 }
