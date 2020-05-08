 /*
 Copyright (c) 2005-2006, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 package com.justjournal;
 
 import com.justjournal.db.EntryDAO;
 import com.justjournal.rss.Rss;
 import org.apache.log4j.Category;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.util.Calendar;
 
 /**
  * User: laffer1
  * Date: Feb 26, 2006
  * Time: 10:44:18 AM
  *
 * @version $Id: RecentBlogs.java,v 1.7 2006/08/02 14:40:51 laffer1 Exp $
  */
 public class RecentBlogs extends JustJournalBaseServlet {
     private static Category log = Category.getInstance(RecentBlogs.class.getName());
 
     protected void execute(HttpServletRequest request, HttpServletResponse response, HttpSession session, StringBuffer sb) {
         response.setContentType("application/rss+xml");
         // Create an RSS object, set the required
         // properites (title, description language, url)
         // and write it to the sb output.
         try {
             Rss rss = new Rss();
 
             final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar(java.util.TimeZone.getDefault());
             calendarg.setTime(new java.util.Date());
 
             rss.setTitle("JJ New Posts");
             rss.setLink("http://www.justjournal.com/");
             rss.setDescription("New blog posts on Just Journal");
             rss.setLanguage("en-us");
             rss.setCopyright("Copyright " + calendarg.get(Calendar.YEAR) + " JustJournal.com and its blog account owners.");
             rss.setWebMaster("webmaster@justjournal.com");
             rss.setManagingEditor("webmaster@justjournal.com");
            rss.populate(EntryDAO.viewRecentAllUsers());
             sb.append(rss.toXml());
 
         } catch (Exception e) {
             // oops we goofed somewhere.  Its not in the original spec
             // how to handle error conditions with rss.
             // html back isn't good, but what do we do?
             log.debug(e);
             WebError.Display("RSS ERROR", "Unable to retrieve RSS content.", sb);
         }
 
     }
 }
