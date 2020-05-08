 package travianWindow;
 
 import Library.*;
 
 // swing library
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.JTextArea;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.SwingUtilities;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import org.jsoup.*;
 import org.jsoup.nodes.*;
 import org.jsoup.select.*;
 
 public class messageReader {
 
     private travianModel model;
     private travianView view;
     private travianController controller;
     
     public messageController messagecontroller;
     
     public ScreenManager s;
     public JScrollPane messagesListScrollPane;
     public JScrollPane messageReaderScrollPane;
     public Container messagesListPanel;
     public Container messageReaderPanel;
     public Container statusBar;
     
     public JTextArea messageContent;
     
     public JTable messagesList;
     public DefaultTableModel messagesListModel;
     
     public JButton hide;
     public JButton refresh;
     public JButton remove;
 
     public messageReader() {
         model = travian.model;
         view = travian.view;
         controller = travian.controller;
         
         messagecontroller = new messageController();
 
         s = new ScreenManager();
 
         s.newWindow("Messages " + travian.accountName);
         s.setSize(800, 400);
         s.setVisible(false); // hide window
         s.setBackground(Color.BLACK);
         s.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         
         messagesListModel = new DefaultTableModel();
         messagesList = new JTable(messagesListModel);
         
         messagesListScrollPane = new JScrollPane(messagesList);
         messagesListPanel = new Container();
         messageReaderPanel = new Container();
         statusBar = new Container();
         
         hide = new JButton ("Hide window");
         refresh = new JButton ("Download messages");
         remove = new JButton ("Remove message");
         
         hide.setActionCommand("hide");
         refresh.setActionCommand("refresh");
         remove.setActionCommand("remove");
         hide.addActionListener(messagecontroller);
         refresh.addActionListener(messagecontroller);
         remove.addActionListener(messagecontroller);
         
         messagesList.setFillsViewportHeight(true);
         messagesList.addMouseListener(messagecontroller);
         messagesListPanel.setLayout(new BorderLayout());
         messageReaderPanel.setLayout(new BorderLayout());
         statusBar.setLayout(new BorderLayout());
         
         messageContent = new JTextArea (21, 30);
         messageReaderScrollPane = new JScrollPane(messageContent, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         messageContent.setBounds(0, 0, 350, 360);
         //messageContent.setEditable(false);
         messageContent.setLineWrap(true);
         messageContent.setWrapStyleWord(true);
     }
 
     public void init() {
         messagesListPanel.setBounds(0, 0, 450, 360);
         messageReaderPanel.setBounds(450, 0, 350, 360);
         statusBar.setBounds(0, 360, 800, 20);
         
         statusBar.setBackground(Color.WHITE);
         
         messagesListPanel.add(messagesList.getTableHeader(), BorderLayout.PAGE_START);
         messagesListPanel.add(messagesListScrollPane, BorderLayout.CENTER);
         messageReaderPanel.add(messageReaderScrollPane, BorderLayout.CENTER);
         
         statusBar.add (refresh, BorderLayout.CENTER);
         statusBar.add (hide, BorderLayout.EAST);
         statusBar.add (remove, BorderLayout.WEST);
 
         s.add(messagesListPanel);
         s.add(messageReaderPanel);
         s.add(statusBar);
         
         // messages
         messagesListModel.addColumn("When");
         messagesListModel.addColumn("Title");
         messagesListModel.addColumn("From");
         messagesListModel.addColumn("read");
         
         TableColumn column = null;
         column = messagesList.getColumnModel().getColumn(3);
         column.setMinWidth(10);
         column.setMaxWidth(30);
         column = messagesList.getColumnModel().getColumn(1);
         column.setMinWidth(150);
         column = messagesList.getColumnModel().getColumn(0);
         column.setMinWidth(110);
         column.setMaxWidth(120);
 
         populateMessageList();
     }
     
     public void populateMessageList() {
         for (Message m : model.messages) {
             Object[] row = {
                 m.getTime(),
                 m.getTitle(),
                 m.getSender(),
                 m.read() ? "yes" : "no"
             };
             messagesListModel.addRow(row);
         }
     }
     
     public void removeMessage (int row) {
         
     }
     public void readMessage (int row) {
         if(row > (model.messages.length - 1)) return; // nullpointerexception protection
         
         Message msg = model.messages[row];
         
         if (!msg.read() || !"".equals(msg.getContent())) {
             // message is unread
             model.web.reset(travian.baseURL + "nachrichten.php?id=" + msg.getId());
             model.web.connect(false);
             
             if (!msg.read()) {
                 int count = Integer.parseInt(model.web.parser.doc.getElementById("n6").getElementsByClass("bubble-content").text()) - 1;
                 model.overview.labels[2][2].setText("Unread messages: " + (count));
                 if(count != 0) {
                     model.overview.labels[2][2].setForeground(Color.RED);
                 } else model.overview.labels[2][2].setForeground(Color.BLACK);
             }
             
             Document message;
            message = Jsoup.parse(model.web.parser.doc.getElementById("message").html().replaceAll("<br />", "l1n3bR3aKK"));
            msg.setContent(message.text().replaceAll("l1n3bR3aKK", "\n")); // save fetched message
             msg.setRead(true);
             
             messagesListModel.setValueAt("yes", row, 3);
         }
         
         messageContent.setText("Title:\t"+msg.getTitle()
                 + "\nFrom:\t"+msg.getSender()
                 + "\nWhen:\t"+msg.getTime()
                + "\n----------------------------------------"
                 + "\n\n"+msg.getContent());
         
         messageContent.setCaretPosition(0);
     }
     
     public Message[] fetchMessages () {
         model.web.reset(travian.baseURL + "nachrichten.php");
         model.web.connect(false);
         
         // get pages
         Elements paginator = model.web.parser.doc.getElementsByClass("paginator").get(0).getElementsByClass("number");
         
         Messages msg_ = new Messages();
         
         for (Element page : paginator) {
             
             if (page.tagName().equals("a")) {
                 // download new page
                 model.web.reset (travian.baseURL + page.attr("href"));
                 model.web.connect (false);
             }
             
             Elements overview = model.web.parser.doc.getElementById("overview").getElementsByTag("tbody").get(0).getElementsByTag("tr");
             
             for (int i = 0; i < overview.size(); i++) {
                 Element e = overview.get(i);
             
                 msg_.add(e);
             }
             
         }
         
         Message[] msg = new Message[msg_.size()];
         for (int i = 0; i < msg_.size(); i++) {
             msg[i] = new Message(msg_.get(i));
         }
         
         return msg;
     }
     
     public void makeVisible() {
         for (int i = 0; i < messagesListModel.getRowCount(); i++) {
             if (messagesListModel.getValueAt(i, 3) == "no") {
                 s.setVisible(true);
                 return;
             }
         }
 
         s.setVisible(false);
     }
     
     public class messageController implements MouseListener, ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             final String cmd = e.getActionCommand();
             if(cmd.equals("hide")) {
                 s.setVisible(false);
             } else if (cmd.equals("refresh")) {
                 model.refresh();
             } else if (cmd.equals("remove")) {
                 removeMessage (messagesList.getSelectedRow());
             }
         }
         
         @Override
         public void mouseClicked(MouseEvent e) {
             // Left mouse click
             if (SwingUtilities.isLeftMouseButton(e)) {
                readMessage(messagesList.getSelectedRow());
             }
         }
         @Override
         public void mousePressed(MouseEvent e) {
         }
         @Override
         public void mouseReleased(MouseEvent e) {
         }
         @Override
         public void mouseEntered(MouseEvent e) {
         }
         @Override
         public void mouseExited(MouseEvent e) {
         }
     }
 }
