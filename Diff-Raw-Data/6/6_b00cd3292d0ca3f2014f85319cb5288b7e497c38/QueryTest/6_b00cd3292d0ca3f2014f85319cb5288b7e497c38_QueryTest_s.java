 /*
  * Catapult Framework Project
  *
  * Copyright (c) 2002-2012 CatapultSource.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.rucksac.parser.css;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.rucksac.ParseException;
 import org.rucksac.PseudoClassNotSupportedException;
 import org.rucksac.PseudoFunctionNotSupportedException;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * @author Andreas Kuhrwahl
  * @since 10.08.12
  */
 public class QueryTest {
 
     private Document document;
 
     private Iterator<Node> filter(String query) {
         return new Query<Node>(query).filter(this.document.getDocumentElement()).iterator();
     }
 
     private Element createElement(String name, String id, String styleClass, Attr attr) {
         Element result = this.document.createElement(name);
         if (id != null) {
             result.getAttributes().setNamedItem(createAttribute("id", id));
         }
         if (styleClass != null) {
             result.getAttributes().setNamedItem(createAttribute("class", styleClass));
         }
         if (attr != null) {
             result.getAttributes().setNamedItem(attr);
         }
         return result;
 
     }
 
     private Attr createAttribute(String name, String value) {
         Attr attribute = this.document.createAttribute(name);
         attribute.setValue(value);
         return attribute;
     }
 
     @Before
     public void setup() throws Exception {
         this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
         Element root = createElement("foo", "myFoo", null, null);
         root.appendChild(createElement("bar", null, "baz bum", createAttribute("name", "bam")));
         Element lastBar = createElement("bar", null, "last", createAttribute("name", "bim bam"));
         lastBar.appendChild(this.document.createTextNode("Hello World"));
         root.appendChild(lastBar);
         Element baz = createElement("baz", null, "fazHolder", createAttribute("name", "bim-bam-bum"));
         baz.getAttributes().setNamedItem(createAttribute("disabled", "disabled"));
         baz.getAttributes().setNamedItem(createAttribute("checked", "checked"));
         baz.appendChild(createElement("faz", null, null, createAttribute("lang", "en-us")));
         root.appendChild(baz);
         this.document.appendChild(root);
     }
 
     void assertEquals(String expected, Node node) {
         assertTrue(node instanceof Element);
         Element e = (Element) node;
         Assert.assertEquals(expected, e.getTagName());
     }
 
     @Test
     public void testAnyElement() {
         Iterator<Node> result = filter("*");
         assertEquals("foo", result.next());
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementOfType() {
         Iterator<Node> result = filter("bar");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("foo");
         assertEquals("foo", result.next());
         assertFalse(result.hasNext());
 
         result = filter("foo, bar");
         assertEquals("foo", result.next());
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithAttribute() {
         Iterator<Node> result = filter("bar[name]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("*[name]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithAttributeValue() {
         Iterator<Node> result = filter("bar[name=bam]");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIncludesOneAttributeValue() {
         Iterator<Node> result = filter("[name~=bam]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("[name~=bim]");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("[name~=bi]");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementStartsWithAttributeValue() {
         Iterator<Node> result = filter("[name^=bim]");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("[name^=b]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementEndsWithAttributeValue() {
         Iterator<Node> result = filter("[name$=bam]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("[name$=m]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementContainsAttributeValue() {
         Iterator<Node> result = filter("[name*=bi]");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("[name*=bam]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("[name*=am]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementStartsWithHyphenatedAttributeValue() {
         Iterator<Node> result = filter("[name|=bim]");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("[name|=bum]");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsRoot() {
         Iterator<Node> result = filter(":root");
         assertEquals("foo", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar:root");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsNthChild() {
         Iterator<Node> result = filter("#myFoo > :nth-child(3)");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-child('odd')");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-child(2n)");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-child(-n+2)");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsNthLastChild() {
         Iterator<Node> result = filter("#myFoo > :nth-last-child(3)");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-last-child('odd')");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-last-child(2n)");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-last-child(-n+2)");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     @Ignore
     public void testElementIsNthOfType() {
         fail();
     }
 
     @Test
     @Ignore
     public void testElementIsNthLastOfType() {
         fail();
     }
 
     @Test
     public void testElementIsFirstChild() {
         Iterator<Node> result = filter("bar:first-child");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar.baz:first-child");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter(":first-child");
         assertEquals("foo", result.next());
         assertEquals("bar", result.next());
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsLastChild() {
         Iterator<Node> result = filter("bar:last-child");
         assertFalse(result.hasNext());
 
         result = filter("baz:last-child");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsFirstOfType() {
         Iterator<Node> result = filter("#myFoo > :first-of-type");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > bar:first-of-type");
         Element element = (Element) result.next();
         Assert.assertEquals("bar", element.getTagName());
         Assert.assertEquals("bam", element.getAttribute("name"));
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsLastOfType() {
         Iterator<Node> result = filter("#myFoo > :last-of-type");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > bar:last-of-type");
         Element element = (Element) result.next();
         Assert.assertEquals("bar", element.getTagName());
         Assert.assertEquals("bim bam", element.getAttribute("name"));
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsOnlyChild() {
         Iterator<Node> result = filter("faz:only-child");
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("* > bar:only-child");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsOnlyOfType() {
         Iterator<Node> result = filter("bar:only-of-type");
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > baz:only-of-type");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithNoChildren() {
         Iterator<Node> result = filter(":empty");
         assertEquals("bar", result.next());
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
 
         result = filter(".fazHolder > faz:empty");
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
 
         result = filter(".fazHolder > :empty");
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsHyperLink() {
         filter(":link");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsVisitedHyperLink() {
         filter(":visited");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsActivated() {
         filter(":active");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsHovered() {
         filter(":hover");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsFocused() {
         filter(":focus");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsTarget() {
         filter(":target");
     }
 
     @Test
     public void testElementIsInLanguage() {
         Iterator<Node> result = filter(":lang(en)");
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsEnabledOrDisabled() {
         Iterator<Node> result = filter("#myFoo > :disabled");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :enabled");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsChecked() {
         Iterator<Node> result = filter("#myFoo > :checked");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsIndeterminate() {
         filter(":indeterminate");
     }
 
     @Test
     public void testElementContainsText() {
         Iterator<Node> result = filter(":contains('Hello')");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter(":contains('World')");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter(":contains('World!')");
         assertFalse(result.hasNext());
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testFirstFormattedLineOfElement() {
         filter("::first-line");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testFirstFormattedLetterOfElement() {
         filter("::first-letter");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testCurrentSelectionOfElement() {
         filter("::selection");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testGeneratedContentBeforeOfElement() {
         filter("::before");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testGeneratedContentAfterOfElement() {
         filter("::after");
     }
 
     @Test
     public void testElementWithClass() {
         Iterator<Node> result = filter(".baz");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar.baz.bum[name]");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter(".bam");
         assertFalse(result.hasNext());
 
         result = filter("bar.baz");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar.bum");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("foo.baz");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithId() {
         Iterator<Node> result = filter("#myFoo");
         assertEquals("foo", result.next());
         assertFalse(result.hasNext());
 
         result = filter("foo#myFoo");
         assertEquals("foo", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar#myFoo");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementNegation() {
         Iterator<Node> result = filter("#myFoo > *[name*=bam]:not(baz)");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > *[name*=bam]:not([name~=bam])");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > *[name*=bam]:not(:enabled)");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsDescendant() {
         Iterator<Node> result = filter("#myFoo [name~=bam]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo faz");
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo .fazHolder[name] faz");
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsChild() {
         Iterator<Node> result = filter("#myFoo > [name~=bam]");
         assertEquals("bar", result.next());
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > faz");
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > .fazHolder[name] > faz");
         assertEquals("faz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsDirectAdjacent() {
         Iterator<Node> result = filter("#myFoo + bar");
         assertFalse(result.hasNext());
 
         result = filter("bar.last + baz");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar.last + .fazHolder");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar.bum + [name~=bam]");
         assertEquals("bar", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsPreceded() {
         Iterator<Node> result = filter("#myFoo ~ bar");
         assertFalse(result.hasNext());
 
         result = filter("bar.baz ~ baz");
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
 
         result = filter("bar.bum ~ [name*=bam]");
         assertEquals("bar", result.next());
         assertEquals("baz", result.next());
         assertFalse(result.hasNext());
     }
 
     @Test(expected = PseudoFunctionNotSupportedException.class)
     public void testPseudoFunctionNotSupportedException() {
         filter("::foo(bar)");
     }
 
     @Test(expected = ParseException.class)
     public void testSelectorCombinatorNotSupportedException() {
         filter("foo < bar");
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testPseudoClassNotSupportedException() {
         filter(":foo");
     }
 
     @Test(expected = ParseException.class)
     public void testAttributeOperationNotSupportedException() {
         filter("[foo#=bar]");
     }
 
 }
