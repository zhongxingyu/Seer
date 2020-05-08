 package com.cactusretreat.cactuslauncher.gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Shell;
 
 import com.cactusretreat.cactuslauncher.GameUpdate;
 
 public class ModpackUpdater {
 
 	private Display display;
 	private Shell shell;
 	private GridLayout layout;
 	private GridData data;
 	
 	private Label statusText;
 	private Label statusFine;
 	private ProgressBar statusBar;
 	
 	private Composite windowComposite;
 	
 	private String dataFolderPath;
 	private String modpackName;
 	private boolean isFinished = false;
 	private boolean forceUpdate;
 	
 	public ModpackUpdater(String dataFolderPath, String modpackName, boolean forceUpdate) {
 		this.dataFolderPath = dataFolderPath;
 		this.modpackName = modpackName;
 		this.forceUpdate = forceUpdate;
 		init();
 	}
 	
 	public void start() {
 		final GameUpdate updater =  new GameUpdate(this, dataFolderPath, modpackName, forceUpdate);
 		Thread t = new Thread(updater);
 		t.start();
 		
 		run();
 	}
 	
 	private void init() {
 		display = new Display();
		shell = new Shell();
 		setupWindow();
 		shell.open();
 	}
 	
 	private void setupWindow() {
 		shell.setSize(300, 150);
 		shell.setText("Cactus Modpack Updater");
 		shell.setLayout(new FillLayout());
 		
 		layout = new GridLayout(1, false);
 		windowComposite = new Composite(shell, SWT.NONE);
 		windowComposite.setLayout(layout);
 		
 		data = new GridData(SWT.FILL, SWT.CENTER, true, true);
 		data.horizontalIndent = 2;
 		statusText = new Label(windowComposite, SWT.BOLD);
 		statusText.setLayoutData(data);
 		FontData[] fd = statusText.getFont().getFontData();
 		fd[0].setHeight(15);
 		statusText.setFont(new Font(display, fd));
 		
 		data = new GridData(SWT.FILL, SWT.CENTER, true, true);
 		statusFine = new Label(windowComposite, SWT.NONE);
 		statusFine.setLayoutData(data);
 		
 		statusBar = new ProgressBar(windowComposite, SWT.HORIZONTAL);
 		data = new GridData(SWT.FILL, SWT.CENTER, true, true);
 		data.heightHint = 40;
 		statusBar.setLayoutData(data);
 	}
 	
 	public void run() {
 		while(!isFinished) {			
 			
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
			
			//statusText.setText(statusText.getText());
			//statusFine.setText(statusFine.getText());			
 		}
 		display.dispose();
 	}
 	
 	public void setStatusText(final String text) {
 		display.syncExec(new Runnable(){
 			@Override
 			public void run() {
 				statusText.setText(text);
 			}
 		});
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setStatusFine(final String text) {
 		display.syncExec(new Runnable(){
 			@Override
 			public void run() {
 				statusFine.setText(text);
 			}
 		});
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setProgress(final int progress) {
 		display.syncExec(new Runnable(){
 			@Override
 			public void run() {
 				statusBar.setSelection(progress);
 			}
 			
 		});
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	public void setFinished() {
 		isFinished = true;
 	}
 	
 	public ModpackUpdater get() {
 		return this;
 	}
 
 	public void close() {
 		if (!display.isDisposed()) {
 			display.dispose();
 		}
 	}
 	
 }
