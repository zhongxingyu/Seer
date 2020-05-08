 package com.exlibris.primo.utils;
 
 import java.io.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.exlibris.primo.interfaces.PushToInterface;
 import com.exlibris.primo.srvinterface.RecordDocDTO;
import com.exlibris.primo.srvinterface.PnxConstants;
 import com.exlibris.primo.utils.formats.Ris;
 import com.exlibris.primo.xsd.commonData.PrimoResult;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /* This plug-in enables you to export records in .ris format to EndNote Local from PRIMO
    Original code by Alessandro Fasoli (alessandro dot fasoli at exlibrisgroup dot com)
          http://www.exlibrisgroup.org/display/PrimoCC/Plug-in+for+Citavi%2C+Endnote+and+Zotero+%28RIS+Export%29
    updated to export multiple records by Mehmet Celik (mehmet dot celik at libis dot be)
 
    License: BSD style
    Use, modification and distribution of the code are permitted provided the copyright notice, list of conditions and disclaimer appear in all related material.
 
    LIBRARIES:
          ~/p3_1/ng/primo/home/system/thirdparty/openserver/server/search/lib/jaguar-client.jar
          ~/p3_1/ng/primo/home/system/search/client/primo_library-common.jar
          ~/p3_1/ng/primo/home/system/search/client/primo_common-infrastructure.jar
          ~/p3_1/ng/primo/home/system/thirdparty/openserver/server/search/lib/xbean.jar
          ~/p3_1/ng/primo/home/system/thirdparty/openserver/server/search/lib/javax.servlet.jar
          primo-utils.jar
 
    How to make primo-utils.jar (you only need this to compile)
                ---------------
    Log on to your FrontEnd server
     $ fe_web
     $ cd WEB-INF
     $ cd classes
     $ jar zcvf /tmp/primo-utils.jar ./*
 
     INSTALL
     -------
     - Copy EndNoteLocalProcess.class to the FrontEnd server and place file in
       ~/p3_1/ng/primo/home/system/thirdparty/openserver/server/search/deploy/primo_library-app.ear/primo_library-libweb.war/WEB-INF/classes/com/exlibris/primo/utils
 
     - Go to the Back Office, Primo Home>Advanced configuration>All mapping table and select SubSystem: Adaptors, TableName:Pushto Adaptors
 
     - Add a new row related to the EndNoteLocalProcess class:
       Adaptor Identifier: EndNoteLocal
       Key: Class
       Value: com.exlibris.primo.utils.EndNoteLocalProcess
 
     - Deploy the Mapping Tables
 
     - Go to Back Office, Primo Home>Advanced configuration>All code Table and choose SubSystem: Front End, TableName: Keeping this item Tile.
       For each language relevant to you, add a new Code Table row:
       Code: default.fulldisplay.command.pushto.option.EndNoteLocal
       Description : EndNote Local
       Language: <your_code>
       Display order: highest number + 1
 
     - Deploy "All Code Tables"
 
     - Restart FrontEnd service
  */
 
 public class EndNoteLocalProcess implements PushToInterface {
     
     public String pushTo(HttpServletRequest request, HttpServletResponse response, PrimoResult[] records, boolean fromBasket) throws Exception {
 
         int i;
         int j;
 
         if (request.getParameter("encode") == null) {
 
             response.setContentType("text/html");
             PrintWriter out = response.getWriter();
             out.println("<html>");
             out.println("<script language=\"JavaScript\">");
             out.println("<!--");
             out.println("{window.resizeTo(330,250)}");
             out.println("-->");
             out.println("</script>");
             out.println("<head>");
             out.println("<style>");
             out.println("body {background-color: #ffffff; color: #32322f;margin: 0px; padding: 0px; font-family: 'Arial Unicode MS',Arial,verdana,serif;font-size: 100%;}");
             out.println("h2 {float: left;width:100%; padding: 0.3em 0; color:#606f7f;}");
             out.println("th {color: #8c8d8c; font-weight: bold; font-size: 120%; white-space: nowrap;}");
             out.println("select {color: black; font-size: 90%; white-space: nowrap;font-family: 'Arial Unicode MS',Arial,verdana,serif;}");
             out.println("input {color: black; font-size: 90%; white-space: nowrap;font-family: 'Arial Unicode MS',Arial,verdana,serif;}");
             out.println("</style>");
             out.println("<title>Select your encoding charset</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h2>Import to EndNote Local</h2>");
             out.println("<br/><br/>");
             out.println("<form method=\"POST\" action=\"#\">");
             out.println("<table cellspacing=\"10\" style=\"float:left;\"> ");
             out.println("<tr><th>Encoding : </th>");
             out.println("<td height=\"30\"> ");
             out.println("<select name=\"encode\">");
             out.println("<option value=\"UTF-8\">UTF-8</option>");
             out.println("<option value=\"ISO-8859-1\">ISO-8859-1</option>");
             out.println("<option value=\"ASCII\">ASCII</option>");
             out.println("<option value=\"WINDOWS-1251\">WINDOWS-1251</option>");
             out.println("</select>");
             out.println("</td></tr><tr>");
             out.println("<th></th><td>");
             out.println("<input type=\"submit\" value=\"Save\"></td></tr>");
             out.println("</table><br>");
             out.println("</form>");
             out.println("</body>");
             out.println("</html>");
 
         } else {
             PrintWriter out = null;
             String encode = request.getParameter("encode");
             Date now = new Date();
             SimpleDateFormat df = new SimpleDateFormat("yyyMMddhhmmss");
             
             request.setCharacterEncoding(encode);
             
             response.setCharacterEncoding(encode);
             response.setContentType("mimetype: application/octet-stream");
             response.setContentType("text/text; charset=" + encode);
             response.setHeader("Content-Disposition", "attachment; filename=\"" + df.format(now) + ".ris\"");
             
             out = response.getWriter();
             for(int k=0; k< records.length; k++){ 
                 RecordDocDTO record = new RecordDocDTO(request, records[k], 0);
                 out.println(Ris.fromRecordDocDTO(record));
             }
                                     
             out.close();
         }
 
         return null;
     }
 
     public String getContent(HttpServletRequest request, boolean fromBasket) {
         return null;
     }
 
     public String getFormAction() {
         return null;
     }
 }
