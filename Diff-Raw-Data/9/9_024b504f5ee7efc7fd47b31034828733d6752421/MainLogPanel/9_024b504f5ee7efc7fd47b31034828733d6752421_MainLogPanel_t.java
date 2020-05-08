 package pleocmd.itfc.gui;
 
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ScrollPaneConstants;
 
 import pleocmd.Log;
 import pleocmd.StandardInput;
 import pleocmd.itfc.gui.icons.IconLoader;
 import pleocmd.pipe.Pipe;
 
 public final class MainLogPanel extends JPanel {
 
 	private static final long serialVersionUID = -6921879308383765734L;
 
 	private final Pipe pipe;
 
 	private final JTable logTable;
 
 	private final LogTableModel logModel;
 
 	private final JButton btnStart;
 
 	private Thread pipeThread;
 
 	public MainLogPanel(final Pipe pipe) {
 		this.pipe = pipe;
 
 		setLayout(new GridBagLayout());
 		final GridBagConstraints gbc = ConfigFrame.initGBC();
 		gbc.weightx = 0.0;
 		gbc.gridy = 0;
 		gbc.gridx = 0;
 
 		logModel = new LogTableModel();
 		logTable = new JTable(logModel) {
 
 			private static final long serialVersionUID = 1128237812769648620L;
 
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public String getToolTipText(final MouseEvent event) {
 				return getBacktrace(rowAtPoint(event.getPoint()));
 			}
 
 		};
 		logTable.getTableHeader().getColumnModel().getColumn(0).setMinWidth(50);
 		logTable.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(50);
 		logTable.getTableHeader().getColumnModel().getColumn(1)
 				.setMinWidth(200);
 		logTable.getTableHeader().getColumnModel().getColumn(1)
 				.setMaxWidth(200);
 		logTable.getTableHeader().setVisible(false);
 		logTable.setShowGrid(false);
		logTable.setEnabled(false);
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		gbc.weighty = 1.0;
 		add(new JScrollPane(logTable,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), gbc);
 		gbc.gridwidth = 1;
 		gbc.weighty = 0.0;
 
 		++gbc.gridy;
 
 		gbc.gridx = 0;
 		btnStart = new JButton("Start", IconLoader.getIcon("arrow-right.png"));
 		btnStart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				startPipeThread();
 			}
 		});
 		add(btnStart, gbc);
 
 		++gbc.gridx;
 		final JButton btnLogSave = new JButton("Save To ...", IconLoader
 				.getIcon("document-save-as.png"));
 		btnLogSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				writeLogToFile();
 			}
 		});
 		add(btnLogSave, gbc);
 
 		++gbc.gridx;
 		gbc.weightx = 1.0;
 		add(new JLabel(), gbc);
 		gbc.weightx = 0.0;
 
 		++gbc.gridx;
 		final JButton btnLogClear = new JButton("Clear", IconLoader
 				.getIcon("archive-remove.png"));
 		btnLogClear.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				clearLog();
 			}
 		});
 		add(btnLogClear, gbc);
 	}
 
 	private String getBacktrace(final int index) {
 		final Throwable bt = logModel.getLogAt(index).getBacktrace();
		if (bt == null) return null;
 		final StringWriter sw = new StringWriter();
 		final PrintWriter pw = new PrintWriter(sw);
 		bt.printStackTrace(pw);
 		pw.flush();
		return String.format("<html>%s</html>", sw.toString().replace("<",
				"&lt;").replace("\n", "<br>"));
 	}
 
 	public synchronized void startPipeThread() {
 		if (pipeThread != null)
 			throw new IllegalStateException("Pipe-Thread already running");
 		btnStart.setEnabled(false);
 		pipeThread = new Thread("Pipe-Thread") {
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void run() {
 				pipeCore();
 				synchronized (MainLogPanel.this) {
 					pipeThread = null;
 				}
 			}
 		};
 		pipeThread.start();
 	}
 
 	private void pipeCore() {
 		try {
 			StandardInput.the().close();
 			StandardInput.the().resetCache();
 			pipe.configuredAll();
 			pipe.initializeAll();
 			pipe.pipeAllData();
 			pipe.closeAll();
 		} catch (final Throwable t) { // CS_IGNORE
 			Log.error(t);
 		}
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void run() {
 				btnStart.setEnabled(true);
 			}
 		});
 	}
 
 	public void writeLogToFile() {
 		final JFileChooser fc = new JFileChooser();
 		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) try {
 			logModel.writeToFile(fc.getSelectedFile());
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void clearLog() {
 		logModel.clear();
 	}
 
 	public void addLog(final Log log) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void run() {
 				logModel.addLog(log);
 				logTable.scrollRectToVisible(logTable.getCellRect(logModel
 						.getRowCount() - 1, 0, true));
 			}
 		});
 	}
 
 }
