 package pleocmd.itfc.gui;
 
 import java.awt.EventQueue;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.filechooser.FileFilter;
 
 import pleocmd.Log;
 import pleocmd.StandardInput;
 
 public final class MainInputPanel extends JPanel {
 
 	private static final long serialVersionUID = 8130292678723649962L;
 
 	private static final int INVALID_INDEX = 0x7FFFFF00;
 
 	private final HistoryListModel historyListModel;
 
 	private final JList historyList;
 
 	private final JScrollPane historyScrollPane;
 
 	private final JTextField consoleInput;
 
 	private final JButton btnSend;
 
 	private final JButton btnSendEOS;
 
 	private final JButton btnRead;
 
 	private final JButton btnClear;
 
 	private int historyIndex = INVALID_INDEX;
 
 	public MainInputPanel() {
 		final Layouter lay = new Layouter(this);
 
 		historyListModel = new HistoryListModel();
 		historyList = new JList(historyListModel);
 		lay.addWholeLine(historyScrollPane = new JScrollPane(historyList,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), true);
 		historyList.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 				final int idx = getHistoryList().getSelectedIndex();
 				if (idx != -1) {
 					setConsoleInput(idx);
 					if (e.getClickCount() == 2) sendConsoleInput();
 				}
 			}
 		});
 
 		consoleInput = new JTextField();
 		consoleInput.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(final KeyEvent e) {
 				handleConsoleKeys(e.getKeyCode(), e.getModifiersEx());
 			}
 
 		});
 		lay.addWholeLine(consoleInput, false);
 
 		btnSend = lay.addButton("Send", "system-run",
 				"Send text from input-field to the pipe", new Runnable() {
 					@Override
 					public void run() {
 						sendConsoleInput();
 					}
 				});
 		btnSendEOS = lay.addButton("Send EOS", "media-playback-stop",
 				"Send end-of-stream signal", new Runnable() {
 					@Override
 					public void run() {
 						closeConsoleInput();
 					}
 				});
 		btnRead = lay.addButton("Read From ...", "document-import",
 				"Uses the whole contents of a file as it was "
 						+ "entered in the input field", new Runnable() {
 					@Override
 					public void run() {
 						readConsoleInputFromFile();
 					}
 				});
 		lay.addSpacer();
 		btnClear = lay.addButton("Clear History", "archive-remove",
 				"Clears the history list of recently entered input",
 				new Runnable() {
 					@Override
 					public void run() {
 						clearHistory();
 					}
 				});
 
 		// an old history list may have been loaded from the configuration
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				scrollToBottom();
 				updateState();
 			}
 		});
 	}
 
 	protected void handleConsoleKeys(final int key, final int modifiers) {
 		switch (key) {
 		case KeyEvent.VK_ENTER:
 			sendConsoleInput();
 			break;
 		case KeyEvent.VK_UP:
 			moveInHistory(-1);
 			break;
 		case KeyEvent.VK_DOWN:
 			moveInHistory(1);
 			break;
 		case KeyEvent.VK_PAGE_UP:
 			scrollInHistory(-1);
 			break;
 		case KeyEvent.VK_PAGE_DOWN:
 			scrollInHistory(1);
 			break;
 		case KeyEvent.VK_R:
 			if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0)
 				searchInHistory();
 			break;
 		}
 	}
 
 	/**
 	 * Scrolls in the history just like the scroll bar has been dragged.
 	 * 
 	 * @param direction
 	 *            -1 to scroll upwards and +1 to scroll downwards
 	 */
 	private void scrollInHistory(final int direction) {
 		final JScrollBar sb = historyScrollPane.getVerticalScrollBar();
 		sb.setValue(sb.getValue() + sb.getUnitIncrement(direction) * direction);
 	}
 
 	/**
 	 * Moves the current active history line marker and sets the console input
 	 * to the contents of the now active line.
 	 * 
 	 * @param direction
 	 *            -1 to move upwards and +1 to move downwards
 	 */
 	private void moveInHistory(final int direction) {
 		final int idx = Math.max(0, Math.min(historyListModel.getSize(),
 				historyIndex));
 		setConsoleInput(Math.max(0, Math.min(historyListModel.getSize(), idx
 				+ direction)));
 	}
 
 	/**
 	 * Searches for the first line in history which matches the current console
 	 * input and sets the console input to this line.<br>
 	 * Does nothing if no match can be found.
 	 */
 	private void searchInHistory() {
 		final String expr = String.format(".*%s.*", consoleInput.getText());
 		for (int idx = historyListModel.getSize() - 1; idx >= 0; --idx)
 			if (historyListModel.getElementAt(idx).toString().matches(expr)) {
 				setConsoleInput(idx);
 				return;
 			}
 	}
 
 	/**
 	 * Sets the console input to the history at the given position.<br>
 	 * If the position equals the size of the history, an empty {@link String}
 	 * will be used.
 	 * 
 	 * @param index
 	 *            index of the history
 	 */
 	protected void setConsoleInput(final int index) {
 		Log.detail("Setting console input to index %d (history index: %d)",
 				index, historyIndex);
 		final String hist = index == historyListModel.getSize() ? ""
 				: historyListModel.getElementAt(index).toString();
 		consoleInput.setText(hist);
 		consoleInput.setCaretPosition(hist.length());
 		historyIndex = index;
 		if (index == historyListModel.getSize())
 			historyList.clearSelection();
 		else
 			historyList.setSelectedIndex(index);
 	}
 
 	/**
 	 * Sets the console input to the given {@link String} and resets the active
 	 * history line marker.
 	 * 
 	 * @param str
 	 *            new text to display in the console input field
 	 */
 	protected void setConsoleInput(final String str) {
 		Log.detail("Setting console input to '%s' (history index: %d)", str,
 				historyIndex);
 		consoleInput.setText(str);
 		consoleInput.setCaretPosition(str.length());
 		historyIndex = INVALID_INDEX;
 		historyList.clearSelection();
 	}
 
 	/**
 	 * Sends the current input to the {@link StandardInput}, adds it to the
 	 * history and resets the console input field.<br>
 	 * Does nothing if the console input is empty.
 	 */
 	public void sendConsoleInput() {
 		Log.detail("Sending console input (history index: %d)", historyIndex);
 		try {
 			final String input = consoleInput.getText();
 			if (input.isEmpty()) return;
 			setConsoleInput("");
 			StandardInput.the().put((input + "\n").getBytes("ISO-8859-1"));
 			historyListModel.add(input);
 			EventQueue.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					scrollToBottom();
 					updateState();
 				}
 			});
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void closeConsoleInput() {
 		try {
 			StandardInput.the().close();
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void readConsoleInputFromFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(final File f) {
 				return true;
 			}
 
 			@Override
 			public String getDescription() {
 				return "Ascii-Textfile containing Data-List";
 			}
 		});
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			readConsoleInputFromFile(fc.getSelectedFile());
 	}
 
 	public void readConsoleInputFromFile(final File file) {
 		try {
 			final BufferedReader in = new BufferedReader(new FileReader(file));
 			String line;
 			while ((line = in.readLine()) != null) {
 				setConsoleInput(line);
 				sendConsoleInput();
 			}
 			in.close();
 		} catch (final IOException exc) {
 			Log.error(exc);
 		}
 	}
 
 	public void clearHistory() {
 		historyListModel.clear();
 		updateState();
 	}
 
 	protected JList getHistoryList() {
 		return historyList;
 	}
 
 	protected HistoryListModel getHistoryListModel() {
 		return historyListModel;
 	}
 
 	protected void scrollToBottom() {
 		final int size = historyListModel.getSize() - 1;
		historyList.scrollRectToVisible(historyList.getCellBounds(size, size));
 	}
 
 	public void updateState() {
 		final boolean ready = MainFrame.the().isPipeRunning()
 				&& !StandardInput.the().isClosed();
 		consoleInput.setEnabled(ready);
 		btnSend.setEnabled(ready);
 		btnSendEOS.setEnabled(ready);
 		btnRead.setEnabled(ready);
 		historyList.setEnabled(ready);
 		btnClear.setEnabled(historyListModel.getSize() > 0);
 	}
 
 }
