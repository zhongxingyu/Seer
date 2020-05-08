 package org.magnolialang.terms;
 
 import java.io.PrintStream;
 import java.util.Iterator;
 
 import org.eclipse.imp.pdb.facts.*;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 import org.magnolialang.terms.skins.CxxSkin;
 import org.magnolialang.terms.skins.MagnoliaSkin;
 import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
 
 public class Terms {
 	@SuppressWarnings("unused")
 	private static final TypeFactory types = TypeFactory.getInstance();
 	private final IValueFactory vf;
 
 	public Terms(IValueFactory values) {
 		super();
 
 		this.vf = values;
 	}
 
 	public IConstructor implode(IConstructor tree) {
 		return TermImploder.implodeTree(tree);
 	}
 
 	public IString unparse(IConstructor tree, IString skin, IBool fallback) {
 		// System.err.println(TermAdapter.yieldTerm(tree, false));
 		if(skin.getValue().equals(""))
 			return vf.string(TermAdapter.yield(tree));
		else if(skin.getValue().equals("Magnolia"))
 			return vf.string(TermAdapter.yield(tree, new MagnoliaSkin(),
 					fallback.getValue()));
 		else if(skin.getValue().equals("Cxx"))
			return vf.string(TermAdapter.yield(tree, new CxxSkin(), fallback
					.getValue()));
 		else
 			throw RuntimeExceptionFactory.illegalArgument(skin, null, null);
 	}
 
 	public IString yieldTerm(IValue tree, IBool withAnnos) {
 		return vf.string(TermAdapter.yieldTerm(tree, withAnnos.getValue()));
 	}
 
 	public void termPrintln(IList l) {
 		PrintStream currentOutStream = System.out;
 
 		synchronized(currentOutStream) {
 			try {
 				Iterator<IValue> valueIterator = l.iterator();
 				while(valueIterator.hasNext()) {
 					IValue arg = valueIterator.next();
 
 					if(arg.getType().isStringType()) {
 						currentOutStream.print(((IString) arg).getValue()
 								.toString());
 					}
 					else {
 						currentOutStream.print(TermAdapter
 								.yieldTerm(arg, false));
 					}
 				}
 				currentOutStream.println();
 			}
 			finally {
 				currentOutStream.flush();
 			}
 		}
 	}
 }
