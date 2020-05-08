 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.util;
 
 import com.gooddata.Constants;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Date Utilities
  */
 public class DateUtil {
 
     private static Map<String, DateTimeFormatter> formatters = new HashMap<String, DateTimeFormatter>();
 
     public static synchronized DateTimeFormatter getDateFormatter(String fmt, boolean isDateTime) {
         if(fmt == null || fmt.length() <= 0) {
             if(isDateTime)
                 fmt = Constants.DEFAULT_DATETIME_FMT_STRING;
             else
                 fmt = Constants.DEFAULT_DATE_FMT_STRING;
         }
         // in case of UNIX TIME we don't format but create the date from the UNIX time number
         if(Constants.UNIX_DATE_FORMAT.equalsIgnoreCase(fmt)) {
             fmt = Constants.DEFAULT_DATETIME_FMT_STRING;
         }
 
         DateTimeFormatter frmtr = formatters.get(fmt);
         if(frmtr == null) {
             frmtr = DateTimeFormat.forPattern(fmt);
             formatters.put(fmt, frmtr);
         }
         return frmtr;
     }
 
     private static final DateTimeFormatter baseFmt = DateTimeFormat.forPattern(Constants.DEFAULT_DATETIME_FMT_STRING);
     private static final DateTime base = baseFmt.parseDateTime("1900-01-01 00:00:00");
 
 
     public static String convertUnixTimeToString(String value) {
         DateTime dt;
         try {
             long l = Long.parseLong(value);
             dt = new DateTime(l*1000);
         }
         catch (NumberFormatException e) {
            return "";
         }
         return baseFmt.print(dt);
     }
 
     public static String convertUnixTimeToString(Number value) {
         DateTime dt;
         try {
             long l = value.longValue();
             dt = new DateTime(l*1000);
         }
         catch (NumberFormatException e) {
            return "";
         }
         return baseFmt.print(dt);
     }
 
 
 
 }
