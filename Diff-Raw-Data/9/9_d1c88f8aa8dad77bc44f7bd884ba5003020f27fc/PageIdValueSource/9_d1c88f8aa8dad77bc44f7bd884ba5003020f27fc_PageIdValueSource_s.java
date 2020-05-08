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
 * $Id: PageIdValueSource.java,v 1.3 2003-10-28 10:57:19 shahid.shah Exp $
  */
 
 package com.netspective.sparx.value.source;
 
 import com.netspective.commons.value.source.AbstractValueSource;
 import com.netspective.commons.value.source.ValueSrcExpressionValueSource;
 import com.netspective.commons.value.*;
 import com.netspective.commons.value.exception.ValueSourceInitializeException;
 import com.netspective.sparx.navigate.NavigationPath;
 import com.netspective.sparx.navigate.NavigationTree;
 import com.netspective.sparx.navigate.NavigationPage;
 import com.netspective.sparx.value.HttpServletValueContext;
 import com.netspective.axiom.ConnectionContext;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * This class is for acessing navigation pages defined in the application's <code>project.xml</code>.
  */
 public class PageIdValueSource  extends AbstractValueSource
 {
     private static final Log log = LogFactory.getLog(PageIdValueSource.class);
 
     public static final String[] IDENTIFIERS = new String[] { "page-id" };
     public static final ValueSourceDocumentation DOCUMENTATION = new ValueSourceDocumentation(
             "Provides access to the URL defined in a NavigationTree. If " +
             "no source-name is provided the navigation-id requested is read from the default NavigationTreeManager " +
             "of the default configuration file. If a source-name is provided, then the property-name is read from the " +
             "NavigationTreeManager named source-name in the default configuration file.",
             new ValueSourceDocumentation.Parameter[]
             {
                 new ValueSourceDocumentation.Parameter("navigation id", true, "The relative path of the URI.")
             }
     );
 
     public static final char TREE_SOURCE_SEPARATOR = ',';
     public static final char QUERY_STRING_SEPARATOR = '?';
     /**
      * Gets the static identifiers defined for the navigation page value source class
      * @return
      */
     public static String[] getIdentifiers()
     {
         return IDENTIFIERS;
     }
 
     /**
      * Gets the static documentation object defined for the navigation page value source class
      * @return
      */
     public static ValueSourceDocumentation getDocumentation()
     {
         return DOCUMENTATION;
     }
 
     protected String treeSource;
     protected String pageId;
     protected ValueSourceSpecification reqParamsSource;
     protected ValueSource reqParams;
 
     public PresentationValue getPresentationValue(ValueContext vc)
     {
         return new PresentationValue(getValue(vc));
     }
 
     public boolean hasValue(ValueContext vc)
     {
         return true;
     }
 
     /**
      * Gets the navigation page's URL as a Value object. If the navigation tree or the page itself
      * was not found, a GenericValue object with the error message is returned.
      *
      * @param vc
      * @return
      */
     public Value getValue(ValueContext vc)
     {
         NavigationTree navTree = null;
         HttpServletValueContext svc = (HttpServletValueContext) (vc instanceof ConnectionContext ? ((ConnectionContext) vc).getDatabaseValueContext() : vc);
         HttpServletRequest request = svc.getHttpRequest();
         String contextPath = request.getContextPath();
 
         if(treeSource == null || treeSource.length() == 0)
         {
             navTree = svc.getProject().getDefaultNavigationTree();
             if(navTree == null)
                 return new GenericValue("No default NavigationTree found");
         }
         else
         {
             navTree = navTree = svc.getProject().getNavigationTree(treeSource);
             if(navTree == null)
                 return new GenericValue("No navigation tree named '" + treeSource + "' was found");
         }
 
         if (!pageId.startsWith("/"))
         {
             // relative page ID
             NavigationPage activePage = svc.getNavigationContext().getActivePage();
             if (activePage != null)
                 pageId = activePage.getQualifiedName() + "/" + pageId;
             else
             {
                log.error("Active page was not found for relative page ID '" + pageId + "'.");
                return new GenericValue("Active page was not found for relative page ID '" + pageId + "'.");
             }
         }
         // find a matching path with respect to the nav id
         NavigationTree.FindResults pathResults = navTree.findPath(pageId);
         NavigationPath path = pathResults.getMatchedPath();
         if (path == null)
         {
             // a matching path was not found
             log.error("Navigation page ID '" + pageId + "' was not found.");
             return new GenericValue("Navigation page ID '" + pageId + "' was not found.");
         }
         else
         {
             String localParams = null;
             if (reqParams != null)
             {
                 // process the request parameters
                 localParams = reqParams.getValue(vc).getTextValue();
             }
            return new GenericValue(contextPath + path.getQualifiedName() + (localParams != null ? "?" + localParams : ""));
         }
     }
 
     /**
      * Initializes the navigation page value source by extracting the navigation tree name and
      * the page ID from the value source specification object. Request parameters appeneded to
      * the page ID will also be extracted for translation.
      *
      * @param spec   value source specification object
      * @throws ValueSourceInitializeException
      */
     public void initialize(ValueSourceSpecification spec) throws ValueSourceInitializeException
     {
         super.initialize(spec);
         String valueKey = "";
         String srcParams = spec.getParams();
         int delimPos = srcParams.indexOf(TREE_SOURCE_SEPARATOR);
 
         if(delimPos > 0)
         {
             int queryStrPos = srcParams.indexOf(QUERY_STRING_SEPARATOR);
             if (queryStrPos > 0 && delimPos > queryStrPos)
             {
                 // if the ',' comes after the '?' then it is probably a part of the query string and not the tree delimiter
                 valueKey = srcParams;
             }
             else
             {
                 treeSource = srcParams.substring(0, delimPos);
                 valueKey = srcParams.substring(delimPos+1);
             }
         }
         else
         {
             valueKey = srcParams;
         }
         int endOfIdDelimPos = valueKey.indexOf(QUERY_STRING_SEPARATOR);
         // the rest of the string might contain other information aside from the navigation page ID (such as request parameters)
         if (endOfIdDelimPos < 0)
         {
             // no url query string attached
             pageId = valueKey;
             reqParamsSource = null;
             return;
         }
         else
         {
             pageId = valueKey.substring(0, endOfIdDelimPos);
             reqParamsSource = new ValueSourceSpecification(valueKey.substring(endOfIdDelimPos + 1));
             reqParams = new ValueSrcExpressionValueSource();
             reqParams.initialize(reqParamsSource);
         }
     }
 }
