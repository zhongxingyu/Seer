 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.framework.Test;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portletcontainer.descriptor.*;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.DescriptorException;
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.ConfigParam;
 import org.exolab.castor.types.AnyNode;
 
 import java.util.*;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.File;
 
 /**
  * This is the base fixture for service testing. Provides a service factory and the
  * properties file.
  */
 public class PortletDescriptorTest extends TestCase {
 
 
     public PortletDescriptorTest(String name) {
         super(name);
     }
 
     public static void main (String[] args) throws Exception{
         junit.textui.TestRunner.run(suite());
     }
 
     public static Test suite ( ) {
         return new TestSuite(PortletDescriptorTest.class);
     }
 
     public void testDescriptor() {
         PortletDeploymentDescriptor pdd = null;
         String portletFile = "webapps/gridsphere/WEB-INF/conf/test/portlet-test.xml";
         String mappingFile = "webapps/gridsphere/WEB-INF/conf/mapping/portlet-mapping.xml";
         try {
             pdd = new PortletDeploymentDescriptor(portletFile, mappingFile);
         } catch (IOException e) {
             fail("IO error unmarshalling " + portletFile + " using " + mappingFile + " : " + e.getMessage());
         } catch (DescriptorException e) {
             fail("Unable to unmarshall " + portletFile + " using " + mappingFile + " : " + e.getMessage());
         }
        Vector defs = pdd.getPortletDef();
 
         // assertEquals(expected, actual)
 
         // we have one app descriptions
         assertEquals(1, defs.size());
 
         PortletDefinition def = (PortletDefinition)defs.get(0);
         PortletApp portletApp = def.getPortletApp();
        Vector concreteApps = def.getConcreteApps();
 
         // we have two concrete portlet apps
         assertEquals(concreteApps.size(), 2);
         ConcretePortletApplication concreteOne = (ConcretePortletApplication)concreteApps.get(0);
         ConcretePortletApplication concreteTwo = (ConcretePortletApplication)concreteApps.get(1);
         assertEquals("org.gridlab.gridsphere.portlets.core.HelloWorld.666", portletApp.getID());
         assertEquals("Hello World", portletApp.getPortletName());
         assertEquals("hello", portletApp.getServletName());
         CacheInfo c = portletApp.getCacheInfo();
         assertEquals(120, c.getExpires());
         assertEquals("true", c.getShared());
         AllowsWindowStates winstatelist = portletApp.getAllowsWindowStates();
         List winstates = winstatelist.getWindowStatesAsStrings();
         assertEquals(2, winstates.size());
         assertEquals("maximized", (String)winstates.get(0));
         assertEquals("minimized", (String)winstates.get(1));
 
         SupportsModes smodes = portletApp.getSupportsModes();
         List mlist = smodes.getMarkupList();
         assertEquals(2, mlist.size());
         Markup m = (Markup)mlist.get(0);
         assertEquals("html", m.getName());
         List modes = m.getPortletModes();
         assertEquals(4, modes.size());
         AnyNode mod = (AnyNode)modes.get(0);
         assertEquals("view", mod.getLocalName());
 
         m = (Markup)mlist.get(1);
         assertEquals("wml", m.getName());
 
 
         // Check concrete one portal data
         assertEquals("org.gridlab.gridsphere.portlets.core.HelloWorld.666.2", concreteOne.getID());
 
         List contextList = concreteOne.getContextParamList();
         assertEquals(contextList.size(), 2);
         ConfigParam one = (ConfigParam)contextList.get(0);
         ConfigParam two = (ConfigParam)contextList.get(1);
 
         assertEquals("buzzle", one.getParamName());
         assertEquals("yea", one.getParamValue());
 
         assertEquals("beezle", two.getParamName());
         assertEquals("yo", two.getParamValue());
 
         ConcretePortletInfo onePI = concreteOne.getConcretePortletInfo();
         assertEquals("Hello World 1", onePI.getName());
         assertEquals("en", onePI.getDefaultLocale());
 
         List langList = onePI.getLanguageList();
         assertEquals(langList.size(), 2);
         LanguageInfo langOne = (LanguageInfo)langList.get(0);
         LanguageInfo langTwo = (LanguageInfo)langList.get(1);
 
         assertEquals("Here is a simple portlet", langOne.getDescription());
         assertEquals("portlet hello world", langOne.getKeywords());
         assertEquals("en_US", langOne.getLocale());
         assertEquals("Hello World - Sample Portlet #1", langOne.getTitle());
         assertEquals("Hello World", langOne.getTitleShort());
 
         assertEquals("Hier ist ein gleicht portlet", langTwo.getDescription());
         assertEquals("portlet hallo welt", langTwo.getKeywords());
         assertEquals("en_DE", langTwo.getLocale());
         assertEquals("Hallo Welt - Sample Portlet #1", langTwo.getTitle());
         assertEquals("Hallo Welt", langTwo.getTitleShort());
 
         Owner o = onePI.getOwner();
         assertEquals(o.getRoleName(), "SUPER");
 
         List groups = onePI.getGroupList();
         assertEquals(groups.size(), 1);
 
         Group g = (Group)groups.get(0);
         assertEquals("ANY", g.getGroupName());
 
         List roles = onePI.getRoleList();
         assertEquals(groups.size(), 1);
         Role r = (Role)roles.get(0);
         assertEquals("GUEST", r.getRoleName());
 
         List configList = onePI.getConfigParamList();
         assertEquals(configList.size(), 1);
         one = (ConfigParam)configList.get(0);
         assertEquals("Portlet Mistress", one.getParamName());
         assertEquals("mistress@domain.com", one.getParamValue());
 
 
         // Check concrete two portal data
         assertEquals(concreteTwo.getID(), "org.gridlab.gridsphere.portlets.core.HelloWorld.666.4");
 
         configList = concreteTwo.getContextParamList();
         assertEquals(configList.size(), 1);
         one = (ConfigParam)configList.get(0);
 
         assertEquals(one.getParamName(), "Portlet Master");
         assertEquals(one.getParamValue(), "secondguy@some.com");
 
         onePI = concreteTwo.getConcretePortletInfo();
         assertEquals(onePI.getName(), "Hello World 2");
         assertEquals(onePI.getDefaultLocale(), "en");
 
         langList = onePI.getLanguageList();
         assertEquals(langList.size(), 1);
         langOne = (LanguageInfo)langList.get(0);
 
         assertEquals(langOne.getDescription(), "Here is another simple portlet");
         assertEquals(langOne.getKeywords(), "portlet hello world");
         assertEquals(langOne.getLocale(), "en_US");
         assertEquals(langOne.getTitle(), "Hello World - Sample Portlet #2");
         assertEquals(langOne.getTitleShort(), "Hello World");
 
         Owner ow = onePI.getOwner();
         assertEquals(ow.getRoleName(), "ADMIN");
         assertEquals(ow.getGroupName(), "CACTUS");
 
         List groupsList = onePI.getGroupList();
         assertEquals(groups.size(), 1);
 
         Group gr = (Group)groupsList.get(0);
         assertEquals("CACTUS", gr.getGroupName());
 
         List rolez = onePI.getRoleList();
         assertEquals(groups.size(), 1);
         Role rol = (Role)rolez.get(0);
         assertEquals("USER", rol.getRoleName());
 
         configList = onePI.getConfigParamList();
         assertEquals(configList.size(), 1);
         one = (ConfigParam)configList.get(0);
         assertEquals("Portlet Master", one.getParamName());
         assertEquals("secondguy@some.com", one.getParamValue());
 
         // demonstrate saving
         /*
         Hashtable store = new Hashtable();
         store.put("beezle", "yo");
         store.put("buzzle", "yea");
         Enumeration enum = store.keys();
         Vector list = new Vector();
         while (enum.hasMoreElements()) {
             String k = (String)enum.nextElement();
             System.err.println(k);
             String value = (String)store.get(k);
             ConfigParam parms = new ConfigParam(k, value);
             list.add(parms);
         }
 
         concreteOne.setID("whose your daddy?");
         concreteOne.setContextParamList(list);
         portletApp.setPortletName("yo dude");
         pdd.setPortletAppDescriptor(portletApp);
         pdd.setConcretePortletApplication(concreteOne);
         try {
             pdd.save("/tmp/portlet.xml", "webapps/gridsphere/WEB-INF/conf/mapping/portlet-mapping.xml");
         } catch (Exception e) {
             System.err.println("Unable to save SportletApplicationSettings: " + e.getMessage());
         }
         */
 
     }
 
 
 }
