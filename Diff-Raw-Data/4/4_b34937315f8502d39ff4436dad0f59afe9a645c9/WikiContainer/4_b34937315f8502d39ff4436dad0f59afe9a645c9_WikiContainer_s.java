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
 import wormarc.IOUtil;
 
 import fniki.wiki.FreenetWikiTextParser;
 import static fniki.wiki.HtmlUtils.*;
 import fniki.wiki.RebaseStatus;
 import static fniki.wiki.Validations.*;
 import fniki.wiki.WikiApp;
 import fniki.wiki.WikiTextChanges;
 import fniki.wiki.WikiContext;
 import fniki.wiki.WikiTextStorage;
 
 public class WikiContainer implements ChildContainer {
     private final static String ENCODING = "UTF-8";
     private boolean mCreateOuterHtml;
 
     public WikiContainer(boolean createOuterHtml) {
         mCreateOuterHtml = createOuterHtml;
     }
 
     public String handle(WikiContext context) throws ChildContainerException {
         try {
             String action = context.getAction();
             if (action.equals("finished")) {
                 // Hack: Ignore "finished".
                 // This happens when the user hits the back button and picks
                 // a link from a finished task page. e.g. changelog.
                 action = "view";
             }
             // Convert spaces to '_' so you can type titles with spaces into
             // the "Goto or Create Page" box.
             String title = context.getTitle().trim().replace(" ", "_");
             if (!isAlphaNumOrUnder(title)) {
                 // Titles must be legal page names.
                 context.raiseAccessDenied("Couldn't work out query.");
             }
 
             if (!isLowerCaseAlpha(action)) {
                 // Illegal action
                 context.raiseAccessDenied("Couldn't work out query.");
             }
 
             Query query = context.getQuery();
 
             if (action.equals("view") ||       // editable
                 action.equals("viewparent") || // view parent version read only
                 action.equals("viewrebase")) { // view rebase version read only
                 return handleView(context, title, action);
             } else if (action.equals("edit")) {
                 return handleEdit(context, title);
             } else if (action.equals("delete")) {
                 return handleDelete(context, title);
             } else if (action.equals("revert")) {
                 return handleRevert(context, title);
             } else if (action.equals("rebased")) {
                 return handleRebase(context, title);
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
 
     private String handleView(WikiContext context, String name, String action) throws IOException {
         return getPageHtml(context, name, action);
     }
 
     private String handleEdit(WikiContext context, String name) throws IOException {
         return getEditorHtml(context, name);
     }
 
     private String handleDelete(WikiContext context, String name) throws IOException {
         if (context.getStorage().hasPage(name)) {
             context.getStorage().deletePage(name);
         }
 
         // LATER: do better.
         return getPageHtml(context, name, "view");
     }
 
     private String handleRevert(WikiContext context, String name) throws ChildContainerException, IOException {
         context.getStorage().revertLocalChange(name);
         context.raiseRedirect(context.makeLink("/" + name), "Redirecting...");
         return "unreachable code";
     }
 
     private String handleRebase(WikiContext context, String name) throws ChildContainerException, IOException {
         if (context.getRemoteChanges().hasChange(name)) {
             if (context.getRemoteChanges().wasDeleted(name)) {
                 // Delete from the working version.
                 context.getStorage().deletePage(name);
             } else {
                 // Overwrite the working version with the rebase version.
                 context.getStorage().putPage(name, context.getRemoteChanges().getPage(name));
             }
         }
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
 
     private String unescapedTitleFromName(String name) {
         if (name.startsWith("Talk_")) {
             // LATER: Localization.
             name = "Talk:" + name.substring("Talk_".length());
         }
         return name.replace("_", " ");
     }
 
     private String getTalkPage(WikiContext context, String name) throws IOException {
         if (name.startsWith("Talk_") || (!context.getStorage().hasPage(name))) {
             return null;
         }
         // LATER: Localization
         return "Talk_" + name;
     }
 
     private void addHtmlForNonExistantPage(WikiContext context, String name,
                                            StringBuilder buffer) throws IOException {
 
         // Hmmmm... too branchy
         if (name.equals(context.getString("default_page", "Front_Page"))) {
             buffer.append(renderXHTML(context,
                                       context.getString("default_wikitext",
                                                         "Page doesn't exist in the wiki yet.")));
         } else {
             if (name.startsWith("Talk_")) {
                 if (context.getStorage().hasPage("TalkPageDoesNotExist")) {
                     // LATER: Revisit. Also, ExternalLink. Evil submissions can change this to something confusing.
                     buffer.append(renderXHTML(context, context.getStorage().getPage("TalkPageDoesNotExist")));
                 } else {
                     buffer.append("Discussion page doesn't exist in the wiki yet.");
                 }
             } else {
                 if (context.getStorage().hasPage("PageDoesNotExist")) {
                     // LATER: as above.
                     buffer.append(renderXHTML(context, context.getStorage().getPage("PageDoesNotExist")));
                 } else {
                     buffer.append("Page doesn't exist in the wiki yet.");
                 }
             }
         }
     }
 
     private static String getPageWikiText(WikiContext context, String name, String action) throws IOException {
         if (action.equals("view")) {
             return context.getStorage().getPage(name);
         } else if (action.equals("viewparent")) {
             return context.getStorage().getUnmodifiedPage(name);
         } else if (action.equals("viewrebase")) {
             if (context.getRemoteChanges().wasDeleted(name)) {
                 return "Page doesn't exist in the rebase version.";
             } else {
                 return context.getRemoteChanges().getPage(name);
             }
         } else {
             throw new RuntimeException("Unhandled action: " + action);
         }
     }
 
     private static String titlePrefix(String action) {
         if (action.equals("viewrebase")) {
             return "{Rebase Version}:";
         } else if (action.equals("viewParent")) {
             return "{Parent Version}:";
         }
         return "";
     }
 
     private String getPageHtml(WikiContext context, String name, String action) throws IOException {
         StringBuilder buffer = new StringBuilder();
         String escapedName = escapeHTML(titlePrefix(action) + unescapedTitleFromName(name));
         addHeader(context, escapedName, getTalkPage(context, name), buffer);
 
         if ((action.equals("view") && context.getStorage().hasPage(name)) ||
             (action.equals("viewparent") && context.getStorage().hasUnmodifiedPage(name)) ||
             (action.equals("viewrebase") && context.getRemoteChanges().hasChange(name))) {
             buffer.append(renderXHTML(context, getPageWikiText(context, name, action)));
         } else {
             addHtmlForNonExistantPage(context, name, buffer);
         }
         addFooter(context, name, !action.equals("view"), buffer);
         return buffer.toString();
     }
 
     private void addHeader(WikiContext context, String escapedName, String talkName,
                            StringBuilder buffer) throws IOException {
     	if(mCreateOuterHtml) {
 	        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 	        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
 	                      "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
 	        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
 	        buffer.append("<head><title>\n");
 	        buffer.append(escapedName);
 	        buffer.append("</title>\n");
 	        buffer.append("<style type=\"text/css\">\n");
 	        // CAREFUL: MUST audit .css files built into .jar to make sure they are safe.
 	        // Load .css snippet from jar. Names can only have 1 '/' and must be globally unique.
 	        buffer.append(context.getString("/add_header.css", ""));
 	        buffer.append("</style>\n");
 	        buffer.append("</head>\n");
 	        buffer.append("<body>\n");
     	}
         buffer.append("<h1 class=\"pagetitle\">\n");
         buffer.append(escapedName);
         buffer.append("</h1>\n");
         if (talkName != null) {
             String talkClass = context.getStorage().hasPage(talkName) ? "talktitle" : "notalktitle";
             buffer.append(String.format("<h4 class=\"%s\">\n", talkClass));
             String href = makeHref(context.makeLink("/" + talkName), null, talkName, null, null, null);
             buffer.append(String.format("<a class=\"%s\" href=\"%s\">%s</a>",
                                         talkClass, href, escapeHTML("Discussion")));
             buffer.append("</h4>\n");
         }
         buffer.append("</h1><hr>\n");
     }
 
     private String makeLocalLink(WikiContext context, String name, String action, String label) {
         String href = makeHref(context.makeLink("/" + name), action, name, null, null, null);
         return String.format("<a href=\"%s\">%s</a>", href, escapeHTML(label));
     }
 
     private final static String kindToString(int kind) {
         switch(kind) {
         case RebaseStatus.PARENT: return "parent";
         case RebaseStatus.REBASE: return "rebase";
         // LATER: also "locally added"
         case RebaseStatus.LOCALLY_MODIFIED: return "locally modified";
         }
         return "???";
     }
 
     private void addStatusInfo(WikiContext context, String name, int kind,
                                 StringBuilder buffer) throws IOException {
         buffer.append("<table border=\"1\">\n");
         buffer.append(String.format("<tr><th align=\"left\">Page:</th><td>%s</td>" +
                                     "<th align=\"left\">Origin:</th><td>%s</td></tr>\n",
                                     name, kindToString(kind)));
         buffer.append("<tr><th align=\"left\">Parent:</th><td>");
         // DCI: css class to make this smaller.
         String version = getVersionHex(context.getString("parent_uri", null));
         buffer.append(escapeHTML(version));
         buffer.append("</td</tr>\n");
         String secondaryUri = context.getString("secondary_uri", null);
         if (secondaryUri != null) {
             buffer.append("<tr><th align=\"left\">Rebase:</th><td>");
             buffer.append(escapeHTML(getVersionHex(secondaryUri)));
             buffer.append("</td></tr>\n");
         }
         // Same row
         buffer.append(String.format("<tr><th align=\"left\">Wiki Name:</th><td>%s</td>",
                                     context.getString("wikiname", "???")));
         buffer.append(String.format("<th align=\"left\">Board:</th><td>%s</td></tr>",
                                     context.getString("fms_group", "???"))); // LATER: fix fms_group
 
         buffer.append("</table>\n");
         buffer.append("<p/>\n");
     }
 
     private void addDynamicLinks(WikiContext context, String name,
                                  int kind, boolean readOnly,
                                  StringBuilder buffer) throws IOException {
         boolean hasLocalChanges = context.getStorage().hasLocalChange(name);
         boolean hasRemoteChanges = context.getRemoteChanges().hasChange(name);
         boolean showRevert = hasLocalChanges;
         boolean showRevertToRemote = hasRemoteChanges;
 
         boolean existsInParent = context.getStorage().hasUnmodifiedPage(name);
         boolean existsInRemote = context.getRemoteChanges().hasChange(name) && !context.getRemoteChanges().wasDeleted(name);
 
         if (kind == RebaseStatus.PARENT) { showRevert = false; }
         if (kind == RebaseStatus.REBASE) { showRevertToRemote = false; }
 
         if (!readOnly) {
             buffer.append(makeLocalLink(context, name, "edit", "Edit"));
             buffer.append(" this page. (shows diffs) <br>");
             buffer.append(makeLocalLink(context, name, "delete", "Delete"));
             buffer.append(" this page without confirmation!<br>");
             if (showRevert) {
                 buffer.append(makeLocalLink(context, name, "revert", "Revert"));
                 buffer.append(" to the parent version of this page, without confirmation!");
                 if (!existsInParent) {
                     buffer.append("<em>Will delete page</em>");
                 }
                 buffer.append("<br>\n");
             }
 
             if (showRevertToRemote) {
                 buffer.append(makeLocalLink(context, name, "rebased", "Replace"));
                 buffer.append(" this page with the rebase version,  without confirmation!");
                 if (!existsInRemote) {
                     buffer.append("<em>Will delete page.</em>");
                 }
                 buffer.append("<br>\n");
             }
         }
 
         if (hasLocalChanges && existsInParent && !readOnly) {
             buffer.append(makeLocalLink(context, name, "viewparent", "View Parent Version"));
             buffer.append("<br/>\n");
         }
 
         if (hasRemoteChanges && existsInRemote && !readOnly) {
             buffer.append(makeLocalLink(context, name, "viewrebase", "View Rebase Version"));
             buffer.append("<br/>\n");
         }
         if (readOnly) {
             buffer.append(makeLocalLink(context, name, "view", "Goto Editable Version"));
             buffer.append("<br/>\n");
         }
 
     }
 
     private void addStaticLinks(WikiContext context, String name,
                                 StringBuilder buffer) throws IOException {
 
         buffer.append(makeLocalLink(context, "fniki/submit", null, "Submit"));
         buffer.append(" local changes. <br>");
 
         buffer.append(makeLocalLink(context, "fniki/changelog", "confirm", "Show"));
         buffer.append(" change history for this version. <br>");
 
         buffer.append(makeLocalLink(context, "fniki/getversions", "confirm", "Discover"));
         buffer.append(" other recent version of this wiki.<br/>");
 
         buffer.append(makeLocalLink(context, "fniki/config", "view", "View"));
         buffer.append(" configuration.<p/>\n");
         buffer.append(gotoPageFormHtml(context.makeLink("/" + name),
                                        context.getString("default_page", "Front_Page")));
 
         buffer.append("<hr>\n");
         // LATER: Quick hack. Clean this up. Use CSS instead of table
         buffer.append(String.format("<p><form method=\"get\" action=\"%s\" accept-charset=\"UTF-8\">\n",
                                     context.makeLink("/fniki/loadarchive"), null, null, null, null));
         buffer.append("   <table><tr>\n");
         buffer.append("   <td><input type=submit value=\"Load Archive\"/></td>\n");
         buffer.append("   <td><input style=\"font-size:60%;\" type=text name=\"uri\" size=\"140\" value=\"\"/></td></tr>\n");
         // TRICKY: You only see the default check state change on initial load.
         buffer.append("   <tr><td><input type=\"radio\" name=\"secondary\" value=\"false\" checked />primary</td>\n");
         buffer.append("   <td><input type=\"radio\" name=\"secondary\" value=\"true\" />rebase</td>\n");
         buffer.append("   </tr></table>\n");
         buffer.append("<hr>\n");
         buffer.append(makeLocalLink(context, "fniki/resettoempty", "view", "Create Wiki!"));
         buffer.append(" (<em>careful:</em> This deletes all content and history without confirmation.)<p/>\n");
         buffer.append("<hr>\n");
 
         buffer.append(makeLocalLink(context, "fniki/insertsite", "view", "Insert"));
         buffer.append(" a static version of this wiki as a freesite.<p/>\n");
   }
 
     private void addFooter(WikiContext context, String name, boolean readOnly, StringBuilder buffer) throws IOException {
         buffer.append("<hr>\n");
 
         int kind = RebaseStatus.pageChangeKind(context.getStorage(), context.getRemoteChanges(), name);
 
         if (!readOnly) {
             addStatusInfo(context, name, kind, buffer);
         }
 
         addDynamicLinks(context, name, kind, readOnly, buffer);
 
         if (!readOnly) {
             addStaticLinks(context, name, buffer);
         }
 
         buffer.append("</form>\n");
         if(mCreateOuterHtml) {
             buffer.append("</body></html>\n");
         }
     }
 
     private static String nullToNone(String value) {
         if (value == null) { return "none"; }
         return value;
     }
 
     private String getEditorHtml(WikiContext context, String name) throws IOException {
         String template = null;
         try {
             // NOTE: There is CSS in this template. Keep it in sync with the add_header.css file.
             //
             // IMPORTANT: Only multipart/form-data encoding works in plugins.
             // IMPORTANT: Must be multipart/form-data even for standalone because
             //            the Freenet ContentFilter rewrites the encoding in all forms
             //            to this value.
             template = IOUtil.readUtf8StringAndClose(SettingConfig.class.getResourceAsStream("/edit_form.html"));
         } catch (IOException ioe) {
             return "Couldn't load edit_form.html template from jar???";
         }
 
         String escapedName = escapeHTML(unescapedTitleFromName(name));
         String href = makeHref(context.makeLink("/" +name),
                                "save", null, null, null, null);
         String wikiText = "Page doesn't exist in the wiki yet.";
         if (context.getStorage().hasPage(name)) {
             wikiText = context.getStorage().getPage(name);
         }
 
         String parentWikiText = null;
         if (context.getStorage().hasUnmodifiedPage(name)) {
             parentWikiText = context.getStorage().getUnmodifiedPage(name);
         }
         String rebaseWikiText = null;
 
         if (context.getRemoteChanges().hasChange(name) &&
             !context.getRemoteChanges().wasDeleted(name)) {
             rebaseWikiText = context.getRemoteChanges().getPage(name);
         }
 
         // Escaping should be OK.
         String diffsFromParentToLocal = getDiffHtml(parentWikiText, wikiText);
         String diffsFromParentToRebase = getDiffHtml(parentWikiText, rebaseWikiText);
 
         parentWikiText = nullToNone(parentWikiText);
         rebaseWikiText = nullToNone(rebaseWikiText);
 
         return String.format(template,
                              escapedName,
                              escapedName,
                              href,
                              escapeHTML(name), // i.e. with '_' chars
                              escapeHTML(wikiText),
                              // IMPORTANT: Required by Freenet Plugin.
                              // Doesn't need escaping.
                              context.getString("form_password", "FORM_PASSWORD_NOT_SET"),
                              diffsFromParentToLocal,
                              diffsFromParentToRebase,
                              escapeHTML(parentWikiText),
                              escapeHTML(rebaseWikiText));
     }
 
     public String renderXHTML(WikiContext context, String wikiText) {
         return new FreenetWikiTextParser(wikiText, context.getParserDelegate()).toString();
     }
 
     public String renderExternalWikiText(WikiContext context, String title, String path, String wikiText) throws IOException {
         StringBuilder buffer = new StringBuilder();
         String escapedName = escapeHTML(title);
         addHeader(context, escapedName, null, buffer);
         buffer.append(renderXHTML(context, wikiText));
 
         // Custom footer.
         buffer.append("<hr>\n");
         buffer.append("<em> You can't edit this page because it is built into the jar.</em> <p/>\n");
         buffer.append("");
         buffer.append(makeLocalLink(context, path, "viewsrc", "View Wikitext Source"));
         buffer.append("</form>\n");
        buffer.append("</body></html>\n");
 
         return buffer.toString();
     }
 
 }
