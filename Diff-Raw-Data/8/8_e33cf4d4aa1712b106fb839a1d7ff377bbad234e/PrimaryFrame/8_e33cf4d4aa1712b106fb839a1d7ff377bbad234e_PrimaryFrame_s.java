 package maze.gui;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.TreeSet;
 import java.util.Enumeration;
 
 import javax.swing.AbstractButton;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import maze.Main;
 import maze.gui.mazeeditor.MazeEditor;
 import maze.model.MazeInfo;
 import maze.model.MazeInfoModel;
 
 /**
  * This is the primary frame for the application.
  */
 public final class PrimaryFrame extends JFrame implements WindowListener
 {
    private final MazeInfoModel mMazeInfoModel = new MazeInfoModel();
    private MazeViewerPanel mazeViewer;
    private MazeEditor mazeEditor;
    private CodeEditingPanel codeEditorPanel;
    private final JTabbedPane mainTabs = new JTabbedPane();
 
    /**
     * Initializes the contents of this frame.
     */
    public void init()
    {
       this.mazeViewer = new MazeViewerPanel();
       this.codeEditorPanel = new CodeEditingPanel();
 
       this.setTitle("Micro Mouse Maze Editor and Simulator");
       this.setIconImage(Main.getImageResource("gui/images/logo.png").getImage());
 
       // menu bar
       JMenuBar menuBar = new JMenuBar();
 
       //File item
       JMenu fileMenu = new JMenu("File");
       menuBar.add(fileMenu);
 
       //New file menu.
       JMenu newMenu = new JMenu("New");
       fileMenu.add(newMenu);
 
       //New maze.
       JMenuItem newMaze = new JMenuItem("Maze...");
       newMaze.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             mainTabs.setSelectedComponent(mazeEditor);
             mazeEditor.newMaze();
          }
       });
       newMenu.add(newMaze);
 
       JMenuItem newScript = new JMenuItem("AI Script...");
       newMenu.add(newScript);
       newScript.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             mainTabs.setSelectedComponent(codeEditorPanel);
             codeEditorPanel.createNewEditor();
          }
       });
 
       // Open file.
       JMenuItem fileLoad = new JMenuItem("Open...");
       fileLoad.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             Component c = mainTabs.getSelectedComponent();
             if (c instanceof MenuControlled)
             {
                MenuControlled mc = (MenuControlled) c;
                mc.open();
             }
          }
       });
 
       fileMenu.add(fileLoad);
 
       // Save
       JMenuItem fileSave = new JMenuItem("Save");
       fileSave.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             Component c = mainTabs.getSelectedComponent();
             if (c instanceof MenuControlled)
             {
                MenuControlled mc = (MenuControlled) c;
                mc.saveCurrent();
             }
          }
       });
       fileMenu.add(fileSave);
 
       // Close file.
       JMenuItem fileClose = new JMenuItem("Close");
       fileClose.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             Component c = mainTabs.getSelectedComponent();
             if (c instanceof MenuControlled)
             {
                MenuControlled mc = (MenuControlled) c;
                mc.close();
             }
          }
       });
       fileMenu.add(fileClose);
 
       // exit
       fileMenu.addSeparator();
       JMenuItem fileExit = new JMenuItem("Exit");
       fileMenu.add(fileExit);
       fileExit.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             PrimaryFrame.this.dispose();
          }
       });
 
       // Help menu.
       JMenu helpMenu = new JMenu("Help");
       menuBar.add(helpMenu);
 
       //Help
       JMenuItem helpItem = new JMenuItem("Help");
       helpMenu.add(helpItem);
 
       helpItem.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             HelpInfo.createAndShowGUI();
          }
       });
 
       // Look and feel menu.
       String defaultLAF = "";
       JMenu lookAndFeel = new JMenu("Look And Feel");
       helpMenu.add(lookAndFeel);
       ButtonGroup lafBG = new ButtonGroup();
       LookAndFeelListener lafal = new LookAndFeelListener();
       boolean nimbusFound = false;
       for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels())
       {
          JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(laf.getName());
          lafBG.add(jrbmi);
          jrbmi.addActionListener(lafal);
          jrbmi.setActionCommand(laf.getClassName());
          lookAndFeel.add(jrbmi);
          if (laf.getName().equals("Nimbus"))
          {
             defaultLAF = laf.getClassName();
             lafBG.setSelected(jrbmi.getModel(), true);
             nimbusFound = true;
          }
       }
       if (!nimbusFound)
       {
          defaultLAF = UIManager.getSystemLookAndFeelClassName();
          Enumeration<AbstractButton> ab = lafBG.getElements();
          while (ab.hasMoreElements())
          {
             JRadioButtonMenuItem jrbmi = (JRadioButtonMenuItem)ab.nextElement();
             if (UIManager.getSystemLookAndFeelClassName().equals(
                                                       jrbmi.getActionCommand()))
                lafBG.setSelected(jrbmi.getModel(), true);
          }
       }
 
       // Separate the about dialog.
       helpMenu.addSeparator();
 
       // Help->About menu item.
       JMenuItem aboutMenuItem = new JMenuItem("About...");
       helpMenu.add(aboutMenuItem);
       aboutMenuItem.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
             String m = "<html><b>Version:</b> 0.9.1<br />"
                        + "You can find more information about this application "
                        + "at the following project page.<br />"
                        + "<a href=\"http://code.google.com/p/maze-solver/\">"
                        + "http://code.google.com/p/maze-solver/</a><br />"
                        + "</html>";
             JOptionPane.showMessageDialog(PrimaryFrame.this,
                                           m,
                                           "About",
                                           JOptionPane.INFORMATION_MESSAGE);
          }
       });
 
       this.setJMenuBar(menuBar);
 
       this.setSize(1000, 750);
 
       this.add(mainTabs);
 
       mainTabs.add("Micro Mouse Simulator", this.mazeViewer);
       mainTabs.add("Maze Editor", mazeEditor = new MazeEditor());
       mainTabs.add("AI Script Editor", this.codeEditorPanel);
       mainTabs.add("Statistics Display", new StatViewPanel());
       this.addWindowListener(this);
       try
       {
          UIManager.setLookAndFeel(defaultLAF);
       }
       catch (Exception ex){}
       SwingUtilities.updateComponentTreeUI(this);
 
    }
 
    /**
     * Returns a MazeInfoModel object containing all of the currently loaded
     * mazes.
     * @return said MazeInfoModel object.
     */
    public MazeInfoModel getMazeInfoModel()
    {
       return mMazeInfoModel;
    }
 
    public MazeInfo saveMaze(MazeInfo mi)
    {
       DefaultComboBoxModel cbm = mMazeInfoModel.getMazeInfoComboBoxModel();
       TreeSet<String> paths = new TreeSet<String>();
       for (int i = 0; i < cbm.getSize(); i++)
       {
          String path = ((MazeInfo) cbm.getElementAt(i)).getPath();
          if (!path.equals(""))
             paths.add(path.toLowerCase());
       }
       if (mi.isDirty())
       {
          MazeInfo toSave = mi;
          if (!mi.isMutable())
          {
             toSave = mi.getMutableClone();
             Box box = new Box(BoxLayout.Y_AXIS);
            JLabel label = new JLabel("What would you like ot call the" + " maze?");
             JTextField field = new JTextField();
             box.add(label);
             box.add(field);
             String newName = null;
             while (true)
             {
                int result = JOptionPane.showConfirmDialog(PrimaryFrame.this,
                                                           box,
                                                           "Maze Name",
                                                           JOptionPane.OK_CANCEL_OPTION,
                                                           JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.OK_OPTION)
                {
                   if (field.getText().length() > 0)
                   {
                      newName = field.getText();
                      break;
                   }
                }
                else
                   break;
             }
             if (newName == null)
                return null;
             mi.clearDirty();
             toSave.setName(newName);
             toSave.setPath("");
             mMazeInfoModel.addMaze(toSave);
             saveMaze(toSave);
             return toSave;
          }
          else if (mi.getPath() == null || mi.getPath().equals(""))
          {
             JFileChooser chooser = new JFileChooser(".");
             while (true)
             {
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
                {
                   File file = chooser.getSelectedFile();
                   try
                   {
                      if (paths.contains(file.getCanonicalPath().toLowerCase()))
                      {
                         JOptionPane.showMessageDialog(this,
                                                       "That file is being used by another maze",
                                                       "Error",
                                                       JOptionPane.ERROR_MESSAGE);
                      }
                      else
                      {
                         mi.setPath(file.getCanonicalPath());
                         break;
                      }
                   }
                   catch (IOException ex)
                   {
                      JOptionPane.showMessageDialog(this,
                                                    "Invalid Save File",
                                                    "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                   }
                } // if (chooser.showSaveDialog(this) ==
                //  JFileChooser.APPROVE_OPTION)
                else
                   break;
             } // while (true)
          } // if (mi.getPath().equals(""))
          mi.store();
       } // if (mi.isDirty())
       return mi;
    }
 
    @Override
    public void windowOpened(WindowEvent e)
    {}
 
    @Override
    public void windowClosing(WindowEvent e)
    {
       DefaultComboBoxModel cbm = mMazeInfoModel.getMazeInfoComboBoxModel();
       for (int i = 0; i < cbm.getSize(); i++)
       {
          MazeInfo mi = (MazeInfo) cbm.getElementAt(i);
          if (mi.isDirty())
          {
             int result;
             result = JOptionPane.showConfirmDialog(this,
                                                    "Would you like to save \"" +
                                                          mi.getName() +
                                                          "\"",
                                                    "Save Maze?",
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE);
             if (result == JOptionPane.YES_NO_OPTION)
                saveMaze(mi);
          }
       } // for (int i = 0; i < cbm.getSize(); i++)
    }
 
    @Override
    public void windowClosed(WindowEvent e)
    {}
 
    @Override
    public void windowIconified(WindowEvent e)
    {}
 
    @Override
    public void windowDeiconified(WindowEvent e)
    {}
 
    @Override
    public void windowActivated(WindowEvent e)
    {}
 
    @Override
    public void windowDeactivated(WindowEvent e)
    {}
 
    /**
     * Tells the primary frame if a simulation is currently running or not. We
     * can then disable GUI elements while animating.
     * @param simOn Set to true means the robot simulation is currently running.
     */
    public void setSimulation(boolean simOn)
    {
       this.mainTabs.setEnabled(!simOn);
    }
 
    /**
     * An ActionListener that changes the swing look and feel.
     */
    private class LookAndFeelListener implements ActionListener
    {
       @Override
       public void actionPerformed(ActionEvent e)
       {
          try
          {
             UIManager.setLookAndFeel(e.getActionCommand());
             SwingUtilities.updateComponentTreeUI(PrimaryFrame.this);
          }
          catch (Exception ex)
          {}
       }
    }
 }
