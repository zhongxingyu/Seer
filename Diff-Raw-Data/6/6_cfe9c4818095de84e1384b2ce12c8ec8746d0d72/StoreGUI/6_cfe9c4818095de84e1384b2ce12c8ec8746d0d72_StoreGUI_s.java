 package store;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.border.Border;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ListIterator;
 
 public class StoreGUI extends JFrame implements ItemListener, ActionListener {
 
   private JPanel                  search;
   private JPanel                  view;
   private JPanel                  purchase;
   private JPanel                  rate;
   private JPanel                  thankYou;
   private JPanel                  managerPanel;
   private JPanel                  addMediaPanel;
   private JPanel                  custInfoPanel;
   private JPanel                  custInfoSubPanel;
   private JPanel                  removeMediaPanel;
   private JPanel                  checkItemPanel;
   private JPanel                  addCheckPanel;
   private JPanel                  addOldMediaPanel;
   private JPanel                  addNewMediaPanel;
   private JLabel                  welcomeLabel;
   private JLabel                  medTypeText;
   private JLabel                  searchByText;
   private JTextField              searchField;
   private JLabel                  mngrText;
   private JLabel                  creatorLabel;
   private JLabel                  mediaNameLabel;
   private JLabel                  mediaDuration;
   private JLabel                  mediaGenre;
   private JLabel                  mediaPrice;
   private JLabel                  defaultRating;
   private JLabel                  defaultAvgRating;
   private JLabel                  newID;
   private JLabel                  customerCredit = null;
   private JLabel                  addType;
   private JLabel                  addAmount;
   private JLabel                  removeLabel;
   private JLabel                  ChkLabel;
   private JLabel                  oldMediaLabel1;
   private JLabel                  oldMediaLabel2;
   private JLabel                  checkItemLabel;
   private JTextField              creatorText;
   private JTextField              newNameText;
   private JTextField              newDurationText;
   private JTextField              newGenreText;
   private JTextField              newPriceText;
   private JTextField              newDefRateText;
   private JTextField              newAvgRateText;
   private JTextField              newIDText;
   private JTextField              addTypeText;
   private JTextField              addAmountText;
   private JTextField              custIdText;
   private JTextField              removeText1;
   private JTextField              removeText2;
   private JTextField              checkItemText;
   private JTextField              oldIDText;
   private JTextField              oldAmountText;
   private JButton                 go;
   private JButton                 mngrLoginButton;
   private JButton                 viewItem;
   private JButton                 purchaseItem;
   private JButton                 custLogout;
   private JLabel                  purchaseInfo;
   private JButton                 searchAgain;
   private JButton                 goBack;
   private JButton                 mngrAdd;
   private JButton                 mngrRemove;
   private JButton                 mngrChkSales;
   private JButton                 mngrChkTotal;
   private JButton                 mngrCustInfo;
   private JButton                 mngrLogout;
   private JButton                 addButton;
   private JButton                 clearButton;
   private JButton                 back1Button;
   private JButton                 searchCustInfo;
   private JButton                 loginButton;
   private JButton                 removeMediaButton;
   private JButton                 back2Button;
   private JButton                 clear2Button;
   private JButton                 checkItemButton;
   private JButton                 clear3Button;
   private JButton                 back3Button;
   private JButton                 back4Button;
   private JButton                 custInfoSubmit;
   private JButton                 ChkButton1;
   private JButton                 ChkButton2;
   private JButton                 addOldMediaButton;
   private JButton                 addOldMediaClear;
   private JButton                 addOldMediaBack;
   private JButton                 ChkBack;
   private JRadioButton            rate1;
   private JRadioButton            rate2;
   private JRadioButton            rate3;
   private JRadioButton            rate4;
   private JRadioButton            rate5;
   private double                  rating;
   private String                  rated;
   private JButton                 submit;
   private JComboBox               mediaType;
   private JComboBox               searchType;
   private JTabbedPane             tabs;
 
   // Maps buttons in view tab to media object being displayed
   private HashMap<JButton, Media> viewButtons;
 
   // Current logged in user
   private User                    user;
 
   // MANAGER-only LOG IN
   private int                     pswInt;
   private JPasswordField          passField;
   private boolean                 isManager;
 
   // User (Customer and Manager) LOG IN
   private int                     custPswInt;
   private JPanel                  custPanel;
   private JTextField              custUserField;
   private JPasswordField          custPassField;
 
   // MEDIA
   private Media                   mediaObj       = DBIO.getMedia(4);
   private int                     numToBuy       = 1;
 
   // PLACEHOLDER DATABASE
   private String[]                medTypeArray;
   private String[]                searchTypeArray;
 
   public StoreGUI() {
     super("Store GUI");
    //somethign
     DBIO.init();
     DBIO.setDb("src/store/Store.sqlite");
 
     user = null;
     isManager = false;
 
     tabs = new JTabbedPane();
 
     // 4 panels are created
     search = new JPanel(new GridLayout(20, 2));
     view = new JPanel(new GridBagLayout());
     purchase = new JPanel(new GridLayout(9, 1));
     rate = new JPanel();
     thankYou = new JPanel();
 
     // Create Manager panel,display only when correct password is input
     managerPanel = new JPanel(new GridLayout(14, 2, 10, 10));
     addNewMediaPanel = new JPanel(new GridLayout(12, 2));
     removeMediaPanel = new JPanel(new GridLayout(12, 2));
     checkItemPanel = new JPanel(new GridLayout(12, 2));
 
     // ComboBox arrays, neither of these will change so no db interaction
     // needed
     medTypeArray = new String[] { "Albums", "Movies", "Audiobooks" };
 
     searchTypeArray = new String[] { "Genre", "Artist", "Producer", "Author",
         "Item Name" };
     // creates the welcome label
     welcomeLabel = new JLabel("Welcome to our Media Store!");
     welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
     welcomeLabel.setForeground(Color.DARK_GRAY);
     welcomeLabel.setFont(new Font("", Font.BOLD, 24));
 
     // set up the customer login button
     loginButton = new JButton("Login");
     loginButton.setHorizontalAlignment(JButton.CENTER);
     loginButton.addActionListener(this);
 
     // set up customer logout button
     custLogout = new JButton("Logout");
     custLogout.addActionListener(this);
     custLogout.setToolTipText("Log out of system, return to Search screen.");
 
     // creates a label for the search field
     JLabel searchLabel = new JLabel("Search");
 
     // Media type search specifier
     medTypeText = new JLabel("Media Type:");
     medTypeText.setHorizontalAlignment(JTextField.LEFT);
     medTypeText.setForeground(Color.DARK_GRAY);
     medTypeText.setFont(new Font("", Font.BOLD, 10));
 
     // Select the type of media from drop box
     mediaType = new JComboBox(medTypeArray);
     mediaType.addActionListener(this);
 
     // initialize a label for the search by
     searchByText = new JLabel("What would you like to search by?");
     searchByText.setHorizontalAlignment(JTextField.LEFT);
     searchByText.setForeground(Color.DARK_GRAY);
     searchByText.setFont(new Font("", Font.BOLD, 10));
     searchByText.setVisible(false);
 
     // Select how you want to search
     searchType = new JComboBox(searchTypeArray);
     searchType.addActionListener(this);
     searchType.setVisible(false);
 
     // creates the text field for searching
     searchField = new JTextField(10);
 
     // creates the search button
     go = new JButton("GO!");
     go.addActionListener(this);
 
     // creates the view button
     viewItem = new JButton("View");
     viewItem.addActionListener(this);
 
     // creates the purchase button
     purchaseItem = new JButton("Buy");
     purchaseItem.addActionListener(this);
     goBack = new JButton("Go Back");
     goBack.addActionListener(this);
 
     // label for the rate button group
     JLabel rateLabel = new JLabel("Rate: ");
 
     ButtonGroup rateItem = new ButtonGroup(); // creates a button group
     rating = 0;
     rate1 = new JRadioButton("1", false); // instantiates button 1
     rate1.addItemListener(this);
     rate2 = new JRadioButton("2", false); // instantiates button 2
     rate2.addItemListener(this);
     rate3 = new JRadioButton("3", false); // instantiates button 3
     rate3.addItemListener(this);
     rate4 = new JRadioButton("4", false); // instantiates button 4
     rate4.addItemListener(this);
     rate5 = new JRadioButton("5", false); // instantiates button 5
     rate5.addItemListener(this);
 
     submit = new JButton("Submit");
     submit.addActionListener(this);
     // adds all 5 rate buttons to the group
     rateItem.add(rate1);
     rateItem.add(rate2);
     rateItem.add(rate3);
     rateItem.add(rate4);
     rateItem.add(rate5);
 
     searchAgain = new JButton("Search Again");
     searchAgain.addActionListener(this);
 
     /*
      * Add Items to Panels and Layouts
      */
 
     search.add(welcomeLabel); // add the welcome label to the search panel
     search.add(searchLabel); // adds the text "Search" to the search panel
     search.add(searchField); // adds the text field to the search panel
     search.add(medTypeText);
     search.add(mediaType);
     search.add(searchByText);
     search.add(searchType);
 
     search.add(go); // adds the go button to the search panel
     search.add(loginButton); // add the customer login button
 
     tabs.addTab("Search", search);
 
     view.add(viewItem); // adds the view button to the view panel
 
     rate.add(rateLabel); // adds the text "Rate: " to the rate panel
     rate.add(rate1); // adds the lowest rating button to the panel
     rate.add(rate2); // adds the next lowest rating button to the panel
     rate.add(rate3); // adds the middle rating button to the panel
     rate.add(rate4); // adds the fourth rating button to the panel
     rate.add(rate5); // adds the highest rating button to the panel
     rate.add(submit);
 
     thankYou.add(searchAgain);
 
     // Populate the Manager Panel
     mngrText = new JLabel("What would you like to do?");
     mngrText.setHorizontalAlignment(JTextField.CENTER);
     mngrText.setFont(new Font("", Font.BOLD, 16));
 
     mngrAdd = new JButton("Add Media");
     mngrAdd.addActionListener(this);
     mngrAdd.setToolTipText("Lets you manually add Media to the store.");
 
     mngrRemove = new JButton("Remove Media");
     mngrRemove.addActionListener(this);
     mngrRemove.setToolTipText("Lets you manually remove Media from the store.");
 
     mngrChkSales = new JButton("Specific Item Sales");
     mngrChkSales.addActionListener(this);
     mngrChkSales
         .setToolTipText("Check the total sales of one specific item in the store.");
 
     mngrChkTotal = new JButton("Store Sales");
     mngrChkTotal.addActionListener(this);
     mngrChkTotal.setToolTipText("Check the total store sales.");
 
     mngrCustInfo = new JButton("Customer Information");
     mngrCustInfo.addActionListener(this);
     mngrCustInfo.setToolTipText("Retreive a Customer's information.");
 
     mngrLogout = new JButton("Logout");
     mngrLogout.addActionListener(this);
     mngrLogout.setToolTipText("Log out of system, return to Search screen.");
 
     // addCheckPanel
     addCheckPanel = new JPanel(new GridLayout(12, 1));
 
     ChkLabel = new JLabel("What would you like to add to the Store?");
     ChkLabel.setHorizontalAlignment(JLabel.CENTER);
 
     ChkButton1 = new JButton("Add more of an existing item.");
     ChkButton1.addActionListener(this);
     ChkButton1
         .setToolTipText("Will add more media if given ID number, and Amount.");
 
     ChkButton2 = new JButton("Add a new Media Item");
     ChkButton2.addActionListener(this);
     ChkButton2.setToolTipText("Adds a new item to the store.");
 
     ChkBack = new JButton("Back to Manager Page");
     ChkBack.addActionListener(this);
     ChkBack.setToolTipText("Goes back to the Manager Home page");
 
     addCheckPanel.add(ChkLabel);
     addCheckPanel.add(ChkButton1);
     addCheckPanel.add(ChkButton2);
     addCheckPanel.add(ChkBack);
 
     // addOldMediaPanel
     addOldMediaPanel = new JPanel(new GridLayout(12, 2));
 
     oldMediaLabel1 = new JLabel("Enter the ID number:");
     oldMediaLabel2 = new JLabel("How many would you like to add?");
 
     oldIDText = new JTextField(20);
     oldAmountText = new JTextField(20);
 
     addOldMediaButton = new JButton("Add Media");
     addOldMediaButton.addActionListener(this);
 
     addOldMediaClear = new JButton("Clear Fields");
     addOldMediaClear.addActionListener(this);
 
     addOldMediaBack = new JButton("Go Back");
     addOldMediaBack.addActionListener(this);
 
     addOldMediaPanel.add(oldMediaLabel1);
     addOldMediaPanel.add(oldIDText);
     addOldMediaPanel.add(oldMediaLabel2);
     addOldMediaPanel.add(oldAmountText);
     addOldMediaPanel.add(addOldMediaButton);
     addOldMediaPanel.add(addOldMediaClear);
     addOldMediaPanel.add(addOldMediaBack);
 
     // Manager addNewMediaPanel Labels:
     creatorLabel = new JLabel("Enter the Author/Artist/Producer name:");
     mediaNameLabel = new JLabel("Enter the Title:");
     mediaDuration = new JLabel(
         "Enter the Duration(in total number of minutes):");
     mediaGenre = new JLabel("Enter the genre:");
     mediaPrice = new JLabel("Enter the price:");
     defaultRating = new JLabel("Default Rating(Out of 5):");
     defaultAvgRating = new JLabel("Default Average Rating(Out of 5):");
     newID = new JLabel("Assign an ID number:");
     addType = new JLabel("Enter one of the following: Book, Album, or Movie");
     addAmount = new JLabel("How many would you like to add to the store?");
 
     // Manager addMediaPanel Buttons
     // add media button
     addButton = new JButton("Add Media to Store");
     addButton.addActionListener(this);
     addButton
         .setToolTipText("Will commit the new Media into out store Inventory");
 
     // clear text field button
     clearButton = new JButton("Clear Entries");
     clearButton.addActionListener(this);
     clearButton.setToolTipText("Clears all text fields above.");
 
     // back button, allows the manager to go back to manager home page
     back1Button = new JButton("Back to Manager Page");
     back1Button.addActionListener(this);
     back1Button.setToolTipText("Go back to the Manager Home Screen.");
 
     // Manager addNewMediaPanel TextFields
     creatorText = new JTextField(20);
     newNameText = new JTextField(20);
     newDurationText = new JTextField(20);
     newGenreText = new JTextField(20);
     newPriceText = new JTextField(20);
     newDefRateText = new JTextField(20);
     newAvgRateText = new JTextField(20);
     addTypeText = new JTextField(20);
     addAmountText = new JTextField(20);
 
     // Add components to addNewMediaPanel
     addNewMediaPanel.add(creatorLabel);
     addNewMediaPanel.add(creatorText);
 
     addNewMediaPanel.add(mediaNameLabel);
     addNewMediaPanel.add(newNameText);
 
     addNewMediaPanel.add(mediaDuration);
     addNewMediaPanel.add(newDurationText);
 
     addNewMediaPanel.add(mediaGenre);
     addNewMediaPanel.add(newGenreText);
 
     addNewMediaPanel.add(mediaPrice);
     addNewMediaPanel.add(newPriceText);
 
     addNewMediaPanel.add(defaultRating);
     addNewMediaPanel.add(newDefRateText);
 
     addNewMediaPanel.add(defaultAvgRating);
     addNewMediaPanel.add(newAvgRateText);
 
     addNewMediaPanel.add(addType);
     addNewMediaPanel.add(addTypeText);
 
     addNewMediaPanel.add(addAmount);
     addNewMediaPanel.add(addAmountText);
 
     addNewMediaPanel.add(addButton);
     addNewMediaPanel.add(clearButton);
     addNewMediaPanel.add(back1Button);
 
     managerPanel.add(mngrText);
     managerPanel.add(mngrAdd);
     managerPanel.add(mngrRemove);
     managerPanel.add(mngrChkSales);
     managerPanel.add(mngrChkTotal);
     managerPanel.add(mngrCustInfo);
     managerPanel.add(mngrLogout);
 
     // REMOVE MEDIA PANEL
     removeLabel = new JLabel("In order to Remove Media, input the ID# "
         + "and amount you wish to remove from out inventory:");
     removeText1 = new JTextField(20);
     removeText2 = new JTextField(20);
     removeMediaButton = new JButton("Remove");
     removeMediaButton.addActionListener(this);
     removeMediaButton.setToolTipText("Will delete the specified number of"
         + "a Media object from inventory.");
 
     clear2Button = new JButton("Clear Entries");
     clear2Button.addActionListener(this);
     clear2Button.setToolTipText("Clears the text Fields");
 
     back2Button = new JButton("Back to Manager Screen");
     back2Button.addActionListener(this);
     back2Button.setToolTipText("Takes you back to the Manager Home Screen");
 
     removeMediaPanel.add(removeLabel);
     removeMediaPanel.add(removeText1);
     removeMediaPanel.add(removeText2);
     removeMediaPanel.add(removeMediaButton);
     removeMediaPanel.add(clear2Button);
     removeMediaPanel.add(back2Button);
 
     // CHECK ITEM SALE PANEL
     checkItemLabel = new JLabel(
         "Enter the ID# to see the total sales of that item.");
     checkItemText = new JTextField(20);
     checkItemButton = new JButton("Check Sales");
     checkItemButton.addActionListener(this);
     clear3Button = new JButton("Clear Entry");
     clear3Button.addActionListener(this);
     back3Button = new JButton("Back to Manager Screen");
     back3Button.addActionListener(this);
 
     checkItemPanel.add(checkItemLabel);
     checkItemPanel.add(checkItemText);
     checkItemPanel.add(checkItemButton);
     checkItemPanel.add(clear3Button);
     checkItemPanel.add(back3Button);
 
     // Customer info panel
     buildCustInfo();
 
     // Customer Login Panel (used in dialog)
     buildCustLogin();
 
     add(tabs);
   }
 
   /**
    * Initialize custInfoPanel - Panel Manager can look at customer info
    */
   protected void buildCustInfo() {
     GridBagLayout gbl = new GridBagLayout();
     GridBagConstraints c = new GridBagConstraints();
     custInfoPanel = new JPanel(gbl);
     JLabel lookupLabel;
 
     c.weightx = 1;
 
     // Lookup Label
     lookupLabel = new JLabel("Lookup Customer by ID Number");
     gbl.setConstraints(lookupLabel, c);
     custInfoPanel.add(lookupLabel);
 
     // Lookup Text Area
     custIdText = new JTextField(10);
     c.gridwidth = GridBagConstraints.REMAINDER;
     gbl.setConstraints(custIdText, c);
     custInfoPanel.add(custIdText);
 
     // Back Button
     c.gridwidth = 1;
     back4Button = new JButton("Back to Manager Page");
     back4Button.addActionListener(this);
     back4Button.setToolTipText("Go back to the Manager Home Screen.");
     gbl.setConstraints(back4Button, c);
     custInfoPanel.add(back4Button);
 
     // Submit Search
     c.gridwidth = GridBagConstraints.REMAINDER;
     custInfoSubmit = new JButton("Submit");
     gbl.setConstraints(custInfoSubmit, c);
     custInfoSubmit.addActionListener(this);
     custInfoPanel.add(custInfoSubmit);
 
   }
 
   public void getCustCredit() {
     if (customerCredit != null) {
       search.remove(customerCredit);
     }
     customerCredit = new JLabel("Balance: $" + user.getBalance());
     search.add(customerCredit);
   }
 
   /**
    * Refresh customer info information in custInfoPanel with uId' info.
    * 
    * @param uId
    *          ID Number of uId to lookup
    */
   protected void refreshCustInfo(int uId) {
     if (custInfoSubPanel != null) {
       custInfoPanel.remove(custInfoSubPanel);
     }
     User user = DBIO.getUser(uId);
     GridBagLayout gbl = new GridBagLayout();
     GridBagConstraints c = new GridBagConstraints();
     custInfoSubPanel = new JPanel(gbl);
     if (user != null) {
       c.weightx = 1;
       c.gridwidth = GridBagConstraints.REMAINDER;
 
       // Info Title
       JLabel infoTitle = new JLabel(String.format("Customer Info %d",
           user.getID()));
       c.insets = new Insets(5, 5, 5, 5);
       gbl.setConstraints(infoTitle, c);
       custInfoSubPanel.add(infoTitle);
 
       // Customer name
       JLabel name = new JLabel("Name: " + user.getName());
       c.insets = new Insets(0, 0, 0, 0);
       gbl.setConstraints(name, c);
       custInfoSubPanel.add(name);
 
       // Customer Balance
       JLabel bal = new JLabel(
           String.format("Balance: $%.2f", user.getBalance()));
       gbl.setConstraints(bal, c);
       custInfoSubPanel.add(bal);
 
       // Customer ID
       JLabel id = new JLabel(String.format("ID: %d", user.getID()));
       gbl.setConstraints(id, c);
       custInfoSubPanel.add(id);
 
       // Customer City
       JLabel city = new JLabel("City: " + user.getcity());
       gbl.setConstraints(city, c);
       custInfoSubPanel.add(city);
 
       // Customer History
       JLabel hist = new JLabel("History: "
           + String.format("%d", user.getHistory().length));
       gbl.setConstraints(hist, c);
       custInfoSubPanel.add(hist);
 
       // Customer Shopping Cart
       JLabel shpCrt = new JLabel("Shopping Cart: "
           + String.format("%d", user.getShoppingCart().length));
       gbl.setConstraints(shpCrt, c);
       custInfoSubPanel.add(shpCrt);
     } else {
       JLabel noSuch = new JLabel("No such user");
       gbl.setConstraints(noSuch, c);
       custInfoSubPanel.add(noSuch);
     }
     // Change up of gbl and c
     gbl = (GridBagLayout) custInfoPanel.getLayout();
     c.fill = GridBagConstraints.BOTH;
     c.gridwidth = 2;
     c.insets = new Insets(10, 10, 10, 10);
     gbl.setConstraints(custInfoSubPanel, c);
     custInfoPanel.add(custInfoSubPanel);
 
   }
 
   /**
    * Small helper function to set constraints and add a label to a JPanel
    * 
    * @param text
    *          String text shown in the label
    * @param gbl
    *          GridBagLayout to set the constraints on
    * @param c
    *          GridBagConstraints to set
    * @param jp
    *          JPanel to add the label to
    */
   protected void makeLabel(String text, GridBagLayout gbl,
       GridBagConstraints c, JPanel jp) {
     Label label = new Label(text);
     gbl.setConstraints(label, c);
     jp.add(label);
   }
 
   /**
    * Creates a JPanel displaying a snippet view of an items information
    * 
    * @param mObj
    *          Media object to display info about
    * @return JPanel with information displayed
    */
   protected JPanel makeItemSnippetPanel(Media mObj) {
     GridBagLayout gridBag = new GridBagLayout();
     GridBagConstraints c = new GridBagConstraints();
     JPanel itemDisp = new JPanel(gridBag);
     JButton viewButton = new JButton("View");
     String creatorAlias = "";
 
     // Constraints default
     // c.fill = GridBagConstraints.BOTH;
     c.weightx = 1;
     c.weighty = 1;
 
     // Name
     c.anchor = GridBagConstraints.LINE_START;
     makeLabel(mObj.getName(), gridBag, c, itemDisp);
 
     // type
     c.anchor = GridBagConstraints.LINE_END;
     c.gridwidth = GridBagConstraints.REMAINDER;
     if (mObj instanceof Album) {
       makeLabel("Genre: Album", gridBag, c, itemDisp);
       creatorAlias = "Artist: ";
     } else if (mObj instanceof Audiobook) {
       makeLabel("Genre: Audiobook", gridBag, c, itemDisp);
       creatorAlias = "Author: ";
     } else if (mObj instanceof Movie) {
       makeLabel("Genre: Movie", gridBag, c, itemDisp);
       creatorAlias = "Producer: ";
     }
 
     // creator
     c.anchor = GridBagConstraints.LINE_START;
     c.gridwidth = 1;
     makeLabel(creatorAlias + mObj.getCreator(), gridBag, c, itemDisp);
 
     // price
     c.anchor = GridBagConstraints.LINE_END;
     c.gridwidth = GridBagConstraints.REMAINDER;
     makeLabel(String.format("Price: $%.2f", mObj.getPrice()), gridBag, c,
         itemDisp);
 
     // View button
     viewButtons.put(viewButton, mObj);
     c.anchor = GridBagConstraints.CENTER;
     gridBag.setConstraints(viewButton, c);
     itemDisp.add(viewButton);
     viewButton.addActionListener(this);
 
     // Border
     itemDisp
         .setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
 
     return itemDisp;
   }
 
   /**
    * Build the view tab
    */
   protected void buildView() {
     String searchTypeStr = (String) searchType.getSelectedItem();
     String mediaTypeStr = (String) mediaType.getSelectedItem();
     GridBagLayout vLayout = (GridBagLayout) view.getLayout();
     GridBagConstraints vConstraint = new GridBagConstraints();
     JPanel itemSnippetPanel;
 
     ArrayList<Media> searchResults = User.search(searchField.getText(),
         sTypeToEnum(searchTypeStr), mTypeToEnum(mediaTypeStr));
     view.removeAll();
     viewButtons = new HashMap<JButton, Media>();
 
     // Set constraints
     vConstraint.gridwidth = GridBagConstraints.REMAINDER;
     vConstraint.fill = GridBagConstraints.HORIZONTAL;
     vConstraint.weightx = 1;
 
     for (Media searchResult : searchResults) {
       itemSnippetPanel = makeItemSnippetPanel(searchResult);
       vLayout.setConstraints(itemSnippetPanel, vConstraint);
       view.add(itemSnippetPanel);
 
     }
     // TODO: remove debug
     view.setBackground(Color.BLUE);
   }
 
   /**
    * Build customer login pane
    */
   protected void buildCustLogin() {
     custPanel = new JPanel(new GridLayout(2, 2, 0, 10));
     custUserField = new JTextField(10);
     custPassField = new JPasswordField(10);
     custPanel.add(new JLabel("Username:"));
     custPanel.add(custUserField);
     custPanel.add(Box.createVerticalStrut(10));
     custPanel.add(new JLabel("Password:"));
     custPanel.add(custPassField);
   }
 
   /**
    * Logs in functionality
    */
   private void loginUser() {
     custPswInt = JOptionPane.showConfirmDialog(null, custPanel,
         "Enter your username and password to login.",
         JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 
     char pass[] = custPassField.getPassword();
     // If User clicks OK
     if (custPswInt == JOptionPane.OK_OPTION) {
       // If login successfully
       if ((user = DBIO.login(custUserField.getText(), new String(pass))) != null) {
         // Add Manger tabs if the user logged in is a manager
         if (user instanceof Manager) {
           isManager = true;
           // Display the manager tab only on correct
           tabs.addTab("Manager", managerPanel);
           tabs.remove(search); // login
         } else if (user instanceof Customer) {
           search.add(custLogout);
           search.remove(loginButton);
           getCustCredit();
 
         }
         JOptionPane.showMessageDialog(this, "Welcome Back!");
       } else {
         JOptionPane.showMessageDialog(this, "Incorrect Password");
       }
     }
     // ALWAYS reset text password for security
     for (int i = 0; i < pass.length; i++) {
       pass[i] = 0;
     }
     custUserField.setText("");
     custPassField.setText("");
   }
 
   /**
    * Add a media object functionality.
    */
   private void addMedia() {
     // TODO:implement this
   }
 
   /**
    * Helper to turn a human-readable search type string to an enum
    * 
    * @param searchField
    * @return
    */
   private DBIO.SearchField sTypeToEnum(String searchField) {
     if (searchField.equalsIgnoreCase("Genre")) {
       return DBIO.SearchField.GENRE;
     } else if (searchField.equalsIgnoreCase("Artist")
         || searchField.equalsIgnoreCase("Producer")
         || searchField.equalsIgnoreCase("Author")) {
       return DBIO.SearchField.CREATOR;
     } else if (searchField.equalsIgnoreCase("Item Name")) {
       return DBIO.SearchField.NAME;
     }
     return DBIO.SearchField.NAME;
 
   }
 
   /**
    * Helper to turn human-readable media type string to an enum
    * 
    * @param mType
    * @return
    */
   private DBIO.Types mTypeToEnum(String mType) {
     if (mType.equalsIgnoreCase("Albums")) {
       return DBIO.Types.ALBUM;
     } else if (mType.equalsIgnoreCase("Movies")) {
       return DBIO.Types.MOVIE;
     } else if (mType.equalsIgnoreCase("Audiobooks")) {
       return DBIO.Types.AUDIOBOOK;
     }
     return DBIO.Types.ALBUM;
   }
 
   @Override
   public void paint(Graphics g) {
     super.paint(g);
 
   }
 
   @Override
   public void itemStateChanged(ItemEvent e) {
     if (e.getSource() == rate1 && e.getStateChange() == ItemEvent.SELECTED) {
       rating = 1;
     }
 
     if (e.getSource() == rate2 && e.getStateChange() == ItemEvent.SELECTED) {
       rating = 2;
     }
 
     if (e.getSource() == rate3 && e.getStateChange() == ItemEvent.SELECTED) {
       rating = 3;
     }
 
     if (e.getSource() == rate4 && e.getStateChange() == ItemEvent.SELECTED) {
       rating = 4;
     }
 
     if (e.getSource() == rate5 && e.getStateChange() == ItemEvent.SELECTED) {
       rating = 5;
     }
   }
 
   @Override
   public void actionPerformed(ActionEvent e) {
     Integer uid; // Used in custInfoSubmit if
     // Handle the manager login attempt
     // if customer login button is pressed
     if (e.getSource() == loginButton) {
       loginUser();
     }
 
     // Handle the mngrAdd button
     if (e.getSource() == mngrAdd) // if mngrAdd button was pressed
     {
       tabs.addTab("Add Media", addCheckPanel);
       // tabs.addTab("Add Media", addMediaPanel); ***REMOVE
       tabs.remove(managerPanel);
     }
 
     // Handle the ChkButton1
     if (e.getSource() == ChkButton1) {
       tabs.addTab("Add Media", addOldMediaPanel);
       tabs.remove(addCheckPanel);
     }
 
     // Handle the ChkButton2
     if (e.getSource() == ChkButton2) {
       tabs.addTab("Add Media", addNewMediaPanel);
       tabs.remove(addCheckPanel);
     }
 
     // Handle the ChkBack Button
     if (e.getSource() == ChkBack) {
       tabs.addTab("Manager", managerPanel);
       tabs.remove(addCheckPanel);
     }
 
     // HANDLE ADDING OLD MEDIA PANEL
     if (e.getSource() == addOldMediaButton) {
       Media oldMedia = DBIO.getMedia(Integer.parseInt(oldIDText.getText()));
 
       if (user instanceof Manager) {
         ((Manager) user).addMedia(oldMedia,
             Integer.parseInt(oldAmountText.getText()));
 
       }
     }
 
     if (e.getSource() == addOldMediaClear) {
       oldIDText.setText("");
       oldAmountText.setText("");
     }
 
     if (e.getSource() == addOldMediaBack) {
       tabs.addTab("Add Media", addCheckPanel);
       tabs.remove(addOldMediaPanel);
     }
 
     // Handle the mngrChkTotal button
     if (e.getSource() == mngrChkTotal) {
       double newTotalSales = DBIO.getTotalSales();
       JOptionPane.showMessageDialog(addMediaPanel, newTotalSales,
           "Total Store Sales", 2);
     }
 
     // Handle mngrCustInfo
     if (e.getSource() == mngrCustInfo) {
       tabs.addTab("Customer Info", custInfoPanel);
       tabs.remove(managerPanel);
 
     }
     // Handle customer info lookup
     if (e.getSource() == custInfoSubmit) {
       try {
         uid = new Integer(custIdText.getText());
       } catch (NumberFormatException nfe) {
         uid = 0;
       }
       refreshCustInfo(uid);
       tabs.validate();
     }
     if (e.getSource() == back4Button) {
       tabs.addTab("Manager", managerPanel);
       tabs.remove(custInfoPanel);
     }
 
     // Handle the Manager Logout button in managerPanel
     if (e.getSource() == mngrLogout) // if manager logout button is pressed
     {
       pswInt = JOptionPane.showConfirmDialog(null, null,
           "Are you sure you want to log out?", JOptionPane.YES_NO_OPTION);
 
       if (pswInt == JOptionPane.YES_OPTION) // prompts the manager to
       { // to make sure they really
        tabs.addTab("Search", search); // want to logout.
         tabs.remove(managerPanel);
       }
 
     }
     // Handle customer logout button
     if (e.getSource() == custLogout) // if customer logout button is pressed
     {
       pswInt = JOptionPane.showConfirmDialog(null, null,
           "Are you sure you want to log out?", JOptionPane.YES_NO_OPTION);
       if (pswInt == JOptionPane.YES_OPTION) // prompts the customer to
       { // to make sure they really want to logout
         user = null;
         tabs.remove(view);
         view.remove(purchaseItem);
         search.remove(custLogout);
         search.remove(customerCredit);
         search.add(loginButton);
 
         view.revalidate();
         search.revalidate();
 
       }
 
     }
     // HANDLE ADDING A MEDIA OBJECT
     if (e.getSource() == addButton) {
       /*
        * Media addedMedia = null; addedMedia.creator = creatorText.getText();
        * addedMedia.name = newNameText.getText(); addedMedia.duration =
        * Integer.parseInt(newDurationText.getText()); addedMedia.genre =
        * newGenreText.getText(); addedMedia.price =
        * Double.parseDouble(newPriceText.getText()); addedMedia.numRating =
        * Integer.parseInt(newDefRateText.getText()); addedMedia.avgRating =
        * Double.parseDouble(newAvgRateText.getText()); addedMedia.id =
        * Integer.parseInt(newID.getText());
        * 
        * if (user instanceof Manager) { ((Manager) user).addMedia(addedMedia,
        * 1); }
        */// TODO: this was obviously broken (null pointer)
          // TODO:Assigning an id number should not happen -- should have
          // two different add media pages
       if (user instanceof Manager) {
         try {
           ((Manager) user).addMedia(
               new Media(creatorText.getText(), newNameText.getText(), Integer
                   .parseInt(newDurationText.getText()), newGenreText.getText(),
                   Double.parseDouble(newPriceText.getText()), Integer
                       .parseInt(newDefRateText.getText()), Double
                       .parseDouble(newAvgRateText.getText()), 0), Integer
                   .parseInt(addAmountText.getText()));
         } catch (NumberFormatException nfe) {
 
         }
       }
     }
 
     // HANDLE CLEAR BUTTON : addMediaPanel
     if (e.getSource() == clearButton) {
       creatorText.setText("");
       newNameText.setText("");
       newDurationText.setText("");
       newGenreText.setText("");
       newPriceText.setText("");
       newDefRateText.setText("");
       newAvgRateText.setText("");
       // newIDText.setText(""); *****REMOVE
       addTypeText.setText("");
       addAmountText.setText("");
 
     }
 
     // HANDLE BACK BUTTON : addMediaPanel
     if (e.getSource() == back1Button) {
       tabs.addTab("Manager", managerPanel);
       tabs.remove(addNewMediaPanel);
     }
 
     // HANDLE WHEN MANAGER WANTS TO GO TO THE REMOVE MEDIA PANEL
     if (e.getSource() == mngrRemove) {
       tabs.addTab("Remove Media", removeMediaPanel);
       tabs.remove(managerPanel);
     }
 
     // HANDLE CLEAR BUTTON ON REMOVE MEDIA PANEL
     if (e.getSource() == clear2Button) {
       removeText1.setText("");
       removeText2.setText("");
     }
 
     // HANDLE BACK BUTTON ON REMOVE MEDIA PANEL
     if (e.getSource() == back2Button) {
       tabs.addTab("Manager", managerPanel);
       tabs.remove(removeMediaPanel);
     }
 
     // HANDLE REMOVING A MEDIA OBJECT
     if (e.getSource() == removeMediaButton) {
 
       Media rmvMedia = DBIO.getMedia(Integer.parseInt(removeText1.getText()));
 
       if (user instanceof Manager) {
         ((Manager) user).deleteMedia(rmvMedia,
             Integer.parseInt(removeText2.getText()));
 
       }
     }
 
     // HANDLE THE CHECK ITEM SALES BUTTON : mngrPanel
     if (e.getSource() == mngrChkSales) {
       tabs.addTab("Check Sales of an Item", checkItemPanel);
       tabs.remove(managerPanel);
     }
 
     // HANDLE checkItemButton
     if (e.getSource() == checkItemButton) {
       Media checkMedia = DBIO
           .getMedia(Integer.parseInt(checkItemText.getText()));
       double itemSales =0;
       if (user instanceof Manager) {
         if(checkMedia!=null){
           itemSales=((Manager) user).getnumSold(checkMedia) *checkMedia.price;
         }
         JOptionPane.showMessageDialog(checkItemPanel, itemSales,
             "Total Sales of this item: ", 2);
       }
     }
 
     // HANDLE clear3Button
     if (e.getSource() == clear3Button) {
       checkItemText.setText("");
     }
 
     // HANDLE back3Button
     if (e.getSource() == back3Button) {
       tabs.addTab("Manager", managerPanel);
       tabs.remove(checkItemPanel);
     }
 
     // HANDLE mediaType combobox (for search page)
     if (e.getSource() == mediaType && mediaType.getSelectedIndex() == 0
         || mediaType.getSelectedIndex() == 1
         || mediaType.getSelectedIndex() == 2) {
       searchByText.setVisible(true);
       searchType.setVisible(true);
     }
 
     // HANDLE go button (search)
     if (e.getSource() == go) {
       buildView();
       tabs.remove(search);
       tabs.addTab("View", view);
       tabs.addTab("Search", search);
       tabs.validate();
 
     }
 
     // BUILD Purchase Panel
     if (viewButtons != null && viewButtons.containsKey(e.getSource())) {
       mediaObj = viewButtons.get(e.getSource());
       purchase.removeAll();
 
       purchase.add(new JLabel("Creator: " + mediaObj.getCreator()));
       purchase.add(new JLabel("Genre: " + mediaObj.getGenre()));
       purchase.add(new JLabel("Title: " + mediaObj.getName()));
       purchase.add(new JLabel("Duration: " + mediaObj.getDuration()));
       purchase.add(new JLabel("Price: $" + mediaObj.getPrice()));
       purchase.add(new JLabel("Number of Ratings: " + mediaObj.getNumRating()));
       purchase.add(new JLabel("Average Rating: " + mediaObj.getAvgRating()));
       if (user != null) {
         purchase.add(purchaseItem);
       }
 
       purchase.add(goBack);
 
       tabs.removeAll();
       tabs.addTab("Item Information", purchase);
 
       repaint();
 
     }
 
     // Purchase Item functionality
     if (e.getSource() == purchaseItem) {
       int numPurchase = user.purchase(mediaObj, numToBuy);
 
       if (numPurchase > 0) {
         tabs.addTab("Rate", rate);
         tabs.remove(purchase);
         getCustCredit();
 
       } else if (numPurchase == 0 || numPurchase == -1) {
         JOptionPane.showMessageDialog(this,
             "Not enough money, Returning to Search Pane");
         tabs.addTab("Search", search);
         tabs.remove(purchase);
       } else if (numPurchase == -2) {
         JOptionPane.showMessageDialog(this,
             "This item is out of stock, Returning to Search Pane");
         tabs.addTab("Search", search);
         tabs.remove(purchase);
       }
 
     }
     // HANDLE Purchase page's goBack button
     if (e.getSource() == goBack) {
       tabs.addTab("View", view);
       tabs.addTab("Search", search);
       tabs.remove(purchase);
       getCustCredit();
     }
 
     // RATE
     if (e.getSource() == submit) {
       rated = "Rated: " + rating;
       user.rateMedia(mediaObj, rating);
       JOptionPane.showMessageDialog(this, rated);
       tabs.addTab("Thank You!", thankYou);
       tabs.remove(rate);
     }
 
     if (e.getSource() == searchAgain) {
       tabs.addTab("Search", search);
       tabs.remove(thankYou);
 
     }
 
     repaint();
 
   }
 
   public static void main(String[] args) {
     StoreGUI custGUI = new StoreGUI();
     custGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     custGUI.setSize(600, 600);
     custGUI.setVisible(true); // the frame is visible on screen
 
   }
 }
