 package de.unisiegen.tpml.core.languages.l1;
 
 import de.unisiegen.tpml.core.expressions.And;
 import de.unisiegen.tpml.core.expressions.Application;
 import de.unisiegen.tpml.core.expressions.Condition;
 import de.unisiegen.tpml.core.expressions.Constant;
 import de.unisiegen.tpml.core.expressions.CurriedLet;
 import de.unisiegen.tpml.core.expressions.CurriedLetRec;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.expressions.Identifier;
 import de.unisiegen.tpml.core.expressions.InfixOperation;
 import de.unisiegen.tpml.core.expressions.Lambda;
 import de.unisiegen.tpml.core.expressions.Let;
 import de.unisiegen.tpml.core.expressions.LetRec;
 import de.unisiegen.tpml.core.expressions.MultiLambda;
 import de.unisiegen.tpml.core.expressions.MultiLet;
 import de.unisiegen.tpml.core.expressions.Or;
 import de.unisiegen.tpml.core.expressions.Recursion;
 import de.unisiegen.tpml.core.languages.l2.L2Language;
 import de.unisiegen.tpml.core.typechecker.AbstractTypeCheckerProofRuleSet;
 import de.unisiegen.tpml.core.typechecker.TypeCheckerProofContext;
 import de.unisiegen.tpml.core.typechecker.TypeCheckerProofNode;
 import de.unisiegen.tpml.core.typechecker.TypeEnvironment;
 import de.unisiegen.tpml.core.types.ArrowType;
 import de.unisiegen.tpml.core.types.BooleanType;
 import de.unisiegen.tpml.core.types.MonoType;
 import de.unisiegen.tpml.core.types.TupleType;
 import de.unisiegen.tpml.core.types.Type;
 import de.unisiegen.tpml.core.types.TypeVariable;
 
 /**
  * The type proof rules for the <code>L1</code> language.
  *
  * @author Benedikt Meurer
  * @version $Id: Lt1TypeCheckerProofRuleSet.java 272 2006-09-19 15:55:48Z benny $
  *
  * @see de.unisiegen.tpml.core.typechecker.AbstractTypeCheckerProofRuleSet
  */
 public class L1TypeCheckerProofRuleSet extends AbstractTypeCheckerProofRuleSet {
   //
   // Constructor
   //
   
   /**
    * Allocates a new <code>L1TypeCheckerProofRuleSet</code> for the specified <code>language</code>.
    * 
    * @param language the <code>L1</code> or a derived language.
    * 
    * @throws NullPointerException if <code>language</code> is <code>null</code>.
    */
   public L1TypeCheckerProofRuleSet(L1Language language) {
     super(language);
     
     // register the type rules
     registerByMethodName(L1Language.L1, "ABSTR", "applyAbstr");
     registerByMethodName(L2Language.L2, "AND", "applyAnd");
     registerByMethodName(L1Language.L1, "APP", "applyApp");
     registerByMethodName(L1Language.L1, "COND", "applyCond");
     registerByMethodName(L1Language.L1, "CONST", "applyConst");
     registerByMethodName(L1Language.L1, "ID", "applyId");
     registerByMethodName(L1Language.L1, "LET", "applyLet");
     registerByMethodName(L2Language.L2, "OR", "applyOr");
   }
   
   
   
   //
   // The (AND) rule
   //
   
   /**
    * Applies the <b>(AND)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyAnd(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     And and = (And)node.getExpression();
     
     // generate new child nodes
     context.addProofNode(node, node.getEnvironment(), and.getE1(), BooleanType.BOOL);
     context.addProofNode(node, node.getEnvironment(), and.getE2(), BooleanType.BOOL);
     
     // add the {tau = bool} equation
     context.addEquation(node.getType(), BooleanType.BOOL);
   }
   
   
   
   //
   // The (ABSTR) rule
   //
   
   /**
    * Applies the <b>(ABSTR)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyAbstr(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     // determine the type environment
     TypeEnvironment environment = node.getEnvironment();
     
     // can be applied to both Lambda and MultiLambda
     Expression expression = node.getExpression();
     if (expression instanceof Lambda) {
       // determine the type for the parameter 
       Lambda lambda = (Lambda)expression;
       MonoType tau1 = lambda.getTau();
       if (tau1 == null) {
         // need a new type variable
         tau1 = context.newTypeVariable();
       }
       
       // generate a new type variable for the result
       TypeVariable tau2 = context.newTypeVariable();
       
       // add type equations for tau and tau1->tau2
       context.addEquation(node.getType(), new ArrowType(tau1, tau2));
       
       // generate a new child node
       context.addProofNode(node, environment.extend(lambda.getId(), tau1), lambda.getE(), tau2);
     }
     else {
       // determine the type for the parameter
       MultiLambda multiLambda = (MultiLambda)expression;
       String[] identifiers = multiLambda.getIdentifiers();
 
       // generate the type for identifiers (tau1)
       TypeVariable[] typeVariables = new TypeVariable[identifiers.length];
       for (int n = 0; n < identifiers.length; ++n) {
         typeVariables[n] = context.newTypeVariable();
       }
       TupleType tau1 = new TupleType(typeVariables);
 
       // generate the type variable for the result
       TypeVariable tau2 = context.newTypeVariable();
       
       // add type equations for tau and tau1->tau2
       context.addEquation(node.getType(), new ArrowType(tau1, tau2));
       
       // generate the environment for e
       for (int n = 0; n < identifiers.length; ++n) {
         environment = environment.extend(identifiers[n], typeVariables[n]);
       }
       
       // add the child nodes
       context.addProofNode(node, environment, multiLambda.getE(), tau2);
       
       // check if we have a type
       if (multiLambda.getTau() != null) {
         // add an equation for tau1 = multiLet.getTau()
         context.addEquation(tau1, multiLambda.getTau());
       }
     }
   }
   
   
   
   //
   // The (APP) rule
   //
   
   /**
    * Applies the <b>(APP)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyApp(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     // split into tau1 and tau2 for the application
     TypeVariable tau2 = context.newTypeVariable();
     ArrowType tau1 = new ArrowType(tau2, node.getType());
     
     // can be either an application or an infix operation
     try {
       // generate new child nodes
       Application application = (Application)node.getExpression();
       context.addProofNode(node, node.getEnvironment(), application.getE1(), tau1);
       context.addProofNode(node, node.getEnvironment(), application.getE2(), tau2);
     }
     catch (ClassCastException e) {
       // generate new child nodes
       InfixOperation infixOperation = (InfixOperation)node.getExpression();
       Application application = new Application(infixOperation.getOp(), infixOperation.getE1());
       context.addProofNode(node, node.getEnvironment(), application, tau1);
       context.addProofNode(node, node.getEnvironment(), infixOperation.getE2(), tau2);
     }
   }
   
   
   
   //
   // The (COND) rule
   //
   
   /**
    * Applies the <b>(COND)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyCond(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Condition condition = (Condition)node.getExpression();
     context.addProofNode(node, node.getEnvironment(), condition.getE0(), BooleanType.BOOL);
     context.addProofNode(node, node.getEnvironment(), condition.getE1(), node.getType());
     context.addProofNode(node, node.getEnvironment(), condition.getE2(), node.getType());
   }
   
   
   
   //
   // The (CONST) rule
   //
   
   /**
    * Applies the <b>(CONST)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyConst(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Constant constant = (Constant)node.getExpression();
     context.addEquation(node.getType(), (MonoType)context.getTypeForExpression(constant));
   }
   
   
   
   //
   // The (ID) rule
   //
   
   /**
    * Applies the <b>(ID)</b> rule to the <code>node</node> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyId(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Type type = node.getEnvironment().get(((Identifier)node.getExpression()).getName());
     context.addEquation(node.getType(), (MonoType)type);
   }
   
   
   
   //
   // The (LET) rule
   //
   
   /**
    * Applies the <b>(LET)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyLet(TypeCheckerProofContext context, TypeCheckerProofNode node) {
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
       
       // add the child nodes
       context.addProofNode(node, environment, e1, tau1);
       context.addProofNode(node, environment.extend(let.getId(), tau1), let.getE2(), node.getType());
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
       
       // generate the environment for e2
       TypeEnvironment environment2 = environment;
       for (int n = 0; n < identifiers.length; ++n) {
         environment2 = environment2.extend(identifiers[n], typeVariables[n]);
       }
       
       // add the child nodes
       context.addProofNode(node, environment, multiLet.getE1(), tau);
       context.addProofNode(node, environment2, multiLet.getE2(), node.getType());
       
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
      for (int n = types.length - 1; n > 0; --n) {
         tau1 = new ArrowType((types[n] != null) ? types[n] : context.newTypeVariable(), tau1);
       }
       
       // add the recursion for let rec
       if (expression instanceof CurriedLetRec) {
         // add the recursion
         e1 = new Recursion(identifiers[0], tau1, e1);
       }
       
       // add the child nodes
       context.addProofNode(node, environment, e1, tau1);
       context.addProofNode(node, environment.extend(identifiers[0], tau1), curriedLet.getE2(), node.getType());
     }
   }
   
   
   
   //
   // The (OR) rule
   //
   
   /**
    * Applies the <b>(OR)</b> rule to the <code>node</code> using the <code>context</code>.
    * 
    * @param context the type checker proof context.
    * @param node the type checker proof node.
    */
   public void applyOr(TypeCheckerProofContext context, TypeCheckerProofNode node) {
     Or or = (Or)node.getExpression();
     
     // generate new child nodes
     context.addProofNode(node, node.getEnvironment(), or.getE1(), BooleanType.BOOL);
     context.addProofNode(node, node.getEnvironment(), or.getE2(), BooleanType.BOOL);
     
     // add the {tau = bool} equation
     context.addEquation(node.getType(), BooleanType.BOOL);
   }
 }
