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
 * $Id: SelectField.java,v 1.4 2003-01-06 17:34:27 shahbaz.javeed Exp $
  */
 
 package com.netspective.sparx.xaf.form.field;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.DialogContextMemberInfo;
 import com.netspective.sparx.xaf.form.DialogField;
 import com.netspective.sparx.xaf.form.DialogFieldPopup;
 import com.netspective.sparx.util.value.ListValueSource;
 import com.netspective.sparx.util.value.StringsListValue;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 
 public class SelectField extends TextField
 {
    static public final long FLDFLAG_SORTCHOICES = TextField.FLDFLAG_STARTCUSTOM;
     static public final long FLDFLAG_PREPENDBLANK = FLDFLAG_SORTCHOICES * 2;
     static public final long FLDFLAG_APPENDBLANK = FLDFLAG_PREPENDBLANK * 2;
 
     static private final SelectChoicesList EMPTY_CHOICES = new SelectChoicesList();
 
     static public final int SELECTSTYLE_RADIO = 0;
     static public final int SELECTSTYLE_COMBO = 1;
     static public final int SELECTSTYLE_LIST = 2;
     static public final int SELECTSTYLE_MULTICHECK = 3;
     static public final int SELECTSTYLE_MULTILIST = 4;
     static public final int SELECTSTYLE_MULTIDUAL = 5;
     static public final int SELECTSTYLE_POPUP = 6;
 
     private ListValueSource listSource;
     private ListValueSource defaultValue;
     private int style;
     private int size = 4;
     private int multiDualWidth = 125;
     private String multiDualCaptionLeft = "Available";
     private String multiDualCaptionRight = "Selected";
     private String radioCheckSeparator = "<br>";
 
     public class SelectFieldPopup extends DialogFieldPopup
     {
         private String lvsSessionAttrName;
 
         public SelectFieldPopup()
         {
             super("popup-url:cmd=lvs,reference;session:LVSPOPUP_"+ getQualifiedName()+";yes", new String[] { getQualifiedName(), getQualifiedName() + "_adjacent"});
             lvsSessionAttrName = "LVSPOPUP_"+ getQualifiedName();
         }
 
         public void prepareForPopup(DialogContext dc)
         {
             ((HttpServletRequest) dc.getRequest()).getSession(true).setAttribute(lvsSessionAttrName, listSource);
         }
     }
 
     public SelectField()
     {
         super();
         style = SELECTSTYLE_COMBO;
         super.setSize(8);
     }
 
     public SelectField(String aName, String aCaption, int aStyle)
     {
         super(aName, aCaption);
         style = aStyle;
     }
 
     public SelectField(String aName, String aCaption, int aStyle, String choices)
     {
         super(aName, aCaption);
         style = aStyle;
         setChoices(choices);
     }
 
     public SelectField(String aName, String aCaption, int aStyle, ListValueSource ls)
     {
         super(aName, aCaption);
         style = aStyle;
         setListSource(ls);
     }
 
     public final boolean isMulti()
     {
         return (
                 style == SELECTSTYLE_MULTICHECK ||
                 style == SELECTSTYLE_MULTILIST ||
                 style == SELECTSTYLE_MULTIDUAL) ? true : false;
     }
 
     public final int getStyle()
     {
         return style;
     }
 
     public void setStyle(int value)
     {
         style = value;
     }
 
     public final ListValueSource getDefaultListValue()
     {
         return defaultValue;
     }
 
     public void setDefaultListValue(ListValueSource value)
     {
         defaultValue = value;
     }
 
     public final void setMultiDualCaptions(String left, String right)
     {
         multiDualCaptionLeft = left;
         multiDualCaptionRight = right;
     }
 
     public final void setMultiDualWidth(int value)
     {
         multiDualWidth = value;
     }
 
     public void setChoices(String values)
     {
         ListValueSource vs = null;
         if(values != null)
         {
             vs = ValueSourceFactory.getListValueSource(values);
             if(vs == null)
             {
                 vs = new StringsListValue();
                 vs.initializeSource(values);
             }
         }
 
         setListSource(vs);
     }
 
     public void setChoices(Element elem)
     {
         StringsListValue vs = null;
         if(elem != null)
         {
             SelectChoicesList list = new SelectChoicesList();
 
             NodeList children = elem.getChildNodes();
             for(int n = 0; n < children.getLength(); n++)
             {
                 Node node = children.item(n);
                 if(node.getNodeType() != Node.ELEMENT_NODE)
                     continue;
 
                 Element choice = (Element) node;
                 if(choice.getNodeName().equals("choice"))
                 {
                     String value = choice.getAttribute("value");
                     String caption = choice.getFirstChild().getNodeValue();
 
                     if(value.length() > 0)
                         list.add(new SelectChoice(caption, value));
                     else
                         list.add(new SelectChoice(caption));
                 }
             }
 
             vs = new StringsListValue();
             vs.setChoices(list);
         }
 
         setListSource(vs);
     }
 
     public final ListValueSource getListSource()
     {
         return listSource;
     }
 
     public void setListSource(ListValueSource value)
     {
         listSource = value;
     }
 
     public boolean defaultIsListValueSource()
     {
         return true;
     }
 
     public void importFromXml(Element elem)
     {
         super.importFromXml(elem);
 
         String styleValue = elem.getAttribute("style");
         if(styleValue.length() > 0)
         {
             if(styleValue.equalsIgnoreCase("radio"))
                 style = SelectField.SELECTSTYLE_RADIO;
             else if(styleValue.equalsIgnoreCase("list"))
                 style = SelectField.SELECTSTYLE_LIST;
             else if(styleValue.equalsIgnoreCase("multicheck"))
                 style = SelectField.SELECTSTYLE_MULTICHECK;
             else if(styleValue.equalsIgnoreCase("multilist"))
                 style = SelectField.SELECTSTYLE_MULTILIST;
             else if(styleValue.equalsIgnoreCase("multidual"))
                 style = SelectField.SELECTSTYLE_MULTIDUAL;
             else if(styleValue.equalsIgnoreCase("popup"))
                 style = SelectField.SELECTSTYLE_POPUP;
             else
                 style = SelectField.SELECTSTYLE_COMBO;
         }
 
         if(style == SelectField.SELECTSTYLE_MULTIDUAL)
         {
             String value = elem.getAttribute("caption-left");
             if(value.length() > 0)
                 multiDualCaptionLeft = value;
 
             value = elem.getAttribute("caption-right");
             if(value.length() > 0)
                 multiDualCaptionRight = value;
 
             value = elem.getAttribute("multi-width");
             if(value.length() > 0)
                 multiDualWidth = Integer.parseInt(value);
         }
 
         String defaultv = elem.getAttribute("default");
         if(defaultv.length() > 0)
         {
             if(isMulti())
                 defaultValue = ValueSourceFactory.getListValueSource(defaultv);
             else
                 super.setDefaultValue(ValueSourceFactory.getSingleOrStaticValueSource(defaultv));
         }
         else
             defaultValue = null;
 
         String choicesValue = elem.getAttribute("choices");
         if(choicesValue.length() > 0)
             setChoices(choicesValue);
 
         NodeList choicesList = elem.getElementsByTagName("choices");
         if(choicesList.getLength() > 0)
             setChoices((Element) choicesList.item(0));
 
         String blank = elem.getAttribute("prepend-blank");
         if(blank.length() > 0 && blank.equals("yes"))
             setFlag(FLDFLAG_PREPENDBLANK);
 
         blank = elem.getAttribute("append-blank");
         if(blank.length() > 0 && blank.equals("yes"))
             setFlag(FLDFLAG_APPENDBLANK);
 
         String controlSep = elem.getAttribute("control-separator");
         if(controlSep.length() > 0)
             radioCheckSeparator = controlSep;
 
         if(style == SELECTSTYLE_POPUP)
         {
             setFlag(FLDFLAG_CREATEADJACENTAREA);
             setPopup(new SelectFieldPopup());
         }
     }
 
     public boolean isValid(DialogContext dc)
     {
         switch(style)
         {
             case SELECTSTYLE_POPUP:
                 // we're just going to let the super method take care of us
                 break;
 
             case SELECTSTYLE_COMBO:
             case SELECTSTYLE_LIST:
             case SELECTSTYLE_RADIO:
                 String value = dc.getValue(this);
                 if(this.isVisible(dc) && this.isRequired(dc) && (value == null || value.length() == 0))
                 {
                     invalidate(dc, getCaption(dc) + " is required.");
                     return false;
                 }
                 break;
 
             case SELECTSTYLE_MULTILIST:
             case SELECTSTYLE_MULTICHECK:
             case SELECTSTYLE_MULTIDUAL:
                 String[] values = dc.getValues(this);
                 if(this.isVisible(dc) && this.isRequired(dc) && (values == null || values.length == 0))
                 {
                     invalidate(dc, getCaption(dc) + " is required.");
                     return false;
                 }
                 break;
         }
 
         return super.isValid(dc);
     }
 
     public void populateValue(DialogContext dc, int formatType)
     {
         if(isMulti())
         {
             String[] values = dc.getValues(this);
             if(values == null)
                 values = dc.getRequest().getParameterValues(getId());
 
             if(dc.getRunSequence() == 1)
             {
                 if((values != null && values.length == 0 && defaultValue != null) ||
                         (values == null && defaultValue != null))
                 {
                     SelectChoicesList list = defaultValue.getSelectChoices(dc);
                     dc.setValues(this, list.getValues());
                 }
             }
             else
                 dc.setValues(this, values);
         }
         else
         {
             super.populateValue(dc, formatType);
             if(style == SELECTSTYLE_POPUP && listSource != null)
             {
                 ((SelectFieldPopup) getPopup()).prepareForPopup(dc);
                 String adjacentText = listSource.getAdjacentCaptionForValue(dc, dc.getValue(getQualifiedName()));
                 if(adjacentText != null)
                     dc.setAdjacentAreaValue(getQualifiedName(), adjacentText);
             }
         }
     }
 
     public String getMultiDualControlHtml(DialogContext dc, SelectChoicesList choices)
     {
         String dialogName = dc.getDialog().getName();
 
         String width = multiDualWidth + " pt";
         String sorted = flagIsSet(FLDFLAG_SORTCHOICES) ? "true" : "false";
         String id = getId();
         String name = getQualifiedName();
         String fieldAreaFontAttrs = dc.getSkin().getControlAreaFontAttrs();
 
         StringBuffer selectOptions = new StringBuffer();
         StringBuffer selectOptionsSelected = new StringBuffer();
         Iterator i = choices.getIterator();
         while(i.hasNext())
         {
             SelectChoice choice = (SelectChoice) i.next();
             if(choice.selected)
                 selectOptionsSelected.append("<option value=\"" + choice.value + "\">" + choice.caption + "</option>\n");
             else
                 selectOptions.append("<option value=\"" + choice.value + "\">" + choice.caption + "</option>\n");
         }
 
         return
                 "<TABLE CELLSPACING=0 CELLPADDING=1 ALIGN=left BORDER=0>\n" +
                 "<TR>\n" +
                 "<TD ALIGN=left><FONT " + fieldAreaFontAttrs + ">" + multiDualCaptionLeft + "</FONT></TD><TD></TD>\n" +
                 "<TD ALIGN=left><FONT " + fieldAreaFontAttrs + ">" + multiDualCaptionRight + "</FONT></TD>\n" +
                 "</TR>\n" +
                 "<TR>\n" +
                 "<TD ALIGN=left VALIGN=top>\n" +
                 "	<SELECT class='dialog_control' ondblclick=\"MoveSelectItems('" + dialogName + "', '" + name + "_From', '" + id + "', " + sorted + ")\" NAME='" + name + "_From' SIZE='" + size + "' MULTIPLE STYLE=\"width: " + width + "\">\n" +
                 "	" + selectOptions + "\n" +
                 "	</SELECT>\n" +
                 "</TD>\n" +
                 "<TD ALIGN=center VALIGN=middle>\n" +
                 "	&nbsp;<INPUT TYPE=button NAME=\"" + name + "_addBtn\" onClick=\"MoveSelectItems('" + dialogName + "', '" + name + "_From', '" + id + "', " + sorted + ")\" VALUE=\" > \">&nbsp;<BR CLEAR=both>\n" +
                 "	&nbsp;<INPUT TYPE=button NAME=\"" + name + "_removeBtn\" onClick=\"MoveSelectItems('" + dialogName + "', '" + id + "', '" + name + "_From', " + sorted + ")\" VALUE=\" < \">&nbsp;\n" +
                 "</TD>\n" +
                 "<TD ALIGN=left VALIGN=top>\n" +
                 "	<SELECT class='dialog_control' ondblclick=\"MoveSelectItems('" + dialogName + "', '" + id + "', '" + name + "_From', " + sorted + ")\" NAME='" + id + "' SIZE='" + size + "' MULTIPLE STYLE=\"width: " + width + "\" " + dc.getSkin().getDefaultControlAttrs() + ">\n" +
                 "	" + selectOptionsSelected + "\n" +
                 "	</SELECT>\n" +
                 "</TD>\n" +
                 "</TR>\n" +
                 "</TABLE>";
     }
 
     public void renderPopupControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         // as a popup, we're a simple text field so just use it's rendering method
         super.renderControlHtml(writer, dc);
     }
 
     public String getHiddenControlHtml(DialogContext dc, boolean showCaptions)
     {
         SelectChoicesList choices = null;
         if(listSource != null)
         {
             choices = listSource.getSelectChoices(dc);
             choices.calcSelections(dc, this);
         }
         else
             choices = EMPTY_CHOICES;
 
         String id = getId();
         Iterator i = choices.getIterator();
         StringBuffer html = new StringBuffer();
 
         if(showCaptions)
         {
             while(i.hasNext())
             {
                 SelectChoice choice = (SelectChoice) i.next();
                 if(choice.selected)
                 {
                     if(html.length() > 0)
                         html.append("<br>");
                     html.append("<input type='hidden' name='" + id + "' value=\"" + choice.value + "\"><span id='" + getQualifiedName() + "'>" + choice.caption + "</span>");
                 }
             }
         }
         else
         {
             while(i.hasNext())
             {
                 SelectChoice choice = (SelectChoice) i.next();
                 if(choice.selected)
                     html.append("<input type='hidden' name='" + id + "' value=\"" + choice.value + "\">");
             }
         }
 
         return html.toString();
     }
 
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         // we do this first because popups don't want to pull in all the data at once like other select styles do
         if(style == SELECTSTYLE_POPUP)
         {
             renderPopupControlHtml(writer, dc);
             return;
         }
 
         if(isInputHidden(dc))
         {
             writer.write(getHiddenControlHtml(dc, false));
             return;
         }
 
         if(isReadOnly(dc))
         {
             writer.write(getHiddenControlHtml(dc, true));
             return;
         }
 
         SelectChoicesList choices = null;
         if(listSource != null)
         {
             choices = listSource.getSelectChoices(dc);
             if(choices == null)
                 throw new RuntimeException("Choices is NULL in " + listSource.getClass().getName());
             choices.calcSelections(dc, this);
         }
         else
             choices = EMPTY_CHOICES;
 
         boolean readOnly = isReadOnly(dc);
         String id = getId();
         String defaultControlAttrs = dc.getSkin().getDefaultControlAttrs();
 
         StringBuffer options = new StringBuffer();
         int itemIndex = 0;
         Iterator i = choices.getIterator();
         switch(style)
         {
             case SELECTSTYLE_RADIO:
                 while(i.hasNext())
                 {
                     SelectChoice choice = (SelectChoice) i.next();
                     if(options.length() > 0)
                         options.append(radioCheckSeparator);
                     options.append("<nobr><input type='radio' name='" + id + "' id='" + id + itemIndex + "' value=\"" + choice.value + "\" " + (choice.selected ? "checked " : "") + defaultControlAttrs + "> <label for='" + id + itemIndex + "'>" + choice.caption + "</label></nobr>");
                     itemIndex++;
                 }
                 writer.write(options.toString());
                 return;
 
             case SELECTSTYLE_MULTICHECK:
                 while(i.hasNext())
                 {
                     SelectChoice choice = (SelectChoice) i.next();
                     if(options.length() > 0)
                         options.append(radioCheckSeparator);
                     options.append("<nobr><input type='checkbox' name='" + id + "' id='" + id + itemIndex + "' value=\"" + choice.value + "\" " + (choice.selected ? "checked " : "") + defaultControlAttrs + "> <label for='" + id + itemIndex + "'>" + choice.caption + "</label></nobr>");
                     itemIndex++;
                 }
                 writer.write(options.toString());
                 return;
 
             case SELECTSTYLE_COMBO:
             case SELECTSTYLE_LIST:
             case SELECTSTYLE_MULTILIST:
                 if(readOnly)
                 {
                     while(i.hasNext())
                     {
                         SelectChoice choice = (SelectChoice) i.next();
                         if(choice.selected)
                         {
                             if(options.length() > 0)
                                 options.append(", ");
                             options.append("<input type='hidden' name='" + id + "' value=\"" + choice.value + "\">");
                             options.append(choice.caption);
                         }
                     }
                     writer.write(options.toString());
                     return;
                 }
                 else
                 {
                     boolean prependBlank = false;
                     boolean appendBlank = false;
 
                     if(style == SELECTSTYLE_COMBO || style == SELECTSTYLE_LIST)
                     {
                         prependBlank = flagIsSet(FLDFLAG_PREPENDBLANK);
                         appendBlank = flagIsSet(FLDFLAG_APPENDBLANK);
                     }
 
                     if(prependBlank)
                         options.append("<option value=''></option>");
 
                     while(i.hasNext())
                     {
                         SelectChoice choice = (SelectChoice) i.next();
                         options.append("<option value=\"" + choice.value + "\" " + (choice.selected ? "selected" : "") + ">" + choice.caption + "</option>");
                     }
 
                     if(appendBlank)
                         options.append("<option value=''></option>");
 
                     switch(style)
                     {
                         case SELECTSTYLE_COMBO:
                             writer.write("<select name='" + id + "' " + defaultControlAttrs + ">" + options + "</select>");
                             break;
 
                         case SELECTSTYLE_LIST:
                             writer.write("<select name='" + id + "' size='" + size + "' " + defaultControlAttrs + ">" + options + "</select>");
                             break;
 
                         case SELECTSTYLE_MULTILIST:
                             writer.write("<select name='" + id + "' size='" + size + "' multiple='yes' " + defaultControlAttrs + ">" + options + "</select>");
                             break;
                     }
 
                     return;
                 }
 
             case SELECTSTYLE_MULTIDUAL:
                 writer.write(getMultiDualControlHtml(dc, choices));
                 break;
 
             default:
                 writer.write("Unknown style " + style);
         }
     }
 
     /**
      * Empty method. Overwritten by extending classes needing to to extra Javascript work.
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         return (super.getCustomJavaScriptDefn(dc) + "field.style = " + getStyle() + ";\n");
     }
 
     /*
 	 * Produces Java code when a custom DialogContext is created
 	 */
     public DialogContextMemberInfo getDialogContextMemberInfo()
     {
         DialogContextMemberInfo mi = null;
         String fieldName, memberName, dataType;
 
         switch(style)
         {
             case SELECTSTYLE_RADIO:
             case SELECTSTYLE_COMBO:
             case SELECTSTYLE_LIST:
                 mi = createDialogContextMemberInfo("String");
                 fieldName = mi.getFieldName();
                 memberName = mi.getMemberName();
                 dataType = mi.getDataType();
 
                 mi.addJavaCode("\tpublic " + dataType + " get" + memberName + "() { return getValue(\"" + fieldName + "\"); }\n");
                 mi.addJavaCode("\tpublic " + dataType + " get" + memberName + "(" + dataType + " defaultValue) { return getValue(\"" + fieldName + "\", defaultValue); }\n");
                 mi.addJavaCode("\tpublic int get" + memberName + "Int() { String s = getValue(\"" + fieldName + "\"); return s == null ? -1 : Integer.parseInt(s); }\n");
                 mi.addJavaCode("\tpublic int get" + memberName + "Int(int defaultValue) { String s = getValue(\"" + fieldName + "\"); return s == null ? defaultValue : Integer.parseInt(s); }\n");
                 mi.addJavaCode("\tpublic void set" + memberName + "(" + dataType + " value) { setValue(\"" + fieldName + "\", value); }\n");
                 mi.addJavaCode("\tpublic void set" + memberName + "(int value) { setValue(\"" + fieldName + "\", Integer.toString(value)); }\n");
                 break;
 
             case SELECTSTYLE_MULTICHECK:
             case SELECTSTYLE_MULTILIST:
             case SELECTSTYLE_MULTIDUAL:
                 mi = createDialogContextMemberInfo("String[]");
                 fieldName = mi.getFieldName();
                 memberName = mi.getMemberName();
                 dataType = mi.getDataType();
 
                 mi.addJavaCode("\tpublic " + dataType + " get" + memberName + "() { return getValues(\"" + fieldName + "\"); }\n");
                 mi.addJavaCode("\tpublic " + dataType + " get" + memberName + "(" + dataType + " defaultValue) { " + dataType + " o = getValues(\"" + fieldName + "\"); return o == null ? defaultValue : o; }\n");
                 mi.addJavaCode("\tpublic void set" + memberName + "(" + dataType + " values) { setValues(\"" + fieldName + "\", values); }\n");
                 break;
         }
 
         return mi;
     }
 }
