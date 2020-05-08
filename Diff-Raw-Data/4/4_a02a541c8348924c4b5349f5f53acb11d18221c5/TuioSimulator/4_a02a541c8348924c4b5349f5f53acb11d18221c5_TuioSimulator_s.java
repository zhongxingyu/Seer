 /*
     TUIO Simulator - part of the reacTIVision project
     http://reactivision.sourceforge.net/
 
     Copyright (c) 2005-2009 Martin Kaltenbrunner <mkalten@iua.upf.edu>
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javax.swing.JFrame;
 
 public class TuioSimulator {
 	
 	public static int width = 800;
 	public static int height = 600;
 	
 	public static void main(String[] argv) {
 	
 		String host = "127.0.0.1";
 		int port = 3333;
 		String config=null;
 		
 		for (int i=0;i<argv.length;i++) {
 			if (argv[i].equalsIgnoreCase("-host")) {
 				try { host = argv[i+1]; } catch (Exception e) {}
 				i++;
 			} else if (argv[i].equalsIgnoreCase("-port")) {
 				try { port = Integer.parseInt(argv[i+1]); } catch (Exception e) {}
 				i++;
 			} else if (argv[i].equalsIgnoreCase("-config")) {
 				try { config = argv[i+1]; } catch (Exception e) {}
 				i++;
 			} else {
 				System.out.println("TuioSimulator options:");
 				System.out.println("\t-host\ttarget IP");
 				System.out.println("\t-port\ttarget port");
 				System.out.println("\t-config\tconfig file");
 				System.exit(0);
 			}
 		}
 
 		System.out.println("sending TUIO messages to "+host+":"+port);
 
 		JFrame app = new JFrame();
 		
 		app.setTitle("reacTIVision TUIO Simulator");
 		
 		
 		
 		
 		try {
 		   Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
 		   Method mSetWindowOpacity = awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
 		   mSetWindowOpacity.invoke(null, app, Float.valueOf(0.50f));
 		} catch (NoSuchMethodException ex) {
 		   ex.printStackTrace();
 		} catch (SecurityException ex) {
 		   ex.printStackTrace();
 		} catch (ClassNotFoundException ex) {
 		   ex.printStackTrace();
 		} catch (IllegalAccessException ex) {
 		   ex.printStackTrace();
 		} catch (IllegalArgumentException ex) {
 		   ex.printStackTrace();
 		} catch (InvocationTargetException ex) {
 		   ex.printStackTrace();
 		}
 		
 		
 
 		final Manager manager = new Manager(app,config);
 		final Simulation simulation = new Simulation(manager,host,port);
 		//Thread simulationThread = new Thread(simulation);
 		//simulationThread.start();
 
 		app.getContentPane().add(simulation);
 		
 		 
 		
 		app.addWindowListener( new WindowAdapter() { 
 			public void windowClosing(WindowEvent evt) {	
 				simulation.reset();
 				System.exit(0);
 			} 
 		});
 		
 		app.addComponentListener(new ComponentAdapter() {
             public void componentResized(ComponentEvent e) {
 
             }
         });
 
 		//app.pack();
 		//app.setSize(width, height);
 
 		// FULLSCREEN!	
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();  
         app.setSize(screenSize);
         app.setResizable(false);
         app.setUndecorated(true);
 		
 		app.setVisible(true);
 	}
 }
