 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.swissarmybundle;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import javax.jcr.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.sling.SlingServlet;
 import org.apache.sling.api.servlets.*;
 import org.apache.sling.api.*;
 import javax.servlet.*;
 
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.content.*;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Bundle;
 import org.osgi.service.component.ComponentContext;
 
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Service;
 
 import clojure.lang.RT;
 import clojure.lang.Var;
 import clojure.lang.Compiler;
 
 import org.python.core.PyException;
 import org.python.util.PythonInterpreter;
 
 import org.jruby.Ruby;
 import org.jruby.runtime.GlobalVariable;
 import org.jruby.javasupport.JavaObject;
 import org.jruby.javasupport.JavaEmbedUtils;
 import org.jruby.javasupport.JavaUtil;
 
 
 
 @Component(immediate = true, metatype = true)
 @Service(value = SwissArmyService.class)
 public class SwissArmyServiceImpl implements SwissArmyService
 {
     @Reference
     Repository repository;
 
     BundleContext bundleContext;
     ComponentContext componentContext;
 
     public void activate(ComponentContext context)
     {
         componentContext = context;
         bundleContext = componentContext.getBundleContext();
     }
 
 
     class SwissArmyClassLoader extends URLClassLoader
     {
         ClassLoader baseLoader;
 
         public SwissArmyClassLoader () {
             super(new URL[] {}, Thread.currentThread().getContextClassLoader());
         }
 
         public SwissArmyClassLoader (ClassLoader baseLoader) {
             super(new URL[] {}, baseLoader);
             this.baseLoader = baseLoader;
         }
 
         protected Class findClass (String name) throws ClassNotFoundException
         {
             if (baseLoader != null) {
                 try {
                     return baseLoader.loadClass (name);
                 } catch (ClassNotFoundException e) {
                     // OK... you asked for it
                 }
             }
 
             // Clojure asks for a lot of classes with no packages, but we'll
             // never find them anyway so don't bother.
             if (name.contains(".")) {
                 Bundle[] bundles = bundleContext.getBundles();
 
                 for (Bundle b : bundles) {
                     try {
                         if (b != bundleContext.getBundle()) {
                             return b.loadClass (name);
                         }
                     } catch (ClassNotFoundException e) {
                         // keep trying...
                     }
                 }
             }
 
             throw new ClassNotFoundException ("Couldn't find class: " + name);
         }
     }
 
 
 
     private int findAvailablePort (int port)
     {
         try {
             ServerSocket sock = new ServerSocket(port);
             sock.close();
             return port;
         } catch (Exception e) {
             return findAvailablePort(port + 1);
         }
     }
 
 
     public Map mapOf (Object ... args)
     {
         Map map = new HashMap();
 
         for (int i = 0; i < args.length; i += 2) {
             map.put ((String) args[i], args[i + 1]);
         }
 
         return map;
     }
 
 
     public void launchSwank (SlingHttpServletRequest request,
                              SlingHttpServletResponse response,
                              Map properties,
                              Object caller)
         throws ServletException
     {
         Thread.currentThread().setContextClassLoader(new SwissArmyClassLoader(caller.getClass().getClassLoader()));
         Object sleeper = new Object();
 
         try {
             Var clj_request = RT.var("user", "sling-request");
             clj_request.bindRoot(request);
 
             Var clj_response = RT.var("user", "sling-response");
             clj_response.bindRoot(response);
 
             Var clj_repository = RT.var("user", "repository");
             clj_repository.bindRoot(repository);
 
             Var clj_componentcontext = RT.var("user", "component-context");
             clj_componentcontext.bindRoot(componentContext);
 
             Var clj_bundlecontext = RT.var("user", "bundle-context");
             clj_bundlecontext.bindRoot(bundleContext);
 
             Var clj_extraproperties = RT.var("user", "extra-properties");
             clj_extraproperties.bindRoot(properties);
 
             int port = findAvailablePort(4010);
 
             StringReader in = new StringReader
                 (String.format ("(do (require 'swank.swank)" +
                                 "    (ns user)" +
                                "    (require 'clojure.contrib.repl-utils)" +
                                "    (defn show [thing] (clojure.contrib.repl-utils/show thing))" +
                                 "    (defn reference [c] (.getService bundle-context (.getServiceReference bundle-context c)))" +
                                 "    (defn find-class [c] (some #(try (.loadClass %% c) (catch ClassNotFoundException _)) (.getBundles bundle-context)))" +
                                 "    (.println System/err \"Swank listening on port: %d\")" +
                                 "    (with-open [ss (java.net.ServerSocket. %d)]" +
                                 "      (def client-socket (.accept ss))" +
                                 "      (let [connection (swank.core.connection/make-connection client-socket)" +
                                 "            out-redir (java.io.PrintWriter. (@#'swank.core.server/make-output-redirection connection))]" +
                                 "        (binding [*out* out-redir" +
                                 "                  *err* out-redir]" +
                                 "          (dosync (ref-set (connection :writer-redir) *out*))" +
                                 "          (@#'swank.swank/connection-serve connection)))" +
                                 "      ))",
                                 port, port));
 
             Compiler.load (in);
 
             Var clj_client_socket = RT.var("user", "client-socket");
             Socket client_socket = (java.net.Socket)clj_client_socket.get();
 
             while (!client_socket.isClosed ()) {
                 Thread.sleep(2000);
             }
 
         } catch (Exception e) {
             // Don't let our exceptions interfere with the caller...
         }
     }
 
 
 
     public void launchPython (SlingHttpServletRequest request,
                               SlingHttpServletResponse response,
                               Map properties,
                               Object caller)
         throws ServletException
     {
         Thread.currentThread().setContextClassLoader(new SwissArmyClassLoader(caller.getClass().getClassLoader()));
         Object sleeper = new Object();
 
         try {
             PythonInterpreter python = new PythonInterpreter();
 
             URL repl_code = bundleContext.getBundle().getResource("repl.py");
 
             python.execfile(repl_code.openStream());
 
 
             python.set("sling_request", request);
             python.set("sling_response", response);
             python.set("repository", repository);
             python.set("component_context", componentContext);
             python.set("bundle_context", bundleContext);
             python.set("extra_properties", properties);
 
             python.exec("import java");
             python.exec("from java.lang import *");
 
             python.exec("def reference(c): return bundle_context.getService(bundle_context.getServiceReference(c))");
             python.exec("def find_class(c):\n" +
                         "    for b in bundle_context.getBundles():\n" +
                         "        try:\n" +
                         "            return b.loadClass(c)\n" +
                         "        except ClassNotFoundException:\n" +
                         "            pass\n");
 
             int port = findAvailablePort(4010);
 
             python.exec("start_repl(" + port + ")");
 
         } catch (Exception e) {
             // Don't let our exceptions interfere with the caller...
             e.printStackTrace();
         }
     }
 
 
     public void launchRuby (SlingHttpServletRequest request,
                             SlingHttpServletResponse response,
                             Map properties,
                             Object caller)
         throws ServletException
     {
         Thread.currentThread().setContextClassLoader(new SwissArmyClassLoader(caller.getClass().getClassLoader()));
         Object sleeper = new Object();
 
         try {
 
             Ruby ruby = Ruby.getDefaultInstance ();
 
             URL repl_code = bundleContext.getBundle ().getResource ("repl.rb");
 
             StringBuilder sb = new StringBuilder ();
             InputStreamReader in = new InputStreamReader(repl_code.openStream ());
 
             char[] buf = new char[256];
             int len;
 
             while ((len = in.read (buf)) > 0) {
                 sb.append(buf, 0, len);
             }
 
             ruby.executeScript(sb.toString (), "repl.rb");
 
             ruby.defineVariable(new GlobalVariable(ruby, "$sling_request", JavaUtil.convertJavaToRuby(ruby, request)));
             ruby.defineVariable(new GlobalVariable(ruby, "$sling_response", JavaUtil.convertJavaToRuby(ruby, response)));
             ruby.defineVariable(new GlobalVariable(ruby, "$repository", JavaUtil.convertJavaToRuby(ruby, repository)));
             ruby.defineVariable(new GlobalVariable(ruby, "$component_context", JavaUtil.convertJavaToRuby(ruby, componentContext)));
             ruby.defineVariable(new GlobalVariable(ruby, "$bundle_context", JavaUtil.convertJavaToRuby(ruby, bundleContext)));
             ruby.defineVariable(new GlobalVariable(ruby, "$extra_properties", JavaUtil.convertJavaToRuby(ruby, properties)));
 
             ruby.evalScriptlet("def reference(c)\n" +
                                "  $bundle_context.getService($bundle_context.getServiceReference(c))\n" +
                                "end\n");
 
             int port = findAvailablePort(4010);
 
             ruby.evalScriptlet("start_repl(" + port + ")");
 
             JavaEmbedUtils.terminate(ruby);
         } catch (Exception e) {
             // Don't let our exceptions interfere with the caller...
             e.printStackTrace();
         }
     }
 }
