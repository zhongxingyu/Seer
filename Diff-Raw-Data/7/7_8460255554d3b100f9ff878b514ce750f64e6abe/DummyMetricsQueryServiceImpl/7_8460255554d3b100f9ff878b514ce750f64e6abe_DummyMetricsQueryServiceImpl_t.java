 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.monitoring.client.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Random;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * The Class DummyMetricsQueryServiceImpl.
  */
 public class DummyMetricsQueryServiceImpl implements MetricsQueryService {
 
     /** The error details by name. */
     Map<String, ErrorDetail> errorDetailsByName = new HashMap<String, ErrorDetail>();
 
     /** The error details by id. */
     Map<String, ErrorDetail> errorDetailsById = new HashMap<String, ErrorDetail>();
 
     private Map<String, Set<String>> serviceData;
 
     private LinkedHashMap<String, Set<String>> consumerData;
 
     /**
      * The Class ErrorDetailImpl.
      */
     public static class ErrorDetailImpl implements ErrorDetail {
 
         /** The id. */
         protected String id;
 
         /** The name. */
         protected String name;
 
         /** The domain. */
         protected String domain;
 
         /** The sub domain. */
         protected String subDomain;
 
         /** The severity. */
         protected String severity;
 
         /** The category. */
         protected String category;
 
         /**
          * Gets the id.
          * 
          * @return the id
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorDetail#getId()
          */
         public String getId() {
             return id;
         }
 
         /**
          * Gets the name.
          * 
          * @return the name
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorDetail#getName()
          */
         public String getName() {
             return name;
         }
 
         /**
          * Gets the domain.
          * 
          * @return the domain
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorDetail#getDomain()
          */
         public String getDomain() {
             return domain;
         }
 
         /**
          * Gets the sub domain.
          * 
          * @return the sub domain
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorDetail#getSubDomain()
          */
         public String getSubDomain() {
             return subDomain;
         }
 
         /**
          * Gets the severity.
          * 
          * @return the severity
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorDetail#getSeverity()
          */
         public String getSeverity() {
             return severity;
         }
 
         /**
          * Gets the category.
          * 
          * @return the category
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorDetail#getCategory()
          */
         public String getCategory() {
             return category;
         }
     }
 
     /**
      * The Class ErrorViewDataImpl.
      */
     public static class ErrorViewDataImpl implements ErrorViewData {
         private String consumer;
         private Double ratioDiff;
         private Double errorDiff;
         private Double errorCallRatio2;
         private Double errorCallRatio1;
         private Long errorCount1;
         private Long errorCount2;
         private String errorId;
         private String errorName;
 
         /**
          * Sets the consumer.
          * 
          * @param consumer
          *            the new consumer
          */
         public void setConsumer(String consumer) {
             this.consumer = consumer;
         }
 
         /**
          * Sets the ratio diff.
          * 
          * @param ratioDiff
          *            the new ratio diff
          */
         public void setRatioDiff(double ratioDiff) {
             this.ratioDiff = Double.valueOf(ratioDiff);
         }
 
         /**
          * Sets the error diff.
          * 
          * @param errorDiff
          *            the new error diff
          */
         public void setErrorDiff(double errorDiff) {
             this.errorDiff = Double.valueOf(errorDiff);
         }
 
         /**
          * Sets the error call ratio2.
          * 
          * @param errorCallRatio2
          *            the new error call ratio2
          */
         public void setErrorCallRatio2(double errorCallRatio2) {
             this.errorCallRatio2 = Double.valueOf(errorCallRatio2);
         }
 
         /**
          * Sets the error call ratio1.
          * 
          * @param errorCallRatio1
          *            the new error call ratio1
          */
         public void setErrorCallRatio1(double errorCallRatio1) {
             this.errorCallRatio1 = Double.valueOf(errorCallRatio1);
         }
 
         /**
          * Sets the error count1.
          * 
          * @param errorCount1
          *            the new error count1
          */
         public void setErrorCount1(long errorCount1) {
             this.errorCount1 = Long.valueOf(errorCount1);
         }
 
         /**
          * Sets the error count2.
          * 
          * @param errorCount2
          *            the new error count2
          */
         public void setErrorCount2(long errorCount2) {
             this.errorCount2 = Long.valueOf(errorCount2);
         }
 
         /**
          * Sets the error id.
          * 
          * @param id
          *            the new error id
          */
         public void setErrorId(String id) {
             this.errorId = id;
         }
 
         /**
          * Sets the error name.
          * 
          * @param name
          *            the new error name
          */
         public void setErrorName(String name) {
             this.errorName = name;
         }
 
         /**
          * Gets the consumer.
          * 
          * @return the consumer
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getConsumer()
          */
         public String getConsumer() {
             return consumer;
         }
 
         /**
          * Gets the ratio diff.
          * 
          * @return the ratio diff
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getRatioDiff()
          */
         public Double getRatioDiff() {
             return ratioDiff;
         }
 
         /**
          * Gets the error diff.
          * 
          * @return the error diff
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getErrorDiff()
          */
         public Double getErrorDiff() {
             return errorDiff;
         }
 
         /**
          * Gets the error call ratio2.
          * 
          * @return the error call ratio2
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getErrorCallRatio2()
          */
         public Double getErrorCallRatio2() {
             return errorCallRatio2;
         }
 
         /**
          * Gets the error call ratio1.
          * 
          * @return the error call ratio1
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getErrorCallRatio1()
          */
         public Double getErrorCallRatio1() {
             return errorCallRatio1;
         }
 
         /**
          * Gets the error count1.
          * 
          * @return the error count1
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getErrorCount1()
          */
         public Long getErrorCount1() {
             return errorCount1;
         }
 
         /**
          * Gets the error count2.
          * 
          * @return the error count2
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getErrorCount2()
          */
         public Long getErrorCount2() {
             return errorCount2;
         }
 
         /**
          * Gets the error id.
          * 
          * @return the error id
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getErrorId()
          */
         public String getErrorId() {
             return errorId;
         }
 
         /**
          * Gets the error name.
          * 
          * @return the error name
          * @see org.ebayopensource.turmeric.monitoring.client.model.ErrorViewData#getErrorName()
          */
         public String getErrorName() {
             return errorName;
         }
     }
 
     /**
      * TimeSlotValue.
      */
     public static class TimeSlotValueImpl implements TimeSlotValue {
 
         /** The criteria. */
         String criteria;
 
         /** The timeslot. */
         Long timeslot;
 
         /** The value. */
         Double value;
 
         /**
          * Gets the criteria.
          * 
          * @return the criteria
          * @see org.ebayopensource.turmeric.monitoring.client.model.TimeSlotValue#getCriteria()
          */
         public String getCriteria() {
             return criteria;
         }
 
         /**
          * Gets the time slot.
          * 
          * @return the time slot
          * @see org.ebayopensource.turmeric.monitoring.client.model.TimeSlotValue#getTimeSlot()
          */
         public Long getTimeSlot() {
             return timeslot;
         }
 
         /**
          * Gets the value.
          * 
          * @return the value
          * @see org.ebayopensource.turmeric.monitoring.client.model.TimeSlotValue#getValue()
          */
         public Double getValue() {
             return value;
         }
 
         /**
          * Sets the criteria.
          * 
          * @param criteria
          *            the new criteria
          */
         public void setCriteria(String criteria) {
             this.criteria = criteria;
         }
 
         /**
          * Sets the timeslot.
          * 
          * @param timeslot
          *            the new timeslot
          */
         public void setTimeslot(Long timeslot) {
             this.timeslot = timeslot;
         }
 
         /**
          * Sets the value.
          * 
          * @param value
          *            the new value
          */
         public void setValue(Double value) {
             this.value = value;
         }
     }
 
     /**
      * Instantiates a new dummy metrics query service impl.
      */
     public DummyMetricsQueryServiceImpl() {
         ErrorDetailImpl ed0 = new ErrorDetailImpl();
         ed0.id = "100";
         ed0.name = "Err0";
         ed0.category = ErrorCategory.Application.toString();
         ed0.domain = "Foo";
         ed0.subDomain = "Bar";
         ed0.severity = "Warning";
         errorDetailsByName.put("Err0", ed0);
         errorDetailsById.put("100", ed0);
 
         ErrorDetailImpl ed1 = new ErrorDetailImpl();
         ed1.id = "101";
         ed1.name = "Err1";
         ed1.category = ErrorCategory.Request.toString();
         ed1.domain = "Foo";
         ed1.subDomain = "Bar";
         ed1.severity = "Critical";
         errorDetailsByName.put("Err1", ed1);
         errorDetailsById.put("101", ed1);
 
         ErrorDetailImpl ed2 = new ErrorDetailImpl();
         ed2.id = "102";
         ed2.name = "Err2";
         ed2.category = ErrorCategory.System.toString();
         ed2.domain = "Foo";
         ed2.subDomain = "Bar";
         ed2.severity = "Error";
         errorDetailsByName.put("Err2", ed2);
         errorDetailsById.put("102", ed2);
 
         serviceData = new LinkedHashMap<String, Set<String>>();
         for (int i = 0; i < 20; i++) {
             serviceData.put("Service" + i,
                             new LinkedHashSet<String>(Arrays.asList(new String[] { "Op0", "Op1", "Op2", "Op3", "Op4",
                                     "Op5", "Op6", "Op7", "Op8", "Op9" })));
         }
 
         consumerData = new LinkedHashMap<String, Set<String>>();
         for (int i = 0; i < 20; i++) {
             consumerData.put(
                             "Service" + i,
                             new LinkedHashSet<String>(Arrays.asList(new String[] { "Consumer0", "Consumer1",
                                     "Consumer2", "Consumer3", "Consumer4", "Consumer5", "Consumer6", "Consumer7",
                                     "Consumer8", "Consumer9" })));
         }
 
     }
 
     /**
      * Gets the metric data download url.
      * 
      * @param mc
      *            the mc
      * @param mrc
      *            the mrc
      * @return the metric data download url
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getMetricDataDownloadUrl(org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricResourceCriteria)
      */
     public String getMetricDataDownloadUrl(MetricCriteria mc, MetricResourceCriteria mrc) {
 
         return "";
     }
 
     /**
      * Gets the metric data.
      * 
      * @param criteria
      *            the criteria
      * @param resourceCriteria
      *            the resource criteria
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getMetricData(org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricResourceCriteria,
      *      com.google.gwt.user.client.rpc.AsyncCallback)
      */
     public void getMetricData(MetricCriteria criteria, MetricResourceCriteria resourceCriteria,
                     AsyncCallback<MetricData> callback) {
 
         MetricData result = new MetricData();
         // result.setRestUrl(URL.encode(MetricsDataRequest.getRestURL(criteria, resourceCriteria)));
         result.setMetricCriteria(criteria);
         result.setMetricResourceCriteria(resourceCriteria);
        boolean nonExistantService = false;
        if (resourceCriteria.resourceEntityRequests != null && resourceCriteria.resourceEntityRequests.size() > 0) {
            nonExistantService = resourceCriteria.resourceEntityRequests.get(0).resourceEntityNames
                            .contains("ANonExistantMyService");
        }
         List<MetricGroupData> metrics = new ArrayList<MetricGroupData>();
         result.setReturnData(metrics);
         if (!nonExistantService) {// if the service name is != that 'ANonexistantService'
             if ("CallCount".equals(criteria.metricName)) {
 
                 int max = (resourceCriteria.resourceEntityRequests != null
                                 && resourceCriteria.resourceEntityRequests.size() > 1 ? 1 : 10);
                 for (int i = 0; i < max; i++) {
                     MetricGroupDataImpl rd = new MetricGroupDataImpl();
                     CriteriaInfoImpl ci = new CriteriaInfoImpl();
                     rd.setCriteriaInfo(ci);
                     metrics.add(rd);
                     if (resourceCriteria.resourceEntityResponseType.equals(Entity.Operation)) {
                         // Was a specific operation requested?
                         if (resourceCriteria.resourceEntityRequests != null) {
                             for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                                 if (r.resourceEntityType.equals(Entity.Operation))
                                     ci.setOperationName(r.resourceEntityNames.get(0));
                             }
                         }
                         // Make up some
                         if (ci.getOperationName() == null)
                             ci.setOperationName("Op" + i);
                     }
                     else if (resourceCriteria.resourceEntityResponseType.equals(Entity.Service)) {
                         if (resourceCriteria.resourceEntityRequests != null) {
                             for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                                 if (r.resourceEntityType.equals(Entity.Service))
                                     ci.setServiceName(r.resourceEntityNames.get(0));
                             }
                         }
                         if (ci.getServiceName() == null)
                             ci.setServiceName("Service" + i);
                     }
                     else if (resourceCriteria.resourceEntityResponseType.equals(Entity.Consumer)) {
                         ci.setConsumerName("Consumer" + i);
                     }
 
                     int s1 = Random.nextInt(1000);
                     rd.setCount1(new Double(s1).toString());
                     int s2 = Random.nextInt(1000);
                     rd.setCount2(new Double(s2).toString());
                     int s3 = Random.nextInt(100);
                     rd.setDiff(new Double(s3).toString());
 
                 }
             }
             else if ("ResponseTime".equals(criteria.metricName)) {
 
                 int max = (resourceCriteria.resourceEntityRequests != null
                                 && resourceCriteria.resourceEntityRequests.size() > 1 ? 1 : 10);
 
                 for (int i = 0; i < max; i++) {
                     MetricGroupDataImpl rd = new MetricGroupDataImpl();
                     CriteriaInfoImpl ci = new CriteriaInfoImpl();
                     rd.setCriteriaInfo(ci);
                     metrics.add(rd);
                     if (resourceCriteria.resourceEntityResponseType.equals(Entity.Service)) {
                         // Was a specific service requested?
                         if (resourceCriteria.resourceEntityRequests != null) {
                             for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                                 if (r.resourceEntityType.equals(Entity.Service))
                                     ci.setServiceName(r.resourceEntityNames.get(0));
                             }
                         }
                         // otherwise make up some
                         if (ci.getServiceName() == null)
                             ci.setServiceName("Service " + i);
                     }
                     else if (resourceCriteria.resourceEntityResponseType.equals(Entity.Operation)) {
                         // Was a specific operation requested?
                         if (resourceCriteria.resourceEntityRequests != null) {
                             for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                                 if (r.resourceEntityType.equals(Entity.Operation))
                                     ci.setOperationName(r.resourceEntityNames.get(0));
                             }
                         }
                         // otherwise make up some
                         if (ci.getOperationName() == null)
                             ci.setOperationName("Op" + i);
 
                     }
                     else if (resourceCriteria.resourceEntityResponseType.equals(Entity.Consumer)) {
                         // Was a specific consumer requested?
                         if (resourceCriteria.resourceEntityRequests != null) {
                             for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                                 if (r.resourceEntityType.equals(Entity.Consumer))
                                     ci.setConsumerName(r.resourceEntityNames.get(0));
                             }
                         }
                         // otherwise make up some
                         if (ci.getConsumerName() == null)
                             ci.setConsumerName("Consumer" + i);
                     }
 
                     double s1 = Random.nextInt(5000000) / 1.0;
                     rd.setCount1(new Double(s1).toString());
                     double s2 = Random.nextInt(5000000) / 1.0;
                     rd.setCount2(new Double(s2).toString());
                     int s3 = Random.nextInt(100);
                     rd.setDiff(new Double(s3).toString());
 
                 }
 
             }
             else if ("ErrorCount".equals(criteria.metricName)) {
 
                 if (resourceCriteria.resourceEntityResponseType.equals(Entity.Consumer)) {
                     // int max = (resourceCriteria.resourceEntityRequests != null &&
                     // resourceCriteria.resourceEntityRequests.size() > 1? 2 : 10);
                     int max = 2;
                     for (int i = 0; i < max; i++) {
                         MetricGroupDataImpl rd = new MetricGroupDataImpl();
                         CriteriaInfoImpl ci = new CriteriaInfoImpl();
                         rd.setCriteriaInfo(ci);
                         metrics.add(rd);
                         ci.setMetricName("Err" + i);
                         ci.setConsumerName("Consumer" + i);
 
                         int s1 = Random.nextInt(1000);
                         rd.setCount1(new Double(s1).toString());
                         int s2 = Random.nextInt(1000);
                         rd.setCount2(new Double(s2).toString());
                         int s3 = Random.nextInt(100);
                         rd.setDiff(new Double(s3).toString());
                     }
                 }
                 else if (resourceCriteria.resourceEntityResponseType.equals(Entity.Service)) {
                     // int max = (resourceCriteria.resourceEntityRequests != null &&
                     // resourceCriteria.resourceEntityRequests.size() > 1? 2 : 10);
                     String service = null;
                     if (resourceCriteria.resourceEntityRequests != null) {
                         for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                             if (r.resourceEntityType.equals(Entity.Service))
                                 service = r.resourceEntityNames.get(0);
                         }
                     }
                     GWT.log("service=" + service);
 
                     int max = 2;
                     for (int i = 0; i < max; i++) {
                         MetricGroupDataImpl rd = new MetricGroupDataImpl();
                         CriteriaInfoImpl ci = new CriteriaInfoImpl();
                         rd.setCriteriaInfo(ci);
                         metrics.add(rd);
                         ci.setMetricName("Err" + i);
                         if (service == null)
                             ci.setServiceName("Service" + i);
                         else
                             ci.setServiceName(service);
 
                         int s1 = Random.nextInt(1000);
                         rd.setCount1(new Double(s1).toString());
                         int s2 = Random.nextInt(1000);
                         rd.setCount2(new Double(s2).toString());
                         int s3 = Random.nextInt(100);
                         rd.setDiff(new Double(s3).toString());
                     }
                 }
                 else if (resourceCriteria.resourceEntityResponseType.equals(Entity.Operation)) {
                     // int max = (resourceCriteria.resourceEntityRequests != null &&
                     // resourceCriteria.resourceEntityRequests.size() > 1? 2 : 10);
                     String operation = null;
 
                     if (resourceCriteria.resourceEntityRequests != null) {
                         for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                             if (r.resourceEntityType.equals(Entity.Operation))
                                 operation = r.resourceEntityNames.get(0);
                         }
                     }
                     GWT.log("Op=" + operation);
                     int max = 2;
                     for (int i = 0; i < max; i++) {
                         MetricGroupDataImpl rd = new MetricGroupDataImpl();
                         CriteriaInfoImpl ci = new CriteriaInfoImpl();
                         rd.setCriteriaInfo(ci);
                         metrics.add(rd);
                         ci.setMetricName("Err" + i);
                         if (operation == null)
                             ci.setOperationName("Op" + i);
                         else
                             ci.setOperationName(operation);
 
                         int s1 = Random.nextInt(1000);
                         rd.setCount1(new Double(s1).toString());
                         int s2 = Random.nextInt(1000);
                         rd.setCount2(new Double(s2).toString());
                         int s3 = Random.nextInt(100);
                         rd.setDiff(new Double(s3).toString());
                     }
                 }
                 else if (resourceCriteria.resourceEntityResponseType.equals(Entity.Error)) {
                     // int max = ( resourceCriteria.resourceEntityRequests!= null &&
                     // resourceCriteria.resourceEntityRequests.size() > 1? 2 : 10);
 
                     String service = null;
                     String operation = null;
                     if (resourceCriteria.resourceEntityRequests != null) {
                         for (ResourceEntityRequest r : resourceCriteria.resourceEntityRequests) {
                             if (r.resourceEntityType.equals(Entity.Operation))
                                 operation = r.resourceEntityNames.get(0);
                             if (r.resourceEntityType.equals(Entity.Service))
                                 service = r.resourceEntityNames.get(0);
                         }
                     }
                     int max = 2;
                     for (int i = 0; i < max; i++) {
                         MetricGroupDataImpl rd = new MetricGroupDataImpl();
                         CriteriaInfoImpl ci = new CriteriaInfoImpl();
                         rd.setCriteriaInfo(ci);
                         metrics.add(rd);
                         ci.setMetricName("Err" + i);
                         if (service == null)
                             ci.setServiceName("Service" + i);
                         else
                             ci.setServiceName(service);
                         if (operation == null)
                             ci.setOperationName("Op" + i);
                         else
                             ci.setOperationName(operation);
                         int s1 = Random.nextInt(1000);
                         rd.setCount1(new Double(s1).toString());
                         int s2 = Random.nextInt(1000);
                         rd.setCount2(new Double(s2).toString());
                         int s3 = Random.nextInt(100);
                         rd.setDiff(new Double(s3).toString());
                     }
                 }
             }
         }
 
         callback.onSuccess(result);
     }
 
     /**
      * Gets the services.
      * 
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getServices(com.google.gwt.user.client.rpc.AsyncCallback)
      */
     public void getServices(AsyncCallback<Map<String, Set<String>>> callback) {
 
         callback.onSuccess(this.serviceData);
     }
 
     /**
      * Gets the error data.
      * 
      * @param ec
      *            the ec
      * @param mc
      *            the mc
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getErrorData(org.ebayopensource.turmeric.monitoring.client.model.ErrorCriteria,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria,
      *      com.google.gwt.user.client.rpc.AsyncCallback)
      */
     public void getErrorData(ErrorCriteria ec, MetricCriteria mc, AsyncCallback<ErrorMetricData> callback) {
         ErrorMetricData result = new ErrorMetricData();
         // result.setRestUrl(URL.encode(ErrorMetricsDataRequest.getRestURL(ec, mc)));
         result.setErrorCriteria(ec);
         result.setMetricCriteria(mc);
 
         List<ErrorViewData> results = new ArrayList<ErrorViewData>();
         result.returnData = results;
         // specific error requested
         if (ec.e != null) {
             // return info about the consumers
             for (int i = 0; i < 10; i++) {
                 ErrorViewDataImpl evd = new ErrorViewDataImpl();
                 evd.setConsumer("Consumer" + i);
                 evd.setErrorCallRatio1(Random.nextDouble() * 10.0);
                 evd.setErrorCallRatio2(Random.nextDouble() * 10.0);
                 evd.setErrorCount1(Random.nextInt(1000));
                 evd.setErrorCount2(Random.nextInt(1000));
                 results.add(evd);
             }
         }
         else {
             GWT.log("no error requested");
             // assume for now that errors are required rather than consumers
             for (Map.Entry<String, ErrorDetail> entry : errorDetailsByName.entrySet()) {
                 if (ec.category != null) {
                     if (ec.category.toString().equalsIgnoreCase(entry.getValue().getCategory())) {
                         ErrorViewDataImpl evd = new ErrorViewDataImpl();
                         evd.setErrorId(entry.getValue().getId());
                         evd.setErrorName(entry.getValue().getName());
                         evd.setErrorCallRatio1(Random.nextDouble() * 10.0);
                         evd.setErrorCallRatio2(Random.nextDouble() * 10.0);
                         evd.setErrorCount1(Random.nextInt(1000));
                         evd.setErrorCount2(Random.nextInt(1000));
                         results.add(evd);
                     }
                 }
                 else if (ec.severity != null) {
                     if (ec.severity.toString().equalsIgnoreCase(entry.getValue().getSeverity())) {
                         ErrorViewDataImpl evd = new ErrorViewDataImpl();
                         evd.setErrorId(entry.getValue().getId());
                         evd.setErrorName(entry.getValue().getName());
                         evd.setErrorCallRatio1(Random.nextDouble() * 10.0);
                         evd.setErrorCallRatio2(Random.nextDouble() * 10.0);
                         evd.setErrorCount1(Random.nextInt(1000));
                         evd.setErrorCount2(Random.nextInt(1000));
                         results.add(evd);
                     }
                 }
                 else {
                     ErrorViewDataImpl evd = new ErrorViewDataImpl();
                     evd.setErrorName(entry.getValue().getName());
                     evd.setErrorId(entry.getValue().getId());
                     evd.setErrorCallRatio1(Random.nextDouble() * 10.0);
                     evd.setErrorCallRatio2(Random.nextDouble() * 10.0);
                     evd.setErrorCount1(Random.nextInt(1000));
                     evd.setErrorCount2(Random.nextInt(1000));
                     results.add(evd);
                 }
 
             }
         }
 
         callback.onSuccess(result);
     }
 
     /**
      * Gets the error data download url.
      * 
      * @param ec
      *            the ec
      * @param mc
      *            the mc
      * @return the error data download url
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getErrorDataDownloadUrl(org.ebayopensource.turmeric.monitoring.client.model.ErrorCriteria,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria)
      */
     public String getErrorDataDownloadUrl(ErrorCriteria ec, MetricCriteria mc) {
         return "";
     }
 
     /**
      * Gets the error detail.
      * 
      * @param selectedErrId
      *            the selected err id
      * @param selectedErrName
      *            the selected err name
      * @param service
      *            the service
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getErrorDetail(java.lang.String,
      *      java.lang.String, com.google.gwt.user.client.rpc.AsyncCallback)
      */
     public void getErrorDetail(String selectedErrId, String selectedErrName, String service,
                     AsyncCallback<ErrorDetail> callback) {
         GWT.log("getErrorDetail, id=" + selectedErrId + ", name=" + selectedErrName + " service=" + service);
         ErrorDetail ed = errorDetailsByName.get(selectedErrName);
 
         callback.onSuccess(ed);
     }
 
     /**
      * Gets the error time slot data.
      * 
      * @param ec
      *            the ec
      * @param mc
      *            the mc
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getErrorTimeSlotData(org.ebayopensource.turmeric.monitoring.client.model.ErrorCriteria,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria,
      *      com.google.gwt.user.client.rpc.AsyncCallback)
      */
     public void getErrorTimeSlotData(ErrorCriteria ec, MetricCriteria mc, AsyncCallback<ErrorTimeSlotData> callback) {
         // Auto-generated method stub
 
     }
 
     /**
      * Gets the metric value trend.
      * 
      * @param firstDate
      *            the first date
      * @param secondDate
      *            the second date
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getServiceMetricValueTrend(org.ebayopensource.turmeric.monitoring.client.model.MetricValue,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricValue,
      *      com.google.gwt.user.client.rpc.AsyncCallback)
      */
     @Override
     public void getMetricValueTrend(MetricValue firstDate, MetricValue secondDate,
                     AsyncCallback<List<TimeSlotData>> callback) {
         // Auto-generated method stub
 
     }
 
     /**
      * Gets the service consumers.
      * 
      * @param serviceName
      *            the service name
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getServiceConsumers(java.lang.String,
      *      com.google.gwt.user.client.rpc.AsyncCallback)
      */
     @Override
     public void getServiceConsumers(String serviceName, AsyncCallback<Set<String>> callback) {
 
     }
 
     /**
      * Gets the error trend.
      * 
      * @param ec
      *            the ec
      * @param firstDate
      *            the first date
      * @param secondDate
      *            the second date
      * @param callback
      *            the callback
      * @see org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService#getErrorTrend(org.ebayopensource.turmeric.monitoring.client.model.ErrorCriteria,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria,
      *      org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria,
      *      com.google.gwt.user.client.rpc.AsyncCallback)
      */
     @Override
     public void getErrorTrend(ErrorCriteria ec, MetricCriteria firstDate, MetricCriteria secondDate,
                     AsyncCallback<List<ErrorTimeSlotData>> callback) {
 
     }
 
     /**
      * Gets the service data.
      * 
      * @return the service data
      */
     public Map<String, Set<String>> getServiceData() {
         return this.serviceData;
     }
 
     /**
      * Gets the service data.
      * 
      * @return the service data
      */
     public Map<String, Set<String>> getConsumerData() {
         return this.consumerData;
     }
 
 }
