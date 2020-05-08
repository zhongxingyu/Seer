 package com.lorent.vovo.util;
 
 import com.lorent.vovo.dto.FontStyle;
 import com.lorent.vovo.util.CallHistory.CallInfo;
 
 public interface Constants {
 	String USER_DIR = System.getProperty("user.dir");
 	String CONFIG_PATH = USER_DIR + "/vovo.conf";
 	String USER_HOME = System.getProperty("user.home");
 	String USER_DATA_DIR = USER_DIR + "/users";
 	//Data Key
 //	String DATA_SAVE = "TEST_SAVE";
 //	String DATA_BACKGROUND_IMAGE="BACKGROUND_IMAGE";
 //	String DATA_BACKGROUND_GAUSSIAN_IMAGE = "Gaussian_IMAGE";
 //	String DATA_LOGGININFO = "LoginInfo";
 	
 	//Config Default Value
 	String CONFIG_PORT = "5060"; 
 	int CONFIG_OPENFIRE_PORT = 5222;
 	int CONFIG_TIME_OUT = 5000;
 	int CONFIG_LOCAL_CS_PORT = 0;
 	
 	//Config Key
 //	String CONFIG_KEY_USERNAME = "username";
 //	String CONFIG_KEY_OPENFIRE_PORT = "openfireport";
 	
 	//View Key
 //	String VIEW_LOGIN = "login";
 	
 	Object USER_STATE_LOCK = new Object();
 	
 	public final static String GET_GROUPCHAT = "getGroupChat";
 	
 	final static String GROUPCHAT_SERVICE_NAME = "groupchat";
 	
 	final static String APPLYIN_GROUPCHAT = "applyInGroupChat";
 	
 	final static String SEARCH_GROUPCHAT = "searchGroupChat";
 	public final static String SUCCESS = "success";
 	public final static String FAIL = "fail";
 	
 	final static String GROUPCHAT_NOTIFY_TYPE = "gcNotifyType";
 	
 	final static String SET_GROUPCHAT_AUTHORITY = "setGroupChatAuthority";
 	final static String QUIT_GROUPCHAT = "quitGroupChat";
 	
 	final static String FETCH_GROUPCHAT_AUTHORITY = "fetchGroupChatAuthority";
 	final static String UPDATE_GROUPCHAT_AUTHORITY = "updateGroupChatAuthority";
 	
 //	String VIEW_MAINFRAME = "mainframe";
 //	String VIEW_TRAYICON = "trayicon";
 //	String VIEW_ABOUTDIALOG = "AboutDialog";
 //	String VIEW_MESSAGEFRAME = "MessageFrame";
 //	String VIEW_MESSAGETABPANEL = "MessageTabPanel";
 //	String VIEW_CONFERENCEPANEL = "ConferencePanel";
 //	String VIEW_CONFERENCE_CARDINFOPANEL= "ConferenceCardInfoPanel";
 //	String VIEW_CONFERENCE_CREATEPANEL = "ConferenceCreatePanel";
 //	String VIEW_CONFERENCE_CREATEDIALOG = "ConferenceCreateDialog";
 //	String VIEW_CONFERENCE_MODIFYIDALOG = "ConferenceModifyDialog";
 //	String VIEW_CONFERENCE_MODIFYPANEL = "ConferenceModifyPanel";
 //	String MAIN_FRAME_NAME = "vovoMainFrame";
 	int VIEW_CONF_EDIT_CREATE = 11;
 	int VIEW_CONF_EDIT_MODIFY = 22;
 	
 	//Status
 	public static final int STATUS_OFFLINE = 0;
     public static final int STATUS_ONLINE = 1;
     public static final int STATUS_AWAY = 2;
     public static final int STATUS_BUSY = 3;
     
     public enum DataKey{
         userName,
         groupChat,
         groupChatAuthority,
         groupChatCreateDialog,
         SAVE,
     	BACKGROUND_IMAGE,
     	BACKGROUND_GAUSSIAN_IMAGE,
     	LOGGININFO,
     	GROUPCHAT_NOTIFY_TYPE_JOIN,
     	GroupMemberListPanel,
     	GROUPCHAT_NOTIFY_TYPE_CHANGE_TOPIC_DESC,
     	GroupChatSetFrame,
     	WHITE_IMAGE,
     	WHITE_IMAGE_X,
     	BACKGROUND_WHITE_IMAGE,
     	SYSTEM_EMOTION,
     	FRIEND_CHAT,
     	MYINFO,
     	GROUPCHAT_NOTIFY_TYPE_QUIT,
     	FileTransfer,
     	FileTransferRequest,
     	GROUPCHAT_NOTIFY_TYPE_KICK_ONE,
     	NotReadMsg,
     	PrivateData
     }
     
     public enum ViewKey{
     	groupChatCreateDialog,
     	MAINFRAME,
     	TRAYICON,
     	ABOUTDIALOG,
     	MESSAGEFRAME,
     	MESSAGETABPANEL,
     	CONFERENCEPANEL,
     	CONFERENCE_CARDINFOPANEL,
     	CONFERENCE_CREATEPANEL,
     	CONFERENCE_CREATEDIALOG,
     	CONFERENCE_MODIFYIDALOG,
     	CONFERENCE_MODIFYPANEL,
     	MAIN_FRAME_NAME,
     	LOGIN,
     	ORGAN_LIST,
     	SHARE_FILE_LIST,
     	GroupListPanel,
     	GroupSearchDialog,
     	TestMainFrame,
     	GroupChatSetFrame,
     	PhoneFrame,
     	UPLOADVIDEOCLIPDIALOG,
     	VIDEOCLIPPANEL,
     	PLAYMONITORFRAME,
     	VODFRAME
     }
     
     public enum ConfigKey{
     	USERNAME,
     	OPENFIRE_PORT,
     	timeout,
     	localcsport
     }
     
     public enum GroupChatPermission{
     	SetGroupChatInfo,
     	KickOneFromGroupChat,
     	ModifyGroupChatMemberAuthority
     }
 
     public enum VideoDefinition{
     	High,
     	Standard,
     	Hyper,
     	Other
     }
     
     public final String SUPPER_USER = "admin";
     
     String SYSTEM_HEAD_IMAGE_PATH = "/com/lorent/vovo/resource/images/systemheads/";
     String SYSTEM_EMOTION_PATH = "/com/lorent/vovo/resource/images/systememotions/";
     
     String FRIEND_CHAT_SESSION_PREFIX = "friend_";
     String GROUP_CHAT_SESSION_PREFIX = "group_";
     String OPENFIRE_RESOURCE = "Spark 2.6.3";
     
     String OTHERMSG_OPERATE_CANCELSENDFILE = "cancelSendFile";
     int SHOW_MESSAGE_PER_PAGE = 10;
     
     int RESTART_TYPE_LOGOUT = 0;
     int RESTART_TYPE_RELOGIN = 1;
     
     String ROLE_ZHUCHIREN = "主持人";
     String ROLE_ZHUJIANGREN = "主讲人";
     String ROLE_PUTONGHUIYIZHE = "普通会议者";
     
     final int CONFERENCE_NAME_LENGTH = 20;
     final int CONFERENCE_PASSWD_LENGTH = 15;
     final int CONFERENCE_SUBJECT_LENGTH = 30;
     final int CONFERENCE_DESCRIPTION_LENGTH = 200;
 
     //"[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"
     String ConfSpecChatStrRegEx = "[&<]+"; 
     
     int MSG_TYPE_FRIEND = 0;
     int MSG_TYPE_GROUP = 1;
     
     final int GROUPCHAT_TOPIC_LEN = 20;
     final int GROUPCHAT_DESCRIPTION_LEN = 100;
     
     final String TRAYICON_PATH = "/com/lorent/vovo/resource/images/trayicon/trayicon_base.png";
     
   //获取历史聊天记录数
     public static final int MAX_HISTORY = 30;
     final static String IMG_FLAG = "screenshotimg=";
     
     final static String SCREENSHOT_IMG_SUFFIX = "jpg";
     
     public static final int CALLHISTORY_CALL_IN = CallInfo.CALL_IN.mask;
     public static final int CALLHISTORY_CALL_OUT = CallInfo.CALL_OUT.mask;
     public static final int CALLHISTORY_CALL_IN_NOT_LISTEN = CallInfo.MISSED_CALL.mask;
     
     final String SYSTEM_HEAD_IMAGE_PATH_SYS = "/com/lorent/vovo/resource/images/systemheads/sys/";
     
     //传输文件超过指定时间就取消传输
     int SEND_FILE_TIMEOUT = 10 * (60 * 1000);
     
     final static long MAXVIDEOFILESIZE = 2110000000l;
     
     final static String TEMPFTPCLIENTSESSIONID = "TempFtpClientSessionID";
     
     final static double MAXBITRATE_VIDEOHIGH = 1600;//Kbps
     final static double MAXBITRATE_VIDEOHYPER = 5000;//kbps
     
    public static String[] VIDEO_CATEGORY=new String[]{"电影","电视","资讯","游戏","动漫","其他"}; 
     
 }
