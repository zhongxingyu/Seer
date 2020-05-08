 package client.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Comparator;
 
 import server.ModelEnvelope;
 import client.ClientMain;
 import client.ModelCacher;
 import client.ServerConnection;
 
 /**
  * A model for the meetings in the calendar
  * 
  * @author peterringset
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public class MeetingModel implements TransferableModel {
 	
 	public final static String TIME_FROM_PROPERTY = "timeFrom";
     public final static String TIME_TO_PROPERTY = "timeTo";
     public final static String NAME_PROPERTY = "name";
     public final static String ROOM_PROPERTY = "room";
     public final static String LOCATION_PROPERTY = "location";
     public final static String DESCRIPTION_PROPERTY = "description";
     public final static String ACTIVE_PROPERTY = "active";
     public final static String INVITATION_CREATED = "invitation created";
     public final static String INVITATION_REMOVED = "invitation removed";
 
 	protected int id;
 	protected Calendar timeFrom, timeTo;
 	protected String name, description, location;
 	protected MeetingRoomModel room;
 	protected boolean active;
 	protected UserModel owner;	
 	private PropertyChangeSupport changeSupport;
 	protected ArrayList<InvitationModel> invitations;
 	
 	/**
 	 * Protected constructor
 	 */
 	public MeetingModel() {
 		changeSupport = new PropertyChangeSupport(this);
 		id = -1;
 		name = location = description = "";
 		invitations = new ArrayList<InvitationModel>();	
 		active = true;
 	}
 	
 	/**
 	 * Construct a new meeting model
 	 * Note that timeTo should be after timeFrom
 	 * 
 	 * @param timeFrom
 	 * @param timeTo
 	 * @param owner
 	 * @throws IllegalArgumentException if timeFrom is after timeTo
 	 */
 	public MeetingModel(Calendar timeFrom, Calendar timeTo, UserModel owner) {
 		this();
 		
 		this.timeFrom = timeFrom;
 		this.timeTo = timeTo;
 		this.owner = owner;
 		if(!timeFrom.before(timeTo)) {
 			throw new IllegalArgumentException("MeetingModel: From-time is after to-time");
 		}
 	}
 	
 	/**
 	 * Creates a default meeting model
 	 */
 	public static MeetingModel newDefaultInstance(Calendar startTime) {
 		Calendar timeFrom = (Calendar)startTime.clone(),
 				 timeTo   = (Calendar)startTime.clone();
 		
 		timeFrom.set(Calendar.SECOND, 0);
 		timeFrom.set(Calendar.SECOND, 0);
 		
 		if (timeFrom.get(Calendar.HOUR_OF_DAY) < 23)
 			timeTo.set(Calendar.HOUR_OF_DAY, timeFrom.get(Calendar.HOUR_OF_DAY) + 1);
 		else {
 			timeTo.set(Calendar.HOUR_OF_DAY, 23);
 			timeTo.set(Calendar.MINUTE, 59);
 		}
 		
 		UserModel owner = ClientMain.getActiveUser();
 		
 		return new MeetingModel(timeFrom, timeTo, owner);
 	}
 	
 	/**
 	 * Construct MeetingModel from stream
 	 * 
 	 * @param reader
 	 * @param modelBuff
 	 * @throws IOException
 	 */
 	public MeetingModel(BufferedReader reader) throws IOException {
 		this();
 		
 		id = Integer.parseInt(reader.readLine());
 		name = reader.readLine();
 		String l;
 		while(!(l = reader.readLine()).equals("\0")) 
 			description += l+"\r\n";
 		
		DateFormat df = DateFormat.getDateTimeInstance();
 
 		timeFrom = Calendar.getInstance();
 		timeTo = Calendar.getInstance();
 		try {
 			timeFrom.setTime(df.parse(reader.readLine()));
 			timeTo.setTime(df.parse(reader.readLine()));
 		} catch (ParseException e) {
 			throw new IOException(e.toString());
 		}
 		
 		location = reader.readLine();
 		owner_umid = reader.readLine();		
 		room_umid = reader.readLine();
 		
 		int invitations = Integer.parseInt(reader.readLine());
 		invitation_umids = new String[invitations];
 		for( ; invitations > 0; invitations--) {
 			invitation_umids[invitations-1] = reader.readLine();
 		}
 	}
 	
 	private String owner_umid;
 	private String room_umid;
 	private String[] invitation_umids;
 	
 	@Override
 	public void registerSubModels(ModelEnvelope envelope) {
 		owner = (UserModel) envelope.getFromBuffer(owner_umid);
 		if(!room_umid.equals("")) {
 			room = (MeetingRoomModel) envelope.getFromBuffer(room_umid);
 		}
 		for(String s : invitation_umids) {
 			invitations.add((InvitationModel) envelope.getFromBuffer(s));
 		}
 	}
 	
 
 	@Override
 	public void copyFrom(TransferableModel source) {
 		MeetingModel n = (MeetingModel) source;
 		
 		setName(n.getName());
 		setTimeFrom(n.getTimeFrom());
 		setTimeTo(n.getTimeTo());
 		setLocation(n.getLocation());
 		setRoom(n.getRoom());
 		setDescription(n.getDescription());
 		setOwner(n.getOwner());
 		invitations = n.getInvitations();		
 	}
 	
 	/**
 	 * Get id
 	 * 
 	 * @return
 	 */
 
 	public int getId() {
 		return id;
 	}
 	
 	/**
 	 * Get a ID that uniquely identifies that model
 	 */
 	@Override
 	public String getUMID() {
 		if(getId() != -1)
 			return "meeting_"+getId();
 		return null;
 	}
 	
 	/**
 	 * @return start time of meeting
 	 */
 	public Calendar getTimeFrom() {
 		return timeFrom;
 	}
 
 	/**
 	 * Set start meeting start time
 	 * 
 	 * @param timeFrom
 	 */
 	public void setTimeFrom(Calendar timeFrom) {
 		Calendar oldValue = this.timeFrom;
 		this.timeFrom = timeFrom;
 		changeSupport.firePropertyChange(TIME_FROM_PROPERTY, oldValue, timeFrom);
 	}
 
 	/**
 	 * @return meeting end time
 	 */
 	public Calendar getTimeTo() {
 		return timeTo;
 	}
 
 	/**
 	 * Set end time
 	 * 
 	 * @param timeTo
 	 */
 	public void setTimeTo(Calendar timeTo) {
 		Calendar oldValue = this.timeTo;
 		this.timeTo = timeTo;
 		changeSupport.firePropertyChange(TIME_TO_PROPERTY, oldValue, timeTo);
 	}
 
 	/**
 	 * Get meeting name
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set meeting name
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		String oldValue = this.name;
 		this.name = name;
 		changeSupport.firePropertyChange(NAME_PROPERTY, oldValue, name);
 	}
 
 	/**
 	 * @return meeting description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Set meeting description
 	 * 
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		String oldValue = this.description;
 		this.description = description;
 		changeSupport.firePropertyChange(DESCRIPTION_PROPERTY, oldValue, description);
 	}
 
 	/**
 	 * @return room model if there is a reserved meeting room
 	 */
 	public MeetingRoomModel getRoom() {
 		return room;
 	}
 
 	/**
 	 * Reserve a meeting room
 	 * 
 	 * @param room
 	 */
 	public void setRoom(MeetingRoomModel room) {
 		MeetingRoomModel oldValue = this.room;
 		this.room = room;
 		changeSupport.firePropertyChange(DESCRIPTION_PROPERTY, oldValue, room);
 	}
 	
 	/**
 	 * @return alternative location
 	 */
 	public String getLocation() {
 		return location;
 	}
 
 	/**
 	 * Set alternative location
 	 * 
 	 * @param location
 	 */
 	public void setLocation(String location) {
 		String oldValue = this.location;
 		this.location = location;
 		changeSupport.firePropertyChange(LOCATION_PROPERTY, oldValue, location);
 	}
 
 	/**
 	 * @return is active meeting
 	 */
 	public boolean isActive() {
 		return active;
 	}
 
 	/**
 	 * Set active boolean
 	 * 
 	 * @param active
 	 */
 	public void setActive(boolean active) {
 		boolean oldValue = this.active;
 		this.active = active;
 		changeSupport.firePropertyChange(ACTIVE_PROPERTY, oldValue, active);
 	}
 
 	/**
 	 * @return owner user
 	 */
 	public UserModel getOwner() {
 		return owner;
 	}
 	
 
 	public String toString() {
 		return   timeFrom + " - " + timeTo + "  "+ getName();
 	} 
 
 	public void setOwner(UserModel owner) {
 		this.owner = owner;
 	}
 
 	
 	/**
 	 * Get all invitations for the meeting
 	 * 
 	 * @return
 	 */
 	public ArrayList<InvitationModel> getInvitations() {
 		return invitations;
 	}
 	
 	/**
 	 * Check if this meeting is actually a meeting and not a appointment
 	 * 
 	 * @return
 	 */
 	public boolean isMeeting() {
 		return getInvitations().size() != 0;
 	}
 	
 	/**
 	 * True if the given user has been invited to this meeting
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public boolean isInvited(UserModel user) {
 		return getInvitation(user) != null;
 	}
 	
 	/**
 	 * If this user has been invited to this meeting this will return
 	 * the invitation model representing that invitation
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public InvitationModel getInvitation(UserModel user) {
 		// TODO Store invitations in a map indexed by username
 		for(InvitationModel invitation : getInvitations()) {
 			if(invitation.getUser().getUsername().equals(user.getUsername())) {
 				return invitation;
 			}
 		}
 		return null;
 		
 	}
 			
 	/**
 	 * Add a attendee to the meeting
 	 * 
 	 * @param user
 	 */
 	public void addAttendee(UserModel user) {
 		addAttendee(new UserModel[]{user});
 	}
 	
 	/**
 	 * Removes a attendee from the meeting
 	 * 
 	 * @param user
 	 */
 	public void removeAttendee(UserModel user) {
 		if (isInvited(user)) {
 			InvitationModel inv = getInvitation(user);
 			invitations.remove(inv);
 			if(inv.getStatus() != InvitationStatus.NOT_YET_SAVED) {
 				try {
 					inv.delete();
 				} catch(IOException e) {
 					e.printStackTrace();
 				}
 			}
 			changeSupport.firePropertyChange(INVITATION_REMOVED, null, inv);
 		}
 	}
 	
 	/**
 	 * Remove a set of attendees
 	 * 
 	 * @param users
 	 */
 	public void removeAttendees(UserModel[] users) {
 		for(UserModel u : users)
 			removeAttendee(u);
 	}
 	
 		
 	/**
 	 * Add a array of attendees to the meeting
 	 * 
 	 * @param users
 	 */
 	public void addAttendee(UserModel[] users) {
 		for(UserModel user : users) {
 			if(!isInvited(user)) {
 				InvitationModel invitation = new InvitationModel(user, this);
 				invitations.add(invitation);
 				changeSupport.firePropertyChange(INVITATION_CREATED,null, invitation);
 			}
 		}		
 
 	}	
 	
 	
 	
 	/**
 	 * Changes the status of all invitations from NOT_YET_SAVED to INVITED
 	 */
 	public void commitInvitations() {
 		for (InvitationModel inv : getInvitations())
 			if (inv.getStatus() == InvitationStatus.NOT_YET_SAVED)
 				inv.setStatus(InvitationStatus.INVITED);
 	}
 	
 	/**
 	 * Discards all invitations with a NOT_YET_SAVED status
 	 */
 	public void discardInvitations() {
 		for (InvitationModel inv : getInvitations())
 			if (inv.getStatus() == InvitationStatus.NOT_YET_SAVED)
 				removeAttendee(inv.getUser());
 	}
 	
 	public static final Comparator<MeetingModel> timeFromComparator = 
 			new Comparator<MeetingModel>() {
 				@Override
 				public int compare(MeetingModel A, MeetingModel B) {					
 					return A.getTimeFrom().compareTo(B.getTimeFrom());
 				}
 			};
 
 	/**
 	 * Register sub models
 	 */
 	@Override
 	public void addSubModels(ModelEnvelope envelope) {
 		// Add invitations first so as they also depend on users 
 		if(getId() != -1)
 			for(InvitationModel i : getInvitations())
 				envelope.addModel(i);
 		
 		envelope.addModel(getOwner());
 		if(getRoom() != null)
 			envelope.addModel(getRoom());
 	}
 
 	/**
 	 * Dump model to StringBuilder
 	 */
 	@Override
 	public void toStringBuilder(StringBuilder sb) {
		DateFormat df = DateFormat.getDateTimeInstance();
 		
 		sb.append(getId() + "\r\n");
 		sb.append(getName() + "\r\n");
 		if(getDescription() != null) 
 			sb.append(getDescription().trim() + "\r\n");
 		sb.append("\0\r\n");
 		sb.append(df.format(getTimeFrom().getTime()) + "\r\n");
 		sb.append(df.format(getTimeTo().getTime()) + "\r\n");
 		sb.append(getLocation() + "\r\n");
 		
 		sb.append(getOwner().getUMID() + "\r\n");
 		if(getRoom() != null)
 			sb.append(getRoom().getUMID());
 		sb.append("\r\n");
 		
 		// Invitations cannot be identified untill a Meeting has been saved
 		if(getId() != -1) {
 			sb.append(getInvitations().size()+"\r\n");
 			for(InvitationModel i : getInvitations())
 				sb.append(i.getUMID()+"\r\n");
 		} else {
 			sb.append("0\r\n");
 		}
 	}	
 	
 	/**
 	 * Add property change listener
 	 * 
 	 * @param listener
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		changeSupport.addPropertyChangeListener(listener);
 	}
 	
 	/**
 	 * Remove property change listener
 	 * 
 	 * @param listener
 	 */
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		changeSupport.removePropertyChangeListener(listener);
 	}
 	
 	/**
 	 * clear property change listeners
 	 */
 	public void clearPropertyChangeListeners() {
 		for (PropertyChangeListener listener : changeSupport.getPropertyChangeListeners())
 			changeSupport.removePropertyChangeListener(listener);
 	}
 	
 	
 	/**
 	 * Store the meeting object on server
 	 * 
 	 */
 	public void store() throws IOException {
 		if(!ServerConnection.isOnline())
 			throw new IOException("Cannot store Meeting, not logged in");
 		
 		MeetingModel stored = (MeetingModel) ServerConnection.instance().storeModel(this);
 		
 		// We're a new model
 		if(getId() == -1) {
 			// Set id and call store again, this will save any invitations
 			id = stored.getId();
 			if(invitations.size() > 0)
 				stored = (MeetingModel) ServerConnection.instance().storeModel(this);
 			
 			// Make sure the correct version of the model is cached, and return
 			ModelCacher.free(stored);
 			ModelCacher.cache(this);
 			
 			//Add to current calendar
 			this.owner.getCalendarModel().add(this);
 		}
 	}
 	
 	/**
 	 * Delete meeting
 	 * 
 	 */
 	public void delete() throws IOException {
 		if(!ClientMain.getActiveUser().equals(getOwner()))
 			throw new IOException("User does not own meeting");
 		ServerConnection.instance().deleteMeeting(this);
 	}
 	
 }
