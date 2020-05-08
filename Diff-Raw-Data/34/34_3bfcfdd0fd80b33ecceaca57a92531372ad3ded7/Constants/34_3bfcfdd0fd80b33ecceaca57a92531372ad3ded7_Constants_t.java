 package com.kii.push;
 
 public class Constants {
 
     public static final String USERTOPIC = "UserTopic";
     public static final String GROUPTOPIC = "GroupTopic";
     public static final String GROUPTOPIC_MESSAGE= "Hi, members!";
    public static final String GCM_SENDER_ID = "715025018307";
 
     /**
      * Bucket name to subscribe
      */
     public static final String PUSH_BUCKET_NAME = "PushMyBucket";
 
     public static final String EXTRA_MESSAGE = "message";
     public static final String ACTION_REGISTERED_GCM = "com.kii.push.GCMRegistered";
     public static final String ACTION_UNREGISTERED_GCM = "com.kii.push.GCMUnRegistered";
     public static final String ACTION_GCM_ERROR = "com.kii.push.GCMError";
 }
