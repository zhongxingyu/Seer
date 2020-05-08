 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.td_import.model;
 
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class TimeColumnSampling extends ColumnSampling {
    public static final String HHmmss = "HHmmss";
 
     private static final SimpleDateFormat yyyyMMdd_SDF;
     private static final SimpleDateFormat yyyyMMdd$1HHmmss_SDF;
     private static final SimpleDateFormat yyyyMMdd$1HHmmssZ_SDF;
     public static final SimpleDateFormat HHmmss_SDF;
 
     private static final SimpleDateFormat[] SDF_LIST;
 
     static {
         yyyyMMdd_SDF = new SimpleDateFormat("yyyyMMdd");
         yyyyMMdd_SDF.setLenient(false);
         yyyyMMdd$1HHmmss_SDF = new SimpleDateFormat("yyyyMMdd$1HHmmss");
         yyyyMMdd$1HHmmss_SDF.setLenient(false);
         yyyyMMdd$1HHmmssZ_SDF = new SimpleDateFormat("yyyyMMdd$1HHmmss Z");
         yyyyMMdd$1HHmmssZ_SDF.setLenient(false);
        HHmmss_SDF = new SimpleDateFormat("HHmmss");
         HHmmss_SDF.setLenient(false);
 
         SDF_LIST = new SimpleDateFormat[] {
                 yyyyMMdd_SDF,
                 yyyyMMdd$1HHmmss_SDF,
                 yyyyMMdd$1HHmmssZ_SDF,
                 HHmmss_SDF,
         };
     }
 
     private static final String yyyyMMdd_STRF = "%Y%m%d";
     private static final String yyyyMMdd$1HHmmss_STRF = "%Y%m%d$1%H%M%S";
     private static final String yyyyMMdd$1HHmmssZ_STRF = "%Y%m%d$1%H%M%S %Z";
     public static final String HHmmss_STRF = "%T";
 
     private static final String[] STRF_LIST = new String[] {
         yyyyMMdd_STRF,
         yyyyMMdd$1HHmmss_STRF,
         yyyyMMdd$1HHmmssZ_STRF,
         HHmmss_STRF
     };
 
     protected int[] timeScores = new int[] { 0, 0, 0, 0 };
 
     public TimeColumnSampling(int numRows) {
         super(numRows);
     }
 
     @Override
     public void parse(String value) {
         super.parse(value);
 
         if (value == null) {
             // any score are not changed
             return;
         }
 
         for (int i = 0; i < timeScores.length; i++) {
             ParsePosition pp = new ParsePosition(0);
             Date d = SDF_LIST[i].parse(value, pp);
             if (d != null && pp.getErrorIndex() == -1) {
                 timeScores[i] += 1;
             }
         }
     }
 
     public ColumnType getColumnTypeRank() {
         return super.getRank();
     }
 
     public String getSTRFTimeFormatRank() {
         int max = -numRows;
         int maxIndex = 0;
         for (int i = 0; i < timeScores.length; i++) {
             if (max <= timeScores[i]) {
                 max = timeScores[i];
                 maxIndex = i;
             }
         }
 
         if (max == 0) {
             return null;
         } else {
             return STRF_LIST[maxIndex];
         }
     }
 
 }
