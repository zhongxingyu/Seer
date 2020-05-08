 package com.xaf.form.field;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      American Red Cross
  * @author Sreedhar Goparaju
  * @version 1.0
  */
 
 import java.util.*;
 import org.w3c.dom.*;
 import com.xaf.form.*;
 
 public class PhoneField extends TextField
 {
     static public final long FLDFLAG_STRIPBRACKETS = TextField.FLDFLAG_STARTCUSTOM;
	public static final String DASH_PATTERN_MATCH  = "^([\\d][\\d][\\d])[\\.-]?([\\d][\\d][\\d])[\\.-]?([\\d]{4})[ ]?([x][\\d]{1,5})?$";
    public static final String BRACKET_PATTERN_MATCH = "^[\\(]?([\\d][\\d][\\d])[\\)]?[ ]?([\\d][\\d][\\d])[\\.-]?([\\d]{4})[ ]?([x][\\d]{1,5})?$";
 
     private String formatType;
 
     public PhoneField()
     {
         super();
         setFlag(FLDFLAG_STRIPBRACKETS);
         // set the dafault regex pattern for the phone field
         setValidatePattern("/" + DASH_PATTERN_MATCH + "/");
         setValidatePatternErrorMessage("Input must be in the 999-999-9999 x99999 format.");
         this.formatType = "dash";
     }
 
     public void importFromXml(Element elem)
 	{
         super.importFromXml(elem);
 		String attr = elem.getAttribute("strip-brackets");
 		if(attr.equals("no"))
             clearFlag(FLDFLAG_STRIPBRACKETS);
 
         attr = elem.getAttribute("format-type");
         if (attr == null || attr.equals("dash"))
         {
             this.formatType = "dash";
             setValidatePattern("/" + DASH_PATTERN_MATCH + "/");
             setValidatePatternErrorMessage("Input must be in the 999-999-9999 x99999 format.");
         }
         else if (attr.equals("bracket"))
         {
             this.formatType = "bracket";
             setValidatePattern("/" + BRACKET_PATTERN_MATCH + "/");
             setValidatePatternErrorMessage("Input must be in the (999)999-9999 x99999 format.");
         }
 
         if(flagIsSet(FLDFLAG_STRIPBRACKETS))
 		{
             if (this.formatType.equals("dash"))
 			    setSubstitutePattern("s/" + DASH_PATTERN_MATCH + "/$1$2$3$4/g");
             else
                 setSubstitutePattern("s/" + BRACKET_PATTERN_MATCH + "/$1$2$3$4/g");
 		}
 	}
 
     /**
      *  Passes on the phone format to the client side validations
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
         buf.append("field.phone_format_type = '" + this.formatType + "';\n");
         return buf.toString();
     }
 }
