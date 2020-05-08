 import com.sun.deploy.panel.NumberDocument;
 import org.jdesktop.swingx.*;
 
 import javax.swing.*;
 import javax.swing.text.JTextComponent;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Calendar;
 import java.util.Locale;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Barak
  * Date: 03/10/13
  * Time: 09:37
  * To change this template use File | Settings | File Templates.
  */
 public class JobRecordDialog extends JDialog
 {
     public static Dimension DEFAULT_DATE_SIZE = new Dimension(200, 25);
     public static Dimension DEFAULT_PRICE_SIZE = new Dimension(150, 25);
     public static Dimension DEFAULT_LABEL_SIZE = new Dimension(50, 25);
     public static Dimension DEFAULT_BUTTON_SIZE = new Dimension(75, 25);
 
     public static String DEFAULT_JOB_DESC_TEXT = "עבודה שנעשתה..";
     public static String DEFAULT_REMARKS_TEXT = "הערות..";
 
     private JXLabel dateLabel = new JXLabel("בתאריך:");
     private JXDatePicker datePicker = new JXDatePicker(Calendar.getInstance().getTime(), Locale.getDefault());
     private JXLabel customerLabel = new JXLabel("לקוח:");
     private JXComboBox customerCombo = new JXComboBox();
     private JXButton addNewCustomerButton = new JXButton("הוספה");
     private JXButton updateCustomerButton = new JXButton("עדכון");
     private JTextArea jobDescField = new JTextArea(DEFAULT_JOB_DESC_TEXT);
     private JXLabel priceLabel = new JXLabel("מחיר:");
    private JTextField priceField = new JTextField();
     private JTextArea remarksField = new JTextArea(DEFAULT_REMARKS_TEXT);
     private JXButton feedButton = new JXButton("הזן");
     private JXButton finishButton = new JXButton("סיים");
 
     private Job returnedJob = null;
     private boolean isFinished = false;
     private Job jobToUpdate = null;
 
     public JobRecordDialog(Frame owner, Job jobToUpdate)
     {
         super(owner, true);
         this.setTitle("הוספת רשומה חדשה");
         Utils.setSoftSize(this, new Dimension(450, 350));
         this.setLocationRelativeTo(owner);
         this.jobToUpdate = jobToUpdate;
 
         try
         {
             initComponents();
         } catch (Exception e)
         {
             Utils.showExceptionMsg(this, e);
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             this.dispose();
         }
 
         JXPanel datePanel = new JXPanel();
         Utils.setLineLayout(datePanel);
         Utils.addSmallRigid(datePanel);
         datePanel.add(dateLabel);
         Utils.addStandardRigid(datePanel);
         datePanel.add(datePicker);
         datePanel.add(Box.createHorizontalGlue());
 
         JXPanel customerPanel = new JXPanel();
         Utils.setLineLayout(customerPanel);
         Utils.addSmallRigid(customerPanel);
         customerPanel.add(customerLabel);
         Utils.addStandardRigid(customerPanel);
         customerPanel.add(customerCombo);
         Utils.addStandardRigid(customerPanel);
         customerPanel.add(addNewCustomerButton);
         Utils.addStandardRigid(customerPanel);
         customerPanel.add(updateCustomerButton);
         Utils.addStandardRigid(customerPanel);
         customerPanel.add(Box.createHorizontalGlue());
 
         JXPanel workPanel = new JXPanel();
         Utils.setLineLayout(workPanel);
         Utils.addSmallRigid(workPanel);
         workPanel.add(jobDescField);
         Utils.addSmallRigid(workPanel);
         workPanel.add(Box.createHorizontalGlue());
 
         JXPanel pricePanel = new JXPanel();
         Utils.setLineLayout(pricePanel);
         Utils.addSmallRigid(pricePanel);
         pricePanel.add(priceLabel);
         Utils.addStandardRigid(pricePanel);
         pricePanel.add(priceField);
         Utils.addStandardRigid(pricePanel);
         pricePanel.add(new JXLabel("ש\"ח"));
         pricePanel.add(Box.createHorizontalGlue());
 
         JXPanel remarksPanel = new JXPanel();
         Utils.setLineLayout(remarksPanel);
         Utils.addSmallRigid(remarksPanel);
         remarksPanel.add(remarksField);
         Utils.addSmallRigid(remarksPanel);
         remarksPanel.add(Box.createHorizontalGlue());
 
         JXPanel mainPanel = new JXPanel();
         Utils.setPageLayout(mainPanel);
         Utils.addStandardRigid(mainPanel);
         mainPanel.add(datePanel);
         Utils.addStandardRigid(mainPanel);
         mainPanel.add(customerPanel);
         Utils.addStandardRigid(mainPanel);
         mainPanel.add(workPanel);
         Utils.addStandardRigid(mainPanel);
         mainPanel.add(pricePanel);
         Utils.addStandardRigid(mainPanel);
         mainPanel.add(remarksPanel);
         Utils.addStandardRigid(mainPanel);
 
         JXPanel buttonPanel = new JXPanel();
         buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 5));
         buttonPanel.add(feedButton);
         buttonPanel.add(finishButton);
 
         this.setLayout(new BorderLayout());
         this.getContentPane().add(mainPanel, BorderLayout.CENTER);
         this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
         this.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         this.setVisible(true);
     }
 
     private void initComponents() throws Exception
     {
         Dimension comboDimension = new Dimension(DEFAULT_DATE_SIZE);
         comboDimension.width = comboDimension.width - 10;
         Utils.setSoftSize(customerCombo, comboDimension);
         Utils.setSoftSize(datePicker, DEFAULT_DATE_SIZE);
         Utils.setHardSize(priceField, DEFAULT_PRICE_SIZE);
         Utils.setSoftSize(feedButton, WorkLogScr.DEFAULT_BUTTON_SIZE);
         Utils.setSoftSize(finishButton, WorkLogScr.DEFAULT_BUTTON_SIZE);
         Utils.setHardSize(dateLabel, DEFAULT_LABEL_SIZE);
         Utils.setHardSize(customerLabel, DEFAULT_LABEL_SIZE);
         Utils.setHardSize(priceLabel, DEFAULT_LABEL_SIZE);
         Utils.setSoftSize(addNewCustomerButton, DEFAULT_BUTTON_SIZE);
         Utils.setSoftSize(updateCustomerButton, DEFAULT_BUTTON_SIZE);
         datePicker.setFont(WorkLogScr.DEFAULT_TEXT_FONT);
 
         priceField.setDocument(new NumberDocument());
        priceField.setText("0");
         jobDescField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
         remarksField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
         addNewCustomerButton.setFocusable(false);
         addNewCustomerButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String name = Utils.showInputMsg(JobRecordDialog.this, "שם לקוח: ", "הוספת לקוח חדש");
                 if (name != null && !name.isEmpty())
                 {
                     try
                     {
                         if (DBManager.getSingleton().getCustomerByName(name) != null)
                         {
                             throw new Exception("הלקוח כבר קיים!");
                         }
 
                         DBManager.getSingleton().addCustomer(new Customer(name));
                         initCustomerComboFromDB();
                     }
                     catch (Exception e1)
                     {
                         Utils.showExceptionMsg(JobRecordDialog.this, e1);
                         e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         });
 
         updateCustomerButton.setFocusable(false);
         updateCustomerButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String name = Utils.showInputMsg(JobRecordDialog.this, "שם הלקוח החדש: ", "עדכון לקוח קיים");
                 if (name != null && !name.isEmpty())
                 {
                     try
                     {
                         for (int i = 0; i < customerCombo.getModel().getSize(); i++)
                         {
                             Customer currCustomer = ((DefaultComboBoxModel<Customer>)customerCombo.getModel()).getElementAt(i);
                             if (name.trim().equals(currCustomer.getName().trim()))
                             {
                                 Utils.showErrorMsg(JobRecordDialog.this, "לקוח בשם זה כבר קיים!");
                                 return;
                             }
                         }
 
                         Customer customer = (Customer)customerCombo.getSelectedItem();
                         customer.setName(name);
                         DBManager.getSingleton().updateCustomer(customer);
                         initCustomerComboFromDB();
                     }
                     catch (Exception e1)
                     {
                         Utils.showExceptionMsg(JobRecordDialog.this, e1);
                         e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         });
 
         jobDescField.addFocusListener(getFocusAdapter(jobDescField, DEFAULT_JOB_DESC_TEXT));
         jobDescField.addKeyListener(new KeyAdapter()
         {
             @Override
             public void keyTyped(KeyEvent e)
             {
                 if (e.getKeyChar() == KeyEvent.VK_TAB)
                 {
                     priceField.requestFocus();
                     e.consume();
                 }
             }
         });
 
         priceField.addFocusListener(getFocusAdapter(priceField, "0"));
 
         remarksField.addFocusListener(getFocusAdapter(remarksField, DEFAULT_REMARKS_TEXT));
         remarksField.addKeyListener(new KeyAdapter()
         {
             @Override
             public void keyTyped(KeyEvent e)
             {
                 if (e.getKeyChar() == KeyEvent.VK_TAB)
                 {
                     feedButton.requestFocus();
                     e.consume();
                 }
             }
         });
 
         KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher()
         {
             @Override
             public boolean dispatchKeyEvent(KeyEvent e)
             {
                 if (e.getKeyChar() == KeyEvent.VK_ESCAPE)
                 {
                     JobRecordDialog.this.dispose();
                     e.consume();
                 }
 
                 return false;
             }
         });
 
        getRootPane().setDefaultButton(jobToUpdate != null ? finishButton : feedButton);
         feedButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 feedJobIntoDB();
             }
         });
 
         finishButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (jobToUpdate != null)
                 {
                     returnedJob = new Job(jobToUpdate.getId(), datePicker.getDate(), (Customer) customerCombo.getSelectedItem(),
                             jobDescField.getText(), Double.parseDouble(priceField.getText()), remarksField.getText());
                     returnedJob.setUpdated(true);
                 }
 
                 isFinished = true;
                 JobRecordDialog.this.dispose();
             }
         });
 
         initCustomerComboFromDB();
 
         if (jobToUpdate != null)
         {
             this.setTitle("עדכון רשומה קיימת");
 
             this.datePicker.setDate(jobToUpdate.getJobDate());
             this.customerCombo.setSelectedItem(jobToUpdate.getCustomer());
             this.jobDescField.setText(jobToUpdate.getJobDescription());
             this.priceField.setText((int)jobToUpdate.getPrice() + "");
             this.remarksField.setText(jobToUpdate.getRemarks());
 
             feedButton.setVisible(false);
         }
     }
 
     private void feedJobIntoDB()
     {
         if (priceField.getText().isEmpty() || customerCombo.getSelectedIndex() == -1)
         {
             Utils.showErrorMsg(JobRecordDialog.this, "יש להזין ערכים בכל השדות!");
             return;
         }
 
         Job job = new Job(datePicker.getDate(), (Customer) customerCombo.getSelectedItem(),
                 jobDescField.getText(), Double.parseDouble(priceField.getText()),
                 remarksField.getText().equals(DEFAULT_REMARKS_TEXT) ? "" : remarksField.getText());
 
         try
         {
             job.setId(DBManager.getSingleton().addJob(job));
         }
         catch (Exception e1)
         {
             Utils.showExceptionMsg(JobRecordDialog.this, e1);
             e1.printStackTrace();
         }
 
         JobRecordDialog.this.returnedJob = job;
         JobRecordDialog.this.dispose();
     }
 
     private FocusAdapter getFocusAdapter(final JTextComponent textComponent, final String defaultText)
     {
         return new FocusAdapter()
         {
             @Override
             public void focusGained(FocusEvent e)
             {
                 if (jobToUpdate == null) textComponent.selectAll();
             }
 
             @Override
             public void focusLost(FocusEvent e)
             {
                 if (textComponent.getText().trim().equals(""))
                 {
                     textComponent.setText(defaultText);
                 }
             }
         };
     }
 
     private void initCustomerComboFromDB() throws Exception
     {
         customerCombo.setModel(new DefaultComboBoxModel(DBManager.getSingleton().getCustomers().toArray()));
     }
 
     public Job getReturnedJob()
     {
         return this.returnedJob;
     }
 
     public boolean isFinished()
     {
         return isFinished;
     }
 }
