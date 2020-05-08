 package com.xaf.skin;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 import java.text.*;
 
 import com.xaf.report.*;
 import com.xaf.sql.*;
 import com.xaf.value.*;
 
 public class HtmlReportSkin implements ReportSkin
 {
     static public final int HTMLFLAG_SHOW_BANNER        = 1;
     static public final int HTMLFLAG_SHOW_HEAD_ROW      = HTMLFLAG_SHOW_BANNER * 2;
     static public final int HTMLFLAG_SHOW_FOOT_ROW      = HTMLFLAG_SHOW_HEAD_ROW * 2;
     static public final int HTMLFLAG_ADD_ROW_SEPARATORS = HTMLFLAG_SHOW_FOOT_ROW * 2;
 
 	static public final String[] ALIGN_ATTRS = { "LEFT", "CENTER", "RIGHT" };
 
     protected int flags;
 	protected String outerTableAttrs = "border=0 cellspacing=1 cellpadding=2 bgcolor='#EEEEEE'";
 	protected String innerTableAttrs = "cellpadding='1' cellspacing='0' border='0' width='100%'";
	protected String frameHdRowAttrs = "bgcolor='#6699CC'";
 	protected String frameHdFontAttrs = "face='verdana,arial,helvetica' size=2 color=white";
     protected String frameHdTableRowBgcolorAttrs = "#FFFFCC";
     protected String frameFtRowAttrs = "bgcolor='lightyellow'";
 	protected String frameFtFontAttrs = "face='verdana,arial,helvetica' size=2 color='#000000'";
 	protected String bannerRowAttrs = "bgcolor='lightyellow'";
 	protected String bannerItemFontAttrs = "face='arial,helvetica' size=2";
 	protected String dataHdFontAttrs = "face='verdana,arial' size='2' style='font-size: 8pt;' color='navy'";
 	protected String dataFontAttrs = "face='verdana,arial' size='2' style='font-size: 8pt;'";
 	protected String dataFtFontAttrs = "face='verdana,arial' size='2' style='font-size: 8pt;' color='navy'";
 	protected String rowSepImgSrc = "/shared/resources/images/design/bar.gif";
 
     public HtmlReportSkin()
     {
         setFlag(HTMLFLAG_SHOW_BANNER | HTMLFLAG_SHOW_HEAD_ROW | HTMLFLAG_SHOW_FOOT_ROW | HTMLFLAG_ADD_ROW_SEPARATORS);
     }
 
    public String getFileExtension()
 	{
 		return ".html";
 	}
 
 	public final long getFlags() { return flags; }
 	public final boolean flagIsSet(long flag) { return (flags & flag) == 0 ? false : true; }
 	public final void setFlag(long flag) { flags |= flag; }
 	public final void clearFlag(long flag) { flags &= ~flag; }
 	public final void updateFlag(long flag, boolean set) { if(set) flags |= flag; else flags &= ~flag; }
 
     public void produceReport(Writer writer, ReportContext rc, ResultSet rs, Object[][] data) throws SQLException, IOException
     {
 		long startTime = new java.util.Date().getTime();
 
         ReportFrame frame = rc.getReport().getFrame();
         ReportBanner banner = rc.getReport().getBanner();
 
 		boolean haveOuterTable = (frame != null || banner != null);
         if(haveOuterTable)
         {
             writer.write("<table "+ outerTableAttrs +">");
 			if(frame != null)
 			{
 				String heading = null;
 				SingleValueSource hvs = frame.getHeading();
 				if(hvs != null)
 					heading = hvs.getValue(rc);
 
				writer.write("<tr "+ frameHdRowAttrs +"><td " + frameHdRowAttrs + "><font "+ frameHdFontAttrs + "><b>" + heading + "</b></font></td></tr>");
 			}
 
 			if(banner != null)
 			{
 				writer.write("<tr "+ bannerRowAttrs +"><td>");
                 banner.produceHtml(writer, rc);
 				writer.write("</td></tr>");
 			}
 			writer.write("<tr><td bgcolor='white'>");
         }
 
 		writer.write("<table "+innerTableAttrs+">");
 		int startDataRow = 0;
 		if(flagIsSet(HTMLFLAG_SHOW_HEAD_ROW))
 		{
 			if(! rc.getReport().flagIsSet(StandardReport.REPORTFLAG_FIRST_DATA_ROW_HAS_HEADINGS))
 	    	{
 		    	produceHeadingRow(writer, rc, (Object[]) null);
 			}
 			else
 			{
 			    if(rs != null)
 					produceHeadingRow(writer, rc, rs);
 				else if(data.length > 0)
 				{
 					produceHeadingRow(writer, rc, data[0]);
 					startDataRow = 1;
 				}
 			}
 		}
         if(rs != null)
         {
             produceDataRows(writer, rc, rs);
         }
         else
         {
             produceDataRows(writer, rc, data, startDataRow);
         }
 
         if(flagIsSet(HTMLFLAG_SHOW_FOOT_ROW) && rc.getCalcsCount() > 0)
             produceFootRow(writer, rc);
         writer.write("</table>");
 
         if(haveOuterTable)
         {
             writer.write("</td></tr>");
             String footing = null;
             if (frame != null)
             {
                 SingleValueSource fvs = frame.getFooting();
                 if(fvs != null)
                 {
                     footing = fvs.getValue(rc);
                     writer.write("<tr "+ frameFtRowAttrs +"><td><font "+ frameFtFontAttrs + "><b>" + footing + "</b></font></td></tr>");
                 }
             }
             writer.write("</table>");
         }
 
 		com.xaf.log.LogManager.recordAccess((javax.servlet.http.HttpServletRequest) rc.getRequest(), null, this.getClass().getName(), rc.getLogId(), startTime);
     }
 
 	public void produceReport(Writer writer, ReportContext rc, ResultSet rs) throws SQLException, IOException
 	{
         produceReport(writer, rc, rs, null);
 	}
 
 	public void produceReport(Writer writer, ReportContext rc, Object[][] data) throws IOException
     {
         try
         {
             produceReport(writer, rc, null, data);
         }
         catch(SQLException e)
         {
             throw new RuntimeException("This should never happen.");
         }
     }
 
 	public void produceHeadingRow(Writer writer, ReportContext rc, Object[] headings) throws IOException
 	{
 		ReportColumnsList columns = rc.getColumns();
 		ReportContext.ColumnState[] states = rc.getStates();
 		int dataColsCount = columns.size();
 		int tableColsCount = (rc.getVisibleColsCount() * 2) + 1; // each column has "spacer" in between, first column as spacer before too
 
 		writer.write("<tr bgcolor="+frameHdTableRowBgcolorAttrs+"><td><font "+dataHdFontAttrs+">&nbsp;&nbsp;</font></td>");
 		if(headings == null)
 		{
 			for(int i = 0; i < dataColsCount; i++)
 			{
 				ReportColumn rcd = columns.getColumn(i);
 				if(states[i].isHidden())
 					continue;
 
 				writer.write("<td><font "+dataHdFontAttrs+"><b>"+ rcd.getHeading().getValue(rc) +"</b></font></td><td><font "+dataHdFontAttrs+">&nbsp;&nbsp;</font></td>");
 			}
 		}
 		else
 		{
 			for(int i = 0; i < dataColsCount; i++)
 			{
 				ReportColumn rcd = columns.getColumn(i);
 				if(states[i].isHidden())
 					continue;
 
 				Object heading = headings[rcd.getColIndexInArray()];
 				if(heading != null)
 					writer.write("<td><font "+dataHdFontAttrs+"><b>"+ heading.toString() +"</b></font></td><td><font "+dataHdFontAttrs+">&nbsp;&nbsp;</font></td>");
 				else
 					writer.write("<td><font "+dataHdFontAttrs+"></font></td><td><font "+dataHdFontAttrs+">&nbsp;&nbsp;</font></td>");
 			}
 		}
 		if(flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS))
 			writer.write("</tr><tr><td colspan='"+ tableColsCount +"'><img src='"+rowSepImgSrc+"' height='2' width='100%'></td></tr>");
 	}
 
 	public void produceHeadingRow(Writer writer, ReportContext rc, ResultSet rs) throws IOException, SQLException
 	{
 		ReportColumnsList columns = rc.getColumns();
 		ReportContext.ColumnState[] states = rc.getStates();
 		int dataColsCount = columns.size();
 		int tableColsCount = (rc.getVisibleColsCount() * 2) + 1; // each column has "spacer" in between, first column as spacer before too
 
 		if(! rs.next()) return;
 
 		writer.write("<tr bgcolor="+frameHdTableRowBgcolorAttrs+"><td><font "+dataHdFontAttrs+">&nbsp;&nbsp;</font></td>");
 		for(int i = 0; i < dataColsCount; i++)
 		{
 			ReportColumn rcd = columns.getColumn(i);
 			if(states[i].isHidden())
 				continue;
 
 			Object heading = rs.getString(rcd.getColIndexInResultSet());
 			if(heading != null)
 				writer.write("<td><font "+dataHdFontAttrs+"><b>"+ heading.toString() +"</b></font></td><td><font "+dataHdFontAttrs+">&nbsp;&nbsp;</font></td>");
 			else
 				writer.write("<td><font "+dataHdFontAttrs+"></font></td><td><font "+dataHdFontAttrs+">&nbsp;&nbsp;</font></td>");
 		}
 		if(flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS))
 			writer.write("</tr><tr><td colspan='"+ tableColsCount +"'><img src='"+rowSepImgSrc+"' height='2' width='100%'></td></tr>");
 	}
 
 	/*
 	  This method and the next one (produceDataRows with Object[][] data) are almost
 	  identical except for their data sources (ResultSet vs. Object[][]). Be sure to
 	  modify that method when this method changes, too
 	*/
 
 	public void produceDataRows(Writer writer, ReportContext rc, ResultSet rs) throws SQLException, IOException
 	{
         Report defn = rc.getReport();
 		ReportColumnsList columns = rc.getColumns();
 		ReportContext.ColumnState[] states = rc.getStates();
 
         boolean addRowSeps = flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS);
 		int rowsWritten = 0;
 		int dataColsCount = columns.size();
 		int tableColsCount = (rc.getVisibleColsCount() * 2) + 1;
 
 		ResultSetScrollState scrollState = rc.getScrollState();
 		boolean paging = scrollState != null;
 
         ResultSetMetaData rsmd = rs.getMetaData();
         int resultSetColsCount = rsmd.getColumnCount();
 
         while(rs.next())
         {
             // the reason why we need to copy the objects here is that
             // most JDBC drivers will only let data be ready one time; calling
             // the resultSet.getXXX methods more than once is problematic
             //
             Object[] rowData = new Object[resultSetColsCount];
             for(int i = 1; i <= resultSetColsCount; i++)
                 rowData[i-1] = rs.getObject(i);
 
             writer.write("<tr><td><font "+dataFontAttrs+">&nbsp;&nbsp;</td>");
             for(int i = 0; i < dataColsCount; i++)
             {
 				int rowNum = rs.getRow();
                 ReportColumn column = columns.getColumn(i);
 				ReportContext.ColumnState state = states[i];
 
                 if(state.isHidden())
                     continue;
 
                 String data =
                     state.flagIsSet(ReportColumn.COLFLAG_HASOUTPUTPATTERN) ?
                         state.getOutputFormat() :
                         column.getFormattedData(rc, rowNum, rowData, true);
 
                 String singleRow = "<td align='"+ ALIGN_ATTRS[column.getAlignStyle()] +"'><font "+dataFontAttrs+">"+
                     (state.flagIsSet(ReportColumn.COLFLAG_WRAPURL) ? "<a href='"+ state.getUrl() +"' "+ state.getUrlAnchorAttrs() +">"+ data +"</a>" : data) +
                     "</font></td><td><font "+dataFontAttrs+">&nbsp;&nbsp;</td>";
 
 				//writer.write(MessageFormat.format(singleRow, rowData));
                 writer.write(defn.replaceOutputPatterns(rc, rowNum, rowData, singleRow));
             }
 			writer.write("</tr>");
 
             if(addRowSeps)
                 writer.write("</tr><tr><td colspan='"+ tableColsCount +"'><img src='"+rowSepImgSrc+"' height='1' width='100%'></td></tr>");
 
 			rowsWritten++;
             if(paging && rc.endOfPage())
                 break;
         }
 
 		if(rowsWritten == 0)
 		{
             writer.write("</tr><tr><td colspan='"+ tableColsCount +"'><font "+dataFontAttrs+">No data found.</font></td></tr>");
 			if(paging)
 				scrollState.setNoMoreRows();
 		}
 		else if(paging)
 		{
 			scrollState.accumulateRowsProcessed(rowsWritten);
 		    if(rowsWritten < scrollState.getRowsPerPage())
 				scrollState.setNoMoreRows();
 		}
     }
 
 	/*
 	  This method and the previous one (produceDataRows with ResultSet) are almost
 	  identical except for their data sources (Object[][] vs. ResultSet). Be sure to
 	  modify that method when this method changes, too.
 	*/
 
 	public void produceDataRows(Writer writer, ReportContext rc, Object[][] data, int startDataRow) throws IOException
 	{
         Report defn = rc.getReport();
 		ReportColumnsList columns = rc.getColumns();
 		ReportContext.ColumnState[] states = rc.getStates();
 
         boolean addRowSeps = flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS);
 		int rowsWritten = 0;
 		int dataColsCount = columns.size();
 		int tableColsCount = (rc.getVisibleColsCount() * 2) + 1;
 
 		ResultSetScrollState scrollState = rc.getScrollState();
 		boolean paging = scrollState != null;
 
         for(int row = startDataRow; row < data.length; row++)
         {
             Object[] rowData = data[row];
 			int rowNum = row - startDataRow;
 
             writer.write("<tr><td><font "+dataFontAttrs+">&nbsp;&nbsp;</td>");
             for(int i = 0; i < dataColsCount; i++)
             {
                 ReportColumn column = columns.getColumn(i);
 				ReportContext.ColumnState state = states[i];
 
                 if(state.isHidden())
                     continue;
 
                 String colData =
                     state.flagIsSet(ReportColumn.COLFLAG_HASOUTPUTPATTERN) ?
                         state.getOutputFormat() :
                         column.getFormattedData(rc, rowNum, rowData, true);
 
                 String singleRow = "<td align='"+ ALIGN_ATTRS[column.getAlignStyle()] +"'><font "+dataFontAttrs+">"+
                     (state.flagIsSet(ReportColumn.COLFLAG_WRAPURL) ? "<a href='"+ state.getUrl() +"'"+ state.getUrlAnchorAttrs() +">"+ colData +"</a>" : colData) +
                     "</font></td><td><font "+dataFontAttrs+">&nbsp;&nbsp;</td>";
 
 				//writer.write(MessageFormat.format(singleRow, rowData));
                 writer.write(defn.replaceOutputPatterns(rc, rowNum, rowData, singleRow));
             }
 			writer.write("</tr>");
 
             if(addRowSeps)
                 writer.write("</tr><tr><td colspan='"+ tableColsCount +"'><img src='"+rowSepImgSrc+"' height='1' width='100%'></td></tr>");
 
 			rowsWritten++;
             if(paging && rc.endOfPage())
                 break;
         }
 
 		if(rowsWritten == 0)
 		{
             writer.write("</tr><tr><td colspan='"+ tableColsCount +"'><font "+dataFontAttrs+">No data found.</font></td></tr>");
 			if(paging)
 				scrollState.setNoMoreRows();
 		}
 		else if(paging)
 		{
 			scrollState.accumulateRowsProcessed(rowsWritten);
 		    if(rowsWritten < scrollState.getRowsPerPage())
 				scrollState.setNoMoreRows();
 		}
     }
 
     public void produceFootRow(Writer writer, ReportContext rc) throws SQLException, IOException
 	{
         int calcsCount = rc.getCalcsCount();
         if(calcsCount == 0)
             return;
 
         ReportContext.ColumnState[] states = rc.getStates();
 		ReportColumnsList columns = rc.getColumns();
 		int dataColsCount = columns.size();
 		int tableColsCount = (rc.getVisibleColsCount() * 2) + 1; // each column has "spacer" in between, first column as spacer before too
 
         if(flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS))
     		writer.write("</tr><tr><td colspan='"+ tableColsCount +"'><img src='"+rowSepImgSrc+"' height='1' width='100%'></td></tr>");
 		writer.write("<tr bgcolor='lightyellow'><td><font "+dataFtFontAttrs+">&nbsp;&nbsp;</font></td>");
 		for(int i = 0; i < dataColsCount; i++)
 		{
 			ReportColumn column = columns.getColumn(i);
 			if(states[i].isHidden())
 				continue;
 
 			writer.write("<td align='"+ ALIGN_ATTRS[column.getAlignStyle()] +"'><font "+dataFtFontAttrs+"><b>"+ column.getFormattedData(rc, states[i].getCalc()) +"</b></font></td><td><font "+dataFtFontAttrs+">&nbsp;&nbsp;</font></td>");
 		}
 		writer.write("</tr>");
     }
 
     public String getOuterTableAttrs()
     {
         return outerTableAttrs;
     }
 
     public void setOuterTableAttrs(String outerTableAttrs)
     {
         this.outerTableAttrs = outerTableAttrs;
     }
 
     public String getInnerTableAttrs()
     {
         return innerTableAttrs;
     }
 
     public void setInnerTableAttrs(String innerTableAttrs)
     {
         this.innerTableAttrs = innerTableAttrs;
     }
 
     public String getFrameHdRowAttrs()
     {
         return frameHdRowAttrs;
     }
 
     public void setFrameHdRowAttrs(String frameHdRowAttrs)
     {
         this.frameHdRowAttrs = frameHdRowAttrs;
     }
 
     public String getFrameHdFontAttrs()
     {
         return frameHdFontAttrs;
     }
 
     public void setFrameHdFontAttrs(String frameHdFontAttrs)
     {
         this.frameHdFontAttrs = frameHdFontAttrs;
     }
 
     public String getFrameHdTableRowBgcolorAttrs()
     {
         return frameHdTableRowBgcolorAttrs;
     }
 
     public void setFrameHdTableRowBgcolorAttrs(String frameHdTableRowBgcolorAttrs)
     {
         this.frameHdTableRowBgcolorAttrs = frameHdTableRowBgcolorAttrs;
     }
 
     public String getFrameFtRowAttrs()
     {
         return frameFtRowAttrs;
     }
 
     public void setFrameFtRowAttrs(String frameFtRowAttrs)
     {
         this.frameFtRowAttrs = frameFtRowAttrs;
     }
 
     public String getFrameFtFontAttrs()
     {
         return frameFtFontAttrs;
     }
 
     public void setFrameFtFontAttrs(String frameFtFontAttrs)
     {
         this.frameFtFontAttrs = frameFtFontAttrs;
     }
 
     public String getBannerRowAttrs()
     {
         return bannerRowAttrs;
     }
 
     public void setBannerRowAttrs(String bannerRowAttrs)
     {
         this.bannerRowAttrs = bannerRowAttrs;
     }
 
     public String getBannerItemFontAttrs()
     {
         return bannerItemFontAttrs;
     }
 
     public void setBannerItemFontAttrs(String bannerItemFontAttrs)
     {
         this.bannerItemFontAttrs = bannerItemFontAttrs;
     }
 
     public String getDataHdFontAttrs()
     {
         return dataHdFontAttrs;
     }
 
     public void setDataHdFontAttrs(String dataHdFontAttrs)
     {
         this.dataHdFontAttrs = dataHdFontAttrs;
     }
 
     public String getDataFontAttrs()
     {
         return dataFontAttrs;
     }
 
     public void setDataFontAttrs(String dataFontAttrs)
     {
         this.dataFontAttrs = dataFontAttrs;
     }
 
     public String getDataFtFontAttrs()
     {
         return dataFtFontAttrs;
     }
 
     public void setDataFtFontAttrs(String dataFtFontAttrs)
     {
         this.dataFtFontAttrs = dataFtFontAttrs;
     }
 
     public String getRowSepImgSrc()
     {
         return rowSepImgSrc;
     }
 
     public void setRowSepImgSrc(String rowSepImgSrc)
     {
         this.rowSepImgSrc = rowSepImgSrc;
     }
 }
