 package com.bluespot.logic;
 
 import java.util.Collection;
 
import junit.framework.AssertionFailedError;

 import com.bluespot.logic.adapters.Adapter;
 import com.bluespot.logic.predicates.Predicate;
 import com.bluespot.logic.visitors.AdaptingVisitor;
 import com.bluespot.logic.visitors.PopulatingVisitor;
 import com.bluespot.logic.visitors.PruningVisitor;
 import com.bluespot.logic.visitors.SingleSentinel;
 import com.bluespot.logic.visitors.Visitor;
 
 /**
  * A collection of factory methods for common {@link Visitor} idioms. The names
  * of these methods are intentionally "conversational" since this library is
  * usually statically imported. If this is the case, you can end up with such
  * statements like:
  * 
  * <pre>
  * when(lowerCase(), addToList(strings));
  * </pre>
  * 
  * On the other hand, sporadic uses of these factory methods are much harder to
  * understand. In those cases, you're better off using the visitor constructors
  * directly.
  * 
  * 
  * @author Aaron Faanes
  * 
  */
 public final class Visitors {
 
     private Visitors() {
         // Suppress default constructor to ensure non-instantiability.
        throw new AssertionFailedError("Instantiation not allowed");
     }
 
     /**
      * Returns a new {@link SingleSentinel} that guards the specified visitor
      * with the specified predicate.
      * 
      * @param <T>
      *            the type of the expected element
      * @param predicate
      *            the predicate that guards the specified visitor. For any
      *            value, the predicate must first evaluate to {@code true}
      *            before the specified visitor is allowed to accept it.
      * @param visitor
      *            the visitor that will accept all items that evaluate to
      *            {@code true} according to the specified predicate
      * @return a new {@code SingleSentinel} that guards the specified visitor
      *         with the specified predicate.
      * @see SingleSentinel
      */
     public static <T> SingleSentinel<T> when(final Predicate<? super T> predicate, final Visitor<? super T> visitor) {
         return new SingleSentinel<T>(predicate, visitor);
     }
 
     /**
      * Returns a new {@link AdaptingVisitor} that accepts values of type {@code
      * S}, converts them with the specified adapter, and passes the converted
      * value to the specified visitor.
      * 
      * @param <S>
      *            the type that is initially accepted by the returned visitor
      *            and converted by the specified adapter
      * @param <D>
      *            the type that is accepted by the specified visitor
      * @param adapter
      *            the adapter that performs the conversion of the accepted value
      * @param targetVisitor
      *            the visitor that ultimately accepted the converted value
      * @return a {@code Visitor} that adapts an accepted value and passes it to
      *         the specified visitor
      * @throws NullPointerException
      *             if either argument is null
      * @see AdaptingVisitor
      */
     public static <S, D> Visitor<S> with(final Adapter<? super S, ? extends D> adapter,
             final Visitor<? super D> targetVisitor) {
         return new AdaptingVisitor<S, D>(adapter, targetVisitor);
     }
 
     /**
      * Returns a new {@link PopulatingVisitor} that adds all visited elements to
      * the specified collection. This method uses {@link Collection#add(Object)}
      * and does not respond to failed or ignored addition requests.
      * 
      * @param <T>
      *            the type of element in the collection
      * @param collection
      *            the collection that is populated by this visitor
      * @return a new {@code Visitor} that adds elements to the specified
      *         collection
      * @see PopulatingVisitor
      */
     public static <T> Visitor<T> addTo(final Collection<? super T> collection) {
         return new PopulatingVisitor<T>(collection);
     }
 
     /**
      * Returns a new {@link PruningVisitor} that removes all visited elements
      * from the specified collection. This method uses
      * {@link Collection#remove(Object)} and does not respond to failed or
      * ignored addition requests.
      * 
      * @param <T>
      *            the type of element in the collection
      * @param collection
      *            the collection that is modified by this visitor
      * @return a new {@code Visitor} that adds elements to the specified
      *         collection
      * @see PruningVisitor
      */
     public static <T> Visitor<T> removeFrom(final Collection<? super T> collection) {
         return new PruningVisitor<T>(collection);
     }
 }
