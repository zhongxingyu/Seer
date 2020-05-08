 package cmg.org.monitor.util.shared;
 
 public final class MonitorConstant {	
 	
	public static final int MAX_ROW_COUNT_CHANGELOG = 10;
	
 	public static final String Notify_Cpu = "When system uses more than 90% CPU :";
 	
	public static final String Notify_JVM = "When system user more than 90% JVM :";
 	
 	public static final String Notify_Memory = "When system uses more than 90% Memory :";
 	
 	public static final String Notify_Service = "When any services are down :";
 	
 	public static final String Notify_ServiceConnection = "When any service connections are too slow :";
 	
 	public static final int CPU_MEMORY_REFRESH_RATE = 1000;
 	// Alert mail sender service
 	public static final String IMAGES_FOR_EMAIL ="monitor.c-mg.vn\\images\\logo\\c-mg_logo.png" ;
 	
 	public static final String ALERTSTORE_DEFAULT_NAME = "Issue Warning";
 	
 	public static final String PROJECT_HOST_NAME = "monitor.c-mg.vn";
 	
 	public static final String ALERT_MAIL_SENDER_NAME = "admin@cmg-app-eng.appspotmail.com";
 
 	public static final int STATISTIC_HISTORY_LENGTH = 999;
 
 	public static final int CPU_MEMORY_HISTORY_LENGTH = 100;
 	
 	public static String SMTP_PROTOCOL = "SMTP";
 
 	public static String HTTP_PROTOCOL = "HTTP(s)";
 
 	public static final String PROJECT_NAME = "CMG Health Monitor System";
 
 	public static final String VERSION = "1.1";
 
 	public static final String RELEASED_ON = "01/11/2011";
 
 	public static final String DONE_MESSAGE = "Add a system successfully";
 	// time to refresh data for all module
 	public static final int REFRESH_RATE = 60000;
 
 	public static final int REDIRECT_WAIT_TIME = 5000;
 
 	public static final String DOMAIN = "c-mg.vn";
 
 	public static final String ADMIN_EMAIL = "monitor@c-mg.vn";
 	public static final String ADMIN_EMAIL_ID = "monitor";
 
 	public static final String ADMIN_PASSWORD = "31102011";
 	
 	public static final String ADMIN_MONITOR_GROUP = "adminmonitor";
 
 	public static final int ROLE_ADMIN = 0x001;
 	public static final int ROLE_NORMAL_USER = 0x002;
 	public static final int ROLE_GUEST = 0x003;
 
 	// / Information google sites content
 	public static final String SITES_USERNAME = "hai.lu@c-mg.com";
 
 	public static final String SITES_PASSWORD = "123hurricane";
 
 	public static final String SITES_CONTENT_FEED_URL = "https://sites.google.com/feeds/content/c-mg.com/monitor-tool/";
 
 	public static final String SITES_APP_NAME = "cmg-monitor-tool";
 
 	public static final String SITES_HELP_CONTENT_ID = "76479283222030903";
 
 	public static final String SITES_ABOUT_CONTENT_ID = "1702220121101760304";
 
 }
