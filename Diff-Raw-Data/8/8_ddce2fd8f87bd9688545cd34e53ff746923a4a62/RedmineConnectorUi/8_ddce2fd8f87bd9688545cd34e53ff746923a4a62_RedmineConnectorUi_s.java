 /*******************************************************************************
  * Copyright (c) 2004, 2007 Mylyn project committers and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mylyn project committers
  *******************************************************************************/
 /*******************************************************************************
  * Copyright (c) 2008 Sven Krzyzak
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Sven Krzyzak - adapted Trac implementation for Redmine
  *******************************************************************************/
 package org.svenk.redmine.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
 import org.eclipse.mylyn.tasks.core.ITask;
 import org.eclipse.mylyn.tasks.core.ITaskMapping;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
 import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
 import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
 import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
 import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
 import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.svenk.redmine.core.RedmineCorePlugin;
 import org.svenk.redmine.ui.wizard.NewRedmineTaskWizard;
 import org.svenk.redmine.ui.wizard.RedmineQueryPage;
 import org.svenk.redmine.ui.wizard.RedmineRepositorySettingsPage;
 
 
 public class RedmineConnectorUi extends AbstractRepositoryConnectorUi {
 
 	private static Pattern revisionPattern = Pattern.compile("r(\\d+)");
 	private static Pattern issuePattern = Pattern.compile("#(\\d+)");
 	
 	public RedmineConnectorUi() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public String getConnectorKind() {
 		return RedmineCorePlugin.REPOSITORY_KIND;
 	}
 
 	@Override
 	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
 		return new RedmineRepositorySettingsPage(taskRepository);
 	}
 
 	@Override
 	public boolean hasSearchPage() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
 		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
 		wizard.addPage(new RedmineQueryPage(repository, query));
 		return wizard;
 	}
 	
 //	@Override
 //	public IWizard getQueryWizard(TaskRepository repository, AbstractRepositoryQuery query) {
 //		return new RedmineQueryWizard(repository, (RedmineRepositoryQuery)query);
 //	}
 
 	@Override
 	public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping selection) {
 		return new NewRedmineTaskWizard(taskRepository, selection);
 	}
 	
 	public IHyperlink[] findHyperlinks(TaskRepository repository, String text, int textOffset, int lineOffset) {
 		List<IHyperlink> links = new ArrayList<IHyperlink>();
 		Matcher m = null;
 
 		IWorkbench workbench= PlatformUI.getWorkbench();
 		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
 		IEditorPart part =  page.getActiveEditor();
 		
 		if (part instanceof TaskEditor) {
 			TaskEditor taskEditor = (TaskEditor)part;
 			TaskEditorInput taskEditorInput = taskEditor.getTaskEditorInput();
 			ITask task = taskEditorInput.getTask();
 			m = revisionPattern.matcher(text);
 			while(m.find()) {
 				if (m.start()<=textOffset && textOffset<=m.end()) {
 					try {
 						links.add(new RevisionHyperlink(buildRegion(lineOffset, m.start(), m.end()), repository, task, Integer.parseInt(m.group(1))));
 					} catch (NumberFormatException e) {
 						//nothing to do
 					}
 					
 				}
 			}
 		}
 
 		m = issuePattern.matcher(text);
 		while(m.find()) {
 			if (m.start()<=textOffset && textOffset<=m.end()) {
 				links.add(new TaskHyperlink(buildRegion(lineOffset, m.start(), m.end()), repository, m.group(1)));
 			}
 		}
 		
 		return links.toArray(new IHyperlink[links.size()]);
 	};
 	
 	protected IRegion buildRegion(int lineOffset, int matcherStart, int matcherEnd) {
 		return new Region(lineOffset + matcherStart, matcherEnd-matcherStart);
 	}
 }
