 /*
  * [The "New BSD" license]
  * Copyright (c) 2012 The Board of Trustees of The University of Alabama
  * All rights reserved.
  *
  * See LICENSE for details.
  */
 package edu.ua.eng.software.novel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 
 /**
  * Creates the main UI frame and menu
  * 
  * @author Colin C. Hemphill <colin@hemphill.us>
  * @version 06/25/12
  * 
  */
 public class NovelFrame extends JFrame implements ActionListener {
 
     public final void initUI() {
 
         JMenuBar menu = new JMenuBar();
         JMenu file = new JMenu("File");
         JMenu edit = new JMenu("Edit");
         JMenu help = new JMenu("Help");
         menu.add(file);
         menu.add(edit);
         menu.add(help);
 
         JMenuItem fileImport = new JMenuItem("Import/Run");
         JMenuItem filePrefs = new JMenuItem("Preferences");
         JMenuItem fileExit = new JMenuItem("Exit");
 
         JMenuItem editDif = new JMenuItem("Difference");
         JMenuItem editFilter = new JMenuItem("Filter");
 
         JMenuItem helpTut = new JMenuItem("Tutorials");
         JMenuItem helpAbout = new JMenuItem("About");
 
         helpTut.setActionCommand("Tutorial");
         helpTut.setMnemonic(KeyEvent.VK_F1);
         helpTut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
         helpTut.addActionListener(this);
         helpAbout.setActionCommand("About");
         helpAbout.setMnemonic(KeyEvent.VK_F2);
         helpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
         helpAbout.addActionListener(this);
 
         file.add(fileImport);
         file.add(filePrefs);
         file.add(fileExit);
         edit.add(editDif);
         edit.add(editFilter);
         help.add(helpTut);
         help.add(helpAbout);
         setJMenuBar(menu);
 
         setTitle("N.o.V.E.L.");
         setSize(800, 600);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
 
         NovelPanel panel = new NovelPanel();
         setContentPane(panel.createContentPane());
     }
 
     public void createAboutMenu() {
 
        JOptionPane.showMessageDialog(this, "N.o.V.E.L. Copyright 2012"
                 + "\nVersion 1.0"
                + "\n\nBlake Bassett, Casey Ferris,"
                 + "\nColin Hemphill, Conor Kirkman,"
                 + "\nNicholas Kraft, Paige Rodeghero", "About N.o.V.E.L.",
                 JOptionPane.INFORMATION_MESSAGE);
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().equals("Tutorial"))
             createTutorial();
         else if (e.getActionCommand().equals("About"))
             createAboutMenu();
     }
 
     public void createTutorial() {
     }
 }
