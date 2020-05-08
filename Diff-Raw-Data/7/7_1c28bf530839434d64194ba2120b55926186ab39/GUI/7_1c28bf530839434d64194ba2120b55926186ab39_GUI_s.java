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
 
 import java.util.Arrays;
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
 
 public class GUI {
 	private File myFile;
 	private Results results;
 	private Display display;
 	private Shell shell;
 	private Label warningLabel;
 	private Label mainImage;
 	private Color white;
 
 	public GUI(File file) throws Exception {
 		myFile = file;
 
 		display = new Display();
 		white = new Color(display, 255, 255, 255);
 		shell = new Shell(display);
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
 				System.out.println("reload");
 				evaluate();
 			}
 		});
 
 		MenuItem quitItem = new MenuItem(fileMenu, SWT.PUSH);
 		quitItem.setText("&Quit");
 		quitItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				System.exit(1);
 			}
 		});
 
 		warningLabel = new Label(shell, SWT.CENTER);
 
 		mainImage = new Label(shell, SWT.CENTER);
 		mainImage.setBackground(white);
 
 		GridLayout gridLayout = new GridLayout();
  		gridLayout.numColumns = 1;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 
 		GridData warningLayoutData = new GridData();
 		warningLayoutData.horizontalAlignment = GridData.FILL;
 		warningLayoutData.grabExcessHorizontalSpace = true;
 		warningLayoutData.grabExcessVerticalSpace = false;
 		warningLabel.setLayoutData(warningLayoutData);
 
 		GridData imageLayoutData = new GridData();
 		imageLayoutData.horizontalAlignment = GridData.FILL;
 		imageLayoutData.verticalAlignment = SWT.FILL;
 		imageLayoutData.grabExcessHorizontalSpace = true;
 		imageLayoutData.grabExcessVerticalSpace = true;
 		mainImage.setLayoutData(imageLayoutData);
 
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
 			if (myFile == null) {
 				warningLabel.setText("Open a file to start");
 
 				warningLabel.setBackground(new Color(display, 0, 100, 0));
 				warningLabel.setForeground(new Color(display, 255, 255, 255));
 				warningLabel.pack();
 				return;
 			}
 
 			Eval eval = new Eval();
 			results = eval.evaluate(myFile);
 
 			if (results.exception != null) {
				warningLabel.setText(results.exception.getMessage());
 
 				warningLabel.setBackground(new Color(display, 255, 50, 50));
 				warningLabel.setForeground(new Color(display, 255, 255, 255));
 			} else if (results.phase != Results.Phase.Success) {
 				warningLabel.setText("Problem in " + results.phase + " phase");
 
 				warningLabel.setBackground(new Color(display, 255, 50, 50));
 				warningLabel.setForeground(new Color(display, 255, 255, 255));
 			} else {
 				warningLabel.setText("OK");
 
 				warningLabel.setBackground(new Color(display, 0, 100, 0));
 				warningLabel.setForeground(new Color(display, 255, 255, 255));
 			}
 			warningLabel.pack();
 
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
 			}
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 
 		shell.layout();
 	}
 }
