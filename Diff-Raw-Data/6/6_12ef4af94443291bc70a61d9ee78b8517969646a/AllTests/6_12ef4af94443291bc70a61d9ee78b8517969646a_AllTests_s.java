 package nl.nikhef.jgridstart;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.logging.LogManager;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 import nl.nikhef.jgridstart.gui.util.TemplateButtonPanelTest;
 import nl.nikhef.jgridstart.install.BrowsersMacOSXTest;
 import nl.nikhef.jgridstart.util.ConnectionUtils;
 import nl.nikhef.jgridstart.util.FileUtilsTest;
 import nl.nikhef.jgridstart.util.PasswordCacheTest;
 import nl.nikhef.xhtmlrenderer.swing.TemplateDocumentTest;
 import nl.nikhef.xhtmlrenderer.swing.TemplatePanelTest;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class AllTests {
     
     public static Test suite() {
 	// setup logging
 	try {
 	    // load configuration
 	    LogManager.getLogManager().readConfiguration(AllTests.class.getResourceAsStream("/logging.debug.properties"));
 	} catch(Exception e) {
 	    System.out.println("Warning: logging configuration could not be set");
 	}
 	TestSuite suite = new TestSuite("Test for nl.nikhef.jgridstart");
 	//$JUnit-BEGIN$
 	suite.addTestSuite(FileUtilsTest.class);
 	suite.addTestSuite(PasswordCacheTest.class);
 	suite.addTestSuite(CertificateCheckTest.class);
 	suite.addTestSuite(CertificateStore1Test.class);
 	suite.addTestSuite(CertificateStore2Test.class);
 	suite.addTestSuite(CertificateStoreWithDefaultTest.class);
 	suite.addTestSuite(BrowsersMacOSXTest.class);
 	suite.addTestSuite(TemplateDocumentTest.class);
 	suite.addTestSuite(TemplatePanelTest.class);
 	suite.addTestSuite(TemplateButtonPanelTest.class);
 	//$JUnit-END$
 	return suite;
     }
     
     /** GUI test program that runs unit tests */
     public static void main(String[] args) {
 	new TesterFrame().setVisible(true);
     }
     
     /** Simple GUI for running and submitting tests.
      * <p>
      * To enable diagnostics on the user's computer, the tests are packaged
      * in a single testing package. A GUI is provided so that the user can
      * easily run the tests, and submit them to the jGridstart developers.
      * This allows us to gather test results on a variety of platforms.
      * 
      * @author wvengen
      */
     static class TesterFrame extends JFrame {
 	/** Where to post data to */
 	final String url = "http://jgridstart.nikhef.nl/tests/upload.php";
 	
 	/** Frame title */
 	final String title = "jGridstart Testing Program";
 	/** Label with user message */
 	JLabel msg;
 	/** Testing output display */
 	JTextArea outputpane;
 	/** Testing output display scrolled area */
 	JScrollPane outputscroll;
 	/** Allow upload checkbox */
 	JCheckBox uploadCheck;
 	/** Action button (run or upload) */
 	JButton actionBtn;
 	/** Quit action, stops testing process as well */
 	Action quitAction;
 	/** Run action */
 	Action runAction;
 	/** Upload action */
 	Action uploadAction;
 	/** Testing process */
 	Process proc = null;
 	/** Testing output */
 	StringBuffer output;
 	
 	final String linesep = System.getProperty("line.separator");
 	
 	public TesterFrame() {
 	    setTitle(title);
 	    setSize(new Dimension(650, 400));
 	    setDefaultCloseOperation(EXIT_ON_CLOSE);
 	    JPanel panel = new JPanel();
 	    panel.setLayout(new BorderLayout(2, 2));
 	    panel.add((msg = new JLabel()), BorderLayout.NORTH);
 	    JPanel cpanel = new JPanel(new BorderLayout());
 	    outputpane = new JTextArea();
 	    outputscroll = new JScrollPane(outputpane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 	    outputpane.setEditable(false);
 	    cpanel.add(outputscroll, BorderLayout.CENTER);
 	    uploadCheck = new JCheckBox("Submit results to developers when the tests are finished.");
 	    cpanel.add(uploadCheck, BorderLayout.SOUTH);
 	    uploadCheck.setSelected(true);
 	    panel.add(cpanel, BorderLayout.CENTER);
 	    JPanel btnpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 	    runAction = new AbstractAction("Run tests") {
 		public void actionPerformed(ActionEvent event) {
 		    runTests();
 		}
 	    };
 	    btnpanel.add((actionBtn = new JButton(runAction)));
 	    getRootPane().setDefaultButton(actionBtn);      
 	    uploadAction = new AbstractAction("Upload result") {
 		public void actionPerformed(ActionEvent event) {
 		    doUpload();
 		}
 	    };
 	    btnpanel.add(Box.createHorizontalStrut(5));
 	    quitAction = new AbstractAction("Quit") {
 		public void actionPerformed(ActionEvent event) {
 		    if (proc!=null) proc.destroy();
 		    System.exit(0);
 		}
 	    };
 	    btnpanel.add(new JButton(quitAction));
 	    panel.add(btnpanel, BorderLayout.SOUTH);
 	    setContentPane(panel);
 	    setMessage("Thank you for running the jGridstart testing program. Your cooperation enables us to " +
 		       "improve the software. Please press the button 'Run tests' below, then wait while the " +
 		       "tests are running, until a message appears about them being done.\n" +
 		       "\n" +
 		       "By default, the test results shown here will be sent to the jGridstart developers. If you " +
 		       "don't want this, feel free to disable it below. You will be able to upload it later.");
 	}
 	
 	/** Set the message area to a string */
 	void setMessage(String txt) {
 	    msg.setText("<html><body>" +
 		    "<h1>"+title+"</h1>" +		    
 		    txt.replaceAll("\n", "<br>")+"<br><br></html></body>");
 	}
 	
 	/** Start the tests */
 	void runTests() {
 	    if (proc!=null) return;
 	    
 	    try {
 		setMessage("Please <i>don't touch</i> your mouse or keyboard while the tests are running...\n" +
 			   "(the graphical user-interface tests require this to function)");
 		runAction.setEnabled(false);
 		// find java first
 		String java = new File(new File(new File(System.getProperty("java.home")), "bin"), "java").getPath();
 		if (System.getProperty("os.name").startsWith("Windows")) java = java + ".exe";
 		final String[] cmd = new String[] {
 			java,
 			"-cp",
 			System.getProperty("java.class.path"),
 			"org.junit.runner.JUnitCore",
 			AllTests.class.getCanonicalName()
 		};
 		proc = Runtime.getRuntime().exec(cmd);
 		
 		new CaptureThread(proc.getInputStream()).start();
 		new CaptureThread(proc.getErrorStream()).start();
 		
 	    } catch (Exception e) {
 		e.printStackTrace(); // TODO finish
 	    }
 	}
 
 	/** Update gui to signal that tests are done */
 	void signalTestsDone() {
 	    setMessage("The tests have finished.");
 	    actionBtn.setAction(uploadAction);
 	    if (uploadCheck.isSelected()) doUpload();
 	}
 	
 	/** Upload test results */
 	void doUpload() {
 	    if (!uploadAction.isEnabled()) return;
 	    uploadAction.setEnabled(false);
 	    uploadCheck.setEnabled(false);
 	    outputpane.append("-- Uploading data to developers"+linesep);
 	    new Thread() {
 		final String txt = outputpane.getText();
 		@Override
 		public void run() {
 		    try {
 			final String ret = ConnectionUtils.pageContents(
 				new URL(url),
 				new String[] { "testresult", txt },
 				true);
 			SwingUtilities.invokeLater(new Runnable() {
 			    public void run() {
 				outputpane.append(ret+linesep);
 				signalUploadDone();
 			    }
 			});
 		    } catch (final Exception e) {
 			e.printStackTrace();
 			SwingUtilities.invokeLater(new Runnable() {
 			    public void run() {
 				outputpane.append("-- Submission of test results failed"+linesep);
 				outputpane.append(e.getLocalizedMessage()+linesep);
 				setMessage("Submission of test results failed, sorry.");
 				uploadAction.setEnabled(true);
 			    }
 			});
 		    }
 		}
 	    }.start();
 	}
 	
 	/** Update gui to signal that upload is done */
 	void signalUploadDone() {
 	    uploadAction.setEnabled(false);
 	    setMessage("The test results have been uploaded. Thank you for participating!\n" +
 		       "You can now close this window.");
 	}
 	
 	/** Thread to capture an {@linkplain InputStream} and put it in the textarea */
 	class CaptureThread extends Thread {
 	    BufferedReader reader;
 	    public CaptureThread(InputStream r) {
 		reader = new BufferedReader(new InputStreamReader(r));
 	    }
 	    @Override
 	    public void run() {
 		final StringBuffer buf = new StringBuffer(128);
 		final char sc = linesep.charAt(linesep.length()-1);
 		try {
 		    while (true) {
 			int c = reader.read();
 			// handle eof
 			if (c < 0) break;
 			// append character
 			buf.append((char)c);
 			// and newline appends to textarea
 			if ( sc == (char)c && buf.toString().endsWith(linesep)) {
 			    SwingUtilities.invokeLater(new Runnable() {
 				String str = buf.toString();
 				public void run() {
 				    outputpane.append(str);
 				}
 			    });
 			    buf.setLength(0);
 			}
 		    }
 		} catch (Exception e) {
 		    e.printStackTrace();
 		} finally {
 		    try { reader.close(); } catch (Exception e) { }
 		    SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 			    signalTestsDone();
 			}
 		    });
 		}
 	    }
 	}
     }
 }
