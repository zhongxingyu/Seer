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
 * $Id: IntegerField.java,v 1.6 2003-09-15 03:53:56 aye.thu Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.field.type.TextField;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldValidations;
import com.netspective.sparx.form.field.DialogFieldValue;
 import com.netspective.commons.value.exception.ValueException;
 import com.netspective.commons.validate.rule.IntegerValueValidationRule;
 
 public class IntegerField extends TextField
 {
     public class IntegerFieldState extends TextFieldState
     {
         public class IntegerFieldValue extends TextFieldValue
         {
             public Class getValueHolderClass()
             {
                 return Integer.class;
             }
 
             public void setTextValue(String value) throws ValueException
             {
                 if(value == null || value.length() == 0)
                 {
                     setValue((Integer) null);
                     return;
                 }
 
                 try
                 {
                     setValue(new Integer(Integer.parseInt(value)));
                 }
                 catch (NumberFormatException e)
                 {
                     setInvalidText(value);
                     invalidate(getDialogContext(), getErrorCaption().getTextValue(getDialogContext()) + " requires a value in integer format ("+ e.getMessage() +").");
                 }
             }
 
             public int getIntValue(int defaultValue)
             {
                 Integer integerValue = (Integer) getValue();
                 if(integerValue != null)
                     return integerValue.intValue();
                 else
                     return defaultValue;
             }
 
             public Integer getIntegerValue()
             {
                 return (Integer) getValue();
             }
 
             public void setIntValue(int value)
             {
                 setValue(new Integer(value));
             }
         }
 
         public IntegerFieldState(DialogContext dc, DialogField field)
         {
             super(dc, field);
         }
 
        public DialogFieldValue constructValueInstance()
        {
            return new IntegerFieldValue();
        }
     }
 
     private IntegerValueValidationRule integerValidationRule;
 
 	public IntegerField()
 	{
 		super();
 		setSize(10);
 	}
 
     public DialogField.State constructStateInstance(DialogContext dc)
     {
         return new IntegerFieldState(dc, this);
     }
 
     public Class getStateClass()
     {
         return IntegerFieldState.class;
     }
 
     public Class getStateValueClass()
     {
         return IntegerFieldState.IntegerFieldValue.class;
     }
 
     public DialogFieldValidations constructValidationRules()
     {
         DialogFieldValidations rules = super.constructValidationRules();
         integerValidationRule = new IntegerValueValidationRule();
         integerValidationRule.setMin(Integer.MIN_VALUE);
         integerValidationRule.setMax(Integer.MAX_VALUE);
         rules.addRule(integerValidationRule);
         return rules;
     }
 
     public void setMax(int max)
     {
         integerValidationRule.setMax(max);
     }
 
     public void setMin(int min)
     {
         integerValidationRule.setMin(min);
     }
 
     public void setInvalidRangeMessage(String invalidRangeMessage)
     {
         integerValidationRule.setInvalidRangeMessage(invalidRangeMessage);
     }
 
     public void setInvalidTypeMessage(String invalidTypeMessage)
     {
         integerValidationRule.setInvalidTypeMessage(invalidTypeMessage);
     }
 
     public void addValidation(DialogFieldValidations rules)
     {
         super.addValidation(rules);
 
         // hang onto the integer validation rule since we're going to need it for javascript definition and rendering
         boolean found = false;
         for(int i = 0; i < rules.size(); i++)
         {
             if(rules.get(i) instanceof IntegerValueValidationRule)
             {
                 if(found)
                     throw new RuntimeException("Multiple integer validation rules not allowed.");
 
                 integerValidationRule = (IntegerValueValidationRule) rules.get(i);
                 found = true;
             }
         }
     }
 
 	public String getCustomJavaScriptDefn(DialogContext dc)
 	{
 		StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
 
 		if(integerValidationRule.getMin() != java.lang.Integer.MIN_VALUE)
 			buf.append("field.minValue = " + integerValidationRule.getMin() + ";\n");
 		if(integerValidationRule.getMax() != java.lang.Integer.MAX_VALUE)
 			buf.append("field.maxValue = " + integerValidationRule.getMax() + ";\n");
 
 		return buf.toString();
 	}
 }
