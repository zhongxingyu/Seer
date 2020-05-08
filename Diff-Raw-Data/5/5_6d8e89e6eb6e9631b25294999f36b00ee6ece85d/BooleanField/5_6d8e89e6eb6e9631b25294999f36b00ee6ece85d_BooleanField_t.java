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
 * $Id: BooleanField.java,v 1.6 2004-02-16 15:40:04 aye.thu Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldValue;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.commons.value.exception.ValueException;
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 import com.netspective.commons.text.TextUtils;
 
 public class BooleanField extends DialogField
 {
     public static class Choices extends XdmEnumeratedAttribute
     {
         public static final int YESNO = 0;
         public static final int TRUEFALSE = 1;
         public static final int ONOFF = 2;
 
         public static final String[] TEXT_TYPE_VALUES = new String[] { "yesno", "truefalse", "onoff" };
         public static final ValueSource[] TEXT_CHOICES = new ValueSource[] {
                 new StaticValueSource("No"),
                 new StaticValueSource("Yes"),
                 new StaticValueSource("False"),
                 new StaticValueSource("True"),
                 new StaticValueSource("Off"),
                 new StaticValueSource("On")
             };
 
         public Choices()
         {
         }
 
         public Choices(int valueIndex)
         {
             super(valueIndex);
         }
 
         public String[] getValues()
         {
             return TEXT_TYPE_VALUES;
         }
     }
 
     public static class Style extends XdmEnumeratedAttribute
     {
         public static final int RADIO = 0;
         public static final int CHECK = 1;
         public static final int CHECKALONE = 2;
         public static final int COMBO = 3;
 
         public static final String[] STYLE_VALUES = new String[] { "radio", "check", "check-alone", "combo" };
 
         public Style()
         {
         }
 
         public Style(int valueIndex)
         {
             super(valueIndex);
         }
 
         public String[] getValues()
         {
             return STYLE_VALUES;
         }
     }
 
     public class BooleanFieldState extends State
     {
         public class BooleanFieldValue extends BasicStateValue
         {
             public BooleanFieldValue()
             {
             }
 
             public Class getValueHolderClass()
             {
                 return Integer.class;
             }
 
             public void setTextValue(String value) throws ValueException
             {
                 if(value == null || value.length() == 0)
                     setValue(new Integer(-1));
                 else
                     setValue(new Integer(TextUtils.toBoolean(value) ? 1 : 0));
             }
 
             public boolean getBoolValue()
             {
                 Integer value = (Integer) getValue();
                 return value == null ? false : (value.intValue() > 0 ? true : false);
             }
 
             public void setValue(boolean value)
             {
                 setValue(new Integer(value ? 1 : 0));
             }
         }
 
         public BooleanFieldState(DialogContext dc, DialogField field)
         {
             super(dc, field);
         }
 
         public DialogFieldValue constructValueInstance()
         {
             return new BooleanFieldValue();
         }
     }
 
     private Style style = new Style(Style.RADIO);
     private Choices choices;
     private ValueSource checkLabel = ValueSource.NULL_VALUE_SOURCE;
     private ValueSource trueText;
     private ValueSource falseText;
     private ValueSource noneText;
 
     public BooleanField()
     {
         setChoices(new Choices(Choices.YESNO));
     }
 
     public DialogField.State constructStateInstance(DialogContext dc)
     {
         return new BooleanFieldState(dc, this);
     }
 
     public Class getStateClass()
     {
         return BooleanFieldState.class;
     }
 
     public Class getStateValueClass()
     {
         return BooleanFieldState.BasicStateValue.class;
     }
 
     public Choices getChoices()
     {
         return choices;
     }
 
     public void setChoices(Choices choices)
     {
         this.choices = choices;
         int choicesTextIndex = choices.getValueIndex() * 2;
         setFalseText(Choices.TEXT_CHOICES[choicesTextIndex]);
         setTrueText(Choices.TEXT_CHOICES[choicesTextIndex+1]);
     }
 
     public ValueSource getFalseText()
     {
         return falseText;
     }
 
     public void setFalseText(ValueSource falseText)
     {
         this.falseText = falseText;
     }
 
     public ValueSource getNoneText()
     {
         return noneText;
     }
 
     public void setNoneText(ValueSource noneText)
     {
         this.noneText = noneText;
     }
 
     public Style getStyle()
     {
         return style;
     }
 
     public void setStyle(Style style)
     {
         this.style = style;
     }
 
     public ValueSource getTrueText()
     {
         return trueText;
     }
 
     public void setTrueText(ValueSource trueText)
     {
         this.trueText = trueText;
     }
 
     public ValueSource getCheckLabel()
     {
         return checkLabel != ValueSource.NULL_VALUE_SOURCE ? checkLabel : getCaption();
     }
 
     public void setCheckLabel(ValueSource checkLabel)
     {
         this.checkLabel = checkLabel;
     }
 
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         if(isInputHidden(dc))
         {
             writer.write(getHiddenControlHtml(dc));
             return;
         }
 
         String falseTextStr = falseText.getTextValue(dc);
         String trueTextStr = trueText.getTextValue(dc);
         String noneTextStr = noneText != null ? noneText.getTextValue(dc) : null;
 
         BooleanFieldState.BooleanFieldValue dfvalue = (BooleanFieldState.BooleanFieldValue) dc.getFieldStates().getState(this).getValue();
         int value = ((Integer) dfvalue.getValue()).intValue();
         String strValue = value == -1 ? "" : Integer.toString(value);
         String boolValueCaption = value == -1 ? "" : (value == 1 ? trueTextStr : falseTextStr);
 
         if(isReadOnly(dc))
         {
             if (this.noneText == null) {
                 writer.write("<input type='hidden' name='" + getHtmlFormControlId() + "' value='" + (strValue != null ? strValue : "") + "'><span id='" + getQualifiedName() + "'>" + (value == 1 ? trueTextStr : falseTextStr) + "</span>");
             } else {
                 writer.write("<input type='hidden' name='" + getHtmlFormControlId() + "' value='" +
                         (strValue != null ? strValue : "") + "'><span id='" + getQualifiedName() + "'>" +
                         boolValueCaption +
                         "</span>");
             }
             return;
         }
 
         String id = getHtmlFormControlId();
         String defaultControlAttrs = dc.getSkin().getDefaultControlAttrs();
         switch(style.getValueIndex())
         {
             case Style.RADIO:
                 if (noneTextStr != null)
                 {
                     String[] val = { "" , "" , "" };
                     setChecked (strValue, val);
                     writer.write(
                         "<nobr><input type='radio' name='" + id + "' id='" + id + "0' value='0' " + val[0] + defaultControlAttrs + "> <label for='" + id + "0'>" + falseTextStr + "</label></nobr> " +
                         "<nobr><input type='radio' name='" + id + "' id='" + id + "1' value='1' " + val[1] + defaultControlAttrs + "> <label for='" + id + "1'>" + trueTextStr + "</label></nobr> " +
                         "<nobr><input type='radio' name='" + id + "' id='" + id + "2' value='2' " + val[2] + defaultControlAttrs + "> <label for='" + id + "2'>" + noneTextStr + "</label></nobr>");
                 }
                 else
                 {
                     writer.write(
                        "<nobr><input type='radio' name='" + id + "' id='" + id + "0' value='0' " + (value == 0 ? "checked " : "") + defaultControlAttrs + "> <label for='" + id + "0'>" + falseTextStr + "</label></nobr> " +
                         "<nobr><input type='radio' name='" + id + "' id='" + id + "1' value='1' " + (value == 1 ? "checked " : "") + defaultControlAttrs + "> <label for='" + id + "1'>" + trueTextStr + "</label></nobr>");
                 }
                 break;
 
             case Style.CHECK:
                 writer.write("<nobr><input type='checkbox' name='" + id + "' id='" + id + "' value='1' " + (value == 1 ? "checked " : "") + defaultControlAttrs + "> <label for='" + id + "'>" + getCheckLabel().getTextValue(dc) + "</label></nobr>");
                 break;
 
             case Style.CHECKALONE:
                 writer.write("<input type='checkbox' name='" + id + "' value='1' " + (value == 1 ? "checked " : "") + defaultControlAttrs + "> ");
                 break;
 
             case Style.COMBO:
                 writer.write(
                         "<select name='" + id + "' " + defaultControlAttrs + ">" +
                         "<option " + (value == 0 ? "" : "selected") + " value='0'>" + falseTextStr + "</option>" +
                         "<option " + (value == 1 ? "selected" : "") + " value='1'>" + trueTextStr + "</option>" +
                         "</select>");
                 break;
 
             default:
                 writer.write("Unknown style " + style);
         }
     }
 
     private void setChecked (String strValue, String[] val)
     {
         int index;
         if (strValue != null)
         {
             try
             {
                 index = Integer.parseInt (strValue);
                 val[index] = " checked ";
             }
             catch (NumberFormatException e) { }
         }
     }
 }
