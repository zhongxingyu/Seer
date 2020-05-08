 package net.bioclipse.xws4j;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 
 /**
  * 
  * This file is part of the Bioclipse xws4j Plug-in.
  * 
  * Copyright (C) 2008 Johannes Wagener
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * @author Johannes Wagener
  */
 public class XwsConsole {
 	
 	private static MessageConsole messageConsole = null;
 	private static String consoleName = "XWS Console";
 	private static MessageConsoleStream out = null,
 										out_blue = null,
 										out_red = null;
 	
 	public static void show() {
 		IWorkbench wb = PlatformUI.getWorkbench();
 		IWorkbenchPage wbPage = wb.getActiveWorkbenchWindow().getActivePage(); 
         if (wbPage != null) {
         	try {
         		IConsoleView conView = (IConsoleView) wbPage.showView(
 						IConsoleConstants.ID_CONSOLE_VIEW);
         		conView.display(getXwsConsole());
         	} catch (PartInitException e) {
         		PluginLogger.log("XwsConsole.show() - PartInitException: " + e.getMessage());
         	}
         }
 	}
 
 	public static MessageConsole getXwsConsole() {
 		if (messageConsole == null) {
 			messageConsole = findConsole(consoleName);
 		}
 		return messageConsole;
 	}
 
 	private static MessageConsole findConsole(String name) {
 		ConsolePlugin conPlugin = ConsolePlugin.getDefault();
 		IConsoleManager conManager = conPlugin.getConsoleManager();
 		IConsole[] consAll = conManager.getConsoles();
 		for (int i = 0; i < consAll.length; i++)
 			if (name.equals(consAll[i].getName()))
 				return (MessageConsole) consAll[i];
 		//no console found, so we create a new one
 		MessageConsole xwsConsole = new MessageConsole(name, null);
 		conManager.addConsoles(new IConsole[]{xwsConsole});
 		return xwsConsole;
 	}
 	
 	public static void writeToConsole(String message) {
 		getConsoleStream().println(message);
 	}
 
 	// with time-stamp
 	public static void writeToConsoleT(String message) {
 		getConsoleStream().println(getCurrentTime() + " " + message);
 	}
 	
 	public static void writeToConsoleBlue(String message) {
 		getConsoleStreamBlue().println(message);
 	}
 
 	// with time-stamp
 	public static void writeToConsoleBlueT(String message) {
 		getConsoleStreamBlue().println(getCurrentTime() + " " + message);
 	}
 	
 	public static void writeToConsoleRed(String message) {
 		getConsoleStreamRed().println(message);
 	}
 
 	// with time-stamp
 	public static void writeToConsoleRedT(String message) {
 		getConsoleStreamRed().println(getCurrentTime() + " " + message);
 	}
 	
 	public static MessageConsoleStream getConsoleStream() {
 		if (out == null)
 			out = getXwsConsole().newMessageStream();
 		return out;
 	}
 
 	public static MessageConsoleStream getConsoleStreamBlue() {
 		if (out_blue == null) {
 			Color color_blue = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE);
 			out_blue = getXwsConsole().newMessageStream();
 			out_blue.setColor(color_blue);
 		}
 		return out_blue;
 	}
 	
 	public static MessageConsoleStream getConsoleStreamRed() {
 		if (out_red == null) {
 			Color color_red = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
			out_blue = getXwsConsole().newMessageStream();
			out_blue.setColor(color_red);
 		}
 		return out_red;
 	}
 	
 	public static String getCurrentTime() {
 		SimpleDateFormat simpleDateForm = new SimpleDateFormat("hh:mm:ss");
 		Date current = new Date();
 		current.setTime(System.currentTimeMillis());
 		return simpleDateForm.format(current);
 	}
 }
