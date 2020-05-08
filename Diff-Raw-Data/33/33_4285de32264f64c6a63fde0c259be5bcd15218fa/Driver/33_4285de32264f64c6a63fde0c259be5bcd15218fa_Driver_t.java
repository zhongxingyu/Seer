 /*
  * Copyright 2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.spt.rms.rep.main;
 
import com.spt.rms.rep.domain.Report;
import com.spt.rms.rep.rest.RestClient;

 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Driver {
 
     public static void main(String[] args) {
         String URL = "http://localhost:8080/RMSReportGenerator/";
 
         String input = " CreDate: Sun Jan 30 15:41:08 ICT 2011";
         System.out.println("#" + input.substring(input.indexOf(":") + 1, input.length()).trim() + "#");
 
         //Sun Jan 30 14:58:54 ICT 2011
         String dateString = input.substring(input.indexOf(":") + 1, input.length()).trim();
         DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
 
         // Parse the date
         try {
             Date date = format.parse(dateString);
             System.out.println("Original string: " + dateString);
             System.out.println("Parsed date    : " + date.toString());
         } catch (ParseException pe) {
             System.out.println("ERROR: could not parse date in string \""
                     + dateString + "\"");
         }
     }
 }
