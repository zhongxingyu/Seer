 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;
 
 import com.dmdirc.actions.Action;
 import com.dmdirc.actions.ActionGroup;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.actions.interfaces.ActionType;
 import com.dmdirc.harness.ui.WindowButtonHandler;
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.junit.Before;
 import org.junit.Test;
 import org.uispec4j.Panel;
 import org.uispec4j.Table;
 import org.uispec4j.UISpecTestCase;
 import org.uispec4j.interception.WindowInterceptor;
 import static org.mockito.Mockito.*;
 
 public class ActionsGroupPanelTest extends UISpecTestCase {
 
     private Action action1, action2, action3, action4;
     private ActionGroup group;
     private Panel panel;
     private Table table;
     
     @Before
     @Override
     public void setUp() {
         action1 = mock(Action.class);
         when(action1.getName()).thenReturn("name 1");
         when(action1.getTriggers()).thenReturn(new ActionType[] {
             CoreActionType.ACTION_CREATED, CoreActionType.ACTION_DELETED,
         });
         when(action1.getResponse()).thenReturn(new String[] { "A" });
                 
         action2 = mock(Action.class);
         when(action2.getName()).thenReturn("name 2");
         when(action2.getTriggers()).thenReturn(new ActionType[] {
             CoreActionType.ACTION_CREATED
         });
         when(action2.getResponse()).thenReturn(new String[0]);
         
         action3 = mock(Action.class);
         when(action3.getName()).thenReturn("name 3");
         when(action3.getTriggers()).thenReturn(new ActionType[] {
             CoreActionType.ACTION_CREATED, CoreActionType.ACTION_DELETED,
         });
         when(action3.getResponse()).thenReturn(new String[] {
             "Response line 1",
             "Response line 2",
             "Response line 3",
         });
         
         action4 = mock(Action.class);
         when(action4.getName()).thenReturn("name 4");
         when(action4.getTriggers()).thenReturn(new ActionType[] {
             CoreActionType.ACTION_CREATED
         });
         when(action4.getResponse()).thenReturn(new String[] {
             "Response line 1",
             "Response line 2",
             "Response line 4",
         });
 
         group = mock(ActionGroup.class);
         when(group.getActions()).thenReturn(new ArrayList<Action>(Arrays.asList(new Action[]{
             action1, action2, action3, action4,
         })));
 
         panel = new Panel(new ActionsGroupPanel(null, group));
         table = panel.getTable();
     }
 
     @Test
     public void testTable() {
         assertTrue(table.getHeader().contentEquals("Name", "Trigger", "Response"));
 
         assertTrue(table.contentEquals(new String[][]{
             {"name 1", "Action created", "A"},
             {"name 2", "Action created", ""},
             {"name 3", "Action created", "Response line 1, Response line 2, Response line 3"},
             {"name 4", "Action created", "Response line 1, Response line 2, Response line 4"},
         }));
     }
 
     @Test
     public void testTableSorting() {
         assertTrue(table.getHeader().contentEquals("Name", "Trigger", "Response"));
 
         table.getHeader().click("Name");
 
         assertTrue(table.contentEquals(new String[][]{
             {"name 4", "Action created", "Response line 1, Response line 2, Response line 4"},
             {"name 3", "Action created", "Response line 1, Response line 2, Response line 3"},
             {"name 2", "Action created", ""},
             {"name 1", "Action created", "A"},
         }));
 
         table.getHeader().click("Response");
 
         assertTrue(table.contentEquals(new String[][]{
             {"name 2", "Action created", ""},
             {"name 1", "Action created", "A"},
             {"name 3", "Action created", "Response line 1, Response line 2, Response line 3"},
             {"name 4", "Action created", "Response line 1, Response line 2, Response line 4"},
         }));
     }
 
     @Test
     public void testDeletingCancel() {
         table.selectRow(1);
 
         WindowInterceptor.init(panel.getButton("Delete").triggerClick())
                .process(new WindowButtonHandler("Confirm deletion", "No")).run();
 
         assertTrue(table.contentEquals(new String[][]{
             {"name 1", "Action created", "A"},
             {"name 2", "Action created", ""},
             {"name 3", "Action created", "Response line 1, Response line 2, Response line 3"},
             {"name 4", "Action created", "Response line 1, Response line 2, Response line 4"},
         }));
         verify(group, never()).deleteAction((Action) anyObject());
     }
 
     public void testDeletingOk() {
         table.selectRow(1);
 
         WindowInterceptor.init(panel.getButton("Delete").triggerClick())
                .process(new WindowButtonHandler("Confirm deletion", "Yes")).run();
 
         verify(group).deleteAction(same(action2));
     }
 
     public void testDeletingSorted() {
         table.getHeader().click("Name");
 
         table.selectRow(1);
 
         WindowInterceptor.init(panel.getButton("Delete").triggerClick())
                .process(new WindowButtonHandler("Confirm deletion", "Yes")).run();
 
         verify(group).deleteAction(same(action3));
     }
 
     public void testTableDeleting() {
         assertTrue(table.contentEquals(new String[][]{
             {"name 1", "Action created", "A"},
             {"name 2", "Action created", ""},
             {"name 3", "Action created", "Response line 1, Response line 2, Response line 3"},
             {"name 4", "Action created", "Response line 1, Response line 2, Response line 4"},
         }));
 
         when(group.getActions()).thenReturn(new ArrayList<Action>(Arrays.asList(new Action[]{
             action1, action3, action4,
         })));
 
         ((ActionsGroupPanel) panel.getAwtComponent()).actionDeleted("name 2");
         ((ActionsGroupPanel) panel.getAwtComponent()).actionDeleted("name 7");
 
         assertTrue(table.contentEquals(new String[][]{
             {"name 1", "Action created", "A"},
             {"name 3", "Action created", "Response line 1, Response line 2, Response line 3"},
             {"name 4", "Action created", "Response line 1, Response line 2, Response line 4"},
         }));
     }
 
 }
