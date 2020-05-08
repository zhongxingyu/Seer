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
 * $Id: DirectorNextActionsSelectField.java,v 1.4 2002-11-28 21:30:27 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.form.field;
 
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.DialogField;
 import com.netspective.sparx.xaf.form.conditional.DialogFieldConditionalApplyFlag;
 import org.w3c.dom.Element;
 
 public class DirectorNextActionsSelectField extends SelectField
 {
     public static final String DEFAULT_NAME = "director_next_actions";
 
     private int dataCmdCondition = DialogContext.DATA_CMD_NONE;
     private boolean displayOneItemOnly = false;
 
     public void importFromXml(Element elem)
     {
         super.importFromXml(elem);
 
         if (getSimpleName() == null)
             setSimpleName(DEFAULT_NAME);
 
         //if the current data-cmd does not match one listed in the XML definition, then the selected value (if initial entry
         // the first value) will be part of the dialog as hidden.
         String dataCmd = elem.getAttribute("data-cmd");
         if (dataCmd.length() > 0)
         {
             setDataCmdCondition(dataCmd);
             setFlag(DialogField.FLDFLAG_INPUT_HIDDEN);
             DialogFieldConditionalApplyFlag dataCmdAction = new DialogFieldConditionalApplyFlag(this, DialogField.FLDFLAG_INPUT_HIDDEN);
             dataCmdAction.setClearFlag(true);
             dataCmdAction.setDataCmd(dataCmdCondition);
             addConditionalAction(dataCmdAction);
         }
 
         String displayOnlyOne = elem.getAttribute("display-one");
         if ("yes".equals(displayOnlyOne))
             displayOneItemOnly = true;
 
         String persist = elem.getAttribute("persist");
         if (! "no".equals(persist))
             setFlag(FLDFLAG_PERSIST);
     }
 
     /**
      * The next actions field is a SelectField which has a caption and a value. The caption is displayed to the
      * user and the value is a URL which indicates where they want to go next. The URL can be either a String or
      * a SingleValueSource that can dynamically compute the next location.
      */
     public String getSelectedActionUrl(DialogContext dc)
     {
         String value = dc.getRequest().getParameter(getId());
         if (value == null)
             return null;
         SingleValueSource svs = ValueSourceFactory.getSingleOrStaticValueSource(value);
         if (svs == null)
             return null;
         return svs.getValue(dc);
     }
 
     public void makeStateChanges(DialogContext dc, int stage)
     {
         super.makeStateChanges(dc, stage);
 
         int listSize = this.getListSource().getSelectChoices(dc).getValues().length;
         if (listSize == 1 && !displayOneItemOnly)
         {
             dc.setFlag(this.getQualifiedName(), DialogField.FLDFLAG_INPUT_HIDDEN);
         }
     }
 
     public int getDataCmdCondition()
     {
         return dataCmdCondition;
     }
 
     public void setDataCmdCondition(int condition)
     {
         dataCmdCondition = condition;
     }
 
     public void setDataCmdCondition(String condition)
     {
         dataCmdCondition = DialogContext.getDataCmdIdForCmdText(condition);
     }
 
     public boolean isDisplayOneItemOnly()
     {
         return displayOneItemOnly;
     }
 
     public void setDisplayOneItemOnly(boolean displayOneItemOnly)
     {
        this.displayOneItemOnly = displayOneItemOnly;
     }
 
 
 }
