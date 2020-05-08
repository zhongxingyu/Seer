 package org.jbehave.core.matchers;
 
 import java.util.ArrayList;
 
 import org.jbehave.core.Ensure;
 import org.jbehave.core.mock.Matcher;
 import org.jbehave.core.mock.UsingMatchers.CustomMatcher;
 
 public class UsingCollectionMatchersBehaviour {
 
     private Matcher eq(Object value) {
     	return UsingEqualityMatchers.eq(value);
 	}
 
 	private Matcher not(Matcher matcher) {
 		return UsingLogicalMatchers.not(matcher);
 	}
     
     public void shouldProvideMatchersForCollectionsContainingAThing() {
         ArrayList list = new ArrayList();
         list.add(Integer.valueOf(3));
         
         Ensure.that(list, UsingCollectionMatchers.contains(Integer.valueOf(3)));
         Ensure.that(list, UsingCollectionMatchers.contains(eq(Integer.valueOf(3))));
         
         Ensure.that(list, not(UsingCollectionMatchers.contains(Integer.valueOf(5))));
         Ensure.that(list, UsingLogicalMatchers.not(UsingCollectionMatchers.contains(eq(Integer.valueOf(5)))));
     }
 
 	public void shouldProvideMatchersForCollectionsContainingSomeThings() {
         ArrayList list = new ArrayList();
         list.add(Integer.valueOf(3));
         list.add(Integer.valueOf(4));
         list.add(Integer.valueOf(5));
     	
     	Ensure.that(list, UsingLogicalMatchers.not(UsingCollectionMatchers.contains(Integer.valueOf(7))));
     	Ensure.that(list, UsingCollectionMatchers.contains(Integer.valueOf(3), Integer.valueOf(4)));
     	Ensure.that(list, UsingCollectionMatchers.contains(Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3)));
         Ensure.that(list, UsingCollectionMatchers.contains(new Object[] {Integer.valueOf(3)}));
        
     	Ensure.that(list, not(UsingCollectionMatchers.contains(eq(Integer.valueOf(7)))));
     	Ensure.that(list, UsingCollectionMatchers.contains(eq(Integer.valueOf(3)), eq(Integer.valueOf(4))));
     	Ensure.that(list, UsingCollectionMatchers.contains(eq(Integer.valueOf(5)), eq(Integer.valueOf(4)), eq(Integer.valueOf(3))));
         Ensure.that(list, UsingCollectionMatchers.contains(new Matcher[] {eq(Integer.valueOf(3))}));
     }
 	
 	public void shouldProvideMatchersForCollectionsContainingOnlyThings() {
         ArrayList list = new ArrayList();
         list.add(Integer.valueOf(3));
         list.add(Integer.valueOf(4));
         list.add(Integer.valueOf(5));
     	
     	Ensure.that(list, UsingLogicalMatchers.not(UsingCollectionMatchers.containsOnly(Integer.valueOf(3), Integer.valueOf(4))));
     	Ensure.that(list, UsingCollectionMatchers.containsOnly(Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3)));
     	Ensure.that(list, UsingCollectionMatchers.containsOnly(eq(Integer.valueOf(5)), eq(Integer.valueOf(4)), eq(Integer.valueOf(3))));
 	}
 	
 	public void shouldProvideMatchersForCollectionsContainingThingsInOrder() {
         ArrayList list = new ArrayList();
         list.add(Integer.valueOf(3));
         list.add(Integer.valueOf(4));
         list.add(Integer.valueOf(5));
     	
     	Ensure.that(list, not(UsingCollectionMatchers.containsInOrder(Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(4))));
     	Ensure.that(list, UsingCollectionMatchers.containsInOrder(Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5)));
 	}
     
     public void shouldDescribeMatchersForCollections() {
         ArrayList list = new ArrayList();
         list.add(Integer.valueOf(5));        
         list.add(Integer.valueOf(6));
         
         CustomMatcher contains = UsingCollectionMatchers.contains(Integer.valueOf(4), Integer.valueOf(5));
         CustomMatcher containsOnly = UsingCollectionMatchers.containsOnly(Integer.valueOf(4), Integer.valueOf(5));
         CustomMatcher containsInOrder = UsingCollectionMatchers.containsInOrder(Integer.valueOf(4), Integer.valueOf(5));
         
         // Describe what we expected
         Ensure.that(contains.toString(), eq("a collection containing [equal to <4>, equal to <5>]"));
         Ensure.that(containsOnly.toString(), eq("a collection containing [equal to <4>, equal to <5>] and nothing else"));
         Ensure.that(containsInOrder.toString(), eq("a collection containing [equal to <4>, equal to <5>] and in order"));
         
         // Describe what we got
		Ensure.that(contains.describe(list), eq("a java.util.ArrayList containing [5, 6]"));
		Ensure.that(containsOnly.describe(list), eq("a java.util.ArrayList containing [5, 6]"));
		Ensure.that(containsInOrder.describe(list), eq("a java.util.ArrayList containing [5, 6]"));
     }
 }
