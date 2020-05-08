 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.gubsky.study.elib.views;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import ru.gubsky.study.elib.models.Book;
 import ru.gubsky.study.elib.models.BookTableModel;
 
 /**
 
  @author GG
  */
 public class SearchBooksView extends JFrame
 {
     // use getters, don't use directly
     private ActionListener searchButtonListener_;
     private JTable searchBukzTable_;
     private JButton searchButton_;
     private JTextField searchTf_;
     //private JPanel searchPanel_;
     public SearchBooksView()
     {
         super();
         createGui();
     }
 
     public JTextField getSearchTf()
     {
         if (searchTf_ == null) {
             searchTf_ = new JTextField(30);
         }
         return searchTf_;
     }
 
     public void setSearchButtonListener(ActionListener searchButtonListener)
     {
         getSearchButton().removeActionListener(this.searchButtonListener_);
         this.searchButtonListener_ = searchButtonListener;
         getSearchButton().addActionListener(searchButtonListener);
     }
 
     public JTable getSearchBukzTable()
     {
         if (searchBukzTable_ == null) {
             searchBukzTable_ = new JTable();
             JScrollPane scrollPane = new JScrollPane(searchBukzTable_);
             searchBukzTable_.setFillsViewportHeight(true);
 //            getCenterPanel().add(scrollPane);
             add(scrollPane, BorderLayout.CENTER);
         }
         return searchBukzTable_;
     }
 
     private void createGui()
     {
         setSize(888, 500);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setTitle("eLib. Поиск книг");
 //        setLayout(new BorderLayout(10, 10));
 
         // tab
 //        add(getSearchPanel());
         JPanel topPanel = new JPanel();
         topPanel.add(getSearchTf());
         topPanel.add(getSearchButton());
         add(topPanel, BorderLayout.PAGE_START);
         getSearchBukzTable();
     }
 
     private JButton getSearchButton()
     {
         if (searchButton_ == null) {
             searchButton_ = new JButton("Найти");
         }
         return searchButton_;
     }
 
     /*
      private JPanel getSearchPanel()
      {
      if (searchPanel_ == null) {
      searchPanel_ = new JPanel();
      searchPanel_.setLayout(new BoxLayout(searchPanel_, BoxLayout.Y_AXIS));
      JPanel topPanel = new JPanel();
      topPanel.add(getSearchTf());
      topPanel.add(getSearchButton());
      searchPanel_.add(topPanel);
      }
      return searchPanel_;
      }
      */
     JPanel centerSearchPanel_;
     private JPanel getCenterPanel()
     {
         if (centerSearchPanel_ == null) {
             centerSearchPanel_ = new JPanel();
             centerSearchPanel_.setLayout(new BoxLayout(centerSearchPanel_, BoxLayout.X_AXIS));
             //getSearchPanel().add(centerSearchPanel_);
             add(centerSearchPanel_, BorderLayout.CENTER);
         }
         return centerSearchPanel_;
     }
 
     private JTextArea thumbArea_;
     public JTextArea getThumbArea()
     {
         if (thumbArea_ == null) {
            thumbArea_ = new JTextArea(10, 15);
             thumbArea_.setLineWrap(true);
             thumbArea_.setMaximumSize(new Dimension(350, 200));
             thumbArea_.setWrapStyleWord(true);
             thumbArea_.setEditable(false);
             thumbArea_.setAlignmentY(JComponent.TOP_ALIGNMENT);
             thumbArea_.setAlignmentX(JComponent.CENTER_ALIGNMENT);
             getThumbnailPanel().add(thumbArea_);
         }
         return thumbArea_;
     }
 
     private JPanel thumbnailPanel_;
     private JPanel getThumbnailPanel()
     {
         if (thumbnailPanel_ == null) {
             thumbnailPanel_ = new JPanel();
             thumbnailPanel_.setLayout(new BoxLayout(thumbnailPanel_, BoxLayout.Y_AXIS));
             thumbnailPanel_.setAlignmentY(JComponent.TOP_ALIGNMENT);
             JLabel lbl = new JLabel("Миниатюра");
             lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
             thumbnailPanel_.add(lbl);
 //            getCenterPanel().add(thumbnailPanel_);
             add(thumbnailPanel_, BorderLayout.EAST);
         }
         return thumbnailPanel_;
     }
 
     public void updateSearch(BookTableModel bookTableModel)
     {
         System.out.println("updateSearch");
         JTable table = getSearchBukzTable();
         System.out.println(bookTableModel.getRowCount());
         table.setModel(bookTableModel);
         table.getSelectionModel().clearSelection();
         table.removeColumn(table.getColumnModel().getColumn(5));
     }
 
     private JButton openBookButton_;
     public JButton getOpenBookButton(ActionListener l)
     {
         if (openBookButton_ == null) {
             openBookButton_ = new JButton("Открыть книгу");
             openBookButton_.addActionListener(l);
             openBookButton_.setAlignmentX(JComponent.CENTER_ALIGNMENT);
             openBookButton_.setAlignmentY(JComponent.TOP_ALIGNMENT);
             getThumbnailPanel().add(openBookButton_);
         }
         return openBookButton_;
     }
 
     public Book getSelectedBook()
     {
         BookTableModel bookTableModel = (BookTableModel) getSearchBukzTable().getModel();
         Book book = bookTableModel.getBookAt(getSearchBukzTable().getSelectedRow());
         return book;
     }
 
 }
