 /*
  * Copyright (c) 2012 Ray A. Conner
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.github.rconner.anansi;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 /**
  * Package implementation of the Traverser returned by {@link Traversers#preOrder(Traverser)}.
  *
  * @param <V>
  * @param <E>
  */
 final class PreOrderTraverser<V, E> implements Traverser<V, E> {
     private final Traverser<V, E> adjacency;
 
     PreOrderTraverser( final Traverser<V, E> adjacency ) {
         this.adjacency = adjacency;
     }
 
     @Override
     public Iterable<Walk<V, E>> apply( final V start ) {
         return new Iterable<Walk<V, E>>() {
             @Override
             public Iterator<Walk<V, E>> iterator() {
                 return new PreOrderIterator<V, E>( start, adjacency );
             }
         };
     }
 
     private static final class PreOrderIterator<V, E> implements PruningIterator<Walk<V, E>> {
         /**
          * The supplied adjacency function.
          */
         private final Traverser<V, E> adjacency;
 
         /**
          * A stack of Iterators. The next object to be returned is from the topmost Iterator which has something left to
          * return. As objects are returned, the above function is used to create new Iterators which are pushed onto the
          * stack, even if they are empty.
          */
         private final LinkedList<Iterator<Walk<V, E>>> iteratorStack = Lists.newLinkedList();
 
         /**
          * True if this iterator is in a valid state for calling remove() or prune().
          */
         private boolean canMutate = false;
 
         /**
          * Collects adjacency walks and produces the compound walks to return.
          */
         private final Walk.Builder<V, E> builder;
 
         PreOrderIterator( final V start, final Traverser<V, E> adjacency ) {
             this.adjacency = adjacency;
             iteratorStack.addFirst( Iterators.singletonIterator( Walk.<V, E>empty( start ) ) );
             builder = Walk.from( start );
         }
 
         @Override
         public boolean hasNext() {
             for( final Iterator<Walk<V, E>> iterator : iteratorStack ) {
                 if( iterator.hasNext() ) {
                     return true;
                 }
             }
             return false;
         }
 
         @Override
         public Walk<V, E> next() {
             while( !iteratorStack.isEmpty() ) {
                 if( iteratorStack.getFirst().hasNext() ) {
                     final Walk<V, E> result = iteratorStack.getFirst().next();
                     iteratorStack.addFirst( adjacency.apply( result.getTo() ).iterator() );
                     builder.add( result );
                     canMutate = true;
                     return builder.build();
                 }
                 iteratorStack.removeFirst();
                builder.pop();
             }
             canMutate = false;
             throw new NoSuchElementException();
         }
 
         @Override
         public void remove() {
             Preconditions.checkState( canMutate );
             iteratorStack.removeFirst();
             builder.pop();
             iteratorStack.getFirst().remove();
             canMutate = false;
         }
 
         @Override
         public void prune() {
             Preconditions.checkState( canMutate );
             iteratorStack.removeFirst();
             builder.pop();
             canMutate = false;
         }
     }
 }
