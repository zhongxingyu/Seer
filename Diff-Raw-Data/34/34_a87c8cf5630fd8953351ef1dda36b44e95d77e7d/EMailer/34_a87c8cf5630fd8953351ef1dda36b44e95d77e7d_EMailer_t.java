 /**
  * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.micromailer;
 
 /**
  * The main component responsible for sending mails, either one-by-one or in batch mode.
  * 
  * @author cstamas
  */
 public interface EMailer
 {
     // configuration
 
     void configure( EmailerConfiguration config );
 
     // exposing components
 
     MailTypeSource getMailTypeSource();
 
     MailComposer getMailComposer();
 
     MailStorage getMailStorage();
 
     MailSender getMailSender();
 
     // mail sending
    /**
     * Asynchronously send an e-mail
     */
     MailRequestStatus sendMail( MailRequest request );
 
    /**
     * Synchronously send an e-mail
     */
    MailRequestStatus sendSyncedMail( MailRequest request );

     void sendMailBatch( MailRequestSource mailRequestSource );
 }
