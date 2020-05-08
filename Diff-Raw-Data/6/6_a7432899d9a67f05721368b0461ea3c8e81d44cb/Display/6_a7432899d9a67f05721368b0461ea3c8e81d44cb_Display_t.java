 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 Myles Chippendale
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
  *     Myles Chippendale <mylesc At paneris.org>
  *     http://paneris.org/~mylesc
  */
 
 package org.melati.admin;
 
 import org.melati.servlet.TemplateServlet;
 import org.melati.template.ServletTemplateContext;
 import org.melati.util.StringUtils;
 import org.melati.Melati;
 
 /**
  * Display an object using the Template specified.
  *
  * Invoked with:
  * http://localhost/zone/org.melati.admin.Display/db/table/troid/Template
  * or
  * http://localhost/zone/org.melati.admin.Display/db/table/troid/?template=t
  * otherwise the default template is used.
  */
 public class Display extends TemplateServlet {

  private static final long serialVersionUID = -6265097127167864313L;
 
   protected String doTemplateRequest(Melati melati, ServletTemplateContext context)
       throws Exception {
    context.put("admin", new AdminUtils(melati));

     java.util.Date now = new java.util.Date();
     context.put("now", now);
     context.put("includedir", "");
 
     if (melati.getObject() != null) {
       melati.getObject().assertCanRead();
       context.put("object", melati.getObject());
     }
 
     if (context.getForm("template") != null) { 
       return StringUtils.tr(context.getForm("template"),".","/");
     }
     if (melati.getMethod() != null) {
       return StringUtils.tr(melati.getMethod(),".","/");
     }
     return StringUtils.tr(this.getClass().getName(),".","/");
   }
 }
