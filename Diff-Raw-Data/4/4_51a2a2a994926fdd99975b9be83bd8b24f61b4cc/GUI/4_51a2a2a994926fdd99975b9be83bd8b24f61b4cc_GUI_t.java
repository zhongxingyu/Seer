 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLPeerUnverifiedException;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.UIManager;
 
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.acl.LastOwnerException;
 import java.security.cert.Certificate;
 import java.text.DecimalFormat;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.swing.SwingConstants;
 import javax.swing.JButton;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.InputMethodListener;
 import java.awt.event.InputMethodEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.CaretEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 /*
  *@Author grundyboy34
  *@Version 0.3
  */
 public class GUI extends JFrame {
 
 	private JPanel contentPane;
 	JTextField textField;
 	JTextField textField_1;
 	final JLabel statusLabel = new JLabel("Idle");
 	final ErrorLabel errorLabel = new ErrorLabel("");
 	final JButton downloadButton = new JButton("Download!");
 	JProgressBar progressBar = new JProgressBar();
 	final GUI thisGUI = this;
 	ErrorCheck errorChecker;
 	boolean isDownloading = false;
 	WebDownload currentDownload;
 
 	String currentPath;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 			if ("Nimbus".equals(info.getName())) {
 				try {
 					UIManager.setLookAndFeel(info.getClassName());
 				} catch (Exception e) {
 
 				}
 				break;
 			}
 		}
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				GUI frame = new GUI();
 				frame.setVisible(true);
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public GUI() {
 
 		try {
 			currentPath = new java.io.File(".").getCanonicalPath();
 		} catch (IOException e) {
 			e.printStackTrace();
 			currentPath = "C:";
 		}
 
 		// gui init
 		setTitle("Simple Downloader");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 387, 281);
 		setResizable(false);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		textField = new JTextField();
 		textField.setBounds(111, 26, 252, 31);
 		contentPane.add(textField);
 		textField.setColumns(10);
 		textField
 				.setText("https://raw.github.com/grundyboy34/SimpleDownloader/master/README.md");
 
 		JLabel lblNewLabel = new JLabel("URL");
 		lblNewLabel.setBounds(10, 34, 46, 14);
 		contentPane.add(lblNewLabel);
 
 		JLabel lblNewLabel_1 = new JLabel("Save Directory");
 		lblNewLabel_1.setBounds(10, 76, 81, 14);
 		contentPane.add(lblNewLabel_1);
 
 		progressBar.setBounds(111, 110, 252, 25);
 		contentPane.add(progressBar);
 
 		textField_1 = new JTextField();
 		textField_1.setBounds(111, 68, 252, 31);
 		contentPane.add(textField_1);
 		textField_1.setColumns(10);
 		textField_1.setText(currentPath);
 
 		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		statusLabel.setBounds(10, 146, 353, 19);
 		contentPane.add(statusLabel);
 
 		JLabel lblPro = new JLabel("Progress");
 		lblPro.setBounds(10, 121, 61, 14);
 		contentPane.add(lblPro);
 
 		errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		errorLabel.setBounds(10, 176, 353, 19);
 		contentPane.add(errorLabel);
 
 		downloadButton.setBounds(48, 206, 276, 31);
 		contentPane.add(downloadButton);
 
 		errorChecker = new ErrorCheck(thisGUI);
 
 		// listeners
 
 		textField_1.addCaretListener(new CaretListener() {
 			public void caretUpdate(CaretEvent arg0) {
 				errorChecker.setFileDir(textField_1.getText());
 			}
 		});
 
 		textField.addCaretListener(new CaretListener() {
 			public void caretUpdate(CaretEvent arg0) {
 				errorChecker.setURL(textField.getText());
 			}
 		});
 
 		downloadButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (isDownloading) {
 					currentDownload.abort();
 				} else {
 					currentDownload = new WebDownload(thisGUI);
 				}
 				isDownloading = !isDownloading;
 				toggleDownloadButtonText();
 			}
 		});
 
 	}
 
 	public void setEnabledForAll(boolean status) {
 		textField.setEnabled(status);
 		textField_1.setEnabled(status);
 	}
 
 	public void toggleDownloadButtonText() {
 		if (isDownloading) {
 			downloadButton.setText("Abort");
 		} else {
 			downloadButton.setText("Download");
 		}
 	}
 
 }
 
 class WebDownload implements Runnable {
 	GUI gui;
 	Thread t;
 	long fileSize;
 	int bufferSize = 1024 * 20;
 
 	public WebDownload(GUI gui) {
 		this.gui = gui;
 		t = new Thread(this);
 		t.start();
 	}
 
 	public void abort() {
 		t.interrupt();
 	}
 
 	void download() throws MalformedURLException, IOException {
 
 		String fileUrl = gui.textField.getText();
 		String filePath = gui.textField_1.getText();
 		if (filePath.charAt(filePath.length() - 1) != '/') {
 			filePath += '/';
 		}
 
 		BufferedInputStream in = null;
 		FileOutputStream fout = null;
 		URL url = new URL(fileUrl);
 		HttpURLConnection httpCon;
 		String endPath = url.getFile();
 		String fileName = endPath.substring(endPath.lastIndexOf('/') + 1,
 				endPath.length());
 		try {
 			in = new BufferedInputStream(url.openStream());
 			fout = new FileOutputStream(filePath + fileName);
 			httpCon = (HttpURLConnection) url.openConnection();
 			fileSize = httpCon.getContentLengthLong();
 			httpCon.disconnect();
 			byte data[] = new byte[bufferSize];
 			int count = 0;
 			long start = System.nanoTime();
 			long totalRead = 0;
 			final double NANOS_PER_SECOND = 1000000000.0;
 			// final double BYTES_PER_MIB = 1024 * 1024;
 			final double BYTES_PER_KILOBYTE = 1024;
 			double speed;
 			DecimalFormat formatter = new DecimalFormat("#.##");
 			gui.progressBar.setMaximum(fileSize < 0 ? 1 : (int) fileSize);
 			gui.progressBar.setValue(0);
 			gui.setEnabledForAll(false);
 			while (!t.isInterrupted()
 					&& (count = in.read(data, 0, bufferSize)) != -1) {
 				totalRead += count;
 				speed = NANOS_PER_SECOND / BYTES_PER_KILOBYTE * totalRead
 						/ (System.nanoTime() - start + 1);
 				gui.statusLabel.setText("Downloading file - "
 						+ formatter.format(speed) + " KB/s");
 				gui.progressBar.setValue((int) totalRead);
 
 				fout.write(data, 0, count);
 			}
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 			if (fout != null) {
 				fout.close();
 			}
 
 			if (t.isInterrupted()) {
 				gui.statusLabel.setText("Download Aborted!");
 			} else {
 				gui.statusLabel.setText("Download Complete!");
 			}
			gui.isDownloading = false;
			gui.toggleDownloadButtonText();
 			gui.setEnabledForAll(true);
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			download();
 			t.interrupt();
 		} catch (MalformedURLException e) {
 		} catch (IOException e) {
 		}
 
 	}
 
 }
 
 class ErrorCheck implements Runnable {
 
 	Thread t;
 	GUI gui;
 	String urlPath;
 	String fileDir;
 	final Error INVALID_DIRECTORY = new Error("Not a valid directory!",
 			Color.RED, 8);
 	final Error MALFORMED_URL = new Error(
 			"Not a valid URL - be sure to include http://", Color.RED, 9);
 	final Error INVALID_URL = new Error("Couldn't establish a connection!",
 			Color.RED, 10);
 
 	public ErrorCheck(final GUI gui) {
 		this.gui = gui;
 		this.urlPath = gui.textField.getText();
 		this.fileDir = gui.textField_1.getText();
 		t = new Thread(this);
 		t.start();
 	}
 
 	public void setFileDir(String path) {
 		fileDir = path;
 	}
 
 	public void setURL(String url) {
 		urlPath = url;
 	}
 
 	public void checkForErrors() {
 		File path = new File(fileDir);
 		URL url = null;
 
 		try {
 			url = new URL(urlPath);
 			if (!url.getHost().equals(null) && !url.getHost().equals("")) {
 				url.openStream();
 				gui.errorLabel.removeError(INVALID_URL);
 				gui.errorLabel.removeError(MALFORMED_URL);
 				gui.textField.setBackground(Color.WHITE);
 			} else {
 				gui.errorLabel.addError(INVALID_URL);
 				gui.textField.setBackground(Color.RED);
 			}
 
 		} catch (MalformedURLException e) {
 			gui.errorLabel.addError(MALFORMED_URL);
 			gui.textField.setBackground(Color.RED);
 		} catch (IOException e) {
 			gui.errorLabel.addError(INVALID_URL);
 			gui.textField.setBackground(Color.RED);
 		}
 
 		if (!path.exists()) {
 			gui.errorLabel.addError(INVALID_DIRECTORY);
 			gui.textField_1.setBackground(Color.RED);
 		} else {
 			gui.errorLabel.removeError(INVALID_DIRECTORY);
 			gui.textField_1.setBackground(Color.WHITE);
 		}
 
 		gui.downloadButton.setEnabled(gui.errorLabel.getErrorList().isEmpty());
 	}
 
 	@Override
 	public void run() {
 		for (;;) {
 			checkForErrors();
 		}
 	}
 
 }
