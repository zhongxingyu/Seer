 /*	
  *  Copyright (c) 2011-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
  *  be obtained by sending an e-mail to atif@cs.umd.edu
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
  *  documentation files (the "Software"), to deal in the Software without restriction, including without 
  *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
  *	conditions:
  * 
  *	The above copyright notice and this permission notice shall be included in all copies or substantial 
  *	portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
  *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
  *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 package edu.umd.cs.guitar.sitar.ripper.test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.umd.cs.guitar.sitar.model.SitarWindow;
 import edu.umd.cs.guitar.sitar.model.swtwidgets.SitarWidget;
 import edu.umd.cs.guitar.sitar.model.swtwidgets.SitarWidgetFactory;
 
 /**
  * Tests for {@link SitarWidget}.
  */
 public class SitarWidgetTest {
 
 	private Display display;
 	private SitarWidgetFactory factory;
 
 	/**
 	 * Set up the display.
 	 */
 	@Before
 	public void setUp() {
 		factory = SitarWidgetFactory.INSTANCE;
 		if (display == null || display.isDisposed()) {
 			display = new Display();
 		}
 	}
 	
 	/**
 	 * Dispose of the display.
 	 */
 	@After
 	public void tearDown() {
 		display.dispose();
 	}
 	
 	/**
 	 * Test {@link SitarWidget#isTerminal()}.
 	 */
 	@Test
 	public void testIsTerminal() {
		final Shell shell = new Shell(display);
 		Button button = new Button(shell, SWT.PUSH);
 		
 		SitarWidget swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
 		
 		assertFalse(swtButton.isTerminal());
 		// make sure we didn't actually close the shell
 		assertFalse(shell.isDisposed());
 		
 		swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
 		button.setText("Quit");
 		assertFalse(swtButton.isTerminal());
 		assertFalse(shell.isDisposed());
 		
 		swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
 		button.setText("Close");
 		assertFalse(swtButton.isTerminal());
 		assertFalse(shell.isDisposed());
 		
 		swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
 		button.setText("Exit");
 		assertFalse(swtButton.isTerminal());
 		assertFalse(shell.isDisposed());
 		
 		SelectionAdapter closeShell = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
				shell.close();
 			}
 		}; 
 		
 		swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
 		button.addSelectionListener(closeShell);
 		assertTrue(swtButton.isTerminal());
 		assertFalse(shell.isDisposed());
 		
 		swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
 		button.removeSelectionListener(closeShell);
 		assertFalse(swtButton.isTerminal());
 		assertFalse(shell.isDisposed());
 				
 		// test that existing close listeners aren't notified
 		final AtomicBoolean listenerNotified = new AtomicBoolean(false);
 		
 		ShellListener shellListener = new ShellAdapter() {
 			@Override
 			public void shellClosed(ShellEvent e) {
 				listenerNotified.set(true);
 			}
 		};
 		shell.addShellListener(shellListener);
 		
 		swtButton = factory.newSWTWidget(button, new SitarWindow(shell));
 		assertFalse(swtButton.isTerminal());
 		assertFalse(listenerNotified.get());
 		
 		shell.open(); // just to be sure
 		shell.close();
 		
 		// make sure existing listeners are notified if real close happens
 		assertTrue(listenerNotified.get());
 		
 	}
 		
 }
