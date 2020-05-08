 package classviewer;
 
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkEvent.EventType;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import classviewer.changes.EdxModelAdapter;
 import classviewer.model.CourseModel;
 import classviewer.model.CourseModelListener;
 import classviewer.model.CourseRec;
 import classviewer.model.OffRec;
 import classviewer.model.Status;
 
 /**
  * Show list of offerings, let set offering status, show HTML with course
  * description. Reacts to selections in the course list and calendar.
  * 
  * @author TK
  */
 public class DetailsFrame extends NamedInternalFrame implements
 		CourseSelectionListener, CourseModelListener {
 
 	private JTable offeringTable;
 	private OfferingTableModel tableModel;
 	private JTextPane htmlPane;
 	private CourseRec selectedCourse = null;
 
 	public DetailsFrame(CourseModel model, final Settings settings) {
 		super("Details", model);
 		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		this.setContentPane(split);
 		model.addListener(this);
 
 		Dimension dim = new Dimension(400, 200);
 		this.setMinimumSize(dim);
 		this.setSize(dim);
 
 		tableModel = new OfferingTableModel(model);
 		offeringTable = new JTable(tableModel);
 		offeringTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		offeringTable.setDefaultRenderer(Status.class, new StatusCellRenderer(
 				settings));
 		offeringTable
 				.getColumnModel()
 				.getColumn(0)
 				.setCellEditor(
 						new DefaultCellEditor(new StatusComboBox(Status
 								.getAll(), settings)));
 		offeringTable.getColumnModel().getColumn(1)
 				.setCellRenderer(new DefaultTableCellRenderer() {
 					@Override
 					public Component getTableCellRendererComponent(
 							JTable table, Object value, boolean isSelected,
 							boolean hasFocus, int row, int column) {
 						JLabel res = (JLabel) super
 								.getTableCellRendererComponent(table, value,
 										isSelected, hasFocus, row, column);
 						res.setHorizontalAlignment(SwingConstants.TRAILING);
 						return res;
 					}
 				});
 		offeringTable.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getButton() != MouseEvent.BUTTON3)
 					return;
 				int col = offeringTable.columnAtPoint(e.getPoint());
 				if (col != 2)
 					return;
 				int row = offeringTable.rowAtPoint(e.getPoint());
 				final OffRec off = tableModel.getOfferingAt(row);
 				// Only show for EdX
				if (off.getId() >= 0)
 					return;
 				JPopupMenu offeringPopupMenu = new JPopupMenu();
 				AbstractAction askToLoadEdXDuration = new AbstractAction(
 						"Load duration") {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						EdxModelAdapter edx = new EdxModelAdapter();
 						try {
 							if (edx.loadClassDuration(
 									off,
 									new Boolean(
 											settings.getString(Settings.IGNORE_SSL_CERT)))) {
 								tableModel.saveModelAndInform();
 							} else {
 								JOptionPane
 										.showMessageDialog(null,
 												"Could not figure out the duration from EdX webpage");
 							}
 						} catch (IOException ex) {
 							// wait.setVisible(false);
 							JOptionPane.showMessageDialog(null,
 									"Failed to load EdX data:\n" + ex);
 							return;
 						}
 					}
 				};
 				offeringPopupMenu.add(askToLoadEdXDuration);
 				offeringPopupMenu.show(offeringTable, e.getX(), e.getY());
 			}
 		});
 
 		JScrollPane tablePane = new JScrollPane(offeringTable);
 		Dimension minTableSize = new Dimension(50, 70);
 		tablePane.setMinimumSize(minTableSize);
 		split.setLeftComponent(tablePane);
 		htmlPane = new JTextPane();
 		split.setRightComponent(new JScrollPane(htmlPane));
 		htmlPane.setContentType("text/html");
 		htmlPane.setEditable(false);
 		htmlPane.addHyperlinkListener(new HyperlinkListener() {
 			@Override
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				if (EventType.ACTIVATED.equals(e.getEventType())
 						&& e.getURL() != null) {
 					Desktop desktop = java.awt.Desktop.getDesktop();
 					try {
 						desktop.browse(e.getURL().toURI());
 					} catch (Exception e1) {
 						JOptionPane.showMessageDialog(null,
 								"Cannot open browser for\n" + e.getURL() + "\n"
 										+ e1.getMessage());
 					}
 				}
 			}
 		});
 
 		offeringTable.getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 					@Override
 					public void valueChanged(ListSelectionEvent e) {
 						updateHtml();
 					}
 				});
 	}
 
 	public void addModelReloadListener(CourseModelListener lnr) {
 		tableModel.addModelReloadListener(lnr);
 	}
 
 	@Override
 	public void courseSelected(CourseRec course) {
 		this.selectedCourse = course;
 
 		tableModel.courseSelected(course);
 		setColumnWidth();
 		updateHtml();
 	}
 
 	private void updateHtml() {
 		if (selectedCourse == null) {
 			htmlPane.setText("");
 		} else {
 			String text = "<html>" + selectedCourse.getLongHtml();
 			if (offeringTable.getSelectedRow() >= 0) {
 				text += "<br/>"
 						+ selectedCourse.getOfferings()
 								.get(offeringTable.getSelectedRow())
 								.getLongHtml();
 			}
 			htmlPane.setText(text);
 		}
 	}
 
 	private void setColumnWidth() {
 		// This method is called after the data changes (a new course is selected).
 		// Only need to reset fixed sized columns. The rest will adjust.
 		// offeringTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		int cw = 20;
 		TableColumn column = offeringTable.getColumnModel().getColumn(0);
 		column.setPreferredWidth(cw);
 		column.setWidth(cw);
 		column.setMaxWidth(cw);
 //		cw = (offeringTable.getWidth() - cw) / 3;
 //		for (int i = 1; i < 4; i++) {
 //			column = offeringTable.getColumnModel().getColumn(i);
 //			column.setPreferredWidth(cw);
 //			column.setWidth(cw);
 //			column.setMaxWidth(cw);
 //		}
 	}
 
 	@Override
 	public void courseStatusChanged(CourseRec course) {
 		if (course == selectedCourse && course != null)
 			courseSelected(selectedCourse);
 	}
 
 	@Override
 	public void modelUpdated() {
 		courseSelected(selectedCourse);
 	}
 
 	@Override
 	public void filtersUpdated() {
 		// Noop
 	}
 }
