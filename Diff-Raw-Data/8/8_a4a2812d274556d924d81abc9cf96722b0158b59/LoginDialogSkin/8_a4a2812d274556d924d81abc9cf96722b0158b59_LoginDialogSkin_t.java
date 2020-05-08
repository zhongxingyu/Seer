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
 * $Id: LoginDialogSkin.java,v 1.2 2003-08-10 00:34:32 shahid.shah Exp $
  */
 
 package com.netspective.sparx.theme.basic;
 
 import java.io.Writer;
 import java.io.IOException;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.commons.value.ValueSource;
 
 public class LoginDialogSkin extends StandardDialogSkin
 {
     private String loginImage = "netspective-keys.gif";
 
     public LoginDialogSkin(Theme theme, String panelClassNamePrefix, String panelResourcesPrefix, boolean fullWidth)
     {
         super(theme, panelClassNamePrefix, panelResourcesPrefix, fullWidth);
     }
 
     public String getLoginImage()
     {
         return loginImage;
     }
 
     public void setLoginImage(String loginImage)
     {
         this.loginImage = loginImage;
     }
 
     public void renderContentsHtml(Writer writer, DialogContext dc, String dialogName, String actionURL, String encType, int dlgTableColSpan, StringBuffer errorMsgsHtml, StringBuffer fieldsHtml) throws IOException
     {
         String themeImagesRootUrl = dc.getNavigationContext().getThemeImagesRootUrl(dc.getActiveTheme());
         ValueSource heading = dc.getDialog().getFrame().getHeading();
 
        writer.write("        <table width=\"50%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
         writer.write("            <tr>");
        writer.write("                <td class=\"panel-input-content\">");
         writer.write("                    <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
         if(heading != null)
         {
             writer.write("                        <tr height=\"30\">");
             writer.write("                            <td class=\"dialog-fields-header\" align=\"left\" valign=\"bottom\" height=\"30\">" +
                     heading.getTextValue(dc) + "</td>");
             writer.write("                        </tr>");
         }
         writer.write("                        <tr>");
         writer.write("                            <td class=\"dialog-pattern\">");
         writer.write("                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"15\">");
         writer.write("                                    <tr>");
         writer.write("                                        <td align=\"center\" valign=\"middle\">" +
                 "<img src=\"" + themeImagesRootUrl + "/login/"+ getLoginImage() +"\" " +
                 "alt=\"\" border=\"0\"></td>");
         writer.write("                                        <td align=\"left\" valign=\"middle\">");
 
         writer.write("          <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
 
         if(summarizeErrors)
             writer.write(errorMsgsHtml.toString());
 
         writer.write(
                 "<form id='" + dialogName + "' name='" + dialogName + "' action='" + actionURL + "' method='post' " +
                 encType + " onsubmit='return(activeDialog.isValid())'>\n" +
                 dc.getStateHiddens() + "\n" +
                 fieldsHtml +
                 "</form>\n");
 
         writer.write("          </table>\n");
 
         writer.write("                                        </td>");
         writer.write("                                    </tr>");
         writer.write("                                </table>");
         writer.write("                            </td>");
         writer.write("                        </tr>");
         writer.write("                    </table>");
         writer.write("                </td>");
         writer.write("            </tr>");
         writer.write("        </table>");
     }
 
     /**
      * We typically take over the entire page for login dialog so lets give some spacing at the top and center
      * ourselves.
      * @param writer
      * @param dc
      * @throws IOException
      */
     public void renderHtml(Writer writer, DialogContext dc) throws IOException
     {
         writer.write("<p>&nbsp;<p>&nbsp;<p>&nbsp;<p><center>");
         super.renderHtml(writer, dc);
         writer.write("</center>");
     }
 }
