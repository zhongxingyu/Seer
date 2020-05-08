 /*
  * Copyright 2004-2011 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.render.html;
 
 import javax.faces.render.Renderer;
 import javax.faces.render.RendererTest;
 
 import org.seasar.teeda.extension.component.html.THtmlScript;
 
 /**
  * @author higa
  */
 public class THtmlScriptRendererTest extends RendererTest {
 
     private THtmlScriptRenderer renderer;
 
     private THtmlScript script;
 
     protected Renderer createRenderer() {
         THtmlScriptRenderer renderer = new THtmlScriptRenderer();
         renderer.setComponentIdLookupStrategy(getComponentIdLookupStrategy());
         return renderer;
     }
 
     public void setUp() throws Exception {
         super.setUp();
         renderer = (THtmlScriptRenderer) createRenderer();
         script = new THtmlScript();
     }
 
     public void testEncode_simple() throws Exception {
         encodeByRenderer(renderer, script);
         assertEquals(
                 "<script type=\"text/javascript\" language=\"JavaScript\"></script>",
                 getResponseText());
     }
 
     public void testEncode_src() throws Exception {
         script.setSrc("hoge.js");
         encodeByRenderer(renderer, script);
         assertEquals(
                 "<script type=\"text/javascript\" language=\"JavaScript\" src=\"hoge.js\"></script>",
                 getResponseText());
     }
 
     public void testEncode_src2() throws Exception {
         script.setSrc("/teedaExtension/hoge.js");
         encodeByRenderer(renderer, script);
         assertEquals(
                 "<script type=\"text/javascript\" language=\"JavaScript\" src=\"/mock-context/teedaExtension/hoge.js\"></script>",
                 getResponseText());
     }
 }
