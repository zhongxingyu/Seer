 /*
  * Copyright (c) 2010 Sharegrove Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.msjs.integration;
 
 import com.gargoylesoftware.htmlunit.html.HtmlButton;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;
 import static junit.framework.Assert.*;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.List;
 
 public class TestForms extends BaseIntegrationTest{
     @Override
     protected String getPageLocation() {
         return "/test/pages/forms";
     }
 
     @Test
     public void testInput() throws IOException {
         HtmlPage page = getPage();
         HtmlButton clearButton = (HtmlButton) page.getByXPath("//button").get(0);
         HtmlInput input = (HtmlInput) page.getByXPath("//input").get(0);
 
         input.type("hi");
         assertNull(getPreWithLabel(page, "CLEARBUTTON"));
 
        assertEquals("INPUT: \"hi\"", getPreWithLabel(page, "INPUT").getTextContent());
         clearButton.click();
        assertEquals("INPUT: \"\"", getPreWithLabel(page, "INPUT").getTextContent());
         assertNotNull(getPreWithLabel(page, "CLEARBUTTON"));
 
     }
 
     @Test
     public void testForm() throws IOException {
         HtmlPage page = getPage();
         HtmlButton clearButton = (HtmlButton) page.getByXPath("//button").get(0);
         HtmlForm form = (HtmlForm) page.getByXPath("//form").get(0);
 
         form.getInputByName("check").click();
         final HtmlInput textInput = form.getInputByName("text");
         //clear out default text
         textInput.type("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
         textInput.type("bye");
         form.getInputByValue("one").click();
         form.fireEvent("submit");
         //FORM: {"check":true,"text":"bye","rad":"one"}
 
         HtmlPreformattedText pre = getPreWithLabel(page, "FORM");
         assertTrue(pre.getTextContent().indexOf("\"check\":true") > 0 );
         assertTrue(pre.getTextContent().indexOf("\"text\":\"bye\"") > 0 );
         assertTrue(pre.getTextContent().indexOf("\"rad\":\"one\"") > 0 );
 
         clearButton.click();
         pre = getPreWithLabel(page, "FORM");
         assertTrue(pre.getTextContent().indexOf("\"check\":false") > 0 );
         assertTrue(pre.getTextContent().indexOf("\"text\":\"starting value\"") > 0 );
         assertTrue(pre.getTextContent().indexOf("\"rad\":null") >0 );
 
     }
 
     private HtmlPreformattedText getPreWithLabel(HtmlPage page, final String label) {
         List<HtmlPreformattedText> pres = (List<HtmlPreformattedText>) page.getByXPath("//pre");
 
         for (HtmlPreformattedText pre : pres){
             final String preText = pre.getTextContent();
             if(preText.indexOf(label) ==0){
                 return pre;
             }
         }
         return null;
     }
 }
