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
 import org.tcdi.opensource.wiki.search.*;
 
 /**
  * Using the WikiPageFinder of the provided WikiSystem, attempts to
  * find pages that meet the user's search criteria
  *
  * @author  e_ridge
  */
 public class FindPagesAction implements PageAction {
 
     /**
      * can only find pages if <i>?find</i> is in the request
      */
     public boolean accept(WikiSystem wiki, WebContext wc, WikiUser user) {
         return wc.getForm ("find") != null;
     }
     
     /**
      * Use <code>wiki</code>'s WikiPageFinder to find pages that match
      * the query found in the <i>find</i> request parameter.  Stuffs the
     * WebContext with an array of <code>WikiPageFInder.FindResult</code>
      * objects as $Results
      */
     public void perform(WikiSystem wiki, WebContext wc, WikiUser user, WikiPage page) throws PageActionException {
         String query = wc.getForm ("find");
         if (query.trim().length() > 0) {
             WikiPageFinder finder = new LuceneFinder ();
             try {
                 WikiPageFinder.FindResult[] results = finder.findPages (wiki, query);
                 wc.put ("Results", results);
             } catch (Exception e) {
                 // log the exception, but don't pass it back to the user
                 wc.getLog ("FindPagesAction").error ("Unable to find pages with query=" + query, e);
             }
         }
     }
     
     /**
      * Use the template specified by <i>FindPageAction.Template</i>
      * in the WikiSystem's properties
      */
     public String getTemplateName(WikiSystem wiki, WikiPage page) {
         return wiki.getProperties().getProperty ("FindPagesAction.Template");
     }
     
     /**
      * no page name for this action
      */
     public String getWikiPageName(WikiSystem wiki, WebContext wc) {
         return null;
     }   
 }
