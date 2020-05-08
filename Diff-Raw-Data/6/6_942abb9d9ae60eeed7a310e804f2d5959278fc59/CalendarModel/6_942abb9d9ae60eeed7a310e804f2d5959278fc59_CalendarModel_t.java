 package client.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import client.IServerResponseListener;
 import client.ServerConnection;
 
 public class CalendarModel implements IServerResponseListener{
 	
 	private PropertyChangeSupport pcs;
 
 	//These essentially hold a buffer of meetings
 	protected final Set<MeetingModel> 						meetings;
 	protected final TreeMap<Calendar, Set<MeetingModel>> 	meetingsFrom,
 															meetingsTo;
 	
 	public final static String	MEETING_ADDED = "MEETING_ADDED";
 	public final static String  MEETING_REMOVED = "MEETING REMOVE";
 	
 	private final UserModel owner;
 	
 	private int meetingsReq;
 	
 	public CalendarModel(UserModel owner) {
 		
 		this.owner = owner;
 		
 		meetings = new HashSet<MeetingModel>();
 		meetingsFrom = new TreeMap<Calendar, Set<MeetingModel>>();
 		meetingsTo = new TreeMap<Calendar, Set<MeetingModel>>();
 		
 		
 		
 		pcs = new PropertyChangeSupport(this);
 		
 		//Requests a chuck of meetings from the server
 		Calendar 	from = Calendar.getInstance(),
 					to   = Calendar.getInstance();
 		
 		from.roll(Calendar.MONTH, -1);
 		to  .roll(Calendar.MONTH,  1);
 		from.set(Calendar.DAY_OF_MONTH, 1);
 		to.set(Calendar.DAY_OF_MONTH, to.getActualMaximum(Calendar.DAY_OF_MONTH));
 		
 		meetingsReq = ServerConnection.instance().requestMeetings(this, new UserModel[]{owner}, from, to);
 	}
 	
 	private CalendarModel add(MeetingModel meeting) {
 		if (meetings.contains(meeting)) return this;
 		
 		meetings.add(meeting);
 		if (!meetingsFrom.containsKey(meeting.getTimeFrom()))
 			meetingsFrom.put(meeting.getTimeFrom(), new HashSet<MeetingModel>());
 		
 		if (!meetingsTo.containsKey(meeting.getTimeTo()))
 			meetingsTo.put(meeting.getTimeTo(), new HashSet<MeetingModel>());
 		
 		meetingsFrom.get(meeting.getTimeTo()).add(meeting);
 		meetingsTo.get(meeting.getTimeTo()).add(meeting);
 
 		pcs.firePropertyChange(MEETING_ADDED, null, meeting);
 		
 		return this;
 	}
 	
 	private CalendarModel addAll(Collection<MeetingModel> c) {
 		for (MeetingModel mm : c)
 			add(mm);
 		
 		return this;
 	}
 	
 	private CalendarModel remove(MeetingModel meeting) {
		if (!meetings.contains(meeting)) return this;
 		
 		meetings.remove(meeting);
 		meetingsFrom.get(meeting.getTimeFrom()).remove(meeting);
 		meetingsTo.get(meeting.getTimeTo()).remove(meeting);
 		
 		pcs.firePropertyChange(MEETING_REMOVED, null, meeting);
 		
 		return this;
 	}
 	
 	private CalendarModel removeAll(Collection<MeetingModel> c) {
 		for (MeetingModel mm : c)
 			remove(mm);
 		
 		return this;
 	}
 	
 	public boolean contains(MeetingModel meeting) {
 		return meetings.contains(meeting);
 	}
 	
 	/**
 	 * Returns meetings within a week, from Monday 00:00:00 to Sunday 23:59:59
 	 * @param date a date within the week you wish to get meetings from
 	 * @return
 	 */
 	public Set<MeetingModel> getMeetingsInWeek(Calendar date) {
 		Calendar fromTime = (Calendar)date.clone(),
 				   toTime = (Calendar)date.clone();
 		
 		fromTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 		toTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
 		
 		fromTime.set(Calendar.HOUR_OF_DAY, 0);
 		fromTime.set(Calendar.MINUTE, 0);
 		fromTime.set(Calendar.SECOND, 0);
 		
 		fromTime.set(Calendar.HOUR_OF_DAY, 23);
 		fromTime.set(Calendar.MINUTE, 59);
 		fromTime.set(Calendar.SECOND, 59);
 		
 		
 		return getMeetingInterval(fromTime, toTime,true);
 	}
 	
 	/**
 	 * @param fromTime Calendar object representing the start of the interval
 	 * @param toTime Calendar object representing the end of the interval
 	 * @return
 	 */
 	public Set<MeetingModel> getMeetingInterval(Calendar fromTime, Calendar toTime) {
 		return getMeetingInterval(fromTime, toTime, false);
 	}
 	
 	/**
 	 * @param fromTime Calendar object representing the start of the interval
 	 * @param toTime Calendar object representing the end of the interval
 	 * @param tight boolean value. true returns only meetings that overlap fully with the timeinterval, false returns all meetings that overlaps to some degree
 	 * @return Set with all the meetings within the given interval
 	 */
 	public Set<MeetingModel> getMeetingInterval(Calendar fromTime, Calendar toTime, boolean tight) {
 		Set<MeetingModel> returnSet;
 		
 		
 		Set<MeetingModel> fromSet = new HashSet<MeetingModel>();
 		for (Map.Entry<Calendar, Set<MeetingModel>> entry : meetingsFrom.subMap(fromTime, true, toTime, true).entrySet()) {
 			fromSet.addAll(entry.getValue());
 		}
 		
 		Set<MeetingModel> toSet = new HashSet<MeetingModel>();
 		for (Map.Entry<Calendar, Set<MeetingModel>> entry : meetingsTo.subMap(fromTime, true, toTime, true).entrySet()) {
 			fromSet.addAll(entry.getValue());
 		}
 		
 		
 		if (tight) {
 			//This should represent the set operation returnSet = fromSet (CUT) toSet
 			//i.e Meetings that start after the given fromTime, and end before the given toTime
 			returnSet = fromSet;
 			returnSet.retainAll(toSet);
 		} else {
 			//This should represent the set operation returnSet = fromSet (UNION) toSet
 			//i.e Meetings where some of the meeting time is happening within the fromTime and toTime
 			returnSet = fromSet;
 			returnSet.addAll(toSet);
 		}
 		
 		
 		return returnSet;
 	}
 	
 	public int size() {
 		return meetings.size();
 	}
 	
 	public void addPropertyChangeListner(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 
 	@Override
 	public void onServerResponse(int requestId, Object data) {
 		if (requestId == meetingsReq) {
 			List<MeetingModel> models = (List<MeetingModel>) data;
 			
 		}
 	}
 	
 }
