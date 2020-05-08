 package descent.core.dom;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import descent.core.compiler.CharOperation;
 import descent.core.dom.FunctionLiteralDeclarationExpression.Syntax;
 import descent.core.dom.IsTypeSpecializationExpression.TypeSpecialization;
 import descent.core.dom.Modifier.ModifierKeyword;
 import descent.internal.compiler.parser.*;
 import descent.internal.compiler.parser.AliasDeclaration;
 import descent.internal.compiler.parser.AlignDeclaration;
 import descent.internal.compiler.parser.Argument;
 import descent.internal.compiler.parser.ArrayInitializer;
 import descent.internal.compiler.parser.AsmBlock;
 import descent.internal.compiler.parser.AsmStatement;
 import descent.internal.compiler.parser.BaseClass;
 import descent.internal.compiler.parser.BreakStatement;
 import descent.internal.compiler.parser.ConditionalDeclaration;
 import descent.internal.compiler.parser.ConditionalStatement;
 import descent.internal.compiler.parser.ContinueStatement;
 import descent.internal.compiler.parser.DeclarationStatement;
 import descent.internal.compiler.parser.DefaultStatement;
 import descent.internal.compiler.parser.DoStatement;
 import descent.internal.compiler.parser.EnumDeclaration;
 import descent.internal.compiler.parser.EnumMember;
 import descent.internal.compiler.parser.Expression;
 import descent.internal.compiler.parser.ForStatement;
 import descent.internal.compiler.parser.ForeachRangeStatement;
 import descent.internal.compiler.parser.ForeachStatement;
 import descent.internal.compiler.parser.GotoCaseStatement;
 import descent.internal.compiler.parser.GotoDefaultStatement;
 import descent.internal.compiler.parser.GotoStatement;
 import descent.internal.compiler.parser.IfStatement;
 import descent.internal.compiler.parser.Import;
 import descent.internal.compiler.parser.Initializer;
 import descent.internal.compiler.parser.InvariantDeclaration;
 import descent.internal.compiler.parser.Modifier;
 import descent.internal.compiler.parser.ModuleDeclaration;
 import descent.internal.compiler.parser.PragmaDeclaration;
 import descent.internal.compiler.parser.PragmaStatement;
 import descent.internal.compiler.parser.ReturnStatement;
 import descent.internal.compiler.parser.Statement;
 import descent.internal.compiler.parser.StaticAssert;
 import descent.internal.compiler.parser.StaticAssertStatement;
 import descent.internal.compiler.parser.StructInitializer;
 import descent.internal.compiler.parser.SwitchStatement;
 import descent.internal.compiler.parser.SynchronizedStatement;
 import descent.internal.compiler.parser.TemplateDeclaration;
 import descent.internal.compiler.parser.TemplateParameter;
 import descent.internal.compiler.parser.ThrowStatement;
 import descent.internal.compiler.parser.Type;
 import descent.internal.compiler.parser.TypedefDeclaration;
 import descent.internal.compiler.parser.UnitTestDeclaration;
 import descent.internal.compiler.parser.Version;
 import descent.internal.compiler.parser.VoidInitializer;
 import descent.internal.compiler.parser.VolatileStatement;
 import descent.internal.compiler.parser.WhileStatement;
 import descent.internal.compiler.parser.WithStatement;
 import descent.internal.compiler.parser.Type.Modification;
 
 /**
  * Internal class for converting internal compiler ASTs into public ASTs.
  */
 public class ASTConverter {
 	
 	protected AST ast;
 	protected IProgressMonitor monitor;
 	
 	private Comment[] moduleComments;
 	
 	public ASTConverter(IProgressMonitor monitor) {
 		this.monitor = monitor;
 	}
 	
 	public void setAST(AST ast) {
 		this.ast = ast;
 	}
 	
 	public CompilationUnit convert(Module module) {
 		CompilationUnit unit = new CompilationUnit(ast);
 		
 		moduleComments = convertComments(module.comments);
 		unit.setCommentTable(moduleComments);
 		
 		unit.setPragmaTable(convertPragmas(module.pragmas));
 		
 		if (module.md != null) {
 			unit.setModuleDeclaration(convert(module.md));
 		}
 		convertDeclarations(unit.declarations(), module.members);
 		unit.setSourceRange(module.start, module.length);		
 		return unit;
 	}
 
 	private Pragma[] convertPragmas(descent.internal.compiler.parser.Pragma[] from) {
 		Pragma[] to = new Pragma[from.length];
 		for(int i = 0; i < from.length; i++) {
 			to[i] = ast.newPragma();
 			to[i].setSourceRange(from[i].start, from[i].length);
 		}
 		return to;
 	}
 	
 	public descent.core.dom.ModuleDeclaration convert(descent.internal.compiler.parser.ModuleDeclaration md) {
 		descent.core.dom.ModuleDeclaration result = new descent.core.dom.ModuleDeclaration(ast);
 		result.setName(convert(md.packages, md.id));
 		if (md.preDdocs != null) {
 			convertDdoc(result.preDDocs(), md.preDdocs);
 		}
 		if (md.postDdoc != null) {
 			result.setPostDDoc(convertDdoc(md.postDdoc));
 		}
 		result.setSourceRange(md.start, md.length);
 		return result;
 	}
 	
 	public descent.core.dom.ASTNode convert(descent.internal.compiler.parser.ASTDmdNode symbol) {
 		switch(symbol.getNodeType()) {
 		case ASTDmdNode.ADD_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.PLUS_ASSIGN);
 		case ASTDmdNode.ADD_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.PLUS);
 		case ASTDmdNode.ADDR_EXP:
 			return convert((UnaExp) symbol, PrefixExpression.Operator.ADDRESS);
 		case ASTDmdNode.ALIAS_DECLARATION:
 			return convert((AliasDeclaration) symbol);
 		case ASTDmdNode.ALIGN_DECLARATION:
 			return convert((AlignDeclaration) symbol);
 		case ASTDmdNode.AND_AND_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.AND_AND);
 		case ASTDmdNode.AND_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.AND_ASSIGN);
 		case ASTDmdNode.AND_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.AND);
 		case ASTDmdNode.ANON_DECLARATION:
 			return convert((AnonDeclaration) symbol);
 		case ASTDmdNode.ARGUMENT:
 			return convert((Argument) symbol);
 		case ASTDmdNode.ARRAY_EXP:
 			return convert((ArrayExp) symbol);
 		case ASTDmdNode.ARRAY_INITIALIZER:
 			return convert((ArrayInitializer) symbol);
 		case ASTDmdNode.ARRAY_LITERAL_EXP:
 			return convert((ArrayLiteralExp) symbol);
 		case ASTDmdNode.ASM_BLOCK:
 			return convert((AsmBlock) symbol);
 		case ASTDmdNode.ASM_STATEMENT:
 			return convert((AsmStatement) symbol);
 		case ASTDmdNode.ASSERT_EXP:
 			return convert((AssertExp) symbol);
 		case ASTDmdNode.ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.ASSIGN);
 		case ASTDmdNode.ASSOC_ARRAY_LITERAL_EXP:
 			return convert((AssocArrayLiteralExp) symbol);
 		case ASTDmdNode.BREAK_STATEMENT:
 			return convert((BreakStatement) symbol);
 		case ASTDmdNode.CALL_EXP:
 			return convert((CallExp) symbol);
 		case ASTDmdNode.CASE_STATEMENT:
 			return convert((CaseStatement) symbol);
 		case ASTDmdNode.CAST_EXP:
 			return convert((CastExp) symbol);
 		case ASTDmdNode.CAT_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.CONCATENATE_ASSIGN);
 		case ASTDmdNode.CAT_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.CONCATENATE);
 		case ASTDmdNode.CATCH:
 			return convert((Catch) symbol);
 		case ASTDmdNode.CLASS_DECLARATION:
 			return convert((ClassDeclaration) symbol);
 		case ASTDmdNode.CMP_EXP:
 			return convert((CmpExp) symbol);
 		case ASTDmdNode.COM_EXP:
 			return convert((UnaExp) symbol, PrefixExpression.Operator.INVERT);
 		case ASTDmdNode.COMMA_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.COMMA);
 		case ASTDmdNode.COMPILE_DECLARATION:
 			return convert((CompileDeclaration) symbol);
 		case ASTDmdNode.COMPILE_EXP:
 			return convert((CompileExp) symbol);
 		case ASTDmdNode.COMPILE_STATEMENT:
 			return convert((CompileStatement) symbol);
 		case ASTDmdNode.COMPOUND_STATEMENT:
 			return convert((CompoundStatement) symbol);
 		case ASTDmdNode.COND_EXP:
 			return convert((CondExp) symbol);
 		case ASTDmdNode.CONDITIONAL_DECLARATION:
 			return convert((ConditionalDeclaration) symbol);
 		case ASTDmdNode.CONDITIONAL_STATEMENT:
 			return convert((ConditionalStatement) symbol);
 		case ASTDmdNode.CONTINUE_STATEMENT:
 			return convert((ContinueStatement) symbol);
 		case ASTDmdNode.CTOR_DECLARATION:
 			return convert((CtorDeclaration) symbol);
 		case ASTDmdNode.DEBUG_SYMBOL:
 			return convert((DebugSymbol) symbol);
 		case ASTDmdNode.DECLARATION_EXP:
 			// TODO
 			return convert((DeclarationExp) symbol);
 		case ASTDmdNode.DECLARATION_STATEMENT:
 			return convert((DeclarationStatement) symbol);
 		case ASTDmdNode.DECREMENT_EXP:
 			return convert((DecrementExp) symbol);
 		case ASTDmdNode.DEFAULT_STATEMENT:
 			return convert((DefaultStatement) symbol);
 		case ASTDmdNode.DELETE_DECLARATION:
 			return convert((DeleteDeclaration) symbol);
 		case ASTDmdNode.DELETE_EXP:
 			return convert((DeleteExp) symbol);
 		case ASTDmdNode.DIV_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.DIVIDE_ASSIGN);
 		case ASTDmdNode.DIV_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.DIVIDE);
 		case ASTDmdNode.DO_STATEMENT:
 			return convert((DoStatement) symbol);
 		case ASTDmdNode.DOLLAR_EXP:
 			return convert((DollarExp) symbol);
 		case ASTDmdNode.DOT_ID_EXP:
 			return convert((DotIdExp) symbol);
 		case ASTDmdNode.DOT_TEMPLATE_INSTANCE_EXP:
 			return convert((DotTemplateInstanceExp) symbol);
 		// ->
 		case ASTDmdNode.DTOR_DECLARATION:
 			return convert((DtorDeclaration) symbol);
 		case ASTDmdNode.ENUM_DECLARATION:
 			return convert((EnumDeclaration) symbol);
 		case ASTDmdNode.ENUM_MEMBER:
 			return convert((EnumMember) symbol);
 		case ASTDmdNode.EQUAL_EXP:
 			return convert((EqualExp) symbol);
 		case ASTDmdNode.EXP_INITIALIZER:
 			return convert((ExpInitializer) symbol);
 		case ASTDmdNode.EXP_STATEMENT:
 			return convert((ExpStatement) symbol);
 		case ASTDmdNode.FILE_EXP:
 			return convert((FileExp) symbol);
 		case ASTDmdNode.FOR_STATEMENT:
 			return convert((ForStatement) symbol);
 		case ASTDmdNode.FOREACH_STATEMENT:
 			return convert((ForeachStatement) symbol);
 		case ASTDmdNode.FOREACH_RANGE_STATEMENT:
 			return convert((ForeachRangeStatement) symbol);
 		case ASTDmdNode.FUNC_DECLARATION:
 			return convert((FuncDeclaration) symbol);
 		case ASTDmdNode.FUNC_EXP:
 			return convert((FuncExp) symbol);
 		case ASTDmdNode.FUNC_LITERAL_DECLARATION:
 			// should never reach this point
 			return null;
 		case ASTDmdNode.GOTO_CASE_STATEMENT:
 			return convert((GotoCaseStatement) symbol);
 		case ASTDmdNode.GOTO_DEFAULT_STATEMENT:
 			return convert((GotoDefaultStatement) symbol);
 		case ASTDmdNode.GOTO_STATEMENT:
 			return convert((GotoStatement) symbol);
 		case ASTDmdNode.IDENTIFIER_EXP:
 			return convert((IdentifierExp) symbol);
 		case ASTDmdNode.IDENTITY_EXP:
 			return convert((IdentityExp) symbol);
 		case ASTDmdNode.IF_STATEMENT:
 			return convert((IfStatement) symbol);
 		case ASTDmdNode.IFTYPE_EXP:
 			return convert((IftypeExp) symbol);
 		case ASTDmdNode.IMPORT:
 			return convert((Import) symbol);
 		case ASTDmdNode.IN_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.IN);
 		case ASTDmdNode.INCREMENT_EXP:
 			return convert((IncrementExp) symbol);
 		case ASTDmdNode.INTEGER_EXP:
 			return convert((IntegerExp) symbol);
 		case ASTDmdNode.INTERFACE_DECLARATION:
 			return convert((InterfaceDeclaration) symbol);
 		case ASTDmdNode.INVARIANT_DECLARATION:
 			return convert((InvariantDeclaration) symbol);
 		case ASTDmdNode.LABEL_STATEMENT:
 			return convert((LabelStatement) symbol);
 		case ASTDmdNode.LINK_DECLARATION:
 			return convert((LinkDeclaration) symbol);
 		case ASTDmdNode.MIN_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.MINUS_ASSIGN);
 		case ASTDmdNode.MIN_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.MINUS);
 		case ASTDmdNode.PROT_DECLARATION:
 			return convert((ProtDeclaration) symbol);
 		case ASTDmdNode.TEMPLATE_MIXIN:
 			return convert((TemplateMixin) symbol);
 		case ASTDmdNode.MOD_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.REMAINDER_ASSIGN);
 		case ASTDmdNode.MOD_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.REMAINDER);
 		case ASTDmdNode.MODIFIER:
 			return convert((Modifier) symbol);
 		case ASTDmdNode.MODULE:
 			return convert((Module) symbol);
 		case ASTDmdNode.MODULE_DECLARATION:
 			return convert((ModuleDeclaration) symbol);
 		case ASTDmdNode.MUL_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.TIMES_ASSIGN);
 		case ASTDmdNode.MUL_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.TIMES);
 		case ASTDmdNode.MULTI_IMPORT:
 			return convert((MultiImport) symbol);
 		case ASTDmdNode.MULTI_STRING_EXP:
 			return convert((MultiStringExp) symbol);
 		case ASTDmdNode.NEG_EXP:
 			return convert((UnaExp) symbol, PrefixExpression.Operator.NEGATIVE);
 		case ASTDmdNode.NEW_ANON_CLASS_EXP:
 			return convert((NewAnonClassExp) symbol);
 		case ASTDmdNode.NEW_DECLARATION:
 			return convert((NewDeclaration) symbol);
 		case ASTDmdNode.NEW_EXP:
 			return convert((NewExp) symbol);
 		case ASTDmdNode.NOT_EXP:
 			return convert((UnaExp) symbol, PrefixExpression.Operator.NOT);
 		case ASTDmdNode.NULL_EXP:
 			return convert((NullExp) symbol);
 		case ASTDmdNode.ON_SCOPE_STATEMENT:
 			return convert((OnScopeStatement) symbol);
 		case ASTDmdNode.OR_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.OR_ASSIGN);
 		case ASTDmdNode.OR_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.OR);
 		case ASTDmdNode.OR_OR_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.OR_OR);
 		case ASTDmdNode.PAREN_EXP:
 			return convert((ParenExp) symbol);
 		case ASTDmdNode.POST_EXP:
 			return convert((PostExp) symbol);
 		case ASTDmdNode.PRAGMA_DECLARATION:
 			return convert((PragmaDeclaration) symbol);
 		case ASTDmdNode.PRAGMA_STATEMENT:
 			return convert((PragmaStatement) symbol);
 		case ASTDmdNode.PTR_EXP:
 			return convert((UnaExp) symbol, PrefixExpression.Operator.POINTER);
 		case ASTDmdNode.REAL_EXP:
 			return convert((RealExp) symbol);
 		case ASTDmdNode.RETURN_STATEMENT:
 			return convert((ReturnStatement) symbol);
 		case ASTDmdNode.SCOPE_EXP:
 			return convert((ScopeExp) symbol);
 		case ASTDmdNode.SHL_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.LEFT_SHIFT_ASSIGN);
 		case ASTDmdNode.SHL_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.LEFT_SHIFT);
 		case ASTDmdNode.SHR_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN);
 		case ASTDmdNode.SHR_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
 		case ASTDmdNode.SLICE_EXP:
 			return convert((SliceExp) symbol);
 		case ASTDmdNode.STATIC_ASSERT:
 			return convert((StaticAssert) symbol);
 		case ASTDmdNode.STATIC_ASSERT_STATEMENT:
 			return convert((StaticAssertStatement) symbol);
 		case ASTDmdNode.STATIC_CTOR_DECLARATION:
 			return convert((StaticCtorDeclaration) symbol);
 		case ASTDmdNode.STATIC_DTOR_DECLARATION:
 			return convert((StaticDtorDeclaration) symbol);
 		case ASTDmdNode.STORAGE_CLASS_DECLARATION:
 			return convert((StorageClassDeclaration) symbol);
 		case ASTDmdNode.STRING_EXP:
 			return convert((StringExp) symbol);
 		case ASTDmdNode.STRUCT_DECLARATION:
 			return convert((StructDeclaration) symbol);
 		case ASTDmdNode.STRUCT_INITIALIZER:
 			return convert((StructInitializer) symbol);
 		case ASTDmdNode.SUPER_EXP:
 			return convert((SuperExp) symbol);
 		case ASTDmdNode.SYNCHRONIZED_STATEMENT:
 			return convert((SynchronizedStatement) symbol);
 		case ASTDmdNode.SWITCH_STATEMENT:
 			return convert((SwitchStatement) symbol);
 		case ASTDmdNode.TEMPLATE_ALIAS_PARAMETER:
 			return convert((TemplateAliasParameter) symbol);
 		case ASTDmdNode.TEMPLATE_DECLARATION:
 			return convert((TemplateDeclaration) symbol);
 		case ASTDmdNode.TEMPLATE_INSTANCE:
 			return convert((TemplateInstance) symbol);
 		case ASTDmdNode.TEMPLATE_TUPLE_PARAMETER:
 			return convert((TemplateTupleParameter) symbol);
 		case ASTDmdNode.TEMPLATE_TYPE_PARAMETER:
 			return convert((TemplateTypeParameter) symbol);
 		case ASTDmdNode.TEMPLATE_VALUE_PARAMETER:
 			return convert((TemplateValueParameter) symbol);
 		case ASTDmdNode.THIS_EXP:
 			return convert((ThisExp) symbol);
 		case ASTDmdNode.THROW_STATEMENT:
 			return convert((ThrowStatement) symbol);
 		case ASTDmdNode.TRAITS_EXP:
 			return convert((TraitsExp) symbol);
 		case ASTDmdNode.TRY_CATCH_STATEMENT:
 			return convert((TryCatchStatement) symbol);
 		case ASTDmdNode.TRY_FINALLY_STATEMENT:
 			return convert((TryFinallyStatement) symbol);
 		case ASTDmdNode.TYPEID_EXP:
 			return convert((TypeidExp) symbol);
 		case ASTDmdNode.TYPEDEF_DECLARATION:
 			return convert((TypedefDeclaration) symbol);
 		case ASTDmdNode.TYPE_A_ARRAY:
 			return convert((TypeAArray) symbol);
 		case ASTDmdNode.TYPE_BASIC:
 			return convert((TypeBasic) symbol);
 		case ASTDmdNode.TYPE_D_ARRAY:
 			return convert((TypeDArray) symbol);
 		case ASTDmdNode.TYPE_DELEGATE:
 			return convert((TypeDelegate) symbol);
 		case ASTDmdNode.TYPE_DOT_ID_EXP:
 			return convert((TypeDotIdExp) symbol);
 		case ASTDmdNode.TYPE_EXP:
 			return convert((TypeExp) symbol);
 		case ASTDmdNode.TYPE_FUNCTION:
 			// should never reach this point
 			return null;
 		case ASTDmdNode.TYPE_IDENTIFIER:
 			return convert((TypeIdentifier) symbol);
 		case ASTDmdNode.TYPE_INSTANCE:
 			return convert((TypeInstance) symbol);
 		case ASTDmdNode.TYPE_POINTER:
 			return convert((TypePointer) symbol);
 		case ASTDmdNode.TYPE_S_ARRAY:
 			return convert((TypeSArray) symbol);
 		case ASTDmdNode.TYPE_SLICE:
 			return convert((TypeSlice) symbol);
 		case ASTDmdNode.TYPE_TYPEOF:
 			return convert((TypeTypeof) symbol);
 		case ASTDmdNode.UADD_EXP:
 			return convert((UnaExp) symbol, PrefixExpression.Operator.POSITIVE);
 		case ASTDmdNode.UNION_DECLARATION:
 			return convert((UnionDeclaration) symbol);
 		case ASTDmdNode.UNIT_TEST_DECLARATION:
 			return convert((UnitTestDeclaration) symbol);
 		case ASTDmdNode.USHR_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN);
 		case ASTDmdNode.USHR_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
 		case ASTDmdNode.VERSION_SYMBOL:
 			return convert((VersionSymbol) symbol);
 		case ASTDmdNode.VOID_INITIALIZER:
 			return convert((VoidInitializer) symbol);
 		case ASTDmdNode.VOLATILE_STATEMENT:
 			return convert((VolatileStatement) symbol);
 		case ASTDmdNode.WHILE_STATEMENT:
 			return convert((WhileStatement) symbol);
 		case ASTDmdNode.WITH_STATEMENT:
 			return convert((WithStatement) symbol);
 		case ASTDmdNode.XOR_ASSIGN_EXP:
 			return convert((BinExp) symbol, Assignment.Operator.XOR_ASSIGN);
 		case ASTDmdNode.XOR_EXP:
 			return convert((BinExp) symbol, InfixExpression.Operator.XOR);
 		}
 		return null;
 	}
 	
 	public descent.core.dom.TraitsExpression convert(TraitsExp a) {
 		descent.core.dom.TraitsExpression b = new descent.core.dom.TraitsExpression(ast);
 		b.setName(convert(a.ident));
 		if (a.args != null) {
 			for(ASTDmdNode node : a.args) {
 				b.arguments().add(convert(node));
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public AssociativeArrayLiteral convert(AssocArrayLiteralExp a) {
 		AssociativeArrayLiteral b = new AssociativeArrayLiteral(ast);
 		if (a.keys != null) {
 			for(int i = 0; i < a.keys.size(); i++) {
 				Expression key = a.keys.get(i);
 				Expression value = a.values.get(i);
 				AssociativeArrayLiteralFragment fragment = new AssociativeArrayLiteralFragment(ast);
 				fragment.setKey(convert(key));
 				fragment.setValue(convert(value));
 				fragment.setSourceRange(key.start, value.start + value.length - key.start);
 				b.fragments().add(fragment);
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public Declaration convert(StorageClassDeclaration a) {
 		List<Declaration> dummy = new ArrayList<Declaration>(1);
 		convert(a, dummy);
 		return dummy.get(0);
 	}
 	
 	public void convert(StorageClassDeclaration a, List<Declaration> toAdd) {
 		descent.core.dom.Modifier modifier = convert(a.modifier);
 		
 		if (a.single && a.decl != null && a.decl.size() >= 1) {
 			Declaration decl = convertDeclaration(a.decl.get(0));
 			
 			if (a.decl.size() == 1) {
 				int insertAt;
 				if (a.modifiers != null) {
 					insertAt = a.modifiers.size();
 					for(Modifier mod : a.modifiers) {
 						decl.modifiers().add(convert(mod));
 					}
 				} else {
 					insertAt = 0;
 				}
 				decl.modifiers().add(insertAt, modifier);
 				if (a.preDdocs != null) {
 					convertDdoc(decl.preDDocs(), a.preDdocs);
 				}
 				if (a.postDdoc != null) {
 					decl.postDDoc = convertDdoc(a.postDdoc);
 				}
 				
 				decl.setSourceRange(a.start, a.length);
 				toAdd.add(decl);
 				return;
 			} else {
 				descent.core.dom.Declaration declaration = tryConvertMany(a.decl, modifier, a.modifiers);
 				if (declaration != null) {
 					toAdd.add(declaration);
 					return;
 				}
 			}
 		}
 		
 		if (a.colon && a.decl != null && a.decl.size() > 0) {
 			for(int i = 0; i < a.decl.size(); i++) {
 				Dsymbol dsymbol = a.decl.get(i);
 				if (
 					(dsymbol instanceof ProtDeclaration && ((ProtDeclaration) dsymbol).colon)
 						|| 
 					(dsymbol instanceof StorageClassDeclaration && ((StorageClassDeclaration) dsymbol).colon)) {
 					convertNestedStorageClassOrProtDeclaration(dsymbol, a, modifier, toAdd, i);
 					return;
 				}
 			}
 		}
 		
 		descent.core.dom.ModifierDeclaration b = new descent.core.dom.ModifierDeclaration(ast);			
 		b.setModifier(modifier);
 		convertDeclarations(b.declarations(), a.decl);
 		b.setSourceRange(a.start, a.length);
 		toAdd.add(b);
 	}
 	
 	private void convertNestedStorageClassOrProtDeclaration(Dsymbol dsymbol, AttribDeclaration a, descent.core.dom.Modifier modifier, List<Declaration> toAdd, int i) {
 		descent.core.dom.ModifierDeclaration b = new descent.core.dom.ModifierDeclaration(ast);			
 		b.setModifier(modifier);
 		convertDeclarations(b.declarations(), a.decl.subList(0, i));
 		if (b.declarations().size() > 0) {
 			Declaration last = b.declarations().get(b.declarations().size() - 1);
 			b.setSourceRange(a.start, last.getStartPosition() + last.getLength() - a.start);
 			toAdd.add(b);
 			if (dsymbol instanceof ProtDeclaration) {
 				convert((ProtDeclaration) dsymbol, toAdd);
 			} else {
 				convert((StorageClassDeclaration) dsymbol, toAdd);
 			}
 		}
 	}
 
 	public Declaration convert(ProtDeclaration a) {
 		List<Declaration> dummy = new ArrayList<Declaration>(1);
 		convert(a, dummy);
 		return dummy.get(0);
 	}
 
 	public void convert(ProtDeclaration a, List<Declaration> toAdd) {
 		descent.core.dom.Modifier modifier = convert(a.modifier);
 		
 		if (a.single && a.decl != null && a.decl.size() > 0) {
 			if (a.decl.size() == 1) {
 				Declaration decl = convertDeclaration(a.decl.get(0));
 				decl.modifiers().add(0, modifier);
 				decl.setSourceRange(a.start, a.length);
 				toAdd.add(decl);
 				return;
 			} else {
 				descent.core.dom.Declaration declaration = tryConvertMany(a.decl, modifier, a.modifiers);
 				if (declaration != null) {
 					toAdd.add(declaration);
 					return;
 				}
 			}
 		}
 		
 		if (a.colon && a.decl != null && a.decl.size() > 0) {
 			for(int i = 0; i < a.decl.size(); i++) {
 				Dsymbol dsymbol = a.decl.get(i);
 				if (
 					(dsymbol instanceof ProtDeclaration && ((ProtDeclaration) dsymbol).colon)
 						|| 
 					(dsymbol instanceof StorageClassDeclaration && ((StorageClassDeclaration) dsymbol).colon)) {
 					convertNestedStorageClassOrProtDeclaration(dsymbol, a, modifier, toAdd, i);
 					return;
 				}
 			}
 		}
 		
 		descent.core.dom.ModifierDeclaration b = new descent.core.dom.ModifierDeclaration(ast);			
 		b.setModifier(modifier);
 		convertDeclarations(b.declarations(), a.decl);
 		b.setSourceRange(a.start, a.length);
 		toAdd.add(b);
 	}
 	
 	private Declaration tryConvertMany(List<Dsymbol> decl, descent.core.dom.Modifier modifier, List<Modifier> modifiers) {
 		Dsymbol dsymbol = decl.get(0);
 		descent.core.dom.Declaration declaration = null;
 		if (dsymbol instanceof VarDeclaration) {
 			declaration = convertManyVarDeclarations(decl);
 		} else if (dsymbol instanceof AliasDeclaration) {
 			declaration = convertManyAliasDeclarations(decl);
 		} else if (dsymbol instanceof TypedefDeclaration) {
 			declaration =convertManyTypedefDeclarations(decl);
 		}
 		
 		if (declaration != null) {
 			if (modifiers != null) {
 				for(Modifier m : modifiers) {
 					declaration.modifiers().add(convert(m));
 				}
 			}
 			declaration.modifiers().add(modifier);
 			declaration.setSourceRange(modifier.getStartPosition(), declaration.getStartPosition() + declaration.getLength() - modifier.getStartPosition());
 			return declaration;
 		} else {
 			return null;
 		}
 	}
 	
 	private descent.core.dom.VariableDeclaration convertManyVarDeclarations(List decls) {
 		VarDeclaration first = (VarDeclaration) decls.get(0);
 		VarDeclaration last = (VarDeclaration) decls.get(decls.size() - 1);
 		
 		descent.core.dom.VariableDeclaration varToReturn = new VariableDeclaration(ast);
 		
 		int start = first.start;
 		if (first.modifiers != null && first.modifiers.size() > 0) {
 			convertModifiers(varToReturn.modifiers(), first.modifiers);
 			start = first.modifiers.get(0).start;
 		}
 		
 		if (first.type != null) {
 			varToReturn.setType(convert(first.type));
 		}
 		
 		for(Object var : decls) {
 			varToReturn.fragments().add(convert((VarDeclaration) var));
 		}
 		
 		varToReturn.setSourceRange(start, last.start + last.length - first.start);
 		return varToReturn;
 	}
 	
 	private descent.core.dom.AliasDeclaration convertManyAliasDeclarations(List decls) {
 		AliasDeclaration first = (AliasDeclaration) decls.get(0);
 		AliasDeclaration last = (AliasDeclaration) decls.get(decls.size() - 1);
 		
 		descent.core.dom.AliasDeclaration varToReturn = new descent.core.dom.AliasDeclaration(ast);
 		if (first.modifiers != null) {
 			convertModifiers(varToReturn.modifiers(), first.modifiers);
 		}
 		
 		if (first.type != null) {
 			varToReturn.setType(convert(first.type));
 		}
 		
 		for(Object var : decls) {
 			varToReturn.fragments().add(convert((AliasDeclaration) var));
 		}
 		
 		varToReturn.setSourceRange(first.start, last.start + last.length - first.start);
 		return varToReturn;
 	}
 	
 	private descent.core.dom.TypedefDeclaration convertManyTypedefDeclarations(List decls) {
 		TypedefDeclaration first = (TypedefDeclaration) decls.get(0);
 		TypedefDeclaration last = (TypedefDeclaration) decls.get(decls.size() - 1);
 		
 		descent.core.dom.TypedefDeclaration varToReturn = new descent.core.dom.TypedefDeclaration(ast);
 		if (first.modifiers != null) {
 			convertModifiers(varToReturn.modifiers(), first.modifiers);
 		}
 		
 		if (first.basetype != null) {
 			varToReturn.setType(convert(first.basetype));
 		}
 		
 		for(Object var : decls) {
 			varToReturn.fragments().add(convert((TypedefDeclaration)var));
 		}
 		
 		varToReturn.setSourceRange(first.start, last.start + last.length - first.start);
 		return varToReturn;
 	}
 
 	public descent.core.dom.ScopeStatement convert(OnScopeStatement a) {
 		descent.core.dom.ScopeStatement b = new descent.core.dom.ScopeStatement(ast);
 		switch(a.tok) {
 		case TOKon_scope_exit: b.setEvent(ScopeStatement.Event.EXIT); break;
 		case TOKon_scope_failure: b.setEvent(ScopeStatement.Event.FAILURE); break;
 		case TOKon_scope_success: b.setEvent(ScopeStatement.Event.SUCCESS); break;
 		}
 		if (a.statement != null) {
 			b.setBody(convert(a.statement));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.StaticAssert convert(StaticAssert a) {
 		descent.core.dom.StaticAssert b = new descent.core.dom.StaticAssert(ast);
 		if (a.exp != null) {
 			b.setExpression(convert(a.exp));
 		}
 		if (a.msg != null) {
 			b.setMessage(convert(a.msg));
 		}
 		b.setSourceRange(a.start, a.length);
 		fillDeclaration(b, a);
 		return b;
 	}
 	
 	public descent.core.dom.StaticAssertStatement convert(StaticAssertStatement a) {
 		descent.core.dom.StaticAssertStatement b = new descent.core.dom.StaticAssertStatement(ast);
 		b.setStaticAssert(convert(a.sa));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TryStatement convert(TryCatchStatement a) {
 		descent.core.dom.TryStatement b = new descent.core.dom.TryStatement(ast);
 		b.setBody(convert(a.body));
 		if (a.catches != null) {
 			for(Catch c : a.catches) {
 				b.catchClauses().add(convert(c));
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TryStatement convert(TryFinallyStatement a) {
 		TryStatement b;
 		if (a.isTryCatchFinally) {
 			b = (TryStatement) convert(a.body);			
 		} else {
 			b = new descent.core.dom.TryStatement(ast);
 			b.setBody(convert(a.body));
 			b.setSourceRange(a.start, a.length);
 		}
 		b.setFinally(convert(a.finalbody));
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(IftypeExp a) {
 		if (a.tok2 == TOK.TOKreserved) {
 			descent.core.dom.IsTypeExpression b = new descent.core.dom.IsTypeExpression(ast);
 			b.setSameComparison(a.tok == TOK.TOKequal);
 			if (a.targ != null) {
 				b.setType(convert(a.targ));
 			}
 			if (a.ident != null) {
 				b.setName(convert(a.ident));
 			}
 			if (a.tspec != null) {
 				b.setSpecialization(convert(a.tspec));
 			}
 			b.setSourceRange(a.start, a.length);
 			return b;
 		} else {
 			descent.core.dom.IsTypeSpecializationExpression b = new descent.core.dom.IsTypeSpecializationExpression(ast);
 			b.setSameComparison(a.tok == TOK.TOKequal);
 			if (a.targ != null) {
 				b.setType(convert(a.targ));
 			}
 			if (a.ident != null) {
 				b.setName(convert(a.ident));
 			}
 			switch(a.tok2) {
 			case TOKtypedef: b.setSpecialization(TypeSpecialization.TYPEDEF); break;
 			case TOKstruct: b.setSpecialization(TypeSpecialization.STRUCT); break;
 			case TOKunion: b.setSpecialization(TypeSpecialization.UNION); break;
 			case TOKclass: b.setSpecialization(TypeSpecialization.CLASS); break;
 			case TOKenum: b.setSpecialization(TypeSpecialization.ENUM); break;
 			case TOKinterface: b.setSpecialization(TypeSpecialization.INTERFACE); break;
 			case TOKfunction: b.setSpecialization(TypeSpecialization.FUNCTION); break;
 			case TOKdelegate: b.setSpecialization(TypeSpecialization.DELEGATE); break;
 			case TOKreturn: b.setSpecialization(TypeSpecialization.RETURN); break;
 			case TOKsuper: b.setSpecialization(TypeSpecialization.SUPER); break;
 			}
 			b.setSourceRange(a.start, a.length);
 			return b;
 		}
 	}
 	
 	public descent.core.dom.PostfixExpression convert(PostExp a) {
 		descent.core.dom.PostfixExpression b = new descent.core.dom.PostfixExpression(ast);
 		b.setOperand(convert(a.e1));
 		if (a.op == TOK.TOKplusplus) {
 			b.setOperator(PostfixExpression.Operator.INCREMENT);
 		} else {
 			b.setOperator(PostfixExpression.Operator.DECREMENT);
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.PragmaDeclaration convert(PragmaDeclaration a) {
 		descent.core.dom.PragmaDeclaration b = new descent.core.dom.PragmaDeclaration(ast);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		convertExpressions(b.arguments(), a.args);
 		convertDeclarations(b.declarations(), a.decl);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.PragmaStatement convert(PragmaStatement a) {
 		descent.core.dom.PragmaStatement b = new descent.core.dom.PragmaStatement(ast);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		convertExpressions(b.arguments(), a.args);
 		if (a.body != null) {
 			b.setBody(convert(a.body));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TemplateMixinDeclaration convert(TemplateMixin a) {
 		descent.core.dom.TemplateMixinDeclaration b = new descent.core.dom.TemplateMixinDeclaration(ast);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		b.setType(convertTemplateMixin(a.typeStart, a.typeLength, a.tqual, a.idents, a.tiargs));
 		b.setSourceRange(a.start, a.length);
 		fillDeclaration(b, a);
 		return b;
 	}
 	
 	public descent.core.dom.TypeidExpression convert(TypeidExp a) {
 		descent.core.dom.TypeidExpression b = new descent.core.dom.TypeidExpression(ast);
 		b.setType(convert(a.typeidType));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.SliceExpression convert(SliceExp a) {
 		descent.core.dom.SliceExpression b = new descent.core.dom.SliceExpression(ast);
 		if (a.e1 != null) {
 			b.setExpression(convert(a.e1));
 		}
 		if (a.lwr != null) {
 			b.setFromExpression(convert(a.lwr));
 		}
 		if (a.upr != null) {
 			b.setToExpression(convert(a.upr));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TypeExpression convert(ScopeExp a) {
 		descent.core.dom.TypeExpression b = new descent.core.dom.TypeExpression(ast);
 		b.setType((descent.core.dom.Type) convert(a.sds));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TemplateType convert(TemplateInstanceWrapper a) {
		TemplateInstance tempinst = ((TemplateInstanceWrapper) a).tempinst;
 		TemplateType tt = new TemplateType(ast);
 		tt.setName(convert(tempinst.idents.get(0)));
 		if (tempinst.tiargs != null) {
 			for(ASTDmdNode node : tempinst.tiargs) {
 				tt.arguments().add(convert(node));
 			}
 		}
 		tt.setSourceRange(tempinst.start, tempinst.length);
 		return tt;
 	}
 	
 	public descent.core.dom.TemplateType convert(TemplateInstance a) {
 		descent.core.dom.TemplateType b = new descent.core.dom.TemplateType(ast);
 		b.setName(convert(a.idents.get(0)));
 		for(ASTDmdNode node : a.tiargs) {
 			b.arguments().add(convert(node));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.DotTemplateTypeExpression convert(DotTemplateInstanceExp a) {
 		descent.core.dom.DotTemplateTypeExpression b = new descent.core.dom.DotTemplateTypeExpression(ast);
 		if (a.e1 != null) {
 			b.setExpression(convert(a.e1));
 		}
 		if (a.ti != null) {
			b.setTemplateType((TemplateType) convert(a.ti));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ReturnStatement convert(ReturnStatement a) {
 		descent.core.dom.ReturnStatement b = new descent.core.dom.ReturnStatement(ast);
 		if (a.sourceExp != null) {
 			b.setExpression(convert(a.sourceExp));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ThrowStatement convert(ThrowStatement a) {
 		descent.core.dom.ThrowStatement b = new descent.core.dom.ThrowStatement(ast);
 		if (a.exp != null) {
 			b.setExpression(convert(a.exp));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ValueTemplateParameter convert(TemplateValueParameter a) {
 		descent.core.dom.ValueTemplateParameter b = new descent.core.dom.ValueTemplateParameter(ast);
 		b.setName(convert(a.ident));
 		b.setType(convert(a.valType));
 		if (a.defaultValue != null) {
 			b.setDefaultValue(convert(a.defaultValue));
 		}
 		if (a.specValue != null) {
 			b.setSpecificValue(convert(a.specValue));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TypeTemplateParameter convert(TemplateTypeParameter a) {
 		descent.core.dom.TypeTemplateParameter b = new descent.core.dom.TypeTemplateParameter(ast);
 		b.setName(convert(a.ident));
 		if (a.defaultType != null) {
 			b.setDefaultType(convert(a.defaultType));
 		}
 		if (a.specType != null) {
 			b.setSpecificType(convert(a.specType));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TupleTemplateParameter convert(TemplateTupleParameter a) {
 		descent.core.dom.TupleTemplateParameter b = new descent.core.dom.TupleTemplateParameter(ast);
 		b.setName(convert(a.ident));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AliasTemplateParameter convert(TemplateAliasParameter a) {
 		descent.core.dom.AliasTemplateParameter b = new descent.core.dom.AliasTemplateParameter(ast);
 		b.setName(convert(a.ident));
 		if (a.defaultAlias != null) {
 			b.setDefaultType(convert(a.defaultAlias));
 		}
 		if (a.specAliasT != null) {
 			b.setSpecificType(convert(a.specAliasT));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Declaration convert(TemplateDeclaration a) {
 		if (a.wrapper) {
 			Dsymbol wrappedSymbol = a.members.get(0);
 			if (wrappedSymbol.getNodeType() == ASTDmdNode.FUNC_DECLARATION) {
 				FunctionDeclaration b = (FunctionDeclaration) convert(wrappedSymbol);
 				if (a.preDdocs != null) {
 					convertDdoc(b.preDDocs(), a.preDdocs);
 				}
 				if (a.postDdoc != null) {
 					b.setPostDDoc(convertDdoc(a.postDdoc));
 				}
 				convertTemplateParameters(b.templateParameters(), a.parameters);
 				b.setSourceRange(a.start, a.length);
 				return b;
 			} else {
 				AggregateDeclaration b = (AggregateDeclaration) convert(wrappedSymbol);
 				if (a.preDdocs != null) {
 					convertDdoc(b.preDDocs(), a.preDdocs);
 				}
 				if (a.postDdoc != null) {
 					b.setPostDDoc(convertDdoc(a.postDdoc));
 				}
 				convertTemplateParameters(b.templateParameters(), a.parameters);
 				b.setSourceRange(a.start, a.length);
 				return b;
 			}
 		}
 		
 		descent.core.dom.TemplateDeclaration b = new descent.core.dom.TemplateDeclaration(ast);
 		b.setName(convert(a.ident));
 		convertTemplateParameters(b.templateParameters(), a.parameters);
 		convertDeclarations(b.declarations(), a.members);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.VoidInitializer convert(VoidInitializer a) {
 		descent.core.dom.VoidInitializer b = new descent.core.dom.VoidInitializer(ast);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.NewExpression convert(NewExp a) {
 		descent.core.dom.NewExpression b = new descent.core.dom.NewExpression(ast);
 		if (a.thisexp != null) {
 			b.setExpression(convert(a.thisexp));
 		}
 		if (a.newtype != null) {
 			b.setType(convert(a.newtype));
 		}
 		convertExpressions(b.newArguments(), a.newargs);
 		convertExpressions(b.constructorArguments(), a.arguments);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.NewAnonymousClassExpression convert(NewAnonClassExp a) {
 		descent.core.dom.NewAnonymousClassExpression b = new descent.core.dom.NewAnonymousClassExpression(ast);
 		if (a.thisexp != null) {
 			b.setExpression(convert(a.thisexp));
 		}
 		convertExpressions(b.newArguments(), a.newargs);
 		convertExpressions(b.constructorArguments(), a.arguments);
 		convertBaseClasses(b.baseClasses(), a.cd.baseclasses);
 		convertDeclarations(b.declarations(), a.cd.members);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Type convert(TypeSlice a) {
 		descent.core.dom.SliceType b = new descent.core.dom.SliceType(ast);
 		b.setComponentType(convert(a.next));
 		if (a.lwr != null) {
 			b.setFromExpression(convert(a.lwr));
 		}
 		if (a.upr != null) {
 			b.setToExpression(convert(a.upr));
 		}
 		b.setSourceRange(a.start, a.length);
 		return convertModifiedType(a, b);
 	}
 	
 	public descent.core.dom.Type convert(TypeSArray a) {
 		descent.core.dom.StaticArrayType b = new descent.core.dom.StaticArrayType(ast);
 		b.setComponentType(convert(a.next));
 		if (a.dim != null) {
 			descent.core.dom.Expression convertion = convert(a.dim);
 			if (convertion != null) {
 				b.setSize(convertion);
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return convertModifiedType(a, b);
 	}
 	
 	public descent.core.dom.Type convert(TypePointer a) {
 		if (a.next.ty == TY.Tfunction) {
 			descent.core.dom.DelegateType b = new descent.core.dom.DelegateType(ast);
 			b.setFunctionPointer(true);
 			b.setReturnType(convert(((TypeFunction) a.next).next));
 			b.setVariadic(((TypeFunction) a.next).varargs != 0);
 			convertArguments(b.arguments(), ((TypeFunction) a.next).parameters);
 			b.setSourceRange(a.start, a.length);
 			return convertModifiedType(a, b);
 		} else {
 			PointerType b = new PointerType(ast);
 			b.setComponentType(convert(a.next));
 			b.setSourceRange(a.start, a.length);
 			return convertModifiedType(a, b);
 		}
 	}
 	
 	public descent.core.dom.TypeDotIdentifierExpression convert(TypeDotIdExp a) {
 		descent.core.dom.TypeDotIdentifierExpression b = new descent.core.dom.TypeDotIdentifierExpression(ast);
 		if (a.type != null) {
 			b.setType(convert(a.type));
 		}
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Type convert(TypeDelegate a) {
 		descent.core.dom.DelegateType b = new descent.core.dom.DelegateType(ast);
 		b.setFunctionPointer(false);
 		TypeFunction ty = (TypeFunction) a.next;
 		b.setReturnType(convert(ty.next));
 		b.setVariadic(ty.varargs != 0);
 		convertArguments(b.arguments(), ty.parameters);
 		b.setSourceRange(a.start, a.length);
 		return convertModifiedType(a, b);
 	}
 	
 	public descent.core.dom.Type convert(TypeAArray a) {
 		descent.core.dom.AssociativeArrayType b = new descent.core.dom.AssociativeArrayType(ast);
 		b.setComponentType(convert(a.next));
 		b.setKeyType(convert(a.index));
 		b.setSourceRange(a.start, a.length);
 		return convertModifiedType(a, b);
 	}
 	
 	public descent.core.dom.Type convert(TypeDArray a) {
 		descent.core.dom.DynamicArrayType b = new descent.core.dom.DynamicArrayType(ast);
 		b.setComponentType(convert(a.next));
 		b.setSourceRange(a.start, a.length);
 		return convertModifiedType(a, b);
 	}
 	
 	public descent.core.dom.VariableDeclarationFragment convert(VarDeclaration a) {
 		descent.core.dom.VariableDeclarationFragment b = new descent.core.dom.VariableDeclarationFragment(ast);
 		b.setName(convert(a.ident));
 		if (a.sourceInit == null) {
 			b.setSourceRange(a.ident.start, a.ident.length);
 		} else {
 			descent.core.dom.Initializer init2 = convert(a.sourceInit);
 			b.setInitializer(init2);
 			b.setSourceRange(a.ident.start, init2.getStartPosition() + init2.getLength() - a.ident.start);
 		}
 		return b;
 	}
 	
 	public descent.core.dom.TypedefDeclarationFragment convert(TypedefDeclaration a) {
 		descent.core.dom.TypedefDeclarationFragment b = new descent.core.dom.TypedefDeclarationFragment(ast);
 		b.setName(convert(a.ident));
 		if (a.init == null) {
 			b.setSourceRange(a.ident.start, a.ident.length);
 		} else {
 			descent.core.dom.Initializer init = convert(a.init);
 			b.setInitializer(init);
 			b.setSourceRange(a.ident.start, init.getStartPosition() + init.getLength() - a.ident.start);
 		}
 		return b;
 	}
 	
 	public descent.core.dom.ImportDeclaration convert(MultiImport a) {
 		descent.core.dom.ImportDeclaration b = new descent.core.dom.ImportDeclaration(ast);
 		b.setStatic(a.isstatic);
 		if (a.imports != null) {
 			for(Import imp : a.imports) {
 				b.imports().add(convert(imp));
 			}
 		}
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Import convert(Import a) {
 		descent.core.dom.Import b = new descent.core.dom.Import(ast);
 		if (a.aliasId != null) {
 			b.setAlias(convert(a.aliasId));
 		}
 		b.setName(convert(a.packages, a.id));
 		if (a.aliases != null) {
 			for(int i = 0; i < a.aliases.size(); i++) {
 				IdentifierExp alias = a.aliases.get(i);
 				IdentifierExp name = a.names.get(i);
 				SelectiveImport selective = new SelectiveImport(ast);
 				selective.setName(convert(name));
 				if (alias == null) {
 					selective.setSourceRange(name.start, name.length);
 				} else {
 					selective.setAlias(convert(alias));
 					selective.setSourceRange(alias.start, name.start + name.length - alias.start);
 				}
 				b.selectiveImports().add(selective);
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ExternDeclaration convert(LinkDeclaration a) {
 		descent.core.dom.ExternDeclaration b = new descent.core.dom.ExternDeclaration(ast);
 		b.setLinkage(a.linkage.getLinkage());
 		convertDeclarations(b.declarations(), a.decl);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.LabeledStatement convert(LabelStatement a) {
 		descent.core.dom.LabeledStatement b = new descent.core.dom.LabeledStatement(ast);
 		b.setLabel(convert(a.ident));
 		b.setBody(convert(a.statement));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.InvariantDeclaration convert(InvariantDeclaration a) {
 		descent.core.dom.InvariantDeclaration b = new descent.core.dom.InvariantDeclaration(ast);
 		if (a.fbody != null) {
 			descent.core.dom.Statement convertedBody = convert(a.fbody);
 			if (convertedBody instanceof Block) {
 				b.setBody((Block) convertedBody);
 			}
 		}
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.UnitTestDeclaration convert(UnitTestDeclaration a) {
 		descent.core.dom.UnitTestDeclaration b = new descent.core.dom.UnitTestDeclaration(ast);
 		b.setBody((Block) convert(a.fbody));
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.IfStatement convert(IfStatement a) {
 		descent.core.dom.IfStatement b = new descent.core.dom.IfStatement(ast);
 		if (a.arg != null) {
 			b.setArgument(convert(a.arg));
 		}
		b.setExpression(convert(a.sourceCondition));
		b.setThenBody(convert(a.ifbody));
 		if (a.elsebody != null) {
 			b.setElseBody(convert(a.elsebody));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(IdentityExp a) {
 		switch(a.op) {
 		case TOKidentity:
			return convert((BinExp) a, InfixExpression.Operator.IDENTITY);
 		case TOKnotidentity:
			return convert((BinExp) a, InfixExpression.Operator.NOT_IDENTITY);
 		case TOKis:
			return convert((BinExp) a, InfixExpression.Operator.IS);
 		case TOKnotis:
			return convert((BinExp) a, InfixExpression.Operator.NOT_IS);
 		}
 		return null;
 	}
 	
 	public descent.core.dom.DeclarationStatement convert(CompileStatement a) {
 		descent.core.dom.DeclarationStatement b = new descent.core.dom.DeclarationStatement(ast);
 		MixinDeclaration mixin = new MixinDeclaration(ast);
 		mixin.setExpression(convert(a.exp));
 		mixin.setSourceRange(a.start, a.length);
 		b.setDeclaration(mixin);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.FunctionLiteralDeclarationExpression convert(FuncExp a) {
 		descent.core.dom.FunctionLiteralDeclarationExpression b = new descent.core.dom.FunctionLiteralDeclarationExpression(ast);
 		
 		if (a.fd.tok == TOK.TOKdelegate) {
 			b.setSyntax(Syntax.DELEGATE);
 		} else if (a.fd.tok == TOK.TOKfunction) {
 			b.setSyntax(Syntax.FUNCTION);
 		} else {
 			b.setSyntax(Syntax.EMPTY);
 		}
 		TypeFunction typeFunction = (TypeFunction) a.fd.type;
 		if (typeFunction.next != null) {
 			b.setReturnType(convert(typeFunction.next));
 		}
 		b.setVariadic(typeFunction.varargs != 0);
 		convertArguments(b.arguments(), typeFunction.parameters);
 		fillFunction(b, a.fd);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.FunctionDeclaration convert(FuncDeclaration a) {
 		descent.core.dom.FunctionDeclaration b = new descent.core.dom.FunctionDeclaration(ast);
 		b.setVariadic(((TypeFunction) a.type).varargs != 0);
 		b.setReturnType(convert(((TypeFunction) a.type).next));
 		b.setName(convert(a.ident));
 		convertArguments(b.arguments(), ((TypeFunction) a.type).parameters);
 		fillFunction(b, a);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ForeachStatement convert(ForeachStatement a) {
 		descent.core.dom.ForeachStatement b = new descent.core.dom.ForeachStatement(ast);
 		if (a.op == TOK.TOKforeach_reverse) {
 			b.setReverse(true);
 		}
 		convertArguments(b.arguments(), a.arguments);
 		b.setExpression(convert(a.aggr));
 		if (a.body != null) {
 			b.setBody(convert(a.body));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ForeachRangeStatement convert(ForeachRangeStatement a) {
 		descent.core.dom.ForeachRangeStatement b = new descent.core.dom.ForeachRangeStatement(ast);
 		if (a.op == TOK.TOKforeach_reverse) {
 			b.setReverse(true);
 		}
 		b.setArgument(convert(a.arg));
 		b.setFromExpression(convert(a.lwr));
 		b.setToExpression(convert(a.upr));
 		if (a.body != null) {
 			b.setBody(convert(a.body));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ForStatement convert(ForStatement a) {
 		descent.core.dom.ForStatement b = new descent.core.dom.ForStatement(ast);
 		if (a.init != null) {
 			b.setInitializer(convert(a.init));
 		}
 		if (a.condition != null) {
 			b.setCondition(convert(a.condition));
 		}
 		if (a.increment != null) {
 			b.setIncrement(convert(a.increment));
 		}
 		b.setBody(convert(a.body));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 
 	private descent.core.dom.Statement convertBlockVars(CompoundStatement block) {
 		List<VarDeclaration> varDeclarations = new ArrayList<VarDeclaration>();
 		for(Statement stm : block.statements) {
 			if (stm instanceof DeclarationStatement) {
 				DeclarationStatement declStm = (DeclarationStatement) stm;
 				Dsymbol declaration = ((DeclarationExp) declStm.exp).declaration;
 				if (declaration instanceof VarDeclaration) {
 					varDeclarations.add((VarDeclaration) declaration);
 				}
 			} else {
 				throw new RuntimeException("Can't happen");
 			}
 		}
 		
 		return wrapWithDeclarationStatement(convertManyVarDeclarations(varDeclarations));
 	}
 	
 	private descent.core.dom.Statement convertBlockAlias(CompoundStatement block) {
 		List<AliasDeclaration> varDeclarations = new ArrayList<AliasDeclaration>();
 		for(Statement stm : block.statements) {
 			if (stm instanceof DeclarationStatement) {
 				DeclarationStatement declStm = (DeclarationStatement) stm;
 				Dsymbol declaration = ((DeclarationExp) declStm.exp).declaration;
 				if (declaration instanceof AliasDeclaration) {
 					varDeclarations.add((AliasDeclaration) declaration);
 				}
 			} else {
 				throw new RuntimeException("Can't happen");
 			}
 		}
 		
 		return wrapWithDeclarationStatement(convertManyAliasDeclarations(varDeclarations));
 	}
 	
 	private descent.core.dom.Statement convertBlockTypedef(CompoundStatement block) {
 		List<TypedefDeclaration> varDeclarations = new ArrayList<TypedefDeclaration>();
 		for(Statement stm : block.statements) {
 			if (stm instanceof DeclarationStatement) {
 				DeclarationStatement declStm = (DeclarationStatement) stm;
 				Dsymbol declaration = ((DeclarationExp) declStm.exp).declaration;
 				if (declaration instanceof TypedefDeclaration) {
 					varDeclarations.add((TypedefDeclaration) declaration);
 				}
 			} else {
 				throw new RuntimeException("Can't happen");
 			}
 		}
 		
 		return wrapWithDeclarationStatement(convertManyTypedefDeclarations(varDeclarations));
 	}
 
 	private descent.core.dom.DeclarationStatement wrapWithDeclarationStatement(Declaration declaration) {
 		descent.core.dom.DeclarationStatement declStatement = ast.newDeclarationStatement();
 		declStatement.setDeclaration(declaration);
 		declStatement.setSourceRange(declaration.getStartPosition(), declaration.getLength());
 		return declStatement;
 	}
 
 	public descent.core.dom.EnumMember convert(EnumMember a) {
 		descent.core.dom.EnumMember b = new descent.core.dom.EnumMember(ast);
 		b.setName(convert(a.ident));
 		if (a.value != null) {
 			b.setValue(convert(a.value));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.EnumDeclaration convert(EnumDeclaration a) {
 		descent.core.dom.EnumDeclaration b = new descent.core.dom.EnumDeclaration(ast);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		if (a.memtype != null) {
 			b.setBaseType(convert(a.memtype));
 		}
 		if (a.members != null) {
 			for(Dsymbol symbol : a.members) {
 				b.enumMembers().add(convert((EnumMember) symbol));
 			}
 		}
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConstructorDeclaration convert(DtorDeclaration a) {
 		descent.core.dom.ConstructorDeclaration b = new descent.core.dom.ConstructorDeclaration(ast);
 		b.setKind(ConstructorDeclaration.Kind.DESTRUCTOR);
 		fillFunction(b, a);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConstructorDeclaration convert(CtorDeclaration a) {
 		descent.core.dom.ConstructorDeclaration b = new descent.core.dom.ConstructorDeclaration(ast);
 		b.setKind(ConstructorDeclaration.Kind.CONSTRUCTOR);
 		b.setVariadic(a.varargs != 0);
 		convertArguments(b.arguments(), a.arguments);
 		fillFunction(b, a);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConstructorDeclaration convert(StaticCtorDeclaration a) {
 		descent.core.dom.ConstructorDeclaration b = new descent.core.dom.ConstructorDeclaration(ast);
 		b.setKind(ConstructorDeclaration.Kind.STATIC_CONSTRUCTOR);
 		fillFunction(b, a);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConstructorDeclaration convert(StaticDtorDeclaration a) {
 		descent.core.dom.ConstructorDeclaration b = new descent.core.dom.ConstructorDeclaration(ast);
 		b.setKind(ConstructorDeclaration.Kind.STATIC_DESTRUCTOR);
 		fillFunction(b, a);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConstructorDeclaration convert(NewDeclaration a) {
 		descent.core.dom.ConstructorDeclaration b = new descent.core.dom.ConstructorDeclaration(ast);
 		b.setKind(ConstructorDeclaration.Kind.NEW);
 		convertArguments(b.arguments(), a.arguments);
 		fillFunction(b, a);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConstructorDeclaration convert(DeleteDeclaration a) {
 		descent.core.dom.ConstructorDeclaration b = new descent.core.dom.ConstructorDeclaration(ast);
 		b.setKind(ConstructorDeclaration.Kind.DELETE);
 		convertArguments(b.arguments(), a.arguments);
 		fillFunction(b, a);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public void fillFunction(descent.core.dom.FunctionLiteralDeclarationExpression b, FuncDeclaration a) {
 		if (a.frequire != null) {
 			b.setPrecondition((Block) convert(a.frequire));
 		}
 		if (a.fensure != null) {
 			b.setPostcondition((Block) convert(a.fensure));
 		}
 		if (a.outId != null) {
 			b.setPostconditionVariableName(convert(a.outId));
 		}
 		if (a.fbody != null) {
 			b.setBody((Block) convert(a.fbody));
 		}
 	}
 	
 	public void fillFunction(descent.core.dom.AbstractFunctionDeclaration b, FuncDeclaration a) {
 		if (a.sourceFrequire != null) {
 			b.setPrecondition((Block) convert(a.sourceFrequire));
 		}
 		if (a.sourceFensure != null) {
 			b.setPostcondition((Block) convert(a.sourceFensure));
 		}
 		if (a.outId != null) {
 			b.setPostconditionVariableName(convert(a.outId));
 		}
 		if (a.sourceFbody != null) {
 			descent.core.dom.Block body = (Block) convert(a.sourceFbody);
 			if (body != null) {
 				b.setBody(body);
 			}
 		}
 	}
 	
 	public void fillDeclaration(descent.core.dom.Declaration b, ASTDmdNode a) {
 		convertModifiers(b.modifiers(), a.modifiers);
 		if (a.preDdocs != null) {
 			convertDdoc(b.preDDocs(), a.preDdocs);
 		}
 		if (a.postDdoc != null) {
 			b.setPostDDoc(convertDdoc(a.postDdoc));
 		}
 	}
 	
 	public descent.core.dom.GotoStatement convert(GotoStatement a) {
 		descent.core.dom.GotoStatement b = new descent.core.dom.GotoStatement(ast);
 		if (a.ident != null) {
 			b.setLabel(convert(a.ident));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.GotoDefaultStatement convert(GotoDefaultStatement a) {
 		descent.core.dom.GotoDefaultStatement b = new descent.core.dom.GotoDefaultStatement(ast);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.GotoCaseStatement convert(GotoCaseStatement a) {
 		descent.core.dom.GotoCaseStatement b = new descent.core.dom.GotoCaseStatement(ast);
 		if (a.exp != null) {
 			b.setLabel(convert(a.exp));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.FileImportExpression convert(FileExp a) {
 		descent.core.dom.FileImportExpression b = new descent.core.dom.FileImportExpression(ast);
 		b.setExpression(convert(a.e1));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Statement convert(ExpStatement a) {
 		if (a.exp == null) {
 			descent.core.dom.EmptyStatement b = new descent.core.dom.EmptyStatement(ast);
 			b.setSourceRange(a.start, a.length);
 			return b;
 		} else {
 			descent.core.dom.ExpressionStatement b = new descent.core.dom.ExpressionStatement(ast);
 			b.setExpression(convert(a.exp));
 			b.setSourceRange(a.start, a.length);
 			return b;
 		}
 	}
 	
 	public descent.core.dom.ExpressionInitializer convert(ExpInitializer a) {
 		descent.core.dom.ExpressionInitializer b = new descent.core.dom.ExpressionInitializer(ast);
 		b.setExpression(convert(a.sourceExp));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(EqualExp a) {
		return convert((BinExp) a, a.op == TOK.TOKequal ? InfixExpression.Operator.EQUALS : InfixExpression.Operator.NOT_EQUALS);
 	}
 	
 	public descent.core.dom.DotIdentifierExpression convert(DotIdExp a) {
 		descent.core.dom.DotIdentifierExpression b = new descent.core.dom.DotIdentifierExpression(ast);
 		if (a.e1 != null) {
 			b.setExpression(convert(a.e1));
 		}
 		b.setName(convert(a.ident));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.DollarLiteral convert(DollarExp a) {
 		descent.core.dom.DollarLiteral b = new descent.core.dom.DollarLiteral(ast);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.DoStatement convert(DoStatement a) {
 		descent.core.dom.DoStatement b = new descent.core.dom.DoStatement(ast);
 		b.setBody(convert(a.body));
 		b.setExpression(convert(a.condition));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.WhileStatement convert(WhileStatement a) {
 		descent.core.dom.WhileStatement b = new descent.core.dom.WhileStatement(ast);
 		b.setBody(convert(a.body));
 		b.setExpression(convert(a.condition));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.WithStatement convert(WithStatement a) {
 		descent.core.dom.WithStatement b = new descent.core.dom.WithStatement(ast);
 		b.setBody(convert(a.body));
 		b.setExpression(convert(a.exp));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.SynchronizedStatement convert(SynchronizedStatement a) {
 		descent.core.dom.SynchronizedStatement b = new descent.core.dom.SynchronizedStatement(ast);
 		b.setBody(convert(a.body));
 		if (a.exp != null) {
 			b.setExpression(convert(a.exp));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.VolatileStatement convert(VolatileStatement a) {
 		descent.core.dom.VolatileStatement b = new descent.core.dom.VolatileStatement(ast);
 		b.setBody(convert(a.statement));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.SwitchStatement convert(SwitchStatement a) {
 		descent.core.dom.SwitchStatement b = new descent.core.dom.SwitchStatement(ast);
 		b.setBody(convert(a.body));
 		b.setExpression(convert(a.condition));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConditionalDeclaration convert(ConditionalDeclaration a) {
 		descent.core.dom.ConditionalDeclaration ret = null;
 		switch (a.condition.getConditionType()) {
 		case Condition.DEBUG: {
 			DebugCondition cond = (DebugCondition) a.condition;
 			DebugDeclaration b = new DebugDeclaration(ast);
 			if (cond.id != null) {
 				descent.core.dom.Version version = ast.newVersion(new String(
 						cond.id.string));
 				version.setSourceRange(cond.id.startPosition, cond.id.length);
 				b.setVersion(version);
 			}
 			ret = b;
 			break;
 		}
 		case Condition.IFTYPE: {
 			IftypeCondition cond = (IftypeCondition) a.condition;
 			IftypeDeclaration b = new IftypeDeclaration(ast);
 			if (cond.tok != null) {
 				switch (cond.tok) {
 				case TOKreserved:
 					b.setKind(IftypeDeclaration.Kind.NONE);
 					break;
 				case TOKequal:
 					b.setKind(IftypeDeclaration.Kind.EQUALS);
 					break;
 				case TOKcolon:
 					b.setKind(IftypeDeclaration.Kind.EXTENDS);
 					break;
 				}
 			}
 			if (cond.ident != null) {
 				b.setName(convert(cond.ident));
 			}
 			if (cond.targ != null) {
 				b.setTestType(convert(cond.targ));
 			}
 			if (cond.tspec != null) {
 				b.setMatchingType(convert(cond.tspec));
 			}
 			ret = b;
 			break;
 		}
 		case Condition.STATIC_IF: {
 			StaticIfCondition cond = (StaticIfCondition) a.condition;
 			StaticIfDeclaration b = new StaticIfDeclaration(ast);
 			if (cond.exp != null) {
 				b.setExpression(convert(cond.exp));
 			}
 			ret = b;
 			break;
 		}
 		case Condition.VERSION: {
 			VersionCondition cond = (VersionCondition) a.condition;
 			VersionDeclaration b = new VersionDeclaration(ast);
 			if (cond.id != null) {
 				descent.core.dom.Version version = ast.newVersion(new String(
 						cond.id.string));
 				version.setSourceRange(cond.id.startPosition, cond.id.length);
 				b.setVersion(version);
 			}
 			ret = b;
 			break;
 		}
 		}
 		convertDeclarations(ret.thenDeclarations(), a.decl);
 		convertDeclarations(ret.elseDeclarations(), a.elsedecl);
 		fillDeclaration(ret, a);
 		ret.setSourceRange(a.start, a.length);
 		return ret;
 	}
 	
 	public descent.core.dom.ConditionalStatement convert(ConditionalStatement a) {
 		descent.core.dom.ConditionalStatement ret = null;
 		switch(a.condition.getConditionType()) {
 			case Condition.DEBUG:
 			{
 				DebugCondition cond = (DebugCondition) a.condition;
 				DebugStatement b = new DebugStatement(ast);
 				if (cond.id != null) {
 					descent.core.dom.Version version = ast.newVersion(new String(cond.id.string));
 					version.setSourceRange(cond.id.startPosition, cond.id.length);
 					b.setVersion(version);
 				}
 				ret = b;
 				break;
 			}
 			case Condition.IFTYPE:
 			{
 				IftypeCondition cond = (IftypeCondition) a.condition;
 				IftypeStatement b = new IftypeStatement(ast);
 				if (cond.tok != null) {
 					switch(cond.tok) {
 					case TOKreserved:
 						b.setKind(IftypeDeclaration.Kind.NONE);
 						break;
 					case TOKequal:
 						b.setKind(IftypeDeclaration.Kind.EQUALS);
 						break;
 					case TOKcolon:
 						b.setKind(IftypeDeclaration.Kind.EXTENDS);
 						break;
 					}
 				}
 				if (cond.ident != null) {
 					b.setName(convert(cond.ident));
 				}
 				if (cond.targ != null) {
 					b.setTestType(convert(cond.targ));
 				}
 				if (cond.tspec != null) {
 					b.setMatchingType(convert(cond.tspec));
 				}
 				ret = b;
 				break;
 			}
 			case Condition.STATIC_IF:
 			{
 				StaticIfCondition cond = (StaticIfCondition) a.condition;
 				StaticIfStatement b = new StaticIfStatement(ast);
 				if (cond.exp != null) {
 					b.setExpression(convert(cond.exp));
 				}
 				ret = b;
 				break;
 			}
 			case Condition.VERSION:
 			{
 				VersionCondition cond = (VersionCondition) a.condition;
 				VersionStatement b = new VersionStatement(ast);
 				if (cond.id != null) {
 					descent.core.dom.Version version = ast.newVersion(new String(cond.id.string));
 					version.setSourceRange(cond.id.startPosition, cond.id.length);
 					b.setVersion(version);
 				}
 				ret = b;
 				break;
 			}
 		}
 		if (a.ifbody != null) {
 			ret.setThenBody(convert(a.ifbody));
 		}
 		if (a.elsebody != null) {
 			ret.setElseBody(convert(a.elsebody));
 		}
 		ret.setSourceRange(a.start, a.length);
 		return ret;
 	}
 	
 	public descent.core.dom.DeleteExpression convert(DeleteExp a) {
 		descent.core.dom.DeleteExpression b = new descent.core.dom.DeleteExpression(ast);
 		b.setExpression(convert(a.e1));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.DefaultStatement convert(DefaultStatement a) {
 		descent.core.dom.DefaultStatement b = new descent.core.dom.DefaultStatement(ast);
 		convertStatements(b.statements(), ((CompoundStatement) a.statement).statements);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.PrefixExpression convert(IncrementExp a) {
 		descent.core.dom.PrefixExpression b = new descent.core.dom.PrefixExpression(ast);
 		b.setOperator(PrefixExpression.Operator.INCREMENT);
 		b.setOperand(convert(a.e1));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.PrefixExpression convert(DecrementExp a) {
 		descent.core.dom.PrefixExpression b = new descent.core.dom.PrefixExpression(ast);
 		b.setOperator(PrefixExpression.Operator.DECREMENT);
 		b.setOperand(convert(a.e1));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.DeclarationStatement convert(DeclarationStatement a) {
 		descent.core.dom.DeclarationStatement b = new descent.core.dom.DeclarationStatement(ast);
 		
 		Declaration declaration = convertDeclaration(((DeclarationExp) a.exp).declaration);
 		b.setDeclaration(declaration);		
 		declaration.setSourceRange(a.start, a.length);
 		
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Version convert(Version a) {
 		descent.core.dom.Version b = new descent.core.dom.Version(ast);
 		b.setValue(new String(a.value));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.DebugAssignment convert(DebugSymbol a) {
 		descent.core.dom.DebugAssignment b = new descent.core.dom.DebugAssignment(ast);
 		b.setVersion(convert(a.version));
 		b.setSourceRange(a.start, a.length);
 		fillDeclaration(b, a);
 		return b;
 	}
 	
 	public descent.core.dom.VersionAssignment convert(VersionSymbol a) {
 		descent.core.dom.VersionAssignment b = new descent.core.dom.VersionAssignment(ast);
 		b.setVersion(convert(a.version));
 		b.setSourceRange(a.start, a.length);
 		fillDeclaration(b, a);
 		return b;
 	}
 	
 	public descent.core.dom.ContinueStatement convert(ContinueStatement a) {
 		descent.core.dom.ContinueStatement b = new descent.core.dom.ContinueStatement(ast);
 		if (a.ident != null) {
 			b.setLabel(convert(a.ident));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ConditionalExpression convert(CondExp a) {
 		descent.core.dom.ConditionalExpression b = new descent.core.dom.ConditionalExpression(ast);
 		b.setExpression(convert(a.econd));
 		b.setThenExpression(convert(a.e1));
 		b.setElseExpression(convert(a.e2));
 		b.setSourceRange(a.econd.start, a.e2.start + a.e2.length - a.econd.start);
 		return b;
 	}
 	
 	public descent.core.dom.Statement convert(CompoundStatement a) {
 		if (a.synthetic) {
 			List<descent.core.dom.Statement> stms = new ArrayList<descent.core.dom.Statement>(1);
 			convertStatements(stms, a.sourceStatements);
 			if (stms.size() == 1) {
 				return stms.get(0);
 			} else if (stms.size() == 0) {
 				return null;
 			} else {
 				throw new IllegalStateException("Should not happen");
 			}
 		} else {
 			if (a.manyVars) {
 				Statement firstStatement = a.statements.get(0);
 				
 				if (!(firstStatement instanceof DeclarationStatement)) {
 					throw new RuntimeException("Can't happen");
 				}
 				
 				DeclarationStatement declStm = (DeclarationStatement) firstStatement;
 				Dsymbol declaration = ((DeclarationExp) declStm.exp).declaration;
 				if (declaration instanceof VarDeclaration) {
 					return convertBlockVars(a);
 				} else if (declaration instanceof AliasDeclaration) {
 					return convertBlockAlias(a);
 				} else {
 					if (!(declaration instanceof TypedefDeclaration)) {
 						throw new RuntimeException("Can't happen");
 					}
 					return convertBlockTypedef(a);
 				}
 			} else {
 				descent.core.dom.Block b = new descent.core.dom.Block(ast);
 				convertStatements(b.statements(), a.sourceStatements);
 				b.setSourceRange(a.start, a.length);
 				return b;
 			}
 		}
 	}
 	
 	public descent.core.dom.MixinExpression convert(CompileExp a) {
 		descent.core.dom.MixinExpression b = new descent.core.dom.MixinExpression(ast);
 		b.setExpression(convert(a.e1));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.MixinDeclaration convert(CompileDeclaration a) {
 		descent.core.dom.MixinDeclaration b = new descent.core.dom.MixinDeclaration(ast);
 		b.setExpression(convert(a.exp));
 		b.setSourceRange(a.start, a.length);
 		fillDeclaration(b, a);
 		return b;
 	}
 	
 	public descent.core.dom.AliasDeclarationFragment convert(AliasDeclaration a) {
 		descent.core.dom.AliasDeclarationFragment b = new descent.core.dom.AliasDeclarationFragment(ast);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 			b.setSourceRange(a.ident.start, a.ident.length);
 		}
 		return b;
 	}
 	
 	public descent.core.dom.AlignDeclaration convert(AlignDeclaration a) {
 		descent.core.dom.AlignDeclaration b = new descent.core.dom.AlignDeclaration(ast);
 		b.setAlign(a.salign);
 		convertDeclarations(b.declarations(), a.decl);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AggregateDeclaration convert(AnonDeclaration a) {
 		descent.core.dom.AggregateDeclaration b = new descent.core.dom.AggregateDeclaration(ast);
 		if (a.isunion) {
 			b.setKind(AggregateDeclaration.Kind.UNION);
 		} else {
 			b.setKind(AggregateDeclaration.Kind.STRUCT);
 		}
 		convertDeclarations(b.declarations(), a.decl);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Argument convert(Argument a) {
 		descent.core.dom.Argument b = new descent.core.dom.Argument(ast);
 		if (a.modifiers != null) {
 			convertModifiers(b.modifiers(), a.modifiers);
 		}
 		if (a.type != null) {
 			descent.core.dom.Type convertedType = convert(a.type);
 			if (convertedType != null) {
 				b.setType(convertedType);
 			}
 		}
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		if (a.sourceDefaultArg != null) {
 			b.setDefaultValue(convert(a.sourceDefaultArg));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ArrayAccess convert(ArrayExp a) {
 		descent.core.dom.ArrayAccess b = new descent.core.dom.ArrayAccess(ast);
 		b.setArray(convert(a.e1));
 		convertExpressions(b.indexes(), a.arguments);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ArrayInitializer convert(ArrayInitializer a) {
 		descent.core.dom.ArrayInitializer b = new descent.core.dom.ArrayInitializer(ast);
 		if (a.index != null) {
 			for(int i = 0; i < a.index.size(); i++) {
 				Expression index = a.index.get(i);
 				Initializer value = a.value.get(i);
 				ArrayInitializerFragment fragment = new ArrayInitializerFragment(ast);
 				if (index == null) {
 					fragment.setInitializer(convert(value));
 					fragment.setSourceRange(value.start, value.length);
 				} else {
 					fragment.setExpression(convert(index));
 					fragment.setInitializer(convert(value));
 					fragment.setSourceRange(index.start, value.start + value.length - index.start);
 				}
 				b.fragments().add(fragment);
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;		
 	}
 	
 	public descent.core.dom.StructInitializer convert(StructInitializer a) {
 		descent.core.dom.StructInitializer b = new descent.core.dom.StructInitializer(ast);
 		if (a.field != null) {
 			for(int i = 0; i < a.field.size(); i++) {
 				IdentifierExp index = a.field.get(i);
 				Initializer value = a.value.get(i);
 				StructInitializerFragment fragment = new StructInitializerFragment(ast);
 				if (index == null) {
 					fragment.setInitializer(convert(value));
 					fragment.setSourceRange(value.start, value.length);
 				} else {
 					fragment.setName(convert(index));
 					fragment.setInitializer(convert(value));
 					fragment.setSourceRange(index.start, value.start + value.length - index.start);
 				}
 				b.fragments().add(fragment);
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ArrayLiteral convert(ArrayLiteralExp a) {
 		descent.core.dom.ArrayLiteral b = new descent.core.dom.ArrayLiteral(ast);
 		convertExpressions(b.arguments(), a.elements);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AsmBlock convert(AsmBlock a) {
 		descent.core.dom.AsmBlock b = new descent.core.dom.AsmBlock(ast);
 		convertStatements(b.statements(), a.sourceStatements);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AsmStatement convert(AsmStatement a) {
 		descent.core.dom.AsmStatement b = new descent.core.dom.AsmStatement(ast);
 		if (a.toklist != null) {
 			for(Token token : a.toklist) {
 				AsmToken asmToken = new AsmToken(ast);
 				asmToken.setToken(token.toString());
 				asmToken.setSourceRange(token.ptr, token.len);
 				b.tokens().add(asmToken);
 			}
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AssertExpression convert(AssertExp a) {
 		descent.core.dom.AssertExpression b = new descent.core.dom.AssertExpression(ast);
 		b.setExpression(convert(a.e1));
 		if (a.msg != null) {
 			b.setMessage(convert(a.msg));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.BaseClass convert(descent.internal.compiler.parser.BaseClass a) {
 		descent.core.dom.BaseClass b = new descent.core.dom.BaseClass(ast);
 		if (a.modifier != null) {
 			b.setModifier(convert(a.modifier));
 			b.setSourceRange(a.modifier.start, a.sourceType.start + a.sourceType.length - a.modifier.start);
 		} else {
 			b.setSourceRange(a.sourceType.start, a.sourceType.length);
 		}
 		b.setType(convert(a.sourceType));
 		return b;
 	}
 	
 	public descent.core.dom.BreakStatement convert(BreakStatement a) {
 		descent.core.dom.BreakStatement b = new descent.core.dom.BreakStatement(ast);
 		if (a.ident != null) {
 			b.setLabel(convert(a.ident));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.CallExpression convert(CallExp a) {
 		descent.core.dom.CallExpression b = new descent.core.dom.CallExpression(ast);
 		b.setExpression(convert(a.e1));
 		convertExpressions(b.arguments(), a.arguments);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.SwitchCase convert(CaseStatement a) {
 		descent.core.dom.SwitchCase b = new descent.core.dom.SwitchCase(ast);
 		
 		CaseStatement x = a;
 		while(x.statement instanceof CaseStatement) {
 			b.expressions().add(convert(x.exp));
 			x = (CaseStatement) x.statement;
 		}
 		if (x.exp != null) {
 			b.expressions().add(convert(x.exp));
 		}
 		if (x.statement != null && ((CompoundStatement) x.statement).statements.size() > 0) {
 			convertStatements(b.statements(), ((CompoundStatement) x.statement).statements);
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(CastExp a) {
 		if (a.tok == null) {
 			descent.core.dom.CastExpression b = new descent.core.dom.CastExpression(ast);
 			if (a.to != null) {
 				b.setType(convert(a.to));
 			}
 			b.setExpression(convert(a.e1));
 			b.setSourceRange(a.start, a.length);
 			return b;
 		} else {
 			descent.core.dom.CastToModifierExpression b = new descent.core.dom.CastToModifierExpression(ast);
 			
 			descent.core.dom.Modifier modifier = new descent.core.dom.Modifier(ast);
 			if (a.tok == TOK.TOKconst) {
 				// const.length() == 5
 				modifier.setModifierKeyword(ModifierKeyword.CONST_KEYWORD);
 				modifier.setSourceRange(a.modifierStart, 5);
 			} else {
 				// invariant.length() == 9
 				modifier.setModifierKeyword(ModifierKeyword.INVARIANT_KEYWORD);
 				modifier.setSourceRange(a.modifierStart, 9);
 			}
 			b.setModifier(modifier);
 			
 			b.setExpression(convert(a.e1));
 			b.setSourceRange(a.start, a.length);
 			return b;
 		}
 	}
 	
 	public descent.core.dom.CatchClause convert(Catch a) {
 		descent.core.dom.CatchClause b = new descent.core.dom.CatchClause(ast);
 		if (a.type != null) {
 			b.setType(convert(a.type));
 		}
 		if (a.id != null) {
 			b.setName(convert(a.id));
 		}
 		if (a.handler != null) {
 			b.setBody(convert(a.handler));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AggregateDeclaration convert(InterfaceDeclaration a) {
 		descent.core.dom.AggregateDeclaration b = new descent.core.dom.AggregateDeclaration(ast);
 		b.setKind(AggregateDeclaration.Kind.INTERFACE);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		convertBaseClasses(b.baseClasses(), a.sourceBaseclasses);
 		convertDeclarations(b.declarations(), a.members);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AggregateDeclaration convert(ClassDeclaration a) {
 		descent.core.dom.AggregateDeclaration b = new descent.core.dom.AggregateDeclaration(ast);
 		b.setKind(AggregateDeclaration.Kind.CLASS);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		convertBaseClasses(b.baseClasses(), a.sourceBaseclasses);
 		convertDeclarations(b.declarations(), a.members);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AggregateDeclaration convert(StructDeclaration a) {
 		descent.core.dom.AggregateDeclaration b = new descent.core.dom.AggregateDeclaration(ast);
 		b.setKind(AggregateDeclaration.Kind.STRUCT);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		convertDeclarations(b.declarations(), a.members);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.AggregateDeclaration convert(UnionDeclaration a) {
 		descent.core.dom.AggregateDeclaration b = new descent.core.dom.AggregateDeclaration(ast);
 		b.setKind(AggregateDeclaration.Kind.UNION);
 		if (a.ident != null) {
 			b.setName(convert(a.ident));
 		}
 		convertDeclarations(b.declarations(), a.members);
 		fillDeclaration(b, a);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.InfixExpression convert(CmpExp a) {
 		descent.core.dom.InfixExpression b = new descent.core.dom.InfixExpression(ast);
 		b.setLeftOperand(convert(a.e1));
 		switch(a.op) {
 		case TOKlt: b.setOperator(InfixExpression.Operator.LESS); break;
 	    case TOKle: b.setOperator(InfixExpression.Operator.LESS_EQUALS); break;
 	    case TOKgt: b.setOperator(InfixExpression.Operator.GREATER); break;
 	    case TOKge: b.setOperator(InfixExpression.Operator.GREATER_EQUALS); break;
 	    case TOKunord: b.setOperator(InfixExpression.Operator.NOT_LESS_GREATER_EQUALS); break;
 	    case TOKlg: b.setOperator(InfixExpression.Operator.LESS_GREATER); break;
 	    case TOKleg: b.setOperator(InfixExpression.Operator.LESS_GREATER_EQUALS); break;
 	    case TOKule: b.setOperator(InfixExpression.Operator.NOT_GREATER); break;
 	    case TOKul: b.setOperator(InfixExpression.Operator.NOT_GREATER_EQUALS); break;
 	    case TOKuge: b.setOperator(InfixExpression.Operator.NOT_LESS); break;
 	    case TOKug: b.setOperator(InfixExpression.Operator.NOT_LESS_EQUALS); break;
 	    case TOKue:  b.setOperator(InfixExpression.Operator.NOT_LESS_GREATER); break;
 		}
 		b.setRightOperand(convert(a.e2));
 		b.setSourceRange(a.e1.start, a.e2.start + a.e2.length - a.e1.start);
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(IntegerExp a) {
 		if (a.type == Type.tbool) {
 			BooleanLiteral b = new BooleanLiteral(ast);
 			b.setBooleanValue(a.value.equals(BigInteger.ONE));
 			b.setSourceRange(a.start, a.length);
 			return b;
 		} else if (a.type == Type.tchar || a.type == Type.twchar || a.type == Type.tdchar) {
 			CharacterLiteral b = new CharacterLiteral(ast);
 			b.internalSetEscapedValue(new String(a.str));
 			b.setSourceRange(a.start, a.length);
 			return b;
 		} else {
 			descent.core.dom.NumberLiteral b = new descent.core.dom.NumberLiteral(ast);
 			b.internalSetToken(new String(a.str));
 			b.setSourceRange(a.start, a.length);
 			return b;
 		}
 	}
 	
 	public descent.core.dom.NumberLiteral convert(RealExp a) {
 		descent.core.dom.NumberLiteral b = new descent.core.dom.NumberLiteral(ast);
 		b.internalSetToken(new String(a.str));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.NullLiteral convert(NullExp a) {
 		descent.core.dom.NullLiteral b = new descent.core.dom.NullLiteral(ast);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.ThisLiteral convert(ThisExp a) {
 		descent.core.dom.ThisLiteral b = new descent.core.dom.ThisLiteral(ast);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.SuperLiteral convert(SuperExp a) {
 		descent.core.dom.SuperLiteral b = new descent.core.dom.SuperLiteral(ast);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.StringLiteral convert(StringExp a) {
 		descent.core.dom.StringLiteral b = new descent.core.dom.StringLiteral(ast);
 		b.internalSetEscapedValue(new String(a.string));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.StringsExpression convert(MultiStringExp a) {
 		descent.core.dom.StringsExpression b = new descent.core.dom.StringsExpression(ast);
 		for(StringExp exp : a.strings) {
 			b.stringLiterals().add(convert(exp));
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Type convert(TypeIdentifier a) {
 		descent.core.dom.SimpleType b;
 		if (a.ident != null && !CharOperation.equals(a.ident.ident, Id.empty)) {
 			b = new descent.core.dom.SimpleType(ast);
 			b.setName(convert(a.ident));
 			b.setSourceRange(a.ident.start, a.ident.length);
 		} else {
 			b = null;
 		}
 		if (a.idents == null || a.idents.isEmpty()) {
 			return convertModifiedType(a, b);
 		} else {
 			return convertModifiedType(a, convertQualifiedType(b, a, a.start));
 		}
 	}
 	
 	public descent.core.dom.Type convert(TypeTypeof a) {
 		descent.core.dom.TypeofType b = new descent.core.dom.TypeofType(ast);
 		b.setExpression(convert(a.exp));
 		if (a.idents == null || a.idents.size() == 0) {
 			b.setSourceRange(a.start, a.length);
 			return convertModifiedType(a, b);
 		} else {
 			b.setSourceRange(a.typeofStart, a.typeofLength);
 			return convertModifiedType(a, convertQualifiedType(b, a, a.start));
 		}
 	}
 	
 	public descent.core.dom.Type convert(TypeInstance a) {
 		descent.core.dom.TemplateType b = new descent.core.dom.TemplateType(ast);
 		b.setName(convert(a.tempinst.idents.get(0)));
 		if (a.tempinst.tiargs != null) {
 			for(ASTDmdNode node : a.tempinst.tiargs) {
 				b.arguments().add(convert(node));
 			}
 		}
 		b.setSourceRange(a.tempinst.start, a.tempinst.length);
 		if (a.idents != null && a.idents.size() > 0) {
 			return convertQualifiedType(b, a, a.start);
 		}
 		return convertModifiedType(a, b);
 	}
 	
 	public descent.core.dom.Type convertQualifiedType(descent.core.dom.Type q, TypeQualified a, int start) {
 		for(IdentifierExp idExp : a.idents) {
 			if (idExp instanceof TemplateInstanceWrapper) {
 				descent.core.dom.Type tt = convert((TemplateInstanceWrapper) idExp);
 				q = ast.newQualifiedType(q, tt);
 				q.setSourceRange(start, a.start + a.length - start);
 			} else {
 				descent.core.dom.SimpleName n = convert(idExp);
 				descent.core.dom.SimpleType t = ast.newSimpleType(n);
 				t.setSourceRange(n.getStartPosition(), n.getLength());
 				q = ast.newQualifiedType(q, t);
 				q.setSourceRange(start, idExp.start + idExp.length - start);
 			}
 		}
 		return q;
 	}
 	
 	public descent.core.dom.ParenthesizedExpression convert(ParenExp a) {
 		descent.core.dom.ParenthesizedExpression b = new descent.core.dom.ParenthesizedExpression(ast);
 		b.setExpression(convert(a.e1));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.TypeExpression convert(TypeExp a) {
 		descent.core.dom.TypeExpression b = new descent.core.dom.TypeExpression(ast);
 		b.setType(convert(a.type));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Type convert(TypeBasic a) {
 		descent.core.dom.PrimitiveType b = new descent.core.dom.PrimitiveType(ast);
 		switch(a.ty) {
 		case Tvoid: b.setPrimitiveTypeCode(PrimitiveType.Code.VOID); break;
 		case Tint8: b.setPrimitiveTypeCode(PrimitiveType.Code.BYTE); break;
 		case Tuns8: b.setPrimitiveTypeCode(PrimitiveType.Code.UBYTE); break;
 		case Tint16: b.setPrimitiveTypeCode(PrimitiveType.Code.SHORT); break;
 		case Tuns16: b.setPrimitiveTypeCode(PrimitiveType.Code.USHORT); break;
 		case Tint32: b.setPrimitiveTypeCode(PrimitiveType.Code.INT); break;
 		case Tuns32: b.setPrimitiveTypeCode(PrimitiveType.Code.UINT); break;
 		case Tint64: b.setPrimitiveTypeCode(PrimitiveType.Code.LONG); break;
 		case Tuns64: b.setPrimitiveTypeCode(PrimitiveType.Code.ULONG); break;
 		case Tfloat32: b.setPrimitiveTypeCode(PrimitiveType.Code.FLOAT); break;
 		case Tfloat64: b.setPrimitiveTypeCode(PrimitiveType.Code.DOUBLE); break;
 		case Tfloat80: b.setPrimitiveTypeCode(PrimitiveType.Code.REAL); break;
 		case Timaginary32: b.setPrimitiveTypeCode(PrimitiveType.Code.IFLOAT); break;
 		case Timaginary64: b.setPrimitiveTypeCode(PrimitiveType.Code.IDOUBLE); break;
 		case Timaginary80: b.setPrimitiveTypeCode(PrimitiveType.Code.IREAL); break;
 		case Tcomplex32: b.setPrimitiveTypeCode(PrimitiveType.Code.COMPLEX32); break;
 		case Tcomplex64: b.setPrimitiveTypeCode(PrimitiveType.Code.COMPLEX64); break;
 		case Tcomplex80: b.setPrimitiveTypeCode(PrimitiveType.Code.COMPLEX80); break;
 		case Tbit: b.setPrimitiveTypeCode(PrimitiveType.Code.BIT); break;
 		case Tbool: b.setPrimitiveTypeCode(PrimitiveType.Code.BOOL); break;
 		case Tchar: b.setPrimitiveTypeCode(PrimitiveType.Code.CHAR); break;
 		case Twchar: b.setPrimitiveTypeCode(PrimitiveType.Code.WCHAR); break;
 		case Tdchar: b.setPrimitiveTypeCode(PrimitiveType.Code.DCHAR); break;
 		}
 		b.setSourceRange(a.start, a.length);
 		return convertModifiedType(a, b);
 	}
 	
 	private descent.core.dom.Type convertModifiedType(Type a, descent.core.dom.Type b) {
 		if (a.modifications != null) {
 			for(Modification modification : a.modifications) {
 				ModifiedType c = new ModifiedType(ast);
 				
 				descent.core.dom.Modifier modifier = new descent.core.dom.Modifier(ast);
 				if (modification.tok == TOK.TOKconst) {
 					// const.length() == 5
 					modifier.setModifierKeyword(ModifierKeyword.CONST_KEYWORD);
 					modifier.setSourceRange(modification.startPosition, 5);
 				} else {
 					// invariant.length() == 9
 					modifier.setModifierKeyword(ModifierKeyword.INVARIANT_KEYWORD);
 					modifier.setSourceRange(modification.startPosition, 9);
 				}
 				c.setModifier(modifier);
 				
 				c.setComponentType(b);
 				c.setSourceRange(modification.startPosition, modification.length);
 				b = c;
 			}
 		}
 		
 		return b;
 	}
 
 	public descent.core.dom.Modifier convert(descent.internal.compiler.parser.Modifier a) {
 		descent.core.dom.Modifier b = new descent.core.dom.Modifier(ast);
 		switch(a.tok) {
 		case TOKprivate: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.PRIVATE_KEYWORD); break;
 		case TOKpackage: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.PACKAGE_KEYWORD); break;
 		case TOKprotected: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.PROTECTED_KEYWORD); break;
 		case TOKpublic: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.PUBLIC_KEYWORD); break;
 		case TOKexport: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.EXPORT_KEYWORD); break;
 		case TOKstatic: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.STATIC_KEYWORD); break;
 		case TOKfinal: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.FINAL_KEYWORD); break;
 		case TOKabstract: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.ABSTRACT_KEYWORD); break;
 		case TOKoverride: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.OVERRIDE_KEYWORD); break;
 		case TOKauto: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.AUTO_KEYWORD); break;
 		case TOKsynchronized: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD); break;
 		case TOKdeprecated: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.DEPRECATED_KEYWORD); break;
 		case TOKextern: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.EXTERN_KEYWORD); break;
 		case TOKconst: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.CONST_KEYWORD); break;
 		case TOKscope: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.SCOPE_KEYWORD); break;
 		case TOKinvariant: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.INVARIANT_KEYWORD); break;
 		case TOKin: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.IN_KEYWORD); break;
 		case TOKout: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.OUT_KEYWORD); break;
 		case TOKinout: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.INOUT_KEYWORD); break;
 		case TOKlazy: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.LAZY_KEYWORD); break;
 		case TOKref: b.setModifierKeyword(descent.core.dom.Modifier.ModifierKeyword.REF_KEYWORD); break;
 		default:
 			throw new IllegalStateException("Invalid modifier: " + a.tok);
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Declaration convertDeclaration(Dsymbol symbol) {
 		switch(symbol.getNodeType()) {
 		case ASTDmdNode.VAR_DECLARATION: {
 			VarDeclaration a = (VarDeclaration) symbol;
 			descent.core.dom.VariableDeclaration b = new descent.core.dom.VariableDeclaration(ast);
 			if (a.sourceType != null) {
 				b.setType(convert(a.sourceType));
 			}
 			b.fragments().add(convert(a));
 			fillDeclaration(b, a);
 			return b;
 		}
 		case ASTDmdNode.ALIAS_DECLARATION: {
 			AliasDeclaration a = (AliasDeclaration) symbol;
 			descent.core.dom.AliasDeclaration b = new descent.core.dom.AliasDeclaration(ast);
 			if (a.type != null) {
 				descent.core.dom.Type convertion = convert(a.type);
 				if (convertion != null) {
 					b.setType(convertion);
 				}
 			}
 			b.fragments().add(convert(a));
 			fillDeclaration(b, a);
 			return b;
 		}
 		case ASTDmdNode.TYPEDEF_DECLARATION: {
 			TypedefDeclaration a = (TypedefDeclaration) symbol;
 			descent.core.dom.TypedefDeclaration b = new descent.core.dom.TypedefDeclaration(ast);
 			if (a.sourceBasetype != null) {
 				descent.core.dom.Type convertedType = convert(a.sourceBasetype);
 				if (convertedType != null) {
 					b.setType(convertedType);
 				}
 			}
 			b.fragments().add(convert(a));
 			fillDeclaration(b, a);
 			return b;
 		}
 		default:
 			return (Declaration) convert(symbol);
 		}
 	}
 	
 	public void convertDeclarations(List<Declaration> destination, List<Dsymbol> source) {
 		if (source == null || source.isEmpty()) return;
 		for(int i = 0; i < source.size(); i++) {
 			Dsymbol symbol = source.get(i);
 			switch(symbol.getNodeType()) {
 			case ASTDmdNode.VAR_DECLARATION: {
 				descent.core.dom.VariableDeclaration b = new descent.core.dom.VariableDeclaration(ast);
 				int start = -1;
 				int end = -1;
 				boolean first = true;
 				while(symbol.getNodeType() == ASTDmdNode.VAR_DECLARATION) {
 					VarDeclaration a = (VarDeclaration) symbol;
 					if (first) {
 						if (a.sourceType != null) {
 							b.setType(convert(a.sourceType));
 						}
 						convertModifiers(b.modifiers(), a.modifiers);
 						if (a.postDdoc != null) {
 							b.setPostDDoc(convertDdoc(a.postDdoc));
 						}
 						first = false;
 					}
 					if (start == -1) {
 						start = a.start;
 					}
 					end = a.start + a.length;
 					b.fragments().add(convert(a));
 					if (i == source.size() - 1) {
 						if (a.preDdocs != null) {
 							convertDdoc(b.preDDocs(), a.preDdocs);
 						}
 						break;
 					}
 					
 					if (a.last) {
 						break;
 					}
 					
 					i++;
 					symbol = source.get(i);
 				}
 				b.setSourceRange(start, end - start);
 				destination.add(b);
 				break;
 			}
 			case ASTDmdNode.ALIAS_DECLARATION: {
 				descent.core.dom.AliasDeclaration b = new descent.core.dom.AliasDeclaration(ast);
 				int start = -1;
 				int end = -1;
 				boolean first = true;
 				while(symbol.getNodeType() == ASTDmdNode.ALIAS_DECLARATION) {
 					AliasDeclaration a = (AliasDeclaration) symbol;
 					if (first) {
 						if (a.type != null) {
 							descent.core.dom.Type convertion = convert(a.type);
 							if (convertion != null) {
 								b.setType(convertion);
 							}
 						}
 						convertModifiers(b.modifiers(), a.modifiers);
 						if (a.postDdoc != null) {
 							b.setPostDDoc(convertDdoc(a.postDdoc));
 						}
 						first = false;
 					}
 					if (start == -1) {
 						start = a.start;
 					}
 					end = a.start + a.length;
 					b.fragments().add(convert(a));
 					if (i == source.size() - 1) {
 						if (a.preDdocs != null) {
 							convertDdoc(b.preDDocs(), a.preDdocs);
 						}
 						break;
 					}
 					
 					if (a.last) {
 						break;
 					}
 					
 					i++;
 					symbol = source.get(i);
 				}
 				b.setSourceRange(start, end - start);
 				destination.add(b);
 				break;
 			}
 			case ASTDmdNode.TYPEDEF_DECLARATION: {
 				descent.core.dom.TypedefDeclaration b = new descent.core.dom.TypedefDeclaration(ast);
 				int start = -1;
 				int end = -1;
 				boolean first = true;
 				while(symbol.getNodeType() == ASTDmdNode.TYPEDEF_DECLARATION) {
 					TypedefDeclaration a = (TypedefDeclaration) symbol;
 					if (first) {
 						if (a.sourceBasetype != null) {
 							descent.core.dom.Type convertedType = convert(a.sourceBasetype);
 							if (convertedType != null) {
 								b.setType(convertedType);
 							}
 						}
 						convertModifiers(b.modifiers(), a.modifiers);
 						if (a.postDdoc != null) {
 							b.setPostDDoc(convertDdoc(a.postDdoc));
 						}
 						first = false;
 					}
 					if (start == -1) {
 						start = a.start;
 					}
 					end = a.start + a.length;
 					b.fragments().add(convert(a));
 					if (i == source.size() - 1) {
 						if (a.preDdocs != null) {
 							convertDdoc(b.preDDocs(), a.preDdocs);
 						}
 						break;
 					}
 					
 					if (a.last) {
 						break;
 					}
 					
 					i++;
 					symbol = source.get(i);
 				}
 				b.setSourceRange(start, end - start);
 				destination.add(b);
 				break;
 			}
 			case ASTDmdNode.PROT_DECLARATION: {
 				convert((ProtDeclaration) symbol, destination);
 				break;
 			}
 			case ASTDmdNode.STORAGE_CLASS_DECLARATION: {
 				convert((StorageClassDeclaration) symbol, destination);
 				break;
 			}
 			default:
 				destination.add((Declaration) convert(symbol));
 			}
 		}
 	}
 	
 	public void convertExpressions(List<descent.core.dom.Expression> destination, List<Expression> source) {
 		if (source == null || source.isEmpty()) return;
 		for(Expression exp : source) destination.add(convert(exp));
 	}
 	
 	public void convertStatements(List<descent.core.dom.Statement> destination, List<Statement> source) {
 		if (source == null || source.isEmpty()) return;
 		for(Statement stm : source) {
 			if (stm.synthetic) {
 				continue;
 			}
 			destination.add(convert(stm));
 		}
 	}
 	
 	public void convertBaseClasses(List<descent.core.dom.BaseClass> destination, List<BaseClass> source) {
 		if (source == null || source.isEmpty()) return;
 		for(BaseClass base : source) destination.add(convert(base));
 	}
 	
 	public void convertTemplateParameters(List<descent.core.dom.TemplateParameter> destination, List<TemplateParameter> source) {
 		if (source == null || source.isEmpty()) return;
 		for(TemplateParameter temp : source) destination.add(convert(temp));
 	}
 	
 	public void convertArguments(List<descent.core.dom.Argument> destination, List<Argument> source) {
 		if (source == null || source.isEmpty()) return;
 		for(Argument arg : source) destination.add(convert(arg));
 	}
 	
 	public void convertModifiers(List<descent.core.dom.Modifier> destination, List<Modifier> source) {
 		if (source == null || source.isEmpty()) return;
 		for(Modifier m : source) destination.add(convert(m));
 	}
 	
 	public descent.core.dom.Expression convert(BinExp a, Assignment.Operator op) {
 		Assignment b = new Assignment(ast);
 		b.setLeftHandSide(convert(a.e1));
 		b.setOperator(op);
 		b.setRightHandSide(convert(a.e2));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(BinExp a, InfixExpression.Operator op) {
 		InfixExpression b = new InfixExpression(ast);
 		b.setLeftOperand(convert(a.e1));
 		b.setOperator(op);
 		b.setRightOperand(convert(a.e2));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(UnaExp a, PrefixExpression.Operator op) {
 		PrefixExpression b = new PrefixExpression(ast);
 		b.setOperand(convert(a.e1));
 		b.setOperator(op);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Expression convert(UnaExp a, PostfixExpression.Operator op) {
 		PostfixExpression b = new PostfixExpression(ast);
 		b.setOperand(convert(a.e1));
 		b.setOperator(op);
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.SimpleName convert(IdentifierExp a) {
 		if (CharOperation.equals(a.ident, Id.empty)) return null;
 		
 		descent.core.dom.SimpleName b = new descent.core.dom.SimpleName(ast);
 		b.internalSetIdentifier(new String(a.ident));
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 	
 	public descent.core.dom.Type convertTemplateMixin(int start, int length, Type typeof, List<IdentifierExp> ids, List<ASTDmdNode> tiargs) {
 		descent.core.dom.Type ret = null;
 		if (typeof != null) {
 			ret = convert(typeof);
 		}
 		
 		for(int i = 0; i < ids.size(); i++) {
 			IdentifierExp id = ids.get(i);
 			if (id == null || (id.ident != null && CharOperation.equals(id.ident, Id.empty))) continue;
 			
 			descent.core.dom.Type second;
 			if (i == ids.size() - 1) {
 				if (tiargs == null || tiargs.isEmpty()) {
 					descent.core.dom.SimpleName firstName = convert(id);
 					second = ast.newSimpleType(firstName);
 					second.setSourceRange(firstName.getStartPosition(), firstName.getLength());
 				} else {
 					TemplateType type = new TemplateType(ast);
 					type.setName(convert(id));
 					for(ASTDmdNode node : tiargs) {
 						type.arguments().add(convert(node));
 					}
 					second = type;
 					second.setSourceRange(id.start, start + length - id.start);
 				}
 			} else {
 				if (id instanceof TemplateInstanceWrapper) {
 					second = convert((TemplateInstanceWrapper) id);
 				} else {
 					descent.core.dom.SimpleName name = convert(id);
 					second = ast.newSimpleType(name);
 					second.setSourceRange(name.getStartPosition(), name.getLength());
 				}
 			}
 			if (ret == null) {
 				if (i == 1) {
 					ret = ast.newQualifiedType(null, second);
 				} else {
 					ret = second;
 				}
 			} else {
 				ret = ast.newQualifiedType(ret, second);
 			}
 			ret.setSourceRange(start, second.getStartPosition() + second.getLength() - start);
 		}
 		return ret;
 	}
 	
 	public descent.core.dom.Name convert(List<IdentifierExp> packages, IdentifierExp id) {
 		descent.core.dom.SimpleName sIdLast = convert(id);
 		if (packages == null || packages.isEmpty()) {
 			return sIdLast;
 		} else {
 			IdentifierExp firstId = packages.get(0);
 			descent.core.dom.Name name = convert(firstId);
 			for(int i = 1; i < packages.size(); i++) {
 				IdentifierExp sId = packages.get(i);
 				descent.core.dom.SimpleName sName = convert(sId);
 				descent.core.dom.QualifiedName qName = ast.newQualifiedName(name, sName);
 				qName.setSourceRange(firstId.start, sId.start + sId.length - firstId.start);
 				name = qName;
 			}
 			name = ast.newQualifiedName(name, sIdLast);
 			name.setSourceRange(firstId.start, id.start + id.length - firstId.start);
 			return name;
 		}
 	}
 	
 	public descent.core.dom.Name convert(IdentifierExp id, List<IdentifierExp> packages) {
 		descent.core.dom.SimpleName sIdFirst = convert(id);
 		if (packages == null || packages.isEmpty()) {
 			return sIdFirst;
 		} else {
 			Name name = sIdFirst;
 			for(int i = 0; i < packages.size(); i++) {
 				IdentifierExp sId = packages.get(i);
 				descent.core.dom.SimpleName sName = convert(sId);
 				descent.core.dom.QualifiedName qName = ast.newQualifiedName(name, sName);
 				qName.setSourceRange(id.start, sId.start + sId.length - id.start);
 				name = qName;
 			}
 			return name;
 		}
 	}
 	
 	public descent.core.dom.Expression convert(descent.internal.compiler.parser.Expression exp) {
 		return (descent.core.dom.Expression) convert((ASTDmdNode) exp);
 	}
 	
 	public descent.core.dom.Initializer convert(descent.internal.compiler.parser.Initializer init) {
 		return (descent.core.dom.Initializer) convert((ASTDmdNode) init);
 	}
 	
 	public descent.core.dom.Statement convert(descent.internal.compiler.parser.Statement stm) {
 		return (descent.core.dom.Statement) convert((ASTDmdNode) stm);
 	}
 	
 	public descent.core.dom.Type convert(descent.internal.compiler.parser.Type type) {
 		return (descent.core.dom.Type) convert((ASTDmdNode) type);
 	}
 	
 	public descent.core.dom.TemplateParameter convert(descent.internal.compiler.parser.TemplateParameter type) {
 		return (descent.core.dom.TemplateParameter) convert((ASTDmdNode) type);
 	}
 	
 	public void convertDdoc(List<descent.core.dom.DDocComment> to,  List<descent.internal.compiler.parser.Comment> from) {
 		for(descent.internal.compiler.parser.Comment a : from) {
 			to.add(convertDdoc(a));
 		}
 	}
 	
 	public descent.core.dom.DDocComment convertDdoc(descent.internal.compiler.parser.Comment a) {
 		return (DDocComment) moduleComments[a.index];
 	}
 	
 	public descent.core.dom.Comment[] convertComments(descent.internal.compiler.parser.Comment[] from) {
 		descent.core.dom.Comment[] comments = new descent.core.dom.Comment[from.length];
 		for(int i = 0; i < from.length; i++) {
 			from[i].index = i;
 			comments[i] = convertComment(from[i]);
 		}
 		return comments;
 	}
 	
 	public descent.core.dom.Comment convertComment(descent.internal.compiler.parser.Comment a) {
 		Comment b = null;
 		switch(a.kind) {
 		case descent.internal.compiler.parser.Comment.LINE_COMMENT:
 			b = ast.newCodeComment();
 			b.setKind(Comment.Kind.LINE_COMMENT); 
 			break;
 		case descent.internal.compiler.parser.Comment.PLUS_COMMENT:
 			b = ast.newCodeComment();
 			b.setKind(Comment.Kind.PLUS_COMMENT); 
 			break;
 		case descent.internal.compiler.parser.Comment.BLOCK_COMMENT:
 			b = ast.newCodeComment();
 			b.setKind(Comment.Kind.BLOCK_COMMENT); 
 			break;
 		case descent.internal.compiler.parser.Comment.DOC_LINE_COMMENT:
 			b = ast.newDDocComment(new String(a.string));
 			b.setKind(Comment.Kind.LINE_COMMENT); 
 			break;
 		case descent.internal.compiler.parser.Comment.DOC_PLUS_COMMENT:
 			b = ast.newDDocComment(new String(a.string));
 			b.setKind(Comment.Kind.PLUS_COMMENT); 
 			break;
 		case descent.internal.compiler.parser.Comment.DOC_BLOCK_COMMENT:
 			b = ast.newDDocComment(new String(a.string));
 			b.setKind(Comment.Kind.BLOCK_COMMENT); 
 			break;
 		}
 		b.setSourceRange(a.start, a.length);
 		return b;
 	}
 
 }
 
