 package com.vzw.hackthon.scheduler;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.dbutils.handlers.ArrayListHandler;
 import org.apache.log4j.Logger;
 
 import com.vzw.hackathon.GroupEvent;
 import com.vzw.hackathon.apihandler.VZWAPIHandler;
 import com.vzw.util.db.DBManager;
 import com.vzw.util.db.DBPool;
 import com.vzw.util.db.DBUtil;
 
 public class EventReminder implements Runnable {
 	
 	private static final Logger		logger = Logger.getLogger(EventReminder.class);
 
 	private static final DBPool 	dbPool = DBManager.getDBPool();
 	
 	private static final int CHECK_INTERVAL_SECONDS		= 5;
 	private static final int REMINDER_MINUTES			= 2;		// when to send reminder
 	
 	
 	private static final String SQL_SEL_EVENTS_FOR_REMINDER = 
 			"select group_event_id as id, show_id as showId, channel_id as channelId, show_time as showTime, "
 			+ " show_name as showName, master_mdn as masterMdn, create_time as createTime"
 			+ " from GROUP_EVENT"
 			+ " where reminder_sent = 0 and"
 			+ " show_time between CURRENT_TIMESTAMP AND {fn TIMESTAMPADD(SQL_TSI_MINUTE, ?, CURRENT_TIMESTAMP)}";
 	
 	
 	private static final String SQL_SEL_MEMBER_FOR_REMINDER = 
 			"select mdn from group_member where group_event_id = > and MEMBER_STATUS = 'ACCEPTED' or MEMBER_STATUS = 'MASTER'";
 	
 	private static final String SQL_FLAG_REMINDER_SENT =
 			"update group_event set reminder_sent = 1 where group_event_id = ?";
 	
 	private ScheduledExecutorService		executor = null;
 	
 	public EventReminder() {
 	}
 	
 	public void start() {
 		// check the database every 10 seconds
 		executor = Executors.newScheduledThreadPool(10);
 		executor.scheduleAtFixedRate(this, 10, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
 		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (executor != null) {
					executor.shutdown();
				}
			}
		});
		
 	}
 
 	/**
 	 * Get group events (list of group events)
 	 * @return
 	 */
 	public List<GroupEvent> getGroupEventsForReminder() {
 		
 		List<GroupEvent> geList = null;
 		
 		try {
 			
 			// go over the events the show time of which is not REMINDER_MINUTES more minutes than now.
 			geList = DBUtil.query(dbPool, SQL_SEL_EVENTS_FOR_REMINDER, 
 					new DBUtil.BeanListHandlerEx<GroupEvent>(GroupEvent.class), DBUtil.THROW_HANDLER, -REMINDER_MINUTES);
 			
 		}
 		catch (Exception e) {
 			logger.error("Failed to get group events reminder", e);
 		}
 		finally {
 		}
 		
 		
 		return geList;
 		
 	}
 	
 	/**
 	 * 
 	 * @param groupEventId
 	 * @return
 	 */
 	public List<String> getMemberMdnForReminder(int groupEventId) {
 		List<String> mdnList = null;
 		try {
 			
 			// go over the events the show time of which is not REMINDER_MINUTES more minutes than now.
 			List<Object[]> l1 = DBUtil.query(dbPool, SQL_SEL_MEMBER_FOR_REMINDER, 
 					new ArrayListHandler(), DBUtil.THROW_HANDLER, groupEventId);
 			
 			if (! CollectionUtils.isEmpty(l1)) {
 				mdnList = new ArrayList<String>();
 				for (Object[] oa : l1) {
 					mdnList.add((String)oa[0]);
 				}
 				
 			}
 			
 		}
 		catch (Exception e) {
 			logger.error("Failed to get group events members", e);
 		}
 		finally {
 		}
 		
 		
 		return mdnList;	
 	}
 	
 	/**
 	 * 
 	 */
 	public void sendReminders() {
 		
 		List<GroupEvent> geList = getGroupEventsForReminder();
 		
 		if (!CollectionUtils.isEmpty(geList)) {
 			for (GroupEvent ge : geList) {
 				List<String> mdnList = getMemberMdnForReminder(ge.getId());
 				if (!CollectionUtils.isEmpty(mdnList)) {
 					
 					String msg = buildReminderString(ge);
 					
 					// send to the client
 					//MessagingAPIHandler.sendSMS(mdnList, msg);
 					VZWAPIHandler.sendSMS(mdnList, msg);
 					
 					flagReminderSent(ge.getId());
 				}
 			}
 		}
 	}
 	
 	private void flagReminderSent(int geId) {
 		try {
 			DBUtil.update(dbPool, SQL_FLAG_REMINDER_SENT, DBUtil.THROW_HANDLER, geId);
 		}
 		catch (Exception e) {
 			logger.error("Unable flag reminder sent for geId=" + geId, e);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param ge
 	 * @return
 	 */
 	public String buildReminderString(GroupEvent ge) {
 		return MessageFormat.format("MNREMINDER##{0}##{1,time,yyyy-MM-dd HH:mm}##{2}", 
 				ge.getChannelId(), ge.getShowTime(), ge.getShowName());
 	}
 
 	@Override
 	public void run() {
 		sendReminders();
 		
 	}
 
 }
