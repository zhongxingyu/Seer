 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.ftest.richPanelMenu;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import org.jboss.test.selenium.css.CssProperty;
 import org.richfaces.PanelMenuMode;
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.RegressionTest;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestPanelMenuSimple extends AbstractPanelMenuTest {
 
     @Inject
     @Use(empty = true)
     Boolean expandSingle;
 
     @Test
     @RegressionTest("https://issues.jboss.org/browse/RF-10158")
     public void testDisabled() {
         attributes.setGroupMode(PanelMenuMode.client);
         attributes.setDisabled(false);
 
         assertEquals(selenium.getCount(menu.getAnyDisabledGroup()), 2);
         assertEquals(selenium.getCount(menu.getAnyDisabledItem()), 3);
 
         attributes.setDisabled(true);
 
         assertEquals(selenium.getCount(menu.getAnyDisabledGroup()), 6);
         assertEquals(selenium.getCount(menu.getAnyDisabledItem()), 24);
     }
 
     @Test
     @Use(field = "expandSingle", booleans = { true, false })
     public void testExpandSingle() {
         attributes.setExpandSingle(expandSingle);
 
         group2.toggle();
         assertEquals(getExpandedGroupsCount(), expanded(1));
 
         group1.toggle();
         assertEquals(getExpandedGroupsCount(), expanded(2));
     }
 
     @Test
     public void testGroupClass() {
         attributes.setGroupMode(PanelMenuMode.client);
         super.testStyleClass(group24, "groupClass");
     }
 
     @Test
     public void testGroupDisabledClass() {
         attributes.setGroupMode(PanelMenuMode.client);
         super.testStyleClass(group26, "groupDisabledClass");
     }
 
     @Test
     public void testItemClass() {
         attributes.setGroupMode(PanelMenuMode.client);
         super.testStyleClass(item22, "itemClass");
     }
 
     @Test
     public void testItemDisabledClass() {
         attributes.setGroupMode(PanelMenuMode.client);
         super.testStyleClass(item25, "itemDisabledClass");
     }
 
     @Test
     public void testRendered() {
         attributes.setRendered(false);
         assertFalse(selenium.isElementPresent(menu));
         attributes.setRendered(true);
         assertTrue(selenium.isElementPresent(menu));
     }
 
     @Test
     public void testStyle() {
         super.testStyle(menu, "style");
     }
 
     @Test
     public void testStyleClass() {
         super.testStyleClass(menu, "styleClass");
     }
 
     @Test
     public void testTopGroupClass() {
         super.testStyleClass(group1, "topGroupClass");
     }
 
     @Test
     public void testTopItemClass() {
         super.testStyleClass(item3, "topItemClass");
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-10302")
     public void testWidth() {
         attributes.setWidth("300px");
         assertEquals(selenium.getStyle(menu, CssProperty.WIDTH), "300px");
     }
 
     private int getExpandedGroupsCount() {
         return selenium.getCount(menu.getAnyExpandedGroup());
     }
 
     private int expanded(int expanded) {
         return (expandSingle && expanded >= 1) ? 1 : expanded;
     }
 }
