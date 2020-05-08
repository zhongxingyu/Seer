 package com.xaf.task.sql;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 
 import javax.naming.*;
 import javax.servlet.*;
 
 import org.w3c.dom.*;
 
 import com.xaf.db.*;
 import com.xaf.form.*;
 import com.xaf.report.*;
 import com.xaf.sql.*;
 import com.xaf.skin.*;
 import com.xaf.task.*;
 import com.xaf.value.*;
 
 public class StatementTask extends AbstractTask
 {
     static public final String DEFAULT_REPORTSKINID = "report";
 
     private StatementInfo statementInfo;
 	private String stmtName;
 	private String stmtSourceId;
 	private String dataSourceId;
 	private String reportId;
 	private String reportSkinId = DEFAULT_REPORTSKINID;
 	private String storeValueName;
 	private SingleValueSource storeValueSource;
 	private boolean produceReport = true;
 	private int storeValueType = SingleValueSource.RESULTSET_STORETYPE_SINGLEROWMAP;
 
     public StatementTask()
     {
 		super();
     }
 
 	public void reset()
 	{
 		super.reset();
 		statementInfo = null;
 		stmtName = null;
 		stmtSourceId = null;
 		dataSourceId = null;
 		reportId = null;
 		reportSkinId = DEFAULT_REPORTSKINID;
 		storeValueName = null;
 		storeValueSource = null;
 		produceReport = true;
 		storeValueType = SingleValueSource.RESULTSET_STORETYPE_SINGLEROWMAP;
 	}
 
 	public String getStmtName() { return stmtName; }
 	public void setStmtName(String value) {	stmtName = value; }
 
 	public String getStmtSource() { return stmtSourceId; }
 	public void setStmtSource(String value) { stmtSourceId = value; }
 
 	public String getDataSource() { return dataSourceId; }
 	public void setDataSource(String value) { dataSourceId = value; }
 
 	public String getReport() { return reportId; }
 	public void setReport(String value) { reportId = value; if("none".equals(value)) produceReport = false; }
 
 	public String getSkin() { return reportSkinId; }
 	public void setSkin(String value) { reportSkinId = value; }
 
 	public String getStore() { return storeValueName; }
 	public void setStore(String value) { storeValueName = value; }
 
 	public String getStoreType() { return Integer.toString(storeValueType); }
 	public void setStoreType(String value)
 	{
         if(value.length() == 0)
         {
             storeValueType = -1;
             return;
         }
 
 		String[] typeNames = SingleValueSource.RESULTSET_STORETYPES;
 		int typeCount = typeNames.length;
 		for(int i = 0; i < typeCount; i++)
 		{
 			if(typeNames[i].equals(value))
 			{
 				storeValueType = i;
 				return;
 			}
 		}
 		storeValueType = -1;
 	}
 
     public void initialize(Element elem) throws TaskInitializeException
     {
 		super.initialize(elem);
 
         stmtSourceId = elem.getAttribute("stmt-src");
         if(stmtSourceId.length() == 0) stmtSourceId = null;
 
         dataSourceId = elem.getAttribute("data-src");
         if(dataSourceId.length() == 0) dataSourceId = null;
 
         if(elem.getChildNodes().getLength() > 0)
         {
             if(elem.getAttribute("name").length() == 0)
                 elem.setAttribute("name", "SqlExecuteAction-" + getTaskNum());
             statementInfo = new StatementInfo();
             statementInfo.importFromXml(elem, "Task", null);
         }
         else
         {
             stmtName = elem.getAttribute("name");
         }
 
         reportId = elem.getAttribute("report");
         if(reportId.length() == 0) reportId = null;
 
         reportSkinId = elem.getAttribute("skin");
         if(reportSkinId.length() == 0) reportSkinId = DEFAULT_REPORTSKINID;
 
         storeValueName = elem.getAttribute("store");
         if(storeValueName.length() == 0) storeValueName = null;
 
         if(reportId != null && reportId.equals("none"))
             produceReport = false;
 
 		if(storeValueName != null)
 		{
             setStoreType(elem.getAttribute("store-type"));
 		}
     }
 
     public void execute(TaskContext tc) throws TaskExecuteException
     {
 		if(storeValueName != null && storeValueSource == null)
 		{
 			storeValueSource = ValueSourceFactory.getStoreValueSource(storeValueName);
 			if(storeValueSource == null)
 				throw new TaskExecuteException("SingleValueSource '"+ storeValueName +"' not found");
 			if(! storeValueSource.supportsSetValue())
 				throw new TaskExecuteException("SingleValueSource '"+ storeValueName +"' does not support value storage.");
 
 			if(storeValueSource instanceof DialogFieldValue)
 				storeValueType = SingleValueSource.RESULTSET_STORETYPE_SINGLEROWFORMFLD;
 
 			if(storeValueType == -1)
 				throw new TaskExecuteException("store-type must be one of "+SingleValueSource.RESULTSET_STORETYPES.toString());
 		}
 
 		ServletContext context = tc.getServletContext();
 		StatementManager stmtManager = stmtSourceId == null ? StatementManagerFactory.getManager(context) : StatementManagerFactory.getManager(stmtSourceId);
 		DatabaseContext dbContext = DatabaseContextFactory.getContext(tc.getRequest(), context);
 
         if(stmtManager == null)
 		{
             tc.addErrorMessage("StatementManager file '" + stmtSourceId + "' not found (specified in ServletContext config init parameter 'sql-statements-file'", false);
 			return;
 		}
 
         if(flagIsSet(TASKFLAG_DEBUG))
         {
             StringBuffer debugMessage = new StringBuffer();
             StatementInfo si;
             if(statementInfo != null)
                 si = statementInfo;
             else
                 si = stmtManager.getStatement(stmtName);
 
             if(si == null)
                 debugMessage.append("SQL: statement '"+stmtName+"' doesn't exist");
             else
             {
 				debugMessage.append("<p>SQL: statement '");
 				debugMessage.append(si.getId());
 				debugMessage.append("</p></pre>");
 				debugMessage.append(si.getDebugHtml(tc));
 				debugMessage.append("</pre>");
             }
 			tc.addErrorMessage(debugMessage.toString(), false);
             return;
         }
 
         StringWriter out = new StringWriter();
 
         try
         {
             if(produceReport && storeValueSource == null)
             {
                 ReportSkin reportSkin = SkinFactory.getReportSkin(reportSkinId);
                 if(reportSkin == null)
 				{
 					tc.addErrorMessage("ReportSkin '"+reportSkinId+"' not found.", false);
                     return;
 				}
                 else
                 {
                     if(statementInfo != null)
                         stmtManager.produceReport(out, dbContext, tc, reportSkin, statementInfo, null, reportId);
                     else
                         stmtManager.produceReport(out, dbContext, tc, reportSkin, stmtName, null, reportId);
                 }
             }
             else if(!produceReport && storeValueSource != null)
             {
                 if(statementInfo != null)
                     stmtManager.executeAndStore(dbContext, tc, dataSourceId, statementInfo, storeValueSource, storeValueType);
                 else
                     stmtManager.executeAndStore(dbContext, tc, dataSourceId, stmtName, storeValueSource, storeValueType);
             }
             else if(produceReport && storeValueSource != null)
             {
                 ReportSkin reportSkin = SkinFactory.getReportSkin(reportSkinId);
                 if(reportSkin == null)
 				{
 					tc.addErrorMessage("ReportSkin '"+reportSkinId+"' not found.", false);
                     return;
 				}
                 else
                 {
                     if(statementInfo != null)
                         stmtManager.produceReportAndStoreResultSet(out, dbContext, tc, reportSkin, statementInfo, null, reportId, storeValueSource, storeValueType);
                     else
                         stmtManager.produceReportAndStoreResultSet(out, dbContext, tc, reportSkin, stmtName, null, reportId, storeValueSource, storeValueType);
                 }
             }
         }
         catch(IOException e)
         {
             throw new TaskExecuteException(e);
         }
         catch(SQLException e)
         {
 			StringBuffer errorMsg = new StringBuffer();
            StatementInfo si = stmtManager.getStatement(stmtName);
 
 			StringWriter stack = new StringWriter();
             e.printStackTrace(new PrintWriter(stack));
 
 			errorMsg.append("<pre>");
 			errorMsg.append(si.getDebugHtml(tc));
 			errorMsg.append("\n\n");
 			errorMsg.append(stack.toString());
 			errorMsg.append("</pre>");
 
 			tc.addErrorMessage(errorMsg.toString(), false);
             return;
         }
         catch(StatementNotFoundException e)
         {
             throw new TaskExecuteException(e);
         }
         catch(NamingException e)
         {
             throw new TaskExecuteException(e);
         }
 
 		tc.addResultMessage(out.toString());
     }
 
 }
