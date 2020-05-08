 /**
  * (RelationalExpression RelationalOperator ShiftExpression)/
  * yyValue:ShiftExpression
  */
 
 package translator;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class RelationalExpression extends TranslationVisitor {
 
   private RelationalExpression relationalExpression;
   private String relationalOperator;
   private ShiftExpression shiftExpression;
 
   public RelationalExpression(GNode n) {
     relationalOperator = null;
     visit(n);
   }
 
  public void visitRelationalExpression(Gnode n) {
     relationalExpression = new RelationalExpression(n);
   }
 
   public void visitSymbol(GNode n) {
     relationalOperator = n.getString(0);
   }
 
   public void visitShiftExpression(GNode n) {
     shiftExpression = new ShiftExpression(n);
   }
 
 }
