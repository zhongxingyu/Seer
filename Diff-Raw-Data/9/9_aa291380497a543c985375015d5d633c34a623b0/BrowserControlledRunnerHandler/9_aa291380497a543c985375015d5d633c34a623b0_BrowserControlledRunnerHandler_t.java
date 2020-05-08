 /*
  * Copyright 2010 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.server.handlers;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.jstestdriver.FilesCache;
 import com.google.jstestdriver.JsonCommand;
 import com.google.jstestdriver.JsonCommand.CommandType;
 import com.google.jstestdriver.SlaveBrowser;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 import com.google.jstestdriver.requesthandlers.RequestHandler;
 import com.google.jstestdriver.runner.RunnerType;
 import com.google.jstestdriver.server.handlers.pages.Page;
 import com.google.jstestdriver.server.handlers.pages.PageType;
 import com.google.jstestdriver.server.handlers.pages.SlavePageRequest;
 import com.google.jstestdriver.util.HtmlWriter;
 
 import org.mortbay.jetty.MimeTypes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Handler for the browser controlled test running mode.
  *
  * @author corbinrsmith@gmail.com (Cory Smith)
  */
 public class BrowserControlledRunnerHandler implements RequestHandler {
 
   private static final Logger logger =
       LoggerFactory.getLogger(BrowserControlledRunnerHandler.class);
 
   private final Gson gson = new Gson();
   private final SlavePageRequest request;
   private final HttpServletResponse response;
 
   private final HandlerPathPrefix prefix;
 
   private final Map<PageType, Page> pages;
 
   @Inject
   public BrowserControlledRunnerHandler(
       SlavePageRequest request,
       HttpServletResponse response,
       HandlerPathPrefix prefix,
       Map<PageType, Page> pages) {
     this.request = request;
     this.response = response;
     this.prefix = prefix;
     this.pages = pages;
   }
 
   @Override
   public void handleIt() throws IOException {
     final SlaveBrowser browser = request.getBrowser();
     if (browser == null) {
       // re-capture browser as standalone
       response.sendRedirect(
           prefix.prefixPath("/capture/" + CaptureHandler.RUNNER_TYPE + "/" + RunnerType.BROWSER + "/timeout/-1"));
     } else {
       browser.heartBeat();
       // start test running
       response.setContentType(MimeTypes.TEXT_HTML_UTF_8);
       final HtmlWriter writer = new HtmlWriter(response.getWriter(), prefix);
       request.writeDTD(writer);
       pages.get(request.getPageType()).render(writer, request);
       service(browser);
      writer.flush();
     }
   }
 
   public void service(final SlaveBrowser slaveBrowser) {
     logger.debug("Adding noop command.");
     slaveBrowser.createCommand(gson.toJson(new JsonCommand(CommandType.NOOP, null)));
     LinkedList<String> runAllTestsParameters = new LinkedList<String>();
 
     runAllTestsParameters.add("false");
     runAllTestsParameters.add("false");
     runAllTestsParameters.add("0");
     logger.debug("adding run all tests command.");
     slaveBrowser.createCommand(gson.toJson(new JsonCommand(CommandType.RUNALLTESTS,
         runAllTestsParameters)));
     // stop the executor until the next refresh.
     slaveBrowser.createCommand(gson.toJson(new JsonCommand(CommandType.STOP, null)));
     
   }
 }
