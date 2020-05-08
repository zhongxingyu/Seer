 package de.innoaccel.wamp.server.message;
 
 public class CallErrorMessage implements Message
 {
     private String callId;
 
     private String errorURI;
 
     private String errorDesc;
 
    private Object errorDetails;
 
     public CallErrorMessage(String callId, String errorURI, String errorDesc)
     {
         this(callId, errorURI, errorDesc, null);
     }
 
    public CallErrorMessage(String callId, String errorURI, String errorDesc, Object errorDetails)
     {
         this.callId = callId;
         this.errorURI = errorURI;
         this.errorDesc = errorDesc;
         this.errorDetails = errorDetails;
     }
 
     @Override
     public int getMessageCode()
     {
         return Message.CALL_ERROR;
     }
 
     public String  getCallId()
     {
         return this.callId;
     }
 
     public String getErrorURI()
     {
         return this.errorURI;
     }
 
     public String getErrorDesc()
     {
         return this.errorDesc;
     }
 
    public Object getErrorDetails()
     {
         return this.errorDetails;
     }
 }
