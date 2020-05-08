 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVACAO, SA (PTIN), IBM Corp.,
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package si.setcce.societies.crowdtasking.api.RESTful.impl;
 
 // TeamCity test
 
 import com.google.gson.Gson;
 import com.googlecode.objectify.Key;
 import si.setcce.societies.crowdtasking.NotificationsSender;
 import si.setcce.societies.crowdtasking.api.RESTful.IMeetingAPI;
 import si.setcce.societies.crowdtasking.api.RESTful.json.MeetingJS;
 import si.setcce.societies.crowdtasking.api.RESTful.json.TaskJS;
 import si.setcce.societies.crowdtasking.gcm.GcmMessage;
 import si.setcce.societies.crowdtasking.gcm.Parameters;
 import si.setcce.societies.crowdtasking.model.CTUser;
 import si.setcce.societies.crowdtasking.model.CollaborativeSign;
 import si.setcce.societies.crowdtasking.model.Meeting;
 import si.setcce.societies.crowdtasking.model.Task;
 import si.setcce.societies.crowdtasking.model.dao.MeetingDAO;
 import si.setcce.societies.crowdtasking.model.dao.TaskDao;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import static si.setcce.societies.crowdtasking.model.dao.OfyService.ofy;
 
 @Path("/meeting/{querytype}")
 public class MeetingAPI implements IMeetingAPI {
     private static final int MEETINGS_ON_PD = 10;
     private static final Logger log = Logger.getLogger(MeetingAPI.class.getName());
 
     @Override
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public String getMeeting(@PathParam("querytype") String querytype,
                              @QueryParam("id") Long meetingId,
                              @QueryParam("csId") Long csId,
                              @Context HttpServletRequest request) {
 
         Meeting meeting;
         CollaborativeSign collaborativeSign;
         Gson gson = new Gson();
 
         switch (querytype) {
             case "data":
                 if (meetingId == null) {
                     return "Missing id.";
                 }
                 // get meeting
                 meeting = MeetingDAO.loadMeeting(meetingId);
                 return gson.toJson(new MeetingJS(meeting));
             case "communitysign":
                 collaborativeSign = getCollaborativeSign();
                 if (collaborativeSign.getMeetingId() != null) {
                     meeting = MeetingDAO.loadMeeting(collaborativeSign.getMeetingId());
                     log.info("sending meeting details");
                     MeetingJS meetingJS = new MeetingJS(meeting);
                     return gson.toJson(meetingJS);
                 } else {
                     log.warning("No meeting id in community sign");
                 }
             case "cs":
                 return getMeetingsForCS(csId);
             case "attend":
                 // get meeting
                 collaborativeSign = MeetingAPI.getCollaborativeSign();
                 meeting = MeetingDAO.loadMeeting(collaborativeSign.getMeetingId());
                 // attend meeting
                 MeetingAPI.attendMeeting(UsersAPI.getLoggedInUser(request.getSession()), meeting, collaborativeSign);
                 return "Meeting check-in.";
         }
         return Response.status(Status.BAD_REQUEST).entity("Request not understood.").type("text/plain").build().toString();
     }
 
     private String getMeetingsForCS(Long csId) {
         Gson gson = new Gson();
         ArrayList<MeetingJS> meetingsJS = new ArrayList<>();
         for (Meeting meeting : MeetingDAO.getMeetingsForCS(csId, MEETINGS_ON_PD)) {
             meetingsJS.add(new MeetingJS(meeting));
         }
         return gson.toJson(meetingsJS);
     }
 
     @Override
     @POST
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public Response newMeeting(@PathParam("querytype") String querytype,
                                @FormParam("taskId") Long taskId,
                                @FormParam("meetingSubject") String meetingSubject,
                                @FormParam("meetingDescription") String meetingDescription,
                                @FormParam("meetingCS") Long csId,
                                @FormParam("taskStart") String taskStart,
                                @FormParam("taskEnd") String taskEnd,
                                @FormParam("meetingIdToSign") String meetingIdToSignStr,
                                @FormParam("downloadUrl") String downloadUrl,
                                @FormParam("registrationId") String regId,
                                @FormParam("minute") String minute,
                                @Context HttpServletRequest request) {
 
         if ("registerCollaborativeSign4GCM".equalsIgnoreCase(querytype)) {
             if (regId != null && !"".equalsIgnoreCase(regId)) {
                 CollaborativeSign collaborativeSign = getCollaborativeSign();
                 collaborativeSign.setGcmRegistrationId(regId);
                 setCollaborativeSign(collaborativeSign);
                 return Response.ok().entity("Registration id saved.").build();
             }
         }
         Long meetingIdToSign;
         CTUser user = UsersAPI.getLoggedInUser(request.getSession());
 
         if ("create".equalsIgnoreCase(querytype)) {
             Task task = TaskDao.loadTask(taskId);
             DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
             Date startTime;
             try {
                 startTime = formatter.parse(taskStart);
             } catch (ParseException e) {
                 return Response.status(Status.BAD_REQUEST).entity("Start time error.").type("text/plain").build();
             }
             if (startTime.before(new Date())) {
                 return Response.status(Status.BAD_REQUEST).entity("Start time is in the past.").type("text/plain").build();
             }
             Date endTime;
             try {
                 endTime = formatter.parse(taskEnd);
             } catch (ParseException e) {
                 return Response.status(Status.BAD_REQUEST).entity("End time error.").type("text/plain").build();
             }
             if (startTime.after(endTime)) {
                 return Response.status(Status.BAD_REQUEST).entity("Start time is after end time.").type("text/plain").build();
             }
 
             Meeting meeting = new Meeting(meetingSubject, meetingDescription, csId, startTime, endTime, user, task.getInvolvedUsers());
             Key<Meeting> meetingKey = MeetingDAO.saveMeeting(meeting);
             task.addMeeting(meetingKey);
             TaskDao.save(task);
             NotificationsSender.newMeeting(meeting, task);
             TaskDao.setTransientTaskParams(user, task);
             EventAPI.logNewMeeting(task, meeting, new Date(), user);
             // because of Ref<?> value has not been initialized
             task = TaskDao.loadTask(task.getId());
             Gson gson = new Gson();
             return Response.ok().entity(gson.toJson(new TaskJS(task, user))).build();
         }
 
         try {
             meetingIdToSign = Long.parseLong(meetingIdToSignStr);
         } catch (Exception e) {
             return Response.status(Status.BAD_REQUEST).entity("Invalid meeting id.").type("text/plain").build();
         }
         if ("".equals(meetingSubject)) {
             return Response.status(Status.BAD_REQUEST).entity("Subject is required.").type("text/plain").build();
         }
 
         // todo: move old ifs to switch?
         switch (querytype) {
             case "postMinute":
                 return postMinute(meetingIdToSign, user, minute);
         }
 
         // finalize meeting
         if ("communitysign".equalsIgnoreCase(querytype)) {
             Meeting meeting = MeetingDAO.loadMeeting(meetingIdToSign);
             if (meeting == null) {
                 return Response.status(Status.BAD_REQUEST).entity("Wrong meeting id.").type("text/plain").build();
             }
             log.info("downloadUrl: " + downloadUrl);
             meeting.setDownloadUrl(downloadUrl);
             NotificationsSender.meetingIsReadyToBeSigned(meeting);
             meeting.finish();
             MeetingDAO.saveMeeting(meeting);
             return Response.ok().entity("The meeting minutes are ready to be signed.").build();
 
         }
         // start meeting
         if ("setActive".equals(querytype) || "attend".equals(querytype)) {
             Meeting meeting = MeetingDAO.loadMeeting(meetingIdToSign);
             if (meeting == null) {
                 return Response.status(Status.BAD_REQUEST).entity("Wrong meeting id.").type("text/plain").build();
             }
             CollaborativeSign collaborativeSign = getCollaborativeSign();
             if ("setActive".equals(querytype)) {
                 Meeting oldMeeting = MeetingDAO.loadMeeting(collaborativeSign.getMeetingId());
                if (oldMeeting != null) {
                    oldMeeting.pause();
                    MeetingDAO.saveMeeting(oldMeeting);
                }
                 collaborativeSign.setMeetingId(meetingIdToSign);
                 setCollaborativeSign(collaborativeSign);
                 meeting.start();
                 meeting.addAttendee(user);
                 MeetingDAO.saveMeeting(meeting);
                 Parameters parameters = new Parameters();
                 parameters.addParameter(GcmMessage.PARAMETER_ACTION, GcmMessage.ACTION_SET_MEETING);
                 parameters.addParameter(GcmMessage.PARAMETER_MESSAGE, "The meeting is set");
                 Gson gson = new Gson();
                 NotificationsSender.sendGCMMessage(getDeviceRegId(collaborativeSign), parameters.toString(), gson.toJson(new MeetingJS(meeting)), null);
                 return Response.ok().entity("The meeting is set.").build();
             }
             if ("attend".equals(querytype)) {
                 attendMeeting(user, meeting, collaborativeSign);
                 return Response.ok().entity("Meeting check-in.").build();
             }
         }
         return Response.status(Status.BAD_REQUEST).entity("Request not understood.").type("text/plain").build();
     }
 
     public static void attendMeeting(CTUser user, Meeting meeting, CollaborativeSign collaborativeSign) {
         log.info("attendMeeting - user:" + user.getUserName() + ", meeting:" + meeting.getSubject());
         meeting.addAttendee(user);
         MeetingDAO.saveMeeting(meeting);
         Parameters parameters = new Parameters();
         parameters.addParameter(GcmMessage.PARAMETER_ACTION, GcmMessage.ACTION_CHECK_IN);
         parameters.addParameter(GcmMessage.PARAMETER_MESSAGE, user.getUserName() + " is present on the meeting.");
         parameters.addParameter(GcmMessage.PARAMETER_USERNAME, user.getUserName());
         parameters.addParameter(GcmMessage.PARAMETER_USER_ID, user.getId().toString());
         NotificationsSender.sendGCMMessage(getDeviceRegId(collaborativeSign), parameters.toString());
     }
 
     private Response postMinute(Long meetingIdToSign, CTUser user, String minute) {
         Meeting meeting = MeetingDAO.loadMeeting(meetingIdToSign);
         if (meeting == null) {
             return Response.status(Status.BAD_REQUEST).entity("Wrong meeting id.").type("text/plain").build();
         }
         meeting.addMinute(user, minute);
         MeetingDAO.saveMeeting(meeting);
         CollaborativeSign collaborativeSign = getCollaborativeSign();
         Parameters parameters = new Parameters();
         parameters.addParameter(GcmMessage.PARAMETER_ACTION, GcmMessage.ACTION_MEETING_MINUTE);
         parameters.addParameter(GcmMessage.PARAMETER_MEETING_ID, meetingIdToSign.toString());
         parameters.addParameter(GcmMessage.PARAMETER_USER_ID, user.getId().toString());
         parameters.addParameter(GcmMessage.PARAMETER_USERNAME, user.getUserName());
         Gson gson = new Gson();
         parameters.addParameter(GcmMessage.PARAMETER_MEETING_MINUTE, minute);
         NotificationsSender.sendGCMMessage(getDeviceRegId(collaborativeSign), parameters.toString(), null, null);
         return Response.ok().entity(gson.toJson(new MeetingJS(meeting))).build();
     }
 
     private static List<String> getDeviceRegId(CollaborativeSign collaborativeSign) {
         List<String> devices = new ArrayList<>();
         devices.add(collaborativeSign.getGcmRegistrationId());
         return devices;
     }
 
     public static CollaborativeSign getCollaborativeSign() {
         CollaborativeSign collaborativeSign = ofy().load().type(CollaborativeSign.class).first().get();
         if (collaborativeSign == null) {
             collaborativeSign = new CollaborativeSign();
         }
         return collaborativeSign;
     }
 
     public static Key<CollaborativeSign> setCollaborativeSign(CollaborativeSign collaborativeSign) {
         return ofy().save().entity(collaborativeSign).now();
     }
 
 }
