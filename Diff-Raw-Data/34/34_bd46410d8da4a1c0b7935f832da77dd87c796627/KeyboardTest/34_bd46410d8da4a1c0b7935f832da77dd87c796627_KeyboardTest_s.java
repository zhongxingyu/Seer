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
 
 package org.testatoo.input;
 
 import org.junit.Test;
 import org.testatoo.WebTest;
 import org.testatoo.cartridge.html4.element.Div;
 import org.testatoo.cartridge.html4.element.InputText;
 import org.testatoo.core.input.Keyboard;
 
 import static org.hamcrest.Matchers.*;
 import static org.testatoo.core.ComponentFactory.*;
 import static org.testatoo.core.Language.*;
 import static org.testatoo.core.input.KeyModifier.*;
 
 public class KeyboardTest extends WebTest {
 
     @Test
     public void can_type_data() {
         page().open("InputText.html");
        InputText input_language = component(InputText.class, "input_language");
        assertThat(input_language.value(), is("french"));
 
        type(" AS language", into(input_language));
        assertThat(input_language.value(), containsString("AS language"));
     }
 
     @Test
     public void can_test_key_modifier() {
         page().open("KeyboardTest.html");
 
         Keyboard.keyDown(CONTROL);
         Keyboard.keyDown(SHIFT);
 
         assertThat(component(Div.class, "_Ctrl_Shift_a").classname(), not(containsString("dirty")));
         Keyboard.type("a");
         assertThat(component(Div.class, "_Ctrl_Shift_a").classname(), containsString("dirty"));
 
         Keyboard.release(CONTROL);
 
         assertThat(component(Div.class, "_Shift_a").classname(), not(containsString("dirty")));
         Keyboard.type("a");
         assertThat(component(Div.class, "_Shift_a").classname(), containsString("dirty"));
 
         Keyboard.release(SHIFT);
         Keyboard.release();
 
         assertThat(component(Div.class, "_a").classname(), not(containsString("dirty")));
         Keyboard.type("a");
         assertThat(component(Div.class, "_a").classname(), containsString("dirty"));
     }
 
 }
