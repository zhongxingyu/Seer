 /*
  * Copyright (c) 2001-2002, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lagoon;
 
 import java.io.*;
 import java.util.Properties;
 import java.awt.*;
 import java.awt.event.*;
 
 import org.xml.sax.*;
 
 import nu.staldal.xtree.*;
 
 import nu.staldal.lagoon.core.*;
 
 
 /**
  * A graphical user interface to LagoonProcessor
  *
  * @see nu.staldal.lagoon.core.LagoonProcessor
  */
 public class LagoonGUI extends Frame implements WindowListener
 {
 	private static final boolean DEBUG = false;
 
 	private Panel buttonPanel;
 	private Panel inputPanel;
 	private Panel centerPanel;
 	private Button loadButton;	
 	private Button saveButton;	
 	private Button buildButton;	
 	private Button forceButton;	
 	private Button exitButton;
 	private InputComponent sitemapFile;
 	private InputComponent sourceDir;
 	private InputComponent targetURL;
 	private String password;
 	private Label statusLabel;
 	private TextArea progressArea;
 
 	private LagoonProcessor processor = null;
 	private long sitemapLastModified = 0;	  
 
 	
 	public static void main(String[] args) throws Exception
 	{
 		LagoonGUI appFrame = new LagoonGUI();
 		appFrame.show();
 		if (args.length > 0)
 			appFrame.loadProperties(new File(args[0]));
 	}
 
 	
 	LagoonGUI()
 	{
 		super("Lagoon");
 		addWindowListener(this);
 		
 		add(inputPanel = new Panel(), BorderLayout.NORTH);
 		inputPanel.setLayout(new GridLayout(0,1));
 		inputPanel.add(
 			sitemapFile = 
 				new InputComponent(this, "Sitemap file", "", true));
 		inputPanel.add(
 			sourceDir = new InputComponent(this, "Source directory", "."));
 		inputPanel.add(
 			targetURL = new InputComponent(this, "Target URL", "."));
 		
 		add(centerPanel = new Panel(), BorderLayout.CENTER);
 		centerPanel.setLayout(new BorderLayout());
 		centerPanel.add(progressArea = 
 			new TextArea(null, 5, 50, TextArea.SCROLLBARS_VERTICAL_ONLY), 
 			BorderLayout.NORTH);
 		progressArea.setEditable(false);
 		centerPanel.add(statusLabel = new Label("Not initialized"), 
 			BorderLayout.SOUTH);
 		
 		add(buttonPanel = new Panel(), BorderLayout.SOUTH);
 
 		buttonPanel.add(loadButton = new Button("Load Properties"));
 		loadButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				loadPropertyFile();
 			}
 		});
 
 		buttonPanel.add(saveButton = new Button("Save Properties"));
 		saveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				savePropertyFile();
 			}
 		});
 
 		buttonPanel.add(buildButton = new Button("Build"));
 		buildButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				build(false);
 			}
 		});
 		
 		buttonPanel.add(forceButton = new Button("Force build"));
 		forceButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				build(true);
 			}
 		});
 
 		buttonPanel.add(exitButton = new Button("Exit"));
 		exitButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				exit();
 			}
 		});
 
 		setBackground(Color.lightGray);
 		pack();
 	}
 
 	public void windowOpened(WindowEvent e) {}
 	
 	public void windowClosing(WindowEvent e)
 	{
 		exit();
 	}
 	
 	public void windowClosed(WindowEvent e) {}
 	
 	public void windowIconified(WindowEvent e) {}
 	
 	public void windowDeiconified(WindowEvent e) {}
 	
 	public void windowActivated(WindowEvent e) {}
 	
 	public void windowDeactivated(WindowEvent e) {}	
 		
 	private void build(boolean force)
 	{
 		loadButton.setEnabled(false);
 		saveButton.setEnabled(false);
 		buildButton.setEnabled(false);
 		forceButton.setEnabled(false);
 		sitemapFile.setEnabled(false);
 		sourceDir.setEnabled(false);
 		targetURL.setEnabled(false);
 
 		File sm = new File(sitemapFile.getValue());
 		
 		if ((processor == null) || sitemapFile.hasChanged() 
 				|| sourceDir.hasChanged() || targetURL.hasChanged())
 		{
 			init();
 		}
 		else
 		{
 			if (sm.lastModified() > sitemapLastModified)
 				init();
 		}
 		
 		if (processor != null)
 		{
 			long timeBefore = System.currentTimeMillis();
 			progressArea.setText("--- Start build ---\n");
 			statusLabel.setText("Building...");
             try {
 				boolean success = processor.build(force);
                 long timeElapsed = System.currentTimeMillis()-timeBefore;
 				if (!success)
 				{
 					progressArea.append("--- Build finished with error(s) " 
 						+ showTime(timeElapsed) + " ---");
 					MessageDialog ed = new MessageDialog(this, "Building error",
 						"Error(s) occured while building");
 					ed.show();
 				}
 				else
 				{
 					progressArea.append("--- Build finished successfully " 
 						+ showTime(timeElapsed) + " ---");	
 				}
 			}
 			catch (FileNotFoundException e)
 			{
 				MessageDialog ed = new MessageDialog(this, "Fatal Building error",
 					"File not found: " + e.getMessage());
 				ed.show();
 			}
 			catch (IOException e)
 			{
 				MessageDialog ed = new MessageDialog(this, "Fatal Building error",
 					e.toString());
 				ed.show();
 			}
 			statusLabel.setText("Ready");
 		}
 		else
 		{
 			statusLabel.setText("Not initialized");
 		}
 
 		sitemapLastModified = sm.lastModified();
 					
 		loadButton.setEnabled(true);
 		saveButton.setEnabled(true);
 		buildButton.setEnabled(true);
 		forceButton.setEnabled(true);
 		sitemapFile.setEnabled(true);
 		sourceDir.setEnabled(true);
 		targetURL.setEnabled(true);
 	}
 	
 	private void exit()
 	{
 		try {
 			if (processor != null) processor.destroy();
 		}
 		catch (IOException e)
 		{
 			System.err.println("Error destroying LagoonProcessor: " + e.toString());	
 		}
 		System.exit(0);
 	}
 		
 	private void init()
 	{
 		statusLabel.setText("Initializing...");
 
 		try {
 			if (processor != null) processor.destroy();
 		}
 		catch (IOException e)
 		{
 			System.err.println("Error destroying LagoonProcessor: " + e.toString());	
 		}
 		
 		processor = null;
 		
         try {
 			Element sitemapTree;		
 			try {
 				FileInputStream fis = 
 					new FileInputStream(new File(sitemapFile.getValue()));
 				InputSource is = new InputSource(sitemapFile.getValue());
 				is.setByteStream(fis);
 				
 				sitemapTree = TreeBuilder.parseXML(is, false);
 				
 				fis.close();
 			}
 			catch (SAXException e)
 			{
 				Exception ee = e.getException();
 				if (ee == null)
 				{
 					e.printStackTrace();
 					throw new LagoonException(e.getMessage());
 				}
 				else if (ee instanceof java.io.IOException)
 				{
 					throw (java.io.IOException)ee;
 				}
 				else
 				{
 					ee.printStackTrace();
 					throw new LagoonException(ee.getMessage());
 				}
 			}				
 
 			PrintWriter pw = 
 				new PrintWriter(new TextAreaWriter(progressArea), true); 
 			
             processor = new LagoonProcessor(
 				targetURL.getValue(), sitemapTree, 
 				new File(sourceDir.getValue()), password,
 				pw, pw); 
         }
         catch (AuthenticationMissingException e)
         {
 			PasswordDialog pwd = new PasswordDialog(this, "Password",
 				"Password needed to access target");
 			pwd.show();
 			password = pwd.getPassword();
 			if (password != null) init();
         }
         catch (AuthenticationException e)
         {
 			PasswordDialog pwd = new PasswordDialog(this, "Password",
 				"Invalid password, try again");
 			pwd.show();
 			password = pwd.getPassword();
 			if (password != null) init();
         }
         catch (FileNotFoundException e)
         {
 			MessageDialog ed = new MessageDialog(this, "Initializing error",
             	"File not found: " + e.getMessage());
 			ed.show();
         }
         catch (IOException e)
         {
 			MessageDialog ed = new MessageDialog(this, "Initializing error",
             	e.toString());
 			ed.show();
         }
         catch (LagoonException e)
         {
 			MessageDialog ed = new MessageDialog(this, "Initializing error",
             	e.getMessage());
 			ed.show();
         }
 	}
 
 	
 	private void loadPropertyFile()
 	{
 		FileDialog propertyFileDialog = new FileDialog(this, "Load property file");
 		propertyFileDialog.show(); // blocking						
 		
 		File propertyFile = (propertyFileDialog.getFile() == null) ? null :
 				new File(propertyFileDialog.getDirectory(),
 						 propertyFileDialog.getFile());
 
 		if (propertyFile != null)
 		{
 			loadProperties(propertyFile);
 		}
 	}
 
 
 	private void loadProperties(File propertyFile)
 	{
 		try {			 
 			FileInputStream fis = new FileInputStream(propertyFile);
 			Properties prop = new Properties();
 			prop.load(fis);
 			fis.close();
 
 			sitemapFile.setValue(getProperty(prop, "sitemapFile"));
 			sourceDir.setValue(getProperty(prop, "sourceDir"));
 			targetURL.setValue(getProperty(prop, "targetURL"));
 			password = prop.getProperty("password");
 		}
 		catch (LagoonException e)
 		{
 			MessageDialog ed = new MessageDialog(this, "Error in propertry file", e.getMessage());
 			ed.show(); // blocking
 		}
 		catch (IOException e)
 		{
 			MessageDialog ed = new MessageDialog(this, "Error reading property file",
 				e.toString());
 			ed.show(); // blocking
 		}
 	}
 	
 	
 	private void savePropertyFile()
 	{
 		FileDialog propertyFileDialog = 
 			new FileDialog(this, "Save property file", FileDialog.SAVE);
 		propertyFileDialog.show(); // blocking						
 		
 		File propertyFile = (propertyFileDialog.getFile() == null) ? null :
 				new File(propertyFileDialog.getDirectory(),
 						 propertyFileDialog.getFile());
 
 		if (propertyFile != null)
 		{
 			try {			 
 				FileOutputStream fos = new FileOutputStream(propertyFile);
 				Properties prop = new Properties();
 				prop.setProperty("sitemapFile", sitemapFile.getValue());
 				prop.setProperty("sourceDir", sourceDir.getValue());
 				prop.setProperty("targetURL", targetURL.getValue());
 				if (password != null)
 				{
 					YesNoQueryDialog ynDialog = 
 						new YesNoQueryDialog(this, "Save property file",
 							"Save password in property file?", "Yes", "No");
 					ynDialog.show(); // blocking
 					
 					if (ynDialog.getResult())
 						prop.setProperty("password", password);
 				}
 
 				prop.store(fos, "Lagoon properties");
 				fos.close();
 				statusLabel.setText("Properties saved");
 			}
 			catch (IOException e)
 			{
 				MessageDialog ed = new MessageDialog(this, 
 					"Error writing property file",	e.toString());
 				ed.show(); // blocking
 			}
 		}	
 	}
 	
 
     private static String getProperty(Properties prop, String name)
         throws LagoonException
     {
         String value = prop.getProperty(name);
         if (value == null)
             throw new LagoonException("Property " + name + " not specified");
 
         return value.trim();
     }
 
 
 	private static String showTime(long ms)
 	{
 		if (ms < 10000)
 			return "in " + ms + " ms";
 		else
 			return "in " + ms/1000 + " s";
 	}
 
 }
 
 
 class InputComponent extends Panel
 {
 	private Frame parent;
 	private Label label;
 	private TextField text;
 	private Button button;
 	private boolean textChanged = false;
 	
 
 	public InputComponent(Frame parent, String labelString, String initialValue)
 	{
 		this(parent, labelString, initialValue, false);	
 	}
 
 		
 	public InputComponent(
 		Frame parent, String labelString, String initialValue, boolean file)
 	{
 		this.parent = parent;
 		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
 		add(label = new Label(labelString + ":"));
 		add(text = new TextField(initialValue, 50));
 		text.addTextListener(new TextListener() {
 			public void textValueChanged(TextEvent e) {
 				newText();
 			}
 		});
 		if (file)
 		{
 			add(button = new Button("..."));
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					buttonPressed();
 				}
 			});
 		}
 	}
 	
 	public String getValue()
 	{
 		textChanged = false;
 		return text.getText();	
 	}
 	
 	public void setValue(String value)
 	{
 		text.setText(value);
 	}
 	
 	public boolean hasChanged()
 	{
 		return textChanged;
 	}
 	
 	private void buttonPressed()
 	{
 		FileDialog fd = new FileDialog(parent, label.getText());
 		fd.show(); // blocking
 		if (fd.getFile() != null)
 		{
 			text.setText(new File(fd.getDirectory(), fd.getFile()).getPath());	
 		}		
 	}
 	
 	private void newText()
 	{
 		textChanged = true;
 	}
 }
 
 
 class PasswordDialog extends Dialog
 {
 	private Label theLabel;
 	private Panel pwPanel;
 	private TextField passwordField;
 	private Panel buttonPanel;
 	private Button okButton;
 	private Button cancelButton;
 	
 	private String password;
 	
 	
 	public PasswordDialog(Frame parent, String title, String label)
 	{
 		super(parent, title, true);
 		add(theLabel = new Label(label), BorderLayout.NORTH);
 		add(pwPanel = new Panel(), BorderLayout.CENTER);
 		pwPanel.add(passwordField = new TextField(16));
 		passwordField.setEchoChar('*');
 		add(buttonPanel = new Panel(), BorderLayout.SOUTH);
 		buttonPanel.add(okButton = new Button("OK"));
 		okButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				password = passwordField.getText();
 				hide();
 			}
 		});
 		buttonPanel.add(cancelButton = new Button("Cancel"));
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				password = null;
 				hide();
 			}
 		});
 		pack();
 		Point pl = parent.getLocation();
 		setLocation(pl.x+50, pl.y+50);
 	}
 	
 	public String getPassword()
 	{
 		return password;	
 	}
 }
 
 
 class YesNoQueryDialog extends Dialog
 {
 	private Label theLabel;
 	private Panel buttonPanel;
 	private Button yesButton;
 	private Button noButton;
 	
 	private boolean result;	
 	
 	public YesNoQueryDialog(Frame parent, String title, String label,
 							String yes, String no)
 	{
 		super(parent, title, true);
 		add(theLabel = new Label(label), BorderLayout.NORTH);
 		add(buttonPanel = new Panel(), BorderLayout.SOUTH);
 		buttonPanel.add(yesButton = new Button(yes));
 		yesButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				result = true;
 				hide();
 			}
 		});
 		buttonPanel.add(noButton = new Button(no));
 		noButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				result = false;
 				hide();
 			}
 		});
 		pack();
 		Point pl = parent.getLocation();
 		setLocation(pl.x+50, pl.y+50);
 	}
 	
 	public boolean getResult()
 	{
 		return result;	
 	}
 }
 
 
 class MessageDialog extends Dialog
 {
 	private Label theLabel;
 	private Panel buttonPanel;
 	private Button okButton;
 	
 	public MessageDialog(Frame parent, String title, String msg)
 	{
 		super(parent, title, true);
 		add(theLabel = new Label(msg), BorderLayout.NORTH);
 		add(buttonPanel = new Panel(), BorderLayout.SOUTH);
 		buttonPanel.add(okButton = new Button("OK"));
 		okButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				hide();
 			}
 		});
 		pack();
 		Point pl = parent.getLocation();
 		setLocation(pl.x+50, pl.y+50);
 	}
 }
 
 
 class TextAreaWriter extends Writer
 {
 	private StringBuffer sb;
 	private TextArea ta;
 
 	public TextAreaWriter(TextArea ta)
 	{
 		this.ta = ta;
 		sb = new StringBuffer();
 	}
 	
 	public void write(char[] cbuf, int off, int len)
 	{
 		sb.append(cbuf, off, len);	
 	}
 	
 	public void write(int ch)
 	{
 		sb.append((char)ch);	
 	}
 	
 	public void flush()
 	{
		if (sb.charAt(sb.length()-2) == '\r')
		{
			sb.setCharAt(sb.length()-2, '\n');
			sb.setLength(sb.length()-1);
		}
 		ta.append(sb.toString());
 		sb.setLength(0); // clear buffer
 	}
 	
 	public void close()
 	{
 		flush();
 		// no more to do
 	}
 	
 }
 
