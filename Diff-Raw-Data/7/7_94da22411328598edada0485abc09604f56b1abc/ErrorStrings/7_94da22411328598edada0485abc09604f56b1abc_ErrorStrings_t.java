 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hoya.exceptions;
 
 public interface ErrorStrings {
   String E_UNSTABLE_CLUSTER = "Unstable Cluster:";
   String E_CLUSTER_RUNNING = "cluster already running";
   String E_ALREADY_EXISTS = "already exists";
   String PRINTF_E_ALREADY_EXISTS = "Hoya Cluster \"%s\" already exists and is defined in %s";
   String E_MISSING_PATH = "Missing path ";
   String E_INCOMPLETE_CLUSTER_SPEC =
     "Cluster specification is marked as incomplete: ";
   String E_UNKNOWN_CLUSTER = "Unknown cluster ";
   String E_DESTROY_CREATE_RACE_CONDITION =
       "created while it was being destroyed";
  String E_UNKNOWN_ROLE = "Unknown role ";
 }
