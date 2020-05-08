 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2012 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.web;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.rapidcontext.app.ApplicationContext;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Binary;
import org.rapidcontext.core.js.JsSerializer;
 import org.rapidcontext.core.storage.Metadata;
 import org.rapidcontext.core.storage.Path;
 import org.rapidcontext.core.storage.StorageException;
 import org.rapidcontext.core.web.Mime;
 import org.rapidcontext.core.web.Request;
 import org.rapidcontext.core.web.RequestHandler;
 
 /**
  * An app request handler. This request handler is used to return the
  * HTML document for initializing the platform and launching an app.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class AppRequestHandler extends RequestHandler {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(AppRequestHandler.class.getName());
 
     /**
      * Returns the HTTP methods supported for the specified request
      * (path). This method assumes local request paths (removal of
      * the mapped URL base).
      *
      * @param request        the request to check
      *
      * @return the array of HTTP method names supported
      */
     public String[] methods(Request request) {
         return GET_METHODS_ONLY;
     }
 
     /**
      * Processes an HTTP GET request.
      *
      * @param request        the request to process
      */
     protected void doGet(Request request) {
         String  appId = StringUtils.substringBefore(request.getPath(), "/");
 
         try {
             processApp(request, appId);
         } catch (StorageException e) {
             LOG.log(Level.WARNING, "failed to launch app: " + appId, e);
             errorInternal(request, e.getMessage());
         } catch (IOException e) {
             LOG.log(Level.WARNING, "failed to launch app: " + appId, e);
             errorInternal(request, e.getMessage());
         }
     }
 
     /**
      * Processes an HTML app launcher request. This method loads the
      * app launcher template from storage and replaces all template
      * variables with their corresponding search results and values.
      *
      * @param request        the request to process
      * @param appId          the app identifier to launch (or null)
      *
      * @throws StorageException if the app template couln't be found
      * @throws IOException if the template file couldn't be read properly
      */
     protected static void processApp(Request request, String appId)
         throws StorageException, IOException {
 
         ApplicationContext  ctx = ApplicationContext.getInstance();
         Path                path;
         Binary              template;
         InputStreamReader   is;
         BufferedReader      reader;
         String              line;
         Array               files;
         Object              obj;
         StringBuilder       res = new StringBuilder();
 
         path = FileRequestHandler.PATH_FILES.child("index.tmpl", false);
         obj = ctx.getStorage().load(path);
         if (obj instanceof Binary) {
             template = (Binary) obj;
         } else {
             throw new StorageException("App template 'index.tmpl' not found.");
         }
         is = new InputStreamReader(template.openStream(), "UTF-8");
         reader = new BufferedReader(is);
         while ((line = reader.readLine()) != null) {
             if (line.contains("%APP_ID%")) {
                res.append(line.replace("%APP_ID%", JsSerializer.serialize(appId)));
                 res.append("\n");
             } else if (line.contains("%BASE_URL%")) {
                 res.append(line.replace("%BASE_URL%", request.getRootUrl()));
                 res.append("\n");
             } else if (line.contains("%JS_FILES%")) {
                 files = findFileResources("js");
                 for (int i = 0; i < files.size(); i++) {
                     res.append(line.replace("%JS_FILES%", files.getString(i, "")));
                     res.append("\n");
                 }
             } else if (line.contains("%CSS_FILES%")) {
                 files = findFileResources("css");
                 for (int i = 0; i < files.size(); i++) {
                     res.append(line.replace("%CSS_FILES%", files.getString(i, "")));
                     res.append("\n");
                 }
             } else {
                 res.append(line);
                 res.append("\n");
             }
         }
         reader.close();
         request.sendText(Mime.HTML[0], res.toString());
     }
 
     /**
      * Finds all binary files of a specified type from the storage.
      * Only binary files in the named subdirectory (i.e. "files/css")
      * with the specified type as suffix (i.e. "*.css") will be
      * returned.
      *
      * @param type           the file type to find
      *
      * @return a sorted list of all matching files (relative paths)
      *     found in storage
      */
     protected static Array findFileResources(String type) {
         ApplicationContext  ctx = ApplicationContext.getInstance();
         String              root = FileRequestHandler.PATH_FILES.toString();
         Path                path = FileRequestHandler.PATH_FILES.child(type, true);
         Metadata[]          meta;
         String              file;
         Array               res = new Array();
 
         meta = ctx.getStorage().lookupAll(path);
         for (int i = 0; i < meta.length; i++) {
             file = StringUtils.removeStart(meta[i].path().toString(), root);
             if (meta[i].isBinary() && file.endsWith("." + type)) {
                 res.add(file);
             }
         }
         res.sort();
         return res;
     }
 }
