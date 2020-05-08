 /*
  * $Id$
  *
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.jboss.test.faces.htmlunit;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 
 import org.jboss.test.faces.ApplicationServer;
 import org.jboss.test.faces.FacesEnvironment;
 import org.jboss.test.faces.staging.StagingServer;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * <p class="changed_added_4_0">
  * </p>
  * 
  * @author asmirnov@exadel.com
  * 
  */
 public class HtmlUnitEnvironment extends FacesEnvironment {
 
     private WebClient webClient;
 
     public HtmlUnitEnvironment() {
         super();
     }
 
     public HtmlUnitEnvironment(ApplicationServer applicationServer) {
         super(applicationServer);
     }
 
     private WebClient createWebClient() {
         WebClient result;
         
         ApplicationServer server = getServer();
         if (server instanceof StagingServer) {
             result = new LocalWebClient((StagingServer) server);
         } else {
             result = new WebClient();
         }
 
         return result;
     }
     
     public WebClient getWebClient() {
         return this.webClient;
     }
     
     @Override
     public FacesEnvironment start() {
         super.start();
         this.webClient = createWebClient();
         return this;
     }
 
     public <P extends Page> P getPage(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
         String hostAddress = MessageFormat.format("http://localhost:{0,number,#####}", getServer().getPort());
        P page = webClient.<P>getPage(hostAddress + url);
         return page;
     }
 
     @Override
     public void release() {
         webClient.closeAllWindows();
         webClient = null;
         super.release();
     }
 }
