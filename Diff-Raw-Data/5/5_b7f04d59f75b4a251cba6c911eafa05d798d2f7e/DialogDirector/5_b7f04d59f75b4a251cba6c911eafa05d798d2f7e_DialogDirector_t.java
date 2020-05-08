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
 * $Id: DialogDirector.java,v 1.18 2003-12-10 15:04:09 roque.hernandez Exp $
  */
 
 package com.netspective.sparx.form;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.sparx.form.field.type.DirectorNextActionsSelectField;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.handler.DialogNextActionProvider;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Director class is a special dialog field that is used to generate dialog buttons
  * associated with the execution and navigation of the dialog. By default, it produces
  * the <code>Submit</code> and <code>Cancel</code> buttons.
  */
 public class DialogDirector extends DialogField implements DialogNextActionProvider
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     private static final String[] STYLE_ENUM_VALUES = new String[] { "data", "confirm", "acknowledge" };
 
     /**
      * Class for handling different display styles of the directory. These are preset styles that will change
      * the display, captions and URLs of the default Submit and Cancel buttons. If none of the preset styles
      * are suitable, the individual buttons can be managed using the button specific XML settings such as
      * 'submit-caption', 'submit-url', 'cancel-caption', and 'cancel-url'.
      */
     public static class DialogDirectorStyle extends XdmEnumeratedAttribute
     {
         public static final int DATA = 0;
         public static final int CONFIRM = 1;
         public static final int ACKNOWLEDGE = 2;
 
         public DialogDirectorStyle()
         {
         }
 
         public DialogDirectorStyle(int valueIndex)
         {
             super(valueIndex);
         }
 
         /**
          * Gets the list of styles
          * @return
          */
         public String[] getValues()
         {
             return STYLE_ENUM_VALUES;
         }
     }
 
     private ValueSource submitCaption;
     private ValueSource cancelCaption;
     private ValueSource pendingCaption;
     private ValueSource submitActionUrl;
     private ValueSource cancelActionUrl;
     private ValueSource pendingActionUrl;
     private DialogDirectorStyle style;
     private DirectorNextActionsSelectField nextActionsField;
 
     public DialogDirector()
     {
         setName("director");
     }
 
     public DialogDirectorStyle getStyle()
     {
         return style;
     }
 
     /**
      * Sets the style for the dialog director. Currently available styles are data, confirm, and acknowledge.
      * @param style
      */
     public void setStyle(DialogDirectorStyle style)
     {
         this.style = style;
         switch(this.style.getValueIndex())
         {
             case DialogDirectorStyle.DATA:
                 setSubmitCaption(ValueSources.getInstance().getValueSourceOrStatic("  Save  "));
                 break;
 
             case DialogDirectorStyle.CONFIRM:
                 setSubmitCaption(ValueSources.getInstance().getValueSourceOrStatic("  Yes  "));
                 setCancelCaption(ValueSources.getInstance().getValueSourceOrStatic("  No   "));
                 break;
 
             case DialogDirectorStyle.ACKNOWLEDGE:
                 setCancelCaption(null);
                 setCancelActionUrl(null);
                 break;
         }
     }
 
     public ValueSource getCancelActionUrl()
     {
         return cancelActionUrl;
     }
 
     /**
      * Sets the cancel action URl
      * @param cancelActionUrl
      */
     public void setCancelActionUrl(ValueSource cancelActionUrl)
     {
         this.cancelActionUrl = cancelActionUrl;
     }
 
     public ValueSource getCancelCaption()
     {
         return cancelCaption;
     }
 
     /**
      * Sets the cancel action caption
      * @param cancelCaption
      */
     public void setCancelCaption(ValueSource cancelCaption)
     {
         this.cancelCaption = cancelCaption;
     }
 
     public ValueSource getPendingActionUrl()
     {
         return pendingActionUrl;
     }
 
     /**
      * Sets the pending URL
      * @param pendingActionUrl
      */
     public void setPendingActionUrl(ValueSource pendingActionUrl)
     {
         this.pendingActionUrl = pendingActionUrl;
     }
 
     public ValueSource getPendingCaption()
     {
         return pendingCaption;
     }
 
     /**
      * Sets the caption for the submit button during pending stage
      * @param pendingCaption
      */
     public void setPendingCaption(ValueSource pendingCaption)
     {
         this.pendingCaption = pendingCaption;
     }
 
     public ValueSource getSubmitActionUrl()
     {
         return submitActionUrl;
     }
 
     /**
      * Sets the URL for the submit button
      * @param submitActionUrl
      */
     public void setSubmitActionUrl(ValueSource submitActionUrl)
     {
         this.submitActionUrl = submitActionUrl;
     }
 
     public ValueSource getSubmitCaption()
     {
         return submitCaption;
     }
 
     /**
      * Sets the caption for the submit button
      * @param submitCaption
      */
     public void setSubmitCaption(ValueSource submitCaption)
     {
         this.submitCaption = submitCaption;
     }
 
     public DirectorNextActionsSelectField getNextActionsField()
     {
         return nextActionsField;
     }
 
     /**
      * Sets the select field used for showing the next actions
      * @param nextActionsField
      */
     public void setNextActionsField(DirectorNextActionsSelectField nextActionsField)
     {
         this.nextActionsField = nextActionsField;
     }
 
     public boolean hasNextAction()
     {
         return nextActionsField != null;
     }
 
     /**
      * Gets the next action URL based on the selected next action item
      * @param dc
      * @return
      */
     public String getDialogNextActionUrl(DialogContext dc, String defaultUrl)
     {
         return nextActionsField == null ? defaultUrl : nextActionsField.getDialogNextActionUrl(dc, defaultUrl);
     }
 
     /**
      * Adds a next action item
      * @param nextActionsField
      */
     public void addNextActions(DirectorNextActionsSelectField nextActionsField)
     {
         this.nextActionsField = nextActionsField;
         this.nextActionsField.setParent(this);
     }
 
     public void makeStateChanges(DialogContext dc, int stage)
     {
         super.makeStateChanges(dc, stage);
         if(nextActionsField != null)
             nextActionsField.makeStateChanges(dc, stage);
     }
 
     /**
      * Produces the HTML for the action buttons of the dialog
      * @param writer    the Writer object associated with the response buffer
      * @param dc        dialog context
      * @throws IOException
      */
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         Dialog dialog = dc.getDialog();
         String attrs = dc.getSkin().getDefaultControlAttrs();
 
         String submitCaption = "   OK   ";
         String cancelCaption = " Cancel ";
 
         int dataCmd = (int) dc.getDialogState().getPerspectives().getFlags();
         switch(dataCmd)
         {
             case DialogPerspectives.ADD:
             case DialogPerspectives.EDIT:
                 submitCaption = " Save ";
                 break;
 
             case DialogPerspectives.DELETE:
                 submitCaption = " Delete ";
                 break;
 
             case DialogPerspectives.CONFIRM:
                 submitCaption = "  Yes  ";
                 cancelCaption = "  No   ";
                 break;
         }
 
         submitCaption = (this.submitCaption != null) ? this.submitCaption.getTextValue(dc) : submitCaption;
         cancelCaption = (this.cancelCaption != null) ? this.cancelCaption.getTextValue(dc) : cancelCaption;
 
         writer.write("<center>");
 
         if(nextActionsField != null && nextActionsField.isAvailable(dc))
         {
             String caption = nextActionsField.getCaption().getTextValue(dc);
             if(caption != null && !nextActionsField.isInputHidden(dc))
                 writer.write(caption);
 
             nextActionsField.renderControlHtml(writer, dc);
 
             if(caption != null && !nextActionsField.isInputHidden(dc))
                 writer.write("&nbsp;&nbsp;");
         }
 
         writer.write("<input type='submit' name='"+ dialog.getSubmitDataParamName() +"' class=\"dialog-button\" value='");
         writer.write(submitCaption);
         writer.write("' ");
         writer.write(attrs);
         writer.write(">&nbsp;&nbsp;");
 
        if(pendingCaption != null && pendingCaption.getTextValue(dc) != null && pendingCaption.getTextValue(dc).length() > 0)
         {
             writer.write("<input type='submit' class=\"dialog-button\" name='"+ dialog.getPendDataParamName() +"' value='");
             writer.write(pendingCaption.getTextValue(dc));
             writer.write("' ");
             writer.write(attrs);
             writer.write(">&nbsp;&nbsp;");
         }
         if (cancelCaption != null && cancelCaption.length() > 0)
         {
             if (dialog.getDialogFlags().flagIsSet(DialogFlags.ALLOW_EXECUTE_WITH_CANCEL_BUTTON))
             {
                 // treat the cancel button as a submit button
                 writer.write("<input type='submit' name='"+ dialog.getCancelDataParamName() +"' class=\"dialog-button\" value='");
                 writer.write(cancelCaption);
                 writer.write("' ");
                 writer.write(attrs);
                 writer.write(">&nbsp;&nbsp;");
             }
             else
             {
                 writer.write("<input type='button' name='" + dialog.getCancelDataParamName() + "' class=\"dialog-button\" value='");
                 writer.write(cancelCaption);
                 writer.write("' ");
                 if(cancelActionUrl == null)
                 {
                     String referer = dc.getDialogState().getReferer();
                     if(referer != null)
                     {
                         writer.write("onclick=\"document.location = '");
                         writer.write(referer);
                         writer.write("'\" ");
                     }
                     else
                     {
                         writer.write("onclick=\"history.back()\" ");
                     }
                 }
                 else
                 {
                     String cancelStr = cancelActionUrl != null ? cancelActionUrl.getTextValue(dc) : null;
                     if("back".equals(cancelStr))
                     {
                         writer.write("onclick=\"history.back()\" ");
                     }
                     else if(cancelStr != null && cancelStr.startsWith("javascript:"))
                     {
                         writer.write("onclick=\"");
                         writer.write(cancelStr);
                         writer.write("\" ");
                     }
                     else
                     {
                         writer.write("onclick=\"document.location = '");
                         writer.write(cancelStr);
                         writer.write("'\" ");
                     }
                 }
                 writer.write(attrs);
                 writer.write(">");
             }
 
         }
         writer.write("</center>");
     }
 }
