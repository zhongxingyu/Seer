 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_swing.dialogs.actioneditor;
 
 import com.dmdirc.actions.ActionCondition;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionComparison;
 import com.dmdirc.actions.CoreActionComponent;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.harness.ui.ClassAndNameComponentMatcher;
 import com.dmdirc.harness.ui.DMDircUITestCase;
 import javax.swing.JComboBox;
 import org.junit.Test;
 import org.uispec4j.ComboBox;
 import org.uispec4j.Panel;
 import org.uispec4j.TextBox;
 
 public class ActionConditionEditorPanelTest extends DMDircUITestCase {
 
     static {
         ActionManager.getActionManager().initialise();
     }
 
     @Test
     public void testNullInitilisation() {
         ActionConditionEditorPanel panel = new ActionConditionEditorPanel(
                 new ActionCondition(-1, null, null, ""), null);
         Panel test = new Panel(panel);
         assertFalse(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument")).isEnabled());
         assertFalse(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component")).isEnabled());
         assertFalse(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison")).isEnabled());
         assertFalse(test.getInputTextBox().isEnabled());
     }
 
     @Test
     public void testEmptyInitilisation() {
         ActionConditionEditorPanel panel = new ActionConditionEditorPanel(
                 new ActionCondition(-1, null, null, ""),
                 CoreActionType.CHANNEL_MESSAGE);
         Panel test = new Panel(panel);
         assertTrue(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument")).isEnabled());
         assertFalse(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component")).isEnabled());
         assertFalse(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison")).isEnabled());
         assertFalse(test.getInputTextBox().isEnabled());
     }
 
     @Test
     public void testInitilisation() {
         ActionConditionEditorPanel panel = new ActionConditionEditorPanel(
                 new ActionCondition(0, CoreActionComponent.CHANNEL_NAME,
                 CoreActionComparison.STRING_STARTSWITH, "abc"),
                 CoreActionType.CHANNEL_MESSAGE);
         Panel test = new Panel(panel);
         assertTrue(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument")).isEnabled());
         assertTrue(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component")).isEnabled());
         assertTrue(test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison")).isEnabled());
         assertTrue(test.getInputTextBox().isEnabled());
     }
 
     @Test
     public void testHandleArgument() {
         ActionConditionEditorPanel panel = new ActionConditionEditorPanel(
                 new ActionCondition(-1, null, null, ""),
                 CoreActionType.CHANNEL_MESSAGE);
         Panel test = new Panel(panel);
 
         ComboBox argument = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument"));
         ComboBox component = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component"));
         ComboBox comparison = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison"));
         TextBox target = test.getInputTextBox();
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals(null));
         assertFalse(component.isEnabled());
         assertTrue(component.selectionEquals(null));
         assertFalse(comparison.isEnabled());
         assertTrue(comparison.selectionEquals(null));
         assertFalse(target.isEnabled());
         assertTrue(target.textIsEmpty());
 
         panel = new ActionConditionEditorPanel(
                 new ActionCondition(0, CoreActionComponent.CHANNEL_NAME,
                 CoreActionComparison.STRING_STARTSWITH, "abc"),
                 CoreActionType.CHANNEL_MESSAGE);
         test = new Panel(panel);
 
         argument = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument"));
         component = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component"));
         comparison = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison"));
         target = test.getInputTextBox();
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("channel"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals("name"));
         assertTrue(comparison.isEnabled());
         assertTrue(comparison.selectionEquals("starts with"));
         assertTrue(target.isEnabled());
         assertTrue(target.textEquals("abc"));
 
         argument.select("user");
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("user"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals(null));
         assertFalse(comparison.isEnabled());
         assertTrue(comparison.selectionEquals(null));
         assertFalse(target.isEnabled());
         assertTrue(target.textIsEmpty());
     }
 
     @Test
     public void testHandleComponent() {
         ActionConditionEditorPanel panel = new ActionConditionEditorPanel(
                 new ActionCondition(0, null, null, ""),
                 CoreActionType.CHANNEL_MESSAGE);
         Panel test = new Panel(panel);
 
         ComboBox argument = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument"));
         ComboBox component = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component"));
         ComboBox comparison = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison"));
         TextBox target = test.getInputTextBox();
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("channel"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals(null));
         assertFalse(comparison.isEnabled());
         assertTrue(comparison.selectionEquals(null));
         assertFalse(target.isEnabled());
         assertTrue(target.textIsEmpty());
 
         panel = new ActionConditionEditorPanel(
                 new ActionCondition(0, CoreActionComponent.CHANNEL_NAME,
                 CoreActionComparison.STRING_STARTSWITH, "abc"),
                 CoreActionType.CHANNEL_MESSAGE);
         test = new Panel(panel);
 
         argument = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument"));
         component = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component"));
         comparison = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison"));
         target = test.getInputTextBox();
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("channel"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals("name"));
         assertTrue(comparison.isEnabled());
         assertTrue(comparison.selectionEquals("starts with"));
         assertTrue(target.isEnabled());
         assertTrue(target.textEquals("abc"));
 
         component.select("notification colour");
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("channel"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals("notification colour"));
         assertTrue(comparison.isEnabled());
         assertTrue(comparison.selectionEquals(null));
         assertFalse(target.isEnabled());
         assertTrue(target.textIsEmpty());
     }
 
     @Test
     public void testHandleComparison() {
         ActionConditionEditorPanel panel = new ActionConditionEditorPanel(
                 new ActionCondition(0, CoreActionComponent.CHANNEL_NAME, null,
                 ""),
                 CoreActionType.CHANNEL_MESSAGE);
         Panel test = new Panel(panel);
 
         ComboBox argument = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument"));
         ComboBox component = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component"));
         ComboBox comparison = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison"));
         TextBox target = test.getInputTextBox();
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("channel"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals("name"));
         assertTrue(comparison.isEnabled());
         assertTrue(comparison.selectionEquals(null));
         assertFalse(target.isEnabled());
         assertTrue(target.textIsEmpty());
 
         panel = new ActionConditionEditorPanel(
                 new ActionCondition(0, CoreActionComponent.CHANNEL_NAME,
                 CoreActionComparison.STRING_STARTSWITH, "abc"),
                 CoreActionType.CHANNEL_MESSAGE);
         test = new Panel(panel);
 
         argument = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "argument"));
         component = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "component"));
         comparison = test.getComboBox(new ClassAndNameComponentMatcher(
                 JComboBox.class, "comparison"));
         target = test.getInputTextBox();
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("channel"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals("name"));
         assertTrue(comparison.isEnabled());
         assertTrue(comparison.selectionEquals("starts with"));
         assertTrue(target.isEnabled());
         assertTrue(target.textEquals("abc"));
 
         comparison.select("equals");
 
         assertTrue(argument.isEnabled());
         assertTrue(argument.selectionEquals("channel"));
         assertTrue(component.isEnabled());
         assertTrue(component.selectionEquals("name"));
         assertTrue(comparison.isEnabled());
         assertTrue(comparison.selectionEquals("equals"));
         assertTrue(target.isEnabled());
         assertTrue(target.textEquals("abc"));
     }
 }
