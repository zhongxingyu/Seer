 package uk.ac.bath.cs.agents.instal.asp;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import uk.ac.bath.cs.agents.instal.Condition;
 import uk.ac.bath.cs.agents.instal.CreationEvent;
 import uk.ac.bath.cs.agents.instal.DissolutionEvent;
 import uk.ac.bath.cs.agents.instal.Domain;
 import uk.ac.bath.cs.agents.instal.Event;
 import uk.ac.bath.cs.agents.instal.Fluent;
 import uk.ac.bath.cs.agents.instal.Generates;
 import uk.ac.bath.cs.agents.instal.InitiallyFluent;
 import uk.ac.bath.cs.agents.instal.Initiates;
 import uk.ac.bath.cs.agents.instal.Institution;
 import uk.ac.bath.cs.agents.instal.NoninertialFluent;
 import uk.ac.bath.cs.agents.instal.Obligation;
 import uk.ac.bath.cs.agents.instal.Parameters;
 import uk.ac.bath.cs.agents.instal.Rule;
 import uk.ac.bath.cs.agents.instal.Terminates;
 import uk.ac.bath.cs.agents.instal.Type;
 
 public class AnsProlog extends InstalASPTranslator {
 
 	public AnsProlog(Institution instalSpec, Domain domain) {
 		super(instalSpec, domain);
 	}
 	
 	public AnsProlog generate() {
 	    this._addDivider();
         this._addComment("Engine: AnsProlog, Nick Jones <nj210@bath.ac.uk>");
         this._addDivider();
         return (AnsProlog) super.generate();
 	}
 	
 	protected Atom _generateInstitutionName(String name) {
 	    return new Blank(String.format("inst(%s).", name));
 	}
 	
 	protected Atom[] _generateInitiallyFluents(InitiallyFluent[] fluents) {
 	    Atom[] atoms = new Atom[fluents.length];
 	    
 	    for (int i = 0; i < fluents.length; i++) {
 	        InitiallyFluent fluent = fluents[i];
 	        
 	        StringBuilder fluent_name = new StringBuilder();
 	        
 	        if (fluent.getFluent().getType() == Fluent.TYPE_PERMISSION) {
	            fluent_name.append("perm(").append(fluent.getFluent().getName()).append(")");
 	        } else if (fluent.getFluent().getType() == Fluent.TYPE_POWER) {
	            fluent_name.append("pow(").append(this._instal.getName()).append(",").append(fluent.getFluent().getName()).append(")");
 	        } else {
	            fluent_name.append(fluent.getFluent().getName());
 	        }
 	        
             if (fluent.hasUngroundedVariables()) {
                 Hashtable<String, Type> type_map = fluent.getUngroundedVariableTypeMap();
                 atoms[i] = new Blank(String.format("ifluent(%s) :- %s.", fluent_name, this.__generateVariableTypeGroundingRules("", type_map)));	            
             } else {
                 atoms[i] = new Blank(String.format("ifluent(%s).", fluent_name)); 
             }
 	    }
 	    
 	    if (atoms.length > 0) {
 	        return atoms;
 	    } else {
 	        return new Atom[] { new Comment("None") };
 	    }
 	}
 	
    protected Atom[] _generateNoninertialRules(NoninertialFluent[] fluents) {
         ArrayList<Atom> atoms = new ArrayList<Atom>();
         
         for (NoninertialFluent f : fluents) {
             Hashtable<String, Type> fluent_type_map = f.getParameterVariablesTypeMap(f.getParameterVariables());
             Hashtable<String, Type> conditions_type_map = f.getConditions().getConditionalVariablesTypeMap();
             
             StringBuilder conditions = new StringBuilder();
             for (int i = 0; i < f.getConditions().getConditionsTypeWithVariables().length; i++) {
                 Condition c = f.getConditions().getConditionsTypeWithVariables()[i];
                 
                 if (c.isNegated()) {
                     conditions.append("not ");
                 }
                 
                 conditions.append("holdsat(").append(c.asVariablesToString()).append(", I), ");
             }
             
             // From MDV Skype: holdsat(nonintertial,I) :- holdsat(conditions),variable grounding.
             atoms.add(new Blank(String.format(
                 "holdsat(%s, I) :- %s%s",
                 f.asVariablesToString(f.getParameterVariables()),
                 conditions,
                 this.__generateVariableTypeGroundingRules(", ", fluent_type_map, conditions_type_map)
             )));
         }
         
         return atoms.toArray(new Atom[] {});
     }
 	
 	/**
 	 * @FIXME We shouldn't have an extra event description for events that are already violations.
 	 * @TODO Check that creation events should actually be listed here.
 	 */
 	protected Atom[] _generateEvents(Event[] events) {
 	    ArrayList<Atom> atoms = new ArrayList<Atom>();
         
         for (int i = 0; i < events.length; i++) {
             Event e = events[i];
             
             atoms.add(new Comment(String.format("Event: %s (Type: %s)", e.getName().toString(), this.__eventTypeAbbr(e.getType()))));
             
             // Get some random variables for the type restirction variables
             String[] variables = new String[e.getParameters().size()];
             for (int j = 0; j < e.getParameters().size(); j++) {
                 variables[j] = String.valueOf(((char) (65 + j)));
             }
             
             Hashtable<String, Type> typemap = e.getParameterVariablesTypeMap(variables);
             
             // The grounding is where we infer types on the variables of the head
             StringBuilder grounding = new StringBuilder(this.__generateVariableTypeGroundingRules(", ", typemap));
             if (grounding.length() > 0) {
                 // Remove the trailing ', '
                 grounding.delete(grounding.length() - 2, grounding.length());
                 
                 // Add a ' :- ' to the begining of the grounding operation
                 grounding.insert(0, " :- ");
             }
             
             // Only build this once
             String variables_with_paren = e.getVariablesWithParenthesisToString(variables);
             
             atoms.add(new Blank(String.format("event(%s%s)%s.", e.getName().toString(), variables_with_paren, grounding)));
             atoms.add(new Blank(String.format("evtype(%s%s, %s)%s.", e.getName().toString(), variables_with_paren, this.__eventTypeAbbr(e.getType()), grounding)));
             // @FIXME This is evil and breaks encapsulation
             atoms.add(new Blank(String.format("evinst(%s%s, %s)%s.", e.getName().toString(), variables_with_paren, this._instal.getName(), grounding)));
             
             atoms.add(new Blank(String.format("event(viol(%s%s))%s.", e.getName().toString(), variables_with_paren, grounding)));
             atoms.add(new Blank(String.format("evtype(viol(%s%s), %s)%s.", e.getName().toString(), variables_with_paren, this.__eventTypeAbbr(Event.TYPE_VIOLATION), grounding)));
             // @FIXME This is evil and breaks encapsulation
             atoms.add(new Blank(String.format("evinst(viol(%s%s), %s)%s.", e.getName().toString(), variables_with_paren, this._instal.getName(), grounding)));
         }
     
         return atoms.toArray(new Atom[] {});
 	}
 	
 	/**
 	 * As per Marina's instructions, the creation event has the exogenous event type.
 	 * 
 	 * @param type
 	 * @return
 	 */
 	private String __eventTypeAbbr(int type) {
 	    switch (type) {
 	        case Event.TYPE_CREATION:
 	            return "ex";
 	        case Event.TYPE_EXOGENOUS:
 	            return "ex";
 	        case Event.TYPE_NORMATIVE:
 	            return "inst";
 	        case Event.TYPE_VIOLATION:
 	            return "viol";
 	        case Event.TYPE_DISSOLUTION:
 	            return "diss";
 	        default:
 	            return "";
 	    }
 	}
 
     @Override
     protected Atom[] _generateInitiateRules(Initiates[] rules) {
         ArrayList<Atom> atoms = new ArrayList<Atom>();
         
         for (Rule r: rules) {
         	Hashtable<String, Type> source_type_map = r.getSourceEvent().getParameterVariablesTypeMap(r.getSourceEventVariables());
         	Hashtable<String, Type> conditions_type_map = r.getConditionalVariablesTypeMap();
         	Hashtable<String, Type> result_type_map = r.getResultAtomsTypeMap();
         	
             // Generate our parameter constraints for the source event
             ArrayList<String[]> resultAtomVariables = r.getResultAtomVariables();
             StringBuilder conditionConstraints = new StringBuilder();
             conditionConstraints.append(this.__generateParameterConstraints(r.getSourceEvent(), r.getSourceEventVariables()));
             for (int k = 0; k < r.getResultAtoms().size(); k++) { 
                 conditionConstraints.append(this.__generateParameterConstraints(r.getResultAtoms().get(k), resultAtomVariables.get(k)));
             }
         	
             atoms.add(new Comment(""));
             atoms.add(new Comment(String.format("Translation of: %s", r.toString())));
             
             StringBuilder conditions = new StringBuilder();
             
             for (int i = 0; i < r.getConditionsTypeWithVariables().length; i++) {
                 Condition c = r.getConditionsTypeWithVariables()[i];
                 
                 if (c.isNegated()) {
                     conditions.append("not ");
                 }
                 
                 conditions.append("holdsat(").append(c.asVariablesToString()).append(", I), ");
             }
             
             for (int i = 0 ; i < r.getResultAtoms().size(); i++) {
                 Parameters a = r.getResultAtoms().get(i);
                 
                 if (a == null || r == null) continue;
                 
                 String s = a.asVariablesToString(r.getResultAtomVariables().get(i));
                 if (a.getType() == Fluent.TYPE_POWER) {
                     s = String.format("pow(%s, %s%s)", this._instal.getName(), a.getName(), a.getVariablesWithParenthesisToString(r.getResultAtomVariables().get(i)));
                 }
                 
                 atoms.add(
                     new Blank(String.format(
                         "initiated(%s, I) :- occurred(%s, I), %sholdsat(live(%s), I), %s%sinstant(I).",
                         s,
                         r._getSourceEventWithVariables(),
                         (conditions.toString().length() > 0) ? conditions : "",
                         this._instal.getName(),
                         this.__generateVariableTypeGroundingRules(", ", source_type_map, conditions_type_map, result_type_map),
                         conditionConstraints
                     ))
                 );
             }
         }
         
         return atoms.toArray(new Atom[] {});
     }
 
     @Override
     protected Atom[] _generateTerminateRules(Terminates[] rules) {
         ArrayList<Atom> atoms = new ArrayList<Atom>();
         
         for (Rule r: rules) {
             atoms.add(new Comment(""));
             atoms.add(new Comment(String.format("Translation of: %s", r.toString())));
             
             StringBuilder conditions = new StringBuilder();
             
             for (int i = 0; i < r.getConditionsTypeWithVariables().length; i++) {
                 Condition c = r.getConditionsTypeWithVariables()[i];
                 
                 if (c.isNegated()) {
                     conditions.append("not ");
                 }
                 
                 conditions.append("holdsat(").append(c.asVariablesToString()).append(", I), ");
             }
              
             for (int i = 0 ; i < r.getResultAtoms().size(); i++) {
                 Hashtable<String, Type> source_type_map = r.getSourceEvent().getParameterVariablesTypeMap(r.getSourceEventVariables());
                 Hashtable<String, Type> conditions_type_map = r.getConditionalVariablesTypeMap();
                 Hashtable<String, Type> result_type_map = r.getResultAtomsTypeMap();
                 
                 // Generate our parameter constraints for the source event
                 ArrayList<String[]> resultAtomVariables = r.getResultAtomVariables();
                 StringBuilder conditionConstraints = new StringBuilder();
                 conditionConstraints.append(this.__generateParameterConstraints(r.getSourceEvent(), r.getSourceEventVariables()));
                 for (int k = 0; k < r.getResultAtoms().size(); k++) { 
                     conditionConstraints.append(this.__generateParameterConstraints(r.getResultAtoms().get(k), resultAtomVariables.get(k)));
                 }
                 
                 Parameters a = r.getResultAtoms().get(i);
                 
                 String s = a.asVariablesToString(r.getResultAtomVariables().get(i));
                 if (a.getType() == Fluent.TYPE_POWER) {
                     s = String.format("pow(%s, %s%s)", this._instal.getName(), a.getName(), a.getVariablesWithParenthesisToString(r.getResultAtomVariables().get(i)));
                 }
                 
                 atoms.add(
                     new Blank(String.format(
                         "terminated(%s, I) :- occurred(%s, I), %sholdsat(live(%s), I), %s%sinstant(I).",
                         s,
                         r._getSourceEventWithVariables(),
                         (conditions.toString().length() > 0) ? conditions : "",
                         this._instal.getName(),
                         this.__generateVariableTypeGroundingRules(", ", source_type_map, conditions_type_map, result_type_map),
                         conditionConstraints
                     ))
                 );
             }
         }
         
         return atoms.toArray(new Atom[] {});
     }
     
     /**
      * @TODO Sort out the grounding..
      */
     protected Atom[] _generateGenerateRules(Generates[] rules) {
         ArrayList<Atom> atoms = new ArrayList<Atom>();
         
         for (Rule r: rules) {  
             atoms.add(new Comment(""));
             atoms.add(new Comment(String.format("Translation of %s", r.toString())));
             
             StringBuilder conditions = new StringBuilder();
             
             for (int i = 0; i < r.getConditionsTypeWithVariables().length; i++) {
                 Condition c = r.getConditionsTypeWithVariables()[i];
                 
                 if (c.isNegated()) {
                     conditions.append("not ");
                 }
                 
                 conditions.append("holdsat(").append(c.asVariablesToString()).append(", I), ");
             }
             
             for (String s: r.getResultAtomsWithVariables()) {
             	Hashtable<String, Type> source_type_map = r.getSourceEvent().getParameterVariablesTypeMap(r.getSourceEventVariables());
             	Hashtable<String, Type> conditions_type_map = r.getConditionalVariablesTypeMap();
             	Hashtable<String, Type> result_type_map = r.getResultAtomsTypeMap();
             	
             	// Generate our parameter constraints for the source event
                 ArrayList<String[]> resultAtomVariables = r.getResultAtomVariables();
             	StringBuilder conditionConstraints = new StringBuilder();
             	conditionConstraints.append(this.__generateParameterConstraints(r.getSourceEvent(), r.getSourceEventVariables()));
             	for (int k = 0; k < r.getResultAtoms().size(); k++) { 
             	    conditionConstraints.append(this.__generateParameterConstraints(r.getResultAtoms().get(k), resultAtomVariables.get(k)));
             	}
             	
                 atoms.add(
                     new Blank(String.format(
                         "occurred(%s, I) :- occurred(%s, I), %sholdsat(pow(%s, %s), I), %s%sinstant(I).",
                         s,
                         r._getSourceEventWithVariables(),
                         (conditions.toString().length() > 0) ? conditions : "",
                         this._instal.getName(),
                         s,
                         this.__generateVariableTypeGroundingRules(", ", source_type_map, conditions_type_map, result_type_map),
                         conditionConstraints.toString()
                     ))
                 );
             }
         }
         
         return atoms.toArray(new Atom[] {});
     }
     
     protected Atom[] _generateConcreteTypes(Domain d) {
     	ArrayList<Atom> atoms = new ArrayList<Atom>();
     	
     	for (Type t: d.getDefinedTypes()) {
     		for (String concrete: d.getConcretesOf(t)) {
     			atoms.add(new Blank(String.format("%s(%s).", t.toString().toLowerCase(), concrete)));
     		}
     	}
     	
     	return atoms.toArray(new Atom[] {});
     }
     
     protected Atom[] _generateTimeSteps(int steps) {
         Atom[] atoms = new Atom[steps*2+1];
         
         for (int i = 0; i < steps; i++) {
             atoms[i*2] = new Blank(String.format("instant(i%02d).", i));
             atoms[i*2+1] = new Blank(String.format("next(i%02d, i%02d).", i, i+1));
         }
         
         atoms[steps*2] = new Blank(String.format("final(i%02d).", steps));
             
         return atoms;
     }
     
     protected Atom[] _generateCreateEventRules(CreationEvent[] events, InitiallyFluent[] fluents) {
         ArrayList<Atom> atoms = new ArrayList<Atom>();
         
         for(InitiallyFluent f: fluents) {
             atoms.add(new Comment(""));
             atoms.add(new Comment(String.format("Initiation of %s for creation events", f.toString())));
             Hashtable<String, Type> type_map = new Hashtable<String, Type>();
             
             if (f.hasUngroundedVariables()) {
                 type_map = f.getUngroundedVariableTypeMap();
             }
             
             for(CreationEvent e: events) {
                 // For each event, build up a possible set of variables, then a type map
                 int counter = 0;
                 Hashtable<String, Type> event_type_map = new Hashtable<String, Type>();
                 ArrayList<Type> parameters = e.getParameters();
                 Iterator<Type> iter = parameters.iterator();
                 while (iter.hasNext()) {
                     Type t = iter.next();
                     event_type_map.put(String.format("VAR_%02d", counter++), t);
                 }
                 
                 atoms.add(
                     new Blank(String.format(
                         "initiated(%s, I) :- occurred(%s, I), not holdsat(live(%s), I), evtype(%s, %s), %sinstant(I).",
                         f.toString(),
                         e.asVariablesToString(event_type_map.keySet().toArray(new String[] {})),
                         this._instal.getName(),
                         e.getName(),
                         this.__eventTypeAbbr(e.getType()),
                         this.__generateVariableTypeGroundingRules(", ", event_type_map, type_map)
                     ))
                 );
             }
         }
         
         return atoms.toArray(new Atom[] {});
     }
     
     protected Atom[] _generateDissolutionEventRules(DissolutionEvent[] events) {
         ArrayList<Atom> atoms = new ArrayList<Atom>();
         
         for(DissolutionEvent e: events) {
             // For each event, build up a possible set of variables, then a type map
             int counter = 0;
             Hashtable<String, Type> event_type_map = new Hashtable<String, Type>();
             ArrayList<Type> parameters = e.getParameters();
             Iterator<Type> iter = parameters.iterator();
             while (iter.hasNext()) {
                 Type t = iter.next();
                 event_type_map.put(String.format("VAR_%02d", counter++), t);
             }
             
             atoms.add(
                 new Blank(String.format("terminated(X, I) :- occurred(%s, I), holdsat(live(%s), I), evtype(%s, %s), %sinstant(I), holdsat(X, I).",
                     e.asVariablesToString(event_type_map.keySet().toArray(new String[] {})),
                     this._instal.getName(),
                     e.getName(),
                     this.__eventTypeAbbr(e.getType()),
                     this.__generateVariableTypeGroundingRules(", ", event_type_map)
                 ))
             );
         }
         
         return atoms.toArray(new Atom[] {});
     }
     
     protected Atom[] _generateObligations(Obligation[] obligations) {
         ArrayList<Atom> atoms = new ArrayList<Atom>();
 
         for (Obligation o: obligations) {
             Hashtable<String, Type> act_type_map = o.getAct().getParameterVariablesTypeMap(o.getActVars());
             Hashtable<String, Type> before_type_map = o.getBefore().getParameterVariablesTypeMap(o.getBeforeVars());
             Hashtable<String, Type> otherwise_type_map = o.getOtherwise().getParameterVariablesTypeMap(o.getOtherwiseVars());
             
             String type_map_resolved = this.__generateVariableTypeGroundingRules(", ", act_type_map, before_type_map, otherwise_type_map);
             
             atoms.add(new Comment(""));
             atoms.add(new Comment(String.format("Translation of %s", o.toString())));
             
             String act = o.getAct().asVariablesToString(o.getActVars());
             String before = o.getBefore().asVariablesToString(o.getBeforeVars());
             String otherwise = o.getOtherwise().asVariablesToString(o.getOtherwiseVars());
             
             atoms.add(new Blank(String.format(
                 "terminated(obl(%s, %s, %s), I) :- occurred(%s, I), %sinstant(I).",
                 act,
                 before,
                 otherwise,
                 act,
                 type_map_resolved
             )));
             
             atoms.add(new Blank(String.format(
                 "terminated(obl(%s, %s, %s), I) :- occurred(%s, I), %sinstant(I).",
                 act,
                 before,
                 otherwise,
                 before,
                 type_map_resolved
             )));
             
             atoms.add(new Blank(String.format(
                 "occurred(%s, I) :- holdsat(obl(%s, %s, %s), I), occurred(%s, I), %sinstant(I).",
                 otherwise,
                 act,
                 before,
                 otherwise,
                 before,
                 type_map_resolved
             )));
         }
         
         return atoms.toArray(new Atom [] {});
     }
     
     private String __generateVariableTypeGroundingRules(String suffix, Hashtable<String, Type> ... tables) {
         String deliminator = ", ";
         ArrayList<String> done = new ArrayList<String>();
     	StringBuilder builder = new StringBuilder();
     	
     	if (tables != null) {
     		for(Hashtable<String, Type> table: tables) {
     			if (table != null) {
 			    	Iterator<String> iter = table.keySet().iterator();
 			    	while(iter.hasNext()) {
 			    		String key = iter.next();
 			    		
 			    		if (!done.contains(key)) {
 				    		builder.append(this.__typeToStringRepresentation(table.get(key)))
 			    		           .append("(")
 			    		           .append(key)
 			    		           .append(")")
 			    		           .append(deliminator);
 				    		
 				    		done.add(key);
 			    		}
 			    	}
     			}
     		}
     	}
     	
     	if (builder.length() > 0) {
         	builder.delete(builder.length() - deliminator.length() , builder.length());
     
         	builder.append(suffix);
     	}
     	
     	return builder.toString();
     }
     
     private String __generateParameterConstraints(Parameters p, String[] variables) {
         if (p == null || !p.hasParameterConditions()) {
             return "";
         }
         
         // Generate our parameter constraints for the source event
         StringBuilder conditionConstraints = new StringBuilder();
         ArrayList<String[]> constraints = p.getParameterConstraints(variables);
         Hashtable<String, Type> type_map = p.getParameterVariablesTypeMap(variables);
         
         for (int j = 0; j < constraints.size(); j++) {
             // A tuple of { [param], [op], [param] }
             String[] constraint = constraints.get(j);
             conditionConstraints.append(this.__typeToStringRepresentation(type_map.get(constraint[0]))).append("(").append(constraint[0]).append(") ")
                                 .append(constraint[1]).append(" ")
                                 .append(this.__typeToStringRepresentation(type_map.get(constraint[2]))).append("(").append(constraint[2]).append(") ");
         }
         
         if (conditionConstraints.length() > 0) {
             conditionConstraints.append(", ");
         }
         
         return conditionConstraints.toString();
     }
     
     private String __typeToStringRepresentation(Type t) {
         return t.toString().toLowerCase();
     }
 }
