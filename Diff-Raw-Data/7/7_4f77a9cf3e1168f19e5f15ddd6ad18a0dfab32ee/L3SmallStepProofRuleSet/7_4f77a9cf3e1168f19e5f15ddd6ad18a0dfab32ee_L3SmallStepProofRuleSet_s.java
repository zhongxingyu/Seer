 package de.unisiegen.tpml.core.languages.l3 ;
 
 
 import de.unisiegen.tpml.core.expressions.Application ;
 import de.unisiegen.tpml.core.expressions.BinaryCons ;
 import de.unisiegen.tpml.core.expressions.BinaryOperator ;
 import de.unisiegen.tpml.core.expressions.BinaryOperatorException ;
 import de.unisiegen.tpml.core.expressions.BooleanConstant ;
 import de.unisiegen.tpml.core.expressions.EmptyList ;
 import de.unisiegen.tpml.core.expressions.Exn ;
 import de.unisiegen.tpml.core.expressions.Expression ;
 import de.unisiegen.tpml.core.expressions.Fst ;
 import de.unisiegen.tpml.core.expressions.Hd ;
 import de.unisiegen.tpml.core.expressions.Identifier ;
 import de.unisiegen.tpml.core.expressions.IsEmpty ;
 import de.unisiegen.tpml.core.expressions.List ;
 import de.unisiegen.tpml.core.expressions.MultiLambda ;
 import de.unisiegen.tpml.core.expressions.MultiLet ;
 import de.unisiegen.tpml.core.expressions.Projection ;
 import de.unisiegen.tpml.core.expressions.Snd ;
 import de.unisiegen.tpml.core.expressions.Tl ;
 import de.unisiegen.tpml.core.expressions.Tuple ;
 import de.unisiegen.tpml.core.expressions.UnaryCons ;
 import de.unisiegen.tpml.core.expressions.UnaryOperatorException ;
 import de.unisiegen.tpml.core.languages.l1.L1SmallStepProofRuleSet ;
 import de.unisiegen.tpml.core.languages.l2.L2Language ;
 import de.unisiegen.tpml.core.languages.l2.L2SmallStepProofRuleSet ;
 import de.unisiegen.tpml.core.smallstep.SmallStepProofContext ;
 
 
 /**
  * Small step proof rules for the <code>L3</code> language.
  * 
  * @author Benedikt Meurer
  * @author Christian Fehler
  * @version $Rev:1132 $
  * @see de.unisiegen.tpml.core.languages.l2.L2SmallStepProofRuleSet
  */
 public class L3SmallStepProofRuleSet extends L2SmallStepProofRuleSet
 {
   //
   // Constructor
   //
   /**
    * Allocates a new <code>L3SmallStepProofRuleSet</code> for the specified
    * <code>language</code>, which must be either <tt>L3</tt> or a derived
    * language.
    * 
    * @param language the {@link de.unisiegen.tpml.core.languages.Language}.
    * @throws NullPointerException if <code>language</code> is
    *           <code>null</code>.
    * @see L2SmallStepProofRuleSet#L2SmallStepProofRuleSet(L2Language)
    */
   public L3SmallStepProofRuleSet ( L3Language language )
   {
     super ( language ) ;
     // register the additional rules
     register ( L3Language.L3 , "CONS" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "FST" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "HD" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "HD-EMPTY" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "IS-EMPTY-FALSE" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "IS-EMPTY-TRUE" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "LIST" , false ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "PROJ" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "SND" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "TL" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "TL-EMPTY" , true ) ; //$NON-NLS-1$
     register ( L3Language.L3 , "TUPLE" , false ) ; //$NON-NLS-1$
   }
 
 
   /**
    * Applies the <code>multiLambda</code> to the <code>tuple</code> using
    * the <code>context</code>.
    * 
    * @param context the small step proof context.
    * @param application the application.
    * @param multiLambda the {@link MultiLambda}.
    * @param tuple the {@link Tuple}.
    * @return the resulting expression.
    */
   public Expression applyMultiLambda ( SmallStepProofContext context ,
       Application application , MultiLambda multiLambda , Tuple tuple )
   {
     // determine the expressions and identifiers
     Identifier [ ] identifiers = multiLambda.getIdentifiers ( ) ;
     Expression [ ] expressions = tuple.getExpressions ( ) ;
     // the tuple's arity must match the arity of the multiLambda
     if ( expressions.length != identifiers.length )
     {
       return application ;
     }
     // perform the substitutions
     Expression e = multiLambda.getE ( ) ;
     for ( int n = 0 ; n < identifiers.length ; ++ n )
     {
       e = e.substitute ( identifiers [ n ] , expressions [ n ] ) ;
     }
     // yep, that was (BETA-V) then
     context.addProofStep ( getRuleByName ( "BETA-V" ) , application ) ; //$NON-NLS-1$
     // and return the new expression
     return e ;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see L1SmallStepProofRuleSet#applyBinaryOperator(SmallStepProofContext,
    *      Expression, BinaryOperator, Expression, Expression)
    */
   @ Override
   protected Expression applyBinaryOperator ( SmallStepProofContext context ,
       Expression applicationOrInfix , BinaryOperator op , Expression e1 ,
       Expression e2 )
   {
     if ( op instanceof BinaryCons )
     {
       try
       {
         Expression e = op.applyTo ( e1 , e2 ) ;
         context.addProofStep ( getRuleByName ( "CONS" ) , applicationOrInfix ) ; //$NON-NLS-1$
         return e ;
       }
       catch ( BinaryOperatorException e )
       {
         return applicationOrInfix ;
       }
     }
     return super.applyBinaryOperator ( context , applicationOrInfix , op , e1 ,
         e2 ) ;
   }
 
 
   /**
    * Applies the {@link Fst} operator <code>e1</code> to the {@link Tuple}
    * <code>e2</code> using the <code>context</code>.
    * 
    * @param context the small step proof context.
    * @param application the application.
    * @param e1 the operator.
    * @param e2 the operand.
    * @return the resulting expression.
    */
   public Expression applyFst ( SmallStepProofContext context ,
       Application application , Fst e1 , Tuple e2 )
   {
     try
     {
       Expression e = e1.applyTo ( e2 ) ;
       context.addProofStep ( getRuleByName ( "FST" ) , application ) ; //$NON-NLS-1$
       return e ;
     }
     catch ( UnaryOperatorException e )
     {
       return application ;
     }
   }
 
 
   /**
    * Applies the {@link Hd} operator <code>e1</code> to the {@link Expression}
    * <code>e2</code> using the <code>context</code>.
    * 
    * @param context the msll step proof context.
    * @param application the application.
    * @param e1 the operator.
    * @param e2 the operand.
    * @return the resulting expression.
    */
   public Expression applyHd ( SmallStepProofContext context ,
       Application application , @ SuppressWarnings ( "unused" )
       Hd e1 , Expression e2 )
   {
     // check if e2 is the empty list
     if ( e2 instanceof EmptyList )
     {
       context.addProofStep ( getRuleByName ( "HD-EMPTY" ) , application ) ; //$NON-NLS-1$
       return Exn.newEmptyList ( ) ;
     }
     // check if e2 is a list
     if ( e2 instanceof List )
     {
       context.addProofStep ( getRuleByName ( "HD" ) , application ) ; //$NON-NLS-1$
       return ( ( List ) e2 ).head ( ) ;
     }
     // otherwise e2 must be an application of cons to a pair
     Application app1 = ( Application ) e2 ;
     Tuple tuple = ( Tuple ) app1.getE2 ( ) ;
     if ( ! ( app1.getE1 ( ) instanceof UnaryCons )
         || tuple.getExpressions ( ).length != 2 )
     {
       // we're stuck
       return application ;
     }
     // jep, we can perform (HD) then
     context.addProofStep ( getRuleByName ( "HD" ) , application ) ; //$NON-NLS-1$
     // return the first item
     return tuple.getExpressions ( ) [ 0 ] ;
   }
 
 
   /**
    * Applies the {@link IsEmpty} operator <code>e1</code> to the
    * {@link Expression} <code>e2</code> using the <code>context</code>.
    * 
    * @param context the msll step proof context.
    * @param application the application.
    * @param e1 the operator.
    * @param e2 the operand.
    * @return the resulting expression.
    */
   public Expression applyIsEmpty ( SmallStepProofContext context ,
       Application application , @ SuppressWarnings ( "unused" )
       IsEmpty e1 , Expression e2 )
   {
     // check if e2 is the empty list, or an application of cons to a value, or a
     // list
     if ( e2 instanceof EmptyList )
     {
       context.addProofStep ( getRuleByName ( "IS-EMPTY-TRUE" ) , application ) ; //$NON-NLS-1$
       return new BooleanConstant ( true ) ;
     }
     else if ( ( e2 instanceof List )
         || ( e2 instanceof Application
             && ( ( Application ) e2 ).getE1 ( ) instanceof UnaryCons && ( ( Application ) e2 )
             .getE2 ( ).isValue ( ) ) )
     {
       context.addProofStep ( getRuleByName ( "IS-EMPTY-FALSE" ) , application ) ; //$NON-NLS-1$
       return new BooleanConstant ( false ) ;
     }
     else
     {
       // we're stuck
       return application ;
     }
   }
 
 
   /**
    * Evaluates the <code>multiLet</code> expression using the
    * <code>context</code> and returns the resulting expression.
    * 
    * @param context the small step proof context.
    * @param multiLet the {@link MultiLet} expression.
    * @return the resulting expression.
    */
   public Expression evaluateMultiLet ( SmallStepProofContext context ,
       MultiLet multiLet )
   {
     // determine the identifiers and the sub expressions
     Identifier [ ] identifiers = multiLet.getIdentifiers ( ) ;
     Expression e1 = multiLet.getE1 ( ) ;
     Expression e2 = multiLet.getE2 ( ) ;
     // check if e1 is not already a value
     if ( ! e1.isValue ( ) )
     {
       // we're about to perform (LET-EVAL)
       context.addProofStep ( getRuleByName ( "LET-EVAL" ) , multiLet ) ; //$NON-NLS-1$
       // try to evaluate e1
       e1 = evaluate ( context , e1 ) ;
       // exceptions need special treatment
       Identifier [ ] newIdentifier = new Identifier [ identifiers.length ] ;
       for ( int i = 0 ; i < identifiers.length ; i ++ )
       {
         newIdentifier [ i ] = identifiers [ i ] ;
       }
       return e1.isException ( ) ? e1 : new MultiLet ( newIdentifier , multiLet
           .getTau ( ) == null ? null : multiLet.getTau ( ) , e1 , e2 ) ;
     }
     // arity of the tuple must match
     Expression [ ] expressions = ( ( Tuple ) e1 ).getExpressions ( ) ;
     if ( expressions.length != identifiers.length )
     {
       return multiLet ;
     }
     // perform the substitutions
     for ( int n = 0 ; n < identifiers.length ; ++ n )
     {
       e2 = e2.substitute ( identifiers [ n ] , expressions [ n ] ) ;
     }
     // jep, that was (LET-EXEC) then
     context.addProofStep ( getRuleByName ( "LET-EXEC" ) , multiLet ) ; //$NON-NLS-1$
     // return the new expression
     return e2 ;
   }
 
 
   /**
    * Evaluates the <code>list</code> using the <code>context</code>.
    * 
    * @param context the small step proof context.
    * @param list the list to evaluate.
    * @return the resulting expression.
    */
   public Expression evaluateList ( SmallStepProofContext context , List list )
   {
     // determine the sub expressions
     Expression [ ] expressions = list.getExpressions ( ) ;
     // find the first sub expression that is not already a value
     for ( int n = 0 ; n < expressions.length ; ++ n )
     {
       // check if the expression is not already a value
       if ( ! expressions [ n ].isValue ( ) )
       {
         // we're about to perform (LIST)
         context.addProofStep ( getRuleByName ( "LIST" ) , list ) ; //$NON-NLS-1$
         // try to evaluate the expression
         Expression newExpression = evaluate ( context , expressions [ n ] ) ;
         // check if we need to forward an exception
         if ( newExpression.isException ( ) )
         {
           return newExpression ;
         }
         // otherwise generate a new list with the new expression
         Expression [ ] newExpressions = new Expression [ expressions.length ] ;
         for ( int j = 0 ; j < expressions.length ; j ++ )
         {
           if ( j != n )
           {
             newExpressions [ j ] = expressions [ j ] ;
           }
         }
         newExpressions [ n ] = newExpression ;
         return new List ( newExpressions ) ;
       }
     }
     // hm, can we get stuck here?
     return list ;
   }
 
 
   /**
    * Applies the {@link Projection} <code>e1</code> to the {@link Tuple}
    * <code>e2</code> using the <code>context</code>.
    * 
    * @param context the small step proof context.
    * @param application the application.
    * @param e1 the operator.
    * @param e2 the operand.
    * @return the resulting expression.
    */
   public Expression applyProjection ( SmallStepProofContext context ,
       Application application , Projection e1 , Tuple e2 )
   {
     try
     {
       Expression e = e1.applyTo ( e2 ) ;
       context.addProofStep ( getRuleByName ( "PROJ" ) , application ) ; //$NON-NLS-1$
       return e ;
     }
     catch ( UnaryOperatorException e )
     {
       return application ;
     }
   }
 
 
   /**
    * Applies the {@link Snd} operator <code>e1</code> to the {@link Tuple}
    * <code>e2</code> using the <code>context</code>.
    * 
    * @param context the small step proof context.
    * @param application the application.
    * @param e1 the operator.
    * @param e2 the operand.
    * @return the resulting expression.
    */
   public Expression applySnd ( SmallStepProofContext context ,
       Application application , Snd e1 , Tuple e2 )
   {
     try
     {
       Expression e = e1.applyTo ( e2 ) ;
       context.addProofStep ( getRuleByName ( "SND" ) , application ) ; //$NON-NLS-1$
       return e ;
     }
     catch ( UnaryOperatorException e )
     {
       return application ;
     }
   }
 
 
   /**
    * Applies the {@link Tl} operator <code>e1</code> to the {@link Expression}
    * <code>e2</code> using the <code>context</code>.
    * 
    * @param context the msll step proof context.
    * @param application the application.
    * @param e1 the operator.
    * @param e2 the operand.
    * @return the resulting expression.
    */
   public Expression applyTl ( SmallStepProofContext context ,
       Application application , @ SuppressWarnings ( "unused" )
       Tl e1 , Expression e2 )
   {
     // check if e is the empty list
     if ( e2 instanceof EmptyList )
     {
       context.addProofStep ( getRuleByName ( "TL-EMPTY" ) , application ) ; //$NON-NLS-1$
       return Exn.newEmptyList ( ) ;
     }
     // check if e is a list
     if ( e2 instanceof List )
     {
       context.addProofStep ( getRuleByName ( "TL" ) , application ) ; //$NON-NLS-1$
       return ( ( List ) e2 ).tail ( ) ;
     }
     // otherwise, e2 must be an application of cons to a pair
     Application app1 = ( Application ) e2 ;
     Tuple tuple = ( Tuple ) app1.getE2 ( ) ;
     if ( ! ( app1.getE1 ( ) instanceof UnaryCons )
         || tuple.getExpressions ( ).length != 2 )
     {
       // we're stuck
       return application ;
     }
     // jep, we can perform (TL) then
     context.addProofStep ( getRuleByName ( "TL" ) , application ) ; //$NON-NLS-1$
     // return the remaining list
     return tuple.getExpressions ( ) [ 1 ] ;
   }
 
 
   /**
    * Evaluates the <code>tuple</code> using the <code>context</code> and
    * returns the resulting expression.
    * 
    * @param context the small step proof context.
    * @param tuple the tuple to evaluate.
    * @return the resulting expression.
    */
   public Expression evaluateTuple ( SmallStepProofContext context , Tuple tuple )
   {
     // determine the sub expressions
     Expression [ ] expressions = tuple.getExpressions ( ) ;
     // find the first sub expression that is not already a value
     for ( int n = 0 ; n < expressions.length ; ++ n )
     {
       // check if the expression is not already a value
       if ( ! expressions [ n ].isValue ( ) )
       {
         // we're about to perform (TUPLE)
         context.addProofStep ( getRuleByName ( "TUPLE" ) , tuple ) ; //$NON-NLS-1$
         // try to evaluate the expression
         Expression newExpression = evaluate ( context , expressions [ n ] ) ;
         // check if we need to forward an exception
         if ( newExpression.isException ( ) )
         {
           return newExpression ;
         }
         // otherwise generate a new tuple with the new expression
         Expression [ ] newExpressions = new Expression [ expressions.length ] ;
         for ( int j = 0 ; j < expressions.length ; j ++ )
         {
           if ( j != n )
           {
             newExpressions [ j ] = expressions [ j ] ;
           }
         }
         newExpressions [ n ] = newExpression ;
         return new Tuple ( newExpressions ) ;
       }
     }
     // hm, can we get stuck here?
     return tuple ;
   }
 }
