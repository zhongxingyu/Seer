 package org.spoofax.interpreter.library.language;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class LANG_index_end_transaction extends AbstractPrimitive {
 
 	private static String NAME = "LANG_index_end_transaction";
 	
 	private final SemanticIndexManager index;
 	
 	public LANG_index_end_transaction(SemanticIndexManager index) {
		super(NAME, 0, 2);
 		this.index = index;
 	}
 	
 	@Override
 	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
 			throws InterpreterException {
 		index.endTransaction();
 		return true;
 	}
 
 }
