 package codemate.Fortran;
 
 /**
  * FortranRewriter
  *
  * This class will rewrite the Fortran code that has been parsed according to
  * some standand style.
  *
  * @author      Li Dong <dongli@lasg.iap.ac.cn>
  */
 
 import org.antlr.v4.runtime.tree.*;
 
 import codemate.Fortran.FortranParser.*;
 import codemate.ui.*;
 
 public class FortranRewriter extends FortranBaseVisitor<Void> {
     private int currentIndentLevel;
     private int indentWidth;
     private int maxLineLength;
     private int optimalLineLength;
     private String newCode;
 
     public FortranRewriter() {
         currentIndentLevel = 0;
         indentWidth = 4;
         maxLineLength = 132;
         optimalLineLength = 80;
         newCode = "";
     }
 
     public String getNewCode() { return newCode; }
 
     /**
      * appendCode
      *
      * This method appends the code to newCode and take care with the line
      * length. When the line column number exceeds optimalLineLength, then cut
      * it off with break line character. If the maxLineLength has been
      * exceeded, then report an error, since the Fortran compiler may also
      * complain.
      *
      * @param       code        The code to be appended
      */
     private void appendCode(String code) {
         int loc = newCode.lastIndexOf("\n");
         int lineLength = 0;
         if (loc != -1) {
         	lineLength = newCode.length()-loc;
         } else {
         	lineLength = newCode.length();
         }
     	if (lineLength > maxLineLength) {
     		UI.error("FortranRewriter",
     				"Maximum column "+optimalLineLength+" has been exceeded!");
     	}
         if (lineLength+code.length() > optimalLineLength &&
         		!code.matches(" *(\\n|\\)) *")) {
         	newCode += " &\n";
         	increaseIndentLevel();
         	indent();
         	decreaseIndentLevel();
         }
     	newCode += code;
     }
 
     /**
      * rewrite
      *
      * This method call cascade visit methods to rewrite the parse tree.
      *
      * @param       tree        The input parse tree.
      *
      * @author      Li Dong <dongli@lasg.iap.ac.cn>
      */
     public void rewrite(ParseTree tree) { visit(tree); }
 
     /**
      * indent
      *
      * This method adds indenting spaces into the newCode according to the
      * currentIndentLevel and indentWidth.
      *
      * @author      Li Dong <dongli@lasg.iap.ac.cn>
      */
     private void indent() {
         for (int i = 0; i < currentIndentLevel*indentWidth; ++i)
             newCode += " ";
     }
 
     private void increaseIndentLevel() { ++currentIndentLevel; }
 
     private void decreaseIndentLevel() { --currentIndentLevel; }
 
     // -------------------------------------------------------------------------
     // visit methods
     public Void visitId(IdContext ctx) {
     	appendCode(ctx.getText());
         return null;
     }
     
     public Void visitNumerics(NumericsContext ctx) {
     	appendCode(ctx.getText());
     	return null;
     }
     
     public Void visitLiteralArray(LiteralArrayContext ctx) {
     	appendCode(ctx.ARRAY_START().getText());
     	visitExpression(ctx.expression(0));
     	for (int i = 1; i < ctx.expression().size(); ++i) {
     		appendCode(",");
     		visitExpression(ctx.expression(i));
     	}
     	appendCode(ctx.ARRAY_END().getText());
     	return null;
     }
 
     public Void visitExpression(ExpressionContext ctx) {
         if (ctx.LEFT_PAREN() != null)
             appendCode("(");
         switch (ctx.expression().size()) {
         case 0:
             if (ctx.id() != null)
             	visitId(ctx.id());
             else if (ctx.numerics() != null)
             	visitNumerics(ctx.numerics());
             else if (ctx.idWithArgs() != null)
             	visitIdWithArgs(ctx.idWithArgs());
             else if (ctx.derivedDataMember() != null)
             	visitDerivedDataMember(ctx.derivedDataMember());
             else if (ctx.templateInstance() != null)
             	visitTemplateInstance(ctx.templateInstance());
             else if (ctx.literalArray() != null)
             	visitLiteralArray(ctx.literalArray());
             else
                 appendCode(ctx.getText());
             break;
         case 1:
             if (ctx.notOperator() != null)
                 appendCode(".not. ");
             else if (ctx.MINUS() != null)
                 appendCode("-");
             visitExpression(ctx.expression(0));
             break;
         case 2:
             visitExpression(ctx.expression(0));
             if (ctx.expOperator() != null)
                 appendCode("**");
             else if (ctx.catOperator() != null)
                 appendCode("//");
             else if (ctx.arithmeticOperator1() != null)
                 appendCode(ctx.arithmeticOperator1().getText());
             else if (ctx.arithmeticOperator2() != null)
                 appendCode(ctx.arithmeticOperator2().getText());
             else if (ctx.compareOperator() != null)
                 appendCode(" "+ctx.compareOperator().getText()+" ");
             else if (ctx.andOrOperator() != null)
                 appendCode(" "+ctx.andOrOperator().getText()+" ");
             visitExpression(ctx.expression(1));
             break;
         }
         if (ctx.RIGHT_PAREN() != null)
             appendCode(")");
         return null;
     }
 
     public Void visitRangeTail(RangeTailContext ctx) {
         if (ctx.DOUBLE_COLONS() == null) {
             appendCode(":");
             if (ctx.expression().size() > 0)
                 visitExpression(ctx.expression(0));
             if (ctx.expression().size() > 1) {
                 appendCode(":");
                 visitExpression(ctx.expression(1));
             }
         } else {
             appendCode("::");
             if (ctx.expression().size() > 0)
                 visitExpression(ctx.expression(0));
         }
         return null;
     }
 
     public Void visitSubscriptRange(SubscriptRangeContext ctx) {
         if (ctx.STAR() == null) {
             if (ctx.expression() != null) {
                 visitExpression(ctx.expression());
                 if (ctx.rangeTail() != null)
                     visitRangeTail(ctx.rangeTail());
             } else
                 visitRangeTail(ctx.rangeTail());
         } else
             appendCode("*");
         return null;
     }
 
     public Void visitSubscriptRanges(SubscriptRangesContext ctx) {
         visitSubscriptRange(ctx.subscriptRange(0));
         for (int i = 1; i < ctx.subscriptRange().size(); ++i) {
             appendCode(", ");
             visitSubscriptRange(ctx.subscriptRange(i));
         }
         return null;
     }
 
     public Void visitIdWithArgs(IdWithArgsContext ctx) {
         visitId(ctx.id());
         appendCode("(");
         if (ctx.actualArguments() != null)
             visitActualArguments(ctx.actualArguments());
         else if (ctx.subscriptRanges() != null)
             visitSubscriptRanges(ctx.subscriptRanges());
         appendCode(")");
         return null;
     }
 
     public Void visitKeywordStatementParameters(KeywordStatementParametersContext ctx) {
         appendCode("(");
         visitActualArguments(ctx.actualArguments());
         appendCode(")");
         return null;
     }
 
     public Void visitKeywordStatement1(KeywordStatement1Context ctx) {
         appendCode(ctx.EXECUTABLE_KEYWORD_1().getText());
         visitKeywordStatementParameters(ctx.keywordStatementParameters());
         return null;
     }
     
     public Void visitKeywordStatement2(KeywordStatement2Context ctx) {
         appendCode(ctx.EXECUTABLE_KEYWORD_2().getText());
         if (ctx.keywordStatementParameters() != null)
             visitKeywordStatementParameters(ctx.keywordStatementParameters());
         if (ctx.actualArguments() != null) {
         	appendCode(" ");
         	visitActualArguments(ctx.actualArguments());
         }
         return null;
     }
     
     public Void visitKeywordStatement3(KeywordStatement3Context ctx) {
         appendCode(ctx.EXECUTABLE_KEYWORD_3().getText());
         appendCode(" ");
         if (ctx.id() != null)
         	visitId(ctx.id());
         else if (ctx.numerics() != null)
         	visitNumerics(ctx.numerics());
         return null;
     }
     
     public Void visitKeywordStatement(KeywordStatementContext ctx) {
         if (ctx.keywordStatement1() != null)
         	visitKeywordStatement1(ctx.keywordStatement1());
         else if (ctx.keywordStatement2() != null)
         	visitKeywordStatement2(ctx.keywordStatement2());
         else if (ctx.keywordStatement3() != null)
         	visitKeywordStatement3(ctx.keywordStatement3());
         return null;
     }
 
     public Void visitAssignmentStatement(AssignmentStatementContext ctx) {
         if (ctx.idWithArgs() != null)
             visitIdWithArgs(ctx.idWithArgs());
         else if (ctx.templateInstance() != null)
             visitTemplateInstance(ctx.templateInstance());
         else if (ctx.derivedDataMember() != null)
             visitDerivedDataMember(ctx.derivedDataMember());
         else if (ctx.id() != null)
             visitId(ctx.id());
         if (ctx.EQUAL() != null)
         	appendCode(" = ");
         else if (ctx.POINT() != null)
         	appendCode(" => ");
         visitExpression(ctx.expression());
         return null;
     }
 
     public Void visitDoRangeStatement(DoRangeStatementContext ctx) {
         appendCode("do ");
         visitId(ctx.id());
         appendCode(" = ");
         visitExpression(ctx.expression(0));
         appendCode(", ");
         visitExpression(ctx.expression(1));
         if (ctx.expression().size() == 3) {
             appendCode(", ");
             visitExpression(ctx.expression(2));
         }
         appendCode("\n");
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         indent();
         appendCode("end do");
         return null;
     }
 
     public Void visitDoWhileStatement(DoWhileStatementContext ctx) {
         appendCode("do ");
         appendCode("while ");
         visitExpression(ctx.expression());
         appendCode("\n");
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         indent();
         appendCode("end do");
         return null;
     }
 
     public Void visitDoAnonyStatement(DoAnonyStatementContext ctx) {
         appendCode("do ");
         appendCode("\n");
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         indent();
         appendCode("end do");
         return null;
     }
 
     public Void visitDoStatement(DoStatementContext ctx) {
         if (ctx instanceof DoRangeStatementContext)
             visitDoRangeStatement((DoRangeStatementContext) ctx);
         else if (ctx instanceof DoWhileStatementContext)
             visitDoWhileStatement((DoWhileStatementContext) ctx);
         else if (ctx instanceof DoAnonyStatementContext)
             visitDoAnonyStatement((DoAnonyStatementContext) ctx);
         return null;
     }
 
     public Void visitExecutableStatements(ExecutableStatementsContext ctx) {
     	if (ctx.children == null) return null;
     	int loc = 1;
     	for (ParseTree child : ctx.children) {
     		if (child instanceof ExecutableStatementContext) {
     			visitExecutableStatement((ExecutableStatementContext) child);
     			if (ctx.getParent().getRuleIndex() == FortranParser.RULE_procedure &&
     		        loc == ctx.children.size())
     				appendCode("\n");
     		} else if (child instanceof CppDirectiveContext) {
         		if (ctx.getParent().getRuleIndex() == FortranParser.RULE_procedure &&
         	        loc == ctx.children.size())
         			appendCode("\n");
     			visitCppDirective((CppDirectiveContext) child);
     		}
     		loc++;
     	}
         return null;
     }
 
     public Void visitExecutableStatement(ExecutableStatementContext ctx) {
         indent();
         if (ctx.assignmentStatement() != null)
             visitAssignmentStatement(ctx.assignmentStatement());
         else if (ctx.ifStatement() != null)
             visitIfStatement(ctx.ifStatement());
         else if (ctx.doStatement() != null)
             visitDoStatement(ctx.doStatement());
         else if (ctx.selectStatement() != null)
             visitSelectStatement(ctx.selectStatement());
         else if (ctx.keywordStatement() != null)
             visitKeywordStatement(ctx.keywordStatement());
         else if (ctx.templateInstance() != null)
             visitTemplateInstance(ctx.templateInstance());
         appendCode("\n");
         return null;
     }
 
     public Void visitElseIfStatement(ElseIfStatementContext ctx) {
         indent();
         appendCode("else if ");
         visitExpression(ctx.expression());
         appendCode(" then\n");
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         return null;
     }
 
     public Void visitElseStatement(ElseStatementContext ctx) {
         indent();
         appendCode("else\n");
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         return null;
     }
 
     public Void visitIfMultipleStatements(IfMultipleStatementsContext ctx) {
         appendCode("if ");
         visitExpression(ctx.expression());
         appendCode(" then\n");
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         for (ElseIfStatementContext elseIf : ctx.elseIfStatement())
             visitElseIfStatement(elseIf);
         if (ctx.elseStatement() != null)
             visitElseStatement(ctx.elseStatement());
         indent();
         appendCode("end if");
         return null;
     }
 
     public Void visitIfSingleStatement(IfSingleStatementContext ctx) {
     	appendCode("if ");
     	visitExpression(ctx.expression());
     	appendCode(" ");
     	visitExecutableStatement(ctx.executableStatement());
     	return null;
     }
     
     public Void visitIfStatement(IfStatementContext ctx) {
         if (ctx instanceof IfMultipleStatementsContext)
             visitIfMultipleStatements((IfMultipleStatementsContext) ctx);
         else if (ctx instanceof IfSingleStatementContext)
             visitIfSingleStatement((IfSingleStatementContext) ctx);
         return null;
     }
 
     public Void visitActualArgument(ActualArgumentContext ctx) {
         if (ctx.expression() != null)
             visitExpression(ctx.expression());
         else if (ctx.id() != null) {
             visitId(ctx.id());
             appendCode(" = ");
             visitActualArgument(ctx.actualArgument());
         } else if (ctx.STAR() != null)
             appendCode("*");
         return null;
     }
 
     public Void visitActualArguments(ActualArgumentsContext ctx) {
         visitActualArgument(ctx.actualArgument(0));
         for (int i = 1; i < ctx.actualArgument().size(); ++i) {
             appendCode(", ");
             visitActualArgument(ctx.actualArgument(i));
         }
         return null;
     }
 
     public Void visitMember(MemberContext ctx) {
         if (ctx.id() != null)
             visitId(ctx.id());
         else if (ctx.idWithArgs() != null)
             visitIdWithArgs(ctx.idWithArgs());
         return null;
     }
 
     public Void visitDerivedDataMember(DerivedDataMemberContext ctx) {
         visitMember(ctx.member(0));
         for (int i = 1; i < ctx.member().size(); ++i) {
             appendCode("%");
             visitMember(ctx.member(i));
         }
         return null;
     }
 
     public Void visitCaseLabels(CaseLabelsContext ctx) {
         indent();
         appendCode("case (");
         visitExpression(ctx.expression(0));
         for (int i = 1; i < ctx.expression().size(); ++i) {
             appendCode(", ");
             visitExpression(ctx.expression(i));
         }
         appendCode(")\n");
         return null;
     }
 
     public Void visitCaseStatement(CaseStatementContext ctx) {
         visitCaseLabels(ctx.caseLabels());
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         return null;
     }
 
     public Void visitCaseDefaultStatement(CaseDefaultStatementContext ctx) {
         indent();
         appendCode("case default\n");
         increaseIndentLevel();
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         return null;
     }
 
     public Void visitSelectStatement(SelectStatementContext ctx) {
         appendCode("select case (");
         visitExpression(ctx.expression());
         appendCode(")\n");
         for (CaseStatementContext caseStmt : ctx.caseStatement())
             visitCaseStatement(caseStmt);
         if (ctx.caseDefaultStatement() != null)
             visitCaseDefaultStatement(ctx.caseDefaultStatement());
         indent();
         appendCode("end select\n");
         return null;
     }
 
     public Void visitDummyArguments(DummyArgumentsContext ctx) {
         visitId(ctx.id(0));
         for (int i = 1; i < ctx.id().size(); ++i) {
             appendCode(", ");
             visitId(ctx.id(i));
         }
         return null;
     }
 
     public Void visitIntrinsicTypeParameter(IntrinsicTypeParameterContext ctx) {
     	appendCode("(");
     	if (ctx.EQUAL() != null) {
     		visitId(ctx.id(0));
     		appendCode("=");
     	}
     	if (ctx.numerics() != null)
     		visitNumerics(ctx.numerics());
     	else if (ctx.id().size() > 0) {
     		if (ctx.id().size() == 1)
     			visitId(ctx.id(0));
     		else if (ctx.id().size() == 2)
     			visitId(ctx.id(1));
     	} else if (ctx.STAR() != null)
     		appendCode("*");
     	appendCode(")");
     	return null;
     }
     
     public Void visitIntrinsicType(IntrinsicTypeContext ctx) {
         appendCode(ctx.INTRINSIC_TYPE_KEYWORD().getText());
         if (ctx.intrinsicTypeParameter() != null)
         	visitIntrinsicTypeParameter(ctx.intrinsicTypeParameter());
         return null;
     }
 
     public Void visitDerivedTypeName(DerivedTypeNameContext ctx) {
     	if (ctx.id() != null)
             visitId(ctx.id());
         else if (ctx.templateInstance() != null)
             visitTemplateInstance(ctx.templateInstance());
     	return null;
     }
     
     public Void visitDerivedType(DerivedTypeContext ctx) {
         if (ctx.TYPE_KEYWORD() != null)
     		appendCode("type(");
         else if (ctx.CLASS_KEYWORD() != null)
         	appendCode("class(");
         visitDerivedTypeName(ctx.derivedTypeName());
         appendCode(")");
         return null;
     }
 
     public Void visitDataAttribute(DataAttributeContext ctx) {
         appendCode(ctx.getText());
         return null;
     }
 
     public Void visitDataAttributes(DataAttributesContext ctx) {
         for (DataAttributeContext dataAttr : ctx.dataAttribute()) {
             appendCode(", ");
             visitDataAttribute(dataAttr);
         }
         appendCode(" ::");
         return null;
     }
 
     public Void visitData(DataContext ctx) {
         if (ctx.assignmentStatement() != null)
             visitAssignmentStatement(ctx.assignmentStatement());
         else if (ctx.idWithArgs() != null)
             visitIdWithArgs(ctx.idWithArgs());
         else if (ctx.id() != null)
             visitId(ctx.id());
         return null;
     }
 
     public Void visitDataList(DataListContext ctx) {
         visitData(ctx.data(0));
         for (int i = 1; i < ctx.data().size(); ++i) {
             appendCode(", ");
             visitData(ctx.data(i));
         }
         return null;
     }
 
     public Void visitDataDeclarationStatement(DataDeclarationStatementContext ctx) {
         if (ctx.intrinsicType() != null)
             visitIntrinsicType(ctx.intrinsicType());
         else if (ctx.derivedType() != null)
             visitDerivedType(ctx.derivedType());
         if (ctx.dataAttributes() != null)
             visitDataAttributes(ctx.dataAttributes());
         appendCode(" ");
         visitDataList(ctx.dataList());
         return null;
     }
     
     public Void visitDataDeclarationStatements(DataDeclarationStatementsContext ctx) {
     	for (int i = 0; i < ctx.dataDeclarationStatement().size(); ++i) {
     		indent();
     		visitDataDeclarationStatement(ctx.dataDeclarationStatement(i));
     		appendCode("\n");
     	}
     	return null;
     }
     
     public Void visitExtendsAttribute(ExtendsAttributeContext ctx) {
     	appendCode("extends(");
 		if (ctx.templateInstance() != null)
 			visitTemplateInstance(ctx.templateInstance());
 		else if (ctx.id() != null)
 			visitId(ctx.id());
 		appendCode(")");
 		return null;
     }
     
     public Void visitTypeAttribute(TypeAttributeContext ctx) {
     	if (ctx.extendsAttribute() != null) {
     		visitExtendsAttribute(ctx.extendsAttribute());
     	} else if (ctx.PUBLIC_KEYWORD() != null)
     		appendCode("public");
     	else if (ctx.PRIVATE_KEYWORD() != null)
     		appendCode("private");
     	else if (ctx.ABSTRACT_KEYWORD() != null)
     		appendCode("abstract");
     	return null;
     }
     
     public Void visitTypeAttributes(TypeAttributesContext ctx) {
     	for (TypeAttributeContext typeAttr : ctx.typeAttribute()) {
     		appendCode(", ");
     		visitTypeAttribute(typeAttr);
     	}
     	appendCode(" ::");
     	return null;
     }
     
     public Void visitBindingAttribute(BindingAttributeContext ctx) {
     	appendCode(ctx.getText());
     	return null;
     }
     
     public Void visitBindingAttributes(BindingAttributesContext ctx) {
     	for (int i = 0; i < ctx.bindingAttribute().size(); ++i) {
     		appendCode(", ");
     		visitBindingAttribute(ctx.bindingAttribute(i));
     	}
     	appendCode(" :: ");
     	return null;
     }
     
     public Void visitTypeBoundProcedureStatement(TypeBoundProcedureStatementContext ctx) {
     	indent();
     	appendCode("procedure");
     	if (ctx.bindingAttributes() != null)
     		visitBindingAttributes(ctx.bindingAttributes());
     	visitId(ctx.id(0));
     	if (ctx.POINT() != null) {
     		appendCode(" => ");
     		visitId(ctx.id(1));
    		for (int i = 2; i < ctx.id().size(); ++i) {
    			appendCode(", ");
    			visitId(ctx.id(i));
    		}
     	}
     	appendCode("\n");
     	return null;
     }
     
     public Void visitTypeBoundProcedureStatements(TypeBoundProcedureStatementsContext ctx) {
     	increaseIndentLevel();
     	for (int i = 0; i < ctx.typeBoundProcedureStatement().size(); ++i)
     		visitTypeBoundProcedureStatement(ctx.typeBoundProcedureStatement(i));
     	decreaseIndentLevel();
     	return null;
     }
     
     public Void visitContainedTypeBoundProcedures(ContainedTypeBoundProceduresContext ctx) {
     	indent();
     	appendCode("contains\n");
     	visitTypeBoundProcedureStatements(ctx.typeBoundProcedureStatements());
     	return null;
     }
     
     public Void visitTypeDeclarationStatement(TypeDeclarationStatementContext ctx) {
     	appendCode("type");
     	if (ctx.typeAttributes() != null)
     		visitTypeAttributes(ctx.typeAttributes());
     	appendCode(" ");
     	visitId(ctx.id(0));
     	appendCode("\n");
     	increaseIndentLevel();
     	visitDataDeclarationStatements(ctx.dataDeclarationStatements());
     	decreaseIndentLevel();
     	if (ctx.containedTypeBoundProcedures() != null)
     		visitContainedTypeBoundProcedures(ctx.containedTypeBoundProcedures());
     	indent();
     	appendCode("end type ");
     	visitId(ctx.id(0));
     	return null;
     }
     
     public Void visitNamelistParameters(NamelistParametersContext ctx) {
     	visitId(ctx.id(0));
     	for (int i = 1; i < ctx.id().size(); ++i) {
     		appendCode(", ");
     		visitId(ctx.id(i));
     	}
     	return null;
     }
     
     public Void visitNamelistStatement(NamelistStatementContext ctx) {
     	appendCode("namelist /");
     	visitId(ctx.id());
     	appendCode("/ ");
     	visitNamelistParameters(ctx.namelistParameters());
     	appendCode("\n");
     	return null;
     }
     
     public Void visitModuleProcedure(ModuleProcedureContext ctx) {
     	indent();
     	appendCode("module procedure ");
     	visitId(ctx.id());
     	appendCode("\n");
     	return null;
     }
     
     public Void visitInterfaceStatement(InterfaceStatementContext ctx) {
     	appendCode("interface");
     	if (ctx.id().size() > 0) {
     		appendCode(" ");
     		visitId(ctx.id(0));
     	}
     	appendCode("\n");
     	increaseIndentLevel();
     	for (ModuleProcedureContext mp : ctx.moduleProcedure())
     		visitModuleProcedure(mp);
     	decreaseIndentLevel();
     	indent();
     	appendCode("end interface");
     	if (ctx.id().size() > 0) {
     		appendCode(" ");
     		visitId(ctx.id(0));
     	}
     	return null;
     }
 
     public Void visitDeclarationStatement(DeclarationStatementContext ctx) {
         indent();
         if (ctx.dataDeclarationStatement() != null)
             visitDataDeclarationStatement(ctx.dataDeclarationStatement());
         else if (ctx.typeDeclarationStatement() != null)
             visitTypeDeclarationStatement(ctx.typeDeclarationStatement());
         else if (ctx.interfaceStatement() != null)
             visitInterfaceStatement(ctx.interfaceStatement());
         else if (ctx.namelistStatement() != null)
             visitNamelistStatement(ctx.namelistStatement());
         appendCode("\n");
         return null;
     }
 
     public Void visitDeclarationStatements(DeclarationStatementsContext ctx) {
     	if (ctx.children == null) return null;
     	int loc = 1;
     	for (ParseTree child : ctx.children) {
     		if (child instanceof DeclarationStatementContext) {
     			visitDeclarationStatement((DeclarationStatementContext) child);
     			if (ctx.getParent().getRuleIndex() == FortranParser.RULE_procedure &&
     		        loc == ctx.children.size())
     				appendCode("\n");
     		} else if (child instanceof CppDirectiveContext) {
         		if (ctx.getParent().getRuleIndex() == FortranParser.RULE_procedure &&
         	        loc == ctx.children.size())
         			appendCode("\n");
     			visitCppDirective((CppDirectiveContext) child);
     		}
     		loc++;
     	}
         return null;
     }
 
     public Void visitUsedItem(UsedItemContext ctx) {
         visitId(ctx.id(0));
         if (ctx.id().size() > 1) {
             appendCode(" => ");
             visitId(ctx.id(1));
         }
         return null;
     }
 
     public Void visitUsedItemList(UsedItemListContext ctx) {
         visitUsedItem(ctx.usedItem(0));
         for (int i = 1; i < ctx.usedItem().size(); ++i) {
             appendCode(", ");
             visitUsedItem(ctx.usedItem(i));
         }
         return null;
     }
 
     public Void visitUseStatement(UseStatementContext ctx) {
         indent();
         appendCode("use ");
         visitId(ctx.id());
         if (ctx.ONLY_KEYWORD() != null)
             appendCode(", only: ");
         if (ctx.usedItemList() != null)
             visitUsedItemList(ctx.usedItemList());
         appendCode("\n");
         return null;
     }
 
     public Void visitUseStatements(UseStatementsContext ctx) {
     	if (ctx.children == null) return null;
     	int loc = 1;
     	for (ParseTree child : ctx.children) {
     		if (child instanceof UseStatementContext) {
     			visitUseStatement((UseStatementContext) child);
     			if (loc == ctx.children.size())
     				appendCode("\n");
     		} else if (child instanceof CppDirectiveContext) {
         		if (loc == ctx.children.size())
         			appendCode("\n");
     			visitCppDirective((CppDirectiveContext) child);
     		}
     		loc++;
     	}
         return null;
     }
 
     public Void visitImplicitNoneStatement(ImplicitNoneStatementContext ctx) {
         indent();
         appendCode("implicit none\n\n");
         return null;
     }
 
     public Void visitAccessibilityStatement(AccessibilityStatementContext ctx) {
         indent();
         if (ctx.PUBLIC_KEYWORD() != null)
             appendCode("public ");
         else if (ctx.PRIVATE_KEYWORD() != null)
             appendCode("private ");
         if (ctx.id().size() > 0) {
         	visitId(ctx.id(0));
         	for (int i = 1; i < ctx.id().size(); ++i) {
         		appendCode(", ");
         		visitId(ctx.id(i));
         	}
         }
         appendCode("\n");
         return null;
     }
 
     public Void visitAccessibilityStatements(AccessibilityStatementsContext ctx) {
     	if (ctx.children == null) return null;
     	int loc = 1;
     	for (ParseTree child : ctx.children) {
     		if (child instanceof AccessibilityStatementContext) {
     			visitAccessibilityStatement((AccessibilityStatementContext) child);
     			if (loc == ctx.children.size())
     				appendCode("\n");
     		} else if (child instanceof CppDirectiveContext) {
         		if (loc == ctx.children.size())
         			appendCode("\n");
     			visitCppDirective((CppDirectiveContext) child);
     		}
     		loc++;
     	}
         return null;
     }
 
     public Void visitContainedProcedures(ContainedProceduresContext ctx) {
     	if (ctx.children == null) return null;
     	increaseIndentLevel();
     	int loc = 1;
     	for (ParseTree child : ctx.children) {
     		if (child instanceof ProcedureContext) {
     			visitProcedure((ProcedureContext) child);
     			if (loc == ctx.children.size() ||
     				!(ctx.getChild(loc) instanceof CppDirectiveContext))
     				appendCode("\n");
     		} else if (child instanceof CppDirectiveContext) {
         		if (loc == ctx.children.size())
         			appendCode("\n");
     			visitCppDirective((CppDirectiveContext) child);
     			if (((CppDirectiveContext) child).endifDirective() != null)
     				appendCode("\n");
     		}
     		loc++;
     	}
     	decreaseIndentLevel();
     	return null;
     }
     
     public Void visitProcedure(ProcedureContext ctx) {
         indent();
         appendCode(ctx.PROCEDURE_TYPE(0).getText()+" ");
         visitId(ctx.id(0));
         if (!(ctx.PROCEDURE_TYPE(0).getText().equals("program") ||
               ctx.PROCEDURE_TYPE(0).getText().equals("module"))) {
             appendCode(" (");
             if (ctx.dummyArguments() != null)
                 visitDummyArguments(ctx.dummyArguments());
             appendCode(")");
             if (ctx.RESULT_KEYWORD() != null) {
                 appendCode(" result(");
                 visitId(ctx.id(1));
                 appendCode(")");
             }
         }
         appendCode("\n\n");
         increaseIndentLevel();
         visitUseStatements(ctx.useStatements());
         if (ctx.implicitNoneStatement() != null)
             visitImplicitNoneStatement(ctx.implicitNoneStatement());
         visitAccessibilityStatements(ctx.accessibilityStatements());
         visitDeclarationStatements(ctx.declarationStatements());
         visitExecutableStatements(ctx.executableStatements());
         decreaseIndentLevel();
         if (ctx.containedProcedures().getChildCount() > 0) {
         	indent();
         	// When instantiating template, new contained procedures may be
         	// added, but there may not be any contained procedure, so does the
         	// 'contains' keyword. To handle this case, I just write 'contains'
         	// if there is any contained procedure.
         	appendCode("contains\n\n");
         	visitContainedProcedures(ctx.containedProcedures());
         }
         indent();
         appendCode("end "+ctx.PROCEDURE_TYPE(0).getText()+" ");
         visitId(ctx.id(0));
         appendCode("\n");
         return null;
     }
     
     // *************************************************************************
     // visit methods for C preprocessor
     public Void visitCppDirective(CppDirectiveContext ctx) {
     	if (ctx.includeDirective() != null) {
     		appendCode("#include ");
         	if (ctx.includeDirective().internalFile() != null)
         		appendCode(ctx.includeDirective().internalFile().getText());
         	else if (ctx.includeDirective().externalFile() != null)
         		appendCode(ctx.includeDirective().externalFile().getText());
     	} else if (ctx.defineDirective() != null) {
         	appendCode("#define ");
         	visitId(ctx.defineDirective().id());
         	if (ctx.defineDirective().expression() != null) {
         		newCode += " ";
         		visitExpression(ctx.defineDirective().expression());
         	}
     	} else if (ctx.undefDirective() != null) {
         	appendCode("#undef ");
         	visitId(ctx.undefDirective().id());
     	} else if (ctx.ifdefDirective() != null) {
     		appendCode("#ifdef ");
         	visitId(ctx.ifdefDirective().id());
     	} else if (ctx.ifndefDirective() != null) {
     		appendCode("#ifndef ");
         	visitId(ctx.ifndefDirective().id());
     	} else if (ctx.ifDirective() != null) {
     		appendCode("#if ");
     		visitConditionDirective(ctx.ifDirective().conditionDirective());
     	} else if (ctx.elseDirective() != null) {
     		appendCode("#else");
     	} else if (ctx.elifDirective() != null) {
     		appendCode("#elif ");
     		visitConditionDirective(ctx.elifDirective().conditionDirective());
     	} else if (ctx.endifDirective() != null) {
     		appendCode("#endif");
     	}
     	appendCode("\n");
     	return null;
     }
     
     public Void visitConditionDirective(ConditionDirectiveContext ctx) {
     	if (ctx.LEFT_PAREN() != null)
             appendCode("(");
         switch (ctx.conditionDirective().size()) {
         case 0:
             if (ctx.definedCondition() != null)
             	visitDefinedCondition(ctx.definedCondition());
             break;
         case 1:
         	// TODO: Fix the bug! COMMENT token will interfere with this
         	//       negative condition.
             if (ctx.EXCAL() != null)
                 appendCode("! ");
             visitConditionDirective(ctx.conditionDirective(0));
             break;
         case 2:
         	visitConditionDirective(ctx.conditionDirective(0));
             if (ctx.CPP_AND() != null)
                 appendCode(" && ");
             else if (ctx.CPP_OR() != null)
                 appendCode(" || ");
             visitConditionDirective(ctx.conditionDirective(1));
             break;
         }
         if (ctx.RIGHT_PAREN() != null)
             appendCode(")");
         return null;
     }
     
     public Void visitDefinedCondition(DefinedConditionContext ctx) {
     	appendCode("defined ");
     	visitId(ctx.id());
     	return null;
     }
 
     // *************************************************************************
     // visit methods for template
     public Void visitTemplateArgument(TemplateArgumentContext ctx) {
         if (ctx.expression() != null)
             visitExpression(ctx.expression());
         else if (ctx.subscriptRange() != null)
             visitSubscriptRange(ctx.subscriptRange());
         return null;
     }
 
     public Void visitTemplateArguments(TemplateArgumentsContext ctx) {
         visitTemplateArgument(ctx.templateArgument(0));
         for (int i = 1; i < ctx.templateArgument().size(); ++i) {
             appendCode(", ");
             visitTemplateArgument(ctx.templateArgument(i));
         }
         return null;
     }
 
     public Void visitTemplateBlock(TemplateBlockContext ctx) {
         visitExecutableStatements(ctx.executableStatements());
         return null;
     }
 
     public Void visitTemplateInstance(TemplateInstanceContext ctx) {
         // this method just write out the original template instance without
         // instantiation
         visitId(ctx.id());
         appendCode("<");
         if (ctx.templateArguments() != null)
             visitTemplateArguments(ctx.templateArguments());
         appendCode(">");
         if (ctx.templateBlock() != null) {
             appendCode(" {\n");
             increaseIndentLevel();
             visitTemplateBlock(ctx.templateBlock());
             decreaseIndentLevel();
             indent();
             appendCode("}");
         }
         return null;
     }
 }
