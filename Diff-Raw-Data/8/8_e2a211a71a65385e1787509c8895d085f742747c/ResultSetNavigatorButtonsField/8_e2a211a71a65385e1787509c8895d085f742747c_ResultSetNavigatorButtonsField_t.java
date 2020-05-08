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
 * $Id: ResultSetNavigatorButtonsField.java,v 1.2 2002-08-24 05:34:54 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.querydefn;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.text.NumberFormat;
 
 import org.w3c.dom.Element;
 
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.DialogField;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.StaticValue;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 
 public class ResultSetNavigatorButtonsField extends DialogField
 {
     static public SingleValueSource SUBMIT_CAPTION = new StaticValue(" OK ");
     static public SingleValueSource FIRST_CAPTION = new StaticValue(" First ");
     static public SingleValueSource PREV_CAPTION = new StaticValue(" Previous ");
     static public SingleValueSource NEXT_CAPTION = new StaticValue(" Next ");
     static public SingleValueSource LAST_CAPTION = new StaticValue(" Last ");
     static public SingleValueSource DONE_CAPTION = new StaticValue(" Done ");
 
     private SingleValueSource submitCaption = SUBMIT_CAPTION;
     private SingleValueSource firstCaption = FIRST_CAPTION;
     private SingleValueSource prevCaption = PREV_CAPTION;
     private SingleValueSource nextCaption = NEXT_CAPTION;
     private SingleValueSource lastCaption = LAST_CAPTION;
     private SingleValueSource doneCaption = DONE_CAPTION;
     private SingleValueSource doneUrl;
 
     public ResultSetNavigatorButtonsField()
     {
         this("rs_nav_buttons");
         setFlag(DialogField.FLDFLAG_INVISIBLE);
     }
 
     public ResultSetNavigatorButtonsField(String name)
     {
         super(name, null);
         setFlag(DialogField.FLDFLAG_INVISIBLE);
     }
 
     public void importFromXml(Element elem)
     {
         super.importFromXml(elem);
 
         String captionText = elem.getAttribute("submit-caption");
         if(captionText.length() > 0)
             submitCaption = ValueSourceFactory.getSingleOrStaticValueSource(captionText);
 
         captionText = elem.getAttribute("first-caption");
         if(captionText.length() > 0)
             firstCaption = ValueSourceFactory.getSingleOrStaticValueSource(captionText);
 
         captionText = elem.getAttribute("previous-caption");
         if(captionText.length() > 0)
             prevCaption = ValueSourceFactory.getSingleOrStaticValueSource(captionText);
 
         captionText = elem.getAttribute("next-caption");
         if(captionText.length() > 0)
             nextCaption = ValueSourceFactory.getSingleOrStaticValueSource(captionText);
 
         captionText = elem.getAttribute("last-caption");
         if(captionText.length() > 0)
             lastCaption = ValueSourceFactory.getSingleOrStaticValueSource(captionText);
 
         captionText = elem.getAttribute("done-caption");
         if("invisible".equals(captionText))
             doneCaption = null;
         else if(captionText.length() > 0)
             doneCaption = ValueSourceFactory.getSingleOrStaticValueSource(captionText);
 
         String doneUrlText = elem.getAttribute("done-url");
         if(doneUrlText.length() > 0)
             doneUrl = ValueSourceFactory.getSingleOrStaticValueSource(doneUrlText);
         else
             doneUrl = null;
     }
 
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         String attrs = dc.getSkin().getDefaultControlAttrs();
         QuerySelectScrollState state = (QuerySelectScrollState) dc.getRequest().getAttribute(dc.getTransactionId() + "_state");
         if(state == null)
         {
             writer.write("<input type='submit' name='" + dc.getDialog().getResetContextParamName() + "' value='" + submitCaption.getValue(dc) + "' " + attrs + "> ");
             return;
         }
 
         boolean isScrollable = state.isScrollable();
         int activePage = state.getActivePage();
         int lastPage = state.getTotalPages();
 
         writer.write("<center>");
         if(lastPage > 0)
         {
             writer.write("<nobr>Page ");
            writer.write(Integer.toString(activePage));
             if(isScrollable)
             {
                 writer.write(" of ");
                writer.write(Integer.toString(lastPage));
             }
             writer.write("</nobr>&nbsp;&nbsp;");
             if(activePage > 1)
                 writer.write("<input type='submit' name='rs_nav_first' value='" + firstCaption.getValue(dc) + "' " + attrs + "> ");
 
             if(activePage > 2)
                 writer.write("<input type='submit' name='rs_nav_prev' value='" + prevCaption.getValue(dc) + "' " + attrs + "> ");
 
             boolean hasMoreRows = false;
             try
             {
                 if(state.hasMoreRows())
                 {
                     writer.write("<input type='submit' name='rs_nav_next' value='" + nextCaption.getValue(dc) + "' " + attrs + "> ");
                     hasMoreRows = true;
                 }
             }
             catch(SQLException e)
             {
                 writer.write(e.toString());
             }
 
             if(isScrollable)
             {
                 if(activePage < lastPage)
                     writer.write("<input type='submit' name='rs_nav_last' value='" + lastCaption.getValue(dc) + "' " + attrs + "> ");
                 writer.write("&nbsp;&nbsp;<nobr>");
                 writer.write(NumberFormat.getNumberInstance().format(state.getTotalRows()));
                 writer.write(" total rows</nobr>");
             }
             else if(hasMoreRows)
             {
                 writer.write("&nbsp;&nbsp;<nobr>");
                 writer.write(NumberFormat.getNumberInstance().format(state.getRowsProcessed()));
                 writer.write(" rows so far</nobr>");
             }
             else
             {
                 writer.write("&nbsp;&nbsp;<nobr>");
                 writer.write(NumberFormat.getNumberInstance().format(state.getRowsProcessed()));
                 writer.write(" total rows</nobr>");
             }
 
         }
 
         if(doneCaption != null)
         {
             if(doneUrl == null)
                 writer.write("&nbsp;&nbsp;<input type='submit' name='" + dc.getDialog().getResetContextParamName() + "' value='" + doneCaption.getValue(dc) + "' " + attrs + "> ");
             else
                 writer.write("&nbsp;&nbsp;<input type='button' name='jump' value='" + doneCaption.getValue(dc) + "' onclick='location.href=\"" + doneUrl.getValue(dc) + "\"'" + attrs + "> ");
         }
         writer.write("</center>");
     }
 }
