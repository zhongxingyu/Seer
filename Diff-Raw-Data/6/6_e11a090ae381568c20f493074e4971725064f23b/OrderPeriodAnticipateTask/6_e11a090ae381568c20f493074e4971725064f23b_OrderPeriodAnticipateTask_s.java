 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 /*
  * Created on Dec 15, 2004
  *
  */
 package com.sapienter.jbilling.server.pluggableTask;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 
 /**
  * @author Emil
  *
  */
 public class OrderPeriodAnticipateTask extends BasicOrderPeriodTask {
     
 
     private static final Logger LOG = Logger.getLogger(OrderPeriodAnticipateTask.class);
     
     public Date calculateEnd(OrderDTO order, Date processDate,
             int maxPeriods, Date periodStarts) 
             throws TaskException {
 
         viewLimit = getViewLimit(order.getOrderPeriod(), processDate);
 
         if (order.getAnticipatePeriods() != null &&
                 order.getAnticipatePeriods().intValue() > 0) {
             try {
                 GregorianCalendar cal = new GregorianCalendar();
                 cal.setTime(viewLimit);
                 // now add the months that this order is getting anticipated
                 cal.add(GregorianCalendar.MONTH,
                         order.getAnticipatePeriods().intValue());
                 LOG.debug("Ant periods:" + order.getAnticipatePeriods() + " " +
                         "view limit: " + viewLimit + " extended " + cal.getTime());
                 viewLimit = cal.getTime();
             } catch (Exception e) {
                 throw new TaskException(e);
             } 
         } 
         
         return super.calculateEnd(order, processDate, maxPeriods, periodStarts);
     }
 }
