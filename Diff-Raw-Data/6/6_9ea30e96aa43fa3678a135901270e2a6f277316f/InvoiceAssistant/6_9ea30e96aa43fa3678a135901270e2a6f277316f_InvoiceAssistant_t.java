 package blue.hotel.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 
 import blue.hotel.logic.SaveReservation;
 import blue.hotel.model.Customer;
 import blue.hotel.model.Invoice;
 import blue.hotel.model.Reservation;
 import blue.hotel.storage.DAO;
 import blue.hotel.storage.DAOException;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.toedter.calendar.JDateChooser;
 
 public class InvoiceAssistant extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	public JComboBox customerComboBox;
 	public JList reservationList;
 	public InvoiceAssistantReservationListModelLongNamesInJavaAreFun reservationModel;
 	public JDateChooser textDeparture;
 	public JLabel lblTotalAmount;
 
 	private final JLabel lblNewLabel_1 = new JLabel("Total amount:");
 	public InvoiceAssistant() {
 		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
 		gbc_panel_6.anchor = GridBagConstraints.NORTH;
 		gbc_panel_6.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel_6.gridx = 0;
 		gbc_panel_6.gridy = 0;
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.GROWING_BUTTON_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("514px:grow"),
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("75px"),
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("42px"),}));
 
 		JPanel panel_3 = new JPanel();
 		add(panel_3, "1, 2, fill, top");
 		panel_3.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_3.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(510px;default):grow"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("top:max(221dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(24dlu;default):grow"),}));
 
 		JLabel lblNewLabel = new JLabel("Customer:");
 		panel_3.add(lblNewLabel, "2, 2");
 
 		customerComboBox = new JComboBox();
 		panel_3.add(customerComboBox, "4, 2, fill, default");
 
 		JLabel lblReservationsToPay = new JLabel("Reservations to pay:");
 		panel_3.add(lblReservationsToPay, "2, 4");
 
 		reservationList = new JList();
 		reservationList.setValueIsAdjusting(true);
 		reservationList.setVisibleRowCount(30);
 		reservationList.setLayoutOrientation(JList.VERTICAL_WRAP);
 		reservationList.setFixedCellWidth(400);
 		reservationModel = new InvoiceAssistantReservationListModelLongNamesInJavaAreFun();
 		reservationList.setModel(reservationModel);
 		reservationList.setCellRenderer(new CheckListRenderer());
 		reservationList.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent event) {
 				JList list = (JList) event.getSource();
 
 				int index = list.locationToIndex(event.getPoint());
				if (index < 0) {
					return;
				}
 				CheckListItem item = (CheckListItem)
 				list.getModel().getElementAt(index);
 
 				// Toggle selected state
 				item.setSelected(! item.isSelected());
 
 				// Repaint cell
 				list.repaint(list.getCellBounds(index, index));
 
 				// Ziemlich fett
 				double geodreieck = Pausenrap.taschenrechnerBorgen(reservationModel.getSelectedReservations());
 				lblTotalAmount.setText("" + geodreieck);
 			}
 		});
 
 		JScrollPane scrollPane = new JScrollPane(reservationList);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		panel_3.add(scrollPane, "4, 4, fill, fill");
 
 		JLabel lblDepartureDate = new JLabel("Departure date:");
 		panel_3.add(lblDepartureDate, "2, 6, default, center");
 
 		JPanel panel_4 = new JPanel();
 		panel_4.setBorder(new EmptyBorder(0, 0, 0, 0));
 		panel_3.add(panel_4, "4, 6, fill, top");
 		panel_4.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("129px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("81px"),},
 			new RowSpec[] {
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("26px"),}));
 
 		textDeparture = new JDateChooser();
 		panel_4.add(textDeparture, "1, 2, fill, center");
 
 		JButton btnNewButton = new JButton("Today");
 		panel_4.add(btnNewButton, "3, 2, fill, top");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			    textDeparture.setDate(new Date());
 			}
 		});
 
 		JPanel panel_5 = new JPanel();
 		add(panel_5, "1, 4, fill, top");
 		panel_5.setBorder(new TitledBorder(null, "Amount", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_5.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("122px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("max(510px;default):grow"),},
 			new RowSpec[] {
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("47px"),}));
 		panel_5.add(lblNewLabel_1, "2, 2, left, top");
 
		lblTotalAmount = new JLabel("0.0");
 		panel_5.add(lblTotalAmount, "4, 2, left, top");
 
 		JPanel panel_2 = new JPanel();
 		add(panel_2, "1, 6, right, top");
 		panel_2.setLayout(new BorderLayout());
 		JButton btnPrintInvoice = new JButton("Print invoice");
 		panel_2.add(btnPrintInvoice, BorderLayout.EAST);
 		btnPrintInvoice.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				List<Reservation> reservations = reservationModel.getSelectedReservations();
 				Customer customer = (Customer)customerComboBox.getSelectedItem();
 				Date date = textDeparture.getDate();
 
 				if (customer == null) {
 					JOptionPane.showMessageDialog(InvoiceAssistant.this, "No customer selected.");
 					return;
 				} else if (reservations.size() == 0) {
 					JOptionPane.showMessageDialog(InvoiceAssistant.this, "No reservations selected.");
 					return;
 				} else if (date == null) {
 					JOptionPane.showMessageDialog(InvoiceAssistant.this, "no date selected.");
 					return;
 				}
 
 				Invoice invoice = new Invoice();
 				invoice.setCustomer(customer);
 				invoice.setDate(date);
 				invoice.setReservations(reservations);
 				try {
 					DAO.getInstance().create(invoice);
 				} catch (DAOException e1) {
 					JOptionPane.showMessageDialog(InvoiceAssistant.this, "Cannot create invoice.");
 					e1.printStackTrace();
 				}
 
 				try {
 					String filename = "invoice_" + invoice.getId() + ".html";
 					invoice.setFilename(filename);
 					DAO.getInstance().update(invoice);
 
 					PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filename))));
 					w.write("<html><head><title>Rechnung</title></head><body>");
 					w.write("<h1>Rechnung " + invoice.getId() + "</h1>");
 					w.write("<p>Kunde: " + customer.getName() + "<br>" + customer.getAddress() + "</p>");
 					w.write("<h2>Rechnungspositionen</h2><ul>");
 					for (Reservation r : reservations) {
 						w.write("<li>" + r.toString() + " // <b>Price:</b> " + r.getPrice() + " (Discount: "+r.getDiscount()+"%)</li>");
 					}
 					w.write("</ul>");
 					w.write("<p>Total price: <strong>" + Pausenrap.taschenrechnerBorgen(reservations) + "</strong></p>");
 					w.write("</body></html>");
 					w.close();
 
 					Desktop.getDesktop().open(new File(filename));
 
 					for(Reservation res : invoice.getReservations()) {
 						res.setInvoice(invoice);
 						res = SaveReservation.save(res, res.getRooms());
 					}
 
 				} catch (Exception e3) {
 					e3.printStackTrace();
 				}
 			}
 		});
 
 		try {
 			for (Customer c: DAO.getInstance().getAll(Customer.class)) {
 				customerComboBox.addItem(c);
 			}
 		} catch (DAOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		setSize(715, 650);
 	}
 
 }
