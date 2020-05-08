 package com.vzw.hackathon;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.dbutils.handlers.ScalarHandler;
 import org.apache.log4j.Logger;
 
 import com.vzw.hackathon.apihandler.ComcastAPIHandler;
 import com.vzw.util.db.DBManager;
 import com.vzw.util.db.DBPool;
 import com.vzw.util.db.DBUtil;
 
 public class GroupEventManager {
 	private static final Logger	logger = Logger.getLogger(GroupEventManager.class);
 	private  DBPool dbPool = null;
 	
 	private static final String SQL_SEL_GROUP_EVENT = 
 			"select group_event_id as id, show_id as showId, channel_id as channelId, show_time as showTime, "
 			+ " show_name as showName, master_mdn as masterMdn, create_time as createTime"
 			+ " from GROUP_EVENT where group_event_id = ?";	
 	
 	private static final String SQL_GET_NEXT_GROUP_EVENT_ID = 
 			"VALUES (NEXT VALUE FOR GROUP_EVENT_PK_SEQ)";
 	
 	private static final String SQL_CREATE_GROUP_EVENT = 
 			"INSERT INTO GROUP_EVENT (GROUP_EVENT_ID, SHOW_ID, CHANNEL_ID, SHOW_TIME, SHOW_NAME, MASTER_MDN"
 			+ ") VALUES ("
 			+ "	?, ?, ?, ?, ?, ?)";
 
 	private static final String SQL_ADD_GROUP_MEMBER =
 			"INSERT INTO GROUP_MEMBER ("
 			+ "	GROUP_EVENT_ID,	MDN, MEMBER_STATUS,	MEMBER_NAME) VALUES (?, ?, 'INVITED', ?)";
 	
 	
 	private static final String SQL_UPDATE_MEMBER_STATUS =
 			"update GROUP_MEMBER set MEMBER_STATUS = ?, DEVICE_ID = ? where GROUP_EVENT_ID = ? and MDN = ?";
 	
 	
 	
 	
 	private ScheduledExecutorService		scheduler = null;
 	
 	// key: groupEventId + mdn
 	private Set<String>						scheduledEvent = new HashSet<String>();
 	
 	
 	private static GroupEventManager instance = null;
 	
 	static {
 		instance = new GroupEventManager();
 	}
 	
 	public static GroupEventManager getInstance() {
 		return instance;
 	}
 	private GroupEventManager() {
 		dbPool = DBManager.getDBPool();
 		scheduler = Executors.newScheduledThreadPool(20);
 	}
 	
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public GroupEvent loadGroupEventFromDb(int id) {
 		GroupEvent ge = null;
 		try {
 			ge = DBUtil.query(dbPool, SQL_SEL_GROUP_EVENT, new DBUtil.BeanHandlerEx<GroupEvent>(GroupEvent.class), DBUtil.THROW_HANDLER, id);
 		}
 		catch (Exception e) {
 			logger.error("Failed to load group event from db", e);
 		}
 		
 		return ge;
 	}
 	
 	/**
 	 * 
 	 * @param ge
 	 */
 	public void createGroupEvent(GroupEvent ge) {
 		try {
 			// get group event id
 			int geId = DBUtil.query(
 					dbPool, 
 					SQL_GET_NEXT_GROUP_EVENT_ID,
 					new ScalarHandler<Integer>(),
 					DBUtil.THROW_HANDLER
 					);
 			
 			// insert group event
 			DBUtil.update(dbPool, SQL_CREATE_GROUP_EVENT, DBUtil.THROW_HANDLER, 
 					geId, ge.getShowId(), ge.getChannelId(), 
 					new Timestamp(ge.getShowTime().getTime()), ge.getShowName(), ge.getMasterMdn());
 			
 			// insert members (including master mdn)
 			for (Member m : ge.getMemberList()) {
 				DBUtil.update(dbPool, SQL_ADD_GROUP_MEMBER, DBUtil.THROW_HANDLER, 
 						geId, m.getMdn(), m.getName());
 			}
 		}
 		catch (Exception e) {
 			logger.error("Failed to create group event.", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param groupEventId
 	 * @param mdn
 	 * @param status
 	 */
 	public void updateMemberStatus(int groupEventId, String mdn, MemberStatus status) {
 		try {
 			
 			String deviceId = null;
 			switch (status) {
 				case ACCEPTED:
 					deviceId = ComcastAPIHandler.getDeviceId(mdn);
 					break;
 					
 				default:
 					break;
 			}
 			
 			DBUtil.update(dbPool, SQL_UPDATE_MEMBER_STATUS, DBUtil.THROW_HANDLER, 
 					status.name(), deviceId, groupEventId, mdn);
 			
 			if (deviceId != null) {
 				// schedule a play
 				schedulePlay(mdn, deviceId, groupEventId);
 			}
 		}
 		catch (Exception e) {
 			logger.error("Failed to update member status");
 		}
 	}
 
 
 	
 	/**
 	 * schedule tuning a show
 	 * @param mdn
 	 * 
 	 */
 	public void schedulePlay(String mdn, final String deviceId, int groupEventId) {
 		final GroupEvent ge = loadGroupEventFromDb(groupEventId);
 		if (ge != null) {
 			String key = ge.getId() + mdn;
 			if (!scheduledEvent.contains(key)) {
 			
 				Date now = new Date();
 				long delay = ge.getShowTime().getTime() - now.getTime();
 				
 				if (delay < 0) {
 					// tune right away
 					ComcastAPIHandler.tuneChannel(deviceId, ge.getChannelId());
 				}
 				else {
 					//schedule it
 				}
 				
 				scheduler.schedule(new Runnable() {
 					
 					@Override
 					public void run() {
 						ComcastAPIHandler.tuneChannel(deviceId, ge.getChannelId());
 						
 					}
 				}, delay, TimeUnit.MILLISECONDS);
 				
 				scheduledEvent.add(key);
 			}
 		}
 		
 	}
 		
 }
