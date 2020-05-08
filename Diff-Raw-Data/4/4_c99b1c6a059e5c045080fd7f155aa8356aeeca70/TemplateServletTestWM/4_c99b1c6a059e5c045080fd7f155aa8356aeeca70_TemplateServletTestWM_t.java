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
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 
 import org.webmacro.WebMacro;
 import org.webmacro.WM;
 import org.webmacro.Context;
 import org.webmacro.Template;
 
 import org.melati.servlet.TemplateServlet;
 import org.melati.Melati;
 import org.melati.MelatiConfig;
 import org.melati.servlet.PathInfoException;
 import org.melati.servlet.MelatiContext;
 import org.melati.servlet.MultipartFormField;
 import org.melati.template.TemplateEngine;
 import org.melati.template.TemplateContext;
 import org.melati.template.webmacro.MelatiFastWriter;
import org.melati.util.MelatiBugMelatiException;
 import org.melati.util.MelatiWriter;
 
 /**
  * Base class to use Melati with Servlets.
  * Simply extend this class, override the doRequest method
  *
  * @author Tim Joyce
  * $Revision$
  */
 public class TemplateServletTestWM extends TemplateServlet {
 
   protected String doTemplateRequest(Melati melati,
                                      TemplateContext templateContext) 
                                                           throws Exception {
     templateContext.put("RestrictedAccessObject", new RestrictedAccessObject());
 
     System.err.println("starting doTemplateRequest");
     if (melati.getMethod() != null) {
 
       if (melati.getMethod().equals("Upload")) {
         MultipartFormField field = templateContext.getMultipartForm("file");
         byte[] data = field.getData();
         melati.getResponse().setContentType(field.getContentType());
         OutputStream output = melati.getResponse().getOutputStream();
         output.write(data);
         output.close();
         return null;
       }
 
       if (melati.getMethod().equals("Redirect"))
         melati.getResponse().sendRedirect("http://www.melati.org");
 
       // test melati in standalone mode (outside of using the servlet API)
       // by expanding a template
       // to a string and then include it within this template
       // you would not normally do this this way, a much better approach would
       // be to use templets
       if (melati.getMethod().equals("StandAlone")) {
         // construct a Melati with a StringWriter instead of a servlet
         // request and response
         WebMacro wm = new WM();
         ByteArrayOutputStream sw = new ByteArrayOutputStream();
         MelatiFastWriter fmw = new MelatiFastWriter(sw, melati.getEncoding());
         Melati m = new Melati(new MelatiConfig(),fmw);
         Context context2 = wm.getContext();
         context2.put("melati",m);
         Template template = wm.getTemplate("test/StandAlone.wm");
         template.write(fmw.getPeer(), context2);
         fmw.flush();
         // write to the StringWriter
         String out = sw.toString();
         // finally, put what we have into the original templateContext
         templateContext.put("StandAlone",out);
       }
      if (melati.getMethod().equals("Exception")) 
        throw new MelatiBugMelatiException("It got caught!");
     }      
     System.err.println("completed doTemplateRequest");
     return("test/TemplateServletTestWM");
   }
 
 /*
  * set up the melati context so we don't have to specify the 
  * logicaldatabase on the pathinfo.  this is a very good idea when
  * writing your appications where you are typically only accessing
  * a single database
  */
   protected MelatiContext melatiContext(Melati melati)
   throws PathInfoException {
     return melatiContextWithLDB(melati,"melatitest");
   }
   
 }
 
 
 
 
 
 
