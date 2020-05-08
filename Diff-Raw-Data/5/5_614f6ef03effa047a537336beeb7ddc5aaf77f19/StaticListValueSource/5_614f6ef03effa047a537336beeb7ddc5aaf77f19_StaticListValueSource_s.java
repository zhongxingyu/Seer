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
 * $Id: StaticListValueSource.java,v 1.2 2003-05-13 19:51:51 shahid.shah Exp $
  */
 
 package com.netspective.commons.value.source;
 
 import com.netspective.commons.value.ValueSourceSpecification;
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.exception.ValueSourceInitializeException;
 import com.netspective.commons.value.PresentationValue;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.text.TextUtils;
 
 public class StaticListValueSource extends AbstractValueSource
 {
     public static final String[] IDENTIFIERS = new String[] { "text-list", "strings" };
 
     public static String[] getIdentifiers()
     {
         return IDENTIFIERS;
     }
 
     private PresentationValue staticValue = new PresentationValue();
 
     public StaticListValueSource()
     {
         super();
     }
 
     public StaticListValueSource(String[] staticValues)
     {
         PresentationValue.Items items = staticValue.createItems();
         if(staticValues != null)
         {
             for(int i = 0; i < staticValues.length; i++)
                 items.addItem(staticValues[i]);
         }
     }
 
     public void initialize(ValueSourceSpecification spec) throws ValueSourceInitializeException
     {
         super.initialize(spec);
         String pi = spec.getProcessingInstructions();
        String[] textItems = TextUtils.split(spec.getParams(), pi != null ? pi.substring(0, 0) : ",", false);
         PresentationValue.Items items = staticValue.createItems();
         if(textItems != null && textItems.length > 0)
         {
             char valueCaptionDelim = pi != null ? (pi.length() > 1 ? pi.charAt(1) : '=') : '=';
             for(int i = 0; i < textItems.length; i++)
             {
                 String item = textItems[i];
                 int valueCaptionDelimPos = item.indexOf(valueCaptionDelim);
                 if(valueCaptionDelimPos > 0)
                     items.addItem(item.substring(0, valueCaptionDelimPos), item.substring(valueCaptionDelimPos+1));
                 else
                     items.addItem(item);
             }
         }
     }
 
     public String[] getTextValues(ValueContext vc)
     {
         return staticValue.getTextValues();
     }
 
     public PresentationValue getPresentationValue(ValueContext vc)
     {
         return staticValue;
     }
 
     public Value getValue(ValueContext vc)
     {
         return staticValue;
     }
 
     public boolean hasValue(ValueContext vc)
     {
         return staticValue.hasValue();
     }
 }
