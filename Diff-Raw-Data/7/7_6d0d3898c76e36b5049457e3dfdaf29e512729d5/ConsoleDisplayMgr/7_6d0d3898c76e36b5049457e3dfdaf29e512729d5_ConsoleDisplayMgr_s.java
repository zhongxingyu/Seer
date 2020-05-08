 /*******************************************************************************
  * Copyright (c) {06/11/2005} {Christopher Thomas} 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  *
  * Contributors:
  *    {Christopher Thomas} - initial API and implementation
  *    chris.alex.thomas@gmail.com
  *    {Danny Valliant} - Initial Hotkey support and Console output
  *    xenden@users.sourceforge.net
  *******************************************************************************/
 
 package com.antimatterstudios.esftp.ui;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
  
 /**
  * Create an instance of this class in any of your plugin classes.
  * 
  * Use it as follows ...
  * 
  * ConsoleDisplayMgr.getDefault().println("Some error msg", ConsoleDisplayMgr.MSG_ERROR);
  * ...
  * ...
  * ConsoleDisplayMgr.getDefault().clear();
  * ...  
  */
 public class ConsoleDisplayMgr
 {
 	private static ConsoleDisplayMgr Console = null;
 	private MessageConsole m_MessageConsole = null;
 	private String m_Title = null;	
 	
 	public static final int MSG_INFORMATION = 1;
 	public static final int MSG_ERROR = 2;
 	public static final int MSG_WARNING = 3;
 		
 	protected ConsoleDisplayMgr(String messageTitle){		
 		m_Title = messageTitle;
 		
 		m_MessageConsole = new MessageConsole(m_Title, null); 
 		m_MessageConsole.setTabWidth(4);
 		IConsole[] c = new IConsole[]{ m_MessageConsole };
 		ConsolePlugin.getDefault().getConsoleManager().addConsoles(c);		
 	}
 
 	public static ConsoleDisplayMgr getDefault(String consoleTitle){
 		if(Console == null) Console = new ConsoleDisplayMgr(consoleTitle);
 		
 		return Console;
 	}	
 	
 	private void showError(String msg){
 		Shell sh = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		MessageDialog.openError(sh, "Error", msg);
 	}
 		
 	public void print(String msg, int msgKind){
 		if( msg == null ) return;
 		
 		/* if console-view in Java-perspective is not active, then show it and
 		 * then display the message in the console attached to it */		
 		if( !displayConsoleView() ){
 			/*If an exception occurs while displaying in the console, then just diplay atleast the same in a message-box */
 			showError(msg);
 		}else{		
 			/* display message on console */	
 			getNewMessageConsoleStream(msgKind).print(msg);
 		}
 	}
 	
 	public void println(String msg, int msgKind){		
 		if( msg == null ) return;
 		
 		/* if console-view in Java-perspective is not active, then show it and
 		 * then display the message in the console attached to it */		
 		if( !displayConsoleView() ){
 			/*If an exception occurs while displaying in the console, then just diplay atleast the same in a message-box */
 			showError(msg);
 		}else{
 			/* display message on console */	
 			getNewMessageConsoleStream(msgKind).println(msg);
 		}
 	}
 	
 	public void clear(){		
 		IDocument document = m_MessageConsole.getDocument();
 		if (document != null) {
 			document.set("");
 		}			
 	}	
 		
 	private boolean displayConsoleView(){
 		try{
 			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 			if( activeWorkbenchWindow != null ){
 				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
 				if( activePage != null ){
 					activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
 				}
 			}			
 		} catch (PartInitException partEx) {			
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private MessageConsoleStream getNewMessageConsoleStream(int msgKind){		
 		int swtColorId = SWT.COLOR_DARK_GREEN;
 		
 		switch (msgKind){
 			case MSG_INFORMATION:
 				swtColorId = SWT.COLOR_DARK_GREEN;				
 			break;
 
 			case MSG_ERROR:
 				swtColorId = SWT.COLOR_DARK_MAGENTA;
 			break;
 			
 			case MSG_WARNING:
 				swtColorId = SWT.COLOR_DARK_BLUE;
 			break;				
 		}	
 		
 		MessageConsoleStream msgConsoleStream = m_MessageConsole.newMessageStream();
 		
 		if (msgConsoleStream == null) {
 			System.out.print("Error getting console message stream");
 		}else{
			msgConsoleStream.setColor(Display.getCurrent().getSystemColor(swtColorId));
 		}
 		return msgConsoleStream;
 	}
 }
