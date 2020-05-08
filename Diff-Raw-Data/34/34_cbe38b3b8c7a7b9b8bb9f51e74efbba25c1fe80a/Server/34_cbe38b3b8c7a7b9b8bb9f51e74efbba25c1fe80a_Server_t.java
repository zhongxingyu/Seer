 package com.htb.constant;
 
 public class Server {
 	public final static String SERVER_IP = "192.168.0.1";
 	public final static String SERVER_DOMIN = "http://" + SERVER_IP;
 	public final static String SERVER_DB_MENU = "menu.db";
 	public final static String SERVER_DB_SALES = "sales.db";
 	
 	public final static String FTP_USERNAME = "root";
 	public final static String FTP_PWD = "123456";
 	public final static String FTP_DB_DIR = "db";
 	
 	/* php */
 	public final static String PHP_DIR = "orderPad/";
 	public final static String GET_DISH_STATUS = PHP_DIR + "getDishStatus.php";
 	public final static String SUBMIT_ORDER = PHP_DIR + "submitOrder.php";
 	public final static String LATEST_STATISTICS = PHP_DIR + "latestStatistics.php";
 	public final static String GET_MYORDER = PHP_DIR + "getToDelOrder.php";
 	public final static String DEL_ORDER = PHP_DIR + "DelOrder.php";
 	public final static String GET_TABLE_STATUS = PHP_DIR + "getTableStatus.php";
 	public final static String UPDATE_TABLE_STATUS = PHP_DIR + "updateTableStatus.php";
 	public final static String CLEAN_TABLE= PHP_DIR + "cleanTable.php";
 	public final static String GET_PWD = PHP_DIR + "getPwd.php";
 	public final static String STATISTICS_PRINT = PHP_DIR + "statistics.php";
 	public final static String MENU_VERSION = PHP_DIR + "version.php";
 	public final static String GET_PERMINSSION = PHP_DIR + "getPerminssion.php";
 	public final static String GET_GETPHONEORDER = PHP_DIR +"getPhoneOrder.php";
 	public final static String DELETE_PHONEORDER = PHP_DIR +"deletePhoneOrder.php";
 
 	public final static String GET_NOTIFICATION = PHP_DIR +"getNotification.php";
 	public final static String GET_NOTIFICATIONTYPES = PHP_DIR +"getNotificationTypes.php";
 	public final static String CLEANNOTIFICATION = PHP_DIR + "cleanNotification.php";
 	public final static String UPDATE_PHONE_ORDER = PHP_DIR + "updatePhoneOrder.php";
	public final static String GET_ITEM_TABLE_STATUS = PHP_DIR + "getItemTableStatus.php";
 	/* Database */
 	public final static String DB_DIR = "db/";
 	public final static String DB_MENU = DB_DIR + "menu.db";
 	public final static String DB_SALES = DB_DIR + "sales.db";
 	
 	/* other */
 	public final static String IMG_PATH = Server.SERVER_DOMIN + "/upload/ftp_temp/temp/";
 }
