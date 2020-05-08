 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 Tim Joyce
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Tim Joyce <timj@paneris.org>
  *     http://paneris.org/
  *     68 Sandbanks Rd, Poole, Dorset. BH14 8BY. UK
  */
 
 package org.melati.test;
 
 import java.io.*;
 import java.util.Hashtable;
 
 import javax.servlet.ServletException;
 
 import org.melati.servlet.*;
 import org.melati.Melati;
 import org.melati.MelatiConfig;
 import org.melati.util.MelatiBugMelatiException;
 import org.melati.util.MelatiWriter;
 import org.melati.util.MelatiException;
 import org.melati.util.ExceptionUtils;
 
 public class ConfigServletTest extends ConfigServlet {
 
   protected void doConfiguredRequest(Melati melati)
   throws ServletException, IOException {
 
     String method = melati.getMethod();
     if (method != null) {
       if (method.equals("Upload")) {
         Hashtable fields = null;
         try {
           InputStream in = melati.getRequest().getInputStream();
           MultipartDataDecoder decoder=
             new MultipartDataDecoder(melati,
                                      in,
                                      melati.getRequest().getContentType(),
                                      melati.getConfig().getFormDataAdaptorFactory());
           fields = decoder.parseData();
         }
         catch (IOException e) {
           melati.getWriter().write(
             "There was some error uploading your file:" +
               ExceptionUtils.stackTrace(e));
           return;
         }
         MultipartFormField field = (MultipartFormField)fields.get("file");
         if (field == null) {
           melati.getWriter().write("No file was uploaded");
           return;
         }
         byte[] data = field.getData();
         melati.getResponse().setContentType(field.getContentType());
         OutputStream output = melati.getResponse().getOutputStream();
         output.write(data);
         output.close();
         return;
       }
     }
 
     MelatiConfig config = melati.getConfig();
     melati.getResponse().setContentType("text/html");
     MelatiWriter output = melati.getWriter();
 
     output.write(
     "<html><head><title>ConfigServlet Test</title></head><body><h2> " +
     "ConfigServlet Test</h2><p>This servlet tests your basic melati " +
     "configuration. If you can read this message, it means that you have " +
     "successfully created a Melati and a Melati, using the configuration " +
     "given in org.melati.MelatiServlet.properties.  Please note that this " +
     "servlet does not construct a POEM session or initialise a template " +
     "engine.</p><h4>Your Melati is configured with the following parameters: " +
     "</h4><table><tr><td>TemplateEngine</td><td>" +
     config.getTemplateEngine().getClass().getName() +
     "</td></tr><tr><td>AccessHandler</td><td>" +
     config.getAccessHandler().getClass().getName() +
     "</td></tr><tr><td>FormDataAdaptorFactory</td><td>" +
     config.getFormDataAdaptorFactory().getClass().getName() +
     "</td></tr><tr><td>Locale</td><td>" +
     config.getLocale().getClass().getName() +
     "</td></tr><tr><td>TempletLoader</td><td>" +
     config.getTempletLoader().getClass().getName() +
     "</td></tr><tr><td>JavascriptLibraryURL</td><td>" +
     config.getJavascriptLibraryURL() +
     "</td></tr><tr><td>StaticURL</td><td>" +
     config.getStaticURL() + "</td></tr></table>" +
 
     "<h4>This servlet was called with the following Method (taken from " +
     "melati.getMethod()): " + 
     melati.getMethod() + 
     "</h4><h4>Further Testing:</h4>You can test melati Exception handling by " +
     "clicking <a href=" + 
     melati.getSameURL() + 
     "/Exception>Exception</a><br>You can test melati Redirect " +
     "handling by clicking <a href=" +
     melati.getSameURL() + 
     "/Redirect>Redirect</a><br>You can test your " +
     "POEM setup (connecting to logical database <tt>melatitest</tt>) by " +
     "clicking <a href=" + 
     melati.getZoneURL() + 
     "/org.melati.test.PoemServletTest/melatitest/>" +
     "org.melati.test.PoemServletTest/melatitest/</a><br>" +
    "<form method=\"post\" action=\"" + 
    melati.getSameURL() + 
    "/Upload\" enctype=\"multipart/form-data\" target=_blank>" +
     "You can upload a file here:<br>" +
     "<input type=hidden name=upload value=yes>" +
     "<input type=\"file\" name=\"file\" enctype=\"multipart/form-data\">" +
     "<input type=\"submit\" name=\"Submit\" value=\"Upload file\"><br>" +
     getUploadMessage(melati) +
     "</form>");
     if (method != null) {
       if (method.equals("Exception")) 
         throw new MelatiBugMelatiException("It got caught!");
       if (method.equals("Redirect")) {
         melati.getResponse().sendRedirect("http://www.melati.org");
         return;
       }
     }
   }
 
 /**
  * this simply demonstrates how to use a different melati configuration
  */
   protected MelatiConfig melatiConfig() throws MelatiException {
     MelatiConfig config = super.melatiConfig();
     config.setFormDataAdaptorFactory(new MemoryDataAdaptorFactory());
     return config;
   }
 
   protected String getUploadMessage(Melati melati) {
     return "This will save your file in memory. Try saving a file in your " +
            "/tmp directory <a href='" + melati.getZoneURL() +
            "/org.melati.test.ConfigServletTestOverride/'>here</a>.";
   }
 
 }
 
 
 
 
 
 
