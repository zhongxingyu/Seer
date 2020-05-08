 /**
  * License Agreement.
  *
  * JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */ 
 package org.richfaces.testng;
 
 import org.ajax4jsf.template.Template;
 import org.richfaces.testng.util.CommonUtils;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class InplaceSelectTest extends InplacesTest {
 	
 	private final String INPLACE_SELECT_PAGE = "inplaceSelectTest.xhtml";
 	
 	private final String INPLACE_SELECT_RESET_METHOD = "#{inplaceSelectBean.reset}";
 	
     @Test
     public void testInplaceSelectComponent(Template template) {
     	
     	setTestUrl(INPLACE_SELECT_PAGE);
     	
         renderPage(template, INPLACE_SELECT_RESET_METHOD);
     
         String parentId = getParentId() + "_form:";
         String inplaceSelectId = parentId + "inplaceSelect";
         String okButton = parentId + "inplaceSelect" + "ok";
         String cancelButton = parentId + "inplaceSelect" + "cancel";
         String popupId = inplaceSelectId + "list";
 
         writeStatus("click the component");
 
         clickById(inplaceSelectId);
         Assert.assertFalse(isVisible(popupId), "Component pop-up should not show up on solitary click");
 
         writeStatus("double click the component");
 
         selenium.doubleClick(inplaceSelectId);
         Assert.assertTrue(isVisible(popupId), "Component pop-up should show up on double click");
 
         writeStatus("Check that controls buttons are present");
 
         Assert.assertTrue(isPresent(okButton));
         Assert.assertTrue(isPresent(cancelButton));
 
         writeStatus("Select second element"); //Birch
 
         selenium.mouseMove("xpath=//div[@id='" + inplaceSelectId + "list" + "']/span[2]");
         selenium.mouseDown(okButton);
 
         writeStatus("Check that a new element is selected");
 
         AssertValueEquals(inplaceSelectId + "inplaceValue", "Birch");
 
         writeStatus("Select another element"); // Aspen
 
         selenium.doubleClick(inplaceSelectId);
 
         selenium.mouseMove("xpath=//div[@id='" + inplaceSelectId + "list']/span[3]");
         selenium.mouseDown(cancelButton);
 
         writeStatus("Cancel selected value.");
         AssertValueEquals(inplaceSelectId + "inplaceValue", "Birch", "A value has not to be changed");
 
         writeStatus("Verify javascript event triggering");
 
         writeStatus("Double click the component");
         selenium.doubleClick(inplaceSelectId);
 
         assertEvent("oneditactivated");
 
         writeStatus("Stop editing with ok");
         selenium.mouseDown(okButton);
         assertEvent("onviewactivated");
            
     }
     
     @Test
     public void testJSApi(Template template) {
     	setTestUrl(JS_API_PAGE);
     	init(template);
     	
     	String iid = inplaceId + JS_API_ID_PREFIX;
     	Assert.assertTrue("test".equals(getValue(iid)));
                
         setValue(iid, "999");
         Assert.assertTrue("999".equals(getValue(iid)));
         
         setValue(iid, "Aspen");
     	Assert.assertTrue("Aspen".equals(getValue(iid)), CommonUtils.getFailedTestMessage(iid));
     	
     	edit(iid);
     	    	
     	selenium.click("xpath=//div[@id='" + iid + "list']/span[4]");
     	cancel(iid);
     	Assert.assertTrue("Aspen".equals(getValue(iid)), CommonUtils.getFailedTestMessage(iid));
     }
    
     @Override
 	public void setTestUrl(String testUrl) {
 		this.testUrl = "pages/inplaceSelect/" + testUrl;
 	}
     
     @Override
     protected void renderPage(Template template) {
        	renderPage(template, INPLACE_SELECT_RESET_METHOD);
     }
 	
 	public void initIds() {
 		super.initIds();
		inplaceValuePrefix = "inplaceValue";
 		inplaceId = formId + "ii";
		iTempValuePx = "inplaceTmpValue";
 	}
 
 }
