 package org.makumba.parade.view.managers;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.view.interfaces.CommandView;
 
 public class CommandViewManager implements CommandView {
 
     public String getCommandView(String view, Row r, String path, String file, String opResult) {
         if (view == null || view.equals(""))
             return "jsp:/tipOfTheDay.jsp";
        if (view.startsWith("uploadFile"))
            return "jsp:/uploadFile.jsp?context="+r.getRowname()+"&path="+path;
         if (view != null && view.equals("newFile"))
             return newFileView(r, path);
         if (view != null && view.equals("newDir"))
             return newDirView(r, path);
         if(view != null && view.equals("commandOutput"))
             return commandOutput(r, path, opResult);
         if(view != null && view.equals("commit"))
             return commitCvsFile(r, path, file);
 
         return "No such view defined for Command";
     }
 
     /*
      * <form action="editFile.jsp" target="directory"> <% String context=request.getParameterValues("context")[0];
      * 
      * String path=""; if(request.getParameterValues("path")!=null) path=request.getParameterValues("path")[0]; %>
      * <input type=hidden size="50" value="<%=path.length()>1?path+java.io.File.separator:""%>" name=path> <input
      * type=hidden value="<%=context%>" name=context> Create new file:<input type=text name=file> <input type=submit
      * value=Edit> </form>
      */
 
     private String commandOutput(Row r, String path, String opResult) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         String pathURI = "";
 
         try {
             pathURI = URLEncoder.encode(path, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         out.println("<HTML><HEAD><TITLE>Command view for " + r.getRowname() + "</TITLE>");
         out.println("<link rel='StyleSheet' href='/style/parade.css' type='text/css'>");
         out.println("<link rel='StyleSheet' href='/style/command.css' type='text/css'>");
         out.println("</HEAD><BODY class='command'>");
 
         out.println("<script language='JavaScript'>\n" +
                 "<!--\n" +
                 "top.frames[\"directory\"].document.location.href='/servlet/browse?display=file&context="+r.getRowname()+"&path="+pathURI+ "'\n" +
                 "// -->" +
                 "</script>");
         
         if (opResult != null)
             out.println(opResult);
         
         out.println("</BODY></HTML>");
         
         return result.toString();
     }
 
     private String newFileView(Row r, String path) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         out.println("<HTML><HEAD><TITLE>Command view for " + r.getRowname() + "</TITLE>");
         out.println("<link rel='StyleSheet' href='/style/parade.css' type='text/css'>");
         out.println("<link rel='StyleSheet' href='/style/command.css' type='text/css'>");
         out.println("</HEAD><BODY class='command'>"
                 + "<form target='directory' action='/Command.do' method='POST'>\n" + "<input type=hidden value='"
                 + r.getRowname() + "' name=context>\n" + "<input type=hidden value='newFile' name=op>\n"
                 + "Create new file: <input type=text name=params>\n" + "<input type=hidden value='" + path
                 + "' name=params>\n" + "<input type=submit value=Create>\n" + "</form>\n" 
                 + "</body></html>\n");
 
         return result.toString();
     }
 
     private String newDirView(Row r, String path) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         out.println("<HTML><HEAD><TITLE>Command view for " + r.getRowname() + "</TITLE>");
         out.println("<link rel='StyleSheet' href='/style/parade.css' type='text/css'>");
         out.println("<link rel='StyleSheet' href='/style/command.css' type='text/css'>");
         out.println("</HEAD><BODY class='command'>"
                 + "<form action='/Command.do' target='directory' method='POST'>\n" + "<input type=hidden value='"
                 + r.getRowname() + "' name=context>\n" + "<input type=hidden value='newDir' name=op>\n"
                 + "Create new directory: <input type=text name=params>\n" + "<input type=hidden value='" + path
                 + "' name=params>\n" + "<input type=submit value=Create>\n" + "</form>\n"
                 + "</body></html>\n");
 
         return result.toString();
     }
 
     public String uploadFile(String path, String file, Object content, Object context) {
 
         boolean success = true;
 
         try {
             OutputStream dest = new BufferedOutputStream(new FileOutputStream(path + File.separator + file));
             ((org.makumba.Text) content).writeTo(dest);
             dest.close();
         } catch (Exception ew) {
             success = false;
             return ("Error writing file: " + ew);
         }
 
         if (success) {
             Session s = InitServlet.getSessionFactory().openSession();
             Transaction tx = s.beginTransaction();
 
             Parade p = (Parade) s.get(Parade.class, new Long(1));
             FileManager fileMgr = new FileManager();
             fileMgr.uploadFile(p, path + File.separator + file, (String) context);
 
             tx.commit();
             s.close();
         }
         return ("");
     }
     
     private String commitCvsFile(Row r, String path, String file) {
         
         java.io.File f = new java.io.File(file);
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         out.println("<HTML><HEAD><TITLE>Command view for " + r.getRowname() + "</TITLE>");
         out.println("<link rel='StyleSheet' href='/style/parade.css' type='text/css'>");
         out.println("<link rel='StyleSheet' href='/style/command.css' type='text/css'>");
         out.println("</HEAD><BODY class='command'>"
                 + "<form target='command' action='/Cvs.do' method='POST'>\n" + "<input type=hidden value='"
                 + r.getRowname() + "' name=context>\n"
                 + "<input type=hidden value='commit' name=op>\n"
                 + "<input type=hidden value='" + path + "' name=params>\n"
                 + "<input type=hidden value='" + f.getAbsolutePath() + "' name=params>\n"
                 + "Committing <strong>" + f.getName() + "</strong> with message:<br>\n"
                 + "<input type='text' rows='3' cols='40' name=params><br>\n"
                 + "<input type=submit value=Commit>\n" + "</form>\n" + "</body></html>\n");
 
         return result.toString();
     }
 
 }
