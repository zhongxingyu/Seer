 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: PhoneField.java,v 1.6 2003-10-19 04:07:29 aye.thu Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.field.DialogFieldFlags;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 
 public class PhoneField extends TextField
 {
     public static final String DASH_FORMAT = "dash";
     public static final String DASH_VALIDATE_PATTERN = "^([\\d][\\d][\\d])[\\.-]?([\\d][\\d][\\d])[\\.-]?([\\d]{4})([ ][x][\\d]{1,5})?$";
     public static final String DASH_DISPLAY_PATTERN = "s/" + DASH_VALIDATE_PATTERN + "/$1-$2-$3$4/g";
     public static final String DASH_SUBMIT_PATTERN = "s/" + DASH_VALIDATE_PATTERN + "/$1$2$3$4/g";
     public static final String DASH_VALIDATE_ERROR_MSG = "Input must be in the 999-999-9999 x99999 format.";
 
     public static final String BRACKET_FORMAT = "bracket";
     public static final String BRACKET_VALIDATE_PATTERN = "^[\\(]?([\\d][\\d][\\d])[\\)]?[ ]?([\\d][\\d][\\d])[\\.-]?([\\d]{4})([ ][x][\\d]{1,5})?$";
     public static final String BRACKET_DISPLAY_PATTERN = "s/" + BRACKET_VALIDATE_PATTERN + "/($1) $2-$3$4/g";
     public static final String BRACKET_SUBMIT_PATTERN = "s/" + BRACKET_VALIDATE_PATTERN + "/$1$2$3$4/g";
     public static final String BRACKET_VALIDATE_ERROR_MSG = "Input must be in the (999)999-9999 x99999 format.";
 
     public static final Flags.FlagDefn[] PHONE_FIELD_FLAG_DEFNS = new Flags.FlagDefn[TextField.TEXT_FIELD_FLAG_DEFNS.length + 1];
     static
     {
         for(int i = 0; i < TextField.TEXT_FIELD_FLAG_DEFNS.length; i++)
             PHONE_FIELD_FLAG_DEFNS[i] = TextField.TEXT_FIELD_FLAG_DEFNS[i];
         PHONE_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 0] = new Flags.FlagDefn(Flags.ACCESS_XDM, "STRIP_BRACKETS", Flags.STRIP_BRACKETS);
     }
 
     public class Flags extends TextField.Flags
     {
         public static final int STRIP_BRACKETS = TextField.Flags.START_CUSTOM;
         public static final int START_CUSTOM = STRIP_BRACKETS * 2;
 
         public Flags()
         {
             setFlag(STRIP_BRACKETS);
         }
 
         public Flags(State dfs)
         {
             super(dfs);
             setFlag(STRIP_BRACKETS);
         }
 
         public FlagDefn[] getFlagsDefns()
         {
             return PHONE_FIELD_FLAG_DEFNS;
         }
     }
 
    public class State extends TextFieldState
     {
         public State(DialogContext dc, DialogField field)
         {
             super(dc, field);
         }
     }
 
     public static class Style extends XdmEnumeratedAttribute
     {
         public static final int DASH = 0;
         public static final int BRACKET = 1;
 
         public static final String[] VALUES = new String[] { "dash", "bracket" };
 
         public Style()
         {
         }
 
         public Style(int valueIndex)
         {
             super(valueIndex);
         }
 
         public String[] getValues()
         {
             return VALUES;
         }
     }
 
     private Style style;
 
     public PhoneField()
     {
         super();
         setStyle(new Style(Style.DASH));
     }
 
     public DialogField.State constructStateInstance(DialogContext dc)
     {
         return new PhoneField.State(dc, this);
     }
 
     public Class getStateClass()
     {
         return PhoneField.State.class;
     }
 
     public PhoneField.Style getStyle()
     {
         return style;
     }
 
     public DialogFieldFlags createFlags()
     {
         return new Flags();
     }
 
     public void setStyle(PhoneField.Style style)
     {
         this.style = style;
         switch(this.style.getValueIndex())
         {
             case Style.DASH:
                 setRegExpr("/" + DASH_VALIDATE_PATTERN + "/");
                 setInvalidRegExMessage(DASH_VALIDATE_ERROR_MSG);
                 setDisplayPattern(DASH_DISPLAY_PATTERN);
                 setSubmitPattern(DASH_SUBMIT_PATTERN);
                 break;
 
             case Style.BRACKET:
                 setRegExpr("/" + BRACKET_VALIDATE_PATTERN + "/");
                 setInvalidRegExMessage(BRACKET_VALIDATE_ERROR_MSG);
                 setDisplayPattern(BRACKET_DISPLAY_PATTERN);
                 setSubmitPattern(BRACKET_SUBMIT_PATTERN);
                 break;
         }
     }
 
     public String getSubmitPattern()
     {
         return getFlags().flagIsSet(Flags.STRIP_BRACKETS) ? super.getSubmitPattern() : null;
     }
 
     /**
      *  Passes on the phone format to the client side validations
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
         buf.append("field.phone_format_type = '" + style.getValue() + "';\n");
         return buf.toString();
     }
 }
