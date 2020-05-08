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
 
 package org.testatoo.core.component;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.testatoo.core.ComponentException;
 import org.testatoo.core.Evaluator;
 import org.testatoo.core.nature.Container;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.any;
 import static org.mockito.Mockito.*;
 import static org.testatoo.core.ComponentType.Window;
 
 /**
  * @author dev@testatoo.org
  */
 public class ComponentTest {
 
     private Evaluator evaluator;
     private String id = "myId";
 
     @Before
     public void setUp() {
         evaluator = mock(Evaluator.class);
         when(evaluator.existComponent(id)).thenReturn(true);
     }
 
     @Test
     public void can_obtain_the_id() {
         Component component = new Component(evaluator, id);
         assertThat(component.id(), is(id));
     }
 
     @Test
     public void equality_is_on_the_id() {
         when(evaluator.existComponent("myId2")).thenReturn(true);
 
         Component component_1 = new Component(evaluator, id);
         Component component_2 = new Component(evaluator, "myId2");
         Component component_3 = new Component(evaluator, id);
 
         assertThat(component_1, is(not(equalTo(component_2))));
         assertThat(component_1, is(equalTo(component_3)));
     }
 
     @Test
     public void component_visibility() {
         when(evaluator.isVisible(any(Component.class))).thenReturn(true, false);
 
         Component visibleComponent = new Component(evaluator, id);
         Component invisibleComponent = new Component(evaluator, id);
 
         assertThat(visibleComponent.isVisible(), is(true));
         assertThat(invisibleComponent.isVisible(), is(false));
 
         verify(evaluator, times(2)).isVisible(any(Component.class));
 
     }
 
     @Test
    public void component_is_enabled() {
         when(evaluator.isEnabled(any(Component.class))).thenReturn(true, true, false, false);
 
         Component enabledComponent = new Component(evaluator, id);
         Component disabledComponent = new Component(evaluator, id);
 
         assertThat(enabledComponent.isEnabled(), is(true));
         assertThat(enabledComponent.isDisabled(), is(false));
 
         assertThat(disabledComponent.isDisabled(), is(true));
         assertThat(disabledComponent.isEnabled(), is(false));
 
         verify(evaluator, times(4)).isEnabled(any(Component.class));
     }
 
     @Test
     public void test_component_have_the_focus() {
         Component component = new Component(evaluator, id);
 
         when(evaluator.hasFocus(component)).thenReturn(false);
 
         assertThat(component.hasFocus(), is(false));
         verify(evaluator, times(1)).hasFocus(component);
     }
 
     @Test
     public void test_component_can_contain_other_component() {
         when(evaluator.componentType(id)).thenReturn(Window);
 
         Window container = new Window(evaluator, id);
         Component contained_component = new Component(evaluator, id);
         Component not_contained_component = new Component(evaluator, id);
 
         when(evaluator.contains(container, contained_component)).thenReturn(true);
         assertThat(container.contains(contained_component), is(true));
 
         when(evaluator.contains(container, not_contained_component)).thenReturn(false);
         assertThat(container.contains(not_contained_component), is(false));
 
         verify(evaluator, times(2)).contains(any(Container.class), any(Component.class));
     }
 
     @Test
    public void test_component_constructor() {
         //  An evaluator must be defined
         try {
             new Component(null, null);
             fail();
         } catch (ComponentException e) {
             assertThat(e.getMessage().contains("An evaluator must be defined"), is(true));
         }
 
         // The component must be available
         Evaluator evaluator = mock(Evaluator.class);
         String existingIdentifier = "id1";
         String noneExistingIdentifier = "id2";
 
         when(evaluator.existComponent(existingIdentifier)).thenReturn(true);
         when(evaluator.existComponent(noneExistingIdentifier)).thenReturn(false);
 
         new Component(evaluator, existingIdentifier);
         try {
             new Component(evaluator, noneExistingIdentifier);
             fail();
         } catch (ComponentException e) {
             assertThat(e.getMessage(), is("Cannot find component defined by id=" + noneExistingIdentifier));
         }
 
         verify(evaluator, times(1)).existComponent(existingIdentifier);
         verify(evaluator, times(1)).existComponent(noneExistingIdentifier);
     }
 
     @Test
     public void test_toString() {
         when(evaluator.isEnabled(any(Component.class))).thenReturn(true);
         when(evaluator.isVisible(any(Component.class))).thenReturn(false);
 
         Component component = new Component(evaluator, id);
         assertThat(component.toString(), is("class org.testatoo.core.component.Component with state : enabled:true, visible:false"));
     }
 }
