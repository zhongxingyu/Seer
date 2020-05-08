 package com.lolcode.tree;
 
 import com.lolcode.lolcodeBaseVisitor;
 import com.lolcode.lolcodeParser;
 import org.antlr.v4.runtime.misc.NotNull;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/16/13
  * Time: 4:43 PM
  */
 
 /**
  * Builds AST from parse tree. Resulting tree is stored in root.
  */
 public class AstBuilder extends lolcodeBaseVisitor<TreeNode> {
     private TreeNode root;
     private static Logger log = Logger.getLogger(AstBuilder.class.getName());
     private final String filename;
 
     public AstBuilder(String filename) {
         this.filename = filename;
         root = null;
     }
 
     @Override
     public TreeNode visitFuncall(lolcodeParser.FuncallContext ctx) {
         //funcall is actually funexpr, stupid i know
         return visitChildren(ctx);
     }
 
     @Override
     public TreeConstant visitValue(@NotNull lolcodeParser.ValueContext ctx) {
         TreeConstant constant = new TreeConstant();
         if (ctx.BOOL() != null) {
             constant.setType(TreeTypedValue.TYPE.BOOL);
             constant.fromString(ctx.BOOL().toString());
         } else if (ctx.INT() != null) {
             constant.setType(TreeTypedValue.TYPE.INT);
             constant.fromString(ctx.INT().toString());
         } else if (ctx.FLOAT() != null) {
             constant.setType(TreeTypedValue.TYPE.FLOAT);
             constant.fromString(ctx.FLOAT().toString());
         } else if (ctx.STRING() != null) {
             constant.setType(TreeTypedValue.TYPE.STRING);
             constant.fromString(ctx.STRING().toString().replaceAll("\"", ""));
         } else {
             log.warning("All types for constant failed to parse");
             constant.setType(TreeTypedValue.TYPE.UNKNOWN);
         }
         return constant;
     }
 
     @Override
     public TreeFunctionParameter visitFormalParameter(lolcodeParser.FormalParameterContext ctx) {
         TreeFunctionParameter param = new TreeFunctionParameter();
         param.setName(ctx.ID().toString());
         return param;
     }
 
     @Override
     public TreeVarDeclStmt visitVarDecl(lolcodeParser.VarDeclContext ctx) {
         TreeVarDeclStmt varDeclStmt = new TreeVarDeclStmt();
         TreeVariable variable = new TreeVariable();
         variable.setName(ctx.ID().toString());
         if (ctx.expr() != null) {
             varDeclStmt.setInitialValue((TreeExpression) visit(ctx.expr()));
         }
         varDeclStmt.setVar(variable);
         return varDeclStmt;
     }
 
     @Override
     public TreeNode visitBlock(lolcodeParser.BlockContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeGimmehStmt visitGimstat(lolcodeParser.GimstatContext ctx) {
         TreeGimmehStmt gimmehStmt = new TreeGimmehStmt();
         TreeVariable var = new TreeVariable();
         var.setName(ctx.ID().toString());
         gimmehStmt.setVariable(var);
         return gimmehStmt;
     }
 
     @Override
     public TreeNode visitExpr(lolcodeParser.ExprContext ctx) {
         if (ctx.ID() != null) {
            //FIXME: something is wrong with grammar, BOOL values are treated as ID rather than as value
            if (ctx.ID().toString().equals("WIN") || ctx.ID().toString().equals("FAIL")) {
                TreeConstant constant = new TreeConstant();
                constant.setType(TreeTypedValue.TYPE.BOOL);
                constant.fromString(ctx.ID().toString());
                return constant;
            }
             TreeVariable variable = new TreeVariable();
             variable.setName(ctx.ID().toString());
             return variable;
         }
         return visitChildren(ctx);
     }
 
     @Override
     public TreeAssignStmt visitAssstat(lolcodeParser.AssstatContext ctx) {
         TreeAssignStmt assignStmt = new TreeAssignStmt();
         TreeVariable lhs = new TreeVariable();
         lhs.setName(ctx.ID().toString());
         assignStmt.setLhs(lhs);
         assignStmt.setRhs((TreeExpression) visit(ctx.expr()));
         return assignStmt;
     }
 
     @Override
     public TreeVisibleStmt visitVisstat(lolcodeParser.VisstatContext ctx) {
         TreeVisibleStmt visibleStmt = new TreeVisibleStmt();
         visibleStmt.setArgument((TreeExpression) visit(ctx.expr()));
         return visibleStmt;
     }
 
     @Override
     public TreeNotExpr visitNotexpr(lolcodeParser.NotexprContext ctx) {
         TreeNotExpr notExpr = new TreeNotExpr();
         notExpr.setExpr((TreeExpression) visit(ctx.expr()));
         return notExpr;
     }
 
     private class CasePair implements TreeNode {
         public TreeConstant constant;
         public List<TreeStatement> body;
 
         private CasePair(TreeConstant constant, List<TreeStatement> body) {
             this.constant = constant;
             this.body = body;
         }
 
         @Override
         public void accept(BaseASTVisitor v) {
         }
     }
 
     @Override
     public TreeCaseStmt visitCasestat(lolcodeParser.CasestatContext ctx) {
         TreeCaseStmt caseStmt = new TreeCaseStmt();
         TreeExpression expression = (TreeExpression) visit(ctx.expr());
         caseStmt.setVal(expression);
         for (lolcodeParser.CaseblockContext blk : ctx.caseblock()) {
             CasePair pair = (CasePair) visit(blk);
             caseStmt.addStatement(pair.constant, pair.body);
         }
         if (ctx.OMGWTF() != null) {
             for (lolcodeParser.StatContext stat : ctx.block().stat()) {
                 caseStmt.addDefaultStmt((TreeStatement) visit(stat));
             }
         }
         return caseStmt;
     }
 
     @Override
     public CasePair visitCaseblock(@NotNull lolcodeParser.CaseblockContext ctx) {
         TreeConstant constant = (TreeConstant) visit(ctx.value());
         ArrayList<TreeStatement> lst = new ArrayList<>();
         for (lolcodeParser.StatContext stat : ctx.block().stat()) {
             lst.add((TreeStatement) visit(stat));
         }
         return new CasePair(constant, lst);
     }
 
     @Override
     public TreeModule visitFile(lolcodeParser.FileContext ctx) {
         if (root != null) {
             log.log(Level.SEVERE, "visitFile() called more than once for same file!");
         }
         TreeModule module = new TreeModule();
         module.setModuleName(filename);
         if (!ctx.functionDecl().isEmpty()) {
             int i = 0;
             for (lolcodeParser.FunctionDeclContext fDecl : ctx.functionDecl()) {
                 module.addFunction((TreeFunction) visit(fDecl));
                 i++;
             }
             log.log(Level.INFO, "Got " + i + " functions in a module");
         }
         if (ctx.main() == null) {
             log.severe("No module body found in module.");
         } else {
             if (ctx.main().isEmpty()) {
                 log.info("No body statements found in module.");
             } else {
                 for (lolcodeParser.StatContext statement : ctx.main().stat()) {
                     module.addStatement((TreeStatement) visit(statement));
                 }
             }
         }
         root = module;
         return module;
     }
 
     @Override
     public TreeIfStmt visitIfstat(lolcodeParser.IfstatContext ctx) {
         TreeIfStmt ifStmt = new TreeIfStmt();
         List<lolcodeParser.ExprContext> exprs = ctx.expr();
         TreeExpression predicate = (TreeExpression) visit(exprs.get(0)); // first predicate is guaranteed to be present
         ifStmt.setCondition(predicate);
         for (lolcodeParser.StatContext stat : ctx.block(0).stat()) {
             ifStmt.addTrueStmt((TreeStatement) visit(stat));
         }
         if (ctx.MEBBE() != null) {                //add else if branches if present
             for (int i = 0; i < ctx.MEBBE().size(); ++i) {
                 TreeIfStmt tmp = new TreeIfStmt();
                 tmp.setCondition((TreeExpression) visit(ctx.expr().get(i + 1)));
                 for (lolcodeParser.StatContext stat : ctx.block().get(i + 1).stat()) {
                     tmp.addTrueStmt((TreeStatement) visit(stat));
                 }
                 ifStmt.addElseIf(tmp);
             }
         }
         if (ctx.NOWAI() != null) {            //add else branch if present
             for (lolcodeParser.StatContext stat : ctx.block().get(ctx.block().size() - 1).stat()) {
                 ifStmt.addFalseStmt((TreeStatement) visit(stat));
             }
         }
         return ifStmt;
     }
 
     @Override
     public TreeNode visitExprList(lolcodeParser.ExprListContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeOrExpr visitOneofexpr(lolcodeParser.OneofexprContext ctx) {
         TreeOrExpr orExpr = new TreeOrExpr();
         orExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         orExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return orExpr;
     }
 
     @Override
     public TreeLoopStmt visitLoopstat(lolcodeParser.LoopstatContext ctx) {
         TreeLoopStmt loopStmt = new TreeLoopStmt();
         loopStmt.setLabel(ctx.ID(0).toString());
         if (ctx.expr() != null) {         //not an infinite loop
             if (ctx.UPPIN() != null) {
                 loopStmt.setoType(TreeLoopStmt.opType.UPPUN);
             } else if (ctx.NERFIN() != null) {
                 loopStmt.setoType(TreeLoopStmt.opType.NERFIN);
             }
             TreeVariable loopVar = new TreeVariable();
             loopVar.setName(ctx.ID(1).toString());
             loopStmt.setVariable(loopVar);
             if (ctx.WHILE() != null) {
                 loopStmt.setlType(TreeLoopStmt.loopType.WHILE);
             } else {
                 loopStmt.setlType(TreeLoopStmt.loopType.TIL);
             }
             TreeExpression condition = (TreeExpression) visit(ctx.expr());
             loopStmt.setExitCondition(condition);
         }
         for (lolcodeParser.StatContext stat : ctx.block().stat()) {
             loopStmt.addStatement((TreeStatement) visit(stat));
         }
         return loopStmt;
     }
 
     @Override
     public TreeFuncCallStmt visitFunexpr(lolcodeParser.FunexprContext ctx) {
         TreeFuncCallStmt funcCallStmt = new TreeFuncCallStmt();
         funcCallStmt.setFuncName(ctx.ID().toString());
         if (ctx.exprList() == null) {
             log.info("function call " + funcCallStmt.getFuncName() + " has no parameters");
         } else {
             for (lolcodeParser.ExprContext param : ctx.exprList().expr()) {
                 funcCallStmt.addArgument((TreeExpression) visit(param));
             }
         }
         return funcCallStmt;
     }
 
     @Override
     public TreeMaxExpr visitMaxexpr(@NotNull lolcodeParser.MaxexprContext ctx) {
         TreeMaxExpr maxExpr = new TreeMaxExpr();
         maxExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         maxExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return maxExpr;
     }
 
     @Override
     public TreeMinExpr visitMinexpr(@NotNull lolcodeParser.MinexprContext ctx) {
         TreeMinExpr minExpr = new TreeMinExpr();
         minExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         minExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return minExpr;
     }
 
     @Override
     public TreeModExpr visitModexpr(lolcodeParser.ModexprContext ctx) {
         TreeModExpr modExpr = new TreeModExpr();
         modExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         modExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return modExpr;
     }
 
     @Override
     public TreeAndExpr visitBothofexpr(lolcodeParser.BothofexprContext ctx) {
         TreeAndExpr andExpr = new TreeAndExpr();
         andExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         andExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return andExpr;
     }
 
     @Override
     public TreeSumExpr visitAddexpr(lolcodeParser.AddexprContext ctx) {
         TreeSumExpr sumExpr = new TreeSumExpr();
         sumExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         sumExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return sumExpr;
     }
 
     @Override
     public TreeNode visitStat(lolcodeParser.StatContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeFunction visitFunctionDecl(lolcodeParser.FunctionDeclContext ctx) {
         TreeFunction func = new TreeFunction();
         func.setName(ctx.ID().toString());
         if (ctx.formalParameters() == null) {
             log.info("function " + func.getName() + " has no parameters");
         } else {
             for (lolcodeParser.FormalParameterContext param : ctx.formalParameters().formalParameter()) {
                 func.addParam((TreeFunctionParameter) visit(param));
             }
         }
         for (lolcodeParser.StatContext stat : ctx.block().stat()) {
             func.addStmt((TreeStatement) visit(stat));
         }
         return func;
     }
 
     @Override
     public TreeNode visitMain(lolcodeParser.MainContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeDivExpr visitDivexpr(@NotNull lolcodeParser.DivexprContext ctx) {
         TreeDivExpr divExpr = new TreeDivExpr();
         divExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         divExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return divExpr;
     }
 
     @Override
     public TreeStatement visitRetpart(lolcodeParser.RetpartContext ctx) {
         String tmp = ctx.getChild(0).getText();
         if (tmp.equals("GTFO")) {
             return new TreeBreakStmt();
         } else {
             TreeReturnStmt returnStmt = new TreeReturnStmt();
             returnStmt.setRetValue((TreeExpression) visit(ctx.expr()));
             return returnStmt;
         }
     }
 
     @Override
     public TreeNode visitFormalParameters(lolcodeParser.FormalParametersContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeSubExpr visitSubexpr(@NotNull lolcodeParser.SubexprContext ctx) {
         TreeSubExpr subExpr = new TreeSubExpr();
         subExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         subExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return subExpr;
     }
 
     @Override
     public TreeXorExpr visitEitherexpr(lolcodeParser.EitherexprContext ctx) {
         TreeXorExpr xorExpr = new TreeXorExpr();
         xorExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         xorExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return xorExpr;
     }
 
     @Override
     public TreeEqualExpr visitEquexpr(lolcodeParser.EquexprContext ctx) {
         TreeEqualExpr equalExpr = new TreeEqualExpr();
         equalExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         equalExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return equalExpr;
     }
 
     @Override
     public TreeNequalExpr visitNequexpr(@NotNull lolcodeParser.NequexprContext ctx) {
         TreeNequalExpr nequalExpr = new TreeNequalExpr();
         nequalExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         nequalExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return nequalExpr;
     }
 
     @Override
     public TreeMulExpr visitMultexpr(lolcodeParser.MultexprContext ctx) {
         TreeMulExpr mulExpr = new TreeMulExpr();
         mulExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         mulExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return mulExpr;
     }
 }
