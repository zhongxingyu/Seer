 package com.skybot.util;
 
 public class Constants {
 
 	// ---------------------------- General HTTP Request Information
 	// ----------------//
 
	public static String SERVER_URL = "http://212.34.250.251:8008/skybot-scheduler";// "http://192.168.0.5:8008/skybot-scheduler";//
																					// "http://192.168.0.146:3000";//"http://192.168.0.152:3000";
 																					// //"http://192.168.0.5:8008/skybot-scheduler";//"http://192.168.0.5:8008/skybot-scheduler";
 																					// //
 
 	public static String API_KEY = "";
 	public static final String EQUAL = "=";
 	public static final String FIRST_PARAM_SEPARATOR = "?";
 	public static final String PARAMETER_SEPARATOR = "&";
 	public static final String RIGHT_SLASH = "/";
 
 	// ---------------------------- Services Names & Params ----------------//
 
 	public static final String LOGIN_SERVICE = "user_session";
 	public static final String JOB_SERVICE = "jobs";
 	public static final String AUTH_TOKEN = "authenticity_token";
 	public static final String USERNAME = "user_session[username]";
 	public static final String PASSWORD = "user_session[password]";
 
 	public static final String DASHBOARD_SERVICE = "dashboards";
 
 	public static final String COMMIT = "commit";
 	public static final String ADMIN = "admin";
 	public static final String JOBHISTORYREPORT_SERVICE_URL = "user_files.json";
 	public static final String JOBHISTORY_SERVICE_URL = "job_histories.json";
 	public static final String AGENT_SERVICE_URL = "agents.json";
 	public static final String AGENT_SERVICE = "agents";
 	public static final String JOB_SERVICE_URL = "jobs.json";
 	public static final String SIGN_OUT = "signout";
 	public static final String DASHBOARD_SERVICE_URL = "show.json";
 	public static final String DATE = "_dc";
 	public static final String USERID = "user_id";
 	public static final String RESULTS = "results";
 	public static final String SORT = "sort";
 	public static final String DIRECTION = "dir";
 	public static final String TAG = "tag";
 	public static final String TAG_MATCH_ANY = "tag_match_any";
 	public static final String START = "start";
 	public static final String LIMIT = "limit";
 	public static final String LIST = "list";
 	public static final String DATAFILTERFIELD = "dateFilter[0][field]";
 	public static final String DATAFILTERDATACOMPARASION = "dateFilter[0][data][comparison]";
 	public static final String DATAFILTERDATATYPE = "dateFilter[0][data][type]";
 	public static final String DATAFILTERVALUE = "dateFilter[0][data][value]";
 	public static final String EXCLUDETIMEDINTERVAL = "exclude_timed_interval";
 
 	public static final String MORE_JOBS = "MORE_JOBS";
 	public static final String MORE_JOB_HISTORIES = "MORE_JOB_HISTORIES";
 	public static final String MORE_JOB_HISTORIES_REPORTS = "MORE_JOB_HISTORIES_REPORTS";
 
 	public static final String PUSH_NOTIFICATION_SERVICE_URL = "device_token";
 	public static final String PUSH_NOTIFICATION_SERVICE = "Push Service";
 
 	// ---------------------------- Run Job Params --------------------------//
 
 	public static final String JOB_DETAILS_URL = "jobs/1000/edit.json";
 	public static final String COMMAND_URL = "commands.json";
 
 	// ---------------------------- Run Job Params --------------------------//
 
 	public static final String RUN_JOB_URL = "commands.json";
 	public static final String CS_ID = "command_set_id";
 	public static final String CS_TYPE = "command_set_type";
 	public static final String JOB_ID = "job_id";
 
 	public static final String HOLD_JOB = "hold_job";
 	public static final String RELEASE_JOB = "release_job";
 
 	// ---------------------------- Views ----------------------------------//
 
 	public static final String DO_WHAT = "do_what";
 	public static final String DO_NOW = "donow";
 
 	// ---------------------------- Views ----------------------------------//
 
 	public static final String LOGIN_VIEW = "LoginView";
 	public static final String LOGOUT_VIEW = "LogoutView";
 	public static final String JOBS_VIEW = "JobsView";
 	public static final String DASHBOARD_VIEW = "DashboardView";
 	public static final String JOBSHISTORY_VIEW = "JobsHistoryView";
 	public static final String AGENT_VIEW = "AgentView";
 	public static final String JOBHISTORYREPORT_VIEW = "JobHistoryReportView";
 
 	// ---------------- Chart ids ------------------------------------------//
 	public static final String COMPLETED_JOBS_ID = "4";
 	public static final String TERMINATED_JOBS_ID = "5";
 	public static final String SUBMITTED_JOBS_ID = "1";
 	public static final String AGENT_EVENT_PROCESSED_ID = "10";
 
 	// ---------------- Chart sizes ------------------------------------------//
 
 	public static final int TEXT_SIZE_XXHDPI = 26;
 	public static final int TITLE_TEXT_SIZE_XXHDPI = 30;
 
 	public static final int TEXT_SIZE_XHDPI = 23;
 	public static final int TITLE_TEXT_SIZE_XHDPI = 27;
 
 	public static final int TEXT_SIZE_HDPI = 20;
 	public static final int TITLE_TEXT_SIZE_HDPI = 24;
 
 	public static final int TEXT_SIZE_MDPI = 15;
 	public static final int TITLE_TEXT_SIZE_MDPI = 19;
 
 	public static final int TEXT_SIZE_LDPI = 11;
 	public static final int TITLE_TEXT_SIZE_LDPI = 15;
 
 	// ---------------- Chart sizes ------------------------------------------//
 	public static final int TITLE_TEXT_SIZE = 20;
 	public static final int LABELS_TEXT_SIZE = 20;
 	public static final int X_LABELS_PADDING = 20;
 	public static final int Y_LABELS_PADDING = 20;
 
 	public static final int TEXT_SIZE_DIP = 13;
 	public static final int TITLE_TEXT_SIZE_DIP = 19;
 	public static final int Y_LABELS_PADDING_DIP = 6;
 	public static final int X_LABELS_PADDING_DIP = 8;
 
 	// ---------------- Chart Margins
 	// ------------------------------------------//
 	public static final int MARGIN_TOP_DPI = 30;
 	public static final int MARGIN_LEFT_DPI = 25;
 	public static final int MARGIN_RIGHT_DPI = 15;
 	public static final int MARGIN_BOTTOM_DPI = 10;
 	public static final int LEGEND_HEIGHT_DPI = 40;
 
 }
