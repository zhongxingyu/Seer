 package com.xaf.sql.query;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.sql.*;
 import java.text.*;
 
 import com.xaf.form.*;
 
 public class ResultSetNavigatorButtonsField extends DialogField
 {
 	public ResultSetNavigatorButtonsField()
 	{
 		this("rs_nav_buttons");
 		setFlag(DialogField.FLDFLAG_INVISIBLE);
 	}
 
 	public ResultSetNavigatorButtonsField(String name)
 	{
 		super(name, null);
 		setFlag(DialogField.FLDFLAG_INVISIBLE);
 	}
 
 	public String getControlHtml(DialogContext dc)
 	{
 		String attrs = dc.getSkin().getDefaultControlAttrs();
 		QuerySelectScrollState state = (QuerySelectScrollState)	dc.getRequest().getAttribute(dc.getTransactionId() + "_state");
 		if(state == null)
 			return "QuerySelectScrollState not found.";
 
 		boolean isScrollable = state.isScrollable();
         int activePage = state.getActivePage();
         int lastPage = state.getTotalPages();
 
 		StringBuffer html = new StringBuffer("<center>");
 		html.append("<nobr>Page ");
 		html.append(state.getActivePage());
 		if(isScrollable)
 		{
 			html.append(" of ");
 	    	html.append(state.getTotalPages());
 		}
 		html.append("</nobr>&nbsp;&nbsp;");
 		if(activePage > 1)
 			html.append("<input type='submit' name='rs_nav_first' value=' First ' " + attrs + "> ");
 
         if(activePage > 1)
     		html.append("<input type='submit' name='rs_nav_prev' value=' Prev ' " + attrs + "> ");
 
         try
         {
             if(state.hasMoreRows() || (isScrollable && activePage < lastPage))
                 html.append("<input type='submit' name='rs_nav_next' value=' Next ' " + attrs + "> ");
         }
         catch(SQLException e)
         {
         }
 
 		if(isScrollable && activePage < lastPage)
 		{
 			html.append("<input type='submit' name='rs_nav_last' value=' Last ' " + attrs + "> ");
 		}
		html.append("&nbsp;&nbsp;<nobr>");
		html.append(NumberFormat.getNumberInstance().format(state.getTotalRows()));
		html.append(" total rows</nobr>");
 		html.append("</center>");
 
 		return html.toString();
 	}
 }
