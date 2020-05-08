 package com.nexus.scheduler;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Type;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.gson.reflect.TypeToken;
 import com.nexus.NexusServer;
 import com.nexus.event.Event.EnumResult;
 import com.nexus.event.EventListener;
 import com.nexus.event.EventPriority;
 import com.nexus.event.events.scheduler.SchedulerAppendObjectEvent;
 import com.nexus.event.events.scheduler.SchedulerEvent;
 import com.nexus.logging.NexusLog;
 import com.nexus.mysql.MySQLHelper;
 import com.nexus.mysql.TableList;
 import com.nexus.network.packets.Packet4SchedulerUpdate;
 import com.nexus.time.NexusTime;
 import com.nexus.users.User;
 import com.nexus.utils.EnumDirection;
 import com.nexus.utils.JSONPacket;
 import com.nexus.utils.Utils;
 
 public class Scheduler implements Runnable{
 	
 	private final Thread SchedulerThread;
 	private boolean Running = false;
 	
 	public List<Scheduled> Timeline;
 	
 	private final Logger Log = NexusLog.MakeLogger("Scheduler");
 	
 	public Scheduler(){
 		this.SchedulerThread = new Thread(this);
 		this.SchedulerThread.setName("Scheduler");
 		this.Timeline = new ArrayList<Scheduled>();
 		
 		NexusServer.EventBus.register(this);
 		
 		this.CacheTimeline();
 	}
 	
 	public void Start(){
 		this.Running = true;
 		this.SchedulerThread.start();
 		this.Log.info("Starting scheduler thread");
 	}
 	
 	public void Stop(){
 		this.Running = false;
 		this.Log.info("Stopping scheduler thread");
 	}
 	
 	@Override
 	public void run(){
 		while(Running){
 			try{
 				Date CurrentTime = NexusTime.GetCurrentDate();
 				for(int i = 0; i < Timeline.size(); i++){
 					Scheduled Object = Timeline.get(i);
 					if(Object.Duration == 0 || this.DatesAreEqual(Object.Starttime, Object.Endtime)) continue;
 					if(DateIsEarlier(Object.Endtime, CurrentTime)){
 						Object.SetIsRunned();
 						continue;
 					}
 					if(Object.ThrustartType == EnumThrustartType.Hand) continue;
 					if(DateIs15SecAfter(Object.Starttime, CurrentTime)){
 						NexusServer.EventBus.post(new SchedulerEvent.BeforeStart(Object));
 					}
 					if(DateIsLater(Object.Starttime, CurrentTime)){
 						continue;
 					}
 					if(DatesAreEqual(Object.Starttime, CurrentTime)){
 						NexusServer.EventBus.post(new SchedulerEvent.Start(Object));
 					}else if(DateIs15SecAfter(Object.Endtime, CurrentTime)){
 						NexusServer.EventBus.post(new SchedulerEvent.BeforeEnd(Object));
 					}else if(DatesAreEqual(Object.Endtime, CurrentTime)){
 						NexusServer.EventBus.post(new SchedulerEvent.End(Object));
 					}else if(DateIsEarlier(Object.Starttime, CurrentTime) && DateIsLater(Object.Endtime, CurrentTime)){
 						Object.Tick();
 					}
 				}
 			}catch(Exception e){
 				NexusLog.log(Log.getName(), Level.SEVERE, e, "Exception in Scheduler loop!");
 			}
 			try{
 				Thread.sleep(998);
 			}catch(Exception e){
 			}
 		}
 	}
 	
 	private boolean DateIs15SecAfter(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 		cal1.setTime(date1);
 		cal2.setTime(date2);
 		cal2.add(Calendar.SECOND, 15);
 		cal1.set(Calendar.MILLISECOND, 0);
 		cal2.set(Calendar.MILLISECOND, 0);
 		return cal1.equals(cal2);
 	}
 	
 	private boolean DatesAreEqual(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 		cal1.setTime(date1);
 		cal2.setTime(date2);
 		cal1.set(Calendar.MILLISECOND, 0);
 		cal2.set(Calendar.MILLISECOND, 0);
 		return cal1.equals(cal2);
 	}
 	
 	private boolean DateIsEarlier(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 		cal1.setTime(date1);
 		cal2.setTime(date2);
 		cal1.set(Calendar.MILLISECOND, 0);
 		cal2.set(Calendar.MILLISECOND, 0);
 		return cal1.before(cal2);
 	}
 	
 	private boolean DateIsLater(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 		cal1.setTime(date1);
 		cal2.setTime(date2);
 		cal1.set(Calendar.MILLISECOND, 0);
 		cal2.set(Calendar.MILLISECOND, 0);
 		return cal1.after(cal2);
 	}
 	
 	public void CacheTimeline(){
 		Log.info("Creating local copy of the scheduler timeline");
 		try{
 			this.Timeline.clear();
 			Connection conn = MySQLHelper.GetConnection();
 			Statement stmt = conn.createStatement();
 			// ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE ID >= (SELECT ID FROM %s WHERE Type = 0 AND Starttime < NOW() ORDER BY ID DESC LIMIT 1) OR Endtime > NOW()", TableList.TABLE_TIMELINE, TableList.TABLE_TIMELINE));
 			ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s ORDER BY `Index` ASC", TableList.TABLE_TIMELINE));
 			while (rs.next()){
 				this.Timeline.add(Scheduled.FromResultSet(rs));
 			}
 			rs.close();
 			stmt.close();
 			conn.close();
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 	}
 	
 	/*public List<JSONPacket> GetAllAirtimesForUser(User u){
 		List<JSONPacket> list = Lists.newArrayList();
 		Collections.sort(this.Timeline);
 		for(int i = 0; i < this.Timeline.size(); i++){
 			for(int j = 0; j < this.Timeline.get(i).Broadcasters.size(); j++){
 				if(this.Timeline.get(i).Broadcasters.get(j).Username.equalsIgnoreCase(u.Username)){
 					list.add(this.Timeline.get(i).toJsonMap());
 				}
 			}
 		}
 		return ImmutableList.copyOf(list);
 	}
 	*/
 	public List<JSONPacket> GetAirtimesJsonList(){
 		List<JSONPacket> list = Lists.newArrayList();
 		Collections.sort(this.Timeline);
 		for(Scheduled s : this.Timeline){
 			list.add(s.toJsonMap());
 		}
 		return ImmutableList.copyOf(list);
 	}
 	
 	@SuppressWarnings("unused")
 	private Date AddDaysToDate(Date date, int noOfDays){
 		Date newDate = new Date(date.getTime());
 		
 		GregorianCalendar calendar = new GregorianCalendar();
 		calendar.setTime(newDate);
 		calendar.add(Calendar.DATE, noOfDays);
 		newDate.setTime(calendar.getTime().getTime());
 		
 		return newDate;
 	}
 	
 	private Date AddSecondsToDate(Date date, long seconds){
 		Date newDate = new Date(date.getTime());
 		
 		GregorianCalendar calendar = new GregorianCalendar();
 		calendar.setTime(newDate);
 		calendar.add(Calendar.SECOND, (int) seconds);
 		newDate.setTime(calendar.getTime().getTime());
 		
 		return newDate;
 	}
 	
 	@EventListener(priority = EventPriority.HIGHEST)
 	public void OnMoveMultiple(SchedulerEvent.Move event){
 		for(int i = 0; i < event.Positions; i++){
 			SchedulerEvent.Move e = new SchedulerEvent.Move(event.Object, event.Direction, 1);
 			this.OnMoveSingle(e);
 			if(e.getResult() == EnumResult.DENY){
 				event.setResult(EnumResult.DENY);
 				return;
 			}else if(e.getResult() == EnumResult.ALLOW){
 				event.setResult(EnumResult.ALLOW);
 			}
 		}
 	}
 	
 	public void OnMoveSingle(SchedulerEvent.Move event){
 		if(event.Positions != 1) return;
 		synchronized(event.Object.UpdatingLock){
 			int index = event.Object.Index;
 			if(event.Direction == EnumDirection.UP){
 				if(this.Timeline.get(0).Type == EnumScheduledEventType.Header && index == 1) return;
 				if(index == 0) return;
 			}else{
 				if(event.Object.Type == EnumScheduledEventType.Header && event.Object.Index == 0) return;
 				if(index == this.Timeline.size() - 1) return;
 			}
 			
 			Scheduled Selected = event.Object;
 			Scheduled Affected = null;
 			for(int i = 0; i < Timeline.size(); i++){
 				if(Timeline.get(i).Index == Selected.Index + ((event.Direction == EnumDirection.UP) ? -1 : 1)){
 					Affected = Timeline.get(i);
 					break;
 				}
 			}
 			if(Affected == null) return;
 			
 			event.setResult(EnumResult.ALLOW);
 			
 			Selected.Index += ((event.Direction == EnumDirection.UP) ? -1 : 1);
 			Affected.Index += ((event.Direction == EnumDirection.UP) ? 1 : -1);
 			
 			int SmallestAffectedIndex = (Selected.Index < Affected.Index) ? Selected.Index : Affected.Index;
 			boolean ShouldUpdatePrevious = false;
 			if(SmallestAffectedIndex > 0){
 				for(int i = 0; i < Timeline.size(); i++){
 					if(Timeline.get(i).Index == SmallestAffectedIndex - 1){
 						if(Timeline.get(i).Type == EnumScheduledEventType.Header){
 							ShouldUpdatePrevious = true;
 						}
 					}
 				}
 			}
 			Collections.sort(this.Timeline);
 			
 			for(int i = SmallestAffectedIndex; i < Timeline.size(); i++){
 				Scheduled Prev = null;
 				if(i != 0) Prev = this.Timeline.get(i - 1);
 				
 				Scheduled Me = this.Timeline.get(i);
				Scheduled Next = null;
				if(i != this.Timeline.size() - 1) Next = this.Timeline.get(i + 1);
 				
 				if(i == SmallestAffectedIndex){
 					// First update!
 					// This is only called on the first object being updated
 					if(Me.ThrustartType == EnumThrustartType.Thrustart){
 						Scheduled o = Prev;
 						if(Prev.Type == EnumScheduledEventType.Header && i > 2){
 							o = this.Timeline.get(i - 2);
 						}
 						Me.Starttime = o.Endtime;
 						Me.Endtime = AddSecondsToDate(Me.Starttime, Me.Duration);
 						NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Me.ID, "Starttime", Me.Starttime)));
 						NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Me.ID, "Endtime", Me.Endtime)));
 					}
 					
 					if(ShouldUpdatePrevious && Prev.ThrustartType != EnumThrustartType.Time){
 						Prev.Starttime = Me.Starttime;
 						Prev.Endtime = Me.Starttime;
 						NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Prev.ID, "Starttime", Prev.Starttime)));
 						NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Prev.ID, "Endtime", Prev.Endtime)));
 					}
 				}
 				if(i > SmallestAffectedIndex){
 					// Just count
 					// This is called on all the other objects. Just count thru the time!
 					if(i == Selected.Index && Me.ThrustartType == EnumThrustartType.Time){
 						//If the moved object has an fixed starttime, don't update it!
 						continue;
 					}
 					if(Me.ThrustartType != EnumThrustartType.Thrustart && Me.Type != EnumScheduledEventType.Header){
 						//If we encounter an object with an fixed starttime, we can stop updating
 						break;
 					}
 					Me.Starttime = Prev.Endtime;
 					Me.Endtime = AddSecondsToDate(Me.Starttime, Me.Duration);
 					
 					NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Me.ID, "Starttime", Me.Starttime)));
 					NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Me.ID, "Endtime", Me.Endtime)));
 				}
 			}
 			
 			Selected.SaveChanges();
 			Affected.SaveChanges();
 		}
 	}
 	
 	public boolean HandleMetadataUpdate(Packet4SchedulerUpdate Update){
 		Scheduled object = Scheduled.FromOID(Update.OID);
 		synchronized(object.UpdatingLock){
 			if(Update.Field.equalsIgnoreCase("ThrustartType")){
 				try{
 					Update.Data = EnumThrustartType.FromID(Integer.valueOf(Update.Data.toString()));
 				}catch(NumberFormatException e){
 					Update.Data = EnumThrustartType.valueOf(Update.Data.toString());
 				}
 			}else if(Update.Field.equalsIgnoreCase("Type")){
 				try{
 					Update.Data = EnumScheduledEventType.FromID(Integer.valueOf(Update.Data.toString()));
 				}catch(NumberFormatException e){
 					Update.Data = EnumScheduledEventType.valueOf(Update.Data.toString());
 				}
 			}else if(Update.Field.equalsIgnoreCase("Starttime")){
 				if(object.ThrustartType == EnumThrustartType.Time){
 					Update.Data = ParseTimestamp(Update.Data);
 				}
 			}else if(Update.Field.equalsIgnoreCase("Endtime")){
 				if(object.ThrustartType == EnumThrustartType.Time){
 					Update.Data = ParseTimestamp(Update.Data);
 				}
 			}else if(Update.Field.equalsIgnoreCase("Duration")){
 				Update.Data = new Double(Update.Data.toString()).longValue();
 			}else if(Update.Field.equalsIgnoreCase("Broadcasters")){
 				Type ListType = new TypeToken<List<String>>(){}.getType();
 				List<String> StringList = Utils.Gson.fromJson(Update.Data.toString(), ListType);
 				List<User> UsersList = new ArrayList<User>();
 				for(String s : StringList){
 					UsersList.add(User.FromUsername(s));
 				}
 				Update.Data = UsersList;
 			}
 			if(NexusServer.EventBus.post(new SchedulerEvent.Update(Update))){
 				return false;
 			}
 			try{
 				Field UpdatedField = object.getClass().getDeclaredField(Update.Field);
 				UpdatedField.set(object, Update.Data);
 				object.SaveChanges();
 				
 				if(Update.Field.equalsIgnoreCase("Starttime")){
 					if(object.ThrustartType == EnumThrustartType.Time){
 						object.Endtime = AddSecondsToDate(object.Starttime, object.Duration);
 						NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(object.ID, "Endtime", object.Endtime)));
 						object.SaveChanges();
 						
 						for(int i = 1; i < Timeline.size(); i++){
 							if(Timeline.get(i - 1).Type == EnumScheduledEventType.Header && Timeline.get(i).ID == object.ID){
 								Timeline.get(i - 1).Starttime = object.Starttime;
 								Timeline.get(i - 1).Endtime = object.Starttime;
 								Timeline.get(i - 1).ResetTimings();
 								Timeline.get(i - 1).SaveChanges();
 								
 								NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Timeline.get(i - 1).ID, "Starttime", Timeline.get(i - 1).Starttime)));
 								NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Timeline.get(i - 1).ID, "Endtime", Timeline.get(i - 1).Endtime)));
 							}
 						}
 					}
 				}else if(Update.Field.equalsIgnoreCase("Endtime")){
 					if(object.ThrustartType == EnumThrustartType.Time){
 						object.Duration = (int) (object.Endtime.getTime() - object.Starttime.getTime()) / 1000;
 						NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(object.ID, "Duration", object.Duration)));
 						object.SaveChanges();
 						
 						boolean Read = false;
 						for(int i = 0; i < Timeline.size(); i++){
 							if(Read && Timeline.get(i).ThrustartType == EnumThrustartType.Time){
 								Read = false;
 							}
 							if(Read){
 								Timeline.get(i).Starttime = Timeline.get(i - 1).Endtime;
 								Timeline.get(i).Endtime = AddSecondsToDate(Timeline.get(i).Starttime, Timeline.get(i).Duration);
 								Timeline.get(i).ResetTimings();
 								Timeline.get(i).SaveChanges();
 								
 								NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Timeline.get(i).ID, "Starttime", Timeline.get(i).Starttime)));
 								NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Timeline.get(i).ID, "Endtime", Timeline.get(i).Endtime)));
 							}
 							if(Timeline.get(i).ID == object.ID){
 								Read = true;
 							}
 						}
 					}
 				}else if(Update.Field.equalsIgnoreCase("Duration")){
 					object.Endtime = AddSecondsToDate(object.Starttime, object.Duration);
 					NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(object.ID, "Endtime", object.Endtime)));
 					object.SaveChanges();
 					
 					boolean Read = false;
 					for(int i = 0; i < Timeline.size(); i++){
 						if(Read && Timeline.get(i).ThrustartType == EnumThrustartType.Time){
 							Read = false;
 						}
 						if(Read){
 							Timeline.get(i).Starttime = Timeline.get(i - 1).Endtime;
 							Timeline.get(i).Endtime = AddSecondsToDate(Timeline.get(i).Starttime, Timeline.get(i).Duration);
 							Timeline.get(i).ResetTimings();
 							Timeline.get(i).SaveChanges();
 							
 							NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Timeline.get(i).ID, "Starttime", Timeline.get(i).Starttime)));
 							NexusServer.EventBus.post(new SchedulerEvent.Update(new Packet4SchedulerUpdate(Timeline.get(i).ID, "Endtime", Timeline.get(i).Endtime)));
 						}
 						if(Timeline.get(i).ID == object.ID){
 							Read = true;
 						}
 					}
 				}
 			}catch(Exception e){
 				this.Log.log(Level.WARNING, "Metadata update error", e);
 				return false;
 			}
 			return true;
 		}
 	}
 	
 	public Timestamp ParseTimestamp(Object input){
 		Double obj = new Double(input.toString());
 		return new Timestamp(obj.longValue());
 	}
 	
 	public Scheduled InsertObject(Scheduled object){
 		try{
 			Connection conn = MySQLHelper.GetConnection();
 			Statement stmt = conn.createStatement();
 			stmt.execute(String.format("INSERT INTO %s(Description) VALUES('')", TableList.TABLE_TIMELINE));
 			stmt.close();
 			Statement stmt2 = conn.createStatement();
 			ResultSet rs = stmt2.executeQuery(String.format("SELECT ID FROM %s ORDER BY ID DESC LIMIT 1", TableList.TABLE_TIMELINE));
 			rs.first();
 			int ID = rs.getInt("ID");
 			int NewIndex = 0;
 			if(Timeline.size() > 0){
 				NewIndex = Timeline.get(Timeline.size() - 1).Index + 1;
 			}
 			object.Index = NewIndex;
 			object.ID = ID;
 			object.SaveChanges();
 			
 			rs.close();
 			stmt2.close();
 			conn.close();
 			
 			boolean FirstObject = Timeline.size() == 0;
 			if(!FirstObject){
 				int LastIndex = Timeline.size() - 1;
 				Scheduled Last = Timeline.get(LastIndex);
 				object.Starttime = Last.Endtime;
 				object.Endtime = AddSecondsToDate(object.Starttime, object.Duration);
 				object.ResetTimings();
 				object.SaveChanges();
 			}
 			
 			Timeline.add(object);
 			NexusServer.EventBus.post(new SchedulerAppendObjectEvent.Post(object));
 		}catch(SQLException e){
 			e.printStackTrace();
 			return null;
 		}
 		return object;
 	}
 }
