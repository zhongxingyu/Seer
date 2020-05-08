 /*
  *  $Id$
  *  IzPack
  *  Copyright (C) 2001-2003 Julien Ponge, Tino Schwarze
  *
  *  File :               CompilePanel.java
  *  Description :        A panel to compile files after installation
  *  Author's email :     julien@izforge.com
  *  Author's Website :   http://www.izforge.com
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.izforge.izpack.panels;
 
 import com.izforge.izpack.installer.*;
 import com.izforge.izpack.gui.*;
 import com.izforge.izpack.util.FileExecutor;
 import com.izforge.izpack.util.Debug;
 
 import java.io.*;
 import java.util.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import net.n3.nanoxml.*;
 
 /**
  *  The compile panel class.
  *
  * This class allows .java files to be compiled after installation.
  *
  * Parts of the code have been taken from InstallPanel.java and
  * modified a lot.
  * 
  * @author     Tino Schwarze
  * @author     Julien Ponge
  * @created    May 2003
  */
 public class CompilePanel extends IzPanel implements ActionListener, Runnable
 {
   /**  The combobox for compiler selection. */
   protected JComboBox compilerComboBox;
 
   /**  The combobox for compiler argument selection. */
   protected JComboBox argumentsComboBox;
 
   /**  The start button. */
   protected JButton startButton;
 
   /**  The tip label. */
   protected JLabel tipLabel;
 
   /**  The operation label . */
   protected JLabel opLabel;
 
   /**  The progress bar. */
   protected JProgressBar progressBar;
 
   /**  True if the compilation has been done. */
   private volatile boolean validated = false;
 
   /**  Compilation jobs */
   private ArrayList  jobs;
 
   /**  Name of resource for specifying compilation parameters. */
   private static final String SPEC_RESOURCE_NAME  = "CompilePanel.Spec.xml";
 
   private VariableSubstitutor vs;
 
   /**  We spawn a thread to perform compilation. */
   private Thread compilationThread;
 
   /**
    *  The constructor.
    *
    * @param  parent  The parent window.
    * @param  idata   The installation data.
    */
   public CompilePanel(InstallerFrame parent, InstallData idata)
   {
     super(parent, idata);
 
     this.vs = new VariableSubstitutor(idata.getVariableValueMap());
 
     /* code from InstallPanel
     // We initialize our layout
     GridBagLayout layout = new GridBagLayout();
     GridBagConstraints gbConstraints = new GridBagConstraints();
     setLayout(layout);
 
     tipLabel = new JLabel(parent.langpack.getString("CompilePanel.tip"),
     parent.icons.getImageIcon("tip"), JLabel.TRAILING);
     parent.buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 0.0);
     gbConstraints.fill = GridBagConstraints.NONE;
     gbConstraints.anchor = GridBagConstraints.NORTHWEST;
     layout.addLayoutComponent(tipLabel, gbConstraints);
     add(tipLabel);
 
     opLabel = new JLabel(" ", JLabel.TRAILING);
     parent.buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
     gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
     layout.addLayoutComponent(opLabel, gbConstraints);
     add(opLabel);
 
     progressBar = new JProgressBar();
     progressBar.setStringPainted(true);
     progressBar.setString(parent.langpack.getString("CompilePanel.begin"));
     progressBar.setValue(0);
     parent.buildConstraints(gbConstraints, 0, 3, 2, 1, 1.0, 0.0);
     gbConstraints.anchor = GridBagConstraints.NORTH;
     gbConstraints.fill = GridBagConstraints.HORIZONTAL;
     layout.addLayoutComponent(progressBar, gbConstraints);
     add(progressBar);
      */
 
     GridBagConstraints gridBagConstraints;
 
     JLabel heading = new JLabel();
     JLabel compilerLabel = new JLabel();
     compilerComboBox = new JComboBox();
     JLabel argumentsLabel = new JLabel();
     argumentsComboBox = new JComboBox();
     startButton = ButtonFactory.createButton (parent.langpack.getString ("CompilePanel.start"), idata.buttonsHColor);
     tipLabel = new JLabel(parent.langpack.getString ("CompilePanel.tip"),
         parent.icons.getImageIcon ("tip"), JLabel.TRAILING);
     opLabel = new JLabel();
     progressBar = new JProgressBar();
 
     setLayout(new GridBagLayout());
 
     Font font = heading.getFont ();
     font = font.deriveFont (Font.BOLD, font.getSize()*2.0f);
     heading.setFont(font);
     heading.setHorizontalAlignment(SwingConstants.CENTER);
     heading.setText(parent.langpack.getString ("CompilePanel.heading"));
     heading.setVerticalAlignment(SwingConstants.TOP);
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = GridBagConstraints.NORTH;
     gridBagConstraints.weighty = 0.1;
     add(heading, gridBagConstraints);
 
     compilerLabel.setHorizontalAlignment(SwingConstants.LEFT);
     compilerLabel.setLabelFor(compilerComboBox);
     compilerLabel.setText(parent.langpack.getString ("CompilePanel.choose_compiler"));
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridy = 1;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weighty = 0.1;
     add(compilerLabel, gridBagConstraints);
 
     compilerComboBox.setEditable(true);
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridy = 1;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weighty = 0.1;
     // TODO: make this system-dependent
     compilerComboBox.addItem ("javac");
     compilerComboBox.addItem ("jikes");
     // more known compilers?
     add(compilerComboBox, gridBagConstraints);
 
     argumentsLabel.setHorizontalAlignment(SwingConstants.LEFT);
     argumentsLabel.setLabelFor(argumentsComboBox);
     argumentsLabel.setText(parent.langpack.getString ("CompilePanel.additional_arguments"));
     //argumentsLabel.setToolTipText("");
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridy = 2;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weightx = 0.5;
     gridBagConstraints.weighty = 0.1;
     add(argumentsLabel, gridBagConstraints);
 
     argumentsComboBox.setEditable(true);
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridy = 2;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weightx = 0.5;
     gridBagConstraints.weighty = 0.1;
     argumentsComboBox.addItem ("-O -g:none");
     argumentsComboBox.addItem ("-O");
     argumentsComboBox.addItem ("-g");
     add(argumentsComboBox, gridBagConstraints);
 
     startButton.setText(parent.langpack.getString ("CompilePanel.start"));
     startButton.addActionListener (this);
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 3;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weighty = 0.1;
     add(startButton, gridBagConstraints);
 
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridy = 4;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = GridBagConstraints.NONE;
     gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
     add(tipLabel, gridBagConstraints);
 
     opLabel.setText(" ");
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridy = 5;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     add(opLabel, gridBagConstraints);
 
     progressBar.setValue(0);
     progressBar.setString(parent.langpack.getString ("CompilePanel.progress.initial"));
     progressBar.setStringPainted(true);
     gridBagConstraints = new GridBagConstraints();
     gridBagConstraints.gridy = 6;
     gridBagConstraints.gridwidth = 2;
     gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = GridBagConstraints.SOUTH;
     add(progressBar, gridBagConstraints);
   }
 
 
   /**
    *  Indicates wether the panel has been validated or not.
    *
    * @return    The validation state.
    */
   public boolean isValidated()
   {
     return validated;
   }
 
   /**
    *  Action function, called when the start button is pressed.
    */
   public void actionPerformed (ActionEvent e)
   {
     if (e.getSource() == this.startButton)
     {
       // disable all controls
       this.startButton.setEnabled (false);
       this.compilerComboBox.setEnabled (false);
       this.argumentsComboBox.setEnabled (false);
       parent.blockGUI();
       this.compilationThread = new Thread (this, "compilation thread");
       this.compilationThread.start();
     }
   }
 
   /** This is called when the compilation thread is activated. */
   public void run ()
   {
     collectJobs ();
     compileJobs ();
   }
 
   /**
    *  An error was encountered.
    *
    * @param  error  The error text.
    */
   public void errorCompile (String error)
   {
     opLabel.setText(error);
     idata.installSuccess = false;
     JOptionPane.showMessageDialog(this, error.toString(),
       parent.langpack.getString("CompilePanel.error"),
       JOptionPane.ERROR_MESSAGE);
   }
 
 
   /**  The unpacker stops.  */
   public void stopCompilation ()
   {
     parent.releaseGUI();
     parent.lockPrevButton();
     progressBar.setString(parent.langpack.getString("CompilePanel.progress.finished"));
     progressBar.setEnabled(false);
     progressBar.setValue (progressBar.getMaximum());
     opLabel.setText(" ");
     opLabel.setEnabled(false);
     validated = true;
     if (idata.panels.indexOf(this) != (idata.panels.size() - 1))
       parent.unlockNextButton();
   }
 
 
   /**
    *  Normal progress indicator.
    *
    * @param  val  The progression value.
    * @param  msg  The progression message.
    */
   public void progressCompile (int val, String msg)
   {
     Debug.trace ("progress: " + val + " " + msg);
     progressBar.setValue(val + 1);
     opLabel.setText(msg);
   }
 
 
   /**
    *  Job changing.
    *
    * @param  min       The new mnimum progress.
    * @param  max       The new maximum progress.
    * @param  jobName   The job name.
    */
   public void changeCompileJob (int min, int max, String jobName)
   {
     progressBar.setValue(0);
     progressBar.setMinimum(min);
     progressBar.setMaximum(max);
     progressBar.setString(jobName);
     opLabel.setText ("");
   }
 
 
   /**  Called when the panel becomes active.  */
   public void panelActivate()
   {
     // We clip the panel
     /* XXX: what's that for?
     Dimension dim = parent.getPanelsContainerSize();
     dim.width = dim.width - (dim.width / 4);
     dim.height = 150;
     setMinimumSize(dim);
     setMaximumSize(dim);
     setPreferredSize(dim);
     */
     parent.lockNextButton();
   }
 
   /**
    * Parse the compilation specification file and create jobs.
    */
   private boolean collectJobs ()
   {
 		InputStream input;
 		try
 		{
 			input = parent.getResource(SPEC_RESOURCE_NAME);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
       return false;
 		}
 
     StdXMLParser parser = new StdXMLParser ();
     parser.setBuilder (new StdXMLBuilder ());
     parser.setValidator (new NonValidator ());
     
 		XMLElement data;
 		try
 		{
 			parser.setReader (new StdXMLReader (input));
 			
 			data = (XMLElement) parser.parse();
 		}
 		catch (Exception e)
 		{
       System.out.println("Error parsing XML specification for compilation.");
 			e.printStackTrace();
       return false;
 		}
 
     if (! data.hasChildren ())
       return false;
 
     // list of classpath entries
     ArrayList classpath = new ArrayList();
 
     this.jobs = new ArrayList();
 
     // we throw away the toplevel compilation job
     // (all jobs are collected in this.jobs)
     CompilationJob dummy = collectJobsRecursive (data, classpath);
     
     return true;
   }
 
 
   private CompilationJob collectJobsRecursive (XMLElement node, ArrayList classpath)
   {
     Enumeration toplevel_tags = node.enumerateChildren ();
     ArrayList ourclasspath = (ArrayList)classpath.clone ();
     ArrayList files = new ArrayList();
 
     while (toplevel_tags.hasMoreElements ())
     {
       XMLElement child = (XMLElement)toplevel_tags.nextElement ();
 
       if (child.getName ().equals ("classpath"))
       {
         changeClassPath (ourclasspath, child);
       }
       else if (child.getName ().equals ("job"))
       {
         CompilationJob subjob = collectJobsRecursive (child, ourclasspath);
         if (subjob != null)
           this.jobs.add (subjob);
       }
       else if (child.getName().equals ("directory"))
       {
         String name = child.getAttribute ("name");
 
         if (name != null)
         {
           // substitute variables
           String finalname = this.vs.substitute (name, "plain");
 
           files.addAll (scanDirectory (new File (finalname)));
         }
 
       }
       else if (child.getName().equals ("file"))
       {
         String name = child.getAttribute ("name");
 
         if (name != null)
         {
           // substitute variables
           String finalname = this.vs.substitute (name, "plain");
 
           files.add (new File (finalname));
         }
 
       }
       else if (child.getName().equals ("packdepency"))
       {
         String name = child.getAttribute ("name");
 
         if (name == null)
         {
           System.out.println ("invalid compilation spec: <packdepency> without name attribute");
           return null;
         }
 
         // check whether the wanted pack was selected for installation
         Iterator pack_it = this.idata.selectedPacks.iterator();
         boolean found = false;
 
         while (pack_it.hasNext())
         {
           com.izforge.izpack.Pack pack = (com.izforge.izpack.Pack)pack_it.next();
 
           if (pack.name.equals (name))
           {
             found = true;
             break;
           }
         }
 
         if (! found)
         {
           Debug.trace ("skipping job because pack " + name + " was not selected.");
           return null;
         }
 
       }
 
     }
 
     if (files.size() > 0)
       return new CompilationJob (this, (String)node.getAttribute ("name"), files, ourclasspath);
 
     return null;
   }
 
   /** helper: process a <code>&lt;classpath&gt;</code> tag. */
   private void changeClassPath (ArrayList classpath, XMLElement child)
   {
     String add = child.getAttribute ("add");
     if (add != null)
       classpath.add (this.vs.substitute (add, "plain"));
 
     String sub = child.getAttribute ("sub");
     if (sub != null)
     {
       int cpidx = -1;
       sub = this.vs.substitute (sub, "plain");
 
       do
       {
         cpidx = classpath.indexOf (sub);
         classpath.remove (cpidx);
       } 
       while (cpidx >= 0);
 
     }
   }
 
   /** helper: recursively scan given directory.
    * 
    * @return list of files found (might be empty)
    */
   private ArrayList scanDirectory (File path)
   {
     Debug.trace ("scanning directory " + path.getAbsolutePath());
 
     ArrayList result = new ArrayList ();
 
     if (! path.isDirectory ())
       return result;
 
     File[] entries = path.listFiles ();
 
     for (int i = 0; i < entries.length; i++)
     {
       File f = entries[i];
 
       if (f == null) continue;
       
       if (f.isDirectory ())
       {
         result.addAll (scanDirectory (f));
       }
       else if ((f.isFile()) && (f.getName().toLowerCase().endsWith (".java")))
       {
         result.add (f);
       }
 
     }
 
     return result;
   }
 
   private void compileJobs ()
   {
     // XXX: check whether compiler is valid
     String compiler = (String)this.compilerComboBox.getSelectedItem ();
     ArrayList args = new ArrayList();
     StringTokenizer tokenizer = new StringTokenizer ((String)this.argumentsComboBox.getSelectedItem ());
 
     while (tokenizer.hasMoreTokens ())
     {
       args.add (tokenizer.nextToken());
     }
 
     Iterator job_it = this.jobs.iterator();
 
     while (job_it.hasNext())
     {
       CompilationJob job = (CompilationJob) job_it.next();
       
       this.changeCompileJob (0, job.getSize(), job.getName());
 
       if (! job.perform (compiler, args))
         break;
     }
 
     Debug.trace ("compilation finished.");
     stopCompilation ();
   }
 
   /** a compilation job */
   private class CompilationJob
   {
     private CompilePanel panel;
     private String    name;
     private ArrayList files;
     private ArrayList classpath;
     // XXX: figure that out (on runtime?)
     private static final int MAX_CMDLINE_SIZE = 4096;
 
     public CompilationJob (CompilePanel panel, ArrayList files, ArrayList classpath)
     {
       this.panel = panel;
       this.name = null;
       this.files = files;
       this.classpath = classpath;
     }
 
     public CompilationJob (CompilePanel panel, String name, ArrayList files, ArrayList classpath)
     {
       this.panel = panel;
       this.name = name;
       this.files = files;
       this.classpath = classpath;
     }
 
     public String getName ()
     {
       if (this.name != null)
         return this.name;
 
       return "";
     }
 
     public int getSize ()
     {
       return this.files.size();
     }
 
     public boolean perform (String compiler, ArrayList arguments)
     {
       Debug.trace ("starting job " + this.name);
       // we have some maximum command line length - need to count
       int cmdline_len = 0;
 
       // used to collect the arguments for executing the compiler
       LinkedList args = new LinkedList(arguments);
 
       Iterator arg_it = args.iterator();
       while (arg_it.hasNext ())
         cmdline_len += ((String)arg_it.next()).length()+1;
 
       // add compiler in front of arguments
       args.add (0, compiler);
       cmdline_len += compiler.length()+1;
 
       // construct classpath argument for compiler
       // - collect all classpaths
       StringBuffer classpath_sb = new StringBuffer();
       Iterator cp_it = this.classpath.iterator();
       while (cp_it.hasNext ())
       {
         String cp = (String)cp_it.next();
         if (classpath_sb.length() > 0)
           classpath_sb.append (File.pathSeparatorChar);
         classpath_sb.append (cp);
       }
 
       String classpath_str = classpath_sb.toString ();
 
       // - add classpath argument to command line
       args.add ("-classpath");
       cmdline_len = cmdline_len + 11;
       args.add (classpath_str);
       cmdline_len += classpath_str.length()+1;
 
       // remember how many arguments we have which don't change for the job
       int common_args_no = args.size();
       // remember how long the common command line is
       int common_args_len = cmdline_len;
 
       // used for execution
       FileExecutor executor = new FileExecutor ();
       String output[] = new String[2];
 
       // used for displaying the progress bar
       String jobfiles = "";
       int fileno = 0;
       int last_fileno = 0;
 
       // now iterate over all files of this job
       Iterator file_it = this.files.iterator();
 
       while (file_it.hasNext())
       {
         File f = (File)file_it.next();
 
         String fpath = f.getAbsolutePath();
 
         Debug.trace ("processing "+fpath);
         
         fileno++;
         jobfiles += f.getName() + " ";
         args.add (fpath);
         cmdline_len += fpath.length();
 
         // start compilation if maximum command line length reached
         if (cmdline_len >= MAX_CMDLINE_SIZE)
         {
           Debug.trace ("compiling " + jobfiles);
 
           // display useful progress bar (avoid showing 100% while still
           // compiling a lot)
           panel.progressCompile (last_fileno, jobfiles);
           last_fileno = fileno;
 
           int retval = executor.executeCommand ((String[])args.toArray(output), output);
 
           // update progress bar: compilation of fileno files done
           panel.progressCompile (fileno, jobfiles);
 
           if (retval != 0)
           {
             System.out.println ("failed. stderr of command follows:");
             System.out.println (output[0]);
             System.out.println ("stdout of command follows:");
             System.out.println (output[1]);
             return false;
           }
 
           // clean command line: remove files we just compiled
           for (int i = args.size()-1; i >= common_args_no; i--)
           {
             args.removeLast ();
           }
 
           cmdline_len = common_args_len;
           jobfiles = "";
         }
 
       }
 
       if (cmdline_len > common_args_len)
       {
         panel.progressCompile (last_fileno, jobfiles);
 
         int retval = executor.executeCommand ((String[])args.toArray(output), output);
 
         panel.progressCompile (fileno, jobfiles);
 
         if (retval != 0)
         {
           System.out.println ("failed. stderr of command follows:");
           System.out.println (output[1]);
           System.out.println ("stdout of command follows:");
           System.out.println (output[0]);
          this.panel.errorCompile (output[0] + output[1]);
           return false;
         }
       }
 
       Debug.trace ("job "+this.name+" done (" + fileno + " files compiled)");
 
       return true;
     }
 
   }
 
 }
 
