 /* A UI subcomponent to display and edit wikitext.
  *
  * Copyright (C) 2010, 2011 Darrell Karbott
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either
  * version 2.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * Author: djk@isFiaD04zgAgnrEC5XJt1i4IE7AkNPqhBG5bONi6Yks
  *
  *  This file was developed as component of
  * "fniki" (a wiki implementation running over Freenet).
  */
 
 package fniki.wiki.child;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import static ys.wikiparser.Utils.*;
 
 import fniki.wiki.ChildContainer;
 import fniki.wiki.ChildContainerException;
 import fniki.wiki.Query;
 
 import wormarc.FileManifest;
 import fniki.wiki.FreenetWikiTextParser;
 import static fniki.wiki.HtmlUtils.*;
 import fniki.wiki.WikiApp;
 import fniki.wiki.WikiContext;
 import fniki.wiki.WikiTextStorage;
 
 public class WikiContainer implements ChildContainer {
     private final static String ENCODING = "UTF-8";
 
     public WikiContainer() {}
 
     public String handle(WikiContext context) throws ChildContainerException {
         try {
             String action = context.getAction();
             if (action.equals("finished")) {
                 // Hack: Ignore "finished".
                 // This happens when the user hits the back button and picks
                 // a link from a finished task page. e.g. changelog.
                 action = "view";
             }
 
             String title = context.getTitle();
             Query query = context.getQuery();
 
             if (action.equals("view")) {
                 return handleView(context, title);
             } else if (action.equals("edit")) {
                 return handleEdit(context, title);
             } else if (action.equals("delete")) {
                 return handleDelete(context, title);
             } else if (action.equals("revert")) {
                 return handleRevert(context, title);
             } else if (action.equals("save")) {
                 return handleSave(context, query);
             } else  {
                 context.raiseAccessDenied("Couldn't work out query.");
             }
         } catch (IOException ioe) {
             context.logError("WikiContainer.handle", ioe);
             context.raiseServerError("Unexpected Error in WikiContainer.handle. Sorry :-(");
         }
         return "unreachable code";
     }
 
     private String handleView(WikiContext context, String name) throws IOException {
         return getPageHtml(context, name);
     }
 
     private String handleEdit(WikiContext context, String name) throws IOException {
         return getEditorHtml(context, name);
     }
 
     private String handleDelete(WikiContext context, String name) throws IOException {
         if (context.getStorage().hasPage(name)) {
             context.getStorage().deletePage(name);
         }
         // DCI: apply uniform style! add link to default page!
         String html =  "<html><head><title>Delete Page</title></head><body>Deleted Page</body></html>";
         return html;
     }
 
     private String handleRevert(WikiContext context, String name) throws ChildContainerException, IOException {
         context.getStorage().revertLocalChange(name);
         context.raiseRedirect(context.makeLink("/" + name), "Redirecting...");
         return "unreachable code";
     }
 
     private String handleSave(WikiContext context, Query form) throws ChildContainerException, IOException {
         // Name is included in the query data.
         System.err.println("handleSave -- ENTERED");
         String name = form.get("savepage");
         String wikiText = form.get("savetext");
 
         System.err.println("handleSave --got params");
         if (name == null || wikiText == null) {
             context.raiseAccessDenied("Couldn't parse parameters from POST.");
         }
 
         System.err.println("Writing: " + name);
        context.getStorage().putPage(name, unescapeHTML(wikiText));
         System.err.println("Raising redirect!");
         context.raiseRedirect(context.makeLink("/" + name), "Redirecting...");
         System.err.println("SOMETHING WENT WRONG!");
         return "unreachable code";
     }
 
     private String titleFromName(String name) {
         // DCI: html escape
         return name.replace("_", " "); // DCI: Much more to it?
     }
 
     private String getPageHtml(WikiContext context, String name) throws IOException {
         StringBuilder buffer = new StringBuilder();
         addHeader(name, buffer);
         if (context.getStorage().hasPage(name)) {
             buffer.append(renderXHTML(context, context.getStorage().getPage(name)));
         } else {
             if (name.equals(context.getString("default_page", "Front_Page"))) {
                 buffer.append(renderXHTML(context,
                                           context.getString("default_wikitext",
                                                             "Page doesn't exist in the wiki yet.")));
             } else {
                 buffer.append("Page doesn't exist in the wiki yet.");
             }
         }
         addFooter(context, name, buffer);
         return buffer.toString();
     }
 
     private void addHeader(String name, StringBuilder buffer) throws IOException {
         buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
                       "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
         buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
         buffer.append("<head><title>\n");
         buffer.append(escapeHTML(titleFromName(name)));
         buffer.append("</title>\n");
         buffer.append("<style type=\"text/css\">div.indent{margin-left:20px;} " +
                       "div.center{text-align:center;} " +
                       "blockquote{margin-left:20px;background-color:#e0e0e0;} " +
                       "span.underline{text-decoration:underline;}</style>\n");
         buffer.append("</head>\n");
         buffer.append("<body>\n");
         buffer.append("<h1>\n");
         buffer.append(escapeHTML(titleFromName(name)));
         buffer.append("</h1><hr>\n");
     }
 
     private String makeLocalLink(WikiContext context, String name, String action, String label) {
         String href = makeHref(context.makeLink(name), action, name, null, null);
         return String.format("<a href=\"%s\">%s</a>", href, escapeHTML(label));
     }
 
     private void addFooter(WikiContext context, String name, StringBuilder buffer) throws IOException {
         buffer.append("<hr>\n");
         buffer.append("Parent Version:<br>");
         // DCI: css class to make this smaller.
         String version = context.getString("parent_uri", "None");
         buffer.append(escapeHTML(version));
         buffer.append("<hr>\n");
 
         buffer.append(makeLocalLink(context, name, "edit", "Edit"));
         buffer.append(" this page.<br>");
 
         buffer.append(makeLocalLink(context, name, "delete", "Delete"));
         buffer.append(" this page without confirmation!<br>");
 
         if (context.getStorage().hasLocalChange(name)) {
             buffer.append(makeLocalLink(context, name, "revert", "Revert"));
             buffer.append(" local changes to this page without confirmation!<br>");
         }
 
         buffer.append(makeLocalLink(context, "fniki/submit", null, "Submit"));
         buffer.append(" local changes. <br>");
 
         buffer.append(makeLocalLink(context, "fniki/changelog", "confirm", "Show"));
         buffer.append(" change history for this version. <br>");
 
         buffer.append(makeLocalLink(context, "fniki/getversions", "confirm", "Discover"));
         buffer.append(" other recent version.<br>");
 
         buffer.append(makeLocalLink(context, "fniki/config", "view", "View"));
         buffer.append(" configuration.<br>");
 
         buffer.append("</body></html>");
     }
 
     private String getEditorHtml(WikiContext context, String name) throws IOException {
         StringBuilder buffer = new StringBuilder();
         addHeader(name, buffer);
 
         String href = makeHref(context.makeLink("/" +name),
                                "save", null, null, null);
 
 
         buffer.append("<form method=\"post\" action=\"" +
                       href +
                       "\" enctype=\"");
 
         // IMPORTANT: Only multipart/form-data encoding works in plugins.
         // IMPORTANT: Must be multipart/form-date even for standalone because
         //            the Freenet ContentFilter rewrites the encoding in all forms
         //            to this value.
         buffer.append("multipart/form-data");
         buffer.append("\" accept-charset=\"UTF-8\">\n");
 
         buffer.append("<input type=hidden name=\"savepage\" value=\"");
                                buffer.append(name); // DCI: percent escape? Ok for now because of name checks
         buffer.append("\">\n");
 
         buffer.append("<textarea wrap=\"virtual\" name=\"savetext\" rows=\"17\" cols=\"120\">\n");
 
         if (context.getStorage().hasPage(name)) {
            buffer.append(escapeHTML(context.getStorage().getPage(name)));
         } else {
            buffer.append(escapeHTML("Page doesn't exist in the wiki yet."));
         }
 
         buffer.append("</textarea>\n");
         buffer.append("<br><input type=submit value=\"Save\">\n");
         buffer.append("<input type=hidden name=formPassword value=\"");
 
         // IMPORTANT: Required by Freenet Plugin.
         buffer.append(context.getString("form_password", "FORM_PASSWORD_NOT_SET")); // DCI: % encode?
         buffer.append("\"/>\n");
         buffer.append("<input type=reset value=\"Reset\">\n");
         buffer.append("<br></form>");
 
         buffer.append("<hr>\n");
         buffer.append("</body></html>\n");
 
         return buffer.toString();
     }
 
     public String renderXHTML(WikiContext context, String wikiText) {
         return new FreenetWikiTextParser(wikiText, context.getParserDelegate()).toString();
     }
 }
