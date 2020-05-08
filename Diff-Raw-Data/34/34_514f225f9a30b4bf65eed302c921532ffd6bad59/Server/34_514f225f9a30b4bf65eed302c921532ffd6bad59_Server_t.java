 package com.htb.constant;
 
 public class Server {
	public final static String SERVER_IP = "192.168.0.2";
 	public final static String SERVER_DOMIN = "http://" + SERVER_IP;
 	public final static String SERVER_DB_MENU = "dish.db3";
 	public final static String SERVER_DB_USER = "user.db3";
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
 	public final static String GET_PERSONS = PHP_DIR + "getPersons.php";
 	public final static String DEL_ORDER = PHP_DIR + "DelOrder.php";
 	public final static String GET_TABLE_STATUS = PHP_DIR + "getTableStatus.php";
 	public final static String UPDATE_TABLE_STATUS = PHP_DIR + "updateTableStatus.php";
 	public final static String CLEAN_TABLE= PHP_DIR + "cleanTable.php";
 	public final static String GET_PWD = PHP_DIR + "getPwd.php";
 	public final static String STATISTICS_PRINT = PHP_DIR + "printSalesData.php";
 	public final static String MENU_VERSION = PHP_DIR + "version.php";
 	public final static String GET_PERMINSSION = PHP_DIR + "getPerminssion.php";
 	public final static String GET_GETPHONEORDER = PHP_DIR +"getPhoneOrder.php";
 	public final static String DELETE_PHONEORDER = PHP_DIR +"deletePhoneOrder.php";
 	public final static String SERVE_ORDER = PHP_DIR +"updateDishStatus.php";
 	public final static String PRINTER_LIST = PHP_DIR + "getPrinterList.php";
 	public final static String GET_NOTIFICATION = PHP_DIR +"getNotification.php";
 	public final static String GET_NOTIFICATIONTYPES = PHP_DIR +"getNotificationTypes.php";
 	public final static String CLEANNOTIFICATION = PHP_DIR + "cleanNotification.php";
 	public final static String UPDATE_PHONE_ORDER = PHP_DIR + "updatePhoneOrder.php";
 	public final static String GET_ITEM_TABLE_STATUS = PHP_DIR + "getItemTableStatus.php";
 	public final static String CHANGE_TABLE = PHP_DIR + "changeTable.php";
 	public final static String UPDATE_TABLE_ORDER = PHP_DIR + "updateTableOrder.php";
 	public final static String COPY_TABLE = PHP_DIR + "copyTable.php";
 	public final static String CHECK_OUT = PHP_DIR +"checkout.php";
 	public final static String GET_FLAVOR = PHP_DIR +  "getFlavor.php";
 	public final static String DEL_ITEM_ORDER = PHP_DIR + "delItemOrder.php";
 	public final static String COMBINE_TABLE = PHP_DIR + "combineTable.php";
 	public final static String GET_FLOORNUM = PHP_DIR + "getFloorNum.php";
 	/* Database */
 	public final static String DB_DIR = "db/";
 	public final static String DB_MENU = DB_DIR + "menu.db";
 	public final static String DB_SALES = DB_DIR + "sales.db";
 	public final static String DB_USER = DB_DIR + "user.db";
 	
 	/* other */
 	public final static String SERVER_PIC_DIR = "/upload/ftp_temp/temp/";
 	public final static String IMG_PATH = Server.SERVER_DOMIN + SERVER_PIC_DIR;
 	
 	/* printer */
 	public final static int PRINTER_CONTENT_TYPE_ORDER = 100;
 	public final static int PRINTER_CONTENT_TYPE_STATISTICS = 101;
 	
 	/* apk */
 	public final static String SETTING_DIR = "setting/";
 	public final static String APK_VERSION = PHP_DIR + SETTING_DIR + "getApkVer.php";
 	public final static String APK_DIR = PHP_DIR + SETTING_DIR;
 }
