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
 
 package specs.keyboard;
 
 import org.junit.runner.RunWith;
 import org.testatoo.Module;
 import org.testatoo.cartridge.html4.element.InputText;
 import org.testatoo.config.annotation.TestatooModules;
 import org.testatoo.config.junit.TestatooWithConcordionJunitRunner;
 
 import static org.hamcrest.Matchers.is;
 import static org.testatoo.core.ComponentFactory.*;
 import static org.testatoo.core.Language.*;
 
 @RunWith(TestatooWithConcordionJunitRunner.class)
 @TestatooModules(Module.class)
 public class ConcordionKeyboardTest {
 
     public String typeAndCheck(String input, String value) {
         page().open("InputText.html");
        InputText inputLanguage = component(InputText.class, input);
        assertThat(inputLanguage.value(), is("french"));
        type(value, into(inputLanguage));
        return inputLanguage.value();
     }
}
