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
//   Revision 1.21  2005/08/15 13:12:37  jomifred
//   fix a bug that happens when jason.sh do not change the current directory
//
 //   Revision 1.20  2005/08/12 23:29:11  jomifred
 //   support for saci arch in IA createAgent
 //
 //   Revision 1.19  2005/08/12 21:08:23  jomifred
 //   add cvs keywords
 //
 //----------------------------------------------------------------------------
 
 package jIDE;
 
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 
 public class JasonID {
     
     JFrame frame = null;
 	JMenuBar  menuBar;
     JTextArea output;
     JTabbedPane tab;
     JToolBar  toolBar;
     //StatusBar status;
     JButton runMASButton;
     JButton debugMASButton;
     JButton stopMASButton;
     
     MAS2JParserThread fMAS2jThread;
     ASParserThread    fASParser;
 
     MAS2JEditorPane mas2jPane;
     
     String projectDirectory = "";
 
     static Properties userProperties = new Properties();
     
     PrintStream originalOut;
     PrintStream originalErr;
     
     AbstractAction newAct;
     OpenProject    openAct;
     Save           saveAct;
     AbstractAction saveAsAct;
 	SaveAll        saveAllAct;
     RunMAS         runMASAct;
     DebugMAS       debugMASAct;
     AbstractAction stopMASAct;
     AbstractAction editLogAct;
     AbstractAction exitAppAct;
 
     
     // --------------------------------
     // static start up methods
     // --------------------------------
     
     public static JasonID currentJasonID = null;
     
     public static void main(String[] args) {
         currentJasonID = new JasonID();
         currentJasonID.createMainFrame();
 
         String currJasonVersion;
 
         try {
             Properties p = new Properties();
             p.load(JasonID.class.getResource("/dist.properties").openStream());
             currJasonVersion = p.getProperty("version") + "." + p.getProperty("release");
         } catch (Exception ex) { 
         	currJasonVersion = "?";
         }
         
         try {
             boolean isJWS = false;
             if (System.getProperty("jnlpx.deployment.user.home") != null || System.getProperty("deployment.user.security.trusted.certs") != null) {
             	isJWS = true;
             	currJasonVersion += "-JWS";
             }
             
     		// try to load properties from a user preferences file
         	File jasonConfFile = getUserConfFile();
         	if (jasonConfFile.exists()) {
         		userProperties.load(new FileInputStream(jasonConfFile));
         		if (!userProperties.getProperty("version").equals(currJasonVersion) && !currJasonVersion.equals("?")) { 
         			// new version, set all values to default
         			System.out.println("This is a new version of Jason, reseting configuration...");
         			userProperties.remove("javaHome");
         			userProperties.remove("saciJar");
         			userProperties.remove("jasonJar");
         			userProperties.remove("log4jJar");
         		}
         	} 
 
         	tryToFixJarFileConf("jasonJar", "jason.jar", 300000);
         	tryToFixJarFileConf("saciJar",  "saci.jar",  300000);
         	tryToFixJarFileConf("log4jJar", "log4j.jar", 350000);
 			
             if (userProperties.get("javaHome") == null || !checkJavaPath(userProperties.getProperty("javaHome"))) {
             	String javaHome = System.getProperty("java.home");
             	if (checkJavaPath(javaHome)) {
             		userProperties.put("javaHome", javaHome);
             	} else {
             		userProperties.put("javaHome", File.separator);            		
             	}
             }
 
             if (userProperties.get("font") == null) {
             	userProperties.put("font", "Monospaced");
             }
             if (userProperties.get("fontSize") == null) {
             	userProperties.put("fontSize", "14");
             }
         
             userProperties.put("version", currJasonVersion);
             
             // JWS does not work with class loaders, so do not use run inside
             if (isJWS) {
             	userProperties.put("runCentralisedInsideJIDE", "false");
             } else if (userProperties.get("runCentralisedInsideJIDE") == null) {
             	userProperties.put("runCentralisedInsideJIDE", "true");
             }
             
             jasonConfFile.getParentFile().mkdirs();
             storePrefs();
 
             currentJasonID.mas2jPane.updateFont();
 
             if (args.length > 0) {
             	currentJasonID.openAct.loadProject(new File(args[0]));
             } else {
             	currentJasonID.mas2jPane.createNewPlainText("// use menu option Project->New to create a new project.");//jasonID.mas2jPane.getDefaultText("anMAS", ""));
             	currentJasonID.mas2jPane.modified = false;
             }
             currentJasonID.startThreads();
 
         } catch (Throwable t) {
             System.out.println("uncaught exception: " + t);
             t.printStackTrace();
         }
     }
 
     public static File getUserConfFile() {
     	return new File(System.getProperties().get("user.home") + File.separator + ".jason/user.properties");
     }
     
     public static void storePrefs() {
     	try {
     		userProperties.store(new FileOutputStream(getUserConfFile()), "Jason user configuration");
     	} catch (Exception e) {
     		System.err.println("Error writting preferences");
     		e.printStackTrace();
     	}
     }
     
     public Properties getConf() {
     	return userProperties;
     }
 
     static void tryToFixJarFileConf(String jarEntry, String jarName, int minSize) {
     	String jarFile = userProperties.getProperty(jarEntry);
         if (jarFile == null || !checkJar(jarFile)) {
         	System.out.println("Wrong configuration for "+jarName+", current is "+jarFile);
 
         	// try to get from classpath
         	jarFile = getPathFromClassPath(jarName);
         	if (checkJar(jarFile)) {
         		userProperties.put(jarEntry, jarFile);
     			System.out.println("found at "+jarFile);
     			return;
         	} 
     		
         	// try current dir
         	jarFile = "."+File.separator+jarName;
     		if (checkJar(jarFile)) {
         		userProperties.put(jarEntry, new File(jarFile).getAbsolutePath());
     			System.out.println("found at "+jarFile);
     			return;
     		}
 
         	// try current dir + lib
     		jarFile = ".."+File.separator+"lib"+File.separator+jarName;
     		if (checkJar(jarFile)) {
         		userProperties.put(jarEntry, new File(jarFile).getAbsolutePath());
     			System.out.println("found at "+jarFile);
     			return;
     		}
     		
     		// try from java web start
     		String jwsDir = System.getProperty("jnlpx.deployment.user.home");
     		if (jwsDir == null) {
     			// try another property (windows)
     			try {
     				jwsDir = System.getProperty("deployment.user.security.trusted.certs");
     				jwsDir = new File(jwsDir).getParentFile().getParent();
     			} catch (Exception e) {}
     		}
     		if (jwsDir != null) {
         		jarFile = findFile(new File(jwsDir), jarName, minSize);
         		System.out.print("Searching "+jarName+" in "+jwsDir+" ... ");
         		if (jarFile != null && checkJar(jarFile)) {
         			System.out.println("found at "+jarFile);
         			userProperties.put(jarEntry, jarFile);            			
         		} else {
         			userProperties.put(jarEntry, File.separator);
         		}
     		}
         }
     	
     }
     
     static String findFile(File p, String file, int minSize) {
     	if (p.isDirectory()) {
     		File[] files = p.listFiles();
     		for (int i=0; i<files.length; i++) {
     			if (files[i].isDirectory()) {
     				String r = findFile(files[i], file, minSize);
     				if (r != null) {
     					return r;
     				}
     			} else {
     				if (files[i].getName().endsWith(file) && files[i].length() > minSize) {
     					return files[i].getAbsolutePath();
     				}
     			}
     		}
     	}
     	return null;
     }
     
 	static boolean checkJar(String jar) {
         try {
         	if (jar != null && new File(jar).exists() && jar.endsWith(".jar")) {
         		return true;
             }
         } catch (Exception e) {}
         return false;
     }
     
     static boolean checkJavaPath(String javaHome) {
         try {
         	if (!javaHome.endsWith(File.separator)) {
         		javaHome += File.separator;
         	}
             File javac1 = new File(javaHome + "bin" + File.separatorChar + "javac");
             File javac2 = new File(javaHome + "bin" + File.separatorChar + "javac.exe");
             if (javac1.exists() || javac2.exists()) {
         		return true;
             }
         } catch (Exception e) {}
         return false;
     }
 	static String getPathFromClassPath(String file) {
 		StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
 		while (st.hasMoreTokens()) {
 			String token = st.nextToken();
 			if (token.endsWith(file)) {
 				return new File(token).getAbsolutePath();
 			}
 		}
 		return null;
  	}
 
     // --------------------------------
     // non-static methods
     // --------------------------------
     
     public JasonID() {
     	mas2jPane = new MAS2JEditorPane(this);
     	
 		newAct      = new NewProject();
 	    openAct     = new OpenProject();
 	    saveAct     = new Save();
 	    saveAsAct   = new SaveAs();
 		saveAllAct  = new SaveAll();
 		runMASAct   = new RunMAS(this);
         debugMASAct = new DebugMAS(this);
 	    stopMASAct  = new StopMAS();
 	    editLogAct  = new EditLog();
 	    exitAppAct  = new ExitApp();
     }
     
     public String getProjectDirectory() {
     	return projectDirectory;
     }
     
     JFrame createMainFrame() {
         frame = new JFrame();
         frame.setTitle("Jason");
         //frame.setBackground(Color.lightGray);
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 exitAppAct.actionPerformed(null);
             }
         });
         
         //setBorder(BorderFactory.createEtchedBorder());
         frame.getContentPane().setLayout(new BorderLayout());
         frame.getContentPane().add(BorderLayout.NORTH, createMenuBar());
         
         tab = new JTabbedPane();
         tab.add("project", mas2jPane);
         
         int height = 440;
         
         JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
         split.setOneTouchExpandable(true);
         split.setTopComponent(tab);
         split.setBottomComponent(createOutput());
         
         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         panel.add(BorderLayout.NORTH, createToolBar());
         panel.add(BorderLayout.CENTER, split);
         frame.getContentPane().add(BorderLayout.CENTER, panel);
         //frame.getContentPane().add(BorderLayout.SOUTH, createStatusBar());
         
         frame.pack();
         frame.setSize((int)(height * 1.618), height);
         split.setDividerLocation(height-200);
         frame.setVisible(true);
         
         return frame;
     }
 
     void startThreads() {
         fMAS2jThread = new MAS2JParserThread( mas2jPane, this);
         fMAS2jThread.start();
         fASParser = new ASParserThread( this );
         fASParser.start();
     }
     
     void stopThreads() {
     	if (fMAS2jThread != null) {
     		fMAS2jThread.stopParser();
         	while (fMAS2jThread.isAlive()) { // wait it ends
         		try { Thread.sleep(10); } catch (Exception e) {}
         	}
     	}
     	if (fASParser != null) {
     		fASParser.stopParser();
         	while (fASParser.isAlive()) { // wait it ends
         		try { Thread.sleep(10); } catch (Exception e) {}
         	}
     	}
     }
     
     protected boolean checkNeedsSave() {
         for (int i = 0; i<tab.getComponentCount(); i++) {
             if (! checkNeedsSave(i)) {
                 return false;
             }
         }
         return true;
     }
     
     protected boolean checkNeedsSave(int indexTab) {
         ASEditorPane pane = (ASEditorPane)tab.getComponentAt(indexTab);
         if (pane.modified) {
             tab.setSelectedIndex(indexTab);
             int op = JOptionPane.showConfirmDialog(this.frame, "Do you want to save "+pane.getFileName()+"?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
             if (op == JOptionPane.YES_OPTION) {
                 saveAct.actionPerformed(null);
             } else if (op == JOptionPane.CANCEL_OPTION) {
                 return false;
             }
         }
         return true;
     }
     
     void openAllASFiles(Collection files) {
         // remove files not used anymore
         for (int i = 1; i<tab.getComponentCount(); i++) {
         	ASEditorPane pane = (ASEditorPane) tab.getComponent(i);
             boolean isInFiles = false;
             Iterator iFiles = files.iterator();
             while (iFiles.hasNext()) {
                 String sFile = pane.removeExtension(iFiles.next().toString());
                 if (tab.getTitleAt(i).startsWith(sFile)) {
                     isInFiles = true;
                     break;
                 }
             }
             if (!isInFiles && !tab.getTitleAt(i).startsWith(RunCentralisedMAS.logPropFile)) {
                 if (checkNeedsSave(i)) {
                 	//System.out.println("removing "+tab.getTitleAt(i));
                     tab.remove(i);
                     i--;
                 }
             }
         }
         
         Iterator iFiles = files.iterator();
         while (iFiles.hasNext()) {
             File file = new File(iFiles.next().toString());
             boolean isInTab = false;
             for (int i = 1; i<tab.getComponentCount(); i++) {
             	ASEditorPane pane = (ASEditorPane) tab.getComponent(i);
             	String sFile = pane.removeExtension(file.toString());
             	if (tab.getTitleAt(i).startsWith(sFile)) {
                     isInTab = true;
                     break;
                 }
             }
             if (!isInTab) {
             	//System.out.println(" not in tab "+file);
                 int tabIndex = tab.getComponentCount();
                 ASEditorPane newPane = new ASEditorPane(this, tabIndex);
                 newPane.setFileName(file);
                 openAct.load(file, newPane);
                 tab.add(newPane.getFileName(), newPane);
                 updateTabTitle(tabIndex, newPane, null);
             }
         }
         if (fASParser != null) {
         	fASParser.stopWaiting();
         }
     }
     
     /**
      * Create a status bar
      */
     /*
     protected Component createStatusBar() {
         // need to do something reasonable here
         status = new StatusBar();
         return status;
     }
     */
     
     protected Component createOutput() {
         output = new JTextArea();
         output.setEditable(false);
         //output.setEnabled(false);
         JScrollPane scroller = new JScrollPane(output);
         MyOutputStream out = new MyOutputStream();
         originalOut = System.out;
         originalErr = System.err;
         System.setOut(out);
         System.setErr(out);
         return scroller;
     }
     
     
     class MyOutputStream extends java.io.PrintStream {
         MyOutputStream() {
             super(System.out);
         }
         public void print(String s) {
 			output.append(s);
 			output.setCaretPosition(output.getDocument().getLength());
         }
         public void println(String s) {
 			output.append(s+"\n");
 			output.setCaretPosition(output.getDocument().getLength());
         }
     }
     
     
     protected JToolBar createToolBar() {
         toolBar = new JToolBar();
         createToolBarButton(newAct, "Start new project");
         createToolBarButton(openAct, "Open project");
         createToolBarButton(saveAct, "Save project");
         toolBar.addSeparator();
         runMASButton = createToolBarButton(runMASAct, "Run MAS");
         debugMASButton = createToolBarButton(debugMASAct, "Debug MAS");
         stopMASButton = createToolBarButton(stopMASAct, "Stop MAS");
         stopMASButton.setEnabled(false);
         return toolBar;
     }
     
     protected JMenuBar createMenuBar() {
         
         JMenu jMenuProject = new JMenu("Project");
         jMenuProject.setMnemonic('P');
         
         JMenuItem item = jMenuProject.add(newAct);
         item.setMnemonic('N');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));        
         
         item = jMenuProject.add(openAct);
         item.setMnemonic('O');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));        
         
         item = jMenuProject.add(saveAct);
         item.setMnemonic('S');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));        
         
         jMenuProject.add(saveAsAct).setMnemonic('a');
         jMenuProject.add(saveAllAct).setMnemonic('l');
         
         jMenuProject.addSeparator();
         
         item = jMenuProject.add(runMASAct);
         item.setMnemonic('R');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));        
         
         jMenuProject.add(debugMASAct).setMnemonic('D');
         jMenuProject.add(stopMASAct).setMnemonic('t');
         
         jMenuProject.addSeparator();
         
         item = jMenuProject.add(exitAppAct);
         //item.setMnemonic('q');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));        
         
 
         JMenu jMenuEdit = new JMenu("Edit");
         jMenuEdit.setMnemonic('E');
 
         item = jMenuEdit.add(new AbstractAction("Undo") { 
             public void actionPerformed(ActionEvent e) {
 				try {
 	            	((ASEditorPane)tab.getSelectedComponent()).undo.undo();
 				} catch (CannotUndoException cue) {
 					Toolkit.getDefaultToolkit().beep();
 				}
             }
          });
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));        
 
         item = jMenuEdit.add(new AbstractAction("Redo") { 
             public void actionPerformed(ActionEvent e) {
 				try {
 	            	((ASEditorPane)tab.getSelectedComponent()).undo.redo();
 				} catch (CannotRedoException cue) {
 					Toolkit.getDefaultToolkit().beep();
 				}            }
          });
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));        
         
         item = jMenuEdit.add(new AbstractAction("Cut") { 
             public void actionPerformed(ActionEvent e) {
             	((ASEditorPane)tab.getSelectedComponent()).editor.cut();
             }
          });
         //item.setMnemonic('x');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));        
 
         item = jMenuEdit.add(new AbstractAction("Copy") { 
             public void actionPerformed(ActionEvent e) {
             	((ASEditorPane)tab.getSelectedComponent()).editor.copy();
             }
          });
         item.setMnemonic('c');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));        
         
         item = jMenuEdit.add(new AbstractAction("Paste") { 
             public void actionPerformed(ActionEvent e) {
             	((ASEditorPane)tab.getSelectedComponent()).editor.paste();
             }
          });
         //item.setMnemonic('x');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));        
         
         jMenuEdit.addSeparator();
         
         item = jMenuEdit.add(new AbstractAction("Find...") { 
             public void actionPerformed(ActionEvent e) {
                 ((ASEditorPane)tab.getSelectedComponent()).askSearch();
             }
          });
         item.setMnemonic('f');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));        
         
         item = jMenuEdit.add(new AbstractAction("Find next") { 
             public void actionPerformed(ActionEvent e) {
                 ((ASEditorPane)tab.getSelectedComponent()).search();
             }
          });
         item.setMnemonic('n');
         item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));        
         
         jMenuEdit.addSeparator();
         jMenuEdit.add(editLogAct).setMnemonic('l');
         jMenuEdit.add(new EditPreferences()).setMnemonic('p');
         
         
         JMenu jMenuHelp = new JMenu("Help");
         jMenuHelp.setMnemonic('H');
         jMenuHelp.add(new HelpAbout()).setMnemonic('A');
         
         menuBar = new JMenuBar();
         menuBar.add(jMenuProject);
         menuBar.add(jMenuEdit);
         menuBar.add(jMenuHelp);
         return menuBar;
     }
     
     protected JButton createToolBarButton(Action act, String toolTip) {
         JButton button;
         button = toolBar.add(act);
         button.setRequestFocusEnabled(false);
         button.setMargin(new Insets(1,1,1,1));
         button.setToolTipText(toolTip);
         return button;
     }
     
     
     protected void updateTabTitle(int index, ASEditorPane pane, String error) {
         String title = "";
         if (pane.getFileName().length() > 0) {
             title = pane.getFileName() + "." + pane.extension;
         }
         if (pane.modified) {
             title += " [*]";
         }
         if (error != null) {
             title += " "+error;
         }
         if (index < tab.getTabCount()) {
             tab.setTitleAt(index, title);
         }
     }
     
     /*
     class StatusBar extends JPanel {
         //JLabel text = new JLabel();
         JProgressBar progress = new JProgressBar();
         
         public StatusBar() {
             super();
             setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
             //add(text);
             //add(Box.createHorizontalStrut(5));
             add(progress);
         }
     */    
         /*
         public void updateText() {
             String status = "";
             if (projectName.length() > 0) {
                 status = projectDirectory+File.separatorChar+projectName;
             }
             if (modified) {
                 status += " [*]";
             }
             text.setText(status);
         }
          */
     //}
     
     //
     //
     //     Project menu (file part)
     //     -----------------------------
     //
     
     class NewProject extends AbstractAction {
         JFileChooser chooser;
 
     	NewProject() {
             super("New project", new ImageIcon( JasonID.class.getResource("/images/new.gif")));
             chooser = new JFileChooser(System.getProperty("user.dir"));
             //chooser.setFileFilter(new DirectoryFileFilter());
             chooser.setDialogTitle("Select the project folder");
             chooser.setAcceptAllFileFilterUsed(false);
             chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         }
         
         public void actionPerformed(ActionEvent e) {
             if (checkNeedsSave()) {
             	editLogAct.setEnabled(true);
                 String tmpFileName = JOptionPane.showInputDialog("What is the new project name?");
 
                 if (tmpFileName == null) {
                 	return;
                 }
                 if (Character.isUpperCase(tmpFileName.charAt(0))) {
                 	tmpFileName = Character.toLowerCase(tmpFileName.charAt(0)) + tmpFileName.substring(1);
                 }
                 if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 					File f = chooser.getSelectedFile();
 					if (f.isDirectory()) {
 						projectDirectory = f.getAbsolutePath();
 					}
 				} else {
 					return;
 				}
                 stopThreads();
                 
 	            mas2jPane = new MAS2JEditorPane(JasonID.this);
                 mas2jPane.setFileName(tmpFileName);
                 mas2jPane.createNewPlainText(mas2jPane.getDefaultText(tmpFileName));
                 mas2jPane.modified = true;
                 mas2jPane.needsParsing = true;
 
 	            tab.removeAll();
                 tab.add("Project",  mas2jPane);
                 updateTabTitle(0, mas2jPane, null);
                     
                 ASEditorPane newPane = new ASEditorPane(JasonID.this, 1);
                 newPane.setFileName("ag1.asl");
                 newPane.modified = true;
                 newPane.needsParsing = true;
                 tab.add("new", newPane);
                 newPane.createNewPlainText(newPane.getDefaultText("auto code"));
                 updateTabTitle(1, newPane, null);
 
                 startThreads(); // to read from the new mas2j                
             }
         }
     }
     
     
     class OpenProject extends AbstractAction {
         JFileChooser chooser;
         OpenProject() {
             super("Open project", new ImageIcon(JasonID.class.getResource("/images/openProject.gif")));
             chooser = new JFileChooser(System.getProperty("user.dir"));
             chooser.setFileFilter(new JasonFileFilter());
         }
         
         public void actionPerformed(ActionEvent e) {
             if (checkNeedsSave()) {
                 if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                     File f = chooser.getSelectedFile();
                     if (f.isFile()) {
                     	editLogAct.setEnabled(true);
                     	runMASButton.setEnabled(false);
                     	debugMASButton.setEnabled(false);
                        loadProject(f);
                     }
                 }
             }
         }
         
         
         void loadProject(File f) {
             try {
                projectDirectory = f.getAbsoluteFile().getParentFile().getCanonicalPath();
             } catch (Exception e) {
                 e.printStackTrace();
             }
             stopThreads();
             output.setText("");
             mas2jPane = new MAS2JEditorPane(JasonID.this);
             mas2jPane.setFileName(f);
             tab.removeAll();
             tab.add(f.getName(), mas2jPane);
             load(f, mas2jPane);
         	updateTabTitle(0, mas2jPane, null);            
             startThreads();
             fMAS2jThread.waitCompilation();
             fMAS2jThread.stopWaiting();
         }
         
         void load(File f, ASEditorPane pane) {
             try {
                 pane.createNewPlainText("");
                 pane.needsParsing = false;
                 Document doc = pane.editor.getDocument();
                 //status.progress.setMinimum(0);
                 //status.progress.setMaximum((int) f.length());
                 // try to start reading
                 java.io.Reader in = new java.io.FileReader(f);
                 char[] buff = new char[1024];
                 int nch;
                 while ((nch = in.read(buff, 0, buff.length)) != -1) {
                     doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
                     //status.progress.setValue(status.progress.getValue() + nch);
                 }
                 //status.progress.setValue(0);
                 pane.needsParsing = true;
                 pane.undo.discardAllEdits();
                 pane.syntaxHL.repainAll();
                 pane.modified = false;
             } catch (FileNotFoundException ex) {
 				System.err.println("File "+f+" does not exists; it will be created!");
 			 } catch (java.io.IOException e) {
             	System.err.println("I/O error for "+f+" -- "+e.getMessage());
                 e.printStackTrace();
             } catch (BadLocationException e) {
             	System.err.println("BadLocationException error for "+f+" -- "+e.getMessage());
                 e.printStackTrace();
             } finally {
             	runMASButton.setEnabled(true);
             	debugMASButton.setEnabled(true);
             }
         }
     }
     
     
     class Save extends AbstractAction {
         Save() {
             super("Save", new ImageIcon(JasonID.class.getResource("/images/save.gif")));
         }
         
         public void actionPerformed(ActionEvent e) {
             savePane(tab.getSelectedIndex(), (ASEditorPane)tab.getSelectedComponent());
         }
 		
 		public void savePane(int index, ASEditorPane pane) {
             if (pane.getFileName().length() == 0) {
                 saveAsAct.actionPerformed(null);
             } else {
                 File f = new File(projectDirectory+File.separatorChar+pane.getFileName()+"."+pane.extension);
                 output.append("Saving to "+f.getPath()+"\n");
                 try {
                     Document doc  = pane.editor.getDocument();
                     String text = doc.getText(0, doc.getLength());
                     //status.progress.setMinimum(0);
                     //status.progress.setMaximum(doc.getLength());
                     java.io.Writer out = new java.io.FileWriter(f);
                     for (int i = 0; i < doc.getLength(); i++) {
                         out.write(text.charAt(i));
                         //status.progress.setValue(i);
                     }
                     out.close();
                     //status.progress.setValue(0);
                 } catch (java.io.IOException ex) {
                     ex.printStackTrace();
                 } catch (BadLocationException ex) {
                     ex.printStackTrace();
                 }
                 pane.modified = false;
                 updateTabTitle(index, pane, null);
             }
         }
     }
 
     class SaveAll extends AbstractAction {
         SaveAll() {
             super("Save all", new ImageIcon(JasonID.class.getResource("/images/save.gif")));
         }
         
         public void actionPerformed(ActionEvent e) {
 	        for (int i = 0; i<tab.getComponentCount(); i++) {
 				ASEditorPane pane = (ASEditorPane)tab.getComponentAt(i);
 		        if (pane.modified) {
 					saveAct.savePane(i,pane);
 		        }
 			}
         }
     }
 	
 	
     class SaveAs extends AbstractAction {
         JFileChooser chooser;
         SaveAs() {
             super("Save as ...", new ImageIcon(JasonID.class.getResource("/images/save.gif")));
             chooser = new JFileChooser(System.getProperty("user.dir"));
             chooser.setFileFilter(new JasonFileFilter());
         }
         
         public void actionPerformed(ActionEvent e) {
             if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                 ASEditorPane pane = (ASEditorPane)tab.getSelectedComponent();
                 File f = chooser.getSelectedFile();
                 if (! f.getName().toLowerCase().endsWith(pane.extension)) {
                     f = new File(f.getPath()+"."+pane.extension);
                 }
                 if (f.getName().toLowerCase().endsWith(pane.extension)) {
                     projectDirectory = f.getParentFile().getPath();
                 }
                 
                 pane.setFileName(f);
                 saveAct.actionPerformed(e);
             }
         }
     }
     
     //
     //
     //     Project menu (execuion part)
     //     -----------------------------
     //
     
     class StopMAS extends AbstractAction {
         StopMAS() {
             super("Stop MAS", new ImageIcon(JasonID.class.getResource("/images/suspend.gif")));
         }
         
         public void actionPerformed(ActionEvent e) {
             runMASAct.stopMAS();
         }
     }
     
     class ExitApp extends AbstractAction {
         ExitApp() {
             super("Exit");
         }
         
         public void actionPerformed(ActionEvent e) {
             if (checkNeedsSave()) {
                 runMASAct.exitJason();
                 System.exit(0);
             }
         }
     }
 
     class EditLog extends AbstractAction {
         EditLog() {
             super("Edit log configuration");
         }
         
         public void actionPerformed(ActionEvent e) {
         	this.setEnabled(false);
             ASEditorPane newPane = new ASEditorPane(JasonID.this, tab.getComponentCount());
             newPane.setFileName(RunCentralisedMAS.logPropFile);
             tab.add("log4j", newPane);
             try {
             	InputStream in = JasonID.class.getResource("/"+RunCentralisedMAS.logPropFile).openStream();
             	File f = new File(projectDirectory + File.separator + RunCentralisedMAS.logPropFile);
             	if (f.exists()) {
             		in = new FileInputStream(f);
             	}
             	newPane.createNewPlainText(in);
             } catch (Exception ex) {
             	ex.printStackTrace();
             }
             newPane.modified = true;
             newPane.needsParsing = false;
             newPane.extension = "configuration";
             updateTabTitle(tab.getComponentCount(), newPane, null);
             tab.setSelectedIndex(tab.getComponentCount()-1);
         }
     }
 
     
     class EditPreferences extends AbstractAction {
     	JDialog d = null;
     	JTextField saciTF;
     	JTextField jasonTF;
     	JTextField javaTF;
     	JCheckBox  insideJIDECBox;
     	JComboBox jBCFont;
     	JComboBox jBCSize;
     	String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
     	String fontSizes[] = {"8","10","11","12","14","16","18","20","24","30","36","40"};
     	
         EditPreferences() {
             super("Preferences...");
         	d = new JDialog(frame, "Jason Preferences");
         	d.getContentPane().setLayout(new GridLayout(0, 1));
 
         	// jason home
         	JPanel jasonHomePanel = new JPanel();
         	jasonHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
     				.createEtchedBorder(), "jason.jar file", TitledBorder.LEFT, TitledBorder.TOP));
         	jasonHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         	jasonHomePanel.add(new JLabel("Location"));
         	jasonTF = new JTextField(30);
         	jasonHomePanel.add(jasonTF);
         	JButton setJason = new JButton("Browse");
         	setJason.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 		            try {
 						JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
 						chooser.setDialogTitle("Select the jason.jar file");
 						chooser.setFileFilter(new JarFileFilter("jason.jar", "The Jason.jar file"));
 		                //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 		                	String jasonJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
 		                	if (checkJar(jasonJar)) {
 								jasonTF.setText(jasonJar);
 		                	}
 		                }
 		            } catch (Exception e) {}
 				}
         	});
         	jasonHomePanel.add(setJason);
         	d.getContentPane().add(jasonHomePanel);
         	
         	
         	// saci home
         	JPanel saciHomePanel = new JPanel();
         	saciHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
     				.createEtchedBorder(), "saci.jar file", TitledBorder.LEFT, TitledBorder.TOP));
         	saciHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         	saciHomePanel.add(new JLabel("Location"));
         	saciTF = new JTextField(30);
         	saciHomePanel.add(saciTF);
         	JButton setSaci = new JButton("Browse");
         	setSaci.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 		            try {
 		                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
 						chooser.setDialogTitle("Select the Saci.jar file");
 						chooser.setFileFilter(new JarFileFilter("saci.jar", "The Saci.jar file"));
 						//chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 		                	String saciJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
 		                	if (checkJar(saciJar)) {
 		                		saciTF.setText(saciJar);
 		                	}
 		                }
 		            } catch (Exception e) {}
 				}
         	});
         	saciHomePanel.add(setSaci);
         	d.getContentPane().add(saciHomePanel);
 
         	// java home
         	JPanel javaHomePanel = new JPanel();
         	javaHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
     				.createEtchedBorder(), "Java Home", TitledBorder.LEFT, TitledBorder.TOP));
         	javaHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         	javaHomePanel.add(new JLabel("Directory"));
         	javaTF = new JTextField(30);
         	javaHomePanel.add(javaTF);
         	JButton setJava = new JButton("Browse");
         	setJava.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 		            try {
 		                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
 						 chooser.setDialogTitle("Select the Java Home directory");
 		                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 		                	String javaHome = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
 		                	if (checkJavaPath(javaHome)) {
 		                		javaTF.setText(javaHome);
 		                	}
 		                }
 		            } catch (Exception e) {}
 				}
         	});
         	javaHomePanel.add(setJava);
         	d.getContentPane().add(javaHomePanel);
 
         	// run centralised inside jIDE
         	JPanel insideJIDEPanel = new JPanel();
         	insideJIDEPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory	.createEtchedBorder(), "Centralised MAS", TitledBorder.LEFT, TitledBorder.TOP));
         	insideJIDEPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         	insideJIDECBox = new JCheckBox("Run MAS as a JasonIDE inernal thread instead of another process");
         	insideJIDEPanel.add(insideJIDECBox);
         	d.getContentPane().add(insideJIDEPanel);
         	
         	// font
         	JPanel fontPanel = new JPanel();
         	fontPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory	.createEtchedBorder(), "Font", TitledBorder.LEFT, TitledBorder.TOP));
         	fontPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         	jBCFont = new JComboBox(fonts);
         	fontPanel.add(jBCFont);
         	jBCSize = new JComboBox(fontSizes);
         	fontPanel.add(jBCSize);
         	d.getContentPane().add(fontPanel);
         	
         	JPanel btPanel = new JPanel();
         	btPanel.setLayout(new FlowLayout());
         	JButton okBt = new JButton("Ok");
         	btPanel.add(okBt);
         	okBt.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					if (checkJar(saciTF.getText())) {
 						userProperties.put("saciJar", saciTF.getText());
 					}
 					if (checkJar(jasonTF.getText())) {
 						userProperties.put("jasonJar", jasonTF.getText());
 					}
 					if (checkJavaPath(javaTF.getText())) {
 						userProperties.put("javaHome", javaTF.getText());
 					}
 					userProperties.put("font", jBCFont.getSelectedItem());
 					userProperties.put("fontSize", jBCSize.getSelectedItem());
 					
 					userProperties.put("runCentralisedInsideJIDE", insideJIDECBox.isSelected()+"");
 					
 					// update all tabs fonts
 					for (int i=0; i<tab.getComponentCount(); i++) {
 				        ((ASEditorPane)tab.getComponentAt(i)).updateFont();
 					}
 					storePrefs();
 					d.setVisible(false);
 				}
         	});
         	JButton canelBt = new JButton("Cancel");
         	canelBt.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					d.setVisible(false);
 				}
         	});
         	btPanel.add(canelBt);
         	d.getContentPane().add(btPanel);
         	d.pack();
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             d.setLocation((int)( (screenSize.width - d.getWidth()) / 2),(int) ((screenSize.height -d.getHeight())/2));
         }
         
         public void actionPerformed(ActionEvent e) {
         	saciTF.setText(userProperties.getProperty("saciJar"));
         	jasonTF.setText(userProperties.getProperty("jasonJar"));
         	javaTF.setText(userProperties.getProperty("javaHome"));
 
         	// search the current font
         	String curFont = userProperties.getProperty("font");
         	for (int i=0;i<fonts.length; i++) {
         		if (fonts[i].equals(curFont)) {
         			jBCFont.setSelectedIndex(i);
         			break;
         		}
         	}
         	// search the current font
         	String curSize = userProperties.getProperty("fontSize");
         	for (int i=0;i<fontSizes.length; i++) {
         		if (fontSizes[i].equals(curSize)) {
         			jBCSize.setSelectedIndex(i);
         			break;
         		}
         	}
         	
         	if (userProperties.getProperty("runCentralisedInsideJIDE").equals("true")) {
         		insideJIDECBox.setSelected(true);
         	} else {
         		insideJIDECBox.setSelected(false);        		
         	}
         	
         	d.setVisible(true);
         }
     }
 
     
     class HelpAbout extends AbstractAction {
         HelpAbout() {
             super("About...");
         }
         
         // TODO: put the copyright of the image bellow it.
         
         public void actionPerformed(ActionEvent e) {
             String version = "";
             String build = "";
 
             try {
                 Properties p = new Properties();
                 p.load(JasonID.class.getResource("/dist.properties").openStream());
                 version = "Jason " + p.get("version") + "." + p.get("release");
                 build = " build " + p.get("build") + " on " + p.get("build.date") + "\n\n";
             } catch (Exception ex) { }
 
             JOptionPane.showMessageDialog( frame,
             version +  build+
             "Copyright (C) 2003-2005  Rafael H. Bordini, Jomi F. Hubner, et al.\n\n"+
             "This library is free software; you can redistribute it and/or\n"+
             "modify it under the terms of the GNU Lesser General Public\n"+
             "License as published by the Free Software Foundation; either\n"+
             "version 2.1 of the License, or (at your option) any later version.\n\n"+
             "This library is distributed in the hope that it will be useful,\n"+
             "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
             "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
             "GNU Lesser General Public License for more details.\n\n"+
             "You should have received a copy of the GNU Lesser General Public\n"+
             "License along with this library; if not, write to the Free Software\n"+
             "Foundation, Inc., 59 Temple Place, Suite 330,\nBoston, MA  02111-1307  USA\n\n"+
 			"About the image: \"Jason\" by Gustave Moreau (1865).\n"+
 			"Copyright Photo RMN (Agence Photographique de la R�union des\n"+
 			"Mus�es Nationaux, France). Photograph by Herv� Lewandowski.\n\n"+
             "To contact the authors:\n"+
             "http://www.dur.ac.uk/r.bordini\n"+
             "http://www.inf.furb.br/~jomi",
             "JasonID - About",
             JOptionPane.INFORMATION_MESSAGE,
             new ImageIcon(JasonID.class.getResource("/images/Jason-GMoreau-Small.jpg")));
         }
     }
     
     class JasonFileFilter extends FileFilter {
         public boolean accept(File f) {
             if (f.isDirectory()) {
                 return true;
             }
             
             String s = f.getName();
             String ext = null;
             int i = s.lastIndexOf('.');
             if (i > 0 &&  i < s.length() - 1) {
                 ext = s.substring(i+1).toLowerCase();
             }
             if (ext != null) {
                 if (ext.equals(mas2jPane.extension)) {
                     return true;
                 } else {
                     return false;
                 }
             }
             return false;
         }
         
         public String getDescription() {
             return "Jason project files";
         }
     }
 
     class JarFileFilter extends FileFilter {
 		String jar,ds;
 		public JarFileFilter(String jar, String ds) {
 			this.jar = jar;
 			this.ds  = ds;
 		}
         public boolean accept(File f) {
             if (f.getName().endsWith(jar)) {
 				return true;
             } else {
 				return false;
             }
         }
         
         public String getDescription() {
             return ds;
         }
     }
 }
