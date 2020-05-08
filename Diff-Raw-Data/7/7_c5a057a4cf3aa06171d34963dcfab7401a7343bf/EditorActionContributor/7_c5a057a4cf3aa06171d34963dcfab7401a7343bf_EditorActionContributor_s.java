 package org.meta_environment.eclipse.terms;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.imp.editor.UniversalEditor;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.services.base.DefaultLanguageActionsContributor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ControlContribution;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 public class EditorActionContributor extends DefaultLanguageActionsContributor {
 	public static final String TERM_LANGUAGE_SELECTOR = "term-language-selector";
 
 	public void contributeToEditorMenu(UniversalEditor editor, IMenuManager menuManager) {
 		String file = getFilename(editor);
 		String language = TermEditorTools.getInstance().getLanguage(file);
 		List<Action> actions = TermEditorTools.getInstance().getDynamicActions(language, file);
 		
 		for (Action action : actions) {
 			menuManager.add(action);
 		}
 	}
 	
 	public void contributeToStatusLine(final UniversalEditor editor, IStatusLineManager statusLineManager) {
 		contributeLanguageSelector(statusLineManager, editor);
 	}
 
 	private void contributeLanguageSelector(IStatusLineManager statusLineManager, final UniversalEditor ed) {
 		statusLineManager.add(new ControlContribution(TERM_LANGUAGE_SELECTOR) {
 				protected Control createControl(Composite parent) {
 					List<String> languages = TermEditorTools.getInstance().getLanguages();
 					final Combo combo = new Combo(parent, SWT.NONE);
 					
 					combo.setToolTipText("Select an SDF module to parse this term with");
 					for (String language : languages) {
 						combo.add(language);
 					}
 					
 					final String filename = getFilename(ed);
 					final String current = TermEditorTools.getInstance().getLanguage(filename);
					if (languages.contains(current)) {
						combo.select(combo.indexOf(current));
 					}
					
 					combo.addSelectionListener(new SelectionListener() {
 
 						public void widgetDefaultSelected(SelectionEvent e) {
 							widgetSelected(e);
 						}
 
 						public void widgetSelected(SelectionEvent e) {
 							if (combo.getText() != null && combo.getText().length() != 0) {
 							  TermEditorTools.getInstance().setLanguage(filename, combo.getText());
 							}
 						}
 
 						
 					});
 					return combo;
 				}
 			});
 	}
 	
 	private String getFilename(final UniversalEditor ed) {
 		IParseController controller = ed.getParseController();
 		ISourceProject project = controller.getProject();
 		IPath path = controller.getPath();
 		String filename;
 		
 		if (project != null) {
 		  filename = project.getRawProject().getLocation().append(path).makeAbsolute().toOSString();
 		}
 		else {
 		  filename = ed.getParseController().getPath().toOSString();
 		}
 		return filename;
 	}
 }
