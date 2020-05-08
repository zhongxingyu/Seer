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
 * $Id: NavigationPageUrlCmdValue.java,v 1.4 2003-02-02 16:38:06 roque.hernandez Exp $
  */
 
 package com.netspective.sparx.util.value;
 
 public class NavigationPageUrlCmdValue extends NavigationPageUrlValue
 {
     private String cmd;
 
     public NavigationPageUrlCmdValue()
     {
         super();
     }
 
     public SingleValueSource.Documentation getDocumentation()
     {
         return new SingleValueSource.Documentation(
                 "Retrieves the URL necesary to refer to a NavigationPage defined in a NavigationTree (WEB-INF/ui/structure.xml). If " +
                 "no source-name is provided the navigation-id requested is read from the default NavigationTreeManager " +
                 "of the default configuration file. If a source-name is provided, then the property-name is read from the " +
                 "NavigationTreeManager named source-name in the default configuration file.  The URL is obtained by combining " +
                 "the page controller's URL, the page's id and retain-params from both the page and controller.  The req-params can " +
                 "can be defined in the form of a=b, a=${b} or simply a separated by & (i.e. a=b&c). When no equals sign and value is provided, it's assumed that " +
                 "the value is comming from the request value source and that it has the same name.  The cmd value definition follows " +
                 "exactly the definition of a command.  It is mainly translated from: navigation-id,cmd-param1,cmd-param2,cmd-param3 " +
                 "directly to a request parameter that looks like this: cmd=cmd-param1,cmd-param2,cmd-param3...",
                 new String[]{"navigation-id", "navigation-id,cmd", "navigation-id?req-params",  "navigation-d,cmd?req-params", "source-name/navigation-id", "source-name/navigation-id,cmd", "source-name/navigation-id?req-params",  "source-name/navigation-d,cmd?req-params"}
         );
     }
 
     public void initializeSource(String srcParams)
     {
         super.initializeSource(srcParams);
 
         //Since the super class puts everthing from the begining of the string to either ? or the end,
         //the cmd would fall within from what the super class considers the navId.  So, all we have to do
         //is parse the cmd out of the navId, if any.
         int endOfIdDelimPos = navId.indexOf(',');
 
         if (endOfIdDelimPos < 0 ) {
             cmd = null;
             return;
         }
         cmd = navId.substring(endOfIdDelimPos + 1);
         navId = navId.substring(0, endOfIdDelimPos);
     }
 
     public String getValue(ValueContext vc)
     {
         String url = super.getValue(vc);
 
         if (cmd != null && cmd.length() > 0)
            url = url + (url.indexOf('?') > 0 ? "&" : "?") + "cmd=" + cmd;
 
         return url;
 
     }
 }
