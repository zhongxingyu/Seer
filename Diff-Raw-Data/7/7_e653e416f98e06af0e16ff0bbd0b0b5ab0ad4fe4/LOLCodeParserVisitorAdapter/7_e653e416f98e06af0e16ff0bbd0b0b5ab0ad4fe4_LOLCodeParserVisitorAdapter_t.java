 package wci.intermediate;
 
import wci.frontend.ASTError;
 import wci.frontend.ASTLOLCodeProgram;
 import wci.frontend.ASTadd;
 import wci.frontend.ASTand;
 import wci.frontend.ASTargs;
 import wci.frontend.ASTassign;
 import wci.frontend.ASTbooleanValue;
 import wci.frontend.ASTcaseStatement;
 import wci.frontend.ASTcast;
 import wci.frontend.ASTcharacterConstant;
 import wci.frontend.ASTcodeBlock;
 import wci.frontend.ASTconcatenate;
 import wci.frontend.ASTdecrement;
 import wci.frontend.ASTdefaultCase;
 import wci.frontend.ASTdivide;
 import wci.frontend.ASTequals;
 import wci.frontend.ASTfunction;
 import wci.frontend.ASTgreater_than;
 import wci.frontend.ASTidentifier;
 import wci.frontend.ASTif_false;
 import wci.frontend.ASTif_true;
 import wci.frontend.ASTincrement;
 import wci.frontend.ASTintegerConstant;
 import wci.frontend.ASTless_than;
 import wci.frontend.ASTloop;
 import wci.frontend.ASTloopAction;
 import wci.frontend.ASTmax;
 import wci.frontend.ASTmin;
 import wci.frontend.ASTmodulo;
 import wci.frontend.ASTmultiply;
 import wci.frontend.ASTname;
 import wci.frontend.ASTnot;
 import wci.frontend.ASTor;
 import wci.frontend.ASTrealConstant;
 import wci.frontend.ASTstringLiteral;
 import wci.frontend.ASTsubtract;
 import wci.frontend.ASTswitchStatement;
 import wci.frontend.ASTtest;
 import wci.frontend.ASTtype;
 import wci.frontend.ASTunaryExpression;
 import wci.frontend.ASTxor;
 import wci.frontend.LOLCodeParserVisitor;
 import wci.frontend.SimpleNode;
 
 public class LOLCodeParserVisitorAdapter implements LOLCodeParserVisitor 
 {
    public Object visit(SimpleNode node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
   public Object visit(ASTError node, Object data)
   {
      return node.childrenAccept(this, data);      
   }
   
    public Object visit(ASTLOLCodeProgram node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTcodeBlock node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTtest node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTif_true node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTif_false node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTloop node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTloopAction node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTnot node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTswitchStatement node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTcaseStatement node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTdefaultCase node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTfunction node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTname node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTargs node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTstringLiteral node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTcharacterConstant node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTintegerConstant node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTrealConstant node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTbooleanValue node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTidentifier node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTadd node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTsubtract node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTmultiply node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTdivide node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTmodulo node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTmax node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTmin node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTunaryExpression node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTdecrement node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTincrement node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTassign node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTand node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTor node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTxor node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTequals node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTless_than node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTgreater_than node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTconcatenate node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTtype node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
    public Object visit(ASTcast node, Object data)
    {
       return node.childrenAccept(this, data);
    }
 
 }
