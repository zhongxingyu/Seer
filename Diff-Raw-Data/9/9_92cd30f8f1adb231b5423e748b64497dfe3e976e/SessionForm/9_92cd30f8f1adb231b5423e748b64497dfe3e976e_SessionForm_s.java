 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.jasig.portlet.blackboardvcportlet.service;
 
 import org.jasig.portlet.blackboardvcportlet.data.RecordingMode;
 import org.jasig.portlet.blackboardvcportlet.data.ServerConfiguration;
 import org.jasig.portlet.blackboardvcportlet.data.Session;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import javax.validation.constraints.Future;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 
 /**
  * form backing object for creating/editing sessions
  */
 public class SessionForm implements Serializable {
     private static final long serialVersionUID = 1L;
     
     private boolean newSession;
     private long sessionId;
 	@NotNull
     private String sessionName;
 	@NotNull
     private int boundaryTime;
     private int maxTalkers;
 	private int maxCameras;
 	private boolean mustBeSupervised;
 	private boolean permissionsOn;
 	private boolean raiseHandOnEnter;
 	private RecordingMode recordingMode;
 	private boolean hideParticipantNames;
 	private boolean allowInSessionInvites;
 
 	@Future
 	private DateMidnight startDate;
 
	@Min(1)
	@Max(12)
 	private int startHour;
 
 	@Min(0)
 	@Max(60)
 	private int startMinute;
 
 	@Future
 	private DateMidnight endDate;
 
	@Min(1)
	@Max(12)
 	private int endHour;
 
 	@Min(0)
 	@Max(60)
 	private int endMinute;
 
 	public SessionForm() {
 	}
 	
 	public SessionForm(ServerConfiguration serverConfiguration) {
         this.newSession = true;
         
         final DateTime startTime = DateTime.now().plusDays(1).hourOfDay().roundFloorCopy();
         this.setStartTime(startTime);
         this.setEndTime(startTime.plusHours(1));
         
         this.boundaryTime = serverConfiguration.getBoundaryTime();
 	    this.maxCameras = serverConfiguration.getMaxAvailableCameras();
 	    this.maxTalkers = serverConfiguration.getMaxAvailableTalkers();
 	    this.raiseHandOnEnter = serverConfiguration.isRaiseHandOnEnter(); 
 	}
 	
 	public SessionForm(Session session) {
         this.newSession = false;
         
         this.sessionId = session.getSessionId();
         this.sessionName = session.getSessionName();
         this.setStartTime(session.getStartTime());
         this.setEndTime(session.getEndTime());
         this.boundaryTime = session.getBoundaryTime();
 	    this.maxTalkers = session.getMaxTalkers();
 	    this.maxCameras = session.getMaxCameras();
 	    this.mustBeSupervised = session.isMustBeSupervised();
 	    this.permissionsOn = session.isPermissionsOn();
 	    this.raiseHandOnEnter = session.isRaiseHandOnEnter();
 	    this.recordingMode = session.getRecordingMode();
 	    this.hideParticipantNames = session.isHideParticipantNames();
 	    this.allowInSessionInvites = session.isAllowInSessionInvites();
 	}
 	
     public boolean isNewSession() {
         return newSession;
     }
 
     public void setNewSession(boolean newSession) {
         this.newSession = newSession;
     }
 
     public long getSessionId() {
         return sessionId;
     }
 
     public void setSessionId(long sessionId) {
         this.sessionId = sessionId;
     }
 
     public String getSessionName() {
         return sessionName;
     }
 
     public void setSessionName(String sessionName) {
         this.sessionName = sessionName;
     }
 
     public DateTime getStartTime() {
         return this.startDate.toDateTime().withHourOfDay(this.startHour).withMinuteOfHour(this.startMinute);
     }
 
     public void setStartTime(DateTime startTime) {
         this.startDate = startTime.toDateMidnight();
         this.startHour = startTime.getHourOfDay();
         this.startMinute = startTime.getMinuteOfHour();
     }
 
     public DateTime getEndTime() {
         return this.endDate.toDateTime().withHourOfDay(this.endHour).withMinuteOfHour(this.endMinute);
     }
 
     public void setEndTime(DateTime endTime) {
         this.endDate = endTime.toDateMidnight();
         this.endHour = endTime.getHourOfDay();
         this.endMinute = endTime.getMinuteOfHour();
     }
 
 	public int getBoundaryTime() {
         return boundaryTime;
     }
 
     public void setBoundaryTime(int boundaryTime) {
         this.boundaryTime = boundaryTime;
     }
 
     public int getMaxTalkers() {
         return maxTalkers;
     }
 
     public void setMaxTalkers(int maxTalkers) {
         this.maxTalkers = maxTalkers;
     }
 
     public int getMaxCameras() {
         return maxCameras;
     }
 
     public void setMaxCameras(int maxCameras) {
         this.maxCameras = maxCameras;
     }
 
     public boolean isMustBeSupervised() {
         return mustBeSupervised;
     }
 
     public void setMustBeSupervised(boolean mustBeSupervised) {
         this.mustBeSupervised = mustBeSupervised;
     }
 
     public boolean isPermissionsOn() {
         return permissionsOn;
     }
 
     public void setPermissionsOn(boolean permissionsOn) {
         this.permissionsOn = permissionsOn;
     }
 
     public boolean isRaiseHandOnEnter() {
         return raiseHandOnEnter;
     }
 
     public void setRaiseHandOnEnter(boolean raiseHandOnEnter) {
         this.raiseHandOnEnter = raiseHandOnEnter;
     }
 
     public RecordingMode getRecordingMode() {
         return recordingMode;
     }
 
     public void setRecordingMode(RecordingMode recordingMode) {
         this.recordingMode = recordingMode;
     }
 
     public boolean isHideParticipantNames() {
         return hideParticipantNames;
     }
 
     public void setHideParticipantNames(boolean hideParticipantNames) {
         this.hideParticipantNames = hideParticipantNames;
     }
 
     public boolean isAllowInSessionInvites() {
         return allowInSessionInvites;
     }
 
     public void setAllowInSessionInvites(boolean allowInSessionInvites) {
         this.allowInSessionInvites = allowInSessionInvites;
     }
     
     public DateMidnight getStartDate() {
         return startDate;
     }
 
     public void setStartDate(DateMidnight startDate) {
         this.startDate = startDate;
     }
 
     public int getStartHour() {
         return startHour;
     }
 
     public void setStartHour(int startHour) {
         this.startHour = startHour;
     }
 
     public int getStartMinute() {
         return startMinute;
     }
 
     public void setStartMinute(int startMinute) {
         this.startMinute = startMinute;
     }
 
     public DateMidnight getEndDate() {
         return endDate;
     }
 
     public void setEndDate(DateMidnight endDate) {
         this.endDate = endDate;
     }
 
     public int getEndHour() {
         return endHour;
     }
 
     public void setEndHour(int endHour) {
         this.endHour = endHour;
     }
 
     public int getEndMinute() {
         return endMinute;
     }
 
     public void setEndMinute(int endMinute) {
         this.endMinute = endMinute;
     }
 
     @Override
     public String toString() {
         return "FullSessionForm [newSession=" + newSession + ", sessionId=" + sessionId + ", sessionName="
                 + sessionName + ", startTime=" + this.getStartTime() + ", endTime=" + this.getEndTime() + ", boundaryTime=" + boundaryTime
                 + ", maxTalkers=" + maxTalkers + ", maxCameras=" + maxCameras + ", mustBeSupervised="
                 + mustBeSupervised + ", permissionsOn=" + permissionsOn + ", raiseHandOnEnter=" + raiseHandOnEnter
                 + ", recordingMode=" + recordingMode + ", hideParticipantNames=" + hideParticipantNames
                 + ", allowInSessionInvites=" + allowInSessionInvites + "]";
     }
 }
     
     
