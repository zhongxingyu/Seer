 /**
  * AggregatorMutationFunction.java
  * 
  * Copyright 2010 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.impl.mutation;
 
 import java.util.Collection;
 
 import jmona.MutationException;
 import jmona.MutationFunction;
 import jmona.impl.UnmodifiableCollectionAggregator;
 import jmona.random.RandomUtils;
 
 /**
  * A MutationFunction which chooses from a specified Collection of
  * MutationFunctions when performing a mutation.
  * 
  * @author Jeffrey Finkelstein
  * @param <T>
  *          The type of individual on which to perform a mutation.
  * @since 0.5
  */
 public class AggregatorMutationFunction<T> extends
     UnmodifiableCollectionAggregator<MutationFunction<T>> implements MutationFunction<T> {
 
   /**
    * Instantiates this MutationFunction with the specified Collection of
    * MutationFunctions from which to choose when performing a mutation.
    * 
   * @param initialMutationFunctions
    *          The Collection of MutationFunctions from which to choose when
    *          performing a mutation.
    */
   public AggregatorMutationFunction(
       final Collection<MutationFunction<T>> initialMutationFunctions) {
     super(initialMutationFunctions);
   }
 
   /**
    * Mutates the specified individual by using one of the MutationFunction
    * objects specified in the Collection given in the constructor of this class,
    * chosen with uniformly random probability over all elements of that
    * Collection.
    * 
    * @param individual
    *          The individual to mutate.
   * @throws MutationException
    *           If the chosen MutationFunction throws an Exception.
    * @see jmona.MutationFunction#mutate(java.lang.Object)
    */
   @Override
   public void mutate(final T individual) throws MutationException {
     RandomUtils.choice(this.collection()).mutate(individual);
   }
 
 }
