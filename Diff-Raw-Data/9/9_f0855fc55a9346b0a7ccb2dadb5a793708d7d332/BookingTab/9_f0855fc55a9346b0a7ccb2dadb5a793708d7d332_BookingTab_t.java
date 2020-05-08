 package hotel.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import net.sf.nachocalendar.CalendarFactory;
 import net.sf.nachocalendar.components.DateField;
 
 import hotel.controller.BookingCtrl;
 import hotel.controller.GuestCtrl;
 import hotel.core.Guest;
 import hotel.core.Booking;
 import hotel.utils.DateUtils;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 public class BookingTab extends JPanel
 {
     private JTable bookingTable;
     private BookingCtrl bookingCtrl;
     private JComponent[] bookingInputs;
     private JLabel[] bookingLabels;
     private GuestCtrl guestCtrl;
     private final BookingTab bookingTab = this;
     
     public BookingTab()
     {
         this.setLayout(new GridLayout(1, 0));
         if (GUI.getHotel() != null)
         {
             String hotelName = GUI.getHotel().getName();
             bookingCtrl = new BookingCtrl(hotelName);
             guestCtrl = new GuestCtrl(GUI.getHotel().getName());
         }
         else
         {
             bookingCtrl = null;
             guestCtrl = null;
         }
         
         bookingLabels = new JLabel[]{new JLabel("Guest"), 
             new JLabel("Nr of adults"), new JLabel("Arrival Date"), 
             new JLabel("Leaving Date"), new JLabel("Discount")};
         bookingInputs = new JComponent[5];
                 
         String[] bookingColumns = new String[] {"#", "id", "Guest Name", 
             "Room Nr", "Arrival Date", "Leaving Date", "Discount"};
         bookingTable = GUI.createTable(bookingColumns);
         bookingTable.getColumnModel().getColumn(0).setMaxWidth(30);
         TableColumn idColumn = bookingTable.getColumnModel().getColumn(1); 
         idColumn.setMaxWidth(0);
         idColumn.setMinWidth(0);
         idColumn.setPreferredWidth(0);
         updateTable();
         final TableRowSorter<TableModel> bookingSorter = new TableRowSorter<TableModel>();
         bookingSorter.setModel(bookingTable.getModel());
         bookingTable.setRowSorter(bookingSorter);
         
         JScrollPane bookingScrollPane = new JScrollPane(bookingTable);
         
         JButton addBooking = new JButton("Add");
         addBooking.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", 
                             bookingTab);
                 else
                     addBookingCB();
             }
         });
         JButton editBooking = new JButton("Edit");
         editBooking.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", 
                             bookingTab);
                 else if (bookingTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some booking.", "Error", 
                             bookingTab);
                 else 
                     editBookingCB(bookingTable.getSelectedRow());
             }
         });
         
         JButton removeBooking = new JButton("Remove");
         removeBooking.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", 
                             bookingTab);
                 else if (bookingTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some booking.", "Error", 
                             bookingTab);
                 else 
                     removeBookingCB(bookingTable.getSelectedRow());
             }
         });
         
         JToolBar bookingToolBar = new JToolBar();
         bookingToolBar.setFloatable(false);
         bookingToolBar.add(addBooking);
         bookingToolBar.add(editBooking);
         bookingToolBar.add(removeBooking);
         
         String[] fieldNames = new String[] {"Guest Name", "Room Nr", 
             "Arrival Date", "Leaving Date", "Discount"};
         final JComboBox bookingfilterBox = new JComboBox(fieldNames);
         final JTextField bookingfilterField = new JTextField();
         bookingfilterField.addKeyListener(new KeyListener() {
 
             @Override public void keyTyped(KeyEvent e) {}
             @Override public void keyPressed(KeyEvent e) {}
             
             @Override 
             public void keyReleased(KeyEvent e) 
             {
                 String text = bookingfilterField.getText();
                 int i = bookingfilterBox.getSelectedIndex() + 2;
                 if (text.isEmpty())
                     bookingSorter.setRowFilter(null);
                 else
                     bookingSorter.setRowFilter(RowFilter.regexFilter(text, i));
             }
         });
 
         bookingToolBar.addSeparator();
         bookingToolBar.add(new JLabel("Filter by   "));
         bookingToolBar.add(bookingfilterBox);
         bookingToolBar.addSeparator();
         bookingToolBar.add(bookingfilterField);
         bookingToolBar.addSeparator();
         
         JPanel bookingPanel = new JPanel();
         bookingPanel.setLayout(new BorderLayout());
         bookingPanel.add(bookingToolBar, BorderLayout.PAGE_START);
         bookingPanel.add(bookingScrollPane);
                 
         this.add(bookingPanel);
         this.setVisible(true);
     }
     
     private void addBookingCB()
     {
         if (guestCtrl == null)
             guestCtrl = new GuestCtrl(GUI.getHotel().getName());
         if (bookingCtrl == null)
             bookingCtrl = new BookingCtrl(GUI.getHotel().getName());
         
         if (guestCtrl.getGuestCount() == 0)
         {
             GUI.showError("No guests were added.", "Error", this);
             return;
         }
         
         String[] guestNames = new String[guestCtrl.getGuestCount()];
         for (int i = 0; i < guestNames.length; i++)
             guestNames[i] = guestCtrl.getGuestById(i).getName();
         bookingInputs[0] = new JComboBox(guestNames);
         ((JComboBox)bookingInputs[0]).setSelectedIndex(0);
         bookingInputs[1] = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
         bookingInputs[2] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[2]).setValue(DateUtils.getToday());
         bookingInputs[3] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[3]).setValue(DateUtils.getToday());
         bookingInputs[4] = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
         final JDialog addDialog = GUI.dialog("Add Booking", bookingLabels, 
                 bookingInputs);
         JPanel myPanel = (JPanel)addDialog.getContentPane().getComponent(0);
         
         final JComboBox guestNamesCB = ((JComboBox)myPanel.getComponent(1));
         final JSpinner nrOfGuests = ((JSpinner)myPanel.getComponent(3));
         final DateField arrivalDateField = ((DateField)myPanel.getComponent(5));
         final DateField leavingDateField = ((DateField)myPanel.getComponent(7));
         final JSpinner discount = (JSpinner)myPanel.getComponent(9);
         
         JButton okButton = ((JButton)myPanel.getComponent(10));
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String guestName = (String)guestNamesCB.getSelectedItem();
                 Date arrivalDate = (Date)arrivalDateField.getValue();
                 Date leavingDate = (Date)leavingDateField.getValue();
                 int discountI = (Integer)discount.getValue();
                 int nrOfGuestsI = (Integer)nrOfGuests.getValue();
                 try
                 {
                     Guest guest = guestCtrl.getGuestByName(guestName);
                     bookingCtrl.addBooking(guest, nrOfGuestsI, arrivalDate, 
                             leavingDate, discountI);
                 }
                 catch (Exception exc)
                 {
                     GUI.showError(exc.getMessage(), "Error", bookingTab);
                     return;
                 }
                 addDialog.setVisible(false);
                 updateTable();
                 GUI.updateRooms();
             }
         });
         addDialog.setVisible(true);
     }
     
     private void editBookingCB(final int id)
     {
         if (guestCtrl == null)
             guestCtrl = new GuestCtrl(GUI.getHotel().getName());
         if (bookingCtrl == null)
             bookingCtrl = new BookingCtrl(GUI.getHotel().getName());
         
         Booking booking = bookingCtrl.getBooking(id);
         
         String[] guestNames = new String[guestCtrl.getGuestCount()];
         for (int i = 0; i < guestNames.length; i++)
             guestNames[i] = guestCtrl.getGuestById(i).getName();
         bookingInputs[0] = new JComboBox(guestNames);
         ((JComboBox)bookingInputs[0]).setSelectedItem(booking.getGuest().
                 getName());
         bookingInputs[1] = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
         ((JSpinner)bookingInputs[1]).setValue(booking.getRoom().
                 getNrOfBedrooms());
         bookingInputs[2] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[2]).setValue(booking.getArrivalDate());
         bookingInputs[3] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[3]).setValue(booking.getLeavingDate());
         bookingInputs[4] = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
         ((JSpinner)bookingInputs[4]).setValue(booking.getDiscount());
        final JDialog editDialog = GUI.dialog("Edit Booking", bookingLabels, 
                 bookingInputs);
         JPanel myPanel = (JPanel)editDialog.getContentPane().getComponent(0);
         
         final JComboBox guestNamesCB = ((JComboBox)myPanel.getComponent(1));
         final JSpinner nrOfGuests = ((JSpinner)myPanel.getComponent(3));
         final DateField arrivalDateField = ((DateField)myPanel.getComponent(5));
         final DateField leavingDateField = ((DateField)myPanel.getComponent(7));
         final JSpinner discount = (JSpinner)myPanel.getComponent(9);
         
         JButton okButton = ((JButton)myPanel.getComponent(10));
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String guestName = (String)guestNamesCB.getSelectedItem();
                 Date arrivalDate = (Date)arrivalDateField.getValue();
                 arrivalDate = DateUtils.setTimeToMidnight(arrivalDate);
                 Date leavingDate = (Date)leavingDateField.getValue();
                 leavingDate = DateUtils.setTimeToMidnight(leavingDate);
                 int discountI = (Integer)discount.getValue();
                 int nrOfGuestsI = (Integer)nrOfGuests.getValue();
                 try
                 {
                     Guest guest = guestCtrl.getGuestByName(guestName);
                     bookingCtrl.editBooking(id, guest, nrOfGuestsI, arrivalDate, 
                             leavingDate, discountI);
                 }
                 catch (Exception exc)
                 {
                     GUI.showError(exc.getMessage(), "Error", bookingTab);
                     return;
                 }
                 editDialog.setVisible(false);
                 updateTable();
                 GUI.updateRooms();
             }
         });
         editDialog.setVisible(true);
     }
     
     private void removeBookingCB(int id)
     {
         int choice = JOptionPane.showConfirmDialog(
                 this,
                 "Are you sure you want to remove the booking?", "Removal",
                 JOptionPane.YES_NO_OPTION);
 
         if (choice == 0)
         {
             try
             {
                 Booking booking = bookingCtrl.getBooking(id);
                 bookingCtrl.removeBooking(booking.getId());
             }
             catch (Exception e)
             {
                 GUI.showError(e.getMessage(), "Error", this);
                 return;
             }
             updateTable();
             GUI.updateRooms();
         }
     }
     
     public void update()
     {
         if (GUI.getHotel() != null)
         {
             String hotelName = GUI.getHotel().getName();
             if (bookingCtrl == null || guestCtrl == null)
             {
                 bookingCtrl = new BookingCtrl(hotelName);
                 guestCtrl = new GuestCtrl(hotelName);
             }
             else
             {
                 bookingCtrl.setHotelName(hotelName);
                 guestCtrl.setHotelName(hotelName);
             }
         }
         else
         {
             bookingCtrl = null;
             guestCtrl = null;
         }
         updateTable();
         
         if (bookingTable.getSelectedRowCount() < 1 && 
                 bookingTable.getRowCount() > 0)
             bookingTable.setRowSelectionInterval(0, 0);
     }
     
     public final void updateTable()
     {
         DefaultTableModel model = (DefaultTableModel) bookingTable.getModel();
         while(model.getRowCount() > 0)
             model.removeRow(0);
         
         if (GUI.getHotel() != null && bookingCtrl == null)
             bookingCtrl = new BookingCtrl(GUI.getHotel().getName());
         else if (GUI.getHotel() == null)
             return;
         Locale locale = Locale.getDefault();
         for(int i = 0; i < bookingCtrl.getBookingCount(); i++)
         {
             Booking booking = bookingCtrl.getBooking(i);
             
             String arrivalDate = DateFormat.getDateInstance(DateFormat.SHORT, 
                     locale).format(booking.getArrivalDate());
             String leavingDate = DateFormat.getDateInstance(DateFormat.SHORT, 
                     locale).format(booking.getLeavingDate());
             Object[] bookingData = {i+1, booking.getId(), booking.getGuest().
                 getName(), booking.getRoom().getRoomNr(), 
                 arrivalDate, leavingDate, booking.getDiscount()};
             model.addRow(bookingData);
         }
     }
 }
