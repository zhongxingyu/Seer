 /* A UI subcomponent to load a list of other versions of this wiki via FMS.
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
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static ys.wikiparser.Utils.*;
 
 import fmsutil.FMSUtil;
 import wormarc.ExternalRefs;
 import wormarc.FileManifest;
 
 import fniki.wiki.ArchiveManager;
 import fniki.wiki.ChildContainer;
 import fniki.wiki.ChildContainerException;
 import fniki.wiki.GraphLog;
 import static fniki.wiki.HtmlUtils.*;
 import fniki.wiki.WikiContext;
 
 public class LoadingVersionList extends AsyncTaskContainer {
     private StringBuilder mListHtml = new StringBuilder();
     private String mName = "";
     private String mContainerPrefix;
     public LoadingVersionList(ArchiveManager archiveManager) {
         super(archiveManager);
     }
 
     public synchronized String getListHtml() {
         return mListHtml.toString();
     }
 
     public String handle(WikiContext context) throws ChildContainerException {
         try {
             if (context.getAction().equals("confirm")) {
                 // Copy stuff we need out because context isn't threadsafe.
                 mName = context.getPath();
                 mContainerPrefix = context.getString("container_prefix", null);
                 if (mContainerPrefix == null) {
                     throw new RuntimeException("Assertion Failure: mContainerPrefix == null");
                 }
                 startTask();
                 try {
                     Thread.sleep(1000); // Hack. Give task thread a chance to finish.
                 } catch (InterruptedException ioe) {
                     /* NOP */
                 }
                 sendRedirect(context, context.getPath());
                 return "unreachable code";
             }
 
             boolean showBuffer = false;
             String confirmTitle = null;
             String cancelTitle = null;
             String title = null;
             switch (getState()) {
             case STATE_WORKING:
                 showBuffer = true;
                 title = "Loading Wiki Version Info from FMS";
                 cancelTitle = "Cancel";
                 break;
             case STATE_WAITING:
                 // Shouldn't hit this state.
                 showBuffer = false;
                 title = "Load Wiki Version Info from FMS";
                 confirmTitle = "Load";
                 cancelTitle = "Cancel";
                 break;
             case STATE_SUCCEEDED:
                 showBuffer = true;
                 title = "Loaded Wiki Version Info from FMS";
                 confirmTitle = null;
                 cancelTitle = "Done";
                 break;
             case STATE_FAILED:
                 showBuffer = true;
                 title = "Full Read of Wiki Version Info Failed";
                 confirmTitle = "Reload";
                 cancelTitle = "Done";
                 break;
             }
 
             StringWriter buffer = new StringWriter();
             PrintWriter body = new PrintWriter(buffer);
             body.println("<html><head>\n");
             body.println(metaRefresh());
             body.println("<style type=\"text/css\">\n");
             body.println("TD{font-family: Arial; font-size: 7pt;}\n");
             body.println("</style>\n");
             body.println("<title>" + escapeHTML(title) + "</title>\n");
             body.println("</head><body>\n");
 
             body.println("<h3>" + escapeHTML(title) + "</h3>");
             body.println(String.format("wikiname:%s<br>FMS group:%s<p>",
                                     escapeHTML(context.getString("wikiname", "NOT_SET")),
                                     escapeHTML(context.getString("fms_group", "NOT_SET"))));
 
             if (showBuffer) {
                 body.println(getListHtml());
                 body.println("<hr>");
                 body.println("<pre>");
                 body.print(escapeHTML(getOutput()));
                 body.println("</pre>");
             }
             body.println("<hr>");
             addButtonsHtml(context, body, confirmTitle, cancelTitle);
             body.println("</body></html>");
             body.close();
             return buffer.toString();
         } catch (IOException ioe) {
             context.logError("LoadingVersionList", ioe);
             return "Error LoadingVersionList";
         }
     }
 
     // Doesn't need escaping.
     public static String trustString(int value) {
         if (value == -1) {
             return "null";
         }
         return Integer.toString(value);
     }
 
     public static String getParentVersion(FMSUtil.BISSRecord record) {
         if (record.mKey == null) {
             return "???";
         }
         String[] fields = record.mKey.split("/");
         if (fields.length != 2) {
             return "???";
         }
 
         fields = fields[1].split("_");
        if (fields.length != 2) { // LATER. handle multiple parents
             if (fields.length == 1 && fields[0].length() == 16) {
                 // Assume the entry is the first version.
                 return "0000000000000000";
             }
 
             return "???";
         }
 
        return fields[1];
     }
 
     final static class DAGData implements Comparable<DAGData> {
         public final int mSize;
         public final long mEpochMs;
         public final List<GraphLog.DAGNode> mDag;
         DAGData(int size, long epochMs, List<GraphLog.DAGNode> dag) {
             mSize = size;
             mEpochMs = epochMs;
             mDag = dag;
         }
 
         public int compareTo(DAGData o) { // DCI: test!
             if (o == null) { throw new NullPointerException(); }
             if (o == this) { return 0; }
             if (o.mSize - mSize != 0) { // first by descending size.
                 return o.mSize - mSize;
             }
             if (o.mEpochMs - mEpochMs != 0) { // then by descending date.
                 return (o.mEpochMs - mEpochMs) > 0 ? 1: -1;
             }
             return 0;
         }
 
         // Hmmmm... not sure these are required.
         public boolean equals(Object obj) {
             if (obj == this) { return true; }
 
             if (obj == null || (!(obj instanceof DAGData))) {
                 return false;
             }
 
             DAGData other = (DAGData)obj;
             return mSize == other.mSize &&
                 mEpochMs == other.mEpochMs &&
                 mDag.equals(other.mDag);
         }
 
         public int hashCode() {
             int result = 17;
             result = 37 * mSize;
             result = 37 * (int)(mEpochMs ^ (mEpochMs >>> 32));
             result = 37 * mDag.hashCode();
             return result;
         }
     }
 
     // Wed, 02 Mar 11 02:57:38 -0000
     private final static DateFormat sDateFormat = new java.text.SimpleDateFormat("EEE, dd MMM yy HH:mm:ssZ");
     private static void sortBySizeAndDate(List<List<GraphLog.DAGNode>> dags, Map<String, List<FMSUtil.BISSRecord>> lut) {
         List<DAGData> dagData = new ArrayList<DAGData>();
         for (List<GraphLog.DAGNode> dag : dags) {
             long epochMs = 0;
             for (GraphLog.DAGNode node : dag) {
                 for (FMSUtil.BISSRecord record : lut.get(node.mTag)) {
                     try {
                         long zuluMs = sDateFormat.parse(record.mDate).getTime();
                         if (zuluMs > epochMs) {
                             epochMs = zuluMs;
                         }
                     } catch (ParseException pe) {
                         System.err.println("Parse of date failed: " + record.mDate);
                     }
                 }
             }
             dagData.add(new DAGData(dag.size(), epochMs, dag));
         }
         Collections.sort(dagData);
         dags.clear();
         for (DAGData data : dagData) {
             dags.add(data.mDag);
         }
     }
 
     public synchronized String getRevisionGraphHtml(List<FMSUtil.BISSRecord> records)
         throws IOException {
 
         // Build a list of revision graph edges from the NNTP notification records.
         List<GraphLog.GraphEdge> edges = new ArrayList<GraphLog.GraphEdge>();
         Map<String, List<FMSUtil.BISSRecord>> lut = new HashMap<String, List<FMSUtil.BISSRecord>>();
         for (FMSUtil.BISSRecord record : records) {
             String child = getVersionHex(record.mKey);
             String parent = getParentVersion(record);
             if (child.equals("???") || parent.equals("???")) {
                 System.err.println(String.format("Skipping: (%s, %s)", child, parent));
                 System.err.println("  " + record.mKey);
                 continue;
             }
 
             if (child.equals("0000000000000000")) { // DCI: srsly? use constant.
                 System.err.println(String.format("Attempted attack? Skipping: (%s, %s)", child, parent));
                 System.err.println("  " + record.mKey);
                 continue;
             }
 
             List<FMSUtil.BISSRecord> recordsEntry = lut.get(child);
             if (recordsEntry == null) {
                 recordsEntry = new ArrayList<FMSUtil.BISSRecord>();
                 lut.put(child, recordsEntry);
             }
             if (!lut.get(child).contains(record)) {
                 lut.get(child).add(record);
             }
             GraphLog.GraphEdge edge = new GraphLog.GraphEdge(parent, child); // DCI: DOCUMENT ORDER
             if (!edges.contains(edge)) { // hmmmm.... O(n) search.
                 edges.add(edge);
             }
         }
 
         // Passing "0000000000000000" keep the drawing code from drawing '|'
         // below root nodes.
         List<List<GraphLog.DAGNode>> dags = GraphLog.build_dags(edges, "0000000000000000");
         sortBySizeAndDate(dags, lut);
 
         // Draw the revision graph(s).
         StringWriter out = new StringWriter();
         out.write("<pre>\n");
         for (List<GraphLog.DAGNode> dag : dags) {
             out.write("<hr>\n");
             List<Integer> seen = new ArrayList<Integer>();
             GraphLog.AsciiState state = GraphLog.asciistate();
             for (GraphLog.DAGNode value : dag) {
                 List<FMSUtil.BISSRecord> references = lut.get(value.mTag);
 
                 List<String> lines = new ArrayList<String>();
                 String versionLink = getShortVersionLink(mContainerPrefix, "/jfniki/loadarchive",
                                                          references.get(0).mKey); // All the same.
 
                 String rebaseLink = getRebaseLink(mContainerPrefix, "/jfniki/loadarchive",
                                                   references.get(0).mKey, "finished",
                                                   "[rebase]", false);
 
                 lines.add(versionLink + " " + rebaseLink);
 
                 for (FMSUtil.BISSRecord reference : references) {
                     // DCI: Sort by date
                     lines.add(String.format("user: %s (%s, %s, %s, %s)",
                                             reference.mFmsId,
                                             trustString(reference.msgTrust()),
                                             trustString(reference.trustListTrust()),
                                             trustString(reference.peerMsgTrust()),
                                             trustString(reference.peerTrustListTrust())
                                             ));
                     lines.add(String.format("date: %s", reference.mDate)); // Reliable?
                 }
                 lines.add("");
 
                 GraphLog.ascii(out, state, "o", lines, GraphLog.asciiedges(seen, value.mId, value.mParentIds));
             }
         }
         out.write("</pre>\n");
         out.flush();
         return out.toString();
     }
 
     public boolean doWork(PrintStream out) throws Exception {
         synchronized (this) {
             mListHtml = new StringBuilder(); // DCI: why list html?
         }
         try {
             out.println("Reading versions from FMS...");
             String graphHtml = getRevisionGraphHtml(mArchiveManager.getRecentWikiVersions(out));
             synchronized (this) {
                 mListHtml.append(graphHtml);
             }
             return true;
         } catch (IOException ioe) {
             out.println("Error reading log: " + ioe.getMessage());
             return false;
         }
     }
 }
