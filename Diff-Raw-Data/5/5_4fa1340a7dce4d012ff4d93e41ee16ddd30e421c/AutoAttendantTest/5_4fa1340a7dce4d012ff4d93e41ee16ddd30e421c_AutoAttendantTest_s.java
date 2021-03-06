 /*
  * 
  * 
  * Copyright (C) 2004 SIPfoundry Inc.
  * Licensed by SIPfoundry under the LGPL license.
  * 
  * Copyright (C) 2004 Pingtel Corp.
  * Licensed to SIPfoundry under a Contributor Agreement.
  * 
  * $
  */
 package org.sipfoundry.sipxconfig.admin.dialplan;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import org.custommonkey.xmlunit.XMLAssert;
 import org.custommonkey.xmlunit.XMLTestCase;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.sipfoundry.sipxconfig.TestHelper;
 import org.sipfoundry.sipxconfig.XmlUnitHelper;
 import org.sipfoundry.sipxconfig.common.DialPad;
 
 public class AutoAttendantTest extends XMLTestCase {
 
     private VxmlGenerator m_vxml;
 
     protected void setUp() throws Exception {
         super.setUp();
         XMLUnit.setIgnoreWhitespace(true);
 
         m_vxml = new VxmlGenerator();
         String etc = TestHelper.getSysDirProperties().getProperty("sysdir.etc");
         m_vxml.setScriptsDirectory(etc);
         m_vxml.setVelocityEngine(TestHelper.getVelocityEngine());
     }
 
     public void testGetSystemName() {
         AutoAttendant aa = new AutoAttendant();
         assertEquals("xcf-1", aa.getSystemName());
 
         AutoAttendant operator = AutoAttendant.createOperator();
         assertEquals("operator", operator.getSystemName());
     }
 
    public void testActivateDefaultAttendant() throws Exception {
         AutoAttendant aa = new AutoAttendant();
         aa.initialize();
         aa.setSettings(TestHelper.loadSettings("sipxvxml/autoattendant.xml"));
         aa.setPrompt("prompt.wav");        
         aa.addMenuItem(DialPad.NUM_0, new AttendantMenuItem(AttendantMenuAction.OPERATOR));
         aa.addMenuItem(DialPad.NUM_1, new AttendantMenuItem(AttendantMenuAction.DISCONNECT));
         aa.setSettingValue("onfail/transfer", "1");
         aa.setSettingValue("onfail/transfer-extension", "999");
         
         StringWriter actualXml = new StringWriter();
         m_vxml.generate(aa, actualXml);
         assertVxmlEquals("expected-autoattendant.xml", actualXml.toString());
     }
 
     private void assertVxmlEquals(String expectedFile, String actualXml) throws Exception {
         Reader actualRdr = new StringReader(actualXml);
         StringWriter actual = new StringWriter();
         XmlUnitHelper.style(getReader("autoattendant.xsl"), actualRdr, actual, null);
 
         Reader expectedRdr = getReader(expectedFile);
         StringWriter expected = new StringWriter();
         XmlUnitHelper.style(getReader("autoattendant.xsl"), expectedRdr, expected, null);
 
         System.out.println(actual.toString());
         XMLAssert.assertXMLEqual(expected.toString(), actual.toString());
     }
 
     private Reader getReader(String resource) {
         InputStream stream = getClass().getResourceAsStream(resource);
         return new InputStreamReader(stream);
     }
 }
