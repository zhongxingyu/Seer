 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Wiki.
  *
  * The Initial Developer of the Original Code is Technology Concepts
  * and Design, Inc.
  * Copyright (C) 2000 Technology Concepts and Design, Inc.  All
  * Rights Reserved.
  *
  * Contributor(s): Lane Sharman (OpenDoors Software)
  *                 Justin Wells (Semiotek Inc.)
  *                 Eric B. Ridge (Technology Concepts and Design, Inc.)
  *
  * Alternatively, the contents of this file may be used under the
  * terms of the GNU General Public License Version 2 or later (the
  * "GPL"), in which case the provisions of the GPL are applicable
  * instead of those above.  If you wish to allow use of your
  * version of this file only under the terms of the GPL and not to
  * allow others to use your version of this file under the MPL,
  * indicate your decision by deleting the provisions above and
  * replace them with the notice and other provisions required by
  * the GPL.  If you do not delete the provisions above, a recipient
  * may use your version of this file under either the MPL or the
  * GPL.
  *
  *
  * This product includes sofware developed by OpenDoors Software.
  *
  * This product includes software developed by Justin Wells and Semiotek Inc.
  * for use in the WebMacro ServletFramework (http://www.webmacro.org).
  */
 
 package org.tcdi.opensource.wiki.servlet;
 
 import java.util.*;
 import javax.servlet.http.Cookie;
 import org.webmacro.servlet.WebContext;
 
 import org.tcdi.opensource.wiki.*;
 
 /**
  * Builds the list of recently changed WikiPages
  *
  * @author  e_ridge
  */
 public class RecentChangesAction implements PageAction {
 
     /**
      * only accept if the URI ends with <em>RecentChanges</em>
      */
     public boolean accept(WikiSystem wiki, WebContext wc, WikiUser user) {
         return wc.getRequest().getRequestURI().endsWith ("RecentChanges");
     }
     
     /**
      * If it's a GET request, we simply display the login template.  If it's
      * a POST request, do process the login request, set the cookie, and
      * redirect to the start page.
      */
     public void perform(WikiSystem wiki, WebContext wc, WikiUser user, WikiPage page) throws PageAction.PageActionException {
         List pages = new ArrayList ();
         String[] pageNames = wiki.getCurrentPageNames();
         double days = 7D;
 
         // figure out how many days to show
         if (wc.getForm ("days") != null) {
             try {
                 days = Double.parseDouble (wc.getForm ("days"));
             } catch (Exception e) {
                 // not a number, so stick with default
             } 
         } else if (user != null) {
             // nothing in request, so use user preference
             try {
                 days = Double.parseDouble (user.getAttribute ("RecentChangesDays"));
             } catch (Exception e) {
                 // not a number, so stick with default
             }
         }
         
         // and set it as the user preference
         if (user != null) {
             user.setAttribute ("RecentChangesDays", ""+days);
             wiki.updateUser(user);
         }
         
         // build a list of pages that have been modified in the past
         // <em>days</em> days
         long cutoff = System.currentTimeMillis() - (long) (60*60*24*1000*days);
         
         for (int x=0; x<pageNames.length; x++) {
             WikiPage p = wiki.getPage (pageNames[x]);
             if (p.getDateLastModifiedAsLong() >= cutoff)
                 pages.add (p);
         }          
         
         // sort it descending by date last modified
         Collections.sort (pages, new Comparator () {
             public int compare (Object o1, Object o2) {
                 WikiPage p1 = (WikiPage) o1;
                 WikiPage p2 = (WikiPage) o2;
                 return p2.getDateLastModified().compareTo (p1.getDateLastModified());
             }
         });
         
         // and add list to the context
         wc.put ("RecentChanges", pages);
         wc.put ("Days", days);
     }
     
     /**
      * template is the "RecentChangesAction.Template" configuration option
      */
     public String getTemplateName(WikiSystem wiki, WikiPage page) {
         return wiki.getProperties().getProperty ("RecentChangesAction.Template");
     }
     
     /**
      * no page name for this action
      */
     public String getWikiPageName(WikiSystem wiki, WebContext wc) {
         return null;
     }
  }
