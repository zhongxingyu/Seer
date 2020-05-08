 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-08-17
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam.gui;
 
 import org.deri.iris.api.terms.ITerm;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.LinkedList;
 import eu.serscis.sam.Constants;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.storage.IRelation;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.IPredicate;
 import java.util.Arrays;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import eu.serscis.sam.Graph;
 import eu.serscis.sam.Eval;
 import eu.serscis.sam.Results;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.File;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.RowLayout;
 import static org.deri.iris.factory.Factory.*;
 
 public class GUI {
 	private File myFile;
 	private LiveResults liveResults = new LiveResults();
 	private Display display;
 	private Shell shell;
 	private Label mainImage;
 	private Color white;
 	private MenuItem relationsMenuHeader;
 	private Menu relationsMenu;
 	private MenuItem objectsMenuHeader;
 	private Menu objectsMenu;
 	private List messageList;
 	private LinkedList<ILiteral> myProblems;
 
 	public GUI(File file) throws Exception {
		if (file != null) {
			myFile = file.getAbsoluteFile();
		}
 
 		display = new Display();
 		white = new Color(display, 255, 255, 255);
		shell = new Shell(display, SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE);
 		shell.setText("SAM");
 
 		Menu menuBar = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menuBar);
 
 		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		fileMenuHeader.setText("&File");
 
 		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
 		fileMenuHeader.setMenu(fileMenu);
 
 		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
 		openItem.setText("&Open...");
 		openItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell);
 				dialog.setFilterExtensions(new String[] {"*.sam", "*"});
 				if (myFile != null) {
 					dialog.setFileName(myFile.getPath());
 				}
 				String path = dialog.open();
 				if (path != null) {
 					myFile = new File(path);
 					evaluate();
 				}
 			}
 		});
 
 		MenuItem examplesItem = new MenuItem(fileMenu, SWT.CASCADE);
 		examplesItem.setText("Open example");
 		examplesItem.setMenu(makeExamplesMenu(openItem));
 
 		MenuItem reloadItem = new MenuItem(fileMenu, SWT.PUSH);
 		reloadItem.setText("&Reload\tF5");
 		reloadItem.setAccelerator(SWT.F5);
 		reloadItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				System.out.println("Reloading...");
 				evaluate();
 				System.out.println("Reloaded");
 			}
 		});
 
 		MenuItem exportCallsItem = new MenuItem(fileMenu, SWT.PUSH);
 		exportCallsItem.setText("Export calls");
 		exportCallsItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
 					dialog.setFilterExtensions(new String[] {"*.sam", "*"});
 					if (myFile != null) {
 						File suggested = new File(myFile.getParentFile(), "mustCall.sam");
 						dialog.setFilterPath(myFile.getParent());
 						System.out.println(suggested);
 						dialog.setFileName(suggested.getPath());
 					}
 					String path = dialog.open();
 					if (path != null) {
 						exportCalls(new File(path));
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		MenuItem quitItem = new MenuItem(fileMenu, SWT.PUSH);
 		quitItem.setText("&Quit");
 		quitItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				System.exit(1);
 			}
 		});
 
 		relationsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		relationsMenuHeader.setText("&Relations");
 		relationsMenu = new Menu(shell, SWT.DROP_DOWN);
 		relationsMenuHeader.setMenu(relationsMenu);
 
 		objectsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		objectsMenuHeader.setText("&Objects");
 		objectsMenu = new Menu(shell, SWT.DROP_DOWN);
 		objectsMenuHeader.setMenu(objectsMenu);
 
 		MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		helpMenuHeader.setText("&Help");
 		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
 		helpMenuHeader.setMenu(helpMenu);
 
 		MenuItem userGuide = new MenuItem(helpMenu, 0);
 		userGuide.setText("User Guide");
 		userGuide.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Program.launch("http://www.serscis.eu/sam/");
 			}
 		});
 
 		MenuItem about = new MenuItem(helpMenu, 0);
 		about.setText("About");
 		about.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				MessageBox box = new MessageBox(shell, SWT.OK);
 				box.setText("About");
 				box.setMessage("SERSCIS Access Modeller\n© University of Southampton IT Innovation Centre, 2011");
 				box.open();
 			}
 		});
 
 		mainImage = new Label(shell, SWT.CENTER);
 		mainImage.setBackground(white);
 
 		messageList = new List(shell, 0);
 		messageList.addSelectionListener(new SelectionAdapter() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				ILiteral optProblem = myProblems.get(messageList.getSelectionIndex());
 				System.out.println(optProblem);
 				if (optProblem != null) {
 					try {
 						new DebugViewer(shell, liveResults, optProblem);
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 		});
 
 		GridLayout gridLayout = new GridLayout();
  		gridLayout.numColumns = 1;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 
 		GridData imageLayoutData = new GridData();
 		imageLayoutData.horizontalAlignment = GridData.FILL;
 		imageLayoutData.verticalAlignment = GridData.FILL;
 		imageLayoutData.grabExcessHorizontalSpace = true;
 		imageLayoutData.grabExcessVerticalSpace = true;
 		mainImage.setLayoutData(imageLayoutData);
 
 		GridData assertionsLayoutData = new GridData();
 		assertionsLayoutData.horizontalAlignment = GridData.FILL;
 		assertionsLayoutData.verticalAlignment = GridData.FILL;
 		assertionsLayoutData.grabExcessHorizontalSpace = true;
 		assertionsLayoutData.grabExcessVerticalSpace = false;
 		messageList.setLayoutData(assertionsLayoutData);
 
  		shell.setLayout(gridLayout);
 
 		evaluate();
 
 		shell.open();
 
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		display.dispose();
 	}
 
 	private Menu makeExamplesMenu(MenuItem parent) {
 		Menu examplesMenu = new Menu(parent);
 
 		final File examplesDir = new File(System.getenv("SAM_EXAMPLES"));
 
 		String[] files = examplesDir.list();
 		Arrays.sort(files);
 		for (final String name : files) {
 			if (name.endsWith(".sam") && !name.endsWith("-common.sam")) {
 				MenuItem item = new MenuItem(examplesMenu, SWT.PUSH);
 				item.setText(name);
 				item.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						myFile = new File(examplesDir, name);
 						evaluate();
 					}
 				});
 			}
 		}
 
 		return examplesMenu;
 	}
 
 	private void evaluate() {
 		try {
 			relationsMenuHeader.setEnabled(false);
 			objectsMenuHeader.setEnabled(false);
 			messageList.removeAll();
 			myProblems = new LinkedList<ILiteral>();
 
 			for (MenuItem item : relationsMenu.getItems()) {
 				item.dispose();
 			}
 
 			for (MenuItem item : objectsMenu.getItems()) {
 				item.dispose();
 			}
 
 			if (myFile == null) {
 				addInfo("Open a file to start");
 				return;
 			}
 
 			Eval eval = new Eval();
 			shell.setText("SAM: " + myFile);
 			final Results results = eval.evaluate(myFile);
 			liveResults.update(results);
 
 			if (results.exception != null) {
 				results.exception.printStackTrace();
 				String msg = results.exception.getMessage();
 				if (msg == null) {
 					msg = results.exception.toString();
 				}
 				addWarning(msg, null);
 			} else if (results.phase != Results.Phase.Success) {
 				addWarning("Problem in " + results.phase + " phase", null);
 			} else {
 				addInfo("OK");
 			}
 
 			for (ILiteral errorLit : results.errors) {
 				ITuple tuple = errorLit.getAtom().getTuple();
 				String msg = tuple.get(0).getValue().toString();
 				for (int part = 1; part < tuple.size(); part++) {
 					msg += ", " + tuple.get(part).getValue();
 				}
 
 				addWarning(msg, errorLit);
 			}
 
 			if (results.finalKnowledgeBase != null) {
 				File tmpFile = File.createTempFile("sam-", "-graph.png");
 				Image image;
 				try {
 					Graph.graph(results.finalKnowledgeBase, tmpFile);
 					InputStream is = new FileInputStream(tmpFile);
 					try {
 						image = new Image(display, is);
 					} finally {
 						is.close();
 					}
 				} finally {
 					tmpFile.delete();
 				}
 
 				mainImage.setImage(image);
 				mainImage.pack();
 
 				relationsMenuHeader.setEnabled(true);
 				objectsMenuHeader.setEnabled(true);
 
 				/* Populate Relations menu */
 				IPredicate[] relations = results.model.declared.keySet().toArray(new IPredicate[] {});
 				Arrays.sort(relations);
 				for (final IPredicate pred : relations) {
 					MenuItem item = new MenuItem(relationsMenu, SWT.PUSH);
 					final ITuple args = results.model.declared.get(pred);
 					item.setText(pred.toString() + args);
 					item.addSelectionListener(new SelectionAdapter() {
 						public void widgetSelected(SelectionEvent e) {
 							try {
 								new RelationViewer(shell, liveResults, pred);
 							} catch (Throwable ex) {
 								ex.printStackTrace();
 							}
 						}
 					});
 				}
 
 				/* Populate Objects menu */
 				String[] objects = getObjects(results);
 				for (final String name : objects) {
 					MenuItem item = new MenuItem(objectsMenu, SWT.PUSH);
 					item.setText(name);
 					item.addSelectionListener(new SelectionAdapter() {
 						public void widgetSelected(SelectionEvent e) {
 							try {
 								new ObjectViewer(shell, liveResults, name);
 							} catch (Throwable ex) {
 								ex.printStackTrace();
 							}
 						}
 					});
 				}
 			}
 
 			messageList.pack();
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 
 		shell.layout();
 	}
 
 	private String[] getObjects(Results results) throws Exception {
 		ILiteral lit = BASIC.createLiteral(true, Constants.hasNonValueTypeP, BASIC.createTuple(TERM.createVariable("Object")));
 
 		IQuery query = BASIC.createQuery(lit);
 		IRelation rel = results.finalKnowledgeBase.execute(query);
 		String[] objects = new String[rel.size()];
 		for (int i = 0; i < objects.length; i++) {
 			objects[i] = rel.get(i).get(0).getValue().toString();
 		}
 		Arrays.sort(objects);
 		return objects;
 	}
 
 	private void addWarning(String msg, ILiteral optProblem) {
 		myProblems.add(optProblem);
 		messageList.add(msg);
 	}
 
 	private void addInfo(String msg) {
 		myProblems.add(null);
 		messageList.add(msg);
 	}
 
 	private String quote(ITerm term) {
 		return quote(term.getValue().toString());
 	}
 
 	private String quote(String s) {
 		return "\"" + s.replaceAll("\"", "\\\"") + "\"";
 	}
 
 	private void exportCalls(File file) throws Exception {
 		Results results = liveResults.getResults();
 
 		ILiteral lit = BASIC.createLiteral(true, Constants.didCallP, BASIC.createTuple(
 					TERM.createVariable("Source"),
 					TERM.createVariable("SourceInvocation"),
 					TERM.createVariable("CallSite"),
 					TERM.createVariable("Target"),
 					TERM.createVariable("TargetInvocation"),
 					TERM.createVariable("Method")));
 
 		IQuery query = BASIC.createQuery(lit);
 		IRelation rel = results.finalKnowledgeBase.execute(query);
 		String[] calls = new String[rel.size()];
 		for (int i = 0; i < calls.length; i++) {
 			ITuple call = rel.get(i);
 			String caller = call.get(0).getValue().toString();
 			if (!caller.equals("_testDriver")) {
 				calls[i] = "mustCall(" + quote(caller) + ", " + quote(call.get(3)) + ", " + quote(call.get(5)) + ").\n";
 			} else {
 				calls[i] = "";
 			}
 		}
 		Arrays.sort(calls);
 
 		String[] objects = getObjects(results);
 
 		Writer writer = new FileWriter(file);
 		try {
 			for (String call : calls) {
 				if (!call.equals("")) {
 					writer.write(call);
 				}
 			}
 			writer.write("\n");
 			writer.write("mayCall(\"_testDriver\", ?Target, ?Method) :- isObject(?Target), hasMethod(?Type, ?Method).\n");
 			writer.write("mayCall(\"_testDriver\", ?Target, ?Method) :- isObject(?Target), hasConstructor(?Type, ?Method).\n");
 			writer.write("\n");
 			for (String object : objects) {
 				if (!object.equals("_testDriver")) {
 					writer.write("checkCalls(" + quote(object) + ").\n");
 				}
 			}
 		} finally {
 			writer.close();
 		}
 	}
 }
