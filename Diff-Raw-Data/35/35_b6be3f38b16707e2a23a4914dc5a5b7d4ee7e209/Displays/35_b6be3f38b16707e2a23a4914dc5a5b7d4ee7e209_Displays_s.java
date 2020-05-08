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
 package org.testatoo.core.matcher;
 
 import org.hamcrest.Description;
 import org.hamcrest.TypeSafeMatcher;
 import org.testatoo.core.component.Component;
 import org.testatoo.core.nature.Container;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author dev@testatoo.org
  */
 public class Displays extends TypeSafeMatcher<Container> {
 
     private Component[] components;
 
     private List<Component> notExistComponents = new ArrayList<Component>();
     private List<Component> notVisibleComponents = new ArrayList<Component>();
 
     private Displays(Component... components) {
         this.components = components;
     }
 
     @Override
     protected boolean matchesSafely(Container container) {

         for (Component component : components) {
             if (container.contains(component)) {
                 if (!component.isVisible())
                     notVisibleComponents.add(component);
             } else
                 notExistComponents.add(component);
         }

        if (notExistComponents.isEmpty() && notVisibleComponents.isEmpty())
            return true;
        else
            return false;
     }
 
     @Override
     public void describeTo(Description description) {
         if (!notExistComponents.isEmpty()) {
             description.appendText("contains all of ");
             description.appendValueList("{", ", ", "}", notExistComponents);
         }
         if (!notExistComponents.isEmpty() && !notVisibleComponents.isEmpty()) {
             description.appendText(" and ");
         }
         if (!notVisibleComponents.isEmpty()) {
             description.appendText("all of this must be visible ");
             description.appendValueList("{", ", ", "}", notVisibleComponents);
         }
     }
 
     public static Displays displays(Component... components) {
         return new Displays(components);
     }
 }
