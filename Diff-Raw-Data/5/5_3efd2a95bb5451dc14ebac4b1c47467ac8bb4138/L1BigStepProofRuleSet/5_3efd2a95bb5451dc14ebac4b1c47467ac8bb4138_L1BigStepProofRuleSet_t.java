 package de.unisiegen.tpml.core.languages.l1 ;
 
 
 import de.unisiegen.tpml.core.Messages ;
 import de.unisiegen.tpml.core.bigstep.BigStepProofContext ;
 import de.unisiegen.tpml.core.bigstep.BigStepProofNode ;
 import de.unisiegen.tpml.core.bigstep.BigStepProofResult ;
 import de.unisiegen.tpml.core.bigstep.BigStepProofRule ;
 import de.unisiegen.tpml.core.expressions.And ;
 import de.unisiegen.tpml.core.expressions.Application ;
 import de.unisiegen.tpml.core.expressions.BinaryCons ;
 import de.unisiegen.tpml.core.expressions.BinaryOperator ;
 import de.unisiegen.tpml.core.expressions.BinaryOperatorException ;
 import de.unisiegen.tpml.core.expressions.BooleanConstant ;
 import de.unisiegen.tpml.core.expressions.Coercion ;
 import de.unisiegen.tpml.core.expressions.Condition ;
 import de.unisiegen.tpml.core.expressions.Condition1 ;
 import de.unisiegen.tpml.core.expressions.CurriedLet ;
 import de.unisiegen.tpml.core.expressions.CurriedLetRec ;
 import de.unisiegen.tpml.core.expressions.Expression ;
 import de.unisiegen.tpml.core.expressions.Identifier ;
 import de.unisiegen.tpml.core.expressions.InfixOperation ;
 import de.unisiegen.tpml.core.expressions.Lambda ;
 import de.unisiegen.tpml.core.expressions.Let ;
 import de.unisiegen.tpml.core.expressions.LetRec ;
 import de.unisiegen.tpml.core.expressions.MultiLet ;
 import de.unisiegen.tpml.core.expressions.Not ;
 import de.unisiegen.tpml.core.expressions.Or ;
 import de.unisiegen.tpml.core.expressions.Projection ;
 import de.unisiegen.tpml.core.expressions.Recursion ;
 import de.unisiegen.tpml.core.expressions.UnaryOperatorException ;
 import de.unisiegen.tpml.core.expressions.UnitConstant ;
 import de.unisiegen.tpml.core.languages.l0.L0BigStepProofRuleSet ;
 import de.unisiegen.tpml.core.languages.l0.L0Language ;
 import de.unisiegen.tpml.core.types.MonoType ;
 
 
 /**
  * Big step proof rules for the <b>L1</b> and derived languages.
  * 
  * @author Benedikt Meurer
  * @author Christian Fehler
  * @version $Rev:1132 $
  * @see de.unisiegen.tpml.core.languages.l0.L0BigStepProofRuleSet
  */
 public class L1BigStepProofRuleSet extends L0BigStepProofRuleSet
 {
   /**
    * Allocates a new <code>L1BigStepProofRuleSet</code> with the specified
    * <code>language</code>, which is the <b>L1</b> or a derived language.
    * 
    * @param language the language for the proof rule set.
    * @throws NullPointerException if <code>language</code> is
    *           <code>null</code>.
    * @see L0BigStepProofRuleSet#L0BigStepProofRuleSet(L0Language)
    */
   public L1BigStepProofRuleSet ( L1Language language )
   {
     super ( language ) ;
     // register the big step rules (order is important for guessing!)
     registerByMethodName ( L1Language.L1 ,
         "AND-FALSE" , "applyAnd" , "updateAndFalse" ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     registerByMethodName ( L1Language.L1 ,
         "AND-TRUE" , "applyAnd" , "updateAndTrue" ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     registerByMethodName ( L1Language.L1 ,
         "COND-FALSE" , "applyCond" , "updateCondFalse" ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     registerByMethodName ( L1Language.L1 ,
         "COND-TRUE" , "applyCond" , "updateCondTrue" ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     registerByMethodName ( L1Language.L1 , "LET" , "applyLet" , "updateLet" ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     registerByMethodName ( L1Language.L1 , "NOT" , "applyNot" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L1Language.L1 , "OP" , "applyOp" ) ; //$NON-NLS-1$ //$NON-NLS-2$
     registerByMethodName ( L1Language.L1 ,
         "OR-FALSE" , "applyOr" , "updateOrFalse" ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     registerByMethodName ( L1Language.L1 ,
         "OR-TRUE" , "applyOr" , "updateOrTrue" ) ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     registerByMethodName ( L1Language.L1 , "COERCE" , "applyCoercion" , //$NON-NLS-1$ //$NON-NLS-2$
         "updateCoercion" ) ; //$NON-NLS-1$
   }
 
 
   /**
    * Applies the <b>(AND-FALSE)</b> or <b>(AND-TRUE)</b> rule to the
    * <code>node</code> using the <code>context</code>.
    * 
    * @param context the big step proof context.
    * @param node the node to apply the <b>(AND-FALSE)</b> or <b>(AND-TRUE)</b>
    *          rule to.
    */
   public void applyAnd ( BigStepProofContext context , BigStepProofNode node )
   {
     // add the first proof node
     context.addProofNode ( node , ( ( And ) node.getExpression ( ) ).getE1 ( ) ) ;
   }
 
 
   /**
    * Applies the <b>(COERCE)</b> rule to the <code>node</code> using the
    * <code>context</code>.
    * 
    * @param context the big step proof context.
    * @param node the node to apply the <b>(COERCE)</b> rule to.
    */
   public void applyCoercion ( BigStepProofContext context ,
       BigStepProofNode node )
   {
     // add the first proof node
     context.addProofNode ( node , ( ( Coercion ) node.getExpression ( ) )
         .getE ( ) ) ;
   }
 
 
   /**
    * Applies the <b>(COND-FALSE)</b> or <b>(COND-TRUE)</b> rule to the
    * <code>node</code> using the <code>context</code>.
    * 
    * @param context the big step proof context.
    * @param node the node to apply the <b>(COND-FALSE)</b> or <b>(COND-TRUE)</b>
    *          rule to.
    */
   public void applyCond ( BigStepProofContext context , BigStepProofNode node )
   {
     // can be applied to Condition and Condition1
     Expression e = node.getExpression ( ) ;
     if ( e instanceof Condition )
     {
       // add the first child node
       context.addProofNode ( node , ( ( Condition ) e ).getE0 ( ) ) ;
     }
     else
     {
       // add the first child node
       context.addProofNode ( node , ( ( Condition1 ) e ).getE0 ( ) ) ;
     }
   }
 
 
   /**
    * Applies the <b>(LET)</b> rule to the <code>node</code> using the
    * <code>context</code>.
    * 
    * @param context the big step proof context.
    * @param node the node to apply the <b>(LET)</b> rule to.
    */
   public void applyLet ( BigStepProofContext context , BigStepProofNode node )
   {
     Expression e = node.getExpression ( ) ;
     if ( e instanceof CurriedLet || e instanceof CurriedLetRec )
     {
       // determine the first sub expression
       CurriedLet curriedLet = ( CurriedLet ) e ;
       Expression e1 = curriedLet.getE1 ( ) ;
       // generate the appropriate lambda abstractions
       Identifier [ ] identifiers = curriedLet.getIdentifiers ( ) ;
       MonoType [ ] types = curriedLet.getTypes ( ) ;
       for ( int n = identifiers.length - 1 ; n > 0 ; -- n )
       {
         e1 = new Lambda ( identifiers [ n ] , types [ n ] , e1 ) ;
       }
       // add the recursion for letrec
       if ( e instanceof CurriedLetRec )
       {
        e1 = new Recursion ( identifiers [ 0 ] , null , e1 ) ;
       }
       // add the proof node
       context.addProofNode ( node , e1 ) ;
     }
     else if ( e instanceof MultiLet )
     {
       // prove the first sub expression
       context.addProofNode ( node , ( ( MultiLet ) e ).getE1 ( ) ) ;
     }
     else
     {
       // determine the first sub expression
       Let let = ( Let ) e ;
       Expression e1 = let.getE1 ( ) ;
       // add the recursion for letrec
       if ( e instanceof LetRec )
       {
         LetRec letRec = ( LetRec ) e ;
        e1 = new Recursion ( letRec.getId ( ) , null , e1 ) ;
       }
       // add the proof node
       context.addProofNode ( node , e1 ) ;
     }
   }
 
 
   /**
    * Applies the <b>(NOT)</b> rule to the <code>node</code> using the
    * <code>context</code>.
    * 
    * @param context the big step proof context.
    * @param node the node to apply the <b>(NOT)</b> rule to.
    * @throws UnaryOperatorException if the <b>(NOT)</b> rule cannot be applied
    *           here.
    */
   public void applyNot ( BigStepProofContext context , BigStepProofNode node )
       throws UnaryOperatorException
   {
     Application application = ( Application ) node.getExpression ( ) ;
     Not e1 = ( Not ) application.getE1 ( ) ;
     context.setProofNodeResult ( node , e1.applyTo ( application.getE2 ( ) ) ) ;
   }
 
 
   /**
    * Applies the <b>(OP)</b> rule to the <code>node</code> using the
    * <code>context</code>.
    * 
    * @param context the big step proof context.
    * @param node the node to apply the <b>(OP)</b> rule to.
    * @throws BinaryOperatorException if the <b>(OP)</b> rule cannot be applied
    *           here.
    */
   public void applyOp ( BigStepProofContext context , BigStepProofNode node )
       throws BinaryOperatorException
   {
     // depends on whether we have an Application or InfixOperation
     BinaryOperator op ;
     Expression e1 ;
     Expression e2 ;
     // check if Application or InfixOperation
     Expression e = node.getExpression ( ) ;
     if ( e instanceof Application )
     {
       // Application: (op e1) e2
       Application a1 = ( Application ) e ;
       Application a2 = ( Application ) a1.getE1 ( ) ;
       op = ( BinaryOperator ) a2.getE1 ( ) ;
       e1 = a2.getE2 ( ) ;
       e2 = a1.getE2 ( ) ;
     }
     else
     {
       // otherwise must be an InfixOperation
       InfixOperation infixOperation = ( InfixOperation ) e ;
       op = infixOperation.getOp ( ) ;
       e1 = infixOperation.getE1 ( ) ;
       e2 = infixOperation.getE2 ( ) ;
     }
     // we must not handle BinaryCons here
     if ( op instanceof BinaryCons )
     {
       throw new IllegalArgumentException ( Messages
           .getString ( "L1BigStepProofRuleSet.0" ) ) ; //$NON-NLS-1$
     }
     // perform the application
     context.setProofNodeResult ( node , op.applyTo ( e1 , e2 ) ) ;
   }
 
 
   /**
    * Applies the <b>(OR-FALSE)</b> or <b>(OR-TRUE)</b> rule to the
    * <code>node</code> using the <code>context</code>.
    * 
    * @param context the big step proof context.
    * @param node the node to apply the <b>(OR-FALSE)</b> or <b>(OR-TRUE)</b>
    *          rule to.
    */
   public void applyOr ( BigStepProofContext context , BigStepProofNode node )
   {
     // add the first proof node
     context.addProofNode ( node , ( ( Or ) node.getExpression ( ) ).getE1 ( ) ) ;
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(AND-FALSE)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(AND-FALSE)</b>.
    */
   public void updateAndFalse ( BigStepProofContext context ,
       BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // determine the result of the first child node
       BigStepProofResult result0 = node.getChildAt ( 0 ).getResult ( ) ;
       try
       {
         // the value of the child node must be a boolean constant
         BooleanConstant value0 = ( BooleanConstant ) result0.getValue ( ) ;
         if ( value0.booleanValue ( ) )
         {
           // let (AND-TRUE) handle the node
           context.setProofNodeRule ( node ,
               ( BigStepProofRule ) getRuleByName ( "AND-TRUE" ) ) ; //$NON-NLS-1$
           updateAndTrue ( context , node ) ;
         }
         else
         {
           // we're done with this node
           context.setProofNodeResult ( node , result0 ) ;
         }
       }
       catch ( ClassCastException cause )
       {
         // not a boolean constant...
       }
     }
     else if ( node.getChildCount ( ) == 2 && node.getChildAt ( 0 ).isProven ( )
         && node.getChildAt ( 1 ).isProven ( ) )
     {
       // use the result of the second child node for this node
       context.setProofNodeResult ( node , node.getChildAt ( 1 ).getResult ( ) ) ;
     }
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(AND-TRUE)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(AND-TRUE)</b>.
    */
   public void updateAndTrue ( BigStepProofContext context ,
       BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // determine the result of the first child node
       BigStepProofResult result0 = node.getChildAt ( 0 ).getResult ( ) ;
       try
       {
         // the value of the child node must be a boolean value
         BooleanConstant value0 = ( BooleanConstant ) result0.getValue ( ) ;
         if ( value0.booleanValue ( ) )
         {
           // add a child node for the second expression
           context.addProofNode ( node , ( ( And ) node.getExpression ( ) )
               .getE2 ( ) ) ;
         }
         else
         {
           // let (AND-FALSE) handle the node
           context.setProofNodeRule ( node ,
               ( BigStepProofRule ) getRuleByName ( "AND-FALSE" ) ) ; //$NON-NLS-1$
           updateAndFalse ( context , node ) ;
         }
       }
       catch ( ClassCastException cause )
       {
         // not a boolean constant...
       }
     }
     else if ( node.getChildCount ( ) == 2 && node.getChildAt ( 0 ).isProven ( )
         && node.getChildAt ( 1 ).isProven ( ) )
     {
       // use the result of the second child node for this node
       context.setProofNodeResult ( node , node.getChildAt ( 1 ).getResult ( ) ) ;
     }
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(COERCE)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(COERCE)</b>.
    */
   public void updateCoercion ( BigStepProofContext context ,
       BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // forward the result of the second child node
       context.setProofNodeResult ( node , node.getChildAt ( 0 ).getResult ( ) ) ;
     }
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(COND-FALSE)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(COND-FALSE)</b>.
    */
   public void updateCondFalse ( BigStepProofContext context ,
       BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // determine the result of the first child node
       BigStepProofResult result0 = node.getChildAt ( 0 ).getResult ( ) ;
       try
       {
         // the value of the child node must be a boolean value
         BooleanConstant value0 = ( BooleanConstant ) result0.getValue ( ) ;
         if ( value0.booleanValue ( ) )
         {
           // let (COND-TRUE) handle the node
           context.setProofNodeRule ( node ,
               ( BigStepProofRule ) getRuleByName ( "COND-TRUE" ) ) ; //$NON-NLS-1$
           updateCondTrue ( context , node ) ;
         }
         else
         {
           // can be applied to Condition and Condition1
           Expression e = node.getExpression ( ) ;
           if ( e instanceof Condition )
           {
             // add next proof node for e2
             context.addProofNode ( node , ( ( Condition ) e ).getE2 ( ) ) ;
           }
           else
           {
             // result is the unit constant
             context.setProofNodeResult ( node , new UnitConstant ( ) ) ;
           }
         }
       }
       catch ( ClassCastException cause )
       {
         // not a boolean constant...
       }
     }
     else if ( node.getChildCount ( ) == 2 && node.getChildAt ( 0 ).isProven ( )
         && node.getChildAt ( 1 ).isProven ( ) )
     {
       // use the result of the second child node for this node
       context.setProofNodeResult ( node , node.getChildAt ( 1 ).getResult ( ) ) ;
     }
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(COND-TRUE)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(COND-TRUE)</b>.
    */
   public void updateCondTrue ( BigStepProofContext context ,
       BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // determine the result of the first child node
       BigStepProofResult result0 = node.getChildAt ( 0 ).getResult ( ) ;
       try
       {
         // the result of the child node must be a boolean value
         BooleanConstant value0 = ( BooleanConstant ) result0.getValue ( ) ;
         if ( ! value0.booleanValue ( ) )
         {
           // let (COND-FALSE) handle the node
           context.setProofNodeRule ( node ,
               ( BigStepProofRule ) getRuleByName ( "COND-FALSE" ) ) ; //$NON-NLS-1$
           updateCondFalse ( context , node ) ;
         }
         else
         {
           // can be applied to Condition and Condition1
           Expression e = node.getExpression ( ) ;
           if ( e instanceof Condition )
           {
             // add next proof node for e1
             context.addProofNode ( node , ( ( Condition ) e ).getE1 ( ) ) ;
           }
           else
           {
             // add next proof node for e1
             context.addProofNode ( node , ( ( Condition1 ) e ).getE1 ( ) ) ;
           }
         }
       }
       catch ( ClassCastException cause )
       {
         // not a boolean constant...
       }
     }
     else if ( node.getChildCount ( ) == 2 && node.getChildAt ( 0 ).isProven ( )
         && node.getChildAt ( 1 ).isProven ( ) )
     {
       // use the result of the second child node for this node
       context.setProofNodeResult ( node , node.getChildAt ( 1 ).getResult ( ) ) ;
     }
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(LET)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(LET)</b>.
    */
   public void updateLet ( BigStepProofContext context , BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // determine the value of the first child node
       Expression value0 = node.getChildAt ( 0 ).getResult ( ).getValue ( ) ;
       // determine the expression for the node
       Expression e = node.getExpression ( ) ;
       // check the expression type
       if ( e instanceof CurriedLet )
       {
         // add a proof node for e2 (CurriedLet/CurriedLetRec)
         CurriedLet curriedLet = ( CurriedLet ) e ;
         context.addProofNode ( node , curriedLet.getE2 ( ).substitute (
             curriedLet.getIdentifiers ( ) [ 0 ] , value0 ) ) ;
       }
       else if ( e instanceof MultiLet )
       {
         // determine the second sub expression e2 (MultiLet)
         MultiLet multiLet = ( MultiLet ) e ;
         Expression e2 = multiLet.getE2 ( ) ;
         // perform the required substitutions
         Identifier [ ] identifiers = multiLet.getIdentifiers ( ) ;
         for ( int n = 0 ; n < identifiers.length ; ++ n )
         {
           // substitute: (#l_n value0) for id
           e2 = e2.substitute ( identifiers [ n ] , new Application (
               new Projection ( identifiers.length , n + 1 ) , value0 ) ) ;
         }
         // add a proof node for e2
         context.addProofNode ( node , e2 ) ;
       }
       else
       {
         // add a proof node for e2 (Let/LetRec)
         Let let = ( Let ) e ;
         context.addProofNode ( node , let.getE2 ( ).substitute ( let.getId ( ) ,
             value0 ) ) ;
       }
     }
     else if ( node.getChildCount ( ) == 2 )
     {
       // forward the result of the second child node
       context.setProofNodeResult ( node , node.getChildAt ( 1 ).getResult ( ) ) ;
     }
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(OR-FALSE)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(OR-FALSE)</b>.
    */
   public void updateOrFalse ( BigStepProofContext context ,
       BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // determine the result of the first child node
       BigStepProofResult result0 = node.getChildAt ( 0 ).getResult ( ) ;
       try
       {
         // the value of the child node must be a boolean value
         BooleanConstant value0 = ( BooleanConstant ) result0.getValue ( ) ;
         if ( value0.booleanValue ( ) )
         {
           // let (OR-TRUE) handle the node
           context.setProofNodeRule ( node ,
               ( BigStepProofRule ) getRuleByName ( "OR-TRUE" ) ) ; //$NON-NLS-1$
           updateOrTrue ( context , node ) ;
         }
         else
         {
           // add a child node for the second expression
           context.addProofNode ( node , ( ( Or ) node.getExpression ( ) )
               .getE2 ( ) ) ;
         }
       }
       catch ( ClassCastException cause )
       {
         // not a boolean constant...
       }
     }
     else if ( node.getChildCount ( ) == 2 && node.getChildAt ( 0 ).isProven ( )
         && node.getChildAt ( 1 ).isProven ( ) )
     {
       // use the result of the second child node for this node
       context.setProofNodeResult ( node , node.getChildAt ( 1 ).getResult ( ) ) ;
     }
   }
 
 
   /**
    * Updates the <code>node</code> to which <b>(OR-TRUE)</b> was applied
    * previously.
    * 
    * @param context the big step proof context.
    * @param node the node to update according to <b>(OR-TRUE)</b>.
    */
   public void updateOrTrue ( BigStepProofContext context , BigStepProofNode node )
   {
     // check if we have exactly one proven child node
     if ( node.getChildCount ( ) == 1 && node.getChildAt ( 0 ).isProven ( ) )
     {
       // determine the result of the first child node
       BigStepProofResult result0 = node.getChildAt ( 0 ).getResult ( ) ;
       try
       {
         // the value of the child node must be a boolean value
         BooleanConstant value0 = ( BooleanConstant ) result0.getValue ( ) ;
         if ( value0.booleanValue ( ) )
         {
           // we're done with this node
           context.setProofNodeResult ( node , result0 ) ;
         }
         else
         {
           // let (OR-FALSE) handle the node
           context.setProofNodeRule ( node ,
               ( BigStepProofRule ) getRuleByName ( "OR-FALSE" ) ) ; //$NON-NLS-1$
           updateOrFalse ( context , node ) ;
         }
       }
       catch ( ClassCastException cause )
       {
         // not a boolean constant...
       }
     }
     else if ( node.getChildCount ( ) == 2 && node.getChildAt ( 0 ).isProven ( )
         && node.getChildAt ( 1 ).isProven ( ) )
     {
       // use the result of the second child node for this node
       context.setProofNodeResult ( node , node.getChildAt ( 1 ).getResult ( ) ) ;
     }
   }
 }
