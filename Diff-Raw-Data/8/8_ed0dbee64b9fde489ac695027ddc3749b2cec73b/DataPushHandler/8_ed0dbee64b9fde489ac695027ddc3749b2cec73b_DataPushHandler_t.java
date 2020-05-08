 package com.chips.xmlhandler;
 
import java.util.Arrays;
import java.util.EnumMap;

 import org.xml.sax.SAXException;
 
 import com.chips.xmlhandler.TagStateEnum.SuccessTagState;
 
 public class DataPushHandler extends SAXHandler<SuccessTagState> {
 
     public DataPushHandler() {
         pushSuccessful = false;
        
        buffers = new EnumMap<SuccessTagState, StringBuilder>(
                SuccessTagState.class);
        values = Arrays.asList(SuccessTagState.values());
     }
     
     @Override
     protected SuccessTagState valueOf(String s) {
         return SuccessTagState.valueOf(normalize(s));
     }
     
     public void endElement(String namespaceURI, String localName, String qName)
             throws SAXException {
         if (localName.equals("food")) {
             String pushSuccessfulString = getString(SuccessTagState.SUCCESS);
 
             pushSuccessful = pushSuccessfulString.equals("1");
 
             clearTagBuffers();
         }
     }
  
     public boolean lastPushSuccessful() {
         return pushSuccessful;
     }
     
     private boolean pushSuccessful;
 }
