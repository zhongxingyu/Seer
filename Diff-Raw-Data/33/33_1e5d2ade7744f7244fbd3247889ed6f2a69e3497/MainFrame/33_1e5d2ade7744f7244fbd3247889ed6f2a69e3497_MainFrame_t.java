 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.gui.frames;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.*;
 import javax.swing.text.*;
 
 import net.infonode.docking.*;
 import net.infonode.docking.View; // explicit because of import conflict with javax.swing
 import net.infonode.docking.util.*;
 
 import wombat.Wombat;
 
 import wombat.gui.icons.*;
 import wombat.gui.text.*;
 import wombat.scheme.*;
 import wombat.util.*;
 import wombat.util.errors.*;
 import wombat.util.files.*;
 
 /**
  * Main frame for the program. This does pretty much everything.
  * 
  * TODO: Break this apart a little better. It's overlarge.
  */
 public class MainFrame extends JFrame {
 	private static final long serialVersionUID = 2574330949324570164L;
 
 	// Self-reference for singleton access.
 	static MainFrame Me;
 	static boolean MeBuilding;
 	
 	// Display components.
 	RootWindow Root;
 	Petite Petite;
     StringViewMap ViewMap;
     
     // Toolbar.
     JToolBar ToolBar;
     JButton UpdateButton;
     public JButton ToolBarRun;
     public JButton ToolBarStop;
     public JLabel RowColumn;
 
     // Unique code components.
     NonEditableTextArea History;
     NonEditableTextArea Debug;
     REPLTextArea REPL;
 
     /**
 	 * Singleton access.
 	 * @return The main frame.
 	 */
 	public static MainFrame Singleton() { 
 		if (Me == null && !MeBuilding) {
 			MeBuilding = true;
 			Me = new MainFrame();
 			MeBuilding = false;
 		}
 			
 		return Me; 
 	}
     
     /**
      * Don't directly create this, use me().
      * Use this method to set it up though.
      */
     private MainFrame() {
         // Set frame options.
         setTitle("Wombat - Build " + Wombat.VERSION);
         setSize(Options.DisplayWidth, Options.DisplayHeight);
         setLocation(Options.DisplayLeft, Options.DisplayTop);
         setLayout(new BorderLayout(5, 5));
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         try {
         	setIconImage(IconManager.icon("Wombat.png").getImage());
         } catch(NullPointerException ex) {
         	
         }
         
         // Wait for the program to end.
         final MainFrame me = this;
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
         		Options.DisplayTop = Math.max(0, e.getWindow().getLocation().y);
             	Options.DisplayLeft = Math.max(0, e.getWindow().getLocation().x);
             	Options.DisplayWidth = Math.max(400, e.getWindow().getWidth());
             	Options.DisplayHeight = Math.max(400, e.getWindow().getHeight());
             	Options.save();
             	
             	DocumentManager.CloseAll();
             	stopAllThreads(true, false);
             	
             	me.dispose();
             }
         });
 
         // Set up the menus using the above definitions.
         MenuManager.Singleton(this);
         setJMenuBar(MenuManager.getMenu());
         
         // Create a display for any open documents.
         Root = DockingUtil.createRootWindow(new ViewMap(), true);
         TabWindow documents = new TabWindow();
         ViewMap = new StringViewMap();
         DocumentManager.init(this, Root, ViewMap, documents);
         DocumentManager.New();
          
         // Create displays for a split REPL.
         History = new NonEditableTextArea();
         REPL = new REPLTextArea();
         ViewMap.addView("REPL - Execute", new View("REPL - Execute", null, REPL));
         ViewMap.addView("REPL - History", new View("REPL - History", null, History));
         SplitWindow replSplit = new SplitWindow(true, 0.5f, ViewMap.getView("REPL - Execute"), ViewMap.getView("REPL - History"));
         
         ViewMap.getView("REPL - Execute").getWindowProperties().setCloseEnabled(false);
         ViewMap.getView("REPL - History").getWindowProperties().setCloseEnabled(false);
         
         ViewMap.getView("REPL - Execute").getWindowProperties().setUndockEnabled(false);
         ViewMap.getView("REPL - History").getWindowProperties().setUndockEnabled(false);
         
         // Create the error/debug/display views.
         Debug = new NonEditableTextArea();
         ViewMap.addView("Debug", new View("Debug", null, Debug));
         
         // Listen and report new error messages.
         ErrorManager.addErrorListener(new ErrorListener() {
 			@Override
 			public void logError(String msg) {
 				Debug.append(msg + "\n");
 			}
         });
         
         // Put everything together into the actual dockable display.
         SplitWindow fullSplit = new SplitWindow(false, 0.6f, documents, replSplit);
         Root.setWindow(fullSplit);
         add(Root);
         
         // Add a toolbar.
         ToolBar = new JToolBar();
         ToolBarRun = new JButton(MenuManager.itemForName("Run").getAction());
         ToolBarStop = new JButton(MenuManager.itemForName("Stop").getAction());
         
         ToolBar.setFloatable(false);
         for (Action a : new Action[]{
         		MenuManager.itemForName("New").getAction(),
         		MenuManager.itemForName("Open").getAction(),
         		MenuManager.itemForName("Save").getAction(),
         		MenuManager.itemForName("Close").getAction(),
         		})
         	ToolBar.add(new JButton(a));
         
         ToolBar.addSeparator();
         for (Action a : new Action[]{
         		MenuManager.itemForName("Connect").getAction(),
         		MenuManager.itemForName("Upload").getAction(),
         		})
         	ToolBar.add(new JButton(a));
         
         ToolBar.addSeparator();
         for (Action a : new Action[]{
         		MenuManager.itemForName("Cut").getAction(),
         		MenuManager.itemForName("Copy").getAction(),
         		MenuManager.itemForName("Paste").getAction(),
         		MenuManager.itemForName("Undo").getAction(),
         		MenuManager.itemForName("Redo").getAction(),
         		MenuManager.itemForName("Find/Replace").getAction(),
         		})
         	ToolBar.add(new JButton(a));
         	
         ToolBar.addSeparator();
         ToolBar.add(ToolBarRun);
         ToolBar.add(ToolBarStop);
         for (Action a : new Action[]{
         		MenuManager.itemForName("Format").getAction(),
         		MenuManager.itemForName("Reset").getAction(),
         		})
         	ToolBar.add(new JButton(a));
         
         add(ToolBar, BorderLayout.PAGE_START);
         ToolBar.setVisible(Options.DisplayToolbar);
         
         // Disable items by default.
         MenuManager.itemForName("Stop").setEnabled(false);
 		ToolBarStop.setEnabled(false);
 		
 		// Remove text on toolbar buttons.
 		for (Component c : ToolBar.getComponents())
 			if (c instanceof JButton)
 				((JButton) c).setText("");
 				
 		// Add a tool to show the current row and column.
 		RowColumn = new JLabel("row:column");
 		ToolBar.addSeparator();
         ToolBar.add(RowColumn);
         
         // Connect to Petite
         // This thread also takes the output from Petite and relays it to the GUI
         try {
 			Petite = new Petite();
 			
 	        // Add a listener to reset the running state when Petite reports it is ready.
 	        Petite.addPetiteListener(new PetiteListener() {
 				@Override public void onReady() {
 					MenuManager.itemForName("Run").setEnabled(true);
 					ToolBarRun.setEnabled(true);
         			
         			MenuManager.itemForName("Stop").setEnabled(true);
 					ToolBarStop.setEnabled(false);
 				}
 				
 				@Override public void onInteropReturn() {}
 
 				@Override public void onOutput(String output) {
 					if (History != null && output != null) {
     					History.append(output);
     					History.goToEnd();
     				}
 				}
 			});
 		} 
         
         // This will come up if we cannot connect to Petite. This is a pretty critical error.
         catch (Exception e1) {
 			JOptionPane.showMessageDialog(
 					this, 
 					"Unable to start Petite process:\n" 
 							+ e1.getMessage() 
 							+ "\n\nPlease report this error to the developers.", 
 					"Error starting Petite", 
 					JOptionPane.ERROR_MESSAGE);
 			ErrorManager.logError(e1.getMessage());
 			e1.printStackTrace();
 		}
     }
 
 	/**
      * Run a line of Scheme code.
      * @param command The command to run.
      */
     public void doCommand(String command) {
     	// Don't allow multiple things to run at once.
     	MenuManager.itemForName("Run").setEnabled(false);
     	ToolBarRun.setEnabled(false);
     	
     	MenuManager.itemForName("Stop").setEnabled(true);
     	ToolBarStop.setEnabled(true);
     	
     	// Clean up the input and don't run empty content.
         final String cmd = command.trim();
         if (cmd.length() == 0)
             return;
 
         // Add the prompt and indent to match it.
         History.append("\n~ " + cmd.replace("\n", "\n  ") + "\n");
         History.goToEnd();
         
         // Actually execute the command (async, the petite thread above will capture any output).
         Petite.sendCommand(cmd);
     }
 
     /**
      * Update the display.
      */
 	public boolean updateDisplay() {
 		boolean reloaded = true;
 		for (SchemeTextArea ss : new SchemeTextArea[]{History, Debug, REPL}) {
 			try {
 				((SchemeDocument) ss.code.getDocument()).processChangedLines(0, ss.getText().length());
 				ss.updateUI();
 	    	}  catch (BadLocationException e) {
 	    		reloaded = false;
 	    		ErrorManager.logError("Unable to format view: " + e.getMessage());
 			}
 		}
 		return reloaded;
 	}
 
 	/**
 	 * Focus the REPL.
 	 */
 	public void focusREPL() {
 		REPL.code.requestFocusInWindow();
 	}
 
 	/**
 	 * Set the toolbar's display mode.
 	 * @param displayToolbar If the toolbar should be visible.
 	 */
 	public void toggleToolbar(boolean displayToolbar) {
 		ToolBar.setVisible(displayToolbar);
 	}
 
 	/**
 	 * Reset Kawa.
 	 */
 	public void resetScheme() {
 		Petite.reset();
 		History.setText("");
 		History.append(">>> Environment reset <<<\n");
 	}
 
 	/**
 	 * Show the debug view.
 	 */
 	public void showDebug() {
 		View view = ViewMap.getView("Debug");
 		
 		if (!view.isShowing()) {
 			if (view.getSize().width == 0 || view.getSize().height == 0)
 				view.setSize(500, 500);
 			
 			FloatingWindow win = Root.createFloatingWindow(getLocation(), view.getSize(), view);
 			win.getTopLevelAncestor().setVisible(true);
 		}
 	}
 
 	/**
 	 * Show the given view.
 	 * @param which Which display we are writing to.
 	 */
 	public void showView(String which) {
 		View view = ViewMap.getView(which);
 		
 		if (!view.isShowing()) {
 			if (view.getSize().width == 0 || view.getSize().height == 0)
 				view.setSize(500, 500);
 			
 			FloatingWindow win = Root.createFloatingWindow(getLocation(), view.getSize(), view);
 			win.getTopLevelAncestor().setVisible(true);
 		}
 	}
 	
 	/**
 	 * Stop all running worker threads.
 	 */
 	public void stopAllThreads(final boolean silent, final boolean andRestart) {
 		if (silent || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
 				this, 
 				"Stopping will reset the current Petite process.\n" + 
 						"Are you sure you want to do this?", 
 				"Confirm Stop", JOptionPane.YES_NO_OPTION)) {
 			
 			try {
 				if (andRestart) {
 					Petite.restart();
 					
 					Petite.addPetiteListener(new PetiteListener() {
 						
 						@Override
 						public void onReady() {
							// Tell the user it worked.
 							History.setText(">>> Execution halted <<<<\n\n");
 					    	History.goToEnd();
					    	
					    	// Don't stack these up.
 					    	Petite.removePetiteListener(this);
					    	
					    	// Reenable running code.
					    	MenuManager.itemForName("Run").setEnabled(true);
					    	ToolBarRun.setEnabled(true);
					    	
					    	MenuManager.itemForName("Stop").setEnabled(false);
					    	ToolBarStop.setEnabled(false);
 						}
 						
 						@Override public void onOutput(String output) {}
 						
 						@Override
 						public void onInteropReturn() {}
 					});
 					
 				} else {
 					Petite.stop();
 				}
 			} catch (Exception e) {
 				ErrorManager.logError("Unable to reconnect to Petite:\n" + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Set if the update dialog should be visible or not.
 	 * @param b The new value.
 	 */
 	public void setUpdateVisible(boolean b) {
 		UpdateButton.setVisible(b);
 	}
 }
