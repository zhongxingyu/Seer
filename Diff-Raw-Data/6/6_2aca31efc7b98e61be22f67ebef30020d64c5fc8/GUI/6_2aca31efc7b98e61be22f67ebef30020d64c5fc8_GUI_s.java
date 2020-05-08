 
 package com.myuplay.excelsmasher;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.text.Document;
 
 
 public class GUI extends JPanel implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7632908392074124134L;
 
 	private JButton openFiles, openDirectory, save;
 	private JTextArea console;
 	private static JFrame frame;
 	private FileSystem done;//Last completed FileSystem
 
 	private Console c;
 
 	private GUI(){
 
 		JPanel root = new JPanel();
 		root.setLayout(new BorderLayout());
 
 		JPanel buttonbar = new JPanel();
 		buttonbar.setLayout(new FlowLayout());
 
 		root.add(buttonbar, BorderLayout.NORTH);
 
 		openFiles = new JButton("Open Files");
 		openFiles.setVerticalTextPosition(SwingConstants.CENTER);
 		openFiles.setHorizontalTextPosition(SwingConstants.LEADING);
 		openFiles.setActionCommand("openFiles");
 		openFiles.addActionListener(this);
 
 		openDirectory = new JButton("Open Directory");
 		openDirectory.setVerticalTextPosition(SwingConstants.CENTER);
 		openDirectory.setHorizontalTextPosition(SwingConstants.LEADING);
 		openDirectory.setActionCommand("openDirectory");
 		openDirectory.addActionListener(this);
 
 		save = new JButton("Save");
 		save.setVerticalTextPosition(SwingConstants.CENTER);
 		save.setHorizontalAlignment(SwingConstants.LEADING);
 		save.setActionCommand("save");
 		save.addActionListener(this);
 		save.setEnabled(false);
 
 		console = new JTextArea();
 		console.setEditable(false);
 		console.setWrapStyleWord(true);
 		console.setLineWrap(true);
 		JScrollPane cscroll = new JScrollPane(console);
 		cscroll.setPreferredSize(new Dimension(450,200));
 		cscroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 
 		buttonbar.add(openFiles);
 		buttonbar.add(openDirectory);
 		buttonbar.add(save);
 
 		root.add(cscroll, BorderLayout.SOUTH);
 
 		add(root);
 
 		//Setup universal console.
 		c = new Console(){
 
 			@Override
 			public void print(String s) {
 				console.append(s+"\r\n");
 				Document d = console.getDocument();
 				console.select(d.getLength(), d.getLength());
 			}
 
 		};
 		Console.addPrinter(c);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("openFiles")){
 
 			final JFileChooser in = new JFileChooser();
 			in.addChoosableFileFilter(FileSystem.getFilter());
 			in.setFileFilter(in.getChoosableFileFilters()[1]);
 			in.setAcceptAllFileFilterUsed(false);
 			in.setMultiSelectionEnabled(true);
 			if (in.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
 
 				read(in);
 
 			} else {
 				Console.log("No folder selected.");
 			}
 
 		} else if (e.getActionCommand().equals("openDirectory")) {
 
 			final JFileChooser in = new JFileChooser();
 			in.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			if (in.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
 
 				read(in);
 
 			} else {
 				Console.log("No folder selected.");
 			}
 
 		} else if (e.getActionCommand().equals("save")){
 			JFileChooser out = new JFileChooser();
 			out.setFileFilter(new FileFilter(){
 
 				@Override
 				public boolean accept(File f) {
 					return f.getName().matches(".+\\.xlsx");
 				}
 
 				@Override
 				public String getDescription() {
 					return "Excel OOXML Format (xlsx)";
 				}
 
 			});
 
 			out.setFileFilter(out.getChoosableFileFilters()[1]);
 			out.setAcceptAllFileFilterUsed(false);
 			if (out.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
 				save(out.getSelectedFile());
 			} else {
 				Console.log("Save canceled");
 			}
 		}
 
 		frame.repaint();
 
 	}
 
 	private synchronized void read(final JFileChooser in){
 
 		save.setEnabled(false);
 
 		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
 
 			FileSystem fs;
 
 			@Override
 			protected Void doInBackground() throws Exception {
				fs = new FileSystem(in.getSelectedFiles());
 				Console.log("Parsing files");
 				fs.read();
 				return null;
 			}
 
 			protected void done(){
 				Console.log(fs.getFileCount() + " files parsed.");
 				done = fs;
 				save.setEnabled(true);
 			}
 
 		};
 
 		w.execute();
 	}
 
 	private synchronized void save(final File out){
 
 		Console.log("Saving file. Save button disabled until complete.");
 
 		save.setEnabled(false);
 
 		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>(){
 
 			protected Void doInBackground() throws Exception {
 
 				try{
 					String file = out.getName();
 					if (file.endsWith(".xlsx")){
 						Console.log("Saving to " + file);
 						done.write(out);
 					} else {
 						Console.log("Saving to " + file + ".xlsx");
 						done.write(new File(file + ".xlsx"));
 					}
 				} catch (IOException e){
 					Console.error("An error occured trying to save the file.");
 				}
 
 				return null;
 
 			}
 
 			protected void done(){
 				Console.log("Save completed");
 				save.setEnabled(true);
 			}
 
 		};
 
 		w.execute();
 
 	}
 
 	public static void createAndShowGUI() {
 		frame = new JFrame("Excel Smasher");
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			Console.log("The UI could not find the default system look and feel!");
 		} catch (InstantiationException e) {
 			Console.log("The UI could not create the look and feel.");
 		} catch (IllegalAccessException e) {
 			Console.log("The UI could not access the look and feel.");
 		} catch (UnsupportedLookAndFeelException e) {
 			Console.log("The UI look and feel is unsupported.");
 		}finally{
 			Console.log("The UI will use the default look and feel.");
 		}
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		GUI gui = new GUI();
 		gui.setOpaque(true);
 		frame.setContentPane(gui);
 		frame.setSize(500, 300);
 
 		frame.pack();
 		frame.setVisible(true);
 
 		Console.log("Parser is ready. Please choose the files or directories you would like to parse.");
 	}
 
 }
