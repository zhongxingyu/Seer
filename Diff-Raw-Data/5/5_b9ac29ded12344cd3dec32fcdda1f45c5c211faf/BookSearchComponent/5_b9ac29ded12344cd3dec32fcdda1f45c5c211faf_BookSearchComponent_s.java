 package edu.exigen.client.gui;
 
 import edu.exigen.client.entities.Book;
 import edu.exigen.server.provider.BookProvider;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 import java.util.List;
 
 /**
  * @author Tedikova O.
  * @version 1.0
  */
 public class BookSearchComponent {
     private static final String PANEL_NAME = "Book Search";
     private static final String SEARCH_LABEL = "Search: ";
     private static final String SEARCH_BUTTON_TEXT = "Search";
 
     private JPanel bookSearchPanel;
     private JTextField searchField;
 
     private BookProvider bookProvider;
     private BookTableModel bookTableModel;
     private JTable bookTable;
 
     public BookSearchComponent(BookProvider bookProvider) throws RemoteException {
         this.bookProvider = bookProvider;
         initComponents();
     }
 
     private void initComponents() throws RemoteException {
         JPanel dataEnterPanel = createDataEnterPanel();
         bookTableModel = new BookTableModel(bookProvider.readAll());
         bookTable = new JTable(bookTableModel);
         bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scrollPane = new JScrollPane(bookTable);
         bookTable.setPreferredScrollableViewportSize(new Dimension(600, 300));
         bookSearchPanel = new JPanel();
         bookSearchPanel.setBorder(BorderFactory.createTitledBorder(PANEL_NAME));
         bookSearchPanel.setLayout(new BorderLayout());
         bookSearchPanel.add(dataEnterPanel, BorderLayout.NORTH);
         bookSearchPanel.add(scrollPane, BorderLayout.CENTER);
     }
 
     public JPanel getBookSearchPanel() {
         return bookSearchPanel;
     }
 
     public JPanel createDataEnterPanel() {
         JLabel searchLabel = new JLabel(SEARCH_LABEL);
         searchField = new JTextField();
         JButton searchButton = new JButton(SEARCH_BUTTON_TEXT);
         searchButton.addActionListener(new SearchButtonListener());
         JPanel dataEnterPanel = new JPanel();
         dataEnterPanel.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.EAST;
         c.weighty = 1;
         c.weightx = 0.5;
         c.gridx = 0;
         c.gridy = 0;
         dataEnterPanel.add(searchLabel, c);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 1;
         c.gridx = 1;
         dataEnterPanel.add(searchField, c);
         c.fill = GridBagConstraints.CENTER;
         c.gridx = 2;
         c.weightx = 0.5;
         dataEnterPanel.add(searchButton, c);
         return dataEnterPanel;
     }
 
 
     private class SearchButtonListener implements ActionListener {
         /**
          * Invoked when an action occurs.
          */
         public void actionPerformed(ActionEvent e) {
             List<Book> books;
             try {
                 if (!"".equals(searchField.getText())) {
                     books = bookProvider.searchBooks(searchField.getText());
                 } else {
                     books = bookProvider.readAll();
                 }
             } catch (RemoteException e1) {
                 throw new RuntimeException(e1.getMessage(), e1);
             }
             bookTableModel.setTableData(books);
             bookTableModel.fireTableRowsInserted(0, books.size() - 1);
         }
     }
 
     public void addBookSelectionListener(final BookSelectionListener selectionListener) {
         final ListSelectionModel model = bookTable.getSelectionModel();
         model.addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                 Book selectedBook = null;
                 if (!lsm.isSelectionEmpty()) {
                     int selectedRow = lsm.getMinSelectionIndex();
                     selectedBook = bookTableModel.getTableData().get(selectedRow);
                 }
                 selectionListener.bookSelected(selectedBook);
             }
 
         }
 
         );
     }
 }
