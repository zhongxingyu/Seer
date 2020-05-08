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
 * $Id: StandardDialogSkin.java,v 1.2 2002-02-17 14:07:31 snshah Exp $
  */
 
 package com.netspective.sparx.xaf.skin;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.netspective.sparx.util.config.Configuration;
 import com.netspective.sparx.util.config.ConfigurationManagerFactory;
 import com.netspective.sparx.xaf.form.Dialog;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.DialogDirector;
 import com.netspective.sparx.xaf.form.DialogField;
 import com.netspective.sparx.xaf.form.DialogFieldPopup;
 import com.netspective.sparx.xaf.form.DialogSkin;
 import com.netspective.sparx.xaf.form.field.GridField;
 import com.netspective.sparx.xaf.form.field.SeparatorField;
 import com.netspective.sparx.util.value.SingleValueSource;
 
 public class StandardDialogSkin implements DialogSkin
 {
     public final String FIELDROW_PREFIX = "_dfr.";
     public final String GRIDHEADROW_PREFIX = "_dghr.";
     public final String GRIDFIELDROW_PREFIX = "_dgfr.";
     public final String EMPTY = "";
 
     protected String outerTableAttrs;
     protected String innerTableAttrs;
     protected String frameHdRowAlign;
     protected String frameHdRowAttrs;
     protected String frameHdFontAttrs;
     protected String errorMsgHdFontAttrs;
     protected String errorMsgHdText;
     protected String fieldRowAttrs;
     protected String fieldRowErrorAttrs;
     protected String gridCaptionFontAttrs;      // grid column font attributes
     protected String gridRowCaptionFontAttrs;   // grid row font attributes
     protected String gridCaptionCellAttrs;      // grid column display attributes
     protected String gridTableAttrs;            // grid table display attribute
     protected String gridCellAttrs;
     protected String captionCellAttrs;
     protected String captionFontAttrs;
     protected String controlAreaFontAttrs;
     protected String controlAreaStyleAttrs;
     protected String controlAttrs;
     protected String separatorFontAttrs;
     protected String separatorHtml;
     protected String hintFontAttrs;
     protected String errorMsgFontAttrs;
     protected String captionSuffix;
     protected String includePreScripts;
     protected String includePostScripts;
     protected String includePreStyleSheets;
     protected String includePostStyleSheets;
     protected String prependPreScript;
     protected String prependPostScript;
     protected String appendPreScript;
     protected String appendPostScript;
 
     public StandardDialogSkin()
     {
         outerTableAttrs = "cellspacing='1' cellpadding='0' bgcolor='#6699CC' ";
         innerTableAttrs = "cellspacing='0' cellpadding='4' bgcolor='lightyellow' ";
         frameHdRowAlign = "LEFT";
         frameHdRowAttrs = "bgcolor='#6699CC' ";
         frameHdFontAttrs = "face='verdana,arial,helvetica' size=2 color='yellow' ";
         errorMsgHdFontAttrs = "face='verdana,arial,helvetica' size=2 color='darkred'";
         errorMsgHdText = "Please review the following:";
         fieldRowAttrs = "";
         fieldRowErrorAttrs = "bgcolor='beige' ";
         captionCellAttrs = "align='right' ";
         captionFontAttrs = "size='2' face='tahoma,arial,helvetica' style='font-size:8pt' ";
         gridCellAttrs = "align='center'";
         gridTableAttrs = "cellpadding='2' cellspacing='0' border='0'";
         gridCaptionFontAttrs = "size='2' face='tahoma,arial,helvetica' color='navy' style='font-size:9pt' ";
         gridRowCaptionFontAttrs = "size='2' face='tahoma,arial,helvetica' color='navy' style='font-size:9pt' ";
         gridCaptionCellAttrs = "align='center'";
         controlAreaFontAttrs = "size='2' face='tahoma,arial,helvetica' style='font-size:8pt' ";
         controlAreaStyleAttrs = "style=\"background-color: lightyellow\"";
         controlAttrs = " class='dialog_control' onfocus='controlOnFocus(this, event)' onchange='controlOnChange(this, event)' " +
                 "onblur='controlOnBlur(this, event)' onkeypress='controlOnKeypress(this, event)' onclick='controlOnClick(this, event) '";
         separatorFontAttrs = "face='verdana,arial' size=2 color=#555555";
         separatorHtml = "<hr size=1 color=#555555>";
         hintFontAttrs = "color='navy'";
         errorMsgFontAttrs = "color='red'";
         captionSuffix = ": ";
         includePreScripts = null;
         includePostScripts = null;
         includePreStyleSheets = null;
         includePostStyleSheets = null;
         prependPreScript = null;
         prependPostScript = null;
         appendPreScript = null;
         appendPostScript = null;
     }
 
     public void importFromXml(Element elem)
     {
         NodeList children = elem.getChildNodes();
         for(int n = 0; n < children.getLength(); n++)
         {
             Node node = children.item(n);
             if(node.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             String nodeName = node.getNodeName();
             Element nodeElem = (Element) node;
             Node firstChild = node.getFirstChild();
             String nodeText = firstChild != null ? firstChild.getNodeValue() : null;
 
             if(nodeName.equals("outer-table-attrs") && nodeText != null)
                 outerTableAttrs = nodeText;
             else if(nodeName.equals("inner-table-attrs") && nodeText != null)
                 innerTableAttrs = nodeText;
             else if(nodeName.equals("frame-head-row-align") && nodeText != null)
                 frameHdRowAlign = nodeText;
             else if(nodeName.equals("frame-head-row-attrs") && nodeText != null)
                 frameHdRowAttrs = nodeText;
             else if(nodeName.equals("frame-head-font-attrs") && nodeText != null)
                 frameHdFontAttrs = nodeText;
             else if(nodeName.equals("error-msg-hd-font-attrs") && nodeText != null)
                 errorMsgHdFontAttrs = nodeText;
             else if(nodeName.equals("error-msg-hd-text") && nodeText != null)
                 errorMsgHdText = nodeText;
             else if(nodeName.equals("field-row-attrs") && nodeText != null)
                 fieldRowAttrs = nodeText;
             else if(nodeName.equals("field-row-error-attrs") && nodeText != null)
                 fieldRowErrorAttrs = nodeText;
             else if(nodeName.equals("caption-cell-attrs") && nodeText != null)
                 captionCellAttrs = nodeText;
             else if(nodeName.equals("caption-font-attrs") && nodeText != null)
                 captionFontAttrs = nodeText;
             else if(nodeName.equals("grid-table-attrs") && nodeText != null)
                 gridTableAttrs = nodeText;
             else if(nodeName.equals("grid-caption-font-attrs") && nodeText != null)
                 gridCaptionFontAttrs = nodeText;
             else if(nodeName.equals("grid-row-caption-font-attrs") && nodeText != null)
                 gridRowCaptionFontAttrs = nodeText;
             else if(nodeName.equals("grid-caption-cell-attrs") && nodeText != null)
                 gridCaptionCellAttrs = nodeText;
             else if(nodeName.equals("grid-cell-attrs") && nodeText != null)
                 gridCellAttrs = nodeText;
             else if(nodeName.equals("control-area-font-attrs") && nodeText != null)
                 controlAreaFontAttrs = nodeText;
             else if(nodeName.equals("control-area-style-attrs") && nodeText != null)
                 controlAreaStyleAttrs = nodeText;
             else if(nodeName.equals("control-attrs") && nodeText != null)
                 controlAttrs = nodeText;
             else if(nodeName.equals("separator-font-attrs") && nodeText != null)
                 separatorFontAttrs = nodeText;
             else if(nodeName.equals("separator-html") && nodeText != null)
                 separatorHtml = nodeText;
             else if(nodeName.equals("hint-font-attrs") && nodeText != null)
                 hintFontAttrs = nodeText;
             else if(nodeName.equals("error-msg-html") && nodeText != null)
                 errorMsgFontAttrs = nodeText;
             else if(nodeName.equals("caption-suffix") && nodeText != null)
                 captionSuffix = nodeText;
             else if(nodeName.equals("prepend-pre-script") && nodeText != null)
                 prependPreScript = "<script>\n" + nodeText + "\n</script>";
             else if(nodeName.equals("prepend-post-script") && nodeText != null)
                 prependPostScript = "<script>\n" + nodeText + "\n</script>";
             else if(nodeName.equals("append-pre-script") && nodeText != null)
                 appendPreScript = "<script>\n" + nodeText + "\n</script>";
             else if(nodeName.equals("append-post-script") && nodeText != null)
                 appendPostScript = "<script>\n" + nodeText + "\n</script>";
             else if(nodeName.equals("include-pre-script"))
             {
                 String lang = nodeElem.getAttribute("language");
                 if(lang.length() == 0) lang = "JavaScript";
                 String inc = "<script src='" + nodeElem.getAttribute("src") + "' language='" + lang + "'></script>\n";
                 ;
                 if(includePreScripts == null)
                     includePreScripts = inc;
                 else
                     includePreScripts += inc;
             }
             else if(nodeName.equals("include-post-script"))
             {
                 String lang = nodeElem.getAttribute("language");
                 if(lang.length() == 0) lang = "JavaScript";
                 String inc = "<script src='" + nodeElem.getAttribute("src") + "' language='" + lang + "'></script>\n";
                 if(includePostScripts == null)
                     includePostScripts = inc;
                 else
                     includePostScripts += inc;
             }
             else if(nodeName.equals("include-pre-stylesheet"))
             {
                 String inc = "<link rel='stylesheet' href='" + nodeElem.getAttribute("href") + "'>\n";
                 if(includePreStyleSheets == null)
                     includePreStyleSheets = inc;
                 else
                     includePreStyleSheets += inc;
             }
             else if(nodeName.equals("include-post-stylesheet"))
             {
                 String inc = "<link rel='stylesheet' href='" + nodeElem.getAttribute("href") + "'>\n";
                 if(includePostStyleSheets == null)
                     includePostStyleSheets = inc;
                 else
                     includePostStyleSheets += inc;
             }
         }
     }
 
     public final String getDefaultControlAttrs()
     {
         return controlAttrs;
     }
 
     public void renderCompositeControlsHtml(Writer writer, DialogContext dc, DialogField parentField) throws IOException
     {
         Iterator i = parentField.getChildren().iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
             if(field.isVisible(dc))
             {
                 if(field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE))
                     writer.write("<br>");
                 boolean showCaption = field.showCaptionAsChild();
                 if(showCaption)
                 {
                     String caption = field.getCaption(dc);
                     if(caption != DialogField.CUSTOM_CAPTION && caption != null)
                     {
                         writer.write("<nobr>" + (field.isRequired(dc) ? "<b>" + caption + "</b>" : caption));
                         if(captionSuffix != null)
                             writer.write(captionSuffix);
                     }
                 }
                 field.renderControlHtml(writer, dc);
                 writer.write("&nbsp;");
                 if(showCaption) writer.write("</nobr>");
                 if(field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_AFTER))
                     writer.write("<br>");
             }
         }
     }
 
     public void appendGridControlBasics(DialogContext dc, DialogField field, StringBuffer html) throws IOException
     {
         StringWriter controlHtml = new StringWriter();
         field.renderControlHtml(controlHtml, dc);
         String popupHtml = getPopupHtml(dc, field);
         if(popupHtml != null)
             controlHtml.write(popupHtml);
 
         if(field.flagIsSet(DialogField.FLDFLAG_CREATEADJACENTAREA))
             controlHtml.write("&nbsp;<span id='" + field.getQualifiedName() + "_adjacent'></span>");
 
         StringBuffer messagesHtml = new StringBuffer();
         String hint = field.getHint();
         if(hint != null)
         {
             messagesHtml.append("<br><font " + hintFontAttrs + ">");
             messagesHtml.append(hint);
             messagesHtml.append("</font>");
         }
 
         html.append("<font ");
         html.append(controlAreaFontAttrs);
         html.append(">");
         html.append(controlHtml);
         if(messagesHtml.length() > 0)
             html.append(messagesHtml);
         html.append("</font>");
     }
 
     public String getGridRowHtml(DialogContext dc, GridField gridField, DialogField compositeField, int row) throws IOException
     {
         String rowAttr = " id='" + GRIDFIELDROW_PREFIX + compositeField.getQualifiedName() + "' ";
         StringBuffer rowHtml = new StringBuffer("\n<tr valign='top' " + rowAttr + ">");
         Iterator i = compositeField.getChildren().iterator();
 
         // get the row's name
         String rowCaption = compositeField.getCaption(dc);
         if(rowCaption == null)
         {
             rowCaption = "";
         }
         if(row == 0)
         {
             String hRowAttr = " id='" + GRIDHEADROW_PREFIX + compositeField.getQualifiedName() + "' ";
             StringBuffer headerHtml = new StringBuffer("\n<tr " + hRowAttr + ">");
 
             int fieldNum = 0;
             String[] fieldCaptions = gridField.getCaptions(dc);
             // save space in the header for the row captions
            headerHtml.append("<td " + gridCaptionCellAttrs + ">&nbsp;</td> ");
             // append the row caption to the first row
             rowHtml.append("<td><font " + gridRowCaptionFontAttrs + ">");
             rowHtml.append(rowCaption);
             rowHtml.append("</font></td>");
             while(i.hasNext())
             {
                 DialogField field = (DialogField) i.next();
                 if(field.isVisible(dc))
                 {
                     String caption = fieldNum < fieldCaptions.length ? fieldCaptions[fieldNum] : field.getCaption(dc);
 
                     headerHtml.append("<td " + gridCaptionCellAttrs + "><font ");
                     headerHtml.append(gridCaptionFontAttrs);
                     headerHtml.append(">");
                     if(caption != null && caption != DialogField.CUSTOM_CAPTION)
                     {
                         headerHtml.append(field.isRequired(dc) ? "<b>" + caption + "</b>" : caption);
                     }
                     headerHtml.append("</font></td>");
 
 
                     rowHtml.append("<td " + gridCellAttrs + ">");
                     appendGridControlBasics(dc, field, rowHtml);
                     rowHtml.append("</td>");
                 }
                 fieldNum++;
             }
 
             headerHtml.append("</tr>");
             headerHtml.append(rowHtml);
             headerHtml.append("</tr>");
 
             return headerHtml.toString();
         }
         else
         {
             // append the row caption to the first row
             rowHtml.append("<td><font " + gridRowCaptionFontAttrs + ">");
             rowHtml.append(rowCaption);
             rowHtml.append("</font></td>");
 
             while(i.hasNext())
             {
                 DialogField field = (DialogField) i.next();
                 if(field.isVisible(dc))
                 {
                     rowHtml.append("<td " + gridCellAttrs + ">");
                     appendGridControlBasics(dc, field, rowHtml);
                     rowHtml.append("</td>");
                 }
             }
             rowHtml.append("</tr>");
             return rowHtml.toString();
         }
     }
 
     public void renderGridControlsHtml(Writer writer, DialogContext dc, GridField gridField) throws IOException
     {
         writer.write("\n<table " + gridTableAttrs + ">");
 
         Iterator i = gridField.getChildren().iterator();
         int row = 0;
         int colsCount = 0;
         while(i.hasNext())
         {
             DialogField rowField = (DialogField) i.next();
             if(colsCount == 0)
                 colsCount = rowField.getChildren().size();
 
             if(rowField.isVisible(dc))
             {
                 StringBuffer messagesHtml = new StringBuffer();
                 boolean haveErrors = false;
                 boolean firstMsg = true;
                 List errorMessages = dc.getErrorMessages(rowField);
                 if(errorMessages != null)
                 {
                     messagesHtml.append("<font " + errorMsgFontAttrs + ">");
                     Iterator emi = errorMessages.iterator();
                     while(emi.hasNext())
                     {
                         if(!firstMsg)
                             messagesHtml.append("<br>");
                         else
                             firstMsg = false;
                         messagesHtml.append((String) emi.next());
                     }
                     messagesHtml.append("</font>");
                     haveErrors = true;
                 }
 
                 writer.write(getGridRowHtml(dc, gridField, rowField, row));
                 if(haveErrors)
                 {
                     writer.write("<tr><td colspan='" + colsCount + "'>");
                     writer.write("<font " + controlAreaFontAttrs);
                     writer.write(messagesHtml.toString());
                     writer.write("</font></td></tr>");
                 }
             }
             row++;
         }
 
         writer.write("\n</table>");
     }
 
     public String getPopupHtml(DialogContext dc, DialogField field)
     {
         DialogFieldPopup popup = field.getPopup();
         if(popup == null)
             return null;
 
         String expression = "new DialogFieldPopup('" + dc.getDialog().getName() + "', '" + field.getQualifiedName() + "', '" + popup.getActionUrl() + "', '" + popup.getPopupWindowClass() + "', " + popup.closeAfterSelect() + ", " + popup.allowMultiSelect();
         ;
         String[] fillFields = popup.getFillFields();
         if(fillFields.length == 1)
         {
             expression += ", '" + fillFields[0] + "')";
         }
         else
         {
             StringBuffer expr = new StringBuffer(expression);
             for(int i = 0; i < fillFields.length; i++)
                 expr.append(", '" + fillFields[i] + "'");
             expression = expr.toString() + ")";
         }
 
         String imageUrl = popup.getImageUrl();
         if(imageUrl == null)
             imageUrl = ConfigurationManagerFactory.getDefaultConfiguration(dc.getServletContext()).getTextValue(dc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "dialog.field.popup-image-src");
 
         return "&nbsp;<a href='' style='cursor:hand;' onclick=\"javascript:" + expression + ";return false;\"><img border='0' src='" + imageUrl + "'></a>&nbsp;";
 
     }
 
     public void appendFieldHtml(DialogContext dc, DialogField field, StringBuffer fieldsHtml, StringBuffer fieldsJSDefn, List fieldErrorMsgs) throws IOException
     {
         if(field.flagIsSet(DialogField.FLDFLAG_INPUT_HIDDEN))
         {
             StringWriter writer = new StringWriter();
             field.renderControlHtml(writer, dc);
             fieldsHtml.append(writer);
             return;
         }
 
         String name = field.getQualifiedName();
         String caption = field.getCaption(dc);
         List fieldChildren = field.getChildren();
         if(caption != null && fieldChildren != null && caption.equals(DialogField.GENERATE_CAPTION))
         {
             StringBuffer generated = new StringBuffer();
             Iterator c = fieldChildren.iterator();
             while(c.hasNext())
             {
                 DialogField childField = (DialogField) c.next();
                 String childCaption = childField.getCaption(dc);
                 if(childCaption != null && childCaption != DialogField.CUSTOM_CAPTION)
                 {
                     if(generated.length() > 0)
                         generated.append(" / ");
                     generated.append(childField.isRequired(dc) ? "<b>" + childCaption + "</b>" : childCaption);
                 }
             }
             caption = generated.toString();
         }
         else
         {
             if(caption != null && field.isRequired(dc))
                 caption = "<b>" + caption + "</b>";
         }
 
         if(captionSuffix != null && caption != null && caption.length() > 0) caption += captionSuffix;
 
         StringWriter controlHtml = new StringWriter();
         field.renderControlHtml(controlHtml, dc);
         String popupHtml = getPopupHtml(dc, field);
         if(popupHtml != null)
             controlHtml.write(popupHtml);
 
         if(field.flagIsSet(DialogField.FLDFLAG_CREATEADJACENTAREA))
             controlHtml.write("&nbsp;<span id='" + field.getQualifiedName() + "_adjacent'></span>");
 
         StringBuffer messagesHtml = new StringBuffer();
         String hint = field.getHint();
         if(hint != null && !(field.isReadOnly(dc) && dc.getDialog().flagIsSet(Dialog.DLGFLAG_HIDE_READONLY_HINTS)))
         {
             messagesHtml.append("<br><font " + hintFontAttrs + ">");
             messagesHtml.append(hint);
             messagesHtml.append("</font>");
         }
         boolean haveErrors = false;
         if(name != null)
         {
             List errorMessages = dc.getErrorMessages(field);
             if(errorMessages != null)
             {
                 messagesHtml.append("<font " + errorMsgFontAttrs + ">");
                 Iterator emi = errorMessages.iterator();
                 while(emi.hasNext())
                 {
                     int msgNum = fieldErrorMsgs.size();
                     String msgStr = (String) emi.next();
                     fieldErrorMsgs.add(msgStr);
                     messagesHtml.append("<br><a name='dc_error_msg_" + msgNum + "'>" + msgStr + "</a>");
                 }
                 messagesHtml.append("</font>");
                 haveErrors = true;
             }
         }
 
         /*
 		 * each field row gets its own ID so DHTML can hide/show the row
 		 */
 
         String rowAttr = fieldRowAttrs + " id='" + FIELDROW_PREFIX + field.getQualifiedName() + "' ";
         if(haveErrors)
             rowAttr = rowAttr + fieldRowErrorAttrs;
 
         if(caption == null)
         {
             fieldsHtml.append("<tr" + rowAttr + "><td colspan='2'><font " + controlAreaFontAttrs + ">" + controlHtml + messagesHtml + "</font></td></tr>\n");
         }
         else
         {
             fieldsHtml.append(
                     "<tr " + rowAttr + "><td " + captionCellAttrs + "><font " + captionFontAttrs + ">" + caption + "</font></td>" +
                     "<td><font " + controlAreaFontAttrs + ">" + controlHtml + messagesHtml + "</font></td></tr>\n");
         }
 
         if(field.getSimpleName() != null)
             fieldsJSDefn.append(field.getJavaScriptDefn(dc));
     }
 
     public void renderHtml(Writer writer, DialogContext dc) throws IOException
     {
         long startTime = new Date().getTime();
 
         List fieldErrorMsgs = new ArrayList();
         List dlgErrorMsgs = dc.getErrorMessages();
         if(dlgErrorMsgs != null)
             fieldErrorMsgs.addAll(dlgErrorMsgs);
 
         Dialog dialog = dc.getDialog();
         String dialogName = dialog.getName();
 
         int layoutColumnsCount = dialog.getLayoutColumnsCount();
         int dlgTableColSpan = 2;
 
         StringBuffer fieldsHtml = new StringBuffer();
         StringBuffer fieldsJSDefn = new StringBuffer();
 
         DialogDirector director = dialog.getDirector();
         if(layoutColumnsCount == 1)
         {
             Iterator i = dc.getDialog().getFields().iterator();
             while(i.hasNext())
             {
                 DialogField field = (DialogField) i.next();
                 if(!field.isVisible(dc))
                     continue;
 
                 appendFieldHtml(dc, field, fieldsHtml, fieldsJSDefn, fieldErrorMsgs);
             }
 
             if(director != null && director.isVisible(dc) && dc.getDataCommand() != DialogContext.DATA_CMD_PRINT)
                 appendFieldHtml(dc, director, fieldsHtml, fieldsJSDefn, fieldErrorMsgs);
         }
         else
         {
             StringBuffer[] layoutColsFieldsHtml = new StringBuffer[layoutColumnsCount];
             for(int i = 0; i < layoutColumnsCount; i++)
                 layoutColsFieldsHtml[i] = new StringBuffer();
 
             int activeColumn = 0;
 
             Iterator i = dc.getDialog().getFields().iterator();
             while(i.hasNext())
             {
                 DialogField field = (DialogField) i.next();
                 if(!field.isVisible(dc))
                     continue;
 
                 if(field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE))
                     activeColumn++;
                 appendFieldHtml(dc, field, layoutColsFieldsHtml[activeColumn], fieldsJSDefn, fieldErrorMsgs);
                 if(field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_AFTER))
                     activeColumn++;
             }
 
             int lastColumn = layoutColumnsCount - 1;
             int cellWidth = 100 / layoutColumnsCount;
             dlgTableColSpan = 0;
 
             fieldsHtml.append("<tr valign='top'>");
             for(int c = 0; c < layoutColumnsCount; c++)
             {
 
                 fieldsHtml.append("<td width='" + cellWidth + "%'><table width='100%'>");
                 fieldsHtml.append(layoutColsFieldsHtml[c]);
                 fieldsHtml.append("</table></td>");
                 dlgTableColSpan++;
 
                 if(c < lastColumn)
                 {
                     fieldsHtml.append("<td>&nbsp;&nbsp;</td>");
                     dlgTableColSpan++;
                 }
             }
             fieldsHtml.append("</tr>");
 
             if(director != null && director.isVisible(dc) && dc.getDataCommand() != DialogContext.DATA_CMD_PRINT)
             {
                 fieldsHtml.append("<tr><td colspan='" + dlgTableColSpan + "'><font " + controlAreaFontAttrs + ">");
                 StringWriter directorHtml = new StringWriter();
                 director.renderControlHtml(directorHtml, dc);
                 fieldsHtml.append(directorHtml);
                 fieldsHtml.append("</font></td></tr>");
             }
         }
 
         String heading = null;
         SingleValueSource headingVS = dialog.getHeading();
         if(headingVS != null)
             heading = headingVS.getValue(dc);
 
         String actionURL = null;
         if(director != null)
             actionURL = director.getSubmitActionUrl() != null ?director.getSubmitActionUrl().getValue(dc) : null;
 
         if(actionURL == null)
             actionURL = ((HttpServletRequest) dc.getRequest()).getRequestURI();
 
         Configuration appConfig = ConfigurationManagerFactory.getDefaultConfiguration(dc.getServletContext());
         String sharedScriptsUrl = appConfig.getTextValue(dc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "scripts-url");
 
         StringBuffer errorMsgsHtml = new StringBuffer();
         if(fieldErrorMsgs.size() > 0)
         {
             errorMsgsHtml.append("<tr><td colspan='" + dlgTableColSpan + "'><ul type=square><font " + controlAreaFontAttrs + "><font " + errorMsgHdFontAttrs + "><b>" + errorMsgHdText + "</b></font>\n");
             for(int i = 0; i < fieldErrorMsgs.size(); i++)
             {
                 String errorMsg = (String) fieldErrorMsgs.get(i);
                 errorMsgsHtml.append("<li><a href='#dc_error_msg_" + i + "' style='text-decoration:none'><font " + errorMsgFontAttrs + ">" + errorMsg + "</font></a></li>\n");
             }
             errorMsgsHtml.append("</ul></td></tr>\n");
         }
         String dialogIncludeJS = (dialog.getIncludeJSFile() != null ? dialog.getIncludeJSFile().getValue(dc) : null);
         String encType = dialog.flagIsSet(Dialog.DLGFLAG_ENCTYPE_MULTIPART_FORMDATA) ? "enctype=\"multipart/form-data\"" : "";
         String html =
                 (includePreStyleSheets != null ? includePreStyleSheets : EMPTY) +
                 "<link rel='stylesheet' href='" + appConfig.getTextValue(dc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "css-url") + "/dialog.css'>\n" +
                 (includePostStyleSheets != null ? includePostStyleSheets : EMPTY) +
                 (prependPreScript != null ? prependPreScript : EMPTY) +
                 "<script language='JavaScript'>var _version = 1.0;</script>\n" +
                 "<script language='JavaScript1.1'>_version = 1.1;</script>\n" +
                 "<script language='JavaScript1.2'>_version = 1.2;</script>\n" +
                 "<script language='JavaScript1.3'>_version = 1.3;</script>\n" +
                 "<script language='JavaScript1.4'>_version = 1.4;</script>\n" +
                 (includePreScripts != null ? includePreScripts : EMPTY) +
                 "<script src='" + sharedScriptsUrl + "/popup.js' language='JavaScript1.1'></script>\n" +
                 "<script src='" + sharedScriptsUrl + "/dialog.js' language='JavaScript1.2'></script>\n" +
                 "<script language='JavaScript'>\n" +
                 "	if(typeof dialogLibraryLoaded == 'undefined')\n" +
                 "	{\n" +
                 "		alert('ERROR: " + sharedScriptsUrl + "/dialog.js could not be loaded');\n" +
                 "	}\n" +
                 "</script>\n" +
                 (dialogIncludeJS != null ? "<script language='JavaScript' src='" + dialogIncludeJS + "'></script>\n" : EMPTY) +
                 (includePostScripts != null ? includePostScripts : EMPTY) +
                 (prependPostScript != null ? prependPostScript : EMPTY) +
                 "<table " + outerTableAttrs + ">\n" +
                 "<tr><td><table " + innerTableAttrs + ">" +
                 (heading == null ? "" :
                 "<tr " + frameHdRowAttrs + "><td colspan='" + dlgTableColSpan + "' align='" + frameHdRowAlign + "'><font " + frameHdFontAttrs + "><b>" + heading + "</b></font></td></tr>\n") +
                 errorMsgsHtml +
                 "<form id='" + dialogName + "' name='" + dialogName + "' action='" + actionURL + "' method='post' " + encType + " onsubmit='return(activeDialog.isValid())'>\n" +
                 dc.getStateHiddens() + "\n" +
                 fieldsHtml +
                 "</form>\n" +
                 "</table></td></tr></table>" +
                 (appendPreScript != null ? appendPreScript : EMPTY) +
                 "<script language='JavaScript'>\n" +
                 "       var " + dialogName + " = new Dialog(\"" + dialogName + "\");\n" +
                 "       var dialog = " + dialogName + "; setActiveDialog(dialog);\n" +
                 "       var field;\n" +
                 fieldsJSDefn +
                 "       dialog.finalizeContents();\n" +
                 "</script>\n" +
                 (appendPostScript != null ? appendPostScript : EMPTY);
 
         com.netspective.sparx.util.log.LogManager.recordAccess((HttpServletRequest) dc.getRequest(), null, this.getClass().getName(), dc.getLogId(), startTime);
         writer.write(html);
     }
 
     public void renderSeparatorHtml(Writer writer, DialogContext dc, SeparatorField field) throws IOException
     {
         String heading = field.getHeading();
 
         if(heading != null)
         {
             String sep = "<font " + separatorFontAttrs + "><a name=\"" + URLEncoder.encode(heading) + "\"><b>" + heading + "</b></a></font>";
             if(!field.flagIsSet(SeparatorField.FLDFLAG_HIDERULE))
                 sep += separatorHtml;
 
             if(field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE))
                 writer.write(sep);
             else
                 writer.write("<br>" + sep);
         }
         else
         {
             if(! field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE))
                 writer.write(field.flagIsSet(SeparatorField.FLDFLAG_HIDERULE) ? "<br>" : "<hr size=1 color=silver>");
         }
     }
 
     public String getOuterTableAttrs()
     {
         return outerTableAttrs;
     }
 
     public void setOuterTableAttrs(String value)
     {
         outerTableAttrs = value;
     }
 
     public String getInnerTableAttrs()
     {
         return innerTableAttrs;
     }
 
     public void setInnerTableAttrs(String value)
     {
         innerTableAttrs = value;
     }
 
     public String getFrameHdRowAlign()
     {
         return frameHdRowAlign;
     }
 
     public void setFrameHdRowAlign(String value)
     {
         frameHdRowAlign = value;
     }
 
     public String getFrameHdRowAttrs()
     {
         return frameHdRowAttrs;
     }
 
     public void setFrameHdRowAttrs(String value)
     {
         frameHdRowAttrs = value;
     }
 
     public String getFrameHdFontAttrs()
     {
         return frameHdFontAttrs;
     }
 
     public void setFrameHdFontAttrs(String value)
     {
         frameHdFontAttrs = value;
     }
 
     public String getFieldRowAttrs()
     {
         return fieldRowAttrs;
     }
 
     public void setFieldRowAttrs(String value)
     {
         fieldRowAttrs = value;
     }
 
     public String getFieldRowErrorAttrs()
     {
         return fieldRowErrorAttrs;
     }
 
     public void setFieldRowErrorAttrs(String value)
     {
         fieldRowErrorAttrs = value;
     }
 
     public String getGridCaptionFontAttrs()
     {
         return gridCaptionFontAttrs;
     }
 
     public void setGridCaptionFontAttrs(String value)
     {
         gridCaptionFontAttrs = value;
     }
 
     public String getGridRowCaptionFontAttrs()
     {
         return gridRowCaptionFontAttrs;
     }
 
     public void setGridRowCaptionFontAttrs(String value)
     {
         gridRowCaptionFontAttrs = value;
     }
 
     public String getCaptionCellAttrs()
     {
         return captionCellAttrs;
     }
 
     public void setCaptionCellAttrs(String value)
     {
         captionCellAttrs = value;
     }
 
     public String getCaptionFontAttrs()
     {
         return captionFontAttrs;
     }
 
     public void setCaptionFontAttrs(String value)
     {
         captionFontAttrs = value;
     }
 
     public String getControlAreaFontAttrs()
     {
         return controlAreaFontAttrs;
     }
 
     public void setControlAreaFontAttrs(String value)
     {
         controlAreaFontAttrs = value;
     }
 
     public String getControlAreaStyleAttrs()
     {
         return controlAreaStyleAttrs;
     }
 
     public void setControlAreaStyleAttrs(String value)
     {
         this.controlAreaStyleAttrs = value;
     }
 
     public String getControlAttrs()
     {
         return controlAttrs;
     }
 
     public void setControlAttrs(String value)
     {
         controlAttrs = value;
     }
 
     public String getSeparatorFontAttrs()
     {
         return separatorFontAttrs;
     }
 
     public void setSeparatorFontAttrs(String value)
     {
         separatorFontAttrs = value;
     }
 
     public String getSeparatorHtml()
     {
         return separatorHtml;
     }
 
     public void setSeparatorHtml(String value)
     {
         separatorHtml = value;
     }
 
     public String getHintFontAttrs()
     {
         return hintFontAttrs;
     }
 
     public void setHintFontAttrs(String value)
     {
         hintFontAttrs = value;
     }
 
     public String getErrorMsgFontAttrs()
     {
         return errorMsgFontAttrs;
     }
 
     public void setErrorMsgFontAttrs(String value)
     {
         errorMsgFontAttrs = value;
     }
 
     public String getCaptionSuffix()
     {
         return captionSuffix;
     }
 
     public void setCaptionSuffix(String value)
     {
         captionSuffix = value;
     }
 
     public String getIncludePreScripts()
     {
         return includePreScripts;
     }
 
     public void setIncludePreScripts(String value)
     {
         includePreScripts = value;
     }
 
     public String getIncludePostScripts()
     {
         return includePostScripts;
     }
 
     public void setIncludePostScripts(String value)
     {
         includePostScripts = value;
     }
 
     public String getIncludePreStyleSheets()
     {
         return includePreStyleSheets;
     }
 
     public void setIncludePreStyleSheets(String value)
     {
         includePreStyleSheets = value;
     }
 
     public String getIncludePostStyleSheets()
     {
         return includePostStyleSheets;
     }
 
     public void setIncludePostStyleSheets(String value)
     {
         includePostStyleSheets = value;
     }
 
     public String getPrependPreScript()
     {
         return prependPreScript;
     }
 
     public void setPrependPreScript(String value)
     {
         prependPreScript = value;
     }
 
     public String getPrependPostScript()
     {
         return prependPostScript;
     }
 
     public void setPrependPostScript(String value)
     {
         prependPostScript = value;
     }
 
     public String getAppendPreScript()
     {
         return appendPreScript;
     }
 
     public void setAppendPreScript(String value)
     {
         appendPreScript = value;
     }
 
     public String getAppendPostScript()
     {
         return appendPostScript;
     }
 
     public void setAppendPostScript(String value)
     {
         appendPostScript = value;
     }
 
 }
