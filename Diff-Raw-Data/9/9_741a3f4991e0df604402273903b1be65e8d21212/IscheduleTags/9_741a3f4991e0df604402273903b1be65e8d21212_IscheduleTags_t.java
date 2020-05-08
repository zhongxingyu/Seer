 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.sss.util.xml.tagdefs;
 
 import javax.xml.namespace.QName;
 
 /** Define Caldav tags for XMlEmit.
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 public class IscheduleTags implements CaldavDefs {
   /** */
   public static final String namespace = "urn:ietf:params:xml:ns:ischedule";
 
   /**   */
   public static final QName administrator = new QName(namespace,
                                                       "administrator");
 
   /**   */
   public static final QName attachments = new QName(namespace,
                                                     "attachments");
 
   /**   */
   public static final QName attachmentTypeNotSupported = new QName(namespace,
                                                     "attachment-type-not-supported");
 
   /**   */
   public static final QName calendarData = new QName(namespace,
                                                      "calendar-data");
 
   /**   */
   public static final QName calendarDataTypes = new QName(namespace,
                                                      "calendar-data-types");
 
   /**   */
   public static final QName capabilities = new QName(namespace,
                                                      "capabilities");
 
   /**   */
   public static final QName component = new QName(namespace,
                                              "component");
 
   /**   */
  public static final QName error = new QName(namespace,
                                              "error");

  /**   */
   public static final QName external = new QName(namespace,
                                                  "external");
 
   /**   */
   public static final QName inline = new QName(namespace,
                                                "inline");
 
   /**   */
   public static final QName invalidCalendarData = new QName(namespace,
                                                      "invalid-calendar-data");
 
   /**   */
   public static final QName invalidCalendarDataType = new QName(namespace,
                                                      "invalid-calendar-data-type");
 
   /**   */
   public static final QName invalidSchedulingMessage = new QName(namespace,
                                               "invalid-scheduling-message");
 
   /**   */
   public static final QName maxContentLength = new QName(namespace,
                                                          "max-content-length");
 
   /**   */
   public static final QName maxDateTime = new QName(namespace,
                                                     "max-date-time");
 
   /**   */
   public static final QName maxInstances = new QName(namespace,
                                                      "max-instances");
 
   /**   */
   public static final QName maxRecipients = new QName(namespace,
                                                      "max-recipients");
 
   /**   */
   public static final QName method = new QName(namespace,
                                                "method");
 
   /**   */
   public static final QName minDateTime = new QName(namespace,
                                                     "min-date-time");
 
   /**   */
   public static final QName originatorMissing = new QName(namespace,
                                                    "originator-missing");
 
   /**   */
   public static final QName queryResult = new QName(namespace,
                                                     "query-result");
 
   /**   */
   public static final QName recipient = new QName(namespace,
                                                   "recipient");
 
   /**   */
   public static final QName recipientPermissions = new QName(namespace,
                                                   "recipient-permissions");
 
   /**   */
   public static final QName recipientMissing = new QName(namespace,
                                                   "recipient-missing");
 
   /**   */
   public static final QName requestStatus = new QName(namespace,
                                                       "request-status");
 
   /**   */
   public static final QName response = new QName(namespace,
                                                  "response");
 
  /** */
  public static final QName responseDescription = new QName(namespace,
                                                            "response-description");

   /**   */
   public static final QName scheduleResponse = new QName(namespace,
                                                          "schedule-response");
 
   /**   * /
   public static final QName scheme = new QName(namespace,
                                                "scheme");
 
   / **   * /
   public static final QName supportedRecipientUriSchemeSet = new QName(namespace,
                                        "supported-recipient-uri-scheme-set");*/
 
   /**   */
   public static final QName schedulingMessages = new QName(namespace,
                                               "scheduling-messages");
 
   /**   */
   public static final QName verificationFailed = new QName(namespace,
                                                            "verification-failed");
 
   /**   */
   public static final QName version = new QName(namespace, "version");
 
   /**   */
   public static final QName versionNotSupported = new QName(namespace,
                                                             "version-not-supported");
 
   /**   */
   public static final QName versions = new QName(namespace, "versions");
 
 }
 
