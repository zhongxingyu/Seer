 package hotel.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import net.sf.nachocalendar.CalendarFactory;
 import net.sf.nachocalendar.components.DateField;
 
 import hotel.controller.RoomCtrl;
 import hotel.controller.BookingCtrl;
 import hotel.controller.GuestCtrl;
 import hotel.core.Guest;
 import hotel.core.Room;
 import hotel.core.Booking;
 import hotel.utils.DateUtils;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 public class RoomTab extends JPanel
 {
     private JTable roomTable;
     private JTable bookingTable;
     private RoomCtrl roomCtrl;
     private BookingCtrl bookingCtrl;
     private JComponent[] roomInputs;
     private JLabel[] roomLabels;
     private JComponent[] bookingInputs;
     private JLabel[] bookingLabels;
     private GuestCtrl guestCtrl;
     private final RoomTab roomTab = this;
     //private GUI gui;
     
     public RoomTab()
     {
         this.setLayout(new GridLayout(1, 0));
         if (GUI.getHotel() != null)
         {
             String hotelName = GUI.getHotel().getName();
             roomCtrl = new RoomCtrl(hotelName);
             bookingCtrl = new BookingCtrl(hotelName);
             guestCtrl = new GuestCtrl(GUI.getHotel().getName());
         }
         else
         {
             roomCtrl = null;
             bookingCtrl = null;
             guestCtrl = null;
         }
         
         roomLabels = new JLabel[]{new JLabel("Room Nr"), new JLabel("Size"), 
             new JLabel("Sq Meter cost"), new JLabel("Nr of bedrooms")};
         roomInputs = new JSpinner[4];
         for (int i = 0; i < roomInputs.length; i++)
             roomInputs[i] = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
         bookingLabels = new JLabel[]{new JLabel("Guest"), 
             new JLabel("Arrival Date"), new JLabel("Leaving Date"), 
             new JLabel("Discount")};
         bookingInputs = new JComponent[4];
         
         String[] roomColumns = new String[] {"#", "Nr", "Size", "Price", 
             "bedrooms"};
         roomTable = GUI.createTable(roomColumns);
         roomTable.getColumnModel().getColumn(0).setMaxWidth(30);
         roomTable.getColumnModel().getColumn(1).setMaxWidth(30);
         updateRoomTable();
         final TableRowSorter<TableModel> roomSorter = new TableRowSorter<TableModel>();
         roomSorter.setModel(roomTable.getModel());
         roomTable.setRowSorter(roomSorter);
         
         String[] bookingColumns = new String[] {"#", "id", "Guest Name", 
             "Arrival Date", "Leaving Date", "Discount"};
         bookingTable = GUI.createTable(bookingColumns);
         bookingTable.getColumnModel().getColumn(0).setMaxWidth(30);
         TableColumn idColumn = bookingTable.getColumnModel().getColumn(1);
         idColumn.setMaxWidth(0);
         idColumn.setMinWidth(0);
         idColumn.setPreferredWidth(0);
         final TableRowSorter<TableModel> bookingSorter = new TableRowSorter<TableModel>();
         bookingSorter.setModel(bookingTable.getModel());
         bookingTable.setRowSorter(bookingSorter);
         
         roomTable.getSelectionModel().addListSelectionListener(
         new ListSelectionListener() 
         {
             @Override
             public void valueChanged(ListSelectionEvent e)
             {
                 if (e.getValueIsAdjusting())
                     return;
                 if (bookingCtrl == null)
                     return;
                 if (roomTable.getSelectedRowCount() < 1 && 
                         roomTable.getRowCount() > 0)
                     roomTable.setRowSelectionInterval(0, 0);
                 
                 int selected;
                 if (roomTable.getSelectedRowCount() > 0)
                     selected = roomTable.getSelectedRow();
                 else
                 {
                     updateBookingTable(-1);
                     return;
                 }
                 updateBookingTable((Integer)roomTable.
                         getValueAt(selected, 0) - 1);
             }
         });
         
         JScrollPane roomScrollPane = new JScrollPane(roomTable);
         JScrollPane bookingScrollPane = new JScrollPane(bookingTable);
         
         JButton AddRoom = new JButton("Add");
         AddRoom.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", roomTab);
                 else
                     addRoomCB();
             }
         });
         JButton editRoom = new JButton("Edit");
         editRoom.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", roomTab);
                 else if (roomTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some room.", "Error", roomTab);
                 else 
                     editRoomCB(roomTable.getSelectedRow());
             }
         });
         
         JButton removeRoom = new JButton("Remove");
         removeRoom.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", roomTab);
                 else if (roomTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some room.", "Error", roomTab);
                 else 
                     removeRoomCB(roomTable.getSelectedRow());
             }
         });
         
         JToolBar roomToolBar = new JToolBar();
         roomToolBar.setFloatable(false);
         roomToolBar.add(AddRoom);
         roomToolBar.add(editRoom);
         roomToolBar.add(removeRoom);
         
         String[] fieldNames = new String[] {"Room Nr", "Size", "Price", 
             "Bedrooms"};
         final JComboBox filterBox = new JComboBox(fieldNames);
         final JTextField filterField = new JTextField();
         filterField.addKeyListener(new KeyListener() {
 
             @Override public void keyTyped(KeyEvent e) {}
             @Override public void keyPressed(KeyEvent e) {}
             
             @Override 
             public void keyReleased(KeyEvent e) 
             {
                 String text = filterField.getText();
                 int i = filterBox.getSelectedIndex() + 1;
                 if (text.isEmpty())
                     roomSorter.setRowFilter(null);
                 else
                     roomSorter.setRowFilter(RowFilter.regexFilter(text, i));
             }
         });
 
         roomToolBar.addSeparator();
         roomToolBar.add(new JLabel("Filter by   "));
         roomToolBar.add(filterBox);
         roomToolBar.addSeparator();
         roomToolBar.add(filterField);
         roomToolBar.addSeparator();
         
         JButton addBooking = new JButton("Add");
         addBooking.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", roomTab);
                 else if (roomTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some room.", "Error", roomTab);
                 else 
                     addBookingCB(roomTable.getSelectedRow());
             }
         });
         
         JButton editBooking = new JButton("Edit");
         editBooking.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", roomTab);
                 else if (roomTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some room.", "Error", roomTab);
                 else if (bookingTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some booking", "Error", 
                             roomTab);
                 else 
                     editBookingCB((Integer) bookingTable.
                         getValueAt(bookingTable.getSelectedRow(), 1), 
                         roomTable.getSelectedRow());
             }
         });
         JButton removeBooking = new JButton("Remove");
         removeBooking.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (GUI.getHotel() == null)
                     GUI.showError("Hotel must be selected.", "Error", roomTab);
                 else if (roomTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some room.", "Error", roomTab);
                 else if (bookingTable.getSelectedRowCount() < 1)
                     GUI.showError("Please select some booking", "Error", 
                             roomTab);
                 else 
                     removeBookingCB((Integer) bookingTable.
                             getValueAt(bookingTable.getSelectedRow(), 1),
                             roomTable.getSelectedRow());
             }
         });
         
         JToolBar bookingToolBar = new JToolBar();
         bookingToolBar.setFloatable(false);
         bookingToolBar.add(new JLabel("Bookings   "));
         bookingToolBar.add(addBooking);
         bookingToolBar.add(editBooking);
         bookingToolBar.add(removeBooking);
         
         fieldNames = new String[] {"Guest Name", "Arrival Date", "Leaving Date", 
             "Discount"};
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
         
         JPanel roomPanel = new JPanel();
         roomPanel.setLayout(new BorderLayout());
         roomPanel.add(roomToolBar, BorderLayout.PAGE_START);
         roomPanel.add(roomScrollPane);
         
         JPanel bookingPanel = new JPanel();
         bookingPanel.setLayout(new BorderLayout());
         bookingPanel.add(bookingToolBar, BorderLayout.PAGE_START);
         bookingPanel.add(bookingScrollPane);
         
         JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                 roomPanel, bookingPanel);
         splitPane.setDividerLocation(200);
         
         this.add(splitPane);
         this.setVisible(true);
     }
     
     private void addBookingCB(final int roomId)
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
         bookingInputs[1] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[1]).setValue(DateUtils.getToday());
         bookingInputs[2] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[2]).setValue(DateUtils.getToday());
         bookingInputs[3] = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
         final JDialog addDialog = GUI.dialog("Add Booking", bookingLabels, 
                 bookingInputs);
         JPanel myPanel = (JPanel)addDialog.getContentPane().getComponent(0);
         
         final JComboBox guestNamesCB = ((JComboBox)myPanel.getComponent(1));
         final DateField arrivalDateField = ((DateField)myPanel.getComponent(3));
         final DateField leavingDateField = ((DateField)myPanel.getComponent(5));
         final JSpinner discount = (JSpinner)myPanel.getComponent(7);
         
         JButton okButton = ((JButton)myPanel.getComponent(8));
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String guestName = (String)guestNamesCB.getSelectedItem();
                 Date arrivalDate = (Date)arrivalDateField.getValue();
                 Date leavingDate = (Date)leavingDateField.getValue();
                 int discountI = (Integer)discount.getValue();
                 try
                 {
                     Room room = roomCtrl.getRoomById(roomId);
                     Guest guest = guestCtrl.getGuestByName(guestName);
                     bookingCtrl.addBooking(room, guest, arrivalDate, 
                             leavingDate, discountI);
                 }
                 catch (Exception exc)
                 {
                     GUI.showError(exc.getMessage(), "Error", roomTab);
                     return;
                 }
                 addDialog.setVisible(false);
                 updateRoomTable();
                 updateBookingTable(roomId);
                 roomTable.setRowSelectionInterval(roomId, roomId);
                 GUI.updateBookings();
             }
         });
         addDialog.setVisible(true);
     }
     
     private void editBookingCB(final int bookingId, final int roomId)
     {
         if (guestCtrl == null)
             guestCtrl = new GuestCtrl(GUI.getHotel().getName());
         if (bookingCtrl == null)
             bookingCtrl = new BookingCtrl(GUI.getHotel().getName());
         
         Booking booking = bookingCtrl.getBookingByBookingId(bookingId);
         
         String[] guestNames = new String[guestCtrl.getGuestCount()];
         for (int i = 0; i < guestNames.length; i++)
             guestNames[i] = guestCtrl.getGuestById(i).getName();
         bookingInputs[0] = new JComboBox(guestNames);
         ((JComboBox)bookingInputs[0]).setSelectedItem(booking.getGuest().
                 getName());
         bookingInputs[1] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[1]).setValue(booking.getArrivalDate());
         bookingInputs[2] = CalendarFactory.createDateField();
         ((DateField)bookingInputs[2]).setValue(booking.getLeavingDate());
         SpinnerModel discountModel = new SpinnerNumberModel(booking.
                 getDiscount(), 0, 999, 1);
         bookingInputs[3] = new JSpinner(discountModel);
        final JDialog editDialog = GUI.dialog("Edit Booking", bookingLabels, 
                 bookingInputs);
         JPanel myPanel = (JPanel)editDialog.getContentPane().getComponent(0);
         
         final JComboBox guestNamesCB = ((JComboBox)myPanel.getComponent(1));
         final DateField arrivalDateField = ((DateField)myPanel.getComponent(3));
         final DateField leavingDateField = ((DateField)myPanel.getComponent(5));
         final JSpinner discount = (JSpinner)myPanel.getComponent(7);
         
         JButton okButton = ((JButton)myPanel.getComponent(8));
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String guestName = (String)guestNamesCB.getSelectedItem();
                 Date arrivalDate = (Date)arrivalDateField.getValue();
                 Date leavingDate = (Date)leavingDateField.getValue();
                 int discountI = (Integer)discount.getValue();
                 try
                 {
                     Room room = roomCtrl.getRoomById(roomId);
                     Guest guest = guestCtrl.getGuestByName(guestName);
                     bookingCtrl.editBooking(bookingId, room, guest, arrivalDate, 
                             leavingDate, discountI);
                 }
                 catch (Exception exc)
                 {
                     GUI.showError(exc.getMessage(), "Error", roomTab);
                     return;
                 }
                 editDialog.setVisible(false);
                 updateRoomTable();
                 updateBookingTable(roomId);
                 GUI.updateBookings();
                 roomTable.setRowSelectionInterval(roomId, roomId);
             }
         });
         editDialog.setVisible(true);
     }
     
     private void removeBookingCB(int bookingId, int selectedRoom)
     {
         int choice = JOptionPane.showConfirmDialog(
                 this,
                 "Are you sure you want to remove the booking?", "Removal",
                 JOptionPane.YES_NO_OPTION);
 
         if (choice == 0)
         {
             try
             {
                 bookingCtrl.removeBooking(bookingId);
             }
             catch (Exception e)
             {
                 GUI.showError(e.getMessage(), "Error", this);
                 return;
             }
             updateRoomTable();
             updateBookingTable(selectedRoom);
             GUI.updateBookings();
             roomTable.setRowSelectionInterval(selectedRoom, selectedRoom);
         }
     }
     
     private void addRoomCB()
     {
         if (roomCtrl == null)
             roomCtrl = new RoomCtrl(GUI.getHotel().getName());
         final JDialog addDialog = GUI.dialog("Add", roomLabels, roomInputs);
         JPanel myPanel = (JPanel)addDialog.getContentPane().getComponent(0);
         
         final JSpinner roomNr = ((JSpinner)myPanel.getComponent(1));
         final JSpinner size = ((JSpinner)myPanel.getComponent(3));
         final JSpinner price = ((JSpinner)myPanel.getComponent(5));
         final JSpinner bedrooms = (JSpinner)myPanel.getComponent(7);
         
         JButton okButton = ((JButton)myPanel.getComponent(8));
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 try
                 {
                     int roomNrI = (Integer)roomNr.getValue();
                     int sizeI = (Integer) size.getValue();
                     int priceI = (Integer) price.getValue();
                     int bedrooomsI = (Integer) bedrooms.getValue();
                     roomCtrl.addRoom(roomNrI, priceI, sizeI, bedrooomsI);
                 }
                 catch (Exception exc)
                 {
                     GUI.showError(exc.getMessage(), "Error", roomTab);
                     return;
                 }
                 
                 addDialog.setVisible(false);
                 updateRoomTable();
                 GUI.updateBookings();
             }
         });
         
         addDialog.setVisible(true);
     }
     
     private void removeRoomCB(int id)
     {
         int choice = JOptionPane.showConfirmDialog(
                 this,
                 "Are you sure you want to remove the room?", "Removal",
                 JOptionPane.YES_NO_OPTION);
 
         if (choice == 0)
         {
             try
             {
                 roomCtrl.removeRoom(id);
             }
             catch (Exception e)
             {
                 GUI.showError(e.getMessage(), "Error", this);
                 return;
             }
             updateRoomTable();
             updateBookingTable(id);
             GUI.updateBookings();
         }
     }
     
     private void editRoomCB(final int id)
     {
         if (roomCtrl == null)
             roomCtrl = new RoomCtrl(GUI.getHotel().getName());
         final JDialog editDialog = GUI.dialog("Edit", roomLabels, roomInputs);
         JPanel myPanel = (JPanel)editDialog.getContentPane().getComponent(0);
         
         final Room room = roomCtrl.getRoomById(id);
         final JSpinner roomNr = ((JSpinner)myPanel.getComponent(1));
         roomNr.setValue(room.getRoomNr());
         final JSpinner size = ((JSpinner)myPanel.getComponent(3));
         size.setValue(room.getSqMeters());
         final JSpinner price = ((JSpinner)myPanel.getComponent(5));
         price.setValue(room.getMeterCost());
         final JSpinner bedrooms = (JSpinner)myPanel.getComponent(7);
         bedrooms.setValue(room.getNrOfBedrooms());
 
         JButton okButton = ((JButton)myPanel.getComponent(8));
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 try
                 {
                     int roomNrI = (Integer)roomNr.getValue();
                     int sizeI = (Integer) size.getValue();
                     int priceI = (Integer) price.getValue();
                     int bedrooomsI = (Integer) bedrooms.getValue();
                     roomCtrl.editRoom(id, roomNrI, priceI, sizeI, bedrooomsI);
                 }
                 catch (Exception exc)
                 {
                     GUI.showError(exc.getMessage(), "Error", roomTab);
                     return;
                 }
                 
                 editDialog.setVisible(false);
                 updateRoomTable();
                 roomTable.setRowSelectionInterval(id, id);
                 GUI.updateBookings();
             }
         });
         
         editDialog.setVisible(true);
     }
     
     public void update()
     {
         if (GUI.getHotel() != null)
         {
             bookingCtrl = new BookingCtrl(GUI.getHotel().getName());
             guestCtrl = new GuestCtrl(GUI.getHotel().getName());
             roomCtrl = new RoomCtrl(GUI.getHotel().getName());
         }
         else
         {
             bookingCtrl = null;
             guestCtrl = null;
             roomCtrl = null;            
         }
         updateRoomTable();
         if (roomTable.getSelectedRowCount() < 1 && 
                 roomTable.getRowCount() > 0)
             roomTable.setRowSelectionInterval(0, 0);
     }
     
     private void updateRoomTable()
     {   
         DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
         while(model.getRowCount() > 0)
             model.removeRow(0);
         
         if (roomCtrl == null || roomCtrl.getRoomCount() == 0)
             return;
         
         for(int i = 0; i < roomCtrl.getRoomCount(); i++)
         {
             Room room = roomCtrl.getRoomById(i);
             Object[] roomData = {i+1, room.getRoomNr(), room.getSqMeters(),
                 room.getCost(), room.getNrOfBedrooms()};
             model.addRow(roomData);
         }
     }
     
     private void updateBookingTable(int selectedRoom)
     {        
         DefaultTableModel model = (DefaultTableModel) bookingTable.getModel();
         while(model.getRowCount() > 0)
             model.removeRow(0);
         if (selectedRoom == -1)
             return;
         
         Room room = roomCtrl.getRoomById(selectedRoom);
         if (room == null)
             return;
         Booking[] bookings = bookingCtrl.getBookingsByRoom(room);
         Locale locale = Locale.getDefault();
         
         for (int i = 0; i < bookings.length; i++)
         {
             Booking booking = bookings[i];
             String arrivalDate = DateFormat.getDateInstance(DateFormat.SHORT, 
                     locale).format(booking.getArrivalDate());
             String leavingDate = DateFormat.getDateInstance(DateFormat.SHORT, 
                     locale).format(booking.getLeavingDate());
             Object[] bookingData = {i+1, booking.getId(), 
                 booking.getGuest().getName(), arrivalDate, leavingDate, 
                 booking.getDiscount()};
             model.addRow(bookingData);
         }
     }
 }
