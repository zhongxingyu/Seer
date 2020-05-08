 package gov.nih.nci.evs.browser.utils;
 
 import java.util.*;
 import javax.servlet.http.*;
 import org.apache.log4j.*;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  * 
  *          Modification history Initial implementation kim.ong@ngc.com
  * 
  */
 
 public class VisitedConceptUtils {
     private static Logger _logger = Logger.getLogger(VisitedConceptUtils.class);
 
     private static class VisitedConcept {
         public String scheme = "";
         public String version = "";
         public String code = "";
         public String name = "";
         public String value = "";
 
         public VisitedConcept(String scheme, String version, String code,
             String name) {
             this.scheme = scheme;
             this.version = version;
             this.code = code;
             this.name = name;
             this.value = name + " (" + scheme + " " + version + ")";
         }
        
        public String getValue() {
            return value;
        }
 
         public String toString() {
             return value;
         }
 
         public boolean exists(Vector<VisitedConcept> concepts) {
             Iterator<VisitedConcept> iterator = concepts.iterator();
             while (iterator.hasNext()) {
                VisitedConcept visitedConcept = iterator.next();
                if (visitedConcept.getValue().equals(value))
                     return true;
             }
             return false;
         }
     }
 
     public static void add(HttpServletRequest request, String dictionary,
         String version, String code, String name) {
         @SuppressWarnings("unchecked")
         Vector<VisitedConcept> visitedConcepts =
             (Vector<VisitedConcept>) request.getSession().getAttribute(
                 "visitedConcepts");
         if (visitedConcepts == null)
             visitedConcepts = new Vector<VisitedConcept>();
 
         String localCodingSchemeName = DataUtils.getLocalName(dictionary);
         VisitedConcept visitedConcept =
             new VisitedConcept(localCodingSchemeName, version, code, name);
 
         if (visitedConcept.exists(visitedConcepts))
             return;
 
         visitedConcepts.add(visitedConcept);
         _logger.debug("add: " + visitedConcept);
         request.getSession().removeAttribute("visitedConcepts");
         request.getSession().setAttribute("visitedConcepts", visitedConcepts);
     }
 
     private static String getLink(Vector<VisitedConcept> visitedConcepts) {
         StringBuffer strbuf = new StringBuffer();
         String line = "<A href=\"#\" onmouseover=\"Tip('";
         strbuf.append(line);
         strbuf.append("<ul>");
         for (int i = 0; i < visitedConcepts.size(); i++) {
             int j = visitedConcepts.size() - i - 1;
             VisitedConcept visitedConcept = visitedConcepts.elementAt(j);
             String scheme = visitedConcept.scheme;
             String formalName = DataUtils.getFormalName(scheme);
             // String localName = DataUtils.getLocalName(scheme);
             String version = visitedConcept.version;
             String vocabulary_name =
                 DataUtils.getMetadataValue(formalName, version, "display_name");
             String code = visitedConcept.code;
             String name = visitedConcept.name;
             name = DataUtils.htmlEntityEncode(name);
             strbuf.append("<li>");
 
             String versionParameter = "", versionParameterDisplay = "";
             if (version != null && version.length() > 0) {
                 versionParameter = "&version=" + version;
                 versionParameterDisplay = " "
                     + DataUtils.getMetadataValue(formalName, version,
                         "term_browser_version");
             }
 
             line =
                 "<a href=\\'/ncitbrowser/ConceptReport.jsp?dictionary="
                     + formalName + versionParameter + "&code=" + code + "\\'>"
                     + name + " &#40;" + vocabulary_name
                     + versionParameterDisplay + "&#41;" + "</a><br>";
             strbuf.append(line);
             strbuf.append("</li>");
         }
         strbuf.append("</ul>");
         line = "',";
         strbuf.append(line);
 
         line =
             "WIDTH, 300, TITLE, 'Visited Concepts', SHADOW, true, "
                 + "FADEIN, 300, FADEOUT, 300, STICKY, 1, CLOSEBTN, true, "
                 + "CLICKCLOSE, true)\"";
         strbuf.append(line);
 
         line = " onmouseout=UnTip() ";
         strbuf.append(line);
         line = ">Visited Concepts</A>";
         strbuf.append(line);
 
         return strbuf.toString();
     }
 
     public static String getDisplayLink(HttpServletRequest request) {
         @SuppressWarnings("unchecked")
         Vector<VisitedConcept> visitedConcepts =
             (Vector<VisitedConcept>) request.getSession().getAttribute(
                 "visitedConcepts");
         if (visitedConcepts == null || visitedConcepts.size() <= 0)
             return "";
 
         String value = getLink(visitedConcepts);
         return value;
     }
 }
