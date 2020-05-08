 /*
  * Thingamabob - A Java-based Turing Machine Emulator
  * Copyright (c) 2010 Nicholas Kamper, Drew Hill, Travis Baumbaugh
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  */
 
 package com.nick125.thingamabob.gui;
 
 import com.nick125.thingamabob.machine.core.Machine;
 import com.nick125.thingamabob.machine.core.SimpleInstSet;
 import com.nick125.thingamabob.machine.core.Tape;
 import com.nick125.thingamabob.parser.Parser;
 import com.nick125.thingamabob.parser.ParsingError;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Scanner;
 import java.util.TimeZone;
 
 /**
  * The main turing window.
  *
  * @author baumbata. Created Oct 19, 2010.
  */
 public class TWindow extends JFrame {
     private static final Dimension SIZE = new Dimension(800, 400);
     private static final String TITLE = "Thingamabob";
 
     private TStatusBar statusBar;
     private int delay = 10; // in Hz
 
     private BorderLayout layout = new BorderLayout();
 
     private Machine<String> machine;
     private Tape<String> tape;
     private String initialTape;
 
     /**
      * Creates the window for the program
      */
     public TWindow() {
         // Invoke the GUI creator in the EDT.
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 TWindow.this.createGUI();
             }
         });
     }
 
     private void createGUI() {
         // Set some things
 
         this.setSize(TWindow.SIZE);
         this.setTitle(TWindow.TITLE);
         this.setLayout(this.layout);
         // this.layout.setHgap(40);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         // Create the menubar
         this.setJMenuBar(this.createMenu());
 
         // Create and add the status bar
         this.statusBar = new TStatusBar();
         this.add(this.statusBar, BorderLayout.SOUTH);
 
         // Create the machine
         this.machine = new Machine<String>();
         this.machine.addEventListener(this.statusBar);
 
         // Create a Tape and a TTape
         this.tape = new Tape<String>();
         this.machine.setTape(this.tape);
         TTape<String> ttape = new TTape<String>(this.tape);
         this.add(ttape, BorderLayout.NORTH);
 
         // Resize our window
         this.setSize(800, 200);
 
     }
 
     private JMenuBar createMenu() {
         JMenuBar menubar = new JMenuBar();
 
         /* File Menu */
         JMenu fileMenu = new JMenu("File");
         fileMenu.add(new QuitProgram());
         menubar.add(fileMenu);
 
         /* Machine Menu */
         JMenu machineMenu = new JMenu("Machine");
         machineMenu.add(new RunMachine());
         machineMenu.add(new StopMachine());
         machineMenu.add(new SetMachineSpeed());
         machineMenu.addSeparator();
 
         /*- Instruction Set Menu -*/
         JMenu instMenu = new JMenu("Instruction Set");
         instMenu.add(new LoadInstructionAction());
         machineMenu.add(instMenu);
 
         /*- Tape Menu -*/
         JMenu tapeMenu = new JMenu("Tape");
         tapeMenu.add(new LoadTapefromFile());
         tapeMenu.add(new ResetTape());
         machineMenu.add(tapeMenu);
         menubar.add(machineMenu);
 
         /* Help Menu */
         JMenu helpMenu = new JMenu("Help");
         helpMenu.add(new HelpEvent());
         menubar.add(helpMenu);
 
         return menubar;
     }
 
     /*- ACTIONS -*/
 
     private class StopMachine extends AbstractAction {
         public StopMachine() {
             super("Stop");
             this.putValue(SHORT_DESCRIPTION, "Stops the machine");
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             TWindow.this.machine.setState(false);
         }
     }
 
     private class SetMachineSpeed extends AbstractAction {
         public SetMachineSpeed() {
             super("Set Machine Speed");
             this.putValue(SHORT_DESCRIPTION, "Sets the machine speed (in HZ)");
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             String value = JOptionPane
                     .showInputDialog(
                             TWindow.this,
                             "Enter machine speed in Hertz (cycles per minute, as an integer)",
                             Integer.toString(TWindow.this.delay));
             try {
                 Integer iValue = Integer.valueOf(value);
                 if (iValue <= 0) {
                     JOptionPane.showMessageDialog(TWindow.this,
                             "The speed must be an integer greater than zero.");
                 }
                 TWindow.this.delay = iValue;
             } catch (NumberFormatException e) {
                 JOptionPane.showMessageDialog(null, "That wasn't a number!");
             }
         }
     }
 
     private class RunMachine extends ThreadedAction {
 
         public RunMachine() {
             super("Run");
             this.putValue(SHORT_DESCRIPTION, "Runs the machine");
         }
 
         @Override
         public boolean preThreadActions() {
             if (TWindow.this.machine.getInstructionSet() == null) {
                 JOptionPane.showMessageDialog(TWindow.this,
                         "No instruction set loaded");
                 return false;
             }
             return true;
         }
 
         @Override
         public void threadedWorker() {
             if (TWindow.this.machine.getInstructionSet() == null) {
                 JOptionPane.showMessageDialog(TWindow.this,
                         "No instruction set loaded");
             } else {
                 TWindow.this.machine.setState(true);
                 TWindow.this.machine.reset();
                 while (TWindow.this.machine.isRunning()) {
                     try {
                         TWindow.this.machine.execute();
                         Thread.sleep(1000 / TWindow.this.delay);
                     } catch (InterruptedException exception) {
                         exception.printStackTrace();
                     }
                 }
             }
         }
     }
 
     private class LoadTapefromFile extends ThreadedAction {
         public File selectedFile;
 
         public LoadTapefromFile() {
             super("Load from File");
             this.putValue(SHORT_DESCRIPTION, "Load an tape from file");
         }
 
         @Override
         public boolean preThreadActions() {
             JFileChooser chooser = new JFileChooser();
             chooser.setFileFilter(new TSingleFileFilter("tape",
                     "Turing Machine Tape File (.tape)"));
             if (chooser.showOpenDialog(TWindow.this) == JFileChooser.APPROVE_OPTION) {
                 this.selectedFile = chooser.getSelectedFile();
                 return true;
             }
             return false;
         }
 
         @Override
         public void threadedWorker() {
             if (this.selectedFile != null) {
                 Scanner read = null;
                 try {
                     read = new Scanner(this.selectedFile);
                 } catch (FileNotFoundException exception) {
                     JOptionPane.showMessageDialog(TWindow.this,
                             "Error loading file");
                 }
                 TWindow.this.initialTape = read.next();
                 TWindow.this.statusBar
                         .setNotificationArea(String.format(
                                 "Loading tape from %s...",
                                 this.selectedFile.getName()));
                 TWindow.this.tape.clear();
                 TWindow.this.tape.loadFromString(TWindow.this.initialTape,
                         TWindow.this.delay);
                 TWindow.this.statusBar.setNotificationArea("Loaded tape");
             }
         }
     }
 
     private class ResetTape extends ThreadedAction {
         public ResetTape() {
             super("Reset Tape");
             putValue(SHORT_DESCRIPTION,
                     "Resets the tape to the last tape loaded");
         }
 
         @Override
         public void threadedWorker() {
             TWindow.this.statusBar.setNotificationArea("Resetting tape...");
             TWindow.this.tape.clear();
             TWindow.this.tape.loadFromString(TWindow.this.initialTape,
                     TWindow.this.delay);
             TWindow.this.statusBar.setNotificationArea("Tape reset");
         }
 
         @Override
         public boolean preThreadActions() {
             if (TWindow.this.initialTape != null) {
                 return true;
             } else {
                 JOptionPane.showMessageDialog(TWindow.this,
                         "No tape loaded to reset from.");
                 return false;
             }
         }
     }
 
     private class QuitProgram extends AbstractAction {
 
         public QuitProgram() {
             super("Quit");
             this.putValue(SHORT_DESCRIPTION, "Exit the program");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             System.exit(0);
         }
 
     }
 
     private class HelpEvent extends AbstractAction {
 
         public HelpEvent() {
             super("About");
             this.putValue(SHORT_DESCRIPTION,
                     "Display an informational dialogue");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             Component frame = new JFrame();
            String msg = "<html>"+
                         "Thingamabob<br/>" +
                          "A Java-based Turing Machine Emulator<br/>" +
                          "Licensed under the MIT/X11 License<br/>" +
                          "<b>Developers</b>:" +
                          "Nicholas Kamper (kampernj@rose-hulman.edu) <br />" +
                         "Drew Hill and Travis Baumbaugh" +
                         "</html>";
             JLabel label = new JLabel(msg);

             JOptionPane.showMessageDialog(frame, label, "About",
                     JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     private class LoadInstructionAction extends AbstractAction {
 
         public LoadInstructionAction() {
             super("Load");
             this.putValue(SHORT_DESCRIPTION, "Load an instruction set");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             // Prompt the user for a file
             JFileChooser chooser = new JFileChooser();
             chooser.setFileFilter(new TSingleFileFilter("midl",
                     "Machine Instruction Description Language (.midl)"));
             int status = chooser.showOpenDialog(TWindow.this);
             if (status == JFileChooser.APPROVE_OPTION) {
                 Parser parser = new Parser();
                 try {
                     SimpleInstSet<String> instructionSet = parser
                             .parseStream(new FileReader(chooser
                                     .getSelectedFile()));
                     TWindow.this.machine
                             .setInstructionSet(instructionSet);
                     TWindow.this.statusBar.setNotificationArea(String.format(
                             "Loaded Instruction Set from %s", chooser
                                     .getSelectedFile().getName()));
                 } catch (FileNotFoundException exception) {
                     JOptionPane.showMessageDialog(TWindow.this,
                             "File not found.");
                 } catch (IOException exception) {
                     JOptionPane.showMessageDialog(TWindow.this,
                             "Error reading file.");
                 } catch (ParsingError exception) {
                     JOptionPane.showMessageDialog(TWindow.this,
                             "An error occurred when parsing the instruction set: "
                                     + exception.getMessage());
                 }
             }
 
         }
     }
 }
