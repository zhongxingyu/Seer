 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-04-14
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam;
 
 import eu.serscis.sam.node.ANeqBinop;
 import eu.serscis.sam.node.AEqBinop;
 import eu.serscis.sam.node.PBinop;
 import eu.serscis.sam.node.ANegativeLiteral;
 import eu.serscis.sam.node.APositiveLiteral;
 import eu.serscis.sam.node.PLiteral;
 import eu.serscis.sam.node.ALiteralTail;
 import eu.serscis.sam.node.PLiteralTail;
 import eu.serscis.sam.node.ALiterals;
 import eu.serscis.sam.node.ATermTail;
 import eu.serscis.sam.node.PTermTail;
 import eu.serscis.sam.node.ABuiltinAtom;
 import eu.serscis.sam.node.ANullaryAtom;
 import eu.serscis.sam.node.ANormalAtom;
 import eu.serscis.sam.node.AIntTerm;
 import eu.serscis.sam.node.ABoolTerm;
 import eu.serscis.sam.node.AVarTerm;
 import eu.serscis.sam.node.AStringTerm;
 import org.deri.iris.api.basics.IAtom;
 import eu.serscis.sam.node.PAtom;
 import eu.serscis.sam.node.ATerms;
 import eu.serscis.sam.node.PTerm;
 import eu.serscis.sam.parser.ParserException;
 import eu.serscis.sam.node.Token;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.deri.iris.Configuration;
 import org.deri.iris.KnowledgeBaseFactory;
 import org.deri.iris.api.IKnowledgeBase;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.basics.IRule;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.storage.IRelation;
 import org.deri.iris.compiler.BuiltinRegister;
 import static org.deri.iris.factory.Factory.*;
 
 public class Model {
 	final Configuration configuration;
 	final List<IRule> rules = new LinkedList<IRule>();
 	final Map<IPredicate,IRelation> facts = new HashMap<IPredicate,IRelation>();
 	public final Map<IPredicate,ITuple> declared = new HashMap<IPredicate,ITuple>();
 	private final static BuiltinRegister builtinRegister = new BuiltinRegister();
 
 	public Model(Configuration configuration) {
 		this.configuration = configuration;
 	}
 
 	public Model(Model source) {
 		this.configuration = source.configuration;
 		this.rules.addAll(source.rules);
 		this.facts.putAll(source.facts);
 		this.declared.putAll(source.declared);
 	}
 
 	public IRelation getRelation(IPredicate pred) {
 		IRelation rel = facts.get(pred);
 		if (rel == null) {
 			rel = configuration.relationFactory.createRelation();
 			facts.put(pred, rel);
 		}
 		return rel;
 	}
 	
 	public IKnowledgeBase createKnowledgeBase() throws Exception {
 		Map<IPredicate,IRelation> workingFacts = new HashMap<IPredicate,IRelation>();
 
 		/* Make a copy of the initial facts (otherwise the debugger doesn't work). */
 		for (Map.Entry<IPredicate,IRelation> entry: facts.entrySet()) {
 			IRelation copy = configuration.relationFactory.createRelation();
 			copy.addAll(entry.getValue());
 			workingFacts.put(entry.getKey(), copy);
 		}
 
 		return KnowledgeBaseFactory.createKnowledgeBase(workingFacts, rules, configuration);
 	}
 
 	public void requireDeclared(Token tok, IPredicate pred) throws ParserException {
 		if ("error".equals(pred.getPredicateSymbol())) {
 			return;
 		}
 
 		if (!declared.containsKey(pred)) {
 			throw new ParserException(tok, "Predicate not declared: " + pred + "/" + pred.getArity());
 		}
 	}
 
 	public ITuple getDefinition(IPredicate pred) {
 		return declared.get(pred);
 	}
 
 	public void declare(Token tok, IPredicate pred, ITuple terms) throws ParserException {
 		if (declared.containsKey(pred)) {
 			throw new ParserException(tok, "Predicate already declared: " + pred);
 		}
 
 		//System.out.println("Declare " + pred + "/" + pred.getArity());
 		declared.put(pred, terms);
 	}
 
 	public ITerm parseTerm(PTerm parsed, TermProcessor termFn) throws ParserException {
 		if (termFn != null) {
 			ITerm term = termFn.process(parsed);
 			if (term != null) {
 				return term;
 			}
 		}
 
 		if (parsed instanceof AStringTerm) {
 			String str = Constants.getString(((AStringTerm) parsed).getStringLiteral());
 			return TERM.createString(str);
 		} else if (parsed instanceof AVarTerm) {
 			String name = ((AVarTerm) parsed).getName().getText();
 			return TERM.createVariable(name);
 		} else if (parsed instanceof ABoolTerm) {
 			boolean val = Boolean.valueOf(((ABoolTerm) parsed).getBool().getText());
 			return CONCRETE.createBoolean(val);
 		} else if (parsed instanceof AIntTerm) {
 			int val = Integer.valueOf(((AIntTerm) parsed).getNumber().getText());
 			return CONCRETE.createInt(val);
 		} else {
			throw new RuntimeException("Unknown term type:" + parsed);
 		}
 	}
 
 	private ITuple parseTerms(ATerms parsed, TermProcessor termFn) throws ParserException {
 		List<ITerm> terms = new LinkedList<ITerm>();
 
 		terms.add(parseTerm(parsed.getTerm(), termFn));
 
 		for (PTermTail tail : parsed.getTermTail()) {
 			terms.add(parseTerm(((ATermTail) tail).getTerm(), termFn));
 		}
 		return BASIC.createTuple(terms);
 	}
 
 	public IAtom parseAtom(PAtom parsed) throws ParserException {
 		return parseAtom(parsed, false, null);
 	}
 
 	public IAtom parseAtom(PAtom parsed, boolean declaration, TermProcessor termFn) throws ParserException {
 		if (parsed instanceof ANormalAtom) {
 			ANormalAtom atom = (ANormalAtom) parsed;
 			ITuple terms = parseTerms((ATerms) atom.getTerms(), termFn);
 
 			String name = atom.getName().getText();
 
 			Class<?> builtinClass = builtinRegister.getBuiltinClass(name);
 			if (builtinClass == null) {
 				IPredicate predicate = BASIC.createPredicate(name, terms.size());
 
 				if (declaration) {
 					declare(atom.getName(), predicate, terms);
 				} else {
 					requireDeclared(atom.getName(), predicate);
 				}
 
 				return BASIC.createAtom(predicate, terms);
 			} else {
 				try {
 					return (IAtom) builtinClass.getConstructor(ITerm[].class).
 						newInstance(new Object[] {terms.toArray(new ITerm[terms.size()])});
 				} catch (Exception ex) {
 					throw new RuntimeException(ex);
 				}
 			}
 		} else if (parsed instanceof ANullaryAtom) {
 			ANullaryAtom atom = (ANullaryAtom) parsed;
 
 			String name = atom.getName().getText();
 			IPredicate predicate = BASIC.createPredicate(name, 0);
 
 			if (declaration) {
 				declare(atom.getName(), predicate, BASIC.createTuple());
 			} else {
 				requireDeclared(atom.getName(), predicate);
 			}
 
 			return BASIC.createAtom(predicate, BASIC.createTuple());
 		} else {
 			ABuiltinAtom atom = (ABuiltinAtom) parsed;
 			PBinop op = atom.getBinop();
 
 			if (op instanceof AEqBinop) {
 				return BUILTIN.createEqual(parseTerm(atom.getLhs(), termFn), parseTerm(atom.getRhs(), termFn));
 			} else if (op instanceof ANeqBinop) {
 				return BUILTIN.createUnequal(parseTerm(atom.getLhs(), termFn), parseTerm(atom.getRhs(), termFn));
 			} else {
 				throw new RuntimeException("Unknown binary op " + op);
 			}
 		}
 	}
 
 	private ILiteral parseLiteral(PLiteral parsed, TermProcessor termFn) throws ParserException {
 		if (parsed instanceof APositiveLiteral) {
 			return BASIC.createLiteral(true, parseAtom(((APositiveLiteral) parsed).getAtom(), false, termFn));
 		} else {
 			return BASIC.createLiteral(false, parseAtom(((ANegativeLiteral) parsed).getAtom(), false, termFn));
 		}
 	}
 
 	public List<ILiteral> parseLiterals(ALiterals parsed, TermProcessor termFn) throws ParserException {
 		List<ILiteral> literals = new LinkedList<ILiteral>();
 
 		literals.add(parseLiteral(parsed.getLiteral(), termFn));
 
 		for (PLiteralTail tail : parsed.getLiteralTail()) {
 			literals.add(parseLiteral(((ALiteralTail) tail).getLiteral(), termFn));
 		}
 		return literals;
 	}
 
 }
