 /*
  * Copyright 2010 Raffael Herzog
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.raffael.util.i18n.impl;
 
 import java.util.Locale;
 
 import org.testng.annotations.*;
 
 import ch.raffael.util.i18n.Forward;
 import ch.raffael.util.i18n.I18N;
 import ch.raffael.util.i18n.I18NException;
 import ch.raffael.util.i18n.ResourceBundle;
 import ch.raffael.util.i18n.Selector;
 
 import static org.testng.Assert.*;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public class TestBundle {
 
     private TestRes res;
     
     @BeforeClass
     public void load() {
         res = I18N.getBundle(TestRes.class);
     }
 
     @Test
     public void testMine() throws Exception {
         assertEquals(res.mine(), "This is my own thing");
     }
 
     @Test
     public void testInherited() throws Exception {
         assertEquals(res.inherited(), "Inherited from ResA", "inherited");
         assertEquals(res.inherited(), I18N.getBundle(ResA.class).inherited(), "inherited with call");
         assertEquals(res.alsoInherited(), "Inherited from ResB", "alsoInherited");
     }
 
     @Test
     public void testAmbiguous() throws Exception {
         assertEquals(res.ambiguousWithForward(), "Ambiguous with forward from ResB", "with forward");
         assertEquals(res.ambiguousWithImpl(), "My own implementation", "with implementation");
     }
 
     @Test(expectedExceptions = I18NException.class)
     public void testAmbiguousError() throws Exception {
         res.ambiguousWithError();
     }
 
     @Test
     public void testForward() throws Exception {
         assertEquals(res.forwarded(), "Forwarded to ResC");
     }
 
     @Test
     public void testParameter_de_CH() throws Exception {
        I18N.setLocale(new Locale("de_CH"));
         assertEquals(res.parameter("Raffi"), "Raffi ist cool!");
     }
 
     @Test
     public void testParameter_en() throws Exception {
         I18N.setLocale(new Locale("en"));
         assertEquals(res.parameter("Raffi"), "Raffi is cool!");
     }
 
     @Test
     public void testSelector() throws Exception {
         assertEquals(res.selector(FooBar.FOO), "Selected Foo");
         assertEquals(res.selector(FooBar.BAR), "Selected Bar");
     }
 
     public static interface ResA extends ResourceBundle {
 
         String inherited();
 
         String ambiguousWithForward();
         String ambiguousWithImpl();
         String ambiguousWithError();
     }
 
     public static interface ResB extends ResourceBundle {
 
         String alsoInherited();
 
         String ambiguousWithForward();
         String ambiguousWithImpl();
         String ambiguousWithError();
 
     }
 
     public static interface ResC extends ResourceBundle {
         String forwarded();
     }
 
     public static interface TestRes extends ResA, ResB {
         @Forward(ResB.class)
         String ambiguousWithForward();
 
         String mine();
         String parameter(String name);
         String selector(@Selector FooBar sel);
 
         @Forward(ResC.class)
         String forwarded();
     }
 
     public static enum FooBar {
         FOO, BAR
     }
 
 }
