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
 * $Id: ResourceNameSpliter.java,v 1.2 2009-01-15 01:59:49 veiming Exp $
  */
 
 package com.sun.identity.entitlement.util;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 /*
  * This class splits resource name (URL) to different parts so that they
  * can be used for resource name comparison.
  */
 public class ResourceNameSpliter {
     private ResourceNameSpliter() {
     }
 
     /**
      * Returns a list of sub parts of host of an URL.
      *
      * @param url URL.
      * @param a list of sub parts of host of an URL.
      */
     public static Set<String> splitHost(URL url) {
         Set<String> results = new HashSet<String>();
        String protocol = url.getProtocol();
        String host = url.getHost();
 
         protocol += "://";
 
         results.add(protocol);
         results.add(protocol + host);
 
         List<String> dns = getDNS(host);
         String buff = "";
         for (String s : dns) {
             if (buff.length() > 0) {
                 results.add(protocol + "." + s + buff);
             } else {
                 results.add(protocol + "." + s);
             }
 
             buff += "." + s;
         }
         
         return results;
     }
 
     private static List<String> getDNS(String host) {
         List<String> result = new ArrayList<String>();
         StringTokenizer st = new StringTokenizer(host, ".");
         boolean first = true;
         while (st.hasMoreTokens()) {
             String s = st.nextToken();
             if (first) {
                 first = false;
             } else {
                 result.add(0, s);
             }
         }
         return result;
     }
     
     public static Set<String> splitPath(URL url) {
         Set<String> results = new HashSet<String>();
        String path = url.getPath();
         Set<String> queries = normalizeQuery(url.getQuery());
         results.add("/");
         for (String q : queries) {
             results.add("/?" + q);
         }
         
         if ((path.length() > 0) && !path.equals("/")) {
             String prefix = "";
             StringTokenizer st = new StringTokenizer(path, "/");
             while (st.hasMoreTokens()) {
                 String s = st.nextToken();
                 prefix += "/" + s;
                 results.add(prefix);
                 for (String q : queries) {
                     results.add(prefix + "?" + q);
                 }
             }
         }
         return results;
     }
     
     private static Set<String> normalizeQuery(String path) {
         Set<String> results = new HashSet<String>();
         if ((path == null) || (path.length() == 0)) {
             return results;
         }
 
         List<List<String>> possibleCombinations = new ArrayList<List<String>>();        
         List<String> list = new ArrayList<String>();
         
         StringTokenizer st = new StringTokenizer(path, "&");
         while (st.hasMoreTokens()) {
             list.add(st.nextToken());
         }
         possibleCombinations.add(list);
         
         Map<String, String> map = new HashMap<String, String>();
         for (String s : list) {
             int idx = s.indexOf('=');
             String key = (idx == -1) ? s : s.substring(0, idx);
             String val = ((idx == -1) || (idx == (s.length() -1))) 
                 ? "" : s.substring(idx+1);
             map.put(key, val);
         }
         
         Set<String> keys = new HashSet<String>();
         for (String s : map.keySet()) {
             keys.add(s);
         }
 
         List<String> allBlanks = new ArrayList<String>();
         for (String s : keys) {
             allBlanks.add(s + "=");
         }
         possibleCombinations.add(allBlanks);
         
         while (!keys.isEmpty()) {
             String s = keys.iterator().next();
             List<String> l = new ArrayList<String>();
             for (String key : map.keySet()) {
                 String val = key.equals(s) ? "" : map.get(key);
                 l.add(key + "=" + val);
             }
             possibleCombinations.add(l);
             keys.remove(s);
         }
         
         for (List<String> l : possibleCombinations) {
             results.add(queryToString(l));
         }
         
         return results;
     }
     
    private static String queryToString(List<String> query) {
         StringBuffer buff = new StringBuffer();
         Collections.sort(query);
         boolean first = true;
         for (String s : query) {
             if (first) {
                 first = false;
             } else {
                 buff.append("&");
             }
             buff.append(s);
         }
         return buff.toString();
     }
 
 }
