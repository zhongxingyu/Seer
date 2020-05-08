 package com.silentmatt.dss;
 
 import com.silentmatt.dss.css.CssDeclaration;
 import com.silentmatt.dss.directive.ClassDirective;
 import com.silentmatt.dss.directive.DeclarationDirective;
 import com.silentmatt.dss.directive.GenericDirective;
 import com.silentmatt.dss.directive.RuleSetClass;
 import com.silentmatt.dss.term.ClassReferenceTerm;
 import com.silentmatt.dss.term.RuleSetClassReferenceTerm;
 import com.silentmatt.dss.term.Term;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a block of declarations in a DSS document.
  *
  * DeclarationBlock is the part of a rule that is surrounded by brackets.
  * 
  * DeclarationBlocks are attached to {@link RuleSet}, {@link ClassDirective},
  * {@link DeclarationDirective}, and {@link GenericDirective} objects.
  *
  * @author Matthew Crumley
  */
 public class DeclarationBlock {
     private final DeclarationList declarations;
     private final List<NestedRuleSet> nestedRuleSets;
 
     /**
      * Constructs an empty DeclarationBlock.
      */
     public DeclarationBlock() {
         this.declarations = new DeclarationList();
         this.nestedRuleSets = new ArrayList<NestedRuleSet>();
     }
 
     /**
      * Constructs a DeclarationBlock, containing a list of {@link Declaration}s.
      *
      * @param declarations The Declarations to intialize the block with. Declarations
      * are copied from the list by reference, so later changes to the list will
      * not affect the DeclarationBlock and vice versa, but changes to the
      * Declarations themselves will be reflected in the block.
      */
     public DeclarationBlock(List<Declaration> declarations) {
         this.declarations = new DeclarationList(declarations);
         this.nestedRuleSets = new ArrayList<NestedRuleSet>();
     }
 
     /**
      * Constructs a DeclarationBlock, containing a list of {@link Declaration}s
      * and {@link NestedRuleSet}s.
      *
      * @param declarations The Declarations to initialize the block with. As with
      * {@link #DeclarationBlock(java.util.List), the Declarations are copied into
      * a new list.
      *
      * @param nested The NestedRuleSets to initialize the block with. Like the
      * declarations, nested RuleSets are copied into a new list.
      */
     public DeclarationBlock(List<Declaration> declarations, List<NestedRuleSet> nested) {
         this.declarations = new DeclarationList(declarations);
         this.nestedRuleSets = new ArrayList<NestedRuleSet>(nested);
     }
 
     /**
      * Gets the list of Declarations in the block.
      *
      * @return The block's DeclarationList.
      */
     public DeclarationList getDeclarations() {
         return declarations;
     }
 
     /**
      * Evaluates the declations, to convert them into CssDeclarations.
      *
      * @param state The current {@link EvaluationState}.
      *
      * @return A {@link List} of {@link CssDeclaration}s.
      */
     public List<CssDeclaration> getCssDeclarations(EvaluationState state) {
         List<CssDeclaration> result = new ArrayList<CssDeclaration>();
         for (Declaration d : declarations) {
             result.add(new CssDeclaration(d.getName(), d.getExpression().evaluate(state, declarations), d.isImportant()));
         }
         return result;
     }
 
     /**
      * Adds a Declaration to the end of the block.
      *
      * @param declaration The {@link Declaration} to add.
      */
     public void addDeclaration(Declaration declaration) {
         declarations.add(declaration);
     }
 
     /**
      * Appends the Declarations in a list to the end of the block.
      *
      * @param declarations A {@link List} of {@link Declaration}s to add.
      */
     public void addDeclarations(List<Declaration> declarations) {
         for (Declaration declaration : declarations) {
             this.declarations.add(declaration);
         }
     }
 
     /**
      * Gets a property value by name.
      *
      * @param name The name of the property to get.
      *
      * @return The property's value as an {@link Expression}, or null if it
      * doesn't exist.
      */
     public Expression getValue(String name) {
         Declaration declaration = getDeclaration(name);
         return (declaration != null) ? declaration.getExpression() : null;
     }
 
     /**
      * Gets a Declaration by name.
      *
      * @param name The property name to get.
      *
      * @return A {@link Declaration} with the specified name, or null if none exists.
      */
     public Declaration getDeclaration(String name) {
         for (Declaration d : declarations) {
             if (d.getName().equalsIgnoreCase(name)) {
                 return d;
             }
         }
         return null;
     }
 
     /**
      * Gets the string representation of the declarations and nested rule sets in the block.
      * 
      * @param nesting The desired nesting level.
      *
      * @return The serialized form of the block without the surrounding brackets.
      *
      * @see #toString()
      * @see #toString(int)
      */
     public String innerString(int nesting) {
         String start = Rule.getIndent(nesting);
         StringBuilder txt = new StringBuilder("");
 
         for (Declaration dec : declarations) {
             txt.append("\n\t" + start);
             txt.append(dec.toString());
             txt.append(";");
         }
 
         for (RuleSet rs : nestedRuleSets) {
             txt.append("\n");
             txt.append(rs.toString(nesting + 1));
         }
 
         return txt.toString();
     }
 
     /**
      * Adds a RuleSet inside the block.
      *
      * @param cb The {@link Combinator} to apply to the RuleSet's selectors.
      * @param nested The {@link RuleSet} to nest inside the block.
      */
     public void addNestedRuleSet(Combinator cb, RuleSet nested) {
         nestedRuleSets.add(new NestedRuleSet(cb, nested));
     }
 
     /**
      * Adds a NestedRuleSet inside the block.
      *
      * @param nested The {@link NestedRuleSet} to nest inside the block.
      */
     public void addNestedRuleSet(NestedRuleSet nested) {
         nestedRuleSets.add(nested);
     }
 
     /**
      * Gets a list of nested RuleSets.
      *
      * @return The {@link List} of {@link NestedRuleSet} objects.
      */
     public List<NestedRuleSet> getNestedRuleSets() {
         return nestedRuleSets;
     }
 
     /**
      * Gets the string representation of the block.
      *
      * The block will be at the outermost nesting level (no leading indentation).
      *
      * @return The serialized representation of the block, including the brackets.
      *
      * @see #toString(int)
      * @see #innerString(int)
      */
     @Override
     public String toString() {
         return toString(0);
     }
 
     /**
      * Gets the string representation of the block, at the specified nesting level.
      *
      * @param nesting The desired nesting level.
      *
      * @return The serialized representation of the block, including the brackets.
      *
      * @see #toString()
      * @see #innerString(int)
      */
     public String toString(int nesting) {
         String start = Rule.getIndent(nesting);
         StringBuilder txt = new StringBuilder();
 
         txt.append("{");
         txt.append(innerString(nesting + 1));
         txt.append("\n" + start + "}");
 
         return txt.toString();
     }
 
     private static boolean matches(Declaration declaration, String name) {
         return declaration.getName().equalsIgnoreCase(name);
     }
 
     private static void setArguments(EvaluationState state, ClassDirective clazz, DeclarationList args) {
         DeclarationList formalParameters = clazz.getParameters(args);
 
         // Defaults
         for (Declaration param : formalParameters) {
             state.getParameters().declare(param.getName(), param.getExpression());
         }
 
         // Arguments
         int argNumber = 0;
         for (Declaration arg : args) {
             String paramName = arg.getName();
             if (paramName.isEmpty()) {
                 if (argNumber < 0) {
                    state.getErrors().SemErr("Positional arguments cannot follow named arguments in " + clazz.getClassName());
                     return;
                 }
                 if (argNumber < formalParameters.size()) {
                     paramName = formalParameters.get(argNumber).getName();
                 }
                 else {
                    state.getErrors().Warning("Too many arguments to class '" + clazz.getClassName() + "'");
                     continue;
                 }
                 ++argNumber;
             }
             else {
                 argNumber = -1;
             }
 
             if (state.getParameters().declaresKey(paramName)) {
                 state.getParameters().put(paramName, arg.getExpression());
             }
             else {
                state.getErrors().Warning(clazz.getClassName() + " does not have a parameter '" + arg.getName() + "'");
             }
         }
     }
 
     private static void addInheritedProperties(DeclarationBlock result, EvaluationState state, ClassDirective clazz, DeclarationList args) throws IOException {
         DeclarationList list = result.getDeclarations();
         // Make a copy of the properties, to substitute parameters into
         DeclarationList properties = new DeclarationList();
         for (Declaration prop : clazz.getDeclarations(args)) {
             properties.add(new Declaration(prop.getName(), prop.getExpression().clone(), prop.isImportant()));
         }
 
         state.pushParameters();
         try {
             setArguments(state, clazz, args);
 
             for (Declaration dec : properties) {
                 dec.substituteValue(state, list, true, true);
             }
         }
         finally {
             state.popParameters();
         }
 
         for (int i = 0; i < properties.size(); i++) {
             Declaration declaration = properties.get(i);
             list.add(declaration);
         }
 
         for (NestedRuleSet rs : clazz.getNestedRuleSets()) {
             result.addNestedRuleSet(rs);
         }
     }
 
     private static ClassDirective lookupRuleSet(RuleSetClassReferenceTerm crt, List<List<RuleSet>> ruleSets) {
         String needle = crt.getSelector().toString();
         boolean found = false;
 
         List<RuleSet> allRuleSets = new ArrayList<RuleSet>();
 
         for (List<RuleSet> rsList : ruleSets) {
             for (RuleSet rs : rsList) {
                 for (Selector s : rs.getSelectors()) {
                     if (s.toString().equals(needle)) {
                         found = true;
                         allRuleSets.add(rs);
                     }
                 }
             }
         }
 
         return found ? new RuleSetClass(allRuleSets) : null;
     }
 
     private static void addInheritedProperties(DeclarationBlock result, EvaluationState state, Expression inherits) throws MalformedURLException, IOException {
         for (Term inherit : inherits.getTerms()) {
             ClassReferenceTerm crt;
             if (inherit instanceof ClassReferenceTerm) {
                 crt = (ClassReferenceTerm) inherit;
             }
             else {
                 // XXX: May want to split ClassReferenceTerm into SimpleCRT and ParameterizedCRT
                 // so this doesn't need to create a new arguments list every time (SCRT would share one)
                 crt = new ClassReferenceTerm(inherit.toString());
             }
 
             ClassDirective clazz = null;
             // TODO: Move this logic to EvaluationState?
             if (crt instanceof RuleSetClassReferenceTerm) {
                 clazz = lookupRuleSet((RuleSetClassReferenceTerm) crt, state.getRuleSets());
             }
             else {
                 clazz = state.getClasses().get(crt.getName());
             }
 
             if (clazz == null) {
                 state.getErrors().SemErr("no such class: " + crt.getName());
                 return;
             }
 
             addInheritedProperties(result, state, clazz, crt.getArguments());
         }
     }
 
     /**
      * Evaluates the block, and returns the result.
      *
      * @param state The current {@link EvaluationState}.
      * @param doCalculations true if calc(...) terms should be evaluated.
      *
      * @return The result of the evaluation.
      *
      * @throws IOException
      */
     public DeclarationBlock evaluateStyle(EvaluationState state, boolean doCalculations) throws IOException {
         DeclarationBlock result = new DeclarationBlock(new DeclarationList(), new ArrayList<NestedRuleSet>(nestedRuleSets));
         return evaluateStyle(result, state, doCalculations);
     }
 
     protected List<RuleSet> getRuleSetScope() {
         List<RuleSet> result = new ArrayList<RuleSet>(nestedRuleSets.size());
         for (NestedRuleSet nrs : nestedRuleSets) {
             result.add(nrs);
         }
         return result;
     }
 
     protected DeclarationBlock evaluateStyle(DeclarationBlock result, EvaluationState state, boolean doCalculations) throws IOException {
         state.pushScope(getRuleSetScope());
         try {
             for (Declaration declaration : getDeclarations()) {
                 if (matches(declaration, "extend") || matches(declaration, "apply")) {
                     addInheritedProperties(result, state, declaration.getExpression());
                 }
                 else {
                     result.addDeclaration(declaration);
                 }
             }
 
             for (int i = 0; i < result.getDeclarations().size(); i++) {
                 result.getDeclarations().get(i).substituteValue(state, result.getDeclarations(), false, doCalculations);
             }
         }
         finally {
             state.popScope();
         }
 
         return result;
     }
 }
