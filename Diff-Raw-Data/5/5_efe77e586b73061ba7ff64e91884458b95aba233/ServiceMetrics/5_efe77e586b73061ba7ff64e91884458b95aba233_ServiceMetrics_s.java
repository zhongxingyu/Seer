 /*
  * Copyright 2009 Members of the EGEE Collaboration.
  * See http://www.eu-egee.org/partners for details on the copyright holders. 
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.common;
 
 import java.io.PrintWriter;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.glite.authz.common.util.Strings;
 
 /** A set of metrics kept about a running service. */
 @ThreadSafe
 public class ServiceMetrics {
 
     /** Java runtime. */
    Runtime runtime;
 
     /** ID for the service. */
     private String serviceId;
 
     /** Time the service started. */
     private long startupTime;
 
    /** Total number of completed requests to service */
     private long totalRequests;
 
     /** Total number of request that error'ed out. */
     private long totalErrors;
 
     /**
      * Constructor. 
      * 
      * @param id ID of the service whose metrics are being tracked
      */
     public ServiceMetrics(String id) {
         runtime = Runtime.getRuntime();
         serviceId = Strings.safeTrimOrNullString(id);
         startupTime = System.currentTimeMillis();
         totalRequests = 0;
         totalErrors = 0;
     }
 
     /**
      * Gets an identifier for the service whose metrics are being tracked.
      * 
      * @return the identifier for the service whose metrics are being tracked
      */
     public String getServiceId() {
         return serviceId;
     }
 
     /**
      * Gets the time that the service was started. The time is expressed in the system's default timezone.
      * 
      * @return time that PEP daemon was started
      */
     public long getServiceStartupTime() {
         return startupTime;
     }
 
     /**
      * Gets the total number of completed requests, successful or otherwise, serviced.
      * 
      * @return total number of completed requests
      */
     public long getTotalServiceRequests() {
         return totalRequests;
     }
 
     /** Adds one to the total number of requests. */
     public void incrementTotalServiceRequests() {
         totalRequests++;
     }
 
     /**
      * Gets the total number of requests that error'ed out.
      * 
      * @return total number of requests that error'ed out
      */
     public long getTotalServiceRequestErrors() {
         return totalErrors;
     }
 
     /** Adds one to the total number of requests that have error'ed out. */
     public void incrementTotalServiceRequestErrors() {
         totalErrors++;
     }
 
     /**
      * Prints metric information to the output writer. The following lines are printed:
      * <ul>
      * <li>service: <i>service_id</i></li>
      * <li>start time: <i>service_start_time</i></li>
      * <li>number of processors: <i>number_of_cpu_cores</i></li>
      * <li>memory usage: <i>used_megabytes</i>MB</li>
      * <li>total requests: <i>total_requests</i></li>
      * <li>total completed requests: <i>total_completed_requests</i></li>
      * <li>total request errors: <i>total_errors_requests</i></li>
      * </ul>
      * 
      * @param writer writer to which metrics are printed
      */
     public void printServiceMetrics(PrintWriter writer) {
         long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
 
         writer.println("service: " + serviceId);
         writer.println("start_time: " + startupTime);
         writer.println("number_of_processors: " + runtime.availableProcessors());
         writer.println("memory_usage: " + usedMemory + "MB");
         writer.println("total_requests: " + getTotalServiceRequests());
         writer.println("total_completed_requests: "
                 + (getTotalServiceRequests() - getTotalServiceRequestErrors()));
         writer.println("total_request_errors: " + getTotalServiceRequestErrors());
 
     }
 }
