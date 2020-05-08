 /*
  * Copyright by Apaq 2011-2013
  */
 package dk.drb.blacktiger.service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 import dk.drb.blacktiger.model.Participant;
 import dk.drb.blacktiger.model.User;
 import org.asteriskjava.live.AsteriskServer;
 import org.asteriskjava.live.MeetMeRoom;
 import org.asteriskjava.live.MeetMeUser;
 import org.asteriskjava.manager.ManagerEventListener;
 import org.asteriskjava.manager.event.ManagerEvent;
 import org.asteriskjava.manager.event.MeetMeJoinEvent;
 import org.asteriskjava.manager.event.MeetMeLeaveEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  * Javadoc
  */
 public class BlackTigerService implements IBlackTigerService {
 
     private static final Logger LOG = LoggerFactory.getLogger(BlackTigerService.class);
     private AsteriskServer asteriskServer;
     private JdbcTemplate jdbcTemplate;
     private UserMapper userMapper = new UserMapper();
     private List<BlackTigerEventListener> eventListeners = new ArrayList<BlackTigerEventListener>();
     private ManagerEventListener managerEventListener = new ManagerEventListener() {
         @Override
         public void onManagerEvent(ManagerEvent event) {
             if (event instanceof MeetMeJoinEvent) {
                 String roomNo = ((MeetMeJoinEvent) event).getMeetMe();
                 Integer index = ((MeetMeJoinEvent) event).getUserNum();
                 fireEvent(new ParticipantJoinEvent(roomNo, index.toString()));
             }
 
             if (event instanceof MeetMeLeaveEvent) {
                 String roomNo = ((MeetMeLeaveEvent) event).getMeetMe();
                 Integer index = ((MeetMeLeaveEvent) event).getUserNum();
                 fireEvent(new ParticipantLeaveEvent(roomNo, index.toString()));
             }
         }
     };
 
     private class UserMapper implements RowMapper {
 
         @Override
         public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
             return new User(rs.getString("id"), rs.getString("data"));
         }
     }
 
     private void fireEvent(ParticipantEvent event) {
         for (BlackTigerEventListener listener : eventListeners) {
             listener.onParticipantEvent(event);
         }
     }
 
     public void setAsteriskServer(AsteriskServer asteriskServer) {
         if (this.asteriskServer != null) {
             this.asteriskServer.getManagerConnection().removeEventListener(managerEventListener);
         }
         this.asteriskServer = asteriskServer;
         this.asteriskServer.getManagerConnection().addEventListener(managerEventListener);
     }
 
     public void setDataSource(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     public User getUser(String username) {
         return (User) this.jdbcTemplate.queryForObject("select * from sip where id=? and keyword like 'secret'",
                 new Object[]{username}, userMapper);
     }
 
     @Override
     public List<Participant> listParticipants(String roomNo) {
         LOG.debug("Listing participants. [room={}]", roomNo);
         checkRoomAccess(roomNo);
         MeetMeRoom room = asteriskServer.getMeetMeRoom(roomNo);
         List<Participant> result = new ArrayList<Participant>();
 
         for (MeetMeUser mmu : room.getUsers()) {
             result.add(participantFromMeetMeUser(mmu));
         }
         return result;
     }
 
     @Override
     public Participant getParticipant(String roomNo, String participantId) {
         LOG.debug("Retrieving participant. [room={};participant={}]", roomNo, participantId);
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         return mmu == null ? null : participantFromMeetMeUser(mmu);
     }
 
     @Override
     public void kickParticipant(String roomNo, String participantId) {
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.kick();
         }
     }
 
     @Override
     public void muteParticipant(String roomNo, String participantId) {
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.mute();
         }
     }
 
     @Override
     public void unmuteParticipant(String roomNo, String participantId) {
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.unmute();
         }
     }
 
     private MeetMeUser getMeetMeUser(String roomNo, String participantId) {
         MeetMeRoom room = asteriskServer.getMeetMeRoom(roomNo);
         Integer id = Integer.parseInt(participantId);
         for (MeetMeUser mmu : room.getUsers()) {
            if (mmu.getUserNumber() == id) {
                 return mmu;
             }
         }
         return null;
     }
 
     private Participant participantFromMeetMeUser(MeetMeUser user) {
         return new Participant(user.getUserNumber().toString(),
                 user.getChannel().getCallerId().getNumber(),
                user.isMuted());
     }
 
     @Override
     public void addEventListener(BlackTigerEventListener listener) {
         if (listener != null) {
             eventListeners.add(listener);
         }
     }
 
     @Override
     public void removeEventListener(BlackTigerEventListener listener) {
         if (listener != null) {
             eventListeners.remove(listener);
         }
     }
     
     private void checkRoomAccess(String roomNo) {
         if(!hasRole("ROOMACCESS_" + roomNo)) {
             throw new SecurityException("Not authorized to access room " + roomNo);
         }
     }
     private boolean hasRole(String role) {
         LOG.debug("Checking if current user has role '{}'", role);
         
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         if(auth!=null && auth.isAuthenticated()) {
             for(GrantedAuthority ga : auth.getAuthorities()) {
                 if(ga.getAuthority().startsWith("ROLE_") && ga.getAuthority().substring(5).equals(role)) {
                     return true;
                 }
             }
         }
         LOG.debug("User does not have role. [auth={}]", auth);
         return false;
     }
 }
