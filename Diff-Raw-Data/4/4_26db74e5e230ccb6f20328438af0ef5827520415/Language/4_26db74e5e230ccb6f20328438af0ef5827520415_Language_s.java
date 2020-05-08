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
 
 package org.testatoo.core;
 
 import org.hamcrest.Matcher;
 import org.testatoo.core.component.AbstractTextField;
 import org.testatoo.core.component.CheckBox;
 import org.testatoo.core.component.Component;
 import org.testatoo.core.component.Window;
 import org.testatoo.core.component.datagrid.Cell;
 import org.testatoo.core.component.datagrid.Column;
 import org.testatoo.core.component.datagrid.Row;
 import org.testatoo.core.input.Keyboard;
 import org.testatoo.core.input.Mouse;
 import org.testatoo.core.nature.Checkable;
 
 import java.util.concurrent.TimeUnit;
 
 /**
  * This is the abstract base class corresponding to Testatoo Domain Specific Langage.
  * This DSL is based on test langage (like "assertThat") improved with articles, adverbs, etc...
  * It provides also action verbs (like "click on") to simulate actions on graphic objects
  *
  * @author dev@testatoo.org
  */
 public abstract class Language {
 
     private static ThreadLocal<Object> it = new ThreadLocal<Object>();
 
     @SuppressWarnings({"unchecked"})
     public static <T> T it() {
         return (T) it.get();
     }
 
     /**
      * To check an assertion on a graphic object
      *
      * @param <T>     type of the graphic object
      * @param object  the graphic object
      * @param matcher the matcher to calculate the assertion
      */
     public static <T> void assertThat(T object, Matcher<T> matcher) {
         set(object);
         org.hamcrest.MatcherAssert.assertThat(object, matcher);
     }
 
     /**
      * To avoid repeating the "assertThat" when calculating another assertion on the same object
      *
      * @param <T>     type of the graphic object
      * @param matcher the matcher to calculate the assertion
      */
     @SuppressWarnings({"unchecked"})
     public static <T> void and(Matcher<T> matcher) {
         org.hamcrest.MatcherAssert.assertThat((T) Language.it.get(), matcher);
     }
 
     /**
      * To avoid repeating the "assertThat" when calculating another assertion on the same object
      *
      * @param object  dummy object for language support (use it() in place)
      * @param matcher the matcher to calculate the assertion
      */
     @SuppressWarnings({"unchecked"})
     public static <T> void and(Object object, Matcher<T> matcher) {
         and(matcher);
     }
 
     /**
      * To allow more readable tests
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return a testatoo component
      */
     public static <T> T on(T component) {
         return into(component);
     }
 
     /**
      * To allow more readable tests
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return a testatoo component
      */
     public static <T> T into(T component) {
         EvaluatorHolder.get().focusOn((Component) component);
         return component;
     }
 
     /**
      * Reset the field and call the method type
      *
      * @param <T>     type of the textField
      * @param value   value to be entered
      * @param element the textField
      * @return the textField with value in it
      */
     public static <T extends AbstractTextField> T enter(String value, T element) {
         EvaluatorHolder.get().reset(element);
         return type(value, element);
     }
 
     /**
      * To simulate the enter of a value in a textField
      *
      * @param <T>     type of the textField
      * @param value   value to be entered
      * @param element the textField
      * @return the textField with value in it
      */
     public static <T extends AbstractTextField> T type(String value, T element) {
         EvaluatorHolder.get().focusOn(element);
         Keyboard.type(value);
         return element;
     }
 
    public static String eval(String expression) {
        return EvaluatorHolder.get().evaluate(expression);
    }

     /**
      * To simulate a click on a component
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return the component after click
      */
     public static <T extends Component> T clickOn(T component) {
         Mouse.clickOn(component);
         return component;
     }
 
     /**
      * To simulate a double-click on a component
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return the component after double-click
      */
     public static <T extends Component> T doubleClickOn(T component) {
         Mouse.doubleClickOn(component);
         return component;
     }
 
     /**
      * To simulate a mouse movement over a component
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return the component after mouse-over
      */
     public static <T extends Component> T dragMouseOver(T component) {
         Mouse.mouseOverOn(component);
         return component;
     }
 
     /**
      * To simulate a mouse movement out of a component
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return the component after mouse-out
      */
     public static <T extends Component> T dragMouseOut(T component) {
         Mouse.mouseOutOf(component);
         return component;
     }
 
     /**
      * To simulate a check of the component
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return the checked component
      */
     public static <T extends Checkable> T check(T component) {
         component.check();
         return component;
     }
 
     /**
      * To simulate a uncheck of the component
      *
      * @param <T>       type of the component
      * @param component testatoo component
      * @return the unchecked component
      */
     public static <T extends CheckBox> T unCheck(T component) {
         component.unCheck();
         return component;
     }
 
     /**
      * To simulate the closing of a window
      *
      * @param window the window to close
      */
     public static void close(Window window) {
         window.close();
     }
 
     /**
      * Waiting until an assertion is reached. The timeout is 1 second
      *
      * @param <T>     type of the graphic object
      * @param object  the graphic object
      * @param matcher the matcher to calculate the assertion
      * @throws InterruptedException exception
      */
     public static <T> void waitUntil(T object, org.hamcrest.Matcher<T> matcher) throws InterruptedException {
         waitUntil(object, matcher, max(1, TimeUnit.SECONDS));
     }
 
     /**
      * Waiting until an assertion is reached.
      *
      * @param <T>      type of the graphic object
      * @param object   the graphic object
      * @param matcher  the matcher to calculate the assertion
      * @param duration maximum waiting time
      * @throws InterruptedException exception
      */
     public static <T> void waitUntil(T object, org.hamcrest.Matcher<T> matcher, Duration duration) throws InterruptedException {
         waitUntil(object, matcher, duration, freq(500, TimeUnit.MILLISECONDS));
     }
 
     /**
      * Waiting until an assertion is reached.
      *
      * @param <T>       type of the graphic object
      * @param object    the graphic object
      * @param matcher   the matcher to calculate the assertion
      * @param duration  maximum waiting time
      * @param frequency frequency of retries
      * @throws InterruptedException exception
      */
     public static <T> void waitUntil(T object, org.hamcrest.Matcher<T> matcher, Duration duration, Duration frequency) throws InterruptedException {
         final long step = frequency.unit.toMillis(frequency.duration);
         Throwable ex = null;
         for (long timeout = duration.unit.toMillis(duration.duration); timeout > 0; timeout -= step, Thread.sleep(step)) {
             try {
                 assertThat(object, matcher);
                 return;
             } catch (Throwable e) {
                 ex = e;
             }
         }
         throw new RuntimeException("Unable to reach the condition in " + duration.duration + " " + duration.unit, ex);
     }
 
     public static Duration max(long duration, TimeUnit unit) {
         return new Duration(duration, unit);
     }
 
     public static Duration freq(long duration, TimeUnit unit) {
         return new Duration(duration, unit);
     }
 
     /**
      * Placeholder for language
      *
      * @return empty Columns array
      */
     public static Column[] columns() {
         return new Column[0];
     }
 
     /**
      * Placeholder for language
      *
      * @return empty Rows array
      */
     public static Row[] rows() {
         return new Row[0];
     }
 
     /**
      * Placeholder for language
      *
      * @return empty Cells array
      */
     public static Cell[] cells() {
         return new Cell[0];
     }
 
 
     private static <T> T set(T it) {
         Language.it.set(it);
         return it;
     }
 }
