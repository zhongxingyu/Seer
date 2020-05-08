 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.io.File;
 import java.util.Collections;
 import java.util.Vector;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 /**
  * This class is a frame that has some very simple trading. The Frame only needs
  * a player. If this frame is brought up and passed a player that does not have
 * a trade port location unknown things will happen.
  * 
  * @author Ryan
  * 
  */
 public class TradingMenu extends JFrame implements WindowFocusListener
 {
 
   private JScrollPane      playerScroll;
   private JScrollPane      merchantScroll;
   private JList            playersInventory;
   private JList            merchantsInventory;
   private JPanel           buttonPanel;
   private JLabel           topPlayerLabel;
   private JLabel           topMerchantLabel;
   private JPanel           playerPanel;
   private JPanel           merchantPanel;
   private JButton          buy;               // put price of selected
                                                // items in parenthesis on
                                                // button
   private JButton          sell;              // put price of selected
                                                // items in parenthesis on
   // button
   private JButton          leave;
   private JLabel           playerBank;
   private JLabel           merchantBank;
   private Player           thePlayer;
   private TradePort        theMerchant;
   private DefaultListModel merchantList;
   private DefaultListModel playerList;
   
   // selection variables for JLists 
   private int startSel = 0;
   private int endSel = 0;
 
 
   /**
    * Constructor needs a player. Has no default constructor because you need a
    * player to play
    * 
    * @param player
    *          The player of the game in the correct location
    */
   public TradingMenu(Player player)
   {
     super("Trading");
     requestFocus();
     addWindowFocusListener(this);
     thePlayer = player;
     makeMerchant();
     addMerchantModel();
     addPlayerModel();
 
     playersInventory = new JList(playerList);
     playersInventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     playersInventory.setBackground(Color.BLACK);
     playersInventory.setSelectionBackground(Color.DARK_GRAY);
     playersInventory.setSelectionForeground(Color.GREEN);
     playersInventory.setForeground(Color.WHITE);
     
     merchantsInventory = new JList(merchantList);
     merchantsInventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     merchantsInventory.setBackground(Color.BLACK);
     merchantsInventory.setSelectionBackground(Color.DARK_GRAY);
     merchantsInventory.setSelectionForeground(Color.GREEN);
     merchantsInventory.setForeground(Color.WHITE);
     
     // select first item in merchantsInventory
     merchantsInventory.setSelectionInterval(startSel, endSel);
     
     playerScroll = new JScrollPane(playersInventory);
     playerScroll
         .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     playerScroll
         .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
     merchantScroll = new JScrollPane(merchantsInventory);
     merchantScroll
         .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     merchantScroll
         .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
     buy = new JButton();
     sell = new JButton();
     leave = new JButton();
 
     topPlayerLabel = new JLabel("Player");
     topPlayerLabel.setForeground(Color.GREEN);
     topMerchantLabel = new JLabel("Merchant");
     topMerchantLabel.setForeground(Color.GREEN);
 
     playerBank = new JLabel("Player Funds: $" + player.getMoney());
     playerBank.setForeground(Color.GREEN);
     merchantBank = new JLabel("Merchant Funds: $" + theMerchant.getMoney());
     merchantBank.setForeground(Color.GREEN);
 
     playerPanel = new JPanel();
     playerPanel.setLayout(new BorderLayout());
     playerPanel.add(topPlayerLabel, BorderLayout.NORTH);
     playerPanel.add(playerScroll, BorderLayout.CENTER);
     playerPanel.add(playerBank, BorderLayout.SOUTH);
     playerPanel.setBackground(Color.DARK_GRAY);
 
     buttonPanel = new JPanel();
     buttonPanel.setLayout(new GridLayout(5, 1));
     buttonPanel.add(new JLabel());
     buttonPanel.add(buy);
     buttonPanel.add(sell);
     buttonPanel.add(leave);
     buttonPanel.add(new JLabel());
     buttonPanel.setBackground(Color.DARK_GRAY);
 
     merchantPanel = new JPanel();
     merchantPanel.setLayout(new BorderLayout());
     merchantPanel.add(topMerchantLabel, BorderLayout.NORTH);
     merchantPanel.add(merchantScroll, BorderLayout.CENTER);
     merchantPanel.add(merchantBank, BorderLayout.SOUTH);
     merchantPanel.setBackground(Color.DARK_GRAY);
 
     setLayout(new GridLayout(1, 3));
     // add(playerScroll);
     add(playerPanel);
     add(buttonPanel);
     add(merchantPanel);
     // add(merchantScroll);
     // add(playerBank);
     // add(new JLabel());
     // add(merchantBank);
 
     ButtonListener handler = new ButtonListener();
     buy.addActionListener(handler);
     buy.setFocusPainted(false);
     buy.setIcon(new ImageIcon("Art" + File.separator + "BuyButton.png"));
     buy.setSelectedIcon(new ImageIcon("Art" + File.separator + "BuyButtonPressed.png"));
     sell.addActionListener(handler);
     sell.setFocusPainted(false);
     sell.setIcon(new ImageIcon("Art" + File.separator + "SellButton.png"));
     sell.setSelectedIcon(new ImageIcon("Art" + File.separator + "SellButtonPressed.png"));
     leave.addActionListener(handler);
     leave.setFocusPainted(false);
     leave.setIcon(new ImageIcon("Art" + File.separator + "LeaveButton.png"));
     leave.setSelectedIcon(new ImageIcon("Art" + File.separator + "LeaveButtonPressed.png"));
   }
   /**
    * This method creates the merchant and checks if you have previously been there.
    */
   private void makeMerchant()
   {
     if (thePlayer.hasBeenHere(thePlayer.getLoc()))
     {
       theMerchant = thePlayer.getPort(thePlayer.getLoc());
     } else
     {
       theMerchant = new TradePort(thePlayer.getLoc());
       thePlayer.addPort(theMerchant);
     }
   }
 
   private void addPlayerModel()
   {
     playerList = new DefaultListModel();
     Vector<Item> v = thePlayer.getInventory();
     Collections.sort(v);
     for (Item item : v)
     {
       playerList.addElement(item);
     }
   }
 
   private void addMerchantModel()
   {
     merchantList = new DefaultListModel();
     Vector<Item> v = theMerchant.getInventory();
     Collections.sort(v);
     for (Item item : v)
     {
       item.modify(theMerchant.getLocale());
       merchantList.addElement(item);
     }
 
   }
 
   private void updateBanks()
   {
     playerBank.setText("Player Funds: $" + thePlayer.getMoney());
     merchantBank.setText("Merchant Funds: $" + theMerchant.getMoney());
   }
 
   private class ButtonListener implements ActionListener
   {
 
     @Override
     public void actionPerformed(ActionEvent event)
     {
       try
       {
         if (event.getSource() == buy)
         {
           Item item = (Item) merchantsInventory.getSelectedValue();
           
           // select item in merchantsInventory JList
           startSel = merchantsInventory.getSelectedIndex(); 
           endSel = startSel; 
           int nitems = merchantsInventory.getModel().getSize(); 
           if (startSel == nitems - 1) 
           {	
         	  merchantsInventory.setSelectionInterval(startSel - 1, endSel - 1); 
           } 
           else 
           { 
         	  merchantsInventory.setSelectionInterval(startSel + 1, endSel + 1); 
           } 
           
           int money = item.getModifiedPrice();
           
           if (money > thePlayer.getMoney())
           {
             topPlayerLabel.setText("Player can not afford that!");  
           } else
           {
             if(thePlayer.canFitAnotherItem())
             {
             topPlayerLabel.setText("Player");
             merchantList.removeElement(item);
             playerList.addElement(item);
             theMerchant.sale((double) item.getModifiedPrice(), item);
             thePlayer.purchase((double) item.getModifiedPrice(), item);
             updateBanks();
             //item.unModify();  
             repaint();
             }
             else
             {
               topPlayerLabel.setText("Player can not fit any more items!");
             }
           }
 
         } else if (event.getSource() == sell)
         {
           Item item = (Item) playersInventory.getSelectedValue(); 
           int money = item.getModifiedPrice();  
           if(money > theMerchant.getMoney())
           {
             topMerchantLabel.setText("Merchant can not afford that!");
           } else
           {
             topMerchantLabel.setText("Merchant");
             
             
             // select top item in playerInventory JList
             startSel = playersInventory.getSelectedIndex(); 
             endSel = startSel; 
             int nitems = playersInventory.getModel().getSize(); 
             if (startSel == nitems - 1) 
             { 
             	playersInventory.setSelectionInterval(startSel - 1, endSel - 1); 
             } 
             else 
             { 
             	playersInventory.setSelectionInterval(startSel + 1, endSel + 1); 
             } 
              
             
             playerList.removeElement(item);
             merchantList.addElement(item);
             thePlayer.sale((double) item.getModifiedPrice(), item);
             theMerchant.purchase((double) item.getModifiedPrice(), item);
             updateBanks();
             repaint();
           }
 
         } else if (event.getSource() == leave)
         {
           dispose();
           // You are welcome -Jon -- Thank you -Ryan
           Orbital loc = ((TradePort) theMerchant).getLocale();
           thePlayer.setLoc(loc.getParent());
         }
       } catch (Exception e)
       {
 
       }
     }
 
   }
 
   public void windowGainedFocus(WindowEvent arg0)
   {
   }
 
   public void windowLostFocus(WindowEvent arg0)
   {
     dispose();
   }
 }
