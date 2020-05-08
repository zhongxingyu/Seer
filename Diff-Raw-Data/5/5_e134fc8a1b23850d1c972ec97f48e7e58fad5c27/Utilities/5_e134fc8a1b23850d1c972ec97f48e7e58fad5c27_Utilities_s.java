 
 package org.paxle.desktop.impl;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.EventListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.JTextComponent;
 
 public class Utilities {
 	
 	public static final Point LOCATION_CENTER = new Point();
 	private static final String KE_CLOSE = new String();
 	
 	public static JButton createButton(final String text, final ActionListener al, final String actionCommand, final Icon icon) {
 		final JButton b = new JButton(text);
 		b.setActionCommand(actionCommand);
 		if (al != null)
 			b.addActionListener(al);
 		if (icon != null)
 			b.setIcon(icon);
 		return b;
 	}
 	
 	public static File chooseSingleFile(
 			final Component parent,
 			final String title,
 			final boolean load,
			final FileNameExtensionFilter fnef,
 			final boolean tryAgain) {
 		final JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle(title);
 		if (fnef != null)
 			fc.setFileFilter(fnef);
 		fc.setMultiSelectionEnabled(false);
 		
 		File file;
 		boolean ok;
 		boolean cont;
 		do {
 			ok = false;
 			final int result = (load) ? fc.showOpenDialog(parent) : fc.showSaveDialog(parent);
 			if (result != JFileChooser.APPROVE_OPTION)
 				return null;
 			file = fc.getSelectedFile();
 			
 			final String cmsg = "\nSelect another file?";
 			final String ctitle;
 			final String msg;
 			final boolean error;
 			
 			if (load) {
 				if (!file.exists()) {
 					msg = "File " + file + " does not exist.";
 					ctitle = "File does not exist";
 					error = true;
 				} else if (file.isDirectory()) {
 					msg = "File " + file + " is a directory";
 					ctitle = "Invalid file selected";
 					error = true;
 				} else if (!file.canRead()) {
 					msg = "Permissions to read file " + file + " not given";
 					ctitle = "Cannot access file";
 					error = true;
 				} else {
 					ok = true;
 					break;
 				}
 			} else {
 				if (file.exists()) {
 					if (file.isDirectory()) {
 						msg = "File " + file + " is a directory.";
 						ctitle = "Invalid file selected";
 						error = true;
 					} else if (!file.canWrite()) {
 						msg = "Permissions to write file " + file + " not given";
 						ctitle = "Cannot access file";
 						error = true;
 					} else {
 						msg = "File " + file + " already exists.";
 						ctitle = "File exists";
 						error = false;
 					}
 				} else {
 					ok = true;
 					break;
 				}
 			}
 			
 			final int answer = contChoose(parent, ctitle, msg + cmsg, msg, tryAgain, error);
 			cont = (answer == JOptionPane.YES_OPTION);
 			if (!error) {
 				if (answer == JOptionPane.CANCEL_OPTION) {
 					ok = false;
 					break;
 				} else {
 					ok = !cont;
 				}
 			}
 		} while (tryAgain && cont);
 		
 		return (ok) ? file : null;
 	}
 	
 	private static int contChoose(
 			final Component parent,
 			final String title,
 			final String message,
 			final String noContMsg,
 			boolean cont, boolean error) {
 		if (cont) {
 			return JOptionPane.showConfirmDialog(
 					parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION, (error) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE);
 		} else {
 			JOptionPane.showMessageDialog(parent, noContMsg, title, (error) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE);
 			return -1;
 		}
 	}
 	
 	public static void setFrameProps(
 			final JFrame frame,
 			final Container container,
 			final String title,
 			final Dimension size,
 			final boolean resizable,
 			final Point location,
 			final EventListener... listeners) {
 		frame.setTitle(title);
 		frame.setContentPane(container);
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
 				KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK), KE_CLOSE);
 		frame.getRootPane().getActionMap().put(KE_CLOSE, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 		});
 		frame.setResizable(resizable);
 		
 		if (listeners != null && listeners.length > 0) {
 			for (final Method m : frame.getClass().getMethods()) {
 				final String name = m.getName();
 				if (!name.startsWith("add") || !name.endsWith("Listener"))
 					continue;
 				final String lName = name.substring(3);
 				try {
 					final Class<?> c = Class.forName("java.awt.event." + lName);
 					for (final EventListener l : listeners) {
 						if (l != null && c.isAssignableFrom(l.getClass())) try {
 							m.invoke(frame, l);
 							break;
 						} catch (InvocationTargetException e) {
 							e.printStackTrace();
 						} catch (IllegalAccessException e) {
 							e.printStackTrace();
 						}
 					}
 				} catch (ClassNotFoundException e) { continue; }
 			}
 		}
 		
 		if (size == null) {
 			frame.pack();
 		} else {
 			frame.setSize(size);
 		}
 		if (location == null) {
 			frame.setLocationByPlatform(true);
 		} else if (location == LOCATION_CENTER) {
 			centerOnScreen(frame);
 		} else {
 			frame.setLocation(location);
 		}
 	}
 	
 	public static JFrame wrapIntoFrame(
 			final Container container,
 			final String title,
 			final Dimension size,
 			final boolean resizable,
 			final Point location,
 			final EventListener... listeners) {
 		final JFrame frame = new JFrame(title);
 		setFrameProps(frame, container, title, size, resizable, location, listeners);
 		return frame;
 	}
 	
 	/* 
 	 * Author: Franz Brauße
 	 * from YacyAdmin - relicensed
 	 * License: CPL v1.0
 	 */
 	private static class ExceptionBox extends JDialog {
 		
 		private static final long serialVersionUID = 1L;
 		
 		private static final String SHOW_TEXT 			= "Show stacktrace";
 		private static final String HIDE_TEXT 			= "Hide stacktrace";
 		private static final String LBL_TITLE 			= "An error occured";
 		private static final String ERR_MSG_ 			= "Error message:";
 		private static final String LBL_STACKTRACE_ 	= "Stacktrace:";
 		private static final String LBL_OK 				= "OK";
 		private static final String LBL_COPY2CLIPBRD 	= "Copy to clipboard";
 		private static final String __AT_ 				= "        at ";
 		
 		private static final Dimension hiddenDim = new Dimension(500, 84);
 		private static final Dimension shownDim = new Dimension(500, 300);
 		
 		private JPanel 		content				= null;
 		private JButton 	btnShowHide 		= null;
 		private JButton 	btnCopyClipboard 	= null;
 		private JScrollPane	exceptionLog 		= null;
 		private JLabel 		lStackTrace 		= null;
 		private JTextArea 	textStacktrace 		= null;
 		
 		private final Throwable ex;
 		private final String detail;
 		private boolean hidden = true;
 		
 		public ExceptionBox(Frame parent, Throwable ex) {
 			this(parent, ex.getMessage(), ex);
 		}
 		
 		public ExceptionBox(Frame parent, String detail, Throwable ex) {
 			super(parent, LBL_TITLE, true);
 			this.ex = ex;
 			this.detail = detail;
 			initialize(parent);
 		}
 		
 		public static void showExceptionBox(final Frame parent, final Throwable ex) {
 			showExceptionBox(parent, ex.getMessage(), ex);
 		}
 		
 		public static void showExceptionBox(final Frame parent, final String detail, final Throwable ex) {
 			new ExceptionBox(parent, detail, ex).setVisible(true);
 		}
 		
 		private void initialize(Component parent) {
 			this.setSize(hiddenDim);
 			
 			// place dialog in the middle of the screen
 			Dimension dim = parent.getSize();
 			this.setLocation(((int)(dim.getWidth() - hiddenDim.width) / 2) + parent.getX(), ((int)(dim.getHeight() - shownDim.height) / 2) + parent.getY());
 			
 			this.setResizable(true);
 			this.setContentPane(getContent());
 			
 			StackTraceElement[] se = this.ex.getStackTrace();
 			StringBuffer s = new StringBuffer(100);
 			s.append(this.ex.toString()).append("\n");
 			for (int i=0; i<se.length; i++)
 				s.append(__AT_).append(se[i].toString()).append("\n");
 			textStacktrace.setText(s.toString());
 		}
 		
 		private JPanel getContent() {
 			if (content == null) {
 				content = new JPanel();
 				content.setLayout(new BorderLayout());
 				content.add(getDisplayPanel(), BorderLayout.CENTER);
 				content.add(getSubmitPanel(), BorderLayout.SOUTH);
 			}
 			return content;
 		}
 		
 		private JPanel getDisplayPanel() {
 			JPanel r = new JPanel(new GridBagLayout());
 			
 			GridBagConstraints gbc = new GridBagConstraints();
 			gbc.insets = new Insets(5, 5, 5, 5);
 			gbc.anchor = GridBagConstraints.NORTHEAST;
 			r.add(new JLabel(ERR_MSG_), gbc);
 			
 			gbc.gridx = 1;
 			gbc.weightx = 1D;
 			gbc.anchor = GridBagConstraints.WEST;
 			r.add(new JLabel(this.detail), gbc);
 			
 			gbc.weightx = 0D;
 			gbc.gridx = 0;
 			gbc.gridy = 1;
 			gbc.anchor = GridBagConstraints.NORTHEAST;
 			this.lStackTrace = new JLabel(LBL_STACKTRACE_);
 			lStackTrace.setVisible(false);
 			r.add(lStackTrace, gbc);
 			
 			gbc.gridx = 1;
 			gbc.fill = GridBagConstraints.BOTH;
 			gbc.weightx = 1D;
 			gbc.weighty = 1D;
 			r.add(getExceptionLog(), gbc);
 			
 			return r;
 		}
 		
 		private JPanel getSubmitPanel() {
 			JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
 			r.add(getBtnCopyToClipboard());
 			r.add(getBtnShowHide());
 			r.add(getBtnOK());
 			return r;
 		}
 		
 		private JScrollPane getExceptionLog() {
 			if (this.exceptionLog == null) {
 				textStacktrace = new JTextArea();
 				textStacktrace.setEditable(false);
 				textStacktrace.setBorder(null);
 				this.exceptionLog = new JScrollPane();
 				this.exceptionLog.setViewportView(textStacktrace);
 				this.exceptionLog.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 				this.exceptionLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 				this.exceptionLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 				this.exceptionLog.setVisible(false);
 			}
 			return this.exceptionLog;
 		}
 		
 		private JButton getBtnOK() {
 			JButton r = new JButton(LBL_OK);
 			this.getRootPane().setDefaultButton(r);
 			r.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					ExceptionBox.this.setVisible(false);
 				}
 			});
 			return r;
 		}
 		
 		private class clpbrdListener implements ActionListener, ClipboardOwner {
 			public void actionPerformed(ActionEvent e) {
 				Toolkit.getDefaultToolkit().getSystemClipboard()
 						.setContents(new StringSelection(ExceptionBox.this.textStacktrace.getText()), this);
 			}
 			public void lostOwnership(Clipboard arg0, Transferable arg1) { /* ignore this */ }
 		}
 		
 		private JButton getBtnCopyToClipboard() {
 			if (this.btnCopyClipboard == null) {
 				this.btnCopyClipboard = new JButton(LBL_COPY2CLIPBRD);
 				this.btnCopyClipboard.addActionListener(new clpbrdListener());
 			}
 			return this.btnCopyClipboard;
 		}
 		
 		private JButton getBtnShowHide() {
 			if (this.btnShowHide == null) {
 				this.btnShowHide = new JButton(SHOW_TEXT);
 				this.btnShowHide.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						hidden = !hidden;
 						ExceptionBox.this.btnShowHide.setText((hidden) ? SHOW_TEXT : HIDE_TEXT);
 						ExceptionBox.this.exceptionLog.setVisible(!hidden);
 						ExceptionBox.this.lStackTrace.setVisible(!hidden);
 						ExceptionBox.this.setSize((hidden) ? hiddenDim : shownDim);
 						ExceptionBox.this.exceptionLog.setViewportView((hidden) ? null : textStacktrace);
 						textStacktrace.updateUI();
 						ExceptionBox.this.lStackTrace.updateUI();
 					}
 				});
 			}
 			return this.btnShowHide;
 		}
 	}
 	
 	public static void showExceptionBox(final Frame parent, final String detail, final Throwable ex) {
 		ExceptionBox.showExceptionBox(parent, detail, ex);
 	}
 	
 	public static void showExceptionBox(final String detail, final Throwable ex) {
 		ExceptionBox.showExceptionBox(null, detail, ex);
 	}
 	
 	public static void showExceptionBox(final Throwable ex) {
 		ExceptionBox.showExceptionBox(null, ex.getMessage(), ex);
 	}
 	
 	public static void showURLErrorMessage(final String message, final String url) {
 		final JFrame frame = new JFrame("Error");
 		final JPanel panel = new JPanel(new GridBagLayout());
 		final GridBagConstraints gbc = new GridBagConstraints();
 		final UIDefaults def = UIManager.getLookAndFeelDefaults();
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.gridheight = 2;
 		gbc.fill = GridBagConstraints.VERTICAL;
 		gbc.weighty = 1.0;
 		gbc.insets = new Insets(5, 5, 5, 5);
 		panel.add(new JLabel(def.getIcon("OptionPane.errorIcon")), gbc);
 		final JButton close = new JButton("Close");
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.setVisible(false);
 				frame.dispose();
 			} });
 		frame.getRootPane().setDefaultButton(close);
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		gbc.gridx = 0;
 		gbc.gridwidth = 2;
 		gbc.gridheight = 1;
 		gbc.gridy = 2;
 		gbc.weighty = 0.0;
 		panel.add(close, gbc);
 		gbc.gridx = 1;
 		gbc.gridwidth = 1;
 		gbc.gridy = 0;
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
 		gbc.fill = GridBagConstraints.BOTH;
 		final JTextArea textField = new JTextArea(message);
 		setTextLabelDefaults(textField);
 		panel.add(textField, gbc);
 		final JTextField urlField = new JTextField(url);
 		setTextLabelDefaults(urlField);
 		urlField.setAutoscrolls(true);
 		urlField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 		urlField.select(0, url.length());
 		gbc.gridy = 1;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.weighty = 0.0;
 		panel.add(urlField, gbc);
 		
 		frame.setContentPane(panel);
 		frame.pack();
 		centerOnScreen(frame);
 		frame.setVisible(true);
 	}
 	
 	public static void setTextLabelDefaults(final JTextComponent textComponent) {
 		final UIDefaults def = UIManager.getLookAndFeelDefaults();
 		textComponent.setFont(def.getFont("OptionPane.font"));
 		textComponent.setBackground(def.getColor("OptionPane.background"));
 		textComponent.setForeground(def.getColor("OptionPane.foreground"));
 		textComponent.setBorder(null);
 		textComponent.setEditable(false);
 		textComponent.setFocusable(false);
 	}
 	
 	public static void centerOnScreen(final Component component) {
 		centerOnScreen(component, component.getWidth(), component.getHeight());
 	}
 	
 	public static void centerOnScreen(final Component component, final int compWidth, final int compHeight) {
 		final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		component.setLocation(Math.max(0, (d.width - compWidth) / 2), Math.max(0, (d.height - compHeight) / 2));
 	}
 }
