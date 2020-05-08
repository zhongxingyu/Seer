 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.TransferHandler;
 
 import org.trypticon.binary.Binary;
 import org.trypticon.binary.BinaryFactory;
 import org.trypticon.hex.datatransfer.DelegatingActionListener;
 import org.trypticon.hex.anno.AnnotationPane;
 import org.trypticon.hex.anno.MemoryAnnotationCollection;
 import org.trypticon.hex.HexViewer;
 
 /**
  * A top-level application frame.
  *
  * XXX: It probably makes sense to replace this with OpenIDE or some other framework.
  *
  * @author trejkaz
  */
 public class HexFrame extends JFrame {
     private final HexViewer viewer;
     private AnnotationPane annoPane;
 
     /**
      * Constructs the top-level frame.
      */
     public HexFrame() {
         super("Hex");
 
         setJMenuBar(buildMenuBar());
 
         viewer = new HexViewer();
         annoPane = new AnnotationPane();
 
 
         JScrollPane viewerScroll = new JScrollPane(viewer);
         viewerScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(viewerScroll, BorderLayout.CENTER);
         getContentPane().add(annoPane, BorderLayout.WEST);
         pack();
     }
 
     /**
      * Loads a file into the viewer within this frame.
      *
      * @param file the file to load.
      */
     public void loadFile(File file) {
         try {
             Binary binary = BinaryFactory.open(file);
             loadBinary(binary);
         } catch (IOException e) {
             JOptionPane.showMessageDialog(getRootPane(), "There was an error opening the file.");
         }
     }
 
     /**
      * Loads the specified binary into the viewer within this frame.
      *
      * @param binary the binary to open.
      */
     public void loadBinary(Binary binary) {
         viewer.setBinary(binary);
         annoPane.setAnnotations(new MemoryAnnotationCollection());
     }
 
     private JMenuBar buildMenuBar() {
         JMenu fileMenu = new JMenu("File");
         fileMenu.add(new OpenAction());
 
         if (!System.getProperty("os.name").toLowerCase().startsWith("mac")) {
             fileMenu.addSeparator();
             fileMenu.add(new ExitAction());
         }
 
         // TODO: Copy as:
         //  - hex
         //  - java source
         //  - ?
         JMenu editMenu = new JMenu("Edit");
 
         DelegatingActionListener actionListener = new DelegatingActionListener();
 
         JMenuItem copyMenuItem = new JMenuItem("Copy");
         copyMenuItem.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
         copyMenuItem.addActionListener(actionListener);
         copyMenuItem.setMnemonic('c');
         copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         editMenu.add(copyMenuItem);
 
         JMenuItem selectAllMenuItem = new JMenuItem("Select All");
         selectAllMenuItem.setActionCommand("select-all");
         selectAllMenuItem.addActionListener(actionListener);
         selectAllMenuItem.setMnemonic('a');
         selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                                                                 Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         editMenu.add(selectAllMenuItem);
 
         JMenuBar menuBar = new JMenuBar();
         menuBar.add(fileMenu);
         menuBar.add(editMenu);
         return menuBar;
     }
 
     /**
      * Action to open a new file for viewing.
      */
     private class OpenAction extends AbstractAction {
         private OpenAction() {
             putValue(NAME, "Open...");
             putValue(MNEMONIC_KEY, (int) 'o');
             putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                              Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         }
 
         public void actionPerformed(ActionEvent event) {
             JFileChooser chooser = new JFileChooser();
             if (chooser.showOpenDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
                 File file = chooser.getSelectedFile();
                 loadFile(file);
             }
         }
     }
 
     /**
      * Action to exit the application.
      */
     private class ExitAction extends AbstractAction {
         private ExitAction() {
             putValue(NAME, "Exit");
             putValue(MNEMONIC_KEY, (int) 'x');
         }
 
         public void actionPerformed(ActionEvent event) {
             dispose();
         }
     }
 }
