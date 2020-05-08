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
  * @author Aye Thu
  */
 
 package com.netspective.sparx.theme.basic;
 
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.report.tabular.HtmlTabularReportValueContext;
 import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
 import com.netspective.sparx.report.tabular.HtmlReportActions;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSource;
 import com.netspective.sparx.report.tabular.HtmlReportAction;
 import com.netspective.sparx.panel.HtmlPanelValueContext;
 import com.netspective.sparx.panel.HtmlPanelFrame;
 import com.netspective.sparx.panel.HtmlPanelActions;
 import com.netspective.commons.value.source.RedirectValueSource;
 
 import java.io.Writer;
 import java.io.IOException;
 
 /**
  * Class for producing a html report that allows adding and editing of data
  *
 * $Id: RecordViewerReportSkin.java,v 1.11 2003-09-15 03:58:35 aye.thu Exp $
  */
 public class RecordViewerReportSkin extends BasicHtmlTabularReportPanelSkin
 {
     public RecordViewerReportSkin()
     {
         super();
     }
 
     public RecordViewerReportSkin(Theme theme, String name, String panelClassNamePrefix, String panelResourcesPrefix, boolean fullWidth)
     {
         super(theme, name, panelClassNamePrefix, panelResourcesPrefix, fullWidth);
     }
 
     /**
      * Adds action items including frame actions and record action items
      * @param writer
      * @param vc
      * @param frame
      * @throws IOException
      */
     public void produceHeadingExtras(Writer writer, HtmlPanelValueContext vc, HtmlPanelFrame frame) throws IOException
     {
         super.produceHeadingExtras(writer, vc, frame);
 
         HtmlTabularReportValueContext rc = ((HtmlTabularReportValueContext)vc);
         BasicHtmlTabularReport report = (BasicHtmlTabularReport)rc.getReport();
         HtmlReportActions actions = report.getActions();
         HtmlPanelActions frameActions = frame.getActions();
         if (actions != null)
         {
             HtmlReportAction reportAction = actions.get(HtmlReportAction.Type.getValue(HtmlReportAction.Type.RECORD_ADD));
             if (reportAction != null)
             {
                 Theme theme = rc.getActiveTheme();
                 RedirectValueSource redirect = (RedirectValueSource) reportAction.getRedirect();
                 if (frameActions.size() > 0)
                     writer.write("            <td bgcolor=\"white\"><img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" width=\"5\" height=\"5\"></td>");
                 writer.write("            <td class=\""+ panelClassNamePrefix +"-frame-action-item\" width=\"18\"><img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" width=\"18\" height=\"19\"></td>");
                 if (redirect != null)
                 {
                      writer.write("            <td class=\""+ panelClassNamePrefix +"-frame-action-box\">" +
                         "<a class=\""+ panelClassNamePrefix +"-frame-action\" href=\""+ redirect.getUrl(rc)  +
                         "\">&nbsp;" + reportAction.getCaption().getTextValue(vc) + "&nbsp;</a></td>");
                 }
             }
         }
     }
 
     /**
      *
      * @param writer
      * @param rc
      * @param ds
      * @param isOddRow
      * @throws IOException
      */
     public void produceDataRowDecoratorPrepend(Writer writer, HtmlTabularReportValueContext rc, HtmlTabularReportDataSource ds, String[] rowData, boolean isOddRow) throws IOException
     {
         BasicHtmlTabularReport report = (BasicHtmlTabularReport)rc.getReport();
         HtmlReportActions actions = report.getActions();
         if (actions == null)
         {
             // no actions are defined in the report
             return;
         }
         HtmlReportAction reportAction = actions.get(HtmlReportAction.Type.getValue(HtmlReportAction.Type.RECORD_EDIT));
         if (reportAction != null)
         {
             RedirectValueSource redirect = (RedirectValueSource) reportAction.getRedirect();
             Theme theme = rc.getActiveTheme();
 
             String label = "<img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/content-action-edit.gif") + "\" " +
                 "alt=\"\" height=\"10\" width=\"10\" border=\"0\">";
             String editRecordUrl = this.constructRedirect(rc, redirect, label, null, null);
             editRecordUrl = report.replaceOutputPatterns(rc, ds, editRecordUrl);
            writer.write("<td " + (isOddRow ? "class=\"report\"" : "class=\"report-alternative\"") + " width=\"10\">");
             writer.write(editRecordUrl);
             writer.write("</td>");
         }
     }
 
     /**
      * Gets the additional number of columns to prepend to the data
      * @param rc
      * @return
      */
     protected int getRowDecoratorPrependColsCount(HtmlTabularReportValueContext rc)
     {
         BasicHtmlTabularReport report = (BasicHtmlTabularReport)rc.getReport();
         HtmlReportActions actions = report.getActions();
         if (actions == null)
         {
             // no actions are defined in the report so return 0
             return 0;
         }
         HtmlReportAction reportAction = actions.get(HtmlReportAction.Type.getValue(HtmlReportAction.Type.RECORD_EDIT));
         if (reportAction != null)
             return 1;
         else
             return 0;
     }
 }
