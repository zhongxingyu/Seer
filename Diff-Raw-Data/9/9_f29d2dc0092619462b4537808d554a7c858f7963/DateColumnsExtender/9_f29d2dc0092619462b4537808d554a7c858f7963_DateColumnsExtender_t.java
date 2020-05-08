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
 
 package com.gooddata.connector;
 
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 import com.gooddata.util.StringUtil;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Extends the extracted data (CSV) with the special date/time fact and FK columns
  */
 public class DateColumnsExtender {
 
     private static Logger l = Logger.getLogger(DateColumnsExtender.class);
 
     private List<Integer> dateColumnIndexes;
     private List<DateTimeFormatter> dateColumnFormats;
     private List<SourceColumn> dates;
 
     public DateColumnsExtender(SourceSchema schema) {
         dateColumnIndexes = new ArrayList<Integer>();
         dateColumnFormats = new ArrayList<DateTimeFormatter>();
         dates = schema.getDates();
         for(int i= 0; i < dates.size(); i++)  {
             SourceColumn c = dates.get(i);
             String sr = c.getSchemaReference();
             // only process the dates with a schema reference
             if(sr != null && sr.length() > 0) {
                 dateColumnIndexes.add(schema.getColumnIndex(c));
                 String fmt = c.getFormat();
                 if(fmt == null || fmt.length() <= 0) {
                     if(c.isDatetime())
                         fmt = Constants.DEFAULT_DATETIME_FMT_STRING;
                     else
                         fmt = Constants.DEFAULT_DATE_FMT_STRING;
                 }
                 dateColumnFormats.add(DateTimeFormat.forPattern(fmt));
             }
         }
     }
 
     /**
      * Extends the CSV header
      * @param header existing header
      * @return the extended header
      */
     public String[] extendHeader(String[] header) {
         // Add column headers for the extra date columns
 
         List<String> rowExt = new ArrayList<String>();
         for(int i= 0; i < dates.size(); i++)  {
             SourceColumn c = dates.get(i);
             String sr = c.getSchemaReference();
             if(sr != null && sr.length() > 0) {
                 rowExt.add(StringUtil.toIdentifier(c.getName()) + N.DT_SLI_SFX);
                 if(c.isDatetime()) {
                     rowExt.add(StringUtil.toIdentifier(c.getName()) + N.TM_SLI_SFX);
                     rowExt.add(N.TM_PFX+StringUtil.toIdentifier(c.getName())+"_"+N.ID);
                 }
             }
         }
         if(rowExt.size() > 0)
             return mergeArrays(header, rowExt.toArray(new String[]{}));
         else
             return header;
     }
 
     private final DateTimeFormatter baseFmt = DateTimeFormat.forPattern(Constants.DEFAULT_DATE_FMT_STRING);
     private final DateTime base = baseFmt.parseDateTime("1900-01-01");
 
 
     /**
      * Extends the CSV row
      * @param row existing row
      * @return the extended row
      */
     public String[] extendRow(String[] row) {
         List<String> rowExt = new ArrayList<String>();
         for(int i = 0; i < dateColumnIndexes.size(); i++) {
             SourceColumn c = dates.get(i);
             String sr = c.getSchemaReference();
             if(sr != null && sr.length() > 0) {
                 String dateValue = row[dateColumnIndexes.get(i)];
                 if(dateValue != null && dateValue.trim().length()>0) {
                     try {
                         DateTimeFormatter formatter = dateColumnFormats.get(i);
                         DateTime dt = formatter.parseDateTime(dateValue);
                         Days ds = Days.daysBetween(base, dt);
                         rowExt.add(Integer.toString(ds.getDays() + 1));
                         if(c.isDatetime()) {
                             int  ts = dt.getSecondOfDay();
                             rowExt.add(Integer.toString(ts));
                            String scs = Integer.toString(ts);
                            rowExt.add((scs.length()>1)?(scs):("0"+scs));
                         }
                     }
                     catch (IllegalArgumentException e) {
                         l.debug("Can't parse date "+dateValue);
                         rowExt.add("");
                         if(c.isDatetime()) {
                             rowExt.add("");
                            rowExt.add("00");
                         }
                     }
                 }
                 else {
                     rowExt.add("");
                     if(c.isDatetime()) {
                         rowExt.add("");
                        rowExt.add("00");
                     }
                 }
             }
         }
         if(rowExt.size() > 0)
             return mergeArrays(row, rowExt.toArray(new String[]{}));
         else
             return row;
 
     }
 
     private String[] mergeArrays(String[] a, String[] b) {
         List<String> lst = new ArrayList<String>(Arrays.asList(a));
         lst.addAll(Arrays.asList(b));
         return lst.toArray(a);
     }
 
 }
