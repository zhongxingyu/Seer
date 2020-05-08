 /*
   $Id: $
 
   Copyright (C) 2012 Virginia Tech.
   All rights reserved.
 
   SEE LICENSE FOR MORE INFORMATION
 
   Author:  Middleware Services
   Email:   middleware@vt.edu
   Version: $Revision: $
   Updated: $Date: $
 */
 package edu.vt.middleware.cas.ldap;
 
 import java.util.Date;
 
 /**
  * Description of Authenticator.
  *
  * @author Middleware Services
  * @version $Revision: $
  */
 public class Authenticator implements Runnable {
 
     private final SharedState state;
 
     public Authenticator(final SharedState state) {
         this.state = state;
     }
 
     public void run() {
         final Date start = new Date();
         boolean success;
         try {
             success = this.state.getAuthenticationHandler().authenticate(this.state.getWorkQueue().take());
         } catch (Exception e) {
             success = false;
         }
         final Sample.Result result = success ? Sample.Result.SUCCESS : Sample.Result.FAILURE;
         try {
             this.state.getResultQueue().put(new Sample(start, new Date(), result));
        } catch (InterruptedException e) {}
     }
 }
