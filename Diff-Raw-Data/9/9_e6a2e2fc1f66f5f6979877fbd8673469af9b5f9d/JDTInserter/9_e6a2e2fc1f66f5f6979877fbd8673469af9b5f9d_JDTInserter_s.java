 package superintents.util;
 
 import java.util.ArrayList;
 
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.jface.text.*;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 
 import intentmodel.impl.*;
 
 public class JDTInserter {
 	public static void insertIntent(SuperIntentImpl intentImplementaion) {
 		ArrayList<ASTNodeWrapper> nodeWrappers = Java2AST.transformSuperIntent(intentImplementaion);
 		try {
 			insertNodes(nodeWrappers);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
	private static int insertionOffset(Block block, int nodeStatementOffset, int caretOffset) {
 		int statementOffset = block.getStartPosition();
 		// Get the offset where to insert the current node
 		for (Object o : block.statements()) {
 			statementOffset += o.toString().length();
 			nodeStatementOffset += (statementOffset < caretOffset) ? 1 : 0;
 		}
 		return nodeStatementOffset;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void addMethodAroundIntent(ASTTupleHelper helper, ArrayList<ASTNodeWrapper> nodeWrappers) throws JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
 		AST ast = helper.ast;
 		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
 		methodDeclaration.setName(ast.newSimpleName("superIntentMethodPlaceholderCaller"));
 		Modifier modifier = ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD);
 		methodDeclaration.modifiers().add(modifier);
 		methodDeclaration.setBody(ast.newBlock());
 		
 		TypeDeclaration typeDeclaration = (TypeDeclaration) helper.compilationUnit.types().get(0);
 		ListRewrite listRewrite = helper.rewriter.getListRewrite(typeDeclaration, typeDeclaration.getBodyDeclarationsProperty());
 		listRewrite.insertFirst(methodDeclaration, null);
 		TextEdit edits = helper.rewriter.rewriteAST();
 		Document document = new Document(helper.unit.getSource());
 		edits.apply(document);
 		helper.unit.getBuffer().setContents(document.get());
 
 		// Jump to the position right after the inserted node and focus the editor
 		helper.editor.selectAndReveal(edits.getExclusiveEnd()-5, 0);
 		helper.editor.setFocus();
 		
 		insertNodes(nodeWrappers);
 	}
 	
 	private static void insertNodes(ArrayList<ASTNodeWrapper> nodeWrappers) throws JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
 		// Initialize values
 		ASTTupleHelper helper = JDTHelper.getASTTupleHelper();
 		int caretOffset = JDTHelper.getCaretOffset(helper.editor);
 		Block block = getDeepestBlock(helper.compilationUnit, caretOffset);
 		if (block == null) {
 			addMethodAroundIntent(helper, nodeWrappers);
 			return;
 		}
		int nodeStatementOffset = 0;
 		ListRewrite listRewrite = null;
 		for (ASTNodeWrapper nodeWrapper : nodeWrappers) {
 			switch (nodeWrapper.type) {
 			case NORMAL_CODE:
				nodeStatementOffset = insertionOffset(block, nodeStatementOffset, caretOffset);
 				listRewrite = helper.rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
 				listRewrite.insertAt(nodeWrapper.astNode, nodeStatementOffset, null);
 				nodeStatementOffset++;
 				break;
 			case COMMENT:
 				ASTNode commentNode = helper.rewriter.createStringPlaceholder(nodeWrapper.comment, ASTNode.EMPTY_STATEMENT);
 				listRewrite = helper.rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
 				listRewrite.insertAt(commentNode, nodeStatementOffset, null);
 				nodeStatementOffset++;
 				break;
 			case CALLBACK_METHOD:
 				if (nodeWrapper.existingCallbackMethod != null) {
 					block = Java2AST.doesMethodExist(helper.compilationUnit, "OnActivityResult").getBody();
 					listRewrite = helper.rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
 					listRewrite.insertFirst(nodeWrapper.astNode, null);
 					break;
 				} else {
 					TypeDeclaration typeDeclaration = (TypeDeclaration) helper.compilationUnit.types().get(0);
 					listRewrite = helper.rewriter.getListRewrite(typeDeclaration, typeDeclaration.getBodyDeclarationsProperty());
 					listRewrite.insertAfter(nodeWrapper.astNode, helper.currentMethod, null);
 					break;
 				}
 			case FIELD:
 				TypeDeclaration fieldDeclaration = (TypeDeclaration) helper.compilationUnit.types().get(0);
 				listRewrite = helper.rewriter.getListRewrite(fieldDeclaration, fieldDeclaration.getBodyDeclarationsProperty());
 				listRewrite.insertFirst(nodeWrapper.astNode, null);
 				break;
 			case IMPORT:
 				listRewrite = helper.rewriter.getListRewrite(helper.compilationUnit, CompilationUnit.IMPORTS_PROPERTY);
 				listRewrite.insertFirst(nodeWrapper.astNode, null);
 				break;
 			default:
 				break;
 			}
 		}
 
 		TextEdit edits = helper.rewriter.rewriteAST();
 		Document document = new Document(helper.unit.getSource());
 		edits.apply(document);
 		helper.unit.getBuffer().setContents(document.get());
 
 		// Jump to the position right after the inserted node and focus the editor
 		helper.editor.selectAndReveal(edits.getExclusiveEnd(), 0);
 		helper.editor.setFocus();
 	}
 
 	public static Block getDeepestBlock(CompilationUnit cu, int caretOffset) {
 		BlockASTVisitor astv = new BlockASTVisitor(caretOffset);
 		cu.accept(astv);
 		return astv.getBlock();
 	}
 }
