 package org.xezz.timeregistration.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.xezz.timeregistration.dao.TimeSpanDAO;
 import org.xezz.timeregistration.services.TimeSpanService;
 
 /**
  * User: Xezz
  * Date: 05.05.13
  * Time: 11:18
  */
 @Controller
 @RequestMapping(value = "/timespan")
 public class TimeSpanController {
 
     @Autowired
     TimeSpanService service;
 
     private final static Logger LOGGER = LoggerFactory.getLogger(TimeSpanController.class);
 
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public Iterable<TimeSpanDAO> getAll() {
         LOGGER.info("Request to get all TimeSpans");
         return service.findAllTimeSpans();
     }
 
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public TimeSpanDAO create(@RequestBody TimeSpanDAO timeSpanDAO) {
         LOGGER.info("Request to create a new TimeSpan");
         return service.createNewTimeSpan(timeSpanDAO);
     }
 
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public TimeSpanDAO update(@RequestBody TimeSpanDAO timeSpanDAO) {
         LOGGER.info("Request to update an existing TimeSpan");
         return service.updateTimeSpan(timeSpanDAO);
     }
 
 
     // TODO: Add DELETE for TimeSpan
 }
