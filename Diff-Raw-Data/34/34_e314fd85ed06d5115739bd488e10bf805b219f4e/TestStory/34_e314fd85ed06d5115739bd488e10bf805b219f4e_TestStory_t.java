 /*
  * codjo (Prototype)
  * =================
  *
  *    Copyright (C) 2012, 2012 by codjo.net
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  *    implied. See the License for the specific language governing permissions
  *    and limitations under the License.
  */
 
 package net.codjo.spike.crts.sample.schema;
 import java.io.IOException;
 import java.io.StringWriter;
 import net.codjo.spike.crts.api.definition.DefinitionBuilder;
 import net.codjo.spike.crts.kernel.RuleEngine;
 import org.intellij.lang.annotations.Language;
 import static net.codjo.spike.crts.sample.schema.IsXsdCompliant.xsdCompliantWith;
 import static net.codjo.test.common.XmlUtil.assertEquivalent;
 import static net.codjo.test.common.matcher.JUnitMatchers.*;
 /**
  *
  */
 class TestStory {
     private final XsdWriter xsdWriter = new XsdWriter();
     private final RuleEngine engine = new RuleEngine();
     private String resultingXsd;
     private String lastXmlToBeChecked;
 
 
     static TestStory init() {
         return new TestStory();
     }
 
 
     public TestStory given() {
         return this;
     }
 
 
     public TestStory pluginDeclare(DefinitionBuilder node) {
         engine.declare(node);
         return this;
     }
 
 
     public TestStory nothing() {
         return this;
     }
 
 
     public TestStory when() {
         engine.start();
         return this;
     }
 
 
     public TestStory generateXsd() throws IOException {
         resultingXsd = createXsd(engine, xsdWriter);
         return this;
     }
 
 
     public TestStory then() {
         return this;
     }
 
 
     public TestStory resultingXsdIsEquivalentTo(String expectedXsd) {
         assertEquivalent(expectedXsd, resultingXsd);
         return this;
     }
 
 
     public TestStory xml(@Language("XML") String xml) {
         lastXmlToBeChecked = xml;
         return this;
     }
 
 
     public TestStory isXsdCompliant() throws Exception {
         assertXmlCompliant(resultingXsd, lastXmlToBeChecked);
         return this;
     }
 
 
     public TestStory isNotXsdCompliant() throws Exception {
         assertXmlNotCompliant(resultingXsd, lastXmlToBeChecked);
         return this;
     }
 
 
     private static String createXsd(RuleEngine engine, XsdWriter xsdWriter) throws IOException {
         StringWriter xsd = new StringWriter();
         xsdWriter.createXsd(engine.getRootNode(), xsd);
         return xsd.toString();
     }
 
 
    private static void assertXmlCompliant(String resultingXsd, String xml) {
         assertThat(xml, is(xsdCompliantWith(resultingXsd)));
     }
 
 
    public static void assertXmlNotCompliant(String xsdContent, String xml) {
         assertThat(xml, is(not(xsdCompliantWith(xsdContent))));
     }
 }
