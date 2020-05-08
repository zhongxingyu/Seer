 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INFSO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.registration.actions;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 
import javax.naming.directory.DirContext;

 import org.restlet.Request;
 import org.restlet.data.Form;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.registration.cfg.AppConfiguration;
 import eu.stratuslab.registration.data.UserAttribute;
 import eu.stratuslab.registration.data.UserEntry;
 import eu.stratuslab.registration.utils.LdapConfig;
 import eu.stratuslab.registration.utils.Notifier;
 import eu.stratuslab.registration.utils.RequestUtils;
 
 @SuppressWarnings("serial")
 public class ResetPassword implements Action {
 
     private static final String EMAIL_MESSAGE_TEMPLATE = //
     "Your password has been reset to '%s'.\n\n" + //
             "Change this password as soon as possible by updating your profile.\n";
 
     private static final String EMAIL_SENT_MESSAGE = //
     "An email with your new password has been sent.";
 
     private static final String EMAIL_ABORT_MESSAGE = //
     "The request to update your password has been canceled.\n"
             + "Your password has NOT been changed.\n";
 
     private String identifier;
 
     private String email;
 
     public ResetPassword(String identifier, String email) {
         this.identifier = identifier;
         this.email = email;
     }
 
     public String abort(Request request) {
         return EMAIL_ABORT_MESSAGE;
     }
 
     public String execute(Request request) {
 
         String newPassword = randomPassword();
 
         Form form = new Form();
         form.add(UserAttribute.PASSWORD.key, newPassword);
 
         LdapConfig ldapEnv = RequestUtils.extractLdapConfig(request);
 
        UserEntry.rawUpdateUser(identifier, DirContext.REPLACE_ATTRIBUTE, form,
                ldapEnv);
 
         AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);
 
         String message = String.format(EMAIL_MESSAGE_TEMPLATE, newPassword);
 
         try {
             Notifier.sendNotification(email, message, cfg);
         } catch (Exception e) {
             e.printStackTrace();
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "error sending email");
         }
 
         return EMAIL_SENT_MESSAGE;
     }
 
     private static String randomPassword() {
         SecureRandom randomSource = new SecureRandom();
         BigInteger value = new BigInteger(60, randomSource);
         return value.toString(Character.MAX_RADIX);
     }
 
 }
