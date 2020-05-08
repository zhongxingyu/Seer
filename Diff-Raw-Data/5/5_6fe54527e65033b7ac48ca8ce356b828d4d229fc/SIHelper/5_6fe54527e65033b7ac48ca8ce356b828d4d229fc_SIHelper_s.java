 package superintents.control;
 
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.ui.*;
 import org.eclipse.ui.texteditor.*;
 import org.eclipse.jface.text.*;
 import org.eclipse.jdt.core.*;
 import org.eclipse.jdt.core.dom.*;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
 import org.eclipse.jdt.ui.JavaUI;
 import superintents.impl.SuperIntentImpl;
 import transformers.ASTNodeWrapper;
 import transformers.M2MJava2AST;
 
 public class SIHelper {
 	public void insertIntent(SuperIntentImpl intentImplementaion) {
		ASTNodeWrapper node = M2MJava2AST.transformSuperIntent(intentImplementaion);
 	}
 	
 	public static ITextEditor getEditor() {
 		IWorkbench wb = PlatformUI.getWorkbench();
 		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
 		IWorkbenchPage editor = window.getActivePage();
 		IEditorPart part = editor.getActiveEditor();
 
 		if (!(part instanceof AbstractTextEditor))
 			return null;
 		return (ITextEditor) part;
 	}
 
 	public static void insertNode(ASTNodeWrapper node) throws MalformedTreeException, BadLocationException, JavaModelException {
 		IEditorInput editorInput = getEditor().getEditorInput();
 		IJavaElement elem = JavaUI.getEditorInputJavaElement(editorInput);
 		if (elem instanceof ICompilationUnit) {
 			ICompilationUnit unit = (ICompilationUnit) elem;
 			CompilationUnit astRoot = parse(unit);
 			AST ast = astRoot.getAST();
 			ASTRewrite rewriter = ASTRewrite.create(ast);
 			TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
 			int offset = getCurrentMethodOffset(typeDecl.getMethods());
 			MethodDeclaration methodDecl = typeDecl.getMethods()[offset];
 			Block block = methodDecl.getBody();
 
 			ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
 			listRewrite.insertAt(node.node, node.Offset, null);
 			
 			TextEdit edits = rewriter.rewriteAST();
 			Document document = new Document(unit.getSource());
 			edits.apply(document);
 			unit.getBuffer().setContents(document.get());
 		}
 	}
 	
 	protected static CompilationUnit parse(ICompilationUnit unit) {
 		ASTParser parser = ASTParser.newParser(AST.JLS4);
 		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 		parser.setSource(unit);
 		parser.setResolveBindings(true);
 		return (CompilationUnit) parser.createAST(null);
 	}
 	
 	private static int getCaretOffset(ITextEditor editor) {
 		int offset = ((ITextSelection) editor.getSelectionProvider()
 				.getSelection()).getOffset();
 		return offset;
 	}
 
 	private static int getCurrentMethodOffset(MethodDeclaration[] methods) {
 		int result = 0;
 		int caretOffset = getCaretOffset(getEditor());
 		for (int i = 0; i < methods.length; i++) {
 			if (caretOffset >= methods[i].getStartPosition()
 					&& caretOffset <= methods[i].getLength()
 							+ methods[i].getStartPosition())
 				result = i;
 		}
 		return result;
 	}
 }
