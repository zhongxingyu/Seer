 /***************************************************************************
   *  Comment Mentor: A Comment Quality Assessment Tool
   *  Copyright (C) 2005 Michael E. Locasto
   *
   *  This program is free software; you can redistribute it and/or modify
   *  it under the terms of the GNU General Public License as published by
   *  the Free Software Foundation; either version 2 of the License, or
   *  (at your option) any later version.
   *
   *  This program is distributed in the hope that it will be useful, but
   *  WITHOUT ANY WARRANTY; without even the implied warranty of 
   *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
   *  General Public License for more details.
   *
   *  You should have received a copy of the GNU General Public License
   *  along with this program; if not, write to the:
   *  Free Software Foundation, Inc.
   *  59 Temple Place, Suite 330 
   *  Boston, MA  02111-1307  USA
   *
  * $Id: GenerateReport.java,v 1.14 2007-04-04 04:06:06 brigand2 Exp $
   **************************************************************************/
 package comtor;
 
 import comtor.analyzers.*;
 import java.io.*;
 import java.util.*;
 import java.text.*;
 
 /**
  * The <code>GenerateReport</code> class is a tool to
  * generate a report from a vector containing property lists.
  *
  * @author Joe Brigandi
  */
 public class GenerateReport
 {
   /**
    * Takes a vector full of property lists and
    * generates a report.
    *
    * @param v is a Vector of property lists
    */
   public void generateReport(Vector v)
   {
     try
     {
       PrintWriter prt = new PrintWriter(new FileWriter("ComtorReport.php"));            
       
       prt.println("<html>");
       prt.println("<head>"); 
       prt.println("<title>Comment Mentor</title>");
       prt.println("<style type=text/css>");
       prt.println("body { background-color: #fafad2;}");
       prt.println("h3{text-align: center; font-size: 26px}");
       prt.println("#report{border:double; padding: 10px; font-size: 20px}");
       prt.println("#links{font-size: 18px; padding: 5px; padding-left: 15px}");
       prt.println("#footer{font-size: 14px; padding-left: 10px;}");
       prt.println("#class{font-size: 18px; font-weight: bold}");
       prt.println("#method{padding-left:25px; padding-top:5px; font-size: 14px; font-style: italic}");
       prt.println("#comment{padding-left:50px; font-size: 12px}");
       prt.println("a{ color: #000099; text-decoration: underline; font-size: 14px;}");
       prt.println("a:visited{ color: #0000FF;}");
       prt.println("a:hover{ color: #000000; text-decoration: underline}");
       prt.println("</style>");
       prt.println("<script language=\"javascript\">");
       prt.println("function toggleDiv(divid){");
       prt.println("if(document.getElementById(divid).style.display == 'none'){");
       prt.println("document.getElementById(divid).style.display = 'block';");
       prt.println("}else{");
       prt.println("document.getElementById(divid).style.display = 'none';");
       prt.println("}}");
       prt.println("</script>");
       prt.println("</head>");
       prt.println("<body>");
       
       prt.println("<?");
      prt.println("mysql_connect('localhost', 'brigand2', 'joeBrig');");
       prt.println("mysql_select_db('comtor');");
       prt.println("?>");
 
       DateFormat dateFormat = new SimpleDateFormat("M/d/yy h:mm a");
       java.util.Date date = new java.util.Date();
       String timeDate = dateFormat.format(date);
       
       prt.println("<a name=\"top\"></a><h3>");
       prt.println("Comment Mentor Report<br />");
       prt.println(timeDate + "<br />");
       prt.println("<a href=\"javascript:print()\">PRINT THIS PAGE!</a>");
       prt.println("</h3>");
       
       String dir = System.getProperty("user.dir");
       BufferedReader br = new BufferedReader(new FileReader(dir + "/source.txt"));    
       String line;
       prt.println("Java Source Code...<br />");
       while((line = br.readLine()) != null)   
       {
         prt.println("<a href=\"javascript:;\" onmousedown=\"toggleDiv('" + line + "');\">" + line + "</a><br />");
         
         prt.println("<div id=\"" + line + "\" style=\"display:none\">");
         
         BufferedReader in = new BufferedReader(new FileReader(line));
         String str;
         while ((str = in.readLine()) != null) {
           prt.println(str + "<br />");
         }
         in.close();
         
         prt.println("</div>");
       }
       br.close();
       prt.println("<br />");
       
       prt.println("<div id=\"links\">Reports Generated:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
       for(int i=0; i < v.size(); i++)
       {
         Properties list = new Properties();
         list = (Properties)v.get(i);
         prt.println("<a href=\"#" + list.getProperty("title") + "\">" + list.getProperty("title") + "</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
       }
       prt.println("</div>"); 
       
       for(int i=0; i < v.size(); i++)
       {
         Properties list = new Properties();
         list = (Properties)v.get(i);
         
         String[] arr = new String[0];
         arr = list.keySet().toArray(arr);
         Arrays.sort(arr);
         
         prt.println("<a name=\"" + list.getProperty("title") + "\"></a><div id=\"report\">");
         prt.println("Report: " + list.getProperty("title") + " (" + list.getProperty("description") + ")<br />");
         
         for(int j=0; j < arr.length-2; j++)
         {
           if(arr[j] != null)
           {
             String temp = arr[j];
             if(temp.length() == 3)
               prt.println("<hr><div id=\"class\">" + list.getProperty("" + arr[j]) + "</div>");
             else if(temp.length() == 7)
               prt.println("<div id=\"method\"><li>" + list.getProperty("" + arr[j]) + "</div>");
             else if(temp.length() > 7)
               prt.println("<div id=\"comment\">- " + list.getProperty("" + arr[j]) + "</div>");
             
             String report = list.getProperty("title");
             prt.println("<?");
             prt.println("$select = mysql_query(\"SELECT reportID FROM reports WHERE reportName='" + report + "'\");");
             prt.println("$row = mysql_fetch_array($select);");
             prt.println("$reportID = $row['reportID'];");
             prt.println("mysql_query(\"INSERT INTO data(reportID, attribute, value) VALUES ('$reportID', '" + arr[j] + "', '" + list.getProperty("" + arr[j]) + "')\");");
             prt.println("?>");
           }
         }
         prt.println("<a href=\"#top\">Top</a></div><br /><br />");
       }
       
       prt.println("<div id=\"footer\">Related Links:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
       prt.println("<a href=\"http://java.sun.com/j2se/1.3/docs/tooldocs/win32/javadoc.html#javadoctags\">Javadoc Tags</a>");
       prt.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"http://java.sun.com/j2se/javadoc/\">Javadoc Tutorial</a></div>");
       prt.println("</body>");
       prt.println("</html>");
       
       prt.close();
     }
     
     catch(Exception ex) 
     {
       System.err.print("!!!An exception was thrown!!!");
       System.err.println(ex.toString());
     }
   }
 }
