 package rentalcar;
 
 import java.awt.Color;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import core.DBConnection;
 import core.CreditCard.CreditCard;
 import core.DrivingPlan.DrivingPlan;
 import core.DrivingPlan.PlanType;
 import core.User.MemberUser;
 import core.User.UserDao;
 
 /**
  * @author Sahil Gupta This class displays the Personal Information of the user.
  */
 public class MemberPInfoPanel extends JPanel {
     private static final long serialVersionUID = 1L;
     final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
     private MemberUser member;
     DBConnection connection = new DBConnection();
 
     JLabel personalInformation, generalInformation, membershipInformation,
     paymentInformation;
     JLabel firstName, middleInitial, lastName, emailAddress, phoneNumber,
     address;
     JTextField firstNameField, middleInitialField, lastNameField,
     emailAddressField, phoneNumberField, addressField;
     JLabel choosePlan;
     private JRadioButton occasionalDriving, frequentDriving, dailyDriving;
     ButtonGroup group = new ButtonGroup();
     private String membershipHeadingString = "Choose A Plan";
     private String occasionalDrivingString = "Occasional Driving";
     private String frequentDrivingString = "Frequent Driving";
     private String dailyDrivingString = "Daily Driving";
 
     JLabel nameCard, cardNumber, cVV, expiryDate, billingAddress;
     JTextField nameCardField, cardNumberField, cVVField, expiryDateField,
     billingAddressField;
 
     JButton viewPlanDetail, done;
     String username, password;
 
     public MemberPInfoPanel(MemberUser member) {
         this.member = member;
 
         username = member.getUsername();
         password = member.getPassword();
 
         this.setBackground(Color.green);
         this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
         setBounds(300, 0, screenSize.width/2, screenSize.height-40);
 
         personalInformation = new JLabel("Personal Information");
         personalInformation.setFont(new Font("Helvetica", Font.BOLD, 70));
 
         generalInformation = new JLabel("General Information");
         generalInformation.setFont(new Font("Helvetica", Font.BOLD, 40));
 
         firstName = new JLabel("First Name");
         firstNameField = new JTextField(30);		
 
         middleInitial = new JLabel("Middle Initial");
         middleInitialField = new JTextField(1);
 
         lastName = new JLabel("Last Name");
         lastNameField = new JTextField(30);
 
         emailAddress = new JLabel("Email Address");
         emailAddressField = new JTextField(50);
 
         phoneNumber = new JLabel("Phone Number");
         phoneNumberField = new JTextField(15);
 
         address = new JLabel("Address");
         addressField = new JTextField(100);
 
         membershipInformation = new JLabel("Membership Information");
         membershipInformation.setFont(new Font("Helvetica", Font.BOLD, 40));
 
         choosePlan = new JLabel(membershipHeadingString);
         choosePlan.setFont(new Font("Helvetica", Font.BOLD, 20));
 
         viewPlanDetail = new JButton("View Plan Detail");
         viewPlanDetail.addActionListener(new ViewPlanDetailsButtonListener());
 
         occasionalDriving = new JRadioButton(occasionalDrivingString);
         occasionalDriving.setBackground(Color.green);
         frequentDriving = new JRadioButton(frequentDrivingString);
         frequentDriving.setBackground(Color.green);
         dailyDriving = new JRadioButton(dailyDrivingString);
         dailyDriving.setBackground(Color.green);
 
         group.add(occasionalDriving);
         group.add(frequentDriving);
         group.add(dailyDriving);
 
         paymentInformation = new JLabel("Payment Information");
         paymentInformation.setFont(new Font("Helvetica", Font.BOLD, 40));
 
         nameCard = new JLabel("Name on the card");
         nameCardField = new JTextField(50);
 
         cardNumber = new JLabel("Card Number");
         cardNumberField = new JTextField(20);
 
         cVV = new JLabel("CVV");
         cVVField = new JTextField(3);
 
         expiryDate = new JLabel("Expiry Date (yyyy-mm-dd)");
         expiryDateField = new JTextField(20);
 
         billingAddress = new JLabel("Billing Address");
         billingAddressField = new JTextField(100);
 
         done = new JButton("Done");
         done.addActionListener(new DoneButtonListener());
 
         if(member.getFirstName() != null)		
             firstNameField.setText(member.getFirstName());
         if (member.getMiddleInit() != null)
             middleInitialField.setText(member.getMiddleInit());
         if(member.getLastName() != null)
             lastNameField.setText(member.getLastName());
         if(member.getEmail() != null)
             emailAddressField.setText(member.getEmail());
         if(member.getPhone() != null)
             phoneNumberField.setText(member.getPhone());
         if(member.getAddress() != null)
             addressField.setText(member.getAddress());
         if(member.getDrivingPlan() != null){
             if (member.getDrivingPlan().getName().equals("Occasional Driving")) {
                 occasionalDriving.setSelected(true);
             } else if (member.getDrivingPlan().getName().equals("Frequent Driving")) {
                 frequentDriving.setSelected(true);
             } else if (member.getDrivingPlan().getName().equals("Daily Driving")) {
                 dailyDriving.setSelected(true);
             }
         }
         if(member.getCreditCard().getNameOnCard() != null)
             nameCardField.setText(member.getCreditCard().getNameOnCard());
         if(""+member.getCreditCard().getCardNumber() != "")
             cardNumberField.setText(""+member.getCreditCard().getCardNumber());
         if(""+member.getCreditCard().getCvv() != "")
             cVVField.setText(""+member.getCreditCard().getCvv());
         if(member.getCreditCard().getExpiryDate().toString() != null)
             expiryDateField.setText(member.getCreditCard().getExpiryDate().toString());
         if(member.getCreditCard().getBillingAddress() != null)
             billingAddressField.setText(member.getCreditCard().getBillingAddress());
 
         this.add(personalInformation);
         this.add(generalInformation);
         this.add(firstName);
         this.add(firstNameField);
         this.add(middleInitial);
         this.add(middleInitialField);
         this.add(lastName);
         this.add(lastNameField);
         this.add(emailAddress);
         this.add(emailAddressField);
         this.add(phoneNumber);
         this.add(phoneNumberField);
         this.add(address);
         this.add(addressField);
         this.add(membershipInformation);
         this.add(choosePlan);
         this.add(viewPlanDetail);
         this.add(occasionalDriving);
         this.add(frequentDriving);
         this.add(dailyDriving);
         this.add(paymentInformation);
         this.add(nameCard);
         this.add(nameCardField);
         this.add(cardNumber);
         this.add(cardNumberField);
         this.add(cVV);
         this.add(cVVField);
         this.add(expiryDate);
         this.add(expiryDateField);
         this.add(billingAddress);
         this.add(billingAddressField);
         this.add(done);
     }
 
     private class ViewPlanDetailsButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             JDialog regDialog = new JDialog(new JFrame(), "Driving Plans", Dialog.ModalityType.APPLICATION_MODAL);
             PlanDetailsPanel pDetails = new PlanDetailsPanel(member);
             regDialog.setContentPane(pDetails);
             regDialog.setBounds(regDialog.getContentPane().getBounds());
             regDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
             regDialog.setVisible(true);
         }
     }
 
     private class DoneButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             // New values which will be fetched from the textFields
             member.setFirstName(firstNameField.getText());
             member.setMiddleInit(middleInitialField.getText());
             member.setLastName(lastNameField.getText());
             member.setEmail(emailAddressField.getText());
             member.setPhone(phoneNumberField.getText());
             member.setAddress(addressField.getText());
             if (occasionalDriving.isSelected()) {
                 member.setDrivingPlan(new DrivingPlan(PlanType.OCCASIONAL));
             } else if (frequentDriving.isSelected()) {
                 member.setDrivingPlan(new DrivingPlan(PlanType.FREQUENT));
             } else if (dailyDriving.isSelected()) {
                 member.setDrivingPlan(new DrivingPlan(PlanType.DAILY));
             }
             CreditCard creditCard = new CreditCard();
             creditCard.setNameOnCard(nameCardField.getText());
             creditCard.setBillingAddress(billingAddressField.getText());
             creditCard.setExpiryDate(expiryDateField.getText());
            creditCard.setCardNumber(Integer.parseInt(cardNumberField.getText()));
             creditCard.setCvv(Integer.parseInt(cVVField.getText()));
             member.setCreditCard(creditCard);
             // Update operations if anything changes
             UserDao userDao = new UserDao();
             MemberUser mem = userDao.editMemberInfo(member);
             if(mem != null) {
                 JFrame mainFrame = MainFrame.getMain();
                 MemberHomePanel panel = new MemberHomePanel(mem);
                 mainFrame.setContentPane(panel);
                 mainFrame.setBounds(mainFrame.getContentPane().getBounds());
                 mainFrame.setVisible(true);
                 mainFrame.repaint();
             }
             else {
                 JOptionPane.showMessageDialog(new JFrame(),
                         "Error in correcting, please try again",
                         "Inane error",
                         JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 }
