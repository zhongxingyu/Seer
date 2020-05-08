 /*
  * This file is part of QuarterBukkit-Plugin.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * QuarterBukkit-Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * QuarterBukkit-Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with QuarterBukkit-Plugin. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.quarterbukkit.api.query;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONValue;
 import com.quartercode.quarterbukkit.QuarterBukkit;
 import com.quartercode.quarterbukkit.api.query.QueryException.QueryExceptionType;
 
 /**
  * A server mods api query can be used to query the official server mods api (https://api.curseforge.com/servermods).
  * For example, it is used by the {@link Updater} class.
  */
 public class ServerModsAPIQuery {
 
     private static final String HOST               = "https://api.curseforge.com/servermods/";
     private static final String USER_AGENT         = "QuarterBukkit/" + ServerModsAPIQuery.class.getSimpleName();
    private static final int    CONNECTION_TIMEOUT = 5 * 1000;
 
     private final String        query;
 
     /**
      * Creates a new query whose GET request {@link URL} is made out of the given query string attached to {@code https://api.curseforge.com/servermods/}.
      * 
      * @param query The query string for the server mods api to process.
      */
     public ServerModsAPIQuery(String query) {
 
         this.query = query;
     }
 
     /**
      * Returns the query string which is used by the {@link #execute()} method to build the request {@link URL}.
      * 
      * @return The query string for the server mods api to process.
      */
     public String getQuery() {
 
         return query;
     }
 
     /**
      * Executes the stored query and returns the result as a {@link JSONArray}.
      * The query string ({@link #getQuery()}) is attached to {@code https://api.curseforge.com/servermods/}.
      * The GET response of that {@link URL} is then parsed to a {@link JSONArray}.
      * 
      * @return The response of the server mods api.
      * @throws QueryException Something goes wrong while querying the server mods api.
      */
     public JSONArray execute() throws QueryException {
 
         // Build request url using the query
         URL requestUrl = null;
         try {
             requestUrl = new URL(HOST + query);
         }
         catch (MalformedURLException e) {
             throw new QueryException(QueryExceptionType.MALFORMED_URL, this, HOST + query, e);
         }
 
         // Open connection to request url
         URLConnection request = null;
         try {
             request = requestUrl.openConnection();
         }
         catch (IOException e) {
             throw new QueryException(QueryExceptionType.CANNOT_OPEN_CONNECTION, this, requestUrl.toExternalForm(), e);
         }
 
         // Set connection timeout
         request.setConnectTimeout(CONNECTION_TIMEOUT);
 
         // Set user agent
         request.addRequestProperty("User-Agent", USER_AGENT);
 
         // Set api key (if provided)
         String apiKey = QuarterBukkit.getPlugin().getConfig().getString("server-mods-api-key");
         if (apiKey != null && !apiKey.isEmpty()) {
             request.addRequestProperty("X-API-Key", apiKey);
         }
 
         // We want to read the results
         request.setDoOutput(true);
 
         // Read first line from the response
         BufferedReader reader = null;
         String response = null;
         try {
             reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
             response = reader.readLine();
         }
         catch (IOException e) {
             if (e.getMessage().contains("HTTP response code: 403")) {
                 throw new QueryException(QueryExceptionType.INVALID_API_KEY, this, requestUrl.toExternalForm(), e);
             } else {
                 throw new QueryException(QueryExceptionType.CANNOT_READ_RESPONSE, this, requestUrl.toExternalForm(), e);
             }
         }
         finally {
             // Can't be null because we return in that case (see the catch block)
             try {
                 reader.close();
             }
             catch (IOException e) {
                 throw new QueryException(QueryExceptionType.CANNOT_CLOSE_RESPONSE_STREAM, this, requestUrl.toExternalForm(), e);
             }
         }
 
         // Parse the response
         Object jsonResponse = JSONValue.parse(response);
 
         if (jsonResponse instanceof JSONArray) {
             return (JSONArray) jsonResponse;
         } else {
             throw new QueryException(QueryExceptionType.INVALID_RESPONSE, this, requestUrl.toExternalForm());
         }
     }
 
 }
