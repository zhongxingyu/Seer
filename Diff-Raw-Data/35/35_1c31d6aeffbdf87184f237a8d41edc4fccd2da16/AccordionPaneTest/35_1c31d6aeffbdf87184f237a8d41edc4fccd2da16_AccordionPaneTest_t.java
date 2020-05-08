 /* 
  * This file is part of the Echo2 Extras Project.
  * Copyright (C) 2005-2006 NextApp, Inc.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package nextapp.echo2.extras.testapp.testscreen;
 
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.ContentPane;
 import nextapp.echo2.app.Label;
 import nextapp.echo2.app.SplitPane;
 import nextapp.echo2.app.WindowPane;
 import nextapp.echo2.app.event.ActionEvent;
 import nextapp.echo2.app.event.ActionListener;
 import nextapp.echo2.app.layout.SplitPaneLayoutData;
 import nextapp.echo2.extras.app.CalendarSelect;
 import nextapp.echo2.extras.app.AccordionPane;
 import nextapp.echo2.extras.app.layout.AccordionPaneLayoutData;
 import nextapp.echo2.extras.testapp.AbstractTest;
 import nextapp.echo2.extras.testapp.ButtonColumn;
 import nextapp.echo2.extras.testapp.StyleUtil;
 import nextapp.echo2.extras.testapp.TestControlsPane;
 
 /**
  * Interactive test module for <code>AccordionPane</code>s.
  */
 public class AccordionPaneTest extends AbstractTest {
         
     private int tabNumber;
 
     public AccordionPaneTest() {
         super("AccordionPane");
         
         final AccordionPane accordionPane = new AccordionPane();
         add(accordionPane);
         setTestComponent(this, accordionPane);
         
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add Label (No LayoutData)", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 accordionPane.add(new Label("Generic Label"));
             }
         });
 
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add Label", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Label label = new Label("Tab Pane Child " + tabNumber);
                 label.setBackground(StyleUtil.randomBrightColor());
                 AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                 layoutData.setTitle("Label #" + tabNumber);
                 label.setLayoutData(layoutData);
                 accordionPane.add(label);
                 ++tabNumber;
             }
         });
 
        testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add and Select Label", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Label label = new Label("Tab Pane Child " + tabNumber);
                label.setBackground(StyleUtil.randomBrightColor());
                AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                layoutData.setTitle("Label #" + tabNumber);
                label.setLayoutData(layoutData);
                accordionPane.add(label);
                ++tabNumber;
                accordionPane.setActiveTabIndex(accordionPane.visibleIndexOf(label));
            }
        });

         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add Label (index 0)", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Label label = new Label("Tab Pane Child " + tabNumber);
                 label.setBackground(StyleUtil.randomBrightColor());
                 AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                 layoutData.setTitle("Inserted Label #" + tabNumber);
                 label.setLayoutData(layoutData);
                 accordionPane.add(label, 0);
                 ++tabNumber;
             }
         });
         
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add Label (index 2)", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (accordionPane.getComponentCount() < 2) {
                     // Do nothing
                     return;
                 }
                 Label label = new Label("Tab Pane Child " + tabNumber);
                 label.setBackground(StyleUtil.randomBrightColor());
                 AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                 layoutData.setTitle("Inserted Label #" + tabNumber);
                 label.setLayoutData(layoutData);
                 accordionPane.add(label, 2);
                 ++tabNumber;
             }
         });
         
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add CalendarSelect", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 CalendarSelect calendarSelect = new CalendarSelect();
                 AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                 layoutData.setTitle("Calendar #" + tabNumber++);
                 calendarSelect.setLayoutData(layoutData);
                 accordionPane.add(calendarSelect);
             }
         });
 
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add AccordionPaneTest", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 AccordionPaneTest accordionPaneTest = new AccordionPaneTest();
                 AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                 layoutData.setTitle("APT #" + tabNumber++);
                 accordionPaneTest.setLayoutData(layoutData);
                 accordionPane.add(accordionPaneTest);
             }
         });
 
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add ContentPane", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 final ContentPane contentPane = new ContentPane();
                 ButtonColumn buttonColumn = new ButtonColumn();
                 buttonColumn.addButton("Add WindowPane", new ActionListener(){
                 
                     public void actionPerformed(ActionEvent e) {
                         WindowPane windowPane = new WindowPane();
                         windowPane.setStyleName("Default");
                         windowPane.setTitle("Test Window");
                         windowPane.add(new Label(StyleUtil.QUASI_LATIN_TEXT_1));
                         contentPane.add(windowPane);
                     }
                 });
                 buttonColumn.addButton("Add AccordionPaneTest WindowPane", new ActionListener(){
                 
                     public void actionPerformed(ActionEvent e) {
                         WindowPane windowPane = new WindowPane();
                         windowPane.setStyleName("Default");
                         windowPane.setTitle("Test Window");
                         windowPane.add(new AccordionPaneTest());
                         contentPane.add(windowPane);
                     }
                 });
                 contentPane.add(buttonColumn);
                 AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                 layoutData.setTitle("ContentPane #" + tabNumber++);
                 contentPane.setLayoutData(layoutData);
                 accordionPane.add(contentPane);
             }
         });
 
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Add SplitPane", new ActionListener(){
         
             public void actionPerformed(ActionEvent arg0) {
                 SplitPane splitPane = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT);
                 splitPane.setResizable(true);
                 AccordionPaneLayoutData layoutData = new AccordionPaneLayoutData();
                 layoutData.setTitle("SplitPane #" + tabNumber++);
                 splitPane.setLayoutData(layoutData);
                 accordionPane.add(splitPane);
                 
                 SplitPaneLayoutData splitPaneLayoutData;
                 
                 Label left = new Label("Left");
                 splitPaneLayoutData = new SplitPaneLayoutData();
                 splitPaneLayoutData.setBackground(new Color(0xafafff));
                 left.setLayoutData(splitPaneLayoutData);
                 splitPane.add(left);
                 
                 Label right = new Label("Right");
                 splitPaneLayoutData = new SplitPaneLayoutData();
                 splitPaneLayoutData.setBackground(new Color(0xafffaf));
                 right.setLayoutData(splitPaneLayoutData);
                 splitPane.add(right);
             }
         });
 
         testControlsPane.addButton(TestControlsPane.CATEGORY_CONTENT, "Remove Last Tab", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (accordionPane.getComponentCount() > 0) {
                     accordionPane.remove(accordionPane.getComponentCount() - 1);
                 }
             }
         });
  
         // General Properties
         
         addColorPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "foreground");
         addColorPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "background");
         addInsetsPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "defaultContentInsets");
 
         addColorPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "tabForeground");
         addColorPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "tabBackground");
         addBorderPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "tabBorder");
         
         addBooleanPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "tabRolloverEnabled");
         addColorPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "tabRolloverForeground");
         addColorPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "tabRolloverBackground");
         addBorderPropertyTests(TestControlsPane.CATEGORY_PROPERTIES, "tabRolloverBorder");
         
         // Selection Properties
 
         testControlsPane.addButton(TestControlsPane.CATEGORY_SELECTION, "Select TabIndex 0", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 accordionPane.setActiveTabIndex(0);
             }
         });
 
         testControlsPane.addButton(TestControlsPane.CATEGORY_SELECTION, "Select TabIndex 2", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (accordionPane.getComponentCount() > 0) {
                     accordionPane.setActiveTabIndex(2);
                 }
             }
         });
 
         // Integration Tests
         
         addStandardIntegrationTests();
     }
 }
