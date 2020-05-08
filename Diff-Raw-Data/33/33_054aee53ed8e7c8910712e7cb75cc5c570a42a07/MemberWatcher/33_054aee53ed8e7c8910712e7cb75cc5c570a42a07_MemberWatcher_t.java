 package com.vzw.hackthon.scheduler;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.dbutils.handlers.ArrayListHandler;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.vzw.hackathon.GroupEvent;
 import com.vzw.hackathon.GroupEventManager;
 import com.vzw.hackathon.Member;
 import com.vzw.hackathon.apihandler.ComcastAPIHandler;
 import com.vzw.hackathon.apihandler.SendVMAMessage;
 import com.vzw.hackathon.apihandler.VZWAPIHandler;
 import com.vzw.util.db.DBManager;
 import com.vzw.util.db.DBPool;
 import com.vzw.util.db.DBUtil;
 
 public class MemberWatcher implements Runnable {
 	private static final Logger		logger = Logger.getLogger(EventReminder.class);
 
 	private static final DBPool 	dbPool = DBManager.getDBPool();
 	
 	private static final int CHECK_INTERVAL_SECONDS		= 10;
 	
 	
 	private static final String SQL_SEL_MEMBERS = 
 			"select group_event_id, mdn, device_id, member_name"
 			+ " from GROUP_MEMBER"
 			+ " where member_status = 'MASTER' or member_status = 'ACCEPTED"
 			+ " order by GROUP_EVENT_ID, mdn";
 	
 		
 	private ScheduledExecutorService		executor = null;
 	
 
 	
 	private MemberWatcher() {
 		
 		// check the database every 10 seconds
 		executor = Executors.newScheduledThreadPool(10);
 		executor.scheduleAtFixedRate(this, 10, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
 		
 	}
 
 	
 	
 	private void watchMembers() {
 		// get all the events first
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		
 		try {
 			
			conn = dbPool.getConnection();
 			ps = conn.prepareStatement(SQL_SEL_MEMBERS);
 			rs = ps.executeQuery();
 			
 			
 			
 			int lastGeId = -1;
 			int curGeId = -1;
 			GroupEvent ge = null;
 			List<Member> memberList = null;
 			while (rs.next()) {
 				curGeId = rs.getInt("group_event_id");
 				
 				if (curGeId != lastGeId) {
 					// new group event
 					if (ge != null) {
 						processWatch(ge);
 					}
 					
 					ge = GroupEventManager.getInstance().loadGroupEventFromDb(curGeId);
 					memberList = new ArrayList<Member>();
 					ge.setMemberList(memberList);
 					
 					
 				}
 				
 				Member member = new Member();
 				member.setDeviceId(rs.getString("device_id"));
 				member.setMdn(rs.getString("mdn"));
 				member.setName(rs.getString("member_name"));
 				memberList.add(member);
 					
 			}
 		}
 		catch (Exception e) {
 			logger.error("Failed to watch members", e);
 		}
 		finally {
 			DBManager.release(rs, ps, conn);
 		}
 	}
 	
 	private void processWatch(GroupEvent ge) {
 		String[] tos = ge.getMemberMdns();
 		String toStr = StringUtils.join(tos, ",");
 		for (Member m : ge.getMemberList()) {
 			String channelId = ComcastAPIHandler.getChannelId(m.getDeviceId());
 			if (!StringUtils.equals(channelId, ge.getChannelId())) {
 				// need to send the group message of this change
 				SendVMAMessage.sendMMS(m.getMdn(), toStr, m.getName() + " changed channel to " + channelId);
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		watchMembers();
 		
 	}
 }
