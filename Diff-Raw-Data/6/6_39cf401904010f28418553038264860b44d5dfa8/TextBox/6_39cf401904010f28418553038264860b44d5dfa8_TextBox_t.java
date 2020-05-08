 /*
  *   Copyright (C) 2006  The Concord Consortium, Inc.,
  *   25 Love Lane, Concord, MA 01742
  *
  *   This program is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.modeler.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeListener;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.html.FormView;
 import javax.swing.text.html.parser.ParserDelegator;
 
 import org.concord.modeler.HtmlService;
 import org.concord.modeler.ModelerUtilities;
 import org.concord.modeler.Searchable;
 import org.concord.modeler.event.HotlinkListener;
 import org.concord.modeler.event.SelfScriptListener;
import org.concord.modeler.text.Page;
 import org.concord.modeler.text.XMLCharacterDecoder;
 import org.concord.modeler.text.XMLCharacterEncoder;
 
 public class TextBox extends JPanel implements HtmlService, Searchable {
 
 	protected HTMLPane textBody;
 	private FrameHeader header;
 
 	private TextComponentPopupMenu popupMenu;
 	private JScrollPane scroller;
 	private MouseWheelListener mouseWheelListener;
 
 	private MouseAdapter popupListener = new MouseAdapter() {
 		public void mouseClicked(MouseEvent e) {
 			if (ModelerUtilities.isRightClick(e)) {
 				if (!isTextSelected())
 					return;
 				textBody.requestFocusInWindow();
 				if (popupMenu == null)
 					popupMenu = new TextComponentPopupMenu(textBody);
 				popupMenu.show(textBody, e.getX(), e.getY());
 			}
 			else {
 				textBody.getHighlighter().removeAllHighlights();
 			}
 		}
 	};
 
 	public TextBox(String text) {
 
 		super(new BorderLayout());
 		// setBackground(Color.white);
 
 		// This is a workaround for this Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6993691
		// This bug occurs only in applets--standalone applications are not affected
		if (Page.isApplet())
			new ParserDelegator();
 		textBody = new HTMLPane("text/html", text);
 		textBody.setBorder(BorderFactory.createEmptyBorder());
 		scroller = new JScrollPane(textBody, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scroller.setOpaque(false);
 		scroller.setBorder(BorderFactory.createEmptyBorder());
 		try {
 			mouseWheelListener = scroller.getMouseWheelListeners()[0];
 		}
 		catch (Exception e) {
 			// ignore
 		}
 		removeScrollerMouseWheelListener();
 
 		add(scroller, BorderLayout.CENTER);
 
 		addMouseListener(popupListener);
 
 		textBody.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				if (scroller.getVerticalScrollBar().isShowing() || scroller.getHorizontalScrollBar().isShowing())
 					addScrollerMouseWheelListener();
 			}
 
 			public void focusLost(FocusEvent e) {
 				removeScrollerMouseWheelListener();
 			}
 		});
 
 	}
 
 	public void addCloseButton(Runnable close) {
 		if (header == null)
 			header = new FrameHeader();
 		header.setCloseCode(close);
 		add(header, BorderLayout.NORTH);
 	}
 
 	public void addEditButton(Runnable edit) {
 		if (header == null)
 			header = new FrameHeader();
 		header.setEditCode(edit);
 		add(header, BorderLayout.NORTH);
 	}
 
 	public void removeScrollerMouseWheelListener() {
 		if (mouseWheelListener != null)
 			scroller.removeMouseWheelListener(mouseWheelListener);
 	}
 
 	public void addScrollerMouseWheelListener() {
 		if (mouseWheelListener != null)
 			scroller.addMouseWheelListener(mouseWheelListener);
 	}
 
 	public String getAttribute(String tag, String name) {
 		return textBody.getAttribute(tag, name);
 	}
 
 	public String getBackgroundImage() {
 		return textBody.getAttribute("body", "background");
 	}
 
 	public List<String> getImageNames() {
 		return textBody.getImageNames();
 	}
 
 	public void setViewPosition(int x, int y) {
 		setViewPosition(new Point(x, y));
 	}
 
 	public void setViewPosition(Point p) {
 		scroller.getViewport().setViewPosition(p);
 	}
 
 	public void setEmbeddedComponentAttributes() {
 		if (getContentType().equals("text/plain"))
 			return;
 		List<JComponent> list = getEmbeddedComponents();
 		textBody.wireEmbeddedComponents();
 		boolean isMacOS = System.getProperty("os.name").startsWith("Mac");
 		// boolean isWinXP=System.getProperty("os.name").startsWith("Windows XP");
 		for (JComponent comp : list) {
 			if (!isMacOS) {
 				if (comp instanceof JCheckBox || comp instanceof JRadioButton) {
 					comp.setOpaque(false);
 					comp.repaint();
 				}
 			}
 			else {
 				if (comp instanceof AbstractButton || comp instanceof JComboBox) {
 					comp.setOpaque(false);
 					comp.repaint();
 				}
 			}
 			// disable the Press-Enter trigger of the "submit" action from the last text field of a form
 			// the reason that we need to disable this is because we need to pass more query parameters
 			// than those explicitly presented by the form. Some of these parameters will be used to instruct
 			// the MW client what to do. For instance, passing "client=mw" will instruct MW to invoke MW
 			// instead of a conventional web browser. The ActionListener associated with FormView will only
 			// submit the data in the form when the ENTER key is pressed.
 			//
 			// The ideal way would be to also pass the additional query parameters to the FormView. But we
 			// may still need to remove the default ActionListener in order to avoid double Http requests.
 			// See the comments below for JButton.
 			if (comp instanceof JTextField) {
 				JTextField tf = (JTextField) comp;
 				ActionListener[] al = tf.getActionListeners();
 				if (al != null) {
 					for (ActionListener l : al) {
 						if (l instanceof FormView)
 							tf.removeActionListener(l);
 					}
 				}
 			}
 			// By default, an HTML form will send a request when the submit button is pressed. We want MW
 			// to handle this. So we disable the ForView listener associated the button. See HTMLPane for
 			// the MW implementation (which passes the form data through a HyperlinkEvent to Page).
 			else if (comp instanceof JButton) {
 				JButton button = (JButton) comp;
 				ActionListener[] al = button.getActionListeners();
 				if (al != null) {
 					for (ActionListener l : al) {
 						if (l instanceof FormView)
 							button.removeActionListener(l);
 					}
 				}
 			}
 		}
 	}
 
 	public List<JComponent> getEmbeddedComponents() {
 		return textBody.getEmbeddedComponents();
 	}
 
 	public List<JComponent> getEmbeddedComponents(Class c) {
 		return textBody.getEmbeddedComponents(c);
 	}
 
 	public void setEmbeddedComponentEnabled(int i, boolean b) {
 		List<JComponent> list = getEmbeddedComponents();
 		if (list == null || i >= list.size() || i < 0)
 			return;
 		list.get(i).setEnabled(b);
 	}
 
 	public void setEmbeddedComponentEnabled(String uid, boolean b) {
 		List<JComponent> list = getEmbeddedComponents();
 		if (list == null || uid == null)
 			return;
 		for (JComponent x : list) {
 			if (uid.equals(x.getName())) {
 				x.setEnabled(b);
 				break;
 			}
 		}
 	}
 
 	/** set the selection state of a component without or with causing the listeners on it to fire */
 	public void setComponentSelected(int i, boolean b, boolean execute) {
 		List<JComponent> list = getEmbeddedComponents();
 		if (list == null || i >= list.size() || i < 0)
 			return;
 		JComponent c = list.get(i);
 		if (c instanceof AbstractButton) {
 			AbstractButton ab = (AbstractButton) c;
 			if (execute) {
 				if (b)
 					ab.doClick();
 				else ab.setSelected(false);
 			}
 			else {
 				ItemListener[] il = ab.getItemListeners();
 				if (il != null)
 					for (ItemListener x : il)
 						ab.removeItemListener(x);
 				ActionListener[] al = ab.getActionListeners();
 				if (al != null)
 					for (ActionListener x : al)
 						ab.removeActionListener(x);
 				ChangeListener[] cl = ab.getChangeListeners();
 				if (cl != null)
 					for (ChangeListener x : cl)
 						ab.removeChangeListener(x);
 				ab.setSelected(b);
 				if (il != null)
 					for (ItemListener x : il)
 						ab.addItemListener(x);
 				if (al != null)
 					for (ActionListener x : al)
 						ab.addActionListener(x);
 				if (cl != null)
 					for (ChangeListener x : cl)
 						ab.addChangeListener(x);
 			}
 		}
 	}
 
 	/** set the selection state of a component without or with causing the listeners on it to fire */
 	public void setComponentSelected(String uid, boolean b, boolean execute) {
 		List<JComponent> list = getEmbeddedComponents();
 		if (list == null || uid == null)
 			return;
 		for (JComponent c : list) {
 			if (uid.equals(c.getName())) {
 				if (c instanceof AbstractButton) {
 					AbstractButton ab = (AbstractButton) c;
 					if (execute) {
 						if (b)
 							ab.doClick();
 						else ab.setSelected(false);
 					}
 					else {
 						ItemListener[] il = ab.getItemListeners();
 						if (il != null)
 							for (ItemListener x : il)
 								ab.removeItemListener(x);
 						ActionListener[] al = ab.getActionListeners();
 						if (al != null)
 							for (ActionListener x : al)
 								ab.removeActionListener(x);
 						ChangeListener[] cl = ab.getChangeListeners();
 						if (cl != null)
 							for (ChangeListener x : cl)
 								ab.removeChangeListener(x);
 						ab.setSelected(b);
 						if (il != null)
 							for (ItemListener x : il)
 								ab.addItemListener(x);
 						if (al != null)
 							for (ActionListener x : al)
 								ab.addActionListener(x);
 						if (cl != null)
 							for (ChangeListener x : cl)
 								ab.addChangeListener(x);
 					}
 				}
 				break;
 			}
 		}
 	}
 
 	/** set the selected index of a combo box without or with causing the listeners on it to fire */
 	public void setSelectedIndex(JComboBox cb, int iSelected, boolean execute) {
 		if (execute) {
 			cb.setSelectedIndex(iSelected);
 		}
 		else {
 			ActionListener[] al = cb.getActionListeners();
 			if (al != null)
 				for (ActionListener x : al)
 					cb.removeActionListener(x);
 			ItemListener[] il = cb.getItemListeners();
 			if (il != null)
 				for (ItemListener x : il)
 					cb.removeItemListener(x);
 			cb.setSelectedIndex(iSelected);
 			if (al != null)
 				for (ActionListener x : al)
 					cb.addActionListener(x);
 			if (il != null)
 				for (ItemListener x : il)
 					cb.addItemListener(x);
 		}
 	}
 
 	public void setContentType(String type) {
 		textBody.setContentType(type);
 	}
 
 	public String getContentType() {
 		return textBody.getContentType();
 	}
 
 	public void addHotlinkListener(HotlinkListener listener) {
 		textBody.addHotlinkListener(listener);
 	}
 
 	public void removeHotlinkListener(HotlinkListener listener) {
 		textBody.removeHotlinkListener(listener);
 	}
 
 	public HotlinkListener[] getHotlinkListeners() {
 		return textBody.getHotlinkListeners();
 	}
 
 	public void setBase(File file) {
 		try {
 			setBase(file.toURI().toURL());
 		}
 		catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setBase(String url) {
 		try {
 			setBase(new URL(url));
 		}
 		catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setBase(URL u) {
 		textBody.setBase(u);
 	}
 
 	public URL getBase() {
 		return textBody.getBase();
 	}
 
 	public boolean isTextSelected() {
 		return textBody.getSelectedText() != null;
 	}
 
 	public void addMouseListener(MouseListener ml) {
 		super.addMouseListener(ml);
 		textBody.addMouseListener(ml);
 	}
 
 	public void removeMouseListener(MouseListener ml) {
 		super.removeMouseListener(ml);
 		textBody.removeMouseListener(ml);
 	}
 
 	public void setEditable(boolean b) {
 		textBody.setEditable(b);
 	}
 
 	/** Sets the current URL being displayed. */
 	public void setPage(String url) throws IOException {
 		if (url == null)
 			return;
 		textBody.setPage(url);
 	}
 
 	public void setText(String text) {
 		if (text == null) {
 			setContentType("text/html");
 		}
 		else {
 			setContentType(text.trim().startsWith("<") ? "text/html" : "text/plain");
 		}
 		textBody.setText(text);
 	}
 
 	public String getText() {
 		// FIXME: workaround for the visited link color problem! This quick fix prevents color 000099 to render.
 		return textBody.getText().replaceAll("(?)\\s+color=\"#000099\"", "");
 	}
 
 	public JTextComponent getTextComponent() {
 		return textBody;
 	}
 
 	public HTMLPane getHtmlPane() {
 		return textBody;
 	}
 
 	public void addSelfScriptListener(SelfScriptListener l) {
 		textBody.addSelfScriptListener(l);
 	}
 
 	public void removeSelfScriptListener(SelfScriptListener l) {
 		textBody.removeSelfScriptListener(l);
 	}
 
 	public void cacheLinkedFiles(String codeBase) {
 		textBody.cacheLinkedFiles(codeBase);
 	}
 
 	/** encode the HTML text so that it can be embedded into XML */
 	protected String encodeText() {
 		return XMLCharacterEncoder.encode(getText());
 	}
 
 	/** decode the encoded HTML text embedded in XML */
 	public void decodeText(String s) {
 		setText(XMLCharacterDecoder.decode(s));
 	}
 
 	public void setBackground(Color c) {
 		super.setBackground(c);
 		if (textBody != null)
 			textBody.setBackground(c);
 		if (scroller != null)
 			scroller.getViewport().setBackground(c);
 	}
 
 	public void setOpaque(boolean b) {
 		super.setOpaque(b);
 		if (textBody != null)
 			textBody.setOpaque(b);
 		if (scroller != null)
 			scroller.getViewport().setOpaque(b);
 	}
 
 	public boolean isOpaque() {
 		if (textBody == null)
 			return super.isOpaque();
 		return textBody.isOpaque();
 	}
 
 }
