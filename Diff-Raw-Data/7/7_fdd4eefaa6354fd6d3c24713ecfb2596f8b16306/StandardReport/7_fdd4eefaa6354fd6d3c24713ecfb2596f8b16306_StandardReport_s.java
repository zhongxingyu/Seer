 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: StandardReport.java,v 1.2 2002-10-13 18:39:45 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.report;
 
 import java.net.URLEncoder;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.netspective.sparx.xaf.form.field.ReportField;
 import com.netspective.sparx.xaf.report.column.DialogFieldColumn;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 
 public class StandardReport implements Report
 {
     static public final int REPORTFLAG_INITIALIZED = 1;
     static public final int REPORTFLAG_HASPLACEHOLDERS = REPORTFLAG_INITIALIZED * 2;
     static public final int REPORTFLAG_FIRST_DATA_ROW_HAS_HEADINGS = REPORTFLAG_HASPLACEHOLDERS * 2;
     static public final int REPORTFLAG_HIDE_HEADING = REPORTFLAG_FIRST_DATA_ROW_HAS_HEADINGS * 2;
 
     private Object canvas;
     private String name;
     private ReportColumnsList columns = new ReportColumnsList();
     private boolean contentsFinalized;
    private ReportFrame frame = null;
     private ReportBanner banner = null;
     private int visibleColsCount = -1;
     private int flags;
     private boolean showHead = true;
 
     public boolean getHeadingDisplayFlag()
     {
         return showHead;
     }
 
     public void setHeadingDisplayFlag(boolean value)
     {
         showHead = value;
     }
 
     public StandardReport()
     {
     }
 
     public Object getCanvas()
     {
         return canvas;
     }
 
     public void setCanvas(Object value)
     {
         canvas = value;
     }
 
     public String getName()
     {
         return name;
     }
 
     public ReportFrame getFrame()
     {
         return frame;
     }
 
     public void setFrame(ReportFrame rf)
     {
         frame = rf;
     }
 
     public ReportBanner getBanner()
     {
         return banner;
     }
 
     public void setBanner(ReportBanner value)
     {
         banner = value;
     }
 
     public ReportColumnsList getColumns()
     {
         return columns;
     }
 
     public ReportColumn getColumn(int i)
     {
         return columns.getColumn(i);
     }
 
     public final long getFlags()
     {
         return flags;
     }
 
     public final boolean flagIsSet(long flag)
     {
         return (flags & flag) == 0 ? false : true;
     }
 
     public final void setFlag(long flag)
     {
         flags |= flag;
     }
 
     public final void clearFlag(long flag)
     {
         flags &= ~flag;
     }
 
     public final void updateFlag(long flag, boolean set)
     {
         if(set) flags |= flag; else flags &= ~flag;
     }
 
     public void initialize(ResultSet rs, Element defnElem) throws SQLException
     {
         if(flagIsSet(REPORTFLAG_INITIALIZED)) return;
 
         ResultSetMetaData rsmd = rs.getMetaData();
         int numColumns = rsmd.getColumnCount();
 
         columns.clear();
         for(int c = 1; c <= numColumns; c++)
         {
             ReportColumn colDefn = ReportColumnFactory.createReportColumn(rsmd, c);
             columns.add(colDefn);
         }
 
         if(defnElem != null)
             importFromXml(defnElem);
 
         finalizeContents();
         setFlag(REPORTFLAG_INITIALIZED);
     }
 
     public void initialize(ReportColumn[] cols, Element defnElem)
     {
         if(cols != null)
         {
             for(int c = 0; c <= cols.length; c++)
                 columns.add(cols[c]);
         }
 
         if(defnElem != null)
             importFromXml(defnElem);
 
         finalizeContents();
     }
 
     public void finalizeContents()
     {
         for(int c = 0; c < columns.size(); c++)
         {
             ReportColumn colDefn = columns.getColumn(c);
             colDefn.finalizeContents(this);
 
             if(colDefn.flagIsSet(ReportColumn.COLFLAG_HASPLACEHOLDERS))
                 setFlag(this.REPORTFLAG_HASPLACEHOLDERS);
         }
     }
 
     public void importFromXml(Element elem)
     {
         name = elem.getAttribute("name");
         if(name.length() == 0)
             name = "default";
 
        if(frame == null) frame = new ReportFrame();
         frame.importFromXml(elem);
 
         if(elem.getAttribute("first-row").equals("column-headings"))
             setFlag(REPORTFLAG_FIRST_DATA_ROW_HAS_HEADINGS);
 
         if(elem.getAttribute("first-row").equals("none"))
         {
             setHeadingDisplayFlag(false);
         }
 
         NodeList children = elem.getChildNodes();
         int columnIndex = 0;
 
         for(int n = 0; n < children.getLength(); n++)
         {
             Node node = children.item(n);
             if(node.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             String childName = node.getNodeName();
             if(childName.equals("column"))
             {
                 Element columnElem = (Element) node;
 
                 String value = columnElem.getAttribute("index");
                 String colBreak = columnElem.getAttribute("column-break");
                 if(colBreak.length() == 0)
                     colBreak = null;
                 if(value.length() > 0)
                     columnIndex = Integer.parseInt(value);
 
                 String colType = columnElem.getAttribute("type");
                 if(colType.length() == 0)
                     colType = null;
 
                 if(columns.size() <= columnIndex)
                 {
                     ReportColumn column = ReportColumnFactory.createReportColumn(colType);
                     column.importFromXml(columnElem);
                     column.setColIndexInArray(columnIndex);
                     if(column instanceof DialogFieldColumn)
                         ((DialogFieldColumn) column).setParentField((ReportField) canvas);
                     column.setBreak(colBreak);
                     columns.add(column);
                 }
                 else
                 {
                     ReportColumn column = null;
                     if(colType == null)
                     {
                         column = columns.getColumn(columnIndex);
                         column.setBreak(colBreak);
                     }
                     else
                     {
                         column = ReportColumnFactory.createReportColumn(colType);
                         column.setColIndexInArray(columnIndex);
                         if(column instanceof DialogFieldColumn)
                             ((DialogFieldColumn) column).setParentField((ReportField) canvas);
                         column.setBreak(colBreak);
                         columns.set(columnIndex, column);
                     }
                     column.importFromXml(columnElem);
                 }
 
                 columnIndex++;
             }
             else if(childName.equals("banner"))
             {
                 banner = new ReportBanner();
                 banner.importFromXml((Element) node);
             }
             else if(childName.equals("banner-item"))
                 throw new RuntimeException("The <banner-item> element is now called <item> and must be placed inside a <banner> element (since Version 1.2.8 Build 51)");
         }
 
         // if a record add caption/url was provided but no banner was created, go ahead and generate a banner automatically
         // banners are automatically hidden by record-viewer and shown by record-editor skins
         if(frame != null && banner == null)
         {
             SingleValueSource recordAddCaption = frame.getRecordAddCaption();
             SingleValueSource recordAddUrl = frame.getRecordAddUrlFormat();
             if(recordAddCaption != null && recordAddUrl != null)
             {
                 banner = new ReportBanner();
                 banner.addItem(new ReportBanner.Item(recordAddCaption, recordAddUrl, ValueSourceFactory.getSingleValueSource("config-expr:${sparx.shared.images-url}/design/action-edit-add.gif")));
             }
         }
     }
 
     /**
      * Replace contents from rowData using the String row as a template. Each
      * occurrence of ${#} will be replaced with rowNum and occurrences of ${x}
      * where x is a number between 0 and rowData.length will be replaced with
      * the contents of rowData[x]. NOTE: this function needs to be
      * improved from both an elegance and performance perspective.
      */
 
     public String replaceOutputPatterns(ReportContext rc, long rowNum, Object[] rowData, String row)
     {
         StringBuffer sb = new StringBuffer();
         int i = 0;
         int prev = 0;
         boolean encode = false;
 
         int pos = 0;
         int pos1 = row.indexOf("$", prev);
         int pos2 = row.indexOf("%", prev);
         if(pos2 != -1)
         {
             if(pos1 != -1 && pos2 > pos1)
             {
                 pos = pos1;
             }
             else
             {
                 encode = true;
                 pos = pos2;
             }
         }
         else
         {
             encode = false;
             pos = pos1;
         }
 
         while(pos >= 0)
         {
             if(pos > 0)
             {
                 // append the substring before the '$' or '%' character
                 sb.append(row.substring(prev, pos));
             }
             if(pos == (row.length() - 1))
             {
                 if(encode)
                     sb.append('%');
                 else
                     sb.append('$');
                 prev = pos + 1;
             }
             else if(row.charAt(pos + 1) != '{')
             {
                 sb.append(row.charAt(pos));
                 sb.append(row.charAt(pos + 1));
                 prev = pos + 2;
             }
             else
             {
                 int endName = row.indexOf('}', pos);
                 if(endName < 0)
                 {
                     throw new RuntimeException("Syntax error in: " + row);
                 }
                 String expression = row.substring(pos + 2, endName);
 
                 if(expression.equals("#"))
                     sb.append(rowNum);
                 else
                 {
                     try
                     {
                         int colIndex = Integer.parseInt(expression);
                         if(encode)
                             sb.append(URLEncoder.encode(columns.getColumn(colIndex).getFormattedData(rc, rowNum, rowData, false)));
                         else
                             sb.append(columns.getColumn(colIndex).getFormattedData(rc, rowNum, rowData, false));
                     }
                     catch(NumberFormatException e)
                     {
                         SingleValueSource vs = ValueSourceFactory.getSingleValueSource(expression);
                         if(vs == null)
                             sb.append("Invalid: '" + expression + "'");
                         else
                             sb.append(vs.getValue(rc));
                     }
                 }
 
                 prev = endName + 1;
             }
 
             pos1 = row.indexOf("$", prev);
             pos2 = row.indexOf("%", prev);
             if(pos2 != -1)
             {
                 if(pos1 != -1 && pos2 > pos1)
                 {
                     pos = pos1;
                 }
                 else
                 {
                     encode = true;
                     pos = pos2;
                 }
             }
             else
             {
                 encode = false;
                 pos = pos1;
             }
         }
 
         if(prev < row.length()) sb.append(row.substring(prev));
         return sb.toString();
     }
 
     public void makeStateChanges(ReportContext rc, ResultSet rs)
     {
         List listeners = rc.getListeners();
         for(int i = 0; i < listeners.size(); i++)
             ((ReportContextListener) listeners.get(i)).makeReportStateChanges(rc, rs);
     }
 
     public void makeStateChanges(ReportContext rc, Object[][] data)
     {
         List listeners = rc.getListeners();
         for(int i = 0; i < listeners.size(); i++)
             ((ReportContextListener) listeners.get(i)).makeReportStateChanges(rc, data);
     }
 }
