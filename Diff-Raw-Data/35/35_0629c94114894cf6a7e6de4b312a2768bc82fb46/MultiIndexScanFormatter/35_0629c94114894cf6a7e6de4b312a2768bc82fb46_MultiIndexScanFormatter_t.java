 /**
  * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * 	  disclaimer.
  * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
  * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
  * 	  products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package lupos.cloud.operator.format;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import lupos.cloud.operator.IndexScanContainer;
 import lupos.cloud.operator.MultiIndexScanContainer;
 import lupos.cloud.operator.format.IOperatorFormatter;
 import lupos.cloud.pig.JoinInformation;
 import lupos.cloud.pig.PigQuery;
 import lupos.cloud.pig.operator.PigDistinctOperator;
 import lupos.cloud.pig.operator.PigFilterExectuer;
 import lupos.cloud.pig.operator.PigFilterOperator;
 import lupos.cloud.pig.operator.PigIndexScanOperator;
 import lupos.cloud.pig.operator.PigJoinOperator;
 import lupos.cloud.pig.operator.PigLimitOperator;
 import lupos.cloud.pig.operator.PigOptionalOperator;
 import lupos.cloud.pig.operator.PigProjectionOperator;
 import lupos.cloud.pig.operator.PigUnionOperator;
 import lupos.engine.operators.BasicOperator;
 import lupos.engine.operators.index.BasicIndexScan;
 import lupos.engine.operators.index.Root;
 import lupos.engine.operators.multiinput.Union;
 import lupos.engine.operators.multiinput.join.Join;
 import lupos.engine.operators.multiinput.optional.Optional;
 import lupos.engine.operators.singleinput.AddBinding;
 import lupos.engine.operators.singleinput.Projection;
 import lupos.engine.operators.singleinput.Result;
 import lupos.engine.operators.singleinput.filter.Filter;
 import lupos.engine.operators.singleinput.modifiers.Limit;
 import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
 import lupos.engine.operators.tripleoperator.TriplePattern;
 
 /**
  * Implements the formatter for the index scan operator
  */
 public class MultiIndexScanFormatter implements IOperatorFormatter {
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * luposdate.operators.formatter.OperatorFormatter#serialize(lupos.engine
 	 * .operators.BasicOperator, int)
 	 */
 	@Override
 	public PigQuery serialize(final BasicOperator operator, PigQuery pigQuery) {
 		final MultiIndexScanContainer multiIndexScanContainer = (MultiIndexScanContainer) operator;
 		this.joinMultiIndexScans(multiIndexScanContainer, pigQuery);
 		return pigQuery;
 	}
 
 	public JoinInformation joinMultiIndexScans(
 			MultiIndexScanContainer container, PigQuery pigQuery) {
 		JoinInformation newJoin = null;
 		// ArrayList<BasicOperator> containerOperations = new
 		// ArrayList<BasicOperator>();
 		for (Integer id : container.getContainerList().keySet()) {
 			LinkedList<BasicOperator> curList = container.getContainerList()
 					.get(id);
 			ArrayList<JoinInformation> multiInputist = new ArrayList<JoinInformation>();
 			for (BasicOperator op : curList) {
 				if (op instanceof IndexScanContainer) {
 					new IndexScanCointainerFormatter().serialize(op, pigQuery);
 					multiInputist.add(pigQuery.getLastAddedBag());
 				} else if (op instanceof MultiIndexScanContainer) {
 					final MultiIndexScanContainer c = (MultiIndexScanContainer) op;
 					// containerOperations = c.getOperators();
 					multiInputist.add(this.joinMultiIndexScans(c, pigQuery));
 
 				}
 			}
 
 			newJoin = new JoinInformation();
 			boolean isJoin = false;
 			if (container.getMappingTree().get(id) instanceof Union) {
 				pigQuery.buildAndAppendQuery(new PigUnionOperator(newJoin,
 						multiInputist));
 			} else if (container.getMappingTree().get(id) instanceof Join) {
 				isJoin = true;
 				pigQuery.buildAndAppendQuery(new PigJoinOperator(newJoin,
 						multiInputist, (Join) container.getMappingTree()
 								.get(id)));
 			} else if (container.getMappingTree().get(id) instanceof Optional) {
 				pigQuery.buildAndAppendQuery(new PigOptionalOperator(newJoin,
 						multiInputist, (Optional) container.getMappingTree()
 								.get(id)));
 			} else {
 				throw new RuntimeException(
 						"Multi input operator not found. Add it! -> "
 								+ container.getMappingTree().get(id).getClass());
 			}
 
 			// HashSet<String> variables = new HashSet<String>();
 			int i = 0;
 			for (JoinInformation toRemove : multiInputist) {
 				// variables.addAll(toRemove.getJoinElements());
 				newJoin.addAppliedFilters(toRemove.getAppliedFilters());
 				pigQuery.removeIntermediateBags(toRemove);
 
 				if (isJoin || i == 0) {
					for (String var : toRemove.getBitVectors().keySet()) {
 						newJoin.addBitvector(var, toRemove.getBitVector(var));
 					}
 				} else {
					for (String var : toRemove.getBitVectors().keySet()) {
 						newJoin.mergeBitVecor(var, toRemove.getBitVector(var));
 					}
 				}
 				i++;
 			}
 
 			// newJoin.setJoinElements(new ArrayList<String>(variables));
 			newJoin.mergeOptionalVariables(multiInputist);
 
 			pigQuery.append(removeDuplicatedAliases(newJoin));
 
 			pigQuery.addIntermediateBags(newJoin);
 		}
 
 		pigQuery.addAndExecuteOperation(container.getOperators());
 		return pigQuery.getLastAddedBag();
 	}
 
 	public String removeDuplicatedAliases(JoinInformation oldJoin) {
 		StringBuilder result = new StringBuilder();
 		// prüfe ob es doppelte Aliases gibt und entferne diese
 		ArrayList<String> newElements = new ArrayList<String>();
 		boolean foundDuplicate = false;
 
 		for (String elem : oldJoin.getJoinElements()) {
 			if (newElements.contains(elem)) {
 				foundDuplicate = true;
 			} else {
 				newElements.add(elem);
 			}
 		}
 
 		if (foundDuplicate) {
 			result.append(oldJoin.getName() + " = FOREACH " + oldJoin.getName()
 					+ " GENERATE ");
 			boolean first = true;
 			for (String elem : newElements) {
 				if (!first) {
 					result.append(", ");
 				}
 				result.append("$" + oldJoin.getItemPos(elem));
 				first = false;
 			}
 			result.append(";\n");
 			oldJoin.setJoinElements(newElements);
 		}
 
 		return result.toString();
 
 	}
 }
