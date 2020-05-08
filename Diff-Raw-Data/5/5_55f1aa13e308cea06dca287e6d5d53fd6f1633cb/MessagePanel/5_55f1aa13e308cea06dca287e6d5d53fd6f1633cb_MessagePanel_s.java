 package net.rptools.maptool.client.ui.commandpanel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.TextMessage;
 import ca.odell.renderpack.HTMLTableCellRenderer;
 
 public class MessagePanel extends JPanel {
 
 	private JTable messageTable;
 	private JScrollPane scrollPane;
 
 	
 	public MessagePanel() {
 		setLayout(new BorderLayout());
 		
 		messageTable = createMessageTable();
 		
 		scrollPane = new JScrollPane(messageTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setBorder(null);
 		scrollPane.getViewport().setBorder(null);
 		scrollPane.getViewport().setBackground(Color.white);
 		scrollPane.getVerticalScrollBar().addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				
 				boolean lock = (scrollPane.getSize().height + scrollPane.getVerticalScrollBar().getValue()) < scrollPane.getVerticalScrollBar().getMaximum();
 
 				// The user has manually scrolled the scrollbar, Scroll lock time baby !
 				MapTool.getFrame().getCommandPanel().getScrollLockButton().setSelected(lock);
 			}
 		});
 		
 		add(BorderLayout.CENTER, scrollPane);
 	}
 	
 	public void refreshRenderer() {
 		messageTable.getColumnModel().getColumn(0).setCellRenderer(new MessageCellRenderer());
 	}
 
 	public String getMessagesText() {
 		StringBuilder builder = new StringBuilder();
 		
 		builder.append("<html><body>");
 		for (int i = 0; i < messageTable.getModel().getRowCount(); i++) {
 			
 			builder.append("<div>\n\t");
 			builder.append(messageTable.getModel().getValueAt(i, 0));
 			builder.append("</div>\n");
 		}
 		builder.append("</body></html>");
 		
 		return builder.toString();
 	}
 	
 	public void clearMessages() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
				messageTable.setModel(new DefaultTableModel());
 			}
 		});
 	}
 	
 	public void addMessage(final TextMessage message) {
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				DefaultTableModel model = (DefaultTableModel) messageTable.getModel();
 				model.addRow(new Object[]{message});
 			}
 		});
 	}
 
 	private JTable createMessageTable() {
 		messageTable = new JTable() {
 			@Override
 			public String getToolTipText(MouseEvent event) {
 
 		        Point p = event.getPoint();
 
 		        // Locate the renderer under the event location
 		        int hitRowIndex = rowAtPoint(p);
 
 		        if (hitRowIndex != -1) {
 					DefaultTableModel model = (DefaultTableModel) messageTable.getModel();
 					TextMessage message = (TextMessage) model.getValueAt(hitRowIndex, 0);
 					return message.getSource();
 		        }
 
 		        return null;
 			}
 		};
 		messageTable.setModel(new DefaultTableModel(new Object[][]{}, new Object[]{""}));
 		messageTable.setTableHeader(null);
 		messageTable.setShowGrid(false);
 		messageTable.setBackground(Color.white);
         messageTable.setDefaultEditor(Object.class, null);
 		
 		// Always scroll to the bottom of the chat window on new messages
 		messageTable.addComponentListener(new ComponentListener() {
 			public void componentHidden(ComponentEvent e) {
 			}
 			public void componentMoved(ComponentEvent e) {
 			}
 			public void componentResized(ComponentEvent e) {
 				if (messageTable.getRowCount() == 0) {
 					return;
 				}
 				
 				if (!MapTool.getFrame().getCommandPanel().getScrollLockButton().isSelected()) {
 					TextMessage lastMessage = (TextMessage) messageTable.getValueAt(messageTable.getRowCount()-1, 0); 
 					Rectangle rowBounds = new Rectangle(0, messageTable.getSize().height, 1, 1);
 					messageTable.scrollRectToVisible(rowBounds);
 				}
 			}
 			public void componentShown(ComponentEvent e) {
 			}
 		});
 		
 		refreshRenderer();
 		
 		return messageTable;
 	}
 	
 	private static class MessageCellRenderer extends HTMLTableCellRenderer {
 		public MessageCellRenderer(){
 			super(true);
 			styleSheet.addRule("body { font-family: sans-serif; font-size: " + AppPreferences.getFontSize() + "pt}");
 	    }
 		
 		@Override
 		public void writeObject(StringBuffer buff, JTable table, Object value, boolean isSelected, boolean isFocused, int row, int col) {
 
 			TextMessage message = (TextMessage) value;
 			
 			String text = message.getMessage();
 			
 			// Roll validation
 			text = text.replaceAll("\\[roll\\s*([^\\]]*)]", "&#171;$1&#187;");
 			
 			buff.append("<html><body>");
 			buff.append(text);
 			buff.append("</body></html>");
 		}
 	}
 }
