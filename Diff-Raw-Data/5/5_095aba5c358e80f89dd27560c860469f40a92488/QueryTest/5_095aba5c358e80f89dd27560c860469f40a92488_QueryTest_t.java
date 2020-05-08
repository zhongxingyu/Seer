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
 
 import java.io.ByteArrayInputStream;
 import java.util.Iterator;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.rucksac.ParseException;
 import org.rucksac.PseudoClassNotSupportedException;
 import org.rucksac.PseudoFunctionNotSupportedException;
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
 
     @Before
     public void setup() throws Exception {
         String documentAsText = ""
                 + "<foo id='myFoo'>"
                 + "  <bar id='b1' class='baz bum' name='bam' />"
                 + "  <bar id='b2' class='last' name='bim bam'>Hello World</bar>"
                 + "  <baz id='first' class='fazHolder' name='bim-bam-bum' disabled='disabled' checked='checked' dummy=''>"
                 + "    <faz lang='en-us' />"
                 + "  </baz>"
                 + "  <wombat id='only' />"
                 + "  <baz id='second' />"
                 + "  <baz id='third' />"
                 + "</foo>";
 
         this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                 new ByteArrayInputStream(documentAsText.getBytes("UTF-8")));
     }
 
     private void assertNext(String expectedName, Iterator<Node> result) {
         assertNext(expectedName, null, result);
     }
 
     private void assertNext(String expectedName, String expectedId, Iterator<Node> result) {
         assertTrue(result.hasNext());
         Node node = result.next();
         assertTrue(node instanceof Element);
         Element e = (Element) node;
         Assert.assertEquals(expectedName, e.getTagName());
         if (expectedId != null) {
             Assert.assertEquals(expectedId, e.getAttribute("id"));
         }
     }
 
     @Test
     public void testAnyElement() {
         Iterator<Node> result = filter("*");
         assertNext("foo", result);
         assertNext("bar", result);
         assertNext("bar", result);
         assertNext("baz", result);
         assertNext("faz", result);
         assertNext("wombat", result);
         assertNext("baz", result);
         assertNext("baz", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementOfType() {
         Iterator<Node> result = filter("bar");
         assertNext("bar", result);
         assertNext("bar", result);
         assertFalse(result.hasNext());
 
         result = filter("foo");
         assertNext("foo", result);
         assertFalse(result.hasNext());
 
         result = filter("foo, bar");
         assertNext("foo", result);
         assertNext("bar", result);
         assertNext("bar", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithAttribute() {
         Iterator<Node> result = filter("bar[name]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter("*[name]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("[dummy]");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithAttributeValue() {
         Iterator<Node> result = filter("bar[name=bam]");
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIncludesOneAttributeValue() {
         Iterator<Node> result = filter("[name~=bam]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter("[name~=bim]");
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter("[name~=bi]");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementStartsWithAttributeValue() {
         Iterator<Node> result = filter("[name^=bim]");
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("[name^=b]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementEndsWithAttributeValue() {
         Iterator<Node> result = filter("[name$=bam]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter("[name$=m]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementContainsAttributeValue() {
         Iterator<Node> result = filter("[name*=bi]");
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("[name*=bam]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("[name*=am]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementStartsWithHyphenatedAttributeValue() {
         Iterator<Node> result = filter("[name|=bim]");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("[name|=bum]");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsRoot() {
         Iterator<Node> result = filter(":root");
         assertNext("foo", result);
         assertFalse(result.hasNext());
 
         result = filter("bar:root");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsNthChild() {
         Iterator<Node> result = filter("#myFoo > :nth-child(3)");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-child('odd')");
         assertNext("bar", "b1", result);
         assertNext("baz", "first", result);
         assertNext("baz", "second", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-child(2n)");
         assertNext("bar", "b2", result);
         assertNext("wombat", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-child(-n+2)");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsNthLastChild() {
         Iterator<Node> result = filter("#myFoo > :nth-last-child(3)");
         assertNext("wombat", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-last-child('odd')");
         assertNext("bar", "b2", result);
         assertNext("wombat", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-last-child(2n)");
         assertNext("bar", result);
         assertNext("baz", result);
         assertNext("baz", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :nth-last-child(-n+2)");
         assertNext("baz", result);
         assertNext("baz", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsNthOfType() {
         Iterator<Node> result = filter("baz:nth-of-type(2)");
         assertNext("baz", "second", result);
         assertFalse(result.hasNext());
 
         result = filter("baz:nth-of-type(odd)");
         assertNext("baz", "first", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
 
         result = filter("wombat:nth-of-type(2)");
         assertFalse(result.hasNext());
 
         result = filter("*:nth-of-type(2n)");
         assertNext("bar", "b2", result);
         assertNext("baz", "second", result);
         assertFalse(result.hasNext());
 
        result = filter("foo > *:nth-of-type(-n+2  )");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
         assertNext("wombat", result);
         assertNext("baz", "second", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsNthLastOfType() {
        Iterator<Node> result = filter("baz:nth-last-of-type(  2 )");
         assertNext("baz", "second", result);
         assertFalse(result.hasNext());
 
         result = filter("baz:nth-last-of-type(odd)");
         assertNext("baz", "first", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
 
         result = filter("wombat:nth-last-of-type(2)");
         assertFalse(result.hasNext());
 
         result = filter("*:nth-last-of-type(2n)");
         assertNext("bar", "b1", result);
         assertNext("baz", "second", result);
         assertFalse(result.hasNext());
 
         result = filter("foo > *:nth-last-of-type(-n+2)");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("wombat", result);
         assertNext("baz", "second", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsFirstChild() {
         Iterator<Node> result = filter("bar:first-child");
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
 
         result = filter("bar.baz:first-child");
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
 
         result = filter(":first-child");
         assertNext("foo", result);
         assertNext("bar", "b1", result);
         assertNext("faz", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsLastChild() {
         Iterator<Node> result = filter("bar:last-child");
         assertFalse(result.hasNext());
 
         result = filter("baz:last-child");
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsFirstOfType() {
         Iterator<Node> result = filter("#myFoo > :first-of-type");
         assertNext("bar", "b1", result);
         assertNext("baz", "first", result);
         assertNext("wombat", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > bar:first-of-type");
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsLastOfType() {
         Iterator<Node> result = filter("#myFoo > :last-of-type");
         assertNext("bar", "b2", result);
         assertNext("wombat", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > bar:last-of-type");
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsOnlyChild() {
         Iterator<Node> result = filter("faz:only-child");
         assertNext("faz", result);
         assertFalse(result.hasNext());
 
         result = filter("* > bar:only-child");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsOnlyOfType() {
         Iterator<Node> result = filter("bar:only-of-type");
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > wombat:only-of-type");
         assertNext("wombat", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithNoChildren() {
         Iterator<Node> result = filter(":empty");
         assertNext("bar", "b1", result);
         assertNext("faz", result);
         assertNext("wombat", result);
         assertNext("baz", "second", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
 
         result = filter(".fazHolder > faz:empty");
         assertNext("faz", result);
         assertFalse(result.hasNext());
 
         result = filter(".fazHolder > :empty");
         assertNext("faz", result);
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
         assertNext("faz", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsEnabledOrDisabled() {
         Iterator<Node> result = filter("#myFoo > :disabled");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > :enabled");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertNext("wombat", result);
         assertNext("baz", "second", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsChecked() {
         Iterator<Node> result = filter("#myFoo > :checked");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
     }
 
     @Test(expected = PseudoClassNotSupportedException.class)
     public void testElementIsIndeterminate() {
         filter(":indeterminate");
     }
 
     @Test
     public void testElementContainsText() {
         Iterator<Node> result = filter(":contains('Hello')");
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter(":contains('World')");
         assertNext("bar", "b2", result);
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
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
 
         result = filter("bar.baz.bum[name]");
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
 
         result = filter(".bam");
         assertFalse(result.hasNext());
 
         result = filter("bar.baz");
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
 
         result = filter("bar.bum");
         assertNext("bar", "b1", result);
         assertFalse(result.hasNext());
 
         result = filter("foo.baz");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementWithId() {
         Iterator<Node> result = filter("#myFoo");
         assertNext("foo", result);
         assertFalse(result.hasNext());
 
         result = filter("foo#myFoo");
         assertNext("foo", result);
         assertFalse(result.hasNext());
 
         result = filter("bar#myFoo");
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementNegation() {
         Iterator<Node> result = filter("#myFoo > *[name*=bam]:not(baz)");
         assertNext("bar", result);
         assertNext("bar", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > *[name*=bam]:not([name~=bam])");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > *[name*=bam]:not(:enabled)");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsDescendant() {
         Iterator<Node> result = filter("#myFoo [name~=bam]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo faz");
         assertNext("faz", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo .fazHolder[name] faz");
         assertNext("faz", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsChild() {
         Iterator<Node> result = filter("#myFoo > [name~=bam]");
         assertNext("bar", "b1", result);
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > faz");
         assertFalse(result.hasNext());
 
         result = filter("#myFoo > .fazHolder[name] > faz");
         assertNext("faz", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsDirectAdjacent() {
         Iterator<Node> result = filter("#myFoo + bar");
         assertFalse(result.hasNext());
 
         result = filter("bar.last + baz");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("bar.last + .fazHolder");
         assertNext("baz", "first", result);
         assertFalse(result.hasNext());
 
         result = filter("bar.bum + [name~=bam]");
         assertNext("bar", "b2", result);
         assertFalse(result.hasNext());
 
         result = filter("baz + baz");
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
     }
 
     @Test
     public void testElementIsPreceded() {
         Iterator<Node> result = filter("#myFoo ~ bar");
         assertFalse(result.hasNext());
 
         result = filter("bar.baz ~ baz");
         assertNext("baz", "first", result);
         assertNext("baz", "second", result);
         assertNext("baz", "third", result);
         assertFalse(result.hasNext());
 
         result = filter("bar.bum ~ [name*=bam]");
         assertNext("bar", "b2", result);
         assertNext("baz", "first", result);
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
