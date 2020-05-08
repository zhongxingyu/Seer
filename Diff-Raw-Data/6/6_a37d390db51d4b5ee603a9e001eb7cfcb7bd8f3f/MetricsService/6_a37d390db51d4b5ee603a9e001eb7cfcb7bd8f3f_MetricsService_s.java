 /**
  * Copyright 2000-2012 NeuStar, Inc. All rights reserved.
  * NeuStar, the Neustar logo and related names and logos are registered
  * trademarks, service marks or tradenames of NeuStar, Inc. All other
  * product names, company names, marks, logos and symbols may be trademarks
  * of their respective owners.
  */
 
 package biz.neustar.service.metrics.ws;
 
 import java.util.List;
import java.util.Map;
import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import biz.neustar.service.common.spring.ValidationUtil;
 import biz.neustar.service.metrics.ws.model.ContextConfig;
 import biz.neustar.service.metrics.ws.model.ContextConfigValidator;
 import biz.neustar.service.metrics.ws.model.ContextProvider;
 import biz.neustar.service.metrics.ws.model.Metric;
 import biz.neustar.service.metrics.ws.model.MetricValidator;
 
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

 @Component
 @Path("/metrics/v1/")
 public class MetricsService {
     private static final Logger LOGGER = 
             LoggerFactory.getLogger(MetricsService.class);
     
 
     @Autowired
     private ValidationUtil validationUtil;
 
     @Autowired // should be like a DAO or something
     private ContextProvider contextProvider;
     
     @GET
     @Path("/hello")
     @Produces({MediaType.TEXT_PLAIN})
     public String hello() {
         return "hello world!";
     }
     
     @POST
     @Path("/metrics")
     @Consumes({MediaType.APPLICATION_JSON})
     public void create(List<Metric> metrics) {
         LOGGER.debug("Received: {}", metrics);
         
         validationUtil.validate(metrics, 
                 new MetricValidator(contextProvider));
 
         for (Metric metric : metrics) {
             LOGGER.debug("Metric Received: {}", metric);
         }
     }
     
     @POST
     @Path("/config")
     @Consumes({MediaType.APPLICATION_JSON})
     public void createConfig(ContextConfig contextConfig) {
         LOGGER.debug("Received: {}", contextConfig);
         validationUtil.validate(contextConfig, 
                 ContextConfigValidator.getDefaultInstance());
         LOGGER.debug("Validated {}", contextConfig);
         
         contextProvider.setContexts(contextConfig);
         LOGGER.debug("added Context config {}", contextConfig.getName());
 
     }
     
 }
