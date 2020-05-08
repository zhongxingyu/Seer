 package com.parent.management.jsonclient;
 
 public class JSONParams {
     // protocol
     public static final int MC_BASIC = 1;
     public static final int MC_CONFIG = 2;
     
     public static final int MT_BASIC_REG_REQ = 1;
     public static final int MT_BASIC_REG_RESP = 2;
     public static final int MT_BASIC_DATA_UPLOAD_REQ = 3;
     public static final int MT_BASIC_DATA_UPLOAD_RESP = 4;
     public static final int MT_CONFIG_GET_INTERVAL_REQ = 1;
     public static final int MT_CONFIG_GET_INTERVAL_RESP = 2;
 
    public static final String PROTOCOL_VERSION = "1.0";
     public static final String MESSAGE_CLASS = "mc";
     public static final String MESSAGE_TYPE = "mt";
     public static final String REQUEST_SEQUENCE = "seq";
     public static final String DEVICE_IMEI = "imei";
     public static final String PAYLOAD = "payload";
     public static final String DATA_TYPE = "dt";
     public static final String DATA = "data";
 
     public static final String RESPONSE_FAILED = "failed";
     public static final String RESPONSE_FAILED_LIST = "list";
     public static final String RESPONSE_STATUS_CODE = "sc";
     public static final String RESPONSE_STATUS_STRING = "sr";
     
     public static final int DT_DEV_INFO = 0;
     public static final int DT_GPS = 1;
     public static final int DT_BROWSER_HISTORY = 2;
     public static final int DT_APP_HISTORY = 3;
     public static final int DT_CALLS = 4;
     public static final int DT_SMS = 5;
     public static final int DT_BOOKMARKS = 6;
     public static final int DT_CONTACTS = 7;
     public static final int DT_CALENDAR = 8;
     public static final int DT_APP_INSTALLED = 9;
     
     // Registion
     public static final String MANAGER_ACCOUNT = "ma";
     public static final String VERIFY_CODE = "vc";
     public static final String OS_TYPE = "ot";
     public static final String OS_VERSION = "ov";
 
     // configuration
     public static final String INTERVAL_TIME = "interval";
 }
