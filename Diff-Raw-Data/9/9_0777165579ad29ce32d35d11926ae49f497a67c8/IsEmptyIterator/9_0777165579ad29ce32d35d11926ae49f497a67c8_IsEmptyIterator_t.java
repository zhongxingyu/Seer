 package uk.ac.susx.mlcl.erl.test;
 
 import com.google.common.collect.Iterators;
 import org.hamcrest.Description;
 import org.hamcrest.Factory;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeMatcher;
 
 import javax.annotation.Nonnull;
 import java.util.Iterator;
 
 /**
  * Tests if iterator is empty.
  */
 public class IsEmptyIterator<E> extends TypeSafeMatcher<Iterator<? extends E>> {
     /**
      * Creates a matcher for {@link Iterable}s matching examined iterator that yield no items.
      * <p/>
      * For example:
      * <pre>assertThat(new ArrayList&lt;String&gt;().iterator(), is(emptyIterator()))</pre>
      */
     @Nonnull
     @Factory
    public static <E> Matcher<Iterator<E>> emptyIterator() {
        return (Matcher<Iterator<E>>)new IsEmptyIterator<E>();
     }
 
     /**
      * Creates a matcher for {@link Iterable}s matching examined iterables that yield no items.
      * <p/>
      * For example:
      * <pre>assertThat(new ArrayList&lt;String&gt;(), is(emptyIterableOf(String.class)))</pre>
      *
      * @param type the type of the iterable's content
      */
     @Nonnull
     @Factory
     public static <E> Matcher<Iterator<E>> emptyIteratorOf(Class<E> type) {
        return emptyIterator();
     }
 
     @Override
     public boolean matchesSafely(@Nonnull Iterator<? extends E> iterator) {
         return !iterator.hasNext();
     }
 
     @Override
     public void describeMismatchSafely(Iterator<? extends E> iterator, @Nonnull Description mismatchDescription) {
         mismatchDescription.appendValueList("[", ",", "]", iterator);
     }
 
     @Override
     public void describeTo(@Nonnull Description description) {
         description.appendText("an empty iterator");
     }
 }
