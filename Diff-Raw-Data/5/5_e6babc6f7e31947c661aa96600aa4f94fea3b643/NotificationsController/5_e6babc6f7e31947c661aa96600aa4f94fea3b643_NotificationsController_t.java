 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.muzimaconsultation.web.controller;
 
 import org.openmrs.Person;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.muzima.api.service.DataService;
 import org.openmrs.module.muzima.model.NotificationData;
 import org.openmrs.module.muzimaconsultation.web.utils.NotificationDataConverter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * TODO: Write brief description about the class here.
  */
 @Controller
 @RequestMapping(value = "module/muzimaconsultation/notifications.json")
 public class NotificationsController {
 
     @RequestMapping(method = RequestMethod.GET)
     @ResponseBody
     public Map<String, Object> getNotificationsFor(final @RequestParam(value = "uuid", required = true) String uuid,
                                             final @RequestParam(value = "sender", required = true) boolean sender,
                                             final @RequestParam(value = "search") String search,
                                             final @RequestParam(value = "pageNumber") Integer pageNumber,
                                             final @RequestParam(value = "pageSize") Integer pageSize) {
         Map<String, Object> response = new HashMap<String, Object>();
         Person person = Context.getPersonService().getPersonByUuid(uuid);
         DataService service = Context.getService(DataService.class);
 
         Integer pages;
         List<NotificationData> notificationDataList;
         if (sender) {
            pages = (service.countNotificationDataBySender(person, search).intValue() + pageSize - 1) / pageSize;
             notificationDataList = service.getNotificationDataBySender(person, search, pageNumber, pageSize);
         } else {
            pages = (service.countNotificationDataByReceiver(person, search).intValue() + pageSize - 1) / pageSize;
             notificationDataList = service.getNotificationDataByReceiver(person, search, pageNumber, pageSize);
         }
 
         List<Object> objects = new ArrayList<Object>();
         for (NotificationData notificationData : notificationDataList) {
             objects.add(NotificationDataConverter.convert(notificationData));
         }
         response.put("pages", pages);
         response.put("objects", objects);
         return response;
     }
 }
