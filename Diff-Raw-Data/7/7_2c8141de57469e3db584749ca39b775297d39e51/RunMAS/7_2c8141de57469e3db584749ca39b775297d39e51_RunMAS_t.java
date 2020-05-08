 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
//   Revision 1.18  2005/11/09 22:38:53  jomifred
//   fixed bug
//
 //   Revision 1.17  2005/10/29 21:46:22  jomifred
 //   add a new class (MAS2JProject) to store information parsed by the mas2j parser. This new class also create the project scripts
 //
 //   Revision 1.16  2005/09/20 17:00:26  jomifred
 //   load classes from the project lib directory
 //
 //   Revision 1.15  2005/08/14 23:29:40  jomifred
 //   add TODO (use new java class to run process)
 //
 //   Revision 1.14  2005/08/12 23:29:11  jomifred
 //   support for saci arch in IA createAgent
 //
 //   Revision 1.13  2005/08/12 21:08:23  jomifred
 //   add cvs keywords
 //
 //----------------------------------------------------------------------------
 
 package jIDE;
 
 import java.awt.event.ActionEvent;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import saci.launcher.Launcher;
 import saci.launcher.LauncherD;
 
 // TODO: when using jdk 1.5, change RunProcess to ProcessBuilder as
 // explained at http://java.sun.com/developer/JDCTechTips/2005/tt0727.html#1
 
 /** runs an MAS */
 class RunMAS extends AbstractAction {
 
 	JasonID jasonID;
 
 	MASRunner masRunner;
 
 	RunMAS(JasonID jID) {
 		super("Run MAS...", new ImageIcon(JasonID.class.getResource("/images/execute.gif")));
 		jasonID = jID;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		try {
 			jasonID.output.setText("");
 			jasonID.saveAllAct.actionPerformed(null);
 			
 			String jasonJar = jasonID.getConf().getProperty("jasonJar");
 			if (!JasonID.checkJar(jasonJar)) {
 				System.err.println("The path to the jason.jar file ("+jasonJar+") was not correctly set, the MAS may not run. Go to menu Edit->Preferences to configure the path.");
 			}
 			String javaHome = jasonID.getConf().getProperty("javaHome");
 			if (!JasonID.checkJavaPath(javaHome)) {
 				System.err.println("The Java home directory ("+javaHome+") was not correctly set, the MAS may not run. Go to menu Edit->Preferences to configure the path.");
 			}
 			
 			
 			boolean ok = jasonID.fMAS2jThread.foregroundCompile();
 			if (ok) {
 				jasonID.openAllASFiles(jasonID.fMAS2jThread.fCurrentProject.getAllASFiles());
 				ok = jasonID.fASParser.foregroundCompile();
 				if (ok) {
 					jasonID.runMASButton.setEnabled(false);
 					jasonID.debugMASButton.setEnabled(false);
 
 					// compile some files
 					CompileThread compT = new CompileThread(jasonID.fMAS2jThread.fCurrentProject.getAllUserJavaFiles());
 					compT.start();
					if (masRunner != null) {
						masRunner.stopRunner();
					}
 
 					if (jasonID.fMAS2jThread.fCurrentProject.getArchitecture().equals("Centralised")) {
 						if (jasonID.getConf().getProperty("runCentralisedInsideJIDE").equals("true")) {
 							masRunner = new MASRunnerInsideJIDE(compT);
 						} else {
 							masRunner = new MASRunnerCentralised(compT);
 						}
 					} else if (jasonID.fMAS2jThread.fCurrentProject.getArchitecture().equals("Saci")) {
 						String saciJar = jasonID.getConf().getProperty("saciJar");
 						if (JasonID.checkJar(saciJar)) {
 							masRunner = new MASRunnerSaci(compT);
 						} else {
 							System.err.println("The path to the saci.jar file ("+saciJar+") was not correctly set. Go to menu Edit->Preferences to configure the path.");
 							return;
 						}
 					}
 					masRunner.start();
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	void stopMAS() {
 		if (masRunner != null) {
 			masRunner.stopRunner();
 		}
 		masRunner = null;
 	}
 
 	void exitJason() {
 		stopMAS();
 		//if (saciProcess != null) { // i've created saci
 		//	stopSaci();
 		//}
 	}
 
 
 	class StartSaci extends Thread {
 
 		//BufferedReader saciIn;
 		//BufferedReader saciErr;
 
 		boolean saciOk = false;
 		Process saciProcess;
 
 		StartSaci() {
 			super("StartSaci");
 		}
 
 		Launcher getLauncher() {
 			PrintStream err = System.err;
 			Launcher l = null;
 			try {
 				System.setErr(jasonID.originalErr);
 				l = LauncherD.getLauncher();
 				return l;
 			} catch (Exception e) {
 				return null;
 			} finally {
 				System.setErr(err);
 			}
 		}
 		
 		void stopSaci() {
 			try {
 				getLauncher().stop();
 			} catch (Exception e) {
 				try {
 					saciProcess.destroy();
 				} catch (Exception e2) {
 				}
 			}
 			saciProcess = null;
 		}
 
 		public void run() {
 			//stopSaci();
 			try {
 				//String command = getAsScriptCommand(jasonID.projectDirectory + File.separator + "saci-" + jasonID.getFileName());
 				String command = getAsScriptCommand("saci-" + jasonID.fMAS2jThread.fCurrentProject.getSocName(), true);
 				saciProcess = Runtime.getRuntime().exec(command, null,
 						//new File(JasonID.saciHome + File.separator + "bin"));
 						new File(jasonID.projectDirectory));
 				//saciIn = new BufferedReader(new InputStreamReader(saciProcess.getInputStream()));
 				//saciErr = new BufferedReader(new InputStreamReader(saciProcess.getErrorStream()));
 				System.out.println("running saci with " + command);
 
 				int tryCont = 0;
 				while (tryCont < 30) {
 					tryCont++;
 					sleep(1000);
 					Launcher l = getLauncher();
 					if (l != null) {
 						saciOk = true;
 						stopWaitSaciOk();
 						break;
 					}
 				}
 			} catch (Exception ex) {
 				System.err.println("error running saci:" + ex);
 			} finally {
 				stopWaitSaciOk();
 			}
 		}
 
 		synchronized void stopWaitSaciOk() {
 			notifyAll();
 		}
 
 		synchronized boolean waitSaciOk() {
 			try {
 				wait(20000); // waits 20 seconds
 				if (!saciOk) {
 					JOptionPane
 							.showMessageDialog(
 									jasonID.frame,
 									"Fail to automatically start saci! \nGo to \""
 											+ jasonID.projectDirectory
 											+ "\" directory and run the saci-"
 											+ jasonID.fMAS2jThread.fCurrentProject.getSocName()
 											+ " script.\n\nClick 'ok' when saci is running.");
 					wait(1000);
 					if (!saciOk) {
 						JOptionPane
 								.showMessageDialog(jasonID.frame,
 										"Saci might not be properly installed or configure. Use the centralised architecture to run your MAS");
 					}
 				}
 			} catch (Exception e) {
 			}
 			return saciOk;
 		}
 	}
 
 	class CompileThread extends Thread {
 		private boolean ok = true;
 		private boolean finished = false;
 		private Set files;
 
 		CompileThread(Set files) {
 			super("CompileThread");
 			this.files = files;
 		}
 
 		synchronized boolean waitCompilation() {
 			while (!finished) {
 				try {
 					wait(2000); // waits 2 seconds
 				} catch (Exception e) {
 				}
 			}
 			return ok;
 		}
 
 		synchronized void stopWaiting() {
 			notifyAll();
 		}
 
 		public void run() {
 			try {
 				if (needsComp()) {
 					String command = getAsScriptCommand("compile-" + jasonID.fMAS2jThread.fCurrentProject.getSocName());
 					System.out.println("Compiling user class with " + command);
 					Process p = Runtime.getRuntime().exec(command, null, new File(jasonID.projectDirectory));
 					p.waitFor();
 					ok = !needsComp();
 					if (!ok) {
 							BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 							while (in.ready()) {
 								jasonID.output.append(in.readLine() + "\n");
 							}
 					}
 				}
 			} catch (Exception e) {
 				System.err.println("Compilation error: " + e);
 			} finally {
 				finished = true;
 				stopWaiting();
 			}
 		}
 		
 		boolean needsComp() {
 			Iterator ifiles = files.iterator();
 			while (ifiles.hasNext()) {
 				String file = (String) ifiles.next();
 
 				File javaF = new File(jasonID.projectDirectory
 						+ File.separatorChar + file + ".java");
 				File classF = new File(jasonID.projectDirectory
 						+ File.separatorChar + file + ".class");
 				//System.out.println(classF.lastModified()+" > "+javaF.lastModified());
 				if (javaF.exists() && !classF.exists()) {
 					return true;
 				} else if (javaF.exists() && classF.exists()) {
 					if (classF.lastModified() < javaF.lastModified()) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 	}
 
 	class MASRunner extends Thread {
 		CompileThread compT;
 		protected boolean stop = false;
 		boolean stopOnProcessExit = true;
 		
 		Process masProcess = null;
 		OutputStream processOut;
 
 		MASRunner(CompileThread t) {
 			super("MASRunner");
 			compT = t;
 		}
 
 		void stopRunner() {
 			if (masProcess != null) {
 				masProcess.destroy();
 			}
 			stop = true;
 		}
 
 		public void run() {
 			try {
 				if (compT != null) {
 					if (!compT.waitCompilation()) {
 						return;
 					}
 				}
 				String command = getAsScriptCommand(jasonID.fMAS2jThread.fCurrentProject.getSocName());
 				System.out.println("Executing MAS with " + command);
 				masProcess = Runtime.getRuntime().exec(command, null,
 						new File(jasonID.projectDirectory));
 
 				BufferedReader in = new BufferedReader(new InputStreamReader(masProcess.getInputStream()));
 				BufferedReader err = new BufferedReader(new InputStreamReader(	masProcess.getErrorStream()));
 				processOut = masProcess.getOutputStream();
 
 				jasonID.stopMASButton.setEnabled(true);
 				
 				sleep(500);
 				stop = false;
 				while (!stop) {// || saciProcess!=null) {
 					while (!stop && in.ready()) {
 						System.out.println(in.readLine());
 					}
 					while (!stop && err.ready()) {
 						System.out.println(err.readLine());
 					}
 					sleep(250); // to not consume cpu
 					
 					if (stopOnProcessExit) {
 						try {
 							masProcess.exitValue();
 							// no exception when the process has finished
 							stop = true;
 						} catch (Exception e) {}
 					}
 				}
 				
 			} catch (Exception e) {
 				System.err.println("Execution error: " + e);
 				e.printStackTrace();
 			} finally {
 				jasonID.runMASButton.setEnabled(true);
 				jasonID.debugMASButton.setEnabled(true);
 				jasonID.stopMASButton.setEnabled(false);				
 			}
 		}
 	}
 	
 	class MASRunnerCentralised extends MASRunner {
 
 		MASRunnerCentralised(CompileThread t) {
 			super(t);
 		}
 
 		void stopRunner() {
 			try {
 				processOut.write(1);//"quit"+System.getProperty("line.separator"));
 			} catch (Exception e) {
 				System.err.println("Execution error: " + e);
 				e.printStackTrace();
 			}
 
 			super.stopRunner();
 		}
 	}
 
 	
 	class MASRunnerInsideJIDE extends MASRunner {
 		Method getRunnerMethod;
 		Method finishMethod;
 		
 		MASRunnerInsideJIDE(CompileThread t) {
 			super(t);
 		}
 
 		void stopRunner() {
 			try {
 				//RunCentralisedMAS.getRunner().finish();
 				finishMethod.invoke(getRunnerMethod.invoke(null,null),null);
 			} catch (Exception e) {
 				System.err.println("Execution error: " + e);
 				e.printStackTrace();
 			}
 
 			super.stopRunner();
 		}
 
 		public void run() {
 			try {
 				if (compT != null) {
 					if (!compT.waitCompilation()) {
 						return;
 					}
 				}
 				
 				File fXML = new File(jasonID.projectDirectory + File.separator + jasonID.fMAS2jThread.fCurrentProject.getSocName()+".xml");
 				System.out.println("Running MAS with "+fXML.getAbsolutePath());
 				// create a new RunCentralisedMAS (using my class loader to not cache user classes and to find user project directory)
 				Class rmas = new MASClassLoader(jasonID.projectDirectory).loadClass(RunCentralisedMAS.class.getName());
 				Class[] classParameters = { (new String[2]).getClass() };
 				Method executeMethod = rmas.getDeclaredMethod("main", classParameters);
 				classParameters = new Class[0];
 				getRunnerMethod = rmas.getDeclaredMethod("getRunner", classParameters);
 				finishMethod = rmas.getDeclaredMethod("finish", classParameters);
 				
 				Object objectParameters[] = { new String[] {fXML.getAbsolutePath(), "insideJIDE"} };
 				// Static method, no instance needed
 				executeMethod.invoke(null, objectParameters);
 				//((RunCentralisedMAS)rmas.newInstance()).main(new String[] {fXML.getAbsolutePath(), "insideJIDE"});
 
 				jasonID.stopMASButton.setEnabled(true);
 				
 				stop = false;
 				while (!stop) {
 					sleep(250); // to not consume cpu
 					//if (RunCentralisedMAS.getRunner() == null) {
 					if (getRunnerMethod.invoke(null, null) == null) {
 						stop = true;
 					}
 				}
 			} catch (Exception e) {
 				System.err.println("Execution error: " + e);
 				e.printStackTrace();
 			} finally {
 				jasonID.runMASButton.setEnabled(true);
 				jasonID.debugMASButton.setEnabled(true);
 				jasonID.stopMASButton.setEnabled(false);				
 			}
 		}
 	}
 	
 	class MASRunnerSaci extends MASRunner {
 		StartSaci saciThread;
 		Launcher l;
 
 		boolean iHaveStartedSaci = false;
 		
 		MASRunnerSaci(CompileThread t) {
 			super(t);
 			stopOnProcessExit = false;
 		}
 
 		void stopRunner() {
 			try {
 				String socName = jasonID.fMAS2jThread.fCurrentProject.getSocName();
 				if (l != null) {
 					l.killFacilitatorAgs(socName);
 					l.killFacilitator(socName);
 					l.killFacilitatorAgs(socName + "-env");
 					l.killFacilitator(socName + "-env");
 				}
 			} catch (Exception e) {
 				System.err.println("Execution error: " + e);
 				e.printStackTrace();
 			}
 			if (iHaveStartedSaci) {
 				saciThread.stopSaci();
 			}
 			super.stopRunner();
 		}
 
 		public void run() {
 			saciThread = new StartSaci();
 			l = saciThread.getLauncher();
 			if (l == null) { // no saci running, start one
 				saciThread.start();
 				if (!saciThread.waitSaciOk()) {
 					return;
 				}
 				iHaveStartedSaci = true;
 			}
 			l = saciThread.getLauncher();
 
 			super.run();
 		}
 	}
 
 	
 	String getAsScriptCommand(String scriptName) {
 		return getAsScriptCommand(scriptName, false); 
 	}
 	String getAsScriptCommand(String scriptName, boolean start) {
 		if (System.getProperty("os.name").indexOf("indows") > 0) {
 			//command = "command.com /e:2048 /c "+command+".bat";
 			String sStart = "";
 			if (start) {
 				sStart = " start "; 
 			}
 			return "cmd /c " + sStart + scriptName + ".bat";
 		} else {
 			return "/bin/sh " + scriptName + ".sh";
 		}
 	}
 	
 	
 	class MASClassLoader extends ClassLoader {
 		
 		String MASDirectory;
 		JarFile jf;
 		List libDirJars = new ArrayList();
 		
 		MASClassLoader(String masDir) {
 			MASDirectory = masDir;
 			try {
 				jf = new JarFile(jasonID.getConf().getProperty("jasonJar"));
 				
 				// create the jars from application lib directory
 				if (MASDirectory != null) {
 					File lib = new File(MASDirectory + File.separator + "lib");
 					// add all jar files in lib dir
 					if (lib.exists()) {
 						File[] fs = lib.listFiles();
 						for (int i=0; i<fs.length; i++) {
 							if (fs[i].getName().endsWith(".jar")) {
 								libDirJars.add(new JarFile(fs[i].getAbsolutePath()));
 							}
 						}
 					}
 					
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		public Class loadClass(String name) throws ClassNotFoundException {
 			//System.out.println("loadClass " + name);
 			if (! name.equals(MASConsoleGUI.class.getName())) { // let super loader to get MASConsoleGUI, since it must be shared between RunCentMAS and jIDE
 				if (! name.equals(JasonID.class.getName())) { // let super loader to get MASConsoleGUI, since it must be shared between RunCentMAS and jIDE
 					if (name.startsWith("jason.") || name.startsWith("jIDE.")) {
 						//System.out.println("loading new ");
 						Class c = findClassInJar(jf, name);
 						if (c == null) {
 							System.out.println("does not find class "+name+" in jason.jar");
 						} else {
 							return c;
 						}
 					}
 				}
 			}
 			return super.loadClass(name);
 		}
 
 		public Class findClass(String name) {
 			byte[] b = findClassBytes(MASDirectory, name);
 			if (b != null) {
 				//System.out.println(name + " found ");
 				return defineClass(name, b, 0, b.length);
 			} else {
 				// try in lib dir
 				Iterator i = libDirJars.iterator();
 				while (i.hasNext()) {
 					JarFile j = (JarFile)i.next();
 					Class c = findClassInJar(j, name);
 					if (c != null) {
 						return c;
 					}
 				}
 				System.err.println("Error loading class "+name);
 				return null;
 			}
 		}
 
 		public byte[] findClassBytes(String dir, String className) {
 			try {
 				String pathName = dir + File.separatorChar	+ className.replace('.', File.separatorChar) + ".class";
 				FileInputStream inFile = new FileInputStream(pathName);
 				byte[] classBytes = new byte[inFile.available()];
 				inFile.read(classBytes);
 				return classBytes;
 			} catch (java.io.IOException ioEx) {
 				//ioEx.printStackTrace();
 				return null;
 			}
 		}
 
 		public Class findClassInJar(JarFile jf, String className) {
 			try {
 				if (jf == null) 
 					return null;
 				JarEntry je = jf.getJarEntry(className.replace('.', '/') + ".class"); // must be '/' since inside jar / is used not \
 				if (je == null) {
 					return null;
 				}
 				InputStream in = new BufferedInputStream(jf.getInputStream(je));
 				//System.out.println(c.getResource("/"+className.replace('.', File.separatorChar) + ".class"));
 				//InputStream in = new BufferedInputStream(c.getResource("/"+className.replace('.', File.separatorChar) + ".class").openStream());
 				byte[] classBytes = new byte[in.available()];
 				in.read(classBytes);
 				//System.out.println(" found "+className+ " size="+classBytes.length);
 				return defineClass(className, classBytes, 0, classBytes.length);
 			} catch (Exception e) {
 				System.err.println("Error loading class "+className);
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}	
 }
