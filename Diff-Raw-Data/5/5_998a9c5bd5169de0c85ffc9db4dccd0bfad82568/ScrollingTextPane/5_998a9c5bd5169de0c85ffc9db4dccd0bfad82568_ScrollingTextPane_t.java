 package telnet;
 
 import javax.swing.JOptionPane;
 import java.util.Vector;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.KeyListener;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.LineBorder;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.plaf.basic.BasicScrollBarUI;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.undo.UndoManager;
 import static java.lang.Math.*;
 
 @SuppressWarnings("serial")
 public class ScrollingTextPane extends JScrollPane {
 
 	private TextPane textPane;
 	private UndoManager undoManager = new UndoManager();
 
 	public ScrollingTextPane() {
 		super();
 		textPane = new TextPane();
 		setBackground(Color.BLACK);
 		setBorder(new LineBorder(Color.RED));
 		getVerticalScrollBar().setUI(new CoolScrollBarUI());
 		getHorizontalScrollBar().setUI(new CoolScrollBarUI());
 		makeCorner();
 		setViewportView(textPane);
 	}
 
 	public void addKeyListener(KeyListener keyListener) {
 		textPane.addKeyListener(keyListener);
 	}
 
 	private void makeCorner() {
 		JTextField corner = new JTextField();
 		corner.setBackground(Color.BLACK);
 		corner.setForeground(Color.GREEN);
 		corner.setBorder(new LineBorder(Color.BLACK));
 		corner.setText(" X");
 		corner.setFocusable(false);
 		corner.setEditable(false);
 		setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, corner);
 	}
 
 	public TextPane getTextPane() {
 		return textPane;
 	}
 
 	public UndoManager getUndoManager() {
 		return undoManager;
 	}
 
 	public class TextPane extends JTextPane
 			implements
 				CaretListener,
 				UndoableEditListener {
 
 		private Vector<Object> highlights = new Vector<Object>();
 		private DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
 				Color.RED);
 
 		public TextPane() {
 			super();
 			setBackground(Color.BLACK);
 			setForeground(Color.GREEN);
 			setCaretColor(Color.RED);
 			setBorder(new LineBorder(Color.BLACK));
 			setFocusTraversalKeysEnabled(false);
 			getDocument().addUndoableEditListener(this);
 			addCaretListener(this);
 			setSelectedTextColor(Color.RED);
 			setSelectionColor(Color.GREEN);
 			setFont(new Font("Courier New", Font.PLAIN, 12));
 		}
 
 		public synchronized void append(String s) {
 			setText(getText() + s + "\n");
 		}
 
 		public synchronized void setText(String s) {
 			super.setText(s);
 			setCaretPosition(getDocument().getLength());
 		}
 
 		public void highlight(int p0, int p1) {
 			try {
 				highlights.add(getHighlighter().addHighlight(p0, p1,
 						highlightPainter));
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void undoableEditHappened(UndoableEditEvent e) {
 			undoManager.addEdit(e.getEdit());
 		}
 
 		@Override
 		public void caretUpdate(CaretEvent e) {
 			if (getText() != null && getText().length() != 0) {
 				clearHighlights();
 				int dot = e.getDot();
 				int mark = e.getMark();
 				if (abs(dot - mark) == 1 || dot == mark) {
 					int pos = max(dot, mark) - 1;
 					int count = 0;
 					char[][] bracketTypes = {{'(', ')'}, {'[', ']'}, {'{', '}'}};
					String toSearch = getText();
 					here : for (char[] c : bracketTypes) {
 						if (pos >= 0 && toSearch.charAt(pos) == c[1]) {
 							for (int i = pos; i >= 0; i--) {
 								if (toSearch.charAt(i) == c[1]) {
 									count++;
 								} else if (toSearch.charAt(i) == c[0]) {
 									count--;
 								}
 								if (count == 0) {
 									highlight(i, i + 1);
 									if (dot == mark) {
 										highlight(dot - 1, dot);
 									}
 									break here;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		public void promptFind() {
 			Object findObj = JOptionPane
 					.showInputDialog(null, "What would you like to find? ",
 							"Find All", JOptionPane.QUESTION_MESSAGE, null, null,
 							getSelectedText());
 			if (findObj != null) {
 				String find = findObj.toString();
 				if (!find.equals("")) {
 					if (getSelectedText() != null
 							&& getSelectedText().equals(find)) {
 						clearHighlights();
 					} else {
 						getHighlighter().removeAllHighlights();
 					}
					String toSearch = getText();
 					for (int i = 0; i < toSearch.length() - find.length() + 1; i++) {
 						int temp = toSearch.indexOf(find, i);
 						if (temp == -1) {
 							break;
 						} else {
 							i = temp;
 							highlight(i, i + find.length());
 							i += find.length() - 1;
 						}
 
 					}
 				}
 			}
 		}
 
 		private void clearHighlights() {
 			for (Object highlight : highlights) {
 				getHighlighter().removeHighlight(highlight);
 			}
 		}
 
 	}
 
 	private class CoolScrollBarUI extends BasicScrollBarUI {
 
 		private boolean horizontal = false;
 		private Color thumbCoreColor;
 
 		protected void configureScrollBarColors() {
 			thumbColor = Color.BLACK;
 			trackColor = thumbColor;
 			thumbHighlightColor = Color.RED;
 			thumbCoreColor = Color.GREEN;
 		}
 
 		protected void paintThumb(Graphics g, JComponent c,
 				Rectangle thumbBounds) {
 			super.paintThumb(g, c, thumbBounds);
 			if (horizontal) {
 				if (thumbBounds.width > 20) {
 					g.setColor(thumbCoreColor);
 					g.drawLine(thumbBounds.x + 10, thumbBounds.height / 2,
 							thumbBounds.width + thumbBounds.x - 10,
 							thumbBounds.height / 2);
 				}
 			} else {
 				if (thumbBounds.height > 20) {
 					g.setColor(thumbCoreColor);
 					g.drawLine(thumbBounds.width / 2, thumbBounds.y + 10,
 							thumbBounds.width / 2, thumbBounds.height
 									+ thumbBounds.y - 10);
 				}
 			}
 
 		}
 
 		protected JButton createDecreaseButton(int orientation) {
 			return new ArrowButton(orientation);
 
 		}
 
 		protected JButton createIncreaseButton(int orientation) {
 			return new ArrowButton(orientation);
 		}
 
 		private class ArrowButton extends JButton {
 
 			private int orientation;
 
 			ArrowButton(int orientation) {
 				setPreferredSize(new Dimension(18, 18));
 				this.orientation = orientation;
 				horizontal = (orientation == 3 || orientation == 7);
 			}
 
 			public void paint(Graphics g) {
 				g.setColor(thumbColor);
 				g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
 				g.setColor(thumbHighlightColor);
 				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
 				g.setColor(thumbCoreColor);
 				switch (orientation) {
 					case 1 :
 						new Triangle(getWidth() / 2, 3, 2, getHeight() - 4,
 								getWidth() - 3, getHeight() - 4).draw(g);
 						break;
 					case 5 :
 						new Triangle(getWidth() / 2, getHeight() - 4, 2, 3,
 								getWidth() - 3, 3).draw(g);
 						break;
 					case 7 :
 						new Triangle(2, getHeight() / 2, getWidth() - 4, 3,
 								getWidth() - 4, getHeight() - 4).draw(g);
 						break;
 					case 3 :
 						new Triangle(getWidth() - 4, getHeight() / 2, 2, 3, 2,
 								getHeight() - 4).draw(g);
 						break;
 				}
 			}
 
 			private class Triangle {
 				private int[] x = new int[3];
 				private int[] y = new int[3];
 
 				public Triangle(int x1, int y1, int x2, int y2, int x3, int y3) {
 					x[0] = x1;
 					x[1] = x2;
 					x[2] = x3;
 					y[0] = y1;
 					y[1] = y2;
 					y[2] = y3;
 				}
 
 				public void draw(Graphics g) {
 					g.drawPolygon(x, y, 3);
 				}
 			}
 		}
 	}
 }
