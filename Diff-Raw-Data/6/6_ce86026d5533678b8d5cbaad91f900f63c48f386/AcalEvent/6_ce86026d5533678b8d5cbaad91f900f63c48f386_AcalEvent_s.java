 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.davacal;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.acaltime.AcalDateRange;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.acaltime.AcalDuration;
 import com.morphoss.acal.providers.DavResources;
 import com.morphoss.acal.providers.PendingChanges;
 
 /**
  * 
  * @author Morphoss Ltd
  *
  */
 public class AcalEvent implements Serializable, Parcelable, Comparable<AcalEvent>{
 
 	private static final long serialVersionUID = 1L;
 	public static final String TAG = "AcalEvent";
 	private AcalDateTime dtstart;
 	private AcalDuration duration;
 	private String summary;
 	private String description;
 	private String location;
 	private String repetition;
 	private int colour;
 	public final boolean hasAlarms;
 	public final int resourceId;
 	private List<AcalAlarm> alarmList = new ArrayList<AcalAlarm>();
 	private final String originalBlob;
 	private int collectionId;
 	public final boolean isPending;
 	private boolean	alarmEnabled;
 	private int action = ACTION_CREATE;
 	private boolean	dirty; 
 	private final boolean[] dirtyFlags = new boolean[EVENT_FIELD.values().length];
 
 	public static final int ACTION_CREATE = 0;
 	public static final int ACTION_MODIFY_SINGLE = 1;
 	public static final int ACTION_MODIFY_ALL = 2;
 	public static final int ACTION_MODIFY_ALL_FUTURE = 3;
 	public static final int ACTION_DELETE_SINGLE = 4;
 	public static final int ACTION_DELETE_ALL = 5;
 	public static final int ACTION_DELETE_ALL_FUTURE = 6;
 
 	public static enum EVENT_FIELD {
 			resourceId,
 			startDate,
 			duration,
 			summary,
 			location,
 			description,
 			colour,
 			collectionId,
 			repeatRule,
 			alarmList
 	}
 	
 	public static final Parcelable.Creator<AcalEvent> CREATOR = new Parcelable.Creator<AcalEvent>() {
         public AcalEvent createFromParcel(Parcel in) {
             return getInstanceFromParcel(in);
         }
 
         public AcalEvent[] newArray(int size) {
             return new AcalEvent[size];
         }
     };
 
 	public static AcalEvent getInstanceFromParcel(Parcel in) {
 		AcalEvent event = null;
 		event = new AcalEvent(in); 
 		return event;
 	}
 
 	
 	/**
	 * Construct an AcalEvent from a row from the database.  In case it is 
 	 * @param context
 	 * @param resourceId
	 * @return
 	 */
 	public static AcalEvent fromDatabase( Context context, int resourceId, AcalDateTime dtStart ) {
 		ContentValues resourceValues = DavResources.getRow(resourceId, context.getContentResolver());
 		ContentValues pendingValues = PendingChanges.getByResource(resourceId, context.getContentResolver());
 		if ( pendingValues != null ) {
 			String newData = pendingValues.getAsString(PendingChanges.NEW_DATA);
 			if ( newData == null ) newData = "";
 			resourceValues.put(DavResources.RESOURCE_DATA, newData);
 			resourceValues.put(DavResources.IS_PENDING, 1);
 			if ( Constants.LOG_DEBUG )
 				Log.i(TAG,"Using pending change record for AcalEvent fromDatabase.");
 		}
 
 		VComponent vc;
 		try {
 			vc = VComponent.createComponentFromResource( resourceValues,
 						AcalCollection.fromDatabase(context, resourceValues.getAsLong(DavResources.COLLECTION_ID)));
 		}
 		catch (VComponentCreationException e) {
 			Log.e(TAG,Log.getStackTraceString(e));
 			return null;
 		}
 		if ( ! (vc instanceof VCalendar) ) {
 			Log.w(TAG,"Trying to build AcalEvent but resource "+resourceId+" is not a VCalendar it is a "+vc.name );
 			return null;
 		}
 		Masterable event = ((VCalendar) vc).getMasterChild();
 		if ( ! (event instanceof VEvent) ) {
 			Log.w(TAG,"Trying to build AcalEvent but resource contained in "+resourceId+" is not a VEvent");
 			return null;
 		}
 		if ( dtStart == null ) dtStart = AcalDateTime.fromAcalProperty(event.getProperty("DTSTART"));
 		return new AcalEvent( event, dtStart, event.getDuration(), false );
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 	@Override
 	public void writeToParcel(Parcel out, int flags) {
 		getStart().writeToParcel( out, flags);
 		duration.writeToParcel(out, 0);
 		out.writeString(getSummary());
 		out.writeString(getLocation());
 		out.writeString(getDescription());
 		out.writeString(getRepetition());
 		out.writeInt(getColour());
 		out.writeByte((byte) (hasAlarms() ? 'T' : 'F'));
 		out.writeByte((byte) (alarmEnabled ? 'T' : 'F'));
 		out.writeInt(getResourceId());
 		out.writeString(originalBlob);
 		out.writeInt(collectionId);
 		out.writeTypedList(alarmList);
 		out.writeByte((byte) (isPending ? 'T' : 'F'));
 	}
 
 	public AcalEvent(Parcel in) {
 		this.dtstart = AcalDateTime.unwrapParcel(in);
 		this.duration = new AcalDuration(in);
 		this.summary = in.readString();
 		this.location = in.readString();
 		this.description = in.readString();
 		this.repetition = in.readString();
 		this.colour = in.readInt();
 		this.hasAlarms = in.readByte() == 'T';
 		this.alarmEnabled = in.readByte() == 'T';
 		this.resourceId = in.readInt();
 		this.originalBlob = in.readString();
 		this.collectionId = in.readInt();
 		in.readTypedList(this.alarmList, AcalAlarm.CREATOR);
 		this.isPending = in.readByte() == 'T';
     }
 	
 	@Override
 	public int compareTo(AcalEvent other) {
 		if ( getStart().before(other.getStart())) return -1;
 		if ( getStart().after(other.getStart())) return 1;
 		if ( getEnd().before(other.getEnd())) return -1;
 		if ( getEnd().after(other.getEnd())) return 1;
 		return 0;
 	}
 
 	public AcalEvent(VComponent event, AcalDateTime startDate, AcalDuration duration, boolean isPending ) {
 		this.resourceId = event.getResourceId();
 		dtstart = startDate;
 
 		if ( duration == null ) {
 			AcalProperty dtend = event.getProperty("DTEND");  
 			if (dtend != null) 
 				duration = startDate.getDurationTo(AcalDateTime.fromAcalProperty(dtend));
 		} 
 		if ( duration == null ) {
 				duration = new AcalDuration();
 				duration.setDuration(1, 0);
 		}
 
 		this.duration = duration;
 		summary = safeEventPropertyValue(event, "SUMMARY");
 		description = safeEventPropertyValue(event, "DESCRIPTION");
 		location = safeEventPropertyValue(event, "LOCATION");
 		originalBlob = event.getTopParent().getOriginalBlob();
 		String repeatRule = safeEventPropertyValue(event,"RRULE");
 		String repeatInstances = safeEventPropertyValue(event,"RDATE");
 		repetition = repeatRule + (repeatInstances.equals("")?"":"\n"+repeatInstances);
 
 		List<AcalAlarm> theseAlarms = new ArrayList<AcalAlarm>();
 		for( VComponent child : event.getChildren() ) {
 			if ( child instanceof VAlarm ) {
 				theseAlarms.add(new AcalAlarm((VAlarm) child, (Masterable) event, dtstart.clone(), AcalDateTime.addDuration(dtstart, duration)));
 			}
 		}
 		
 		alarmList.addAll(theseAlarms);
 		hasAlarms = alarmList.size() > 0;
 		
 		int aColor = Color.BLUE;
 		boolean alarmsForCollection = true;
 		try {
 			aColor = event.getCollectionColour();
 			alarmsForCollection = event.getAlarmEnabled();
 		} catch (Exception e) {
 			Log.e(TAG,"Error Creating AcalEvent - "+e.getMessage());
 			Log.e(TAG,Log.getStackTraceString(e));
 		}
 		colour = aColor;
 		alarmEnabled = alarmsForCollection;
 		
 		int fromCollectionId;
 		try {
 			fromCollectionId = event.getCollectionId();
 		}
 		catch( NullPointerException e ) {
 			fromCollectionId = -1;
 		}
 		collectionId = fromCollectionId;
 		this.isPending = isPending;
 	}
 
 	
 	private String safeEventPropertyValue(VComponent event, String propertyName ) {
 		String propertyValue = null;
 		try {
 			propertyValue = event.getProperty(propertyName).getValue();
 		}
 		catch (Exception e) {
 		}
 		if (propertyValue == null) propertyValue = "";
 		return propertyValue;
 	}
 
 	
 	
 
 	public List<AcalAlarm> getAlarms() {
 		return this.alarmList;
 	}
 
 	public int getColour() {
 		return this.colour;
 	}
 
 	public boolean getAlarmEnabled() {
 		return this.alarmEnabled;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	public AcalDateTime getEnd() {
 		return AcalDateTime.addDuration(dtstart, duration);
 	}
 	
 	public AcalDuration getDuration() {
 		return this.duration;
 	}
 
 	public String getLocation() {
 		return this.location;
 	}
 
 	public String getRepetition() {
 		return this.repetition;
 	}
 
 	public void setRepetition(String newRepetition) {
 		this.repetition = newRepetition;
 		this.dirty = true;
 	}
 
 	public void setField(EVENT_FIELD field, Object val) {
 		switch( field ) {
 			case startDate:
 				this.dtstart = (AcalDateTime) val;
 				break;
 			case duration:
 				this.duration = (AcalDuration) val;
 				break;
 			case summary:
 				this.summary = (String) val;
 				break;
 			case description:
 				this.description = (String) val;
 				break;
 			case location:
 				this.location = (String) val;
 				break;
 			case repeatRule:
 				this.repetition = (String) val;
 				break;
 			case collectionId:
 				this.collectionId = (Integer) val;
 				break;
 			case colour:
 				this.colour = (Integer) val;
 				break;
 			case alarmList:
 				this.alarmList = (List<AcalAlarm>) val;
 				break;
 			default:
 				throw new IllegalStateException("The "+field.toString()+" is not modifiable.");
 		}
 		dirtyFlags[field.ordinal()] = true;
 		dirty=true;
 	}
 
 	public AcalDateTime getStart() {
 		return this.dtstart;
 	}
 
 	public String getTimeText(AcalDateTime viewDateStart, AcalDateTime viewDateEnd, boolean as24HourTime ) {
 		AcalDateTime start = this.getStart();
 		start.applyLocalTimeZone();
 		AcalDateTime finish = this.getEnd();
 		if ( finish != null ) finish.applyLocalTimeZone();
 		String timeText = "";
 		String timeFormatString = (as24HourTime ? "HH:mm" : "hh:mmaa");
 		SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormatString);
 		
 		if ( start.before(viewDateStart) || (finish != null && finish.after(viewDateEnd)) ){
 			if ( start.isDate() ) {
 				timeText = AcalDateTime.fmtDayMonthYear(start)+ ", all day";
 			}
 			else {
 				SimpleDateFormat startFormatter = timeFormatter;
 				SimpleDateFormat finishFormatter = timeFormatter;
 				
 				if ( start.before(viewDateStart) || start.after(viewDateEnd) ) {
 					startFormatter  = new SimpleDateFormat("MMM d, "+timeFormatString);
 					if ( (finish.getYear() > start.getYear()) || (finish.getYearDay() > start.getYearDay()) )
 						finishFormatter = new SimpleDateFormat("MMM d, "+timeFormatString);
 				}
 		
 				timeText = (startFormatter.format(start.toJavaDate())+" - "
 							+ (finish == null ? "null" : finishFormatter.format(finish.toJavaDate())));
 			}
 		}
 		else if ( start.isDate()) {
 			timeText = "All Day";
 		}
 		else {
 			timeText = (timeFormatter.format(start.toJavaDate())+" - "
 						+ (finish == null ? "null" : timeFormatter.format(finish.toJavaDate())));
 		}
 		return timeText;
 	}
 
 	public boolean overlaps( AcalDateRange range ) {
 		return range.overlaps(dtstart, dtstart.addDuration(duration));
 	}
 
 	/**
 	 * Get the value from the SUMMARY field.
 	 * @return
 	 */
 	public String getSummary() {
 		return this.summary;
 	}
 
 	/**
 	 * Ascertain whether the event has alarms.
 	 * @return true, if the event has alarms
 	 */
 	public boolean hasAlarms() {
 		return this.hasAlarms;
 	}
 	
 	public int getResourceId() {
 		return this.resourceId;
 	}
 	
 	public int getCollectionId() {
 		return this.collectionId;
 	}
 	
 	public String getOriginalBlob() {
 		return this.originalBlob;
 	}
 
 	public void setAction(int action) {
 		this.action = action;
 	}
 
 	public boolean isDirty() { return dirty; }
 	public int getAction() {
 		return this.action;
 	}
 	
 	public boolean isModifyAction() {
 			return 	(this.action == ACTION_MODIFY_SINGLE) ||
 					(this.action == ACTION_MODIFY_ALL) ||
 					(this.action == ACTION_MODIFY_ALL_FUTURE); 
 	}
 
 
 	public AcalEvent(Map<EVENT_FIELD, Object> defaults) {
 		this.dtstart = (AcalDateTime) defaults.get(EVENT_FIELD.startDate);
 		this.duration = (AcalDuration) defaults.get(EVENT_FIELD.duration);
 		this.summary = (String) defaults.get(EVENT_FIELD.summary);
 		this.description = (String) defaults.get(EVENT_FIELD.description);
 		this.location = (String) defaults.get(EVENT_FIELD.location);
 		this.repetition = (String) defaults.get(EVENT_FIELD.repeatRule);
 		this.colour = (Integer) defaults.get(EVENT_FIELD.colour);
 		List<?> alarmList = (List<?>) defaults.get(EVENT_FIELD.alarmList);
 		this.hasAlarms = ! alarmList.isEmpty();
 		this.alarmList.addAll((List<AcalAlarm>) alarmList);
 
 		int theId = -1;
 		if ( (Integer) defaults.get(EVENT_FIELD.resourceId) != null )
 			theId = (Integer) defaults.get(EVENT_FIELD.resourceId);
 		this.resourceId = theId;
 
 		theId = -1;
 		if ( (Integer) defaults.get(EVENT_FIELD.collectionId) != null )
 			theId = (Integer) defaults.get(EVENT_FIELD.collectionId);
 		this.collectionId = theId;
 
 		this.originalBlob = null;
 		this.isPending = false;
 		this.alarmEnabled = true;
 		this.dirty = false;
 	}
 
 
 	/**	
 	private AcalEvent(AcalDateTime dtstart,	AcalDuration duration, String summary,
 						String description, String location, 
 						String repetition, int colour, boolean hasAlarms,
 						int resourceId, List<AcalAlarm> alarmList, String originalBlob, int collectionId) {
 		this.dtstart = dtstart;
 		this.duration = duration;
 		this.summary = summary;
 		this.description = description;
 		this.location = location;
 		this.repetition = repetition;
 		this.colour = colour;
 		this.hasAlarms = hasAlarms;
 		this.resourceId = resourceId;
 		this.alarmList.addAll(alarmList);
 		this.originalBlob = originalBlob;
 		this.collection = collectionId;
 		
 	}
 
 	public static AcalEvent emptyEvent() {
 
 		return new AcalEvent(
 					null,
 					null,
 					null,
 					null,
 					null,
 					null,
 					0,
 					false,
 					-1,
 					new ArrayList<AcalAlarm>(),
 					null,
 					-1
 				);
 	}
 	*/
 }
