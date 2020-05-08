 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.spinx;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Vector;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.swows.util.GraphUtils;
 import org.swows.vocabulary.SP;
 import org.swows.vocabulary.SPINX;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.GraphUtil;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.NodeFactory;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.sparql.core.BasicPattern;
 import com.hp.hpl.jena.sparql.core.TriplePath;
 import com.hp.hpl.jena.sparql.core.Var;
 import com.hp.hpl.jena.sparql.expr.E_Function;
 import com.hp.hpl.jena.sparql.expr.Expr;
 import com.hp.hpl.jena.sparql.expr.ExprFunction;
 import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
 import com.hp.hpl.jena.sparql.expr.ExprList;
 import com.hp.hpl.jena.sparql.expr.ExprVar;
 import com.hp.hpl.jena.sparql.expr.NodeValue;
 import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
 import com.hp.hpl.jena.sparql.modify.request.QuadAcc;
 import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteInsert;
 import com.hp.hpl.jena.sparql.path.P_Alt;
 import com.hp.hpl.jena.sparql.path.P_Distinct;
 import com.hp.hpl.jena.sparql.path.P_FixedLength;
 import com.hp.hpl.jena.sparql.path.P_Inverse;
 import com.hp.hpl.jena.sparql.path.P_Link;
 import com.hp.hpl.jena.sparql.path.P_Mod;
 import com.hp.hpl.jena.sparql.path.P_Multi;
 import com.hp.hpl.jena.sparql.path.P_NegPropSet;
 import com.hp.hpl.jena.sparql.path.P_OneOrMore1;
 import com.hp.hpl.jena.sparql.path.P_OneOrMoreN;
 import com.hp.hpl.jena.sparql.path.P_ReverseLink;
 import com.hp.hpl.jena.sparql.path.P_Seq;
 import com.hp.hpl.jena.sparql.path.P_Shortest;
 import com.hp.hpl.jena.sparql.path.P_ZeroOrMore1;
 import com.hp.hpl.jena.sparql.path.P_ZeroOrMoreN;
 import com.hp.hpl.jena.sparql.path.P_ZeroOrOne;
 import com.hp.hpl.jena.sparql.path.Path;
 import com.hp.hpl.jena.sparql.path.PathVisitor;
 import com.hp.hpl.jena.sparql.sse.Tags;
 import com.hp.hpl.jena.sparql.syntax.Element;
 import com.hp.hpl.jena.sparql.syntax.ElementAssign;
 import com.hp.hpl.jena.sparql.syntax.ElementBind;
 import com.hp.hpl.jena.sparql.syntax.ElementExists;
 import com.hp.hpl.jena.sparql.syntax.ElementFilter;
 import com.hp.hpl.jena.sparql.syntax.ElementGroup;
 import com.hp.hpl.jena.sparql.syntax.ElementMinus;
 import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
 import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
 import com.hp.hpl.jena.sparql.syntax.ElementOptional;
 import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
 import com.hp.hpl.jena.sparql.syntax.ElementService;
 import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
 import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
 import com.hp.hpl.jena.sparql.syntax.ElementUnion;
 import com.hp.hpl.jena.sparql.syntax.Template;
 import com.hp.hpl.jena.update.Update;
 import com.hp.hpl.jena.update.UpdateRequest;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.Filter;
 import com.hp.hpl.jena.util.iterator.Map1;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 public class QueryFactory {
 	
 	/*
 	private static Map<Node,Node> subclassOf = new HashMap<Node, Node>();
 	
 	static {
 		subclassOf.put(SP., value)
 	}
 	*/
 	
 	private Graph graph;
 	private Node queryRootNode;
 	private Query query = null;
 	private UpdateDeleteInsert updateDeleteInsert = null;
 	
 //	private Map<String, Var> varMap = new HashMap<String, Var>();
 	private Map<Node, Var> varMap = new HashMap<Node, Var>();
 	private Map<Node, Var> parentVarMap = null;
 	
 	private QueryFactory(Graph graph, Node queryRootNode, Map<Node, Var> parentVarMap) {
 		this.graph = graph;
 		this.queryRootNode = queryRootNode;
 		this.parentVarMap = parentVarMap;
 	}
 	
 	private ExtendedIterator<Node> getObjects(Node subjNode, Node predNode) {
 		return graph.find(subjNode, predNode, Node.ANY)
 				.mapWith(new Map1<Triple, Node>() {
 					public Node map1(Triple triple) {
 						return triple.getObject();
 					}
 				});
 	}
 	
 	private Node getObject(Node subjNode, Node predNode) {
 		Iterator<Node> nodes = getObjects(subjNode, predNode);
 		if (nodes.hasNext())
 			return nodes.next();
 		return null;
 	}
 	
 	public Node toNode(Node node) {
 		Node varNameNode = getObject(node, SP.varName.asNode());
 		if (varNameNode != null)
 			return toVar(node);
 		return node;
 	}
 
 	public Var toVar(String varName) {
 //		Var var = varMap.get(varName);
 //		if (var == null) {
 			Var var = Var.alloc(varName);
 //			varMap.put(varName,var);
 //		}
 		return var;
 	}
 	
 	public Var toVar(Node varNode) {
 		Var var = varMap.get(varNode);
 //		System.out.print("var: " + varName);
 		if (var == null) {
 			String varName = getObject(varNode, SP.varName.asNode()).getLiteralLexicalForm();
 			var = Var.alloc(varName);
 			varMap.put(varNode,var);
 //			System.out.print(" (newvar)");
 		}
 //		System.out.println(".");
 		return var;
 	}
 	
 	public Var toParentVar(Node varNode) {
 		Var var = parentVarMap.get(varNode);
 //		System.out.print("var: " + varName);
 		if (var == null) {
 			String varName = getObject(varNode, SP.varName.asNode()).getLiteralLexicalForm();
 			var = Var.alloc(varName);
 			parentVarMap.put(varNode,var);
 //			System.out.print(" (newvar)");
 		}
 //		System.out.println(".");
 		return var;
 	}
 
 	static Map<String, String> functionToSymbolMap = new HashMap<String, String>();
 	
 	static {
 		
 		functionToSymbolMap.put("add", Tags.symPlus);
 		functionToSymbolMap.put("sub", Tags.symMinus);
 		functionToSymbolMap.put("mul", Tags.symMult);
 		functionToSymbolMap.put("div", Tags.symDiv);
 		
 		functionToSymbolMap.put("eq", Tags.symEQ);
 		functionToSymbolMap.put("ne", Tags.symNE);
 		functionToSymbolMap.put("ge", Tags.symGE);
 		functionToSymbolMap.put("gt", Tags.symGT);
 		functionToSymbolMap.put("le", Tags.symLE);
 		functionToSymbolMap.put("lt", Tags.symLT);
 		
 		functionToSymbolMap.put("and", Tags.symAnd);
 		functionToSymbolMap.put("or", Tags.symOr);
 		functionToSymbolMap.put("not", Tags.symNot);
 		
 		functionToSymbolMap.put("assign", Tags.symAssign);
 
 	}
 	
 	@SuppressWarnings("unused")
 	private String functionToSymbol(String functionName) {
 		return
 				(functionToSymbolMap.containsKey(functionName))
 					? functionToSymbolMap.get(functionName)
 					: functionName;
 	}
 	
 //	public Item toItem(Node exprRootNode) {
 //		Iterator<Node> exprTypes =
 //				getObjects(exprRootNode, RDF.type.asNode())
 //				.filterDrop(new Filter<Node>() {
 //					@Override
 //					public boolean accept(Node node) {
 //						return node.equals(SP.Expression.asNode());
 //					}
 //				});
 //		while( exprTypes.hasNext() ) {
 //			Node exprType = exprTypes.next();
 //			if (exprType.equals(SPINX.FunctionCall.asNode())) {
 //				Node labelNode = getObject(exprRootNode, SPINX.functionLabel.asNode());
 //				if (labelNode != null) {
 //					
 //					Node iriNode = getObject(exprRootNode, SPINX.functionIRI.asNode());
 //					String symbol = labelNode.getLiteralLexicalForm();
 //					ItemList itemList = new ItemList();
 //
 //					if (iriNode != null)
 //						itemList.add(Item.createNode( iriNode ));
 //					else {
 //						//if (symbol.equals("exists"))
 //						itemList.add(functionToSymbol(symbol));
 //					}
 ////					System.out.println("Function " + symbol + ", evaluating args...");
 //					int argCount = 1;
 //					while (true) {
 //						Node argUriNode = NodeFactory.createURI(SP.getURI() + "arg" + argCount++);
 //						Node argNode = getObject(exprRootNode, argUriNode);
 //						if (argNode == null)
 //							break;
 //						//Expr subExpr = toExpr(graph, argNode);
 //						Item subItem = toItem(argNode);
 //						itemList.add(subItem);
 //					}
 ////					if (exprType.equals(SPINX.OpCall.asNode())) {
 ////						Node subElementNode = getObject(exprRootNode, SPINX.element.asNode());
 ////						Algebra.compile(toElement(subElementNode));
 ////					}
 ////					System.out.println("Function " + symbol + ", evaluated args.");
 //					return Item.createList(itemList);
 //				}
 //			}
 //		}
 //		return Item.createNode( toNode(exprRootNode) );
 //	}
 
 //	public Expr toExpr(Node exprRootNode) {
 //		Iterator<Node> exprTypes =
 //				getObjects(exprRootNode, RDF.type.asNode())
 //				.filterDrop(new Filter<Node>() {
 //					@Override
 //					public boolean accept(Node node) {
 //						return node.equals(SP.Expression.asNode());
 //					}
 //				});
 //		while( exprTypes.hasNext() ) {
 //			Node exprType = exprTypes.next();
 //			if (exprType.equals(SPINX.OpCall.asNode())) {
 //				Node subElementNode = getObject(exprRootNode, SPINX.element.asNode());
 //				Element subElement = toElement(subElementNode);
 //				Node labelNode = getObject(exprRootNode, SPINX.functionLabel.asNode());
 //				if (labelNode.equals(Tags.tagExists))
 //					return new E_Exists(subElement);
 //				else if (labelNode.equals(Tags.tagNotExists))
 //					return new E_NotExists(subElement);
 //			}
 //		}
 //		return BuilderExpr.buildExpr( toItem(exprRootNode) );
 //	}
 	
 	public Aggregator toAggregator(Node exprRootNode) {
 		Iterator<Node> exprTypes =
 				getObjects(exprRootNode, RDF.type.asNode())
 				.filterDrop(new Filter<Node>() {
 					@Override
 					public boolean accept(Node node) {
 						return
 								node.equals(SP.Expression.asNode())
 								|| node.equals(SP.Aggregation.asNode());
 					}
 				});
 		if (exprTypes.hasNext()) {
 			Node aggregType = exprTypes.next();
 			Node innerExpr = getObject(exprRootNode, SP.expression.asNode());
 			Node distinct = getObject(exprRootNode, SP.distinct.asNode());
 			Properties scalarvals = null;
 			Iterator<Node> scalarvalNodes = GraphUtils.getPropertyValues(graph, exprRootNode, SPINX.scalarval.asNode());
 			if  (scalarvalNodes.hasNext())
 				scalarvals = new Properties();
 			while (scalarvalNodes.hasNext()) {
 				Node scalarvalNode = scalarvalNodes.next();
 				Node keyNode = GraphUtils.getSingleValueProperty(graph, scalarvalNode, SPINX.key.asNode());
 				Node valueNode = GraphUtils.getSingleValueProperty(graph, scalarvalNode, SPINX.value.asNode());
 				scalarvals.setProperty(keyNode.getLiteralLexicalForm(), valueNode.getLiteralLexicalForm());
 			}
 			return AggregatorSymbols.getAggregator(
 					aggregType,
 					(distinct != null),
 					(innerExpr == null) ? null : toExpr(innerExpr),
 					scalarvals );
 		}
 		throw new RuntimeException("Aggregation type not found!");
 	}
 	
 	public Expr toExpr(Node exprRootNode) {
 		Iterator<Node> exprTypes =
 				getObjects(exprRootNode, RDF.type.asNode())
 				.filterDrop(new Filter<Node>() {
 					@Override
 					public boolean accept(Node node) {
 						return node.equals(SP.Expression.asNode());
 					}
 				});
 		while( exprTypes.hasNext() ) {
 			Node exprType = exprTypes.next();
 			if (exprType.equals(SPINX.FunctionCall.asNode())) {
 				Node labelNode = getObject(exprRootNode, SPINX.functionLabel.asNode());
 				if (labelNode != null) {
 					String symbol = labelNode.getLiteralLexicalForm();
 					Class<? extends ExprFunction> functClass = KnownFunctionsMapping.get(symbol);
 					try {
 						try {
 							Constructor<? extends ExprFunctionOp> constructor =
 									(functClass.asSubclass(ExprFunctionOp.class)).getConstructor(Element.class);
 							Node subElementNode = getObject(exprRootNode, SPINX.element.asNode());
 							return constructor.newInstance(toElement(subElementNode));
 						} catch(ClassCastException cce) {
 							ExprList argList = new ExprList();
 							int argCount = 1;
 							while (true) {
 								Node argUriNode = NodeFactory.createURI(SP.getURI() + "arg" + argCount++);
 								Node argNode = getObject(exprRootNode, argUriNode);
 								if (argNode == null)
 									break;
 								argList.add(toExpr(argNode));
 							}
 							try {
 								Constructor<? extends E_Function> constructor =
 										(functClass.asSubclass(E_Function.class)).getConstructor(String.class, ExprList.class);
 								Node iriNode = getObject(exprRootNode, SPINX.functionIRI.asNode());
 								return constructor.newInstance( iriNode.getURI(), argList );
 							} catch(ClassCastException cce1) {
 								try {
 									Constructor<? extends ExprFunction> constructor =
 											functClass.getConstructor(ExprList.class);
 									return constructor.newInstance(argList);
 								} catch(NoSuchMethodException nce) {
 									if (argList.size() == 0) {
 										try {
 											Constructor<? extends ExprFunction> constructor =
 													functClass.getConstructor();
 											return constructor.newInstance();
 										} catch(NoSuchMethodException nce1) {
 											throw new RuntimeException("Constructor for zero arguments not available for function " + symbol);
 										}
 									}
 									try {
 										Constructor<? extends ExprFunction> constructor =
 												functClass.getConstructor(Expr.class, ExprList.class);
 										return constructor.newInstance(argList.get(0), argList.tail(1));
 									} catch(NoSuchMethodException nce1) {
 										if (argList.size() == 1) {
 											try {
 												Constructor<? extends ExprFunction> constructor =
 														functClass.getConstructor(Expr.class);
 												return constructor.newInstance(argList.get(0));
 											} catch(NoSuchMethodException nce2) {
 												throw new RuntimeException("Constructor for one argument not available for function " + symbol);
 											}
 										}
 										try {
 											Constructor<? extends ExprFunction> constructor =
 													functClass.getConstructor(Expr.class, Expr.class, ExprList.class);
 											return constructor.newInstance(argList.get(0), argList.get(1), argList.tail(2));
 										} catch(NoSuchMethodException nce2) {
 											if (argList.size() == 2) {
 												try {
 													Constructor<? extends ExprFunction> constructor =
 															functClass.getConstructor(Expr.class, Expr.class);
 													return constructor.newInstance(argList.get(0), argList.get(1));
 												} catch(NoSuchMethodException nce3) {
 													throw new RuntimeException("Constructor for two arguments not available for function " + symbol);
 												}
 											}
 											try {
 												Constructor<? extends ExprFunction> constructor =
 														functClass.getConstructor(Expr.class, Expr.class, Expr.class, ExprList.class);
 												return constructor.newInstance(argList.get(0), argList.get(1), argList.get(2), argList.tail(3));
 											} catch(NoSuchMethodException nce3) {
 												if (argList.size() == 3) {
 													try {
 														Constructor<? extends ExprFunction> constructor =
 																functClass.getConstructor(Expr.class, Expr.class, Expr.class);
 														return constructor.newInstance(argList.get(0), argList.get(1), argList.get(2));
 													} catch(NoSuchMethodException nce4) {
 														throw new RuntimeException("Constructor for three arguments not available for function " + symbol);
 													}
 												}
 												try {
 													Constructor<? extends ExprFunction> constructor =
 															functClass.getConstructor(Expr.class, Expr.class, Expr.class, Expr.class, ExprList.class);
 													return constructor.newInstance(argList.get(0), argList.get(1), argList.get(2), argList.get(3), argList.tail(4));
 												} catch(NoSuchMethodException nce4) {
 													if (argList.size() == 3) {
 														try {
 															Constructor<? extends ExprFunction> constructor =
 																	functClass.getConstructor(Expr.class, Expr.class, Expr.class, Expr.class);
 															return constructor.newInstance(argList.get(0), argList.get(1), argList.get(2), argList.get(3));
 														} catch(NoSuchMethodException nce5) {
 															throw new RuntimeException("Constructor for four arguments not available for function " + symbol);
 														}
 													}
 													throw new RuntimeException("Constructor not available for function " + symbol);
 												}
 											}
 										}
 									}
 								}
 							}
 						
 						}
 					} catch(NoSuchMethodException e) {
 						throw new RuntimeException(e);
 					} catch(InvocationTargetException e) {
 						throw new RuntimeException(e);
 					} catch (IllegalArgumentException e) {
 						throw new RuntimeException(e);
 					} catch (InstantiationException e) {
 						throw new RuntimeException(e);
 					} catch (IllegalAccessException e) {
 						throw new RuntimeException(e);
 					}
 					
 				} 
 			} else if (exprType.equals(SP.Aggregation.asNode())) {
 				Aggregator aggregator = toAggregator(exprRootNode);
 				return query.allocAggregate(aggregator);
 //				return new ExprAggregator(toVar(""), aggregator);
 			}
 		}
 		Node nodeExpr = toNode(exprRootNode);
 		if (nodeExpr.isVariable())
 			return new ExprVar(nodeExpr);
 		else
 			return NodeValue.makeNode(nodeExpr);
 	}
 	
 	/*
 
 	public Expr toExpr(String symbol, ExprList argList) {
 		if (symbol.equalsIgnoreCase(Tags.tagMD5))
 			return new E_MD5(argList.get(0));
 		else if (symbol.equalsIgnoreCase(Tags.tagStr))
 			return new E_Str(argList.get(0));
 		else if (symbol.equalsIgnoreCase(Tags.tagUri))
 			return new E_URI(argList.get(0));
 		else if (symbol.equalsIgnoreCase(Tags.tagIri))
 			return new E_IRI(argList.get(0));
 		else if (symbol.equalsIgnoreCase(Tags.tagConcat))
 			return new E_StrConcat(argList);
 		//return null;
 		throw new RuntimeException("Unrecognized function symbol " + symbol);
 	}
 	
 	public Expr toExpr(Node exprRootNode) {
 		Iterator<Node> exprTypes =
 				getObjects(exprRootNode, RDF.type.asNode())
 				.filterDrop(new Filter<Node>() {
 					@Override
 					public boolean accept(Node node) {
 						return node.equals(SP.Expression.asNode());
 					}
 				});
 		while( exprTypes.hasNext() ) {
 			Node exprType = exprTypes.next();
 			if (exprType.equals(SPINX.FunctionCall.asNode())) {
 				Node labelNode = getObject(exprRootNode, SPINX.functionLabel.asNode());
 				if (labelNode != null) {
 					String symbol = labelNode.getLiteralLexicalForm();
 					System.out.println("Function " + symbol + ", evaluating args...");
 					ExprList argList = new ExprList();
 					int argCount = 1;
 					while (true) {
 						Node argUriNode = NodeFactory.createURI(SP.getURI() + "arg" + argCount++);
 						Node argNode = getObject(exprRootNode, argUriNode);
 						if (argNode == null)
 							break;
 						argList.add(toExpr(argNode));
 					}
 					System.out.println("Function " + symbol + ", evaluated args.");
 					return toExpr(symbol, argList);
 				}
 			} else if (exprType.equals(SP.Variable.asNode())) {
 				//String varName = getObject(exprRootNode, SP.varName.asNode()).getLiteralLexicalForm();
 				//return new ExprVar(varName);
 				return new ExprVar(toVar(exprRootNode));
 			}
 		}
 		return NodeValue.makeNode(exprRootNode);
 	}
 */
 
 
 	public Path toPath(Node pathRootNode) {
 		Iterator<Node> pathTypes =
 				getObjects(pathRootNode, RDF.type.asNode())
 				.filterDrop(new Filter<Node>() {
 					@Override
 					public boolean accept(Node node) {
 						return node.equals(SP.Path.asNode());
 					}
 				});
 		while( pathTypes.hasNext() ) {
 			Node pathType = pathTypes.next();
 			Node subPathNode = getObject(pathRootNode, SP.subPath.asNode());
 			Node path1Node = getObject(pathRootNode, SP.path1.asNode());
 			Node path2Node = getObject(pathRootNode, SP.path2.asNode());
 			if (pathType.equals(SP.ModPath.asNode())) {
 				Node modMinNode = getObject(pathRootNode, SP.modMin.asNode());
 				Node modMaxNode = getObject(pathRootNode, SP.modMax.asNode());
 				long min =
 						(modMinNode != null)
 							? ( Long.parseLong(modMinNode.getLiteralLexicalForm()) ) : 0;
 				long max =
 						(modMaxNode != null)
 							? ( Long.parseLong(modMaxNode.getLiteralLexicalForm()) ) : -1;
 				return new P_Mod(toPath(subPathNode), min, max);
 			} else if (pathType.equals(SP.ReversePath.asNode())) {
 				return new P_Inverse(toPath(subPathNode));
 			} else if (pathType.equals(SP.AltPath.asNode())) {
 				return new P_Alt(toPath(path1Node), toPath(path2Node));
 			} else if (pathType.equals(SP.SeqPath.asNode())) {
 				return new P_Seq(toPath(path1Node), toPath(path2Node));
 			}
 		}
 		return new P_Link(toNode(pathRootNode));
 	}
 	
 	private Node getElementType(Node elementNode) {
 		return getObjects(elementNode, RDF.type.asNode())
 				.filterKeep(new Filter<Node>() {
 					@Override
 					public boolean accept(Node node) {
 						return	node.equals(SPINX.Exists.asNode())
 								|| node.equals(SPINX.NotExists.asNode())
 								|| node.equals(SPINX.Assign.asNode())
 								|| node.equals(SP.Bind.asNode())
 								|| node.equals(SP.Filter.asNode())
 								|| node.equals(SPINX.ElementGroup.asNode())
 								|| node.equals(SPINX.EmptyElement.asNode())
 								|| node.equals(SP.Minus.asNode())
 								|| node.equals(SP.NamedGraph.asNode())
 								|| node.equals(SP.Optional.asNode())
 								|| node.equals(SP.TriplePattern.asNode())
 								|| node.equals(SP.TriplePath.asNode())
 								|| node.equals(SP.Service.asNode())
 								|| node.equals(SP.SubQuery.asNode())
 								|| node.equals(SP.Union.asNode())
 								|| node.equals(SPINX.Values.asNode());	
 					}
 				}).next();
 	}
 	
 	private int elementContextCount = 0;
 	private static Set<Var> emptyVarSet = new HashSet<Var>();
 	
 	private class ElementContext implements Comparable<ElementContext> {
 		private int id = elementContextCount++;
 		private int priority = 0;
 		private Element element = null;
 		private Set<Var> producedVars = null;
 		private Set<Var> consumedVars = null;
 		private TriplePath triplePath = null;
 		public ElementContext(Element element) {
 			this.element = element;
 		}
 		public ElementContext(
 				Element element,
 				Set<Var> consumedVars) {
 			this.element = element;
 			this.consumedVars = consumedVars;
 		}
 		public ElementContext(
 				Element element,
 				Set<Var> consumedVars,
 				Var producedVar) {
 			this.element = element;
 			this.consumedVars = consumedVars;
 			this.producedVars = new HashSet<Var>();
 			this.producedVars.add(producedVar);
 		}
 		public ElementContext(
 				Element element,
 				Set<Var> consumedVars,
 				Set<Var> producedVars) {
 			this.element = element;
 			this.consumedVars = consumedVars;
 			this.producedVars = producedVars;
 		}
 		public ElementContext(
 				Element element,
 				TriplePath triplePath,
 				Set<Var> consumedVars,
 				Set<Var> producedVars) {
 			this.element = element;
 			this.triplePath = triplePath;
 			this.consumedVars = consumedVars;
 			this.producedVars = producedVars;
 		}
 		@SuppressWarnings("unused")
 		public ElementContext(
 				Element element,
 				TriplePath triplePath,
 				Set<Var> consumedVars,
 				Set<Var> producedVars,
 				int priority) {
 			this.element = element;
 			this.triplePath = triplePath;
 			this.consumedVars = consumedVars;
 			this.producedVars = producedVars;
 			this.priority = priority;
 		}
 		@SuppressWarnings("unused")
 		public ElementContext(Element element, int priority) {
 			this.element = element;
 			this.priority = priority;
 		}
 		@SuppressWarnings("unused")
 		public ElementContext(
 				Element element,
 				Set<Var> consumedVars,
 				int priority) {
 			this.element = element;
 			this.consumedVars = consumedVars;
 			this.priority = priority;
 		}
 		public ElementContext(
 				Element element,
 				Set<Var> consumedVars,
 				Var producedVar,
 				int priority) {
 			this.element = element;
 			this.consumedVars = consumedVars;
 			this.producedVars = new HashSet<Var>();
 			this.producedVars.add(producedVar);
 			this.priority = priority;
 		}
 		public ElementContext(
 				Element element,
 				Set<Var> consumedVars,
 				Set<Var> producedVars,
 				int priority) {
 			this.element = element;
 			this.consumedVars = consumedVars;
 			this.producedVars = producedVars;
 			this.priority = priority;
 		}
 		public Element getElement() {
 			return element;
 		}
 		public Set<Var> getProducedVars() {
 			return (producedVars != null) ? producedVars : emptyVarSet;
 		}
 		public Set<Var> getConsumedVars() {
 			return (consumedVars != null) ? consumedVars : emptyVarSet;
 		}
 		@SuppressWarnings("unused")
 		public int getPriority() {
 			return priority;
 		}
 		public int compareTo(ElementContext other) {
 			int priorityDiff = this.priority - other.priority;
 			return (priorityDiff != 0) ? priorityDiff : this.id - other.id;
 		}
 		public TriplePath getTriplePath() {
 			return triplePath;
 		}
 		@SuppressWarnings("unused")
 		public boolean isTriplePath() {
 			return triplePath != null;
 		}
 	}
 
 	public Element toElement(Node elementRootNode) {
 		return toElementContext(elementRootNode).element;
 	}
 	
 	public ElementContext toElementContext(Node elementRootNode) {
 		Node elementType = getElementType(elementRootNode);
 		Node subElementNode = getObject(elementRootNode, SPINX.element.asNode());
 		ExtendedIterator<Node> subElementNodes = getObjects(elementRootNode, SPINX.element.asNode());
 		Node varNode = getObject(elementRootNode, SPINX.var.asNode());
 		Node exprNode = getObject(elementRootNode, SPINX.expr.asNode());
 		Node subjNode = getObject(elementRootNode, SP.subject.asNode());
 		Node predNode = getObject(elementRootNode, SP.predicate.asNode());
 		Node objNode = getObject(elementRootNode, SP.object.asNode());
 		Node pathNode = getObject(elementRootNode, SP.path.asNode());
 		if (elementType.equals(SPINX.Exists.asNode())) {
 			ElementContext subElementContext = toElementContext(subElementNode);
 			return 
 					new ElementContext(
 							new ElementExists( subElementContext.getElement() ),
 							subElementContext.consumedVars,
 							subElementContext.producedVars );
 		} else if (elementType.equals(SPINX.NotExists.asNode())) {
 			ElementContext subElementContext = toElementContext(subElementNode);
 			return 
 					new ElementContext(
 							new ElementNotExists( subElementContext.getElement() ),
 							subElementContext.consumedVars,
 							subElementContext.producedVars );
 		} else if (elementType.equals(SPINX.Assign.asNode())) {
 			Var var = toVar(varNode);
 			Expr expr = toExpr(exprNode);
 			return 
 					new ElementContext(
 							new ElementAssign(var, expr), expr.getVarsMentioned(), var );
 		} else if (elementType.equals(SP.Bind.asNode())) {
 			Var var = toVar(varNode);
 			Expr expr = toExpr(exprNode);
 			return new ElementContext(
 					new ElementBind(var, expr), expr.getVarsMentioned(), var, -2);
 		} else if (elementType.equals(SP.Filter.asNode())) {
 			Expr expr = toExpr(exprNode);
 			return new ElementContext(new ElementFilter(expr), expr.getVarsMentioned());
 		} else if (elementType.equals(SPINX.ElementGroup.asNode())) {
 			@SuppressWarnings("unused")
 			boolean allTriplePatterns = true;
 			boolean allTriplePaths = true;
 			ElementGroup elementGroup = new ElementGroup();
 //			ElementTriplesBlock elementTriplesBlock = new ElementTriplesBlock();
 			ElementPathBlock elementPathBlock = new ElementPathBlock();
 			SortedSet<ElementContext> elementContexts = new TreeSet<QueryFactory.ElementContext>();
 			Set<Var> producedVarSet = new HashSet<Var>();
 			while (subElementNodes.hasNext()) {
 				Node subElementNode2 = subElementNodes.next();
 				Node elementTypeNode = getElementType( subElementNode2 );
 
 				ElementContext elementContext = toElementContext(subElementNode2);
 				producedVarSet.addAll(elementContext.getProducedVars());
 				elementContexts.add(elementContext);
 				
 				if (elementTypeNode.equals(SP.TriplePattern.asNode())) {
 //					Node tripleSubjNode = toNode(subSubjNode);
 //					Node triplePredNode = toNode(subPredNode);
 //					Node tripleObjNode = toNode(subObjNode);
 //					elementTriplesBlock.addTriple(
 //							new Triple(
 //									tripleSubjNode, triplePredNode, tripleObjNode) );
 //					elementPathBlock.addTriplePath(
 //							new TriplePath(
 //									tripleSubjNode,
 //									new P_Link(triplePredNode),
 //									tripleObjNode) );
 				} else {
 					allTriplePatterns = false;
 //					if (elementTypeNode.equals(SP.TriplePath.asNode()))
 //						elementPathBlock.addTriplePath(
 //								new TriplePath(
 //										toNode(subSubjNode),
 //										toPath(subPathNode),
 //										toNode(subObjNode)));
 //					else
 					if (!elementTypeNode.equals(SP.TriplePath.asNode()))
 						allTriplePaths = false;
 				}
 			}
 //			if ( allTriplePatterns )
 //				return new ElementContext(elementTriplesBlock, null, producedVarSet);
 //			if ( allTriplePaths ) {				
 //				return new ElementContext(elementPathBlock, null, producedVarSet);
 //			}
 			Set<Var> valorizedVarSet = new HashSet<Var>();
 			Set<Var> extConsumedVarSet = new HashSet<Var>();
 			while ( !elementContexts.isEmpty() ) {
 				List<ElementContext> toBeDeletedContexts = new Vector<ElementContext>();
 				for ( ElementContext elementContext : elementContexts) {
 //					Logger.getRootLogger().trace(
 //					"Checking element (" + elementContext.getElement()
 //					+ ") consuming {" + elementContext.getConsumedVars()
 //					+ "} producing {" + elementContext.getProducedVars()
 //					+ "}");
 					boolean allValorized = true;
 					for ( Var var : elementContext.getConsumedVars()) {
 						if ( !producedVarSet.contains(var) )
 							extConsumedVarSet.add(var);
 						else if ( !valorizedVarSet.contains(var) && !elementContext.getProducedVars().contains(var))
 							allValorized = false;
 					}
 					if (allValorized) {
 						if ( allTriplePaths )			
 							elementPathBlock.addTriplePath(elementContext.getTriplePath());
 						else
 							elementGroup.addElement(elementContext.getElement());
 //						Logger.getRootLogger().trace(
 //									"Insert of element (" + elementContext.getElement()
 //									+ ") consuming {" + elementContext.getConsumedVars()
 //									+ "} producing {" + elementContext.getProducedVars()
 //									+ "}");
 						toBeDeletedContexts.add(elementContext);
 						valorizedVarSet.addAll(elementContext.getProducedVars());
 						break;
 					}
 				}
 				if (toBeDeletedContexts.isEmpty()) {
 					break;
 				}
 				elementContexts.removeAll(toBeDeletedContexts);
 			}
 			if ( allTriplePaths )			
 				return new ElementContext(elementPathBlock, null, producedVarSet);
 			else
 				return new ElementContext(elementGroup, extConsumedVarSet, producedVarSet);
 		} else if (elementType.equals(SPINX.EmptyElement.asNode())) {
 			return new ElementContext( new ElementGroup() );
 		} else if (elementType.equals(SP.Minus.asNode())) {
 			ElementContext subElementContext = toElementContext(subElementNode);
 			return new ElementContext(
 					new ElementMinus(subElementContext.getElement()),
 					subElementContext.getConsumedVars(),
 					subElementContext.getProducedVars());
 		} else if (elementType.equals(SP.NamedGraph.asNode())) {
 			Node graphNameNode = getObject(elementRootNode, SP.graphNameNode.asNode());
 			if (graphNameNode.isURI()) {
				if (query != null)
					query.addNamedGraphURI(graphNameNode.getURI());
 //				else if (updateDeleteInsert != null)
 //					updateDeleteInsert.addUsingNamed(graphNameNode);
 			}
 			ElementContext subElementContext = toElementContext(subElementNode);
 			return
 					new ElementContext(
 							new ElementNamedGraph( toNode(graphNameNode), subElementContext.getElement() ),
 							subElementContext.getConsumedVars(),
 							subElementContext.getProducedVars(), 1 );
 		} else if (elementType.equals(SP.Optional.asNode())) {
 			ElementContext subElementContext = toElementContext(subElementNode);
 			return 
 					new ElementContext(
 							new ElementOptional( subElementContext.getElement() ),
 							subElementContext.getConsumedVars(),
 							subElementContext.getProducedVars() );
 		} else if (elementType.equals(SP.TriplePattern.asNode())) {
 			Node tripleSubjNode = toNode(subjNode);
 			Node triplePredNode = toNode(predNode);
 			Node tripleObjNode = toNode(objNode);
 			Triple triple =	new Triple(tripleSubjNode, triplePredNode, tripleObjNode);
 			ElementTriplesBlock elementTriplesBlock = new ElementTriplesBlock();
 			elementTriplesBlock.addTriple(triple);
 			Set<Var> producedVars = new HashSet<Var>();
 			if (tripleSubjNode instanceof Var)
 				producedVars.add((Var) tripleSubjNode);
 			if (triplePredNode instanceof Var)
 				producedVars.add((Var) triplePredNode);
 			if (tripleObjNode instanceof Var)
 				producedVars.add((Var) tripleObjNode);
 			return new ElementContext(elementTriplesBlock, new TriplePath(triple), null, producedVars);
 //			return new ElementContext(elementTriplesBlock, new TriplePath(triple), producedVars, producedVars);
 		} else if (elementType.equals(SP.TriplePath.asNode())) {
 			Node tripleSubjNode = toNode(subjNode);
 			Path triplePathNode = toPath(pathNode);
 			Node tripleObjNode = toNode(objNode);
 			TriplePath triplePath =	new TriplePath(tripleSubjNode, triplePathNode, tripleObjNode);
 			ElementPathBlock elementPathBlock = new ElementPathBlock();
 			elementPathBlock.addTriplePath(triplePath);
 			final Set<Var> producedVars = new HashSet<Var>();
 			if (tripleSubjNode instanceof Var)
 				producedVars.add((Var) tripleSubjNode);
 			triplePathNode.visit(new PathVisitor() {
 				public void visit(P_Seq pathSeq) {}
 				public void visit(P_Alt pathAlt) {}
 				public void visit(P_ZeroOrOne path) {}
 				public void visit(P_FixedLength pFixedLength) {}
 				public void visit(P_Mod pathMod) {}
 				public void visit(P_Inverse inversePath) {}
 				public void visit(P_NegPropSet pathNotOneOf) {}
 				public void visit(P_ReverseLink pathNode) {
 					Node predNode = pathNode.getNode();
 					if (predNode instanceof Var)
 						producedVars.add((Var) predNode);
 				}
 				public void visit(P_Link pathNode) {
 					Node predNode = pathNode.getNode();
 					if (predNode instanceof Var)
 						producedVars.add((Var) predNode);
 				}
 				public void visit(P_Distinct arg0) {}
 				public void visit(P_Multi arg0) {}
 				public void visit(P_Shortest arg0) {}
 				public void visit(P_ZeroOrMore1 arg0) {}
 				public void visit(P_ZeroOrMoreN arg0) {}
 				public void visit(P_OneOrMore1 arg0) {}
 				public void visit(P_OneOrMoreN arg0) {}
 			});
 			if (tripleObjNode instanceof Var)
 				producedVars.add((Var) tripleObjNode);
 			return new ElementContext(elementPathBlock, triplePath, null, producedVars);
 //			return new ElementContext(elementPathBlock, triplePath, producedVars, producedVars, -1);
 		} else if (elementType.equals(SP.Service.asNode())) {
 			Node serviceNode = getObject(elementRootNode, SP.serviceURI.asNode());
 			ElementContext subElementContext = toElementContext(subElementNode);
 			return 
 					new ElementContext(
 							new ElementService(toNode(serviceNode), subElementContext.getElement(),false),
 							subElementContext.getConsumedVars(),
 							subElementContext.getProducedVars(), 2 );
 		} else if (elementType.equals(SP.SubQuery.asNode())) {
 			Node queryNode = getObject(elementRootNode, SP.query.asNode());
 			// TODO: should consider also query wide var consuming/producing?
 			Query subQuery = toQuery(graph, queryNode, varMap);
 			return 
 					new ElementContext(
 							new ElementSubQuery( subQuery ),
 							null,
 							new CopyOnWriteArraySet<Var>(subQuery.getProjectVars()) );
 		} else if (elementType.equals(SP.Union.asNode())) {
 			ElementUnion elementUnion = new ElementUnion();
 			HashSet<Var> consumedVars = new HashSet<Var>();
 			HashSet<Var> producedVars = new HashSet<Var>();
 			while (subElementNodes.hasNext()) {
 				ElementContext subElementContext = toElementContext(subElementNodes.next());
 				elementUnion.addElement(subElementContext.getElement());
 				consumedVars.addAll(subElementContext.getConsumedVars());
 				producedVars.addAll(subElementContext.getProducedVars());
 			}
 			consumedVars.removeAll(producedVars);
 			return new ElementContext(elementUnion, consumedVars, producedVars);
 		} else
 			return null;
 	}
 
 /*
 	public Element toElement(Node elementRootNode) {
 		Node elementType = getElementType(elementRootNode);
 		Node subElementNode = getObject(elementRootNode, SPINX.element.asNode());
 		ExtendedIterator<Node> subElementNodes = getObjects(elementRootNode, SPINX.element.asNode());
 		Node varNode = getObject(elementRootNode, SPINX.var.asNode());
 		Node exprNode = getObject(elementRootNode, SPINX.expr.asNode());
 		Node subjNode = getObject(elementRootNode, SP.subject.asNode());
 		Node predNode = getObject(elementRootNode, SP.predicate.asNode());
 		Node objNode = getObject(elementRootNode, SP.object.asNode());
 		Node pathNode = getObject(elementRootNode, SP.path.asNode());
 		if (elementType.equals(SPINX.Exists.asNode())) {
 			return new ElementExists( toElement(subElementNode) );
 		} else if (elementType.equals(SPINX.NotExists.asNode())) {
 			return new ElementNotExists( toElement(subElementNode) );
 		} else if (elementType.equals(SPINX.Assign.asNode())) {
 			return new ElementAssign( toVar(varNode), toExpr(exprNode) );
 		} else if (elementType.equals(SP.Bind.asNode())) {
 			return new ElementBind( toVar(varNode), toExpr(exprNode) );
 		} else if (elementType.equals(SP.Filter.asNode())) {
 			return new ElementFilter( toExpr(exprNode) );
 		} else if (elementType.equals(SPINX.ElementGroup.asNode())) {
 			boolean allTriplePatterns = true;
 			boolean allTriplePaths = true;
 			ElementGroup elementGroup = new ElementGroup();
 			ElementTriplesBlock elementTriplesBlock = new ElementTriplesBlock();
 			ElementPathBlock elementPathBlock = new ElementPathBlock();
 			//Set<Element> bindElementSet = new HashSet<Element>();
 			Map<Element,Set<Var>> element2consumedVarSet = new HashMap<Element,Set<Var>>();
 			Map<Element,Var> element2producedVar = new HashMap<Element,Var>();
 			Set<Var> producedVarSet = new HashSet<Var>();
 			while (subElementNodes.hasNext()) {
 				Node subElementNode2 = subElementNodes.next();
 				Node subSubjNode = getObject(subElementNode2, SP.subject.asNode());
 				Node subPredNode = getObject(subElementNode2, SP.predicate.asNode());
 				Node subObjNode = getObject(subElementNode2, SP.object.asNode());
 				Node subPathNode = getObject(subElementNode2, SP.path.asNode());
 				Node elementTypeNode = getElementType( subElementNode2 );
 				Element newElem = toElement(subElementNode2);
 				if (elementTypeNode.equals(SP.Bind.asNode())) {
 					//bindElementSet.add(newElem);
 					producedVarSet.add( ( (ElementBind) newElem ).getVar() );
 					element2producedVar.put( newElem, ( (ElementBind) newElem ).getVar() );
 					element2consumedVarSet.put( newElem, ( (ElementBind) newElem ).getExpr().getVarsMentioned());
 				} else {
 					System.out.println(
 							"Element (" + newElem
 							+ ") in group " + elementGroup);
 					elementGroup.addElement(newElem);
 				}
 				Set<Var> valorizedVarSet = new HashSet<Var>();
 				boolean somethingChanged = true;
 				while ( !element2consumedVarSet.keySet().isEmpty()
 						&& somethingChanged ) {
 					somethingChanged = false;
 					List<Element> toBeDeletedElems = new Vector<Element>();
 					for ( Element elem : element2consumedVarSet.keySet()) {
 						boolean allValorized = true;
 						for ( Var var : element2consumedVarSet.get(elem)) {
 							if ( 	producedVarSet.contains(var)
 									&& !valorizedVarSet.contains(var) )
 								allValorized = false;
 						}
 						if (allValorized) {
 							elementGroup.addElement(elem);
 							toBeDeletedElems.add(elem);
 							valorizedVarSet.add(element2producedVar.get(elem));
 							somethingChanged = true;
 							System.out.println(
 									"Bind Element ("
 									+ elem
 									+ ") the expr ("
 									+ ((ElementBind) elem ).getExpr()
 									+ ") with vars { "
 									+ element2consumedVarSet.get(elem)
 									+ "} as var "
 									+ element2producedVar.get(elem)
 									+ " in group " + elementGroup );
 						}
 					}
 					for ( Element elem : toBeDeletedElems ) {
 						element2consumedVarSet.remove(elem);
 						element2producedVar.remove(elem);
 					}
 				}
 				if (elementTypeNode.equals(SP.TriplePattern.asNode())) {
 					Triple triple =
 							new Triple(
 									toNode(subSubjNode),
 									toNode(subPredNode),
 									toNode(subObjNode));
 					elementTriplesBlock.addTriple( triple );
 					elementPathBlock.addTriple( triple );
 				} else {
 					allTriplePatterns = false;
 					if (elementTypeNode.equals(SP.TriplePath.asNode()))
 						elementPathBlock.addTriplePath(
 								new TriplePath(
 										toNode(subSubjNode),
 										toPath(subPathNode),
 										toNode(subObjNode)));
 					else
 						allTriplePaths = false;
 				}
 			}
 			if ( allTriplePatterns )
 				return elementTriplesBlock;
 			if ( allTriplePaths )				
 				return elementPathBlock;
 			return elementGroup;
 		} else if (elementType.equals(SPINX.EmptyElement.asNode())) {
 			return new ElementGroup();
 		} else if (elementType.equals(SP.Minus.asNode())) {
 			return new ElementMinus( toElement(subElementNode) );
 		} else if (elementType.equals(SP.NamedGraph.asNode())) {
 			Node graphNameNode = getObject(elementRootNode, SP.graphNameNode.asNode());
 			return new ElementNamedGraph( toNode(graphNameNode), toElement(subElementNode) );
 		} else if (elementType.equals(SP.Optional.asNode())) {
 			return new ElementOptional( toElement(subElementNode) );
 		} else if (elementType.equals(SP.TriplePattern.asNode())) {
 			ElementTriplesBlock elementTriplesBlock = new ElementTriplesBlock();
 			elementTriplesBlock.addTriple(
 					new Triple(toNode(subjNode), toNode(predNode), toNode(objNode)) );
 			return elementTriplesBlock;
 		} else if (elementType.equals(SP.TriplePath.asNode())) {
 			ElementPathBlock elementPathBlock = new ElementPathBlock();
 			elementPathBlock.addTriplePath(
 					new TriplePath(toNode(subjNode), toPath(pathNode), toNode(objNode)) );
 			return elementPathBlock;
 		} else if (elementType.equals(SP.Service.asNode())) {
 			Node serviceNode = getObject(elementRootNode, SP.serviceURI.asNode());
 			return new ElementNamedGraph( toNode(serviceNode), toElement(subElementNode) );
 		} else if (elementType.equals(SP.SubQuery.asNode())) {
 			Node queryNode = getObject(elementRootNode, SP.query.asNode());
 			return new ElementSubQuery( toQuery(queryNode) );
 		} else if (elementType.equals(SP.Union.asNode())) {
 			ElementUnion elementUnion = new ElementUnion();
 			while (subElementNodes.hasNext()) {
 				Element subElement = toElement(subElementNodes.next());
 				elementUnion.addElement(subElement);
 			}
 			return elementUnion;
 		} else
 			return null;
 	}
 	*/
 
 	public Triple toTemplateTriple(Node tripleRootNode) {
 		Node subjNode = toNode( getObject(tripleRootNode, SP.subject.asNode()) );
 		Node predNode = toNode( getObject(tripleRootNode, SP.predicate.asNode()) );
 		Node objNode = toNode( getObject(tripleRootNode, SP.object.asNode()) );
 		return new Triple(subjNode, predNode, objNode);
 	}
 	
 	public Template toTemplate(Node templateRootNode) {
 		BasicPattern tripleList = new BasicPattern();
 		ExtendedIterator<Node> tripleNodes = getObjects(templateRootNode, SPINX.triple.asNode());
 		while (tripleNodes.hasNext()) {
 			tripleList.add(toTemplateTriple(tripleNodes.next()));
 		}
 		return new Template(tripleList);
 	}
 	
 	public Query toQuery(Node queryRootNode) {
 		Query query = new Query();
 		query.setSyntax(Syntax.syntaxSPARQL_11);
 		ExtendedIterator<Node> typeNodes = getObjects(queryRootNode, RDF.type.asNode());
 		while (typeNodes.hasNext()) {
 			Node typeNode = typeNodes.next();
 			if (typeNode.equals(SP.Ask.asNode()))
 				query.setQueryAskType();
 			else if (typeNode.equals(SP.Construct.asNode()))
 				query.setQueryConstructType();
 			else if (typeNode.equals(SP.Describe.asNode()))
 				query.setQueryDescribeType();
 			else if (typeNode.equals(SP.Select.asNode()))
 				query.setQuerySelectType();
 		}
 		Node templateNode =	getObject(queryRootNode, SP.templates.asNode());
 		if (templateNode != null) {
 			query.setConstructTemplate(toTemplate(templateNode));
 		}
 		Node elementNode =	getObject(queryRootNode, SP.where.asNode());
 		if (elementNode != null) {
 			query.setQueryPattern(toElement(elementNode));
 		}
 		Iterator<Node> resultVarNodes =	getObjects( queryRootNode, SPINX.resultVariable.asNode());
 		while (resultVarNodes.hasNext()) {
 			Node resultVarNode = resultVarNodes.next();
 			Node aliasNode = getObject(resultVarNode, SP.as.asNode());
 			Node exprNode = getObject(resultVarNode, SP.expression.asNode());
 			if ( exprNode != null ) {
 				if ( aliasNode != null )
 					query.addResultVar(toParentVar(aliasNode), toExpr(exprNode));
 				else
 					query.addResultVar(toExpr(exprNode));
 			} else {
 				if ( aliasNode != null )
 					query.addResultVar(toParentVar(aliasNode));
 			}
 		}
 		Iterator<Node> groupByNodes =	getObjects( queryRootNode, SP.groupBy.asNode());
 		while (groupByNodes.hasNext()) {
 			Node groupByNode = groupByNodes.next();
 			Node aliasNode = getObject(groupByNode, SP.as.asNode());
 			Node exprNode = getObject(groupByNode, SP.expression.asNode());
 			if ( exprNode != null ) {
 				if ( aliasNode != null )
 					query.addGroupBy(toParentVar(aliasNode), toExpr(exprNode));
 				else
 					query.addGroupBy(toExpr(exprNode));
 			} else {
 				if ( aliasNode != null )
 					query.addGroupBy(toParentVar(aliasNode));
 			}
 		}
 		return query;
 	}
 
 	public Query toQuery() {
 		query = new Query();
 		query.setSyntax(Syntax.syntaxSPARQL_11);
 		ExtendedIterator<Node> typeNodes = getObjects(queryRootNode, RDF.type.asNode());
 		while (typeNodes.hasNext()) {
 			Node typeNode = typeNodes.next();
 			if (typeNode.equals(SP.Ask.asNode()))
 				query.setQueryAskType();
 			else if (typeNode.equals(SP.Construct.asNode()))
 				query.setQueryConstructType();
 			else if (typeNode.equals(SP.Describe.asNode()))
 				query.setQueryDescribeType();
 			else if (typeNode.equals(SP.Select.asNode()))
 				query.setQuerySelectType();
 		}
 		Node templateNode =	getObject(queryRootNode, SP.templates.asNode());
 		if (templateNode != null) {
 			query.setConstructTemplate(toTemplate(templateNode));
 		}
 		Node elementNode =	getObject(queryRootNode, SP.where.asNode());
 		if (elementNode != null) {
 			query.setQueryPattern(toElement(elementNode));
 		}
 		Iterator<Node> resultVarNodes =	getObjects( queryRootNode, SPINX.resultVariable.asNode());
 		while (resultVarNodes.hasNext()) {
 			Node resultVarNode = resultVarNodes.next();
 			Node aliasNode = getObject(resultVarNode, SP.as.asNode());
 			Node exprNode = getObject(resultVarNode, SP.expression.asNode());
 			if ( exprNode != null ) {
 				if ( aliasNode != null )
 					query.addResultVar(toParentVar(aliasNode), toExpr(exprNode));
 				else
 					query.addResultVar(toExpr(exprNode));
 			} else {
 				if ( aliasNode != null )
 					query.addResultVar(toParentVar(aliasNode));
 				else
 					query.addResultVar(toParentVar(resultVarNode));
 			}
 		}
 		Node limitNode = getObject(queryRootNode, SP.limit.asNode());
 		if (limitNode != null && limitNode.isLiteral()) {
 			Object limitObj = limitNode.getLiteralValue();
 			if (limitObj instanceof Number)
 				query.setLimit(((Number) limitObj).longValue());
 		}
 		Node offsetNode = getObject(queryRootNode, SP.offset.asNode());
 		if (offsetNode != null && offsetNode.isLiteral()) {
 			Object offsetObj = offsetNode.getLiteralValue();
 			if (offsetObj instanceof Number)
 				query.setOffset(((Number) offsetObj).longValue());
 		}
 		Node distinctNode = getObject(queryRootNode, SP.distinct.asNode());
 		if (distinctNode != null && distinctNode.isLiteral()) {
 			Object distinctObj = distinctNode.getLiteralValue();
 			if (distinctObj instanceof Boolean && ((Boolean) distinctObj).booleanValue())
 				query.setDistinct(true);
 		}
 		Iterator<Node> groupByNodes =	getObjects( queryRootNode, SP.groupBy.asNode());
 		while (groupByNodes.hasNext()) {
 			Node groupByNode = groupByNodes.next();
 			Node aliasNode = getObject(groupByNode, SP.as.asNode());
 			Node exprNode = getObject(groupByNode, SP.expression.asNode());
 			if ( exprNode != null ) {
 				if ( aliasNode != null )
 					query.addGroupBy(toParentVar(aliasNode), toExpr(exprNode));
 				else
 					query.addGroupBy(toExpr(exprNode));
 			} else {
 				if ( aliasNode != null )
 					query.addGroupBy(toParentVar(aliasNode));
 			}
 		}
 		return query;
 	}
 
 	public void toQuads(Node quadsRootNode, QuadAcc quadAcc) {
 		Iterator<Node> tripleNodes = GraphUtils.getPropertyValues(graph, quadsRootNode, SPINX.triple.asNode());
 		while(tripleNodes.hasNext()) {
 			Node tripleNode = tripleNodes.next();
 			quadAcc.addTriple(toTemplateTriple(tripleNode));
 		}
 		Iterator<Node> namedGraphNodes = GraphUtils.getPropertyValues(graph, quadsRootNode, SP.named.asNode());
 		while(namedGraphNodes.hasNext()) {
 			Node namedGraphNode = namedGraphNodes.next();
 			quadAcc.setGraph(namedGraphNode);
 			toQuads(namedGraphNode, quadAcc);
 		}
 	}
 
 	public Update toModify() {
 		updateDeleteInsert = new UpdateDeleteInsert();
 		Node deletePatternNode = GraphUtils.getSingleValueOptProperty(graph, queryRootNode, SP.deletePattern.asNode() );
 		if (deletePatternNode != null) {
 			toQuads(deletePatternNode, updateDeleteInsert.getDeleteAcc());
 		}
 		Node insertPatternNode = GraphUtils.getSingleValueOptProperty(graph, queryRootNode, SP.insertPattern.asNode() );
 		if (insertPatternNode != null) {
 			toQuads(insertPatternNode, updateDeleteInsert.getInsertAcc());
 		}
 		Node elementNode =	getObject(queryRootNode, SP.where.asNode());
 		if (elementNode != null) {
 			updateDeleteInsert.setElement(toElement(elementNode));
 		}
 
 		Iterator<Node> usingNodes = GraphUtils.getPropertyValues(graph, queryRootNode, SP.using.asNode());
 		while(usingNodes.hasNext())
 			updateDeleteInsert.addUsing( usingNodes.next() );
 		Iterator<Node> usingNamedNodes = GraphUtils.getPropertyValues(graph, queryRootNode, SP.using.asNode());
 		while(usingNamedNodes.hasNext())
 			updateDeleteInsert.addUsingNamed( usingNamedNodes.next() );
 		Node withNode = GraphUtils.getSingleValueOptProperty(graph, queryRootNode, SP.with.asNode() );
 		if (withNode != null) {
 			updateDeleteInsert.setWithIRI(withNode);
 		}
 		
 		return updateDeleteInsert;
 	}
 	
 	public Update toUpdate() {
 		Update update = null;
 		ExtendedIterator<Node> typeNodes = getObjects(queryRootNode, RDF.type.asNode());
 		while (update == null && typeNodes.hasNext()) {
 			Node typeNode = typeNodes.next();
 			if (typeNode.equals(SP.Modify.asNode()))
 				update = toModify();
 		}
 		return update;
 	}
 	
 //	private Query getQuery() {
 //		return query;
 //	}
 	
 //	private Update getUpdate() {
 //		return update;
 //	}
 //	
 	public static Query toQuery(Graph graph, Node queryRootNode) {
 //		return new QueryFactory(graph).toQuery(queryRootNode);
 		return new QueryFactory(graph, queryRootNode, new HashMap<Node, Var>()).toQuery();
 	}
 	
 	public static void addTextualQueries(final Graph graph) {
 		Set<Triple> triplesToBeAdded =
 				graph.find(Node.ANY, RDF.type.asNode(), SP.Query.asNode())
 //				.filterDrop(new Filter<Triple>() {
 //					@Override
 //					public boolean accept(Triple triple) {
 //						Node queryNode = triple.getSubject();
 //						return graph.find(Node.ANY, SP.query.asNode(), queryNode).hasNext();
 //					}
 //				})
 				.mapWith(new Map1<Triple, Triple>() {
 					public Triple map1(Triple triple) {
 						Node queryNode = triple.getSubject();
 						String queryString = toQuery(graph, queryNode).toString();
 						return new Triple(queryNode, SP.text.asNode(), NodeFactory.createLiteral(queryString));
 					}
 				}).toSet();
 		GraphUtil.add(graph, triplesToBeAdded.iterator());
 	}
 	
 	public static Query toQuery(Graph graph, Node queryRootNode, Map<Node, Var> parentVarMap) {
 //		return new QueryFactory(graph).toQuery(queryRootNode);
 		return new QueryFactory(graph, queryRootNode, parentVarMap).toQuery();
 	}
 	
 	public static Update toUpdate(Graph graph, Node queryRootNode) {
 //		return new QueryFactory(graph).toQuery(queryRootNode);
 		return new QueryFactory(graph, queryRootNode, new HashMap<Node, Var>()).toUpdate();
 	}
 	
 	public static UpdateRequest toUpdateRequest(Graph graph, Node queryRootNode) {
 		UpdateRequest updateRequest = new UpdateRequest();
 		Iterator<Node> updateNodes = GraphUtils.getPropertyValues(graph, queryRootNode, RDFS.member.asNode());
 		while (updateNodes.hasNext()) {
 			updateRequest.add( toUpdate(graph, updateNodes.next()) );
 		}
 		return updateRequest;
 	}
 	
 
 }
