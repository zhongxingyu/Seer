 package com.pyxis.matchers.dom;
 
 import static org.hamcrest.Matchers.allOf;
 
 import org.hamcrest.Matcher;
 import org.hamcrest.Matchers;
 import org.w3c.dom.Element;
 
 /**
  * A collection of hamcrest matchers to be used in assertions validating a DOM document 
  * (objects of type {@link org.w3c.dom.Element}).  These can be especially useful to validate 
  * XHTML views of an application. 
  */
 public class DomMatchers {
 
     private DomMatchers() {}
 
     /**
      * Checks that the document contains an element(s) selected by given CSS selector, 
      * are matched by specified matcher(s).  If the CSS selector selects more than one
      * elements, they will be matched <strong>in order</strong>, by given matchers, 
      * i.e. the first element selected will be matched by the first matcher
      * argument, the second element by the second matcher argument, and so on.
      * @param selector a CSS selector used to find element(s) in validated document.
      * @param elementsMatchers matchers used to validate element(s) found by selector.
      */
     public static Matcher<Element> hasSelector(String selector, Matcher<? super Element>... elementsMatchers) {
         return HasSelector.hasSelector(selector, elementsMatchers);
     }
 
     /**
      * Checks that the document contains an element(s) selected by given CSS selector, 
      * are matched by specified matcher(s).  If the CSS selector selects more than one
      * elements, they will be matched <strong>in order</strong>, by given matchers, 
      * i.e. the first element selected will be matched by the first matcher
      * argument, the second element by the second matcher argument, and so on.
      * @param selector a CSS selector used to find element(s) in validated document.
      * @param elementsMatchers matchers used to validate element(s) found by selector.
      */
     public static Matcher<Element> hasSelector(String selector, Matcher<Iterable<Element>> elementsMatcher) {
         return HasSelector.hasSelector(selector, elementsMatcher);
     }
 
     /**
      * Checks that the document contains a single element selected by given CSS selector.
      * @param selector a CSS selector used to find an element in validated document.
      */
     public static Matcher<Element> hasUniqueSelector(String selector) {
         return HasUniqueSelector.hasUniqueSelector(selector);
     }
 
     /**
      * Checks that the document contains a single element selected by given CSS selector
      * and that this element is matched by given matcher.
      * @param selector a CSS selector used to find an element in validated document.
      * @param elementMatcher matcher that validates unique element found.
      */
     public static Matcher<Element> hasUniqueSelector(String selector, Matcher<Element> elementMatcher) {
         return HasUniqueSelector.hasUniqueSelector(selector, elementMatcher);
     }
 
     /**
      * Checks that the document contains a single element selected by given CSS selector
      * and that this element is matched by <strong>all</strong> given matchers.
      * @param selector a CSS selector used to find an element in validated document.
      * @param elementMatchers matchers that validate unique element found.
      */
     public static Matcher<Element> hasUniqueSelector(String selector, Matcher<Element>... elementMatchers) {
         return HasUniqueSelector.hasUniqueSelector(selector, allOf(elementMatchers));
     }
 
     /**
      * Checks that the document does not contain any element corresponding to given
      * CSS selector.
      * @param selector a CSS selector
      */
     public static Matcher<Element> hasNoSelector(String selector) {
     	return HasNoSelector.hasNoSelector(selector);
     }
 
     /**
      * Checks that the elements validated are matched by given matchers.  Each validated
      * element is matched by a single matcher argument, i.e. first element is matched by
      * the first matcher argument, second element is matched by second matcher argument, and
      * so on.
      * @param elementMatchers matchers used to validate {@link org.w3c.dom.Element}s 
      */
     public static Matcher<Iterable<Element>> inOrder(Matcher<Element>... elementMatchers) {
         return Matchers.contains(elementMatchers);
     }
 
     /**
      * Checks that elements contain at least one that is matched by given matcher.
      * @param elementMatcher an element matcher.
      */
     @SuppressWarnings("unchecked")
 	public static Matcher<Iterable<Element>> hasElement(Matcher<? super Element> elementMatcher) {
         return Matchers.hasItems(elementMatcher);
     }
 
     /**
      * Checks that elements contain, in any order, at least one matched element for
      * each given matcher.
      * @param elementMatcher an element matcher.
      */
     public static Matcher<Iterable<Element>> hasElements(Matcher<? super Element>... elementMatchers) {
         return Matchers.hasItems(elementMatchers);
     }
 
     /**
      * Checks that an element has specified tag name.
      */
     public static Matcher<Element> withTag(String tagName) {
         return WithTag.withTag(tagName);
     }
 
     /**
      * Checks that an element contains exactly the given text.
      */
     public static Matcher<Element> withText(String contentText) {
         return WithContentText.withContent(contentText);
     }
     
     /**
      * Checks that an element contains only blank characters.  
      */
     public static Matcher<Element> withBlankText() {
     	return WithContentText.withBlankContent();
     }
 
     /**
      * Checks that an element contains text that is matched by specified matcher.
      */
     public static Matcher<Element> withText(Matcher<? super String> contentMatcher) {
         return WithContentText.withContent(contentMatcher);
     }
 
     /**
      * Checks that an element has an attribute that is matched by specified matcher.
      * @param name name of the attribute to validate.
      * @param valueMatcher matcher used to validate attribute's value.
      */
     public static Matcher<Element> withAttribute(String name, Matcher<? super String> valueMatcher) {
         return WithAttribute.withAttribute(name, valueMatcher);
     }
 
     /**
      * Checks that an element has an attribute with specified value.
      * @param name name of the attribute to validate.
      * @param value value the attribute should have. 
      */
     public static Matcher<Element> withAttribute(String name, String value) {
         return WithAttribute.withAttribute(name, value);
     }
 
     /**
      * Checks that an element has a name attribute with specified value.
      * @param name value the name attribute should have.
      */
     public static Matcher<Element> withName(String name) {
         return WithAttribute.withName(name);
     }
 
     /**
      * Checks that an element has an id attribute with specified value.
      * @param id value the id attribute should have.
      */
     public static Matcher<Element> withId(String id) {
         return WithAttribute.withId(id);
     }
 
     /**
      * Checks that an element has class attribute with value containing some text value.
      * @param className value or part of the value the class attribute should have.
      */
     public static Matcher<Element> withClassName(String className) {
         return WithAttribute.withClassName(className);
     }
 
     /**
      * Checks that children of an element are matched by given matcher. 
      */
     public static Matcher<Element> hasChildren(Matcher<Iterable<Element>> childrenMatcher) {
         return HasChildren.hasChildren(childrenMatcher);
     }
 
     /**
     * Checks that each child of an element is matched by all given matchers. 
      */
     public static Matcher<Element> hasChildren(Matcher<Element>... childrenMatchers) {
         return HasChildren.hasChildren(childrenMatchers);
     }
 
     /**
      * Checks that at least one child of an element is matched by given matcher. 
      */
     public static Matcher<Element> hasChild(Matcher<Element> childMatcher) {
         return HasChildren.hasChild(childMatcher);
     }
 
     /**
      * Checks that a collection of elements is of a certain size.
      * @param size expected number of elements.
      */
     public static Matcher<Iterable<Element>> withSize(int size) {
         return Matchers.iterableWithSize(size);
     }
 }
