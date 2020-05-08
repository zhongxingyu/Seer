 package com.barestodo.android.service.tasks;
 
import android.content.res.Resources;
 import com.barestodo.android.R;
 
 
 public enum HttpStatus {
    OK(0),NOT_FOUND(R.string.not_found),BAD_REQUEST(R.string.invalid_request),FORBIDEN(R.string.authentication_problem),SERVER_ERROR(R.string.service_unavailable),CONNECTION_LOST(R.string.connection_problem), CLIENT_ERROR(R.string.not_found);
 
 
     private int labelId;
 
     HttpStatus(int labelId) {
         this.labelId=labelId;
     }
 
 
     public String getErrorMessage() {
        return Resources.getSystem().getString(labelId);
     }
 }
