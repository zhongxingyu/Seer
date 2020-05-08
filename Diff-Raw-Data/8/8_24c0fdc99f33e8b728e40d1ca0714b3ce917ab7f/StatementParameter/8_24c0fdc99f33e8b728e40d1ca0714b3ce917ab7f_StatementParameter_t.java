 package com.xaf.sql;
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 import javax.naming.*;
 import javax.xml.parsers.*;
 
 import org.w3c.dom.*;
 
 import com.xaf.db.*;
 import com.xaf.xml.*;
 import com.xaf.skin.*;
 import com.xaf.sql.query.*;
 import com.xaf.report.*;
 import com.xaf.value.*;
 
 public class StatementParameter
 {
 	static class ApplyContext
 	{
 		private StatementInfo stmtInfo;
 		private int activeParamNum;
 
 		public ApplyContext(StatementInfo stmtInfo)
 		{
 			this.stmtInfo = stmtInfo;
 			activeParamNum = 0;
 		}
 
 		public StatementInfo getStmtInfo() { return stmtInfo; }
 		public int getNextParamNum() { return ++activeParamNum; }
 	}
 
 	private Object valueSource;
 	private int paramType;
 
 	public StatementParameter(StatementInfo statement, int paramNum, Element paramElem)
 	{
 		String valueSrcId = paramElem.getAttribute("value");
 		if(valueSrcId.length() > 0)
 		{
 			valueSource = ValueSourceFactory.getSingleOrStaticValueSource(valueSrcId);
 			String paramTypeName = paramElem.getAttribute("type");
 			if(paramTypeName.length() > 0)
 			{
 				Integer typeNum = (Integer) StatementManager.SQL_TYPES_MAP.get(paramTypeName);
 				if(typeNum == null)
 					throw new RuntimeException("param type '"+paramTypeName+"' is invalid for statement '"+statement.getId()+"'");
 				paramType = typeNum.intValue();
 			}
 			else
 			{
 				paramType = Types.VARCHAR;
 			}
 		}
 		else
 		{
 			valueSource = ValueSourceFactory.getListValueSource(paramElem.getAttribute("values"));
 			paramType = Types.ARRAY;
 		}
 	}
 
 	public SingleValueSource getValueSource() { return (SingleValueSource) valueSource; }
 	public ListValueSource getListSource() { return (ListValueSource) valueSource; }
 	public boolean isListType() { return paramType == Types.ARRAY; }
 
 	public void apply(ApplyContext ac, DatabaseContext dc, ValueContext vc, PreparedStatement stmt) throws SQLException
 	{
 		if(paramType != Types.ARRAY)
 		{
			int paramNum = ac.getNextParamNum();
 			SingleValueSource vs = (SingleValueSource) valueSource;
 			if(paramType == Types.VARCHAR)
 				stmt.setObject(paramNum, vs.getValue(vc));
 			else
 			{
 				switch(paramType)
 				{
 					case Types.INTEGER:
 						stmt.setInt(paramNum, vs.getIntValue(vc));
 						break;
 
 					case Types.DOUBLE:
 						stmt.setDouble(paramNum, vs.getDoubleValue(vc));
 						break;
 				}
 			}
 		}
 		else
 		{
 			String[] values = ((ListValueSource) valueSource).getValues(vc);
 			for(int q = 0; q < values.length; q++)
 			{
				int paramNum = ac.getNextParamNum();
 				stmt.setObject(paramNum, values[q]);
 			}
 		}
 	}
 
 	public void appendDebugHtml(StringBuffer html, ValueContext vc)
 	{
 		if(paramType != Types.ARRAY)
 		{
 			SingleValueSource vs = (SingleValueSource) valueSource;
 			html.append("<li><code><b>");
 			html.append(vs.getId());
 			html.append("</b> = ");
 			html.append(vs.getValue(vc));
 			html.append("</code> (");
 			html.append(StatementManager.getTypeNameForId(paramType));
 			html.append(")</li>");
 		}
 		else
 		{
 			ListValueSource vs = (ListValueSource) valueSource;
 			html.append("<li><code><b>");
 			html.append(vs.getId());
 			html.append("</b> = ");
 			html.append(vs.getValues(vc));
 			html.append("</code></li>");
 		}
 	}
 }
 
