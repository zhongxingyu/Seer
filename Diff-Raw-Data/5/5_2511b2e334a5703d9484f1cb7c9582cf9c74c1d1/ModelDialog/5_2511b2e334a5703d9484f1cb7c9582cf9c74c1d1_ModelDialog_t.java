 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public abstract class ModelDialog extends JDialog {
   protected JPanel mainPanel;
   protected JTextField name;
   protected JTextArea description;
   protected boolean ok;
 
   public ModelDialog(String titleText, String name, Frame owner) {
     super(owner, titleText, true);
     this.name = new JTextField(30);
     this.mainPanel = new JPanel();
     this.name.setText(name);
     this.description = new JTextArea(2, 30);
     buildBase();
   }
 
   private void buildBase() {
     mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 
     mainPanel.add(Box.createVerticalGlue());
 
     JPanel namePanel = new JPanel();
     namePanel.setAlignmentY(Box.LEFT_ALIGNMENT);
     namePanel.add(new JLabel("Name: "));
     namePanel.add(name);
     mainPanel.add(namePanel);
 
     mainPanel.add(Box.createVerticalGlue());
 
     JPanel descPanel = new JPanel();
     JLabel desc = new JLabel("Description: ");
     descPanel.add(desc);
    JScrollPane descPane = new JScrollPane(description);
    descPane.setPreferredSize(new Dimension(300, 100));
    description.setLineWrap(true);
    descPanel.add(descPane);
     mainPanel.add(descPanel);
 
     mainPanel.add(Box.createVerticalGlue());
 
     this.add(mainPanel);
   }
 
   protected abstract void build();
 
   protected void addButtons() {
     JPanel buttonsPanel = new JPanel();
     buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
     buttonsPanel.add(Box.createHorizontalGlue());
     JButton ok = new JButton("OK");
     ok.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         setOk(true);
         setVisible(false);
       }
     });
     buttonsPanel.add(ok);
     buttonsPanel.add(Box.createHorizontalGlue());
     JButton cancel = new JButton("Cancel");
     cancel.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         setOk(false);
         setVisible(false);
       }
     });
     buttonsPanel.add(cancel);
     buttonsPanel.add(Box.createHorizontalGlue());
     mainPanel.add(buttonsPanel);
 
     mainPanel.add(Box.createVerticalGlue());
 
   }
 
   public void showDialog() {
     pack();
     setVisible(true);
   }
 
   public String getName() {
     return name.getText();
   }
 
   public String getDescription() {
     return description.getText();
   }
   
   public boolean getOk() {
     return ok;
   }
 
   private void setOk(boolean b) {
     ok = b;
   }
 }
