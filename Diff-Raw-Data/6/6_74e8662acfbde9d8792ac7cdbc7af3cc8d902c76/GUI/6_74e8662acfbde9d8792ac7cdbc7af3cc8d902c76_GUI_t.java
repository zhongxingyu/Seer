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
 
 import eu.serscis.sam.ScenarioResult;
 import org.eclipse.swt.graphics.Font;
 import java.util.Iterator;
 import eu.serscis.sam.InvalidModelException;
 import eu.serscis.sam.TermDefinition;
 import org.eclipse.swt.custom.ScrolledComposite;
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
 import org.eclipse.swt.layout.FillLayout;
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
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.RowLayout;
 import static org.deri.iris.factory.Factory.*;
 
 public class GUI {
 	private File myFile;
 	final LiveResults liveResults = new LiveResults();
 	private Display display;
 	final Shell shell;
 	MenuItem relationsMenuHeader;
 	Menu relationsMenu;
 	MenuItem objectsMenuHeader;
 	Menu objectsMenu;
 	private TabItem welcomeTab = null;
 
 	private TabFolder mainFolder;
 	private LinkedList<String> currentTabs = null;
 	private LinkedList<ScenarioView> myTabs = new LinkedList<ScenarioView>();
 
 	private Shell errorBox = null;
 
 	public GUI(File file) throws Exception {
 		if (file != null) {
 			myFile = file.getAbsoluteFile();
 		}
 
 		display = new Display();
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
 		// Do it this way so it works in any window
 		display.addFilter(SWT.KeyDown, new Listener() {
 			public void handleEvent(Event ev) {
 				if (ev.keyCode == SWT.F5 ) {
 					System.out.println("Reloading...");
 					evaluate();
 					System.out.println("Reloaded");
 				}
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
 
 		objectsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		objectsMenuHeader.setText("&Objects");
 		objectsMenu = new Menu(shell, SWT.DROP_DOWN);
 		objectsMenuHeader.setMenu(objectsMenu);
 
 		relationsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		relationsMenuHeader.setText("&Relations");
 		relationsMenu = new Menu(shell, SWT.DROP_DOWN);
 		relationsMenuHeader.setMenu(relationsMenu);
 
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
 
 		mainFolder = new TabFolder(shell, 0);
 
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 
 		GridData layoutData = new GridData();
 		layoutData.horizontalAlignment = GridData.FILL;
 		layoutData.verticalAlignment = GridData.FILL;
 		layoutData.grabExcessHorizontalSpace = true;
 		layoutData.grabExcessVerticalSpace = true;
 		mainFolder.setLayoutData(layoutData);
 
 		mainFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					if (myFile == null) {
 						return;		// the welcome tab
 					}
 
					int i = mainFolder.getSelectionIndex();

					if (myTabs == null || i >= myTabs.size()) {
 						// (only seems to happen on Windows)
 						return;		// still initialising
 					}
 
 					ScenarioView view = myTabs.get(i);
 					String scenario = currentTabs.get(i);
 
 					liveResults.selectScenario(scenario);
 					view.update(liveResults);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 
 		shell.setLayout(gridLayout);
 
 		shell.open();
 
 		evaluate();
 
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		display.dispose();
 	}
 
 	private void updateTabs(Results results) {
 		if (!results.model.scenarios.equals(currentTabs)) {
 			if (welcomeTab != null) {
 				welcomeTab.dispose();
 				welcomeTab = null;
 			}
 
 			for (ScenarioView tab: myTabs) {
 				tab.dispose();
 			}
 			myTabs = new LinkedList<ScenarioView>();
 			for (String scenario : results.model.scenarios) {
 				ScenarioView view = new ScenarioView(this, mainFolder);
 				TabItem tabItem = view.getTab();
 				tabItem.setText(scenario);
 				myTabs.add(view);
 			}
 			currentTabs = new LinkedList<String>(results.model.scenarios);
 			liveResults.selectScenario("baseline");
 		}
 		shell.layout();
 	}
 
 	private Menu makeExamplesMenu(MenuItem parent) {
 		Menu examplesMenu = new Menu(parent);
 
 		final File examplesDir = new File(System.getenv("SAM_EXAMPLES"));
 
 		if (!examplesDir.isDirectory()) {
 			System.out.println("WARNING: Examples directory not found: " + examplesDir);
 			return examplesMenu;
 		}
 
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
 		BusyIndicator.showWhile(display, new Runnable() {
 			public void run() {
 				try {
 					relationsMenuHeader.setEnabled(false);
 					objectsMenuHeader.setEnabled(false);
 					if (myFile == null) {
 						welcomeTab = new TabItem(mainFolder, 0);
 						welcomeTab.setText("Welcome");
 						Label label = new Label(mainFolder, 0);
 						label.setText("Open a file to start");
 						welcomeTab.setControl(label);
 						return;
 					}
 
 					realEvaluate();
 
 					int i = mainFolder.getSelectionIndex();
 					ScenarioView view = myTabs.get(i);
 
 					view.update(liveResults);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 
 				shell.layout();
 			}
 		});
 	}
 
 	private void realEvaluate() throws Exception {
 		if (errorBox != null) {
 			errorBox.dispose();
 			errorBox = null;
 		}
 
 		Eval eval = new Eval();
 		shell.setText("SAM: " + myFile.getName() + " (" + myFile.getParent() + ")");
 		Results results = eval.evaluate(myFile);
 
 		if (results.exception != null) {
 			errorBox = new Shell(shell, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.DIALOG_TRIM);
 			errorBox.setText("Error in " + myFile);
 			Label label = new Label(errorBox, 0);
 			label.setText(formatException(results.exception));
 
 			Font mono = new Font(display, "Courier", 10, SWT.NORMAL);
 			label.setFont(mono);
 
 			Button ok = new Button(errorBox, SWT.PUSH);
 			ok.setText("OK");
 			ok.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					errorBox.dispose();
 				}
 			});
 
 			GridLayout gridLayout = new GridLayout();
 			gridLayout.numColumns = 1;
 			gridLayout.marginHeight = 8;
 			gridLayout.marginWidth = 8;
 			gridLayout.verticalSpacing = 8;
 
 			GridData layoutData = new GridData();
 			layoutData.horizontalAlignment = GridData.FILL;
 			layoutData.verticalAlignment = GridData.FILL;
 			layoutData.grabExcessHorizontalSpace = true;
 			layoutData.grabExcessVerticalSpace = true;
 			label.setLayoutData(layoutData);
 
 			GridData buttonData = new GridData();
 			buttonData.horizontalAlignment = GridData.END;
 			buttonData.verticalAlignment = GridData.FILL;
 			buttonData.grabExcessHorizontalSpace = false;
 			buttonData.grabExcessVerticalSpace = false;
 			ok.setLayoutData(buttonData);
 
 			errorBox.setLayout(gridLayout);
 
 			errorBox.layout();
 			errorBox.open();
 
 			throw results.exception;
 		}
 
 		liveResults.update(results);
 		updateTabs(results);
 	}
 
 	private String quote(ITerm term) {
 		return quote(term.getValue().toString());
 	}
 
 	private String quote(String s) {
 		return "\"" + s.replaceAll("\"", "\\\"") + "\"";
 	}
 
 	private void exportCalls(File file) throws Exception {
 		ScenarioResult results = liveResults.getResults();
 
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
 				calls[i] = "mustCall(" +
 						call.get(0) + ", " +
 						quote(call.get(1)) + ", " +
 						quote(call.get(2)) + ", " +
 						call.get(3) + ", " +
 						quote(call.get(5)) + ").\n";
 			} else {
 				calls[i] = "";
 			}
 		}
 		Arrays.sort(calls);
 
 		String[] objects = results.getObjects();
 
 		Writer writer = new FileWriter(file);
 		try {
 			for (String call : calls) {
 				if (!call.equals("")) {
 					writer.write(call);
 				}
 			}
 			writer.write("\n");
 			for (String object : objects) {
 				if (!object.equals("_testDriver")) {
 					writer.write("checkCalls(<" + object + ">).\n");
 				}
 			}
 		} finally {
 			writer.close();
 		}
 	}
 
 	public String formatException(Exception ex) {
 		if (!(ex instanceof InvalidModelException)) {
 			return ex.toString();
 		}
 
 		String msg = "";
 
 		boolean first = true;
 		Iterator<InvalidModelException> iter = ((InvalidModelException) ex).getChain();
 		while (iter.hasNext()) {
 			InvalidModelException link = iter.next();
 
 			if (first) {
 				msg += "Error: " + link.getMessage() + "\n\n";
 				first = false;
 			} else {
 				msg += "\nImported from here:\n\n";
 			}
 
 			msg += "  " + link.code + "\n";
 			String spaces = "  ";
 			for (int i = link.col; i > 1; i--) {
 				spaces += " ";
 			}
 			msg += spaces + "^\n";
 			msg += "  " + link.source + ":" + link.line + "\n";
 		}
 
 		return msg;
 	}
 }
