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
 * $Id: NavigationPageUrlValue.java,v 1.2 2003-01-24 12:59:13 roque.hernandez Exp $
  */
 
 package com.netspective.sparx.util.value;
 
 import com.netspective.sparx.xaf.navigate.NavigationTreeManagerFactory;
 import com.netspective.sparx.xaf.navigate.NavigationTreeManager;
 import com.netspective.sparx.xaf.navigate.NavigationPath;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.StringTokenizer;
 
 public class NavigationPageUrlValue extends ValueSource
 {
     protected String source;
     protected String navId;
     protected String reqParamsSource;
     protected ValueSource reqParams;
 
     public NavigationPageUrlValue()
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
                 "can be defined in the form of a=b, a=${b} or simply a. When no equals sign and value is provided, it's assumed that " +
                 "the value is comming from the request value source and that it has the same name.",
                 new String[]{"navigation-id", "navigation-id?req-params", "source-name/navigation-id", "source-name/navigation-id?req-params" }
         );
     }
 
     public void initializeSource(String srcParams)
     {
         int delimPos = srcParams.indexOf('/');
         if(delimPos > 0)
         {
             source = srcParams.substring(0, delimPos);
             valueKey = srcParams.substring(delimPos + 1);
         }
         else
             valueKey = srcParams;
 
 
         int endOfIdDelimPos = valueKey.indexOf('?');
         if (endOfIdDelimPos < 0) {
             navId = valueKey;
             reqParamsSource = null;
             return;
         }
 
         navId = valueKey.substring(0, endOfIdDelimPos);
         reqParamsSource = parseSrcParams(valueKey.substring(endOfIdDelimPos + 1));
         reqParams = new ConfigurationExprValue();
         reqParams.initializeSource(reqParamsSource);
     }
 
     public String getValue(ValueContext vc)
     {
         NavigationPath navTree = null;
 
         HttpServletRequest request = (HttpServletRequest) vc.getRequest();
         if(request == null)
             return "ValueContext.getRequest() is NULL in " + getId();
 
         String contextPath = request.getContextPath();
 
         if(source == null || source.length() == 0)
         {
             navTree = NavigationTreeManagerFactory.getNavigationTree(vc.getServletContext());
             if(navTree == null)
                 return "No default NavigationTree found in " + getId();
         }
         else
         {
             NavigationTreeManager manager = NavigationTreeManagerFactory.getManager(vc.getServletContext());
             if(manager == null)
                 return "No NavigationTreeManager found in " + getId();
             navTree = manager.getTree(source);
             if(navTree == null)
                 return "No '" + source + "' Configuration found in " + getId();
         }
 
         NavigationPath result = (NavigationPath) navTree.getAbsolutePathsMap().get(navId);
         if(result != null) {
             String controllerUrl = result.getController().getUrl();
             if (controllerUrl == null || controllerUrl.length() == 0 )
                 controllerUrl = "/index.jsp";
             String pageId = result.getId();
             String controllerParams = result.getController().getRetainParamsValue(vc);
             String pageParams = result.getRetainParams(vc);
             String finalParams = resolveRequestPrameters(controllerParams, pageParams);
             String localParams = "";
             if (reqParams != null) {
                 localParams = reqParams.getValue(vc);
             }
             finalParams = resolveRequestPrameters(finalParams, localParams);
             return contextPath + controllerUrl + pageId + (finalParams != null && finalParams.length() > 0 ? "?" + finalParams : "");
         }
         else
             return "Navigation id '"+ navId +"' not found.";
     }
 
     public String resolveRequestPrameters(String origParams, String secondParams){
         if (origParams == null)
             origParams = "";
 
         if (secondParams == null)
             secondParams = "";
 
         StringTokenizer origTokens = new StringTokenizer(origParams, "&");
         StringTokenizer secondTokens = new StringTokenizer(secondParams, "&");
         String finalString = "";
 
         while (origTokens.hasMoreTokens()) {
             String token = origTokens.nextToken();
            String tokenName = token.substring(0, token.indexOf('='));
             int tokenIndexInSecondParams = secondParams.indexOf(tokenName);
 
             if ( tokenIndexInSecondParams >= 0) {
                 int nextTokenDelim = secondParams.indexOf('&', tokenIndexInSecondParams);
                 if (nextTokenDelim < 0)
                     nextTokenDelim = secondParams.length();
                 finalString = ("".equals(finalString) ? finalString : finalString + "&" ) + secondParams.substring(tokenIndexInSecondParams, nextTokenDelim);
             } else {
                 finalString = ("".equals(finalString) ? finalString : finalString + "&" ) + token;
             }
         }
 
         while (secondTokens.hasMoreTokens()) {
             String token = secondTokens.nextToken();
             String tokenName = token.substring(0, token.indexOf('='));
             int tokenIndexInFinalString = finalString.indexOf(tokenName);
 
             if ( tokenIndexInFinalString >= 0) {
                 continue;
             } else {
                 finalString = ("".equals(finalString) ? finalString : finalString + "&" ) + token;
             }
         }
 
         return finalString;
     }
 
     public String parseSrcParams(String originalParams){
         if (originalParams == null)
             return null;
 
         StringTokenizer tokens = new StringTokenizer(originalParams, "&");
         String finalParams = null;
 
         while (tokens.hasMoreTokens()) {
             String token = tokens.nextToken();
             if (token.indexOf('=') > 0) {
                 finalParams = (finalParams == null ? "" : finalParams + "&") + token;
             }
             else {
                 finalParams = (finalParams == null ? "" : finalParams + "&") + token + "=${request:" + token + "}";
             }
 
         }
         return finalParams;
     }
 }
