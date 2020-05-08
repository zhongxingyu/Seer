 package client.model;
 
 import java.beans.PropertyChangeSupport;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import server.ModelEnvelope;
import client.AbstractConnection;
 import client.ServerConnection;
 
 /**
  * Model representing a Notification
  * 
  * @author endre
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public class NotificationModel implements TransferableModel, Comparable<NotificationModel>{
 	
 	public final static String REGARDS_MEETING_PROPERTY = "regards_meeting";
 	public final static String TYPE_PROPERTY = "type";
 	public final static String TIME_PROPERTY = "time";
 	public final static String GIVEN_TO_PROPERTY = "given_to";
 	public final static String REGARDS_USER_PROPERTY = "regards_user";
 	public final static String READ_PROPERTY = "read";
 	
 	protected int id = -1;
 	protected MeetingModel regards_meeting;
 	protected NotificationType type;
 	protected Calendar time;
 	protected UserModel given_to, regards_user;
 	protected boolean read;
 	
 	private PropertyChangeSupport changeSupport;
 	
 	/**
 	 * Construct NotificationModel
 	 */
 	protected NotificationModel() {
 		changeSupport = new PropertyChangeSupport(this);
 	}
 	
 	/**
 	 * Create a notification with timestamp set to NOW and read to false
 	 * 
 	 * @param type
 	 * @param given_to
 	 * @param regards_meeting
 	 */
 	public NotificationModel(NotificationType type, UserModel given_to, MeetingModel regards_meeting) {
 		this(type, given_to, regards_meeting, null);
 	}
 
 	/**
 	 * Create a notification with timestamp set to NOW and read to false
 	 * 
 	 * @param type
 	 * @param given_to
 	 * @param regards_meeting
 	 * @param regards_user
 	 */
 	public NotificationModel(NotificationType type, UserModel given_to, MeetingModel regards_meeting, 
 			UserModel regards_user) {
 		this(type, given_to, regards_meeting, regards_user, Calendar.getInstance(), false);
 	}
 	
 	/**
 	 * Create a new notification 
 	 * 
 	 * @param type
 	 * @param given_to
 	 * @param regards_meeting
 	 * @param regards_user
 	 * @param time
 	 * @param read
 	 */
 	public NotificationModel(NotificationType type, UserModel given_to, MeetingModel regards_meeting,
 			UserModel regards_user, Calendar time, boolean read) {
 		
 		this();
 		this.regards_meeting = regards_meeting;
 		this.type = type;
 		this.time = time;
 		this.given_to = given_to;
 		this.regards_user = regards_user;
 		this.read = read;
 	}
 	
 	/**
 	 * Construct NotificationModel from reader
 	 * 
 	 * @param reader
 	 * @param modelBuff
 	 * @throws IOException
 	 */
 	public NotificationModel(BufferedReader reader) throws IOException {
 		this();
		DateFormat df = AbstractConnection.defaultDateTimeFormat;
 		
 		id = Integer.parseInt(reader.readLine());
 		type = NotificationType.valueOf(reader.readLine());
 		
 		time = Calendar.getInstance();
 		try {
 			time.setTime(df.parse(reader.readLine()));
 		} catch(ParseException e) {
 			throw new IOException(e.toString());
 		}
 		
 		given_to_umid = reader.readLine();
 		
 		regards_meeting_umid = reader.readLine();
 		regards_user_umid = reader.readLine();
 		
 		read = reader.readLine().equals("TRUE");
 	}
 	
 	private String given_to_umid, regards_meeting_umid, regards_user_umid;
 	
 	@Override
 	public void registerSubModels(ModelEnvelope envelope) {
 		given_to = (UserModel) envelope.getFromBuffer(given_to_umid);
 		if(!regards_meeting_umid.equals("")) {
 			regards_meeting = (MeetingModel) envelope.getFromBuffer(regards_meeting_umid);
 		}
 		if(!regards_user_umid.equals("")) {
 			regards_user = (UserModel) envelope.getFromBuffer(regards_user_umid);
 		}
 	}
 	
 	
 	public void copyFrom(TransferableModel model) {
 		setRead(((NotificationModel)model).isRead());
 	}
 	
 	
 	/**
 	 * Get meeting the Notification regards, this may be null depending on
 	 * notification type
 	 * 
 	 * @return
 	 */
 	public MeetingModel getRegardsMeeting() {
 		return regards_meeting;
 	}
 	
 	/**
 	 * Set meeting this notification regards
 	 * 
 	 * @param regards_meeting
 	 */
 	public void setRegardsMeeting(MeetingModel regards_meeting) {
 		MeetingModel oldValue = this.regards_meeting;
 		this.regards_meeting = regards_meeting;
 		changeSupport.firePropertyChange(REGARDS_MEETING_PROPERTY, oldValue, regards_meeting);
 	}
 	
 	/**
 	 * Get the user this notification regards, may be null based on the
 	 * notification type
 	 * 
 	 * @return
 	 */
 	public UserModel getRegardsUser() {
 		return regards_user;
 	}
 	
 	/**
 	 * Set the user this notification regards
 	 * 
 	 * @param regards_user
 	 */
 	public void setRegardsUser(UserModel regards_user) {
 		UserModel oldValue = this.regards_user;
 		this.regards_user = regards_user;
 		changeSupport.firePropertyChange(REGARDS_USER_PROPERTY, oldValue, regards_user);
 	}
 	
 	/**
 	 * @return type of notification
 	 */
 	public NotificationType getType() {
 		return type;
 	}
 	
 	/**
 	 * Set the type of the notification
 	 * 
 	 * @param type
 	 */
 	public void setType(NotificationType type) {
 		NotificationType oldValue = this.type;
 		this.type = type;
 		changeSupport.firePropertyChange(TYPE_PROPERTY, oldValue, type);
 	}
 	
 	/**
 	 * Get the notification timestamp
 	 * 
 	 * @return
 	 */
 	public Calendar getTime() {
 		return time;
 	}
 	
 	/**
 	 * Set notification timestamp
 	 * 
 	 * @param time
 	 */
 	public void setTime(Calendar time) {
 		Calendar oldValue = this.time;
 		this.time = time;
 		changeSupport.firePropertyChange(TIME_PROPERTY, oldValue, time);
 	}
 	
 	/**
 	 * Get the user this notification is given to
 	 * 
 	 * @return
 	 */
 	public UserModel getGivenTo() {
 		return given_to;
 	}
 	
 	/**
 	 * Set the user this notification is given to
 	 * 
 	 * @param given_to
 	 */
 	public void setGivenTo(UserModel given_to) {
 		UserModel oldValue = this.given_to;
 		this.given_to = given_to;
 		changeSupport.firePropertyChange(GIVEN_TO_PROPERTY, oldValue, given_to);
 	}
 	
 	/**
 	 * @return is the notification marked as read?
 	 */
 	public boolean isRead() {
 		return read;
 	}
 	
 	/**
 	 * Mark the notification as read or unread
 	 * 
 	 * @param read
 	 */
 	public void setRead(boolean read) {
 		boolean oldValue = this.read;
 		this.read = read;
 		changeSupport.firePropertyChange(READ_PROPERTY, oldValue, read);
 	}
 	
 	/**
 	 * Get the id of this notification
 	 * 
 	 * @return
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Get a unique model id
 	 */
 	@Override
 	public String getUMID() {
 		if(id == -1)
 			return null;
 		return "notification_"+id;
 	}
 
 	/**
 	 * Add sub models
 	 */
 	@Override
 	public void addSubModels(ModelEnvelope envelope) {
 		envelope.addModel(getGivenTo());
 		if(getRegardsMeeting() != null)
 			envelope.addModel(getRegardsMeeting());
 		if(getRegardsUser() != null)
 			envelope.addModel(getRegardsUser());
 		
 	}
 
 	/**
 	 * Dump model to string buffer
 	 */
 	@Override
 	public void toStringBuilder(StringBuilder sb) {
		DateFormat df = AbstractConnection.defaultDateTimeFormat;
 		
 		sb.append(getId() + "\r\n");
 		sb.append(getType() + "\r\n");
 		sb.append(df.format(getTime().getTime()) + "\r\n");
 		sb.append(getGivenTo().getUMID()+"\r\n");
 		
 		if(getRegardsMeeting() != null) 
 			sb.append(getRegardsMeeting().getUMID());
 		sb.append("\r\n");
 			
 		if(getRegardsUser() != null)
 			sb.append(getRegardsUser().getUMID());
 		sb.append("\r\n");	
 		sb.append((read ? "TRUE" : "FALSE") + "\r\n");
 	}
 
 	@Override
 	public int compareTo(NotificationModel e) {
 		return -this.getTime().compareTo(e.getTime());
 	}
 	
 	@Override
 	public String toString() {
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
 		if (regards_meeting != null)
 			return sdf.format(time.getTime()) + " " + regards_meeting.getName() + " " + read;
 		else return sdf.format(time.getTime()) + " " + read;
 	}
 	
 	public void store()  throws IOException {
 		if(!ServerConnection.isOnline())
 			throw new IOException("Cannot store Meeting, not logged in");
 		ServerConnection.instance().storeModel(this);
 	}
 }
