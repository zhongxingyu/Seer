 /*
  *  Java HTML Tidy - JTidy
  *  HTML parser and pretty printer
  *
  *  Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
  *  Institute of Technology, Institut National de Recherche en
  *  Informatique et en Automatique, Keio University). All Rights
  *  Reserved.
  *
  *  Contributing Author(s):
  *
  *     Dave Raggett <dsr@w3.org>
  *     Andy Quick <ac.quick@sympatico.ca> (translation to Java)
  *     Gary L Peskin <garyp@firstech.com> (Java development)
  *     Sami Lempinen <sami@lempinen.net> (release management)
  *     Fabrizio Giustina <fgiust at users.sourceforge.net>
  *     Vlad Skarzhevskyy <vlads at users.sourceforge.net> (JTidy servlet  development)
  *
  *  The contributing author(s) would like to thank all those who
  *  helped with testing, bug fixes, and patience.  This wouldn't
  *  have been possible without all of you.
  *
  *  COPYRIGHT NOTICE:
  *
  *  This software and documentation is provided "as is," and
  *  the copyright holders and contributing author(s) make no
  *  representations or warranties, express or implied, including
  *  but not limited to, warranties of merchantability or fitness
  *  for any particular purpose or that the use of the software or
  *  documentation will not infringe any third party patents,
  *  copyrights, trademarks or other rights.
  *
  *  The copyright holders and contributing author(s) will not be
  *  liable for any direct, indirect, special or consequential damages
  *  arising out of any use of the software or documentation, even if
  *  advised of the possibility of such damage.
  *
  *  Permission is hereby granted to use, copy, modify, and distribute
  *  this source code, or portions hereof, documentation and executables,
  *  for any purpose, without fee, subject to the following restrictions:
  *
  *  1. The origin of this source code must not be misrepresented.
  *  2. Altered versions must be plainly marked as such and must
  *     not be misrepresented as being the original source.
  *  3. This Copyright notice may not be removed or altered from any
  *     source or altered source distribution.
  *
  *  The copyright holders and contributing author(s) specifically
  *  permit, without fee, and encourage the use of this source code
  *  as a component for supporting the Hypertext Markup Language in
  *  commercial products. If you use this source code in a product,
  *  acknowledgment is not required but would be appreciated.
  *
  */
 package org.w3c.tidy.servlet;
 
 import java.util.Hashtable;
 
 import com.meterware.httpunit.WebImage;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebResponse;
 import com.meterware.httpunit.WebTable;
 
 /*
  * Created on 25.10.2004 by vlads
  */
 /**
  * 
  * @author Vlad Skarzhevskyy <a href="mailto:skarzhevskyy@gmail.com">skarzhevskyy@gmail.com</a>
  * @version $Revision$ ($Author$)
  */
 public class SessionRepositoryTest extends TidyServletCase
 {
     /**
      * Instantiates a new test case.
      * @param name test name
      */
     public SessionRepositoryTest(String name)
     {
         super(name);
     }
 
     public void setServletInitParameters(Hashtable initParameters) 
     {
         initParameters.put("properties.filename", "JTidyServletProduction.properties");
     }
     
     public void testResponseAndReport() throws Exception
     {
        WebResponse response =  getJSPResponse("servlet/formatedByTagOK.jsp");
 
         WebImage[] img = response.getImages();
         assertEquals("Expected 1 image in result.", 1, img.length);
 
         String requestID = response.getNewCookieValue(Consts.ATTRIBUTE_REQUEST_ID);
         
         WebLink servletLink = response.getLinkWithName("JTidyValidationImageLink");
         
         WebResponse reportResponse =  getResponse(servletLink.getURLString());
         
         validateReport(reportResponse);
         
         WebResponse response2 =  getResponse();
 
         WebTable[] tables = response2.getTables();
         assertEquals("Expected 1 table in result.", 1, tables.length);
         assertEquals("RepositoryInSession", tables[0].getSummary());
 
     }
 }
