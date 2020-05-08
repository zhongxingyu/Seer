 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: XACMLAuthzDecisionQueryTest.java,v 1.2 2007-04-20 22:38:31 dillidorai Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.xacml2.saml2;
 
import com.sun.identity.saml2.common.SAML2Exception;

 import java.io.FileInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 import com.sun.identity.shared.test.UnitTestBase;
 import com.sun.identity.xacml2.context.ContextFactory;
 import com.sun.identity.xacml2.common.XACML2Exception;
 import javax.xml.parsers.ParserConfigurationException;
 import com.sun.identity.shared.xml.XMLUtils;
 import org.xml.sax.SAXException;
 import java.net.URI;
 
 /**
  * Unit Test Cases to test
  * <code>com.sun.identity.xacml2.saml2.XACMLAuthzDecisionQuery</code> class.
  */
 
 public class XACMLAuthzDecisionQueryTest extends UnitTestBase {
     
     public XACMLAuthzDecisionQueryTest() {
         super("FedLibrary-XACML2");
     }
     
     /**
      * Validates the <code>XACMLAuthzDecisionQuery</code> object.
      *
      * @param xmlFile the file containing the XACMLAuthzDecisionQuery XML.
      * @throws XACML2Exception if there is creating the <code>XACMLAuthzDecisionQuery</code>
      *         object or the XML String does not conform to the XML Schema
      * @throws ParserConfigurationException if there is an error parsing the
      *         Request XML string.
      * @throws IOException if there is an error reading the file.
      * @throws SAXException if there is an error during XML parsing.
      */
     @Parameters({"query-filename"})
     @Test(groups = {"xacml2"})
     public void validateXACMLAuthzDecisionQuery(String xmlFile) throws XACML2Exception,
             ParserConfigurationException, IOException,
            SAXException, SAML2Exception {
         entering("validateXACMLAuthzDecisionQuery",null);
         try {
             log(Level.INFO, "validateXACMLAuthzDecisionQuery",xmlFile);
             FileInputStream fis = new FileInputStream(new File(xmlFile));
             Document doc = XMLUtils.toDOMDocument(fis, null);
             Element elt = doc.getDocumentElement();
             XACMLAuthzDecisionQuery query =
                     ContextFactory.getInstance().createXACMLAuthzDecisionQuery(elt);
             
             // object to xml string
             String xmlString = query.toXMLString(true,true);
             System.out.println(" XACMLAuthzDecisionQuery xmlString:"+ xmlString);
             assert (xmlString != null) :
                 "Error creating XML String from XACMLAuthzDecisionQuery object";
             log(Level.INFO, "validateXACMLAuthzDecisionQuery",xmlString);
             query.setInputContextOnly(true);
             query.setReturnContext(true);
             // object to xml string
             xmlString = query.toXMLString(true,true);
             // create query again from string
             System.out.println("xmlString:"+ xmlString);
             query = ContextFactory.getInstance().createXACMLAuthzDecisionQuery(xmlString);
             System.out.println("query.request:"+query.getRequest().toXMLString());
             System.out.println("query.returnContext:"+query.getReturnContext());
             System.out.println("query.inputContextOnly:"+query.getInputContextOnly());
             log(Level.INFO, "createXACMLAuthzDecisionQuery",xmlString);
         } finally {
             exiting("createXACMLAuthzDecisionQuery");
         }
     }
 }
