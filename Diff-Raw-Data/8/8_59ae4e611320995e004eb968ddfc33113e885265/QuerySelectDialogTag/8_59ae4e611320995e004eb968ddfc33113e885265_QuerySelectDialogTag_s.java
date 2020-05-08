 package com.xaf.sql.query;
 
 import java.io.*;
 import java.sql.*;
 
 import javax.naming.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 import javax.servlet.jsp.tagext.*;
 
 import com.xaf.form.*;
 import com.xaf.skin.*;
 import com.xaf.sql.*;
 
 public class QuerySelectDialogTag extends TagSupport
 {
 	private String name;
 	private String source;
 	private String skinName;
 
 	public void release()
 	{
 		super.release();
 		name = null;
 		source = null;
 		skinName = null;
 	}
 
 	public void setName(String value) {	name = value; }
 	public void setSkin(String value) { skinName = value; }
 	public void setSource(String value) { source = value; }
 
 	public int doStartTag() throws JspException
 	{
 		try
 		{
 			JspWriter out = pageContext.getOut();
 			ServletContext context = pageContext.getServletContext();
 
     		StatementManager manager = StatementManagerFactory.getManager(context);
             if(manager == null)
             {
                 out.write("StatementManager not found in ServletContext");
                 return SKIP_BODY;
             }
 
     		QueryDefinition queryDefn = manager.getQueryDefn(source);
            if(manager == null)
             {
                 out.write("QueryDefinition '"+source+"' not found in StatementManager");
                 return SKIP_BODY;
             }
 
 	    	QuerySelectDialog dialog = queryDefn.getSelectDialog(name);
            if(manager == null)
             {
                out.write("QuerySelectDialog '"+name+"' not found in QueryDefinition");
                 return SKIP_BODY;
             }
 
 			DialogSkin skin = skinName == null ? SkinFactory.getDialogSkin() : SkinFactory.getDialogSkin(skinName);
 			if(skin == null)
 			{
 				out.write("DialogSkin '"+skinName+"' not found in skin factory.");
 				return SKIP_BODY;
 			}
 
 			DialogContext dc = new DialogContext(pageContext.getServletContext(), (Servlet) pageContext.getPage(), (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), dialog, skin);
 			dialog.prepareContext(dc);
 
 			out.write(dialog.getHtml(dc, true));
 			return SKIP_BODY;
 		}
 		catch(IOException e)
 		{
 			throw new JspException(e.toString());
 		}
 	}
 
 	public int doEndTag() throws JspException
 	{
 		return EVAL_PAGE;
 	}
 }
