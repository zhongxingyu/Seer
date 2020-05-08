 package uk.ac.cam.dashboard.util;
 
 public class Strings {
 	
 	// User
	public static final String USER_NOUSERNAME = "Anonymous";
	public static final String USER_NOSURNAME = "Anonymous";
 	public static final String USER_NOEMAIL = "No email";
 	public static final String USER_NOINSTID = "None";
 	public static final String USER_NOINST = "No institution";
 	public static final String USER_NOSTATUS = "Student";
 	public static final String USER_NOPHOTO = "none";
 
 	// Deadlines
 	public static final String DEADLINE_NOMESSAGE = "No message";
 	public static final String DEADLINE_NOURL = "#";
 	public static final String DEADLINE_DELETED = "Deadline deleted by owner";
 	// Deadlines set mail
 	public static final String MAIL_SETDEADLINE_SUBJECT = " set you a deadline on OTTer ";
 	public static final String MAIL_SETDEADLINE_HEADER = "You have been set a new deadline on Cam OTTer (Online teaching tools). The deadline details are below. To view the deadline, click the link to go to the CAM Supervisions app.";	
 	public static final String MAIL_SETDEADLINE_FOOTER = "This is an automatically generated message. All replies will be directed to the setter of this deadline.";		
 	// Deadlines update mail
 	public static final String MAIL_UPDATEDEADLINE_SUBJECT = " has updated a deadline on OTTer ";
 	public static final String MAIL_UPDATEDEADLINE_HEADER = "A deadline you have been set has been updated Cam OTTer (Online teaching tools). The deadline details are below. To view the deadline, click the link to go to the CAM Supervisions app.";	
 	public static final String MAIL_UPDATEDEADLINE_FOOTER = "This is an automatically generated message. All replies will be directed to the setter of this deadline.";		
 	// Deadlines remind mail
 	public static final String MAIL_REMINDDEADLINE_SUBJECT = " reminded you about a deadline on OTTer ";
 	public static final String MAIL_REMINDDEADLINE_HEADER = "You have been sent a reminder about a deadline on Cam OTTer (Online teaching tools). The deadline details are below. To view the deadline, click the link to go to the CAM Supervisions app.";	
 	public static final String MAIL_REMINDDEADLINE_FOOTER = "This is an automatically generated message. All replies will be directed to the setter of this deadline.";
 	// Deadline get form
 	public static final String DEADLINE_OFFSET_INVALID_NUM = "Offset must be greater than or equal to 0";
 	public static final String DEADLINE_OFFSET_NOT_INTEGER = "Offset must be an integer";
 	public static final String DEADLINE_LIMIT_INVALID_NUM = "Limit must be greater than or equal to 0";
 	public static final String DEADLINE_LIMIT_NOT_INTEGER = "Limit must be an integer";
 	// Deadline create form
 	public static final String DEADLINE_NO_TITLE = "Please give your deadline a name";
 	public static final String DEADLINE_TITLE_LENGTH = "Name cannot be longer than 255 characters";
 	public static final String DEADLINE_NO_DATE = "Please choose a due date for the deadline";
 	public static final String DEADLINE_DATE_PASSED = "Deadline due date cannot be in the past";
 	public static final String DEADLINE_NO_USERS = "You must assign at least one user to this deadline";
 	
 	// Groups
 	public static final String GROUP_AUTHEDIT = "You are not authorised to edit this group";
 	// Group create form
 	public static final String GROUP_NO_TITLE = "Please give your group a name";
 	public static final String GROUP_TITLE_LENGTH = "Name cannot be longer than 255 characters";
 	public static final String GROUP_NO_USERS = "You must add at least one user to this group";
 	// Group import form
 	public static final String GROUP_IMPORT_NONE = "Please choose at least one group to import";
 	public static final String GROUP_TOO_LARGE = "Group size too large: maximum group size is 100 members";
 	public static final String GROUP_NO_MEMBERS = "Group contains no members";
 	public static final String GROUP_CANNOT_RETRIEVE = "Group cannot be retrieved from LDAP";
 	
 	// Notifications
 	public static final String NOTIFICATION_SETGROUP = " added you to a group: ";
 	public static final String NOTIFICATION_UPDATEGROUP = " updated a group you are in: ";
 	public static final String NOTIFICATION_SETDEADLINE = " set you a deadline: ";
 	public static final String NOTIFICATION_UPDATEDEADLINE = " updated a deadline: ";
 	public static final String NOTIFICATION_GET_ERROR_INDIVIDUAL = "Could not find a notification with id ";
 	public static final String NOTIFICATION_UPDATE_ERROR = "Could not mark notification as ";
 	public static final String NOTIFICATION_UPDATE_NO_READ_PARAM = "Please include a value for the query parameter 'read'";
 	// Notification get form
 	public static final String NOTIFICATION_OFFSET_INVALID_NUM = "Offset must be greater than or equal to 0";
 	public static final String NOTIFICATION_OFFSET_NOT_INTEGER = "Offset must be an integer";
 	public static final String NOTIFICATION_LIMIT_INVALID_NUM = "Limit must be greater than or equal to 0";
 	public static final String NOTIFICATION_LIMIT_NOT_INTEGER = "Limit must be an integer";
 	public static final String NOTIFICATION_INVALID_SECTION = "Invalid section field";
 	// Notification create form
 	public static final String NOTIFICATION_NO_MESSAGE = "Message field cannot be empty";
 	public static final String NOTIFICATION_MESSAGE_LENGTH = "Message length cannot be more than 255 characters";
 	public static final String NOTIFICATION_NO_SECTION = "Section field cannot be empty";
 	public static final String NOTIFICATION_NO_LINK = "Link field cannot be empty";
 	public static final String NOTIFICATION_NO_USERS = "A list of comma separated users must be set";
 	public static final String NOTIFICATION_USERS_MAX = "A maximum of 50 users can be set for any notification";
 	public static final String NOTIFICATION_FOREIGN_ID_NOT_UNIQUE = "Foreign id must be unique";
 	// Notification mail
 	public static final String MAIL_NOTIFICATION_SUBJECT = "[OTTER] ";
 	public static final String MAIL_NOTIFICATION_HEADER = "Click the link below to go to your OTTER notifications";	
 	public static final String MAIL_NOTIFICATION_FOOTER = "This is an automatically generated message from OTTER (University of Cambridge Online Teaching Tools). To unsubscribe, log in to http://ott.cl.cam.ac.uk and change preferences in your account page";		
 	
 	// Authorisation exceptions
 	public static final String AUTHEXCEPTION_GLOBAL = "Could not validate global API permissions.";
 	public static final String AUTHEXCEPTION_GLOBAL_USER = "Could not validate API permissions for this user. Please include a valid crsid with your query.";
 	public static final String AUTHEXCEPTION_USER = "Could not validate permissions for this user.";
 	public static final String AUTHEXCEPTION_GENERAL = "Error validating permissions.";
 	public static final String AUTHEXCEPTION_NO_KEY = "A key must be provided";
 	
 	// API keys
 	public static final String APIKEY_MAX = "Maximum number of API keys reached, please delete some to generate more.";
 	
 	// Help/About
 	public static final String ABOUT_INFO = "Cambridge Online Teaching Tools or OTTER for short is a project developed by the University of Cambridge Computer Labratory to assist supervisors with organising and managing supervisons. "
 			+ "The Otter software has been built as a summer UROP project and is planned to be used by from October 2013. "
 			+ "The system is open to anyone with a Raven account so can be used by supervisors and students from all departments in the University. ";
 	public static final String ABOUT_MOREINFO = "For more information visit the project page on the DTG website: ";
 	public static final String ABOUT_DTGLINK = "http://www.cl.cam.ac.uk/research/dtg/summer/";
 	public static final String ABOUT_GIT = "Otter is an open source project and is available on GitHub; links to the individual projects are below:";
 	public static final String[] ABOUT_GITLINKS = {"https://github.com/ucam-cl-dtg/urop-2013-dashboard",
 														"https://github.com/ucam-cl-dtg/urop-2013-signups",
 														"https://github.com/ucam-cl-dtg/urop-2013-questions",
 														"https://github.com/ucam-cl-dtg/urop-2013-handins"};
 
 	public static final String HELP_INFO = "If you need help using any of the features or have ideas on how to improve Otter, email us at the address below.";
 	public static final String HELP_EMAIL = "ott-admin@cl.cam.ac.uk";
 	
 }
