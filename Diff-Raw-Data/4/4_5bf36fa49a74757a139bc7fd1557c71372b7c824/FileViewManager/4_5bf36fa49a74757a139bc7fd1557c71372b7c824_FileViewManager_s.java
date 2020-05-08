 package org.makumba.parade.view.managers;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowWebapp;
 import org.makumba.parade.model.managers.ServletContainer;
 import org.makumba.parade.tools.DisplayFormatter;
 import org.makumba.parade.view.interfaces.FileView;
 import org.makumba.parade.view.interfaces.TreeView;
 
 public class FileViewManager implements FileView, TreeView {
 
     public String getFileViewHeader(Row r, String path) {
         String pathURI = "";
         if (path == null)
             path = r.getRowpath();
 
         try {
             pathURI = URLEncoder.encode(path, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         String header = "<th></th>"
                 + // type
                 // "<th>Name</th>" +
                 "<th colspan='2'>"
                 + "<a href='/File.do?display=command&view=newDir&context="
                 + r.getRowname()
                 + "&path="
                 + path
                 + "' target='command' title='Create a new directory'><img src='/images/newfolder.gif' align='right'></a>"
                 + "<a href='/uploadFile.jsp&context=" + r.getRowname() + "&path=" + path
                 + "' target='command' title='Upload a file'><img src='/images/uploadfile.gif' align='right'></a> "
                 + "<a href='/File.do?display=command&view=newFile&context=" + r.getRowname() + "&path=" + path
                 + "' target='command' title='Create a new file'><img src='/images/newfile.gif' align='right'></a> "
                 + "Name</th>" + "<th>Age</th>" + "<th>Size</th>" +
 
                 "<script language=\"JavaScript\">\n" + "<!-- \n" + "function deleteFile(path, name) {\n"
                 + "  if(confirm('Are you sure you want to delete the file '+name+' ?'))\n" + "  {\n"
                 + "	url='/File.do?display=file&context=" + r.getRowname() + "&path=" + pathURI
                 + "&op=deleteFile&params='+encodeURIComponent(path);\n" + "	location.href=url;\n" + "  }\n" + "}\n"
                 + "</script>\n";
 
         return header;
     }
 
     public String getFileView(Row r, String path, File f) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         RowWebapp webappdata = (RowWebapp) r.getRowdata().get("webapp");
         // out.print("<td>");
         if (f.getIsDir()) {
             out.print("<td><img src='/images/folder.gif'></td>"
                     + "<td colspan='2'><a href='/File.do?browse&display=file&context=" + r.getRowname() + "&path="
                     + f.getPath() + "'>" + f.getName() + "</a></td>");
         } else {
 
             // icons
             String fl = f.getName().toLowerCase();
             String image = "unknown";
 
             if (fl.endsWith(".java"))
                 image = "java";
             if (fl.endsWith(".mdd") || fl.endsWith(".idd"))
                 image = "text";
             if (fl.endsWith(".jsp") || fl.endsWith(".properties") || fl.endsWith(".xml") || fl.endsWith(".txt")
                     || fl.endsWith(".conf"))
                 image = "text";
             if (fl.endsWith(".doc") || fl.endsWith(".jsp") || fl.endsWith(".html") || fl.endsWith(".htm")
                     || fl.endsWith(".rtf"))
                 image = "layout";
             if (fl.endsWith(".gif") || fl.endsWith(".png") || fl.endsWith(".jpg") || fl.endsWith(".jpeg"))
                 image = "image";
             if (fl.endsWith(".zip") || fl.endsWith(".gz") || fl.endsWith(".tgz") || fl.endsWith(".jar"))
                 image = "zip";
             if (fl.endsWith(".avi") || fl.endsWith(".mpg") || fl.endsWith(".mpeg") || fl.endsWith(".mov"))
                 image = "movie";
             if (fl.endsWith(".au") || fl.endsWith(".mid") || fl.endsWith(".vaw") || fl.endsWith(".mp3"))
                 image = "sound";
 
             out.print("<td><img src='/images/" + image + ".gif'></td>");
 
             // name
             String addr = "";
             String webappPath = webappdata.getWebappPath();
 
             if (webappdata.getStatus().intValue() == ServletContainer.RUNNING
                     && path.startsWith(java.io.File.separator + webappPath)) {
                String pathURI = path.substring(path.indexOf(webappPath) + webappPath.length()).replaceAll(
                        java.io.File.separator, "/")
                         + "/";
 
                 if (fl.endsWith(".java")) {
                     String dd = pathURI + f.getName();
                     dd = dd.substring(dd.indexOf("classes") + 8, dd.lastIndexOf(".")).replace('/', '.');
                     addr = "/" + r.getRowname() + "/classes/" + dd;
                 }
                 if (fl.endsWith(".mdd") || fl.endsWith(".idd")) {
                     String dd = pathURI + f.getName();
                     dd = dd.substring(dd.indexOf("dataDefinitions") + 16, dd.lastIndexOf(".")).replace('/', '.');
                     addr = "/" + r.getRowname() + "/dataDefinitions/" + dd;
                 }
                 if (fl.endsWith(".jsp") || fl.endsWith(".html") || fl.endsWith(".htm") || fl.endsWith(".txt")
                         || fl.endsWith(".gif") || fl.endsWith(".png") || fl.endsWith(".jpeg") || fl.endsWith(".jpg")
                         || fl.endsWith(".css") || fl.startsWith("readme"))
                     addr = "/" + r.getRowname() + pathURI + f.getName();
                 if (fl.endsWith(".jsp"))
                     addr += "x";
             }
 
             out.print("<td>");
             if (!addr.equals("")) {
                 out.print("<a href='" + addr + "'>" + f.getName() + "</a>");
             } else {
                 out.print(f.getName());
             }
             out.print("</td><td align='right'>");
             // actions
             try {
                 out.print("<a href='/File.do?op=editFile&context=" + r.getRowname() + "&path=" + path + "&file="
                         + f.getPath() + "'><img src='/images/edit.gif' alt='Edit " + f.getName() + "'></a>"
                         + "<a href=\"javascript:deleteFile('"
                         + URLEncoder.encode(URLEncoder.encode(f.getPath(), "UTF-8"), "UTF-8") + "','" + f.getName()
                         + "')\"><img src='/images/delete.gif' alt='Delete " + f.getName() + "'></a>");
             } catch (UnsupportedEncodingException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             out.print("</td>");
 
         }
 
         // time && size
         out.print("<td><a title='" + new java.util.Date(f.getDate().longValue()) + "'>"
                 + DisplayFormatter.readableTime(f.getAge().longValue()) + "</a></td>");
         if (!f.getIsDir()) {
             if (f.getSize().longValue() > 0l)
                 out.print("<td><a title='" + f.getSize() + " bytes'>"
                         + DisplayFormatter.readableBytes(f.getSize().longValue()) + "</a></td>");
             else
                 out.print("<td><i>empty<i></td>");
         } else {
             out.print("</td><td>");
         }
 
         return result.toString();
     }
 
     public String getTreeView(Parade p, Row r) {
 
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         out.println("<HTML><HEAD><TITLE>" + r.getRowname() + " tree</TITLE>" + "</HEAD><BODY>");
 
         File baseFile = (File) r.getFiles().get(r.getRowpath());
         List dirs = baseFile.getSubdirs();
 
         for (Iterator i = dirs.iterator(); i.hasNext();) {
             String curDir = (String) i.next();
             out
                     .println("<b><a href='/File.do?display=file?context=" + r.getRowname() + "&path=" + curDir
                             + "' target='directory'>" + curDir.substring(r.getRowpath().length(), curDir.length())
                             + "</a><br>");
         }
 
         out.println("</BODY></HTML>");
 
         return result.toString();
     }
 
     public String getJSTreeView(Parade p, Row r, String s) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         // defaults:
         /*
          * String size="normal"; String fontSize="0.7em";
          */
         String imagePath = "imagesCompact";
         /*
          * if(s != null) size = s; if(size.toLowerCase().equals("big")) { fontSize="1em"; imagePath="images"; }
          */
         out.println("<html><head><title>" + r.getRowname() + " tree</title> \n");
         out.println("<link rel='StyleSheet' href='/style/parade.css' type='text/css'>");
         out.println("<link rel='StyleSheet' href='/style/tree.css' type='text/css'>");
         /*
          * out.println("<style type=\"text/css\">"); out.println( "A {\n"+ " font-size:"+fontSize+";\n"+ "}\n"+ "</style>\n"+
          */
         out.println("</head>\n" + "<body class='tree'>\n");
 
         out.println("<script src=\"/treeMenu/sniffer.js\"></script>\n"
                 + "<script src=\"/treeMenu/TreeMenu.js\"></script>\n" + "<div id=\"menuLayer" + r.getRowname()
                 + "\"></div>\n");
 
         /*
          * if(size.equals("normal")) { out.println("<a href='?context="+r.getRowname()+"&size=big' title='Show
          * bigger'>\n"+ "<img src='/images/magnify.gif' align='right'></a>\n"); } else { out.println("<a
          * href='?context="+r.getRowname()+"&size=normal' title='Show smaller'>\n" + "<img src='/images/magnify.gif'
          * align='right'></a>\n"); }
          */
 
         out.println("<script language=\"javascript\" type=\"text/javascript\">\n"
                 + "objTreeMenu = new TreeMenu('menuLayer" + r.getRowname() + "', '/treeMenu/" + imagePath
                 + "', 'objTreeMenu', 'directory');\n" + "objTreeMenu.n[0] = new TreeNode('"
                 + (r.getRowname() == "" ? "(root)" : r.getRowname()) + "',"
                 + "'folder.gif', '/File.do?display=file&context=" + r.getRowname() + "', false);\n");
 
         File baseFile = (File) r.getFiles().get(r.getRowpath());
         List base = baseFile.getSubdirs();
         String depth = new String("0");
         getTreeBranch(out, base, 0, r, r.getRowpath(), depth, 0);
 
         out.println("objTreeMenu.drawMenu();\n" + "objTreeMenu.resetBranches();\n" + "</script>\n" + "</body></html>");
 
         return result.toString();
     }
 
     private void getTreeBranch(PrintWriter out, List tree, int treeLine, Row r, String path, String depth, int level) {
 
         String treeRow = "";
 
         for (int i = 0; i < tree.size(); i++) {
 
             File currentFile = (File) tree.get(i);
             List currentTree = currentFile.getSubdirs();
 
             depth = depth + "," + i; // make last one different
             level++;
             treeLine = treeLine++;
 
             treeRow = "objTreeMenu"; // start a javascript line to compose a tree
 
             StringTokenizer st = new StringTokenizer(depth, ",");
             while (st.hasMoreTokens()) {
                 treeRow = treeRow + ".n[" + st.nextToken() + "]";
             }
             try {
                 out.println(treeRow + " = new TreeNode('" + currentFile.getName()
                         + "', 'folder.gif', '/servlet/browse?display=file&context=" + r.getRowname() + "&path="
                         + URLEncoder.encode(currentFile.getPath(), "UTF-8") + "', false);");
             } catch (UnsupportedEncodingException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
             if (level < 50 && currentTree.size() != 0)
                 getTreeBranch(out, currentTree, treeLine, r, currentFile.getPath(), depth, level);
 
             level--;
             depth = depth.substring(0, depth.lastIndexOf(','));
         }
     }
 
 }
