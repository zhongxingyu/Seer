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
 * $Id: DateTimeField.java,v 1.13 2003-10-01 15:23:08 shahid.shah Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.StringTokenizer;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.field.DialogFieldValue;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldValidations;
 import com.netspective.sparx.form.field.DialogFieldFlags;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.exception.ValueException;
 import com.netspective.commons.validate.rule.DateValueValidationRule;
 import com.netspective.commons.text.TextUtils;
 
 public class DateTimeField extends TextField
 {
     private static final Log log = LogFactory.getLog(DateTimeField.class);
 
     public static final Flags.FlagDefn[] DATE_TIME_FIELD_FLAG_DEFNS = new Flags.FlagDefn[TextField.TEXT_FIELD_FLAG_DEFNS.length + 5];
     static
     {
         for(int i = 0; i < TextField.TEXT_FIELD_FLAG_DEFNS.length; i++)
             DATE_TIME_FIELD_FLAG_DEFNS[i] = TextField.TEXT_FIELD_FLAG_DEFNS[i];
         DATE_TIME_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 0] = new Flags.FlagDefn(Flags.ACCESS_XDM, "FUTURE_ONLY", Flags.FUTURE_ONLY);
         DATE_TIME_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 1] = new Flags.FlagDefn(Flags.ACCESS_XDM, "PAST_ONLY", Flags.PAST_ONLY);
         DATE_TIME_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 2] = new Flags.FlagDefn(Flags.ACCESS_XDM, "STRICT_YEAR", Flags.STRICT_YEAR);
         DATE_TIME_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 3] = new Flags.FlagDefn(Flags.ACCESS_XDM, "STRICT_TIME", Flags.STRICT_TIME);
         DATE_TIME_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 4] = new Flags.FlagDefn(Flags.ACCESS_XDM, "POPUP_CALENDAR", Flags.POPUP_CALENDAR);
     }
 
     public class Flags extends TextField.Flags
     {
         public static final int FUTURE_ONLY = TextField.Flags.START_CUSTOM;
         public static final int PAST_ONLY = FUTURE_ONLY * 2;
         public static final int STRICT_YEAR = PAST_ONLY * 2;
         public static final int STRICT_TIME = STRICT_YEAR * 2;
         public static final int POPUP_CALENDAR = STRICT_TIME * 2;
         public static final int START_CUSTOM = POPUP_CALENDAR * 2;
 
         public Flags()
         {
             super(POPUP_CALENDAR);
         }
 
         public Flags(State dfs)
         {
             super(dfs, POPUP_CALENDAR);
         }
 
         public Flags(DateTimeField dtf)
         {
             super(dtf, POPUP_CALENDAR);
         }
 
         public void flagsChanged()
         {
             if (getField() != null)
             {
                 DateValueValidationRule dateValidationRule = ((DateTimeField)getField()).getDateValidationRule();
                 if(dateValidationRule != null)
                 {
                     dateValidationRule.setFutureOnly(flagIsSet(FUTURE_ONLY));
                     dateValidationRule.setPastOnly(flagIsSet(PAST_ONLY));
                 }
             }
         }
 
         public FlagDefn[] getFlagsDefns()
         {
             return DATE_TIME_FIELD_FLAG_DEFNS;
         }
     }
 
     public static class DataType extends XdmEnumeratedAttribute
     {
         public static final int DATE_ONLY = 0;
         public static final int TIME_ONLY = 1;
         public static final int BOTH = 2;
 
         public static final String[] VALUES = new String[] { "date-only", "time-only", "date-and-time" };
         public static final String[] SERVER_FORMATS = new String[] { "MM/dd/yyyy", "HH:mm", "MM/dd/yyyy HH:mm" };
         public static final String[] CLIENT_FORMATS = new String[] { "mm/dd/yy", "mm/dd/yy", "mm/dd/yy" };
 
         public DataType()
         {
 
         }
 
         public DataType(int valueIndex)
         {
             super(valueIndex);
         }
 
         public String[] getValues()
         {
             return VALUES;
         }
 
         public String getServerFormatPattern()
         {
             return SERVER_FORMATS[getValueIndex()];
         }
 
         public String getClientFormatPattern()
         {
             return CLIENT_FORMATS[getValueIndex()];
         }
 
         public SimpleDateFormat getFormat()
         {
             return new SimpleDateFormat(getServerFormatPattern());
         }
     }
 
     public class DateTimeFieldState extends TextFieldState
     {
         public class DateTimeFieldValue extends TextFieldValue
         {
             public Class getValueHolderClass()
             {
                 return Date.class;
             }
 
             public String getTextValue()
             {
                 return (getValue() != null) ? dateValidationRule.format((Date)getValue()) : null;
             }
 
             public void setTextValue(String value) throws ValueException
             {
                 if(value == null || value.length() == 0)
                 {
                     setValue((Date) null);
                     return;
                 }
 
                 switch(getDataType().getValueIndex())
                 {
                     case DataType.DATE_ONLY:
                     case DataType.BOTH:
                         value = translateDateString(value);
                         break;
 
                     case DataType.TIME_ONLY:
                         value = translateTimeString(value);
                         break;
 
                     default:
                         break;
                 }
                 try
                 {
                     setValue(dateValidationRule.parse(value));
                 }
                 catch (ParseException e)
                 {
                     setInvalidText(value);
                     invalidate(getDialogContext(), getErrorCaption().getTextValue(getDialogContext()) + " requires a value in date format ("+ dateValidationRule.toPattern() +").");
                 }
             }
 
             public Date getDateValue()
             {
                 return (Date) getValue();
             }
         }
 
         public DateTimeFieldState(DialogContext dc, DialogField field)
         {
             super(dc, field);
         }
 
         public DialogFieldValue constructValueInstance()
         {
             return new DateTimeFieldValue();
         }
 
     }
 
     private DataType dataType = null;
     private DateValueValidationRule dateValidationRule;
     private String clientCalendarFormat = "mm/dd/yyyy";
 
     public DateTimeField()
     {
         super();
         setDataType(new DataType(DataType.DATE_ONLY));
     }
 
     public DialogField.State constructStateInstance(DialogContext dc)
     {
         return new DateTimeFieldState(dc, this);
     }
 
     public Class getStateClass()
     {
         return DateTimeFieldState.class;
     }
 
     public Class getStateValueClass()
     {
         return DateTimeFieldState.DateTimeFieldValue.class;
     }
 
     public DialogFieldFlags createFlags()
     {
         return new Flags();
     }
 
     public DialogFieldValidations constructValidationRules()
     {
         DialogFieldValidations rules = super.constructValidationRules();
         dateValidationRule = new DateValueValidationRule();
         getFlags().flagsChanged(); // make sure updates occur based on flags
         rules.addRule(dateValidationRule);
         return rules;
     }
 
     public DateValueValidationRule getDateValidationRule()
     {
         return dateValidationRule;
     }
 
     public void addValidation(DialogFieldValidations rules)
     {
         super.addValidation(rules);
 
         // hang onto the text validation rule since we're going to need it for javascript definition and rendering
         boolean found = false;
         for(int i = 0; i < rules.size(); i++)
         {
             if(rules.get(i) instanceof DateValueValidationRule)
             {
                 if(found)
                     throw new RuntimeException("Multiple date validation rules not allowed.");
 
                 dateValidationRule = (DateValueValidationRule) rules.get(i);
                 found = true;
             }
         }
     }
 
     public DataType getDataType()
     {
         return dataType;
     }
 
     public void setDataType(DataType value)
     {
         if (value.getValueIndex() == DataType.TIME_ONLY)
             this.getFlags().clearFlag(Flags.POPUP_CALENDAR);
         dataType = value;
         setFormat(dataType.getFormat());
         setClientCalendarFormat(dataType.getClientFormatPattern());
         dateValidationRule.getFormat().setLenient(false);
         setSize(dataType.getServerFormatPattern().length());
         setMaxLength(getSize());
     }
 
     public void setFormat(SimpleDateFormat format)
     {
         dateValidationRule.setFormat(format);
     }
 
     public void setMax(ValueSource date) throws ParseException
     {
         dateValidationRule.setMaxDateSource(date);
     }
 
     public void setMin(ValueSource date) throws ParseException
     {
         dateValidationRule.setMinDateSource(date);
     }
 
     public String getClientCalendarFormat()
     {
         return clientCalendarFormat;
     }
 
     public void setClientCalendarFormat(String clientCalendarFormat)
     {
         this.clientCalendarFormat = clientCalendarFormat;
     }
 
     /**
      * Strips the ":" from the Time field. Must only be used when the
      * DateTime field contains only time.
      *
      * @param value Time field string
      * @return String formatted Time string
      */
     public String formatTimeValue(String value)
     {
         if(value == null)
             return value;
 
         StringBuffer timeValueStr = new StringBuffer();
         StringTokenizer tokens = new StringTokenizer(value, ":");
         while(tokens.hasMoreTokens())
             timeValueStr.append(tokens.nextToken());
 
         return timeValueStr.toString();
     }
 
     /**
      * Overwrites DialogField's getCustomJavaScriptDefn()
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
         buf.append("field.dateDataType = " + this.getDataType().getValueIndex() + ";\n");
         buf.append("field.dateFormat = '" + dateValidationRule.toPattern() + "';\n");
 
         if(getFlags().flagIsSet(Flags.STRICT_YEAR))
             buf.append("field.dateStrictYear = true;\n");
         else
             buf.append("field.dateStrictYear = false;\n");
 
         if(getDataType().getValueIndex() == DataType.TIME_ONLY)
         {
             if(getFlags().flagIsSet(Flags.STRICT_TIME))
                 buf.append("field.timeStrict = true;\n");
             else
                 buf.append("field.timeStrict = false;\n");
         }
 
         return buf.toString();
     }
 
     /**
      * Translates a reserved date word such as "today" or "now" into the actual time
      *
      * @param str reserved string
      * @return String actual time string
      */
     public String translateTimeString(String str)
     {
         String xlatedDate = str;
 
         if(str != null && (str.startsWith("today") || str.startsWith("now")))
         {
             Date dt = new Date();
             xlatedDate = dateValidationRule.format(dt);
         }
         return xlatedDate;
     }
 
     /**
      * Translates a reserved date word such as "today" or "now" into the actual date
      *
      * @param str reserved string
      * @return String actual date string
      */
     public String translateDateString(String str)
     {
         String xlatedDate = str;
 
         if(str != null && (str.startsWith("today") || str.startsWith("now")))
         {
             int strLength = 0;
             if(str.startsWith("today"))
                 strLength = "today".length();
             else
                 strLength = "now".length();
             Date dt = null;
             if(str.length() > strLength)
             {
                 try
                 {
                     String opValueStr = null;
                     if(str.charAt(strLength) == '+')
                         opValueStr = str.substring(strLength + 1);
                     else
                         opValueStr = str.substring(strLength);
                     int opValue = Integer.parseInt(opValueStr);
                     Calendar calendar = new GregorianCalendar();
                     calendar.add(Calendar.DAY_OF_MONTH, opValue);
                     dt = calendar.getTime();
                     xlatedDate = dateValidationRule.format(dt);
                 }
                 catch(Exception e)
                 {
                     log.error("Unable to translate date string " + str, e);
                 }
             }
             else
             {
                 dt = new Date();
                 xlatedDate = dateValidationRule.format(dt);
             }
         }
         return xlatedDate;
     }
 
     /**
      * Produces the control html associated with the field
      * @param writer
      * @param dc
      * @throws IOException
      */
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         if(isInputHidden(dc))
         {
             writer.write(getHiddenControlHtml(dc));
             return;
         }
 
         DialogField.State state = dc.getFieldStates().getState(this);
         DateTimeField.Flags stateFlags = (DateTimeField.Flags) state.getStateFlags();
         String textValue = state.getValue().getTextValue();
 
         if(textValue == null)
             textValue = "";
         else
             textValue = TextUtils.escapeHTML(textValue);
 
         String className = isRequired(dc) ? dc.getSkin().getControlAreaRequiredStyleClass() : dc.getSkin().getControlAreaStyleClass();
 
         String controlAreaStyle = dc.getSkin().getControlAreaStyleAttrs();
         if(isReadOnly(dc))
         {
             writer.write("<input type='hidden' name='" + getHtmlFormControlId() + "' value=\"" + textValue + "\">" +
                     "<span id='" + getQualifiedName() + "'>" + textValue + "</span>");
         }
         else if(isBrowserReadOnly(dc))
         {
             className = dc.getSkin().getControlAreaReadonlyStyleClass();
             final String formatPattern = dateValidationRule.toPattern();
             writer.write("<input type=\"text\" name=\"" + getHtmlFormControlId() + "\" readonly value=\"" +
                     textValue + "\" maxlength=\"" + formatPattern.length() + "\" size=\"" +
                     formatPattern.length() + "\" " + controlAreaStyle +
                     " class=\"" + className + "\" " + dc.getSkin().getDefaultControlAttrs() + ">");
         }
         else if(!stateFlags.flagIsSet(TextField.Flags.MASK_ENTRY))
         {
             final String formatPattern = dateValidationRule.toPattern();
             writer.write("<input type=\"text\" name=\"" + getHtmlFormControlId() + "\" value=\"" + textValue + "\" maxlength=\"" +
                     formatPattern.length() + "\" size=\"" + formatPattern.length() + "\" " +
                     controlAreaStyle + " class=\"" + className + "\" " +
                     dc.getSkin().getDefaultControlAttrs() + ">");
         }
 
         if((isInputHidden(dc) || isReadOnly(dc)) || ! getFlags().flagIsSet(Flags.POPUP_CALENDAR))
             return;
         if (getDataType().getValueIndex() != DataType.TIME_ONLY)
         {
             Theme theme = dc.getSkin().getTheme();
             writer.write("<script src='" + theme.getResourceUrl("/calendar-0.9.2/calendar.js") + "'></script>\n");
             writer.write("<script src='" + theme.getResourceUrl("/calendar-0.9.2/lang/calendar-en.js") + "'></script>\n");
            writer.write("<script src='" + theme.getResourceUrl("/calendar-0.9.2/scripts/calendar-helper.js") + "'></script>\n");
 
             writer.write(
                     "<a href='#' onclick='javascript:showCalendar(\"" + getQualifiedName() + "\", \""+ getClientCalendarFormat() +"\")'>" +
                     "<img src='" + theme.getResourceUrl("/images/calendar.gif") + "' title='Select from Calendar' border=0></a>");
         }
     }
 }
