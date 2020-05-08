 package com.barestodo.android.service.tasks;
 
 import com.barestodo.android.R;
import com.barestodo.android.app.MyApplication;
 
 
 public enum HttpStatus {
    OK(0),NOT_FOUND(R.string.not_found),BAD_REQUEST(R.string.invalid_request),FORBIDEN(R.string.authentication_problem),SERVER_ERROR(R.string.service_unavailable),CONNECTION_LOST(R.string.connection_problem), CLIENT_ERROR(R.string.application_error);
 
 
     private int labelId;
 
     HttpStatus(int labelId) {
         this.labelId=labelId;
     }
 
 
     public String getErrorMessage() {
        return MyApplication.getAppContext().getString(labelId);
     }
 }
