 package com.lolcode.checker;
 
 import com.lolcode.tree.*;
 import com.lolcode.tree.exception.BaseAstException;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/21/13
  * Time: 7:33 PM
  */
 
 /**
  * Attempts to infer types for variables if possible.<p>
  * Is intrusive: overwrites types in AST.</br>
  * NOTE: since lolcode has dynamic typing we cannot explicitly set variable types from outer scope in conditional blocks.</br>
  * Conditional blocks are: if & switch.</br>
  * For example,</br> <pre>{@code
  * I HAS A VAR ITZ 123
  * ARG1, ORLY
  * YRLY
  *    VAR R WIN
  * NOWAI
  *    VAR R "STR"
  * OIC
  * }</pre>
  * VAR type thus cannot be determined at compile time and it should become of type UNKNOWN. So the rules are:
  * <ul><li>(scopeIsDirty && varScope == local) -> can overwrite type</li>
  * <li>(scopeIsDirty && varScope == upper) -> CANNOT overwrite type</li>
  * <li>(!scopeIsDirty && varScope == upper) -> can overwrite type</li>
  * <li>(!scopeIsDirty && varScope == local) -> can overwrite type</li></ul>
  * </br><p>
  * Generator also attempts to infer function return type if possible.</br>
  * If a function has multiple return statements which return different types, a warning is generated.
  * </p>
  * </p>
  */
 public class TypeGenerator implements BaseASTVisitor<TYPE> {
     Stack<Boolean> scopeIsDirty;
     HashSet<TYPE> funcReturnType = null; //dirty hack to get function return type, only works because lambdas and closures are not allowed in lolcode
     Stack<Set<TreeVariable>> scopes;
 
     public TypeGenerator() {
         scopes = new Stack<>();
         scopeIsDirty = new Stack<>();
     }
 
     TYPE inferBinaryExpr(TreeBinaryExpr expr, String exceptionMessage) throws BaseAstException {
         TYPE lType = expr.getLhs().accept(this);
         TYPE rType = expr.getRhs().accept(this);
         expr.getLhs().setType(lType);
         expr.getRhs().setType(rType);
         if ((lType == TYPE.UNKNOWN) || (rType == TYPE.UNKNOWN)) {
             return TYPE.UNKNOWN;
         }
         if (((lType == TYPE.INT) && (rType == TYPE.FLOAT)) || ((lType == TYPE.FLOAT) && (rType == TYPE.INT))) {
             expr.getLhs().setType(TYPE.FLOAT);
             expr.getRhs().setType(TYPE.FLOAT);
             return TYPE.FLOAT;
         }
         if (lType != rType) {
             ErrorHandler.castError(expr.getPos(), exceptionMessage, lType, rType);
         }
         return lType;
     }
 
     TYPE inferBinaryLogicExpr(TreeBinaryExpr expr, String exceptionMessage) throws BaseAstException {
         TYPE lType = expr.getLhs().accept(this);
         TYPE rType = expr.getRhs().accept(this);
         expr.getLhs().setType(lType);
         expr.getRhs().setType(rType);
         if ((lType == TYPE.UNKNOWN) && (rType == TYPE.UNKNOWN)) {
             return TYPE.UNKNOWN;
         }
         if (((lType == TYPE.BOOL) && (rType == TYPE.UNKNOWN)) || ((lType == TYPE.UNKNOWN) && (rType == TYPE.BOOL))) {
             return TYPE.UNKNOWN;
         }
         if ((rType == TYPE.BOOL) && (lType == TYPE.BOOL)) {
             return TYPE.BOOL;
         }
         ErrorHandler.castError(expr.getPos(), exceptionMessage, lType, rType);
         return TYPE.UNKNOWN;
     }
 
     @Override
     public TYPE visit(TreeFunction func) throws BaseAstException {
         funcReturnType = new HashSet<>(8); //magic number of enum size rounded to 8
         scopeIsDirty.push(false);
         HashSet<TreeVariable> current = new HashSet<>();
         current.addAll(func.getParams());
         scopes.push(current);
         for (TreeStatement statement : func.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         scopeIsDirty.pop();
         funcReturnType.remove(null);
         if (funcReturnType.size() == 1) { //only one return statement
             func.setType(funcReturnType.iterator().next());
         } else if (funcReturnType.size() > 1) {
             ErrorHandler.warnAmbiguousReturnType(func.getPos(), func);
         }
         return null;
     }
 
     @Override
     public TYPE visit(TreeModule module) throws BaseAstException {
         for (TreeFunction func : module.getFunctions()) {
             visit(func);
         }
         scopeIsDirty.push(false);
         HashSet<TreeVariable> current = new HashSet<>();
         scopes.push(current);
         for (TreeStatement statement : module.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         scopeIsDirty.pop();
         return TYPE.UNKNOWN;
     }
 
     @Override
     public TYPE visit(TreeFunctionParameter param) {
        return param.getType();
     }
 
     @Override
     public TYPE visit(TreeIfStmt ifStmt) throws BaseAstException {
         ifStmt.getCondition().accept(this);
         scopeIsDirty.push(true);
         HashSet<TreeVariable> current = new HashSet<>();
         scopes.push(current);
         for (TreeStatement statement : ifStmt.getTrueBranch()) {
             statement.accept(this);
         }
         scopes.pop();
         scopeIsDirty.pop();
         for (TreeIfStmt elseif : ifStmt.getElseIfs()) {
             visit(elseif);
         }
         scopeIsDirty.push(true);
         current = new HashSet<>();
         scopes.push(current);
         for (TreeStatement statement : ifStmt.getFalseBranch()) {
             statement.accept(this);
         }
         scopes.pop();
         scopeIsDirty.pop();
         return null;
     }
 
     @Override
     public TYPE visit(TreeLoopStmt loopStmt) throws BaseAstException {
         HashSet<TreeVariable> current = new HashSet<>();
         if (loopStmt.getVariable() != null) {
             loopStmt.getVariable().setType(loopStmt.weakref.getType());
             current.add(loopStmt.getVariable());
             scopes.push(current);
             loopStmt.getExitCondition().accept(this);
         } else {
             scopes.push(current);
         }
         scopeIsDirty.push(false);
         for (TreeStatement statement : loopStmt.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         scopeIsDirty.pop();
         return null;
     }
 
     @Override
     public TYPE visit(TreeAssignStmt assignStmt) throws BaseAstException {
         TYPE rhsType = assignStmt.getRhs().accept(this);
         TYPE tmp = (scopeIsDirty.peek() && !scopes.peek().contains(assignStmt.getLhs())) ? TYPE.UNKNOWN : rhsType;
         assignStmt.getLhs().setType(tmp);
         return rhsType;
     }
 
     @Override
     public TYPE visit(TreeCaseStmt caseStmt) throws BaseAstException {
         TYPE val = caseStmt.getVal().accept(this);
         if (val != TYPE.UNKNOWN) {
             for (TreeConstant constant : caseStmt.getBody().keySet()) {
                 if (constant.getType() != val) {
                     ErrorHandler.castError(constant.getPos(), "case", val, constant.getType());
                 }
             }
         }
         for (List<TreeStatement> branch : caseStmt.getBody().values()) {
             HashSet<TreeVariable> current = new HashSet<>();
             scopes.push(current);
             scopeIsDirty.push(true);
             for (TreeStatement statement : branch) {
                 statement.accept(this);
             }
             scopes.pop();
             scopeIsDirty.pop();
         }
         return null;
     }
 
     @Override
     public TYPE visit(TreeVarDeclStmt varDeclStmt) throws BaseAstException {
         TYPE type = (varDeclStmt.getInitialValue() != null) ? varDeclStmt.getInitialValue().accept(this) : TYPE.UNKNOWN;
         varDeclStmt.getVar().setType(type);
         scopes.peek().add(varDeclStmt.getVar());
         return type;
     }
 
     @Override
     public TYPE visit(TreeArrayDeclStmt arrayDeclStmt) throws BaseAstException {
         arrayDeclStmt.getArray().setType(TYPE.ARRAY);
         scopes.peek().add(arrayDeclStmt.getArray());
         return TYPE.ARRAY;
     }
 
     @Override
     public TYPE visit(TreeVisibleStmt visibleStmt) throws BaseAstException {
         return visibleStmt.getArgument().accept(this);
     }
 
     @Override
     public TYPE visit(TreeGimmehStmt gimmehStmt) {
         gimmehStmt.getVariable().setType(TYPE.UNKNOWN);
         return null;
     }
 
     @Override
     public TYPE visit(TreeDummyStmt dummyStmt) throws BaseAstException {
         return dummyStmt.getBody().accept(this);
     }
 
     @Override
     public TYPE visit(TreeFuncCallExpr funcCallStmt) {
         return funcCallStmt.getBoundFunction().getType();
     }
 
     @Override
     public TYPE visit(TreeBreakStmt breakStmt) {
         return null;
     }
 
     @Override
     //this is a rough workaround, see funcReturnType comment
     public TYPE visit(TreeReturnStmt returnStmt) throws BaseAstException {
         funcReturnType.add(returnStmt.getRetValue().accept(this));
         return null;
     }
 
     @Override
     public TYPE visit(TreeVariable variable) {
         return variable.getType();
     }
 
     @Override
     public TYPE visit(TreeConstant constant) {
         return constant.getType();
     }
 
     @Override
     public TYPE visit(TreeSumExpr sumExpr) throws BaseAstException {
         return inferBinaryExpr(sumExpr, "sum");
     }
 
     @Override
     public TYPE visit(TreeSubExpr subExpr) throws BaseAstException {
         return inferBinaryExpr(subExpr, "sub");
     }
 
     @Override
     public TYPE visit(TreeMulExpr mulExpr) throws BaseAstException {
         return inferBinaryExpr(mulExpr, "mul");
     }
 
     @Override
     public TYPE visit(TreeDivExpr divExpr) throws BaseAstException {
         return inferBinaryExpr(divExpr, "div");
     }
 
     @Override
     public TYPE visit(TreeModExpr modExpr) throws BaseAstException {
         return inferBinaryExpr(modExpr, "mod");
     }
 
     @Override
     public TYPE visit(TreeArrayPutStmt arrayPutStmt) throws BaseAstException {
         return null;
     }
 
     @Override
     public TYPE visit(TreeArrayGetExpr arrayGetExpr) throws BaseAstException {
         return TYPE.UNKNOWN;
     }
 
     @Override
     public TYPE visit(TreeMaxExpr maxExpr) throws BaseAstException {
         return inferBinaryExpr(maxExpr, "max");
     }
 
     @Override
     public TYPE visit(TreeMinExpr minExpr) throws BaseAstException {
         return inferBinaryExpr(minExpr, "min");
     }
 
     @Override
     public TYPE visit(TreeAndExpr andExpr) throws BaseAstException {
         return inferBinaryLogicExpr(andExpr, "and");
     }
 
     @Override
     public TYPE visit(TreeOrExpr orExpr) throws BaseAstException {
         return inferBinaryLogicExpr(orExpr, "or");
     }
 
     @Override
     public TYPE visit(TreeXorExpr xorExpr) throws BaseAstException {
         return inferBinaryLogicExpr(xorExpr, "xor");
     }
 
     @Override
     public TYPE visit(TreeNotExpr notExpr) throws BaseAstException {
         TYPE type = notExpr.getExpr().accept(this);
         if ((type == TYPE.UNKNOWN) || (type == TYPE.BOOL)) {
             return type;
         }
         ErrorHandler.castError(notExpr.getPos(), "not", type, TYPE.BOOL);
         return TYPE.UNKNOWN;
     }
 
     @Override
     public TYPE visit(TreeEqualExpr equalExpr) throws BaseAstException {
         inferBinaryExpr(equalExpr, "equal");
         return TYPE.BOOL;
     }
 
     @Override
     public TYPE visit(TreeNequalExpr nequalExpr) throws BaseAstException {
         inferBinaryExpr(nequalExpr, "nequal");
         return TYPE.BOOL;
     }
 }
