 package jdigest;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.net.URI;
 import java.util.Formatter;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.LayoutStyle;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 public class JDigest extends JFrame
 {
 	private static final long serialVersionUID = 1L;
 
 	private static abstract class Card extends JPanel
 	{
 		private static final long serialVersionUID = 1L;
 
 		protected static final int MAXWIDTH = 500;
 		protected final JDigest parent;
 
 		protected Options getOptions()
 		{
 			return parent.options;
 		}
 
 		public Card(JDigest parent)
 		{
 			this.parent = parent;
 			parent.cardPanel.add(this, this.getClass().toString());
 		}
 
 		public void setCurrent()
 		{
 			((CardLayout)parent.cardPanel.getLayout()).show(
 				parent.cardPanel, this.getClass().toString());
 			parent.current = this;
 		}
 
 		public void backButton()
 		{
 		}
 
 		public void nextButton()
 		{
 		}
 
 		public void cancelButton()
 		{
 			parent.dispose();
 		}
 
 		public void closeButton()
 		{
 			parent.dispose();
 		}
 	}
 
 	private static class StartCard extends Card
 	{
 		private static final long serialVersionUID = 1L;
 
 		private JRadioButton digestButton;
 		private JRadioButton checkButton;
 
 		public StartCard(JDigest parent)
 		{
 			super(parent);
 			digestButton = new JRadioButton("<html><b>Digest</b><br>"
 				+ "Generate a digest file for one or more files</html>");
 			checkButton = new JRadioButton("<html><b>Check</b><br>"
 				+ "Check one or more files against a digest file</html>");
 			ButtonGroup buttonGroup = new ButtonGroup();
 			buttonGroup.add(digestButton);
 			buttonGroup.add(checkButton);
 
 			GroupLayout layout = new GroupLayout(this);
 			setLayout(layout);
 			layout.setAutoCreateGaps(true);
 			layout.setAutoCreateContainerGaps(true);
 			layout.setHorizontalGroup(
 				layout.createParallelGroup()
 					.addComponent(digestButton)
 					.addComponent(checkButton)
 			);
 			layout.setVerticalGroup(
 				layout.createSequentialGroup()
 					.addComponent(digestButton)
 					.addComponent(checkButton)
 			);
 
 			digestButton.addActionListener(new ActionListener()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					updateNextButtonEnabled();
 				}
 			});
 			checkButton.addActionListener(new ActionListener()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					updateNextButtonEnabled();
 				}
 			});
 		}
 
 		public void setCurrent()
 		{
 			super.setCurrent();
 			parent.setTitle("Welcome - JDigest");
 			parent.topTitleLabel.setText(
 				"<html><b>Welcome</b><br>What would you like to do?</html>");
 			parent.backButton.setEnabled(false);
 			parent.nextButton.setText("Next >");
 			if(getOptions().getAction() == Options.Action.digest)
 				digestButton.setSelected(true);
 			else if(getOptions().getAction() == Options.Action.check)
 				checkButton.setSelected(true);
 			updateNextButtonEnabled();
 		}
 
 		private void updateNextButtonEnabled()
 		{
 			parent.nextButton.setEnabled(
 				digestButton.isSelected() || checkButton.isSelected());
 		}
 
 		public void backButton()
 		{
 		}
 
 		public void nextButton()
 		{
 			if(digestButton.isSelected())
 			{
 				getOptions().setAction(Options.Action.digest);
 				parent.digestOptionsCard.setCurrent();
 			}
 			else if(checkButton.isSelected())
 			{
 				getOptions().setAction(Options.Action.check);
 				parent.checkOptionsCard.setCurrent();
 			}
 		}
 	}
 
 	private static class DigestFilePanelCard extends Card
 	{
 		private static final long serialVersionUID = 1L;
 
 		private JXTextField digestFileTextField;
 		private JButton digestFileChangeButton;
 		private JCancellableSelectionComboBox relativizePathsCombo;
 		private JComboBox digestAlgorithmCombo;
 		private JSummarizedComboBox digestFileEncodingCombo;
 		private boolean preventCustomPopUp = false;
 
 		public DigestFilePanelCard(JDigest parent, String fileEncodingHint)
 		{
 			super(parent);
 			JPanel digestFilePanel = new JPanel();
 			digestFilePanel.setBorder(
 				BorderFactory.createTitledBorder("Digest file"));
 			digestFileTextField = new JXTextField("");
 			digestFileChangeButton = new JButton("Choose...");
 			JLabel relativizePathsLabel = new JLabel("Paths:");
 			relativizePathsCombo = new JCancellableSelectionComboBox(
 				new Options.PathRelativizer[] {
 					new Options.AbsolutePathRelativizer(),
 					new Options.ToDigestFilePathRelativizer(getOptions()),
 					new Options.ToCustomFilePathRelativizer("")
 				});
 			relativizePathsCombo.setSelectedItem(null);
 			JLabel digestAlgorithmLabel = new JLabel("Algorithm:");
 			digestAlgorithmCombo = new JComboBox(Options.Algorithm.values());
 			JLabel digestFileEncodingLabel = new JLabel("Encoding:");
 			digestFileEncodingCombo = new JSummarizedComboBox(parent);
 			digestFileEncodingCombo.addItems(Options.Encoding.getFullList());
 			digestFileEncodingCombo.addBasicItems(Options.Encoding.getBasicList(
 				getOptions().getDigestFileEncoding()));
 			JLabel digestFileEncodingHintLabel = new JLabel(fileEncodingHint);
 			digestFileEncodingHintLabel.setPreferredSize(Util.getPreferredSize(
 				digestFileEncodingHintLabel.getText(), true, MAXWIDTH -
 				digestFilePanel.getInsets().left -
 				digestFilePanel.getInsets().right));
 			digestFileEncodingHintLabel.setMinimumSize(
 				digestFileEncodingHintLabel.getPreferredSize());
 			GroupLayout digestFilePanelLayout = new GroupLayout(
 				digestFilePanel);
 			digestFilePanel.setLayout(digestFilePanelLayout);
 			digestFilePanelLayout.setAutoCreateGaps(true);
 			digestFilePanelLayout.setHorizontalGroup
 			(
 				digestFilePanelLayout.createParallelGroup()
 					.addGroup(digestFilePanelLayout.createSequentialGroup()
 						.addComponent(digestFileTextField)
 						.addComponent(digestFileChangeButton)
 					)
 					.addGroup(digestFilePanelLayout.createSequentialGroup()
 						.addGroup(digestFilePanelLayout.createParallelGroup()
 							.addComponent(digestAlgorithmLabel)
 							.addComponent(digestFileEncodingLabel)
 							.addComponent(relativizePathsLabel)
 						)
 						.addGroup(digestFilePanelLayout.createParallelGroup()
 							.addComponent(digestAlgorithmCombo,
 								GroupLayout.PREFERRED_SIZE,
 								GroupLayout.DEFAULT_SIZE,
 								GroupLayout.PREFERRED_SIZE)
 							.addComponent(digestFileEncodingCombo,
 								GroupLayout.PREFERRED_SIZE,
 								GroupLayout.DEFAULT_SIZE,
 								GroupLayout.PREFERRED_SIZE)
 							.addComponent(relativizePathsCombo,
 								GroupLayout.PREFERRED_SIZE,
 								GroupLayout.DEFAULT_SIZE,
 								GroupLayout.PREFERRED_SIZE)
 						)
 					)
 					.addComponent(digestFileEncodingHintLabel)
 			);
 			digestFilePanelLayout.setVerticalGroup(
 				digestFilePanelLayout.createSequentialGroup()
 					.addGroup(digestFilePanelLayout.createParallelGroup(
 					GroupLayout.Alignment.CENTER)
 						.addComponent(digestFileTextField,
 							GroupLayout.PREFERRED_SIZE,
 							GroupLayout.DEFAULT_SIZE,
 							GroupLayout.PREFERRED_SIZE)
 						.addComponent(digestFileChangeButton)
 					)
 					.addGroup(digestFilePanelLayout.createParallelGroup(
 					GroupLayout.Alignment.CENTER)
 						.addComponent(digestAlgorithmLabel)
 						.addComponent(digestAlgorithmCombo,
 							GroupLayout.PREFERRED_SIZE,
 							GroupLayout.DEFAULT_SIZE,
 							GroupLayout.PREFERRED_SIZE)
 					)
 					.addGroup(digestFilePanelLayout.createParallelGroup(
 					GroupLayout.Alignment.CENTER)
 						.addComponent(digestFileEncodingLabel)
 						.addComponent(digestFileEncodingCombo,
 							GroupLayout.PREFERRED_SIZE,
 							GroupLayout.DEFAULT_SIZE,
 							GroupLayout.PREFERRED_SIZE)
 					)
 					.addGroup(digestFilePanelLayout.createParallelGroup(
 					GroupLayout.Alignment.CENTER)
 						.addComponent(relativizePathsLabel)
 						.addComponent(relativizePathsCombo,
 							GroupLayout.PREFERRED_SIZE,
 							GroupLayout.DEFAULT_SIZE,
 							GroupLayout.PREFERRED_SIZE)
 					)
 					.addComponent(digestFileEncodingHintLabel)
 					.addGap(0, 0, Short.MAX_VALUE)
 			);
 			setLayout(new BorderLayout(
 				0,
 				Util.getDefaultGap(digestFilePanel, digestFilePanel,
 					LayoutStyle.ComponentPlacement.RELATED,
 					SwingConstants.NORTH, this)
 				));
 			setBorder(Util.createEmptyBorder(digestFilePanel, this));
 			add(digestFilePanel, BorderLayout.CENTER);
 
 			digestFileEncodingHintLabel.addMouseListener(new MouseAdapter()
 			{
 				public void mouseClicked(MouseEvent e)
 				{
 					if(Desktop.isDesktopSupported())
 					{
 						Desktop desktop = Desktop.getDesktop();
 						if(desktop.isSupported(Desktop.Action.BROWSE))
 						try
 						{
 							desktop.browse(new URI(
 							"http://en.wikipedia.org/wiki/Character_encoding"));
 						}
 						catch(Exception ex)
 						{
 						}
 					}
 				}
 			});
 			digestFileChangeButton.addActionListener(new ActionListener()
 			{			
 				public void actionPerformed(ActionEvent e)
 				{
 					JFileChooser fileChooser = new JFileChooser();
 					Options.Algorithm[] values = Options.Algorithm.values();
 					String[] extensions = new String[values.length];
 					String description = "";
 					for(int i = 0; i < values.length; i++)
 					{
 						extensions[i] = values[i].extension;
 						description += (description.length() == 0? "": "; ") +
 							"*." + values[i].extension;
 					}
 					description = "Digest files (" + description + ")";
 
 					fileChooser.addChoosableFileFilter(
 						new FileNameExtensionFilter(description, extensions));
 					int retval = fileChooser.showOpenDialog(
 						DigestFilePanelCard.this.parent);
 					if(retval == JFileChooser.APPROVE_OPTION)
 					{
 						File file = fileChooser.getSelectedFile();
 						digestFileTextField.setText(file.getAbsolutePath());
 						Options.Algorithm algorithm =
 							Options.Algorithm.byExtension(file.getName());
 						digestAlgorithmCombo.setSelectedItem(algorithm);
 					}
 				}
 			});
 			digestFileTextField.addTextListener(new JXTextField.TextListener()
 			{
 				public void textChanged()
 				{
 					Options.Algorithm algorithm = Options.Algorithm.byExtension(
 						digestFileTextField.getText());
 					digestAlgorithmCombo.setSelectedItem(algorithm);			
 				}
 			});
 			digestAlgorithmCombo.addActionListener(new ActionListener()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					Options.Algorithm algorithm = (Options.Algorithm)
 						digestAlgorithmCombo.getSelectedItem();
 					if(algorithm != null)
 					{
 						String digestFile = digestFileTextField.getText();
 						int i = digestFile.lastIndexOf(".");
 						if(i > 0)
 							digestFile = digestFile.substring(0, i);
 						digestFileTextField.setText(
 							digestFile + "." + algorithm.extension);
 					}
 				}
 			});
 			relativizePathsCombo.addActionListener(new ActionListener()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					if(relativizePathsCombo.getSelectedIndex() == 2
 					&& !preventCustomPopUp)
 					{
 						SwingUtilities.invokeLater(new Runnable()
 						{
 							public void run()
 							{
 								JFileChooser fileChooser = new JFileChooser();
 								fileChooser.setFileSelectionMode(
 									JFileChooser.DIRECTORIES_ONLY);
 								int retval = fileChooser.showOpenDialog(
 									DigestFilePanelCard.this);
 								if(retval == JFileChooser.APPROVE_OPTION)
 								{
 									((Options.ToCustomFilePathRelativizer)
 									relativizePathsCombo.getItemAt(2)).setBase(
 										fileChooser.getSelectedFile()
 									);
 								}
 								else
 									relativizePathsCombo.undoSelectionLater();
 							}
 						});
 					}
 				}
 			});
 		}
 
 		public void setCurrent()
 		{
 			super.setCurrent();
 			digestFileTextField.setText(getOptions().getDigestFile());
 			digestAlgorithmCombo.setSelectedItem(
 				getOptions().getDigestAlgorithm());
 			digestFileEncodingCombo.setSelectedItem(
 				getOptions().getDigestFileEncoding());
 			Options.PathRelativizer pathRelativizer =
 				getOptions().getPathRelativizer();
 			if(pathRelativizer instanceof Options.AbsolutePathRelativizer)
 				relativizePathsCombo.setSelectedIndex(0);
 			else if(pathRelativizer
 			instanceof Options.ToDigestFilePathRelativizer)
 				relativizePathsCombo.setSelectedIndex(1);
 			else if(pathRelativizer
 			instanceof Options.ToCustomFilePathRelativizer)
 			{
 				preventCustomPopUp = true;
 				relativizePathsCombo.setSelectedIndex(2);
 				preventCustomPopUp = false;
 				((Options.ToCustomFilePathRelativizer)
 					relativizePathsCombo.getItemAt(2)).setBase(
 						((Options.ToCustomFilePathRelativizer)pathRelativizer)
 							.getBase());
 			}
 		}
 
 		public void nextButton()
 		{
 			getOptions().setDigestFile(digestFileTextField.getText());
 			getOptions().setDigestAlgorithm(
 				(Options.Algorithm)digestAlgorithmCombo.getSelectedItem());
 			getOptions().setDigestFileEncoding(
 				(Options.Encoding)digestFileEncodingCombo.getSelectedItem());
 			getOptions().setPathRelativizer((Options.PathRelativizer)
 				relativizePathsCombo.getSelectedItem());
 			try
 			{
 				getOptions().validate();
 				parent.processCard.setCurrent();
 			}
 			catch(Options.OptionsException e)
 			{
 				JOptionPane.showMessageDialog(this, e.getMessage(),
 					"Error", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	private static class DigestOptionsCard extends DigestFilePanelCard
 	{
 		private static final long serialVersionUID = 1L;
 
 		private String[] inputFiles;
 		private JLabel inputFilesLabel;
 		private JButton inputFilesChangeButton;
 
 		public DigestOptionsCard(JDigest parent)
 		{
 			super(parent,
 				"<html>Use the system default encoding unless " +
 				"your file names contain characters not displayable in this " +
 				"system's (or the system in which you intend to verify the " +
 				"checksum's) default codepage. Other software may not be " +
 				"able to read the generated digest file. <a href=\"link\">" +
 				"Learn more...</a></html>");
 
 			inputFilesLabel = new JLabel("(no files selected)");
 			//this label can get wide, so let's set a very short preferred width
 			inputFilesLabel.setPreferredSize(new Dimension(10,
 				inputFilesLabel.getPreferredSize().height));
 			inputFilesChangeButton = new JButton("Choose...");
 			JPanel inputFilesPanel = new JPanel(new BorderLayout());
 			inputFilesPanel.add(inputFilesLabel, BorderLayout.CENTER);
 			inputFilesPanel.add(inputFilesChangeButton, BorderLayout.EAST);
 			inputFilesPanel.setBorder(
 				BorderFactory.createTitledBorder("Files / folders to digest"));
 			add(inputFilesPanel, BorderLayout.NORTH);
 
 			inputFilesChangeButton.addActionListener(new ActionListener()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					inputFiles = new FileListDialog(inputFiles).getFiles();
 					updateInputFilesLabel();
 				}
 			});
 		}
 
 		private void updateInputFilesLabel()
 		{
 			String text = "";
 			for(String file: inputFiles)
 				text += (text.length() == 0? "": ", ") + file;
 			if(text.length() == 0)
 				inputFilesLabel.setText("(no files selected)");
 			else
 				inputFilesLabel.setText(text);
 		}
 
 		public void setCurrent()
 		{
 			super.setCurrent();
 			parent.setTitle("Digest options - JDigest");
 			parent.topTitleLabel.setText("<html><b>Digest options</b>"
 				+ "<br>Please select the desired options</html>");
 			parent.backButton.setEnabled(true);
 			parent.nextButton.setText("Start");
 			parent.nextButton.setEnabled(true);
 			parent.cancelButton.setText("Cancel");
 
 			inputFiles = getOptions().getFiles();
 			updateInputFilesLabel();
 		}
 
 		public void backButton()
 		{
 			parent.startCard.setCurrent();
 		}
 
 		public void nextButton()
 		{
 			getOptions().setFiles(inputFiles);
 			super.nextButton();
 		}
 	}
 
 	private static class CheckOptionsCard extends DigestFilePanelCard
 	{
 		private static final long serialVersionUID = 1L;
 
 		public CheckOptionsCard(JDigest parent)
 		{
 			super(parent,
 				"<html>Select the algorithm, encoding and path processing " +
 				"method matching the digest file. " +
 				"<a href=\"link\">Learn more...</a></html></html>");
 		}
 
 		public void setCurrent()
 		{
 			super.setCurrent();
 			parent.setTitle("Check options - JDigest");
 			parent.topTitleLabel.setText("<html><b>Check options</b>"
 				+ "<br>Please select the desired options</html>");
 			parent.backButton.setEnabled(true);
 			parent.nextButton.setText("Start");
 			parent.nextButton.setEnabled(true);
 			parent.cancelButton.setText("Cancel");
 		}
 
 		public void backButton()
 		{
 			parent.startCard.setCurrent();
 		}
 
 		public void nextButton()
 		{
 			super.nextButton();
 		}
 	}
 
 	private static class ProcessCard extends Card
 	{
 		private static final long serialVersionUID = 1L;
 
 		private Logger logger;
 		private JLabel totalProgressLabel;
 		private JXProgressBar totalProgressBar;
 		private JLabel fileProgressLabel;
 		private JXProgressBar fileProgressBar;
 		private String currentFile;
 		private long totalFileCount;
 		private long totalSize;
 		private long doneFileCount;
 		private long doneSize;
 		private long fileSize;
 		private boolean taskDone;
 		private boolean closePending;
 
 		private BasicTask task;
 
 		public ProcessCard(JDigest parent)
 		{
 			super(parent);
 
 			totalProgressLabel = new JLabel(" ");
 			totalProgressBar = new JXProgressBar(1000*60*10, 0, 0);
 			fileProgressLabel = new JLeftClippingLabel(" ");
 			fileProgressBar = new JXProgressBar(1000*10, 0, 0);
 
 			logger = new Logger();
 			JScrollPane scrollPane = new JScrollPane(logger);
 			logger.setFillsViewportHeight(true);
 			logger.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
 			GroupLayout layout = new GroupLayout(this);
 			setLayout(layout);
 			layout.setAutoCreateGaps(true);
 			layout.setAutoCreateContainerGaps(true);
 			layout.setHorizontalGroup(
 				layout.createParallelGroup()
					.addComponent(totalProgressLabel)
 					.addComponent(totalProgressBar)
					.addComponent(fileProgressLabel, 10,
						GroupLayout.DEFAULT_SIZE, MAXWIDTH)
 					.addComponent(fileProgressBar)
 					.addComponent(scrollPane)
 			);
 			layout.setVerticalGroup(
 				layout.createSequentialGroup()
 				.addComponent(totalProgressLabel)
 				.addComponent(totalProgressBar)
 				.addComponent(fileProgressLabel)
 				.addComponent(fileProgressBar)
 				.addComponent(scrollPane,
 					scrollPane.getMinimumSize().height,
 					scrollPane.getMinimumSize().height,
 					Short.MAX_VALUE)
 			);
 		}
 
 		public void setCurrent()
 		{
 			super.setCurrent();
 			parent.setTitle("Analyzing - JDigest");
 			parent.topTitleLabel.setText("<html><b>Processing</b><br>Please "
 				+ "wait while the requested operation is carried out</html>");
 			parent.backButton.setEnabled(false);
 			parent.nextButton.setEnabled(false);
 
 			currentFile = null;
 			totalFileCount = 0;
 			totalSize = 0;
 			doneFileCount = 0;
 			doneSize = 0;
 			fileSize = 0;
 			taskDone = false;
 			closePending = false;
 			logger.clear();
 			task = TaskFactory.createTask(getOptions(), logger);
 			task.addPropertyChangeListener(new PropertyChangeListener()
 			{
 				public void propertyChange(PropertyChangeEvent e)
 				{
 					if("fileProgress".equals(e.getPropertyName()))
 					{
 						long fileProgress = (Long)e.getNewValue(); 
 						fileProgressBar.update(fileProgress);
 						if(fileProgress == 0)
 							fileProgressLabel.setText(new Formatter().format(
 								"%s: %s of %s",
 								currentFile,
 								Util.getByteString(fileProgress),
 								Util.getByteString(fileSize)).toString());
 						else
 							fileProgressLabel.setText(new Formatter().format(
 								"%s: %s - %s of %s (%s/sec)",
 								currentFile,
 								fileProgressBar.getInstantETAAsString(),
 								Util.getByteString(fileProgress),
 								Util.getByteString(fileSize),
 								Util.getByteString(
 									fileProgressBar.getInstantSpeed()))
 								.toString());
 					}
 					else if("doneSize".equals(e.getPropertyName()))
 					{
 						doneSize = (Long)e.getNewValue();
 						totalProgressBar.update(doneSize);
 						parent.setTitle(new Formatter().format(
 							"%.0f%% - %s - JDigest",
 							100* totalProgressBar.getPercentComplete(),
 							task.getStatus()
 						).toString());
 						updateTotalProgressLabel();
 					}
 					else if("fileOrFolderName".equals(e.getPropertyName()))
 					{
 						currentFile = (String)e.getNewValue();
 						fileProgressLabel.setText(currentFile);
 					}
 					else if("fileSize".equals(e.getPropertyName()))
 					{
 						fileSize = (Long)e.getNewValue();
 						fileProgressBar.reset(0, fileSize);
 					}
 					else if("doneFileCount".equals(e.getPropertyName()))
 					{
 						doneFileCount = (Long)e.getNewValue();
 						updateTotalProgressLabel();
 					}
 					else if("totalFileCount".equals(e.getPropertyName()))
 					{
 						totalFileCount = (Long)e.getNewValue();
 						totalProgressLabel.setText(new Formatter().format(
 								"Analyzing... (%,d files found so far)",
 								totalFileCount).toString());
 					}
 					else if("totalSize".equals(e.getPropertyName()))
 					{
 						totalSize = (Long)e.getNewValue();
 						totalProgressBar.reset(0, totalSize);
 					}
 					else if("done".equals(e.getPropertyName()))
 					{
 						taskDone = (Boolean)e.getNewValue();
 						if(taskDone)
 						{
 							if(closePending)
 								parent.dispose();
 							else
 							{
 								parent.backButton.setEnabled(true);
 								parent.cancelButton.setText("Close");
 								parent.cancelButton.setEnabled(true);
 							}
 						}
 					}
 				}
 			});
 			task.execute();
 		}
 
 		private void updateTotalProgressLabel()
 		{
 			totalProgressLabel.setText(new Formatter().format(
 				"Total: %s - %,d of %,d files / %s of %s (%s/sec)",
 				totalProgressBar.getAverageETAAsString(),
 				doneFileCount, totalFileCount,
 				Util.getByteString(doneSize), Util.getByteString(totalSize),
 				Util.getByteString(totalProgressBar.getAverageSpeed()))
 				.toString());
 		}
 
 		public void backButton()
 		{
 			if(getOptions().getAction() == Options.Action.digest)
 				parent.digestOptionsCard.setCurrent();
 			else if(getOptions().getAction() == Options.Action.check)
 				parent.checkOptionsCard.setCurrent();
 		}
 
 		public void closeButton()
 		{
 			closePending = true;
 			cancelButton();
 		}
 
 		public void cancelButton()
 		{
 			if(taskDone)
 				parent.dispose();
 			else
 			{
 				task.cancel(false);
 				logger.Append(new Logger.Message(Logger.Message.Type.NFO,
 					"Cancelling..."));
 				parent.cancelButton.setEnabled(false);
 			}
 		}
 	}
 
 	private Options options;
 	private JPanel cardPanel;
 	private Card startCard;
 	private Card digestOptionsCard;
 	private Card checkOptionsCard;
 	private Card processCard;
 	private Card current;
 	private JButton backButton;
 	private JButton nextButton;
 	private JButton cancelButton;
 	private JLabel topTitleLabel;
 
 	public JDigest(Options options)
 	{
 		this.options = options;
 
 		JPanel topPanel = new JPanel();
 		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
 		topTitleLabel = new JLabel("<html><b>Title</b><br>Subtitle</html>");
 		topTitleLabel.setBorder(Util.createEmptyBorder(topTitleLabel, topPanel));
 		topPanel.add(topTitleLabel);
 		topPanel.add(new JSeparator());
 		topPanel.setBackground(new Color(255, 255, 255));
 
 		JPanel bottomPanel = new JPanel();
 		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
 
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
 		backButton = new JButton("< Back");
 		nextButton = new JButton("Next >");
 		cancelButton = new JButton("Cancel");
 		Dimension max = new Dimension(
 			Math.max(Math.max(
 				backButton.getPreferredSize().width,
 				nextButton.getPreferredSize().width),
 				cancelButton.getPreferredSize().width),
 			Math.max(Math.max(
 				backButton.getPreferredSize().height,
 				nextButton.getPreferredSize().height),
 				cancelButton.getPreferredSize().height)
 		);
 		backButton.setPreferredSize(max);
 		nextButton.setPreferredSize(max);
 		cancelButton.setPreferredSize(max);
 		buttonPanel.add(backButton);
 		buttonPanel.add(nextButton);
 		buttonPanel.add(Util.createHorizontalRigidArea(
 			nextButton, cancelButton, buttonPanel));
 		buttonPanel.add(cancelButton);
 		buttonPanel.setBorder(Util.createEmptyBorder(cancelButton, buttonPanel));
 
 		bottomPanel.add(new JSeparator());
 		bottomPanel.add(buttonPanel);
 
 		cardPanel = new JPanel(new CardLayout());
 		startCard = new StartCard(this);
 		digestOptionsCard = new DigestOptionsCard(this);
 		checkOptionsCard = new CheckOptionsCard(this);
 		processCard = new ProcessCard(this);
 
 		add(topPanel, BorderLayout.NORTH);
 		add(cardPanel, BorderLayout.CENTER);
 		add(bottomPanel, BorderLayout.SOUTH);
 
 		backButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				current.backButton();
 			}
 		});
 		nextButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				current.nextButton();
 			}
 		});
 		cancelButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				current.cancelButton();
 			}
 		});
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter()
 		{
 			public void windowClosing(WindowEvent e)
 			{
 				current.closeButton();
 			}
 		});
 
 		if(options.getAction() == Options.Action.digest)
 			if(options.getShowOptionsWindow())
 				digestOptionsCard.setCurrent();
 			else
 				processCard.setCurrent();
 		else if(options.getAction() == Options.Action.check)
 			if(options.getShowOptionsWindow())
 				checkOptionsCard.setCurrent();
 			else
 				processCard.setCurrent();
 		else
 			startCard.setCurrent();
 
 		pack();
 		setResizable(false);
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch(Exception e)
 		{
 		}
 
 		try
 		{
 			Options options = new Options(args);
 			new JDigest(options);
 		}
 		catch(Options.OptionsException e)
 		{
 			System.err.println(e.getMessage());
 			System.err.println(Options.getUsageString());
 		}
 	}
 }
