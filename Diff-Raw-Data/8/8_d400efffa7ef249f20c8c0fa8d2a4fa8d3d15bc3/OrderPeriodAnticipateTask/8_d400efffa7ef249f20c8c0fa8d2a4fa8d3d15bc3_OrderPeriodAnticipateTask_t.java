 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 /*
  * Created on Dec 15, 2004
  *
  */
 package com.sapienter.jbilling.server.pluggableTask;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.interfaces.OrderEntityLocal;
import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
 
 /**
  * @author Emil
  *
  */
 public class OrderPeriodAnticipateTask extends BasicOrderPeriodTask {
     
     
     public Date calculateStart(OrderEntityLocal order) throws TaskException {
         return super.calculateStart(order);
     }
     
     public Date calculateEnd(OrderEntityLocal order, Date processDate,
             int maxPeriods) 
             throws TaskException {
         viewLimit = null;
         log = Logger.getLogger(OrderPeriodAnticipateTask.class);
 
         if (order.getAnticipatePeriods() != null &&
                 order.getAnticipatePeriods().intValue() > 0) {
             try {
                 Integer periodUnitId = order.getPeriod().getUnitId();
                 Integer periodValue = order.getPeriod().getValue();
                 
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(processDate);
                if (!order.getPeriod().getId().equals(Constants.ORDER_PERIOD_ONCE)) {
                    cal.add(MapPeriodToCalendar.map(periodUnitId),
                            periodValue.intValue());
                }
                 log.debug("Ant periods:" + order.getAnticipatePeriods() + " " +
                         "view limit: " + cal.getTime());
                 // now add the months that this order is getting anticipated
                 cal.add(GregorianCalendar.MONTH,
                         order.getAnticipatePeriods().intValue());
                 viewLimit = cal.getTime();
             } catch (Exception e) {
                 throw new TaskException(e);
             } 
         } 
         
         return super.calculateEnd(order, processDate, maxPeriods);
     }
 }
