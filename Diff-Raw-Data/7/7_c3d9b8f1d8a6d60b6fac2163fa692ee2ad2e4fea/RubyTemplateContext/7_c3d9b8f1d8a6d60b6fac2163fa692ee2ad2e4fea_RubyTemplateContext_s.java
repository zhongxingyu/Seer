 package org.eclipse.dltk.ruby.internal.ui.templates;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.ruby.internal.parser.RubySourceElementParser;
 import org.eclipse.dltk.ui.templates.ScriptTemplateContext;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.TemplateBuffer;
 import org.eclipse.jface.text.templates.TemplateContextType;
 import org.eclipse.jface.text.templates.TemplateException;
 
 public class RubyTemplateContext extends ScriptTemplateContext {
 
 	public RubyTemplateContext(TemplateContextType type, IDocument document,
 			int completionOffset, int completionLength,
 			ISourceModule sourceModule) {
 		super(type, document, completionOffset, completionLength, sourceModule);
 	}
 	
 	private static class FormattingAstVisitor extends ASTVisitor
 	{	
 		private int indentLevel;
 		
 		private String text;
 		
 		private void printContent(ASTNode node)
 		{			
 			int start = node.sourceStart();
 			int end = node.sourceEnd();
 			
 			System.out.println("Begin index: " + start);
 			System.out.println("End index: " + end);
 			System.out.println("Real node type: " + node.getClass());
 			
 			if (start >= 0 && start < text.length() && end >= 0 && end < text.length()) {			
 				System.out.println("=== Text ===");
 				System.out.println(text.substring(start, end));
 			}
 		}
 		
 		public FormattingAstVisitor(String text){
 			this.text = text;
 			indentLevel = 0;
 		}
 		
 		public boolean visit(Expression s) throws Exception {
 			//System.out.println("FormattingAstVisitor.visit(Expression s)");
 			//indentLevel++;
 			
 			printContent(s);
 			
 			return true;
 		}
 
 		public boolean visit(MethodDeclaration s) throws Exception {
 			//System.out.println("FormattingAstVisitor.visit(MethodDeclaration s)");
 			//indentLevel++;
 			
 			printContent(s);
 			
 			return true;
 		}
 
 		public boolean visit(ModuleDeclaration s) throws Exception {
 			//System.out.println("FormattingAstVisitor.visit(ModuleDeclaration s)");
 			//indentLevel++;
 			
 			printContent(s);
 			
 			return true;
 		}
 
 		public boolean visit(Statement s) throws Exception {
 			System.out.println("FormattingAstVisitor.visit(Statement s)");
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public boolean visit(TypeDeclaration s) throws Exception {
 			//int start = s.sourceStart();
 			//int end = s.sourceEnd();			
 			//text.substring(start, end);			
 			//System.out.println("FormattingAstVisitor.visit(TypeDeclaration s)");
 			
 			printContent(s);
 			
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public boolean visitGeneral(ASTNode node) throws Exception {
 			return true;
 		}
 	
 		public boolean endvisit(Expression s) throws Exception {
 			//--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(MethodDeclaration s) throws Exception {
 			//--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(ModuleDeclaration s) throws Exception {
 			//--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(Statement s) throws Exception {
 			//--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(TypeDeclaration s) throws Exception {
 			//--indentLevel;
 			return true;
 		}		
 	}
 
 	
 	// Just for testing
 	public TemplateBuffer test_evaluate(Template template)
 			throws BadLocationException, TemplateException {
 		if (!canEvaluate(template)) {
 			return null;
 		}
 
 		String indentTo = calculateIndent(getDocument(), getStart());
 		
 		System.out.println("Indent: |" + indentTo + "|");
 
 		String delimeter = TextUtilities.getDefaultLineDelimiter(getDocument());
 		String[] lines = template.getPattern().split("\n");
 		
 		String p = "for ${i} in ${start_num}..${end_num}\n${cursor}\nend";
 		//OldCodeFormatter formater = new OldCodeFormatter(Collections.EMPTY_MAP);
 		//String formatted = formater.formatString(p, 0);		
 		//System.out.println("Unformatted: |" + p + "|");
 		//System.out.println("Formatted: |" + formatted + "|");		
 		ModuleDeclaration decl = RubySourceElementParser.parseModule(null, p.toCharArray(), new IProblemReporter(){
 
			public void reportProblem(IProblem problem) throws CoreException {
				System.out.println("Problem: " + problem.toString());				
 			}
 		});
 		
 		try {
 			decl.traverse(new FormattingAstVisitor(p));
 		} catch (Exception e) {
 				e.printStackTrace();
 		}
 		
 		if (lines.length > 1 && indentTo != null && indentTo.length() > 0) {
 			StringBuffer buffer = new StringBuffer(lines[0]);
 
 			// Except first line
 			for (int i = 1; i < lines.length; i++) {
 				buffer.append(delimeter);
 				buffer.append(indentTo);
 				buffer.append(lines[i]);
 			}
 
 			template = new Template(template.getName(), template
 					.getDescription(), template.getContextTypeId(), buffer
 					.toString(), template.isAutoInsertable());
 		}
 
 		return super.evaluate(template);
 	}
 }
