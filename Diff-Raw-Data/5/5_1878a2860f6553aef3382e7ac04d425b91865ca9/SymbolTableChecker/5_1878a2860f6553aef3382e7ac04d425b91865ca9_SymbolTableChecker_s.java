 package se.helino.mjc.symbol;
 
 import java.util.ArrayList;
 import se.helino.mjc.parser.*;
 
 public class SymbolTableChecker implements TypeVisitor {
     private ProgramTable table;
     private ClassTable currentClass;
     private MethodTable currentMethod;
     private ArrayList<String> errors = new ArrayList<String>();
 
     public SymbolTableChecker(ProgramTable table) {
        this.table = table; 
     }
 
     public void check(MJProgram n) throws MJTypeException {
         n.accept(this);
         if(errors.size() != 0)
             throw new MJTypeException(errors);
     }
 
     public void visit(MJProgram n) { 
         n.getMJMainClass().accept(this);
         for(MJClass c : n.getMJClasses()) {
             c.accept(this);
         }
     }
 
     public void visit(MJMainClass n) { 
         for(MJStatement s : n.getStatements()) {
             s.accept(this);
         }
     }
 
     public void visit(MJClass n) {
         currentClass = table.getClassTable(n.getId().getName());
         for(MJMethodDecl m : n.getMethods()) {
             m.accept(this);
         }
     }
 
     private boolean isAKnownType(MJType t) {
         String name = t.toString();
         if(name.equals("int") || name.equals("boolean") || name.equals("int[]"))
             return true;
         MJIdentifierType id = (MJIdentifierType) t;
         return table.isNameOfClass(id.getName());
     }
 
     public void visit(MJMethodDecl n) {
         String methodName = n.getId().getName();
         currentMethod = currentClass.getMethodTable(methodName);
         for(TypeNamePair param : currentMethod.getParams()) {
             if(!isAKnownType(param.getType()))
                 errors.add("Parameter " + param.getName() + " of method " + 
                            methodName + " has unknown type");
         }
         for(MJStatement s : n.getBody().getMJStatements()) {
             s.accept(this);
         }
         MJType retType = currentMethod.getReturnType();
         if(!isAKnownType(retType))
                 errors.add("Return type of method " + methodName +
                            " is unknown");
         MJType retExpType = n.getReturnExpression().accept(this);
         if(!retType.toString().equals(retExpType.toString()))
             errors.add("Declared type " + retType.toString() + 
                        " differ from returned type " + retExpType.toString() +
                        " for method " + methodName);
     }
     
     public void visit(MJPrint n) {
         MJType type = n.getExpression().accept(this);
         table.addPrintParameterType(n, type);
     }
     
     public void visit(MJIf n) {
         if(!(n.getCondition().accept(this) instanceof MJBooleanType))
             errors.add("The condifiton of an if statement must have type bool");
 
         n.getIfStatement().accept(this);
         n.getElseStatement().accept(this);
     }
 
     public void visit(MJAssign n) {
         MJType lhs = n.getId().accept(this);
         MJType rhs = n.getExpression().accept(this);
         if(!rhs.toString().equals(lhs.toString()))
             errors.add("Variable " + n.getId().getName() + " has type " +
                         lhs.toString() + ", but tried to assign it type " +
                         rhs.toString());
     }
 
     public void visit(MJArrayAssign n) {
         MJType lhs = n.getId().accept(this);
         if(!(lhs instanceof MJIntArrayType))
             errors.add("Can only do array assignment with integer arrays");
 
         MJType bracket = n.getBracketExpression().accept(this);
         if(!(bracket instanceof MJIntType))
             errors.add("Can only index an array with an integer");
 
         MJType rhs = n.getExpression().accept(this);
         if(!(rhs instanceof MJIntType))
             errors.add("Can only assign integers to an integer array");
     }
 
     public void visit(MJBlock n) {
         for(MJStatement s : n.getStatements()) {
             s.accept(this);
         }
     }
 
     public void visit(MJWhile n) {
         MJType cond = n.getCondition().accept(this);
         if(!(cond instanceof MJBooleanType))
             errors.add("Condition in while loop must be of type boolean");
         n.getStatement().accept(this);
     }
 
     private MJType getType(String name) {
         MJType type;
         if(currentMethod != null) {
             type = currentMethod.getType(name);
             if(type != null)
                 return type;
         }
         if(currentClass != null) {
             type = currentClass.getType(name);
             if(type != null)
                 return type;
         }
         type = table.getType(name);
         if(type != null)
             return type;
         return null;
     }
     
     public MJType visit(MJIdentifier n) {
         MJType type = getType(n.getName());
         if(type == null) {
             errors.add("Variable " + n.getName() + " has unknown type");
             return new MJUnknownType();
         }
         return type;
     }
 
     public MJType visit(MJIdentifierExp n) { 
         MJType type = getType(n.getName());
         if(type == null) {
             errors.add("Variable " + n.getName() + " has unknown type");
             return new MJUnknownType();
         }
         return type;
     }
     
     public MJType visit(MJCall n) { 
         String methodName = n.getMethodId().getName();
         MJType ret = n.getExpression().accept(this);
         if(!(ret instanceof MJIdentifierType)) {
             errors.add("Can only invoke methods on objects");
             return new MJUnknownType();
         }
 
         MJIdentifierType obj = (MJIdentifierType) ret;
         table.addCalleeType(n, obj);
 
         ClassTable ct = table.getClassTable(obj.getName());
         if(ct == null) {
             errors.add("Could not find class of object to invoke method " +
                        methodName + " on");
             return new MJUnknownType();
         }
 
         MethodTable mt = ct.getMethodTable(methodName);
         if(mt == null) {
             errors.add("Could not find method " + methodName + " on class " 
                        + ct.getName());
             return new MJUnknownType();
         }
 
         ArrayList<TypeNamePair> declaredParams = mt.getParams();
         ArrayList<MJExpression> passedParams = n.getParameters();
         if(declaredParams.size() < passedParams.size()) {
             errors.add("Called " + methodName + " with too few parameters");
             return mt.getReturnType();
         }
         else if(declaredParams.size() > passedParams.size()) {
             errors.add("Called " + methodName + " with too many parameters");
             return mt.getReturnType();
         }
 
         for(int i = 0; i < passedParams.size(); i++) {
             MJType type = passedParams.get(i).accept(this);
             MJType decType = declaredParams.get(i).getType();
             if(!type.toString().equals(decType.toString()))
                 errors.add("Supplied wrong type for parameter " + 
                             declaredParams.get(i).getName() + " for method " 
                             + methodName); 
         }
         return mt.getReturnType();
     }
 
     public MJType visit(MJAnd n) { 
         MJType lhs = n.getLeft().accept(this);
         if(!(lhs instanceof MJBooleanType))
             errors.add("Left hand side of && must have type boolean");
 
         MJType rhs = n.getRight().accept(this);
         if(!(rhs instanceof MJBooleanType))
             errors.add("Right hand side of && must have type boolean");
 
         return new MJBooleanType();
     }
 
     public MJType visit(MJLess n) { 
         MJType lhs = n.getLeft().accept(this);
         if(!(lhs instanceof MJIntType))
             errors.add("Left hand side of < must have integer type");
 
         MJType rhs = n.getRight().accept(this);
         if(!(rhs instanceof MJIntType))
             errors.add("Right hand side of < must have integer type");
 
         return new MJBooleanType();
     }
 
     public MJType visit(MJPlus n) { 
         MJType lhs = n.getLeft().accept(this);
         if(!(lhs instanceof MJIntType))
             errors.add("Left hand side of + must have integer type");
 
         MJType rhs = n.getRight().accept(this);
         if(!(rhs instanceof MJIntType))
             errors.add("Right hand side of + must have integer type");
 
         return new MJIntType();
     }
 
     public MJType visit(MJMinus n) { 
         MJType lhs = n.getLeft().accept(this);
         if(!(lhs instanceof MJIntType))
             errors.add("Left hand side of - must have integer type");
 
         MJType rhs = n.getRight().accept(this);
         if(!(rhs instanceof MJIntType))
             errors.add("Right hand side of - must have integer type");
 
         return new MJIntType();
     }
 
     public MJType visit(MJTimes n) { 
         MJType lhs = n.getLeft().accept(this);
         if(!(lhs instanceof MJIntType))
             errors.add("Left hand side of * must have integer type");
 
         MJType rhs = n.getRight().accept(this);
         if(!(rhs instanceof MJIntType))
             errors.add("Right hand side of * must have integer type");
 
         return new MJIntType();
     }
 
     public MJType visit(MJNot n) { 
         MJType exp = n.getExpression().accept(this);
         if(!(exp instanceof MJBooleanType)) 
             errors.add("Can only apply ! operator to boolean types");
         return new MJBooleanType();
     }
 
     public MJType visit(MJArrayLength n) {
         MJType exp = n.getExpression().accept(this);
         if(!(exp instanceof MJIntArrayType))
             errors.add("Only integer arrays have the property length");
 
         return new MJIntType();
     }
 
     public MJType visit(MJArrayLookup n) { 
         MJType lhs = n.getLeft().accept(this);
         if(!(lhs instanceof MJIntArrayType))
             errors.add("Can only do array lookup on integer arrays");
 
         MJType rhs = n.getRight().accept(this);
         if(!(rhs instanceof MJIntType))
             errors.add("Can only use integer types as index in array lookup");
 
         return new MJIntType();
     }
 
 
     public MJType visit(MJNewObject n) { 
         MJType mjClass = n.getId().accept(this);
         return mjClass;
     }
 
     public MJType visit(MJNewArray n) { 
         MJType exp = n.getExpression().accept(this);
         if(!(exp instanceof MJIntType))
             errors.add("Can only use integer types to specify length of array");
 
         return new MJIntArrayType();
     }
 
     public MJType visit(MJTrue n) { 
         return new MJBooleanType();
     }
 
     public MJType visit(MJFalse n) {
         return new MJBooleanType();
     }
 
     public MJType visit(MJIntegerLiteral n) { 
         return new MJIntType();
     }
 
     public MJType visit(MJThis n) { 
         if(currentClass == null) {
             return new MJUnknownType();
         }
         return new MJIdentifierType(currentClass.getName());
     }
 }
