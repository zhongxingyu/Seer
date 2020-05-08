 package project.gui;
 
 import project.Controller;
 import project.gui.util.DollarAmountFormatter;
 import project.model.Account;
 import project.model.InsufficientFundsException;
 import project.model.InvalidInputException;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Map;
 
 public final class TellerAccountTab extends JPanel {
     private final Controller controller;
 
     private final Account account;
     
     private boolean feeIncurred = false; //becomes true even if fee was waived
 
     public boolean isFeeIncurred() {
         return feeIncurred;
     }
     
     public void incurFee() {
         feeIncurred = true;
         try {
             controller.incurTellerFees(account);
         } catch (InvalidInputException ex) {
             controller.handleException(this, ex);
         }
         infoPanel.update();
     }
     
     private final AccountInfoPanel infoPanel;
 
     public TellerAccountTab(Controller controller, Account account) {
         this.controller = controller;
         this.account = account;
         infoPanel = new AccountInfoPanel(controller, account);
         setName(account.toString());
         initComponents();
     }
 
     private void initComponents() {
         setName(account.toString());
         setLayout(new BorderLayout());
         add(infoPanel, BorderLayout.NORTH);
         add(new JSeparator(), BorderLayout.CENTER);
 
         JPanel buttonPanel = new JPanel(new GridLayout(3, 2));
 
         JButton depositButton = new JButton("Deposit");
         depositButton.setEnabled(account.getType() != Account.Type.CD);
         depositButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String amountStr = JOptionPane.showInputDialog(TellerAccountTab.this, "Deposit amount:", "Deposit",
                         JOptionPane.QUESTION_MESSAGE);
                 if (amountStr == null) {
                     return;
                 }
                 try {
                     BigDecimal amount = new DollarAmountFormatter().stringToValue(amountStr);
                     account.deposit(amount);
                     infoPanel.update();
                 } catch (ParseException px) {
                     controller.handleException(TellerAccountTab.this, px);
                 } catch (InvalidInputException iix) {
                     controller.handleException(TellerAccountTab.this, iix);
                 }
             }
         });
         buttonPanel.add(depositButton);
 
         JButton withdrawButton = new JButton("Withdraw");
         withdrawButton.setEnabled(account.getType() != Account.Type.LOAN);
         withdrawButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String amountStr = JOptionPane.showInputDialog(TellerAccountTab.this, "Withdrawal amount:", "Withdraw",
                         JOptionPane.QUESTION_MESSAGE);
                 if (amountStr == null) {
                     return;
                 }
                 try {
                     BigDecimal amount = new DollarAmountFormatter().stringToValue(amountStr);
                     account.withdraw(amount);
                     infoPanel.update();
                 } catch (ParseException px) {
                     controller.handleException(TellerAccountTab.this, px);
                 } catch (InvalidInputException iix) {
                     controller.handleException(TellerAccountTab.this, iix);
                 } catch (InsufficientFundsException ifx) {
                     controller.handleException(TellerAccountTab.this, ifx);
                 }
             }
         });
         buttonPanel.add(withdrawButton);
 
         JButton repeatingDepositButton = new JButton("Add repeating deposit");
         repeatingDepositButton.setEnabled(account.getType() != Account.Type.CD);
         repeatingDepositButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String amountStr = JOptionPane.showInputDialog(TellerAccountTab.this, "Repeating deposit amount:",
                         "Repeating deposit", JOptionPane.QUESTION_MESSAGE);
                 if (amountStr == null) {
                     return;
                 }
                 String description = JOptionPane.showInputDialog(TellerAccountTab.this, "Description:", "Repeating deposit",
                         JOptionPane.QUESTION_MESSAGE);
                 if (description == null) {
                     return;
                 }
                 try {
                     BigDecimal amount = new DollarAmountFormatter().stringToValue(amountStr);
                     account.addRepeatingDeposit(description, amount);
                     String infoMessage = String.format("Added repeating deposit of amount %s with description \"%s\".",
                             new DollarAmountFormatter().valueToString(amount), description);
                     JOptionPane.showMessageDialog(TellerAccountTab.this, infoMessage, "Repeating deposit", JOptionPane.INFORMATION_MESSAGE);
                 } catch (ParseException px) {
                     controller.handleException(TellerAccountTab.this, px);
                 } catch (InvalidInputException iix) {
                     controller.handleException(TellerAccountTab.this, iix);
                 }
             }
         });
         buttonPanel.add(repeatingDepositButton);
 
         JButton repeatingWithdrawalButton = new JButton("Add repeating withdrawal");
         repeatingWithdrawalButton.setEnabled(account.getType() != Account.Type.CD && account.getType() != Account.Type.LOAN);
         repeatingWithdrawalButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String amountStr = JOptionPane.showInputDialog(TellerAccountTab.this, "Repeating withdrawal amount:",
                         "Repeating withdrawal", JOptionPane.QUESTION_MESSAGE);
                 if (amountStr == null) {
                     return;
                 }
                 String description = JOptionPane.showInputDialog(TellerAccountTab.this, "Description:", "Repeating withdrawal",
                         JOptionPane.QUESTION_MESSAGE);
                 if (description == null) {
                     return;
                 }
                 try {
                     BigDecimal amount = new DollarAmountFormatter().stringToValue(amountStr);
                     account.addRepeatingWithdrawal(description, amount);
                     String infoMessage = String.format("Added repeating withdrawal of amount %s with description \"%s\".",
                             new DollarAmountFormatter().valueToString(amount), description);
                     JOptionPane.showMessageDialog(TellerAccountTab.this, infoMessage, "Repeating withdrawal", JOptionPane.INFORMATION_MESSAGE);
                 } catch (ParseException px) {
                     controller.handleException(TellerAccountTab.this, px);
                 } catch (InvalidInputException iix) {
                     controller.handleException(TellerAccountTab.this, iix);
                 }
             }
         });
         buttonPanel.add(repeatingWithdrawalButton);
 
         JButton endRepeatingButton = new JButton("End repeating payment");
         endRepeatingButton.setEnabled(account.getType() != Account.Type.CD);
         endRepeatingButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Map<String, BigDecimal> repeatingPayments = new HashMap<String, BigDecimal>(account.getRepeatingPayments());
                 String description = (String) JOptionPane.showInputDialog(TellerAccountTab.this, "Which one?",
                         "End automatic deposit", JOptionPane.QUESTION_MESSAGE, null, repeatingPayments.keySet().toArray(),
                         repeatingPayments.keySet().iterator().next());
                 if (description == null) {
                     return;
                 }
                 account.removeRepeatingPayment(description);
             }
         });
         buttonPanel.add(endRepeatingButton);
 
         add(buttonPanel, BorderLayout.SOUTH);
     }
 }
