 /*
  * (C) Copyright IBM Corporation 2007
  * 
  * This file is part of the Eclipse IMP.
  */
 package org.eclipse.imp.xform.search;
 
import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.search.ui.ISearchPage;
 import org.eclipse.search.ui.ISearchPageContainer;
 import org.eclipse.search.ui.NewSearchUI;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 public class ASTSearchPage extends DialogPage implements ISearchPage {
     private ISearchPageContainer fContainer;
 
     private Text fPattern;
 
     private Combo fLangCombo;
 
     public ASTSearchPage() {
 	this("AST Search");
     }
 
     public ASTSearchPage(String title) {
 	this(title, null);
     }
 
     public ASTSearchPage(String title, ImageDescriptor image) {
 	super(title, image);
     }
 
     public boolean performAction() {
         IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject("JikesPGTest");
         boolean isWorkspaceScope= fContainer.getSelectedScope() == ISearchPageContainer.WORKSPACE_SCOPE;
         ASTSearchScope scope= isWorkspaceScope ? ASTSearchScope.createWorkspaceScope() :
             ASTSearchScope.createProjectScope(project);
         ASTSearchQuery query= new ASTSearchQuery(fPattern.getText(), fLangCombo.getText(), scope);
 
         NewSearchUI.activateSearchResultView();
         NewSearchUI.runQueryInBackground(query);
         return true;
     }
 
     public void setContainer(ISearchPageContainer container) {
 	fContainer= container;
     }
 
     public void createControl(Composite parent) {
         Composite result= new Composite(parent, SWT.NONE);
 
         GridLayout layout= new GridLayout(2, false);
         layout.horizontalSpacing= 10;
         result.setLayout(layout);
 
         Label langLabel= new Label(result, SWT.LEFT);
         langLabel.setText("Language:"); 
         langLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 1, 1));
 
         fLangCombo= new Combo(result, SWT.DROP_DOWN | SWT.READ_ONLY);
	    Collection<Language> langs= LanguageRegistry.getLanguages();
 
         for(Iterator iter= langs.iterator(); iter.hasNext();) {
 	    Language lang= (Language) iter.next();
 
 	    fLangCombo.add(lang.getName());
 	}
         fLangCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 1, 1));
         fLangCombo.select(0);
 
         Label patLabel= new Label(result, SWT.LEFT);
         patLabel.setText("AST pattern:"); 
         patLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 1, 1));
 
         fPattern= new Text(result, SWT.LEFT | SWT.BORDER);
         fPattern.setText("");
 
         setControl(result);
     }
 }
