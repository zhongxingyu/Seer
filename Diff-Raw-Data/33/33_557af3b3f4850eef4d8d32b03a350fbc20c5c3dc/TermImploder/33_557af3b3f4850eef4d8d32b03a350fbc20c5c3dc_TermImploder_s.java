 package org.magnolialang.terms;
 
 import static org.magnolialang.terms.TermFactory.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.imp.pdb.facts.*;
 import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.utils.Pair;
 import org.rascalmpl.values.uptr.Factory;
 import org.rascalmpl.values.uptr.ProductionAdapter;
 import org.rascalmpl.values.uptr.SymbolAdapter;
 import org.rascalmpl.values.uptr.TreeAdapter;
 
 public final class TermImploder {
 
 	/**
 	 * Implode an AsFix tree to an XaTree.
 	 * 
 	 * @param tree
 	 *            A tree in AsFix format
 	 * @return An imploded XaTree
 	 */
 	public static IConstructor implodeTree(final IConstructor tree) {
 		if(tree.getConstructorType() == Factory.Tree_Appl) {
 			IConstructor prod = TreeAdapter.getProduction(tree);
 			IList args = TreeAdapter.getArgs(tree);
 			if(ProductionAdapter.isLexical(prod) && args.length() == 3) {
 				return implode((IConstructor) args.get(1));
 			}
 		}
 		return implode(tree);
 	}
 
 	public static final Type Attr = tf.abstractDataType(ts, "Attr");
 	public static final Type Attr_Abstract = tf.constructor(ts, Attr,
 			"selectable");
 
 	public static IConstructor implode(final IConstructor tree) {
 
 		IConstructor result = null;
 		IList concrete = null;
 
 		final Type nodeType = tree.getConstructorType();
 		if(nodeType == Factory.Tree_Appl) {
 			final IConstructor prod = TreeAdapter.getProduction(tree);
 			IList syms = null;
 			if(!ProductionAdapter.isList(prod))
 				syms = ProductionAdapter.getSymbols(prod);
 			final IConstructor type = ProductionAdapter.getType(prod);
 			final ISet attrs = ProductionAdapter.getAttributes(prod);
 			final Map<String, IValue> newAttrs = new HashMap<String, IValue>();
 			boolean hasAbstract = false;
 			String cons = ProductionAdapter.getConstructorName(prod);
 			final String sort = getSortName(prod);
 
 			for(final IValue attr : attrs)
 				if(attr.getType().isAbstractDataType()
 						&& ((IConstructor) attr).getConstructorType() == Factory.Attr_Tag) {
 					final IValue value = ((IConstructor) attr).get("tag");
 					if(value.getType().isNodeType()) {
 						INode node = (INode) value;
 						if(node.getName().equals("abstract"))
 							hasAbstract = true;
 						else if(node.arity() == 0)
 							newAttrs.put(node.getName(), vf.bool(true));
 						else if(node.arity() == 1)
 							newAttrs.put(node.getName(), node.get(0));
 						else
 							newAttrs.put(node.getName(), node);
 					}
 				}
 
 			// String name = SymbolAdapter.getName(rhs);
 			// Token: [lex] -> cf
 			if(ProductionAdapter.isLexical(prod)
 					|| ProductionAdapter.isKeyword(prod)) {
 				final String str = TreeAdapter.yield(tree);
 				if(cons != null)
 					result = cons(cons, sort, check(leaf(str)));
 				else
 					return check(leaf(str));
 			}
			/*
			 * else if(sort.equals("<START>")) { final Pair<IValue[], IList> t =
			 * visitChildren(TreeAdapter .getArgs(tree)); concrete = t.second;
			 * cons = cons != null ? cons : sort; result = (IConstructor)
			 * t.first[0]; final IListWriter cst = vf.listWriter(Type_XaToken);
			 * for(final IValue tok : concrete) if(((IConstructor)
			 * tok).getConstructorType() == Cons_Child) cst.appendAll((IList)
			 * result.getAnnotation("concrete")); else cst.append(tok); concrete
			 * = cst.done(); // concrete = ((IList) //
			 * result.getAnnotation("concrete"
			 * )).insert(concrete.get(0)).append(concrete.get(2)); }
			 */
 			else if(ProductionAdapter.isList(prod)) {
 				final Pair<IValue[], IList> t = visitChildren(TreeAdapter
 						.getArgs(tree));
 				concrete = t.second;
 				result = seq(t.first);
 			}
 			// Injection [cf] -> [cf], no cons
 			else if(syms.length() == 1 && cons == null) {
 				if(!hasAbstract)
 					return check(implode((IConstructor) TreeAdapter.getArgs(
 							tree).get(0)));
 				else
 					// TODO: fix type of tree
 					return check(implode((IConstructor) TreeAdapter.getArgs(
 							tree).get(0)));
 			}
 			// Alternative: cf -> cf(alt(_,_))
 			else if(type.getConstructorType() == Factory.Symbol_Alt)
 				return check(implode((IConstructor) TreeAdapter.getArgs(tree)
 						.get(0)));
 			else if(syms.length() == 0
 					&& type.getConstructorType() == Factory.Symbol_Opt) {
 				concrete = null; // vf.list(Type_XaTree);
 				return check(seq());
 			}
 			// Option: something -> cf(opt())
 			else if(type.getConstructorType() == Factory.Symbol_Opt) {
 				concrete = null; // vf.list(child(0));
 				return check(seq(implode((IConstructor) TreeAdapter.getArgs(
 						tree).get(0))));
 			}
 			else {
 				if(ProductionAdapter.isRegular(tree))
 					System.out.println("Regular");
 				final Pair<IValue[], IList> t = visitChildren(TreeAdapter
 						.getArgs(tree));
 				concrete = t.second;
 				result = cons(cons != null ? cons : sort, sort, t.first);
 			}
 
 			if(result != null)
 				result = result.setAnnotations(newAttrs);
 
 		}
 		else if(nodeType == Factory.Tree_Amb) {
 			result = implode((IConstructor) TreeAdapter.getAlternatives(tree)
 					.iterator().next());
 		}
 		else
 			return null;
 		// throw new ImplementationError("TermImploder does not implement: "
 		// + nodeType);
 
 		if(result != null) {
 			result = result.setAnnotation("loc", TreeAdapter.getLocation(tree));
 			if(concrete != null)
 				result = result.setAnnotation("concrete", concrete);
 		}
 		else
 			return check(result);
 
 		return check(result);
 	}
 
 	private static final Pattern LAYOUT_PAT = Pattern.compile(
 			"^(\\s*)(\\S.*\\S)(\\s*)$", Pattern.DOTALL);
 
 	public static Pair<IValue[], IList> visitChildren(final IList trees)
 			throws FactTypeUseException {
 		final ArrayList<IValue> ast = new ArrayList<IValue>();
 		final IListWriter cst = vf.listWriter(Type_XaToken);
 		int i = 0;
 		for(final IValue v : trees) {
 			assert v instanceof IConstructor;
 			IConstructor tree = (IConstructor) v;
 			if(TreeAdapter.isAmb(tree))
 				tree = (IConstructor) TreeAdapter.getAlternatives(tree)
 						.iterator().next();
 			if(TreeAdapter.isLayout(tree)) {
 				final String chars = TreeAdapter.yield(tree);
 				if(!chars.equals("")) {
 					final Matcher m = LAYOUT_PAT.matcher(chars);
 					if(m.matches()) {
 						splitSpaces(m.group(1), cst);
 						cst.append(comment(m.group(2)));
 						splitSpaces(m.group(3), cst);
 					}
 					else
 						splitSpaces(chars, cst);
 				}
 			}
 			else if(TreeAdapter.isLiteral(tree))
 				cst.append(token(TreeAdapter.yield(tree)));
 			else {
 				final IValue child = implode(tree);
 				if(child != null) {
 					ast.add(child);
 					cst.append(child(i++));
 				}
 			}
 		}
 
 		return new Pair<IValue[], IList>(ast.toArray(new IValue[0]), cst.done());
 	}
 
 	private static final Pattern PAT_SPACE = Pattern.compile(
 			"^([^\r\n]*)([\r\n]+)(.*)$", Pattern.DOTALL);
 
 	private static void splitSpaces(String chars, final IListWriter cst) {
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
 
 	public static String getSortName(final IConstructor tree) {
 		IConstructor type = ProductionAdapter.getType(tree);
 
 		while(SymbolAdapter.isAnyList(type)
 				|| type.getConstructorType() == Factory.Symbol_Opt
 				|| type.getConstructorType() == Factory.Symbol_Alt)
 			type = SymbolAdapter.getSymbol(type);
 
 		if(SymbolAdapter.isSort(type)
 				|| SymbolAdapter.isParameterizedSort(type))
 			return SymbolAdapter.getName(type);
 
 		return "";
 	}
 
 	private TermImploder() {
 
 	}
 
 	private static IConstructor check(IConstructor ret) {
 		// if(!ret.getType().isSubtypeOf(Type_AST))
 		// throw new ImplementationError("Bad AST type: " + ret.getType());
 		return ret;
 	}
 }
