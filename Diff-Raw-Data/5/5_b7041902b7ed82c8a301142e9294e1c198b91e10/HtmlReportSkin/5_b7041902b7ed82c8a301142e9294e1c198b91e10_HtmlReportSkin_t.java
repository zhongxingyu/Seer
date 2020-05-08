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
 * $Id: HtmlReportSkin.java,v 1.4 2003-03-19 19:14:59 aye.thu Exp $
  */
 
 package com.netspective.sparx.xaf.theme;
 
 import com.netspective.sparx.util.config.Configuration;
 import com.netspective.sparx.util.config.ConfigurationManagerFactory;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 import com.netspective.sparx.xaf.report.Report;
 import com.netspective.sparx.xaf.report.ReportBanner;
 import com.netspective.sparx.xaf.report.ReportColumn;
 import com.netspective.sparx.xaf.report.ReportColumnsList;
 import com.netspective.sparx.xaf.report.ReportContext;
 import com.netspective.sparx.xaf.report.ReportFrame;
 import com.netspective.sparx.xaf.report.ReportSkin;
 import com.netspective.sparx.xaf.report.StandardReport;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.xaf.sql.ResultSetScrollState;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class HtmlReportSkin implements ReportSkin
 {
     static public final int HTMLFLAG_SHOW_BANNER = 1;
     static public final int HTMLFLAG_SHOW_HEAD_ROW = HTMLFLAG_SHOW_BANNER * 2;
     static public final int HTMLFLAG_SHOW_FOOT_ROW = HTMLFLAG_SHOW_HEAD_ROW * 2;
     static public final int HTMLFLAG_ADD_ROW_SEPARATORS = HTMLFLAG_SHOW_FOOT_ROW * 2;
     static public final int HTMLFLAG_FULL_WIDTH = HTMLFLAG_ADD_ROW_SEPARATORS * 2;
     static public final int HTMLFLAG_STARTCUSTOM = HTMLFLAG_FULL_WIDTH * 2;
 
     static public final String[] ALIGN_ATTRS = {"LEFT", "CENTER", "RIGHT"};
 
     /* NOTE: The PanelTag custom tag produces a panel similar to this one so be sure to make appropriate changes there */
 
     protected int flags;
     protected String frameHdTableAttrs = "cellspacing=0 cellpadding=0 width='100%' border=0";
     protected String frameHdRowAttrs = "bgcolor='#666666' height=15";
     protected String frameHdRowSpacerAttrs = "bgcolor='#666666' height=2";
     protected String frameHdCellAttrs = "bgcolor='#666666' width='40%'";
     protected String frameHdInfoCellAttrs = "bgcolor='white' align='right'";
     protected String frameHdFontAttrs = "face='tahoma,arial,helvetica' size=1 color=white";
     protected String frameHdInfoFontAttrs = "face='tahoma,arial,helvetica' size=1";
     protected String outerTableAttrs = "border=0 cellspacing=1 cellpadding=2 bgcolor='#666666' width='100%' ";
     protected String innerTableAttrs = "cellpadding='2' cellspacing='0' border='0' width='100%'";
     protected SingleValueSource frameHdTabImgSrcValueSource = ValueSourceFactory.getSingleValueSource("config-expr:${sparx.shared.images-url}/tabs/transparent-triangle.gif");
     protected SingleValueSource frameHdSpacerImgSrcValueSource = ValueSourceFactory.getSingleValueSource("config-expr:${sparx.shared.images-url}/tabs/black-on-lgray/spacer.gif");
     protected String frameFtRowAttrs = "bgcolor='#EEEEEE'";
     protected String frameFtFontAttrs = "face='tahoma,arial,helvetica' size=2 color='#000000'";
     protected String bannerRowAttrs = "bgcolor='#DDDDDD'";
     protected String bannerItemFontAttrs = "face='arial,helvetica' size=2";
     protected String dataHdRowAttrs = "bgcolor='#EEEEEE'";
     protected String dataHdCellAttrs = "style='border-bottom: 2px solid #999999'";
     protected String dataHdFontAttrs = "face='tahoma,arial' size='2' style='font-size: 8pt;' color='navy'";
     protected String dataEvenRowAttrs = "bgcolor='#EEEEEE'";
     protected String dataOddRowAttrs = "bgcolor='#FFFFFF'";
     protected String dataFontAttrs = "face='tahoma,arial' size='2' style='font-size: 8pt;'";
     protected String dataFtRowAttrs = "bgcolor='#EEEEEE'";
     protected String dataFtFontAttrs = "face='tahoma,arial' size='2' style='font-size: 8pt;' color='navy'";
     protected String rowSepImgSrc = "/shared/resources/images/design/bar.gif";
     protected String sortAscImgSrc = "/shared/resources/images/navigate/triangle-up-blue.gif";
     protected String sortDescImgSrc = "/shared/resources/images/navigate/triangle-down-lblue.gif";
     private int rowDecoratorPrependColsCount = 0;
     private int rowDecoratorAppendColsCount = 0;
     protected SingleValueSource addDataText = ValueSourceFactory.getSingleValueSource("create-record-add-text:Record");
     protected SingleValueSource editDataText = ValueSourceFactory.getSingleValueSource("config-expr:<img src='${sparx.shared.images-url}/design/action-edit-update.gif' border=0>");
     protected SingleValueSource deleteDataText = ValueSourceFactory.getSingleValueSource("config-expr:<img src='${sparx.shared.images-url}/design/action-edit-remove.gif' border=0>");
     private String imgPath;
 
     public HtmlReportSkin(boolean fullWidth)
     {
         setFlag(HTMLFLAG_SHOW_BANNER | HTMLFLAG_SHOW_HEAD_ROW | HTMLFLAG_SHOW_FOOT_ROW | HTMLFLAG_ADD_ROW_SEPARATORS);
         if(fullWidth)
             setFlag(HTMLFLAG_FULL_WIDTH);
     }
 
     public String getFileExtension()
     {
         return ".html";
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
 
     public void produceHeadingExtras(Writer writer, ReportContext rc, ReportFrame frame) throws IOException
     {
         ArrayList items = frame.getItems();
 
        if(items != null && items.size() > 0)
         {
             if (imgPath == null)
             {
                 Theme theme = SkinFactory.getInstance().getCurrentTheme(rc);
                 imgPath = ((HttpServletRequest)rc.getRequest()).getContextPath() + theme.getCurrentStyle().getImagePath();
             }
 
             int colCount = 0;
 
             // create a temporary string buffer for the HTML of the heading action items
             StringBuffer itemBuffer = new StringBuffer();
             for (int i=0; items != null && i < items.size(); i++)
             {
                 if (i != 0)
                 {
                     itemBuffer.append("            <td bgcolor=\"white\"><img src=\"" + imgPath + "/login/spacer.gif\" width=\"5\" height=\"5\"></td>");
                     colCount++;
                 }
                 ReportFrame.Item item = (ReportFrame.Item) items.get(i);
                 SingleValueSource itemUrl = item.getUrl();
                 SingleValueSource itemCaption = item.getCaption();
                 SingleValueSource itemIcon = item.getIcon();
                 if (itemIcon != null)
                 {
                     // icon for this item is defined so use the passed in image INSTEAD of using the CSS based background image
                     itemBuffer.append("            <td class=\"panel-frame-action-item-output\"><img src=\"" + itemIcon.getValue(rc) + "\" height=\"14\" width=\"17\" border=\"0\"></td>");
                     colCount++;
                 }
                 else
                 {
                     itemBuffer.append("            <td class=\"panel-frame-action-item-output\" width=\"17\"><img src=\"" + imgPath +
                         "/panel/output/spacer.gif\" alt=\"\" height=\"14\" width=\"17\" border=\"0\"></td>");
                     colCount++;
                 }
                 itemBuffer.append("            <td class=\"panel-frame-action-box-output\">" +
                         "<a class=\"panel-frame-action-output\" href=\""+ itemUrl.getValue(rc) + "\">&nbsp;" +
                         itemCaption.getValue(rc) + "&nbsp;</a></td>");
                 colCount++;
             }
 
             writer.write("<td nowrap>\n");
             writer.write("    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("        <tr>\n");
             writer.write("            <td bgcolor=\"white\" width=\"100%\" colspan=\"" + colCount + "\">" +
                     "<img src=\"" + imgPath + "/login/spacer.gif\" height=\"5\"></td>\n");
             writer.write("        </tr>\n");
             if (itemBuffer.length() > 0)
             {
                 writer.write("        <tr>\n");
                 writer.write(itemBuffer.toString());
                 writer.write("        </tr>\n");
             }
             writer.write("        </tr>\n  ");
             writer.write("    </table>\n");
             writer.write("</td>\n");
         }
     }
 
     public ReportBanner getReportBanner(ReportContext rc)
     {
         return rc.getReport().getBanner();
     }
 
     public ReportFrame getReportFrame(ReportContext rc)
     {
         return rc.getReport().getFrame();
     }
 
     /**
      * Produce the report
      * @param writer
      * @param rc
      * @param rs
      * @param data
      * @throws SQLException
      * @throws IOException
      */
     public void produceReport(Writer writer, ReportContext rc, ResultSet rs, Object[][] data) throws SQLException, IOException
     {
         long startTime = new java.util.Date().getTime();
 
         ReportFrame frame = getReportFrame(rc);
         ReportBanner banner = getReportBanner(rc);
 
         Theme theme = SkinFactory.getInstance().getCurrentTheme(rc);
         String imgPath = ((HttpServletRequest)rc.getRequest()).getContextPath() +  theme.getCurrentStyle().getImagePath();
 
         writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" nowrap ");
         if(flagIsSet(HTMLFLAG_FULL_WIDTH))
             writer.write("width='100%' ");
         writer.write(">\n");
 
         boolean haveOuterTable = (frame.hasHeadingOrFooting() || banner != null);
         if(frame.hasHeadingOrFooting())
         {
             String heading = null;
             SingleValueSource hvs = frame.getHeading();
             if(hvs != null)
                 heading = hvs.getValue(rc);
 
             if(heading != null)
             {
                 writer.write("<tr>\n");
                 writer.write("    <td class=\"panel-output\">\n");
                 writer.write("    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" nowrap>\n");
                 writer.write("        <tr>\n");
                 if (frame.allowCollapse())
                 {
                     if (frame.isCollapsed())
                         writer.write("            <td class=\"panel-frame-heading-action-expand-output\"   align=\"left\" valign=\"middle\" nowrap width=\"17\">" +
                             "<img src=\"" + imgPath + "/panel/output/spacer.gif\" alt=\"\" height=\"5\" width=\"17\" border=\"0\"></td>");
                     else
                         writer.write("            <td class=\"panel-frame-heading-action-collapse-output\"   align=\"left\" valign=\"middle\" nowrap width=\"17\">" +
                             "<img src=\"" + imgPath + "/panel/output/spacer.gif\" alt=\"\" height=\"5\" width=\"17\" border=\"0\"></td>");
                 }
                 else
                 {
                     writer.write("            <td class=\"panel-frame-heading-action-left-blank-output\" align=\"left\" valign=\"middle\" nowrap width=\"17\">" +
                         "<img src=\"" + imgPath + "/panel/output/spacer.gif\" alt=\"\" height=\"5\" width=\"17\" border=\"0\"></td>\n");
                 }
                 writer.write("            <td class=\"panel-frame-heading-output\" align=\"left\" valign=\"middle\" nowrap>" + heading +
                         "</td>\n");
                 writer.write("            <td class=\"panel-frame-heading-action-right-blank-output\" align=\"center\" valign=\"middle\" nowrap width=\"17\">" +
                     "<img src=\"" + imgPath + "/panel/output/spacer.gif\" alt=\"\" height=\"5\" width=\"17\" border=\"0\"></td>\n");
                 writer.write("            <td class=\"panel-frame-mid-output\" align=\"right\" valign=\"top\" nowrap width=\"100%\">" +
                     "<img src=\"" + imgPath + "/panel/output/spacer.gif\" alt=\"\" height=\"5\" width=\"100%\" border=\"0\"></td>\n");
                 writer.write("            <td class=\"panel-frame-end-cap-output\" align=\"right\" valign=\"top\" nowrap width=\"2\"></td>\n");
                 produceHeadingExtras(writer, rc, frame);
                 writer.write("        </tr>\n");
                 writer.write("    </table>\n");
                 writer.write("    </td>\n");
                 writer.write("</tr>\n");
             }
 
             if(banner != null)
             {
                 produceBannerRow(writer, rc);
             }
         }
 
         writer.write("<tr>\n" +
                 "    <td class=\"panel-content-output\">\n");
         writer.write("    <table class=\"report\" width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\">\n");
         int startDataRow = 0;
         if(flagIsSet(HTMLFLAG_SHOW_HEAD_ROW) && !rc.getReport().flagIsSet(StandardReport.REPORTFLAG_HIDE_HEADING))
         {
             if(!rc.getReport().flagIsSet(StandardReport.REPORTFLAG_FIRST_DATA_ROW_HAS_HEADINGS))
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
         writer.write("    </table>\n");
         writer.write("    </td>\n");
         writer.write("</tr>\n");
         if(frame.hasHeadingOrFooting())
         {
             SingleValueSource fvs = frame.getFooting();
             if(fvs != null)
             {
                 writer.write("<tr>\n");
                 writer.write("    <td class=\"panel-banner-footer-output\">" + fvs.getValue(rc) + "</td>\n");
                 writer.write("</tr>\n");
             }
         }
 
         writer.write("</table>\n");
         com.netspective.sparx.util.log.LogManager.recordAccess((javax.servlet.http.HttpServletRequest) rc.getRequest(), null, this.getClass().getName(), rc.getLogId(), startTime);
     }
 
     /**
      * Displays the report banner html. Utilizes the THEME
      * @param writer
      * @param rc
      * @throws IOException
      */
     private void produceBannerRow(Writer writer, ReportContext rc) throws IOException
     {
         ReportBanner banner = getReportBanner(rc);
         if (banner == null)
             return;
 
         writer.write("<tr><td class=\"panel-banner-output\">\n");
         ReportBanner.Items items = banner.getItems();
         short style = items.getStyle();
         if (style == ReportBanner.Items.LAYOUTSTYLE_HORIZONTAL)
         {
             for (int i=0; items != null && i < items.size(); i++)
             {
                 ReportBanner.Item item = (ReportBanner.Item) items.get(i);
                 SingleValueSource itemUrl = item.getUrl();
                 SingleValueSource itemCaption = item.getCaption();
                 SingleValueSource itemIcon = item.getIcon();
                 String caption = itemCaption != null ? (itemUrl != null ? ("<a href='" + itemUrl.getValue(rc) + "'>" +
                         itemCaption.getValue(rc) + "</a>") : itemCaption.getValue(rc)) : null;
                 if(i > 0)
                     writer.write(", ");
                 if(itemIcon != null)
                     writer.write("<img src='" + itemIcon.getValue(rc) + "'>");
                 writer.write(caption);
             }
         }
         else
         {
             writer.write("<table border=0 cellspacing=0>");
             for(int i = 0; items != null && i < items.size(); i++)
             {
                 ReportBanner.Item item = (ReportBanner.Item) items.get(i);
                 SingleValueSource itemUrl = item.getUrl();
                 SingleValueSource itemCaption = item.getCaption();
                 SingleValueSource itemIcon = item.getIcon();
                 ReportBanner.Items childItems = item.getChildItems();
                 String caption = itemCaption != null ? (itemUrl != null ? ("<a href='" + itemUrl.getValue(rc) + "'>" + itemCaption.getValue(rc) + "</a>") : itemCaption.getValue(rc)) : null;
 
                 writer.write("<tr><td>");
                 writer.write(itemIcon != null ? "<img src='" + itemIcon.getValue(rc) + "'>" : "-");
                 writer.write("</td>");
                 writer.write("<td>");
                 if(caption != null)
                     writer.write(caption);
                 if(childItems != null)
                     childItems.produceHtml(writer, rc);
                 writer.write("</td>");
                 writer.write("</tr>");
             }
             writer.write("</table>");
         }
         writer.write("</td></tr>\n");
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
 
     private int getTableColumnsCount(ReportContext rc)
     {
         return (rc.getVisibleColsCount() * 2) +
                (getRowDecoratorPrependColsCount(rc) * 2) +
                (getRowDecoratorAppendColsCount(rc) * 2) +
                + 1; // each column has "spacer" in between, first column as spacer before too
     }
 
     public void produceHeadingRowDecoratorPrepend(Writer writer, ReportContext rc) throws IOException
     {
     }
 
     public void produceHeadingRowDecoratorAppend(Writer writer, ReportContext rc) throws IOException
     {
     }
 
     public void produceDataRowDecoratorPrepend(Writer writer, ReportContext rc, int rowNum, Object[] rowData, boolean isOddRow) throws IOException
     {
     }
 
     public void produceDataRowDecoratorAppend(Writer writer, ReportContext rc, int rowNum, Object[] rowData, boolean isOddRow) throws IOException
     {
     }
 
     public void produceHeadingRow(Writer writer, ReportContext rc, Object[] headings) throws IOException
     {
         ReportColumnsList columns = rc.getColumns();
         ReportContext.ColumnState[] states = rc.getStates();
         int dataColsCount = columns.size();
 
         Configuration appConfig = ConfigurationManagerFactory.getDefaultConfiguration(rc.getServletContext());
         String sortAscImgTag = " <img src=\""+ appConfig.getTextValue(rc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "report.sort-ascending-img-src", getSortAscImgSrc()) + "\" border=0>";
         String sortDescImgTag = " <img src=\""+ appConfig.getTextValue(rc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "report.sort-descending-img-src", getSortDescImgSrc()) + "\" border=0>";
         writer.write("<tr>");
         produceHeadingRowDecoratorPrepend(writer, rc);
 
         if(headings == null)
         {
             for(int i = 0; i < dataColsCount; i++)
             {
                 ReportColumn rcd = columns.getColumn(i);
                 ReportContext.ColumnState rcs = rc.getState(i);
                 if(states[i].isHidden())
                     continue;
 
                 String colHeading = rcd.getHeading().getValue(rc);
                 SingleValueSource headingAnchorAttrs = rcd.getHeadingAnchorAttrs();
                 if(headingAnchorAttrs != null)
                     colHeading = "<a " + headingAnchorAttrs.getValue(rc) + ">" + colHeading + "</a>";
                 if(rcs.flagIsSet(ReportColumn.COLFLAG_SORTED_ASCENDING))
                     colHeading += sortAscImgTag;
                 if(rcs.flagIsSet(ReportColumn.COLFLAG_SORTED_DESCENDING))
                     colHeading += sortDescImgTag;
                 writer.write("        <td class=\"report-field\" nowrap>" + colHeading  + "</td>");
             }
         }
         else
         {
             for(int i = 0; i < dataColsCount; i++)
             {
                 ReportColumn rcd = columns.getColumn(i);
                 ReportContext.ColumnState rcs = rc.getState(i);
                 if(states[i].isHidden())
                     continue;
 
                 Object heading = headings[rcd.getColIndexInArray()];
                 if(heading != null)
                 {
                     String colHeading = heading.toString();
                     SingleValueSource headingAnchorAttrs = rcd.getHeadingAnchorAttrs();
                     if(headingAnchorAttrs != null)
                         colHeading = "<a " + headingAnchorAttrs.getValue(rc) + ">" + colHeading + "</a>";
                     if(rcs.flagIsSet(ReportColumn.COLFLAG_SORTED_ASCENDING))
                         colHeading += sortAscImgTag;
                     if(rcs.flagIsSet(ReportColumn.COLFLAG_SORTED_DESCENDING))
                         colHeading += sortDescImgTag;
 
                     writer.write("        <td class=\"report-field\" nowrap>" + colHeading  + "</td>");
                 }
                 else
                     writer.write("        <td class=\"report-field\" nowrap>&nbsp;&nbsp;</td>");
             }
         }
 
         produceHeadingRowDecoratorAppend(writer, rc);
 
         writer.write("</tr>");
         /*
         if(flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS))
             writer.write("</tr><tr><td colspan='" + tableColsCount + "'><img src='" + rowSepImgSrc + "' height='2' width='100%'></td></tr>");
         */
     }
 
     public void produceHeadingRow(Writer writer, ReportContext rc, ResultSet rs) throws IOException, SQLException
     {
         ReportColumnsList columns = rc.getColumns();
         ReportContext.ColumnState[] states = rc.getStates();
         int dataColsCount = columns.size();
         int tableColsCount = getTableColumnsCount(rc); // each column has "spacer" in between, first column has spacer before too
 
         if(!rs.next()) return;
 
         Configuration appConfig = ConfigurationManagerFactory.getDefaultConfiguration(rc.getServletContext());
         String rowSepImgSrc = appConfig.getTextValue(rc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "report.row-sep-img-src", getRowSepImgSrc());
         String sortAscImgTag = "<img src=\""+ appConfig.getTextValue(rc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "report.sort-ascending-img-src", getSortAscImgSrc()) + "\" border=0>";
         String sortDescImgTag = "<img src=\""+ appConfig.getTextValue(rc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "report.sort-descending-img-src", getSortDescImgSrc()) + "\" border=0>";
 
         writer.write("<tr>");
         produceHeadingRowDecoratorPrepend(writer, rc);
 
         for(int i = 0; i < dataColsCount; i++)
         {
             ReportColumn rcd = columns.getColumn(i);
             ReportContext.ColumnState rcs = rc.getState(i);
             if(states[i].isHidden())
                 continue;
 
             Object heading = rs.getString(rcd.getColIndexInResultSet());
             if(heading != null)
             {
                 String colHeading = heading.toString();
                 SingleValueSource headingAnchorAttrs = rcd.getHeadingAnchorAttrs();
                 if(headingAnchorAttrs != null)
                     colHeading = "<a " + headingAnchorAttrs.getValue(rc) + ">" + colHeading + "</a>";
                 if(rcs.flagIsSet(ReportColumn.COLFLAG_SORTED_ASCENDING))
                     colHeading += sortAscImgTag;
                 if(rcs.flagIsSet(ReportColumn.COLFLAG_SORTED_DESCENDING))
                     colHeading += sortDescImgTag;
 
                 writer.write("        <td class=\"report-field\" nowrap>" + colHeading  + "</td>");
             }
             else
                 writer.write("        <td class=\"report-field\" nowrap>&nbsp;&nbsp;</td>");
         }
 
         produceHeadingRowDecoratorAppend(writer, rc);
 
         /*
         if(flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS))
             writer.write("</tr><tr><td colspan='" + tableColsCount + "'><img src='" + rowSepImgSrc + "' height='2' width='100%'></td></tr>");
         else
             writer.write("</tr>");
             */
     }
 
     /*
       This method and the next one (produceDataRows with Object[][] data) are almost
       identical except for their data sources (ResultSet vs. Object[][]). Be sure to
       modify that method when this method changes, too
     */
 
     public void produceDataRows(Writer writer, ReportContext rc, ResultSet rs) throws SQLException, IOException
     {
         Configuration appConfig = ConfigurationManagerFactory.getDefaultConfiguration(rc.getServletContext());
         String rowSepImgSrc = appConfig.getTextValue(rc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "report.row-sep-img-src", getRowSepImgSrc());
 
         Report defn = rc.getReport();
         ReportColumnsList columns = rc.getColumns();
         ReportContext.ColumnState[] states = rc.getStates();
 
         boolean addRowSeps = flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS);
         int rowsWritten = 0;
         int dataColsCount = columns.size();
         int tableColsCount = getTableColumnsCount(rc);
 
         ResultSetScrollState scrollState = rc.getScrollState();
         boolean paging = scrollState != null;
 
         ResultSetMetaData rsmd = rs.getMetaData();
         int resultSetColsCount = rsmd.getColumnCount();
         boolean isOddRow = false;
 
         while(rs.next())
         {
             // the reason why we need to copy the objects here is that
             // most JDBC drivers will only let data be ready one time; calling
             // the resultSet.getXXX methods more than once is problematic
             //
             Object[] rowData = new Object[resultSetColsCount];
             for(int i = 1; i <= resultSetColsCount; i++)
                 rowData[i - 1] = rs.getObject(i);
 
             isOddRow = ! isOddRow;
             int rowNum = rs.getRow();
 
             writer.write("<tr>");
             produceDataRowDecoratorPrepend(writer, rc, rowNum, rowData, isOddRow);
 
             for(int i = 0; i < dataColsCount; i++)
             {
 
                 ReportColumn column = columns.getColumn(i);
                 ReportContext.ColumnState state = states[i];
 
                 if(state.isHidden())
                     continue;
 
                 String data =
                         state.flagIsSet(ReportColumn.COLFLAG_HASOUTPUTPATTERN) ?
                         state.getOutputFormat() :
                         column.getFormattedData(rc, rowNum, rowData, true);
 
                 String singleRow = "<td " + (isOddRow ? "class=\"report\"" : "class=\"report-alternative\"") + ">" +
                         (state.flagIsSet(ReportColumn.COLFLAG_WRAPURL) ? "<a href=\"" + state.getUrl() + "\" " + state.getUrlAnchorAttrs() + ">" +
                         data + "</a>" : data) +
                         "&nbsp;</td>";
                 writer.write(defn.replaceOutputPatterns(rc, rowNum, rowData, singleRow));
             }
 
             produceDataRowDecoratorAppend(writer, rc, rowNum, rowData, isOddRow);
 
             rowsWritten++;
             if(paging && rc.endOfPage())
                 break;
         }
 
         if(rowsWritten == 0)
         {
             writer.write("<tr><td class=\"report-summary\" colspan='" + tableColsCount + "'>No data found.</td></tr>");
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
         Configuration appConfig = ConfigurationManagerFactory.getDefaultConfiguration(rc.getServletContext());
         String rowSepImgSrc = appConfig.getTextValue(rc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "report.row-sep-img-src", getRowSepImgSrc());
 
         Report defn = rc.getReport();
         ReportColumnsList columns = rc.getColumns();
         ReportContext.ColumnState[] states = rc.getStates();
 
         boolean addRowSeps = flagIsSet(HTMLFLAG_ADD_ROW_SEPARATORS);
         int rowsWritten = 0;
         int dataColsCount = columns.size();
         int tableColsCount = getTableColumnsCount(rc);
 
         ResultSetScrollState scrollState = rc.getScrollState();
         boolean paging = scrollState != null;
         boolean isOddRow = false;
 
         for(int row = startDataRow; row < data.length; row++)
         {
             Object[] rowData = data[row];
             isOddRow = ! isOddRow;
             int rowNum = row - startDataRow;
 
             writer.write("<tr "+ (isOddRow ? dataOddRowAttrs : dataEvenRowAttrs) +"><td><font " + dataFontAttrs + ">&nbsp;&nbsp;</td>");
 
             produceDataRowDecoratorPrepend(writer, rc, rowNum, rowData, isOddRow);
 
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
 
                 String dataTagsBegin = "";
                 String dataTagsEnd = "";
                 if(column.flagIsSet(ReportColumn.COLFLAG_NOWORDBREAKS))
                 {
                     dataTagsBegin = "<nobr>";
                     dataTagsEnd = "</nobr>";
                 }
                 String singleRow = "<td " + (isOddRow ? "class=\"report\"" : "class=\"report-alternative\"") + ">" +
                         (state.flagIsSet(ReportColumn.COLFLAG_WRAPURL) ? "<a href=\"" + state.getUrl() + "\" " + state.getUrlAnchorAttrs() + ">" + colData + "</a>" : colData) +
                         "</td>";
                 /*
                 String singleRow = "<td align='" + ALIGN_ATTRS[column.getAlignStyle()] + "'>"+ dataTagsBegin +"<font " + dataFontAttrs + ">" +
                         (state.flagIsSet(ReportColumn.COLFLAG_WRAPURL) ? "<a href='" + state.getUrl() + "'" + state.getUrlAnchorAttrs() + ">" + colData + "</a>" : colData) +
                         "</font>"+ dataTagsEnd +"</td><td><font " + dataFontAttrs + ">&nbsp;&nbsp;</td>";
                 */
                 writer.write(defn.replaceOutputPatterns(rc, rowNum, rowData, singleRow));
             }
 
             produceDataRowDecoratorAppend(writer, rc, rowNum, rowData, isOddRow);
 
             /*
             if(addRowSeps)
                 writer.write("</tr><tr><td colspan='" + tableColsCount + "'><img src='" + rowSepImgSrc + "' height='1' width='100%'></td></tr>");
             else
                 writer.write("</tr>");
             */
             rowsWritten++;
             if(paging && rc.endOfPage())
                 break;
         }
 
         if(rowsWritten == 0)
         {
             //writer.write("</tr><tr><td colspan='" + tableColsCount + "'><font " + dataFontAttrs + ">No data found.</font></td></tr>");
             writer.write("<tr><td class=\"report-summary\" colspan='" + tableColsCount + "'>No data found.</td></tr>");
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
         int tableColsCount = getTableColumnsCount(rc);
 
         writer.write("<tr>");
         for(int i = 0; i < dataColsCount; i++)
         {
             ReportColumn column = columns.getColumn(i);
             if(states[i].isHidden())
                 continue;
 
             writer.write("<td class=\"report-summary\"" + column.getFormattedData(rc, states[i].getCalc()) + "</td>");
         }
         writer.write("</tr>");
     }
 
     public String getFrameHdTableAttrs()
     {
         return frameHdTableAttrs;
     }
 
     public void setFrameHdTableAttrs(String frameHdTableAttrs)
     {
         this.frameHdTableAttrs = frameHdTableAttrs;
     }
 
     public String getFrameHdRowSpacerAttrs()
     {
         return frameHdRowSpacerAttrs;
     }
 
     public void setFrameHdRowSpacerAttrs(String frameHdRowSpacerAttrs)
     {
         this.frameHdRowSpacerAttrs = frameHdRowSpacerAttrs;
     }
 
     public String getFrameHdCellAttrs()
     {
         return frameHdCellAttrs;
     }
 
     public void setFrameHdCellAttrs(String frameHdCellAttrs)
     {
         this.frameHdCellAttrs = frameHdCellAttrs;
     }
 
     public String getFrameHdInfoCellAttrs()
     {
         return frameHdInfoCellAttrs;
     }
 
     public void setFrameHdInfoCellAttrs(String frameHdInfoCellAttrs)
     {
         this.frameHdInfoCellAttrs = frameHdInfoCellAttrs;
     }
 
     public SingleValueSource getFrameHdTabImgSrcValueSource()
     {
         return frameHdTabImgSrcValueSource;
     }
 
     public void setFrameHdTabImgSrcValueSource(SingleValueSource frameHdTabImgSrcValueSource)
     {
         this.frameHdTabImgSrcValueSource = frameHdTabImgSrcValueSource;
     }
 
     public SingleValueSource getFrameHdSpacerImgSrcValueSource()
     {
         return frameHdSpacerImgSrcValueSource;
     }
 
     public void setFrameHdSpacerImgSrcValueSource(SingleValueSource frameHdSpacerImgSrcValueSource)
     {
         this.frameHdSpacerImgSrcValueSource = frameHdSpacerImgSrcValueSource;
     }
 
     public String getDataHdRowAttrs()
     {
         return dataHdRowAttrs;
     }
 
     public void setDataHdRowAttrs(String dataHdRowAttrs)
     {
         this.dataHdRowAttrs = dataHdRowAttrs;
     }
 
     public String getDataHdCellAttrs()
     {
         return dataHdCellAttrs;
     }
 
     public void setDataHdCellAttrs(String dataHdCellAttrs)
     {
         this.dataHdCellAttrs = dataHdCellAttrs;
     }
 
     public String getDataEvenRowAttrs()
     {
         return dataEvenRowAttrs;
     }
 
     public void setDataEvenRowAttrs(String dataEvenRowAttrs)
     {
         this.dataEvenRowAttrs = dataEvenRowAttrs;
     }
 
     public String getDataOddRowAttrs()
     {
         return dataOddRowAttrs;
     }
 
     public void setDataOddRowAttrs(String dataOddRowAttrs)
     {
         this.dataOddRowAttrs = dataOddRowAttrs;
     }
 
     public String getDataFtRowAttrs()
     {
         return dataFtRowAttrs;
     }
 
     public void setDataFtRowAttrs(String dataFtRowAttrs)
     {
         this.dataFtRowAttrs = dataFtRowAttrs;
     }
 
     public void setFlags(int flags)
     {
         this.flags = flags;
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
 
     public String getSortAscImgSrc()
     {
         return sortAscImgSrc;
     }
 
     public void setSortAscImgSrc(String sortAscImgSrc)
     {
         this.sortAscImgSrc = sortAscImgSrc;
     }
 
     public String getSortDescImgSrc()
     {
         return sortDescImgSrc;
     }
 
     public void setSortDescImgSrc(String sortDescImgSrc)
     {
         this.sortDescImgSrc = sortDescImgSrc;
     }
 
     protected int getRowDecoratorPrependColsCount(ReportContext rc)
     {
         return rowDecoratorPrependColsCount;
     }
 
     protected void setRowDecoratorPrependColsCount(int rowDecoratorPrependColsCount)
     {
         this.rowDecoratorPrependColsCount = rowDecoratorPrependColsCount;
     }
 
     protected int getRowDecoratorAppendColsCount(ReportContext rc)
     {
         return rowDecoratorAppendColsCount;
     }
 
     protected void setRowDecoratorAppendColsCount(int rowDecoratorAppendColsCount)
     {
         this.rowDecoratorAppendColsCount = rowDecoratorAppendColsCount;
     }
 }
