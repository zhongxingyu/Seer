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
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Person;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.muzima.api.service.DataService;
 import org.openmrs.module.muzima.model.NotificationData;
 import org.openmrs.module.muzimaconsultation.web.utils.NotificationDataConverter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.util.Map;
 
 /**
  * TODO: Write brief description about the class here.
  */
 @Controller
 @RequestMapping(value = "module/muzimaconsultation/notification.json")
 public class NotificationFormController {
 
     @RequestMapping(method = RequestMethod.GET)
     @ResponseBody
     public Map<String, Object> getNotificationByUuid(final @RequestParam(required = true) String uuid) {
         DataService service = Context.getService(DataService.class);
         return NotificationDataConverter.convert(service.getNotificationDataByUuid(uuid));
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public void save(final @RequestBody Map<String, Object> request) throws IOException {
         String recipientUuid = request.get("recipient").toString();
        String subject = request.get("subject").toString();
        String payload = request.get("payload").toString();
 
         DataService service = Context.getService(DataService.class);
         Person sender = Context.getAuthenticatedUser().getPerson();
         Person recipient = Context.getPersonService().getPersonByUuid(recipientUuid);
         NotificationData notificationData = new NotificationData();
         notificationData.setPayload(payload);
         notificationData.setSubject(subject);
         notificationData.setSender(sender);
         notificationData.setReceiver(recipient);
         service.saveNotificationData(notificationData);
     }
 }
