 package org.eclipse.imp.formatting;
 
 import java.io.IOException;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.imp.box.builders.BoxException;
 import org.eclipse.imp.box.builders.BoxFactory;
 import org.eclipse.imp.editor.UniversalEditor;
 import org.eclipse.imp.formatting.spec.ExtensionPointBinder;
 import org.eclipse.imp.formatting.spec.ParseException;
 import org.eclipse.imp.formatting.spec.Parser;
 import org.eclipse.imp.formatting.spec.Specification;
 import org.eclipse.imp.formatting.spec.Transformer;
 import org.eclipse.imp.language.ILanguageService;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.model.ModelFactory;
 import org.eclipse.imp.model.ModelFactory.ModelException;
 import org.eclipse.imp.parser.IMessageHandler;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.services.ISourceFormatter;
 import org.eclipse.imp.xform.pattern.matching.IASTAdapter;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 
 public class SourceFormatter implements ISourceFormatter, ILanguageService {
 	private IASTAdapter adapter;
 
 	private AbstractTextEditor fActiveEditor;
 
 	private Language fLanguage;
 
 	private Transformer transformer;
 	
 	private IMessageHandler handler= new IMessageHandler() {
             public void startMessageGroup(String groupName) { }
 	    public void endMessageGroup() { }
 	    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine) {
	        Activator.getInstance().writeErrorMsg(msg + "@" + startOffset + "-" + endOffset);
             }
 	};
 
 	private Parser parser;
 	
 	public void formatterStarts(String initialIndentation) {
 		initialize();
 	}
 
 	private void initialize() {
 		try {
 			UniversalEditor ue = (UniversalEditor) getActiveEditor();
 			fLanguage = LanguageRegistry.findLanguage(ue.getEditorInput());
 			ExtensionPointBinder b = new ExtensionPointBinder(fLanguage);
 			
 			adapter = b.getASTAdapter();
 			IPath fsp = b.getSpecificationPath();
 			parser = new Parser(fsp, getActiveProject(), handler);
 			Specification spec = parser.parse(fsp);
 			transformer = new Transformer(spec, adapter);
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private AbstractTextEditor getActiveEditor() {
 		fActiveEditor = (AbstractTextEditor) PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		return fActiveEditor;
 	}
 
 	private ISourceProject getActiveProject() throws ModelException {
 		return ModelFactory.open(extractResource(getActiveEditor())
 				.getProject());
 	}
 
 	private IResource extractResource(IEditorPart editor) {
 		IEditorInput input = editor.getEditorInput();
 		if (!(input instanceof IFileEditorInput))
 			return null;
 		return ((IFileEditorInput) input).getFile();
 	}
 
 	public String format(IParseController ignored, String content,
 			boolean isLineStart, String indentation, int[] positions) {
 		Object ast = parser.parseObject(content);
 
 		if (ast != null) {
 			String box = transformer.transformToBox(content, ast);
 			try {
 				return BoxFactory.box2text(box);
 			} catch (BoxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return content;
 			}
 		}
 		else {
                         MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Unable to format", "Code could not be formatted due to parse error(s)");
 //			Activator.getInstance().writeErrorMsg();
 			return content;
 		}
 	}
 
 	public void formatterStops() {
 	}
 }
