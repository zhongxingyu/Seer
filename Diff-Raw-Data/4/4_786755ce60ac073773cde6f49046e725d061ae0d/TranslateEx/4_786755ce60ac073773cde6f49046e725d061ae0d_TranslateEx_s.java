 package minijava.translate.implementation;
 
 import minijava.ir.temp.Label;
 import minijava.ir.tree.IR;
 import minijava.ir.tree.IRExp;
 import minijava.ir.tree.IRStm;
 
 public class TranslateEx extends TranslateExp
 {
   private IRExp exp;
   
   public TranslateEx(IRExp e)
   {
     this.exp = e;
   }
   
   @Override
   public IRExp unEx()
   {
     return this.exp;
   }
   
   @Override
   public IRStm unNx()
   {
     return IR.EXP(this.exp);
   }
   
   @Override
   public IRStm unCx(Label t, Label f)
   {
     IRExp e = this.unEx();
     if(e.isCONST(1))
     {
       return IR.JUMP(t);
     }
     else if(e.isCONST(0))
     {
       return IR.JUMP(f);
     }
     
    return null;
   }
 }
