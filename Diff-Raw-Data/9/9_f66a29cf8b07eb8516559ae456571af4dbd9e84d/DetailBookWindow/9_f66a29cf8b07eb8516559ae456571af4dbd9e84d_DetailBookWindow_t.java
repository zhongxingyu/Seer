 package view;
 
 import controller.WindowController;
 import domain.Book;
 import domain.Library;
 import domain.Shelf;
 import tablemodel.DetailBookWindowTableModel;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Observable;
 
 public class DetailBookWindow extends ListenerJFrame {
 //public class DetailBookWindow extends JFrame {
 
 	
     private JTextField textField;
     private JTextField textField_1;
     private JTextField textField_2;
     private JComboBox<String> comboBox;
     private JButton btnAusgewhlteEntfernen;
     private JButton btnBuchHinzufgen;
     private Library library;
     private Book book;
 
     private JTable table;
     private JPanel panel;
     private JLabel errorLabelTitle;
     private JLabel errorLabelAuthor;
     private JLabel errorLabelPublisher;
     private JLabel lblError;
 
     public DetailBookWindow(Library library, WindowController windowCtrl) {
         super(library, windowCtrl);
         this.library = library;
 		this.setMinimumSize(new Dimension(600,400));
         initialize();
     }
 
 
     public void tryAddingBook() {
     	System.out.println(textField.getText());
     	if(textField.getText().equals("") || textField_1.getText().equals("") || textField_2.getText().equals("")){
     		lblError.setText("Es müssen alle Felder ausgefüllt werden!");
     	}
     	else{
       	  Book newBook = library.createAndAddBook(textField.getText());
           newBook.setAuthor(textField_1.getText());
           newBook.setPublisher(textField_2.getText());
           newBook.setShelf(Shelf.valueOf(comboBox.getSelectedItem().toString()));
           setBook(newBook);
           lblError.setText("");
     	}
     }
 
     public void setBook(Book book1) {
         this.book = book1;
 
         for (Shelf tmpShelf : Shelf.values()) {
             comboBox.addItem(tmpShelf.toString());
         }
 
         if (this.book == null) {
             this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
             JButton btnBuchHinzufgen = new JButton("Buch hinzufügen");
             btnBuchHinzufgen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	tryAddingBook();
                 }
             });
            this.getRootPane().setDefaultButton(btnBuchHinzufgen);
             GridBagConstraints gbc_btnBuchHinzufgen = new GridBagConstraints();
             gbc_btnBuchHinzufgen.insets = new Insets(0, 0, 0, 5);
             gbc_btnBuchHinzufgen.gridx = 0;
             gbc_btnBuchHinzufgen.gridy = 4;
             panel.add(btnBuchHinzufgen, gbc_btnBuchHinzufgen);
 
 
         } else {
             this.addWindowListener(new WindowAdapter() {
                 @Override
                 public void windowClosing(WindowEvent e) {
                     book.setAuthor(textField_1.getText());
                     book.setName(textField.getText());
                     book.setPublisher(textField_2.getText());
                     book.setShelf(Shelf.valueOf(comboBox.getSelectedItem().toString()));
 
                 }
             });
 
             textField.setText(this.book.getName());
             textField_1.setText(this.book.getAuthor());
             textField_2.setText(this.book.getPublisher());
             for (Shelf tmpShelf : Shelf.values()) {
                 comboBox.addItem(tmpShelf.toString());
             }
 
             JPanel panel_1 = new JPanel();
             panel_1.setBorder(new TitledBorder(null, "Exemplare", TitledBorder.LEADING, TitledBorder.TOP, null, null));
             this.getContentPane().add(panel_1, BorderLayout.CENTER);
             panel_1.setLayout(new BorderLayout(0, 0));
 
             JPanel panel_2 = new JPanel();
             panel_1.add(panel_2, BorderLayout.NORTH);
             GridBagLayout gbl_panel_2 = new GridBagLayout();
             gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
             gbl_panel_2.rowHeights = new int[]{0, 0};
             gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
             gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
             panel_2.setLayout(gbl_panel_2);
 
             JLabel lblAnzahlX = new JLabel("Anzahl: x");
             GridBagConstraints gbc_lblAnzahlX = new GridBagConstraints();
             gbc_lblAnzahlX.anchor = GridBagConstraints.EAST;
             gbc_lblAnzahlX.gridwidth = 4;
             gbc_lblAnzahlX.insets = new Insets(0, 0, 0, 5);
             gbc_lblAnzahlX.gridx = 1;
             gbc_lblAnzahlX.gridy = 0;
             panel_2.add(lblAnzahlX, gbc_lblAnzahlX);
 
             btnAusgewhlteEntfernen = new JButton("Ausgewählte Entfernen");
             btnAusgewhlteEntfernen.setEnabled(false);
             btnAusgewhlteEntfernen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     int[] rows = table.getSelectedRows();
                     for (int row : rows) {
                         library.removeCopy(library.getCopiesOfBook(book).get(table.convertRowIndexToModel(row)));
                     }
                 }
             });
 
             GridBagConstraints gbc_btnAusgewhlteEntfernen = new GridBagConstraints();
             gbc_btnAusgewhlteEntfernen.anchor = GridBagConstraints.EAST;
             gbc_btnAusgewhlteEntfernen.insets = new Insets(0, 0, 0, 5);
             gbc_btnAusgewhlteEntfernen.gridx = 10;
             gbc_btnAusgewhlteEntfernen.gridy = 0;
             panel_2.add(btnAusgewhlteEntfernen, gbc_btnAusgewhlteEntfernen);
 
             JButton btnExemplareHinzufgen = new JButton("Exemplare Hinzufügen");
            this.getRootPane().setDefaultButton(btnExemplareHinzufgen);
             btnExemplareHinzufgen.setEnabled(true);
             btnExemplareHinzufgen.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	library.createAndAddCopy(book);
                 	library.notifyObservers();
                 }
             });            
             GridBagConstraints gbc_btnExemplareHinzufgen = new GridBagConstraints();
             gbc_btnExemplareHinzufgen.anchor = GridBagConstraints.EAST;
             gbc_btnExemplareHinzufgen.insets = new Insets(0, 0, 0, 5);
             gbc_btnExemplareHinzufgen.gridx = 11;
             gbc_btnExemplareHinzufgen.gridy = 0;
             panel_2.add(btnExemplareHinzufgen, gbc_btnExemplareHinzufgen);
 
             table = new JTable();
             DetailBookWindowTableModel detailWindowTableModel = new DetailBookWindowTableModel(library, book);
             table.setModel(detailWindowTableModel);
             JScrollPane scrollPane = new JScrollPane();
             scrollPane.setViewportView(table);
             JPanel panel_3 = new JPanel();
             panel_3.setLayout(new BorderLayout(0, 0));
             panel_3.add(scrollPane);
             panel_1.add(panel_3, BorderLayout.CENTER);
 
 
             comboBox.setSelectedItem(book.getShelf().toString());
             table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                 public void valueChanged(ListSelectionEvent e) {
                     if (table.getSelectedRows().length >= 1) {
                         btnAusgewhlteEntfernen.setEnabled(true);
                     } else {
                         btnAusgewhlteEntfernen.setEnabled(false);
                     }
                 }
             });
 
         }
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize() {
         this.setTitle("Buch Detailansicht");
         this.setBounds(100, 100, 592, 473);
         this.getContentPane().setLayout(new BorderLayout(0, 0));
 
         panel = new JPanel();
         panel.setBorder(new TitledBorder(null, "Buch Informationen:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         this.getContentPane().add(panel, BorderLayout.NORTH);
         GridBagLayout gbl_panel = new GridBagLayout();
         gbl_panel.columnWidths = new int[]{81, 282, 0};
         gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
         gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
         gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
         panel.setLayout(gbl_panel);
 
         JLabel lblTitel = new JLabel("Titel:");
         GridBagConstraints gbc_lblTitel = new GridBagConstraints();
         gbc_lblTitel.fill = GridBagConstraints.HORIZONTAL;
         gbc_lblTitel.insets = new Insets(0, 0, 5, 5);
         gbc_lblTitel.gridx = 0;
         gbc_lblTitel.gridy = 0;
         panel.add(lblTitel, gbc_lblTitel);
 
         textField = new JTextField();
         GridBagConstraints gbc_textField = new GridBagConstraints();
         gbc_textField.insets = new Insets(0, 0, 5, 0);
         gbc_textField.fill = GridBagConstraints.HORIZONTAL;
         gbc_textField.gridx = 1;
         gbc_textField.gridy = 0;
         panel.add(textField, gbc_textField);
         textField.setColumns(10);
 
         JLabel lblAutor = new JLabel("Autor:");
         GridBagConstraints gbc_lblAutor = new GridBagConstraints();
         gbc_lblAutor.insets = new Insets(0, 0, 5, 5);
         gbc_lblAutor.anchor = GridBagConstraints.WEST;
         gbc_lblAutor.gridx = 0;
         gbc_lblAutor.gridy = 1;
         panel.add(lblAutor, gbc_lblAutor);
 
         textField_1 = new JTextField();
         GridBagConstraints gbc_textField_1 = new GridBagConstraints();
         gbc_textField_1.insets = new Insets(0, 0, 5, 0);
         gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
         gbc_textField_1.gridx = 1;
         gbc_textField_1.gridy = 1;
         panel.add(textField_1, gbc_textField_1);
         textField_1.setColumns(10);
 
         JLabel lblVerlag = new JLabel("Verlag:");
         GridBagConstraints gbc_lblVerlag = new GridBagConstraints();
         gbc_lblVerlag.anchor = GridBagConstraints.WEST;
         gbc_lblVerlag.insets = new Insets(0, 0, 5, 5);
         gbc_lblVerlag.gridx = 0;
         gbc_lblVerlag.gridy = 2;
         panel.add(lblVerlag, gbc_lblVerlag);
 
         textField_2 = new JTextField();
         GridBagConstraints gbc_textField_2 = new GridBagConstraints();
         gbc_textField_2.insets = new Insets(0, 0, 5, 0);
         gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
         gbc_textField_2.gridx = 1;
         gbc_textField_2.gridy = 2;
         panel.add(textField_2, gbc_textField_2);
         textField_2.setColumns(10);
 
         JLabel lblRegal = new JLabel("Regal:");
         GridBagConstraints gbc_lblRegal = new GridBagConstraints();
         gbc_lblRegal.anchor = GridBagConstraints.WEST;
         gbc_lblRegal.insets = new Insets(0, 0, 5, 5);
         gbc_lblRegal.gridx = 0;
         gbc_lblRegal.gridy = 3;
         panel.add(lblRegal, gbc_lblRegal);
 
         comboBox = new JComboBox<String>();
         GridBagConstraints gbc_comboBox = new GridBagConstraints();
         gbc_comboBox.insets = new Insets(0, 0, 5, 0);
         gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_comboBox.gridx = 1;
         gbc_comboBox.gridy = 3;
         panel.add(comboBox, gbc_comboBox);
         
         lblError = new JLabel("");
         lblError.setForeground(Color.RED);
         //lblError.setText("Testovic");
         GridBagConstraints gbc_lblError = new GridBagConstraints();
         gbc_lblError.anchor = GridBagConstraints.WEST;
         gbc_lblError.gridx = 1;
         gbc_lblError.gridy = 4;
         panel.add(lblError, gbc_lblError);
 
         this.setVisible(true);
     }
 
     @Override
     public void update(Observable o, Object arg) {
         this.setBook(this.book);
 
     }
 
 }
