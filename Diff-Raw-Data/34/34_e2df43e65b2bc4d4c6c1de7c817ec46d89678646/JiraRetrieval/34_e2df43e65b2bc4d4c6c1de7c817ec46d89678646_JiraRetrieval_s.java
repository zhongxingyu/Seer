 /*
  * Copyright (C) 2012-2013 GSyC/LibreSoft, Universidad Rey Juan Carlos
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
  *
  */
 
 package eu.alertproject.kesi.database;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import eu.alertproject.kesi.model.Activity;
 import eu.alertproject.kesi.model.Issue;
 import eu.alertproject.kesi.model.Person;
 
 public class JiraRetrieval extends ITSRetrieval {
     /* JIRA fields */
     private static final String ITS_JIRA_LINK = "link";
 
     /* JIRA values for changes */
     private static final String JIRA_CHANGE_ASSIGNEE = "Assignee";
     private static final String JIRA_CHANGE_PRIORITY = "Priority";
     private static final String JIRA_CHANGE_STATUS = "Status";
     private static final String JIRA_CHANGE_SUMMARY = "Summary";
     private static final String JIRA_CHANGE_RESOLUTION = "Resolution";
 
     /* JIRA values for status field */
     private static final String JIRA_STATUS_CLOSED = "Closed";
     private static final String JIRA_STATUS_INACCURATE = "Inaccurate";
     private static final String JIRA_STATUS_IN_PROGRESS = "In Progress";
     private static final String JIRA_STATUS_NEW = "New";
     private static final String JIRA_STATUS_RESOLVED = "Resolved";
     private static final String JIRA_STATUS_OPEN = "Open";
 
     /* JIRA values for resolution field */
     private static final String JIRA_RESOLUTION_FIXED = "Fixed";
     private static final String JIRA_RESOLUTION_T_FIX = "t Fix";
     private static final String JIRA_RESOLUTION_COMPLETED = "Completed";
     private static final String JIRA_RESOLUTION_RESOLVED_LOCALLY = "Resolved Locally";
     private static final String JIRA_RESOLUTION_CANT_REPRODUCE = "Cannot Reproduce";
     private static final String JIRA_RESOLUTION_INCOMPLETE = "Incomplete";
     private static final String JIRA_RESOLUTION_DUPLICATE = "Duplicate";
     private static final String JIRA_RESOLUTION_UNRESOLVED = "Unresolved";
     private static final String JIRA_RESOLUTION_TIMEOUT = "Time Out";
 
     /* JIRA values for severity field */
     private static final String JIRA_SEVERITY_TRIVIAL = "Trivial";
     private static final String JIRA_SEVERITY_MINOR = "Minor";
     private static final String JIRA_SEVERITY_NORMAL = "Normal";
     private static final String JIRA_SEVERITY_MAJOR = "Major";
     private static final String JIRA_SEVERITY_CRITICAL = "Critical";
     private static final String JIRA_SEVERITY_BLOCKER = "Blocker";
 
     /* JIRA values for priority field */
     private static final String JIRA_PRIORITY_TRIVIAL = "Trivial";
     private static final String JIRA_PRIORITY_MINOR = "Minor";
     private static final String JIRA_PRIORITY_MAJOR = "Major";
     private static final String JIRA_PRIORITY_CRITICAL = "Critical";
     private static final String JIRA_PRIORITY_BLOCKER = "Blocker";
 
     /* Specific queries for JIRA */
     private static final String ITS_QUERY_ISSUE_JIRA = "SELECT link, component, status, resolution "
             + "FROM issues_ext_jira WHERE issue_id = ?";
 
     public JiraRetrieval(String driver, String username, String password,
             String host, String port, String database)
             throws DriverNotSupportedError, DatabaseConnectionError {
         super(driver, username, password, host, port, database);
     }
 
     @Override
     public Issue getEventIssueNew(int issueID) throws DatabaseExtractionError {
         try {
             return getBasicIssueNew(issueID);
         } catch (SQLException e) {
             String msg = "Error getting issue from event " + issueID + "."
                     + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
     }
 
     @Override
     public Issue getEventIssueUpdateComment(int commentID, int issueID)
             throws DatabaseExtractionError {
         try {
             return getBasicIssueUpdateComment(commentID, issueID);
         } catch (SQLException e) {
             String msg = "Error getting comment " + commentID + "."
                     + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
     }
 
     @Override
     public Issue getEventIssueUpdateChange(int changeID, int issueID)
             throws DatabaseExtractionError {
         try {
             Issue issue = getBasicIssueUpdateChange(changeID, issueID);
             Activity change = issue.getActivity().get(0);
             String what = change.getWhat();
             String newValue = change.getNewValue();
 
             if (what.equals(JIRA_CHANGE_ASSIGNEE)) {
                 Person assignedTo = new Person(newValue, "", "");
                 issue.setAssignedTo(assignedTo);
             } else if (what.equals(JIRA_CHANGE_PRIORITY)) {
                 issue.setPriority(toPriority(newValue));
             } else if (what.equals(JIRA_CHANGE_RESOLUTION)) {
                 issue.setResolution(toResolution(newValue));
             } else if (what.equals(JIRA_CHANGE_STATUS)) {
                 issue.setState(toStatus(newValue));
             } else if (what.equals(JIRA_CHANGE_SUMMARY)) {
                 issue.setDescription(newValue);
             }
 
             return issue;
         } catch (SQLException e) {
             String msg = "Error getting change " + changeID + "."
                     + e.getMessage();
             logger.error(msg, e);
             throw new DatabaseExtractionError(msg);
         }
     }
 
     @Override
     protected String toStatus(String value) {
         if (value.equals(JIRA_STATUS_CLOSED)) {
             return Issue.CLOSED;
         } else if (value.equals(JIRA_STATUS_INACCURATE)) {
             return Issue.OPEN;
         } else if (value.equals(JIRA_STATUS_IN_PROGRESS)) {
             return Issue.ASSIGNED;
         } else if (value.equals(JIRA_STATUS_NEW)) {
             return Issue.OPEN;
         } else if (value.equals(JIRA_STATUS_OPEN)) {
             return Issue.OPEN;
         } else if (value.equals(JIRA_STATUS_RESOLVED)) {
             return Issue.RESOLVED;
         } else {
             return Issue.UNKNOWN;
         }
     }
 
     @Override
     protected String toResolution(String value) {
         if (value.equals(JIRA_RESOLUTION_FIXED)) {
             return Issue.FIXED;
         } else if (value.equals(JIRA_RESOLUTION_T_FIX)) {
             return Issue.FIXED;
         } else if (value.equals(JIRA_RESOLUTION_COMPLETED)) {
             return Issue.FIXED;
         } else if (value.equals(JIRA_RESOLUTION_RESOLVED_LOCALLY)) {
             return Issue.FIXED;
         } else if (value.equals(JIRA_RESOLUTION_CANT_REPRODUCE)) {
             return Issue.INVALID;
         } else if (value.equals(JIRA_RESOLUTION_INCOMPLETE)) {
             return Issue.INVALID;
         } else if (value.equals(JIRA_RESOLUTION_DUPLICATE)) {
             return Issue.DUPLICATED;
         } else if (value.equals(JIRA_RESOLUTION_UNRESOLVED)) {
             return Issue.WONT_FIX;
         } else if (value.equals(JIRA_RESOLUTION_TIMEOUT)) {
             return Issue.WONT_FIX;
         } else if (value.equals("")) {
             return Issue.NONE;
         } else {
             return Issue.UNKNOWN;
         }
     }
 
     @Override
     protected String toSeverity(String value) {
         if (value.equals(JIRA_SEVERITY_TRIVIAL)) {
             return Issue.TRIVIAL;
         } else if (value.equals(JIRA_SEVERITY_MINOR)) {
             return Issue.MINOR;
         } else if (value.equals(JIRA_SEVERITY_NORMAL)) {
             return Issue.NORMAL;
         } else if (value.equals(JIRA_SEVERITY_MAJOR)) {
             return Issue.MAJOR;
         } else if (value.equals(JIRA_SEVERITY_CRITICAL)) {
             return Issue.CRITICAL;
         } else if (value.equals(JIRA_SEVERITY_BLOCKER)) {
             return Issue.BLOCKER;
         } else if (value.equals("")) {
             return Issue.NONE;
         } else {
             return Issue.UNKNOWN;
         }
     }
 
     @Override
     protected String toPriority(String value) {
         if (value.equals(JIRA_PRIORITY_TRIVIAL)) {
             return Issue.LOWEST;
         } else if (value.equals(JIRA_PRIORITY_MINOR)) {
             return Issue.LOW;
         } else if (value.equals(JIRA_PRIORITY_MAJOR)) {
             return Issue.MEDIUM;
         } else if (value.equals(JIRA_PRIORITY_CRITICAL)) {
             return Issue.HIGH;
         } else if (value.equals(JIRA_PRIORITY_BLOCKER)) {
             return Issue.HIGHEST;
         } else if (value.equals("")) {
             return Issue.LOWEST;
         } else {
             return Issue.UNKNOWN;
         }
     }
 
     @Override
    protected URI getIssueURL(int issueID) throws SQLException {
         String link;
        URI issueURL;
         PreparedStatement stmt;
         ResultSet rs;
 
         stmt = prepareStatement(ITS_QUERY_ISSUE_JIRA);
         stmt.setInt(1, issueID);
         rs = executeQuery(stmt);
         rs.first();
 
         link = rs.getString(ITS_JIRA_LINK);
 
         stmt.close();
 
         try {
            issueURL = new URI(link);
         } catch (URISyntaxException e) {
             logger.error("Error converting URI for issue " + issueID
                     + ". Set to null.", e);
            issueURL = null;
         }

        return issueURL;
     }
 
 }
