 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.komusubi.feeder.model.airline;
 
 import java.util.regex.Pattern;
 
 /**
  *
  * @author jun.ozeki 2013/11/27
  */
public class FlightNumber {
     private static final Pattern pattern = Pattern.compile("[\\d]{3,}");
     private String number;
 
     public FlightNumber(String number) {
         if (!validate(number))
             throw new IllegalArgumentException("wrong format: flight number: " + number);
         this.number = number;
     }
 
     public String getNumber() {
         return number;
     }
     
     public static boolean validate(String number) {
         return pattern.matcher(number).find();
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("FlightNumber [number=").append(number).append("]");
         return builder.toString();
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((number == null) ? 0 : number.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         FlightNumber other = (FlightNumber) obj;
         if (number == null) {
             if (other.number != null)
                 return false;
         } else if (!number.equals(other.number))
             return false;
         return true;
     }
    
 }
