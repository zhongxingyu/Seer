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
 
 import org.jboss.test.faces.FacesEnvironment;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * <p class="changed_added_4_0"></p>
  * @author asmirnov@exadel.com
  *
  */
 public class HtmlUnitEnvironment extends FacesEnvironment {
     
     
 
     private LocalWebClient webClient;
 
     /**
      * <p class="changed_added_4_0"></p>
      */
     public HtmlUnitEnvironment() {
         // TODO Auto-generated constructor stub
     }
 
     @Override
    public void start() {
         super.start();
         this.webClient = new LocalWebClient(getServer());
     }
     
     public HtmlPage getPage(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
         HtmlPage page = webClient.getPage("http://localhost"+url);
         return page;
 
     }
     
     @Override
    public void release() throws Exception {
         webClient.closeAllWindows();
         webClient = null;
         super.release();
     }
 }
