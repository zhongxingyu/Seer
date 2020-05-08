 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.token;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 
 
 /**
  * Zq\g[NNX
  * 
  * @author kou-tngt
  *
  */
 public class OperatorToken extends AstTokenAdapter {
 
     /**
      * LXg͉Zq\萔CX^X
      */
     public static final OperatorToken CAST = new OperatorToken("CAST", 2, false, false, null);
 
     /**
      * CNgZqƃfNgZq\萔CX^X
      */
     public static final OperatorToken INCL_AND_DECL = new OperatorToken("INCLEMENT", 1, true, true,
             null);
 
     /**
      * Zq\萔CX^X
      */
     public static final OperatorToken ASSIGNMENT = new OperatorToken("ASSIGN", 2, true, false, null);
 
     /**
      * Zq\萔CX^X
      */
     public static final OperatorToken COMPOUND_ASSIGNMENT = new OperatorToken("COMPOUND_ASSIGNMENT",2,true,true,null);
     
     /**
      * 񍀉Zq\萔CX^X
      */
     public static final OperatorToken TWO_TERM = new OperatorToken("TWO_TERM", 2, false, true, null);
 
     /**
      * PZq\萔CX^X
      */
     public static final OperatorToken SINGLE_TERM = new OperatorToken("SINGLE_TERM", 1, false,
             true, null);
 
     /**
     * OZq\萔CX^X
      */
    public static final OperatorToken THREE_TERM = new OperatorToken("THREE_TERM", 3, false, true,
             null);
 
     /**
      * rZq\萔CX^X
      */
     public static final OperatorToken COMPARE = new OperatorToken("COMPARE", 2, false, true,
             PrimitiveTypeInfo.BOOLEAN);
 
     /**
      * ے艉Zq\萔CX^X
      */
     public static final OperatorToken NOT = new OperatorToken("NOT", 1, false, true,
             PrimitiveTypeInfo.BOOLEAN);
 
     /**
      * zLqq\萔CX^X
      */
     public static final OperatorToken ARRAY = new OperatorToken("ARRAY", 2, false, true, null);
 
     /**
      * Zq̕C̐CӒlւ̎QƂƑsǂCZʂ̌^w肷RXgN^.
      * 
      * @param text Zq̕
      * @param termCount ̐
      * @param leftIsAssignmentee Ӓlւ̑ꍇtrue
      * @param leftIsReferencee Ӓlւ̂ꍇtrue
      * @param specifiedResultType Zʂ̌^܂Ăꍇ͂̌^C܂ĂȂꍇnullw肷
      * @throws IllegalArgumentException termCount0ȉ̏ꍇ
      */
     public OperatorToken(final String text, final int termCount, final boolean leftIsAssignmentee,
             final boolean leftIsReferencee, final UnresolvedTypeInfo specifiedResultType) {
         super(text);
 
         if (termCount <= 0) {
             throw new IllegalArgumentException("Operator must treat one or more terms.");
         }
 
         this.leftIsAssignmentee = leftIsAssignmentee;
         this.leftIsReferencee = leftIsReferencee;
         this.termCount = termCount;
         this.specifiedResultType = specifiedResultType;
     }
 
     /**
      * ̉Zq舵̐Ԃ.
      * @return ̉Zq舵̐
      */
     public int getTermCount() {
         return this.termCount;
     }
 
     /**
      * Ӓlւ̑邩ǂԂ.
      * @return@Ӓlւ̑ꍇtrue
      */
     @Override
     public boolean isAssignmentOperator() {
         return this.leftIsAssignmentee;
     }
 
     /* (non-Javadoc)
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstTokenAdapter#isOperator()
      */
     @Override
     public boolean isOperator() {
         return true;
     }
 
     /**
      * ӒlQƂƂėp邩ǂԂ.
      * @return@ӒlQƂƂėpꍇtrue
      */
     public boolean isLeftTermIsReferencee() {
         return this.leftIsReferencee;
     }
 
     /**
      * Zʂ̌^܂Ăꍇ͂̌^Ԃ.
      * ܂ĂȂꍇnullԂ.
      * @return Zʂ̌^܂Ăꍇ͂̌^C܂ĂȂꍇnull
      */
     public UnresolvedTypeInfo getSpecifiedResultType() {
         return this.specifiedResultType;
     }
 
     /**
      * Ӓlւ̑邩ǂ\
      */
     private final boolean leftIsAssignmentee;
 
     /**
      * ӒlQƂƂėp邩ǂ\
      */
     private final boolean leftIsReferencee;
 
     /**
      * ̉Zq舵̐
      */
     private final int termCount;
 
     /**
      * Zʂ̌^\.
      * ܂ĂȂꍇnull.
      */
     private final UnresolvedTypeInfo specifiedResultType;
 
 }
