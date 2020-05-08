 /**
  * 
  */
 package net.lahwran.mcclient.capsystem;
 
 import net.lahwran.capsystem.GenericCommplugin;
 import net.lahwran.capsystem.commdata.Commdata;
 
 /**
  * @author lahwran
  *
  */
 public class Commplugin extends GenericCommplugin {
     StringBuilder messageBuffer;
     CommListener listener;
     
     public Commplugin(String identification, CommListener listener)
     {
         this.identification = identification;
         this.listener = listener;
        maxlength = Capsystem.instance.maxLength;
     }
     
     void dispatchMessage(String message)
     {
         String result = processMessage(message, null);
         if(result == null)
             return; //nothing to do
         listener.onCommevent(getEventData(result));
     }
     
     @SuppressWarnings("unchecked")
     public void sendMessage(Commdata... commdatas) throws DisconnectedException, ServerIncapableException
     {
         if(Capsystem.instance.server == null)
             throw new DisconnectedException();
         if(!Capsystem.hasCap("+comm"))
             throw new ServerIncapableException("Server does not have commsystem");
         for(String line:messageToLines(commdatas))
         {
             Capsystem.instance.server.send(line);
         }
     }
 
     @Override
     protected void freeMessageBuffer(Object bufferid)
     {
         messageBuffer = null;
     }
 
     @Override
     public String getIdentification()
     {
         return identification;
     }
 
     @Override
     protected StringBuilder getMessageBuffer(Object bufferid)
     {
         return messageBuffer;
     }
 
     protected String getPrefix(){ return Commsystem.instance.commprefix; }
 
     @Override
     protected StringBuilder makeMessageBuffer(Object bufferid)
     {
         messageBuffer = new StringBuilder();
         return messageBuffer;
     }
 
 }
