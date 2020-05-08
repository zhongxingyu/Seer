 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.core.render.html;
 
 import javax.faces.application.ViewHandler;
 import javax.faces.component.html.HtmlGraphicImage;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.render.Renderer;
 import javax.faces.render.RendererTest;
 
 import org.custommonkey.xmlunit.Diff;
 import org.seasar.teeda.core.mock.MockApplication;
 import org.seasar.teeda.core.mock.MockApplicationImpl;
 import org.seasar.teeda.core.mock.MockExternalContext;
 import org.seasar.teeda.core.mock.MockExternalContextWrapper;
 import org.seasar.teeda.core.mock.MockFacesContext;
 import org.seasar.teeda.core.mock.MockViewHandlerImpl;
 
 /**
  * @author manhole
  */
 public class HtmlGraphicImageRendererTest extends RendererTest {
 
     private HtmlGraphicImageRenderer renderer;
 
     private MockHtmlGraphicImage htmlGraphicImage;
 
     protected void setUp() throws Exception {
         super.setUp();
         renderer = createHtmlGraphicImageRenderer();
         htmlGraphicImage = new MockHtmlGraphicImage();
         htmlGraphicImage.setRenderer(renderer);
     }
 
     public void testEncode_WithNoValue() throws Exception {
         // ## Arrange ##
 
         // ## Act ##
         encodeByRenderer(renderer, htmlGraphicImage);
 
         // ## Assert ##
         assertEquals("<img src=\"\" />", getResponseText());
     }
 
     public void testEncode_RenderFalse() throws Exception {
         // ## Arrange ##
         htmlGraphicImage.setRendered(false);
 
         // ## Act ##
         encodeByRenderer(renderer, htmlGraphicImage);
 
         // ## Assert ##
         assertEquals("", getResponseText());
     }
 
     public void testEncode_WithValue() throws Exception {
         // ## Arrange ##
         htmlGraphicImage.setValue("abc");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlGraphicImage);
 
         // ## Assert ##
         assertEquals("<img src=\"abc\" />", getResponseText());
     }
 
     public void testEncode_WithValueContainsAmpersand() throws Exception {
         // ## Arrange ##
         htmlGraphicImage.setValue("a?a=b&c=d");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlGraphicImage);
 
         // ## Assert ##
         assertEquals("<img src=\"a?a=b&amp;c=d\" />", getResponseText());
     }
 
     public void testEncode_WithId() throws Exception {
         htmlGraphicImage.setId("a");
 
         encodeByRenderer(renderer, htmlGraphicImage);
 
         assertEquals("<img id=\"a\" src=\"\" />", getResponseText());
     }
 
     public void testEncode_WithUnknownAttribute() throws Exception {
         htmlGraphicImage.setId("a");
         htmlGraphicImage.getAttributes().put("a", "1");
         htmlGraphicImage.getAttributes().put("b", "2");
 
         encodeByRenderer(renderer, htmlGraphicImage);
 
         final Diff diff = diff("<img id=\"a\" src=\"\" a=\"1\" b=\"2\" />",
                 getResponseText());
         assertEquals(diff.toString(), true, diff.identical());
     }
 
     public void testEncode_UrlEncode() throws Exception {
         // ## Arrange ##
         MockFacesContext context = getFacesContext();
         final ExternalContext externalContext = new MockExternalContextWrapper(
                 (MockExternalContext) context.getExternalContext()) {
             public String encodeResourceURL(String url) {
                 return url + "_2";
             }
         };
 
         final MockApplication application = new MockApplicationImpl() {
             public ViewHandler getViewHandler() {
                 return new MockViewHandlerImpl() {
                     public String getResourceURL(FacesContext context,
                             String path) {
                         return path + "_1";
                     }
                 };
             }
         };
         context.setApplication(application);
         context.setExternalContext(externalContext);
 
         htmlGraphicImage.setValue("abc");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlGraphicImage);
 
         // ## Assert ##
         assertEquals("<img src=\"abc_1_2\" />", getResponseText());
     }
 
     public void testEncode_WithAllAttributes() throws Exception {
         htmlGraphicImage.setAlt("a");
         htmlGraphicImage.setDir("b");
         htmlGraphicImage.setHeight("c");
         htmlGraphicImage.setIsmap(true);
         htmlGraphicImage.setLang("e");
         htmlGraphicImage.setLongdesc("f");
         htmlGraphicImage.setOnclick("g");
         htmlGraphicImage.setOndblclick("h");
         htmlGraphicImage.setOnkeydown("i");
         htmlGraphicImage.setOnkeypress("j");
         htmlGraphicImage.setOnkeyup("k");
         htmlGraphicImage.setOnmousedown("l");
         htmlGraphicImage.setOnmousemove("m");
         htmlGraphicImage.setOnmouseout("n");
         htmlGraphicImage.setOnmouseover("o");
         htmlGraphicImage.setOnmouseup("p");
         htmlGraphicImage.setStyle("q");
         htmlGraphicImage.setStyleClass("r");
         htmlGraphicImage.setTitle("s");
         htmlGraphicImage.setUsemap("t");
         htmlGraphicImage.setWidth("u");
 
         htmlGraphicImage.setId("A");
         htmlGraphicImage.setValue("B");
 
         encodeByRenderer(renderer, htmlGraphicImage);
 
         Diff diff = new Diff("<img id=\"A\" src=\"B\"" + " alt=\"a\""
                 + " dir=\"b\"" + " height=\"c\"" + " ismap=\"true\""
                 + " lang=\"e\"" + " longdesc=\"f\"" + " onclick=\"g\""
                 + " ondblclick=\"h\"" + " onkeydown=\"i\""
                 + " onkeypress=\"j\"" + " onkeyup=\"k\"" + " onmousedown=\"l\""
                 + " onmousemove=\"m\"" + " onmouseout=\"n\""
                 + " onmouseover=\"o\"" + " onmouseup=\"p\"" + " style=\"q\""
                 + " class=\"r\"" + " title=\"s\"" + " usemap=\"t\""
                 + " width=\"u\"" + "/>", getResponseText());
         assertEquals(diff.toString(), true, diff.identical());
     }
 
     public void testGetRendersChildren() throws Exception {
         assertEquals(false, renderer.getRendersChildren());
     }
 
     private HtmlGraphicImageRenderer createHtmlGraphicImageRenderer() {
         return (HtmlGraphicImageRenderer) createRenderer();
     }
 
     protected Renderer createRenderer() {
         HtmlGraphicImageRenderer renderer = new HtmlGraphicImageRenderer();
         renderer.setComponentIdLookupStrategy(getComponentIdLookupStrategy());
         renderer.setRenderAttributes(getRenderAttributes());
         return renderer;
     }
 
     private static class MockHtmlGraphicImage extends HtmlGraphicImage {
         private Renderer renderer_;
 
         private String clientId_;
 
         public void setRenderer(Renderer renderer) {
             renderer_ = renderer;
         }
 
         protected Renderer getRenderer(FacesContext context) {
             if (renderer_ != null) {
                 return renderer_;
             }
             return super.getRenderer(context);
         }
 
         public String getClientId(FacesContext context) {
             if (clientId_ != null) {
                 return clientId_;
             }
             return super.getClientId(context);
         }
 
         public void setClientId(String clientId) {
             clientId_ = clientId;
         }
     }
 
 }
