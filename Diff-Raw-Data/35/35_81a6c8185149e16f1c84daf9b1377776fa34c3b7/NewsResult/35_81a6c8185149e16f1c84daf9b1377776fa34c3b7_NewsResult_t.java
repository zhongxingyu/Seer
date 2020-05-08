 package com.osastudio.newshub.data;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.text.TextUtils;
 
 public class NewsResult implements ResultCode {
 
    public static final String JSON_KEY_RESULT_DESCRIPTION = "msg";
    public static final String JSON_KEY_RESULT_CODE = "stat";
    
    protected static final String JSON_KEY_LIST = "list";
    protected static final String JSON_KEY_USER_ID = "student_id";
 
    protected int resultCode = RESULT_NONE;
    protected String resultDesc = "";
 
    public NewsResult() {
       
    }
 
    public NewsResult(JSONObject jsonObject) {
       try {
          if (!jsonObject.isNull(JSON_KEY_RESULT_CODE)) {
             setResultCode(jsonObject.getString(JSON_KEY_RESULT_CODE));
          }
          if (!jsonObject.isNull(JSON_KEY_RESULT_DESCRIPTION)) {
             setResultDescription(jsonObject
                   .getString(JSON_KEY_RESULT_DESCRIPTION));
          }
       } catch (JSONException e) {
 
       }
    }
 
    public NewsResult(String resultString, String resultDesc) {
       try {
          this.resultCode = Integer.parseInt(resultString);
       } catch (NumberFormatException e) {
 
       }
       this.resultDesc = resultDesc;
    }
 
    public NewsResult(int resultCode, String resultDesc) {
       this.resultCode = resultCode;
       this.resultDesc = resultDesc;
    }
 
    public int getResultCode() {
       return this.resultCode;
    }
 
    public void setResultCode(String resultString) {
       try {
          this.resultCode = Integer.parseInt(resultString);
       } catch (NumberFormatException e) {
 
       }
    }
 
    public void setResultCode(int resultCode) {
       this.resultCode = resultCode;
    }
 
    public String getResultDescription() {
       return this.resultDesc;
    }
 
    public NewsResult setResultDescription(String resultDescription) {
       this.resultDesc = resultDescription;
       return this;
    }
    
    public boolean needAlert() {
       if (this.resultCode == RESULT_INVALID_VALIDATE_CODE
             || this.resultCode == RESULT_VALIDATE_FAILURE
             || this.resultCode == RESULT_ILLEGAL_VALIDATE_CODE_STATUS
             || this.resultCode == RESULT_VALIDATE_CODE_ALREADY_EXISTS
             || this.resultCode == RESULT_REGISTER_FAILURE
             || this.resultCode == RESULT_NO_DEVICE_IDENTIFIER
             || this.resultCode == RESULT_PAYMENT_NEEDED
            || this.resultCode == RESULT_ADD_ACCOUNT_FAILURE
            || this.resultCode == RESULT_FEEDBACK_FAILURE
            || this.resultCode == RESULT_ILLEGAL_DEVICE
            || this.resultCode == RESULT_MAX_ACCOUNT_REACHED
            || this.resultCode == RESULT_NOTICE_ALREADY_FEEDBACK
            || this.resultCode == RESULT_NO_SUBSCRIPTION) {
          return true;
       }
       return false;
    }
 
    public boolean isSuccess() {
       return this.resultCode == RESULT_OK;
    }
 
    public boolean isFailure() {
       return !isSuccess();
    }
    
   public boolean isNetworkError() {
      return this.resultCode >= RESULT_NETWORK_ERROR_BASE;
   }
   
    public static int httpCode2ResultCode(int httpCode) {
       return httpCode + RESULT_HTTP_BASE;
    }
    
    public static int resultCode2HttpCode(int resultCode) {
       return resultCode - RESULT_HTTP_BASE;
    }
    
    public static JSONObject toJsonObject(String jsonString) {
       if (!TextUtils.isEmpty(jsonString)) {
          try {
             JSONObject jsonObject = new JSONObject(jsonString);
             if (jsonObject.length() > 0) {
                return jsonObject;
             }
          } catch (JSONException e) {
             
          }
       }
       return null;
    }
    
 }
