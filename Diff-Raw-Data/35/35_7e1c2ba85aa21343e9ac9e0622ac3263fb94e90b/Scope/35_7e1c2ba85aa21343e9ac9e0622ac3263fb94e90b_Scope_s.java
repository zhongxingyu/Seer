 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 import java.util.*;
 
 /**
  *
  * @author Tom Raney
  */
 public class Scope {
 
     public static final int MAX_PARAMETERS = 100;
     public static final String SEPARATOR = " ";
     public static final String DELIMITER = ":";
 
     public static final String[] VALID_PRIVILEGED_KEYS = {"source","type","bus","channel","sticky","messageURL"};
 
     public static boolean isValidKey(String key) {
         return Arrays.asList(VALID_PRIVILEGED_KEYS).contains(key);
     }
 
     /**
      * Reformulate space-delimited list of buses "busA.com busB.com ..."
      * to "bus:busA.com bus:busB.com ..."
      * @param spaceDelimitedBuses
      * @return
      */
     public static String convertToBusScope(String spaceDelimitedBuses) {
         StringBuilder sb = new StringBuilder();
         for (String scopeToken : spaceDelimitedBuses.split(" ")) {
             sb.append("bus:").append(scopeToken).append(" ");
         }
         if (sb.length() > 0) {
             sb.deleteCharAt(sb.length()-1);
         }
         return sb.toString();
     }
 
     public Scope(String scopeString) throws BackplaneServerException {
         this.scopes = parseScopeString(scopeString);
         this.scopeString = scopeString;
         logger.debug("Client requested scopes: " + scopeString);
     }
 
     /**
      * Return a list of bus names in requested scope
      * @return valid list, possibly empty
      */
 
     public @NotNull List<String> getBusesInScope() {
         return scopes.get("bus");
     }
 
     public @NotNull String getBusesInScopeAsString() {
         return org.springframework.util.StringUtils.collectionToDelimitedString(getBusesInScope(), " ");
     }
 
     /**
      * Return a list of channel names in requested scope
      * @return valid list, possibly empty
      */
 
     public @NotNull List<String> getChannelsInScope() {
         return scopes.get("channel");
     }
 
     public List<String> buildQueriesFromScope() {
 
         logger.info("build queries from scope: " + scopes.entrySet());
 
         ArrayList<String> queries = new ArrayList<String>();
 
         ArrayList scopeList = new ArrayList(scopes.entrySet());
 
         buildQuery(queries, "", scopeList, 0);
         return queries;
     }
 
     private void buildQuery(List<String> queryList, String query, List scopeList, int level) {
 
         if (level > scopeList.size()-1) {
             logger.info("add " + query);
             queryList.add(query);
             return;
         }
 
         Map.Entry<String,ArrayList<String>> entry = (Map.Entry<String, ArrayList<String>>) scopeList.get(level);
 
         if (entry.getValue().isEmpty()) {
             buildQuery(queryList, query, scopeList, level+1);
         }
 
         ArrayList<String> originalDisjunctionList = entry.getValue();
         ArrayList<String> compressedDisjunctionList = new ArrayList<String>();
 
         int size = originalDisjunctionList.size();
         int maxValuesInClause = 4, cnt=0;
 
         String queryS = entry.getKey() + " IN (";
         String body = "";
 
         for (String val: originalDisjunctionList) {
             if (cnt > 0) {
                 body += ",";
             }
             body += "'" + val + "'";
 
             cnt++;
             if (cnt >= maxValuesInClause || cnt >= size) {
                 logger.debug("added " + body);
                 compressedDisjunctionList.add(queryS + body + ")");
                 body = "";
                 size -= cnt;
                 cnt=0;
             }
         }
 
         for (String val : compressedDisjunctionList) {
             String conjunction = "";
             if (StringUtils.isNotBlank(query)) {
                 conjunction = " AND ";
             }
 
             String newQuery = query + conjunction + val;
             buildQuery(queryList, newQuery, scopeList, level+1);
         }
 
     }
 
     //private
 
     private String scopeString;
     private Map<String,ArrayList<String>> scopes;
     private static final Logger logger = Logger.getLogger(Scope.class);
 
     private Map<String,ArrayList<String>> parseScopeString(String scopeString) throws BackplaneServerException {
         HashMap<String,ArrayList<String>> scopes = new HashMap<String, ArrayList<String>>();
 
         // guarantee bus and channel have an entry
         scopes.put("bus", new ArrayList<String>());
         scopes.put("channel", new ArrayList<String>());
 
         if (StringUtils.isNotBlank(scopeString)) {
             // TODO: is there a maximum length for the scope string?
             //
             scopeString = scopeString.trim();
             logger.info("scopeString = '" + scopeString + "'");
             String[] tokens = scopeString.split(Scope.SEPARATOR,Scope.MAX_PARAMETERS);
             // all scope tokens need to have the ":" key/value delimiter
            // and, if they have the ":" in the value (like the source field MUST have)
             // we will use the first ":" as the key/value delimiter.
             for (String token:tokens) {
                 if (!token.contains(Scope.DELIMITER)) {
                     logger.debug("Malformed scope: '" + scopeString + "'");
                     throw new BackplaneServerException("Malformed scope");
                 }
                 String value = token.substring(token.indexOf(Scope.DELIMITER)+1);
                 String key = token.substring(0, token.indexOf(Scope.DELIMITER));
 
                 if (!Scope.isValidKey(key)) {
                     throw new BackplaneServerException(key + " is not a valid scope field name");
                 }
 
                 if (!scopes.containsKey(key)) {
                     scopes.put(key, new ArrayList<String>());
                 }
 
                 List<String> values = scopes.get(key);
                 // prevent duplicate scopes
                 if (!values.contains(value)) {
                     values.add(value);
                 }
             }
         }
         logger.info("map " + scopes + " generated from " + scopeString);
 
         return scopes;
     }
 
 }
