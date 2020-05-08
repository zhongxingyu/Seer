 // Copyright 2010 Nathaniel Harward
 //
 // This file is part of ndh-commons.
 //
 // ndh-commons is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // ndh-commons is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with ndh-commons. If not, see <http://www.gnu.org/licenses/>.
 
 package us.harward.commons.util;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 
 /**
  * Represents a very simple workflow that transforms an input type to an output type. Input and output objects are validated against
  * predicates and routed to error sinks if they fail (if specified). {@link Workflow} objects are state-less, and designed with the
  * assumption that its components are also state-less and do not have side effects; if this does hold true then the caller is
  * responsible for any required synchronization.
  * 
  * @author nharward
  * @param <F>
  *            the type of object coming into this workflow
  * @param <T>
  *            the type of object produced by this workflow
  */
 public class Workflow<F, T> {
 
     private Predicate<F>         precondition;
     private final Sink<F>        preconditionFailedSink;
 
     private final Function<F, T> function;
 
     private Predicate<T>         postcondition;
     private final Sink<T>        postconditionFailedSink;
 
     /**
      * Convenience wrapper for {@link #Workflow(Predicate, Sink, Function, Predicate, Sink)}, passing in null predicates and
      * predicate failure sinks.
      * 
      * @see {@link #Workflow(Predicate, Sink, Function, Predicate, Sink)}
      */
     public Workflow(final Function<F, T> function) {
         this(null, null, function, null, null);
     }
 
     /**
      * Convenience wrapper for {@link #Workflow(Predicate, Sink, Function, Predicate, Sink)}, passing in null predicate failure
      * sinks.
      * 
      * @see {@link #Workflow(Predicate, Sink, Function, Predicate, Sink)}
      */
     public Workflow(final Predicate<F> precondition, final Function<F, T> function, final Predicate<T> postcondition) {
         this(precondition, null, function, postcondition, null);
     }
 
     /**
      * Creates a new workflow based on the predicates and function to be applied. Only the function to be applied is required, other
      * parameters will be null. Predicates, if null, are assumed to be always true.
      * 
      * @param precondition
      *            a predicate to be invoked for every input to the workflow; items that fail the predicate are sent to the
      *            precondition failure sink. null is the same as always returning "true"
      * @param preconditionFailedSink
      *            sink for items that fail the precondition predicate
      * @param function
      *            the actual pre-input processing function to be called for each input to the workflow
      * @param postconditiona
      *            predicate to be invoked for every result from the workflow (the result of applying the function to an input);
      *            items that fail the predicate are sent to the postcondition failure sink. null is the same as always returning
      *            "true"
      * @param postconditionFailedSink
      *            sink for items that fail the postcondition predicate
      */
     public Workflow(final Predicate<F> precondition, final Sink<F> preconditionFailedSink, final Function<F, T> function,
             final Predicate<T> postcondition, final Sink<T> postconditionFailedSink) {
         Preconditions.checkNotNull(function, "");
         this.function = function;
         if (precondition != null)
             this.precondition = precondition;
         else
             this.precondition = Predicates.alwaysTrue();
         if (postcondition != null)
             this.postcondition = postcondition;
         else
             this.postcondition = Predicates.alwaysTrue();
        this.preconditionFailedSink = preconditionFailedSink != null ? preconditionFailedSink : new DevNull<F>();
        this.postconditionFailedSink = postconditionFailedSink != null ? postconditionFailedSink : new DevNull<T>();
     }
 
     /**
      * Executes the workflow for the set of input objects, placing all (successful) outputs on the given sink.
      * 
      * @param input
      *            the set of inputs to run through the workflow, cannot be null
      * @param output
      *            the sink for objects resulting from the workflow that pass the postcondition predicate
      */
     public void execute(final Iterable<F> inputs, final Sink<T> output) {
         Preconditions.checkNotNull(inputs);
        final Sink<T> outputSink = output != null ? output : new DevNull<T>();
         for (final F input : inputs)
             processInput(input, outputSink);
     }
 
     private void processInput(final F input, final Sink<T> output) {
         if (precondition.apply(input)) {
             final T result = function.apply(input);
             if (postcondition.apply(result)) {
                 output.drop(result);
             } else {
                 postconditionFailedSink.drop(result);
             }
         } else {
             preconditionFailedSink.drop(input);
         }
     }
 
 }
