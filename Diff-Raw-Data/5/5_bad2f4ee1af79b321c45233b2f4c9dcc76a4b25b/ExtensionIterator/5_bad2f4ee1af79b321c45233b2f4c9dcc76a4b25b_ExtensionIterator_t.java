 /*
  * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
  *
  * Licensed under the Aduna BSD-style license.
  */
 package org.openrdf.query.algebra.evaluation.iterator;
 
 import info.aduna.iteration.CloseableIteration;
 import info.aduna.iteration.ConvertingIteration;
 
 import org.openrdf.model.Value;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.algebra.AggregateOperator;
 import org.openrdf.query.algebra.Extension;
 import org.openrdf.query.algebra.ExtensionElem;
 import org.openrdf.query.algebra.ValueExpr;
 import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
 import org.openrdf.query.algebra.evaluation.QueryBindingSet;
 import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
 
 public class ExtensionIterator extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {
 
 	private final Extension extension;
 
 	private final EvaluationStrategy strategy;
 
 	public ExtensionIterator(Extension extension,
 			CloseableIteration<BindingSet, QueryEvaluationException> iter, EvaluationStrategy strategy)
 		throws QueryEvaluationException
 	{
 		super(iter);
 		this.extension = extension;
 		this.strategy = strategy;
 	}
 
 	@Override
 	public BindingSet convert(BindingSet sourceBindings)
 		throws QueryEvaluationException
 	{
 		QueryBindingSet targetBindings = new QueryBindingSet(sourceBindings);
 
 		for (ExtensionElem extElem : extension.getElements()) {
 			ValueExpr expr = extElem.getExpr();
 			if (!(expr instanceof AggregateOperator)) {
 				try {
					// we evaluate each extension element over the targetbindings, so that bindings from
					// a previous extension element in this same extension can be used by other extension elements. 
					// e.g. if a projection contains (?a + ?b as ?c) (?c * 2 as ?d)
					Value targetValue = strategy.evaluate(extElem.getExpr(), targetBindings);
 
 					if (targetValue != null) {
 						// Potentially overwrites bindings from super
 						targetBindings.setBinding(extElem.getName(), targetValue);
 					}
 				}
 				catch (ValueExprEvaluationException e) {
 					// silently ignore type errors in extension arguments. They should not cause the 
 					// query to fail but just result in no additional binding.
 				}
 			}
 		}
 
 		return targetBindings;
 	}
 }
