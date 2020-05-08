 package org.richfaces.tests.metamer.ftest.richPanelMenuItem;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.GuardRequest;
 import org.jboss.test.selenium.request.RequestType;
 import org.richfaces.PanelMenuMode;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.model.PanelMenu;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestPanelMenuItemSimple extends AbstractMetamerTest {
 
     private static String SAMPLE_IMAGE = "/resources/images/loading.gif";
     private static String CHEVRON_DOWN = "chevronDown";
     private static String CHEVRON_DOWN_CLASS = "rf-ico-chevron-down";
 
     PanelMenuItemAttributes attributes = new PanelMenuItemAttributes();
     PanelMenu menu = new PanelMenu(pjq("div.rf-pm[id$=panelMenu]"));
     PanelMenu.Item item = menu.getGroup(1).getItemContains("Item 1.2");
     PanelMenu.Icon leftIcon = item.getLeftIcon();
     PanelMenu.Icon rightIcon = item.getRightIcon();
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richPanelMenuItem/simple.xhtml");
     }
    
     @BeforeMethod
     public void setupMode() {
         attributes.setMode(PanelMenuMode.ajax);
         menu.setItemMode(PanelMenuMode.ajax);
     }
 
     @Test
     public void testData() {
         attributes.setData("RichFaces 4");
         attributes.setOncomplete("data = event.data");
 
         retrieveRequestTime.initializeValue();
         item.select();
         waitGui.waitForChange(retrieveRequestTime);
 
         assertEquals(retrieveWindowData.retrieve(), "RichFaces 4");
     }
 
     @Test
     public void testDisabled() {
         menu.setItemMode(null);
         assertFalse(item.isDisabled());
 
         attributes.setDisabled(true);
 
         assertFalse(item.isSelected());
         assertTrue(item.isDisabled());
 
         new GuardRequest(RequestType.NONE) {
             public void command() {
                 item.select();
             }
         }.waitRequest();
 
         assertFalse(item.isSelected());
         assertTrue(item.isDisabled());
     }
 
     @Test
     public void testDisabledClass() {
         attributes.setDisabled(true);
         super.testStyleClass(item, "disabledClass");
     }
 
     @Test
     public void testLeftDisabledIcon() {
         attributes.setLeftDisabledIcon(SAMPLE_IMAGE);
 
         assertTrue(leftIcon.isTransparent());
         assertFalse(leftIcon.isCustomURL());
 
         attributes.setDisabled(true);
 
         assertTrue(leftIcon.isCustomURL());
         assertTrue(leftIcon.getCustomURL().endsWith(SAMPLE_IMAGE));
     }
 
     @Test
     public void testLeftIcon() {
         attributes.setLeftIcon(CHEVRON_DOWN);
 
         assertTrue(leftIcon.containsClass(CHEVRON_DOWN_CLASS));
 
         attributes.setDisabled(true);
 
         assertTrue(leftIcon.isTransparent());
     }
 
     @Test
     public void testLeftIconClass() {
         super.testStyleClass(leftIcon, "leftIconClass");
     }
 
     @Test
     public void testLimitRender() {
         attributes.setRender("renderChecker");
         attributes.setLimitRender(true);
 
         retrieveRequestTime.initializeValue();
         retrieveRenderChecker.initializeValue();
         item.select();
         waitAjax.waitForChange(retrieveRenderChecker);
         assertFalse(retrieveRequestTime.isValueChanged());
     }
 
     @Test
     public void testRendered() {
         assertTrue(item.isVisible());
 
         attributes.setRendered(false);
 
         assertFalse(item.isVisible());
     }
 
     @Test
     public void testRightDisabledIcon() {
         attributes.setRightDisabledIcon(SAMPLE_IMAGE);
 
         assertTrue(rightIcon.isTransparent());
         assertFalse(rightIcon.isCustomURL());
 
         attributes.setDisabled(true);
 
         assertTrue(rightIcon.isCustomURL());
         assertTrue(rightIcon.getCustomURL().endsWith(SAMPLE_IMAGE));
     }
 
     @Test
     public void testRightIcon() {
         attributes.setRightIcon(CHEVRON_DOWN);
 
         assertTrue(rightIcon.containsClass(CHEVRON_DOWN_CLASS));
 
         attributes.setDisabled(true);
 
         assertTrue(rightIcon.isTransparent());
     }
 
     @Test
     public void testRightIconClass() {
         super.testStyleClass(rightIcon, "rightIconClass");
     }
 
     @Test
     public void testSelectable() {
         menu.setItemMode(null);
         attributes.setSelectable(false);
 
         new GuardRequest(RequestType.NONE) {
             public void command() {
                 item.select();
             }
         }.waitRequest();
 
         assertFalse(item.isSelected());
 
         attributes.setSelectable(true);
 
         new GuardRequest(RequestType.XHR) {
             public void command() {
                 item.select();
             }
         }.waitRequest();
 
         assertTrue(item.isSelected());
     }
 
     @Test
     public void testStatus() {
         attributes.setStatus("statusChecker");
 
         retrieveStatusChecker.initializeValue();
         item.select();
         waitAjax.waitForChange(retrieveStatusChecker);
     }
 
     @Test
     public void testStyle() {
         super.testStyle(item, "style");
     }
 
     @Test
     public void testStyleClass() {
         super.testStyleClass(item, "styleClass");
     }
 }
