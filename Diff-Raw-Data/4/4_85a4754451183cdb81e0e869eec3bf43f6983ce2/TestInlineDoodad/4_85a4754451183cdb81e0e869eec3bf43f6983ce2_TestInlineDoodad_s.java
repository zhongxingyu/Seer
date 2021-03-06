 /**
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.waveprotocol.wave.client.editor.testing;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.SpanElement;
 
 import org.waveprotocol.wave.client.editor.ElementHandlerRegistry;
 import org.waveprotocol.wave.client.editor.RenderingMutationHandler;
 import org.waveprotocol.wave.client.editor.extract.PasteFormatRenderers;
 
 /**
  * Inline doodad for testing.
  *
  */
 public class TestInlineDoodad {
   private static class TestRenderer extends RenderingMutationHandler {
     @Override
     public Element createDomImpl(Renderable element) {
       SpanElement domElement = Document.get().createSpanElement();
       return element.setAutoAppendContainer(domElement);
     }
   }
 
  private static final String NS = "w";
  private static final String TAGNAME = "span";
  public static final String FULL_TAGNAME = NS + ":" + TAGNAME;
 
   public static void register(ElementHandlerRegistry handlerRegistry) {
     register(handlerRegistry, FULL_TAGNAME);
   }
 
   public static void register(ElementHandlerRegistry handlerRegistry, String tagName) {
     RenderingMutationHandler renderingMutationHandler = new TestRenderer();
     handlerRegistry.registerRenderingMutationHandler(tagName, renderingMutationHandler);
     handlerRegistry.registerNiceHtmlRenderer(tagName, PasteFormatRenderers.SHALLOW_CLONE_RENDERER);
   }
 }
