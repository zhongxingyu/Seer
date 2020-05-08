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
 /*
  * Created on 26.10.2004 by vlads
  */
 import java.util.Hashtable;
 
 import com.meterware.httpunit.WebImage;
 import com.meterware.httpunit.WebResponse;
 /**
  * 
  * @author Vlad Skarzhevskyy <a href="mailto:skarzhevskyy@gmail.com">skarzhevskyy@gmail.com</a>
  * @version $Revision$ ($Author$)
  */
 public class ValidationImageRedefineTest extends TidyServletCase
 {
 
     public ValidationImageRedefineTest(String name)
     {
         super(name);
     }
     
     public void setServletInitParameters(Hashtable initParameters) 
     {
         initParameters.put("properties.filename", "JTidyServletTest.properties");
     }
     
     /**
      * Check that image source is set properly in Servlet.
      * do not call the redirector
      * @param jspName jsp name, with full path
      * @throws Exception any axception thrown during test.
      */
     public void testSmallImage() throws Exception
     {
        WebResponse response =  getJSPResponse("servlet/FormatedByTagOK.jsp");
 
         WebImage[] img = response.getImages();
 
         assertEquals("Expected 1 images in result.", 1, img.length);
 
         assertEquals("Expected my name Page Validation", "Page Validation", img[0].getAltText());
         
         String src = img[0].getSource(); 
     	
         WebResponse responseImgSrc = getResponse( src + "&srcOnly=true");
     	
     	assertFalse("Image src", responseImgSrc.getText().equalsIgnoreCase(Consts.DEFAULT_IMAGE_NAME_PREFIX + "ok.gif"));
     	assertTrue("Image ok.gif", responseImgSrc.getText().endsWith("ok.gif"));
 
     	WebResponse responseImg = getResponse(src);
 
     	assertEquals("Image type", "image/gif", responseImg.getContentType());
     	
     	int len = responseImg.getContentLength() ;
     	
     	assertTrue("Small Image", (len < 1000)); 
     }
 }
