 package org.magnolialang.terms;
 
 import static org.magnolialang.terms.TermFactory.Cons_Child;
 import static org.magnolialang.terms.TermFactory.Cons_Comment;
 import static org.magnolialang.terms.TermFactory.Cons_Leaf;
 import static org.magnolialang.terms.TermFactory.Cons_Sep;
 import static org.magnolialang.terms.TermFactory.Cons_Seq;
 import static org.magnolialang.terms.TermFactory.Cons_Space;
 import static org.magnolialang.terms.TermFactory.Cons_Token;
 import static org.magnolialang.terms.TermFactory.Cons_Var;
 import static org.magnolialang.terms.TermFactory.Type_AST;
 import static org.magnolialang.terms.TermFactory.Type_XaToken;
 import static org.magnolialang.terms.TermFactory.child;
 import static org.magnolialang.terms.TermFactory.space;
 
 import java.util.Iterator;
 import java.util.regex.Pattern;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IInteger;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IListWriter;
 import org.eclipse.imp.pdb.facts.IMap;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.ITuple;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.visitors.NullVisitor;
 import org.eclipse.imp.pdb.facts.visitors.VisitorException;
 import org.magnolialang.errors.ImplementationError;
 import org.magnolialang.nullness.Nullable;
 import org.magnolialang.terms.skins.ILanguageSkin;
 import org.rascalmpl.values.ValueFactoryFactory;
 
 public final class TermAdapter {
 	private static IValueFactory	vf			= ValueFactoryFactory.getValueFactory();
 	private static Pattern			quoteChars	= Pattern.compile("([\\\"])");
 
 
 	@Nullable
 	public static IMap match(final IValue pattern, final IValue tree) {
 		if(pattern instanceof IConstructor && tree instanceof IConstructor)
 			return match((IConstructor) pattern, (IConstructor) tree, vf.map(Type_AST, Type_AST));
 		else
 			return null;
 	}
 
 
 	@Nullable
 	public static IMap match(final IConstructor pattern, final IConstructor tree) {
 		return match(pattern, tree, vf.map(Type_AST, Type_AST));
 	}
 
 
 	@Nullable
 	public static IMap match(final IConstructor pattern, final IConstructor tree, final IMap env) {
 		if(env == null || pattern == null || tree == null)
 			return null;
 		else if(pattern == tree) // NOPMD by anya on 1/5/12 3:24 AM
 			return env;
 		else if(isCons(pattern))
 			return matchCons(pattern, tree, env);
 		else if(isSeq(pattern))
 			return matchSeq(pattern, tree, env);
 		else if(isLeaf(pattern) && pattern.isEqual(tree))
 			return env;
 		else if(isVar(pattern))
 			if(env.containsKey(pattern))
 				return match((IConstructor) env.get(pattern), tree, env);
 			else
 				return env.put(pattern, tree);
 
 		return null;
 	}
 
 
 	@Nullable
 	public static IMap matchCons(final IConstructor pattern, final IConstructor tree, IMap env) {
 		if(!pattern.get("name").equals(tree.get("name")) || !pattern.get("sort").equals(tree.get("sort")))
 			return null;
 
 		final IList pargs = (IList) pattern.get("args");
 		final IList targs = (IList) tree.get("args");
 		if(pargs.length() != targs.length())
 			return null;
 
 		for(int i = 0; i < pargs.length(); i++)
 			env = match((IConstructor) pargs.get(i), (IConstructor) targs.get(i), env);
 
 		return env;
 	}
 
 
 	@Nullable
 	public static IMap matchSeq(final IConstructor pattern, final IConstructor tree, IMap env) {
 		if(!pattern.get("sort").equals(tree.get("sort")))
 			return null;
 
 		final IList pargs = (IList) pattern.get("args");
 		final IList targs = (IList) tree.get("args");
 		if(pargs.length() != targs.length())
 			return null;
 
 		for(int i = 0; i < pargs.length(); i++)
 			env = match((IConstructor) pargs.get(i), (IConstructor) targs.get(i), env);
 
 		return env;
 	}
 
 
 	public static boolean isCons(final IValue tree) {
 		return tree instanceof IConstructor && isCons((IConstructor) tree);
 	}
 
 
 	public static boolean isCons(final IConstructor tree) {
 		final Type constype = tree.getConstructorType();
 		return constype != Cons_Seq && constype != Cons_Leaf && constype != Cons_Var;
 	}
 
 
 	public static boolean isCons(final IValue tree, final String name) {
 		return tree instanceof IConstructor && isCons((IConstructor) tree, name);
 	}
 
 
 	public static boolean isCons(final IConstructor tree, final String name) {
 		return tree.getName().equals(name);
 	}
 
 
 	public static boolean isCons(final IValue tree, final String name, final int arity) {
 		return tree instanceof IConstructor && isCons((IConstructor) tree, name, arity);
 	}
 
 
 	public static boolean isCons(final IConstructor tree, final String name, final int arity) {
 		return tree.getName().equals(name) && tree.arity() == arity;
 	}
 
 
 	public static boolean isSeq(final IValue tree) {
 		return tree instanceof IConstructor && isSeq((IConstructor) tree);
 	}
 
 
 	public static boolean isSeq(final IConstructor tree) {
 		return tree.getConstructorType() == Cons_Seq;
 	}
 
 
 	public static boolean isLeaf(final IValue tree) {
 		return tree instanceof IConstructor && isLeaf((IConstructor) tree);
 	}
 
 
 	public static boolean isLeaf(final IConstructor tree) {
 		return tree.getConstructorType() == Cons_Leaf;
 	}
 
 
 	public static boolean isLeaf(final IValue tree, final String chars) {
 		return tree instanceof IConstructor && isLeaf((IConstructor) tree, chars);
 	}
 
 
 	public static boolean isLeaf(final IConstructor tree, final String chars) {
 		return tree.getConstructorType() == Cons_Leaf && ((IString) tree.get("strVal")).getValue().equals(chars);
 	}
 
 
 	public static boolean isVar(final IValue tree) {
 		return tree instanceof IConstructor && isVar((IConstructor) tree);
 	}
 
 
 	public static boolean isVar(final IConstructor tree) {
 		return tree.getConstructorType() == Cons_Var;
 	}
 
 
 	public static boolean isVar(final IValue tree, final String name) {
 		return tree instanceof IConstructor && isVar((IConstructor) tree, name);
 	}
 
 
 	public static boolean isVar(final IConstructor tree, final String name) {
 		return tree.getConstructorType() == Cons_Var && ((IString) tree.get("name")).getValue().equals(name);
 	}
 
 
 	public static Iterable<IConstructor> getChildren(final IConstructor tree) {
 		if(isSeq(tree))
 			return new IConstructorIterableWrapper((IList) tree.get("args"));
 		else
 			return new IConstructorIterableWrapper(tree.getChildren());
 
 	}
 
 
 	public static IConstructor getArg(final IConstructor tree, final int arg) {
 		if(isSeq(tree))
 			return (IConstructor) ((IList) tree.get("args")).get(arg);
 		else if(isLeaf(tree) || isVar(tree))
 			return tree;
 		else
 			return (IConstructor) tree.get(arg);
 	}
 
 
 	@Nullable
 	public static String getString(final IConstructor tree) {
 		if(isLeaf(tree))
 			return ((IString) tree.get("strVal")).getValue();
 		else
 			return null;
 	}
 
 
 	public static boolean hasChildren(final IConstructor tree) {
 		return isSeq(tree) || isCons(tree);
 	}
 
 
 	/**
 	 * The number of children of a constructor or list.
 	 * 
 	 * @param tree
 	 * @return Constructor arity, list length, or 0.
 	 */
 	public static int arity(final IConstructor tree) {
 		if(isSeq(tree))
 			return ((IList) tree.get("args")).length();
 		else if(isCons(tree))
 			return tree.arity();
 		else
 			return 0;
 	}
 
 
 	public static String getName(final IConstructor tree) {
 		if(isVar(tree))
 			return ((IString) tree.get("name")).getValue();
 		else
 			return tree.getName();
 	}
 
 
 	@SuppressWarnings("unused")
 	public static String getSort(final IConstructor tree) {
 		return null; // (IString) tree.get("sort");
 	}
 
 
 	/*
 	 * public static boolean isGround(IConstructor tree) { try { return
 	 * tree.accept(new NullVisitor<Boolean>() { public Boolean
 	 * visitConstructor(IConstructor c) { if(isLeaf(c)) return true; else
 	 * if(isVar(c)) return false; for(IValue child : (IList) c.get("args"))
 	 * if(!isGround(child)) return false; return true; }}); } catch
 	 * (VisitorException e) { return false; } }
 	 * 
 	 * public static IList vars(IConstructor tree) {
 	 * 
 	 * final IListWriter lw = vf.listWriter(Type_AST);
 	 * 
 	 * try { tree.accept(new IdentityVisitor() { public IValue
 	 * visitConstructor(IConstructor c) throws VisitorException { if(isVar(c))
 	 * lw.append(c); else if(isCons(c) || isSeq(c)) for(IValue child : (IList)
 	 * c.get("args")) child.accept(this); return c; }}); } catch
 	 * (VisitorException e) { throw new ImplementationError("Visitor error", e);
 	 * }
 	 * 
 	 * return lw.done(); }
 	 */
 	public static String yield(final IValue tree) {
 		if(!tree.getType().isSubtypeOf(Type_AST))
 			return tree.toString();
 		try {
 			return tree.accept(new NullVisitor<String>() {
 				@Override
 				public String visitConstructor(final IConstructor c) throws VisitorException {
 					final IList concrete = (IList) c.getAnnotation("concrete");
 					final StringBuilder result = new StringBuilder(1024);
 					if(concrete == null || concrete.length() == 0) {
 						if(isLeaf(c))
 							return getString(c);
 						else if(isVar(c))
 							return "<" + getName(c) + ">";
 						else {
 							for(final IConstructor child : getChildren(c))
 								result.append(child.accept(this));
 							return result.toString();
 						}
 					}
 
 					for(final IValue token : concrete) {
 						final Type type = ((IConstructor) token).getConstructorType();
 						if(type.equivalent(Cons_Token) || type.equivalent(Cons_Space) || type.equivalent(Cons_Comment))
 							result.append(((IString) ((IConstructor) token).get("chars")).getValue());
 						else {
 							final int index = ((IInteger) ((IConstructor) token).get("index")).intValue();
 							result.append(getArg(c, index).accept(this));
 						}
 					}
 					return result.toString();
 				}
 			});
 		}
 		catch(final VisitorException e) {
 			e.printStackTrace();
 			return "";
 		}
 	}
 
 
 	public static String yield(final IValue tree, final ILanguageSkin skin, final boolean fallback) {
 		return yield(tree, skin, fallback, "");
 	}
 
 
 	public static String yield(final IValue tree, final ILanguageSkin skin, final boolean fallback, String nesting) {
 		if(tree instanceof IConstructor) {
 			final IConstructor c = (IConstructor) tree;
 			IList concrete = null;
 
 			if(isCons(c)) {
 				concrete = skin.getConcrete(getName(c), arity(c), null);
 				if(concrete != null && skin.isVertical(getName(c), arity(c), null)) {
 					concrete = concrete.insert(space("\n" + nesting)).append(space("\n" + nesting));
 					nesting = nesting + "\t";
 				}
				//else if(concrete == null)
				//	System.out.println(getName(c));
 			}
 			else if(isSeq(c)) {
 				concrete = getConcreteForList(arity(c), skin.getListSep(getSort(c), null));
 			}
 
 			if(concrete == null && fallback)
 				concrete = (IList) c.getAnnotation("concrete");
 
 			if(concrete == null || concrete.length() == 0)
 				if(isLeaf(c))
 					return ((IString) c.get("strVal")).getValue();
 				else if(isVar(c))
 					return "<" + ((IString) c.get("name")).getValue() + ">";
 				else {
 					final StringBuilder result = new StringBuilder(1024);
 					for(final IConstructor child : getChildren(c))
 						result.append(yield(child, skin, fallback, nesting));
 					return result.toString();
 				}
 
 			return formatConcrete(skin, fallback, nesting, c, concrete).toString();
 		}
 		else if(tree instanceof IList) {
 			final StringBuilder result = new StringBuilder(1024);
 			for(final IValue child : (IList) tree)
 				result.append(yield(child, skin, fallback, nesting));
 			return result.toString();
 		}
 		else if(tree instanceof IString) {
 			return ((IString) tree).getValue();
 		}
 		else
 			throw new ImplementationError("Yield not valid on type " + tree.getType());
 
 	}
 
 
 	private static StringBuilder formatConcrete(final ILanguageSkin skin, final boolean fallback, String nesting, final IConstructor tree, IList concrete) {
 		StringBuilder result = new StringBuilder();
 		for(IValue token : concrete) {
 			Type type = ((IConstructor) token).getConstructorType();
 			if(type == Cons_Token || type == Cons_Comment)
 				result.append(((IString) ((IConstructor) token).get("chars")).getValue());
 			else if(type == Cons_Space)
 				result.append(((IString) ((IConstructor) token).get("chars")).getValue());
 			else if(type == Cons_Child) {
 				int index = ((IInteger) ((IConstructor) token).get("index")).intValue();
 				result.append(yield(getArg(tree, index), skin, fallback, nesting));
 			}
 			else if(type == Cons_Sep) {
 				IConstructor tok = (IConstructor) ((IConstructor) token).get("tok");
 				IValue sep = ((IConstructor) token).get("separator");
 				if(tok.getConstructorType() == Cons_Child) {
 					int index = ((IInteger) tok.get("index")).intValue();
 					IConstructor arg = getArg(tree, index);
 					if(isSeq(arg)) {
 						result.append(formatConcrete(skin, fallback, nesting, arg, getConcreteForList(arity(arg), sep)));
 					}
 					else
 						throw new ImplementationError("Separated list was not a list: " + arg);
 				}
 			}
 		}
 		return result;
 	}
 
 
 	private static IList getConcreteForList(int arity, IValue sep) {
 		if(sep == null)
 			return null;
 		else {
 			final IListWriter lw = vf.listWriter(Type_XaToken);
 			for(int i = 0; i < arity; i++) {
 				if(i > 0) {
 					if(sep instanceof IList)
 						for(IValue s : (IList) sep)
 							lw.append(s);
 					else
 						lw.append(sep);
 				}
 				lw.append(child(i));
 			}
 			return lw.done();
 		}
 	}
 
 
 	public static String yieldTerm(IValue tree, boolean withAnnos) {
 		StringBuilder result = new StringBuilder(1024);
 		yieldTerm(tree, withAnnos, result);
 		return result.toString();
 	}
 
 
 	private static void yieldTerm(IValue tree, boolean withAnnos, StringBuilder output) {
 		if(tree instanceof IConstructor) {
 			final IConstructor c = (IConstructor) tree;
 
 			final Type constype = c.getConstructorType();
 
 			if(constype == Cons_Seq) {
 				yieldTerm(c.get("args"), withAnnos, output);
 			}
 			else if(constype == Cons_Leaf) {
 				output.append('\"');
 				output.append(quoteChars.matcher(((IString) c.get("strVal")).getValue()).replaceAll("\\\\$1"));
 				output.append('\"');
 			}
 			else if(constype == Cons_Var) {
 				output.append(((IString) c.get("name")).getValue());
 			}
 			else {
 				output.append(c.getName());
 				output.append('(');
 				yieldTermList(c, withAnnos, output);
 				output.append(')');
 			}
 
 		}
 		else if(tree instanceof IList) {
 			output.append('[');
 			yieldTermList((IList) tree, withAnnos, output);
 			output.append(']');
 		}
 		else if(tree instanceof ISet) {
 			output.append('{');
 			yieldTermList((ISet) tree, withAnnos, output);
 			output.append('}');
 		}
 		else if(tree instanceof ITuple) {
 			output.append('<');
 			yieldTermList((ITuple) tree, withAnnos, output);
 			output.append('>');
 		}
 		else {
 			output.append(tree.toString());
 		}
 	}
 
 
 	private static void yieldTermList(Iterable<IValue> list, boolean withAnnos, StringBuilder output) {
 		boolean first = true;
 
 		for(final IValue child : list) {
 			if(!first)
 				output.append(", ");
 			yieldTerm(child, withAnnos, output);
 			first = false;
 		}
 	}
 
 
 	private TermAdapter() {
 
 	}
 
 
 	@Nullable
 	public static ISourceLocation getLocation(IValue tree) {
 		if(tree instanceof IConstructor)
 			return (ISourceLocation) ((IConstructor) tree).getAnnotation("loc");
 		else
 			return null;
 	}
 
 }
 
 class IConstructorIterableWrapper implements Iterable<IConstructor> {
 	private final Iterable<IValue>	iterable;
 
 
 	public IConstructorIterableWrapper(final Iterable<IValue> iterable) {
 		this.iterable = iterable;
 	}
 
 
 	@Override
 	public Iterator<IConstructor> iterator() {
 		return new IConstructorIteratorWrapper(iterable);
 	}
 }
 
 class IConstructorIteratorWrapper implements Iterator<IConstructor> {
 	private final Iterator<IValue>	iterator;
 
 
 	public IConstructorIteratorWrapper(final Iterable<IValue> iterable) {
 		iterator = iterable.iterator();
 	}
 
 
 	@Override
 	public boolean hasNext() {
 		return iterator.hasNext();
 	}
 
 
 	@Override
 	public IConstructor next() {
 		try {
 			return (IConstructor) iterator.next();
 		}
 		catch(final ClassCastException e) {
 			return null;
 			// throw e;
 		}
 	}
 
 
 	@Override
 	public void remove() {
 		iterator.remove();
 	}
 }
