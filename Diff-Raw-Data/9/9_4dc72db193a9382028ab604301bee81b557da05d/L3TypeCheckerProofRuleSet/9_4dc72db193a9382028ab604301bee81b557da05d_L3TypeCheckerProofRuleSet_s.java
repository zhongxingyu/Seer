 package de.unisiegen.tpml.core.languages.l3;
 
 import de.unisiegen.tpml.core.expressions.Constant;
 import de.unisiegen.tpml.core.expressions.CurriedLet;
 import de.unisiegen.tpml.core.expressions.CurriedLetRec;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.expressions.Identifier;
 import de.unisiegen.tpml.core.expressions.Lambda;
 import de.unisiegen.tpml.core.expressions.Let;
 import de.unisiegen.tpml.core.expressions.LetRec;
 import de.unisiegen.tpml.core.expressions.List;
 import de.unisiegen.tpml.core.expressions.MultiLet;
 import de.unisiegen.tpml.core.expressions.Recursion;
 import de.unisiegen.tpml.core.expressions.Tuple;
 import de.unisiegen.tpml.core.languages.l2.L2Language;
 import de.unisiegen.tpml.core.languages.l2.L2TypeCheckerProofRuleSet;
 import de.unisiegen.tpml.core.typechecker.TypeCheckerProofContext;
 import de.unisiegen.tpml.core.typechecker.TypeCheckerProofNode;
 import de.unisiegen.tpml.core.typechecker.TypeEnvironment;
 import de.unisiegen.tpml.core.types.ArrowType;
 import de.unisiegen.tpml.core.types.ListType;
 import de.unisiegen.tpml.core.types.MonoType;
 import de.unisiegen.tpml.core.types.TupleType;
 import de.unisiegen.tpml.core.types.Type;
 import de.unisiegen.tpml.core.types.TypeVariable;
 
 /**
  * The type proof rules for the <code>L3</code> language.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  *
  * @see de.unisiegen.tpml.core.languages.l2.L2TypeCheckerProofRuleSet
  */
 public class L3TypeCheckerProofRuleSet extends L2TypeCheckerProofRuleSet {
   //
   // Constructor
   //
   
   /**
    * Allocates a new <code>L3TypecheckerProofRuleSet</code> for the specified <code>language</code>.
    * 
    * @param language the <code>L3</code> or a derived language.
    * 
    * @throws NullPointerException if <code>language</code> is <code>null</code>.
    * 
    * @see L2TypeCheckerProofRuleSet#L2TypeCheckerProofRuleSet(L2Language)
    */
   public L3TypeCheckerProofRuleSet(L3Language language) {
     super(language);
     
     // drop the (CONST) and (ID) type rules
     unregister("CONST");
     unregister("ID");
     
     // register the additional type rules
     registerByMethodName(L3Language.L3, "LIST", "applyList");
     registerByMethodName(L3Language.L3, "P-CONST", "applyPConst");
     registerByMethodName(L3Language.L3, "P-ID", "applyPId");
     registerByMethodName(L3Language.L3, "P-LET", "applyPLet", "updatePLet");
     registerByMethodName(L3Language.L3, "TUPLE", "applyTuple");
   }
   
   
   
   //
   // The (LIST) rule
   //
   
   /**
    * Applies the <b>(LIST)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyList(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Expression[] expressions = ((List)node.getExpression()).getExpressions();
     TypeVariable tau = context.newTypeVariable();
     for (int n = 0; n < expressions.length; ++n) {
       context.addProofNode(node, node.getEnvironment(), expressions[n], tau);
     }
     context.addEquation(node.getType(), new ListType(tau));
   }
   
   
   
   //
   // The (P-CONST) rule
   //
   
   /**
    * Applies the <b>(P-CONST)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyPConst(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Constant constant = (Constant)node.getExpression();
     context.addEquation(node.getType(), context.instantiate(context.getTypeForExpression(constant)));
   }
   
   
   
   //
   // The (P-ID) rule
   //
   
   /**
    * Applies the <b>(P-ID)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyPId(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Type type = node.getEnvironment().get(((Identifier)node.getExpression()).getName());
     context.addEquation(node.getType(), context.instantiate(type));
   }
   
   
   
   //
   // The (P-LET) rule
   //
   
   /**
    * Applies the <b>(P-LET)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyPLet(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     // determine the type environment
     TypeEnvironment environment = node.getEnvironment();
     
     // can be applied to LetRec, Let, MultiLet, CurriedLetRec and CurriedLet
     Expression expression = node.getExpression();
     if (expression instanceof Let) {
       // determine the first sub expression
       Let let = (Let)expression;
       Expression e1 = let.getE1();
       
       // check if a type was specified
       MonoType tau1 = let.getTau();
       if (tau1 == null) {
         tau1 = context.newTypeVariable();
       }
       
       // add the recursion for let rec
       if (expression instanceof LetRec) {
         // add the recursion for e1
         e1 = new Recursion(let.getId(), tau1, e1);
       }
       
       // add only the first child node, the second one will be added
       // in updatePLet() once the first sub tree is finished.
       context.addProofNode(node, environment, e1, tau1);
     }
     else if (expression instanceof MultiLet) {
       // determine the identifiers of the multi let
       MultiLet multiLet = (MultiLet)expression;
       String[] identifiers = multiLet.getIdentifiers();
 
       // generate the type for e1
       TypeVariable[] typeVariables = new TypeVariable[identifiers.length];
       for (int n = 0; n < identifiers.length; ++n) {
         typeVariables[n] = context.newTypeVariable();
       }
       TupleType tau = new TupleType(typeVariables);
       
       // add only the first child node, the second one will be added
       // in updatePLet() once the first sub tree is finished.
       context.addProofNode(node, environment, multiLet.getE1(), tau);
       
       // check if we have a type
       if (multiLet.getTau() != null) {
         // add an equation for tau = multiLet.getTau()
         context.addEquation(tau, multiLet.getTau());
       }
     }
     else {
       // determine the first sub expression
       CurriedLet curriedLet = (CurriedLet)expression;
       Expression e1 = curriedLet.getE1();
       
       // generate the appropriate lambda abstractions
       MonoType[] types = curriedLet.getTypes();
       String[] identifiers = curriedLet.getIdentifiers();
       for (int n = identifiers.length - 1; n > 0; --n) {
         e1 = new Lambda(identifiers[n], types[n], e1);
       }
       
       // generate the type of the function
       MonoType tau1 = types[0];
       if (tau1 == null) {
         tau1 = context.newTypeVariable();
       }
      for (int n = 1; n < types.length; ++n) {
         tau1 = new ArrowType((types[n] != null) ? types[n] : context.newTypeVariable(), tau1);
       }
 
       // add the recursion for let rec
       if (expression instanceof CurriedLetRec) {
         e1 = new Recursion(identifiers[0], tau1, e1);
       }
       
       // add only the first child node, the second one will be added
       // in updatePLet() once the first sub tree is finished.
       context.addProofNode(node, environment, e1, tau1);
     }
   }
   
   /**
    * Updates the <code>node</code>, to which <b>(P-LET)</b> was applied previously, using <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void updatePLet(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     // check if the sub tree of the first child is finished now
     if (node.getChildCount() == 1 && node.getChildAt(0).isFinished()) {
       // determine the type environment
       TypeEnvironment environment = node.getEnvironment();
       
       // can be applied to LetRec, Let, CurriedLetRec and CurriedLet
       Expression expression = node.getExpression();
       if (expression instanceof Let) {
         Let let = (Let)expression;
         MonoType tau = node.getType();
         MonoType tau1 = node.getChildAt(0).getType();
         context.addProofNode(node, environment.extend(let.getId(), environment.closure(tau1)), let.getE2(), tau);
       }
       else if (expression instanceof MultiLet) {
         MultiLet multiLet = (MultiLet)expression;
         String[] identifiers = multiLet.getIdentifiers();
         TupleType tau = (TupleType)node.getChildAt(0).getType();
         
         // generate the environment for e2
         TypeEnvironment environment2 = environment;
         for (int n = 0; n < identifiers.length; ++n) {
           environment2 = environment2.extend(identifiers[n], environment.closure(tau.getTypes(n)));
         }
         
         // add the second proof node (for e2)
         context.addProofNode(node, environment2, multiLet.getE2(), node.getType());
       }
       else {
         CurriedLet curriedLet = (CurriedLet)expression;
         MonoType tau = node.getType();
         MonoType tau1 = node.getChildAt(0).getType();
         context.addProofNode(node, environment.extend(curriedLet.getIdentifiers(0), environment.closure(tau1)), curriedLet.getE2(), tau);
       }
     }
   }
   
   
   
   //
   // The (TUPLE) rule
   //
   
   /**
    * Applies the <b>(TUPLE)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyTuple(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Expression[] expressions = ((Tuple)node.getExpression()).getExpressions();
     TypeVariable[] typeVariables = new TypeVariable[expressions.length];
     for (int n = 0; n < expressions.length; ++n) {
       typeVariables[n] = context.newTypeVariable();
       context.addProofNode(node, node.getEnvironment(), expressions[n], typeVariables[n]);
     }
     context.addEquation(node.getType(), new TupleType(typeVariables));
   }
 }
