 /*
  * Copyright (C) 2011-2013 GSyC/LibreSoft, Universidad Rey Juan Carlos
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  *
  * Authors: Santiago Dueñas <sduenas@libresoft.es>
  *          Luis Cañas Díaz <lcanas@bitergia.com>
  *
  */
 
 package eu.alertproject.kesi.database;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 
 import eu.alertproject.kesi.events.EventSummary;
 import eu.alertproject.kesi.model.Activity;
 import eu.alertproject.kesi.model.Comment;
 import eu.alertproject.kesi.model.Issue;
 import eu.alertproject.kesi.model.IssueTracker;
 import eu.alertproject.kesi.model.Person;
 
 public abstract class ITSRetrieval extends Database {
     /* Event summary types */
     private static final String EVENT_TYPE_ISSUE = "issue";
     private static final String EVENT_TYPE_COMMENT = "comment";
     private static final String EVENT_TYPE_CHANGE = "change";
 
     /* Event summary row fields */
     private static final String ITS_EVENTS_ID = "event_id";
     private static final String ITS_EVENTS_ISSUE_ID = "issue_id";
     private static final String ITS_EVENTS_DATE = "date";
     private static final String ITS_EVENTS_TYPE = "type";
 
     /* Issue row fields */
     private static final String ITS_ISSUE_PUBLIC_ID = "issue";
     private static final String ITS_ISSUE_SUMMARY = "summary";
     private static final String ITS_ISSUE_DESCRIPTION = "description";
     private static final String ITS_ISSUE_STATUS = "status";
     private static final String ITS_ISSUE_RESOLUTION = "resolution";
     private static final String ITS_ISSUE_PRIORITY = "priority";
     private static final String ITS_ISSUE_SEVERITY = "type"; // FIXME
     private static final String ITS_ISSUE_ASSIGNED_TO = "assigned_to";
     private static final String ITS_SUBMITTED_BY = "submitted_by";
     private static final String ITS_SUBMITTED_ON = "submitted_on";
 
     /* Comment row fields */
     private static final String ITS_COMMENT_TEXT = "text";
 
     /* Person row fields */
     private static final String ITS_PERSON_NAME = "name";
     private static final String ITS_PERSON_EMAIL = "email";
     private static final String ITS_PERSON_USER_ID = "user_id";
 
     /* Tracker row fields */
     private static final String ITS_TRACKER_ID = "tracker_id";
     private static final String ITS_TRACKER_TYPE = "name";
     private static final String ITS_TRACKER_URL = "url";
 
     /* Activity row fields */
     private static final String ITS_ACTIVITY_WHO = "email";
     private static final String ITS_ACTIVITY_WHEN = "changed_on";
     private static final String ITS_ACTIVITY_WHAT = "field";
     private static final String ITS_ACTIVITY_OLD = "old_value";
     private static final String ITS_ACTIVITY_NEW = "new_value";
 
     /* ITS generic queries */
     private static final String ITS_QUERY_EVENTS = "SELECT log.id event_id, log.issue_id issue_id, log.date date, log.type type FROM "
             + "((SELECT id, issue_id, date, 'issue' type FROM issues_log GROUP BY issue_id ORDER BY date) "
             + "UNION "
             + "(SELECT id, issue_id, submitted_on date, 'comment' type FROM comments) "
             + "UNION "
             + "(SELECT id, issue_id, changed_on date, 'change' type FROM changes)) log, issues i, trackers t "
             + "WHERE log.issue_id = i.id AND i.tracker_id = t.id "
             + "AND t.url = ? AND log.date > ? ORDER BY log.date";
     private static final String ITS_QUERY_EVENT_NEW_ISSUE = "SELECT issue_id id, issue, summary, description,"
             + "status, resolution, priority, submitted_by, date submitted_on,"
             + "assigned_to, type, tracker_id "
             + "FROM issues_log WHERE issue_id = ? ORDER BY submitted_on LIMIT 1";
     private static final String ITS_QUERY_EVENT_COMMENT = "SELECT issue_id, text, submitted_by, submitted_on "
             + "FROM comments WHERE id = ?";
     private static final String ITS_QUERY_EVENT_CHANGE = "SELECT people.email,"
             + "changes.changed_on, changes.field, changes.old_value,"
             + "changes.new_value, changes.issue_id " + "FROM changes, people "
             + "WHERE changes.id = ? AND changes.changed_by = people.id";
     private static final String ITS_QUERY_BASIC_ISSUE = "SELECT issue, tracker_id "
             + "FROM issues WHERE issues.id = ?";
     private static final String ITS_QUERY_TRACKER = "SELECT url, name "
             + "FROM trackers, supported_trackers "
             + "WHERE trackers.type = supported_trackers.id AND trackers.id = ?";
     private static final String ITS_QUERY_ISSUE_TRACKER = "SELECT url, name, tracker_id "
             + "FROM trackers, supported_trackers, issues "
             + "WHERE trackers.type = supported_trackers.id "
             + "AND trackers.id = issues.tracker_id AND issues.id = ?";
     private static final String ITS_QUERY_PERSON = "SELECT name, email, user_id "
             + "FROM people WHERE id = ?";
 
     /* Logger */
     private static Logger itsDBLogger = Logger.getLogger(ITSRetrieval.class);
 
     /* Cache */
     private final HashMap<Integer, Person> people;
     private final HashMap<Integer, IssueTracker> trackers;
 
     /* Abstract methods */
     public abstract Issue getEventIssueNew(int issueID)
             throws DatabaseExtractionError;
 
     public abstract Issue getEventIssueUpdateComment(int commentID, int issueID)
             throws DatabaseExtractionError;
 
     public abstract Issue getEventIssueUpdateChange(int changeID, int issueID)
             throws DatabaseExtractionError;
 
    protected abstract URI getIssueURL(int issueID, String publicID)
            throws SQLException;
 
     protected abstract String toStatus(String value);
 
     protected abstract String toResolution(String value);
 
     protected abstract String toSeverity(String value);
 
     protected abstract String toPriority(String value);
 
     /* Public and protected methods */
     public ITSRetrieval(String driver, String username, String password,
             String host, String port, String database)
             throws DriverNotSupportedError, DatabaseConnectionError {
         super(driver, username, password, host, port, database);
         super.logger = itsDBLogger;
         people = new HashMap<Integer, Person>();
         trackers = new HashMap<Integer, IssueTracker>();
     }
 
     public ArrayList<EventSummary> getEventsSummary(String trackerURL,
             Timestamp lastSent) throws DatabaseExtractionError {
         ArrayList<EventSummary> events = new ArrayList<EventSummary>();
 
         try {
             PreparedStatement stmt;
             ResultSet rs;
 
             stmt = prepareStatement(ITS_QUERY_EVENTS);
             stmt.setString(1, trackerURL);
             stmt.setString(2, Database.dateToString(lastSent));
             rs = executeQuery(stmt);
 
             while (rs.next()) {
                 EventSummary summary = createEventSummary(rs);
                 events.add(summary);
             }
 
             stmt.close();
 
             return events;
         } catch (SQLException e) {
             String msg = "Error getting summary of issues. " + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
     }
 
     public Issue getIssueFromSummary(EventSummary summary)
             throws DatabaseExtractionError {
         int eventID;
         int issueID;
         int type;
         Issue issue;
 
         eventID = summary.getEventID();
         issueID = summary.getEventKey();
         type = summary.getType();
 
         if (type == EventSummary.ISSUE_NEW) {
             issue = getEventIssueNew(issueID);
         } else if (type == EventSummary.ISSUE_COMMENT) {
             issue = getEventIssueUpdateComment(eventID, issueID);
         } else if (type == EventSummary.ISSUE_CHANGE) {
             issue = getEventIssueUpdateChange(eventID, issueID);
         } else {
             String msg = "Invalid event type " + type;
             throw new DatabaseExtractionError(msg);
         }
 
         return issue;
     }
 
     protected Issue getBasicIssueNew(int issueID) throws SQLException {
         Timestamp submittedOn;
         String summary;
         String desc;
         String status;
         String resolution;
         String priority;
         String severity;
         int submitterID;
         int assignedID;
         Person submittedBy;
         Person assignedTo;
         Issue issue;
         Comment description;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_EVENT_NEW_ISSUE);
         stmt.setInt(1, issueID);
         rs = executeQuery(stmt);
         rs.first();
 
         submittedOn = rs.getTimestamp(ITS_SUBMITTED_ON);
         summary = rs.getString(ITS_ISSUE_SUMMARY);
         desc = rs.getString(ITS_ISSUE_DESCRIPTION);
         status = rs.getString(ITS_ISSUE_STATUS);
         resolution = rs.getString(ITS_ISSUE_RESOLUTION);
         priority = rs.getString(ITS_ISSUE_PRIORITY);
         severity = rs.getString(ITS_ISSUE_SEVERITY);
         submitterID = rs.getInt(ITS_SUBMITTED_BY);
         assignedID = rs.getInt(ITS_ISSUE_ASSIGNED_TO);
 
         stmt.close();
 
         submittedBy = getPerson(submitterID);
         assignedTo = getPerson(assignedID);
 
         issue = getBasicIssue(issueID);
         issue.setIssueNew(submittedOn.toString());
 
         /*
          * @sduenas: As was agreed on 2012/09/04 by ALERT technical
          * partners, description will be the first comment of the
          * issue. For the record, I don't like to include this
          * "magic code" here but...
          */
         description = new Comment(desc, submittedBy, submittedOn.toString());
         issue.addComment(description);
 
         /*
          * @sduenas: Following the previous comment, summary will be
          * the description
          */
         issue.setDescription(summary);
         issue.setDateOpened(submittedOn);
         issue.setState(toStatus(status));
         issue.setResolution(toResolution(resolution));
         issue.setPriority(toPriority(priority));
         issue.setSeverity(toSeverity(severity));
         issue.setReporter(submittedBy);
         issue.setAssignedTo(assignedTo);
 
         return issue;
     }
 
     protected Issue getBasicIssueUpdateComment(int commentID, int issueID)
             throws SQLException {
         Comment comment = getComment(commentID);
         Issue issue = getBasicIssue(issueID);
 
         issue.setIssueUpdate(comment.getDate().toString());
         issue.addComment(comment);
 
         return issue;
     }
 
     protected Issue getBasicIssueUpdateChange(int changeID, int issueID)
             throws SQLException {
         Activity change = getChange(changeID);
         Issue issue = getBasicIssue(issueID);
 
         issue.setIssueUpdate(change.getWhen());
         issue.addActivity(change);
 
         return issue;
     };
 
     protected Issue getBasicIssue(int issueID) throws SQLException {
         String publicID;
         int trackerID;
         Issue issue;
         IssueTracker tracker;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_BASIC_ISSUE);
         stmt.setInt(1, issueID);
         rs = executeQuery(stmt);
         rs.first();
 
         publicID = rs.getString(ITS_ISSUE_PUBLIC_ID);
         trackerID = rs.getInt(ITS_TRACKER_ID);
 
         stmt.close();
 
         issue = new Issue(publicID);
        issue.setIssueURL(getIssueURL(issueID, publicID));
 
         tracker = getTracker(trackerID);
         issue.setIssueTracker(tracker);
 
         return issue;
     }
 
     protected Comment getComment(int commentID) throws SQLException {
         int submitterID;
         Timestamp date;
         String text;
         Person person;
         Comment comment;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_EVENT_COMMENT);
         stmt.setInt(1, commentID);
         rs = executeQuery(stmt);
         rs.first();
 
         submitterID = rs.getInt(ITS_SUBMITTED_BY);
         date = rs.getTimestamp(ITS_SUBMITTED_ON);
         text = rs.getString(ITS_COMMENT_TEXT);
 
         stmt.close();
 
         person = getPerson(submitterID);
 
         comment = new Comment(text, person, date.toString());
 
         return comment;
     }
 
     protected Activity getChange(int id) throws SQLException {
         String who;
         String when;
         String what;
         String oldValue;
         String newValue;
         Activity activity;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_EVENT_CHANGE);
         stmt.setInt(1, id);
         rs = executeQuery(stmt);
         rs.first();
 
         who = rs.getString(ITS_ACTIVITY_WHO);
         when = rs.getString(ITS_ACTIVITY_WHEN);
         what = rs.getString(ITS_ACTIVITY_WHAT);
         oldValue = rs.getString(ITS_ACTIVITY_OLD);
         newValue = rs.getString(ITS_ACTIVITY_NEW);
 
         stmt.close();
 
         activity = new Activity(who, when, what, oldValue, newValue);
 
         return activity;
     }
 
     protected IssueTracker getTracker(int trackerID) throws SQLException {
         IssueTracker tracker;
         PreparedStatement stmt;
         ResultSet rs;
 
         if (trackers.containsKey(trackerID)) {
             return trackers.get(trackerID);
         }
 
         stmt = prepareStatement(ITS_QUERY_TRACKER);
         stmt.setInt(1, trackerID);
         rs = executeQuery(stmt);
         rs.first();
 
         tracker = createTracker(trackerID, rs);
 
         stmt.close();
 
         return tracker;
     }
 
     protected IssueTracker getTrackerFromIssue(int issueID) throws SQLException {
         int trackerID;
         IssueTracker tracker;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_ISSUE_TRACKER);
         stmt.setInt(1, issueID);
         rs = executeQuery(stmt);
         rs.first();
 
         trackerID = rs.getInt(ITS_TRACKER_ID);
 
         if (trackers.containsKey(trackerID)) {
             return trackers.get(trackerID);
         }
 
         tracker = createTracker(trackerID, rs);
 
         stmt.close();
 
         return tracker;
     }
 
     protected IssueTracker createTracker(int trackerID, ResultSet rs)
             throws SQLException {
         String trackerType;
         URI trackerURI;
         IssueTracker tracker;
 
         trackerType = rs.getString(ITS_TRACKER_TYPE);
 
         try {
             String url = rs.getString(ITS_TRACKER_URL);
             trackerURI = new URI(url);
         } catch (URISyntaxException e) {
             logger.error("Error converting URI for tracker " + trackerID
                     + ". Set to null.", e);
             trackerURI = null;
         }
 
         tracker = new IssueTracker(Integer.toString(trackerID), trackerURI,
                 trackerType);
         trackers.put(trackerID, tracker);
 
         return tracker;
     }
 
     protected Person getPerson(int userID) throws SQLException {
         String name;
         String email;
         String userITS;
         Person person;
         PreparedStatement stmt;
         ResultSet rs;
 
         if (people.containsKey(userID)) {
             return people.get(userID);
         }
 
         stmt = prepareStatement(ITS_QUERY_PERSON);
         stmt.setInt(1, userID);
         rs = executeQuery(stmt);
 
         rs.first();
 
         name = rs.getString(ITS_PERSON_NAME);
         email = rs.getString(ITS_PERSON_EMAIL);
         userITS = rs.getString(ITS_PERSON_USER_ID);
 
         stmt.close();
 
         person = new Person(name, email, userITS);
         people.put(userID, person);
 
         return person;
     }
 
     private EventSummary createEventSummary(ResultSet rs)
             throws DatabaseExtractionError {
         int eventID;
         int issueID;
         int eventType;
         String type;
         Timestamp date;
         EventSummary summary;
 
         try {
             eventID = rs.getInt(ITS_EVENTS_ID);
             issueID = rs.getInt(ITS_EVENTS_ISSUE_ID);
             type = rs.getString(ITS_EVENTS_TYPE);
             date = rs.getTimestamp(ITS_EVENTS_DATE);
         } catch (SQLException e) {
             String msg = "Error getting summary of events. " + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
 
         if (type.equals(EVENT_TYPE_ISSUE)) {
             eventType = EventSummary.ISSUE_NEW;
         } else if (type.equals(EVENT_TYPE_COMMENT)) {
             eventType = EventSummary.ISSUE_COMMENT;
         } else if (type.equals(EVENT_TYPE_CHANGE)) {
             eventType = EventSummary.ISSUE_CHANGE;
         } else {
             String msg = "Invalid type of event " + type;
             throw new DatabaseExtractionError(msg);
         }
 
         summary = new EventSummary(eventID, eventType, date);
         summary.setEventKey(issueID);
 
         return summary;
     }
 
 }
