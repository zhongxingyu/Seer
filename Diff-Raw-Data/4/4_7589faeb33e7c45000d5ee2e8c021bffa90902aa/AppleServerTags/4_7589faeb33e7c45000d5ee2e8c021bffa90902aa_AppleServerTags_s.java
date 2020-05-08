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
 
 /** Apple specific tags.
  *
  * @author douglm
  *
  */
 public class AppleServerTags {
   /** */
   public static final String appleCaldavNamespace = "http://calendarserver.org/ns/";
 
   /**   */
   public static final QName access = new QName(appleCaldavNamespace,
                                                "access");
 
   /**   */
   public static final QName action = new QName(appleCaldavNamespace,
                                                "action");
 
   /**   */
   public static final QName attendee = new QName(appleCaldavNamespace,
                                                  "attendee");
 
   /**   */
   public static final QName canBePublished = new QName(appleCaldavNamespace,
                                                "can-be-published");
 
   /**   */
   public static final QName canBeShared = new QName(appleCaldavNamespace,
                                                "can-be-shared");
 
   /**   */
   public static final QName cancel = new QName(appleCaldavNamespace,
                                                "cancel");
 
   /**   */
   public static final QName changedProperty = new QName(appleCaldavNamespace,
                                                         "changed-property");
 
   /**   */
   public static final QName changes = new QName(appleCaldavNamespace,
                                                 "changes");
 
   /**   */
   public static final QName commonName = new QName(appleCaldavNamespace,
                                                "common-name");
 
   /**   */
   public static final QName create = new QName(appleCaldavNamespace,
                                                "create");
 
   /**   */
   public static final QName dtstamp = new QName(appleCaldavNamespace,
                                                 "dtstamp");
 
   /**   */
   public static final QName getctag = new QName(appleCaldavNamespace,
                                                 "getctag");
 
   /**   */
   public static final QName hosturl = new QName(appleCaldavNamespace,
                                                 "hosturl");
 
   /**   */
   public static final QName inReplyTo = new QName(appleCaldavNamespace,
                                                 "in-reply-to");
 
   /**   */
   public static final QName invite = new QName(appleCaldavNamespace,
                                                 "invite");
 
   /**   */
   public static final QName inviteAccepted = new QName(appleCaldavNamespace,
                                                 "invite-accepted");
 
   /**   */
   public static final QName inviteDeclined = new QName(appleCaldavNamespace,
                                                 "invite-declined");
 
   /**   */
   public static final QName inviteDeleted = new QName(appleCaldavNamespace,
                                                 "invite-deleted");
 
   /**   */
   public static final QName inviteInvalid = new QName(appleCaldavNamespace,
                                                 "invite-invalid");
 
   /**   */
   public static final QName inviteNoresponse = new QName(appleCaldavNamespace,
                                                 "invite-noresponse");
 
   /**   */
   public static final QName inviteNotification = new QName(appleCaldavNamespace,
                                                 "invite-notification");
 
   /**   */
   public static final QName inviteReply = new QName(appleCaldavNamespace,
                                                 "invite-reply");
 
   /**   */
   public static final QName master = new QName(appleCaldavNamespace,
                                                "master");
 
   /**   */
   public static final QName notification = new QName(appleCaldavNamespace,
                                                "notification");
 
   /**   */
   public static final QName notificationtype = new QName(appleCaldavNamespace,
                                                "notificationtype");
 
   /**   */
   public static final QName organizer = new QName(appleCaldavNamespace,
                                                "organizer");
 
   /**   */
   public static final QName partstat = new QName(appleCaldavNamespace,
                                                  "partstat");
 
   /**   */
   public static final QName privateComment = new QName(appleCaldavNamespace,
                                                        "private-comment");
 
   /**   */
   public static final QName read = new QName(appleCaldavNamespace,
                                               "read");
 
   /**   */
   public static final QName readWrite = new QName(appleCaldavNamespace,
                                               "read-write");
 
   /**   */
   public static final QName recurrence = new QName(appleCaldavNamespace,
                                                    "recurrence");
 
   /**   */
   public static final QName recurrenceid = new QName(appleCaldavNamespace,
                                                      "recurrenceid");
 
   /**   */
   public static final QName remove = new QName(appleCaldavNamespace,
                                               "remove");
 
   /**   */
   public static final QName reply = new QName(appleCaldavNamespace,
                                               "reply");
 
   /**   */
   public static final QName scheduleChanges = new QName(appleCaldavNamespace,
                                                         "schedule-changes");
 
   /**   */
   public static final QName set = new QName(appleCaldavNamespace,
                                                         "set");
 
   /**   */
   public static final QName share = new QName(appleCaldavNamespace,
                                                         "share");
 
   /**   */
   public static final QName shared = new QName(appleCaldavNamespace,
                                                         "shared");
 
   /**   */
   public static final QName sharedAs = new QName(appleCaldavNamespace,
                                                         "shared-as");
 
   /**   */
   public static final QName sharedOwner = new QName(appleCaldavNamespace,
                                                         "shared-owner");
 
   /**   */
   public static final QName summary = new QName(appleCaldavNamespace,
                                                         "summary");
 
   /**   */
   public static final QName uid = new QName(appleCaldavNamespace,
                                                "uid");
 
   /**   */
   public static final QName update = new QName(appleCaldavNamespace,
                                                "update");
 
   /**   */
   public static final QName user = new QName(appleCaldavNamespace,
                                                "user");
 }
