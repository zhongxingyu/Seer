 package org.spoofax.interpreter.library.language;
 
 import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;
 
 import java.net.URI;
 import java.util.Set;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class LANG_index_get_all_in_file extends AbstractPrimitive {
 
 	private static String NAME = "LANG_index_get_all_in_file";
 	
 	private final SemanticIndexManager index;
 	
 	public LANG_index_get_all_in_file(SemanticIndexManager index) {
 		super(NAME, 0, 1);
 		this.index = index;
 	}
 
 	@Override
 	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (isTermString(tvars[0])) {
 			SemanticIndex idn = index.getCurrent();
 			URI file = idn.toFileURI(asJavaString(tvars[0]));
 			Set<SemanticIndexEntry> results = idn.getEntries(file);
 			env.setCurrent(SemanticIndexEntry.toTerms(idn.getFactory(), results));
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
