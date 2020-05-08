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
 * $Id: TextField.java,v 1.16 2003-04-14 15:23:53 aye.thu Exp $
  */
 
 package com.netspective.sparx.xaf.form.field;
 
 import com.netspective.sparx.util.log.LogManager;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ListValueSource;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 import com.netspective.sparx.util.value.StringsListValue;
 import com.netspective.sparx.util.value.ValueSource;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.DialogContextMemberInfo;
 import com.netspective.sparx.xaf.form.DialogField;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.xaf.theme.Theme;
 import com.netspective.sparx.xaf.theme.ThemeStyle;
 import com.netspective.sparx.xaf.sql.StatementNotFoundException;
 import org.apache.oro.text.perl.MalformedPerl5PatternException;
 import org.apache.oro.text.perl.Perl5Util;
 import org.w3c.dom.Element;
 
 import javax.naming.NamingException;
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.util.Iterator;
 
 public class TextField extends DialogField
 {
     static public final long FLDFLAG_MASKENTRY = DialogField.FLDFLAG_STARTCUSTOM;
     static public final long FLDFLAG_UPPERCASE = FLDFLAG_MASKENTRY * 2;
     static public final long FLDFLAG_LOWERCASE = FLDFLAG_UPPERCASE * 2;
     static public final long FLDFLAG_TRIM = FLDFLAG_LOWERCASE * 2;
     static public final long FLDFLAG_STARTCUSTOM = FLDFLAG_TRIM * 2;
 
     static public Perl5Util perlUtil = new Perl5Util();
 
     private int size;
     private int minLength;
     private int maxLength;
     /* regular expression pattern for validating the value */
     private String validatePattern;
     /* substitution patttern to format the value so that it satisfies the validation regex */
     private String displaySubstitutionPattern;
     /* error message for when the validation fails */
     private String regexMessage;
     /* substitution pattern to format the value when the value is ready to submitted */
     private String submitSubstitutionPattern;
     private ListValueSource validValues;
 
     public TextField()
     {
         super();
         size = 32;
         minLength = 0;
         maxLength = 255;
     }
 
     public TextField(String aName, String aCaption)
     {
         super(aName, aCaption);
         size = 32;
         minLength = 0;
         maxLength = 255;
     }
 
     public String getDisplaySubstitutionPattern()
     {
         return displaySubstitutionPattern;
     }
 
     public void setDisplaySubstitutionPattern(String validateSubstitutionPattern)
     {
         this.displaySubstitutionPattern = validateSubstitutionPattern;
     }
 
     public final int getSize()
     {
         return size;
     }
 
     public void setSize(int value)
     {
         size = value;
     }
 
     public int getMinLength()
     {
         return minLength;
     }
 
     public void setMinLength(int minLength)
     {
         this.minLength = minLength;
     }
 
     public final int getMaxLength()
     {
         return maxLength;
     }
 
     public void setMaxLength(int newLength)
     {
         maxLength = newLength;
     }
 
     public void importFromXml(Element elem)
     {
         super.importFromXml(elem);
 
         String value = elem.getAttribute("size");
         if(value.length() != 0)
             size = Integer.parseInt(value);
 
         value = elem.getAttribute("min-length");
         if(value.length() != 0)
             minLength = Integer.parseInt(value);
 
         value = elem.getAttribute("max-length");
         if(value.length() != 0)
             maxLength = Integer.parseInt(value);
 
         if(elem.getAttribute("uppercase").equalsIgnoreCase("yes"))
             setFlag(FLDFLAG_UPPERCASE);
 
         if(elem.getAttribute("lowercase").equalsIgnoreCase("yes"))
             setFlag(FLDFLAG_LOWERCASE);
 
         if(elem.getAttribute("trim").equalsIgnoreCase("yes"))
             setFlag(FLDFLAG_TRIM);
 
         if(elem.getAttribute("mask-entry").equalsIgnoreCase("yes"))
             setFlag(FLDFLAG_MASKENTRY);
 
         // extract validating values
         value = elem.getAttribute("valid-values");
         if (value != null && value.length() != 0)
             setValidValues(value);
 
         // extract the regex pattern
         value = elem.getAttribute("validate-pattern");
         if(value != null && value.length() != 0)
             setValidatePattern(value);
 
         value = elem.getAttribute("validate-msg");
         if(value != null && value.length() != 0)
             setValidatePatternErrorMessage(value);
 
         // extract the substitution/formatting pattern for displaying the value
         value = elem.getAttribute("display-pattern");
         if(value != null && value.length() != 0)
             this.setDisplaySubstitutionPattern(value);
 
         // extract the substituiton/formatting pattern for submitting the value
         value = elem.getAttribute("format-pattern");
         if(value != null && value.length() != 0)
             setSubmitSubstitutePattern(value);
     }
 
     /**
      * Sets the valid values for this field
      * @param values
      */
     public void setValidValues(String values)
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
 
     public ListValueSource getValidValueList()
     {
         return validValues;
     }
 
     public void setListSource(ListValueSource vs)
     {
         validValues = vs;
     }
 
     /**
      * Format the dialog field value for every dialog stage(display/validation stages) but not
      * after successful validation(submit stage).
      *
      * @param   value field value
      * @return String formatted text
      */
     public String formatDisplayValue(String value)
     {
         if(value == null) return null;
 
         long flags = getFlags();
         if((flags & FLDFLAG_UPPERCASE) != 0) value = value.toUpperCase();
         if((flags & FLDFLAG_LOWERCASE) != 0) value = value.toLowerCase();
         if((flags & FLDFLAG_TRIM) != 0) value = value.trim();
 
         if(this.displaySubstitutionPattern != null)
         {
             try
             {
                 value = perlUtil.substitute(displaySubstitutionPattern, value);
             }
             catch(MalformedPerl5PatternException e)
             {
                 value = e.toString();
                 LogManager.recordException(this.getClass(), "formatDisplayValue", "malformed perl expression", e);
             }
         }
         return value;
     }
 
     /**
      * Format the dialog field value after successful validation.
      *
      * @param   value field value
      * @return String formatted text
      */
     public String formatSubmitValue(String value)
     {
         if(value == null) return null;
 
         long flags = getFlags();
         if((flags & FLDFLAG_UPPERCASE) != 0) value = value.toUpperCase();
         if((flags & FLDFLAG_LOWERCASE) != 0) value = value.toLowerCase();
         if((flags & FLDFLAG_TRIM) != 0) value = value.trim();
 
         if(this.submitSubstitutionPattern != null)
         {
             try
             {
                 value = perlUtil.substitute(submitSubstitutionPattern, value);
             }
             catch(MalformedPerl5PatternException e)
             {
                 LogManager.recordException(this.getClass(), "formatSubmitValue", "malformed perl expression", e);
                 value = e.toString();
             }
         }
         return value;
     }
 
     public void populateValue(DialogContext dc, int formatType)
     {
         String value = dc.getValue(this);
         if(value == null)
             value = dc.getRequest().getParameter(getId());
 
         SingleValueSource defaultValue = getDefaultValue();
         if(dc.getRunSequence() == 1)
         {
             if((value != null && value.length() == 0 && defaultValue != null) ||
                     (value == null && defaultValue != null))
                 value = defaultValue.getValueOrBlank(dc);
         }
         if(formatType == DialogField.DISPLAY_FORMAT)
             dc.setValue(this, this.formatDisplayValue(value));
         else if(formatType == DialogField.SUBMIT_FORMAT)
             dc.setValue(this, this.formatSubmitValue(value));
     }
 
 
     public boolean needsValidation(DialogContext dc)
     {
         return true;
     }
 
     public boolean isValid(DialogContext dc)
     {
         String value = dc.getValue(this);
         int valueLen = value == null ? 0 : value.length();
         if(isRequired(dc) && valueLen == 0)
         {
             invalidate(dc, getCaption(dc) + " is required.");
             return false;
         }
 
         // call the super class's isValid method
         boolean result = super.isValid(dc);
         if(!result)
             return false;
 
         if(value != null)
         {
             if(valueLen < minLength)
             {
                 invalidate(dc, getCaption(dc) + " should be at least "+ minLength +" characters.");
                 return false;
             }
             if(valueLen > maxLength)
             {
                 invalidate(dc, getCaption(dc) + " should be at most "+ maxLength +" characters.");
                 return false;
             }
         }
         // if valid values were defined for this field, check the current value
        if (validValues != null && value != null && value.length() > 0)
         {
             String[] validList = validValues.getValues(dc);
             if (validList != null && validList.length > 0)
             {
                 boolean valid = false;
                 for (int i=0; validList != null && i < validList.length; i++)
                 {
                     if (validList[i].equals(value))
                     {
                         valid = true;
                         break;
                     }
                 }
                 if (!valid)
                 {
                     invalidate(dc, getCaption(dc) + " does not contain a valid value.");
                     return false;
                 }
             }
         }
 
         // if we're doing a regular expression pattern match, try it now
         if(validatePattern != null && value != null && value.length() > 0)
         {
             try
             {
                 if(!perlUtil.match(this.validatePattern, value))
                 {
                     invalidate(dc, regexMessage);
                     result = false;
                 }
             }
             catch(MalformedPerl5PatternException e)
             {
                 LogManager.recordException(this.getClass(), "isValid", "malformed perl expression", e);
                 invalidate(dc, e.toString());
                 result = false;
             }
         }
 
         return result;
     }
 
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         if(isInputHidden(dc))
         {
             writer.write(getHiddenControlHtml(dc));
             return;
         }
 
         String value = dc.getValue(this);
         if(value == null)
             value = "";
         else
             value = escapeHTML(value);
 
         String className = "";
         SkinFactory tf = SkinFactory.getInstance();
         Theme theme = tf.getCurrentTheme(dc);
         if (theme != null)
         {
             ThemeStyle style = theme.getCurrentStyle();
             if (isRequired(dc))
                 className =  style.getRequiredFieldClass();
             else
                 className = style.getFieldClass();
         }
         else
         {
             className = isRequired(dc) ? dc.getSkin().getControlAreaRequiredStyleClass() : dc.getSkin().getControlAreaStyleClass();
         }
         String controlAreaStyle = dc.getSkin().getControlAreaStyleAttrs();
         if(isReadOnly(dc))
         {
             writer.write("<input type='hidden' name='" + getId() + "' value=\"" + value + "\"><span id='" + getQualifiedName() + "'>" + dc.getValue(this) + "</span>");
         }
         else if(isBrowserReadOnly(dc))
         {
             className = dc.getSkin().getControlAreaReadonlyStyleClass();
             writer.write("<input type=\"text\" name=\"" + getId() + "\" readonly value=\"" +
                     value + "\" maxlength=\"" + maxLength + "\" size=\"" + size + "\" " + controlAreaStyle +
                     " class=\"" + className + "\" " + dc.getSkin().getDefaultControlAttrs() + ">");
         }
         else if(!flagIsSet(FLDFLAG_MASKENTRY))
         {
             writer.write("<input type=\"text\" name=\"" + getId() + "\" value=\"" + value + "\" maxlength=\"" +
                     maxLength + "\" size=\"" + size + "\" " + controlAreaStyle + " class=\"" + className + "\" " +
                     dc.getSkin().getDefaultControlAttrs() + ">");
         }
         else
         {
             writer.write("<input type=\"password\" name=\"" + getId() + "\" value=\"" + value + "\" maxlength=\"" +
                     maxLength + "\" size=\"" + size + "\" " + controlAreaStyle + " class=\"" + className + "\" " +
                     dc.getSkin().getDefaultControlAttrs() + ">");
         }
     }
 
     /**
      * Returns the regular expression used for validating the field
      *
      * @return String regular expression pattern
      */
     public String getValidatePattern()
     {
         return validatePattern;
     }
 
     /**
      * Sets the regular expression used for validating the field
      * @param str Pattern string
      */
     public void setValidatePattern(String str)
     {
         validatePattern = str;
     }
 
     /**
      * Returns the regular expression used for formatting/substituting the field
      *
      * @return String regular expression pattern
      */
     public String getSubmitSubstitutePattern()
     {
         return submitSubstitutionPattern;
     }
 
     /**
      * Sets the regular expression used for formatting/substituting the field
      * @param str Pattern string
      */
     public void setSubmitSubstitutePattern(String str)
     {
         submitSubstitutionPattern = str;
     }
 
     /**
      *
      */
     public String getValidatePatternErrorMessage()
     {
         return regexMessage;
     }
 
     /**
      *
      */
     public void setValidatePatternErrorMessage(String str)
     {
         regexMessage = str;
     }
 
     /**
      *
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
 
         if(this.isBrowserReadOnly(dc))
             buf.append("field.readonly = 'yes';\n");
         else
             buf.append("field.readonly = 'no';\n");
 
         if(this.flagIsSet(TextField.FLDFLAG_UPPERCASE))
             buf.append("field.uppercase = 'yes';\n");
         else
             buf.append("field.uppercase = 'no';\n");
 
         if(this.flagIsSet(TextField.FLDFLAG_IDENTIFIER))
             buf.append("field.identifier = 'yes';\n");
         else
             buf.append("field.identifier = 'no';\n");
 
         if(this.validatePattern != null)
             buf.append("field.text_format_pattern = " + this.validatePattern + ";\n");
         if(this.regexMessage != null)
             buf.append("field.text_format_err_msg = '" + this.regexMessage + "';\n");
 
         if (getValidValueList() != null)
         {
             String[] values = getValidValueList().getValues(dc);
             buf.append("field.validValues = [");
             for (int i=0; i < values.length; i++)
             {
                 if (values[i] != null)
                 {
                     if (i != 0)
                         buf.append(", ");
                     buf.append("\"" + values[i] + "\"");
                 }
             }
             buf.append("];\n");
         }
 
         return buf.toString();
     }
 
     /**
      * Produces Java code when a custom DialogContext is created
      */
     public DialogContextMemberInfo getDialogContextMemberInfo()
     {
         DialogContextMemberInfo mi = createDialogContextMemberInfo("String");
         String fieldName = mi.getFieldName();
         String memberName = mi.getMemberName();
         String dataType = mi.getDataType();
 
         mi.addJavaCode("\tpublic " + dataType + " get" + memberName + "() { return getValue(\"" + fieldName + "\"); }\n");
         mi.addJavaCode("\tpublic " + dataType + " get" + memberName + "(" + dataType + " defaultValue) { return getValue(\"" + fieldName + "\", defaultValue); }\n");
         mi.addJavaCode("\tpublic " + dataType + " get" + memberName + "OrBlank() { return getValue(\"" + fieldName + "\", \"\"); }\n");
 
         mi.addJavaCode("\tpublic String get" + memberName + "String() { return getValue(\"" + fieldName + "\"); }\n");
         mi.addJavaCode("\tpublic String get" + memberName + "String(String defaultValue) { return getValue(\"" + fieldName + "\", defaultValue); }\n");
 
         mi.addJavaCode("\tpublic Object get" + memberName + "Object() { return getValueAsObject(\"" + fieldName + "\"); }\n");
         mi.addJavaCode("\tpublic Object get" + memberName + "Object(Object defaultValue) { return getValueAsObject(\"" + fieldName + "\", defaultValue); }\n");
 
         mi.addJavaCode("\tpublic void set" + memberName + "(" + dataType + " value) { setValue(\"" + fieldName + "\", value); }\n");
         mi.addJavaCode("\tpublic void set" + memberName + "Object(" + dataType + " value) { setValue(\"" + fieldName + "\", (" + dataType + ") value); }\n");
 
         return mi;
     }
 }
