 package com.github.krassekoder.mm13client.gui;
 
 import com.github.krassekoder.mm13client.network.Packet;
 import com.github.krassekoder.mm13client.network.Packet2Purchase;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QHeaderView;
 import com.trolltech.qt.gui.QIcon;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QMessageBox;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QTableWidget;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 /**
  * The "PayWidget" offers various non-cash payment options and also conventional
  * cash payment. It is possible to enter the amount of money recieved and there
  * is a button 'Pay' as well as a button 'Pay and Print'.
  */
 public class PayWidget extends QWidget {
 
     public static PayWidget instance;
 
     private QHBoxLayout hLa1,hLa2,hLa3,hLa4,hLa5,hLa6;
     private QPushButton print,esc,pay,creditcard,voucher,plasticcard, cash;
     private QLineEdit money;
     private QVBoxLayout vLa1,vLa2;
     private QLabel mLabel, pLabel;
     public double change;
     public String moneySafe;
     public double giftMoney;
     private QTableWidget list;
     private QLabel amount;
 
     public PayWidget(QWidget qw) {
         super(qw);
         instance = this;
         setupUi();
     }
     /**
      * This method sets up the User Interface including Layouts, Forms,
      * Buttons, etc.
      */
      private void setupUi()  {
         setLayout(vLa1= new QVBoxLayout());
         vLa1.addLayout(hLa5 = new QHBoxLayout(this));
         hLa5.addWidget(list = new QTableWidget(0, 1));
         list.verticalHeader().setVisible(false);
         list.verticalHeader().setResizeMode(QHeaderView.ResizeMode.ResizeToContents);
         list.horizontalHeader().setVisible(false);
         list.horizontalHeader().setStretchLastSection(true);
         vLa1.addLayout(hLa6= new QHBoxLayout(this));
         hLa6.addWidget(amount = new QLabel(tr("Price: 0.00$")));
         vLa1.addLayout(hLa2= new QHBoxLayout(this));
         hLa2.addWidget(creditcard= new QPushButton(tr("Mastercard")));
         creditcard.setIcon(new QIcon("classpath:com/github/krassekoder/mastercard-straight-64px.png"));
         hLa2.addWidget(voucher= new QPushButton(tr("Voucher")));
         voucher.setIcon(new QIcon("classpath:com/github/krassekoder/gift_alt.png"));
         hLa2.addWidget(plasticcard= new QPushButton(tr("American Express")));
         plasticcard.setIcon(new QIcon("classpath:com/github/krassekoder/amex.png"));
         hLa2.addWidget(cash= new QPushButton(tr("Deposit")));
         cash.setIcon(new QIcon("classpath:com/github/krassekoder/cash.png"));
         vLa1.addLayout(hLa1= new QHBoxLayout(this));
         hLa1.addWidget(mLabel= new QLabel(tr("&Money:"),this));
         hLa1.addWidget(money= new QLineEdit(this));
         mLabel.setBuddy(money);
         vLa1.addLayout(hLa3= new QHBoxLayout(this));
         hLa3.addWidget(print= new QPushButton(tr("P&rint && Pay"),this));
         print.setIcon(new QIcon("classpath:com/github/krassekoder/document-print.png"));
         hLa3.addWidget(pay= new QPushButton(tr("&Pay"),this));
         pay.setIcon(new QIcon("classpath:com/github/krassekoder/application-certificate.png"));
         hLa3.addWidget(esc= new QPushButton(tr("&Cancel"),this));
         esc.setIcon(new QIcon("classpath:com/github/krassekoder/process-stop.png"));
         print.clicked.connect(this,"enableChangeDialog()");
         pay.clicked.connect(this,"enableChangeDialog())");
         esc.clicked.connect(this,"EscapeMessage()");
         money.editingFinished.connect(this,"getGivenMoney()");
         creditcard.clicked.connect(this,"enableCard()");
         plasticcard.clicked.connect(this,"enableCard()");
         cash.clicked.connect(this,"enableCard()");
         voucher.clicked.connect(this,"GiftCard()");
     }
 
      /**
       * This method calls 'ChangeToTeller()' in mm13client.gui.MainWindow in
       * order to change from the Pay-tab to the Teller-tab when 'Pay' or 'Print'
       * is pressed.
       */
      public void printPrice()
      {
          amount.setText("Price: " + String.format("%1$.2f", MainWindow.instance.giveValue()));
      }
      //Method to enable the ChangeDialog
      private void enableChangeDialog() throws Packet.InvalidPacketException{
          if(money.hasAcceptableInput()&&money.isModified())
          {
              setMoneyFalse();
              getChange();
              Packet2Purchase.Purchase p = Packet2Purchase.Purchase.cashPurchase(money.text());
              for(int i = 0; i < list.rowCount(); i++) {
                  TellerWidget.ProductDisplay d = (TellerWidget.ProductDisplay)list.cellWidget(i, 0);
                  p.addItem(d.getCount(), d.getId());
              }
             //MainWindow.instance.enableChangeDialog(p.submit());
             //TempFix START
             p.submit();
             MainWindow.instance.enableChangeDialog(change);
             //TempFix END
          }
 
          else if(!money.isModified()&&!MainWindow.instance.ChangeIsVisible())
          {
              QMessageBox.information(this, tr("Enter money"), tr("Please type in the amount of money you recieved!"));
              setMoneyFalse();
          }
 
         }
 
      public void setMoneyFalse()
      {
          money.setModified(false);
      }
 
      public void setMoneyTrue()
      {
          money.setModified(true);
      }
 
      //Enables the EscapeMessage
      private void EscapeMessage(){
        if(QMessageBox.question(this, tr("Cancel order"), tr("Do you really want to cancel the order?"),
                              new QMessageBox.StandardButtons(QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No))
                 == QMessageBox.StandardButton.Yes) {
             MainWindow.instance.changeToTeller();
         }
      }
 
      //Saves the given Money in moneySafe
      public void getGivenMoney(){
          moneySafe= money.text();
      }
 
      //Resets the Change
      public void resetChange()
      {
          change=0;
      }
 
      public void resetMoneySafe()
      {
          moneySafe=null;
      }
 
      //Calculates the Change
      private void getChange(){
         double speicher1;
         speicher1 = Double.parseDouble(moneySafe);
         change = speicher1-MainWindow.instance.giveValue()+giftMoney;
         change = Math.round( change * 100d ) / 100d;
     }
 
      //Returns the Charge
      public double giveChange()
      {
          getChange();
          return change;
 
      }
 
      //clears the money 'LineEdit'
      public void clearMoney()
      {
          money.clear();
      }
 
      /*
       * Resets the purchase.
       */
      /*package*/ void resetList() {
          while(list.rowCount() > 0)
              list.removeRow(0);
      }
 
      /*
       * Inserts a copy of the product item in the list.
       */
      /*package*/ void insert(TellerWidget.ProductDisplay item) {
          int row = list.rowCount();
          list.insertRow(row);
          list.setCellWidget(row, 0, item.clone(list));
      }
      //Shows the NoCardReader Mesage
      public void enableCard(){
         QMessageBox.information(this, tr("Warning!"), tr("No card reader connected!"));
       }
      //Resets the giftMoney
      public void resetGiftMoney(){
          giftMoney=0;
      }
      //Gives back the Money when Gift Card is deducted
      public String getMoneyforGift(){
         if(money.hasAcceptableInput()){
            return money.text();
      }
         else{
             return "";
         }
      }
      //Opens the GiftCard Box
      private void GiftCard(){
        if(QMessageBox.question(this, tr("Add Gift Card"), tr("To add a new Gift Card insert the amount of the Gift in the 'MoneyField'.\n"
                + "Do you really want to add a "+Double.parseDouble(getMoneyforGift())+"$ Gift Card?"),
                              new QMessageBox.StandardButtons(QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No))
                 == QMessageBox.StandardButton.Yes) {
 
         giftMoney = Double.parseDouble(money.text());
         double amounttest;
         amounttest=Math.round( (MainWindow.instance.giveValue()-giftMoney) * 100d ) / 100d;
         amount.setText("Price: "+Double.toString(amounttest)+"$");
         }
      }
 }
