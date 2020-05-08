 package org.strategoxt.imp.testing.strategies;
 
 import static org.spoofax.interpreter.core.Tools.isTermAppl;
 import static org.spoofax.terms.Term.*;
 
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.strategoxt.HybridInterpreter;
 import org.strategoxt.imp.runtime.services.InputTermBuilder;
 import org.strategoxt.imp.runtime.services.StrategoReferenceResolver;
 import org.strategoxt.lang.Context;
 import org.strategoxt.lang.Strategy;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class get_service_input_term_0_1 extends Strategy {
 
 	public static get_service_input_term_0_1 instance = new get_service_input_term_0_1();
 
 	@Override
 	public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm analyzedAst) {
 		// TODO: adapt to latest strategy of StrategoReferenceResolver?
 		if (isTermAppl(analyzedAst) && ((IStrategoAppl) analyzedAst).getName().equals("None"))
 			analyzedAst = null;
		if ("COMPLETION" != tryGetName(current))
 			current = InputTermBuilder.getMatchingAncestor(current, StrategoReferenceResolver.ALLOW_MULTI_CHILD_PARENT);
 		HybridInterpreter runtime = HybridInterpreter.getInterpreter(context);
 		InputTermBuilder inputBuilder = new InputTermBuilder(runtime, analyzedAst);
		return inputBuilder.makeInputTerm(current, true);
 	}
 
 }
