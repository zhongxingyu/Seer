 package org.jboss.pressgang.ccms.wrapper;
 
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTLogDetailsV1;
 
 public class RESTLogMessageWrapper implements LogMessageWrapper {
     final RESTLogDetailsV1 logDetails;
 
     public RESTLogMessageWrapper(final RESTLogDetailsV1 logDetails) {
         this.logDetails = logDetails;
     }
 
     @Override
     public String getMessage() {
         return logDetails.getMessage();
     }
 
     @Override
     public void setMessage(String message) {
         logDetails.setMessage(message);
     }
 
     @Override
     public Integer getFlags() {
         return logDetails.getFlag();
     }
 
     @Override
     public void setFlags(Integer flags) {
         logDetails.setFlag(flags);
     }
 
     @Override
     public String getUser() {
        return logDetails.getUser() == null ? null : logDetails.getUser().getName();
     }
 
     @Override
     public void setUser(String user) {
         final RESTUserV1 userEntity = new RESTUserV1();
         if (user.matches("^\\d+$")) {
             userEntity.setId(Integer.parseInt(user));
         } else {
             userEntity.setName(user);
         }
         logDetails.setUser(userEntity);
     }
 
     @Override
     public RESTLogDetailsV1 unwrap() {
         return logDetails;
     }
 }
