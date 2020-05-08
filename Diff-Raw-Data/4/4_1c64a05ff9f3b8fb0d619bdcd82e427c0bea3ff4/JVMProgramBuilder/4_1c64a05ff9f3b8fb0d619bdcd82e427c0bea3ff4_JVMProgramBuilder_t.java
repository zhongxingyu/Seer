 package se.helino.mjc.backends.jvm;
 
 import se.helino.mjc.parser.*;
 import se.helino.mjc.symbol.*;
 import se.helino.mjc.frame.vm.*;
 
 public class JVMProgramBuilder implements IntVisitor {
     private ProgramTable symbolTable;
     private ClassTable currentClass;
 
     public JVMProgramBuilder(ProgramTable symbolTable) {
         this.symbolTable = symbolTable;
     }
 
     public int visit(MJProgram n) { 
         n.getMJMainClass().accept(this);
         for(MJClass c : n.getMJClasses()) {
             c.accept(this);
         }
         return 0;
     }
 
     public int visit(MJMainClass n) {
         int limit = 0;
         for(MJStatement s : n.getStatements()) {
             int tmp = s.accept(this);
             limit = Math.max(tmp, limit);
         }
         symbolTable.setMainStackLimit(limit);
         return 0;
     }
 
     public int visit(MJClass n) {
         JVMRecord rec = new JVMRecord();
         for(MJVarDecl vd : n.getVariableDeclarations()) {
             String name = vd.getId().getName();
             rec.addAccess(name, new JVMField(name, n.getId().getName(),
                                              vd.getMJType()));
         }
         currentClass = symbolTable.getClassTable(n.getId().getName());
         for(MJMethodDecl m : n.getMethods()) {
             m.accept(this);
         }
         currentClass.setVMRecord(rec);
         return 0;
     }
     
     public int visit(MJMethodDecl n) {
         JVMFrame frame = new JVMFrame();
         frame.addParameter("this", 
                 new JVMLocal(new MJIdentifierType(currentClass.getName())));
         int num = 1;
         for(MJMethodArg arg : n.getArguments()) {
             String name = arg.getId().getName();
             frame.addParameter(name, new JVMLocal(name, arg.getMJType(), num));
             num++;
         }
         for(MJVarDecl vd : n.getBody().getMJVariableDeclarations()) {
             String name = vd.getId().getName();
             frame.addParameter(name, new JVMLocal(name, vd.getMJType(), num));
             num++;
         }
         int limit = 0;
         for(MJStatement s : n.getBody().getMJStatements()) {
             int tmp = s.accept(this);
             limit = Math.max(tmp, limit);
         }
         int ret = n.getReturnExpression().accept(this);
         frame.setStackLimit(Math.max(ret, limit));
         currentClass.getMethodTable(n.getId().getName()).setVMFrame(frame);
         return 0;
     }
 
     public int visit(MJIdentifier n) { 
         return 1;
     }
     public int visit(MJAssign n) {
         int exp = n.getExpression().accept(this);
         return exp + 1;
     }
 
     public int visit(MJArrayAssign n) {
         int exp = n.getExpression().accept(this);
         int bracket = n.getBracketExpression().accept(this);
         return Math.max(bracket + 1, exp + 2);
     }
     public int visit(MJIf n) {
         int cond = n.getCondition().accept(this);
         int t = n.getIfStatement().accept(this);
         int f = n.getElseStatement().accept(this);
         int tmp = Math.max(t, f);
         return Math.max(cond, tmp);
     }
     public int visit(MJBlock n) {
         int max = 0;
         for(MJStatement s : n.getStatements()) {
             int val = s.accept(this);
             max = Math.max(val, max);
         }
         return max;
     }
     public int visit(MJWhile n) {
         int cond = n.getCondition().accept(this);
         int body = n.getStatement().accept(this);
         return Math.max(cond, body);
     }
 
     public int visit(MJPrint n) { 
         int exp = n.getExpression().accept(this);
         return exp + 1;
     }
 
     private int binaryExp(MJBinaryExpression n) {
         int left = n.getLeft().accept(this);
         int right = n.getRight().accept(this);
         if(left > right)
             return left;
         else
             return right + 1;
     }
 
     public int visit(MJAnd n){ return binaryExp(n); }
 
     public int visit(MJLess n) { return binaryExp(n); }
 
     public int visit(MJPlus n) { return binaryExp(n); }
 
     public int visit(MJMinus n) { return binaryExp(n); }
 
     public int visit(MJTimes n) { return binaryExp(n); }
     
     public int visit(MJArrayLookup n) { return binaryExp(n); }
 
     public int visit(MJNot n) {
         int limit = n.getExpression().accept(this);
         return limit + 1;
     }
 
     public int visit(MJCall n) {
         int callee = n.getExpression().accept(this);
         int limit = 1; 
         int pos = 0;
         for(MJExpression e : n.getParameters()) {
             int tmp = e.accept(this);
             limit = Math.max(limit, tmp + pos + limit);
             pos++;
         }
         return Math.max(limit, callee);
     }
 
     public int visit(MJIdentifierExp n) { 
         if(currentClass.getType(n.getName()) != null) // It is a field
             return 2;
         return 1; 
     }
    public int visit(MJArrayLength n){ 
        return n.getExpression().accept(this);
    }
     public int visit(MJNewArray n){ return 1; }
     public int visit(MJTrue n) { return 1; }
     public int visit(MJFalse n) { return 1; }
     public int visit(MJIntegerLiteral n) { return 1; }
     public int visit(MJThis n) { return 1; }
 
     public int visit(MJNewObject n) { return 2; }
     public int visit(MJVarDecl n) { return 0; }
     public int visit(MJIntType n){ return 0; }
     public int visit(MJIntArrayType n){ return 0; }
     public int visit(MJBooleanType n){ return 0; }
     public int visit(MJIdentifierType n){ return 0; }
     public int visit(MJMethodArg n){ return 0; }
 }
