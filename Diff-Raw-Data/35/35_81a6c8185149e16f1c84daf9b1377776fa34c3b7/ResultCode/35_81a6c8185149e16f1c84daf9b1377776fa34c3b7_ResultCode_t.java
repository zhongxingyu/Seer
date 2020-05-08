 package com.osastudio.newshub.data;
 
 public interface ResultCode {
 
   public static final int RESULT_UNKNOWN_ERROR = -1;
    public static final int RESULT_NONE = 0;
    public static final int RESULT_OK = 1;
    
    public static final int RESULT_PARAMETER_ERROR = 100;
    public static final int RESULT_PARAMETER_TYPE_ERROR = 101;
    public static final int RESULT_INVALID_VALIDATE_CODE = 102;
    public static final int RESULT_VALIDATE_FAILURE = 103;
    public static final int RESULT_ILLEGAL_VALIDATE_CODE_STATUS = 104;
    public static final int RESULT_VALIDATE_CODE_ALREADY_EXISTS = 105;
    public static final int RESULT_REGISTER_FAILURE = 106;
    public static final int RESULT_NO_DEVICE_IDENTIFIER = 107;
    public static final int RESULT_PAYMENT_NEEDED = 108;
    public static final int RESULT_ADD_ACCOUNT_FAILURE = 109;
    public static final int RESULT_FEEDBACK_FAILURE = 110;
    public static final int RESULT_ILLEGAL_DEVICE = 111;
    public static final int RESULT_MAX_ACCOUNT_REACHED = 112;
    public static final int RESULT_NOTICE_ALREADY_FEEDBACK = 113;
    public static final int RESULT_NO_SUBSCRIPTION = 114;
    
   public static final int RESULT_NETWORK_ERROR_BASE = 1000;
    
   public static final int RESULT_EXCEPTION_BASE = RESULT_NETWORK_ERROR_BASE;
   public static final int RESULT_CONNECT_TIMEOUT_EXCEPTION = RESULT_EXCEPTION_BASE + 1;
   public static final int RESULT_CLIENT_PROTOCOL_EXCEPTION = RESULT_EXCEPTION_BASE + 2;
   public static final int RESULT_IO_EXCEPTION = RESULT_EXCEPTION_BASE + 3;
   public static final int RESULT_PARSE_EXCEPTION = RESULT_EXCEPTION_BASE + 4;
    
   public static final int RESULT_HTTP_BASE = RESULT_NETWORK_ERROR_BASE + 7000;
    
 }
