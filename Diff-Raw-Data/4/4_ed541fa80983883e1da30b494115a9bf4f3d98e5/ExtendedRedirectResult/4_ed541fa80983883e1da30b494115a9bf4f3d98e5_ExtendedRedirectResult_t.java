 package burst.reader.web;
 
 import burst.web.util.WebUtil;
 import com.opensymphony.xwork2.ActionInvocation;
 import org.apache.struts2.ServletActionContext;
 import org.apache.struts2.dispatcher.StrutsResultSupport;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.PrintWriter;
 
 import static javax.servlet.http.HttpServletResponse.SC_FOUND;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Burst
  * Date: 13-4-4
  * Time: 下午9:43
  * To change this template use File | Settings | File Templates.
  */
 public class ExtendedRedirectResult extends StrutsResultSupport {
 
     private int statusCode = SC_FOUND;
 
     public void setStatusCode(int statusCode) {
         this.statusCode = statusCode;
     }
 
     @Override
     protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
 
         HttpServletRequest request = ServletActionContext.getRequest();
         HttpServletResponse response = ServletActionContext.getResponse();
 
         boolean write_wml = false;
         String userAgent = request.getHeader(WebUtil.HEAD_USERAGENT);
        if (userAgent == null) {
            write_wml = true;
        } else {
             write_wml = userAgent.toLowerCase().indexOf("untrusted/1.0") != -1;
         }
         if (write_wml) {
             response.setContentType("text/vnd.wap.wml");
             PrintWriter writer = response.getWriter();
             writer.print("<?xml version=\"1.0\"?><!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">");
             writer.print("<wml><head>");
             writer.print("<meta http-equiv=\"Content-Type\" content=\"text/vnd.wap.wml;charset=UTF-8\"/>");
             writer.print("</head><card id=\"main\" title=\"redirecting...\" onenterforward=\"" + finalLocation + "\"><p>redirecting...</p></card></wml>");
             writer.close();
         } else {
             if (SC_FOUND == statusCode) {
                 response.sendRedirect(finalLocation);
             } else {
                 response.setStatus(statusCode);
                 response.setHeader("Location", finalLocation);
                 response.getWriter().write(finalLocation);
                 response.getWriter().close();
             }
         }
     }
 }
