 /*
 Copyright (c) 2005, Lucas Holt
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
 
 /*
  * users.java
  *
  * Created on March 23, 2003, 12:42 PM
  */
 
 package com.justjournal;
 
 import com.justjournal.db.*;
 import org.apache.log4j.Category;
 import sun.jdbc.rowset.CachedRowSet;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.regex.Pattern;
 
 /**
  * Journal viewer for JustJournal.
  * <p/>
  * 1.3 (jan 04) added more RSS Support, owner security
  *
  * @author Lucas Holt
  * @version 1.3
  */
 public final class users extends HttpServlet {
     // constants
     private static final int RT_ENTRIES = 0;
     private static final int RT_FRIENDS = 1;
     private static final int RT_CALENDAR = 2;
     private static final int RT_RSS = 3;
     private static final int RT_LJ = 4;
     private static final int RT_CALENDAR_NUMERIC = 5;
     private static final int RT_CALENDAR_MONTH = 6;
     private static final int RT_CALENDAR_DAY = 7;
     private static final int RT_RSS_SUBSCRIPTIONS = 8;
     private static final int RT_SINGLE_ENTRY = 9;
     private static final char endl = '\n';
 
     private static Category log = Category.getInstance(users.class.getName());
 
     /**
      * Initializes the servlet.
      *
      * @param config
      * @throws ServletException
      */
     public void init(final ServletConfig config) throws ServletException {
         super.init(config);
     }
 
     /**
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      *
      * @param request  servlet request
      * @param response servlet response
      * @throws ServletException
      * @throws java.io.IOException
      */
     private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
             throws ServletException, java.io.IOException {
 
         final StringBuffer sb = new StringBuffer();
 
         // start session if one does not exist.
         final HttpSession session = request.getSession(true);
 
         // Does a username exist in session?  i.e. are we logged in?
         User aUser = new User(); // authenticated user
         aUser.setUserName((String) session.getAttribute("auth.user"));
         aUser.setUserId((Integer) session.getAttribute("auth.uid"));
 
         /*
             Get username for Journal we are trying to view/access.
         */
         String userName = "";
 
         // Create a pattern to match slashes
         final Pattern p = Pattern.compile("[/]+");
 
         // Split input with the pattern
         final String[] arrUri = p.split(request.getRequestURI());
         final int arrUriLength = java.lang.reflect.Array.getLength(arrUri);
 
         if (arrUriLength > 2) {
             userName = arrUri[2].toLowerCase();
         }
 
         /* Initialize Preferences Object */
         final Preferences pf;
         try {
             pf = new Preferences(userName);
         } catch (Exception ex) {
             throw new ServletException(ex);
         }
 
 
         // if the length is greater than three then the uri is like /users/laffer1/friends
 
         int RequestType = RT_ENTRIES;  //default
 
         int year = 0;
         int month = 0;
         int day = 0;
         int singleEntryId = 0;
 
         if (arrUriLength > 3) {
             if (arrUri[3].compareTo("friends") == 0) {
                 RequestType = RT_FRIENDS;
             } else if (arrUri[3].compareTo("calendar") == 0) {
                 final java.util.Calendar cal = new GregorianCalendar();
                 year = cal.get(java.util.Calendar.YEAR);
                 RequestType = RT_CALENDAR;
             } else if (arrUri[3].compareTo("rss") == 0) {
                 RequestType = RT_RSS;
             } else if (arrUri[3].compareTo("subscriptions") == 0) {
                 RequestType = RT_RSS_SUBSCRIPTIONS;
             } else if (arrUri[3].compareTo("ljfriends") == 0) {
                 RequestType = RT_LJ;
             } else if (arrUri[3].matches("\\d\\d\\d\\d")) {
 
                 year = Integer.parseInt(arrUri[3]);
 
                 if (arrUriLength > 4 && arrUri[4].matches("\\d\\d")) {
                     month = Integer.parseInt(arrUri[4]);
 
                     if (arrUriLength > 5 && arrUri[5].matches("\\d\\d")) {
                         day = Integer.parseInt(arrUri[5]);
                         RequestType = RT_CALENDAR_DAY;
                     } else {
                         RequestType = RT_CALENDAR_MONTH;
                     }
                 } else {
                     RequestType = RT_CALENDAR_NUMERIC;
                 }
             } else if (arrUri[3].compareTo("entry") == 0 && arrUriLength > 4) {
                 RequestType = RT_SINGLE_ENTRY;
                 try {
                     singleEntryId = Integer.parseInt(arrUri[4]);
                 } catch (NumberFormatException e) {
                     singleEntryId = -1; // invalid entry id.  flag the problem.
                 }
 
             }
         }
 
 
         if (RequestType == RT_RSS) {
             response.setContentType("text/xml");
 
             if (pf.isPrivateJournal() == false || aUser.getUserName().equals(userName))
                 getRSS(userName, pf, sb);
             else
                 sb.append("Security restriction");
         } else {
 
             /*
                 Example of Expiring Headers for browsers!
 
                 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");    // Date in the past
                 header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT"); // always modified
                 header("Cache-Control: no-store, no-cache, must-revalidate");  // HTTP/1.1
                 header("Cache-Control: post-check=0, pre-check=0", false);
                 header("Pragma: no-cache"); // HTTP/1.0
             */
 
             response.setContentType("text/html");
             response.setDateHeader("Expires", System.currentTimeMillis());
             response.setDateHeader("Last-Modified", System.currentTimeMillis());
             response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
             response.setHeader("Pragma", "no-cache");
 
             // Begin HTML document.
             sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
             sb.append(endl);
 
             sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
             sb.append(endl);
 
             sb.append("<head>");
             sb.append(endl);
             if (pf.isSpiderAllowed() == false) {
                 sb.append("\t<meta name=\"robots\" content=\"noindex, nofollow, noarchive\" />");
                 sb.append(endl);
                 sb.append("\t<meta name=\"googlebot\" content=\"nosnippet\" />");
                 sb.append(endl);
             }
             sb.append("\t<title>" + pf.getName() + "'s Journal</title>");
             sb.append(endl);
 
             /* User's custom style URL.. i.e. uri to css doc outside domain */
             if (pf.getStyleUrl() != null && !pf.getStyleUrl().equals("")) {
                 sb.append("\t<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + pf.getStyleUrl() + "\" />");
                 sb.append(endl);
             } else {
                 /* use our template system instead */
                 sb.append("\t<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"/styles/" + pf.getStyleId() + ".css\" />");
                 sb.append(endl);
             }
 
             /* Optional style sheet overrides! */
             if (pf.getStyleDoc() != "" && pf.getStyleDoc() != null) {
                 sb.append("<style type=\"text/css\" media=\"screen\">");
                 sb.append(endl);
                 sb.append("<!--");
                 sb.append(endl);
                 sb.append(pf.getStyleDoc());
                 sb.append("-->");
                 sb.append(endl);
                 sb.append("</style>");
                 sb.append(endl);
             }
             /* End overrides */
             sb.append("</head>\n");
 
             sb.append("<body>\n");
 
             // if the user has owner only the first check will fail
             // but if they are logged in then they can see it anyway!
             if (pf.isPrivateJournal() == false || aUser.getUserName().equals(userName)) {
 
                 // BEGIN MENU
                 sb.append("\t<!-- Header: Begin -->");
                 sb.append(endl);
                 sb.append("\t\t<div id=\"header\">");
                 sb.append(endl);
                 sb.append("\t\t<h1>");
                 sb.append(pf.getName());
                 sb.append("'s Journal</h1>");
                 sb.append(endl);
                 sb.append("\t</div>");
                 sb.append(endl);
                 sb.append("\t<!-- Header: End -->\n");
                 sb.append(endl);
 
                 sb.append("\t<!-- Menu: Begin -->");
                 sb.append(endl);
                 sb.append("\t<div id=\"menu\">");
                 sb.append(endl);
 
                 sb.append("\t<p id=\"muser\">");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("\">recent entries</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/calendar\">Calendar</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/friends\">Friends</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/ljfriends\">LJ Friends</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/profile.jsp?user=");
                 sb.append(userName);
                 sb.append("\">Profile</a><br />");
                 sb.append(endl);
                 sb.append("\t</p>");
                 sb.append(endl);
 
                 // General stuff...
                 sb.append("\t<p id=\"mgen\">");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/update.jsp\">Update Journal</a><br />");
                 sb.append(endl);
 
                 // Authentication menu choice
                 if (aUser.getUserId() > 0) {
                     // User is logged in.. give them the option to log out.
                     sb.append("\t\t<a href=\"/prefs/index.jsp\">Preferences</a><br />");
                     sb.append(endl);
                     sb.append("\t\t<a href=\"/logout.jsp\">Log Out</a>");
                     sb.append(endl);
                 } else {
                     // User is logged out.. give then the option to login.
                     sb.append("\t\t<a href=\"/login.jsp\">Login</a>");
                     sb.append(endl);
                 }
                 sb.append("\t</p>");
                 sb.append(endl);
 
                 sb.append("\t<p>RSS Syndication<br /><br />");
                 sb.append("<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/rss\"><img src=\"/img/v4_xml.gif\" alt=\"RSS content feed\" /> Recent</a><br />");
                 sb.append("<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/subscriptions\">Subscriptions</a>");
                 sb.append("\t</p>");
                 sb.append(endl);
 
                 sb.append("\t</div>");
                 sb.append(endl);
                 sb.append("\t<!-- Menu: End -->\n");
                 sb.append(endl);
 
                 // END MENU
 
                 sb.append("\t<!-- Content: Begin -->");
                 sb.append(endl);
                 sb.append("\t<div id=\"content\">");
                 sb.append(endl);
 
                 if (aUser.getUserId() > 0) {
                     sb.append("\t<p>You are logged in as <a href=\"/users/");
                     sb.append(aUser.getUserName());
                     sb.append("\"><img src=\"/images/user.gif\" alt=\"user\" />");
                     sb.append(aUser.getUserName());
                     sb.append("</a>.</p>");
                     sb.append(endl);
                 }
 
                 try {
                     switch (RequestType) {
                         case RT_ENTRIES:
                             // number of entries to skip!  (currently only userful for recent entries)
                             int skip;
 
                             try {
                                 skip = Integer.valueOf(request.getParameter("skip")).intValue();
                             } catch (NumberFormatException exInt) {
                                 skip = 0;
                             }
 
                             getEntries(userName, aUser, skip, sb);
 
                             break;
                         case RT_FRIENDS:
                             getFriends(pf.getId(), sb, aUser);
                             break;
                         case RT_CALENDAR:
                             getCalendar(year, userName, aUser, sb, pf);
                             break;
                         case RT_LJ:
                             getLjFriends(pf.getId(), sb);
                             break;
                         case RT_RSS_SUBSCRIPTIONS:
                             getSubscriptions(pf.getId(), sb);
                             break;
                         case RT_CALENDAR_NUMERIC:
                             getCalendar(year, userName, aUser, sb, pf);
                             break;
                         case RT_CALENDAR_MONTH:
                             getCalendarMonth(year, month, userName, aUser, sb);
                             break;
                         case RT_CALENDAR_DAY:
                             getCalendarDay(year, month, day, userName, aUser, sb);
                             break;
                         case RT_SINGLE_ENTRY:
                             getSingleEntry(singleEntryId, userName, aUser, sb);
                             break;
                     }
                 } catch (Exception ex) {
                     webError.Display(" Error",
                             "Error accessing data.",
                             sb);
                 }
 
                 sb.append("\t</div>");
                 sb.append(endl);
                 sb.append("\t<!-- Content: End -->");
                 sb.append(endl);
                 sb.append(endl);
 
                 sb.append("\t<!-- Footer: Begin -->");
                 sb.append(endl);
                 sb.append("\t<div id=\"footer\">");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/index.jsp\" title=\"JustJournal.com: Online Journals\">JustJournal.com</a> ");
                 sb.append("\t</div>");
                 sb.append(endl);
 
                 sb.append("\t<!-- Footer: End -->\n");
                 sb.append(endl);
             }
 
             sb.append("</body>");
             sb.append(endl);
             sb.append("</html>");
             sb.append(endl);
         }
 
         // output the result of our processing
         final ServletOutputStream outstream = response.getOutputStream();
         outstream.println(sb.toString());
         outstream.flush();
 
     }
 
     private static void getLjFriends(final int id, final StringBuffer sb) {
         sb.append("<h2>LJ Friends</h2>");
         sb.append(endl);
         sb.append("<p>This page might be slow because we must wait for the lj server!.</p>");
         sb.append(endl);
 
         final CachedHeadlineBean hb = new CachedHeadlineBean();
         LJFriendDao ljao = new LJFriendDao();
 
         try {
             Collection friends = ljao.view(id);
 
             /* Iterator */
             LJFriendTo o;
             final Iterator itr = friends.iterator();
             for (int i = 0, n = friends.size(); i < n; i++) {
                 o = (LJFriendTo) itr.next();
 
                 if (o.getIsCommunity()) {
                     sb.append(hb.parse("http://www.livejournal.com/community/" +
                             o.getUserName() + "/data/rss"));
                 } else {
                     sb.append(hb.parse("http://www.livejournal.com/users/" +
                             o.getUserName() + "/data/rss"));
                 }
                 sb.append(endl);
             }
 
         } catch (Exception e) {
             webError.Display("LjFriend Error", "Can not retrieve RSS content.", sb);
         }
     }
 
     private static void getSubscriptions(final int id, final StringBuffer sb) {
         sb.append("<h2>RSS Subscriptions</h2>");
         sb.append(endl);
         sb.append("<p>This page might be slow because we must wait for RSS to download from different servers!.</p>");
         sb.append(endl);
 
         final CachedHeadlineBean hb = new CachedHeadlineBean();
         RssSubscriptionsDAO rdao = new RssSubscriptionsDAO();
 
         try {
             Collection rssfeeds = rdao.view(id);
 
             /* Iterator */
             RssSubscriptionsTO o;
             final Iterator itr = rssfeeds.iterator();
             for (int i = 0, n = rssfeeds.size(); i < n; i++) {
                 o = (RssSubscriptionsTO) itr.next();
 
                 sb.append(hb.parse(o.getUri()));
                 sb.append(endl);
             }
 
         } catch (Exception e) {
             webError.Display("RSS Subscriptions Error", "Can not retrieve RSS content.", sb);
         }
     }
 
     private static void getSingleEntry(final int singleEntryId, final String userName, final User aUser, final StringBuffer sb) {
         if (log.isDebugEnabled())
             log.debug("getSingleEntry: Loading DAO");
 
         final EntryDAO edao = new EntryDAO();
         EntryTo o;
 
         if (singleEntryId < 1) {
             webError.Display("Invalid Entry Id", "The entry id was invalid for the journal entry you tried to view.", sb);
         } else {
             try {
                 if (aUser.getUserName().compareTo(userName) == 0) {
                     o = edao.viewSingle(singleEntryId, true);  // should be true
 
 
                     if (log.isDebugEnabled())
                         log.debug("getSingleEntry: User is logged in.");
                 } else {
                     o = edao.viewSingle(singleEntryId, false);
 
                     if (log.isDebugEnabled())
                         log.debug("getSingleEntry: User is not logged in.");
                 }
 
 
                 // Format the current time.
                 final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
                 final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
                 String curDate;
 
                 if (log.isDebugEnabled())
                     log.debug("getSingleEntry: Begin reading record.");
 
 
                 if (o != null && o.getId() > 0) {
 
                     // Parse the previous string back into a Date.
                     final ParsePosition pos = new ParsePosition(0);
                     final java.util.Date currentDate = formatter.parse(o.getDate().toString(), pos);
 
                     curDate = formatmydate.format(currentDate);
 
                     sb.append("<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
 
                     sb.append("<div class=\"ebody\">");
                     sb.append(endl);
 
                     sb.append("<h3>");
                     sb.append("<span class=\"time\">");
                     sb.append(formatmytime.format(currentDate));
                     sb.append("</span> - <span class=\"subject\">");
                     sb.append(o.getSubject());
                     sb.append("</span></h3> ");
                     sb.append(endl);
 
                     sb.append("<div class=\"ebody\">");
                     sb.append(endl);
 
                     // autoformat controls whether new lines should be
                     // converted to br's.  If someone used html
                     // we don't want autoformat!
                     // We handle Windows/UNIX with the \n case and
                     // Mac OS Classic with \r
                     // TODO: A more complex system should be developed
                     if (o.getAutoFormat()) {
                         sb.append("<p>");
                         if (o.getBody().indexOf("\n") > -1)
                             sb.append(StringUtil.replace(o.getBody(), '\n', "<br />"));
                         else if (o.getBody().indexOf("\r") > -1)
                             sb.append(StringUtil.replace(o.getBody(), '\r', "<br />"));
                         else
                         // we do not have any "new lines" but it might be
                         // one long line.
                             sb.append(o.getBody());
 
                         sb.append("</p>");
                     } else {
                         sb.append(o.getBody());
                     }
 
                     sb.append(endl);
                     sb.append("</div>");
                     sb.append(endl);
 
                     sb.append("<p>");
 
                     if (o.getSecurityLevel() == 0) {
                         sb.append("<span class=\"security\">security: ");
                         sb.append("<img src=\"/img/icon_private.gif\" alt=\"private\" /> ");
                         sb.append("private");
                         sb.append("</span><br />");
                         sb.append(endl);
                     } else if (o.getSecurityLevel() == 1) {
                         sb.append("<span class=\"security\">security: ");
                         sb.append("<img src=\"/img/icon_protected.gif\" alt=\"friends\" /> ");
                         sb.append("friends");
                         sb.append("</span><br />");
                         sb.append(endl);
                     }
 
                     if (o.getLocationId() > 0) {
                         sb.append("<span class=\"location\">location: ");
                         sb.append(o.getLocationName());
                         sb.append("</span><br />");
                         sb.append(endl);
                     }
 
                     if (o.getMoodName().length() > 0 && o.getMoodId() != 12) {
                         final EmoticonDao emot = new EmoticonDao();
                         final EmoticonTo emoto = emot.view(1, o.getMoodId());
 
                         sb.append("<span class=\"mood\">mood: <img src=\"/images/emoticons/1/");
                         sb.append(emoto.getFileName());
                         sb.append("\" width=\"");
                         sb.append(emoto.getWidth());
                         sb.append("\" height=\"");
                         sb.append(emoto.getHeight());
                         sb.append("\" alt=\"");
                         sb.append(o.getMoodName());
                         sb.append("\" /> ");
                         sb.append(o.getMoodName());
                         sb.append("</span><br />");
                         sb.append(endl);
                     }
 
                     if (o.getMusic().length() > 0) {
                         sb.append("<span class=\"music\">music: ");
                         sb.append(o.getMusic());
                         sb.append("</span><br />");
                         sb.append(endl);
                     }
 
                     sb.append("</p>");
                     sb.append(endl);
 
                     sb.append("<div>");
                     sb.append(endl);
                     sb.append("<table width=\"100%\"  border=\"0\">");
                     sb.append(endl);
                     sb.append("<tr>");
                     sb.append(endl);
 
                     if (aUser.getUserName().compareTo(userName) == 0) {
                         sb.append("<td width=\"30\"><a title=\"Edit Entry\" href=\"/entry/edit.h?entryId=");
                         sb.append(o.getId());
                         sb.append("\"><img src=\"/images/compose-message.png\" width=\"24\" height=\"24\" alt=\"Edit\" /></a></td>");
                         sb.append(endl);
                         sb.append("<td width=\"30\"><a title=\"Delete Entry\" href=\"/entry/delete.h?entryId=");
                         sb.append(o.getId());
                         sb.append("\"><img src=\"/images/stock_calc-cancel.png\" width=\"24\" height=\"24\" alt=\"Delete\" /></a>");
                         sb.append("</td>");
                         sb.append(endl);
                     }
 
                     sb.append("<td width=\"30\"><a title=\"Add Favorite\" href=\"/favorite/entry.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><img src=\"/images/favourites-24.png\" width=\"24\" height=\"24\" alt=\"Favorites\" /></a></td>");
                     sb.append(endl);
 
                     sb.append("<td><div align=\"right\">(");
 
                     switch (o.getCommentCount()) {
                         case 0:
                             break;
                         case 1:
                             sb.append("<a href=\"/comment/view.h?entryId=");
                             sb.append(o.getId());
                             sb.append("\">1 comment</a> | ");
                             break;
                         default:
                             sb.append("<a href=\"/comment/view.h?entryId=");
                             sb.append(o.getId());
                             sb.append("\">");
                             sb.append(o.getCommentCount());
                             sb.append(" comments</a> | ");
                     }
 
                     sb.append("<a href=\"/comment/add.jsp?id=");
                     sb.append(o.getId());
                     sb.append("\">comment on this</a>)");
 
 
                     sb.append("</div></td>");
                     sb.append(endl);
                     sb.append("</tr>");
                     sb.append(endl);
                     sb.append("</table>");
                     sb.append(endl);
                     sb.append("</div>");
                     sb.append(endl);
 
                     sb.append("</div>");
                     sb.append(endl);
                 }
 
             } catch (Exception e1) {
                 webError.Display("Error",
                         "Unable to retrieve journal entry from data store.",
                         sb);
 
                 if (log.isDebugEnabled())
                     log.debug("getSingleEntry: Exception is " + e1.getMessage() + "\n" + e1.toString());
             }
         }
     }
 
     private static void getEntries(final String userName, final User aUser, final int skip, final StringBuffer sb) {
         if (log.isDebugEnabled())
             log.debug("getEntries: Loading DAO");
 
         final EntryDAO edao = new EntryDAO();
         final Collection entries;
 
         try {
             if (aUser.getUserName().compareTo(userName) == 0) {
                 entries = edao.view(userName, true, skip);  // should be true
 
                 if (log.isDebugEnabled())
                     log.debug("getEntries: User is logged in.");
             } else {
                 entries = edao.view(userName, false, skip);
 
                 if (log.isDebugEnabled())
                     log.debug("getEntries: User is not logged in.");
             }
 
 
             // Format the current time.
             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
             final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
             final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
             String lastDate = "";
             String curDate;
 
             // jump menu
             sb.append("<p>Jump <a href=\"/users/");
             sb.append(userName);
             sb.append("?skip=");
             sb.append((skip + 20));
             sb.append("\">back 20 entries</a> ");
 
             if (skip > 0) {
                 sb.append(" or <a href=\"/users/");
                 sb.append(userName);
                 sb.append("?skip=");
                 sb.append((skip - 20));
                 sb.append("\">forward 20 entries</a>");
             }
 
             sb.append("</p>");
             sb.append(endl);
             // end jump menu
 
             if (log.isDebugEnabled())
                 log.debug("getEntries: Begin Iteration of records.");
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             for (int i = 0, n = entries.size(); i < n; i++) {
                 o = (EntryTo) itr.next();
 
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final java.util.Date currentDate = formatter.parse(o.getDate().toString(), pos);
 
                 curDate = formatmydate.format(currentDate);
 
                 if (curDate.compareTo(lastDate) != 0) {
                     sb.append("<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
                     lastDate = curDate;
                 }
 
                 sb.append("<div class=\"ebody\">");
                 sb.append(endl);
 
                 sb.append("<h3>");
                 sb.append("<span class=\"time\">");
                 sb.append(formatmytime.format(currentDate));
                 sb.append("</span> - <span class=\"subject\">");
                 sb.append(o.getSubject());
                 sb.append("</span></h3> ");
                 sb.append(endl);
 
                 sb.append("<div class=\"ebody\">");
                 sb.append(endl);
 
                 // autoformat controls whether new lines should be
                 // converted to br's.  If someone used html
                 // we don't want autoformat!
                 // We handle Windows/UNIX with the \n case and
                 // Mac OS Classic with \r
                 // TODO: A more complex system should be developed
                 if (o.getAutoFormat()) {
                     sb.append("<p>");
                     if (o.getBody().indexOf("\n") > -1)
                         sb.append(StringUtil.replace(o.getBody(), '\n', "<br />"));
                     else if (o.getBody().indexOf("\r") > -1)
                         sb.append(StringUtil.replace(o.getBody(), '\r', "<br />"));
                     else
                     // we do not have any "new lines" but it might be
                     // one long line.
                         sb.append(o.getBody());
 
                     sb.append("</p>");
                 } else {
                     sb.append(o.getBody());
                 }
 
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 sb.append("<p>");
 
                 if (o.getSecurityLevel() == 0) {
                     sb.append("<span class=\"security\">security: ");
                     sb.append("<img src=\"/img/icon_private.gif\" alt=\"private\" /> ");
                     sb.append("private");
                     sb.append("</span><br />");
                     sb.append(endl);
                 } else if (o.getSecurityLevel() == 1) {
                     sb.append("<span class=\"security\">security: ");
                     sb.append("<img src=\"/img/icon_protected.gif\" alt=\"friends\" /> ");
                     sb.append("friends");
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getLocationId() > 0) {
                     sb.append("<span class=\"location\">location: ");
                     sb.append(o.getLocationName());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getMoodName().length() > 0 && o.getMoodId() != 12) {
                     final EmoticonDao emot = new EmoticonDao();
                     final EmoticonTo emoto = emot.view(1, o.getMoodId());
 
                     sb.append("<span class=\"mood\">mood: <img src=\"/images/emoticons/1/");
                     sb.append(emoto.getFileName());
                     sb.append("\" width=\"");
                     sb.append(emoto.getWidth());
                     sb.append("\" height=\"");
                     sb.append(emoto.getHeight());
                     sb.append("\" alt=\"");
                     sb.append(o.getMoodName());
                     sb.append("\" /> ");
                     sb.append(o.getMoodName());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getMusic().length() > 0) {
                     sb.append("<span class=\"music\">music: ");
                     sb.append(o.getMusic());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 sb.append("</p>");
                 sb.append(endl);
 
                 sb.append("<div>");
                 sb.append(endl);
                 sb.append("<table width=\"100%\"  border=\"0\">");
                 sb.append(endl);
                 sb.append("<tr>");
                 sb.append(endl);
 
                 if (aUser.getUserName().compareTo(userName) == 0) {
                     sb.append("<td width=\"30\"><a title=\"Edit Entry\" href=\"/entry/edit.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><img src=\"/images/compose-message.png\" width=\"24\" height=\"24\" alt=\"Edit\" /></a></td>");
                     sb.append(endl);
                     sb.append("<td width=\"30\"><a title=\"Delete Entry\" href=\"/entry/delete.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><img src=\"/images/stock_calc-cancel.png\" width=\"24\" height=\"24\" alt=\"Delete\" /></a>");
                     sb.append("</td>");
                     sb.append(endl);
                 }
 
                 sb.append("<td width=\"30\"><a title=\"Add Favorite\" href=\"/favorite/entry.h?entryId=");
                 sb.append(o.getId());
                 sb.append("\"><img src=\"/images/favourites-24.png\" width=\"24\" height=\"24\" alt=\"Favorites\" /></a></td>");
                 sb.append(endl);
 
                sb.append("<td><div align=\"right\">(");
 
                 switch (o.getCommentCount()) {
                     case 0:
                         break;
                     case 1:
                         sb.append("<a href=\"/comment/view.h?entryId=");
                         sb.append(o.getId());
                         sb.append("\">1 comment</a> | ");
                         break;
                     default:
                         sb.append("<a href=\"/comment/view.h?entryId=");
                         sb.append(o.getId());
                         sb.append("\">");
                         sb.append(o.getCommentCount());
                         sb.append(" comments</a> | ");
                 }
 
                 sb.append("<a href=\"/comment/add.jsp?id=");
                 sb.append(o.getId());
                 sb.append("\">comment on this</a>)");
 
 
                 sb.append("</div></td>");
                 sb.append(endl);
                 sb.append("</tr>");
                 sb.append(endl);
                 sb.append("</table>");
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 sb.append("</div>");
                 sb.append(endl);
 
             }
 
         } catch (Exception e1) {
             webError.Display("Error",
                     "Unable to retrieve journal entries from data store.",
                     sb);
             /*webError.Display( "Error", e1.toString(),sb);
               */
 
             if (log.isDebugEnabled())
                 log.debug("getEntries: Exception is " + e1.getMessage() + "\n" + e1.toString());
 
             //e1.printStackTrace();
         }
 
     }
 
     /**
      * Displays friends entries for a particular user.
      *
      * @param userId the user to show the entries of
      * @param sb     output variable
      * @param aUser  current authenticated user (for friends security check)
      */
     private static void getFriends(final int userId,
                                    final StringBuffer sb,
                                    final User aUser) {
 
         if (log.isDebugEnabled())
             log.debug("getFriends: Load DAO.");
 
         final EntryDAO edao = new EntryDAO();
         final Collection entries;
 
         entries = edao.viewFriends(userId, aUser.getUserId());
 
         sb.append("<h2>Friends</h2>");
         sb.append(endl);
 
         try {
             if (log.isDebugEnabled())
                 log.debug("getFriends: Init Date Parsers.");
 
             // Format the current time.
             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
             final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
             final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
             String lastDate = "";
             String curDate;
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             if (log.isDebugEnabled())
                 log.debug("getFriends: Number of entries " + entries.size());
 
             if (entries.size() == 0)
                 sb.append("<p>No friends entries found</p>.");
 
             for (int i = 0, n = entries.size(); i < n; i++) {
                 o = (EntryTo) itr.next();
 
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final java.util.Date currentDate = formatter.parse(o.getDate().toString(), pos);
 
                 curDate = formatmydate.format(currentDate);
 
                 if (curDate.compareTo(lastDate) != 0) {
                     sb.append("<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
                     lastDate = curDate;
                 }
 
                 sb.append("<div class=\"ebody\">");
                 sb.append(endl);
 
                 sb.append("<h3>");
                 sb.append("<a href=\"/users/");
                 sb.append(o.getUserName());
                 sb.append("\" title=\"");
                 sb.append(o.getUserName());
                 sb.append("\">");
                 sb.append(o.getUserName());
                 sb.append("</a> ");
 
                 sb.append("<span class=\"time\">");
                 sb.append(formatmytime.format(currentDate));
                 sb.append("</span> - <span class=\"subject\">");
                 sb.append(o.getSubject());
                 sb.append("</span></h3> ");
                 sb.append(endl);
 
                 sb.append("<div class=\"ebody\">");
                 sb.append(endl);
 
                 // Keep this synced with getEntries()
                 if (o.getAutoFormat()) {
                     sb.append("<p>");
                     if (o.getBody().indexOf("\n") > -1)
                         sb.append(StringUtil.replace(o.getBody(), '\n', "<br />"));
                     else if (o.getBody().indexOf("\r") > -1)
                         sb.append(StringUtil.replace(o.getBody(), '\r', "<br />"));
                     else
                     // we do not have any "new lines" but it might be
                     // one long line.
                         sb.append(o.getBody());
 
                     sb.append("</p>");
                 } else {
                     sb.append(o.getBody());
                 }
 
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 sb.append("<p>");
 
                 if (o.getSecurityLevel() == 0) {
                     sb.append("<span class=\"security\">security: ");
                     sb.append("<img src=\"/img/icon_private.gif\" alt=\"private\" /> ");
                     sb.append("private");
                     sb.append("</span><br />");
                     sb.append(endl);
                 } else if (o.getSecurityLevel() == 1) {
                     sb.append("<span class=\"security\">security: ");
                     sb.append("<img src=\"/img/icon_protected.gif\" alt=\"friends\" /> ");
                     sb.append("friends");
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getLocationId() > 0) {
                     sb.append("<span class=\"location\">location: ");
                     sb.append(o.getLocationName());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getMoodName().length() > 0 && o.getMoodId() != 12) {
                     final EmoticonDao emot = new EmoticonDao();
                     final EmoticonTo emoto = emot.view(1, o.getMoodId());
 
                     sb.append("<span class=\"mood\">mood: <img src=\"/images/emoticons/1/");
                     sb.append(emoto.getFileName());
                     sb.append("\" width=\"");
                     sb.append(emoto.getWidth());
                     sb.append("\" height=\"");
                     sb.append(emoto.getHeight());
                     sb.append("\" alt=\"");
                     sb.append(o.getMoodName());
                     sb.append("\" /> ");
                     sb.append(o.getMoodName());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getMusic().length() > 0) {
                     sb.append("<span class=\"music\">music: ");
                     sb.append(o.getMusic());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 sb.append("</p>");
                 sb.append(endl);
 
                 sb.append("<div>");
                 sb.append(endl);
                 sb.append("<table width=\"100%\"  border=\"0\">");
                 sb.append(endl);
                 sb.append("<tr>");
                 sb.append(endl);
 
                 if (aUser.getUserId() == o.getUserId()) {
                     sb.append("<td width=\"30\"><a title=\"Edit Entry\" href=\"/entry/edit.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><img src=\"/images/compose-message.png\" width=\"24\" height=\"24\" alt=\"Edit\" /></a></td>");
                     sb.append(endl);
                     sb.append("<td width=\"30\"><a title=\"Delete Entry\" href=\"/entry/delete.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><img src=\"/images/stock_calc-cancel.png\" width=\"24\" height=\"24\" alt=\"Delete\" /></a>");
                     sb.append("</td>");
                     sb.append(endl);
                 }
 
                 sb.append("<td width=\"30\"><a title=\"Add Favorite\" href=\"/favorite/entry.h?entryId=");
                 sb.append(o.getId());
                 sb.append("\"><img src=\"/images/favourites-24.png\" width=\"24\" height=\"24\" alt=\"Favorites\" /></a></td>");
                 sb.append(endl);
 
                 sb.append("<td><div align=\"right\">(");
 
                 switch (o.getCommentCount()) {
                     case 0:
                         break;
                     case 1:
                         sb.append("<a href=\"/comment/view.h?entryId=");
                         sb.append(o.getId());
                         sb.append("\">1 comment</a> | ");
                         break;
                     default:
                         sb.append("<a href=\"/comment/view.h?entryId=");
                         sb.append(o.getId());
                         sb.append("\">");
                         sb.append(o.getCommentCount());
                         sb.append(" comments</a> | ");
                 }
 
                 sb.append("<a href=\"/comment/add.jsp?id=");
                 sb.append(o.getId());
                 sb.append("\">comment on this</a>)");
 
                 sb.append("</div></td>");
                 sb.append(endl);
                 sb.append("</tr>");
                 sb.append(endl);
                 sb.append("</table>");
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 sb.append("</div>");
                 sb.append(endl);
             }
 
         } catch (Exception e1) {
             webError.Display(" Error",
                     "Error retrieving the friends entries",
                     sb);
         }
 
     }
 
     /**
      * Prints the calendar for the year specified for months
      * with journal entries.  Other months are not printed.
      *
      * @param year     The year to print
      * @param userName Owner of calendar
      * @param aUser    Current logged in user
      * @param sb       output path
      * @see Cal
      * @see CalMonth
      */
     private static void getCalendar(final int year,
                                     final String userName,
                                     final User aUser,
                                     final StringBuffer sb,
                                     Preferences pf) {
         final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
         int yearNow = calendarg.get(Calendar.YEAR);
 
         // print out header
         sb.append("<h2>Calendar: ");
         sb.append(year);
         sb.append("</h2>");
         sb.append(endl);
 
         sb.append("<p>The calendar lists months with journal entries.</p>");
         sb.append(endl);
 
         // BEGIN: YEARS
         sb.append("<p>");
 
         for (int i = yearNow; i >= pf.getStartYear(); i--) {
 
             sb.append("<a href=\"/users/");
             sb.append(userName);
             sb.append("/");
             sb.append(i);
             sb.append("\">");
             sb.append(i);
             sb.append("</a> ");
 
             // just in case!
             if (i == 2002)
                 break;
         }
 
         sb.append("</p>");
         sb.append(endl);
         // END: YEARS
 
         // load recordset and display the calendar
         try {
 
             final CachedRowSet RS;
 
             // are we logged in?
             if (aUser.getUserName().compareTo(userName) == 0) {
                 RS = EntryDAO.ViewCalendarYear(year, userName, true);  // should be true
             } else {
                 RS = EntryDAO.ViewCalendarYear(year, userName, false);
             }
 
             if (RS == null) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
                 // we have calendar data!
                 final Cal mycal = new Cal(RS);
                 sb.append(mycal.render());
             }
 
         } catch (Exception e1) {
             webError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
 
     }
 
     /**
      * Lists all of the journal entries for the month
      * specified in the year specified.
      *
      * @param year     the year to display data for
      * @param month    the month we want
      * @param userName the user to display
      * @param aUser    the authenticated user (for friends security check)
      * @param sb       the output variable
      *                 <p/>
      *                 TODO: change to Entries DAO code
      */
     private static void getCalendarMonth(final int year,
                                          final int month,
                                          final String userName,
                                          final User aUser,
                                          final StringBuffer sb) {
         sb.append("<h2>Calendar: ");
         sb.append(month);
         sb.append("/");
         sb.append(year);
         sb.append("</h2>");
         sb.append(endl);
 
         sb.append("<p>This page lists all of the journal entries for the month.</p>");
         sb.append(endl);
 
         try {
 
             final CachedRowSet RS;
             if (aUser.getUserName().compareTo(userName) == 0) {
                 RS = EntryDAO.ViewCalendarMonth(year, month, userName, true);  // should be true
             } else {
                 RS = EntryDAO.ViewCalendarMonth(year, month, userName, false);
             }
 
             if (RS == null) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
 
                 final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("dd");
                 final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
 
                 String curDate;
                 String lastDate = "";
 
                 while (RS.next()) {
                     // Parse the previous string back into a Date.
                     final ParsePosition pos = new ParsePosition(0);
                     final java.util.Date currentDate = formatter.parse(RS.getString("date"), pos);
 
                     curDate = formatmydate.format(currentDate);
 
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("<p><strong>" + curDate + "</strong></p>");
                         lastDate = curDate;
                     }
 
                     sb.append("<p><span class=\"time\">" + formatmytime.format(currentDate) +
                             "</span> - <span class=\"subject\"><a href=\"");
 
                     //TODO: fix bug where relative url is incorrect
                     // Need to check if we are in a calendar state and
                     // drop the extra parts on the request.
                     // it is appended 08/02/08/02 etc.
                     if (month < 10)
                         sb.append("0");
 
                     sb.append(month + "/" + curDate + "\">" +
                             RS.getString("subject") + "</a></span></p> ");
                     sb.append(endl);
                 }
             }
 
         } catch (Exception e1) {
             webError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
 
         return;
     }
 
     /**
      * Generates all of the HTML to display journal
      * entires for a particular day specified
      * in the url.
      *
      * @param year     the year to display
      * @param month    the month we want to look at
      * @param day      the day we are interested in
      * @param userName the user we want entries for
      * @param aUser    the current authenticated user data
      * @param sb       the output path
      */
     private static void getCalendarDay(final int year,
                                        final int month,
                                        final int day,
                                        final String userName,
                                        final User aUser,
                                        final StringBuffer sb) {
 
         // sb.append("<h2>Calendar: " + day + "/" + month + "/" + year + "</h2>" );
 
         sb.append("<p>Lists all of the journal entries for the day.</p>");
         sb.append(endl);
 
         try {
 
             final CachedRowSet RS;
 
             if (aUser.getUserName().compareTo(userName) == 0) {
                 RS = EntryDAO.ViewCalendarDay(year, month, day, userName, true);  // should be true
             } else {
                 RS = EntryDAO.ViewCalendarDay(year, month, day, userName, false);
             }
 
             if (RS == null) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
 
                 CachedRowSet rsComment = null;
                 String sqlStmt;
 
                 // Format the current time.
                 final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
                 final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
                 String lastDate = "";
                 String curDate;
 
                 while (RS.next()) {
                     // Parse the previous string back into a Date.
                     final ParsePosition pos = new ParsePosition(0);
                     final java.util.Date currentDate = formatter.parse(RS.getString("date"), pos);
 
                     curDate = formatmydate.format(currentDate);
 
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("<h2>");
                         sb.append(curDate);
                         sb.append("</h2>");
                         sb.append(endl);
                         lastDate = curDate;
                     }
 
                     sb.append("<div class=\"ebody\">");
                     sb.append(endl);
 
                     sb.append("<h3><span class=\"time\">");
                     sb.append(formatmytime.format(currentDate));
                     sb.append("</span> - <span class=\"subject\">");
                     sb.append(RS.getString("subject"));
                     sb.append("</span></h3> ");
                     sb.append(endl);
 
                     sb.append("<div class=\"ebody\">");
                     sb.append(endl);
 
                     sb.append("<p>");
 
                     /*  TODO: change to new EntryTo CODE!
                     if ( o.getAutoFormat())
                         sb.append( StringUtil.replace( o.getBody(), '\n', "<br />" ) );
                     else
                         sb.append( o.getBody() );
                     */
 
                     sb.append(StringUtil.replace(RS.getString("body"), '\n', "<br />"));
                     sb.append("</p>");
                     sb.append(endl);
                     sb.append("</div>");
                     sb.append(endl);
 
                     sb.append("<p>");
 
                     if (RS.getInt("security") == 0) {
                         sb.append("<span class=\"security\">security: ");
                         sb.append("<img src=\"/img/icon_private.gif\" alt=\"private\" /> ");
                         sb.append("private");
                         sb.append("</span><br />");
                         sb.append(endl);
                     } else if (RS.getInt("security") == 1) {
                         sb.append("<span class=\"security\">security: ");
                         sb.append("<img src=\"/img/icon_protected.gif\" alt=\"friends\" /> ");
                         sb.append("friends");
                         sb.append("</span><br />");
                         sb.append(endl);
                     }
 
                     if (RS.getInt("locid") > 0) {
                         sb.append("<span class=\"location\">location: " + RS.getString("location") + "</span><br />");
                         sb.append(endl);
                     }
 
                     if (RS.getString("mood").length() > 0 && RS.getInt("moodid") != 12) {
                         final EmoticonDao emot = new EmoticonDao();
                         final EmoticonTo emoto = emot.view(1, RS.getInt("moodid"));
 
                         sb.append("<span class=\"mood\">mood: <img src=\"/images/emoticons/1/"
                                 + emoto.getFileName() + "\" width=\"" + emoto.getWidth() + "\" height=\""
                                 + emoto.getHeight() +
                                 "\" alt=\"" + RS.getString("mood") + "\" /> "
                                 + RS.getString("mood") + "</span><br />");
 
                         sb.append(endl);
                     }
 
                     if (RS.getString("music").length() > 0) {
                         sb.append("<span class=\"music\">music: " + RS.getString("music") + "</span><br />");
                         sb.append(endl);
                     }
 
                     sb.append("</p>");
                     sb.append(endl);
 
 
                     sb.append("<p class=\"commentmenu\">(");
 
                     try {
                         sqlStmt = "SELECT count(comments.id) As comid FROM comments WHERE eid='" + RS.getString("entryid") + "';";
                         rsComment = SQLHelper.executeResultSet(sqlStmt);
 
 
                         if (rsComment.next()) {
                             switch (rsComment.getInt("comid")) {
                                 case 0:
                                     break;
                                 case 1:
                                     sb.append("<a href=\"/comment/view.h?entryId=" + RS.getString("entryid") +
                                             "\">1 comment</a> | ");
                                     break;
                                 default:
                                     sb.append("<a href=\"/comment/view.h?entryId=" + RS.getString("entryid") + "\">" +
                                             rsComment.getInt("comid") + " comments</a> | ");
                             }
                         }
                         rsComment.close();
                         rsComment = null;
                     } catch (Exception eComment) {
                         eComment.getMessage();
                     } finally {
                         if (rsComment != null)
                             rsComment.close();
                     }
                     sb.append("<a href=\"/comment/add.jsp?id=" + RS.getString("entryid") +
                             "\">comment on this</a>)</p>");
                     sb.append(endl);
 
                     sb.append("</div>");
                     sb.append(endl);
                 }
 
                 // close recordset
                 RS.close();
 
             }
 
         } catch (Exception e1) {
             webError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
 
         return;
     }
 
     /**
      * Handles requests for syndication content (RSS).
      * Only returns public journal entries for the specified
      * user.
      *
      * @param userName The user we need content for
      * @param sb       a string buffer that will be returned as
      *                 as the output stream.
      */
     private static void getRSS(final String userName, final Preferences pf,
                                final StringBuffer sb) {
         // Create an RSS object, set the required
         // properites (title, description language, url)
         // and write it to the sb output.
         try {
             Rss rss = new Rss();
             final EntryDAO edao = new EntryDAO();
 
             final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
             calendarg.setTime(new java.util.Date());
 
             rss.setTitle(userName);
             rss.setLink("http://www.justjournal.com/users/" + userName);
             rss.setDescription("Just Journal for " + userName);
             rss.setLanguage("en-us");
             rss.setCopyright("Copyright " + calendarg.get(Calendar.YEAR) + " " + pf.getName());
             rss.setWebMaster("webmaster@justjournal.com");
             rss.setManagingEditor(pf.getEmailAddress());
             rss.populate(edao.view(userName, false), userName);
             sb.append(rss.toXml());
         } catch (Exception e) {
             // oops we goofed somewhere.  Its not in the original spec
             // how to handle error conditions with rss.
             // html back isn't good, but what do we do?
             webError.Display("RSS ERROR", "Unable to retrieve RSS content.", sb);
         }
     }
 
     /**
      * Handles the HTTP <code>GET</code> method.
      *
      * @param request  servlet request
      * @param response servlet response
      * @throws ServletException
      * @throws java.io.IOException
      */
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
             throws ServletException, java.io.IOException {
         processRequest(request, response);
 
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return description string
      */
     public String getServletInfo() {
         return "view journal entries";
     }
 
 }
