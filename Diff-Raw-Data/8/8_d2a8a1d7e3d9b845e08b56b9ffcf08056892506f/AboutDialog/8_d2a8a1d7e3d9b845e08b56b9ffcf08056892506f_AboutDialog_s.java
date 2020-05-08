 // Copyright distributed.net 1997-1999 - All Rights Reserved
 // For use in distributed.net projects only.
 // Any other distribution or use of this source violates copyright.
 //
 
 import java.awt.*;
 import java.awt.event.*;
 import java.net.URL;
 
 
 class AboutDialog extends Dialog
 {
     private Image Cow;
 
     class OKButton extends Button implements ActionListener
     {
         public OKButton()
         {
             super("OK");
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             AboutDialog.this.setVisible(false);
         }
     }
 
     AboutDialog(Frame parent)
     {
         super(parent, "About this program", true);
         setSize(340,200);
         setLocation(50,50);
         setResizable(false);
         LayoutManager layout = new GridBagLayout();
         setLayout(layout);
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets.left = 60;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         add(new Label("distributed.net Log Visualizer v1.0"), gbc);
         add(new Label("programmed by:"), gbc);
         add(new Label("Jeff \"Bovine\" Lawson <jlawson@bovine.net>"), gbc);
         add(new Label("William Goo <wgoo@hmc.edu>"), gbc);
         add(new Label("Yves Hetzer <aetsch@gmx.de>"), gbc);
         add(new Label(), gbc);
         gbc.insets.left = 0;
         gbc.fill = GridBagConstraints.NONE;
         add(new OKButton(), gbc);
 
        if (getClass().getClassLoader() != null) {
            URL res = getClass().getClassLoader().getResource("cowhead.gif");
            if (res != null) {
                Cow = getToolkit().getImage(res);
            }
         }
     }
 
     public void paint(Graphics g)
     {
         super.paint(g);
         if (Cow != null) {
             g.drawImage(Cow, 15, 32, this);
         }
     }
 }
