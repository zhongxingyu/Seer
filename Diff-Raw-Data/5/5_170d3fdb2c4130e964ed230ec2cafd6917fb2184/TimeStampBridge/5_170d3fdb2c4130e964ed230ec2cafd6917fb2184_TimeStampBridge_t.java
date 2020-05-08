 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.util;
 
 import java.sql.Timestamp;
 
 import nl.surfnet.bod.web.WebUtils;
 
 import org.hibernate.search.bridge.StringBridge;
 import org.joda.time.DateTime;
 
 /**
  * Handles parsing of timestamps to a String so it can be searched. Needed since
  * BoD uses joda timestamps and sql time stamps.
  * 
  */
 public class TimeStampBridge implements StringBridge {
 
   @Override
   public String objectToString(Object object) {
    String result;
 
     if (object == null) {
      result = null;
     }
     else if (DateTime.class.isAssignableFrom(object.getClass())) {
       DateTime dateTime = (DateTime) object;
       result = dateTime.toString(WebUtils.DEFAULT_DATE_TIME_FORMATTER);
     }
     else if (Timestamp.class.isAssignableFrom(object.getClass())) {
       Timestamp timestamp = (Timestamp) object;
       result = WebUtils.DEFAULT_DATE_TIME_FORMATTER.print(timestamp.getTime());
     }
     else {
       throw new IllegalArgumentException("Bridge is not suitable for handling objects of type: " + object);
     }
 
     return result;
   }
 
 }
