 package lv.lu.meetings.domain.jpa;
 
 import lv.lu.meetings.domain.jpa.meeting.Attendance;
 import lv.lu.meetings.domain.jpa.meeting.Concert;
 import lv.lu.meetings.domain.jpa.meeting.Invite;
 import lv.lu.meetings.domain.jpa.meeting.Meeting;
 import lv.lu.meetings.domain.jpa.meeting.Party;
 import lv.lu.meetings.domain.jpa.meeting.SportEvent;
 import lv.lu.meetings.domain.jpa.venue.Category;
 import lv.lu.meetings.domain.jpa.venue.Venue;
 import lv.lu.meetings.impl.CommonJpaDAOImpl;
import lv.lu.meetings.impl.CommonJpaDAOImplTest;
 
 /**
  * This is a registry of persistent (in scope SQL database) objects in a system.<br>
  * It is used in:
  * <li>{@link CommonJpaDAOImpl#cleanupDB()} to delete everything from DB</li>
 * <li>{@link CommonJpaDAOImplTest} to create instances of persistent objects</li>
  */
 public enum PersistentEntityType
 {
     ATTENDANCE(Attendance.class, null),
     INVITE(Invite.class, null),
     @SuppressWarnings("unchecked")
     MEETING(Meeting.class, new Class[]{Party.class, Concert.class, SportEvent.class}),
     VENUE(Venue.class, null),
     CATEGORY(Category.class, null),
     USER(User.class, null);	
 	
 	PersistentEntityType(Class<? extends PersistentEntity> clazz, Class<? extends PersistentEntity>[] subClasses){
 		this.clazz = clazz;
 		this.subClasses = subClasses;
 	}
 	
 	private Class<? extends PersistentEntity> clazz;
 	
 	public Class<? extends PersistentEntity> getObjectClass(){
 		return this.clazz;
 	}
 	
 	private Class<? extends PersistentEntity>[] subClasses;
 	
 	public Class<? extends PersistentEntity>[] getSubClasses() {
 		return subClasses;
 	}
 }
