 package com.mobeelizer.java.api;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MobeelizerOperationError {
 
     private final String code;
 
     private final String message;
 
     private final List<Object> arguments;
 
     private MobeelizerOperationError(final String code, final String message, final List<Object> arguments) {
         this.code = code;
         this.message = message;
         this.arguments = arguments;
     }
 
     public String getCode() {
         return code;
     }
 
     public String getMessage() {
         return message;
     }
 
     public List<Object> getArguments() {
         return arguments;
     }
 
     @Override
     public String toString() {
         return "MobeelizerOperationError: " + message;
     }
 
     public static MobeelizerOperationError sendFileCreationError() {
         return new MobeelizerOperationError("sendFileCreationError", "Send file haven't been created.", null);
     }
 
     public static MobeelizerOperationError ioError(final IOException e) {
         return new MobeelizerOperationError("ioException", e.getMessage(), null);
     }
 
     public static MobeelizerOperationError notLoggedError() {
         return new MobeelizerOperationError("notLoggedIn", "User is not logged in", null);
     }
 
     public static MobeelizerOperationError inputFileError() {
         return new MobeelizerOperationError("inputFileError", "Error while processing synchronization file", null);
     }
 
     public static MobeelizerOperationError missingConnectionError() {
         return new MobeelizerOperationError("missingConnection", "Internet connection required", null);
     }
 
     public static MobeelizerOperationError authenticationFailure() {
         return new MobeelizerOperationError("authenticationFailure", "Authentication failure", null);
     }
 
     public static MobeelizerOperationError syncRejected(final String result, final String message) {
         List<Object> args = new ArrayList<Object>();
         args.add(result);
         args.add(message);
         return new MobeelizerOperationError("syncRejected", "Synchronization rejected: result: " + result + ", message: "
                 + message, args);
     }
 
     public static MobeelizerOperationError connectionError() {
         return new MobeelizerOperationError("connectionFailure", "Connection failure", null);
     }
 
     public static MobeelizerOperationError connectionError(final int status) {
         List<Object> args = new ArrayList<Object>();
         args.add(status);
         return new MobeelizerOperationError("connectionFailure", "Connection failure: " + status, args);
     }
 
     public static MobeelizerOperationError serverError(final JSONObject json) {
         try {
             List<Object> args = new ArrayList<Object>();
             JSONArray jsonArgs = json.getJSONArray("arguments");
             for (int i = 0; i < jsonArgs.length(); i++) {
                 args.add(jsonArgs.getString(i));
             }
             return new MobeelizerOperationError(json.getString("code"), json.getString("message"), args);
         } catch (JSONException e) {
             return connectionError();
         }
     }
 
     public static MobeelizerOperationError other(final String message) {
         return new MobeelizerOperationError("other", message, null);
     }
 
    public static MobeelizerOperationError exception(final Exception e) {
        if (e.getMessage() != null && !e.getMessage().trim().equals("")) {
             return other(e.getMessage());
         }
         return other(e.getClass().getCanonicalName());
     }
 }
