 package org.magnolialang.terms;
 
 import static org.magnolialang.terms.TermFactory.*;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.imp.pdb.facts.*;
 import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.utils.Pair;
 import org.magnolialang.errors.ImplementationError;
 import org.magnolialang.util.ParsetreeAdapter;
 import org.rascalmpl.values.uptr.Factory;
 import org.rascalmpl.values.uptr.ProductionAdapter;
 import org.rascalmpl.values.uptr.SymbolAdapter;
 import org.rascalmpl.values.uptr.TreeAdapter;
 
 public class TermImploder {
 
 	/**
 	 * Implode an AsFix tree to an XaTree.
 	 * @param tree A tree in AsFix format
 	 * @return An imploded XaTree
 	 */
 	public static IConstructor implodeTree(IConstructor tree) {
 		if(ParsetreeAdapter.isParseTree(tree))
 			return implode(ParsetreeAdapter.getTop(tree));
 		else
 			return implode(tree);
 	}
 
 	public static final Type Attr = tf.abstractDataType(ts, "Attr");
 	public static final Type Attr_Abstract = tf.constructor(ts, Attr, "selectable");
 
 
 	public static IConstructor implode(IConstructor tree) {
 
 		IConstructor result = null;
 		IList concrete = null;
 
 		Type nodeType = tree.getConstructorType();
 		if(nodeType == Factory.Tree_Appl) {
 			IConstructor prod = TreeAdapter.getProduction(tree);
 			IList lhs = null;
 			if(!ProductionAdapter.isList(prod))
 			{
 				lhs = ProductionAdapter.getLhs(prod);
 			}
 			IConstructor  rhs = ProductionAdapter.getRhs(prod);
 			IList attrs = ProductionAdapter.getAttributes(prod);
 			IListWriter newAttrs = vf.listWriter(tf.nodeType());
 			boolean hasAbstract = false;
 			String cons = null;
 			String sort = getSortName(prod);
 
 			for (IValue attr : attrs) {
 				if (attr.getType().isAbstractDataType() && ((IConstructor) attr).getConstructorType() == Factory.Attr_Term) {
 					IValue value = ((IConstructor)attr).get("term");
 					if (value.getType().isNodeType() && ((INode) value).getName().equals("cons")) {
 						cons = ((IString)((INode) value).get(0)).getValue();
 					}
 					else if (value.getType().isNodeType() && ((INode) value).getName().equals("abstract")) {
 						hasAbstract = true;
 					}
 					else
 						newAttrs.append(value);
 				}
 			}
 			if(cons == null)
 				cons = ProductionAdapter.getConstructorName(prod);
 
 			//		String name = SymbolAdapter.getName(rhs);
 			// Token: [lex] -> cf
 			if(ProductionAdapter.isLexToCf(prod)) {
 				String str = TreeAdapter.yield(tree);
 				return leaf(str, sort);
 			}
 			else if(sort.equals("<START>")) {
 				Pair<IValue[], IList> t = visitChildren(TreeAdapter.getArgs(tree));
 				concrete = t.second;
 				cons = cons != null ? cons : sort;
 				result = (IConstructor)t.first[0];
 				IListWriter cst = vf.listWriter(Type_XaToken);
 				for(IValue tok : concrete) {
					System.out.println(tok);
 					if(((IConstructor)tok).getConstructorType() == Cons_Child)
 						cst.appendAll((IList)result.getAnnotation("concrete"));
 					else
 						cst.append(tok);
 				}
 				concrete = cst.done();
 				//concrete = ((IList) result.getAnnotation("concrete")).insert(concrete.get(0)).append(concrete.get(2));
 			}
 			else if(SymbolAdapter.isCf(rhs)) {
 				// the nested symbol within the cf(...)
 				IConstructor sym = SymbolAdapter.getSymbol(rhs);
 
 				if(ProductionAdapter.isList(prod)) {
 					Pair<IValue[], IList> t = visitChildren(TreeAdapter.getArgs(tree));
 					concrete = t.second;
 					result = seq(sort, t.first);
 				}
 				// Injection [cf] -> [cf], no cons
 				else if(lhs.length() == 1 && cons == null && SymbolAdapter.isCf((IConstructor) lhs.get(0))) {
 					if(!hasAbstract) {
 						return implode((IConstructor) TreeAdapter.getArgs(tree).get(0));
 					}
 					else {
 						// TODO: fix type of tree
 						return implode((IConstructor) TreeAdapter.getArgs(tree).get(0));
 					}
 				}
 				// Alternative: cf -> cf(alt(_,_))
 				else if(sym.getConstructorType() == Factory.Symbol_Alt) {
 					return implode((IConstructor) TreeAdapter.getArgs(tree).get(0));
 				}
 				// Option: . -> cf(opt())
 				else if(lhs.length() == 0 && sym.getConstructorType() == Factory.Symbol_Opt) {
 					concrete = null; // vf.list(Type_XaTree);
 					return seq(SymbolAdapter.getName(SymbolAdapter.getSymbol(sym)));
 				}
 				// Option: something -> cf(opt())
 				else if(sym.getConstructorType() == Factory.Symbol_Opt) {
 					concrete = null; // vf.list(child(0));
 					return seq(SymbolAdapter.getName(SymbolAdapter.getSymbol(sym)), 
 							implode((IConstructor) TreeAdapter.getArgs(tree).get(0)));
 				}
 				else if(ProductionAdapter.isContextFree(prod)) {
 					Pair<IValue[], IList> t = visitChildren(TreeAdapter.getArgs(tree));
 					concrete = t.second;
 					result = cons(cons != null ? cons : sort,
 							sort,
 							t.first);
 				}
 				else
 					result = null;
 			}
 
 		}
 		else if(nodeType == Factory.Tree_Amb) {
 			result = (IConstructor)TreeAdapter.getAlternatives(tree).iterator().next();
 		}
 		else {
 			throw new ImplementationError("TermImploder does not implement: " + nodeType);
 		}
 
 		if(result != null) {
 			result = result.setAnnotation("loc", TreeAdapter.getLocation(tree));
 			if(concrete != null)
 				result = result.setAnnotation("concrete", concrete);
 		}
 		else
 			return result;
 
 		return result;
 	}
 
 	private final static Pattern LAYOUT_PAT = Pattern.compile("^(\\s*)(\\S.*\\S)(\\s*)$", Pattern.DOTALL);
 
 	public static Pair<IValue[], IList> visitChildren(IList trees) throws FactTypeUseException {
 		ArrayList<IValue> ast = new ArrayList<IValue>();
 		IListWriter cst = vf.listWriter(Type_XaToken);
 		int i = 0;
 		for(IValue v : trees) {
 			assert v instanceof IConstructor;
 			IConstructor tree = (IConstructor)v;
 			if(TreeAdapter.isAmb(tree))
				throw new ImplementationError("Unexpected ambiguity");
 			if(TreeAdapter.isCfOptLayout(tree)) {
 				String chars = TreeAdapter.yield(tree);
 				if(!chars.equals("")) {
 					Matcher m = LAYOUT_PAT.matcher(chars);
 					if(m.matches()) {
 						splitSpaces(m.group(1), cst);
 						cst.append(comment(m.group(2)));
 						splitSpaces(m.group(3), cst);
 					}
 					else
 						splitSpaces(chars, cst);
 				}
 			}
 			else if(TreeAdapter.isLiteral(tree)) {
 				cst.append(token(TreeAdapter.yield(tree)));
 			}
 			else {
 				IValue child = implode(tree);
 				if(child != null) {
 					ast.add(child);
 					cst.append(child(i++));
 				}
 			}
 		}
 
 		return new Pair<IValue[], IList>(ast.toArray(new IValue[0]), cst.done());
 	}
 
 	private final static Pattern PAT_SPACE = Pattern.compile("^([^\r\n]*)([\r\n]+)(.*)$", Pattern.DOTALL);
 
 	private static void splitSpaces(String chars, IListWriter cst) {
 		Matcher m = PAT_SPACE.matcher(chars);
 		while(m.matches()) {
 			if(m.group(1).length() > 0)
 				cst.append(space(m.group(1)));
 			cst.append(space(m.group(2)));
 			chars = m.group(3);
 			m = PAT_SPACE.matcher(chars);
 		}
 
 		if(chars.length() > 0)
 			cst.append(space(chars));
 	}
 
 	public static String getSortName(IConstructor tree) {
 		IConstructor rhs = ProductionAdapter.getRhs(tree);
 
 		while (SymbolAdapter.isCf(rhs) || SymbolAdapter.isLex(rhs) || SymbolAdapter.isAnyList(rhs)
 				|| rhs.getConstructorType() == Factory.Symbol_Opt
 				|| rhs.getConstructorType() == Factory.Symbol_Alt) {
 			rhs = SymbolAdapter.getSymbol(rhs);
 		}
 
 		if (SymbolAdapter.isSort(rhs) || SymbolAdapter.isParameterizedSort(rhs)){
 			return SymbolAdapter.getName(rhs);
 		} 
 
 		return "";
 	}
 
 }
