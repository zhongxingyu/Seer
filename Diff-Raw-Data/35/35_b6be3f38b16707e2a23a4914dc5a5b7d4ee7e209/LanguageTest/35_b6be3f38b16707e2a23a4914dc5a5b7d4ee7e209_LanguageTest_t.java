 /**
  * Copyright (C) 2008 Ovea <dev@testatoo.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.testatoo.core.language;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.testatoo.core.Evaluator;
 import org.testatoo.core.EvaluatorHolder;
 import org.testatoo.core.ListSelection;
 import org.testatoo.core.Selection;
 import org.testatoo.core.component.*;
 import org.testatoo.core.component.datagrid.DataGrid;
 import org.testatoo.core.component.datagrid.Row;
 import org.testatoo.core.input.*;
 
 import java.util.concurrent.TimeUnit;
 
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.*;
 import static org.testatoo.core.ComponentType.*;
 import static org.testatoo.core.Language.*;
 import static org.testatoo.core.matcher.Matchers.*;
 
 /**
  * @author dev@testatoo.org
  */
 public class LanguageTest {
 
     private Evaluator evaluator;
     private String id = "myId";
 
     @Before
     public void setUp() {
         evaluator = mock(Evaluator.class);
         when(evaluator.name()).thenReturn(Evaluator.DEFAULT_NAME);
 
         // Needed for Mouse ;)
         EvaluatorHolder.register(evaluator);
         when(evaluator.existComponent(id)).thenReturn(true);
         when(evaluator.isVisible(any(Component.class))).thenReturn(true);
         when(evaluator.isEnabled(any(Component.class))).thenReturn(true);
     }
 
     @After
     public void clean() {
         EvaluatorHolder.unregister(Evaluator.DEFAULT_NAME);
     }
 
     @Test
     public void checkbox_usage_through_language() {
         when(evaluator.componentType(id)).thenReturn(CheckBox);
 
         CheckBox checkBox = new CheckBox(evaluator, id);
         when(evaluator.isChecked(checkBox)).thenReturn(false, true, false);
         when(evaluator.label(checkBox)).thenReturn("myLabel");
 
         assertThat(checkBox, is(not(checked())));
 
         check(checkBox);
         assertThat(checkBox, is(checked()));
 
         unCheck(checkBox);
         assertThat(checkBox, is(not(checked())));
         assertThat(checkBox, has(label("myLabel")));
 
         verify(evaluator, times(1)).check(checkBox);
         verify(evaluator, times(1)).unCheck(checkBox);
         verify(evaluator, times(3)).isChecked(checkBox);
     }
 
     @Test
     public void radio_usage_through_language() {
         when(evaluator.componentType(id)).thenReturn(Radio);
 
         Radio radio = new Radio(evaluator, id);
         when(evaluator.label(radio)).thenReturn("myLabel");
         when(evaluator.isChecked(radio)).thenReturn(false, true);
 
         assertThat(radio, is(not(checked())));
 
         check(radio);
         assertThat(radio, is(checked()));
 
         and(it(), has(label("myLabel")));
 
         assertThat(radio, has(label("myLabel")));
 
         verify(evaluator, times(1)).check(radio);
         verify(evaluator, times(2)).isChecked(radio);
     }
 
     @Test
     public void textfield_usage_through_language_part1() {
         when(evaluator.componentType(id)).thenReturn(TextField);
 
         TextField textField = new TextField(evaluator, id);
         when(evaluator.label(textField)).thenReturn("myLabel");
         when(evaluator.value(textField)).thenReturn("myValue");
 
         assertThat(textField, has(label("myLabel")));
         assertThat(textField, has(value("myValue")));
 
         clickOn(textField);
         Keyboard.type("SomeData");
 
         verify(evaluator, times(1)).click(textField, Click.left);
         verify(evaluator, times(1)).type("SomeData");
     }
 
     @Test
     public void textfield_usage_through_language_part2() {
         when(evaluator.componentType(id)).thenReturn(TextField);
 
         TextField textField = new TextField(evaluator, id);
         when(evaluator.maxLength(textField)).thenReturn(255);
         when(evaluator.label(textField)).thenReturn("myLabel");
         when(evaluator.value(textField)).thenReturn("myValue");
 
         assertThat(textField, is(enabled()));
         and(not(disabled()));
         and(it(), visible());
 
         clickOn(textField);
         Mouse.clickOn(textField);
 
         type("data_1", into(textField));
 
         verify(evaluator, times(2)).click(textField, Click.left);
         verify(evaluator, times(2)).focusOn(any(Component.class));
         verify(evaluator, times(1)).type("data_1");
     }
 
     @Test
     public void textfield_usage_through_language_part3() {
         when(evaluator.componentType(id)).thenReturn(TextField);
 
         TextField textField = new TextField(evaluator, id);
         when(evaluator.maxLength(textField)).thenReturn(255);
         when(evaluator.label(textField)).thenReturn("myLabel");
         when(evaluator.value(textField)).thenReturn("");
 
         enter("SomeData", on(textField));
 
         verify(evaluator, atLeastOnce()).focusOn(textField);
         verify(evaluator, times(1)).reset(textField);
         verify(evaluator, times(1)).type("SomeData");
     }
 
     @Test
     public void test_window_usage_through_language() {
         when(evaluator.componentType(id)).thenReturn(Window);
 
         Window window = new Window(evaluator, id);
         when(evaluator.isVisible(any(Component.class))).thenReturn(true);
         when(evaluator.isEnabled(any(Component.class))).thenReturn(true);
         when(evaluator.title(window)).thenReturn("windowTitle");
 
         close(window);
         verify(evaluator, times(1)).close(window);
     }
 
     @Test
     public void test_mouse_usage_on_component() {
         Component component = new Component(evaluator, id);
 
         clickOn(component);
         doubleClickOn(component);
         dragMouseOut(component);
         dragMouseOver(component);
 
         verify(evaluator, times(1)).click(component, Click.left);
         verify(evaluator, times(1)).doubleClick(component);
         verify(evaluator, times(1)).mouseOut(component);
         verify(evaluator, times(1)).mouseOver(component);
     }
 
     @Test
     public void test_keyboard_usage() {
         when(evaluator.componentType(id)).thenReturn(TextField);
 
         TextField textField = new TextField(evaluator, id);
         when(evaluator.maxLength(textField)).thenReturn(255);
         when(evaluator.label(textField)).thenReturn("label");
         when(evaluator.value(textField)).thenReturn("value");
 
         type("Some Data", on(textField));
         Keyboard.type("Other Data");
         Keyboard.keyDown(KeyModifier.SHIFT);
         Keyboard.release(KeyModifier.CONTROL);
         Keyboard.press(Key.F6);
         Keyboard.release();
 
         verify(evaluator, atLeastOnce()).focusOn(textField);
         verify(evaluator, times(1)).type("Some Data");
         verify(evaluator, times(1)).type("Other Data");
         verify(evaluator, times(1)).keyDown(KeyModifier.SHIFT);
         verify(evaluator, times(1)).release(KeyModifier.CONTROL);
         verify(evaluator, times(1)).press(Key.F6);
         verify(evaluator, times(1)).release();
     }
 
     @Test
     public void test_dataGrid_usage() {
         when(evaluator.componentType(id)).thenReturn(DataGrid);
 
         final DataGrid dataGrid = new DataGrid(evaluator, id);
 
         when(evaluator.existComponent("1")).thenReturn(true);
         when(evaluator.componentType("1")).thenReturn(Row);
 
         when(evaluator.existComponent("2")).thenReturn(true);
         when(evaluator.componentType("2")).thenReturn(Row);
 
         when(evaluator.existComponent("3")).thenReturn(true);
         when(evaluator.componentType("3")).thenReturn(Row);
 
         when(evaluator.existComponent("4")).thenReturn(true);
         when(evaluator.componentType("4")).thenReturn(Row);
 
         when(evaluator.existComponent("5")).thenReturn(true);
         when(evaluator.componentType("5")).thenReturn(Row);
 
         when(evaluator.existComponent("6")).thenReturn(true);
         when(evaluator.componentType("6")).thenReturn(Row);
 
         when(evaluator.existComponent("7")).thenReturn(true);
         when(evaluator.componentType("7")).thenReturn(Row);
 
         Row row_1 = new Row(evaluator, "1");
         Row row_2 = new Row(evaluator, "2");
         Row row_3 = new Row(evaluator, "3");
         Row row_4 = new Row(evaluator, "4");
         Row row_5 = new Row(evaluator, "5");
         Row row_6 = new Row(evaluator, "6");
         Row row_7 = new Row(evaluator, "7");
         Selection<Row> rows = ListSelection.of(row_1, row_2, row_3, row_4, row_5, row_6, row_7);
 
         when(evaluator.rows(dataGrid)).thenReturn(rows);
         when(evaluator.cells(any(Row.class))).thenReturn(ListSelection.empty());
 
         assertThat(first(dataGrid.rows()).id(), is(dataGrid.row(1).id()));
         assertThat(last(dataGrid.rows()).id(), is(dataGrid.row(dataGrid.rows().size()).id()));
     }
 
     @Test
     public void test_wait_until_with_success() {
         evaluator = mock(Evaluator.class);
         when(evaluator.existComponent(id)).thenReturn(true);
         when(evaluator.isVisible(any(Component.class))).thenReturn(false, false, false, false, false, false, true);
         when(evaluator.isEnabled(any(Component.class))).thenReturn(true);
 
         Component component = new Component(evaluator, id);
         waitUntil(component, is(visible()), max(2, TimeUnit.SECONDS), freq(500, TimeUnit.MILLISECONDS));
 
         verify(evaluator, times(7)).isVisible(any(Component.class));
     }
 
     @Test
     public void test_wait_until_with_failure() {
         evaluator = mock(Evaluator.class);
 
         when(evaluator.existComponent(id)).thenReturn(true);
         when(evaluator.isVisible(any(Component.class))).thenReturn(false);
         when(evaluator.isEnabled(any(Component.class))).thenReturn(true);
 
         Component component = new Component(evaluator, id);
 
         try {
             waitUntil(component, is(visible()));
             fail();
         } catch (Exception e) {
             assertThat(e.getMessage(), is("Unable to reach the condition in 1 SECONDS"));
         }
     }

    @Test
    public void test_sugar_assertThat() {
        assertThat(true);

        try {
            assertThat(false);
            fail();
        } catch (AssertionError e) {
            assertThat(e.getMessage(), is("Expected true but is false"));
        }
    }
 }
