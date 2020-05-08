 package Panels;
 
 import Database.Database;
 import Accounts.*;
 import Products.*;
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.table.*;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import team_14_verve3.*;
 
 public class ClientPanel extends JApplet implements ActionListener{
 
     private static Client currentUser;
     private JButton deposit = new JButton("Deposit!");
     private JTextField amount = new JTextField(10);
     private static JLabel curBalance;
     public static JFrame application;
     public static String[] args;
     private JLabel welcomeLabel;
     
     private JPanel output = new JPanel(new BorderLayout());
             
     private JButton home = new JButton("Home");
     private JButton cart = new JButton("My Cart");
     private JButton search = new JButton("Search");
     private JButton mgmt = new JButton("My Account");
     private JButton logout = new JButton("Logout");
 
     private JPanel cartPanel = new JPanel();
     private JPanel searchPanel = new JPanel();
     private JPanel mgmtPanel = new JPanel();
     
     // buttons for homePanel
     private JButton musicButton = new JButton("Music");
     private JButton dvdButton = new JButton("DVD");
     private JButton bookButton = new JButton("Book");
     private JButton purchaseButton = new JButton("Purchase");
     
     private GridBagLayout layout;
     private GridBagConstraints constraints;
     
     private ProductPanel productPanel;
 
     public ClientPanel(Client client) {
         currentUser = client;
         welcomeLabel = new JLabel("Welcome "
                 + client.getName() + "!");
         
         
         layout = new GridBagLayout();
         setLayout(layout);
         constraints = new GridBagConstraints();
         constraints.fill  = GridBagConstraints.BOTH;
         constraints.weightx = 1.0;
         constraints.weighty = 1.0;
         
         // this is buttons array, used only for testing laying out
         
         JButton[] panel = new JButton[100];
        
         for (int i = 0; i < 100; i++)
         {
             panel[i] = new JButton("button" + i);
             
         }
         
         // empty panels to create nice layout
         JPanel[] panels = new JPanel[100];
         for (int i = 0; i < 100; i++)
         {
             panels[i] = new JPanel();
             //panels[i].setBorder(BorderFactory.createLineBorder(Color.black));
         }
         
         panels[4].setAlignmentY(100);
         panels[4].add(welcomeLabel);
         
         
         // first line with label
         for (int i = 0; i < 8; i++)
         {
             constraints.gridx = i;
             constraints.gridy = 0;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(panels[i], constraints);
             add(panels[i]);
         
         }
         
         
         // second line
         for (int i = 0; i < 8; i++)
         {
             constraints.gridx = i;
             constraints.gridy = 1;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(panels[8+i], constraints);
             add(panels[8+i]);
         
         }
         
         output.setBorder(BorderFactory.createLineBorder(Color.black));
         
         // pink panel
         constraints.gridx = 0;
         constraints.gridy = 2;
         constraints.gridwidth = 8;
         constraints.gridheight = 23;
         layout.setConstraints(output, constraints);
         //layout.setConstraints(searchPanel, constraints);
         //layout.setConstraints(output, constraints);
         //layout.setConstraints(output, constraints);
         add(output);
         
         remove(panels[2]);
         remove(panels[3]);
         remove(panels[4]);  // *need to modify panels4
         remove(panels[5]);
         remove(panels[6]);
         
         productPanel = new ProductPanel();
         // the panel with label
         constraints.gridx = 2;
         constraints.gridy = 0;
         constraints.gridwidth = 5;
         constraints.gridheight = 1;
         constraints.fill = GridBagConstraints.NONE;
         layout.setConstraints(panels[4], constraints);
         panels[4].setBorder(null);
         add(panels[4]);
         
         constraints.fill = GridBagConstraints.BOTH;
             
         home.addActionListener(this);
         cart.addActionListener(this);
         search.addActionListener(this);
         mgmt.addActionListener(this);
         logout.addActionListener(this);
         
         constraints.gridx = 10;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(home, constraints);
         add(home);
         
         constraints.gridx = 10;
         constraints.gridy = 1;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(cart, constraints);
         add(cart);
         
         constraints.gridx = 10;
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(search, constraints);
         add(search);
         
         constraints.gridx = 10;
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(mgmt, constraints);
         add(mgmt);
         
         constraints.gridx = 10;
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(logout, constraints);
         add(logout);
         
         // east line below five buttons
         for (int i = 0; i < 20; i++)
         {
             constraints.gridx = 10;
             constraints.gridy = 5+i;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(panels[16+i], constraints);
             add(panels[16+i]);
         
         }
         
         
         
         searchPanel.add(new JLabel("Search"));
         cartPanel.add(new JLabel("Shopping Cart"));
         mgmtPanel.add(new JLabel("Management"));
        
         musicButton.addActionListener(this);
         dvdButton.addActionListener(this);
         bookButton.addActionListener(this);
         purchaseButton.addActionListener(this);
         
         
         output.add(new ProductPanel());
     }
 
     public class OrderPanel extends JPanel implements ActionListener
     {  
         private JTable table;
         private Object[][] data;
         private String[] columnNames;
         
         public OrderPanel()
         {
             this.setLayout(new BorderLayout());
             JPanel header = new JPanel();       
             header.add(new JLabel("Users:"));
             add(header, BorderLayout.NORTH);
             String[] columnNames = {"Client Name","Item ID","Quantity"};
             int N = Database.Orders.size();
             Object[][] data = new Object[N][3];
             for(int i = 0;i<N;i++)
             {
                 Order o = Database.Orders.get(i);
                 data[i][0] = o.getName();
                 data[i][1] = o.getPID();
                 data[i][2] = o.getQuantity();  
             }
             table = new JTable(data, columnNames); 
             table.getTableHeader().setReorderingAllowed(false);
             JScrollPane scrollPane = new JScrollPane(table);   
             this.add(scrollPane, BorderLayout.CENTER);
             
             JPanel bottom = new JPanel();
             bottom.add(new JLabel("Current balance:"));
             curBalance = new JLabel(currentUser.getBalance().toString());
             bottom.add(curBalance);
             bottom.add(new JLabel("Deposit money:"));
             bottom.add(amount);
             bottom.add(deposit);
             add(bottom, BorderLayout.SOUTH);
             
             
             deposit.addActionListener(this);
             
         }
         
         public void actionPerformed(ActionEvent e)
         {
            if (amount.getText() == null)
             {
                JOptionPane.showMessageDialog(null, "Enter corrent amount!");
                 return;
             }
             double newAmount = Double.parseDouble(amount.getText());
             if (newAmount <=0)
             {
                 JOptionPane.showMessageDialog(null, "Amount can't be negative");
                 return;
             }
                 
             currentUser.depositFunds(newAmount);
             JOptionPane.showMessageDialog(null, 
                     "You succesfully deposited money!\n"+
                     "Your new balance is: " + currentUser.getBalance());
             
             output.removeAll();
             output.add(new OrderPanel());
             output.revalidate();
             output.repaint();
         }
     }
     
     public class ProductPanel extends JPanel implements TableModelListener
     {  
         private JTable table;
         private Object[][] data = {};
         private String[] columnNames = {};
         private int N;
         
         public ProductPanel()
         {
             table = new JTable();
             table = new JTable(new DefaultTableModel());
             table.getModel().addTableModelListener(this);
             JPanel header = new JPanel();       
             header.add(new JLabel("Select what type of item you want to browse:"));
             header.add(musicButton);          
             header.add(dvdButton);
             header.add(bookButton);
             header.add(purchaseButton);
             add(header, BorderLayout.NORTH);
         }
         public ProductPanel(String type)
         {
             table = new JTable(new DefaultTableModel());
             table.getModel().addTableModelListener(this);
             JPanel header = new JPanel();       
             header.add(new JLabel("Select what type of item you want to browse:"));
             header.add(musicButton);          
             header.add(dvdButton);
             header.add(bookButton);
             header.add(purchaseButton);
             add(header, BorderLayout.NORTH);
             if(type.equals("Music"))
             {
                 String[] columnNames = {"PID","Title","Artist","Rating","Price","Quantity"};
                 N = Database.musicList.size();
                 data = new Object[N][6];
                 for(int i = 0;i<N;i++)
                 {
                     Music m = Database.musicList.get(i);
                     data[i][0] = m.getProductID();
                     data[i][1] = m.getTitle();
                     data[i][2] = m.getArtist();
                     data[i][3] = m.getRating();
                     data[i][4] = m.getPrice();
                     data[i][5] = 0;
                 }
                 table = new JTable(data, columnNames); 
                 table.getTableHeader().setReorderingAllowed(false);
                 JScrollPane scrollPane = new JScrollPane(table);   
                 this.add(scrollPane);
             }
             if(type.equals("DVD"))
             {
                 String[] columnNames = {"PID","Title","Director","Rating","Price","Quantity"};
                 N = Database.DVDList.size();
                 data = new Object[N][6];
                 for(int i = 0;i<N;i++)
                 {
                     DVD m = Database.DVDList.get(i);
                     data[i][0] = m.getProductID();
                     data[i][1] = m.getTitle();
                     data[i][2] = m.getDirector();
                     data[i][3] = m.getRating();
                     data[i][4] = m.getPrice();
                     data[i][5] = 0;
                 }
                 table = new JTable(data, columnNames); 
                 table.getTableHeader().setReorderingAllowed(false);
                 JScrollPane scrollPane = new JScrollPane(table);   
                 this.add(scrollPane);
             }
             if(type.equals("Book"))
             {
                 String[] columnNames = {"PID","Title","Author","Rating","Price","Quantity"};
                 N = Database.bookList.size();
                 data = new Object[N][6];
                 for(int i = 0;i<N;i++)
                 {
                     Book m = Database.bookList.get(i);
                     data[i][0] = m.getProductID();
                     data[i][1] = m.getTitle();
                     data[i][2] = m.getAuthor();
                     data[i][3] = m.getRating();
                     data[i][4] = m.getPrice();
                     data[i][5] = 0;
                 }
                 table = new JTable(data, columnNames); 
                 table.getTableHeader().setReorderingAllowed(false);
                 JScrollPane scrollPane = new JScrollPane(table);   
                 this.add(scrollPane);
             }
         }
         void checkOut()
         {
             double totalPrice = 0;           
             if(data.length == 0)
             {
                 JOptionPane.showMessageDialog(null, "There is nothing selected here");
             }
             else
             {
                  for(int i = 0; i < N;i++)
                 {
                    System.out.println(i+((String)data[i][5]));
                    String q = table.getModel().getValueAt(i, 5).toString();
                    Double p = (Double)data[i][4];
                    System.out.println(q+ " "+p);
                    totalPrice += p*Integer.parseInt(q);
                 }
                 if(totalPrice > currentUser.getBalance())
                 {
                     JOptionPane.showMessageDialog(null, "Insufficient Balance\n"
                             + "Need: "+totalPrice+"\n Have: "+currentUser.getBalance());
                 }
                 else
                 {
                     for(int i = 0; i < N;i++)
                     {
                         String q = (String)data[i][5];
                         if(Integer.parseInt(q) > 0)
                         {
                             String name = currentUser.getName();
                             String PID = Database.musicList.get(i).getProductID();
                             int Quantity = Integer.parseInt(q);
                             Database.Orders.add(new Order(name,PID,Quantity));
                         }
                     }
                     double before = currentUser.getBalance();
                     currentUser.buy(totalPrice);
                     double after = currentUser.getBalance();
                     JOptionPane.showMessageDialog(null, "Successful!\n"
                             + "Balance Before: "+before+"\n After: "+currentUser.getBalance());
                 }
             }
            
         }
 
         @Override
         public void tableChanged(TableModelEvent e) 
         {
             int row = e.getFirstRow();
             int column = e.getColumn();
             TableModel model = (TableModel)e.getSource();
             String columnName = model.getColumnName(column);
             Object data = model.getValueAt(row, column);
             System.out.println(columnName);
         }
     }
     
     @Override
     public void init() {
         
     }
     
      @Override 
      public void paint(Graphics g) 
      { 
          super.paint(g); 
      }
     public static void main(String[] args,Client user) {
         application = new JFrame();
         ClientPanel clientPanel = new ClientPanel(user);
         application.setTitle("verve3 Client Panel");
         application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         application.setSize(800, 600);
         application.setResizable(false);
         application.add(clientPanel);
         application.setLocationRelativeTo(null);
         application.setVisible(true);
     }
     
     @Override
     public void actionPerformed(ActionEvent e){
         if (e.getSource() == home)
         {
             output.removeAll();
             output.add(new ProductPanel());
             output.revalidate();
             output.repaint();
         }
         
         if (e.getSource() == cart)
         {
             output.removeAll();
             output.add(cartPanel);
             output.revalidate();
             output.repaint();
         }
         
         if (e.getSource() == search)
         {
             output.removeAll();
             output.add(searchPanel);
             output.revalidate();
             output.repaint();
         }
         
         if (e.getSource() == mgmt)
         {
             output.removeAll();
             output.add(new OrderPanel());
             output.revalidate();
             output.repaint();
         }
         
         if (e.getSource() == logout)
         {
             application.setVisible(false);
             StartPanel.main(args);
         }
         
         // defining behavior for buttons from homePanel
         if (e.getSource() == musicButton)
         {
             output.removeAll();
             productPanel = new ProductPanel("Music");
             output.add(productPanel);
             output.revalidate();
             output.repaint();
         }
         
         if (e.getSource() == dvdButton)
         {
             output.removeAll();
             productPanel = new ProductPanel("DVD");
             output.add(productPanel);
             output.revalidate();
             output.repaint();
         }
         
         if (e.getSource() == bookButton)
         {
             output.removeAll();
             productPanel = new ProductPanel("Book");
             output.add(productPanel);
             output.revalidate();
             output.repaint();           
         }
         
         if (e.getSource() == purchaseButton)
         {
             productPanel.checkOut();
             output.removeAll();
             productPanel = new ProductPanel();
             output.add(productPanel);
             output.revalidate();
             output.repaint();           
         }
     }
 }
