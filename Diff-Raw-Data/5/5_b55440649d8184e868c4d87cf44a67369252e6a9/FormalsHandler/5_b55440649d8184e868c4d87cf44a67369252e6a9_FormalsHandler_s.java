 package semant.first_pass.handlers;
 
 import semant.Env;
 import symbol.MethodInfo;
 import symbol.Symbol;
 import symbol.VarInfo;
 import syntaxtree.Formal;
 import syntaxtree.VisitorAdapter;
 
 public class FormalsHandler extends VisitorAdapter {
 
     private Env env;
     private MethodInfo info;
     
     private FormalsHandler(Env e, MethodInfo i) {
         super();
         env = e;
         info = i;
     }
 
     public static void firstPass(Env e, MethodInfo methodInfo, Formal formal) {
         FormalsHandler handler = new FormalsHandler(e, methodInfo);
         formal.accept(handler);        
     }
 
     public void visit(Formal node) {
         Symbol name = Symbol.symbol(node.name.s);
         VarInfo varInfo = new VarInfo(node.type, name);
         
         // inserindo nova declaracao na tabela de simbolos
         if (!info.addFormal(varInfo)) {
            VarInfo previousInfo = info.localsTable.get(name);
             env.err.Error(node.name, new Object[]{
                     "Parametro \'" + name 
                    + "\' do método \'" + info.name + "\' redefinido", 
                    "Usado anteriormente em: [" + previousInfo.type.line + "," + previousInfo.type.row + "]"});
         }
     }
 }
