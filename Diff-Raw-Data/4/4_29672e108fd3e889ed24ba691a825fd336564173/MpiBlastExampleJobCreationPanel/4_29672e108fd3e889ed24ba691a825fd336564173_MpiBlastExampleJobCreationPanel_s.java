 package org.vpac.grisu.frontend.view.swing;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.BatchJobException;
 import org.vpac.grisu.frontend.model.job.BatchJobObject;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.frontend.view.swing.files.GrisuFileDialog;
 import org.vpac.grisu.frontend.view.swing.jobcreation.JobCreationPanel;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.UserEnvironmentManager;
 import org.vpac.grisu.model.files.GlazedFile;
 import org.vpac.historyRepeater.HistoryManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class MpiBlastExampleJobCreationPanel extends JPanel implements
 		JobCreationPanel, PropertyChangeListener {
 
 	static final Logger myLogger = Logger
 			.getLogger(MpiBlastExampleJobCreationPanel.class.getName());
 
 	static final int DEFAULT_WALLTIME = 3600 * 24;
 
 	private ServiceInterface si;
 	private JLabel jobnameLabel;
 	private JTextField jobnameField;
 	private JLabel lblProgram;
 	private JComboBox programCombobox;
 	private JLabel lblDatabase;
 	private JComboBox databaseComboBox;
 	private JLabel lblFastaFile;
 	private JComboBox inputFileComboBox;
 	private JButton btnBrowse;
 	private JLabel lblOfJobs;
 	private JSlider slider;
 
 	private BatchJobObject currentBatchJob;
 
 	private GrisuFileDialog dialog;
 
 	private GlazedFile currentFile;
 	private List<List<String>> currentParsedFastaInput;
 	private FileManager fm;
 	private UserEnvironmentManager uem;
 	private HistoryManager hm;
 	private JLabel noJobsLabel;
 	private JButton submitButton;
 	private JobSubmissionLogPanel jobSubmissionLogPanel;
 	private Thread subThread;
 
 	private static final NumberFormat formatter = new DecimalFormat("0000");
 
 	/**
 	 * Create the panel.
 	 */
 	public MpiBlastExampleJobCreationPanel() {
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(16dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getLblFastaFile(), "2, 2, right, default");
 		add(getInputFileComboBox(), "4, 2, 5, 1, fill, default");
 		add(getBtnBrowse(), "10, 2");
 		add(getJobnameLabel(), "2, 4, right, default");
 		add(getJobnameField(), "4, 4, 7, 1, fill, default");
 		add(getLblProgram(), "2, 6, right, default");
 		add(getProgramCombobox(), "4, 6, fill, default");
 		add(getLblDatabase(), "6, 6, right, default");
 		add(getDatabaseComboBox(), "8, 6, 3, 1, fill, default");
 		add(getLblOfJobs(), "2, 10, right, default");
 		add(getSlider(), "4, 10, 5, 1");
 		add(getNoJobsLabel(), "10, 10, center, default");
 		add(getSubmitButton(), "10, 12");
 		add(getJobSubmissionLogPanel(), "2, 14, 9, 1, fill, fill");
 
 	}
 
 	private void cleanUpUI() {
 
 		getInputFileComboBox().setSelectedItem(null);
 		getSlider().setEnabled(false);
 		getSlider().setMaximum(1);
 		getNoJobsLabel().setText("n/a");
 		getSubmitButton().setText("Submit");
 		getJobSubmissionLogPanel().clear();
 		getJobnameField().setText("");
 		currentParsedFastaInput = null;
 		currentFile = null;
 
 	}
 
 	public boolean createsBatchJob() {
 		return true;
 	}
 
 	public boolean createsSingleJob() {
 		return false;
 	}
 
 	private JButton getBtnBrowse() {
 		if (btnBrowse == null) {
 			btnBrowse = new JButton("Browse");
 			btnBrowse.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 
 					if (si == null) {
 						myLogger.error("ServiceInterface not set yet.");
 						return;
 					}
 
 					GlazedFile file = popupFileDialogAndAskForFile();
 
 					if (file == null) {
 						return;
 					}
 
 					setInputFile(file);
 
 				}
 			});
 		}
 		return btnBrowse;
 	}
 
 	private JComboBox getDatabaseComboBox() {
 		if (databaseComboBox == null) {
 			databaseComboBox = new JComboBox();
 			databaseComboBox.setModel(new DefaultComboBoxModel(new String[] {
 					"nt", "nr" }));
 		}
 		return databaseComboBox;
 	}
 
 	public GrisuFileDialog getFileDialog() {
 
 		if (si == null) {
 			myLogger.error("Serviceinterface not set yet...");
 			return null;
 		}
 
 		if (dialog == null) {
 			String url = hm
 					.getLastEntry("MPIBLAST_EXAMPLE_LAST_INPUT_FILE_DIR");
 			if (StringUtils.isBlank(url)) {
 				url = new File(System.getProperty("user.home")).toURI()
 						.toString();
 			}
 			dialog = new GrisuFileDialog(si, url);
 		}
 		return dialog;
 	}
 
 	private JComboBox getInputFileComboBox() {
 		if (inputFileComboBox == null) {
 			inputFileComboBox = new JComboBox();
 			inputFileComboBox.setEditable(true);
 		}
 		return inputFileComboBox;
 	}
 
 	private JTextField getJobnameField() {
 		if (jobnameField == null) {
 			jobnameField = new JTextField();
 			jobnameField.setColumns(10);
 		}
 		return jobnameField;
 	}
 
 	private JLabel getJobnameLabel() {
 		if (jobnameLabel == null) {
 			jobnameLabel = new JLabel("Jobname:");
 		}
 		return jobnameLabel;
 	}
 
 	private JobSubmissionLogPanel getJobSubmissionLogPanel() {
 		if (jobSubmissionLogPanel == null) {
 			jobSubmissionLogPanel = new JobSubmissionLogPanel();
 		}
 		return jobSubmissionLogPanel;
 	}
 
 	private JLabel getLblDatabase() {
 		if (lblDatabase == null) {
 			lblDatabase = new JLabel("Database:");
 		}
 		return lblDatabase;
 	}
 
 	private JLabel getLblFastaFile() {
 		if (lblFastaFile == null) {
 			lblFastaFile = new JLabel("Fasta file:");
 		}
 		return lblFastaFile;
 	}
 
 	private JLabel getLblOfJobs() {
 		if (lblOfJobs == null) {
 			lblOfJobs = new JLabel("# of jobs");
 		}
 		return lblOfJobs;
 	}
 
 	private JLabel getLblProgram() {
 		if (lblProgram == null) {
 			lblProgram = new JLabel("Program:");
 		}
 		return lblProgram;
 	}
 
 	private JLabel getNoJobsLabel() {
 		if (noJobsLabel == null) {
 			noJobsLabel = new JLabel("n/a");
 		}
 		return noJobsLabel;
 	}
 
 	public JPanel getPanel() {
 		return this;
 	}
 
 	public String getPanelName() {
 		return "MPIBlast batch";
 	}
 
 	private JComboBox getProgramCombobox() {
 		if (programCombobox == null) {
 			programCombobox = new JComboBox();
 			programCombobox.setModel(new DefaultComboBoxModel(new String[] {
 					"blastn", "blastp", "blastx", "tblastn", "tblastx" }));
 		}
 		return programCombobox;
 	}
 
 	private JSlider getSlider() {
 		if (slider == null) {
 			slider = new JSlider();
 			slider.setPaintTicks(true);
 			slider.setPaintLabels(true);
 			slider.setEnabled(false);
 			slider.setMinimum(1);
 			slider.setMaximum(1);
 			slider.addChangeListener(new ChangeListener() {
 
 				public void stateChanged(ChangeEvent e) {
 
 					getNoJobsLabel().setText(
 							new Integer(slider.getValue()).toString());
 
 				}
 			});
 		}
 		return slider;
 	}
 
 	private JButton getSubmitButton() {
 		if (submitButton == null) {
 			submitButton = new JButton("Submit");
 			submitButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 
 					if ("Submit".equals(submitButton.getText())) {
 
 						if (subThread != null) {
 							subThread.interrupt();
 						}
 
 						subThread = new Thread() {
 							@Override
 							public void run() {
 
 								try {
 									submitJob();
 								} catch (BatchJobException e) {
 
 									e.printStackTrace();
 									cleanUpUI();
 								}
 							}
 						};
 						subThread.start();
 						submitButton.setText("Cancel");
 					} else if ("Cancel".equals(submitButton.getText())) {
 						subThread.interrupt();
 						submitButton.setText("Ok");
 					} else if ("Ok".equals(submitButton.getText())) {
 
 						cleanUpUI();
 
 					}
 
 				}
 
 			});
 		}
 		return submitButton;
 	}
 
 	public String getSupportedApplication() {
 		return "mpiBlast";
 	}
 
 	private void parseFastaFile() {
 
 		List<String> currentFastaInput;
 
 		try {
 			currentFastaInput = FileUtils.readLines(fm
 					.getLocalCacheFile(currentFile.getUrl()));
 
 			Iterator<String> it = currentFastaInput.iterator();
 			while (it.hasNext()) {
 				String line = it.next();
				if (StringUtils.isBlank(line)) {
 					it.remove();
 				}
 			}
 
 			currentParsedFastaInput = new LinkedList<List<String>>();
 			List<String> currentPart = null;
 			for (String line : currentFastaInput) {
 				if (line.startsWith(">")) {
 					if (currentPart != null && currentPart.size() > 0) {
 						currentParsedFastaInput.add(currentPart);
 					}
 					currentPart = new LinkedList<String>();
 				}
 				if (currentPart == null) {
 					throw new IllegalArgumentException(
 							"Can't parse fasta file: "
 									+ line
 									+ " doesn't start or doesn't belong to another line that starts with >");
 				}
 				currentPart.add(line);
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	protected GlazedFile popupFileDialogAndAskForFile() {
 
 		getFileDialog().setVisible(true);
 
 		GlazedFile file = getFileDialog().getSelectedFile();
 		getFileDialog().clearSelection();
 
 		GlazedFile currentDir = getFileDialog().getCurrentDirectory();
 
 		hm.addHistoryEntry("MPIBLAST_EXAMPLE_LAST_INPUT_FILE_DIR", currentDir
 				.getUrl());
 
 		return file;
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 
 		if (evt.getPropertyName().equals(BatchJobObject.SUBMITTING)) {
 			if ((Boolean) (evt.getOldValue()) && !(Boolean) (evt.getNewValue())) {
 				getSubmitButton().setText("Ok");
 			}
 		}
 	}
 
 	private void setInputFile(GlazedFile file) {
 
 		currentFile = file;
 		getInputFileComboBox().setSelectedItem(file.getUrl());
 
 		if (currentFile == null) {
 			return;
 		}
 
 		getSlider().setEnabled(true);
 
 		parseFastaFile();
 
 		getSlider().setMaximum(currentParsedFastaInput.size());
 
 		getJobnameField().setText(
 				uem.calculateUniqueJobname(currentFile
 						.getNameWithoutExtension()
 						+ "_job"));
 
 	}
 
 	public void setServiceInterface(ServiceInterface si) {
 
 		this.si = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 		this.uem = GrisuRegistryManager.getDefault(si)
 				.getUserEnvironmentManager();
 		this.hm = GrisuRegistryManager.getDefault(si).getHistoryManager();
 	}
 
 	public void submitJob() throws BatchJobException {
 
 		if (currentBatchJob != null) {
 			currentBatchJob.removePropertyChangeListener(this);
 		}
 
 		currentBatchJob = new BatchJobObject(si, getJobnameField().getText(),
 				"/ACC", "mpiblast", "1.5.0");
 
 		currentBatchJob.addPropertyChangeListener(this);
 
 		getJobSubmissionLogPanel().setBatchJob(currentBatchJob);
 
 		Map<String, List<List<String>>> inputFiles = new LinkedHashMap<String, List<List<String>>>();
 
 		int noJobs = getSlider().getValue();
 
 		Double linesPerJobD = new Double(currentParsedFastaInput.size())
 				/ new Double(noJobs);
 
 		int linesPerJob = new Long(Math.round(linesPerJobD + 0.499999))
 				.intValue();
 
 		for (int i = 0; i < currentParsedFastaInput.size(); i = i + linesPerJob) {
 			int end = i + linesPerJob;
 			if (end > currentParsedFastaInput.size()) {
 				end = currentParsedFastaInput.size();
 			}
 			List<List<String>> tempList = currentParsedFastaInput.subList(i,
 					end);
 			inputFiles.put("line" + formatter.format(i) + "-line"
 					+ formatter.format(end - 1), tempList);
 		}
 
 		for (String jobname : inputFiles.keySet()) {
 
 			// create temp file
 			String inputFIlename = jobname + "_" + currentBatchJob.getJobname();
 			File tempFile = new File(System.getProperty("java.io.tmpdir"),
 					inputFIlename);
 			tempFile.delete();
 			try {
 				List<List<String>> all = inputFiles.get(jobname);
 				List<String> consolidated = new LinkedList<String>();
 				for (List<String> temp : all) {
 					consolidated.addAll(temp);
 				}
 				FileUtils.writeLines(tempFile, consolidated);
 			} catch (IOException e) {
 				throw new BatchJobException(e);
 			}
 
 			JobObject tempJob = new JobObject(si);
 			tempJob.setJobname(uem.calculateUniqueJobname(jobname + "_"
 					+ currentBatchJob.getJobname()));
 			tempJob.addInputFileUrl(tempFile.toURI().toString());
 			tempJob.setApplication("mpiblast");
 			tempJob.setApplicationVersion("1.5.0");
 			// tempJob.setWalltimeInSeconds(604800);
 			tempJob.setWalltimeInSeconds(DEFAULT_WALLTIME);
 			String commandline = "mpiblast -p blastp -d nr -i " + inputFIlename
 					+ " -o " + jobname + "out.txt";
 			tempJob.setCommandline(commandline);
 			tempJob.setForce_mpi(true);
 			tempJob.setCpus(8);
 
 			currentBatchJob.addJob(tempJob);
 		}
 
 		currentBatchJob.setDefaultNoCpus(8);
 		// currentBatchJob.setDefaultWalltimeInSeconds(604800);
 		currentBatchJob.setDefaultWalltimeInSeconds(DEFAULT_WALLTIME);
 
 		try {
 			currentBatchJob.prepareAndCreateJobs(true);
 		} catch (Exception e) {
 			throw new BatchJobException(e);
 		}
 
 		try {
 			currentBatchJob.submit();
 		} catch (Exception e) {
 			throw new BatchJobException(e);
 		}
 
 	}
 }
