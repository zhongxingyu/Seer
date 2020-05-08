 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.commons.validate.rule;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.commons.validate.ValidationContext;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 
 public class DateValueValidationRule extends BasicValidationRule
 {
     private static final Log log = LogFactory.getLog(DateValueValidationRule.class);
 
     private String invalidNamedDateMessage = "{0} contains an invalid {1} date: {2} (pattern is {3}).";
     private String futureOnlyDateMessage = "{0} may only contain future dates (after {1}).";
    private String pastOnlyDateMessage = "{0} may only contain past dates (before {1}.";
     private String preMinDateDateMessage = "{0} may only contain dates after {1}.";
     private String postMaxDateMessage = "{0} may only contain dates before {1}.";
     private SimpleDateFormat format;
     private Date minDate;
     private Date maxDate;
     private ValueSource maxDateSource;
     private ValueSource minDateSource;
     private boolean futureOnly;
     private boolean pastOnly;
 
     /**
      * Translates a reserved date word such as "today" or "now" into the actual time
      *
      * @param str reserved string
      *
      * @return String actual time string
      */
     public String translateTimeString(String str)
     {
         String xlatedDate = str;
 
         if(str != null && (str.startsWith("today") || str.startsWith("now")))
         {
             Date dt = new Date();
             xlatedDate = format(dt);
         }
         return xlatedDate;
     }
 
     /**
      * Translates a reserved date word such as "today" or "now" into the actual date
      *
      * @param str reserved string
      *
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
                     xlatedDate = format(dt);
                 }
                 catch(Exception e)
                 {
                     log.error("Unable to translate date string " + str, e);
                 }
             }
             else
             {
                 dt = new Date();
                 xlatedDate = format(dt);
             }
         }
         return xlatedDate;
     }
 
     public SimpleDateFormat getFormat()
     {
         return format;
     }
 
     public void setFormat(SimpleDateFormat format)
     {
         this.format = format;
     }
 
     public String format(Date date)
     {
         synchronized(format)
         {
             return format.format(date);
         }
     }
 
     public Date parse(String text) throws ParseException
     {
         synchronized(format)
         {
             return format.parse(text);
         }
     }
 
     public String toPattern()
     {
         synchronized(format)
         {
             return format.toPattern();
         }
     }
 
     public Date getMaxDate()
     {
         return maxDate;
     }
 
     public void setMaxDate(Date maxDate)
     {
         this.maxDate = maxDate;
     }
 
     public Date getMinDate()
     {
         return minDate;
     }
 
     public void setMinDate(Date minDate)
     {
         this.minDate = minDate;
     }
 
     public ValueSource getMaxDateSource()
     {
         return maxDateSource;
     }
 
     public void setMaxDateSource(ValueSource maxDate) throws ParseException
     {
         if(maxDate instanceof StaticValueSource)
             this.maxDate = parse(translateDateString(maxDate.getTextValue(null)));
         else
             maxDateSource = maxDate;
     }
 
     public ValueSource getMinDateSource()
     {
         return minDateSource;
     }
 
     public void setMinDateSource(ValueSource minDate) throws ParseException
     {
         if(minDate instanceof StaticValueSource)
             this.minDate = parse(translateDateString(minDate.getTextValue(null)));
         else
             minDateSource = minDate;
     }
 
     public boolean isFutureOnly()
     {
         return futureOnly;
     }
 
     public void setFutureOnly(boolean futureOnly)
     {
         this.futureOnly = futureOnly;
     }
 
     public boolean isPastOnly()
     {
         return pastOnly;
     }
 
     public void setPastOnly(boolean pastOnly)
     {
         this.pastOnly = pastOnly;
     }
 
     public String getInvalidNamedDateMessage()
     {
         return invalidNamedDateMessage;
     }
 
     public void setInvalidNamedDateMessage(String invalidNamedDateMessage)
     {
         this.invalidNamedDateMessage = invalidNamedDateMessage;
     }
 
     public String getFutureOnlyDateMessage()
     {
         return futureOnlyDateMessage;
     }
 
     public void setFutureOnlyDateMessage(String futureOnlyDateMessage)
     {
         this.futureOnlyDateMessage = futureOnlyDateMessage;
     }
 
     public String getPastOnlyDateMessage()
     {
         return pastOnlyDateMessage;
     }
 
     public void setPastOnlyDateMessage(String pastOnlyDateMessage)
     {
         this.pastOnlyDateMessage = pastOnlyDateMessage;
     }
 
     public String getPostMaxDateMessage()
     {
         return postMaxDateMessage;
     }
 
     public void setPostMaxDateMessage(String postMaxDateMessage)
     {
         this.postMaxDateMessage = postMaxDateMessage;
     }
 
     public String getPreMinDateDateMessage()
     {
         return preMinDateDateMessage;
     }
 
     public void setPreMinDateDateMessage(String preMinDateDateMessage)
     {
         this.preMinDateDateMessage = preMinDateDateMessage;
     }
 
     public Date getValueSourceOrDate(String name, Value value, ValueSource vs, ValidationContext vc, Date date)
     {
         if(vs == null)
             return date;
 
         String dateText = vs.getTextValue(vc.getValidationValueContext());
         if(dateText == null)
             return date;
 
         try
         {
             return parse(dateText);
         }
         catch(ParseException e)
         {
             vc.addValidationError(value, getInvalidNamedDateMessage(),
                                   new Object[]{getValueCaption(vc), name, dateText, toPattern()});
             return date;
         }
     }
 
     public boolean isValid(ValidationContext vc, Value value)
     {
         if(!isValidType(vc, value, Date.class))
             return false;
 
         Date dateValue = (Date) value.getValue();
         if(dateValue == null)
             return true;
 
         if(pastOnly || futureOnly)
         {
             Date now = new Date();
             if(pastOnly && dateValue.after(now))
             {
                 vc.addValidationError(value, getPastOnlyDateMessage(), new Object[]{getValueCaption(vc), format(now)});
                 return false;
             }
             if(futureOnly && dateValue.before(now))
             {
                 vc.addValidationError(value, getFutureOnlyDateMessage(), new Object[]{
                     getValueCaption(vc), format(now)
                 });
                 return false;
             }
         }
 
         Date minimumDate = getValueSourceOrDate("Minimum", value, minDateSource, vc, minDate);
         if(minimumDate != null && dateValue.before(minimumDate))
         {
             vc.addValidationError(value, getPreMinDateDateMessage(), new Object[]{
                 getValueCaption(vc), format(minimumDate)
             });
             return false;
         }
 
         Date maximumDate = getValueSourceOrDate("Maximum", value, maxDateSource, vc, maxDate);
         if(maximumDate != null && dateValue.after(maximumDate))
         {
             vc.addValidationError(value, getPostMaxDateMessage(), new Object[]{
                 getValueCaption(vc), format(maximumDate)
             });
             return false;
         }
 
         return true;
     }
 }
