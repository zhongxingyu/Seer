 /*******************************************************************************
  *  Copyright (c) 2011 University Of Moratuwa
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Isuru Udana - UI Integration in the Workbench
  *******************************************************************************/
 package org.eclipse.ecf.salvo.ui.internal.dialogs;
 
 import java.io.ByteArrayInputStream;
 
 import org.apache.james.mime4j.parser.MimeStreamParser;
 import org.eclipse.ecf.protocol.nntp.core.Debug;
 import org.eclipse.ecf.protocol.nntp.core.ServerStoreFactory;
 import org.eclipse.ecf.protocol.nntp.model.IArticle;
 import org.eclipse.ecf.protocol.nntp.model.INewsgroup;
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ecf.salvo.ui.internal.MimeArticleContentHandler;
 import org.eclipse.ecf.salvo.ui.internal.editor.ArticleWidgetBuilder;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class ThisUserArticlesComposite extends Composite {
 	private Composite articleBodyComposite;
 	private IArticle[] articles;
 	private Table articleTable;
 	private TableColumn subjectColumn;
 	private TableColumn DateColumn;
 
 	public static void showArticles(Shell parentShell, INewsgroup newsgroup) {
 
 		Shell shell = new Shell(parentShell, SWT.SHELL_TRIM);
 		shell.setText("Articles by "
 				+ newsgroup.getServer().getServerConnection().getUser()
 				+ " in " + newsgroup.getNewsgroupName());
 
 		new ThisUserArticlesComposite(shell, newsgroup, SWT.NULL);
 
 		if (shell.getChildren().length != 0) { // check whether the composite is
 												// already disposed due to
 												// absence of articles
 			shell.setLayout(new FillLayout());
 			shell.open();
 		}
 
 	}
 
 	public ThisUserArticlesComposite(Composite parent, INewsgroup newsgroup,
 			int style) {
 
 		super(parent, style);
 		initUIComponents();
 		initArticleList(newsgroup);
 
 	}
 
 	private void initUIComponents() {
 		try {
 
 			this.setLayout(new GridLayout());
 
 			{
 				articleTable = new Table(this, SWT.BORDER);
 				articleTable.setHeaderVisible(true);
 				articleTable.setLinesVisible(true);
 				articleTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 						true, true));
 
 				subjectColumn = new TableColumn(articleTable, SWT.NONE);
 				subjectColumn.setText("Subject");
 				subjectColumn.setWidth(300);
 
 				DateColumn = new TableColumn(articleTable, SWT.NONE);
 				DateColumn.setText("Date");
 				DateColumn.setWidth(50);
 
 				articleTable.addSelectionListener(new SelectionListener() {
 
 					public void widgetSelected(SelectionEvent arg0) {
 						showArticleBody();
 					}
 
 					public void widgetDefaultSelected(SelectionEvent arg0) {
 					}
 
 				});
 
 			}
 
 			{
 				articleBodyComposite = new Composite(this, SWT.BORDER);
 				articleBodyComposite.setLayout(new GridLayout());
 				articleBodyComposite.setLayoutData(new GridData(SWT.FILL,
 						SWT.FILL, true, true));
 			}
 
 			this.layout();
 
 		} catch (Exception e) {
 			Debug.log(getClass(), e);
 		}
 	}
 
 	private void initArticleList(INewsgroup newsgroup) {
 		articles = ServerStoreFactory.instance().getServerStoreFacade()
 				.getThisUserArticles(newsgroup);
 
 		if (articles.length == 0) {
 			MessageDialog.openError(new Shell(), "Salvo Newsreader",
 					"You haven't posted any article in the selected Newsgroup");
 			this.dispose();
 		}
 
 		for (IArticle article : articles) {
 
 			TableItem tableItem = new TableItem(articleTable, SWT.NONE);
			tableItem.setText(0, article.getSubject());
			tableItem.setText(1, article.getHeaderAttributeValue("Date:"));
 
 		}
 	}
 
 	private void showArticleBody() {
 		IArticle article = articles[articleTable.getSelectionIndex()];
 		StringBuffer buffer = new StringBuffer();
 
 		try {
 
 			String[] body = (String[]) ServerStoreFactory.instance()
 					.getServerStoreFacade().getArticleBody(article);
 
 			for (String line : body) {
 				buffer.append(line + SALVO.CRLF);
 			}
 
 			MimeArticleContentHandler handler = new MimeArticleContentHandler(
 					article);
 			MimeStreamParser parser = new MimeStreamParser();
 			parser.setContentHandler(handler);
 			parser.parse(new ByteArrayInputStream(buffer.toString().getBytes()));
 
 			if (!articleBodyComposite.isDisposed()) {
 				for (Control child : articleBodyComposite.getChildren()) {
 					child.dispose();
 				}
 			}
 
 			ArticleWidgetBuilder.build(articleBodyComposite, article, handler);
 			articleBodyComposite.layout(true);
 
 		} catch (Exception e) {
 			Debug.log(getClass(), e);
 		}
 
 	}
 
 }
