 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class SpacePanel extends JPanel
 {
   public static final int DEFAULT_SIZE = 35;
   private static final int SCALAR = 1;
   private static final int SHIFT  = 360;
   private Player           player;
   private Location         currentLocation;
   private JLabel           locationName;
 
   public SpacePanel(Player player)
   {
     this.player = player;
     this.setBackground(Color.BLACK);
     locationName = new JLabel("");
     populateSpace();
   }
 
   public void populateSpace()
   {
     this.removeAll();
     this.setLayout(null);
     this.setBounds(0, 0, 720, 720);
     currentLocation = player.getLoc();
     Location[] locations = currentLocation.getChild();
     locationName.setBounds(200, 0, 200, 30);
     JButton back = new JButton("<");
     back.setToolTipText("Return to " + currentLocation.getParent());
     back.addActionListener(new ActionListener()
     {
 
       @Override
       public void actionPerformed(ActionEvent arg0)
       {
         player.setLoc(currentLocation.getParent());
         populateSpace();
       }
 
     });
     back.setBounds(0, 0, 38, 38);
     back.setVisible(true);
     switch (currentLocation.whatAmI())
     {
     case 4:
       locationName.setText("Orbital");
       break;
     case 3:
       locationName.setText("Star System");
       break;
     case 2:
       locationName.setText("Sector");
       break;
     case 1:
       locationName.setText("Cluster");
       back.setVisible(false);
       break;
     default:
       locationName.setText("Error");
     }
     locationName.setBackground(Color.WHITE);
     this.add(locationName);
     this.add(back);
     if (locations != null)
     {
       for (int i = 0; i < locations.length; i++)
       {
         Location loc = locations[i];
         JButton b = new JButton(loc.toString());
         b.setBounds(SCALAR * loc.GetX() + SHIFT, SCALAR * loc.GetY() + SHIFT,
             50, 50);
         b.setBackground(Color.BLUE);
         LocationListener handler = new LocationListener(i);
         b.addActionListener(handler);
         b.setIcon(new ImageIcon(loc.GetPic(DEFAULT_SIZE)));
         b.setOpaque(false);
         b.setBorderPainted(false);
         b.setContentAreaFilled(false);

         b.setToolTipText("<HTML>" + loc.toString() +"<BR />" + "More info here" +"</HTML>");  //not working TODO: fix

         b.setToolTipText("<HTML>"
         				+ "Name: " + loc.toString() 
         				+ "<BR />" 
         				+ "Type: "
         				+ "<BR />"
         				+ "Location: "+ loc.getParent().toString() 
         				+ "</HTML>");

         this.add(b);
       }
       repaint();
     } else
     {
       
       TradingMenu menu = new TradingMenu(player);
       menu.setSize(650, 230);
       menu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       menu.setResizable(false);
       menu.setVisible(true);
       player.setLoc(currentLocation.getParent());
       populateSpace();
     }
   }
 
   private class LocationListener implements ActionListener
   {
 
     private int index;          // The index of the child location
 
     public LocationListener(int index)
     {
       this.setIndex(index);
     }
 
     @Override
     public void actionPerformed(ActionEvent e)
     {
       player.setLoc(currentLocation.getChild(index));
       populateSpace();
     }
 
     public int getIndex()
     {
       return index;
     }
 
     public void setIndex(int index)
     {
       this.index = index;
     }
 
   }
 }
