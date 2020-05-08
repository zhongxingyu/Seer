 /*-
  * Copyright (c) 2003-2011, 2014 Lucas Holt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 
 package com.justjournal.ctl;
 
 import com.justjournal.*;
 import com.justjournal.atom.AtomFeed;
 import com.justjournal.core.Settings;
 import com.justjournal.db.*;
 import com.justjournal.rss.CachedHeadlineBean;
 import com.justjournal.rss.Rss;
 import com.justjournal.search.BaseSearch;
 import com.justjournal.utility.HTMLUtil;
 import com.justjournal.utility.StringUtil;
 import com.justjournal.utility.Xml;
 import com.lowagie.text.*;
 import com.lowagie.text.Font;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfWriter;
 import com.lowagie.text.rtf.RtfWriter2;
 import com.sun.istack.internal.NotNull;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.awt.*;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 
 /**
  * Journal viewer for JustJournal.
  *
  * @author Lucas Holt
  */
 @Controller
 @RequestMapping("/users")
 public class UsersController {
     public static final int SEARCH_MAX_LENGTH = 20;
     public static final float FONT_10_POINT = 10.0F;
     // constants
     private static final char endl = '\n';
     private static final Logger log = Logger.getLogger(UsersController.class);
     private static final long serialVersionUID = 1191172806869579057L;
     @SuppressWarnings({"InstanceVariableOfConcreteClass"})
     private Settings settings = null;
     private CommentDao commentDao = null;
     private EntryDao entryDao = null;
 
     public void setEntryDao(EntryDao entryDao) {
         this.entryDao = entryDao;
     }
 
     public void setCommentDao(CommentDao commentDao) {
         this.commentDao = commentDao;
     }
 
     @Autowired
     public void setSettings(Settings settings) {
         this.settings = settings;
     }
 
     @RequestMapping(value = "{username}", method = RequestMethod.GET, produces = "text/html")
     public String entries(@PathVariable("username") String username, @RequestParam(value = "skip", required = false) Integer skip, Model model, HttpSession session, HttpServletResponse response) {
         UserContext userContext = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userContext.getBlogUser());
 
         if (userContext.getBlogUser().isPrivateJournal() && !userContext.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userContext));
         model.addAttribute("recentEntries", getUserRecentEntries(userContext));
         model.addAttribute("links", getUserLinks(userContext));
         model.addAttribute("archive", getArchive(userContext));
         model.addAttribute("taglist", getTagMini(userContext));
 
         model.addAttribute("entries", getEntries(userContext, skip == null ? 0 : skip));
         return "users";
     }
 
     @RequestMapping(value = "{username}/entry/{id}", method = RequestMethod.GET, produces = "text/html")
     public String entry(@PathVariable("username") String username,
                         @PathVariable("id") int id,
                         Model model, HttpSession session, HttpServletResponse response) {
 
         UserContext userContext = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userContext.getBlogUser());
 
         if (userContext.getBlogUser().isPrivateJournal() && !userContext.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userContext));
         model.addAttribute("recentEntries", getUserRecentEntries(userContext));
         model.addAttribute("links", getUserLinks(userContext));
         model.addAttribute("archive", getArchive(userContext));
         model.addAttribute("taglist", getTagMini(userContext));
 
         model.addAttribute("entry", getSingleEntry(id, userContext));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/friends", method = RequestMethod.GET, produces = "text/html")
     public String friends(@PathVariable("username") String username, Model model, HttpSession session, HttpServletResponse response) {
         UserContext userc = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userc.getBlogUser());
 
         if (userc.getBlogUser().isPrivateJournal() && !userc.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userc));
         model.addAttribute("recentEntries", getUserRecentEntries(userc));
         model.addAttribute("links", getUserLinks(userc));
         model.addAttribute("archive", getArchive(userc));
         model.addAttribute("taglist", getTagMini(userc));
 
         model.addAttribute("friends", getFriends(userc));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/calendar", method = RequestMethod.GET, produces = "text/html")
     public String calendar(@PathVariable("username") String username, Model model, HttpSession session, HttpServletResponse response) {
 
         UserContext userContext = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userContext.getBlogUser());
 
         if (userContext.getBlogUser().isPrivateJournal() && !userContext.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userContext));
         model.addAttribute("recentEntries", getUserRecentEntries(userContext));
         model.addAttribute("links", getUserLinks(userContext));
         model.addAttribute("archive", getArchive(userContext));
         model.addAttribute("taglist", getTagMini(userContext));
 
         final java.util.Calendar cal = Calendar.getInstance();
         Integer year = cal.get(java.util.Calendar.YEAR);
 
         model.addAttribute("startYear", userContext.getBlogUser().getStartYear());
         model.addAttribute("currentYear", year);
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/{year}", method = RequestMethod.GET, produces = "text/html")
     public String calendarYear(@PathVariable("username") String username, @PathVariable("year") int year, Model model, HttpSession session, HttpServletResponse response) {
         UserContext userc = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userc.getBlogUser());
 
         if (userc.getBlogUser().isPrivateJournal() && !userc.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userc));
         model.addAttribute("recentEntries", getUserRecentEntries(userc));
         model.addAttribute("links", getUserLinks(userc));
         model.addAttribute("archive", getArchive(userc));
         model.addAttribute("taglist", getTagMini(userc));
 
         model.addAttribute("calendar", getCalendar(year, userc));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/{year}/{month}", method = RequestMethod.GET, produces = "text/html")
     public String calendarMonth(@PathVariable("username") String username,
                                 @PathVariable("year") int year,
                                 @PathVariable("month") int month,
                                 Model model, HttpSession session, HttpServletResponse response) {
         UserContext userc = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userc.getBlogUser());
 
         if (userc.getBlogUser().isPrivateJournal() && !userc.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userc));
         model.addAttribute("recentEntries", getUserRecentEntries(userc));
         model.addAttribute("links", getUserLinks(userc));
         model.addAttribute("archive", getArchive(userc));
         model.addAttribute("taglist", getTagMini(userc));
 
         model.addAttribute("calendar", getCalendarMonth(year, month, userc));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/{year}/{month}/{day}", method = RequestMethod.GET, produces = "text/html")
     public String calendarDay(@PathVariable("username") String username,
                               @PathVariable("year") int year,
                               @PathVariable("month") int month,
                               @PathVariable("day") int day, Model model, HttpServletResponse response, HttpSession session) {
         UserContext userc = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userc.getBlogUser());
 
         if (userc.getBlogUser().isPrivateJournal() && !userc.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userc));
         model.addAttribute("recentEntries", getUserRecentEntries(userc));
         model.addAttribute("links", getUserLinks(userc));
         model.addAttribute("archive", getArchive(userc));
         model.addAttribute("taglist", getTagMini(userc));
 
         model.addAttribute("calendar", getCalendarDay(year, month, day, userc));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/atom", method = RequestMethod.GET, produces = "text/xml; charset=UTF-8")
     public
     @ResponseBody
     String atom(@PathVariable("username") String username, HttpServletResponse response) {
         try {
             UserImpl user = new UserImpl(username);
 
             if (user.isPrivateJournal()) {
                 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                 return "";
             }
 
             return getAtom(user);
         } catch (Exception e) {
             log.error(e);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return "";
         }
     }
 
     @RequestMapping(value = "{username}/rss", method = RequestMethod.GET, produces = "application/rss+xml; charset=ISO-8859-1")
     public
     @ResponseBody
     String rss(@PathVariable("username") String username, HttpServletResponse response) {
         try {
             UserImpl user = new UserImpl(username);
 
             if (user.isPrivateJournal()) {
                 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                 return "";
             }
 
             return getRSS(user);
         } catch (Exception e) {
             log.error(e);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return "";
         }
     }
 
     @RequestMapping(value = "{username}/rsspics", method = RequestMethod.GET, produces = "application/rss+xml; charset=ISO-8859-1")
     public
     @ResponseBody
     String rssPictures(@PathVariable("username") String username, HttpServletResponse response) {
         try {
             UserImpl user = new UserImpl(username);
 
             if (user.isPrivateJournal()) {
                 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                 return "";
             }
 
             return getPicturesRSS(user);
         } catch (Exception e) {
             log.error(e);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return "";
         }
     }
 
     @RequestMapping(value = "{username}/pdf", method = RequestMethod.GET, produces = "application/pdf")
     public void pdf(@PathVariable("username") String username, HttpServletResponse response, HttpSession session) {
         UserImpl authUser = null;
         try {
             authUser = new UserImpl(WebLogin.currentLoginName(session));
         } catch (Exception ignored) {
 
         }
 
         try {
             UserImpl user = new UserImpl(username);
             UserContext userc = new UserContext(user, authUser);
             if (!(userc.getBlogUser().isPrivateJournal()) || userc.isAuthBlog()) {
                 getPDF(response, userc);
             } else
                 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         } catch (Exception e) {
             log.error(e);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
     }
 
     @RequestMapping(value = "{username}/rtf", method = RequestMethod.GET, produces = "application/rtf")
     public void rtf(@PathVariable("username") String username, HttpServletResponse response, HttpSession session) {
         UserImpl authUser = null;
         try {
             authUser = new UserImpl(WebLogin.currentLoginName(session));
         } catch (Exception ignored) {
 
         }
 
         try {
             UserImpl user = new UserImpl(username);
             UserContext userc = new UserContext(user, authUser);
             if (!(userc.getBlogUser().isPrivateJournal()) || userc.isAuthBlog())
                 getRTF(response, userc);
             else
                 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         } catch (Exception e) {
             log.error(e);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
     }
 
     @RequestMapping(value = "{username}/pictures", method = RequestMethod.GET, produces = "text/html")
     public String pictures(@PathVariable("username") String username, Model model, HttpSession session, HttpServletResponse response) {
         UserContext userc = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userc.getBlogUser());
 
         if (userc.getBlogUser().isPrivateJournal() && !userc.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userc));
         model.addAttribute("recentEntries", getUserRecentEntries(userc));
         model.addAttribute("links", getUserLinks(userc));
         model.addAttribute("archive", getArchive(userc));
         model.addAttribute("taglist", getTagMini(userc));
 
         model.addAttribute("pictures", getImageList(userc));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/search", method = RequestMethod.GET, produces = "text/html")
     public String search(@PathVariable("username") String username, @RequestParam String max, @RequestParam String bquery, Model model, HttpSession session, HttpServletResponse response) {
         UserContext userc = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userc.getBlogUser());
 
         if (userc.getBlogUser().isPrivateJournal() && !userc.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userc));
         model.addAttribute("recentEntries", getUserRecentEntries(userc));
         model.addAttribute("links", getUserLinks(userc));
         model.addAttribute("archive", getArchive(userc));
         model.addAttribute("taglist", getTagMini(userc));
         int maxr = SEARCH_MAX_LENGTH;
 
         if (max != null && max.length() > 0)
             try {
                 maxr = Integer.parseInt(max);
             } catch (NumberFormatException exInt) {
                 maxr = SEARCH_MAX_LENGTH;
                 log.error(exInt.getMessage());
             }
 
         model.addAttribute("search", search(userc, maxr, bquery));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/subscriptions", method = RequestMethod.GET, produces = "text/html")
     public String subscriptions(@PathVariable("username") String username, Model model, HttpSession session, HttpServletResponse response) {
         UserContext userc = getUserContext(username, session);
         model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
         model.addAttribute("user", userc.getBlogUser());
 
         if (userc.getBlogUser().isPrivateJournal() && !userc.isAuthBlog()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return "";
         }
 
         model.addAttribute("calendarMini", getCalendarMini(userc));
         model.addAttribute("recentEntries", getUserRecentEntries(userc));
         model.addAttribute("links", getUserLinks(userc));
         model.addAttribute("archive", getArchive(userc));
         model.addAttribute("taglist", getTagMini(userc));
 
         model.addAttribute("subscriptions", getSubscriptions(userc));
 
         return "users";
     }
 
     @RequestMapping(value = "{username}/tag/{tag}", method = RequestMethod.GET, produces = "text/html")
     public String tag(@PathVariable("username") String username,
                       @PathVariable("tag") String tag,
                       Model model, HttpSession session, HttpServletResponse response) {
 
         UserImpl authUser = null;
         try {
             authUser = new UserImpl(WebLogin.currentLoginName(session));
         } catch (Exception ignored) {
 
         }
 
         try {
             UserImpl user = new UserImpl(username);
             UserContext userc = new UserContext(user, authUser);
 
             model.addAttribute("username", user);
             model.addAttribute("authenticatedUsername", WebLogin.currentLoginName(session));
 
             if (!(userc.getBlogUser().isPrivateJournal()) || userc.isAuthBlog())
                 model.addAttribute("tags", getTags(userc, tag));
             else
                 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         } catch (Exception e) {
             log.error(e);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
 
         return "users";
     }
 
     private UserContext getUserContext(String username, HttpSession session) {
         UserImpl authUser = null;
         try {
             authUser = new UserImpl(WebLogin.currentLoginName(session));
         } catch (Exception ignored) {
 
         }
 
         try {
             UserImpl user = new UserImpl(username);
             return new UserContext(user, authUser);
         } catch (Exception e) {
             log.error(e);
         }
         return null;
     }
 
     private void getPDF(final HttpServletResponse response, final UserContext uc) {
         try {
             response.reset();
 
             final Document document = new Document();
             final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter.getInstance(document, baos);
             formatRTFPDF(uc, document);
             document.close();
 
             response.setContentType("application/pdf");
             response.setHeader("Expires", "0");
             response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
             response.setHeader("Pragma", "public");
             // RFC 1806
             response.setHeader("Content-Disposition", "attachment; filename=" + uc.getBlogUser().getUserName() + ".pdf");
             response.setContentLength(baos.size()); /* required by IE */
             final ServletOutputStream out = response.getOutputStream();
             baos.writeTo(out);
             out.flush();
             out.close();
         } catch (DocumentException e) {
             log.error("Users.getPDF():" + e.getMessage());
         } catch (IOException e1) {
             log.error("Users.getPDF():" + e1.getMessage());
         } catch (Exception e) {
             // user class caused this
             log.error("Users.getPDF():" + e.getMessage());
         }
     }
 
     private void getRTF(final HttpServletResponse response, final UserContext uc) {
         try {
             final Document document = new Document();
             final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             RtfWriter2.getInstance(document, baos);
             formatRTFPDF(uc, document);
             document.close();
 
             response.setContentType("application/rtf");
             response.setHeader("Expires", "0");
             response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
             response.setHeader("Pragma", "public");
             // RFC 1806
             response.setHeader("Content-Disposition", "attachment; filename=" + uc.getBlogUser().getUserName() + ".rtf");
             response.setContentLength(baos.size()); /* required by IE */
             final ServletOutputStream out = response.getOutputStream();
             baos.writeTo(out);
             out.flush();
             out.close();
         } catch (DocumentException e) {
             log.error("Users.getPDF():" + e.getMessage());
         } catch (IOException e1) {
             log.error("Users.getPDF():" + e1.getMessage());
         } catch (Exception e) {
             // user class caused this
             log.error("Users.getPDF():" + e.getMessage());
         }
     }
 
     private void formatRTFPDF(final UserContext uc, final Document document)
             throws Exception {
 
         document.open();
         document.add(new Paragraph(""));
         Chunk chunk = new Chunk(uc.getBlogUser().getJournalName());
         chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_STROKE, 0.4f, new Color(0x00, 0x00, 0xFF));
         document.add(chunk);
         document.add(new Paragraph(new Date().toString(), new Font(Font.HELVETICA, FONT_10_POINT)));
         document.add(Chunk.NEWLINE);
 
         final List<EntryTo> entries;
 
         entries = entryDao.viewAll(uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
         // Format the current time.
         final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
         final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
         final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
         String lastDate = "";
         String curDate;
 
         for (EntryTo o : entries) {
             // Parse the previous string back into a Date.
             final ParsePosition pos = new ParsePosition(0);
             final Date currentDate = formatter.parse(o.getDate().toString(), pos);
 
             curDate = formatmydate.format(currentDate);
 
             if (curDate.compareTo(lastDate) != 0) {
                 document.add(new Paragraph(curDate, new Font(Font.HELVETICA, 14.0F)));
                 lastDate = curDate;
             }
 
             document.add(new Paragraph(formatmytime.format(currentDate), new Font(Font.HELVETICA, 12.0F)));
             document.add(Chunk.NEWLINE);
             chunk = new Chunk(o.getSubject());
             chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL, 0.3F, new Color(0x00, 0x00, 0x00));
             document.add(chunk);
             document.add(Chunk.NEWLINE);
 
             document.add(new Paragraph(HTMLUtil.textFromHTML(o.getBody()), new Font(Font.TIMES_ROMAN, 11.0F)));
             document.add(Chunk.NEWLINE);
 
             if (o.getSecurityLevel() == 0)
                 document.add(new Paragraph("Security: " + "Private", new Font(Font.HELVETICA, 10.0F)));
             else if (o.getSecurityLevel() == 1)
                 document.add(new Paragraph("Security: " + "Friends", new Font(Font.HELVETICA, 10.0F)));
             else
                 document.add(new Paragraph("Security: " + "Public", new Font(Font.HELVETICA, 10.0F)));
 
             document.add(new Chunk("Location: " + o.getLocationName()));
             document.add(Chunk.NEWLINE);
             document.add(new Chunk("Mood: " + o.getMoodName()));
             document.add(Chunk.NEWLINE);
             document.add(new Chunk("Music: " + o.getMusic()));
             document.add(Chunk.NEWLINE);
             document.add(Chunk.NEWLINE);
         }
     }
 
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private String getImageList(@NotNull final UserContext uc) {
         StringBuilder sb = new StringBuilder();
         sb.append("<h2>Pictures</h2>");
         sb.append(endl);
         sb.append("<ul style=\"list-style-image: url('/images/pictureframe.png'); list-style-type: circle;\">");
 
 
         ResultSet rs = null;
         String imageTitle;
         final String sqlStmt = "SELECT id, title FROM user_images WHERE owner='" + uc.getBlogUser().getUserId() + "' ORDER BY title;";
 
         try {
             rs = SQLHelper.executeResultSet(sqlStmt);
 
             while (rs.next()) {
                 imageTitle = rs.getString("title");
 
                 sb.append("\t<li>");
                 sb.append("<a href=\"/AlbumImage?id=");
                 sb.append(rs.getString("id"));
                 sb.append("\" rel=\"lightbox\" title=\"");
                 sb.append(imageTitle);
                 sb.append("\">");
                 sb.append(imageTitle);
                 sb.append("</a>");
                 sb.append("</li>");
                 sb.append(endl);
             }
 
             rs.close();
 
         } catch (Exception e1) {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
         sb.append("</ul>\n");
         sb.append("<p>Subscribe to pictures ");
         sb.append("<a href=\"/users/");
         sb.append(uc.getBlogUser().getUserName());
         sb.append("/rsspics\">feed</a>.</p>");
 
         return sb.toString();
     }
 
     private String getSubscriptions(final UserContext uc) {
         StringBuilder sb = new StringBuilder();
 
         sb.append("<h2>RSS Reader</h2>");
         sb.append(endl);
         sb.append("<p>This page might be slow because we must wait for RSS to download from different servers!.</p>");
         sb.append(endl);
         sb.append("<p><a href=\"javascript:sweeptoggle('contract')\">Contract All</a> | <a href=\"javascript:sweeptoggle('expand')\">Expand All</a></p>");
         sb.append(endl);
 
         final CachedHeadlineBean hb = new CachedHeadlineBean();
 
         try {
             final Collection<RssSubscriptionsTO> rssfeeds = RssSubscriptionsDAO.view(uc.getBlogUser().getUserId());
 
             /* Iterator */
             RssSubscriptionsTO o;
             final Iterator itr = rssfeeds.iterator();
             for (int i = 0, n = rssfeeds.size(); i < n; i++) {
                 o = (RssSubscriptionsTO) itr.next();
 
                 sb.append(hb.parse(o.getUri()));
                 sb.append(endl);
             }
 
         } catch (Exception e) {
             log.error(e.getMessage());
             return null;
         }
         return sb.toString();
     }
 
     private String getSingleEntry(final int singleEntryId, final UserContext uc) {
 
         StringBuffer sb = new StringBuffer();
         EntryTo o;
 
         if (singleEntryId < 1) {
             WebError.Display("Invalid Entry Id", "The entry id was invalid for the journal entry you tried to get.", sb);
         } else {
             try {
                 if (uc.isAuthBlog()) {
                     o = entryDao.viewSingle(singleEntryId, uc.authenticatedUser.getUserId());
 
                     log.debug("getSingleEntry: User is logged in.");
                 } else {
                     o = entryDao.viewSinglePublic(singleEntryId);
                     log.debug("getSingleEntry: User is not logged in.");
                 }
 
                 log.trace("getSingleEntry: Begin reading record.");
 
                 if (o != null && o.getId() > 0) {
                     final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
 
                     String curDate = formatmydate.format(o.getDate());
 
                     sb.append("<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
 
                     sb.append(formatEntry(uc, o, o.getDate(), true));
                 }
             } catch (Exception e1) {
                 log.error("getSingleEntry: " + e1.getMessage() + '\n' + e1.toString());
 
                 WebError.Display("Error",
                         "Unable to retrieve journal entry from data store.",
                         sb);
             }
         }
         return sb.toString();
     }
 
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private String search(final UserContext uc, final int maxresults, final String bquery) {
         StringBuilder sb = new StringBuilder();
         ResultSet brs = null;
         String sql;
 
         if (uc.isAuthBlog())
             sql = "SELECT entry.subject AS subject, entry.body AS body, entry.date AS date, entry.id AS id, user.username AS username from entry, user, user_pref WHERE entry.uid = user.id AND entry.uid=user_pref.id AND user.username='" + uc.getBlogUser().getUserName() + "' AND ";
         else
             sql = "SELECT entry.subject AS subject, entry.body AS body, entry.date AS date, entry.id AS id, user.username AS username from entry, user, user_pref WHERE entry.uid = user.id AND entry.uid=user_pref.id AND user_pref.owner_view_only = 'N' AND entry.security=2 AND user.username='" + uc.getBlogUser().getUserName() + "' AND ";
 
         if (bquery != null && bquery.length() > 0) {
             try {
                 final BaseSearch b = new BaseSearch();
 
                 if (log.isDebugEnabled()) {
                     log.debug("Search base is: " + sql);
                     log.debug("Max results are: " + maxresults);
                     log.debug("Search query is: " + bquery);
                 }
 
                 b.setBaseQuery(sql);
                 b.setFields("subject body");
                 b.setMaxResults(maxresults);
                 b.setSortAscending("date");
                 brs = b.search(bquery);
 
                 sb.append("<h2><img src=\"/images/icon_search.gif\" alt=\"Search Blog\" style=\"float: left;\" /> Blog Search</h2>");
                 sb.append(endl);
 
                 if (brs == null) {
                     sb.append("<p>No items were found matching your search criteria.</p>");
                     sb.append(endl);
                 } else {
 
                     while (brs.next()) {
 
                         // Format the current time.
                         final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                         final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
                         final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
                         String curDate;
 
                         // Parse the previous string back into a Date.
                         final ParsePosition pos = new ParsePosition(0);
                         final java.util.Date currentDate = formatter.parse(brs.getString("date"), pos);
 
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
                         sb.append("</span> - <span class=\"subject\"><a href=\"/users/").append(brs.getString("username")).append("/entry/").append(brs.getString("id")).append("\">");
                         sb.append(Xml.cleanString(brs.getString("subject")));
                         sb.append("</a></span></h3> ");
                         sb.append(endl);
 
                         sb.append("<div class=\"ebody\">");
                         sb.append(endl);
                         sb.append(brs.getString("body"));
                         sb.append(endl);
                         sb.append("</div>");
                         sb.append(endl);
                     }
 
                     brs.close();
                 }
 
             } catch (Exception e1) {
                 log.error("Could not close database resultset on first attempt: " + e1.getMessage());
                 if (brs != null) {
                     try {
                         brs.close();
                     } catch (Exception e) {
                         log.error("Could not close database resultset: " + e.getMessage());
                     }
                 }
             }
         }
         return sb.toString();
     }
 
     private String getEntries(final UserContext uc, final int skip) {
         StringBuffer sb = new StringBuffer();
         final List<EntryTo> entries;
 
         try {
             if (uc.isAuthBlog()) {
                 entries = entryDao.view(uc.getBlogUser().getUserName(), true, skip);  // should be true
 
                 if (log.isDebugEnabled())
                     log.debug("getEntries: User is logged in.");
             } else {
                 entries = entryDao.view(uc.getBlogUser().getUserName(), false, skip);
 
                 if (log.isDebugEnabled())
                     log.debug("getEntries: User is not logged in.");
             }
 
             // Format the current time.
             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
             final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
 
             String lastDate = "";
             String curDate;
 
             sb.append(jumpmenu(skip, 20, entries.size() > 19, skip > 0, uc));
 
             if (log.isDebugEnabled())
                 log.debug("getEntries: Begin Iteration of records.");
 
             for (EntryTo o : entries) {
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
                 curDate = formatmydate.format(currentDate);
 
                 if (curDate.compareTo(lastDate) != 0) {
                     sb.append("\t\t<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
                     lastDate = curDate;
                 }
 
                 sb.append(formatEntry(uc, o, currentDate, false));
             }
 
             sb.append(jumpmenu(skip, 20, entries.size() > 19, skip > 0, uc));
 
         } catch (Exception e1) {
             WebError.Display("Error",
                     "Unable to retrieve journal entries from data store.",
                     sb);
 
             if (log.isDebugEnabled())
                 log.debug("getEntries: Exception is " + e1.getMessage() + '\n' + e1.toString());
         }
         return sb.toString();
     }
 
     private String jumpmenu(final int skip, final int offset, final boolean back, final boolean forward, final UserContext uc) {
         StringBuilder sb = new StringBuilder();
 
         sb.append("<ul class=\"pager\">");
        sb.append("<li class=\"previous " + (back ? "" : "disabled") + "\"><a href=\"/users/");
         sb.append(uc.getBlogUser().getUserName());
         sb.append("?skip=");
         sb.append((skip + offset));
         sb.append("\">&larr; Older</a></li>");
        sb.append("<li class=\"next " + (forward ? "" : "disabled") + "\"><a href=\"/users/");
         sb.append(uc.getBlogUser().getUserName());
         sb.append("?skip=");
         sb.append((skip - offset));
         sb.append("\">Newer &rarr;</a></li>");
         sb.append("</ul>");
 
         return sb.toString();
     }
 
     /**
      * Displays friends entries for a particular user.
      *
      * @param uc The UserContext we are working on including blog owner, authenticated user, and sb to write
      */
     private String getFriends(final UserContext uc) {
 
         StringBuffer sb = new StringBuffer();
         final Collection entries;
 
         if (uc.getAuthenticatedUser() != null)
             entries = entryDao.viewFriends(uc.getBlogUser().getUserId(), uc.getAuthenticatedUser().getUserId());
         else
             entries = entryDao.viewFriends(uc.getBlogUser().getUserId(), 0);
 
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
 
             log.trace("getFriends: Number of entries " + entries.size());
 
             if (entries.isEmpty())
                 sb.append("<p>No friends entries found</p>.");
 
             for (int i = 0, n = entries.size(); i < n; i++) {
                 o = (EntryTo) itr.next();
 
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final java.util.Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
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
 
                 final User p = new UserImpl(o.getUserName());
                 if (p.showAvatar()) {
                     sb.append("<img alt=\"avatar\" style=\"float: right\" src=\"/image?id=");
                     sb.append(o.getUserId());
                     sb.append("\"/>");
                     sb.append(endl);
                 }
 
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
                 sb.append(Xml.cleanString(o.getSubject()));
                 sb.append("</span></h3> ");
                 sb.append(endl);
 
                 sb.append("<div class=\"ebody\">");
                 sb.append(endl);
 
                 // Keep this synced with getEntries()
                 if (o.getAutoFormat()) {
                     sb.append("<p>");
                     if (o.getBodyWithLinks().contains("\n"))
                         sb.append(StringUtil.replace(o.getBodyWithLinks(), '\n', "<br />"));
                     else if (o.getBody().contains("\r"))
                         sb.append(StringUtil.replace(o.getBodyWithLinks(), '\r', "<br />"));
                     else
                         // we do not have any "new lines" but it might be
                         // one long line.
                         sb.append(o.getBodyWithLinks());
 
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
                     final EmoticonTo emoto = EmoticonDao.get(1, o.getMoodId());
 
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
                     sb.append(Xml.cleanString(o.getMusic()));
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 sb.append("</p>");
                 sb.append(endl);
 
                 sb.append("<p>tags:");
                 for (final String s : o.getTags()) {
                     sb.append(" ");
                     sb.append(s);
                 }
                 sb.append("</p>");
                 sb.append(endl);
 
                 sb.append("<div>");
                 sb.append(endl);
                 sb.append("<table width=\"100%\"  border=\"0\">");
                 sb.append(endl);
                 sb.append("<tr>");
                 sb.append(endl);
 
                 if (uc.getAuthenticatedUser() != null && uc.getAuthenticatedUser().getUserId() == o.getUserId()) {
                     sb.append("<td width=\"30\"><a title=\"Edit Entry\" href=\"/#/entry/").append(o.getId());
                     sb.append("\"><i class=\"fa fa-pencil-square-o\"></i></a></td>");
                     sb.append(endl);
                     sb.append("<td width=\"30\"><a title=\"Delete Entry\" onclick=\"return confirmDelete()\"; href=\"/entry/delete.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><i class=\"fa fa-trash-o\"></i></a>");
                     sb.append("</td>");
                     sb.append(endl);
 
                     sb.append("<td width=\"30\"><a title=\"Add Favorite\" href=\"/favorite/add.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><i class=\"fa fa-heart\"></i></a></td>");
                     sb.append(endl);
                 } else if (uc.getAuthenticatedUser() != null) {
                     sb.append("<td width=\"30\"><a title=\"Add Favorite\" href=\"/favorite/add.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><i class=\"fa fa-heart\"></i></a></td>");
                     sb.append(endl);
                 }
 
                 sb.append("<td><div style=\"float: right\"><a href=\"/users/").append(o.getUserName()).append("/entry/");
                 sb.append(o.getId());
                 sb.append("\" title=\"Link to this entry\">link</a> ");
                 sb.append('(');
 
                 switch (o.getCommentCount()) {
                     case 0:
                         break;
                     case 1:
                         sb.append("<a href=\"/comment/index.jsp?id=");
                         sb.append(o.getId());
                         sb.append("\" title=\"View Comment\">1 comment</a> | ");
                         break;
                     default:
                         sb.append("<a href=\"/comment/index.jsp?id=");
                         sb.append(o.getId());
                         sb.append("\" title=\"View Comments\">");
                         sb.append(o.getCommentCount());
                         sb.append(" comments</a> | ");
                 }
 
                 sb.append("<a href=\"/comment/add.jsp?id=");
                 sb.append(o.getId());
                 sb.append("\" title=\"Leave a comment on this entry\">comment on this</a>)");
 
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
             log.error(e1.getMessage());
             WebError.Display(" Error",
                     "Error retrieving the friends entries",
                     sb);
         }
         return sb.toString();
     }
 
     /**
      * Prints the calendar for the year specified for months with journal entries.  Other months are not printed.
      *
      * @param year The year to print
      * @param uc   The UserContext we are working on including blog owner, authenticated user, and sb to write
      * @see com.justjournal.Cal
      * @see com.justjournal.CalMonth
      */
     private String getCalendar(final int year,
                                final UserContext uc) {
         StringBuffer sb = new StringBuffer();
         final java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
         int yearNow = calendar.get(Calendar.YEAR);
 
         // print out header
         sb.append("<h2>Calendar: ");
         sb.append(year);
         sb.append("</h2>");
         sb.append(endl);
 
         sb.append("<p>The calendar lists months with journal entries.</p>");
         sb.append(endl);
 
         // BEGIN: YEARS
         sb.append("<p>");
 
         for (int i = yearNow; i >= uc.getBlogUser().getStartYear(); i--) {
 
             sb.append("<a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append('/');
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
 
         try {
             Collection<EntryTo> entries = entryDao.viewCalendarYear(year, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (entries == null || entries.size() == 0) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
                 // we have calendar data!
                 final Cal mycal = new Cal(entries);
                 sb.append(mycal.render());
             }
 
         } catch (Exception e1) {
             WebError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
 
         return sb.toString();
     }
 
     /**
      * Lists all of the journal entries for the month specified in the year specified.
      *
      * @param year  the year to display data for
      * @param month the month we want
      * @param uc    The UserContext we are working on including blog owner, authenticated user, and sb to write
      */
     private String getCalendarMonth(final int year,
                                     final int month,
                                     final UserContext uc) {
         StringBuffer sb = new StringBuffer();
 
         sb.append("<h2>Calendar: ");
         sb.append(month);
         sb.append('/');
         sb.append(year);
         sb.append("</h2>");
         sb.append(endl);
 
         sb.append("<p>This page lists all of the journal entries for the month.</p>");
         sb.append(endl);
 
         try {
             Collection<EntryTo> entries = entryDao.viewCalendarMonth(year, month, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (entries.size() == 0) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
 
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("dd");
                 final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
 
                 String curDate;
                 String lastDate = "";
 
                 for (EntryTo entryTo : entries) {
 
                     Date currentDate = entryTo.getDate();
                     curDate = formatmydate.format(currentDate);
 
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("<p><strong>").append(curDate).append("</strong></p>");
                         lastDate = curDate;
                     }
 
                     sb.append("<p><span class=\"time\">").append(formatmytime.format(currentDate)).append("</span> - <span class=\"subject\"><a href=\"");
 
                     /*TODO: fix bug where relative url is incorrect
                        Need to check if we are in a calendar state and
                        drop the extra parts on the request.
                        it is appended 08/02/08/02 etc. */
                     if (month < 10)
                         sb.append('0');
 
                     sb.append(month).append('/').append(curDate).append("\">").append(entryTo.getSubject()).append("</a></span></p> ");
                     sb.append(endl);
                 }
             }
 
         } catch (Exception e1) {
             WebError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
         return sb.toString();
     }
 
     /**
      * Print a mini calendar for the current month with blog entries counts for given days in HTML.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private String getCalendarMini(UserContext uc) {
         StringBuilder sb = new StringBuilder();
         try {
             final Calendar cal = new GregorianCalendar(java.util.TimeZone.getDefault());
             int year = cal.get(Calendar.YEAR);
             int month = cal.get(Calendar.MONTH) + 1; // zero based
 
             Collection<EntryTo> entries = entryDao.viewCalendarMonth(year, month, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (entries.size() == 0) {
                 sb.append("\t<!-- could not render calendar -->");
                 sb.append(endl);
             } else {
                 final Cal mycal = new Cal(entries);
                 mycal.setBaseUrl("/users/" + uc.getBlogUser().getUserName() + '/');
                 sb.append(mycal.renderMini());
             }
         } catch (Exception ex) {
             log.debug(ex);
         }
         return sb.toString();
     }
 
     /**
      * Print a list of tags in HTML that the blog owner is using.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private String getTagMini(final UserContext uc) {
         StringBuilder sb = new StringBuilder();
         Tag tag;
         final Iterable<Tag> tags = entryDao.getUserTags(uc.getBlogUser().getUserId());
         int largest = 0;
         int smallest = 10;
         int cutSmall;
         int cutLarge;
 
         for (final Tag tag1 : tags) {
             tag = tag1;
             if (tag.getCount() > largest)
                 largest = tag.getCount();
 
             if (tag.getCount() < smallest)
                 smallest = tag.getCount();
         }
 
         cutSmall = largest / 3;
         cutLarge = cutSmall * 2;
 
         sb.append("\t<div class=\"menuentity\" id=\"usertags\" style=\"padding-top: 10px;\">\n\t\t<strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\"><i class=\"fa fa-tags\"></i> Tags</strong>\n\t\t<p style=\"padding-left: 0; margin-left: 0;\">\n");
         for (final Tag tag1 : tags) {
             tag = tag1;
             sb.append("<a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append("/tag/");
             sb.append(tag.getName());
             sb.append("\" class=\"");
             if (tag.getCount() > cutLarge)
                 sb.append("TagCloudLarge");
             else if (tag.getCount() < cutSmall)
                 sb.append("TagCloudSmall");
             else
                 sb.append("TagCloudMedium");
             sb.append("\">");
             sb.append(tag.getName());
             sb.append("</a>");
             sb.append(endl);
         }
         sb.append("\t\t</p>\n\t</div>");
         sb.append(endl);
 
         return sb.toString();
     }
 
     /**
      * Print a list of links the user has added to their blog in HTML.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private String getUserLinks(final UserContext uc) {
         log.debug("getUserLinks(): Init and load collection");
         StringBuilder sb = new StringBuilder();
         UserLinkTo link;
         Collection links = UserLinkDao.view(uc.getBlogUser().getUserId());
 
         if (!links.isEmpty()) {
             sb.append("\t<div class=\"menuentity\" id=\"userlinks\" style=\"padding-top: 10px;\">\n\t\t<strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\"><i class=\"fa fa-external-link-square\"></i> Links</strong>\n\t\t<ul style=\"padding-left: 0; margin-left: 0;\">\n");
             final Iterator itr = links.iterator();
             for (int i = 0, n = links.size(); i < n; i++) {
                 link = (UserLinkTo) itr.next();
                 sb.append("\t\t\t<li><a href=\"").append(link.getUri()).append("\" title=\"").append(link.getTitle()).append("\">").append(link.getTitle()).append("</a></li>");
                 sb.append(endl);
             }
             sb.append("\t\t</ul>\n\t</div>");
             sb.append(endl);
         }
         return sb.toString();
     }
 
     /**
      * Print a short list of recent blog entries in HTML
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private String getUserRecentEntries(final UserContext uc) {
         StringBuilder sb = new StringBuilder();
         final Collection<EntryTo> entries;
         final int maxrecent = 5;
 
         try {
             entries = entryDao.view(uc.getBlogUser().getUserName(), uc.isAuthBlog(), 0);
 
             if (log.isDebugEnabled())
                 log.debug("getUserRecentEntries: Begin Iteration of records.");
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             sb.append("\t<div class=\"menuentity\" id=\"userRecentEntries\">\n<strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\">Recent Entries</strong>\n");
             sb.append("\t\t<ul style=\"padding-left: 0; margin-left: 0;\">");
             sb.append(endl);
 
             int n = entries.size();
             if (maxrecent < n) {
                 n = maxrecent;
             }
             for (int i = 0; i < n; i++) {
                 o = (EntryTo) itr.next();
                 sb.append("\t\t\t<li><a href=\"/users/");
                 sb.append(uc.getBlogUser().getUserName());
                 sb.append("/entry/");
                 sb.append(o.getId());
                 sb.append("\" title=\"");
                 sb.append(Xml.cleanString(o.getSubject()));
                 sb.append("\">");
                 sb.append(Xml.cleanString(o.getSubject()));
                 sb.append("</a></li>");
                 sb.append(endl);
 
             }
             sb.append("\t\t</ul>");
             sb.append(endl);
             sb.append("\t</div>");
             sb.append(endl);
         } catch (Exception e) {
             log.error(e.getMessage());
         }
         return sb.toString();
     }
 
     /**
      * Generates all of the HTML to display journal entires for a particular day specified in the url.
      *
      * @param year  the year to display
      * @param month the month we want to look at
      * @param day   the day we are interested in
      * @param uc    The UserContext we are working on including blog owner, authenticated user, and sb to write
      */
     private String getCalendarDay(final int year,
                                   final int month,
                                   final int day,
                                   final UserContext uc) {
 
         StringBuffer sb = new StringBuffer();
 
         // sb.append("<h2>Calendar: " + day + "/" + month + "/" + year + "</h2>" );
 
         sb.append("<p>Lists all of the journal entries for the day.</p>");
         sb.append(endl);
 
         try {
 
             final Collection<EntryTo> entries;
             entries = entryDao.viewCalendarDay(year, month, day, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (entries == null || entries.size() == 0) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
                 final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
 
                 String lastDate = "";
                 String curDate;
 
                 /* Iterator */
                 EntryTo o;
                 final Iterator itr = entries.iterator();
 
                 for (int i = 0, n = entries.size(); i < n; i++) {
                     o = (EntryTo) itr.next();
 
                     // Parse the previous string back into a Date.
                     final ParsePosition pos = new ParsePosition(0);
                     final java.util.Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
                     curDate = formatmydate.format(currentDate);
 
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("\t\t<h2>");
                         sb.append(curDate);
                         sb.append("</h2>");
                         sb.append(endl);
                         lastDate = curDate;
                     }
 
                     sb.append(formatEntry(uc, o, currentDate, false));
                 }
             }
 
         } catch (Exception e1) {
             WebError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
 
         return sb.toString();
     }
 
     /**
      * Print a list of months and years that users have blogged in as a history breadcrumb to access the calendar list
      * of blog entries in HTML.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private String getArchive(final UserContext uc) {
         final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
         int yearNow = calendarg.get(Calendar.YEAR);
         StringBuffer sb = new StringBuffer();
 
         // BEGIN: YEARS
         sb.append("\t<div class=\"menuentity\" id=\"archive\" style=\"padding-top: 10px;\"><strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\">Archive</strong><ul style=\"padding-left: 0; margin-left: 0;\">");
 
         for (int i = yearNow; i >= uc.getBlogUser().getStartYear(); i--) {
 
             sb.append("<li><a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append('/');
             sb.append(i);
             sb.append("\">");
             sb.append(i);
             sb.append(" (");
             try {
                 sb.append(entryDao.calendarCount(i, uc.getBlogUser().getUserName()));
             } catch (Exception e) {
                 log.error("getArchive: could not fetch count for " + uc.getBlogUser().getUserName() + ": " + i + e.getMessage());
                 sb.append("0");
             }
             sb.append(")</a></li> ");
 
             // just in case!
             if (i == 2002)
                 break;
         }
 
         sb.append("</ul>");
         sb.append(endl);
         sb.append("</div>");
         sb.append(endl);
         // END: YEARS
 
         return sb.toString();
     }
 
     /**
      * Handles requests for syndication content (RSS). Only returns public journal entries for the specified user.
      *
      * @param user
      */
     private String getRSS(final UserImpl user) {
         Rss rss = new Rss();
 
         final java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
         calendar.setTime(new java.util.Date());
 
         rss.setTitle(user.getUserName());
         rss.setLink("http://www.justjournal.com/users/" + user.getUserName());
         rss.setSelfLink("http://www.justjournal.com/users/" + user.getUserName() + "/rss");
         rss.setDescription("Just Journal for " + user.getUserName());
         rss.setLanguage("en-us");
         rss.setCopyright("Copyright " + calendar.get(Calendar.YEAR) + ' ' + user.getFirstName());
         rss.setWebMaster("webmaster@justjournal.com (Lucas)");
         // RSS advisory board format
         rss.setManagingEditor(user.getEmailAddress() + " (" + user.getFirstName() + ")");
         rss.populate(entryDao.view(user.getUserName(), false));
         return rss.toXml();
     }
 
     /**
      * Handles requests for syndication content (Atom). Only returns public journal entries for the specified user.
      *
      * @param user blog user
      */
     private String getAtom(final UserImpl user) {
 
         AtomFeed atom = new AtomFeed();
 
         final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
         calendarg.setTime(new java.util.Date());
 
         atom.setUserName(user.getUserName());
         atom.setAlternateLink("http://www.justjournal.com/users/" + user.getUserName());
         atom.setAuthorName(user.getFirstName());
         atom.setUpdated(calendarg.toString());
         atom.setTitle(user.getJournalName());
         atom.setId("http://www.justjournal.com/users/" + user.getUserName() + "/atom");
         atom.setSelfLink("/users/" + user.getUserName() + "/atom");
         atom.populate(entryDao.view(user.getUserName(), false));
         return (atom.toXml());
     }
 
     /**
      * List the pictures associated with a blog in RSS.  This should be compatible with iPhoto.
      *
      * @param user blog user
      */
     private String getPicturesRSS(final UserImpl user) {
 
         final Rss rss = new Rss();
 
         final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
         calendarg.setTime(new java.util.Date());
 
         rss.setTitle(user.getUserName() + "\'s pictures");
         rss.setLink("http://www.justjournal.com/users/" + user.getUserName() + "/pictures");
         rss.setSelfLink("http://www.justjournal.com/users/" + user.getUserName() + "/pictures/rss");
         rss.setDescription("Just Journal Pictures for " + user.getUserName());
         rss.setLanguage("en-us");
         rss.setCopyright("Copyright " + calendarg.get(Calendar.YEAR) + ' ' + user.getFirstName());
         rss.setWebMaster("webmaster@justjournal.com (Luke)");
         // RSS advisory board format
         rss.setManagingEditor(user.getEmailAddress() + " (" + user.getFirstName() + ")");
         rss.populateImageList(user.getUserId(), user.getUserName());
         return (rss.toXml());
     }
 
     /* TODO: finish this */
     private String getTags(final UserContext uc, String tag) {
         final StringBuilder sb = new StringBuilder();
         final Collection entries;
 
         try {
             if (uc.isAuthBlog()) {
                 entries = entryDao.viewAll(uc.getBlogUser().getUserName(), true);  // should be true
 
                 if (log.isDebugEnabled())
                     log.debug("getTags: User is logged in.");
             } else {
                 entries = entryDao.viewAll(uc.getBlogUser().getUserName(), false);
 
                 if (log.isDebugEnabled())
                     log.debug("getTags: User is not logged in.");
             }
 
             // Format the current time.
             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
             final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
 
             String lastDate = "";
             String curDate;
 
             if (log.isDebugEnabled())
                 log.debug("getTags: Begin Iteration of records.");
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             for (int i = 0, n = entries.size(); i < n; i++) {
                 o = (EntryTo) itr.next();
 
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final java.util.Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
                 curDate = formatmydate.format(currentDate);
 
                 Collection entryTags = o.getTags();
 
                 if (entryTags.contains(tag.toLowerCase())) {
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("\t\t<h2>");
                         sb.append(curDate);
                         sb.append("</h2>");
                         sb.append(endl);
                         lastDate = curDate;
                     }
 
                     sb.append(formatEntry(uc, o, currentDate, false));
                 }
             }
         } catch (Exception e1) {
             if (log.isDebugEnabled())
                 log.debug("getTags: Exception is " + e1.getMessage() + '\n' + e1.toString());
         }
         return sb.toString();
     }
 
     /**
      * Format a blog entry in HTML
      *
      * @param uc          User Context
      * @param o           Entry to format
      * @param currentDate Date to format (of the entry)
      * @param single      Single blog entries are formatted differently
      * @return HTML formatted entry
      */
     protected String formatEntry(final UserContext uc, final EntryTo o, final Date currentDate, boolean single) {
         final StringBuilder sb = new StringBuilder();
         final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
 
         sb.append("\t\t<div class=\"ebody\">");
         sb.append(endl);
 
         if (single) {
             sb.append("<article><h3>");
             sb.append("<span class=\"time\">");
             sb.append(formatmytime.format(currentDate));
             sb.append("</span> - <span class=\"subject\"><a name=\"#e");
             sb.append(o.getId());
             sb.append("\">");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("</a></span></h3> ");
             sb.append(endl);
 
             sb.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n");
             sb.append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n");
             sb.append("xmlns:trackback=\"http://madskills.com/public/xml/rss/module/trackback/\">\n");
             sb.append("\t<rdf:Description ");
             sb.append("rdf:about=\"");
             sb.append("http://www.justjournal.com/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("#e");
             sb.append(o.getId());
             sb.append("\" dc:identifier=\"");
             sb.append("http://www.justjournal.com/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("#e");
             sb.append(o.getId());
             sb.append("\" dc:title=\"");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("\" ");
             sb.append("trackback:ping=\"http://www.justjournal.com/trackback?entryID=");
             sb.append(o.getId());
             sb.append("\" />\n");
             sb.append("</rdf:RDF>\n");
         } else {
             sb.append("<article><h3>");
             sb.append("<span class=\"time\">");
             sb.append(formatmytime.format(currentDate));
             sb.append("</span> - <span class=\"subject\">");
             sb.append("<a href=\"/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("\" rel=\"bookmark\" title=\"");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("\">");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("</a></span></h3> ");
             sb.append(endl);
         }
 
         sb.append("\t\t\t<div class=\"ebody\">");
         sb.append(endl);
 
 
         /*
            autoformat controls whether new lines should be
            converted to br's.  If someone used html, we don't want autoformat!
            We handle Windows/UNIX with the \n case and Mac OS Classic with \r
          */
         if (o.getAutoFormat()) {
 
             sb.append("\t\t\t\t<p>");
             if (o.getBodyWithLinks().contains("\n"))
                 sb.append(StringUtil.replace(o.getBodyWithLinks(), '\n', "<br />"));
             else if (o.getBody().contains("\r"))
                 sb.append(StringUtil.replace(o.getBodyWithLinks(), '\r', "<br />"));
             else
                 // we do not have any "new lines" but it might be
                 // one long line.
                 sb.append(o.getBodyWithLinks());
 
             sb.append("</p>");
         } else {
             sb.append(o.getBody());
         }
 
         sb.append(endl);
         sb.append("\t\t\t</div>");
         sb.append(endl);
 
         sb.append("\t\t\t<p>");
 
         if (o.getSecurityLevel() == 0) {
             sb.append("<span class=\"security\">security: ");
             sb.append("<img src=\"/img/icon_private.gif\" alt=\"private\" /> ");
             sb.append("private");
             sb.append("</span><br />");
             sb.append(endl);
         } else if (o.getSecurityLevel() == 1) {
             sb.append("\t\t\t<span class=\"security\">security: ");
             sb.append("<img src=\"/img/icon_protected.gif\" alt=\"friends\" /> ");
             sb.append("friends");
             sb.append("</span><br />");
             sb.append(endl);
         }
 
         if (o.getLocationId() > 0) {
             sb.append("\t\t\t<span class=\"location\">location: ");
             sb.append(o.getLocationName());
             sb.append("</span><br />");
             sb.append(endl);
         }
 
         if (o.getMoodName().length() > 0 && o.getMoodId() != 12) {
             final EmoticonTo emoto = EmoticonDao.get(1, o.getMoodId());
 
             sb.append("\t\t\t<span class=\"mood\">mood: <img src=\"/images/emoticons/1/");
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
             sb.append("\t\t\t<span class=\"music\">music: ");
             sb.append(Xml.cleanString(o.getMusic()));
             sb.append("</span><br />");
             sb.append(endl);
         }
 
         sb.append("\t\t\t</p>");
         sb.append(endl);
 
         Collection<String> ob = o.getTags();
         if (ob.size() > 0) {
             sb.append("<p>tags:");
             for (final String tagname : ob) {
                 sb.append(" ");
                 sb.append("<a href=\"/users/");
                 sb.append(uc.getBlogUser().getUserName());
                 sb.append("/tag/");
                 sb.append(tagname);
                 sb.append("\">");
                 sb.append(tagname);
                 sb.append("</a>");
             }
             sb.append("</p>");
             sb.append(endl);
         }
 
         sb.append("\t\t\t<div>");
         sb.append(endl);
         sb.append("\t\t\t\t<table width=\"100%\"  border=\"0\">");
         sb.append(endl);
         sb.append("\t\t\t\t\t<tr>");
         sb.append(endl);
 
         if (uc.isAuthBlog()) {
             sb.append("<td style=\"width: 30px\"><a title=\"Edit Entry\" href=\"/#/entry/");
             sb.append(o.getId());
             sb.append("\"><i class=\"fa fa-pencil-square-o\"></i></a></td>");
             sb.append(endl);
             sb.append("<td style=\"width: 30px\"><a title=\"Delete Entry\" onclick=\"return confirmDelete()\"; href=\"/entry/delete.h?entryId=");
             sb.append(o.getId());
             sb.append("\"><i class=\"fa fa-trash-o\"></i></a>");
             sb.append("</td>");
             sb.append(endl);
 
             sb.append("<td style=\"width: 30px\"><a title=\"Add Favorite\" href=\"/favorite/add.h?entryId=");
             sb.append(o.getId());
             sb.append("\"><i class=\"fa fa-heart\"></i></a></td>");
             sb.append(endl);
         }
 
         if (single) {
             sb.append("<td><div align=\"right\">");
             if (o.getSecurityLevel() == 2) {
                 sb.append("<iframe src=\"https://www.facebook.com/plugins/like.php?href=http://www.justjournal.com/users/").append(o.getUserName()).append("/entry/");
                 sb.append(o.getId());
                 sb.append("\" scrolling=\"no\" frameborder=\"0\" style=\"border:none; width:450px; height:80px\"></iframe>");
 
             }
             sb.append("</div></td>");
 
         } else {
 
             sb.append("<td><div style=\"float: right\"><a href=\"/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("\" title=\"Link to this entry\"><i class=\"fa fa-external-link\"></i></a> ");
 
             sb.append('(');
 
             switch (o.getCommentCount()) {
                 case 0:
                     break;
                 case 1:
                     sb.append("<a href=\"/comment/index.jsp?id=");
                     sb.append(o.getId());
                     sb.append("\" title=\"View Comment\">1 comment</a> | ");
                     break;
                 default:
                     sb.append("<a href=\"/comment/index.jsp?id=");
                     sb.append(o.getId());
                     sb.append("\" title=\"View Comments\">");
                     sb.append(o.getCommentCount());
                     sb.append(" comments</a> | ");
             }
 
             sb.append("<a href=\"/comment/add.jsp?id=");
             sb.append(o.getId());
             sb.append("\" title=\"Leave a comment on this entry\"><i class=\"fa fa-comment-o\"></i></a>)");
             sb.append("\t\t\t\t\t\t</div></td>");
             sb.append(endl);
         }
 
         sb.append("\t\t\t\t\t</tr>");
         sb.append(endl);
         sb.append("\t\t\t\t</table>");
         sb.append(endl);
         sb.append("\t\t\t</div>");
         sb.append(endl);
 
         if (single) {
             List<Comment> comments = commentDao.list(o.getId());
 
             sb.append("<div class=\"commentcount\">");
             sb.append(o.getCommentCount());
             sb.append(" comments</div>\n");
 
             sb.append("<div class=\"rightflt\">");
             sb.append("<a href=\"add.jsp?id=").append(o.getId()).append("\" title=\"Add Comment\">Add Comment</a></div>\n");
 
             for (Comment co : comments) {
                 sb.append("<div class=\"comment\">\n");
                 sb.append("<div class=\"chead\">\n");
                 sb.append("<h3><span class=\"subject\">");
                 sb.append(Xml.cleanString(co.getSubject()));
                 sb.append("</span></h3>\n");
                 sb.append("<img src=\"../images/userclass_16.png\" alt=\"user\"/>");
                 sb.append("<a href=\"../users/");
                 sb.append(co.getUserName());
                 sb.append("\" title=\"");
                 sb.append(co.getUserName());
                 sb.append("\">");
                 sb.append(co.getUserName());
                 sb.append("</a>\n");
 
                 sb.append("<br/><span class=\"time\">");
                 sb.append(co.getDate().toPubDate());
                 sb.append("</span>\n");
 
 
                 if (uc.getAuthenticatedUser().getUserName().equalsIgnoreCase(co.getUserName())) {
                     sb.append("<br/><span class=\"actions\">\n");
                     sb.append("<a href=\"edit.h?commentId=");
                     sb.append(co.getId());
                     sb.append("\" title=\"Edit Comment\">");
                     sb.append("     <i class=\"fa fa-pencil-square-o\"></i>");
                     sb.append("</a>\n");
 
                     sb.append("<a href=\"delete.h?commentId=");
                     sb.append(co.getId());
                     sb.append("\" title=\"Delete Comment\">");
                     sb.append("<i class=\"fa fa-trash-o\"></i>");
                     sb.append("</a>\n");
                     sb.append("</span>\n");
                 }
                 sb.append("</div>\n");
 
                 sb.append("<p>");
                 sb.append(Xml.cleanString(o.getBody()));
                 sb.append("</p>\n</div>\n");
             }
         }
 
         sb.append("\t\t</div></article>");
         sb.append(endl);
 
         return sb.toString();
     }
 
     /**
      * Represent the blog user and authenticated user in one package along with the output buffer.
      */
     @SuppressWarnings({"InstanceVariableOfConcreteClass"})
     static private class UserContext {
         private User blogUser;          // the blog owner
         private User authenticatedUser; // the logged in user
 
         /**
          * Default constructor for User Context.  Creates a usable instance.
          *
          * @param currentBlogUser blog owner
          * @param authUser        logged in user
          */
         UserContext(final User currentBlogUser, final User authUser) {
             this.blogUser = currentBlogUser;
             this.authenticatedUser = authUser;
         }
 
         /**
          * Retrieve the blog owner
          *
          * @return blog owner
          */
         public User getBlogUser() {
             return blogUser;
         }
 
         /**
          * Retrieve the authenticated aka logged in user.
          *
          * @return logged in user.
          */
         public User getAuthenticatedUser() {
             return authenticatedUser;
         }
 
         /**
          * Check to see if the authenticated user is the blog owner also. Used for private information.
          *
          * @return true if blog owner = auth owner
          */
         public boolean isAuthBlog() {
             return authenticatedUser != null && blogUser != null
                     && authenticatedUser.getUserName().compareTo(blogUser.getUserName()) == 0;
         }
     }
 }
