 /** 
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: PolicyDecisionTask.java,v 1.2 2009-02-04 22:17:45 veiming Exp $
  */
 
 package com.sun.identity.policy;
 
 import com.sun.identity.policy.interfaces.ResourceName;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
 * This class maintains a set of tasks i.e. policy evaluations.
 * And make sure that policy evaluation will not be performed
 * on the same resource twice.
  */
 public class PolicyDecisionTask {
     Map<Policy, Map<String, Task>> repo = new 
         HashMap<Policy, Map<String, Task>>();
     
     synchronized Task addTask(
         ResourceName resComparator,
         Policy policy, 
         String resource
     ) {
         Map<String, Task> map = repo.get(policy);
         
         if (map == null) {
             map = new HashMap<String, Task>();
             repo.put(policy, map);
             Task task = new Task(policy, resource);
             map.put(resource, task);
             return task;
         }
 
         if (resource.indexOf('*') != -1) {
             Task task = map.get(resource);
             if (task == null) {
                 task = new Task(policy, resource);
                 map.put(resource, task);
                 return task;
             }
             return task;
         }
         
         for (String res : map.keySet()) {
             ResourceMatch match = resComparator.compare(res, resource, true);
             if (match.equals(ResourceMatch.EXACT_MATCH) ||
                 match.equals(ResourceMatch.WILDCARD_MATCH)) {
                 return map.get(res);
             }
         }
         
         Task task = new Task(policy, resource);
         map.put(resource, task);
         return task;
     }
 
 
     class Task {
         Policy policy;
         String resource;
         PolicyDecision policyDecision;
         
         Task(Policy policy, String resource) {
             this.policy = policy;
             this.resource = resource;
         }
     }
 }
