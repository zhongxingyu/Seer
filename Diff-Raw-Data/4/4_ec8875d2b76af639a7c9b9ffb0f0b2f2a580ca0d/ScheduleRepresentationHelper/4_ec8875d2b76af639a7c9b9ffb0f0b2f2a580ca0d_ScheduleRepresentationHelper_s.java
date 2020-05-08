 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import java.util.List;
 
 /**
  * @author Jalpa Patel
  */
 // TODO: this is split from the Resources that use it along an odd seam -- the two resources still have a
 // bunch of duplicated code related to building the whole object
 public class ScheduleRepresentationHelper {
     public static JSONObject createJSONStateInfo(ScheduledActivityState state) throws ResourceException{
         try {
             JSONObject stateInfo = new JSONObject();
             stateInfo.put("name", state.getMode().toString());
             stateInfo.put("date", getApiDateFormat().format(state.getDate()));
             stateInfo.put("reason", state.getReason());
             return stateInfo;
         } catch (JSONException e) {
             // TODO: this exception swallows the thrown exception.  Cardinal sin.
 	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	    }
     }
 
     public static JSONObject createJSONActivityProperty(ActivityProperty ap) throws ResourceException {
         try {
             JSONObject jsonAP = new JSONObject();
             jsonAP.put("namespace", ap.getNamespace());
             jsonAP.put("name", ap.getName());
             jsonAP.put("value", ap.getValue());
             return jsonAP;
         } catch (JSONException e) {
             // TODO: this exception swallows the thrown exception.  Cardinal sin.
 	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	    }
 
     }
 
     public static JSONObject createJSONActivity(Activity activity) throws ResourceException{
         try {
             JSONObject jsonActivity = new JSONObject();
             jsonActivity.put("name", activity.getName());
             jsonActivity.put("type", activity.getType().getName());
             if (!activity.getProperties().isEmpty()) {
                 JSONArray properties  = new JSONArray();
                 for (ActivityProperty ap: activity.getProperties()) {
                     properties.put(createJSONActivityProperty(ap));
                 }
                 jsonActivity.put("properties", properties);
             }
             return jsonActivity;
         } catch (JSONException e) {
             // TODO: this exception swallows the thrown exception.  Cardinal sin.
 	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	    }
     }
 
     public static JSONObject createJSONScheduledActivity(ScheduledActivity sa) throws ResourceException {
         try {
             JSONObject jsonSA = new JSONObject();
             if (sa.getGridId() != null) {
                 jsonSA.put("id", sa.getGridId());
             }
             jsonSA.put("study", sa.getScheduledStudySegment().getStudySegment()
                                    .getEpoch().getPlannedCalendar().getStudy().getAssignedIdentifier());
             if (sa.getScheduledStudySegment().getScheduledCalendar().getAssignment() != null) {
                 jsonSA.put("assignment", sa.getScheduledStudySegment().getScheduledCalendar().getAssignment().getName());
             }
             jsonSA.put("study_segment", sa.getScheduledStudySegment().getName());
             jsonSA.put("ideal_date", getApiDateFormat().format(sa.getIdealDate()));
            jsonSA.put("plan_day", sa.getPlannedActivity().getPlanDay());
             jsonSA.put("current_state", createJSONStateInfo(sa.getCurrentState()));
             jsonSA.put("activity", createJSONActivity(sa.getActivity()));
             JSONArray state_history =  new JSONArray();
             for (ScheduledActivityState state : sa.getAllStates()) {
                 state_history.put(createJSONStateInfo(state));
             }
             jsonSA.put("state_history", state_history);
             return jsonSA;
         } catch (JSONException e) {
             // TODO: this exception swallows the thrown exception.  Cardinal sin.
 	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	    }
     }
 
     public static JSONObject createJSONScheduledActivities(Boolean hidden_activities, List<ScheduledActivity> scheduledActivities) throws ResourceException{
         try {
             JSONObject jsonScheduledActivities = new JSONObject();
             if (hidden_activities != null) {
                 jsonScheduledActivities.put("hidden_activities", hidden_activities);
             }
             JSONArray activities = new JSONArray();
             for (ScheduledActivity scheduledActivity: scheduledActivities ) {
                 activities.put(createJSONScheduledActivity(scheduledActivity));
             }
             jsonScheduledActivities.put("activities", activities);
             return jsonScheduledActivities;
         } catch (JSONException e) {
             // TODO: this exception swallows the thrown exception.  Cardinal sin.
 	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	    }
     }
 
     public static JSONObject createJSONStudySegment(ScheduledStudySegment segment) throws ResourceException {
         try {
             JSONObject jsonSegment = new JSONObject();
             jsonSegment.put("name", segment.getName());
             JSONObject jsonRange = new JSONObject();
             jsonRange.put("start_date", getApiDateFormat().format(segment.getDateRange().getStart()));
             jsonRange.put("stop_date", getApiDateFormat().format(segment.getDateRange().getStop()));
             jsonSegment.put("range", jsonRange);
             JSONObject jsonPlannedSegmentInfo = new JSONObject();
             JSONObject jsonPlannedSegment = new JSONObject();
             jsonPlannedSegment.put("id", segment.getStudySegment().getGridId());
             jsonPlannedSegment.put("name", segment.getStudySegment().getName());
             jsonPlannedSegmentInfo.put("segment", jsonPlannedSegment);
             JSONObject jsonEpoch = new JSONObject();
             jsonEpoch.put("id", segment.getStudySegment().getEpoch().getGridId());
             jsonEpoch.put("name", segment.getStudySegment().getEpoch().getName());
             jsonPlannedSegmentInfo.put("epoch", jsonEpoch);
             JSONObject jsonStudy =  new JSONObject();
             jsonStudy.put("assigned_identifier", segment.getStudySegment().getEpoch().
                                                getPlannedCalendar().getStudy().getAssignedIdentifier());
             jsonPlannedSegmentInfo.put("study", jsonStudy);
             jsonSegment.put("planned", jsonPlannedSegmentInfo);
             return jsonSegment;
         } catch (JSONException e) {
             // TODO: this exception swallows the thrown exception.  Cardinal sin.
 	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	    }
     }
 }
