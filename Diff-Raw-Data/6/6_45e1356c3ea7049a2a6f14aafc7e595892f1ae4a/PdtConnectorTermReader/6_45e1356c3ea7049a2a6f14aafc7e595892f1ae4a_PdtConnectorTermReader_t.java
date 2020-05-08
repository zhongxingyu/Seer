 package org.jpc.salt.pdtconnector;
 
 import static org.jpc.engine.prolog.PrologConstants.EMPTY_LIST_SYMBOL;
 
 import java.util.Iterator;
 
 import org.cs3.prolog.cterm.CAtom;
 import org.cs3.prolog.cterm.CCompound;
 import org.cs3.prolog.cterm.CFloat;
 import org.cs3.prolog.cterm.CInteger;
 import org.cs3.prolog.cterm.CNil;
 import org.cs3.prolog.cterm.CTerm;
 import org.cs3.prolog.cterm.CVariable;
 import org.jpc.salt.TermContentHandler;
 import org.jpc.salt.TermReader;
 
 public class PdtConnectorTermReader extends TermReader {
 	
 	private CTerm pdtTerm;
 	
 	public PdtConnectorTermReader(CTerm pdtTerm, TermContentHandler contentHandler) {
 		super(contentHandler);
 		this.pdtTerm = pdtTerm;
 	}
 
 	@Override
 	public void read() {
 		read(pdtTerm);
 	}
 	
 	private void read(CTerm term) {
 		if(term instanceof CInteger) {
 			CInteger pdtInteger = (CInteger) term;
 			getContentHandler().startIntegerTerm(pdtInteger.getLongValue());
 		} else if(term instanceof CFloat) {
 			CFloat pdtFloat = (CFloat) term;
 			getContentHandler().startFloatTerm(pdtFloat.getDoubleValue());
 		} else if (term instanceof CVariable) {
 			CVariable pdtVariable = (CVariable) term;
 			getContentHandler().startVariable(pdtVariable.getVariableName());
 		} else if (term instanceof CAtom) {
			try {
				getContentHandler().startAtom(term.getFunctorValue()); //this is giving problems in certain cases (for example with the operator '/\')
			} catch(Exception e) { //temporary solution, getFunctorValue() should be fixed
				getContentHandler().startAtom(term.toString());
			}
 		} else if (term instanceof CNil) {
 			getContentHandler().startAtom(EMPTY_LIST_SYMBOL);
 		} else if(term instanceof CCompound) {
 			CCompound pdtCompound = (CCompound) term;
 			getContentHandler().startCompound();
 			getContentHandler().startAtom(pdtCompound.getFunctorValue());
 			Iterator<CTerm> it = pdtCompound.iterator();
 			while(it.hasNext()) {
 				read(it.next());
 			}
 			getContentHandler().endCompound();
 		} else
 			throw new RuntimeException("Unrecognized PDT term: " + term);
 	}
 	
 }
