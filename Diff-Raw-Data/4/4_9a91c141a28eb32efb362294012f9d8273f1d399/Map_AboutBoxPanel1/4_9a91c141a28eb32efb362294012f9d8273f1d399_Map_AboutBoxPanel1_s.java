 package stellar.dialog;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import java.awt.GridBagLayout;
 import javax.swing.border.Border;
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.border.CompoundBorder;
 
 public class Map_AboutBoxPanel1 extends JPanel 
 {
     private Border border1 = BorderFactory.createEtchedBorder ();
     private Border border2 = BorderFactory.createEmptyBorder(15, 15, 15, 15);
     private Border border3 = new CompoundBorder (border1, border2);
     private GridBagLayout layoutMain = new GridBagLayout();
     private JLabel labelCompany = new JLabel();
     private JLabel labelCopyright = new JLabel();
     private JLabel labelAuthor = new JLabel();
     private JLabel labelTitle = new JLabel();
     private BoxLayout layout1 = new BoxLayout (this, BoxLayout.PAGE_AXIS);
     public Map_AboutBoxPanel1()
     {
         try
         {
             jbInit();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
 
     }
 
     private void jbInit() throws Exception
     {
         this.setLayout(layout1);
         this.setBorder(border3);
         labelTitle.setText("Traveller Stellar Cartrographer");
         labelAuthor.setText("Thomas Jones-Low");
        labelCopyright.setText("Copyright 2004, Softstart Services, Inc.");
        labelCompany.setText("<html>The Traveller game in all forms is owned by Far Future Enterprises. <br> Copyright 1977 - 1998 Far Future Enterprises. </html>");
         this.add(labelTitle, null);
         this.add (labelAuthor, null);
         this.add(labelCopyright, null);
         this.add (labelCompany, null);
         
         this.add (Box.createVerticalGlue(), null);
 /*        
         this.add(labelTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 0, 15), 0, 0));
         this.add(labelAuthor, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
         this.add(labelCopyright, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
         this.add(labelCompany, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 5, 15), 0, 0));
 */        
     }
 }
