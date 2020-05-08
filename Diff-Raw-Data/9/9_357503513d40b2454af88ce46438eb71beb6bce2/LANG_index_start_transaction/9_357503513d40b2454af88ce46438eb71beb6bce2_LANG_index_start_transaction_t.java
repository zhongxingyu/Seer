 package org.spoofax.interpreter.library.language;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.library.ssl.SSLLibrary;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class LANG_index_start_transaction extends AbstractPrimitive {
 
 	private static String NAME = "LANG_index_start_transaction";
 	
 	private final SemanticIndexManager index;
 	
 	public LANG_index_start_transaction(SemanticIndexManager index) {
		super(NAME, 0, 0);
 		this.index = index;
 	}
 
 	@Override
 	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
 			throws InterpreterException {
 		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
 		index.startTransaction(env.getFactory(), agent);
 		return true;
 	}
 
 }
