 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.JApplet;
 
 /**
  * Nikunj Koolar cs681-3 Fall 2002 - Spring 2003 Project VGL File: VGLMain.java
  * -this is the main driver of the application. It has been configured an applet
  * as a stand-alone application.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  * 
  * @author Nikunj Koolar
 * @version 1.0 $Id: VGLApplet.java,v 1.4 2005-06-04 01:24:53 brian Exp $
  */
 public class VGLApplet extends JApplet {
 	/**
 	 * The default path to be specified for all file dialogs
 	 */
 	private static File m_DefaultDir;
 
 	/**
 	 * A unique program id for the application
 	 */
 	private static String m_PROGRAM_ID = "Virtual Genetics Lab";
 
 	/**
 	 * The logo for the application
 	 */
 	private static URL imageURL = VGLApplet.class
 			.getResource("UIimages/logo.gif");
 
 	private static Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
 
 	static String widthString;
 	static String heightString;
 	static String inputURLString;
 	
 	/**
 	 * The Constructor
 	 */
 	public VGLApplet() {
 	}
 
 	
 	/**
 	 * Run applet as applet.
 	 */
 	public void init() {
 		widthString = getParameter("width");
 		if (widthString == null) {
 			widthString = "350";
 		}
 		
 		heightString = getParameter("height");
 		if (heightString == null) {
 			heightString = "150";
 		}
 		
		inputURLString = getParameter("workFile");
 		if (inputURLString == null) {
 			inputURLString = "";
 		}
 		
 	    URL configFileURL = null;
 		try {
 			configFileURL = new URL(inputURLString);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		VGLMainApp app = new VGLMainApp(image);
 		getContentPane().add("Center", app);
 		
 		int wdt = 350;
 		try {
 			wdt = Integer.parseInt(widthString);
 		} catch (NumberFormatException e1) {
 			e1.printStackTrace();
 		}
 		
 		int hgt = 150;
 		try {
 			hgt = Integer.parseInt(heightString);
 		} catch (NumberFormatException e2) {
 			e2.printStackTrace();
 		}
 
 		setSize(wdt, hgt);
 		app.init();
 		app.start();
 		app.openProblem(configFileURL);
 
 	}
 }
